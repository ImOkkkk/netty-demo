package com.imokkkk.model;

import lombok.Data;

/**
 * @author liuwy
 * @date 2023-05-08 10:28
 * @since 1.0
 */
@Data
public class SendToAllRequest implements Message {
    public static final String TYPE = "SEND_TO_ALL_REQUEST";

    /** 消息编号 */
    private String msgId;
    /** 内容 */
    private String content;
}
