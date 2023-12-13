package utils;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.attribute.*;
import java.util.*;

public class TestUtils {

    public static void deleteDir(String path){
        try {
            File dir = new File(path);
            if(dir.exists()) {
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    // make sure the file has the right permissions for deletion
                    final Process p = Runtime.getRuntime().exec("icacls " + path + " /grant \"" + System.getProperty("user.name") + ":F\" /t /inheritance:r");
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
            dir.setReadOnly();  // for linux and mac this is enough

            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                AclFileAttributeView aclFileAttributesView = Files.getFileAttributeView(dir.toPath(), AclFileAttributeView.class);
                List<AclEntry> acl = new ArrayList<>();
                for (AclEntry aclEntry : aclFileAttributesView.getAcl()) {
                    AclEntry entry = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(aclEntry.principal())
                        .setPermissions(AclEntryPermission.READ_DATA)
                        .setFlags(AclEntryFlag.DIRECTORY_INHERIT, AclEntryFlag.FILE_INHERIT).build();
                    acl.add(entry);
                }
                aclFileAttributesView.setAcl(acl);

                System.out.println("----------");
                System.out.println("path is " + path);
                final Process p = Runtime.getRuntime().exec("icacls " + path);
                p.waitFor();  // wait for it to end before continue with the next line
                BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream(  )));
                String s;
                while ((s = is.readLine()) != null) {
                    System.out.println(s);
                }
                System.out.println("----------");
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
