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

//java
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
   * RxInstallerProperties is used to manage the resource bundle
  */
public class RxInstallerProperties
{

    private static final Logger log = LogManager.getLogger(RxInstallerProperties.class);

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
         log.error(th.getMessage());
         log.debug(th.getMessage(), th);
        
        log.info("RxInstallerProperties: key missing: " + key);
        
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
          log.error(mre.getMessage());
          log.debug(mre.getMessage(), mre);
      }
      
      return m_res;
  }
  
   private static ResourceBundle m_res = null;
}
