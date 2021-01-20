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

(function($){

    var TABLE_STATUS_FOOTER_PADDING_TOP = 5;
    // number of rows per page
    var itemsPerPage = 5;
    
    // grab necessary Perc APIs
    var PercActivityService  = percJQuery.PercActivityService;
    var PercServiceUtils     = percJQuery.PercServiceUtils;
    var perc_paths           = percJQuery.perc_paths;
    
    var isLargeColumn = true;
    var tableDiv;

    // API for this library
    $.fn.PercActivityGadget = function(site, durationType, durationValue, rows) {
        // never show a scrollbar in the gadget
        $("body").css("overflow","hidden");
        // resize gadget to fit the rows
        itemsPerPage = rows;
        
        tableDiv = $(this);
        if(site == null)
            site="";
        loadGadget(site, durationType, durationValue);
    }

    function loadGadget(site, durationType, durationValue) {
        PercActivityService
            .getContentActivity(
                perc_paths.SITES_ROOT+'/'+site,
                durationType,
                durationValue,
                function(status, data){
                    if(status == PercServiceUtils.STATUS_SUCCESS) {
                        createActivityTable(data);
                    } else {
                        displayErrorMessage(data);
                    }
                });
    }
    
    function createActivityTable(data) {
        var percData = [];
        var contentActivity = data.ContentActivity;
        for(i in contentActivity) 
        {
            var activity  = contentActivity[i];
            
            var name      = activity.name;
            var title     = name;
            var total     = activity.publishedItems;
            var changes   = activity.newItems + activity.updatedItems + activity.archivedItems;
            var news      = activity.newItems;
            var updates   = activity.updatedItems;
            var takeDowns = activity.archivedItems;
            var pending   = activity.pendingItems;
            
            var row = {rowContent : [name, total, changes, news, updates, takeDowns, pending]};
            
            percData.push(row);
        }
        
        var headers = ["Name","Total","Changes","New","Updates", "Take Downs", "Pending"];
        
        var aoColumns = [
                { sType: "string"  },
                { sType: "numeric" },
                { sType: "numeric" },
                { sType: "numeric" },
                { sType: "numeric" },
                { sType: "numeric" },
                { sType: "numeric" }
            ];
                
        var percVisibleColumns = null;
        isLargeColumn = gadgets.window.getDashboardColumn() == 1;
        if(!isLargeColumn)
            percVisibleColumns = [0,1,2,6];

        var percColumnWidths = ["*","42","64","38","60","84","60"];

        var config = {percColumnWidths : percColumnWidths, percVisibleColumns : percVisibleColumns, iDisplayLength : itemsPerPage, percData : percData, percHeaders : headers, aoColumns : aoColumns};
        
        miniMsg.dismissMessage(loadingMsg);

        tableDiv.PercDataTable(config);
    }
    
    function displayErrorMessage(message) {
        tableDiv.append("<div class='perc-gadget-errormessage'>" + message + "</div>");
        miniMsg.dismissMessage(loadingMsg);
    }
})(jQuery);
