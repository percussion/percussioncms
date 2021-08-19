<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
        ]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:psxctl="urn:percussion.com/control"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">

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
                <psxctl:FileLocation>/cm/cssMin/perc_webmgt.packed.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="ui.datepicker.css" type="css" mimetype="text/css">
                <psxctl:FileLocation>/cm/themes/smoothness/ui.datepicker.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="PercDatetimePicker.css" type="css" mimetype="text/css">
                <psxctl:FileLocation>/cm/css/PercMultiDropDown.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>

            <!-- JavaScript -->
            <psxctl:FileDescriptor name="dropdownchecklist.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-ui-multiselect-widget/jquery.multiselect.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="perc_utils.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>/cm/plugins/perc_utils.js</psxctl:FileLocation>
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
        <script src="/Rhythmyx/rx_resources/js/calendarControl.js"/>
    </xsl:template>
    <xsl:template match="Control[@name='percCalendarControl' and @isReadOnly='yes']" priority='10' mode="psxcontrol">
        <div id = "perc-content-edit-page_calendar" style = "padding-bottom:10px;" class="ui-perc-ctl_daterange">
            <table style = "line-height:17px;">
                <tr>
                    <td>
                        <label for="{@paramName}">Select calendar:<br/></label>
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
        <script type="text/javascript">
            $(function() {
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
