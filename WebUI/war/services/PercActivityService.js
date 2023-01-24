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
