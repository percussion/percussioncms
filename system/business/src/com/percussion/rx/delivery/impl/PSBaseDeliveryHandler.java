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
package com.percussion.rx.delivery.impl;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.percussion.rx.delivery.IPSDeliveryErrors;
import com.percussion.rx.delivery.IPSDeliveryHandler;
import com.percussion.rx.delivery.IPSDeliveryItem;
import com.percussion.rx.delivery.IPSDeliveryManager;
import com.percussion.rx.delivery.IPSDeliveryResult;
import com.percussion.rx.delivery.IPSDeliveryResult.Outcome;
import com.percussion.rx.delivery.PSDeliveryException;
import com.percussion.rx.delivery.data.PSDeliveryResult;
import com.percussion.rx.publisher.IPSPublisherJobStatus.ItemState;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.IPSRxPublisherServiceInternal;
import com.percussion.rx.publisher.PSRxPubServiceInternalLocator;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.rx.publisher.data.PSPubItemStatus;
import com.percussion.rx.publisher.impl.PSPublishHandler;
import com.percussion.rx.publisher.impl.PSPublishingJob;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.util.PSStopwatch;
import com.percussion.utils.guid.IPSGuid;

/**
 * Base delivery handler. This defines a basic mechanism that enables the
 * subclasses. It implements the basic handler in a transactional fashion. The
 * delivery and remove methods store {@link Item} objects to be handled during
 * commit or rollback. The items delegate back to the handler to do the actual
 * delivery or removal during commit by calling the <code>doDelivery</code> and
 * <code>doRemoval</code> methods.
 * <p>
 * Each call to {@link #deliver(IPSDeliveryItem)} causes a temporary file to be
 * created, along with a registration for where the temporary file should be
 * stored in the file system. On {@link #commit(long)}, the cached information
 * is delivered, and the temporary files are purged.
 * <h3>Locations</h3>
 * Locations for file based handlers are straightforward - they are simply the
 * actual path to the file for the handler to use. These are used for both
 * delivery and removal operations.
 * <p>
 * For non-file based handlers, the location needs to be any unique abstract
 * value that can be used in the <code>JobData</code> maps to disambiguate the
 * stored information. For example, the database handler simply uses the
 * reference id.
 * <p>
 * Locations are supplied by the <code>getPublishingLocation</code> method,
 * which can be overridden by subclasses.
 * <p>
 * Locations may be used "bare" for unpublishing, as is done by the file based
 * handlers. The location is simply derived from the unpublishing information
 * directly. The mechanism is determined by the
 * <code>getUnpublishingLocation</code> method, which can be overridden by
 * subclasses.
 * 
 * @author dougrand
 * @see Item
 * @see JobData
 */
public abstract class PSBaseDeliveryHandler implements IPSDeliveryHandler
{
   /**
    * Logger.
    */
    protected static final Logger ms_log = LogManager.getLogger(PSBaseDeliveryHandler.class);
   
   @Autowired
   private IPSDeliveryManager ms_deliveryManager;

   /**
    * A single item for the job to be delivered. The item holds a temporary file
    * that is used for delivery. The item's commit method delegate to the outer
    * class's methods, which allows the item to be used in a number of handlers.
    * The abort method simply removes the temp file.
    * <p>
    * It is important to store the delivery information in a file because a
    * publishing run can consist of thousands and thousands of individual items,
    * and each item can be very large. So the use of files mitigates the amount
    * of memory required.
    */
   public class Item
   {
      /**
       * The file to be written, it may be <code>null</code> if
       */
      private final PSPurgableTempFile mi_file;

      /**
       * The additional metadata from the assembly template bindings to send to
       * the metadata service it may be <code>null</code> if
       * {@link #mi_meta_data} is not <code>null</code>.
       */
      private final Map<String, Object> mi_meta_data;

      /**
       * The delivery data, it may be <code>null</code> if {@link #mi_file} is
       * not <code>null</code>.
       */
      private final byte[] mi_data;

      /**
       * The item's id, used for error information, never <code>null</code>
       * after ctor.
       */
      private final IPSGuid mi_id;

      /**
       * The reference id for this item.
       */
      private final long mi_referenceId;

      /**
       * @see #isRemoval()
       */
      private final boolean mi_removal;

      /**
       * The job ID.
       */
      private final long mi_jobId;

      private final long mi_length;

      private final String mi_mimeType;

      private final Long mi_pubServerId;
      
      private final int mi_deliveryContext;

      /**
       * See {@link #setDoRelease(boolean)}
       */
      private boolean mi_doRelease = true;

      /**
       * A list of items which have the same published location as the current
       * item. The current item will override all of these items on the
       * published target. Defaults to <code>null</code>.
       */
      private List<Item> mi_overrideItems = null;

      /**
       * Constructs an instance from the given parameters.
       * 
       * @param id the id of the item, never <code>null</code>.
       * @param file the temp file to store the item, never <code>null</code>.
       * @param mimeType the mime type of the result, it may be
       *           <code>null</code>.
       * @param refId the reference id of the item
       * @param removal indicator whether the delivery handler should remove the
       *           target file upon delivery. If <code>false</code> the file is
       *           delivered, if <code>true</code>, it is removed.
       */
      public Item(IPSGuid id, PSPurgableTempFile file, Map<String, Object> metaData, String mimeType, long refId,
            boolean removal, long jobId, Long pubserverId, int deliveryContext)
      {
         this(id, file, null, metaData, file != null ? file.length() : -1, mimeType, refId, removal, jobId, pubserverId, deliveryContext);
      }

      /**
       * Constructs an instance from the given parameters.
       * 
       * @param id the ID of the item, never <code>null</code>.
       * @param file the temporary file to store the item, this may be
       *           <code>null</code> if inputStream is not <code>null</code>.
       * @param data the item content, it may be <code>null</code> if the
       *           temporary file is not <code>null</code>.
       * @param metaMap
       * @param refId the reference id of the item
       * @param removal indicator whether the delivery handler should remove the
       *           target file upon delivery. If <code>false</code> the file is
       *           delivered, if <code>true</code>, it is removed.
       */
      public Item(IPSGuid id, PSPurgableTempFile file, byte[] data, Map<String, Object> metaMap, long length,
            String mimeType, long refId, boolean removal, long jobId, Long pubserverId, int deliveryContext)
      {
         notNull(id, "id may not be null");
         if (file == null && data == null)
            throw new IllegalArgumentException("Both file and data may not be null");

         mi_id = id;
         mi_file = file;
         mi_data = data;
         mi_referenceId = refId;
         mi_removal = removal;
         mi_jobId = jobId;
         mi_length = length;
         mi_mimeType = mimeType;
         mi_pubServerId = pubserverId;
         mi_meta_data = metaMap;
         mi_deliveryContext = deliveryContext;
      }

      /**
       * Returns the job ID.
       * 
       * @return the job ID.
       */
      public long getJobId()
      {
         return mi_jobId;
      }

      /**
       * Add the supplied override item. Note the child of the override items
       * are also added if there is any. The current item will take the
       * ownership of the overridden item list. This can happen when more than
       * one items publish to the same location.
       * 
       * @param overrideItem the override item, which may contains other
       *           override items. Never <code>null</code>.
       */
      public void addOverride(Item overrideItem)
      {
         if (overrideItem == null)
            throw new IllegalArgumentException("overrideItem may not be null.");

         if (mi_overrideItems == null)
            mi_overrideItems = new ArrayList<>();

         // removed the temp file which is the assembled result.
         overrideItem.getFile().delete();

         mi_overrideItems.add(overrideItem);
         if (overrideItem.mi_overrideItems != null)
         {
            mi_overrideItems.addAll(overrideItem.mi_overrideItems);
            overrideItem.mi_overrideItems = null;
         }
      }

      /**
       * Gets the override items. The publish location of the items will be
       * overridden by the current item.
       * 
       * @return the override items. It may be <code>null</code> if there is no
       *         override items.
       */
      public List<Item> getOverrideItems()
      {
         return mi_overrideItems;
      }

      /**
       * @return the file to be written, it may be <code>null</code> if
       *         {@link #getResultStream()} is not <code>null</code>.
       */
      public PSPurgableTempFile getFile()
      {
         return mi_file;
      }

      /**
       * Gets the item content in stream. Caller will take the ownership of this
       * stream. In other words, caller must close this stream afterwards.
       * 
       * @return the item content in stream, it may be <code>null</code> if (@link
       *         {@link #getFile()} is not <code>null</code>.
       */
      public InputStream getResultStream()
      {
         return new ByteArrayInputStream(mi_data);
      }

      /**
       * Sets the behavior of the {@link #release()} method.
       * 
       * @param doRelease <code>true</code> if keep the {@link #release()}
       *           original behavior; <code>false</code> if {@link #release()}
       *           do nothing.
       */
      public void setDoRelease(boolean doRelease)
      {
         mi_doRelease = doRelease;
      }

      /**
       * Releases resource, such as close result stream or "release" the
       * purgable temporary file. This should only be called after the object is
       * no longer needed.
       * <p>
       * This method do nothing if previously called
       * {@link #setDoRelease(boolean) setDoRelease(true)}
       */
      public void release()
      {
         if (!mi_doRelease)
            return;

         if (mi_file != null)
            mi_file.release();
      }

      /**
       * @return the item's id, used for error information, never
       *         <code>null</code> after ctor.
       */
      public IPSGuid getId()
      {
         return mi_id;
      }

      /**
       * @return the reference id for this item.
       */

      public long getReferenceId()
      {
         return mi_referenceId;
      }

      public long getLength()
      {
         return mi_length;
      }

      public String getMimeType()
      {
         return mi_mimeType;
      }

      public Map<String, Object> getMetaData()
      {
         return mi_meta_data;
      }

      /**
       * Indicates whether this item should be removed.
       * 
       * @return the item should be removed if <code>true</code>, otherwise it
       *         should be delivered.
       */
      public boolean isRemoval()
      {
         return mi_removal;
      }

      /**
       * Abort the item.
       * 
       * @deprecated Use {@link #release()} instead.
       */
      public void abort()
      {
         mi_file.delete();
      }

      public Long getPubServerId()
      {
         return mi_pubServerId;
      }
      public int getDeliveryContext()
      {
         return mi_deliveryContext;
      }

      /**
       * Commit the item, copy the item's temp data to the final location or
       * removes the item from the file system. If the file is not
       * <code>null</code> this is a delivery, otherwise it is a removal. The
       * actual work is done by the outer class.
       * 
       * @param jobId the job id this item belongs to
       * @param location the path to write, never <code>null</code> or empty.
       * @return the delivery result for this commit
       * @throws PSDeliveryException
       */
      public IPSDeliveryResult commit(long jobId, String location) throws PSDeliveryException
      {
         return isRemoval() ? doRemoval(this, jobId, location) : doDelivery(this, jobId, location);
      }
   }

   /**
    * Each job's information is stored in this static class. Access to this
    * class should be carefully synchronized.
    * <p>
    * The object stores the work to be done for the job, in the context of a
    * particular handler. Deliveries and removals are both held in maps where
    * the key is the path for the delivery or removal and the value is the item.
    */
   public static class JobData
   {
      /**
       * Site information.
       */
      public IPSSite m_site = null;

      public IPSPubServer m_pubServer = null;

      /**
       * Each item to be delivered has a location and a temp file stored here.
       */
      public ConcurrentHashMap<String, Item> m_deliveries = new ConcurrentHashMap<>(8, 0.9f, 1);

      /**
       * Each item to be removed has a location stored in this map.
       */
      public ConcurrentHashMap<String, Item> m_removals = new ConcurrentHashMap<>(8, 0.9f, 1);

      /**
       * Ctor.
       * 
       * @param site the site, never <code>null</code>.
       * @param pubServer the publishing server, may be <code>null</code> if not
       *           available.
       */
      public JobData(IPSSite site, IPSPubServer pubServer)
      {
         if (site == null)
         {
            throw new IllegalArgumentException("site may not be null");
         }

         m_site = site;
         m_pubServer = pubServer;
      }

   }

   /**
    * Could not delete the file on removal.
    */
   protected static final String COULD_NOT_DELETE = "Could not delete ";

   /**
    * This map contains the association from a given job id to the job data for
    * the job. Access to this map requires synchronization.
    */
   protected Map<Long, JobData> m_jobData = new ConcurrentHashMap<>(8, 0.9f, 1);

   /**
    * Deliver a content item.
    * 
    * @param item
    * @param jobId
    * @param location
    * @return the result of delivering the item, never <code>null</code>.
    * @throws PSDeliveryException
    */
   protected abstract IPSDeliveryResult doDelivery(Item item, long jobId, String location) throws PSDeliveryException;

   /**
    * Remove the single item specified by location.
    * 
    * @param jobId
    * @param item
    * @param location the location, never <code>null</code> or empty.
    * @return the result of the removal operation
    */
   protected abstract IPSDeliveryResult doRemoval(Item item, long jobId, String location);

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.delivery.IPSDeliveryHandler#init(long,
    * com.percussion.services.sitemgr.IPSSite)
    */
   public void init(long jobid, IPSSite site, IPSPubServer pubServer) throws PSDeliveryException
   {
      ms_log.debug(this.getClass().getSimpleName() + " in job (" + jobid + ") is being published "
            + (isTransactional() ? "" : "non-") + "Transactionally");

      JobData job = createJobData(jobid, site, pubServer);

      if (!isTransactional())
      {
         job.m_deliveries = null;
         job.m_removals = null;
      }

      m_jobData.put(jobid, job);

   }

   /**
    * Creates a Job Data for the given job ID and Site.
    * 
    * @param jobId the ID of the job.
    * @param site the Site that the job publishes to/from, never
    *           <code>null</code>.
    * 
    * @return the created job data, never <code>null</code>.
    */
   public JobData createJobData(long jobId, IPSSite site, IPSPubServer pubServer)
   {
      return new JobData(site, pubServer);
   }

   /**
    * Gets the job data for the specified job ID.
    * 
    * @param jobId the job ID.
    * 
    * @return the job data, it may be <code>null</code> if the job is not active
    *         or the job is finished. It is <code>null</code> after the
    *         {@link #commit(long)} call.
    */
   public JobData getJobData(long jobId)
   {
      return m_jobData.get(jobId);
   }

   /**
    * {@inheritDoc} <strong>If this method is overridden consider you must
    * cleanup: </strong> <code> try {...} finally { cleanup(jobId); } </code>
    */
   public void rollback(long jobId) throws PSDeliveryException
   {
      try
      {
         if (isTransactional())
            transactionalRollback(jobId);
      }
      finally
      {
         cleanup(jobId);
      }
   }

   /**
    * Cleans up the job data. Its OK if the job is no longer running.
    * 
    * @param jobId any long.
    */
   protected void cleanup(long jobId)
   {
      m_jobData.remove(jobId);
      if (ms_log.isDebugEnabled())
         ms_log.debug("After cleanup(" + jobId + ") - job data size: " + m_jobData.size());

   }

   /**
    * This does the actual work of {@link #rollback(long)} if it is in
    * transactional mode.
    * 
    * @param jobId the ID of the publishing job.
    * 
    * @throws PSDeliveryException if an error occurs.
    */
   protected void transactionalRollback(long jobId) throws PSDeliveryException
   {
      JobData perJob = m_jobData.get(jobId);
      if (perJob != null)
      {
         PSDeliveryException de = releaseItems(perJob.m_deliveries.values());
         de = releaseItems(perJob.m_removals.values());

         if (de != null)
         {
            throw de;
         }
      }
      ms_log.info("Released buffered files for job " + jobId);
   }

   /**
    * Invokes {@link Item#release()} on each of the specified items.
    * 
    * @param items collection of <code>Item</code> objects.
    * 
    * @return {@link PSDeliveryException} if an error occurs, <code>null</code>
    *         otherwise.
    */
   private PSDeliveryException releaseItems(Collection<Item> items)
   {
      try
      {
         for (Item item : items)
         {
            item.release();
         }
      }
      catch (Throwable th)
      {
         ms_log.error(th);

         return new PSDeliveryException(IPSDeliveryErrors.ABORT_FAILURE, th);
      }

      return null;
   }

   /**
    * Get the result for an item with <code>null</code> unpublish value.
    * 
    * @param status the delivery status for the result. Not <code>null</code>.
    * @param item the item being processed, never <code>null</code>.
    * @param jobId the job id
    * @param message the message, may be <code>null</code>
    * @return the result for the item, never <code>null</code>.
    */
   protected IPSDeliveryResult getItemResult(Outcome status, Item item, long jobId, String message)
   {
      notNull(status, "status may not be null");
      notNull(item, "item may not be null");
      return new PSDeliveryResult(status, message, item.getId(), jobId, item.getReferenceId(), item.getDeliveryContext(), null);
   }

   /**
    * Indicates if the handler is in transactional or non-transactional mode.
    * See detail {@link #isTransactional()}.
    */
   private boolean m_isTransactional = true;

   /**
    * Determines if the handler is in transactional or non-transactional mode.
    * 
    * @return <code>true</code> if the handler is in transactional mode;
    *         otherwise the handler is in non-transactional mode. Defaults to
    *         transactional mode.
    */
   public boolean isTransactional()
   {
      return m_isTransactional;
   }

   /**
    * Sets the handler to transactional or non-transactional mode.
    * 
    * @param isTransactional if it is <code>true</code>, then set the handler to
    *           transactional mode; otherwise set the handler to
    *           non-transactional mode.
    */
   public void setTransactional(boolean isTransactional)
   {
      m_isTransactional = isTransactional;
   }

   /**
    * Indicates if the handler allows assembly work items to have empty delivery
    * locations. See details {@link #isEmptyLocationAllowed()}.
    */
   private boolean m_isEmptyLocationAllowed = false;

   /**
    * Determines if empty delivery locations are allowed for assembly work
    * items.
    * 
    * @return <code>true</code> if the handler allows empty delivery locations;
    *         otherwise the handler will mark the items as failed. Defaults to
    *         not allowed.
    */
   public boolean isEmptyLocationAllowed()
   {
      return m_isEmptyLocationAllowed;
   }

   /**
    * Sets the handler to allow or disallow empty delivery locations.
    * 
    * @param emptyLocationAllowed if it is <code>true</code>, then the handler
    *           allows empty delivery locations; otherwise the handler will mark
    *           the items as failed.
    */
   public void setEmptyLocationAllowed(boolean emptyLocationAllowed)
   {
      m_isEmptyLocationAllowed = emptyLocationAllowed;
   }

   public IPSDeliveryResult deliver(IPSDeliveryItem result)
   {
      if (isTransactional())
         return transactionDeliver(result);
      else
         return nonTransactionDeliver(result);
   }

   /**
    * Creates a delivery result from the specified item.
    * 
    * @param result the specified item, not <code>null</code>.
    * @param location the location of the published item, it may be
    *           <code>null</code> or empty.
    * 
    * @return the delivery result, never <code>null</code>.
    */
   protected IPSDeliveryResult createDeliveryResult(IPSDeliveryItem result, String location)
   {
      notNull(result);
      notEmpty(location);

      try
      {
         return new PSDeliveryResult(getDeliverySuccess(), null, result.getId(), result.getJobId(),
               result.getReferenceId(), result.getDeliveryContext(), getUnpublishingInfo(result, location));
      }
      catch (Exception e)
      {
         return getExceptionResult(result, IPSDeliveryErrors.UNEXPECTED_ERROR, e);
      }
   }

   /**
    * Creates an item from the specified delivery item to be delivered or
    * published (but not removed or unpublished).
    * 
    * @param result the delivery item, not <code>null</code>.
    * 
    * @return the created item, not <code>null</code>.
    * 
    * @throws IOException if IO error occurs.
    */
   protected Item createItemForDelivery(IPSDeliveryItem result) throws IOException
   {
      notNull(result);

      if (isTransactional() || (!(result instanceof PSAssemblyWorkItem)))
      {
         return new Item(result.getId(), result.getResultFile(), result.getMetaData(), result.getMimeType(),
               result.getReferenceId(), false, result.getJobId(), result.getPubServerId(), result.getDeliveryContext());
      }

      PSAssemblyWorkItem res = (PSAssemblyWorkItem) result;
      PSPurgableTempFile file = res.isResultInFile() ? result.getResultFile() : null;
      byte[] data = res.isResultInFile() ? null : res.getResultData();

      Map<String, Object> metaMap = res.getMetaData();

      return new Item(result.getId(), file, data, metaMap, res.getResultLength(), result.getMimeType(),
            result.getReferenceId(), false, result.getJobId(), result.getPubServerId(), result.getDeliveryContext());
   }

   /**
    * The same as {@link #deliver(IPSDeliveryItem)}, except this is used in
    * non-transaction mode.
    * 
    * @param result the to be "delivered" item, not <code>null</code>.
    * 
    * @return the delivered result, not <code>null</code>.
    */
   protected IPSDeliveryResult nonTransactionDeliver(IPSDeliveryItem result)
   {
      notNull(result);

      String location;
      try
      {
         location = getPublishingLocation(result);
      }
      catch (Exception e1)
      {
         return getExceptionResult(result, IPSDeliveryErrors.UNEXPECTED_ERROR, e1);
      }

      Item item = null;

      try
      {
         prepareForDelivery(result.getJobId());
         item = createItemForDelivery(result);
         return item.commit(result.getJobId(), location);
      }
      catch (Exception e)
      {
         if (item != null)
         {
            return getItemResult(Outcome.FAILED, item, result.getJobId(), getErrorMessage(e));
         }
         else
         {
            return getExceptionResult(result, IPSDeliveryErrors.UNEXPECTED_ERROR, e);
         }
      }
      finally
      {
         releaseForDelivery(result.getJobId());
      }
   }

   /**
    * The same as {@link #deliver(IPSDeliveryItem)}, except this is used in
    * transactional mode.
    * 
    * @param result the to be "delivered" item, not <code>null</code>.
    * 
    * @return the delivered result, not <code>null</code>.
    */
   protected IPSDeliveryResult transactionDeliver(IPSDeliveryItem result)
   {
      notNull(result);

      String location;
      try
      {
         location = getPublishingLocation(result);
      }
      catch (PSNotFoundException e1)
      {
         return getExceptionResult(result, IPSDeliveryErrors.UNEXPECTED_ERROR, e1);
      }
      JobData data = m_jobData.get(result.getJobId());
      if (data == null)
      {
         return getFailureResult(result, "No data for job");
      }

      try
      {
         Item item = createItemForDelivery(result);
         // add override item if the location has been used by other item
         
         Item di = data.m_deliveries.putIfAbsent(location, item);
         
         Item ri = data.m_removals.remove(location);
         
         if (di != null && di != item)
            di.addOverride(item);

         if (ri != null)
         {
            ms_log.debug("delivery of item at location " + location
                  + " that is already set for a removal - skipping removal");
            item.addOverride(ri);
         }
      }
      catch (IOException ioe)
      {
         return getExceptionResult(result, IPSDeliveryErrors.COULD_NOT_WRITE_TEMP, ioe);
      }

      return createDeliveryResult(result, location);

   }

   /**
    * Get the unpublishing information for the given result. Override as needed.
    * 
    * @param result the result, never <code>null</code>
    * @param path the location for file publishing, may be <code>null</code> for
    *           non-file handlers
    * @return the unpublishing bytes
    * @throws Exception
    */
   protected byte[] getUnpublishingInfo(IPSDeliveryItem result, String path) throws Exception
   {
      if (result == null)
      {
         throw new IllegalArgumentException("result may not be null");
      }
      return path.getBytes("UTF8");
   }

   /**
    * Gets the message from the specified exception.
    * 
    * @param e the exception in question, assumed not <code>null</code>.
    * 
    * @return the error message, not blank.
    */
   private String getErrorMessage(Throwable e)
   {
      if (StringUtils.isNotBlank(e.getLocalizedMessage()))
      {
         return e.getLocalizedMessage();
      }
      else
      {
         return "Failure due to: " + e.getClass().getCanonicalName() + ", " + "check the console log for details";
      }
   }

   /**
    * Handles exceptions during commit the specified item.
    * 
    * @param rval it is used to collect the committed items, assume not
    *           <code>null</code>.
    * @param item the item had failures during commit, assume not
    *           <code>null</code>.
    * @param jobId the job id of the current publishing run.
    * @param e the exception caught while committing the above item. Assumne not
    *           <code>null</code>.
    * @param errorHit the error hit. Assume not <code>null</code>.
    */
   private void handleCommitError(Collection<IPSDeliveryResult> rval, Item item, long jobId, Throwable e,
         String errorHit)
   {
      ms_log.error(errorHit, e);
      if (ms_log.isDebugEnabled())
         ms_log.debug("Commit error for item: " + item.getId());

      IPSDeliveryResult result;
      String message = getErrorMessage(e);
      result = getItemResult(Outcome.FAILED, item, jobId, message);
      rval.add(result);

      if (item.getOverrideItems() == null)
         return;

      // set the status for all override items
      for (Item override : item.getOverrideItems())
      {
         if (ms_log.isDebugEnabled())
            ms_log.debug("Commit error for item: " + override.getId());

         result = getItemResult(Outcome.FAILED, override, jobId, message);
         updateItemStatus(result);
         rval.add(result);
      }
   }

   /**
    * Prepares for delivery. This should be called before (indirectly) calling
    * one or more {@link Item#commit(long, String)}. Defaults to do nothing and
    * return <code>null</code>. However, the derived class may overwrite this to
    * establish connection to the remote server for example.
    * {@link #releaseForDelivery(long)} must be called afterwards if there is no
    * error occurs.
    * 
    * @param jobId the job ID.
    * 
    * @return <code>null</code> if it is successful; otherwise it returns error
    *         results if error occurs.
    * 
    * @throws PSDeliveryException if error occurs.
    */
   protected Collection<IPSDeliveryResult> prepareForDelivery(long jobId) throws PSDeliveryException
   {
      return null;
   }

   /**
    * This is called after calling {@link #prepareForDelivery(long)} and
    * completed a set of calls to {@link Item#commit(long, String)}. Defaults to
    * do nothing. However, the derived class may use this to release resources,
    * such as connection to remote server.
    * 
    * @param jobId the job ID.
    */
   protected void releaseForDelivery(long jobId)
   {
   }

   /**
    * {@inheritDoc} If you override this method you must call
    * {@link #cleanup(long)} yourself in a finally blog.
    */
   public Collection<IPSDeliveryResult> commit(long jobId) throws PSDeliveryException
   {
      try
      {
         return doCommit(jobId);
      }
      finally
      {
         cleanup(jobId);
      }
   }

   /**
    * This process the actual operation of {@link #commit(long)}. The caller
    * {@link #commit(long)} is responsible to call {@link #cleanup(long)}.
    */
   private Collection<IPSDeliveryResult> doCommit(long jobId) throws PSDeliveryException
   {
      if (!isTransactional())
         return Collections.emptyList();

      try
      {
         Collection<IPSDeliveryResult> errorResults = prepareForDelivery(jobId);
         if (errorResults != null)
            return errorResults;

         return transactionCommit(jobId);
      }
      finally
      {
         releaseForDelivery(jobId);
      }
   }

   /**
    * The same as {@link #commit(long)}, except this is used in transactional
    * mode only.
    * 
    * @param jobId the publishing job ID.
    * 
    * @return the results, never <code>null</code>, but may be empty.
    * 
    * @throws PSDeliveryException if an error occurs.
    */
   protected Collection<IPSDeliveryResult> transactionCommit(long jobId) throws PSDeliveryException
   {
      PSStopwatch watch = new PSStopwatch();
      watch.start();
      if (ms_log.isDebugEnabled())
         ms_log.debug("Committing buffered files for job " + jobId + " ...");

      Collection<IPSDeliveryResult> rval = new ArrayList<>();

      JobData perJob = m_jobData.get(jobId);
      if (perJob != null)
      {
         rval.addAll(commitResults(jobId, perJob.m_deliveries, "Problem during delivery"));

         rval.addAll(commitResults(jobId, perJob.m_removals, "Problem during removal"));

         removeEmptyDirectories(perJob.m_removals.keySet());
      }

      watch.stop();
      if (ms_log.isDebugEnabled())
      {
         ms_log.debug("Committed buffered " + rval.size() + " files for job " + jobId + ". Elapsed time: "
               + watch.toString());
      }

      return rval;
   }
   
   @Override
   public void updateItemStatus(List<IPSDeliveryResult> results)
   {
      for(IPSDeliveryResult result : results)
      {
         ms_deliveryManager.updateItemState(result);
      }
      
   }
   
   @Override
   public void updateItemStatus(IPSDeliveryResult result)
   {
         JobData data = m_jobData.get(result.getJobId());
         final ItemState state = PSPublishHandler.OUTCOME_STATE.get(result.getOutcome());
         PSPubItemStatus status = new PSPubItemStatus(
               result.getReferenceId(), result.getJobId(), data.m_pubServer.getServerId(), result.getDeliveryContext(), state);
         if (result.getUnpublishData() != null)
         {
            status.setUnpublishingInformation(result.getUnpublishData());
         }
         if (StringUtils.isNotBlank(result.getFailureMessage()))
         {
            status.addMessage(result.getFailureMessage());
         }
         status.setSiteId(data.m_site.getGUID());
        
         IPSRxPublisherService pubService = PSRxPublisherServiceLocator.getRxPublisherService();
         pubService.updateItemState(status);      
     
         result.updateSent();
   }
    
  

   /**
    * Removes empty directories after removed the specified files. This does
    * nothing if {@link #isEmptyLocationAllowed()} is true.
    * 
    * @param fileLocations the path of the removed files, not <code>null</code>
    *           may be empty.
    */
   protected void removeEmptyDirectories(Set<String> fileLocations)
   {
      if (isEmptyLocationAllowed())
         return;

      Set<String> directories = getParentDirectories(fileLocations);
      for (String dir : directories)
      {
         removeEmptyDirectory(dir);
      }
   }

   /**
    * Gets the parent directories for the specified file paths.
    * 
    * @param filePaths the file paths in question, not <code>null</code>, may be
    *           empty.
    * 
    * @return the parent directory paths, not <code>null</code>, may be empty.
    */
   private Set<String> getParentDirectories(Set<String> filePaths)
   {
      Set<String> dirLocations = new HashSet<>();
      for (String floc : filePaths)
      {
         File f = new File(floc);
         String parent = f.getParent();
         dirLocations.add(parent);
      }
      return dirLocations;
   }

   /**
    * Removes the specified directory if it is empty. It does nothing by
    * default. It is the responsibility of the derived classes to overwrite this
    * method.
    * 
    * @param dir the directory path/name in question.
    */
   protected void removeEmptyDirectory(String dir)
   {
      // defaults to do nothing.
   }

   /**
    * Commits the supplied results for the given job.
    * 
    * @param jobId the ID of the committed job.
    * @param results the results to be committed, which may be the to be removed
    *           results or delivered results. Assumed not <code>null</code>.
    * @param errorMessage the error message to be used if error occurs. Assumed
    *           not <code>null</code>.
    * 
    * @return the committed results, never <code>null</code>, but may be empty.
    */
   private Collection<IPSDeliveryResult> commitResults(long jobId, Map<String, Item> results, String errorMessage)
   {
      PSPublishingJob job = PSRxPubServiceInternalLocator.getRxPublisherService().getPublishingJob(jobId);

      Collection<IPSDeliveryResult> rval = new ArrayList<>();
      for (String location : results.keySet())
      {
         if (job.isCanceled())
         {
            break;
         }

         Item item = results.get(location);
         try
         {
            rval.addAll(commitItems(item, jobId, location));
         }
         catch (PSDeliveryException th)
         {
            handleCommitError(rval, item, jobId, th, errorMessage);
         }
         catch (Throwable e)
         {
            handleCommitError(rval, item, jobId, e, errorMessage);
         }
      }
      return rval;
   }

   /**
    * Commit the supplied items. Note the supplied item may contain a list of
    * override items.
    * 
    * @param item the to be committed item, assumed not <code>null</code>.
    * @param jobId the job ID
    * @param location the published location, assumed not <code>null</code> or
    *           empty.
    * 
    * @return a list of delivery results, never <code>null</code> or empty.
    * 
    * @throws PSDeliveryException if error occurs.
    */
   private List<IPSDeliveryResult> commitItems(Item item, long jobId, String location) throws PSDeliveryException
   {
      IPSDeliveryResult result = item.commit(jobId, location);
      if (item.getOverrideItems() == null)
         return Collections.singletonList(result);

      List<IPSDeliveryResult> reList = new ArrayList<>();
      reList.add(result);
      String message;
      for (Item override : item.getOverrideItems())
      {
         if (result.getOutcome().equals(Outcome.DELIVERED))
         {
            message = MessageFormat.format(LOCATION_OVERRIDDEN, location, override.mi_id.getUUID(), item.getId()
                  .getUUID());
            ms_log.warn(message);
         }
         else
         {
            message = result.getFailureMessage();
         }

         IPSDeliveryResult v = new PSDeliveryResult(result.getOutcome(), message, override.getId(), jobId,
               override.getReferenceId(), item.getDeliveryContext(), result.getUnpublishData());
         
         updateItemStatus(v);

         reList.add(v);
      }
      
      return reList;
   }
   
   
   /**
    * Message when a file has already been published to a given location in the
    * same job.
    */
   private static final String LOCATION_OVERRIDDEN = "Item id={1} was overridden by item id={2} at published location {0}.";

   /**
    * Make every item fail to a global problem.
    * 
    * @param jobId the job id. Note that the job must be synchronized in the
    *           caller.
    * @param message the message to return.
    * @return the results, never <code>null</code>.
    */
   protected Collection<IPSDeliveryResult> failAll(long jobId, String message)
   {
      JobData data = m_jobData.get(jobId);
      Collection<IPSDeliveryResult> results = new ArrayList<>();
      for (Item item : data.m_deliveries.values())
      {
         results.add(getItemResult(Outcome.FAILED, item, jobId, message));
         if (item.getOverrideItems() == null)
            continue;

         // set the status for all override items
         for (Item override : item.getOverrideItems())
         {
            results.add(getItemResult(Outcome.FAILED, override, jobId, message));
         }
      }

      return results;
   }

   /**
    * Get a failure result from a delivery attempt.
    * 
    * @param result the assembly result, never <code>null</code>.
    * @param message the message, never <code>null</code> or empty.
    * @return the delivery result, never <code>null</code>.
    */
   protected IPSDeliveryResult getFailureResult(IPSDeliveryItem result, String message)
   {
      if (result == null)
      {
         throw new IllegalArgumentException("result may not be null");
      }
      if (StringUtils.isBlank(message))
      {
         throw new IllegalArgumentException("message may not be null or empty");
      }
      return new PSDeliveryResult(Outcome.FAILED, message, result.getId(), result.getJobId(), result.getReferenceId(), result.getDeliveryContext(),
            null);
   }

   /**
    * Get the exception return value for a delivery.
    * 
    * @param result the assembly result, never <code>null</code>.
    * @param th the exception, never <code>null</code>
    * @return the delivery result, never <code>null</code>.
    */
   protected IPSDeliveryResult getExceptionResult(IPSDeliveryItem result, int errorCode, Throwable th)
   {
      if (result == null)
      {
         throw new IllegalArgumentException("result may not be null");
      }
      if (th == null)
      {
         throw new IllegalArgumentException("th may not be null");
      }
      String message = StringUtils.isBlank(th.getLocalizedMessage()) ? th.getClass().getName() : th
            .getLocalizedMessage();
      ms_log.error(message, th);

      PSDeliveryException e = new PSDeliveryException(errorCode, th, message);
      return new PSDeliveryResult(Outcome.FAILED, e.getLocalizedMessage(), result.getId(), result.getJobId(),
            result.getReferenceId(), result.getDeliveryContext(), null);
   }

   /**
    * Calculate the path for the given delivery path and site. If there is no
    * delivery path, a pseudo path is created that is only intended to create a
    * unique reference.
    * 
    * @param result the delivery item, never <code>null</code>.
    * @return the calculated path.
    * @throws PSNotFoundException if failed to load Site object.
    */
   protected String getPublishingLocation(IPSDeliveryItem result) throws PSNotFoundException
   {
      if (result == null)
      {
         throw new IllegalArgumentException("result may not be null");
      }
      String deliveryPath = result.getDeliveryPath();
      String root = getPublishRoot(result);
      root = StringUtils.defaultString(root);
      if (StringUtils.isBlank(deliveryPath))
      {
         deliveryPath = "ref" + result.getReferenceId();
      }
      if (root == null)
         return deliveryPath;

      return combineRootAndPath(root, deliveryPath);
   }

   private String getPublishRoot(IPSDeliveryItem result) throws PSNotFoundException {
      if (result.getPubServerId() != null)
      {
         try
         {
            IPSPubServer pubServer = getPubServerDao().findPubServer(result.getPubServerId());
            return getPublishRoot(pubServer, null);
         }
         catch (Exception e)
         {
            IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
            throw new PSNotFoundException(gmgr.makeGuid(result.getPubServerId(), PSTypeEnum.PUBLISHING_SERVER));
         }
      }
      else
      {
         IPSGuid siteId = result.getSiteId();
         IPSSiteManager sitem = PSSiteManagerLocator.getSiteManager();
         IPSSite site = sitem.loadSite(siteId);
         return getPublishRoot(null, site);
      }
   }

   /**
    * Gets the pub-server service, lazy load.
    * 
    * @return pub-server service, never <code>null</code>.
    */
   private IPSPubServerDao getPubServerDao()
   {
      if (m_pubServerDao == null)
         m_pubServerDao = PSSiteManagerLocator.getPubServerDao();

      return m_pubServerDao;
   }

   private IPSPubServerDao m_pubServerDao = null;

   /**
    * Gets the publish root (file path) from either publish-server (if not
    * <code>null</code>) or site.
    * 
    * @param pubServer the publish-server. It may be <code>null</code>, then the
    *           "site" object cannot be <code>null</code>.
    * @param site the site. It may be <code>null</code>, then the "pubServer"
    *           cannot be <code>null</code>.
    * @return the publish root, never <code>null</code>.
    */
   protected String getPublishRoot(IPSPubServer pubServer, IPSSite site)
   {
      if (pubServer == null && site == null)
         throw new IllegalArgumentException("both pubServer and site cannot be null.");

      if (pubServer != null)
         return pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_FOLDER_PROPERTY, "");
      else
         return site.getRoot();
   }

   /**
    * Combine the root and relative path
    * 
    * @param root the root, assumed not <code>null</code>.
    * @param path the relative path to the root, assumed not <code>null</code>.
    * @return the combined path, never <code>null</code>.
    */
   private String combineRootAndPath(String root, String path)
   {
      notNull(root);
      notNull(path);

      if (root.endsWith("/"))
      {
         path = path.startsWith("/") ? root + path.substring(1) : root + path;
      }
      else
      {
         path = path.startsWith("/") ? root + path : root + "/" + path;
      }
      return path;
   }

   /**
    * Return an absolute path in unix format. This method converts all
    * backslashes to forward slashes, remove multiple slashes and removes any
    * leading drive letter in the path.
    * <p>
    * This is used for ftp and sftp file transfer in an attempt to provide a
    * canonical path that will not cause compatibility issues.
    * <p>
    * This method is not used by this class, but is defined here to allow it to
    * be common code for both file transfer classes.
    * 
    * @param path input path, never <code>null</code> or empty.
    * @return the canonicalized path, never <code>null</code>
    */
   protected String canonicalPath(String path)
   {
      if (StringUtils.isBlank(path))
      {
         throw new IllegalArgumentException("path may not be null");
      }
      final String slash = "/";
      String abs = path.replace('\\', '/');
      abs = abs.replaceAll("//+", slash);
      abs = abs.replaceAll("^\\w:/", slash);
      return abs;
   }

   public IPSDeliveryResult remove(IPSDeliveryItem item)
   {
      if (item.getResultData() == null)
      {
         throw new RuntimeException("No unpublishing information " + "available for reference id: "
               + item.getReferenceId() + " and content item: " + item.getId());
      }
      try
      {
         if (isTransactional())
            return transactionRemove(item);
         else
            return nonTransactionRemove(item);
      }
      catch (Exception e)
      {
         ms_log.error("Problem removing item", e);
         return getFailureResult(item, e.getLocalizedMessage());
      }
   }

   /**
    * The same as {@link #remove(IPSDeliveryItem)}, except this is used in
    * non-transactional mode only.
    * 
    * @param item the to be removed item, not <code>null</code>.
    * 
    * @return the result, not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   protected IPSDeliveryResult nonTransactionRemove(IPSDeliveryItem item) throws Exception
   {
      notNull(item);
      prepareForDelivery(item.getJobId());

      try
      {
         String location = getUnpublishingLocation(item);
         Item rmItem = new Item(item.getId(), item.getResultFile(), item.getMetaData(), item.getMimeType(),
               item.getReferenceId(), true, item.getJobId(), item.getPubServerId(), item.getDeliveryContext());
         return rmItem.commit(item.getJobId(), location);
      }
      finally
      {
         releaseForDelivery(item.getJobId());
      }
   }

   /**
    * The same as {@link #remove(IPSDeliveryItem)}, except this is used in
    * transactional mode only.
    * 
    * @param item the to be removed item, not <code>null</code>.
    * 
    * @return the result, not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   protected IPSDeliveryResult transactionRemove(IPSDeliveryItem item) throws Exception
   {
      notNull(item);

      String location = getUnpublishingLocation(item);
      JobData data = m_jobData.get(item.getJobId());

      Item rmItem = new Item(item.getId(), item.getResultFile(), null, item.getMimeType(), item.getReferenceId(), true,
            item.getJobId(), item.getPubServerId(), item.getDeliveryContext());

      Item di = data.m_deliveries.get(location);
      
      if (di != null)
      {
         di.addOverride(rmItem);
         // We are delivering to this path so get rid of the removals and add them to the delivery
         // overrides so the status is returned when the delivery item is delivered or fails.
         Item ri = data.m_removals.remove(location);
         if (ri != null)
         {
            di.addOverride(ri);
            List<Item> overrides = di.getOverrideItems();
            if (overrides != null)
            {
               for (Item override : overrides)
               {
                  di.addOverride(override);
               }
            }
         }
         ms_log.debug("Removal of item at location " + location + " that is also being delivered - skipping removal");
      }
      else
      {
      Item ri = data.m_removals.putIfAbsent(location,rmItem);
      if (ri!=null && ri != rmItem)
      {
            ms_log.debug("Duplicate removal at location: " + location + ". Existing id = " + ri.getId().getUUID()
               + " new id =" + item.getId().getUUID());
          ri.addOverride(rmItem);
      }
      }

      return new PSDeliveryResult(getDeliverySuccess(), null, item.getId(), item.getJobId(), item.getReferenceId(), item.getDeliveryContext(),
            null);
   }

   /**
    * Get the unpublishing location to use when indexing the removals. The
    * information <i>may</i> also be used by the delivery handler directly.
    * 
    * @param item the item, never <code>null</code>
    * @return a string representing the location, which may be the actual path
    *         or simply an abstract path, never <code>null</code> or empty.
    * @throws Exception
    */
   protected String getUnpublishingLocation(IPSDeliveryItem item) throws Exception
   {
      return new String(item.getResultData(), "UTF-8");
   }

   /**
    * The outcome for the successful delivery. Never <code>null</code>.
    */
   private Outcome getDeliverySuccess()
   {
      return isTransactional() ? Outcome.PREPARED_FOR_DELIVERY : Outcome.DELIVERED;
   }

   /**
    * 
    * @param pubServer - IPSPubServer represents the pubServer
    * @param site - IPSSite represents the site to be published
    * @return <code>true</code> always
    */
   public boolean checkConnection(IPSPubServer pubServer, IPSSite site)
   {
      return true;
   }
}
