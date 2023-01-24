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
package com.percussion.generickey.utils.services.impl;

import com.percussion.generickey.services.IPSGenericKeyService;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.Validate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/key")
@Component
@Scope("singleton")
public class PSGenericKeyRestService
{

    private IPSGenericKeyService genericKeyService;

    /**
     * Ctor, autowired by spring.
     * 
     * @param service The service to use, may not be <code>null</code>.
     */
    public PSGenericKeyRestService(IPSGenericKeyService service)
    {
        Validate.notNull(service);
        genericKeyService = service;
    }

    /**
     * Creates a reset key.
     * <p>
     * 
     * @url /perc-generickey-services/key/requestKey
     * @httpverb POST
     * @nullipotent yes (read-only method).
     * @secured yes (SSL and HTTP Basic Authentication).
     * @return the reset key value generated.
     * @throws WebApplicationException
     * @httpcodeonsuccess HTTP 200.
     * @httpcodeonerror HTTP 500.
     */
    @POST
    @Path("/requestKey")
    @Produces(MediaType.TEXT_PLAIN)
    public String generateKey()
    {
        String resetKey = "";
        try
        {
            resetKey = genericKeyService.generateKey(DAY_IN_MILLISECONDS);
        }
        catch (Exception e)
        {
            throw new WebApplicationException(e, Response.serverError().build());
        }
        return resetKey;
    }

    /**
     * Processes the reset key provided, and if exists, compare the reset date
     * against with the current date, to determine if the key is still valid.
     * <p>
     * 
     * @url /perc-generickey-services/key/isvalid/{key}
     * @httpverb POST
     * @nullipotent yes (read-only method).
     * @secured yes (SSL and HTTP Basic Authentication).
     * @param key the reset key to delete. Never <code>null</code>, or empty.
     * @return true if the key is valid, otherwise false.
     * @throws WebApplicationException
     * @httpcodeonsuccess HTTP 200.
     * @httpcodeonerror HTTP 500.
     */
    @POST
    @Path("/isvalid/{key}")
    @Produces(MediaType.TEXT_PLAIN)
    public String isValidKey(@PathParam("key") String key)
    {
        Boolean keyIsValid = false;
        try
        {
            keyIsValid = genericKeyService.isValidKey(key);
        }
        catch (Exception e)
        {
            throw new WebApplicationException(e, Response.serverError().build());
        }
        return keyIsValid.toString();
    }

    /**
     * 
     * Delete a reset key using the key provided. If 'key' doesn't exist, throw
     * a web application Exception.
     * 
     * @url /perc-generickey-services/key/{key}
     * @httpverb DELETE
     * @nullipotent no.
     * @secured yes (SSL and HTTP Basic Authentication).
     * @param key The reset key to delete. Never <code>null</code>, or empty.
     * @throws WebApplicationException
     * @httpcodeonsuccess HTTP 204.
     * @httpcodeonerror HTTP 500.
     */
    @DELETE
    @Path("/{key}")
    public void deleteKey(@PathParam("key") String key)
    {
        try
        {
            genericKeyService.deleteKey(key);
        }
        catch (Exception e)
        {
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    /**
     * Constant to set the duration time one day into milliseconds
     */
    private static final long DAY_IN_MILLISECONDS = 86400000;
}
