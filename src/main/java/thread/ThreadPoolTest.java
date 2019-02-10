package thread;

import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolTest {

    @Test
    public void ThreadPoolTest() throws InterruptedException {
//        BlockingQueue<Runnable> queue = new ArrayBlockingQueue(5);
//        BlockingQueue<Runnable> queue = new LinkedBlockingQueue();
        BlockingQueue<Runnable> queue = new SynchronousQueue();

        //自定义名称的ThreadFactory，用于创建线程时使用。
        //注意将任务传递给线程执行
        ThreadFactory threadFactory = new ThreadFactory() {

            private AtomicInteger atomicInt = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                String tName = "testThreadPool-" + atomicInt.getAndIncrement();
                System.out.println("创建线程：" + tName);
                return new Thread(r, tName);
            }
        };
        //默认的ThreadFactory
        ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

        //拒绝策略是指当线程池刚关闭或者线程池中工作线程数量已经达到最大值时接收到新任务的处理方式。
        //使用ThreadPool中自带的四种默认策略就行：拒绝策略所在线程执行任务、抛出异常拒绝任务（默认的策略）、直接丢弃任务、丢弃队列中等待时间最久的任务并执行该任务
        //一般情况下不需要自己重写
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
//        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
//        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardOldestPolicy();
//        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();//能看到main线程执行任务
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(5, 10, 10, TimeUnit.SECONDS, queue, threadFactory, handler);

        for (int i = 0; i < 100; i++) {
            poolExecutor.execute(new Task());
//            Thread.sleep(0, 3);//等待3ns
        }
            Thread.sleep(5000);
    }

    private static class Task implements Runnable {

        private static final AtomicInteger index = new AtomicInteger(0);

        @Override
        public void run() {
            System.out.println("线程" + Thread.currentThread().getName() + "执行任务编号为：" + index.incrementAndGet());
            try {
                //用于跟踪线程池如何判断线程处于工作状态。
                Thread.sleep(300000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
