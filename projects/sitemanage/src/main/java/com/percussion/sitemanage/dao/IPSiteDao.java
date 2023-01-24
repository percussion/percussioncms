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
