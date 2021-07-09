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
* Schedules Dialog
*/
(function($){
    $.PercViewCommentsDialog = {
            open: openDialog
    };
    /**
     * Opens the schedule dialog.
     * @param itemId(String), assumed to be a valid guid of the item (Page or Asset)
     */
    function openDialog(siteName, pagePath) 
    {
        createViewCommentsDialog(siteName, pagePath);
    }// End open dialog
    /**
     * Creats the content editor dialog. Sets the height and widths as per the criteria
     * Sets the src of the Iframe to the url from the criteria.
     */
    function createViewCommentsDialog(siteName, pagePath) {
        //Create a dialog html with iframe, set the src to the content edit criteria url
        var url = $.PercCommentsGadgetService().constants.URLS.VIEW_COMMENTS_JSP + "?" + "site=" + $.perc_utils.encodeURL(siteName) + "&" + "pagePath=" + $.perc_utils.encodeURL(pagePath);
        var dlgHtml = "<div id='perc-gadget-comments-viewComments-frame-container'>" + 
        "<iframe name='perc-gadget-comments-viewComments-frame' id='perc-gadget-comments-viewComments-frame' height='100%' FRAMEBORDER='0' width='100%' src='" +
        url + "'></iframe>" + "</div>";
        
        //Create dialog and set the preferred height and width from the criteria
        dialog = window.parent.jQuery(dlgHtml).perc_dialog({
            title:I18N.message("perc.ui.view.comments.dialog@Comments"), 
            resizable:false, modal:true,
            height: 685, 
            width:  800,
            open : function(){
                // show vertical scrollbars if needed
                addDocumentScrollbars();
            },
            percButtons:{    
                "Save":    {
                    click: function() {
                        var approveRejectedStateChanges = parseStateChanges(this);

                        if(approveRejectedStateChanges != null) {
                            saveApproveRejectedStateChanges(siteName,approveRejectedStateChanges, function(){
                                removeDocumentScrollbars();
                                dialog.remove();
                                window.location.reload();
                            });
                        } else {
                            removeDocumentScrollbars();
                            dialog.remove();
                        }
                    },
                    id: "perc-gadget-comments-viewComments-frame-save-button"
                },
                "Cancel": {
                    click: function() {
                        dialog.remove();
                        removeDocumentScrollbars();
                        window.location.reload();
                    },
                    id: "perc-gadget-comments-viewComments-frame-cancel-button"
                }            
            },        
            id:'perc-gadget-comments-viewComments'
        });
        
        function removeDocumentScrollbars() {
            window.parent.jQuery("body")
                .css("overflow","hidden")
                .css("position","fixed");
        }
        
        function addDocumentScrollbars() {
            window.parent.jQuery("body")
                .css("overflow-y","scroll")
                .css("position","static");
        }
    
        function saveApproveRejectedStateChanges(sitename,approveRejectedStateChanges, callback) {
            $.PercCommentsGadgetService()
                .setCommentModeration(sitename,approveRejectedStateChanges, callback);
        }
    
        /**
         * @author Jose Annunziato
         * @param dialog Comments Moderation dialog to be parsed
         *
         * Parses the Comments Moderation dialog using jQuery to extract comments
         * that have been approved, rejected or deleted.
         * Builds a JSON object with IDs of comments with following structure
         *   { moderation : {
         *       deletes : [
         *               { site : "SomeSite 1", comments : [1,4]},
         *               { site : "SomeSite 2", comments : [2,3,7]}
         *       ],
         *       approves : [
         *               { site : "SomeSite 3", comments : [1,4]},
         *               { site : "SomeSite 4", comments : [2,3,7]}
         *       ],
         *       rejects : [
         *               {site : "another site",comments : [5,10,17,32]},
         *               {site : "another site 2",comments : [6,12]}
         *   ]}}
         */
        function parseStateChanges(dialog) {
            var site;
            var dialog = $(dialog);
            var iframe = dialog.find("iframe");
            var contents = iframe.contents();
            var approved = contents.find(".perc-gadget-comments-approved");
            var rejected = contents.find(".perc-gadget-comments-rejected");
            var deleted = contents.find(".perc-gadget-comments-deleted");
            var approvedIds = [];
            $.each(approved, function(){
                var actionsDiv = $(this);
                var id = actionsDiv.attr("commentId");
                site = actionsDiv.attr("site");
                var originalState = actionsDiv.attr("originalState");
                var currentState  = actionsDiv.attr("currentState");
                if(originalState === currentState)
                    return true;
                approvedIds.push(id);
            });
            var rejectedIds = [];
            $.each(rejected, function(){
                var actionsDiv = $(this);
                var id = actionsDiv.attr("commentid");
                site = actionsDiv.attr("site");
                var originalState = actionsDiv.attr("originalState");
                var currentState  = actionsDiv.attr("currentState");
                if(originalState === currentState)
                    return true;
                rejectedIds.push(id);
            });
            var deletedIds = [];
            $.each(deleted, function(){
                var actionsDiv = $(this);
                var id = actionsDiv.attr("commentid");
                site = actionsDiv.attr("site");
                var originalState = actionsDiv.attr("originalState");
                var currentState  = actionsDiv.attr("currentState");
                if(originalState === currentState)
                    return true;
                deletedIds.push(id);
            });
            if(approvedIds.length === 0 && rejectedIds.length === 0 && deletedIds.length === 0)
                return null;
            var moderation = {};
            moderation.moderation = {};
            if(approvedIds.length > 0) {
                moderation.moderation.approves = [];
                moderation.moderation.approves[0] = {site : site, comments : approvedIds};
            }
            if(rejectedIds.length > 0) {
                moderation.moderation.rejects = [];
                moderation.moderation.rejects[0] = {site : site, comments : rejectedIds};
            }
            if(deletedIds.length > 0) {
                moderation.moderation.deletes = [];
                moderation.moderation.deletes[0] = {site : site, comments : deletedIds};
            }
            
            return moderation;
        }
    }
})(jQuery);
