package cr.fran.gui;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;

public class GUIShell implements GUIInterface{
    private static PasswordProtection pin;
	private String documenttosign=null;
	private String documenttosave=null;

	public void setArgs(String[] args) {
		List<String> arguments = new ArrayList<String>();
		for (String params : args) {
			if(!params.startsWith("-")) {
				arguments.add(params);
			}
		}
		if(arguments.size()>1)
			documenttosign = Paths.get(arguments.get(0)).toAbsolutePath().toString();
		
		if(arguments.size()>2)
		documenttosave = Paths.get(arguments.get(1)).toAbsolutePath().toString();
		
	}
	
	private String readFromInput(String message){
		System.out.println(message);
		String plaintext = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			plaintext = br.readLine();
	
		} catch (IOException e) {
			System.err.println("I can't read in stdin");
			System.exit(1);
		}
		return plaintext;
	}

	public String getDocumentToSign() {
		String docpath=readFromInput("Path del documento a firmar: ");
		return Paths.get(docpath).toAbsolutePath().toString();
	}

	public String getPathToSave() {
		String docpath=readFromInput("Path del documento a guardar: ");
		return Paths.get(docpath).toAbsolutePath().toString();
	}

	public PasswordProtection getPin() {
		Console console = System.console(); 
		char[] password = null;
		if(console != null) {
			password = console.readPassword("PIN: ");
		}else{
			System.err.println("System console not present, maybe you are running on IDE, got fallback");
			String docpath=readFromInput("PIN: ");
			password = docpath.toCharArray();

		}
		pin = new PasswordProtection(password);
		return pin;
	}

}
