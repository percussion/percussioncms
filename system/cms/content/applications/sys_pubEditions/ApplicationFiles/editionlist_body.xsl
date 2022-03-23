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
      <table width="100%" height="100%" cellpadding="0" cellspacing="1" border="0">
         <tr>
            <td class="outerboxcell" align="right" valign="top">
               <span class="outerboxcellfont">Editions</span>
            </td>
         </tr>
         <tr class="headercell">
            <!--   Repeats once per category   -->
            <td width="100%" valign="top" align="right" class="headercellfont">
               <a>
                  <xsl:attribute name="href"><xsl:value-of select="newedition"/></xsl:attribute>
                  New Edition
               </a>&nbsp;&nbsp;&nbsp;
               <a>
                  <xsl:attribute name="href"><xsl:value-of select="copyedition"/></xsl:attribute>
                  Copy Edition
               </a>
            </td>
         </tr>
         <tr class="headercell">
            <td>
               <table width="100%" cellpadding="0" cellspacing="1" border="0">
                  <tr class="datacell1">
                     <td width="5%" align="center" class="headercell2font">&nbsp;</td>
                     <td width="15%" align="center" class="headercell2font">Publish</td>
                     <td width="15%" align="left" class="headercell2font">Edition&nbsp;Name&nbsp;(id)&nbsp;&nbsp;&nbsp;&nbsp;</td>
                     <td width="45%" align="left" class="headercell2font">Description&nbsp;&nbsp;&nbsp;&nbsp;</td>
                     <td width="15%" align="left" class="headercell2font">Destination&nbsp;Site&nbsp;(id)&nbsp;&nbsp;</td>
                     <td width="15%" align="left" class="headercell2font">Edition&nbsp;Type</td>
                  </tr>
                  <xsl:apply-templates select="list"/>
                  <tr>
                     <td align="center" colspan="6">
                        <xsl:apply-templates select="/" mode="paging"/>
                     </td>
                  </tr>
               </table>
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
   <xsl:template match="list">
      <tr class="datacell1">
         <xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2'"/></xsl:otherwise></xsl:choose></xsl:attribute>
         <xsl:choose>
            <xsl:when test="position()=1 and not(string-length(./editionid))">
               <td align="center" colspan="6" class="datacellnoentriesfound">
					No entries found.&nbsp;
            </td>
            </xsl:when>
            <xsl:otherwise>
               <td align="center" class="datacell1font">
                  <xsl:if test="string-length(./editionid)">
                     <a href="javascript:delConfirm('{editiondelete}');">
                        <img height="21" alt="Delete" title="Delete" src="../sys_resources/images/delete.gif" width="21" border="0"/>
                     </a>
                  </xsl:if>
               </td>
               <td align="center" class="datacell1font">
                  <xsl:apply-templates select="document(./editionstatuslink)/*" mode="pubstatus"/>
               </td>
               <td align="left" class="datacell1font">
                  <xsl:if test="string-length(./editionid)">
                     <a href="{editionlink}">
                        <xsl:value-of select="./editionname"/>&nbsp;(<xsl:value-of select="./editionid"/>)</a>
                  </xsl:if> 
					&nbsp;
				</td>
               <td align="left" class="datacell1font">
                  <xsl:apply-templates select="editiondesc"/>
               </td>
               <td align="left" class="datacell1font">
                  <xsl:choose>
                     <xsl:when test="string-length(./destsiteid)">
                        <xsl:apply-templates select="destsite"/>&nbsp;(
         					   <xsl:apply-templates select="destsiteid"/>)
         					</xsl:when>
                     <xsl:otherwise>Not Assigned</xsl:otherwise>
                  </xsl:choose>
               </td>
               <td align="center" class="datacell1font">
                  <xsl:choose>
                     <xsl:when test="./edtype='1'">
                        <img src="../sys_resources/images/pubmanual.gif" width="16" height="16" border="0" alt="Manual" title="Manual"/>
                     </xsl:when>
                     <xsl:when test="./edtype='2'">
                        <img src="../sys_resources/images/pubautomatic.gif" width="16" height="16" border="0" alt="Automatic" title="Automatic"/>
                     </xsl:when>
                     <xsl:when test="./edtype='3'">
                        <img src="../sys_resources/images/pubrecovery.gif" width="16" height="16" border="0" alt="Recovery" title="Recovery"/>
                     </xsl:when>
                     <xsl:when test="./edtype='4'">
                        <img src="../sys_resources/images/pubmirror.gif" width="16" height="16" border="0" alt="Mirror" title="Mirror"/>
                     </xsl:when>
                  </xsl:choose>
               </td>
            </xsl:otherwise>
         </xsl:choose>
      </tr>
   </xsl:template>
</xsl:stylesheet>
