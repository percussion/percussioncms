<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
                xmlns:psxi18n="com.percussion.i18n.PSI18nUtils" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:variable name="firstcontenttypeid">
		<xsl:value-of select="//ActionList/ContentTypeIdList/sys_contenttypeid[position()=1]"/>
	</xsl:variable>
	<xsl:variable name="multiplecontenttypes">
		<xsl:choose>
			<xsl:when test="count(//ActionList/ContentTypeIdList) != count(//ActionList/ContentTypeIdList[sys_contenttypeid=$firstcontenttypeid])">yes</xsl:when>
			<xsl:otherwise>no</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:template match="/">
		<ActionList>
			<xsl:if test="/ActionList/Action/@name != 'Change_Variant' and $multiplecontenttypes='yes'">
				<Action name="{/ActionList/Action/@name}" label="Current Variant" url="" type="MENUITEM" handler="CLIENT">
					<xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxSupport.filtervariantlist@Current Variant'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
					<Props>
						<Prop name="sys_slotid">
							<xsl:value-of select="/ActionList/Action/Props/sys_slotid"/>
						</Prop>
					</Props>
				</Action>
			</xsl:if>
			<xsl:if test="/ActionList/Action/@name = 'Change_Variant' or $multiplecontenttypes='no'">
				<xsl:apply-templates select="/ActionList/*" mode="copy"/>
			</xsl:if>
		</ActionList>
	</xsl:template>
	<xsl:template match="*" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="*" mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="Props" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:if test="sys_variantid=/ActionList/@sys_variantid">
				<Prop name="menuItemChecked">true</Prop>
			</xsl:if>
			<xsl:for-each select="*">
				<Prop>
					<xsl:attribute name="propid">0</xsl:attribute>
					<xsl:attribute name="name"><xsl:value-of select="name()"/></xsl:attribute>
					<xsl:value-of select="."/>
				</Prop>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="Params" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:for-each select="*">
				<Param>
					<xsl:attribute name="paramid">0</xsl:attribute>
					<xsl:attribute name="name"><xsl:value-of select="name()"/></xsl:attribute>
					<xsl:value-of select="."/>
				</Param>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_cxSupport.filtervariantlist@Current Variant">Variant option menu to choose default variant of each item</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
