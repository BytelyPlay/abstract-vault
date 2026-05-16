import lombok.extern.slf4j.Slf4j;
import org.abstractvault.bytelyplay.data.DataSetter;
import org.abstractvault.bytelyplay.enums.DataFormat;
import org.abstractvault.bytelyplay.io.ResettableInputStream;

import java.io.*;
import java.nio.file.Path;

@Slf4j
public class TestingMain {
    public static void main(String[] args) {
        DataSetter dataSetter = new DataSetter.Builder()
                .getterSetter(TestingMain::get, TestingMain::set, "1", String.class)
                .getterSetter(TestingMain::get1, TestingMain::set1, "2", String.class)
                .getterSetter(TestingMain::get2, TestingMain::set2, "3", String.class)
                .build();
        Path jsonFile = Path.of("data.json");
        ByteArrayInputStream input1;

        try {
            dataSetter.save(jsonFile, DataFormat.TEXT_PRETTY_JSON);
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                long startTime = System.currentTimeMillis();
                dataSetter.serialize(out, DataFormat.TEXT_PRETTY_JSON);
                long endTime = System.currentTimeMillis();
                System.out.println(endTime - startTime);

                input1 = new ByteArrayInputStream(out.toByteArray());
            }
            {
                long startTime = System.currentTimeMillis();
                dataSetter.deserialize(input1);
                long endTime = System.currentTimeMillis();
                System.out.println(endTime - startTime);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
