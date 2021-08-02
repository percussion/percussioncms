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
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
        %HTMLspecial;
        ]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxctl="URN:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<xsl:template match="/" />
    <!--
         imageAutoListWidgetControl
     -->
    <psxctl:ControlMeta name="imageAutoListWidgetControl" dimension="single" choiceset="none">
		<psxctl:Description>The control for building the JCR query for the Image Auto List Widget:</psxctl:Description>
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
			<psxctl:FileDescriptor name="PercDatetimePicker.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>../../cm/css/PercDatetimePicker.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="jquery-ui-1.8.9.custom.css     " type="css" mimetype="text/css">
				<psxctl:FileLocation>../../cm/themes/smoothness/jquery-ui-1.8.9.custom.css     </psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="ui.datepicker.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>../../cm/themes/smoothness/ui.datepicker.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_webmgt.packed.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>../../cm/css/dynatree/skin/ui.dynatree.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_webmgt.packed.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>../rx_resources/widgets/imageAutoList/css/jquery.imageAutoList.css</psxctl:FileLocation>
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
			<psxctl:FileDescriptor name="PercServiceUtils.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/services/PercServiceUtils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercFinderTree.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/widgets/PercFinderTree.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercExtendUiDialog.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/plugins/PercExtendUiDialog.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
            <!-- Image Auto List Control JS-->
            <psxctl:FileDescriptor name="jquery.imageAutoList.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../rx_resources/widgets/imageAutoList/js/jquery.imageAutoList.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
		</psxctl:AssociatedFileList>
	</psxctl:ControlMeta>

	<xsl:template match="Control[@name='imageAutoListWidgetControl']" mode="psxcontrol">

		<script >
			$(function() {

			$('#display_start_date').datepicker({
			onSelect:
			function(value, date) {
			// *********************************************************************
			//This will be set from the widget config $date_format (set by velocity)
			// *********************************************************************
			$(this).trigger("focus");
			var p_start_date = new Date(value);
			var p_end_date = new Date($('[name="end_date"]').val());
			<xsl:text disable-output-escaping="yes">
				if(p_start_date > p_end_date)
                {
                    $.perc_utils.alert_dialog({title:"Error", content:"Created on or after date must be less than Created before date.", okCallBack:function(){
                        setDisplayDate($('[name="start_date"]').val(),"display_start_date");
                        return false;
                    }});
                }
                else
                {
                    $('[name="start_date"]').val(value);

                    setDisplayDate(value,"display_start_date");

                    buildQuery();
                }
								</xsl:text>
			// if the top most jquery is defined
			if($.topFrameJQuery != undefined)
			// mark the asset as dirty
			$.topFrameJQuery.PercDirtyController.setDirty(true, "asset");
			},
			showOn: 'button', buttonImage: '../rx_resources/widgets/imageAutoList/images/calendar.gif', buttonImageOnly: true, altFormat: 'yy-mm-dd'
			});

			$('#display_end_date').datepicker({
			onSelect:
			function(value, date) {
			$(this).focus();
			var p_start_date = new Date($('[name="start_date"]').val());
			var p_end_date = new Date(value);
			<xsl:text disable-output-escaping="yes">
				if(p_start_date >= p_end_date)
                {
                    $.perc_utils.alert_dialog({title:"Error", content:"Created before date must be greater than Created on or after date.", okCallBack:function(){
                        setDisplayDate($('[name="end_date"]').val(),"display_end_date");
                        return false;
                    }});
                }
                else
                {
                    $('[name="end_date"]').val(value);
                    setDisplayDate(value,"display_end_date");
                    buildQuery();
                }
								</xsl:text>
			// if the top most jquery is defined
			if($.topFrameJQuery != undefined)
			// mark the asset as dirty
			$.topFrameJQuery.PercDirtyController.setDirty(true, "asset");
			},
			showOn: 'button', buttonImage: '../rx_resources/widgets/imageAutoList/images/calendar.gif', buttonImageOnly: true, altFormat: 'yy-mm-dd'
			});

			$('#perc-content-form').imageAutoListControl({});
			});


		</script>

		<script >
			$(document).ready(function(){
                $("#autolist-title").on("click",function () {
                    $("#criteria_for_list").toggle();
                    $("#autolist-title").toggleClass("autolist-expand-image autolist-close-image");
                });
            });
		</script>
		<div id = "perc-autolist-wrapper">
			<div id = "autolist-title-wrapper">
				<div id = "autolist-title" class = "autolist-close-image"> Criteria for List Creation </div>
			</div>
			<div id="criteria_for_list">
				<div class="ui-perc-ctl_daterange">

					<table style = "line-height:17px;">
						<tr>
							<td> <label for="display_title_contains">Title contains:<br /></label>
								<input style = "width:300px" type="text" name="display_title_contains" id="display_title_contains"/></td>
							<td width = "30px"></td>
							<td> <label for="display_start_date">Created on or after:<br /></label>
								<input size = "16" type="text" name="display_start_date" id="display_start_date" class="date-pick"/></td>
							<td width = "10px"></td>
							<td><label for="display_end_date">Created before: <br /></label>
								<input size = "16" type="text" name="display_end_date" id="display_end_date" class="date-pick"/></td>
						</tr>
					</table>
				</div>
				<p></p>
				<div id = "perc-imageautolist-detail">
					<table style = "line-height:17px;">
						<tr>
							<td>
								<span class = "autolistlabel"><label for="perc-folder-selector" style="font-weight:bold">Asset library location:</label></span>
							</td>
						</tr>
						<tr>
							<td height = "265px" valign = "top">
								<div id="perc-folder-selector">
								</div>
							</td>
						</tr>
					</table>
				</div>
				<input type="hidden" aarenderer="NONE" class="datadisplay" id="perc-content-edit-query" name="query"/>
			</div>
		</div>

	</xsl:template>
	<xsl:template match="Control[@name='imageAutoListWidgetControl' and @isReadOnly='yes']" priority='10' mode="psxcontrol">
		<script >
	<![CDATA[
            $(document).ready(function(){
                $("#autolist-title").on("click",function () {
                    $("#criteria_for_list").toggle();
                    $("#autolist-title").toggleClass("autolist-expand-image autolist-close-image");
                });

                // Put asset library value in appropriate field
                var assetlibrarypath = $("#perc_asset_library_path").val().substring(26);
                var splitPath = assetlibrarypath.split("/");

                $("#perc_display_asset_library_path").text(assetlibrarypath);
            });
            ]]>
   </script>
		<div id = "perc-autolist-wrapper" >
			<div id = "autolist-title-wrapper">
				<div id = "autolist-title" class = "autolist-close-image"> Criteria for List Creation </div>
			</div>
			<div id="criteria_for_list">
				<label for="display_title_contains">Title contains:</label>
				<br/>
				<div class="datadisplay" id="perc_display_title_contains"><xsl:value-of select="//DisplayField/Control[@paramName='title_contains']/Value"/></div>
				<br/>
				<label for="display_start_date">Created on or after:</label>
				<br/>
				<div class="datadisplay" id="perc_display_start_date"><xsl:value-of select="//DisplayField/Control[@paramName='start_date']/Value"/></div>
				<br/>
				<label for="display_end_date">Created before:</label>
				<br/>
				<div class="datadisplay" id="perc_display_end_date"><xsl:value-of select="//DisplayField/Control[@paramName='end_date']/Value"/></div>
				<br/>
				<label for="display_asset_library_path">Asset library location:</label>
				<br/>
				<input type="hidden" id="perc_asset_library_path">
					<xsl:attribute name="value"><xsl:value-of select="//DisplayField/Control[@paramName='asset_library_path']/Value"/></xsl:attribute>
				</input>
				<div class="datadisplay" id="perc_display_asset_library_path"></div>
			</div>
		</div>
	</xsl:template>
</xsl:stylesheet>
