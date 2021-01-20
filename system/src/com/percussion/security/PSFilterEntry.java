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

