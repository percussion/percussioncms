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
 * Handles the restoration of a deleted item/folder from the recycle bin.
 */
(function ($) {
    var itemName;
    $.perc_build_restore_button = function (finderRef, content) {
        var btn = $("<a id='perc-finder-restore-item' href='#' title='Click to restore the selected item'>Restore Item</a>")
            .on("click",function (event) {
                restorePageValidate(event);
            });

        function restorePageValidate(evt) {
            $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
            var selectedItem = $('.mcol-opened.perc_last_selected');
            var selectedItemList = $("#perc-finder-listview .perc-datatable-row-highlighted");

            if (!(isItem() || isFolder()) && selectedItemList.length === 0)
                return;
            //Don't allow user restore landing page of Navigation.Let them use folder to restore.
            if(isLandingPage()){
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.restore.title@Restore"),content:I18N.message("perc.ui.restore.messsage@Restore Not Allowed")});
                $.unblockUI();
                return;
            }

            itemName = selectedItem.text();

            // TODO: make sure ID is set here under all use cases

            var currentItem = finderRef.getCurrentItem();
            var itemId = "";
            if (currentItem != null) {
                itemId = currentItem.id;
            }

            $.PercUserService.getAccessLevel("percPage", itemId, function (status, result) {
                if (status == $.PercServiceUtils.STATUS_ERROR || result == $.PercUserService.ACCESS_READ || result == $.PercUserService.ACCESS_NONE) {
                    $.perc_utils.alert_dialog({ title: I18N.message("perc.ui.copy.page.button@Copy Page"), content: I18N.message("perc.ui.copy.page.button@Copy Page Authorization") + itemName + ".'" });
                    $.unblockUI();
                    return;
                }
                else {
                    restoreSelection(itemId);
                }
            });
        }

        function restoreSelection(id) {
            var path = '';

            if (isFolder()) {
                path = $.perc_paths.PATH_RESTORE_FOLDER;
            } else if (isPage()) {
                path = $.perc_paths.PAGE_RESTORE;
            } else if (isAsset()) {
                path = $.perc_paths.ASSET_RESTORE;
            } else {
                console.warn('The seleted item for restore is not an asset, page, or folder.', id);
                return;
            }

            $.PercRecycleService.restoreItem(id, path, function(status, data) {
                if (status === $.PercServiceUtils.STATUS_ERROR) {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: data});
                } else {
                    finderRef.refresh();
                }
            });

            $.unblockUI();
        }

        function showErrorMessage(message) {
            message = message.replace("PAGE_NAME", itemName);
            $.perc_utils.alert_dialog({ id: 'perc-finder-copy-page-error', title: I18N.message("perc.ui.publish.title@Error"), content: message });
        }

        /**
         * Logic in charge of enabling/disablig the link, according to a given path
         * @param String path Path of the folder that contains the page
         */
        function update_restore_btn(path) {
            if (path[1] == $.perc_paths.RECYCLING_ROOT_NO_SLASH) {
                if (path.length < 4) {
                    enableButtonRestore(false);
                }
                else {
                    var selectedItemList = $("#perc-finder-listview .perc-datatable-row-highlighted");
                    // This is present to select items under List mode in CM1 UI.
                    if (selectedItemList.length > 0) {
                        listSelectedRowData = selectedItemList.data("percRowData");

                        // First we must check if the containingFolder is writable
                        isFolderWritable(path, function (isWritable) {
                            if (isWritable && (isRestorableCategory(listSelectedRowData))) {
                                enableButtonRestore(true);
                            }
                            else {
                                enableButtonRestore(false);
                            }
                        });
                        return;
                    }
                    else if (isItem()) {
                        // we have a page selected for restore
                        // First we must check if the containingFolder is writable
                        isFolderWritable(path, function (isWritable) {
                            // This if is to correct an error of the finder refresh regarding this enablement.
                            // When selecting a page and going back to it's folder due to addPathChangedListener executing order, the enablement is not well performed
                            // If the target is a folder then turn the button to false
                            if (!isWritable) {
                                enableButtonRestore(false);
                            }
                            else {
                                enableButtonRestore(true);
                            }
                        });
                    } else if(isFolder()) {
                        // we have a folder selected for restore
                        enableButtonRestore(true);
                    }
                    else {
                        enableButtonRestore(false);
                    }
                }
            }
            else {
                enableButtonRestore(false);
            }
        }

        /**
         * Checks if the currently selected element in Finder list view is valid for restore.
         * @param {*} listSelectedRowData the currently selected html element in the Finder List VIew.
         */
        function isRestorableCategory(listSelectedRowData) {
            return listSelectedRowData.category === 'LANDING_PAGE' ||
                listSelectedRowData.category === 'PAGE' ||
                listSelectedRowData.category === 'SECTION_FOLDER' ||
                listSelectedRowData.category === 'FOLDER' ||
                listSelectedRowData.category === 'ASSET';
        }

        /**
         * Checks if the currently selected item is an item (asset or page or w/e). :)
         */
        function isItem() {
            return $('.mcol-opened.perc_last_selected').hasClass('perc-listing-category-ASSET') ||
                $('.mcol-opened.perc_last_selected').hasClass('perc-listing-category-PAGE') ||
                $('.mcol-opened.perc_last_selected').hasClass('perc-listing-category-LANDING_PAGE');
        }

        /**
         * Checks if the currently selected item is a page :)
         */
        function isPage() {
            return $('.mcol-opened.perc_last_selected').hasClass('perc-listing-category-PAGE') ||
                $('.mcol-opened.perc_last_selected').hasClass('perc-listing-category-LANDING_PAGE');
        }

        /**
         * Checks if the currently selected item is a page :)
         */
        function isLandingPage() {
            return $('.mcol-opened.perc_last_selected').hasClass('perc-listing-category-LANDING_PAGE');
        }


        /**
         * Checks if the currently selected item is an asset :)
         *
         */
        function isAsset() {
            return $('.mcol-opened.perc_last_selected').hasClass('perc-listing-category-ASSET');
        }

        /**
         * Checks if the currently selected item is a folder.
         */
        function isFolder() {
            return $('.mcol-opened.perc_last_selected').hasClass('perc-listing-category-FOLDER') ||
                $('.mcol-opened.perc_last_selected').hasClass('perc-listing-category-SECTION_FOLDER');
        }

        /**
         * Helper function that ask if the containing folder of the file to be copied is writable.
         * @param String path Path of the folder that contains the page
         * @param function callback Function that will get evaluated, with a boolean param
         */
        function isFolderWritable(path, callback) {
            var folderPath = path.slice(0, path.length - 1).join('/');
            $.PercFolderHelper().getAccessLevelByPath(folderPath, false, function (status, result) {
                // If error requesting folder properties show a dialog, else continue with the logic
                if (status == $.PercFolderHelper().PERMISSION_ERROR) {
                    $.perc_utils.alert_dialog({ title: I18N.message("perc.ui.publish.title@Error"), content: result });
                    return;
                }
                else {
                    // We evaluate the callback with the result of the comparission of permission
                    //debugger;
                    callback((result !== $.PercFolderHelper().PERMISSION_READ));
                    return;
                }
            });
        }

        /**
         * Helper function to enable or disable the new folder button on finder.
         * @param flag(boolean) if <code>true</code> the button is enabled, otherwise the button is disabled.
         */
        function enableButtonRestore(flag) {
            if (flag) {
                btn.removeClass('ui-disabled').addClass('ui-enabled').off('click').on("click",
                    function(evt){
                        restorePageValidate(evt);
                    });
            }
            else {
                btn.addClass('ui-disabled').removeClass('ui-enabled').off('click');
            }
            // We trigger a custom event using jQuery, the actions button will act acordingly
            btn.trigger('actions-change-enabled-state');
        }

        finderRef.addPathChangedListener(update_restore_btn);
        return btn;
    };
})(jQuery);
