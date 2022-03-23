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
<xsl:template mode="sitelist_mainbody" match="*">
<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus" /> 
<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
<xsl:variable name="componentname" select="componentname"/>
 <table width="100%" height="100%" cellpadding="0" cellspacing="1" border="0">
   <tr>
     <td width="100%" class="outerboxcell" align="right" valign="top">
       <span class="outerboxcellfont">Sites</span>
     </td>
   </tr>
   <tr class="headercell">        <!--   Repeats once per category   -->
      <td width="100%" valign="top" align="right" class="headercellfont">
         <a>
            <xsl:attribute name="href"><xsl:value-of select="newsite"/></xsl:attribute>
            New Site
         </a>&nbsp;&nbsp;&nbsp;
         <a>
            <xsl:attribute name="href"><xsl:value-of select="copysite"/></xsl:attribute>
            Copy Site
         </a>
      </td>
   </tr>
   <tr class="headercell">
      <td width="100%">
          <table width="100%" cellpadding="0" cellspacing="1" border="0">
            <tr class="datacell1">
              <td width="5%" align="left" class="headercell2font">&nbsp;</td>
              <td width="20%" align="left" class="headercell2font">Site(id)</td>
              <td width="35%" align="left" class="headercell2font">Description</td>
              <td width="15%" align="center" class="headercell2font">File Tree</td>
              <td width="25%" align="left" class="headercell2font">Last Publication Date</td>
            </tr>
            <xsl:apply-templates select="list"/>
            <tr><td align="center" colspan="5">
               <xsl:apply-templates select="/" mode="paging"/>
            </td></tr>
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
			<xsl:when test="position()=1 and not(string-length(./siteid))">
				<td align="center" colspan="5" class="datacellnoentriesfound">
					No entries found.&nbsp;
            </td>
			</xsl:when>
			<xsl:otherwise>
				<td align="center" class="datacell1font">
					 <xsl:attribute name="id">
						<xsl:value-of select="siteid"/>
					 </xsl:attribute>
					 <xsl:if test="string-length(./siteid)" > 
						<a href="javascript:delConfirm('{./sitedelete}');"> <img height="21" alt="Delete" title="Delete" src="../sys_resources/images/delete.gif"
																		  width="21" border="0"/></a> 
					 </xsl:if> 
				</td>
				<td align="left" class="datacell1font">
					<xsl:if test="string-length(./siteid)" > 
						<a href="{./sitelink}"><xsl:value-of select="./sitename" />(<xsl:value-of select="./siteid" />)</a> 
					</xsl:if> 
					&nbsp;
				</td>
				<td align="left" class="datacell1font">
					 <xsl:apply-templates select="sitedesc"/>&nbsp;
				</td>
				<td align="center" class="datacell1font">
					 <a>
						<xsl:attribute name="href">
						  <xsl:value-of select="filetree"/>
						</xsl:attribute>
						<img src="../sys_resources/images/filetree.gif" width="16" height="16" border="0" alt="File Tree" title="File Tree"/>
					 </a>
				</td>
				<td align="left" class="datacell1font">
					 <xsl:apply-templates select="lastpubdate"/>&nbsp;
				</td>
				</xsl:otherwise>
			</xsl:choose>
   </tr>
</xsl:template>
</xsl:stylesheet>
