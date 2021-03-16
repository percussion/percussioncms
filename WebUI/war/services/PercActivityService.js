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

(function($)
{
    $.PercActivityService = {
        getContentActivity: getContentActivity,
        getContentTraffic : getContentTraffic,
        getContentEffectiveness: getContentEffectiveness,
        getActivityForDateRange : getActivityForDateRange
    };
    
    function getContentActivity(path, durationType, duration, callback)
    {
        var obj = {ContentActivityRequest: {
           path: path,
           durationType: durationType,
           duration: duration
        }};
        $.PercServiceUtils.makeJsonRequest(
                $.perc_paths.ACTIVITY_CONTENT,
           $.PercServiceUtils.TYPE_POST,
           false,
           function(status, result)
           {
              if(status === $.PercServiceUtils.STATUS_SUCCESS)
              {
                 callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
              }
              else
              {
                 var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                 callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
              }
           },
           obj
        );
    }
    
    function getActivityForDateRange(path, startDateDrill, endDateDrill, usage, callback) {
        var request = {TrafficDetailsRequest:{path:path,startDate:startDateDrill,endDate:endDateDrill, usage:usage}};
        
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ACTIVITY_TRAFFIC_DETAILS,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            request
        );
    }
    
    function getContentTraffic(path, startDate, endDate, granularity, trafficRequested, usage, callback)
    {
        var obj = {ContentTrafficRequest: {
            path : path,
            startDate: startDate,
            endDate: endDate,
            granularity : granularity,
            usage : usage,
            trafficRequested: trafficRequested
        }};

        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ACTIVITY_TRAFFIC,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            obj
        );
    }
	
	function getContentEffectiveness(path, durationType, duration, usage, threshold, callback)
    {
        var obj = {EffectivenessRequest: {
           path: path,
           durationType: durationType,
           duration: duration,
		   usage: usage,
		   threshold: threshold
        }};
        $.PercServiceUtils.makeJsonRequest(
                $.perc_paths.ACTIVITY_EFFECTIVENESS,
           $.PercServiceUtils.TYPE_POST,
           false,
           function(status, result)
           {
              if(status === $.PercServiceUtils.STATUS_SUCCESS)
              {
                 callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
              }
              else
              {
                 var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                 callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
              }
           },
           obj
        );
    }
})(jQuery);