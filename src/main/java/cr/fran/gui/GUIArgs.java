package cr.fran.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;

public class GUIArgs implements GUIInterface {

    private String documenttosign;
    private String documenttosave;
    private static PasswordProtection pin;

    public void setArgs(String[] args) {
        List<String> arguments = new ArrayList<String>();

        for (String params : args) {
            if (!params.startsWith("-")) {
                arguments.add(params);
            }
        }
        documenttosign = Paths.get(arguments.get(0)).toAbsolutePath()
            .toString();
        documenttosave = Paths.get(arguments.get(1)).toAbsolutePath()
            .toString();
    }

    public String getDocumentToSign() {
        return documenttosign;
    }

    public String getPathToSave() {
        return documenttosave;
    }

    public PasswordProtection getPin() {
        String pintext = null;
        BufferedReader br = new BufferedReader(
            new InputStreamReader(System.in));

        try {
            pintext = br.readLine();
        } catch (IOException e) {
            System.err.println("PIN not Found");
            System.exit(1);
        }
        pin = new PasswordProtection(pintext.toCharArray());

        return pin;
    }

}
