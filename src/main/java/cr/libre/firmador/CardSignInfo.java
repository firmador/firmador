package cr.libre.firmador;

import java.security.KeyStore.PasswordProtection;

import org.slf4j.LoggerFactory;

import cr.libre.firmador.gui.BaseSwing;

public class CardSignInfo {
	public static int PKCS11TYPE=1;
	public static int PKCS12TYPE=2;
	public static int ONLYPIN=3;
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CardSignInfo.class);
	private String identification;
	private String firstName;
	private String lastName;
	private String expires;
	private String certSerialNumber;
	// On pkcs12 use tokenSerialNumber to store pkcs12 file path 
	private String tokenSerialNumber;
	private long slotID;
	private PasswordProtection pin;
	private int cardType;
	

	public CardSignInfo(int cardType, String identification, String firstName, String lastName, String expires,
			String certSerialNumber, String tokenSerialNumber, long slotID) {
		super();
		this.cardType=cardType;
		this.identification = identification;
		this.firstName = firstName;
		this.lastName = lastName;
		this.expires = expires;
		this.certSerialNumber = certSerialNumber;
		this.tokenSerialNumber = tokenSerialNumber;
		this.slotID=slotID;
	}
	
	public CardSignInfo(int cardType, String path, String identification) {
		this.cardType = cardType;
		this.tokenSerialNumber=path;
		this.identification = identification;		
	}
	
	public CardSignInfo(String pin) {
		this.setPin(pin);
		this.cardType=ONLYPIN;
		this.identification="Pin card";
	}
	
	public CardSignInfo(char[] password) {
		this.setPin(password);
		this.cardType=ONLYPIN;
	}

	public int getCardType() {
		return cardType;
	}

	public void setCardType(int cardType) {
		this.cardType = cardType;
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
		
		if(this.cardType == PKCS11TYPE)
			return firstName + " " + lastName + " (" + identification + ") (Expira: " + expires+ ")";
	//+ this.certSerialNumber+ " [Token serial number: " + this.tokenSerialNumber + "] (Expires: " + expires+ ")";
		return this.identification;
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

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	

}
