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
