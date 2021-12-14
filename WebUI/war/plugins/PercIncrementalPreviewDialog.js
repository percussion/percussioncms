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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

/**
* Comments Dialog
*/
(function($){
    //Public API for the comments dialog.
    $.PercIncrementalPreviewDialog = {
            open: openDialog,
            CANCEL:"cancel",
            CONTINUE:"continue",
            PAGE_SIZE: 15
    };
    //Global variables
    var gSiteName;
    var gServerName;
    var gShowRelated;
    var gOnRelatedView;
    var gDialog;
    /**
     * Opens the incremental preview items dialog, lets the user to preview the items being published if they continue.
     * @param siteName(String), assumed to be a valid site name
     * @param serverName(String) assumed to be a valid publishing server name
     */
    function openDialog(siteName, serverName, showRelated, callback){
        gSiteName = siteName;
        gServerName = serverName;
        gShowRelated = showRelated;
        gOnRelatedView = false;
        var $dialogHtml = $("<div><div id='perc-incremental-preview'><div id='perc-incremental-preview-table'/><div id='perc-incremental-preview-paging'/></div><div style=\"display:none\" id='perc-incremental-related-preview'><div id='perc-incremental-related-preview-table'/><div id='perc-incremental-related-preview-paging'/></div></div>");
        gDialog = _createDialog($dialogHtml, callback);
        _createPagedTable(1);
    }// End open dialog
    /**
     * Creates the dialog
     * @param {Object} $dialogHtml
     * @param {Object} callback
     */
    function _createDialog($dialogHtml, callback){
        var dialog = $dialogHtml.perc_dialog( {
            resizable : false,
            title: "Incremental Publishing Preview",
            modal: true,
            closeOnEscape : true,
            percButtons:{
                "Continue Preferred":{
                    click: function(){
                        if(gShowRelated && !gOnRelatedView){
                            gOnRelatedView = true;
                            $dialogHtml.find("#perc-incremental-preview").hide();
                            $dialogHtml.find("#perc-incremental-related-preview").show();
                            _createPagedTable(1);
                        } else {
                            dialog.remove();
                            callback($.PercIncrementalPreviewDialog.CONTINUE);
                        }
                    },
                    id: "perc-incremental-dialog-continue"
                },
                "Cancel":{
                    click: function(){
                        dialog.remove();
                        callback($.PercIncrementalPreviewDialog.CANCEL);
                    },
                    id: "perc-incremental-dialog-close"
                }
            },
            id: "perc-incremental-dialog",
            width: 1000,
            height:650
        });
        return dialog;
    }
    /**
     * Empties the table and paging bar and calls _createPagedTable with the supplied startIndex to create the table and paging bar.
     * @param {Object} startIndex
     */
    function paginateItems(startIndex){
    	if(gOnRelatedView){
            gDialog.find("#perc-incremental-related-preview-table").empty();
            gDialog.find("#perc-incremental-related-preview-paging").empty();
    	} else {
            gDialog.find("#perc-incremental-preview-table").empty();
            gDialog.find("#perc-incremental-preview-paging").empty();
    	}
        _createPagedTable(startIndex);
    }
    
    /**
     * Creates the paged table for the incremental items, makes a call to the server and creates the table.
     * @param {Object} startIndex
     */
    function _createPagedTable(startIndex){
    	var callback = function(status, data){
            if(status){
                var colsInfo = [_createColInfo("sys_title","Filename","string","150", function(colName, rowData){return "<div title='" + rowData['path'] + "'>" + rowData[colName] + "</div>";}),
                               _createColInfo("name", "Title", "string","-1",null),
                               _createColInfo("sys_contentlastmodifieddate", "Last Modified", "html", "100", $.PercDataTableUtil.dateColumnDataBuilder),
                               _createColInfo("sys_contentlastmodifier","Last Modified By", "string", "100", null),
                               _createColInfo("sys_postdate", "Last Published", "html", "100", $.PercDataTableUtil.dateColumnDataBuilder)];
                //If there are no items then hide the continue button and change cancel button to close
                if(!gOnRelatedView && data.PagedItemList.childrenCount < 1){
                    $("#perc-incremental-dialog-continue").hide();   
                }

                var config = $.PercDataTableUtil.buildDataTableConfig(colsInfo, data);
                var percData     = config.percData;
                var aoColumns    = config.percTypes;
                var percHeaders  = config.percHeaders;
                var percColNames = config.percColNames;
                var percWidths   = config.percWidths;
                var percColumnWidths = percWidths;
                var noItemsMsg = gOnRelatedView ? "No unapproved related items found." : "No items are available for incremental publishing.";
                var configDT = {percRowClickCallback : $.noop, percRowDblclickCallback : $.noop, 
                                percColumnWidths : percColumnWidths, percData : percData, 
                                percHeaders : percHeaders, aoColumns : aoColumns};
                configDT.oLanguage = {"sZeroRecords": I18N.message("perc.ui.incremental.preview@Incremental Publishing")};
                configDT.bPaginate = false;
                configDT.bInfo = false;
                configDT.bSort = false;
                configDT.sortFunction = config.sortFunction;
                configDT.sortColumn = config.sortColumn;
                configDT.sortOrder = config.sortOrder;
                configDT.percColNames = percColNames;
                configDT.iDisplayLength = $.PercIncrementalPreviewDialog.PAGE_SIZE;
                var container = gOnRelatedView ? gDialog.find("#perc-incremental-related-preview-table") : gDialog.find("#perc-incremental-preview-table");
                container.empty();
                container.PercDataTable(configDT);
                if(data.PagedItemList.childrenCount > 0){
                    var pagingConfig = {totalItems:data.PagedItemList.childrenCount,startIndex:startIndex,pageSize:$.PercIncrementalPreviewDialog.PAGE_SIZE, pagingCallback:paginateItems};
                    var pagingContainer = gOnRelatedView ? gDialog.find("#perc-incremental-related-preview-paging") : gDialog.find("#perc-incremental-preview-paging");
                    pagingContainer.PercPagingBar(pagingConfig);
                }
            }
        };
    	
    	if(gOnRelatedView){
        	$.PercPublisherService().getIncrementalRelatedItems(gSiteName, gServerName, startIndex,  $.PercIncrementalPreviewDialog.PAGE_SIZE, callback);
    	} else {
        	$.PercPublisherService().getIncrementalItems(gSiteName, gServerName, startIndex,  $.PercIncrementalPreviewDialog.PAGE_SIZE, callback);
    	}
    }
    /**
     * Helper function that creates a colInfo object required by data table utility functions.
     */
    function _createColInfo(name, label, type, width, dataUpdater){
        var colInfo = $.extend({},$.PercDataTableUtil.defaultColInfo);
        colInfo.name = name;
        colInfo.label = label;
        colInfo.type = type;
        colInfo.width = width;
        colInfo.dataUpdater = dataUpdater;
        return colInfo;
    }
})(jQuery);
