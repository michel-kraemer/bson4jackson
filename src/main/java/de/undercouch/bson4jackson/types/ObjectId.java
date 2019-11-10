package de.undercouch.bson4jackson.types;

/**
 * A unique identifier for MongoDB documents. Such identifiers
 * consist of a timestamp, a machine ID and a counter.
 * @author Michel Kraemer
 */
public class ObjectId {
    /**
     * The timestamp
     */
    protected final int _time;

    /**
     * The machine ID
     */
    protected final int _machine;

    /**
     * The counter
     */
    protected final int _inc;

    /**
     * Constructs a new identifier
     * @param time the timestamp
     * @param machine the machine ID
     * @param inc the counter
     */
    public ObjectId(int time, int machine, int inc) {
        _time = time;
        _machine = machine;
        _inc = inc;
    }

    /**
     * @return the timestamp
     */
    public int getTime() {
        return _time;
    }

    /**
     * @return the machine ID
     */
    public int getMachine() {
        return _machine;
    }

    /**
     * @return the counter
     */
    public int getInc() {
        return _inc;
    }
}
