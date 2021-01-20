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

/**
 * PercRegistrationView.js
 * 
 * @author Luis Mendez 
 * View for RegistrationWidget. Contains functionality to handle the registration and redirect behaviors
 * (*) Dependencies
 * /jslib/jquery.cookie.js
 * /jslib/jquery.validate.js
 */
(function($)
{
    $(document).ready(function() {
        $.PercRegistrationView.init();
    });
    var regMode = "";
    $.PercRegistrationView = {
        init : init
    };
    /**
     * Initialize and configure each instance of registration widget in the page.
     */
    function init() {
        var urlstring = $.deparam.querystring();
        
        // Verify that resetkey is valid when Registration form is rendered in Reset password mode        
        if("undefined" !== typeof (urlstring.resetkey) && 0 < $("#perc-password-reset").length) {
            var resetKey = urlstring.resetkey;
             $.PercMembershipService.validateResetPwKey(resetKey, function(status, data) {
                 data = $.PercServiceUtils.toJSON(data);
                if("SUCCESS" === data.status) {
                    var userEmail = data.userSummary.email;
                    var resetForm = $("#perc-password-reset");
                    resetForm.show();
                    resetForm.find("#perc-registration-email-field").val(userEmail);
                    resetForm.data("key", resetKey);                    
                }
                else {
                    console.log(data.message);
                }
             
             });
        }

        //Show the registration form if there is no rvkey param in the url if it exists then confirm the user and redirect them to reg success page.
        if("undefined" !== typeof (urlstring.rvkey) && 0 < $(".perc-reg-confirmation-mode").length) {
            $(".perc-reg-confirmation-mode").show();
            var rvKey = urlstring.rvkey;
             $.PercMembershipService.confirmRegistration(rvKey, function(status, data) {
                 data = $.PercServiceUtils.toJSON(data);
                if("SUCCESS" === data.status) {
                    var redirectUrl = $("#perc-confirmation-page").val();
                    if(!redirectUrl || "" === redirectUrl) {
                        redirectUrl = "/";
                    }
                    window.location.href=redirectUrl;
                }
                else {
                    $(".perc-reg-confirmation-message").text(data.message);
                }
             });
        }
        else if($(".perc-registration-mode").length)
        {
            $(".perc-registration-mode").show();
        }

        // If user is logged-in remove the password reset form and password reset request form from DOM
        var sessionId = $.cookie('perc_membership_session_id');
            if (null !== sessionId && 'undefined' !== typeof (sessionId)) {
                //If the user is logged in, show the welcome message
                $.PercMembershipService.getUser(sessionId, function(status, data) {
                    if (status === $.PercServiceUtils.STATUS_SUCCESS && '' !== data.email) {
                        $("#perc-password-request, #perc-password-reset").remove();
                    }
                });         
            }
            
            
        $('#perc-registration-form, #perc-registration-cform').each(function() {
            var self = $(this);
            var options;
            
            // If data property does not exists, create it and add the paramUrlRedirect element
            if (self.parent(".percRegistration").attr("data") !== undefined) {
                options = $.parseJSON(self.parent(".percRegistration").attr("data"));
            }
            else {
                options = {};
            }

            if($("#perc-registration-cform").length)
            {
                options.verReq = true;
            }
            self.data("options", options);
            self.submit(handleSubmitForm);
        });
        
        // Bind the submit button of password reset request form        
        $("#perc-password-request").each(function() {
            var self = $(this);
            self.submit(handleResetPwdRequest);
        });
        
        // Bind the submit button of password reset form
        $("#perc-password-reset").each(function() {
            var self = $(this);
            self.submit(handleResetPassword);
        });
    }
    
    /**
     * Callback bound to submit event of reset password request form.
     * @param e
     * @return boolean false Because we are preventing a form submission
     */
     
     function handleResetPwdRequest(e) {
        e.preventDefault();
        var self = $(this);
        if (self.validate().form()) {
            var email = $("#perc-registration-email-field").val();
            var redirectUrl = $("input[name='perc_password_reset_page']").val();
            $.PercMembershipService.resetPwRequest(email, redirectUrl, function(status, data) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    if("AUTH_FAILED" === data.status) {
                        $(".perc-reg-error-message").show().html(data.message);
                    }
                    else {
                    //On Reset Request Successful  show the confirmation message.
                    $(".perc-confirmation-message").show();
                    $("#perc-password-request").remove();
                    $(".perc-reg-error-message").hide(); 
                    }
                   
                }
                else
                {
                    alert(data.message);
                }
            });
        }
        return false;
     }
     
     
     /**
     * Callback bound to submit event of update password form.
     * @param e
     * @return boolean false Because we are preventing a form submission
     */
      function handleResetPassword(e) {
        e.preventDefault();
        var self = $(this);
        if (self.validate().form()) {
            var email = $("#perc-registration-email-field").val();
            var password = $("#perc-registration-password-field").val();
            var resetKey = self.data("key");
            $.PercMembershipService.resetPassword(email, password, resetKey, function(status, data) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                     // If successful login, store the ID session in a cookie and redirect to corresponding page
                    $.cookie('perc_membership_session_id', data.sessionId, { "path": "/", "expires": "null" });
                    // After successfully resetting the password - take user to home page fo website.                   
                    window.location.replace("/");

                }
                else
                {
                    alert(data.message);
                }
            });
        }
        return false;
    }
     
    
    /**
     * Callback bound to submit event of registration widget.
     * @param e event
     * @return boolean false Because we are preventing a form submission
     */
      function handleSubmitForm(e) {
          e.preventDefault();
          var self = $(this);
          if (self.validate().form()) {
              // Retrieve widget and form options
              var widgetOptions = $.parseJSON(self.parent().parent(".percRegistration").attr("data"));
              var options = self.data("options");
              
              var registerObj = {
                  email: self.find("#perc-registration-email-field").val(),
                  password: self.find("#perc-registration-password-field").val(),
                  confirmationRequired:"perc-registration-cform" === self.attr("name"),
                  confirmationPage: window.location.href
              };
              
              //registraionPageUrl:options.verReq
              $.PercMembershipService.register(registerObj, function(status, data) {
                  data = $.PercServiceUtils.toJSON(data);
                  // Retrieve where the user came from (URL param) and Confirmation page
                  var urlstring = $.deparam.querystring();
                  var redirectURL = "";
                  if ("undefined" !== typeof (urlstring.registrationRedirect)){
                      redirectURL = urlstring.registrationRedirect
                  }
                  var confirmation_page = self.find("#perc-confirmation-page").val();
                  
                  if (status === $.PercServiceUtils.STATUS_SUCCESS) {
                      // Check for registration service REST service result and redirect
                      if (data.status.toUpperCase() === $.PercServiceUtils.STATUS_SUCCESS.toUpperCase()) {
                          if(!options.verReq) {
                              // If successful registration, store the ID session in a cookie
                              $.cookie('perc_membership_session_id', data.sessionId, { "path": "/", "expires": "null" });
                              
                              // If there is no confirmation page redirect to registrationRedirect (after
                              // removing the corresponding URL parameter)
                              if ('' === confirmation_page) {
                                  // Confirmation page and registrationRedirect are not defined, 
                                  // redirect to Home
                                  if ('' === redirectURL) {
                                      redirectURL = widgetOptions.homePageLink;
                                  }
                                  else {
                                      delete urlstring.registrationRedirect;
                                  }
                                  
                                  var params = $.param.querystring('', urlstring);
                                  if (1 < params.length) {
                                      window.location = redirectURL + params;
                                  }
                                  else {
                                      window.location = redirectURL
                                  }
                              }
                              // Redirect to the confirmation page and pass all the current URL parameters
                              else {
                                  var params = '';
                                  if ($.param.querystring()) {
                                      params = '?' + $.param.querystring(); 
                                  }
                                  window.location = confirmation_page + params;
                              }
                          }
                          else
                          {
                              $(".perc-registration-mode").hide();
                              $(".perc-reg-confirmation-message").html("<div>Thank you for registering with us.</div><div>Please check your email and confirm your registration to activate your account.</div>");
                              $(".perc-reg-confirmation-mode").show();
                          }
                    }
                    else {
                        // If the registration wasn't succesful show the error message inline and remain in the same page.
                    	$(".perc-reg-error-message").show().html(data.message);
                    }
                }
                else {
                    // If there was an error with the AJAX registration request
                	$(".perc-reg-error-message").show().html("There was an unexpected error.");
                }
            });
        }
        return false;
    }
})(jQuery);