
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

(function($){

//Add custom validation method for the URL name.
//$.validator.addMethod( 'url_name',
//        function(x) { return x.match( /^[a-zA-Z0-9\-]*$/ ); },
//       I18N.message( "perc.ui.newpagedialog.error@Url name validation error" ));

    $.perc_build_new_page_button = function(finderRef, contentViewer)
    {
        var finderPath;


        var newPageButton = $('<a id="mcol-new-page" class="perc-font-icon" href="#" title="' +I18N.message("perc.ui.new.page.button@Click New Page") + '"class="ui-disabled"><span class="icon-plus fas fa-plus"></span><span class="icon-file fas fa-file"></span></a>').perc_button();

        /**
         * Listener function that is added to the finder listeners, this method gets called whenever there is a path change
         * happens on the finder.
         * @param path, an array of the path entries. For a page Page1 under Foo site Bar folder will be
         * ["Sites","Foo","Bar","Page1"].
         * Based on the supplied path decides whether the button needs to be enabled or disabled.
         * Enables only when the root node is a site or site folder and user has at least write access to the folder.
         */
        function newPageButtonListener(path) {
            finderPath = path;

            // If current view is Search then keep the button disabled (since no path to create is defined in Finder)
            if ($.Percussion.getCurrentFinderView() == $.Percussion.PERC_FINDER_SEARCH_RESULTS || $.Percussion.getCurrentFinderView() == $.Percussion.PERC_FINDER_RESULT)
            {
                enableButton(false);
            }
            else if(path[1] == $.perc_paths.SITES_ROOT_NO_SLASH)
            {
                newPageButton.show();
                enableButton(true);
                if(path.length < 3)
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
            else
            {
                if(path[1] == $.perc_paths.DESIGN_ROOT_NO_SLASH || path[1] == $.perc_paths.ASSETS_ROOT_NO_SLASH || path[1] == $.perc_paths.RECYCLING_ROOT_NO_SLASH )
                {
                    newPageButton.hide();
                }
            }

        }

        /**
         * Helper function to enable or disable the new page button on finder.
         * @param flag(boolean) if <code>true</code> the button is enabled, otherwise the button is disabled.
         */
        function enableButton(flag)
        {
            if(flag){
                newPageButton.removeClass('ui-disabled').addClass('ui-enabled').off('click').on("click",
                    function(evt){
                        checkAndOpenNewPageDialog(evt);
                    } );
            }
            else{
                newPageButton.addClass('ui-disabled').removeClass('ui-enabled').off('click');
            }
        }


        /**
         * Checks whether the current user has permission to create a new page, if yes, then calls the contentViewer
         */
        function checkAndOpenNewPageDialog(evt)
        {
            var currentItem = finderRef.getCurrentItem();
            var folderPath = "";
            if (currentItem != null){
                if (typeof(currentItem.folderPaths) === 'object') {
                    folderPath = currentItem.folderPaths[0];
                } else {
                    folderPath = currentItem.folderPaths;
                }
                //if the current item is a Folder select the current path.
                if (currentItem.type == "Folder"){
                    folderPath = currentItem.folderPath;
                }
            }
            //Check user access
            $.PercUserService.getAccessLevel("percPage", -1, function(status, result){
                if(status == $.PercServiceUtils.STATUS_ERROR || result == $.PercUserService.ACCESS_READ || result == $.PercUserService.ACCESS_NONE)
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.new.page.button@New Page"), content: I18N.message("perc.ui.new.page.button@New Page Authorization")});
                    return;
                }
                else if(contentViewer)
                {
                    //contentViewer.confirm_if_dirty( $.PercNewPageDialog().openDialog(finderPath.join('/')));
                    contentViewer.confirm_if_dirty( function () {$.PercNewPageDialog().openDialog(finderPath.join('/'));});
                }
                else
                {
                    open_new_page_dialog($.PercNewPageDialog().openDialog(finderPath.join('/')),null);
                }
            }, folderPath);
        }

        finderRef.addPathChangedListener( newPageButtonListener );
        return newPageButton;
    };

})(jQuery);
