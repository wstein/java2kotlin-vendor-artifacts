# R8 keep rules for the slim Flix parser artifact.
#
# Seeds shrinking at the front-end entry points and the CST/token API that the
# OpenRewrite consumer (rewrite-flix) walks. Everything not reachable from these
# roots -- the entire Weeder/Resolver/Typer/backend and their exclusive deps --
# is removed by dead-code elimination. The front-end must first be decoupled
# from ca.uwaterloo.flix.api.Flix (see the flix-fork feature/java2kotlin branch)
# so these entry points are reachable without a compiler instance.
#
# Shrink only: names are preserved (-dontobfuscate) for the consumer and for
# Scala reflection.

-dontobfuscate
-dontnote
-dontwarn
-ignorewarnings
-keepattributes *

# --- Entry points the consumer calls ----------------------------------------
-keep class ca.uwaterloo.flix.language.phase.Lexer { *; }
-keep class ca.uwaterloo.flix.language.phase.Lexer$ { *; }
-keep class ca.uwaterloo.flix.language.phase.Parser2 { *; }
-keep class ca.uwaterloo.flix.language.phase.Parser2$ { *; }

# --- CST / token API the consumer walks -------------------------------------
-keep class ca.uwaterloo.flix.language.ast.SyntaxTree { *; }
-keep class ca.uwaterloo.flix.language.ast.SyntaxTree$** { *; }
-keep class ca.uwaterloo.flix.language.ast.Token { *; }
-keep class ca.uwaterloo.flix.language.ast.Token$** { *; }
-keep class ca.uwaterloo.flix.language.ast.TokenKind { *; }
-keep class ca.uwaterloo.flix.language.ast.TokenKind$** { *; }
-keep class ca.uwaterloo.flix.language.ast.SourceLocation { *; }
-keep class ca.uwaterloo.flix.language.ast.SourceLocation$** { *; }
-keep class ca.uwaterloo.flix.language.ast.SourcePosition { *; }
-keep class ca.uwaterloo.flix.language.ast.ChangeSet { *; }
-keep class ca.uwaterloo.flix.language.ast.ChangeSet$** { *; }
-keep class ca.uwaterloo.flix.language.ast.ReadAst { *; }
-keep class ca.uwaterloo.flix.language.ast.ReadAst$** { *; }
-keep class ca.uwaterloo.flix.language.ast.shared.** { *; }

# Scala runtime bits reached reflectively / via invokedynamic.
-keep class scala.runtime.** { *; }
-keepclassmembers class * { ** MODULE$; }
