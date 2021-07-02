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
package com.percussion.rx.publisher.impl;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.rx.publisher.IPSEditionTask;
import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.sitemgr.IPSSite;

import java.io.File;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
import com.percussion.delivery.caching.data.PSInvalidateRequest;
import com.percussion.delivery.caching.utils.PSJaxbUtils;
**/
/**
 * Post edition task which invokes the cache manager to flush the publication cache.
 * Caching not currently being used.  This task should be moved if needed so dependency on com.percussion.delivery and percCachingAPI.jar does not exist in core
 */
@Deprecated
public class PSFlushPublicationCacheTask implements IPSEditionTask
{

   public TaskType getType()
   {
      return TaskType.POSTEDITION;
   }

   @SuppressWarnings("unused")
   public void perform(IPSEdition edition, IPSSite site, Date startTime,
         Date endTime, long jobId, long duration, boolean success,
         Map<String, String> params, IPSEditionTaskStatusCallback status)
      throws Exception
   {
      Validate.notNull(edition, "edition may not be null");

      Validate.notNull(site, "site may not be null");

      int maxUrls = MAX_URLS;
      String maxUrlsParam = params.get("maxUrls");
      if (StringUtils.isNumeric(maxUrlsParam))
      {
          int parsed = Integer.parseInt(maxUrlsParam.trim());
          if (parsed >= 0)
          {
              maxUrls = parsed;
          }
      }

      if (ms_log.isDebugEnabled())
      {
          ms_log.debug("Maximum urls: " + maxUrls);
      }

      //flushPageCache(jobId, site.getName(), maxUrls);
   }

   /**
    * Flushes the page cache if the specified job resulted in delivered items.  If the number of delivered items is less
    * than or equal to the maxUrls param, these items will be flushed from the cache, otherwise, the entire cache will
    * be flushed.
    * 
    * @param jobId publishing job for this task.
    * @param siteName site for which publishing was performed.
    * @param maxUrls maximum number of urls to flush.
    */
   /*
   private void flushPageCache(long jobId, String siteName, int maxUrls)
   {
       IPSPublisherService pubSvc = PSPublisherServiceLocator.getPublisherService();
       
       IPSPubStatus pubStatus = pubSvc.findPubStatusForJob(jobId);
       int delivered = pubStatus.getDeliveredCount();
       if (delivered > 0)
       {
           PSInvalidateRequest req = new PSInvalidateRequest();
           req.setRegionName(siteName);
           
           if (delivered <= maxUrls)
           {
               List<String> paths = req.getPaths();

               Iterator<IPSPubItemStatus> iter = pubSvc.findPubItemStatusForJobIterable(jobId).iterator();
               while (iter.hasNext())
               {
                   paths.add(iter.next().getLocation());
               }
           }
           else
           {
               req.setType(PSInvalidateRequest.Type.REGION);
           }
           
           if (ms_log.isDebugEnabled())
           {
               ms_log.debug("Flushing page cache.");
           }
           
           flushCache(req);
       }
   }
   */

   /**
    * Performs cache-flushing for the specified request.
    * 
    * @param req cache invalidate request.
    */
   /*
   private void flushCache(PSInvalidateRequest req)
   {
       if (ms_log.isDebugEnabled())
       {
           ms_log.debug("Flush cache request type: " + req.getType());
           ms_log.debug("Flush cache request region name: " + req.getRegionName());
           ms_log.debug("Flush cache request paths size: " + req.getPaths().size());
       }
       
       Object reqBody = null;
       try
       {
           reqBody = PSJaxbUtils.marshall(req, true);
       }
       catch (Exception e)
       {
           ms_log.error("Error marshalling flush cache request: " + e.getLocalizedMessage(), e);
       }
           
       if (reqBody != null)
       {
           IPSDeliveryInfoService infoSvc = PSDeliveryInfoServiceLocator.getDeliveryInfoService();
           PSDeliveryInfo cacheSvcInfo = infoSvc.findByService(PSDeliveryInfo.SERVICE_CACHING);
           if (cacheSvcInfo != null)
           {
               if (ms_log.isDebugEnabled())
               {
                   ms_log.debug("Caching service url: " + cacheSvcInfo.getUrl() + CACHE_SVC_URL);
                   ms_log.debug("Caching service admin url: " + cacheSvcInfo.getAdminUrl() + CACHE_SVC_URL);
               }
               
               PSDeliveryClient client = null;
               try
               {
                   MultiThreadedHttpConnectionManager connMgr = new MultiThreadedHttpConnectionManager();
                   PSDeliveryActionOptions options = new PSDeliveryActionOptions(cacheSvcInfo, FLUSH_CACHE_URL,
                           HttpMethodType.POST, true);
                   client = new PSDeliveryClient(connMgr);
                   client.push(options, MediaType.TEXT_XML, reqBody);
               }
               finally
               {
                   if (client != null)
                   {
                       client.close();
                   }
               }
           }
           else
           {
               ms_log.debug("Delivery info not found for caching service");
           }
       }
   }
   */
   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
      // No init
   }
   
   /**
    * Logger.
    */
   private static final Logger ms_log = LogManager.getLogger(PSFlushPublicationCacheTask.class);
   
   /**
    * Default maximum number of urls to submit for page cache flushing.
    */
   private static final int MAX_URLS = 5000;
   
   /**
    * Base url for the caching service.
    */
   private static final String CACHE_SVC_URL = "/perc-caching/manager"; 
   
   /**
    * Action url for the flush cache request.
    */
   private static final String FLUSH_CACHE_URL = CACHE_SVC_URL + "/invalidate";
}
