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
package com.percussion.rx.delivery.impl;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.delivery.client.IPSDeliveryClient.HttpMethodType;
import com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions;
import com.percussion.delivery.client.PSDeliveryClient;
import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.metadata.IPSMetadataEntry;
import com.percussion.delivery.metadata.IPSMetadataExtractorService;
import com.percussion.delivery.metadata.PSMetadataExtractorServiceLocator;
import com.percussion.delivery.metadata.extractor.data.PSMetadataProperty;
import com.percussion.delivery.metadata.solr.impl.PSSolrDeliveryHandler;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.delivery.service.PSDeliveryInfoServiceLocator;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.rx.delivery.IPSDeliveryResult;
import com.percussion.rx.delivery.IPSDeliveryResult.Outcome;
import com.percussion.rx.delivery.PSDeliveryException;
import com.percussion.rx.delivery.data.PSDeliveryResult;
import com.percussion.rx.publisher.PSRxPubServiceInternalLocator;
import com.percussion.rx.publisher.impl.PSPublishingJob;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

/**
 * The meta-data delivery handler, which publishes and unpublishes pages to the indexer
 * on the delivery server. The meta-data may be in RDF format in the published page, which
 * will be extracted by the indexer and saved to its repository.
 * 
 * @author AadmGent
 */
public class PSMetadataDeliveryHandler extends PSBaseDeliveryHandler
{
    private static final String PUBLISH_NON_PAGE_METADATA = "publishNonPageMetadata";

    public static final int DEFAULT_HTTP_CLIENT_RETRY_COUNT = 3;

    public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    
    public static final int DEFAULT_OPERATION_TIMEOUT = 30000;
    
    private int retryCount = DEFAULT_HTTP_CLIENT_RETRY_COUNT;
    
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    
    private int operationTimeout = DEFAULT_OPERATION_TIMEOUT;
    
    private Map<Long, Worker> workers = new ConcurrentHashMap<Long, PSMetadataDeliveryHandler.Worker>();

    private List<String> supportedMimeTypes = asList("application/xhtml+xml", "application/html", "text/html");

    private List<String> supportedContentTypes = asList("");
    
    private Map<IPSGuid, String> allContentTypes = new HashMap<IPSGuid,String>();
    
    private IPSCmsObjectMgr cmsObjectMgr;
    
    private PSDeliveryInfo defaultDeliveryServer;

    private PSSolrDeliveryHandler solrDeliveryService;


   /**
     * The logger
     */
    private static final Log log = LogFactory.getLog(PSMetadataDeliveryHandler.class);

    public void init(long jobid, IPSSite site, IPSPubServer pubServer) throws PSDeliveryException
    {
        super.init(jobid, site, pubServer);
        
        //  Be careful with object fields.  This instance is shared between jobs.  m_jobData and workers are 
        //  used to create job specific data. 
        if (this.cmsObjectMgr == null)
           this.cmsObjectMgr = PSCmsObjectMgrLocator.getObjectManager();
        
        List<PSContentTypeSummary> contentTypeSummariesList = PSContentTypeHelper.loadContentTypeSummaries(null);
        
        // Initializes the content types cache list
        for (PSContentTypeSummary psContentTypeSummary : contentTypeSummariesList)
        {
           if (!allContentTypes.containsKey(psContentTypeSummary.getGuid()))
              allContentTypes.put(psContentTypeSummary.getGuid(),psContentTypeSummary.getName());
        }
    }

    /**
     * Sets a list of supported mime-types.
     * 
     * @param mimeTypes it may be <code>null</code>, which is the same as empty.
     * It does nothing if there is no supported mime-types.
     */
    public void setSupportedMimeTypes(List<String> mimeTypes)
    {
        if (mimeTypes == null)
            supportedMimeTypes = new ArrayList<String>();
        else
            supportedMimeTypes = mimeTypes;
    }
    
    /**
     * Sets a list of supported content-types. This method is required by Spring beans file to set the property.
     * 
     * @param contentTypes the list of contentTypes. it may be <code>null</code>, which is the same as empty.
     * It does nothing if there is no supported content-types.
     */
    public void setSupportedContentTypes(List<String> contentTypes)
    {
        if (contentTypes == null)
            supportedContentTypes = new ArrayList<String>();
        else
           supportedContentTypes = contentTypes;
    }    

    /**
     * Sets the number of retry if failure, must not less than zero.
     * Defaults to {@link #DEFAULT_HTTP_CLIENT_RETRY_COUNT}
     * 
     * @param retryCount must be greater or equals to zero.
     */
    public void setRetryCount(int retryCount)
    {
        this.retryCount = retryCount;
    }

    /**
     * Sets the timeout until a connection is established in milli-seconds. 
     * Defaults to {@link #DEFAULT_CONNECTION_TIMEOUT}.
     * 
     * @param connectionTimeout must not be less than zero.
     */
    public void setConnectionTimeout(int connectionTimeout)
    {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Sets the socket timeout for each operation in milli-seconds. 
     * Defaults to {@link #DEFAULT_OPERATION_TIMEOUT}.
     * 
     * @param operationTimeout must not be less than zero.
     */
    public void setOperationTimeout(int operationTimeout)
    {
        this.operationTimeout = operationTimeout;
    }

    /**
     * Is this handler enabled.
     * 
     * @return <code>true</code> if it is enabled.
     */
    private boolean isEnabled(long jobId)
    {
        return workers.get(jobId)==null ? false : workers.get(jobId).isEnabled();
    }

    /**
     * Should only be used for testing.  Each job id gets its own delivery server setup
     * in prepareForDelivery.  This is based upon the pubServer stored in the jobData from init.
     * @param deliveryServer
    */
   public void setDeliveryServer(PSDeliveryInfo deliveryServer)
    {
        this.defaultDeliveryServer = deliveryServer;
    }

   /**
    * Used to inject DeliveryService for testing only.  
    * @return
    */
   public PSSolrDeliveryHandler getSolrDeliveryService()
   {
      return solrDeliveryService;
   }

   public void setSolrDeliveryService(PSSolrDeliveryHandler solrDeliveryService)
   {
      this.solrDeliveryService = solrDeliveryService;
   }
   
    /**
     * Validates the configuration of this handler.
     * 
     * @throws IllegalStateException if the configuration in incorrect.
     */
    private void validateConfig()
    {
        if (!isTransactional())
        {
           String msg = "The metadata delivery handler cannot be run in non-transactional mode.";
           IllegalStateException e = new IllegalStateException(msg);
           ms_log.error(msg, e);

           throw e;
       }
    }
    
    @Override
    protected Collection<IPSDeliveryResult> prepareForDelivery(long jobId) throws PSDeliveryException
    {
        validateConfig();
        
        log.debug("Preparing: " + jobId);
        
        log.debug("Retry count for HTTP connections: " + retryCount);
        log.debug("Connection timeout for HTTP connections: " + connectionTimeout);
        log.debug("Operation timeout for HTTP connections: " + operationTimeout);
        PSDeliveryInfo deliveryServer;
        PSSolrDeliveryHandler solr;
        
        JobData jobData = getJobData(jobId);
        
        if (jobData!=null)
        {
           String serverType = jobData.m_pubServer.getServerType();
           String adminURL="";
           if(jobData.m_pubServer.getPropertyValue("publishServer")!=null){
               adminURL=jobData.m_pubServer.getPropertyValue("publishServer");
           }


           IPSDeliveryInfoService srv = PSDeliveryInfoServiceLocator.getDeliveryInfoService();
           if(adminURL!=""){
               deliveryServer = srv.findByService(PSDeliveryInfo.SERVICE_INDEXER,serverType,adminURL);

           }else{
               deliveryServer = srv.findByService(PSDeliveryInfo.SERVICE_INDEXER,serverType);

           }

           PSPublishingJob job = PSRxPubServiceInternalLocator.getRxPublisherService().getPublishingJob(jobId);
           
           if (deliveryServer == null) 
           {
              String message = "PSMetadataDeliveryHandler is disabled because cannot find a server that runs \""
                    + PSDeliveryInfo.SERVICE_INDEXER + "\" service.";
              log.info(message);
           }

           //TODO:  Shouldn't this be optional if SOLR is not configured at all?
           solr = new PSSolrDeliveryHandler(jobData.m_site.getName(), serverType, job.isCleanPublish());
           this.solrDeliveryService = solr;
        }
        else
        {
           deliveryServer = defaultDeliveryServer;
           solr = solrDeliveryService;
        }
        
        
        Worker w = new Worker(deliveryServer, solr, PSDeliveryInfo.SERVICE_INDEXER + "/indexer/entry",
                retryCount, connectionTimeout, operationTimeout);
        
        workers.put(jobId, w);
        return super.prepareForDelivery(jobId);
    }

    @Override
    protected void releaseForDelivery(long jobId)
    {
        if (!isEnabled(jobId))
            return;

        log.debug("Releasing: " + jobId);
        super.releaseForDelivery(jobId);
        Worker w = workers.remove(jobId);
        if (w != null)
            w.close();
    }

   @Override
   protected IPSDeliveryResult doDelivery(Item item, long jobId, String location) throws PSDeliveryException
   {
      if (!isEnabled(jobId))
         return createResult(item, jobId, location);

      Worker w = workers.get(jobId);
      notNull(w);

      String mimeType = item.getMimeType();
      String contentType = "";
      List<IPSGuid> itemID = asList(item.getId());

      Set<Long> itemEntries = cmsObjectMgr.findContentTypesForIds(itemID);

      if (!itemEntries.isEmpty())
      {
         IPSGuid guid = PSGuidUtils.makeGuid(itemEntries.iterator().next(), PSTypeEnum.NODEDEF);
         contentType = allContentTypes.get(guid);
      }

      String description = description(item, jobId, location, w);

      IPSMetadataEntry metadata = null;
      String path = pathFromItem(location, item);
      IPSMetadataExtractorService extractor = PSMetadataExtractorServiceLocator.getMetadataExtractorService();
      boolean pubNonPageMeta = PSServer.getServerProps().getProperty(PUBLISH_NON_PAGE_METADATA, "false").equalsIgnoreCase("true");
      try
      {
         if (isContentTypeSupported(contentType))
         {
             //Strip off the charset if present as the parser doesn't like it.
             mimeType = mimeType.replace(";charset=UTF-8", "");

             if (isMimeTypeSupported(mimeType))
            {
               log.debug("Posting " + description);
               FileReader reader = null;
               try {
                   item.getFile().setSourceContentType(mimeType);
                  reader = new FileReader(item.getFile());


                  metadata = extractor.process(reader, mimeType, path, item.getMetaData());
               } finally {
                  IOUtils.closeQuietly(reader);
               }

            }
            else
            {
               log.debug("Skipped because of mime type: " + description);
            }
         }
         // Metadata can not be added to non page asset templates for example files and images.
         // metadata is added to bindings on template.  set values on $sys.metadata hashmap e.g.
         // $sys.metadata.dcterms:title    
         else if (item.getMetaData() != null && item.getMetaData().size() > 0 && pubNonPageMeta )
         {
            item.getMetaData().put("type", contentType);
            metadata = extractor.process(null, mimeType, path, item.getMetaData());
         }
         if (metadata != null)
         {
            PSLegacyGuid guid = (PSLegacyGuid)item.getId();
            metadata.addProperty(new PSMetadataProperty("sys_contentid", String.valueOf(guid.getContentId())));
            metadata.addProperty(new PSMetadataProperty("sys_revision", String.valueOf(guid.getRevision())));
            w.postMetadata(path, metadata);

            if(this.getSolrDeliveryService() != null && this.getSolrDeliveryService().isEnabled()) {
                w.postSolr(path, metadata, item.getFile());
            }
         }

         return createResult(item, jobId, location);
      }
      catch (WorkerHttpException wh)
      {
         log.error("Error for " + description, wh);
         return createErrorResult("Error for " + description + " caused by server responding: " + wh.getStatus()
               + "\nbody: " + wh.getError(), item, jobId, location);
      }
      catch (Exception e)
      {
         log.error("Error for " + description, e);
         return createErrorResult("Error for " + description + " caused by: " + e.getMessage(), item,
               jobId, location);
      }
      finally
      {
         item.release();
      }

   }

    /**
     * Remove a root from the specified location.
     * 
     * @param root the root, may be <code>null</code> or empty.
     * @param location the location, assumed not <code>null</code>.
     * @return the location without root, never <code>null</code>.
     */
    private String removeRoot(String root, String location)
    {
        if (StringUtils.isEmpty(root))
            return location;

        if (location.startsWith(root))
            return location.substring(root.length());

        return location;
    }

    /**
     * Creates a full path from the base URL of the site and a path that is
     * relative to the base URL.
     * 
     * @param baseUrl the base URL, not <code>null</code>. The format of it can
     *            be "http://<host>[:port]/...".
     * @param location the path that is relative to the base URL, not
     *            <code>null</code>. The expected format is
     *            "/vfolder1/vfolder2/..."/
     * 
     * @return the full path in the format or /<host>/vfolder1/vfolder2/.... It
     *         can never <code>null</code>.
     */
    public String createFullPath(String baseUrl, String location)
    {
        notNull(baseUrl);
        notNull(location);

        String path = location;
        try
        {
            URL url = new URL(baseUrl);
            baseUrl = "/" + url.getHost() + url.getPath();
        }
        catch (Exception e)
        {
            return path;
        }
        if (baseUrl.endsWith("/"))
        {
            path = path.startsWith("/") ? baseUrl + path.substring(1) : baseUrl + path;
        }
        else
        {
            path = path.startsWith("/") ? baseUrl + path : baseUrl + "/" + path;
        }
        return path;
    }

    protected String pathFromItem(String location, Item item)
    {
        notNull(location);

        JobData jobData = getJobData(item.getJobId());
        if (jobData == null)
            return location;

        PSPair<String, String> rootPair = getRootPath(location, item);
        if (rootPair == null)
           return location;

        location = removeRoot(rootPair.getFirst(), location);
        return createFullPath(rootPair.getSecond(), location);
    }

    /**
     * Gets the publish-root (as file location) and URL Root for the supplied item.
     * 
     * @param location the relative location of the site. 
     * @param item the published item, assumed not <code>null</code>.
     * 
     * @return the pair root, 1st element is the publish-root, 2nd element is the URL-root.
     * It may be <code>null</code> if the site object is not available in the job-data.
     */
   private PSPair<String, String> getRootPath(String location, Item item)
   {
      JobData data = getJobData(item.getJobId());
      if (data == null || data.m_site == null || data.m_site.getBaseUrl() == null)
         return null;

      IPSSite site = data.m_site;
      if (site == null || site.getBaseUrl() == null)
         return null;
      String root = getPublishRoot(data.m_pubServer, site);
      return new PSPair<String, String>(root, site.getBaseUrl());
   }

    @Override
    protected IPSDeliveryResult doRemoval(Item item, long jobId, String location)
    {
        if (!isEnabled(jobId))
            return createResult(item, jobId, location);

        Worker w = workers.get(jobId);
        notNull(w);
        String description = description(item, jobId, location, w);
        try
        {
            log.debug("Deleting: " + description);
            String path = pathFromItem(location, item);
            
            w.delete(path);
        }
        catch (WorkerHttpException wh)
        {
            log.error("Error for " + description, wh);
            return createErrorResult("Error for " + description +
                    " caused by server responding: " + wh.getStatus()
                    + "\nbody: " + wh.getError(),
                    item,
                    jobId,
                    location);
        }
        catch (Exception e)
        {
            log.error("Error " + description, e);
            return createErrorResult("Error for " + description +
                    " caused by unknown error: " + e.getMessage(),
                    item,
                    jobId,
                    location);
        }
        return createResult(item, jobId, location);
    }

    protected IPSDeliveryResult createResult(Item item, long jobId, String location)
    {
        return new PSDeliveryResult(Outcome.DELIVERED, null, item.getId(), jobId, item.getReferenceId(),
                location.getBytes());
    }

    protected IPSDeliveryResult createErrorResult(String failureMessage, Item item, long jobId, String location)
    {
        return new PSDeliveryResult(Outcome.FAILED, failureMessage, item.getId(), jobId, item.getReferenceId(),
                location.getBytes());
    }

    @SuppressWarnings("serial")
    private static class WorkerHttpException extends RuntimeException
    {
        private int status;

        private String error;

        public WorkerHttpException(int status, String error)
        {
            super(error);
            this.status = status;
            this.error = error;
        }

        public int getStatus()
        {
            return status;
        }

        public String getError()
        {
            return error;
        }

    }

    /**
     * The worker class used to communicate with the indexer to do the actual POST and DELETE
     * for the published and unpublished pages.
     * <p>
     * Each publishing job has a worker. The caller must call {@link #close()} after finished
     * publishing and/or unpublishing operations. 
     * 
     * @author AdamGent
     */
   private static class Worker
   {
      private PSDeliveryClient deliveryClient;

      private PSDeliveryInfo deliveryServer;

      private String actionUrl;

      private PSSolrDeliveryHandler solr;
      
      public Worker(PSDeliveryInfo deliveryServer, PSSolrDeliveryHandler solr, String actionUrl, int retryCount, int connectionTimeout,
            int operationTimeout)
      {
         this.deliveryServer = deliveryServer;
         if (deliveryServer !=null)
         {
            MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
            deliveryClient = new PSDeliveryClient(connectionManager, retryCount, connectionTimeout, operationTimeout);
         }
         this.actionUrl = actionUrl;
         this.solr = solr;
      }

      public boolean isEnabled()
      {
         return deliveryServer != null || (solr!=null && solr.isEnabled());
      }

      public boolean deliveryEnabled()
      {
         return deliveryServer != null;
      }

      public void postSolr(String path, IPSMetadataEntry entry, PSPurgableTempFile psPurgableTempFile) throws PSDeliveryException
      {
         solr.sendMetadataToSolr(path, entry, psPurgableTempFile);
      }

      public void postMetadata(String path, IPSMetadataEntry metadataEntry) throws HttpException, IOException,
            WorkerHttpException, JSONException
      {
         if (!deliveryEnabled())
            return;
         
         log.debug("Posting metadata: " + metadataEntry.getJson());
         deliveryClient.push(
               new PSDeliveryActionOptions().setDeliveryInfo(deliveryServer).setActionUrl(actionUrl + "/" + path)
                     .setAdminOperation(true).setSuccessfullHttpStatusCodes(Collections.singleton(202))
                     .setHttpMethod(HttpMethodType.POST), "application/json", metadataEntry.getJson());
      }

      public void delete(String path) throws PSDeliveryException
      {
         if (!deliveryEnabled())
            return;
         deliveryClient.push(
               new PSDeliveryActionOptions().setDeliveryInfo(deliveryServer).setActionUrl(actionUrl + "/" + path)
                     .setAdminOperation(true).setHttpMethod(HttpMethodType.DELETE), MediaType.TEXT_PLAIN,
               StringUtils.EMPTY);
         if(this.solr != null && this.solr.isEnabled()) {
             solr.delete(path);
         }
      }

      /**
       * Closes all connections that were created for various operations.
       */
      public void close()
      {
         if (deliveryEnabled() && deliveryClient!=null)
            deliveryClient.close();
         try
         {
            if (solr!=null)
               solr.commit();
         }
         catch (PSDeliveryException e)
         {
            log.error("Failed to commit solr transaction:",e);
         }
      }
   }

    protected Item createItemForTest(IPSGuid id, PSPurgableTempFile file, String mimeType, long refId, boolean removal,
            long jobId, long pubServerId, int deliveryContext)
    {
        return new Item(id, file, null, mimeType, refId, removal, jobId, pubServerId, deliveryContext);
    }
    
    private String description(Item item, long jobId, String location, Worker w)
    {
        String description = format(
                "metadataEndpoint: {4}, Item: {0}, jobId: {5}, location: {1}, contentType: {2}, length: {3}",
                item.getId().toString(), location, item.getMimeType(), item.getLength(), w.actionUrl, jobId);
        return description;
    }
    
    /**
     * Checks the corresponding mime type is supported.
     * 
     * @author adamgent
     * 
     * @param mimeType the string with the corresponding mime type, may be <code>null</code>. Eg: text/html 
     * 
     * @return true if it's included in the list of supported mime types, false otherwise or param is null.
     */
    private boolean isMimeTypeSupported(String mimeType)
    {
        if (mimeType == null)
            return false;
        for (String m : supportedMimeTypes)
        {
            if (mimeType.startsWith(m))
                return true;
        }
        return false;
    }

    /**
     * Checks the corresponding content type of item is supported (it's included in the supportedContentTypes list).
     * 
     * @author federicoromanelli
     * 
     * @param contentType the string with the corresponding contentType name, may be <code>null</code>. Eg: percPage 
     * 
     * @return true if it's included in the list, false otherwise or param is null.
     */
    private boolean isContentTypeSupported(String contentType)
    {
        if (contentType == null)
            return false;
        return supportedContentTypes.contains(contentType);
    }    
}
