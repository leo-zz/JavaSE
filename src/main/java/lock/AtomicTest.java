package lock;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicTest {

    private static class MyInteger{
        int count=0;
        AtomicInteger acount=new AtomicInteger(0);
        public int  add(){
            return count++;
        }
        public int  atomicAdd(){
            return acount.incrementAndGet();
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public AtomicInteger getAcount() {
            return acount;
        }

        public void setAcount(AtomicInteger acount) {
            this.acount = acount;
        }
    }
    //1、AtomicInteger实现整数的并发计算
    /*
        结果：
        线性不安全的结果：9888
        原子操作的结果：10000
     */
    @Test
    public void atomicIntegerUseTest() throws InterruptedException {
        int count=10;
        MyInteger myInteger = new MyInteger();
        ExecutorService executorService = Executors.newCachedThreadPool();
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for(int i=0;i<10;i++){
            executorService.execute(()->{
                for (int j=0;j<1000;j++){
                    myInteger.add();
                    myInteger.atomicAdd();
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        System.out.println("线性不安全的结果："+myInteger.getCount());
        System.out.println("原子操作的结果："+myInteger.getAcount());
    }

    //    AtomicStampedReference解决ABA问题
    //    AtomicReference的对象CAS原子操作


}
