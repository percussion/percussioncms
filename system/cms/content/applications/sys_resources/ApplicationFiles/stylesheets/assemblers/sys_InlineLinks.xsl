<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:html="http://www.w3.org/TR/REC-html40" xmlns:urldecoder="java.net.URLDecoder"
                exclude-result-prefixes="urldecoder"
>
	<!--Old template for anchor tags -->
	<xsl:template match="html:a[@sys_contentid] | a[@sys_contentid]" mode="rxbodyfield" priority="5">
		<!--Get the siteid and folderid from the url and pass it to the link generator-->
		<xsl:variable name="tempsiteid" select="substring-after(@href,'sys_siteid=')"/>
		<xsl:variable name="siteid">
				<xsl:choose>
					<xsl:when test="contains($tempsiteid,'&amp;')">
						<xsl:value-of select="substring-before($tempsiteid,'&amp;')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$tempsiteid"/>
					</xsl:otherwise>
				</xsl:choose>
		</xsl:variable>
		<xsl:variable name="tempfolderid" select="substring-after(@href,'sys_folderid=')"/>
		<xsl:variable name="folderid">
				<xsl:choose>
					<xsl:when test="contains($tempfolderid,'&amp;')">
						<xsl:value-of select="substring-before($tempfolderid,'&amp;')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$tempfolderid"/>
					</xsl:otherwise>
				</xsl:choose>
		</xsl:variable>
		<xsl:variable name="assemblyurl" select="document(concat(/*/sys_AssemblerInfo/InlineLink/@url,'&amp;sys_contentid=', @sys_contentid, '&amp;sys_variantid=', @sys_variantid, '&amp;sys_siteid=', $siteid,'&amp;sys_folderid=', $folderid))/*/@current"/>
		<xsl:variable name="anchorvalue" select="substring-after(@href,'#')"/>
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:attribute name="href"><xsl:choose><xsl:when test="$anchorvalue!=''"><xsl:value-of select="concat($assemblyurl,'#',$anchorvalue)"/></xsl:when><xsl:otherwise><xsl:value-of select="$assemblyurl"/></xsl:otherwise></xsl:choose></xsl:attribute>
			<xsl:apply-templates mode="rxbodyfield"/>
		</xsl:copy>
	</xsl:template>
	<!-- Old template for images -->
	<xsl:template match="html:img[@sys_contentid] | img[@sys_contentid]" mode="rxbodyfield">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:attribute name="src"><xsl:value-of select="document(concat(/*/sys_AssemblerInfo/InlineLink/@url,'&amp;sys_contentid=', @sys_contentid, '&amp;sys_variantid=', @sys_variantid))/*/@current"/></xsl:attribute>
			<xsl:apply-templates mode="rxbodyfield"/>
		</xsl:copy>
	</xsl:template>
	<!-- Template matches on inlinetype of variant and replaces the variant with the latest output -->
	<xsl:template match="*[@inlinetype='rxhyperlink'] | *[@inlinetype='rximage'] | *[@inlinetype='rxvariant'] | *[@inlinetype='hyperlink'] | *[@inlinetype='image']" mode="rxbodyfield" priority="10">
		<xsl:choose>
			<xsl:when test="not(@sys_dependentid=/*/sys_AssemblerInfo/RelatedContent/linkurl/@contentid)">
				<xsl:choose>
					<xsl:when test="@inlinetype='rxvariant'">
						<xsl:call-template name="displayinlinetext">
							<xsl:with-param name="inlinetext" select="@rxselectedtext"/>>
							<xsl:with-param name="urldecode" select="yes"/>>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="displayinlinetext">
							<xsl:with-param name="inlinetext" select="."/>>
							<xsl:with-param name="urldecode" select="no"/>>
					</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:apply-templates select="@*" mode="copy"/>
					<xsl:apply-templates mode="copy"/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="@*|*|comment()" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!-- This template suppresses all the attributes we add for inline links, images and variants -->
	<xsl:template match="@*[name()='inlinetype'  or name()='contenteditable' or name()='unselectable' or name()='rxinlineslot' or name()='sys_contentid' or name()='sys_dependentid' or name()='sys_dependentvariantid' or name()='sys_relationshipid' or name()='sys_variantid' or name()='sys_siteid' or name()='sys_folderid' or (name()='class' and .='rxbodyfield')]" mode="copy" priority="10"/>
	<!-- Template to display inline text when the link is no more valid. Clients can override this template in rx_InlineLinks.xsl file to add a error page link which shows a message like, The page you are trying to view is no longer exists on this server. -->
	<xsl:template name="displayinlinetext">
		<xsl:param name="inlinetext"/>
		<xsl:param name="urldecode"/>
		<xsl:choose>
			<xsl:when test="$urldecode='yes'">
				<xsl:value-of select="urldecoder:decode($inlinetext)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$inlinetext"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- This template "eats" the extra <div> tag that we have to add to the HTML -->
	<xsl:template match="div[@class=&apos;rxbodyfield&apos;]" mode="rxbodyfield">
		<xsl:apply-templates mode="rxbodyfield"/>
	</xsl:template>
	<xsl:template match="*|comment()" mode="rxbodyfield">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="rxbodyfield"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
