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
   <xsl:import href="file:sys_resources/stylesheets/sys_bannerTemplate.xsl"/>
   <xsl:import href="file:rx_resources/stylesheets/rx_bannerTemplate.xsl"/>
   <xsl:include href="file:sys_pubSites/copysite_body.xsl"/>
   <xsl:variable name="this" select="/"/>
   <xsl:variable name="relatedlinks" select="/*/relatedlinks"/>
   <xsl:template match="/">
      <html>
         <head>
            <meta name="generator" content="Percussion XSpLit Version 3.5"/>
            <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
            <title>Rhythmyx - Publishing Administrator</title>
            <link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
            <script type="text/javaScript" src="../sys_resources/js/formValidation.js"/>
            <script type="text/javaScript" src="../sys_resources/js/href.js"/>
            <script type="text/javascript" language="javascript"><![CDATA[
               function create_onclick()
               {
                  // validate that a new edition name was provided
                  if (!(reqField(document.copysite.newsitename.value, "New Site Name")))
                     return false;
               
                  // add hidden and user input html parameters to the redirect url
                  var bounceTo = document.copysite.sourcesitelist[document.copysite.sourcesitelist.selectedIndex].value;
                  var params = PSHref2Hash(bounceTo);
                  params["DBActionType"] = document.copysite.DBActionType.value;
                  params["sys_componentname"] = document.copysite.sys_componentname.value;
                  params["requiredsitename"] = document.copysite.newsitename.value;
               
                  // create the clone by redirecting to the update resource
				    	   window.location.href = PSHash2Href(params, bounceTo);
               }
            ]]></script>
         </head>
         <body class="backgroundcolor" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
            <!--   BEGIN Banner and Login Details   -->
            <xsl:call-template name="bannerAndUserStatus"/>
            <!--   END Banner and Login Details   -->
            <table width="100%" cellpadding="0" cellspacing="1" border="0">
               <tr>
                  <td align="middle" valign="top" width="150" height="100%" class="outerboxcell">
                     <!--   start left nav slot   -->
                     <!-- begin XSL -->
                     <xsl:for-each select="document($relatedlinks)/*/component[@slotname='slt_pub_nav']">
                        <xsl:copy-of select="document(url)/*/body/*"/>
                     </xsl:for-each>
                     <!-- end XSL -->
                     <!--   end left nav slot   -->
                  </td>
                  <td align="middle" width="100%" valign="top" height="100%" class="outerboxcell">
                     <!--   start main body slot   -->
                     <xsl:apply-templates mode="copysite_mainbody">
</xsl:apply-templates>
                     <!--   end main body slot   -->
                  </td>
               </tr>
            </table>
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
</xsl:stylesheet>
