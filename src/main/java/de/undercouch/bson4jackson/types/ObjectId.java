package de.undercouch.bson4jackson.types;

/**
 * A unique identifier for MongoDB documents. Such identifiers
 * consist of a timestamp, a machine ID and a counter.
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
     * @return the timestamp
     */
    public int getTimestamp() {
        return timestamp;
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
