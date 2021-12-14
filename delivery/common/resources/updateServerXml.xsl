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
