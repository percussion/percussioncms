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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;


/**
 * This functionality is placed in its own interface so that it can be passed
 * to components during the toDbXml processing. This interface requires no
 * configuration, all required data is supplied to the methods.
 * 
 * @author Paul Howard
 * @version 1.0
 */
public interface IPSKeyGenerator
{
   /**
    * Queries the server to obtain a block of unique ids that could be used
    * to pre-assign keys to new <code>IPSDbComponent</code>s matching the
    * supplied component type.
    *
    * @param lookup  The name of the 'key' in the next number table that 
    *    contains the next id to be used for the component.
    *
    * @param count  How many keys to create. Must be > 0. The number of keys
    *    returned may be less than the number requested if the requested
    *    value is too large.
    *
    * @return An array with 0 or more PSKeys, each of which is assigned, but
    *    not persisted.
    *
    * @throws PSCmsException If the processor for the supplied type is not
    *    found or cannot be instantiated, or any problems occur while
    *    generating the keys.
    */
   public int [] allocateIds(String lookup, int count)
      throws PSCmsException;
      

   /**
    * This method is used to cache keys when processing a bunch of 
    * components in sequence. See {@link #createKey(String)} for more details. 
    * By default, the value of this property is 1. It is reset to 1 after the
    * first request is made.
    * 
    * @param count  How many keys to create on the next call to createKey. 
    *    Must be > 0. The number of keys returned may be less than the number 
    *    requested if the requested value is too large.
    */
   public void setNextAllocationSize(int count);
      
      
   /**
    * A simplified version of {@link #createKeys(String,int) createKeys} that
    * is used when a single id is needed. To improve performance, this method
    * can create several keys and cache the rest. This is controlled by the
    * setNextAllocationSize method. The following steps are performed by
    * this method:
    * <ol>
    *    <li>Check the cache for unallocated keys using lookup</li>
    *    <li>If found, take one of those and return it.</li>
    *    <li>If not found, call createKeys, asking for the number in the
    *       nextAllocationSize property. Then set nextAllocationSize to 1.</li>
    *    <li>Cache all keys but 1, return the first.</li>
    * </ol>
    * <p>See {@link #createKeys(String,int)} for further details.
    */
   public int allocateId(String lookup)
      throws PSCmsException;
}
