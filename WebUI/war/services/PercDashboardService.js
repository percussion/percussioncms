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

(function($){

    /**
     * TODO:
     * This is a placeholder for the dashboard service.
     * Ajax methods in PercDashboard.js will be moved here.
     */

    $.PercDashboardService =  {
    	init : init,
    	getTrayGadgets : getTrayGadgets
    };

    function init() {
    	
    }
    
    /**
     * Retrieves list of gadgets from the server as a JSON object with the following structure:
     * {"Gadget":[  {"description":"description 1","iconUrl":"/url/to/image1.png","name":"Gadget 1","url":"/url/to/gadget1.xml"},
     *              {"description":"description 2","iconUrl":"/url/to/image2.png","name":"Gadget 2","url":"/url/to/gadget2.xml"}]}
     * @param type (string) Type value to filter Gadgets (All, Percussion, Community, Custom) case insensitive.
     * @param callback (function) to pass list of gadgets to
     */
    function getTrayGadgets(type, callback) {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.GADGETLIST + "?type=" + type,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status == $.PercServiceUtils.STATUS_SUCCESS) {
                    if(typeof(callback) == "function")
                        callback($.PercServiceUtils.STATUS_SUCCESS, filterGadgets(result.data));
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    if(typeof(callback) == "function")
                        callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }    
    
	/**
	 * Filters a list of gadgets if the saas variable is set to true
	 * 
	 * @param {Object} data (json) To filter for saas gadgets
	 */
    function filterGadgets(data) {
        // Just return no longer filtering
        return data;
    }

})(jQuery);
