Interfaces
#################

Firmador libre prosee varias interfaces por las que puede ser llamado, todas las interfaces deben heredar de ``GUIInterface`` el cual posee los siguientes métodos:

1.  ``void loadGUI()``: Es el método llamado inmediatamente después de que el programa inicia.
2.  ``void setArgs(String[] args)``: Pasa a la interfaz los parámetros con los que firmador libre fue llamado.
3.  ``void showError(Throwable error)``: Se proporciona un mensaje de error para ser mostrado, almacenado o lo que la interfaz quiera hacer con el, estos mensajes se generan durante el flujo de firmado y no tienen un orden fijo.
4.  ``void showMessage(String message)``: Se proporciona un mensaje para ser mostrado al usuario o realizar lo que la interfaz disponga.
5.  ``String getDocumentToSign()``: Retorna la ruta completa del documento a firmar.
6.  ``String getPathToSave(String extension)``: Retorna la ruta completa donde se debe guardar el documento.
7.  ``CardSignInfo getPin()``: Retorna la información del PIN de la tarjeta para usarse a la hora de firmar.
8.  ``void configurePluginManager()``: Registra el plugin manager para que la interfaz pueda proporcionale eventos u obtener eventos del plugin.
9.  ``public void loadDocument(String fileName)``: Carga un documento dado la ruta completa del documento.
10. ``public void loadDocument(SupportedMimeTypeEnum mimeType, PDDocument doc)``: Carga un documento según el mimetype proporcionado y el documento `PDDocument`.
11. ``public void extendDocument()``: Extiende el documento ya firmado.
12. ``String getPathToSaveExtended(String extension)``: Retorna la ruta de guardado cuando el documento solo se extendió, cuando se firma el documento se utiliza `getPathToSave`.
13. ``public boolean signDocuments()``: Firma todos los documentos encolados.
14. ``public void displayFunctionality(String functionality)``: En el caso de `Swing Interface` permite cambiarse de TAB según el nombre proporcionado.
15. ``public void nextStep(String msg)``: Permite indicar un mensaje para el paso que se está dando en el flujo de firmado.
16.  ``void previewDone(Document document)``: Es llamada cuando se termina de cargar la previsualización de un documento.
17. ``void validateDone(Document document)``: Es llamada cuando se termina de validar un documento.
18. ``void signDone(Document document)``: Es llamada cuando se termina de firmar un documento.
19. ``void extendsDone(Document document)``: Es llamada cuando se termina de extender un documento.
20. ``void doPreview(Document document)``:  Encola la generación de previsualización de un documento en el Agendador de previsualizaciones.
21. ``void validateAllDone()``: Es llamada cuando se termina de validar todos los documentos encolados.
22. ``void signAllDone()``: Es llamada cuando se termina de firmar todos los documentos encolados.
23. ``Settings getCurrentSettings()``: Obtiene los settings que están seleccionados actualmente en la interfaz (no confundir con los settings de toda la aplicación ya que usan la misma clase Settings)
24. ``void signDocument(Document document)``: Encola un documento para ser firmado con el Agendador de firmado.
25. ``void setPluginManager(PluginManager pluginManager)``: Asigna el plugin manager para uso de la interfaz.

Existen las siguientes implementaciones de esta interfaz.

* GUIArgs
* GUIShell
* GUISwing

Seleccionando la intefaz
---------------------------

``GUISelector`` es la clase encargada de seleccionar que interfaz se debe utilizar, para ello proporciona el método `GUIInterface getInterface(String[] args)` de forma que pueda ser llamada por el firmador.
Se puede especificar la interfaz a utilizar mediante los comandos.

.. code:: bash 

	java -jar firmador.jar -dswing
	
Las posibles opciones para la línea de comandos son `args`, `shell` y `swing` siendo la última la interfaz por defecto.

