package jvm;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/1/2.
 */

public class MemTest extends ClassLoader {

    //触发java栈的溢出
    //到9100多次就会溢出
    //-Xss 表示每个线程的内存大小，该值越大，那么一个线程允许的栈帧数越大
    //疑问 栈内存的位置，直接存放到物理内存？ 为什么6000个栈，每个栈1mb，但是没有占用6000mb的空间
    //  -Xss1m 每个线程1mb的大小 迭代5677次
    //  -Xss2m 每个线程2mb的大小 迭代11494次
    //  -Xss10m 每个线程10mb的大小 迭代74066次
    @Test
    public void stackOverFlow(){

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        recursion(1);
    }

    private void recursion(int i){
        System.out.println("迭代"+i);
        i++;
        if(i==6000){
            System.out.println("迭代"+i);
        }
        recursion(i);
//        注意，i++ 是先返回i,然后对i加1
//        recursion(i++);

    }


    //触发java堆溢出
    @Test
    public void heapOverFlow(){
        int count=1;
        //方式一：频繁发生GC，防止发生GC，要保持引用的存在
//        while (true){
//            count++;
//            System.out.println("创建"+count);
//            //1个char是2个字节，strs是2MB的大小
//            char[] strs=new char[1024*1024];
//        }

        //方式二：仍然会进行GC，造成栈内存溢出
//        recursionHeapAllocate(1);

        //方式三：通过分配大对象占用内存，防止GC回收
        //默认情况下分配1360MB的内存会出错
//        while (true){
//            char[] strs=new char[count*10*1024*1024];
//            count++;
//            System.out.println("堆内存已使用"+count*20+"MB");
//        }

        //测试4 默认情况下分配1360MB的大对象会发生OOM
//        count=68;    //68时就不能创建了
//        count=67;  //67时可以创建
//        char[] strs=new char[count*10*1024*1024];

        //测试5：最大100MB的堆内存，-Xms50m -Xmx100m，使用60MB时就会出错
//        while (true){
//            count++;
//            char[] strs1=new char[count*10*1024*1024];
//            System.out.println("堆内存已使用"+count*20+"MB");
//        }

        //测试6 最大100MB的堆内存，-Xms100m -Xmx100m -XX:NewSize=60m -XX:MaxNewSize=60m
        // -XX:SurvivorRatio=8 -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC
        // -Xloggc:./log/gc.log
        //此时：eden区 48MB s0 6MB s1 6MB， old区 40MB
        //结果，可以分配54M的空间，不能分配56MB的,疑问，最大空间48MB，如何分配这么大的空间？
        //  PSYoungGen total 55296K
        // -------eden space 49152K  from space 6144K  to   space 6144K
        // ParOldGen   total 40960K, used 0K
        // -------object space 40960K, 0% used
        //Metaspace       used 5429K, capacity 5620K, committed 5888K, reserved 1056768K
        // -------class space    used 629K, capacity 659K, committed 768K, reserved 1048576K
        //极端情况：YoungGen中 eden和survivor的比例会变化
        // PSYoungGen      total 59392K
        // eden space 57344K
        // from space 2048K to   space 2048K


        //测试7 最大100MB的堆内存，-Xms120m -Xmx120m -XX:NewRatio=3
        // -XX:SurvivorRatio=6 -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC
        // -Xloggc:./log/gc.log
        //此时：Young:Old=1:3 eden:suvivor=6:2:2  eden 18MB s0 6MB s1 6MB， old区 90MB,总的young区24MB
        //结论：-XX:SurvivorRatio的比例会改变，最低为58:1:1


        //测试8 默认情况下 eden:s0:s1=6:1:1， youngGen:oldGen=1:2  Metaspace大小默认1G
        //-Xms120m -Xmx120m -XX:MaxMetadataSize=20m -XX:MetadataSize=20m
        try {
//            Thread.sleep(10000);
            while (true){
                count++;
                Thread.sleep(10000);
                char[] strs1=new char[count*1024*1024];
                System.out.println("堆内存已使用"+count*2+"MB");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 问题：
     Maven打包时报错jdk.internal.org.objectweb.asm找不到，但是JDK1.8的rt.jar中确实有此包。
     在Maven中配置bootclasspath解决该问题
     */
    //Metadata 的调优： https://blog.csdn.net/bolg_hero/article/details/78189621
    //测试9: 动态加载类 来撑爆metaspace
    //结论： [Metaspace: 1266964K->1266964K(1593344K)]
    //默认情况下，Metaspace区域的大小会动态调整，不断占用本机内存，不抛出oom
    //当通过-XX:MaxMetaspaceSize=100m ,Metaspace 限制到指定容量，超过该容量时， 会抛出java.lang.OutOfMemoryError: Metaspace
    //Metaspace       used 102180K, capacity 102370K, committed 102400K, reserved 1095680K
    //100m的空间 加载了将近11万个简单类
    @Test
    public void metaspaceOOM(){
        // 类持有
        List<Class<?>> classes = new ArrayList<Class<?>>();
        // 循环1000w次生成1000w个不同的类。
        for (int i = 0; i < 10000000; ++i) {
            ClassWriter cw = new ClassWriter(0);
            // 定义一个类名称为Class{i}，它的访问域为public，父类为java.lang.Object，不实现任何接口
            cw.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, "Class" + i, null,
                    "java/lang/Object", null);
            // 定义构造函数<init>方法
            MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                    "()V", null, null);
            // 第一个指令为加载this
            mw.visitVarInsn(Opcodes.ALOAD, 0);
            // 第二个指令为调用父类Object的构造函数
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object",
                    "<init>", "()V", false);
            // 第三条指令为return
            mw.visitInsn(Opcodes.RETURN);
            mw.visitMaxs(1, 1);
            mw.visitEnd();

            byte[] code = cw.toByteArray();
            // 定义类
            Class<?> exampleClass = this.defineClass("Class" + i, code, 0, code.length);
            classes.add(exampleClass);
            System.out.println("加载类的数量："+i);
        }
    }




    private void recursionHeapAllocate(int i){
        char[] strs=new char[1024*1024];
        System.out.println("堆内存已使用"+i*2+"MB");
        i++;
        recursionHeapAllocate(i);

    }

    //通过多线程耗尽主机内存,线程执行完毕后自己会销毁，因此要保持线程处在存活状态
    //线程数的增加会造成系统主机内存耗尽，系统卡死，但是并不会报OOM异常
    @Test
    public void multiThread(){
        final Object o=new Object();
        int count=1;
        while (true){
            new Thread(){
                public void run() {
                    try {
                        //调用对象的wait方法时，必须先拿到该对象的锁
                        synchronized (o){
                            o.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            System.out.println("已创建"+count+"个线程");
            count++;
        }
    }

    /**
     *
     *
     C:\Users\Administrator>jstat -gc  2720
     S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT     GCT
     1536.0 1536.0  0.0    0.0   37888.0   749.0    81920.0    52224.0   5632.0 5246.5 768.0  613.9      17    0.021   8      0.079    0.100

     FGC 时YGC耗时的8-10倍
     指针压缩空间,64位虚拟机需要比32位多一倍的空间来存储指针因此默认开启了指针压缩,这个空间用来存储压缩后的指针
     */


}
