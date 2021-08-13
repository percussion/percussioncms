<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
                xmlns:psxi18n="com.percussion.i18n.PSI18nUtils" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:template match="ActionLinkList" mode="actionlist">
		<xsl:param name="separator">
			<br id="action"/>
		</xsl:param>
		<xsl:param name="formref" select="'this.form'"/>
		<xsl:param name="isLast"/>
		<xsl:param name="isFirst"/>
		<xsl:comment>ActionList</xsl:comment>
		<xsl:apply-templates select="ActionLink" mode="actionlist">
			<xsl:with-param name="separator" select="$separator"/>
			<xsl:with-param name="formref" select="$formref"/>
			<xsl:with-param name="isLast" select="$isLast"/>
			<xsl:with-param name="isFirst" select="$isFirst"/>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template name="ActionLinkTable" match="ActionLink" mode="actionlist-table">
		<xsl:param name="formref" select="'this.form'"/>
		<xsl:param name="isDisabled">false</xsl:param>
		<xsl:comment>ActionList Table <xsl:value-of select="$isDisabled"/>
		</xsl:comment>
		<xsl:if test="not (($isDisabled = 'yes') or (@isDisabled = 'yes'))">
			<tr>
				<xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2'"/></xsl:otherwise></xsl:choose></xsl:attribute>
				<td align="center">
					<xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1font'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2font'"/></xsl:otherwise></xsl:choose></xsl:attribute>
					<xsl:call-template name="ActionLink">
						<xsl:with-param name="formref" select="$formref"/>
						<xsl:with-param name="isDisabled" select="$isDisabled"/>
					</xsl:call-template>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
	<xsl:template name="ActionLink" match="ActionLink" mode="actionlist">
		<xsl:param name="isLast"/>
		<xsl:param name="isFirst"/>
		<xsl:param name="separator">
			<br id="action"/>
		</xsl:param>
		<xsl:param name="isDisabled">false</xsl:param>
		<xsl:param name="formref" select="'this.form'"/>
		<xsl:comment>ActionLink Disable=<xsl:value-of select="$isDisabled"/>
			<xsl:value-of select="@isDisabled"/>
		</xsl:comment>
		<xsl:variable name="container" select="name(../..)"/>
		<xsl:variable name="rowCount" select="count(../../RowData/Row)"/>
		<xsl:if test="not (($isDisabled ='yes') or (@isDisabled = 'yes'))">
			<xsl:if test="not(($isLast='yes' and DisplayLabel='Dn') or ($isFirst='yes'  and DisplayLabel='Up'))">
            <xsl:variable name="syspageid" select="/*/ActionLinkList/ActionLink/Param[@name='sys_pageid']"/>
			<input type="submit" value="{DisplayLabel}" class="nav_body" accesskey="{DisplayLabel/@accessKey}">
				<xsl:choose>
					<xsl:when test="@imageHref">
						<xsl:attribute name="type">image</xsl:attribute>
						<xsl:attribute name="src"><xsl:value-of select="@imageHref"/></xsl:attribute>
						<xsl:attribute name="alt"><xsl:call-template name="childtableactionimagealt"><xsl:with-param name="alttext" select="DisplayLabel"/></xsl:call-template></xsl:attribute>
						<xsl:attribute name="title"><xsl:call-template name="childtableactionimagealt"><xsl:with-param name="alttext" select="DisplayLabel"/></xsl:call-template></xsl:attribute>
						<xsl:attribute name="onclick">modifyFormParams(<xsl:value-of select="$formref"/><xsl:apply-templates select="Param" mode="actionlist"/>)</xsl:attribute>
					</xsl:when>
                    <xsl:when test="$container = 'Table' and $rowCount = 0 and $syspageid and not($syspageid='0')">
						<xsl:attribute name="type">button</xsl:attribute>
						<xsl:attribute name="name">addNewButton</xsl:attribute>
						<xsl:variable name="pageid" select="./Param[@name='sys_pageid']"/>
						<xsl:if test="$pageid">
							<xsl:attribute name="onclick">addNewChildItem(<xsl:value-of select="$pageid"/>);</xsl:attribute>
						</xsl:if>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="pageid" select="./Param[@name='sys_pageid']"/>
						<xsl:attribute name="type">submit</xsl:attribute>
						<xsl:attribute name="name">submitButton</xsl:attribute>
						<xsl:if test="$container != 'Table'">
						<xsl:attribute name="id">rxCESubmit</xsl:attribute>
						</xsl:if>
						<xsl:if test="$pageid">
							<xsl:attribute name="onclick">modifyPageId(<xsl:value-of select="$formref"/>, <xsl:value-of select="$pageid"/>);</xsl:attribute>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:if test="$isDisabled ='yes' or @isDisabled = 'yes'">
					<xsl:attribute name="Disabled">Disabled</xsl:attribute>
				</xsl:if>
			</input>
			<xsl:if test="position() != last()">
				<xsl:copy-of select="$separator"/>
				</xsl:if>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	<xsl:template match="ActionLink" mode="addformparams">
		<xsl:apply-templates select="Param" mode="HiddenParams"/>
	</xsl:template>
	<xsl:template match="Param" mode="actionlist">,&quot;<xsl:value-of select="@name"/>&quot;,&quot;<xsl:value-of select="."/>&quot;</xsl:template>
	<xsl:template match="Param" mode="HiddenParams">
		<input type="hidden" id="perc-content-edit-{@name}" name="{@name}" value="{.}"/>
	</xsl:template>
	<xsl:template name="childtableactionimagealt">
		<xsl:param name="alttext"/>
		<xsl:choose>
			<xsl:when test="$alttext='Del'">
				<xsl:call-template name="getLocaleString">
					<xsl:with-param name="key" select="'psx.contenteditor.ActionList.alt@Delete row'"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$alttext='Up'">
				<xsl:call-template name="getLocaleString">
					<xsl:with-param name="key" select="'psx.contenteditor.ActionList.alt@Move up'"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$alttext='Dn'">
				<xsl:call-template name="getLocaleString">
					<xsl:with-param name="key" select="'psx.contenteditor.ActionList.alt@Move down'"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$alttext='Edit'">
				<xsl:call-template name="getLocaleString">
					<xsl:with-param name="key" select="'psx.contenteditor.ActionList.alt@Edit row'"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$alttext"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.contenteditor.ActionList.alt@Delete row">Alt text displayed for Delete Row image.</key>
		<key name="psx.contenteditor.ActionList.alt@Move up">Alt text displayed for Move up image.</key>
		<key name="psx.contenteditor.ActionList.alt@Move down">Alt text displayed for Move down image.l</key>
		<key name="psx.contenteditor.ActionList.alt@Edit row">Alt text displayed for Edit Row image.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
