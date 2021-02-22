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
package com.percussion.sitemanage.dao.impl;


import com.percussion.error.PSException;
import com.percussion.fastforward.managednav.IPSNavigationErrors;
import com.percussion.fastforward.managednav.PSNavException;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.data.PSItemSummaryUtils;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSitePublishProperties;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.error.IPSSiteManageErrors;
import com.percussion.util.PSPathUtil;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.publishing.IPSPublishingWs;
import com.percussion.webservices.publishing.PSPublishingWsLocator;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * The site handler which is used for site management.
 */
@Component("siteDao")
@Lazy
public class PSSiteDao implements IPSiteDao
{

    private PSSiteContentDao siteContentDao;
    private PSSitePublishDao sitePublishDao;
    
    
    @Autowired
    public PSSiteDao(PSSiteContentDao siteContentDao, PSSitePublishDao sitePublishDao)
    {
        super();
        this.siteContentDao = siteContentDao;
        this.sitePublishDao = sitePublishDao;
    }

    public PSSite find(String id) throws LoadException, DeleteException {
        return loadSite(id);
    }
    
    @Override
    public PSSiteSummary findByLegacySiteId(String id, boolean isValidate)
    {
        return sitePublishDao.findByLegacySiteId(id, isValidate);
    }

    /**
     * @return all site summaries sorted alphabetically by name (case-insensitive).
     */
    public List<PSSiteSummary> findAllSummaries()
    {
        List<PSSiteSummary> sums = sitePublishDao.findAllSummaries();
        
        sums.sort(siteComp);
        
        return sums;
    }

    public PSSiteSummary findSummary(String id) throws LoadException {
        return sitePublishDao.findSummary(id);
    }
    

    /**
     * @return list of sites, sorted alphabetically by name (case-insensitive).
     */
    public List<PSSite> findAll()
    {
        List<PSSite> sites = new ArrayList<>();

        List<PSSiteSummary> sums = findAllSummaries();
        
        for (PSSiteSummary pubSite : sums)
        {
            
            String name = pubSite.getName();
            try
            {
                PSSite site = find(name);
                sites.add(site);
            } catch (DeleteException | LoadException e) {
                log.error("#findAll: Failed to load site: {}... skipping it.  Error: {}",
                        name , e.getMessage());
                log.debug(e.getMessage(),e);
            }


        }

        sites.sort(siteComp);
        
        return sites;
    }
    

    public void delete(String id) throws DeleteException {
        try
        {
        	deleteSite(id);
        }
        catch (Exception e)
        {
            throw new DeleteException(e);
        }
    }

    public PSSite save(PSSite site) throws SaveException {
        try
        {
            saveSite(site, null);
            return site;
        }
        catch (Exception e)
        {
            throw new SaveException("Error saving site", e);
        }
    }


    protected PSSite loadSite(String name) throws LoadException, PSNavException, DeleteException {

        PSSiteSummary sum = findSummary(name);
        if(sum == null) return null;
        PSSite site=null;
        try
        {
            site = summaryToFull(sum);
        }
        catch (PSNavException e)
        {
            if(e.getErrorCode() == IPSNavigationErrors.NAVIGATION_SERVICE_FOLDER_ID_NOT_FOUND_FOR_PATH){
                // Try to self heal - Delete the site
                PSException ex = new PSException(IPSSiteManageErrors.SITEMANAGE_SERVICE_DELETING_BAD_SITE_RECORD,name);
                log.warn(ex.getLocalizedMessage());
                log.debug(ex);
                this.delete(sum.getId());
            }else {
                throw (e);
            }
        }catch(Exception e){
            throw(new LoadException(e));
        }
        return site;
    }


    /**
     * Deletes the specified site including all related and publishing items.
     * 
     * @param name The site name, never blank.
     * 
     * @throws PSErrorsException If an error occurs deleting related items.
     */
    protected void deleteSite(String name) throws PSErrorsException, DeleteException {
    	log.info("Starting delete of site "+name);
    	IPSPublishingWs publishWs = PSPublishingWsLocator.getPublishingWebservice();
    	IPSSite site = publishWs.findSite(name);
    	
        if (site == null)
            throw new DeleteException("Cannot delete site because site does not exist, site: " + name);
        
        PSSiteSummary summary = new PSSiteSummary();
        sitePublishDao.convertToSummary(site, summary);
        
        siteContentDao.deleteRelatedItems(summary);
        sitePublishDao.deleteSite(name);
    }

    /**
     * Saves the specified site. If the site is new, all related and publishing items will be created.
     * 
     * @param site The site object, may not be <code>null</code>.
     * @param origSite The site from which this site is being created or <code>null</code> to indicate that this site is
     * not being created from an existing site.
     * 
     * @throws PSErrorException If an error occurs creating related items.
     */
    protected void saveSite(PSSite site, PSSite origSite) throws IPSPubServerService.PSPubServerServiceException, PSNotFoundException {
        notNull(site,"site may not be null");
        boolean isNew = sitePublishDao.saveSite(site);
        if (isNew) {
            if (origSite == null)
            {
                siteContentDao.createRelatedItems(site);
            }
            else
            {
                siteContentDao.copy(origSite, site);
            }
        }
    }

    /*
     * //see base interface method for details
     */
    public boolean updateSite(IPSSite site, String newName, String newDescrption) throws PSNotFoundException {
        return sitePublishDao.updateSite(site, newName, newDescrption);
    }

    /**
     * Details can be found on the base interface
     */
    public void updateSitePublishProperties(IPSSite site, PSSitePublishProperties publishProps) throws PSNotFoundException {
        notNull(site, "site may not be null");
        notNull(publishProps, "publishProps may not be null");
        
        sitePublishDao.updateSitePublishProperties(site, publishProps);  
    }

    public void addPublishNow(IPSSite site) throws PSNotFoundException {
        notNull(site, "site may not be null");
        
        sitePublishDao.addPublishNow(site);
    }

    public void addUnpublishNow(IPSSite site) throws PSNotFoundException {
        notNull(site, "site may not be null");
        
        sitePublishDao.addUnpublishNow(site);
    }

    /**
     * Details can be found on the base interface
     */
    public String getSiteDeliveryType(IPSSite site) throws PSNotFoundException {
        return sitePublishDao.getSiteDeliveryType(site); 
    }
    
    public PSSite createSiteWithContent(String origId, String newName) throws PSDataServiceException, IPSPubServerService.PSPubServerServiceException, PSNotFoundException {
        notEmpty(origId, "origId may not be blank");
        notEmpty(newName, "newName may not be blank");
        
        PSSite orig = find(origId);
        if (orig==null)
            throw new PSDataServiceException();
        
        PSSite copy = new PSSite();
        copy.setBaseTemplateName(orig.getBaseTemplateName());
        copy.setDescription(orig.getDescription());
        copy.setHomePageTitle(orig.getHomePageTitle());
        copy.setName(newName);
        copy.setNavigationTitle(orig.getNavigationTitle());
        copy.setLabel(newName);
        copy.setTemplateName(orig.getTemplateName());
        
 
        saveSite(copy, orig);
  
        
        return copy;
    }

    public PSSiteSummary findByName(String name){
        Validate.notEmpty(name);

        List<PSSiteSummary> sums = findAllSummaries();

        for (PSSiteSummary sum : sums) {
          if(sum.getName().equalsIgnoreCase(name))
              return sum;
        }
        return null;
    }

    /***
     * Returns the site for the given path.
     * @param path never blank. //Sites/SiteFolder/folder
     *
     * @return
     */
    public PSSiteSummary findByPath(String path)
    {
        Validate.notEmpty(path);

        List<PSSiteSummary> sums = findAllSummaries();

        String siteName = null;
        if(path.startsWith("/Sites/")) {
            path = "/" + path;
        }else if(! path.startsWith(PSPathUtil.SITES_ROOT)){
            path = PSPathUtil.SITES_ROOT + "/" + path;
        }

        siteName = PSPathUtils.getSiteFromPath(path);

        for (PSSiteSummary sum : sums) {
            if ( sum.getFolderPath() != null 
                    && PSFolderPathUtils.isDescedentPath(path, sum.getFolderPath())) {
                return sum;
            }else if(sum.getName().equalsIgnoreCase(siteName)){
                //Site Folder Path may be different than site name
                return sum;
            }
        }
        
        return null;
    }
    
    /**
     * Creates a site model from a specified site object.
     * 
     * @param site The site, may not be <code>null</code>.
     * @return The site model representing the site.
     *
     */
    protected PSSite summaryToFull(PSSiteSummary site) throws PSNavException, PSDataServiceException {
        notNull(site,"site may not be null");
        PSSite s = new PSSite();
        PSItemSummaryUtils.copyProperties(site, s);
        s.setSiteId(site.getSiteId());
        s.setDefaultFileExtention(site.getDefaultFileExtention());
        s.setCanonical(site.isCanonical());
        s.setCanonicalDist(site.getCanonicalDist());
        s.setCanonicalReplace(site.isCanonicalReplace());
        s.setSiteProtocol(site.getSiteProtocol());
        s.setDefaultDocument(site.getDefaultDocument());
        
        PSPage homepage = siteContentDao.getHomePage(site);
        String navTitle = siteContentDao.getNavTitle(site);
        
        if (homepage != null) {
            s.setBaseTemplateName(homepage.getTemplateId());
            s.setHomePageTitle(homepage.getTitle());
        }
        else {
            log.error("No homepage for site: {}", site.getName());
        }
        s.setDescription(site.getDescription());
        
        s.setNavigationTitle(navTitle);

        siteContentDao.loadTemplateInfo(s);

        return s;
    }



    /**
     * Used for sorting of {@link PSSiteSummary} objects.  Sorts alphabetically by name (case-insensitive).
     * 
     * @author peterfrontiero
     */
    public static class PSSiteSummaryComparator implements Comparator<PSSiteSummary>
    {
        public int compare(PSSiteSummary s1, PSSiteSummary s2)
        {
            if(s1 != null && s2 !=null) {
                if(s1.getName()!= null && s2.getName()!= null) {
                    return s1.getName().compareToIgnoreCase(s2.getName());
                }
                else{
                    if(s1.getName() == null){
                        if(s2.getName() == null){
                            return 0;
                        }else{
                            return 1;
                        }
                    }else{
                        return -1;
                    }
                }
            }else{
                return 0;
            }
        }
    }
    



    
    

    /**
     * Used for site sorting.  Never <code>null</code>.
     */
    private PSSiteSummaryComparator siteComp = new PSSiteSummaryComparator();
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSSiteDao.class);


}
