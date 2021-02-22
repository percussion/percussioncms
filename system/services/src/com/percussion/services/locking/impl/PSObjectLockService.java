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
package com.percussion.services.locking.impl;

import com.percussion.server.PSUserSession;
import com.percussion.server.PSUserSessionManager;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.locking.IPSLockErrors;
import com.percussion.services.locking.IPSObjectLockService;
import com.percussion.services.locking.PSLockException;
import com.percussion.services.locking.data.PSObjectLock;
import com.percussion.services.utils.orm.PSCriteriaQueryRepeater;
import com.percussion.services.utils.orm.PSORMUtils;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.PSWebserviceErrors;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Implements all services used to manage object locks 
 * represented through the <code>PSObjectLock</code> object type.
 */
@Transactional
@PSBaseBean("sys_lockingService")
public class PSObjectLockService
   implements IPSObjectLockService
{
   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   //see interface
   public synchronized PSObjectLock createLock(IPSGuid id, String session, 
         String locker, Integer version, boolean overrideLock) 
         throws PSLockException
   {
      try
      {
         List<PSObjectLock> lock = createLocks(Collections.singletonList(id),
               session, locker, Collections.singletonList(version), overrideLock);
         return lock.get(0);
      }
      catch (PSLockException e)
      {
         throw e.getErrors().values().iterator().next();
      }
   }
   
   /* (non-Javadoc)
    * @see IPSObjectLockService#createLocks(List, String, String, List, boolean)
    */
   public synchronized List<PSObjectLock> createLocks(List<IPSGuid> ids,
         String session, String locker, List<Integer> versions,
         boolean overrideLock) throws PSLockException
   {
      if (ids == null || ids.isEmpty())
         throw new IllegalArgumentException("ids cannot be null or empty");
      
      if (versions == null)
         throw new IllegalArgumentException("versions cannot be null");
      
      if (ids.size() != versions.size())
      {
         throw new IllegalArgumentException(
               "ids and versions must be same size");
      }
      
      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (StringUtils.isBlank(locker))
         throw new IllegalArgumentException("locker cannot be null or empty");
      
      String lockSession = getLockSession(session);
      
      List<PSObjectLock> locks = findLocksByObjectIds(ids, null, null);
      Map<IPSGuid, PSObjectLock> idToLock = new HashMap<>();
      for (PSObjectLock l : locks)
      {
         idToLock.put(l.getObjectId(), l);
      }
      
      List<PSObjectLock> toSave = new ArrayList<>();
      Iterator<Integer> versionIter = versions.iterator();
      Map<IPSGuid, PSLockException> errors = 
         new HashMap<>();
      Map<IPSGuid, PSObjectLock> existingLocks = 
         new HashMap<>();
      
      for (IPSGuid id : ids)
      {
         PSObjectLock lock = idToLock.get(id);
         Integer version = versionIter.next();
         //assume that expired lock is rare, so don't need to batch it
         lock = releaseExpiredLock(lock);
         if (lock == null)
         {
            // create the new lock
            lock = new PSObjectLock(id, lockSession, locker, version);
            toSave.add(lock);
         }
         else
         {
            if (!lock.getLocker().equals(locker))
            {
               errors.put(id, new PSLockException(
                  IPSLockErrors.LOCK_EXTENSION_LOCKED_BY_SOMEBODY_ELSE, 
                  new PSDesignGuid(id).getValue(), lock.getLocker(), 
                  lock.getRemainingTime()));
            }
            else if (!overrideLock && !lock.getLockSession().equals(lockSession))
            {
               errors.put(id, new PSLockException(
                  IPSLockErrors.LOCK_EXTENSION_INVALID_SESSION, 
                  new PSDesignGuid(id).getValue()));
            }
            else if (overrideLock && !lock.getLockSession().equals(lockSession))
            {
               // override lock for same user
               lock.setLockSession(lockSession);
               toSave.add(lock);
            }
            else
            {
               // extend the existing lock
               // assume this is rare and doesn't require bulk processing
               lock = extendLock(id, lockSession, locker, version);
               existingLocks.put(id, lock);
            }
         }
      }

      assert(existingLocks.size() + errors.size() + toSave.size() == ids.size());
      saveLocks(toSave);

      Map<IPSGuid, PSObjectLock> tmp = new HashMap<>();
      for (PSObjectLock l : existingLocks.values())
         tmp.put(l.getObjectId(), l);
      for (PSObjectLock l : toSave)
         tmp.put(l.getObjectId(), l);

      List<PSObjectLock> results = new ArrayList<>();
      for (IPSGuid id : ids)
      {
         results.add(tmp.get(id));
      }
      if (!errors.isEmpty())
      {
         throw new PSLockException(results, errors);
      }
            
      return results;
   }
   
   /* (non-Javadoc)
    * @see IPSObjectLockService#createLock(PSErrorResultsException, String, 
    *    String, boolean)
    * @todo - this method should not know about a webservice class, it would be
    * easy to refactor to correct this
    */
   public void createLocks(PSErrorResultsException results, String session, 
      String user, boolean overrideLock)
   {
      if (results == null)
         throw new IllegalArgumentException("results cannot be null");
      
      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");
      
      if (StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");
      
      Iterator resultIds = results.getResults().keySet().iterator();
      while (resultIds.hasNext())
      {
         IPSGuid id = (IPSGuid) resultIds.next();
         Object value = results.getResults().get(id);
         try
         {
            Integer version = PSORMUtils.getVersion(value);
            
            createLock(id, session, user, version, overrideLock);
         }
         catch (PSLockException e)
         {
            int code = IPSWebserviceErrors.CREATE_LOCK_FAILED;
            PSLockErrorException error = new PSLockErrorException(code, 
               PSWebserviceErrors.createErrorMessage(code, 
                  value.getClass().getName(), new PSDesignGuid(id).getValue(), 
                  e.getLocalizedMessage()), 
                  ExceptionUtils.getFullStackTrace(e), e.getLocker(), 
                  e.getRemainigTime());
            results.addError(id, error);
         }
      }
      
      Iterator errors = results.getErrors().keySet().iterator();
      while (errors.hasNext())
         results.removeResult((IPSGuid) errors.next());
   }

   /* (non-Javadoc)
    * @see IPSObjectLockService#extendLock(IPSGuid, String, String, Integer)
    */
   public PSObjectLock extendLock(IPSGuid id, String session, 
      String locker, Integer version) throws PSLockException
   {
      return extendLock(id, session, locker, version, 
         PSObjectLock.LOCK_INTERVAL);
   }

   //see interface
   public synchronized PSObjectLock extendLock(IPSGuid id, String session, 
         String locker, Integer version, long interval) throws PSLockException
   {
      try
      {
         return extendLocks(Collections.singletonList(id),
            session, locker, Collections.singletonList(version), interval).get(0);
      }
      catch (PSLockException e)
      {
         throw e.getErrors().values().iterator().next();
      }
      
   }
   
   /* (non-Javadoc)
    * @see IPSObjectLockService#extendLock(IPSGuid, String, String, Integer, 
    *    long)
    */
   public synchronized List<PSObjectLock> extendLocks(List<IPSGuid> ids,
         String session, String locker, List<Integer> versions, long interval)
      throws PSLockException
   {      
      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");
      
      if (StringUtils.isBlank(locker))
         throw new IllegalArgumentException("locker cannot be null or empty");
      
      if (interval < 1000)
         throw new IllegalArgumentException("interval must be minimum 1000ms");
    
      //validates ids contract
      List<PSObjectLock> locks = findLocksByObjectIds(ids, session, locker);
      /*
       * Do not release expired locks on purpose. If the lock has expired but
       * nobody else aquired one for the same object in the meantime, this
       * is still valid. This allows clients to extend locks after a server 
       * crash that was longer that 30 minutes ago.
       */
      Map<IPSGuid, PSLockException> errors = 
         new HashMap<>();
      
      Map<IPSGuid, PSObjectLock> idToLock = new HashMap<>();
      for (PSObjectLock l : locks)
      {
         idToLock.put(l.getObjectId(), l);
      }
      
      String normalizedSession = getLockSession(session);
      Iterator<Integer> verIter = versions.iterator();
      List<PSObjectLock> toSaveLocks = new ArrayList<>();
      for (IPSGuid id : ids)
      {
         PSObjectLock lock = idToLock.get(id);
         if (lock == null)
         {
            errors.put(id, new PSLockException(
               IPSLockErrors.LOCK_EXTENSION_NOT_LOCKED, 
               new PSDesignGuid(id).getValue()));
            continue;
         }
         
         if (!lock.getLocker().equals(locker))
         {
            errors.put(id, new PSLockException(
               IPSLockErrors.LOCK_EXTENSION_LOCKED_BY_SOMEBODY_ELSE, 
               new PSDesignGuid(id).getValue(), lock.getLocker(), 
               lock.getRemainingTime()));
            continue;
         }
         
         if (!lock.getLockSession().equals(normalizedSession))
         {
            errors.put(id, new PSLockException(
               IPSLockErrors.LOCK_EXTENSION_INVALID_SESSION, 
               new PSDesignGuid(id).getValue()));
            continue;
         }
         
         lock.updateLockTime(interval);
         Integer version = verIter.next();
         if (version != null)
            lock.setLockedVersion(version);
         toSaveLocks.add(lock);
      }
      saveLocks(toSaveLocks);
      
      if (!errors.isEmpty())
      {
         throw new PSLockException(toSaveLocks, errors);
      }
      return toSaveLocks;
   }

   /* (non-Javadoc)
    * @see IPSObjectLockService#findExpiredLocks()
    */
   @SuppressWarnings("unchecked")
   public List<PSObjectLock> findExpiredLocks()
   {
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSObjectLock.class);
         criteria.add(Restrictions.lt("expirationTime", 
            System.currentTimeMillis()));
         
         List<PSObjectLock> locks = criteria.list();
         
         return locks;

   }

   /* (non-Javadoc)
    * @see IPSObjectLockService#findLockByObjectId(IPSGuid)
    */
   @SuppressWarnings("unchecked")
   public PSObjectLock findLockByObjectId(IPSGuid id)
   {
      return findLockByObjectId(id, null, null);
   }
   
   /* (non-Javadoc)
    * @see IPSObjectLockService#findLockByObjectId(IPSGuid)
    */
   @SuppressWarnings("unchecked")
   public PSObjectLock findLockByObjectId(IPSGuid id, String lockSession, 
      String locker)
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be  null");
      
      Session session = sessionFactory.getCurrentSession();

         List<PSObjectLock> locks = findLocksByObjectIds(Collections
               .singletonList(id), lockSession, locker);
         
         // there must only be one lock for a specific object
         if (locks != null && locks.size() > 1)
            throw new IllegalStateException(
               "Found multiple locks for object with id " + id.toString());
         
         return (locks == null || locks.isEmpty()) ? 
            null : releaseExpiredLock(locks.get(0));

   }

   /* (non-Javadoc)
    * @see IPSObjectLockService#findLocksByObjectIds(List)
    * todo: either return a collection, or make the results match the order of 
    * the supplied ids
    */
   public List<PSObjectLock> findLocksByObjectIds(List<IPSGuid> ids, 
      final String lockSession, final String locker)
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException("ids cannot be null or empty");
      
      Session session = sessionFactory.getCurrentSession();

         PSCriteriaQueryRepeater<PSObjectLock> cr = 
            new PSCriteriaQueryRepeater<PSObjectLock>()
         {
            public void add(Criteria criteria, List<IPSGuid> ids)
            {
               criteria.add(Restrictions.in("objectId", 
                     PSGuidUtils.toFullLongList(ids)));
               if (!StringUtils.isBlank(lockSession))
                  criteria.add(Restrictions.eq("lockSession", 
                     getLockSession(lockSession)));
               if (!StringUtils.isBlank(locker))
                  criteria.add(Restrictions.eq("locker", locker));
            }
         };
         List<PSObjectLock> locks = cr.query(ids, session, PSObjectLock.class);
         
         return releaseExpiredLocks(locks);

   }
   
   /* (non-Javadoc)
    * @see IPSObjectLockService#loadLocksByIds(List)
    */
   @SuppressWarnings("unchecked")
   public List<PSObjectLock> loadLocksByIds(List<IPSGuid> ids)
   {
      return loadLocksByIds(ids, false);
   }
   
   /**
    * Load all locks by their id.
    * 
    * @param ids the ids of all locks to load, not <code>null</code> or empty.
    * @param skipRelease <code>true</code> if expired locks should not be
    *    releases before the return, <code>false</code> otherwise.
    * @return the requested locks, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   private List<PSObjectLock> loadLocksByIds(List<IPSGuid> ids, 
      boolean skipRelease)
   {
      if (ids == null)
         throw new IllegalArgumentException("ids cannot be null");
      
      if (ids.isEmpty())
         throw new IllegalArgumentException("ids cannot be empty");
      
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSObjectLock.class);
         criteria.add(Restrictions.in("id", PSGuidUtils.toFullLongList(ids)));
         
         List<PSObjectLock> locks = criteria.list();
         
         if (skipRelease)
            return locks;
         
         return releaseExpiredLocks(locks);

   }

   /* (non-Javadoc)
    * @see IPSObjectLockService#releaseLock(PSObjectLock)
    */
   public void releaseLock(PSObjectLock lock)
   {
      if (lock != null)
      {
         List<PSObjectLock> locks = new ArrayList<>();
         locks.add(lock);
         
         releaseLocks(locks);
      }
   }

   /* (non-Javadoc)
    * @see IPSObjectLockService#releaseLocks(List)
    */
   public synchronized void releaseLocks(List<PSObjectLock> locks)
   {
      if (locks != null && !locks.isEmpty())
      {
         List<IPSGuid> ids = new ArrayList<>();
         for (PSObjectLock lock : locks)
            ids.add(lock.getGUID());

         Session session = sessionFactory.getCurrentSession();
         loadLocksByIds(ids, true).forEach(lock -> session.delete(lock));
      }
   }
   
   /* (non-Javadoc)
    * @see IPSObjectLockService#isLockedFor(IPSGuid, String, String)
    */
   public boolean isLockedFor(IPSGuid id, String session, String locker)
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");
      
      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");
      
      if (StringUtils.isBlank(locker))
         throw new IllegalArgumentException("locker cannot be null or empty");
      
      PSObjectLock lock = findLockByObjectId(id, session, locker);
      
      // release the lock if it has expired
      lock = releaseExpiredLock(lock);
      
      return lock != null;
   }
   

   /* (non-Javadoc)
    * @see IPSObjectLockService#getVersion(IPSGuid)
    */
   public Integer getLockedVersion(IPSGuid id) throws PSLockException
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");
      
      PSObjectLock lock = findLockByObjectId(id);
      if (lock == null)
         throw new PSLockException(IPSLockErrors.OBJECT_NOT_LOCKED, 
            new PSDesignGuid(id).getValue());
      
      return lock.getLockedVersion();
   }
   
   /**
    * Save the supplied locks.
    * 
    * @param locks the lock to save, assumed not <code>null</code> and no
    * <code>null</code> entries.
    */
   private void saveLocks(Collection<PSObjectLock> locks)
   {
      Session session = sessionFactory.getCurrentSession();
      try
      {
         for (PSObjectLock lock : locks)
            session.saveOrUpdate(lock);
      }
      finally
      {
         session.flush();
      }
   }
   
   /**
    * Get the lock session used for object locks. This is either the session
    * id if no client id was supplied with the login request creating the 
    * session id, otherwise that client id will be used.
    * 
    * @param session the session id for which to get the lock session, assumed
    *    not <code>null</code> or empty.
    * @return the lock session sued to store with the lock objects, never 
    *    <code>null</code> or empty.
    */
   private String getLockSession(String session)
   {
      PSUserSession rxSession = PSUserSessionManager.getUserSession(session);
      String lockSession = null;
      if (rxSession != null)
         lockSession = (String) rxSession.getPrivateObject(
            PSUserSession.CLIENTID);
      if (lockSession == null)
         lockSession = session;
      
      return lockSession;
   }
   
   /**
    * Releases the supplied lock if it is expired.
    * 
    * @param lock the lock to be released if it is expired, may be
    *    <code>null</code>.
    * @return the supplied lock or <code>null</code> if it was expired.
    */
   private PSObjectLock releaseExpiredLock(PSObjectLock lock)
   {
      if (lock != null)
      {
         // release the lock if it is expired
         if (lock.getExpirationTime() < System.currentTimeMillis())
         {
            releaseLock(lock);
            lock = null;
         }
      }
      
      return lock;
   }
   
   /**
    * Release all expired locks and return the list with all still valid locks.
    * 
    * @param locks the locks to test for expiration, assumed not 
    *    <code>null</code>, may be empty.
    * @return a list with all still valid locks, never <code>null</code>,
    *    may be empty.
    */
   private List<PSObjectLock> releaseExpiredLocks(List<PSObjectLock> locks)
   {
      List<PSObjectLock> results = new ArrayList<>();
      for (PSObjectLock lock : locks)
      {
         PSObjectLock validLock = releaseExpiredLock(lock);
         if (validLock != null)
            results.add(validLock);
      }
      
      return results;
   }

   /* (non-Javadoc)
    * @see IPSObjectLockService#findLocksByUser(String, String)
    */
   @SuppressWarnings("unchecked")
   public List<PSObjectLock> findLocksByUser(String lockSession, String locker)
   {
      if (StringUtils.isBlank(lockSession))
         throw new IllegalArgumentException(
            "lockSession cannot be null or empty");

      if (StringUtils.isBlank(locker))
         throw new IllegalArgumentException(
            "locker cannot be null or empty");
      
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSObjectLock.class);
         criteria.add(Restrictions.eq("lockSession", 
            getLockSession(lockSession)));
         criteria.add(Restrictions.eq("locker", locker));

         List<PSObjectLock> locks = criteria.list();
         
         return releaseExpiredLocks(locks);


   }
}
