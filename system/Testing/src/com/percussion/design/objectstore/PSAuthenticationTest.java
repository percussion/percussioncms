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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.junit.Assert.assertTrue;

/**
 * Authentication object store class testing, including constructors,
 * <code>PSComponent</code> functionality, accessors and XML functionality.
 */
public class PSAuthenticationTest
{

   @Rule
   public TemporaryFolder temporaryFolder = new TemporaryFolder();

   private String rxdeploydir;

   @Before
   public void setup(){
      rxdeploydir = System.getProperty("rxdeploydir");
      System.setProperty("rxdeploydir",temporaryFolder.getRoot().getAbsolutePath());
   }

   @After
   public void teardown(){
      //Reset the deploy dir property if it was set prior to test
      if(rxdeploydir != null)
         System.setProperty("rxdeploydir",rxdeploydir);
   }

   public PSAuthenticationTest() {}

   /**
    * Test component constructors and accessors.
    *
    * @throws Exception if any exceptions occur or assertions fail.
    */
   @Test
   public void testComponent() throws Exception
   {
      PSAuthentication auth1 = new PSAuthentication("auth1",
         PSAuthentication.SCHEME_SIMPLE, "user", "userAttr", "pw",
         "filter");

      PSAuthentication auth2 = new PSAuthentication("auth2",
         PSAuthentication.SCHEME_SIMPLE, "user", "userAttr", "pw",
         "filter");

      PSAuthentication auth2_1 = new PSAuthentication("auth2",
         PSAuthentication.SCHEME_NONE, "user", "userAttr", "pw",
         "filter");

      PSAuthentication auth2_2 = new PSAuthentication("auth2",
         PSAuthentication.SCHEME_SIMPLE, "user1", "userAttr", "pw",
         "filter");

      PSAuthentication auth2_3 = new PSAuthentication("auth2",
         PSAuthentication.SCHEME_SIMPLE, "user", "userAttr1", "pw",
         "filter");

      PSAuthentication auth2_4 = new PSAuthentication("auth2",
         PSAuthentication.SCHEME_SIMPLE, "user", "userAttr", "pw1",
         "filter");

      PSAuthentication auth2_5 = new PSAuthentication("auth2",
         PSAuthentication.SCHEME_SIMPLE, "user", "userAttr", "pw",
         "filter1");

      // testing compare/equals
      assertTrue(PSComponent.compare(auth1, auth1));
      assertTrue(!PSComponent.compare(auth1, auth2));
      assertTrue(!PSComponent.compare(auth2, auth2_1));
      assertTrue(!PSComponent.compare(auth2, auth2_2));
      assertTrue(!PSComponent.compare(auth2, auth2_3));
      assertTrue(PSComponent.compare(auth2, auth2_4));
      assertTrue(!PSComponent.compare(auth2, auth2_5));

      // testing clone/copyFrom
      PSAuthentication clone = (PSAuthentication) auth1.clone();
      assertTrue(clone.equals(auth1));
      auth2.copyFrom(auth1);
      assertTrue(auth1.equals(auth2));
      
      // test append base dn
      clone.setAppendBaseDn(true);
      assertTrue(!clone.equals(auth1));

      // testing name accessors
      boolean didThrow = false;
      try
      {
         auth1.setName(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         auth1.setName(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      // testing scheme accessors
      didThrow = false;
      try
      {
         auth1.setScheme(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         auth1.setScheme(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         auth1.setScheme("not suported");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      // testing user accessors
      didThrow = false;
      try
      {
         auth1.setUser(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(!didThrow);

      didThrow = false;
      try
      {
         auth1.setUser(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(!didThrow);

      // testing user attribute accessors
      didThrow = false;
      try
      {
         auth1.setUserAttr(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(!didThrow);

      didThrow = false;
      try
      {
         auth1.setUserAttr(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(!didThrow);

      // testing password accessors
      didThrow = false;
      try
      {
         auth1.setPassword(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(!didThrow);
      assertTrue(auth1.getPassword().equals(""));

      didThrow = false;
      try
      {
         auth1.setPassword(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(!didThrow);

      // testing filter extension accessors
      didThrow = false;
      try
      {
         auth1.setFilterExtension(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(!didThrow);
      assertTrue(auth1.getFilterExtension().equals(""));

      didThrow = false;
      try
      {
         auth1.setFilterExtension(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(!didThrow);
   }

   /**
    * Test to and from xml methods of this os object.
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   @Test
   public void testXml() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      // test with optional elements defined
      PSAuthentication auth1 = new PSAuthentication("auth1",
         PSAuthentication.SCHEME_SIMPLE, "user", "userAttr", "pw", "filter");
      System.out.println("authentication 1:\n" +
         PSXmlDocumentBuilder.toString(auth1.toXml(doc)));
      
      Element auth1Elem = auth1.toXml(doc);   
      
      PSAuthentication auth2 = new PSAuthentication(auth1Elem, null, null);
      System.out.println("authentication 2:\n" +
         PSXmlDocumentBuilder.toString(auth2.toXml(doc)));
      assertTrue(auth1.equals(auth2));
      
      // test unencrypted password
      NodeList passwords = auth1Elem.getElementsByTagName("Password");
      Element oldPassword = (Element) passwords.item(0);
      Element newPassword = doc.createElement("Password");
      newPassword.setAttribute("attributeName", "pwAttr");
      newPassword.setAttribute("encrypted", "no");
      newPassword.appendChild(doc.createTextNode("pw"));
      auth1Elem.getFirstChild().replaceChild(newPassword, oldPassword);
      PSAuthentication auth2unencrypted = new PSAuthentication(auth1Elem, 
         null, null);
      System.out.println("authentication 2 unencrypted:\n" +
         PSXmlDocumentBuilder.toString(auth2unencrypted.toXml(doc)));
      assertTrue(auth1.equals(auth2unencrypted));

      // test with optional elements undefined
      PSAuthentication auth3 = new PSAuthentication("auth3",
         PSAuthentication.SCHEME_SIMPLE, null, null, null, null);
      System.out.println("authentication 3:\n" +
         PSXmlDocumentBuilder.toString(auth3.toXml(doc)));
      PSAuthentication auth4 = new PSAuthentication(auth3.toXml(doc), null, null);
      System.out.println("authentication 4:\n" +
         PSXmlDocumentBuilder.toString(auth4.toXml(doc)));
      assertTrue(auth3.equals(auth4));
   }
}

