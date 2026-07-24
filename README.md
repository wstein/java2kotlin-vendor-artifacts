# java2kotlin vendor artifacts

This repository publishes immutable, versioned build artifacts for
[`wstein/java2kotlin`](https://github.com/wstein/java2kotlin)'s vendored
Flix and OpenRewrite forks.

The release and both source forks receive the same `vendor-YYYY.MM.DD.N` tag.
The workflow resolves each fork's `feature/java2kotlin` head at dispatch time,
creates immutable tags pointing to those exact commits, and records them in
the manifest. The `vendor-` prefix keeps these artifact tags distinct from
each project's ordinary source-version tags.

## Publishing a release

Before publishing, create matching `vendor-YYYY.MM.DD.N` tags on both fork
repositories, pointing to the tip of the `feature/java2kotlin` branch:

```bash
# On wstein/flix-fork
git checkout feature/java2kotlin
git tag vendor-2026.07.20.1
git push origin vendor-2026.07.20.1

# On wstein/rewrite-fork
git checkout feature/java2kotlin
git tag vendor-2026.07.20.1
git push origin vendor-2026.07.20.1
```

Then run **Publish vendor artifacts** from Actions and provide:

- a release version such as `2026.07.20.1` (must match the tags created above);

The workflow builds one generic, versioned `flix-vendor-*.jar` (the full Flix
compiler), one generic, versioned `flix-parser-*.jar` (the slim, R8-shrunk and
Scala-shaded front-end parser consumed by `rewrite-flix`), and one generic,
versioned local-Maven bundle on Linux AMD64, plus a checksum manifest.
Consumers verify the generic files; they do not select a platform variant.

The `flix-parser-*.jar` is produced by shrinking the Flix assembly to the
decoupled front-end (`Lexer` + `Parser2` + the `SyntaxTree`/`Token` API) with
R8 (`flix-parser.pro`), then relocating Scala 2.13 into
`org.openrewrite.flix.shaded.scala` with Gradle Shadow (`tools/shade`). It
depends on the flix fork's `feature/java2kotlin` branch carrying the
Flix-decoupling of `Lexer.run`/`Parser2.run`.

The generated Rewrite bundle contains the `org/openrewrite` subtree of a
Maven local repository. It intentionally does not duplicate third-party Maven
dependencies, which remain resolved normally by the consumer's build tool.
