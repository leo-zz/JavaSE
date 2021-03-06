package thread;

import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadTest {

    //测试线程状态
    /*
    线程dump结果：
        thread2-running 处于运行状态， RUNNABLE
        thread3-sleep 处于休眠状态，TIMED_WAITING (sleeping)
        thread4-wait  处于等待状态，WAITING (on object monitor)
        thread5-ioBlocking 处于运行状态， RUNNABLE
     JVM Thread中的state与操作系统中线程的状态有出入
     */
    @Test
    public void testThreadState() throws InterruptedException {
        Thread thread1 = new Thread("thread1-unStart");


        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName()+"开始忙等待");
                while (true) {
                    int i = 0;
                    int j=10;
                    double y=0.0;
                    j++;
                    for (; i < 100000000; i++) {
                        y+=i;
                    }
                    y=y/j;
                }
            }
        },"thread2-running");

        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int i = 0;
                    try {
                        System.out.println(Thread.currentThread().getName()+"开始sleep");
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        },"thread3-sleep");

        final Object o=new Object();
        Thread thread4 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (o){
                        System.out.println(Thread.currentThread().getName()+"开始等待");
                        o.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"thread4-wait");

        Thread thread5 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(5140);
                    System.out.println(Thread.currentThread().getName()+"启动ServerSocket");
                    Socket accept = serverSocket.accept();
                    System.out.println(Thread.currentThread().getName()+"接收到Socket请求");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        },"thread5-ioBlocking");

        Thread thread6 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName()+"执行完毕");
            }
        },"thread6-dead");

        Thread.sleep(20*1000);//等待10s，用于启动JVisualVM
        System.out.println("除thread1外启动其他线程");
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
        thread6.start();

        Thread.sleep(50*1000);//等待50s，用于观察
    }


    //run()和start()方法区别
    /*
    结果：
        线程main执行任务编号为：1
        线程main执行任务编号为：1
        线程Thread-0执行任务编号为：2

    结论：
        start()方法是在当前线程中启动一个新的线程，新线程会执行run()；start()方法只能执行一次。
        run()与普通方法一致，是在当前线程中执行；run()方法可以重复调用。
     */
    @Test
    public void testRunAndStart() throws InterruptedException {
        Task task1 = new Task();
        Task task2 = new Task();
        Thread thread = new Thread(task2);
        thread.start();
//        thread.start();
        task1.run();
        task1.run();
        Thread.sleep(100);
    }


    private static class Task implements Runnable {

        private static AtomicInteger atomicInt = new AtomicInteger(0);
        private int taskId;

        public Task() {
            this.taskId = atomicInt.incrementAndGet();
        }

        @Override
        public void run() {
            System.out.println("线程" + Thread.currentThread().getName() + "执行Runnable任务编号为：" + taskId);
            try {
                throw new Exception("runnable无法抛出异常，必须在run()方法内处理");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //什么是Callable和Future?什么是FutureTask?
    /*
    结果：
        java.lang.Exception: runnable无法抛出异常，必须在run()方法内处理
            at thread.ThreadTest$Task.run(ThreadTest.java:47)
            at java.lang.Thread.run(Thread.java:748)
        线程thread-task1执行Runnable任务编号为：1
        线程thread-task2执行Callable任务编号为：1
        线程启动完毕，并等待100ms
        task2已经完成
        执行结束，task2的结果为：1
    结论：
        Runnable与Callable都是用来创建可被线程执行的任务的，二者的区别：
        1、使用方面：Runnable可以直接赋给Thread，Callable必须由FutureTask封装才能赋给Thread，
            FutureTask又提供了丰富的功能，比如任务取消、结果阻塞获取、结果限时获取。
        2、返回结果方面：实现Callable接口的任务线程能返回执行结果；而实现Runnable接口的任务线程不能返回结果；
        3、受检异常抛出方面：Callable接口的call()方法允许抛出异常；而Runnable接口的run()方法的异常只能在内部消化，不能继续上抛；
    */
    @Test
    public void testCallable() throws InterruptedException {
        Task task1 = new Task();
        CallableTask callableTask = new CallableTask(); //task2检测时已经完成
//        CallableTask callableTask = new CallableTask(1000);
//        CallableTask callableTask = new CallableTask(3000);//取消任务执行；执行超时，未拿到结果。
        FutureTask<Integer> task2 = new FutureTask<>(callableTask);

        Thread thread1 = new Thread(task1, "thread-task1");
        Thread thread2 = new Thread(task2, "thread-task2");
        thread1.start();
        thread2.start();

        Thread.sleep(100);
        System.out.println("线程启动完毕，并等待100ms");

        //方法非阻塞
        if (task2.isDone()) {
            System.out.println("task2已经完成");
        } else {
            System.out.println("task2未完成，取消任务");
            boolean cancel = task2.cancel(true);
            System.out.println(cancel ? "取消成功" : "取消失败");

        }

        if (task2.isCancelled()) {
            System.out.println("task2取消执行，不在获取结果");
        } else {
            try {
                //方法阻塞
                Integer integer = task2.get(2, TimeUnit.SECONDS);
                System.out.println("执行结束，task2的结果为：" + integer);
            } catch (ExecutionException e) {
                System.out.println("task2执行出错");
                e.printStackTrace();
            } catch (TimeoutException e) {
                System.out.println("task2执行超时，未拿到结果");
                e.printStackTrace();
            }
        }
    }


    private static class CallableTask implements Callable<Integer> {

        private static AtomicInteger atomicInt = new AtomicInteger(0);
        private int taskId;
        private int sleepTime;


        public CallableTask(int sleepTime) {
            this.sleepTime = sleepTime;
            this.taskId = atomicInt.incrementAndGet();
        }


        public CallableTask() {
            this.taskId = atomicInt.incrementAndGet();
        }

        @Override
        public Integer call() throws Exception {
            Thread.sleep(sleepTime);
            System.out.println("线程" + Thread.currentThread().getName() + "执行Callable任务编号为：" + taskId);
            if (taskId == 4) throw new Exception("runnable无法抛出异常，必须在run()方法内处理");
            return taskId;
        }

    }


}
