package us.b3k.kafka.ws.messages;

import org.junit.Test;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class BinaryMessageTest {
    public static BinaryMessage.BinaryMessageEncoder encoder = new BinaryMessage.BinaryMessageEncoder();
    public static BinaryMessage.BinaryMessageDecoder decoder = new BinaryMessage.BinaryMessageDecoder();

    public static byte[] message =
            new byte[] { 8, 109, 121, 95, 116, 111, 112, 105, 99, 109,
                         121, 32, 97, 119, 101, 115, 111, 109, 101, 32,
                         109, 101, 115, 115, 97, 103, 101 };

    @Test
    public void binaryToMessage() throws DecodeException, EncodeException, UnsupportedEncodingException {
        BinaryMessage binaryMessage = decoder.decode(ByteBuffer.wrap(message));
        assertEquals(binaryMessage.getTopic(), "my_topic");
        assertEquals(new String(binaryMessage.getMessage()), "my awesome message");
    }

    @Test
    public void messageToBinary() throws EncodeException {
        BinaryMessage binaryMessage =
                new BinaryMessage("my_topic", "my awesome message".getBytes(Charset.forName("UTF-8")));
        assertArrayEquals(encoder.encode(binaryMessage).array(), message);
    }
}
