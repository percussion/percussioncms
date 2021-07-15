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

package com.percussion.queue.impl;

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.monitor.process.PSImportProcessMonitor;
import com.percussion.search.PSSearchIndexEventQueue;
import com.percussion.server.PSRequest;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.impl.PSNotificationService;
import com.percussion.share.dao.impl.PSIdMapper;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.importer.PSLink;
import com.percussion.utils.request.PSRequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PSSiteQueue
{
    private PSSite m_site = null;
    private TreeSet<Integer> m_catalogedIds = new TreeSet<>();
    private TreeSet<Integer> m_importedIds = new TreeSet<>();
    private HashMap<String, PSLink> m_importedLinks = new HashMap<>();
    private TreeSet<Integer> m_importingIds = new TreeSet<>();
    private String m_userAgent = null;
    private int m_maxImportCount = 0;
    private static final String POUND = "#";
    private static final String SLASH = "/";
    

    private static final Logger ms_log = LogManager.getLogger(PSSiteQueue.class);

    
    /**
     * The actual request that will result in spawning a thread.
     */
    private Map<String, Object> m_requestInfoMap = null;
	private PSNotificationService notificationService;
	private PSIdMapper idService;

    public PSSiteQueue()
    {
    	
    	if (this.notificationService == null) {
            notificationService = (PSNotificationService) getWebApplicationContext().getBean("sys_notificationService");
        }
    		idService = (PSIdMapper) getWebApplicationContext().getBean("sys_idMapper");
    		
    	 notificationService.addListener(EventType.PAGE_DELETE, new IPSNotificationListener()
         {
    		
             @Override
             public void notifyEvent(PSNotificationEvent event)
             {
            	 try {
            	 
            		 String guid = (String) event.getTarget();
            		 Integer id = idService.getContentId(guid);

            		 if (m_importedIds.contains(id))
            	 
            		 {
            			 m_importedIds.remove(id);
            		 }
                 
             }catch (Exception e)
             {
            	 
             }}
         });
    }
    
    public PSSiteQueue(PSSite site, String userAgent)
    {
        notNull(site);
        notNull(userAgent);
        notEmpty(userAgent);
        
        m_site = site;
        m_userAgent = userAgent;
    }
    
    
    public void setProcessedLink(final String link, PSLink linkObject)
    {
        if (m_importedLinks.size() > 50000)
        {
            m_importedLinks.clear();
        }
        m_importedLinks.put(processLinkForCache(link), linkObject);
    }
 
    public PSLink getProcessedLink(final String link)
    {
        return m_importedLinks.get(processLinkForCache(link));
    }
    
    public boolean hasLinkBeenProcessed(final String link) 
    {
        return m_importedLinks.containsKey(processLinkForCache(link));
    }
    
    private String processLinkForCache(final String link)
    {
       
        
        
        String finalPart = "";
        if (link.contains(SLASH))
        {
            finalPart = link.substring(link.lastIndexOf(SLASH));
        }
        
        String processedLink = link; 
        if (finalPart.contains(POUND)) 
        {
            processedLink = processedLink.substring(0, processedLink.indexOf(POUND) - 1);
        }
        /*
         Future filter for indices here.
        if (finalPart.toLowerCase().startsWith("index"))
        {
            processedLink = processedLink.substring(0, processedLink.toLowerCase().indexOf("index") + 5);
        }*/
        
        return processedLink.toLowerCase();
        
        
    }
    
    public void clearProcessedLinkCache()
    {
        m_importedLinks.clear();
    }
    
    public int sizeProcessedLinkCache()
    {
        return m_importedLinks.size();
    }
    
    public void setMaxImportCount(int max)
    {
        m_maxImportCount = max;
    }
    
    public int getMaxImportCount()
    {
        return m_maxImportCount;
    }
    
    public void setUserAgent(String userAgent)
    {
        notNull(userAgent);
        notEmpty(userAgent);
        
        m_userAgent = userAgent;
    }

    public void setSite(PSSite s)
    {
        m_site = s;
    }
    
    public String getUserAgent()
    {
        return m_userAgent;
    }

    /**
     * Get the request info. Must call {@link #setRequestInfoMap()} first.
     * This is used to set the request info for each importing page process.
     * 
     * @return the stored request info, never <code>null</code>.
     */
    public Map<String, Object> getRequestInfoMap()
    {
        if (m_requestInfoMap == null) {
            throw new IllegalStateException("The request info has not been set yet.");
        }
        
        return m_requestInfoMap;
    }
    
    /**
     * Set the request info. This cannot be called if the request info has not been
     * initialized in the current thread.
     * It do nothing if the request info has already configure.
     */
    public void setRequestInfoMap()
    {
        if (m_requestInfoMap != null) {
            return;
        }
        
        if (!PSRequestInfo.isInited()) {
            throw new IllegalStateException("The request info has not been initialized.");
        }
        
        m_requestInfoMap = PSRequestInfo.copyRequestInfoMap();
        PSRequest request = (PSRequest) m_requestInfoMap.get(PSRequestInfo.KEY_PSREQUEST);
        m_requestInfoMap.put(PSRequestInfo.KEY_PSREQUEST, request.cloneRequest());
    }
    
    public PSSite getSite()
    {
        return m_site;
    }
    
    public List<Integer> getImportingIds()
    {

        List<Integer> ids = new ArrayList<>(m_importingIds);
        return ids;
     
    }
    
    public void removeImportingId(Integer id)
    {
        m_importingIds.remove(id);
        checkSearchIndexQueueStatus();
    }
    
    /**
     * Ensure the search index queue is paused while there is a site importing, and resumed
     * once it is done.
     */
    public void checkSearchIndexQueueStatus()
    {
        checkSearchIndexQueueStatus(true);
    }
    
    /**
     * Ensure the search index queue is paused while there is a site importing, and resumed
     * once it is done.
     * 
     * @param checkImporting <code>true</code> to check if a page is importing, <code>false</code> to only
     * consider if there are cataloged pages waiting for import.
     */
    public void checkSearchIndexQueueStatus(boolean checkImporting)
    {
       // Need to start an stop during the entire import job.  
       return;
    }

    public synchronized boolean containsPagesForImport()
    {
        return m_catalogedIds.size() > 0 && m_site != null && m_userAgent != null && (!isMaxCountReached());
    }
    
    public synchronized List<Integer> getCatalogedIds()
    {
        List<Integer> ids = new ArrayList<>(m_catalogedIds);
        return ids;
    }

    public synchronized List<Integer> getImportedIds()
    {
        List<Integer> ids = new ArrayList<>(m_importedIds);
        return ids;
    }

    public synchronized void addCatalogedIds(List<Integer> ids)
    {
        // de-dupe the IDs if there is any
        m_catalogedIds.removeAll(ids);
        m_catalogedIds.addAll(ids);
        checkSearchIndexQueueStatus();
        PSImportProcessMonitor.setCatalogCount(m_catalogedIds.size());
    }
    
    public synchronized Integer getNextId()
    {
        try
        {
            if (isMaxCountReached())
            {
                return handleReachMaxCount();
            }
            
            if (m_catalogedIds.isEmpty())
            {
                return handleEmptyCatalogedIds();
            }
            
            return processNextCatalogedId();
        }
        finally
        {
            checkSearchIndexQueueStatus();
        }
    }

    private Integer processNextCatalogedId()
    {   
    	Integer nextId = m_catalogedIds.first();
        m_importingIds.add(nextId);
        m_catalogedIds.remove(nextId);
        logState();
        PSImportProcessMonitor.setCatalogCount(m_catalogedIds.size());
        return nextId;
    }

    private Integer handleEmptyCatalogedIds()
    {
   
        
        return null;
    }

    private Integer handleReachMaxCount()
    {
        logState();
        PSImportProcessMonitor.setCatalogCount(0);
        
        return null;
    }
    
    private boolean isMaxCountReached()
    {
        if (m_maxImportCount < 0) {
            return false;
        }
        
        int currentCount = m_importedIds.size();
        if (m_importingIds.size() != 0) {
            currentCount = currentCount + m_importingIds.size();
        }

        return currentCount >= m_maxImportCount;
    }
    
    private void logState()
    {
        if(! ms_log.isDebugEnabled()) {
            return;
        }
        
        ms_log.debug("[getNextId] m_importingId: {}", m_importingIds);
        ms_log.debug("[getNextId] m_catalogedIds: {}", m_catalogedIds.toString());
        ms_log.debug("[getNextId] m_importedIds: {}", m_importedIds.toString());
    }

    public void addImportedId(Integer id)
    {
        m_importedIds.add(id);
        m_importingIds.remove(id);
        checkSearchIndexQueueStatus();
    }
    
    public synchronized void addImportedIds(List<Integer> ids)
    {
        // de-dupe the IDs if there is any
        m_importedIds.removeAll(ids);
        m_importedIds.addAll(ids);
        m_importingIds.removeAll(ids);
        checkSearchIndexQueueStatus();
    }
    
    public synchronized void removeImportedId(Integer id)
    {
        m_importedIds.remove(id);
        m_importingIds.remove(id);
        checkSearchIndexQueueStatus();
    }
}
