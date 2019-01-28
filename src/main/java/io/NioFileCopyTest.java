package io;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Author: leo-zz
 * @Date: 2019/1/28 21:28
 */
public class NioFileCopyTest {

    //1、buffer测试
    /*
        创建之初——buffer的容量：1024，buffer的已读数据的大小：0，buffer的可写数据大小：1024
        写入之初——buffer的容量：1024，buffer的已读数据的大小：32，buffer的可写数据大小：1024
        转换后——buffer的容量：1024，buffer的已读数据的大小：0，buffer的可写数据大小：32
        读取后——buffer的容量：1024，buffer的已读数据的大小：32，buffer的可写数据大小：32
        清除后——buffer的容量：1024，buffer的已读数据的大小：0，buffer的可写数据大小：1024
        结果：https://github.com/leo-zz/JavaSE
     */
    @Test
    public void bufferTest(){
        String str="https://github.com/leo-zz/JavaSE";
        ByteBuffer buffer=ByteBuffer.allocate(1024);
        System.out.println("创建之初——"+bufferInfo(buffer));

        ByteBuffer put = buffer.put(str.getBytes());
        System.out.println("写入之初——"+bufferInfo(buffer));

        buffer.flip();
        System.out.println("转换后——"+bufferInfo(buffer));

        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        System.out.println("读取后——"+bufferInfo(buffer));
        buffer.clear();
        System.out.println("清除后——"+bufferInfo(buffer));
        System.out.println("结果："+new String(bytes));
    }

    //2、  NIO以块为单位进行处理，普通IO以字节为单位处理。
    /*  结果：
        拷贝文件名称：jdk-8u131-windows-x64.exe，拷贝大小：207649848
        已累计拷贝202783K字节。
        文件复制完毕。
        普通IO总耗时：1751

        拷贝文件名称：jdk-8u131-windows-x64.exe，拷贝大小：207649848
        已累计拷贝202783K字节。
        文件复制完毕。
        NIO总耗时：807
     */
    @Test
    public void testFileCopy() {

//        long start = System.currentTimeMillis();
//        useIOCopyFile("F:\\Java\\jdk-8u131-windows-x64.exe", "F:\\Java\\testCopy");
//        long end = System.currentTimeMillis();
//        System.out.println("NIO总耗时：" + (end - start));

        long start = System.currentTimeMillis();
        useNIOCopyFile("F:\\Java\\jdk-8u131-windows-x64.exe", "F:\\Java\\testCopy");
        long end = System.currentTimeMillis();
        System.out.println("NIO总耗时：" + (end - start));
    }

    //使用普通io实现文件拷贝
    public void useIOCopyFile(String src, String desc) {
        File srcFile = new File(src);
        File descFile = new File(desc);
        if (!srcFile.isFile()) throw new RuntimeException(src + "不是一个文件！");
        System.out.println("拷贝文件名称：" + srcFile.getName() + "，拷贝大小：" + srcFile.length());
        if (!descFile.isDirectory()) throw new RuntimeException(desc + "不是一个路径！");
        byte[] bytes = new byte[10 * 1024];

        try {
            FileInputStream fis = new FileInputStream(srcFile);
            FileOutputStream fos = new FileOutputStream(desc + "/" + srcFile.getName());

            int read = 0;
            int totalCount = 0;
            while ((read = fis.read(bytes)) != -1) {
                fos.write(bytes, 0, read);
                totalCount += read;
                System.out.println("已累计拷贝" + (totalCount / 1024) + "K字节。");
            }
            fos.flush();
            fos.close();
            System.out.println("文件复制完毕。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //使用nio实现文件拷贝
    public void useNIOCopyFile(String src, String desc) {
        File srcFile = new File(src);
         File descFile = new File(desc);
        if (!srcFile.isFile()) throw new RuntimeException(src + "不是一个文件！");
        System.out.println("拷贝文件名称：" + srcFile.getName() + "，拷贝大小：" + srcFile.length());
        if (!descFile.isDirectory()) throw new RuntimeException(desc + "不是一个路径！");
        //buffer的创建方式
        ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);

        try {
            FileInputStream fis = new FileInputStream(srcFile);
            FileOutputStream fos = new FileOutputStream(desc + "/" + srcFile.getName());
            FileChannel fisChannel = fis.getChannel();
            FileChannel fosChannel = fos.getChannel();
            int totalCount=0;
            while(fisChannel.read(buffer) != -1) {
                //buffer读取数据后写入数据前，需要使用flip()转变下buffer的状态，令limit等于positon，并将position清零
                buffer.flip();
                int write = fosChannel.write(buffer);
                //buffer写入数据后，需要调用clear()方法清空缓冲区，令limit等于capacity，将position清零，
                buffer.clear();
                totalCount += write;
                System.out.println("已累计拷贝" + (totalCount / 1024) + "K字节。");
            }
            fosChannel.close();
            System.out.println("文件复制完毕。");
        } catch (Exception e) {
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
