package de.undercouch.bson4jackson;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;

import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.impl.JsonParserMinimalBase;

import de.undercouch.bson4jackson.io.CountingInputStream;
import de.undercouch.bson4jackson.io.LittleEndianInputStream;
import de.undercouch.bson4jackson.types.Symbol;
import de.undercouch.bson4jackson.types.Timestamp;

/**
 * Reads a BSON document from the provided input stream
 * @author Michel Kraemer
 */
public class BsonParser extends JsonParserMinimalBase {
	/**
	 * The input stream to read from
	 */
	private LittleEndianInputStream _in;
	
	/**
	 * Counts the number of bytes read from {@link #_in}
	 */
	private CountingInputStream _counter;
	
	/**
	 * True if the parser has been closed
	 */
	private boolean _closed;
	
	/**
	 * The ObjectCodec used to parse the Bson object(s)
	 */
	private ObjectCodec _codec;
	
	/**
	 * The position of the current token
	 */
	private int _tokenPos;
	
	/**
	 * A stack of {@link Context} objects describing the current
	 * parser state.
	 */
	private Deque<Context> _contexts = new ArrayDeque<Context>();
	
	/**
	 * Constructs a new parser
	 * @param jsonFeatures bit flag composed of bits that indicate which
     * {@link org.codehaus.jackson.JsonParser.Feature}s are enabled.
	 * @param in the input stream to parse. 
	 */
	public BsonParser(int jsonFeatures, InputStream in) {
		super(jsonFeatures);
		if (!(in instanceof BufferedInputStream)) {
			in = new BufferedInputStream(in);
		}
		_counter = new CountingInputStream(in);
		_in = new LittleEndianInputStream(_counter);
	}
	
	@Override
	public ObjectCodec getCodec() {
		return _codec;
	}

	@Override
	public void setCodec(ObjectCodec c) {
		_codec = c;
	}

	@Override
	public void close() throws IOException {
		if (isEnabled(Feature.AUTO_CLOSE_SOURCE)) {
			_in.close();
		}
		_closed = true;
	}

	@Override
	public JsonToken nextToken() throws IOException, JsonParseException {
		if (_currToken == null) {
			//read document header (skip size, we're not interested)
			_in.readInt();
			_currToken = JsonToken.START_OBJECT;
			_contexts.push(new Context());
		} else {
			_tokenPos = _counter.getPosition();
			Context ctx = _contexts.peek();
			if (ctx == null) {
				throw new JsonParseException("Found element outside the document",
						getTokenLocation());
			}
			
			if (ctx.state == State.DONE) {
				//next field
				ctx.reset();
			}
			
			if (ctx.state == State.FIELDNAME) {
				while (true) {
					//read field name or end of document
					ctx.type = _in.readByte();
					if (ctx.type == BsonConstants.TYPE_END) {
						//end of document
						_currToken = JsonToken.END_OBJECT;
						_contexts.pop();
					} else if (ctx.type == BsonConstants.TYPE_UNDEFINED) {
						//read field name and then ignore this token
						readCString();
						continue;
					} else {
						//read field name
						ctx.fieldName = readCString();
						ctx.state = State.VALUE;
						_currToken = JsonToken.FIELD_NAME;
					}
					break;
				}
			} else {
				//parse element's value
				switch (ctx.type) {
				case BsonConstants.TYPE_DOUBLE:
					ctx.value = _in.readDouble();
					_currToken = JsonToken.VALUE_NUMBER_FLOAT;
					break;
					
				case BsonConstants.TYPE_STRING:
					ctx.value = readString();
					_currToken = JsonToken.VALUE_STRING;
					break;
					
				//case BsonConstants.TYPE_DOCUMENT:
					//TODO
					
				//case BsonConstants.TYPE_ARRAY:
					//TODO
					
				//case BsonConstants.TYPE_BINARY:
					//TODO
					
				//case BsonConstants.TYPE_OBJECTID:
					//TODO
					
				case BsonConstants.TYPE_BOOLEAN:
					boolean b = _in.readBoolean();
					ctx.value = b;
					_currToken = (b ? JsonToken.VALUE_TRUE : JsonToken.VALUE_FALSE);
					break;
					
				//case BsonConstants.TYPE_DATETIME:
					//TODO
					
				case BsonConstants.TYPE_NULL:
					_currToken = JsonToken.VALUE_NULL;
					break;
					
				//case BsonConstants.TYPE_REGEX:
					//TODO
					
				//case BsonConstants.TYPE_DBPOINTER:
					//TODO
					
				//case BsonConstants.TYPE_JAVASCRIPT:
					//TODO
					
				case BsonConstants.TYPE_SYMBOL:
					ctx.value = readSymbol();
					_currToken = JsonToken.VALUE_EMBEDDED_OBJECT;
					break;
					
				//case BsonConstants.TYPE_JAVASCRIPT_WITH_SCOPE:
					//TODO
					
				case BsonConstants.TYPE_INT32:
					ctx.value = _in.readInt();
					_currToken = JsonToken.VALUE_NUMBER_INT;
					break;
					
				case BsonConstants.TYPE_TIMESTAMP:
					ctx.value = readTimestamp();
					_currToken = JsonToken.VALUE_EMBEDDED_OBJECT;
					break;
					
				case BsonConstants.TYPE_INT64:
					ctx.value = _in.readLong();
					_currToken = JsonToken.VALUE_NUMBER_INT;
					break;
					
				case BsonConstants.TYPE_MINKEY:
					ctx.value = "MinKey";
					_currToken = JsonToken.VALUE_STRING;
					break;
					
				case BsonConstants.TYPE_MAXKEY:
					ctx.value = "MaxKey";
					_currToken = JsonToken.VALUE_STRING;
					break;
				
				default:
					throw new JsonParseException("Unknown element type " + ctx.type,
							getTokenLocation());
				}
				ctx.state = State.DONE;
			}
		}
		return _currToken;
	}
	
	/**
	 * @return a null-terminated string read from the input stream
	 * @throws IOException if the string could not be read
	 */
	protected String readCString() throws IOException {
		return _in.readUTF(-1);
	}
	
	/**
	 * Reads a string that consists of a integer denoting the number of bytes,
	 * the bytes (including a terminating 0 byte)
	 * @return the string
	 * @throws IOException if the string could not be read
	 */
	protected String readString() throws IOException {
		//read number of bytes
		int bytes = _in.readInt();
		if (bytes <= 0) {
			throw new IOException("Invalid number of string bytes");
		}
		String s;
		if (bytes > 1) {
			s = _in.readUTF(bytes - 1);
		} else {
			s = "";
		}
		//read terminating zero
		_in.readByte();
		return s;
	}
	
	/**
	 * Reads a symbol object from the input stream
	 * @return the symbol
	 * @throws IOException if the symbol could not be read
	 */
	protected Symbol readSymbol() throws IOException {
		return new Symbol(readString());
	}
	
	/**
	 * Reads a timestamp object from the input stream
	 * @return the timestamp
	 * @throws IOException if the timestamp could not be read
	 */
	protected Timestamp readTimestamp() throws IOException {
		int inc = _in.readInt();
		int time = _in.readInt();
		return new Timestamp(time, inc);
	}
	
	/**
	 * @return the context of the current element
	 * @throws IOException if there is no context
	 */
	protected Context getContext() throws IOException {
		Context ctx = _contexts.peek();
		if (ctx == null) {
			throw new IOException("Context unknown");
		}
		return ctx;
	}

	@Override
	public boolean isClosed() {
		return _closed;
	}

	@Override
	public String getCurrentName() throws IOException, JsonParseException {
		Context ctx = _contexts.peek();
		if (ctx == null) {
			return null;
		}
		return ctx.fieldName;
	}

	@Override
	public JsonStreamContext getParsingContext() {
		//this parser does not use JsonStreamContext
		return null;
	}

	@Override
	public JsonLocation getTokenLocation() {
		return new BsonLocation(_in, _tokenPos);
	}

	@Override
	public JsonLocation getCurrentLocation() {
		return new BsonLocation(_in, _counter.getPosition());
	}

	@Override
	public String getText() throws IOException, JsonParseException {
		Context ctx = _contexts.peek();
		if (ctx == null || ctx.state == State.FIELDNAME) {
			return null;
		}
		if (ctx.state == State.VALUE) {
			return ctx.fieldName;
		}
		return (String)ctx.value;
	}

	@Override
	public char[] getTextCharacters() throws IOException, JsonParseException {
		//not very efficient; that's why hasTextCharacters()
		//always returns false
		return getText().toCharArray();
	}

	@Override
	public int getTextLength() throws IOException, JsonParseException {
		return getText().length();
	}

	@Override
	public int getTextOffset() throws IOException, JsonParseException {
		return 0;
	}
	
	@Override
	public boolean hasTextCharacters() {
		//getTextCharacters is obviously not the most efficient way
		return false;
	}

	@Override
	public Number getNumberValue() throws IOException, JsonParseException {
		return (Number)getContext().value;
	}

	@Override
	public NumberType getNumberType() throws IOException, JsonParseException {
		Context ctx = _contexts.peek();
		if (ctx == null) {
			return null;
		}
		if (ctx.value instanceof Integer) {
			return NumberType.INT;
		} else if (ctx.value instanceof Long) {
			return NumberType.LONG;
		} else if (ctx.value instanceof BigInteger) {
			return NumberType.BIG_INTEGER;
		} else if (ctx.value instanceof Float) {
			return NumberType.FLOAT;
		} else if (ctx.value instanceof Double) {
			return NumberType.DOUBLE;
		} else if (ctx.value instanceof BigDecimal) {
			return NumberType.BIG_DECIMAL;
		}
		return null;
	}

	@Override
	public int getIntValue() throws IOException, JsonParseException {
		return ((Number)getContext().value).intValue();
	}

	@Override
	public long getLongValue() throws IOException, JsonParseException {
		return ((Number)getContext().value).longValue();
	}

	@Override
	public BigInteger getBigIntegerValue() throws IOException,
			JsonParseException {
		Number n = getNumberValue();
		if (n == null) {
			return null;
		}
		if (n instanceof Byte || n instanceof Integer ||
			n instanceof Long || n instanceof Short) {
			return BigInteger.valueOf(n.longValue());
		} else if (n instanceof Double || n instanceof Float) {
			return BigDecimal.valueOf(n.doubleValue()).toBigInteger();
		}
		return new BigInteger(n.toString());
	}

	@Override
	public float getFloatValue() throws IOException, JsonParseException {
		return ((Number)getContext().value).floatValue();
	}

	@Override
	public double getDoubleValue() throws IOException, JsonParseException {
		return ((Number)getContext().value).doubleValue();
	}

	@Override
	public BigDecimal getDecimalValue() throws IOException, JsonParseException {
		Number n = getNumberValue();
		if (n == null) {
			return null;
		}
		if (n instanceof Byte || n instanceof Integer ||
			n instanceof Long || n instanceof Short) {
			return BigDecimal.valueOf(n.longValue());
		} else if (n instanceof Double || n instanceof Float) {
			return BigDecimal.valueOf(n.doubleValue());
		}
		return new BigDecimal(n.toString());
	}

	@Override
	public byte[] getBinaryValue(Base64Variant b64variant) throws IOException,
			JsonParseException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object getEmbeddedObject() throws IOException, JsonParseException {
		Context ctx = _contexts.peek();
		return (ctx != null ? ctx.value : null);
	}

	@Override
	protected void _handleEOF() throws JsonParseException {
		_reportInvalidEOF();		
	}

	/**
	 * Specifies what the parser is currently parsing (field name or value) or
	 * if it is done with the current element
	 */
	private enum State {
		FIELDNAME,
		VALUE,
		DONE
	}
	
	/**
	 * Information about the element currently begin parsed
	 */
	private static class Context {
		/**
		 * The bson type of the current element
		 */
		byte type;
		
		/**
		 * The field name of the current element
		 */
		String fieldName;
		
		/**
		 * The value of the current element
		 */
		Object value;
		
		/**
		 * The parsing state of the current token
		 */
		State state = State.FIELDNAME;
		
		public void reset() {
			type = 0;
			fieldName = null;
			value = null;
			state = State.FIELDNAME;
		}
	}
	
	/**
	 * Extends {@link JsonLocation} to offer a specialized string representation
	 */
	private static class BsonLocation extends JsonLocation {
		private static final long serialVersionUID = -5441597278886285168L;

		public BsonLocation(Object srcRef, long totalBytes) {
			super(srcRef, totalBytes, -1, -1, -1);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(80);
	        sb.append("[Source: ");
	        if (getSourceRef() == null) {
	            sb.append("UNKNOWN");
	        } else {
	            sb.append(getSourceRef().toString());
	        }
	        sb.append("; pos: ");
	        sb.append(getByteOffset());
	        sb.append(']');
	        return sb.toString();
		}
	}
}
