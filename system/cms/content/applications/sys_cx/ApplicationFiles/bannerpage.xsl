<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
        <!ENTITY % w3centities-f PUBLIC
                "-//W3C//ENTITIES Combined Set//EN//XML"
                "http://www.w3.org/2003/entities/2007/w3centities-f.ent"
                >
        %w3centities-f;
        ]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
                xmlns:psxi18n="com.percussion.i18n" extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:output method= "html" indent= "yes" doctype-public= "-//W3C//DTD XHTML 1.0 Strict//EN" doctype-system = "DTD/xhtml1-strict.dtd" />
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:template match="/">
      <html lang="{$lang}">
         <head>
            <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_bannerFrame.bannerframe@Rhythmyx - Banner'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
            <link rel="stylesheet" type="text/css" href="/sys_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="/rx_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="{concat('/rx_resources/css/',$lang,'/templates.css')}"/>
            <link rel="stylesheet" type="text/css" href="/sys_resources/css/tabs.css"/>
         </head>
         <body>
            <div id="RhythmyxBanner">
               <table border='0' cellspacing='0' cellpadding='0' class="rx-banner-table">
                  <tr class="rx-banner-row">
                     <td valign="bottom">
                        <xsl:variable name="bannerurl" select="document(//bannerurl)/*/url"/>
                        <xsl:copy-of select="document($bannerurl)/*/body/*"/>
                     </td>
                     <td align="left" valign="top">
                        <a href="/dce/dce.jnlp">Desktop Content Explorer</a>
                     </td>

                     <td align="left" valign="top">
                        <a href="https://help.percussion.com/rhythmyx/implementation/dce/index.html" target="_blank" rel="noopener noreferrer">Help</a>
                     </td>
                     <td align="left" valign="bottom">
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
