<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml"/>
	<xsl:template match="/">
		<xsl:apply-templates select="." mode="copy"/>
	</xsl:template>
	<xsl:template match="@*|*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!-- Template to upgrade  category='rs_copy' -->
	<xsl:template match="PSXRelationshipConfig[@category='rs_copy']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:if test="not(PSXCloneOverrideFieldList)">
				<PSXCloneOverrideFieldList id="0">
					<PSXCloneOverrideField id="0" name="sys_title">
						<value>
							<PSXExtensionCall id="0">
								<name>Java/global/percussion/relationship/sys_CloneTitle</name>
								<PSXExtensionParamValue id="0">
									<value>
										<PSXTextLiteral id="0">
											<text>Copy ($clone_count) of {0}</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="0">
									<value>
										<PSXContentItemStatus id="0">
											<name>CONTENTSTATUS.TITLE</name>
										</PSXContentItemStatus>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="0">
									<value>
										<PSXTextLiteral id="0">
											<text/>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="0">
									<value>
										<PSXTextLiteral id="0">
											<text/>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="0">
									<value>
										<PSXTextLiteral id="0">
											<text/>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
							</PSXExtensionCall>
						</value>
						<Conditionals/>
					</PSXCloneOverrideField>
					<PSXCloneOverrideField id="0" name="sys_communityid">
						<value>
							<PSXExtensionCall id="0">
								<name>Java/global/percussion/generic/sys_Literal</name>
								<PSXExtensionParamValue id="0">
									<value>
										<PSXContentItemStatus id="0">
											<name>CONTENTSTATUS.COMMUNITYID</name>
										</PSXContentItemStatus>
									</value>
								</PSXExtensionParamValue>
							</PSXExtensionCall>
						</value>
						<Conditionals/>
					</PSXCloneOverrideField>
					<PSXCloneOverrideField id="0" name="sys_workflowid">
						<value>
							<PSXExtensionCall id="0">
								<name>Java/global/percussion/generic/sys_Literal</name>
								<PSXExtensionParamValue id="0">
									<value>
										<PSXContentItemStatus id="0">
											<name>WORKFLOWAPPS.WORKFLOWAPPID</name>
										</PSXContentItemStatus>
									</value>
								</PSXExtensionParamValue>
							</PSXExtensionCall>
						</value>
						<Conditionals/>
					</PSXCloneOverrideField>
				</PSXCloneOverrideFieldList>
			</xsl:if>
			<xsl:apply-templates select="*" mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!-- Template to upgrade  category='rs_promotable' -->
	<xsl:template match="PSXRelationshipConfig[@category='rs_promotable']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:if test="not(PSXCloneOverrideFieldList)">
				<PSXCloneOverrideFieldList id="0">
					<PSXCloneOverrideField id="0" name="sys_title">
						<value>
							<PSXExtensionCall id="0">
								<name>Java/global/percussion/relationship/sys_CloneTitle</name>
								<PSXExtensionParamValue id="0">
									<value>
										<PSXTextLiteral id="0">
											<text>PV Copy ($clone_count) of {0}</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="0">
									<value>
										<PSXContentItemStatus id="0">
											<name>CONTENTSTATUS.TITLE</name>
										</PSXContentItemStatus>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="0">
									<value>
										<PSXTextLiteral id="0">
											<text/>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="0">
									<value>
										<PSXTextLiteral id="0">
											<text/>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="0">
									<value>
										<PSXTextLiteral id="0">
											<text/>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
							</PSXExtensionCall>
						</value>
						<Conditionals/>
					</PSXCloneOverrideField>
					<PSXCloneOverrideField id="0" name="sys_communityid">
						<value>
							<PSXExtensionCall id="0">
								<name>Java/global/percussion/generic/sys_Literal</name>
								<PSXExtensionParamValue id="0">
									<value>
										<PSXContentItemStatus id="0">
											<name>CONTENTSTATUS.COMMUNITYID</name>
										</PSXContentItemStatus>
									</value>
								</PSXExtensionParamValue>
							</PSXExtensionCall>
						</value>
						<Conditionals/>
					</PSXCloneOverrideField>
					<PSXCloneOverrideField id="0" name="sys_workflowid">
						<value>
							<PSXExtensionCall id="0">
								<name>Java/global/percussion/generic/sys_Literal</name>
								<PSXExtensionParamValue id="0">
									<value>
										<PSXContentItemStatus id="0">
											<name>CONTENTSTATUS.WORKFLOWAPPID</name>
										</PSXContentItemStatus>
									</value>
								</PSXExtensionParamValue>
							</PSXExtensionCall>
						</value>
						<Conditionals/>
					</PSXCloneOverrideField>
				</PSXCloneOverrideFieldList>
			</xsl:if>
			<xsl:apply-templates select="*" mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!-- Template to add empty PSXCloneOverrideFieldList to category='rs_activeassembly' -->
	<xsl:template match="PSXRelationshipConfig[@category='rs_activeassembly']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:if test="not(PSXCloneOverrideFieldList)">
				<PSXCloneOverrideFieldList id="0"/>
			</xsl:if>
			<xsl:apply-templates select="*" mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!-- Template to add empty PSXCloneOverrideFieldList to category='rs_activeassembly' -->
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

	<!-- Template to add empty PSXCloneOverrideFieldList to category='rs_folder' -->
	<xsl:template match="PSXRelationshipConfig[@category='rs_folder']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:if test="not(PSXCloneOverrideFieldList)">
				<PSXCloneOverrideFieldList id="0"/>
			</xsl:if>
			<xsl:apply-templates select="*" mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!--  add rs_skippromotion property equal to yes for folder relationship -->
	<xsl:template match="PSXRelationshipConfig[@category='rs_folder']/PSXPropertySet" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates select="*" mode="copy"/>
			<PSXProperty locked="yes" name="rs_skippromotion">
				<Value type="Boolean">yes</Value>
				<Description>Specifies if this relationship should be skipped when an item is promoted.</Description>
			</PSXProperty>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXRelationshipConfig[@category='rs_folder']/PSXPropertySet/PSXProperty[@name='rs_skippromotion']" mode="copy"/>
	<!-- Template to upgrade @category='rs_translation' and @label='Translation' -->
	<xsl:template match="PSXRelationshipConfig[@category='rs_translation' and @label='Translation']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:if test="not(PSXCloneOverrideFieldList)">
				<xsl:call-template name="rs_translationCloneOverrideFieldList"/>
			</xsl:if>
			<xsl:apply-templates select="*" mode="copy"/>
			<xsl:if test="not(EffectSet)">
				<EffectSet>
					<PSXConditionalEffect activationEndpoint="owner">
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/relationship/effect/sys_isCloneExists</name>
						</PSXExtensionCall>
					</PSXConditionalEffect>
					<PSXConditionalEffect activationEndpoint="owner">
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/relationship/effect/sys_AttachTranslatedFolder</name>
						</PSXExtensionCall>
					</PSXConditionalEffect>
				</EffectSet>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<!--  replace category 'rs_newcopy' by 'rs_copy' -->
	<xsl:template match="text[.='rs_newcopy']" mode="copy">
		<text>rs_copy</text>
	</xsl:template>
	<!--  remove sys_TranslationConstraint Extension -->
	<xsl:template match="PSXRelationshipConfig[@category='rs_translation']/ExtensionSet/PSXConditionalExtension[PSXExtensionCall/name='Java/global/percussion/relationship/sys_TranslationConstraint']" mode="copy"/>
	<!-- Template to add two translation effects to EffectSet of category='rs_translation' -->
	<xsl:template match="PSXRelationshipConfig[@category='rs_translation']/EffectSet" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:if test="not(PSXConditionalEffect/PSXExtensionCall/name='Java/global/percussion/relationship/effect/sys_isCloneExists')">
				<PSXConditionalEffect activationEndpoint="owner">
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/relationship/effect/sys_isCloneExists</name>
					</PSXExtensionCall>
				</PSXConditionalEffect>
			</xsl:if>
			<xsl:apply-templates select="*" mode="copy"/>
			<xsl:if test="not(PSXConditionalEffect/PSXExtensionCall/name='Java/global/percussion/relationship/effect/sys_AttachTranslatedFolder')">
				<PSXConditionalEffect activationEndpoint="owner">
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/relationship/effect/sys_AttachTranslatedFolder</name>
					</PSXExtensionCall>
				</PSXConditionalEffect>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<!-- Template to add to upgrade 'Translation - Mandatory' -->
	<xsl:template match="PSXRelationshipConfig[@category='rs_translation' and @label='Translation - Mandatory']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:if test="not(PSXCloneOverrideFieldList)">
				<xsl:call-template name="rs_translationCloneOverrideFieldList"/>
			</xsl:if>
			<xsl:apply-templates select="*" mode="copy"/>
			<xsl:if test="not(EffectSet)">
				<EffectSet>
					<PSXConditionalEffect activationEndpoint="owner">
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/relationship/effect/sys_isCloneExists</name>
						</PSXExtensionCall>
					</PSXConditionalEffect>
					<PSXConditionalEffect activationEndpoint="dependent">
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/relationship/effect/sys_PublishMandatory</name>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>no</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text/>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text/>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
						</PSXExtensionCall>
					</PSXConditionalEffect>
					<PSXConditionalEffect activationEndpoint="dependent">
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/relationship/effect/sys_UnpublishMandatory</name>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>no</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text/>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text/>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
						</PSXExtensionCall>
					</PSXConditionalEffect>
					<PSXConditionalEffect activationEndpoint="owner">
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/relationship/effect/sys_AttachTranslatedFolder</name>
						</PSXExtensionCall>
					</PSXConditionalEffect>
				</EffectSet>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<!-- Named Template to insert CloneOverrideFieldList to category='rs_translation' -->
	<xsl:template name="rs_translationCloneOverrideFieldList">
		<PSXCloneOverrideFieldList id="0">
			<PSXCloneOverrideField id="0" name="sys_title">
				<value>
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/relationship/sys_CloneTitle</name>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>[{0}] Copy ($clone_count) of {1}</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXSingleHtmlParameter id="0">
									<name>sys_lang</name>
								</PSXSingleHtmlParameter>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXContentItemStatus id="0">
									<name>CONTENTSTATUS.TITLE</name>
								</PSXContentItemStatus>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text/>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text/>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
					</PSXExtensionCall>
				</value>
				<Conditionals/>
			</PSXCloneOverrideField>
			<PSXCloneOverrideField id="0" name="sys_communityid">
				<value>
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/cms/sys_cloneOverrideField</name>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>../sys_trFieldOverride/TranslationFieldOverride.xml</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>CommunityId</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>sys_contentid</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXContentItemStatus id="0">
									<name>CONTENTSTATUS.CONTENTID</name>
								</PSXContentItemStatus>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>sys_lang</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXSingleHtmlParameter id="0">
									<name>sys_lang</name>
								</PSXSingleHtmlParameter>
							</value>
						</PSXExtensionParamValue>
					</PSXExtensionCall>
				</value>
				<Conditionals/>
			</PSXCloneOverrideField>
			<PSXCloneOverrideField id="0" name="sys_workflowid">
				<value>
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/cms/sys_cloneOverrideField</name>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>../sys_trFieldOverride/TranslationFieldOverride.xml</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>WorkflowId</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>sys_contentid</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXContentItemStatus id="0">
									<name>CONTENTSTATUS.CONTENTID</name>
								</PSXContentItemStatus>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>sys_lang</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXSingleHtmlParameter id="0">
									<name>sys_lang</name>
								</PSXSingleHtmlParameter>
							</value>
						</PSXExtensionParamValue>
					</PSXExtensionCall>
				</value>
				<Conditionals/>
			</PSXCloneOverrideField>
			<PSXCloneOverrideField id="0" name="sys_lang">
				<value>
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/generic/sys_Literal</name>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXSingleHtmlParameter id="0">
									<name>sys_lang</name>
								</PSXSingleHtmlParameter>
							</value>
						</PSXExtensionParamValue>
					</PSXExtensionCall>
				</value>
				<Conditionals/>
			</PSXCloneOverrideField>
		</PSXCloneOverrideFieldList>
	</xsl:template>
</xsl:stylesheet>
