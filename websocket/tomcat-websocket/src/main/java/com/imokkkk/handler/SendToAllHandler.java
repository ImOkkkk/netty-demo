package com.imokkkk.handler;

import com.imokkkk.model.SendResponse;
import com.imokkkk.model.SendToAllRequest;
import com.imokkkk.model.SendToUserRequest;
import com.imokkkk.util.WebSocketUtil;
import javax.websocket.Session;
import org.springframework.stereotype.Component;

/**
 * @author liuwy
 * @date 2023-05-08 10:57
 * @since 1.0
 */
@Component
public class SendToAllHandler implements MessageHandler<SendToAllRequest> {

    @Override
    public void execute(Session session, SendToAllRequest message) {

        // 创建转发的消息
        // 广播发送
        WebSocketUtil.broadcast(
                SendToUserRequest.TYPE,
                new SendToUserRequest()
                        .setMsgId(message.getMsgId())
                        .setContent(message.getContent()));

        // 这里，假装直接成功
        WebSocketUtil.send(
                session,
                SendResponse.TYPE,
                new SendResponse().setMsgId(message.getMsgId()).setCode(200));
    }

    @Override
    public String getType() {
        return SendToAllRequest.TYPE;
    }
}
