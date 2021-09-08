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
package com.percussion.pagemanagement.dao;

import java.util.List;

import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplate.PSTemplateTypeEnum;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.utils.guid.IPSGuid;

/**
 * 
 * Low level CRUD of templates.
 * 
 * @author adamgent
 *
 */
public interface IPSTemplateDao extends IPSGenericDao<PSTemplate, String>
{
    /**
     * Saves the specified template and add it to the specified folder.
     * 
     * @param template the to be saved template, not <code>null</code>.
     * @param siteId the ID of the site. It may be <code>null</code> 
     * if there is no need to attach the template to the site, assumed
     * the template has already been attached to a site.
     * 
     * @return the saved template, not <code>null</code>.
     */
    public PSTemplate save(PSTemplate template, String siteId) throws PSDataServiceException;
 
    /**
     * Loads the specified base template by ID.
     * 
     * @param id the ID of the specified template, assumed not <code>null</code>.
     * 
     * @return the template, never <code>null</code>.
     */
    public IPSAssemblyTemplate loadBaseTemplateById(IPSGuid id) throws IPSTemplateService.PSTemplateException;

    /**
     * Loads the specified base template by name.
     * 
     * @param name the name of the specified template, assumed not
     * <code>null</code>.
     *
     * @return the template, never <code>null</code>.
     */
    public PSAssemblyTemplate loadBaseTemplateByName(String name) throws IPSTemplateService.PSTemplateException;
    
    public PSTemplate createTemplate(String name, String sourceTemplateId) throws PSDataServiceException;
    
    /**
     * Find all readonly system templates for the supplied type.
     * @param type The type of the readonly or base templates, tries to find all the templates
     * that start with perc.<type>. where type is the supplied type.
     * 
     * @return never <code>null</code> or empty.  Results will be sorted alphabetically by name.
     */
    public List<PSTemplateSummary> findBaseTemplates(String type);
    
    /**
     * Find all user created templates.
     * 
     * @return the template summaries, never <code>null</code>, may be empty.  Results will be sorted alphabetically by
     * name.
     */
    public List<PSTemplateSummary> findAllUserTemplates() throws IPSTemplateService.PSTemplateException;

    /**
     * Find all templates (excluding ones with type PSTemplateTypeEnum.UNASSIGNED)
     *  
     * @return all template summaries, never <code>null</code>, may be empty.  Results will be sorted alphabetically by
     * name.
     * @throws com.percussion.share.dao.IPSGenericDao.LoadException 
     */
    public List<PSTemplateSummary> findAllSummaries() throws com.percussion.share.dao.IPSGenericDao.LoadException, IPSTemplateService.PSTemplateException;
    
    /**
     * Find all templates (excluding ones with type PSTemplateTypeEnum.UNASSIGNED)
     *  
     * @return all template summaries from the site selected, never <code>null</code>, may be empty.  
     * Results will be sorted alphabetically by name.
     * @throws com.percussion.share.dao.IPSGenericDao.LoadException
     * @param siteName accepts the siteName from PercSiteTemplatesController.js 
     */
    public List<PSTemplateSummary> findAllSummaries(String siteName) throws com.percussion.share.dao.IPSGenericDao.LoadException, IPSTemplateService.PSTemplateException;
 
    /**
     * Loads a list of user template summaries.
     *  
     * @param ids a list of IDs of the user templates, not <code>null</code>, but may be empty.
     * 
     * @return the loaded template summaries, not <code>null</code>, but may be empty
     */
    public List<PSTemplateSummary> loadUserTemplateSummaries(List<String> ids, String siteName) throws IPSTemplateService.PSTemplateException;

    /**
     * Finds the user template with the specified name. This is used by unit test only.
     * It cannot be used by production code
     * 
     * @param name never blank.
     * 
     * @return the template, never <code>null</code>.
     * 
     * @deprecated this method does not make sense any more because the same template 
     * can be used for different site. This is only used by unit test for now
     * and should be removed along with related unit test.
     */
    public PSTemplate findUserTemplateByName_UsedByUnitTestOnly(String name) throws PSDataServiceException;
    
    /**
     * Finds the user template for the specified name and site.
     * 
     * @param templateName the name of the template in question, not blank.
     * @param siteName the name of the site that template belongs to, not blank.
     * 
     * @return the ID of the template. It may be <code>null</code> if such template does not exist.
     */
    public IPSGuid findUserTemplateIdByName(String templateName, String siteName);
    
    /**
     * Generate the template to export
     *  
     * @param id never blank, the Id of the template to load
     * @param name maybe be empty, the name for the template
     * 
     * @return the loaded template 
     */
    public PSTemplate generateTemplateToExport(String id, String name) throws IPSTemplateService.PSTemplateException;
    
    /**
     * Import a given template
     *  
     * @param template never blank, the template to load
     * @param siteId maybe be empty, the site for the template
     * 
     * @return the loaded template 
     */
    public PSTemplate generateTemplateFromSource(PSTemplate template, String siteId) throws IPSTemplateService.PSTemplateException, IPSPathService.PSPathNotFoundServiceException;
   
   /**
    * Gets the thumbnail path for a template given the specific site name.
    * This method was refactored to be able to get the thumbnail path as a service method.
    * The template Summary must be a valid summary in order to get the name of the template, the method
    * assembles the path of the thumbnail using the template name and the site name
    * 
    * @param summary - the template object to retrieve the information
    * @param siteName - the corresponding site name from the template
    * - may be <code>null</code>
    * @return String - the path to the thumbnail for that template
    * - may return <code>null</code> if siteName is <code>empty</code> or <code>null</code> 
    */
    public String getTemplateThumbPath(PSTemplateSummary summary, String siteName);
    
    /**
     * Retrieves the list of user templates (not base templates) by type.
     * If type param is null, then normal type and untyped templates are returned.
     * Else the corresponding typed templates are returned.
     * 
     * @author federicoromanelli
     * @param type - the type of template: NORMAL or UNASSIGNED. May be <code>null</code>
     * @return the list of templates that correspond to that type. Never <code>null</code>.
     * May be <code>empty</code>
     */
    public List<PSTemplate> findUserTemplatesByType(PSTemplateTypeEnum type) throws IPSTemplateService.PSTemplateException;

    /**
     * Retrieves the list of summaries that correspond to user templates (not base templates)
     * filtered by type.
     * See com.percussion.pagemanagement.dao.findUserTemplatesByType(PSTemplateTypeEnum) for details.
     * 
     * @author federicoromanelli
     * @param type - the type of template: NORMAL or UNASSIGNED. May be <code>null</code>
     * @return the list of template summaries that correspond to that type. Never <code>null</code>.
     * May be <code>empty</code>
     */
    public List<PSTemplateSummary> findAllUserTemplateSummariesByType(PSTemplateTypeEnum type) throws IPSTemplateService.PSTemplateException;

    public enum BaseTemplateTypeEnum{
 	   all,base,resp
    }
}
