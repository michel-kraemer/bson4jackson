================
BSON for Jackson
================

This library adds support for `BSON <http://bsonspec.org>`_ to the
`Jackson JSON processor <http://http://jackson.codehaus.org/>`_.

BSON is a binary representation of `JSON <http://json.org/>`_. It has
gained prominence by its usage as the main exchange and persistence
format of the document-oriented database management system `MongoDB
<http://www.mongodb.com>`_.

Compiling
---------

You need `sbt <http://code.google.com/p/simple-build-tool/>`_ in order
to compile the bson4jackson library. Please follow the `instructions on
the sbt wiki <http://code.google.com/p/simple-build-tool/wiki/Setup>`_.

Execute the following command to compile the library and to run the
unit tests::

  sbt update compile test

If everything runs successfully, you may create a .jar library::

  sbt clean package

The library will be located under the ``target`` directory.

Eclipse
.......

The source code includes a Eclipse project file. You may use the
`sbt-eclipse-plugin <http://github.com/Gekkio/sbt-eclipse-plugin>`_
to include libraries managed by sbt into the project's classpath.

Usage
-----

The bson4jackson library integrates completely into Jackson. Please have
a look at the `Jackson wiki <http://wiki.fasterxml.com/JacksonDocumentation>`_
for a complete description.

The BSON specification defines some additional types not available in
the original JSON specification. These types are mapped to simple
wrapper objects.
