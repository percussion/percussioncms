<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n" >
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:template match="/">
		<html xmlns="http://www.w3.org/1999/xhtml">
			<head>
				<script language="javascript" src="../sys_resources/js/href.js">;</script>
				<meta name="generator" content="Percussion Rhythmyx"/>
				<title>Redirect page</title>
				<script language="javascript" >
				function initBody()
				{
					if (!window.opener || window.opener.closed)
					{
						return;
					}
					<xsl:variable name="rev" select="//@sys_revision"/>
					<xsl:variable name="contentid" select="//@sys_contentid"/>
					<xsl:choose>
						<xsl:when test="$rev and $rev != ''">					    
						     window.opener.location.href = updateRevision('<xsl:value-of select="$rev"/>');
						</xsl:when>
						<xsl:otherwise>
						 
						 if(window.opener.ps_noCloseFlag != true){
						  window.opener.location.reload();
						  }else{
						  setTimeout("window.opener.location.reload()",2000);
						  }
						</xsl:otherwise>
					</xsl:choose>
					if(window.opener.ps_noCloseFlag == true){
					var loc = getReturnURL(window.opener.ps_returnUrl,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$rev"/>');
					self.location.href = loc; 
					}else{
					self.close();
					}
				}
				<![CDATA[
				function updateRevision(revid)
				{
					var h = PSHref2Hash(window.opener.location.href);
					// parent item only - update the revision to reflect the updates
					if (h["sys_activeitemid"] == null || h["sys_activeitemid"] == "")
					{
						if (h["sys_revision"] != null)
						{
							h["sys_revision"] = revid;
						}
					}
					return PSHash2Href(h, window.opener.location.href);
				}
				
				function getReturnURL(oldHref, contentid, revision){
				   var h = PSHref2Hash(oldHref);
				   var newhash = new Array();
				   newhash["sys_command"] = h["sys_command"];
				   newhash["sys_view"] = h["sys_view"];
				   if(revision.length != 0){
				   	newhash["sys_revision"] = revision;
				   	}else{
				   	newhash["sys_revision"] = '1';
				   	}
				   newhash["sys_contentid"] = contentid;
				   
				   return PSHash2Href(newhash, oldHref);
				  
				
				}
				
				]]>						
				</script>
            <link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
			</head>
			<body  onload="initBody();">
            <table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0" class="headercell">
               <tr  class="outerboxcell">
                  <td align="center" valign="middle" class="outerboxcellfont">
                     <xsl:call-template name="getLocaleString">
                        <xsl:with-param name="key" select="'psx.generic@Your request is being processed'"/>
                        <xsl:with-param name="lang" select="$lang"/>
                     </xsl:call-template>. 
                     <xsl:call-template name="getLocaleString">
                        <xsl:with-param name="key" select="'psx.generic@Please wait a moment'"/>
                        <xsl:with-param name="lang" select="$lang"/>
                     </xsl:call-template>...
                  </td>
               </tr>
            </table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
