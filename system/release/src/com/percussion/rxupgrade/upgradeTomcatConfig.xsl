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
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" encoding="UTF-8"/>
   <xsl:preserve-space elements="*"/>
   <xsl:template match="@*|node()">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
   </xsl:template>
   <!-- add new comments and nodes to the Server node -->
   <xsl:template match="Server">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <!-- quick test to see if the upgrade has added this listener classname already, don't add new nodes if they already exist. -->
         <xsl:if test="not(//Listener[@className='org.apache.catalina.mbeans.ServerLifecycleListener'])">
            <xsl:comment>Comment these entries out to disable JMX MBeans support </xsl:comment>
            <xsl:comment>You may also configure custom components (e.g. Valves/Realms) by
       including your own mbean-descriptor file(s), and setting the
       "descriptors" attribute to point to a ';' seperated list of paths
       (in the ClassLoader sense) of files to add to the default list.
       e.g. descriptors="/com/myfirm/mypackage/mbean-descriptor.xml"
  </xsl:comment>
            <Listener className="org.apache.catalina.mbeans.ServerLifecycleListener" debug="0"/>
            <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" debug="0"/>
            <xsl:comment>Global JNDI resources </xsl:comment>
            <GlobalNamingResources>
               <xsl:comment>Test entry for demonstration purposes </xsl:comment>
               <Environment name="simpleValue" type="java.lang.Integer" value="30"/>
               <xsl:comment>Editable user database that can also be used by
         UserDatabaseRealm to authenticate users</xsl:comment>
               <Resource name="UserDatabase" auth="Container" type="org.apache.catalina.UserDatabase" description="User database that can be updated and saved">
    </Resource>
               <ResourceParams name="UserDatabase">
                  <parameter>
                     <name>factory</name>
                     <value>org.apache.catalina.users.MemoryUserDatabaseFactory</value>
                  </parameter>
                  <parameter>
                     <name>pathname</name>
                     <value>conf/tomcat-users.xml</value>
                  </parameter>
               </ResourceParams>
            </GlobalNamingResources>
         </xsl:if>
         <xsl:apply-templates select="node()"/>
      </xsl:copy>
   </xsl:template>
   <!-- change the HTTP 1.1 connector class names from catalina to coyote, add 2 new attributes, if default values of other attributes are detected, modify to new default attributes otherwise copy unchanged -->
   <xsl:template match="//Connector">
      <xsl:choose>
         <xsl:when test="./@className='org.apache.catalina.connector.http.HttpConnector'">
            <xsl:copy>
               <xsl:copy-of select="@*"/>
               <xsl:attribute name="className">org.apache.coyote.tomcat4.CoyoteConnector</xsl:attribute>
               <xsl:apply-templates select="node()"/>
            </xsl:copy>
         </xsl:when>
         <xsl:when test="./@className='org.apache.ajp.tomcat4.Ajp13Connector'">
            <xsl:copy>
               <xsl:copy-of select="@*"/>
               <xsl:attribute name="className">org.apache.coyote.tomcat4.CoyoteConnector</xsl:attribute>
               <xsl:apply-templates select="node()"/>
            </xsl:copy>
         </xsl:when>
         <xsl:otherwise>
            <xsl:copy>
               <xsl:copy-of select="@*"/>
               <xsl:apply-templates select="node()"/>
            </xsl:copy>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <!-- update the SSL Connector Factory classname, otherwise copy unchanged -->
   <xsl:template match="//Connector/Factory">
      <xsl:choose>
         <xsl:when test="./@className='org.apache.catalina.net.SSLServerSocketFactory'">
            <xsl:copy>
               <xsl:copy-of select="@*"/>
               <xsl:attribute name="className">org.apache.coyote.tomcat4.CoyoteServerSocketFactory</xsl:attribute>
            </xsl:copy>
         </xsl:when>
         <xsl:otherwise>
            <xsl:copy>
               <xsl:copy-of select="@*"/>
               <xsl:apply-templates select="node()"/>
            </xsl:copy>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <!-- add autoDeploy=true attribute to Host element -->
   <xsl:template match="//Service/Engine/Host">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:attribute name="autoDeploy">true</xsl:attribute>
         <xsl:apply-templates select="node()"/>
      </xsl:copy>
   </xsl:template>
   <!-- change memory realm to user database realm -->
   <xsl:template match="//Realm">
      <Realm>
      <xsl:choose>
         <xsl:when test="./@className='org.apache.catalina.realm.MemoryRealm'">
            <xsl:attribute name="className">org.apache.catalina.realm.UserDatabaseRealm</xsl:attribute>
            <xsl:attribute name="resourceName">UserDatabase</xsl:attribute>
         </xsl:when>
         <xsl:otherwise>
            <xsl:copy>
               <xsl:copy-of select="@*"/>
               <xsl:apply-templates select="node()"/>
            </xsl:copy>
         </xsl:otherwise>         
      </xsl:choose>
      </Realm>
   </xsl:template>
   <!-- comment out access log values due to Tomcat bug -->
   <xsl:template match="//Valve[@className='org.apache.catalina.valves.AccessLogValve']">
      <xsl:text disable-output-escaping="yes">
      &lt;!-- This valve is commented out due to a bug in Tomcat 4.1.27
      </xsl:text>
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates select="node()"/>
      </xsl:copy>      
      <xsl:text disable-output-escaping="yes">
      --&gt;
      </xsl:text>
   </xsl:template>
   <!-- change user to username and driverName to url for all Resource Parameters of type javax.sql.DataSource -->
   <xsl:template match="//ResourceParams">
      <xsl:choose>
         <xsl:when test="./@name=../Resource[@type='javax.sql.DataSource']/@name">
            <xsl:copy>
               <xsl:copy-of select="@*"/>
               <xsl:for-each select="./parameter">
                  <parameter>
                     <xsl:copy-of select="@*"/>
                     <xsl:choose>
                        <xsl:when test="./name='user'">
                           <name>username</name>
                           <value>
                              <xsl:value-of select="./value"/>
                           </value>
                        </xsl:when>
                        <xsl:when test="./name='driverName'">
                           <name>url</name>
                           <value>
                              <xsl:value-of select="./value"/>
                           </value>
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:copy-of select="@*"/>
                           <xsl:apply-templates select="node()"/>
                        </xsl:otherwise>
                     </xsl:choose>
                  </parameter>
               </xsl:for-each>
            </xsl:copy>
         </xsl:when>
         <xsl:otherwise>
            <xsl:copy>
               <xsl:for-each select="@*">
                  <xsl:copy/>
               </xsl:for-each>
               <xsl:apply-templates select="node()"/>
            </xsl:copy>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
</xsl:stylesheet>
