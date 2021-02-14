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
package com.percussion.sitemanage.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import com.percussion.pagemanagement.data.PSTemplate.PSTemplateTypeEnum;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.sitemanage.data.PSSiteSummary;


/**
 * 
 * Manages the site -> template assocations.
 * @author adamgent
 *
 */
public interface IPSSiteTemplateService
{
    String generateNewTemplateName(String templateName, String siteId);
    
    /**
     * Finds templates associated to a site.
     * 
     * @param siteId The id of a site. This <strong>DOES NOT</strong> have to be a valid id of an existing site.
     * @return if the site cannot be found the list of templates will be empty, never <code>null</code>.
     */
    List<PSTemplateSummary> findTemplatesBySite(String siteId);
    
    /**
     * Finds templates for the specified site which have an instance of the specified widget.
     * 
     * @param siteId never blank.
     * @param widgetId the widget definition id, never blank.
     * 
     * @return list of template summaries, never <code>null</code>, may be empty.
     */
    List<PSTemplateSummary> findTemplatesBySite(String siteId, String widgetId);
    
    /**
     * Finds templates for the specified site which have a corresponding type.
     * See com.percussion.pagemanagement.dao.findUserTemplatesByType(PSTemplateTypeEnum) for details.

     * @author federicoromanelli
     * 
     * @param siteId never blank.
     * @param type - the type of template. Never <code>null</code>
     * @return list of template summaries, never <code>null</code>, may be <code>empty</code>.
     */
    List<PSTemplateSummary> findTypedTemplatesBySite(String siteId, PSTemplateTypeEnum type);
    
    List<PSSiteSummary> findSitesByTemplate(String templateId);
    
    List<PSTemplateSummary> findTemplatesWithNoSite();
    
    List<PSTemplateSummary> save(PSSiteTemplates siteTemplates);
    
    
    
    /**
     * Creates a new template with an HTML widget on it, a new page using that
     * template, and in the local content of the widget in the page, the
     * contents of the imported page from the provided URL.
     * 
     * @param request the {@link HttpServletRequest} object that represents that request.
     * @param siteTemplates It must contain an ImportTemplate object that has
     *            the name of the site to which the template will belong, and a
     *            valid url to use to import the content for the new template.
     *            Note: The id of a site is its name.
     * 
     * @return The template summary of the template that was created from the
     *         contents of the provided URL.
     * 
     */
    PSTemplateSummary createTemplateFromUrl(@Context HttpServletRequest request, PSSiteTemplates siteTemplates);
    
    /**
     * Creates a new template with an HTML widget on it, a new page using that
     * template, and in the local content of the widget in the page, the
     * contents of the imported page from the provided URL.
     * 
     * @param request the {@link HttpServletRequest} object that represents that request.
     * @param siteTemplates It must contain an ImportTemplate object that has
     *            the name of the site to which the template will belong, and a
     *            valid url to use to import the content for the new template.
     *            Note: The id of a site is its name.
     * 
     * @return jobId of the asynchronous job created to process importing
     *         template from URL. This id can be used to poll for job status.
     * 
     */
    Long createTemplateFromUrlAsync(@Context HttpServletRequest request, PSSiteTemplates siteTemplates);
    
    /**
     * Once the import template from url job is completed, it gets the new
     * template that was created in the process.
     * 
     * @param jobId the id of the job that was running to import the template.
     *            This id is the same that is returned by the method
     *            createTemplateFromUrl.
     * @return the imported template, can be <code>null</code> if the job hasn't
     *         finished, or if it wasn't possible to retrieve result from the
     *         job.
     */
    PSTemplateSummary getImportedTemplate(Long jobId);
    
    PSValidationErrors validate(PSSiteTemplates siteTemplates);
    
    /**
     * Copies all templates from the specified source site to the specified target site.  Names, assets, and local
     * content will be retained.  Existing templates will not be updated.
     * 
     * @param site1Id The source site id, never blank.
     * @param site2Id The target site id, never blank.
     * 
     * @return map whose key is the source template id (guid string) and associated value is the id (guid string) of the
     * newly created template, never <code>null</code>.
     */
    Map<String, String> copyTemplates(String site1Id, String site2Id);

    PSTemplateSummary createTemplateFromPage(PSPageToTemplatePair pair);

}
