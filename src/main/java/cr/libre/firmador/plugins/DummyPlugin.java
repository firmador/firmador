package cr.libre.firmador.plugins;

public class DummyPlugin implements Plugin {

	public void start() {
		System.out.println("Stating DummyPlugin");
	}

	public void stop() {
		System.out.println("Stop DummyPlugin");
	}
}
