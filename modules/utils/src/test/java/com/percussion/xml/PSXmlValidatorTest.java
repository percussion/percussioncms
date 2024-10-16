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
package com.percussion.xml;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the xml validator class.
 *
 */
public class PSXmlValidatorTest
{  
   
   /**
    * 
    */
   public PSXmlValidatorTest()
   {
      super();     
   }

   @Test
   @Ignore("TODO: Add namespace to test catalog")
   public void testValidation() throws Exception
   {
      List<Exception> errors = new ArrayList<Exception>();
      assertTrue(PSXmlValidator.validateXmlAgainstSchema(
         m_goodFile, m_xsdFile, errors));
      assertEquals(0, errors.size());
      errors = new ArrayList<Exception>();
      assertFalse(PSXmlValidator.validateXmlAgainstSchema(
              m_badFile, m_xsdFile, errors));
      assertEquals(1, errors.size());
   }

   /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
   @Before
   public void setUp() throws Exception
   {
      if(!DIR.exists())
         DIR.mkdirs();
      m_xsdFile = new File(DIR, "testXsd.xsd");
      m_goodFile = new File(DIR, "good.xml");
      m_badFile = new File(DIR, "bad.xml");
            
      createFile(m_xsdFile, XSD);
      createFile(m_goodFile, StringUtils.replace(
         GOOD_XML, "@@XSD_PATH@@", m_xsdFile.getAbsolutePath()));
      createFile(m_badFile, StringUtils.replace(
         BAD_XML, "@@XSD_PATH@@", m_xsdFile.getAbsolutePath()));
   }
   
   /**
    * Utility method to create a file
    * @param file
    * @param content
    * @throws Exception
    */
   private void createFile(File file, String content)
      throws Exception
   {
      FileWriter fw = null;
      
      try
      {
         if(file.exists())
            file.delete();
         fw = new FileWriter(file);
         fw.write(content);
      }
      finally
      {
         if(fw != null)
            fw.close();
      }
   }

   /* (non-Javadoc)
    * @see junit.framework.TestCase#tearDown()
    */
   @After
   public void tearDown() throws Exception
   {
      m_xsdFile.delete();
      m_goodFile.delete();
      m_badFile.delete();
      DIR.delete();
   }

   private File m_xsdFile;
   private File m_goodFile;
   private File m_badFile;
   
   private static final File DIR = new File("CONFIGTESTTEMP");
   
   private static final String XSD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\">\r\n" + 
                "   <xs:element name=\"bar\" type=\"xs:string\"/>\r\n" + 
                "   <xs:element name=\"foo\">\r\n" + 
                "      <xs:complexType>\r\n" + 
                "         <xs:attribute name=\"name\" type=\"xs:string\" use=\"required\"/>\r\n" + 
                "      </xs:complexType>\r\n" + 
                "   </xs:element>\r\n" + 
                "   <xs:element name=\"testXml\">\r\n" + 
                "      <xs:complexType>\r\n" + 
                "         <xs:sequence>\r\n" + 
                "            <xs:element ref=\"foo\"/>\r\n" + 
                "            <xs:element ref=\"bar\"/>\r\n" + 
                "         </xs:sequence>\r\n" + 
                "      </xs:complexType>\r\n" + 
                "   </xs:element>\r\n" + 
                "</xs:schema>\r\n" + 
                "";
   
   private static final String GOOD_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
                "<testXml xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"@@XSD_PATH@@\">\r\n" + 
                "   <foo name=\"hello\"/>\r\n" + 
                "   <bar>world</bar>\r\n" + 
                "</testXml>\r\n" + 
                "";
   
   private static final String BAD_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
                "<testXml xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"@@XSD_PATH@@\">\r\n" + 
                "   <foo name=\"hello\"/>\r\n" + 
                "   <bar>world</bar>\r\n" +
                "   <dog>bad</dog>\r\n" + 
                "</testXml>\r\n" + 
                "";
   
}
