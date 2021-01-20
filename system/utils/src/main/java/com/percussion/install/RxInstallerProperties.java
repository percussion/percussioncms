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

//java
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
   * RxInstallerProperties is used to manage the resource bundle
  */
public class RxInstallerProperties
{
  /**
   * Constructs an RxInstallerProperties
   */
  public RxInstallerProperties()
  {
  }

  /**
   * Get string resource given a key. If resourse or a key
   * is not available it prints a message on a console and
   * returns a key itself.
   * @param key string key, never <code>null</code> or <code>empty</code>.
   * @return resource string, never <code>null</code>. 
   */
  static public String getString(String key)
  {
     if (key == null || key.trim().length() < 1)
        throw new IllegalArgumentException("key may not be null or empty");
      
     try
     {
        return getResources().getString(key);
     }
     catch(Throwable th)
     {
        th.printStackTrace();
        
        System.out.println("RxInstallerProperties: key missing: " + key);
        
        return key;
     }
  }
    
  /**
    * Get the resource 
    */
  static public ResourceBundle getResources()
  {
      try 
      {
         if ( null == m_res )
         {
            String bundleName = "com.percussion.install.RxInstaller";
            m_res = ResourceBundle.getBundle(bundleName, Locale.getDefault());
         }
      }
      catch(MissingResourceException mre)
      {
         mre.printStackTrace();
      }
      
      return m_res;
  }
  
   private static ResourceBundle m_res = null;
}
