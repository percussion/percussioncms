<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
   <!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
   %HTMLlat1;
   <!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
   %HTMLsymbol;
   <!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
   %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n">
   <xsl:template mode="neweditsite_mainbody" match="*">
      <xsl:variable name="userroles" select="document(userrolesurl)/UserStatus"/>
      <xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
      <xsl:variable name="componentname" select="componentname"/>
      <xsl:variable name="publisherlookup" select="/*/publisherlookupurl"/>
      <xsl:variable name="navthemelookup" select="/*/navthemelookupurl"/>
      <xsl:variable name="globaltemplatelookup" select="/*/globaltemplatelookupurl"/>
      <table width="100%" height="100%" cellpadding="0" cellspacing="3" border="0">
         <tr>
            <td class="outerboxcell" align="right" valign="top">
               <span class="outerboxcellfont">Edit Site Properties</span>
            </td>
         </tr>
         <tr class="headercell">
            <td>
               <table width="100%" cellpadding="0" cellspacing="1" border="0">
                  <xsl:apply-templates select="new">
                     <xsl:with-param name="publisherlookup" select="$publisherlookup"/>
                     <xsl:with-param name="navthemelookup" select="$navthemelookup"/>
                     <xsl:with-param name="globaltemplatelookup" select="$globaltemplatelookup"/>
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
   <xsl:template match="new">
      <xsl:param name="publisherlookup"/>
      <xsl:param name="navthemelookup"/>
      <xsl:param name="globaltemplatelookup"/>
      <tr class="headercell">
         <td valign="top" align="left" class="headercellfont" colspan="2">
            <xsl:if test="string-length(newsiteid)"> Site(id):&nbsp;<xsl:value-of select="sitename"/>(<xsl:value-of select="newsiteid"/>) 
</xsl:if> 
&nbsp;
</td>
      </tr>
      <form name="newsite" method="post" action="">
         <xsl:attribute name="action"><xsl:choose><!--when site is created --><xsl:when test="newsiteid=''"><xsl:choose><xsl:when test="//sys_community=0">newsite_gen.html</xsl:when><xsl:otherwise>newsite_comm.html</xsl:otherwise></xsl:choose></xsl:when><!--when site is updated --><xsl:otherwise><xsl:value-of select="'editsite.html'"/></xsl:otherwise></xsl:choose></xsl:attribute>
         <input name="DBActionType" type="hidden" value="UPDATE"/>
         <input type="hidden" name="doccancelurl">
            <xsl:attribute name="value"><xsl:value-of select="../cancelurl"/></xsl:attribute>
         </input>
         <input name="sys_componentname" type="hidden">
            <xsl:attribute name="value"><xsl:value-of select="../componentname"/></xsl:attribute>
         </input>
         <input name="newsiteid" type="hidden">
            <xsl:attribute name="value"><xsl:value-of select="newsiteid"/></xsl:attribute>
         </input>
         <tr class="datacell1">
            <td width="25%" align="left" class="datacell1font">
               <font class="reqfieldfont">*</font>Site Name</td>
            <td width="80%" align="left" class="datacell1font">
               <input size="30" name="requiredsitename">
                  <xsl:attribute name="value"><xsl:value-of select="requiredsitename"/></xsl:attribute>
               </input>
            </td>
         </tr>
         <tr class="datacell2">
            <td align="left" class="datacell2font">Description</td>
            <td align="left" class="datacell2font">
               <input size="40" name="sitdesc">
                  <xsl:attribute name="value"><xsl:value-of select="sitdesc"/></xsl:attribute>
               </input>
            </td>
         </tr>
         <tr class="datacell1">
            <td align="left" class="datacell1font">Site Address (URL)</td>
            <td align="left" class="datacell1font">
               <input size="30" name="baseurl">
                  <xsl:attribute name="value"><xsl:value-of select="baseurl"/></xsl:attribute>
               </input>
            </td>
         </tr>
         <tr class="datacell1">
            <td align="left" class="datacell1font">Home Page (URL)</td>
            <td align="left" class="datacell1font">
               <input size="40" name="homepageurl">
                  <xsl:attribute name="value"><xsl:value-of select="homepageurl"/></xsl:attribute>
               </input>
            </td>
         </tr>
         <tr class="datacell2">
            <td align="left" class="datacell2font">Publishing Root Location</td>
            <td align="left" class="datacell2font">
               <input size="30" name="root">
                  <xsl:attribute name="value"><xsl:value-of select="root"/></xsl:attribute>
               </input>
            </td>
         </tr>
         <tr class="datacell1">
            <td align="left" class="datacell1font">
               <font class="reqfieldfont">*</font>Publisher</td>
            <td align="left" class="datacell1font">
               <xsl:apply-templates select="document($publisherlookup)" mode="publisher"/>
            </td>
         </tr>
         <tr class="datacell2">
            <td align="left" class="datacell2font">Status</td>
            <td align="left" class="datacell2font">
               <select name="state">
                  <option value="1">
                     <xsl:if test="/*/statevalue=&apos;1&apos;">
                        <xsl:attribute name="selected"/>
                     </xsl:if>
Active
</option>
                  <option value="0">
                     <xsl:if test="/*/statevalue=&apos;0&apos;">
                        <xsl:attribute name="selected"/>
                     </xsl:if>
Inactive
</option>
               </select>
            </td>
         </tr>
         <tr class="datacell1">
            <td align="left" class="datacell1font">Folder Root</td>
            <td align="left" class="datacell1font">
               <input size="40" name="folderroot">
                  <xsl:attribute name="value"><xsl:value-of select="folderroot"/></xsl:attribute>
               </input>
            </td>
         </tr>
         <tr class="datacell2">
            <td align="left" class="datacell2font">Global Template</td>
            <td align="left" class="datacell2font">
               <xsl:apply-templates select="document($globaltemplatelookup)" mode="globaltemplate"/>
            </td>
         </tr>
         <tr class="datacell1">
            <td align="left" class="datacell1font">Nav Theme</td>
            <td align="left" class="datacell1font">
               <xsl:apply-templates select="document($navthemelookup)" mode="navtheme"/>
            </td>
         </tr>
         <tr class="datacell2">
            <td align="left" class="datacell2font">Allowed Namespaces</td>
            <td align="left" class="datacell2font">
	             <input size="20" name="allowednamespaces">
	                  <xsl:attribute name="value"><xsl:value-of select="//allowednamespaces"/></xsl:attribute>
	               </input>
            </td>
         </tr>           
         <tr class="datacell1">
            <td colspan="2" align="left" class="headercell2font">FTP Information:</td>
         </tr>
         <tr class="datacell1">
            <td align="left" class="datacell1font">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;IP Address</td>
            <td align="left" class="datacell1font">
               <input size="30" name="ipaddress">
                  <xsl:attribute name="value"><xsl:value-of select="ipaddress"/></xsl:attribute>
               </input>
            </td>
         </tr>
         <tr class="datacell2">
            <td align="left" class="datacell2font">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Port Number</td>
            <td align="left" class="datacell2font">
               <input size="8" name="port">
                  <xsl:attribute name="value"><xsl:value-of select="port"/></xsl:attribute>
               </input>
            </td>
         </tr>
         <tr class="datacell1">
            <td align="left" class="datacell1font">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;User ID</td>
            <td align="left" class="datacell1font">
               <input size="30" name="ftpuserid">
                  <xsl:attribute name="value"><xsl:value-of select="ftpuserid"/></xsl:attribute>
               </input>
            </td>
         </tr>
         <tr class="datacell2">
            <td align="left" class="datacell2font">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Password</td>
            <td align="left" class="datacell2font">
               <input size="15" type="password" name="ftppassword">
                  <xsl:attribute name="value"><xsl:value-of select="ftppassword"/></xsl:attribute>
               </input>
            </td>
         </tr>
         <tr class="datacell1">
            <td align="left" class="datacell1font" colspan="2">
               <input type="button" value="Save" name="save" language="javascript" onclick="return save_onclick()"/>&nbsp;
<input type="button" value="Cancel" name="cancel" language="javascript" onclick="cancelFunc();"/>
            </td>
         </tr>
      </form>
   </xsl:template>
   <xsl:template match="*" mode="publisher">
      <select name="publisher">
         <option>--Choose--</option>
         <xsl:for-each select="item">
            <option>
               <xsl:variable name="value">
                  <xsl:value-of select="id"/>
               </xsl:variable>
               <xsl:if test="$this/neweditsite/publisherlookupvalue=$value">
                  <xsl:attribute name="selected"/>
               </xsl:if>
               <xsl:attribute name="value"><xsl:value-of select="id"/></xsl:attribute>
               <xsl:apply-templates select="name"/>
            </option>
         </xsl:for-each>
      </select>
   </xsl:template>
   <xsl:template match="*" mode="navtheme">
      <select name="navtheme">
         <option value=""></option>
         <xsl:for-each select="PSXEntry">
            <option>
               <xsl:variable name="optionValue" select="./Value"/>
               <xsl:attribute name="value"><xsl:value-of select="$optionValue"/></xsl:attribute>
               <xsl:if test="$this/neweditsite/new/navtheme=$optionValue">
                  <xsl:attribute name="selected"/>
               </xsl:if>
               <xsl:value-of select="./PSXDisplayText"/>
            </option>
         </xsl:for-each>
      </select>
   </xsl:template>
   <xsl:template match="*" mode="globaltemplate">
      <select name="globaltemplate">
         <option value=""></option>
         <xsl:for-each select="Template">
            <option>
               <xsl:variable name="optionValue" select="./@fileName"/>
               <xsl:attribute name="value"><xsl:value-of select="$optionValue"/></xsl:attribute>
               <xsl:if test="$this/neweditsite/new/globaltemplate=$optionValue">
                  <xsl:attribute name="selected"/>
               </xsl:if>
               <xsl:value-of select="./@name"/>
            </option>
         </xsl:for-each>
      </select>
   </xsl:template>
</xsl:stylesheet>
