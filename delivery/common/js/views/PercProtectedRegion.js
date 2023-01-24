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

//Moved this file to delivery, but left it here to not to break any unknown references//
(function($)
{
    var userloggedIn = false;
    
    $(document).ready(function(){
		protectContent();
    });
    
    function protectContent() {
		//Check if there's protected content on the page.
		$('#protectedRegionInformation').each(function() {
			//Check if there's a logged in user.
			var protectedRegionInformationElem = $(this);
			var sessionId = $.cookie('perc_membership_session_id');
			userloggedIn = false;
			if (null !== sessionId && 'undefined' !== typeof (sessionId))
            {
				
                //TODO: Call GetUser service.
                $.PercMembershipService.getUser(sessionId, function(status, data)
                {
                    if (status === $.PercServiceUtils.STATUS_SUCCESS && null !== data.userSummary && '' !== data.userSummary.email)
                    {
                        userloggedIn = true;
                        
                    }
                    handleProtectContent(protectedRegionInformationElem);
                });
            }
			else
			{
				handleProtectContent(protectedRegionInformationElem);
			}
       });
    }
    
    function handleProtectContent(elem)
    {
        //Parse the region protection object.
        var protectObj = JSON.parse(elem.attr('data'));
        if("undefined" !== typeof (protectObj)){
            //Get the id of the region to protect.
            if ("undefined" !== typeof (protectObj.protectedRegion) && "" !== protectObj.protectedRegion)
            {
                if(!userloggedIn)
                {
                    var message = protectObj.protectedRegionText;
                    var linkHtml = '<div class="perc-protected-region-text"><a href="<hrefText>">'+message+'</a></div>';
                    
                    if ("undefined" !== typeof (protectObj.siteLoginPage) && "" !== protectObj.siteLoginPage)
                    {
                        linkHtml = linkHtml.replace("<hrefText>", protectObj.siteLoginPage +
                            '?loginRedirect=' + encodeURIComponent(window.location));
                    }
                    else
                    {
                        linkHtml = linkHtml.replace("<hrefText>", "#");
                    }
                    
                    $('#'+protectObj.protectedRegion).html(linkHtml);
                }
                $('#'+protectObj.protectedRegion).show();
            }
        }
     }
 
})(jQuery);
