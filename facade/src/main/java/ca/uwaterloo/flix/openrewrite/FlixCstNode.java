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

/** An interior node: a Flix {@code TreeKind} and its children. */
public final class FlixCstNode implements FlixCstElement {

    private final String kind;
    private final List<FlixCstElement> children;
    private final String detail;

    public FlixCstNode(String kind, List<FlixCstElement> children, String detail) {
        this.kind = kind;
        this.children = Collections.unmodifiableList(children);
        this.detail = detail;
    }

    /**
     * The {@code TreeKind}, qualified by its enclosing group and dotted --
     * {@code "Decl.Def"}, {@code "Expr.Binary"}, {@code "Pattern.Tuple"}. Qualified
     * because Flix reuses simple names: {@code Tuple} exists under {@code Expr},
     * {@code Pattern} and {@code Type} alike.
     */
    public String getKind() {
        return kind;
    }

    public List<FlixCstElement> getChildren() {
        return children;
    }

    /** The diagnostic on an {@code ErrorTree}, or null for every other kind. */
    public String getDetail() {
        return detail;
    }
}
