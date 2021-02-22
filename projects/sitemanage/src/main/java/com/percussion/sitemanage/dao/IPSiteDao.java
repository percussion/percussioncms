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
package com.percussion.sitemanage.dao;

import com.percussion.pubserver.IPSPubServerService;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSitePublishProperties;
import com.percussion.sitemanage.data.PSSiteSummary;

import java.util.List;


public interface IPSiteDao extends IPSGenericDao<PSSite, String> {

    /***
     * Finds a site  by the site name.
     * @param name
     * @return
     */
    public PSSiteSummary findByName(String name);

    public PSSiteSummary findSummary(String id) throws LoadException;
    
    public List<PSSiteSummary> findAllSummaries();
    
    /**
     * Finds the site summary by the legacy ID.
     * 
     * @param id the legacy ID of the site, not <code>null</code>.
     * @param isValidate it is <code>true</code> if wants to validate the site that contains "category" folder;
     * otherwise don't validate the returned site object. The returned object should not to be validated if it is
     * used for assembly process, such as previewing or publishing. 
     * 
     * @return the site with the specified ID. It may be <code>null</code> if cannot find the site.
     */
    public PSSiteSummary findByLegacySiteId(String id, boolean isValidate);
    
    /**
     * Updates the specified site, and its related edition/content-list/pubservers with the
     * new name and description.
     * 
     * @param site the existing site, not <code>null</code>.
     * @param newName the new name of the site, not blank.
     * @param newDescrption the new description of the site, may be blank.
     * @return <code>true</code> if a pubserver was modified as a result of the change, <code>false</code> if not.
     */
    public boolean updateSite(IPSSite site, String newName, String newDescrption) throws PSNotFoundException;
    
    /**
     * Updates the specified site with passed in publishing properties and updates 
     * Content-list with user passed in delivery type and this is part of the 
     * publish properties. 
     * @param site the existing site, not <code>null</code>.
     * @param publishProps publishing properties to be updated on the site. not <code>null</code>.
     */
    public void updateSitePublishProperties(IPSSite site, PSSitePublishProperties publishProps) throws PSNotFoundException;
    
    /**
     * The delivery type is the type of publishing which will be used for the specified site.
     * @param site the existing site, not <code>null</code>.
     * @return delivery type of the given site. not <code>null</code>. or not <code>empty</code>.
     */
    public String getSiteDeliveryType(IPSSite site) throws PSNotFoundException;
    
    /**
     * Adds the publish-now infrastructure (edition, content list) for the specified site.  It is assumed that support
     * for publish now does not exist for the site.
     * @param site the existing site, not <code>null</code>.
     */
    public void addPublishNow(IPSSite site) throws PSNotFoundException;
    
    /**
     * Adds the unpublish-now infrastructure (edition, content list) for the specified site.  It is assumed that support
     * for un-publish now does not exist for the site.
     * @param site the existing site, not <code>null</code>.
     */
    public void addUnpublishNow(IPSSite site) throws PSNotFoundException;

    /**
     * Creates a site with content copied from an existing site.  The result will be a new site visible in the Finder
     * which includes a copy of all content from the original site.  Templates are not included.  The site will have a
     * default publishing configuration.
     * 
     * @param origId the id of the original site, may not be blank.
     * @param newName the name of the new site, may not be blank.
     * 
     * @return the newly created site, never <code>null</code>.
     */
    public PSSite createSiteWithContent(String origId, String newName) throws PSDataServiceException, IPSPubServerService.PSPubServerServiceException, PSNotFoundException;
    
    /**
     * Finds the parent site of the specified path.
     * 
     * @param path never blank.
     * 
     * @return summary of the parent site, may be <code>null</code> if there are no sites or the path does not exist
     * under a site.
     */
    public PSSiteSummary findByPath(String path);
}
