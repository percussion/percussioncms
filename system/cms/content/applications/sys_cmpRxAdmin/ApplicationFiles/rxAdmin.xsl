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
<xsl:output method="xml"/>
<xsl:template match="/"> 
    <html>
      <head>
        <meta name="generator" content="Percussion XSpLit Version 3.5"/>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
        <title>New Content Menu</title>
      </head>
      <body>
        <table width="100%" cellpadding="1" cellspacing="0" border="0">
		            <xsl:apply-templates select="*" mode="mode0"/>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="*" mode="mode0">
          <tr class="outerboxcellfont">
            <td align="center">
						&#160;
				</td>
          </tr>
          <tr class="outerboxcellfont ">
            <td align="left" class="outerboxcellfont">
               <img border="0" height="2" src="../sys_resources/images/invis.gif"/>
            </td>
          </tr>
  </xsl:template>
</xsl:stylesheet>
