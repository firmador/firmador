# Firmador

Herramienta de escritorio para firmar documentos que cumplen con la [Política
de Formatos Oficiales de los Documentos Electrónicos Firmados Digitalmente](
https://mifirmadigital.go.cr/wp-content/uploads/2016/03/DCFD-Política-de-Formato-Oficial-v1.0.pdf
) de Costa Rica.

Está diseñado para funcionar con Java y funciona en GNU/Linux, macOS y Windows.
En el caso de macOS y Windows requiere instalar [Java](https://java.com/) y los
controladores de lector y tarjeta del sitio web de [descargas de Soporte Firma
Digital](https://soportefirmadigital.com/sfdj/dl.aspx).
En el caso de GNU/Linux, la forma recomendada para instalarlo está explicada en
los siguientes artículos para instalar firma digital de Costa Rica en
[Fedora](https://fran.cr/instalar-firma-digital-costa-rica-linux-fedora/) y en
[Ubuntu](https://fran.cr/instalar-firma-digital-costa-rica-gnu-linux-ubuntu/).


## Descarga

- [Descargar firmador](https://firmador.app/firmador.jar) para Windows,
  GNU/Linux y macOS.


## Capturas de pantalla

![Firmador para GNU/Linux](pantallazos/gnulinux.png)

![Firmador para macOS](pantallazos/macos.png)

![Firmado para Windows](pantallazos/windows.png)


## Instalación y ejecución

Por el momento Firmador no requiere instalación, es un paquete ejecutable.

En Windows se puede abrir directamente con doble clic.

En macOS se puede ejecutar también con doble clic, aunque la primera vez habrá
que abrirlo con control + clic o bien clic secundario sobre el fichero desde el
Finder y seleccionar Abrir. El sistema preguntará si se quiere abrir una
aplicación descargada de Internet y habrá que confirmar. Una vez hecho esto,
las siguientes aperturas funcionarán con doble clic.

En algunas distribuciones como Ubuntu se puede ejecutar con doble clic si se
configura el fichero como ejecutable. En las que no esté disponible esta
característica, se puede ejecutar desde terminal con `java -jar firmador.jar`.

En próximas versiones podría distribuirse con instaladores propios para Windows
y macOS, desde el momento en el que Oracle no ofrezca instaladores para Java y
por tanto deba incorporarse con el propio instalador. Asimismo, podrían
ofrecerse paquetes para distribuciones GNU/Linux para mayor comodidad de
ejecución.


## Compilación del código fuente

Para las personas interesadas en el desarrollo de Firmador, para compilar
Firmador se requiere git, Maven y OpenJDK.

En este momento se requiere una versión en desarrollo (snapshot) de DSS, la
librería implementación de referencia de los formatos AdES, que se puede
obtener desde el repositorio git de DSS con espejo en GitHub para descargar más
rápidamente:

    git clone https://github.com/esig/dss.git

Para entrar en el directorio del repositorio descargado:

    pushd dss

Para cambiar a la rama de desarrollo:

    git checkout develop

Para compilar mucho más rápido saltando todos los tests posibles excepto los
de algunas dependencias intermedias:

    pushd dss-utils
    mvn install -DskipTests
    popd

    pushd dss-crl-parser
    mvn install -DskipTests
    popd

    pushd dss-pades
    mvn install -DskipTests
    popd

Para compilar e instalar esta dependencia en el repositorio local de Maven:

    mvn install -DskipTests -Dmaven.test.skip=true

Para salir del directorio dss:

    popd

Para obtener el código fuente de Firmador, ejecutar:

    git clone https://gitlab.com/firmador/firmador.git

Para entrar en el directorio del repositorio descargado:

    pushd firmador

Para generar el JAR:

    mvn clean package

Para ejecutar el JAR:

    java -jar target/firmador.jar


## Licencia

Copyright © 2019 de los autores de Firmador.

La lista de autores de Firmador se encuentra en el fichero
[AUTHORS.md](AUTHORS.md).

Este programa es software libre, distribuido bajo la licencia GPL versión 3 o
en sus versiones posteriores.

El texto de la licencia está disponible en el fichero [COPYING](COPYING).
