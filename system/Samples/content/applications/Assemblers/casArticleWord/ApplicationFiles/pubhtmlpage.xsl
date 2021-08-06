<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
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
  <xsl:output encoding="UTF-8"/>
  <xsl:variable name="related" select="/*/sys_AssemblerInfo/RelatedContent"/>
  <xsl:variable name="syscommand" select="//@sys_command"/>
  <xsl:variable name="this" select="/"/>
  <xsl:template match="/">
    <html>
      <head>
        <meta content="Percussion XSpLit" name="generator"/>
        <title>Article for Publishing</title>
      </head>

      <body>
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
          <tr>
            <td>
              <table align="left" border="0" cellpadding="0" cellspacing="0" width="750">
                <tr>
                  <td width="100">&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>

              </table>

            </td>

          </tr>

          <xsl:apply-templates mode="mode3" select="*"/>
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

  <xsl:template match="rxslot[@template=&apos;Related Image&apos;]" mode="rxslot">
    <xsl:if test="linkurl">
      <table border="1" bordercolor="blue">
        <!--   start snippet wrapper   -->

        <xsl:apply-templates mode="rxcas-1" select="linkurl"/>
        <!--   end snippet wrapper   -->

      </table>

      <br id="XSpLit"/>
    </xsl:if>

  </xsl:template>

  <xsl:template match="linkurl" mode="rxcas-1">
    <tr>
      <td>
        <xsl:copy-of select="document(Value/@current)/*/body/*"/>
      </td>

    </tr>

  </xsl:template>

  <xsl:template match="*" mode="mode0">
    <xsl:for-each select=".">
      <tr>
        <td>
          <p>
            <font size="2">
              <xsl:choose>
                <xsl:when test="$syscommand=&apos;editrc&apos;">
                  <span psxedit="bodysource">
                    <xsl:apply-templates select="displaytitle"/>
                  </span>

                </xsl:when>

                <xsl:otherwise>
                  <xsl:apply-templates select="displaytitle"/>
                </xsl:otherwise>

              </xsl:choose>

              <br id="XSpLit"/>
              <br id="XSpLit"/>
              <i>By:
                <xsl:choose>
                  <xsl:when test="$syscommand=&apos;editrc&apos;">
                    <span psxedit="bodysource">
                      <xsl:apply-templates select="authorname"/>
                    </span>

                  </xsl:when>

                  <xsl:otherwise>
                    <xsl:apply-templates select="authorname"/>
                  </xsl:otherwise>

                </xsl:choose>

              </i>
              <br id="XSpLit"/>
              <br id="XSpLit"/>
            </font>

          </p>

          <!--   start slot RelatedImage   -->

          <xsl:variable name="rxslot-2">
            <rxslot psxeditslot="yes" slotname="Related Image" template="Related Image">
              <xsl:copy-of select="$related/linkurl[@slotname=&apos;Related Image&apos;]"/>
            </rxslot>

          </xsl:variable>

          <xsl:apply-templates mode="rxslot" select="$rxslot-2"/>
          <!--   end slot RelatedImage  -->

          <xsl:choose>
            <xsl:when test="$syscommand=&apos;editrc&apos;">
              <span psxedit="bodysource">
                <xsl:apply-templates select="bodycontent"/>
              </span>

            </xsl:when>

            <xsl:otherwise>
              <xsl:apply-templates select="bodycontent"/>
            </xsl:otherwise>

          </xsl:choose>

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

  <xsl:template match="rxslot[@template=&apos;Related Articles&apos;]" mode="rxslot">
    <xsl:if test="linkurl">
      <table border="1" bordercolor="red" cellspacing="0" width="100%">
        <tr>
          <td height="39"/>
        </tr>

        <!--   start snippet wrapper   -->

        <xsl:apply-templates mode="rxcas-3" select="linkurl"/>
        <!--   end snippet wrapper   -->

      </table>

    </xsl:if>

  </xsl:template>

  <xsl:template match="linkurl" mode="rxcas-3">
    <tr>
      <td>
        <xsl:copy-of select="document(Value/@current)/*/body/*"/>
      </td>

    </tr>

  </xsl:template>

  <xsl:template match="rxslot[@template=&apos;Hot Articles&apos;]" mode="rxslot">
    <xsl:if test="linkurl">
      <table bodercolor="green" border="1" cellspacing="0" width="100%">
        <tr>
          <td height="30"/>
        </tr>

        <!--   start snippet wrapper   -->

        <xsl:apply-templates mode="rxcas-5" select="linkurl"/>
        <!--   end snippet wrapper   -->

      </table>

    </xsl:if>

  </xsl:template>

  <xsl:template match="linkurl" mode="rxcas-5">
    <tr>
      <td>
        <xsl:copy-of select="document(Value/@current)/*/body/*"/>
      </td>

    </tr>

  </xsl:template>

  <xsl:template match="*" mode="mode1">
    <xsl:for-each select=".">
      <tr valign="top">
        <td width="500">
          <table border="0" cellpadding="5" cellspacing="0" width="100%">
            <xsl:apply-templates mode="mode0" select="."/>
          </table>

        </td>

        <td>
          <!--   start slot RelatedArticles   -->

          <xsl:variable name="rxslot-4">
            <rxslot psxeditslot="yes" slotname="Related Articles" template="Related Articles">
              <xsl:copy-of select="$related/linkurl[@slotname=&apos;Related Articles&apos;]"/>
            </rxslot>

          </xsl:variable>

          <xsl:apply-templates mode="rxslot" select="$rxslot-4"/>
          <!--   end slot RelatedArticles   -->

          <!--   start slot HotArticles   -->

          <xsl:variable name="rxslot-6">
            <rxslot psxeditslot="yes" slotname="Hot Articles" template="Hot Articles">
              <xsl:copy-of select="$related/linkurl[@slotname=&apos;Hot Articles&apos;]"/>
            </rxslot>

          </xsl:variable>

          <xsl:apply-templates mode="rxslot" select="$rxslot-6"/>
          <!--   end slot HotArticles   -->

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

  <xsl:template match="*" mode="mode2">
    <xsl:for-each select=".">
      <tr>
        <td valign="top" width="85%">
          <table border="0" cellpadding="0" cellspacing="0">
            <xsl:apply-templates mode="mode1" select="."/>
          </table>

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

  <xsl:template match="*" mode="mode3">
    <xsl:for-each select=".">
      <tr>
        <td>
          <table border="0" cellpadding="0" cellspacing="0" width="850">
            <xsl:apply-templates mode="mode2" select="."/>
          </table>

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

  <xsl:template match="*[div/@class=&apos;rxbodyfield&apos;]">
    <xsl:apply-templates mode="rxbodyfield" select="*"/>
  </xsl:template>

  <xsl:template match="sys_AssemblerInfo"/>
</xsl:stylesheet>
