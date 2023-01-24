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
 * Upload button 
 */
(function ($)
{
    /**
     * Constructs the upload button.
     *
     * @param finder
     * @param contentViewer
     */
    $.perc_build_upload_button = function (finder, contentViewer)
    {
        var btn = $('<a id="perc-finder-upload" href="#" title="' +I18N.message("perc.ui.upload.button@Click Upload File") + '">Upload File...</a>')
            .on("click",function(evt){
                lauchClickHandler(evt);
            });
        
        /**
         * Listener function that is added to the finder listeners, this method gets called whenever a path change
         * happens on the finder.
         * @param path, an array of the path entries. For a page Page1 under Foo site Bar folder will be
         * ["Sites","Foo","Bar","Page1"].
         * Based on the supplied path decides whether the button needs to be enabled or disabled.
         */
        function uploadButtonChangePathListener(path)
        {
            if(path.length > 3 && $.perc_paths.DESIGN_THEMES === path[0] + '/' + path[1] + '/' + path[2] + '/' + path[3])
            {
                enableButton(true);
                return;
            }
            // Any other option disables the button
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
                    function(evt){
                        lauchClickHandler(evt);
                    } );
            }
            else
            {
                btn.addClass('ui-disabled').removeClass('ui-enabled').off('click');
            }
            $(document).trigger('actions-change-enabled-state');
        }

        /**
         * Launches the download functionality specific to the browser on the selected item.
         */
        function lauchClickHandler(evt)
        {
            // Open the dialog and pass it the current finder path
            $.perc_upload_theme_file_dialog.open(finder);
        }

        
        // Finally, return the button element
        finder.addPathChangedListener( uploadButtonChangePathListener );
        return btn;
    };
})(jQuery);
