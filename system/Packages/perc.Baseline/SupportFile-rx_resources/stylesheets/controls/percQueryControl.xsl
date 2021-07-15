<?xml version="1.0" encoding="UTF-8" ?>
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

<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 SYSTEM "../../../DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol SYSTEM "../../../DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial SYSTEM "../../../DTD/HTMLspecialx.ent">
		%HTMLspecial;
		]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxctl="URN:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<xsl:template match="/" />
	<!--
         percQueryControl
     -->
	<psxctl:ControlMeta name="percQueryControl" dimension="single" choiceset="none">
		<psxctl:Description>The control for building the JCR query for the any List Widget:</psxctl:Description>
		<psxctl:ParamList>
		</psxctl:ParamList>
		<!-- Add Extension here
		<psxctl:Dependencies>
			<psxctl:Dependency status="readyToGo" occurrence="single">
				<psxctl:Default>
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/generic/sys_FileInfo</name>
					</PSXExtensionCall>
				</psxctl:Default>
			</psxctl:Dependency>
		</psxctl:Dependencies>
		-->
		<!-- CSS perc_webmgt.packed.css contains all css for jquery datepicker and Site/Save As control-->
		<psxctl:AssociatedFileList>
			<psxctl:FileDescriptor name="perc_webmgt.packed.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>../../cm/css/dynatree/skin/ui.dynatree.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="percQueryControl.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>../rx_resources/controls/percQueryControl/css/percQueryControl.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>

			<!-- JavaScript -->
			<psxctl:FileDescriptor name="jquery-ui.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/libraries/jquery-ui/jquery-ui.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="jquery.percutils.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/plugins/jquery-percutils/jquery.percutils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_path_constants.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/plugins/perc_path_constants.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_utils.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/plugins/perc_utils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="jquery.layout.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/plugins/jquery-layout/jquery.layout_and_plugins.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="jquery.metadata.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.metadata.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="tools.scrollable-1.1.2.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/tools.scrollable-1.1.2.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="tools.scrollable.mousewheel-1.0.1.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/tools.scrollable.mousewheel-1.0.1.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="jquery.validate.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/plugins/jquery-validation/jquery.validate.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<!-- SimpleDateFormat JS-->
			<psxctl:FileDescriptor name="date.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/profiles/3x/libraries/perc-retiredjs/date.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_extend_jQueryValidate.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/plugins/perc_extend_jQueryValidate.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<!-- Site Picker Control JS-->
			<psxctl:FileDescriptor name="perc_path_manager.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/plugins/perc_path_manager.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="jquery.dynatree.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/plugins/jquery-dynatree/jquery.dynatree.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="jquery.text-overflow.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.text-overflow.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="jquery.jeditable.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/plugins/jquery-jeditable/jquery.jeditable.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercContentBrowserWidget.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/services/PercServiceUtils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercContentBrowserWidget.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/widgets/PercFinderTree.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercContentBrowserWidget.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/plugins/PercExtendUiDialog.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<!-- Query List Control JS-->
			<psxctl:FileDescriptor name="percQueryControl" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../rx_resources/controls/percQueryControl/js/percQueryControl.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_save_as.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/widgets/perc_save_as.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
		</psxctl:AssociatedFileList>
	</psxctl:ControlMeta>

	<xsl:template match="Control[@name='percQueryControl']" mode="psxcontrol">

		<script >
			$(function() {
			$(".hasDatepicker").addClass('datadisplay');
			$('#display_query_published_after').attr("autocomplete","off");
			$('#display_query_published_before').attr("autocomplete","off");
			$('#display_query_published_after').datepicker({
			onSelect:
			function(value, date) {
			// *********************************************************************
			//This will be set from the widget config $date_format (set by velocity)
			// *********************************************************************
			$(this).trigger("focus");
			var p_query_published_after = new Date(value);
			var p_query_published_before = new Date($('[name="query_published_before"]').val());
			<xsl:text disable-output-escaping="yes">
				if(p_query_published_after >= p_query_published_before)
                {
                    $.perc_utils.alert_dialog({title:"Error", content:"First published on or after date must be less than First published before date.", okCallBack:function(){
                        setDisplayDate($('[name="query_published_after"]').val(),"display_query_published_after");
                        return false;
                    }});
                }
                else
                {
                    $('[name="query_published_after"]').val(value);

                    setDisplayDate(value,"display_query_published_after");

                    buildQuery();
                }
								</xsl:text>
			// if the top most jquery is defined
			if($.topFrameJQuery != undefined)
			// mark the asset as dirty
			$.topFrameJQuery.PercDirtyController.setDirty(true, "asset");
			},
			showOn: 'button', buttonImage: '../rx_resources/controls/percQueryControl/images/calendar.gif', buttonImageOnly: true, altFormat: 'yy-mm-dd', buttonText: ''
			});

			$('#display_query_published_before').datepicker({
			onSelect:
			function(value, date) {
			$(this).trigger('focus');
			var p_query_published_after = new Date($('[name="query_published_after"]').val());
			var p_query_published_before = new Date(value);
			<xsl:text disable-output-escaping="yes">
				if(p_query_published_after >= p_query_published_before)
                {
                    $.perc_utils.alert_dialog({title:"Error", content:"First published before date must be greater than First published on or after date.", okCallBack:function(){
                        setDisplayDate($('[name="query_published_before"]').val(),"display_query_published_before");
                        return false;
                    }});
                }
                else
                {
                    $('[name="query_published_before"]').val(value);
                    setDisplayDate(value,"display_query_published_before");
                    buildQuery();
                }
								</xsl:text>
			// if the top most jquery is defined
			if($.topFrameJQuery != undefined)
			// mark the asset as dirty
			$.topFrameJQuery.PercDirtyController.setDirty(true, "asset");
			},
			showOn: 'button', buttonImage: '../rx_resources/controls/percQueryControl/images/calendar.gif', buttonImageOnly: true, altFormat: 'yy-mm-dd', buttonText: ''
			});

			$('#perc-content-form').queryListControl({});
			});


		</script>

		<script >
			$(document).ready(function(){
				$("#querylist-title").on("click",function () {
					$("#criteria_for_list").toggle();
					$("#querylist-title").toggleClass("querylist-expand-image querylist-close-image");
				});
			});
		</script>
		<div id = "perc-querylist-wrapper">
			<div id = "querylist-title-wrapper">
				<div id = "querylist-title" class = "querylist-close-image"> Archive Criteria </div>
			</div>
			<div id="criteria_for_list">
				<div class="ui-perc-ctl_daterange">
					<table style = "line-height:17px;">
						<tr>
							<td> <label for="display_title_contains">Title contains:<br /></label>
								<input style = 'width:300px' type="text" name="display_title_contains" id="display_title_contains"/></td>
							<td width = "30px"></td>
							<td> <label for="display_query_published_after">First published on or after:<br /></label>
								<input size = "16" type="text" name="display_query_published_after" id="display_query_published_after" class="date-pick"/></td>
							<td width = "10px"></td>
							<td><label for="display_query_published_before">First published before: <br /></label>
								<input size = "16" type="text" name="display_query_published_before" id="display_query_published_before" class="date-pick"/></td>
						</tr>
					</table>
				</div>
				<p></p>
				<div id = "perc-querylist-detail">
					<table style = "line-height:17px;">
						<tr>
							<td>
								<span class = "querylistlabel"><label for="perc-folder-selector" style="font-weight:bold">Website location:</label></span>
							</td>
							<td>
								<span class = "querylistlabel"><label for="display_pagetemplates_list" style="font-weight:bold">Template:</label></span>
							</td>
						</tr>
						<tr>
							<td height = "265px" valign = "top">
								<div id="perc-folder-selector">
								</div>
							</td>
							<td height = "265px" valign = "top">
								<div class="ui-perc-querylist-pagetypes">
								</div>
							</td>
						</tr>
					</table>
				</div>
				<input type="hidden" aarenderer="NONE" class="datadisplay" id="perc-content-edit-query" name="query_string"/>

				<div>
					<span class = "querylistlabel"><label for='perc_querylist_resultspage' style="font-weight:bold">Results page:</label></span>
					<div style="margin-top: 5px;">
						<input type='text' name='display_query_page_result' id='display_query_page_result' />
						<span id='perc_querylist_resultspage_browse'>Browse</span>
						<input type='hidden' id='perc-formbuild-success-url-paired-unencrypted' />
						<input type='hidden' id='perc-formbuild-success-url-paired-encrypted' />
					</div>
				</div>

			</div>
		</div>

	</xsl:template>
	<xsl:template match="Control[@name='percQueryControl' and @isReadOnly='yes']" priority='10' mode="psxcontrol">
		<script >
	<![CDATA[
			$(document).ready(function(){

				$(".hasDatepicker").addClass('datadisplay');

				$("#querylist-title").on("click",function () {
					$("#criteria_for_list").toggle();
					$("#querylist-title").toggleClass("querylist-expand-image querylist-close-image");
				});

				// Put site value in website location field
				var sitepath = $("#perc_site_path").val().substring(8);
				var splitPath = sitepath.split("/");



				$("#perc_display_site_path").text(sitepath);

				// Fill templates field

				if (splitPath[0] != undefined && splitPath[0] != "")
				{
					$.PercServiceUtils.makeJsonRequest(
							$.perc_paths.TEMPLATES_BY_SITE + "/" + splitPath[0],
							$.PercServiceUtils.TYPE_GET,
							false,
							function(status, result){
								if(status == $.PercServiceUtils.STATUS_SUCCESS)
								{
									var summaries = result.data.TemplateSummary;
									var temps = {};
									var tempsArray = [];
									var tempIds = ($("#perc_template_list").val() != "") ? $("#perc_template_list").val().split(',') : "";
									for(i = 0; i < summaries.length; i++)
									{
										temps[summaries[i].id] = summaries[i].name;
									}
									for(i = 0; i < tempIds.length; i++)
									{
										tempsArray[i] = temps[tempIds[i]];
									}
									tempsArray.sort();
									var buff = "";
									for(i = 0; i < tempsArray.length; i++)
									{
										if(i > 0)
											buff += "<br/>";
										buff += tempsArray[i];
									}
									$("#perc_display_template_list").append(buff);
								}
								else
								{
									var defaultMsg =
											$.PercServiceUtils.extractDefaultErrorMessage(result.request);
									$.perc_utils.alert_dialog({title: 'Error', content: defaultMsg});
								}
							}
					);
				}
			});
			]]>
                </script>
		<div id = "perc-querylist-wrapper">
			<div id = "querylist-title-wrapper">
				<div id = "querylist-title" class = "querylist-close-image">Archive Criteria</div>
			</div>
			<div id="criteria_for_list">
				<label for="display_title_contains">Title contains:</label>
				<br/>
				<div class="datadisplay" id="perc_display_title_contains"><xsl:value-of select="//DisplayField/Control[@paramName='query_title_contains']/Value"/></div>
				<br/>
				<label for="display_query_published_after">Posted on or after:</label>
				<br/>
				<div class="datadisplay" id="perc_display_query_published_after"><xsl:value-of select="//DisplayField/Control[@paramName='query_published_after']/Value"/></div>
				<br/>
				<label for="display_query_published_before">Posted before:</label>
				<br/>
				<div class="datadisplay" id="perc_display_query_published_before"><xsl:value-of select="//DisplayField/Control[@paramName='query_published_before']/Value"/></div>
				<br/>
				<label for="display_site_path">Website location:</label>
				<br/>
				<input type="hidden" id="perc_site_path">
					<xsl:attribute name="value"><xsl:value-of select="//DisplayField/Control[@paramName='query_site_path']/Value"/></xsl:attribute>
				</input>
				<div class="datadisplay" id="perc_display_site_path"></div>
				<br/>
				<label for="display_query_published_before">Template:</label>
				<br/>
				<input type="hidden" id="perc_template_list">
					<xsl:attribute name="value"><xsl:value-of select="//DisplayField/Control[@paramName='query_template_list']/Value"/></xsl:attribute>
				</input>
				<div class="datadisplay" id="perc_display_template_list">
				</div>
				<br/>
				<label for="perc_display_query_page_result">Results page:</label>
				<br/>
				<div class="datadisplay" id="perc_display_query_page_result"><xsl:value-of select="//DisplayField/Control[@paramName='archive_page_result']/Value"/></div>
			</div>
		</div>
	</xsl:template>
</xsl:stylesheet>
