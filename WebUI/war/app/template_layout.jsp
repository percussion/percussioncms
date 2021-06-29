<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
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

<%
    String locale= PSRoleUtilities.getUserCurrentLocale();
    String lang="en";
    if(locale==null){
        locale="en-us";
    }else{
        if(locale.contains("-"))
            lang=locale.split("-")[0];
        else
            lang=locale;
    }
    String debug = request.getParameter("debug");
    boolean isDebug = "true".equals(debug);
    String debugQueryString = isDebug ? "?debug=true" : "";
    String site = request.getParameter("site");
    if (debug == null)
        debug = "false";
%>
<i18n:settings lang="<%= locale %>" prefixes="perc.ui." debug="<%=debug%>"/>
<div id="perc-layout-menu">
    <table style = 'width:100%; min-width:983px'>
        <tr>
            <td style = 'width:43%; min-width:558px'>

                <div id="perc-dropdown-actions-layout" style="float:left;"></div>
                <div id="perc-dropdown-view-layout" style="float:left;"></div>

                <div id="dragger-tool">&nbsp;</div>
                <div class="perc-lib-expander-div">
                        <span id="perc-wid-lib-expander">
                            <a id="perc-wid-lib-maximizer" href="#" style="float: left;"></a>
                            <span><i18n:message key = "perc.ui.template.layout@Add Widget"/></span>                
                        </span>
                </div>

                <!-- Explore Regions Expander Link-->
                <div class="perc-lib-expander-div">
                        <span id="perc-region-library-expander">
                            <a id="perc-region-library-maximizer" href="#" style="float: left;"></a>
                            <span><i18n:message key = "perc.ui.template.layout@Explore Regions"/></span>                
                        </span>
                </div>
                <div id="perc-dropdown-help-layout" style="float:left;line-height:31px;"></div>
                <div id='perc-error-alert' title = 'Errors' style="float:left;line-height:31px;">
                </div>
            </td>
            <td style = 'width:14%; min-width:135px'>
                <div id = 'perc-region-tool-wrapper'>
                    <div id='region-tool'>
                        <img src='../images/images/iconAddCell.gif' alt="Click and Drag to Add a Region" title ="<i18n:message key = "perc.ui.template.layout@Drag and drop"/>" onMouseOver="this.src='../images/images/iconAddCellOver.gif';this.style.cursor='pointer';" onMouseOut="this.src='../images/images/iconAddCell.gif';" draggable="true" aria-dropeffect="execute"/>
                    </div>
                    <div id='region-tool-disabled' style = 'display:none' aria-hidden="true">
                        <img src='../images/images/iconAddCellGray.png' alt="<i18n:message key = "perc.ui.template.layout@Drag and drop disabled"/>" title ="<i18n:message key = "perc.ui.template.layout@Drag and drop"/>" />
                    </div>
                    <div id='perc-region-tool-inspector' title = "<i18n:message key = "perc.ui.template.layout@Design Inspector"/>"></div>
                    <div id = 'perc-region-tool-menu'></div>
                    <div id='perc-undo-tool' title = "<i18n:message key = "perc.ui.template.layout@Undo"/>" style = 'display:none'>
                    </div>
                    <div id='perc-undo-tool-disabled' title = "<i18n:message key = "perc.ui.template.layout@Undo"/>" style = 'display:none'></div>
                </div>
            </td>
            <td style = 'width:43%'>
                <button id="perc-save" class="btn btn-primary" type="button" name="perc_wizard_save" ><i18n:message key ="perc.ui.button@Save"/></button>
                <button id="perc-layout-cancel" class="btn btn-primary" type="button" name="perc_wizard_cancel"><i18n:message key ="perc.ui.common.label@Cancel"/></button>
            </td>
            <div class = 'perc-overlay-div' style = 'display:none'></div>
        </tr>
    </table>
</div>
<div id='perc-widget-library' class='perc-template-container perc-hidden' style="clear:both;">
    <div class="perc-widget-filter">
        <label><i18n:message key = "perc.ui.dashboard@Type"/></label>
        <select role="listbox" class="perc-widget-type" style="width:150px; margin-right: 35px;">
            <option role="listitem" value="all"><i18n:message key = "perc.ui.template.layout@View all"/></option>
            <option role="listitem" selected="true" value="percussion">Percussion</option>
            <option role="listitem" value="community"><i18n:message key = "perc.ui.template.layout@Community"/></option>
            <option role="listitem" value="custom"><i18n:message key = "perc.ui.template.layout@Custom"/></option>
        </select>
        <label><i18n:message key = "perc.ui.dashboard@Category"/></label>
        <select class="perc-widget-category" role="listbox" style="width:150px">
            <option role="listitem" class="perc-widget-category-default" selected="true" value="all"><i18n:message key = "perc.ui.template.layout@View all"/></option>
            <option role="listitem" class="perc-widget-category-predefined" value="blog"><i18n:message key = "perc.ui.dashboard@Blog"/></option>
            <option role="listitem" class="perc-widget-category-predefined" value="content"><i18n:message key = "perc.ui.dashboard@Content"/></option>
            <option role="listitem" class="perc-widget-category-predefined" value="integration"><i18n:message key = "perc.ui.dashboard@Integration"/></option>
            <option role="listitem" class="perc-widget-category-predefined" value="navigation"><i18n:message key = "perc.ui.navMenu.architecture@Architecture"/></option>
            <option role="listitem" class="perc-widget-category-predefined" value="rich media"><i18n:message key = "perc.ui.template.layout@Rich Media"/></option>
            <option role="listitem" class="perc-widget-category-predefined" value="search"><i18n:message key = "perc.ui.dashboard@Search"/></option>
            <option role="listitem" class="perc-widget-category-predefined" value="social"><i18n:message key = "perc.ui.dashboard@Social"/></option>
        </select>
    </div>
    <div class="perc-widget-list">
        <!-- This is generated dynamically from the controller -->
    </div>
</div>

<!-- Explore Regions Tray-->
<div id='perc-region-library' class='perc-region-library-container perc-hidden' style="clear:both;">
    <!-- This is generated dynamically from the controller -->
</div>

<!-- Explore Orphan Assets Tray-->
<div id='perc-orphan-assets' class='perc-orphan-assets-container perc-hidden' style="clear:both;">
    <!-- This is generated dynamically from the controller -->
</div>

<div id="template_layout_root" style="margin:auto; max-width : 1000px">
</div>
<div id="perc-menu" style="display:none;" >
    <img src="../images/templates/edit.gif" id="perc-edit" alt="Click to Edit" title="Edit"/>
    <img src="../images/templates/delete.gif" id="perc-close" alt="Click to Delete" title="Delete"/>
</div>

<div id="perc-delete-region-dialog" title=<i18n:message key = "perc.ui.layout.view@Delete Region"/> style="display:none;">
    <table>
        <tr>
            <td>  <i18n:message key = "perc.ui.template.layout@Delete Region Question"/> "<span id="perc-region-id-label">REGION-ID</span>"?            </td>
        </tr>
        <tr>
            <td style='text-align:left'>&nbsp;</td>
        </tr>
        <tr>
            <td style='text-align:left'>    <label id='perc-delete-content-choice-label'><input id='perc-delete-content-choice' type='checkbox' /><i18n:message key = "perc.ui.template.layout@Delete Content Of Region"/></label></td>
        </tr>
    </table>
</div>

<div id="perc-region-edit-rename-confirm-dialog" title=<i18n:message key = "perc.ui.page.general@Warning"/> style="display:none;">
    <table>
        <tr>
            <td><i18n:message key = "perc.ui.template.layout@Delete Content Of Region"/> "<span id="perc-region-edit-rename-confirm-dialog-id-label"></span>". <i18n:message key = "perc.ui.template.layout@What To Do"/></td>
        </tr>
        <tr>
            <td style='text-align:left'>&nbsp;</td>
        </tr>
        <tr>
            <td style='text-align:left'>
                <label class='perc-region-edit-rename-confirm-dialog-label'><input name='perc-region-edit-rename-confirm-dialog-choice' value='replaceedits' type='radio' style="width:20px;"  checked /><i18n:message key = "perc.ui.template.layout@Use my edits"/></label><br/>
                <label class='perc-region-edit-rename-confirm-dialog-label'><input name='perc-region-edit-rename-confirm-dialog-choice' value='ignoreedits' type='radio' style="width:20px;" /><i18n:message key = "perc.ui.template.layout@Ignore my edits"/></label>
            </td>
        </tr>
    </table>
</div>

<div id="perc-region-edit" title=<i18n:message key = "perc.ui.template.layout@Region Properties"/> style="display:none;">
    <span class="perc-required-legend"><label><i18n:message key = "perc.ui.general@Denotes Required Field"/></label></span>
    <div class="fieldGroup">
        <div id='perc-region-edit-properties-container'>
            <label class="perc-required-field" for="perc-region-name" accesskey=""><i18n:message key = "perc.ui.template.layout@Region Name"/></label><br/>
            <input id='perc-region-name' type='text' />
            <div id="perc-region-css-table_wrapper" class="dataTables_wrapper">
                <table style="padding: 0; border-spacing: 0; border-width: 0" id="perc-region-css-table">
                </table>
            </div>
            <p><a id="perc-new-css-property" href=""><i18n:message key = "perc.ui.template.layout@Add another CSS property"/></a></p>
        </div>
    </div>
    <div class="fieldGroup">
        <div id='perc-region-edit-attributes-container'>
            <div id="perc-region-attributes-table_wrapper" class="dataTables_wrapper">
                <table style="padding: 0; border-spacing: 0; border-width: 0" id="perc-region-attributes-table">
                </table>
            </div>
            <p><a id="perc-new-attribute" href=""><i18n:message key = "perc.ui.template.layout@Add another HTML Attribute"/></a></p>
        </div>
    </div>
    <div class="fieldGroup">
        <div id='perc-region-edit-css-container'>
            <label><i18n:message key = "perc.ui.template.layout@Region Root Class"/></label><br/>
            <label class="visuallyhidden" for="perc-region-cssClass"><i18n:message key = "perc.ui.template.layout@Search"/></label>
            <input id='perc-region-cssClass' type='text' placeholder='Enter root class from site theme' />
            <div id="perc-region-cssClass-hint"><i18n:message key = "perc.ui.template.layout@Seperate Classes"/></div>
        </div>
    </div>
    <div class="fieldGroup">
        <div id='perc-region-edit-cssoverrides-container'>
            <div id="perc-css-overrides-table_wrapper" class="dataTables_wrapper">
                <table style="padding: 0; border-spacing: 0; border-width: 0" id="perc-css-overrides-table">
                </table>
            </div>
            <div id="perc-css-overrides-disable-container">
                <input id="perc-css-overrides-disable" type="checkbox" /> <span id="perc-css-overrides-disable-span"><i18n:message key = "perc.ui.template.layout@Do Not Use CSS Overrides"/></span>
            </div>
        </div>
    </div>
</div>
<div id="perc-widget-edit" title="Widget Editor" style="width:500px;">
    <div id="perc-widget-property-form">
    </div>
</div>
<div id="perc-insert-region-helper">
</div>
