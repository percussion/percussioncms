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
import com.percussion.pagemanagement.data.PSHtmlMetadata;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSTemplateSummaryList;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.IPSDataService.DataServiceSaveException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSParametersValidationException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.share.web.service.PSRestServicePathConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.isTrue;


@Path("/template")
@Component("templateRestService")
public class PSTemplateRestService {
    
    private IPSTemplateService templateService;
    private static final Logger log = LogManager.getLogger(PSTemplateRestService.class);

    @Autowired
    public PSTemplateRestService(IPSTemplateService templateService) {
        super();
        this.templateService = templateService;
    }

    @GET
    @Path("/create/{name}/{srcId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSTemplateSummary createTemplate(@PathParam("name") String name, @PathParam("srcId") String srcId) 
    {
        try {
            return templateService.createTemplate(name, srcId);
        } catch (PSDataServiceException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") String id) throws PSParametersValidationException
    {
        try {
            templateService.delete(id);
        } catch (PSParametersValidationException pve){
            log.debug(pve.getMessage(),pve);
            throw pve;
        } catch (PSNotFoundException | PSDataServiceException e) {
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }
    
    @GET
    @Path("/summary/all")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSTemplateSummary> findAll() {
        try {
            return new PSTemplateSummaryList(templateService.findAll());
        } catch (IPSTemplateService.PSTemplateException | IPSGenericDao.LoadException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/summary/all/{siteName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSTemplateSummary> findAll(@PathParam("siteName") String siteName) {
        try {
            return new PSTemplateSummaryList(templateService.findAll(siteName));
        } catch (IPSTemplateService.PSTemplateException | IPSGenericDao.LoadException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/summary/all/user")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSTemplateSummary> findAllUserTemplates() {
        try {
            return new PSTemplateSummaryList(templateService.findAllUserTemplates());
        } catch (IPSTemplateService.PSTemplateException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/summary/all/readonly")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSTemplateSummary> findReadOnlyTemplates(@QueryParam("type") String type) {
    	String baseType = null;
    	if(StringUtils.isBlank(type))
    		baseType = "base";
    	else
    		baseType = type;
    	return new PSTemplateSummaryList(templateService.findBaseTemplates(baseType));
    }

    @GET
    @Path("/summary/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSTemplateSummary findTemplate(@PathParam("id") String id) {
        try {
            return templateService.find(id);
        } catch (PSDataServiceException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/loadTemplateMetadata/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSHtmlMetadata loadHtmlMetadata(@PathParam("id") String id) {
        try {
            return templateService.loadHtmlMetadata(id);
        } catch (PSDataServiceException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }
    
    @POST
    @Path("/saveTemplateMetadata")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void saveHtmlMetadata(PSHtmlMetadata object) {
        try {
            templateService.saveHtmlMetadata(object);
        } catch (PSDataServiceException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/templateEditUrl/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPageEditUrl(@PathParam("id") String id)
    {
        isTrue(isNotBlank(id), "id may not be blank");

        return templateService.getTemplateEditUrl(id);
    }

    
    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSTemplate load(@PathParam("id") String id)
    {
        try {
            PSTemplate template = templateService.load(id);

            //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
            // The following code is to make sure always inject XML element
            // for NULL valued properties. This is to insure easy update the XML
            // content with JavaScript.
            // We may not to do this if the client side is processing JSON object
            // in the future.
            if (template.getHtmlHeader() == null)
                template.setHtmlHeader("");
            if (template.getDescription() == null)
                template.setDescription("");
            if (template.getImageThumbPath() == null)
                template.setImageThumbPath("");
            if (template.getLabel() == null)
                template.setLabel("");
            if (template.getTheme() == null)
                template.setTheme("");
            if (template.getCssOverride() == null)
                template.setCssOverride("");
            if (template.getCssRegion() == null)
                template.setCssRegion("");

            return template;
        } catch (PSDataServiceException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }
       
    @POST
    @Path(PSRestServicePathConstants.SAVE_PATH)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSTemplate save(PSTemplate object)
    {
        try {
            return templateService.save(object);
        } catch (PSDataServiceException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    @POST
    @Path("/page/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSTemplate save(PSTemplate object, @PathParam("id") String pageId)
    {
        try {
            return templateService.save(object, null, pageId);
        } catch (PSDataServiceException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path(PSRestServicePathConstants.VALIDATE_PATH)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSValidationErrors validate(PSTemplate object)
    {
        try {
            return templateService.validate(object);
        } catch (PSValidationException | DataServiceSaveException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e.getMessage());
        }
    }

}
