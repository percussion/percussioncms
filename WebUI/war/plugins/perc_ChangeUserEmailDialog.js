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
