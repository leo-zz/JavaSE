package io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: leo-zz
 * @Date: 2019/1/28 21:28
 */
public class IoServer {

    public void startServer(int port) {
        ExecutorService executorService = Executors.newCachedThreadPool();

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println(Thread.currentThread().getName() + "普通SocketServer开启，端口号：" + port);

            //循环处理不同的客户端链接
            while (true) {
                //accept方法时阻塞的
                Socket socket = serverSocket.accept();

                executorService.execute(() -> {
                    handleSocket(socket);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSocket(Socket socket) {

        // 问：如何让socket一直工作？
        // 答：重复执行read方法，该方法阻塞，重复读取数据前先判断流中是否有可读数据
        System.out.println(Thread.currentThread().getName() + "创建新的线程，处理Socket");
        byte[] bytes = new byte[10];
        StringBuilder sb = new StringBuilder();
        OutputStream ops = null;
        InputStream ips = null;
        int num = 0;
        try {
            ips = socket.getInputStream();
            ops = socket.getOutputStream();
            while (true) {
                int readCount=0;
                //read方法时阻塞的，确保Socket不关闭的话，可以一直使用这个连接通信
                //使用do-while语句确保数据读完
                do {
                    readCount++;
                    num = ips.read(bytes);
                    if (num == -1) {
                        System.out.println("Socket通信结束");
                        break;
                    }
                    sb.append(new String(bytes, 0, num));
                } while (ips.available() > 0);
                System.out.println(Thread.currentThread().getName() + "数据读取"+readCount+"次完毕，内容:" + sb.toString());

                ops.write((Thread.currentThread().getName() + "接受成功").getBytes());
                ops.flush();
                System.out.println(Thread.currentThread().getName() + "返回数据完成");
                //清空StringBuilder
                sb.delete(0, sb.length());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ops != null) ops.close();
                System.out.println("关闭链接");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
