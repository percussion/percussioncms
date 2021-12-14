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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
