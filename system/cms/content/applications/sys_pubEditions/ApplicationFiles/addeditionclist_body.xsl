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
<xsl:template mode="addeditionclist_mainbody" match="*">
<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus" /> 
<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
<xsl:variable name="componentname" select="componentname"/>
<xsl:variable name="contentlist" select="/*/contentlisturl"/>
<xsl:variable name="sourcesite" select="/*/sourcesiteurl"/>
<xsl:variable name="authtype" select="/*/authtypeurl"/>
<xsl:variable name="contextsinfourl" select="/*/contextsinfourl"/>
  <xsl:variable name="edition" select="/*/editionurl"/>
 <table width="100%" height="100%" cellpadding="0" cellspacing="3" border="0">
   <tr>
     <td class="outerboxcell" align="right" valign="top">
       <span class="outerboxcellfont">Edition Content List</span>
     </td>
   </tr>
   <tr class="headercell">
      <td>
       <table width="100%" height="100%" cellpadding="0" cellspacing="1" border="0">
       <form name="addclist" method="post" action="addcontlist.html">
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
			<input name="sys_pagename" type="hidden">
				<xsl:attribute name="value">
					<xsl:value-of select="pagename"/>
				</xsl:attribute>
			</input>
        <input name="editionclistid" type="hidden">
           <xsl:attribute name="value">
             <xsl:value-of select="editionclistid"/>
           </xsl:attribute>
         </input>
            <xsl:apply-templates select="document($edition)" mode="edition"/>
            <xsl:choose>
               <xsl:when test="contentlistid=''">
						<xsl:variable name="list" select="document($contentlist)/extcontentlist"/>
						<xsl:comment><xsl:value-of select="$list/@excludelisturl"/></xsl:comment>
						<xsl:variable name="exclude" select="document($list/@excludelisturl)/extcontentlist/item/contentlistid"/>
					<tr class="datacell1">
					  <td width="25%" align="left" class="datacell1font"><font class="reqfieldfont">*</font>Content List</td>
					  <td align="left" class="datacell1font">
						<select name="contentlist">
						  <option selected="selected">--Choose--</option>
							 <xsl:for-each select="$list/item[not(contentlistid=$exclude)]">
								<xsl:if test="not(contentlistvalue='')">
									<option>
									  <xsl:attribute name="value">
										 <xsl:value-of select="contentlistid"/>
									  </xsl:attribute>
									  <xsl:apply-templates select="contentlistvalue"/>
									</option>
								</xsl:if>
							 </xsl:for-each>
						</select>
					  </td>
					</tr>
			         <input name="DBActionType" type="hidden" value="INSERT"/>
               </xsl:when>
               <xsl:otherwise>
                  <tr class="datacell2">
                     <td align="left" class="datacell2font">Content List</td>
                     <td align="left" class="datacell2font">
                        <xsl:value-of select="clistname"/>
                     </td>
						  <input name="contentlist" type="hidden">
							  <xsl:attribute name="value">
								 <xsl:value-of select="contentlistid"/>
							  </xsl:attribute>
							</input>
							<input name="DBActionType" type="hidden" value="UPDATE"/>
                 </tr>
               </xsl:otherwise>
            </xsl:choose>
            <tr class="datacell2">
              <td align="left" class="datacell2font">Sequence</td>
              <td align="left" class="datacell2font">
               <input size="3" type="text" name="sequence">
                 <xsl:attribute name="value">
                   <xsl:value-of select="sequence"/>
                 </xsl:attribute>
               </input>
              </td>
            </tr>
            <xsl:apply-templates select="document($authtype)" mode="atype"/>
            <xsl:apply-templates select="document($contextsinfourl)" mode="contextinfo"/>
            <tr class="datacell1">
              <td align="left" class="datacell1font" colspan="2">
                <input type="button" value="Save" name="add" language="javascript" onclick="return save_onclick()"/>&nbsp;&nbsp;
                <input type="button" value="Cancel" name="cancel" language="javascript" onclick="cancelFunc();"/>
              </td>

            </tr>
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
  <xsl:template match="*" mode="edition">
    <xsl:for-each select=".">
      <tr class="headercell">
        <td valign="top" align="left" class="headercellfont" colspan="2">Edition(id):&nbsp;
          <xsl:apply-templates select="editiontitle"/>&nbsp;(
          <xsl:apply-templates select="editionid"/>)
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>
  <xsl:template match="*" mode="contextinfo">
    <xsl:for-each select=".">
      <tr class="datacell1">
        <td align="left" class="datacell1font"><font class="reqfieldfont">*</font>Context</td>
        <td align="left" class="datacell1font">
         <select name="context">
           <option selected="selected">--Choose--</option>
             <xsl:for-each select="context">
               <option>
                 <xsl:attribute name="value">
                   <xsl:value-of select="@id"/>
                 </xsl:attribute>
                 <xsl:if test="$this/addeditionclist/context=@id">
                   <xsl:attribute name="selected"/>
                 </xsl:if>
                 <xsl:value-of select="@name"/>
               </option>
             </xsl:for-each>
         </select>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>
  <xsl:template match="*" mode="atype">
    <xsl:for-each select=".">
      <tr class="datacell1">
        <td align="left" class="datacell1font"><font class="reqfieldfont">*</font>Authorization Type</td>
        <td align="left" class="datacell1font">
         <select name="authtype">
           <option selected="selected">--Choose--</option>
             <xsl:for-each select="item">
               <option>
                 <xsl:attribute name="value">
                   <xsl:value-of select="authtypeid"/>
                 </xsl:attribute>
                 <xsl:if test="$this/addeditionclist/authtype=authtypeid">
                   <xsl:attribute name="selected"/>
                 </xsl:if>
                 <xsl:value-of select="authtypename"/>
               </option>
             </xsl:for-each>
         </select>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
