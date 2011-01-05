package de.undercouch.bson4jackson;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.impl.JsonGeneratorBase;
import org.codehaus.jackson.impl.JsonWriteContext;

import de.undercouch.bson4jackson.io.DynamicOutputBuffer;

/**
 * Writes BSON code to the provided output stream
 * @author Michel Kraemer
 */
public class BsonGenerator extends JsonGeneratorBase {
	/**
     * Defines toggable features
     */
	public enum Feature {
		/**
		 * <p>Enables streaming by setting the document's total
		 * number of bytes in the header to 0. This allows the generator
		 * to flush the output buffer from time to time. Otherwise the
		 * generator would have to buffer the whole file to be able to
		 * calculate the total number of bytes.</p>
		 * <p><b>ATTENTION:</b> By enabling this feature, the BSON document
		 * generated by this class will not be compatible to the
		 * specification! However, if you know what you are doing and
		 * if you know that the document will be read by a parser that
		 * ignores the total number of bytes anyway (like {@link BsonParser}
		 * or <code>org.bson.BSONDecoder</code> from the MongoDB Java Driver
		 * do) then this feature will be very useful.</p>
		 */
		ENABLE_STREAMING;
		
		/**
		 * @return the bit mask that identifies this feature
		 */
		public int getMask() {
			return (1 << ordinal());
		}
	}
	
	/**
	 * A structure describing the document currently being generated
	 * @author Michel Kraemer
	 */
	private static class DocumentInfo {
		/**
		 * The position of the document's header in the output buffer
		 */
		final int headerPos;
		
		/**
		 * The current position in the array or -1 if the
		 * document is no array
		 */
		int currentArrayPos;
		
		/**
		 * Creates a new DocumentInfo object
		 * @param headerPos the position of the document's header
		 * in the output buffer
		 * @param array true if the document is an array
		 */
		public DocumentInfo(int headerPos, boolean array) {
			this.headerPos = headerPos;
			this.currentArrayPos = (array ? 0 : -1);
		}
	}
	
	/**
	 * Bit flag composed of bits that indicate which
	 * {@link Feature}s are enabled.
	 */
    protected final int _bsonFeatures;
    
	/**
	 * The output stream to write to
	 */
	protected final OutputStream _out;
	
	/**
	 * Since a BSON document's header must include the size of the whole document
	 * in bytes, we have to buffer the whole document first, before we can
	 * write it to the output stream. BSON specifies LITTLE_ENDIAN for all tokens.
	 */
	protected final DynamicOutputBuffer _buffer = new DynamicOutputBuffer(ByteOrder.LITTLE_ENDIAN);
	
	/**
	 * Saves the position of the type marker for the object currently begin written
	 */
	protected int _typeMarker = 0;
	
	/**
	 * Saves information about documents (the main document and embedded ones)
	 */
	protected Deque<DocumentInfo> _documents = new ArrayDeque<DocumentInfo>();
	
	/**
	 * Creates a new generator
	 * @param jsonFeatures bit flag composed of bits that indicate which
     * {@link org.codehaus.jackson.JsonGenerator.Feature}s are enabled.
     * @param bsonFeatures bit flag composed of bits that indicate which
	 * {@link Feature}s are enabled.
	 * @param out the output stream to write to
	 */
	public BsonGenerator(int jsonFeatures, int bsonFeatures, OutputStream out) {
		super(jsonFeatures, null);
		_bsonFeatures = bsonFeatures;
		_out = out;
		
		if (isEnabled(Feature.ENABLE_STREAMING)) {
			//if streaming is enabled, try to reuse some buffers
			//this will save garbage collector cycles if the tokens
			//written to the buffer are not too large
			_buffer.setReuseBuffersCount(2);
		}
	}
	
	/**
	 * Checks if a generator feature is enabled
	 * @param f the feature
	 * @return true if the given feature is enabled
	 */
	protected boolean isEnabled(Feature f) {
		return (_bsonFeatures & f.getMask()) != 0;
	}
	
	/**
	 * @return true if the generator is currently processing an array
	 */
	protected boolean isArray() {
		return (_documents.isEmpty() ? false : _documents.peek().currentArrayPos >= 0);
	}
	
	/**
	 * Retrieves and then increases the current position in the array
	 * currently being generated
	 * @return the position (before it has been increased) or -1 if
	 * the current document is not an array
	 */
	protected int getAndIncCurrentArrayPos() {
		if (_documents.isEmpty()) {
			return -1;
		}
		DocumentInfo di = _documents.peek();
		int r = di.currentArrayPos;
		++di.currentArrayPos;
		return r;
	}
	
	/**
	 * Encodes the given string into modified UTF-8 and writes it
	 * to the output buffer
	 * @param s the string
	 */
	protected void writeModifiedUTF8(CharSequence s) {
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (c >= 0x0001 && c <= 0x007F) {
				_buffer.putByte((byte)c);
			} else if (c > 0x07FF) {
				_buffer.putByte((byte)(0xE0 | ((c >> 12) & 0x0F)));
				_buffer.putByte((byte)(0x80 | ((c >> 6) & 0x3F)));
				_buffer.putByte((byte)(0x80 | (c & 0x3F)));
			} else {
				_buffer.putByte((byte)(0xC0 | ((c >> 6) & 0x1F)));
				_buffer.putByte((byte)(0x80 | (c & 0x3F)));
			}
		}
	}
	
	/**
	 * Reserves bytes for the BSON document header
	 */
	protected void reserveHeader() {
		_buffer.putInt(0);
	}
	
	/**
	 * Writes the BSON document header to the output buffer at the
	 * given position. Does not increase the buffer's write position. 
	 * @param pos the position where to write the header
	 */
	protected void putHeader(int pos) {
		_buffer.putInt(pos, _buffer.size() - pos);
	}
	
	@Override
	public void flush() throws IOException {
		_out.flush();
	}

	@Override
	protected void _releaseBuffers() {
		_buffer.clear();
	}
	
	@Override
	public void close() throws IOException {
		//finish document
		if (isEnabled(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT)) {
			while (!_documents.isEmpty()) {
				writeEndObject();
			}
		}
		
		//write buffer to output stream (if streaming is enabled,
		//this will write the the rest of the buffer)
		_buffer.writeTo(_out);
		_buffer.clear();
		_out.flush();
		
		if (isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET)) {
			_out.close();
		}
		
		super.close();
	}
	
	@Override
	public void writeStartArray() throws IOException,
			JsonGenerationException {
		_verifyValueWrite("start an array");
        _writeContext = _writeContext.createChildArrayContext();
		_writeStartObject(true);
	}

	@Override
	public void writeEndArray() throws IOException, JsonGenerationException {
		if (!_writeContext.inArray()) {
            _reportError("Current context not an ARRAY but " + _writeContext.getTypeDesc());
        }
		writeEndObjectInternal();
		_writeContext = _writeContext.getParent();
	}

	@Override
	public void writeStartObject() throws IOException, JsonGenerationException {
		_verifyValueWrite("start an object");
        _writeContext = _writeContext.createChildObjectContext();
		_writeStartObject(false);
	}
	
	/**
	 * Creates a new embedded document or array
	 * @param array true if the embedded object is an array
	 * @throws IOException if the document could not be created
	 */
	protected void _writeStartObject(boolean array) throws IOException {
		_writeArrayFieldNameIfNeeded();
		if (!_documents.isEmpty()) {
			//embedded document/array
			_buffer.putByte(_typeMarker, (array ? BsonConstants.TYPE_ARRAY :
				BsonConstants.TYPE_DOCUMENT));
		}
		_documents.push(new DocumentInfo(_buffer.size(), array));
		reserveHeader();
	}

	@Override
	public void writeEndObject() throws IOException, JsonGenerationException {
		if (!_writeContext.inObject()) {
            _reportError("Current context not an object but " +
            		_writeContext.getTypeDesc());
        }
        _writeContext = _writeContext.getParent();
        writeEndObjectInternal();
	}
        
	private void writeEndObjectInternal() {
		if (!_documents.isEmpty()) {
			_buffer.putByte(BsonConstants.TYPE_END);
			DocumentInfo info = _documents.pop();
			
			//re-write header to update document size (only if
			//streaming is not enabled since in this case the buffer
			//containing the header might not be available anymore)
			if (!isEnabled(Feature.ENABLE_STREAMING)) {
				putHeader(info.headerPos);
			}
		}
	}
	
	/**
	 * If the generator is currently processing an array, this method writes
	 * the field name of the current element (which is just the position of the
	 * element in the array)
	 * @throws IOException if the field name could not be written
	 */
	protected void _writeArrayFieldNameIfNeeded() throws IOException {
		if (isArray()) {
			int p = getAndIncCurrentArrayPos();
			_writeFieldName(String.valueOf(p));
		}
	}

	@Override
	public void writeFieldName(String name) throws IOException, JsonGenerationException {
		int status = _writeContext.writeFieldName(name);
        if (status == JsonWriteContext.STATUS_EXPECT_VALUE) {
            _reportError("Can not write a field name, expecting a value");
        }
        _writeFieldName(name);
	}
        
	private void _writeFieldName(String name) throws IOException, JsonGenerationException {
		//reserve bytes for the type
		_typeMarker = _buffer.size();
		_buffer.putByte((byte)0);
		
		//write field name
		writeModifiedUTF8(name);
		_buffer.putByte(BsonConstants.END_OF_STRING);
	}

	@Override
	protected void _verifyValueWrite(String typeMsg) throws IOException {
		int status = _writeContext.writeValue();
		if (status == JsonWriteContext.STATUS_EXPECT_NAME) {
			_reportError("Can not " + typeMsg + ", expecting field name");
		}
	}
	
	/**
	 * Tries to flush the output buffer if streaming is enabled. This
	 * method is a no-op if streaming is disabled.
	 * @throws IOException if flushing failed
	 */
	protected void flushBuffer() throws IOException {
		if (isEnabled(Feature.ENABLE_STREAMING)) {
			_buffer.flushTo(_out);
		}
	}

	@Override
	public void writeString(String text) throws IOException,
			JsonGenerationException {
		_writeArrayFieldNameIfNeeded();
		
		_verifyValueWrite("write string");
		_buffer.putByte(_typeMarker, BsonConstants.TYPE_STRING);
		
		//reserve space for the string size
		int p = _buffer.size();
		_buffer.putInt(0);
		
		//write string
		int l = _buffer.putUTF8(text);
		_buffer.putByte(BsonConstants.END_OF_STRING);
		
		//write string size
		_buffer.putInt(p, l + 1);
		
		flushBuffer();
	}

	@Override
	public void writeString(char[] text, int offset, int len)
			throws IOException, JsonGenerationException {
		writeString(new String(text, offset, len));
	}

	@Override
	public void writeRaw(String text) throws IOException,
			JsonGenerationException {
		_writeArrayFieldNameIfNeeded();
		_verifyValueWrite("write raw string");
		_buffer.putByte(_typeMarker, BsonConstants.TYPE_BINARY);
		_buffer.putInt(text.length() * 2);
		_buffer.putByte(BsonConstants.SUBTYPE_BINARY);
		_buffer.putString(text);
		flushBuffer();
	}

	@Override
	public void writeRaw(String text, int offset, int len) throws IOException,
			JsonGenerationException {
		writeRaw(text.substring(offset, len));
	}

	@Override
	public void writeRaw(char[] text, int offset, int len) throws IOException,
			JsonGenerationException {
		_writeArrayFieldNameIfNeeded();
		_verifyValueWrite("write raw string");
		_buffer.putByte(_typeMarker, BsonConstants.TYPE_BINARY);
		_buffer.putInt(text.length * 2);
		_buffer.putByte(BsonConstants.SUBTYPE_BINARY);
		_buffer.putString(CharBuffer.wrap(text));
		flushBuffer();
	}

	@Override
	public void writeRaw(char c) throws IOException, JsonGenerationException {
		writeRaw(new char[] { c }, 0, 1);
	}

	@Override
	public void writeBinary(Base64Variant b64variant, byte[] data, int offset,
			int len) throws IOException, JsonGenerationException {
		//base64 is not needed for BSON
		_writeArrayFieldNameIfNeeded();
		_verifyValueWrite("write binary");
		_buffer.putByte(_typeMarker, BsonConstants.TYPE_BINARY);
		_buffer.putInt(data.length);
		_buffer.putByte(BsonConstants.SUBTYPE_BINARY);
		int end = offset + len;
		if (end > data.length) {
			end = data.length;
		}
		while (offset < end) {
			_buffer.putByte(data[offset]);
			++offset;
		}
		flushBuffer();
	}

	@Override
	public void writeNumber(int v) throws IOException, JsonGenerationException {
		_writeArrayFieldNameIfNeeded();
		_verifyValueWrite("write number");
		_buffer.putByte(_typeMarker, BsonConstants.TYPE_INT32);
		_buffer.putInt(v);
		flushBuffer();
	}

	@Override
	public void writeNumber(long v) throws IOException, JsonGenerationException {
		_writeArrayFieldNameIfNeeded();
		_verifyValueWrite("write number");
		_buffer.putByte(_typeMarker, BsonConstants.TYPE_INT64);
		_buffer.putLong(v);
		flushBuffer();
	}

	@Override
	public void writeNumber(BigInteger v) throws IOException,
			JsonGenerationException {
		int bl = v.bitLength();
		if (bl < 32) {
			writeNumber(v.intValue());
		} else if (bl < 64) {
			writeNumber(v.longValue());
		} else {
			writeString(v.toString());
		}
	}

	@Override
	public void writeNumber(double d) throws IOException,
			JsonGenerationException {
		_writeArrayFieldNameIfNeeded();
		_verifyValueWrite("write number");
		_buffer.putByte(_typeMarker, BsonConstants.TYPE_DOUBLE);
		_buffer.putDouble(d);
		flushBuffer();
	}

	@Override
	public void writeNumber(float f) throws IOException,
			JsonGenerationException {
		//BSON understands double values only
		writeNumber((double)f);
	}

	@Override
	public void writeNumber(BigDecimal dec) throws IOException,
			JsonGenerationException {
		float f = dec.floatValue();
		if (!Float.isInfinite(f)) {
			writeNumber(f);
		} else {
			double d = dec.doubleValue();
			if (!Double.isInfinite(d)) {
				writeNumber(d);
			} else {
				writeString(dec.toString());
			}
		}
	}

	@Override
	public void writeNumber(String encodedValue) throws IOException,
			JsonGenerationException, UnsupportedOperationException {
		writeString(encodedValue);
	}

	@Override
	public void writeBoolean(boolean state) throws IOException,
			JsonGenerationException {
		_writeArrayFieldNameIfNeeded();
		_verifyValueWrite("write boolean");
		_buffer.putByte(_typeMarker, BsonConstants.TYPE_BOOLEAN);
		_buffer.putByte((byte)(state ? 1 : 0));
		flushBuffer();
	}

	@Override
	public void writeNull() throws IOException, JsonGenerationException {
		_writeArrayFieldNameIfNeeded();
		_verifyValueWrite("write null");
		_buffer.putByte(_typeMarker, BsonConstants.TYPE_NULL);
		flushBuffer();
	}

	@Override
	public void writeRawUTF8String(byte[] text, int offset, int length)
			throws IOException, JsonGenerationException {
		_writeArrayFieldNameIfNeeded();
		
		_verifyValueWrite("write raw utf8 string");
		_buffer.putByte(_typeMarker, BsonConstants.TYPE_STRING);
		
		//reserve space for the string size
		int p = _buffer.size();
		_buffer.putInt(0);
		
		//write string
		for (int i = offset; i < length; ++i) {
			_buffer.putByte(text[i]);
		}
		_buffer.putByte(BsonConstants.END_OF_STRING);
		
		//write string size
		_buffer.putInt(p, length);
		
		flushBuffer();		
	}

	@Override
	public void writeUTF8String(byte[] text, int offset, int length)
			throws IOException, JsonGenerationException {
		writeRawUTF8String(text, offset, length);
	}
}
