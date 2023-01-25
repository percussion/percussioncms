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
                var timeStamp = new Date(pubEntry.publishedDate).getTime(); //This timestamp will be used to set the data-order attribute of datatable to sort it as date on basis of timestamp
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
                    "<td data-order ='"+timeStamp+"' style='vertical-align: middle;'><div class='data-cell perc-ellipsis'>" + pubDateDate + " " + pubDateTime + "</div></td>" +
                    "<td style='vertical-align: middle;'><div class='data-cell perc-ellipsis'>" + pubEntry.operation + "</div></td>" +
                    "<td style='vertical-align: middle;'><div class='data-cell perc-ellipsis' title='" + errorMsg + "'>" + pubEntry.status + "</div></td>" +
                    "</tr>");

                $rowHTML.find("td:eq(1)").data("timedate", pubDate);
                $tbody.append($rowHTML);
            }

            var table = $("#pubHistoryTable").DataTable({
                "order": [[ 3, "desc" ]],
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
