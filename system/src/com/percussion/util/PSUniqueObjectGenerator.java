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
package com.percussion.util;

/**
 * Implements a singleton object that stores a session counter. Each time it
 * is called, a different number will be returned. The counter resets for
 * each new session. To guarantee uniqueness within the session, a long
 * lived object must take ownership of a reference to keep the class from
 * getting garbage collected.
 */
public class PSUniqueObjectGenerator
{
   /**
    * Implements the singleton pattern.
    *
    * @return The single instance of this class, creating it if necesasry.
    */
   public static PSUniqueObjectGenerator getInstance()
   {
      if ( null == ms_instance )
         ms_instance = new PSUniqueObjectGenerator();
      return ms_instance;
   }

   /**
    * Creates a unique identifier within this session of the server and
    * appends it to the supplied root. A very simple algorithm of an
    * incrementing counter value converted to a string is used.
    * <p>Note:</p>
    * <p>It is possible to get non-unique names by supplying different roots
    * at the right time. For example, if you first submitted 'rx1' as the root,
    * then, 10 counts later, submitted 'rx' as the root, both would return
    * 'rx11'.
    *
    * @param root The base of the unique name. If <code>null</code> or empty,
    *    "rxname" is used.
    *
    * @return The unique name.
    */
   public static String makeUniqueName( String root )
   {
      if ( null == root || root.trim().length() == 0 )
         root = "rxname";
      int count;
      synchronized ( getInstance().getClass())
      {
         count = ms_counter++;
      }
      return root + count;
   }



   /**
    * A counter used to generate the unique values.
    */
   private static int ms_counter = 1;

   /**
    * The one and only instance of this object. <code>null</code> until
    * <code>getInstance</code> is called the first time. Then immutable.
    */
   private volatile static PSUniqueObjectGenerator ms_instance;

   /**
    * Private to implement the singleton pattern.
    */
   private PSUniqueObjectGenerator()
   {
   }
}
