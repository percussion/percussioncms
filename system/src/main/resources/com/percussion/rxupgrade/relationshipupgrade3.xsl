<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" encoding="UTF-8" />
	<xsl:template match="/">
		<xsl:apply-templates select="." mode="copy"/>
	</xsl:template>
	<xsl:template match="@*|*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!-- This template replaces the override field for the sys_communityid -->
	<xsl:template match="PSXRelationshipConfig[@name='New Copy']/PSXCloneOverrideFieldList/PSXCloneOverrideField[@name='sys_communityid']" mode="copy">
		<PSXCloneOverrideField id="0" name="sys_communityid">
			<value>
				<PSXExtensionCall id="0">
					<name>Java/global/percussion/generic/sys_OverrideLiteral</name>
					<PSXExtensionParamValue id="0">
						<value>
							<PSXContentItemStatus id="0">
								<name>CONTENTSTATUS.COMMUNITYID</name>
							</PSXContentItemStatus>
						</value>
					</PSXExtensionParamValue>
					<PSXExtensionParamValue id="0">
						<value>
							<PSXTextLiteral id="0">
								<text>sys_communityid_override</text>
							</PSXTextLiteral>
						</value>
					</PSXExtensionParamValue>
				</PSXExtensionCall>
			</value>
			<Conditions/>
		</PSXCloneOverrideField>
	</xsl:template>
	<!-- This template replaces the override field for the sys_workflowid -->
	<xsl:template match="PSXRelationshipConfig[@name='New Copy']/PSXCloneOverrideFieldList/PSXCloneOverrideField[@name='sys_workflowid']" mode="copy">
		<PSXCloneOverrideField id="0" name="sys_workflowid">
			<value>
				<PSXExtensionCall id="0">
					<name>Java/global/percussion/generic/sys_OverrideLiteral</name>
					<PSXExtensionParamValue id="0">
						<value>
							<PSXContentItemStatus id="0">
								<name>WORKFLOWAPPS.WORKFLOWAPPID</name>
							</PSXContentItemStatus>
						</value>
					</PSXExtensionParamValue>
					<PSXExtensionParamValue id="0">
						<value>
							<PSXTextLiteral id="0">
								<text>sys_workflowid_override</text>
							</PSXTextLiteral>
						</value>
					</PSXExtensionParamValue>
				</PSXExtensionCall>
			</value>
			<Conditions/>
		</PSXCloneOverrideField>
	</xsl:template>
	<xsl:template match="PSXRelationshipConfig[@name='Promotable Version']/EffectSet" mode="copy">
		<EffectSet>
			<xsl:apply-templates select="*" mode="copy" />
			<xsl:if test="count(PSXConditionalEffect[@activationEndpoint='owner']/PSXExtensionCall[name='Java/global/percussion/relationship/effect/sys_AddCloneToFolder']) = 0">
				<PSXConditionalEffect activationEndpoint="owner">
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/relationship/effect/sys_AddCloneToFolder</name>
					</PSXExtensionCall>
				</PSXConditionalEffect>
			</xsl:if>
		</EffectSet>
	</xsl:template>
	<!-- This template replaces the sys_ValidateFolder with sys_TouchParentFolderEffect for Folder Content relationship -->
	<xsl:template match="/PSXRelationshipConfigSet/PSXRelationshipConfig/EffectSet/PSXConditionalEffect/PSXExtensionCall/name[text()='Java/global/percussion/relationship/effect/sys_ValidateFolder']" mode="copy">
		<name>Java/global/percussion/relationship/effect/sys_TouchParentFolderEffect</name>
	</xsl:template>

   <!-- Template to add  sys_folderid and sys_siteid user properties-->
	<xsl:template match="PSXRelationshipConfig[@category='rs_activeassembly']/UserPropertySet" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates select="*" mode="copy"/>
         <xsl:if test="not(PSXProperty/@name='sys_folderid')">
            <PSXProperty locked="no" name="sys_folderid">
               <Value type="String"/>
               <Description>The folder id used, optional.</Description>
            </PSXProperty>
         </xsl:if>
         <xsl:if test="not(PSXProperty/@name='sys_siteid')">
            <PSXProperty locked="no" name="sys_siteid">
               <Value type="String"/>
               <Description>The site id used, optional.</Description>
            </PSXProperty>
         </xsl:if>
		</xsl:copy>
	</xsl:template>
   <!-- Template to remove the sys_siteid and sys_folderid PSXProperty elements from PSXPropertySet if exists.-->
   <!-- The previous template adds them to the UserPropertySet.-->
	<xsl:template match="PSXRelationshipConfig[@category='rs_activeassembly']/PSXPropertySet/PSXProperty[@name='sys_siteid']" mode="copy"/>
	<xsl:template match="PSXRelationshipConfig[@category='rs_activeassembly']/PSXPropertySet/PSXProperty[@name='sys_folderid']" mode="copy"/>

</xsl:stylesheet>
