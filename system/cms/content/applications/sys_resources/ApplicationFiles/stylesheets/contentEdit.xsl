<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 SYSTEM "../../DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol SYSTEM "../../DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial SYSTEM "../../DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<!-- $ Id: $ -->
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxctl="URN:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:import href="file:sys_resources/stylesheets/sys_Templates.xsl"/>
	<xsl:import href="file:sys_resources/stylesheets/customControlImports.xsl"/>
	<xsl:import href="file:rx_resources/stylesheets/rx_Templates.xsl"/>
	<xsl:import href="file:sys_resources/stylesheets/ActionList.xsl"/>
	<xsl:output method="html" indent="yes" encoding="UTF-8"/>
	<xsl:variable name="systemLibrary" select="'file:sys_resources/stylesheets/sys_Templates.xsl'"/>
	<xsl:variable name="systemLibraryDoc" select="document($systemLibrary)"/>
	<xsl:variable name="userLibrary" select="'file:rx_resources/stylesheets/rx_Templates.xsl'"/>
	<xsl:variable name="userLibraryDoc" select="document($userLibrary)"/>
	<xsl:variable name="customControlImportsDoc" select="document('../sys_resources/stylesheets/customControlImports.xsl')"/>
	<xsl:variable name="lang" select="/*/UserStatus/@xml:lang"/>
	<xsl:template match="/">
		<xsl:apply-templates select="ContentEditor"/>
	</xsl:template>
	<xsl:template match="ContentEditor">
		<xsl:variable name="sysview" select="ItemContent/DisplayField/Control[@paramName='sys_currentview']/Value"/>
		<xsl:variable name="syscontentid" select="Workflow/@contentId"/>
		<xsl:variable name="sysrevision" select="Workflow/BasicInfo/HiddenFormParams/Param[@name='sys_revision']"/>
		<xsl:variable name="syspageid" select="/*/ActionLinkList/ActionLink/Param[@name='sys_pageid']"/>
		<html>
			<head>
				<xsl:text>&#10;</xsl:text>
				<script language="javascript" src="../tmx/tmx.jsp?/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang={$lang}">;</script>
				<xsl:text>&#10;</xsl:text>
				<script language="javascript" src="../../cm/jslib/jquery.js">;</script>
				<xsl:text>&#10;</xsl:text>
                <script language="javascript" src="../../cm/jslib/jquery-ui.js">;</script>
                <xsl:text>&#10;</xsl:text>
				<script language="javascript" src="../sys_resources/js/cm/init.js">;</script>
				<xsl:text>&#10;</xsl:text>
				<xsl:variable name="scripttags">
					<xsl:apply-templates select="ControlNameSet/ControlName" mode="scriptfiles"/>
				</xsl:variable>
				<xsl:variable name="styletags">
					<xsl:apply-templates select="ControlNameSet/ControlName" mode="stylefiles"/>
				</xsl:variable>
				<xsl:call-template name="createControlScriptTags">
					<xsl:with-param name="scripttags" select="$scripttags"/>
				</xsl:call-template>
				<xsl:call-template name="createControlStyleTags">
					<xsl:with-param name="styletags" select="$styletags"/>
				</xsl:call-template>
                <link rel="stylesheet" href="../../cm/widgets/PercDataTable/PercDataTable.css"/>
				<link rel="stylesheet" href="../../cm/widgets/PercActionDataTable/PercActionDataTable.css"/>
                <link rel="stylesheet" href="../../cm/widgets/PercPageDataTable/PercPageDataTable.css"/>
                <link rel="stylesheet" href="../../cm/widgets/PercSimpleMenu/PercSimpleMenu.css"/>
				<link rel="stylesheet" href="../sys_resources/css/cmlite.css"/>
                <link rel="stylesheet" href="/web_resources/cm/themes/smoothness/jquery-ui-1.8.9.custom.css"/>
				<xsl:text>&#10;</xsl:text>
				<script language="javascript" src="{concat('../web_resources/cm/common/js/PercGlobalVariablesData.js?_',@currentTimeStamp)}">;</script>
				<xsl:text>&#10;</xsl:text>
				<script> 
                   (function($)
                   {
                        $(document).ready(function(){
                            
                             if(!$("#perc-content-edit-metadata-link").hasClass('noClick')) {
                                $(".perc-content-edit-data").hide();
                                $("#perc-content-edit-metadata-link, #perc-content-edit-site-link").click(function () {
                                    $(this).parent().find(".perc-content-edit-data").toggle();
                                    $(this).toggleClass('perc-spacer perc-tab-open');                                
                                });
                            }
                        // Add class 'perc-shared-asset' to body tag if asset is shared one    
                        if($('body').find('#perc-content-edit-sys_title').length)
                        {   
                            var getFieldType = $('#perc-content-edit-sys_title').attr('type');
                            if(getFieldType == 'text') { 
                                $('body').addClass('perc-shared-asset');
                            }
                        }
                     <xsl:if test="@commandName='edit'">
                            findTopMostJQuery();
                            addKeyPressDirtyEvents();
                     </xsl:if>       
			                   fixIE($); // Will only get run if IE browser detected.
                        });
                    })(jQuery);
				</script>
				<xsl:text>&#10;</xsl:text>
				<title><xsl:value-of select="//DisplayField/Control[@paramName='sys_title']/Value"/></title>
				<xsl:text>&#10;</xsl:text>
			</head>
			<body>
				<style type="text/css"> 
				   .perc-required-legend{
                        position: relative; float: right; padding-right: 9px; margin-top: 3px;
				   }

				   #edit-page-metadata > form > span.perc-required-legend{
                                      margin-top: -29px;
				        padding-right: 0px;
				        margin-right: -2px;
				   }
				   .perc-required-field:before {
                        content: "* ";
                        margin-left: -11px;
                    }
                    
                   .perc-content-edit-data {
                        padding-right:35px;
                        padding-bottom:30px;
                    }

                    
				</style>
				<form method="post" action="{@submitHref}" id="perc-content-form" name="perc-content-form" encType="multipart/form-data">
				<xsl:if test="//DisplayField[@displayType='sys_normal']/Control[@isRequired = 'yes' and @isReadOnly='no' and @name != 'sys_HiddenInput']">
			   	<span class="perc-required-legend"><label>* - denotes required field</label></span>
			   </xsl:if>
					<xsl:apply-templates select="ItemContent"/>
					<xsl:apply-templates select="ActionLinkList" mode="addformparams"/>
					<input xmlns="" type="hidden" name="sys_contenttypeid">
						<xsl:attribute name="value"><xsl:value-of select="//ContentEditor/@contentTypeId"/></xsl:attribute>
					</input>
				</form>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="ItemContent">
		<xsl:comment>ItemContent</xsl:comment>
		<xsl:if test="//FieldError">
			<div id="perc-content-edit-errors">
				<label class="perc_field_error">Error saving the asset.</label>
			</div>
		</xsl:if>
		<div id="perc-content-edit-content" class = "asset-details" >
			<!-- The following is a place-holder at the top for additional client specific content -->
			<div id="perc-content-edit-content-top-placeholder" style="display:none"></div>
			<xsl:apply-templates select="DisplayField[@fieldValueType='content'] | DisplayField[@fieldValueType='unknown' and Control/@dataType!='sys_system']"/>
		</div>
		<xsl:variable name="metaCount" select="count(DisplayField[@fieldValueType='meta'] | DisplayField[@fieldValueType='unknown' and DisplayField[@fieldValueType!='content'] and @displayType='sys_normal' and Control/@dataType='sys_system' and Control/@name!='sys_HiddenInput'])"/>
		<div id="perc-content-edit-metadata-panel">
			<xsl:if test="$metaCount &lt; 2"><xsl:attribute name="style">display:none</xsl:attribute></xsl:if>
			<div id="perc-content-edit-metadata-link" class = "perc-spacer">
				<span id="perc-content-edit-metadata-icon" />Meta-data</div>
			<div class="perc-content-edit-data">
				<div id="perc-content-edit-meta-data-top-placeholder"></div>

				<xsl:apply-templates select="DisplayField[@fieldValueType='meta'] | DisplayField[@fieldValueType='unknown' and Control/@dataType='sys_system']"/>
			</div>
			<div id="perc-content-edit-metadata-sep"/>
		</div>
        
		<div id="perc-site-impact-panel" style = "display:none">			
			<div id="perc-content-edit-site-link" class = "perc-spacer">
				<span id="perc-content-edit-site-icon" />Site Impact</div>
			<div class="perc-content-edit-data">
                    <span class = "font_normal_07em_black">Pages using this asset</span>
                    <div class = "perc-site-impact-pages">
                    </div>
                    <span class = "font_normal_07em_black">Templates using this asset</span>
                    <div class = "perc-site-impact-templates">
                    </div>                    
			</div>            
        </div>
	</xsl:template>
	<xsl:template match="DisplayField[@displayType='sys_hidden']">
		<xsl:comment>Hidden Control</xsl:comment>
		<xsl:apply-templates select="Control" mode="psxcontrol-hidden"/>
	</xsl:template>
	<!-- Treat sys_HiddenInput control also as hidden field-->
	<xsl:template match="DisplayField[Control[@name='sys_HiddenInput']]" priority="10">
		<xsl:comment>Hidden Control</xsl:comment>
		<xsl:apply-templates select="Control" mode="psxcontrol-hidden"/>
	</xsl:template>
	<xsl:template match="DisplayField[@displayType='sys_normal'] | DisplayField[@displayType='sys_error']">
		<xsl:comment>Normal Control</xsl:comment>
        <div type="{@displayType}">
        <xsl:if test="DisplayLabel!=''">
			<xsl:variable name="keyval">
				<xsl:choose>
					<xsl:when test="DisplayLabel/@sourceType='sys_system'">
						<xsl:value-of select="concat('psx.ce.system.', Control/@paramName, '@', DisplayLabel)"/>
					</xsl:when>
					<xsl:when test="DisplayLabel/@sourceType='sys_shared'">
						<xsl:value-of select="concat('psx.ce.shared.', Control/@paramName, '@', DisplayLabel)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat('psx.ce.local.', /ContentEditor/@contentTypeId, '.', Control/@paramName, '@', DisplayLabel)"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:variable name="mnemonickeyval">
				<xsl:choose>
					<xsl:when test="DisplayLabel/@sourceType='sys_system'">
						<xsl:value-of select="concat('psx.ce.system.', Control/@paramName, '.mnemonic.', DisplayLabel, '@', Control/@accessKey)"/>
					</xsl:when>
					<xsl:when test="DisplayLabel/@sourceType='sys_shared'">
						<xsl:value-of select="concat('psx.ce.shared.', Control/@paramName, '.mnemonic.', DisplayLabel, '@', Control/@accessKey)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat('psx.ce.local.', /ContentEditor/@contentTypeId, '.', Control/@paramName, '.mnemonic.', DisplayLabel, '@', Control/@accessKey)"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<label for="{Control/@paramName}" accesskey="{Control/@accessKey}">
			<xsl:if test="Control/@isRequired='yes'"><xsl:attribute name="class">perc-required-field</xsl:attribute></xsl:if>
				<xsl:call-template name="getMnemonicLocaleString">
					<xsl:with-param name="key" select="$keyval"/>
					<xsl:with-param name="mnemonickey" select="$mnemonickeyval"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>
			</label>
			<br/>
		</xsl:if>
		<xsl:apply-templates select="Control" mode="psxcontrol"/>
        <xsl:if test="position()!=last()">
            <br/>
        </xsl:if>
		<xsl:variable name="paramName" select="Control/@paramName"/>
		<xsl:if test="//FieldError[@submitName=$paramName]">
			<label class="perc_field_error" for="{$paramName}" generated="true" style="display: block;">
				<xsl:value-of select="//FieldError[@submitName=$paramName]"/>
			</label>
		</xsl:if>
        </div>
	</xsl:template>
	<xsl:template match="DisplayField">
		<b>unmatched display field type: '<xsl:copy-of select="@displayType"/>'</b>
		<br id="Rhythmyx"/>
		<xsl:comment>Unmatched display field</xsl:comment>
		<xsl:copy-of select="."/>
	</xsl:template>
	<xsl:template match="ControlName" mode="scriptfiles">
		<xsl:variable name="ctlname" select="."/>
		<xsl:choose>
			<xsl:when test="$userLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]">
				<xsl:apply-templates select="$userLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]/psxctl:AssociatedFileList/psxctl:FileDescriptor[@type='script']" mode="scriptfiles"/>
			</xsl:when>
			<xsl:when test="$systemLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]">
				<xsl:apply-templates select="$systemLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]/psxctl:AssociatedFileList/psxctl:FileDescriptor[@type='script']" mode="scriptfiles"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="$customControlImportsDoc//xsl:import">
					<xsl:variable name="customLibrary" select="@href"/>
					<xsl:choose>
						<xsl:when test="document($customLibrary)/*/psxctl:ControlMeta[@name=$ctlname]">
							<xsl:apply-templates select="document($customLibrary)/*/psxctl:ControlMeta[@name=$ctlname]/psxctl:AssociatedFileList/psxctl:FileDescriptor[@type='script']" mode="scriptfiles"/>
						</xsl:when>
					</xsl:choose>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="psxctl:FileDescriptor" mode="scriptfiles"><xsl:value-of select="psxctl:FileLocation"/>;</xsl:template>
	<xsl:template match="ControlName" mode="stylefiles">
		<xsl:variable name="ctlname" select="."/>
		<xsl:choose> 
			<xsl:when test="$systemLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]">
				<xsl:apply-templates select="$systemLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]/psxctl:AssociatedFileList/psxctl:FileDescriptor[@type='css']" mode="stylefiles"/>
			</xsl:when>
			<!-- User CSS should be after the System CSS -->
			<xsl:when test="$userLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]">
				<xsl:apply-templates select="$userLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]/psxctl:AssociatedFileList/psxctl:FileDescriptor[@type='css']" mode="stylefiles"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="$customControlImportsDoc/*/xsl:import">
					<xsl:variable name="customLibrary" select="@href"/>
					<xsl:choose>
						<xsl:when test="document($customLibrary)/*/psxctl:ControlMeta[@name=$ctlname]">
							<xsl:apply-templates select="document($customLibrary)/*/psxctl:ControlMeta[@name=$ctlname]/psxctl:AssociatedFileList/psxctl:FileDescriptor[@type='css']" mode="stylefiles"/>
						</xsl:when>
					</xsl:choose>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="psxctl:FileDescriptor" mode="stylefiles"><xsl:value-of select="psxctl:FileLocation"/>:<xsl:value-of select="@name"/>;</xsl:template>
	<xsl:template match="ControlNameSet"/>
	<xsl:template match="Workflow"/>
	<xsl:template match="SectionLinkList"/>
	<xsl:template match="ActionLinkList"/>
	<xsl:template name="createControlScriptTags">
		<xsl:param name="scripttags"/>
		<xsl:if test="not(contains(substring-after($scripttags, ';'),substring-before($scripttags, ';')))">
			<script src="{substring-before($scripttags, ';')}">;</script>
			<xsl:text>&#10;</xsl:text>
		</xsl:if>
		<xsl:if test="string-length(substring-after($scripttags, ';')) &gt; 1">
			<xsl:call-template name="createControlScriptTags">
				<xsl:with-param name="scripttags" select="substring-after($scripttags, ';')"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	<xsl:template name="createControlStyleTags">
		<xsl:param name="styletags"/>
		<xsl:if test="not(contains(substring-after($styletags, ';'),substring-before($styletags, ';')))">
			<xsl:variable name="styletag"><xsl:value-of select="substring-before($styletags, ';')"/></xsl:variable>
			<link rel="stylesheet" href="{substring-before($styletag, ':')}" type="text/css" media="screen" />
		</xsl:if>
		<xsl:if test="string-length(substring-after($styletags, ';')) &gt; 1">
			<xsl:call-template name="createControlStyleTags">
				<xsl:with-param name="styletags" select="substring-after($styletags, ';')"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
