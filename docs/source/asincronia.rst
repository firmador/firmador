Agendadores y Workers
=============================

Los agendadores son clases que se ejecutan en un Hilo aparte para no bloquear el hilo que maneja la interfaz, tiene una lista de documentos que deben procesar, 
por cada archivo crean una instancia de un worker para procesar el documento.  Hacen uso de semáforos para identificar la cantidad de workers en ejecución.


* ``PreviewScheduler`` hace uso de ``PreviewWorker``.
* ``SignerScheduler`` hace uso de ``SignerWorker``.
* ``ValidateScheduler` hace uso de ``ValidationWorker``.

Si bien no existe una interfaz que determine los métodos requeridos para un worker, todos tiene los siguientes métodos 

1. ``addDocument(Document document)``: Agrega un documento a la cola y libera un semáforo.
2. ``addDocuments(List<Document> documents)``: Agrega todos los documentos de la lista entrante en la cola y libera un semáforo.
3. ``done()``: Es llamado cuando un worker termina su tarea.
