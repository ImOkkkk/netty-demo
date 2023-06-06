package com.imokkkk.io;

import cn.hutool.core.util.StrUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author liuwy
 * @date 2023-06-06 9:42
 * @since 1.0
 */
//BIO：同步阻塞I/O，传统的I/O模型。在进行I/O操作时，必须等待数据读取或写入完成后才能进行下一步操作。
// 1.一个连接需要一个线程，一台机器开辟线程数量有限；
// 2.线程是轻量级进程，操作系统会为每一个线程分配1M独立的栈空间，如果要处理c10k(10000个连接)，就得有10G的栈空间；
// 3.即使内存空间足够，一台机器的CPU核数也是有限的，CPU大量时间耗费在线程调度而不是业务处理上
public class BIOServer {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(9998, 20);
        System.out.println("server begin");
        while (true) {
            // 阻塞1：接收到一个客户端连接后才能继续运行
            Socket client = server.accept();
            System.out.println("accept client" + client.getPort());
            new Thread(
                            () -> {
                                try {
                                    InputStream in = client.getInputStream();
                                    BufferedReader reader =
                                            new BufferedReader(new InputStreamReader(in));
                                    while (true) {
                                        // 阻塞2：如果是单线程，则线程挂起，那么只能处理极少数连接，所以使用多线程
                                        String data = reader.readLine();
                                        if (StrUtil.isNotBlank(data)) {
                                            System.out.println(data);
                                        } else {
                                            client.close();
                                            break;
                                        }
                                    }
                                    System.out.println("client break");
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                    .start();
        }
    }
}
