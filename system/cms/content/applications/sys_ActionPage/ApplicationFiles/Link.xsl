<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- This very simple stylesheet just returns the text link from a link element passed -->
<xsl:template match="/link">
    <xsl:value-of select="@url"/>
</xsl:template>

</xsl:stylesheet>
