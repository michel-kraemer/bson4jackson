import sbt._

class Bson4JacksonProject(info: ProjectInfo) extends DefaultProject(info) {
  val jacksonRepo = "Jackson Maven Repository" at "http://repository.codehaus.org/org/codehaus/jackson/"
  val jackson = "org.codehaus.jackson" % "jackson-core-asl" % "1.6.0"
  
  val junit = "junit" % "junit" % "4.8.1" % "test"
  val junitInterface = "com.novocode" % "junit-interface" % "0.4" % "test"
  val jacksonMapper = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.6.0" % "test"
  val mongodb = "org.mongodb" % "mongo-java-driver" % "2.1" % "test"
}
