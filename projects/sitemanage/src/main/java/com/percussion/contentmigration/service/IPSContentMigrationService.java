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

package com.percussion.contentmigration.service;

import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.share.service.exception.PSValidationException;

import java.util.List;

/**
 * Service for migrating content from an unassigned page to a template or from one template to another template or for pages within the template.
 *
 */
public interface IPSContentMigrationService
{
    /**
     * Finds all applicable widgets based on the template and reference page if exists and runs content matching rules to find the content, then
     * runs content converters to convert the content in to fields and then creates local content and associates the content to the target page.
     * Applicable widgets are the widgets that have registered converter and is the only widget under a region.
     * Content migration is skipped for the pages that have been checked out by someone else, if the page has been checked out to the current user
     * then it will be left in checked out state, if not the page will be checked out and updated and checked back in. If the page is in non-editable
     * state (like pending or live) then the page is moved to an editable state and checked in but left in that editable state.
     *  
     * @param siteName The name of the site, if not blank, removes the pages from unassigned queue after the pages have been migrated.
     * @param templateId The is of the template that is used as basis for migrating content, must not be <code>null</code>, throws PSContentMigrationException
     * if supplied id is not a valid template id.
     * @param referencePageId The id of the reference page if not blank, the rendered page is used in the rules to better match the content
     * if blank then assembled template is used.  If the supplied id is not a valid page id, then the error is logged and the page id is ignored.
     * @param newPageIds Must not be empty, migrates content for all valid pages that are modifiable by the current user, collects all invalid pages and 
     * pages that are not modifiable by the current user (checked out to someone else) and sets them as failed items on PSContentMigrationException and throws it.
     *   
     * @throws PSContentMigrationException in case of an error.
     */
    void migrateContent(String siteName, String templateId, String referencePageId, List<String> newPageIds) throws PSContentMigrationException, PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException;
    
    /**
     * Migrates content on template change, all the content that can be matched by widget name will be automatically matched and for rest of the empty widgets
     * runs the content matching rules on un-assigned assets.
     * @param templateId must not be blank.
     * @param referencePageId may be blank.
     * @param newPageIds must not be empty.
     * @throws PSContentMigrationException in case of error.
     */
    void migrateContentOnTemplateChange(String templateId, String referencePageId, List<String> newPageIds) throws PSContentMigrationException, PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException;

    /**
     * Migrates same template changes to the other pages of the template, if page content migration version matches with the template content migration
     * version then skips the content migration process for that page.
     * @param templateId must not be blank.
     * @param pageIds if <code>null</code> migrates content for all pages using the supplied template, if not migrates content only for the pages in the list.
     */
    void migrateSameTemplateChanges(String templateId, List<String> pageIds) throws PSContentMigrationException, PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException;
    
    /**
     * Gets all the pages using the supplied template.
     * @param templateId  must not be <code>null</code>
     * @return List of page guid strings never <code>null</code> may be empty.
     */
    List<String> getTemplatePages(String templateId) throws IPSPageService.PSPageException;
    
    
}
