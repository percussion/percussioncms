/*[ ResourceBundleHelper.java ]************************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
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
