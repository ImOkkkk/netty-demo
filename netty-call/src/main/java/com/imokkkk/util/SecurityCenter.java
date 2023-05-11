package com.imokkkk.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author liuwy
 * @date 2023-05-09 15:57
 * @since 1.0
 */
public class SecurityCenter {
    // 用以检查用户是否重复登录的缓存
    private static Map<String, Boolean> existUser = new ConcurrentHashMap<String, Boolean>();
    // 用户登录的白名单
    private static Set<String> whiteList = new CopyOnWriteArraySet<>();

    static {
        whiteList.add("127.0.0.1");
    }

    public static boolean isWhiteIP(String ip) {
        return whiteList.contains(ip);
    }

    public static boolean isDupLog(String usrInfo) {
        return existUser.containsKey(usrInfo);
    }

    public static void addLoginUser(String usrInfo) {
        existUser.put(usrInfo, true);
    }

    public static void removeLoginUser(String usrInfo) {
        existUser.remove(usrInfo, true);
    }
}
