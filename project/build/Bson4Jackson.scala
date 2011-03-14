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
  val mongodb = "org.mongodb" % "mongo-java-driver" % "2.1" % "test"
  
  //omit scala version
  override def outputPath = "target"
  override def moduleID = "bson4jackson"
  
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
}
