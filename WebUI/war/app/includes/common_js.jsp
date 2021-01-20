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

<% String jsProfile = request.getParameter("profile");
    if(jsProfile == null || jsProfile==""){ %>
<script src="../jslib/jquery.js"></script>
<script src="../jslib/json.js"></script>
<script src="../jslib/jquery.form.js"></script>
<script src="../jslib/jquery.blockui.js"></script>
<script src="../jslib/jquery.caret.js"></script>
<script src="../jslib/tools.scrollable-1.1.2.js"></script>
<script src="../jslib/jquery.dataTables.js"></script>
<script src="../jslib/jquery.xmldom-1.0.js"></script>

<!-- PSJSUtils.js must come after jQuery core js file -->
<script src="../plugins/perc_errors.js"></script>
<script src="../plugins/PercViewReadyManager.js"></script>
<script src="../jslib/PSJSUtils.js"></script>
<script src="../plugins/perc_sessionTimeout.js"></script>
<script src="../jslib/jquery.text-overflow.js"></script>
<script src="../plugins/perc_path_constants.js"></script>
<script src="../jslib/jquery-ui.js"></script>
<script src="../plugins/PercExtendUiDialog.js"></script>
<script src="../plugins/perc_utils.js"></script>
<script src="../jslib/jquery.bbq.js"></script>
<script src="../jslib/jquery.cookie.js"></script>
<script src="../jslib/jquery.dynatree.js"></script>
<script src="../services/PercServiceUtils.js"></script>
<script src="../jslib/jquery.layout.js"></script>
<script src="../jslib/jquery.metadata.js"></script>
<script src="../jslib/Jeditable.js"></script>
<script src="../jslib/jquery.validate.js"></script>
<script src="../plugins/perc_extend_jQueryValidate.js"></script>
<script src="../jslib/jquery.tooltip.js"></script>
<script src="../controllers/PercDirtyController.js"></script>
<script src="../plugins/PercNavigationManager.js"></script>
<script src="../plugins/PercFolderHelper.js"></script>
<script src="../plugins/PercFolderPropertiesDialog.js"></script>
<script src="../widgets/PercFinderTree.js"></script>
<script src="../plugins/PercPathSelectionDialog.js"></script>
<script src="../views/PercCreateNewAssetDialog.js"></script>
<script src="../jslib/jquery.autocomplete.js"></script>
<script src="../plugins/PercListEditorWidget.js"></script>
<script src="../jslib/hoverIntent.js"></script>
<script src="../jslib/superfish.js"></script>
<script src="../widgets/PercDropdown.js"></script>
<script src="../jslib/dropdownchecklist.js"></script>
<script src="../services/PercFolderService.js"></script>
<!-- Recent Service -->
<script src="../services/PercRecentListService.js"></script>
<!-- Redirect Handler -->
<script src="../plugins/PercRedirectHandler.js"></script>
<script src="../jslib/mousetrap.js"></script>
<% }else if(jsProfile.equals("1x")){%>
<script src="../jslib/profiles/1x/jquery/jquery-1.12.4.js"></script>
<script src="../jslib/profiles/1x/jquery/jquery-migrate-1.4.1.js"></script>
<script src="../jslib/profiles/1x/jquery/libraries/jquery-ui/jquery-ui.js"></script>
<script src="../jslib/profiles/1x/libraries/perc-retiredjs/json2.js"></script>
<script src="../jslib/profiles/1x/libraries/perc-retiredjs/jquery.xmldom-1.0.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-form/jquery.form.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-perc-retiredjs/jquery.blockUI.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-perc-retiredjs/jquery.caret.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-datatables/js/jquery.dataTables.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-datatables/js/dataTables.jqueryui.js"></script>

<!-- PSJSUtils.js must come after jQuery core js file -->
<script src="../plugins/PercViewReadyManager.js"></script>
<script src="../jslib/PSJSUtils.js"></script>
<script src="../plugins/perc_sessionTimeout.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-perc-retiredjs/jquery.text-overflow.js"></script>
<script src="../plugins/perc_path_constants.js"></script>
<script src="../plugins/PercExtendUiDialog.js"></script>
<script src="../plugins/perc_utils.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-perc-retiredjs/jquery.bbq.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-perc-retiredjs/jquery.cookie.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-perc-retiredjs/tools.scrollable-1.1.2.js"></script>
<script src="../services/PercServiceUtils.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-layout/jquery.layout_and_plugins.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-layout/jquery.layout_customresizer.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-perc-retiredjs/jquery.metadata.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-jeditable/jquery.jeditable.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-jeditable/jquery.jeditable.autogrow.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-jeditable/jquery.jeditable.charcounter.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-jeditable/jquery.jeditable.datepicker.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-jeditable/jquery.jeditable.masked.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-jeditable/jquery.jeditable.time.js"></script>

<script src="../jslib/profiles/1x/jquery/plugins/jquery-validation/jquery.validate.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-validation/additional-methods.js"></script>

<script src="../plugins/perc_extend_jQueryValidate.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-perc-retiredjs/jquery.tooltip.js"></script>
<script src="../controllers/PercDirtyController.js"></script>
<script src="../plugins/PercNavigationManager.js"></script>
<script src="../plugins/PercFolderHelper.js"></script>
<script src="../plugins/PercFolderPropertiesDialog.js"></script>
<script src="../widgets/PercFinderTree.js"></script>
<script src="../plugins/PercPathSelectionDialog.js"></script>
<script src="../views/PercCreateNewAssetDialog.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-perc-retiredjs/jquery.autocomplete.js"></script>
<script src="../plugins/PercListEditorWidget.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-hoverintent/jquery.hoverIntent.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-superfish/js/superfish.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-superfish/js/supersub.js"></script>

<script src="../widgets/PercDropdown.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-ui-multiselect-widget/jquery.multiselect.js"></script>
<script src="../jslib/profiles/1x/jquery/plugins/jquery-ui-multiselect-widget/jquery.multiselect.filter.js"></script>

<script src="../services/PercFolderService.js"></script>
<!-- Recent Service -->
<script src="../services/PercRecentListService.js"></script>
<!-- Redirect Handler -->
<script src="../plugins/PercRedirectHandler.js"></script>
<script src="../jslib/profiles/1x/libraries/mousetrap/mousetrap.js"></script>
<script src="../jslib/profiles/1x/libraries/mousetrap/plugins/bind-dictionary/mousetrap-bind-dictionary.js"></script>
<script src="../jslib/profiles/1x/libraries/mousetrap/plugins/global-bind/mousetrap-global-bind.js"></script>
<script src="../jslib/profiles/1x/libraries/mousetrap/plugins/pause/mousetrap-pause.js"></script>
<script src="../jslib/profiles/1x/libraries/mousetrap/plugins/record/mousetrap-record.js"></script>

<% }else if(jsProfile.equals("2x")){%>
<script src="../jslib/profiles/2x/jquery/jquery-2.2.4.js"></script>
<script src="../jslib/profiles/2x/jquery/jquery-migrate-1.4.1.js"></script>
<script src="../jslib/profiles/2x/jquery/libraries/jquery-ui/jquery-ui.js"></script>
<script src="../jslib/profiles/2x/libraries/perc-retiredjs/json2.js"></script>
<script src="../jslib/profiles/1x/libraries/perc-retiredjs/jquery.xmldom-1.0.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-form/jquery.form.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-perc-retiredjs/jquery.blockUI.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-perc-retiredjs/jquery.caret.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-datatables/js/jquery.dataTables.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-datatables/js/dataTables.jqueryui.js"></script>

<!-- PSJSUtils.js must come after jQuery core js file -->
<script src="../plugins/PercViewReadyManager.js"></script>
<script src="../jslib/PSJSUtils.js"></script>
<script src="../plugins/perc_sessionTimeout.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-perc-retiredjs/jquery.text-overflow.js"></script>
<script src="../plugins/perc_path_constants.js"></script>
<script src="../plugins/PercExtendUiDialog.js"></script>
<script src="../plugins/perc_utils.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-perc-retiredjs/jquery.bbq.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-perc-retiredjs/jquery.cookie.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-perc-retiredjs/tools.scrollable-1.1.2.js"></script>
<script src="../services/PercServiceUtils.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-layout/jquery.layout_and_plugins.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-layout/jquery.layout_customresizer.js"></script>

<script src="../jslib/profiles/2x/jquery/plugins/jquery-perc-retiredjs/jquery.metadata.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-jeditable/jquery.jeditable.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-jeditable/jquery.jeditable.autogrow.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-jeditable/jquery.jeditable.charcounter.js"></script>

<script src="../jslib/profiles/2x/jquery/plugins/jquery-jeditable/jquery.jeditable.datepicker.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-jeditable/jquery.jeditable.masked.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-jeditable/jquery.jeditable.time.js"></script>

<script src="../jslib/profiles/2x/jquery/plugins/jquery-validation/jquery.validate.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-validation/additional-methods.js"></script>

<script src="../plugins/perc_extend_jQueryValidate.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-perc-retiredjs/jquery.tooltip.js"></script>
<script src="../controllers/PercDirtyController.js"></script>
<script src="../plugins/PercNavigationManager.js"></script>
<script src="../plugins/PercFolderHelper.js"></script>
<script src="../plugins/PercFolderPropertiesDialog.js"></script>
<script src="../widgets/PercFinderTree.js"></script>
<script src="../plugins/PercPathSelectionDialog.js"></script>
<script src="../views/PercCreateNewAssetDialog.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-perc-retiredjs/jquery.autocomplete.js"></script>
<script src="../plugins/PercListEditorWidget.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-hoverintent/jquery.hoverIntent.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-superfish/js/superfish.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-superfish/js/supersub.js"></script>

<script src="../widgets/PercDropdown.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-ui-multiselect-widget/jquery.multiselect.js"></script>
<script src="../jslib/profiles/2x/jquery/plugins/jquery-ui-multiselect-widget/jquery.multiselect.filter.js"></script>

<script src="../services/PercFolderService.js"></script>
<!-- Recent Service -->
<script src="../services/PercRecentListService.js"></script>
<!-- Redirect Handler -->
<script src="../plugins/PercRedirectHandler.js"></script>
<script src="../jslib/profiles/2x/libraries/mousetrap/mousetrap.js"></script>
<script src="../jslib/profiles/2x/libraries/mousetrap/plugins/bind-dictionary/mousetrap-bind-dictionary.js"></script>
<script src="../jslib/profiles/2x/libraries/mousetrap/plugins/global-bind/mousetrap-global-bind.js"></script>
<script src="../jslib/profiles/2x/libraries/mousetrap/plugins/pause/mousetrap-pause.js"></script>
<script src="../jslib/profiles/2x/libraries/mousetrap/plugins/record/mousetrap-record.js"></script>
<%}else if(jsProfile.equals("3x")){%>
<script src="../jslib/profiles/3x/jquery/jquery-3.5.1.js"></script>
<script src="../jslib/profiles/3x/jquery/jquery-migrate-3.0.1.js"></script>
<script src="../jslib/profiles/3x/jquery/libraries/jquery-ui/jquery-ui.js"></script>
<script src="../jslib/profiles/3x/libraries/perc-retiredjs/json2.js"></script>
<script src="../jslib/profiles/3x/libraries/perc-retiredjs/jquery.xmldom-1.0.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-form/jquery.form.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.blockUI.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.caret.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-datatables/js/jquery.dataTables.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-datatables/js/dataTables.jqueryui.js"></script>

<!-- PSJSUtils.js must come after jQuery core js file -->
<script src="../plugins/PercViewReadyManager.js"></script>
<script src="../jslib/PSJSUtils.js"></script>
<script src="../plugins/perc_sessionTimeout.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.text-overflow.js"></script>
<script src="../plugins/perc_path_constants.js"></script>
<script src="../plugins/PercExtendUiDialog.js"></script>
<script src="../plugins/perc_utils.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.bbq.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.cookie.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/tools.scrollable-1.1.2.js"></script>

<script src="../services/PercServiceUtils.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-layout/jquery.layout_and_plugins.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-layout/jquery.layout_customresizer.js"></script>

<script src="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.metadata.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-jeditable/jquery.jeditable.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-jeditable/jquery.jeditable.autogrow.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-jeditable/jquery.jeditable.charcounter.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-jeditable/jquery.jeditable.datepicker.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-jeditable/jquery.jeditable.masked.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-jeditable/jquery.jeditable.time.js"></script>

<script src="../jslib/profiles/3x/jquery/plugins/jquery-validation/jquery.validate.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-validation/additional-methods.js"></script>

<script src="../plugins/perc_extend_jQueryValidate.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.tooltip.js"></script>
<script src="../controllers/PercDirtyController.js"></script>
<script src="../plugins/PercNavigationManager.js"></script>
<script src="../plugins/PercFolderHelper.js"></script>
<script src="../plugins/PercFolderPropertiesDialog.js"></script>
<script src="../widgets/PercFinderTree.js"></script>
<script src="../plugins/PercPathSelectionDialog.js"></script>
<script src="../views/PercCreateNewAssetDialog.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.autocomplete.js"></script>
<script src="../plugins/PercListEditorWidget.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-hoverintent/jquery.hoverIntent.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-superfish/js/superfish.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-superfish/js/supersub.js"></script>
<script src="../jslib/profiles/3x/libraries/fontawesome/fontawesome-all.js"></script>
<script src="../jslib/profiles/3x/libraries/fontawesome/v4-shims.js"></script>
<script src="../jslib/profiles/3x/libraries/handlebars/handlebars-v4.0.12.js"></script>
<script src="../jslib/profiles/3x/libraries/popper/popper.js"></script>
<script src="../jslib/profiles/3x/libraries/bootstrap/bootstrap.bundle.js"></script>
<script src="../widgets/PercDropdown.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-ui-multiselect-widget/jquery.multiselect.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-ui-multiselect-widget/jquery.multiselect.filter.js"></script>
<%}%>