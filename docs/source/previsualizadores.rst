Previsualizadores
=======================

``PreviewerInterface`` Describe los métodos que debe implementar un previsualizador si desea formar parte de firmador libre. Sus métodos son:

1. ``void loadDocument(String filename)``: Carga en memoria el archivo y genera el previsualizado desde una ruta dada.
2. ``void loadDocument(byte[] data)``: Carga en memoria el archivo y genera el previsualizado desde bytes.
3. ``PDDocument getDocument()``: Retorna el documento de previsualización, tener en cuenta que es diferente al documento de firmado si este no es PDF.
4. ``BufferedImage getPageImage(int page)``: Retorna la representación de la página como una imágen.
5. ``int getNumberOfPages()``: Obtiene el número de páginas del documento previsualizado.
6. ``PDFRenderer getRender()``: Obtiene el render de pdf para generar las previsualizaciones de página.
7. ``boolean showSignLabelPreview()``: Retorna si se puede o no mostrar la representación de la firma en el espacio de previsualización.
8. ``void closePreview()``: Cierra y elimina los archivos temporales de previsualización.

Existen 3 previsualizadores actualmente estos son:
 
* PDFPreviewer
* NonPreviewer
* SofficePreviewer

en el caso de ``SofficePreviewer`` de momento solo funciona en Linux, aunque se puede forzar la ruta de Libreoffice en otros sistemas operativos y debería funcionar (no se ha probado aún).


