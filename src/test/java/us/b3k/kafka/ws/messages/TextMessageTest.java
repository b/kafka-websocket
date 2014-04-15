package us.b3k.kafka.ws.messages;

import org.junit.Test;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TextMessageTest {
    public static TextMessage.TextMessageEncoder encoder = new TextMessage.TextMessageEncoder();
    public static TextMessage.TextMessageDecoder decoder = new TextMessage.TextMessageDecoder();

    public static String message = "{\"topic\":\"my_topic\",\"message\":\"my awesome message\"}";
    public static String keyedMessage = "{\"topic\":\"my_topic\",\"key\":\"my_key123\",\"message\":\"my awesome message\"}";

    @Test
    public void textToMessage() throws DecodeException {
        TextMessage textMessage = decoder.decode(message);
        assertEquals(textMessage.getTopic(), "my_topic");
        assertFalse(textMessage.isKeyed());
        assertEquals(textMessage.getMessage(), "my awesome message");
    }

    @Test
    public void messageToText() throws EncodeException {
        TextMessage textMessage = new TextMessage("my_topic", "my awesome message");
        assertEquals(encoder.encode(textMessage), message);
    }

    @Test
    public void textToKeyedMessage() throws DecodeException {
        TextMessage textMessage = decoder.decode(keyedMessage);
        assertEquals(textMessage.getTopic(), "my_topic");
        assertEquals(textMessage.getKey(), "my_key123");
        assertEquals(textMessage.getMessage(), "my awesome message");
    }

    @Test
    public void keyedMessageToText() throws EncodeException {
        TextMessage textMessage = new TextMessage("my_topic", "my_key123", "my awesome message");
        assertEquals(encoder.encode(textMessage), keyedMessage);
    }

}
