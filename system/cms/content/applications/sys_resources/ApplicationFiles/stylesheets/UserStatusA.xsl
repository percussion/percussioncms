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
  
  <xsl:template match="UserStatus" mode="UserLogin">

<table height="125" width="100%" border="0" cellpadding="4" cellspacing="0">
  <tr>
    <td class="outerboxcell"> 
      <table height="100%" width="150" border="0" cellspacing="0" cellpadding="0" class="backgroundcolor">
        <xsl:attribute name="pssessionid"><xsl:value-of select="@pssessionid" /></xsl:attribute> 
        <xsl:attribute name="sessiond"><xsl:value-of select="@sessionid" /></xsl:attribute> 
		<tr class="outerboxcell">
          <td align="center" class="outerboxcellfont" height="34">Rhythmyx<br />Login Details</td>
        </tr>
        <tr class="outerboxcell">
          <td class="backgroundcolor"> 
            <table width="150" height="49" border="0" cellpadding="0" cellspacing="1">
              <tr> 
                <td class="headercell" height="23" align="center">&nbsp;<span class="headercellfont">User:</span>&nbsp;</td>
                <td class="headercell" align="center">&nbsp;<span class="headercellfont"><xsl:value-of select="UserName"/></span>&nbsp;</td>
              </tr>
              <tr> 
                <td class="headercell" height="23" align="center">&nbsp;<span class="headercellfont">Roles:</span>&nbsp;</td>
                <td class="headercell" align="center">&nbsp;<span class="headercellfont"><xsl:value-of select="UserRoles" /></span>&nbsp;</td>
              </tr>
            </table>
          </td>
        </tr>
        <tr class="outerboxcell"> 
          <td align="center" class="headercellfont" height="34"><xsl:value-of select="UserTime" />&nbsp;</td>
        </tr>
      </table>
    </td>
  </tr>
</table>
</xsl:template>

<xsl:template name="firstrole" match="Role[position()=1]" mode="rolelist"><xsl:value-of select="."/></xsl:template>

<xsl:template match="Role" mode="rolelist">,<xsl:value-of select="."/></xsl:template>

</xsl:stylesheet>
