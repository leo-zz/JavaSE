package lock;

import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.LockSupport;

public class LockSupportTest {

    //ReentrantLock使用LockSupport实现线程的挂起和恢复，以及CyclicBarrier模拟实现CountdownLatch
    /*
        结果:
        main等待其他线程，等待7s后再调用barrier的wait，让main线程成为最后进入
        t1开始执行
        t1开始等待
        t2开始执行
        t2等待5s后唤醒t1
        t2执行完毕
        t1恢复执行
        main，action由最后进入barrier的线程执行。使用CyclicBarrier模拟实现CountdownLatch的功能
        main执行完毕
     */
    @Test
    public void lockSupportUseTest() throws BrokenBarrierException, InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3, () ->
                System.out.println(Thread.currentThread().getName() + "，action由最后进入barrier的线程执行。使用CyclicBarrier模拟实现CountdownLatch的功能")
        );

        Thread t1 = new Thread(() -> {
            try {
                System.out.println("t1开始执行");
                System.out.println("t1开始等待");
                LockSupport.park();
                System.out.println("t1恢复执行");
                cyclicBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            try {
                System.out.println("t2开始执行");
                System.out.println("t2等待5s后唤醒t1");
                Thread.sleep(5000);
                LockSupport.unpark(t1);
                System.out.println("t2执行完毕");
                cyclicBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }, "t2");
        t1.start();
        t2.start();
        System.out.println(Thread.currentThread().getName() + "等待其他线程，等待7s后再调用barrier的wait，让main线程成为最后进入");
        Thread.sleep(7000);
        cyclicBarrier.await();
        System.out.println(Thread.currentThread().getName() + "执行完毕");
    }

}
