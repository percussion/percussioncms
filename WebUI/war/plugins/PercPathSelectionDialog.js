/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

/**
 * PercPathSelectionDialog.js
 *
 * Show a dialog with a items list with Checkbox to be selected.
 */
(function($){
    //Public API
    $.PercPathSelectionDialog = {
            open: _open
    };
    
    /*
    * Options are
    * okCallback -- Callback function that gets called when the OK button is clicked.
    * cancelCallback -- Callback function that gets called when cancel button is clicked
    * dialogTitle -- Title of the dialog
    * rootPath -- The root path of the tree, see PercFinderTree.js file for more details.
    * selectableTypes -- An array of content type names that are allowed to select from this dialog.
    * emptySelectionMessage -- The message that will be displayed when no item is selected.
    * initialPath -- The initial path, if provided the tree will be expanded to this path
    * createNew -- An object expected to have the following options.
    * {label:"",iconclass:"",onclick:function(successCallback, cancelCallback){}}
    * If the object is not null and has valid options, a button is created and when clicked
    * the function will be called with two callback functions, on success the new dialog must call 
    * successCallback with PathItem and on error or cancel it should call cancelCallback
    * 
    */
    function _open(options)
    {
        var defaults = {
            okCallback:function(){},
            cancelCallback:function(){},
            dialogTitle: I18N.message("perc.ui.page.path.selection.dialog@Select Path"),
            rootPath:$.PercFinderTreeConstants.ROOT_PATH_SITES,
            emptySelectionMessage:I18N.message("perc.ui.page.path.selection.dialog@Select Page"),
            initialPath:null,
            createNew:{},
            showFoldersOnly:false,
            selectedItemValidator:$.noop,
            acceptableTypes:'',
            acceptableCategories:''
        };
        var finderTree = null;
        var settings = $.extend(defaults, options);
        if(!settings.initialPath) {
            settings.initialPath = null;
        }
        var selectedItem = null;
        var dialog = null;
        var dialogHTML = createDialog(settings);
        var buttons = { }
        var buttonSaveOk = {
                 /*
                 * selectedItem.type is used instead of selectableTypes when typechecking (defaults overwritten).
                 * The valid types are determined in the file(s) that call this method. 
                 */
                 click: function(){
                    if(settings.selectedItemValidator && $.isFunction(settings.selectedItemValidator))
                    {
                        var errMsg = settings.selectedItemValidator(selectedItem);
                        if(errMsg != null){
                            dialogHTML.find("label[for='perc-path-location']").text(errMsg).show();
                            return;
                        }
                    }
                    dialog.remove();
                    settings.okCallback(selectedItem);
                 },
                 id: "perc-path-selection-dialog-save"
            }
        var buttonCancel = {
                   click: function(){
                        dialog.remove();
                        settings.cancelCallback();
                    },
                    id: "perc-path-selection-dialog-cancel"
                }
        buttons.Ok = buttonSaveOk;
        buttons.Cancel = buttonCancel;
                
        dialog = $(dialogHTML).perc_dialog( {
            resizable : false,
            title: settings.dialogTitle,
            modal: true,
            closeOnEscape : true,
            percButtons: buttons,
            id: "perc-path-selection-dialog",
            width: 400
        });
        
        //Dialog Html
        function createDialog(){
            var inpath = settings.initialPath?settings.initialPath:"";
            var container =   $('<div class="perc-path-selector-container"><div class="perc-label" style="height:10px;"><span style="float:left;font-weight:bold">Select from path:</span><span class="perc-create-new-button" style="float:right;margin-right:55px;display:none"></span></div></div>');
            if(settings.createNew && settings.createNew.label && settings.createNew.onclick){
                var successCallback = function(pathItem){
                    dialog.remove();
                    settings.okCallback(pathItem);
                }
                var cancelCallback = function(){};
                container.find(".perc-create-new-button").show().click(function() {
                    
                    if(!endsWith($('#perc_selected_path').text(),"/")) {
                        //TODO: I18N TESTME
                        // $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: I18N.message("perc.ui.page.path.selection.dialog@Select Folder")});
                        dialogHTML.find("label[for='perc-path-location']").text(I18N.message("perc.ui.page.path.selection.dialog@Select Folder")).show();
                        return;
                    }
                    
                    $.PercFolderHelper().getAccessLevelByPath(
                            $('#perc_selected_path').text(),
                            true,
                            function( status, result ) {
                                var error = status == $.PercFolderHelper().PERMISSION_ERROR,
                                    onlyWrite = result == $.PercFolderHelper().PERMISSION_READ;
                                if ( error || onlyWrite ) {
                                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.newassetdialog.title@New Asset"), content: I18N.message("perc.ui.page.path.selection.dialog@Not Authorized to Create")});
                                    return;
                                }
                                else {
                                    checkUserWorkflowPermission();
                                }
                            }
                    );
                    function checkUserWorkflowPermission() {
                        $.PercUserService.getAccessLevel(
                            null,
                            -1,
                            function( status, result) {
                                var error = status == $.PercServiceUtils.STATUS_ERROR,
                                    accessRead = result == $.PercUserService.ACCESS_READ,
                                    accessNone = result == $.PercUserService.ACCESS_NONE;
                                if (  error || accessRead || accessNone ) {
                                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.newassetdialog.title@New Asset"), content: I18N.message("perc.ui.page.path.selection.dialog@Not Authorized to Create")});
                                    return;
                                }
                                else {
                                    settings.createNew.onclick(successCallback, cancelCallback);
                                }
                            },
                            $('#perc_selected_path').text()
                        );
                    }
                }).append('<a class="perc-font perc-font-button" href="#"><i class="' + settings.createNew.iconclass + '"></i> ' + settings.createNew.label + '</a>');
            }
            finderTree = $("<div class='perc-path-selection-tree' style='margin-top:20px;'>")
                .PercFinderTree({
                    rootPath:settings.rootPath,
                    initialPath:settings.initialPath,
                    showFoldersOnly:settings.showFoldersOnly,
                    acceptableTypes:settings.acceptableTypes,
                    acceptableCategories:settings.acceptableCategories,
                    height:"230px",
                    width:"600px",
                    classNames:{container:"perc-section-selector-container", selected:"perc-section-selected-item"},
                    getInitialPathItem: function(pathItem){
                        selectedItem = pathItem;
                    },
                    onClick:function(pathItem){
                        selectedItem = pathItem;
                        $("#perc_selected_path").html(pathItem.path);
                        if($.inArray(pathItem.category,settings.selectableTypes)!=-1)
                            dialogHTML.find("label[for='perc-select-blog-location-tree']").hide();

                    }
                });
            container.append(finderTree); 
            container.append('<div style="margin-top:10px;"><span style="color:gray; font-weight:bold">Selected item path:</span> <span id="perc_selected_path">' + 
                                inpath + '</span></div>')
            container.append('<label for="perc-path-location" class="perc_field_error" style="display: none;"></label>');
            return container;
        }
        
        /**
         * Checks if the selected path ends with '/' and if
         * it matches '/Assets/' as those are not valid selections.
         * If it ends with '/' then we have a selected a folder.  Cannot be
         * '/Assets/'
         * @param {*} str - the string in which to validate
         * @param {*} suffix - the char in which to check the ending for
         */
        function endsWith(str, suffix) {
            return str.indexOf(suffix, str.length - suffix.length) !== -1 && str !== '/Assets/';
        }
        
    }// End open dialog
})(jQuery);