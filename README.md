# Firmador PDF

Este es un pequeño firmador PDF creado para firmar documentos cumpliendo con la
[Política de Formatos Oficiales de los
Documentos Electrónicos Firmados Digitalmente](
https://www.mifirmadigital.go.cr/wp-content/uploads/2016/03/DCFD-Política-de-Formato-Oficial-v1.0.pdf
) de Costa Rica.

Se creó a partir de los ejemplos de la documentación de la [librería DSS](
https://ec.europa.eu/cefdigital/DSS/webapp-demo/doc/dss-documentation.html),
para uso personal del autor, pero aquí está el código fuente para quien quiera
un ejemplo de uso práctico bajo una licencia de software libre.

El ejemplo es mínimo, diseñado para funcionar con Java 8 y usando
la librería PKCS#11 de Firma Digital para GNU/Linux de 64 bits, que se espera
que esté instalada, así como los certificados de Firma Digital jerarquía SHA2
para persona física. Para conocer una forma adecuada de cómo instalar los
certificados existen un par de artículos de blog sobre cómo realizarlo en
[Fedora](https://fran.cr/instalar-firma-digital-costa-rica-linux-fedora/) y
[Ubuntu](https://fran.cr/instalar-firma-digital-costa-rica-gnu-linux-ubuntu-16-04/).

Para compilar el ejemplo se requiere Maven. Para generar el JAR se sugiere
`mvn clean package` y para ejecutarlo `java -jar target/firmador.jar`
