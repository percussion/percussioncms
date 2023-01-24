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

(function($) {
    $.PercReusableSearchService =  {
        getSites: getSites,
        getWorkflows: getWorkflows,
        getStates: getStates,
        getUsers: getUsers
    };

    function getUsers(searchQuery, callback)
    {
        var url = $.perc_paths.USER_USERS_NAMES + "/" + searchQuery;

        $.PercServiceUtils.makeJsonRequest(
            url,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    var data = $.perc_utils.convertCXFArray(result.data.UserList.users);

                    callback($.PercServiceUtils.STATUS_SUCCESS, data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(status, defaultMsg);
                }
            }
        );
    }

    /**
     * Retrieves the license information status.
     * @param function
     */
    function getSites(callback)
    {
        var url = $.perc_paths.SITES_ALL_CHOICES;

        $.PercServiceUtils.makeJsonRequest(
            url,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                var data = $.perc_utils.convertCXFArray(result.data.EnumVals.entries);
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(status, defaultMsg);
                }
            }
        );
    }

    function getWorkflows(callback)
    {
        var url = $.perc_paths.WORKFLOW_STEPPED;

        $.PercServiceUtils.makeJsonRequest(
            url,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                var data = $.perc_utils.convertCXFArray(result.data.EnumVals.entries);
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(status, defaultMsg);
                }
            }
        );
    }

    function getStates(workflow, callback)
    {
        var url = $.perc_paths.WORKFLOW_STEPPED + workflow + "/states/choices";

        $.PercServiceUtils.makeJsonRequest(
            url,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                var data = $.perc_utils.convertCXFArray(result.data.EnumVals.entries);
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(status, defaultMsg);
                }
            }
        );
    }
})(jQuery);
