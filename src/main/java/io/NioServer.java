package io;

import util.DynamicByteArray;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author: leo-zz
 * @Date: 2019/1/28 21:28
 * NIO在一个线程中实现了Socket连接的创建和通信数据的处理，将所有的阻塞操作，通过一个select()替代。
 * Server端使用一个selector管理了socket建立连接的accept()和socket通信的read()方法。
 */
public class NioServer {

    public static void main(String[] args) {

    }

    public void startServer(int port) {
        boolean flag = true;
        try {
            //创建Selector
            Selector selector = Selector.open();
            //创建server端的channel，需要绑定端口后才能accept请求
            ServerSocketChannel ssChannel = ServerSocketChannel.open();
            //设置通道为非阻塞模式，相比传统IO的优点
            ssChannel.configureBlocking(false);
            //将selector注册到channel中，serversocket只监听socket的连接
            ssChannel.register(selector, SelectionKey.OP_ACCEPT);
            //拿到channel中的serverSocket对象
            ServerSocket serverSocket = ssChannel.socket();
            //监听本机IP指定端口
            serverSocket.bind(new InetSocketAddress(port));
            System.out.println("创建完毕");

            while (flag) {
                //获取selector中可以进行IO操作的keys
                //selector绑定的事件有：
                // SelectionKey.OP_ACCEPT   ：Socket连接建立
                // SelectionKey.OP_READ     ：Socket通信
                int num = selector.select();
                System.out.println("接收到可进行IO操作的key的数量：" + num);

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    //建立新的socket连接
                    if (key.isAcceptable()) {
                        //只有ServerSocketChannel才会处理这种请求
                        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                        //接收到新的socket连接，获取socketChannel并注册到selector中
                        SocketChannel sChannel = channel.accept();
                        if (sChannel != null) {
                            sChannel.configureBlocking(false);
                            sChannel.register(selector, SelectionKey.OP_READ);
                        }
                        //处理Scoket通信
                    } else if (key.isReadable()) {
                        System.out.println(key + "可读");
                        SocketChannel channel = (SocketChannel) key.channel();

                        int read = 0;
                        //确保能存储一次读/写的数据
                        ByteBuffer buffer = ByteBuffer.allocate(10);
                        DynamicByteArray byteArray = new DynamicByteArray();

                        //一个sokcet是长久的连接，但是NIO的机制不同，不能拿到socket阻塞反复通信


                        while ((read = channel.read(buffer)) > 0) {
                            System.out.println(bufferInfo(buffer));
                            buffer.flip();
                            System.out.println(bufferInfo(buffer));
                            //buffer.array() 将整个array数组返回，包括没用到的部分
                            //字节解码时要注意不要截取片段，否则中文可能会出现乱码。
                            byteArray.add(buffer.array(), 0, read);
                            buffer.clear();
                        }
                        if (read == -1) {
                            channel.close();//读取到-1,表明连接断开
                            System.out.println("关闭连接");
                            break;//关闭连接后退出此循环
                        }
                        //接收到的数据存在多余的后缀 “SE c” 4个字节长度
                        //main  https://github.com/leo-zz/JavaSE count:0SE c
                        System.out.println("读取到的内容：" + new String(byteArray.getBytes()));

                        //如果传入的数据长度超过put，则会报错：java.nio.BufferOverflowException
                        byte[] resBytes = (Thread.currentThread().getName() + "接受成功").getBytes();
//                        if(buffer.capacity()<resBytes.length){
                        buffer = ByteBuffer.wrap(resBytes);
//                        };
                        //多余的flip会造成数据无法写入
//                        buffer.flip();
                        //问题：写入数据长度为0，因为多了一次flip()操作
                        //读取操作要求limit可写入长度不能为0
                        int writeNum=channel.write(buffer);
                        //不会清除旧数据，注意操作时只读取需要的长度，否则会出错。
                        buffer.clear();
                        byteArray.clear();
                        System.out.println("写入数据成功");
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String bufferInfo(ByteBuffer bb) {
        StringBuilder sb = new StringBuilder();
        sb.append("buffer的容量：" + bb.capacity())
                .append("，buffer的已读数据的大小：" + bb.position())
                .append("，buffer的可写数据大小：" + bb.limit());
        return sb.toString();
    }

}
