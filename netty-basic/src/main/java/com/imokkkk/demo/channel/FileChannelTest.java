package com.imokkkk.demo.channel;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author wyliu
 * @date 2024/5/16 13:51
 * @since 1.0
 */
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
