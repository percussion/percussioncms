<?xml version='1.0' encoding='UTF-8'?>


<!DOCTYPE xsl:stylesheet [
<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
	<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
	<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">

  <xsl:template match="*" mode="workflowactions">
    <xsl:param name="selectedaction"/>
<!-- 
This is added temporarily only. Once actions exit is done we will revise this to render 
actions into a drop down list box
-->
   <select name="workflowactions">
	   <option value="">None</option>
      <xsl:for-each select="workflowaction">
         <option>
            <xsl:attribute name="value">
               <xsl:value-of select="."/>
            </xsl:attribute>

            <xsl:if test=".=$selectedaction">
               <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="@extensionname"/>
         </option>
      </xsl:for-each>
   </select>
  </xsl:template>
</xsl:stylesheet>
