<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dss="http://dss.esig.europa.eu/validation/simple-report">

    <xsl:output method="html" encoding="utf-8" indent="yes" omit-xml-declaration="yes" />

    <xsl:template match="/dss:SimpleReport">
        <xsl:apply-templates/>
        <xsl:call-template name="documentInformation"/>
    </xsl:template>

    <xsl:template match="dss:DocumentName"/>
    <xsl:template match="dss:SignatureFormat"/>
    <xsl:template match="dss:SignaturesCount"/>
    <xsl:template match="dss:ValidSignaturesCount"/>
    <xsl:template match="dss:ValidationTime"/>
    <xsl:template match="dss:ContainerType"/>

    <xsl:template match="dss:Policy">
        <p>
            <!--Validation Policy : <xsl:value-of select="dss:PolicyName"/>-->
        </p>
        <p>
            <!--<xsl:value-of select="dss:PolicyDescription"/>-->
        </p>
    </xsl:template>

    <xsl:template match="dss:Signature">
        <xsl:variable name="indicationText" select="dss:Indication/text()"/>
        <xsl:variable name="idSig" select="@Id" />
        <xsl:variable name="indicationCssClass">
            <xsl:choose>
                <xsl:when test="$indicationText='TOTAL_PASSED'">success</xsl:when>
                <xsl:when test="$indicationText='INDETERMINATE'">warning</xsl:when>
                <xsl:when test="$indicationText='TOTAL_FAILED'">danger</xsl:when>
            </xsl:choose>
        </xsl:variable>

        <xsl:if test="@CounterSignature = 'true'">
            <p>Contrafirma</p>
        </xsl:if>
        <!--<p>Firma</p> <p><xsl:value-of select="$idSig" />-->
        <xsl:attribute name="class">panel-body collapse in</xsl:attribute>
        <xsl:attribute name="id">collapseSig<xsl:value-of select="$idSig" /></xsl:attribute>

        <xsl:apply-templates select="dss:Filename" />
        <xsl:apply-templates select="dss:SignatureLevel" />

        <p>Formato de firma: <xsl:value-of select="@SignatureFormat"/></p>

        <p>Indicación:
            <xsl:attribute name="class">text-<xsl:value-of select="$indicationCssClass" /></xsl:attribute>
            <xsl:choose>
                <xsl:when test="$indicationText='TOTAL_PASSED'">
                    <!--(total passed)-->
                </xsl:when>
                <xsl:when test="$indicationText='INDETERMINATE'">
                    <!--(indeterminate)-->
                </xsl:when>
                <xsl:when test="$indicationText='TOTAL_FAILED'">
                    <!--(total failed)-->
                </xsl:when>
            </xsl:choose>

            <xsl:text> </xsl:text>
            <xsl:value-of select="dss:Indication" />
        </p>

        <xsl:apply-templates select="dss:SubIndication" />

        <xsl:apply-templates select="dss:Errors" />
        <xsl:apply-templates select="dss:Warnings" />
        <xsl:apply-templates select="dss:Infos" />

        <p>Cadena de certificados:</p>
        <xsl:choose>
            <xsl:when test="dss:CertificateChain">
                <xsl:for-each select="dss:CertificateChain/dss:Certificate">
                    <xsl:variable name="index" select="position()"/>
                    <p>
                        <xsl:choose>
                            <xsl:when test="$index = 1">
                                <b><xsl:value-of select="dss:qualifiedName" /></b>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="dss:qualifiedName" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </p>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <p>/</p>
            </xsl:otherwise>
        </xsl:choose>

        <p>
            Fecha de firma declarada:
            <xsl:value-of select="dss:SigningTime"/>
        </p>

        <p>
            Fecha de firma más cercana:
            <xsl:value-of select="dss:BestSignatureTime"/>
        </p>

        <p>
            Posición de la firma:
            <xsl:value-of select="count(preceding-sibling::dss:Signature) + 1"/>
            de <xsl:value-of select="count(ancestor::*/dss:Signature)"/>
        </p>

        <xsl:for-each select="dss:SignatureScope">
            <p>Alcance de la firma:</p>
            <p><xsl:value-of select="@name"/></p>
            <p>(<xsl:value-of select="@scope"/>)</p>
            <p><xsl:value-of select="."/></p>
        </xsl:for-each>

    </xsl:template>

    <xsl:template match="dss:SignatureLevel">
            <p>Calificación:
                <xsl:value-of select="." />
                Desc <xsl:value-of select="@description" />
            </p>
    </xsl:template>

    <xsl:template match="dss:Filename">
        <dl>
            <dt>Nombre del fichero de firma:</dt>
            <dd>
                <xsl:value-of select="." />
            </dd>
        </dl>
    </xsl:template>

    <xsl:template match="dss:SubIndication">
        <p>Subindicación: <xsl:value-of select="." /></p>
    </xsl:template>

    <xsl:template match="dss:Errors">
        <p>Error: <xsl:value-of select="." /></p>
    </xsl:template>

    <xsl:template match="dss:Warnings">
        <p>Advertencia: <xsl:value-of select="." /></p>
    </xsl:template>

    <xsl:template match="dss:Infos">
        <p>Información: <xsl:value-of select="." /></p>
    </xsl:template>

    <xsl:template name="documentInformation">
        <p>Información del documento</p>
        <xsl:if test="dss:ContainerType">
            <p>
                Tipo de contenedor: <xsl:value-of select="dss:ContainerType"/>
            </p>
        </xsl:if>
        <p>Estado de las firmas:</p>
        <dd>
            <xsl:choose>
                <xsl:when test="dss:ValidSignaturesCount = dss:SignaturesCount">
                    Exitoso:
                </xsl:when>
                <xsl:otherwise>
                    Advertencia:
                </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="dss:ValidSignaturesCount"/> de <xsl:value-of select="dss:SignaturesCount"/> firmas válidas.
        </dd>
        <dl>
            <dt>Nombre del documento:</dt>
            <dd><xsl:value-of select="dss:DocumentName"/></dd>
        </dl>
    </xsl:template>
</xsl:stylesheet>
