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
 * Handles the copy page action. 
 */
(function ($) {
    $.perc_build_preview_button = function (mcol, content) {
        
        var btn = $("<a id='perc-finder-preview' class='perc-font-icon icon-eye-open' href='#' title='" +I18N.message("perc.ui.preview.button@Launch Preview") + "'></a>")
            .perc_button().on("click",function (event) {
                launchPreview();
        });        
      
        function launchPreview()
        {
            if ($("#perc-finder-listview .perc-datatable-row-highlighted").length > 0)
            {
                var listSelectedRowData = $("#perc-finder-listview .perc-datatable-row-highlighted").data("percRowData");
                
                if (listSelectedRowData.category === "LANDING_PAGE" || listSelectedRowData.category === "PAGE")
                {
                    mcol.launchPagePreview(listSelectedRowData.id);                    
                }
                else if (listSelectedRowData.category === "ASSET")
                {
                    mcol.launchAssetPreview(listSelectedRowData.id);
                }
            }
            else
            {
                var selectedPage = $(".mcol-opened.perc-listing-type-percPage");
                var selectedAsset = $(".mcol-opened.perc-listing-category-ASSET");
                
                if (selectedPage.length > 0)
                {
                    mcol.launchPagePreview(selectedPage.data("spec").id);
                }
                else if (selectedAsset.length > 0)
                {
                    mcol.launchAssetPreview(selectedAsset.data("spec").id);                
                }
            }
        }
    
        function update_launch_preview_btn(path)
        {
            var selectedPageColumn = $(".mcol-opened.perc-listing-type-percPage");
            var selectedAssetColumn = $(".mcol-opened.perc-listing-category-ASSET");
            var selectedItemList = $("#perc-finder-listview .perc-datatable-row-highlighted");
            
            if (path[1] === "Sites" && path.length < 4)
            {
                enableButtonLaunchPreview(false);
            }
            else if(path[1]==="Recycling"){
                enableButtonLaunchPreview(false);
            }
            else if (selectedItemList.length > 0)
            {
                listSelectedRowData = selectedItemList.data("percRowData");
                if (listSelectedRowData.category == "LANDING_PAGE" || listSelectedRowData.category == "PAGE" || listSelectedRowData.category == "ASSET")
                {
                    enableButtonLaunchPreview(true);                 
                }
                else
                {
                    enableButtonLaunchPreview(false);
                }                
            }
            else if (selectedPageColumn.length > 0)
            {
                var last_path = selectedPageColumn.data("spec").path.split("/");
                if (last_path.length == path.length && $(last_path).last()[0] == $(path).last()[0])
                {
                    enableButtonLaunchPreview(true);
                }
                else
                {
                    enableButtonLaunchPreview(false);
                }
            }
            else if (selectedAssetColumn.length > 0) {
                if (selectedAssetColumn.data("spec") !== undefined) {

                last_path = selectedAssetColumn.data("spec").path.split("/");
                if (last_path.length === path.length && $(last_path).last()[0] === $(path).last()[0]) {
                    enableButtonLaunchPreview(true);
                } else {
                    enableButtonLaunchPreview(false);
                }
            }
            }
            else
            {
                enableButtonLaunchPreview(false);
            }
        }
        
        /**
         * Helper function to enable or disable the new folder button on finder.
         * @param flag(boolean) if <code>true</code> the button is enabled, otherwise the button is disabled.
         */
        function enableButtonLaunchPreview(flag)
        {
            if(flag)
            {
                $( "#perc-finder-preview" ).removeClass('ui-disabled').addClass('ui-enabled').off('click').on("click", launchPreview );

            }
            else
            {
                $( "#perc-finder-preview" ).addClass('ui-disabled').removeClass('ui-enabled').off('click');
            }
        }
        
        mcol.addPathChangedListener( update_launch_preview_btn );
        return btn;
    };
})(jQuery);

