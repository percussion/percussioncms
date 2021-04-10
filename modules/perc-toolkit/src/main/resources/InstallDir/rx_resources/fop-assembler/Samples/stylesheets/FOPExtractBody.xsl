<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:svg="http://www.w3.org/2000/svg" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
    <xsl:param name="sessionid" />
	<xsl:output omit-xml-declaration="yes"/>
	<xsl:template match="/">
	    <fo:block-container>
		<fo:block>
			<xsl:apply-templates select="*" />
		</fo:block>
		</fo:block-container>
	</xsl:template>
	<xsl:template match="div">
	    <xsl:apply-templates select="node()" />
	</xsl:template>
	<xsl:template match="p">
		<fo:block font-size="10pt" font-family="Times" line-height="11pt" space-after.optimum="15pt">
			<xsl:apply-templates select="node()" />
		</fo:block>
	</xsl:template>
	<xsl:template match="b">
		<fo:inline font-weight="bold">
			<xsl:apply-templates select="node()"/>
		</fo:inline>
	</xsl:template>
	<xsl:template match="em">
		<fo:inline font-weight="bold">
			<xsl:apply-templates select="node()"/>
		</fo:inline>
	</xsl:template>
	<xsl:template match="strong">
		<fo:inline font-weight="1000">
			<xsl:apply-templates select="node()"/>
		</fo:inline>
	</xsl:template>
	<xsl:template match="i">
		<fo:inline font-style="italic">
			<xsl:apply-templates select="node()"/>
		</fo:inline>
	</xsl:template>
	<xsl:template match="u">
		<fo:inline text-decoration="underline">
			<xsl:apply-templates select="node()"/>
		</fo:inline>
	</xsl:template>
	<xsl:template match="span[contains(@style,'underline')]">
		<fo:inline text-decoration="underline">
			<xsl:apply-templates select="node()"/>
		</fo:inline>
	</xsl:template>
	<xsl:template match="img">
	    
		<fo:block font-size="10pt" font-family="Times" line-height="11pt" space-after.optimum="15pt" keep-with-next.within-column="always">
			<fo:external-graphic content-height="100%" content-width="100%">
			  <xsl:attribute name="src">
			    <xsl:call-template name="rewriteURL">
			        <xsl:with-param name="url"><xsl:value-of select="@src" /></xsl:with-param>
			    </xsl:call-template>
			  </xsl:attribute>
			</fo:external-graphic>
		</fo:block>
		<fo:block font-weight="bold">
            <xsl:value-of select="@title" />	
        </fo:block>
	</xsl:template>
	<xsl:template match="table">
		<fo:table width="15cm" table-layout="fixed">
			<fo:table-column column-width="100pt" column-number="1"/>
			<fo:table-column column-width="100pt" column-number="2"/>
			<fo:table-column column-width="100pt" column-number="3"/>
			<fo:table-column column-width="100pt" column-number="4"/>
			<fo:table-column column-width="100pt" column-number="5"/>
			<fo:table-column column-width="100pt" column-number="6"/>
			<fo:table-column column-width="100pt" column-number="7"/>
			<fo:table-body>
				<xsl:for-each select="tr">
					<fo:table-row>
						<xsl:for-each select="td">
							<fo:table-cell border-color="black" border-style="solid" border-width="0.2mm">
								<fo:block>
									<xsl:apply-templates />
								</fo:block>
							</fo:table-cell>
						</xsl:for-each>
					</fo:table-row>
				</xsl:for-each>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	<xsl:template name="rewriteURL">
	   <xsl:param name="url" />
	   url(<xsl:value-of select="$url"/>&amp;pssessionid=<xsl:value-of select="$sessionid"/>)
	</xsl:template>
</xsl:stylesheet>