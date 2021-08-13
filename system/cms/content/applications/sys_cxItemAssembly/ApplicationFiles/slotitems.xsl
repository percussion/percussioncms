<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" exclude-result-prefixes="psxi18n"
                extension-element-prefixes="psxi18n">
	
	<xsl:template match="/">
		<Node name="SlotItems" type="ROOT" label="Slot Items" expanded="true" childrenurl="">
			<xsl:for-each select="slotitems/item[contentid!='']">
				<xsl:sort select="sortrank" order="ascending" data-type="number"/>
				<xsl:variable name="revision">
					<xsl:choose>
						<xsl:when test="checkoutstatus = 'Checked Out by Me'">
							<xsl:value-of select="tiprevision"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="revision"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<Node name="SlotItem-{position()}" type="SlotItem" label="{label}" iconkey="{@iconPath}" childrenurl="{concat(//itemslotsurl,'?sys_contentid=', contentid, '&amp;sys_revision=', $revision, '&amp;sys_slotid=',slotid, '&amp;sys_variantid=', variant/@id)}" expanded="false">
					<RowData>
						<ColumnData name="Variant">
							<xsl:value-of select="variant"/>
						</ColumnData>
						<ColumnData name="Content Type">
							<xsl:value-of select="contenttypename"/>
						</ColumnData>
					</RowData>
					<Props>
						<Prop name="sys_contentid">
							<xsl:value-of select="contentid"/>
						</Prop>
						<Prop name="sys_revision">
							<xsl:value-of select="$revision"/>
						</Prop>
						<Prop name="sys_slotid">
							<xsl:value-of select="relProps/prop[@name='sys_slotid']"/>
						</Prop>
						<Prop name="sys_variantid">
							<xsl:value-of select="relProps/prop[@name='sys_variantid']"/>
						</Prop>
						<Prop name="sys_sortrank">
							<xsl:value-of select="relProps/prop[@name='sys_sortrank']"/>
						</Prop>
						<Prop name="sys_siteid">
							<xsl:value-of select="relProps/prop[@name='sys_siteid']"/>
						</Prop>
						<Prop name="sys_folderid">
							<xsl:value-of select="relProps/prop[@name='sys_folderid']"/>
						</Prop>
						<Prop name="sys_contenttypeid">
							<xsl:value-of select="contenttypeid"/>
						</Prop>
						<Prop name="sys_relationshipid">
							<xsl:value-of select="relProps/@rid"/>
						</Prop>
						<Prop name="sys_communityid">
							<xsl:value-of select="communityid"/>
						</Prop>
						<Prop name="sys_checkoutstatus">
							<xsl:value-of select="checkoutstatus"/>
						</Prop>
						<Prop name="sys_contentcheckoutusername">
							<xsl:value-of select="contentcheckoutusername"/>
						</Prop>
						<Prop name="sys_workflowappid">
							<xsl:value-of select="workflowappid"/>
						</Prop>
						<Prop name="sys_publishabletype">
							<xsl:value-of select="publishabletype"/>
						</Prop>
					</Props>
					<TableMeta>
						<Column name="ID" type="Text"/>
					</TableMeta>
				</Node>
			</xsl:for-each>
		</Node>
	</xsl:template>
</xsl:stylesheet>
