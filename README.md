# java2kotlin vendor artifacts

This repository publishes immutable, versioned build artifacts for
[`wstein/java2kotlin`](https://github.com/wstein/java2kotlin)'s vendored
Flix and OpenRewrite forks.

Its tags are deliberately independent of the source repositories. A release
tag has the form `vendor-YYYY.MM.DD.N`; it identifies a manifest containing
the exact 40-character commits used for both source builds. It does **not**
tag either source fork and therefore cannot affect their source-versioning
tools.

## Publishing a release

Run **Publish vendor artifacts** from Actions and provide:

- a release version such as `2026.07.20.1`;
- the immutable Flix fork commit SHA; and
- the immutable Rewrite fork commit SHA.

The workflow builds both inputs on Linux AMD64, Linux ARM64, and Apple Silicon
macOS, then attaches per-platform Flix JAR and local-Maven bundles plus a
checksum manifest to the GitHub Release. Consumers must select an asset by
platform and verify it against the matching manifest before use.

The generated Rewrite bundle contains the `org/openrewrite` subtree of a
Maven local repository. It intentionally does not duplicate third-party Maven
dependencies, which remain resolved normally by the consumer's build tool.
