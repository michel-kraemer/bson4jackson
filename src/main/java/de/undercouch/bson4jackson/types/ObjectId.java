package de.undercouch.bson4jackson.types;

/**
 * A unique identifier for MongoDB documents. Such identifiers
 * consist of a timestamp, a machine ID and a counter.
 * @author Michel Kraemer
 */
public class ObjectId {
    private final int timestamp;
    private final int counter;
    private final int randomValue1;
    private final short randomValue2;

    /**
     * Constructs a new identifier
     * @param timestamp the timestamp
     * @param counter the counter
     * @param randomValue1 a random value
     * @param randomValue2 a random value
     */
    public ObjectId(int timestamp, int counter, int randomValue1, short randomValue2) {
        this.timestamp = timestamp;
        this.counter = counter;
        this.randomValue1 = randomValue1;
        this.randomValue2 = randomValue2;
    }

    /**
     * Constructs a new identifier from legacy parameters
     * @param time the timestamp
     * @param machine the machine ID
     * @param inc the counter
     * @deprecated this constructor uses the legacy format of {@link ObjectId}.
     * Please use the modern {@link #ObjectId(int, int, int, short)} instead.
     */
    @Deprecated
    public ObjectId(int time, int machine, int inc) {
        this.timestamp = time;
        this.randomValue1 = (machine >> 8) & 0xFFFFFF;
        this.randomValue2 = (short)((machine & 0xFF) << 8 | (inc >> 24) & 0xFF);
        this.counter = inc & 0xFFFFFF;
    }

    /**
     * @return the timestamp
     * @deprecated Use {@link #getTimestamp()} instead
     */
    @Deprecated
    public int getTime() {
        return timestamp;
    }

    /**
     * @return the timestamp
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * @return the machine ID
     * @deprecated This method will be removed in a subsequent version of
     * bson4jackson. There is no replacement
     */
    @Deprecated
    public int getMachine() {
        return (randomValue1 & 0xFFFFFF) << 8 | (randomValue2 >> 8) & 0xFF;
    }

    /**
     * @return the counter
     * @deprecated Use {@link #getCounter()}
     */
    @Deprecated
    public int getInc() {
        return (randomValue2 & 0xFF) << 24 | counter;
    }

    /**
     * @return the counter
     */
    public int getCounter() {
        return counter;
    }

    /**
     * @return a random value
     */
    public int getRandomValue1() {
        return randomValue1;
    }

    /**
     * @return a random value
     */
    public short getRandomValue2() {
        return randomValue2;
    }
}
