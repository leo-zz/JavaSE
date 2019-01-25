package juc;

import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class JUCTest {

    //  CyclicBarrier的使用，可重用，相比CountdownLatch更灵活。
    //  借助赛跑比赛的情景体现CyclicBarrier的含义
    /*
        结果：
        t1 准备比赛,预计准备 1000ms 1548380939323
        t2 准备比赛,预计准备 1500ms 1548380939324
        t1 准备完毕 1548380940323
        t2 准备完毕 1548380940830
        t2最后完成 1548380940830
        main宣布比赛正式开始 1548380940830
        t1 比赛开始 1548380940830
        t2 比赛开始 1548380940832
        t2 比赛进行中,预计全程耗时1500ms 1548380940832
        t1 比赛进行中,预计全程耗时2000ms 1548380940832
        t2 到达终点1548380942333
        t1 到达终点1548380942832
        t1最后完成 1548380942832
        t1 比赛结束 1548380942832
        main宣布比赛结束 1548380942832
        t2 比赛结束 1548380942832
     */
    @Test
    public void cyclicBarrierUseTest() throws BrokenBarrierException, InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3, () ->
                System.out.println(Thread.currentThread().getName() + "最后完成 " + System.currentTimeMillis())
        );
        Thread t1 = null;
        Thread t2 = null;

        t1 = new Thread(() -> {
            raceBegin(cyclicBarrier, 1000);
        }, "t1");

        t2 = new Thread(() -> {
            raceBegin(cyclicBarrier, 1500);
        }, "t2");
        t1.start();
        t2.start();
        cyclicBarrier.await();
        System.out.println(Thread.currentThread().getName() + "宣布比赛正式开始 " + System.currentTimeMillis());

        //重置cyclicBarrier，以便复用；如果有其他进程正在等待时，会造成BrokenBarrierException异常抛出。
        cyclicBarrier.reset();
        t1 = new Thread(() -> {
            raceFinish(cyclicBarrier, 2000);
        }, "t1");

        t2 = new Thread(() -> {
            raceFinish(cyclicBarrier, 1500);
        }, "t2");

        t1.start();
        t2.start();
        cyclicBarrier.await();
        System.out.println(Thread.currentThread().getName() + "宣布比赛结束 " + System.currentTimeMillis());
    }

    private void raceBegin(CyclicBarrier cyclicBarrier, long i) {
        try {
            System.out.println(Thread.currentThread().getName() + " 准备比赛,预计准备 " + i + "ms " + System.currentTimeMillis());
            Thread.sleep(i);
            System.out.println(Thread.currentThread().getName() + " 准备完毕 " + System.currentTimeMillis());
            cyclicBarrier.await();
            System.out.println(Thread.currentThread().getName() + " 比赛开始 " + System.currentTimeMillis());
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private void raceFinish(CyclicBarrier cyclicBarrier, long i) {
        try {
            System.out.println(Thread.currentThread().getName() + " 比赛进行中,预计全程耗时" + i + "ms " + System.currentTimeMillis());
            Thread.sleep(i);
            System.out.println(Thread.currentThread().getName() + " 到达终点" + System.currentTimeMillis());
            cyclicBarrier.await();
            System.out.println(Thread.currentThread().getName() + " 比赛结束 " + System.currentTimeMillis());
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    //semaphore的使用

    //FutureTask的使用

    //使用 BlockingQueue 实现生产者消费者问题

    //ForkJoin
}
