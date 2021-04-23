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
* Comments Dialog
*/
(function($){
    //Public API for the comments dialog.
    $.PercCommentsDialog = {
            open: openDialog,
            ITEM_TYPE_ASSET:"asset",
            ITEM_TYPE_PAGE:"page",
            ITEM_MODE_EDIT:"edit",
            ITEM_MODE_VIEW:"view"
    };
    /**
     * Opens the comments dialog and shows the comments in a table for the supplied item.
     * @param itemId(String), assumed to be a valid guid of the item (Page or Asset)
     * @param itemName(String) name of the item to be shown part of the dialog.
     * @param itemType(String), The type of the item. Use ITEM_TYPE_XXX constants.
     */
    function openDialog(itemId, itemName, itemType) 
    {
        //Makes a service call and gets the comments, passes the createDialog as the callback
        $.PercRevisionService.getRevisionDetails(itemId, createDialog);
        var dialog;
        /**
        * Creates the dialog and sets the field values from the supplied result.data object. 
        * On dialog open calls the addRevisionRows to add the comments rows.
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
            var revSummary = result.data.RevisionsSummary;
            var comments = revSummary.comments?$.isArray(revSummary.comments)?revSummary.comments:[revSummary.comments]:[];
            if(comments.length === 0){
                $.perc_utils.alert_dialog({"title": I18N.message("perc.ui.comments.dialog@No Comments"),"content":I18N.message("perc.ui.comments.dialog@No Comments Yet")});
                return;
            }
            var dialogHTML = createCommentsHtml(comments);

            percDialogObject = {
                resizable : false,
                title: I18N.message("perc.ui.commentsDialog.title@Comments") + ": " + itemName,
                modal: true,
                closeOnEscape : false,
                percButtons:{},
                id: "perc-comments-dialog",
                width: 700,
                height:490
            };

            percDialogObject.percButtons["Close"] = {
                click: function(){
                    dialog.remove();
                },
                id: "perc-comments-dialog-close"
            };

            dialog = $(dialogHTML).perc_dialog(percDialogObject);

        }
        
        /**
         * Creates the comments table html, with wrapper div. Empty tbody is added.
         * @return comments dialog html.
         */
        function createCommentsHtml(comments)
        {
            var $dialogHtml = $("<div id='perc-comments-container'/>");
            var commentHTML = "";
            if(comments.length === 0){
                commentHTML += "<div class='perc-comments-row'>" + I18N.message("perc.ui.comments.dialog@No Comments Selected Page") + "</div>";
            }
            else{
                $.each(comments, function(){ 
                    var comment = this;
                    var commentDate = new Date(comment.commentDate);
                    var commentDateParts = $.perc_utils.splitDateTime(comment.commentDate);
                    var commentDateDate = commentDateParts.date;
                    var commentDateTime = commentDateParts.time;
                    commentHTML += "<div class='perc-comments-row'>" +
                                        "<div class='perc-comments-header'><span class='perc-comment-madeby'>" + comment.commenter + " -- </span>" + "<span class='perc-comment-date'>" + commentDateDate + " " + commentDateTime + "</span>" + "<span class='perc-comment-type'> (" + comment.commentType + ")</span></div>" +
                                        "<div>" + comment.comment + "</div><hr class='perc-comment-sep'/>" +
                                    "</div>";
                });
            }
            $dialogHtml.append(commentHTML);
            return $dialogHtml;
        }
        
    }// End open dialog      

})(jQuery);
