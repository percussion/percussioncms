<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">

<xsl:variable name="indent_level">&nbsp;&nbsp;&nbsp;</xsl:variable>

<xsl:template match="filetree" mode="ftree">
	<table summary="File List goes here" width="100%" border="0" cellspacing="0" cellpadding="0">
		<tr class="headercell2">
		  <td width="30%" class="headercell2font" align="center">Filename<img src="../sys_resources/images/invis.gif" width="60" height="1" alt="" border="0" /></td>
		  <td width="30%" class="headercell2font" align="left">Operation<img src="../sys_resources/images/invis.gif" width="60" height="1" alt="" border="0" /></td>
		  <td width="30%" class="headercell2font" align="left">Status<img src="../sys_resources/images/invis.gif" width="60" height="1" alt="" border="0" /></td>
        <td width="30%" class="headercell2font" align="left">CMS&nbsp;Link<img src="../sys_resources/images/invis.gif" width="60" height="1" alt="" border="0" /></td>
		</tr>
		<tr><td colspan="4" widtn="100%" class="bordercolor"><img src="../sys_resources/images/invis.gif" width="1" height="1" border="0" alt=""/></td></tr>
		<xsl:for-each select="./path">
			<xsl:apply-templates select="." mode="path" />      
		</xsl:for-each> 
	</table>
</xsl:template>

<xsl:template match="path" mode="path">
	<xsl:param name="indent_string"  />
	<tr class="datacell1">
		<td class="datacell2font" valign="top">
			<xsl:value-of select="$indent_string" /> 
			<xsl:value-of select="./@name" />/
		</td>
		<td class="datacell2font">&nbsp;</td>
		<td class="datacell2font">&nbsp;</td>
		<td class="datacell2font">&nbsp;</td>
	</tr>
		<xsl:for-each select="./file" >
			<xsl:apply-templates select="." mode="file" > 
				<xsl:with-param name="indent_string" select="concat($indent_string,$indent_level)" />
			</xsl:apply-templates>
		</xsl:for-each> 
		<xsl:for-each select="./path">
			<xsl:apply-templates select="."  mode="path" >
				<xsl:with-param name="indent_string" select="concat($indent_string,$indent_level)" />
			</xsl:apply-templates>
		</xsl:for-each>
</xsl:template> 

<xsl:template match="file" mode="file">
	<xsl:param name="indent_string" />
	<tr class="datacell1">
		<td class="datacell1font" valign="top">
			<xsl:value-of select="$indent_string" /> 
			<a target="pubpreview"> 
   			<xsl:attribute name="href">
	   			<xsl:value-of select="./link" />
		   	</xsl:attribute>
			   <xsl:value-of select="@filename" />
			</a>
		</td>
		<td class="datacell1font" valign="top"><xsl:value-of select="./operation" /></td>
		<td class="datacell1font" valign="top"><xsl:value-of select="./status" /></td>
		<td class="datacell1font" valign="top">
			<a target="pubpreview"> 
			<xsl:attribute name="href">
				<xsl:value-of select="./cmslink" />
			</xsl:attribute>
			<xsl:value-of select="./title" />
			</a>
		</td> 
	</tr>
</xsl:template> 
</xsl:stylesheet> 
