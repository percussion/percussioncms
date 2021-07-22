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

var COOKIE_CONSENT_NAME = "percCookieConsent";

$(function() {
    var doesCookieExist = checkForConsentCookie();

    $('#accept-btn-preview').off('click').on('click', dismissDialog);

    if (!doesCookieExist) {
        $('.perc-consent-window').css('opacity', 1);
        $('#accept-btn').off('click').on('click', createCookie);
        $('#deny-btn').off('click').on('click', denyCookie);
    }else {
        document.getElementById("perc-cookie-consent").style.visibility = "hidden";
    }
});

/**
 * checks to see if there is a consent cookie in browser.
 * if there is, do not display accept consent dialog.
 * @returns the cookie entry if present
 */
function checkForConsentCookie() {
    var result = document.cookie.match(new RegExp(COOKIE_CONSENT_NAME + '=([^;]+)'));
    result && (result = result[1]);
    return result === "true" || checkForConsentDenyCookie() ;
}

function checkForConsentDenyCookie(){
    var deny  = document.cookie.match("deny_cookie");
    if(deny != null){
        return true;
    }
    return false;
}

/**
 * Stores a cookie in client's browser if they accept
 * cookie policy so as not to display the consent
 * dialog on next visit.
 */
function createCookie() {
    // sets cookie to expire in 5 years
    var futureDate = getDate(5, 0, 0);

    document.cookie = COOKIE_CONSENT_NAME + "=true; expires=" + futureDate + ";path=/; SameSite=Lax;";

    dismissDialog();

    postEntry();
}

function denyCookie(){
    var futureDate = getDate(5, 0, 0);

    document.cookie = "deny_cookie" + "=true; expires=" + futureDate + ";path=/; SameSite=Lax";

    dismissDialog();
}

/**
 * Dismisses cookie consenet dialog in preview mode.
 */
function dismissDialog() {

    $('#perc-cookie-consent').css('opacity', 0);

    // wait 1 second (set in cookie-consent.css) to set opacity to 0
    // before setting style to none.
    setTimeout(function() {
        document.getElementById("perc-cookie-consent").style.visibility = "hidden";
    }, 1000);
}

/**
 *  Posts an entry with consnet information
 *  to the DTS meta data service.
 */
function postEntry() {

    var cookieConsentElem = $('#perc-cookie-consent');

    if (cookieConsentElem.attr("data-query") === "") return;

    var queryString = eval("(" + cookieConsentElem.attr("data-query") + ")");
    var siteName = queryString.siteName;

    if (!siteName) return;

    var deliveryUrl = queryString.deliveryurl || "";

    var opts = {
        siteName : siteName,
        consentDate : new Date(),
        optIn: true,
        // currently setting consented services to 'All' until V2 of the widget.
        services : ["All"]
    };

    $.PercCookieConsentService.postConsentEntry(opts, deliveryUrl, function(status) {
        console.log(status);
        if (status == $.PercServiceUtils.STATUS_ERROR) {
            // sets the cookie to expire in 5 days to try save again.
            var futureDate = getDate(0, 0, 5);
            document.cookie = COOKIE_CONSENT_NAME + "=true; expires=" + futureDate + ";path=/; SameSite=Lax;";
        }
    });
}

/**
 * Method to create and return a new date for the browser cookie.
 * Creates a new date and adds values to the current date
 * year, month, and day.
 * @param {*} yearVal - how many years to add to curent date
 * @param {*} monthVal - how many months to add to current date
 * @param {*} dayVal - how many days to add to current date
 * @returns A new date object
 */
function getDate(yearVal, monthVal, dayVal) {
    var date = new Date();
    var year = date.getFullYear();
    var month = date.getMonth();
    var day = date.getDate();
    return new Date(year + yearVal, month + monthVal, day + dayVal);
}
