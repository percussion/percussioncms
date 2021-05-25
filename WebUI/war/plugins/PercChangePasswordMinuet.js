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

(function ($) {
    // public API
    $.PercChangePasswordMinuet = {
        submitNewPassword : submitNewPassword,
        clearPasswordFields: clearPasswordFields,
        validateNewPassword: validateNewPassword,
        togglePassword: togglePassword,
    };

    getUserInfo();

    function submitNewPassword() {
        validateNewPassword();
        if(validPassword === true) {
            startProcessRunningAlert();
            userInfo.password = $('#percNewPassword').val();
            payload = {};
            payload.User = userInfo;
            $.PercUserService.changePassword(payload, submitNewPasswordCallback);
        }
        else {
            $('.perc-change-password-error > span').fadeOut('fast', function() {
                $('.perc-change-password-error > span').fadeIn('fast');
            });
        }

    }

    function submitNewPasswordCallback(status, result) {
        stopProcessRunningAlert();
        $.when(processRunningAnimationDeferred).then(function() {
            response = {};
            response.result = {};
            response.source = I18N.message("perc.ui.common.label@Change Password");
            if( status === 'success' ) {
                response.result.status = I18N.message("perc.ui.common.label@Change Password Success") + ' ' + result.User.name;
                togglePassword();
            }
            else {
                response.result.warning = result;
            }
            processAlert(response);
        });
    }

    function clearPasswordFields() {
        $('#percNewPassword').val('');
        $('#percConfirmNewPassword').val('');
        oldPasswordErrorText = '\xa0';
        $('.perc-change-password-error > span').text('\xa0');
    }

    function validateNewPassword() {

        if((typeof oldPasswordErrorText == 'undefined')) {
            oldPasswordErrorText = '\xa0';
        }

        if( $('#percNewPassword').val() !== $('#percConfirmNewPassword').val() ) {
            validPassword = false;
            passwordErrorText = I18N.message("perc.ui.common.label@Password Match");
        }
        else if ( $('#percNewPassword').val().length < 6 ||  $('#percConfirmNewPassword').val().length < 6 ) {
            validPassword = false;
            passwordErrorText = I18N.message("perc.ui.common.label@Password Six Characters");
        }
        else {
            validPassword = true;
            passwordErrorText = '\xa0';
        }

        if(  passwordErrorText !== oldPasswordErrorText ) {
            $('.perc-change-password-error > span').fadeOut('fast', function() {
                $('.perc-change-password-error > span').text(passwordErrorText);
            });
            $('.perc-change-password-error > span').fadeIn('fast');
        }

        oldPasswordErrorText = passwordErrorText;

    }

    function togglePassword() {
        if( $('#percPasswordDialogTarget').is(":hidden") ) {
            $('#percPasswordDialogTarget').show();
            navigationEscapeListener(false);
            $('#percPasswordDialogTarget').modal('_enforceFocus');
            $('#percPasswordDialogTarget').animateCss('fadeIn faster');
        }
        else {
            $('#percPasswordDialogTarget').animateCss('fadeOut faster', function() {
                navigationEscapeListener(true);
                $('#percNavigationBody').modal('_enforceFocus');
                $('#percPasswordDialogTarget').hide();
                $('.perc-toggle-password').focus();
            });
        }

    }

    function getUserInfo() {
        userInfo = {};
        passwordDialog = {};
        userInfo.name = $.PercNavigationManager.getUserName();
        $.PercUserService.findUser(userInfo.name, function (status, response) {
            if (status !== $.PercUserService.STATUS_ERROR) {
                userInfo.roles = $.perc_utils.convertCXFArray(response.User.roles);
                userInfo.email = response.User.email;
                if (response.User.providerType !== 'INTERNAL') {
                    // Hide the change password option if we are an external user
                    $('.perc-toggle-password').hide();
                }
            }
        });
    }

})($);
