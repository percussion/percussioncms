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
package com.percussion.webservices.publishing.impl;

import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSPublishingJobStatusCallback;
import com.percussion.rx.publisher.IPSRxPublisherServiceInternal;
import com.percussion.rx.publisher.PSRxPubServiceInternalLocator;
import com.percussion.rx.publisher.data.PSDemandWork;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.error.PSRuntimeException;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionContentList;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.IPSPubStatus;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.publishing.IPSPublishingWs;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Wraps various methods of the filter, publisher, and site manager services.
 */
public class PSPublishingWs implements IPSPublishingWs
{
   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#createContentList(java.lang.String)
    */
   public IPSContentList createContentList(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      return pubSvc.createContentList(name);
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#createEdition()
    */
   public IPSEdition createEdition()
   {
      return pubSvc.createEdition();
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#createEditionContentList()
    */
   public IPSEditionContentList createEditionContentList()
   {
      return pubSvc.createEditionContentList();
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#createSite()
    */
   public IPSSite createSite()
   {
      return siteMgr.createSite();
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#deleteContentLists(java.util.List)
    */
   public void deleteContentLists(List<IPSContentList> lists)
   {
      if (lists == null || lists.size() == 0)
         throw new IllegalArgumentException("lists may not be null or empty");
      
      pubSvc.deleteContentLists(lists);      
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#deleteEdition(com.percussion.services.publisher.IPSEdition)
    */
   public void deleteEdition(IPSEdition edition)
   {
      if (edition == null)
         throw new IllegalArgumentException("edition may not be null");
      
      pubSvc.deleteEdition(edition);
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#deleteSite(com.percussion.services.sitemgr.IPSSite)
    */
   public void deleteSite(IPSSite site)
   {
      if (site == null)
         throw new IllegalArgumentException("site may not be null");
      
      siteMgr.deleteSite(site);      
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#deleteSiteItems(com.percussion.utils.guid.IPSGuid)
    */
   public void deleteSiteItems(IPSGuid siteguid)
   {
      if (siteguid == null)
         throw new IllegalArgumentException("siteguid may not be null");
      
      pubSvc.deleteSiteItems(siteguid);      
   }
   
   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#findAllEditionsBySite(com.percussion.utils.guid.IPSGuid)
    */
   public List<IPSEdition> findAllEditionsBySite(IPSGuid siteId)
   {
      if (siteId == null)
         throw new IllegalArgumentException("siteId may not be null");
      
      return pubSvc.findAllEditionsBySite(siteId);
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#findAllEditionsBySite(com.percussion.utils.guid.IPSGuid)
    */
   public List<IPSEdition> findAllEditionsByPubServer(IPSGuid pubServerId)
   {
      if (pubServerId == null)
         throw new IllegalArgumentException("pubServerId may not be null");
      
      return pubSvc.findAllEditionsByPubServer(pubServerId);
   }
   
   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#findEditionByName(java.lang.String)
    */
   public IPSEdition findEditionByName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      return pubSvc.findEditionByName(name);
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#findContentListById(com.percussion.utils.guid.IPSGuid)
    */
   public IPSContentList findContentListById(IPSGuid contListId)
   {
      if (contListId == null)
         throw new IllegalArgumentException("contListId may not be null");
      try{
      return pubSvc.findContentListById(contListId);
      } catch (PSNotFoundException e) {
         throw new PSRuntimeException(e);
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#findFilterByName(java.lang.String)
    */
   public IPSItemFilter findFilterByName(String name) throws PSErrorException
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      try
      {
         return filterSvc.findFilterByName(name);
      }
      catch (PSFilterException e)
      {
         int code = IPSWebserviceErrors.OBJECT_NOT_FOUND_BY_NAME;
         PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                     IPSItemFilter.class.getName(), name), ExceptionUtils
                     .getFullStackTrace(new Exception()));
         throw error;
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#loadContentList(java.lang.String)
    */
   public IPSContentList loadContentList(String name) throws PSErrorException
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      try
      {
         return pubSvc.loadContentList(name);
      }
      catch (PSNotFoundException e)
      {
         int code = IPSWebserviceErrors.OBJECT_NOT_FOUND_BY_NAME;
         PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                     IPSContentList.class.getName(), name), ExceptionUtils
                     .getFullStackTrace(new Exception()));
         throw error;
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#loadContext(java.lang.String)
    */
   public IPSPublishingContext loadContext(String contextname)
         throws PSErrorException
   {
      if (StringUtils.isBlank(contextname))
      {
         throw new IllegalArgumentException("contextname may not be null or "
               + "empty");
      }
      
      try
      {
         return siteMgr.loadContext(contextname);
      }
      catch (PSNotFoundException e)
      {
         int code = IPSWebserviceErrors.OBJECT_NOT_FOUND_BY_NAME;
         PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                     IPSPublishingContext.class.getName(), contextname),
                     ExceptionUtils.getFullStackTrace(new Exception()));
         throw error;
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#loadEditionContentLists(com.percussion.utils.guid.IPSGuid)
    */
   public List<IPSEditionContentList> loadEditionContentLists(IPSGuid editionId)
   {
      if (editionId == null)
         throw new IllegalArgumentException("editionId may not be null");
      
      return pubSvc.loadEditionContentLists(editionId);
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#loadSite(java.lang.String)
    */
   public IPSSite findSite(String sitename) throws PSErrorException
   {
      if (StringUtils.isBlank(sitename))
         throw new IllegalArgumentException("sitename may not be null or empty");
      
      IPSSite site = siteMgr.findSite(sitename);
      if (site != null)
         return site;
      
      int code = IPSWebserviceErrors.OBJECT_NOT_FOUND_BY_NAME;
      PSErrorException error = new PSErrorException(code,
            PSWebserviceErrors.createErrorMessage(code,
                  IPSSite.class.getName(), sitename), ExceptionUtils
                  .getFullStackTrace(new Exception()));
      throw error;
   }
   

   public IPSSite findSiteById(IPSGuid siteId) throws PSErrorException
   {
      return siteMgr.findSite(siteId);
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#getItemSites(com.percussion.utils.guid.IPSGuid)
    */
   public List<IPSSite> getItemSites(IPSGuid contentId)
   {
      if (contentId == null)
         throw new IllegalArgumentException("contentId may not be null");
      
      return siteMgr.getItemSites(contentId);
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#saveContentList(com.percussion.services.publisher.IPSContentList)
    */
   public void saveContentList(IPSContentList clist)
   {
      if (clist == null)
         throw new IllegalArgumentException("clist may not be null");
      
      pubSvc.saveContentList(clist);
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#saveEdition(com.percussion.services.publisher.IPSEdition)
    */
   public void saveEdition(IPSEdition edition)
   {
      if (edition == null)
         throw new IllegalArgumentException("edition may not be null");
      
      pubSvc.saveEdition(edition);
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#saveEditionContentList(com.percussion.services.publisher.IPSEditionContentList)
    */
   public void saveEditionContentList(IPSEditionContentList list)
   {
      if (list == null)
         throw new IllegalArgumentException("list may not be null");
      
      pubSvc.saveEditionContentList(list);
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#saveSite(com.percussion.services.sitemgr.IPSSite)
    */
   public void saveSite(IPSSite site)
   {
      if (site == null)
         throw new IllegalArgumentException("site may not be null");
      
      siteMgr.saveSite(site);
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#findAllSites()
    */
   public List<IPSSite> findAllSites()
   {
      return siteMgr.findAllSites();
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#findPubStatusBySite(com.percussion.utils.guid.IPSGuid)
    */
   public List<IPSPubStatus> findPubStatusBySite(IPSGuid siteId)
   {
      if (siteId == null)
         throw new IllegalArgumentException("siteId may not be null");
      
      return pubSvc.findPubStatusBySite(siteId);
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#findPubStatusBySite(com.percussion.utils.guid.IPSGuid)
    */
   public List<IPSPubStatus> findPubStatusByEdition(IPSGuid editionId)
   {
      if (editionId == null)
         throw new IllegalArgumentException("editionId may not be null");
      
      return pubSvc.findPubStatusByEdition(editionId);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#purgeJobLog(long)
    */
   public void purgeJobLog(long jobid)
   {
      pubSvc.purgeJobLog(jobid);      
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#getDemandRequestJob(long)
    */
   public Long getDemandRequestJob(long requestid)
   {
      return getRxPubSvc().getDemandRequestJob(requestid);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#getPublishingJobStatus(long)
    */
   public IPSPublisherJobStatus getPublishingJobStatus(long jobId)
   {
      return getRxPubSvc().getPublishingJobStatus(jobId);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#queueDemandWork(int, com.percussion.rx.publisher.data.PSDemandWork)
    */
   public long queueDemandWork(int editionid, PSDemandWork work)
   {
      notNull(work, "work");

      try {
         return getRxPubSvc().queueDemandWork(editionid, work);
      }catch (PSNotFoundException e) {
         throw new PSRuntimeException(e);
      }
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#startPublishingJob(com.percussion.utils.guid.IPSGuid, com.percussion.rx.publisher.IPSPublishingJobStatusCallback)
    */
   public long startPublishingJob(IPSGuid edition,
         IPSPublishingJobStatusCallback callback)
   {
      notNull(edition, "edition");
            
      return getRxPubSvc().startPublishingJob(edition, callback);
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#getInProgressPublishingJobs(java.lang.String)
    */
   public List<Long> getInProgressPublishingJobs(String siteName)
   {
      notEmpty(siteName, "siteName");
      
      List<Long> inProgressJobs = new ArrayList<Long>();
      
      IPSSite site = siteMgr.findSite(siteName);
      if (site != null)
      {
         Collection<Long> activeJobs = getRxPubSvc().getActiveJobIds(site.getGUID());
         for (Long job : activeJobs)
         {
            IPSPublisherJobStatus pubJob = getPublishingJobStatus(job);
            if (!pubJob.getState().isTerminal())
            {
               inProgressJobs.add(job);
            }
         }
      }
      
      return inProgressJobs;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#createEditionTask()
    */
   public IPSEditionTaskDef createEditionTask()
   {
      return pubSvc.createEditionTask();
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#deleteEditionTask(com.percussion.services.publisher.IPSEditionTaskDef)
    */
   public void deleteEditionTask(IPSEditionTaskDef task)
   {
      notNull(task, "task");
      
      pubSvc.deleteEditionTask(task);
   }

   public void deleteStatusList(List<IPSPubStatus> statusList)
   {
      notNull(statusList, "statusList");
      
      pubSvc.deleteStatusList(statusList);
   }
   
   public void deleteEditionContentList(IPSEditionContentList edtContentList)
   {
      notNull(edtContentList, "edtContentList");
      
      pubSvc.deleteEditionContentList(edtContentList);
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#findEditionTaskById(com.percussion.utils.guid.IPSGuid)
    */
   public IPSEditionTaskDef findEditionTaskById(IPSGuid id)
   {
      notNull(id, "id");
      try {
         return pubSvc.findEditionTaskById(id);
      } catch (PSNotFoundException e) {
         throw new PSRuntimeException(e);
      }
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#saveEditionTask(com.percussion.services.publisher.IPSEditionTaskDef)
    */
   public void saveEditionTask(IPSEditionTaskDef task)
   {
      notNull(task, "task");
      
      pubSvc.saveEditionTask(task);
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.publishing.IPSPublishingWs#loadEditionTaskByEdition(com.percussion.utils.guid.IPSGuid)
    */
   public List<IPSEditionTaskDef> loadEditionTaskByEdition(IPSGuid editionid)
   {
      notNull(editionid, "editionid");
      return pubSvc.loadEditionTasks(editionid);
   }

   
   public void setFilterSvc(IPSFilterService filterSvc)
   {
      this.filterSvc = filterSvc;
   }
   
   public void setPubSvc(IPSPublisherService pubSvc)
   {
      this.pubSvc = pubSvc;
   }
   
   public void setSiteMgr(IPSSiteManager siteMgr)
   {
      this.siteMgr = siteMgr;
   }
   
   public void setPubServerDao(IPSPubServerDao pubServerDao)
   {
      this.pubServerDao = pubServerDao;
   }
   
   private IPSRxPublisherServiceInternal getRxPubSvc()
   {
      if (rxPubSvc == null)
      {
         rxPubSvc = PSRxPubServiceInternalLocator.getRxPublisherService();
      }
      
      return rxPubSvc;
   }

   /**
    * The publishing service.  Initialized in ctor, never <code>null</code>
    * after that.
    */
   private IPSPublisherService pubSvc;
   
   /**
    * The item filter service.  Initialized in ctor, never <code>null</code>
    * after that.
    */
   private IPSFilterService filterSvc;
   
   /**
    * The site manager.  Initialized in ctor, never <code>null</code> after
    * that.
    */
   private IPSSiteManager siteMgr;
   
   /**
    * The publisher service used for invoking and retrieving status of
    * publishing jobs.  Initialized in {@link #getRxPubSvc()} , never 
    * <code>null</code> after that.
    */
   private IPSRxPublisherServiceInternal rxPubSvc;
   
   /**
    * The pubserver dao.  Initialized in ctor, never <code>null</code> after
    * that.
    */
   private IPSPubServerDao pubServerDao;

}
