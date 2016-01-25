# BSON for Jackson [![Build Status](https://travis-ci.org/michel-kraemer/bson4jackson.svg?branch=master)](https://travis-ci.org/michel-kraemer/bson4jackson) [![Apache License, Version 2.0](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.undercouch/bson4jackson/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.undercouch/bson4jackson)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/de.undercouch/bson4jackson/badge.svg)](http://www.javadoc.io/doc/de.undercouch/bson4jackson)

This library adds support for [BSON](http://bsonspec.org) to the
[Jackson JSON processor](https://github.com/FasterXML/jackson).

BSON is a binary representation of [JSON](http://json.org/). It has
gained prominence by its usage as the main exchange and persistence
format of the document-oriented database management system
[MongoDB](http://www.mongodb.org).

## Usage

The bson4jackson library integrates completely into Jackson. Please have
a look at the [Jackson wiki](http://wiki.fasterxml.com/JacksonDocumentation)
for a complete description. For more information you may also have a
look at the [bson4jackson tutorial](http://www.michel-kraemer.com/binary-json-with-bson4jackson).

The BSON specification defines some additional types not available in
the original JSON specification. These types are mapped to simple
wrapper objects.

## Download

bson4jackson binaries are available from the
[GitHub Releases page](https://github.com/michel-kraemer/bson4jackson/releases>).

You may also use [Maven](http://maven.apache.org/) to download bson4jackson:

```xml
<dependencies>
    <dependency>
        <groupId>de.undercouch</groupId>
        <artifactId>bson4jackson</artifactId>
        <version>2.7.0</version>
    </dependency>
</dependencies>
```

If you're using [Gradle](http://www.gradle.org/), you may add the
following snippet to your `build.gradle`:

```gradle
dependencies {
    compile 'de.undercouch:bson4jackson:2.7.0'
}
```

For [sbt](http://code.google.com/p/simple-build-tool/),
add the following line to your project::

```sbt
val bson4jackson = "de.undercouch" % "bson4jackson" % "2.7.0"
```

For [buildr](http://buildr.apache.org/) use the following snippet::

```ruby
compile.with 'de.undercouch:bson4jackson:jar:2.7.0'
```

## Compatibility

bson4jackson 2.x is compatible to Jackson 2.x and higher. Due to some
compatibility issues both libraries' major and minor version numbers
have to match. That means you have to use at least bson4jackson 2.1
if you use Jackson 2.1, bson4jackson 2.2 if you use Jackson 2.2, etc.
I will try to keep bson4jackson up to date. If there is a compatibility
issue I will update bson4jackon, usually within a couple of days after
the new Jackson version has been released.

Here's the compatibility matrix for the current library versions:

Library                | Jackson 2.7.x | Jackson 2.6.x | Jackson 2.5.x | Jackson 2.4.x | Jackson 2.3.x
:----------------------|:-------------:|:-------------:|:-------------:|:-------------:|:-------------:
**bson4jackson 2.7.x** |      Yes      |      Yes      |      Yes      |      Yes      |      Yes
**bson4jackson 2.6.x** |      No       |      Yes      |      Yes      |      Yes      |      Yes
**bson4jackson 2.5.x** |      No       |      No       |      Yes      |      Yes      |      Yes
**bson4jackson 2.4.x** |      No       |      No       |      No       |      Yes      |      Yes

If you're looking for a version compatible to Jackson 1.x, please use
bson4jackson 1.3.0. It's the last version for the 1.x branch.
bson4jackson 1.3.0 is compatible to Jackson 1.7 up to 1.9.

## Compiling

Execute the following command to compile the library and to run the
unit tests:

    ./gradlew test

The script automatically downloads the correct Gradle version, so you
won't have to do anything else. If everything runs successfully, you
may create a .jar library:

    ./gradlew jar

The library will be located under the `build/libs` directory.

### Eclipse

Gradle includes a task that creates all files required to develop
bson4jackson in Eclipse. Run the following command:

    ./gradlew eclipse

Then import the project into your workspace.

### IntelliJ

Gradle includes a task that creates all files required to develop
bson4jackson in IntelliJ. Run the following command:

    ./gradlew idea

Then import the project into your workspace or open the root `bson4jackson.ipr`
project file.

## License

bson4jackson is licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
