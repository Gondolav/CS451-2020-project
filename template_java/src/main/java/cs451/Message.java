package cs451;

public class Message {

    private final int seqNb;

    private final int senderNb;

    public Message(int seqNb, int senderNb) {
        this.seqNb = seqNb;
        this.senderNb = senderNb;
    }

    public int getSeqNb() {
        return seqNb;
    }

    public int getSenderNb() {
        return senderNb;
    }
}
