<?xml version='1.0' encoding='UTF-8'?>
<!--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:html="http://www.w3.org/TR/REC-html40" exclude-result-prefixes="psxi18n"
                xmlns:psxi18n="com.percussion.i18n.PSI18nUtils" extension-element-prefixes="psxi18n" >

<xsl:strip-space elements="html:p html:span" /> 
<xsl:output method="xml" /> 

<!-- don't show any nodes that contain deleted text (Word's track changes) -->
<xsl:template match="html:del" mode="contentfilter"/>

<!-- filter out all <font> tags without class attributes -->
<xsl:template match="font[not(@class)]" >
   <xsl:apply-templates select="child::node()" mode="contentfilter" /> 
</xsl:template> 


<xsl:template match="html:p[@class='PageDisplayTitle'] | p[@class='PageDisplayTitle']" mode="contentfilter" />

<xsl:template match="html:p[@class='PageDescription'] | p[@class='PageDescription']" mode="contentfilter" />

<xsl:template match="html:p[@class='PageAuthor'] | p[@class='PageAuthor']" mode="contentfilter" />

<xsl:template match="html:xml | xml" mode="contentfilter" />

<!-- default template, just copy it -->
<xsl:template match="*" mode="contentfilter">
   <xsl:copy><xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="contentfilter" />    		
   </xsl:copy>
</xsl:template>

</xsl:stylesheet>













