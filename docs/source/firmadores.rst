Firmadores
==================

Son los elementos que permiten firmar los documentos, existe uno por cada tipo de documento soportado.

``DocumentSigner`` es la interfaz que proporciona las funcionalidades que tiene que tener un Firmador. Sus métodos son:

1. ``void setGui(GUIInterface gui)``: Asigna la interfaz a utilizar para enviar mensajes de error o mensajes para el usuario.
2. ``void setSettings(Settings settings)``: Asigna las settings del documento a utilizar.
3. ``DSSDocument sign(Document toSignDocument, CardSignInfo card)``: Firma el documento con la ``CardSignInfo`` proporcionada de donde puede sacar el certificado y la llave privada.
4. ``DSSDocument extend(DSSDocument document)``: Extiende el documento agregando una estampa de tiempo.
5. ``void setDetached(List<DSSDocument> detacheddocs)``:  Cuando se tienen firmas CAdES o ASIC se puede agregar los documentos que se quieran incorporar al contenedor o al validador, osea son los documentos fuente.

Existen las siguientes implementaciones: 

* FirmadorPAdES
* FirmadorOpenDocument
* FirmadorCAdES
* FirmadorOpenXmlFormat
* FirmadorASiC
* FirmadorXAdES


``CRSigner`` Es una clase de la cual heredan todas las implementaciones ya que proporciona implementación de varios métodos comunes.

1. ``void setGui(GUIInterface gui)``: Asigna la interfaz a utilizar para enviar mensajes de error o mensajes para el usuario.
2. ``void setSettings(Settings settings)``: Asigna las settings del documento a utilizar.
3. ``DSSPrivateKeyEntry getPrivateKey(SignatureTokenConnection signingToken)``: A partir de un `SignatureTokenConnection` obtiene la representación de la llave privada para firmar documentos con DSS.
4. ``String getPkcs11Lib``: En los contextos con PKCS11 obtiene la ruta de la biblioteca encargada de conectarse al dispositivo físico.
5. ``CertificateVerifier getCertificateVerifier``: Obtiene el manejador de verificación para DSS utilizando todas las cadenas de certificados disponibles.
6. ``CertificateVerifier getCertificateVerifier(CertificateToken subjectCertificate)``: Obtiene el manejador de verificación para DSS utilizando solamente las cadenas de certificados que tienen relación con el certificado dado.

