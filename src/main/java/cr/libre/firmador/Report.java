/* Firmador is a program to sign documents using AdES standards.

Copyright (C) Firmador authors.

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

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import eu.europa.esig.dss.DSSXmlErrorListener;
import eu.europa.esig.dss.DomUtils;
import eu.europa.esig.dss.diagnostic.SignatureWrapper;
import eu.europa.esig.dss.validation.reports.Reports;

public class Report {

    private StringWriter writer = new StringWriter();
    private String annotationChanges = new String();

    public Report(Reports reports) throws Exception {
        Transformer transformer = DomUtils.getSecureTransformerFactory().newTemplates(new StreamSource(this.getClass().getResourceAsStream("/xslt/html/simple-report.xslt"))).newTransformer();
        transformer.setErrorListener(new DSSXmlErrorListener());
        transformer.transform(new StreamSource(new StringReader(reports.getXmlSimpleReport())), new StreamResult(writer));
        for (SignatureWrapper wrapper : reports.getDiagnosticData().getSignatures())
            if (!wrapper.getPdfAnnotationChanges().isEmpty())
                annotationChanges = "<p><b>Aviso importante:</b> el siguiente documento firmado tiene modificaciones (anotaciones) añadidas después de haberse firmado.</p>";
    }

    public String getReport() {
        return "<html>" + annotationChanges + writer.toString() + "</html>";
    }

}
