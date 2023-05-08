package com.imokkkk.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author liuwy
 * @date 2023-05-08 10:26
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class UserJoinNoticeRequest implements Message {

    public static final String TYPE = "USER_JOIN_NOTICE_REQUEST";

    /** 昵称 */
    private String nickname;
}
