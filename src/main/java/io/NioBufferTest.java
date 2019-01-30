package io;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Author: leo-zz
 * @Date: 2019/1/30 7:13
 * 测试Buffer的put和read操作，观察三个属性position、limit、capacity的变化
 */
public class NioBufferTest {

    /*
    flip操作将limit等于position，将position置为0；
    clear操作将position置为0，limit置为capaticy；clear并未清空buffer中的数据；
    只要position<limit就可以进行写入操作；
        输出：
        创建buffer:   capacity容量：20，position已读/写大小：0，limit可读/写大小：20
        写入数据到buffer:    capacity容量：20，position已读/写大小：12，limit可读/写大小：20
        进行flip操作:    capacity容量：20，position已读/写大小：0，limit可读/写大小：12
        进行读取操作，读到字节个数12:    capacity容量：20，position已读/写大小：12，limit可读/写大小：12
        进行读取操作，读到字节个数0:    capacity容量：20，position已读/写大小：12，limit可读/写大小：12
        进行清除操作:    capacity容量：20，position已读/写大小：0，limit可读/写大小：20
        进行读取操作，读到字节个数0:    capacity容量：20，position已读/写大小：20，limit可读/写大小：20
        文件内容：
        "测试数据测试数据        " 带有8个空格
     */
    @Test
    public void testBuffer() {
        File file = new File("E:\\testbuffer.txt");
        FileOutputStream fops = null;
        FileChannel channel = null;
        ByteBuffer buffer = ByteBuffer.allocate(20);
        try {
            fops = new FileOutputStream(file);
            channel = fops.getChannel();
            byte[] bytes = "测试数据".getBytes();
            System.out.println("创建buffer:   "+bufferInfo(buffer));
            buffer.put(bytes);
            System.out.println("写入数据到buffer:    "+bufferInfo(buffer));
            //flip操作将limit等于position，将position置为0；
            buffer.flip();
            System.out.println("进行flip操作:    "+bufferInfo(buffer));
            //写入操作要求position<limit
            int writeNum1 = channel.write(buffer);
            System.out.println("进行读取操作，读到字节个数"+writeNum1+":    "+bufferInfo(buffer));
            int writeNum2 = channel.write(buffer);
            System.out.println("进行读取操作，读到字节个数"+writeNum2+":    "+bufferInfo(buffer));
            //clear操作将position置为0，limit置为capaticy
            buffer.clear();
            System.out.println("进行清除操作:    "+bufferInfo(buffer));
            //根据position的长度和clear的特性，还可写入20个字节的数据
            int writeNum3 = channel.write(buffer);
            System.out.println("进行读取操作，读到字节个数"+writeNum2+":    "+bufferInfo(buffer));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (channel != null) {
                    channel.close();
                }
                if (fops != null){
                    fops.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String bufferInfo(ByteBuffer bb) {
        StringBuilder sb = new StringBuilder();
        sb.append("capacity容量：" + bb.capacity())
                .append("，position已读/写大小：" + bb.position())
                .append("，limit可读/写大小：" + bb.limit());
        return sb.toString();
    }

}
