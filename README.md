# java2kotlin vendor artifacts

This repository publishes immutable, versioned build artifacts for
[`wstein/java2kotlin`](https://github.com/wstein/java2kotlin)'s vendored
Flix and OpenRewrite forks.

The release and both source forks receive the same `vendor-YYYY.MM.DD.N` tag.
The workflow checks each fork out **at that tag** and records the resolved
commits in the manifest, so the tags are what determine a release's contents.
The `vendor-` prefix keeps these artifact tags distinct from each project's
ordinary source-version tags.

## Publishing a release

Before publishing, create matching `vendor-YYYY.MM.DD.N` tags on both fork
repositories.

**Tag the branch that actually carries the vendored work**, and check what that
is rather than assuming: `wstein/flix-fork` must be tagged at a commit carrying
the `Lexer`/`Parser2` decoupling, currently on `feature/frontend-decoupling`.
Getting this wrong does not fail loudly — release `vendor-2026.07.24.1` was
tagged at a commit without the decoupling and produced a parser jar that no
consumer could use.

```bash
# On wstein/flix-fork -- must carry the Lexer/Parser2 decoupling
git checkout feature/frontend-decoupling
git tag vendor-2026.07.20.1
git push origin vendor-2026.07.20.1

# On wstein/rewrite-fork
git checkout feature/rewrite-flix
git tag vendor-2026.07.20.1
git push origin vendor-2026.07.20.1
```

Verify before dispatching, since the workflow trusts the tags:

```bash
gh api repos/wstein/flix-fork/git/ref/tags/vendor-2026.07.20.1 --jq .object.sha
gh api repos/wstein/rewrite-fork/git/ref/tags/vendor-2026.07.20.1 --jq .object.sha
```

Then run **Publish vendor artifacts** from Actions and provide:

- a release version such as `2026.07.20.1` (must match the tags created above);

The workflow builds one generic, versioned `flix-vendor-*.jar` (the full Flix
compiler), one generic, versioned `flix-parser-*.jar` (the slim, Scala-shaded
front-end parser consumed by `rewrite-flix`), and one generic, versioned
local-Maven bundle on Linux AMD64, plus a checksum manifest. Consumers verify
the generic files; they do not select a platform variant.

### How `flix-parser-*.jar` is built, and why

1. The **facade** in `facade/src` is compiled against the Flix assembly. It is a
   handful of plain-Java types plus a Scala object whose only entry point,
   `org.openrewrite.flix.spi.FlixFrontEnd.parse`, takes and returns Java types.
2. The result is merged into the assembly and shrunk with ProGuard
   (`flix-facade.pro`), seeded only at the facade, which drops the
   Weeder/Resolver/Typer and the JVM backend.
3. Scala is relocated into `org.openrewrite.flix.shaded.scala` with Gradle
   Shadow (`tools/shade`).

The facade is why this works. Shading rewrites the Flix front-end's own public
signatures — `Lexer.run` would take a relocated `Map` — so a consumer compiled
against the real Scala library fails at runtime with `NoSuchMethodError`, and
shading the consumer too is impossible: scalac hardwires `scala.Any` and
refuses to compile against a relocated standard library. Because the boundary
carries only Java types, the relocation is invisible and **consumers need no
Scala dependency at all**.

It also means the jar only has to *run*: nothing compiles against the Scala
classes, so pickle signatures need not survive, and the artifact can be both
aggressively shrunk and shaded. A final workflow step runs the published jar
from plain Java with no scala-library on the classpath, so an unusable artifact
cannot be released again.

Building requires the tagged flix-fork commit to carry the decoupling of
`Lexer.run`/`Parser2.run` from the `Flix` god-object; without it the whole
compiler is reachable and nothing shrinks.

The generated Rewrite bundle contains the `org/openrewrite` subtree of a
Maven local repository. It intentionally does not duplicate third-party Maven
dependencies, which remain resolved normally by the consumer's build tool.
