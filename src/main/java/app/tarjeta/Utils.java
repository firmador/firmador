package app.tarjeta;

public class Utils {

    public static String getPKCS11Lib() {
        String pkcs11lib = "";
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("mac")) {
            pkcs11lib = "/Library/Application Support/Athena/libASEP11.dylib";
        } else if (osName.contains("linux")) {
            pkcs11lib = "/usr/lib/x64-athena/libASEP11.so";
        } else if (osName.contains("windows")) {
            pkcs11lib = System.getenv("SystemRoot")
                + "\\System32\\asepkcs.dll";
        }

        return pkcs11lib;
    }

}
