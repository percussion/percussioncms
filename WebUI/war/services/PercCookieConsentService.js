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
 * PercCookieConsentService.js
 * Cookie Consent service, makes a call to the server to log and retrieve
 * cookie consent information.  Interfaces with DTS backend via perc-metadata-services
 * end points.
 */
(function($)
{
    $.PercCookieConsentService = {
        getAllCookieConsentEntries           : getAllCookieConsentEntries,
        exportCookieCSV                      : exportCookieCSV,
        getCookieConsentEntriesPerSite       : getCookieConsentEntriesPerSite,
        deleteAllCookieConsentEntries        : deleteAllCookieConsentEntries,
        deleteAllCookieConsentEntriesForSite : deleteAllCookieConsentEntriesForSite
    };

    /**
     * Gets all cookie consent entry information from DB.
     * Response from server should be a Map<String, Integer>.
     * @param {*} callback - returns data/response to calling function.
     */
    function getAllCookieConsentEntries(site,callback) {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.COOKIE_CONSENT_TOTALS+ "/" + site,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data, I18N.message("perc.ui.gadgets.cookieConsent@Success retrieving cookie consent entries"));
                }
                else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg, I18N.message("perc.ui.gadgets.cookieConsent@No cookie consent entries found"));
                }
            }
        );
    }

    /**
     * Gets all services that have been logged for the specified
     * site with totals for each service.
     * 
     * @param {*} siteName - the site in which to get entries for.
     * @param {*} callback - returns data to calling function.
     */
    function getCookieConsentEntriesPerSite(siteName, callback) {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.COOKIE_CONSENT_TOTALS + "/" + siteName,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data, I18N.message("perc.ui.gadgets.cookieConsent@Success retrieving cookie consent entries"));
                }
                else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg, I18N.message("perc.ui.gadgets.cookieConsent@No cookie consent entries found"));
                }
            }
        );
    }

    /**
     * Gets all cookie consent entries from DTS DB
     * and exports in .CSV format.
     * 
     * @param url - The URL for the Sitemanage endpoint which returns
     * a String in .CSV format.
     */
    function exportCookieCSV(url, callback) {
        $.PercServiceUtils.makeRequest(
            url,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    result.url = url;
                    callback($.PercServiceUtils.STATUS_SUCCESS, result);
                }
                else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },"","text/csv","text");
    }

    /**
     * Deletes all cookie consent entries
     * @param {*} url - the url for the sitemanage cookie consent delete service
     * @param {*} callback 
     * @param {*} errorCallBack 
     */
    function deleteAllCookieConsentEntries(url, callback, errorCallBack) {
        $.ajax({
            url     : url, 
            type    : $.PercServiceUtils.TYPE_DELETE,
            success : function() {
                callback();
            },
            error   : errorCallBack
        });
    }

    function deleteAllCookieConsentEntriesForSite(url, callback, errorCallBack) {
        $.ajax({
            url   : url,
            type  : $.PercServiceUtils.TYPE_DELETE,
            success: function() {
                callback();
            },
            error : errorCallBack
        });
    }

})(jQuery);
