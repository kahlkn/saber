package artoria.identifier;

import artoria.time.Clock;
import artoria.time.SystemClock;

import static artoria.common.Constants.ONE;
import static artoria.common.Constants.ZERO;

/**
 * Id generator implement by snow flake id simple.
 * @author Kahle
 */
public class SnowFlakeIdGenerator implements LongIdentifierGenerator {
    /**
     * The offset of the timestamp (2018-06-06T06:06:06.666).
     */
    private static final long TIME_OFFSET = 1528236366666L;
    /**
     * The number of bits of machine id.
     */
    private static final long WORKER_ID_BITS = 5L;
    /**
     * The number of bits of data center id.
     */
    private static final long DATA_CENTER_ID_BITS = 5L;
    /**
     * The maximum supported machine id, and the result is 31.
     */
    private static final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);
    /**
     * The maximum supported data center id, and the result is 31.
     */
    private static final long MAX_DATA_CENTER_ID = -1L ^ (-1L << DATA_CENTER_ID_BITS);
    /**
     * The number of bits of a sequence in a snow flake id.
     */
    private static final long SEQUENCE_BITS = 12L;
    /**
     * The mask of the generated sequence, and the result is 4095.
     * (0b111111111111 = 0xfff = 4095)
     */
    private static final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);
    /**
     * The machine id moves 12 bits to the left.
     */
    private static final long WORKER_ID_LEFT_SHIFT = SEQUENCE_BITS;
    /**
     * The data center id moves 17 bits to the left (12 + 5).
     */
    private static final long DATA_CENTER_ID_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    /**
     * The timestamp moves 22 bits to the left (5 + 5 + 12).
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;
    /**
     * The timestamp when the snow flake id was last generated.
     */
    private long lastTimestamp = -1L;
    /**
     * Sequence in millisecond (0~4095).
     */
    private long sequence = 0L;
    /**
     * Data center id (0~31).
     */
    private long dataCenterId;
    /**
     * Work machine id (0~31).
     */
    private long workerId;
    /**
     * The clock used to get the current timestamp.
     */
    private Clock clock;

    /**
     * No-parameter constructor method.
     */
    public SnowFlakeIdGenerator() {

        this(ZERO, ZERO, new SystemClock());
    }

    /**
     * The constructor method.
     * @param dataCenterId The data center id
     * @param workerId The work machine id
     * @param clock The clock
     */
    public SnowFlakeIdGenerator(long dataCenterId, long workerId, Clock clock) {
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < ZERO) {
            throw new IllegalArgumentException(
                    "Data center id can't be greater than " + MAX_DATA_CENTER_ID + " or less than 0"
            );
        }
        if (workerId > MAX_WORKER_ID || workerId < ZERO) {
            throw new IllegalArgumentException(
                    "Worker id can't be greater than " + MAX_WORKER_ID + " or less than 0"
            );
        }
        if (clock == null) {
            throw new IllegalArgumentException(
                    "The clock can't be null"
            );
        }
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
        this.clock = clock;
    }

    /**
     * Block to the next millisecond until a new timestamp is obtained.
     * @param lastTimestamp The timestamp when id was last generated
     * @return The current timestamp
     */
    protected long waitUntilNextMillis(long lastTimestamp) {
        long currentTimestamp = clock.getTime();
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = clock.getTime();
        }
        return currentTimestamp;
    }

    @Override
    public Object nextIdentifier() {

        return nextLongIdentifier();
    }

    @Override
    public synchronized Long nextLongIdentifier() {
        long currentTimestamp = clock.getTime();
        // If the current time is less than the timestamp generated by the last id.
        // An exception should be thrown when the system clock has gone back.
        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException(
                    "Clock moved backwards. Refusing to generate id for "
                            + (lastTimestamp - currentTimestamp) + " milliseconds"
            );
        }
        // If it is generated at the same time.
        // The millisecond sequence is performed.
        if (lastTimestamp == currentTimestamp) {
            sequence = (sequence + ONE) & SEQUENCE_MASK;
            // Sequence overflow in milliseconds.
            if (sequence == ZERO) {
                // Block to the next millisecond and get the new timestamp.
                currentTimestamp = waitUntilNextMillis(lastTimestamp);
            }
        }
        else {
            // The timestamp changes and the sequence is reset in milliseconds.
            sequence = ZERO;
        }
        lastTimestamp = currentTimestamp;
        // Computes the result by shifting and or operations.
        return ((currentTimestamp - TIME_OFFSET) << TIMESTAMP_LEFT_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_LEFT_SHIFT)
                | (workerId << WORKER_ID_LEFT_SHIFT)
                | sequence;
    }

}
