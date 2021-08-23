<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >

<xsl:variable name="shared" select="/*/shared" />

<!-- define XSL variables for each context variable -->
<xsl:variable name="rxs_js" select="/*/sys_AssemblerInfo/AssemblerProperties/Property[@name='rxs_js']/Value/@current"/>
<xsl:variable name="rxs_img" select="/*/sys_AssemblerInfo/AssemblerProperties/Property[@name='rxs_img']/Value/@current"/>
<xsl:variable name="rxs_css" select="/*/sys_AssemblerInfo/AssemblerProperties/Property[@name='rxs_css']/Value/@current"/>

<xsl:template name="xsplit_root">
   <!-- This template provides a default xsplit_root in case the current variant has not defined one -->
   The variant being previewed does not defined an xsplit_root.  Perhaps it is hand-crafted XSLT?
</xsl:template>


<xsl:template name="xsplit_body">
   <!-- This template provides a default xsplit_body in case the current variant has not defined one -->
   The variant being previewed does not defined an xsplit_body.  Perhaps the source needs to be re-split?
</xsl:template>


<xsl:template name="placeholder_xsplit_body">
   <!-- This template provides a xsplit_body to use while developing a global template.
        (If the global template actually called xsplit_body it would recurse on itself) -->
   <xsl:text>The local template rendering will be included here.</xsl:text>
</xsl:template>

<!-- this is required for fastforward only -->
<xsl:template name="rxglobal_head">
   <!-- populates the shared-metadata-driven elements of the <head> element -->
   <!-- (doesn't include <title> because Tidy requires that element in the source file) -->
   <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
   <meta name="description" content="{$shared/description}" />
   <meta name="keywords" content="{$shared/keywords}" />
</xsl:template>

</xsl:stylesheet>
