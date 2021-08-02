<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n">
	<xsl:template match="Workflow" mode="statusbar">
		<table width="100%" cellpadding="0" cellspacing="0" border="0">
			<tr class="headercell">
				<td>
					<!-- Query Part of Status Bar -->
					<table width="100%" cellpadding="2" cellspacing="1" border="0" class="backgroundcolor">
						<tr class="headercell">
							<td class="headercell2font" width="20%" align="center">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.sys_welcome.communitylogin@Content Title (ID)'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
							<td class="headercell2font" width="20%" align="center">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.contenteditor.statusbar@Creator'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
							<td class="headercell2font" width="20%" align="center">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.contenteditor.statusbar@Created On'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
							<td class="headercell2font" width="20%" align="center">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.contenteditor.statusbar@Last Modifier'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
							<td class="headercell2font" width="20%" align="center">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.contenteditor.statusbar@Last Modified On'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
						</tr>
						<xsl:choose>
							<xsl:when test="not(@contentId)">
								<tr class="datacell1">
									<td class="datacellnoentriesfound" colspan="5" align="center">&nbsp;</td>
								</tr>
							</xsl:when>
							<xsl:otherwise>
								<tr class="datacell1">
									<td class="datacell1font" align="center">
										<xsl:apply-templates select="ContentStatus/Title" mode="statusbar"/>&#160;(<xsl:apply-templates select="@contentId" mode="statusbar"/>)&#160;</td>
									<td class="datacell1font" align="center">
										<xsl:apply-templates select="ContentStatus/CreatedBy" mode="statusbar"/>&#160;</td>
									<td class="datacell1font" align="center">
										<xsl:apply-templates select="ContentStatus/CreatedDate" mode="statusbar"/>&#160;</td>
									<td class="datacell1font" align="center">
										<xsl:apply-templates select="ContentStatus/LastModifier" mode="statusbar"/>&#160;</td>
									<td class="datacell1font" align="center">
										<xsl:apply-templates select="ContentStatus/LastModified" mode="statusbar"/>&#160;</td>
								</tr>
							</xsl:otherwise>
						</xsl:choose>
					</table>
				</td>
			</tr>
			<tr class="headercell">
				<td>
					<!-- Second Row of Status Bar -->
					<table width="100%" cellpadding="0" cellspacing="1" border="0" class="backgroundcolor">
						<tr class="headercell">
							<td class="headercell2font" width="20%" align="center">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.contenteditor.statusbar@State(ID)'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
							<td class="headercell2font" width="20%" align="center">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.contenteditor.statusbar@Public'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
							<td class="headercell2font" width="20%" align="center">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.contenteditor.statusbar@Checked Out'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
							<td class="headercell2font" width="40%" align="center">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.contenteditor.statusbar@Assignees(Type)'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
						</tr>
						<xsl:choose>
							<xsl:when test="not(@contentId)">
								<tr class="datacell1">
									<td class="datacellnoentriesfound" colspan="4" align="center">&nbsp;</td>
								</tr>
							</xsl:when>
							<xsl:otherwise>
								<tr class="datacell1">
									<td class="datacell1font" align="center">
										<xsl:value-of select="ContentStatus/StateName"/>(<xsl:value-of select="ContentStatus/StateName/@stateId"/>)&#160;</td>
									<td class="datacell1font" align="center">
										<xsl:choose>
											<xsl:when test="ContentStatus/StateName/@isPublishable='yes'">
												<img src="../sys_resources/images/public.gif" width="16" height="16" border="0" alt="Yes">
													<xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.statusbar.alt@Yes'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
												</img>
											</xsl:when>
											<xsl:otherwise>
												<img src="../sys_resources/images/notpublic.gif" width="16" height="16" border="0" alt="No">
													<xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.statusbar.alt@No'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
												</img>
											</xsl:otherwise>
										</xsl:choose>&#160;
									     </td>
									<td class="datacell1font" align="center">
										<xsl:choose>
											<xsl:when test="ContentStatus/CheckOutUserName/@checkOutStatus='0'">
												<img height="16" width="16" border="0" src="../sys_resources/images/checkedin.gif" alt="Not Checked Out">
													<xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.statusbar.alt@Not Checked Out'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
												</img>
												<xsl:call-template name="getLocaleString">
													<xsl:with-param name="key" select="'psx.contenteditor.statusbar@not checked out'"/>
													<xsl:with-param name="lang" select="$lang"/>
												</xsl:call-template>
											</xsl:when>
											<xsl:when test="ContentStatus/CheckOutUserName/@checkOutStatus='1'">
												<img height="16" width="16" border="0" src="../sys_resources/images/checkedme.gif" alt="Checked Out by Me">
													<xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.statusbar.alt@Checked Out by Me'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
												</img>
												<!-- add by Me to alt tag when xsl added to checked out by other image -->
												<xsl:call-template name="getLocaleString">
													<xsl:with-param name="key" select="'psx.contenteditor.statusbar@by me'"/>
													<xsl:with-param name="lang" select="$lang"/>
												</xsl:call-template>
											</xsl:when>
											<xsl:when test="ContentStatus/CheckOutUserName/@checkOutStatus='2'">
												<img height="16" width="16" border="0" src="../sys_resources/images/checkedout.gif" alt="">
													<xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.statusbar.alt@by'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template>&nbsp;<xsl:value-of select="ContentStatus/CheckOutUserName"/></xsl:attribute>
													<!-- add xsl for user name in the alt tag -->
												</img>
												<xsl:call-template name="getLocaleString">
													<xsl:with-param name="key" select="'psx.contenteditor.statusbar@by'"/>
													<xsl:with-param name="lang" select="$lang"/>
												</xsl:call-template>&nbsp;
                                    <xsl:value-of select="ContentStatus/CheckOutUserName"/>
											</xsl:when>
										</xsl:choose>&#160;
									     </td>
									<td class="datacell1font" align="center">
										<xsl:apply-templates select="BasicInfo/AssignedRoles" mode="statusbar"/>&#160;
                           </td>
								</tr>
							</xsl:otherwise>
						</xsl:choose>
					</table>
				</td>
			</tr>
			<tr>
				<td>
					<!-- Third Row of Status Bar -->
					<table width="100%" cellpadding="0" cellspacing="1" border="0" class="backgroundcolor">
						<tr class="headercell">
							<td class="headercell2font" width="20%" align="center">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.contenteditor.statusbar@Community'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
							<td class="headercell2font" width="20%" align="center">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.contenteditor.statusbar@Workflow'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
							<td class="headercell2font" width="20%" align="center">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.contenteditor.statusbar@Locale'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
							<td width="20%" align="center"/>
						</tr>
						<xsl:choose>
							<xsl:when test="not(@contentId)">
								<tr class="datacell1">
									<td class="datacellnoentriesfound" colspan="4" align="center">&nbsp;</td>
								</tr>
							</xsl:when>
							<xsl:otherwise>
								<xsl:variable name="fields" select="/ContentEditor/ItemContent/DisplayField"/>
								<tr class="datacell1">
									<td class="datacell1font" align="center">
										<xsl:variable name="communityIdNode" select="$fields/Control[@paramName='sys_communityid']"/>
										<xsl:variable name="communityId" select="$communityIdNode/Value"/>
										<xsl:variable name="communityIdText" select="$communityIdNode//DisplayEntry[Value=$communityId]/DisplayLabel"/>
										<xsl:value-of select="$communityIdText"/>&#160;(<xsl:value-of select="$communityId"/>)
									</td>
									<td class="datacell1font" align="center">
										<xsl:value-of select="ContentStatus/WorkflowName"/>&#160;(<xsl:value-of select="ContentStatus/WorkflowName/@workflowId"/>)&#160;</td>
									<td class="datacell1font" align="center">
										<xsl:variable name="localeNode" select="$fields/Control[@paramName='sys_lang']"/>
										<xsl:variable name="localeId" select="$localeNode/Value"/>
										<xsl:variable name="localeIdText" select="$localeNode//DisplayEntry[Value=$localeId]/DisplayLabel"/>
										<xsl:value-of select="$localeIdText"/>
									</td>
									<td/>
								</tr>
							</xsl:otherwise>
						</xsl:choose>
					</table>
				</td>
			</tr>
		</table>
	</xsl:template>
	<xsl:template match="AssignedRoles" mode="statusbar">
		<xsl:apply-templates select="Role" mode="statusbar"/>
	</xsl:template>
	<xsl:template name="firstrole" match="Role[1]" mode="statusbar">
		<xsl:choose>
			<xsl:when test="@assignmentType='1'">
				<img height="16" width="16" border="0" src="../sys_resources/images/assignnot.gif" alt="No Access">
					<xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.statusbar.alt@No Access'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template>&nbsp;<xsl:value-of select="ContentStatus/CheckOutUserName"/></xsl:attribute>
				</img>
			</xsl:when>
			<xsl:when test="@assignmentType='2'">
				<img height="16" width="16" border="0" src="../sys_resources/images/assignedr.gif" alt="Reader">
					<xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.statusbar.alt@Reader'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template>&nbsp;<xsl:value-of select="ContentStatus/CheckOutUserName"/></xsl:attribute>
				</img>
			</xsl:when>
			<xsl:when test="@assignmentType='3'">
				<img height="16" width="16" border="0" src="../sys_resources/images/assigned.gif" alt="Assignee">
					<xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.statusbar.alt@Assignee'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template>&nbsp;<xsl:value-of select="ContentStatus/CheckOutUserName"/></xsl:attribute>
				</img>
			</xsl:when>
		</xsl:choose>
      &#160;&#160;
      <xsl:call-template name="getLocaleString">
			<xsl:with-param name="key" select="concat('psx.role@',.)"/>
			<xsl:with-param name="lang" select="$lang"/>
		</xsl:call-template>
	</xsl:template>
	<xsl:template match="Role" mode="statusbar">
		<br id="rx"/>
		<xsl:call-template name="firstrole"/>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_welcome.communitylogin@Content Title (ID)">Content Title column header in Status Bar.</key>
		<key name="psx.contenteditor.statusbar@Creator">Creator column header in Status Bar.</key>
		<key name="psx.contenteditor.statusbar@Created On">Created on date column header in Status Bar.</key>
		<key name="psx.contenteditor.statusbar@Last Modifier">Last modifier column header in Status Bar.</key>
		<key name="psx.contenteditor.statusbar@Last Modified On">Last modified on column header in Status Bar.</key>
		<key name="psx.contenteditor.statusbar@State(ID)">State(ID) column header in Status Bar.</key>
		<key name="psx.contenteditor.statusbar@Public">Public state column header in Status Bar.</key>
		<key name="psx.contenteditor.statusbar@Checked Out">Checked column header in Status Bar.</key>
		<key name="psx.contenteditor.statusbar@Assignees(Type)">Assignee type column header in Status Bar.</key>
		<key name="psx.contenteditor.statusbar.alt@Not Checked Out">Alt text for not checked out (open lock) image in Status Bar.</key>
		<key name="psx.contenteditor.statusbar@not checked out">Text next to not checked out image in Status Bar.</key>
		<key name="psx.contenteditor.statusbar.alt@Checked Out by Me">Alt text for checked out by me(pen) image in Status Bar.</key>
		<key name="psx.contenteditor.statusbar@by me">Text next to checked out by me image in Status Bar.</key>
		<key name="psx.contenteditor.statusbar.alt@by">Alt text for checked out by other member(closed lock) image in Status Bar. The member name next to by will be added dynamically. Ex: by admin1</key>
		<key name="psx.contenteditor.statusbar@by">Text next to checked out by other memeber image in Status Bar. The member name next to by will be added dynamically.  Ex: by admin1</key>
		<key name="psx.contenteditor.statusbar.alt@Yes">Alt text for public image in Status Bar.</key>
		<key name="psx.contenteditor.statusbar.alt@No">Alt text for nonpublic image in Status Bar.</key>
		<key name="psx.contenteditor.statusbar@Community">Community column header in Status Bar</key>
		<key name="psx.contenteditor.statusbar@Locale">Locale column header in Status Bar</key>
		<key name="psx.contenteditor.statusbar@Workflow">Workflow column header in Status Bar</key>
		<key name="psx.contenteditor.statusbar.alt@No Access">Alt text for No Access image</key>
		<key name="psx.contenteditor.statusbar.alt@Reader">Alt text for Reader access image</key>
		<key name="psx.contenteditor.statusbar.alt@Assignee">Alt text for Assignee access image</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
