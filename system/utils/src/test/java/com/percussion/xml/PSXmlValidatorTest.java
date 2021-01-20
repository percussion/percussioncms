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
package com.percussion.xml;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the xml validator class.
 *
 */
public class PSXmlValidatorTest extends TestCase
{  
   
   /**
    * 
    */
   public PSXmlValidatorTest()
   {
      super();     
   }

   /**
    * @param name
    */
   public PSXmlValidatorTest(String name)
   {
      super(name);      
   }
   
   public void testValidation() throws Exception
   {
      List<Exception> errors = new ArrayList<Exception>();
      assertTrue(PSXmlValidator.validateXmlAgainstSchema(
         m_goodFile, m_xsdFile, errors));
      assertTrue(errors.size() == 0);
      errors = new ArrayList<Exception>();
      assertTrue(!PSXmlValidator.validateXmlAgainstSchema(
         m_badFile, m_xsdFile, errors));
      assertTrue(errors.size() == 1);
   }

   /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
   @Override
   protected void setUp() throws Exception
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
   @Override
   protected void tearDown() throws Exception
   {
      m_xsdFile.delete();
      m_goodFile.delete();
      m_badFile.delete();
      DIR.delete();
   }
   
// collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSXmlValidatorTest("testValidation"));      
      return suite;
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
