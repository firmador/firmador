/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2020 Firmador authors.

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

import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;

public class Validator {

    private SignedDocumentValidator documentValidator;

    public Validator(String fileName) {
        CertificateSource trustedCertSource =
            new CommonTrustedCertificateSource();
        trustedCertSource.addCertificate(DSSUtils.loadCertificate(
            this.getClass().getClassLoader().getResourceAsStream(
                "CA RAIZ NACIONAL - COSTA RICA v2.crt")));
        trustedCertSource.addCertificate(DSSUtils.loadCertificate(
            this.getClass().getClassLoader().getResourceAsStream(
                "CA RAIZ NACIONAL COSTA RICA.cer")));

        CertificateVerifier cv = new CommonCertificateVerifier();
        cv.setTrustedCertSources(trustedCertSource);
        cv.setDataLoader(new CommonsDataLoader());
        cv.setOcspSource(new OnlineOCSPSource());
        cv.setCrlSource(new OnlineCRLSource());

        documentValidator = SignedDocumentValidator.fromDocument(
            new FileDocument(fileName));
        documentValidator.setCertificateVerifier(cv);
    }

    public Reports getReports() {
        Reports reports = documentValidator.validateDocument();
        return reports;
    }

    public boolean isSigned() {
        return !documentValidator.getSignatures().isEmpty();
    }

}
