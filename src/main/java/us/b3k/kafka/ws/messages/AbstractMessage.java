package us.b3k.kafka.ws.messages;

public abstract class AbstractMessage {
    protected String topic;

    public abstract Boolean isKeyed();
    public abstract byte[] getMessageBytes();

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public abstract String getKey();

    public Boolean discard = false;
}
