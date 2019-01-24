package lock;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


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

    private void testCondition(Lock lock, CountDownLatch countDownLatch, Condition c1, String lname, String cname) {
        System.out.println(Thread.currentThread().getName() + "开始执行");
        try {
            System.out.println(Thread.currentThread().getName() + "加锁" + lname);

            lock.lock();
            System.out.println(Thread.currentThread().getName() + "等待" + cname);
            countDownLatch.countDown();
            c1.await();
            System.out.println(Thread.currentThread().getName() + "被" + cname + "唤醒并拿到锁" + lname);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            System.out.println(Thread.currentThread().getName() + "释放锁" + lname);
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
        //原理：由内部类FairSync调用AQS实现      FairSync是AbstractQueuedSynchronizer的子类，实现了具体的代码。
        /*
        a.尝试获取独占锁
        1、获取AQS 的state(表示线程获取锁的次数，可重入，volatile)
        2、如果state为0，判断等待队列中是否存在早于当前线程请求锁的线程（公平性的体现）
        2.1、若不存在，使通过CAS操作将state改为1，并将锁的线程指向当前线程。其中，CAS由Unsafe类中的本地代码实现
        2.2、若存在，锁获取失败，跳转至b。
        3、如果state不为0，则获取持有该锁的线程，判断与当前线程是否相等。
        3.1、若相等，则更新state（加一），如果重入次数太多造成int型的state溢出，则抛异常。
        b.锁获取失败，将当前线程加入到等待队列 由AbstractQueuedSynchronizer.Node节点组成的双向链表，Node封装了线程信息，有两种模式，EXCLUSIVE与SHARED
        1、首先将当前线程封装为Node节点，并判断等待队列是否为空。
        1.1、若不为空，执行1次CAS操作将Node节点写入到队尾，如果成功则返回。
        1.2、若CAS写入队尾失败或者队列为空，则使用enq()继续写入。
        2、如果队列为空，则先初始化队列，然后自旋式执行CAS操作将Node写入队尾。自旋保证写入操作一定能够完成。
        c.写入成功后会挂起当前线程
        1、执行挂起操作前先判断Node的前继节点是否为head，如果是说明Node节点在队列中排第一，会尝试获取锁，执行步骤a。
        1.1、若成功获取锁，会将当前线程Node节点移出队列，完成操作。
        1.2、若获取锁失败会根据Node节点的状态判断是否需要挂起线程。
        1.3、若需要挂起则借助LockSupport挂起线程，并检查是否要中断。
        1.4、被唤醒后重复执行c。
        d.释放锁
        1、检查当前线程是否为占有锁的线程，如果不是则抛出异常。
        1.1、如果是，判断state减一后是否为0；
        1.2、如果为0，则清除 锁的线程信息，并更新state值，并唤醒队列中排第一位的被挂起的线程。
        1.3、如果不为0，锁继续被该线程占有。

         */
        fairService.execute(() -> {
            reentrantLockTest(fair, "公平锁", 3000);
            fairLatch.countDown();
        });
        for (int i = 0; i < count - 1; i++) {
            final int j = i;
            fairService.execute(() -> {
                try {
                    Thread.sleep(j);
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

        //Statement lambda can be replaced with expression lambda
        new Thread(() ->
                testCondition(lock, countDownLatch, c1, "lock1", "c1"), "t1").start();

        new Thread(() ->
                testCondition(lock, countDownLatch, c2, "lock1", "c2"), "t2").start();

        new Thread(() ->
                testCondition(lock, countDownLatch, c1, "lock1", "c1")
                , "t3").start();

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
        } finally {
            lock.unlock();
        }
    }


    //5、 ReentrantReadWriteLock的使用，
    // 结论：读锁不是互斥锁,写锁是互斥所。有线程拿到读锁/写锁后，其他线程不能再拿写锁/读锁。
    /*
        结果：
        t1请求写锁 1548235231622
        t1拿到写锁，开始写入数据 1548235231622
        t2请求写锁 1548235231623
        t3请求读锁 1548235231624
        t4请求读锁 1548235231625
        t5请求写锁 1548235231625
        t1释放写锁 1548235233622
        t2拿到写锁，开始写入数据 1548235233622
        t2释放写锁 1548235235623
        t3拿到读锁 1548235235623
        t4拿到读锁 1548235235623
        t4读取到的数据：SharedVar{num=2, str='second'}
        t3读取到的数据：SharedVar{num=2, str='second'}
        t4释放读锁 1548235237624
        t3释放读锁 1548235237624
        t5拿到写锁，开始写入数据 1548235237624
        t5释放写锁 1548235239624
        执行完毕 1548235239624
     */
    @Test
    public void testReentrantReadWriteLock() throws InterruptedException {
        int a = 0;
        SharedVar sharedVar = new SharedVar();

        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        CountDownLatch countDownLatch = new CountDownLatch(5);

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService.execute(() -> {
            try{
                System.out.println("t1请求写锁 "+System.currentTimeMillis());
                writeLock.lock();
                System.out.println("t1拿到写锁，开始写入数据 "+System.currentTimeMillis());
                sharedVar.setNum(1);
                sharedVar.setStr("first");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("t1释放写锁 "+System.currentTimeMillis());
                writeLock.unlock();
                countDownLatch.countDown();
            }
        });

        executorService.execute(() -> {
            try{
                System.out.println("t2请求写锁 "+System.currentTimeMillis());
                writeLock.lock();
                System.out.println("t2拿到写锁，开始写入数据 "+System.currentTimeMillis());
                sharedVar.setNum(2);
                sharedVar.setStr("second");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("t2释放写锁 "+System.currentTimeMillis());
                writeLock.unlock();
                countDownLatch.countDown();
            }
        });

        executorService.execute(() -> {
            try{
                System.out.println("t3请求读锁 "+System.currentTimeMillis());
                readLock.lock();
                System.out.println("t3拿到读锁 "+System.currentTimeMillis());
                Thread.sleep(2000);
                System.out.println("t3读取到的数据："+sharedVar);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("t3释放读锁 "+System.currentTimeMillis());
                readLock.unlock();
                countDownLatch.countDown();
            }
        });

        executorService.execute(() -> {
            try{
                System.out.println("t4请求读锁 "+System.currentTimeMillis());
                readLock.lock();
                System.out.println("t4拿到读锁 "+System.currentTimeMillis());
                Thread.sleep(2000);
                System.out.println("t4读取到的数据："+sharedVar);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("t4释放读锁 "+System.currentTimeMillis());
                readLock.unlock();
                countDownLatch.countDown();
            }
        });
        executorService.execute(() -> {
            try{
                System.out.println("t5请求写锁 "+System.currentTimeMillis());
                writeLock.lock();
                System.out.println("t5拿到写锁，开始写入数据 "+System.currentTimeMillis());
                sharedVar.setNum(1);
                sharedVar.setStr("first");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("t5释放写锁 "+System.currentTimeMillis());
                writeLock.unlock();
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();
        System.out.println("执行完毕 "+System.currentTimeMillis());

    }

    static class SharedVar {
        int num;
        String str;

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return "SharedVar{" +
                    "num=" + num +
                    ", str='" + str + '\'' +
                    '}';
        }
    }
}
