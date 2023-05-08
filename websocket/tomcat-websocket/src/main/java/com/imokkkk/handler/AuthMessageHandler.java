package com.imokkkk.handler;

import cn.hutool.core.util.StrUtil;
import com.imokkkk.model.AuthRequest;
import com.imokkkk.model.AuthResponse;
import com.imokkkk.model.UserJoinNoticeRequest;
import com.imokkkk.util.WebSocketUtil;
import javax.websocket.Session;
import org.springframework.stereotype.Component;

/**
 * @author liuwy
 * @date 2023-05-08 10:32
 * @since 1.0
 */
@Component
public class AuthMessageHandler implements MessageHandler<AuthRequest> {

    @Override
    public void execute(Session session, AuthRequest message) {
        if (StrUtil.isBlank(message.getAccessToken())) {
            WebSocketUtil.send(
                    session,
                    AuthResponse.TYPE,
                    new AuthResponse().setCode(401).setMessage("认证 accessToken 未传入"));
            return;
        }
        // 代码简化，先直接使用 accessToken 作为 User
        WebSocketUtil.addSession(session, message.getAccessToken());
        // TODO accessToken校验
        WebSocketUtil.send(session, AuthResponse.TYPE, new AuthResponse().setCode(200));
        // 通知所有人，有人上线了
        WebSocketUtil.broadcast(
                UserJoinNoticeRequest.TYPE,
                new UserJoinNoticeRequest().setNickname(message.getAccessToken()));
    }

    @Override
    public String getType() {
        return AuthRequest.TYPE;
    }
}
