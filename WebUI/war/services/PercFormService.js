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
 *  Form related services
 */
(function($) {
    $.PercFormService = {
        getAllForms         : getAllForms,
        getAllSubmissions   : getAllSubmissions,
        clearForm           : clearForm
    };

    /**
     *  Returns a list of all the published forms in CM1 and Delivery servers
     */
    function getAllForms(site,callback) {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ASSET_FORMS+ '/' + site,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data, I18N.message("perc.ui.form.service@No Forms Found"));
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg, I18N.message("perc.ui.form.service@Delivery Service Unavailable"));
                }
            }
        );
    }
    /**
     *  Gets all submissions for CM1 and deliver forms in a CSV format
     */
    function getAllSubmissions(siteName,formName, callback) {

        var url = $.perc_paths.ASSET_FORMS_EXPORT+ "/" + siteName + "/" + formName + ".csv";
        $.PercServiceUtils.makeRequest(
            url,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    result.url = url;
                    callback($.PercServiceUtils.STATUS_SUCCESS, result);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },"","text/csv","text");
    }
    /**
     *  Gets all submissions for CM1 and deliver forms in a CSV format
     */
    function clearForm(siteName,formName, callback) {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ASSET_FORMS_CLEAR + "/" + formName+ "/" + siteName,
            $.PercServiceUtils.TYPE_DELETE,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }
})(jQuery);
