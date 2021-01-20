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
package com.percussion.extension;

import java.io.File;
import java.util.Iterator;

import junit.framework.TestCase;

/**
 * Unit test for class <code>PSExtensionHandlerConfiguration</code>.
 */
public class PSExtensionHandlerConfigurationTest extends TestCase
{
   /**
    * Loads the extenstion file and stores it to the users temp directory.
    * The stored file is then reloaded and compared to the original.
    * 
    * @throws Exception for any error.
    */
   @SuppressWarnings("unchecked")
   public void testConfiguration() throws Exception
   {
      // load the extensions configuration
      PSExtensionHandlerConfiguration config =
         new PSExtensionHandlerConfiguration(ms_extensions, null);

      // store it to a temp file, include the methods
      File tempExtensions = File.createTempFile("tempExtensions", ".xml");
      tempExtensions.deleteOnExit();
      config.store(tempExtensions, false);
      
      // reload the temp file
      PSExtensionHandlerConfiguration tempConfig =
         new PSExtensionHandlerConfiguration(tempExtensions, null);
      
      Iterator<PSExtensionRef> refs = config.getExtensionNames();
      while (refs.hasNext())
      {
         PSExtensionRef ref = refs.next();
         
         IPSExtensionDef tempDef = tempConfig.getExtensionDef(ref);
         assertTrue(tempDef != null);
      }
   }
   
   /**
    * The extensions file to use for testing.
    */
   private static final File ms_extensions = 
      new File("Exits/Java/Extensions.xml");
}

