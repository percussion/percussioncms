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
	<xsl:variable name="datasource" select="//datasource"/>
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
	<!-- Template to add rxs_pubAppendPurgedOrMovedItems post exit to "unpub_clist" resource.-->
	<xsl:template match="PSXDataSet[name='unpub_clist']/PSXQueryPipe/ResultDataExits/PSXExtensionCallSet" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:if test="not(PSXExtensionCall[name='Java/global/percussion/fastforward/sfp/rxs_pubAppendPurgedOrMovedItems'])">
				<PSXExtensionCall id="0">
					<name>Java/global/percussion/fastforward/sfp/rxs_pubAppendPurgedOrMovedItems</name>
				</PSXExtensionCall>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<!-- Template to convert CONTENTVALID condition from equal to 'n' to IN ('u','n')-->
	<xsl:template match="PSXDataSet[name='unpubA_clist' or name='unpubB_clist']/PSXQueryPipe/PSXDataSelector/WhereClauses/PSXWhereClause[PSXConditional/variable/PSXBackEndColumn[tableAlias='STATES' and column='CONTENTVALID']]" mode="copy">
		<PSXWhereClause id="0" omitWhenNull="no">
			<PSXConditional id="0">
				<variable>
					<PSXBackEndColumn id="0">
						<tableAlias>STATES</tableAlias>
						<column>CONTENTVALID</column>
						<columnAlias/>
					</PSXBackEndColumn>
				</variable>
				<operator>IN</operator>
				<value>
					<PSXTextLiteral id="0">
						<text>('u','n')</text>
					</PSXTextLiteral>
				</value>
				<boolean>AND</boolean>
			</PSXConditional>
		</PSXWhereClause>
	</xsl:template>
	<!--Template to add parameters to rxs_SiteFolderContentListBulkBuilder exit.-->
	<xsl:template match="PSXExtensionCall[name='Java/global/percussion/fastforward/sfp/rxs_SiteFolderContentListBulkBuilder']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:if test="not(PSXExtensionParamValue[value/PSXSingleHtmlParameter/name='sys_protocol'])">
				<PSXExtensionParamValue id="0">
					<value>
						<PSXSingleHtmlParameter id="0">
							<name>sys_protocol</name>
						</PSXSingleHtmlParameter>
					</value>
				</PSXExtensionParamValue>
			</xsl:if>
			<xsl:if test="not(PSXExtensionParamValue[value/PSXSingleHtmlParameter/name='sys_host'])">
				<PSXExtensionParamValue id="0">
					<value>
						<PSXSingleHtmlParameter id="0">
							<name>sys_host</name>
						</PSXSingleHtmlParameter>
					</value>
				</PSXExtensionParamValue>
			</xsl:if>
			<xsl:if test="not(PSXExtensionParamValue[value/PSXSingleHtmlParameter/name='sys_port'])">
				<PSXExtensionParamValue id="0">
					<value>
						<PSXSingleHtmlParameter id="0">
							<name>sys_port</name>
						</PSXSingleHtmlParameter>
					</value>
				</PSXExtensionParamValue>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<!-- Template to add RXSITEITEMS.PUBOPERATION = 'publish' and RXSITEITEMS.PUBSTATUS='success' 
		where clause conditions to "siteitem_clist" and "buildUnpub_clist" resources. If those conditions does not exist.
	-->
	<xsl:template match="PSXDataSet[name='siteitem_clist' or name='buildUnpub_clist']/PSXQueryPipe/PSXDataSelector/WhereClauses" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:if test="not(PSXWhereClause[PSXConditional/variable/PSXBackEndColumn[tableAlias='RXSITEITEMS' and column='PUBOPERATION']])">
				<PSXWhereClause id="0" omitWhenNull="no">
					<PSXConditional id="0">
						<variable>
							<PSXBackEndColumn id="0">
								<tableAlias>RXSITEITEMS</tableAlias>
								<column>PUBOPERATION</column>
								<columnAlias/>
							</PSXBackEndColumn>
						</variable>
						<operator>=</operator>
						<value>
							<PSXTextLiteral id="0">
								<text>publish</text>
							</PSXTextLiteral>
						</value>
						<boolean>AND</boolean>
					</PSXConditional>
				</PSXWhereClause>
			</xsl:if>
			<xsl:if test="not(PSXWhereClause[PSXConditional/variable/PSXBackEndColumn[tableAlias='RXSITEITEMS' and column='PUBSTATUS']])">
				<PSXWhereClause id="0" omitWhenNull="no">
					<PSXConditional id="0">
						<variable>
							<PSXBackEndColumn id="0">
								<tableAlias>RXSITEITEMS</tableAlias>
								<column>PUBSTATUS</column>
								<columnAlias/>
							</PSXBackEndColumn>
						</variable>
						<operator>=</operator>
						<value>
							<PSXTextLiteral id="0">
								<text>success</text>
							</PSXTextLiteral>
						</value>
						<boolean>AND</boolean>
					</PSXConditional>
				</PSXWhereClause>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<!-- Template to add buildUnpub_clist and siteitem_clist datasets to the application. This is basically for 5.5 upgrades.-->
	<xsl:template match="PSXApplication" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates select="node()[not(name()='userProperty' or name()='PSXLogger' or name()='PSXTraceInfo' or name()='PSXErrorWebPages' or name()='backEndLoginPassthru'  or name()='PSXNotifier')]" mode="copy"/>
			<xsl:if test="not(PSXDataSet[name='buildUnpub_clist'])">
				<PSXDataSet id="1216">
					<name>buildUnpub_clist</name>
					<description>This resource builds unpublish content list based on the list of content ids submitted as sys_contentid parameter, sys_siteid and sys_context. An outer join is made bwteen RXSIEITEMS and CONTENTSTATUS table to add info that is not available in RXSITEITEMS table. For items that are purged from the system, this information will be missing.</description>
					<transactionType>none</transactionType>
					<PSXQueryPipe id="1174">
						<name>QueryPipe</name>
						<description/>
						<PSXBackEndDataTank id="1170">
							<PSXBackEndTable id="1168">
								<alias>RXSITEITEMS</alias>
								<datasource>
									<xsl:value-of select="$datasource"/>
								</datasource>
								<table>RXSITEITEMS</table>
							</PSXBackEndTable>
							<PSXBackEndTable id="1169">
								<alias>CONTENTSTATUS</alias>
								<datasource>
									<xsl:value-of select="$datasource"/>
								</datasource>
								<table>CONTENTSTATUS</table>
							</PSXBackEndTable>
							<PSXBackEndJoin id="0" joinType="leftOuter">
								<leftColumn>
									<PSXBackEndColumn id="0">
										<tableAlias>RXSITEITEMS</tableAlias>
										<column>CONTENTID</column>
										<columnAlias/>
									</PSXBackEndColumn>
								</leftColumn>
								<rightColumn>
									<PSXBackEndColumn id="0">
										<tableAlias>CONTENTSTATUS</tableAlias>
										<column>CONTENTID</column>
										<columnAlias/>
									</PSXBackEndColumn>
								</rightColumn>
							</PSXBackEndJoin>
						</PSXBackEndDataTank>
						<PSXDataMapper id="1172" returnEmptyXml="yes">
							<PSXDataMapping groupId="1" id="0">
								<PSXXmlField id="0">
									<name>contentlist/contentitem/contenturl</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>RXSITEITEMS</tableAlias>
									<column>CONTENTURL</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="1" id="0">
								<PSXXmlField id="0">
									<name>contentlist/contentitem/delivery/location</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>RXSITEITEMS</tableAlias>
									<column>LOCATION</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="1" id="0">
								<PSXXmlField id="0">
									<name>contentlist/@context</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>RXSITEITEMS</tableAlias>
									<column>CONTEXT</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="1" id="0">
								<PSXXmlField id="0">
									<name>contentlist/@deliverytype</name>
								</PSXXmlField>
								<PSXSingleHtmlParameter id="0">
									<name>delivery</name>
								</PSXSingleHtmlParameter>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="1" id="0">
								<PSXXmlField id="0">
									<name>contentlist/contentitem/@contentid</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>RXSITEITEMS</tableAlias>
									<column>CONTENTID</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="1" id="0">
								<PSXXmlField id="0">
									<name>contentlist/contentitem/@revision</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>RXSITEITEMS</tableAlias>
									<column>REVISIONID</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="1" id="0">
								<PSXXmlField id="0">
									<name>contentlist/contentitem/@unpublish</name>
								</PSXXmlField>
								<PSXTextLiteral id="0">
									<text>yes</text>
								</PSXTextLiteral>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="2" id="0">
								<PSXXmlField id="0">
									<name>contentlist/contentitem/modifydate</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>CONTENTSTATUS</tableAlias>
									<column>CONTENTLASTMODIFIEDDATE</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="3" id="0">
								<PSXXmlField id="0">
									<name>contentlist/contentitem/@variantid</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>RXSITEITEMS</tableAlias>
									<column>VARIANTID</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="4" id="0">
								<PSXXmlField id="0">
									<name>contentlist/contentitem/modifyuser</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>CONTENTSTATUS</tableAlias>
									<column>CONTENTLASTMODIFIER</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="4" id="0">
								<PSXXmlField id="0">
									<name>contentlist/contentitem/expiredate</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>CONTENTSTATUS</tableAlias>
									<column>CONTENTEXPIRYDATE</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="4" id="0">
								<PSXXmlField id="0">
									<name>contentlist/contentitem/contenttype</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>CONTENTSTATUS</tableAlias>
									<column>CONTENTTYPEID</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
						</PSXDataMapper>
						<PSXDataSelector id="1171" method="whereClause" unique="no">
							<WhereClauses>
								<PSXWhereClause id="0" omitWhenNull="no">
									<PSXConditional id="0">
										<variable>
											<PSXBackEndColumn id="0">
												<tableAlias>RXSITEITEMS</tableAlias>
												<column>SITEID</column>
												<columnAlias/>
											</PSXBackEndColumn>
										</variable>
										<operator>=</operator>
										<value>
											<PSXSingleHtmlParameter id="0">
												<name>sys_siteid</name>
											</PSXSingleHtmlParameter>
										</value>
										<boolean>AND</boolean>
									</PSXConditional>
								</PSXWhereClause>
								<PSXWhereClause id="0" omitWhenNull="no">
									<PSXConditional id="0">
										<variable>
											<PSXBackEndColumn id="0">
												<tableAlias>RXSITEITEMS</tableAlias>
												<column>CONTEXT</column>
												<columnAlias/>
											</PSXBackEndColumn>
										</variable>
										<operator>=</operator>
										<value>
											<PSXSingleHtmlParameter id="0">
												<name>sys_context</name>
											</PSXSingleHtmlParameter>
										</value>
										<boolean>AND</boolean>
									</PSXConditional>
								</PSXWhereClause>
								<PSXWhereClause id="0" omitWhenNull="no">
									<PSXConditional id="0">
										<variable>
											<PSXBackEndColumn id="0">
												<tableAlias>RXSITEITEMS</tableAlias>
												<column>PUBOPERATION</column>
												<columnAlias/>
											</PSXBackEndColumn>
										</variable>
										<operator>=</operator>
										<value>
											<PSXTextLiteral id="0">
												<text>publish</text>
											</PSXTextLiteral>
										</value>
										<boolean>AND</boolean>
									</PSXConditional>
								</PSXWhereClause>
								<PSXWhereClause id="0" omitWhenNull="no">
									<PSXConditional id="0">
										<variable>
											<PSXBackEndColumn id="0">
												<tableAlias>RXSITEITEMS</tableAlias>
												<column>PUBSTATUS</column>
												<columnAlias/>
											</PSXBackEndColumn>
										</variable>
										<operator>=</operator>
										<value>
											<PSXTextLiteral id="0">
												<text>success</text>
											</PSXTextLiteral>
										</value>
										<boolean>AND</boolean>
									</PSXConditional>
								</PSXWhereClause>
								<PSXWhereClause id="0" omitWhenNull="no">
									<PSXConditional id="0">
										<variable>
											<PSXBackEndColumn id="0">
												<tableAlias>RXSITEITEMS</tableAlias>
												<column>CONTENTID</column>
												<columnAlias/>
											</PSXBackEndColumn>
										</variable>
										<operator>IN</operator>
										<value>
											<PSXFunctionCall id="0">
												<name>IN-NUMBER</name>
												<PSXFunctionParamValue id="0">
													<value>
														<PSXSingleHtmlParameter id="0">
															<name>sys_contentid</name>
														</PSXSingleHtmlParameter>
													</value>
												</PSXFunctionParamValue>
												<PSXFunctionParamValue id="0">
													<value>
														<PSXTextLiteral id="0">
															<text/>
														</PSXTextLiteral>
													</value>
												</PSXFunctionParamValue>
											</PSXFunctionCall>
										</value>
										<boolean>AND</boolean>
									</PSXConditional>
								</PSXWhereClause>
							</WhereClauses>
							<Sorting/>
							<nativeStatement/>
							<Caching enabled="no" type="interval">
								<ageInterval>15</ageInterval>
							</Caching>
						</PSXDataSelector>
						<PSXResourceCacheSettings enabled="no" id="0">
							<Keys/>
							<Dependencies/>
						</PSXResourceCacheSettings>
					</PSXQueryPipe>
					<PSXPageDataTank id="1215">
						<schemaSource>file:contentlist.dtd</schemaSource>
						<actionTypeXmlField/>
					</PSXPageDataTank>
					<PSXRequestor directDataStream="no" id="0">
						<requestPage>buildUnpub_clist</requestPage>
						<SelectionParams/>
						<ValidationRules/>
						<characterEncoding>UTF-8</characterEncoding>
						<MimeProperties>
							<html>
								<PSXTextLiteral id="0">
									<text>text/html</text>
								</PSXTextLiteral>
							</html>
							<htm>
								<PSXTextLiteral id="0">
									<text>text/html</text>
								</PSXTextLiteral>
							</htm>
						</MimeProperties>
					</PSXRequestor>
					<PSXResultPageSet id="0">
						<PSXResultPage allowNamespaceCleanup="false" id="1201">
							<extensionsSupported/>
						</PSXResultPage>
					</PSXResultPageSet>
				</PSXDataSet>
			</xsl:if>
			<xsl:if test="not(PSXDataSet[name='siteitem_clist'])">
				<PSXDataSet id="1218">
					<name>siteitem_clist</name>
					<description>This resource builds a contentlist XML document (contentlist.dtd) for all items that were published to a site (sys_siteid parameter) and context (sys_context parameter). </description>
					<transactionType>none</transactionType>
					<PSXQueryPipe id="1180">
						<name>QueryPipe</name>
						<description/>
						<PSXBackEndDataTank id="1176">
							<PSXBackEndTable id="1175">
								<alias>RXSITEITEMS</alias>
								<datasource>
									<xsl:value-of select="$datasource"/>
								</datasource>
								<table>RXSITEITEMS</table>
							</PSXBackEndTable>
						</PSXBackEndDataTank>
						<PSXDataMapper id="1178" returnEmptyXml="yes">
							<PSXDataMapping groupId="1" id="0">
								<PSXXmlField id="0">
									<name>contentlist/contentitem/contenturl</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>RXSITEITEMS</tableAlias>
									<column>CONTENTURL</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="1" id="0">
								<PSXXmlField id="0">
									<name>contentlist/contentitem/delivery/location</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>RXSITEITEMS</tableAlias>
									<column>LOCATION</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="1" id="0">
								<PSXXmlField id="0">
									<name>contentlist/@context</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>RXSITEITEMS</tableAlias>
									<column>CONTEXT</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="1" id="0">
								<PSXXmlField id="0">
									<name>contentlist/contentitem/@contentid</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>RXSITEITEMS</tableAlias>
									<column>CONTENTID</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="1" id="0">
								<PSXXmlField id="0">
									<name>contentlist/contentitem/@revision</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>RXSITEITEMS</tableAlias>
									<column>REVISIONID</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
							<PSXDataMapping groupId="1" id="0">
								<PSXXmlField id="0">
									<name>contentlist/contentitem/@variantid</name>
								</PSXXmlField>
								<PSXBackEndColumn id="0">
									<tableAlias>RXSITEITEMS</tableAlias>
									<column>VARIANTID</column>
									<columnAlias/>
								</PSXBackEndColumn>
								<Conditionals/>
							</PSXDataMapping>
						</PSXDataMapper>
						<PSXDataSelector id="1177" method="whereClause" unique="yes">
							<WhereClauses>
								<PSXWhereClause id="0" omitWhenNull="no">
									<PSXConditional id="0">
										<variable>
											<PSXBackEndColumn id="0">
												<tableAlias>RXSITEITEMS</tableAlias>
												<column>SITEID</column>
												<columnAlias/>
											</PSXBackEndColumn>
										</variable>
										<operator>=</operator>
										<value>
											<PSXSingleHtmlParameter id="0">
												<name>sys_siteid</name>
											</PSXSingleHtmlParameter>
										</value>
										<boolean>AND</boolean>
									</PSXConditional>
								</PSXWhereClause>
								<PSXWhereClause id="0" omitWhenNull="no">
									<PSXConditional id="0">
										<variable>
											<PSXBackEndColumn id="0">
												<tableAlias>RXSITEITEMS</tableAlias>
												<column>CONTEXT</column>
												<columnAlias/>
											</PSXBackEndColumn>
										</variable>
										<operator>=</operator>
										<value>
											<PSXSingleHtmlParameter id="0">
												<name>sys_context</name>
											</PSXSingleHtmlParameter>
										</value>
										<boolean>AND</boolean>
									</PSXConditional>
								</PSXWhereClause>
								<PSXWhereClause id="0" omitWhenNull="no">
									<PSXConditional id="0">
										<variable>
											<PSXBackEndColumn id="0">
												<tableAlias>RXSITEITEMS</tableAlias>
												<column>PUBOPERATION</column>
												<columnAlias/>
											</PSXBackEndColumn>
										</variable>
										<operator>=</operator>
										<value>
											<PSXTextLiteral id="0">
												<text>publish</text>
											</PSXTextLiteral>
										</value>
										<boolean>AND</boolean>
									</PSXConditional>
								</PSXWhereClause>
								<PSXWhereClause id="0" omitWhenNull="no">
									<PSXConditional id="0">
										<variable>
											<PSXBackEndColumn id="0">
												<tableAlias>RXSITEITEMS</tableAlias>
												<column>PUBSTATUS</column>
												<columnAlias/>
											</PSXBackEndColumn>
										</variable>
										<operator>=</operator>
										<value>
											<PSXTextLiteral id="0">
												<text>success</text>
											</PSXTextLiteral>
										</value>
										<boolean>AND</boolean>
									</PSXConditional>
								</PSXWhereClause>
							</WhereClauses>
							<Sorting/>
							<nativeStatement/>
							<Caching enabled="no" type="interval">
								<ageInterval>15</ageInterval>
							</Caching>
						</PSXDataSelector>
						<PSXResourceCacheSettings enabled="no" id="0">
							<Keys/>
							<Dependencies/>
						</PSXResourceCacheSettings>
					</PSXQueryPipe>
					<PSXPageDataTank id="1217">
						<schemaSource>file:contentlist.dtd</schemaSource>
						<actionTypeXmlField/>
					</PSXPageDataTank>
					<PSXRequestor directDataStream="no" id="0">
						<requestPage>siteitem_clist</requestPage>
						<SelectionParams/>
						<ValidationRules/>
						<characterEncoding>UTF-8</characterEncoding>
						<MimeProperties>
							<html>
								<PSXTextLiteral id="0">
									<text>text/html</text>
								</PSXTextLiteral>
							</html>
							<htm>
								<PSXTextLiteral id="0">
									<text>text/html</text>
								</PSXTextLiteral>
							</htm>
						</MimeProperties>
					</PSXRequestor>
					<PSXResultPageSet id="0">
						<PSXResultPage allowNamespaceCleanup="false" id="1201">
							<extensionsSupported/>
						</PSXResultPage>
					</PSXResultPageSet>
				</PSXDataSet>
			</xsl:if>
			<xsl:apply-templates select="PSXLogger" mode="copy"/>
			<xsl:apply-templates select="PSXTraceInfo" mode="copy"/>
			<xsl:apply-templates select="PSXErrorWebPages" mode="copy"/>
			<xsl:apply-templates select="backEndLoginPassthru" mode="copy"/>
			<xsl:apply-templates select="PSXNotifier" mode="copy"/>
			<xsl:apply-templates select="userProperty" mode="copy"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
