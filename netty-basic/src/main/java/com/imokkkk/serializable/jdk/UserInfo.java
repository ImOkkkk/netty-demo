package com.imokkkk.serializable.jdk;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * @author Mark老师 类说明：实体类
 */
public class UserInfo implements Serializable {

    private static final long serialVersionUID = -1896972598384122503L;
    /** 默认的序列号 */
    private String userName;

    private int userID;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    // 自定义序列化
    public byte[] codeC(ByteBuffer buffer) {
        if (buffer != null) {
            buffer.clear();
        } else {
            buffer = ByteBuffer.allocate(1024);
        }

        byte[] userNameBytes = this.userName.getBytes();
        buffer.putInt(userNameBytes.length);
        buffer.put(userNameBytes);

        buffer.putInt(this.userID);
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        return bytes;
    }

    public byte[] jdkSerializable(ByteArrayOutputStream bos, ObjectOutputStream oos)
            throws Exception {
        oos.writeObject(this);
        oos.flush();
        oos.close();
        byte[] bytes = bos.toByteArray();
        return bytes;
    }
}
