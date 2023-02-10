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
package com.percussion.sitemanage.dao.impl;

import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.data.PSDataItemSummary;
import com.percussion.share.service.IPSDataItemSummaryService;
import com.percussion.share.service.IPSDataService;
import com.percussion.sitemanage.dao.IPSSiteArchitectureDao;
import com.percussion.sitemanage.data.PSSiteArchitecture;
import com.percussion.sitemanage.data.PSSiteSection;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.publishing.IPSPublishingWs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("siteArchitectureDao")
@Lazy
public class PSSiteArchitectureDao implements IPSSiteArchitectureDao
{
    @Autowired
    public PSSiteArchitectureDao(IPSDataItemSummaryService dataItemSummaryService, IPSPublishingWs pubWs)
    {

        this.dataItemSummaryService = dataItemSummaryService;
        this.pubWs = pubWs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.sitemanage.dao.IGenericDao#get(java.io.Serializable)
     */
    public PSSiteArchitecture find(String id) throws LoadException
    {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        PSSiteArchitecture sa = createSiteArchitecture(id);
        return sa;
    }

    /**
     * Creates the object {@link PSSiteArchitecture} for the given site. Returns
     * the structure expanded upto the first level.
     * 
     * @param name The name of the site for which the site architecture needs to
     *            be created.
     * @return The site architecture for the given site.
     * @throws LoadException
     */
    private PSSiteArchitecture createSiteArchitecture(String name) throws LoadException
    {
        PSSiteArchitecture sa = null;
        try
        {
            IPSSite site = pubWs.findSite(name);
            sa = new PSSiteArchitecture();
            sa.setName(name);
            String folderRoot = site.getFolderRoot();
            List<PSSiteSection> sections = new ArrayList<>();
            sections.add(createSiteSection(folderRoot));
            sa.setSections(sections);
        }
        catch (PSErrorException | IPSDataService.DataServiceNotFoundException | IPSDataService.DataServiceLoadException e)
        {
            throw new LoadException(e);
        }
        return sa;
    }

    /**
     * Creates a site section for the given folder root.
     * 
     * @param folderRoot assumed not <code>null</code>.
     * @return The site section object for the given folder root.
     */
    private PSSiteSection createSiteSection(String folderRoot) throws IPSDataService.DataServiceNotFoundException, IPSDataService.DataServiceLoadException {
        PSSiteSection siteSection = new PSSiteSection();
        String id = dataItemSummaryService.pathToId(folderRoot);
        List<PSDataItemSummary> sums = dataItemSummaryService.findFolderChildren(id);
        for (PSDataItemSummary itemSummary : sums)
        {
            if (itemSummary.getType().equals(NAV_TREE_CONTENTTYPE_NAME))
            {
                siteSection.setId(itemSummary.getId());
                siteSection.setTitle(itemSummary.getName());
                siteSection.setFolderPath(folderRoot);
            }
        }
        return siteSection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.sitemanage.dao.IGenericDao#getAll()
     */
    public List<PSSiteArchitecture> findAll() throws com.percussion.share.dao.IPSGenericDao.LoadException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.sitemanage.dao.IGenericDao#remove(java.io.Serializable)
     */
    public void delete(String id) throws com.percussion.share.dao.IPSGenericDao.DeleteException
    {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.sitemanage.dao.IGenericDao#save(java.lang.Object)
     */
    public PSSiteArchitecture save(PSSiteArchitecture object)
            throws com.percussion.share.dao.IPSGenericDao.SaveException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public List<PSSiteSection> getSections(String id) throws com.percussion.share.dao.IPSGenericDao.LoadException
    {
        // TODO Auto-generated method stub
        return null;
    }

    private IPSDataItemSummaryService dataItemSummaryService;

    /**
     * The publishing ws. Initialized in ctor, never <code>null</code> after
     * that.
     */
    private IPSPublishingWs pubWs;

    /**
     * The constant for the name of the nav tree content type.
     */
    private static final String NAV_TREE_CONTENTTYPE_NAME = "rffNavTree";

}
