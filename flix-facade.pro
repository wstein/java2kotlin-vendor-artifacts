# Keep rules for the facade-based parser jar.
#
# The only entry point is the pure-Java facade; everything reachable from it --
# Lexer, Parser2, the CST -- is retained transitively, and the rest (Weeder,
# Resolver, Typer, JVM backend) is dropped.
#
# Note there is no need to preserve Scala pickle signatures here. Consumers compile
# against the Java facade, never against the Scala classes, so the jar only has to
# RUN. That is what lets it be both aggressively shrunk and shaded.
-dontobfuscate
-dontnote
-dontwarn
-ignorewarnings
-keepattributes *

-keep class ca.uwaterloo.flix.openrewrite.** { *; }
-keep class scala.runtime.** { *; }
-keepclassmembers class * { ** MODULE$; }
