package de.undercouch.bson4jackson;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteOrder;

import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.impl.JsonGeneratorBase;
import org.codehaus.jackson.impl.JsonWriteContext;

/**
 * Writes BSON code to the provided output stream
 * @author Michel Kraemer
 */
public class BsonGenerator extends JsonGeneratorBase {
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
	 * Counts the number of nested objects. In level 0 no object marker will
	 * be written.
	 */
	protected int _objectLevel = 0;
	
	/**
	 * Creates a new generator
	 * @param codec the codec used to write the document
	 * @param out the output stream to write to
	 */
	public BsonGenerator(ObjectCodec codec, OutputStream out) {
		super(0, codec);
		_out = out;
	}
	
	/**
	 * Calculates the length of the given string as if it would be
	 * encoded as modified UTF-8
	 * TODO refactor this!
	 * @param s the string
	 * @return the length of s as modified UTF-8
	 */
	protected int getModifiedUTF8Length(CharSequence s) {
		int length = 0;
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (c >= 0x0001 && c <= 0x007F) {
				++length;
			} else if (c > 0x07FF) {
				length += 3;
			} else {
				length += 2;
			}
		}
		return length;
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
	 * Writes the BSON header to the output buffer. This method can be called
	 * repeatedly. It just overwrites the first bytes of the buffer.
	 */
	public void putHeader() {
		if (_buffer.size() == 0) {
			_buffer.putInt32(_buffer.size());
		} else {
			_buffer.putInt32(0, _buffer.size());
		}
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
		_buffer.putByte(BsonConstants.TYPE_END);
		
		//re-write header to update document size
		putHeader();
		
		//write buffer to output stream
		_buffer.writeTo(_out);
		_out.flush();
	}
	
	@Override
	protected void _writeStartArray() throws IOException,
			JsonGenerationException {
		//TODO test this method
		_buffer.putByte(BsonConstants.TYPE_ARRAY);
	}

	@Override
	protected void _writeEndArray() throws IOException, JsonGenerationException {
		// TODO Auto-generated method stub
	}

	@Override
	protected void _writeStartObject() throws IOException,
			JsonGenerationException {
		if (_objectLevel > 0) {
			_buffer.putByte(BsonConstants.TYPE_DOCUMENT);
		}
		++_objectLevel;
	}

	@Override
	protected void _writeEndObject() throws IOException,
			JsonGenerationException {
		if (_objectLevel > 0) {
			--_objectLevel;
		}
	}

	@Override
	protected void _writeFieldName(String name, boolean commaBefore)
			throws IOException, JsonGenerationException {
		//reserve bytes for the type
		_typeMarker = _buffer.size();
		_buffer.putByte((byte)0);
		
		writeModifiedUTF8(name);
		_buffer.putByte(BsonConstants.END_OF_STRING);
	}

	@Override
	protected final void _verifyValueWrite(String typeMsg) throws IOException, JsonGenerationException {
		int status = _writeContext.writeValue();
		if (status == JsonWriteContext.STATUS_EXPECT_NAME) {
			_reportError("Can not " + typeMsg + ", expecting field name");
		}
	}

	@Override
	public void writeString(String text) throws IOException,
			JsonGenerationException {
		_verifyValueWrite("write string");
		_buffer.putByte(_typeMarker, BsonConstants.TYPE_STRING);
		_buffer.putInt32(getModifiedUTF8Length(text) + 1);
		writeModifiedUTF8(text);
		_buffer.putByte(BsonConstants.END_OF_STRING);
	}

	@Override
	public void writeString(char[] text, int offset, int len)
			throws IOException, JsonGenerationException {
		writeString(new String(text, offset, len));
	}

	@Override
	public void writeRaw(String text) throws IOException,
			JsonGenerationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void writeRaw(String text, int offset, int len) throws IOException,
			JsonGenerationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void writeRaw(char[] text, int offset, int len) throws IOException,
			JsonGenerationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void writeRaw(char c) throws IOException, JsonGenerationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void writeBinary(Base64Variant b64variant, byte[] data, int offset,
			int len) throws IOException, JsonGenerationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void writeNumber(int v) throws IOException, JsonGenerationException {
		_verifyValueWrite("write number");
		_buffer.putByte(_typeMarker, BsonConstants.TYPE_INT32);
		_buffer.putInt32(v);
	}

	@Override
	public void writeNumber(long v) throws IOException, JsonGenerationException {
		_verifyValueWrite("write number");
		_buffer.putByte(_typeMarker, BsonConstants.TYPE_INT64);
		_buffer.putInt64(v);
	}

	@Override
	public void writeNumber(BigInteger v) throws IOException,
			JsonGenerationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void writeNumber(double d) throws IOException,
			JsonGenerationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void writeNumber(float f) throws IOException,
			JsonGenerationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void writeNumber(BigDecimal dec) throws IOException,
			JsonGenerationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void writeNumber(String encodedValue) throws IOException,
			JsonGenerationException, UnsupportedOperationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void writeBoolean(boolean state) throws IOException,
			JsonGenerationException {
		_verifyValueWrite("write boolean");
		_buffer.putByte(_typeMarker, BsonConstants.TYPE_BOOLEAN);
		_buffer.putByte((byte)(state ? 1 : 0));
	}

	@Override
	public void writeNull() throws IOException, JsonGenerationException {
		_verifyValueWrite("write null");
		_buffer.putByte(_typeMarker, BsonConstants.TYPE_NULL);
	}
}
