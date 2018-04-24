libraryDependencies += "io.monix" %% "monix" % "3.0.0-RC1"

javaOptions in Test += "-Xss228k"
fork in Test := true