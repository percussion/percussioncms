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
package com.percussion.secure.services;

import com.percussion.secure.data.PSMembershipConfiguration;
import org.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

//import org.eclipse.update.internal.ui.security.Authentication;

/**
 * Handle logging out of membership
 * 
 * @author JaySeletz
 *
 */
public class PSMembershipLogoutHandler extends SimpleUrlLogoutSuccessHandler
{
    
    private static Client ms_client = ClientBuilder.newClient();
    PSMembershipConfiguration membershipConfig;
    

    public void setMembershipConfig(PSMembershipConfiguration membershipConfig)
    {
        this.membershipConfig = membershipConfig;
    }
    
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException
    {
        try
        {
            // first log out of membership
            String sessionId = getSessionId(request);
            if (sessionId != null && !sessionId.isEmpty())
            {

                WebTarget webTarget = ms_client.target(membershipConfig.getBaseUrl() +
                        "/perc-membership-services/membership/logout/" + sessionId);

                Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
                Response resp = invocationBuilder.get();
                String queryRespose = resp.readEntity(String.class);


                if (resp.getStatus() != 200)
                {
                    logger.error("Logout call to membership service failed : " + resp.getStatus());
                }
                else
                {
                    String jsonString = (String)resp.getEntity();
                    JSONObject resultObj = new JSONObject(jsonString);
                    String status = resultObj.getString("status");
                    String message = resultObj.getString("message");

                    if (!"SUCCESS".equals(status))
                    {
                        logger.error("Error logging out of membership service, status: " + status + ", message: " + message);
                    }
                }
          }

        }
        catch (Exception e)
        {
            logger.error("Error logging out of membership service", e);
        }

        // now let superclass handle the redirect
        super.handle(request, response, authentication);
    }

    /**
     * Get the current session id from the cookie in the request
     * 
     * @param request Assumed not <code>null</code>
     * 
     * @return The id, <code>null</code> if not found.
     */
    private String getSessionId(HttpServletRequest request)
    {
        String sessionId = null;
        String sessionCookieName = membershipConfig.getMembershipSessionCookieName();
        
        Cookie[] cookies = request.getCookies();
        for (int i = 0; i < cookies.length; i++)
        {
            Cookie cookie = cookies[i];
            if (sessionCookieName.equals(cookie.getName()))
            {
                sessionId = cookie.getValue();
                break;
            }
        }
        return sessionId;
    }
}
