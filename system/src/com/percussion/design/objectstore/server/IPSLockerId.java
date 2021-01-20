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
package com.percussion.design.objectstore.server;

import com.percussion.design.objectstore.PSLockedException;

import java.util.Properties;

public interface IPSLockerId
{
   /**
    * Reads all uniquely identifying properties from the given properties
    * object.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/8/6
    * 
    * 
    * @param   props
    * 
    */
   public void readFrom(Properties props);

   /**
    * Writes all uniquely identifying properties to the given properties
    * object.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/8/6
    * 
    * 
    * @param   props
    * 
    */
   public void writeTo(Properties props);
   
   /**
    * Returns <CODE>true</CODE> if this locker id is the same
    * as the given locker id. This may or may not be consistent
    * with the equals method for this object (for example, under
    * some situations, the implementation is free to treat two
    * distinct ids as the same id).
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/8/6
    * 
    * @param   other The other id
    *
    * @param   ex An exception to be filled out explaining that
    * the resource is locked by this locked id (not other), and
    * any other applicable error messages. May be <CODE>null</CODE>,
    * in which case no reporting will be done.
    * 
    * @return   boolean
    */
   public boolean sameId(IPSLockerId other, PSLockedException ex);
   
   
   /**
    * Should this lock override an existing lock held by a different user?
    * 
    * @return <code>true</code> if this lock should override any existing
    * lock, no matter which user holds it; <code>false</code> otherwise.
    * 
    * @since 4.0
    */ 
   public boolean isOverrideDifferentUser();
}
