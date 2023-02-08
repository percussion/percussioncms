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

package com.percussion.theme.service.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSDataService.DataServiceDeleteException;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;
import com.percussion.share.service.IPSDataService.DataServiceNotFoundException;
import com.percussion.share.service.IPSDataService.DataServiceSaveException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.theme.data.PSRegionCSS;
import com.percussion.theme.data.PSRegionCssList;
import com.percussion.theme.data.PSRichTextCustomStyle;
import com.percussion.theme.data.PSRichTextCustomStyleList;
import com.percussion.theme.data.PSTheme;
import com.percussion.theme.data.PSThemeSummary;
import com.percussion.theme.service.IPSThemeService;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Implementation of the {@link IPSThemeService}.
 *
 * @author YuBingChen
 */
@Path("/theme")
@Component("themeRestService")
@Lazy
public class PSThemeRestService
{
    private PSThemeService themeService;
    
    @Autowired
    public PSThemeRestService(PSThemeService themeService)
    {
        this.themeService = themeService;
    }
    
    /*
     * //see base interface method for details
     */
    @GET
    @Path("/summary/all")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSThemeSummary> findAll()
    {
      return themeService.findAll();
    }
    
    /*
     * //see base interface method for details
     */
    @GET
    @Path("/css/{name}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSTheme load(@PathParam("name") String name)
    {
        try {
            return themeService.load(name);
        } catch (DataServiceLoadException | PSValidationException | DataServiceNotFoundException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    @GET
    @Path("/create/{newTheme}/{existingTheme}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSThemeSummary create(@PathParam("newTheme") String newTheme,
            @PathParam("existingTheme") String existingTheme)
    {
        try {
            return themeService.create(newTheme, existingTheme);
        } catch (DataServiceSaveException | DataServiceNotFoundException | DataServiceLoadException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see com.percussion.theme.service.IPSThemeService#createFromDefault(java.lang.String)
     */   
    @GET
    @Path("/create/{newTheme}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSThemeSummary createFromDefault(@PathParam("newTheme") String newTheme) throws DataServiceLoadException,
            DataServiceNotFoundException, DataServiceSaveException
    {
        return themeService.createFromDefault(newTheme);
    }
    
    @DELETE
    @Path("/delete/{theme}")
    public void delete(@PathParam("theme") String theme) throws DataServiceNotFoundException,
          DataServiceDeleteException
    {
        themeService.delete(theme);
    }
    
    @GET
    @Path("/regioncss/{theme}/{templatename}/{outerregion}/{region}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSRegionCSS getRegionCSS(@PathParam("theme") String theme, @PathParam("templatename") String templatename,
            @PathParam("outerregion") String outerregion, @PathParam("region") String region)
    {
        try {
            return themeService.getRegionCSS(theme, templatename, outerregion, region);
        } catch (IPSDataService.PSThemeNotFoundException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e.getMessage());
        }
    }

    @POST
    @Path("/regioncss/{theme}/{templatename}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void saveRegionCSS(@PathParam("theme") String theme, @PathParam("templatename") String templatename,
            PSRegionCSS regionCSS)
    {
        try {
            themeService.saveRegionCSS(theme, templatename, regionCSS);
        } catch (IPSDataService.PSThemeNotFoundException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e.getMessage());
        }
    }
    @DELETE
    @Path("/regioncss/{theme}/{templatename}/{outerregion}/{region}")
    public void deleteRegionCSS(@PathParam("theme") String theme, @PathParam("templatename") String templatename,
            @PathParam("outerregion") String outerregion, @PathParam("region") String region)
    {
        try {
            themeService.deleteRegionCSS(theme, templatename, outerregion, region);
        } catch (IPSDataService.PSThemeNotFoundException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e.getMessage());
        }
    }
    @POST
    @Path("/regioncss/merge/{theme}/{templateId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void mergeRegionCSS(@PathParam("theme") String theme, @PathParam("templateId") String templateId, PSRegionCssList deletedRegions)
    {
        try {
            themeService.mergeRegionCSS(theme, templateId, deletedRegions);
        } catch (PSDataServiceException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
           throw new WebApplicationException(e.getMessage());
        }
    }
    @POST
    @Path("/regioncss/prepareForEdit/{theme}/{templatename}")
    public void prepareForEditRegionCSS(@PathParam("theme") String theme, @PathParam("templatename") String templatename)
    {
        try {
            themeService.prepareForEditRegionCSS(theme, templatename);
        } catch (IPSDataService.PSThemeNotFoundException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e.getMessage());
        }
    }
    @DELETE
    @Path("/regioncss/clearCache/{theme}/{templatename}")
    public void clearCacheRegionCSS(@PathParam("theme") String theme, @PathParam("templatename") String templatename)
    {
        themeService.clearCacheRegionCSS(theme, templatename);
    }

    
    @GET
    @Path("/customstyles")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSRichTextCustomStyle> getRichTextCustomStyles()
    {
        return new PSRichTextCustomStyleList(themeService.getRichTextCustomStyles());
    }
 
    
    /**
     * Logger for this service.
     */
    public static final Logger log = LogManager.getLogger(PSThemeRestService.class);

    


}
