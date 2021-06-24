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

//services:
var PercSIServiceUtils = percJQuery.PercServiceUtils;
var PercSISiteService = percJQuery.PercSiteService;
var PercSIMetadataService = percJQuery.PercMetadataService;
var PercUtils = percJQuery.perc_utils;

//our endpoints
var SITEIMPROVE_ROOT = '/Rhythmyx/services/integrations/siteimprove';
var PUBLISH_CONFIG = '/publish/config';
var DELETE_CONFIG = '/delete/config/';
var CREDENTIALS_URL = '/credentials';
var TOKEN_URL = '/token';
var NEW_TOKEN_URL = '/getnewtoken';
var IS_REFRESH_TOKEN = false;
var IS_EXTEND_HEIGHT = true;
var SITEIMPROVE_COOKIE_PATH = '/';
var SITEIMPROVE_COOKIE_NAME = 'siteimprove_credentials';

$(document).ready(function () {

    //Hold onto our site configuration.
    var siteInfos = {};

    // Back button functionality
    function onBack() {
        $('#save-config-status').removeClass();
        $('#perc-site-improve-main').removeAttr('style');
        $('#perc-site-improve-back-div').css('display', 'none');
        $('#perc-existing-site-improve-credentials').css('display', 'none');
        //$('#perc-try-out-siteimprove').css('display', 'none');
        $('#inactive-save-credentials-site-improve-img').css('display', 'none');
        $('#save-config-status-failure-message').text('');
    }

    //Existing Siteimprove screen functionality
    function onExistingSite() {
        $('#perc-site-improve-back-div').removeAttr('style');
        $('#perc-existing-site-improve-credentials').removeAttr('style');
        $('#perc-site-improve-main').css('display', 'none');
    }

    function validateSiteConfig(parsedMetadata) {
        return parsedMetadata.hasOwnProperty('doPreview') && parsedMetadata.hasOwnProperty('doProduction') && parsedMetadata.hasOwnProperty('doStaging');
    }

    function retrieveSiteConfig() {

        // find pre-existing configurations
        getAllSISiteConfig(function (status, results) {

            if (status === PercSIServiceUtils.STATUS_ERROR) {
                siteInfos = {};
            } else {

                if (results.metadata.length > 0) {
                    onExistingSite();
                }

                var selectedSite = $('#mySites').val();

                for (var metadatakey in results.metadata) {

                    //get our site info and sitename from the meta data
                    var parsedMetadata = JSON.parse(results.metadata[metadatakey].data);
                    var regexRemove = /perc.siteimprove.site./;
                    var sitename = results.metadata[metadatakey].key.replace(regexRemove, '');

                    //check to see if values exist on parse
                    if (validateSiteConfig(parsedMetadata) && sitename in siteInfos) {
                        siteInfos[sitename].siteSettings.doPreview = parsedMetadata.doPreview;
                        siteInfos[sitename].siteSettings.doProduction = parsedMetadata.doProduction;
                        siteInfos[sitename].siteSettings.doStaging = parsedMetadata.doStaging;
                        siteInfos[sitename].siteSettings.isSiteImproveEnabled = parsedMetadata.isSiteImproveEnabled;

                        //If our current selected item in the drop down box matches our results, then set the values accordingly.
                        if (selectedSite === sitename) {
                            $("#perc-si-preview").prop('checked', parsedMetadata.doPreview);
                            $("#perc-si-staging").prop('checked', parsedMetadata.doStaging);
                            $("#perc-si-production").prop('checked', parsedMetadata.doProduction);
                            $("#enableSiteimprove").prop('checked', parsedMetadata.isSiteImproveEnabled);
                        }
                    }
                }
            }
        });
    }

    function saveSiteimproveConfig() {

        var siteName = $("#mySites").val();

        var isPreviewEnabled = $("#perc-si-preview").is(':checked');
        var isStagingEnabled = $("#perc-si-staging").is(':checked');
        var isProductionEnabled = $("#perc-si-production").is(':checked');
        var isSiteImproveEnabled = $("#enableSiteimprove").is(':checked');
        var metadataName = "perc.siteimprove.site." + siteName;

        var siteSettings = {
            SiteimproveConfiguration: {
                siteName: siteName,
                doPreview: isPreviewEnabled,
                doStaging: isStagingEnabled,
                doProduction: isProductionEnabled,
                isSiteImproveEnabled: isSiteImproveEnabled
            }
        }

        saveSISiteConfig(metadataName, siteSettings, function (status, results) {
            if (status === PercSIServiceUtils.STATUS_ERROR) {
                $("#save-config-status").removeClass();
                $("#siteimprove-status-wrapper").stop();
                $("#siteimprove-status-wrapper").css({ 'opacity': 1 });
                $("#siteimprove-status-wrapper").attr("role", "alert");
                $("#save-config-status").addClass("fa fa-times fa-3x site-improve-config-status-failure");
                var failMessage = I18N.message("perc.ui.gadgets.siteimprove@Failure Save Site Config") + " " + siteName;
                $('#save-config-status-failure-message').removeAttr('style');
                $('#save-config-status-failure-message').text(failMessage);
                $('#inactive-save-settings-site-improve-img').css('display', 'none');
                $('#perc-site-improve-back-btn').prop('disabled', false);
                $('#perc-site-improve-save-btn').prop('disabled', false);
                setTimeout(function () {
                    $('#siteimprove-status-wrapper').removeAttr('role');
                    $('#siteimprove-status-wrapper').css({ 'opacity': 1 }).animate({ 'opacity': 0 }, 5000);
                }, 10000);
                $("#save-config-status").attr("title", failMessage);

            } else {
                setSuccessIndicators();
                retrieveSiteConfig();
                // TODO:i18n
                alertDialog(I18N.message("perc.ui.gadgets.siteimprove@Action Required"), I18N.message("perc.ui.gadgets.siteimprove@Confirm Registration"));
            }
        });

    }

    function setSuccessIndicators() {
        var siteName = $("#mySites").val();
        $("#save-config-status").removeClass();
        $("#siteimprove-status-wrapper").stop();
        $("#siteimprove-status-wrapper").css({ 'opacity': 1 });
        $("#siteimprove-status-wrapper").attr("role", "alert");
        $("#save-config-status").addClass("fa fa-check-square fa-3x site-improve-config-status-success");
        $("#inactive-save-settings-site-improve-img").css("display", "none");
        $("#perc-site-improve-back-btn").prop('disabled', false);
        $('#perc-site-improve-save-btn').prop('disabled', false);
        $('#save-config-status-failure-message').text('');
        setTimeout(function () {
            $('#siteimprove-status-wrapper').removeAttr('role');
            $('#siteimprove-status-wrapper').css({ 'opacity': 1 }).animate({ 'opacity': 0 }, 5000);
        }, 10000);
        $('#save-config-status').attr('title', I18N.message("perc.ui.gadgets.siteimprove@Success Save Site Config") + " " + siteName);
    }

    //Validated that the parsed credentials are valid
    function validateReturnedCredentials(parsedResults) {
        return parsedResults.hasOwnProperty("sitename") && parsedResults.hasOwnProperty("token")
            && parsedResults.hasOwnProperty("canonicalDist") && parsedResults.hasOwnProperty("defaultDocument")
            && parsedResults.hasOwnProperty("siteProtocol") && parsedResults.siteProtocol && parsedResults.defaultDocument
            && parsedResults.sitename in siteInfos && parsedResults.token && parsedResults.canonicalDist;

    }

    function adjustIframeHeight(module) {
        var moduleID = module.currentTarget.attributes["data-moduleid"].value;
        var iframe = $(percJQuery.find("#remote_iframe_" + moduleID));
        if(IS_EXTEND_HEIGHT) {
            iframe.height(iframe.height() + 60);
            IS_EXTEND_HEIGHT = false;
        }
        else {
            iframe.height(iframe.height() - 60);
            IS_EXTEND_HEIGHT = true;
        }
    }

    //Determine if credentials already exist or not, if they do
    function retrieveCredentials() {

        getAllSISiteCredentials(function (status, results) {
            if (status === PercSIServiceUtils.STATUS_ERROR) {
                console.error(I18N.message("perc.ui.gadgets.siteimprove@Error Getting Credentials"));
            } else {
                var siteName = $("#mySites").val();

                for (var result in results.metadata) {

                    var parsedResults = JSON.parse(results.metadata[result].data);

                    // check to see if it is a properly created credentials object and is not a deleted site.
                    //additional check for empty strings and null
                    if (validateReturnedCredentials(parsedResults)) {
                        siteInfos[parsedResults.sitename].siteCredentials.token = parsedResults.token;
                    }
                }
            }
            retrieveSiteConfig();
        });
    }

    // Get all sites for account
    function getSites() {
        PercSISiteService.getSites(function (status, results) {
            if (status === PercSIServiceUtils.STATUS_ERROR) {
                sites = [];
            }
            else {
                for (var result in results.SiteSummary) {

                    // add a site info object.
                    var siteCredentials = {
                        token: "",
                        canonicalDist: results.SiteSummary[result].canonicalDist,
                        defaultDocument: results.SiteSummary[result].defaultDocument,
                        siteProtocol: results.SiteSummary[result].siteProtocol
                    }

                    var siteSettings = {
                        doProduction: true,
                        doStaging: "",
                        doPreview: "",
                        isSiteImproveEnabled: false
                    }

                    var siteInfo = {
                        siteCredentials: siteCredentials,
                        siteSettings: siteSettings
                    }

                    siteInfos[results.SiteSummary[result].name] = siteInfo;
                    $("#mySites").append("<option id='perc-si-" + results.SiteSummary[result].name + "'>" + results.SiteSummary[result].name + "</option>");
                }
                //Retrieve pre-existing credentials
                retrieveCredentials();
            }
        });
    }

    function storeCredentials(siteName, token) {

        // store cookie with the token so when we delete
        document.cookie = SITEIMPROVE_COOKIE_NAME + "=" + token + ";path=" + SITEIMPROVE_COOKIE_PATH + ";";

        // get current site
        if(siteName === undefined || siteName == '')
            siteName = $("#mySites").val();

        var credentials = {
            SiteimproveCredentials: {
                siteName: siteName,
                token: token,
                siteProtocol: siteInfos[siteName].siteCredentials.siteProtocol,
                defaultDocument: siteInfos[siteName].siteCredentials.defaultDocument,
                canonicalDist: siteInfos[siteName].siteCredentials.canonicalDist
            }
        }

        //Validate and save credentials on the backend.
        saveSICredentials("perc.siteimprove.credentials." + siteName, credentials, function (status, results) {
            if (status === PercSIServiceUtils.STATUS_ERROR) {
                $("#save-config-status").removeClass();
                $("#siteimprove-status-wrapper").stop();
                $("#siteimprove-status-wrapper").css({ 'opacity': 1 });
                $("#siteimprove-status-wrapper").attr("role", "alert");
                $("#save-config-status").addClass("fa fa-times fa-3x site-improve-config-status-failure");
                var failMessage = I18N.message("perc.ui.gadgets.siteimprove@Failed To Save");
                $("#save-config-status-failure-message").removeAttr('style');
                $("#save-config-status-failure-message").text(failMessage);
                $("#inactive-save-settings-site-improve-img").css("display", "none");
                $("#perc-site-improve-back-btn").prop('disabled', false);
                $("#perc-site-improve-save-btn").prop('disabled', false);
                $("#perc-site-improve-reset-btn").prop('disabled', false);
                setTimeout(function () {
                    $("#siteimprove-status-wrapper").removeAttr('role');
                    $("#siteimprove-status-wrapper").css({ 'opacity': 1 }).animate({ 'opacity': 0 }, 4000);
                }, 10000);
                $("#save-config-status").attr("title", failMessage);
            } else {
                siteInfos[siteName].siteCredentials.token = token;
                saveSiteimproveConfig();
            }
        });
    }

    function deleteSiteImproveConfig() {

        // get current site
        var siteName = $("#mySites").val();

        //Delete credentials on the backend.
        deleteSISiteConfig(siteName, function (status, results) {
            if (status === PercSIServiceUtils.STATUS_ERROR) {
                $("#save-config-status").removeClass();
                $("#siteimprove-status-wrapper").stop();
                $("#siteimprove-status-wrapper").css({ 'opacity': 1 });
                $("#siteimprove-status-wrapper").attr("role", "alert");
                $("#save-config-status").addClass("fa fa-times fa-3x site-improve-config-status-failure");
                var failMessage = I18N.message("perc.ui.gadgets.siteimprove@Failed To Save");
                $("#save-config-status-failure-message").removeAttr('style');
                $("#save-config-status-failure-message").text(failMessage);
                $("#inactive-save-settings-site-improve-img").css("display", "none");
                $("#perc-site-improve-back-btn").prop('disabled', false);
                $("#perc-site-improve-save-btn").prop('disabled', false);
                $("#perc-site-improve-reset-btn").prop('disabled', false);
                setTimeout(function () {
                    $("#siteimprove-status-wrapper").removeAttr('role');
                    $("#siteimprove-status-wrapper").css({ 'opacity': 1 }).animate({ 'opacity': 0 }, 4000);
                }, 10000);
                $("#save-config-status").attr("title", failMessage);
            } else {
                if(siteInfos && typeof siteInfos !== 'undefined' && siteInfos.length > 0) {
                    siteInfos[siteName].siteSettings.doStaging = false;
                    siteInfos[siteName].siteSettings.doProduction = true;
                    siteInfos[siteName].siteSettings.doPreview = false;
                    siteInfos[siteName].siteSettings.isSiteImproveEnabled = false;
                }
                restoreDefaults();
                // TODO:i18n
                alertDialog(I18N.message("perc.ui.gadgets.siteimprove@Siteimprove Disabled"), I18N.message("perc.ui.gadgets.siteimprove@Successfully Removed Siteimprove Configuration") + " " + $("#mySites").val() + ". " + I18N.message("perc.ui.gadgets.siteimprove@Please Remember To Unregister"));
            }
        });
    }

    function save() {

        var isSiteImproveEnabled = $("#enableSiteimprove").is(':checked');
        var token = '';
        var existing_token = '';
        var siteName = $("#mySites").val();

        var siteimprove_cookie = getSiteImproveCookie();
        if(siteimprove_cookie != null) {
            existing_token = siteimprove_cookie;
        }

        if(!isSiteImproveEnabled && !IS_REFRESH_TOKEN) {
            deleteSiteImproveConfig();
            return;
        }

        if(existing_token === '' || IS_REFRESH_TOKEN) {
            getSIToken(siteName, function (status, results) {
                if (status === PercSIServiceUtils.STATUS_ERROR) {
                    console.log(I18N.message("perc.ui.gadgets.siteimprove@Error Getting Token") + ": " + siteName);
                } else {
                    if(results && results !== null && results.metadata !== null) {
                        var parsedMetadata = JSON.parse(results.metadata.data);
                        token = parsedMetadata.token;
                    }
                    if((token === null || token === "") || IS_REFRESH_TOKEN) {
                        getNewToken();
                    }
                    else {
                        storeCredentials(siteName, token);
                    }
                }
            });
        }
        else {
            storeCredentials(siteName, existing_token);
        }

        //waiting state
        $("#perc-site-improve-back-btn").prop('disabled', true);
        $("#perc-site-improve-save-btn").prop('disabled', true);
        $("#inactive-save-settings-site-improve-img").removeAttr("style");

    }

    function getNewToken() {
        var siteName = $("#mySites").val();
        getNewSiteImproveToken(function (status, results) {
            if (status === PercSIServiceUtils.STATUS_ERROR) {
                console.log(I18N.message("perc.ui.gadgets.siteimprove@Error Getting New Token"));
            } else {
                token = results.token;
                if(IS_REFRESH_TOKEN) {
                    updateTokenForAccount(token);
                    return;
                }
                else {
                    storeCredentials(siteName, token);
                }
            }
        });
    }

    /**
     * this method is used to clear the tokens from all sites
     * with siteimprove enabled.  It then generates a new token
     * and disperses it across all sites that currently have
     * siteimprove enabled.
     */
    function refreshToken() {
        IS_REFRESH_TOKEN = true;
        save(); // save in this context will call getNewToken()
        setSuccessIndicators();
        alertDialog(I18N.message("perc.ui.gadgets.siteimprove@Token Refreshed"), I18N.message("perc.ui.gadgets.siteimprove@Token Successfully Refreshed"));
    }

    /**
     * checks to see if there is a siteimprove cookie in browser.
     * this is done to alleviate so many round trips to server and back
     * to check siteimprove if tokens exist
     * @returns a siteInfos cookie object if siteimprove cookie is present
     */
    function getSiteImproveCookie() {
        var result = document.cookie.match(new RegExp(SITEIMPROVE_COOKIE_NAME + '=([^;]+)'));
        result && (result = result[1]);
        return result;
    }

    function alertDialog(title, message, w) {
        var parentNode;

        if(w == null || w == undefined || w == "" || w < 1)
            w = 400;
        PercUtils.alert_dialog({
            title: title,
            content: message,
            width: w,
            okCallBack: function()
            {}
        });
    }

    /**
     *
     * @param {*} token the token that should be updated for all
     * sites that have siteimprove enabled
     */
    function updateTokenForAccount(token) {
        document.cookie = SITEIMPROVE_COOKIE_NAME + "=" + token + ";path=" + SITEIMPROVE_COOKIE_PATH + ";";
        getAllSISiteCredentials(function (status, results) {
            if (status === PercSIServiceUtils.STATUS_ERROR) {
                console.error(I18N.message("perc.ui.gadgets.siteimprove@Error Getting Credentials"));
            } else {
                for (var result in results.metadata) {
                    var site = JSON.parse(results.metadata[result].data);
                    if(site.token !== '' && site.token !== undefined) {
                        updateSiteCredentialsFromRefresh(site.sitename, site);
                    }
                }
            }
        });
    }

    function updateSiteCredentialsFromRefresh(siteName, siteProperties) {

        var credentials = {
            SiteimproveCredentials: {
                siteName: siteName,
                token: token,
                siteProtocol: siteProperties.siteProtocol,
                defaultDocument: siteProperties.defaultDocument,
                canonicalDist: siteProperties.canonicalDist
            }
        }

        saveSICredentials("perc.siteimprove.credentials." + siteName, credentials, function (status, results) {
            if (status === PercSIServiceUtils.STATUS_ERROR) {
                $("#save-config-status").removeClass();
                $("#siteimprove-status-wrapper").stop();
                $("#siteimprove-status-wrapper").css({ 'opacity': 1 });
                $("#siteimprove-status-wrapper").attr("role", "alert");
                $("#save-config-status").addClass("fa fa-times fa-3x site-improve-config-status-failure");
                var failMessage = I18N.message("perc.ui.gadgets.siteimprove@Failed To Save");
                $("#save-config-status-failure-message").removeAttr('style');
                $("#save-config-status-failure-message").text(failMessage);
                $("#inactive-save-settings-site-improve-img").css("display", "none");
                $("#perc-site-improve-back-btn").prop('disabled', false);
                $("#perc-site-improve-save-btn").prop('disabled', false);
                $("#perc-site-improve-reset-btn").prop('disabled', false);
                setTimeout(function () {
                    $("#siteimprove-status-wrapper").removeAttr('role');
                    $("#siteimprove-status-wrapper").css({ 'opacity': 1 }).animate({ 'opacity': 0 }, 4000);
                }, 10000);
                $("#save-config-status").attr("title", failMessage);
            } else {
                siteInfos[siteName].siteCredentials.token = token;
                IS_REFRESH_TOKEN = false;
            }
        });
    }

    function siteDropdown() {
        var $mySiteDropdown = $("#mySites");

        // need to do it first
        $mySiteDropdown.on("change",function () {

            $("#save-config-status").removeClass();
            $("#save-config-status-failure-message").text("");

            var key = $mySiteDropdown.val();

            if (key in siteInfos) {
                // change the checkbox values and autofill the input fields.
                $("#perc-si-preview").prop('checked', siteInfos[key].siteSettings.doPreview);
                $("#perc-si-staging").prop('checked', siteInfos[key].siteSettings.doStaging);
                $("#perc-si-production").prop('checked', siteInfos[key].siteSettings.doProduction);
                $("#enableSiteimprove").prop('checked', siteInfos[key].siteSettings.isSiteImproveEnabled);
            } else {
                // Use default checkbox values and keep input field blank.
                restoreDefaults();
            }

        });
    }

    function restoreDefaults() {
        $("#perc-si-preview").prop('checked', false);
        $("#perc-si-staging").prop('checked', false);
        $("#perc-si-production").prop('checked', true);
        $("#enableSiteimprove").prop('checked', false);
    }

    //Try Siteimprove screen functionality
    function onTrySite() {
        //if try site is enabled again, uncomment.
        /*$('#perc-site-improve-main').css('display', 'none');
        $("#perc-try-out-siteimprove").removeAttr("style");
        $("#perc-site-improve-back-div").removeAttr("style");*/
        window.top.open("https://www.percussion.com/partners/siteimprove/");
    }

    // initialize our buttons and bind click events to them.
    function initButtons() {

        var $backBtn = $("#perc-site-improve-back-btn");
        var $existingSiteBtn = $("#perc-existing-site-improve-btn");
        var $TrySiteBtn = $("#perc-try-site-improve-btn");
        var $saveBtn = $("#perc-site-improve-save-btn");
        var $resetBtn = $("#perc-site-improve-reset-btn");
        var $advancedBtn = $("#perc-site-improve-advanced-btn");

        $backBtn.unbind().bind('click', onBack);
        $existingSiteBtn.unbind().bind('click', onExistingSite);
        $TrySiteBtn.unbind().bind('click', onTrySite);
        $saveBtn.unbind().bind('click', save);
        $resetBtn.unbind().bind('click', refreshToken);
        $advancedBtn.unbind().bind('click', adjustIframeHeight);
    }

    //Initialize the gadget
    function init() {
        initButtons();
        getSites();
        siteDropdown();
    }
    // Perform initialization functions
    init();

});

//////////////////////////// Siteimprove credentials ajax calls ////////////////////////////
function saveSICredentials(metadataName, data, callback) {
    PercSIServiceUtils.makeJsonRequest(SITEIMPROVE_ROOT + TOKEN_URL,
        PercSIServiceUtils.TYPE_PUT,
        false,
        function (status, result) {
            if (status === PercSIServiceUtils.STATUS_SUCCESS) {
                callback(PercSIServiceUtils.STATUS_SUCCESS, result.data);
            }
            else {
                var defaultMsg =
                    PercSIServiceUtils.extractDefaultErrorMessage(result.request);
                callback(PercSIServiceUtils.STATUS_ERROR, defaultMsg);
            }
        }, data, null, 120000);
}

// not currently being used
function getSIToken(siteName, callback) {
    PercSIServiceUtils.makeJsonRequest(SITEIMPROVE_ROOT + TOKEN_URL + "/" + siteName,
        PercSIServiceUtils.TYPE_GET,
        false,
        function (status, result) {
            if (status === PercSIServiceUtils.STATUS_SUCCESS) {
                callback(PercSIServiceUtils.STATUS_SUCCESS, result.data);
            }
            else {
                var defaultMsg =
                    PercSIServiceUtils.extractDefaultErrorMessage(result.request);
                callback(PercSIServiceUtils.STATUS_ERROR, defaultMsg);
            }
        });
}

function getNewSiteImproveToken(callback) {
    PercSIServiceUtils.makeJsonRequest(SITEIMPROVE_ROOT + NEW_TOKEN_URL,
        PercSIServiceUtils.TYPE_GET,
        false,
        function (status, result) {
            if (status === PercSIServiceUtils.STATUS_SUCCESS) {
                callback(PercSIServiceUtils.STATUS_SUCCESS, result.data);
            }
            else {
                var defaultMsg =
                    PercSIServiceUtils.extractDefaultErrorMessage(result.request);
                callback(PercSIServiceUtils.STATUS_ERROR, defaultMsg);
            }
        });
}

function getAllSISiteCredentials(callback) {
    PercSIServiceUtils.makeJsonRequest(SITEIMPROVE_ROOT + TOKEN_URL,
        PercSIServiceUtils.TYPE_GET,
        false,
        function (status, result) {
            if (status === PercSIServiceUtils.STATUS_SUCCESS) {
                callback(PercSIServiceUtils.STATUS_SUCCESS, result.data);
            }
            else {
                var defaultMsg =
                    PercSIServiceUtils.extractDefaultErrorMessage(result.request);
                callback(PercSIServiceUtils.STATUS_ERROR, defaultMsg);
            }
        });
}

//////////////////////////// Site configuration ajax calls ////////////////////////////
function saveSISiteConfig(metadataName, data, callback) {
    PercSIServiceUtils.makeJsonRequest(SITEIMPROVE_ROOT + PUBLISH_CONFIG,
        PercSIServiceUtils.TYPE_PUT,
        false,
        function (status, result) {
            if (status === PercSIServiceUtils.STATUS_SUCCESS) {
                callback(PercSIServiceUtils.STATUS_SUCCESS, result.data);
            }
            else {
                var defaultMsg =
                    PercSIServiceUtils.extractDefaultErrorMessage(result.request);
                callback(PercSIServiceUtils.STATUS_ERROR, defaultMsg);
            }
        }, data);
}

function getAllSISiteConfig(callback) {
    PercSIServiceUtils.makeJsonRequest(SITEIMPROVE_ROOT + PUBLISH_CONFIG,
        PercSIServiceUtils.TYPE_GET,
        false,
        function (status, result) {
            if (status === PercSIServiceUtils.STATUS_SUCCESS) {
                callback(PercSIServiceUtils.STATUS_SUCCESS, result.data);
            }
            else {
                var defaultMsg =
                    PercSIServiceUtils.extractDefaultErrorMessage(result.request);
                callback(PercSIServiceUtils.STATUS_ERROR, defaultMsg);
            }
        });
}

function deleteSISiteConfig(siteName, callback) {
    PercSIServiceUtils.makeDeleteRequest(SITEIMPROVE_ROOT + DELETE_CONFIG + siteName,
        false,
        function (status, result) {
            if (status == PercSIServiceUtils.STATUS_SUCCESS) {
                callback(PercSIServiceUtils.STATUS_SUCCESS, result.data);
            }
            else {
                var defaultMsg =
                    PercSIServiceUtils.extractDefaultErrorMessage(result.request);
                callback(PercSIServiceUtils.STATUS_ERROR, defaultMsg);
            }
        });
}
