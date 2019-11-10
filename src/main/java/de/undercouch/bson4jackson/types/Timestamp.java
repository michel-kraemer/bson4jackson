package de.undercouch.bson4jackson.types;

/**
 * A special internal type used by MongoDB replication and sharding.
 * @author Michel Kraemer
 */
public class Timestamp {
    /**
     * The increment
     */
    protected final int _inc;

    /**
     * The actual timestamp
     */
    protected final int _time;

    /**
     * Constructs a new timestamp object
     * @param time the actual timestamp
     * @param inc the increment
     */
    public Timestamp(int time, int inc) {
        _inc = inc;
        _time = time;
    }

    /**
     * @return the increment
     */
    public int getInc() {
        return _inc;
    }

    /**
     * @return the actual timestamp
     */
    public int getTime() {
        return _time;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Timestamp)) {
            return false;
        }
        Timestamp t = (Timestamp)o;
        return (_inc == t._inc && _time == t._time);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _inc;
        result = prime * result + _time;
        return result;
    }
}
