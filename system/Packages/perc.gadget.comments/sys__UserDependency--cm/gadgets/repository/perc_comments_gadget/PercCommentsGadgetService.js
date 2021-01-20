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
(function($){
    /**
     * Class to handle interaction between the Comments Moderation Gadget and the sitemange service.
     *
     * Depends upon:
     * /cm/jslib/jQuery.js
     * /cm/plugins/perc_path_constants.js
     * /cm/services/PercServiceUtils.js
     * 
     * Use this class by calling it as a function.  The returned object is the API which is available to external code.
     * For example,
     * <code>$.PercCommentsGadgetService().getAllCommentTotals(mySite, myFunction, myOptions);</code>
     * For a list of avilable API for this class, see the bottom of this file (gadgetServiceAPI).
     */
    var PercCommentsGadgetService = function()
    {
        /**
         * All constants used by the service.
         */
        var constants = {
            "URLS" : {
                "SET_MODERATIONS"    : $.perc_paths.COMMENTS_SET_MODERATIONS,
                "ALL_COMMENT_TOTALS" : $.perc_paths.COMMENTS_GET_ALL,
                "COMMENTS_ON_PAGE"   : $.perc_paths.COMMENTS_GET_PAGE,
                "ARTICLE_DESC"       : $.perc_paths.COMMENTS_GET_ARTICLE,
                "DEFAULT_MODERATION" : $.perc_paths.COMMENTS_DEFAULT_MODERATION,
                "VIEW_COMMENTS_JSP"  : "/cm/app/dialogs/Perc_CommentsGadget_ViewComments.jsp"
            }
        };
        /**
        * Constants which are available to external calls.
        */
        var exportedConstants = {
            "URLS" : {
                "VIEW_COMMENTS_JSP" : constants.URLS.VIEW_COMMENTS_JSP,
                "SET_DEFAULT_COMMENT_MODERATION_DIALOG_JSP"  : "/cm/app/dialogs/PercCommentsGadgetSetDefaultCommentModeration.jsp"
            }
        };
        
        /**
         * Requests a total number of comments for each page with comments in the system.
         * @param callback {function} A callback function to be called once data is retrieved, cannot be <code>null</code>.
         * @param callbackOptions {object} An object containing all the options which should be passed to the callback function, may be <code>null</code>.
         */
        var getAllCommentTotals = function(site, callback, callbackOptions)
        {
            // This service can return mock data at the javascript level.  If you need it to make a fake call, use this code.
            /*summaries = [
                { // Row
                    "pageLinkText"  : "44",
                    "pagePath"      : "/44",
                    "commentCount"  : 1,
                    "approvedCount" : 1
                },
                { // Row
                    "pageLinkText"  : "Home",
                    "pagePath"      : "/index",
                    "commentCount"  : 8,
                    "approvedCount" : 6
                },
                { // Row
                    "pageLinkText"  : "MySpecialPage",
                    "pagePath"      : "/SiteSection01/MySpecialPage",
                    "commentCount"  : 23,
                    "approvedCount" : 16
                },
                { // Row
                    "pageLinkText"  : "Random Page with Long Name with Spaces",
                    "pagePath"      : "/SiteSection01/RandomPagewithLongNamewithSpacing",
                    "commentCount"  : 5,
                    "approvedCount" : 5
                }
            ];
            
            callback(summaries, callbackOptions);*/
            
            $.PercServiceUtils.makeJsonRequest(constants.URLS.ALL_COMMENT_TOTALS + '/' + site,$.PercServiceUtils.TYPE_GET,false,function(status, result){
                if(status === $.PercServiceUtils.STATUS_SUCCESS){
                    callback(result.data.commentsSummary, callbackOptions, I18N.message("perc.ui.gadgets.comments@No comments found"));
                }
                else {
                    callback([], callbackOptions, I18N.message("perc.ui.gadgets.comments@Delivery service may be unavailable"));
                }
            });
        };
        
        var getCommentsOnPage = function(site, pagePath, callback, callbackOptions)
        {
            // Note, pagePath should start with a single /
            $.PercServiceUtils.makeJsonRequest(constants.URLS.COMMENTS_ON_PAGE + '/' + site + pagePath, $.PercServiceUtils.TYPE_GET, false, function(status, result){
                if(status === $.PercServiceUtils.STATUS_SUCCESS){
                    callback(result.data.comments, callbackOptions);
                }
                else {
                    callback([], callbackOptions);
                }
            });
        };
        
        var getArticleDescription = function(site, pagePath, callback, callbackOptions)
        {
            // Note, pagePath should start with a single /
            $.PercServiceUtils.makeJsonRequest(constants.URLS.ARTICLE_DESC + '/' + site + pagePath, $.PercServiceUtils.TYPE_GET, false, function(status, result){
                if(status === $.PercServiceUtils.STATUS_SUCCESS){
                    callback(result.data.Page, callbackOptions);
                }
                else {
                    callback( { "title" : "", "summary" : "" }, callbackOptions);
                }
            });
        };
        var setCommentModeration = function(site,moderations, callback)
        {
            $.PercServiceUtils.makeJsonRequest(constants.URLS.SET_MODERATIONS+ '/' + site, $.PercServiceUtils.TYPE_PUT, false,
                function(status, result){
                  callback();   
                },moderations);
        };
        /**
         *  @param moderation
         *  {defaultModerationState : {site : "Site Name", state : "REJECTED"}}
         */
        var setDefaultCommentModeration = function(moderation, callback)
        {
            $.PercServiceUtils.makeJsonRequest(constants.URLS.DEFAULT_MODERATION, $.PercServiceUtils.TYPE_PUT, false,
                function(status, result){
                    if(status === $.PercServiceUtils.STATUS_SUCCESS){
                        callback($.PercServiceUtils.STATUS_SUCCESS, "");
                    } else {
                        var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                        callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                    }
                },moderation);
        };
        var getDefaultCommentModeration = function(site, callback)
        {
            var url = constants.URLS.DEFAULT_MODERATION + "/" +site;
            
            $.PercServiceUtils.makeJsonRequest(url, $.PercServiceUtils.TYPE_GET, false,
                function(status, result){
                    if(status === $.PercServiceUtils.STATUS_SUCCESS){
                        callback($.PercServiceUtils.STATUS_SUCCESS, result.data.defaultModerationState);
                    } else {
                        var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                        callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                    }
                });
        };
        /**
         * The methods/properties to be exported as an API for usage.
         * 
         * Exports the following methods:
         * * getAllCommentTotals
         * * getCommentsOnPage
         *
         * Exports the following properties:
         * * constants
         * 
         */
        var gadgetServiceAPI = {
            "constants"                   : exportedConstants,
            "getAllCommentTotals"         : getAllCommentTotals,
            "getCommentsOnPage"           : getCommentsOnPage,
            "getArticleDescription"       : getArticleDescription,
            "setCommentModeration"        : setCommentModeration,
            "getDefaultCommentModeration" : getDefaultCommentModeration,
            "setDefaultCommentModeration" : setDefaultCommentModeration
        };
        return gadgetServiceAPI;
    };
    $.PercCommentsGadgetService = PercCommentsGadgetService;
})(jQuery);