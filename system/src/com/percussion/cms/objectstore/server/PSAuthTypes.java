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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.IPSConstants;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.util.PSObservableFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This singleton class wraps the authtype configuration read off of
 * {@link com.percussion.cms.IPSConstants#AUTHTYPE_PROP_FILE}. For each entry
 * in this Java properties file, the key is assumed to have the format
 * 'authtype.N', where N is the numeric authtype identifier as registered w/ the
 * system and the value is the rhythmyx resource name (appName/resourceName,
 * case sensitive)implementing this authtype.
 */
public class PSAuthTypes implements Observer
{
   /**
    * Make ctor private since this is a singleton class.
    */
   private PSAuthTypes()
   {
      try
      {
         loadProperties(ms_configFile);
      }
      catch (IOException e)
      {
         handleException(e);
      }
      ms_configFile.addObserver(this);
}

   /**
    * Get a singleton instance of this class.
    * 
    * @return only object of the class, never <code>null</code>.
    */
   static synchronized public PSAuthTypes getInstance()
   {
      if (ms_this == null)
         ms_this = new PSAuthTypes();
      return ms_this;
   }

   /**
    * Get the Rhythmyx resource name implementing the supplied authtype.
    * 
    * @param authType Auth type value as string, must not be <code>null</code>
    *           or empty.
    * @return Rhythmyx resource implementing the authtype as configured in the
    *         {@link IPSConstants#AUTHTYPE_PROP_FILE} file. Will be
    *         <code>null</code> if the supplied authtype is not configured.
    */
   public String getResourceForAuthtype(String authType)
   {
      if (authType == null || authType.length() == 0)
      {
         throw new IllegalArgumentException(
               "authType must not be null or empty");
      }
      return m_authTypeMap.get(authType);
   }
   
   /**
    * Returns all registered types.
    * @return Never <code>null</code>, may be empty. The returned set is 
    * unmodifiable.
    */
   public Set<String> getAuthtypes()
   {
      if (m_authTypeMap.isEmpty())
         return Collections.emptySet();
      return Collections.unmodifiableSet(m_authTypeMap.keySet());
   }
   
   /**
    * Get the file object for the authtype configuration file
    * {@link IPSConstants#AUTHTYPE_PROP_FILE}.
    * 
    * @return as described above, never <code>null</code>.
    */
   public File getConfigFile()
   {
      return ms_configFile.getFile();
   }

   /**
    * Load the properties from the config file and build the authtype-resource
    * name map.
    * 
    * @param configFile the observable file from which the properties are
    *           loaded, assumed not <code>null</code>
    * @throws IOException if loading the file fails.
    */
   @SuppressWarnings("unchecked") // for iterator of prop keyset
   synchronized private void loadProperties(PSObservableFile configFile)
         throws IOException
   {
      m_authTypeMap.clear();
      InputStream is = null;
      try
      {
         is = new FileInputStream(configFile.getFile());
         Properties props = new Properties();
         props.load(is);
         Iterator iter = props.keySet().iterator();
         final String AUTHTYPE_PREFIX = "authtype.";
         while (iter.hasNext())
         {
            String name = (String) iter.next();
            String value = props.getProperty(name);
            if (value != null && value.length() > 0)
            {
               String key = null;
               if (name.length() > AUTHTYPE_PREFIX.length())
                  key = name.substring(AUTHTYPE_PREFIX.length());
               if (key != null)
                  m_authTypeMap.put(key, value);
            }
         }
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (Exception e)
            {
            }
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
    */
   public void update(@SuppressWarnings("unused") Observable o, 
         @SuppressWarnings("unused") Object arg)
   {
      try
      {
         loadProperties(ms_configFile);
      }
      catch (IOException e)
      {
         handleException(e);
      }
   }

   /**
    * Helper method to handle exception during loading of config file. Prints a
    * detailed message to log file/server console.
    * 
    * @param e Exception thrown by the system while trying to load the config
    *           file, assumed not <code>null</code>
    */
   private void handleException(IOException e)
   {
      m_log.error("Error loading "
            + ms_configFile.getFile().getPath()
            + " file. Actual message is given below. "
            + "Assembly does not work without these properties");
      m_log.error(e.getLocalizedMessage());
   }

   /**
    * Reference to instance of this class. Created only once in
    * {@link #getInstance()} method. Never <code>null</code> after that.
    */
   static private PSAuthTypes ms_this = null;

   /**
    * The observable config file from which the properties are loaded initially
    * and whenever the file is modified. Never <code>null</code> and never
    * modified.
    */
   private static final PSObservableFile ms_configFile = new PSObservableFile(
         (String) PSRhythmyxInfoLocator.getRhythmyxInfo().getProperty(
               IPSRhythmyxInfo.Key.ROOT_DIRECTORY)
               + File.separator + IPSConstants.AUTHTYPE_PROP_FILE);

   /**
    * Map of all authtype-resource names implementing the authtypes. The key in
    * the map is the value of the authtype (String) and the value is the name of
    * the resource in the syntax of <RxApp>/<resource>. Never <code>null</code>
    * and rebuilt everytime the config file is modified.
    */
   private Map<String,String> m_authTypeMap = new HashMap<String,String>();

   /**
    * Logger to write the error log.
    */
   private Logger m_log = Logger.getLogger(getClass());
}
