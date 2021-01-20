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
package com.percussion.webdav.test;

import com.percussion.webdav.error.IPSWebdavErrors;
import com.percussion.webdav.error.PSWebdavException;
import com.percussion.webdav.objectstore.IPSRxWebDavDTD;
import com.percussion.webdav.objectstore.PSWebdavConfigDef;
import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.util.Iterator;
/**
 * Unit test class for the <code>PSWebdavConfig</code> class.
 */
public class PSWebdavConfigTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
   public PSWebdavConfigTest(String name)
   {
      super(name);
   }
   /**
    * Test loading a good configuration
    * @throws Exception for any error
    */
   public void testGoodConfig() throws Exception
   {
      PSWebdavConfigDef config =
         new PSWebdavConfigDef(loadXmlResource(CONFIG_GOOD));
      assertEquals("default", config.getCommunityName());
      assertEquals("/Site/mysite", config.getRootPath());
      Iterator it = config.getContentTypes();
      int typesCount = 0;
      while (it.hasNext())
      {
         typesCount++;
         it.next();
      }
      assertEquals(2, typesCount);
      assertEquals("image", config.getDefaultContentType().getName());
      // test to/from XML
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element targetEl = config.toXml(doc);
      PSWebdavConfigDef target = new PSWebdavConfigDef(targetEl);
      assertTrue(config.equals(target));
   }
   /**
    * Test loading a configuration missing a required community
    * attribute.
    * @throws Exception for any error
    */
   public void testMissingCommunity() throws Exception
   {
      String errorMsg = "";
      try
      {
         new PSWebdavConfigDef(loadXmlResource(CONFIG_MISSING_COMM));
      }
      catch (PSWebdavException e)
      {
         errorMsg = e.getMessage();
      }
      assertEquals(
         getAttributeErrorMsg(
            IPSRxWebDavDTD.ATTR_COMMUNITY_NAME,
            IPSRxWebDavDTD.ELEM_CONFIG),
         errorMsg);
   }
   /**
    * Test loading a configuration missing a required contentfield
    * attribute.
    * @throws Exception for any error
    */
   public void testMissingContentField() throws Exception
   {
      String errorMsg = "";
      try
      {
         new PSWebdavConfigDef(loadXmlResource(CONFIG_MISSING_CONTENTFIELD));
      }
      catch (PSWebdavException e)
      {
         errorMsg = e.getMessage();
      }
      assertEquals(
         getAttributeErrorMsg(
            IPSRxWebDavDTD.ATTR_CONTENTFIELD,
            IPSRxWebDavDTD.ELEM_CONTENTTYPE),
         errorMsg);
   }
   /**
    * Test loading a configuration missing a required property name
    * attribute.
    * @throws Exception for any error
    */
   public void testMissingPropName() throws Exception
   {
      String errorMsg = "";
      try
      {
         new PSWebdavConfigDef(loadXmlResource(CONFIG_MISSING_PROPNAME));
      }
      catch (PSWebdavException e)
      {
         errorMsg = e.getMessage();
      }
      assertEquals(
         getAttributeErrorMsg(
            IPSRxWebDavDTD.ATTR_NAME,
            IPSRxWebDavDTD.ELEM_PROPERTYFIELD_MAPPING),
         errorMsg);
   }
   /**
    * Test loading a configuration missing a required root
    * attribute.
    * @throws Exception for any error
    */
   public void testMissingRoot() throws Exception
   {
      String errorMsg = "";
      try
      {
         new PSWebdavConfigDef(loadXmlResource(CONFIG_MISSING_ROOT));
      }
      catch (PSWebdavException e)
      {
         errorMsg = e.getMessage();
      }
      assertEquals(
         getAttributeErrorMsg(
            IPSRxWebDavDTD.ATTR_ROOT,
            IPSRxWebDavDTD.ELEM_CONFIG),
         errorMsg);
   }
   /**
    * Test loading a configuration missing a required content type id
    * attribute.
    * @throws Exception for any error
    */
   public void testMissingTypeID() throws Exception
   {
      String errorMsg = "";
      try
      {
         new PSWebdavConfigDef(loadXmlResource(CONFIG_MISSING_TYPEID));
      }
      catch (PSWebdavException e)
      {
         errorMsg = e.getMessage();
      }
      assertEquals(
         getAttributeErrorMsg(
            IPSRxWebDavDTD.ATTR_ID,
            IPSRxWebDavDTD.ELEM_CONTENTTYPE),
         errorMsg);
   }
   /**
    * Test loading a configuration missing a required content type name
    * attribute.
    * @throws Exception for any error
    */
   public void testMissingTypeName() throws Exception
   {
      String errorMsg = "";
      try
      {
         new PSWebdavConfigDef(loadXmlResource(CONFIG_MISSING_TYPENAME));
      }
      catch (PSWebdavException e)
      {
         errorMsg = e.getMessage();
      }
      assertEquals(
         getAttributeErrorMsg(
            IPSRxWebDavDTD.ATTR_NAME,
            IPSRxWebDavDTD.ELEM_CONTENTTYPE),
         errorMsg);
   }
   /**
    * Test loading a configuration missing a required field name element data
    * @throws Exception for any error
    */
   public void testMissingFieldName() throws Exception
   {
      PSWebdavException expectedEx = null;
      try
      {
         new PSWebdavConfigDef(loadXmlResource(CONFIG_MISSING_FIELDNAME));
      }
      catch (PSWebdavException e)
      {
         expectedEx = e;
      }
      assertEquals(
         IPSWebdavErrors.FIELDNAME_CANNOT_BE_EMPTY_OR_MISSING,
         expectedEx.getErrorCode());
   }
   
   /**
    * Test loading a configuration missing required properties
    * @throws Exception for any error
    */
   public void testMissingProps() throws Exception
   {
      PSWebdavException expectedEx = null;
      try
      {
         new PSWebdavConfigDef(loadXmlResource(CONFIG_MISSING_PROPS));
      }
      catch (PSWebdavException e)
      {
         expectedEx = e;
      }
      assertEquals(
         IPSWebdavErrors.MISSING_REQUIRED_PROPERTY,
         expectedEx.getErrorCode());
   }
   
   /**
    * Test loading a configuration with duplicate content types
    * @throws Exception for any error
    */
   public void testDuplicateTypes() throws Exception
   {
      PSWebdavException expectedEx = null;
      try
      {
         new PSWebdavConfigDef(loadXmlResource(CONFIG_DUP_TYPES));
      }
      catch (PSWebdavException e)
      {
         expectedEx = e;
      }
      assertEquals(
         IPSWebdavErrors.DUPLICATE_CONTENTTYPE_NAMES,
         expectedEx.getErrorCode());
   }
   
   /**
    * Test loading a configuration missing required properties
    * @throws Exception for any error
    */
   public void testDuplicateProps() throws Exception
   {
      PSWebdavException expectedEx = null;
      try
      {
         new PSWebdavConfigDef(loadXmlResource(CONFIG_DUP_PROPS));
      }
      catch (PSWebdavException e)
      {
         expectedEx = e;
      }
      assertEquals(
         IPSWebdavErrors.CANNOT_HAVE_DUPLICATE_PROPERTIES,
         expectedEx.getErrorCode());
   }
   
   /**
    * Test loading a configuration missing mimetypes if default is false
    * @throws Exception for any error
    */
   public void testMissingMimetypes() throws Exception
   {
      PSWebdavException expectedEx = null;
      try
      {
         new PSWebdavConfigDef(loadXmlResource(CONFIG_MISSING_MIMES));
      }
      catch (PSWebdavException e)
      {
         expectedEx = e;
      }
      assertEquals(
         IPSWebdavErrors.MIMETYPES_REQUIRED,
         expectedEx.getErrorCode());
   }
   
   /**
    * Test loading a configuration that has more then one default
    * content type.
    * @throws Exception for any error
    */
   public void testMoreThenOneDefaultCT() throws Exception
   {
      PSWebdavException expectedEx = null;
      try
      {
         new PSWebdavConfigDef(loadXmlResource(CONFIG_DEFAULT));
      }
      catch (PSWebdavException e)
      {
         expectedEx = e;
      }
      assertEquals(
         IPSWebdavErrors.CAN_ONLY_HAVE_ONE_DEFAULT_CONTENTTYPE,
         expectedEx.getErrorCode());
   }
   
   /**
    * Loads the xml resource into an xml document and returns the
    * root element.
    * @param name name of the resource,. Cannot be <code>null</code>.
    * @return root element of the xml document
    * @throws Exception on any error
    */
   private Element loadXmlResource(String name) throws Exception
   {
      InputStream in = getClass().getResourceAsStream("/com/percussion/webdav/test/" + name);
      Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      return doc.getDocumentElement();
   }
   
   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSWebdavConfigTest("testGoodConfig"));
      suite.addTest(new PSWebdavConfigTest("testMissingCommunity"));
      suite.addTest(new PSWebdavConfigTest("testMissingContentField"));
      suite.addTest(new PSWebdavConfigTest("testMissingPropName"));
      suite.addTest(new PSWebdavConfigTest("testMissingRoot"));
      suite.addTest(new PSWebdavConfigTest("testMissingTypeID"));
      suite.addTest(new PSWebdavConfigTest("testMissingTypeName"));
      suite.addTest(new PSWebdavConfigTest("testMissingFieldName"));
      suite.addTest(new PSWebdavConfigTest("testMissingProps"));
      suite.addTest(new PSWebdavConfigTest("testDuplicateTypes"));
      suite.addTest(new PSWebdavConfigTest("testDuplicateProps"));
      suite.addTest(new PSWebdavConfigTest("testMissingMimetypes"));
      suite.addTest(new PSWebdavConfigTest("testMoreThenOneDefaultCT"));
      return suite;
   }
   
   private String getAttributeErrorMsg(String attr, String elem)
   {
      return "Attribute '"
         + attr
         + "' must be specified for element '"
         + elem
         + "'.";
   }
   
   // Various configuration files for testing
   private final static String CONFIG_GOOD = "WebDavConfig_Good.xml";
   private final static String CONFIG_DUP_PROPS = "WebDavConfig_dupProps.xml";
   private final static String CONFIG_DUP_TYPES = "WebDavConfig_dupTypes.xml";
   private final static String CONFIG_MISSING_COMM =
      "WebDavConfig_missingCommunity.xml";
   private final static String CONFIG_MISSING_CONTENTFIELD =
      "WebDavConfig_missingContentField.xml";
   private final static String CONFIG_MISSING_FIELDNAME =
      "WebDavConfig_missingFieldName.xml";
   private final static String CONFIG_MISSING_MIMES =
      "WebDavConfig_missingMimes.xml";
   private final static String CONFIG_MISSING_PROPNAME =
      "WebDavConfig_missingPropName.xml";
   private final static String CONFIG_MISSING_PROPS =
      "WebDavConfig_missingProps.xml";
   private final static String CONFIG_MISSING_ROOT =
      "WebDavConfig_missingRoot.xml";
   private final static String CONFIG_MISSING_TYPEID =
      "WebDavConfig_missingTypeID.xml";
   private final static String CONFIG_MISSING_TYPENAME =
      "WebDavConfig_missingTypeName.xml";
   private final static String CONFIG_DEFAULT = "WebDavConfig_Defaults.xml";
}