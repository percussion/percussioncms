
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
            for(var i in revisions)
            {
                var revdata = revisions[i];
                var lastModifiedDate = new Date(revdata.lastModifiedDate);
                var lastModifiedDateParts = $.perc_utils.splitDateTime(revdata.lastModifiedDate);
                var lastModifiedDateDate = lastModifiedDateParts.date;
                var lastModifiedDateTime = lastModifiedDateParts.time;

                var lastCol = "";
                if(itemCount === revdata.revId)
                    lastCol = "<span title='" + I18N.message("perc.ui.revisionDialog.tooltip@LatestRevision") + "'>"+ I18N.message("perc.ui.revisionDialog.label@Latest") +"</span>";
                else
                {
                    lastCol = "<img alt='" + I18N.message("perc.ui.revisionDialog.tooltip@PreviewRevision") +
                        "' revId='"+ revdata.revId + "' class='perc-revisions-preview-img' style='vertical-align:middle;margin-right:1px;' src='/cm/images/icons/editor/preview.png' />" +
                        "<img alt='"+ restorableTitle +"' revId='"+ revdata.revId + "' class='perc-revisions-restore-img' style='vertical-align:middle;' src='/cm/images/icons/editor/" + restorableIcon + "' />";
                }
                var $rowHTML = $(
                    "<tr>" +
                    "<td style='vertical-align: middle'><div class='data-cell perc-ellipsis'>" + revdata.revId + "</div></td>" +
                    "<td style='vertical-align: middle'><div for='" + revdata.status + "' class='data-cell perc-ellipsis'>" + revdata.status + "</div></td>" +
                    "<td style='vertical-align: middle''><div class='data-cell perc-ellipsis'>" + lastModifiedDateDate + " " + lastModifiedDateTime + "</div></td>" +
                    "<td style='vertical-align: middle'><div class='data-cell perc-ellipsis'>" + revdata.lastModifier + "</div></td>" +
                    "<td style='vertical-align: middle'><div class='data-cell perc-ellipsis'>" + lastCol + "</div></td>" +
                    "</tr>");
                $rowHTML.find("td:eq(2)").data("timedate", lastModifiedDate);
                $tbody.append($rowHTML);
            }
            var iType = itemType;
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
            }).on("mouseenter", function(){
                $(this).attr("src", "/cm/images/icons/editor/previewOver.png");
            })
                .on("mouseleave", function(){
                    $(this).attr("src", "/cm/images/icons/editor/preview.png");
                });

            //Hanlde restore click events and icon hover events if the item is restorable
            if(isRestorable)
            {
                $("#revisionsTable").find(".perc-revisions-restore-img").on("click",function(){
                    var revId = $(this).attr("revId");
                    $.PercRevisionService.restoreRevision(itemId,revId,afterRestore);
                })
                    .on("mouseenter", function(){
                        $(this).attr("src", "/cm/images/icons/editor/restoreOver.png");
                    })
                    .on("mouseleave", function(){
                        $(this).attr("src", "/cm/images/icons/editor/restore.png");
                    });
            }

            $("#revisionsTable").dataTable({
                "aaSorting": [[ 0, "desc" ]],
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
                // set custom column sorter data types
                "aoColumns": [
                    {"sType":"numeric", "sSortDataType":"perc-dom-text"},
                    null,
                    {"sType": "date" , "sSortDataType": "perc-dom-date"},
                    null,
                    {"bSortable":false}
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
         * Call back function that is passed to the $.PercRevisionService.restoreRevision method.
         * If the status is error, then displays the error. Otherwise closes the dialog and opens the item in edit mode.
         * The parameters match with the caller $.PercServiceUtils.makeJsonRequest method.
         */
        function afterRestore(status,result)
        {
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
