
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
 * Revisions Dialog
 **/
(function($){
    //Public API for the revisions dialog.
    $.PercRevisionDialog = {
        open: openDialog,
        ITEM_TYPE_ASSET:"asset",
        ITEM_TYPE_PAGE:"page",
        ITEM_MODE_EDIT:"edit",
        ITEM_MODE_VIEW:"view"
    };
    /**
     * Opens the revisions dialog and shows the revisions in a table for the supplied item.
     * @param itemId(String), assumed to be a valid guid of the item (Page or Asset)
     * @param itemName(String) name of the item to be shown part of the dialog.
     * @param itemType(String), The type of the item. Use ITEM_TYPE_XXX constants.
     * @param itemMode (String), the mode for the dialog, view or edit, used for showing the appropriate actions.
     * Use ITEM_MODE_XXX constants.
     */
    function openDialog(itemId, itemName, itemType, itemMode)
    {

        //Makes a service call and gets the revisions, passes the createDialog as the callback
        $.PercRevisionService.getRevisionDetails(itemId, createDialog);
        var dialog;
        /**
         * Creates the dialog and sets the field values from the supplied result.data object.
         * On dialog open calls the addRevisionRows to add the revision rows.
         */
        function createDialog(status,result)
        {
            var self = this;

            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                $.perc_utils.alert_dialog({title: 'Error', content: defaultMsg});
                callback(false);
                return;
            }

            percDialogObject = {
                resizable : false,
                title: I18N.message("perc.ui.revisionDialog.title@Revisions") + ": " + itemName,
                modal: true,
                closeOnEscape : false,
                percButtons:{},
                id: "perc-revisions-dialog",
                open:function(){
                    addRevisionRows(result.data);
                },
                width: 700,
                height:490
            };

            percDialogObject.percButtons.Close = {
                click: function(){
                    dialog.remove();
                },
                id: "perc-revisions-dialog-close"
            };

            var dialogHTML = createRevisionTable();
            dialog = $(dialogHTML).perc_dialog(percDialogObject);
            $("#perc-revisions-container").css( "min-height", "350px" );
            $("#revisionsTable_paginate").css( "right", "-25px" );

        }

        /**
         * Creates the revision table html, with wrapper div. Empty tbody is added.
         * @return revision dialog html.
         */
        function createRevisionTable()
        {
            return $("<div class='dataTables_wrapper' style='height: 100%; z-index: 4470;position:relative;' id='perc-revisions-container'>" +
                "<table id='revisionsTable' style='width:100%'>" +
                "<thead>" +
                "<tr>" +
                "<th style='text-align:left;width:10%' id='perc-header-revision'><span class='col-name'>" +
                I18N.message("perc.ui.revisionDialog@Revision") + "</span></th>" +
                "<th style='text-align:left;width:20%' id='perc-header-status'><span class='col-name'>" +
                I18N.message("perc.ui.revisionDialog@Status") + "</span></th>" +
                "<th style='text-align:left;width:35%' id='perc-header-last-modified'><span class='col-name'>" +
                I18N.message("perc.ui.revisionDialog@Last Modified") + "</span></th>" +
                "<th style='text-align:left;width:20%' id='perc-header-last-modifier'><span class='col-name'>" +
                I18N.message("perc.ui.revisionDialog@Last Modifier") + "</span></th>" +
                "<th style='text-align:left;width:15%' id='perc-header-actions'><span class='col-name'>" +
                I18N.message("perc.ui.revisionDialog@Actions") + "</span></th>" +
                "</tr>" +
                "</thead>" +
                "<tbody></tbody>" +
                "</table>" +
                "</div>");
        }

        /**
         * Creates the revision rows html appends them to the tbody element of the table and converts the table into
         * a datatable by adding the appropriate sort columns and handles the paging link display.
         * @param revisionData, The revision data is expected to be in the format of
         * com.percussion.itemmanagement.data.PSRevisions
         */
        function addRevisionRows(revisionData)
        {
            var revSummary = revisionData.RevisionsSummary;
            var revisions = Array.isArray(revSummary.revisions)?revSummary.revisions:[revSummary.revisions];
            var itemCount = revisions.length;
            var isRestorable = revSummary.restorable;
            var restorableIcon = isRestorable?"restore.png":"restoreInactive.png";
            var restorableTitle = isRestorable?I18N.message("perc.ui.revisionDialog.tooltip@Restorable"):I18N.message("perc.ui.revisionDialog.tooltip@NotRestorable",[itemType]);
            var $tbody = $("#revisionsTable").find("tbody");
            var latestRevision;
            var allRevisions = new Map([]);

            for(var i in revisions)
            {
                var revdata = revisions[i];
                var lastModifiedDate = new Date(revdata.lastModifiedDate);
                var timeStamp = new Date(revdata.lastModifiedDate).getTime(); //This timestamp will be used to set the data-order attribute of datatable to sort it as date on basis of timestamp
                var lastModifiedDateParts = $.perc_utils.splitDateTime(revdata.lastModifiedDate);
                var lastModifiedDateDate = lastModifiedDateParts.date;
                var lastModifiedDateTime = lastModifiedDateParts.time;
                var revObj={};

                revObj.revId = revdata.revId;
                revObj.status = revdata.status;
                revObj.lastModified = lastModifiedDateDate + " " + lastModifiedDateTime;
                revObj.modifier = revdata.lastModifier;
                allRevisions.set(revObj.revId, revObj);

                var lastCol = "";
                if(itemCount === revdata.revId){
                    latestRevision =  revObj;
                    lastCol = "<span title='" + I18N.message("perc.ui.revisionDialog.tooltip@LatestRevision") + "'>"+ I18N.message("perc.ui.revisionDialog.label@Latest") +"</span>";
                }else
                {
                    lastCol = "<img title= '" + I18N.message("perc.ui.revisionDialog.tooltip@PreviewRevision") +
                        "' alt='" + I18N.message("perc.ui.revisionDialog.tooltip@PreviewRevision") +
                        "' revId='"+ revdata.revId + "' class='perc-revisions-preview-img' style='vertical-align:middle;margin-right:1px;' src='/cm/images/icons/editor/preview.png' />" +

                        "<img title= 'Compare this revision to latest revision' "  +
                        "'id='compare'  alt='Compare this revision to latest revision' " +
                        "' revId='"+ revdata.revId + "' class='perc-revisions-compare-img' style='vertical-align:middle;margin-right:1px;' src='/cm/images/icons/editor/revision-compare.png' />" +
                        "<img title= '" + I18N.message("perc.ui.revisionDialog.tooltip@Restorable") + "' alt='restorableTitle' revId='" + revdata.revId + "' class='perc-revisions-restore-img' style='vertical-align:middle;margin-right:1px;' src='/cm/images/icons/editor/" + restorableIcon + "'  />";
                }
                var $rowHTML = $(
                    "<tr>" +
                    "<td style='vertical-align: middle'><div class='data-cell perc-ellipsis'>" + revdata.revId + "</div></td>" +
                    "<td style='vertical-align: middle'><div for='" + revdata.status + "' class='data-cell perc-ellipsis'>" + revdata.status + "</div></td>" +
                    "<td data-order ='"+timeStamp+"' style='vertical-align: middle''><div class='data-cell perc-ellipsis'>" +  revObj.lastModified + "</div></td>" +
                    "<td style='vertical-align: middle'><div class='data-cell perc-ellipsis'>" + revdata.lastModifier + "</div></td>" +
                    "<td style='vertical-align: middle'><div class='data-cell perc-ellipsis'>" + lastCol + "</div></td>" +
                    "</tr>");
                $rowHTML.find("td:eq(2)").data("timedate", lastModifiedDate);
                $tbody.append($rowHTML);
            }
            var iType = itemType;

            $("#revisionsTable").find(".perc-revisions-compare-img").on("click",function(){
                var revId = $(this).attr("revId");
                var currRev = $(this).attr("currRevData");
                if(iType === $.PercRevisionDialog.ITEM_TYPE_PAGE)
                {
                    $.perc_finder().launchPageCompareView(itemId,itemName,Number(revId),Number(latestRevision.revId),allRevisions);
                }
            });
            $("#revisionsTable").find(".perc-revisions-preview-img").on("click",function(){
                var revId = $(this).attr("revId");
                if(iType === $.PercRevisionDialog.ITEM_TYPE_PAGE)
                {
                    $.perc_finder().launchPagePreview(itemId, revId);
                }
                else if(iType === $.PercRevisionDialog.ITEM_TYPE_ASSET)
                {
                    $.perc_finder().launchAssetPreview(itemId, revId);
                }
                else
                {
                    var eMsg = "Cannot preview unknown type.";
                    $.perc_utils.alert_dialog({title: 'Error', content: eMsg});
                }
            });


            $("#revisionsTable").find(".perc-revisions-preview-img").on("mouseenter", function(){
                $(this).attr("src", "/cm/images/icons/editor/previewOver.png");
            });
            $("#revisionsTable").find(".perc-revisions-preview-img").on("mouseleave", function(){
                $(this).attr("src", "/cm/images/icons/editor/preview.png");
            });

            //Hanlde restore click events and icon hover events if the item is restorable
            if(isRestorable)
            {
                $("#revisionsTable").find(".perc-revisions-restore-img").on("click",function(){
					var revId = $(this).attr("revId");
                    $.perc_utils.confirm_dialog({
                        title: 'Restore Revision',
                        question: 'Want to Restore Revision (' + revId + ') as current revision?',
                        success: function()
                        {
                        $.blockUI();
                        $.PercRevisionService.restoreRevision(itemId,revId,afterRestore);
                        },
                        cancel: function () {}
                    });
                });
                $("#revisionsTable").find(".perc-revisions-restore-img").on("mouseenter", function(){
                    $(this).attr("src", "/cm/images/icons/editor/restoreOver.png");
                });
                $("#revisionsTable").find(".perc-revisions-restore-img").on("mouseleave", function(){
                    $(this).attr("src", "/cm/images/icons/editor/restore.png");
                });
            }

            var table = $("#revisionsTable").dataTable({
                "order":[[ 0, "desc" ]],
                "bFilter" : false,
                "bAutoWidth" : false,
                // turn on pagination and use sequential and random access pagination controls
                "bPaginate" : true,
                "sPaginationType" : "full_numbers",
                "iDisplayLength" : 10,
                "bLengthChange" : false,
                "bInfo" : true,
                // if table has no rows show the following error
                "oLanguage": {"sZeroRecords": "No Revisions Found"},

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

            $('#revisionsTable tbody').on( 'click', 'tr', function () {
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
         * Call back function that is passed to the $.PercRevisionService.restoreRevision method.
         * If the status is error, then displays the error. Otherwise closes the dialog and opens the item in edit mode.
         * The parameters match with the caller $.PercServiceUtils.makeJsonRequest method.
         */
        function afterRestore(status,result)
        {
            if ( $.blockUI ) {
                $.unblockUI();
            }
            var self = this;

            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: defaultMsg});
                return;
            }
            dialog.remove();
            $.PercNavigationManager.setReopenAllowed(true);
            var item = {
                "id": $.PercNavigationManager.getId(),
                "name": $.PercNavigationManager.getName(),
                "path": $.PercNavigationManager.getPath()
            };
            if($.PercNavigationManager.getView() === $.PercNavigationManager.VIEW_EDITOR)
            {
                $.PercNavigationManager.handleOpenPage(item, true);
            }
            else if($.PercNavigationManager.getView() === $.PercNavigationManager.VIEW_EDIT_ASSET)
            {
                $.PercNavigationManager.handleOpenAsset(item, true);
            }
            else
            {
                // This should never happen.
                var eMsg = I18N.message("perc.ui.revision.dialog@Cannot Open Unknown View");
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: eMsg});
            }
        }
    }// End open dialog

})(jQuery);
