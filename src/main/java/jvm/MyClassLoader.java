package jvm;

import util.DynamicByteArray;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Author: leo-zz
 * @Date: 2019/2/11 12:03
 */
public class MyClassLoader extends ClassLoader {
    //父类的构造方法不会自动继承
    public MyClassLoader(ClassLoader parent) {
        super(parent);
    }

    //1、如果不想打破双亲委派模型，那么只需要重写findClass方法即可
    //2、如果想打破双亲委派模型，那么就重写整个loadClass方法
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            if ("classLoadTest".equals(name)) {
//                    System.out.println("自定义classloader，重写LoadClass()，打破了双亲委派模型。");
                int length = 0;
                //使用nio读取字节信息，必须是编译后的字节文件
                File classFile = new File("D:\\MyClass.class");
//                    File classFile = new File("D:\\ClassLoaderTest.java");
                FileInputStream fis = new FileInputStream(classFile);
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                FileChannel channel = fis.getChannel();
                DynamicByteArray dynamicByteArray = new DynamicByteArray();
                int read = 0;
                do {
                    read = channel.read(byteBuffer);
                    if (read > 0) {
                        System.out.println("读取到数据长度为：" + read);
                        length += read;
                        dynamicByteArray.add(byteBuffer.array(), 0, read);
                        byteBuffer.clear();
                    }
                } while (read != -1);
                return defineClass(dynamicByteArray.getBytes(), 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
