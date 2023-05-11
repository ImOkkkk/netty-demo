package com.imokkkk.model;

import com.imokkkk.model.Message.Header;

/**
 * @author liuwy
 * @date 2023-05-08 15:19
 * @since 1.0
 */
public enum MessageType {
    SERVICE_REQ((byte) 0), /*业务请求消息*/
    SERVICE_RESP((byte) 1), /*TWO_WAY消息，需要业务应答*/
    ONE_WAY((byte) 2), /*无需应答的业务请求消息*/
    LOGIN_REQ((byte) 3), /*登录请求消息*/
    LOGIN_RESP((byte) 4), /*登录响应消息*/
    HEARTBEAT_REQ((byte) 5), /*心跳请求消息*/
    HEARTBEAT_RESP((byte) 6); /*心跳应答消息*/

    private byte value;

    private MessageType(byte value) {
        this.value = value;
    }

    public byte value() {
        return this.value;
    }

    public boolean same(Header header){
        return header == null ? false : this.value == header.getType();
    }
}
