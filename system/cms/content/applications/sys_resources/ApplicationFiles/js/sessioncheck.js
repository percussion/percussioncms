//
// jquery.sessionTimeout.js
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
//   4. Call $.sessionTimeout(); after document ready
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
(function($) {
    $.sessionTimeout = function(options) {
        var defaults = {
            serverCheck: 10, // Number of seconds between server checks
            sessionStateUrl: '/Rhythmyx/sessioncheck',
            logoutUrl: '/Rhythmyx/logout',
            dialogTitle: 'Are you still there?',
            dialogMessageTimeout: 'You will be automatically logged out in'
        };

        // Extend user-set options over defaults
        var o = defaults;
        if (options) {
            o = $.extend(defaults, options);
        }

        var status = {
            "expiry": 0,
            "warning": 0,
            "counter": 0
        };

        var dialog = null;


        var countdown;
        var checktimer;
        var checkTime = new Date().getTime();

        // Setup when created check if we were opened as a popup
        // If we are we should close on logout
        var popup = (window.opener && window.opener !== window);

        // Get the session status from the server and set the right
        // Timers and dialog based on the result
        function updateServerStatus(refresh) {

            var refreshParam = "";
            if (refresh) {
                refreshParam = 'extendSession=true';
                dialogOff();
            }

            var now = new Date().getTime();

            $.ajax({
                url: o.sessionStateUrl,
                type: 'get',
                cache: false,
                data: refreshParam,
                contentType: 'application/json; charset=utf-8',
                dataType: 'json',
                success: function(json) {
                    status = json;
                    checkTime = now;
                    checkWarning();
                },
                error: function(XMLHttpRequest, textStatus, errorThrown) {
                    console.log("error :" + XMLHttpRequest.responseText);
                    // If cannot connect assume countdown continues
                    status.expiry = status.expiry + now - checkTime;
                    checkTime = now;

                    checkWarning();
                }

            });
        }

        // Set action based upon current state of status object
        function checkWarning() {
            if (status.expiry <= 0) {
                logout();
            } else if (status.expiry < status.warning) {
                if (dialog === null)
                    dialogOn();
            } else {
                if (dialog !== null) {
                    dialogOff();
                }
                window.clearInterval(checktimer);

                if (status.warning < status.expiry)
                    checktimer = window.setTimeout(function() {
                        updateServerStatus(false);
                    }, status.expiry - status.warning);
            }
        }

        // Update the dialog counter. We only ping the server ever serverCheck
        // seconds. A longer time will mean other windows may not see an update
        // as quickly but will not load the server as much
        function setCounter() {
            var now = new Date().getTime();
            var elapsed = (now - checkTime);
            var remain = status.expiry - elapsed;

            if (remain > 0) {
                $('#logouttime').html(_millisecondsToStr(remain));
                if (elapsed > o.serverCheck * 1000)
                    updateServerStatus(false);
            } else {
                dialogOff();
                updateServerStatus(false);
            }


        }
        // Turn on the warning dialog.
        function dialogOn() {

			if (top.frames['navcontent']) {
					top.frames['navcontent'].$('#theCxApplet').hide()
				}

            dialog = openWarningDialog();


            window.clearInterval(countdown);
            setCounter();

            countdown = window.setInterval(function() {
                setCounter();
            }, 1000);

        }
        // Turn off the warning dialog
        function dialogOff() {
	if (top.frames['navcontent']) {
				top.frames['navcontent'].$('#theCxApplet').show();
			}
            window.clearInterval(countdown);

            if (dialog !== null) {
                dialog.remove();
                dialog = null;
            }

        }


        // Convert milliseconds to a hh:mm:ss
        function _millisecondsToStr(t) {

            var seconds = Math.floor((t / 1000) % 60);
  			var minutes = Math.floor((t / 1000 / 60) % 60);
  			var hours = Math.floor((t / (1000 * 60 * 60)) % 24);
  			var ret = "";
           	if (hours >0 )
           	{
           		ret += hours +  ' hour';
           		if (hours > 1)
           			ret += "s";
           		ret += " ";
           	}
            return ret + minutes + ' min ' + seconds + "s";

        }

        // Logout from the server and close if popup window
        function logout() {
            dialogOff();
            if (popup) {
                $.get("/Rhythmyx/logout", function(data) {
                    window.close();
                });
            } else {
                top.document.location.href = '/Rhythmyx/logout';
            }
        }

        // Extend the session
        function extendSession() {
            dialogOff();
            updateServerStatus(true);
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

			
              			$('body')
						.append(
								'<div id="session-warning" title="'
										+ o.dialogTitle
										+ '"><p><span class="ui-icon ui-icon-alert no-close" style="float: left; margin: 0 7px 20px 0;"></span>'
										+ o.dialogMessageTimeout
										+ ' <span id="logouttime"/></p></div>');

				alertDialog = $('#session-warning');
			
				var d = alertDialog.dialog({
					resizable : false,
					height : 140,
					modal : true,
					close : function() {
						dialogOff();
						extendSession();
					},
					buttons : {
						"Extend Session" : function() {
							dialogOff();
							extendSession();
						},
						Logout : function() {
							logout();
						}
					}
				});
  				return $(d);

            }

            return dialog;

        } // End of function: openUploadDialog


        // Begin warning period
        updateServerStatus(false);
    };
})(jQuery);