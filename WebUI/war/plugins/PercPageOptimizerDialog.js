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
 * Page Optimizer Dialog
 */
(function($){
    //Public API for the page optimizer dialog.
    $.PercPageOptimizerDialog = {
        open: openDialog
    };
    /**
     * Opens the Page Optimizer Dialog and shows the optimizer details.
     * @param pagePath(String), assumed to be a valid page path
     */
    function openDialog(pageId)
    {
        //Makes a service call and gets the page optimizer details, passes the createDialog as the callback
        $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
        $.PercPageOptimizerService.getPageOptimizerDetails(pageId, createDialog);
        var dialog;
        /**
         */
        function createDialog(status, pageOptDetails)
        {
            $.unblockUI();
            var self = this;

            if(!status)
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: defaultMsg});
                return;
            }
            var dialogHTML = createDialogHtml(pageOptDetails);
            dialog = $(dialogHTML).perc_dialog( {
                resizable : false,
                title: I18N.message("perc.ui.page.optimizer.dialog@Page Optimizer"),
                modal: true,
                closeOnEscape : true,
                percButtons:{
                    "Close":{
                        click: function(){
                            dialog.remove();
                        },
                        id: "perc-page-optimizer-dialog-close"
                    }
                },
                open: function(){
                    addPageOptimizerContent(pageOptDetails);
                },
                id: "perc-page-optimizer-dialog",
                width: 1024,
                height:660
            });

        }

        /**
         * Creates the comments table html, with wrapper div. Empty tbody is added.
         * @return comments dialog html.
         */
        function createDialogHtml(pageOptDetails)
        {
            var $dialogHtml = $("<div id='perc-po-top-container'/>");
            //Last published formatted date
            var lp = "";
            if(pageOptDetails.lastPublished){
                lp = $.perc_utils.splitDateTime(pageOptDetails.lastPublished);
                lp = lp.date + " " + lp.time;
            }
            //Last edited formatted date
            var le = "";
            if(pageOptDetails.lastEdited){
                let le = $.perc_utils.splitDateTime(pageOptDetails.lastEdited);
                le = le.date + " " + le.time;
            }
            var wfstatus = pageOptDetails.status + " (" + pageOptDetails.workflow + ")";

            //TODO: I18N with correct formatting below
            var thumbHtml = "<div class=\"perc-missing-thumb\"><i title=\"Thumbnail is not yet available for this page.\" class=\"perc-missing-thumb-i perc-font-icon icon-camera fa-5x\"></i></div>";
            if(pageOptDetails.thumbUrl){
                thumbHtml = "<img src=\"" + pageOptDetails.thumbUrl + "\"/>";
            }
            var pageOptimizerHtml =
                "<div id='perc-po-page-details-container'>" +
                "<div class='perc-po-page-col perc-po-page-col0'>" +
                "<div style='height:150px;line-height:150px;text-align:center;'>" + thumbHtml + "</div>" +
                "</div>" +
                "<div class='perc-po-page-col perc-po-page-col1'>" +
                "<div><label>File Name:</label><span title='"+ pageOptDetails.pageName + "'>" + pageOptDetails.pageName + "</span></div>" +
                "<div><label>Display Title:</label><span title='"+ pageOptDetails.pageTitle + "'>" + pageOptDetails.pageTitle + "</span></div>" +
                "<div><label>Status:</label><span title='"+ wfstatus + "'>" + wfstatus + "</span></div>" +
                "</div>" +
                "<div class='perc-po-page-col perc-po-page-col2'>" +
                "<div><label>Last Published:</label><span>" + lp + "</span></div>" +
                "<div><label>Last Edited:</label><span>" + le + "</span></div>" +
                "</div>" +
                "</div>" +
                "<div id='perc-po-details-container' class='top-border'>" +
                "<div class='loading center-text'><div style='position: absolute; top: 40%; margin: auto; width: 100%;'><img src='https://optimizer-ui.percussion.com/img/ajaxLoader.gif'></div></div>" +
                "</div>";
            $dialogHtml.append(pageOptimizerHtml);
            return $dialogHtml;
        }

        /**
         * adds the page optimizer content/iframe to the page
         *
         * @param {Object} pageOptDetails - detailed object containing all the information to be passed to the page optimizer
         */
        function addPageOptimizerContent(pageOptDetails){
            var optimizerServiceUrl = pageOptDetails.pageOptimizerUrl;

            $("#perc-po-details-container .loading").hide();
            $("#perc-po-details-container").removeClass("top-border");
            //add iframe to the page
            $('<iframe/>', {
                id: 'perc-page-optimizer',
                src: optimizerServiceUrl + '/start/optimizer',
                width: '100%',
                height: 355
            }).appendTo('#perc-po-details-container');

            $("#perc-page-optimizer").on('load',function(){
                var pageOptimizerData = {
                    CI: pageOptDetails.clientIdentity,
                    page: pageOptDetails.pageHtml
                };
                this.contentWindow.postMessage(pageOptimizerData, '*');
                $("#perc-po-details-container").addClass("perc-po-frame-loaded");
            });

        }

        /**
         * ping the service to ensure that it is available
         *
         * @param {string} thisUrl - url to be checked
         */
        function pingOptimizerService(thisUrl) {
            return $.ajax({
                method: 'GET',
                url: thisUrl
            });
        }

    }// End open dialog

})(jQuery);
