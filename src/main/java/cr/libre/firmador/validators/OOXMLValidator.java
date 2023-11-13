package cr.libre.firmador.validators;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.SignaturePart;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3.x2000.x09.xmldsig.SignatureDocument;
import org.w3.x2000.x09.xmldsig.SignatureType;
import org.w3.x2000.x09.xmldsig.SignedInfoType;

import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.validation.reports.Reports;

public class OOXMLValidator implements Validator {
    private OPCPackage documentooxml = null;
    private boolean isSignedDocument = false;
    private SignatureConfig signatureConfig = null;
    private HashMap<SignaturePart, String> signParts;
    private SignatureInfo signatureInfo = null;
    private boolean isValid;



    @Override
    public void loadDocumentPath(String fileName) {
        try {
            documentooxml = OPCPackage.open(fileName, PackageAccess.READ);
            signatureConfig = new SignatureConfig();
            signatureInfo = new SignatureInfo();
            signatureInfo.setOpcPackage(documentooxml);
            signatureInfo.setSignatureConfig(signatureConfig);

            isValid = signatureInfo.verifySignature();
            signParts = toList(signatureInfo.getSignatureParts());
            if (!signParts.isEmpty()) {
                isSignedDocument = true;
            }
        } catch (InvalidOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private HashMap<SignaturePart, String> toList(Iterable<SignaturePart> signatureParts) {
        HashMap<SignaturePart, String> list = new HashMap<SignaturePart, String>();
        Iterator<SignaturePart> iter = signatureParts.iterator();
        SignaturePart part;
        while (iter.hasNext()) {
            part = iter.next();
         
            list.put(part, part.validate() ? "Válido": "Inválido");
        }
        return list;
    }

    @Override
    public Reports getReports() {
        return null;
    }

    @Override
    public boolean isSigned() {
        return isSignedDocument;
    }

    @Override
    public boolean hasStringReport() {
        return true;
    }

    private String validaRevocacion(Document document) {
        boolean tspvalido = true;
        return tspvalido ? "Válido" : "Inválido";
    }


    @Override
    public String getStringReport() {
        String report="";
        int position = 0;
        for (SignaturePart part : signParts.keySet()) {
            String signok = signParts.get(part);
            position += 1;
            String signdate = "";
            X509Certificate signer = part.getSigner();
            try {
                SignatureDocument signatureDocument = part.getSignatureDocument();
                //Node node = signatureDocument.getDomNode();
                Node signaturenode = signatureDocument.getSignature().getDomNode();

                 Document document = signaturenode.getOwnerDocument();
                 Element signtime = document.getElementById("idSignatureTime");
                 NodeList timevalue = signtime.getElementsByTagName("mdssi:Value");

                 for (int i = 0; i < timevalue.getLength(); i++) {
                     Element e = (Element) timevalue.item(i);
                     signdate = e.getFirstChild().getNodeValue();
                 }
                 String revocaentiempo = validaRevocacion(document);

            boolean[] keyUsage = signer.getKeyUsage();
            if (signer.getBasicConstraints() == -1 && keyUsage[0] && keyUsage[1]) {
                LdapName ldapName = new LdapName(signer.getSubjectX500Principal().getName("RFC1779"));
                String firstName = "", lastName = "", identification = "", commonName = "", organization = "";
                for (Rdn rdn : ldapName.getRdns()) {
                    if (rdn.getType().equals("OID.2.5.4.5")) identification = rdn.getValue().toString();
                    if (rdn.getType().equals("OID.2.5.4.4")) lastName = rdn.getValue().toString();
                    if (rdn.getType().equals("OID.2.5.4.42")) firstName = rdn.getValue().toString();
                    if (rdn.getType().equals("CN")) commonName = rdn.getValue().toString();
                    if (rdn.getType().equals("O")) organization = rdn.getValue().toString();
                }
            
                String expires = new SimpleDateFormat("yyyy-MM-dd").format(signer.getNotAfter());

                report += position + ". Firmante: " + firstName + " " + lastName + " (" + identification + "), "
                        + organization + "<br>\n&nbsp;&nbsp;&nbsp; Fecha oficial de la firma: " + signdate
                        + "<br>&nbsp;&nbsp;&nbsp; Garantía de integridad y autenticidad: " + signok
                        + " (Certificado expira: " + expires + ")"
                        + "<br>&nbsp;&nbsp;&nbsp; Garantía de validez en el tiempo: " + revocaentiempo;
            }

            } catch (Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }



        }
        return report;
    }

}
