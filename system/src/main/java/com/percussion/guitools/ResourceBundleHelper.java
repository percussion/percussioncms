/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
package com.percussion.guitools;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author DougRand
 *
 * Helper to access resource bundles from jar files. This file is simply 
 * a compilation of what is done elsewhere with some minor refactoring.
 * The resources for a given class are expected to exist in a file called
 * <i>classname</i>Resources.properties in the same place as the .class
 * file. This means that the properties file will normally be included in 
 * the same jar file as the class that is using it.
 */
public class ResourceBundleHelper
{
   /**
    * Bundle value should never be <code>null</code> after the 
    * constructor is run.
    */
   private ResourceBundle m_res = null;

   /**
    * Constructor that extracts the initial bundle using the given pathname
    * @param classname must not be <code>null</code>. This method does not 
    * validate that the passed classname is, in fact, a valid class. 
    */
   public ResourceBundleHelper(String classname)
   {
      if (classname == null || classname.trim().length() == 0)
      {
         throw 
            new IllegalArgumentException("Bundle classname must not be null or empty");
      }

      m_res =
         ResourceBundle.getBundle(classname + "Resources", Locale.getDefault());
   }

   /**
    * Return a resource given the passed name
    * @param name of the resource to be retrieved, may not be 
    * <code>null</code> or empty.
    * @return resource value as a string, which may be <code>null</code> or
    * empty if unspecified.
    */
   protected String getResource(String name)
   {
      if (name == null || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      
      return m_res.getString(name);
   }

}
