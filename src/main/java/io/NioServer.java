package io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Administrator on 2019/1/26.
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
            //将selector注册到channel中
            ssChannel.register(selector, SelectionKey.OP_ACCEPT);
            //拿到channel中的serverSocket对象
            ServerSocket serverSocket = ssChannel.socket();
            //监听本机IP指定端口
            serverSocket.bind(new InetSocketAddress(port));
            System.out.println("创建完毕");

            while (flag) {
                //获取selector中可以进行IO操作的keys
                int num = selector.select();
                System.out.println("接收到可进行IO操作的key的数量：" + num);

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        //不清楚为什么要在此处进行accpet()重复
                        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                        //为每个新的连接创建socketChannel
                        SocketChannel sChannel = channel.accept();
                        if (sChannel != null) {
                            sChannel.configureBlocking(false);
                            sChannel.register(selector, SelectionKey.OP_READ);
                        }
                    } else if (key.isReadable()) {
                        System.out.println(key + "可读");
                        SocketChannel channel = (SocketChannel) key.channel();

                        int read = 0;
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        StringBuilder sb = new StringBuilder();
                        //一个sokcet是长久的连接，可以建立连接后反复通信，
                        // 只要client的socket不关闭,server就不会读到-1
                        while ((read = channel.read(buffer)) != -1) {
                            System.out.println(bufferInfo(buffer));
                            buffer.flip();
                            System.out.println(bufferInfo(buffer));
                            //buffer.array() 将整个array数组返回，包括没用到的部分
                            byte[] bytes = Arrays.copyOf(buffer.array(), read);
                            sb.append(new String(bytes));
                            buffer.clear();
                        }
                        System.out.println("读取到的内容：" + sb.toString());
                        channel.close();//读取到-1,表明连接断开
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
