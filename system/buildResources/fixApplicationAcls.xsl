<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    <xsl:template match="PSXAcl">
        <PSXAcl id="0">
            <PSXAclEntry id="0" type="user">
                <name>Default</name>
                <securityProviderType>Any</securityProviderType>
                <securityProviderInstance/>
                <applicationAccessLevel dataCreate="yes" dataDelete="yes" 
                    dataQuery="yes" dataUpdate="yes" designDelete="no" 
                    designRead="no" designUpdate="no" modifyAcl="no"/>
            </PSXAclEntry>
            <PSXAclEntry id="0" type="role">
                <name>Admin</name>
                <securityProviderType>Any</securityProviderType>
                <securityProviderInstance/>
                <applicationAccessLevel dataCreate="yes" dataDelete="yes" 
                    dataQuery="yes" dataUpdate="yes" designDelete="yes" 
                    designRead="yes" designUpdate="yes" modifyAcl="yes"/>
            </PSXAclEntry>
            <multiMembershipBehavior>mergeMaximumAccess</multiMembershipBehavior>
        </PSXAcl>
    </xsl:template>
    <xsl:template match="*|@*|comment()|text()">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
