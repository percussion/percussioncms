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

package com.percussion.theme.service.impl;

import com.percussion.share.service.IPSDataService.DataServiceDeleteException;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;
import com.percussion.share.service.IPSDataService.DataServiceNotFoundException;
import com.percussion.share.service.IPSDataService.DataServiceSaveException;
import com.percussion.theme.data.*;
import com.percussion.theme.service.IPSThemeService;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

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
        return themeService.load(name);
    }
    
    @GET
    @Path("/create/{newTheme}/{existingTheme}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSThemeSummary create(@PathParam("newTheme") String newTheme,
            @PathParam("existingTheme") String existingTheme) throws DataServiceLoadException,
            DataServiceNotFoundException, DataServiceSaveException
    {
        return themeService.create(newTheme, existingTheme);
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
        return themeService.getRegionCSS(theme, templatename, outerregion, region);
    }

    @POST
    @Path("/regioncss/{theme}/{templatename}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void saveRegionCSS(@PathParam("theme") String theme, @PathParam("templatename") String templatename,
            PSRegionCSS regionCSS)
    {
       themeService.saveRegionCSS(theme, templatename, regionCSS);
    }
    @DELETE
    @Path("/regioncss/{theme}/{templatename}/{outerregion}/{region}")
    public void deleteRegionCSS(@PathParam("theme") String theme, @PathParam("templatename") String templatename,
            @PathParam("outerregion") String outerregion, @PathParam("region") String region)
    {
       themeService.deleteRegionCSS(theme, templatename, outerregion, region);
    }
    @POST
    @Path("/regioncss/merge/{theme}/{templateId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void mergeRegionCSS(@PathParam("theme") String theme, @PathParam("templateId") String templateId, PSRegionCssList deletedRegions)
    {
        themeService.mergeRegionCSS(theme, templateId, deletedRegions);
    }
    @POST
    @Path("/regioncss/prepareForEdit/{theme}/{templatename}")
    public void prepareForEditRegionCSS(@PathParam("theme") String theme, @PathParam("templatename") String templatename)
    {
        themeService.prepareForEditRegionCSS(theme, templatename);
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
    public static Log log = LogFactory.getLog(PSThemeRestService.class);

    


}
