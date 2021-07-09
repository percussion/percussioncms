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

package com.percussion.sitemanage.service.impl;

import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSCreateExternalLinkSection;
import com.percussion.sitemanage.data.PSCreateSectionFromFolderRequest;
import com.percussion.sitemanage.data.PSCreateSiteSection;
import com.percussion.sitemanage.data.PSMoveSiteSection;
import com.percussion.sitemanage.data.PSReplaceLandingPage;
import com.percussion.sitemanage.data.PSSectionNode;
import com.percussion.sitemanage.data.PSSiteBlogPosts;
import com.percussion.sitemanage.data.PSSiteBlogProperties;
import com.percussion.sitemanage.data.PSSiteBlogPropertiesList;
import com.percussion.sitemanage.data.PSSiteSection;
import com.percussion.sitemanage.data.PSSiteSectionList;
import com.percussion.sitemanage.data.PSSiteSectionProperties;
import com.percussion.sitemanage.data.PSUpdateSectionLink;
import com.percussion.sitemanage.service.IPSSiteSectionService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.percussion.share.web.service.PSRestServicePathConstants.DELETE_PATH;
import static com.percussion.share.web.service.PSRestServicePathConstants.ID_PATH_PARAM;
import static com.percussion.share.web.service.PSRestServicePathConstants.LOAD_PATH;
import static org.apache.commons.lang.Validate.notNull;

/**
 * The actual implementation of the CRUD service for site sections.
 * 
 * @author YuBingChen
 */
 
@Path("/section")
@Component("siteSectionRestService")
@Lazy
public class PSSiteSectionRestService 
{
    private static final Logger log = LogManager.getLogger(PSSiteSectionRestService.class);

    /**
     * The base SiteSectionService
     */
    private final PSSiteSectionService siteSectionService;

   
    @Autowired
    public PSSiteSectionRestService(PSSiteSectionService siteSectionService)
    {
        notNull(siteSectionService);
      
        this.siteSectionService = siteSectionService;
        
    }
    
    /*
     * see base interface method for details
     */
    @POST
    @Path("/create")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection create(PSCreateSiteSection req) throws PSDataServiceException
    {
        try {
            return siteSectionService.create(req);
        } catch (PSDataServiceException e) {
            log.debug(e.getMessage(),e);
            throw (e);
        }

    }

    /*
     * see base interface method for details
     */
    @POST
    @Path("/createExternalLinkSection")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection createExternalLinkSection(PSCreateExternalLinkSection req) throws IPSSiteSectionService.PSSiteSectionException, PSValidationException, IPSPathService.PSPathNotFoundServiceException {
        try {
            return siteSectionService.createExternalLinkSection(req);
        } catch (IPSSiteSectionService.PSSiteSectionException | PSValidationException | IPSPathService.PSPathNotFoundServiceException e) {
            log.debug(e.getMessage(),e);
            throw (e);
        }
    }

    /*
     * see base interface method for details
     */
    @GET
    @Path("/createSectionLink/{targetSectionGuid}/{parentSectionGuid}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection createSectionLink(@PathParam("targetSectionGuid") String targetSectionGuid,
            @PathParam("parentSectionGuid") String parentSectionGuid) throws IPSSiteSectionService.PSSiteSectionException {
       try {
           return siteSectionService.createSectionLink(targetSectionGuid, parentSectionGuid);
       } catch (IPSSiteSectionService.PSSiteSectionException e) {
           log.debug(e.getMessage(),e);
           throw (e);
       }
    }
    
    
    @POST
    @Path("/createSectionFromFolder")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection createSectionFromFolder(PSCreateSectionFromFolderRequest req) throws PSDataServiceException {
        try {
            return siteSectionService.createSectionFromFolder(req);
        } catch (PSDataServiceException e) {
            log.debug(e.getMessage(),e);
            throw (e);
        }
    }


    
    @GET
    @Path("/deleteSectionLink/{sectionGuid}/{parentSectionGuid}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSNoContent deleteSectionLink(@PathParam("sectionGuid") String sectionGuid,
            @PathParam("parentSectionGuid") String parentSectionGuid)
    {
        notNull(sectionGuid);
        notNull(parentSectionGuid);

        return siteSectionService.deleteSectionLink(sectionGuid, parentSectionGuid);
    }

    
    @POST
    @Path("/updateExternalLink/{sectionGuid}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection updateExternalLink(@PathParam("sectionGuid") String sectionGuid,
            PSCreateExternalLinkSection req) throws IPSSiteSectionService.PSSiteSectionException, PSValidationException {
       try {
           return siteSectionService.updateExternalLink(sectionGuid, req);
       } catch (IPSSiteSectionService.PSSiteSectionException | PSValidationException e) {
           log.debug(e.getMessage(),e);
           throw (e);
       }
    }

    
    @POST
    @Path("/updateSectionLink")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection updateSectionLink(PSUpdateSectionLink req) throws IPSSiteSectionService.PSSiteSectionException {
        try {
            return siteSectionService.updateSectionLink(req);
        } catch (IPSSiteSectionService.PSSiteSectionException e) {
            log.debug(e.getMessage(),e);
            throw (e);
        }
    }
    

    
    @GET
    @Path("/blogs/{siteName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
    public List<PSSiteBlogProperties> getBlogsForSite(@PathParam("siteName")String siteName) throws PSValidationException {
        try {
            return new PSSiteBlogPropertiesList(siteSectionService.getBlogsForSite(siteName));
        } catch (PSValidationException e) {
            log.debug(e.getMessage(),e);
            throw (e);
        }
    }
    
    
    @GET
    @Path("/allBlogs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSSiteBlogProperties> getAllBlogs() throws PSDataServiceException {
        try {
            return new PSSiteBlogPropertiesList(siteSectionService.getAllBlogs());
        } catch (PSDataServiceException e) {
            log.debug(e.getMessage(),e);
            throw (e);
        }
    }
  
    @GET
    @Path("/blogPosts/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteBlogPosts getBlogPosts(@PathParam("id") String id) throws IPSSiteSectionService.PSSiteSectionException, PSValidationException {
        try {
            return siteSectionService.getBlogPosts(id);
        } catch (PSValidationException | IPSSiteSectionService.PSSiteSectionException e) {
            log.debug(e.getMessage(),e);
            throw (e);
        }
    }

    @GET
    @Path("/properties/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSectionProperties getSectionProperties(@PathParam("id") String id) throws IPSSiteSectionService.PSSiteSectionException {
       try {
           return siteSectionService.getSectionProperties(id);
       } catch (IPSSiteSectionService.PSSiteSectionException e) {
           log.debug(e.getMessage(),e);
           throw (e);
       }
    }

    /*
     * see base interface method for details
     */
    @POST
    @Path("/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection update(PSSiteSectionProperties req) throws PSDataServiceException {
        try {
            return siteSectionService.update(req);
        } catch (PSDataServiceException e) {
            log.debug(e.getMessage(),e);
            throw (e);
        }
    }

    
    /*
     * //see base interface method for details
     */
    @GET
    @Path("/tree/{siteName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSectionNode loadTree(@PathParam("siteName") String siteName) throws IPSSiteSectionService.PSSiteSectionException, PSNotFoundException {
        try {
            return siteSectionService.loadTree(siteName);
        } catch (IPSSiteSectionService.PSSiteSectionException | PSNotFoundException e) {
            log.debug(e.getMessage(),e);
            throw (e);
        }
    }
   
    @GET
    @Path("/root/{siteName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection loadRoot(@PathParam("siteName") String siteName) throws IPSSiteSectionService.PSSiteSectionException, PSNotFoundException {
        try {
            return siteSectionService.loadRoot(siteName);
        } catch (IPSSiteSectionService.PSSiteSectionException | PSNotFoundException e) {
            log.debug(e.getMessage(),e);
            throw (e);
        }
    }
    
    
    @GET
    @Path(LOAD_PATH)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection load(@PathParam(value = "id") String id) throws IPSSiteSectionService.PSSiteSectionException
    {
        try {
            return siteSectionService.load(id);
        } catch (IPSSiteSectionService.PSSiteSectionException e) {
            log.debug(e.getMessage(),e);
            throw (e);
        }
    }

    /*
     * //see base interface method for details
     */
    @DELETE
    @Path(DELETE_PATH)
    public void delete(@PathParam(ID_PATH_PARAM) String id) throws IPSDataService.DataServiceSaveException, PSValidationException {
        try {
            siteSectionService.delete(id);
        } catch (PSValidationException | IPSDataService.DataServiceSaveException e) {
            log.debug(e.getMessage(),e);
            throw (e);
        }
    }
    
    @DELETE
    @Path("/convertToFolder/{id}")
    public void convertToFolder(@PathParam("id") String id) throws PSValidationException{
        try {
            siteSectionService.convertToFolder(id);
        } catch (PSValidationException e) {
            log.debug(e.getMessage(),e);
            throw (e);
        }
    }
    
    @POST
    @Path("/childSections")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSSiteSection> loadChildSections(PSSiteSection section)
    {
        return new PSSiteSectionList(siteSectionService.loadChildSections(section));
    }
    
    @POST
    @Path("/replaceLandingPage")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSReplaceLandingPage replaceLandingPage(PSReplaceLandingPage request) throws PSDataServiceException {
        try {
            return siteSectionService.replaceLandingPage(request);
        } catch (PSDataServiceException e) {
            log.debug(e.getMessage(),e);
            throw (e);
        }
    }
    
    @POST
    @Path("/move")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection move(PSMoveSiteSection req) throws IPSSiteSectionService.PSSiteSectionException, PSValidationException {
        try {
            return siteSectionService.move(req);
        } catch (PSValidationException | IPSSiteSectionService.PSSiteSectionException e) {
            log.debug(e.getMessage(),e);
            throw (e);
        }
    }

}
