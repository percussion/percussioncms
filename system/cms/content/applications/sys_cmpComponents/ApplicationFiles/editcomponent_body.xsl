<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 SYSTEM "file:../../DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol SYSTEM "file:../../DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial SYSTEM "file:../../DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
<xsl:template mode="editcomponent_mainbody" match="*">
<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus" /> 
<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
<xsl:variable name="componentname" select="componentname"/>
<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
   <tr class="outerboxcell">
      <td class="outerboxcell" align="right" valign="top" colspan="2">
         <span class="outerboxcellfont">Edit Component&nbsp;&nbsp;</span>
      </td>
   </tr>
   <tr class="headercell">
     <td>
       <table width="100%" cellpadding="0" cellspacing="1" border="0">
          <form name="editcomponent" method="post">
				<xsl:attribute name="action"> 
					<xsl:choose> 
						<!--when component is created -->
						<xsl:when test="componentid=''"> 
                  <xsl:choose><xsl:when test="//sys_community=0">newcomponent_gen.html</xsl:when><xsl:otherwise>newcomponent_comm.html</xsl:otherwise></xsl:choose>
						</xsl:when> 
						<!--when component is updated -->
						<xsl:otherwise> 
							<xsl:value-of select="'updatecomponent.html'"/> 
						</xsl:otherwise> 
					</xsl:choose> 
				</xsl:attribute> 
				<input name="DBActionType" type="hidden" value="UPDATE"/>
	    <input type="hidden" name="doccancelurl">
			<xsl:attribute name="value">
				<xsl:value-of select="cancelurl"/>
			</xsl:attribute>
	    </input>
            <input name="componentid" type="hidden">
              <xsl:attribute name="value">
                <xsl:value-of select="componentid"/>
              </xsl:attribute>
            </input>
            <input name="sys_componentname" type="hidden">
              <xsl:attribute name="value">
                <xsl:value-of select="componentname"/>
              </xsl:attribute>
            </input>
         <tr class="headercell2">
           <td width="30%" align="left" class="headercell2font">Component&nbsp;ID&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
           <td width="90%" align="left" class="headercell2font">&nbsp;
             <xsl:apply-templates select="componentid"/>
           </td>
         </tr>
         <tr class="datacell1">
           <td class="datacell1font"><font class="reqfieldfont">*</font>Name</td>
           <td class="datacell1font">
            <input name="cmpname" size="30">
              <xsl:attribute name="value">
                <xsl:value-of select="cmpname"/>
              </xsl:attribute>
            </input>
           </td>
         </tr>
         <tr class="datacell1">
           <td class="datacell1font"><font class="reqfieldfont">*</font>Display Name</td>
           <td class="datacell1font">
            <input name="componentdisplayname" size="30">
              <xsl:attribute name="value">
                <xsl:value-of select="componentdisplayname"/>
              </xsl:attribute>
            </input>
           </td>
         </tr>
         <tr class="datacell2">
           <td class="datacell1font">Description</td>
           <td class="datacell1font">
            <input name="componentdesc" size="60">
              <xsl:attribute name="value">
                <xsl:value-of select="componentdesc"/>
              </xsl:attribute>
            </input>
           </td>
         </tr>
         <tr class="datacell1">
           <td class="datacell1font"><font class="reqfieldfont">*</font>URL</td>
           <td class="datacell1font">
            <input name="componenturl" size="60">
              <xsl:attribute name="value">
                <xsl:value-of select="componenturl"/>
              </xsl:attribute>
            </input>
           </td>
         </tr>
         <tr class="datacell2">
           <td class="datacell1font">Type</td>
           <td class="datacell1font">
					<select name="componenttype">
						<option value="1">
							<xsl:if test="componenttype=1"><xsl:attribute name="selected"/></xsl:if>Page Component&nbsp;
						</option>
						<option value="2">
							<xsl:if test="componenttype=2"><xsl:attribute name="selected"/></xsl:if>Page&nbsp;
						</option>
					</select>
			  </td>
         </tr>
         <tr class="datacell1">
           <td colspan="2">
             <input type="button" value="Save" class="nav_body" name="save" onclick="javascript:save_onclick()"/>&nbsp;
             <input type="button" value="Cancel" class="nav_body" name="cancel" language="javascript" onclick="cancelFunc();"/>
           </td>
         </tr>
         </form>
       </table>
		<xsl:if test="not(componentid='')">
			 <xsl:apply-templates select="document(childlookupurl)/*" mode="complookup">
	 				<xsl:with-param name="viewparenturl" select="viewparenturl"/>
			 </xsl:apply-templates>
		 </xsl:if>
		<xsl:if test="not(componentid='')">
			 <xsl:apply-templates select="document(propertylookupurl)/*" mode="propertylookup"/>
		 </xsl:if>
     </td>
   </tr>
 <tr class="headercell">
   <td height="100%">&nbsp;</td>
   <!--   Fill down to the bottom   -->
 </tr>
</table>
</xsl:template>
<xsl:template match="*" mode="complookup">
<xsl:param name="viewparenturl"/>
	<table width="100%" cellpadding="0" cellspacing="1" border="0">
		<tr class="headercell">
			<td class="headercell" align="left" valign="top" colspan="5">
				&nbsp;
			</td>
		</tr>
		<tr class="outerboxcell">
			<td class="outerboxcell" align="left" valign="top" colspan="5">
				<span class="outerboxcellfont">Child Components&nbsp;&nbsp;</span>
			</td>
		</tr>
		<tr class="headercell">        <!--   Repeats once per category   -->
		  <td valign="top" align="right" colspan="5">
			 <a>
				<xsl:attribute name="href">
				  <xsl:value-of select="addurl"/>
				</xsl:attribute>
				<b>Add&nbsp;Child&nbsp;</b>
			 </a>
			&nbsp;&nbsp;&nbsp;
		  </td>
		</tr>
		<tr class="headercell2">
		  <td width="5%" align="center" class="headercell2font">&nbsp;</td>
		  <td width="5%" align="center" class="headercell2font">&nbsp;</td>
		  <td width="40%" align="left" class="headercell2font">Name&nbsp;(ID)&nbsp;&nbsp;&nbsp;</td>
		  <td width="40%" align="left" class="headercell2font">Display&nbsp;Name&nbsp;</td>
		  <td width="10%" align="left" class="headercell2font">Sort Order&nbsp;</td>
		</tr>
		<xsl:for-each select="slot">
			<xsl:if test="not(count(list)=1 and list/componentid='')">
			<tr class="headercell2">
			  <td colspan="3" align="left" class="headercell2font">Slot Name: <xsl:value-of select="slotname"/></td>
			  <td colspan="2" align="right" class="headercell2font">
				 <a>
					<xsl:attribute name="href">
					  <xsl:value-of select="concat(/*/addurl,'&amp;slotcategoryname=',slotname)"/>
					</xsl:attribute>
					Add&nbsp;Child&nbsp;to&nbsp;Slot
				 </a>&nbsp;&nbsp;&nbsp;
			  </td>
			</tr>
			</xsl:if>
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
						<xsl:when test="count(.)=1 and componentid=''">
							<td align="center" colspan="6" class="datacellnoentriesfound">
								No entries found.&nbsp;
							</td>
						</xsl:when>
						<xsl:otherwise>
						  <td align="center" class="datacell1font">
							 <a href="javascript:delConfirm('{deleteurl}');">
								<img height="21" alt="Delete" src="../sys_resources/images/delete.gif" width="21" border="0"/>
							 </a>
						  </td>
						  <td align="center" class="datacell1font">
								 <a href="{editcomponenturl}">
									<img height="17" alt="Edit" src="../sys_resources/images/update.gif" width="17" border="0"/>
								 </a>
						  </td>
						  <td align="left" class="datacell1font">
							 <a>
								<xsl:attribute name="href">
								  <xsl:value-of select="editurl"/>
								</xsl:attribute>
								<xsl:apply-templates select="componentname"/>&nbsp;(<xsl:apply-templates select="componentid"/>)
							 </a>
						  </td>
						  <td align="left" class="datacell1font">
							 <xsl:apply-templates select="componentdisplayname"/>
						  </td>
						  <td align="left" class="datacell1font">
							 <xsl:apply-templates select="sortorder"/>
						  </td>
						</xsl:otherwise>
					</xsl:choose>
				</tr>
			</xsl:for-each>
			<tr><td class="datacell1font" colspan="5">&nbsp;</td></tr>
		</xsl:for-each>
		<tr>
			<td class="datacell1font"  colspan="5">
				<input type="button" name="parentview" value="View Parents" onClick="window.open('')">
					<xsl:attribute name="onClick">window.open(&#34;<xsl:value-of select="$viewparenturl"/>&#34;,&#34;viewparents&#34;,&#34;toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=500,height=400,z-lock=1&#34;)</xsl:attribute>
				</input>
			</td>
		</tr>
	 </table>
</xsl:template>
<xsl:template match="*" mode="propertylookup">
	<table width="100%" cellpadding="0" cellspacing="1" border="0">
		<tr class="headercell">
			<td class="headercell" align="left" valign="top" colspan="4">
				&nbsp;
			</td>
		</tr>
		<tr class="outerboxcell">
			<td class="outerboxcell" align="left" valign="top" colspan="4">
				<span class="outerboxcellfont">Component&nbsp;Properties&nbsp;</span>
			</td>
		</tr>
		<tr class="headercell"> 
		  <td valign="top" align="right" colspan="4">
			 <a>
				<xsl:attribute name="href">
				  <xsl:value-of select="addurl"/>
				</xsl:attribute>
				<b>Add&nbsp;Property&nbsp;</b>
			 </a>
			&nbsp;&nbsp;&nbsp;
		  </td>
		</tr>
		<tr class="headercell2">
		  <td width="5%" align="center" class="headercell2font">&nbsp;</td>
		  <td width="25%" align="left" class="headercell2font">Property&nbsp;Name&nbsp;</td>
		  <td width="25%" align="left" class="headercell2font">Property&nbsp;Value&nbsp;</td>
		  <td width="45%" align="left" class="headercell2font">Property&nbsp;Description&nbsp;</td>
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
					<xsl:when test="count(.)=1 and propertyname=''">
						<td align="center" colspan="6" class="datacellnoentriesfound">
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
						<a href="{editurl}">
						 <xsl:apply-templates select="propertyname"/>(<xsl:value-of select="propertyid"/>)
						</a>
					  </td>
					  <td align="left" class="datacell1font">
						 <xsl:apply-templates select="propertyvalue"/>
					  </td>
					  <td align="left" class="datacell1font">
						 <xsl:apply-templates select="propertydesc"/>
					  </td>
					</xsl:otherwise>
				</xsl:choose>
			</tr>
		</xsl:for-each>
	 </table>
</xsl:template>
</xsl:stylesheet>
