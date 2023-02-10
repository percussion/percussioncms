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

