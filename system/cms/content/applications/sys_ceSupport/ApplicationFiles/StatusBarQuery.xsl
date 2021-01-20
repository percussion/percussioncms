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
  <xsl:variable name="this" select="/"/>
  <xsl:template match="/">
    <html>
      <head>
        <meta name="generator" content="Percussion XSpLit Version 3.0"/>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
        <title>Query Part of Status Bar</title>
      </head>

      <body>        <!--   Query Part of Status Bar   -->

        <table width="100%">
          <tr>
            <td class="headercell" width="20%">
              <div align="center">Content Title (ID)</div>
            </td>

            <td class="headercell" width="20%">
              <div align="center">Creator</div>
            </td>

            <td class="headercell" width="20%">
              <div align="center">Created on</div>
            </td>

            <td class="headercell" width="20%">
              <div align="center">Last Modifier</div>
            </td>

            <td class="headercell" width="20%">
              <div align="center">Last Modified on</div>
            </td>

          </tr>

          <xsl:apply-templates select="*" mode="mode0"/>
        </table>
        <!--   End Query Part of Status Bar   -->

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
      <tr class="datacell2">
        <td>
          <div align="center">
            <xsl:apply-templates select="ContentTitle"/>&nbsp;(
            <xsl:apply-templates select="ContentId"/>)
          </div>
        </td>

        <td>
          <div align="center">
            <xsl:apply-templates select="Creator"/>
          </div>

        </td>

        <td>
          <div align="center">
            <xsl:apply-templates select="CreatedDate"/>
          </div>

        </td>

        <td>
          <div align="center">
            <xsl:apply-templates select="LastModifier"/>
          </div>

        </td>

        <td>
          <div align="center">
            <xsl:apply-templates select="LastModifyDate"/>
          </div>

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

</xsl:stylesheet>
