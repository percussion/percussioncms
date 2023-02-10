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

