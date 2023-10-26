package cr.libre.firmador.oxml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.openxml4j.opc.OPCPackage;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.DSSDocument;

public class DSSDocumentOXML implements DSSDocument   {
	private OPCPackage opcpkg;
	protected MimeType mimeType;
	protected String name;

	public OPCPackage getOpcpkg() {
		return opcpkg;
	}
	public void setOpcpkg(OPCPackage opcpkg) {
		this.opcpkg = opcpkg;
	}
	public MimeType getMimeType() {
		return mimeType;
	}
	public void setMimeType(MimeType mimeType) {
		this.mimeType = mimeType;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public DSSDocumentOXML(OPCPackage opcpkg){
		this.opcpkg=opcpkg;
	}
	@Override
	public InputStream openStream() {
		
		return null;
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		this.opcpkg.save(stream);
	}


	@Override
	public void save(String filePath) throws IOException {
		File archive = new File(filePath);
		this.opcpkg.save(archive);
		
	}

	@Override
	public String getDigest(DigestAlgorithm digestAlgorithm) {
		// TODO Auto-generated method stub
		return null;
	}

}
