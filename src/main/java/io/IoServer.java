package io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2019/1/26.
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
                    //如何让socket一直工作？
                    System.out.println(Thread.currentThread().getName() + "创建新的线程，处理Socket");
                    byte[] bytes = new byte[1024];
                    StringBuilder sb = new StringBuilder();
                    OutputStream ops = null;
                    InputStream ips = null;
                    int num = 0;
                    try {
                        ips = socket.getInputStream();
                        ops = socket.getOutputStream();
                        while (true) {
                            //read方法时阻塞的，确保Socket不关闭的话，可以一直使用这个连接通信
//                            while ((num = ips.read(bytes)) != -1) {
                            num = ips.read(bytes);
                            if(num==-1){
                                System.out.println("Socket通信结束");
                                break;
                            }
                            sb.append(new String(bytes, 0, num));
                            System.out.println(Thread.currentThread().getName() + "开始读取数据，长度:" + num);
//                            }
                            System.out.println(Thread.currentThread().getName() + "数据读取完毕，内容:" + sb.toString());

                            ops.write((Thread.currentThread().getName() + "接受成功").getBytes());
                            ops.flush();
                            System.out.println(Thread.currentThread().getName() + "返回数据完成");
                            //清空
                            sb.delete(0,sb.length());
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
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
