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
    var secure = "";
    if (window.isSecureContext) {
        secure = " Secure;";
    }
    document.cookie = COOKIE_CONSENT_NAME + "=true; expires=" + futureDate + ";path=/; SameSite=Lax;" + secure;

    dismissDialog();

    postEntry();
}

function denyCookie() {
    var futureDate = getDate(5, 0, 0);

    var secure = "";
    if (window.isSecureContext) {
        secure = " Secure;";
    }
    document.cookie = "deny_cookie" + "=true; expires=" + futureDate + ";path=/; SameSite=Lax;" + secure;

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
            var secure = "";
            if (window.isSecureContext) {
                secure = " Secure;";
            }
            document.cookie = COOKIE_CONSENT_NAME + "=true; expires=" + futureDate + ";path=/; SameSite=Lax;" + secure;
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
