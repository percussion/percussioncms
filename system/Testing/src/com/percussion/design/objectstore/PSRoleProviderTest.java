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

import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;

/**
 * Role provider object store class testing, including constructors,
 * <code>PSComponent</code> functionality, accessors and XML functionality.
 */
public class PSRoleProviderTest extends TestCase
{
   /**
    * Constructor to call base class constructor.
    *
    * @see TestCase#TestCase(String) for more information.
    */
   public PSRoleProviderTest(String name)
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
      PSRoleProvider prov1 = new PSRoleProvider("prov1",
         PSRoleProvider.TYPE_DIRECTORY, "directoryRef");

      PSRoleProvider prov2 = new PSRoleProvider("prov2",
         PSRoleProvider.TYPE_DIRECTORY, "directoryRef");

      PSRoleProvider prov2_1 = new PSRoleProvider("prov2",
         PSRoleProvider.TYPE_BACKEND, (String) null);

      // testing compare/equals
      assertTrue(prov1.compare(prov1, prov1));
      assertTrue(!prov1.compare(prov1, prov2));
      assertTrue(!prov2.compare(prov2, prov2_1));

      // testing clone/copyFrom
      PSRoleProvider clone = (PSRoleProvider) prov1.clone();
      assertTrue(clone.equals(prov1));
      prov2.copyFrom(prov1);
      assertTrue(prov1.equals(prov2));

      // testing name accessors
      boolean didThrow = false;
      try
      {
         prov1.setName(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         prov1.setName(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      // testing type accessors
      assertTrue(prov1.isDirectoryRoleProvider());
      assertTrue(!prov1.isBackendRoleProvider());

      // testing directory reference accessors
      didThrow = false;
      try
      {
         prov1.setDirectoryRef((String) null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         prov1.setDirectoryRef(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         prov1.setDirectoryRef((PSReference) null);
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
         prov1.setDirectoryRef(ref);
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
      PSRoleProvider prov1 = new PSRoleProvider("prov1",
         PSRoleProvider.TYPE_DIRECTORY, "directoryRef");
      System.out.println("role provider 1:\n" +
         PSXmlDocumentBuilder.toString(prov1.toXml(doc)));
      PSRoleProvider prov2 = new PSRoleProvider(prov1.toXml(doc), null, null);
      System.out.println("role provider 2:\n" +
         PSXmlDocumentBuilder.toString(prov2.toXml(doc)));
      assertTrue(prov1.equals(prov2));

      // test with optional elements undefined
      PSRoleProvider prov3 = new PSRoleProvider("prov3",
         PSRoleProvider.TYPE_BACKEND, (String) null);
      System.out.println("role provider 3:\n" +
         PSXmlDocumentBuilder.toString(prov3.toXml(doc)));
      PSRoleProvider prov4 = new PSRoleProvider(prov3.toXml(doc), null, null);
      System.out.println("role provider 4:\n" +
         PSXmlDocumentBuilder.toString(prov4.toXml(doc)));
      assertTrue(prov3.equals(prov4));
   }

   /**
    * Collect all tests into a TestSuite and return it.
    *
    * @return The suite of test methods for this class.  Not <code>null</code>.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();

      suite.addTest(new PSRoleProviderTest("testComponent"));
      suite.addTest(new PSRoleProviderTest("testXml"));

      return suite;
   }
}

