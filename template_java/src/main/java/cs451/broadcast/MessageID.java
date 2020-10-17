package cs451.broadcast;

import java.util.Objects;

class MessageID {
    final int senderID;
    final long messageID;

    MessageID(int senderID, long messageID) {
        this.senderID = senderID;
        this.messageID = messageID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageID that = (MessageID) o;
        return senderID == that.senderID &&
                messageID == that.messageID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderID, messageID);
    }

    @Override
    public String toString() {
        return "MessageID{" +
                "senderID=" + senderID +
                ", messageID=" + messageID +
                '}';
    }
}
