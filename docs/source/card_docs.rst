
Documentos y manejo de tarjetas
####################################


Firmador Libre es una aplicación escrita en java y mantiene una estructura de paquetes propia del lenguaje.

Los paquetes que posee son:

Cards
====================

Contiene todo el código necesario para proveer acceso a las tarjetas físicas, así como la representación de tarjetas que se usa para identificar el certificado deseado para la firma de documentos.


* ``CardManager``: es el encargado de detectar si se utiliza PKCS11 o PKCS12, posee el método estático ``getCartdManager`` que retorna el ``CardManagerInterface`` adecuado según el ``CardSignInfo`` proporcionado.
* ``SmartCardDetector``: Monitoriza cambios en los dispositivos físicos, es el encargado de identificar si el usuario tiene una tarjeta o varias conectadas.
* ``CardManagerInterface``: es el encargado de la comunicación con las tarjetas físicas o los keystore de java, se encarga de convertir la información que está en el almacenamiento en ``CardSignInfo``. Posee los métodos:

1. ``getCertificates`` que retorna la lista de certificados disponibles en el almacenamiento físico.
2. ``getKeyStore`` retorna un Keystore de java que contiene el certificado y la llave privada (en el caso de pkcs11 tiene el apuntador a la llave).
3. ``getPrivateKey`` Obtiene la llave privada para firmar.
4. ``getCertificate`` Obtiene el certificado a partir del token (Dispositivo físico) y slot (Posición del certificado en el dispositivo) proporcionados.
5. ``setSerialNumber`` en el caso de PKCS12 indica la ruta donde se debe leer la información del keystore, en PKCS11 no se utiliza.
6. ``loadTokens`` en PKCS12 busca el primer token de un keystore y lo retorna, en PKCS11 no se utiliza.

Posee 2 implementaciones ``PKCS11Manager`` y ``PKCS12Manager``.

Documents
=================

Contiene la representación de un documento para firmador libre, así como los manejadores de previsualización de documentos.

* ``SupportedMimeTypeEnum``: Es un Enum diseñado para representar los formatos soportados por la aplicación.
* ``MimeTypeDetector``: Proporciona el método estático ``detect`` el cual intenta adivinar el MimeType correspondiente al documento, si el mimetype no está soportado, retorna BINARY como mimetype para poder firmar documentos con ASIC-E.
* ``PreviewerManager``: Proporciona el método estático ``getPreviewManager`` el cual según el MimeType proporcionado retorna el manejador de Preview adecuado.  Posee las implementaciones:

1. ``NonPreviewer``: Genera una imagen que indica que la previsualización no está disponible.
2. ``PDFPreviewer``: Previsualizador de documentos PDF (Es el más exacto de todos)
3. ``SofficePreviewer``: Si se tiene instalado libreoffice genera previsualizaciones de los documentos usando esta herramienta, la cual puede dar una idea del documento a firmar pero no es una representación exacta.

* ``Document``: Representa un documento dentro de la aplicación, es el encargado de almacenar la información y las referencias necesarias para manipular el documento. Generalmente las operaciones de firmado, previsualización y validación se realizan en el documento pero llamado desde otro Hilo o Swing Worker.

1. ``getSettings`` y  ``setSettings``:  Los settings son los mismos ``Settings`` que se usan a lo largo de la aplicación, pero se almacena una copia de los mismos por cada documento, de forma que se puedan firmar multiples documentos con configuraciones diferentes.
2. ``validate`` Ejecuta el proceso de validación del documento bloqueando el hilo que lo llame.
3. ``sign`` Firma el documento con el ``CardSignInfo`` proporcionado bloqueando el hilo que lo llame.
4. ``extend`` Extiende la firma de un documento agregando la información de estampa de tiempo necesaria, bloqueando el hilo que lo llame.
5. ``loadPreview`` Genera una previsualización el documento, bloqueando el hilo que lo llame.
6. ``setPrincipal`` puede ser llamado cuando se quiere que el documento sea el documento de trabajo del usuario, llama a las validaciones y al preview si no se tienen en cache.
7. ``getPathName`` retorna la ruta completa de donde se lee el documento.
8. ``getName`` retorna solo el nombre del documento.
9. ``getMimeType`` retorna el MimeType del documento según se haya detectado.
10. ``getExtension`` retorna la extensión del documento, acá la extensión del documento de entrada puede variar si por ejemplo se firma CAdES o ASIC-E.
11. ``getPathToSave`` retorna la ruta de guardado del documento.
12. ``getPathToSaveName`` retorna el nombre del documento a guardar, incluye ya la extensión.
13. ``setPathToSaveName`` permite guardar el nombre del documento a guardar.
14. ``setPathToSave``  permite guardar la ruta de guardado del documento.
15. ``getReport`` retorna el reporte de validación del documento en HTML. 
16. ``getPlainReport`` retorna el reporte de validación sin los tags de HTML, el cual se usa para llenar los contextos de accesibilidad.
17. ``isValid``  retorna si el documento tiene firmas válidas, en caso de que el documento no haya sido validado retorna `False`.
18. ``getPreviewManager`` retorna el manejador de previsualizaciones para ser utilizado en el panel de firmado.
19. ``amountOfSignatures`` retorna la cantidad de firmas que tiene el documento, si no está validado retorna 0.
20. ``getSignedDocument`` retorna el documento firmado o extendido.
21. ``setSignedDocument`` guarda el documento firmado, los `signers` después del proceso de firma actualizan el documento proporcionando el documento ya firmado.
22. ``getIsReady`` retorna verdadero si ya el proceso de validación y generación de previsualizaciones terminó.
23. ``getNumberOfPages`` retorna el número de páguinas que prosee la previsualización. **Nota:** la previsualización puede ser inexacta y diferenciarse de lo que se vería en un programa de edición de documentos, por lo que se debe tomar la previsualización como una forma de referencia.
24. ``checkIsReady`` verifica si la validación y el preview ya se realizaron.
25. ``forcesignASiC`` fuerza a utilizar el ``FirmadorASIC`` en lugar del detectado según el tipo de documento.
26. ``forceCades`` fuerza a utilizar el ``FirmadorCAdES`` en lugar del detectado según el tipo de documento.

Eventos del documento
------------------------

El documento propociona varios eventos que pueden ser escuchados por cualquier clase que lo requiera, para escucharlos se registrar usando el método.
``registerListener`` y ser una clase que implemente ``DocumentChangeListener``.

``DocumentChangeListener`` proporciona los siguientes métodos:

1. ``previewDone``  Se llama cuando la previsualización ha sido cargada en memoria.
2. ``validateDone`` Se llama cuando la validación se ha completado.
3. ``signDone`` Se llama cuando la firma de un documento se ha completado.
4. ``extendsDone`` Se llama cuando el documento ha sido extendido.

Es importante mencionar que todos los métodos pasan como parámetro el documento que generó el evento, además que el orden de llamado no es necesariamente ``signDone`` y luego ``extendDone`` si no que podría darse que la extensión del documento se llame primero.
Adicionalmente indicar que se realiza por documento, así que se llama multiples veces cuando se realizan firmas multiples.


