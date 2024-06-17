package cr.libre.firmador;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class MessageUtils {
    public static String t(String msgkey) {
        Settings settings = SettingsManager.getInstance().getAndCreateSettings();
        Locale locale = new Locale.Builder().setLanguage(settings.language).setRegion(settings.country).build();
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        return bundle.getString(msgkey);
    }

    public static char k(char key) {
        return key;
    }

    public static String html2txt(String text) {
        return text.replaceAll("<[^>]*>", "");
    }
}
