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

//
// $.perc_sessionTimeout.js
//
// After a set amount of time, a dialog is shown to the user with the option
// to either log out now, or stay connected. If log out now is selected,
// the page is redirected to a logout URL. If stay connected is selected,
// a keep-alive URL is requested through AJAX. If no options is selected
// after another set amount of time, the page is automatically redirected
// to a timeout URL.
//
//
// USAGE
//
//   1. Include jQuery
//   2. Include jQuery UI (for dialog)
//   3. Include jquery.sessionTimeout.js
//   4. Call $.perc_sessionTimeout(); after document ready
//
//
// OPTIONS
//
//   serverCheck
//  	Number of seconds between ping to server when dialog is open
//
//   sessionStateUrl
//     	url to get ajax session state information
//	 	Default: '/Rhythmyx/sessionstatus.jsp'
//
//
//   logoutUrl
//     URL to take browser to if user clicks "Log Out Now"
//     Default: '/Rhythmyx/logout'
//

(function ($) {
    $.perc_sessionTimeout = function (options) {

        var defaults = {
            serverCheck: 10, // Number of seconds between server checks
            sessionStateUrl: '/Rhythmyx/sessioncheck',
            logoutUrl: '/Rhythmyx/logout',
            dialogTitle: I18N.message("perc.ui.session.timeout@Still There"),
            dialogMessageTimeout: I18N.message("perc.ui.session.timeout@Auto Log Out"),
            dialogConnectionWarning: I18N.message("perc.ui.session.timeout@Lost Connection")
        };

        // Extend user-set options over defaults
        var o = defaults;

        if (options) {
            o = $.extend(defaults, options);
        }


        var status = {
            "expiry": 3600000,
            "warning": 60000,
            "counter": 0
        };

        var dialog = null;

        var cancelError = false;

        // Setup when created check if we were opened as a popup
        // If we are we should close on logout
        var popup = (window.opener && window.opener !== window);


        var isDialogOn = false;

        var myStorageTime = Date.now();

        var connectionFailure = false;

        var lastActivity = Date.now();



        // Set action based upon current state of status object
        function checkWarning() {
            if (status.expiry <= 0) {
                logout();
            } else if (status.expiry < status.warning || connectionFailure) {
                if (dialog === null) {
                    dialogOn();
                }
                setCounter();
            } else {
                if (dialog !== null) {
                    dialogOff();
                }
            }
        }


        // Update the dialog counter. We only ping the server ever serverCheck
        // seconds. A longer time will mean other windows may not see an update
        // as quickly but will not load the server as much
        function setCounter() {
            var now = new Date().getTime();
            var elapsed = (now - myStorageTime);
            var remain = status.expiry - elapsed;
            if (remain < 0) remain = 0;

            if (remain > 0) {
                $('#logouttime').html(_millisecondsToStr(remain));
            } else {
                if (!connectionFailure) {
                    dialogOff();
                }
                $('#logouttime').html("");
            }


        }

        // Turn on the warning dialog.
        function dialogOn() {

            if (typeof uiVersion != 'undefined' && uiVersion === 'Minuet' && dialog !== 'active') {
                openMinuetWarningDialog();
            }
            else {
                dialog = openWarningDialog();
            }
            isDialogOn = true;
            setCounter();


        }

        function openMinuetWarningDialog() {
            dialog = 'active';
            var dialogObject = {};
            dialogObject.type = 'warning';
            dialogObject.title = o.dialogTitle;
            if (connectionFailure) {
                dialogObject.message = o.dialogConnectionWarning;
            } else {
                dialogObject.message = o.dialogMessageTimeout;
            }
            processTemplate(dialogObject, 'templatePercSessionExpiringDialog', 'percDialogTarget');
            // Temporarily using jQuery UI on this because of a scoping issue with two versions of jQuery
            $('#percDialogTarget').fadeIn('slow');

        }

        // Turn off the warning dialog
        function dialogOff() {

            if (dialog !== null) {
                if (typeof uiVersion !== 'undefined' && uiVersion === 'Minuet') {
                    hideSection('percDialogTarget', 'fadeOut', true);
                }
                else {
                    dialog.remove();
                }
                dialog = null;
            }
            isDialogOn = false;

        }


        // Convert milliseconds to a hh:mm:ss
        function _millisecondsToStr(t) {

            var seconds = Math.floor((t / 1000) % 60);
            var minutes = Math.floor((t / 1000 / 60) % 60);
            var hours = Math.floor((t / (1000 * 60 * 60)) % 24);
            var ret = "";
            if (hours > 0) {
                ret += hours + ' hour';
                if (hours > 1)
                    ret += "s";
                ret += " ";
            }
            return ret + minutes + ' min ' + seconds + "s";

        }

        // Logout from the server and close if popup window
        function logout() {
            if (!connectionFailure) {
                dialogOff();
            }
            if (popup) {
                $.get("/Rhythmyx/logout", function () {
                    window.close();
                });
            } else {
                top.document.location.href = '/Rhythmyx/logout';
            }
        }


        /**
         * Constructs the dialog
         * @param finder The finder object.
         */
        function openWarningDialog() {


            dialog = createDialog();

            /**
             * Builds the dialog (and its form) invoking perc_dialog()
             * @returns jQuery element wrapping the dialog and form created with perc_dialog()
             */
            function createDialog() {
                let dialogMessage;
                let button1;

                if (connectionFailure) {
                    button1 = "Close";
                    dialogMessage = o.dialogConnectionWarning;


                } else {
                    dialogMessage = o.dialogMessageTimeout;
                    button1 = "Close";
                }

                // Basic dialog content HTML markup
                var dialogContent = "<div id='perc-session-timeout-content'><p>" +
                    dialogMessage + '</p><p><span id="logouttime"/>' +
                    "</p></div>";
                var buttons = {};
                buttons[button1] = function () {
                    // Should close by mouse activity or keyboard
                };


                // Create the upload dialog
                var d = $(dialogContent).perc_dialog({
                    title: o.dialogTitle,
                    id: "perc-session-timeout-dialog",
                    width: 400,
                    resizable: false,
                    modal: true,
                    //closeOnEscape : true,
                    buttons: buttons
                });

                return $(d);
            }

            return dialog;

        } // End of function: openUploadDialog


        // Get the session status from the server and set the right
        // Timers and dialog based on the result
        function updateServerStatus(refresh) {
            var now = new Date().getTime();

            var offset = now - lastActivity;

            var parms = {
                lastActivity: offset
            };


            var storageTime =  Number(localStorage.getItem("serverCheckTime"));
            if (storageTime) {
                if (now - storageTime < 1000) {
                    return;
                }
                else if (storageTime > myStorageTime) {
                    // Other window updated the status so lets get it
                    status = JSON.parse(localStorage.getItem("serverStatus"));
                    myStorageTime = storageTime;
                    console.log("other setStatus" + JSON.stringify(status));
                    checkWarning();
                    return;
                }
            }
            else {
                myStorageTime = now;
                localStorage.setItem("serverCheckTime", now.toString());
            }

            // adjustedExpiry gives us what we expect if no activity since last status
            var adjustedExpiry = status.expiry - (now - myStorageTime);
            if (adjustedExpiry > status.warning && !refresh)
                return;

            checkWarning();

            if (isDialogOn && adjustedExpiry > 2000 && lastActivity < myStorageTime && !refresh)
                return;

            console.log("Checking server status offset " + offset + "dialogOn=" + isDialogOn);
            $.ajax({
                url: o.sessionStateUrl,
                type: 'get',
                cache: false,
                data: parms,
                contentType: 'application/json; charset=utf-8',
                dataType: 'json',
                success: function (json) {
                    if (connectionFailure) {
                        connectionFailure = false;
                    }
                    cancelError = false;
                    status = json;
                    myStorageTime = now;
                    localStorage.setItem("serverCheckTime", now.toString());
                    localStorage.setItem("serverStatus", JSON.stringify(status));

                    console.log("setStatus" + JSON.stringify(status));

                    checkWarning();

                },
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    console.log("error :" + XMLHttpRequest.responseText);
                    if (!cancelError) {
                        connectionFailure = true;
                    }
                    // If cannot connect assume countdown continues
                    status.expiry = status.expiry - (now - myStorageTime);

                    myStorageTime = now;
                    checkWarning();
                }

            });
        }


        function syncLast() {
            var storageActivity = new Number(localStorage.getItem("lastActivity"));
            if (storageActivity) {
                var timeDiff = lastActivity - storageActivity;
                if (timeDiff > 1000) {
                    localStorage.setItem("lastActivity", lastActivity.toString());

                }
                if (timeDiff < 0) {
                    //console.log("Other window updated last activity")
                    lastActivity = storageActivity;
                }
            } else {
                localStorage.setItem("lastActivity", lastActivity.toString());
            }
        }

        //  Only monitor activity to the second
        function userActivity() {
            var now = Date.now();
            var timeDiff = now - lastActivity;
            if (timeDiff > 1000) {
                lastActivity = Date.now();
                syncLast();
            }
        }


        function timerIncrement() {
            syncLast();
            updateServerStatus(false);

        }


        //  Track user activity events
        document.addEventListener('visibilitychange', function () {
            console.log("Visibility change");
            userActivity();
        }, false);

        $(document).mousemove(function () {
            userActivity();
        });

        $(document).keypress(function () {
            userActivity();
        });

        // Initial sync of status
        syncLast();
        updateServerStatus(true);

        var serverInterval = setInterval(timerIncrement, 1000); // 1 second

    };
})(jQuery);
