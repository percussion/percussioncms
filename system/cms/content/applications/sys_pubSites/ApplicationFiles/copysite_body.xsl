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
   <xsl:template mode="copysite_mainbody" match="*">
      <xsl:variable name="userroles" select="document(userrolesurl)/UserStatus"/>
      <xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
      <xsl:variable name="componentname" select="componentname"/>
      <table width="100%" height="100%" cellpadding="0" cellspacing="1" border="0">
         <tr>
            <td class="outerboxcell" align="right" valign="top">
               <span class="outerboxcellfont">Copy Site</span>
            </td>
         </tr>
         <tr class="headercell">
            <td>
               <table width="100%" cellpadding="0" cellspacing="1" border="0">
                  <form name="copysite" method="post">
                     <input name="DBActionType" type="hidden" value="UPDATE"/>
                     <input name="sys_componentname" type="hidden" value="{$componentname}"/>
                     <tr class="headercell">
                        <td valign="top" align="left" class="headercellfont" colspan="2">Souce Site</td>
                     </tr>
                     <tr class="datacell1">
                        <td width="30%" align="left" class="datacell1font">Select the Site you want to copy</td>
                        <td align="left" class="datacell1font">
                           <select name="sourcesitelist">
                              <xsl:for-each select="list">
                                 <xsl:variable name="copylink" select="./copylink"/>
                                 <xsl:variable name="ftppassword" select="./ftppassword"/>
                                 <xsl:variable name="clonesourceid" select="./clonesourceid"/>
                                 <option value="{concat($copylink, '&amp;ftppassword=', $ftppassword, '&amp;clonesourceid=', $clonesourceid)}">
                                    <xsl:value-of select="./sitename"/>
                                 </option>
                              </xsl:for-each>
                           </select>
                        </td>
                     </tr>
                     <tr class="headercell">
                        <td valign="top" align="left" class="headercellfont" colspan="2">New Site</td>
                     </tr>
                     <tr class="datacell1">
                        <td width="30%" align="left" class="datacell1font">
                           <font class="reqfieldfont">*</font>Name</td>
                        <td align="left" class="datacell1font">
                           <input size="30" name="newsitename"/>
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
