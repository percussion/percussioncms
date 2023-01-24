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
package com.percussion.webservices.transformation.converter;

import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.server.PSServer;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.PSContentEditorDefinition;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;

import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

/**
 * Unit tests for the {@link PSContentEditorDefinitionConverter} class.
 */
@Category(IntegrationTest.class)
public class PSContentEditorDefinitionConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object as well as a
    * server array of objects to a client array of objects and back.
    * 
    * @throws Exception if an error occurs.
    */
   public void testConversion() throws Exception
   {
      File configDir = new File(PSServer.getRxFile(CFG_DIR));
      File cmsDir = new File(configDir, CMS_DIR);
      File cfgDirShared = new File(cmsDir, CMS_SHARED_DIR);
      
      FileInputStream in = null;
      
      // test system definition
      try
      {
         File def = new File(cmsDir, "ContentEditorSystemDef.xml");
         in = new FileInputStream(def);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         PSContentEditorSystemDef systemSource = 
            new PSContentEditorSystemDef(doc);
         
         PSContentEditorSystemDef systemTarget = 
            (PSContentEditorSystemDef) roundTripConversion(
               PSContentEditorSystemDef.class, 
               PSContentEditorDefinition.class, 
               systemSource);
         
         assertTrue(systemSource.equals(systemTarget));
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }
      
      // test shared definition
      try
      {
         File def = new File(cfgDirShared, "rxs_ct_shared.xml");
         in = new FileInputStream(def);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         PSContentEditorSharedDef sharedSource = 
            new PSContentEditorSharedDef(doc);
         
         PSContentEditorSharedDef sharedTarget = 
            (PSContentEditorSharedDef) roundTripConversion(
               PSContentEditorSharedDef.class, 
               PSContentEditorDefinition.class, 
               sharedSource);
         
         assertTrue(sharedSource.equals(sharedTarget));
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }
   }
   
   /**
    * Constant for the directory containing content management configurations.
    * Assumed to be relative to the Rx root directory.
    */
   private static final String CFG_DIR = "rxconfig";

   /**
    * Constant for the directory containing content editor configurations.
    * Assumed to be relative to the {@link #CFG_DIR} directory.
    */
   private static final String CMS_DIR = "Server/ContentEditors";

   /**
    * Constant for the directory containing content editor shared configs.
    * Assumed to be relative to the {@link #CMS_DIR} directory.
    */
   private static final String CMS_SHARED_DIR = "shared";
}

