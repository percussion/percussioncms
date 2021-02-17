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
package com.percussion.pagemanagement.service.impl;

import com.percussion.pagemanagement.data.PSHtmlMetadata;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSTemplateSummaryList;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.share.service.IPSDataService.DataServiceSaveException;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSDataServiceException;
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
        return templateService.createTemplate(name, srcId);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") String id)
    {
        templateService.delete(id);
    }
    
    @GET
    @Path("/summary/all")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSTemplateSummary> findAll() {
        return new PSTemplateSummaryList(templateService.findAll());
    }

    @GET
    @Path("/summary/all/{siteName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSTemplateSummary> findAll(@PathParam("siteName") String siteName) {
        return new PSTemplateSummaryList(templateService.findAll(siteName));
    }

    @GET
    @Path("/summary/all/user")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSTemplateSummary> findAllUserTemplates() {
        return new PSTemplateSummaryList(templateService.findAllUserTemplates());
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
        return templateService.find(id);
    }

    @GET
    @Path("/loadTemplateMetadata/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSHtmlMetadata loadHtmlMetadata(@PathParam("id") String id) {
        return templateService.loadHtmlMetadata(id);
    }
    
    @POST
    @Path("/saveTemplateMetadata")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void saveHtmlMetadata(PSHtmlMetadata object) {
        templateService.saveHtmlMetadata(object);
    }
    
    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSTemplate load(@PathParam("id") String id) 
    {
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
    public PSTemplate save(PSTemplate object, @PathParam("id") String pageId) throws PSBeanValidationException, DataServiceSaveException
    {
        return templateService.save(object, null, pageId);
    }

    @POST
    @Path(PSRestServicePathConstants.VALIDATE_PATH)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSValidationErrors validate(PSTemplate object)
    {
        try {
            return templateService.validate(object);
        } catch (PSValidationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }
    }

}
