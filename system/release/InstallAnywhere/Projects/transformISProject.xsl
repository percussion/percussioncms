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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:output method="xml"/>
   <!-- This Xsl is applied to all the Rx IS projects during the build time just before IS has a chance to compile the project -->
   <xsl:template match="*|@*">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
   </xsl:template>
   <!-- sets all file permissions to all read write and execute -->
   <xsl:template match="arrayItem[@type='com.installshield.product.actions.SourceFile']">
      <xsl:copy>
         <xsl:apply-templates select="@*"/>
         <property name="attributes" type="com.installshield.util.FileAttributes">
            <property name="attributes">511</property>
         </property>
         <xsl:apply-templates select="node()"/>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="property[@type='com.installshield.util.FileAttributes']"/>
   <!-- This template is a part of the fix for bug Rx-04-07-0150, note: the classpath itself is not cleaned up yet;
          uncommenting the template below will result in build time replacement of all launcher classpaths.
          So this classpath has to be the greatest common denominator for all the launchers that we have.
    -->
   <!-- set same classpath for all launchers -->
   <!--
   <xsl:template match="action[@class='com.installshield.product.actions.Launcher']/property[@name='classPath']">
      <property array="True" length="52" name="classPath" type="string">
         <arrayItem>lib/log4j.jar</arrayItem>
         <arrayItem>lib/xmlParserAPIs.jar</arrayItem>
         <arrayItem>lib/xercesImpl.jar</arrayItem>
         <arrayItem>lib/saxon.jar</arrayItem>
         <arrayItem>lib/rxextensions.jar</arrayItem>
         <arrayItem>lib/rxinstall.jar</arrayItem>
         <arrayItem>lib/psjniregistry.jar</arrayItem>
         <arrayItem>lib/rxserver.jar</arrayItem>
         <arrayItem>lib/rxclient.jar</arrayItem>
         <arrayItem>lib/jaas.jar</arrayItem>
         <arrayItem>lib/jndi.jar</arrayItem>
         <arrayItem>lib/providerutil.jar</arrayItem>
         <arrayItem>lib/js.jar</arrayItem>
         <arrayItem>lib/servlet.jar</arrayItem>
         <arrayItem>lib/server.jar</arrayItem>
         <arrayItem>lib/tcljava.jar</arrayItem>
         <arrayItem>lib/jacl.jar</arrayItem>
         <arrayItem>lib/soap.jar</arrayItem>
         <arrayItem>lib/ant.jar</arrayItem>
         <arrayItem>lib/jasper.jar</arrayItem>
         <arrayItem>lib/jaxp.jar</arrayItem>
         <arrayItem>lib/parser.jar</arrayItem>
         <arrayItem>lib/ldapbp.jar</arrayItem>
         <arrayItem>lib/ldap.jar</arrayItem>
         <arrayItem>lib/nis.jar</arrayItem>
         <arrayItem>lib/html.jar</arrayItem>
         <arrayItem>lib/mail.jar</arrayItem>
         <arrayItem>lib/activation.jar</arrayItem>
         <arrayItem>lib/rxworkflow.jar</arrayItem>
         <arrayItem>lib/rxpublisher.jar</arrayItem>
         <arrayItem>lib/rxmisctools.jar</arrayItem>
         <arrayItem>lib/rxuploader.jar</arrayItem>
         <arrayItem>lib/Tidy.jar</arrayItem>
         <arrayItem>jdbc/oracle/classes12.jar</arrayItem>
         <arrayItem>jdbc/sprinta/Sprinta2000.jar</arrayItem>
         <arrayItem>lib/rxagent.jar</arrayItem>
         <arrayItem>lib/rxtablefactory.jar</arrayItem>
         <arrayItem>lib/percbeans.jar</arrayItem>
         <arrayItem>lib/rxi18n.jar</arrayItem>
         <arrayItem>lib/xml4j.jar</arrayItem>
         <arrayItem>lib/docucomp.jar</arrayItem>
         <arrayItem>lib/serveruicomp.jar</arrayItem>
         <arrayItem>lib/psctoolkit5.jar</arrayItem>
         <arrayItem>lib/userextensions.jar</arrayItem>
         <arrayItem>lib/jai_codec.jar</arrayItem>
         <arrayItem>lib/jai_core.jar</arrayItem>
         <arrayItem>lib/jericho-html-1.2.jar</arrayItem>
         <arrayItem>lib/htmlConverter.jar</arrayItem>
         <arrayItem>lib/rxff.jar</arrayItem>
         <arrayItem>lib/htmlparser.jar</arrayItem>
         <arrayItem>jdbc/jtds/jtds.jar</arrayItem>
         <arrayItem>&quot;$P(udbClasspathSolaris.udbJarFilePath)&quot;</arrayItem>
      </property>
   </xsl:template>
   -->
</xsl:stylesheet>
