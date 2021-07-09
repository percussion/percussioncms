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
package com.percussion.services.locking.data;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
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

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
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
