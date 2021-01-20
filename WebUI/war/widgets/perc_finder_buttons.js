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
            $( ".perc-finder-menu #perc-finder-preview" ).addClass('ui-disabled').removeClass('ui-enabled').unbind('click'); //Disabled
            $( ".perc-finder-menu #perc-finder-actions-button" ).addClass('ui-disabled').removeClass('ui-enabled').unbind('click'); //Disabled
            $( ".perc-finder-menu #perc-finder-delete" ).removeClass('ui-enabled').addClass('ui-disabled').unbind('click'); //Disabled
            $( ".perc-finder-menu #perc-finder-new-folder" ).addClass('ui-disabled').removeClass('ui-enabled').unbind('click'); //Disabled
            $( ".perc-finder-menu #mcol-new-page" ).addClass('ui-disabled').removeClass('ui-enabled').unbind('click'); //Disabled
            $( ".perc-finder-menu #mcol-new-asset" ).addClass('ui-disabled').removeClass('ui-enabled').unbind('click'); //Disabled
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
            
            //percButtons.append( "&nbsp;&nbsp;" );
            percButtons.append( dp );
            var isLibMode = typeof gInitialScreen !== 'undefined' && gInitialScreen == "library";
            if (!isLibMode) {
                // create the new page button and its handler
                var np = $.perc_build_new_page_button(finder, contentViewer);
                percButtons.append(np);
                
                // create the new asset button and its handler
                var na = $.PercNewAssetDialog.init(finder, contentViewer);
                percButtons.append(na);
            }
            // create the new folder button and its handler
            var nf = $.perc_build_new_folder_button( finder, contentViewer );
            percButtons.append( nf );

            //Adding new site event
            // create new site button and its handler {
            if (!isLibMode) {
                var ns = '<a id="perc-finder-new-site" class="perc-form ui-state-default perc-font-icon icon-sitemap" href="#" title="' + I18N.message("perc.ui.finder.buttons@Click New Site") + '"></a>';
                percButtons.append(ns);
            }

            var lp = $.perc_build_preview_button( finder, contentViewer );
            percButtons.append( lp );
            
            // create the actions button
            var ab = $.perc_build_actions_button( finder, contentViewer);
            percButtons.append( ab );
            
           $(".perc-finder-menu ").append(percButtons);

            function onSuccessCallBackHandler(sitename)
            {
              $.PercNavigationManager.goToLocation(
                 $.PercNavigationManager.VIEW_DESIGN, sitename, null, null, null,
                 $.perc_paths.SITES_ROOT + "/" + sitename , null, null);
            }

            $newSiteDialog = $.perc_createNewSiteDialog(onSuccessCallBackHandler);

            $('#perc-finder-new-site').unbind('click').click(createFn);
            
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
        function createFn() {
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
                $( ".perc-finder-menu #perc-finder-new-site" ).removeClass('ui-enabled').addClass('ui-disabled').unbind('click');
            }
            else{
                if(!$.PercNavigationManager.getPath().startsWith($.perc_paths.RECYCLING_ROOT)){
                    $( ".perc-finder-menu #perc-finder-new-site" ).removeClass('ui-disabled').addClass('ui-enabled').unbind('click').click( createFn );
                }else{
                    $( ".perc-finder-menu #perc-finder-new-site" ).removeClass('ui-enabled').addClass('ui-disabled').unbind('click');
                }
            }
        }
    }
})(jQuery);
