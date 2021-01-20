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
                if(status == $.PercServiceUtils.STATUS_SUCCESS) {
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
                if(status == $.PercServiceUtils.STATUS_SUCCESS) {
                    result.url = url;
                    callback($.PercServiceUtils.STATUS_SUCCESS, result);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
            ,""
            ,"text/csv"
            ,"text"
        );
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
                if(status == $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }
})(jQuery);