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
package com.percussion.tools.simple;

import java.io.File;
import java.net.URL;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;

import com.percussion.utils.xml.PSEntityResolver;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
