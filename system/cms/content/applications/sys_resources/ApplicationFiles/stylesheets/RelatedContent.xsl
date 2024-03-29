<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
		<!ENTITY % w3centities-f PUBLIC
				"-//W3C//ENTITIES Combined Set//EN//XML"
				"http://www.w3.org/2003/entities/2007/w3centities-f.ent"
				>
		%w3centities-f;
		]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="psxi18n"
                extension-element-prefixes="psxi18n">
  
<xsl:template match="RelatedContentLookup" mode="relatedcontent">
<xsl:if test="string-length(relatedentry/itemrelatedid)!=0">
      <table width="100%" cellpadding="4" cellspacing="0" border="0">
         <tr>
            <td class="headercell2">
<table width="100%" cellpadding="0" cellspacing="0" border="0">
<tr>
<td class="backgroundcolor">
               <table width="100%" cellpadding="0" cellspacing="1" border="0" class="backgroundcolor">
                  <tr class="headercell">
						<td class="headercell2font" align="center"> 
							Title(ID)
						</td>
						<td class="headercell2font" align="center"> 
							Slot
						</td>
						<td class="headercell2font" align="center"> 
							URL
						</td>
			</tr>
					<xsl:for-each select="relatedentry">
						<tr class="datacell1">
							<td class="datacell1font"> 
								<xsl:value-of select="itemtitle" />(<xsl:value-of select="itemcontentid" />)												</td>
							<td class="datacell1font"> 
								<xsl:value-of select="itemrelationship" />																	</td>
							<td class="datacell1font"> 
								<xsl:choose>
									<xsl:when test="string(itemurl)=''">
										<xsl:value-of select="'- Internal Link -'" />
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="itemurl" />
									</xsl:otherwise>
								</xsl:choose>
							</td>
						</tr>
					</xsl:for-each>					
				</table>
			</td>
		</tr>
	</table></td></tr></table>
</xsl:if>
    </xsl:template>

</xsl:stylesheet>
