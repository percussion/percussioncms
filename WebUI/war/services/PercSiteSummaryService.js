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
            if(status == $.PercServiceUtils.STATUS_ERROR)
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
        }  
        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,true,serviceCallback);
        
        /* Test Data****/
       function fake(max) {
           return Math.floor(Math.random() * max);
       }
       /*
       var statistics, issues, result;
       statistics = { 
           "Pages": fake(1000), 
           "Templates": fake(5), 
           "Files": fake(170), 
           "Style Sheets": fake(25), 
           "Internal Links": fake(2400), 
           //"links-external": fake(1019), 
           //"links-broken": fake(213), 
           // support new stats that might show up in the output
           //docx: fake(24), 
           //pptx: fake(217),
           //foobar1: fake(24), 
           //foobar2: fake(217),
           //foobar3: fake(24), 
           //foobar4: fake(217),
           //foobar5: fake(24), 
           //foobar6: fake(217)
       };
       function issue (txt) {
           var uri, refrence, action, label = txt.toLowerCase();
           uri = "//sites/www.foo.com/" + label +".htm";
           name = label + ".htm";
           action = "Link: //sites/www.food.com/missing" + label + ".htm with abacus" + label + ".htm";
           reference = "//sites/www.foo.com/path/to/file/" + label + ".htm";
           return {
               type: "missing-" + label, 
               resource: {name: name, uri: uri}, 
               "ref-uri": reference,
               suggestion: action
           };
       }
       function issues(count) {
           // NOTE: types must be code (not labels) they are used as css
           var issues = [], types = ["page", "css", "asset", "random-thingy-1", "random-thingy-2"];
           for (var i = 0; i < 100; i++) {
               var type = Math.floor(Math.random() * 5);
               issues.push(issue(types[type]));
           }
           return issues;
       }
       result = {id:siteId, statistics: statistics, issues: issues(100) };
       callback($.PercServiceUtils.STATUS_SUCCESS, result);
       */
    }
    
})(jQuery);
    
