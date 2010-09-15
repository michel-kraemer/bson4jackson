/**
 * Copyright (c) 2010 WeigleWilczek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.bnd4sbt

import sbt.{ DefaultProject, MavenStyleScalaPaths, Path, PathFinder}
import sbt.Configurations._

/**
 * Execution environments available for bnd4sbt. As Scala relies on Java 5, only Java 5 and later are supported.
 */
object ExecutionEnvironment extends Enumeration {

  /**
   * Type alias for the enumeration values. Use <code>import ExecutionEnvironment._</code> to make the
   * enumeration values available under type <code>ExecutionEnvironment</code>.
   */
	type ExecutionEnvironment = Value

  /**
   * Execution environment for Java 5.
   */
  val Java5 = Value("J2SE-1.5")

  /**
   * Execution environment for Java 6.
   */
  val Java6 = Value("JavaSE-1.6")
}

/**
 * Properties for BND with sensible defaults. 
 */
private[bnd4sbt] trait BNDPluginProperties extends ProjectAccessor {
  import ExecutionEnvironment._

  /**
   * The value for <code>Bundle-SymbolicName</code>. Defaults to <i>projectOrganization.projectName</i>
   * with duplicate subsequences removed, e.g. "a.b.c" + "c-d" => "a.b.c.d".
   * Recognized namespace separators are "." and "-".
   */
  protected def bndBundleSymbolicName: String = {
    def split(s: String) = s split """\.|-""" match {
      case a if a.size > 1 => a
      case _ => s split "-"
    }
    val organization = split(project.organization).toList
    val name = split(project.name).toList
    def concat(nameTaken: List[String], nameDropped: List[String]): List[String] = nameTaken match {
      case Nil => organization ::: nameDropped
      case _ if organization endsWith nameTaken => organization ::: nameDropped
      case _ => concat(nameTaken.init, nameTaken.last :: nameDropped)
    }
    concat(name, Nil) mkString "."
  }

  /**
   * The value for <code>Bundle-Name</code>. Defaults to <code>bndBundleSymbolicName</code>.
   */
  protected def bndBundleName: String = bndBundleSymbolicName

  /**
   * The value for <code>Bundle-Version</code>. Defaults to this project's version.
   */
  protected def bndBundleVersion: String = project.version.toString

  /**
   * The value for <code>Fragment-Host</code>, wrapped in an <code>Option</code>. Defaults to <code>None</code>,
   * i.e. no fragment is defined.
   */
  protected def bndFragmentHost: Option[String] = None

  /**
   * The value for <code>Bundle-Vendor</code>, wrapped in an <code>Option</code>. Defaults to <code>None</code>,
   * i.e. no vendor is defined.
   */
  protected def bndBundleVendor: Option[String] = None

  /**
   * The value for <code>Bundle-License</code>, wrapped in an <code>Option</code>. Defaults to <code>None</code>,
   * i.e. no license is defined.
   */
  protected def bndBundleLicense: Option[String] = None

  /**
   * The value for <code>Bundle-RequiredExecutionEnvironment</code>. Defaults to empty <code>Set</code>,
   * i.e. no execution environments are defined.
   */
  protected def bndExecutionEnvironment: Set[ExecutionEnvironment] = Set.empty

  /**
   * The value for <code>Private-Package</code>. Defaults to <code>"%s.*".format(bndBundleSymbolicName) :: Nil</code>,
   * i.e. contains the root package and all subpackages of this project.
   */
  protected def bndPrivatePackage: Seq[String] = "%s.*".format(bndBundleSymbolicName) :: Nil

  /**
   * The value for <code>Export-Package</code>. Defaults to empty <code>Seq</code>, i.e. nothing is exported.
   */
  protected def bndExportPackage: Seq[String] = Nil

  /**
   * The value for <code>Import-Package</code>. Defaults to
   * <code>""scala.*;version=[%1$s,%1$s]".format(project.buildScalaVersion) ::  "*" :: Nil</code>,
   * i.e. Scala is imported only in the exact version which is used to build this project.
   */
  protected def bndImportPackage: Seq[String] =
    "scala.*;version=\"[%1$s,%1$s]\"".format(project.buildScalaVersion) ::  "*" :: Nil

  /**
   * The value for <code>Dynamic-ImportPackage</code>. Defaults to empty <code>Seq</code>,
   * i.e. nothing is imported dynamically.
   */
  protected def bndDynamicImportPackage: Seq[String] = Nil

  /**
   * The value for <code>Require-Bundle</code>. Defaults to empty <code>Seq</code>, i.e. no bundles are required.
   */
  protected def bndRequireBundle: Seq[String] = Nil

  /**
   * The value for <code>Bundle-Actiavtor</code>, wrapped in an <code>Option</code>. Defaults to <code>None</code>,
   * i.e. no activator is defined.
   */
  protected def bndBundleActivator: Option[String] = None

  /**
   * The value for <code>Include-Resource</code>. Defaults to the main resources of this project.
   */
  protected def bndIncludeResource: Seq[String] = project.mainResourcesPath.absolutePath :: Nil

  /**
   * Should the dependencies be embedded? Defaults to <code>false</code>.
   */
  protected def bndEmbedDependencies = false

  /**
   * The value for the <code>versionpolicy</code> directive, wrapped in an <code>Option</code>.
   * Defaults to <code>None</code>, i.e. no version policy is defined.
   */
  protected def bndVersionPolicy: Option[String] = None

  /**
   * Should the <code>nouses</code> directive be applied? Defaults to <code>false</code>.
   */
  protected def bndNoUses = false

  /**
   * The file name as part of <code>bndOutput</code>. Defaults to this project's <code>defaultJarName</code>.
   * <b>Attention</b>: Better not change this, but the SBT default property <code>artifactBaseName</code>!
   */
  protected def bndFileName: String = project.defaultJarName

  /**
   * The output path used by BND. Defaults to the <code>outputPath</code> of this project plus the value of
   * <code>bndFileName</code>.
   * <b>Attention</b>: Better not change this, but the appropriate SBT default properties!
   */
  protected def bndOutput: Path = project.outputPath / bndFileName

  /**
   * The classpath used by BND. Defaults to the <code>projectClasspath(Compile)</code> plus
   * <code>providedClasspath</code> of this project.
   * <b>Attention</b>: Don't mistake this for the Bundle-Classpath!
   */
  protected def bndClasspath: PathFinder = project.projectClasspath(Compile) +++ project.providedClasspath

  private[bnd4sbt] def bundleClasspath =
    if (bndEmbedDependencies) Set(".") ++ (project.publicClasspath.get filter { !_.isDirectory } map { _.name })
    else Set(".")

  private[bnd4sbt] def resourcesToBeIncluded = {
    val classpathResources = project.publicClasspath.get filter { _ != project.mainCompilePath } map { _.absolutePath }
    val resourceResources = project.dependencies flatMap { 
      _ match {
        case d: MavenStyleScalaPaths => Some(d.mainResourcesPath.absolutePath)
        case _ => None
      }
    }
    if (bndEmbedDependencies) bndIncludeResource ++ classpathResources ++ resourceResources
    else bndIncludeResource
  }
}

/**
 * Gives access to a SBT project.
 */
private[bnd4sbt] trait ProjectAccessor {

  /**
   * The SBT project.
   */
  protected[bnd4sbt] val project: DefaultProject
}
