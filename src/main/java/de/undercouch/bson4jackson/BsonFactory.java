package de.undercouch.bson4jackson;

import de.undercouch.bson4jackson.io.UnsafeByteArrayInputStream;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.io.CharacterEscapes;
import tools.jackson.core.io.IOContext;
import tools.jackson.core.json.JsonFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serial;
import java.io.Writer;

/**
 * Factory for {@link BsonGenerator} and {@link BsonParser}
 */
public class BsonFactory extends JsonFactory {
    @Serial
    private static final long serialVersionUID = 1991836957699496674L;

    /**
     * The BSON generator features enabled by default
     */
    protected static final int DEFAULT_BSON_GENERATOR_FEATURE_FLAGS = 0;

    /**
     * The BSON parser features enabled by default
     */
    protected static final int DEFAULT_BSON_PARSER_FEATURE_FLAGS = 0;

    /**
     * Custom character escapes to use for generators created by this factory
     */
    protected CharacterEscapes _bsonCharacterEscapes;

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
        super();
    }

    /**
     * <p>Constructor used when copy()ing a factory instance.</p>
     * @param src the source BsonFactory to copy
     * @since 2.5
     */
    protected BsonFactory(BsonFactory src) {
        super(src);
        _bsonGeneratorFeatures = src._bsonGeneratorFeatures;
        _bsonParserFeatures = src._bsonParserFeatures;
        _bsonCharacterEscapes = src._bsonCharacterEscapes;
    }

    /**
     * Returns a new cloned copy of the factory
     *
     * @return deep copy of the factory
     * @see JsonFactory#copy()
     *
     * @since 2.5
     */
    public BsonFactory copy() {
        return new BsonFactory(this);
    }

    /**
     * Method for enabling/disabling specified generator features
     * (check {@link BsonGenerator.Feature} for list of features)
     * @param f the feature to enable or disable
     * @param state true if the feature should be enabled, false otherwise
     * @return this BsonFactory
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
     * @return this BsonFactory
     */
    public BsonFactory enable(BsonGenerator.Feature f) {
        _bsonGeneratorFeatures |= f.getMask();
        return this;
    }

    /**
     * Method for disabling specified generator features
     * (check {@link BsonGenerator.Feature} for list of features)
     * @param f the feature to disable
     * @return this BsonFactory
     */
    public BsonFactory disable(BsonGenerator.Feature f) {
        _bsonGeneratorFeatures &= ~f.getMask();
        return this;
    }

    /**
     * Checks whether a generator feature is enabled
     * @param f the feature to check for
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
     * @return this BsonFactory
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
     * @return this BsonFactory
     */
    public BsonFactory enable(BsonParser.Feature f) {
        _bsonParserFeatures |= f.getMask();
        return this;
    }

    /**
     * Method for disabling specified parser features
     * (check {@link BsonParser.Feature} for list of features)
     * @param f the feature to disable
     * @return this BsonFactory
     */
    public BsonFactory disable(BsonParser.Feature f) {
        _bsonParserFeatures &= ~f.getMask();
        return this;
    }

    /**
     * Checks whether a parser feature is enabled
     * @param f the feature to check for
     * @return true if the specified parser feature is enabled
     */
    public final boolean isEnabled(BsonParser.Feature f) {
        return (_bsonParserFeatures & f.getMask()) != 0;
    }

    /**
     * Sets the character escapes to use for generators created by this factory
     * @param esc the character escapes (may be null)
     */
    public void setCharacterEscapes(CharacterEscapes esc) {
        _bsonCharacterEscapes = esc;
    }

    @Override
    protected BsonParser _createParser(ObjectReadContext readCtxt, IOContext ctxt, InputStream in) {
        return new BsonParser(readCtxt, ctxt, _streamReadFeatures, _bsonParserFeatures, in);
    }

    @Override
    protected BsonParser _createParser(ObjectReadContext readCtxt, IOContext ctxt, byte[] data, int offset, int len) {
        return _createParser(readCtxt, ctxt, new UnsafeByteArrayInputStream(data, offset, len));
    }

    @Override
    protected BsonParser _createParser(ObjectReadContext readCtxt, IOContext ctxt, Reader r) {
        throw new UnsupportedOperationException("Can not create reader for non-byte-based source");
    }

    @Override
    protected BsonGenerator _createUTF8Generator(ObjectWriteContext writeCtxt, IOContext ctxt, OutputStream out) {
        BsonGenerator g = new BsonGenerator(writeCtxt, ctxt, _streamWriteFeatures, _bsonGeneratorFeatures, out);
        if (_bsonCharacterEscapes != null) {
            g.setCharacterEscapes(_bsonCharacterEscapes);
        }
        return g;
    }

    @Override
    protected BsonGenerator _createGenerator(ObjectWriteContext writeCtxt, IOContext ctxt, Writer out) {
        throw new UnsupportedOperationException("Can not create generator for non-byte-based target");
    }
}
