<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:import href="file:rx_resources/stylesheets/rx_bannerTemplate.xsl"/>
<xsl:variable name="relatedlinks" select="/*/relatedlinks"/>
  <xsl:template match="/">
    <html>
      <head>
        <meta name="generator" content="Percussion XSpLit Version 3.0"/>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
        <link rel="stylesheet" type="text/css" href="/sys_resources/css/templates.css"/>
        <link rel="stylesheet" type="text/css" href="/rx_resources/css/templates.css"/>
        <script src="../sys_resources/js/href.js">;</script>

        <title>Rhythmyx - Publisher - Publication Status Details</title>
        <script>
           function Reload()
           {
              var loc = document.location.href;
              var params = PSHref2Hash(loc);
              if(params['PUBAction'] != 'publish')
              {
                 document.location.reload(true);
              }
              else
              {
                 params['PUBAction'] = 'status';
                 newLoc = PSHash2Href(params, loc);
                 document.location.href = newLoc;
              }
           }
        </script>
      </head>
	<body class="backgroundcolor" leftmargin="0" topmargin="0" marginheight="0" marginwidth="0" onload="setTimeout('Reload()',15000);">
		<!--   BEGIN Banner and Login Details   -->
		<xsl:call-template name="bannerAndUserStatus"/>
		<!--   END Banner and Login Details   -->
  <table width="100%" cellpadding="0" cellspacing="1" border="0">
    <tr> 
      <td valign="top" height="100%" class="headercell"> 
        <!-- Main View Area Start -->
        <table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
          <tr> 
            <td class="outerboxcell" align="right" valign="top"><span class="outerboxcellfont">Publication Status Details</span></td>
          </tr>
          <tr> 
            <td width="100%" height="1" class="bordercolor"><img src="/sys_resources/images/invis.gif" width="1" height="1" border="0" alt="" /></td>
          </tr>
	<tr><td valign="top" class="bordercolor">
		        	<xsl:apply-templates select="." mode="mode1"/>
						</td>
					</tr>
        </table></td></tr></table>
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

  <xsl:template match="*" mode="mode1">
<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
                <tr class="headercell"> 
        			<td valign="top" align="center">
<table width="100%" cellpadding="4" cellspacing="0" border="0">
                      <tr> 
                        <td valign="top" class="headercell">
									<!--   Results Details   -->
							    <xsl:apply-templates select="response"/>
							    <xsl:if test="@type='status' and response/@code = 'inProgress'">
						  	    <xsl:apply-templates select="status"/>
							    </xsl:if>
				          <!--   End Results Details   -->
        			</td>
		      	</tr>
						<tr>
							<td>
						 		<xsl:apply-templates select="." mode="buttons"/>
							</td>
						</tr>
       		</table></td></tr></table>  </xsl:template>

  <xsl:template match="response">
<table width="100%" cellpadding="0" cellspacing="0" border="0">
                            <tr> 
                              <td> 
                                <table width="100%" cellpadding="0" cellspacing="1" border="0">
                                  <tr> 
                                    <td width="20%" align="left" class="statussectionfont">
        <xsl:text>Publisher Status:</xsl:text>
      </td>
      <td width="80%" class="statusvaluefont">
        <xsl:value-of select="."/>
      </td>
    </tr>
	</table></td></tr></table>

	</xsl:template>

	<xsl:template match="status">
  <p>
	<table border="0" width="100%">
      <xsl:choose>
      <xsl:when test="@clistindex='-1'">
       <tr>
         <th>
           <xsl:text>Edition is in queue for publishing</xsl:text>
         </th>
       </tr>
      </xsl:when>
    <xsl:otherwise>
       <tr>
	<td colspan="3"><span class="statussectionfont">Currently Publishing:</span></td>
	</tr>
	<tr>
	<td colspan="3">&nbsp;</td>
	</tr>
	<tr>
	   <td width="5%">&nbsp;</td>
	   <td width="15%"><span class="statuslabelfont">Edition:</span></td>
	   <td><span class="statusvaluefont"><xsl:value-of select="@editionname"/></span></td>
	</tr>
	<tr>
	   <td width="5%">&nbsp;</td>
	   <td width="15%"><span class="statuslabelfont">Content list:</span></td>
	   <td><span class="statusvaluefont"><xsl:value-of select="@clistname"/> ( <xsl:value-of select="@clistindex"/> of <xsl:value-of select="@clistcount"/> lists )</span></td>
	</tr>
	<!--
	<tr>
	   <td width="5%">&nbsp;</td>
	   <td width="15%"><span class="statuslabelfont">Content list page:</span></td>
	   <td><span class="statusvaluefont"><xsl:value-of select="@pageindex"/></span></td>
	</tr>    
        -->
        </xsl:otherwise>
    </xsl:choose>
	</table>
  </p>
	</xsl:template>

  <xsl:template match="*" mode="buttons">
    <xsl:param name="link">/Rhythmyx/sys_pubHandler/publisher.htm?editionid=<xsl:value-of select="@editionid"/>&amp;PUBAction=</xsl:param>
    <table border="0" cellpadding="0" cellspacing="2" width="100%">
      <tr>
        <xsl:if test="response/@code = 'publish'">
          <td align="center">
            <a>
              <xsl:attribute name="href"><xsl:value-of select="$link"/>status</xsl:attribute>
                <img alt="Get publish status" title="Get publish status" src="../sys_resources/images/status.gif" border="0"/>
            </a>
          </td>
        </xsl:if>
        <xsl:if test="response/@code != 'publish'">
          <td align="center"><form><input type="button" onClick="javascript:location.reload(true);" value="Refresh status"/></form></td>
          </xsl:if>
      </tr>
    </table>
  </xsl:template>

</xsl:stylesheet>
