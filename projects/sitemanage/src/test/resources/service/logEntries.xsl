<?xml version="1.0" encoding="iso-8859-1"?>
<!--
  ~     Percussion CMS
  ~     Copyright (C) Percussion Software, Inc.  1999-2020
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU General Public License as published by
  ~     the Free Software Foundation, either version 3 of the License, or
  ~     (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU General Public License for more details.
  ~
  ~      Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU General Public License
  ~     along with this program.  If not, see <https://www.gnu.org/licenses/>.
  -->

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
                <a href="{siteUrl}" target="_blank"><xsl:value-of select="siteUrl"/></a>
              </td>
              <td>
                <xsl:value-of select="importError" />
              </td>
              <td>
                <a href="{logUrl}" target="_blank">Download</a>
              </td>
              <td>
	             <a href="{previewPageUrl}" target="_blank"><xsl:value-of select="importedSiteName"/></a>
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
