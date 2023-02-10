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

/**
 * 
 */
package com.percussion.membership.service.impl;

import com.percussion.delivery.client.IPSDeliveryClient.HttpMethodType;
import com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions;
import com.percussion.delivery.client.PSDeliveryClient;
import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.delivery.service.PSDeliveryInfoServiceLocator;
import com.percussion.error.PSExceptionUtils;
import com.percussion.membership.data.PSAccountSummary;
import com.percussion.membership.data.PSUserGroup;
import com.percussion.membership.data.PSUserSummaries;
import com.percussion.membership.data.PSUserSummary;
import com.percussion.membership.service.IPSMembershipService;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.services.error.PSNotFoundException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JaySeletz
 *
 */
@Path(IPSMembershipService.MEMBERSHIP)
@Component("membershipService")
@Lazy
public class PSMembershipService implements IPSMembershipService
{
    @Autowired
    @Lazy
    private IPSPubServerService pubServerService;

    @Override
    @GET
    @Path(ADMIN_USERS+"/"+"{site}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUserSummaries getUsers(@PathParam("site") String site)
    {
        try
        {
        String adminURl= pubServerService.getDefaultAdminURL(site);
        IPSDeliveryInfoService deliveryService  = PSDeliveryInfoServiceLocator.getDeliveryInfoService();
        PSDeliveryInfo server = deliveryService.findByService(PSDeliveryInfo.SERVICE_MEMBERSHIP,null,adminURl);
        if (server == null) {
            throw new WebApplicationException("Cannot find service of: " + PSDeliveryInfo.SERVICE_MEMBERSHIP);
        }
        
        String url = "/" + PSDeliveryInfo.SERVICE_MEMBERSHIP + MEMBERSHIP + ADMIN_USERS;

            List<PSUserSummary> summaries = new ArrayList<>();
            
            PSDeliveryClient deliveryClient = new PSDeliveryClient();
            JSONArray users = deliveryClient.getJsonArray(new PSDeliveryActionOptions(server, url));
            for (int i = 0; i < users.size(); i++)
            {
                JSONObject userSum = users.getJSONObject(i);
                PSUserSummary userSummary = new PSUserSummary();
                userSummary.setEmail(userSum.getString("email"));
                userSummary.setCreatedDate(userSum.getString("createdDate"));
                userSummary.setStatus(userSum.getString("status"));
                userSummary.setGroups(userSum.getString("groups"));
                summaries.add(userSummary);
            }
            
            return new PSUserSummaries(summaries);
        }
        catch (Exception e)
        {
            log.warn("Error getting all users from the membership service: Error: {}" ,  e.getMessage());
            throw new WebApplicationException(e);
        }
    }
    
    @Override
    @PUT
    @Path(ADMIN_ACCOUNT+"/"+"{site}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUserSummaries changeStateAccount(PSAccountSummary account,@PathParam("site") String site)
    {
        try
        {
            String adminURl= pubServerService.getDefaultAdminURL(site);
            IPSDeliveryInfoService deliveryService = PSDeliveryInfoServiceLocator.getDeliveryInfoService();
            PSDeliveryInfo server = deliveryService.findByService(PSDeliveryInfo.SERVICE_MEMBERSHIP,null,adminURl);
            if (server == null) {
                throw new WebApplicationException("Cannot find service of: " + PSDeliveryInfo.SERVICE_MEMBERSHIP);
            }

        String url = "/" + PSDeliveryInfo.SERVICE_MEMBERSHIP + MEMBERSHIP + ADMIN_ACCOUNT;

            PSDeliveryClient deliveryClient = new PSDeliveryClient();
            JSONObject accountJson = new JSONObject();
            accountJson.put("email", account.getEmail());
            accountJson.put("action", account.getAction());
            deliveryClient.push(new PSDeliveryActionOptions(server, url, HttpMethodType.PUT, true), 
                    accountJson.toString());
            
            return getUsers(site);
        }
        catch (Exception e)
        {
            log.warn("Error changing membership account type: {}" ,PSExceptionUtils.getMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }
    
    @Override
    @DELETE
    @Path(ADMIN_ACCOUNT + "/{email:.*}"+"/"+"{site}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUserSummaries deleteAccount(@PathParam("email") String email,@PathParam("site") String site)
    {
        try
        {
            String adminURl= pubServerService.getDefaultAdminURL(site);
            IPSDeliveryInfoService deliveryService = PSDeliveryInfoServiceLocator.getDeliveryInfoService();
            PSDeliveryInfo server = deliveryService.findByService(PSDeliveryInfo.SERVICE_MEMBERSHIP,null,adminURl);
            if (server == null) {
                throw new WebApplicationException("Cannot find service of: " + PSDeliveryInfo.SERVICE_MEMBERSHIP);
            }

            String url = "/" + PSDeliveryInfo.SERVICE_MEMBERSHIP + MEMBERSHIP + ADMIN_ACCOUNT + "/" + email;

            PSDeliveryClient deliveryClient = new PSDeliveryClient();
            deliveryClient.push(new PSDeliveryActionOptions(server, url, HttpMethodType.DELETE, true), "");
            
            return getUsers(site);
        } catch (IPSPubServerService.PSPubServerServiceException | PSNotFoundException e) {
            log.warn("Error deleting user(s) from the membership service.  Error: {}",  e.getMessage());
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }

    }
    
    @Override
    @PUT
    @Path(ADMIN_USER_GROUP+"/"+"{site}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUserSummaries updateGroupAccount(PSUserGroup userGroup,@PathParam("site") String site)
    {
        try {

            String adminURl= pubServerService.getDefaultAdminURL(site);
            IPSDeliveryInfoService deliveryService = PSDeliveryInfoServiceLocator.getDeliveryInfoService();
            PSDeliveryInfo server = deliveryService.findByService(PSDeliveryInfo.SERVICE_MEMBERSHIP,null,adminURl);
            if (server == null) {
                throw new WebApplicationException("Cannot find service of: " + PSDeliveryInfo.SERVICE_MEMBERSHIP);
            }

            String url = "/" + PSDeliveryInfo.SERVICE_MEMBERSHIP + MEMBERSHIP + ADMIN_USER_GROUP+"/"+site;

            PSDeliveryClient deliveryClient = new PSDeliveryClient();
            JSONObject accountJson = new JSONObject();
            accountJson.put("email", userGroup.getEmail());
            accountJson.put("groups", userGroup.getGroups());
            deliveryClient.push(new PSDeliveryActionOptions(server, url, HttpMethodType.PUT, true), 
                    accountJson.toString());
            
            return getUsers(site);
        } catch (IPSPubServerService.PSPubServerServiceException | PSNotFoundException e) {
            log.warn("Error updating group account.  Error: {}",  e.getMessage());
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }
    
    /**
     * Logger for this service.
     */
    public static final Logger log = LogManager.getLogger(PSMembershipService.class);

}
