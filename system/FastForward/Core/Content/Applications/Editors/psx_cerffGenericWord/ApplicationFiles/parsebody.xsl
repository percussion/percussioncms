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

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:w="urn:schemas-microsoft-com:office:word" xmlns:dt="uuid:C2F41010-65B3-11d1-A29F-00AA00C14882" xmlns:html="http://www.w3.org/TR/REC-html40" exclude-result-prefixes="v o w dt" xmlns:psxi18n="urn:www.percussion.com/i18n" >
<xsl:import href="../psx_cerffGenericWord/contentfilter.xsl" /> 	
<xsl:strip-space elements="html:p html:span" /> 
<xsl:output method="xml" /> 
<xsl:variable name="styles" select="//html:head/html:style"/>

<xsl:template match="/">
<div class="rxbodyfield">
		<!-- Uncomment this line if you want to preserve the styles from word. If you preserve the word styles it may conflict with the styles on the assembly page and may render assembly page wrong.-->
      <!--<xsl:copy-of select="$styles"/>-->
		<xsl:apply-templates select="//html:body[1]/*" mode="contentfilter" />
</div>		
</xsl:template>

<!-- don't show any nodes that contain deleted text (Word's track changes) -->
<xsl:template match="html:del" />

</xsl:stylesheet>
