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
* Publishing History Dialog
*/
(function($){
    //Public API for the publishing history dialog.
    $.PercPublishingHistoryDialog = {
            open: openDialog,
            ITEM_TYPE_ASSET:"asset",
            ITEM_TYPE_PAGE:"page",
            ITEM_MODE_EDIT:"edit",
            ITEM_MODE_VIEW:"view"
    };
    
    /**
     * Opens the publishing history dialog and shows the publishing history in a table for the supplied item.
     * @param itemId(String), assumed to be a valid guid of the item (Page or Asset)
     * @param itemName(String) name of the item to be shown part of the dialog.
     * @param itemType(String), The type of the item. Use ITEM_TYPE_XXX constants.
     */
    function openDialog(itemId, itemName, itemType) 
    {
        // custom column sorting for Modified and Published columns (We need this before the table is assigned)
        $.fn.dataTableExt.afnSortData['perc-dom-text'] = function  ( oSettings, iColumn ) {
            var aData = [];
            $( 'td:eq('+iColumn+')', oSettings.oApi._fnGetTrNodes(oSettings) ).each( function () {
                var data = $(this).text();
                aData.push( data );
            });
            return aData;
        };
        // custom column sorting for Modified column
        $.fn.dataTableExt.afnSortData['perc-dom-date'] = function  ( oSettings, iColumn ) {
            var aData = [];
            $( 'td:eq('+iColumn+')', oSettings.oApi._fnGetTrNodes(oSettings) ).each( function () {
                var data = $(this).data("timedate");
                aData.push( data );
            });
            return aData;
        };
    	
    	
    	getItemPublishingHistory(itemId).done(function(data){
    		if(data.ItemPublishingHistory.length === 0){
                $.perc_utils.alert_dialog({"title":I18N.message("perc.ui.publishing.history@No Publishing History") + itemType,"content":I18N.message("perc.ui.publishing.history@No Publishing History For") + ' ' + itemType});
                return;
    		}
    		createDialog(data);
    	}).fail(function(errorMsg){
            $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: errorMsg});

    	});
        var dialog;
        /**
        * Creates the dialog and on dialog open calls the addHistoryRows to add the history entries.
        */
        function createDialog(data)
        {
            var self = this;
            var pubHistory = data.ItemPublishingHistory;
            if(!$.isArray(data.ItemPublishingHistory)){
            	pubHistory = [data.ItemPublishingHistory];
            }
            var dialogHTML = createPubHistoryTable();
            dialog = $(dialogHTML).perc_dialog( {
                resizable : false,
                title: I18N.message("Publishing History") + ": " + itemName,
                modal: true,
                closeOnEscape : true,
                percButtons:{
                    "Close":{
                        click: function(){
                            dialog.remove();
                        },
                        id: "perc-pubhistory-dialog-close"
                    }
                },
                id: "perc-pubhistory-dialog",
                open:function(){
                    addHistoryRows(pubHistory);
                },
                width: 700,
                height:490
            });

        }
        
        /**
         * Creates the publisshing history table html, with wrapper div. Empty tbody is added.
         * @return publishing history table html.
         */
        function createPubHistoryTable()
        {
            var $dialogHtml = $("<div class='dataTables_wrapper' style='height: 100%; z-index: 4470;position:relative;' id='perc-pubhistory-container'>" +
                                "<table id='pubHistoryTable' width='100%''>" +
                                    "<thead>" +
                                        "<tr>" +
                                            "<th align='left' width='30%' id='perc-header-server'><span class='col-name'>Server</span></th>" +
                                            "<th align='left' width='30%' id='perc-header-date'><span class='col-name'>Date</span></th>" +
                                            "<th align='left' width='20%' id='perc-header-status'><span class='col-name'>Status</span></th>" +
                                        "</tr>" +
                                    "</thead>" +
                                    "<tbody></tbody>" +
                                 "</table>" +
                             "</div>");
            return $dialogHtml;
        }
        
        /**
         * Adds the publishing history table header and data rows, then applies the data table plugin.
         */
        function addHistoryRows(pubHistory)
        {
            var itemCount = pubHistory.length;
            var $tbody = $("#pubHistoryTable").find("tbody");
            for(var i in pubHistory)
            {
                var pubEntry = pubHistory[i];
                var pubDate = new Date(pubEntry.publishedDate);
                var pubDateParts = $.perc_utils.splitDateTime(pubDate);
                var pubDateDate = pubDateParts.date;
                var pubDateTime = pubDateParts.time;
                var errorMsg="";
                if(pubEntry.status === "FAILURE"){
                	errorMsg = " title=\"" + pubEntry.errorMessage + "\" ";
                }
                var $rowHTML = $(
                "<tr>" +
                    "<td valign='middle'><div class='data-cell perc-ellipsis'>" + pubEntry.server + "</div></td>" +
                    "<td valign='middle'><div class='data-cell perc-ellipsis'>" + pubDateDate + " " + pubDateTime + "</div></td>" +
                    "<td valign='middle'><div class='data-cell perc-ellipsis'>" + pubEntry.operation + "</div></td>" +
                    "<td valign='middle'><div class='data-cell perc-ellipsis'" + errorMsg + ">" + pubEntry.status + "</div></td>" +
                "</tr>");
                $rowHTML.find("td:eq(1)").data("timedate", pubDate);
                $tbody.append($rowHTML);
            }

            $("#pubHistoryTable").dataTable({
                "aaSorting": [[ 1, "desc" ]],
                "bFilter" : false,
                "bDestroy":true,
                "bAutoWidth" : false,
                // turn on pagination and use sequential and random access pagination controls
                "bPaginate" : true,
                "sPaginationType" : "full_numbers",
                "iDisplayLength" : 10,
                "bLengthChange" : false,
                "bInfo" : true,
                // if table has no rows show the following error
                "oLanguage": {"sZeroRecords": "No History Found"},
                // set custom column sorter data types
                "aoColumns": [
                    null,
                    {"sType": "date" , "sSortDataType": "perc-dom-date"},
                    null,
                    null
                ],
                // if on first or last page, update the disabled color of the sequential pagination controls
                "fnFooterCallback": function( nFoot, aasData, iStart, iEnd, aiDisplay ) {
                    // set them all to their default active color
                    $(".first").addClass("perc-active").removeClass("perc-disabled");
                    $(".previous").addClass("perc-active").removeClass("perc-disabled");
                    $(".next").addClass("perc-active").removeClass("perc-disabled");
                    $(".last").addClass("perc-active").removeClass("perc-disabled");
                    // if on the first page disable First and Previous controls
                    if(iStart === 0) {
                        $(".first").addClass("perc-disabled").removeClass("perc-active");
                        $(".previous").addClass("perc-disabled").removeClass("perc-active");
                    }
                    // if on the last page disable Last and Next controls
                    if(iEnd === itemCount) {
                        $(".next").addClass("perc-disabled").removeClass("perc-active");
                        $(".last").addClass("perc-disabled").removeClass("perc-active");
                    }
                }
            });
        }
        
        /**
         * Makes a service call to fetch the publishing history entries from server.
         * If successful resolves with data object returned by the service, rejects with error message.
         */
        function getItemPublishingHistory(itemId)
        {
            var deferred  = $.Deferred();
            var url = $.perc_paths.ITEM_PUB_HISTORY + itemId;
            var serviceCallback = function(status, result){
                    if(status === $.PercServiceUtils.STATUS_SUCCESS){
                        deferred.resolve(result.data);
                    } else {
                        var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                        $.perc_utils.info(I18N.message("perc.ui.publishing.history@Error Fetching History") + itemId + defaultMsg);
                        deferred.reject(defaultMsg);
                    }
            };
            $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,true,serviceCallback);
            return deferred.promise();
        }

    }// End open dialog      

})(jQuery);
