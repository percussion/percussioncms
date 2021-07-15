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
package com.percussion.delivery.metadata.impl;

import com.percussion.delivery.listeners.IPSServiceDataChangeListener;
import com.percussion.delivery.metadata.IPSMetadataDao;
import com.percussion.delivery.metadata.IPSMetadataEntry;
import com.percussion.delivery.metadata.IPSMetadataIndexerService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Hibernate-based implementation of {@link IPSMetadataIndexerService}.
 * 
 * @see IPSMetadataIndexerService
 * @author miltonpividori
 */
public class PSMetadataIndexerService implements IPSMetadataIndexerService
{
    private IPSMetadataDao dao;     
    
    
    /* Connector to be notified of data change events */
    private IPSServiceDataChangeListener connector;
    private List<IPSServiceDataChangeListener> listeners = new ArrayList<>();
    private final String[] PERC_METADATA_SERVICES = {"perc-metadata-services"};
    
    @Autowired
    public PSMetadataIndexerService(IPSMetadataDao dao)
    {
        this.dao = dao;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.metadata.IPSMetadataIndexerService#save(com.percussion
     * .metadata.data.PSDbMetadataEntry)
     */
    public void save(IPSMetadataEntry entry)
    {
        dao.save(entry);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.metadata.IPSMetadataIndexerService#save(java.util.Collection
     * )
     */
    public void save(Collection<IPSMetadataEntry> entries)
    {
        Validate.notNull(entries, "entries cannot be null");
        
        if (entries.size() == 0)
            return;

        // array of sites for data changed event
        HashSet<String> siteNames = new HashSet<>(entries.size());
        for (IPSMetadataEntry entry : entries)
        {
            siteNames.add(entry.getSite());
        }
        
                
        boolean hasDirty = dao.hasDirtyEntries(entries);
        if(hasDirty) // We check dirty as we don't consider inserts a change that the cache needs to know about.          
            fireDataChangeRequestedEvent(siteNames);

        try
        {
           dao.save(entries);
        }
        finally
        {
            if(hasDirty)
                fireDataChangedEvent(siteNames);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.metadata.IPSMetadataIndexerService#delete(java.lang.String
     * )
     */
    public void delete(String pagepath)
    {
        Validate.notEmpty(pagepath, "pagepath cannot be null or empty.");

        HashSet<String> siteNames = new HashSet<>();
        String site = getSiteNameFromPagePath(pagepath);
        siteNames.add(site);

        if (dao.delete(pagepath))
        {
            fireDataChangeRequestedEvent(siteNames);
            fireDataChangedEvent(siteNames);
        }

    }

    /**
     * Utility method to extract site name from a page path.
     * Assumes page path is of the form /sitename/rest/of/path/to/page
     * @param pagepath
     * @return site
     */
    private String getSiteNameFromPagePath(String pagepath) {
        String[] splitPath = pagepath.split("/");
        String site = splitPath[1];

        if (site.endsWith("apps"))
            site = site.substring(0, site.length() - 4);
        
        return site;
    }

    
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.metadata.IPSMetadataIndexerService#delete(java.util.Collection
     * )
     */
    public void delete(Collection<String> pagepaths)
    {
        Validate.notNull(pagepaths, "pagepaths cannot be null.");

                
        HashSet<String> siteNames = new HashSet<>(pagepaths.size());
        for(String path : pagepaths)
        {
            String site = getSiteNameFromPagePath(path);
            siteNames.add(site);
        }
        dao.delete(pagepaths);
        fireDataChangeRequestedEvent(siteNames);
        fireDataChangedEvent(siteNames);
    }   

    /*
     * (non-Javadoc)
     * @see com.percussion.metadata.IPSMetadataIndexerService#findEntry(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public IPSMetadataEntry findEntry(String pagepath)
    {
        Validate.notEmpty(pagepath, "pagepath cannot be null nor empty");
        
        return dao.findEntry(pagepath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.metadata.IPSMetadataIndexerService#getAllIndexedDirectories
     * ()
     */
    public Set<String> getAllIndexedDirectories()
    {
        return dao.getAllIndexedDirectories();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.metadata.IPSMetadataIndexerService#deleteAllMetadataEntries
     * ()
     */
    public void deleteAllMetadataEntries()
    {
        List<String> sites = dao.getAllSites();
        HashSet<String> siteSet = new HashSet<>(sites);
        fireDataChangeRequestedEvent(siteSet);
        dao.deleteAllMetadataEntries();
        fireDataChangedEvent(siteSet);
    }

    public List<IPSMetadataEntry> getAllEntries()
    {
        return dao.getAllEntries();
    }

    /* (non-Javadoc)
     * @see com.percussion.metadata.IPSMetadataIndexerService#addMetadataListener(com.percussion.metadata.event.IPSMetadataListener)
     */
    public void addMetadataListener(IPSServiceDataChangeListener listener)
    {
        Validate.notNull(listener, "listener cannot be null.");
        if(!listeners.contains(listener))
            listeners.add(listener);
        
    }

    /* (non-Javadoc)
     * @see com.percussion.metadata.IPSMetadataIndexerService#removeMetadataListener(com.percussion.metadata.event.IPSMetadataListener)
     */
    public void removeMetadataListener(IPSServiceDataChangeListener listener)
    {
        Validate.notNull(listener, "listener cannot be null.");
        if(listeners.contains(listener))
            listeners.remove(listener);
    }

    /**
     * Fire a data change event for all registered listeners.
     */
    private void fireDataChangedEvent(Set<String> sites)
    {
        if(sites == null || sites.size() == 0)
        {
            return;
        }

        for(IPSServiceDataChangeListener listener : listeners)
        {
            listener.dataChanged(sites, PERC_METADATA_SERVICES);
        }
    }

    /**
     * Fire a data change event for all registered listeners.
     */
    private void fireDataChangeRequestedEvent(Set<String> sites)
    {
        if(sites == null || sites.size() == 0)
        {
            return;
        }

        for(IPSServiceDataChangeListener listener : listeners)
        {
            listener.dataChangeRequested(sites, PERC_METADATA_SERVICES);
        }
    }
    
    
    public IPSServiceDataChangeListener getConnector()
    {
        return connector;
    }

    public void setConnector(IPSServiceDataChangeListener connector)
    {
        this.connector = connector;
        this.addMetadataListener(connector);
    }
}
