<?xml version="1.0" encoding="UTF-8"?>
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
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxctl="URN:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n">
    <xsl:template match="/"/>
    <!--
     percGoogleCalendarControl
 -->
    <psxctl:ControlMeta name="percGoogleCalendarControl" dimension="single" choiceset="none">>
        <psxctl:Description>Provides UI for adding Google Calendars</psxctl:Description>
        <psxctl:ParamList>
            <psxctl:Param name="id" datatype="String" paramtype="generic">
                <psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
            </psxctl:Param>
            <psxctl:Param name="class" datatype="String" paramtype="generic">
                <psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
                <psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
            </psxctl:Param>
            <psxctl:Param name="style" datatype="String" paramtype="generic">
                <psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
            </psxctl:Param>
            <psxctl:Param name="columncount" datatype="String" paramtype="generic">
                <psxctl:Description>This parameter specifies the number of column(s) displayed.</psxctl:Description>
                <psxctl:DefaultValue>1</psxctl:DefaultValue>
            </psxctl:Param>
            <psxctl:Param name="columnwidth" datatype="String" paramtype="generic">
                <psxctl:Description>This parameter specifies the width of the column in pixels or percentage.</psxctl:Description>
                <psxctl:DefaultValue>100%</psxctl:DefaultValue>
            </psxctl:Param>
            <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
                <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
                <psxctl:DefaultValue>400</psxctl:DefaultValue>
            </psxctl:Param>
            <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
                <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
                <psxctl:DefaultValue>200</psxctl:DefaultValue>
            </psxctl:Param>
        </psxctl:ParamList>
        <psxctl:AssociatedFileList>
            <psxctl:FileDescriptor name="all.css" type="css" mimetype="text/css">
                <psxctl:FileLocation>../../cm/jslib/profiles/3x/libraries/fontawesome/css/all.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="percCalendarTwo.css" type="css" mimetype="text/css">
                <psxctl:FileLocation>../rx_resources/widgets/percCalendarTwo/css/percCalendarTwo.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="jquery.minicolors.css" type="css" mimetype="text/css">
                <psxctl:FileLocation>../rx_resources/widgets/percCalendarTwo/css/jquery.minicolors.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="percCalendarTwo.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../rx_resources/widgets/percCalendarTwo/js/percCalendarTwo.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="jquery.minicolors.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../rx_resources/widgets/percCalendarTwo/js/jquery.minicolors.js</psxctl:FileLocation>
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
            <psxctl:FileDescriptor name="PercSiteService.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/services/PercSiteService.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="PercServiceUtils.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/services/PercServiceUtils.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
        </psxctl:AssociatedFileList>

    </psxctl:ControlMeta>
    <xsl:template match="Control[@name='percGoogleCalendarControl']" mode="psxcontrol">
        <input type="hidden" name="{@paramName}" id="perc-content-edit-{@paramName}" value="{Value}"/>
        <span id="perc-google-setup-toggle" class="perc-toggle-google-setup fa fa-cogs" title="Click to toggle Google Calendar Setup (Calendar 2.0 Only)"></span><label for="perc-google-setup-toggle">Click to toggle Google Calendar Setup <i>(Calendar 2.0 Only)</i></label>
        <div class="perc-google-calendar" id="{@paramName}">
            <h2>Google Calendar Setup</h2>
            <span id="perc-help-toggle" class="perc-toggle-help fa fa-question-circle" title="Click to toggle help"></span><label for="perc-help-toggle">Click to toggle help.</label>
            <div class="perc-help">
                Integration of Google Calendar sources requires the following information:

                <ul>
                    <li>The calendar name as you would like it appear on your page.</li>
                    <li>The ID of the Google Calendar you would like to add.</li>
                    <li>A Google Calendar API Key.</li>
                </ul>
                If all of the Google Calendars you are planning to integrate are managed by the same Google account, the same API key can be used for each calendar.  However, if the they are managed by separate Google accounts, it is a best practice to create an API key for each calendar within the calendar owner accounts.<br /><br />
                The steps below must be followed to enable Google Calendar integration.
                <ul>
                    <li><b>Generate a Google Calendar API key:</b>
                        <ul>
                            <li>Navigate to the <a href="https://console.developers.google.com/" target="_blank" rel="noopener noreferrer">Google Developer Console</a> and create a new project.</li>
                            <li>Enter the newly created project and select <b>Library</b> on the sidebar.</li>
                            <li>*Find &quot;Calendar API&quot; in the list and set it to ENABLE.</li>
                            <li>On the sidebar, select Credentials and choose <b>Create credentials &gt; API key</b> and then select &quot;Browser key&quot;</li>
                            <li>To control your API key usage, it is recommended to enter the domains that will be hosting your calendar.</li>
                            <li>The new API key will be generated, but may take a short period of time before it can be actively used.</li>
                        </ul>
                    </li>
                    <br />
                    <li><b>Make your Google Calendar public:</b>
                        <ul>
                            <li>Within Google Calendar settings, select &quot;Calendars&quot;, and choose the calendar you would like to configure.</li>
                            <li>Select &quot;Share this Calendar&quot;.</li>
                            <li>Check the &quot;Make this calendar public&quot; option.</li>
                            <li>&quot;See all event details&quot; should be chosen from the dropdown.</li>
                            <li>Click the save button.</li>
                        </ul>
                    </li>
                    <br />
                    <li><b>Retrieve your Google Calendar ID:</b>
                        <ul>
                            <li>Within Google Calendar settings, select &quot;Calendars&quot;, and choose the calendar you would like to view.</li>
                            <li>Across from the <b>Calendar Address:</b> section, next to the <b>ICAL</b> and <b>HTML</b> buttons, will be your Calendar ID.</li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div id="perc-google-calendar-setup-editor" class="perc-google-calendar-setup-editor">
                <span class="perc-table-add fa fa-plus-square"></span>
                <table class="perc-table">
                    <tbody>
                        <tr><td class="perc-input-header">*Calendar Name</td><td class="perc-input-header">*Calendar ID</td><td class="perc-input-header">*API Key</td><td class="perc-input-header">*Background Color</td><td class="perc-input-header">*Text Color</td><td></td></tr>
                        <!-- This is the template table row -->
                        <tr class="hide perc-google-calendar-row" role="presentation">
                            <td class="perc-input-td">
                                <input id="perc-google-calendar-name" class="perc-google-calendar-name" type="text"></input>
                            </td>
                            <td class="perc-input-td">
                                <input id="perc-google-calendar-id" class="perc-google-calendar-id" type="text"></input>
                            </td>
                            <td class="perc-input-td">
                                <input id="perc-google-calendar-api-key" class="perc-google-calendar-api-key" type="text"></input>
                            </td>
                            <td class="perc-input-td">
                                <input id="perc-google-calendar-background-color" class="perc-google-calendar-background-color" data-control="hue" value="#3a87ad" type="text"></input>
                            </td>
                            <td class="perc-input-td">
                                <input id="perc-google-calendar-text-color" class="perc-google-calendar-text-color" data-control="hue" value="#ffffff" type="text"></input>
                            </td>
                            <td class="perc-input-td">
                                <span class="perc-table-remove fa fa-minus"></span>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

    </xsl:template>
    <xsl:template match="Control[@name='percGoogleCalendarControl' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
        <input type="hidden" name="{@paramName}" id="perc-content-edit-{@paramName}" value="{Value}"/>
        <div class="perc-google-calendar-readonly" id="{@paramName}">
            <div style="display:none;">TODO: Finish ME!</div>
        </div>
    </xsl:template>
</xsl:stylesheet>
