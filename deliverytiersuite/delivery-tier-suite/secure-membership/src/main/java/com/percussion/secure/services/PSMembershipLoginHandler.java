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
package com.percussion.secure.services;

import com.percussion.secure.data.PSMembershipConfiguration;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * Class to handle setting the membership session cookie after a successful authentication
 * 
 * @author JaySeletz
 *
 */
public class PSMembershipLoginHandler extends SavedRequestAwareAuthenticationSuccessHandler
{
    private PSMembershipConfiguration membershipConfig;
    
    public void setMembershipConfig(PSMembershipConfiguration membershipConfig)
    {
        this.membershipConfig = membershipConfig;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException
    {
    	if(membershipConfig.getUseLdap() == null || membershipConfig.getUseLdap().equalsIgnoreCase("no")) {
    		
	        // get the session id and set the cookie
	        String sessionId = PSMembershipAuthProvider.getAuthenticatedSessionId();
	        if (sessionId != null)
	        {
	            Cookie cookie = new Cookie(membershipConfig.getMembershipSessionCookieName(), sessionId);
	            cookie.setSecure(true);
	            cookie.setPath("/");
	            response.addCookie(cookie);            
	        }
    	} else {
    		super.setUseReferer(false);
    	}
        // now let handling pass through to base class
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
