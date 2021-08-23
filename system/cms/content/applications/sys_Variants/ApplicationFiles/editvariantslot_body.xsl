<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 SYSTEM "../../DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol SYSTEM "../../DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial SYSTEM "../../DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
<xsl:template mode="editvariantslot_mainbody" match="*">
<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus" /> 
<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
<xsl:variable name="contenttype" select="/*/contenttypeurl"/>
<xsl:variable name="variantslot" select="/*/variantsloturl"/>
<xsl:variable name="contentlookup" select="/*/contentlookupurl"/>
<xsl:variable name="contvariantid" select="/*/contvariantidurl"/>
<xsl:variable name="componentname" select="componentname"/>
<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
   <tr class="outerboxcell">
      <td class="outerboxcellfont" align="right" valign="top">
         Edit Slot
      </td>
   </tr>
   <tr class="headercell">
     <td>
       <table width="100%" cellpadding="0" cellspacing="1" border="0">
	      <xsl:apply-templates select="document($contvariantid)" mode="mode5"/>
         <tr class="headercell2">
            <td width="5%" align="center" valign="middle" class="headercell2font">&nbsp;</td>
            <td align="center" width="95%" class="headercell2font">Slot Type(ID)</td>
         </tr>
         <xsl:for-each select="add">
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
				<xsl:choose>
					<xsl:when test="not(string-length(./slottypeid)) and position()=1" >
						<td class="datacellnoentriesfound" colspan="2" align="center">
                     No entries found.&nbsp;
                  </td>
					</xsl:when>
					<xsl:otherwise>
						<td width="5%" align="center" class="datacell1font">
							<xsl:attribute name="id">
							<xsl:value-of select="slottypeid"/>
							</xsl:attribute>
							<xsl:if test="string-length(./slottypeid)" > <a href="{./sloturl}"> 
							<img height="17" alt="Add" src="../sys_resources/images/new.gif" width="17" border="0" /></a> 
							</xsl:if> 
						</td>
						<td align="left" class="datacell1font">  
							<xsl:if test="string-length(./slottypeid)" > <xsl:value-of select="./slottype" />(<xsl:value-of select="./slottypeid" />) 
							</xsl:if> 
						</td>
					</xsl:otherwise>
				</xsl:choose>
         </tr>
         </xsl:for-each>
      </table>
     </td>
   </tr>
 <tr class="headercell">
   <td height="100%">&nbsp;</td>
   <!--   Fill down to the bottom   -->
 </tr>
</table>
</xsl:template>

<xsl:template match="*" mode="mode5">
 <xsl:for-each select=".">
   <input name="contenttypeid" type="hidden">
     <xsl:attribute name="value">
       <xsl:value-of select="contenttypeid"/>
     </xsl:attribute>
   </input>
   <input name="variantid" type="hidden">
     <xsl:attribute name="value">
       <xsl:value-of select="variantid"/>
     </xsl:attribute>
   </input>
   <tr class="headercell">        <!--   Repeats once per category   -->
     <td valign="top" align="left" class="headercellfont" colspan="2">
       Edit&nbsp;<xsl:apply-templates select="contenttype"/>&nbsp;Variant Slot
     </td>
   </tr>
    <tr class="headercell">        <!--   Repeats once per category   -->
     <td valign="top" align="left" class="headercellfont" colspan="2">
      Variant Name:&nbsp;<xsl:apply-templates select="varianttype"/>&nbsp;(<xsl:apply-templates select="variantid"/>)
     </td>
   </tr>
 </xsl:for-each>
</xsl:template>
</xsl:stylesheet>
