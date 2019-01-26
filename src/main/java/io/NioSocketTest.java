package io;

import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2019/1/26.
 */
public class NioSocketTest {

    @Test
    public void nioSocketTest() throws InterruptedException {
        ExecutorService pool = Executors.newCachedThreadPool();
        Object o1 = new Object();
        Object o2 = new Object();

        pool.execute(() -> {
            NioServer nioServer = new NioServer();
            System.out.println(Thread.currentThread().getName() + "开启NioSocketServer");
            nioServer.startServer(5140);//该方法是阻塞的
        });

        pool.execute(() -> {
            try {
//                Thread.sleep(2000);//等待server启动
                String content = "https://github.com/leo-zz/JavaSE";
                Socket socket = new Socket();

                socket.connect(new InetSocketAddress(5140));

                //nio方式
//                SocketChannel sChannel = socket.getChannel();
//                sChannel.write(ByteBuffer.wrap(content.getBytes()));//wrap方式写入数据
//                System.out.println(Thread.currentThread().getName() + "使用nio写入数据内容");
//                sChannel.close();

                //普通io方式写入
                OutputStream ops = socket.getOutputStream();
                byte[] bytes = content.getBytes();
                ops.write(bytes);
                ops.flush();
                System.out.println(Thread.currentThread().getName() + "使用普通io写入数据内容"+bytes);
                ops.close();
                synchronized (o1) {
                    System.out.println("写入完成，通知主线程。");
                    o1.notify();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        synchronized (o1) {
            System.out.println(Thread.currentThread().getName() + "等待测试完毕");
            o1.wait();
            System.out.println(Thread.currentThread().getName() + "结束");
        }
    }
}
