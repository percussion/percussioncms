<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
	<xsl:template name="bannerAndUserStatus">
        <div id="RhythmyxBanner">
			<table border='0' cellspacing='0' cellpadding='0' class="rx-banner-table">
				<tr class="rx-banner-row">
					<td style="vertical-align: bottom">
						<!--   start banner slot   -->
						<!-- begin XSL -->
						<xsl:for-each select="document($relatedlinks)/*/component[@slotname='slt_banner']">
							<xsl:copy-of select="document(url)/*/body/*"/>
						</xsl:for-each>
						<!-- end XSL -->
						<!--   end banner slot   -->
					</td>
					<td align="right" valign="bottom">
						<!--   start user status slot   -->
						<!-- begin XSL -->
						<xsl:for-each select="document($relatedlinks)/*/component[@slotname='slt_userstatus']">
							<xsl:copy-of select="document(url)/*/body/*"/>
						</xsl:for-each>
						<!-- end XSL -->
						<!--   end user status slot   -->
					</td>
				</tr>
			</table>
		</div>
	</xsl:template>
</xsl:stylesheet>
