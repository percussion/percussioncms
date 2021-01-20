<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
   <!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
   %HTMLlat1;
   <!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
   %HTMLsymbol;
   <!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
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
                  <xsl:with-param name="key" select="'psx.sys_caSites.casites@Rhythmyx - Content Explorer'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
				<script>
					function showWindow(url, target, style)
					{
						win = parent.window.open(url, target, style);
   				   if (win == null)
   				      alert("Popups must be enabled for the content explorer");
   				   else
      				    win.focus();
					}
				</script>
         </head>
         <frameset rows="74px,*" border="0">
            <frame name="banner" title="banner" scrolling="no" src="{//bannerpageurl}"/>
            <frame name="navcontent" title="navcontent" scrolling="no" src="{//cxpageurl}"/>
            <noframes>
         <body class="backgroundcolor" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
                  <p>This page uses frames, but your browser doesn't support them.</p>
               </body>
            </noframes>
         </frameset>
      </html>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_mainPage.mainpage@Rhythmyx - Content Administrator">Title for Rhythmyx main page.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
