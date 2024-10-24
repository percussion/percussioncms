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
 * Top-level functions for Web Management page.
 */

(function($)
{
    $.percFinderButtons = function()
    {
        // singleton to keep track of dirty state across various types of resources such as pages, templates and assets
        var dirtyController = $.PercDirtyController;
        
        var finderButtons = {
            createButtons : createButtons,
            disableAllButtonsButSite : disableAllButtonsButSite 
        };
        return finderButtons;
        
        /**
         * Disables all the action buttons in the finder leaving the Create Site button as it is (enabled)
         */
        function disableAllButtonsButSite()
        {
            $( ".perc-finder-menu #perc-finder-preview" ).addClass('ui-disabled').removeClass('ui-enabled').off('click'); //Disabled
            $( ".perc-finder-menu #perc-finder-actions-button" ).addClass('ui-disabled').removeClass('ui-enabled').off('click'); //Disabled
            $( ".perc-finder-menu #perc-finder-delete" ).removeClass('ui-enabled').addClass('ui-disabled').off('click'); //Disabled
            $( ".perc-finder-menu #perc-finder-new-folder" ).addClass('ui-disabled').removeClass('ui-enabled').off('click'); //Disabled
            $( ".perc-finder-menu #mcol-new-page" ).addClass('ui-disabled').removeClass('ui-enabled').off('click'); //Disabled
            $( ".perc-finder-menu #mcol-new-asset" ).addClass('ui-disabled').removeClass('ui-enabled').off('click'); //Disabled
        }
        
        // this is called by PercPageView.js
        // creates buttons at the top right of the page Editor
        // adds them to .perc-finder-menu
        // each button invokes a handler when clicked
        // handlers get passed the finder and the contentviewer passed in from PercPageView
        function createButtons(finder, contentViewer)
        {
            // create the delete page button and its handler
            var dp = $.perc_build_delete_page_button( finder, contentViewer );
            var percButtons = $('<div class="perc-finder-buttonbar"/>');

            percButtons.append( dp );
            var np;
            var na;
            var isLibMode = typeof gInitialScreen !== 'undefined' && gInitialScreen === "library";
            if (!isLibMode) {
                // create the new page button and its handler
                 np = $.perc_build_new_page_button(finder, contentViewer);
                percButtons.append(np);
                
                // create the new asset button and its handler
                 na = $.PercNewAssetDialog.init(finder, contentViewer);
                percButtons.append(na);
            }
            // create the new folder button and its handler
            var nf = $.perc_build_new_folder_button( finder, contentViewer );
            percButtons.append( nf );

            //Adding new site event
            // create new site button and its handler {
            var ns;
            if (!isLibMode) {
                 ns = '<a id="perc-finder-new-site" class="perc-form ui-state-default perc-font-icon icon-sitemap fas fa-sitemap" href="#" title="' + I18N.message("perc.ui.finder.buttons@Click New Site") + '"></a>';
                percButtons.append(ns);
            }

            var lp = $.perc_build_preview_button( finder, contentViewer );
            percButtons.append( lp );
            
            // create the actions button
            var ab = $.perc_build_actions_button( finder, contentViewer);
            percButtons.append( ab );
            
           $(".perc-finder-menu ").append(percButtons);

		    var tabIndex = 19;
			$( ".perc-finder-menu" ).find('a').each(function (i, el) {
					this.setAttribute("tabindex", tabIndex--);
			});

            function onSuccessCallBackHandler(sitename)
            {
              $.PercNavigationManager.goToLocation(
                 $.PercNavigationManager.VIEW_DESIGN, sitename, null, null, null,
                 $.perc_paths.SITES_ROOT + "/" + sitename , null, null);
            }

            $newSiteDialog = $.perc_createNewSiteDialog(onSuccessCallBackHandler);

            $('#perc-finder-new-site').off('click').on("click",
                function(evt){
                    createFn(evt);
                });
            
            finder.addPathChangedListener( update_newsite_btn );

            var finderButtons = {
                "delete" : dp,
                "newPage" : np,
                "newFolder" : nf,
                "newAsset" : na,
                "newSite" : ns,
                "launchPreview" : lp,
                "launchAction" : ab
            };
            
            // return the list of buttons to PercPageView
            return finderButtons;
        }

        /**
         * Create new site function.
         */
        function createFn(evt) {
            // check to see if dirty before allowing creating a new site
            // show confirm dialog if dirty
            dirtyController.confirmIfDirty(function(){
            $newSiteDialog.perc_wizard('open');
            //remove the unwanted stupid z-index values
            $(".ui-dialog.ui-widget.ui-widget-content.ui-corner-all.perc-dialog.perc-dialog-corner-all.ui-draggable").find('div').css('z-index', '');
            });
        }

        /**
         * Update the new site button state based on the current user.
         * The button will only be enabled for Admin users.
         */
        function update_newsite_btn() {
            if(!$.PercNavigationManager.isAdmin()){
                $( ".perc-finder-menu #perc-finder-new-site" ).removeClass('ui-enabled').addClass('ui-disabled').off('click');
            }
            else{
                if(!$.PercNavigationManager.getPath().startsWith($.perc_paths.RECYCLING_ROOT)){
                    $( ".perc-finder-menu #perc-finder-new-site" ).removeClass('ui-disabled').addClass('ui-enabled').off('click').on('click',
                        function(evt){
                            createFn(evt);
                        } );
                }else{
                    $( ".perc-finder-menu #perc-finder-new-site" ).removeClass('ui-enabled').addClass('ui-disabled').off('click');
                }
            }
        }
    };
})(jQuery);
