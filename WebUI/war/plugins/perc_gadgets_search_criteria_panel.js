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
* Schedules Dialog
*/
(function($){
    $.perc_gadgets_search_criteria_panel = {
            buildSearchInfoPanel: buildSearchInfoPanel
    };
    
    /**
     * Opens the schedule dialog.
     * @param itemId(String), assumed to be a valid guid of the item (Page or Asset)
     */
    function buildSearchInfoPanel(config)
    {
        var container = config.container;
        var criteriaObj = config.criteriaObj;
        var labels = config.labels;
        
        var previousPanel = container.find('#perc-search-criteria-panel-content');
        var panelContent;
        panelContent =  '<div id="perc-search-criteria-panel-content" >';
        panelContent += '    <div id="perc-search-criteria-panel-content-header">';
        panelContent += '        <button id="perc-search-criteria-panel-content-refresh-search" class="btn btn-primary" title="Click to refresh the results">Refresh</button>';
        panelContent += '        <a id="perc-search-criteria-panel-content-select-filters" href="#" title="Click to select the filters used to search">Select Filters</a>';
        panelContent += '    </div>';        
        if (criteriaObj != null && !checkAllValues(criteriaObj))
        {
            panelContent += '    <div id="perc-search-criteria-panel-content-fields" >';
            panelContent +=     '    <span id="perc-search-criteria-panel-filters">Filters: </span>';
            var numberIndex = 0;
            $.each(criteriaObj, function(index, value) {
                var fieldName = value["name"];
                var fieldValue = value["value"];
                if (fieldValue !== "@all")
                    panelContent +=     '<span class="perc-search-criteria-panel-content-spans" id="perc-search-criteria-panel-' + index + '">' + labels[index] +' = <span class="perc-search-criteria-panel-values-spans" id="perc-search-criteria-panel-' + index + '-value">' + fieldName + ';</span></span>&nbsp;';
            });
            panelContent += '    </div>';
       }

       panelContent += '</div>';       

       if (previousPanel.length === 0)
       {
           container.append(panelContent);
       }
       else
       {
           previousPanel.replaceWith(panelContent);
       }
       if (criteriaObj != null && !checkAllValues(criteriaObj))
           $(container).find(".perc-search-criteria-panel-content-spans:last .perc-search-criteria-panel-values-spans").html(
                $(container).find(".perc-search-criteria-panel-content-spans:last .perc-search-criteria-panel-values-spans").html().slice(0,-1));
       
       container.find("#perc-search-criteria-panel-content-select-filters").on("click",function()
       {
            if (typeof config.selectFiltersCallback === "function")
            {
                config.selectFiltersCallback();
            }
       });
       
       if (typeof config.refreshSearchCallback === "function")
       {
           container.find("#perc-search-criteria-panel-content-refresh-search").on("click",function()
           {
                if ( typeof config.refreshSearchCallback === "function")
                {
                    config.refreshSearchCallback();
                }
           });
       }
    }// End open dialog
    
    function checkAllValues(criteriaObj)
    {
        var flag = true;
        $.each(criteriaObj, function(index, value) {
        if (value["value"] !== "@all")
            flag = false;
        });
        
        return flag;
        
    }
    
})(jQuery);
