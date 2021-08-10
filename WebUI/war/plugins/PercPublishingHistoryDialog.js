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
            if(!Array.isArray(data.ItemPublishingHistory)){
                pubHistory = [data.ItemPublishingHistory];
            }
            var dialogHTML = createPubHistoryTable();
            dialog = $(dialogHTML).perc_dialog( {
                resizable : false,
                title: I18N.message("perc.ui.publishing.history@Publishing History") + ": " + itemName + " (" + pubHistory[0].contentId + ")",
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
            return $("<div class='dataTables_wrapper' style='height: 100%; z-index: 4470;position:relative;' id='perc-pubhistory-container'>" +
                "<table id='pubHistoryTable' style='width:100%'>" +
                "<thead>" +
                "<tr>" +
                "<th style='text-align:left;width:10%;' id='perc-header-server'><span class='col-name'>" +
                I18N.message("perc.ui.publishing.history@Server") + "</span></th>" +
                "<th style='text-align:left;width:30%;' id='perc-header-location'><span class='col-name'>" +
                I18N.message("perc.ui.publishing.history@Location") + "</span></th>" +
                "<th style='text-align:left;width:10%;' id='perc-header-revision'><span class='col-name'>" +
                I18N.message("perc.ui.publishing.history@Revision") + "</span></th>" +
                "<th style='text-align:left;width:30%;' id='perc-header-date'><span class='col-name'>" +
                I18N.message("perc.ui.publishing.history@Date") + "</span></th>" +
                "<th style='text-align:left;width:10%;' id='perc-header-operation'><span class='col-name'>" +
                I18N.message("perc.ui.publishing.history@Operation") + "</span></th>" +
                "<th style='text-align:left;width:10%;' id='perc-header-status'><span class='col-name'>" +
                I18N.message("perc.ui.publishing.history@Status") + "</span></th>" +
                "</tr>" +
                "</thead>" +
                "<tbody></tbody>" +
                "</table>" +
                "</div>");
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
                    "<td style='vertical-align: middle;'><div class='data-cell perc-ellipsis perc-tooltip-container'>" + pubEntry.server + "<div class='perc-tooltip'>" + pubEntry.server + "</div></div></td>" +
                    "<td style='vertical-align: middle;'><div class='data-cell perc-ellipsis perc-tooltip-container'>" + pubEntry.location + "<div class='perc-tooltip'>" + pubEntry.location + "</div></div></td>" +
                    "<td style='vertical-align: middle;'><div class='data-cell perc-ellipsis'>" + pubEntry.revisionId + "</div></td>" +
                    "<td style='vertical-align: middle;'><div class='data-cell perc-ellipsis'>" + pubDateDate + " " + pubDateTime + "</div></td>" +
                    "<td style='vertical-align: middle;'><div class='data-cell perc-ellipsis'>" + pubEntry.operation + "</div></td>" +
                    "<td style='vertical-align: middle;'><div class='data-cell perc-ellipsis' title='" + errorMsg + "'>" + pubEntry.status + "</div></td>" +
                    "</tr>");

                $rowHTML.find("td:eq(1)").data("timedate", pubDate);
                $tbody.append($rowHTML);
            }

            var table = $("#pubHistoryTable").DataTable({
                "aaSorting": [[ 1, "desc" ]],
                "bFilter" : false,
                "bDestroy":true,
                "autoWidth" : true,
                "ordering": true,
                "searching": false,
                responsive: true,
                // turn on pagination and use sequential and random access pagination controls
                "paging" : true,
                "sPaginationType" : "full_numbers",
                "iDisplayLength" : 10,
                "bLengthChange" : false,
                "bInfo" : true,
                // if table has no rows show the following error
                "oLanguage": {"sZeroRecords": "No History Found"},
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
            $('#pubHistoryTable tbody').on( 'click', 'tr', function () {
                if ( $(this).hasClass('selected') ) {
                    $(this).removeClass('selected');
                }
                else {
                    table.$('tr.selected').removeClass('selected');
                    $(this).addClass('selected');
                }
            } );
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
            $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,serviceCallback);
            return deferred.promise();
        }

    }// End open dialog      

})(jQuery);
