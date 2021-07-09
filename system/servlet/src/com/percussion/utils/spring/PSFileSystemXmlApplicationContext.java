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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.utils.spring;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.springframework.beans.BeansException;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * A version of the spring application context that overrides the classloader
 * method to allow us to load hibernate.cfg.xml from a specific location.
 * 
 * @author dougrand
 */
public class PSFileSystemXmlApplicationContext
      extends
         FileSystemXmlApplicationContext
{
   /**
    * Where to find hibernate.cfg.xml so we can place it on the class loader
    */
   private static File ms_hibernateConfigDir = null;
   
   /**
    * Our calculated class loader to find the hibernate configuration
    */
   private volatile static ClassLoader ms_cl = null;
   
   /**
    * @see FileSystemXmlApplicationContext
    */
   public PSFileSystemXmlApplicationContext(String[] configFiles) throws BeansException {
      super(configFiles);
   }

   /**
    * Set the configuration dir to use
    * @param dir the dir, never <code>null</code>
    */
   public static void setConfigDir(File dir)
   {
      if (dir == null)
      {
         throw new IllegalArgumentException("dir may not be null");
      }
      ms_hibernateConfigDir = dir;
   }
   
   /* (non-Javadoc)
    * @see org.springframework.core.io.DefaultResourceLoader#getClassLoader()
    */
   @Override
   public ClassLoader getClassLoader()
   {
      if(ms_hibernateConfigDir == null)
         return super.getClassLoader();
      
      if (ms_cl == null)
      {
         synchronized(ms_hibernateConfigDir)
         {
            URL urls[] = new URL[1];
            try
            {
               urls[0] = ms_hibernateConfigDir.toURI().toURL();
            }
            catch (MalformedURLException e)
            {
               throw new RuntimeException("Fatal error finding class loader", e);
            }
            ms_cl = new URLClassLoader(urls, super.getClassLoader());
         }
      }
      return ms_cl;
   }

   
   
}
