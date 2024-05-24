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

## Java NIO核心组件

### Channel

linux系统中，一切皆是文件，Channel就是到文件的连接，并可以通过IO操作这些文件。因此，针对不同的文件类型又衍生出不同类型的Channel。

- FileChannel：操作普通文件
- DatagramChannel：用于UDP协议
- SocketChannel：用于TCP协议，客户端与服务端之间的Channel
- ServerSocketChannel：用于TCP协议，仅用于服务端的Channel

> ServerSocketChannel和SocketChannel是专门用于TCP协议中的。
>
> ServerSocketChannel是一种服务端的Channel，只能用在服务端，可以看作是到网卡一种Channel，它监听着网卡的某个端口。
>
> SocketChannel是一种客户端与服务端之间的Channel，客户端连接到服务器的网卡之后，被服务端的Channel监听到，然后与客户端之间建立一个Channel，这个Channel就是SocketChannel。

**FileChannel**

```java
public class FileChannelTest {
    public static void main(String[] args) throws IOException {
        //从文件获取一个FileChannel
        FileChannel fileChannel = new RandomAccessFile(
          "/Users/wyliu/probject/idea/netty-demo/pom.xml", "rw").getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        //将FileChanel中的数据读出到buffer中，-1表示读取完毕
        //buffer默认为写模式
        //read()方法是相对channel而言，相对于buffer就是写
        while (fileChannel.read(buffer) != -1){
            //buffer切换为读模式
            buffer.flip();
            //buffer中是否有未读取数据
            while (buffer.hasRemaining()){
                //未读数据长度
                int remain = buffer.remaining();
                byte[] bytes = new byte[remain];
                buffer.get(bytes);
                // 打印出来
                System.out.println(new String(bytes, StandardCharsets.UTF_8));
            }
            //清空buffer，为下一次写入数据做准备
            //clear()会将buffer再次切换为写模式
            buffer.clear();
        }
    }
}
```

### Buffer

存放特定基本类型数据的容器。**特点**：线性，有限的序列，元素是基本类型的数据。**属性**：capacity、limit、position。

**NIO的传输方式与BIO的传输方式的区别**

BIO是面向流的，NIO是面向Channel或者缓冲区的，效率更高。

- 流是单向的，所以分为InputStream和OutputStream，而Channel是双向的，即可读也可写；
- 流只支持同步读写，而Channel是可以支持异步读写的；
- 流一般与字节数组或者字符数组配合使用，而Channel一般与Buffer配合使用。

#### 类型

**基本数据类型**：byte、char、boolean、int、double、float、short、long

**Buffer的类型**：ByteBuffer、CharBuffer、IntBuffer、DoubleBuffer、FloatBuffer、ShortBuffer、LongBuffer

堆内存实现个直接内存实现，如HeapByteBuffer和DirectByteBuffer

#### 基本属性

- **capacity**：Buffer的容量，即能够容纳多少数据。
- **limit**：最大可写或最大可读的数据。
- **position**：下一次可使用的位置，针对读模式标识下一个可读的位置；针对写模式表示下一个可写的位置。

![image-20240516143516906](https://pic-go-image.oss-cn-beijing.aliyuncs.com/pic/image-20240516143516906.png)

写模式下：capacity=8，limit=8，position=3；读模式下：capacity=8，limit=3，position=0

> position表示的是位置，类似数组的下标，从0开始。而limit和capacity表示大小，类似数组的长度，从1开始。当buffer从写模式切换为读模式，limit变为原position的值，position变为0。

#### API

- 分配一个Buffer：allocate()
- 写入数据：buf.put()或者channel.read(buf)，read为read to的意思，从channel读出并写入buffer
- 切换为读模式：buf.flip()
- 读取数据：buf.read()或者channel.write(buf)，write为write from的意思，从buffer读出并写入channel
- 重新读取或重新写入：rewind()，重置position为0，limit和capacity保持不变，可以重新读取或写入数据
- 清空数据：buf.clear()，清空所有数据
- 压缩数据：buf.compact()，清除已读取的数据，并将未读取的数据往前移

### Selector

SelectableChannel对象的多路复用器，一个Selector可以关联到多个SelectableChannel。

SelectableChannel：和网络编程相关的Channel，如SocketChannel、ServerSocketChannel、DatagramChannel等。

```java
// 创建一个Selector
Selector selector = Selector.open();
// 注册事件到Selector上
channel.configureBlocking(false);
SelectionKey key = channel.register(selector, SelectionKey.OP_READ);

// select()只有询问的意思，加上循环才是轮询的意思 
while(true) {
selector.select(); // 一直阻塞直到有感兴趣的事件 // selector.selectNow(); // 立即返回，不阻塞
// selector.select(timeout); // 阻塞一段时间返回
// ...
  
Set<SelectionKey> selectedKeys = selector.selectedKeys(); 
Iterator<SelectionKey> keyIterator = selectedKeys.iterator(); 
while(keyIterator.hasNext()) {
	SelectionKey key = keyIterator.next(); 
  if(key.isAcceptable()) {
	// 接受连接事件已就绪
	} else if (key.isConnectable()) {
	// 连接事件已就绪
	} else if (key.isReadable()) {
	// 读事件已就绪
	} else if (key.isWritable()) {
	// 写事件已就绪 }
	keyIterator.remove(); 
	}
}
```

#### 事件

Channel感兴趣的事情。

- 读事件:SelectionKey.OP_READ = 1 << 0 = 0000 0001
- 写事件:SelectionKey.OP_WRITE = 1 << 2 = 0000 0100 
- 连接事件:SelectionKey.OP_CONNECT = 1 << 3 = 0000 1000 
- 接受连接事件:SelectionKey.OP_ACCEPT = 1 << 4 = 0001 0000

使用"位或"操作监听多种感兴趣的事件：

```java
int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
```

## Netty整体架构

![image-20240517163300703](https://pic-go-image.oss-cn-beijing.aliyuncs.com/pic/image-20240517163300703.png)

- **Core**：核心层，主要自定义一些基础设施，如事件模型、通信API、缓冲区等。
- **Transport Service**：传输服务层，主要定义一些通信的底层能力，或者说是传输协议的支持，比如TCP、UDP、HTTP隧道、虚拟机管道等。
- **Protocol Supprot**：协议支持层，不仅指编码协议，还可以是应用层协议的编解码，如HTTP、WebSocket、SSL、Protobuf、文本协议、二进制协议、压缩协议、大文件传输等，基本上主流的协议都支持。

Netty的核心在于其**可扩展的事件模型**、**通用的通信API**、**基于零拷贝的缓冲区**等。

### 模块设计

![CleanShot 2024-05-19 at 21.15.44](https://pic-go-image.oss-cn-beijing.aliyuncs.com/pic/CleanShot%202024-05-19%20at%2021.15.44.png)

#### netty-common

主要定义了一些工具类，大概如下：

- 通用的工具类，如`StringUtil`等
- 对于JDK原生类的增强，如`Future`、`FastThreadLocal`等
- Netty定义的并发包，如`EventExecutor`等
- Netty自定义的集合包，主要是对HashMap的增强

其它所有模块都依赖于common包。

#### netty-buffer

Netty自己实现的Buffer，做了很多优化，如池化Buffer、组合Buffer等。

#### netty-resolver

主要做地址解析用的。

#### netty-transport

主要定义了一些服务于传输层的接口和类，比如Channel、ChannelHandler、ChannelHandlerContext、EventLoop等。还实现了对于TCP、UDP通信协议的支持，另外三个包`netty-transport`、`netty-transport-rxtx`、`netty-transport-udt`也是对不同协议的支持。

> TCP：传输控制协议，Java中一般用SocketXxx、ServerSocketXxx表示基于TCP协议通信。
>
> UDP：用户数据报文协议，Java中一般用DatagramXxx表示基于UDP协议通信，Datagram，数据报文的意思。
>
> SCTP：流控制传输协议。
>
> RXTX：串口通信协议。
>
> UDT：基于UDP的数据传输协议。

#### netty-handler

定义了各种不同的Handler，满足不同的业务需求，比如，IP过滤、日志、SSL、空闲检测、流量整形等。

#### netty-codec

定义了一系列编码解码器，比如，base64、json、marshalling、protobuf、serializaion、string、xml等，几乎市面上能想到的编码、解码、序列化、反序列化方式，Netty中都支持。它们是一类特殊的ChannelHandler，专门负责编解码的工作。Netty还实现了很多主流的编解码器，如http、http2、mqtt、redis、stomp等等，也可以基于ChannelHandler接口自定义编解码器来解决。

> `netty-codec`与`netty-handler`是两个平齐的模块，并不互相依赖，没有包含和被包含关系，ChannelHandler接口位于`netty-transport`模块中，两者都依赖于`netty-transport`模块。

#### netty-example

包含了各种各样的案例。

## Netty编程十步曲

### 1.声明线程池(必须)

```java
EventLoopGroup bossGroup = new NioEventLoopGroup(1); 
EventLoopGroup workerGroup = new NioEventLoopGroup();
```

**bossGroup**：处理Accept事件

**workerGroup**：处理消息的读写事件

### 2.创建服务端引导类(必须)

```java
ServerBootstrap serverBootstrap = new ServerBootstrap();
```

引导类：集成所有配置，引导程序加载。

- 客户端引导类：Bootstrap
- 服务端引导类：ServerBootstrap

### 3.绑定线程池(必须)

```java
serverBootstrap.group(bossGroup, workerGroup);
```

把声明的线程池绑定到ServerBootstrap

### 4.设置ServerSocketChannel类型(必须)

```java
serverBootstrap.channel(NioServerSocketChannel.class);
```

- NioServerSocketChannel
- OioServerSocketChannel
- EpollServerSocketChannel

### 5.设置参数(可选)

```java
serverBootstrap.option(ChannelOption.SO_BACKLOG, 100);
```

一般不需要修改Netty的默认参数

### 6.设置Handler(可选)

```java
serverBootstrap.handler(new LoggingHandler(LogLevel.INFO))
```

只能设置一个

### 7.编写并设置子Handler(必须)

```java
serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() { 
	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		// 可以添加多个子Handler
		p.addLast(new LoggingHandler(LogLevel.INFO)); 
    p.addLast(new EchoServerHandler());
	} 
});
```

Netty中的Handler分为两种：

- Inbound

  ```java
  public class EchoServerHandler extends ChannelInboundHandlerAdapter {
  	@Override
  	public void channelRead(ChannelHandlerContext ctx, Object msg) {
  	// 读取数据后写回客户端 ctx.write(msg);
  	}
  	@Override
  	public void channelReadComplete(ChannelHandlerContext ctx) {
  		ctx.flush(); 
  	}
  	@Override
  	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
  		cause.printStackTrace(); 
  		ctx.close();
  	} 
  }
  ```

- Outbound

### 8.绑定端口(必须)

```java
ChannelFuture f = serverBootstrap.bind(PORT).sync();
```

绑定端口，并启动服务端程序，sync()会阻塞直到启动完成才执行后面的代码。

### 9.等待服务端端口关闭(必须)

```java
f.channel().closeFuture().sync();
```

等待服务端监听端口关闭，sync()会阻塞线程，内部调用的是Object的wait()。

### 10.优雅关闭线程池

```java
bossGroup.shutdownGracefully(); 
workerGroup.shutdownGracefully();
```

在finally中调用shutdownGracefully()。

**为什么需要设置ServerSocketChannel的类型，而不需要设置SocketChannel的类型？**

SocketChannel是ServerSocketChannel在接受连接之后创建出来的，所以，并不需要单独设置它的类型。

## Netty核心组件

### Bootstrap与ServerBootstrap

Netty程序的引导类，主要用于配置各种参数，并启动整个Netty服务。

**Bootstrap**用于客户端引导

**ServerBootstrap**用于服务端引导

### EventLoopGroup

可以理解为一个**线程池**，对于服务端程序，一般绑定两个线程池，一个用于**处理Accept事件**，一个用于**处理读写事件**。

![image-20240524140043574](https://pic-go-image.oss-cn-beijing.aliyuncs.com/pic/image-20240524140043574.png)

- Iterable：迭代器的接口，EventLoopGroup是一个容器，可以通过迭代的方式查看里面的元素。
- Executor：线程池的顶级接口，提供了execute()方法，用于提交任务到线程池。
- ExecutorService：扩展自Executor接口，提供了通过submit()方法提交任务的方式，并增加了shutdown()等其它方法。
- ScheduleExecutorService：扩展自ExecutorService，增加了定时任务执行相关的方法。

Netty

- EventExecutorGroup：扩展自ScheduleExecutorService，并增加了两个功能，一是提供了next()方法获取一个EventExecutor，二是管理这些EventExecuor的生命周期。

- EventLoopGroup：扩展自EventExecutorGroup，并增加或修改了两大功能，一是提供了netx()方法用于获取一个EventLoop，二是提供了注册Channel到事件轮询器中。

- MultithreadEventLoopgroup：抽象类，EventLoopGroup的所有实现类都继承自这个类，可以看作是一种模版。

- **NioEventLoopGroup**：具体实现类，使用NIO形式(多路复用中的select)工作的EventLoopGroup。

  - **EpollEventLoopGroup**：用于Linux平台
  - KQueueEventLoopGroup：用于MacOS/BSD平台

  > select/epoll/kqueue，是实现IO多路复用的不同形式，select支持的平台比较广泛，epoll和kqueue比select更高效，epoll只支持linux，kqueue只支持MacOS/BSD平台。

### EventLoop

EventLoop可以理解为EventLoopGroup中的工作线程，类似于ThreadPoolExecutor中的Worker，但它并不是一个线程，它里面包含了一个线程，控制着这个线程的生命周期。

- EventEcecutor：扩展自EventLoopGroup，主要增加了判断一个线程是不是在EventLoop中的方法。

- OrderedEventExecutor：扩展自EventExecutor，这是一个标记接口，标志着里面的任务都是按顺序执行的。

- EventLoop：扩展至EventLoopGroup，它将为已注册进来的Channel处理所有的IO事件，另外，它还扩展自OrderedEventExecutor接口，说明里面的任务是按顺序执行的。

- SingleThreadEventLoop：抽象类，EventLoop的所有的实现类都继承自这个类，可以看作是一种模版，单线程处理。

- **NioEventLoop**：具体实现类，绑定到一个Selector上，同时可以注册多个Channel到Selector上，同时，它继承自SingleThreadEventLoop，说明一个Selector对应一个线程。

  - **EpollEventLoop**
  - KQueueEventLoop

  ![image-20240524155252235](https://pic-go-image.oss-cn-beijing.aliyuncs.com/pic/image-20240524155252235.png)

### ByteBuf

声明了两个指针：读指针readIndex：读取数据，写指针writeIndex：写数据

![image-20240524155627387](https://pic-go-image.oss-cn-beijing.aliyuncs.com/pic/image-20240524155627387.png)

读写指针分离：解决了读写模式切换、position变来变去的问题。

#### 池化和非池化

- **池化**：初始化分配好一块内存作为内存池，每次创建ByteBuf时从这个内存池中分配一块连续的内存给这个ByteBuf使用，使用完毕后再访问内存池。
- 非池化：对象的内存分配完全交给JVM来管理。

#### Heap和Direct

堆内存和直接内存。

- 堆内存：JVM的堆内存；
- **直接内存**：独立于JVM之外的内存空间，直接向操作系统申请一块内存。

#### Safe和Unsafe

安全和非安全。

Unsafe：底层使用Java本身的Unsafe来操作底层的数据结构，即**直接利用对象在内存中的指针来操作对象**，所以，比较危险。

- PooledByteBufAllocator：使用池化技术，内部根据平台特性自行决定使用哪种ByteBuf
- UnpooledByteBufAllocator：不使用池化技术，内部会根据平台特性自行决定使用哪种 ByteBuf
- PreferHeapByteBufAllocator：更偏向于使用堆内存，即除了显式地指明了使用直接内存的方法都使用堆内存 
- PreferDirectByteBufAllocator：更偏向于使用直接内存，即除了显式地指明了使用堆内存的方法都使用直接内存

Netty创建最适合当前平台的ByteBuf：**最大努力的使用池化、Unsafe、直接内存的方式创建ByteBuf**

```java
ByteBufAllocator allocator = ByteBufAllocator.DEFAULT; 
ByteBuf buffer = allocator.buffer(length); 
buffer.writeXxx(xxx);
```

### Channel

对Java原生Channel的**进一步封装**，比如：

- 获取当前连接的状态及配置参数
- 通过ChannelPipeline来处理IO事件
- 在Netty中的所有IO操作都是异步的
- 可继承的Channel体系

协议**Channel的包装类**，和对**协议的扩展**。

### ChannelHandler

核心业务处理接口，用于处理或拦截IO事件，并将其转发到ChannelPipeline中的下一个ChannelHandler，运用的是**责任链**设计模式。

ChannelHandler分为入站和出站两种：ChannelInboundHandler和ChannelOutboundHandler，建议实现它们的抽象类：

- **SimpleChannelInboundHandler**：处理入站事件，不建议直接使用ChannelInboundHandlerAdapter
- **ChannelOutboundHandlerAdapter**：处理出站事件
- **ChannelDuplexHandler**：双向的

SimpleChannelInboundHandler相比于ChannelInboundHandlerAdapter可以做**资源的自动释放**。

### ChannelFuture

异步用来获取返回值的对象。通过ChannelFuture，可以查看IO操作是否已完成、是否已成功、是否已取消等。

### ChannelPipeline

ChannelPipeline是ChannelHandler的集合，负责处理和拦截入站和出站的事件和操作，每个Channel都有一个ChannelPipeline与之对应，会自动创建。

更确切的说，ChannelPipeline中存储的是ChannelHandlerContext链，通过这个链**把ChannelHandler连接起来**。

- 一个Channel对应一个ChannelPipeline
- 一个ChannelPipeline包含一条双向的ChannelHandlerContext链
- 一个ChannelHandlerContext中包含一个ChannelHandler
- 一个Channel会绑定一个EventLoop上
- 一个NioEventLoop维护了一个Selector
- 一个NioEventLoop相当于一个线程

因此，**ChannelPipeline、ChannelHandlerContext都是线程安全的，因为同一个Channel的事件都会在一个线程中处理完成**。但ChannelHandler不一定，ChannelHandler类似于Spring MVC中的Service层，专门处理业务逻辑的地方，一个ChannelHandler实例可以供多个Channel使用，**不建议把有状态的变量放在ChannelHandler中，而是放在消息本身或ChannelHandlerContext中**。

![image-20240524171041049](https://pic-go-image.oss-cn-beijing.aliyuncs.com/pic/image-20240524171041049.png)

### ChannelOption

保存了拿来即用的参数，如ChannelOption.SO_BACKLOG。

























