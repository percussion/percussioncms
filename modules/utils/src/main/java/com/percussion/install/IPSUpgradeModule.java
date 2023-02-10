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

import java.io.File;
import java.io.PrintStream;

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
