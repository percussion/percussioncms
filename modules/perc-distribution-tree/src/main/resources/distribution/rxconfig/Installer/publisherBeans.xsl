<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
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
    <!-- rx 7.3.2 had this capitalized -->
    <xsl:template match="bean[@id='sys_amazonS3']" />
    <xsl:template match="bean[@id='sys_amazonS3DeliveryHandler']" />
    <xsl:template match="bean[@id='sys_ftp']" />
    <xsl:template match="bean[@id='sys_ftp_default']" />
    <xsl:template match="bean[@id='sys_databaseDeliveryHandler']" />
    <xsl:template match="bean[@id='sys_sftp']" />
    <xsl:template match="bean[@id='sys_sftp_default']" />
    <xsl:template match="bean[@id='sys_sftpDeliveryHandler']" />
    <xsl:template match="bean[@id='sys_deliveryHandlerRunner']" />
    <xsl:template match="bean[@id='sys_metadataDeliveryHandler']" />

</xsl:stylesheet>
