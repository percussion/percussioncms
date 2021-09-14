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

package com.percussion.search;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.IPSEditorChangeListener;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSEditorChangeEvent;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.objectstore.IPSCataloger;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSBinaryValue;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentType;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemChildLocator;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSLocalCataloger;
import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.error.PSException;
import com.percussion.search.data.PSSearchIndexQueueItem;
import com.percussion.search.lucene.PSSearchUtils;
import com.percussion.security.PSThreadRequestUtils;
import com.percussion.server.IPSHandlerInitListener;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.data.PSNodeDefinition;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.util.PSIteratorUtils;
import com.percussion.util.PSStopwatch;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import javax.annotation.concurrent.ThreadSafe;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Persisted queue for events which require reindexing. All events are queued
 * both in memory and in the repository so that in the event of a server crash
 * the unprocessed events remaining in the queue can be restored and processed
 * so that the index may be maintained. Queued events are processed
 * asynchronously in a separate thread, and are serialized even if the queue
 * isn't running. Events with the highest priority are processed first (these
 * are events with a lower valued priority indicator, as the lower the value of
 * the indicator, the higher the priority).
 * <p>
 * This class follows the singleton pattern, and a reference to the single
 * instance may be obtained using {@link #getInstance()}.
 */
@ThreadSafe
public class PSSearchIndexEventQueue implements IPSEditorChangeListener, IPSHandlerInitListener
{
   private static final int EVENT_WAIT_TIME_MS = 10000;

   public static final String QUEUE_ALREADY_PAUSED = "The Search Index Queue is already paused";
   
   /**
    * Private ctor to enforce singleton pattern.
    */
   private PSSearchIndexEventQueue()
   {
   }

   /**
    * Obtain the single instance of this class. If one has not yet been created,
    * it will be created and returned.
    *
    * @return The instance, never <code>null</code>.
    */
   public static synchronized PSSearchIndexEventQueue getInstance()
   {
      if (ms_instance == null)
      {
         synchronized (PSSearchIndexEventQueue.class)
         {
            if (ms_instance == null)
            {
               ms_instance = new PSSearchIndexEventQueue();

            }


         }
      }

      if(PSServer.isInitialized()) {
         PSSearchConfig config = PSServer.getServerConfiguration().getSearchConfig();
         ms_searchEnabled = config.isFtsEnabled();
      }

      return ms_instance;
   }

   /**
    * Determine if the singleton instance of the index queue has been
    * initialized. If the server is running, then this method should return
    * <code>true</code>, used to ensure unit tests do not fail.
    * 
    * @return <code>true</code> if the queue is initialized, <code>false</code>
    *         if not.
    */
   public static boolean isInitialized()
   {
      return ms_instance != null;
   }

   /**
    * Returns the number of queue events in the repository. This is the number
    * of events waiting to be processed because every item in the in memory
    * queue has a copy in the DB. Once it has been indexed it is removed from
    * the in memory queue and the repository. There may be more items in the
    * repository than in the memory queue.
    *
    * @return A value of 0 or higher that indicates how many entries are
    *         currently in the queue.
    */
   public int size()
   {
      return m_queueService.getEventCount();
   }

   /**
    * Causes any persisted events to be restored from the repository, and starts
    * the thread that processes queued events.
    *
    * @throws IllegalStateException if the queue has already been started or is
    *            shutting down.
    */
   public void start()
   {

      synchronized (m_runMonitor)
      {
         if (m_run)
            throw new IllegalStateException("Index queue is already running");

         if (m_shutdown)
            throw new IllegalStateException("Index queue is shutting down");

         PSConsole.printMsg(QUEUE_THREAD_NAME, "Initializing index queue");

         m_queueThread = new Thread(QUEUE_THREAD_NAME)
         {
            public void run()
            {
               PSThreadRequestUtils.initServerThreadRequest();
               while (!m_shutdown)
               {

                  PSRequest request = null;
                  try
                  {
                     request = PSThreadRequestUtils.getPSRequest();
                     processNextEventSet(INDEX_OPTIMIZE_WAIT, request);
                  }
                  catch (InterruptedException e)
                  {
                     Thread.currentThread().interrupt();
                     break;
                  }
                  catch (Throwable t)
                  {
                     PSConsole.printMsg(QUEUE_THREAD_NAME, t);
                     if (t instanceof ThreadDeath)
                        break;
                  }
                  finally
                  {
                     //Bug: request is null guaranteed to be dereferenced in com.percussion.search.PSSearchIndexEventQueue$1.run() on exception path
                     if(request != null){
                     request.release();
                  }
               }
               }

               PSRequestInfo.resetRequestInfo();

               // we've finished, so notify the shutdown method if it's waiting
               m_run = false;
               synchronized (m_runMonitor)
               {
                  m_runMonitor.notifyAll();
               }
            }
         };

         m_queueThread.setDaemon(true);

         m_queueThread.start();

         m_run = true;

         notifyStatusChange();
      }
   }

   private IPSNotificationService getNotificationService()
   {
      if (notificationService == null)
      {
         synchronized (nsLock)
         {
            if (notificationService == null)
            {
               notificationService = PSNotificationServiceLocator.getNotificationService();
            }
         }
      }

      return notificationService;
   }

   private void notifyStatusChange()
   {
      m_queueService.pollNow();
      getNotificationService().notifyEvent(new PSNotificationEvent(EventType.SEARCH_INDEX_STATUS_CHANGE, null));
   }

   /**
    * Stops processing queued events and shuts down the queue. Will not return
    * until it has completed any work in progress. Calling this multiple times
    * is safe.
    */
   public void shutdown()
   {
      synchronized (m_shutdownMonitor)
      {
         if (m_shutdown)
            return;

         // set shutdown flag and wait till the queue is shutdown
         m_shutdown = true;
         m_queueService.shutdown();

         notifyStatusChange();
      }

      PSConsole.printMsg(QUEUE_THREAD_NAME, "Shutting down index queue");

      // now wait for queue processing thread to finish any current work
      synchronized (m_runMonitor)
      {
         while (m_run)
         {
            try
            {
               m_runMonitor.wait(5000L);
            }
            catch (InterruptedException e)
            {
               Thread.currentThread().interrupt();
            }
         }

         m_shutdown = false;
         notifyStatusChange();
      }
   }

   /**
    * Pauses the processing of the queued events. Events may continue to be
    * Multiple calls will queue up all requests.  resume must be called in a finally
    * block or index may never resume.
    * 
    * @return Any results, never <code>null</code>.
    */
   public boolean pause()
   {
      int pauseCount = m_pausedCount.incrementAndGet();
      boolean wasPaused = pauseCount > 1;

      String result = QUEUE_ALREADY_PAUSED;
      if (!wasPaused)
      {
         result = "Pausing Search Index Queue processing with " + size() + " entries in the queue";
         m_logger.info(result);
         notifyStatusChange();
      }
      else 
      {
         m_logger.debug("Pause called when already called with " +pauseCount+ " pause requests");
      }

      return !wasPaused;
   }

   /**
    * Resumes processing of queued events if the queue is paused (see
    * {@link #pause()}). If this method is called and the queue is not paused,
    * it is a no-op and a warning is logged.
    * 
    * @param warn <code>true</code> to log a warning if the queue processing is
    *           already running, <code>false</code> to suppress these warnings.
    * 
    * @return Any results, never <code>null</code>.
    */
   public boolean resume()
   {
      int pausedCount  = m_pausedCount.decrementAndGet();
    
      if (pausedCount==0)
      {
         m_logger.info("Resuming Search Index Queue processing with " + size() + " entries in the queue");
         notifyStatusChange();
      } else if (pausedCount<0)
      {
         m_logger.info("Indexer resume called when already resumed");
         notifyStatusChange();
         m_pausedCount.set(0);
      }
      else
         m_logger.debug("Resume Search Index Queue called still "+pausedCount+ " resumes until indexer restarts");
      
      return pausedCount<=0;
   }

   /**
    * Returns the current status
    * 
    * @return The status, either "Running", "Paused", "Stopped", or
    *         "Shutting down"
    */
   public String getStatus()
   {
      if (m_shutdown)
         return "Shutting down";
      else if (!m_run)
         return "Stopped";
      else if (m_pausedCount.get()>0)
         return "Paused";
      else
         return "Running";
   }

   /**
    * Queue's the supplied change event for asynchronous processing. If full
    * text search is not enabled, the queue will not store events for later
    * indexing.
    *
    * @param changeEvent The event to queue, may not be <code>null</code>.
    */
   public void queueEvent(PSSearchEditorChangeEvent changeEvent)
   {
      if (ms_searchEnabled == false)
      {
         return;
      }

      if (changeEvent == null)
         throw new IllegalArgumentException("changeEvent may not be null");

      // make sure its a valid contenttype/child for indexing
      if (!canIndexContentType(changeEvent.getContentTypeId(), changeEvent.getChildId()))
      {
         return;
      }
      
      persistEvent(changeEvent);

      getNotificationService().notifyEvent(
            new PSNotificationEvent(EventType.SEARCH_INDEX_ITEM_QUEUED, Integer.valueOf(1)));
   }

   /**
    * Causes the current revision of the specified item to be queued for
    * (re)indexing. Calls {@link #queueEvent(PSSearchEditorChangeEvent)} with
    * the appropriate event, and the actual indexing will occur asynchronously.
    * Current index entries for this item are deleted first. These events will
    * have a lower priority and be processed after any index events
    * automatically submitted by the server due to actions taken on an item.
    *
    * @param locator The locator of the item to index, the revision is not
    *           considered ({@link PSLocator#useRevision()} may return
    *           <code>true</code>). May not be <code>null</code>.
    *
    * @return <code>1</code> if the item was queued for indexing, <code>0</code>
    *         if the item did not exist, and <code>-1</code> if the item was
    *         valid but it's content type was not valid for indexing.
    */
   public int indexItem(PSLocator locator)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      int result = 0;

      PSComponentSummary sum = getComponentSummary(locator.getId());
      if (sum != null)
      {
         // This is a special case for the pig image that should never be
         // indexed.
         if (sum.getName().startsWith("percussion_test_image_donotuse"))
            return -1;

         result = -1;
         long contentTypeId = sum.getContentTypeId();

         // see if we have a valid content type.
         if (canIndexContentType(contentTypeId))
         {
            result = 1;

            // get current revision
            int currentRevision = sum.getCurrentLocator().getRevision();

            PSSearchEditorChangeEvent event = new PSSearchEditorChangeEvent(PSEditorChangeEvent.ACTION_REINDEX,
                  locator.getId(), currentRevision, contentTypeId, true);
            event.setPriority(REINDEX_PRIORITY);
            queueEvent(event);
         }
      }

      return result;
   }

   /**
    * Causes the current revision of all items for the specified content type to
    * be queued for (re)indexing. See {@link #indexItem(PSLocator)} for more
    * info.
    *
    * @param contentTypeId Must be a valid content type id for an active content
    *           editor application. A search for all items of this type will be
    *           performed, and a call to
    *           {@link #queueEvent(PSSearchEditorChangeEvent)} will be made for
    *           each item returned to queue it for indexing.
    *
    * @return The number of items queued for indexing, or <code>-1</code> if the
    *         content type was not valid for indexing.
    *
    * @throws PSSearchException if there is an error searching for all items of
    *            the specified content type.
    */
   public int indexContentType(int contentTypeId) throws PSSearchException
   {
      try
      {
         // validate the content type
         if (!canIndexContentType(contentTypeId))
         {
            return (-1);
         }

         IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
         IPSGuid contentTypeGuid = guidMgr.makeGuid(contentTypeId, PSTypeEnum.NODEDEF);

         IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();

         PSNodeDefinition nodeDef = PSContentTypeHelper.findNodeDef(contentTypeGuid);

         Collection<IPSGuid> guids = mgr.findItemIdsByNodeDefinition(nodeDef);

         // Tech Support requested to leave this logging in and on all the time
         m_logger.info("Inserting " + Integer.toString(guids.size()) + " content items for content type "
               + nodeDef.getName() + " (" + Integer.toString(contentTypeId) + ")" + " into Search Index Queue.");

         int results = 0;
         for (IPSGuid guid : guids)
         {
            int contentId = ((PSLegacyGuid) guid).getContentId();
            if (contentId != PSFolder.ROOT_ID)
            {
               boolean isLast = results == guids.size() - 1;
               PSSearchEditorChangeEvent event = new PSSearchEditorChangeEvent(PSEditorChangeEvent.ACTION_REINDEX,
                     contentId, ((PSLegacyGuid) guid).getRevision(), contentTypeId, isLast);
               event.setPriority(REINDEX_PRIORITY);
               queueEvent(event);
            }
            results++;
         }
         return results;
      }
      catch (RepositoryException e)
      {
         String msg = "Error searching for items for reindexing: ";

         m_logger.error(msg, e);
         // internal search should not fail for any reason
         throw new RuntimeException(msg, e);
      }
   }

   /**
    * Set a modifier to process fields values loaded for an item before the
    * values are submitted to the indexer.
    * 
    * @param fieldValueModifier The modifier, will replace any existing
    *           modifier, may be <code>null</code> to clear the value.
    */
   public void setFieldValueModifier(IPSFieldValueModifier fieldValueModifier)
   {
      m_fieldValueModifier = fieldValueModifier;
   }

   /**
    * Convenience method calls {@link #canIndexContentType(int, int)
    * canIndexContentType(contentTypeId, -1)}.
    */
   private boolean canIndexContentType(long contentTypeId)
   {
      return canIndexContentType(contentTypeId, -1);
   }

   /**
    * Determines if the supplied content type id (or optional child type id) is
    * indexable. It must have a running "visible" content editor, and it must be
    * enabled for searching.
    * <p>
    * If the content type is not currently running, this method will wait 5
    * seconds for it to restart (checking every second). This prevents the loss
    * of a bunch of items if the editor is changed while several of a type are
    * queued.
    * <p>
    * At this time the folder content type is not visible, as well as any
    * inactive content editor.
    *
    * @param contentTypeId the content type id to check.
    * @param childId The child id to check, <code>-1</code> to check parent type
    *           only, otherwise combination of the parent and child are checked.
    *
    * @return <code>true</code> if it can be indexed, <code>false</code> if not.
    */
   private boolean canIndexContentType(long contentTypeId, int childId)
   {
      boolean canIndex = false;
      int retryCount = 5;
      /*
       * get visible content types so we don't index changes for inactive
       * content types
       */
      while (retryCount > 0)
      {
         retryCount--;
         PSItemDefManager mgr = PSItemDefManager.getInstance();

         long visTypes[] = mgr.getContentTypeIds(PSItemDefManager.COMMUNITY_ANY);
         long[] allowedTypes = new long[visTypes.length + 1];
         System.arraycopy(visTypes, 0, allowedTypes, 0, visTypes.length);
         // allow folders to be indexed
         allowedTypes[allowedTypes.length - 1] = PSFolder.FOLDER_CONTENT_TYPE_ID;
         for (int i = 0; i < allowedTypes.length; i++)
         {
            if (allowedTypes[i] == contentTypeId)
            {
               // found it so end outer loop
               retryCount = 0;
               try
               {
                  PSItemDefinition itemDef = mgr.getItemDef(contentTypeId, -1);
                  if (childId == -1)
                     canIndex = itemDef.isUserSearcheable();
                  else
                     canIndex = itemDef.isUserSearcheable(childId);

               }
               catch (PSInvalidContentTypeException e1)
               {
                  // can't happen, since the mgr gave us the id
               }

               break;
            }
         }
         if (retryCount > 0)
         {
            try
            {
               Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
               Thread.currentThread().interrupt();
            }
         }
      }

      return canIndex;
   }

   /**
    * Get the component summary for the supplied content id.
    *
    * @param contentId The content id for the item to load.
    *
    * @return The summary, or <code>null</code> if none found.
    */
   private PSComponentSummary getComponentSummary(int contentId)
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      return cms.loadComponentSummary(contentId);
   }

   /**
    * Retrieves the next set of events from the queue and processes them. Events
    * are queued by content id, and all events queued for a single content id
    * are coalesced into a set and processed together. Will wait on an empty
    * queue for a limited time before returning. Events with the highest
    * priority are processed first (this means the actual value of their
    * priority is lower). Events are coalesced and processed with those of a
    * higher priority.
    *
    * @param timeOut The number of milliseconds to wait before returning. Pass
    *           zero to wait indefinitely, assumed not negative.
    * @param request The request used to process the queue. Assume not
    *           <code>null</code>.
    *
    * @return <code>true</code> if anything was found to process,
    *         <code>false</code> if not.
    *
    * @throws InterruptedException If interrupted while waiting on the queue.
    * @throws PSSearchException if there are any errors updating the index.
    * @throws PSCmsException if there is an error loading the item.
    * @throws PSException if there are any other errors.
    */
   @SuppressWarnings("unchecked")
   private void processNextEventSet(long timeOut, PSRequest request) throws InterruptedException, PSSearchException,
         PSCmsException, PSException
   {
      List<PSSearchIndexQueueItem> eventSet = getNextEventSet(timeOut);
      if (eventSet != null && !eventSet.isEmpty())
      {
         List<String> queueIdList = new ArrayList<String>();

         int loadFlags = 0;
         boolean indexParent = false;
         boolean deleteParent = false;
         boolean reindex = false;
         Set binaryFields = new HashSet();
         Map childRows = new HashMap();
         List childDeletes = new ArrayList();
         long contentTypeId = -1;
         boolean commit = false;
         int revision = -1;
         boolean deleteAll = false;
         int contentid = eventSet.get(0).getContentId();
         
         m_logger.debug("Processing "+eventSet.size()+" queue entry for id "+contentid);
         
         for (PSSearchIndexQueueItem queueItem : eventSet)
         {
            PSSearchEditorChangeEvent changeEvent = queueItem.getChangeEvent(true);

            if (changeEvent.getRevisionId() == -2)
            {
               m_logger.debug("Missing CONTENTSTATUS record for contentid "+contentid+" Must have been purged, deleting records for this id");
               deleteParent = true;
            }
            if (changeEvent.getRevisionId() > revision)
               revision = changeEvent.getRevisionId();

            int actionType = changeEvent.getActionType();

            // take content type id of the first event, all should be the same
            if (contentTypeId == -1)
               contentTypeId = changeEvent.getContentTypeId();

            // add to delete list
            queueIdList.add(String.valueOf(queueItem.getQueueId()));

            // log the event processing
            if (PSSearchEngine.getInstance().isTraceEnabled() && m_logger.isInfoEnabled())
            {
               Document doc = PSXmlDocumentBuilder.createXmlDocument();
               String out = "processing event: [" + queueItem.getQueueId() + "]:\n";
               out += PSXmlDocumentBuilder.toString(changeEvent.toXml(doc));
               m_logger.info(out);
            }

            int childId = changeEvent.getChildId();
            if (childId <= 0) // this is parent
            {
               if (actionType == PSEditorChangeEvent.ACTION_DELETE)
               {
                  deleteParent = true;
                  m_logger.debug("Handling delete event for item"+contentid);
               }
               else if (actionType == PSEditorChangeEvent.ACTION_REINDEX)
               {
                  reindex = true;
                  m_logger.debug("Handling reindex event for item "+contentid);
               }
               else
               {
                  indexParent = true;
                  binaryFields.addAll(PSIteratorUtils.cloneList(changeEvent.getBinaryFields()));
               }
            }
            else
            {
               // it's a child row action
               PSItemChildLocator childLoc = new PSItemChildLocator(String.valueOf(changeEvent.getChildId()),
                     String.valueOf(changeEvent.getChildRowId()));

               // see if we have an invalid content type. If so, create an
               // exception to be logged and written to the console, and
               // continue
               if (!canIndexContentType(contentTypeId, changeEvent.getChildId()))
               {
                  m_logger.error("Found invalid content type id " + contentTypeId
                        + " in index queue.  Removing entries");
                  deletePersistedEventsForType(contentTypeId);
                  return;
               }

               if (actionType == PSEditorChangeEvent.ACTION_DELETE)
               {
                  childDeletes.add(childLoc);
                  // don't process any other event's if it's deleted
                  childRows.remove(childLoc);

               }
               else
               {
                  /*
                   * don't process if we're already deleting it (although we
                   * shouldn't get an event after a delete)
                   * 
                   * NOTE: this assumes that we never reuse content ids
                   */
                  if (!childDeletes.contains(childLoc))
                  {
                     Set childBinFields = (Set) childRows.get(childLoc);
                     if (childBinFields == null)
                     {
                        childBinFields = new HashSet();
                        childRows.put(childLoc, childBinFields);
                     }
                     childBinFields.addAll(PSIteratorUtils.cloneList(changeEvent.getBinaryFields()));
                  }
               }
            }
         }

         PSLocator parentLoc = new PSLocator(contentid, revision);

         // PSSecurityToken tok = request.getSecurityToken();

         // see if we have an invalid content type. If so, delete the events
         // and throw an exception to be logged and written to the console
         if (!canIndexContentType(contentTypeId))
         {
            m_logger.error("Found invalid content type id " + contentTypeId + " in index queue.  Removing entries");
            deletePersistedEventsForType(contentTypeId);
            return;

         }

         PSKey cTypeKey = PSContentType.createKey((int) contentTypeId);
         // if deleting the parent or reindex, everything else can be ignored.
         try
         {
            if (deleteParent || reindex)
            {
               indexItemDeletion(cTypeKey, parentLoc, null);

               if (reindex && !deleteParent)
               {
                  indexFullItem(request, cTypeKey, parentLoc, commit);
               }
            }
            else
            {
               // process any child row deletes
               if (!childDeletes.isEmpty())
               {
                  indexItemDeletion(cTypeKey, parentLoc, childDeletes);
               }

               // now load the item and index the necessary parts
               if (indexParent || !childRows.isEmpty())
               {
                  Map itemChanges = new HashMap();

                  // determine load flags
                  if (indexParent)
                  {
                     loadFlags |= PSServerItem.TYPE_FIELDS;
                     if (!binaryFields.isEmpty())
                        loadFlags |= PSServerItem.TYPE_BINARY;
                     itemChanges.put(parentLoc, binaryFields);
                  }

                  if (!childRows.isEmpty())
                  {
                     loadFlags |= PSServerItem.TYPE_CHILD;

                     // walk childrows and check for binary fields
                     Iterator childRowValues = childRows.values().iterator();
                     while (childRowValues.hasNext())
                     {
                        Set childBinFields = (Set) childRowValues.next();
                        if (!childBinFields.isEmpty())
                        {
                           loadFlags |= PSServerItem.TYPE_BINARY;
                           break;
                        }
                     }

                     itemChanges.putAll(childRows);
                  }
                  PSServerItem item = null;

                  if (m_logger.isDebugEnabled())
                  {
                     boolean binaries = ((loadFlags & PSServerItem.TYPE_BINARY) == PSServerItem.TYPE_BINARY);
                     m_logger.debug("Loading item for index " + parentLoc.getId() + " revision="
                           + parentLoc.getRevision() + ", binaries = " + binaries);
                  }
                  try
                  {
                     item = loadItem(request, parentLoc, loadFlags);
                     indexItemChanges(cTypeKey, item, itemChanges, commit);

                     m_logger.debug("Finished Indexing item " + parentLoc.getId() + " revision="
                           + parentLoc.getRevision());
                  }
                  catch (Exception e)
                  {
                     // Item may have been deleted while we were processing.
                     // Check and only throw exception if it really is not there
                     PSComponentSummary sum = getComponentSummary(contentid);
                     if (sum != null)
                        throw e;

                     if (m_logger.isDebugEnabled())
                        m_logger.debug("Item +" + parentLoc.getId()
                              + " Deleted from CONTENTSTATUS while attempting to index, cleaning up");
                     deleteAll = true;

                  }

               }
            }

         }
         catch (Exception e)
         {

            m_logger.error("There was an error indexing item " + String.valueOf(contentid) + " change will be skipped",
                  e);
            deleteAll = true;
         }
         // now delete these events from the repository
         if (deleteAll)
            deletePersistedEventsForId(contentid);
         else
            deletePersistedEvents(queueIdList);
      }
      else
      {

         // If we have checked no items still in queue after timeout then do an
         // indexer optimize and try again.
         m_logger.debug("No items in indexer queue, optimizing");
         PSSearchEngine engine = PSSearchEngine.getInstance();
         PSSearchIndexer indexer = engine.getSearchIndexer();
         try
         {
            indexer.close(false);
         }
         finally
         {
            engine.releaseSearchIndexer(indexer);
         }
      }

      return;
   }

   /**
    * Loads the complete item specified by the parent locator and reindexes all
    * applicable parts.
    *
    * @param req Used to load the item, assumed not <code>null</code>.
    * @param cTypeKey The content type key, assumed not <code>null</code> and to
    *           be indexable.
    * @param parentLoc The locator of the item to reindex, assumed not
    *           <code>null</code>.
    *
    * @throws PSInvalidContentTypeException if the supplied parent locator is
    *            invalid.
    * @throws PSCmsException if there is an error loading the item.
    * @throws PSSearchException if there is an error updating the index.
    * @throws PSException if there are any other errors.
    */
   @SuppressWarnings("unchecked")
   private void indexFullItem(PSRequest req, PSKey cTypeKey, PSLocator parentLoc, boolean commit)
         throws PSSearchException, PSCmsException, PSException
   {
      // load the entire item
      int loadFlags = PSServerItem.TYPE_FIELDS | PSServerItem.TYPE_BINARY | PSServerItem.TYPE_CHILD;
      PSServerItem item = loadItem(req, parentLoc, loadFlags);

      // get parent bin fields
      Map itemChanges = new HashMap();
      itemChanges.put(parentLoc, getBinFieldNames(item.getAllFields()));

      // get list of children and their binfields
      Iterator children = item.getAllChildren();
      while (children.hasNext())
      {
         // use first entry of child to get bin field names
         Set binFields = null;
         PSItemChild child = (PSItemChild) children.next();
         Iterator entries = child.getAllEntries();
         if (entries.hasNext())
         {
            PSItemChildEntry entry = (PSItemChildEntry) entries.next();
            if (binFields == null)
            {
               binFields = getBinFieldNames(entry.getAllFields());
            }
            PSItemChildLocator childLoc = new PSItemChildLocator(String.valueOf(child.getChildId()),
                  String.valueOf(entry.getChildRowId()));
            itemChanges.put(childLoc, binFields);
         }
      }

      indexItemChanges(cTypeKey, item, itemChanges, commit);
   }

   /**
    * Loads the server item using the supplied values. Sets the
    * {@link IPSConstants#SKIP_FIELD_VISIBILITY_RULES} request private object to
    * disable field visibility rules, and resets it to it's previous value when
    * finished. Also sets the {@link IPSConstants#LOAD_FOR_SEARCH_INDEX} request
    * private object to enable index specific functionality, and resets it to
    * it's previous value when finished.
    * 
    * @param req The request to use, assumed not <code>null</code>.
    * @param itemLoc The locator of the item to load, assumed not
    *           <code>null</code> and to reference a valid item.
    * @param loadFlags Set of flags to specify what to load, see
    *           {@link PSServerItem#loadItem(PSLocator, PSRequest, int)} for
    *           details.
    * 
    * @return The item, never <code>null</code>.
    * 
    * @throws PSInvalidContentTypeException If the locator is invalid.
    * @throws PSCmsException If there are any other errors.
    */
   private PSServerItem loadItem(PSRequest req, PSLocator itemLoc, int loadFlags) throws PSCmsException,
         PSInvalidContentTypeException
   {
      // current visibility rules flag value
      Object previousSkipVal = req.getPrivateObject(IPSConstants.SKIP_FIELD_VISIBILITY_RULES);

      // set to skip visibility rules
      req.setPrivateObject(IPSConstants.SKIP_FIELD_VISIBILITY_RULES, Boolean.valueOf(true));

      // set flag to indicate item is being loaded for search indexing
      Object previousLoadVal = req.getPrivateObject(IPSConstants.LOAD_FOR_SEARCH_INDEX);
      req.setPrivateObject(IPSConstants.LOAD_FOR_SEARCH_INDEX, Boolean.valueOf(true));

      try
      {
         // load the item
         PSServerItem item = PSServerItem.loadItem(itemLoc, req, loadFlags);

         return item;
      }
      finally
      {
         // reset the flags to their previous value
         req.setPrivateObject(IPSConstants.SKIP_FIELD_VISIBILITY_RULES, previousSkipVal);
         req.setPrivateObject(IPSConstants.LOAD_FOR_SEARCH_INDEX, previousLoadVal);
      }
   }

   /**
    * Get names of all fields in the supplied iterator that are binary.
    *
    * @param fields An iterator over zero or more <code>PSItemField</code>
    *           objects, assumed not <code>null</code>.
    *
    * @return A set of binary field names, never <code>null</code>, may be
    *         empty.
    */
   @SuppressWarnings("unchecked")
   private Set getBinFieldNames(Iterator fields)
   {
      Set binFields = new HashSet();
      while (fields.hasNext())
      {
         PSItemField field = (PSItemField) fields.next();
         if (field.getItemFieldMeta().isBinary())
            binFields.add(field.getName());
      }

      return binFields;
   }

   /**
    * Call the indexer to update itself with the supplied item changes.
    *
    * @param contentType The content type of the item that has changed, assumed
    *           not <code>null</code>.
    * @param item The item that has changed, containing the current values for
    *           the items fields and children specified by the
    *           <code>itemChanges</code>.
    * @param itemChanges Identifies the parts of the item that have changed. Key
    *           is assumed to be either a {@link PSLocator} or a
    *           {@link PSItemChildLocator}, value is a <code>Set</code> of
    *           modified binary field names as <code>String</code> objects,
    *           never <code>null</code>, may be empty.
    *
    * @throws PSCmsException if there is an error retrieving a field value.
    * @throws PSSearchException if there is an error updating the index.
    * @throws PSException if there are any other errors.
    */
   @SuppressWarnings("unchecked")
   private void indexItemChanges(PSKey contentType, PSServerItem item, Map itemChanges, boolean commit)
         throws PSSearchException, PSCmsException, PSException
   {
      PSSearchEngine engine = PSSearchEngine.getInstance();
      PSSearchIndexer indexer = engine.getSearchIndexer();
      PSLocator itemKey = item.getKey();

      try
      {
         PSSearchKey unitId = null;
         Map fragment = null;
         Iterator entries = itemChanges.entrySet().iterator();
         while (entries.hasNext())
         {
            Map.Entry entry = (Entry) entries.next();
            PSKey key = (PSKey) entry.getKey();
            Set binFields = (Set) entry.getValue();
            if (key instanceof PSLocator)
            {
               unitId = new PSSearchKey(contentType, (PSLocator) key, null);

               /*
                * build the item fragment - load system fields, then overlay the
                * item fields. This must be done as not all system fields end up
                * in the item as fields - some end up as metadata in the content
                * editor xml and the field names are lost. This will add them to
                * the item with the system field names.
                */
               fragment = loadSystemFields((PSLocator) key);
               if (fragment == null)
               {
                  // didn't find the item? should not be possible
                  throw new RuntimeException("failed to locate system field info for item with id: "
                        + ((PSLocator) key).getId());

               }
               Iterator fields = item.getAllFields();
               fragment.putAll(extractItemFragment(fields, binFields));

               // add additional fields which are searchable
               fragment.putAll(loadAdditionalFields(item));
            }
            else
            {
               PSItemChildLocator childLocator = (PSItemChildLocator) key;
               unitId = new PSSearchKey(contentType, itemKey, childLocator);
               int childId = Integer.parseInt(childLocator.getChildContentType());
               int childRowId = Integer.parseInt(childLocator.getChildRowId());
               fragment = new HashMap();
               PSItemChild itemChild = item.getChildById(childId);
               if (itemChild != null)
               {
                  PSItemChildEntry childEntry = itemChild.getChildEntryByRowId(childRowId);
                  if (childEntry != null)
                     fragment = extractItemFragment(childEntry.getAllFields(), binFields);
               }

               if (fragment == null)
               {
                  // this would be a bug, throw a runtime exception so it will
                  // be logged and printed on the console.
                  throw new RuntimeException("Failed to extract item fragment for indexing item: " + itemKey.getId()
                        + "," + itemKey.getRevision() + "child item: " + childId + "," + childRowId);
               }
            }

            if (m_fieldValueModifier != null)
               m_fieldValueModifier.modifyFields(fragment);

            if (engine.isTraceEnabled() && m_logger.isInfoEnabled())
            {
               StringBuilder buf = new StringBuilder();
               buf.append("Update index: \n");
               buf.append(PSXmlDocumentBuilder.toString(key.toXml(PSXmlDocumentBuilder.createXmlDocument())));
               buf.append(fragment);
               m_logger.info(buf.toString());
            }

            indexer.update(unitId, fragment, false);
         }

      }
      finally
      {
         indexer.commit();
         engine.releaseSearchIndexer(indexer);
      }
   }

   /**
    * Loads all system fields for the specified item by cataloging all system
    * fields and then performing a search on the specified item.
    * <p>
    * NOTE: This implementation assumes that there are no binary fields in the
    * system def as we get all values back as strings. This will need to change
    * if we ever put a binary field in the system def.
    *
    * @param key The key identifying the item, assumed not <code>null</code>.
    *
    * @return A map of system fields, key is the field name and value is the
    *         value of the field, both as <code>String</code> objects. Will be
    *         <code>null</code> if the specified item cannot be found.
    *
    * @throws PSSearchException if the system field values cannot be obtained.
    */
   @SuppressWarnings("unchecked")
   private Map loadSystemFields(PSLocator key) throws PSSearchException
   {
      // get system field catalog
      PSRequest req = PSRequest.getContextForRequest();
      PSLocalCataloger cat = new PSLocalCataloger(req);
      Set systemFieldNames = cat.getSystemFields(IPSCataloger.FLAG_USER_SEARCH | IPSCataloger.FLAG_INCLUDE_HIDDEN);

      Map fields = loadFields(key.getId(), systemFieldNames);

      return (!fields.isEmpty()) ? fields : null;
   }

   /**
    * Loads all searchable fields which are found in the item definition of the
    * item, but not in the item itself as they do not have an associated display
    * mapping.
    *
    * @return see {@link #loadFields(int, Set)}.
    * 
    * @throws PSSearchException if the additional fields cannot be obtained.
    */
   private Map<String, String> loadAdditionalFields(PSServerItem item) throws PSSearchException
   {
      Set<String> additionalFieldNames = new HashSet<String>();

      Collection<PSField> searchableFields = PSSearchUtils.getSearchableFields(item.getItemDefinition());
      for (PSField field : searchableFields)
      {
         String name = field.getSubmitName();
         if (item.getFieldByName(name) == null)
         {
            additionalFieldNames.add(name);
         }
      }

      return loadFields(item.getContentId(), additionalFieldNames);
   }

   /**
    * Uses search to load the specified fields for the specified item.
    * 
    * @param contentId of the item.
    * @param fieldNames of the fields to load.
    * 
    * @return A map of field-value pairs where the key is the field name as a
    *         <code>String</code> and the value is the field value, also as a
    *         <code>String</code>.
    * 
    * @throws PSSearchException if the field values cannot be obtained.
    */
   @SuppressWarnings("unchecked")
   private Map<String, String> loadFields(int contentId, Set fieldNames) throws PSSearchException
   {
      Map<String, String> fields = new HashMap<String, String>();

      PSRequest req = PSRequest.getContextForRequest();

      List idList = new ArrayList();
      idList.add(new Integer(contentId));

      IPSExecutableSearch search = PSExecutableSearchFactory.createExecutableSearch(req, fieldNames, idList);
      PSWSSearchResponse searchResponse = search.executeSearch();
      Iterator rows = searchResponse.getRows();
      // only one row returned per item
      if (rows.hasNext())
      {
         IPSSearchResultRow row = (IPSSearchResultRow) rows.next();
         Iterator iter = row.getColumnValueMap().entrySet().iterator();
         while (iter.hasNext())
         {
            Map.Entry entry = (Map.Entry) iter.next();
            fields.put(entry.getKey().toString(), entry.getValue().toString());
         }

      }

      return fields;
   }

   /**
    * Creates the map of field-value pairs used to update the indexer.
    *
    * @param fields An iterator over zero or more {@link PSItemField} objects to
    *           use to build the map, assumed not <code>null</code>.
    * @param binFields A set of binary fields names that have been modified,
    *           assumed not <code>null</code>, may be empty.
    *
    * @return A map of field-value pairs where the key is the field name as a
    *         <code>String</code> and the value is an object representing the
    *         value.
    *
    * @throws PSCmsException if there is an error getting a field value.
    */
   @SuppressWarnings("unchecked")
   private Map extractItemFragment(Iterator fields, Set binFields) throws PSCmsException
   {
      Map fragment = new HashMap();
      while (fields.hasNext())
      {
         PSItemField itemField = (PSItemField) fields.next();
         if (itemField.getItemFieldMeta().isBinary())
         {
            // only add binary fields to the fragment if it was modified
            if (binFields.contains(itemField.getName()))
            {
               Object val = null;
               PSBinaryValue binval = (PSBinaryValue) itemField.getValue();
               if (binval != null)
                  val = binval.getValue();
               if (val == null)
                  val = "";
               fragment.put(itemField.getName(), val);
            }
         }
         else
         {
            // build space delimited list of string values (words)
            String val = "";
            Iterator values = itemField.getAllValues();
            while (values.hasNext())
            {
               String value = null;

               IPSFieldValue fieldVal = (IPSFieldValue) values.next();
               if (fieldVal != null)
                  value = fieldVal.getValueAsString();

               if (value != null && value.trim().length() > 0)
               {
                  if (val.length() > 0)
                     val += " ";
                  val += value;
               }
            }

            fragment.put(itemField.getName(), val);
         }
      }

      return fragment;
   }

   /**
    * Deletes all entries for the specified item from the index.
    *
    * @param contentType The content type of the item, assumed not
    *           <code>null</code>.
    * @param itemId The locator identifying the item to remove, assumed not
    *           <code>null</code>.
    * @param childIds A list of {@link PSItemChildLocator} objects, may be
    *           <code>null</code> to delete the parent item, which will cascade
    *           to all child index entries as well.
    *
    * @throws PSSearchException if there are any errors.
    */
   private void indexItemDeletion(PSKey contentType, PSLocator itemId, List childIds) throws PSSearchException
   {
      PSSearchEngine engine = PSSearchEngine.getInstance();
      PSSearchIndexer indexer = engine.getSearchIndexer();
      Collection<PSSearchKey> unitIds = new ArrayList<PSSearchKey>();
      try
      {
         if (childIds == null)
         {
            if (engine.isTraceEnabled() && m_logger.isInfoEnabled())
            {
               StringBuilder buf = new StringBuilder();
               buf.append("delete from index: \n");
               Document doc = PSXmlDocumentBuilder.createXmlDocument();
               buf.append(PSXmlDocumentBuilder.toString(contentType.toXml(doc)));
               buf.append(PSXmlDocumentBuilder.toString(itemId.toXml(doc)));
               m_logger.info(buf.toString());
            }

            PSSearchKey unitId = new PSSearchKey(contentType, itemId, null);
            unitIds.add(unitId);
         }
         else
         {
            Iterator children = childIds.iterator();
            while (children.hasNext())
            {
               PSItemChildLocator childLoc = (PSItemChildLocator) children.next();

               if (engine.isTraceEnabled() && m_logger.isInfoEnabled())
               {
                  StringBuilder buf = new StringBuilder();
                  buf.append("delete from index: \n");
                  Document doc = PSXmlDocumentBuilder.createXmlDocument();
                  buf.append(PSXmlDocumentBuilder.toString(contentType.toXml(doc)));
                  buf.append(PSXmlDocumentBuilder.toString(itemId.toXml(doc)));
                  buf.append(PSXmlDocumentBuilder.toString(childLoc.toXml(doc)));
                  m_logger.info(buf.toString());
               }

               PSSearchKey unitId = new PSSearchKey(contentType, itemId, childLoc);
               unitIds.add(unitId);
            }
         }

         indexer.delete(unitIds);
         indexer.commit();
      }
      finally
      {
         engine.releaseSearchIndexer(indexer);
      }
   }

   /**
    * Get next event, and return all related events that can be coalesced and
    * processed at the same time. Events are removed from all in memory
    * collections. Will wait on empty queues for a limited time before
    * returning, but will not process any new events that are ready after
    * waiting. Queues are checked in priority order.
    * 
    * @param timeOut The number of milliseconds to wait before returning. Pass
    *           zero to wait indefinitely, assumed not negative.
    * 
    * @return The next set of events to process, may be <code>null</code> if
    *         there are no events in the queue or if we waited.
    * 
    * @throws InterruptedException if interrupted while waiting on the queue
    *            monitor.
    */
   private List<PSSearchIndexQueueItem> getNextEventSet(long timeOut) throws InterruptedException
   {
      while (!m_shutdown)
      {
         while (m_pausedCount.get()>0 && !m_shutdown)
         {
            Thread.sleep(2000);
         }
         if (m_processingEvents.isEmpty())
         {
            m_processingEvents = getPersistedEventsByIds();
            if (m_processingEvents.isEmpty())
            {
               m_queueService.waitForPoll(timeOut);
               m_logger.debug("Waking up for Queue Poll");
            }
         }
         else
         {
            int id = m_processingEvents.keySet().iterator().next();
            List<PSSearchIndexQueueItem> events = m_processingEvents.get(id);
            m_processingEvents.remove(id);
            return events;
         }

      }
      return null;
   }

   /**
    * persist's the supplied event to the repository.
    *
    * @param event The event to persist, assumed not <code>null</code>.
    *
    * @return The id used to locate this event in the repository.
    */
   @SuppressWarnings("unchecked")
   private int persistEvent(PSSearchEditorChangeEvent event)
   {
      m_logger
      .debug("Indexer Change Event for id="+ event.getContentId() + " rev=" + event.getRevisionId() + "\n" + "Event is " + event.getActionTypeName());

      try
      {
         PSSearchIndexQueueItem item = new PSSearchIndexQueueItem(event);
         int ret = m_queueService.saveItem(item);
         return (ret);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to persist queue event:" + e.getLocalizedMessage());
      }
   }

   /**
    * Deletes the specified events from the repository
    *
    * @param request The request to use, assumed not <code>null</code>.
    * @param queueIdList The list of queue id's to delete, as
    *           <code>String</code> objects, assumed not <code>null</code>.
    */
   private void deletePersistedEvents(List<String> queueIdList)
   {

      if (queueIdList != null && !queueIdList.isEmpty())
      {
         try
         {
            Collection<Integer> queueIds = new ArrayList<Integer>();
            for (Object textQID : queueIdList)
            {
               Integer queueId = Integer.decode(textQID.toString());
               queueIds.add(queueId);
            }

            m_queueService.deleteItems(queueIds);
            getNotificationService().notifyEvent(
                  new PSNotificationEvent(EventType.SEARCH_INDEX_ITEM_PROCESSED, Integer.valueOf(queueIdList.size())));
         }
         catch (Exception e)
         {
            throw new RuntimeException("Unable to delete persisted queue event:" + e.getLocalizedMessage());
         }
      }
   }

   /**
    * Deletes the specified contentid from the repository
    *
    * @param contentid the contentid to remove
    */
   private void deletePersistedEventsForId(int contentid)
   {
      try
      {
         m_queueService.deleteIdItems(contentid);
         getNotificationService().notifyEvent(
               new PSNotificationEvent(EventType.SEARCH_INDEX_ITEM_PROCESSED, Integer.valueOf(-1)));
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to delete persisted queue event:" + e.getLocalizedMessage());
      }
   }

   /**
    * Deletes the specified contenttypeid from the repository
    *
    * @param contentid the contenttypeid to remove
    */
   private void deletePersistedEventsForType(long contentTypeId)
   {
      try
      {
         m_queueService.deleteTypeIdItems(contentTypeId);
         getNotificationService().notifyEvent(
               new PSNotificationEvent(EventType.SEARCH_INDEX_ITEM_PROCESSED, Integer.valueOf(-1)));
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to delete persisted queue event:" + e.getLocalizedMessage());
      }
   }

   /**
    * Gets up to <code>QUERY_MAX_EVENTS</code> persisted events from repository.
    * A commit is done for every set so we may want to increase the size of
    * <code>QUERY_MAX_EVENTS</code> if this is causing performance issues.
    *
    * @return map of queue ids and search editor change events. May be empty but
    *         never <code>null</code>.
    */
   private Map<Integer, List<PSSearchIndexQueueItem>> getPersistedEventsByIds()
   {
      Map<Integer, List<PSSearchIndexQueueItem>> events = new HashMap<Integer, List<PSSearchIndexQueueItem>>();

      List<PSSearchIndexQueueItem> items = m_queueService.loadItems(QUERY_MAX_EVENT_IDS);

      Iterator<PSSearchIndexQueueItem> iter = items.iterator();
      while (iter.hasNext())
      {
         PSSearchIndexQueueItem item = iter.next();

         int id = item.getContentId();
         List<PSSearchIndexQueueItem> eventList = events.get(id);
         if (eventList == null)
         {
            eventList = new ArrayList<PSSearchIndexQueueItem>();
            events.put(id, eventList);
         }

         eventList.add(item);

      }
      m_logger.debug("Pulled "+items.size()+" index entries from database for ids :"+ events.keySet());
      
      return events;
   }

   /**
    * Clears all in memory events and repository events. Shuts down the queue
    * first that clears all in memory events. Then deletes all repository
    * events. Restarts the queue.
    */
   public void clearQueues()
   {
      try
      {
         shutdown();
         m_queueService.deleteAllItems();
         getNotificationService().notifyEvent(new PSNotificationEvent(EventType.SEARCH_INDEX_ITEM_PROCESSED, -1));
      }
      finally
      {
         start();
      }
   }

   // see IPSEditorChangeListener interface
   public void editorChanged(PSEditorChangeEvent e)
   {

      PSSearchEditorChangeEvent ev = new PSSearchEditorChangeEvent(e.getActionType(), e.getContentId(),
            e.getRevisionId(), e.getContentTypeId(), true);
      ev.setPriority(e.getPriority());
      List<String> fields = new ArrayList<String>();
      Iterator it = e.getBinaryFields();
      while (it.hasNext())
      {
         fields.add((String) it.next());
      }
      ev.setBinaryFields(fields);

      queueEvent(ev);
   }

   // see IPSHandlerInitListener interface
   public void initHandler(IPSRequestHandler requestHandler)
   {
      if (requestHandler instanceof PSContentEditorHandler)
      {
         PSContentEditorHandler ceh = (PSContentEditorHandler) requestHandler;
         ceh.addEditorChangeListener(this);
      }
   }

   // see IPSHandlerInitListener interface
   public void shutdownHandler(@SuppressWarnings("unused") IPSRequestHandler requestHandler)
   {
      // noop
   }

   /**
    * The singleton instance of this class, <code>null</code> until first call
    * to {@link #getInstance()}, never <code>null</code> or modified after that.
    */
   private static volatile PSSearchIndexEventQueue ms_instance = null;

   /**
    * The name of the aging thread created by this object.
    */
   public static final String QUEUE_THREAD_NAME = "FTSIndexQueue";

   /**
    * Priority to use when constructing events for reindexing so that they will
    * be processed after all other events.
    */
   private static final int REINDEX_PRIORITY = 100;

   /**
    * Name of the parameter to specify the limit of events to retrieve from the
    * repository for large volumne of items
    */
   private static final int QUERY_MAX_EVENT_IDS = 10;

   /**
    * How long to wait of no items in queue before clearing out index writers
    * and optimizing index
    */
   private static final int INDEX_OPTIMIZE_WAIT = EVENT_WAIT_TIME_MS * 180;

   /**
    * Monitor object to provide synchronization of start and shutdown. Never
    * <code>null</code> or modified.
    */
   private Object m_runMonitor = new Object();

   private Object nsLock = new Object();

   /**
    * Monitor object to provide synchronized access to the {@link #m_shutdown}
    * flag. Never <code>null</code> or modified.
    */
   private Object m_shutdownMonitor = new Object();

   /**
    * Indicates if the queue is running. Initially <code>false</code>, set to
    * <code>true</code> by {@link #start()}, set to <code>false</code> by
    * {@link #shutdown()}. Value should only be modified if synchronized on the
    * {@link #m_runMonitor} object.
    */
   private boolean m_run = false;

   /**
    * Indicates if the queue is paused. If paused, events are still queued, but
    * they are not processed.
    */
   private AtomicInteger m_pausedCount = new AtomicInteger(0);

   /**
    * Thread to handle processing of events. Intialized by {@link #start()}, not
    * <code>null</code> or modified until {@link #shutdown()} is called.
    */
   private Thread m_queueThread;

   /**
    * Indicates that the queue is shutting down. Initially <code>false</code>,
    * set to <code>true</code> by {@link #shutdown()}, set back to
    * <code>false</code> once shutdown is completed. Value should only be
    * modified by the {@link #shutdown()} method, where access is synchrnoized
    * appropriately.
    */
   private boolean m_shutdown = false;

   /**
    * Map of params to submit when inserting events into the db queue.
    * Initialized by a static intializer, never <code>null</code>, empty, or
    * modified after that.
    */
   private static Map<String, String> ms_insertParamMap;

   /**
    * Map of params to submit when deleting events into the db queue.
    * Initialized by a static intializer, never <code>null</code>, empty, or
    * modified after that.
    */
   private static Map<String, String> ms_deleteParamMap;

   /**
    * During construction, this class checks the server configuration to see if
    * full text search is enabled. This static holds the result.
    */
   private static boolean ms_searchEnabled;

   static
   {
      ms_insertParamMap = new HashMap<String, String>();
      ms_insertParamMap.put("DBActionType", "INSERT");

      ms_deleteParamMap = new HashMap<String, String>();
      ms_deleteParamMap.put("DBActionType", "DELETE");
   }

   /**
    * Get hibernate class for Queue Events persisted to the db
    */
   private IPSSearchIndexQueue m_queueService = PSSearchIndexQueueLocator.getPSSearchIndexQueue();

   /**
    * We keep a local copy to decrease the performance impact and to make the
    * code a little clearer. Initialized during class construction, then never
    * <code>null</code> or modified.
    */
   private Logger m_logger = LogManager.getLogger(getClass());

   /**
    * Used for keeping performance metrics and logging results to the server
    * console when debug is enabled.
    */

   private QueuePerformaceLogging m_perfLogger = new QueuePerformaceLogging();

   private IPSNotificationService notificationService;

   private IPSFieldValueModifier m_fieldValueModifier = null;

   private Map<Integer, List<PSSearchIndexQueueItem>> m_processingEvents = new HashMap<Integer, List<PSSearchIndexQueueItem>>();

   /**
    * Encapsulates a set of events to process for a single content id.
    */
   private class PSQueueEventSet
   {
      /**
       * Construct an event set
       *
       * @param queueKey The key for which all events in the set are to be
       *           processed, may not be <code>null</code>.
       */
      public PSQueueEventSet(PSQueueKey queueKey)
      {
         if (queueKey == null)
            throw new IllegalArgumentException("queueKey may not be null");

         m_queueKey = queueKey;
      }

      /**
       * Adds the supplied queue event to this set.
       *
       * @param queueEvent The event to add, may not be <code>null</code>, must
       *           match this set's queue key.
       */
      @SuppressWarnings("unchecked")
      public void addQueueEvent(PSQueueEvent queueEvent)
      {
         if (queueEvent == null)
            throw new IllegalArgumentException("changeEvent may not be null");

         PSQueueKey key = new PSQueueKey(queueEvent.getChangeEvent());
         if (!m_queueKey.equals(key))
            throw new IllegalArgumentException("invalid queue key");

         m_eventList.add(queueEvent);
      }

      /**
       * Gets the queue key supplied during construction.
       *
       * @return The key.
       */
      public PSQueueKey getQueueKey()
      {
         return m_queueKey;
      }

      /**
       * Gets the events in this set.
       *
       * @return An iterator over zero or more <code>PSQueueEvent</code>
       *         objects, never <code>null</code>.
       *
       */
      public Iterator getChangeEvents()
      {
         return m_eventList.iterator();
      }

      /**
       * The key used to construct this object, never <code>null</code> or
       * modified after that.
       */
      private PSQueueKey m_queueKey;

      /**
       * The list of queue events, never <code>null</code>, may be empty. Events
       * are added by {@link #addQueueEvent(PSQueueEvent)}.
       */
      private List m_eventList = new ArrayList();

      public int getEventCount()
      {
         return m_eventList.size();
      }
   }

   /**
    * Represents an event in the queue. Objects of this class are immutable.
    */
   private class PSQueueEvent
   {
      /**
       * Construct a queue event from an editor change event.
       *
       * @param queueId The queue id to assign to this change event
       * @param changeEvent The change event that will be processed, may not be
       *           <code>null</code>.
       */
      public PSQueueEvent(int queueId, PSSearchEditorChangeEvent changeEvent)
      {
         if (changeEvent == null)
            throw new IllegalArgumentException("changeEvent may not be null");

         m_queueid = queueId;
         m_changeEvent = changeEvent;
      }

      /**
       * Get the id assigned to this event.
       *
       * @return The queueId supplied in the ctor.
       */
      public int getQueueId()
      {
         return m_queueid;
      }

      /**
       * Get the change event supplied during construction.
       *
       * @return The change event, never <code>null</code>.
       */
      public PSSearchEditorChangeEvent getChangeEvent()
      {
         return m_changeEvent;
      }

      /**
       * The queue id assigned to this event, intialized during construction,
       * greater than 0, never modified after that.
       */
      private int m_queueid;

      /**
       * The editor change event supplied during construction, never
       * <code>null</code> or modified after that.
       */
      private PSSearchEditorChangeEvent m_changeEvent;
   }

   /**
    * Key that can be used to identify a change for a particular content item.
    * Objects of this class are immutable.
    */
   private class PSQueueKey
   {
      /**
       * Construct this key from an editor change event. The content id and
       * revision id are extracted and used to create a key.
       *
       * @param e The change event, may not be <code>null</code>.
       */
      public PSQueueKey(PSSearchEditorChangeEvent e)
      {
         if (e == null)
            throw new IllegalArgumentException("e may not be null");

         m_contentId = e.getContentId();
         m_revisionId = e.getRevisionId();
      }

      /**
       * Get the content id portion of this key.
       *
       * @return The content id.
       */
      public int getContentId()
      {
         return m_contentId;
      }

      /**
       * Get the revision id portion of this key.
       *
       * @return The revision id.
       */
      public int getRevisionId()
      {
         return m_revisionId;
      }

      /**
       * Determines if this key is equal to another key. See
       * {@link Object#equals(Object)} for more info.
       *
       * @param o The object to compare, may be <code>null</code>.
       *
       * @return <code>true</code> if the supplied object is a
       *         <code>PSQueueKey</code> with the same contentid and revisionid,
       *         <code>false</code> otherwise.
       */
      public boolean equals(Object o)
      {
         boolean isEqual = true;

         if (!(o instanceof PSQueueKey))
            isEqual = false;
         else
         {
            PSQueueKey other = (PSQueueKey) o;
            if (m_contentId != other.m_contentId)
               isEqual = false;
            else if (m_revisionId != other.m_revisionId)
               isEqual = false;
         }

         return isEqual;
      }

      // see base class
      public int hashCode()
      {
         return (m_contentId + "" + m_revisionId).hashCode();
      }

      /**
       * The content id of this key, initialized during ctor, never modified
       * after that.
       */
      private int m_contentId;

      /**
       * The revision id of this key, initialized during ctor, never modified
       * after that.
       */
      private int m_revisionId;
   }

   /**
    * Handles in-memory queue operations for events of a particular priority.
    * May also handle events of a different priority if events for the same item
    * have already been queued. It is the responsibility of the caller to
    * synchronize access to this object where required.
    */
   private class PSPriorityEventQueue
   {
      /**
       * Construct a queue for the specified priority.
       *
       * @param priority The priority assigned to this queue. This object does
       *           not interpret this value relative to that of other
       *           priorities; it is the callers responsibility to determine how
       *           to interpret this value.
       */
      public PSPriorityEventQueue(int priority)
      {
         m_priority = priority;
      }

      /**
       * Returns a value equal to the number of entries in this queue.
       *
       * @return A value of 0 or higher.
       */
      public int size()
      {
         return m_eventQueue.size();
      }

      /**
       * Get the priority of this queue.
       *
       * @return The priority specified during construction.
       */
      public int getPriority()
      {
         return m_priority;
      }

      /**
       * Determine if this queue contains any events.
       *
       * @return <code>true</code> if this queue is empty, <code>false</code> if
       *         not.
       */
      public boolean isEmpty()
      {
         return m_eventQueue.isEmpty();
      }

      /**
       * Queues the supplied change event. Will only queue the event if the
       * priority matches, or if there are already events queued for the same
       * item.
       *
       * @param queueId The id uniquely identifying this event in the
       *           repository.
       * @param changeEvent The change event that is to be queued, assumed not
       *           <code>null</code>.
       *
       * @return <code>true</code> if the item was queued, <code>false</code> if
       *         not.
       */
      @SuppressWarnings("unchecked")
      public boolean queueEvent(int queueId, PSSearchEditorChangeEvent changeEvent)
      {
         PSQueueEvent queueEvent = new PSQueueEvent(queueId, changeEvent);

         // check map to see if item is already in the queue
         PSQueueKey queueKey = new PSQueueKey(changeEvent);
         PSQueueEventSet queueEventSet = (PSQueueEventSet) m_queueIdEventMap.get(queueKey);

         // accept if we have an event set, or if priority matches
         boolean accept = (changeEvent.getPriority() == m_priority || queueEventSet != null);
         if (accept)
         {
            if (queueEventSet == null)
            {
               // add content id to the queue, add an event set with this event
               // to the map
               m_eventQueue.add(queueKey);
               queueEventSet = new PSQueueEventSet(queueKey);
               m_queueIdEventMap.put(queueKey, queueEventSet);

               // add to content id map so we can handle item deletes by content
               // id
               Integer ctId = new Integer(changeEvent.getContentId());
               Set idSet = (Set) m_contentIdEventMap.get(ctId);
               if (idSet == null)
               {
                  idSet = new HashSet();
                  m_contentIdEventMap.put(ctId, idSet);
               }
               idSet.add(queueKey);
            }
            queueEventSet.addQueueEvent(queueEvent);
         }

         return accept;
      }

      /**
       * Clears all events from this queue.
       */
      public void clear()
      {
         m_eventQueue.clear();
         m_queueIdEventMap.clear();
         m_contentIdEventMap.clear();
      }

      /**
       * The priority assigned to this queue during construction, never modified
       * after that.
       */
      private int m_priority;

      /**
       * Queue'd list of keys as <code>PSQueueKey</code> objects. Never
       * <code>null</code>, keys are added as events are queued and removed as
       * they are retrieved.
       */
      private List m_eventQueue = new ArrayList(50);

      /**
       * Map of queue keys as <code>PSQueueKey</code> objects to queued events
       * as <code>PSQueueEventSet</code> objects. Entries are added as events
       * are queued and removed as they are retrieved.
       */
      private Map m_queueIdEventMap = new HashMap();

      /**
       * Map of content ids as <code>Integer</code> objects to all queued events
       * for the same content id. Value is a <code>Set</code> of
       * <code>PSQueueKey</code> objects. Entries are added as events are queued
       * and removed as they are retrieved.
       */
      private Map m_contentIdEventMap = new HashMap();

   }

   /**
    * This class is used for logging performance metrics for the indexing of
    * items.
    */
   private class QueuePerformaceLogging
   {
      /**
       * Starts the timer and keeps a count of how many times we have done a
       * commit since the queue started or was empty.
       */
      public void startCommittMetrics()
      {
         if (!m_logger.isDebugEnabled())
         {
            return;
         }

         ++mi_commitCount;
         mi_swTimeSpentCommitting.start();
      }

      /**
       * Logs the metrics accumulated during the commit of indexing events
       */
      public void logCommitMetrics()
      {
         if (!m_logger.isDebugEnabled())
         {
            return;
         }

         mi_swTimeSpentCommitting.stop();
         int count = mi_itemCount - mi_itemsLastCommit;

         m_logger.debug(count + " items committed in " + mi_swTimeSpentCommitting.elapsed());
         mi_commitTime += mi_swTimeSpentCommitting.elapsed();
         mi_itemsLastCommit = mi_itemCount;
      }

      /**
       * Logs the metrics accumulated during the indexing of events since the
       * last time the queue started or was empty.
       */
      public void logIndexingMetrics()
      {
         if (!m_logger.isDebugEnabled())
         {
            return;
         }

         mi_end_time = new Date();
         if (mi_swTimeSinceEmpty.elapsed() > 0)
         {
            mi_swTimeSinceEmpty.stop();
         }
         if (mi_itemCount > 0)
         {
            m_logger.debug("Queue started processing at " + mi_start_time + " and finished at " + mi_end_time);
            m_logger.debug(mi_itemCount + " items processed in " + mi_swTimeSinceEmpty);
            m_logger.debug(mi_commitCount + " commits in " + mi_commitTime);
         }
      }

      /**
       * Sets up the state for measuring the performance of indexing the events
       * until the queue empties.
       */
      public void startIndexingMetrics()
      {
         if (!m_logger.isDebugEnabled())
         {
            return;
         }

         mi_itemCount = 0;
         mi_commitCount = 0;
         mi_commitTime = 0;
         mi_swTimeSinceEmpty.start();
         mi_start_time = new Date();
         mi_itemsLastCommit = 0;
      }

      /**
       * Used to keep track of the number of events processed since the last
       * time the queue was started or empty.
       * 
       * @param eventSet - the set of events that are about to be processed.
       */
      public void setEventCount(PSQueueEventSet eventSet)
      {
         if (!m_logger.isDebugEnabled() || eventSet == null)
         {
            return;
         }

         mi_itemCount += eventSet.getEventCount();
      }

      private int mi_itemCount = 0;

      private PSStopwatch mi_swTimeSinceEmpty = new PSStopwatch();

      private PSStopwatch mi_swTimeSpentCommitting = new PSStopwatch();

      private Date mi_start_time;

      private Date mi_end_time;

      private int mi_commitTime = 0;

      private int mi_commitCount = 0;

      private int mi_itemsLastCommit = 0;
   }

}
