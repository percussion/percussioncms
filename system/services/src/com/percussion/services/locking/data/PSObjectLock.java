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
package com.percussion.services.locking.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * This object represents a single object lock.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSObjectLock")
@Table(name = "PSX_LOCKS")
public class PSObjectLock implements Serializable
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 2763322019031010678L;

   @Id
   @Column(name = "ID", nullable = false)
   private long id;

   @Basic
   @Column(name = "OBJECTID", nullable = false)
   private long objectId;

   @Basic
   @Column(name = "LOCKTIME", nullable = false)
   private long lockTime;

   @Basic
   @Column(name = "EXPIRATIONTIME", nullable = false)
   private long expirationTime;

   @Basic
   @Column(name = "LOCKSESSION", nullable = false, length = 50)
   private String lockSession;

   @Basic
   @Column(name = "LOCKER", nullable = false, length = 50)
   private String locker;

   @Basic
   @Column(name = "LOCKEDVERSION", nullable = true)
   private Integer lockedVersion;
   
   /**
    * The lock time interval in milliseconds.
    */
   public static final long LOCK_INTERVAL = 30 * 60 * 1000;

   /**
    * Default constructor.
    *
    */
   public PSObjectLock()
   {
   }
   
   /**
    * Convenience constructor to create a new object lock for the default
    * interval. Calls {@link #PSObjectLock(IPSGuid, String, String, Integer, 
    * long) PSObjectLock(id, session, locker, version, LOCK_INTERVAL)}.
    */
   public PSObjectLock(IPSGuid id, String session, String locker, 
      Integer version)
   {
      this(id, session, locker, version, LOCK_INTERVAL);
   }
   
   /**
    * Constructs a new object lock for the supplied parameters. The returned
    * object is fully initialized including a valid GUID and ready to be
    * saved to the repository.
    * 
    * @param id the id of the object to lock, not <code>null</code>.
    * @param session the session of the lock holders, not <code>null</code>
    *    or empty.
    * @param locker the name of the lock holder, not <code>null</code> or empty.
    * @param version the version of the locked object, <code>null</code> if 
    *    the locked object does not use a version.
    * @param interval specifies the time in milliseconds how long the 
    *    requested lock will be held. Must be minimum 1000 ms. 
    */
   public PSObjectLock(IPSGuid id, String session, String locker, 
      Integer version, long interval)
   {
      setObjectId(id);
      setLockSession(session);
      setLocker(locker);
      setLockedVersion(version);
      
      updateLockTime(interval);
      
      setGUID(PSGuidManagerLocator.getGuidMgr().createGuid(
         PSTypeEnum.OBJECT_LOCK));
   }
   
   /**
    * Get the lock id, which is unique across all lock objects.
    * 
    * @return the lock id, 0 if not assigned yet.
    */
   public long getId()
   {
      return id;
   }

   /**
    * Get the full guid of the object which this lock locks.
    * 
    * @return the full guid of the object that this lock locks.
    */
   public IPSGuid getObjectId()
   {
      return new PSDesignGuid(objectId);
   }
   
   /**
    * Set the guid of the object for which this holds the lock.
    * 
    * @param guid the guid of the object for which this holds the lock, not
    *    <code>null</code>.
    */
   private void setObjectId(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("id cannot be null");
      
      objectId = new PSDesignGuid(guid).getValue();
   }

   /**
    * Get the time when this lock was created in milliseconds.
    * 
    * @return the time when this lock was created in milliseconds, never 
    *    <code>null</code>.
    */
   public long getLockTime()
   {
      return lockTime;
   }
   
   /**
    * Updates the lock time to the current system time and the expiration time
    * to the current time with the supplied interval added.
    * 
    * @param interval specifies the time in milliseconds how long the 
    *    requested lock will be held, must be minimum 1000 ms. 
    */
   public void updateLockTime(long interval)
   {
      if (interval < 1000)
         throw new IllegalArgumentException("interval must be minimum 1000ms");
      
      lockTime = System.currentTimeMillis();
      expirationTime = lockTime + interval;
   }
   
   /**
    * Get the lock expiration time in milliseconds.
    * 
    * @return the lock expiration time in milliseconds, never <code>null</code>.
    */
   public long getExpirationTime()
   {
      return expirationTime;
   }
   
   /**
    * Get the remaining time of this lock in minutes.
    * 
    * @return the remaining lock time in minutes.
    */
   public long getRemainingTime()
   {
      return (getExpirationTime() - System.currentTimeMillis()) / (1000 * 60);
   }

   /**
    * Get the session which holds this lock.
    * 
    * @return the session of the lock holder, never <code>null</code> or empty.
    */
   public String getLockSession()
   {
      return lockSession;
   }
   
   /**
    * Set a new session which holds this lock.
    * 
    * @param lockSession the new session holding this lock, not 
    *    <code>null</code> or empty.
    */
   public void setLockSession(String lockSession)
   {
      if (StringUtils.isBlank(lockSession))
         throw new IllegalArgumentException("lockSession cannot be null");
      
      this.lockSession = lockSession;
   }

   /**
    * Get the name of the lock holder.
    * 
    * @return the name of the lock holder, never <code>null</code> or empty.
    */
   public String getLocker()
   {
      return locker;
   }
   
   /**
    * Set the holder of this lock.
    * 
    * @param locker the new lock holder, not <code>null</code> or empty.
    */
   private void setLocker(String locker)
   {
      if (StringUtils.isBlank(locker))
         throw new IllegalArgumentException("locker cannot be null or empty");
      
      this.locker = locker;
   }
   
   /**
    * Get the version of the locked object.
    * 
    * @return the version of the locked object, <code>null</code> if the 
    *    object does not use a version.
    */
   public Integer getLockedVersion()
   {
      return lockedVersion;
   }
   
   /**
    * Set the version of the locked object.
    * 
    * @param lockedVersion the new version of the locked object, 
    *    <code>null</code> if the locked object does not use a version. 
    *    Must be >= 0.
    */
   public void setLockedVersion(Integer lockedVersion)
   {
      if (lockedVersion != null && lockedVersion < 0)
         throw new IllegalArgumentException("lockedVersion must be >= 0");
      
      this.lockedVersion = lockedVersion;
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.OBJECT_LOCK, id);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#setGUID(IPSGuid)
    */
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid may not be null");

      if (id != 0)
         throw new IllegalStateException("cannot change existing guid");

      id = newguid.longValue();
   }

   @Override
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }
}
