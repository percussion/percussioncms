<?xml version="1.0" encoding="iso-8859-1"?>


<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/">
    <html>
      <body>
        <h2>Report - Results after importing sites</h2>
        <table border="1">
          <tr bgcolor="#CCCCFF">
            <th>Original Website URL</th>
            <th>Imported (Yes/No)</th>
            <th>Import log</th>
            <th>Link to preview page</th>
            <th>Remarks</th>
          </tr>
          <xsl:for-each select="entries/entry">
            <tr>
              <td>
                <a href="{siteUrl}" target="_blank" rel = "noopener noreferrer"><xsl:value-of select="siteUrl"/></a>
              </td>
              <td>
                <xsl:value-of select="importError" />
              </td>
              <td>
                <a href="{logUrl}" target="_blank" rel = "noopener noreferrer">Download</a>
              </td>
              <td>
	             <a href="{previewPageUrl}" target="_blank" rel = "noopener noreferrer"><xsl:value-of select="importedSiteName"/></a>
              </td>
              <td>
                <xsl:value-of select="remarks" />
              </td>
            </tr>
          </xsl:for-each>
        </table>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
