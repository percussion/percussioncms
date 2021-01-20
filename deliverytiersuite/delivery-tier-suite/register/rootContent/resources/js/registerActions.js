/*
 *     Percussion CMS
 *     Copyright (C) Percussion Software, Inc.  1999-2020
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *      Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

$(document).ready(function() {
    var methodName = $.perc_constants.METHOD_NAME;
    var serviceBase = window.location.hostname;
    var tennantID = $.perc_constants.TENNANT_ID;
    var privacyPolicyURL = $.perc_constants.PRIVACY_POLICY_URL;
    var helpURL = $.perc_constants.HELP_URL;
    
    $(".perc-privacy-policy-link-anchor").attr("href", privacyPolicyURL);
    $(".perc-help-link-anchor").attr("href", helpURL);
    
    function showProcessing()
    {
        var processingDiv = $("<img>")
                                .addClass("perc-processing")
                                .attr("src","resources/images/Busy.gif")
                                .width("15px")
                                .height("15px");
        $('#field-general-error-container').html("");
        $('#field-general-error-container').append(processingDiv);
        $('#field-general-error-container').append("<span>Processing ...</span>");
        $('#field-general-error-container').css("visibility","visible");
        $("#perc-register-button").unbind("click");
        $("#perc-register-button").addClass("disabled");
    }
    
    function hideProcessing()
    {
        $('#field-general-error-container').html("");
        $('#perc-register-button').click(doRegister);
        $("#perc-register-button").removeClass("disabled");
    }
    
    function hideErrorFields(field)
    {
        if (field != null)
        {
            field.css("visibility", "hidden");
        }
        else
        {
            $(".field-error-container").css("visibility", "hidden");                    
        }
    }

    function validateFields()
    {
        var email = $.trim($("#field-registration-email").val());
        var companyName = $.trim($("#field-registration-company").val());
        var firstName = $.trim($("#field-registration-first-name").val());
        var lastName = $.trim($("#field-registration-last-name").val());
        
        var flag = true;
        if (email == null || email == "")
        {
            flag = false;
            $("#field-email-error-container").css("visibility", "visible");
        }
        if (companyName == null || companyName == "")
        {
            flag = false;
            $("#field-company-error-container").css("visibility", "visible");
        }
        if (firstName == null || firstName == "")
        {
            flag = false;
            $("#field-first-name-error-container").css("visibility", "visible");
        }
        if (lastName == null || lastName == "")
        {
            flag = false;
            $("#field-last-name-error-container").css("visibility", "visible");
        }
        return flag;
    }
    
    function doRegister()
    {
        hideErrorFields();
        if (validateFields())
        {
            var email = $.trim($("#field-registration-email").val());
            var companyName = $.trim($("#field-registration-company").val());
            var firstName = $.trim($("#field-registration-first-name").val());
            var lastName = $.trim($("#field-registration-last-name").val());
            
            var serverId = "";
            if ($(document).getUrlParam("serverId") != null)
                serverId = $(document).getUrlParam("serverId");
            
            var jsonData = 
            { 
               "email" : email,
               "companyName" : companyName, 
               "firstName" : firstName,
               "lastName" : lastName,
               "serverId" : serverId
            };
            
            var url = "https://" + serviceBase + "/perc-thirdparty-services/netsuite/restletProxy";
            var urlMethod = "https://" + serviceBase + "/perc-thirdparty-services/netsuite/method/" + methodName;
            showProcessing();
            $.PercServiceUtils.makeXdmJsonRequest("https://" + serviceBase, urlMethod, $.PercServiceUtils.TYPE_GET, function(statusMethod, results)
            {
                if(statusMethod == $.PercServiceUtils.STATUS_SUCCESS)
                {
                    var dataObject = { 
                        "id": "1",
                        "method": results.data.methodType,
                        "scriptURL": results.data.url,
                        "jsonData": $.URLEncode(JSON.stringify(jsonData))
                    }
                    $.PercServiceUtils.makeXdmJsonRequest("https://" + serviceBase, url, $.PercServiceUtils.TYPE_POST, function(status, results)
                    {
                        hideProcessing();
                        hideErrorFields($('#field-general-error-container'));
                        if(status == $.PercServiceUtils.STATUS_SUCCESS)
                        {
                            if (results.data.status == $.PercServiceUtils.STATUS_SUCCESS){
                                $("#registration_form").submit();
                            }
                            else
                            {
                                $("#field-general-error-container").text(results.data.message);
                                $("#field-general-error-container").css("visibility", "visible");
                            }
                        }
                        else
                        {
                            $("#field-general-error-container").text(results.message);
                            $("#field-general-error-container").css("visibility", "visible");
                        }
                        
                    }, dataObject);
                }
            });
        }
    }
    
    $("#field-registration-email").keydown(function(){
        hideErrorFields($("#field-email-error-container"));
        hideErrorFields($("#field-general-error-container"));
    });
    $("#field-registration-company").keydown(function(){
        hideErrorFields($("#field-company-error-container"));
        hideErrorFields($("#field-general-error-container"));
    });
    $("#field-registration-first-name").keydown(function(){
        hideErrorFields($("#field-first-name-error-container"));
        hideErrorFields($("#field-general-error-container"));
    });
    $("#field-registration-last-name").keydown(function(){
        hideErrorFields($("#field-last-name-error-container"));
        hideErrorFields($("#field-general-error-container"));
    });
    
    $('#perc-register-button').click(doRegister);
});