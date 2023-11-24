package cr.libre.firmador;

public class MessageUtils {
    public static String t(String msgkey) {
        Settings settings = SettingsManager.getInstance().getAndCreateSettings();
        return settings.bundle.getString(msgkey);
    }
}
