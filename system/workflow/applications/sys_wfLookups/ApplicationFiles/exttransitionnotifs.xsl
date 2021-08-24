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
  <xsl:template match="*" mode="exttransitionnotifs">
		<tr>
		  <td colspan="10" class="headercellfont" align="right">
			 <a href="{transitionnotifs/newlink}">New Transition Notification</a>
		  </td>
		</tr>
      <tr class="headercell2">
        <td height="21" width="5%" align="center" class="headercell2font">&nbsp;</td>
        <td height="21" width="25%" align="left" class="headercell2font">Subject (Notification ID)</td>
        <td height="21" width="20%" align="left" class="headercell2font">State Role Recipient Type</td>
        <td height="21" width="25%" align="left" class="headercell2font">Additional Recipient List</td>
        <td height="21" width="25%" align="left" class="headercell2font">CC List</td>
      </tr>
    <xsl:for-each select="transitionnotifs">
     <xsl:if test="not(notificationid = '')">
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
        <td class="datacell1font" height="25" align="center">
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
					<xsl:apply-templates select="subject"/>&nbsp;(<xsl:apply-templates select="notificationid"/>)&nbsp;
             </a>
       </td>
        <td class="datacell1font" height="20" align="left">
			<xsl:variable name="reciptype"><xsl:value-of select="staterolerecipienttypes"/></xsl:variable>
			<xsl:apply-templates select="document(reciptypeurl)/*/item[value=$reciptype]/display"/>&nbsp;
        </td>
        <td class="datacell1font" height="20" align="left">
          <xsl:apply-templates select="additionalrecipientlist"/>
        </td>
        <td class="datacell1font" height="20" align="left">
          <xsl:apply-templates select="cclist"/>
        </td>
      </tr>
     </xsl:if>
	  <xsl:if test="count(.)=1 and notificationid = ''">
		<tr class="datacell1">
			<td align="center" colspan="5" class="datacellnoentriesfound">
				No entries found.&nbsp;
			</td>
      </tr>
	  </xsl:if>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
