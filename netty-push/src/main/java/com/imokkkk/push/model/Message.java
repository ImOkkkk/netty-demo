package com.imokkkk.push.model;

import cn.hutool.core.lang.id.NanoId;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author liuwy
 * @date 2023-05-06 14:40
 * @since 1.0
 */
@Data
public class Message {
    private String userId;

    private String messageId;

    @NotBlank(message = "发送的消息不能为空！")
    private String messageText;

    private Date pushDate;

    public Message complete() {
        this.pushDate = new Date();
        this.messageId = NanoId.randomNanoId();
        return this;
    }
}
