<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
	<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
	<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
<xsl:include href="file:sys_resources/stylesheets/viewpaging.xsl" />
   <xsl:variable name="this" select="/"/>
   <xsl:template match="/">
   <html>
   <head>
     <meta name="generator" content="Percussion XSpLit Version 3.0"/>
     <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
     <title>Rhythmyx - Content Editor - Saved Searches</title>
     <link rel="stylesheet" type="text/css" href="/sys_resources/css/templates.css"/>
     <link rel="stylesheet" type="text/css" href="/rx_resources/css/templates.css"/>
	<script src="../sys_resources/js/formValidation.js"/>
	</head>
   <script>
   function editComponent(editurl)
   {
      if(!window.opener || window.opener.closed)
      {
         self.close();
         return;
      }
      window.opener.location.href = editurl;
		self.close();
   }
	</script>
   <body onload="javascript:self.focus();">
	  <form name="deletesavedsearch" method="post" action="">
		 <table width="100%" cellpadding="0" cellspacing="1" border="0" class="headercell">
			<xsl:apply-templates select="*" mode="mode0"/>
			<tr class="headercell">
				<td align="center" colspan="2">
					<xsl:apply-templates select="/" mode="paging"/>
				</td>
			</tr>
			<tr class="headercell">
				<td align="center" colspan="2">
					<input type="button" value="Close" onclick="window.close()"/>
				</td>
			</tr>
		 </table>
     </form>
	</body>
   </html>
  </xsl:template>
<xsl:template match="*" mode="mode0"> 
	<tr class="headercell">
		<td align="center" class="outerboxcellfont"  colspan="2">
			Parents of: <xsl:value-of select="parentsfor"/>
		</td>
	</tr>
  <tr class="headercell2"> 
	 <td width="40%" class="headercell2font" align="left">Name&nbsp;</td>
	 <td width="60%" class="headercell2font" align="left">Display Name&nbsp;</td>
  </tr>
  <xsl:choose>
	  <xsl:when test="count(list)=1 and list/componentid=''"> 
		  <tr class="datacell1"> 
				<td class="datacellnoentriesfound" colspan="2" align="center">No entries found.&nbsp;</td>
		  </tr>
	  </xsl:when> 
	  <xsl:otherwise>
		  <xsl:apply-templates select="list" mode="mode1"/>
	  </xsl:otherwise>
  </xsl:choose>
</xsl:template> 

<xsl:template match="list" mode="mode1"> 
  <tr class="datacell1"> 
    <td valign="top" class="datacell1font" align="left">
		<a onclick="javascript:editComponent('{editurl}')">
			<xsl:attribute name="href">javascript:{}</xsl:attribute>
			<xsl:value-of select="componentname"/>
		</a>
    </td>
    <td valign="top" class="datacell1font"><xsl:comment><xsl:value-of select="editlink"/></xsl:comment>
			<xsl:value-of select="componentdisplayname"/>
	 </td>
  </tr>
</xsl:template> 
</xsl:stylesheet>
