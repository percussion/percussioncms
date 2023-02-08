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
            var comments = revSummary.comments?Array.isArray(revSummary.comments)?revSummary.comments:[revSummary.comments]:[];
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
