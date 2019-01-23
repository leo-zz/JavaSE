package lock;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock的高级特性：等待可中断、公平锁、多条件绑定实现选择性通知
 */
public class ReentrantTest {

    private void reentrantLockTest(Lock lock, String name, int i) {
        try {
            lock.lock();
            Thread.sleep(i);
            System.out.println(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " 第一次获取锁:" + name);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            System.out.println(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " 释放锁:" + name);
        }


    }

    //1、ReentrantLock的等待可中断


    //2、ReentrantLock的公平锁
    //  由于受CPU的特性线程的调度没有绝对的先后顺序，使用sleep尽量让线程排好队。
    /**
       在并发度不是特别高的时候，非公平锁的不公平性很难体现：
     */
    @Test
    public void FairLockTest() throws InterruptedException {



//        int count = 100;
//        ExecutorService fairService = Executors.newFixedThreadPool(count);
//        CountDownLatch fairLatch = new CountDownLatch(count);
//
//        ExecutorService unfairService = Executors.newFixedThreadPool(count);
//        CountDownLatch unfairLatch = new CountDownLatch(count);
//
//
//        ReentrantLock fair = new ReentrantLock(true);
//        ReentrantLock unfair = new ReentrantLock();
//
//        //非公平锁
//        //先持有3s以上的锁，让其他线程都进入争抢该锁的状态
//        unfairService.execute(() -> {
//            reentrantLockTest(unfair, "非公平锁", 3000);
//            unfairLatch.countDown();
//        });
//        //确保锁的排队顺序
//        for (int i = 0; i < count-1; i++) {
//            final int j = i;
//            unfairService.execute(() -> {
//                try {
//                    Thread.sleep(j * 1);
//                    System.out.println(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " 开始排队等非公平锁：");
//                    reentrantLockTest(unfair, "非公平锁", 1);
//                    unfairLatch.countDown();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//        unfairLatch.await();
//        System.out.println("***********************************");
//
//        //公平锁
//        fairService.execute(() -> {
//            reentrantLockTest(fair, "公平锁", 3000);
//            fairLatch.countDown();
//        });
//        for (int i = 0; i < count-1; i++) {
//            final int j = i;
//            fairService.execute(() -> {
//                try {
//                    Thread.sleep(j * 1);
//                    System.out.println(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " 开始排队等公平锁：");
//                    reentrantLockTest(fair, "公平锁", 10);
//                    fairLatch.countDown();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//        fairLatch.await();
//        System.out.println("执行完毕");
    }


    //3、ReentrantLock的多条件绑定

    //8、 ReentrantReadWriteLock的使用


    //9、 AtomicReference的对象CAS原子操作
}
