package de.undercouch.bson4jackson.types;

/**
 * A distinct string
 * @author Michel Kraemer
 */
public class Symbol {
    /**
     * The actual symbol
     */
    protected final String _symbol;

    /**
     * Constructs a new symbol
     * @param symbol the actual symbol
     */
    public Symbol(String symbol) {
        _symbol = symbol;
    }

    /**
     * @return the actual symbol
     */
    public String getSymbol() {
        return _symbol;
    }

    @Override
    public String toString() {
        return _symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof String) {
            return _symbol.equals((String)o);
        }
        if (o instanceof Symbol) {
            return _symbol.equals(((Symbol)o)._symbol);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_symbol == null) ? 0 : _symbol.hashCode());
        return result;
    }
}
