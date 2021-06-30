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
 * Depends upon the following:
 * /cm/jslib/jQuery.js
 * /cm/jslib/jQuery.dataTables.js
 * /cm/widgets/PercDataTable/PercDataTable.js
 * /cm/plugins/perc_path_constants.js - Ancestral Dependancy of PercCommentsGadgetService.js
 * /cm/services/PercServiceUtils.js - Ancestral Dependancy of PercCommentsGadgetService.js
 * /cm/gadgets/repository/perc_comments_gadget/PercCommentsGadgetService.js
 * 
 */
(function($){
    var TABLE_STATUS_FOOTER_PADDING_TOP = 5;
    var GADGET_TOOLBAR_HEIGHT = 25;
    var itemsPerPage = 5;

    // grab necessary Perc APIs
    var PercCommentsGadgetService = $.PercCommentsGadgetService;
    var PercPageService           = $.PercPageService;
    var PercServiceUtils          = $.PercServiceUtils;
    
    var isLargeColumn = true;       // if gadget is on the right side (large column)
    var statusTable = undefined;
    var defaultModerationLink;
    
    var PAGE_NOT_FOUND_LABEL   = I18N.message("perc.ui.gadgets.comments@Page not found in CM1");
    var PAGE_NOT_FOUND_TOOLTIP = I18N.message("perc.ui.gadgets.comments@Associated page does not exist in CM1");
    
    // API for this library
    $.fn.PercCommentsGadget = function(site, rows) {
        // never show a scrollbar in the gadget
        $("body").css("overflow","hidden");
        
        // resize gadget to fit the rows
        itemsPerPage = rows;

        defaultModerationLink = $(this).find("#perc-set-default-moderation-link")
            .on("click",function(evt){
                displayDefaultModerationDialog(evt);
            })
            .attr("site", site);
        
        var tableDiv = $(this);
        tableDiv.append('<div id="perc-gadget-comments-table-header">Comments</div>');
        if(site == null)
            site="";
        loadGadget(site, tableDiv);
        
    };
    
    /**
     * Retrieves pages from a site that are in a particular status and then renders them as a table
     * @param site (string) Site to request comments for.
     */
    function loadGadget(site, tableDiv) {
        // get the data and then pass it to createDataTable to create the table
        var callbackOptions = {};
        $.PercCommentsGadgetService()
            .getAllCommentTotals(site,
                function(summaries, callbackOptions, message){
                    createDataTable(summaries, site, tableDiv, message);
                }
                , callbackOptions);
    }
    
    /**
     * Creates a table using jQuery.dataTable.js
     * A lot of this functionality should be pulled out and refactored into a central function, to provide guard rails for the developers.
     */
    function createDataTable(data, siteName, tableDiv, message){
        
        statusTable = $("<div id='perc-comments-table' />");
        tableDiv.append($('<div id="perc-comments-table-wrapper" />').append(statusTable));
        isLargeColumn = gadgets.window.getDashboardColumn() == 1; // if the gadget is in first column then we have to render it as large 
        
        var percData = [];
        var pageStats = data;
        // Sane default values in case of problems.  Extended to every row.
        var defaultValues = { "newCount" : 0, "commentCount" : 0, "pagePath" : "/", "path" : "", "datePosted" : new Date(), "summary" : "", "id" : null };
        
        defaultModerationLink.show();
        
        var menus = [];
        var CM1Menu = {
            "title"             : "",
            "menuItemsAlign" : "left",
            //stayInsideOf     : ".perc-datatable",
            "items"          : [
                {label : I18N.message("perc.ui.gadgets.comments@Open Page"),    callback : editPage},
                {label : I18N.message("perc.ui.gadgets.comments@Preview Page"), callback : previewPage},
                {label : I18N.message("perc.ui.gadgets.comments@View Comments"),  callback : viewComments}
            ]
        };
        var deliveryMenu = {
            "title"             : "",
            "menuItemsAlign" : "left",
            //stayInsideOf     : ".perc-datatable",
            "items"          : [
                {label : I18N.message("perc.ui.gadgets.comments@View Comments"),  callback : viewComments}
            ]
        };
        
        $.each(data, function(){
            var pageSummary = $.extend({}, defaultValues, this);
            
            var summary;
            
            var postedDateParts = $.perc_utils.splitDateTime(pageSummary.datePosted);

            postedDate  = postedDateParts.date;
            postedTime  = postedDateParts.time;
            
            if (pageSummary.datePosted == "")
            {
                postedDate = "";
                postedTime = "";
            }
            
            var linkTitle;
            var linkTitleTooltip;
            var path = (pageSummary.path ? pageSummary.path : pageSummary.pagePath);
            var previewCallback;
            var nullFunc = function(){};

            if(pageSummary.id) {
                linkTitle = htmlEntities(pageSummary.pageLinkTitle);
                linkTitleTooltip = linkTitle;
                summary = $(pageSummary.summary).appendTo('<div />').parent().text();
                menus.push(CM1Menu);
                previewCallback = previewPage;
            } else {
                linkTitle = PAGE_NOT_FOUND_LABEL;
                linkTitleTooltip = PAGE_NOT_FOUND_TOOLTIP;
                summary = linkTitleTooltip;
                path = "/Sites/" + siteName + path;
                menus.push(deliveryMenu);
                previewCallback = null;
            }
            
            var commentLine = "";
            if (pageSummary.commentCount > 0)
            {
                if (pageSummary.newCount > 0)
                {
                    commentLine = "<span class='perc-comments-commentsIcon'>&nbsp;</span> Comments (" + pageSummary.commentCount + ") <span class='perc-comments-newCount'>" + pageSummary.newCount + " New</span>";
                }
                else
                {
                    commentLine = "<span class='perc-comments-commentsIcon'>&nbsp;</span> Comments (" + pageSummary.commentCount + ")";
                }
            }
            else
            {
                // For now, should not happen.
                // Pages should only be returned from the service if they have comments.
                commentLine = "";
            }
            var row = {rowContent : [[{content: linkTitle, title : path, callback : previewCallback}, {content : truncateSummary(summary), title : summary}, {content: commentLine, title : "", callback : viewComments}], [postedDate, postedTime]], rowData : { "pageId" : pageSummary.id, "pagePath" : pageSummary.pagePath, "path" : path, "siteName" : siteName }};
            
            percData.push(row);
        });
        
        var dataTypeCols = [
                { sSortDataType: "dom-ptext", sType: "html", sClass : "perc-datatable-cell-string perc-comments-linktext" },
                { sSortDataType: "dom-pdate", sType: "html", sClass : "perc-datatable-cell-date perc-comments-publishDate" }
            ];
        
        var tableConfig = {
            iDisplayLength : itemsPerPage,
            aoColumns: dataTypeCols,
            percHeaderClasses : ["perc-datatable-cell-string perc-comments-linktext", "perc-datatable-cell-date perc-comments-publishDate"],
            percHeaders : [I18N.message("perc.ui.gadgets.comments@Title"), I18N.message("perc.ui.gadgets.comments@Posted")],
            percData : percData,
            oLanguage : { sZeroRecords: (message!= null)? message : I18N.message("perc.ui.gadgets.comments@No comments found")},
            percMenus : menus,
            percColumnWidths : ["auto", "105"],
            percStayBelow: "#perc-gadget-comments-table-header",
            percRowDblclickCallback : editPage,
            percExpandParentFrameVertically : true
        };
        
        statusTable.PercPageDataTable(tableConfig);
        miniMsg.dismissMessage(loadingMsg);
        
        $("#perc-gadget-comments-viewComments-table_wrapper")
            .css("min-height","488px")
            .css("background", "white");
    }
    
    function getPageIdFromTableRow(row) {
        var menu   = row.parents("tr").find(".perc-action-menu");
        var pageId = $(menu).attr("pageId");
        return pageId;
    }
   
    function editPage(e) {
        percJQuery.PercNavigationManager.openPage(e.data.path);
    }
    
    function previewPage(e) {
        percJQuery.perc_finder().launchPagePreview(e.data.pageId);
    }
    function viewComments(e) {
        jQuery.PercViewCommentsDialog.open(e.data.siteName, e.data.pagePath);
    }
    /**
     * Displays an error message inside of the gadget.
     * Copied over from another gadget, should be centralized/refactored out.
     */
    function displayErrorMessage(message) {
        tableDiv.append("<div class='perc-gadget-errormessage'>" + message + "</div>");
        miniMsg.dismissMessage(loadingMsg);
    }
    
    /**
     *
     */
    
    function displayDefaultModerationDialog(evt) {
        //CMS-8176 : as event is passed so $(this) returned event instead of link, thus site was undefined and also causing save error.
        var link = $("#perc-set-default-moderation-link");
        var site = link.attr("site");
        
        $.PercCommentsGadgetService().getDefaultCommentModeration(site,
            function(status, defaultCommentsModeration){
                if(status === $.PercServiceUtils.STATUS_ERROR) {
                    percJQuery.perc_utils.alert_dialog({title: I18N.message("perc.ui.gadgets.comments@Error"), content: defaultCommentsModeration});
                    return;
                }
                var url = $.PercCommentsGadgetService().constants.URLS.SET_DEFAULT_COMMENT_MODERATION_DIALOG_JSP + "?" + "site=" + escape(site) + "&" + "state=" + escape(defaultCommentsModeration);
                var dlgHtml = "<div id='perc-gadget-comments-default-moderation-dialog' style='padding:0px'>" + 
                    "<iframe name='perc-gadget-comments-default-moderation-dialog-frame' id='perc-gadget-comments-default-moderation-dialog-frame' height='100%' FRAMEBORDER='0' width='100%' src='" +
                    url + "'></iframe>" + "</div>";

                percDialogObject = {
                    title: I18N.message("perc.ui.gadgets.comments@MODERATION"),
                    resizable:false, modal:true,
                    height: 213,
                    width:  429,
                    percButtons:{},
                    id:'perc-gadget-comments-default-moderation-dialog-wrapper'
                };

                percDialogObject.percButtons["Save"] = {
                    click: function() {
                        var moderation = parseDefaultModerationFromDialog(dialog, site);
                        $.PercCommentsGadgetService().setDefaultCommentModeration(moderation, function(status, defaultMsg){
                        if(status === $.PercServiceUtils.STATUS_ERROR) {
                            percJQuery.perc_utils.alert_dialog({title: I18N.message("perc.ui.gadgets.comments@Error"), content: defaultMsg});
                        } else {
                            dialog.remove();
                        }
                        });
                    },
                    id: "perc-gadget-comments-viewComments-frame-save-button"
                };

                percDialogObject.percButtons["Cancel"] = {
                    click: function() {
                        dialog.remove();
                    },
                    id: "perc-gadget-comments-viewComments-frame-cancel-button"
                };
                
                var dialog = window.parent.jQuery(dlgHtml).perc_dialog(percDialogObject);
            });
    
        function parseDefaultModerationFromDialog(dialog, site) {
            var iframe = dialog.find("iframe");
            var contents = iframe.contents();

            var checked = contents.find("input:checked");
            var checkedId = checked.attr("id");
            var moderation = {defaultModerationState : {site : site, state : "APPROVED" }};
            if(checkedId == "perc-dialog-reject")
                moderation.defaultModerationState.state="REJECTED";
            return moderation;
        }
    }
    
    function truncateSummary(summary)
    {
/*        var TRUNCATELENGTH;
        if (isLargeColumn)
            TRUNCATELENGTH = 120;
        else
            TRUNCATELENGTH = 120;
        if (summary.length < TRUNCATELENGTH)
        {*/
            if (summary.trim().length === 0)
                return "&nbsp;";
            else
                return summary;
/*        }
        else
        {
            return summary.substring(0,TRUNCATELENGTH) + "...";
        }*/
    }
})(jQuery);
