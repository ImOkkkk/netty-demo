package com.imokkkk.push.service;

import com.imokkkk.push.model.Message;

/**
 * @author liuwy
 * @date 2023-05-06 15:20
 * @since 1.0
 */
public interface PushService {

    void push(Message message);

}
