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

