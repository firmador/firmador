package cr.fran.gui;

public class GUISelector {
	
	public String getGUIClassName(String[] args){
		String dev = "awt";
		
		for (String params : args) {
			if(params.startsWith("-d")) {
				dev = params.substring(2);
			}
		}
		return dev;
	}

	public GUIInterface getInterface(String[] args){
		String name = this.getGUIClassName(args);
		return this.getInterface(name);
	}
	public GUIInterface getInterface(String name){
		GUIInterface gui=null;
		
		if(name.equals("args")){
			gui=new GUIArgs();
			
		}else if(name.equals("shell")){
			gui=new GUIShell();
		}else{
			gui=new GUIAwt();
		}
	  return gui;
	}
}
