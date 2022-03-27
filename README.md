# BSON for Jackson [![Actions Status](https://github.com/michel-kraemer/bson4jackson/workflows/CI/badge.svg)](https://github.com/michel-kraemer/bson4jackson/actions) [![Apache License, Version 2.0](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

This library adds support for [BSON](http://bsonspec.org) to the
[Jackson JSON processor](https://github.com/FasterXML/jackson).

BSON is a binary representation of [JSON](https://json.org/). It is
well known as the main exchange and persistence
format of [MongoDB](https://www.mongodb.com/).

## Quick start

Just create a Jackson `ObjectMapper` with a `BsonFactory` as follows:

```java
ObjectMapper mapper = new ObjectMapper(new BsonFactory());
```

For more information, you may read my
[bson4jackson tutorial](https://michelkraemer.com/binary-json-with-bson4jackson)
or the complete [documentation of Jackson](https://github.com/FasterXML/jackson).

## Download

bson4jackson binaries are available from the
[GitHub releases page](https://github.com/michel-kraemer/bson4jackson/releases>).

You may also use [Maven](http://maven.apache.org/) to download bson4jackson:

```xml
<dependencies>
    <dependency>
        <groupId>de.undercouch</groupId>
        <artifactId>bson4jackson</artifactId>
        <version>2.13.1</version>
    </dependency>
</dependencies>
```

If you are using [Gradle](https://gradle.org/), you may add the
following snippet to your `build.gradle`:

```gradle
dependencies {
    implementation 'de.undercouch:bson4jackson:2.13.1'
}
```

## Compatibility

The latest version of bson4jackson is backward compatible to all versions of
Jackson 2.x released up to date. It should be compatible to newer versions as
well. If you experience compatibility issues,
[just let me know](https://github.com/michel-kraemer/bson4jackson/issues).

If you are looking for a version compatible to Jackson 1.x, please use
bson4jackson 1.3.0. It's the last version for the 1.x branch.
bson4jackson 1.3.0 is compatible to Jackson 1.7 up to 1.9.

## Compiling

Execute the following command to compile the library and to run the
unit tests:

    ./gradlew test

The script automatically downloads the correct Gradle version so you
won't have to do anything else. If everything runs successfully, you
may create a .jar library:

    ./gradlew jar

The library will be located under `build/libs`.

## License

bson4jackson is licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
