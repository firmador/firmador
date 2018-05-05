package cr.fran.gui;

import java.security.KeyStore.PasswordProtection;

public interface GUIInterface {

	void setArgs(String[] args);
	String getDocumentToSign();
	String getPathToSave();
	PasswordProtection getPin();
	
}
