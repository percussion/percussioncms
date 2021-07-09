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
