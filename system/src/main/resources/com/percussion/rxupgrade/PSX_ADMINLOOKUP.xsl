<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!-- main template -->
	<xsl:template match="/">
		<xsl:apply-templates select="." mode="copy"/>
	</xsl:template>
	<!-- copy any attribute or template -->
	<xsl:template match="@*|*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="row" mode="sortandgetmax">
		<xsl:if test="position()=last()">
			<xsl:value-of select="column[@name='ID']"/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="row" mode="findexists">
		<xsl:param name="type"/>
		<xsl:param name="cat"/>
		<xsl:param name="val"/>
		<xsl:choose>
			<xsl:when test="val=''">
				<xsl:if test="(column[@name='TYPE']=$type and column[@name='CATEGORY']=$cat)">
					<xsl:value-of select="'found'"/>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="(column[@name='TYPE']=$type and column[@name='CATEGORY']=$cat and column[@name='NAME']=$val)">
					<xsl:value-of select="'found'"/>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="table" mode="copy">
		<xsl:variable name="maxid">
			<xsl:apply-templates select="row" mode="sortandgetmax">
				<xsl:sort select="column[@name='ID']"/>
			</xsl:apply-templates>
		</xsl:variable>
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:attribute name="onCreateOnly">no</xsl:attribute>
			<xsl:apply-templates mode="copy"/>
			<xsl:variable name="var1">
				<xsl:apply-templates select="row" mode="findexists">
					<xsl:with-param name="type" select="'role'"/>
					<xsl:with-param name="cat" select="'SYS_ATTRIBUTENAME'"/>
					<xsl:with-param name="val" select="'sys_defaultCommunity'"/>
				</xsl:apply-templates>
			</xsl:variable>
			<xsl:if test="$var1!='found'">
				<row>
					<column name="ID"><xsl:value-of select="($maxid + 1)"/></column>
					<column name="TYPE">role</column>
					<column name="CATEGORY">SYS_ATTRIBUTENAME</column>
					<column name="NAME">sys_defaultCommunity</column>
					<column name="LIMITTOLIST">Y</column>
					<column name="CATALOGURL">sys_commSupport/sysdefaultcommunity.xml</column>
				</row>
			</xsl:if>
			<xsl:variable name="var2">
				<xsl:apply-templates select="row" mode="findexists">
					<xsl:with-param name="type" select="'role'"/>
					<xsl:with-param name="cat" select="'SYS_ATTRIBUTENAME'"/>
					<xsl:with-param name="val" select="'sys_defaultHomepageURL'"/>
				</xsl:apply-templates>
			</xsl:variable>
			<xsl:if test="$var2!='found'">
				<row>
					<column name="ID"><xsl:value-of select="($maxid + 2)"/></column>
					<column name="TYPE">role</column>
					<column name="CATEGORY">SYS_ATTRIBUTENAME</column>
					<column name="NAME">sys_defaultHomepageURL</column>
					<column name="LIMITTOLIST"/>
					<column name="CATALOGURL"/>
				</row>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
