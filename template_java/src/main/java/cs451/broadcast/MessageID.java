package cs451.broadcast;

import java.util.Objects;

class MessageID {
    final byte senderNb;
    final int messageNb;

    MessageID(byte senderNb, int messageNb) {
        this.senderNb = senderNb;
        this.messageNb = messageNb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageID that = (MessageID) o;
        return senderNb == that.senderNb &&
                messageNb == that.messageNb;
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderNb, messageNb);
    }

    @Override
    public String toString() {
        return "MessageID{" +
                "senderNb=" + senderNb +
                ", messageNb=" + messageNb +
                '}';
    }
}
