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

package de.undercouch.bson4jackson;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;

/**
 * Factory for {@link BsonGenerator} and {@link BsonParser}
 * @author Michel Kraemer
 */
public class BsonFactory extends JsonFactory {
	private static final long serialVersionUID = 1991836957699496674L;

	/**
	 * The BSON generator features enabled by default
	 */
	private static final int DEFAULT_BSON_GENERATOR_FEATURE_FLAGS = 0;

	/**
	 * The BSON parser features enabled by default
	 */
	private static final int DEFAULT_BSON_PARSER_FEATURE_FLAGS = 0;

	/**
	 * The BSON generator features to be enabled when a new
	 * generator is created
	 */
	protected int _bsonGeneratorFeatures = DEFAULT_BSON_GENERATOR_FEATURE_FLAGS;

	/**
	 * The BSON parser features to be enabled when a new parser
	 * is created
	 */
	protected int _bsonParserFeatures = DEFAULT_BSON_PARSER_FEATURE_FLAGS;

	/**
	 * @see JsonFactory#JsonFactory()
	 */
	public BsonFactory() {
		this(null);
	}

	/**
	 * @see JsonFactory#JsonFactory(ObjectCodec)
	 */
    public BsonFactory(ObjectCodec oc) {
    	super(oc);
    }
    
    /**
     * Method for enabling/disabling specified generator features
     * (check {@link BsonGenerator.Feature} for list of features)
     * @param f the feature to enable or disable
     * @param state true if the feature should be enabled, false otherwise
     */
    public final BsonFactory configure(BsonGenerator.Feature f, boolean state) {
    	if (state) {
    		return enable(f);
    	}
    	return disable(f);
    }

    /**
     * Method for enabling specified generator features
     * (check {@link BsonGenerator.Feature} for list of features)
     * @param f the feature to enable
     */
    public BsonFactory enable(BsonGenerator.Feature f) {
    	_bsonGeneratorFeatures |= f.getMask();
    	return this;
    }

    /**
     * Method for disabling specified generator features
     * (check {@link BsonGenerator.Feature} for list of features)
     * @param f the feature to disable
     */
    public BsonFactory disable(BsonGenerator.Feature f) {
    	_bsonGeneratorFeatures &= ~f.getMask();
    	return this;
    }

    /**
     * @return true if the specified generator feature is enabled
     */
    public final boolean isEnabled(BsonGenerator.Feature f) {
    	return (_bsonGeneratorFeatures & f.getMask()) != 0;
    }

	/**
	 * Method for enabling/disabling specified parser features
	 * (check {@link BsonParser.Feature} for list of features)
	 * @param f the feature to enable or disable
	 * @param state true if the feature should be enabled, false otherwise
	 */
	public final BsonFactory configure(BsonParser.Feature f, boolean state) {
		if (state) {
			return enable(f);
		}
		return disable(f);
	}

	/**
	 * Method for enabling specified parser features
	 * (check {@link BsonParser.Feature} for list of features)
	 * @param f the feature to enable
	 */
	public BsonFactory enable(BsonParser.Feature f) {
		_bsonParserFeatures |= f.getMask();
		return this;
	}

	/**
	 * Method for disabling specified parser features
	 * (check {@link BsonParser.Feature} for list of features)
	 * @param f the feature to disable
	 */
	public BsonFactory disable(BsonParser.Feature f) {
		_bsonParserFeatures &= ~f.getMask();
		return this;
	}

	/**
	 * @return true if the specified parser feature is enabled
	 */
	public final boolean isEnabled(BsonParser.Feature f) {
		return (_bsonParserFeatures & f.getMask()) != 0;
	}

	@Override
    public BsonGenerator createJsonGenerator(OutputStream out, JsonEncoding enc)
    	throws IOException {
    	return createJsonGenerator(out);
    }
    
    public BsonGenerator createJsonGenerator(OutputStream out) throws IOException {
    	BsonGenerator g = new BsonGenerator(_generatorFeatures, _bsonGeneratorFeatures, out);
    	ObjectCodec codec = getCodec();
    	if (codec != null) {
    		g.setCodec(codec);
    	}
    	return g;
    }

    @Override
    public BsonParser createJsonParser(InputStream in) throws IOException {
		return _createJsonParser(in, _createContext(in, false));
    }

    @Override
    protected BsonParser _createJsonParser(InputStream in, IOContext ctxt)
    	throws IOException, JsonParseException {
		BsonParser p = new BsonParser(ctxt, _parserFeatures, _bsonParserFeatures, in);
		ObjectCodec codec = getCodec();
		if (codec != null) {
			p.setCodec(codec);
		}
		return p;
     }

    @Override
    protected JsonParser _createJsonParser(Reader r, IOContext ctxt)
    	throws IOException, JsonParseException {
    	throw new UnsupportedOperationException("Can not create reader for non-byte-based source");
    }
    
    @Override
    protected JsonParser _createJsonParser(byte[] data, int offset, int len, IOContext ctxt)
    	throws IOException, JsonParseException {
    	return _createJsonParser(new ByteArrayInputStream(data, offset, len), ctxt);
    }
    
    @Override
    protected JsonGenerator _createUTF8JsonGenerator(OutputStream out, IOContext ctxt) throws IOException {
    	return createJsonGenerator(out, ctxt.getEncoding());
    }
    
    @Override
    protected JsonGenerator _createJsonGenerator(Writer out, IOContext ctxt)
    	throws IOException {
    	throw new UnsupportedOperationException("Can not create generator for non-byte-based target");
    }
    
    @Override
    protected Writer _createWriter(OutputStream out, JsonEncoding enc, IOContext ctxt)
    	throws IOException {
    	throw new UnsupportedOperationException("Can not create generator for non-byte-based target");
    }
}
