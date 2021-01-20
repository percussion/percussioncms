<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!--Action visibility contexts document becomes too big, if the clients have several communities and roles and causes the CX to fail to load sometimes.
	For some clients the documents are  of order of 12MB. 
	This xsl strips PSXKey elements from the document and strips the state attribute.
	Creates a vc element for each PSXActionVisibilityContexts element with name as attribute and comma separated list of values.
	If the value consists of comma character in it, it will be replaced with #RXC#. The consumer has to replace this with comma character before using the data.

The dtd of xml document where this xsl applied is expected to be.
<?xml version='1.0' encoding='UTF-8'?>
<!ELEMENT PSXVisibilityContextEntry (PSXKey, Value, Description? )>
<!ELEMENT Description (#PCDATA)>
<!ELEMENT Value (#PCDATA)>
<!ELEMENT PSXKey (VISIBILITYCONTEXT, VALUE, ACTIONID )>
<!ELEMENT ACTIONID (#PCDATA)>
<!ELEMENT VALUE (#PCDATA)>
<!ELEMENT VISIBILITYCONTEXT (#PCDATA)>
<!ATTLIST  PSXVisibilityContextEntry state CDATA #REQUIRED>
<!ATTLIST  PSXVisibilityContextEntry propName CDATA #REQUIRED>
<!ATTLIST  PSXVisibilityContextEntry keyControl CDATA #REQUIRED>
<!ELEMENT PSXActionVisibilityContext (PSXVisibilityContextEntry+ )>
<!ATTLIST  PSXActionVisibilityContext state CDATA #REQUIRED>
<!ATTLIST  PSXActionVisibilityContext propName CDATA #REQUIRED>
<!ATTLIST PSXActionVisibilityContext ordered (yes | no ) "no">
<!ATTLIST  PSXActionVisibilityContext className CDATA #IMPLIED>
<!ELEMENT PSXActionVisibilityContexts (PSXActionVisibilityContext )>
<!ATTLIST  PSXActionVisibilityContexts state CDATA #REQUIRED>
<!ATTLIST  PSXActionVisibilityContexts ordered CDATA #REQUIRED>
<!ATTLIST  PSXActionVisibilityContexts className CDATA #REQUIRED>
<!ELEMENT Action (PSXActionVisibilityContexts? )>
<!ATTLIST  Action actionid CDATA #REQUIRED>
<!ELEMENT ActionSet (Action* )>

The output xml dtd after applying the this xsl will be
<!ELEMENT ActionSet (Action* )>
<!ELEMENT Action (vc? )>
<!ATTLIST  Action actionid CDATA #REQUIRED>
<!ELEMENT vc (#PCDATA)>
<!ATTLIST  vc name CDATA #REQUIRED>
vc consists comma separated list of values.
	-->
	<xsl:template match="/">
		<xsl:apply-templates mode="copy"/>
	</xsl:template>
	<!-- copy any attribute or template -->
	<xsl:template match="@*|*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXKey" mode="copy"/>
	<xsl:template match="PSXActionVisibilityContexts" mode="copy">
		<vc name="{PSXActionVisibilityContext/@propName}">
			<xsl:for-each select="PSXActionVisibilityContext/PSXVisibilityContextEntry">
				<xsl:call-template name="replace-string">
					<xsl:with-param name="text"><xsl:value-of select="Value"/></xsl:with-param>
					<xsl:with-param name="replace">,</xsl:with-param>
					<xsl:with-param name="with">#RXC#</xsl:with-param>
				</xsl:call-template>
				<xsl:if test="position()!=last()">
					<xsl:text>,</xsl:text>
				</xsl:if>
			</xsl:for-each>
		</vc>
	</xsl:template>
	<xsl:template match="@state" mode="copy"/>
	<xsl:template name="replace-string">
		<xsl:param name="text"/>
		<xsl:param name="replace"/>
		<xsl:param name="with"/>
		<xsl:variable name="stringText" select="string($text)"/>
		<xsl:choose>
			<xsl:when test="contains($stringText,$replace)">
				<xsl:value-of select="substring-before($stringText,$replace)"/>
				<xsl:value-of select="$with"/>
				<xsl:call-template name="replace-string">
					<xsl:with-param name="text" select="substring-after($stringText,$replace)"/>
					<xsl:with-param name="replace" select="$replace"/>
					<xsl:with-param name="with" select="$with"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$stringText"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
