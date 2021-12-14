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
	<xsl:variable name="spi" select="//SecurityProviders/PSXSecurityProviderInstance[@type='DirectoryConn']"/>
	<xsl:variable name="ds" select="//DirectoryServices"/>
	<xsl:template match="/">
		<xsl:apply-templates select="." mode="copy"/>
	</xsl:template>
	<!-- Template to change the oracle driver classname -->
	<xsl:template match="JdbcDriverConfigs/PSXJdbcDriverConfig[@driverName='oracle:thin']" mode="copy">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:if test="@className='oracle.jdbc.driver.OracleDriver'">
                <xsl:attribute name="className">oracle.jdbc.OracleDriver</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates mode="copy"/>
        </xsl:copy>
    </xsl:template>
	<!-- Template to change the DB2 driver classname -->
	<xsl:template match="JdbcDriverConfigs/PSXJdbcDriverConfig[@driverName='db2']" mode="copy">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:if test="@className='COM.ibm.db2.jdbc.app.DB2Driver'">
                <xsl:attribute name="className">com.ibm.db2.jcc.DB2Driver</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates mode="copy"/>
        </xsl:copy>
    </xsl:template>
	<!-- Remove Sybase jdbc driver configuration if exists -->
	<xsl:template match="JdbcDriverConfigs/PSXJdbcDriverConfig[@driverName='sybase']" mode="copy"/>
	<!-- Template to add jtds configuration on upgrade -->
	<xsl:template match="BackEndConnections/Pools" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:if test="not(PSXBackEndConnection/jdbcClassName[.='net.sourceforge.jtds.jdbc.Driver'])">
				<PSXBackEndConnection id="10">
					<jdbcDriverName>jtds:sqlserver</jdbcDriverName>
					<jdbcClassName>net.sourceforge.jtds.jdbc.Driver</jdbcClassName>
					<serverName>*</serverName>
					<connectionMin>0</connectionMin>
					<connectionMax>-1</connectionMax>
					<connectionIdleTimeout>900</connectionIdleTimeout>
					<connectionRefreshPeriod>3600</connectionRefreshPeriod>
				</PSXBackEndConnection>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXBackEndConnection[jdbcClassName='net.sourceforge.jtds.jdbc.Driver']/connectionRefreshPeriod[.=-1]" mode="copy">
		<connectionRefreshPeriod>3600</connectionRefreshPeriod>
	</xsl:template>
	<!-- Template to modify the PSXServerCacheSettings maxPageSize-->
	<xsl:template match="PSXServerCacheSettings" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:if test="not(@maxPageSize) or @maxPageSize='102400'">
				<xsl:attribute name="maxPageSize">256000</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!--
      Template to fix the PSXSecurityProviderInstance element with type as 
      DirectoryConn, if that element does not exist then this template will 
      not be used. 
   -->
	<xsl:template match="SecurityProviders/PSXSecurityProviderInstance[@type='DirectoryConn']" mode="copy">
		<xsl:copy>
			<xsl:variable name="connname">
				<xsl:value-of select="name"/>
			</xsl:variable>
			<xsl:variable name="pos">
				<xsl:for-each select="$spi">
					<xsl:if test="name=$connname">
						<xsl:value-of select="position()"/>
					</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:choose>
				<!--
               If there is a directory services element then we have already 
               modified this file we need not do anything.
            -->
				<xsl:when test="$ds">
					<xsl:apply-templates mode="copy"/>
				</xsl:when>
				<xsl:otherwise>
					<name>
						<xsl:value-of select="name"/>
					</name>
					<Properties/>
					<xsl:copy-of select="groupProviders"/>
					<DirectoryProvider>
						<PSXProvider class="com.percussion.security.PSDirectoryServerCataloger" type="directory">
							<PSXReference name="{concat('PSXDirectorySet',$pos)}" type="com.percussion.design.objectstore.PSDirectorySet"/>
						</PSXProvider>
					</DirectoryProvider>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	<!-- 
      Template to add the Authentications, DirectoryServices, and JdbcDriverConfigs elements. 
   -->
	<xsl:template match="PSXServerConfiguration" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<!--
            If at least one PSXSecurityProviderInstance exists with type 
            attribute value as DirectoryConn and if there is no 
            DirectoryServices element exists then add Authentications and 
            DirectoryServices elements.
         -->
			<xsl:if test="$spi and not($ds)">
				<Authentications>
					<xsl:for-each select="$spi">
						<PSXAuthentication name="{concat('PSXAuthentication',position())}" scheme="{Properties/authenticationScheme}">
							<Credentials>
								<xsl:variable name="user" select="Properties/authenticationPrincipal"/>
								<User attributeName="{substring-before($user, '=')}">
									<xsl:value-of select="substring-after($user, '=')"/>
								</User>
								<Password encrypted="no" attributeName="{Properties/credentialAttribute}">
									<xsl:value-of select="Properties/authenticationCredential"/>
								</Password>
							</Credentials>
							<xsl:if test="Properties/credentialFilterClassName">
								<FilterExtensionName>
									<xsl:value-of select="Properties/credentialFilterClassName"/>
								</FilterExtensionName>
							</xsl:if>
						</PSXAuthentication>
					</xsl:for-each>
				</Authentications>
				<DirectoryServices>
					<Directories>
						<xsl:for-each select="$spi">
							<PSXDirectory catalog="shallow" name="{concat('PSXDirectory',position())}">
								<Factory>
									<xsl:value-of select="Properties/providerClassName"/>
								</Factory>
								<Authentication>
									<PSXReference name="{concat('PSXAuthentication',position())}" type="com.percussion.design.objectstore.PSAuthentication"/>
								</Authentication>
								<ProviderUrl>
									<xsl:variable name="providerUrl" select="Properties/providerURL"/>
									<xsl:value-of select="translate($providerUrl,' ','')"/>
								</ProviderUrl>
								<Attributes>
									<xsl:for-each select="Properties/*">
										<xsl:choose>
											<xsl:when test="name()='credentialFilterClassName' or name()='credentialAttribute' or name()='authenticationScheme' or name()='providerURL' or name()='authenticationCredential' or name()='principalAttribute' or name()='authenticationPrincipal' or name()='providerClassName'"/>
											<xsl:otherwise>
												<Attribute name="{.}"/>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:for-each>
								</Attributes>
							</PSXDirectory>
						</xsl:for-each>
					</Directories>
					<DirectorySets>
						<xsl:for-each select="$spi">
							<PSXDirectorySet name="{concat('PSXDirectorySet',position())}">
								<PSXReference name="{concat('PSXDirectory',position())}" type="com.percussion.design.objectstore.PSDirectory"/>
								<RequiredAttributeNames>
									<Attribute name="roleAttributeName"/>
									<Attribute name="emailAttributeName"/>
									<Attribute name="objectAttributeName">
										<xsl:value-of select="Properties/principalAttribute"/>
									</Attribute>
								</RequiredAttributeNames>
							</PSXDirectorySet>
						</xsl:for-each>
					</DirectorySets>
				</DirectoryServices>
			</xsl:if>
			<xsl:if test="not(PSXSearchConfig)">
				<PSXSearchConfig adminMaster="yes" maxSearchResult="-1" fullTextSearchEnabled="yes" traceEnabled="no">
					<Properties>
						<Property name="indexRootDir">sys_search/lucene</Property>
						<Property name="synonym_expansion">no</Property>
						<Property name="index_on_startup">no</Property>
					</Properties>
					<ResultProcessingExits>
						<PSXExtensionCallSet id="102">
							<PSXExtensionCall id="0">
								<name>Java/global/percussion/search/sys_AddVariantSiteFolder</name>
							</PSXExtensionCall>
							<PSXExtensionCall id="0">
								<name>Java/global/percussion/search/sys_AddThumbnailURL</name>
							</PSXExtensionCall>
							<PSXExtensionCall id="0">
								<name>Java/global/percussion/search/sys_cleanFolderSearchResults</name>
							</PSXExtensionCall>
						</PSXExtensionCallSet>
					</ResultProcessingExits>
					<Analyzers/>
					<TextConverters/>
				</PSXSearchConfig>
			</xsl:if>
			<xsl:if test="not(JdbcDriverConfigs)">
				<JdbcDriverConfigs>
					<PSXJdbcDriverConfig className="oracle.jdbc.OracleDriver" containerTypeMapping="Oracle8" driverName="oracle:thin"/>
					<PSXJdbcDriverConfig className="net.sourceforge.jtds.jdbc.Driver" containerTypeMapping="MS SQLSERVER2000" driverName="jtds:sqlserver"/>
					<PSXJdbcDriverConfig className="com.ibm.db2.jcc.DB2Driver" containerTypeMapping="DB2" driverName="db2"/>
				</JdbcDriverConfigs>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXSearchConfig" mode="copy">
		<xsl:copy>
			<xsl:if test="not(@maxSearchResult)">
				<xsl:attribute name="maxSearchResult">-1</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:if test="not(ResultProcessingExits)">
				<ResultProcessingExits>
					<PSXExtensionCallSet id="102">
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/search/sys_AddVariantSiteFolder</name>
						</PSXExtensionCall>
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/search/sys_AddThumbnailURL</name>
						</PSXExtensionCall>
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/search/sys_cleanFolderSearchResults</name>
						</PSXExtensionCall>
					</PSXExtensionCallSet>
				</ResultProcessingExits>
			</xsl:if>
			<xsl:if test="not(Analyzers)">
				<Analyzers/>
			</xsl:if>
			<xsl:if test="not(TextConverters)">
				<TextConverters/>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXSearchConfig/Properties" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:if test="not(Property[@name='indexRootDir'])">
				<Property name="indexRootDir">sys_search/lucene</Property>
			</xsl:if>
			<xsl:if test="not(Property[@name='synonym_expansion'])">
				<Property name="synonym_expansion">no</Property>
			</xsl:if>
			<xsl:if test="not(Property[@name='index_on_startup'])">
				<Property name="index_on_startup">no</Property>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<!-- Remove unused search configuration attributes -->
	<xsl:template match="@*[name()='serverHost']" mode="copy" priority="10"/>
	<xsl:template match="@*[name()='serverPort']" mode="copy" priority="10"/>
	<!-- Remove unused search configuration properties -->
	<xsl:template match="PSXSearchConfig/Properties/Property[@name='configDir']" mode="copy"/>
	<xsl:template match="PSXSearchConfig/Properties/Property[@name='rxqh_expansionlevel']" mode="copy"/>
	<xsl:template match="PSXSearchConfig/Properties/Property[@name='daemonport']" mode="copy"/>
	<!-- replace all the plugin declarations with 1.5 plugin declaration -->
	<xsl:template match="PSXJavaPluginConfig" mode="copy">
		<PSXJavaPluginConfig>
			<PSXJavaPlugin browserkey="Any" downloadlocation="http://java.sun.com/update/1.5.0/jinstall-1_5_0_12-windows-i586.cab#Version=1,5,0,12" oskey="Any" versioningtype="dynamic" versiontouse="1.5.0_12"/>
		</PSXJavaPluginConfig>
	</xsl:template>
	<xsl:template match="@*|*|comment()" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
