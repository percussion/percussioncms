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
 * Service to handle the page optimizer related actions.
 */

(function($)
{
    //Public API
    $.PercPageOptimizerService = 
    {
            isPageOptimizerActive : isPageOptimizerActive,
            getPageOptimizerDetails : getPageOptimizerDetails,
            getPageOptimizerInfo : getPageOptimizerInfo
    };
    
    /**
     */
    function isPageOptimizerActive(callback)
    {
        var url = $.perc_paths.PAGE_OPT_ACTIVE;
        var serviceCallback = function(status, result){
                if(status === $.PercServiceUtils.STATUS_SUCCESS){
                    var isActive = result.data && result.data === "true";
                    if(!isActive){
                        $.perc_utils.info(I18N.message("perc.ui.page.optimizer.service@Page Optimizer Not Enabled"));
                    }
                    callback(isActive);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    $.perc_utils.info(I18N.message("perc.ui.page.optimizer.service@Access CM1 Page Optimizer") + defaultMsg);
                    callback(false, defaultMsg);
                }
        };
        $.PercServiceUtils.makeRequest(url,$.PercServiceUtils.TYPE_GET,false,serviceCallback);
    }
    
    /**
     */
    function getPageOptimizerDetails(pageId, callback)
    {
        var url = $.perc_paths.PAGE_OPT_DATA + pageId;
        var serviceCallback = function(status, result){
                if(status === $.PercServiceUtils.STATUS_SUCCESS){
                    callback(true, result.data.PageOptimizerData);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(false, defaultMsg);
                }
        };

        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,serviceCallback);
    }

    /**
     * Gets the page optimizer info from server.
     */
    function getPageOptimizerInfo(callback)
    {
        var url = $.perc_paths.PAGE_OPT_INFO;
        var serviceCallback = function(status, result){
                if(status === $.PercServiceUtils.STATUS_SUCCESS){
                    callback(true, result.data.PageOptimizerInfo);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(false, defaultMsg);
                }
        };

        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,serviceCallback);
    }
    
})(jQuery);
    
