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
package com.percussion.server;

import org.apache.commons.lang.Validate;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author JaySeletz
 *
 */
public class PSSystemControlManager extends PSBaseControlManager
{
   
   /**
    * The subsystem name.
    */
   private static final String SUBSYSTEM = "SystemControlMgr";
   private static PSSystemControlManager ms_sysCtrlMgr;
   
   private File m_resourceRoot;

   /**
    * private ctor, use singleton 
    * 
    * @param resourceRoot The root from which to load the system control file (e.g. sys_resources), not <code>null</code> and must exist. 
    */
   private PSSystemControlManager(File resourceRoot)
   {
      Validate.notNull(resourceRoot);
      if (!resourceRoot.exists())
         throw new IllegalArgumentException(resourceRoot + " does not exist");
      
      m_resourceRoot = resourceRoot;
   }

   /**
    * Gets the singleton instance of this class.  
    * 
    * @return The manager, never <code>null</code>.
    */
   public static PSSystemControlManager getInstance()
   {
      if (ms_sysCtrlMgr == null)
      {
         throw new IllegalStateException("createInstance() must be called first");
      }
      
      return ms_sysCtrlMgr;
   }
   
   public static PSSystemControlManager createInstance(File rxRoot)
   {
      if (ms_sysCtrlMgr != null)
         throw new IllegalStateException("Instance already initialized");
      
      ms_sysCtrlMgr = new PSSystemControlManager(rxRoot);
      
      return ms_sysCtrlMgr;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.server.PSBaseControlManager#getSubSystem()
    */
   @Override
   protected String getSubSystem()
   {
      return SUBSYSTEM;
   }

   /* (non-Javadoc)
    * @see com.percussion.server.PSBaseControlManager#getControlFiles()
    */
   @Override
   public List<File> getControlFiles()
   {
      return Arrays.asList(new File(m_resourceRoot, "stylesheets/sys_Templates.xsl"));
   }

}
