package jvm;

import org.junit.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//测试自定义类加载器
//类加载的用处：
/*
    一个功能健全的Web服务器，要解决如下几个问题：
        1、部署在同一个服务器上的两个Web应用程序所使用的Java类库可以实现相互隔离。
        这是最基本的要求，两个不同的应用程序可能会依赖同一个第三方类库的不同版本，
        不能要求一个类库在一个服务器中只有一份，服务器应当保证两个应用程序的类库可以互相使用。
        2、部署在同一个服务器上的两个Web应用程序所使用的Java类库可以相互共享。
        这个需求也很常见，比如相同的Spring类库10个应用程序在用不可能分别存放在各个应用程序的隔离目录中。
        3、支持热替换，我们知道JSP文件最终要编译成.class文件才能由虚拟机执行，
        但JSP文件由于其纯文本存储特性，运行时修改的概率远远大于第三方类库或自身.class文件，
        而且JSP这种网页应用也把修改后无须重启作为一个很大的优势看待。
 */
public class ClassLoaderTest {


    /*
    结果：
        Thread[main,5,main]实例化由sun.misc.Launcher$AppClassLoader@18b4aac2加载的类class jvm.MyClass的对象
        我是class jvm.MyClass类的实例，我是由加载器sun.misc.Launcher$AppClassLoader@18b4aac2加载的。
        读取到数据长度为：1024
        读取到数据长度为：101
        Thread[main,5,main]实例化由jvm.MyClassLoader@22927a81加载的类class jvm.MyClass的对象
        我是class jvm.MyClass类的实例，我是由加载器jvm.MyClassLoader@22927a81加载的。
     */
    @Test
    public void testMyClassLoader() {
        //使用自定义的classloader重复加载当前类。
        MyClass myClass = new MyClass();
        myClass.dispalyInfo();
        MyClassLoader myClassLoader = new MyClassLoader(ClassLoader.getSystemClassLoader());
        try {
            Class<?> loadClass = myClassLoader.loadClass("classLoadTest");
            Constructor<?> constructor = loadClass.getConstructor();
            Object o = constructor.newInstance();
            Method method = loadClass.getMethod("dispalyInfo");
            method.invoke(o);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
