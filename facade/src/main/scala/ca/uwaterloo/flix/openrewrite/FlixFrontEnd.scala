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
package ca.uwaterloo.flix.openrewrite

import ca.uwaterloo.flix.language.ast.shared.{AvailableClasses, Input, SecurityContext, Source}
import ca.uwaterloo.flix.language.ast.{ChangeSet, ReadAst, SyntaxTree, Token}
import ca.uwaterloo.flix.language.phase.{Lexer, Parser2}

import java.nio.file.Paths
import java.{util => ju}
import scala.collection.immutable.Map

/**
 * The parser jar's public entry point.
 *
 * Every signature here uses only Java types, which is what makes the jar usable after
 * shading. Relocating `scala` rewrites the Flix front-end's own public signatures --
 * `Lexer.run` would take a relocated `Map` -- so a consumer compiled against real Scala
 * fails at runtime with `NoSuchMethodError`. Shading the consumer's compile classpath
 * cannot fix that either: scalac hardwires `scala.Any`/`scala.Predef` and refuses to
 * compile against a relocated standard library.
 *
 * With the boundary free of Scala types, the shading is invisible to consumers and they
 * need no Scala dependency at all.
 *
 * Requires the A1 decoupling on the flix fork's `feature/java2kotlin` branch: `Lexer.run`
 * and `Parser2.run` must not take an implicit `Flix`, or the whole compiler -- including
 * the JVM backend -- is reachable and the jar cannot be slimmed.
 */
object FlixFrontEnd {

  /** Lex and parse `source`, returning its CST and any diagnostics. */
  def parse(path: String, source: String): FlixParseResult = {
    val input = Input.VirtualFile(Paths.get(path), source, SecurityContext.Default)
    val src = Source.fromString(input, source)
    val root = ReadAst.Root(Map(src -> (())), AvailableClasses.empty)

    val (tokens, lexErrors) = Lexer.run(root, Map.empty, ChangeSet.Everything)
    val (trees, parseErrors) = Parser2.run(tokens, SyntaxTree.empty, ChangeSet.Everything)

    val diagnostics = new ju.ArrayList[String]()
    lexErrors.foreach(e => diagnostics.add(e.toString))
    parseErrors.foreach(e => diagnostics.add(e.toString))

    val unit = trees.units.values.headOption.getOrElse(
      throw new IllegalStateException(s"Flix parser produced no compilation unit for $path"))

    new FlixParseResult(convertTree(unit), diagnostics)
  }

  private def convertTree(tree: SyntaxTree.Tree): FlixCstNode = {
    val children = new ju.ArrayList[FlixCstElement](tree.children.length)
    tree.children.foreach {
      case t: SyntaxTree.Tree => children.add(convertTree(t))
      case t: Token => children.add(convertToken(t))
      case other =>
        throw new IllegalStateException(s"Unexpected SyntaxTree child: ${other.getClass.getName}")
    }
    new FlixCstNode(kindName(tree.kind), children, detailOf(tree.kind))
  }

  private def convertToken(token: Token): FlixCstToken =
    new FlixCstToken(simpleName(token.kind), token.startIndex, token.endIndex)

  /**
   * The diagnostic carried by an `ErrorTree`, or null for every other kind. Flix's
   * parser records a failure in the tree and carries on, so this is the only place the
   * reason survives.
   */
  private def detailOf(kind: SyntaxTree.TreeKind): String = kind match {
    case SyntaxTree.TreeKind.ErrorTree(error) => error.toString
    case _ => null
  }

  /**
   * Renders a `TreeKind` as a dotted, group-qualified name -- `Decl.Def`, `Expr.Binary`.
   * Derived from the class name rather than `toString`, because `toString` on a case
   * object yields only its simple name and Flix reuses simple names across groups.
   */
  private def kindName(kind: SyntaxTree.TreeKind): String = {
    val name = kind.getClass.getName
    val marker = "TreeKind$"
    val idx = name.indexOf(marker)
    if (idx < 0) simpleName(kind)
    else name.substring(idx + marker.length).stripSuffix("$").replace('$', '.')
  }

  private def simpleName(value: Any): String = {
    val name = value.getClass.getName.stripSuffix("$")
    name.substring(math.max(name.lastIndexOf('$'), name.lastIndexOf('.')) + 1)
  }
}
