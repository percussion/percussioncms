<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <!-- main template -->
    <xsl:template match="@*|node()">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>
    <xsl:template match="Engine" priority="100">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:if test="not(Valve/@className = 'com.percussion.tomcat.valves.PSSimpleRedirectorValve')">
                <Valve className="com.percussion.tomcat.valves.PSSimpleRedirectorValve" targetHost="localhost" serviceNames="perc-form-processor,perc-common-ui,perc-metadata-services,perc-comments-services,feeds,perc-caching,perc-membership-services,perc-generickey-services,perc-polls-services,perc-thirdparty-services"/>          
            </xsl:if>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="Valve[@className = 'com.percussion.tomcat.valves.PSSimpleRedirectorValve']" priority="100">
        <Valve className="com.percussion.tomcat.valves.PSSimpleRedirectorValve" targetHost="localhost" serviceNames="perc-form-processor,perc-common-ui,perc-metadata-services,perc-comments-services,feeds,perc-membership-services,perc-generickey-service,perc-polls-services,perc-thirdparty-services"/>          
    </xsl:template>
    <xsl:template match="Realm[@resourceName = 'UserDatabase' and @className='org.apache.catalina.realm.UserDatabaseRealm']" priority="100">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:attribute name="digest">sha</xsl:attribute>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="Service">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:if test="not(Connector[@scheme='https'])">
			<Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true" maxThreads="150" scheme="https" secure="true" clientAuth="false" sslProtocol="TLS" keystoreFile="conf/.keystore"/>
             </xsl:if>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="Connector[@scheme='https' and not(@keystoreFile)]" priority="100">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:attribute name="keystoreFile">conf/.keystore</xsl:attribute>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="Host">
        <xsl:copy>
            <xsl:apply-templates select="@*[not(local-name() = 'xmlNamespaceAware' or local-name() = 'xmlValidation')]|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
