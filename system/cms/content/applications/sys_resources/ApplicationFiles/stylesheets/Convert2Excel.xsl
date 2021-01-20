<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns="urn:schemas-microsoft-com:office:spreadsheet" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet" xmlns:html="http://www.w3.org/TR/REC-html40">
	<xsl:template match="/">
		<Workbook>
			<Worksheet ss:Name="{name(*)}">
				<xsl:apply-templates select="/*" mode="table"/>
			</Worksheet>
		</Workbook>
	</xsl:template>
	<xsl:template match="*" mode="table">
		<xsl:variable name="firstRow" select="*[position()=1]"/>
		<xsl:variable name="rowcount" select="count(*)"/>
		<xsl:variable name="colcount" select="count($firstRow/*)"/>
		<Table ss:ExpandedColumnCount="{$colcount + 1}" ss:ExpandedRowCount="{$rowcount + 1}" x:FullColumns="1" x:FullRows="1">
			<Row>
				<xsl:for-each select="$firstRow/*">
					<Cell>
						<Data ss:Type="String">
							<xsl:value-of select="name()"/>
						</Data>
					</Cell>
				</xsl:for-each>
			</Row>
			<xsl:apply-templates select="*" mode="data"/>
		</Table>
	</xsl:template>
	<xsl:template match="*" mode="data">
		<Row>
			<xsl:for-each select="*">
				<xsl:variable name="sstype" select="@type"/>
				<Cell>
					<Data>
						<xsl:choose>
							<xsl:when test="$sstype and ($sstype = 'Number')">
								<xsl:attribute name="ss:Type"><xsl:value-of select="$sstype"/></xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="ss:Type">String</xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:value-of select="."/>
					</Data>
				</Cell>
			</xsl:for-each>
		</Row>
	</xsl:template>
</xsl:stylesheet>
