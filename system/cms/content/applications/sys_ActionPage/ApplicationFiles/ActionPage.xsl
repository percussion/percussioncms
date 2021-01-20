<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:output method="html" omit-xml-declaration="yes"/>
	<xsl:include href="file:sys_ActionPage/sys_actionpage.xsl"/>
	<xsl:include href="file:sys_ActionPage/rxx_actionpage.xsl"/>
	<xsl:include href="file:sys_ActionPage/sys_wfPageActions.xsl"/>
	<xsl:include href="file:sys_ActionPage/sys_cmsPageActions.xsl"/>
	<xsl:variable name="sessionid" select="//@sessionid"/>
	<xsl:variable name="contentid" select="//@contentid"/>
	<xsl:variable name="siteid" select="//@siteid"/>
	<xsl:variable name="folderid" select="//@folderid"/>
	<xsl:variable name="assignmentType" select="//@assignmenttype"/>
	<!-- Note that the session id is already on the action url this SS receives -->
	<xsl:variable name="actionlist">
		<xsl:value-of select="//@actionlisturl"/>&amp;sys_contentid=<xsl:value-of select="$contentid"/>&amp;sys_assignmenttype=<xsl:value-of select="$assignmentType"/>
	</xsl:variable>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:variable name="itemactions" select="document($actionlist)/*"/>
	<xsl:variable name="currentrevision" select="//@currentrevision"/>
	<xsl:variable name="tiprevision" select="//@tiprevision"/>
	<xsl:variable name="link" select="//@link"/>
	<xsl:variable name="rxroottemp" select="//@rxroot"/>
	<xsl:variable name="rxroot" select="substring($rxroottemp,0,string-length($rxroottemp) - 1)"/>
	<xsl:variable name="cxlink">
		<xsl:value-of select="$rxroot"/>/sys_cx/mainpage.html?sys_componentname=ca_search&amp;sys_pagename=ca_search&amp;sys_sortparam=title&amp;sys_contentid=<xsl:value-of select="$contentid"/>
	</xsl:variable>
	<xsl:variable name="revid">
		<xsl:choose>
			<xsl:when test="string(translate(//@checkedoutby, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'))=string(translate(//@username, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'))">
				<xsl:value-of select="$tiprevision"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$currentrevision"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<!-- Indent a category -->
	<xsl:template name="subtable">
		<xsl:param name="content"/>
		<xsl:param name="label"/>
		<xsl:param name="icon"/>
		<xsl:param name="level"/>
		<xsl:if test="string-length($label) &gt; 0">
			<xsl:choose>
				<xsl:when test="string-length($icon) &gt; 0">
					<img width="26" height="19" border="0" align="absmiddle" src="../sys_ActionPage/{$icon}" alt="{$label}"/>
				</xsl:when>
				<xsl:otherwise>
					<img width="26" height="19" border="0" align="absmiddle" src="../sys_ActionPage/Blank.gif" alt="{$label}"/>
				</xsl:otherwise>
			</xsl:choose>
			<span class="parent">
				<xsl:value-of select="$label"/>
			</span>
			<br/>
		</xsl:if>
		<div id="child">
			<xsl:copy-of select="$content"/>
		</div>
	</xsl:template>
	<!-- create an item -->
	<xsl:template name="item">
		<xsl:param name="action"/>
		<xsl:param name="label"/>
		<xsl:param name="icon"/>
		<xsl:param name="level"/>
		<a onmouseover="doroll(this)" onmouseout="dorollout(this)" style="cursor:pointer;cursor:hand" onclick="{$action}">
			<xsl:choose>
				<xsl:when test="string-length($icon) &gt; 0">
					<img width="26" height="19" border="0" align="absmiddle" src="../sys_ActionPage/{$icon}" alt="{$label}"/>
				</xsl:when>
				<xsl:otherwise>
					<img width="26" height="19" border="0" align="absmiddle" src="../sys_ActionPage/Blank.gif" alt="{$label}"/>
				</xsl:otherwise>
			</xsl:choose>
			<span>
				<xsl:choose>
					<xsl:when test="$level &lt; 1">
						<xsl:attribute name="class">parent</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="class">child</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:value-of select="$label"/>
			</span>
		</a>
		<br/>
	</xsl:template>
	<xsl:template match="/">
		<!-- Start Content Meta Data -->
		<table id="TitleBox">
			<tr>
				<td>
					<table>
						<tr>
							<td id="Title" class="MetaSubTitle">
								<span class="MetaTitle">
									<xsl:call-template name="getLocaleString">
										<xsl:with-param name="key" select="'psx.sys_ActionPage.ActionPage@Content Title'"/>
										<xsl:with-param name="lang" select="$lang"/>
									</xsl:call-template>:</span>
							</td>
							<td id="RxTitle" class="bodyblack">
								<xsl:value-of select="//@title"/>
							</td>
						</tr>
						<tr>
							<td id="Title" class="MetaSubTitle">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.sys_ActionPage.ActionPage@Status'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>:</td>
							<td id="RxTitle" class="bodyblacksmall">
								<xsl:value-of select="//@state"/>
							</td>
						</tr>
						<tr>
							<td id="Title" class="MetaSubTitle">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.sys_ActionPage.ActionPage@Creator'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>:</td>
							<td id="RxTitle" class="bodyblacksmall">
								<xsl:value-of select="//@createdby"/>
							</td>
						</tr>
						<tr>
							<td id="Title" class="MetaSubTitle">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.sys_ActionPage.ActionPage@Last Modified    By'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>:</td>
							<td id="RxTitle" class="bodyblacksmall">
								<xsl:value-of select="//@lastmodifiedby"/>
							</td>
						</tr>
					</table>
				</td>
				<td class="bodyblacksmall"/>
			</tr>
		</table>
		<!-- End Content Meta Data -->
		<!-- Start Main Body Box -->
		<table id="BodyBox">
			<tr>
				<td>
					<table width="100%">
						<tr valign="top">
							<td>
								<!-- Start Left Column -->
								<xsl:apply-templates select="$itemactions" mode="mainmenu">
									<xsl:with-param name="actionsetid" select="$contentid"/>
									<xsl:with-param name="contentid" select="$contentid"/>
									<xsl:with-param name="sessionid" select="$sessionid"/>
									<xsl:with-param name="siteid" select="$siteid"/>
									<xsl:with-param name="folderid" select="$folderid"/>
									<xsl:with-param name="revision" select="$revid"/>
									<xsl:with-param name="tiprevision" select="$tiprevision"/>
									<xsl:with-param name="portal">yes</xsl:with-param>
									<xsl:with-param name="contentvalid" select="//@contentvalid"/>
									<xsl:with-param name="assignmenttype" select="$assignmentType"/>
									<xsl:with-param name="rhythmyxRoot" select="$rxroot"/>
									<xsl:with-param name="level">0</xsl:with-param>
								</xsl:apply-templates>
								<xsl:if test="$assignmentType != 'Default' and $assignmentType != '1'">
									<xsl:call-template name="item">
										<xsl:with-param name="label">
											<xsl:call-template name="getLocaleString">
												<xsl:with-param name="key" select="'psx.sys_ActionPage.ActionPage@View in Content       Explorer'"/>
												<xsl:with-param name="lang" select="$lang"/>
											</xsl:call-template>
										</xsl:with-param>
										<xsl:with-param name="icon">Content_Explorer.gif</xsl:with-param>
										<xsl:with-param name="level">0</xsl:with-param>
										<xsl:with-param name="action">window.open('<xsl:value-of select="$cxlink"/>')</xsl:with-param>
									</xsl:call-template>
								</xsl:if>
							</td>
							<td>
								<!-- Start Right Column -->
								<xsl:apply-templates select="$itemactions" mode="rightmenu">
									<xsl:with-param name="actionsetid" select="$contentid"/>
									<xsl:with-param name="contentid" select="$contentid"/>
									<xsl:with-param name="sessionid" select="$sessionid"/>
									<xsl:with-param name="siteid" select="$siteid"/>
									<xsl:with-param name="folderid" select="$folderid"/>
									<xsl:with-param name="revision" select="$revid"/>
									<xsl:with-param name="tiprevision" select="$tiprevision"/>
									<xsl:with-param name="portal">yes</xsl:with-param>
									<xsl:with-param name="contentvalid" select="//@contentvalid"/>
									<xsl:with-param name="assignmenttype" select="$assignmentType"/>
									<xsl:with-param name="rhythmyxRoot" select="$rxroot"/>
									<xsl:with-param name="level">0</xsl:with-param>
								</xsl:apply-templates>
							</td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_ActionPage.ActionPage@Rhythmyx Action Panel for">Used part of the action panel page title. This will be appended by the title of the content item.</key>
		<key name="psx.sys_ActionPage.ActionPage@Rhythmyx Action Panel">Rhythmyx action panel image alt text.</key>
		<key name="psx.sys_ActionPage.ActionPage@View in Content Explorer">This is the label for a link to the content explorer that will bring up the item</key>
		<key name="psx.sys_ActionPage.ActionPage@Status">A label for the current workflow state  of the content item</key>
		<key name="psx.sys_ActionPage.ActionPage@Creator">A label for the person who created the content item</key>
		<key name="psx.sys_ActionPage.ActionPage@Last Modified By">A label for the person who last modified the content item</key>
		<key name="psx.sys_ActionPage.ActionPage@Content Title">A label for the title of the content item</key>
		<key name="psx.sys_ActionPage.ActionPage@Close Window">A label that indicates the button closes the window</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
