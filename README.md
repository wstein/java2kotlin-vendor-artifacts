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
macOS. These are compatibility gates, not separate distributions: the release
contains one generic `flix.jar` and one generic local-Maven bundle built on
Linux AMD64, plus a checksum manifest. Consumers verify the generic files;
they do not select a platform variant.

The generated Rewrite bundle contains the `org/openrewrite` subtree of a
Maven local repository. It intentionally does not duplicate third-party Maven
dependencies, which remain resolved normally by the consumer's build tool.
