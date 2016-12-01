// Copyright 2010-2014 Michel Kraemer
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

package de.undercouch.bson4jackson.io;

import de.undercouch.bson4jackson.BsonConstants;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.io.EOFException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class MinKeyTest {

    @Test
    public void unableToReadADocumentWithAMinKeyField() throws Exception {

        /*
            { "_id" : ObjectId("55b917fe6415495d7e11fe4d"), "test" : { "$minKey" : 1 } }

            Document created in MongoDB Shell with command
                db.test.insert({test:MinKey()})

            Dumped with mongodump command line tool
                ./mongodb-osx-x86_64-3.0.5/bin/mongodump --db test --collection test
         */
        byte[] bytes = Files.readAllBytes(Paths.get("src/test/resources", "de/undercouch/bson4jackson", "document-with-minkey.bson"));

        LittleEndianInputStream stream =
                new LittleEndianInputStream(
                        new CountingInputStream(
                                new StaticBufferedInputStream(
                                        new UnsafeByteArrayInputStream(bytes))));

        int documentSize = stream.readInt();

        //ObjectId
        byte type = stream.readByte();
        String fieldName = stream.readUTF(-1);
        int time = ByteOrderUtil.flip(stream.readInt());
        int machine = ByteOrderUtil.flip(stream.readInt());
        int inc = ByteOrderUtil.flip(stream.readInt());
        assertEquals(BsonConstants.TYPE_OBJECTID, type);
        assertEquals("55b917fe6415495d7e11fe4d", new ObjectId(time, machine, inc).toString());


        //MinKey
        try {
            type = stream.readByte();
        } catch (EOFException e) {
            fail("Unable to read MinKey");
        }
    }
}
