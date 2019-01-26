package jvm.gc;

import org.junit.Test;

public class GCTest {

   static FinalObject ref=null;

    //测试垃圾回收，
    // jvm参数为： -XX:+PrintGCDetails
    @Test
    public void memLeakTest(){
        //1个char2个byte，char1占用40M的大小
        char[] char1 = new char[20 * 1024 * 1024];
        char1=null;
        System.gc();
        System.out.println("测试垃圾回收");
    }



    //对象的finalize()
    private static class FinalObject{
        //1个char2个byte，char1占用40M的大小
        char[] char1 = new char[20 * 1024 * 1024];
        public FinalObject() {
            System.out.println("执行了objcet的construct方法"+" hash信息："+this);
        }

        //由GC收集器回收该对象时执行,只会执行一次。
        @Override
        protected void finalize() throws Throwable {
            System.out.println("执行了objcet的finalize方法，hash信息："+this);
            //在销毁的过程中重新引用
            ref=this;
            System.out.println("在finalize()中进行一次自救");
            super.finalize();
        }
    }
    /*
    jvm参数 -XX:+PrintGCDetails
     执行结果：
        执行了objcet的construct方法 hash信息：jvm.gc.GCTest$FinalObject@32a1bec0
        创建对象完成 hash信息：jvm.gc.GCTest$FinalObject@32a1bec0
        自救前，ref:null
        [GC (System.gc()) [PSYoungGen: 50859K->1432K(95744K)] 50859K->42400K(314368K), 0.0330124 secs] [Times: user=0.03 sys=0.00, real=0.03 secs]
        [Full GC (System.gc()) [PSYoungGen: 1432K->0K(95744K)] [ParOldGen: 40968K->42284K(218624K)] 42400K->42284K(314368K), [Metaspace: 5198K->5198K(1056768K)], 0.0169083 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]
        执行了objcet的finalize方法，hash信息：jvm.gc.GCTest$FinalObject@32a1bec0
        在finalize()中进行一次自救
        *******************自救后，ref:jvm.gc.GCTest$FinalObject@32a1bec0
        [GC (System.gc()) [PSYoungGen: 3217K->32K(95744K)] 45501K->42316K(314368K), 0.0009434 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
        [Full GC (System.gc()) [PSYoungGen: 32K->0K(95744K)] [ParOldGen: 42284K->965K(218624K)] 42316K->965K(314368K), [Metaspace: 5202K->5202K(1056768K)], 0.0116544 secs] [Times: user=0.03 sys=0.00, real=0.01 secs]
        第二次收集该对象时，不会再自救：ref:null
     */
    /*  对于GC信息的解读
                GC原因                  年轻代GC前后的大小        Java堆GC前后的大小        GC耗时
        [GC (System.gc()) [PSYoungGen: 50859K->1432K(95744K)] 50859K->42400K(314368K), 0.0330124 secs] [Times: user=0.03 sys=0.00, real=0.03 secs]
        可以看到char1从eden区直接进入old区，由于char1的大小大于survivor区。YGC后eden区清空。
                GC原因                  年轻代GC前后的大小                老年代GC前后大小               Java堆GC前后的大小            MetaspaceGC前后大小                    GC耗时
        [Full GC (System.gc()) [PSYoungGen: 1432K->0K(95744K)] [ParOldGen: 40968K->42284K(218624K)] 42400K->42284K(314368K), [Metaspace: 5198K->5198K(1056768K)], 0.0169083 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]
        其中，[Times: user=0.01 sys=0.00, real=0.02 secs] 提供cpu使用及时间消耗，user是用户模式垃圾收集消耗的cpu时间即用户态cpu时间，sys是消耗系统态cpu时间,real是指垃圾收集器消耗的实际时间。
     */
    @Test
    public void testObjectFinalize() throws InterruptedException {
        FinalObject object = new FinalObject();
        System.out.println("创建对象完成 hash信息："+object);
        object=null;
        System.out.println("自救前，ref:"+ref);
        System.gc();
        Thread.sleep(1000);
        System.out.println("*******************自救后，ref:"+ref);
        ref=null;
        System.gc();
        System.out.println("第二次收集该对象时，不会再自救：ref:"+ref);

    }
}
