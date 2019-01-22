package lock;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class TestLock {

    private void synMethod(String name,Object o,int i) throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + "尝试获取"+name);
        synchronized (o) {
            System.out.println(System.currentTimeMillis()+"第一次拿到对象锁"+name);
            Thread.sleep(i);
        }
    }

    //1、测试synchronized的代码如何使用，
    // 注意多线程同时执行同一处synchronized语句时才会出现同步。
    /** 结果：
     pool-1-thread-1尝试获取lock
     1548152384442第一次拿到对象锁lock
     pool-1-thread-2尝试获取lock
     1548152389443第一次拿到对象锁lock
     执行完毕
     */

    @Test
    public void testSyn() throws InterruptedException {
        Object lock = new Object();

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(2);

        executorService.execute(() -> {
            try {
                Thread.sleep(100);
                synMethod("lock",lock,5000);
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        executorService.execute(() -> {
            try {
                Thread.sleep(200);
                synMethod("lock",lock,1000);
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        countDownLatch.await();
        System.out.println("执行完毕");
    }

    //2、非重入性:测试结果JDK8中 synchronized 可重入
    @Test
    public void testReentrant() {
        Object lock = new Object();
        synchronized (lock) {
            System.out.println("第一次拿到lock对象的对象锁");
            synchronized (lock) {
                System.out.println("第二次拿到lock对象的对象锁");
            }
        }
    }

    //3、synchronized 给String 字面量上锁（字符串缓冲池的特点）
    /** 两个String引用实际上指向字符串缓冲池中同一个对象。
     *  结果：
     pool-1-thread-1尝试获取lock1
     1548152958273第一次拿到对象锁lock1
     pool-1-thread-2尝试获取lock2
     1548152963274第一次拿到对象锁lock2
     执行完毕
     */
    @Test
    public void testStringLock() throws InterruptedException {
        String lock1 = "abcdefg";
        String lock2 = "abcdefg";

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(2);

        executorService.execute(() -> {
            try {
                Thread.sleep(100);
                synMethod("lock1",lock1,5000);
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        executorService.execute(() -> {
            try {
                Thread.sleep(200);
                synMethod("lock2",lock2,1000);
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        countDownLatch.await();
        System.out.println("执行完毕");
    }


    //4 使用javap -c  查看字节码


    //5、ReentrantLock的condition，ReentrantReadWriteLock的使用


    //6、    AtomicReference的对象CAS原子操作

}
