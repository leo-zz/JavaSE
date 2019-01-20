package jvm;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Administrator on 2019/1/20.
 */
public class MapTest {


    @Test
    public void main1() {
//        Map<String, Integer> map = new LinkedHashMap<String, Integer>(16,0.75f,true);
        Map<String, Integer> map = new HashMap<String, Integer>(4, 0.75f);
        String[] a = {"a", "b", "c", "d", "e", "f"};
        for (int i = 0; i < 5; i++) {

            for (int j=0;j<a.length;j++){
                map.put(a[i]+j,i*j);

            }
            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
            map.forEach((k, v) -> {
            System.out.println("k:" + k + ",v:" + v);
        });
        }
//        map.forEach((k, v) -> {
//            System.out.println("k:" + k + ",v:" + v);
//        });
//
//        map.get("a");
//        map.forEach((k, v) -> {
//            System.out.println("k:" + k + ",v:" + v);
//        });
//
//        map.put("e", 5);
//
//        map.get("a");
//
//        map.forEach((k, v) -> {
//            System.out.println("k:" + k + ",v:" + v);
//        });
    }
}
