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

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" />
    <xsl:template match="/ | @* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" />
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.gadget.socialPromotionConfiguration']]">
        <PackageFileEntry>
            <packageName>perc.gadget.socialPromotionConfiguration</packageName>
            <status>UNINSTALL</status>
        </PackageFileEntry>
    </xsl:template>
    
    <xsl:template match="//PackageFileList/PackageFileEntry[last()]">
        <xsl:copy xml:space='preserve'>
            <xsl:apply-templates/>
        </xsl:copy>
        <xsl:if test="not(../PackageFileEntry[packageName/text()='perc.gadget.socialPromotionConfiguration'])">
            <PackageFileEntry>
                <packageName>perc.gadget.socialPromotionConfiguration</packageName>
                <status>UNINSTALL</status>
            </PackageFileEntry>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>