package com.imokkkk.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author liuwy
 * @date 2023-05-08 10:23
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AuthRequest implements Message {

    public static final String TYPE = "AUTH_REQUEST";

    /** 认证 Token */
    private String accessToken;
}
