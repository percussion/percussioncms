<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
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

<xsl:template match="rxslot[@template='Zigzag Links' and not(linkurl)]" mode="rxslot">
    <xsl:comment>Empty ZigZag Links Slot</xsl:comment>
</xsl:template>

<xsl:template match="rxslot[@template='Zigzag Links' and linkurl]" mode="rxslot" >
    <xsl:comment>Start ZigZag Links Template</xsl:comment>
    <table width="300" border="1">
       <xsl:for-each select="linkurl[(position() mod 2) = 1]" >
          <tr>
            <td align="left">
              <xsl:copy-of select="document(Value/@current)/*/body/*" />
            </td>
          </tr>
          <xsl:apply-templates select="following-sibling::linkurl[1]" mode="rightlink" />
       </xsl:for-each>
    </table>
    <xsl:comment>End ZigZag Links Template</xsl:comment>
</xsl:template>

<xsl:template match="linkurl" mode="rightlink">
   <tr>
     <td align="right">
        <xsl:copy-of select="document(Value/@current)/*/body/*" />
     </td>
   </tr>
</xsl:template>

<!-- end XSL -->
  <xsl:variable name="related" select="/*/sys_AssemblerInfo/RelatedContent"/>
  <xsl:variable name="syscommand" select="//@sys_command"/>
  <xsl:variable name="this" select="/"/>
  <xsl:template match="/">
    <html>
      <head>
        <meta content="Percussion XSpLit" name="generator"/>
        <title>Related item Snippet</title>
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

          <xsl:apply-templates mode="mode5" select="*"/>
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

  <xsl:template match="displaytitle" mode="mode0">
    <xsl:for-each select=".">
      <tr>
        <td>
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

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

  <xsl:template match="abstractcontent" mode="mode1">
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

  <xsl:template match="rxslot[@template=&apos;Related Articles&apos;]" mode="rxslot">
    <xsl:if test="linkurl">
      <!--   start snippet wrapper   -->

      <xsl:apply-templates mode="rxcas-5" select="linkurl"/>
      <!--   end snippet wrapper   -->

    </xsl:if>

  </xsl:template>

  <xsl:template match="linkurl" mode="rxcas-5">
    <span id="psx-RelatedItems/sys_AssemblerInfo" psxeditslot="yes" slotname="Related Articles" template="SimpleTableSlot">Related Articles</span>
  </xsl:template>

  <xsl:template match="*" mode="mode2">
    <xsl:for-each select=".">
      <tr valign="top">
        <td>
          <table width="100%" border="0" cellspacing="0" cellpadding="5">
            <xsl:apply-templates select="displaytitle" mode="mode0"/>
            <xsl:apply-templates select="abstractcontent" mode="mode1"/>
            <tr>
              <td>
                <!--   start slot Related Articles   -->

                <xsl:variable name="rxslot-6">
                  <rxslot psxeditslot="yes" slotname="Related Articles" template="SimpleTableSlot">
                    <xsl:copy-of select="$related/linkurl[@slotname=&apos;Related Articles&apos;]"/>
                  </rxslot>

                </xsl:variable>

                <xsl:apply-templates mode="rxslot" select="$rxslot-6"/>
                <!--   end slot Releated Articles   -->

              </td>

            </tr>

          </table>

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

  <xsl:template match="*" mode="mode3">
    <xsl:for-each select=".">
      <tr valign="top">
        <td colspan="4">
          <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <xsl:apply-templates mode="mode2" select="."/>
          </table>

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

  <xsl:template match="rxslot[@template=&apos;Related Links&apos;]" mode="rxslot">
    <xsl:if test="linkurl">
      <!--   start snippet wrapper   -->

      <xsl:apply-templates mode="rxcas-7" select="linkurl"/>
      <!--   end snippet wrapper   -->

    </xsl:if>

  </xsl:template>

  <xsl:template match="linkurl" mode="rxcas-7">
    <span id="psx-RelatedItems/sys_AssemblerInfo" psxeditslot="yes" slotname="Related Links" template="Zigzag Links">Related Links</span>
  </xsl:template>

  <xsl:template match="*" mode="mode4">
    <xsl:for-each select=".">
      <tr>
        <td valign="top" width="65%">
          <table border="0" cellpadding="0" cellspacing="0">
            <xsl:apply-templates mode="mode3" select="."/>
          </table>

        </td>

        <td valign="top" width="20%">
          <!--   start slot Related Articles   -->

          <xsl:variable name="rxslot-8">
            <rxslot psxeditslot="yes" slotname="Related Links" template="Zigzag Links">
              <xsl:copy-of select="$related/linkurl[@slotname=&apos;Related Links&apos;]"/>
            </rxslot>

          </xsl:variable>

          <xsl:apply-templates mode="rxslot" select="$rxslot-8"/>
          <!--   end slot Releated Articles   -->

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

  <xsl:template match="*" mode="mode5">
    <xsl:for-each select=".">
      <tr>
        <td>
          <table border="0" cellpadding="0" cellspacing="0" width="750">
            <xsl:apply-templates mode="mode4" select="."/>
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
