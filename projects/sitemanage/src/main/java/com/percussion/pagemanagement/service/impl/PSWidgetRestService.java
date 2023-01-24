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
package com.percussion.pagemanagement.service.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.data.PSWidgetPackageInfoRequest;
import com.percussion.pagemanagement.data.PSWidgetPackageInfoResult;
import com.percussion.pagemanagement.data.PSWidgetSummary;
import com.percussion.pagemanagement.data.PSWidgetSummaryList;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSPropertiesValidationException;
import com.percussion.share.service.exception.PSSpringValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.util.List;


@Path("/widget")
@Component("widgetRestService")
@Lazy
public class PSWidgetRestService {

    private static final Logger log = LogManager.getLogger(PSWidgetRestService.class);

    private IPSWidgetService widgetService;
  
    @Autowired
    public PSWidgetRestService(IPSWidgetService widgetService)
    {
        this.widgetService = widgetService;
    }

    
    @POST
    @Path("/validate/item")
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    public PSSpringValidationException validateWidgetItem(PSWidgetItem widgetItem)
    {
        try {
            return widgetService.validateWidgetItem(widgetItem);
        } catch (PSPropertiesValidationException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }
    

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    public PSWidgetSummary find(@PathParam("id") String id)
    {
        try {
            return widgetService.find(id);
        } catch (PSDataServiceException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }
    
    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    public List<PSWidgetSummary> findAll() {
        try {
            return new PSWidgetSummaryList(widgetService.findAll());
        } catch (PSDataServiceException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }
    
    @GET
    @Path("/type/{type:.*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    public List<PSWidgetSummary> findByType(@PathParam("type") String type, @QueryParam("filterDisabledWidgets") String filterDisabledWidgets) {
        try {
            return new PSWidgetSummaryList(widgetService.findByType(type, filterDisabledWidgets));
        } catch (PSDataServiceException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/full/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    public PSWidgetDefinition load(@PathParam("id") String id)
    {
        try {
            return widgetService.load(id);
        } catch (PSDataServiceException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @POST    
    @Path("/packageinfo")
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML})
    public PSWidgetPackageInfoResult findWidgetPackageInfo(PSWidgetPackageInfoRequest request)
    {
        try {
            return widgetService.findWidgetPackageInfo(request);
        }catch(Exception e){
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    

}
