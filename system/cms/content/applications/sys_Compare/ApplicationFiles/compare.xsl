<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 SYSTEM "../../DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol SYSTEM "../../DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial SYSTEM "../../DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n" >
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:variable name="lang" select="//@xml:lang"/>

   <xsl:template match="/">
      <html>
         <head>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_Compare.compare@Rhythmyx - Document Comparison'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
         </head>
         <frameset cols="250,*">
            <frame name="leftframe" scrolling="auto" src="{//leftframeurl}"/>
            <frame name="rightframe" scrolling="auto" src="{//rightframeurl}"/>
            <noframes>
               <body>
                  <p>This page uses frames, but your browser doesn't support them.</p>
               </body>
            </noframes>
         </frameset>
      </html>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_Compare.compare@Rhythmyx - Document Comparison">Title for Rhythmyx Document Comparison window</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
