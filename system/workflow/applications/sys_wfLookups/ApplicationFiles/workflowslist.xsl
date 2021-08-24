<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
	<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
	<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
    <xsl:output method="html" omit-xml-declaration="yes"/>
  <xsl:template match="*" mode="workflowlist">
  <table width="100%" cellpadding="0" cellspacing="1" border="0">
	 <tr class="datacelll"> 
		<!-- Repeats once per view row -->
        <td height="21" width="100%" align="right" class="outerboxcellfont"  colspan="4">Workflows</td>
      </tr>
	 <tr class="headercell"> 
		<!-- Repeats once per view row -->
        <td height="21" width="100%" align="right" class="headercellfont"  colspan="4">
		      <a href="{newwflink}"> New Workflow</a>&nbsp;&nbsp;<a href="{copywflink}">Copy Workflow</a>
			</td>
      </tr>
	 <tr class="datacell2"> 
		<!-- Repeats once per view row -->
        <td height="21" width="5%" align="center" class="headercellfont">&nbsp;</td>
        <td height="21" width="45%" align="left" class="headercellfont">Name (ID)</td>
        <td height="21" width="40%" align="left" class="headercellfont">Description</td>
        <td height="21" width="10%" align="center" class="headercellfont">Preview</td>
      </tr>
    <xsl:for-each select="workflow">
     <xsl:if test="workflowid != ''">
      <tr class="datacell1">
			<xsl:attribute name="class"> 
				<xsl:choose> 
					<xsl:when test="position() mod 2 = 1"> 
						<xsl:value-of select="'datacell1'"/> 
					</xsl:when> 
					<xsl:otherwise> 
						<xsl:value-of select="'datacell2'"/> 
					</xsl:otherwise> 
				</xsl:choose> 
			</xsl:attribute> 
		  <td class="datacell1font" height="21" align="center">
            <a>
              <xsl:attribute name="href">javascript: delConfirmWf('<xsl:value-of select="deletelink"/>' );</xsl:attribute>

              <img src="/sys_resources/images/delete.gif" width="21" height="21" border="0" alt="Delete" title="Delete"/>
            </a>
        </td>
        <td class="datacell1font" height="21" align="left">
             <a>
              <xsl:attribute name="href">
                <xsl:value-of select="link"/>
              </xsl:attribute>
					<xsl:apply-templates select="name"/>&nbsp;(<xsl:apply-templates select="workflowid"/>)&nbsp;
            </a>
        </td>

        <td class="datacell1font" height="21" align="left">
          <xsl:apply-templates select="description"/>
        </td>

        <td class="datacell1font" height="21" align="center">
            <a>
					<xsl:attribute name="href">
						<xsl:text>javascript:{}</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="onClick">
						<xsl:text>window.open('</xsl:text>
						<xsl:value-of select="previewlink"/>
						<xsl:text>', 'previewworkflow', 'width=860,height=500,location=0;0,toolbar=no,menubar=no,scrollbars=yes,resizable=yes')</xsl:text>
					</xsl:attribute>
              <img src="/sys_resources/images/preview.gif" width="21" height="21" border="0" alt="Preview Workflow" title="Preview Workflow"/>
            </a>
        </td>

      </tr>
     </xsl:if>
	  <xsl:if test="count(.)=1 and workflowid = ''">
		<tr class="datacell1">
			<td align="center" colspan="4" class="datacellnoentriesfound">
				No entries found.&nbsp;
			</td>
      </tr>
	  </xsl:if>
    </xsl:for-each>
    </table>
  <table width="100%" height="100%" cellpadding="0" cellspacing="1" border="0">
	 <tr class="headercell"> 
		<!-- Repeats once per view row -->
        <td height="100%" width="100%">&nbsp;</td>
    </tr>
    </table>
  </xsl:template>

</xsl:stylesheet>
