<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
	<!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
		%HTMLlat1;
	<!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
	<!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n">
<xsl:template mode="sitetree_mainbody" match="*">
<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus" /> 
<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
<xsl:variable name="componentname" select="componentname"/>
 <table width="100%" cellpadding="0" cellspacing="3" border="0">
   <tr>
     <td class="outerboxcell" align="right" valign="top">
       <span class="outerboxcellfont">Virtual Site Map</span>
     </td>
   </tr>
   <tr class="headerll">                                      <!--   Repeats once per view row   -->
     <td>
         <xsl:apply-templates select="/*/filetree" mode="ftree" /> 
     </td>
   </tr>
   <tr class="headercell">
     <td height="100%" width="100%">
       <img src="../sys_resources/images/invis.gif" width="1" height="1"/>
     </td>
     <!--   Fill down to the bottom   -->
   </tr>
</table>
</xsl:template>
</xsl:stylesheet>
