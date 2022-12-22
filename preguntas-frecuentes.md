# Preguntas frecuentes


## ¿Cómo utilizar Firmador por línea de comandos o de forma automatizada?

Firmador cuenta actualmente con 3 tipos de interfaces de usuario:
- `swing` (predeterminada)
- `shell` (interactiva por consola)
- `args` (no interactiva, con parámetros)

Para seleccionar la interfaz, utilizar -d seguido de la interfaz sin espacios.

Ejemplo para seleccionar la interfaz interactiva por consola:

    java -jar firmador.jar -dshell

Ejemplo para la interfaz por parámetros:

    java -jar firmador.jar -dargs original.pdf firmado.pdf

En este caso solicita el pin de forma interactiva pero podría suministrarse
por ejemplo de la siguiente manera para que quede completamente automatizado:

    echo 0000 | java -jar firmador.jar -dargs original.pdf firmado.pdf

Donde `0000` es un pin de ejemplo, así como las rutas de carga y guardado, que
pueden ser absolutas o relativas. Se recomienda entrecomillar las rutas si
contienen caracteres especiales, como por ejemplo espacios.

También hay un parámetro `-slot`, en caso de que existiera más de un slot en el
sistema, al que se le puede proporcionar el número deseado. Este parámetro es
opcional. El parámetro es un número junto al parámetro, por ejemplo `-slot0`.
NOTA: el parámetro `-slot` no está funcionando desde la versión 1.9.0, se
plantea reparar en versiones posteriores.

El parámetro `-timestamp` permite agregar sellos de tiempo a documentos. Cuando
se define, no firmará, solo sellará, por lo que no requiere suministrar PIN.

El parámetro `-visible-timestamp` es similar a `-timestamp` pero muestra una
representación visual dentro de los documentos PDF en la esquina superior
izquierda de la primera página.

Es posible utilizar un tercer parámetro en la interfaz args para firmar con
un fichero de almacén de certificados. Se puede utilizar de la siguiente forma:

    echo contraseña | java -jar firmador.jar -dargs original.pdf firmado.pdf almacen.p12

En el caso de ejecutarse en Windows, no debe dejar espacio antes del símbolo
`|` o no reconocerá el PIN o la contraseña como válidos.


## ¿Cómo integrar firmador en un sitio web para que se lance la app, cargue un documento en la app y suba el documento firmado automáticamente?

NOTA: la integración web no está funcionando desde la versión 1.9.0, se
plantea reparar en versiones posteriores.

Desde JavaScript deberá crearse una conexión XMLHttpRequest a 127.0.0.1:3516
por POST que envíe el fichero a firmar. Esperar la respuesta al POST que
contendrá el documento firmado de regreso. El sitio web además deberá lanzar la
descarga de un fichero JNLP similar al que existe en
https://firmador.libre.cr/firmador.jnlp y reintentar la conexión (por ejemplo
con `setTimeout()`) hasta que la aplicación acepte la conexión del sitio web.
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

    java -Djnlp.remoteOrigin=https://example.org -jar firmador.jar

Para no sobrecargar el servidor de firmador.libre.cr, se recomienda modificar
el atributo `codebase` y alojar el JAR en un servidor propio, además de que
reduce el riesgo de que en caso de caerse el servidor de firmador.libre.cr,
afecte a integraciones de terceros. Aun así, si se quiere experimentar con una
versión de Firmador habilitada para CORS en cualquier dominio, existe
https://firmador.libre.cr/firmador-en-pruebas.jnlp que permite recibir
peticiones desde cualquier origen. Esta versión puede recibir modificaciones
inestables y caídas al tratarse de una versión para desarrollo y pruebas.

Puede accederse a una demostración de firma web en la siguiente dirección:
https://firmador.libre.cr./demo-firma-web/


## ¿Por qué Firmador utiliza el puerto 3516 para el mecanismo de firma remota y no otro número en particular?

Porque ese número de puerto está registrado en IANA con el nombre
[smartcard-port](https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml?search=smartcard-port)
y descrito como "Smartcard Port", por lo que por contexto resulta conveniente.
Algunas herramientas como `netstat` o `ss` muestran este nombre que identifica
de forma más intuitiva el tipo de servicio que está funcionando en esa
conexión, facilitando la auditoría de red.


## ¿Se puede agregar una imagen personalizada por línea de comandos a la hora de firmar ficheros PDF?

Sí, aunque la norma PAdES en su parte 6 recomienda que solo se agreguen
imágenes que provienen del propio certificado. Firmador lo tiene habilitado
como parámetro opcional, como en el caso de la firma remota. Puede agregarse
una propiedad a un fichero JNLP llamada `jnlp.signatureImage` con el valor
correspondiente a una URL que contenga la imagen. El tamaño sugerido es de 170
pixeles de alto y aparecerá a la izquierda del texto de la firma. Este
parámetro también se puede agregar por línea de comandos.


## ¿Es posible quitar el mensaje de que la firma visible no es fuente de confianza por línea de comandos?

Este mensaje opcional es una recomendación de la norma PAdES parte 6 para
que las personas no capacitadas en Firma Digital no crean que la representación
visual de la firma (firma visible) es una firma digital como tal. Firmador
actualmente en el caso de modificar alguno de los campos de razón, lugar y
contacto, el mensaje se reemplazará por uno o más de los valores ingresados en
estos campos. Si aun así se desea eliminar el texto cuando no se rellena
ninguno de esos campos opcionales, se puede realizar mediante JNLP agregando la
propiedad `jnlp.hideSignatureAdvice` con el valor `true`.


## ¿Cómo se puede ajustar el nivel de información de depuración mostrada por el firmador mediante línea de comandos?

Se puede utilizar el siguiente comando donde el nivel máximo es `trace`:

    java -jar -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog -Dorg.apache.commons.logging.simplelog.defaultlog=trace -jar firmador.jar

Los valores posibles son: `off`, `error`, `warn`, `info` (predeterminado),
`debug` y `trace`.
