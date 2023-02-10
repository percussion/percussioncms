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
 * Handles the copy page action.
 */
(function ($) {
    $.perc_build_preview_button = function (mcol, content) {

        var btn = $("<a id='perc-finder-preview' class='perc-font-icon icon-eye-open fas fa-eye' href='#' title='" +I18N.message("perc.ui.preview.button@Launch Preview") + "'></a>")
            .perc_button().on("click",function (event) {
                launchPreview();
            });

        function launchPreview(event)
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
                $( "#perc-finder-preview" ).removeClass('ui-disabled').addClass('ui-enabled').off('click').on("click",
                    function(evt){
                        launchPreview(evt);
                    } );

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

