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
  ~      https://www.percusssion.com
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
     percCalendarWidgetControl
 -->
    <psxctl:ControlMeta name="percCalendarWidgetControl" dimension="single" choiceset="none">
        <psxctl:Description>The control for calendar widget</psxctl:Description>
        <psxctl:ParamList>
        </psxctl:ParamList>
        <psxctl:AssociatedFileList>
        <!-- CSS-->
            <psxctl:FileDescriptor name="ui.dynatree.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>../../cm/css/dynatree/skin/ui.dynatree.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="percQueryControl.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>../rx_resources/controls/percQueryControl/css/percQueryControl.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>
<!-- JavaScript -->
            <psxctl:FileDescriptor name="jquery.jeditable.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/plugins/jquery-jeditable/jquery.jeditable.js</psxctl:FileLocation>
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
        <psxctl:FileDescriptor name="perc_path_manager.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>../../cm/plugins/perc_path_manager.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
        </psxctl:FileDescriptor>
        <psxctl:FileDescriptor name="perc_utils.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/plugins/perc_utils.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
        </psxctl:FileDescriptor>
        <psxctl:FileDescriptor name="perc_save_as.js" type="script" mimetype="text/javascript">
            <psxctl:FileLocation>../../cm/widgets/perc_save_as.js</psxctl:FileLocation>
            <psxctl:Timestamp/>
        </psxctl:FileDescriptor>
        </psxctl:AssociatedFileList>
    </psxctl:ControlMeta>

    <xsl:template match="Control[@name='percCalendarWidgetControl']" mode="psxcontrol">
        <xsl:if test="//DisplayError/Details/FieldError[@submitName='calendar_unique_name']">
            <input type="hidden" id="calendar_unique_name_error">
                <xsl:attribute name="value"><xsl:value-of select="//DisplayError/Details/FieldError[@submitName='calendar_unique_name']"/></xsl:attribute>
            </input>
        </xsl:if>
        <label class = "perc-required-field" for="display_sys_title">Name in finder:</label><br />
        <input class = "datadisplay" size = "50" type="text" name="display_sys_title" id="display_sys_title"/><br />
        <xsl:if test="/ContentEditor/DisplayError/Details/FieldError[@submitName='sys_title']">
            <label class="perc_field_error"><xsl:value-of select="/ContentEditor/DisplayError/Details/FieldError[@submitName='sys_title']"/></label><br /><br />
        </xsl:if>
        <label for="display_full_calendar">Full calendar page: (for mini calendar setup only)</label><br />
        <input class = "datadisplay" size = "50" type="text" name="full_calendar_page" id="full_calendar_page" value="{Value}"/>
        <span id='perc_querylist_resultspage_browse'>Browse</span>
        <script >
        (function($)
        {
            $(document).ready(function() {
                //If there is an error on sys_title, the error message needs to be shown below display_sys_title field
                //and hide the sys_title field.
                var calName = $('#perc-content-edit-sys_title').val();
                $("#display_sys_title").val(calName).attr('maxlength', 49);
                //handle the uniqueness of the form name here.
                if($("#calendar_unique_name_error").length)
                {
                    if(!$('div.perc_field_error').length){
                        var errorText = $("#calendar_unique_name_error").val();
                        $("label[for = 'display_full_calendar']").before('<div class = "perc_unique_name perc_field_error"/>');
                        $('.perc_unique_name').append(errorText);
                    }
                }

               //Text auto fill and filter settings for form fields
               {
                    var calendarTitle = $('#perc-content-edit-calendar_title');
                    var calendarName = $('#display_sys_title');
                    $.perc_textAutoFill(calendarTitle, calendarName, $.perc_autoFillTextFilters.URL, null, 49);
               }

            //Handle the results page browse button click
                $("#perc_querylist_resultspage_browse").on("click",function(){
                     var dlgTitle = "Select Full Calendar Page"
                     var inputElemId = "full_calendar_page";
                     handleBrowseButtonClick(dlgTitle, inputElemId );
                });
                $.topFrameJQuery.PercContentPreSubmitHandlers.addHandler(updateCalendarName);
            });


            //Function to handle click on browse button.
            function handleBrowseButtonClick(dlgTitle, inputElemId)
            {
                $.perc_browser
                        ({
                            on_save: function(spec, closer, show_error)
                            {
                                var pagePath = spec.path;
                                pagePath = pagePath.replace("/Sites/", "");
                                var chopStartPosition = pagePath.indexOf("/");
                                pagePath = pagePath.substring(chopStartPosition);
                                $("#" + inputElemId).val(pagePath);
                                //Update the hidden field for page result
                                $('[name="archive_page_result"]').val(pagePath);
                                closer();
                            },
                            new_asset_option: false,
                            selectable_object: "leaf",
                            new_folder_opt: false,
                            displayed_containers: "Sites",
                            asset_name: I18N.message( "perc.ui.saveasdialog.label@Selected Page:" ),
                            title: dlgTitle,
                            save_class: 'perc-save'
                        });
            }

            // A helper method called on form submission for validation
            function updateCalendarName()
            {
                $("#perc-content-edit-calendar_unique_name").val($("#perc-content-edit-sys_title").val());
                var calName = $('#display_sys_title').val();
                $("#perc-content-edit-sys_title").val(calName);
                return true;
            }
         })(jQuery);
        </script>
        <style type = "text/css">
            #perc_querylist_resultspage_browse {
                margin-top: 6px;
            }
        </style>
    </xsl:template>
    <xsl:template match="Control[@name='percCalendarWidgetControl' and @isReadOnly='yes']" priority='10' mode="psxcontrol">
            <label for="display_sys_title">Name in finder:</label><br />
            <div id = "perc-readonly-calendar_name" class = "datadisplay"></div>
            <input style = "display:none" class = "datadisplay" size = "50" type="text" name="display_sys_title" id="display_sys_title"/><br />
            <label for="display_full_calendar">Full calendar page: (for mini calendar setup only)</label><br />
            <div id = "perc-readonly-full_calendar_page" class = "datadisplay"></div>
            <input style = "display:none" class = "datadisplay" size = "50" type="text" name="full_calendar_page" id="full_calendar_page" value="{Value}"/>
        <script >
        (function($) {
            $(document).ready(function() {
                var calName = $('#perc-content-edit-sys_title').val();
                $("#display_sys_title").val(calName);
                $("#perc-readonly-calendar_name").text($('#display_sys_title').val());
                $("#perc-readonly-full_calendar_page").text($('#full_calendar_page').val());
            });
        })(jQuery);
        </script>
    </xsl:template>
</xsl:stylesheet>
