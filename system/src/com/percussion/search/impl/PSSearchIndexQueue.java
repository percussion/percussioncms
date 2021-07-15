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
package com.percussion.search.impl;

import com.percussion.data.PSIdGenerator;
import com.percussion.search.IPSSearchIndexQueue;
import com.percussion.search.PSSearchIndexEventQueue;
import com.percussion.search.data.PSSearchIndexQueueItem;
import com.percussion.util.PSSqlHelper;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang.Validate.notNull;

/**
 * Class description - implementation of {@link IPSSearchIndexQueue} See
 * interface for details
 * 
 * 
 * @author BillLanglais
 */
@Transactional
public class PSSearchIndexQueue  implements IPSSearchIndexQueue
{
   private static final int MAX_DELAY = 120000;

   private static final int POLL_DELAY = 20000;

   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   // see base class method for details
   @SuppressWarnings("unchecked")
   @Transactional
   public List<PSSearchIndexQueueItem> loadItems(int count)
   {
      Date maxDelayDate = new Date(new Date().getTime() - MAX_DELAY);
      Date minDelayDate = new Date(new Date().getTime() - POLL_DELAY);

      List<PSSearchIndexQueueItem> queueItems = new ArrayList<PSSearchIndexQueueItem>();
      
      Session sess = sessionFactory.getCurrentSession();


         //  Don't pull changes that are not yet current revision.  Checked out items that have been public will collect in queue until checked in.
         //  Ignore id's that have been modified in last 15s to group changes,  index item anyway if the first change in set is 60s old
         //  Order items by highest priority in set (lowest number) Then by the age of the update.
      String sql = null;
      try {
         sql = "select qi.CONTENTID from "
               + PSSqlHelper.qualifyTableName("PSX_SEARCHINDEXQUEUE")
               + " as qi left outer join "
               + PSSqlHelper.qualifyTableName("CONTENTSTATUS")
               + " as cs on qi.CONTENTID = cs.CONTENTID group by qi.CONTENTID "
               + "having coalesce(min(cs.CURRENTREVISION),-2)=-2  or  ( min(qi.REVISIONID) <= min(cs.CURRENTREVISION) and ( max(qi.CREATED) <= :minDelayDate) or min(qi.CREATED) <= :maxDelayDate ) order by min(qi.PRIORITY) asc, "
               + "min(qi.CREATED) asc";
      } catch (SQLException e) {
         throw new RuntimeException(e);
      }

      SQLQuery query = sess.createSQLQuery(sql);
         query.setTimestamp("minDelayDate", minDelayDate);
         query.setTimestamp("maxDelayDate", maxDelayDate);

         if (count > 0)
         {
            query.setMaxResults(count);
         }

         List<Object> idRows = query.list();

         // Now we have a good priorized orderd list of ids. Now get the items
         // for the ids

         

         if (idRows.size() > 0)
         {
            List<Integer> idList = new ArrayList<Integer>();

            for (Object r : idRows)
            {
               idList.add(((Integer) r));
            }
            
            // Once we have selected the ids to process the following query will get all change events for the set of ids.
            // We will ignore revisions higher than current revision.

            try {
               sql = "select qi.*, cs.CURRENTREVISION from "
                     + PSSqlHelper.qualifyTableName("PSX_SEARCHINDEXQUEUE")
                     + " as qi left outer join "
                     + PSSqlHelper.qualifyTableName("CONTENTSTATUS")
                     + " as cs on qi.CONTENTID = cs.CONTENTID where (cs.CURRENTREVISION is null  or   qi.REVISIONID  <= cs.CURRENTREVISION )  and qi.CONTENTID in (:idList) order by qi.QUEUEID asc";
            } catch (SQLException e) {
               throw new RuntimeException(e);
            }

            query = sess.createSQLQuery(sql);
            query.addEntity(PSSearchIndexQueueItem.class);
            query.addScalar("CURRENTREVISION", StandardBasicTypes.LONG);
            query.setParameterList("idList", idList);

            List<Object[]> results = query.list();
            for (Object[] result : results)
            {
               PSSearchIndexQueueItem item = (PSSearchIndexQueueItem)result[0];
               if (result[1]==null)
                  item.setRevisionId(-2);
               queueItems.add(item);
            }
           
            m_logger.debug("Pulling ids from search index queue " + idList + " found " + queueItems.size());
         }

         return queueItems;

   }

   @Transactional
   public int saveItem(PSSearchIndexQueueItem item)
   {
      notNull(item, "Queue Item must not be null!");

      Session sess = sessionFactory.getCurrentSession();
      try
      {
         item.setQueueId(PSIdGenerator.getNextId(PSSearchIndexEventQueue.QUEUE_THREAD_NAME));
         sess.save(item);
         updatePollTime(item.getCreated());
         return (item.getQueueId());
      }
      catch (SQLException e)
      {
         String msg = "Failed save ";

         m_logger.error(msg, e);

         throw new RuntimeException(msg, e);
      }


   }

   // see base class method for details
   @Transactional
   public void deleteItems(Collection<Integer> queueIds)
   {
      if (queueIds != null && queueIds.size() > 0)
      {
         if (queueIds.size() < MAX_SQL_IN_LIST)
         {
            deleteItemsChunk(queueIds);
         }
         else
         {
            for (int inc = 0; inc < queueIds.size(); inc += MAX_SQL_IN_LIST)
            {
               int copyLength = Math.min(queueIds.size() - inc, MAX_SQL_IN_LIST);
               Collection<Integer> deleteList = new ArrayList<Integer>(((List<Integer>) queueIds).subList(inc, inc
                     + copyLength));
               deleteItemsChunk(deleteList);
            }
         }
      }
   }

   /**
    * This method is used by the public method to delete items in checks. This
    * is because oracle cannot handle more then 1000 items at a time
    * 
    * @param queueIds - Ids of items to delete Assumed not <code>null</null>
    * @throws DataAccessResourceFailureException
    * @throws IllegalStateException
    * @throws HibernateException
    */
   @Transactional
   public void deleteItemsChunk(Collection<Integer> queueIds) throws DataAccessResourceFailureException,
         IllegalStateException, HibernateException
   {
      Session sess = sessionFactory.getCurrentSession();

       Query sql = sess.createQuery("delete from PSSearchIndexQueueItem where m_queueId in (:queueIds)");
         sql.setParameterList("queueIds", queueIds);
         sql.executeUpdate();
         sess.flush();


   }
   
   /**
    * This method is used by the public method to delete all index items related
    * to an id.  This can be used to clean up purged items from the queue.
    * 
    * @param id - content id of item to delete related index queue items
    * @throws DataAccessResourceFailureException
    * @throws IllegalStateException
    * @throws HibernateException
    */
   @Transactional
   public void deleteIdItems(int id) throws DataAccessResourceFailureException,
         IllegalStateException, HibernateException
   {
      Session sess = sessionFactory.getCurrentSession();


         Query sql = sess.createQuery("delete from PSSearchIndexQueueItem where m_contentId = :contentId");
         sql.setInteger("contentId", id);
         sql.executeUpdate();
         sess.flush();

   }
   
   /**
    * This method is used by the public method to delete all index items related
    * to an id.  This can be used to clean up purged items from the queue.
    * 
    * @param id - content id of item to delete related index queue items
    * @throws DataAccessResourceFailureException
    * @throws IllegalStateException
    * @throws HibernateException
    */
   @Transactional
   public void deleteTypeIdItems(long id) throws DataAccessResourceFailureException,
         IllegalStateException, HibernateException
   {
      Session sess = sessionFactory.getCurrentSession();


         Query sql = sess.createQuery("delete from PSSearchIndexQueueItem where m_contentTypeId = :typeId");
         sql.setLong("typeId", id);
         sql.executeUpdate();
         sess.flush();

   }

   // see base class method for details
   @Transactional
   public void deleteAllItems()
   {
      Session sess = sessionFactory.getCurrentSession();


         Query sql = sess.createQuery("delete from PSSearchIndexQueueItem");
         sql.executeUpdate();

   }

   // see base class method for details
   public int getEventCount()
   {
      int queueCount = 0;
      Session sess = sessionFactory.getCurrentSession();

         // Chose to do count here as count method was causing performance issues being checked every change request insert
         // caused by event handling for monitor gadget.  This is running on a separate thread
      String sql = null;
      try {
         sql = "select count(distinct(qi.CONTENTID)) from "
                + PSSqlHelper.qualifyTableName("PSX_SEARCHINDEXQUEUE")
                + " as qi left outer join "
                + PSSqlHelper.qualifyTableName("CONTENTSTATUS")
                + " as cs on qi.CONTENTID = cs.CONTENTID where cs.CONTENTID is null or qi.REVISIONID <= cs.CURRENTREVISION";
      } catch (SQLException e) {
         throw new RuntimeException(e);
      }

      SQLQuery query = sess.createSQLQuery(sql);
         Number queueNumber = (Number) query.uniqueResult();
         if (queueNumber == null)
            queueCount = 0;
         else
            queueCount = queueNumber.intValue();

      return queueCount;
   }
   
   /**
    * When we add an item to the queue there is a delay before it is available to the indexer.
    * We want to only poll database when events are added but if we check immediately we will miss
    * the new events due to the delay.  When we create an update we will update a wait time and
    * notify the queue if it is waiting for events.  The queue can use this to know how long
    * to wait before trying again.
    * 
    * @param date
    */
   private void updatePollTime(Date date)
   {
      // Ste update inteval such that date is not reset for every event but
      // within a second window
      Date newPollTime = DateUtils.round(DateUtils.addMilliseconds(date, POLL_DELAY), Calendar.SECOND);
      if (newPollTime.compareTo(nextPollTime) > 0)
      {
         synchronized (nextPollTime)
         {
            if (newPollTime.compareTo(nextPollTime) > 0)
            {
               synchronized (pollMonitor)
               {
                  
                  nextPollTime = DateUtils.round(DateUtils.addMilliseconds(date, POLL_DELAY + 1000), Calendar.SECOND);
                  pollMonitor.notify();
               }
            }
         }
      }

   }
   
   /**
    * Will wait for an event to be added and then an approprate delay to make
    * sure the new event is available to the indexer query. Or will wait for an
    * existing recent event.
    * 
    * @throws InterruptedException
    */
   public void waitForPoll(long timeout) throws InterruptedException
   {
      // Only gets called when database returning no items. Will wait to pick up
      // recent items
      // Or will wait util a new event is added. Sleep is called so recent item
      // will be available to queue when requested.
 
      long currPollTime = nextPollTime.getTime();
      long diff = currPollTime - System.currentTimeMillis();

      if (diff > 0)
      {
         m_logger.debug("Waiting "+ diff +"ms for delayed items");
         Thread.sleep(diff);
      }
      else
      {
         // No items updated in delay period so don't do anything until we
         // get an update.
         boolean skipWait = false;
         synchronized (pollMonitor)
         {
            pollMonitor.wait(timeout);
            // An item may have been added to queue, now wait for delay period
            currPollTime = nextPollTime.getTime();
            skipWait = skipPollWait;
            skipPollWait = false;
         }
         if (shutdown || skipWait)
            return;
         
         diff = currPollTime - System.currentTimeMillis();
         if (diff >0)
         {
            m_logger.debug("Waiting "+ diff +"ms for delayed items after new events");
            Thread.sleep(diff);
         }
      }

   }
   
   public void pollNow() {
      synchronized (pollMonitor)
      {
         skipPollWait = true;
         pollMonitor.notify();
      }
   }
   
   public boolean isShutdown()
   {
      return shutdown;
   }
   
   
   public void shutdown()
   {
      synchronized (pollMonitor)
      {
         pollMonitor.notify();
         shutdown = true;
      }
   }
    
  
   private boolean shutdown = false;
   
 
   private volatile static Date nextPollTime = new Date();

   private static Object pollMonitor = new Object();
   private volatile static boolean skipPollWait = false;
   
   /**
    * Maximum items that can be used by the in list of the delete SQL command.
    * The limit is currently 1000 for Oracle.
    */
   private static final int MAX_SQL_IN_LIST = 1000;
   
   /**
    * We keep a local copy to decrease the performance impact and to make the
    * code a little clearer. Initialized during class construction, then never
    * <code>null</code> or modified.
    */
   private static final Logger m_logger = LogManager.getLogger(PSSearchIndexQueue.class);
}
