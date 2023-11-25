package cr.libre.firmador;

public class MessageUtils {
    public static String t(String msgkey) {
        Settings settings = SettingsManager.getInstance().getAndCreateSettings();
        return settings.bundle.getString(msgkey);
    }

    public static char k(char key) {
        return key;
    }

    public static String html2txt(String text) {
        return text.replaceAll("<[^>]*>", "");
    }
}
