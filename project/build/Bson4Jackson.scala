// Copyright 2010-2011 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import sbt._
import com.weiglewilczek.bnd4sbt.BNDPlugin

class Bson4JacksonProject(info: ProjectInfo) extends DefaultProject(info) with BNDPlugin {
  //include license
  def extraResources = "LICENSE.txt"
  override def mainResources = super.mainResources +++ extraResources
  override def testResources = super.testResources +++ extraResources
  
  //snapshot dependencies:
  //val jacksonRepo = Resolver.url("Jackson Maven Repository",
  //  new java.net.URL("http://snapshots.repository.codehaus.org/"))(Patterns(
  //  "[organisation]/[module]/[revision]-SNAPSHOT/[artifact]-[revision](-[timestamp]).[ext]"))
  //val jackson = "org.codehaus.jackson" % "jackson-core-asl" % "1.7.0" extra ("timestamp" -> "20110105.013252-7")
  //val jacksonMapper = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.7.0" extra ("timestamp" -> "20110105.013252-7")
  
  //stable dependencies
  val jackson = "org.codehaus.jackson" % "jackson-core-asl" % "1.7.4"
  val jacksonMapper = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.7.4"
  
  val junit = "junit" % "junit" % "4.8.1" % "test"
  val junitInterface = "com.novocode" % "junit-interface" % "0.4" % "test"
  val mongodb = "org.mongodb" % "mongo-java-driver" % "2.5.3" % "test"
  
  //omit scala version
  override def outputPath = "target"
  override def moduleID = "bson4jackson"
  
  //change names of source artifacts
  override def packageSrcJar = defaultJarPath("-sources.jar")
  override def packageTestSrcJar = defaultJarPath("-test-sources.jar")
  
  //change names of doc artifacts
  override def packageDocsJar = defaultJarPath("-javadoc.jar")
  
  //configure OSGi bundle
  override def bndBundleName = "bson4jackson"
  override def bndBundleVendor = Some("Michel Kraemer")
  override def bndExportPackage = Seq(
    "de.undercouch.bson4jackson",
    "de.undercouch.bson4jackson.io",
    "de.undercouch.bson4jackson.types"
  ).map(_ + ";version=" + projectVersion.value)
  override def bndPrivatePackage = Seq()
  override def bndIncludeResource = Seq(extraResources)
  
  //add information to Maven pom.xml
  override def pomExtra =
    <name>bson4jackson</name> ++
    <description>A pluggable BSON generator and parser for Jackson JSON processor.</description> ++
    <url>http://www.michel-kraemer.de</url> ++
    <licenses>
      <license>
        <name>The Apache Software License, Version 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        <distribution>repo</distribution>
      </license>
    </licenses> ++
    <scm>
      <connection>
        scm:git:git://github.com/michel-kraemer/bson4jackson.git
      </connection>
      <url>scm:git:git://github.com/michel-kraemer/bson4jackson.git</url>
      <developerConnection>
        scm:git:git://github.com/michel-kraemer/bson4jackson.git
      </developerConnection>
    </scm> ++
    <developers>
      <developer>
        <id>michel-kraemer</id>
        <name>Michel Kraemer</name>
        <email>michel@undercouch.de</email>
      </developer>
    </developers>
  
  //never include other repositories in the pom.xml (needed for Maven central)
  override def pomIncludeRepository(repo: MavenRepository) = false
}
