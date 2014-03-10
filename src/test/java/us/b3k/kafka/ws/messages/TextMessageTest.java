package us.b3k.kafka.ws.messages;

import org.junit.Test;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

import static org.junit.Assert.assertEquals;

public class TextMessageTest {
    public static TextMessage.TextMessageEncoder encoder = new TextMessage.TextMessageEncoder();
    public static TextMessage.TextMessageDecoder decoder = new TextMessage.TextMessageDecoder();

    public static String message = "{\"topic\":\"my_topic\",\"message\":\"my awesome message\"}";

    @Test
    public void textToMessage() throws DecodeException {
        TextMessage textMessage = decoder.decode(message);
        assertEquals(textMessage.getTopic(), "my_topic");
        assertEquals(textMessage.getMessage(), "my awesome message");
    }

    @Test
    public void messageToText() throws EncodeException {
        TextMessage textMessage = new TextMessage("my_topic", "my awesome message");
        assertEquals(encoder.encode(textMessage), message);
    }
}
