package cr.libre.firmador;

public class Settings {
	public boolean withoutvisiblesign=false;
	public boolean uselta=false;
	public boolean overwritesourcefile=false;
	public String reason="";
	public String place="";
	public String contact="";
	public String dateformat="dd/MM/yyyy hh:mm:ss a";
	public String defaultsignmessage="Esta representación visual no es fuente \nde confianza. Valide siempre la firma."; 
	public Integer signwith=133;
	public Integer signheight=33;
	public Integer fontsize=7;
	public String extrapkcs11Lib=null;
	public Integer signx=198;
	public Integer signy=0;
	
	public Settings() {}
	
	public String getDefaultSignMessage() {
		return this.defaultsignmessage;
	}
	
	public String getDateFormat() {
		return this.dateformat;
	}
}
