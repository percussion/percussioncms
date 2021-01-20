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
