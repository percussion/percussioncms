(function($){
    $(function(){
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
                formAction = formAction + "collect/";
                if(formAction && formAction.indexOf("/perc-form-processor") === 0){
                    var version = typeof($.getCMSVersion) === "function" ?$.getCMSVersion():"";
					var servicebase = typeof($.getDeliveryServiceBase)==="function"?$.getDeliveryServiceBase():"";


                    formAction = $.PercServiceUtils.joinURL( servicebase, formAction);

                    $(this).attr("action", formAction);
                    $(this).append(
                        $('<input/>').
                        attr("type", "hidden").
                        attr("name", "perc_hostUrl").
                        attr("value", getLocation(location.href))
                    );

                    var tokenHeader;
                    var token;

                    var doc=$(this);

                    if( jQuery("input[type = 'submit']").attr('disabled') === 'disabled'){
                        return;
                    };

                    $.PercServiceUtils.csrfGetToken($.PercServiceUtils.joinURL(servicebase,"/perc-form-processor/forms/csrf"),function (response) {
                        if (typeof response !== 'undefined' && response != null)
                            tokenHeader = response.getResponseHeader("X-CSRF-HEADER");
                        if (typeof tokenHeader !== "undefined" && tokenHeader != null){
                            token = response.getResponseHeader("X-CSRF-TOKEN");
                            if(typeof token !== "undefined" && token != null) {
                                formAction = formAction + ((formAction.indexOf('?')!==-1)?"&":"?") + "_csrf=" + token;
                                doc.attr( "action", formAction);
                            }
                        }
                    });
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
