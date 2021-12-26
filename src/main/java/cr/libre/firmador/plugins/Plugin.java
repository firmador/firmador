package cr.libre.firmador.plugins;

public interface Plugin {

	public boolean isrunnable = false;

	// Es el primer método que se llama en el plugin.
	public void start();

	// Es el último método que se llama al cerrar la aplicación
	public void stop();

}
