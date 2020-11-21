package cs451.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

public class Message {

    private final int seqNb;
    private final byte senderNb; // p in the book
    private final byte originalSenderNb; // s in the book
    private final byte isAck;
    private final int[] vectorClock;

    public Message(int seqNb, byte senderNb, byte originalSenderNb, boolean isAck, int[] vectorClock) {
        this.seqNb = seqNb;
        this.senderNb = senderNb;
        this.originalSenderNb = originalSenderNb;
        this.isAck = (byte) (isAck ? 1 : 0);
        this.vectorClock = vectorClock == null ? null : vectorClock.clone();
    }

    private Message(int seqNb, byte senderNb, byte originalSenderNb, byte isAck, int[] vectorClock) {
        this.seqNb = seqNb;
        this.senderNb = senderNb;
        this.originalSenderNb = originalSenderNb;
        this.isAck = isAck;
        this.vectorClock = vectorClock == null ? null : vectorClock.clone();
    }

    public static Message fromByteArray(byte[] array) {
        int seqNb = ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN).getInt();
        byte senderNb = array[4];
        byte originalSenderNb = array[5];
        byte isAck = array[6];
        byte vectorClockSize = array[7];

        if (vectorClockSize > 0) {
            ByteBuffer vectorClockBuffer = ByteBuffer.wrap(array, 8, vectorClockSize).order(ByteOrder.LITTLE_ENDIAN);
            int[] vectorClock = new int[(int) Math.ceil(vectorClockSize / 4.0)];
            for (int i = 0; i < vectorClock.length; i++) {
                vectorClock[i] = vectorClockBuffer.getInt();
            }
            return new Message(seqNb, senderNb, originalSenderNb, isAck, vectorClock);
        }

        return new Message(seqNb, senderNb, originalSenderNb, isAck, null);
    }

    public int getSeqNb() {
        return seqNb;
    }

    public byte getSenderNb() {
        return senderNb;
    }

    public byte getOriginalSenderNb() {
        return originalSenderNb;
    }

    public boolean isAck() {
        return isAck == 1;
    }

    public int[] getVectorClock() {
        return vectorClock == null ? null : vectorClock.clone();
    }

    public byte[] toByteArray() {
        byte[] seqNbArray = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(seqNb).array();
        byte[] result = new byte[8];
        System.arraycopy(seqNbArray, 0, result, 0, seqNbArray.length);
        result[4] = senderNb;
        result[5] = originalSenderNb;
        result[6] = isAck;
        result[7] = 0;
        if (vectorClock == null) {
            return result;
        } else {
            var vectorClockSize = vectorClock.length * 4;
            var buffer = ByteBuffer.allocate(vectorClockSize).order(ByteOrder.LITTLE_ENDIAN);
            for (var i : vectorClock) {
                buffer.putInt(i);
            }
            byte[] newResult = new byte[8 + vectorClockSize];
            System.arraycopy(result, 0, newResult, 0, result.length);
            newResult[7] = (byte) vectorClockSize;

            byte[] bufferArray = buffer.array();
            System.arraycopy(bufferArray, 0, newResult, 8, bufferArray.length);

            return newResult;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return seqNb == message.seqNb &&
                senderNb == message.senderNb &&
                originalSenderNb == message.originalSenderNb &&
                isAck == message.isAck &&
                Arrays.equals(vectorClock, message.vectorClock);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(seqNb, senderNb, originalSenderNb, isAck);
        result = 31 * result + Arrays.hashCode(vectorClock);
        return result;
    }

    @Override
    public String toString() {
        return "Message{" +
                "seqNb=" + seqNb +
                ", senderNb=" + senderNb +
                ", originalSenderNb=" + originalSenderNb +
                ", isAck=" + isAck +
                ", vectorClock=" + Arrays.toString(vectorClock) +
                '}';
    }
}
