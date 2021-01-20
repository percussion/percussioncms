<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
   <!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
   %HTMLlat1;
   <!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
   %HTMLsymbol;
   <!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
   %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
   <xsl:template match="/">
      <html>
         <head>
            <title>Preview</title>
            <script language="javascript">
            	function redirecttopreview()
            	{
            		document.location.href="<xsl:value-of select="//@previewurl"/>";
            	}
            </script>
         </head>
         <body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="javascript:redirecttopreview();">
         </body>
      </html>
   </xsl:template>
</xsl:stylesheet>
