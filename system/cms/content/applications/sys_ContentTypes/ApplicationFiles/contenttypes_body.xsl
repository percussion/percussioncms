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
<xsl:template mode="contenttypes_mainbody" match="*">
<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus" /> 
<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
<xsl:variable name="componentname" select="componentname"/>
<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
   <tr class="outerboxcell">
      <td class="outerboxcellfont" align="right" valign="top">
         Content Types
      </td>
   </tr>
   <tr class="headercell">
     <td>
       <table width="100%" cellpadding="0" cellspacing="1" border="0">
         <tr class="headercell">        <!--   Repeats once per category   -->
           <td valign="top" align="right" colspan="10">
             <a>
               <xsl:attribute name="href">
                 <xsl:value-of select="newcontenttypeurl"/>
               </xsl:attribute>
               <b>New&nbsp;Content&nbsp;Type</b>
             </a>
            &nbsp;&nbsp;&nbsp;
           </td>
         </tr>
            <tr class="headercell2">
              <td width="5%" align="center" class="headercell2font">&nbsp;</td>
              <td width="40%" align="left" class="headercell2font">Content&nbsp;Type&nbsp;Name&nbsp;(ID)&nbsp;&nbsp;&nbsp;</td>
              <td width="55%" align="left" class="headercell2font">Description&nbsp;&nbsp;&nbsp;</td>
            </tr>
         <xsl:for-each select="contenttype">
				<tr class="datacell1">        <!--   Repeats once per view row   -->
					<xsl:attribute name="class"> 
						<xsl:choose> 
							<xsl:when test="position() mod 2 = 1"> 
								<xsl:value-of select="'datacell1'"/> 
							</xsl:when> 
							<xsl:otherwise> 
								<xsl:value-of select="'datacell2'"/> 
							</xsl:otherwise> 
						</xsl:choose> 
					</xsl:attribute> 
					<xsl:choose>
						<xsl:when test="count(.)=1 and contenttypeid=''">
							<td align="center" colspan="3" class="datacellnoentriesfound">
								No entries found.&nbsp;
							</td>
						</xsl:when>
						<xsl:otherwise>
						  <td align="center" class="datacell1font">
							 <a href="javascript:delConfirm('{deletecontenttypeurl}');">
								<img height="21" alt="Delete" src="../sys_resources/images/delete.gif" width="21" border="0"/>
							 </a>
						  </td>
						  <td align="left" class="datacell1font">
							 <a>
								<xsl:attribute name="href">
								  <xsl:value-of select="editcontenttypeurl"/>
								</xsl:attribute>
								<xsl:apply-templates select="contenttypename"/>&nbsp;(
								<xsl:apply-templates select="contenttypeid"/>)
							 </a>
						  </td>
						  <td align="left" class="datacell1font">
							 <xsl:apply-templates select="contenttypedesc"/>
						  </td>
						</xsl:otherwise>
					</xsl:choose>
				</tr>
			</xsl:for-each>
         <tr>
            <td align="center" colspan="10">
               <xsl:apply-templates select="/" mode="paging"/>
            </td>
         </tr>
       </table>
     </td>
   </tr>
 <tr class="headercell">
   <td height="100%">&nbsp;</td>
   <!--   Fill down to the bottom   -->
 </tr>
</table>
</xsl:template>
   <xsl:template match="contenttype">
   </xsl:template>
</xsl:stylesheet>
