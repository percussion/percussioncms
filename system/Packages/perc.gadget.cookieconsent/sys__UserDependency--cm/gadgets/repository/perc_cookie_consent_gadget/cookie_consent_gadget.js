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

var PercServiceUtils = percJQuery.PercServiceUtils;
var PercCookieConsentService = percJQuery.PercCookieConsentService;
var PercUtils = percJQuery.perc_utils;
var PercPathConstants = percJQuery.perc_paths;

var LOG_ENDPOINT = PercPathConstants.COOKIE_CONSENT_LOG;
var SELECTED_SITES_CSV_URL = LOG_ENDPOINT;

$(document).ready(function () {
    var prefs    = new gadgets.Prefs();
    var rows     = parseInt(prefs.getString("zrows"));
    var site = prefs.getString("site");
    if(site==""){
        var sites = [];
        $(".perc-listing-type-site", window.parent.document).each(function() {
            sites.push($(this).find("div.perc-finder-item-name").html());
        });
        site = sites[0];
    }
    siteName=site;
    var mainHTML = '';
    var $percCookieTable = $('#perc-cookie-table');
    var $percCookieSiteTable = $('#perc-cookie-site-table');
    var moduleID = $('.perc-cookie-consent-gadget').data("moduleid");
    var iframe = $(percJQuery.find("#remote_iframe_" + moduleID));
    var isAdvancedDisplayed = false;
    var miniMsg = new gadgets.MiniMessage(moduleID);
    var loadingMsg = miniMsg.createStaticMessage(I18N.message( "perc.ui.gadgets.cookieConsent@Loading" ));


    /**
     * Method that sets up the gadget.
     */
    function init() {
        initButtons();
        getAllConsentEntries(site);
    };

    /**
     * Initializes the 'export cookie entries' button/URL.
     */
    function initButtons() {
        var $exportButton = $("#export-link");
        $exportButton.unbind().bind('click', function() {
            exportCookieCSV(SELECTED_SITES_CSV_URL+"/"+site+ "/cookie_consent_log.csv");
        });

        var $backButton = $('#perc-cookie-back-button');
        $backButton.unbind().bind('click', returnToMainScreen);

        var $advancedAllSitesButton = $('#advanced-all-sites-btn');
        $advancedAllSitesButton.unbind().bind('click', addAdvancedHeight);

        var $advancedSiteButton = $('#advanced-site-btn');
        $advancedSiteButton.unbind().bind('click', addAdvancedHeight);

        var $deleteButton = $('#cookie-consent-delete-btn');
        $deleteButton.unbind().bind('click', confirmDelete);
    }

    /**
     * Brings up the confirmation dialog to delete
     * the entries. This can be to delete all cookie
     * consent entries or for individual site.
     *
     * May be null to delete all entries.
     * @param {*} siteName - if not null, deletes specified sites.
     */
    function confirmDelete(siteName) {
        var warnOpenSpan = "<span id='perc-delete-warn-msg'>";
        var warnCloseSpan = "</span>";
        var cookieQuestion = typeof siteName === 'object' ? I18N.message( "perc.ui.deletecookiesdialog.warning@Confirm" ) : I18N.message( "perc.ui.deletesitecookiesdialog.warning@Confirm" );
        var tag = '';

        // if siteName was not passed in, Event object was passed by default
        if (typeof siteName === 'object') {
            tag = I18N.message( "perc.ui.deletecookiesdialog.tag@Cookies" );
        }
        else {
            tag = I18N.message( "perc.ui.deletesitecookiesdialog.tag@Cookies" ) +" " + siteName;
        }

        var options = {
            id       : 'perc-finder-delete-confirm',
            title : I18N.message( "perc.ui.deletecookiesdialog.title@Delete Cookies" ),
            question : warnOpenSpan + tag + "<br/><br/>"
                + cookieQuestion + "<br/><br/>" + I18N.message( "perc.ui.common.question@Continue" )
                + warnCloseSpan,
            success : function() {
                if (typeof siteName === 'object') {
                    deleteAllEntries();
                }
                else {
                    deleteAllEntriesForSite(siteName);
                }
            },
            yes: I18N.message("perc.ui.copy.page.button@Delete")
        };

        PercUtils.confirm_dialog(options);
    }

    /**
     * Updates the gadget with site(s) and total cookie
     * consent entries.
     * @param stats - the results of all cookie consent entries.  Map<String, Integer> format.
     * Contains key(sitename) and value(totals for site).
     */
    function updateGadgetTable(stats) {
        var table = $('#perc-cookie-table tbody');
        // iterate each site name / key append it to column 1.
        // append totals per site to column 2.
        Object.keys(stats).forEach(function(key) {
            var elem = $('<tr/>')
                .append($('<td/>')
                    .append($('<button/>')
                        .addClass('perc-cookie-button-site')
                        .text(key)
                    )
                ).append($('<td/>')
                    .text(stats[key])
                );

            $(table).append(elem);
        });

        setSiteButtonBindings(); // buttons must be set first
        initializeDataTable($percCookieTable);
    }

    /**
     * Updates the gadget with site(s) and total cookie
     * consent entries.
     * @param stats - the results of all cookie consent entries.  Map<String, Integer> format.
     * Contains key(sitename) and value(totals for site).
     */
    function updateSiteGadgetTable(stats) {
        var isDataTableCreated = $percCookieSiteTable.fnSettings !== undefined;
        var table = $('#perc-cookie-site-table tbody');
        table.empty();

        // iterate each cookie/service name / key append it to column 1.
        // append totals per site to column 2.
        Object.keys(stats).forEach(function(key) {
            var elem = $('<tr/>')
                .append($('<td/>')
                    .text(key)
                )
                .append($('<td/>')
                    .text(stats[key])
                );

            $(table).append(elem);
            if (isDataTableCreated) {
                $percCookieSiteTable.fnAddData([key, stats[key]], true);
            }
        });

        initializeDataTable($percCookieSiteTable);
    }

    /**
     * Initializes the jQuery dataTable for sorting, etc.
     * @param tableSelector - the table jQuery select elem
     * in which to update. Could be main table or
     * site-specific table.
     */
    function initializeDataTable(tableSelector) {
        // using this method to apply PercDataTable style classes
        // until PercDataTable is implemented.
        if (tableSelector === $percCookieTable) {
            $('.col-header').each(function(key, val) {
                $(val).append($('<span/>')
                    .addClass('perc-sort')
                    .attr('style', 'padding: 0 10 0 0;')
                    .html('&nbsp;')
                )
            });
        }

        if (tableSelector.fnSettings !== undefined) {
            adjustIframeHeight(tableSelector);
            return;
        }

        // TODO: change to PercDataTable wrapper once time allows.
        tableSelector.dataTable( {
            // sorting for columns
            "aoColumns": [
                { "asSorting": [ "desc", "asc" ] },
                { "asSorting": [ "desc", "asc" ] }
            ],
            "sPaginationType": "full_numbers",
            "oLanguage" : {sZeroRecords: I18N.message( "perc.ui.gadgets.cookieConsent@No Entries Found" ), oPaginate : {sFirst : "&lt;&lt;", sPrevious : "&lt;", sNext : "&gt;", sLast : "&gt;&gt;"}},
            "bFilter": false,
            "aLengthMenu": [ 5, 10, 15, 20 ],
            "iDisplayLength": 5,
            "bRetrieve": true,
            "bRedraw": true
        });

        var name = tableSelector.selector + "_length";
        // selector has # in it, removing this character
        name = name.substring(1);
        $("select[name='" + name + "']").unbind().bind('change', function() {
            adjustIframeHeight(tableSelector);
        });

        adjustIframeHeight(tableSelector);
    }

    /**
     * Sets an onclick binding for each
     * site entry that comes back.  Needed
     * to create a URL that will download
     * cookie consent entries for each site.
     */
    function setSiteButtonBindings() {
        $('.perc-cookie-button-site').each(function(key, val) {
            $(val).unbind().bind('click', function() {
                displaySiteStatistics($(val).text());
            });
        });
    }

    /**
     * When a site is clicked in the gadget window,
     * it brings up the consent entries for each
     * consent that was approved.
     * @param {*} siteName - the site to display properties for.
     */
    function displaySiteStatistics(siteName) {
        // first store the current HTML so we may switch back to it.
        mainHTML = $('.cookie-consent-content');
        mainHTML.hide();

        loadingMsg = miniMsg.createStaticMessage(I18N.message( "perc.ui.gadgets.cookieConsent@Loading" ));

        $('.cookie-consent-site-stats').show();
        $('.site-name').text(siteName);

        // put sitename in path param as well as csv file name
        var siteURL = LOG_ENDPOINT + "/" + siteName + "/" + siteName + "_consent_log.csv";

        // update export entries button
        $exportSiteButton = $('#export-site-link');
        $exportSiteButton.text(I18N.message( "perc.ui.gadgets.cookieConsent@Export consent entries for" ) + ' ' + siteName);
        $exportSiteButton.unbind().bind('click', function() {
            exportCookieCSV(siteURL);
        });

        //update delete entries button in advanced to match site
        isAdvancedDisplayed = false;
        $('#delete-btn-all').hide();
        $('#delete-btn-site').show();
        $('#advanced-site').addClass('collapse');
        $('#advanced-site').removeClass('in');
        $('#advanced-site p').text(I18N.message( "perc.ui.gadgets.cookieConsent@Delete entries" ) + ': ' + siteName);

        $deleteSiteButton = $('#cookie-consent-site-delete-btn');
        $deleteSiteButton.unbind().bind('click', function() {
            confirmDelete(siteName);
        });

        iframe.height(iframe.height() + 30);

        getCookieConsentTotalsPerSite(siteName);
    }

    /**
     * When returning to the main cookie consent
     * screen in the gadget, this function modifies
     * several CSS properties to act as a reset for
     * the screen.
     */
    function returnToMainScreen() {
        if ($percCookieSiteTable.fnClearTable !== undefined) {
            $percCookieSiteTable.fnClearTable();
        }
        $('.cookie-consent-site-stats').hide();
        $('#delete-btn-site').hide();
        $('#delete-btn-all').show();
        $('#advanced').addClass('collapse');
        $('#advanced').removeClass('in');
        isAdvancedDisplayed = false;
        miniMsg.dismissMessage(loadingMsg);
        adjustIframeHeight($percCookieTable);
        mainHTML.show();
    }

    /**
     * When changing the number of pages
     * to display for pagination, height of
     * iframe needs to be adjusted.
     * @param tableSelector - the table jQuery element in which to monitor.
     */
    function adjustIframeHeight(tableSelector) {
        var name = tableSelector.selector + "_length";
        name = name.substring(1);

        var value = $('select[name="' + name + '"] option:selected').val();
        value = parseInt(value, 10);

        var rowCount = tableSelector.fnGetData().length;
        var newHeight = 0;

        if (rowCount === 0) {
            newHeight += 50;
        }

        if ($percCookieSiteTable === tableSelector) {
            // add additional padding due to extra buttons in this window.
            newHeight += 80;
        }

        // using 165 for additional padding
        // 32 is the height of <tr> elements
        if (rowCount > value) {
            newHeight += 32 * value + 165;
        }
        else {
            newHeight += 32 * rowCount + 165;
        }

        iframe.height(newHeight);

        if (isAdvancedDisplayed) {
            iframe.height(iframe.height() + 60);
        }
    }

    /**
     * Adds more height to gadget when the advanced button
     * is clicked.
     */
    function addAdvancedHeight() {
        isAdvancedDisplayed = !isAdvancedDisplayed;
        if (isAdvancedDisplayed) {
            iframe.height(iframe.height() + 60);
        }
        else {
            iframe.height(iframe.height() - 60);
        }
    }

    //////////////////////////////REST CALLS////////////////////////////////

    /**
     * Gets the total number of cookie consent entries per site.
     * Populates the Dashboard gadget with statistics.
     */
    function getAllConsentEntries(site) {
        PercCookieConsentService.getAllCookieConsentEntries(site,function(status, result, message) {
            if(status == PercServiceUtils.STATUS_SUCCESS) {
                miniMsg.dismissMessage(loadingMsg);
                updateGadgetTable(result);
            } else {
                miniMsg.dismissMessage(loadingMsg);
                PercUtils.alert_dialog({title: I18N.message( "perc.ui.gadgets.cookieConsent@Error" ), content: message});
                $('#perc-cookie-table').text(message);
                console.error(I18N.message( "perc.ui.gadgets.cookieConsent@Error populating entries" ));
            }
        });
    }

    /**
     * Gets the total number of cookie consent entries
     * per site.
     * @param {*} siteName - the site in which to display
     * the entries for.
     */
    function getCookieConsentTotalsPerSite(siteName) {
        PercCookieConsentService.getCookieConsentEntriesPerSite(siteName, function(status, result, message) {
            if (status === PercServiceUtils.STATUS_SUCCESS) {
                updateSiteGadgetTable(result);
                miniMsg.dismissMessage(loadingMsg);
            }
            else {
                console.error(I18N.message( "perc.ui.gadgets.cookieConsent@Error retrieving entries" ) + ': ' + siteName);
                console.error(message);
                miniMsg.dismissMessage(loadingMsg);
                $('#perc-cookie-site-table').text(message);
            }
        });

    }

    /**
     * Exports the cookie consent entries in .CSV format.
     */
    function exportCookieCSV(url) {
        PercCookieConsentService.exportCookieCSV(url, function(status, result) {
            if (status == PercServiceUtils.STATUS_SUCCESS) {
                window.open(result.url);
            }
            else {
                var errMessage = I18N.message( "perc.ui.gadgets.cookieConsent@Error exporting entries" );
                PercUtils.alert_dialog({title: I18N.message( "perc.ui.gadgets.cookieConsent@Error" ), content: errMessage});
                console.error(result);
            }
        });
    }

    /**
     * Deletes all cookie consent entries in the database.
     */
    function deleteAllEntries() {
        PercCookieConsentService.deleteAllCookieConsentEntries(LOG_ENDPOINT+"/"+site, function() {
            window.location.reload();
            console.log(I18N.message( "perc.ui.gadgets.cookieConsent@Success deleting entries" ));
        }, function(results) {
            var errMessage = I18N.message( "perc.ui.gadgets.cookieConsent@Error deleting entries" );
            PercUtils.alert_dialog({title: I18N.message( "perc.ui.gadgets.cookieConsent@Error" ), content: errMessage});
            var defaultMessage = PercServiceUtils.extractDefaultErrorMessage(results);
            console.error(defaultMessage);
        });
    }

    /**
     * Deletes all cookie consent entries for the specified site.
     * @param {*} siteName - the site in which to delete the entries for.
     */
    function deleteAllEntriesForSite(siteName) {
        var finalUrl = LOG_ENDPOINT + "/" + siteName;
        PercCookieConsentService.deleteAllCookieConsentEntriesForSite(finalUrl, function() {
            window.location.reload();
            console.log(I18N.message( "perc.ui.gadgets.cookieConsent@Success deleting entries for site" ) + ': ' + siteName);
        }, function(results) {
            var defaultMessage = PercServiceUtils.extractDefaultErrorMessage(results);
            var errMessage = I18N.message( "perc.ui.gadgets.cookieConsent@Error deleting entries for site" ) + ': ' + siteName;
            PercUtils.alert_dialog({title: I18N.message( "perc.ui.gadgets.cookieConsent@Error" ), content: errMessage});
            console.error(defaultMessage);
        });
    }

    init();

});
