<?xml version="1.0" encoding="UTF-8" ?>
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
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:psxctl="urn:percussion.com/control"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
	<xsl:template match="/"/>
	<!--
         categoryListWidgetControl
     -->
	<psxctl:ControlMeta name="categoryListWidgetControl" dimension="single" choiceset="none">
		<psxctl:Description>The control for building the JCR query for the Category List Widget:</psxctl:Description>
		<psxctl:ParamList>
		</psxctl:ParamList>
		<!-- CSS perc_webmgt.packed.css contains all css for jquery datepicker and Site/Save As control-->
		<psxctl:AssociatedFileList>
			<psxctl:FileDescriptor name="PercDatetimePicker.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>/cm/css/PercDatetimePicker.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="jquery-ui-1.8.9.custom.css     " type="css" mimetype="text/css">
				<psxctl:FileLocation>/cm/themes/smoothness/jquery-ui-1.8.9.custom.css     </psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="ui.datepicker.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>/cm/themes/smoothness/ui.datepicker.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_webmgt.packed.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>/cm/css/dynatree/skin/ui.dynatree.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_webmgt.packed.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>/rx_resources/widgets/categoryList/css/jquery.categoryList.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PSJSUtils.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-percutils/jquery.percutils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_path_constants.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/plugins/perc_path_constants.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_utils.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/plugins/perc_utils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="jquery.layout.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-layout/jquery.layout_and_plugins.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="jquery.metadata.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.metadata.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="tools.scrollable-1.1.2.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/tools.scrollable-1.1.2.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="tools.scrollable.mousewheel-1.0.1.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/tools.scrollable.mousewheel-1.0.1.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="jquery.validate.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-validation/jquery.validate.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<!-- SimpleDateFormat JS-->
			<psxctl:FileDescriptor name="date.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/jslib/profiles/3x/libraries/perc-retiredjs/date.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_extend_jQueryValidate.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/plugins/perc_extend_jQueryValidate.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<!-- Site Picker Control JS-->
			<psxctl:FileDescriptor name="perc_path_manager.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/plugins/perc_path_manager.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercContentBrowserWidget.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-dynatree/jquery.dynatree.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercContentBrowserWidget.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.text-overflow.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercContentBrowserWidget.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-jeditable/jquery.jeditable.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercContentBrowserWidget.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/services/PercServiceUtils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercContentBrowserWidget.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/widgets/PercFinderTree.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercContentBrowserWidget.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/plugins/PercExtendUiDialog.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<!-- Category List Control JS-->
			<psxctl:FileDescriptor name="jquery.categoryListControl.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/rx_resources/widgets/categoryList/js/jquery.categoryList.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="categoryListWidget.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/rx_resources/widgets/categoryList/js/categoryListWidget.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_save_as.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>/cm/widgets/perc_save_as.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
		</psxctl:AssociatedFileList>
	</psxctl:ControlMeta>

	<xsl:template match="Control[@name='categoryListWidgetControl']" mode="psxcontrol">

		<script>
			$(function () {
				$.categoryListWidget.psxControl(); // maintained in categoryListWidget.js
			});
		</script>
		<div id="perc-categorylist-wrapper" class="categorylist-with-border">
			<div id="categorylist-title-wrapper">
				<div id="categorylist-title" class="categorylist-close-image"> Category Criteria </div>
			</div>
			<div id="criteria_for_list">
				<div class="ui-perc-ctl_daterange">
					<table style="line-height:17px;">
						<tr>
							<td> <label for="display_title_contains">Title contains:<br/></label>
								<input style="width:300px" type="text" name="display_title_contains"
									   id="display_title_contains"/></td>
							<td width="30px"></td>
							<td> <label for="display_start_date">First published on or after:<br/></label>
								<input size="16" type="text" name="display_start_date" id="display_start_date"
									   class="date-pick"/></td>
							<td width="10px"></td>
							<td><label for="display_end_date">First published before: <br/></label>
								<input size="16" type="text" name="display_end_date" id="display_end_date"
									   class="date-pick"/></td>
						</tr>
					</table>
				</div>
				<p></p>
				<div id="perc-categorylist-detail">
					<table style="line-height:17px;">
						<tr>
							<td>
								<span class="categorylistlabel"><label for="perc-folder-selector"
																	   style="font-weight:bold">Website location:</label></span>
							</td>
							<td>
								<span class="categorylistlabel"><label for="display_pagetemplates_list"
																	   style="font-weight:bold">Template:</label></span>
							</td>
						</tr>
						<tr>
							<td height="265px" valign="top">
								<div id="perc-folder-selector">
								</div>
							</td>
							<td height="265px" valign="top">
								<div class="ui-perc-categorylist-pagetypes">
								</div>
							</td>
						</tr>
					</table>
				</div>
				<input type="hidden" aarenderer="NONE" class="datadisplay" id="perc-content-edit-query"
					   name="query"/>

				<div>
					<span class="categorylistlabel"><label for='perc_categorylist_resultspage' style="font-weight:bold">Results page:</label></span>
					<div style="margin-top: 5px;">
						<input type='text' name='display_category_page_result' id='display_category_page_result'/>
						<span id='perc_categorylist_resultspage_browse'>Browse</span>
						<input type='hidden' id='perc-formbuild-success-url-paired-unencrypted'/>
						<input type='hidden' id='perc-formbuild-success-url-paired-encrypted'/>
					</div>
				</div>

			</div>
		</div>

	</xsl:template>
	<xsl:template match="Control[@name='categoryListWidgetControl' and @isReadOnly='yes']" priority='10'
				  mode="psxcontrol">
		<script >
	<![CDATA[
			$(function () {
				$.categoryListWidget.readOnlyControl(); // maintained in categoryListWidget.js
			});
			]]>
	</script>
		<div id="perc-categorylist-wrapper">
			<div id="categorylist-title-wrapper">
				<div id="categorylist-title" class="categorylist-close-image">Results Criteria</div>
			</div>
			<div id="criteria_for_list">
				<label for="display_title_contains">Title contains:</label>
				<br/>
				<div class="datadisplay" id="perc_display_title_contains"><xsl:value-of
						select="//DisplayField/Control[@paramName='title_contains']/Value"/></div>
				<br/>
				<label for="display_start_date">Posted on or after:</label>
				<br/>
				<div class="datadisplay" id="perc_display_start_date"><xsl:value-of
						select="//DisplayField/Control[@paramName='start_date']/Value"/></div>
				<br/>
				<label for="display_end_date">Posted before:</label>
				<br/>
				<div class="datadisplay" id="perc_display_end_date"><xsl:value-of
						select="//DisplayField/Control[@paramName='end_date']/Value"/></div>
				<br/>
				<label for="display_site_path">Website location:</label>
				<br/>
				<input type="hidden" id="perc_site_path">
					<xsl:attribute name="value"><xsl:value-of select="//DisplayField/Control[@paramName='site_path']/Value"/></xsl:attribute>
				</input>
				<div class="datadisplay" id="perc_display_site_path"></div>
				<br/>
				<label for="display_end_date">Template:</label>
				<br/>
				<input type="hidden" id="perc_template_list">
					<xsl:attribute name="value"><xsl:value-of
							select="//DisplayField/Control[@paramName='page_templates_list']/Value"/></xsl:attribute>
				</input>
				<div class="datadisplay" id="perc_display_template_list">
				</div>
				<br/>
				<label for="perc_display_category_page_result">Results page:</label>
				<br/>
				<div class="datadisplay" id="perc_display_category_page_result"><xsl:value-of
						select="//DisplayField/Control[@paramName='category_page_result']/Value"/></div>
				<br/>

			</div>
		</div>
	</xsl:template>
</xsl:stylesheet>
