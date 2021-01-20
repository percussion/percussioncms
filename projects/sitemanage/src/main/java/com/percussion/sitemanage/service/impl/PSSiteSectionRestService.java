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

package com.percussion.sitemanage.service.impl;

import static com.percussion.share.web.service.PSRestServicePathConstants.DELETE_PATH;
import static com.percussion.share.web.service.PSRestServicePathConstants.ID_PATH_PARAM;
import static com.percussion.share.web.service.PSRestServicePathConstants.LOAD_PATH;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.share.data.PSNoContent;
import com.percussion.sitemanage.data.*;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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

    /**
     * The base SiteSectionService
     */
    private PSSiteSectionService siteSectionService;

   
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
    public PSSiteSection create(PSCreateSiteSection req)
    {
        return siteSectionService.create(req);

    }

    /*
     * see base interface method for details
     */
    @POST
    @Path("/createExternalLinkSection")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection createExternalLinkSection(PSCreateExternalLinkSection req)
    {
        return siteSectionService.createExternalLinkSection(req);
    }

    /*
     * see base interface method for details
     */
    @GET
    @Path("/createSectionLink/{targetSectionGuid}/{parentSectionGuid}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection createSectionLink(@PathParam("targetSectionGuid") String targetSectionGuid,
            @PathParam("parentSectionGuid") String parentSectionGuid)
    {
       
        return siteSectionService.createSectionLink(targetSectionGuid, parentSectionGuid);
    }
    
    
    @POST
    @Path("/createSectionFromFolder")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection createSectionFromFolder(PSCreateSectionFromFolderRequest req)
    {
        
        return siteSectionService.createSectionFromFolder(req);
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
            PSCreateExternalLinkSection req)
    {
       
        return siteSectionService.updateExternalLink(sectionGuid, req);
    }

    
    @POST
    @Path("/updateSectionLink")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection updateSectionLink(PSUpdateSectionLink req)
    {
        return siteSectionService.updateSectionLink(req);
    }
    

    
    @GET
    @Path("/blogs/{siteName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
    public List<PSSiteBlogProperties> getBlogsForSite(@PathParam("siteName")String siteName)
    {
        return new PSSiteBlogPropertiesList(siteSectionService.getBlogsForSite(siteName));
    }
    
    
    @GET
    @Path("/allBlogs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSSiteBlogProperties> getAllBlogs()
    {
        return new PSSiteBlogPropertiesList(siteSectionService.getAllBlogs());
    }
  
    @GET
    @Path("/blogPosts/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteBlogPosts getBlogPosts(@PathParam("id") String id)
    {
        return siteSectionService.getBlogPosts(id);
    }

    @GET
    @Path("/properties/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSectionProperties getSectionProperties(@PathParam("id") String id)
    {
       
        return siteSectionService.getSectionProperties(id);
    }

    /*
     * see base interface method for details
     */
    @POST
    @Path("/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection update(PSSiteSectionProperties req)
    {
        return siteSectionService.update(req);
    }

    
    /*
     * //see base interface method for details
     */
    @GET
    @Path("/tree/{siteName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSectionNode loadTree(@PathParam("siteName") String siteName)
    {
        return siteSectionService.loadTree(siteName);
    }
   
    @GET
    @Path("/root/{siteName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection loadRoot(@PathParam("siteName") String siteName)
    {
        return siteSectionService.loadRoot(siteName);
    }
    
    
    @GET
    @Path(LOAD_PATH)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection load(@PathParam("id") String sId)
    {
        return siteSectionService.load(sId);
    }

    /*
     * //see base interface method for details
     */
    @DELETE
    @Path(DELETE_PATH)
    public void delete(@PathParam(ID_PATH_PARAM) String id)
    {
        siteSectionService.delete(id);
    }
    
    @DELETE
    @Path("/convertToFolder/{id}")
    public void convertToFolder(@PathParam("id") String id) {
        siteSectionService.convertToFolder(id);
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
    public PSReplaceLandingPage replaceLandingPage(PSReplaceLandingPage request)
    {
        return siteSectionService.replaceLandingPage(request);
    }
    
    @POST
    @Path("/move")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSection move(PSMoveSiteSection req)
    {
        return siteSectionService.move(req);
    }

}
