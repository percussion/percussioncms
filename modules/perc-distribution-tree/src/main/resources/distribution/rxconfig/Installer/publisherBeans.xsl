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

<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="bn">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="yes"/>
    <xsl:param name="filename"/>
    <xsl:param name="filedir">.</xsl:param>

    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Delete the built-in delivery handlers -->
    <xsl:template match="bean[@id='sys_publishMessageHandlerContainerQ']" />
    <xsl:template match="bean[@id='sys_filesystem']" />
    <xsl:template match="bean[@id='sys_fileDeliveryHandler']" />
    <xsl:template match="bean[@id='sys_ftps']" />
    <xsl:template match="bean[@id='sys_ftpsDeliveryHandler']" />
    <xsl:template match="bean[@id='sys_ftps_default']" />
    <xsl:template match="bean[@id='sys_amazons3']" />
    <xsl:template match="bean[@id='sys_amazons3DeliveryHandler']" />
    <xsl:template match="bean[@id='sys_ftp']" />
    <xsl:template match="bean[@id='sys_ftp_default']" />
    <xsl:template match="bean[@id='sys_databaseDeliveryHandler']" />
    <xsl:template match="bean[@id='sys_sftp']" />
    <xsl:template match="bean[@id='sys_sftp_default']" />
    <xsl:template match="bean[@id='sys_sftpDeliveryHandler']" />
    <xsl:template match="bean[@id='sys_deliveryHandlerRunner']" />
    <xsl:template match="bean[@id='sys_metadataDeliveryHandler']" />

</xsl:stylesheet>
