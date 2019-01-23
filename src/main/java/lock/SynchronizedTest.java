package lock;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 资料：
 * Java的对象头和对象组成详解 https://blog.csdn.net/lkforce/article/details/81128115#1%EF%BC%8CMark%20Word
 */
public class SynchronizedTest {

    public void synMethod1(String name, Object o, int i) throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + "尝试获取" + name);
        System.out.println("加锁前lock的hash: " + o.hashCode());
        synchronized (o) {
            Thread.sleep(10);
            System.out.println(Thread.currentThread().getName() + "  " + System.currentTimeMillis() + "第一次拿到对象锁" + name);
            System.out.println("加锁后lock的hash: " + o.hashCode());
            Thread.sleep(i);
        }
        System.out.println(Thread.currentThread().getName() + "  " + System.currentTimeMillis() + "释放对象锁" + name);

    }

    public synchronized void synMethod2(String name, int i) throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + "  " + System.currentTimeMillis() + "第一次拿到对象锁" + name);
        Thread.sleep(i);
    }

    public void deadLockMethod(String name1,String name2,Object o1, Object o2, int i1,int i2) throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + "尝试获取锁:"+name1);
        synchronized (o1) {
            Thread.sleep(10);
            System.out.println(Thread.currentThread().getName() + "  " + System.currentTimeMillis() + "第一次拿到对象锁" + name1);
            Thread.sleep(i1);
            System.out.println(Thread.currentThread().getName() + "尝试获取锁:"+name2);
            synchronized (o2) {
                Thread.sleep(10);
                System.out.println(Thread.currentThread().getName() + "  " + System.currentTimeMillis() + "第一次拿到对象锁" + name2);
                Thread.sleep(i2);
            }
            System.out.println(Thread.currentThread().getName() + "  " + System.currentTimeMillis() + "释放对象锁" + name2);
        }
        System.out.println(Thread.currentThread().getName() + "  " + System.currentTimeMillis() + "释放对象锁" + name1);
    }

    //1、测试synchronized的代码如何使用，
    // 多线程同时执行同一处synchronized语句时才会出现同步。
    // 对象加锁后对象头的Mark Word虽然不存放hash，但是对象的hash仍然可以获取。

    /**
     * 结果：
     * pool-1-thread-1尝试获取lock
     * 加锁前lock的hash: 212383667
     * pool-1-thread-1  1548204048888第一次拿到对象锁lock
     * 加锁后lock的hash: 212383667
     * pool-1-thread-2尝试获取lock
     * 加锁前lock的hash: 212383667
     * pool-1-thread-1  1548204053888释放对象锁lock
     * pool-1-thread-2  1548204053898第一次拿到对象锁lock
     * 加锁后lock的hash: 212383667
     * pool-1-thread-2  1548204054899释放对象锁lock
     * 执行完毕
     */


    @Test
    public void testSyn() throws InterruptedException {
        Object lock = new Object();

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(2);

        executorService.execute(() -> {
            try {
                Thread.sleep(100);
                synMethod1("lock", lock, 5000);
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        executorService.execute(() -> {
            try {
                Thread.sleep(200);
                synMethod1("lock", lock, 1000);
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        countDownLatch.await();
        System.out.println("执行完毕");
    }

    //2、 多个不同的synchronized语句对同一个对象加锁时也会出现同步，要清楚Synchronized基于对象头实现。
    //参考：https://blog.csdn.net/lkforce/article/details/81128115

    /**
     * 结果：
     * pool-1-thread-1尝试获取thislock
     * pool-1-thread-1  1548203132510第一次拿到对象锁thislock
     * pool-1-thread-2尝试获取thislock
     * pool-1-thread-1  1548203137510释放对象锁thislock
     * pool-1-thread-2 1548203137510第一次拿到对象锁thislock
     * pool-1-thread-2  1548203138511释放对象锁thislock
     * 执行完毕
     */

    @Test
    public void testMultiSynAcquireOneObject() throws InterruptedException {
        Object lock = new Object();

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(2);

        executorService.execute(() -> {
            try {
                Thread.sleep(100);
                synMethod1("thislock", this, 5000);
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        executorService.execute(() -> {
            try {
                Thread.sleep(200);
                System.out.println(Thread.currentThread().getName() + "尝试获取" + "thislock");
                synMethod2("thislock", 1000);
                System.out.println(Thread.currentThread().getName() + "  " + System.currentTimeMillis() + "释放对象锁" + "thislock");
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        countDownLatch.await();
        System.out.println("执行完毕");
    }

    //3、可重入性:测试结果JDK8中 synchronized 可重入
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

    //4、synchronized 给String 字面量上锁（字符串缓冲池的特点）,其他基本类型也是如此。
    //  使用时避免确保锁的粒度尽可能细，避免无畏的争抢。

    /**
     * 两个String引用实际上指向字符串缓冲池中同一个对象。
     * 结果：
     * pool-1-thread-1尝试获取lock1
     * 1548152958273第一次拿到对象锁lock1
     * pool-1-thread-2尝试获取lock2
     * 1548152963274第一次拿到对象锁lock2
     * 执行完毕
     */

    @Test
    public void testStringLock() throws InterruptedException {
//        String lock1 = "abcdefg";
//        String lock2 = "abcdefg";
        int lock1 = 1;
        int lock2 = 1;

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(2);

        executorService.execute(() -> {
            try {
                Thread.sleep(100);
                synMethod1("lock1", lock1, 5000);
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        executorService.execute(() -> {
            try {
                Thread.sleep(200);
                synMethod1("lock2", lock2, 1000);
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        countDownLatch.await();
        System.out.println("执行完毕");
    }
    //5 使用javap -c classFile 查看字节码，
    // 注意private的方法 看不到字节码
    /*
        修饰代码段可以看到：monitorenter与monitorexit
        修饰方法，字节码中看不到相关信息
     */

    //6、synchronized的死锁
    /*
    结果：
    pool-1-thread-1尝试获取锁:lock1
    pool-1-thread-1  1548211849436第一次拿到对象锁lock1
    pool-1-thread-2尝试获取锁:lock2
    pool-1-thread-2  1548211849533第一次拿到对象锁lock2
    pool-1-thread-1尝试获取锁:lock2
    pool-1-thread-2尝试获取锁:lock1

    使用JVisualVM可以检测到死锁，线程Dump如下
"pool-1-thread-2" #12 prio=5 os_prio=0 tid=0x0000000020096800 nid=0x1b68 waiting for monitor entry [0x000000002137e000]
   java.lang.Thread.State: BLOCKED (on object monitor)
	at lock.SynchronizedTest.deadLockMethod(SynchronizedTest.java:41)
	- waiting to lock <0x00000007566a7da8> (a java.lang.Object)
	- locked <0x00000007566a7db8> (a java.lang.Object)

"pool-1-thread-1" #11 prio=5 os_prio=0 tid=0x0000000020b20800 nid=0x323c waiting for monitor entry [0x000000002127e000]
   java.lang.Thread.State: BLOCKED (on object monitor)
	at lock.SynchronizedTest.deadLockMethod(SynchronizedTest.java:41)
	- waiting to lock <0x00000007566a7db8> (a java.lang.Object)
	- locked <0x00000007566a7da8> (a java.lang.Object)
     */
    @Test
    public void testDeadLock() throws InterruptedException {
        Object lock1 = new Object();
        Object lock2 = new Object();

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(2);

        executorService.execute(() -> {
            try {
                Thread.sleep(100);
                deadLockMethod("lock1","lock2", lock1, lock2,  1000,100);
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        executorService.execute(() -> {
            try {
                Thread.sleep(200);
                deadLockMethod("lock2","lock1", lock2, lock1,1000,100);
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        countDownLatch.await();
        System.out.println("执行完毕");
    }
}
