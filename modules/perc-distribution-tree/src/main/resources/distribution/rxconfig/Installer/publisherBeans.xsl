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

<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:bn="http://www.springframework.org/schema/beans"
                exclude-result-prefixes="bn">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="yes"/>
    <xsl:param name="filename"></xsl:param>
    <xsl:param name="filedir">.</xsl:param>

    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/*">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
            <xsl:if test="not(bn:bean[@id = 'sys_ftpsDeliveryHandler'])">
            <bean id="sys_ftpsDeliveryHandler"
                  class="com.percussion.rx.delivery.impl.PSFtpsDeliveryHandler"
                  xmlns="http://www.springframework.org/schema/beans">
                     <xsl:comment>
                        Properties that can be set here:
                        timeout
                            (defaults to -1 as not defined.
                            It is the socket timeout in milliseconds for both when opening a
                            socket and a currently open connection).
                        usePassiveMode
                            (defaults to false.
                            Determines if using passive or active mode for the FTP client.
                            Defaults to use active mode. If using passive mode is on, then it
                            will also disable the remote verification
                        </xsl:comment>
                        <property name="usePassiveMode" value="true" />
                 </bean>
                <bean id="sys_ftps_default"
                      class="com.percussion.rx.delivery.impl.PSFtpsDeliveryHandler"
                      xmlns="http://www.springframework.org/schema/beans">
                         <xsl:comment>
                            Properties that can be set here:
                            timeout
                                (defaults to -1 as not defined.
                                It is the socket timeout in milliseconds for both when opening a
                                socket and a currently open connection).
                            usePassiveMode
                                (defaults to false.
                                Determines if using passive or active mode for the FTP client.
                                Defaults to use active mode. If using passive mode is on, then it
                                will also disable the remote verification
                        </xsl:comment>
                            <property name="usePassiveMode" value="true" />
                </bean>
                <bean id="sys_ftps"
                      class="com.percussion.rx.delivery.impl.PSFtpsDeliveryHandler"
                      xmlns="http://www.springframework.org/schema/beans">
                 <xsl:comment> Properties that can be set here:
                    timeout
                        (defaults to -1 as not defined.
                        It is the socket timeout in milliseconds for both when opening a
                        socket and a currently open connection).
                    usePassiveMode
                        (defaults to false.
                        Determines if using passive or active mode for the FTP client.
                        Defaults to use active mode. If using passive mode is on, then it
                        will also disable the remote verification
                 </xsl:comment>
                    <property name="usePassiveMode" value="true" />
                </bean>
      </xsl:if>
      </xsl:copy>
    </xsl:template>

</xsl:stylesheet>