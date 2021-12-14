<?xml version="1.0" encoding="UTF-8"?>
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
                xmlns:psxi18n="com.percussion.i18n" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="lang" select="/*/UserStatus/@xml:lang"/>
	<xsl:variable name="syscontentid" select="//Workflow/@contentId"/>
	<xsl:variable name="sysrevision" select="//Workflow/BasicInfo/HiddenFormParams/Param[@name='sys_revision']"/>
	<xsl:include href="file:sys_resources/stylesheets/assemblers/sys_popmenu.xsl"/>
	<xsl:variable name="revisionactions" select="document(concat(substring-before(//SectionLink[@name='actionlisturl'],'?'), '?pssessionid=',//UserStatus/@sessionId,'&amp;sys_mode=CXMAIN&amp;sys_uicontext=Revision'))/*"/>
	<xsl:template match="/">
		<xsl:apply-templates select="ContentEditor"/>
	</xsl:template>
	<xsl:template match="ContentEditor">
		<html>
			<head>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.contenteditor.revisionedit@Rhythmyx'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>&nbsp;-&nbsp;
               <xsl:value-of select="Workflow/ContentStatus/Title"/>&nbsp;-&nbsp;
               <xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.contenteditor.revisionedit@Revision List'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
				<link rel="stylesheet" type="text/css" href="/sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="/rx_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="{concat('/rx_resources/css/',$lang,'/templates.css')}"/>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/popmenu.css"/>
				<script language="javascript" src="../sys_resources/js/globalErrorMessages.js">;</script>
				<script language="javascript" src="{concat('../rx_resources/js/',$lang,'/globalErrorMessages.js')}">;</script>
				<script language="javascript" src="../sys_resources/js/browser.js">;</script>
				<script language="javascript" src="../sys_resources/js/href.js">;</script>
				<script language="javascript" src="../sys_resources/js/popmenu.js">;</script>
				<script>
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
				<xsl:variable name="syscontentid" select="//Workflow/@contentId"/>
				<xsl:variable name="sysrevision" select="//Workflow/BasicInfo/HiddenFormParams/Param[@name='sys_revision']"/>
				<xsl:variable name="sessionid" select="/*/@sessionid"/>
				<table width="100%" cellpadding="0" cellspacing="0" border="0" class="outerboxcell">
					<tr>
						<td>
							<a>
								<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.revisionedit.mnemonic.Audit Trail@A'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
								<xsl:attribute name="href"><xsl:value-of select="concat(@submitHref,'?sys_contentid=',$syscontentid,'&amp;sys_revision=',$sysrevision,'&amp;sys_command=preview&amp;sys_userview=sys_audittrail')"/></xsl:attribute>
								<xsl:call-template name="getMnemonicLocaleString">
									<xsl:with-param name="key" select="'psx.contenteditor.revisionedit@Audit Trail'"/>
									<xsl:with-param name="mnemonickey" select="'psx.contenteditor.revisionedit.mnemonic.Audit Trail@A'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</a>
						</td>
					</tr>
					<tr class="outerboxcell">
						<td align="center" class="outerboxcellfont">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.contenteditor.revisionedit@Revision List'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</td>
					</tr>
					<tr>
						<td valign="top">
							<table width="100%" cellpadding="0" cellspacing="1" border="0" class="backgroundcolor">
								<tr class="headercell">
									<th class="headercellfont" align="center">&#160;</th>
									<th class="headercellfont" align="center">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.contenteditor.revisionedit@Revision ID'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>
									</th>
									<th class="headercellfont" align="center">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.contenteditor.revisionedit@Date'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>
									</th>
									<th class="headercellfont" align="center">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.contenteditor.revisionedit@Who'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>
									</th>
									<th class="headercellfont" align="center">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.contenteditor.revisionedit@Comment'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>
									</th>
								</tr>
								<xsl:apply-templates select="/ContentEditor/Workflow/HistoryList" mode="historybar"/>
								<xsl:if test="count(/ContentEditor/Workflow/HistoryList)=0">
									<tr class="datacell1">
										<td align="center" colspan="5" class="datacellnoentriesfound">No entries found.&#160;</td>
									</tr>
								</xsl:if>
							</table>
						</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
	<xsl:key name="uniqueElement" match="HistoryEntry" use="@revision"/>
	<xsl:template match="HistoryList" mode="historybar">
		<xsl:for-each select="HistoryEntry[generate-id() = generate-id(key('uniqueElement',@revision))]">
			<tr>
				<xsl:choose>
					<xsl:when test="position() mod 2 = 1">
						<xsl:attribute name="class"><xsl:value-of select="'datacell1'"/></xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="class"><xsl:value-of select="'datacell2'"/></xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				<td width="5%" align="center" class="datacell1font">
					<xsl:apply-templates select="$revisionactions" mode="revactions">
						<xsl:with-param name="rev" select="@revision"/>
						<xsl:with-param name="showpromote">
							<xsl:choose>
								<xsl:when test="//BasicInfo/@CheckOutUserName='' and //BasicInfo/UserName/@assignmentType > 2 and 	//ContentStatus/StateName/@isPublishable='no'">
									<xsl:value-of select="'yes'"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="'no'"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:with-param>						
					</xsl:apply-templates>
				</td>
				<td align="center" class="datacell1font">
					<xsl:value-of select="@revision"/>
				</td>
				<td align="center" class="datacell1font">
					<xsl:value-of select="EventTime"/>
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
	<xsl:template match="ActionList" mode="revactions">
		<xsl:param name="rev"/>
		<xsl:param name="url"/>
		<xsl:param name="showpromote"/>
		<xsl:apply-templates select="Action" mode="revactions">
			<xsl:with-param name="rev" select="$rev"/>
			<xsl:with-param name="showpromote" select="$showpromote" />
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template match="Action[@name='Revision_ViewContent']" mode="revactions">
		<xsl:param name="rev"/>
		<a href="javascript:void(0)">
			<xsl:attribute name="onclick">PSViewContent('','<xsl:value-of select="$syscontentid"/>','<xsl:value-of select="$rev"/>')</xsl:attribute>
			<xsl:attribute name="title">
				<xsl:call-template name="readablelabel">
					<xsl:with-param name="id" select="$syscontentid"/><xsl:with-param name="rev" select="$rev"/>
				</xsl:call-template>
			</xsl:attribute>
			<xsl:value-of select="translate(@displayname,' ','&nbsp;')"/><xsl:text> </xsl:text>
		</a>
	</xsl:template>
	<xsl:template match="Action[@name='Revision_ViewProperties']" mode="revactions">
		<xsl:param name="rev"/>
		<a href="javascript:void(0)">
			<xsl:attribute name="onclick">PSViewMeta('','<xsl:value-of select="$syscontentid"/>','<xsl:value-of select="$rev"/>')</xsl:attribute>
			<xsl:attribute name="title">
				<xsl:call-template name="readablelabel">
					<xsl:with-param name="id" select="$syscontentid"/><xsl:with-param name="rev" select="$rev"/>
				</xsl:call-template>
			</xsl:attribute>
			<xsl:value-of select="translate(@displayname,' ','&nbsp;')"/><xsl:text> </xsl:text>
		</a>
	</xsl:template>
	<xsl:template match="Action[@name='Revision_Promote']" mode="revactions">
		<xsl:param name="rev"/>
		<xsl:param name="showpromote"/>	
		<xsl:if test="contains($showpromote,'yes')">
			<a href="javascript:void(0)">
				<xsl:attribute name="onclick">PSPromoteVersion('','<xsl:value-of select="$syscontentid"/>','<xsl:value-of select="$rev"/>')	</xsl:attribute>
				<xsl:attribute name="title">
					<xsl:call-template name="readablelabel">
						<xsl:with-param name="id" select="$syscontentid"/><xsl:with-param name="rev" select="$rev"/>
					</xsl:call-template>
				</xsl:attribute>
				<xsl:value-of select="translate(@displayname,' ','&nbsp;')"/><xsl:text> </xsl:text>
			</a>
		</xsl:if>
	</xsl:template>	
	<xsl:template name="readablelabel">
		<xsl:param name="id"/>
		<xsl:param name="rev"/>
		<xsl:value-of select="@displayname"/> <xsl:text> </xsl:text>
			<xsl:call-template name="getLocaleString">
				<xsl:with-param name="key" select="'psx.contenteditor.revisionedit@id'"/>
				<xsl:with-param name="lang" select="$lang"/>
			</xsl:call-template><xsl:text> </xsl:text>
			<xsl:value-of select="$syscontentid"/><xsl:text> </xsl:text> 
			<xsl:call-template name="getLocaleString">
				<xsl:with-param name="key" select="'psx.contenteditor.revisionedit@revision'"/>
				<xsl:with-param name="lang" select="$lang"/>
			</xsl:call-template><xsl:text> </xsl:text> 
		<xsl:value-of select="$rev"/>
	</xsl:template>	
	<psxi18n:lookupkeys>
		<key name="psx.contenteditor.revisionedit@Rhythmyx">Appears as part of the title.</key>
		<key name="psx.contenteditor.revisionedit@Revision List">Main header in Revisions.</key>
		<key name="psx.contenteditor.revisionedit@Revision ID">Revision column header in Revisions.</key>
		<key name="psx.contenteditor.revisionedit@Date">Date column header in Revisions.</key>
		<key name="psx.contenteditor.revisionedit@Who">Who acted on the content item column header in Revisions.</key>
		<key name="psx.contenteditor.revisionedit@Comment">Comment column header in Revisions.</key>
		<key name="psx.contenteditor.revisionedit@Audit Trail">Link to audit trail page.</key>
		<key name="psx.contenteditor.revisionedit.alt@Action Menu">Alt text for gray triangle image.</key>
		<key name="psx.contenteditor.revisionedit.mnemonic.Audit Trail@A">Mnemonic for label &quot;Audit Trail&quot;.</key>
		<key name="psx.contenteditor.revisionedit@id">Appears as part of the title text for actions</key>
		<key name="psx.contenteditor.revisionedit@revision">Appears as part of the title text for actions</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
