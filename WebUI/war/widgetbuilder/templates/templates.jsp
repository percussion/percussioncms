    <%--
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
  --%>

<!-- Template for widget fields -->
    <script type="text/template" id="perc-widget-fields-collection-template">
        <div class="perc-widget-field-wrapper">
            <div class="perc-widget-field-entry" title="<@- name @>"><@- name @></div>
            <div class="perc-widget-field-entry" title="<@- label @>"><@- label @></div>
            <div class="perc-widget-field-entry"><@- type.toLowerCase().replace("_", " ") @></div>
            <div class="perc-widget-field-entry"><div class="perc-widget-field-actions" for="<@- name @>" style="display:none"><span class="perc-widget-field-action-edit" style="cursor:pointer"><i18n:message key="perc.ui.widget.builder@Edit"/></span> | <span class="perc-widget-field-action-delete" style="cursor:pointer"><i18n:message key="perc.ui.widget.builder@Delete"/></span></div>
        </div>
    </script>

	<!-- Template for General tab of widget builder -->
    <script type="text/template" id="perc-widget-general-tab-template">
        <form name="perc-widget-general-tab-form">
            <div type="sys_normal">
                <label accesskey="" for="widgetname" class="perc-required-field"><i18n:message key="perc.ui.widget.builder@Name"/>:</label><br/>
                <input <@- widgetname != ''?'readonly=readonly':'' @> <@- widgetname != ''?'class=perc-disabled-input':'class="datadisplay"' @> type="text" name="widgetname" size="50" maxlength="100" style="height: 15px; padding-top: 3px; padding-bottom: 5px;" value="<@- widgetname @>"/>
                <br/>
            </div>
            <div type="sys_normal">
                <label accesskey="" for="description"><i18n:message key="perc.ui.widget.builder@Description"/>:</label><br/>
                <textarea name="description" wrap="soft" class="datadisplay" rows="4" cols="80" maxlength="1024"><@- description @></textarea>
                <br/>
            </div>
            <div type="sys_normal">
                <label accesskey="" for="prefix" class="perc-required-field"><i18n:message key="perc.ui.widget.builder@Prefix"/>:</label><br/>
                <input type="text" name="prefix" class="datadisplay" size="50" maxlength="100" style="height: 15px; padding-top: 3px; padding-bottom: 5px;" value="<@- prefix @>"/>
                <br/>
            </div>
            <div type="sys_normal">
                <label accesskey="" for="author" class="perc-required-field"><i18n:message key="perc.ui.widget.builder@Author"/>:</label><br/>
                <input type="text" name="author" class="datadisplay" size="50" maxlength="100" style="height: 15px; padding-top: 3px; padding-bottom: 5px;" value="<@- author @>"/>
                <br/>
            </div>
            <div type="sys_normal">
                <label accesskey="" for="publisherUrl" class="perc-required-field"><i18n:message key="perc.ui.widget.builder@Publisher URL"/>:</label><br/>
                <input type="text" name="publisherUrl" class="datadisplay" size="50" maxlength="100" style="height: 15px; padding-top: 3px; padding-bottom: 5px;" value="<@- publisherUrl @>"/>
                <br/>
            </div>
            <div type="sys_normal">
                <label accesskey="" for="version" class="perc-required-field"><i18n:message key="perc.ui.widget.builder@Version"/>:</label><br/>
                <input type="text" name="version" class="datadisplay" size="50" maxlength="50" style="height: 15px; padding-top: 3px; padding-bottom: 5px;" value="<@- version @>"/>
                <br/>
            </div>
            <div type="sys_normal">
                <label accesskey="" for="widgetTrayCustomizedIconPath" ><i18n:message key="perc.ui.widget.builder@Widget Tray Icon Path"/>:</label><br/>
                <input type="text" name="widgetTrayCustomizedIconPath" class="datadisplay" size="50" maxlength="100" style="height: 15px; padding-top: 3px; padding-bottom: 5px;" value="<@- widgetTrayCustomizedIconPath @>"/>
                <br/>
            </div>
            <div type="sys_normal">
                <label accesskey="" for="toolTipMessage" ><i18n:message key="perc.ui.widget.builder@ToolTip Message"/>:</label><br/>
                <input type="text" name="toolTipMessage" class="datadisplay" size="50" maxlength="100" style="height: 15px; padding-top: 3px; padding-bottom: 5px;" value="<@- toolTipMessage @>"/>
                <br/>
            </div>
            <div type="sys_normal">
                <input type="checkbox" name="responsive" class="datadisplay" style="height: 15px; padding-top: 3px; padding-bottom: 5px; vertical-align:middle" <@ print(responsive==true?checked='checked':'') @>/><label accesskey="" for="isResponsive"><i18n:message key="perc.ui.widget.builder@Is Responsive"/></label>
                <br/>
            </div>
        </form>
    </script>

    <!-- Template for field editor dialog of widget builder -->
    <script type="text/template" id="perc-widget-field-editor-template">
        <form name="perc-widget-field-editor-form">
            <div type="sys_normal">
                <label accesskey="" for="name" class="perc-required-field"><i18n:message key="perc.ui.widget.builder@Name"/>:</label><br/>
                <input <@- name != ''?'readonly=readonly':'' @> <@- name != ''?'class=perc-disabled-input':'class="datadisplay"' @> type="text" name="name" size="50" maxlength="50" style="height: 15px; padding-top: 3px; padding-bottom: 5px;" value="<@- name @>">
                <br/>
            </div>
            <div type="sys_normal">
                <label accesskey="" for="label" class="perc-required-field"><i18n:message key="perc.ui.widget.builder@Label"/>:</label><br/>
                <input type="text" name="label" class="datadisplay" size="50" maxlength="50" style="height: 15px; padding-top: 3px; padding-bottom: 5px;" value="<@- label @>">
                <br/>
            </div>
            <div type="sys_normal">
                <label accesskey="" for="type" class="perc-required-field"><i18n:message key="perc.ui.widget.builder@Type"/>:</label><br/>
                <@ if(name == '') { @>
                    <select class="datadisplay" name="type">
                        <option <@- type == 'TEXT'?'selected="selected"':''@> value="TEXT"><i18n:message key="perc.ui.widget.builder@Text"/></option>
                        <option <@- type == 'RICH_TEXT'?'selected="selected"':''@> value="RICH_TEXT"><i18n:message key="perc.ui.widget.builder@Rich Text"/></option>
                        <option <@- type == 'DATE'?'selected="selected"':''@> value="DATE"><i18n:message key="perc.ui.widget.builder@Date"/></option>
                        <option <@- type == 'TEXT_AREA'?'selected="selected"':''@> value="TEXT_AREA"><i18n:message key="perc.ui.widget.builder@Textarea"/></option>
                        <option <@- type == 'FILE'?'selected="selected"':''@> value="FILE"><i18n:message key="perc.ui.widget.builder@File"/></option>
                        <option <@- type == 'IMAGE'?'selected="selected"':''@> value="IMAGE"><i18n:message key="perc.ui.widget.builder@Image"/></option>
                        <option <@- type == 'PAGE'?'selected="selected"':''@> value="PAGE"><i18n:message key="perc.ui.widget.builder@Page"/></option>
                    </select>
                <@ } else {@>
                    <!-- top input field contains the actual value, the bottom is for display purposes only to handle localization -->
                    <input value="<@ if(type == 'TEXT'){ @>text<@ }else if(type == 'RICH_TEXT'){ @>rich text<@ }else if(type == 'DATE'){ @>date<@ }else if(type == 'TEXT_AREA'){ @>textarea<@ }else if(type == 'FILE'){ @>file<@ }else if(type == 'IMAGE'){ @>image<@ }else if(type == 'PAGE'){ @>page<@ } @>" readonly="readonly" class="perc-disabled-input" type="hidden" name="type" size="50" maxlength="255" style="height: 15px; padding-top: 3px; padding-bottom: 5px;">
                    <input value="<@ if(type == 'TEXT'){ @><i18n:message key="perc.ui.widget.builder@Text"/><@ }else if(type == 'RICH_TEXT'){ @><i18n:message key="perc.ui.widget.builder@Rich Text"/><@ }else if(type == 'DATE'){ @><i18n:message key="perc.ui.widget.builder@Date"/><@ }else if(type == 'TEXT_AREA'){ @><i18n:message key="perc.ui.widget.builder@Textarea"/><@ }else if(type == 'FILE'){ @><i18n:message key="perc.ui.widget.builder@File"/><@ }else if(type == 'IMAGE'){ @><i18n:message key="perc.ui.widget.builder@Image"/><@ }else if(type == 'PAGE'){ @><i18n:message key="perc.ui.widget.builder@Page"/><@ } @>" readonly="readonly" class="perc-disabled-input" type="text" name="type-display" size="50" maxlength="255" style="height: 15px; padding-top: 3px; padding-bottom: 5px;">
                <@ } @>
                <br/>
            </div>
        </form>
    </script>

	<!-- Template for display tab of widget builder -->
    <script type="text/template" id="perc-widget-display-editor-template">
        <form name="perc-widget-display-tab-form">
            <div type="sys_normal">
                <textarea name="widgetHtml" wrap="soft" class="datadisplay" rows="25" cols="110"><@- widgetHtml @></textarea>
                <br/>
            </div>
        </form>
    </script>

    <!-- Template for display tab of widget builder -->
    <script type="text/template" id="perc-widget-resource-item-editor-template">
        <form>
            <div type="sys_normal" class="perc-widget-resource">
                <input type="text" class="datadisplay perc-resource-entry-field" size="50" maxlength="255" style="height: 15px; padding-top: 3px; padding-bottom: 5px;" value="<@- name @>"><span class="perc-resource-delete perc-font-icon resource-tab-button-background icon-remove" title='<i18n:message key="perc.ui.widget.builder@Remove Resource"/>'></span>
                <br/>
            </div>
        </form>
    </script>
