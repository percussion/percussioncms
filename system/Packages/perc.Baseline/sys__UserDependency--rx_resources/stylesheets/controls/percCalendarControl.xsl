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
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:psxctl="urn:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml"
                xmlns:psxi18n="com.percussion.i18n" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
<xsl:template match="/" />
<!--
     percQueryControl
 -->
    <psxctl:ControlMeta name="percCalendarControl" dimension="array" choiceset="required">
        <psxctl:Description>The control for calendars</psxctl:Description>
        <psxctl:ParamList>
        </psxctl:ParamList>
        <psxctl:AssociatedFileList>
        <!-- CSS-->
        <psxctl:FileDescriptor name="perc_webmgt.packed.css" type="css" mimetype="text/css">
            <psxctl:FileLocation>../../cm/cssMin/perc_webmgt.packed.css</psxctl:FileLocation>
            <psxctl:Timestamp/>
        </psxctl:FileDescriptor>        
        <psxctl:FileDescriptor name="ui.datepicker.css" type="css" mimetype="text/css">
            <psxctl:FileLocation>../../cm/themes/smoothness/ui.datepicker.css</psxctl:FileLocation>
            <psxctl:Timestamp/>
        </psxctl:FileDescriptor>
        <psxctl:FileDescriptor name="PercDatetimePicker.css" type="css" mimetype="text/css">
            <psxctl:FileLocation>../../cm/css/PercMultiDropDown.css</psxctl:FileLocation>
            <psxctl:Timestamp/>
        </psxctl:FileDescriptor>        
        
<!-- JavaScript -->            
        <psxctl:FileDescriptor name="dropdownchecklist.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/plugins/jquery-ui-multiselect-widget/jquery.multiselect.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
        <psxctl:FileDescriptor name="perc_utils.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/plugins/perc_utils.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
        </psxctl:FileDescriptor>
        </psxctl:AssociatedFileList>
    </psxctl:ControlMeta>
    
    <xsl:template match="Control[@name='percCalendarControl']" mode="psxcontrol">
        <div id = "perc-content-edit-page_calendar" class="ui-perc-ctl_daterange" for = "page_calendar">
            <table style = "line-height:17px;">
                    <tr>
                        <td>
                          <label>Select calendar:<br/></label>
                          <select multiple="1" id="{@paramName}" name="{@paramName}">
                             <xsl:apply-templates select="DisplayChoices" mode="psxcontrol-sysdropdownmultiple">
                                <xsl:with-param name="controlValue" select="Value"/>
                                <xsl:with-param name="paramName" select="@paramName"/>
                             </xsl:apply-templates>
                          </select>
                        </td>
                        <td width = "30px"></td>
                        <td> <label for="display_calendar_start_date">Start date:<br /></label>
                            <input readonly = "" size = "19"  type="text" name="display_calendar_start_date" style = "height:13px" id="display_calendar_start_date" class="perc-datetime-picker"/>
                        </td>
                        <td width = "10px"></td>
                        <td><label for="display_calendar_end_date">End date: <br /></label>
                            <input readonly = "" size = "19" type="text" name="display_calendar_end_date" id="display_calendar_end_date" class="perc-datetime-picker"/>
                        </td>
                    </tr>
            </table>    
        </div>
        <script >
            $(document).ready(function() {
                $("#page_calendar").multiselect({
                    height:175,
                    minWidth:225,
                    checkAllText: 'Select all',
                    uncheckAllText: 'Deselect all'
            });
                var startDate = $("#perc-content-edit-page_start_date").val().replace(':00.0', '');
                $('#display_calendar_start_date').val(startDate);
                var endDate = $("#perc-content-edit-page_end_date").val().replace(':00.0', '');
                $('#display_calendar_end_date').val(endDate);
                
                
                $('#display_calendar_start_date, #display_calendar_end_date').datepicker({
                    showTime:true,
                    timeFormat: 'hh:mm:ss.s',
                    stepHours: 1,
                    time24h: true,
                    stepMinutes: 10,
                    changeMonth:true,
                    changeyear:true,
                    constrainInput:true,
                    showOn: 'button', 
                    buttonImage: '../rx_resources/controls/percQueryControl/images/calendar.gif', 
                    buttonImageOnly: true,  
                    dateFormat: 'yy-mm-dd',
                    buttonText: ''
                }).bind('paste', function(evt){evt.preventDefault();})
                .bind('keypress keydown', function(evt){
                    if(evt.keyCode == 46 || evt.keyCode == 8 )
                    {
                        var field = evt.target;
                        field.value = "";
                        evt.preventDefault();
                        return;
                    }
                    if(evt.charCode == 0 || typeof(evt.charCode) == 'undefined')
                        return;                                     
                    evt.preventDefault();
                });
                
                $.topFrameJQuery.PercContentPreSubmitHandlers.addHandler(updateCalendar);
            });
            
            function updateCalendar()
            {
                $("#perc-content-edit-page_calendar").find('.perc_field_error').remove();
                var startDate = $('#display_calendar_start_date').val();
                $("#perc-content-edit-page_start_date").val(startDate);
                var endDate = $('#display_calendar_end_date').val();
                $("#perc-content-edit-page_end_date").val(endDate);
                if(startDate == '' &amp;&amp; endDate != '')
                {
                    $("#perc-content-edit-page_calendar").append('<label style="display: block;" generated="true" for="page_calendar" class="perc_field_error">Start date must be less than End date.</label>');
                    return false;
                }
                var p_start_date_temp = startDate.replace(/-|:| /g, ',').split(',');
                p_start_date = new Date(p_start_date_temp[0], p_start_date_temp[1]-1, p_start_date_temp[2], p_start_date_temp[3], p_start_date_temp[4]);
                var p_end_date_temp = endDate.replace(/-|:| /g, ',').split(',');
                p_end_date = new Date(p_end_date_temp[0], p_end_date_temp[1]-1, p_end_date_temp[2], p_end_date_temp[3], p_end_date_temp[4]);
                             
                if(p_start_date &gt;= p_end_date)
                {
                    $("#perc-content-edit-page_calendar").append('<label style="display: block;" generated="true" for="page_calendar" class="perc_field_error">Start date must be less than End date.</label>');
                    return false;
                }
                if($("#page_calendar").val() &amp;&amp; startDate == '')
                {
                    $("#perc-content-edit-page_calendar").append('<label style="display: block;" generated="true" for="page_calendar" class="perc_field_error">Start date must not be empty, if at least one calendar is selected.</label>');
                    return false;
                }
                return true;
            }
        </script>
    
    </xsl:template>
    <xsl:template match="Control[@name='percCalendarControl' and @isReadOnly='yes']" priority='10' mode="psxcontrol">
        <div id = "perc-content-edit-page_calendar" style = "padding-bottom:10px;" class="ui-perc-ctl_daterange" for = "page_calendar">
            <table style = "line-height:17px;">
                    <tr>
                        <td>
                          <label>Select calendar:<br/></label>
                          <div id = "selectedCalendars" class = "datadisplay"> </div>
                          <select multiple="1" id="{@paramName}" name="{@paramName}" style = "display:none">
                             <xsl:apply-templates select="DisplayChoices" mode="psxcontrol-sysdropdownmultiple">
                                <xsl:with-param name="controlValue" select="Value"/>
                                <xsl:with-param name="paramName" select="@paramName"/>
                             </xsl:apply-templates>
                          </select>
                        </td>
                        <td width = "30px"></td>
                        <td> <label for="display_calendar_start_date">Start date:<br /></label>
                            <div name="display_calendar_start_date" style = "width:138px; height:14px; margin-top:5px ; padding:4px;" id="display_calendar_start_date" class="perc-datetime-picker"></div>                            
                        </td>
                        <td width = "10px"></td>
                        <td><label for="display_calendar_end_date">End date: <br /></label>
                            <div name="display_calendar_end_date" style = "width:138px; height:14px;  margin-top:5px; padding:4px;"  id="display_calendar_end_date" class="perc-datetime-picker"></div>                            
                        </td>
                    </tr>
            </table>    
        </div>
        <script >
        $(document).ready(function() {
                var startDate = $("#perc-content-edit-page_start_date").val().replace(':00.0', '');
                $('#display_calendar_start_date').html(startDate);
                var endDate = $("#perc-content-edit-page_end_date").val().replace(':00.0', '');
                $('#display_calendar_end_date').html(endDate);                
                
                var selectedCalendars = $('select#page_calendar').val() + "";
                if(selectedCalendars != 'null') {
                    selectedCalendars = selectedCalendars.replace(/,/g, ", ");
                    $('div#selectedCalendars').html(selectedCalendars);
                }
            });
            
        </script>
    </xsl:template>
</xsl:stylesheet>
