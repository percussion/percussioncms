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

(function($) {
    /**
     * Common utility functions that are useful for creating data table from a given paginated item list.
     */
    $.PercDataTableUtil = {
        convertToDataTableData : convertToDataTableData,
        buildDataTableConfig : buildDataTableConfig,
        dateColumnDataBuilder : dateColumnDataBuilder,
        defaultColInfo: {
            name: "",
            label: "",
            type: "",
            width: "",
            dataUpdater: function(colName, rowData){
                return "";
            }
        }
    };
    
    /**
     * Converts pagedItemList data to $.PercDataTable consumable percData object.
     * @param {Object} colsInfo assumed to extended from defaultColInfo object.
     * @param {Object} pagedItemList assumed to be returned from server from PSPagedItemList object.
     */
    function convertToDataTableData(colsInfo, pagedItemList){
        var percData = [];
        if(pagedItemList.PagedItemList.childrenCount < 1)
            return percData;
        var rows = pagedItemList.PagedItemList.childrenInPage;
        if(!$.isArray(rows)){
            rows = [pagedItemList.PagedItemList.childrenInPage];
        }
        for(i=0; i<rows.length; i++){
            var row = rows[i];
            var cols = row.columnData.column;
            for(j=0;j<cols.length;j++){
                var name = cols[j]["@name"];
                row[name] = cols[j]["$"];
            }
            var dataRow = [];
            for(k=0;k<colsInfo.length;k++){
                var name = colsInfo[k].name;
                var dataUpdater = colsInfo[k].dataUpdater;
                dataRow.push($.isFunction(dataUpdater)?dataUpdater(name, row):row[name]);
            }
            var percContent = {"rowContent" : dataRow, "rowData" : row };
            percData.push(percContent);
        }
        return percData;
    }
    
    /**
     * Helper function that builds data table acceptable config.
     * @param {Object} colsInfo assumed to extended from defaultColInfo object.
     * @param {Object} pagedItemList assumed to be returned from server from PSPagedItemList object.
     */
    function buildDataTableConfig(colsInfo, pagedItemList){
        var dtConfig = {};
        dtConfig.percData = convertToDataTableData(colsInfo, pagedItemList);
        dtConfig.percColNames = [];
        dtConfig.percHeaders = [];
        dtConfig.percWidths = [];
        dtConfig.percTypes = [];
        for(c=0; c<colsInfo.length; c++)
        {
            var column  = colsInfo[c];
            dtConfig.percColNames.push(column.name);
            dtConfig.percHeaders.push(column.label);
            dtConfig.percWidths.push((column.width === -1 ? "*" : ($.browser.msie ? column.width - 20 : column.width ) ));
            dtConfig.percTypes.push({"sType" : column.type});
        }
        return dtConfig;
    }
    /**
     * Helper function that builds a generic date and date time column value.
     * @param {Object} colName assumed not null
     * @param {Object} rowData assumed not null.
     */
    function dateColumnDataBuilder(colName, rowData){
        var dateData = rowData[colName];
        var data = "&nbsp;";
        if(dateData){
            var dateParts = $.perc_utils.splitDateTime(dateData);
            var dateAndTime = dateParts.date + ", " + dateParts.time;
            data = "<div title='"+dateAndTime+"'>"+ dateParts.date + "</div>";
        }
        return data;
    }         
    
    /**
     * Creates a paging bar and appends it to the current element.
     * @param {Object} config an object that consists of the following parameters
     * {totalItems : "Total number of items", pageSize: "Page size", startIndex: "The starting index", "pagingCallback": function(){}}
     */
    $.fn.PercPagingBar = function(config){
        var totalItems = config.totalItems;
        var pageSize = config.pageSize;
        var startIndex = config.startIndex;
        var pagingCallback = config.pagingCallback;
        var totalPages = Math.ceil(totalItems/pageSize);
        var activePage = Math.ceil(startIndex/pageSize);
        
        var pageSpan = $("<span class='paginate_button'></span>");
        var first = pageSpan.clone().text("<<").addClass('perc-paging-first').data("startIndex",1);
        var sind = ((activePage-2)*pageSize + 1)<1?1:(activePage-2)*pageSize + 1;
        var prev = pageSpan.clone().text("<").addClass('perc-paging-prev').data("startIndex",sind);
        sind = ((activePage*pageSize) + 1)>totalPages*pageSize?((activePage-1)*pageSize) + 1:activePage*pageSize + 1;
        var next = pageSpan.clone().text(">").addClass('perc-paging-next').data("startIndex",sind);
        var last = pageSpan.clone().text(">>").addClass('perc-paging-last').data("startIndex",(totalPages-1)*pageSize+1);
        
        var pages = [];
        pages.push(activePage);
        for(i=1;i<=4 && pages.length<5;i++){
            if(activePage-i > 0)
                pages.unshift(activePage-i);
            if(activePage+i <= totalPages)
                pages.push(activePage+i);
        }
        
        var pagedItems = $("<span/>");
        for(i=0; i<pages.length; i++){
            var clName = activePage === pages[i]?"paginate_active":"perc-paging-number";
            var pageNum = pageSpan.clone().text(pages[i]).addClass(clName).data("startIndex",((pages[i]-1)*pageSize)+1);
            pagedItems.append(pageNum);
            if(i<pages.length-1){
                pagedItems.append(",");
            }
            pagedItems.append("&nbsp;");
        }
        
        var pagingBar = $("<div class='perc-paging-bar'/>");
        pagingBar.append(first).append("&nbsp;").append(prev).append("&nbsp;").append(pagedItems).append(next).append("&nbsp;").append(last);
        pagingBar.find(".paginate_button").click(function(){
            pagingCallback($(this).data("startIndex"));
        });
        pagingBar.find(".paginate_button").each(function(){
           if($(this).data("startIndex") === (activePage-1)*pageSize + 1){
               $(this).addClass("perc-disabled").unbind();
           }
        });
        
        $(this).append(pagingBar);       
    };
})(jQuery); 