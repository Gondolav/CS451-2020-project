package cs451;

import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable {

    private final int seqNb;
    private final int senderNb; // p in the book
    private final int originalSenderNb; // s in the book
    private final boolean isAck;

    public Message(int seqNb, int senderNb, int originalSenderNb, boolean isAck) {
        this.seqNb = seqNb;
        this.senderNb = senderNb;
        this.originalSenderNb = originalSenderNb;
        this.isAck = isAck;
    }

    public int getSeqNb() {
        return seqNb;
    }

    public int getSenderNb() {
        return senderNb;
    }

    public int getOriginalSenderNb() {
        return originalSenderNb;
    }

    public boolean isAck() {
        return isAck;
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
