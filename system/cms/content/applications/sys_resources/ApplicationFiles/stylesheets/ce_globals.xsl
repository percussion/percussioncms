<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 SYSTEM "../../../DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol SYSTEM "../../../DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial SYSTEM "../../../DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<!-- $ Id: $ -->
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxctl="URN:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
<xsl:variable name="docComponent" select="document(/*/SectionLinkList/SectionLink[@name='ComponentLookupURL'])/*"/>
<xsl:variable name="UserStatus" select="$docComponent/component[@name='ce_userstatus']/@name"/>
<xsl:variable name="Workflow" select="$docComponent/component[@name='ce_workflow']/@name"/>
<xsl:variable name="ContentStatus" select="$docComponent/component[@name='ce_contentstatus']/@name"/>
<xsl:variable name="History" select="$docComponent/component[@name='ce_history']/@name"/>
<xsl:variable name="Banner" select="$docComponent/component[@name='ce_banner']/@name"/>
<xsl:variable name="Help" select="$docComponent/component[@name='ce_help']/@name"/>
<xsl:variable name="RelatedContent" select="$docComponent/component[@name='ce_relatedcontent']/@name"/>
<xsl:variable name="VariantList" select="$docComponent/component[@name='ce_variantlist']/@name"/>
</xsl:stylesheet>
