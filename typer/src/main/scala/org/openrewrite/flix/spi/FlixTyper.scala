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
import scala.jdk.CollectionConverters._

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
   * A reserved directory the source under test is placed under.
   *
   * Inputs are keyed by their path string, and the standard library is added under
   * bare module paths -- `List.flix`, `String.flix`, `Option.flix`. A source that
   * happened to be named after a library module would key to the same string and
   * silently REPLACE that module, so its type would then be undefined. Namespacing the
   * source under a directory the library never uses removes the collision; the
   * directory is cosmetic, because a Flix file's path does not determine its module (a
   * `mod` declaration does).
   */
  private val InputRoot = "rewrite-flix-input"

  /**
   * Type checks `source` and returns the types of the expressions belonging to it.
   *
   * Expressions from the standard library are excluded: they are typed as a consequence
   * of compiling the program, but they are not what the caller asked about.
   */
  def typeCheck(path: String, source: String): FlixTypeCheckResult = {
    implicit val sctx: SecurityContext = SecurityContext.Default

    // See InputRoot: the source is placed under a reserved directory so a file named
    // after a standard-library module cannot clobber it.
    val inputPath = InputRoot + "/" + path

    val flix = new Flix()
    flix.addVirtualPath(Paths.get(inputPath), source)
    val (rootOpt, errors) = flix.check()

    // Transform once and expose as a Java view. `asJava` wraps the Scala collection
    // in O(1) rather than copying it into a fresh java.util.List, and the map has to
    // run either way, so nothing is copied twice. The elements are FlixTypedExpression
    // and String -- plain data -- so the wrapper keeps no compiler AST alive.
    val diagnostics: ju.List[String] =
      errors.map(e => e.getClass.getSimpleName + ": " + e.summary).asJava

    // Filter DURING the walk rather than collecting all expressions and filtering
    // after. A five-line program types tens of thousands of expressions, almost all of
    // them the standard library's; materialising them only to drop them is the
    // expensive part. Keeping just the matches allocates a handful instead.
    val kept = mutable.ArrayBuffer.empty[FlixTypedExpression]
    rootOpt.foreach(root => collect(root.defs, inputPath, kept))

    new FlixTypeCheckResult(kept.asJava, diagnostics)
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
  private def collect(any: Any, inputPath: String,
                      out: mutable.ArrayBuffer[FlixTypedExpression]): Unit = any match {
    case e: TypedAst.Expr =>
      // Keep only expressions belonging to the source under test; the rest are the
      // standard library's, typed as a side effect of compiling the program.
      if (belongsTo(e.loc, inputPath)) {
        out += new FlixTypedExpression(
          e.loc.start.lineOneIndexed, e.loc.start.colOneIndexed,
          e.loc.end.lineOneIndexed, e.loc.end.colOneIndexed,
          e.tpe.toString, e.eff.toString)
      }
      e.productIterator.foreach(collect(_, inputPath, out))
    case _: Type => ()
    case _: SourceLocation => ()
    case m: scala.collection.Map[_, _] => m.foreach { case (_, v) => collect(v, inputPath, out) }
    case i: Iterable[_] => i.foreach(collect(_, inputPath, out))
    case p: Product => p.productIterator.foreach(collect(_, inputPath, out))
    case _ => ()
  }

  /**
   * Whether a location came from the source under test rather than the standard
   * library. An exact match on the namespaced path: the library is never added under
   * InputRoot, so nothing of its own can match.
   */
  private def belongsTo(loc: SourceLocation, inputPath: String): Boolean =
    loc.source.name == inputPath
}
