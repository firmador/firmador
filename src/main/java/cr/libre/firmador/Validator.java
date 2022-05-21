/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018, 2022 Firmador authors.

This file is part of Firmador.

Firmador is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Firmador is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Firmador.  If not, see <http://www.gnu.org/licenses/>.  */

package cr.libre.firmador;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.MimeType;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignaturePolicyProvider;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.xades.validation.XMLDocumentValidator;

public class Validator {

    private SignedDocumentValidator documentValidator;

    public Validator(String fileName) {
        CertificateSource trustedCertSource = new CommonTrustedCertificateSource();
        trustedCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("CA RAIZ NACIONAL - COSTA RICA v2.crt")));
        trustedCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("CA RAIZ NACIONAL COSTA RICA.cer")));
        // Just for testing for now, it should be adviced this Root CA is not trusted and not a part of national official document format policy. It is just for tax office purposes
        //trustedCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("CA RAIZ MINISTERIO DE HACIENDA.crt")));
        CertificateVerifier cv = new CommonCertificateVerifier();
        cv.setTrustedCertSources(trustedCertSource);
        cv.setOcspSource(new OnlineOCSPSource());
        cv.setCrlSource(new OnlineCRLSource());
        cv.setAIASource(new DefaultAIASource());
        FileDocument fileDocument = new FileDocument(fileName);
        documentValidator = SignedDocumentValidator.fromDocument(fileDocument);
        documentValidator.setCertificateVerifier(cv);
        if (fileDocument.getMimeType() == MimeType.XML) {
            String electronicReceipt = new XMLDocumentValidator(fileDocument).getRootElement().getDocumentElement().getTagName();
            String[] receiptTypes = {"FacturaElectronica", "TiqueteElectronico", "NotaDebitoElectronica", "NotaCreditoElectronica", "FacturaElectronicaCompra", "FacturaElectronicaExportacion", "MensajeReceptor"};
            if (Arrays.asList(receiptTypes).contains(electronicReceipt)) {
                SignaturePolicyProvider signaturePolicyProvider = new SignaturePolicyProvider(); // Custom policy provider for offline policy validation (no PDF download required)
                Map<String, DSSDocument> signaturePoliciesById = new HashMap<>(); // FIXME support more versions by checking schema version later, not just v4.3
                String policyId = "https://www.hacienda.go.cr/ATV/ComprobanteElectronico/docs/esquemas/2016/v4.3/Resoluci%C3%B3n_General_sobre_disposiciones_t%C3%A9cnicas_comprobantes_electr%C3%B3nicos_para_efectos_tributarios.pdf";
                DSSDocument policyDocument = new InMemoryDocument(this.getClass().getClassLoader().getResourceAsStream("Resolución_General_sobre_disposiciones_técnicas_comprobantes_electrónicos_para_efectos_tributarios.pdf"));
                signaturePoliciesById.put(policyId, policyDocument);
                signaturePolicyProvider.setSignaturePoliciesById(signaturePoliciesById);
                documentValidator.setSignaturePolicyProvider(signaturePolicyProvider);
            }
        }
    }

    public Reports getReports() {
        Reports reports = documentValidator.validateDocument();
        return reports;
    }

    public boolean isSigned() {
        return !documentValidator.getSignatures().isEmpty();
    }

}
