package CollectionTest;

import org.junit.Test;

import java.util.*;

/**
 * Created by Administrator on 2019/1/20.
 */
public class MapTest {


    /**
     * LinkedHashMap LRU
     */
    @Test
    public void testLinkedHashMap() {
        Map<String, Integer> map = new LinkedHashMap<String, Integer>(16, 0.75f, true);
        /**
         *  lambda 和 匿名类 调用外边的局部变量 要求局部变量是final 的
         *  参考 https://stackoverflow.com/questions/34865383/variable-used-in-lambda-expression-should-be-final-or-effectively-final
         */
        map.forEach((k, v) -> {
            System.out.println("k:" + k + ",v:" + v);
        });

        map.get("a");
        map.forEach((k, v) -> {
            System.out.println("k:" + k + ",v:" + v);
        });

        map.put("e", 5);

        map.get("a");

        map.forEach((k, v) -> {
            System.out.println("k:" + k + ",v:" + v);
        });
    }

    /**
     * 无序性的体现：插入顺序与遍历顺序不同，随着元素的插入，遍历的顺序会改变
     * 而linkedHashMap的遍历是有序的。
     */
    @Test
    public void testHashMap() {
//        Map<String, Integer> map = new HashMap<String, Integer>();
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        String[] a = {"a", "b", "c", "d", "e", "f"};
        for (int i = 0; i < 5; i++) {

            for (int j = 0; j < a.length; j++) {
                map.put(a[i] + j, i * j);

            }
            System.out.println(i + "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
            map.forEach((k, v) -> {
                System.out.println("k:" + k + ",v:" + v);
            });
        }
    }

    @Test
    public void tesTreeMap() {
        TreeMap<Beauty, String> beautyMap = new TreeMap<>();
        Beauty zs1 = new Beauty(14, 18, 24, "zs1");
        Beauty zs2 = new Beauty(14, 24, 18, "zs2");
        Beauty zs21 = new Beauty(14, 23, 18, "zs2-1");
        Beauty zs3 = new Beauty(24, 18, 14, "zs3");
        beautyMap.put(zs1, "zs1");
        beautyMap.put(zs2, "zs2");
        beautyMap.put(zs21, "zs21");
        beautyMap.put(zs3, "zs3");

        /**
         * zs1 与 zs2 得分相同，因此TreeMap会认为这两个key相同，默认按照升序排列。
         */
        beautyMap.forEach((k, v) -> {
            System.out.println("k:" + k + ",v:" + v);
        });
    }

    static class Beauty implements Comparable {

        int age;
        int legLength;
        int appearence;
        String name;

        public Beauty(int age, int legLength, int appearence, String name) {
            this.age = age;
            this.legLength = legLength;
            this.appearence = appearence;
            this.name = name;
        }

        /**
         * 返回负值表示小。
         *
         * @param o
         * @return
         */
        @Override
        public int compareTo(Object o) {
            int result = 0;
            Beauty beauty = (Beauty) o;
            double bs = score(beauty);
            double as = score(this);
            if (as - bs > 0) {
                result = 1;
            } else if (as - bs < 0) {
                result = -1;
            }
            return result;
        }

        private double score(Beauty b) {
            return b.age * 0.3 + b.legLength * 0.4 + b.appearence * 0.4;

        }
    }
}
