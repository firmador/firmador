<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dss="http://dss.esig.europa.eu/validation/simple-report">
    <xsl:output method="html" encoding="utf-8" indent="yes"
        omit-xml-declaration="yes"/>
    <xsl:template match="/dss:SimpleReport">
        <xsl:call-template name="documentInformation"/>
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="dss:DocumentName"/>
    <xsl:template match="dss:SignatureFormat"/>
    <xsl:template match="dss:SignaturesCount"/>
    <xsl:template match="dss:ValidSignaturesCount"/>
    <xsl:template match="dss:ValidationTime"/>
    <xsl:template match="dss:ContainerType"/>
    <xsl:template match="dss:Policy"/>
    <xsl:template name="documentInformation">
        <p>
            El documento <xsl:value-of select="dss:DocumentName"/>
            <xsl:text> </xsl:text>
            <xsl:choose>
                <xsl:when test="dss:SignaturesCount = 0">
                    <b>no está firmado digitalmente</b>.<xsl:text> </xsl:text>
                </xsl:when>
                <xsl:when
                    test="dss:ValidSignaturesCount = dss:SignaturesCount">
                    <b>está firmado digitalmente</b>. Contiene
                    <xsl:value-of select="dss:ValidSignaturesCount"/>
                    firma(s) válida(s).
                </xsl:when>
                <xsl:otherwise>
                    contiene <xsl:value-of select="dss:SignaturesCount"/>
                    firma(s) digital(es) pero <b>se han encontrado
                    problemas</b>.
                </xsl:otherwise>
            </xsl:choose>
        </p>
        <p></p>
    </xsl:template>
    <xsl:template match="dss:Signature">
        <xsl:if test="@CounterSignature = 'true'"><p>Contrafirma</p></xsl:if>
        <xsl:apply-templates select="dss:Filename"/>
        <xsl:apply-templates select="dss:SignatureLevel"/>
        <p>Firmado por <b>
            <xsl:choose>
                <xsl:when test="dss:CertificateChain">
                    <xsl:for-each
                        select="dss:CertificateChain/dss:Certificate">
                        <xsl:variable name="index" select="position()"/>
                        <xsl:choose>
                            <xsl:when test="$index = 1">
                                <xsl:value-of select="dss:qualifiedName"/>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text> / </xsl:text>
                </xsl:otherwise>
            </xsl:choose></b>
        </p>
        <xsl:variable name="indicationText" select="dss:Indication/text()"/>
        <p>Firma:
            <xsl:choose>
                <xsl:when test="$indicationText='TOTAL_PASSED'">
                    <b>válida</b>
                </xsl:when>
                <xsl:when test="$indicationText='INDETERMINATE'">
                    <b>sin determinación</b>
                </xsl:when>
                <xsl:when test="$indicationText='TOTAL_FAILED'">
                    <b>no válida</b>
                </xsl:when>
            </xsl:choose>, formato <xsl:value-of select="@SignatureFormat"/>.
        </p>
        <xsl:apply-templates select="dss:SubIndication"/>
        <xsl:apply-templates select="dss:Errors"/>
        <xsl:apply-templates select="dss:Warnings"/>
        <xsl:apply-templates select="dss:Infos"/>
        <p>
            Fecha declarada de la firma:
            <xsl:value-of select="dss:SigningTime"/>
        </p>
        <p>
            Fecha mínima probada de la existencia de la firma:
            <xsl:value-of select="dss:BestSignatureTime"/>
        </p>
        <p></p>
    </xsl:template>
    <xsl:template match="dss:SignatureLevel">
            <p>Calificación: <xsl:value-of select="."/></p>
            <p>Descripción: <xsl:value-of select="@description"/></p>
    </xsl:template>
    <xsl:template match="dss:Filename">
        <p>Nombre del fichero de firma: <xsl:value-of select="."/></p>
    </xsl:template>
    <xsl:template match="dss:SubIndication">
        <p>Subindicación: <xsl:value-of select="."/></p>
    </xsl:template>
    <xsl:template match="dss:Errors">
        <p>Error: <xsl:value-of select="."/></p>
    </xsl:template>
    <xsl:template match="dss:Warnings">
        <p>Aviso: <xsl:value-of select="."/></p>
    </xsl:template>
    <xsl:template match="dss:Infos">
        <p>Información: <xsl:value-of select="."/></p>
    </xsl:template>
</xsl:stylesheet>
