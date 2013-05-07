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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
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
	protected BsonGenerator _createGenerator(Writer out, IOContext ctxt) {
		throw new UnsupportedOperationException("Can not create writer for non-byte-based target");
	}
	
	@Override
	@SuppressWarnings("deprecation")
	protected BsonGenerator _createJsonGenerator(Writer out, IOContext ctxt) {
		return _createGenerator(out, ctxt);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	protected BsonParser _createJsonParser(byte[] data, int offset, int len, IOContext ctxt) {
        return _createParser(data, offset, len, ctxt);
    }
	
	@Override
	@SuppressWarnings("deprecation")
	protected BsonParser _createJsonParser(InputStream in, IOContext ctxt) {
        return _createParser(in, ctxt);
    }
	
	@Override
	@SuppressWarnings("deprecation")
	protected BsonParser _createJsonParser(Reader r, IOContext ctxt) {
        return _createParser(r, ctxt);
    }
	
	@Override
	protected BsonParser _createParser(byte[] data, int offset, int len, IOContext ctxt) {
		return _createParser(new ByteArrayInputStream(data, offset, len), ctxt);
	}
	
	@Override
	protected BsonParser _createParser(InputStream in, IOContext ctxt) {
		BsonParser p = new BsonParser(ctxt, _parserFeatures, _bsonParserFeatures, in);
		ObjectCodec codec = getCodec();
		if (codec != null) {
			p.setCodec(codec);
		}
		return p;
	}
	
	@Override
	protected BsonParser _createParser(Reader r, IOContext ctxt) {
		throw new UnsupportedOperationException("Can not create reader for non-byte-based source");
	}
	
	@Override
	protected BsonGenerator _createUTF8Generator(OutputStream out, IOContext ctxt) throws IOException {
		return createGenerator(out);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	protected BsonGenerator _createUTF8JsonGenerator(OutputStream out, IOContext ctxt) throws IOException {
		return _createUTF8Generator(out, ctxt);
	}
	
	@Override
	protected Writer _createWriter(OutputStream out, JsonEncoding enc, IOContext ctxt)
			throws IOException {
		throw new UnsupportedOperationException("Can not create generator for non-byte-based target");
	}
	
	@Override
	public BsonGenerator createGenerator(File f, JsonEncoding enc) throws IOException {
		OutputStream out = new FileOutputStream(f);
		IOContext ctxt = _createContext(out, true);
		ctxt.setEncoding(enc);
		if (enc == JsonEncoding.UTF8 && _outputDecorator != null) {
			out = _outputDecorator.decorate(ctxt, out);
		}
		return createGenerator(out, enc);
	}
	
	@Override
	public BsonGenerator createGenerator(OutputStream out) throws IOException {
		return createGenerator(out, JsonEncoding.UTF8);
	}
	
	@Override
	public BsonGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException {
		IOContext ctxt = _createContext(out, true);
		ctxt.setEncoding(enc);
		if (enc == JsonEncoding.UTF8 && _outputDecorator != null) {
			out = _outputDecorator.decorate(ctxt, out);
		}
		BsonGenerator g = new BsonGenerator(_generatorFeatures, _bsonGeneratorFeatures, out);
    	ObjectCodec codec = getCodec();
    	if (codec != null) {
    		g.setCodec(codec);
    	}
    	return g;
	}
	
	@Override
	public BsonGenerator createGenerator(Writer writer) {
		throw new UnsupportedOperationException("Can not create generator for non-byte-based target");
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public BsonGenerator createJsonGenerator(File f, JsonEncoding enc) throws IOException {
		return createGenerator(f, enc);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public BsonGenerator createJsonGenerator(OutputStream out) throws IOException {
		return createGenerator(out);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public BsonGenerator createJsonGenerator(OutputStream out, JsonEncoding enc) throws IOException {
		return createGenerator(out, enc);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public BsonGenerator createJsonGenerator(Writer out) {
		return createGenerator(out);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public BsonParser createJsonParser(byte[] data) throws IOException {
		return createParser(data);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public BsonParser createJsonParser(byte[] data, int offset, int len) throws IOException {
		return createParser(data, offset, len);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public BsonParser createJsonParser(File f) throws IOException {
		return createParser(f);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public BsonParser createJsonParser(InputStream in) throws IOException {
		return createParser(in);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public BsonParser createJsonParser(Reader r) {
		return createParser(r);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public BsonParser createJsonParser(String content) {
		return createParser(content);
	}

	@Override
	@SuppressWarnings("deprecation")
	public BsonParser createJsonParser(URL url) throws IOException {
		return createParser(url);
	}
	
	@Override
	public BsonParser createParser(byte[] data) throws IOException {
		IOContext ctxt = _createContext(data, true);
        if (_inputDecorator != null) {
            InputStream in = _inputDecorator.decorate(ctxt, data, 0, data.length);
            if (in != null) {
                return _createParser(in, ctxt);
            }
        }
        return _createParser(data, 0, data.length, ctxt);
	}
	
	@Override
	public BsonParser createParser(byte[] data, int offset, int len) throws IOException {
		IOContext ctxt = _createContext(data, true);
        if (_inputDecorator != null) {
            InputStream in = _inputDecorator.decorate(ctxt, data, offset, len);
            if (in != null) {
                return _createParser(in, ctxt);
            }
        }
        return _createParser(data, offset, len, ctxt);
	}
	
	@SuppressWarnings("resource")
	@Override
	public BsonParser createParser(File f) throws IOException {
        IOContext ctxt = _createContext(f, true);
        InputStream in = new FileInputStream(f);
        if (_inputDecorator != null) {
            in = _inputDecorator.decorate(ctxt, in);
        }
        return _createParser(in, ctxt);
    }
	
	@Override
	public BsonParser createParser(InputStream in) throws IOException {
        IOContext ctxt = _createContext(in, false);
        if (_inputDecorator != null) {
            in = _inputDecorator.decorate(ctxt, in);
        }
        return _createParser(in, ctxt);
    }
	
	@Override
	public BsonParser createParser(Reader r) {
		throw new UnsupportedOperationException("Can not create reader for non-byte-based source");
	}
	
	@Override
	public BsonParser createParser(String content) {
		throw new UnsupportedOperationException("Can not create reader for non-byte-based source");
	}
	
	@Override
	public BsonParser createParser(URL url) throws IOException {
        IOContext ctxt = _createContext(url, true);
        InputStream in = _optimizedStreamFromURL(url);
        if (_inputDecorator != null) {
            in = _inputDecorator.decorate(ctxt, in);
        }
        return _createParser(in, ctxt);
    }
}
