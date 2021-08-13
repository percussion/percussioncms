<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
	<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
	<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
   <xsl:output method="xml"/>
  <xsl:variable name="this" select="/"/>
  <xsl:variable name="relatedlinks" select="/*/relatedlinks"/>
  <xsl:variable name="userroles" select="/*/userrolesurl"/>
  <xsl:variable name="variantsurl" select="/*/variantsurl"/>
  <xsl:variable name="slotsurl" select="/*/slotsurl"/>
  <xsl:variable name="keywordsurl" select="/*/keywordsurl"/>
  <xsl:variable name="contenttypeurl" select="/*/contenttypeurl"/>
  <xsl:template match="/">
    <html>
      <head>
        <meta name="generator" content="Percussion XSpLit Version 3.5"/>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
        <title>New Document</title>
      </head>
      <body>
        <table height="100%" width="150" cellpadding="4" cellspacing="0" border="0">
          <xsl:attribute name="id">
            <xsl:value-of select="*/x"/>
          </xsl:attribute>

          <xsl:apply-templates select="*" mode="mode0"/>

          <tr class="outerboxcell">
            <td height="100%">&nbsp;</td>
            <!--   Fill down to the bottom   -->

          </tr>

        </table>

      </body>

    </html>

  </xsl:template>

  <xsl:template match="*">
    <xsl:choose>
      <xsl:when test="text()">
        <xsl:choose>
          <xsl:when test="@no-escaping">
            <xsl:value-of select="." disable-output-escaping="yes"/>
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
        <td height="100%" valign="top" class="outerboxcell">          <!--   Function Boxes Start   -->

          <table width="100%" cellpadding="0" cellspacing="0" class="outerboxcell" border="0">
            <xsl:attribute name="id">
              <xsl:value-of select="."/>
            </xsl:attribute>

            <tr>
              <td valign="top">                <!--   new content menu slot   -->
                <!-- begin XSL -->
			<xsl:for-each select="document($relatedlinks)/*/component">
				<xsl:copy-of select="document(url)/*/body/*" />
			</xsl:for-each>
			<!-- end XSL -->
                <!--   end content menu slot   -->
	      </td>

            </tr>
             <!--   Function Boxes End   -->
           <tr class="outerboxcell">
            <td class="datacell1font" align="center">
              <br id="XSpLit"/>(c) Percussion Software 1999-@COPYRIGHTYEAR@
            </td>
            </tr>

          </table>

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

</xsl:stylesheet>
