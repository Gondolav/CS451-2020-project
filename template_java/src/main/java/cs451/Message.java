package cs451;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class Message {

    private final int seqNb;
    private final byte senderNb; // p in the book
    private final byte originalSenderNb; // s in the book
    private final byte isAck;

    public Message(int seqNb, byte senderNb, byte originalSenderNb, boolean isAck) {
        this.seqNb = seqNb;
        this.senderNb = senderNb;
        this.originalSenderNb = originalSenderNb;
        this.isAck = (byte) (isAck ? 1 : 0);
    }

    private Message(int seqNb, byte senderNb, byte originalSenderNb, byte isAck) {
        this.seqNb = seqNb;
        this.senderNb = senderNb;
        this.originalSenderNb = originalSenderNb;
        this.isAck = isAck;
    }

    public static Message fromByteArray(byte[] array) {
        int seqNb = ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN).getInt();
        byte senderNb = array[4];
        byte originalSenderNb = array[5];
        byte isAck = array[6];
        return new Message(seqNb, senderNb, originalSenderNb, isAck);
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

    public byte[] toByteArray() {
        byte[] seqNbArray = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(seqNb).array();
        byte[] result = new byte[7];
        System.arraycopy(seqNbArray, 0, result, 0, seqNbArray.length);
        result[4] = senderNb;
        result[5] = originalSenderNb;
        result[6] = isAck;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return seqNb == message.seqNb &&
                senderNb == message.senderNb &&
                originalSenderNb == message.originalSenderNb &&
                isAck == message.isAck;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seqNb, senderNb, originalSenderNb, isAck);
    }

    @Override
    public String toString() {
        return "Message{" +
                "seqNb=" + seqNb +
                ", senderNb=" + senderNb +
                ", originalSenderNb=" + originalSenderNb +
                ", isAck=" + isAck +
                '}';
    }
}
