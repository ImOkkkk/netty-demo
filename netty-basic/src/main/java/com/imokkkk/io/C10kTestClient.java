package com.imokkkk.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.stream.IntStream;

/**
 * @author liuwy
 * @date 2023-06-06 9:37
 * @since 1.0
 */
public class C10kTestClient {
    static String ip = "127.0.0.1";

    public static void main(String[] args) throws IOException {
        LinkedList<SocketChannel> clients = new LinkedList<>();
        InetSocketAddress serverAddr = new InetSocketAddress(ip, 9998);
        IntStream.range(20000, 50000).forEach(i -> {
            try{
                SocketChannel client = SocketChannel.open();
                client.bind(new InetSocketAddress(ip, i));
                client.connect(serverAddr);
                System.out.println("client:" + i + " connected");
                clients.add(client);
            }catch (IOException e){
                System.out.println("IOException" + i);
                e.printStackTrace();
            }
        });
        System.out.println("clients.size: " + clients.size());
        //阻塞主线程
        System.in.read();
    }
}
