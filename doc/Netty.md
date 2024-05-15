## IO模型

### 用户空间和内核空间

**内核**：独立于普通的应用程序，可以访问受保护的内核空间，也有访问底层硬件设备的所有权限。

为了保护内核的安全，操作系统一般强制用户进程不能直接操作内核，所以操作系统把内存空间划分为两部分：内核空间和用户空间。

**一次IO的读取操作分为两个阶段(写入操作类似)**

- 等待内核空间数据准备阶段
- 数据从内核空间拷贝到用户空间

### 5种IO模型

#### 阻塞型IO

当用户进程发起请求时，一直阻塞直到数据拷贝到用户空间为止才返回。

阻塞型IO在两个阶段都是连续阻塞的，直到数据返回。

![CleanShot 2024-05-15 at 21.51.20](https://pic-go-image.oss-cn-beijing.aliyuncs.com/pic/CleanShot%202024-05-15%20at%2021.51.20.png)

#### 非阻塞型IO

用户进程不断询问内核，数据准备好了吗？一直重试，直到内核说数据准备好了，然后把数据从内核拷贝到用户空间，返回成功，开始处理数据。

非阻塞型IO第一阶段不阻塞，第二阶段阻塞。

![CleanShot 2024-05-15 at 21.54.40](https://pic-go-image.oss-cn-beijing.aliyuncs.com/pic/CleanShot%202024-05-15%20at%2021.54.40.png)

#### IO多路复用

多个IO操作共同使用一个selector(选择器)去询问哪些IO准备好了，selector负责通知那些数据准备好了，它们再自己去请求内核数据。

IO多路复用，第一阶段会阻塞在selector上，第二阶段拷贝数据也会阻塞。

![CleanShot 2024-05-15 at 21.58.21](https://pic-go-image.oss-cn-beijing.aliyuncs.com/pic/CleanShot%202024-05-15%20at%2021.58.21.png)

##### 实现

- **select 轮训监视多个阻塞线程的文件描述符**

  ```c
  int select(int nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, struct timeval *timeout);
  ```

  - nfds：文件描述符的数量，即文件描述符集合中最大的文件描述符加1；
  - readfds：读文件描述符集合；
  - writefds：写文件描述符集合；
  - exceptfds：异常文件描述符集合；
  - timeout：超时时间；

  select根据监听的事件类型分别创建3个文件描述符数组，然后在timeout时间内阻塞线程进行监听，直到事件发生或者超时。之后检查数组中是否有事件到达。

  **缺点**

  1. 文件描述符数组大小优先，为1024，因此高并发场景不适用；
  2. 维持3个文件描述符数组，浪费内存空间；
  3. 每次调用select需要将数组从用户空间态拷贝到内核态，同时对数组进行遍历查找，效率低。

- **poll：与select类似，但没有最大文件描述符的限制**

  ```c
  int poll(struct pollfd *fds, nfds_t nfds, int timeout);
  ```

  - fds：文件描述符数组；
  - ndfs：文件描述符数组的大小；
  - timeout：超时时间；

  与select类似，区别是只需要构建一个数组，并且可以指定数组大小

  **缺点**

  1. 调用poll仍需要将数组从用户空间态拷贝到内核态，同时对数组进行遍历查找。

- **epoll：事件驱动代替轮询扫描文件描述符**

  1. 在内核空间开辟一块指定大小的数据表，并由epdf指向这部分内存

     ```c
     int epfd = epoll_create(10);
     ```

  2. 使用epoll_ctl注册需要监听的事件

     ```c
     int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event);
     ```

     - epfd是创建数组之后的内存指针；
     - op是操作类型，包括三种模式：
       	EPOLL_CTL_ADD：添加需要监听的事件；
       	EPOLL_CTL_MOD：修改需要监听的事件；
       	EPOLL_CTL_DEL：删除需要监听的事件；
     - fd是需要监听的文件描述符，需要支持NIO；
     - event记录了注册事件的具体信息

  3. 使用epoll_wait进行监听

     ```c
     int epoll_wait(int epfd, struct epoll_event *evlist, int maxevents, int timeout);
     ```

     - epfd：创建数组之后的内存指针；
     - evlist：是用于存放事件的数组，也是返回的结果数组，包含被触发事件的对应文件描述符；和select、poll的区别，**select、poll会返回所有文件描述符然后遍历，而epoll只会返回被触发事件的文件描述符**；
     - maxevents：监听事件的最大容量；
     - timeout：超时时间。

  **优点**

  1. 只返回触发事件的文件描述符，避免了整个数组的遍历；
  2. 支持水平触发和边缘触发两种模式。

#### 信号驱动IO

用户进程发起读请求之前先注册一个信号给内核说明自己需要什么数据，这个注册请求立即返回，等内核数据准备好了，主动通知用户进程，用户进程再去请求读取数据，此时，需要等待数据从内核空间拷贝到用户空间再返回。

信号驱动，第一阶段不阻塞，第二阶段阻塞。

![CleanShot 2024-05-15 at 22.02.18](https://pic-go-image.oss-cn-beijing.aliyuncs.com/pic/CleanShot%202024-05-15%20at%2022.02.18.png)

#### 异步IO

用户进程发起读取请求后立即返回，当数据完全拷贝到用户空间后通知用户直接使用数据。

异步IO，两个阶段都不阻塞。

![CleanShot 2024-05-15 at 22.03.57](https://pic-go-image.oss-cn-beijing.aliyuncs.com/pic/CleanShot%202024-05-15%20at%2022.03.57.png)

### 阻塞与非阻塞

- 阻塞：调用结果返回之前，当前线程会被挂起，直到调用结果返回。
- 非阻塞：不能立即得到结果之前，当前线程不被挂起，而是可以继续做其它的事。

### 同步与异步

- 同步：调用者会被阻塞直到IO操作完成，调用的结果随请求的结束而返回。

- 异步：调用则不会被阻塞，调用的结果不随请求的结束而返回，而是通过通知或回调函数的形式返回。

  > 阻塞/非阻塞：关心的是当前线程是不是被挂起。
  >
  > 同步/异步：关心的调用结果是不是随请求结束而返回。

所以，阻塞型IO、非阻塞型IO、IO多路复用、信号驱动IO都是同步IO，只有最后一种才是异步IO。

## Java中如何使用BIO、NIO、AIO

linux系统上AIO还不成熟，现在NIO最流行。

> BIO，阻塞型IO，也称为OIO，Old IO。
>
> NIO，New IO，Java中使用IO多路复用技术实现，JDK1.4引入。
>
> AIO，异步IO，又称为NIO2，JDK1.7引入。

### BIO

```java
public class BIOEchoServer {
    //curl 127.0.0.1:8001
    public static void main(String[] args) throws IOException {
        // 启动服务端，绑定8001端口
        ServerSocket serverSocket = new ServerSocket(8001);
        System.out.println("server start");
        while (true) {
            // 开始接受客户端连接
            Socket socket = serverSocket.accept();
            System.out.println("one client connection：" + socket);
            // 启动线程处理连接数据
            new Thread(
                            () -> {
                                try {
                                    BufferedReader reader =
                                            new BufferedReader(
                                                    new InputStreamReader(socket.getInputStream()));
                                    String msg;
                                    while ((msg = reader.readLine()) != null) {
                                        System.out.println("receive msg：" + msg);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            })
                    .start();
        }
    }
}
```

![CleanShot 2024-05-15 at 22.37.48](https://pic-go-image.oss-cn-beijing.aliyuncs.com/pic/CleanShot%202024-05-15%20at%2022.37.48.png)

每来一个客户端连接都要分配一个线程，如果客户端一直增加，服务端线程会无限增加，直到服务器资源耗尽。

### NIO

```java
public class NIOEchoServer {
    public static void main(String[] args) throws IOException {
        // 创建一个Selector
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8002));
        // 设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        // 将Channel注册到selector上，并注册Accept事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("server start");
        while (true) {
            // 阻塞在select上(第一阶段阻塞)
            selector.select();
            // 如果使用的是select(timeout)或selectNow()需要判断返回值是否大于0
            // 有就绪的Channel
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                // 如果是accept事件
                if (selectionKey.isAcceptable()) {
                    // 强制转换为ServerSocketChannel
                    ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel socketChannel = ssc.accept();
                    System.out.println("accept new connection：" + socketChannel.getRemoteAddress());
                    socketChannel.configureBlocking(false);
                    // 将SocketChannel注册到Selector上，并注册读事件
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    // 强制转换为SocketChannel
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    // 创建Buffer用于读取数据
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    // 将数据读入到buffer中(第二阶段阻塞)
                    int length = socketChannel.read(buffer);
                    if (length > 0) {
                        buffer.flip();
                        byte[] bytes = new byte[buffer.remaining()];
                        // 将数据读入到byte数组中
                        buffer.get(bytes);
                        // 换行符会跟着消息一起传过来
                        String content = new String(bytes, "UTF-8").replace("\r\n", "");
                        System.out.println("receive msg：" + content);
                    }
                }
                iterator.remove();
            }
        }
    }
}

```

![CleanShot 2024-05-15 at 22.56.04](https://pic-go-image.oss-cn-beijing.aliyuncs.com/pic/CleanShot%202024-05-15%20at%2022.56.04.png)

比BIO代码复杂好几倍，但始终只有一个线程，并没有启动额外的线程来处理每个连接，解决了BIO线程无限增加的问题。但如果连接非常多的情况下，有可能一次Select拿到的SelectionKey非常多，而且取数据本身还需要把数据从内核空间拷贝到用户空间，这是一个阻塞操作，这时候都放在主线程来遍历所有的SelectionKey就会变得非常慢，可以把处理数据的部分扔到线程池中来处理。

### AIO

```java
public class AIOEchoServer {
    public static void main(String[] args) throws IOException {
        // 启动服务端
        AsynchronousServerSocketChannel serverSocketChannel =
                AsynchronousServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8003));
        System.out.println("server start");
        // 监听accept事件，完全异步，不会阻塞
        serverSocketChannel.accept(
                null,
                new CompletionHandler<AsynchronousSocketChannel, Object>() {
                    @Override
                    public void completed(
                            AsynchronousSocketChannel socketChannel, Object attachment) {
                        try {
                            System.out.println(
                                    "accept new connection：" + socketChannel.getRemoteAddress());
                            // 再次监听accept事件
                            serverSocketChannel.accept(null, this);
                            // 消息的处理
                            while (true) {
                                ByteBuffer buffer = ByteBuffer.allocate(1024);
                                // 将数据读入到buffer中
                                Future<Integer> future = socketChannel.read(buffer);
                                if (future.get() > 0) {
                                    buffer.flip();
                                    byte[] bytes = new byte[buffer.remaining()];
                                    // 将数据读入到byte数组中
                                    buffer.get(bytes);
                                    String content = new String(bytes, "UTF-8");
                                    // 换行符会当成另一条消息传过来
                                    if (content.equals("\r\n")) {
                                        continue;
                                    }
                                    System.out.println("receive msg: " + content);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        System.out.println("failed");
                    }
                });
        // 阻塞住主线程
        System.in.read();
    }
}
```

BIO中的accept()是完全阻塞当前线程的，NIO中的accept()是通过Accept事件来实现的，而AIO中的accept()是完全异步的，执行了这个方法之后会立即执行后续的代码，不会停留在accept()这一行。

accept()方法的回调方法complete()中处理数据，这里的数据已经经历过数据准备和从内核空间拷贝到用户空间两个阶段了，到达用户空间是真正可用的数据。而不像BIO和NIO那样还要自己去阻塞着把数据从内核空间拷贝到用户空间再使用。