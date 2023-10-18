/**
 This function takes care of the session timout logic.
 */
(function($) {
	$.perc_sessionTimeout = function(options) {
		var dialogTitle =  I18N.message("perc.ui.session.timeout@Still There");
		var dialogMessageTimeout =  I18N.message("perc.ui.session.timeout@Auto Log Out");

		var isDialogOn = false;
		var countDownTime = null;

		var sessionTimeoutLimit = null;
		var sessionTimeoutWarning = null;

		var popup = (window.opener && window.opener !== window);

		getSessionTimeoutVariables();

		/**
		 Getting the session variables from config.xml file through servlet.
		 For example in config.xml:
		 <userSessionTimeout>1800</userSessionTimeout>
		 <userSessionWarning>60</userSessionWarning>
		 */
		function getSessionTimeoutVariables() {
			console.log("Calling getSessionTimeoutVariables() function***************");
			$.ajax({
				url: '/sessionCheckServlet',
				type: 'get',
				cache: false,
				contentType: 'application/json; charset=utf-8',
				dataType: 'json',
				success: function(json) {
					console.log("sessionTimeoutLimit: " + json['expiry']);
					console.log("sessionTimeoutWarning: " + json['warning']);

					sessionTimeoutLimit = json['expiry'];
					sessionTimeoutWarning = json['warning'];

					localStorage.setItem("sessionTimeoutLimit", sessionTimeoutLimit);
					localStorage.setItem("sessionTimeoutWarning", sessionTimeoutWarning);

					var sessionPopupTime = (sessionTimeoutLimit-sessionTimeoutWarning)*1000;

					//Entry point for session timeout logic.Setting setInterval process to execute when only warning time left in session timeout.
					callSetInterval(sessionPopupTime);


				},
				error: function(XMLHttpRequest, textStatus, errorThrown) {
					console.log("error :" + XMLHttpRequest.responseText);
				}
			});
		}

		/**
		 This function is the main function which take cares of session timeout functionality.
		 */
		function trackSession() {
			console.log("Calling trackSession***************");
			sessionTimeoutLimit = localStorage.getItem("sessionTimeoutLimit");
			sessionTimeoutWarning = localStorage.getItem("sessionTimeoutWarning");
			if (sessionTimeoutWarning != null && sessionTimeoutLimit != null) {
				if (isDialogOn) {
					var timeLeft = countDownTime / 1000;
					if (timeLeft < 1) {
						logout();
					} else {
						$('#logouttime').html(convertMilliSecondsToStr(getRemainingTime()));
					}
					return;
				}
				countDownTime = sessionTimeoutWarning*1000;
				openSessionDialog();

				//call tracksession() function for the remaining time in session timeout, to show decreasing seconds on warning popup.
				callSetInterval(1000);
			}
		}

		/**
		 Returns remaining second after decreasing one second from it.
		 */
		function getRemainingTime() {
			return countDownTime -= 1000;

		}

		function convertMilliSecondsToStr(t) {
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
			return ret + minutes + ' min ' + seconds + "s";;
		}

		/**
		 Extending the session so that session is alive. It is hitting the server through servlet.
		 After hitting servlet closing the session timeout dialog and resetting the variables.
		 */
		function extendSession() {
			console.log("Calling extendSession***************");
			$.ajax({
				url: '/sessionExtendServlet',
				type: 'get',
				cache: false,
				contentType: 'application/json; charset=utf-8',
				dataType: 'json',
				success: function(json) {
					console.log("success: " + json['success']);
				},
				error: function(XMLHttpRequest, textStatus, errorThrown) {
					console.log("error :" + XMLHttpRequest.responseText);
				}
			});
		}

		function openSessionDialog() {
			dialog = createDialog();
			$('#logouttime').html(convertMilliSecondsToStr(countDownTime));

			function createDialog() {
				let dialogMessage;
				let button1;

				isDialogOn = true;
				dialogMessage = dialogMessageTimeout;
				button1 = "Close";


				// Basic dialog content HTML markup
				var dialogContent = "<div ' id='perc-session-timeout-content'><p>" +
					dialogMessage + '</p><p><span id="logouttime"/>' +
					"</p></div>";
				var buttons = {};

				// Create the upload dialog
				var d = $(dialogContent).perc_dialog({
					title: dialogTitle,
					id: "perc-session-timeout-dialog",
					width: 400,
					resizable: false,
					modal: true,
					buttons: buttons,
					percButtons: {
						"Close": {
							click: function() {
								closeSessionDialog();
							},
							id: "perc-wfstep-close_session"
						}
					}
				});
				return $(d);
			}

			return dialog;

		} // End of function: openUploadDialog

		function closeSessionDialog() {
			extendSession();
			dialog.remove();
			isDialogOn = false;
			localStorage.setItem("lastRequestTime", new Date().getTime());
			countDownTime = null;

			//Initiate new session timeout cycle
			var sessionPopupTime = (sessionTimeoutLimit-sessionTimeoutWarning)*1000;
			callSetInterval(sessionPopupTime);
		}

		// Logout from the server and close if popup window
		function logout() {
			if (popup) {
				$.get("/Rhythmyx/logout", function() {
					window.close();
				});
			} else {
				top.document.location.href = '/Rhythmyx/logout';
			}
		}

		//call trackSession() function after a given (intervalTime) time.
		function callSetInterval(intervalTime){
			var intervalId = localStorage.getItem("intervalId");
			if(intervalId != null){
				clearInterval(intervalId);
			}

			console.log("1_intervalTime: " + intervalTime);
			intervalId = setInterval(trackSession, intervalTime);
			localStorage.setItem("intervalId", intervalId);
		}

	};
})(jQuery);