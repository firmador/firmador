package cr.libre.firmador;

import java.security.KeyStore.PasswordProtection;

import org.slf4j.LoggerFactory;

import cr.libre.firmador.gui.BaseSwing;

public class CardSignInfo {
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CardSignInfo.class);
	private String identification;
	private String firstName;
	private String lastName;
	private String expires;
	private String certSerialNumber;
	private String tokenSerialNumber;
	private long slotID;
	private PasswordProtection pin;
	
	
	public CardSignInfo(String identification, String firstName, String lastName, String expires,
			String certSerialNumber, String tokenSerialNumber, long slotID) {
		super();
		this.identification = identification;
		this.firstName = firstName;
		this.lastName = lastName;
		this.expires = expires;
		this.certSerialNumber = certSerialNumber;
		this.tokenSerialNumber = tokenSerialNumber;
		this.slotID=slotID;
	}
	
	public CardSignInfo(String pin) {
		this.setPin(pin);
	}
	
	public CardSignInfo(char[] password) {
		this.setPin(password);
	}

	public String getTokenSerialNumber() {
		return tokenSerialNumber;
	}

	public void setTokenSerialNumber(String tokenSerialNumber) {
		this.tokenSerialNumber = tokenSerialNumber;
	}

	public long getSlotID() {
		return slotID;
	}

	public void setSlotID(long slotID) {
		this.slotID = slotID;
	}

	public void setPin(PasswordProtection pin) {
		this.pin = pin;
	}
	public void setPin(String pin) {
		this.pin = new PasswordProtection(pin.toCharArray());
	}
	public void setPin(char[] pin) {
		this.pin = new PasswordProtection(pin);
	}
	
	public String getDisplayInfo() {
        return firstName + " " + lastName + " (" + identification + ") " + this.certSerialNumber+ " [Token serial number: " + this.tokenSerialNumber + "] (Expires: " + expires+ ")";

	}
	
	public PasswordProtection getPin() {
		return this.pin;
	}
	
	public void destroyPin() {
		try {
            pin.destroy();
        } catch (Exception e) {
        	LOG.error("Error destruyendo el pin", e);
        }
	}
	
	public boolean isValid() {
		return pin.getPassword() != null && pin.getPassword().length != 0;
	}
	

}
