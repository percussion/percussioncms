<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
   <!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
   %HTMLlat1;
   <!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
   %HTMLsymbol;
   <!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
   %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n">
   <xsl:template mode="editions_mainbody" match="*">
      <xsl:variable name="userroles" select="document(userrolesurl)/UserStatus"/>
      <xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
      <xsl:variable name="componentname" select="componentname"/>
      <xsl:variable name="pagename" select="pagename"/>
      <table width="100%" height="100%" cellpadding="0" cellspacing="1" border="0">
         <tr>
            <td class="outerboxcell" align="right" valign="top">
               <span class="outerboxcellfont">Copy Edition</span>
            </td>
         </tr>
         <tr class="headercell">
            <td>
               <table width="100%" cellpadding="0" cellspacing="1" border="0">
                  <form name="copyedition" method="post">
                     <input name="DBActionType" type="hidden" value="UPDATE"/>
                     <input name="sys_componentname" type="hidden" value="{$componentname}"/>
                     <input name="sys_pagename" type="hidden" value="{$pagename}"/>
                     <tr class="headercell">
                        <td valign="top" align="left" class="headercellfont" colspan="2">Source Edition</td>
                     </tr>
                     <tr class="datacell1">
                        <td width="30%" align="left" class="datacell1font">Select the Edition you want to copy</td>
                        <td align="left" class="datacell1font">
                           <select name="sourceeditionlist">
                              <xsl:for-each select="list">
                                 <option>
                                    <xsl:attribute name="value"><xsl:value-of select="./copylink"/></xsl:attribute>
                                    <xsl:value-of select="./editionname"/>
                                 </option>
                              </xsl:for-each>
                           </select>
                        </td>
                     </tr>
                     <tr class="headercell">
                        <td valign="top" align="left" class="headercellfont" colspan="2">New Edition</td>
                     </tr>
                     <tr class="datacell1">
                        <td width="30%" align="left" class="datacell1font">
                           <font class="reqfieldfont">*</font>Name</td>
                        <td align="left" class="datacell1font">
                           <input size="30" name="neweditionname"/>
                        </td>
                     </tr>
                     <tr class="datacell1">
                        <td align="left" class="datacell1font" colspan="2">
                           <input type="button" value="Create" name="create" language="javascript" onclick="create_onclick()"/>&nbsp;
<input type="button" value="Cancel" name="cancel" language="javascript" onclick="history.back();"/>
                        </td>
                     </tr>
                  </form>
               </table>
            </td>
         </tr>
         <tr class="headercell">
            <!-- Fill down to the bottom -->
            <td height="100%" width="100%">
               <img src="../sys_resources/images/invis.gif" width="1" height="1"/>
            </td>
         </tr>
      </table>
   </xsl:template>
</xsl:stylesheet>
