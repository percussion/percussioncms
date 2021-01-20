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
package com.percussion.install;

import java.util.Set;

/**
 * Helps to deal with witespaces in names.
 *
 * @author Andriy Palamarchuk
 */
public class PSNameSpacesUtil
{
   /**
    * Constructor prohibiting creation of instance of the class.
    */
   private PSNameSpacesUtil() {}

   /**
    * Replaces whitespace characters with underscores.
    * Make sure new name does not clash with any existing names. 
    * If generated name already exists the method adds "1" to the name, if
    * that name exists, it adds "2" and so on until it finds unused name.
    * @param name the name to correct. Never <code>null</code>.
    * @param names all existing names to check for clash against.
    * Never <code>null</code>.
    * @return the corrected name. If this name is applied caller
    * should add it to the names set.
    */
   public static String removeWhitespacesFromName(final String name, Set names)
   {
      final StringBuffer buf = new StringBuffer();
      for (int i = 0; i < name.length(); i++)
      {
         final char ch = name.charAt(i);
         buf.append(Character.isWhitespace(ch) ? '_' : ch);
      }
      final String nameBase = buf.toString();
      String newName = nameBase;
      int i = 1;
      while (names.contains(newName))
      {
         newName = nameBase + i;
         i++;
      }
      return newName;
   }
}
