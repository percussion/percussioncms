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
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<!DOCTYPE xsl:stylesheet [
	<!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
		%HTMLlat1;
	<!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
	<!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:w="urn:schemas-microsoft-com:office:word" xmlns:dt="uuid:C2F41010-65B3-11d1-A29F-00AA00C14882" xmlns:html="http://www.w3.org/TR/REC-html40" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >

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













