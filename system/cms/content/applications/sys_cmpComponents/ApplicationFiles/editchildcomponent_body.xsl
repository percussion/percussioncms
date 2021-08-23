<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY % HTMLlat1 SYSTEM "../../DTD/HTMLlat1x.ent">
		%HTMLlat1;
	<!ENTITY % HTMLsymbol SYSTEM "../../DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
	<!ENTITY % HTMLspecial SYSTEM "../../DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
<xsl:template mode="editchildcomponent_mainbody" match="*">
<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus" /> 
<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
<xsl:variable name="componentname" select="componentname"/>
<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
   <tr class="outerboxcell">
      <td class="outerboxcell" align="right" valign="top" colspan="2">
         <span class="outerboxcellfont">Edit Child Component&nbsp;&nbsp;</span>
      </td>
   </tr>
   <tr class="headercell">
     <td>
       <table width="100%" cellpadding="0" cellspacing="1" border="0">
          <form name="editchildcomponent" method="post" action="updatechild.html">
            <input name="DBActionType" type="hidden" value="UPDATE"/>
				<input type="hidden" name="doccancelurl" value="{cancelurl}"/>
            <input name="componentid" type="hidden" value="{componentid}"/>
            <input name="sys_componentname" type="hidden" value="{componentname}"/>
            <input name="sys_pagename" type="hidden" value="{pagename}"/>
            <input name="childcomponentid" type="hidden" value="{childcomponentid}"/>
         <tr class="datacell1">
           <td class="datacell1font"><font class="reqfieldfont">*</font>Name</td>
           <td class="datacell1font">
					<xsl:choose>
						<xsl:when test="childcomponentid=''">
							<input type="hidden" name="neworold" value="new"/>
							<xsl:apply-templates select="document(componentlookup)/*" mode="complookup">
								<xsl:with-param name="childcompid" select="childcomponentid"/>
							</xsl:apply-templates>
						</xsl:when>
						<xsl:otherwise>
							<input type="hidden" name="neworold" value="old"/>
							<xsl:value-of select="document(componentlookup)/*/item/componentname"/>
						</xsl:otherwise>
					</xsl:choose>
           </td>
         </tr>
         <tr class="datacell1">
           <td class="datacell1font">Slot Name</td>
           <td class="datacell1font">
            <input name="slotname" size="30">
              <xsl:attribute name="value">
					 <xsl:choose>
						<xsl:when test="not(slotname='')">
							<xsl:value-of select="slotname"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="slotcategoryname"/>
						</xsl:otherwise>
					 </xsl:choose>
              </xsl:attribute>
            </input>
           </td>
         </tr>
         <tr class="datacell2">
           <td class="datacell1font">Sort Order</td>
           <td class="datacell1font">
            <input name="sortorder" size="10">
              <xsl:attribute name="value">
                <xsl:value-of select="sortorder"/>
              </xsl:attribute>
            </input>
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
     </td>
   </tr>
 <tr class="headercell">
   <td height="100%">&nbsp;</td>
   <!--   Fill down to the bottom   -->
 </tr>
</table>
</xsl:template>

<xsl:template match="*" mode="complookup">
<xsl:param name="childcompid"/>
	<select name="childcomponent">
		<option value=""></option>
		<xsl:for-each select="item">
			<option value="{componentid}">
				<xsl:if test="$childcompid = componentid">
					<xsl:attribute name="selected"/>
				</xsl:if>
				<xsl:value-of select="componentname"/>&nbsp;(<xsl:value-of select="componentid"/>)
			</option>
		</xsl:for-each>
	</select>
</xsl:template>
</xsl:stylesheet>
