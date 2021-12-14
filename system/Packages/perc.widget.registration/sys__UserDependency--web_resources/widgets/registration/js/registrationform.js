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
