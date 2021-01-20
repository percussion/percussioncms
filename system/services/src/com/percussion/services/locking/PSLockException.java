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
package com.percussion.services.locking;

import com.percussion.services.locking.data.PSObjectLock;
import com.percussion.utils.exceptions.PSBaseException;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Generic problem in locking, the message will indicate what issue occurred.
 * <p>
 * This class is used for both single exception cases and multi-object operation
 * exceptions. In the latter case, there may be successful results and 1 or more
 * errors. Only <code>getResults</code> and <code>getErrors</code> are
 * meaningful for multi-operation exceptions. Similarly for the single op
 * exception cases for the other get operations.
 */
public class PSLockException extends PSBaseException
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 8098402028890822664L;

   /**
    * This ctor is used when bulk operations fail. It includes the successful
    * results and an exception for each error. 
    * 
    * @param successes An entry for each attempt. Either a valid lock, or
    * <code>null</code> if a lock could not be obtained for the corresponding
    * id. If <code>null</code>, there must be an entry in the
    * <code>errors</code> list. The order of the results is the same as the
    * order of the original request. May be <code>null</code> or empty.
    * 
    * @param errors The exception that occurred for each id that has a
    * <code>null</code> entry in the <code>successes</code> list. Never
    * <code>null</code> or empty.
    */
   public PSLockException(List<PSObjectLock> successes,
         Map<IPSGuid, PSLockException> errors)
   {
      super(IPSLockErrors.MULTI_OPERATION);
      if (errors == null || errors.isEmpty())
      {
         throw new IllegalArgumentException("errors cannot be null or empty");
      }
      
      if (successes == null)
         successes = Collections.emptyList();
      m_results = successes;
      m_errors = errors;
   }
   
   /**
    * Convenience constructor that calls {@link #PSLockException(int, long, 
    * String, long) PSLockException(msgCode, id, null, -1)}.
    */
   public PSLockException(int msgCode, long id)
   {
      this(msgCode, id, null, -1);
   }
   
   /**
    * Convenience constructor that calls {@link #PSLockException(int, long, 
    * String, long, Throwable) PSLockException(msgCode, id, locker, 
    * remainingTime, null)}.    */
   public PSLockException(int msgCode, long id, String locker, 
      long remainingTime)
   {
      this(msgCode, id, locker, remainingTime, null);
   }

   /**
    * Constructs a new lock exception for the suppliedd parameters.
    * 
    * @param msgCode the error code.
    * @param id the object id for which the lock failed.
    * @param locker the name of the user who has the requested object locked,
    *    may be <code>null</code> if the object is not locked, never empty.
    * @param remainingTime the remaining time of the current lock, -1 if the
    *    requested object is not locked.
    * @param cause the original exception that caused this exception to be
    *    thrown, may be <code>null</code>.
    */
   public PSLockException(int msgCode, long id, String locker, 
      long remainingTime, Throwable cause)
   {
      super(msgCode, cause, new Object[] {id, locker, remainingTime});
      
      if (locker != null && locker.trim().length() == 0)
         throw new IllegalArgumentException("locker cannot be empty");
      
      if (StringUtils.isNotEmpty(locker) && remainingTime <= 0)
         throw new IllegalArgumentException("remainingTime must be > 0");
      
      m_id = id;
      m_locker = locker;
      m_remainingTime = remainingTime;
   }
   
   /**
    * Contains an entry for each id supplied to the original operation. The
    * entry is either a valid lock, or <code>null</code>. If
    * <code>null</code>, there will be an entry in the errors list, obtained
    * by calling {@link #getErrors()}.
    * 
    * @return If this is a multi-operation exception, then a non-
    * <code>null</code> list is returned, otherwise the return is
    * <code>null</code>.
    */
   public List<PSObjectLock> getResults()
   {
      return Collections.unmodifiableList(m_results);
   }
   
   /**
    * Contains an entry for every entry that failed to obtain a lock. The
    * exception explains why for that id.
    * 
    * @return If this is a multi-operation exception, then a non-
    * <code>null</code>, non empty list is returned, otherwise the return is
    * <code>null</code>.
    */
   public Map<IPSGuid, PSLockException> getErrors()
   {
      return Collections.unmodifiableMap(m_errors);
   }
   
   /**
    * Get the id of the object for which this exception was thrown.
    *   
    * @return the id of the object for which this exception was thrown if this
    * is a single-op error, otherwise -1.
    */
   public long getId()
   {
      return m_id;
   }
   
   /**
    * Get the name of the user who locks the requested object.
    * 
    * @return the name of the user who locks the requested object, may be 
    *    <code>null</code> if the requested object is not locked. If this is a
    *    multi-op error, it is <code>null</code>.
    */
   public String getLocker()
   {
      return m_locker;
   }
   
   /**
    * Get the remaining lock time.
    * 
    * @return the remaining lock time, -1 if the requested object is not locked.
    * If this is a multi-op error, -1.
    */
   public long getRemainigTime()
   {
      return m_remainingTime;
   }

   /* (non-Javadoc)
    * @see PSBaseException#getResourceBundleBaseName()
    */
   @Override
   protected String getResourceBundleBaseName()
   {
      return "com.percussion.services.locking.PSLockErrorStringBundle";
   }
   
   /**
    * The id of the object we tried to lock, extend or release.
    */
   private long m_id = -1;
   
   /**
    * The name of the user who has the requested object locked. May be 
    * <code>null</code> if the requested object is not locked.
    */
   private String m_locker;
   
   /**
    * The remaining time of the existing lock, -1 if the requested object is
    * not locked.
    */
   private long m_remainingTime = -1;
   
   /**
    * See {@link #getResults()} for details. <code>null</code> unless this
    * object was created using {@link #PSLockException(List, Map)this ctor}, in
    * which case it is never <code>null</code>.
    */
   private List<PSObjectLock> m_results = null;

   /**
    * See {@link #getErrors()} for details. <code>null</code> unless this
    * object was created using {@link #PSLockException(List, Map)this ctor}, in
    * which case it is never <code>null</code>.
    */
   private Map<IPSGuid, PSLockException> m_errors = null;
}
