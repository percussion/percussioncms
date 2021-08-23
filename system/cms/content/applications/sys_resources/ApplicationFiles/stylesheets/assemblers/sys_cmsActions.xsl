<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<xsl:template match="Action[@name='Item_ViewDependents']" mode="popmenu">
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		new PSMenuItem("<xsl:value-of select='@displayname'/>", "PSEditSimpleItem('<xsl:value-of select="@url"/>?sys_contentid=<xsl:value-of select="$contentid"/>&amp;sys_revision=<xsl:value-of select="$revision"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
	<!-- Preview  and 	Active Assembly -->
	<xsl:template match="Action[@name='Item_Preview' or @name='Item_ActiveAssembly']" mode="popmenu">
		<xsl:param name="contentid"/>
		<xsl:param name="sessionid"/>
		<xsl:param name="revision"/>
		<xsl:param name="assignmenttype"/>
		<xsl:if test="(@name='Item_ActiveAssembly' and $assignmenttype > 2) or (@name='Item_Preview')">
		<xsl:variable name="jsfunction">
			<xsl:choose>
				<xsl:when test="@name='Item_Preview'">PSPreviewItem</xsl:when>
				<xsl:otherwise>PSActiveAssemblyItem</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		new PSMenu("<xsl:value-of select='@displayname'/>",150,new Array(
		<xsl:apply-templates select="document(concat(substring-before(//@wfurlint, 'Rhythmyx'), 'Rhythmyx/sys_rcSupport/variantlist.xml?sys_contentid=', $contentid, '&amp;pssessionid=',$sessionid))/*" mode="list">
		<xsl:with-param name="urlParams" select="document(concat(substring-before(//@wfurlint, 'Rhythmyx'), 'Rhythmyx/sys_cxSupport/Params.xml?sys_actionid=', @actionid, '&amp;pssessionid=',$sessionid))//Params"/>
			<xsl:with-param name="contentid" select="$contentid"/>
			<xsl:with-param name="revision" select="$revision"/>
			<xsl:with-param name="jsfunction" select="$jsfunction"/>
			<xsl:with-param name="sessionid" select="$sessionid"/>
		</xsl:apply-templates>
		))<xsl:if test="position() != last()">,</xsl:if>
		</xsl:if>
	</xsl:template>
	<xsl:template match="*" mode="list">
		<xsl:param name="urlParams"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="jsfunction"/>
		<xsl:param name="sessionid"/>
		<xsl:if test="Variant/@variantId != ''">
			<xsl:apply-templates select="Variant" mode="variant-copy">
				<xsl:with-param name="contentid" select="$contentid"/>
				<xsl:with-param name="revision" select="$revision"/>
				<xsl:with-param name="jsfunction" select="$jsfunction"/>
				<xsl:with-param name="sessionid" select="$sessionid"/>
				<xsl:with-param name="urlParams" select="$urlParams"/>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>
	<xsl:template match="*" mode="variant-copy">
		<xsl:param name="urlParams"/>
		<xsl:param name="contentid"/>
		<xsl:param name="revision"/>
		<xsl:param name="jsfunction"/>
		<xsl:param name="sessionid"/>
		<xsl:param name="siteid" select="$urlParams/Param[@name='sys_siteid']"/>
	 		new PSMenuItem("<xsl:value-of select='DisplayName'/>", "<xsl:value-of select="$jsfunction"/>('<xsl:value-of select="AssemblyUrl"/>','<xsl:value-of select="$contentid"/>','<xsl:value-of select="@variantId"/>','<xsl:value-of select="$revision"/>','<xsl:value-of select="$siteid"/>','<xsl:value-of select="$sessionid"/>')")<xsl:if test="position() != last()">,</xsl:if>
	</xsl:template>
</xsl:stylesheet>
