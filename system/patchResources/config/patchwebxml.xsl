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
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  exclude-result-prefixes="xalan">
  <xsl:output method="xml" indent="yes" xalan:indent-amount="4"/>
    <xsl:param name="filename"></xsl:param>
    <xsl:param name="filedir">.</xsl:param>

   <xsl:template match="/ | @* | node()">
         <xsl:copy>
               <xsl:apply-templates select="@* | node()" />
         </xsl:copy>
   </xsl:template>

   <xsl:template match="servlet[not(following-sibling::servlet)]">
     <xsl:copy>
        <xsl:apply-templates select="@* | node()" />
     </xsl:copy>
    <xsl:if test="not(//servlet/servlet-name[text()='cms-api'])">
        <servlet>
          <servlet-name>cms-api</servlet-name>
          <servlet-class>com.sun.jersey.spi.container.servlet.ServletContainer</servlet-class>
          <!-- The following initialization parameter is only required for registering 
             Jersey managed classes. Spring-managed classes are automatically registered. -->
    
          <init-param>
             <param-name>com.sun.jersey.config.property.packages</param-name>
             <param-value>com.percussion.cms.api.rest;com.wordnik.swagger.jaxrs.listing</param-value>
          </init-param>
          <init-param>
             <param-name>com.sun.jersey.api.json.POJOMappingFeature</param-name>
             <param-value>true</param-value>
          </init-param>
          <init-param>
             <param-name>com.sun.jersey.config.feature.DisableWADL</param-name>
             <param-value>true</param-value>
          </init-param>
          <init-param>
             <param-name>com.sun.jersey.spi.container.ContainerResponseFilters</param-name>
             <param-value>com.sun.jersey.server.linking.LinkFilter</param-value>
          </init-param>
        </servlet>
    </xsl:if>
   </xsl:template>

    <xsl:template match="servlet-mapping[not(following-sibling::servlet-mapping)]">
     <xsl:copy>
        <xsl:apply-templates select="@* | node()" />
     </xsl:copy>
    <xsl:if test="not(//servlet-mapping/servlet-name[text()='cms-api'])">
       <servlet-mapping>
          <servlet-name>cms-api</servlet-name>
          <url-pattern>/v8/*</url-pattern>
       </servlet-mapping>
    </xsl:if>
   </xsl:template>
   
      <xsl:template match="filter[not(following-sibling::filter)]">
     <xsl:copy>
        <xsl:apply-templates select="@* | node()" />
     </xsl:copy>
    <xsl:if test="not(//filter/filter-name[text()='PSIECompatibleFilter'])">
        <filter> 
            <filter-name><xsl:text>PSIECompatibleFilter</xsl:text></filter-name> 
            <filter-class><xsl:text>com.percussion.servlets.PSIECompatibleFilter</xsl:text></filter-class> 
        </filter>
    </xsl:if>
   </xsl:template>

   <xsl:template match="filter-mapping[not(following-sibling::filter-mapping)]">
     <xsl:copy>
        <xsl:apply-templates select="@* | node()" />
     </xsl:copy>
    <xsl:if test="not(//filter-mapping/filter-name[text()='PSIECompatibleFilter'])">
        <filter-mapping>
            <filter-name><xsl:text>PSIECompatibleFilter</xsl:text></filter-name> 
            <servlet-name><xsl:text>Faces Servlet</xsl:text></servlet-name> 
        </filter-mapping>
    </xsl:if>
   </xsl:template>

    <xsl:template match="mime-mapping[not(following-sibling::mime-mapping)]">
     <xsl:copy>
        <xsl:apply-templates select="@* | node()" />
     </xsl:copy>
    <xsl:if test="not(//mime-mapping/extension[text() = 'svg'])">
        <mime-mapping>
          <extension>svg</extension>
          <mime-type>image/svg+xml</mime-type>
        </mime-mapping>
    </xsl:if>
   </xsl:template>
   
   <!-- remove httpheadersecurityfilter -->
   <xsl:template match="filter[filter-name[text()='HttpHeaderSecurityFilter']]"/>
   <xsl:template match="filter-mapping[filter-name[text()='HttpHeaderSecurityFilter']]"/>
   
   <!-- remove cachecontrolfilter -->
   <xsl:template match="filter[filter-name[text()='CacheControlFilter']]"/>
   <xsl:template match="filter-mapping[filter-name[text()='CacheControlFilter']]"/>
   
    <xsl:template match="filter[not(following-sibling::filter)]">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" />
        </xsl:copy>
        <xsl:if test="not(//filter/filter-name[text()='PSSecurityHeaderFilter'])">
            <filter> 
                <filter-name><xsl:text>PSSecurityHeaderFilter</xsl:text></filter-name> 
                <filter-class><xsl:text>com.percussion.utils.security.PSSecurityHeaderFilter</xsl:text></filter-class> 
            </filter>
        </xsl:if>
   </xsl:template>

    <xsl:template match="filter-mapping[not(following-sibling::filter-mapping)]">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" />
        </xsl:copy>
        <xsl:if test="not(//filter-mapping/filter-name[text()='PSSecurityHeaderFilter'])">
            <filter-mapping> 
                <filter-name><xsl:text>PSSecurityHeaderFilter</xsl:text></filter-name> 
                <url-pattern>/*</url-pattern> 
            </filter-mapping>
        </xsl:if>
   </xsl:template>

</xsl:stylesheet>
