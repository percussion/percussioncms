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
