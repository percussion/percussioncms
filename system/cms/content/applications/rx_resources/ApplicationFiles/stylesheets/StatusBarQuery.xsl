<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
  <xsl:variable name="this" select="/"/>
  <xsl:template match="StatusBarQuery" mode="statusbarquery">
    <!--   Query Part of Status Bar   -->

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

     <tr class="datacell2">
        <td>
          <div align="center">
            	<xsl:apply-templates select="ContentTitle" mode="statusbarquery"/>&#160;(<xsl:apply-templates select="ContentId" mode="statusbarquery"/>)
          </div>
        </td>

        <td>
          <div align="center">
            <xsl:apply-templates select="Creator" mode="statusbarquery"/>
          </div>

        </td>

        <td>
          <div align="center">
            <xsl:apply-templates select="CreatedDate" mode="statusbarquery"/>
          </div>

        </td>

        <td>
          <div align="center">
            <xsl:apply-templates select="LastModifier" mode="statusbarquery"/>
          </div>

        </td>

        <td>
          <div align="center">
            <xsl:apply-templates select="LastModifyDate" mode="statusbarquery"/>
          </div>

        </td>

      </tr>

     </table>
    <!--   End Query Part of Status Bar   -->

  </xsl:template>

  <xsl:template match="*" mode="statusbarquery">
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

  <xsl:template match="attribute::*"   mode="statusbarquery" >
    <xsl:value-of select="."/>
    <xsl:if test="not(position()=last())">
      <br id="XSpLit"/>
    </xsl:if>

  </xsl:template>

 </xsl:stylesheet>
