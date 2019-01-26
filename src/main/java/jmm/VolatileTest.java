package jmm;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//如何模拟出内存不可见的情况？
public class VolatileTest {


    //希望能通过测试看出，改变flag后，还运行了一段时间。
    /*
    内存可见性的体现，有大约20%的线程出现了问题：
    pool-1-thread-28改变flag 265638085964616
    pool-1-thread-30改变flag 265638095268916
    pool-1-thread-32改变flag 265638102869113
    pool-1-thread-2由于内存可见性导致flag修改后，循环还运行了一段时间InnerVolatile{stopTime=265637965798046, lastRunTime=265638147604165, finishTime=0}
    pool-1-thread-4由于内存可见性导致flag修改后，循环还运行了一段时间InnerVolatile{stopTime=265637970405609, lastRunTime=265638152592314, finishTime=0}
    pool-1-thread-6由于内存可见性导致flag修改后，循环还运行了一段时间InnerVolatile{stopTime=265637992769295, lastRunTime=265638172022673, finishTime=0}
    将flag改为volatile，可以杜绝此问题。
     */
    @Test
    public void volatileTest() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(100);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i=0;i<100;i++){
            InnerVolatile iv1 = new InnerVolatile();
            executorService.execute(iv1);
            executorService.execute(()->{
                iv1.stopThreadByFlag();
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        System.out.println("over");
    }

    private static class InnerVolatile implements Runnable{

//        private  boolean flag=true;
        private  volatile boolean flag=true;
        private long stopTime;
        private long lastRunTime;
        private long finishTime;


        @Override
        public void run() {
            while(flag){
                lastRunTime=System.nanoTime();
            }
            finishTime=System.nanoTime();
            System.out.println(Thread.currentThread().getName() +"执行完毕 "+finishTime);
        }

        void stopThreadByFlag(){
            flag=false;
            stopTime= System.nanoTime();
            System.out.println(Thread.currentThread().getName() +"改变flag "+stopTime);
            try {
                //增加延迟，等待finishTime的赋值。
                Thread.sleep(100);
                if(lastRunTime>stopTime){
                    System.out.println(Thread.currentThread().getName()+"由于内存可见性导致flag修改后，循环还运行了一段时间"+this);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        @Override
        public String toString() {
            return "InnerVolatile{" +
                    "stopTime=" + stopTime +
                    ", lastRunTime=" + lastRunTime +
                    ", finishTime=" + finishTime +
                    '}';
        }
    }

}
