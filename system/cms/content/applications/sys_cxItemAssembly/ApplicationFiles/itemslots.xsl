<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n"
                extension-element-prefixes="psxi18n">
	<xsl:template match="/">
		<Node name="ItemSlots" type="ROOT" label="Item Slots" expanded="true" childrenurl="">
			<xsl:for-each select="itemslots/slot[slotid!='']">
				<Node name="Slot-{position()}" type="Slot" label="{slotname}" childrenurl="{concat(//slotitemsurl,'?sys_contentid=', contentid, '&amp;sys_revision=', revision, '&amp;sys_slotid=', slotid)}" expanded="false">
					<TableMeta>
						<Column name="Variant" type="Text"/>
						<Column name="Content Type" type="Text"/>
					</TableMeta>
					<RowData>
						<ColumnData name="ID">
							<xsl:value-of select="slotid"/>
						</ColumnData>
					</RowData>
					<Props>
						<Prop name="sys_contentid">
							<xsl:value-of select="contentid"/>
						</Prop>
						<Prop name="sys_revision">
							<xsl:value-of select="revision"/>
						</Prop>
						<Prop name="sys_slotid">
							<xsl:value-of select="slotid"/>
						</Prop>
						<Prop name="sys_slotname">
							<xsl:value-of select="slotname"/>
						</Prop>
					</Props>
				</Node>
			</xsl:for-each>
		</Node>
	</xsl:template>
</xsl:stylesheet>
