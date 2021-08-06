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
   <xsl:template mode="addcommrelation_mainbody" match="*">
      <xsl:variable name="userroles" select="document(userrolesurl)/UserStatus"/>
      <xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
      <xsl:variable name="componentname" select="componentname"/>
      <table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
         <tr class="outerboxcell">
            <td class="outerboxcell" align="right" valign="top" colspan="2">
               <span class="outerboxcellfont">Add <xsl:value-of select="reltypename"/>&nbsp;for&nbsp;<a href="{communityurl}">
                     <xsl:value-of select="communityname"/>
                  </a>(<xsl:value-of select="communityid"/>) Community</span>
            </td>
         </tr>
         <tr class="headercell">
            <td>
               <table width="100%" cellpadding="0" cellspacing="1" border="0">
                  <form name="addcommrelation" method="post">
                     <xsl:attribute name="action"><xsl:value-of select="updateurl"/></xsl:attribute>
                     <input name="DBActionType" type="hidden" value="INSERT"/>
                     <input type="hidden" name="doccancelurl">
                        <xsl:attribute name="value"><xsl:value-of select="cancelurl"/></xsl:attribute>
                     </input>
                     <input name="communityid" type="hidden">
                        <xsl:attribute name="value"><xsl:value-of select="communityid"/></xsl:attribute>
                     </input>
                     <input name="communityname" type="hidden">
                        <xsl:attribute name="value"><xsl:value-of select="communityname"/></xsl:attribute>
                     </input>
                     <xsl:choose>
                        <xsl:when test="reltypename='Variants'">
                           <!-- for variants we want to show what content type they represent, so the xml and html output are different -->
                           <xsl:choose>
                              <xsl:when test="contenttype/list/id=''">
                                 <xsl:call-template name="NoEntries"/>
                              </xsl:when>
                              <xsl:otherwise>
                                 <xsl:apply-templates mode="AddVariants" select="contenttype"/>
                                 <xsl:call-template name="ShowButtons"/>
                              </xsl:otherwise>
                           </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:choose>
                              <xsl:when test="list/id=''">
                                 <xsl:call-template name="NoEntries"/>
                              </xsl:when>
                              <xsl:otherwise>
                                 <xsl:call-template name="ShowListHeader"/>
                                 <xsl:apply-templates mode="AddOthers" select="list"/>
                                 <xsl:call-template name="ShowButtons"/>
                              </xsl:otherwise>
                           </xsl:choose>
                        </xsl:otherwise>
                     </xsl:choose>
                     <tr>
                        <td align="center">
                           <xsl:attribute name="colspan"><xsl:choose><xsl:when test="list/desc">3</xsl:when><xsl:otherwise>2</xsl:otherwise></xsl:choose></xsl:attribute>
                           <xsl:apply-templates select="/" mode="paging"/>
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
   <xsl:template mode="AddVariants" match="contenttype">
      <tr class="headercell2">
         <td width="5%" align="center" class="headercell2font">&nbsp;</td>
         <xsl:choose>
            <xsl:when test="list/desc">
               <td width="45%" align="left" valign="top" class="headercell2font">
					      Content Type&nbsp;:&nbsp;<xsl:value-of select="name"/>
					      &nbsp;(<xsl:value-of select="id"/>)
					</td>
               <td width="50%" align="left" class="headercell2font">Description&nbsp;</td>
            </xsl:when>
            <xsl:otherwise>
               <td width="95%" align="left" valign="top" class="headercellfont">
					      Content Type&nbsp;:&nbsp;<xsl:value-of select="name"/>
					      &nbsp;(<xsl:value-of select="id"/>)
					</td>
            </xsl:otherwise>
         </xsl:choose>
      </tr>
      <xsl:for-each select="list">
         <tr class="datacell1">
            <td align="center" class="datacell1font">
               <input type="checkbox" name="{../../relidname}" value="{id}"/>
            </td>
            <td align="left" class="datacell1font">
               <xsl:apply-templates select="name"/>&nbsp;(<xsl:apply-templates select="id"/>)
				     </td>
            <xsl:if test="desc">
               <td align="left" class="datacell1font">
                  <xsl:apply-templates select="desc"/>
               </td>
            </xsl:if>
         </tr>
      </xsl:for-each>
   </xsl:template>
   <xsl:template mode="AddOthers" match="list">
      <tr class="datacell1">
         <!--   Repeats once per view row   -->
         <xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2'"/></xsl:otherwise></xsl:choose></xsl:attribute>
         <td align="center" class="datacell1font">
            <input type="checkbox" name="{../relidname}" value="{id}"/>
         </td>
         <td align="left" class="datacell1font">
            <xsl:apply-templates select="name"/>&nbsp;(<xsl:apply-templates select="id"/>)
				  </td>
         <xsl:if test="desc">
            <td align="left" class="datacell1font">
               <xsl:apply-templates select="desc"/>
            </td>
         </xsl:if>
      </tr>
   </xsl:template>
   <xsl:template match="commrelationlookup" mode="rellookup">
      <xsl:comment>Test</xsl:comment>
      <table width="100%" cellpadding="0" cellspacing="1" border="0">
         <tr class="headercell">
            <td class="headercell" align="left" valign="top" colspan="2">
				&nbsp;
			</td>
         </tr>
         <tr class="outerboxcell">
            <td colspan="2">
               <table width="100%" cellpadding="0" cellspacing="1" border="0">
                  <td class="outerboxcell" align="left" valign="top" width="50%">
                     <span class="outerboxcellfont">
                        <xsl:value-of select="reltypename"/>&nbsp;&nbsp;</span>
                  </td>
                  <td valign="top" align="right" width="50%">
                     <a>
                        <xsl:attribute name="href"><xsl:value-of select="Addurl"/></xsl:attribute>
                        <b>Add</b>
                     </a>
					&nbsp;&nbsp;&nbsp;
					</td>
               </table>
            </td>
         </tr>
         <tr class="headercell2">
            <td width="5%" align="center" class="headercell2font">&nbsp;</td>
            <td width="95%" align="left" class="headercell2font">Name&nbsp;(ID)&nbsp;&nbsp;&nbsp;</td>
         </tr>
         <xsl:for-each select="list">
            <tr class="datacell1">
               <!--   Repeats once per view row   -->
               <xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2'"/></xsl:otherwise></xsl:choose></xsl:attribute>
               <xsl:choose>
                  <xsl:when test="count(.)=1 and id=''">
                     <td align="center" colspan="2" class="datacellnoentriesfound">
							No entries found.&nbsp;
						</td>
                  </xsl:when>
                  <xsl:otherwise>
                     <td align="center" class="datacell1font">
                        <a href="javascript:delConfirm('{deleteurl}');">
                           <img height="21" alt="Delete" src="../sys_resources/images/delete.gif" width="21" border="0"/>
                        </a>
                     </td>
                     <td align="left" class="datacell1font">
                        <xsl:apply-templates select="name"/>&nbsp;(<xsl:apply-templates select="id"/>)
					         </td>
                  </xsl:otherwise>
               </xsl:choose>
            </tr>
         </xsl:for-each>
      </table>
   </xsl:template>
   <xsl:template name="NoEntries">
      <tr class="datacell1">
         <td align="center" colspan="2" class="datacellnoentriesfound">
            <xsl:if test="list/desc">
               <xsl:attribute name="colspan">3</xsl:attribute>
            </xsl:if>
							No entries found.&nbsp;
					</td>
      </tr>
   </xsl:template>
   <xsl:template name="ShowListHeader">
      <tr class="headercell2">
         <td width="5%" align="center" class="headercell2font">&nbsp;</td>
         <xsl:choose>
            <xsl:when test="list/desc">
               <td width="45%" align="left" class="headercell2font">Name&nbsp;(ID)&nbsp;&nbsp;&nbsp;</td>
               <td width="50%" align="left" class="headercell2font">Description&nbsp;</td>
            </xsl:when>
            <xsl:otherwise>
               <td width="95%" align="left" class="headercell2font">Name&nbsp;(ID)&nbsp;&nbsp;&nbsp;</td>
            </xsl:otherwise>
         </xsl:choose>
      </tr>
   </xsl:template>
   <xsl:template name="ShowButtons">
      <tr class="datacell1">
         <td colspan="2">
            <xsl:if test="list/desc">
               <xsl:attribute name="colspan">3</xsl:attribute>
            </xsl:if>
            <input type="button" value="Save" class="nav_body" name="save" onclick="javascript:save_onclick()"/>&nbsp;
			<input type="button" value="Cancel" class="nav_body" name="cancel" language="javascript" onclick="cancelFunc();"/>&nbsp;
			<input type="button" value="Select All" onclick="javascript:setChecked(1);" name="B3"/>&nbsp;
			<input type="button" value="Clear All" onclick="javascript:setChecked(0)" name="ResetButton"/>
         </td>
      </tr>
   </xsl:template>
</xsl:stylesheet>
