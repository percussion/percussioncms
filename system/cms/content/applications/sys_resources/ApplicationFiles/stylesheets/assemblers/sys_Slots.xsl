<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n"
                xmlns:psxi18n="urn:www.percussion.com/i18n">


	<xsl:template match="/">
		<xsl:apply-templates select="*" mode="rxslot"/>
	</xsl:template>
	<!-- Simple Table Slot -->
	<!-- this template handles an empty slot -->
	<xsl:template match="rxslot[@template='SimpleTableSlot']" mode="rxslot">
		<xsl:comment>Empty Simple Table Slot</xsl:comment>
	</xsl:template>
	<!-- Simple Table Slot Template -->
	<xsl:template match="rxslot[@template='SimpleTableSlot' and linkurl]" priority="10" mode="rxslot">
		<xsl:comment>Simple Table Slot Template</xsl:comment>
		<table>
			<xsl:for-each select="linkurl">
				<tr>
					<td>
						<xsl:if test="not(Value/@current = '')">
							<xsl:copy-of select="document(Value/@current)/*/body/*"/>
						</xsl:if>
						<xsl:if test="Value/@current = ''">&#160;</xsl:if>
					</td>
				</tr>
			</xsl:for-each>
		</table>
		<xsl:comment>End of Simple Table Slot Template</xsl:comment>
	</xsl:template>
   <!-- the following templates are used for fastforward only -->
	<!--rxs_navTemplate -->
	<!-- a simple template for use with Managed Nav 
  This temple copies the snippets with no delimiters or tables -->
	<xsl:template match="rxslot[@template='rxs_navTemplate' and linkurl]" mode="rxslot">
		<xsl:apply-templates select="linkurl" mode="rxs_nav_Inner"/>
	</xsl:template>
	<xsl:template match="linkurl" mode="rxs_nav_Inner">
		<xsl:copy-of select="document(Value/@current)/*/body/*"/>
	</xsl:template>
	<!-- empty nav slot -->
	<xsl:template match="rxslot[@template='rxs_navTemplate' and not (linkurl) ]" mode="rxslot"/>
<!-- 	MutliColumnSlot -->
	<!-- This template handles an empty slot -->
	<xsl:template match="rxslot[@template='MultiColumnSlot']" mode="rxslot">
		<xsl:comment>Empty MultiColumnSlot</xsl:comment>
	</xsl:template>

	<!-- MultiColumnSlot Template -->
	<xsl:template match="rxslot[@template='MultiColumnSlot' and linkurl]" priority="10" mode="rxslot">
		<xsl:param name="COLS"/>
		<xsl:comment>Start MultiColumnSlot Template</xsl:comment>
		<table>
			<xsl:if test="$COLS=1">
				<!-- This will handle the case of a 1-column table -->
				<xsl:for-each select="linkurl">
					<tr valign="top">
						<td class="lc_column">
							<xsl:if test="not(Value/@current = '')">
								<xsl:copy-of select="document(Value/@current)/*/body/*"/>
							</xsl:if>
							<xsl:if test="Value/@current = ''">&#160;</xsl:if>
						</td>
					</tr>
				</xsl:for-each>
			</xsl:if>
			<!-- This will handle the case of an N-column table, where N > 1 -->
			<xsl:apply-templates select="linkurl[position() mod $COLS = 1]" mode="newrow">
				<xsl:with-param name="COLS" select="$COLS"/>
			</xsl:apply-templates>
		</table>
		<xsl:comment>End MultiColumnSlot Template</xsl:comment>
	</xsl:template>

	<!-- The template below will only match on the linkurl that should fall in the first column for each row. -->
	<xsl:template match="linkurl" mode="newrow">
		<xsl:param name="COLS"/>
		<tr valign="top">
			<td class="lc_column">
				<xsl:copy-of select="document(Value/@current)/*/body/*"/>
			</td>
			<xsl:apply-templates select="following-sibling::linkurl" mode="ncolumns">
				<xsl:with-param name="COLS" select="$COLS"/>
			</xsl:apply-templates>
		</tr>
	</xsl:template>

	<!-- The template below will match on the linkurls that come after the first column.  The conditional determines which linkurl should fall in the last column for each row. -->
	<xsl:template match="linkurl" mode="ncolumns">
		<xsl:param name="COLS"/>
		<xsl:if test="position() &lt; $COLS">
			<td class="lc_column">
				<xsl:copy-of select="document(Value/@current)/*/body/*"/>
			</td>
		</xsl:if>
	</xsl:template>
	<!-- We only get to this template when a slot name does not match or is not provided. -->
	<xsl:template match="rxslot" mode="rxslot">
		<xsl:comment>DEFAULT SLOT TEMPLATE</xsl:comment>
		<xsl:if test="linkurl">
			<table border="1">
				<xsl:for-each select="linkurl">      
					<tr>
						<td>
							<xsl:copy-of select="document(Value/@current)/*/body/*"/>
						</td>
					</tr>
				</xsl:for-each>
			</table>
		</xsl:if>
	</xsl:template>

	<!-- This one handles any stray slots without names. This should never happen. -->
	<xsl:template match="*" mode="rxslot">
		<xsl:message>Unknown Slot found &#10; 
			<xsl:copy-of select="."/>
		</xsl:message>
		<xsl:comment>UNKNOWN SLOT</xsl:comment>
	</xsl:template>
</xsl:stylesheet>
