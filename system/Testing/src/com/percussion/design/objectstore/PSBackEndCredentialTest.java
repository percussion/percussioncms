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

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.xml.PSXmlDocumentBuilder;

/**
 * Unit tests for the PSBackEndCredential class.
 */
public class PSBackEndCredentialTest extends TestCase
{
   public void testConstructor() throws Exception
   {
      String alias = "foobarbaz";
      PSBackEndCredential cred = new PSBackEndCredential(alias);
      assertEquals(alias, cred.getAlias());
      PSBackEndCredential otherCred = new PSBackEndCredential(alias);
      assertEquals(cred, otherCred);

      // pass invalid stuff to the constructor
      boolean didThrow = false;
      try
      {
         new PSBackEndCredential(null);
      }
      catch (IllegalArgumentException ex)
      {
         didThrow = true;
      }
      assertTrue("Caught cons with null alias?", didThrow);

      didThrow = false;
      try
      {
         new PSBackEndCredential("");
      }
      catch (IllegalArgumentException ex)
      {
         didThrow = true;
      }
      assertTrue("Caught cons with empty alias?", didThrow);
   }

   public void testGetSet() throws Exception
   {
      String alias = "alias";
      PSBackEndCredential cred = new PSBackEndCredential(alias);
      assertEquals(alias, cred.getAlias());
      cred.setAlias("foo");
      assertEquals("foo", cred.getAlias());

      assertNull(cred.getDataSource());
      String ds = "datasource";
      cred.setDataSource(ds);
      assertEquals(ds, cred.getDataSource());

      assertEquals("", cred.getComment());
      String comment = "this is a comment";
      cred.setComment(comment);
      assertEquals(comment, cred.getComment());
   }

   public void testXml() throws Exception
   {
      String alias = "alias", ds = "datasource", comment = "comment";

      PSBackEndCredential cred = new PSBackEndCredential(alias);
        cred.setDataSource(ds);
      cred.setComment(comment);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = cred.toXml(doc);

      PSBackEndCredential otherCred = new PSBackEndCredential();
      assertTrue(!cred.equals(otherCred));

      otherCred.fromXml(el, null, null);

      assertEquals(cred, otherCred);
   }
}
