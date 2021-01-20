<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
	<!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
		%HTMLlat1;
	<!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
	<!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
  <xsl:template match="*" mode="pubstatus">
    <xsl:param name="link">/Rhythmyx/sys_pubHandler/publisher.htm?editionid=<xsl:value-of select="@editionid"/>&amp;PUBAction=</xsl:param>
    <table border="0" cellpadding="0" cellspacing="2">
      <tr>
        <xsl:if test="response/@code = 'inProgress'">
          <td>
            <a>
              <xsl:attribute name="href"><xsl:value-of select="$link"/>status</xsl:attribute>
                <img alt="Status" title="Status" src="../sys_resources/images/status.gif" border="0"/>
            </a>
          </td>
          <td>
            <a>
              <xsl:attribute name="href"><xsl:value-of select="$link"/>stop</xsl:attribute>
                <img alt="Stop" title="Stop" src="../sys_resources/images/stop.gif" border="0"/>
            </a>
          </td>
        </xsl:if>
        <xsl:if test="response/@code != 'inProgress'">
          <td>
          <a>
            <xsl:attribute name="href"><xsl:value-of select="$link"/>publish</xsl:attribute>
              <img alt="Publish" title="Publish" src="../sys_resources/images/publish.gif" border="0"/>
            </a>
          </td>
          <td>&nbsp;</td>
          </xsl:if>
      </tr>
    </table>
  </xsl:template>
</xsl:stylesheet>
