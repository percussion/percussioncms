<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="lang" select="/*/UserStatus/@xml:lang"/>
	<xsl:variable name="varContentStatus" select="//ContentStatus"/>
	<xsl:variable name="varHistoryList">
		<HistoryList>
			<xsl:call-template name="AddFirstCreateRow"/>
		</HistoryList>
	</xsl:variable>
	<xsl:variable name="syscontentid" select="//Workflow/@contentId"/>
	<xsl:variable name="sysrevision" select="//Workflow/BasicInfo/HiddenFormParams/Param[@name='sys_revision']"/>
	<xsl:template match="/">
		<xsl:apply-templates select="ContentEditor"/>
	</xsl:template>
	<xsl:template match="ContentEditor">
		<html>
			<head>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.contenteditor.audittrail@Rhythmyx'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>&nbsp;-&nbsp;
               <xsl:value-of select="$varContentStatus/Title"/>&nbsp;-&nbsp;
               <xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.contenteditor.audittrail@Audit Trail'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/popmenu.css"/>
				<script src="../sys_resources/js/globalErrorMessages.js">;</script>
				<script src="{concat('../rx_resources/js/',$lang,'/globalErrorMessages.js')}">;</script>
				<script language="javascript" src="../sys_resources/js/browser.js">;</script>
				<script language="javascript" src="../sys_resources/js/href.js">;</script>
				<script language="javascript" src="../sys_resources/js/popmenu.js">;</script>
				<script language="javascript">
					<![CDATA[

					   var textWin = null;
					   function textWindow(s)
					   {

					      textWin = window.open('','HistoryComment','width=500,height=100,resizable=yes');
					      textWin.document.open();
					      textWin.document.writeln("<html><head><title>History Comment</title></head><body>");
					      textWin.document.write(s);
					      textWin.document.writeln("</body></html>");
					      textWin.document.close();
					      setTimeout('textWin.close()',5000);				      
					   }
					]]>
				</script>

			</head>
			<body class="backgroundcolor" topmargin="5" leftmargin="5">
				<table width="100%" cellpadding="0" cellspacing="0" border="0" class="outerboxcell">
					<tr>
						<td>
							<a>
								<xsl:attribute name="title"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.audittrail@Revisions'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
								<xsl:attribute name="accesskey">
<xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.audittrail.mnemonic.Revisions@R'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template>								
								</xsl:attribute>
								<xsl:attribute name="href"><xsl:value-of select="concat(@submitHref,'?sys_contentid=',$syscontentid,'&amp;sys_revision=',$sysrevision,'&amp;sys_command=preview&amp;sys_userview=sys_Revisions')"/></xsl:attribute>
								<xsl:call-template name="getMnemonicLocaleString">
									<xsl:with-param name="key" select="'psx.contenteditor.audittrail@Revisions'"/>
									<xsl:with-param name="mnemonickey" select="'psx.contenteditor.audittrail.mnemonic.Revisions@R'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</a>
						</td>
					</tr>
					<tr class="outerboxcell">
						<td align="center" class="outerboxcellfont">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.contenteditor.audittrail@History'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</td>
					</tr>
					<tr>
						<td valign="top">
							<table width="100%" cellpadding="0" cellspacing="1" border="0" class="backgroundcolor">
								<tr class="headercell">
									<th class="headercellfont" align="center">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.contenteditor.audittrail@Date'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>
									</th>
									<th class="headercellfont" align="center">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.contenteditor.audittrail@Transition(id)'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>
									</th>
									<th class="headercellfont" align="center">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.contenteditor.audittrail@Rev'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>
									</th>
									<th class="headercellfont" align="center">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.contenteditor.audittrail@To State(id)'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>
									</th>
									<th class="headercellfont" align="center">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.contenteditor.audittrail@Publishable'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>
									</th>
									<th class="headercellfont" align="center">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.contenteditor.audittrail@Who'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>
									</th>
									<th class="headercellfont" align="center">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.contenteditor.audittrail@Comment'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>
									</th>
								</tr>
								<xsl:apply-templates select="$varHistoryList" mode="historybar"/>
							</table>
						</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="HistoryList" mode="historybar">
		<xsl:for-each select="HistoryEntry">
			<tr>
				<xsl:choose>
					<xsl:when test="position() mod 2 = 1">
						<xsl:attribute name="class"><xsl:value-of select="'datacell1'"/></xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="class"><xsl:value-of select="'datacell2'"/></xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				<td align="center" class="datacell1font">
					<xsl:value-of select="EventTime"/>
				</td>
				<td align="center" class="datacell1font">
					<xsl:choose>
						<xsl:when test="position()!=1 and CheckOutUserName='' and (@transitionId='' or @transitionId='0')">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.contenteditor.audittrail@Checked In'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:when test="CheckOutUserName!='' and (@transitionId='' or @transitionId='0')">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.contenteditor.audittrail@Checked out by'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>&nbsp;
						<xsl:value-of select="CheckOutUserName"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="TransitionLabel"/>
							<xsl:if test="TransitionLabel/@transitionid &gt; 0">(<xsl:value-of select="TransitionLabel/@transitionid"/>)</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:value-of select="historyaction"/>
				</td>
				<td align="center" class="datacell1font">
					<xsl:value-of select="@revision"/>
				</td>
				<td align="center" class="datacell1font">
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="concat('psx.workflow.state@',StateName)"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>&nbsp;
               (<xsl:value-of select="StateName/@stateId"/>)
            </td>
				<td align="center" class="datacell1font">
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="concat('psx.contenteditor.audittrail@',@isValid)"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>&nbsp;
            </td>
				<td align="center" class="datacell1font">
					<xsl:value-of select="Actor"/>
				</td>
				<td align="center" class="datacell1font">
					<xsl:if test="Comment!=''">
						<xsl:variable name="tmp">
							<xsl:call-template name="replace-apos">
								<xsl:with-param name="text" select="Comment"/>
							</xsl:call-template>
						</xsl:variable>
						<xsl:variable name="tmp2">
							<xsl:call-template name="replace-return">
								<xsl:with-param name="text" select="$tmp"/>
							</xsl:call-template>
						</xsl:variable>
						<img alt="{Comment}" src="../sys_resources/images/singlecomment.gif" width="16" height="16" border="0">
							<xsl:attribute name="OnClick">textWindow('<xsl:value-of select="$tmp2"/>');</xsl:attribute>
						</img>
					</xsl:if>&nbsp;
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
	<!-- bug fix for Rx-03-10-0065, the audit trail is missing the first entry when it has not been transitioned at least once. -->
	<!-- Below we add the first row, which is always the "create" row. We gather the data from a couple of places, we do -->
	<!-- this because when an item is first created there is no history list, so we retrieve the data from the content status -->
	<!-- element. We cannot however do this always since the state name, valid/publishable will change appropriately     -->
	<!-- based on the current state of the content item. -->
	<xsl:template name="AddFirstCreateRow">
		<xsl:variable name="haveHistory" select="count(//HistoryList/HistoryEntry)!=0"/>
		<xsl:variable name="stateName">
			<xsl:choose>
				<xsl:when test="$haveHistory">
					<xsl:value-of select="//HistoryList/HistoryEntry/StateName"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$varContentStatus/StateName"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="stateId">
			<xsl:choose>
				<xsl:when test="$haveHistory">
					<xsl:value-of select="//HistoryList/HistoryEntry/StateName/@stateId"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$varContentStatus/StateName/@stateId"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="valid">
			<xsl:choose>
				<xsl:when test="$haveHistory">
					<xsl:value-of select="//HistoryList/HistoryEntry/@isValid"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$varContentStatus/StateName/@isPublishable"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<HistoryEntry isValid="{$valid}" revision="1">
			<EventTime>
				<xsl:value-of select="$varContentStatus/CreatedDate"/>
			</EventTime>
			<Actor>
				<xsl:value-of select="$varContentStatus/CreatedBy"/>
			</Actor>
			<StateName stateId="{$stateId}">
				<xsl:value-of select="$stateName"/>
			</StateName>
			<Comment/>
			<TransitionLabel>
				<xsl:call-template name="getLocaleString">
					<xsl:with-param name="key" select="'psx.generic@Created'"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>
			</TransitionLabel>
		</HistoryEntry>
		<xsl:copy-of select="//HistoryList/HistoryEntry[position() != 1]"/>
	</xsl:template>
	<xsl:template name="replace-apos">
		<xsl:param name="text"/>
		<xsl:choose>
			<xsl:when test='contains($text, "&apos;" )'>
				<xsl:value-of select='concat(substring-before($text, "&apos;"), "\&apos;")'/>
				<xsl:call-template name="replace-apos">
					<xsl:with-param name="text" select='substring-after($text, "&apos;")'/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="replace-return">
		<xsl:param name="text"/>
		<xsl:choose>
			<xsl:when test='contains($text,"&#xA;")'>
				<xsl:value-of select='concat(substring-before($text,"&#xA;"), "&lt;br&gt;")'/>
				<xsl:call-template name="replace-return">
					<xsl:with-param name="text" select='substring-after($text,"&#xA;")'/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.contenteditor.audittrail@Rhythmyx">Appears as part of the title.</key>
		<key name="psx.contenteditor.audittrail@Revisions">Revisions link label, when clicked opens revisions view.</key>
		<key name="psx.contenteditor.audittrail.mnemonic.Revisions@R">Mnemonic key for Revisions link.</key>
		<key name="psx.contenteditor.audittrail@Audit Trail">Label for history bar in Audit Trail.</key>
		<key name="psx.contenteditor.audittrail@Rev">Revision column header in Audit Trail.</key>
		<key name="psx.contenteditor.audittrail@Date">Date column header in Audit Trail.</key>
		<key name="psx.contenteditor.audittrail@Transition(id)">Transition column header in Audit Trail.</key>
		<key name="psx.contenteditor.audittrail@To State(id)">To State column header in Audit Trail.</key>
		<key name="psx.contenteditor.audittrail@Publishable">Publishable column header in Audit Trail.</key>
		<key name="psx.contenteditor.audittrail@Who">Who acted on the content item column header in Audit Trail.</key>
		<key name="psx.contenteditor.audittrail@Comment">Comment column header in Audit Trail.</key>
		<key name="psx.contenteditor.audittrail@Created">This is the transition display in the history bar when an item gets created.</key>
		<key name="psx.contenteditor.audittrail@Checked In">This is the transition display in the history bar when an item checked in.</key>
		<key name="psx.contenteditor.audittrail@Checked out by">This is the transition display in the history bar when an item checked out by a member. The member name appears next to it.</key>
		<key name="psx.contenteditor.audittrail@yes">Publishable column value in the history bar if publishable.</key>
		<key name="psx.contenteditor.audittrail@no">Publishable column value in the history bar if not publishable.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
