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
package com.percussion.utils.container;

import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;

import junit.framework.TestCase;

/**
 * Test case for the {@link PSSecureCredentials} class.
 */
public class PSSecureCredentialsTest extends TestCase
{
   /**
    * Test the parameterized ctor and accessors
    * 
    * @throws Exception
    */
   public void testCtor() throws Exception
   {
      String ds = "jdbc/RxDefault";
      String uid = "sa";
      String pwd = "demo";
      PSSecureCredentials creds = new PSSecureCredentials(ds, uid, pwd);
      assertEquals(ds, creds.getDatasourceName());
      assertEquals(uid, creds.getUserId());
      assertEquals(pwd, creds.getPassword());
      assertEquals("rx.datasource." + ds.replace('/', '_'), 
         creds.getSecurityDomainName());
      
      creds = new PSSecureCredentials(ds, uid, "");
      assertEquals("", creds.getPassword());
      creds = new PSSecureCredentials(ds, uid, null);
      assertEquals("", creds.getPassword());
      
      doTestCtor(null, uid, pwd, true);
      doTestCtor("", uid, pwd, true);
      doTestCtor(ds, null, pwd, true);
      doTestCtor(ds, "", pwd, true);   
      
   }
   
   /**
    * Test round trip XML serialization
    * 
    * @throws Exception
    */
   public void testXml() throws Exception
   {
      String ds = "jdbc/RxDefault";
      String uid = "sa";
      String pwd = "demo";
      PSSecureCredentials creds = new PSSecureCredentials(ds, uid, pwd);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      assertEquals(creds, new PSSecureCredentials(creds.toXml(doc)));
      creds = new PSSecureCredentials(ds, uid, "");
      assertEquals(creds, new PSSecureCredentials(creds.toXml(doc)));
   }
   
   /**
    * Attempts to construct a {@link PSSecureCredentials} using the supplied 
    * args.
    * 
    * @param ds The datasource, may be <code>null</code>.
    * @param uid The username, may be <code>null</code> or empty.
    * @param pwd The password, may be <code>null</code> or empty.
    * @param shouldThrow <code>true</code> if the supplied args should cause
    * an exception, <code>false</code> if not.
    * 
    * @throws Exception if the test fails.
    */
   private void doTestCtor(String ds, String uid, String pwd, 
      boolean shouldThrow) throws Exception
   {
      try
      {
         new PSSecureCredentials(ds, uid, pwd);
      }
      catch (Exception e)
      {
         assertTrue(shouldThrow);
         return;
      }
      
      assertFalse(shouldThrow);
   }
}

