/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.flix.spi

import ca.uwaterloo.flix.api.Flix
import ca.uwaterloo.flix.language.ast.shared.SecurityContext
import ca.uwaterloo.flix.language.ast.{SourceLocation, Type, TypedAst}

import java.nio.file.Paths
import java.{util => ju}
import scala.collection.mutable

/**
 * Type checking, behind a pure-Java boundary.
 *
 * Unlike the parser facade this needs the WHOLE front-end -- Weeder, Desugar, Namer,
 * Resolver, Deriver and finally Typer -- plus the standard library, because Flix types a
 * program rather than a file. It therefore ships in its own, much larger artifact, and
 * the parser jar stays slim.
 *
 * Two consequences a consumer has to design around, both properties of Flix rather than
 * of this code:
 *
 *   - Typing is whole-program and expensive. A five-line file yields tens of thousands
 *     of typed expressions, nearly all of them the standard library's, because the
 *     library is compiled every time. Results are filtered to the requested source here,
 *     but the work is not avoided: this belongs at source-set granularity with caching,
 *     not on a per-file parse path.
 *   - Typing is all-or-nothing. A source that does not resolve yields diagnostics and no
 *     types at all, so a consumer must cope with having none -- code mid-refactor
 *     frequently does not compile.
 */
object FlixTyper {

  /**
   * Type checks `source` and returns the types of the expressions belonging to it.
   *
   * Expressions from the standard library are excluded: they are typed as a consequence
   * of compiling the program, but they are not what the caller asked about.
   */
  def typeCheck(path: String, source: String): FlixTypeCheckResult = {
    implicit val sctx: SecurityContext = SecurityContext.Default

    val flix = new Flix()
    flix.addVirtualPath(Paths.get(path), source)
    val (rootOpt, errors) = flix.check()

    val diagnostics = new ju.ArrayList[String]()
    errors.foreach(e => diagnostics.add(e.getClass.getSimpleName + ": " + e.summary))

    val expressions = new ju.ArrayList[FlixTypedExpression]()
    rootOpt.foreach { root =>
      val collected = mutable.ArrayBuffer.empty[TypedAst.Expr]
      collect(root.defs, collected)
      collected.foreach { e =>
        if (belongsTo(e.loc, path)) {
          expressions.add(new FlixTypedExpression(
            e.loc.start.lineOneIndexed, e.loc.start.colOneIndexed,
            e.loc.end.lineOneIndexed, e.loc.end.colOneIndexed,
            e.tpe.toString, e.eff.toString))
        }
      }
    }
    new FlixTypeCheckResult(expressions, diagnostics)
  }

  /**
   * Walks anything and collects every typed expression.
   *
   * `TypedAst.Expr` is a sealed trait extending `Product` and declaring `tpe`, `eff` and
   * `loc` on the trait itself, so a generic walk reaches every expression without a case
   * for each of the ~90 forms -- and keeps reaching them when upstream adds one.
   *
   * `Type` and `SourceLocation` are Products too and are not descended into: they hold no
   * expressions, and walking them is wasted work on a hot path.
   */
  private def collect(any: Any, out: mutable.ArrayBuffer[TypedAst.Expr]): Unit = any match {
    case e: TypedAst.Expr =>
      out += e
      e.productIterator.foreach(collect(_, out))
    case _: Type => ()
    case _: SourceLocation => ()
    case m: scala.collection.Map[_, _] => m.foreach { case (_, v) => collect(v, out) }
    case i: Iterable[_] => i.foreach(collect(_, out))
    case p: Product => p.productIterator.foreach(collect(_, out))
    case _ => ()
  }

  /** Whether a location came from the source the caller asked about. */
  private def belongsTo(loc: SourceLocation, path: String): Boolean =
    loc.source.name == path || loc.source.name.endsWith("/" + path)
}
