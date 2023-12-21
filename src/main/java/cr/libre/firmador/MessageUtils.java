package cr.libre.firmador;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class MessageUtils {
    public static String t(String msgkey) {
        Settings settings = SettingsManager.getInstance().getAndCreateSettings();
        Locale locale = new Locale(settings.language, settings.country);
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        return bundle.getString(msgkey);
    }

    public String getKey(String variable_name){
        Map<String, String> keyByVariableName = new HashMap<String, String>() {
            {
                put("defaultSignMessage", "configpanel_default_sign_message");
            }
        };
        return keyByVariableName.get(variable_name);

    }

    public static char k(char key) {
        return key;
    }

    public static String html2txt(String text) {
        return text.replaceAll("<[^>]*>", "");
    }
}
