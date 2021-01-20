/******************************************************************************
 *
 * [ RxInstallerProperties.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

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
