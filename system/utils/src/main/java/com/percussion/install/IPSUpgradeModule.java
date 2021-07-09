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
package com.percussion.install;

import java.io.File;
import java.io.PrintStream;

import org.w3c.dom.Element;

/**
 * This interface defines access methods (per module basis) and string 
 * constants for the elements in upgrade configuration file.
*/
public interface IPSUpgradeModule
{
   /**
    * Get method for modulename
    * @return name of the module from the module element of the configuration file.
    */
   String getModuleName();

   /**
    * Get method for logfile
    * @return name of the logfile from the module element of the configuration file.
    */
   String getLogFile();

   /**
    * Get method for logstream
    * @return logstream.
    */
   PrintStream getLogStream();

   /**
    * Get method for element value for a given element name from module element
    * @param elemName name of the element
    * @return Element object of given element name
    */
   Element getElement(String elemName);

   /**
    * Get method for module element
    * @return Element object of module
    */
   Element getModuleElement();

   /**
    * close method for closing open log files and or log streams.
    */
   void close();
   /*
    * Element names
    */
   static final String ELEM_MODULE_NAME = "module";
   static final String ELEM_TRANSFORMFILES = "transformfiles";
   static final String ELEM_PLUGINS = "plugins";
   static final String ELEM_CLASS = "class";
   static final String ELEM_DATA = "data";
   /*
    * Attribute names
    */
   static final String ATTR_NAME = "name";
   static final String ATTR_LOGFILE = "logfile";
   static final String ATTR_RXROOT = "rxroot";
   /*
    * Constant string for upgrade directory.
    */
   static final String UPGRADE_DIR = "upgrade";
   /*
    * Constant string for repository properties file name with full path.
    */
   static public final String REPOSITORY_PROPFILEPATH =
      "rxconfig" + File.separator + "Installer" + File.separator +
      "rxrepository.properties";
}
