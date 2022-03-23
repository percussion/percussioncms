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
<xsl:template mode="editvariables_mainbody" match="*">
<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus" /> 
<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
<xsl:variable name="componentname" select="componentname"/>
<xsl:variable name="sitelookup" select="/*/sitelookup"/>
<xsl:variable name="contextlookup" select="/*/contextlookup"/>
 <table width="100%" height="100%" cellpadding="0" cellspacing="3" border="0">
   <tr>
     <td class="outerboxcell" align="right" valign="top">
       <span class="outerboxcellfont">Edit Global Variables</span>
     </td>
   </tr>
   <tr class="headercell">
      <td>
             <table width="100%" cellpadding="0" cellspacing="1" border="0">
               <xsl:apply-templates select="variables">
						<xsl:with-param name="sitelookup" select="$sitelookup"/>
						<xsl:with-param name="contextlookup" select="$contextlookup"/>
					</xsl:apply-templates>
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
<xsl:template match="variables">
	<xsl:param name="sitelookup"/>
	<xsl:param name="contextlookup"/>
	<tr class="headercell">
	  <td valign="top" align="left" class="headercellfont" colspan="2">
			  <xsl:if test="string-length(assemblerpropid)" > Assembler Property(id):&nbsp;<xsl:value-of select="assemblerpropname" />(<xsl:value-of select="assemblerpropid" />) 
			  </xsl:if> 
				&nbsp;
		</td>
	</tr>
 <form name="editassemblerprop" method="post" action="updatevariables.html">
   <input name="DBActionType" type="hidden" value="UPDATE"/>
   <input type="hidden" name="doccancelurl">
      <xsl:attribute name="value">
         <xsl:value-of select="../cancelurl"/>
      </xsl:attribute>
   </input>
   <input name="sys_componentname" type="hidden">
     <xsl:attribute name="value">
       <xsl:value-of select="../componentname"/>
     </xsl:attribute>
   </input>
   <input name="version" type="hidden">
     <xsl:attribute name="value">
	<xsl:choose>
		<xsl:when test="version=''">0</xsl:when>
		<xsl:otherwise><xsl:value-of select="version"/></xsl:otherwise>
	</xsl:choose>
     </xsl:attribute>
   </input>
   <input name="contextid" type="hidden">
     <xsl:attribute name="value">
       <xsl:value-of select="contextid"/>
     </xsl:attribute>
   </input>
   <input name="variableid" type="hidden">
     <xsl:attribute name="value">
       <xsl:value-of select="assemblerpropid"/>
     </xsl:attribute>
   </input>
   <input name="siteid" type="hidden">
     <xsl:attribute name="value">
       <xsl:value-of select="siteid"/>
     </xsl:attribute>
   </input>
   <tr class="datacell1">
     <td width="25%" align="left" class="datacell1font"><font class="reqfieldfont">*</font>Name</td>
     <td width="80%" align="left" class="datacell1font">
      <input size="30" name="assemblerpropname" value="{assemblerpropname}"/>
     </td>
   </tr>
   <tr class="datacell2">
     <td align="left" class="datacell2font"><font class="reqfieldfont">*</font>Value</td>
     <td align="left" class="datacell2font">
      <input size="40" name="assemblerpropvalue" value="{assemblerpropvalue}"/>
     </td>
   </tr>
   <xsl:apply-templates select="document($contextlookup)" mode="contextinfo"/>
   <xsl:apply-templates select="document($sitelookup)" mode="site"/>
   <tr class="datacell1">
     <td align="left" class="datacell1font" colspan="2">
       <input type="button" value="Save" name="save" language="javascript" onclick="return save_onclick()"/>&nbsp;
       <input type="button" value="Cancel" name="cancel" language="javascript" onclick="cancelFunc();"/>
     </td>
   </tr>
  </form>
</xsl:template>
<xsl:template match="*" mode="site">
   <tr class="datacell1">
     <td align="left" class="datacell1font">Site</td>
     <td align="left" class="datacell1font">
         <select name="sitelist">
           <option value="0">
			  <xsl:if test="$this/editvariables/variables/siteid=0">
				 <xsl:attribute name="selected"/>
			  </xsl:if>Preview Site
			  </option>
             <xsl:for-each select="item">
               <option>
                 <xsl:variable name="value">
                   <xsl:value-of select="srcsitevalue"/>
                 </xsl:variable>
                 <xsl:attribute name="value">
                   <xsl:value-of select="srcsitevalue"/>
                 </xsl:attribute>
                 <xsl:if test="$this/editvariables/variables/siteid=$value">
                   <xsl:attribute name="selected"/>
                 </xsl:if>
                 <xsl:value-of select="srcsitename"/>
               </option>
             </xsl:for-each>
         </select>
     </td>
   </tr>
</xsl:template>
  <xsl:template match="*" mode="contextinfo">
    <xsl:for-each select=".">
      <tr class="datacell1">
        <td align="left" class="datacell1font">Context</td>
        <td align="left" class="datacell1font">
         <select name="context">
             <xsl:for-each select="context">
               <option>
                 <xsl:attribute name="value">
                   <xsl:value-of select="@id"/>
                 </xsl:attribute>
                 <xsl:if test="$this/editvariables/variables/contextid=@id">
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
</xsl:stylesheet>
