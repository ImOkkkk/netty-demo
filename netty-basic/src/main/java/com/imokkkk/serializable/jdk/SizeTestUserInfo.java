package com.imokkkk.serializable.jdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author liuwy
 * @date 2023-04-24 13:48
 * @since 1.0
 */
public class SizeTestUserInfo {
    public static void main(String[] args) throws Exception {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserID(100);
        userInfo.setUserName("Netty!Netty!Netty!");

        // JDK序列化
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        byte[] bytes = userInfo.jdkSerializable(bos, oos);
        System.out.println("JDK serializable size is：" + bytes.length);
        bos.close();

        // 自定义序列化
        System.out.println("Custom serializable size is：" + userInfo.codeC(null).length);
    }
}
