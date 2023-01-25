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

