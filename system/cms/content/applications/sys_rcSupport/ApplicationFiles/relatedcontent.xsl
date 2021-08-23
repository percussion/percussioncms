<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE html [
<!ENTITY % HTMLlat1 SYSTEM "../../DTD/HTMLlat1x.ent">
		%HTMLlat1;
	<!ENTITY % HTMLsymbol SYSTEM "../../DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
	<!ENTITY % HTMLspecial SYSTEM "../../DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
  <xsl:variable name="this" select="/"/>
  <xsl:template match="/">
    <html>
      <head>
        <meta name="generator" content="Percussion XSpLit Version 3.5"/>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
        <title>Related Content Control</title>
        <link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
        <link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
        <script language="javascript">
        <![CDATA[
          function onClose()
          {
            if(window.opener && !window.opener.closed)
            {
               alert("You need to update the document to see the changes");
               self.close();
            }
          }
          function onClickEdit(url)
          {
            if(window.opener && !window.opener.closed)
            {
               window.opener.location.href = url;
            }
            self.close();
          }
          ]]>
        </script>
      </head>

      <body onload="self.focus()">
        <table width="100%" cellpadding="0" cellspacing="1" border="0">
          <tr class="headercell">
            <td class="headercell2font" align="left" width="20%">Slot(ID)</td>
            <td class="headercell2font" align="left" width="20%">Item Title(ID)</td>
            <td class="headercell2font" align="left" width="20%">Item Type(ID)</td>
            <td class="headercell2font" align="left" width="20%">Item Template(ID)</td>
            <td class="headercell2font" align="center" width="8%">Preview</td>
            <td class="headercellfont" align="center" width="12%">Action</td>
          </tr>

          <xsl:apply-templates select="*/slot" mode="mode1"/>
          <tr class="headercell">
            <td class="headercell2font" align="center" colspan="6">
               <a onclick="javascript:onClose()">
                  <xsl:attribute name="href">javascript:{}</xsl:attribute>CLOSE</a>
            </td>
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

  <xsl:template match="item" mode="mode0">
    <xsl:for-each select=".">
      <tr class="datacell1">
        <td class="datacell1font" valign="top" align="left" width="25%">
          <xsl:apply-templates select="@title"/>(
          <xsl:apply-templates select="@itemcontentid"/>)
        </td>
        <td class="datacell1font" valign="top" align="left" width="25%">
          <xsl:apply-templates select="type"/>(
          <xsl:apply-templates select="type/@typeid"/>)
        </td>
        <td class="datacell1font" valign="top" align="left" width="25%">
          <xsl:apply-templates select="variant"/>(
          <xsl:apply-templates select="variant/@variantid"/>)
        </td>
        <td class="datacell1font" valign="top" align="center" width="10%">
          <a href="http://10.10.10.156:9992/Rhythmyx/xr_casFeatures/featuresnip.html?rxcontext=0&amp;authtype=0&amp;contentid=31&amp;variantid=203" target="_blank"  rel = "noopener noreferrer">
            <img src="../sys_resources/images/eye.gif" alt="Preview" align="top" width="16" height="16" border="0"/>
          </a>

        </td>

        <td class="datacell2" width="15%">
          <a href="" onclick="javascript:alert(&apos;delete&apos;);">
            <img border="0" src="../sys_resources/images/delete.gif"/>
          </a>

          <a href="#" onclick="javascript:alert(&apos;edit&apos;);">
            <img border="0" src="../sys_resources/images/edit.gif"/>
          </a>

          <a href="#" onclick="javascript:alert(&apos;moveup&apos;);">
            <img border="0" src="../sys_resources/images/arrowup.gif"/>
          </a>

          <a href="#" onclick="javascript:alert(&apos;movedown&apos;);">
            <img border="0" src="../sys_resources/images/arrowdown.gif"/>
          </a>

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

  <xsl:template match="*/slot" mode="mode1">
    <xsl:for-each select=".">
      <tr class="datacell1">
        <td class="headercell2font" valign="middle" align="left" width="20%">
          <a>
            <xsl:attribute name="href">javascript:{}</xsl:attribute>
            <xsl:attribute name="onclick">
              <xsl:value-of select="concat('javascript:window.open(&quot;', @slotediturl, '&quot;', ',&quot;searchitems&quot;',',&quot;toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=500,height=400,z-lock=1&quot;',')')"/>
            </xsl:attribute>

            <xsl:apply-templates select="@slotname"/>(
            <xsl:apply-templates select="@slotid"/>)
          </a>
        </td>

        <td colspan="5">
          <table width="100%" cellpadding="0" cellspacing="0" border="0">
            <xsl:apply-templates select="item" mode="mode0"/>
          </table>

        </td>

      </tr>

    </xsl:for-each>

  </xsl:template>

</xsl:stylesheet>
