<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="xml" indent="no" omit-xml-declaration="no" encoding="UTF-8"/>
    <xsl:template match="@*|node()"> 
       <xsl:copy> 
          <xsl:apply-templates select="@*|node()" /> 
       </xsl:copy> 
    </xsl:template>
   <xsl:template match="SectionLinkList"><!-- removes SecionLinkList/PSXUrlRequest nodes referring only to URLs used by the relatedcontent shared group, only if the relatedcontent shared group was included a a SharedFieldIncludes/SharedFieldGroupName. -->
      	<xsl:choose>
	   	<xsl:when test="//PSXContentEditor/@enableRelatedContent='yes'"> <!-- if the relatedcontent shared group was included -->
	   	<SectionLinkList>
	   	<xsl:for-each select="PSXUrlRequest">
	   		<xsl:choose>
	   			<xsl:when test="./@name='RelatedLookupURL'"/> <!-- RelatedLookupURL, VariantListURL, and ContentSlotLookupURL are the URLs used by related content in the relatedcontent shared group.  If encountered, the PSXUrlRequest containing it gets ignored -->
	   			<xsl:when test="./@name='VariantListURL'"/>
	   			<xsl:when test="./@name='ContentSlotLookupURL'"/>
	   			 <xsl:otherwise> <!-- all other PSXUrlRequests get copied -->
	   			 	<xsl:copy>
	   			 		<xsl:for-each select="@*">
	   			 			<xsl:copy/>
	   			 		</xsl:for-each>
	   			 		<xsl:apply-templates/>
	   			 	</xsl:copy>
	   			 </xsl:otherwise>
	   		 </xsl:choose>
	   	</xsl:for-each>
	   	</SectionLinkList>
	   	</xsl:when>
	   	<xsl:otherwise>  <!-- if the relatedcontent shared group was not included, copy the entire SectionLinkList node -->
 			 <xsl:copy>
 			 	<xsl:for-each select="@*">
 			 		<xsl:copy/>
 			 	</xsl:for-each>
 			 	<xsl:apply-templates/>
 			 </xsl:copy>
 		</xsl:otherwise>
   	</xsl:choose>
   </xsl:template>
  					
<xsl:template match="PSXDisplayMapping"> <!-- remove PSXDisplayMapping overrides for the relatedcontent control if the relatedcontent shared field group was included in the CE-->
	<xsl:choose>
 		<xsl:when test="//PSXContentEditor/@enableRelatedContent='yes'"> 
   			<xsl:choose>
				<xsl:when test="./FieldRef='relatedcontent'"/>  <!-- ignore the relatedcontent PSXDisplayMapping -->
				<xsl:otherwise>  <!-- copy all others -->
 			 		<xsl:copy>
 			 			<xsl:for-each select="@*">
 			 				<xsl:copy/>
 			 			</xsl:for-each>
 			 			<xsl:apply-templates/>
 					 </xsl:copy>
 				</xsl:otherwise>
			</xsl:choose>
		</xsl:when>
		 <xsl:otherwise> 
			 <xsl:copy>
 			 	<xsl:for-each select="@*">
 			 		<xsl:copy/>
 			 	</xsl:for-each>
 			 	<xsl:apply-templates/>
 			 </xsl:copy>
 		</xsl:otherwise>
   	</xsl:choose>
</xsl:template>
</xsl:stylesheet>
