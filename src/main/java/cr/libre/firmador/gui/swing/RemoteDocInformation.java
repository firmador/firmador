package cr.libre.firmador.gui.swing;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class RemoteDocInformation {
	String name;
	ByteArrayOutputStream data;
	InputStream inputdata;
	int status;

	public RemoteDocInformation(String name, ByteArrayOutputStream data, int status) {
		super();
		this.name = name;
		this.data = data;
		this.status = status;

	}

	public InputStream getInputdata() {
		return inputdata;
	}

	public void setInputdata(InputStream inputdata) {
		this.inputdata = inputdata;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ByteArrayOutputStream getData() {
		return data;
	}

	public void setData(ByteArrayOutputStream data) {
		this.data = data;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
