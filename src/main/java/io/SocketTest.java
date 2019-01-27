package io;

import org.junit.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2019/1/26.
 */
public class SocketTest {

    @Test
    public void startNIOServer() {
        NioServer nioServer = new NioServer();
        System.out.println(Thread.currentThread().getName() + "开启NioSocketServer");
        nioServer.startServer(5140);//该方法会阻塞执行
    }

    @Test
    public void startIOServer() {
        IoServer ioServer = new IoServer();
        System.out.println(Thread.currentThread().getName() + "开启IoSocketServer");
        ioServer.startServer(5140);//该方法是阻塞的
    }


    //模拟客户端
    @Test
    public void socketTest() throws InterruptedException {
        try {
            String content = Thread.currentThread().getName() + "https://github.com/leo-zz/JavaSE";
            Socket socket = new Socket();
            System.out.println(Thread.currentThread().getName() + "连接Server");
            socket.connect(new InetSocketAddress(5140));

            //普通io方式写入
            OutputStream ops = socket.getOutputStream();
            InputStream ips = socket.getInputStream();
            StringBuilder sb = new StringBuilder();
            byte[] recBytes = new byte[10];

//            BufferedInputStream bufferedInputStream = new BufferedInputStream(ips);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ips));
            String str;

            outfor:
            for (int i = 0; i < 10; i++) {
                byte[] bytes = (content + " count:" + i).getBytes();
                ops.write(bytes);
                ops.flush();
                System.out.println(Thread.currentThread().getName() + "第" + i + "次成功写入数据内容");
                int num = 0;
                //如何实现一次能够读完所有的内容？防止重复执行read()造成阻塞
//                while ((num = ips.read(recBytes)) != 0) {
//                while ((num = bufferedInputStream.read(recBytes)) != -1) {
                //TODO 这里阻塞不会往下执行。
                //如何能确保数据读完整，重复执行read又不会导致阻塞？
                while ((str = bufferedReader.readLine()) != null) {
//                    if(num==-1){
//                        System.out.println("关闭Socket链接");
//                        break outfor;
//                    }
                    sb.append(str);
//                sb.append(new String(bytes, 0, num));
                System.out.println(Thread.currentThread().getName() + "开始第" + i + "次读取的数据，长度：" + str);
                }
                System.out.println(Thread.currentThread().getName() + "第" + i + "次数据读取完毕：" + sb.toString());
                //清空StringBuilder
                sb.delete(0, sb.length());
                Thread.sleep(1000);
            }

            ops.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "测试完毕");
    }
}
