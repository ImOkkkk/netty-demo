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
 * @date 2023-06-06 10:14
 * @since 1.0
 */
// 多路复用：多路(多个客户端)复用(复用一次系统调用)，实现同时监控多个文件描述符(包括socket、文件和标准输入输出等)。
// 它可以通过一个进程同时接受多个连接请求或处理多个文件的IO操作，提高程序的效率和响应速度。
// 即一次系统调用，就能得到多个客户端是否有读写事件。

/** 多路复用依赖内核的能力，不同的操作系统都有自己不同的多路复用实现，以linux为例：
 阶段1：select&poll
 select：
    int select(int nfds, //要监视的文件描述符数量
        fd_set *restrict readfds, //可读文件描述符集合
        fd_set *restrict writefds, //可写文件描述符集合
        fd_set *restrict errorfds, //异常文件描述符集合
        struct timeval *restrict timeout//超时时间
        )；
 之前由用户态遍历所有客户端产生系统调用(如果10k个socket，需要产生10k个系统调用)，改成了由内核遍历，如果select模式，
 只需要10个系统调用(select最大支持传入1024个文件描述符)，如果是poll模式(不限制文件描述符数量)，则只需要1次系统调用。
 poll与select的区别：
    实现机制：select使用轮询的方式来查询文件描述符上是否有事件发生，而poll使用链表来存储文件描述符，查询时只需要对链表进行遍历；
    文件描述符的数量限制不同：select最大支持1024个文件描述符，poll没有数量限制；
    阻塞方式不同：select会阻塞整个进程，而poll可以只阻塞等待的文件描述符；
    可移植性不同：select可以在各种操作系统上使用，而poll是linux特有的函数。
 存在的问题：大量的fd(即连接)需要再用户态和内核态相互拷贝
 阶段2：epoll
 空间换时间，在内核为应用程序开辟一块空间。https://blog.csdn.net/shift_wwx/article/details/104275383
    - 应用程序调用内核系统调用，开辟内存空间：int epoll_create(int size)
    - 应用程序新连接，注册到对应的内核空间：int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event)
    - 应用程序询问需要处理的连接：有读、写、错误的事件；int epoll_wait(int epfd, struct epoll_event *events, int maxevents, int timeout);

 */
public class SelectorNIOSimple {
    private Selector selector = null;
    int port = 9998;

    public static void main(String[] args) {
        SelectorNIOSimple service = new SelectorNIOSimple();
        service.start();
    }

    private void initServer() {
        try {
            ServerSocketChannel server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));
            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        initServer();
        while (true) {
            try {
                Set<SelectionKey> keys = selector.keys();
                while (selector.select() > 0) {
                    // 返回待处理的文件描述符集合
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        // 使用后需移除，否则会被一直处理
                        iterator.remove();
                        if (key.isAcceptable()) {
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            readHandler(key);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void acceptHandler(SelectionKey key) {
        try {
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel client = ssc.accept();
            client.configureBlocking(false);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            client.register(selector, SelectionKey.OP_READ, buffer);
            System.out.println("client connected：" + client.getRemoteAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readHandler(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        buffer.clear();
        int read;
        try {
            while (true) {
                read = client.read(buffer);
                if (read > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        client.write(buffer);
                    }
                    buffer.clear();
                } else if (read == 0) {
                    break;
                } else {
                    client.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
