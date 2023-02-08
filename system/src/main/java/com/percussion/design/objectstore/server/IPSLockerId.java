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
