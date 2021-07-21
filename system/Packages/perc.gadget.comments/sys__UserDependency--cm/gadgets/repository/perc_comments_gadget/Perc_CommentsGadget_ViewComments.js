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

(function($){
    /**
     * Creates a table using jQuery.dataTable.js
     * A lot of this functionality should be pulled out and refactored into a central function, to provide guard rails for the developers.
     */
    function createDataTable(data, site, pagePath){

        var itemsPerPage = 999999999; // No paging support yet.  Change to sensible number once we implement it.
        var isLargeColumn = true;
        window.gadgets = false;
        var tableDiv = $('#perc-gadget-comments-viewComments-container');
        statusTable = $("<table id='perc-gadget-comments-viewComments-table' cellspacing='0'>");
        tableDiv.append(statusTable);

        var percData = [];

        $.PercCommentsGadgetService().getArticleDescription(site, pagePath, function(article, callbackOptions){

            var articleDesc = $('<div />').
            append(
                $('<div />').
                attr('id', 'perc-gadget-comments-viewComments-articleSummary').
                append(
                    $('<span />').
                    text(
                        $('<div />').
                        append(article.title).
                        text()
                    )
                ).append(
                    $('<span />').
                    text(
                        $('<div />').
                        append(article.summary).
                        text()
                    )
                )
            ).append(
                $('<div />').
                attr('id', 'perc-gadget-comments-viewComments-numComments').
                text(data.length + ((data.length>1)?' comments:':' comment:'))
            );
            var allModeration = newRejectApproveAllActions();
            let newRow = statusTable.append('<tr>');
            newRow.append('<td>').append(articleDesc);
            newRow.append('<td>').append(allModeration);

            $.each(data, function(){
                var commentData = this;
                var commentTitle   = $('<h2 />').addClass('perc-gadget-comments-viewComments-commentTitle').text($('<div />').append(commentData.title).text());
                var commentDateRaw = $.timeago.parse(commentData.createdDate);
                var dateParts = $.perc_utils.splitDateTime(commentData.createdDate);

                var commentDate    = $('<span />').
                addClass('perc-gadget-comments-viewComments-commentDate').
                append(
                    $('<span />').
                    text(commentData.createdDate).
                    attr('title', commentData.createdDate).
                    timeago()
                ).append(
                    $('<span />').
                    text(

                        ', ' + dateParts.date + ", " + dateParts.time
                    )
                );
                var commentAuthor  = $('<span />').addClass('perc-gadget-comments-viewComments-commentAuthor').text($('<div />').append( ( commentData.username ? commentData.username : "Anonymous" ) ).text() + " said...");
                var commentText    = $('<div />').addClass('perc-gadget-comments-viewComments-commentText').text($('<div />').append(commentData.text).text());

                var commentAggregate = $('<div />').addClass('perc-datatable-firstrow').append(commentDate).append(commentAuthor).append(commentText);

                var commentId = commentData.id;
                var rejectApproveActions = newRejectApproveActions(commentData.approvalState, commentId, site);

                let newRow = statusTable.append('<tr>');
                newRow.append('<td>').append(commentAggregate);
                newRow.append('<td>').append(rejectApproveActions);


            });

            var dataTypeCols0 = [
                { bSortable: false, sClass : "perc-datatable-cell-string perc-gadget-comments-viewComments-comment" },
                { bSortable: false, sClass : "perc-datatable-cell-action perc-gadget-comments-viewComments-action" }
            ];
            var dataTypeCols1 = [
                { bSortable: false, sClass : "perc-datatable-cell-string perc-gadget-comments-viewComments-comment" },
                { bSortable: false, sClass : "perc-datatable-cell-action perc-gadget-comments-viewComments-action" }
            ];
            var dataTypeCols = isLargeColumn?dataTypeCols1:dataTypeCols0;

            var tableConfig = {
                autoWidth: true,
                ordering: true,
                fixedHeader: true,
                searching: false,
                paging:false,
                iDisplayLength : itemsPerPage,
                aoColumns: dataTypeCols,
                percHeaderClasses : ["perc-datatable-cell-string perc-gadget-comments-viewComments-comment", "perc-datatable-cell-string perc-gadget-comments-viewComments-action"],
                percHeaders : ["Title", "Action"],
                oLanguage: {"sZeroRecords": "&nbsp;", "sInfo" : "Showing _START_ to _END_ of _TOTAL_ total results", "sInfoEmpty" : "&nbsp;"},
                percColumnWidths : [["82%", "18%"],["82%", "18%"]]
            };



            // if the last row does not fill the bottom of the dialog
            // expand the height of the last row to the bottom of the dialog
            var container = $("#perc-gadget-comments-viewComments-container");
            var containerHeight = container.height();
            $('#perc-gadget-comments-viewComments-table').DataTable(tableConfig);

            $(window).trigger('perc-datatable-doneLoading');

            updateApproveRejectAllActions();
        }, {});

    }

    function newRejectApproveAllActions() {
        var approvalActionMenu          = $("<div class='perc-gadget-comments-actionMenu-all'>");
        var approvalActionRejectAction  = $("<div class='perc-gadget-comments-rejectAction-all'  title='Reject All Comments'>");
        var approvalActionApproveAction = $("<div class='perc-gadget-comments-approveAction-all' title='Approve All Comments'>");
        var approvalActionDeleteAction  = $("<div class='perc-gadget-comments-deleteAction-all' title='Delete All Comments'>");

        var approvalActionAllLabel      = $("<div class='perc-gadget-comments-label-all'>ALL</div>");
        approvalActionMenu
            .append(approvalActionRejectAction)
            .append(approvalActionApproveAction)
            .append(approvalActionDeleteAction)
            .append(approvalActionAllLabel)
            .addClass("perc-gadget-comments-default-all");

        approvalActionRejectAction.on("click",function(evt){
            rejectAll(evt);
        });
        approvalActionApproveAction.on("click",function(evt){
            approveAll(evt);
        });
        approvalActionDeleteAction.on("click", function(evt){
            deleteAll(evt);
        });

        return approvalActionMenu;
    }

    function deleteAll(evt) {
        var approvalActionMenu = $(evt.target).parent();
        approvalActionMenu
            .attr("currentState", "ALL_DELETED")
            .removeClass("perc-gadget-comments-approved-all")
            .removeClass("perc-gadget-comments-rejected-all")
            .addClass("perc-gadget-comments-deleted-all");

        var allIndividualMenus = $(".perc-gadget-comments-actionMenu");
        allIndividualMenus
            .attr("currentState", "DELETED")
            .removeClass("perc-gadget-comments-approved")
            .removeClass("perc-gadget-comments-rejected")
            .addClass("perc-gadget-comments-deleted");
    }

    function rejectAll(evt) {
        var approvalActionMenu = $(evt.target).parent();
        approvalActionMenu
            .attr("currentState", "ALL_REJECTED")
            .removeClass("perc-gadget-comments-approved-all")
            .removeClass("perc-gadget-comments-deleted-all")
            .addClass("perc-gadget-comments-rejected-all");

        var allIndividualMenus = $(".perc-gadget-comments-actionMenu");
        allIndividualMenus
            .attr("currentState", "REJECTED")
            .removeClass("perc-gadget-comments-approved")
            .removeClass("perc-gadget-comments-deleted")
            .addClass("perc-gadget-comments-rejected");
    }

    function approveAll(evt) {
        var approvalActionMenu = $(evt.target).parent();
        approvalActionMenu
            .attr("currentState", "ALL_APPROVED")
            .addClass("perc-gadget-comments-approved-all")
            .removeClass("perc-gadget-comments-deleted-all")
            .removeClass("perc-gadget-comments-rejected-all");

        var allIndividualMenus = $(".perc-gadget-comments-actionMenu");
        allIndividualMenus
            .attr("currentState", "APPROVED")
            .addClass("perc-gadget-comments-approved")
            .removeClass("perc-gadget-comments-deleted")
            .removeClass("perc-gadget-comments-rejected");
    }

    function newRejectApproveActions(approvalState, commentId, site) {

        var approvalStateClass          = approvalState == "APPROVED" ? "perc-gadget-comments-approved" : "perc-gadget-comments-rejected";
        var approvalStateClassOriginal  = approvalStateClass;

        var approvalActionMenu          = $("<div class='perc-gadget-comments-actionMenu' site='"+site+"' commentId='"+commentId+"'>");
        var approvalActionRejectAction  = $("<div class='perc-gadget-comments-rejectAction'  title='Reject Comment'>");
        var approvalActionApproveAction = $("<div class='perc-gadget-comments-approveAction' title='Approve Comment'>");
        var approvalActionDeleteAction  = $("<div class='perc-gadget-comments-deleteAction' title='Delete Comment'>");

        approvalActionMenu
            .append(approvalActionRejectAction)
            .append(approvalActionApproveAction)
            .append(approvalActionDeleteAction)
            .addClass(approvalStateClass)
            .attr("currentState",  approvalState)
            .attr("originalState", approvalState);

        approvalActionRejectAction.on("click",function (evt){
            reject(evt);
        });

        approvalActionApproveAction.on("click", function(evt){
            approve(evt);
        });

        approvalActionDeleteAction.on("click",function(evt){
            deleteComment(evt);
        });

        return approvalActionMenu;
    }

    function reject(evt) {
        var approvalActionMenu = $(evt.target).parent();
        approvalActionMenu
            .attr("currentState", "REJECTED")
            .removeClass("perc-gadget-comments-approved")
            .removeClass("perc-gadget-comments-deleted")
            .addClass("perc-gadget-comments-rejected");
        updateApproveRejectAllActions();
    }

    function approve(evt) {
        var approvalActionMenu = $(evt.target).parent();
        approvalActionMenu
            .attr("currentState", "APPROVED")
            .addClass("perc-gadget-comments-approved")
            .removeClass("perc-gadget-comments-deleted")
            .removeClass("perc-gadget-comments-rejected");
        updateApproveRejectAllActions();
    }

    function deleteComment(evt) {
        var approvalActionMenu = $(evt.target).parent();
        approvalActionMenu
            .attr("currentState", "DELETED")
            .addClass("perc-gadget-comments-deleted")
            .removeClass("perc-gadget-comments-approved")
            .removeClass("perc-gadget-comments-rejected");
        updateApproveRejectAllActions();
    }

    function updateApproveRejectAllActions() {
        var allIndividualMenus = $(".perc-gadget-comments-actionMenu");
        var individualMenuCount = allIndividualMenus.length;
        var allTheSame = true;
        var firstState = $(allIndividualMenus[0]).attr("currentState");
        $.each(allIndividualMenus, function(){
            var action = $(this);
            var state = action.attr("currentState");
            if(state !== firstState) {
                allTheSame = false;
                return false;
            }
        });
        var approveRejectAllAction = $(".perc-gadget-comments-actionMenu-all");
        if(allTheSame) {
            if(firstState == "APPROVED") {
                approveRejectAllAction
                    .attr("currentState", "ALL_APPROVED")
                    .addClass("perc-gadget-comments-approved-all")
                    .removeClass("perc-gadget-comments-deleted-all")
                    .removeClass("perc-gadget-comments-rejected-all");
            } if(firstState == "REJECTED") {
                approveRejectAllAction
                    .attr("currentState", "ALL_REJECTED")
                    .addClass("perc-gadget-comments-rejected-all")
                    .removeClass("perc-gadget-comments-deleted-all")
                    .removeClass("perc-gadget-comments-approved-all");
            } if(firstState == "DELETED") {
                approveRejectAllAction
                    .attr("currentState", "ALL_DELETED")
                    .addClass("perc-gadget-comments-deleted-all")
                    .removeClass("perc-gadget-comments-rejected-all")
                    .removeClass("perc-gadget-comments-approved-all");
            }
        } else {
            approveRejectAllAction
                .attr("currentState", "")
                .removeClass("perc-gadget-comments-approved-all")
                .removeClass("perc-gadget-comments-rejected-all")
                .removeClass("perc-gadget-comments-deleted-all")
                .addClass("perc-gadget-comments-default-all");
        }
    }

    $(function(){

        var site     = $(document).getUrlParam('site');
        var pagePath = $(document).getUrlParam('pagePath');
        window.parent.jQuery("#" + window.name).parent().css('padding', '0');
        $.PercCommentsGadgetService().getCommentsOnPage(site, pagePath, function(comments, callbackOptions){
            createDataTable(comments, site, pagePath);
        }, {});
    });
})(jQuery);
