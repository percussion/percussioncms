<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <!-- main template -->
    <xsl:template match="@*|node()">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>
    
    <!-- remove old ServerLifecycleListner -->
    <xsl:template match="Listener[@className='org.apache.catalina.mbeans.ServerLifecycleListener']"/>
   
    <xsl:template match="Listener[not(following-sibling::Listener)]">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
        <xsl:if test="not(//Listener[@className='org.apache.catalina.core.JreMemoryLeakPreventionListener'])">
            <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener"/>
        </xsl:if>
         <xsl:if test="not(//Listener[@className='org.apache.catalina.core.ThreadLocalLeakPreventionListener'])">
            <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener"/>
        </xsl:if>
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
			 <Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true" maxThreads="150" scheme="https" secure="true" clientAuth="false" sslProtocol="TLS" keystoreFile="conf/.keystore" xpoweredBy="false" address="0.0.0.0"/>
            </xsl:if>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="Connector" priority="90">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:if test="@scheme='https' and not(@keystoreFile)">
                 <xsl:attribute name="keystoreFile">conf/.keystore</xsl:attribute>
            </xsl:if>
            <xsl:if test="not(@address)">
                <xsl:attribute name="address">0.0.0.0</xsl:attribute>
            </xsl:if>
            <xsl:if test="not(@xpoweredBy)">
                <xsl:attribute name="xpoweredBy">false</xsl:attribute>
            </xsl:if>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
