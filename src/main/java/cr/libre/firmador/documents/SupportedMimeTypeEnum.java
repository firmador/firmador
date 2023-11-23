package cr.libre.firmador.documents;

import eu.europa.esig.dss.enumerations.MimeType;

public enum SupportedMimeTypeEnum implements MimeType{
    /** octet-stream */
    BINARY("application/octet-stream"),
		
    XML("text/xml", "xml"),
    /** opendocument text */
    ODT("application/vnd.oasis.opendocument.text", "odt"),

    /** opendocument spreadsheet */
    ODS("application/vnd.oasis.opendocument.spreadsheet", "ods"),

    /** opendocument presentation */
    ODP("application/vnd.oasis.opendocument.presentation", "odp"),

    /** opendocument graphics */
    ODG("application/vnd.oasis.opendocument.graphics", "odg"),
    
    PDF ("application/pdf", "pdf"),
    
    DOCX ("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"),
    XLSX ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    PPTX ("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx"),    
    

    DOC ("application/msword", "doc"),
    PPT ("application/vnd.ms-powerpoint", "ppt"),
    XLS ("application/vnd.ms-excel", "xls"),
    /** json */
    JSON("application/json", "json");
	
    /** MimeType identifier */
    final String mimeTypeString;

    /** File extension corresponding to the MimeType */
    final String[] extensions;

	SupportedMimeTypeEnum(final String mimeTypeString, final String... extensions) {
        this.extensions = extensions;
        this.mimeTypeString = mimeTypeString;
    
	}

    @Override
    public String getMimeTypeString() {
        return mimeTypeString;
    }

    @Override
    public String getExtension() {
        if (extensions != null && extensions.length > 0) {
            return extensions[0];
        }
        return null;
    }
    
    public boolean withoutVisualization() {
    
    	return mimeTypeString == XML.getMimeTypeString()  || 
    			isOpenDocument() ||
    			isOpenxmlformats() || 
    			isMSoldOffice();
    }
    public boolean isOpenDocument() {
    	return mimeTypeString == SupportedMimeTypeEnum.ODG.getMimeTypeString() ||
     		   mimeTypeString == SupportedMimeTypeEnum.ODP.getMimeTypeString() || 
     		   mimeTypeString == SupportedMimeTypeEnum.ODS.getMimeTypeString() || 
     		   mimeTypeString == SupportedMimeTypeEnum.ODT.getMimeTypeString();
    }
    public boolean isOpenxmlformats() {
    	return mimeTypeString == SupportedMimeTypeEnum.XLSX.getMimeTypeString() ||
      		   mimeTypeString == SupportedMimeTypeEnum.DOCX.getMimeTypeString() || 
      		   mimeTypeString == SupportedMimeTypeEnum.PPTX.getMimeTypeString() ;
    }
    public boolean isMSoldOffice() {
    	return mimeTypeString == SupportedMimeTypeEnum.XLS.getMimeTypeString() ||
       		   mimeTypeString == SupportedMimeTypeEnum.DOC.getMimeTypeString() || 
       		   mimeTypeString == SupportedMimeTypeEnum.PPT.getMimeTypeString() ;   	
    }

    public boolean isPDF() {
        return mimeTypeString == SupportedMimeTypeEnum.PDF.getMimeTypeString();
    }

    public boolean isXML() {
        return mimeTypeString == SupportedMimeTypeEnum.XML.getMimeTypeString();
    }
}
