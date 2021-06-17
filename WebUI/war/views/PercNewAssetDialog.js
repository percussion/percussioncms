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
 * PercNewAssetDialog.js
 *
 * Author: Jose Annunziato
 * Date: 1/21/2010
 *
 * Creates a dialog that displays a list of asset editors.
 * Selecting an editor and clicking Ok opens the editor in the bottom frame.
 * The editor is used to create and configure the new asset.
 */
(function($)
{
    $.PercNewAssetDialog = {};
    $.PercNewAssetDialog.init = function(finder, contentViewer)
    {
        var utils = $.perc_utils;
        var finderPath;
        $.PercNewAssetDialog.open = function()
        {
            var taborder = 30;
            var v;

            var dialog;
            var buttons = {};

            // create the perc_dialog with a placeholder for the selectable items
            // selectable items will go in the div with class .perc-items
            // the items are inserted by the controller
            dialog = $("<div>" +
                 "<p class='perc-field-error' id='perc-save-error'></p>" +
                 "<form action='' method='GET'> " +
                 "  <label for='perc-select-template'>"+ I18N.message( "perc.ui.newassetdialog.label@Select Asset Type" ) + ": </label><br/>" +
                 "  <a class='prevPage browse left' style = 'margin:21px 0px 30px 0px'></a>" +
                 "  <div class='perc-scrollable' style = 'height:64px'>" +
                 "    <input type='hidden' id='perc-select-template'   name='template'/>" +
                 "    <input type='hidden' id='perc-editor-url'        name='url'/>" +
                 "    <input type='hidden' id='perc-workflow-id'       name='workflowId'/>" +
                // here is where the items will be inserted by the controller
                 "    <div class='perc-items'>" +
                 "    </div>" +
                 "  </div>" +
                 "  <a class='nextPage browse right' style = 'margin:21px 0px 30px 0px' ></a>" +
                 "  <div class='ui-layout-south'>" +
                 "    <div id='perc_buttons' style='z-index: 100;'></div>" +
                 "  </div>" +
                 "</form></div>")
                .perc_dialog(
                    {
                        title: I18N.message( "perc.ui.newassetdialog.title@New Asset" ),
                        buttons: buttons,
                        percButtons:
                            {
                                "Next":
                                    {
                                        click: function()
                                        {
                                            var contentId = $.PercNavigationManager.getMemento().lastOpenedItem;
                                            //If there is a opened page or asset check it in before creating new asset.
                                            if(!utils.isBlankString(contentId))
                                            {
                                                $.PercWorkflowController().checkIn(contentId, function(status)
                                                {
                                                    _submit();
                                                });
                                            }
                                            else
                                            {
                                                _submit();
                                            }
                                        },
                                        id:"perc-page-save"
                                    },
                                "Cancel":
                                    {
                                        click: function()
                                        {
                                            _remove();
                                            if(typeof($.PercNavigationManager.getMemento().lastLocation) != 'undefined')
                                            {
                                                var bookmark = $.PercNavigationManager.getBookmark();
                                                var last = $.PercNavigationManager.getMemento().lastLocation;
                                                if (decodeURIComponent(bookmark) !== last)
                                                {
                                                    window.location.href = last;
                                                }
                                                else
                                                    $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                                            }
                                        },
                                        id:"perc-page-cancel"
                                    }
                            },
                        id: "perc-new-page-dialog",
                        width: 774,
                        modal: true
                    });


            var pseudoSelect = $('#perc-select-template_perc_is');
            var selectLocalStyle = "height: 160px; width: 410px; overflow-x: scroll; overflow-y: hidden;";
            pseudoSelect.attr("style", selectLocalStyle);

            // removes the dialog box when cancel is clicked
            function _remove()
            {
                dialog.remove();
            }


            function _submit()
            {
                dialog.find('form').trigger("submit");
            }

            // add a validator to the form
            // intercepts submit event and performs function in submitHandler
            // we are not going to actually submit the form,
            // instead we will get a hold of the URL associated with the selected editor
            // and open the editor in the #frame down below
            v = dialog.find('form').validate(
                {
                    errorClass: "perc-field-error",
                    validClass: "perc-field-success",
                    wrapper: "p",
                    validateHiddenFields: false,
                    debug: false,
                    submitHandler: function(form)
                    {
                        var editorUrl = $("#perc-editor-url").val( );
                        var workflowId = $("#perc-workflow-id").val( );

                        $.perc_pathmanager.open_containing_folder( finderPath, function( fspec, pathArray )
                        {
                            // close dialog
                            dialog.remove();

                            path = pathArray.toString().replace(/,/g,"/");
                            pathtemp = path;
                            path = path.replace($.perc_paths.ASSETS_ROOT,"");

                            // get the folder id
                            $.PercAssetController.getPathItemForPath(pathtemp, function(pathItemObj)
                            {
                                // the folderId format is ########-###-###
                                var folderId = pathItemObj.id;

                                // we only care about the last 3 digits after the last dash '-'
                                // because of a legacy representation
                                var oldFolderId = folderId.substring(folderId.lastIndexOf("-")+1);

                                // append the folder id to the URL of the form
                                //Adam Gent (added sys_asset_folderid for PSAddNewItemToFolder Core Extension)
                                editorUrl += "&sys_folderid="+oldFolderId+"&sys_asset_folderid="+oldFolderId+"&sys_workflowid="+workflowId;

                                // render the form editor in the bottom frame
                                $.PercIFrameView.renderAssetEditor(finder, workflowId, editorUrl, path, pathArray, true);
                                $("#perc-page-button").html('Asset:').append("<span id='perc-pageEditor-menu-name'> (New Asset)</span>" );
                                $.PercNavigationManager.clearId();

                                //Hide the inline help when in Editor mode and fix the iframe height.
                                $("#perc-editor-inline-help").hide();
                                fixIframeHeight();
                            });
                        });
                    }
                });

            // the controller uses the service to query for the list of editors
            // and then creates a datastructure and passes it to the callback
            $.PercAssetController.getAssetEditorLibrary(getCurrentFolderPath(), function(assetEditorLibrary)
            {
                // get the placeholder where to insert the selectable items
                var itemContainer = dialog.find('div.perc-scrollable div.perc-items');

                // iterate over the editors, create a div for each, and then insert it in div.perc-items
                for(a in assetEditorLibrary)
                {
                    // get the datastructure for the editor
                    var assetEditor = assetEditorLibrary[a];

                    // create the div and insert it into the place holder
                    itemContainer.append(createAssetEditorEntry(assetEditor));

                    $("div.perc-scrollable").scrollable(
                        {
                            items: ".perc-items",
                            size: 4,
                            keyboard: false
                        });
                    $(".perc-items .item .item-id").hide();
                    $(".perc-items .item .item-editor-url").hide();
                    $(".perc-items .item .item-workflow-id").hide();

                    // bind click event to each item to handle selection
                    // each div has hidden inner divs with data specific to each item
                    // when the user clicks on the selection, get the values in the hidden inner divs
                    // and assign the values to the hidden input fields of the form
                    // the form will be submitted with the selected item's values in the hidden input fields
                    $(".perc-items .item").on('click', function()
                    {
                        var editorUrl = $(this).find(".item-editor-url").text();
                        var workflowId = $(this).find(".item-workflow-id").text();
                        $("#perc-select-template").val($(this).find(".item-id").text());
                        $("#perc-editor-url").val(editorUrl);
                        $("#perc-workflow-id").val(workflowId);
                        $(".perc-items .item").removeClass("perc-selected-item");
                        $(this).addClass("perc-selected-item");
                    });

                    // select first item by default
                    $firstItem = $(".perc-items .item:first");
                    $("#perc-select-template").val($firstItem.find(".item-id").text());
                    $("#perc-editor-url").val($firstItem.find(".item-editor-url").text());
                    $("#perc-workflow-id").val($firstItem.find(".item-workflow-id").text());
                    $firstItem.addClass("perc-selected-item");
                }
            });

            var nm = $('#perc-page-name');
            var ti = $('#perc-page-title');
            var url = $('#perc-page-url');

            /**
             * Url filter function to allow only url valid chars.
             * @param txt the text to be filtered.
             */
            function url_filter( txt )
            {
                return txt.replace( /\s/g, '-' ).replace( /[^\$\{\}\^\[\]\`\=\,\;\`a-zA-Z0-9\-]/g, '' );
            }

            /**
             * Slash filter function to remove backslash chars.
             * @param txt the text to be filtered.
             */
            function slash_filter( txt)
            {
                return txt.replace(/\\/g, '');
            }

            $.perc_textAutoFill(nm, ti, slash_filter);
            $.perc_textAutoFill(nm, url, url_filter);
            $.perc_textAutoFill(nm, '#perc-page-linktitle', slash_filter);

            $.perc_filterField(nm, slash_filter);
            $.perc_filterField(ti, slash_filter);
            $.perc_filterField(url, url_filter);
            $.perc_filterField('#perc-page-linktitle', slash_filter);
        };// End open dialog

        $.PercNewAssetDialog.openViewer = function(folderPath,widgetId){
            $.PercAssetService.getAssetEditorForWidgetAndFolder(folderPath, widgetId, function(status, result){
                if(status === $.PercServiceUtils.STATUS_ERROR){
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: I18N.message("perc.ui.new.asset.dialog@Unknown Error Asset")});
                    return;
                }
                var assetEditor = result.AssetEditor.pop();
                var editorUrl = assetEditor.url;
                var workflowId = assetEditor.workflowId;
                path = folderPath.replace($.perc_paths.ASSETS_ROOT,"");
                // get the folder id
                $.PercAssetController.getPathItemForPath(folderPath, function(pathItemObj)
                {
                    // the folderId format is ########-###-###
                    var folderId = pathItemObj.id;

                    // we only care about the last 3 digits after the last dash '-'
                    // because of a legacy representation
                    var oldFolderId = folderId.substring(folderId.lastIndexOf("-")+1);

                    // append the folder id to the URL of the form
                    //Adam Gent (added sys_asset_folderid for PSAddNewItemToFolder Core Extension)
                    editorUrl += "&sys_folderid="+oldFolderId+"&sys_asset_folderid="+oldFolderId+"&sys_workflowid="+workflowId;

                    // render the form editor in the bottom frame
                    $.PercIFrameView.renderAssetEditor(finder, workflowId, editorUrl, path, folderPath.split("/"), true);
                    $("#perc-page-button").html('Asset:').append("<span id='perc-pageEditor-menu-name'> (New Asset)</span>" );
                    $.PercNavigationManager.clearId();

                    //Hide the inline help when in Editor mode and fix the iframe height.
                    $("#perc-editor-inline-help").hide();
                    fixIframeHeight();
                });

            });
        };

        function check_for_dirty_page()
        {
            $.PercFolderHelper().getAccessLevelByPath(finderPath.join('/'),false,function(status, result){
                if(status === $.PercFolderHelper().PERMISSION_ERROR || result === $.PercFolderHelper().PERMISSION_READ)
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.page.general@Warning"), content: I18N.message("perc.ui.new.asset.dialog@Permission For Asset")});

                }
                else
                {
                    var currentItem = finder.getCurrentItem();
                    var folderPath = "";
                    if (currentItem != null){
                        folderPath = currentItem.folderPaths;
                        //if the current item is a Folder select the current path.
                        if (currentItem.type === "Folder"){
                            folderPath = currentItem.folderPath;
                        }
                    }
                    $.PercUserService.getAccessLevel(null,-1,function(status, result){
                        if(status === $.PercServiceUtils.STATUS_ERROR || result === $.PercUserService.ACCESS_READ || result === $.PercUserService.ACCESS_NONE)
                        {
                            $.perc_utils.alert_dialog({title: I18N.message("perc.ui.new.asset.dialog@New Asset"), content: I18N.message("perc.ui.new.asset.dialog@Not Authorized Asset")});

                        }
                        else
                        {
                            contentViewer.confirm_if_dirty(function(){
                                if($.PercNavigationManager.getView() === $.PercNavigationManager.VIEW_EDIT_ASSET)
                                    $.PercNavigationManager.clearMemento();
                                $.PercNavigationManager.goToLocation(
                                    $.PercNavigationManager.VIEW_EDIT_ASSET,
                                    $.PercNavigationManager.getSiteName(),
                                    'edit',
                                    null,
                                    null,
                                    $.PercNavigationManager.getPath(),
                                    $.PercNavigationManager.PATH_TYPE_ASSET,
                                    {lastLocation: $.PercNavigationManager.getBookmark(), lastOpenedItem:$.PercNavigationManager.getId()});
                            });
                        }
                    }, folderPath);
                }
            });
        }

        function createAssetEditorEntry(assetEditor)
        {
            var temp =    "<div class='item' for='@ITEM_ID@'>" +
                 "   <div class='item-id'>@ITEM_ID@</div>" +
                 "   <div class='item-editor-url'>@ITEM_URL@</div>" +
                 "   <div class='item-workflow-id'>@ITEM_WORKFLOW_ID@</div>" +
                 "   <table style='vertical-align:middle'>" +
                 "       <tr><td align='center' valign='middle'>" +
                 "           <img src='/Rhythmyx@IMG_SRC@'/>" +
                 "       </td><td style='vertical-align:middle'><span>@ITEM_LABEL@</span></td></tr>" +
                 "   </table>";
                 "</div>";
            return temp.replace(/@IMG_SRC@/, assetEditor.icon)
                .replace(/@ITEM_ID@/g, assetEditor.title)
                .replace(/@ITEM_URL@/, assetEditor.url)
                .replace(/@ITEM_WORKFLOW_ID@/, assetEditor.workflowId)
                .replace(/@ITEM_LABEL@/, assetEditor.title);
        }


        function getCurrentFolderPath()
        {
            return $.deparam.querystring().path;
        }

        var newAssetButton = $('<a id="mcol-new-asset" class="perc-font-icon" title="'+I18N.message("perc.ui.new.asset.dialog@Click New Asset")+'" href="#" class="ui-disabled"><span class="icon-plus"></span><span class="icon-file-alt"></span></a>').perc_button();
        function updateBtn(path)
        {
            finderPath = path;

            // If current view is Search then keep the button disabled (since no path to create is defined in Finder)
            if ($.Percussion.getCurrentFinderView() === $.Percussion.PERC_FINDER_SEARCH_RESULTS || $.Percussion.getCurrentFinderView() === $.Percussion.PERC_FINDER_RESULT)
            {
                enableButton(false);
            }
            else if(path[1] === $.perc_paths.ASSETS_ROOT_NO_SLASH)
            {
                newAssetButton.show();
                enableButton(true);
                if(path.length < 3)
                {
                    enableButton(false);
                }
                else
                {
                    $.PercFolderHelper().getAccessLevelByPath(path.join('/'),true,function(status, result){
                        if(status === $.PercFolderHelper().PERMISSION_ERROR || result === $.PercFolderHelper().PERMISSION_READ)
                        {
                            enableButton(false);
                        }
                    });
                }
            }
            else
            {
                if(path[1] === $.perc_paths.DESIGN_ROOT_NO_SLASH || path[1] === $.perc_paths.SITES_ROOT_NO_SLASH || path[1] === $.perc_paths.RECYCLING_ROOT_NO_SLASH)
                {
                    newAssetButton.hide();
                }
            }
        }

        /**
         * Helper function to enable or disable the new asset button on finder.
         * @param flag(boolean) if <code>true</code> the button is enabled, otherwise the button is disabled.
         */
        function enableButton(flag)
        {
            if(flag){
                newAssetButton.removeClass('ui-disabled').addClass('ui-enabled').off('click').on("click", check_for_dirty_page );
            }
            else{
                newAssetButton.addClass('ui-disabled').removeClass('ui-enabled').off('click');
            }
        }

        finder.addPathChangedListener( updateBtn );
        return newAssetButton;
    };
})(jQuery);
