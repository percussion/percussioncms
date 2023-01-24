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

package com.percussion.security;


/**
 * The PSGroupEntry class defines the implementation of a group entry
 * within E2.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSGroupEntry extends PSEntry {

   /**
    * Construct an group entry object for the named entry.
    *
    * @param   name               the name of the entry
    *
    * @param   accessLevel         the access level to assign this entry
    */
   public PSGroupEntry(String name, int accessLevel)
   {
      super(name, accessLevel);
   }


   /* ********************** PSEntry Implementation ********************** */

   /**
    * Does the specified entry match this one? The entry must be of the
    * same provider type. If one of the entries is a filter, it will use
    * the information defined in the other entry to test for equality.
    *
    *   @param      entry         the entry to check
    *
    * @return                  <code>true</code> if the entry matches;
    *                           <code>false</code> otherwise
    */
   public boolean isMatch(PSEntry entry)
   {
      /* we can only check our own type, or if it's a filter, ask it
       * to perform the check.
       */
      if (this.getClass().isInstance(entry))
         return super.isMatch(entry);   // do the default comparison
      else if (entry.isFilter())
         return entry.isMatch(this);   // let the filter do the check

      return false;
   }

   /**
    * Is this class a filter? Filters can be used to perform checks against
    * other entries based upon attributes, etc.
    *
    * @return                  <code>false</code> is always returned for the
    *                           PSGroupEntry class
    */
   public boolean isFilter()
   {
      return false;
   }
}

