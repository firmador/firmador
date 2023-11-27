Validadores
=================

Los validadores constituyen las clases que permiten determinar si un documento está correctamente firmado o no.

``Validator`` es la clase que describe como debe ser un validador, sus métodos son: 

1. `DSSDocument loadDocumentPath(String fileName)``: Valida el documento de la ruta proporcionada, retorna el documento DSS utilizado para la validación.
2. ``Reports getReports()``: Obtiene los reportes del documento.
3. ``boolean isSigned()``:  Retorna verdadero si el documento tiene firmas (no importa si son válidad o no)
4. ``boolean hasStringReport()``: Retorna verdadero si el validador genera ``Reports`` de DSS.
5. ``String getStringReport()``: Obtiene el reporte en String o vació, en general convierte ``Reports`` en html.
6.  ``int amountOfSignatures()``: Retorna la cantidad de firmas que posee el documento (no necesariamente válidas, con solo que estén se toman en cuenta).


``ValidatorFactory``: es la clase utilizada para detectar cual validador se debe utilizar dependiendo del formato de documento que se tiene.

Las implementaciones existentes actualmente son: 

* GeneralValidator:  Se encarga de casi todas los documentos soportados.
* OOXMLValidator: Valida los documentos OpenXML Format de Microsoft Office.
