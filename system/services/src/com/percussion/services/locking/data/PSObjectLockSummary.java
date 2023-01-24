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
package com.percussion.services.locking.data;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Objects;

/**
 * Lightweight object lock information.
 */
public class PSObjectLockSummary
{
   /**
    * Default constructor.
    */
   public PSObjectLockSummary()
   {
   }
   
   /**
    * Create a new lock information object.
    * 
    * @param session the session that has the object locked, not 
    *    <code>null</code> or empty.
    * @param locker the user who has the object locked, not <code>null</code>
    *    or empty.
    * @param remainingTime the remaining time of the lock, must be > 0.
    */
   public PSObjectLockSummary(String session, String locker, long remainingTime)
   {
      setSession(session);
      setLocker(locker);
      setRemainingTime(remainingTime);
   }
   
   /**
    * @return the user who has this object locked, never <code>null</code>
    *    or empty.
    */
   public String getLocker()
   {
      return locker;
   }
   
   /**
    * @param locker the user who has this object locked, not 
    *    <code>null</code> or empty.
    */
   public void setLocker(String locker)
   {
      if (StringUtils.isBlank(locker))
         throw new IllegalArgumentException(
            "locker cannot be null or empty");
      
      this.locker = locker;
   }
   /**
    * @return the remaining lock time, always > 0.
    */
   public long getRemainingTime()
   {
      return remainingTime;
   }
   /**
    * @param remainingTime the remaining lock time, must be > 0.
    */
   public void setRemainingTime(long remainingTime)
   {
      if (remainingTime <= 0)
         throw new IllegalArgumentException("remainingTime must be > 0");

      this.remainingTime = remainingTime;
   }
   /**
    * @return the session who has this object locked, never <code>null</code>
    *    or empty.
    */
   public String getSession()
   {
      return session;
   }
   /**
    * @param session the session who has this object locked, not
    *    <code>null</code> or empty.
    */
   public void setSession(String session)
   {
      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException(
            "session cannot be null or empty");
      
      this.session = session;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSObjectLockSummary)) return false;
      PSObjectLockSummary that = (PSObjectLockSummary) o;
      return getRemainingTime() == that.getRemainingTime() &&
              getSession().equals(that.getSession()) &&
              getLocker().equals(that.getLocker());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getSession(), getLocker(), getRemainingTime());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSObjectLockSummary{");
      sb.append("session='").append(session).append('\'');
      sb.append(", locker='").append(locker).append('\'');
      sb.append(", remainingTime=").append(remainingTime);
      sb.append('}');
      return sb.toString();
   }

   /**
    * The session which has this object locked, never <code>null</code> or
    * empty.
    */
   private String session;
   
   /**
    * The user who has this object locked, never <code>null</code> or empty.
    */
   private String locker;
   
   /**
    * The remaining lock time, always > 0.
    */
   private long remainingTime;
}
