<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
   <xsl:template mode="editlookupitem_mainbody" match="*">
      <xsl:variable name="userroles" select="document(userrolesurl)/UserStatus"/>
      <xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
      <xsl:variable name="componentname" select="componentname"/>
      <table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
         <tr class="outerboxcell">
            <td class="outerboxcell" align="right" valign="top" colspan="2">
               <span class="outerboxcellfont">Edit Choice</span>
            </td>
         </tr>
         <tr class="headercell">
            <td>
               <table width="100%" cellpadding="0" cellspacing="1" border="0">
                  <form name="updatelookupitem" method="post" action="updatelookupitem.html">
                     <input name="DBActionType" type="hidden">
                        <xsl:attribute name="value"><xsl:value-of select="dbactiontype"/></xsl:attribute>
                     </input>
                     <input name="categoryid" type="hidden">
                        <xsl:attribute name="value"><xsl:value-of select="categoryid"/></xsl:attribute>
                     </input>
                     <input name="sys_componentname" type="hidden">
                        <xsl:attribute name="value"><xsl:value-of select="componentname"/></xsl:attribute>
                     </input>
							<input name="sys_pagename" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="pagename"/></xsl:attribute>
							</input>
                     <input name="doccancelurl" type="hidden">
                        <xsl:attribute name="value"><xsl:value-of select="cancelurl"/></xsl:attribute>
                     </input>
                     <input name="lookupid" type="hidden">
                        <xsl:attribute name="value"><xsl:value-of select="lookupid"/></xsl:attribute>
                     </input>
                     <input name="lookuptype" type="hidden">
                        <xsl:attribute name="value"><xsl:value-of select="lookuptype"/></xsl:attribute>
                     </input>
                     <tr class="datacell1">
                        <td width="30%" align="left" class="datacell1font">Choice&nbsp;ID&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                        <td width="90%" align="left" class="datacell1font">
                           <xsl:apply-templates select="lookupid"/>
                        </td>
                     </tr>
                     <tr class="datacell1">
                        <td align="left" class="datacell1font">
                           <font class="reqfieldfont">*</font>Choice Label</td>
                        <td align="left" class="datacell1font">
                           <input name="lookupname">
                              <xsl:attribute name="value"><xsl:value-of select="lookupname"/></xsl:attribute>
                           </input>
                        </td>
                     </tr>
                     <tr class="datacell2">
                        <td class="datacell1font">Description</td>
                        <td class="datacell1font">
                           <input name="lookupdescription" size="50" maxlength="s">
                              <xsl:attribute name="value"><xsl:value-of select="lookupdescription"/></xsl:attribute>
                           </input>
                        </td>
                     </tr>
                     <tr class="datacell2">
                        <td class="datacell1font">
                           <font class="reqfieldfont">*</font>Choice Value</td>
                        <td class="datacell1font">
                           <input name="lookupvalue" size="50" maxlength="s">
                              <xsl:attribute name="value"><xsl:value-of select="lookupvalue"/></xsl:attribute>
                           </input>
                        </td>
                     </tr>
                     <tr class="datacell1">
                        <td class="datacell1font">Sort Order</td>
                        <td class="datacell1font">
                           <input name="lookupsequence" size="5">
                              <xsl:attribute name="value"><xsl:value-of select="lookupsequence"/></xsl:attribute>
                           </input>
                        </td>
                     </tr>
                     <tr class="datacell2">
                        <td colspan="2">
                           <input type="button" value="Save" class="nav_body" name="save" language="javascript" onclick="save_onclick()"/>&nbsp;
                <input type="button" value="Cancel" class="nav_body" name="cancel" language="javascript" onclick="history.back();"/>
                        </td>
                     </tr>
                  </form>
               </table>
            </td>
         </tr>
         <tr class="headercell">
            <td height="100%">&nbsp;</td>
            <!--   Fill down to the bottom   -->
         </tr>
      </table>
   </xsl:template>
</xsl:stylesheet>
