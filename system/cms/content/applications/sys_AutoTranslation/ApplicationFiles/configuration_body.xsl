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
<xsl:template mode="configuration_mainbody" match="*">
<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
   <tr class="outerboxcell">
      <td class="outerboxcellfont" align="right" valign="top">
         Auto Translations
      </td>
   </tr>
   <tr class="headercell">
     <td>
       <table width="100%" cellpadding="0" cellspacing="1" border="0">
         <tr class="headercell">        <!--   Repeats once per category   -->
           <td valign="top" align="right" colspan="10">
             <a>
               <xsl:attribute name="href">
                 <xsl:value-of select="//addconfigurl"/>
               </xsl:attribute>
               <b>New&nbsp;Configuration</b>
             </a>
            &nbsp;&nbsp;&nbsp;
           </td>
         </tr>
            <tr class="headercell2">
              <td width="5%" align="center" class="headercell2font">&nbsp;</td>
              <td width="25%" align="left" class="headercell2font">Content&nbsp;Type&nbsp;(ID)</td>
              <td width="20%" align="left" class="headercell2font">Locale</td>
              <td width="25%" align="left" class="headercell2font">Community&nbsp;(ID)</td>
              <td width="25%" align="left" class="headercell2font">Workflow&nbsp;(ID)</td>
            </tr>
         <xsl:for-each select="list">
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
							<td align="center" colspan="6" class="datacellnoentriesfound">
								No entries found.&nbsp;
							</td>
						</xsl:when>
						<xsl:otherwise>
						  <td align="center" class="datacell1font">
							 <a href="javascript:delConfirm('{deleteconfigurl}');">
								<img height="21" alt="Delete" src="../sys_resources/images/delete.gif" width="21" border="0"/>
							 </a>
						  </td>
						  <td align="left" class="datacell1font">
							 <a>
								<xsl:attribute name="href">
								  <xsl:value-of select="editconfigurl"/>
								</xsl:attribute>
								<xsl:apply-templates select="contenttypename"/>&nbsp;(<xsl:apply-templates select="contenttypeid"/>)
							 </a>
						  </td>
						  <td align="left" class="datacell1font">
							 <xsl:apply-templates select="localename"/>
						  </td>
						  <td align="left" class="datacell1font">
								<xsl:apply-templates select="communityname"/>&nbsp;(<xsl:apply-templates select="communityid"/>)
						  </td>
						  <td align="left" class="datacell1font">
								<xsl:apply-templates select="workflowname"/>&nbsp;(<xsl:apply-templates select="workflowid"/>)
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
</xsl:stylesheet>
