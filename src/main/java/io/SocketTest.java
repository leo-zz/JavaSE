package io;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @Author: leo-zz
 * @Date: 2019/1/28 21:28
 * Socket开发的五个要点：
 * ServerSocket要循环处理Socket的接入，调用阻塞的accept()方法实现
 * 保持Socket的长连接，基于请求响应模型，循环调用阻塞的read()方法
 * 为了保证每次Socket通信能够读取完整的数据，要使用available()方法判断是否读取完毕（结合do-while方法）
 * 尽量在字节数据读取完毕之后再统一解码，避免中文乱码情况
 * 如果并发量很大或者存在连续发生数据的情景，要考虑粘包问题的处理，合理定义协议（分隔符）确保能从字节数组中分离出多个请求信息
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
            String content = Thread.currentThread().getName() + "  https://github.com/leo-zz/JavaSE";
            Socket socket = new Socket();
            System.out.println(Thread.currentThread().getName() + "连接Server");
            socket.connect(new InetSocketAddress(5140));

            OutputStream ops = socket.getOutputStream();
            InputStream ips = socket.getInputStream();
            StringBuilder sb = new StringBuilder();
            //测试一次请求数据多次读取出来，实际情景中尽量一次数据读取完毕
            byte[] recBytes = new byte[10];

            //连续输出10次，测试长连接
            for (int i = 0; i < 10; i++) {
                byte[] bytes = (content + " count:" + i).getBytes();
                ops.write(bytes);
                ops.flush();
                System.out.println(Thread.currentThread().getName() + "第" + i + "次成功写入数据内容");
                //因此要注意读取前确保流中存在数据，否则会阻塞
                int count=0;
                do{
                    count++;
                    //如果连接存在，且读不到数据时，该方法阻塞
                    int num = ips.read(recBytes);
                    if (num == -1) {
                        System.out.println("关闭Socket链接");
                        break;
                    }
                    String s = new String(recBytes, 0, num);
//                    System.out.println(count+"******************* "+s);
                    //不能把字节数组的片段转码，容易造成乱码，当然英文字符没事
                    // 比如main第9次数据读取3后完毕：pool-1-thread-2接��成功
                    sb.append(s);
                }while (ips.available()>0);
                System.out.println(Thread.currentThread().getName() + "第" + i + "次数据读取"+count+"后完毕：" + sb.toString());
                //清空StringBuilder
                sb.delete(0,sb.length());
            }
            ops.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "测试完毕");
    }
}