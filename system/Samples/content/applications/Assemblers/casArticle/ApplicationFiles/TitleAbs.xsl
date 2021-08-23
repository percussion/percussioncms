<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 SYSTEM "../../DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol SYSTEM "../../DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial SYSTEM "../../DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>



<xsl:stylesheet extension-element-prefixes="saxon" version="1.1" xmlns:saxon="http://icl.com/saxon" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
  <xsl:import href="file:sys_resources/stylesheets/assemblers/sys_InlineLinks.xsl"/>
  <xsl:import href="file:rx_resources/stylesheets/assemblers/rx_InlineLinks.xsl"/>
  <xsl:import href="file:sys_resources/stylesheets/assemblers/sys_Slots.xsl"/>
  <xsl:import href="file:rx_resources/stylesheets/assemblers/rx_Slots.xsl"/>
  <xsl:import href="file:sys_resources/stylesheets/assemblers/sys_ContextTemplates.xsl"/>
  <xsl:import href="file:sys_resources/stylesheets/assemblers/sys_Globals.xsl"/>
  <xsl:import href="file:rx_resources/stylesheets/assemblers/rx_Globals.xsl"/>
<!-- begin XSL -->
<xsl:output method="xml" omit-xml-declaration="yes" />
<!-- end XSL -->
  <xsl:variable name="related" select="/*/sys_AssemblerInfo/RelatedContent"/>
  <xsl:variable name="syscommand" select="//@sys_command"/>
  <xsl:variable name="this" select="/"/>
  <xsl:template match="/">
    <html>
      <head>
        <meta content="Percussion XSpLit" name="generator"/>
        <title>Article Snippet</title>
      </head>

      <body>
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
          <xsl:apply-templates select="*" mode="mode0"/>
          <xsl:apply-templates select="*/abstractcontent" mode="mode1"/>
        </table>

      </body>

    </html>

  </xsl:template>

  <xsl:template match="*">
    <xsl:choose>
      <xsl:when test="text()">
        <xsl:choose>
          <xsl:when test="@no-escaping">
            <xsl:value-of disable-output-escaping="yes" select="."/>
          </xsl:when>

          <xsl:otherwise>
            <xsl:value-of select="."/>
          </xsl:otherwise>

        </xsl:choose>

      </xsl:when>

      <xsl:otherwise>&nbsp;</xsl:otherwise>

    </xsl:choose>

    <xsl:if test="not(position()=last())">
      <br id="XSpLit"/>
    </xsl:if>

  </xsl:template>

  <xsl:template match="attribute::*">
    <xsl:value-of select="."/>
    <xsl:if test="not(position()=last())">
      <br id="XSpLit"/>
    </xsl:if>

  </xsl:template>

  <xsl:template match="*" mode="mode0">
    <xsl:for-each select=".">
      <tr>
        <td>
          <a>
            <xsl:attribute name="href">
              <xsl:value-of select="link"/>
            </xsl:attribute>

            <xsl:choose>
              <xsl:when test="$syscommand=&apos;editrc&apos;">
                <span psxedit="displaytitle">
                  <xsl:apply-templates select="displaytitle"/>
                </span>

              </xsl:when>

              <xsl:otherwise>
                <xsl:apply-templates select="displaytitle"/>
              </xsl:otherwise>

            </xsl:choose>

          </a>

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

  <xsl:template match="*/abstractcontent" mode="mode1">
    <xsl:for-each select=".">
      <tr>
        <td>
          <xsl:choose>
            <xsl:when test="$syscommand=&apos;editrc&apos;">
              <span psxedit="abstractcontent">
                <xsl:apply-templates select="."/>
              </span>

            </xsl:when>

            <xsl:otherwise>
              <xsl:apply-templates select="."/>
            </xsl:otherwise>

          </xsl:choose>

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

  <xsl:template match="*[div/@class=&apos;rxbodyfield&apos;]">
    <xsl:apply-templates mode="rxbodyfield" select="*"/>
  </xsl:template>

  <xsl:template match="sys_AssemblerInfo"/>
</xsl:stylesheet>
