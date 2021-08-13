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

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
                xmlns:html="http://www.w3.org/TR/REC-html40" exclude-result-prefixes="psxi18n"
                extension-element-prefixes="psxi18n">
<xsl:import href="../psx_cerffGenericWord/contentfilter.xsl" /> 

<xsl:strip-space elements="html:p html:span" /> 
<xsl:output method="xml" /> 

<xsl:template match="/">
<PSXParam>
	<displaytitle>
		<xsl:choose>
			<xsl:when test="string(//html:body[1]//html:p[@class='PageDisplayTitle'])">
				<xsl:apply-templates select="//html:body[1]//html:p[@class='PageDisplayTitle']" />
			</xsl:when>
			<xsl:when test="string(//html:body[1]//html:p[@class='MsoTitle'])">
				<xsl:apply-templates select="//html:body[1]//html:p[@class='MsoTitle']" />
			</xsl:when>
			<xsl:otherwise>COULD NOT FIND A TITLE</xsl:otherwise>
		</xsl:choose>
	</displaytitle>
	<description>
		<xsl:choose>
			<xsl:when test="string(//html:body[1]//html:p[@class='PageDescription'])">
				<xsl:apply-templates select="//html:body[1]//html:p[@class='PageDescription']" />
			</xsl:when>
			<xsl:otherwise>COULD NOT FIND AN ABSTRACT</xsl:otherwise>
		</xsl:choose>
	</description>
	<PageAuthor>
		<xsl:choose>
			<xsl:when test="string(//html:body[1]//html:p[@class='PageAuthor'])">
				<xsl:apply-templates select="//html:body[1]//html:p[@class='PageAuthor']" />
			</xsl:when>
			<xsl:otherwise>COULD NOT FIND AN AUTHOR</xsl:otherwise>
		</xsl:choose>
	</PageAuthor>	
</PSXParam>
</xsl:template>

<!-- don't show any nodes that contain deleted text (Word's track changes) -->
<xsl:template match="html:del" />

</xsl:stylesheet>
