<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" />
	
	<!-- Assigns whatever file path parameter is passed in to secondSchema --> 
	<xsl:param name="secondSchema" />
	<!-- Grab the table entries of the schema file parameter passed in. -->
	<xsl:variable name="schemaToAdd" select="document($secondSchema)/tables/table" />
	
	<!--  Create an identity file (our output) of the original file given in -->
	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
	
	<!-- Add all table entries to the tables tree of the output file -->
	<xsl:template match="tables">
		<xsl:copy>
			<!-- Ignore nodes already inside the tree -->
			<xsl:apply-templates select="table" />
			<!-- add table entries -->
			<xsl:apply-templates select="$schemaToAdd" />
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>