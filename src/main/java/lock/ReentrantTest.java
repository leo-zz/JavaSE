package lock;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * ReentrantLock的高级特性：等待可中断、公平锁、多条件绑定实现选择性通知
 */
public class ReentrantTest {

    private void reentrantLockTest(ReentrantLock lock, String name, int i) {
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

    private void deadLockTest1(ReentrantLock lock1, ReentrantLock lock2, String name1, String name2, int i) {
        try {
            System.out.println(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " 获取锁:" + name1);
            lock1.lock();
            Thread.sleep(i);
            //建议不要使用嵌套锁，如果必须要用，同时使用嵌套try-catch，避免unlock失败，造成其他线程死锁。
            try {
                lock2.lock();
                System.out.println(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " 获取锁:" + name2);
            } finally {
                lock2.unlock();
                System.out.println(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " 释放锁:" + name2);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock1.unlock();
            System.out.println(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " 释放锁:" + name1);
        }
    }

    private void testCondition(Lock lock, CountDownLatch countDownLatch, Condition c1,String lname, String cname) {
        System.out.println(Thread.currentThread().getName()+"开始执行");
        try {
            System.out.println(Thread.currentThread().getName()+"加锁"+lname);

            lock.lock();
            System.out.println(Thread.currentThread().getName()+"等待"+cname);
            countDownLatch.countDown();
            c1.await();
            System.out.println(Thread.currentThread().getName()+"被"+cname+"唤醒并拿到锁"+lname);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
            System.out.println(Thread.currentThread().getName()+"释放锁"+lname);
        }
    }

    /**
     * 使用lock.lockInterruptibly();方法进行加锁，当等待锁的时候，可以调用线程的interrpt()方法中断等待过程。
     */
    private void deadLockTest2(ReentrantLock lock1, ReentrantLock lock2, String name1, String name2, int i) {
        try {
            System.out.println(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " 获取锁:" + name1);
            lock1.lock();
            Thread.sleep(i);
            //建议不要使用嵌套锁，如果必须要用，同时使用嵌套try-catch，避免unlock失败，造成其他线程死锁。
            try {
                lock2.lockInterruptibly();
                System.out.println(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " 获取锁:" + name2);
            } finally {
                lock2.unlock();
                System.out.println(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " 释放锁:" + name2);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock1.unlock();
            System.out.println(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " 释放锁:" + name1);
        }
    }

    //1、ReentrantLock的使用习惯，建议跟try-finally连用，确保锁能够关闭，避免引起死锁。
    /*
     结果：
        lock1未释放
        lock2已释放
     */
    @Test
    public void lockUseTest() throws InterruptedException {
        ReentrantLock lock1 = new ReentrantLock();
        ReentrantLock lock2 = new ReentrantLock();

        Thread t1 = new Thread(() -> {
            lock1.lock();
            int a = 3 / 0;
            lock1.unlock();
        }, "t1");

        Thread t2 = new Thread(() -> {
            try {
                lock2.lock();
                int a = 3 / 0;
            } finally {
                lock2.unlock();
            }
        }, "t2");
        t1.start();
        t2.start();
        Thread.sleep(3000);

        System.out.println(lock1.isLocked() ? "lock1未释放" : "lock1已释放");
        System.out.println(lock2.isLocked() ? "lock2未释放" : "lock2已释放");
    }

    //2、ReentrantLock的等待可中断
    /*
        使用ReentrantLock模拟死锁并演示中断。
        deadLockTest1()不会触发中断，会导致死锁。
        deadLockTest2()会触发中断，不会导致死锁。

        死锁线程的dump信息：
        "t2" #12 prio=5 os_prio=0 tid=0x0000000020b08000 nid=0x73c4 waiting on condition [0x000000002119e000]
           java.lang.Thread.State: WAITING (parking)
            at sun.misc.Unsafe.park(Native Method)
            - parking to wait for  <0x00000007566a52f0> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)
        Locked ownable synchronizers:
            - <0x00000007566a5320> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)

        "t1" #11 prio=5 os_prio=0 tid=0x0000000020b07800 nid=0x7274 waiting on condition [0x000000002109e000]
           java.lang.Thread.State: WAITING (parking)
            at sun.misc.Unsafe.park(Native Method)
            - parking to wait for  <0x00000007566a5320> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)
         Locked ownable synchronizers:
            - <0x00000007566a52f0> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)
     */

    @Test
    public void interruptLockTest() throws InterruptedException {
        ReentrantLock lock1 = new ReentrantLock();
        ReentrantLock lock2 = new ReentrantLock();

        CountDownLatch countDownLatch = new CountDownLatch(2);

        Thread t1 = new Thread(() -> {
            //不允许中断
            deadLockTest1(lock1, lock2, "lock1", "lock2", 1000);
            //使用lock.lockInterruptibly();，等待锁的过程允许中断
//            deadLockTest2(lock1, lock2, "lock1", "lock2", 1000);
            countDownLatch.countDown();
        }, "t1");

        Thread t2 = new Thread(() -> {
            deadLockTest2(lock2, lock1, "lock2", "lock1", 1000);
            countDownLatch.countDown();
        }, "t2");
        t1.start();
        t2.start();
        Thread.sleep(3000);
        if (countDownLatch.getCount() > 0) {
            System.out.println("超时！中断线程t1释放锁");
            t1.interrupt();
        } else {
            System.out.println("执行完毕！");
        }
        while (t2.getState() != Thread.State.TERMINATED) {
            Thread.sleep(1000);
        }
    }


    //3、ReentrantLock的公平锁
    //  由于受CPU的特性线程的调度没有绝对的先后顺序，使用sleep尽量让线程排好队。
    // 结果：在并发度不是特别高的时候，非公平锁的不公平性很难体现：
    @Test
    public void fairLockTest() throws InterruptedException {
        int count = 100;
        ExecutorService fairService = Executors.newFixedThreadPool(count);
        CountDownLatch fairLatch = new CountDownLatch(count);

        ExecutorService unfairService = Executors.newFixedThreadPool(count);
        CountDownLatch unfairLatch = new CountDownLatch(count);


        ReentrantLock fair = new ReentrantLock(true);
        ReentrantLock unfair = new ReentrantLock();

        //非公平锁
        //先持有3s以上的锁，让其他线程都进入争抢该锁的状态
        unfairService.execute(() -> {
            reentrantLockTest(unfair, "非公平锁", 3000);
            unfairLatch.countDown();
        });
        //确保锁的排队顺序
        for (int i = 0; i < count - 1; i++) {
            final int j = i;
            unfairService.execute(() -> {
                try {
                    Thread.sleep(j * 1);
                    System.out.println(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " 开始排队等非公平锁：");
                    reentrantLockTest(unfair, "非公平锁", 1);
                    unfairLatch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        unfairLatch.await();
        System.out.println("***********************************");

        //公平锁
        fairService.execute(() -> {
            reentrantLockTest(fair, "公平锁", 3000);
            fairLatch.countDown();
        });
        for (int i = 0; i < count - 1; i++) {
            final int j = i;
            fairService.execute(() -> {
                try {
                    Thread.sleep(j * 1);
                    System.out.println(Thread.currentThread().getName() + " " + System.currentTimeMillis() + " 开始排队等公平锁：");
                    reentrantLockTest(fair, "公平锁", 10);
                    fairLatch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        fairLatch.await();
        System.out.println("执行完毕");
    }
    //4、ReentrantLock的多条件绑定
    /*
        t1绑定c1
        t2绑定c2
        t3绑定c1
      执行结果：
        t1开始执行
        t1加锁lock1
        t1等待c1
        t2开始执行
        t2加锁lock1
        t2等待c2
        t3开始执行
        t3加锁lock1
        t3等待c1
        三个线程都处于等待状态
        加锁后唤醒t2
        唤醒t1与t3
        解锁，执行完毕
        #############################
        t2被c2唤醒并拿到锁lock1
        t2释放锁lock1
        t1被c1唤醒并拿到锁lock1
        t1释放锁lock1
        t3被c1唤醒并拿到锁lock1
        t3释放锁lock1
     */
    @Test
    public void testMultiCondition() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        CountDownLatch countDownLatch = new CountDownLatch(3);

        /**
         *  使用condition的await/signal()时与object的wait/notify()的异同
         *  condition要在持有对应lock锁的代码块中调用，否则抛异常。      object要在Synchronized语句块中使用
         *  调用condition的await()方法时会释放该线程使用的锁,signal()后线程会重新请求锁。   与object一致
         *  condition的await()时可以被中断
         *  线程的signal()顺序是FIFO的，也就是说先等待的先唤醒。
         */
        Condition c1 = lock.newCondition();
        Condition c2 = lock.newCondition();

        new Thread(()->{
            testCondition(lock,countDownLatch, c1, "lock1","c1");
        },"t1").start();

        new Thread(()->{
            testCondition(lock,countDownLatch, c2, "lock1","c2");
        },"t2").start();

        new Thread(()->{
            testCondition(lock,countDownLatch, c1, "lock1","c1");
        },"t3").start();

        countDownLatch.await();
        System.out.println("三个线程都处于等待状态");

        try {
            lock.lock();
            Thread.sleep(1000);
            System.out.println("加锁后唤醒t2");
            c2.signalAll();
            Thread.sleep(1000);
            c1.signalAll();
            System.out.println("唤醒t1与t3");
            System.out.println("解锁，执行完毕");
            System.out.println("#############################");
        }finally {
            lock.unlock();
        }

    }



    //5、 ReentrantReadWriteLock的使用



}
