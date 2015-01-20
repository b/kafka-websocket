package us.b3k.kafka.ws.transforms;

import us.b3k.kafka.ws.messages.AbstractMessage;
import us.b3k.kafka.ws.messages.BinaryMessage;
import us.b3k.kafka.ws.messages.TextMessage;

import javax.websocket.Session;

public class DiscardTransform extends Transform {
    @Override
    public AbstractMessage transform(TextMessage message, final Session session) {
        message.setDiscard(true);
        return message;
    }

    @Override
    public AbstractMessage transform(BinaryMessage message, final Session session) {
        message.setDiscard(true);
        return message;
    }
}
