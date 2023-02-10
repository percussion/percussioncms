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
 * PercChangeUserEmailDialog.js.js
 * @author Santosh Dhariwal
 *
 * Show a change email.
 */
(function ($) {
	// public API
	$.perc_ChangeUserEmailDialog = {
		openDialogIfEmptyEmail : openDialogIfEmptyEmail
	};

	function openDialogIfEmptyEmail() {
		var dialog,
			userInfo = {},
			dialogHTML = createDialogHTML();

		userInfo.username = $.PercNavigationManager.getUserName();

		$.PercUserService.findUser(userInfo.username, function (status, response) {
			if (status !== $.PercUserService.STATUS_ERROR) {
				userInfo.roles = $.perc_utils.convertCXFArray(response.User.roles);
				userInfo.email = response.User.email;
				if(userInfo.email){
					return;
				}

				if (response.User.providerType === 'INTERNAL') {
					dialog = $(dialogHTML).perc_dialog({
						resizable : false,
						title : I18N.message('perc.ui.change.pw@Change Email'),
						modal : true,
						closeOnEscape : true,
						percButtons : {
							//FIXME: I18N
							'Close' : {
								click : function () {
									dialog.remove();
								},
								id : 'perc-changeEmail-cancel'
							}
						},
						id : 'perc-changeEmail-dialog',
						open : function () {
							initValidator();
						}
					});
				}
			} else {
                $.perc_utils.alert_dialog({
                    title : I18N.message("perc.ui.change.pw@Error") ,
                    content : i18N.message("perc.ui.change.pw@Error Message")
                });
			}
		});

		function initValidator() {
			$('#changeEmailForm').validate({
				rules : {
					newEmail : {
						required : true,
						minlength : 6
					},
					confirmEmail : {
						required : true,
						minlength : 6,
						equalTo : '#newEmail'
					}
				},
				errorElement: 'em',
				submitHandler : function (form) {
					$('.pw-submit').attr('disabled', true);
					$('.pw-message').show();
					var payload = {
							User : {
								name : userInfo.username,
								password: userInfo.password,
								roles : userInfo.roles,
								email : form.newEmail.value
							}
						},
						callback = function (status) {
							$('.pw-submit').attr('disabled', false);
							$('.pw-message').hide();
							if (status === $.PercServiceUtils.STATUS_SUCCESS) {
								$('.pw-status').addClass('success').html('<b>' + I18N.message("perc.ui.change.email@Success") + '</b> ' + I18N.message("perc.ui.change.email@Success Message") + '').show();
							} else if (status === $.PercServiceUtils.STATUS_ERROR) {
								$('.pw-status').addClass('danger').html('<b>' + I18N.message("perc.ui.change.email@Error") + '</b> ' + I18N.message("perc.ui.change.email@Error Updating") + '').show();
							}
						};
					$.PercUserService.updateUser(payload, callback);
				}
			});
		}

		function createDialogHTML(baseUrl) {
			return '<div class="email-container">' +
				 '    <form id="changeEmailForm" name="changeEmailForm" class="pw-form" novalidate>' +
				 '        <div class="pw-status" style="display:none"></div>' +
				 '        <p>' +
				 '            <label for="newEmail">' +
				 '                <span>' + I18N.message("perc.ui.change.email@New Email") + '</span>' +
				 '                <input type="email" id="newEmail" name="newEmail" placeholder= "' + I18N.message("perc.ui.change.email@Enter New Email") + '" autofocus autocomplete="off" />' +
				 '            </label>' +
				 '            <label for="confirmEmail">' +
				 '                <span>' + I18N.message("perc.ui.change.email@Confirm New Email") + '</span>' +
				 '                <input type="email" id="confirmEmail" name="confirmEmail" placeholder= "' + I18N.message("perc.ui.change.email@Confirm New Email") + '" autocomplete="off" />' +
				 '            </label>' +
				 '                <button class="pw-submit" type="submit">'+ I18N.message("perc.ui.change.email@Change Email") + '</button>' +
				 '                <span class="pw-message" style="display:none">' + I18N.message("perc.ui.change.email@Working on it") + '</span>' +
				 '        </p>' +
				 '    </form>' +
				 '</div>';
		}
	}
})(jQuery);
