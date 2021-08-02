<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<xsl:template mode="communityrelation_mainbody" match="*">
		<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus"/>
		<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
		<xsl:variable name="componentname" select="componentname"/>
		

		<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
			<tr class="outerboxcell">
				<td class="outerboxcell" align="right" valign="top">
					<span class="outerboxcellfont"><xsl:value-of select="reltypename"/>s&nbsp;for <a href="{communityurl}"><xsl:value-of select="communityname"/></a>(<xsl:value-of select="communityid"/>)&nbsp;Community&nbsp;</span>
				</td>
			</tr>
			<tr class="headercell">
				<td align="right" valign="top">
					<span class="headercellfont"><a href="{addurl}">Add&nbsp;<xsl:value-of select="reltypename"/>&nbsp;</a></span>
				</td>
			</tr>
			<tr class="headercell">
				<td>
					<table width="100%" cellpadding="0" cellspacing="1" border="0">
						<form name="editcommunity" method="post" action="updatecommunity.html">
							<input type="hidden" name="doccancelurl">
								<xsl:attribute name="value"><xsl:value-of select="cancelurl"/></xsl:attribute>
							</input>
							<input name="communityid" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="communityid"/></xsl:attribute>
							</input>
							<input name="sys_componentname" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="componentname"/></xsl:attribute>
							</input>
							<input name="sys_pagename" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="pagename"/></xsl:attribute>
							</input>
							<input type="hidden" name="reltypeurl" value="{reltypeurl}"/>
							<input type="hidden" name="communityname" value="{communityname}"/>
							
						     <xsl:choose>
								<xsl:when test="reltypename='Variant'">
							           <!-- for variants we want to show what content type they represent, so the xml and output layout are different -->
							           <xsl:choose>
							                <xsl:when test="contenttype/list/id=''">
											 <xsl:call-template name="NoEntries"/>
										 </xsl:when>
										 <xsl:otherwise>
										       <xsl:apply-templates mode="ShowVariants" select="contenttype"/>										                                    </xsl:otherwise>
								       </xsl:choose>
									 
								</xsl:when>
								<xsl:otherwise>
								         <xsl:choose>
									         <xsl:when test="list/id=''">
												 <xsl:call-template name="NoEntries"/>
										    </xsl:when>
										    <xsl:otherwise>
								               <!-- show column names for anything except Variant -->
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
		
						                	     <xsl:apply-templates mode="ShowOthers" select="list"/>
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
			
     <xsl:template mode="ShowOthers" match="list">

			<tr class="datacell1">
					<xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2'"/></xsl:otherwise></xsl:choose></xsl:attribute>
					<xsl:choose>
						<xsl:when test="count(.)=1 and id=''">
							<td align="center" colspan="2" class="datacellnoentriesfound">
								<xsl:if test="desc">
									<xsl:attribute name="colspan">3</xsl:attribute>
								</xsl:if>
								No entries found.&nbsp;
							</td>
						</xsl:when>
						<xsl:otherwise>
							<td align="center" class="datacell1font">
								<a href="javascript:delConfirm1('{deleteurl}');">
									<img height="21" alt="Delete" src="../sys_resources/images/delete.gif" width="21" border="0"/>
								</a>
							</td>
							<td align="left" class="datacell1font">
								<xsl:apply-templates select="name"/>&nbsp;(<xsl:apply-templates select="id"/>)
	  						</td>
							<xsl:if test="desc">
							<td align="left" class="datacell1font">
								<xsl:apply-templates select="desc"/>
	  						</td>
						</xsl:if>							
						</xsl:otherwise>
					</xsl:choose>
			</tr>

	</xsl:template>
	
	<xsl:template mode="ShowVariants" match="contenttype">
	
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
	               
				    <xsl:choose>
					  	    <xsl:when test="count(.)=1 and id=''">
							      <td align="center" colspan="2" class="datacellnoentriesfound">
									<xsl:if test="desc">
										<xsl:attribute name="colspan">3</xsl:attribute>
									</xsl:if>
									No entries found.&nbsp;
								</td>
							</xsl:when>
							<xsl:otherwise>
								<td align="center" class="datacell1font">
										<a href="javascript:delConfirm1('{deleteurl}');">
											<img height="21" alt="Delete" src="../sys_resources/images/delete.gif" width="21" border="0"/>
										</a>
								</td>
								<td align="left" class="datacell1font">
										<xsl:apply-templates select="name"/>&nbsp;(<xsl:apply-templates select="id"/>)
					                </td>
								 <xsl:if test="desc">
									     <td align="left" class="datacell1font">
											<xsl:apply-templates select="desc"/>
  									     </td>
								   </xsl:if>							
							</xsl:otherwise>
					</xsl:choose>
			</tr>
	     </xsl:for-each>
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

</xsl:stylesheet>
