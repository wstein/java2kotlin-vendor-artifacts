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
package org.openrewrite.flix.spi;

/**
 * One expression's inferred type, located by the span it occupies in the source.
 *
 * <p>Positions are one-indexed line and column, which is what Flix's own
 * {@code SourceLocation} carries. The consumer converts them to offsets against the
 * source it already holds; shipping offsets from here would mean re-deriving them
 * from the same text on this side of the boundary for no gain.
 *
 * <p>Types are rendered as strings rather than as a structured model. The structured
 * form is {@code ca.uwaterloo.flix.language.ast.Type}, a Scala type, and exposing it
 * would put Scala back on the boundary -- the exact thing the facade exists to keep
 * off it.
 */
public final class FlixTypedExpression {

    private final int beginLine;
    private final int beginColumn;
    private final int endLine;
    private final int endColumn;
    private final String type;
    private final String effect;

    public FlixTypedExpression(int beginLine, int beginColumn, int endLine, int endColumn,
                               String type, String effect) {
        this.beginLine = beginLine;
        this.beginColumn = beginColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.type = type;
        this.effect = effect;
    }

    public int getBeginLine() {
        return beginLine;
    }

    public int getBeginColumn() {
        return beginColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    /** The inferred type, as Flix renders it. */
    public String getType() {
        return type;
    }

    /** The inferred effect; {@code Pure} when the expression performs none. */
    public String getEffect() {
        return effect;
    }
}
