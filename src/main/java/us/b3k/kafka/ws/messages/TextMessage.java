/*
    Copyright 2014 Benjamin Black

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package us.b3k.kafka.ws.messages;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;

/*
 text messages are JSON strings of the form

 {"topic": "my_topic", "message": "my amazing message" }

 both attributes are required and any other attributes will be ignored (and lost)
 */
public class TextMessage {
    private static Logger LOG = LoggerFactory.getLogger(TextMessage.class);

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
        static public final JsonParser jsonParser = new JsonParser();

        public TextMessageDecoder() {

        }

        @Override
        public TextMessage decode(String s) throws DecodeException {
            JsonObject jsonObject = TextMessageDecoder.jsonParser.parse(s).getAsJsonObject();
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
