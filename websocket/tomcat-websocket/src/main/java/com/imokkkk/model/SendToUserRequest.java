package com.imokkkk.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author liuwy
 * @date 2023-05-08 10:29
 * @since 1.0
 */

@Data
@Accessors(chain = true)
public class SendToUserRequest implements Message {

    public static final String TYPE = "SEND_TO_USER_REQUEST";

    /** 消息编号 */
    private String msgId;
    /** 内容 */
    private String content;
}
