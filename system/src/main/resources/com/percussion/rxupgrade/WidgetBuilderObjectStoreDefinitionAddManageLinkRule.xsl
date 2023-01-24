<?xml version="1.0" encoding="utf-8"?>


<!-- This stylesheet is used to upgrade 4.0 and 4.5 content editor applications
 to 5.0. It adds the new attributes with default values supported by 5.0 -->
<xsl:stylesheet version="1.1"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:output method="xml" indent="yes" omit-xml-declaration="no"
  encoding="UTF-8" />
 <xsl:template match="/">
  <xsl:apply-templates mode="copy" />
 </xsl:template>
 <xsl:template match="*" mode="copy">
  <xsl:copy>
   <xsl:copy-of select="@*" />
   <xsl:apply-templates mode="copy" />
  </xsl:copy>
 </xsl:template>
 <!-- template for adding new attribute objectType to content editor applications -->
 <xsl:template
  match="PSXField[PSXPropertySet/PSXProperty/@name='mayHaveInlineLinks']/FieldRules"
  mode="copy" priority="10">
  <xsl:variable name="fieldName">
   <xsl:value-of select="../@name"></xsl:value-of>
  </xsl:variable>
  <xsl:copy>
   <xsl:copy-of select="@*" />
   <xsl:apply-templates mode="copy" />
   <xsl:if test="../PSXPropertySet/PSXProperty[@name='mayHaveInlineLinks']/Value='yes' and not(FieldInputTranslation/PSXFieldTranslation/PSXExtensionCallSet/PSXExtensionCall/name='Java/global/percussion/content/sys_manageLinksConverter')">
    <xsl:element name="FieldInputTranslation">
     <xsl:element name="PSXFieldTranslation">
      <xsl:element name="PSXExtensionCallSet">
       <xsl:attribute name="id">0</xsl:attribute>
       <xsl:element name="PSXExtensionCall">
        <xsl:attribute name="id">0</xsl:attribute>
        <xsl:element name="name">
         <xsl:text>Java/global/percussion/content/sys_manageLinksConverter</xsl:text>
        </xsl:element>
        <xsl:element name="PSXExtensionParamValue">
         <xsl:attribute name="id">0</xsl:attribute>
         <xsl:element name="value">
          <xsl:element name="PSXSingleHtmlParameter">
           <xsl:attribute name="id">0</xsl:attribute>
           <xsl:element name="name">
            <xsl:value-of select="$fieldName" />
           </xsl:element>
          </xsl:element>
         </xsl:element>
        </xsl:element>
       </xsl:element>
      </xsl:element>
     </xsl:element>
    </xsl:element>

   </xsl:if>
  </xsl:copy>
 </xsl:template>
</xsl:stylesheet>
