package cr.libre.firmador;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Exception;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKA_CLASS;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKA_ID;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKA_VALUE;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKF_OS_LOCKING_OK;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKF_SERIAL_SESSION;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKF_TOKEN_PRESENT;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKO_CERTIFICATE;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKO_PRIVATE_KEY;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKU_USER;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKS_RO_USER_FUNCTIONS;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKS_RW_USER_FUNCTIONS;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKA_PRIVATE;  
import static sun.security.pkcs11.wrapper.PKCS11Constants.*;  

import sun.security.pkcs11.wrapper.CK_SESSION_INFO;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.io.ByteArrayInputStream;
import java.lang.invoke.MethodHandles;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_INFO;
import sun.security.pkcs11.wrapper.CK_SLOT_INFO;
import sun.security.pkcs11.wrapper.CK_TOKEN_INFO;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;

import java.security.spec.PKCS8EncodedKeySpec;


import java.security.Provider;
import java.security.Security;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PKCS11Manager {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String PKCS11_PROVIDER_NAME = "SunPKCS11";
    protected Settings settings;
    KeyStore keystore = null;
    Long lastSlotID = null;
    private String lib;
    Map<BigInteger, Long> slot_by_cert = new HashMap<>();
    Map<BigInteger, char[]> token_by_cert = new HashMap<>();
    Map<String, X509Certificate> certificateMap = new HashMap<>();
    
    private Provider provider;
    
    public PKCS11Manager() {
        settings = SettingsManager.getInstance().getAndCreateSettings();
        //settings.addListener(this);
    }
    
    public Provider getProvider() {
		return provider;
	}

	public void updateLib() {
        lib = CRSigner.getPkcs11Lib();
    }
    private void clean() {
        slot_by_cert.clear();
        token_by_cert.clear();    	
    }
	public PKCS11 initialize() throws Throwable {
		this.updateLib();
        String functionList = "C_GetFunctionList";
        CK_C_INITIALIZE_ARGS pInitArgs = new CK_C_INITIALIZE_ARGS();
        PKCS11 pkcs11;
        try {
            pInitArgs.flags = CKF_OS_LOCKING_OK;
            pkcs11 = PKCS11.getInstance(lib, functionList, pInitArgs, false);
        } catch (PKCS11Exception e) {
            LOG.debug("C_GetFunctionList didn't like CKF_OS_LOCKING_OK on pInitArgs", e);
            pInitArgs.flags = 0;
            pkcs11 = PKCS11.getInstance(lib, functionList, pInitArgs, false);
        }
        return pkcs11;
	}
	public List<X509Certificate> getCertificates() throws Throwable {	
			List<X509Certificate> certlist = new ArrayList<X509Certificate>();
			PKCS11 pkcs11 = this.initialize();
			char[] keyIdentifier;
			this.clean();
			CK_INFO info = pkcs11.C_GetInfo();
	        LOG.info("Interface: " + new String(info.libraryDescription).trim());
	        Boolean tokenPresent = true;
	        for (long slotID : pkcs11.C_GetSlotList(tokenPresent)) {
	            CK_SLOT_INFO slotInfo = pkcs11.C_GetSlotInfo(slotID);
	            LOG.debug("Slot " + slotID + ": " + new String(slotInfo.slotDescription).trim());
	            if ((slotInfo.flags & CKF_TOKEN_PRESENT) != 0) { // Not required if tokenPresent = true, condition could be removed if true, it's just for testing empty slot enumeration
	                try { // FIXME: slotID may be reused after switching card! try CK_SESSION_INFO sessionInfo = pkcs11.C_GetSessionInfo(hSession); and catch PCKCS11Exception meaning invalid session instead!
	                    CK_TOKEN_INFO tokenInfo = pkcs11.C_GetTokenInfo(slotID);
	                    LOG.info("Token: " + new String(tokenInfo.label).trim() + " (" + new String(tokenInfo.serialNumber).trim() + ")");
	                    CK_ATTRIBUTE[] pTemplate = { new CK_ATTRIBUTE(CKA_CLASS, CKO_CERTIFICATE) };
	                    long ulMaxObjectCount = 32;
	                    long hSession = pkcs11.C_OpenSession(slotID, CKF_SERIAL_SESSION, null, null); // FIXME: verify slot session just after getting PIN but just before login
	                    pkcs11.C_FindObjectsInit(hSession, pTemplate);
	                    long[] phObject = pkcs11.C_FindObjects(hSession, ulMaxObjectCount);
	                    pkcs11.C_FindObjectsFinal(hSession);
	                    for (long object : phObject) {
	                        CK_ATTRIBUTE[] pTemplate2 = { new CK_ATTRIBUTE(CKA_VALUE), new CK_ATTRIBUTE(CKA_LABEL) }; // if you add more attributes, update the iterator jump
	                        pkcs11.C_GetAttributeValue(hSession, object, pTemplate2);
	                        for (int i = 0; i < pTemplate2.length; i = i + 2) { // iterator jump value to read just certificates at pTemplate[0], pTemplate[2]... FIXME: better use pValue filtering
	                        	keyIdentifier = pTemplate2[i + 1].getCharArray(); 
	                            X509Certificate certificate = (X509Certificate)CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream((byte[])pTemplate2[i].pValue));
	                            certlist.add(certificate); 
	                            slot_by_cert.put(certificate.getSerialNumber(), slotID);
	                            token_by_cert.put(certificate.getSerialNumber(), keyIdentifier);
	                            LOG.debug("Public/Private key pair identifier: " + keyIdentifier); // After logging in with PIN, find the matching private key pValue. NOTE: Old certificates didn't use "LlaveDeFirma" id/label.
	                            // FIXME: Don't assume there's a single valid certificate per token (Persona Jurídica keystores might contain more than 1 usable certificate per token as they are handmade)
	                        }
	                    }
	                    pkcs11.C_CloseSession(hSession);
	                } catch (PKCS11Exception e) {
	                    if (e.getLocalizedMessage().equals("CKR_TOKEN_NOT_RECOGNIZED")) {
	                        LOG.info("Slot reports token is present but not recognized by the cryptoki library", e);
	                    } else throw e;
	                }
	            } else LOG.info("No token present in this slot"); // Condition could be removed

	        }
	        return certlist;
	}
	public Long getSlotByCert(X509Certificate cert) {
		return slot_by_cert.get(cert.getSerialNumber());
	}
	public char[] getTokenByCert(X509Certificate cert) {
		return token_by_cert.get(cert.getSerialNumber());
	}
	public static String generateRandomString() {
        int length = 6;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }
	public static Path createTempFileWithContent(String content) throws IOException {
        // Crear un archivo temporal
        Path tempFile = Files.createTempFile("temp", ".cfg");

        // Escribir el contenido en el archivo temporal
        Files.write(tempFile, content.getBytes(), StandardOpenOption.WRITE);

        // Obtener la ruta del archivo temporal
       // String tempFilePath = tempFile.toAbsolutePath().toString();
        return tempFile;
    }

	private KeyStore getKeyStoreFromPKCS11(String pkcs11Config, PasswordProtection password) throws Exception {
	    // Crear una instancia de SunPKCS11 con la configuración proporcionada
        provider = Security.getProvider(PKCS11_PROVIDER_NAME);
	    
        Path configpath =createTempFileWithContent(pkcs11Config);
        
        provider=provider.configure(configpath.toAbsolutePath().toString());
	    	
	    // Agregar el proveedor a la lista de proveedores de seguridad
	    Security.addProvider(provider);
	
	    // Obtener el KeyStore del proveedor PKCS11
	    KeyStore keyStore = KeyStore.getInstance("PKCS11", provider);
	    keyStore.load(null, password.getPassword()); 

	    return keyStore;
	}
	
	public KeyStore getKeyStore(Long slotID, PasswordProtection password) throws Throwable {
		if(keystore == null || lastSlotID != slotID) {
			this.updateLib();
			String pkcs11Config = "name = SunPKCS11-"+generateRandomString()+"\nlibrary = "+lib
			  + "\nslot = " + slotID;  
			keystore= getKeyStoreFromPKCS11(pkcs11Config, password);
			lastSlotID=slotID;
		}
		return keystore;
	}
	
	public Key getPrivateKey(String token, Long slotID, PasswordProtection password) throws Throwable {
		Key privateKey = null;
		KeyStore keystore=getKeyStore(slotID, password);
		privateKey=keystore.getKey(token, password.getPassword());		
		return privateKey;
	}
	
	public X509Certificate getCertificate(String token, Long slotID, PasswordProtection password) throws Throwable {
		X509Certificate cert=null;
		KeyStore keystore=getKeyStore(slotID, password);
		cert = (X509Certificate)keystore.getCertificate(token);
		return cert;
		
	}
	private X509Certificate parseX509(String dataname) throws CertificateException  {
		InputStream data = this.getClass().getClassLoader().getResourceAsStream(dataname);
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		return (X509Certificate) certificateFactory.generateCertificate(data);
	}
	public List<X509Certificate> getCertificateChain(boolean forvalidate) throws CertificateException{
		List<X509Certificate> certlist = new ArrayList<X509Certificate>();
		certlist.add(parseX509("certs/CA RAIZ NACIONAL - COSTA RICA v2.crt"));
		certlist.add(parseX509("certs/CA RAIZ NACIONAL COSTA RICA.cer"));
		certlist.add(parseX509("certs/CA POLITICA PERSONA FISICA - COSTA RICA v2.crt"));
		if(forvalidate)certlist.add(parseX509("certs/CA POLITICA PERSONA JURIDICA - COSTA RICA v2.crt"));
		if(forvalidate)certlist.add(parseX509("certs/CA POLITICA SELLADO DE TIEMPO - COSTA RICA v2.crt"));
		certlist.add(parseX509("certs/CA SINPE - PERSONA FISICA v2.cer"));
		certlist.add(parseX509("certs/CA SINPE - PERSONA FISICA v2(1).crt"));
		certlist.add(parseX509("certs/CA SINPE - PERSONA FISICA v2(2).crt"));
		if(forvalidate)certlist.add(parseX509("certs/CA SINPE - PERSONA JURIDICA v2.cer"));
		if(forvalidate)certlist.add(parseX509("certs/CA SINPE - PERSONA JURIDICA v2(1).crt"));
		if(forvalidate)certlist.add(parseX509("certs/CA SINPE - PERSONA JURIDICA v2(2).crt"));
		if(forvalidate)certlist.add(parseX509("certs/TSA SINPE v2.cer"));
		return certlist;
	}
	
    public List<X509Certificate> getSignCertificates() throws CertificateException {
        List<X509Certificate> certlist = new ArrayList<X509Certificate>();
        certlist.add(parseX509("certs/CA RAIZ NACIONAL - COSTA RICA v2.crt"));//
        certlist.add(parseX509("certs/CA RAIZ NACIONAL COSTA RICA.cer"));
        certlist.add(parseX509("certs/CA POLITICA PERSONA FISICA - COSTA RICA v2.crt"));//
        certlist.add(parseX509("certs/CA SINPE - PERSONA FISICA v2.cer"));
        certlist.add(parseX509("certs/CA SINPE - PERSONA FISICA v2(1).crt")); //
        certlist.add(parseX509("certs/CA SINPE - PERSONA FISICA v2(2).crt"));
        return certlist;
    }

	public List<X509Certificate> getCertificateChainTSA() throws CertificateException{
		List<X509Certificate> certlist = new ArrayList<X509Certificate>();
		certlist.add(parseX509("certs/CA RAIZ NACIONAL - COSTA RICA v2.crt"));
        // certlist.add(parseX509("certs/CA RAIZ NACIONAL COSTA RICA.cer"));
		certlist.add(parseX509("certs/CA POLITICA SELLADO DE TIEMPO - COSTA RICA v2.crt"));
		certlist.add(parseX509("certs/TSA SINPE v2.cer"));
		
		return certlist;
	}

    private void addCertificateToMap(X509Certificate certificate) {

        certificateMap.put(extractCommonName(certificate.getSubjectX500Principal().getName()), certificate);
    }

    private void loadCertificateCache() throws Throwable {

        if (certificateMap.isEmpty()) {
            addCertificateToMap(parseX509("certs/CA RAIZ NACIONAL - COSTA RICA v2.crt"));
            addCertificateToMap(parseX509("certs/CA RAIZ NACIONAL COSTA RICA.cer"));
            addCertificateToMap(parseX509("certs/CA POLITICA PERSONA FISICA - COSTA RICA v2.crt"));
            addCertificateToMap(parseX509("certs/CA POLITICA PERSONA JURIDICA - COSTA RICA v2.crt"));
            addCertificateToMap(parseX509("certs/CA POLITICA SELLADO DE TIEMPO - COSTA RICA v2.crt"));
            addCertificateToMap(parseX509("certs/CA SINPE - PERSONA FISICA v2.cer"));
            addCertificateToMap(parseX509("certs/CA SINPE - PERSONA FISICA v2(1).crt"));
            addCertificateToMap(parseX509("certs/CA SINPE - PERSONA FISICA v2(2).crt"));
            addCertificateToMap(parseX509("certs/CA SINPE - PERSONA JURIDICA v2.cer"));
            addCertificateToMap(parseX509("certs/CA SINPE - PERSONA JURIDICA v2(1).crt"));
            addCertificateToMap(parseX509("certs/CA SINPE - PERSONA JURIDICA v2(2).crt"));
            addCertificateToMap(parseX509("certs/TSA SINPE v2.cer"));
        }

    }

    public List<X509Certificate> getCertificateChain(X509Certificate certificate) throws Throwable {
        loadCertificateCache();
        X500Principal issuerPrincipal;
        String issuerSerialNumber;
        X509Certificate foundCertificate = null;
        X509Certificate actualCertificate = certificate;
        String actualSerialNumber;
        List<X509Certificate> certchain = new ArrayList<>();
        while(actualCertificate!=null) {
            certchain.add(actualCertificate);
            actualSerialNumber = extractCommonName(actualCertificate.getSubjectX500Principal().getName());
            issuerPrincipal = actualCertificate.getIssuerX500Principal();
            issuerSerialNumber = extractCommonName(issuerPrincipal.getName());
            foundCertificate = certificateMap.get(issuerSerialNumber);
            if (!actualSerialNumber.contentEquals(issuerSerialNumber)) {
                actualCertificate = foundCertificate;
            } else {
                actualCertificate = null;
            }
        }
        
        return certchain;
    }


    private static String extractCommonName(String subjectName) {
        String[] parts = subjectName.split(",");
        for (String part : parts) {
            String[] keyValue = part.trim().split("=");
            if (keyValue.length == 2 && keyValue[0].equalsIgnoreCase("CN")) {
                return keyValue[1];
            }
        }
        return null;
    }

    public X509Certificate getCertByCN(String cn) {
        return certificateMap.get(cn);

    }
}
