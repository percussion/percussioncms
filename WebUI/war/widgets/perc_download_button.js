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
            if(path[1] === $.perc_paths.DESIGN_ROOT_NO_SLASH && path.length > 4)
            {
                // Get the selected item from Column or List mode with the class FSFile
            	var selectedItemSpec = $("#perc-finder-listview .perc-datatable-row-highlighted").data("percRowData");
                if (typeof selectedItemSpec === 'undefined')
                {
                	 selectedItemSpec = $(".mcol-listing.perc-listing-type-FSFile.mcol-opened.perc_last_selected").data("spec");
                }
                // Now check the 3rd condition, that the element selected is a file under Design
                if (typeof selectedItemSpec !== 'undefined' &&
                    selectedItemSpec.type === 'FSFile' &&
                    selectedItemSpec.leaf)
                {
                    updateButtonUrl(true);
                    enableButton(true);
                    return;
                }
            }
            // Any other option disables the button
            updateButtonUrl(false);
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
                btn.removeClass('ui-disabled').addClass('ui-enabled').off('click').on("click",
                    function(evt) {
                        launchDownload(evt);
                    });
            }
            else
            {
                btn.addClass('ui-disabled').removeClass('ui-enabled').off('click');
            }
            btn.trigger('actions-change-enabled-state');
        }

        /**
         * Launches the download functionality specific to the browser on the selected item.
         */
        function launchDownload(evt)
        {
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
