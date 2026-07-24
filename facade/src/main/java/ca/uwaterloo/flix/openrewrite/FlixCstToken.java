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
 * A leaf token. Carries source offsets rather than text, so the consumer slices every
 * character -- including the whitespace between tokens -- from the original source.
 */
public final class FlixCstToken implements FlixCstElement {

    private final String kind;
    private final int start;
    private final int end;

    public FlixCstToken(String kind, int start, int end) {
        this.kind = kind;
        this.start = start;
        this.end = end;
    }

    /** The {@code TokenKind} name, for example {@code "KeywordDef"} or {@code "CommentLine"}. */
    public String getKind() {
        return kind;
    }

    /** Absolute, zero-based offset of the first character. */
    public int getStart() {
        return start;
    }

    /** Absolute, zero-based offset one past the last character. */
    public int getEnd() {
        return end;
    }
}
