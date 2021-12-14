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
