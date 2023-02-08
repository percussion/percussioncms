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
            logger.error("Error logging out of membership service {}", e);
            logger.debug(e.getMessage(), e);
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
