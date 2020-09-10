# Preguntas frecuentes

## ¿Cómo integrar firmador en un sitio web para que se lance la app, cargue un documento en la app y suba el documento firmado automáticamente?

Desde JavaScript deberá crearse una conexión XMLHttpRequest a 127.0.0.1:3516
por POST que envíe el fichero a firmar. Esperar la respuesta al POST que
contendrá el documento firmado de regreso. El sitio web además deberá lanzar la
descarga de un fichero JNLP similar al que existe en
https://firmador.app/firmador.jnlp y reintentar la conexión (por ejemplo con
`setTimeout()`) hasta que la aplicación acepte la conexión del sitio web.
El sitio web tendrá que manejar la respuesta del POST por sus propios medios
según lo que requieran las particularidades de la integración.

Los navegadores puede comunicarse con servicios locales que estén en la
interfaz loopback o en localhost sin que se bloquee la comunicación por
contenido mixto (HTTPS con HTTP) al considerarse en un estándar W3C como zona
segura. Para que este mecanismo evite que sitios web maliciosos examinen
servicios locales no autorizados, este tipo de servicios, como es el caso de
Firmador, usan explícitamente el encabezado `Access-Control-Allow-Origin`.

Para mejorar la seguridad, se usa un parámetro en el fichero JNLP para que
sitios de terceros maliciosos no puedan comunicarse con el firmador mientras
está ejecutándose desde Web Start en otro dominio diferente. El servicio viene
deshabilitado por defecto hasta que se agregue esta línea explicitamente.

Propiedad a agregar en el JNLP que activa el mecanismo de firma web:

    <property name="jnlp.remoteOrigin" value="https://example.org"/>

Reemplazar `https://example.org` por el origen deseado. No se recomienda usar
el valor `*` en producción por razones de seguridad. Una alternativa es generar
el JNLP de forma dinámica tomando el protocolo y host actuales para formar el
valor sin tener que cambiarlo entre los diferentes ambientes. El servicio HTTP
de Firmador utiliza `Vary: Origin` para prevenir que el navegador almacene en
memoria caché el valor.

Si se quieren probar la propiedad sin crear un JNLP, se puede lanzar por línea
de comandos mediante:

    java -Djnlp.remoteOrigin=https://example.org -jar target/firmador.jar


## ¿Por qué Firmador utiliza el puerto 3516 para el mecanismo de firma remota y no otro número en particular?

Porque ese número de puerto está registrado en IANA con el nombre
[smartcard-port](https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml?search=smartcard-port)
y descrito como "Smartcard Port", por lo que por contexto resulta conveniente.
Algunas herramientas como `netstat` o `ss` muestran este nombre que identifica
de forma más intuitiva el tipo de servicio que está funcionando en esa
conexión, facilitando la auditoría de red.
