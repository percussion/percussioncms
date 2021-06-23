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
