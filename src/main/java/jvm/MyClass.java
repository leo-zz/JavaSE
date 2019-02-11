package jvm;

/**
 * @Author: leo-zz
 * @Date: 2019/2/11 12:03
 */
public class MyClass {


    public MyClass() {
        System.out.println(Thread.currentThread() +"实例化由"+this.getClass().getClassLoader()+"加载的类"+this.getClass()+"的对象");
    }

    public String dispalyInfo() {
        String s = "我是" + this.getClass() + "类的实例，我是由加载器" + getClass().getClassLoader() + "加载的。";
        System.out.println(s);
        return s;
    }
}
