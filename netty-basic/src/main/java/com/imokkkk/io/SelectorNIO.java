package com.imokkkk.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author liuwy
 * @date 2023-06-06 13:23
 * @since 1.0
 */
// 使用PollSelectorProvider作为多路复用器的实现(Unix-like系统中默认使用EPollSelectorProvider，Windows系统中默认使用WindowsSelectorProvider)：
// java -Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.PollSelectorProvider MyApp
public class SelectorNIO {
    private Selector selector = null;
    int port = 9998;

    public static void main(String[] args) {
        SelectorNIO service = new SelectorNIO();
        service.start();
    }

    private void start(){
        initServer();
        System.out.println("server start");
        while (true){
            try{
                Set<SelectionKey> keys = selector.keys();
                System.out.println("可处理事件数量：" + keys.size());
                //对应系统调用：
                //select/poll模式下：调用内核select(fd4) poll(fd4)
                //epoll：调用内核epoll_wait()
                while (selector.select() > 0){
                    //返回的待处理的文件描述符集合
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while(iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        //使用后需移除，否则会被一直处理
                        iterator.remove();
                        if(key.isAcceptable()){
                            //对应系统调用：
                            //select/poll模式下：因为内核未开辟空间，那么在jvm中存放fd4的数组空间
                            //epoll：过epoll_ctl把新客户端fd注册到内核空间
                            acceptHandler(key);
                        }else if(key.isReadable()){
                            readHandler(key);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void initServer() {
        try {
            ServerSocketChannel server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));
            // epoll模式下，open会调用一个系统调用 epoll_create 返回文件描述符 fd3
            selector = Selector.open();
            // 对应系统调用：
            // select/poll模式下：jvm里开辟一个文件描述符数组，并把fd4加入
            // epoll模式下：调用内核epoll_ctl(fd3, ADD, fd4, EPOLLIN)
            server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptHandler(SelectionKey key) {
        try {
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            // 接受新客户端
            SocketChannel client = ssc.accept();
            // 设置非阻塞
            client.configureBlocking(false);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            // 对应系统调用
            // select，poll模式下：jvm里开辟一个数组存入 fd7
            // epoll模式下： 调用 epoll_ctl(fd3, ADD, fd7, EPOLLIN)
            client.register(selector, SelectionKey.OP_READ, buffer);
            System.out.println("client connected：" + client.getRemoteAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readHandler(SelectionKey key){
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        buffer.clear();
        int read;
        try{
            while (true){
                read = client.read(buffer);
                if(read > 0){
                    buffer.flip();
                    while (buffer.hasRemaining()){
                        client.write(buffer);
                    }
                    buffer.clear();
                }else if(read == 0){
                    break;
                }else{
                    client.close();
                    break;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
