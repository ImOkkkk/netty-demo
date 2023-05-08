package com.imokkkk.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author liuwy
 * @date 2023-05-08 10:28
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class SendResponse implements Message {

    public static final String TYPE = "SEND_RESPONSE";

    /** 消息编号 */
    private String msgId;
    /** 响应状态码 */
    private Integer code;
    /** 响应提示 */
    private String message;
}
