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
 * PercChangePasswordDialog.js
 * @author Anthony Romano
 *
 * Show a change password.
 */
(function ($) {
	// public API
	$.perc_ChangePwDialog = {
		open : openDialog
	};

	function openDialog() {
		var dialog,
			userInfo = {},
			dialogHTML = createDialogHTML();

		userInfo.username = $.PercNavigationManager.getUserName();

		$.PercUserService.findUser(userInfo.username, function (status, response) {
			if (status !== $.PercUserService.STATUS_ERROR) {
				userInfo.roles = $.perc_utils.convertCXFArray(response.User.roles);
				userInfo.email = response.User.email;

				if (response.User.providerType === 'INTERNAL') {
					dialog = $(dialogHTML).perc_dialog({
						resizable : false,
						title : I18N.message('perc.ui.change.pw@Change Password'),
						modal : true,
						closeOnEscape : true,
						percButtons : {
							//FIXME: I18N
							'Close' : {
								click : function () {
									dialog.remove();
								},
								id : 'perc-changepw-cancel'
							}
						},
						id : 'perc-changepw-dialog',
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
			$('#changePasswordForm').validate({
				rules : {
					newPassword : {
						required : true,
						minlength : 6
					},
					confirmPassword : {
						required : true,
						minlength : 6,
						equalTo : '#newPassword'
					}
				},
				errorElement: 'em',
				submitHandler : function (form) {
					$('.pw-submit').attr('disabled', true);
					$('.pw-message').show();
					var payload = {
							User : {
								name : userInfo.username,
								password : form.newPassword.value,
								roles : userInfo.roles,
								email : userInfo.email
							}
						},
						callback = function (status) {
							$('.pw-submit').attr('disabled', false);
							$('.pw-message').hide();
							if (status === $.PercServiceUtils.STATUS_SUCCESS) {
								$('.pw-status').addClass('success').html('<b>' + I18N.message("perc.ui.change.pw@Success") + '</b> ' + I18N.message("perc.ui.change.pw@Success Message") + '').show();
							} else if (status === $.PercServiceUtils.STATUS_ERROR) {
								$('.pw-status').addClass('danger').html('<b>' + I18N.message("perc.ui.change.pw@Error") + '</b> ' + I18N.message("perc.ui.change.pw@Error Updating") + '').show();
							}
						};
					$.PercUserService.changePassword(payload, callback);
				}
			});
		}

		function createDialogHTML(baseUrl) {
			return '<div class="pw-container">' +
				 '    <form id="changePasswordForm" name="changePasswordForm" class="pw-form" novalidate>' +
				 '        <div class="pw-status" style="display:none"></div>' +
				 '        <p>' +
				 '            <label for="newPassword">' +
				 '                <span>' + I18N.message("perc.ui.change.pw@New Password") + '</span>' +
				 '                <input type="password" id="newPassword" name="newPassword" placeholder= "' + I18N.message("perc.ui.change.pw@Enter New Password") + '" autofocus autocomplete="off" />' +
				 '            </label>' +
				 '            <label for="confirmPassword">' +
				 '                <span>' + I18N.message("perc.ui.change.pw@Confirm New Password") + '</span>' +
				 '                <input type="password" id="confirmPassword" name="confirmPassword" placeholder= "' + I18N.message("perc.ui.change.pw@Confirm New Password") + '" autocomplete="off" />' +
				 '            </label>' +
				 '                <button class="pw-submit" type="submit">'+ I18N.message("perc.ui.change.pw@Change Password") + '</button>' +
				 '                <span class="pw-message" style="display:none">' + I18N.message("perc.ui.change.pw@Working on it") + '</span>' +
				 '        </p>' +
				 '    </form>' +
				 '</div>';
		}
	}
})(jQuery);
