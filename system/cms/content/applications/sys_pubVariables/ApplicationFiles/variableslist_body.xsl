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
<xsl:template mode="variableslist_mainbody" match="*">
<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus" /> 
<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
<xsl:variable name="componentname" select="componentname"/>
 <table width="100%" height="100%" cellpadding="0" cellspacing="3" border="0">
   <tr>
     <td width="100%" class="outerboxcell" align="right" valign="top">
       <span class="outerboxcellfont">Variables</span>
     </td>
   </tr>
   <tr class="headercell">        <!--   Repeats once per category   -->
     <td width="100%" valign="top" align="right" class="headercellfont">
       <a>
         <xsl:attribute name="href">
           <xsl:value-of select="newlink"/>
         </xsl:attribute>
            New Variable
       </a>
     </td>
   </tr>
 	<xsl:apply-templates select="category"/>
   <tr>
      <td align="center">
         <xsl:apply-templates select="/" mode="paging"/>
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
<xsl:template match="category">
   <tr class="headercell">
      <td width="100%">
          <table width="100%" cellpadding="0" cellspacing="1" border="0">
				<tr class="headercell">        <!--   Repeats once per category   -->
				  <td width="100%" valign="top" align="left" class="headercellfont" colspan="2">
						<xsl:value-of select="propname"/>
				  </td>
				  <td width="100%" valign="top" align="right" class="headercellfont" colspan="2">
					 <a>
						<xsl:attribute name="href">
						  <xsl:value-of select="newproplink"/>
						</xsl:attribute>
							Add Value
					 </a>
				  </td>
				</tr>
            <tr class="datacell1">
              <td width="5%" align="left" class="headercell2font">&nbsp;</td>
              <td width="45%" align="left" class="headercell2font">Value</td>
              <td width="25%" align="left" class="headercell2font">Site(id)</td>
              <td width="25%" align="left" class="headercell2font">Context(id)</td>
            </tr>
            <xsl:apply-templates select="varlist"/>
          </table>
      </td>
   </tr>
</xsl:template>
<xsl:template match="varlist">
	<xsl:variable name="contextinfo" select="document(contextlookupurl)"/>
	<xsl:variable name="siteinfo" select="document(sitelookupurl)"/>
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
			<xsl:when test="position()=1 and not(string-length(propid))">
				<td align="center" colspan="5" class="datacellnoentriesfound">
					No entries found.&nbsp;
            </td>
			</xsl:when>
			<xsl:otherwise>
				<td align="center" class="datacell1font">
						<a href="javascript:delConfirm('{deletelink}');"><img height="21" alt="Delete" title="Delete" src="../sys_resources/images/delete.gif" width="21" border="0"/></a>
				</td>
				<td align="left" class="datacell1font">
					 <a href="{editlink}"><xsl:apply-templates select="propertyvalue"/></a>&nbsp;
				</td>
				<td align="left" class="datacell1font">
					 <xsl:if test="string-length($siteinfo/*/@siteid)"><xsl:value-of select="$siteinfo/*/@name"/>(<xsl:value-of select="$siteinfo/*/@siteid"/>)</xsl:if>
					 <xsl:if test="not(string-length($siteinfo/*/@siteid))">Preview Site(0)</xsl:if>
					 &nbsp;
				</td>
				<td align="left" class="datacell1font">
					 <xsl:value-of select="$contextinfo/*/context/@name"/><xsl:if test="not($contextinfo/*/context/@id='')">(<xsl:value-of select="$contextinfo/*/context/@id"/>)</xsl:if>&nbsp;
				</td>
			</xsl:otherwise>
		</xsl:choose>
   </tr>
</xsl:template>
</xsl:stylesheet>
