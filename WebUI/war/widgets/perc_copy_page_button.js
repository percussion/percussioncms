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
 * Handles the copy page action. 
 */
(function ($) {
    var pageName;
    $.perc_build_copy_page_button = function (finderRef, content) {
        var btn = $("<a id='perc-finder-copy-page' href='#' title='Click to copy selected page'>Copy</a>")
        	.click(function (event) {
        		copyPageValidate();
        });        
      
        function copyPageValidate(){
            $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
            var selectedPage = $(".mcol-opened.perc-listing-type-percPage.perc-listing-category-PAGE");
            var selectedPage1 = $(".mcol-opened.perc-listing-type-percPage.perc-listing-category-LANDING_PAGE");
            var selectedItemList = $("#perc-finder-listview .perc-datatable-row-highlighted");
            
            if (selectedPage[0] == undefined && selectedPage1[0] == undefined && selectedItemList.size() == 0)
                 return;
            
            pageName = selectedPage.text();
            if(!pageName || pageName == "")
                pageName = selectedPage1.text();
            
            var id;
            if (selectedItemList.size() > 0)
            {
                listSelectedRowData = selectedItemList.data("percRowData");
                if (listSelectedRowData.category == "LANDING_PAGE" || listSelectedRowData.category == "PAGE")
                {
                    id = listSelectedRowData.id;
                    pageName = listSelectedRowData.name;
                }
            }
            else if (selectedPage[0] != undefined)
            {
                id = selectedPage.attr("id");
            }
            else
            {
                id = selectedPage1.attr("id");
            }

            var currentItem = finderRef.getCurrentItem();
            var itemId = "";
            if (currentItem != null){
                itemId = currentItem.id;
            }
            
            $.PercUserService.getAccessLevel("percPage", itemId,function(status, result){
                if(status == $.PercServiceUtils.STATUS_ERROR || result == $.PercUserService.ACCESS_READ || result == $.PercUserService.ACCESS_NONE)
                {
                   $.perc_utils.alert_dialog({title: I18N.message("perc.ui.copy.page.button@Copy Page"), content: I18N.message("perc.ui.copy.page.button@Copy Page Authorization") + pageName + ".'"});
                   $.unblockUI();
                   return;
                }
                else
                {
                   id = id.replace("perc-finder-listing-", "");
                   copyPage(id);
                }
            });
        }
        
        function copyPage(pageId){
            var currentPath = finderRef.getCurrentPath();
            var pathItem = finderRef.getPathItemById(pageId);
            $.PercPageService.copyPage(pageId, function(status, result){
                if(status=="error") {
                     showErrorMessage(result);
                     finderRef.refresh();
                } else {
                    finderRef.refresh(function(){
                        var pathItems = result.data.split("/");
                        var pageName = pathItems[pathItems.length-1];
                        currentPath[currentPath.length-1] = pageName;
                        finderRef.open(currentPath);
                    });
                }
                $.unblockUI();
            });
        }
        function showErrorMessage(message) {
            message = message.replace("PAGE_NAME", pageName);
            $.perc_utils.alert_dialog({id: 'perc-finder-copy-page-error', title: I18N.message("perc.ui.publish.title@Error"), content: message});
        }
    
        /**
         * Logic in charge of enabling/disablig the link, according to a given path
         * @param String path Path of the folder that contains the page
         */
        function update_copy_btn(path) {
            var last_page = path[path.length - 1];
            if(path[1] == $.perc_paths.SITES_ROOT_NO_SLASH)
            {
                if(path.length < 4)
                {
                    enableButtonCopy(false);
                }
                else
                {
                	var selectedPage = $(".mcol-opened.perc-listing-type-percPage.perc-listing-category-PAGE.perc_last_selected");
                    var selectedPage1 = $(".mcol-opened.perc-listing-type-percPage.perc-listing-category-LANDING_PAGE.perc_last_selected");
                    var selectedFolderId = $(".mcol-listing.perc-listing-type-Folder.perc-listing-category-FOLDER.ui-draggable.perc_last_selected.ui-droppable[title='" + last_page + "']").eq(0).attr("id");
                    var selectedSectionFolderId = $(".mcol-listing.perc-listing-type-Folder.perc-listing-category-SECTION_FOLDER.perc_last_selected.ui-droppable[title='" + last_page + "']").eq(0).attr("id");
                    
                    var selectedItemList = $("#perc-finder-listview .perc-datatable-row-highlighted");
                    
                    if (selectedItemList.size() > 0)
                    {
                        listSelectedRowData = selectedItemList.data("percRowData");

                        // First we must check if the containingFolder is writable
                        isFolderWritable(path, function(isWritable) {
                            if (isWritable && (listSelectedRowData.category == "LANDING_PAGE" || listSelectedRowData.category == "PAGE"))
                            {
                                enableButtonCopy(true);                 
                            }
                            else
                            {
                                enableButtonCopy(false);
                            }
                        });
                        return;
                    }
                    else if (selectedPage[0] != undefined || selectedPage1[0] != undefined)
                    {
                        // First we must check if the containingFolder is writable
                        isFolderWritable(path, function(isWritable) {
                            // This if is to correct an error of the finder refresh regarding this enablement.
                            // When selecting a page and going back to it's folder due to addPathChangedListener executing order, the enablement is not well performed
                            // If the target is a folder then turn the button to false
                            if (!isWritable || selectedFolderId != undefined || selectedSectionFolderId != undefined)
                            {
                                enableButtonCopy(false);
                            }
                            else
                            {
                                enableButtonCopy(true);
                            }                        
                        });
                    }
                    else
                    {
                        enableButtonCopy(false);
                    }
                }
            }
            else
            {
                enableButtonCopy(false);
            }
        }
        
        /**
         * Helper function that ask if the containing folder of the file to be copied is writable.
         * @param String path Path of the folder that contains the page
         * @param function callback Function that will get evaluated, with a boolean param
         */
        function isFolderWritable(path, callback)
        {
            var folderPath = path.slice(0, path.length - 1).join('/');
            $.PercFolderHelper().getAccessLevelByPath(folderPath, false, function(status, result) {
                // If error requesting folder properties show a dialog, else continue with the logic
                if (status == $.PercFolderHelper().PERMISSION_ERROR)
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result});
                    return;
                }
                else
                {
                    // We evaluate the callback with the result of the comparission of permission
                    //debugger;
                    callback(! (result == $.PercFolderHelper().PERMISSION_READ)); 
                    return;
                }
            });
        }
        
        /**
         * Helper function to enable or disable the new folder button on finder.
         * @param flag(boolean) if <code>true</code> the button is enabled, otherwise the button is disabled.
         */
        function enableButtonCopy(flag)
        {
            if (flag)
            {
                btn.removeClass('ui-disabled').addClass('ui-enabled').unbind('click').click( copyPageValidate );
            }
            else
            {
                btn.addClass('ui-disabled').removeClass('ui-enabled').unbind('click');
            }
            // We trigger a custom event using jQuery, the actions button will act acordingly
            btn.trigger('actions-change-enabled-state');
        }
        
        finderRef.addPathChangedListener( update_copy_btn );
        return btn;
    };
})(jQuery);

