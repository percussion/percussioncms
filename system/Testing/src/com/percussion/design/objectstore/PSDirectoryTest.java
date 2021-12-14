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
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;

/**
 * Directory object store class testing, including constructors,
 * <code>PSComponent</code> functionality, accessors and XML functionality.
 */
public class PSDirectoryTest extends TestCase
{
   /**
    * Constructor to call base class constructor.
    *
    * @see TestCase#TestCase(String) for more information.
    */
   public PSDirectoryTest(String name)
   {
      super(name);
   }

   // See base class
   public void setUp()
   {
   }

   /**
    * Test component constructors and accessors.
    *
    * @throws Exception if any exceptions occur or assertions fail.
    */
   public void testComponent() throws Exception
   {
      PSDirectory dir1 = new PSDirectory("dir1", PSDirectory.CATALOG_SHALLOW,
         "factory", "auth", PROVIDER_URL, ms_attributes);

      PSDirectory dir2 = new PSDirectory("dir2", PSDirectory.CATALOG_SHALLOW,
         "factory", "auth", PROVIDER_URL, ms_attributes);

      PSDirectory dir2_1 = new PSDirectory("dir2", PSDirectory.CATALOG_DEEP,
         "factory", "auth", PROVIDER_URL, ms_attributes);

      PSDirectory dir2_2 = new PSDirectory("dir2", PSDirectory.CATALOG_SHALLOW,
         "factory1", "auth", PROVIDER_URL, ms_attributes);

      PSDirectory dir2_3 = new PSDirectory("dir2", PSDirectory.CATALOG_SHALLOW,
         "factory", "auth1", PROVIDER_URL, ms_attributes);

      PSDirectory dir2_4 = new PSDirectory("dir2", PSDirectory.CATALOG_SHALLOW,
         "factory", "auth", PROVIDER_URL + "1", ms_attributes);

      PSDirectory dir2_5 = new PSDirectory("dir2", PSDirectory.CATALOG_SHALLOW,
         "factory", "auth", PROVIDER_URL, null);

      PSDirectory dir2_6 = new PSDirectory("dir2", PSDirectory.CATALOG_SHALLOW,
         "factory", "auth", PROVIDER_URL, ms_attributes_1);

      // testing compare/equals
      assertTrue(dir1.compare(dir1, dir1));
      assertTrue(!dir1.compare(dir1, dir2));
      assertTrue(!dir2.compare(dir2, dir2_1));
      assertTrue(!dir2.compare(dir2, dir2_2));
      assertTrue(!dir2.compare(dir2, dir2_3));
      assertTrue(!dir2.compare(dir2, dir2_4));
      assertTrue(!dir2.compare(dir2, dir2_5));
      assertTrue(dir2.compare(dir2, dir2_6));

      // testing clone/copyFrom
      PSDirectory clone = (PSDirectory) dir1.clone();
      assertTrue(clone.equals(dir1));
      dir2.copyFrom(dir1);
      assertTrue(dir1.equals(dir2));

      // testing name accessors
      boolean didThrow = false;
      try
      {
         dir1.setName(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         dir1.setName(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      // testing factory accessors
      didThrow = false;
      try
      {
         dir1.setFactory(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         dir1.setFactory(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      // testing provierUrl accessors
      didThrow = false;
      try
      {
         dir1.setProviderUrl(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         dir1.setProviderUrl(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      // testing catalog option accessors
      didThrow = false;
      try
      {
         dir1.setCatalogOption(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         dir1.setCatalogOption(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         dir1.setCatalogOption("invalid");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      assertTrue(dir1.isShallowCatalogOption());
      assertTrue(!dir1.isDeepCatalogOption());

      // testing authentication reference accessors
      didThrow = false;
      try
      {
         dir1.setAuthenticationRef((String) null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         dir1.setAuthenticationRef(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         dir1.setAuthenticationRef((PSReference) null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         PSReference ref = new PSReference("ref1", "invalid");
         dir1.setAuthenticationRef(ref);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      // test attributes accessors
      didThrow = false;
      try
      {
         PSCollection attributes = new PSCollection(String.class);
         dir1.setAttributes(attributes);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         PSCollection attributes = new PSCollection(PSReference.class);
         attributes.add(new PSReference("name", "type"));
         dir1.setAttributes(attributes);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
   }

   /**
    * Test to and from xml methods of this os object.
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testXml() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      // test with optional elements defined
      PSDirectory dir1 = new PSDirectory("dir1", PSDirectory.CATALOG_SHALLOW,
         "factory", "auth", PROVIDER_URL, ms_attributes);
      System.out.println("directory 1:\n" +
         PSXmlDocumentBuilder.toString(dir1.toXml(doc)));
      PSDirectory dir2 = new PSDirectory(dir1.toXml(doc), null, null);
      System.out.println("directory 2:\n" +
         PSXmlDocumentBuilder.toString(dir2.toXml(doc)));
      assertTrue(dir1.equals(dir2));

      // test with optional elements undefined
      PSDirectory dir3 = new PSDirectory("dir3", PSDirectory.CATALOG_SHALLOW,
         "factory", "auth", PROVIDER_URL, null);
      System.out.println("directory 3:\n" +
         PSXmlDocumentBuilder.toString(dir3.toXml(doc)));
      PSDirectory dir4 = new PSDirectory(dir3.toXml(doc), null, null);
      System.out.println("directory 4:\n" +
         PSXmlDocumentBuilder.toString(dir4.toXml(doc)));
      assertTrue(dir3.equals(dir4));
   }

   /**
    * Collect all tests into a TestSuite and return it.
    *
    * @return The suite of test methods for this class.  Not <code>null</code>.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();

      suite.addTest(new PSDirectoryTest("testComponent"));
      suite.addTest(new PSDirectoryTest("testXml"));

      return suite;
   }

   /**
    * A provider url string.
    */
   public static final String PROVIDER_URL =
      "ldap://e2srv:390/dc=Percussion,dc=com";

   /**
    * A collection of attributes.
    */
   public static PSCollection ms_attributes = new PSCollection(String.class);
   static
   {
      ms_attributes.add("attr1");
      ms_attributes.add("attr2");
      ms_attributes.add("attr3");
   }

   /**
    * A collection of attributes.
    */
   public static PSCollection ms_attributes_1 = new PSCollection(String.class);
   static
   {
      ms_attributes_1.add("attr1_1");
      ms_attributes_1.add("attr2_1");
      ms_attributes_1.add("attr3_1");
   }
}

