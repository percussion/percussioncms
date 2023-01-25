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
 * Service to handle the site impact of items
 */

(function($)
{
    //Public API
    $.PercSiteImpactService = 
    {
            getSiteImpactDetails : getSiteImpactDetails
    };
    
    /**
     * Makes a call to the server and calls the supplied callback with status and result. See $.PercServiceUtils.makeJsonRequest
     * for more details.
     */
    function getSiteImpactDetails(itemId, callback)
    {
        var url = $.perc_paths.ASSET_SITE_IMPACT + "/" + itemId;
        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,callback);
    }
})(jQuery);
    
