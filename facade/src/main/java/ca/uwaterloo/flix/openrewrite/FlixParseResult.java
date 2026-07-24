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

import java.util.Collections;
import java.util.List;

/**
 * The outcome of parsing one source.
 *
 * <p>Diagnostics are returned rather than thrown: Flix's parser is error tolerant and
 * still produces a complete, offset-covering tree for faulty input.
 */
public final class FlixParseResult {

    private final FlixCstNode tree;
    private final List<String> diagnostics;

    public FlixParseResult(FlixCstNode tree, List<String> diagnostics) {
        this.tree = tree;
        this.diagnostics = Collections.unmodifiableList(diagnostics);
    }

    public FlixCstNode getTree() {
        return tree;
    }

    public List<String> getDiagnostics() {
        return diagnostics;
    }
}
