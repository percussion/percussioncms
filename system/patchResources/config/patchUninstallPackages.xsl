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

<!-- xslt for reverting and removing packages for an uninstall. -->
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/ | @* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" />
        </xsl:copy>
    </xsl:template>
	<!--  
	To uninstall a package, add the entry:
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='<packagename>']]">
		<PackageFileEntry>
			<packageName><packagename></packageName>
			<status>UNINSTALL</status>
		</PackageFileEntry>
    </xsl:template>
	
	To revert a package, add the entry:	
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='<packagename>']]">
		<PackageFileEntry>
			<packageName><packagename></packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	-->
	<!-- examples:
	-->
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.gadget.siteimprove']]">
		<PackageFileEntry>
			<packageName>perc.gadget.siteimprove</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>	
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.baseWidgets']]">
		<PackageFileEntry>
			<packageName>perc.baseWidgets</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>			
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widgets.nav']]">
		<PackageFileEntry>
			<packageName>perc.widgets.nav</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>	
		<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.eventWidget']]">
		<PackageFileEntry>
			<packageName>perc.eventWidget</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>	
		<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.FileAssetWidget']]">
		<PackageFileEntry>
			<packageName>perc.FileAssetWidget</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.fileAutoList']]">
		<PackageFileEntry>
			<packageName>perc.fileAutoList</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.flashWidget']]">
		<PackageFileEntry>
			<packageName>perc.flashWidget</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.iframe']]">
		<PackageFileEntry>
			<packageName>perc.widget.iframe</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.form']]">
		<PackageFileEntry>
			<packageName>perc.widget.form</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.commentForm']]">
		<PackageFileEntry>
			<packageName>perc.widget.commentForm</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.PageAutoListWidget']]">
		<PackageFileEntry>
			<packageName>perc.PageAutoListWidget</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.blogIndexPage']]">
		<PackageFileEntry>
			<packageName>perc.widget.blogIndexPage</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.taglist']]">
		<PackageFileEntry>
			<packageName>perc.widget.taglist</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.categoryList']]">
		<PackageFileEntry>
			<packageName>perc.widget.categoryList</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.directory']]">
		<PackageFileEntry>
			<packageName>perc.widget.directory</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.archiveList']]">
		<PackageFileEntry>
			<packageName>perc.widget.archiveList</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.ImageAutoListWidget']]">
		<PackageFileEntry>
			<packageName>perc.ImageAutoListWidget</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.comments']]">
		<PackageFileEntry>
			<packageName>perc.widget.comments</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.title']]">
		<PackageFileEntry>
			<packageName>perc.widget.title</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.blog']]">
		<PackageFileEntry>
			<packageName>perc.widget.blog</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.login']]">
		<PackageFileEntry>
			<packageName>perc.widget.login</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
		<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.registration']]">
		<PackageFileEntry>
			<packageName>perc.widget.registration</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
		<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.rss']]">
		<PackageFileEntry>
			<packageName>perc.widget.rss</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
		<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.shareThis']]">
		<PackageFileEntry>
			<packageName>perc.widget.shareThis</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
		<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.poll']]">
		<PackageFileEntry>
			<packageName>perc.widget.poll</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
		<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.secureLogin']]">
		<PackageFileEntry>
			<packageName>perc.widget.secureLogin</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
		<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.gadget.blogs']]">
		<PackageFileEntry>
			<packageName>perc.gadget.blogs</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
		<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.gadget.welcome']]">
		<PackageFileEntry>
			<packageName>perc.gadget.welcome</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
		<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.gadget.globalVariables']]">
		<PackageFileEntry>
			<packageName>perc.gadget.globalVariables</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.gadget.processMonitor']]">
		<PackageFileEntry>
			<packageName>perc.gadget.processMonitor</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
		<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.evergageBeacon']]">
		<PackageFileEntry>
			<packageName>perc.evergageBeacon</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widgets.image']]">
		<PackageFileEntry>
			<packageName>perc.widgets.image</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.calendar']]">
		<PackageFileEntry>
			<packageName>perc.widget.calendar</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
    </xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.PageAutoListWidget']]">
		<PackageFileEntry>
			<packageName>perc.PageAutoListWidget</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
	</xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.gadget.bulkFileUpload']]">
		<PackageFileEntry>
			<packageName>perc.gadget.bulkFileUpload</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
	</xsl:template>
	<xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.jquery']]">
		<PackageFileEntry>
			<packageName>perc.widget.jquery</packageName>
			<status>REVERT</status>
		</PackageFileEntry>
	</xsl:template>
    <xsl:template match="PackageFileList/PackageFileEntry[packageName[text()='perc.widget.jqueryUI']]">
        <PackageFileEntry>
            <packageName>perc.widget.jqueryUI</packageName>
            <status>REVERT</status>
        </PackageFileEntry>
    </xsl:template>
</xsl:stylesheet>