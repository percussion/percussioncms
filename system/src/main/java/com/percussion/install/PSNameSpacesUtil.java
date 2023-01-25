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
      final StringBuilder buf = new StringBuilder();
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
