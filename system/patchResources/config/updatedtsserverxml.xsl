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

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:output method="xml" indent="yes"/>

   <xsl:variable name="ssl-connector"><Connector port="8443" URIEncoding="UTF-8" protocol="org.apache.coyote.http11.Http11NioProtocol" connectionTimeout="20000" SSLEnabled="true" maxThreads="150" scheme="https" secure="true" keystoreFile="conf/.keystore" clientAuth="false" sslProtocol="TLS" sslEnabledProtocols="TLSv1.2,TLSv1.1" 
    ciphers="TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384,TLS_ECDH_RSA_WITH_AES_256_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256,TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA"
     xpoweredBy="false" address="0.0.0.0" compression="on" compressableMimeType="text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json,application/xml" compressionMinSize="256"
    /></xsl:variable>
   <!-- hack to simulate node set without exslt:node-set or xslt2 , we find node in this xslt document -->
   <xsl:variable name="ssl-connector-node" select="document('')//xsl:variable[@name='ssl-connector']/node()"/>
   
    <xsl:variable name="http-connector"><Connector port="9980" protocol="org.apache.coyote.http11.Http11NioProtocol" connectionTimeout="20000" redirectPort="8443" URIEncoding="UTF-8" xpoweredBy="false" address="0.0.0.0" compression="on" compressableMimeType="text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json,application/xml" compressionMinSize="256"/></xsl:variable>
   <!-- hack to simulate node set without exslt:node-set or xslt2 , we find node in this xslt document -->
   <xsl:variable name="http-connector-node" select="document('')//xsl:variable[@name='http-connector']/node()"/>
   
   
   <xsl:variable name="ajp-connector"><Connector port="9982" protocol="AJP/1.3" redirectPort="8443" xpoweredBy="false" address="0.0.0.0"/></xsl:variable>
   <!-- hack to simulate node set without exslt:node-set or xslt2 , we find node in this xslt document -->
   <xsl:variable name="ajp-connector-node" select="document('')//xsl:variable[@name='ajp-connector']/node()"/>
   
   
   <!-- Identity Transform -->
   <xsl:template match="/ | @* | node()">
         <xsl:copy>
               <xsl:apply-templates select="@* | node()" />
         </xsl:copy>
   </xsl:template>

 
 	<xsl:template match="Connector[@SSLEnabled='true' and not(@perc-no-update='true')]">
 		<xsl:apply-templates select="." mode="tester">
 			<xsl:with-param name="force-attrs" select="'|xpoweredBy|scheme|sslProtocol|secure|protocol'"/>
 			<xsl:with-param name="del-attrs"  select="''"/>
 		    <xsl:with-param name="update-node" select="$ssl-connector-node"/>
 		</xsl:apply-templates>
 	</xsl:template>
 	
 	
    <xsl:template match="Connector[not(@SSLEnabled='true') and not(contains(@protocol,'AJP')) and not(@perc-no-update='true')]">
 		<xsl:apply-templates select="." mode="tester">
 			<xsl:with-param name="force-attrs" select="'|xpoweredBy|'"/>
 			<xsl:with-param name="del-attrs"  select="''"/>
 		    <xsl:with-param name="update-node" select="$http-connector-node"/>
 		</xsl:apply-templates>
 	</xsl:template>
 
   <xsl:template match="Connector[contains(@protocol,'AJP') and not(@perc-no-update='true')]">
 		<xsl:apply-templates select="." mode="tester">
 			<xsl:with-param name="force-attrs" select="'|xpoweredBy|'"/>
 			<xsl:with-param name="del-attrs"  select="''"/>
 		    <xsl:with-param name="update-node" select="$ajp-connector-node"/>
 		</xsl:apply-templates>
 	</xsl:template>
 	
    <xsl:template match="node()" mode="tester">
    	<xsl:param name="force-attrs"></xsl:param>
    	<xsl:param name="del-attrs"></xsl:param>
    	<xsl:param name="update-node"></xsl:param>
    	<xsl:variable name="existing" select="."/>
 	    <xsl:copy>
 	    	  <xsl:for-each select="$update-node/@*">
 	    	  	<xsl:choose>
 	    	  		<xsl:when test="$existing/@*[name(.) = name(current())] and not(contains($force-attrs,concat('|',name(.),'|')))">
 	    	  			<!-- Attribute exists we are not forcing -->
 	    	  			<xsl:attribute name="{name(.)}"><xsl:value-of select="$existing/@*[name(.) = name(current())]"/></xsl:attribute>
 	    	  		</xsl:when>
 	    	  		<!-- Force attribute or add if new -->
 	    	  		<xsl:otherwise><xsl:attribute name="{name(.)}"><xsl:value-of select="."/></xsl:attribute></xsl:otherwise>
 	    	  	</xsl:choose>
 	    	  </xsl:for-each>
 	    	  <xsl:for-each select="$existing/@*">
 	    	    <!-- Add remaining unknown attributes or delete if in del-attrs -->
 	    	  	<xsl:if test="not($update-node/@*[name(.) = name(current())]) and not(contains($del-attrs,concat('|',name(.),'|')))">
 	    	  		<xsl:attribute name="{name(.)}"><xsl:value-of select="."/></xsl:attribute>
 	    	  	</xsl:if>
 	    	  </xsl:for-each>
         </xsl:copy>
    </xsl:template>
    
    <xsl:template match="Engine">
        <xsl:copy xml:space='preserve'>
            <xsl:apply-templates select="@* | node()" />
            <xsl:if test="not(Valve[@className = 'org.apache.catalina.valves.RemoteIpValve'])">
                <Valve
                   className="org.apache.catalina.valves.RemoteIpValve"
                      internalProxies="127\.0\.0\.1"
                      remoteIpHeader="x-forwarded-for"
                      proxiesHeader="x-forwarded-by"
                      protocolHeader="x-forwarded-proto" />
            </xsl:if>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
