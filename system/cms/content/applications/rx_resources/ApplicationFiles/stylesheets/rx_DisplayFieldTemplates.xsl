<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
   <!--
This file contains the templates for all matching fields by name that override the display field vale. This scheme is useful when one wants to override the field display value that is normally computed by the content editor. For example in Related Content Search Results, we want to display the variant using thumbnail. We can define the variant field (name for example id sys_variant) in the Content Editor System Definition as shown below:
-->
   <!-- Content Editor Field definition begin
<PSXField defaultSearchLabel="Variant" forceBinary="no" modificationType="none" name="sys_variant" showInPreview="no" showInSummary="no" type="system" userSearchable="yes">
   <DataLocator>
      <PSXExtensionCall id="0">
         <name>Java/global/percussion/generic/sys_MakeIntLink</name>
         <PSXExtensionParamValue id="0">
            <value>
               <PSXTextLiteral id="0">
                  <text>../sys_casSupport/PublicationUrl.xml</text>
               </PSXTextLiteral>
            </value>
         </PSXExtensionParamValue>
      </PSXExtensionCall>
   </DataLocator>
   <OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
</PSXField>
 Content Editor Field definition end -->
   <!-- 
This field definition generates value that is a stub for the internal link URL that can be used in the XSL template for which a sample is provided below. This link URL needs the sys_contentid and sys_variantid parameters to generate a valid preview URL. The implementer must note that it is his/her responsiblity to generate appropriate output suitable for the field display type specified for the display column while configuring the display format in the workbench. For example if the display column id is of type Image, the field value produced by overriding template must be a link to an image binary. If the variant produced is not a binary and the column display type is Image, the column cannot be rendered.
 -->
   <!--
 The following template is a working sample that overrides a content editor field display value as defined above and produces a an assembly URL that could point to a binary image depending on variantid. The template is called with the content id and variant id parameters that can be used to generate the value appropriately. The mode must be rc_res_displayfield and the output of the template must be the XML block like this:
   <ResultField name="[fieldname]">[newfieldvalue]</ResultField>
The template match pattern must be ResultField[@name='fieldname'] where fieldname is the name of the field that is being overridden.

Limitations of this scheme are:
1. It is currently valid for search results for active assembly and document assembly. The content explorer search results cannot make use of this scheme.
2. If the field display type is Image, the image is rendered only in the main view of the applet not in the navigation pane of the applet. This means a display column of type Image should not be defined as category field.
-->
<!-- Template used in the sample applications we ship -->
   <xsl:template match="ResultField[@name='sys_variantname']" mode="rc_res_displayfield">
      <xsl:param name="sys_contentid"/>
      <xsl:param name="sys_revision"/>
      <xsl:param name="sys_contentypeid"/>
      <xsl:param name="sys_variantid"/>
      <ResultField name="{@name}">
         <xsl:value-of select="document(concat(., '&amp;keyPart1=', $sys_variantid))/CmsObject/@objectName"/>
      </ResultField>
   </xsl:template>
</xsl:stylesheet>
