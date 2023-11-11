package cr.libre.firmador.validators;

import eu.europa.esig.dss.validation.reports.Reports;

public interface Validator {
    public void loadDocumentPath(String fileName);
    public Reports getReports();
    public boolean isSigned();

    public boolean hasStringReport();

    public String getStringReport();
}
