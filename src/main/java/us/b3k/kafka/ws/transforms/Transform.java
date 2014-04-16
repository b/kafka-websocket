package us.b3k.kafka.ws.transforms;

import us.b3k.kafka.ws.messages.BinaryMessage;
import us.b3k.kafka.ws.messages.TextMessage;

import javax.websocket.Session;

public class Transform {
    public void initialize() {

    }

    public TextMessage transform(TextMessage message, final Session session) {
        return message;
    }

    public BinaryMessage transform(BinaryMessage message, final Session session) {
        return message;
    }
}
