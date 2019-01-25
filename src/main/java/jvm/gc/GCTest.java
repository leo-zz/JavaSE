package jvm.gc;

import org.junit.Test;

public class GCTest {

    //测试垃圾回收，
    // jvm参数为： -XX:+PrintGCDetails
    @Test
    public void memLeakTest(){
        char[] char1 = new char[20 * 1024 * 1024];
        char1=null;
        System.gc();
        System.out.println("测试垃圾回收");
    }
}
