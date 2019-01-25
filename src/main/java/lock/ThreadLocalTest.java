package lock;

import org.junit.Test;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class ThreadLocalTest {
    ThreadLocal<Integer> age=new ThreadLocal<>();
    ThreadLocal<String> name=new ThreadLocal<>();


    /*
        ThreadLocal的原理:
        涉及类：Thread、ThreadLocalMap、ThreadLocal
        涉及区域：栈、堆、方法区
        关系：
        1、ThreadLocalMap是ThreadLocal的静态内部类，是一个Map，包含有size、capacity、loadFactor、threshould。
        2、ThreadLocal封装了对于ThreadLocalMap的增删改查操作。
        3、Thread类存在ThreadLocalMap的属性，相当于每个线程有个私有的Map，
        并且Map的Key是threadLocal对象的弱引用，并且key是被多线程共享的。
        类比：
        每个线程都相当于一个人，每个人都有一些通用的属性：比如名字、性别等，每个人的都有自己的名字和性别，且之间互补影响。
     */
    @Test
    public void threadLocalUseTest() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(2);
        Thread t1 = new Thread(() -> {
            try {
                age.set(18);
                name.set("lee");
                System.out.println(Thread.currentThread().getName() + "数据写入完成，等待输出");
                Thread.sleep(1000);
                System.out.println(age.get() + "  " + name.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
        }, "t1");

        Thread t2 = new Thread(() -> {
            try {
                age.set(1);
                name.set("leo");
                System.out.println(Thread.currentThread().getName() + "数据写入完成，等待输出");
                Thread.sleep(1000);
                System.out.println(age.get() + "  " + name.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
        }, "t2");

        t1.start();
        t2.start();
        countDownLatch.await();
        System.out.println("执行完毕");
    }

    @Test
    public void memLeakTest(){
//        char[] char1 = new char[20 * 1024 * 1024];
//        char1=null;
//        System.jvm.gc();
//        System.out.println("测试垃圾回收");
    }

}
