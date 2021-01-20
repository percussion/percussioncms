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
package com.percussion.services.utils.orm;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.utils.orm.data.PSTempId;
import com.percussion.services.utils.orm.data.PSTempIdPK;
import com.percussion.utils.timing.PSTimer;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

/**
 * This class manages id sets in a temporary table in the database. It is
 * initialized by the spring bean configuration.
 * <H2>Description</h2>
 * Hibernate has an important limitation that an <em>in</em> clause is
 * implemented as a series of comparisons. For example:
 * <pre>
 *     x in (1, 2, 3)
 * </pre>
 * becomes
 * <pre>
 *     (x = 1) or (x = 2) or (x = 3)
 * </pre>
 * further, hibernate passes the values 1, 2 and 3 as parameters, so the
 * expression is really:
 * <pre>
 *     (x = ?) or (x = ?) or (x = ?)
 * </pre>
 * with 1, 2, and 3 passed as parameters. This proves to be a problem for JDBC
 * drivers as the total number of passed parameters is limited. For jTDS this is
 * around 1000, and Oracle has a similar limitation. This then requires that we
 * avoid the problem.
 * <p>
 * This class implements a temporary storage pool that allows a collection of
 * numeric ids to be stored away. The user of this can take one of a couple of
 * approaches: either do this all the time, or only do this when the collection
 * is large.
 * {@link IPSCmsObjectMgr#filterItemsByPublishableFlag(java.util.List, List)}
 * does this in a conditional fashion, as does the query engine.
 * <h2>Example usage:</h2>
 * <pre>
 * idset = PSDataCollectionHelper.createIdSet(s, cids);
 * q = s.createQuery(&quot;SELECT c.m_contentId &quot;
 *       + &quot;FROM PSComponentSummary c, PSState s, PSTempId t &quot;
 *       + &quot;WHERE c.m_contentId = t.pk.itemId AND &quot; 
 *       + &quot;t.pk.id = :idset AND &quot;
 *       + &quot;c.m_workflowAppId = s.workflowId AND &quot;
 *       + &quot;c.m_contentStateId = s.stateId AND &quot;
 *       + &quot;s.contentValidValue in (:flags)&quot;);
 * q.setParameter(&quot;idset&quot;, idset);
 * q.setParameterList(&quot;flags&quot;, flags);
 * </pre>
 * This sets up the collection, which is then passed as just an id to the join
 * in the query. Note the use of <em>t.pk</em> to find the data. Later the
 * collection is removed:
 * <pre>
 * if (idset != 0)
 * {
 *    PSDataCollectionHelper.clearIdSet(s, idset);
 * }
 * </pre> 
 * IDs are allocated using the GUID manager, which ensures that multiple running
 * implementations will not collide in the table.
 * <p>
 * Note, due to a potential database deadlock (which we had experienced from
 * a SQL server, where sub-query select and delete IDs from the PSX_TEMPID table 
 * at the same time caused deadlock in SQL Server), {@link #executeQuery(Query)
 * and {@link #createIdSet(Session, Collection)} can be processed from more than one 
 * thread at the same time, but {@link #clearIdSet(Session, long)} can only be 
 * processed from one thread at a time.
 * 
 * @author dougrand
 */
public class PSDataCollectionHelper
{
   /**
    * This reader/writer lock allows safe clean-up IDs while
    * allowing queries and insertions. The read lock is taken for query
    * and insert operations. The write lock is taken when delete IDs.
    * <p>
    * Note, we have not experience the insert operation causing deadlocks so far,
    * so the insert operation does not have to be constrained by this internal
    * lock, but we choose to do so, just in case.
    * <p>
    * This reader/writer lock is used to prevent (database) deadlock that
    * we had experienced in SQL server, where sub-query select and delete IDs
    * from the PSX_TEMPID table at the same time caused deadlock in SQL Server.
    */
   private static ReentrantReadWriteLock ms_rwlock = new ReentrantReadWriteLock(true);


   /**
    * The logger for this class.
    */
   private static Log ms_logger = LogFactory.getLog("PSDataCollectionHelper");
   
   /**
    * If there are more ids than this in a query, use the data service to store
    * the ids in an idset and join to PSTempId.id.id and PSTempId.id.itemId to
    * select. This number must be low enough to not trigger issues with any of
    * our supported databases and, in particular, JDBC driver implementations.
    */
   public static final int MAX_IDS = 650;

   /**
    * Initialize system by removing all idsets
    * 
    * @param sf the hibernate session factory, passed via the beans.xml file as
    *           a constructor argument
    */
   public PSDataCollectionHelper(SessionFactory sf) {
      //fixme this code should be moved as it breaks install if left here
      /*if (sf == null)
      {
         throw new IllegalArgumentException("sf may not be null");
      }
      Session session = null;
      try
      {
         session = sf.openSession();
         session.createQuery("delete from PSTempId").executeUpdate();
      }
      finally
      {
         if (session != null)
         {
            session.close();
         }
      }*/
   }

   /**
    * Clear the id set formed by an earlier call to
    * {@link #createIdSet(Session, List)} within the same transaction. The id
    * passed must match the earlier call.
    * <p>
    * Note, this method may be blocked if it is called from more than one threads,
    * and it will wait until previous calls to {@link #executeQuery(Query)} and
    * {@link #createIdSet(Session, Collection)} are finished.
    * 
    * @param session the hibernate session to use, never <code>null</code>
    * @param idset the id set to clear, must be a value from an earlier call to
    *           {@link #createIdSet(Session, List)} or problems may occur (such
    *           as removing the wrong set).
    */
   public static void clearIdSet(Session session, long idset)
   {
      notNull(session, "session may not be null");
      
      if (ms_logger.isDebugEnabled())
      {
         ms_logger.debug("clearIdSet() idset = " + idset);
      }
      
      ms_rwlock.writeLock().lock();
      try
      {
          session.createQuery("delete from PSTempId tid where tid.pk.id = :id")
             .setLong("id", idset).executeUpdate();
      }
      finally
      {
         ms_rwlock.writeLock().unlock();
      }
   }

   /**
    * Executes the specified query. 
    * <p>
    * This query execution and {@link #createIdSet(Session, Collection)} can be 
    * processed from more than one thread at the same time, but it has to wait 
    * until previous calls to {@link #clearIdSet(Session, long)} to be finished 
    * (from different thread).
    * <p>
    * The caller of {@link #clearIdSet(Session, long)} must call this
    * method to execute the query.
    *  
    * @param q the specified query, not <code>null</code>.
    * 
    * @return the query result in a list.
    */
   public static List<?> executeQuery(Query q)
   {
      notNull(q, "query (q) may not be null");

      PSTimer timer = new PSTimer(ms_logger);

      ms_rwlock.readLock().lock();

      try
      {
         return q.list();
      }
      finally
      {
         ms_rwlock.readLock().unlock();

         if (ms_logger.isDebugEnabled())
            timer.logElapsed("Execute HQL \"" + q.getQueryString() + "\"");
      }
   }

   /**
    * Create the id set. The IDs passed in the list are inserted into the table.
    * <p>
    * This method and {@link #createIdSet(Session, Collection)} can be 
    * processed from more than one thread at the same time, but it has to wait 
    * until previous calls to {@link #clearIdSet(Session, long)} to be finished 
    * (from different thread).
    * 
    * @param session the hibernate session to use, never <code>null</code>
    * @param ids a collection of integral ids to persist, never 
    *       <code>null</code>. Note that the values are coerced to 
    *       <code>long</code> values, so do not pass floating point values.
    * @return an id to be used in the call to 
    *       {@link #clearIdSet(Session, long)}.
    */
   public static long createIdSet(Session session,
         Collection<? extends Number> ids)
   {
      notNull(session, "session may not be null");
      notNull(ids, "ids may not be null");

      PSTimer timer = new PSTimer(ms_logger);

      ms_rwlock.readLock().lock();
      try
      {
         return doCreateIdSet(session, ids);
      }
      finally
      {
         ms_rwlock.readLock().unlock();

         timer.logElapsed("Done insert " + ids.size() + " IDs.");
      }
   }
   
   /**
    * The same as {@link #createIdSet(Session, Collection)} except this method
    * does not involves read/write locks.
    */
   private static long doCreateIdSet(Session session, 
         Collection<? extends Number> ids)
   {
      StatelessSession s = session.getSessionFactory().openStatelessSession();
      Transaction tx = s.beginTransaction();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      long rval = gmgr.createGuid(PSTypeEnum.TEMP_IDS).longValue();

      try
      {
         // Create and persist
         for (Number id : ids)
         {
            PSTempIdPK pk = new PSTempIdPK(rval, id.longValue());
            PSTempId tempid = new PSTempId(pk);
            s.insert(tempid);
         }
      }
      finally
      {
         tx.commit();
         s.close();
      }

      return rval;
   }
}
