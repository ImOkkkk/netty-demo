package com.imokkkk.handler;

import cn.hutool.core.util.StrUtil;

import com.imokkkk.model.AuthRequest;
import com.imokkkk.model.AuthResponse;
import com.imokkkk.util.WebSocketUtil;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author liuwy
 * @date 2023-05-08 10:32
 * @since 1.0
 */
@Component
public class AuthMessageHandler implements MessageHandler<AuthRequest> {

    @Override
    public void execute(WebSocketSession session, AuthRequest message) {
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
    }

    @Override
    public String getType() {
        return AuthRequest.TYPE;
    }
}
