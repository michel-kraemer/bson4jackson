package de.undercouch.bson4jackson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.io.IOContext;

/**
 * Factory for {@link BsonGenerator} and {@link BsonParser}
 * @author Michel Kraemer
 */
public class BsonFactory extends JsonFactory {
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
    
    @Override
    public BsonGenerator createJsonGenerator(OutputStream out, JsonEncoding enc)
    	throws IOException {
    	return createJsonGenerator(out);
    }
    
    public BsonGenerator createJsonGenerator(OutputStream out) throws IOException {
    	BsonGenerator result = new BsonGenerator(_objectCodec, out);
    	result.putHeader();
    	return result;
    }
    
    @Override
    protected JsonParser _createJsonParser(InputStream in, IOContext ctxt)
    	throws IOException, JsonParseException {
    	//TODO
    	return null;
    }

    @Override
    protected JsonParser _createJsonParser(Reader r, IOContext ctxt)
    	throws IOException, JsonParseException {
    	//TODO
    	return null;
    }
    
    @Override
    protected JsonParser _createJsonParser(byte[] data, int offset, int len, IOContext ctxt)
    	throws IOException, JsonParseException {
    	//TODO
    	return null;
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
