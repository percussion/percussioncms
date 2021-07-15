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

package com.percussion.contentmigration.service.impl;

import com.percussion.contentmigration.service.IPSContentMigrationService;
import com.percussion.contentmigration.service.PSContentMigrationException;
import com.percussion.contentmigration.service.impl.PSMigrateContentResponse.PSMigrateResponseStatus;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/contentmigration")
@Component("contentMigrationRestService")
public class PSContentMigrationRestService
{
    private IPSContentMigrationService migrationService;
    
    @Autowired
    public PSContentMigrationRestService(IPSContentMigrationService migrationService)
    {
        this.migrationService = migrationService;
    }
    
    @POST
    @Path("/migrate")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSMigrateContentResponse migrateContent(PSMigrateContentRestData data)
    {
        return migrateContent(data.getSourceType(),data.getTemplateId(), data.getRefPageId(), data.getPageIds(),data.getSiteName());
    }
    
    @POST
    @Path("/migrate/{templateId}/{pageId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSMigrateContentResponse migrateSameTemplateChanges(@PathParam("templateId") String templateId, @PathParam("pageId") String pageId)
    {
        PSMigrateContentResponse response = new PSMigrateContentResponse();
        try
        {
            List<String> pageIds = null;
            if(pageId.equalsIgnoreCase("ALL"))
            {
                pageIds = migrationService.getTemplatePages(templateId);
            }
            else
            {
                pageIds = new ArrayList<>();
                pageIds.add(pageId);
            }
            response = migrateContent("SAMETEMPLATE", templateId, null, pageIds, null);

        }
        catch(Exception e)
        {
            response.setStatus(PSMigrateResponseStatus.ERROR);
            response.setMessage("Unexpected error occurred while migrating the content. Please see log for more details.");
        }
        return response;
    }
    
    private PSMigrateContentResponse migrateContent(String type, String templateId, String refPageId, List<String> pageIds, String siteName)
    {
        PSMigrateContentResponse response = new PSMigrateContentResponse();
        try
        {
            if(type.equals("UNASSIGNED"))
            {
                migrationService.migrateContent(siteName, templateId, refPageId, pageIds);
            }
            else if(type.equals("TEMPLATE"))
            {
                migrationService.migrateContentOnTemplateChange(templateId, refPageId, pageIds);
            }
            else if(type.equals("SAMETEMPLATE"))
            {
                migrationService.migrateSameTemplateChanges(templateId, pageIds);
            }
            response.setStatus(PSMigrateResponseStatus.SUCCESS);
            response.setMessage("Successfully migrated content for the supplied pages.");
        }
        catch(PSContentMigrationException cme)
        {
            response.setStatus(PSMigrateResponseStatus.ERROR);
            if(cme.getFailedItems().size()==1)
            {
                response.setMessage(cme.getFailedItems().get(cme.getFailedItems().keySet().iterator().next()));
            }
            else
            {
                response.setMessage("Failed to migrate content.");
                response.setErrors(cme.getFailedItems());
            }
        }
        catch(Exception e)
        {
            response.setStatus(PSMigrateResponseStatus.ERROR);
            response.setMessage("Unexpected error occurred while migrating the content. Please see log for more details.");
        }
        return response;
        
    }
    
}
