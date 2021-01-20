<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
<xsl:template match="/">
<html>
<head>
<title> 
	<xsl:choose>
		<xsl:when test="not(//@title='')"> 
			<xsl:value-of select="//@title"/>
		</xsl:when>
		<xsl:otherwise>New Document</xsl:otherwise>
	</xsl:choose>	
</title>	
<script language="javascript">
	function redirect(url)
	{
		document.location.href=url;
	}
</script>
</head>
<body>
	<xsl:attribute name="onload">
		<xsl:text>javascript:redirect('</xsl:text><xsl:value-of select="//@editurl"/>
		<xsl:choose>
			<xsl:when test="not(//@view='')"><xsl:text>&amp;sys_view=</xsl:text><xsl:value-of select="//@view"/></xsl:when>
			<xsl:when test="not(//@userview='')"><xsl:text>&amp;sys_userview=</xsl:text><xsl:value-of select="//@userview"/></xsl:when>
		</xsl:choose>
		<xsl:text>');</xsl:text>
	</xsl:attribute>
</body>
</html>
</xsl:template>
</xsl:stylesheet>
