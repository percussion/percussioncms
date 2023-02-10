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

$(document).ready(function(){
    $('#perc-registration-form, #perc-password-reset, #perc-password-request, #perc-registration-cform').each(function(){
        var myRules = {
            'perc-registration-email-field': {
                required: true,
                minlength: 5,
                maxlength: 254,
                email: true
            },
            'perc-registration-password-field': {
                required: true,
                minlength: 6,
                maxlength: 25
            },
            'perc-registration-password-confirm-field': {
                required: true,
                equalTo: "#perc-registration-password-field"
            }
        };

        var myMessages = {
            'perc-registration-email-field': {
                email: "Invalid Email address format.",
                minlength: "Invalid Email address format.",
                maxlength: "Invalid Email address format."
            },
            'perc-registration-password-field': {
                required: "Please provide a password.",
                minlength: "Your password must be at least 6 characters long.",
                maxlength: "Your password must be less than 26 characters long."
            },
            'perc-registration-password-confirm-field': {
                equalTo: "Please provide matching password."
            }
        };
        
        $(this).validate({
            errorClass: "form-error-msg",
            errorPlacement: function(error, element) {
               if(element.attr('type') == 'checkbox'){
                   error.appendTo( element.parent().parent());
                }
                else {
                   error.appendTo( element.parent());
                }
            },
            rules: myRules,
            messages: myMessages
        });
    });
});
