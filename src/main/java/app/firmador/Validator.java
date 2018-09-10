/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018 Firmador authors.

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

package app.firmador;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.FileDocument;
import eu.europa.esig.dss.client.crl.OnlineCRLSource;
import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.client.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;

public class Validator {

    private SignedDocumentValidator documentValidator;

    public Validator(String fileName) {
        CertificateVerifier cv = new CommonCertificateVerifier();
        cv.setDataLoader(new CommonsDataLoader());
        cv.setOcspSource(new OnlineOCSPSource());
        cv.setCrlSource(new OnlineCRLSource());
        // TODO For CR, trusting CA RAIZ NACIONAL should be enough
        //cv.setTrustedCertSource(trustedCertSource);
        // [remove] AIA should be enough for CR, not using offline validation
        //cv.setAdjunctCertSource(adjunctCertSource);

        DSSDocument document = new FileDocument(fileName);

        documentValidator = SignedDocumentValidator.fromDocument(document);
        documentValidator.setCertificateVerifier(cv);
    }

    public Reports getReports() {
        // TODO use custom validation policy for CR instead of default
        Reports reports = documentValidator.validateDocument();

        return reports;
    }

}
