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
                if (fieldValue != "@all")
                    panelContent +=     '<span class="perc-search-criteria-panel-content-spans" id="perc-search-criteria-panel-' + index + '">' + labels[index] +' = <span class="perc-search-criteria-panel-values-spans" id="perc-search-criteria-panel-' + index + '-value">' + fieldName + ';</span></span>&nbsp;';
            });
            panelContent += '    </div>';
       }

       panelContent += '</div>';       

       if (previousPanel.length == 0)
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
       
       container.find("#perc-search-criteria-panel-content-select-filters").click(function()
       {
            if ($.isFunction(config.selectFiltersCallback))
            {
                config.selectFiltersCallback();
            }
       });
       
       if ($.isFunction(config.refreshSearchCallback))
       {
           container.find("#perc-search-criteria-panel-content-refresh-search").click(function()
           {
                if ($.isFunction(config.refreshSearchCallback))
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
        if (value["value"] != "@all")
            flag = false;
        });
        
        return flag;
        
    }
    
})(jQuery);
