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
	<xsl:template match="dss:ValidationPolicy"/>
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
	</xsl:template>
	<xsl:template match="dss:Signature|dss:Timestamp">
		<xsl:if test="@CounterSignature = 'true'"><p>[Contrafirma]</p></xsl:if>
		<xsl:if test="dss:Filename">
			<p>
				<xsl:variable name="nodeName" select="name()"/>
				<xsl:if test="$nodeName = 'Signature'">Nombre del fichero de firma: </xsl:if>
				<xsl:if test="$nodeName = 'Timestamp'">Nombre del fichero de sello de tiempo: </xsl:if>
				<xsl:value-of select="dss:Filename"/>
			</p>
		</xsl:if>
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
			</xsl:choose></b>
		</p>
		<p>Firma
			<xsl:variable name="indicationText" select="dss:Indication/text()"/>
			<xsl:choose>
				<xsl:when test="$indicationText='TOTAL_PASSED'"><b>válida</b></xsl:when>
				<xsl:when test="$indicationText='PASSED'"><b>válida</b></xsl:when>
				<xsl:when test="$indicationText='INDETERMINATE'"><b>sin determinación</b></xsl:when>
				<xsl:when test="$indicationText='FAILED'"><b>NO válida</b></xsl:when>
				<xsl:when test="$indicationText='TOTAL_FAILED'"><b>NO válida</b></xsl:when>
			</xsl:choose>
			<xsl:if test="@SignatureFormat">, formato <xsl:value-of select="@SignatureFormat"/>.</xsl:if>
		</p>
		<xsl:apply-templates select="dss:SubIndication"/>
		<xsl:apply-templates select="dss:AdESValidationDetails"/>
		<xsl:apply-templates select="dss:Error"/>
		<xsl:apply-templates select="dss:Warning"/>
		<xsl:apply-templates select="dss:Info"/>
		<xsl:if test="dss:SigningTime">
			<p>
				Fecha declarada de la firma:
				<xsl:call-template name="formatdate">
					<xsl:with-param name="DateTimeStr" select="dss:SigningTime"/>
				</xsl:call-template>
			</p>
		</xsl:if>
		<xsl:if test="dss:ProductionTime">
			<p>
				Fecha de producción:
				<xsl:call-template name="formatdate">
					<xsl:with-param name="DateTimeStr" select="dss:ProductionTime"/>
				</xsl:call-template>
			</p>
		</xsl:if>
		<xsl:if test="dss:BestSignatureTime">
			<p>
				Fecha mínima probada de la existencia de la firma:
				<xsl:call-template name="formatdate">
					<xsl:with-param name="DateTimeStr" select="dss:BestSignatureTime"/>
				</xsl:call-template>
			</p>
		</xsl:if>
		<p></p>
	</xsl:template>
	<xsl:template match="dss:SubIndication">
		<p>Subindicación: <xsl:value-of select="."/></p>
	</xsl:template>

	<xsl:template match="dss:AdESValidationDetails|dss:QualificationDetails">
		<xsl:variable name="header">
			<xsl:choose>
				<xsl:when test="name() = 'AdESValidationDetails'">Detalles de validación AdES</xsl:when>
				<xsl:when test="name() = 'QualificationDetails'">Detalles de calificación</xsl:when>
			</xsl:choose>
		</xsl:variable>
		<dl>
			<dt>
				<xsl:value-of select="$header"/>:
			</dt>
			<dd>
				<xsl:apply-templates select="dss:Error"/>
				<xsl:apply-templates select="dss:Warning"/>
				<xsl:apply-templates select="dss:Info"/>
			</dd>
		</dl>
	</xsl:template>

	<xsl:template match="dss:Error">
		<p><b>ERROR: </b><xsl:value-of select="."/></p>
	</xsl:template>
	<xsl:template match="dss:Warning">
		<p><b>Advertencia: </b><xsl:value-of select="."/></p>
	</xsl:template>
	<xsl:template match="dss:Info">
		<p><b>Información: </b><xsl:value-of select="."/></p>
	</xsl:template>

	<xsl:template name="formatdate">
		<xsl:param name="DateTimeStr" />

		<xsl:variable name="date">
			<xsl:value-of select="substring-before($DateTimeStr,'T')" />
		</xsl:variable>

		<xsl:variable name="after-T">
			<xsl:value-of select="substring-after($DateTimeStr,'T')" />
		</xsl:variable>

		<xsl:variable name="time">
			<xsl:value-of select="substring-before($after-T,'Z')" />
		</xsl:variable>

		<xsl:choose>
			<xsl:when test="string-length($date) &gt; 0 and string-length($time) &gt; 0">
				<xsl:value-of select="concat($date,' ', $time, ' (hora UTC)')" />
			</xsl:when>
			<xsl:when test="string-length($date) &gt; 0">
				<xsl:value-of select="$date" />
			</xsl:when>
			<xsl:when test="string-length($time) &gt; 0">
				<xsl:value-of select="$time" />
			</xsl:when>
			<xsl:otherwise>-</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
