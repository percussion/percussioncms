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

(function($){
    $(document).ready(function(){ 
            $( ".form-datepicker" ).datepicker({
                showOn: "button",
                buttonImage: "/web_resources/widgets/form/images/calendar.gif",
                buttonText: "Date Picker",
                dateFormat: "d M, yy",
                buttonImageOnly: true,
                changeMonth: true,
                changeYear: true,
                onClose: function(dateText, inst) {
                        $(this).trigger('focusout');     
                                                }


            });
            
            /**
             * Returns  a string containing the schema, domain and if present the port
             * @param {String} url The url to extract the location from
             * @return {String} The location part of the url
             */
            function getLocation(url){
                var reURI = /^((http.?:)\/\/([^:\/\s]+)(:\d+)*)/; // returns groups for protocol (2), domain (3) and port (4)

                var m = url.toLowerCase().match(reURI);
                var proto = m[2], domain = m[3], port = m[4] || "";
                if ((proto === "http:" && port === ":80") || (proto === "https:" && port === ":443")) {
                    port = "";
                }
                return proto + "//" + domain + port;
            }
			
			/**
             * Returns  a string containing the name of requested email-to name from URL
             * @param {String} url The url to extract the name from
             * @return {String} The name parameter as a string of the url
             */
			function getName(url) {
				var nameResult = "Not found",
					temp = [];
				location.search
				.substr(1)
					.split("&")
					.forEach(function (item) {
					temp = item.split("=");
					if (temp[0] === 'name') nameResult = decodeURIComponent(temp[1]);
				});
				return nameResult;
			}
          
            $('.perc-form').find('form').each(function(){
				//if the URL has a name parameter, it will set the default value of the send to option 
				// to what is passed in by the URL
				var name = getName(location.href),
					length = 0;
				
				length = ($('option[data-personname="' + name + '"]').length);
				
				if(name !== 'Not found' && length > 0) {
					document.querySelector('option[data-personname="' + name + '"]').selected = 'selected';
				}
				
                var formAction = $(this).attr("action");
                if(formAction && formAction.indexOf("/perc-form-processor") === 0){
                    var version = $.isFunction($.getCMSVersion)?$.getCMSVersion():"";
					var servicebase = $.isFunction($.getDeliveryServiceBase)?$.getDeliveryServiceBase():"";


                    formAction = $.PercServiceUtils.joinURL( servicebase, formAction + ((formAction.indexOf('?')!==-1)?"&":"?") + "perc-version=" + version);

                    $(this).attr("action", formAction);
                    $(this).append(
                        $('<input/>').
                        attr("type", "hidden").
                        attr("name", "perc_hostUrl").
                        attr("value", getLocation(location.href))
                    );
                }
                
                var myRules = {};
                $(this).find('input[type=text], textarea').each(function(){
                    if($(this).attr('fieldmaxlength') > 0)
                    {
                        if(typeof(myRules[$(this).attr('name')]) === "undefined")
                            myRules[$(this).attr('name')] = {};
                        
                        myRules[$(this).attr('name')].maxlength = $(this).attr('fieldmaxlength');
                    }
                });
                
                $(this).validate({
                    errorClass:"form-error-msg"  ,
                    errorPlacement: function(error, element) {
                                       if(element.attr('type') === 'checkbox'){
                                           error.appendTo( element.parent().parent());    
                                        }
                                        else{									
                                           error.appendTo( element.parent());
                                        }   
                                    },
                    rules:myRules				
                                    
                });
            });		
    });
})(jQuery);