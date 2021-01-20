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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *   Utility class to read in an entire resource bundle file into a hashtable
 *   that uses integer keys. Every key in the resource bundle must be
 *   representable as an integer.
 *
 *   This class can be specialized to store any object which can be represented
 *   as a string. You would do this by overriding the putHash() method:
 *   <PRE>
 *      // map from int to URL instead of String
 *      class URLHashFromBundle extends PSHashTableFromBundle
 *      {
 *         public URLHashFromBundle(String bundleBaseName, Locale loc)
 *         {
 *            super(bundleBaseName, loc);
 *         }
 *
 *         // override
 *         protected void putHash(int key, String value)
 *         {
 *            try
 *            {
 *               put(new Integer(key), new URL(value));
 *            }
 *            catch (java.net.MalformedURLException e) {}
 *         }
 *      }
 *   </PRE>
 */
public class PSHashTableFromBundle extends Hashtable
{

   /**
    *   Convenience method that calls constructor with the default locale.
    *   @param   bundleBaseName   The name of the bundle
    *
    *   @throw   MissingResourceException   if a bundle cannot be found for
    *   the default locale
    *
    *   @throw   NumberFormatException   if any of the keys cannot be
    *   represented as an integer
    */
   public PSHashTableFromBundle(String bundleBaseName)
      throws MissingResourceException, NumberFormatException
   {
      this(bundleBaseName, Locale.getDefault());
   }

   /**
    *   Get a new hashtable representation of a resource bundle whose
    *   keys can all be represented as numeric types, and whose values
    *   can all be represented as Strings.
    *
    *   @param   bundleBaseName   The name of the bundle
    *
    *   @param   loc   The locale to use
    *
    *   @return   A hashtable containing all of the values from the resource
    *   bundle.
    *
    *   @throw   MissingResourceException   if a bundle cannot be found for
    *   the given locale
    *
    *   @throw   NumberFormatException   if any of the keys cannot be
    *   represented as an integer
    */
   public PSHashTableFromBundle(String bundleBaseName, Locale loc)
      throws MissingResourceException, NumberFormatException
   {
      ResourceBundle bun = ResourceBundle.getBundle(
         bundleBaseName, loc);

      String key = null;
      int i = 0;
      for (Enumeration e = bun.getKeys(); e.hasMoreElements() ;)
      {
         key = (String)e.nextElement();
         i = Integer.parseInt(key);
         putHash(i, bun.getString(key));
      }
   }

   /**
    *   Puts the specified value into the hashtable using the given key.
    *   Meant to be overridden by subclasses so they can put values
    *   of a different type in the table, as long as those values can
    *   be somehow built using a string.
    */
   protected void putHash(int key, String value)
   {
      put(new Integer(key), value);
   }
}
