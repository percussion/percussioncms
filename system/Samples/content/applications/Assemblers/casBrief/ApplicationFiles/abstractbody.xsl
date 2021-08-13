<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="xalan://com.percussion.i18n.PSI18nUtils"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
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
        <title>This is the Brief Variant</title>
      </head>

      <body>
        <table>
          <xsl:apply-templates mode="mode0" select="*/displaytitle"/>
          <xsl:apply-templates mode="mode1" select="*/bodycontent"/>
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

  <xsl:template match="*/displaytitle" mode="mode0">
    <xsl:for-each select=".">
      <tr>
        <td>
          <font face="arial, helvetica" size="2">
            <b>
              <u>
                <xsl:choose>
                  <xsl:when test="$syscommand=&apos;editrc&apos;">
                    <span psxedit="displaytitle">
                      <xsl:apply-templates select="."/>
                    </span>

                  </xsl:when>

                  <xsl:otherwise>
                    <xsl:apply-templates select="."/>
                  </xsl:otherwise>

                </xsl:choose>

              </u>

            </b>

          </font>

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

  <xsl:template match="*/bodycontent" mode="mode1">
    <xsl:for-each select=".">
      <tr>
        <td>
          <font face="arial, helvetica" size="1">
            <xsl:choose>
              <xsl:when test="$syscommand=&apos;editrc&apos;">
                <span psxedit="bodycontent">
                  <xsl:apply-templates select="."/>
                </span>

              </xsl:when>

              <xsl:otherwise>
                <xsl:apply-templates select="."/>
              </xsl:otherwise>

            </xsl:choose>

          </font>

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

  <xsl:template match="*[div/@class=&apos;rxbodyfield&apos;]">
    <xsl:apply-templates mode="rxbodyfield" select="*"/>
  </xsl:template>

  <xsl:template match="sys_AssemblerInfo"/>
</xsl:stylesheet>
