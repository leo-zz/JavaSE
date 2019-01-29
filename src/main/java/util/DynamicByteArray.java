package util;

import java.util.Arrays;

/**
 * @Author: leo-zz
 * @Date: 2019/1/29$ 10:34$
 */
public class DynamicByteArray {
    private transient byte[] byteData;
    private static int INITIAL_CAPACITY = 1024;
    private int size = 0;
    private int capacity = 0;


    public DynamicByteArray() {
        byteData = new byte[INITIAL_CAPACITY];
        capacity = INITIAL_CAPACITY;
    }

    public void add(byte[] bytes, int src, int length) {
        int newCap = size + length;
        if ((size + length) > capacity) {
            //如果添加数组后的长度超过capacity，则会进行扩容
            resize(newCap);
        }
        //将数组指定取件范围的字节添加到数组中
        for (int i=src;i<src+length;i++) {
            byteData[size++]=bytes[i];
        }
    }

    private void resize(int newCap) {
        if(newCap<0)throw new RuntimeException("扩展后容量超过最大值");
        int cap=capacity<<1;
        while (cap < newCap) {
            cap=cap<<1;
        }
        if(cap<0&newCap<=Integer.MAX_VALUE){
            cap=Integer.MAX_VALUE;
            System.out.println("扩展后容量超过最大值");
        }
        byte[] newElementData = new byte[cap];
        System.arraycopy(byteData,0,newElementData,0,size);
        byteData =newElementData;
    }

    //清除数组的内容
    public void clear(){
        size=0;
        capacity = INITIAL_CAPACITY;
        byteData =new byte[INITIAL_CAPACITY];
    }

    public byte[] getBytes(){
        return Arrays.copyOf(byteData,size);
    }

}
