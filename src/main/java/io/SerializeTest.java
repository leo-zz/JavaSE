package io;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Administrator on 2019/1/27.
 */
public class SerializeTest {

    /*
    如果序列化的所有类存在没有实现Serializable的，会在序列化的过程中抛出异常
        结果：
        序列化前:Persion{name='lee', age=28, mypet=Pet{name='hedgehog', age=15}, skill=Skill{name='看娃', degree=7}}
        序列化后:Persion{name='lee', age=28, mypet=Pet{name='hedgehog', age=15}, skill=Skill{name='看娃', degree=7}}
     */
    @Test
    public void testSerialze() {
        //准备序列化对象
        Skill skill = new Skill("看娃", Skill.SKILLED_DEGREE);
        Pet pet = new Pet("hedgehog", 15);
        Persion lee = new Persion("lee", 28, pet, skill);

        //拿到目标存储文件
        File file = new File("F:\\Java\\testCopy\\testserialize.txt");
        ObjectOutputStream oos = null;
        FileOutputStream fos = null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;

        try {
            //拿到流
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            //将序列化信息写入到文件中，以普通io写入
            oos.writeObject(lee);
            oos.flush();

            //以NIO的方式不能实现序列化
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            Persion leeS = (Persion) ois.readObject();
            System.out.println("序列化前:" + lee);
            System.out.println("序列化后:" + leeS);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) oos.close();
                if (ois != null) ois.close();
                if (fos != null) fos.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    //OuterSerial
    private static class Persion implements Serializable {

        String name;
        int age;
        Pet mypet;
        Skill skill;

        public Persion(String name, int age, Pet mypet, Skill skill) {
            this.name = name;
            this.age = age;
            this.mypet = mypet;
            this.skill = skill;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Pet getMypet() {
            return mypet;
        }

        public void setMypet(Pet mypet) {
            this.mypet = mypet;
        }

        public Skill getSkill() {
            return skill;
        }

        public void setSkill(Skill skill) {
            this.skill = skill;
        }

        @Override
        public String toString() {
            return "Persion{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", mypet=" + mypet +
                    ", skill=" + skill +
                    '}';
        }
    }

    //InnerSerial
    private static class Pet implements Serializable {

        public Pet(String name, int age) {
            this.name = name;
            this.age = age;
        }

        String name;
        int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "Pet{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }

    //InerNonSerial ----如果不实现接口：java.io.NotSerializableException: io.SerializeTest$Skill
    private static class Skill implements Serializable {

        public static int MASTER_DEGREE = 10;
        public static int SKILLED_DEGREE = 7;
        public static int ACQUAINTED_DEGREE = 3;
        public static int DONTKONW_DEGREE = 0;


        public Skill(String name, int degree) {
            this.name = name;
            this.degree = degree;
        }

        String name;
        int degree;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getDegree() {
            return degree;
        }

        public void setDegree(int degree) {
            this.degree = degree;
        }

        @Override
        public String toString() {
            return "Skill{" +
                    "name='" + name + '\'' +
                    ", degree=" + degree +
                    '}';
        }
    }
}


