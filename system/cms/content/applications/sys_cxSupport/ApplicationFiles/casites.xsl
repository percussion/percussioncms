<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/">
		<xsl:apply-templates select="/*" mode="copy"/>
	</xsl:template>
	<xsl:template match="*" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="*" mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="ActionList" mode="copy" priority="10">
		<Action>
			<xsl:attribute name="name">ROOT</xsl:attribute>
			<xsl:attribute name="label">ROOT</xsl:attribute>
			<xsl:attribute name="type">MENU</xsl:attribute>
			<xsl:copy-of select="@*[not(name()='assignmenttypeurl')]"/>
			<xsl:apply-templates select="*" mode="copy"/>
		</Action>
	</xsl:template>
	<xsl:template match="Action" mode="copy" priority="10">
		<xsl:variable name="vsurl">
			<xsl:value-of select="document(concat(@vsurl,'&amp;',substring-after(@url,'?')))//@current"/>
		</xsl:variable>
		<xsl:variable name="assignmenttype">
			<xsl:value-of select="document(concat(//@assignmenttypeurl,'&amp;',substring-after(@url,'?')))//@sys_assignmenttype"/>
		</xsl:variable>
			<xsl:if test="string-length($vsurl)!=0 and $assignmenttype &gt; 1">
			<xsl:copy>
				<xsl:copy-of select="@*"/>
				<xsl:attribute name="url"><xsl:value-of select="$vsurl"/><xsl:if test="not(contains($vsurl,'sys_command=editrc'))"><xsl:text>&amp;sys_command=editrc</xsl:text></xsl:if><xsl:if test="not(contains($vsurl,'parentPage=yes'))"><xsl:text>&amp;parentPage=yes</xsl:text></xsl:if></xsl:attribute>
			</xsl:copy>
			</xsl:if>
	</xsl:template>
</xsl:stylesheet>
