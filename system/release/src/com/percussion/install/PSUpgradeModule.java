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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
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


