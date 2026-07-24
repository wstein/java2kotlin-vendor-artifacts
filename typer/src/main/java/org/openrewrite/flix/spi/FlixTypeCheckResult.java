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

import java.util.Collections;
import java.util.List;

/**
 * The outcome of type checking one source.
 *
 * <p>Typing is all-or-nothing in Flix: a source that does not resolve produces errors
 * and no typed tree, rather than partial types. So {@link #isTyped()} can be false with
 * a complete list of diagnostics and no expressions at all, and a consumer must handle
 * that rather than assume types are available -- code being refactored frequently does
 * not compile.
 */
public final class FlixTypeCheckResult {

    private final List<FlixTypedExpression> expressions;
    private final List<String> diagnostics;

    public FlixTypeCheckResult(List<FlixTypedExpression> expressions, List<String> diagnostics) {
        this.expressions = Collections.unmodifiableList(expressions);
        this.diagnostics = Collections.unmodifiableList(diagnostics);
    }

    /** Whether type checking succeeded and produced types. */
    public boolean isTyped() {
        return diagnostics.isEmpty();
    }

    /** Every typed expression belonging to the requested source, in no particular order. */
    public List<FlixTypedExpression> getExpressions() {
        return expressions;
    }

    public List<String> getDiagnostics() {
        return diagnostics;
    }
}
