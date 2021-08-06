<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
<xsl:template mode="editcompproperty_mainbody" match="*">
<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus" /> 
<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
<xsl:variable name="componentname" select="componentname"/>
<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
   <tr class="outerboxcell">
      <td class="outerboxcell" align="right" valign="top" colspan="2">
         <span class="outerboxcellfont">Edit Component Property&nbsp;&nbsp;</span>
      </td>
   </tr>
   <tr class="headercell">
     <td>
       <table width="100%" cellpadding="0" cellspacing="1" border="0">
          <form name="editcompproperty" method="post" action="updatecompproperty.html">
            <input name="DBActionType" type="hidden" value="UPDATE"/>
				<input type="hidden" name="doccancelurl" value="{cancelurl}"/>
            <input name="componentid" type="hidden" value="{componentid}"/>
            <input name="propertyid" type="hidden" value="{propertyid}"/>
            <input name="sys_componentname" type="hidden" value="{componentname}"/>
            <input name="sys_pagename" type="hidden" value="{pagename}"/>
         <tr class="headercell2">
           <td width="30%" align="left" class="headercell2font">Property&nbsp;ID&nbsp;</td>
           <td width="70%" align="left" class="headercell2font">&nbsp;
             <xsl:apply-templates select="propertyid"/>
           </td>
         </tr>
         <tr class="datacell1">
           <td class="datacell1font" width="30%"><font class="reqfieldfont">*</font>Property Name</td>
           <td class="datacell1font">
            <input name="propertyname" size="20" value="{propertyname}"/>
           </td>
         </tr>
         <tr class="datacell2">
           <td class="datacell1font"><font class="reqfieldfont">*</font>Property Value</td>
           <td class="datacell1font">
            <input name="propertyvalue" size="20" value="{propertyvalue}"/>
           </td>
         </tr>
         <tr class="datacell2">
           <td class="datacell1font">Property Description</td>
           <td class="datacell1font">
            <input name="propertydesc" size="50" value="{propertydesc}"/>
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
</xsl:stylesheet>
