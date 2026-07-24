# ProGuard rules for the compile-time ("API") Flix parser artifact.
#
# Why this exists as a SECOND shrink, rather than reusing the R8 output:
#
#   R8 removes every Scala pickle signature. Scala 2.13 carries a class's type
#   information in a `@scala.reflect.ScalaSignature` annotation, and R8 drops
#   annotation instances whose annotation *type* was shrunk away -- and then
#   drops them even when that type is explicitly kept and `-keepattributes *`
#   is in force (measured: 0 of 200 sampled ca.uwaterloo.flix.language.ast
#   classes retained ScalaSignature). Without the pickle, scalac reads the Flix
#   classes as plain Java classes, so case-class companions fail to resolve
#   ("Input.VirtualFile is not a value") and the consumer cannot compile.
#
#   ProGuard retains ScalaSignature under the same rules, so it -- not R8 --
#   produces the compile-time artifact. R8 still produces the runtime artifact
#   (flix-parser.pro): it is smaller, and only its bytecode has to run.
#
# Two further constraints, both established by experiment:
#
#   * The retained `scala/**` subset must be excluded. A partial scala-library
#     on the compile classpath makes scalac fail to build its symbol table
#     ("Missing dependency 'object scala.native'"). The consumer supplies a
#     complete org.scala-lang:scala-library instead.
#   * All non-class resources must be excluded. A root `library.properties`
#     alone makes scalac misidentify this jar as the Scala library.
#
# `-dontoptimize` keeps the artifact a faithful ABI of the assembly; nothing
# here is ever executed, so there is nothing to gain from optimising it.

-include flix-parser.pro

-dontoptimize

# The pickle carriers. Keeping the annotation types is necessary (though, for
# R8, not sufficient) for the ScalaSignature annotations to survive shrinking.
-keep class scala.reflect.ScalaSignature { *; }
-keep class scala.reflect.ScalaLongSignature { *; }

-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,Signature,Scala,ScalaSig,ScalaInlineInfo
