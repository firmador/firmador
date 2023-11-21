package utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    public static Map<String, String> getModifiableEnvironment(){
        try {
            Class<?> pe = Class.forName("java.lang.ProcessEnvironment");
            Method getenv = pe.getDeclaredMethod("getenv", String.class);
            getenv.setAccessible(true);
            Field props = pe.getDeclaredField("theCaseInsensitiveEnvironment");
            props.setAccessible(true);
            return (Map<String, String>) props.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Not possible to get the modifiable environment", e);
        }
    }
}
