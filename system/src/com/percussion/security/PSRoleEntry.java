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
 * The PSRoleEntry class defines the implementation of a role entry
 * within E2.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSRoleEntry extends PSEntry 
{

   /**
    * Construct a role entry object for the named entry.
    *
    * @param   name               the name of the role
    *
    * @param   accessLevel         the access level to assign this entry
    */
   public PSRoleEntry(String name, int accessLevel)
   {
      super(name, accessLevel);
   }

   /* ********************** PSEntry Implementation ********************** */

   /**
    * Does the specified entry match this one? The entry must be of the
    * same provider type. If one of the entries is a filter, it will use
    * the information defined in the other entry to test for equality.
    *   For PSRoleEntry, this method will always return false.
    *
    *   @param      entry         the entry to check
    *
    * @return                  <code>false</code> is always returned for the
    *                           PSRoleEntry class
    */
   public boolean isMatch(PSEntry entry)
   {
      return false;
   }

   /**
    * Is this class a filter? Filters can be used to perform checks against
    * other entries based upon attributes, etc.
    *   For PSRoleEntry, this method will always return false.
    *
    * @return                  <code>false</code> is always returned for the
    *                           PSRoleEntry class
    */
   public boolean isFilter()
   {
      return false;
   }
}

