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
 * Handles the new folder action.
 */
(function ($) {
    $.perc_build_new_folder_button = function (finder, contentViewer) {
        var ut = $.perc_utils;
        var finder_path = ["", $.perc_paths.SITES_ROOT_NO_SLASH];
        var pitem = {};
        var btn = $("<a id='perc-finder-new-folder' class='perc-font-icon ui-disabled' href='#' title='"+I18N.message("perc.ui.new.folder.button@Click New Folder") + "'><span class='icon-plus'></span><span class='icon-folder-close'></span></a>")
            .perc_button().on("click",function () {
                createNewFolder();
            });


        /**
         * Makes an ajax request to create the new folder. Passes the finder path.
         */
        function createNewFolder(){
            //Check user access
            $.PercFolderHelper().getAccessLevelByPath(finder_path.join('/'),false,function(status, result){
                if(status === $.PercFolderHelper().PERMISSION_ERROR || result === $.PercFolderHelper().PERMISSION_READ)
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.page.general@Warning"), content: I18N.message("perc.ui.new.folder.button@Permissions to Create Folder")});
                    return;
                }
                else
                {
                    $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
                    $.PercPathService.createNewFolder(finder_path.join('/'),
                        function(status, result){
                            if(status === $.PercServiceUtils.STATUS_SUCCESS)
                            {
                                pitem = result;
                                finder.refresh(function() {
                                    var expanded = $(".perc-finder").css("visibility") === "visible";
                                    if(expanded){
                                        ut.makeFolderEditable(pitem.PathItem);
                                    }
                                });
                                $.unblockUI();
                            }
                            else
                            {
                                $.unblockUI();
                                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result});
                            }
                        });
                }
            });
        }

        /**
         * Path changed listener function. Updates the finder_path variable with the current path.
         */
        function pathChangedListener(path) {
            finder_path = path;
            btn.show();
            enableButton(true);

            // If current view is Search then keep the button disabled (since no path to create is defined in Finder)
            if ($.Percussion.getCurrentFinderView() == $.Percussion.PERC_FINDER_SEARCH_RESULTS || $.Percussion.getCurrentFinderView() == $.Percussion.PERC_FINDER_RESULT)
            {
                enableButton(false);
            }
            else if (path[1] == $.perc_paths.RECYCLING_ROOT_NO_SLASH) {
                btn.hide();
            }
            else if(path[1] == $.perc_paths.DESIGN_ROOT_NO_SLASH && path.length < 4)
            {
                enableButton(false);
            }
            else if(path.length == 2 && path[1] == $.perc_paths.SITES_ROOT_NO_SLASH)
            {
                enableButton(false);
            }
            else
            {
                $.PercFolderHelper().getAccessLevelByPath(path.join('/'),true,function(status, result){
                    if(status == $.PercFolderHelper().PERMISSION_ERROR || result == $.PercFolderHelper().PERMISSION_READ)
                    {
                        enableButton(false);
                    }
                });
            }
        }

        /**
         * Helper function to enable or disable the new folder button on finder.
         * @param flag(boolean) if <code>true</code> the button is enabled, otherwise the button is disabled.
         */
        function enableButton(flag)
        {
            if(flag){
                $( ".perc-finder-menu #perc-finder-new-folder" ).removeClass('ui-disabled').addClass('ui-enabled').off('click').on("click", createNewFolder );
            }
            else{
                $( ".perc-finder-menu #perc-finder-new-folder" ).addClass('ui-disabled').removeClass('ui-enabled').off('click');
            }
        }

        finder.addPathChangedListener(pathChangedListener);
        return btn;
    };
})(jQuery);

