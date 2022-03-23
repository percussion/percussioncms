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
<xsl:template mode="newedition_mainbody" match="*">
<xsl:variable name="configparamlookup" select="/*/configparamlookup"/>
<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus" /> 
<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
<xsl:variable name="destinationsite" select="/*/destinationsiteurl"/>
<xsl:variable name="editiontype" select="/*/editiontypeurl"/>
<xsl:variable name="sourcesite" select="/*/sourcesiteurl"/>
<xsl:variable name="componentname" select="componentname"/>
 <table width="100%" height="100%" cellpadding="0" cellspacing="3" border="0">
   <tr>
     <td class="outerboxcellfont" align="right" valign="top">Edit Edition Properties
     </td>
   </tr>
   <tr class="headercell">
      <td>
         <table width="100%" cellpadding="0" cellspacing="1" border="0">
          <form name="newedition" method="post" action="editedition.html">
            <tr class="headercell">
              <td valign="top" align="left" class="headercellfont" colspan="2">
               <xsl:if test="string-length(editionid)">Edition(id):&nbsp;<xsl:value-of select="editiontitle" />(<xsl:value-of select="editionid" />) 
               </xsl:if>&nbsp;
              </td>
            </tr>
            <input name="DBActionType" type="hidden" value="UPDATE"/>
            <input type="hidden" name="doccancelurl">
               <xsl:attribute name="value">
                  <xsl:value-of select="cancelurl"/>
               </xsl:attribute>
            </input>
            <input name="editionid" type="hidden">
              <xsl:attribute name="value">
                <xsl:value-of select="editionid"/>
              </xsl:attribute>
            </input>
            <input name="sys_componentname" type="hidden">
              <xsl:attribute name="value">
                <xsl:value-of select="componentname"/>
              </xsl:attribute>
            </input>
               <tr class="datacell1">
                 <td width="25%" align="left" class="datacell1font"><font class="reqfieldfont">*</font>Edition Name</td>
                 <td align="left" class="datacell1font">
                  <input size="30" name="editiontitle">
                    <xsl:attribute name="value">
                      <xsl:value-of select="editiontitle"/>
                    </xsl:attribute>
                  </input>
                 </td>
               </tr>
               <tr class="datacell2">
                 <td align="left" class="datacell2font">Description</td>
                 <td align="left" class="datacell2font">
                  <input size="40" name="editioncomments">
                    <xsl:attribute name="value">
                      <xsl:value-of select="editioncomments"/>
                    </xsl:attribute>
                  </input>
                 </td>
               </tr>
               <xsl:apply-templates select="document($destinationsite)" mode="dessite"/>
               <xsl:apply-templates select="document($editiontype)" mode="edtype"/>
               <tr class="datacell1">
                 <td align="left" class="datacell1font">Recovery Publication(id)</td>
                 <td align="left" class="datacell1font">
                     <input size="6" name="pubstatusid">
                        <xsl:attribute name="value">
                         <xsl:value-of select="pubstatusid"/>
                        </xsl:attribute>
                     </input>
                     &nbsp;(Recovery only)
                 </td>
               </tr>
               <xsl:apply-templates select="document($sourcesite)" mode="mrsite"/>
               <tr class="datacell1">
                 <td align="left" class="datacell1font" colspan="2">
                   <input type="button" value="Save" name="save" language="javascript" onclick="save_onclick()"/>&nbsp;
                   <input type="button" value="Cancel" name="cancel" language="javascript" onclick="cancelFunc();"/>
                 </td>
               </tr>

               <xsl:if test="string-length(editionid)">
                  <tr>
                     <td valign="top" class="headercell" colspan="2">&nbsp;
                     </td>
                  </tr>
                  <tr class="headercell">        <!--   Repeats once per category   -->
                     <td valign="top" width="25%" align="left" class="headercellfont">Edit Edition: Allowed Content</td>
                     <td valign="top" align="right" class="headercellfont">
                        <a language="javascript">
                           <xsl:attribute name="href">
                              <xsl:value-of select="newaddeditionclisturl"/>
                           </xsl:attribute>
                           <b>Add&nbsp;Content&nbsp;List</b>
                        </a>
                        &nbsp;&nbsp;&nbsp;
                     </td>
                  </tr>
                  <tr class="headercell">
                     <td valign="top" colspan="2">
                        <table width="100%" cellpadding="0" cellspacing="1" border="0">
                           <tr class="headercell2">
                              <td align="center" width="5%" class="headercell2font">&nbsp;</td>
                              <td width="50%" align="left" class="headercell2font">Content&nbsp;List&nbsp;Name&nbsp;(id)&nbsp;&nbsp;&nbsp;</td>
                              <td width="15%" align="left" class="headercell2font">Sequence&nbsp;&nbsp;&nbsp;</td>
                              <td width="15%" align="left" class="headercell2font">Authorization&nbsp;Type&nbsp;&nbsp;&nbsp;</td>
                              <td width="15%" align="left" class="headercell2font">Context&nbsp;&nbsp;&nbsp;</td>
                              <td width="10%" align="left" class="headercell2font">Preview&nbsp;&nbsp;&nbsp;</td>
                           </tr>
                           <xsl:for-each select="edition">
									<xsl:choose>
										<xsl:when test="position()=1 and string-length(./contentlistid)=0" > 
											<tr class="datacell1">
											   <td colspan="5" class="datacellnoentriesfound" align="center">No entries found.&nbsp;</td>
											   <td>&nbsp;</td>
											</tr>
										</xsl:when>
										<xsl:otherwise>
										<tr>
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
											<td class="datacell1font" align="center">
												<xsl:attribute name="id">
												<xsl:value-of select="."/>
												</xsl:attribute>
												<xsl:if test="string-length(./contentlistid)" > 
												<a href="javascript:delConfirm('{./deleteeditionclist}');"> <img height="21" alt="Delete" title="Delete" src="../sys_resources/images/delete.gif"
												width="21" border="0"/></a> 
												</xsl:if> 
											</td>
											<td class="datacell1font">
												<xsl:if test="string-length(./contentlistid)"> 
												<a>
													<xsl:attribute name="href">
														<xsl:value-of select="editaddeditionclisturl"/>
													</xsl:attribute>
													<xsl:value-of select="./contentlistname" />
												</a>
												(<xsl:value-of select="./contentlistid" />) 
												</xsl:if> 
												&nbsp;
											</td>
											<td class="datacell1font">
												<xsl:apply-templates select="editionclistseq"/>&nbsp;												
											</td>
											<td class="datacell1font">
												<xsl:variable name="authtype"><xsl:value-of select="editionclistauth"/></xsl:variable>
												<xsl:apply-templates select="document(authtypeurl)/*/item[value=$authtype]/display"/>&nbsp;
											</td>
											<td class="datacell1font">
												<xsl:value-of select="document(editionclistcont)/*/context/@name"/>&nbsp;
											</td>
											<td class="datacell1font" align="center">
											<xsl:if test="string-length(clisturl)">
											  <a>
												<xsl:attribute name="href">
													<xsl:text>javascript:previewContentList('</xsl:text>
													<xsl:value-of select="clisturl"/>
													<xsl:text>')</xsl:text>
												</xsl:attribute>
											      <img src="../sys_resources/images/preview.gif" width="21" height="21" border="0" alt="Preview Content List" title="Preview Content List"/>
  										    </a>
											</xsl:if>&nbsp; 
											</td>
										</tr>
										</xsl:otherwise>
									</xsl:choose>
                           </xsl:for-each>
                        </table>
                     </td>
                  </tr>
               </xsl:if>
           </form>
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
<xsl:template match="*" mode="dessite">
   <tr class="datacell1">
     <td align="left" class="datacell1font"><font class="reqfieldfont">*</font>Destination Site</td>
     <td align="left" class="datacell1font">
         <select name="destinationsitelist">
           <option>--Choose--</option>
             <xsl:for-each select="item">
               <option>
                 <xsl:variable name="value">
                   <xsl:value-of select="destsitevalue"/>
                 </xsl:variable>
                 <xsl:attribute name="value">
                   <xsl:value-of select="destsitevalue"/>
                 </xsl:attribute>
                 <xsl:if test="$this/newedition/destinationsitevalue=$value">
                   <xsl:attribute name="selected"/>
                 </xsl:if>
                 <xsl:apply-templates select="destsitename"/>
               </option>
             </xsl:for-each>
         </select>
     </td>
   </tr>
</xsl:template>
<xsl:template match="*" mode="edtype">
<tr class="datacell2">
   <td align="left" class="datacell2font">Edition Type</td>
   <td align="left" class="datacell2font">
      <select name="editiontypelist">
         <xsl:for-each select="item">
            <option>
              <xsl:variable name="value">
                <xsl:value-of select="value"/>
              </xsl:variable>
              <xsl:attribute name="value">
                <xsl:value-of select="value"/>
              </xsl:attribute>
              <xsl:if test="$this/newedition/editiontypevalue=$value">
                <xsl:attribute name="selected"/>
              </xsl:if>
				  <xsl:if test="$this/newedition/editiontypevalue='' and $value='2'">
                <xsl:attribute name="selected"/>
              </xsl:if>
              <xsl:apply-templates select="display"/>
            </option>
          </xsl:for-each>
      </select>
   </td>
</tr>
</xsl:template>
<xsl:template match="*" mode="mrsite">
<tr class="datacell2">
   <td align="left" class="datacell2font">Mirror Source Site</td>
   <td align="left" class="datacell2font">
      <select name="sourcesitelist">
         <option>--Choose--</option>
            <xsl:for-each select="item">
         <option>
            <xsl:variable name="value">
               <xsl:value-of select="srcsitevalue"/>
            </xsl:variable>
            <xsl:attribute name="value">
               <xsl:value-of select="srcsitevalue"/>
            </xsl:attribute>
            <xsl:if test="$this/newedition/sourcesitevalue=$value">
               <xsl:attribute name="selected"/>
            </xsl:if>
            <xsl:apply-templates select="srcsitename"/>
         </option>
         </xsl:for-each>
      </select>
      &nbsp;(Mirror only)
   </td>
</tr>
</xsl:template>
</xsl:stylesheet>
