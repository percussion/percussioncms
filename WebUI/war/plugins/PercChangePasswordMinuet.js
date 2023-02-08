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
                $('.perc-toggle-password').trigger('focus');
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
