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
 * IFC for getting site summary data from server.
 **/
(function ($) {
    //Public API
    $.PercSiteSummaryService = {
        getSiteSummaryData : getSiteSummaryDataFromServer
    };
    
    /**
     * get data, invoke callback.
     * @see $.PercServiceUtils.makeJsonRequest
     */
    function getSiteSummaryDataFromServer (siteName, callback) {
        var url = $.perc_paths.SITE_STATS_SUMMARY + "/" + siteName;
        var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback($.PercServiceUtils.STATUS_ERROR,results);
            }
            else
            {
               var sumData = results.data.SiteSummaryData;
               var stats = sumData.statistics;
               var statistics = {
                   "Pages": stats.pages,
                   "Templates": stats.templates,
                   "Files": stats.binary,
                   "Style Sheets": stats.css,
                   "Internal Links": stats.linksInternal
               };
               
               var result = {id:sumData.siteId, statistics: statistics, issues: sumData.issues, abridged_log_message: sumData.abridgedErrorMessage};
               callback($.PercServiceUtils.STATUS_SUCCESS,result);
            }
        };
        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,true,serviceCallback);
    }
    
})(jQuery);
    
