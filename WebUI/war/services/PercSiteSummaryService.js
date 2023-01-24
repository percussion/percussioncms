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
        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,serviceCallback);
    }
    
})(jQuery);
    
