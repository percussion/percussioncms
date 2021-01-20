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
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!-- main template -->
	<xsl:template match="/">
		<xsl:apply-templates mode="copy"/>
	</xsl:template>
	<!-- copy any attribute or template -->
	<xsl:template match="@*|*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="comment()" mode="copy">
		<xsl:copy/>
	</xsl:template>
	<!-- Add <PSXUrlRequest name="ComponentLookupURL"> element if does not exist to <SectionLinkList> element-->
	<xsl:template match="SectionLinkList" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:if test="not(PSXUrlRequest/@name='ComponentLookupURL')">
				<PSXUrlRequest name="ComponentLookupURL">
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/generic/sys_MakeIntLink</name>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>../sys_ComponentSupport/componentsupport.xml</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>sys_componentname</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>ce_main</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
					</PSXExtensionCall>
				</PSXUrlRequest>
			</xsl:if>
			<xsl:if test="not(PSXUrlRequest/@name='actionlisturl')">
				<PSXUrlRequest name="actionlisturl">
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/generic/sys_MakeIntLink</name>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>../sys_uiSupport/ActionList.xml</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>sys_mode</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>CMS Centric</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
					</PSXExtensionCall>
				</PSXUrlRequest>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXField" mode="copy">
		<xsl:variable name="readonly" select="generate-id(@readOnly)"/>
		<xsl:copy>
			<xsl:apply-templates select="@*[generate-id(.) != $readonly]" mode="copy"/>
			<xsl:choose>
				<xsl:when test="@name='sys_communityid'">
					<xsl:if test="not(@defaultSearchLabel)">
						<xsl:attribute name="defaultSearchLabel">Community Id</xsl:attribute>
					</xsl:if>
					<xsl:if test="not(@modificationType)">
						<xsl:attribute name="modificationType">userCreate</xsl:attribute>
					</xsl:if>
					<xsl:attribute name="systemMandatory">yes</xsl:attribute>
				</xsl:when>
				<xsl:when test="@name='sys_contentstartdate' and not(@defaultSearchLabel)">
					<xsl:attribute name="defaultSearchLabel">Content start date</xsl:attribute>
				</xsl:when>
				<xsl:when test="@name='sys_contentexpirydate' and not(@defaultSearchLabel)">
					<xsl:attribute name="defaultSearchLabel">Content expiration date</xsl:attribute>
				</xsl:when>
				<xsl:when test="@name='sys_reminderdate' and not(@defaultSearchLabel)">
					<xsl:attribute name="defaultSearchLabel">Reminder date</xsl:attribute>
				</xsl:when>
				<xsl:when test="@name='sys_title' and not(@defaultSearchLabel)">
					<xsl:attribute name="defaultSearchLabel">System title</xsl:attribute>
				</xsl:when>
				<xsl:when test="@name='sys_pubdate' and not(@defaultSearchLabel)">
					<xsl:attribute name="defaultSearchLabel">Publication date</xsl:attribute>
				</xsl:when>
				<xsl:when test="@name='sys_pathname' and not(@defaultSearchLabel)">
					<xsl:attribute name="defaultSearchLabel">Path name</xsl:attribute>
				</xsl:when>
				<xsl:when test="@name='sys_suffix' and not(@defaultSearchLabel)">
					<xsl:attribute name="defaultSearchLabel">Suffix</xsl:attribute>
				</xsl:when>
				<xsl:when test="@name='sys_lang'">
					<xsl:if test="not(@defaultSearchLabel)">
						<xsl:attribute name="defaultSearchLabel">Locale ID</xsl:attribute>
					</xsl:if>
					<xsl:if test="not(@modificationType)">
						<xsl:attribute name="modificationType">userCreate</xsl:attribute>
					</xsl:if>
					<xsl:if test="not(@tokenizeSearchContent)">
						<xsl:attribute name="tokenizeSearchContent">yes</xsl:attribute>
					</xsl:if>
					<xsl:attribute name="systemMandatory">yes</xsl:attribute>
				</xsl:when>
				<xsl:when test="@name='sys_workflowid'">
					<xsl:if test="not(@defaultSearchLabel)">
						<xsl:attribute name="defaultSearchLabel">Workflow</xsl:attribute>
					</xsl:if>
					<xsl:if test="not(@modificationType)">
						<xsl:attribute name="modificationType">userCreate</xsl:attribute>
					</xsl:if>
					<xsl:attribute name="systemMandatory">yes</xsl:attribute>
				</xsl:when>
				<xsl:when test="@name='sys_currentview'">
					<xsl:if test="not(@userSearchable)">
						<xsl:attribute name="userSearchable">no</xsl:attribute>
					</xsl:if>
					<xsl:attribute name="systemMandatory">yes</xsl:attribute>
				</xsl:when>
			</xsl:choose>
         <xsl:if test="@name='sys_title'">
            <xsl:attribute name="systemMandatory">yes</xsl:attribute>
         </xsl:if>
         <xsl:apply-templates mode="copy"/>
			<xsl:if test="@name='sys_communityid' and not(DefaultValue)">
				<DefaultValue>
					<DataLocator>
						<PSXUserContext id="0">
							<name>User/SessionObject/sys_community</name>
						</PSXUserContext>
					</DataLocator>
				</DefaultValue>
			</xsl:if>
			<xsl:if test="@name='sys_contenttypeid'">
				<xsl:if test="not(PSXChoices)">
					<PSXChoices sortOrder="ascending" type="internalLookup">
						<PSXUrlRequest>
							<Href>../sys_psxContentEditorCataloger/ContentTypeLookup.xml</Href>
						</PSXUrlRequest>
						<PSXChoiceFilter>
							<DependentField fieldRef="sys_communityid" dependencyType="required"/>
							<PSXUrlRequest>
								<Href>sys_psxContentEditorCataloger/ContentTypeCommunityLookup.xml</Href>
							</PSXUrlRequest>
						</PSXChoiceFilter>
					</PSXChoices>
				</xsl:if>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXFieldSet" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:if test="not(PSXField[@name='sys_currentview'])">
				<PSXField name="sys_currentview" showInSummary="yes" showInPreview="yes" forceBinary="no">
					<DataLocator>
						<PSXSingleHtmlParameter id="29">
							<name>sys_view</name>
						</PSXSingleHtmlParameter>
					</DataLocator>
					<OccurrenceSettings dimension="optional" multiValuedType="delimited" delimiter=";"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_lang'])">
				<PSXField name="sys_lang" defaultSearchLabel="Locale ID" modificationType="userCreate" showInSummary="yes" showInPreview="yes" forceBinary="no" tokenizeSearchContent="yes">
					<DataLocator>
						<PSXBackEndColumn id="282">
							<tableAlias>CONTENTSTATUS</tableAlias>
							<column>LOCALE</column>
							<columnAlias/>
						</PSXBackEndColumn>
					</DataLocator>
					<DefaultValue>
						<DataLocator>
							<PSXUserContext id="0">
								<name>User/SessionObject/sys_lang</name>
							</PSXUserContext>
						</DataLocator>
					</DefaultValue>
					<OccurrenceSettings dimension="optional" multiValuedType="delimited" delimiter=";"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_assignees'])">
				<PSXField defaultSearchLabel="Assignees" forceBinary="no" modificationType="none" name="sys_assignees" showInPreview="no" showInSummary="no" type="system">
					<DataLocator>
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/workflow/sys_ComputeAssignees</name>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXBackEndColumn id="0">
										<tableAlias>CONTENTSTATUS</tableAlias>
										<column>CONTENTID</column>
										<columnAlias/>
									</PSXBackEndColumn>
								</value>
							</PSXExtensionParamValue>
						</PSXExtensionCall>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_assignmenttypeid'])">
				<PSXField defaultSearchLabel="Assignment type ID" forceBinary="no" modificationType="none" name="sys_assignmenttypeid" showInPreview="no" showInSummary="no" type="system">
					<DataLocator>
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/workflow/sys_ComputeAssignmenType</name>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXBackEndColumn id="0">
										<tableAlias>CONTENTSTATUS</tableAlias>
										<column>CONTENTID</column>
										<columnAlias/>
									</PSXBackEndColumn>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>1</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>2</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>3</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>4</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
						</PSXExtensionCall>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_assignmenttype'])">
				<PSXField defaultSearchLabel="Assignment type" forceBinary="no" modificationType="none" name="sys_assignmenttype" showInPreview="no" showInSummary="no" type="system">
					<DataLocator>
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/workflow/sys_ComputeAssignmenType</name>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXBackEndColumn id="0">
										<tableAlias>CONTENTSTATUS</tableAlias>
										<column>CONTENTID</column>
										<columnAlias/>
									</PSXBackEndColumn>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>None</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>Reader</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>Assignee</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>Admin</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
						</PSXExtensionCall>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_checkoutstatus'])">
				<PSXField defaultSearchLabel="Checkout status" forceBinary="no" modificationType="none" name="sys_checkoutstatus" showInPreview="no" showInSummary="no" type="system">
					<DataLocator>
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/workflow/sys_ComputeUserCheckoutStatus</name>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXBackEndColumn id="0">
										<tableAlias>CONTENTSTATUS</tableAlias>
										<column>CONTENTCHECKOUTUSERNAME</column>
										<columnAlias/>
									</PSXBackEndColumn>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>Nobody</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>SomeOneElse</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>MySelf</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
						</PSXExtensionCall>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_contenttypename'])">
				<PSXField defaultSearchLabel="Content Type Name" forceBinary="no" modificationType="none" name="sys_contenttypename" showInPreview="no" showInSummary="no" type="system" userSearchable="yes">
					<DataLocator>
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/cms/sys_CmsObjectNameLookup</name>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>ContentType</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXBackEndColumn id="0">
										<tableAlias>CONTENTSTATUS</tableAlias>
										<column>CONTENTTYPEID</column>
										<columnAlias/>
									</PSXBackEndColumn>
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
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_statename'])">
				<PSXField defaultSearchLabel="Workflow State Name" forceBinary="no" modificationType="none" name="sys_statename" showInPreview="no" showInSummary="no" type="system" userSearchable="yes">
					<DataLocator>
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/cms/sys_CmsObjectNameLookup</name>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>WorkflowState</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXBackEndColumn id="0">
										<tableAlias>CONTENTSTATUS</tableAlias>
										<column>CONTENTSTATEID</column>
										<columnAlias/>
									</PSXBackEndColumn>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXBackEndColumn id="0">
										<tableAlias>CONTENTSTATUS</tableAlias>
										<column>WORKFLOWAPPID</column>
										<columnAlias/>
									</PSXBackEndColumn>
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
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
					<PSXChoices sortOrder="ascending" type="internalLookup">
						<PSXUrlRequest>
							<Href>../sys_commSupport/communitystatenames.xml</Href>
						</PSXUrlRequest>
						<PSXChoiceFilter>
							<DependentField fieldRef="sys_workflowid" dependencyType="required"/>
							<PSXUrlRequest>
								<Href>sys_commSupport/workflowstatenames.xml</Href>
							</PSXUrlRequest>
						</PSXChoiceFilter>
					</PSXChoices>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_communityname'])">
				<PSXField defaultSearchLabel="Community Name" forceBinary="no" modificationType="none" name="sys_communityname" showInPreview="no" showInSummary="no" type="system" userSearchable="yes">
					<DataLocator>
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/cms/sys_CmsObjectNameLookup</name>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>Community</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXBackEndColumn id="0">
										<tableAlias>CONTENTSTATUS</tableAlias>
										<column>COMMUNITYID</column>
										<columnAlias/>
									</PSXBackEndColumn>
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
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_localename'])">
				<PSXField defaultSearchLabel="Locale Name" forceBinary="no" modificationType="none" name="sys_localename" showInPreview="no" showInSummary="no" type="system" userSearchable="yes">
					<DataLocator>
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/cms/sys_CmsObjectNameLookup</name>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>Locale</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXBackEndColumn id="0">
										<tableAlias>CONTENTSTATUS</tableAlias>
										<column>LOCALE</column>
										<columnAlias/>
									</PSXBackEndColumn>
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
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_workflowname'])">
				<PSXField defaultSearchLabel="Workflow Name" forceBinary="no" modificationType="none" name="sys_workflowname" showInPreview="no" showInSummary="no" type="system" userSearchable="yes">
					<DataLocator>
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/cms/sys_CmsObjectNameLookup</name>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>Workflow</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXBackEndColumn id="0">
										<tableAlias>CONTENTSTATUS</tableAlias>
										<column>WORKFLOWAPPID</column>
										<columnAlias/>
									</PSXBackEndColumn>
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
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_contentid'])">
				<PSXField defaultSearchLabel="Content id" forceBinary="no" modificationType="systemCreate" name="sys_contentid" showInPreview="yes" showInSummary="yes" type="system">
					<DataLocator>
						<PSXBackEndColumn id="1">
							<tableAlias>CONTENTSTATUS</tableAlias>
							<column>CONTENTID</column>
							<columnAlias/>
						</PSXBackEndColumn>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_contentstateid'])">
				<PSXField defaultSearchLabel="Workflow State ID" forceBinary="no" modificationType="system" name="sys_contentstateid" showInPreview="yes" showInSummary="yes" type="system">
					<DataLocator>
						<PSXBackEndColumn id="1">
							<tableAlias>CONTENTSTATUS</tableAlias>
							<column>CONTENTSTATEID</column>
							<columnAlias/>
						</PSXBackEndColumn>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_contentcheckoutusername'])">
				<PSXField defaultSearchLabel="Checked out user name" forceBinary="no" modificationType="system" name="sys_contentcheckoutusername" showInPreview="yes" showInSummary="yes" type="system">
					<DataLocator>
						<PSXBackEndColumn id="1">
							<tableAlias>CONTENTSTATUS</tableAlias>
							<column>CONTENTCHECKOUTUSERNAME</column>
							<columnAlias/>
						</PSXBackEndColumn>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_contentlastmodifier'])">
				<PSXField defaultSearchLabel="Last modified by" forceBinary="no" modificationType="system" name="sys_contentlastmodifier" showInPreview="yes" showInSummary="yes" type="system">
					<DataLocator>
						<PSXBackEndColumn id="1">
							<tableAlias>CONTENTSTATUS</tableAlias>
							<column>CONTENTLASTMODIFIER</column>
							<columnAlias/>
						</PSXBackEndColumn>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_contentlastmodifieddate'])">
				<PSXField defaultSearchLabel="Last modified date" forceBinary="no" modificationType="system" name="sys_contentlastmodifieddate" showInPreview="yes" showInSummary="yes" type="system">
					<DataLocator>
						<PSXBackEndColumn id="1">
							<tableAlias>CONTENTSTATUS</tableAlias>
							<column>CONTENTLASTMODIFIEDDATE</column>
							<columnAlias/>
						</PSXBackEndColumn>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_objecttype'])">
				<PSXField defaultSearchLabel="Object type" forceBinary="no" modificationType="systemCreate" name="sys_objecttype" showInPreview="yes" showInSummary="yes" type="system" userSearchable="no">
					<DataLocator>
						<PSXBackEndColumn id="1">
							<tableAlias>CONTENTSTATUS</tableAlias>
							<column>OBJECTTYPE</column>
							<columnAlias/>
						</PSXBackEndColumn>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_contenttypeid'])">
				<PSXField defaultSearchLabel="Content Type" forceBinary="no" modificationType="systemCreate" name="sys_contenttypeid" showInPreview="yes" showInSummary="yes" type="system">
					<DataLocator>
						<PSXBackEndColumn id="1">
							<tableAlias>CONTENTSTATUS</tableAlias>
							<column>CONTENTTYPEID</column>
							<columnAlias/>
						</PSXBackEndColumn>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
					<PSXChoices sortOrder="ascending" type="internalLookup">
						<PSXUrlRequest>
							<Href>../sys_psxContentEditorCataloger/ContentTypeLookup.xml</Href>
						</PSXUrlRequest>
						<PSXChoiceFilter>
							<DependentField fieldRef="sys_communityid" dependencyType="required"/>
							<PSXUrlRequest>
								<Href>sys_psxContentEditorCataloger/ContentTypeCommunityLookup.xml</Href>
							</PSXUrlRequest>
						</PSXChoiceFilter>
					</PSXChoices>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_contentcreateddate'])">
				<PSXField defaultSearchLabel="Created on" forceBinary="no" modificationType="systemCreate" name="sys_contentcreateddate" showInPreview="yes" showInSummary="yes" type="system">
					<DataLocator>
						<PSXBackEndColumn id="1">
							<tableAlias>CONTENTSTATUS</tableAlias>
							<column>CONTENTCREATEDDATE</column>
							<columnAlias/>
						</PSXBackEndColumn>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_contentcreatedby'])">
				<PSXField defaultSearchLabel="Created by" forceBinary="no" modificationType="systemCreate" name="sys_contentcreatedby" showInPreview="yes" showInSummary="yes" type="system">
					<DataLocator>
						<PSXBackEndColumn id="1">
							<tableAlias>CONTENTSTATUS</tableAlias>
							<column>CONTENTCREATEDBY</column>
							<columnAlias/>
						</PSXBackEndColumn>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_publishabletype'])">
				<PSXField defaultSearchLabel="Publishable status" forceBinary="no" modificationType="none" name="sys_publishabletype" showInPreview="no" showInSummary="no" type="system">
					<DataLocator>
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/cms/sys_CmsObjectNameLookup</name>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>ContentValid</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXBackEndColumn id="0">
										<tableAlias>CONTENTSTATUS</tableAlias>
										<column>WORKFLOWAPPID</column>
										<columnAlias/>
									</PSXBackEndColumn>
								</value>
							</PSXExtensionParamValue>
							<PSXExtensionParamValue id="1">
								<value>
									<PSXBackEndColumn id="0">
										<tableAlias>CONTENTSTATUS</tableAlias>
										<column>CONTENTSTATEID</column>
										<columnAlias/>
									</PSXBackEndColumn>
								</value>
							</PSXExtensionParamValue>
						</PSXExtensionCall>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_variantname'])">
				<PSXField defaultSearchLabel="Variant Name" forceBinary="no" modificationType="none" name="sys_variantname" showInPreview="no" showInSummary="no" type="system" userSearchable="yes">
					<DataLocator>
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/generic/sys_MakeIntLink</name>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text>../sys_psxCms/Variant.xml</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
						</PSXExtensionCall>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_relevancy'])">
				<xsl:variable name="FTSEnabled" select="document('../rxconfig/Server/config.xml')//PSXServerConfiguration/PSXSearchConfig/@fullTextSearchEnabled"/>
				<xsl:if test="$FTSEnabled='yes'">
					<PSXField defaultSearchLabel="Rank" forceBinary="no" modificationType="none" name="sys_relevancy" showInPreview="no" showInSummary="no" type="system" userSearchable="yes">
						<DataLocator>
							<PSXExtensionCall id="0">
								<name>Java/global/percussion/generic/sys_Literal</name>
								<PSXExtensionParamValue id="0">
									<value>
										<PSXTextLiteral id="0">
											<text>-1</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
							</PSXExtensionCall>
						</DataLocator>
						<DataType>integer</DataType>
						<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
					</PSXField>
				</xsl:if>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_variantid'])">
				<PSXField defaultSearchLabel="Variant" forceBinary="no" modificationType="none" name="sys_variantid" showInPreview="no" showInSummary="no" type="system" userSearchable="yes">
					<DataLocator>
						<PSXTextLiteral id="0">
							<text>sys_variantid</text>
						</PSXTextLiteral>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_siteid'])">
				<PSXField defaultSearchLabel="Site" forceBinary="no" modificationType="none" name="sys_siteid" showInPreview="no" showInSummary="no" type="system" userSearchable="yes">
					<DataLocator>
						<PSXTextLiteral id="0">
							<text>sys_siteid</text>
						</PSXTextLiteral>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_folderid'])">
				<PSXField defaultSearchLabel="Folder Path" forceBinary="no" modificationType="none" name="sys_folderid" showInPreview="no" showInSummary="no" type="system" userSearchable="yes">
					<DataLocator>
						<PSXTextLiteral id="0">
							<text>sys_folderid</text>
						</PSXTextLiteral>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_thumbnail'])">
				<PSXField defaultSearchLabel="Thumbnail" forceBinary="no" modificationType="none" name="sys_thumbnail" showInPreview="no" showInSummary="no" systemMandatory="no" type="system" userCustomizable="yes" userSearchable="yes">
					<DataLocator>
						<PSXExtensionCall id="0">
							<name>Java/global/percussion/generic/sys_Literal</name>
							<PSXExtensionParamValue id="0">
								<value>
									<PSXTextLiteral id="0">
										<text/>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
						</PSXExtensionCall>
					</DataLocator>
					<DataType>image</DataType>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
			<xsl:if test="not(PSXField[@name='sys_hibernateVersion'])">
				<PSXField forceBinary="no" modificationType="system" name="sys_hibernateVersion" showInPreview="no" showInSummary="no" systemMandatory="yes"  userSearchable="no" systemInternal="yes">
					<DataLocator>
						<PSXBackEndColumn id="1">
							<tableAlias>CONTENTSTATUS</tableAlias>
							<column>HIB_VER</column>
							<columnAlias/>
						</PSXBackEndColumn>
					</DataLocator>
					<OccurrenceSettings delimiter=";" dimension="optional" multiValuedType="delimited"/>
				</PSXField>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<!-- Remove sys_view PSXField element from PSXFieldSet element if exists -->
	<xsl:template match="PSXFieldSet/PSXField[@name='sys_view']" mode="copy"/>
	<!-- Add PSXDisplayMapping/sys_communityid if does not exist to <PSXDisplayMapper fieldSetRef='systemFieldset'> element-->
	<xsl:template match="PSXDisplayMapper[@fieldSetRef='systemFieldset']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:if test="not(PSXDisplayMapping/FieldRef='sys_communityid')">
				<PSXDisplayMapping>
					<FieldRef>sys_communityid</FieldRef>
					<PSXUISet name="" defaultSet="">
						<Label>
							<PSXDisplayText/>
						</Label>
						<PSXControlRef name="sys_HiddenInput"/>
					</PSXUISet>
				</PSXDisplayMapping>
			</xsl:if>
			<xsl:if test="not(PSXDisplayMapping/FieldRef='sys_reminderdate')">
				<PSXDisplayMapping>
					<FieldRef>sys_reminderdate</FieldRef>
					<PSXUISet>
						<Label>
							<PSXDisplayText>Reminder Date:</PSXDisplayText>
						</Label>
						<PSXControlRef id="0" name="sys_CalendarSimple"/>
					</PSXUISet>
				</PSXDisplayMapping>
			</xsl:if>
			<xsl:if test="not(PSXDisplayMapping/FieldRef='sys_currentview')">
				<PSXDisplayMapping>
					<FieldRef>sys_currentview</FieldRef>
					<PSXUISet name="" defaultSet="">
						<Label>
							<PSXDisplayText/>
						</Label>
						<PSXControlRef name="sys_HiddenInput"/>
					</PSXUISet>
				</PSXDisplayMapping>
			</xsl:if>
			<xsl:if test="not(PSXDisplayMapping/FieldRef='sys_lang')">
				<PSXDisplayMapping>
					<FieldRef>sys_lang</FieldRef>
					<PSXUISet name="" defaultSet="">
						<Label>
							<PSXDisplayText>Locale:</PSXDisplayText>
						</Label>
						<PSXControlRef name="sys_DropDownSingle"/>
						<PSXChoices sortOrder="ascending" type="internalLookup">
							<PSXUrlRequest>
								<Href>../sys_i18nSupport/languagelookup.xml</Href>
							</PSXUrlRequest>
						</PSXChoices>
						<ReadOnlyRules>
							<PSXRule boolean="and">
								<PSXConditional id="0">
									<variable>
										<PSXSingleHtmlParameter id="0">
											<name>sys_contentid</name>
										</PSXSingleHtmlParameter>
									</variable>
									<operator>IS NOT NULL</operator>
									<value>
										<PSXTextLiteral id="0">
											<text/>
										</PSXTextLiteral>
									</value>
									<boolean>AND</boolean>
								</PSXConditional>
							</PSXRule>
						</ReadOnlyRules>
					</PSXUISet>
				</PSXDisplayMapping>
			</xsl:if>
			<xsl:if test="not(PSXDisplayMapping/FieldRef='sys_hibernateVersion')">
				<PSXDisplayMapping>
					<FieldRef>sys_hibernateVersion</FieldRef>
					<PSXUISet defaultSet="" name="">
						<Label>
							<PSXDisplayText/>
						</Label>
						<PSXControlRef name="sys_HiddenInput"/>
					</PSXUISet>
				</PSXDisplayMapping>
			</xsl:if>
			<xsl:if test="not(PSXDisplayMapping/FieldRef='sys_pubdate')">
				<PSXDisplayMapping>
					<FieldRef>sys_pubdate</FieldRef>
					<PSXUISet defaultSet="" name="">
						<Label>
							<PSXDisplayText>Pub Date:</PSXDisplayText>
						</Label>
						<PSXControlRef name="sys_CalendarSimple"/>
					</PSXUISet>
				</PSXDisplayMapping>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<!-- Remove sys_view FieldRef element from PSXDisplayMapping element if exists -->
	<xsl:template match="PSXDisplayMapper[@fieldSetRef='systemFieldset']/PSXDisplayMapping[FieldRef='sys_view']" mode="copy"/>
	<!-- Add sys_commAuthenticateUser exit to InputDataExits of CommandHandlerExits with commandName=edit -->
	<xsl:template match="CommandHandlerExits[@commandName='edit']/InputDataExits/PSXExtensionCallSet" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:if test="not(PSXExtensionCall[@id='284'])">
				<PSXExtensionCall id="284">
					<name>Java/global/percussion/communities/sys_commAuthenticateUser</name>
				</PSXExtensionCall>
			</xsl:if>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!-- Add sys_commAuthenticateUser exit to InputDataExits of CommandHandlerExits with commandName=modify -->
	<xsl:template match="CommandHandlerExits[@commandName='modify']/InputDataExits/PSXExtensionCallSet" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:if test="not(PSXExtensionCall[@id='285'])">
				<PSXExtensionCall id="285">
					<name>Java/global/percussion/communities/sys_commAuthenticateUser</name>
				</PSXExtensionCall>
			</xsl:if>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!-- Add sys_casInsertAsRelatedItem exit to ResultDataExits of CommandHandlerExits with commandName=modify -->
	<xsl:template match="CommandHandlerExits[@commandName='modify']/ResultDataExits/PSXExtensionCallSet" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:if test="not(PSXExtensionCall/name='Java/global/percussion/assemblers/sys_casInsertAsRelatedItem')">
				<PSXExtensionCall id="285">
					<name>Java/global/percussion/assemblers/sys_casInsertAsRelatedItem</name>
				</PSXExtensionCall>
			</xsl:if>
			<xsl:if test="not(PSXExtensionCall/name='Java/global/percussion/cx/sys_addNewItemToFolder')">
				<PSXExtensionCall id="0">
					<name>Java/global/percussion/cx/sys_addNewItemToFolder</name>
				</PSXExtensionCall>
			</xsl:if>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!-- Add sys_commAuthenticateUser exit to InputDataExits of CommandHandlerExits with commandName=workflow -->
	<xsl:template match="CommandHandlerExits[@commandName='workflow']/InputDataExits/PSXExtensionCallSet" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:if test="not(PSXExtensionCall[@id='287'])">
				<PSXExtensionCall id="287">
					<name>Java/global/percussion/communities/sys_commAuthenticateUser</name>
				</PSXExtensionCall>
			</xsl:if>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXCommandHandlerStylesheets/CommandHandler[@name='edit']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:if test="not(PSXConditionalStylesheet/PSXStylesheet/PSXUrlRequest/Href='file:../sys_resources/stylesheets/activeEdit.xsl')">
				<PSXConditionalStylesheet>
					<PSXStylesheet>
						<PSXUrlRequest>
							<Href>file:../sys_resources/stylesheets/activeEdit.xsl</Href>
						</PSXUrlRequest>
					</PSXStylesheet>
					<Conditions>
						<PSXRule>
							<PSXConditional id="28">
								<variable>
									<PSXSingleHtmlParameter id="29">
										<name>sys_view</name>
									</PSXSingleHtmlParameter>
								</variable>
								<operator>=</operator>
								<value>
									<PSXTextLiteral id="30">
										<text>sys_All</text>
									</PSXTextLiteral>
								</value>
								<boolean>OR</boolean>
							</PSXConditional>
							<PSXConditional id="28">
								<variable>
									<PSXSingleHtmlParameter id="29">
										<name>sys_view</name>
									</PSXSingleHtmlParameter>
								</variable>
								<operator>=</operator>
								<value>
									<PSXTextLiteral id="30">
										<text>sys_Content</text>
									</PSXTextLiteral>
								</value>
								<boolean>OR</boolean>
							</PSXConditional>
							<PSXConditional id="28">
								<variable>
									<PSXSingleHtmlParameter id="29">
										<name>sys_view</name>
									</PSXSingleHtmlParameter>
								</variable>
								<operator>=</operator>
								<value>
									<PSXTextLiteral id="30">
										<text>sys_ItemMeta</text>
									</PSXTextLiteral>
								</value>
								<boolean>OR</boolean>
							</PSXConditional>
						</PSXRule>
					</Conditions>
				</PSXConditionalStylesheet>
			</xsl:if>
			<xsl:if test="not(PSXConditionalStylesheet/PSXStylesheet/PSXUrlRequest/Href='file:../sys_resources/stylesheets/singleFieldEdit.xsl')">
				<PSXConditionalStylesheet>
					<PSXStylesheet>
						<PSXUrlRequest>
							<Href>file:../sys_resources/stylesheets/singleFieldEdit.xsl</Href>
						</PSXUrlRequest>
					</PSXStylesheet>
					<Conditions>
						<PSXRule>
							<PSXConditional id="28">
								<variable>
									<PSXSingleHtmlParameter id="29">
										<name>sys_view</name>
									</PSXSingleHtmlParameter>
								</variable>
								<operator>LIKE</operator>
								<value>
									<PSXTextLiteral id="30">
										<text>sys_SingleField:%</text>
									</PSXTextLiteral>
								</value>
							</PSXConditional>
						</PSXRule>
					</Conditions>
				</PSXConditionalStylesheet>
			</xsl:if>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXCommandHandlerStylesheets/CommandHandler[@name='preview']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:if test="not(PSXConditionalStylesheet/PSXStylesheet/PSXUrlRequest/Href='file:../sys_resources/stylesheets/activeEdit.xsl')">
				<PSXConditionalStylesheet>
					<PSXStylesheet>
						<PSXUrlRequest>
							<Href>file:../sys_resources/stylesheets/activeEdit.xsl</Href>
						</PSXUrlRequest>
					</PSXStylesheet>
					<Conditions>
						<PSXRule>
							<PSXConditional id="28">
								<variable>
									<PSXSingleHtmlParameter id="29">
										<name>sys_view</name>
									</PSXSingleHtmlParameter>
								</variable>
								<operator>=</operator>
								<value>
									<PSXTextLiteral id="30">
										<text>sys_All</text>
									</PSXTextLiteral>
								</value>
								<boolean>OR</boolean>
							</PSXConditional>
							<PSXConditional id="28">
								<variable>
									<PSXSingleHtmlParameter id="29">
										<name>sys_view</name>
									</PSXSingleHtmlParameter>
								</variable>
								<operator>=</operator>
								<value>
									<PSXTextLiteral id="30">
										<text>sys_Content</text>
									</PSXTextLiteral>
								</value>
								<boolean>OR</boolean>
							</PSXConditional>
							<PSXConditional id="28">
								<variable>
									<PSXSingleHtmlParameter id="29">
										<name>sys_view</name>
									</PSXSingleHtmlParameter>
								</variable>
								<operator>=</operator>
								<value>
									<PSXTextLiteral id="30">
										<text>sys_ItemMeta</text>
									</PSXTextLiteral>
								</value>
								<boolean>OR</boolean>
							</PSXConditional>
						</PSXRule>
					</Conditions>
				</PSXConditionalStylesheet>
			</xsl:if>
			<xsl:if test="not(PSXConditionalStylesheet/PSXStylesheet/PSXUrlRequest/Href='file:../sys_resources/stylesheets/revisionEdit.xsl')">
				<PSXConditionalStylesheet>
					<PSXStylesheet>
						<PSXUrlRequest>
							<Href>file:../sys_resources/stylesheets/revisionEdit.xsl</Href>
						</PSXUrlRequest>
					</PSXStylesheet>
					<Conditions>
						<PSXRule>
							<PSXConditional id="28">
								<variable>
									<PSXSingleHtmlParameter id="29">
										<name>sys_userview</name>
									</PSXSingleHtmlParameter>
								</variable>
								<operator>=</operator>
								<value>
									<PSXTextLiteral id="30">
										<text>sys_Revisions</text>
									</PSXTextLiteral>
								</value>
							</PSXConditional>
						</PSXRule>
					</Conditions>
				</PSXConditionalStylesheet>
			</xsl:if>
			<xsl:if test="not(PSXConditionalStylesheet/PSXStylesheet/PSXUrlRequest/Href='file:../sys_resources/stylesheets/auditTrail.xsl')">
				<PSXConditionalStylesheet>
					<PSXStylesheet>
						<PSXUrlRequest>
							<Href>file:../sys_resources/stylesheets/auditTrail.xsl</Href>
						</PSXUrlRequest>
					</PSXStylesheet>
					<Conditions>
						<PSXRule>
							<PSXConditional id="28">
								<variable>
									<PSXSingleHtmlParameter id="29">
										<name>sys_userview</name>
									</PSXSingleHtmlParameter>
								</variable>
								<operator>=</operator>
								<value>
									<PSXTextLiteral id="30">
										<text>sys_audittrail</text>
									</PSXTextLiteral>
								</value>
							</PSXConditional>
						</PSXRule>
					</Conditions>
				</PSXConditionalStylesheet>
			</xsl:if>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXApplicationFlow/CommandHandler[not(@name='relate')]/PSXConditionalRequest/PSXUrlRequest/PSXExtensionCall | PSXApplicationFlow/CommandHandler/PSXUrlRequest/PSXExtensionCall" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:for-each select="PSXExtensionParamValue | name">
				<xsl:if test="position()&lt;=2">
					<xsl:apply-templates select="." mode="copy"/>
				</xsl:if>
			</xsl:for-each>
			<xsl:variable name="sysviewcheck">
				<xsl:for-each select="PSXExtensionParamValue">
					<xsl:if test="./value/PSXTextLiteral/text='sys_view'">found</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<xsl:if test="$sysviewcheck!='found'">
				<PSXExtensionParamValue id="8">
					<value>
						<PSXTextLiteral id="9">
							<text>sys_view</text>
						</PSXTextLiteral>
					</value>
				</PSXExtensionParamValue>
				<PSXExtensionParamValue id="10">
					<value>
						<PSXSingleHtmlParameter id="11">
							<name>sys_view</name>
						</PSXSingleHtmlParameter>
					</value>
				</PSXExtensionParamValue>
			</xsl:if>
			<xsl:for-each select="PSXExtensionParamValue | name">
				<xsl:if test="position()&gt;2">
					<xsl:apply-templates select="." mode="copy"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXApplicationFlow/CommandHandler[(@name='relate')]/PSXConditionalRequest[position()=1]/Conditions" mode="copy">
		<Conditions>
			<PSXRule boolean="and">
				<PSXConditional id="28">
					<variable>
						<PSXSingleHtmlParameter id="29">
							<name>sys_relationshiptype</name>
						</PSXSingleHtmlParameter>
					</variable>
					<operator>=</operator>
					<value>
						<PSXTextLiteral id="30">
							<text>NewCopy</text>
						</PSXTextLiteral>
					</value>
					<boolean>OR</boolean>
				</PSXConditional>
				<PSXConditional id="29">
					<variable>
						<PSXSingleHtmlParameter id="30">
							<name>sys_relationshiptype</name>
						</PSXSingleHtmlParameter>
					</variable>
					<operator>=</operator>
					<value>
						<PSXTextLiteral id="31">
							<text>PromotableVersion</text>
						</PSXTextLiteral>
					</value>
				</PSXConditional>
			</PSXRule>
		</Conditions>
	</xsl:template>
	<xsl:template match="PSXApplicationFlow/CommandHandler[(@name='relate')]/PSXConditionalRequest/PSXUrlRequest/PSXExtensionCall" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:for-each select="PSXExtensionParamValue | name">
				<xsl:if test="position()&lt;=2">
					<xsl:apply-templates select="." mode="copy"/>
				</xsl:if>
			</xsl:for-each>
			<xsl:variable name="refreshhintcheck">
				<xsl:for-each select="PSXExtensionParamValue">
					<xsl:if test="./value/PSXTextLiteral/text='refreshHint'">found</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<xsl:if test="$refreshhintcheck!='found'">
				<PSXExtensionParamValue id="156">
					<value>
						<PSXTextLiteral id="157">
							<text>refreshHint</text>
						</PSXTextLiteral>
					</value>
				</PSXExtensionParamValue>
				<PSXExtensionParamValue id="158">
					<value>
						<PSXSingleHtmlParameter id="159">
							<name>refreshHint</name>
						</PSXSingleHtmlParameter>
					</value>
				</PSXExtensionParamValue>
			</xsl:if>
			<xsl:for-each select="PSXExtensionParamValue | name">
				<xsl:if test="position()&gt;2">
					<xsl:apply-templates select="." mode="copy"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXApplicationFlow" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:if test="not(CommandHandler[@name='relate'])">
				<CommandHandler name="relate">
					<PSXConditionalRequest>
						<PSXUrlRequest>
							<PSXExtensionCall id="145">
								<name>Java/global/percussion/generic/sys_MakeAbsLink</name>
								<PSXExtensionParamValue id="119">
									<value>
										<PSXTextLiteral id="120">
											<text>../sys_action/checkoutedit.xml</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="148">
									<value>
										<PSXTextLiteral id="149">
											<text>sys_command</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="150">
									<value>
										<PSXTextLiteral id="151">
											<text>edit</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="8">
									<value>
										<PSXTextLiteral id="9">
											<text>sys_view</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="10">
									<value>
										<PSXTextLiteral id="9">
											<text>sys_All</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="152">
									<value>
										<PSXTextLiteral id="153">
											<text>sys_contentid</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="154">
									<value>
										<PSXSingleHtmlParameter id="155">
											<name>sys_contentid</name>
										</PSXSingleHtmlParameter>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="156">
									<value>
										<PSXTextLiteral id="157">
											<text>sys_revision</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="158">
									<value>
										<PSXSingleHtmlParameter id="159">
											<name>sys_revision</name>
										</PSXSingleHtmlParameter>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="156">
									<value>
										<PSXTextLiteral id="157">
											<text>sys_contenttypeid</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="158">
									<value>
										<PSXSingleHtmlParameter id="159">
											<name>sys_contenttypeid</name>
										</PSXSingleHtmlParameter>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="156">
									<value>
										<PSXTextLiteral id="157">
											<text>refreshHint</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="158">
									<value>
										<PSXSingleHtmlParameter id="159">
											<name>refreshHint</name>
										</PSXSingleHtmlParameter>
									</value>
								</PSXExtensionParamValue>
							</PSXExtensionCall>
						</PSXUrlRequest>
						<Conditions>
							<PSXRule boolean="and">
								<PSXConditional id="28">
									<variable>
										<PSXSingleHtmlParameter id="29">
											<name>sys_relationshiptype</name>
										</PSXSingleHtmlParameter>
									</variable>
									<operator>=</operator>
									<value>
										<PSXTextLiteral id="30">
											<text>NewCopy</text>
										</PSXTextLiteral>
									</value>
									<boolean>OR</boolean>
								</PSXConditional>
								<PSXConditional id="29">
									<variable>
										<PSXSingleHtmlParameter id="30">
											<name>sys_relationshiptype</name>
										</PSXSingleHtmlParameter>
									</variable>
									<operator>=</operator>
									<value>
										<PSXTextLiteral id="31">
											<text>PromotableVersion</text>
										</PSXTextLiteral>
									</value>
								</PSXConditional>
							</PSXRule>
						</Conditions>
					</PSXConditionalRequest>
					<PSXConditionalRequest>
						<PSXUrlRequest>
							<PSXExtensionCall id="145">
								<name>Java/global/percussion/generic/sys_MakeAbsLink</name>
								<PSXExtensionParamValue id="119">
									<value>
										<PSXTextLiteral id="120">
											<text>../sys_psxRelationshipSupport/translationresult.html</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="152">
									<value>
										<PSXTextLiteral id="153">
											<text>sys_contentid</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="154">
									<value>
										<PSXSingleHtmlParameter id="155">
											<name>sys_contentid</name>
										</PSXSingleHtmlParameter>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="156">
									<value>
										<PSXTextLiteral id="157">
											<text>sys_revision</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="158">
									<value>
										<PSXSingleHtmlParameter id="159">
											<name>sys_revision</name>
										</PSXSingleHtmlParameter>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="156">
									<value>
										<PSXTextLiteral id="157">
											<text>refreshHint</text>
										</PSXTextLiteral>
									</value>
								</PSXExtensionParamValue>
								<PSXExtensionParamValue id="158">
									<value>
										<PSXSingleHtmlParameter id="159">
											<name>refreshHint</name>
										</PSXSingleHtmlParameter>
									</value>
								</PSXExtensionParamValue>
							</PSXExtensionCall>
						</PSXUrlRequest>
						<Conditions>
							<PSXRule boolean="and">
								<PSXConditional id="28">
									<variable>
										<PSXSingleHtmlParameter id="29">
											<name>sys_relationshiptype</name>
										</PSXSingleHtmlParameter>
									</variable>
									<operator>=</operator>
									<value>
										<PSXTextLiteral id="30">
											<text>Translation</text>
										</PSXTextLiteral>
									</value>
									<boolean>AND</boolean>
								</PSXConditional>
							</PSXRule>
						</Conditions>
					</PSXConditionalRequest>
					<PSXUrlRequest>
						<PSXExtensionCall id="118">
							<name>Java/global/percussion/generic/sys_MakeAbsLink</name>
							<PSXExtensionParamValue id="119">
								<value>
									<PSXTextLiteral id="120">
										<text>../sys_cx/mainpage.html</text>
									</PSXTextLiteral>
								</value>
							</PSXExtensionParamValue>
						</PSXExtensionCall>
					</PSXUrlRequest>
				</CommandHandler>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXField[@name='sys_contenttypeid']/PSXChoices" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:if test="not(PSXChoiceFilter)">
				<PSXChoiceFilter>
					<DependentField fieldRef="sys_communityid" dependencyType="required"/>
					<PSXUrlRequest>
						<Href>sys_psxContentEditorCataloger/ContentTypeCommunityLookup.xml</Href>
					</PSXUrlRequest>
				</PSXChoiceFilter>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<!-- change the display name -->
	<xsl:template match="PSXField[@name='sys_variantname' and @defaultSearchLabel!='Variant Name']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:attribute name="defaultSearchLabel">Variant Name</xsl:attribute>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXField[@name='sys_statename']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:if test="not(PSXChoices)">
				<PSXChoices sortOrder="ascending" type="internalLookup">
					<PSXUrlRequest>
						<Href>../sys_commSupport/communitystatenames.xml</Href>
					</PSXUrlRequest>
					<PSXChoiceFilter>
						<DependentField fieldRef="sys_workflowid" dependencyType="required"/>
						<PSXUrlRequest>
							<Href>sys_commSupport/workflowstatenames.xml</Href>
						</PSXUrlRequest>
					</PSXChoiceFilter>
				</PSXChoices>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXField[@name='sys_checkoutstatus' and DataLocator/PSXExtensionCall/name='Java/global/percussion/workflow/sys_ComputeCheckoutStatus']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<DataLocator>
				<PSXExtensionCall id="0">
					<name>Java/global/percussion/workflow/sys_ComputeUserCheckoutStatus</name>
					<PSXExtensionParamValue id="0">
						<value>
							<PSXBackEndColumn id="0">
								<tableAlias>CONTENTSTATUS</tableAlias>
								<column>CONTENTCHECKOUTUSERNAME</column>
								<columnAlias/>
							</PSXBackEndColumn>
						</value>
					</PSXExtensionParamValue>
					<xsl:for-each select="DataLocator/PSXExtensionCall/PSXExtensionParamValue[position()&gt;1]">
						<xsl:apply-templates select="." mode="copy"/>
					</xsl:for-each>
				</PSXExtensionCall>
			</DataLocator>
			<xsl:apply-templates select="*[name()!='DataLocator']" mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXDisplayMapping[FieldRef='sys_workflowid']/PSXUISet/PSXChoices/PSXChoiceFilter" mode="copy">
		<PSXChoiceFilter>
			<DependentField fieldRef="sys_communityid" dependencyType="required"/>
			<PSXUrlRequest>
				<Href>sys_commSupport/communityworkflowchoices.xml</Href>
			</PSXUrlRequest>
		</PSXChoiceFilter>
	</xsl:template>
</xsl:stylesheet>
