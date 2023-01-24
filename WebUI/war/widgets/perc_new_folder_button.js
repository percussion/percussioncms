/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Handles the new folder action.
 */
(function ($) {
    $.perc_build_new_folder_button = function (finder, contentViewer) {
        var ut = $.perc_utils;
        var finder_path = ["", $.perc_paths.SITES_ROOT_NO_SLASH];
        var pitem = {};
        var btn = $("<a id='perc-finder-new-folder' class='perc-font-icon ui-disabled' href='#' title='"+I18N.message("perc.ui.new.folder.button@Click New Folder") + "'><span class='icon-plus fas fa-plus'></span><span class='icon-folder-close fas fa-folder'></span></a>")
            .perc_button().on("click",function (evt) {
                createNewFolder(evt);
            });


        /**
         * Makes an ajax request to create the new folder. Passes the finder path.
         */
        function createNewFolder(evt){
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
                $( ".perc-finder-menu #perc-finder-new-folder" ).removeClass('ui-disabled').addClass('ui-enabled').off('click').on("click",
                    function(evt){
                        createNewFolder(evt);
                    } );
            }
            else{
                $( ".perc-finder-menu #perc-finder-new-folder" ).addClass('ui-disabled').removeClass('ui-enabled').off('click');
            }
        }

        finder.addPathChangedListener(pathChangedListener);
        return btn;
    };
})(jQuery);

