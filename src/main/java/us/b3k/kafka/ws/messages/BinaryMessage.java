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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class BinaryMessage {
    private static Logger LOG = LoggerFactory.getLogger(BinaryMessage.class);

    private String topic;
    private byte[] message;

    public BinaryMessage(String topic, byte[] message) {
        this.topic = topic;
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    static public class BinaryMessageDecoder implements Decoder.Binary<BinaryMessage> {
        public BinaryMessageDecoder() {

        }

        @Override
        public BinaryMessage decode(ByteBuffer byteBuffer) throws DecodeException {
            int bufLen = byteBuffer.array().length;
            int topicLen = 0;
            while (topicLen < bufLen)
            {
                if (byteBuffer.get() == 0) { break; }
                topicLen++;
            }
            String topic = new String(byteBuffer.array(), 0, topicLen, Charset.forName("UTF-8"));
            ByteBuffer messageBuf = ByteBuffer.allocate(bufLen - topicLen);
            System.arraycopy(byteBuffer.array(), topicLen + 1, messageBuf.array(), 0, bufLen - topicLen);
            return new BinaryMessage(topic, messageBuf.array());
        }

        @Override
        public boolean willDecode(ByteBuffer byteBuffer) {
            return true;
        }

        @Override
        public void init(EndpointConfig endpointConfig) {

        }

        @Override
        public void destroy() {

        }
    }

    static public class BinaryMessageEncoder implements Encoder.Binary<BinaryMessage> {
        public BinaryMessageEncoder() {

        }

        @Override
        public ByteBuffer encode(BinaryMessage binaryMessage) throws EncodeException {
            ByteBuffer buf =
                    ByteBuffer.allocate(binaryMessage.getTopic().length() + 1 + binaryMessage.getMessage().length);
            try {
                final byte nullTerm = 0;
                buf.put(binaryMessage.getTopic().getBytes("UTF-8"))
                   .put(nullTerm)
                   .put(binaryMessage.getMessage());
                return buf;
            } catch (UnsupportedEncodingException e) {
                throw new EncodeException(binaryMessage, e.getMessage(), e.getCause());
            }
        }

        @Override
        public void init(EndpointConfig endpointConfig) {

        }

        @Override
        public void destroy() {

        }
    }
}
