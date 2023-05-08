package com.imokkkk.handler;

import cn.hutool.core.lang.id.NanoId;
import com.imokkkk.model.SendResponse;
import com.imokkkk.model.SendToOneRequest;
import com.imokkkk.model.SendToUserRequest;
import com.imokkkk.util.WebSocketUtil;
import javax.websocket.Session;
import org.springframework.stereotype.Component;

/**
 * @author liuwy
 * @date 2023-05-08 10:49
 * @since 1.0
 */
@Component
public class SendToOneHandler implements MessageHandler<SendToOneRequest> {

    @Override
    public void execute(Session session, SendToOneRequest message) {
        WebSocketUtil.send(
                message.getToUser(),
                SendToUserRequest.TYPE,
                new SendToUserRequest()
                        .setMsgId(message.getMsgId())
                        .setContent(message.getContent()));

        // 认为发送成功
        WebSocketUtil.send(
                session,
                SendResponse.TYPE,
                new SendResponse().setCode(200).setMsgId(message.getMsgId()));
    }

    @Override
    public String getType() {
        return SendToOneRequest.TYPE;
    }
}
