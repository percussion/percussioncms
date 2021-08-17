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
package com.percussion.pagemanagement.service.impl;

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
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
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
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
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
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
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
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
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
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
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
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
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
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
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
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
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
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
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
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
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
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
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
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }
    }

}
