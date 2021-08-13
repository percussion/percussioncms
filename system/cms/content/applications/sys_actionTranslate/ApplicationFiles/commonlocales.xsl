<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
	<xsl:variable name="locales" select="document(//@localesurl)"/>
	<xsl:variable name="childids">
		<xsl:for-each select="//item">
			<xsl:text>&amp;sys_contentid=</xsl:text>
			<xsl:value-of select="@contentid"/>
		</xsl:for-each>
	</xsl:variable>
	<xsl:variable name="childlocalesurl" select="concat(//@childlocalesurl,$childids)"/>
	<xsl:variable name="childlocales" select="document($childlocalesurl)"/>
	<xsl:variable name="transitionedlocales">
		<xsl:for-each select="//item">
			<xsl:variable name="contentid" select="@contentid"/>
			<item contentid="{@contentid}">
				<locale name="{locale/@name}"/>
				<xsl:for-each select="$childlocales//item[@contentid=$contentid]/locale">
					<locale name="{@name}"/>
				</xsl:for-each>
			</item>
		</xsl:for-each>
	</xsl:variable>
	<xsl:variable name="untransitionedlocales">
		<xsl:for-each select="$transitionedlocales//item">
			<xsl:variable name="trlocales" select="locale"/>
			<item contentid="{@contentid}">
				<xsl:for-each select="$locales//PSXEntry[not(Value=$trlocales/@name)]">
					<locale displaytext="{PSXDisplayText}" name="{Value}"/>
				</xsl:for-each>
			</item>
		</xsl:for-each>
	</xsl:variable>
	<xsl:variable name="commonuntransitionedlocales">
		<xsl:variable name="itemcount" select="count($untransitionedlocales//item)"/>
		<xsl:for-each select="$untransitionedlocales//item[position()=1]/locale">
			<xsl:variable name="name" select="@name"/>
			<xsl:variable name="displaytext" select="@displaytext"/>
			<xsl:if test="count($untransitionedlocales//item[locale[@name=$name]])=$itemcount">
				<locale displaytext="{$displaytext}" name="{$name}"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:variable>
	<!-- main template -->
	<xsl:template match="/">
		<commonlocales>
			<xsl:apply-templates select="$commonuntransitionedlocales" mode="copy"/>
		</commonlocales>
	</xsl:template>
	<!-- copy any attribute or template -->
	<xsl:template match="@*|*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="ite" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
