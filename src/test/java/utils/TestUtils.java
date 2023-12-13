package utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Map;

public class TestUtils {

    public static void deleteDir(String path){
        try {
            File dir = new File(path);
            if(dir.exists()) {
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    // make sure the file has the permissions for deletion
                    System.out.println("icacls " + path + " /grant \"" + System.getProperty("user.name") + ":(OI)(CI)F\" /t /inheritance:r");
                    final Process p = Runtime.getRuntime().exec("icacls " + path + " /grant \"" + System.getProperty("user.name") + ":(OI)(CI)F\" /t /inheritance:r");
                    p.waitFor();  // wait for it to end before continue with the next line
                }
                FileUtils.forceDelete(dir);
            }
        } catch (Exception e) {
            throw new RuntimeException("Not possible to delete the directory " + path, e);
        }
    }

    public static void createFile(String path){
        try {
            // it creates all the dirs required and then the file
            File file = new File(path);
            Files.createDirectories(file.getParentFile().toPath());
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Not possible to create the file " + path, e);
        }
    }

    public static void createDirectoryWithNoAccess(String path){
        try {
            File dir = new File(path);
            Files.createDirectories(dir.toPath());
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                final Process p = Runtime.getRuntime().exec("icacls " + path + " /deny \"" + System.getProperty("user.name") + ":(OI)(CI)F\" /t /inheritance:r");
                p.waitFor();  // wait for it to end before continue with the next line
            }else{
                dir.setReadOnly();
            }
        } catch (Exception e) {
            throw new RuntimeException("Not possible create a directory with no access " + path, e);
        }
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
