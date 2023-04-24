package com.imokkkk.serializable.jdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author liuwy
 * @date 2023-04-24 14:00
 * @since 1.0
 */
public class PerformTestUserInfo {
    public static void main(String[] args) throws Exception {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserID(100);
        userInfo.setUserName("Netty!Netty!Netty!");

        int loop = 5_000_000;

        long start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            userInfo.jdkSerializable(bos, oos);
            bos.close();
        }
        System.out.println("JDK serializable cost time：" + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            userInfo.codeC(null);
        }
        System.out.println("Custom serializable cost time：" + (System.currentTimeMillis() - start));
    }
}
