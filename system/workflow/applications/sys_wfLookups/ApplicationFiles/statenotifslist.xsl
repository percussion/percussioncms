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
  <xsl:template match="*" mode="statenotiflist">
 		<tr>
		  <td colspan="4" class="headercellfont" align="right">
			 <a href="{newlink}">New Notification</a>
		  </td>
		</tr>
     <tr class="headercell2">
        <td height="21" align="center" width="5%" class="headercell2font">&nbsp;</td>
        <td height="21" align="left" width="25%" class="headercell2font">Subject (ID)</td>
        <td height="21" align="left" width="30%" class="headercell2font">Description</td>
        <td height="21" align="left" width="40%" class="headercell2font">Body</td>
      </tr>
    <xsl:for-each select="statenotif">
     <xsl:if test="notifid != ''">
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
        <td class="datacell1font" align="center" height="25">
            <a>
              <xsl:attribute name="href">javascript: delConfirm('<xsl:value-of select="deletelink"/>' );</xsl:attribute>
              <img src="/sys_resources/images/delete.gif" width="21" height="21" border="0"/>
            </a>
        </td>
        <td class="datacell1font" height="20" align="left">
           <a>
              <xsl:attribute name="href">
                <xsl:value-of select="link"/>
              </xsl:attribute>
					<xsl:apply-templates select="subject"/>&nbsp;(<xsl:apply-templates select="notifid"/>)&nbsp;
           </a>
       </td>
        <td class="datacell1font" height="20" align="left">
					<xsl:apply-templates select="description"/>&nbsp;
       </td>
        <td class="datacell1font" height="20" align="left">
          <xsl:apply-templates select="body"/>
        </td>
      </tr>
     </xsl:if>
	  <xsl:if test="count(.)=1 and notifid = ''">
		<tr class="datacell1">
			<td align="center" colspan="4" class="datacellnoentriesfound">
				No entries found.&nbsp;
			</td>
      </tr>
	  </xsl:if>
    </xsl:for-each>
  </xsl:template>
  <xsl:template match="body">
	<xsl:choose>
		<xsl:when test="string-length(.)&gt;50">
			<xsl:value-of select="concat(substring(.,0,50),'...')"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="."/>
		</xsl:otherwise>
	</xsl:choose>
  </xsl:template>
</xsl:stylesheet>
