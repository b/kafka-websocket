package us.b3k.kafka.ws.messages;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;

import javax.websocket.*;

/*
 text messages are JSON strings of the form

 {"topic": "my_topic", "message": "my amazing message" }

 both attributes are required and any other attributes will be ignored (and lost)
 */
public class TextMessage {
    private static Logger LOG = Logger.getLogger(TextMessage.class);

    private String topic;
    private String message;

    public TextMessage(String topic, String message) {
        this.topic = topic;
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    static public class TextMessageDecoder implements Decoder.Text<TextMessage> {
        public TextMessageDecoder() {

        }

        @Override
        public TextMessage decode(String s) throws DecodeException {
            JsonObject jsonObject = new JsonParser().parse(s).getAsJsonObject();
            if (jsonObject.has("topic") && jsonObject.has("message")) {
                String topic = jsonObject.getAsJsonPrimitive("topic").getAsString();
                String message = jsonObject.getAsJsonPrimitive("message").getAsString();

                return new TextMessage(topic, message);
            } else {
                throw new DecodeException(s, "Missing required fields");
            }
        }

        @Override
        public boolean willDecode(String s) {
            return true;
        }

        @Override
        public void init(EndpointConfig endpointConfig) {

        }

        @Override
        public void destroy() {

        }
    }

    static public class TextMessageEncoder implements Encoder.Text<TextMessage> {
        public TextMessageEncoder() {

        }

        @Override
        public String encode(TextMessage textMessage) throws EncodeException {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("topic", textMessage.getTopic());
            jsonObject.addProperty("message", textMessage.getMessage());

            return jsonObject.toString();
        }

        @Override
        public void init(EndpointConfig endpointConfig) {

        }

        @Override
        public void destroy() {

        }
    }
}
