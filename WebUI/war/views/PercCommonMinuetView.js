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

/*
*   PercCommonMinuetView.js
*   Contains all public methods used for the Minuet UI,
*   including Handlebars helper methods
*/

var uiVersion = 'Minuet';

function processTemplate(dataObject, templateName, destination, callback) {

    templateScript = $("#" + templateName).html();
    var template = Handlebars.compile(templateScript);
    var compiledHtml = template(dataObject);
    $('#' + destination).html(compiledHtml);

    if (typeof callback == 'function') {
        callback();
    }

}

function createDialogObject() {
    dialogObject = {};
    dialogObject.leftButton = {};
    dialogObject.rightButton = {};
    return dialogObject;
}

function bindFooterAlertEvents() {
    $('#percDismissFooterAlert').on("click",function() {
        hideSection('#percFooterAlertTarget', 'fadeOutDown');
    });
}

function hideSection(target, animationType, emptyContents) {
    /*  This has to be done in a specific order and is shown again at the end
    *   so the template can be reprocessed for a future alert or target
    */

    $(target).animateCss(animationType, function() {
        $(target).hide();
        $(target).empty();
        $(target).show();
    });
}

var percAlertTimer;
function processAlert(response) {
    alertTimeout();
    processTemplate(response, 'templateResponseFooterAlert', 'percFooterAlertTarget');
    bindFooterAlertEvents();
    $('#percFooterAlertTarget').animateCss('fadeInUp');
}

function alertTimeout() {
    // Clear the timeout each time a new alert is fired so that the new alert
    // gets a fresh countdown
    clearTimeout(percAlertTimer);
    percAlertTimer = setTimeout(function() {
        hideSection('#percFooterAlertTarget', 'fadeOutDown');
    }, 5000);
}

function startProcessRunningAlert() {
    $('html').addClass('wait');
    processRunningAnimationDeferred = $.Deferred();
    processRunningTimeout = setTimeout(function(){
        processTemplate(siteListObject, 'templateProcessRunningFooterAlert', 'percFooterAlertTarget');
        $('#percFooterAlertTarget').animateCss('fadeInUp');
    }, 500);
}

function stopProcessRunningAlert() {
    // Clear the timeout so any alert that is queued gets destroyed if process
    // finishes faster than timeout
    clearTimeout(processRunningTimeout);
    $('#percFooterAlertTarget').animateCss('fadeOutDown', function() {
        $('html').removeClass('wait');
        $('#percFooterAlertTarget').hide();
        $('#percFooterAlertTarget').empty();
        $('#percFooterAlertTarget').show();
        processRunningAnimationDeferred.resolve(true);
    });
}

function getArrayProperty(array, keyNeedle, keyName) {
    /*
    **  This code is temporarily deactivated until we integrate webpack
    **  due to lack of YUI support for ES6
    **
    **  result = array.find(x => x[keyNeedle] == keyName);
    **  return result;
    */

    /*
    **  This is the replacement code for the section above
    **
    */

    $(array).each(function() {
        if(this[keyNeedle]) {
            if(this[keyNeedle] === keyName) {
                targetObject = this;
            }
        }
    });

    return targetObject;
}

function triggerEvent(eventId, eventType) {
    $('#' + eventId).trigger(eventType);
}

function requestResultsParser(source, status, result) {

    response = {};
    response.source = source;
    response.status = status;
    if(Array.isArray(result)) {
        resultJSON = JSON.parse(result[0]);
        response.result = resultJSON.SitePublishResponse;
        if(response.result.warningMessage) {
            response.result.warning = response.result.warningMessage;
        }
    }
    else {
        response.result = {};
        response.result.warning = result;
    }

    return response;

}

/* This function helps parse the error responses to get the
* description when it might be found within different properties
*/
function findVal(object, key) {
    var value;
    Object.keys(object).some(function(k) {
        if (k === key) {
            value = object[k];
            return true;
        }
        if (object[k] && typeof object[k] === 'object') {
            value = findVal(object[k], key);
            return value !== undefined;
        }
    });
    return value;
}

var updateQueryStringParam = function (key, value) {

    var baseUrl = [location.protocol, '//', location.host, location.pathname].join(''),
        urlQueryString = document.location.search,
        newParam = key + '=' + value,
        params = '?' + newParam;

    // If the "search" string exists, then build params from it
    if (urlQueryString) {

        updateRegex = new RegExp('([\?&])' + key + '[^&]*');
        removeRegex = new RegExp('([\?&])' + key + '=[^&;]+[&;]?');

        if( typeof value === 'undefined' || value === null || value === '' ) { // Remove param if value is empty

            params = urlQueryString.replace(removeRegex, "$1");
            params = params.replace( /[&;]$/, "" );

        } else if (urlQueryString.match(updateRegex) !== null) { // If param exists already, update it

            params = urlQueryString.replace(updateRegex, "$1" + newParam);

        } else { // Otherwise, add it to end of query string

            params = urlQueryString + '&' + newParam;

        }

    }
    window.history.replaceState({}, "", baseUrl + params);
};

$.fn.extend({
    animateCss: function(animationName, callback) {
        var animationEnd = (function(el) {
            var animations = {
                animation: 'animationend',
                OAnimation: 'oAnimationEnd',
                MozAnimation: 'mozAnimationEnd',
                WebkitAnimation: 'webkitAnimationEnd',
            };

            for (var t in animations) {
                if (el.style[t] !== undefined) {
                    return animations[t];
                }
            }
        })(document.createElement('div'));

        this.addClass('animated ' + animationName).one(animationEnd, function() {
            $(this).removeClass('animated ' + animationName);

            if (typeof callback === 'function') callback();
        });

        return this;
    },
});

// Handlebars Helpers Section

Handlebars.registerHelper('enableIncremental', function(canIncrementalPublish, isFullPublishRequired) {
    if (isFullPublishRequired === true || canIncrementalPublish === false) {
        return false;
    } else {
        return true;
    }
});

Handlebars.registerHelper('validatePropertyValue', function(propertyName, propertyValue) {
    if (propertyName === propertyValue) {
        return true;
    } else {
        return false;
    }
});

Handlebars.registerHelper('filterByValue', function(array, keyName, keyValue) {
    var i, result = '';
    for (i = 0; i < array.length; i++) {
        if (array[i][keyName] === keyValue) {
            // String true/false needs to be converted to Boolean
            if (array[i].value === 'false') {
                result = false;
            } else if (array[i].value === 'true') {
                result = true;
            } else {
                result = array[i].value;
            }
        }
    }
    return result;
});

Handlebars.registerHelper('checkCurrentPrivateKey', function(keyName) {
    //  First check to make sure serverInfo exists in case this is a new server setup
    if(selectedServerData.serverInfo) {
        if(getArrayProperty(selectedServerData.serverInfo.properties, 'key', 'privateKey')) {
            storedKey = getArrayProperty(selectedServerData.serverInfo.properties, 'key', 'privateKey').value;
            if(keyName === storedKey) {
                return true;
            }
        }
        return false;
    }

});

Handlebars.registerHelper('duration', function(time) {
    duration = moment.duration(time).humanize();
    return duration;
});

Handlebars.registerHelper('JSONstring', function(object) {
    jsonString = JSON.stringify(object);
    return htmlEntities(jsonString);
});

Handlebars.registerHelper('stringToDate', function(dateString) {
    formattedDate = moment(dateString).format('L LTS');
    return formattedDate;
});
