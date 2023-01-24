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
