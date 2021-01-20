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
 * Download button 
 */
(function ($)
{
    /**
     * Constructs the download button.
     *
     * @param finder
     * @param contentViewer
     */
    $.perc_build_download_button = function (finder, contentViewer)
    {
        //TODO: I18N With correct Formatting on btn below.
    	var btn = $('<a id="perc-finder-download" href="#" title="Click to download the selected file">Download File</a>');

        /**
         * Listener function that is added to the finder listeners, this method gets called whenever a path change
         * happens on the finder.
         * @param path, an array of the path entries. For a page Page1 under Foo site Bar folder will be
         * ["Sites","Foo","Bar","Page1"].
         * Based on the supplied path decides whether the button needs to be enabled or disabled.
         */
        function downloadButtonListener(path)
        {
            // Enable de button only on 
            //     1. "Design" view. We figure out this by checking the Path
            //     2. Add to the previous condition(s): file should be under path "/Design/Web Resources/themes"
            //        {0:"", 1:"Design", 2:"Web Resources", 3:"themes", 4:"THEMENAME", 5:"THEMEFILE"}
            //     3. The element selected is a file (not a folder)
            if(path[1] == $.perc_paths.DESIGN_ROOT_NO_SLASH && path.length > 4)
            {
                // Get the selected item from Column or List mode with the class FSFile
            	selectedItemSpec = $("#perc-finder-listview .perc-datatable-row-highlighted").data("percRowData");
                if (selectedItemSpec == undefined)
                {
                	var selectedItemSpec = $(".mcol-listing.perc-listing-type-FSFile.mcol-opened.perc_last_selected").data("spec");
                }
                // Now check the 3rd condition, that the element selected is a file under Design
                if (selectedItemSpec != undefined 
                        && selectedItemSpec.type == 'FSFile'
                        && selectedItemSpec.leaf)
                {
                    updateButtonUrl(true)
                    enableButton(true);
                    return;
                }
            }
            // Any other option disables the button
            updateButtonUrl(false)
            enableButton(false);
        }

        /**
         * Helper function to enable or disable the button in the finder.
         * @param flag(boolean) if <code>true</code> the button is enabled, otherwise the button is disabled.
         */
        function enableButton(flag)
        {
            if (flag)
            {
                btn.removeClass('ui-disabled').addClass('ui-enabled').unbind('click').click( launchDownload );
            }
            else
            {
                btn.addClass('ui-disabled').removeClass('ui-enabled').unbind('click');
            }
            btn.trigger('actions-change-enabled-state');
        }

        /**
         * Launches the download functionality specific to the browser on the selected item.
         */
        function launchDownload()
        {
            ;
        }
        
        /**
         * Updates the href attribute of the button according to the path and the corresponding
         * server side URL to download the file
         */
        function updateButtonUrl(flag)
        {
            var downloadUrl;
            if (flag)
            {
                downloadUrl = $.perc_paths.WEBRESOURCESMGT + '/' + finder.getCurrentPath().slice(3).join("/");
            }
            else
            {
                downloadUrl = "#";
            }
            btn.attr('href', downloadUrl);
        }
        
        // Finally, return the button element
        finder.addPathChangedListener( downloadButtonListener );
        return btn;
    };

})(jQuery);
