<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
   <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
   %HTMLlat1;
   <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
   %HTMLsymbol;
   <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
   %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
                xmlns:psxi18n="com.percussion.i18n.PSI18nUtils" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:template match="/">
      <html>
         <head>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_bannerFrame.bannerframe@Rhythmyx - Banner'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
            <link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
	    <link rel="stylesheet" type="text/css" href="../sys_resources/css/tabs.css"/>
         </head>
         <body>
             <div id="RhythmyxBanner">
            <table border='0' cellspacing='0' cellpadding='0' class="rx-banner-table">
               <tr class="rx-banner-row">
                  <td valign="bottom">
                        <xsl:variable name="bannerurl" select="document(//bannerurl)/*/url"/>
                        <xsl:copy-of select="document($bannerurl)/*/body/*"/>
                  </td>
                  <td align="right" valign="bottom">
                        <xsl:variable name="userstatusurl" select="document(//userstatusurl)/*/url"/>
                        <xsl:copy-of select="document($userstatusurl)/*/body/*"/>
                  </td>
               </tr>
            </table>
         </div>
         </body>
      </html>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_bannerFrame.bannerframe@Rhythmyx - Banner">Title for Rhythmyx banner page.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
