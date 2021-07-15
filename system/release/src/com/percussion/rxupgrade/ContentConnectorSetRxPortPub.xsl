<?xml version="1.0" encoding="UTF-8"?>
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

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- 
	This stylesheet is used to set the Rhythmyx server port in the Content Connector 
	configuration file "rxconfig/ContentConnector/contentconnector.xml". If the
	user has selected to install Content Connector then the contentconnector.xml file
	is installed in the "rxconfig/ContentConnector" directory. If the user has also
 	selected to install the content connector without the server, then he enters the value of the 
	port in the Installshield panel whose bean id is "PublisherPortPanel". The contents
	of this stylesheet is then resolved. During resolution "$W(installProperties.publisherHTTPPort)"
	changes to the server port entered by the user. The stylesheet is then applied to
	contentconnector.xml, thus setting the Rhythmyx Server port appropritely.
	-->

	<xsl:template match="/">
		<xsl:apply-templates select="." mode="copy"/>
	</xsl:template>
	<!-- copy any attribute or template -->
	<xsl:template match="@*|*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXProperty[@name='Port']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<Value>$W(installProperties.publisherHTTPPort)</Value>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
