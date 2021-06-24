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
 * Opens the Folder properties dialog, by creating.
 */
(function ($)
{
    /**
     * Constructs the menu entry.
     *
     * @param finder
     * @param contentViewer
     */
    $.perc_build_folderproperties_button = function (finder, contentViewer)
    {
        var btn = $('<a id="perc-finder-folderproperties" href="#" title="Folder properties">Folder properties</a>');
        var selectedItem;
        var pathItemSpec;

        /**
         * Listener function that is added to the finder listeners, this method gets called whenever a path change
         * happens on the finder.
         * @param path, an array of the path entries. For a page Page1 under Foo site Bar folder will be
         * ["Sites","Foo","Bar","Page1"].
         * Based on the supplied path decides whether the button needs to be enabled or disabled.
         */
        function finderPathChangedListener(path)
        {
            // Disable the menu entry under under //Design
            if (path.length > 1 && path[1] == $.perc_paths.DESIGN_ROOT_NO_SLASH) {
                enableButton(false);
                pathItemSpec = undefined;
                return;
            }

            if (path[1] == $.perc_paths.RECYCLING_ROOT_NO_SLASH) {
                enableButton(false);
                pathItemSpec = undefined;
                return;
            }

            // Reached this point, enable the button only on Folder selection
            selectedItem = $("#perc-finder-listview .perc-datatable-row-highlighted");
            if (selectedItem.length > 0) {
                // Element selected in list mode: if the selectedItem is not a Folder, make
                // selectedItem undefined, so the menu entry will not be enabled
                pathItemSpec = selectedItem.data("percRowData");
                if (pathItemSpec.type !== 'Folder' && pathItemSpec.type !== 'FSFolder') {
                    selectedItem = undefined;
                }
            }
            else {
                // If the jQuery selected collection is empty, it means that we are in column mode
                // In column mode is difficult to select the selected folder with jQuery, so we
                // will have to use the current path in the finder
                var folderSelector;
                folderSelector = '.mcol-listing.perc-listing-type-FSFolder.mcol-opened';
                folderSelector += ', .mcol-listing.perc-listing-type-Folder.mcol-opened';
                var highlightedElems = $(folderSelector);

                var i;
                for (i = 0; i < highlightedElems.length; i++) {
                    var self = $(highlightedElems[i]);
                    var elemPath;
                    if(typeof self.data('spec') !== 'undefined'){
                        elemPath = self.data('spec').path;
                    }
                    //var elemPath = self.data('spec').path;
                    // Now make the path comparisson
                    if (elemPath == path.join('/') + '/') {
                        selectedItem = self;
                        pathItemSpec = self.data('spec');
                    }
                }
            }

            // Enable the menu entry if the corresponding selectedItem is not undefined
            if (selectedItem != undefined && selectedItem.length > 0) {
                enableButton(true);
            }
            else {
                enableButton(false);
                pathItemSpec = undefined;
            }
        }

        /**
         * Helper function to enable or disable the button in the finder.
         * @param flag(boolean) if <code>true</code> the button is enabled, otherwise the button is disabled.
         */
        function enableButton(flag)
        {
            if (flag) {
                btn.removeClass('ui-disabled').addClass('ui-enabled').off('click').on("click",
                    function(evt){
                        clickHandler(evt);
                    } );
            }
            else {
                btn.addClass('ui-disabled').removeClass('ui-enabled').off('click');
            }
            btn.trigger('actions-change-enabled-state');
        }

        /**
         * Handler function invoked when clicking the Folder properties option in the actions menu.
         */
        function clickHandler(evt)
        {
            // We must perform some checks before opening the dialog
            if(pathItemSpec.category === "SYSTEM") {
                $.perc_utils.alert_dialog({
                    title: I18N.message("perc.ui.page.general@Warning"),
                    content: I18N.message("perc.ui.folder.properties.button@Path Nonvalid String")
                });
                return;
            }
            else if(pathItemSpec.accessLevel !== $.PercFolderHelper().PERMISSION_ADMIN) {
                var type = pathItemSpec.category === "SECTION_FOLDER" ? "section" : "folder";
                $.perc_utils.alert_dialog({
                    title: I18N.message("perc.ui.page.general@Warning"),
                    content: I18N.message("perc.ui.folder.properties.button@Permissions Error") + type + "."
                });
                return;
            }
            else if(pathItemSpec.category === "SECTION_FOLDER") {
                $.perc_utils.alert_dialog({
                    title: I18N.message("perc.ui.page.general@Warning"),
                    content: I18N.message("perc.ui.folder.properties.button@Use Navigation Editor")
                });
                return;
            }

            // Reached this point, we can open the dialog safely
            $.PercFolderPropertiesDialog().open(pathItemSpec, function(newName,status) {
                if (status === undefined) {
                    if (newName !== "" && newName !== pathItemSpec.name)
                    {
                        var newPath = pathItemSpec.folderPaths + "/" + newName;
                        newPath = newPath.substring(1);
                        newPath = newPath.replace('/Folders/$System$', '');
                        $.perc_finder().open(newPath.split("/"));
                    }
                    else
                    {
                        $.perc_finder().refresh();
                    }
                }
            });
        }

        /**
         * Updates the href attribute of the button according to the path and the corresponding
         * server side URL to download the file
         */
        function updateButtonUrl(flag)
        {
            var downloadUrl;
            if (flag) {
                downloadUrl = $.perc_paths.WEBRESOURCESMGT + '/' + finder.getCurrentPath().slice(3).join("/");
            }
            else {
                downloadUrl = "#";
            }
            btn.attr('href', downloadUrl);
        }

        // Finally, return the button element
        finder.addPathChangedListener( finderPathChangedListener );
        return btn;
    };

})(jQuery);
