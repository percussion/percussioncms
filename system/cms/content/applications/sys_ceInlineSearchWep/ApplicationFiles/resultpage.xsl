<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
   <xsl:variable name="this" select="/"/>
   <xsl:template match="/">
   <html>
   <head>
     <meta name="generator" content="Percussion XSpLit Version 3.0"/>
     <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
     <title>Rhythmyx - Content Editor - Related Content Search</title>
     <link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
     <link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
   </head>
   <script language="javascript">
   var closeWindowthrCancel = 1;
	function onClickCancel()
   {
      if(closeWindowthrCancel==1){
			window.returnValue="cancel";
			self.close();
		}
	}
   function onClickSearchAgain()
   {
         closeWindowthrCancel = 0;
         this.location.href =  top.opener.INLINE_SEARCH_PAGE;
   }
   function onClickUpdate(link)
   {
	closeWindowthrCancel = 0;
       top.opener.formatOutput(link);
      self.close();
   }
 
   
   </script>

   <body onload="javascript:self.focus();"  onUnload="javascript:onClickCancel()">
     <form name="updaterelateditems" method="post" action="">
     <input type="button" name="searchagain" value="Search Again" onclick="javascript:onClickSearchAgain()"/>&nbsp;
     <input type="button" name="cancel" value="Close" onclick="javascript:onClickCancel()"/>&nbsp;
     <input type="hidden" name="sys_command" value="update"/>&nbsp;
     <input type="hidden" name="sys_contentid" value=""/>&nbsp;
     <input type="hidden" name="sys_revision" value=""/>&nbsp;
     <input type="hidden" name="sys_slotid" value="{/*/slotid}"/>&nbsp;
     <input type="hidden" name="sys_variantid" value=""/>&nbsp;
     <input type="hidden" name="sys_context" value=""/>&nbsp;
     <input type="hidden" name="sys_authtype" value=""/>&nbsp;
     <input type="hidden" name="httpcaller" value=""/>&nbsp;
       <div align="center">
          <table width="100%" cellpadding="0" cellspacing="3" border="0">
              <xsl:apply-templates select="/*/search" mode="mode2"/>
        </table>
       </div>
     </form>
   </body>
   </html>
  </xsl:template>

  <xsl:template match="*">
    <xsl:choose>
      <xsl:when test="text()">
        <xsl:choose>
          <xsl:when test="@no-escaping">
            <xsl:value-of select="." disable-output-escaping="yes"/>
          </xsl:when>

          <xsl:otherwise>
            <xsl:value-of select="."/>
          </xsl:otherwise>

        </xsl:choose>

      </xsl:when>

      <xsl:otherwise>&nbsp;</xsl:otherwise>

    </xsl:choose>

    <xsl:if test="not(position()=last())">
      <br id="XSpLit"/>
    </xsl:if>

  </xsl:template>

  <xsl:template match="attribute::*">
    <xsl:value-of select="."/>
    <xsl:if test="not(position()=last())">
      <br id="XSpLit"/>
    </xsl:if>

  </xsl:template>

<xsl:template match="search" mode="mode2"> 
<table width="100%" cellpadding="4" cellspacing="0" border="0">
  <tr class="headercell"> 
    <td align="left" class="headercellfont"><xsl:if test="string-length(variantid)"><xsl:value-of select="variantname" />(<xsl:value-of select="variantid" />)</xsl:if>&nbsp;</td>
  </tr>
  <tr> 
    <td valign="top" class="headercell"> 
      <table width="100%" border="0" cellspacing="1" cellpadding="0">
        <tr class="headercell2"> 
          <td class="headercell2font" align="center">Content&nbsp;Title&nbsp;(ID)&nbsp;&nbsp;&nbsp;</td>
          <td class="headercell2font" align="center">Content&nbsp;Type&nbsp;&nbsp;&nbsp;</td>
          <td class="headercell2font" align="center">&nbsp;</td>
        </tr>
			<xsl:if test="string-length(variantid)"> 
				<xsl:apply-templates select="item" mode="item" /> 
			</xsl:if> 
		  <xsl:if test="not(string-length(variantid))"> 
			  <tr class="datacell1"> 
					<td class="datacellnoentriesfound" colspan="4" align="center">No entries found.&nbsp;</td>
			  </tr>
		  </xsl:if> 
      </table>
    </td>
  </tr>
</table>
</xsl:template> 
<xsl:template match="item" mode="item"> 
<tr class="datacell1"> 
  <td align="left" class="datacell1font">
  <a>
  <xsl:attribute name="href">#</xsl:attribute>
  <xsl:attribute name="onclick">javascript:onClickUpdate(&quot;<xsl:value-of select="previewurl"/>&quot;)</xsl:attribute>
  <xsl:value-of select="contentname" />(<xsl:value-of select="contentid" />)
  </a>
  </td>
  <td align="left" class="datacell1font"><xsl:value-of select="contenttype" /></td>
  <td align="left" class="datacell1font">
  <a>
  <xsl:attribute name="href">#</xsl:attribute>
  <xsl:attribute name="onclick">javascript:window.open(&quot;<xsl:value-of select="previewurl"/>&quot;,&quot;preview&quot;, &quot;toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=400,height=300,z-lock=1&quot;)</xsl:attribute>
  <img src="../sys_resources/images/preview.gif" alt="Preview" align="top" border="0" /></a></td>
</tr>
</xsl:template> 
</xsl:stylesheet>
