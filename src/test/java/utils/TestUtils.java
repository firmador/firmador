package utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public class TestUtils {

    public static void deleteDir(String path){
        try {
            File dir = new File(path);
            if(dir.exists()) {
                FileUtils.forceDelete(dir);
            }
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

    public static void createDirectoryWithNoAccess(String path){
        File dir = new File(path);
        dir.mkdirs();
        dir.setReadOnly();
    }

    public static Map<String, String> getModifiableEnvironment()
    {
        try {
            Map<String, String> env = System.getenv();
            String className = env.getClass().getName();
            String fieldName = "m";
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
