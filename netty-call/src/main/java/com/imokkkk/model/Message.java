package com.imokkkk.model;

import cn.hutool.core.util.ByteUtil;
import com.imokkkk.util.EncryptUtils;
import java.nio.charset.StandardCharsets;
import lombok.Data;

import java.util.Map;
import java.util.StringJoiner;

/**
 * @author liuwy
 * @date 2023-05-08 15:15
 * @since 1.0
 */
@Data
public class Message {

    private Header header;

    private Object body;

    @Override
    public String toString() {
        return new StringJoiner(", ", Message.class.getSimpleName() + "[", "]")
                .add("header=" + header)
                .add("body=" + (body == null ? null : ((Byte) body).intValue()))
                .toString();
    }

    @Data
    public static class Header {
        /*消息体的MD5摘要*/
        private String md5;

        /*消息的ID，因为是同步处理模式，不考虑应答消息需要填入请求消息ID*/
        private String msgId;

        /*消息类型*/
        private byte type;

        /*消息优先级*/
        private byte priority;

        /** 消息头额外信息 */
        private Map<String, Object> extra;

        @Override
        public String toString() {
            return new StringJoiner(", ", Header.class.getSimpleName() + "[", "]")
                    .add("md5='" + md5 + "'")
                    .add("msgId=" + msgId)
                    .add("type=" + type)
                    .add("priority=" + priority)
                    .add("extra=" + extra)
                    .toString();
        }
    }

    public Message buildBasicMessage(Object body, MessageType messageType) {
        return this.buildBasicMessage(body, null, messageType);
    }

    public Message buildBasicMessage(Object body, String msgId, MessageType messageType) {
        Header msgHeader = new Header();
        msgHeader.setType(messageType.value());
        msgHeader.setMsgId(msgId);
        msgHeader.setMd5(EncryptUtils.encryptObj(body));
        Message message = new Message();
        message.setHeader(msgHeader);
        if (body != null) {
            if (body instanceof Integer) {
                message.setBody(ByteUtil.intToByte((Integer) body));
            } else if (body instanceof String) {
                message.setBody(((String) body).getBytes(StandardCharsets.UTF_8));
            } else {
                message.setBody(body);
            }
        }
        return message;
    }
}
