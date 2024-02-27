API para acceso remoto
=======================

Firmador libre posee un pequeño servidor web que permite registrar documentos en la aplicación de forma remota, para eso se posee la siguiente API

Creación
------------------

Registra un documento para firmar 

.. code:: html

    POST /create/<Nombre de archivo>

Si se desean firmados múltiples se necesita enviar los documentos vía post de tipo multipart, se logra en html incluyendo al tag form `enctype='multipart/form-data'`.  En el caso de multiples documentos se utiliza el nombre del documento dentro del multipart por lo que se ignora el nombre de archivo de la URL.

En ambos caso retorna http 200 para indicar que el documento fue cargado en la apliación correctamente.

El contenido del archivo cuando es único debe ser en formato binario.

Detalle y solicitud de documento
--------------------------------------

Con el nombre de archivo utilizado al enviar a crear se puede solicitar el documento firmado.

.. code:: html

    GET /detail/<Nombre de archivo>

Por defecto devolverá 204 cuando el documento no haya sido firmado y cuando ya está disponible lo retornará con código 200.
Si no se encuentra el documento se retornará un 404.

Eliminar documento
------------------------

Para remover el documento remoto debe ejecutar la siguiente petición 

.. code:: html

    DELETE /<Nombre de archivo>

Si no se encuentra el documento se retornará un 404.

Limpiar lista de documentos
----------------------------------

Para quitar todos los documentos de la lista de documentos se debe enviar la solicitud con la siguiente forma 

.. code:: html

    GET /clean
    
Cerrar la aplicación 
-----------------------------

En contextos como java applet se requiere poder cerrar el cliente, y se puede hacer mediante la siguiente petición 

.. code:: html

    GET /close
    
 
