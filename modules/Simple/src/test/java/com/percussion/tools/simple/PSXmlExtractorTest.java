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
package com.percussion.tools.simple;

import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.xml.PSEntityResolver;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the extractor. This currently just tests a particular error case
 * found in 5.5 development, it should be filled out with other tests.
 * 
 * Note that this test must be run with the working directory set to the 
 * root of the development tree. Also note that the referenced xml file
 * should be replaced if necessary.
 */
@Category(IntegrationTest.class)
public class PSXmlExtractorTest
{
   private static final String TEST_EDITOR_DTD = 
      "/com/percussion/tools/simple/sys_ContentEditorLocalDef.dtd";
   private static final String TEST_EDITOR = 
      "/com/percussion/tools/simple/rx_cePage.xml";

   @BeforeClass
   public void setupResolver() throws Exception
   {
     PSEntityResolver res = PSEntityResolver.getInstance();
     res.setResolutionHome(new File(System.getProperty("rxdeploydir") + File.separatorChar + "DTD"));
   }
   
   /**
    * Quick test to check that the create document call can be called
    * without a system or public id
    * @throws Exception
    */
   @Test
   public void testDoc() throws Exception
   {
      PSXmlDocumentBuilder.createXmlDocument("XYZ", null, null);
   }
   
   /**
    * Test extracting a content editor with validation. 
    * @throws Exception
    */
   @Test
   public void testExtractCE() throws Exception
   {
      File source = File.createTempFile("test", "xml");
      source.deleteOnExit();
      FileUtils.copyInputStreamToFile(PSXmlExtractorTest.class.getResourceAsStream(TEST_EDITOR),source);
      
      File target = File.createTempFile("test",".xml");
      target.deleteOnExit();

      URL dtd = new URL("file:///" + System.getProperty("rxdeploydir") + File.separatorChar + "DTD/sys_ContentEditorLocalDef.dtd");
      
      String result = PSXmlExtractor.extract( source, target,
                     CE_ROOT_ELEMENT_NAME, dtd, null, null, true);
      assertNull(result, result);
   }
   
   /**
    * Test extracting a content editor without validation. 
    * @throws Exception
    */
   @Test
   public void testExtractCEnoDTDCheck() throws Exception
   {
      File source = File.createTempFile("test", "xml");
      source.deleteOnExit();
      FileUtils.copyInputStreamToFile(PSXmlExtractorTest.class.getResourceAsStream(TEST_EDITOR),source);

      File target = File.createTempFile("testnodtd",".xml");
      target.deleteOnExit();

      String result = PSXmlExtractor.extract( source, target,
                     CE_ROOT_ELEMENT_NAME, null, null, null, false);
      assertTrue(result == null);
   }

   @Test
   public void testExtraceCE2() throws Exception
   {
      File source = File.createTempFile("test", "xml");
      source.deleteOnExit();
      FileUtils.copyInputStreamToFile(PSXmlExtractorTest.class.getResourceAsStream(TEST_EDITOR),source);

      File target = File.createTempFile("testb",".xml");
      target.deleteOnExit();
      URL dtd = new URL("file:///" + System.getProperty("rxdeploydir") + File.separatorChar + "DTD/sys_ContentEditorLocalDef.dtd");


      String result = PSXmlExtractor.extract(source, target, 
            "PSXContentEditor", dtd, null, null, "a/b/c");
      assertTrue(result == null);        
   }

   /**
    * The tag name of root content editor element.
    */
   private static final String CE_ROOT_ELEMENT_NAME = "PSXContentEditor";


}
