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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
/**
 * This class is used to load the module element from the configuration file
 * and give some utility methods.
 */

public class PSUpgradeModule implements IPSUpgradeModule
{
   /**
    * Constructor constructs this class object from the module element of
    * configuration file.
    * @param module element from configuration file.
    * must not be <code>null</code>.
    *
    */
   public PSUpgradeModule(Element module)
      throws FileNotFoundException, IOException, SAXException
   {
      if(module == null || !module.getNodeName().equals(ELEM_MODULE_NAME))
      {
         throw (new IllegalArgumentException(
            "module element must not be null and name must be module."));
      }
      m_module = module;

      String logfile = m_module.getAttribute(ATTR_LOGFILE).trim();
      if(logfile.length() < 1)
      {
         m_ps = System.out;
      }
      else
      {
         String logFileDir;
         NodeList nl = module.getElementsByTagName("preupgrade");
         if (nl != null && nl.getLength() >= 1)
            logFileDir = RxUpgrade.getPreLogFileDir();
         else
            logFileDir = RxUpgrade.getPostLogFileDir();
         
         m_ps = new PrintStream(new FileOutputStream(logFileDir + logfile));
      }
   }

   /**
    * Gives the name of the module.
    */
   public String getModuleName()
   {
      return m_module.getAttribute(ATTR_NAME).trim();
   }

   /**
    * Gives the name of the log file.
    */
   public String getLogFile()
   {
      return m_module.getAttribute(ATTR_LOGFILE).trim();
   }

   /**
    * Gives PrintStream object m_ps.
    */
   public PrintStream getLogStream()
   {
      return m_ps;
   }

   /**
    * Gives an element from module if we give the element name.
    * returns null if element name is empty.
    * @param elemName Name of the element.
    */
   public Element getElement(String elemName)
   {
      return InstallUtil.getElement(m_module, elemName);
   }

   /**
    * Gives the module element.
    */
   public Element getModuleElement()
   {
      return m_module;
   }

   /**
    * Closes the PrintStream object.
    */
   public void close()
   {
      if(m_ps != null)
      {
         try
         {
            m_ps.flush();
            m_ps.close();
         }
         catch(Throwable t)
         {
         }
      }
   }
   
   /**
    * Module element
    */
   private Element m_module = null;
   /**
    * PrintStream object m_ps.
    */
   private PrintStream m_ps = null;
}


