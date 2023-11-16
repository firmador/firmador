package utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TestUtils {

    public static void deleteDir(String path){
        try {
            FileUtils.deleteDirectory(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createFile(String path){
        try {
            // it creates all the dirs required and then the file
            File file = new File(path);
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
