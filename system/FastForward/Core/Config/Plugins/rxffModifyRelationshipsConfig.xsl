<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!-- The purpose of this XSL file is to add NavFolderEffect to Relationship Configuration XML file-->
	<xml:output method="xml" encoding="UTF-8"/>
	<!-- main template -->
	<xsl:template match="/">
		<xsl:apply-templates select="." mode="copy"/>
	</xsl:template>
	<!-- copy any attribute or template -->
	<xsl:template match="@*|*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!-- Match on PSXRelationshipConfig with category name as rs_folder and add the NavFolderEffect if it does not exist-->
	<xsl:template match="PSXRelationshipConfig[@category='rs_folder']/EffectSet" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:if test="not(PSXConditionalEffect/PSXExtensionCall/name='Java/global/percussion/fastforward/managednav/rxs_NavFolderEffect')">
         <PSXConditionalEffect activationEndpoint="either">
            <PSXExtensionCall id="0">
               <name>Java/global/percussion/fastforward/managednav/rxs_NavFolderEffect</name>
            </PSXExtensionCall>
            <Conditions>
               <PSXRule boolean="and">
                  <PSXConditional id="0">
                     <variable>
                        <PSXSingleHtmlParameter id="0">
                           <name>rxs_disableNavFolderEffect</name>
                        </PSXSingleHtmlParameter>
                     </variable>
                     <operator>&lt;&gt;</operator>
                     <value>
                        <PSXTextLiteral id="0">
                           <text>y</text>
                        </PSXTextLiteral>
                     </value>
                     <boolean>AND</boolean>
                  </PSXConditional>
               </PSXRule>
               <PSXRule boolean="and">
                  <PSXConditional id="0">
                     <variable>
                        <PSXOriginatingRelationshipProperty id="0">
                           <name>category</name>
                        </PSXOriginatingRelationshipProperty>
                     </variable>
                     <operator>&lt;&gt;</operator>
                     <value>
                        <PSXTextLiteral id="0">
                           <text>rs_translation</text>
                        </PSXTextLiteral>
                     </value>
                     <boolean>AND</boolean>
                  </PSXConditional>
               </PSXRule>
            </Conditions>
            <ExecutionContextSet>
               <ExecutionContext type="PreConstruction"/>
               <ExecutionContext type="PreDestruction"/>
            </ExecutionContextSet>
         </PSXConditionalEffect>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
