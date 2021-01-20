<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:urlencoder="java.net.URLEncoder" exclude-result-prefixes="urlencoder" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<!-- main template -->
	<xsl:output method="xml"/>
	<xsl:variable name="chkoutuser" select="translate(//BasicInfo/@CheckOutUserName, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
	<xsl:variable name="loginuser" select="translate(//BasicInfo/UserName, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
	<xsl:variable name="isAssignee" select="//BasicInfo/UserName/@assignmentType > 2"/>
	<xsl:variable name="isAdmin" select="//BasicInfo/UserName/@assignmentType = 4"/>
	<xsl:variable name="mode" select="/workflowactions/@sys_mode"/>
	<xsl:template match="/">
		<ActionList>
			<xsl:apply-templates select="*//ActionLinkList" mode="copy"/>
		</ActionList>
	</xsl:template>
	<!-- take away the Label in the UI set -->
	<xsl:template match="ActionLinkList" mode="copy">
		<xsl:apply-templates select="*" mode="copy"/>
	</xsl:template>
	<!-- copy any attribute or template -->
	<xsl:template match="@*|*" mode="copy"/>
	<xsl:template match="*|@*" mode="copyParams">
		<xsl:if test="@name!='sys_transitionid'">
			<xsl:copy-of select="."/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="ActionLink[@isTransition='yes']" mode="copy">
		<xsl:if test="$isAssignee and ($chkoutuser = '' or $chkoutuser = $loginuser or $isAdmin)">
			<xsl:apply-templates select="." mode="actioncopy"/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="ActionLink[@name='checkin'] | ActionLink[@name='forcecheckin']" mode="copy">
		<xsl:if test="$isAssignee and (($chkoutuser = $loginuser) or $isAdmin) and not($mode='IANAV')">
			<xsl:apply-templates select="." mode="actioncopy"/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="ActionLink[@name='checkout']" mode="copy">
		<xsl:if test="$isAssignee">
			<xsl:apply-templates select="." mode="actioncopy"/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="*" mode="actioncopy">
		<Action>
			<xsl:attribute name="actionid"><xsl:value-of select="'-1'"/></xsl:attribute>
			<xsl:attribute name="name"><xsl:choose><xsl:when test="@isTransition='yes'"><xsl:value-of select="DisplayLabel"/></xsl:when><xsl:otherwise><xsl:value-of select="@name"/></xsl:otherwise></xsl:choose></xsl:attribute>
			<xsl:attribute name="label"><xsl:value-of select="DisplayLabel"/></xsl:attribute>
			<xsl:attribute name="type">MENUITEM</xsl:attribute>
			<xsl:attribute name="handler">SERVER</xsl:attribute>
			<xsl:choose>
				<xsl:when test="(@name='checkin') or (@name='forcecheckin')">
					<xsl:attribute name="url"><xsl:value-of select="/*/@checkinurl"/></xsl:attribute>
				</xsl:when>
				<xsl:when test="@name='checkout'">
					<xsl:attribute name="url"><xsl:value-of select="/*/@checkouturl"/></xsl:attribute>
				</xsl:when>
				<xsl:when test="AssignedRoles/Role[@adhocType != '0']">
					<xsl:attribute name="url"><xsl:value-of select="/*/@transitionurl"/></xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="url"><xsl:value-of select="/*/@workflowactionurl"/></xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<Props>
				<xsl:choose>
					<xsl:when test="AssignedRoles/Role[@adhocType != '0']">
						<Prop name="batchProcessing">no</Prop>
						<Prop name="launchesWindow">yes</Prop>
						<Prop name="target">workflowtransition</Prop>
						<Prop name="targetStyle">toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=0,resizable=1,width=260,height=345</Prop>
					</xsl:when>
					<xsl:otherwise>
						<Prop name="batchProcessing">yes</Prop>
						<Prop name="SupportsMultiSelect">yes</Prop>
						<Prop name="launchesWindow">no</Prop>
						<xsl:if test="@commentRequired">
							<Prop name="commentRequired">
								<xsl:value-of select="@commentRequired"/>
							</Prop>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
			</Props>
			<Params>
				<xsl:apply-templates select="Param" mode="copyParams"/>
				<Param>
					<xsl:attribute name="name">sys_contentid</xsl:attribute>
					<xsl:value-of select="'$sys_contentid'"/>
				</Param>
				<Param>
					<xsl:attribute name="name">sys_revision</xsl:attribute>
					<xsl:value-of select="'$sys_revision'"/>
				</Param>
				<Param name="psredirect">../sys_cxSupport/blank.html</Param>
				<xsl:if test="@commentRequired">
					<Param name="commentRequired">
						<xsl:value-of select="@commentRequired"/>
					</Param>
				</xsl:if>
				<xsl:if test="AssignedRoles/Role[@adhocType != '0'] and @isTransition='yes'">
					<Param name="showAdhoc">yes</Param>
				</xsl:if>
				<Param name="transitionName">
					<xsl:value-of select="DisplayLabel"/>
				</Param>
				<Param name="sys_transitionid">
					<xsl:value-of select="Param[@name='sys_transitionid']"/>
				</Param>
			</Params>
		</Action>
	</xsl:template>
	<xsl:template match="*" mode="buildlist">
		<xsl:value-of select="."/>
		<xsl:text>:</xsl:text>
		<xsl:value-of select="@adhocType"/>
		<xsl:text>;</xsl:text>
	</xsl:template>
</xsl:stylesheet>
