/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.webservices;

import com.percussion.services.locking.PSLockException;
import com.percussion.services.locking.data.PSObjectLock;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This exception describes errors that happend in web service lock calls.
 * <p>
 * This exception is identical to
 * {@link com.percussion.services.locking.PSLockException}, except for
 * difference in the base class.
 */
public class PSLockErrorException extends PSErrorException
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = -3956879082678029069L;

   /**
    * The name of the user holding the lock, may be <code>null</code> if the
    * requested object was not locked, never empty.
    */
   private String locker;
   
   /**
    * The remaining time of the current lock, -1 if the requested object is
    * not locked.
    */
   private long remainingTime;
   
   /**
    * See {@link #getResults()} for details. <code>null</code> unless this
    * object was created using {@link #PSLockErrorException(List, Map)this ctor},
    * in which case it is never <code>null</code>.
    */
   private List<PSObjectLock> m_results = null;

   /**
    * See {@link #getErrors()} for details. <code>null</code> unless this
    * object was created using {@link #PSLockErrorException(List, Map)this ctor},
    * in which case it is never <code>null</code>.
    */
   private Map<IPSGuid, PSLockException> m_errors = null;
   
   /*(non-Javadoc)
    * @see PSErrorException#PSErrorException()
    */
   public PSLockErrorException()
   {}

   /**
    * This ctor is used when bulk operations fail. It includes the successful
    * results and an exception for each error. O
    * 
    * @param successes An entry for each attempt. Either a valid lock, or
    * <code>null</code> if a lock could not be obtained for the corresponding
    * id. If <code>null</code>, there must be an entry in the
    * <code>errors</code> list. The order of the results is the same as the
    * order of the original request. Never <code>null</code> or empty.
    * 
    * @param errors The exception that occurred for each id that has a
    * <code>null</code> entry in the <code>successes</code> list. Never
    * <code>null</code> or empty.
    */
   public PSLockErrorException(List<PSObjectLock> successes,
         Map<IPSGuid, PSLockException> errors)
   {
      if (successes == null || successes.isEmpty())
      {
         throw new IllegalArgumentException("errors cannot be null or empty");
      }
      if (errors == null || errors.isEmpty())
      {
         throw new IllegalArgumentException("errors cannot be null or empty");
      }
      
      m_results = successes;
      m_errors = errors;
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
   
   /*(non-Javadoc)
    * @see PSErrorException#PSErrorException(int, String, String)
    */
   public PSLockErrorException(int code, String message, String stack)
   {
      super(code, message, stack);
   }

   /**
    * Constructor the adds additional parameters to 
    * {@link #PSLockErrorException(int, String, String)}.
    * 
    * @param locker the name of the user locking the requested object, may
    *    be <code>null</code>, not empty.
    * @param remainingTime the remaining lock time, is ignored if the supplied
    *    locker is <code>null</code>.
    */
   public PSLockErrorException(int code, String message, String stack, 
      String locker, long remainingTime)
   {
      super(code, message, stack);
      
      setLocker(locker);
      if (locker != null)
         setRemainingTime(remainingTime);
   }
   
   /**
    * Get the name of the user locking the requested object.
    * 
    * @return the name of the user holding the lock, mey be <code>null</code>,
    *    mot empty.
    */
   public String getLocker()
   {
      return locker;
   }
   
   /**
    * Set the name of the user holding the lock for the requested object.
    * 
    * @param locker the name of the lock holder, may be <code>null</code> if
    *    the requested object is not locked, not empty.
    */
   public void setLocker(String locker)
   {
      if (locker != null && locker.trim().length() == 0)
         throw new IllegalArgumentException("locker cannot be empty");
      
      this.locker = locker;
   }
   
   /**
    * Get the remaining lock time.
    * 
    * @return the remaining lock time in minutes, -1 if the requested object
    *    is not locked.
    */
   public long getRemainingTime()
   {
      return remainingTime;
   }
   
   /**
    * Set the new remaining lock time.
    * 
    * @param remainingTime the new remaining lock time in minutes, -1 if
    *    the requested object is not locked.
    */
   public void setRemainingTime(long remainingTime)
   {
      this.remainingTime = remainingTime;
   }
}

