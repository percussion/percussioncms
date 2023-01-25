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
            //async service call
            $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,serviceCallback);
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
            var postdata = {};
            postdata.value=data;
            $.ajax({
                 url: url,
                 type: "POST",
                 data: postdata,
            }).done(function(){
                deferred.resolve();
            }).fail(function(jqXHR, textStatus){
                deferred.reject(I18N.message("perc.ui.recent.list.service@Failed To Add Item"));
            });
        }
        return deferred.promise();
    }
    
})(jQuery);
    
