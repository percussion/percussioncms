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
    $.PercRecentListService = 
    {
        getRecentList  : getRecentList,
        setRecent   : setRecent,
        RECENT_TYPE_ITEM    : "item",
        RECENT_TYPE_TEMPLATE : "template",
        RECENT_TYPE_SITE_FOLDER : "site-folder", 
        RECENT_TYPE_ASSET_FOLDER : "asset-folder",
        RECENT_TYPES : [this.RECENT_TYPE_ITEM, this.RECENT_TYPE_TEMPLATE, this.RECENT_TYPE_SITE_FOLDER, this.RECENT_TYPE_ASSET_FOLDER]
    };
    
    /**
     * Returns a list of objects for the supplied type 
     * @param {Object} type type must be one of RECENT_TYPES
     * @param {Object} site required for template and site folder types.
     */
    function getRecentList(type, site)
    {
        var deferred  = $.Deferred();
        if(!$.inArray(type, $.PercRecentListService.RECENT_TYPES)){
            deferred.reject(I18N.message("perc.ui.recent.list.service@Invalid Type"));        
        }
        else{
            var url = $.perc_paths.RECENT_ROOT + type;
            if(type === $.PercRecentListService.RECENT_TYPE_TEMPLATE || 
                type === $.PercRecentListService.RECENT_TYPE_SITE_FOLDER)
                url += "/" + site;
            var serviceCallback = function(status, result){
                    if(status === $.PercServiceUtils.STATUS_SUCCESS){
                        deferred.resolve(result);
                    } else {
                        var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                        $.perc_utils.info(I18N.message("perc.ui.page.optimizer.service@Access CM1 Page Optimizer") + defaultMsg);
                        deferred.reject(defaultMsg);
                    }
            };
            $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,true,serviceCallback);
        }
        return deferred.promise();
    }
    /**
     * Method to set an object to recent list.
     * @param {Object} type must be one of RECENT_TYPES
     * @param {Object} data assumed to be matched with the type
     * @param {Object} site required for template and site folder types.
     */
    function setRecent(type, data, site){
        var deferred  = $.Deferred();
        if(!$.inArray(type, $.PercRecentListService.RECENT_TYPES)){
            deferred.reject(I18N.message("perc.ui.recent.list.service@Invalid Type"));        
        }
        else{
            var url = $.perc_paths.RECENT_ROOT + type;
            if(type === $.PercRecentListService.RECENT_TYPE_TEMPLATE || 
                type === $.PercRecentListService.RECENT_TYPE_SITE_FOLDER)
                url += "/" + site;
            var postdata = {"value":data};
            $.ajax({
                 url: url,
                 type: "POST",
                 data: postdata,
                 contentType: "application/x-www-form-urlencoded",
                 dataType: "json"
            }).done(function(){
                deferred.resolve();
            }).fail(function(jqXHR, textStatus){
                deferred.reject(I18N.message("perc.ui.recent.list.service@Failed To Add Item"));
            });
        }
        return deferred.promise();
    }
    
})(jQuery);
    
