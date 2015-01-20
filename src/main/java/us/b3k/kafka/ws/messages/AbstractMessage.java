package us.b3k.kafka.ws.messages;

public abstract class AbstractMessage {
    protected String topic;
    protected Boolean discard = false;

    public abstract Boolean isKeyed();
    public abstract byte[] getMessageBytes();

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Boolean isDiscard() {
        return this.discard;
    }

    public void setDiscard(Boolean discard) {
        this.discard = discard;
    }

    public abstract String getKey();


}
