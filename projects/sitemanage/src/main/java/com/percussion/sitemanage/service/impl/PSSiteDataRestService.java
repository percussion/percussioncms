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

import com.percussion.foldermanagement.service.IPSFolderService;
import com.percussion.itemmanagement.service.IPSItemService;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.PSEnumVals;
import com.percussion.share.data.PSMapWrapper;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteCopyRequest;
import com.percussion.sitemanage.data.PSSiteProperties;
import com.percussion.sitemanage.data.PSSitePublishProperties;
import com.percussion.sitemanage.data.PSSiteStatisticsSummary;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.data.PSSiteSummaryList;
import com.percussion.sitemanage.data.PSValidateCopyFoldersRequest;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.service.IPSSiteSectionService;
import com.percussion.util.PSSiteManageBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import static com.percussion.share.web.service.PSRestServicePathConstants.DELETE_PATH;
import static com.percussion.share.web.service.PSRestServicePathConstants.FIND_ALL_PATH;
import static com.percussion.share.web.service.PSRestServicePathConstants.FIND_PATH;
import static com.percussion.share.web.service.PSRestServicePathConstants.GET_IMPORTED_SITE_PATH;
import static com.percussion.share.web.service.PSRestServicePathConstants.ID_PATH_PARAM;
import static com.percussion.share.web.service.PSRestServicePathConstants.IMPORT_SITE_FROM_URL_PATH;
import static com.percussion.share.web.service.PSRestServicePathConstants.IMPORT_SITE_FROM_URL_PATH_ASYNC;
import static com.percussion.share.web.service.PSRestServicePathConstants.JOB_ID_PARAM;
import static com.percussion.share.web.service.PSRestServicePathConstants.LOAD_PATH;
import static com.percussion.share.web.service.PSRestServicePathConstants.SAVE_PATH;
import static com.percussion.share.web.service.PSRestServicePathConstants.VALIDATE_PATH;

@Path("/site")
@PSSiteManageBean("siteDataRestService")
public class PSSiteDataRestService 
{
    private static final Logger log = LogManager.getLogger(PSSiteDataRestService.class);

    private final PSSiteDataService siteDataService;
    
    @Autowired
    public PSSiteDataRestService(PSSiteDataService siteDataService)
    {
        this.siteDataService = siteDataService;
    }

    @GET
    @Path(LOAD_PATH)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSite load(@PathParam(ID_PATH_PARAM)
    String id) throws DataServiceLoadException
    {
        try {
            return siteDataService.load(id);
        } catch (IPSDataService.DataServiceNotFoundException | PSValidationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(404);
        }
    }

    @GET
    @Path(FIND_PATH)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteSummary find(@PathParam(ID_PATH_PARAM)
    String id) throws com.percussion.share.service.IPSDataService.DataServiceLoadException
    {
        try {
            return siteDataService.find(id);
        } catch (PSValidationException | IPSGenericDao.LoadException e) {
            throw new WebApplicationException(e);
        }
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path(FIND_ALL_PATH)
    public PSSiteSummaryList findAll(@QueryParam("includePubInfo") boolean includePubInfo)
    {
        return new PSSiteSummaryList(siteDataService.findAll(includePubInfo));
    }

    @DELETE
    @Path(DELETE_PATH)
    public void delete(@PathParam(ID_PATH_PARAM)
    String id)
    {
        try {
            siteDataService.delete(id);
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
           throw new WebApplicationException(e.getMessage());
        }
    }

    @POST
    @Path(SAVE_PATH)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSite save(PSSite site)
    {
        try {
            return siteDataService.save(site);
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }
    }

    @POST
    @Path(IMPORT_SITE_FROM_URL_PATH)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSite createSiteFromUrl(@Context
    HttpServletRequest request, PSSite site) throws PSSiteImportException
    {
        try {
            return siteDataService.createSiteFromUrl(request, site);
        } catch (PSValidationException e) {
            throw new WebApplicationException(e);
        }
    }


    @POST
    @Path(IMPORT_SITE_FROM_URL_PATH_ASYNC)
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public long createSiteFromUrlAsync(@Context
    HttpServletRequest request, PSSite site)
    {
        try {
            return siteDataService.createSiteFromUrlAsync(request, site);
        } catch (PSValidationException | IPSFolderService.PSWorkflowNotFoundException e) {
            throw new WebApplicationException(e);
        }
    }


    @GET
    @Path(GET_IMPORTED_SITE_PATH)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSite getImportedSite(@PathParam(JOB_ID_PARAM)
    Long jobId)
    {
        return siteDataService.getImportedSite(jobId);
    }


    @POST
    @Path(VALIDATE_PATH)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSValidationErrors validate(PSSite site)
    {
        try {
            return siteDataService.validate(site);
        } catch (PSValidationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }
    }

    @GET
    @Path("/properties/{siteName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteProperties getSiteProperties(@PathParam("siteName")
    String siteName)
    {
        try {
            return siteDataService.getSiteProperties(siteName);
        } catch (IPSSiteSectionService.PSSiteSectionException | PSValidationException | PSNotFoundException e) {
           throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/updateProperties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteProperties updateSiteProperties(PSSiteProperties props)
    {
        try {
            return siteDataService.updateSiteProperties(props);
        } catch (PSNotFoundException | PSDataServiceException e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/publishProperties/{siteName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSitePublishProperties getSitePublishProperties(@PathParam("siteName")
    String siteName)
    {
        try {
            return siteDataService.getSitePublishProperties(siteName);
        } catch (PSValidationException | PSNotFoundException e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/updatePublishProperties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSitePublishProperties updateSitePublishProperties(PSSitePublishProperties publishProps)
    {
        try {
            return siteDataService.updateSitePublishProperties(publishProps);
        } catch (IPSDataService.DataServiceSaveException | PSNotFoundException e) {
            throw new WebApplicationException(e);
        }
    }


    @GET
    @Path("/choices")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSEnumVals getChoices()
    {
        return siteDataService.getChoices();
    }

    @GET
    @Path("/copysiteinfo")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSMapWrapper getCopySiteInfo()
    {
        return siteDataService.getCopySiteInfo();
    }



    @GET
    @Path("/statistics/{siteId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSiteStatisticsSummary getSiteStatistics(@PathParam("siteId")
    String siteId)
    {
        try {
            return siteDataService.getSiteStatistics(siteId);
        } catch (PSDataServiceException e) {
            throw new WebApplicationException(e.getMessage());
        }
    }


    @GET
    @Path("/sass/sitenames")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    public PSMapWrapper getSaaSSiteNames(@QueryParam("filterUsedSites") boolean filterUsedSites)
    {
        try {
            return siteDataService.getSaaSSiteNames(filterUsedSites);
        } catch (DataServiceLoadException e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/isSiteImporting/{sitename}")
    @Produces(MediaType.TEXT_PLAIN)
    public String isSiteBeingImported(@PathParam("sitename")
    String sitename)
    {
        try {
            return siteDataService.isSiteBeingImported(sitename);
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }
    }
    @POST
    @Path("/validateFolders")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void  validateFolders(PSValidateCopyFoldersRequest req)
    {
        try {
            siteDataService.validateFolders(req);
        } catch (PSValidationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    @POST
    @Path("/copy")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSSite copy(PSSiteCopyRequest req)
    {
        try {
            return siteDataService.copy(req);
        } catch (IPSItemService.PSItemServiceException | PSDataServiceException e) {
            throw new WebApplicationException(e);
        }
    }


}
