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
package ca.uwaterloo.flix.openrewrite;

/**
 * A node or a token in the concrete syntax tree handed to consumers.
 *
 * <p>Every type in this package is deliberately plain Java. The parser jar shades
 * Scala into a private namespace so it can coexist with another Scala version on one
 * classpath, and shading rewrites the Flix front-end's own public signatures --
 * {@code Lexer.run} would take a relocated {@code Map}. A consumer compiled against
 * real Scala then fails at runtime with {@code NoSuchMethodError}, and it cannot be
 * fixed by shading the consumer too, because scalac hardwires {@code scala.Any} and
 * refuses to compile against a relocated standard library.
 *
 * <p>Keeping the boundary free of Scala types removes the problem entirely: nothing
 * shading touches appears in these signatures, so the consumer needs no Scala at all.
 */
public interface FlixCstElement {
}
