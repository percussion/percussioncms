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
 * The PSFilterEntry class defines the implementation of a filter entry
 * within E2. Filters are used to define security provider specific
 * user or group matching strings. For instance, an LDAP compliant filter
 * string to check the user's organizational unit (ou) is:
 * <pre><code>
 *      (ou=Engineering)
 * </code></pre>
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public abstract class PSFilterEntry extends PSEntry {

   /**
    * Construct an entry object for the named entry.
    *
    * @param   name               the name of the entry
    *
    * @param   accessLevel         the access level to assign this entry
    */
   protected PSFilterEntry(String name, int accessLevel)
   {
      super(name, accessLevel);
   }

   /**
    * Does the specified entry pass the filter condition?
    *
    * @param      entry         the entry to check
    *
    * @return                  <code>true</code> if it does;
    *                           <code>false</code> otherwise
    */
   public abstract boolean passesFilter(PSEntry entry);


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
      if (entry.isFilter()) {
         /* if they're both filters, then they must be an exact match
          * to return true
          */
         return this.getName().equals(entry.getName());
      }

      // otherwise, we must run the filter against the entry
      return passesFilter(entry);   // call the sub-class' implementation
   }

   /**
    * Is this class a filter? Filters can be used to perform checks against
    * other entries based upon attributes, etc.
    *
    * @return                  <code>true</code> is always returned for the
    *                           PSFilterEntry class
    */
   public boolean isFilter()
   {
      return true;
   }
}

