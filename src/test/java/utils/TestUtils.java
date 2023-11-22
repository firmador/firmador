package utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public class TestUtils {

    public static void deleteDir(String path){
        try {
            FileUtils.deleteDirectory(new File(path));
        } catch (IOException e) {
            throw new RuntimeException("Not possible to delete the directory", e);
        }
    }

    public static void createFile(String path){
        try {
            // it creates all the dirs required and then the file
            File file = new File(path);
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Not possible to create the file", e);
        }
    }

    public static Map<String, String> getModifiableEnvironment()
    {
        try {
            String className = "java.util.Collections$UnmodifiableMap";
            String fieldName = "m";
            Map<String, String> env = System.getenv();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                // since the implementations of ProcessEnvironment are different between OS, the way of getting the editable environment is also different
                className = "java.lang.ProcessEnvironment";
                fieldName = "theCaseInsensitiveEnvironment";
            }
            Class<?> cl = Class.forName(className);
            Field field = cl.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (Map<String, String>) field.get(env);
        } catch (Exception e) {
                throw new RuntimeException("Not possible to get the modifiable environment", e);
            }
        }
}
