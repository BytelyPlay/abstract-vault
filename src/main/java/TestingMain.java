import org.abstractvault.bytelyplay.data.DataSetter;
import org.abstractvault.bytelyplay.enums.DataFormat;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestingMain {
    public static void main(String[] args) {
        DataSetter dataSetter = new DataSetter.Builder()
                .getterSetter(TestingMain::get, TestingMain::set, "setReal", String.class)
                .getterSetter(TestingMain::get1, TestingMain::set1, "setReal1", String.class)
                .getterSetter(TestingMain::get2, TestingMain::set2, "setReal2", String.class)
                .build();
        Path jsonFile = Path.of("data.json");
        ByteArrayInputStream input1;

        dataSetter.save(jsonFile, DataFormat.TEXT_PRETTY_JSON);
        {
            long startTime = System.currentTimeMillis();
            byte[] end = dataSetter.serialize(DataFormat.TEXT_PRETTY_JSON);
            long endTime = System.currentTimeMillis();
            System.out.println(endTime - startTime);
            input1 = new ByteArrayInputStream(end);
        }
        {
            long startTime = System.currentTimeMillis();
            dataSetter.load(input1);
            long endTime = System.currentTimeMillis();
            System.out.println(endTime - startTime);
        }
    }
    public static String get() {
        System.out.println("get()");
        return "true";
    }
    public static void set(String bool) {
        System.out.println("set() " + bool);
    }
    public static String get1() {
        System.out.println("get1()");
        return "asdqwrqrw";
    }
    public static void set1(String bool) {
        System.out.println("set1() " + bool);
    }
    public static String get2() {
        System.out.println("get2()");
        return "";
    }
    public static void set2(String bool) {
        System.out.println("set2() " + bool);
    }
}
