package thread;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadTest {

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


    private static class Task implements Runnable{

        private static AtomicInteger atomicInt=new AtomicInteger(0);
        private int taskId;

        public Task() {
            this.taskId=atomicInt.incrementAndGet();
        }

        @Override
        public void run() {
            System.out.println("线程" + Thread.currentThread().getName() + "执行任务编号为：" + taskId);
        }
    }

    //什么是Callable和Future?什么是FutureTask?

}
