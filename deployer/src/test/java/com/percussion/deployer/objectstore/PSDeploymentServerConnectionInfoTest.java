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

package com.percussion.deployer.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;

/**
 * Unit tests for <code>PSDeploymentServerConnectionInfo</code>.
 */
public class PSDeploymentServerConnectionInfoTest extends TestCase
{
   /**
    * Constructs a test case for the specified test name.
    * 
    * @param name The name of this test.
    */
   public PSDeploymentServerConnectionInfoTest(String name)
   {
      super(name);
   }

   /**
    * Tests constructor using valid and invalid parameters.
    * 
    * @throws Exception If there are any errors.
    */
   public void testConstructor() throws Exception
   {
      // these should work fine
      assertTrue(isCtorValid("localhost", 9992, "admin1", "demo", false));
      assertTrue(isCtorValid("localhost", 9992, "admin1", "demo", true));
      assertTrue(isCtorValid("localhost", 9992, "admin1", null, false));
      assertTrue(isCtorValid("localhost", 9992, "admin1", "", false));

      // these should be a problem
      assertTrue(!isCtorValid("", 9992, "admin1", "demo", false));
      assertTrue(!isCtorValid(null, 9992, "admin1", "demo", false));
      assertTrue(!isCtorValid("localhost", 0, "admin1", "demo", false));
      assertTrue(!isCtorValid("localhost", 9992, "", "demo", false));
      assertTrue(!isCtorValid("localhost", 9992, null, "demo", false));
   }

   /**
    * Tests the <code>equals</code> and <code>copyFrom</code> methods.
    * 
    * @throws Exception if there are any errors.
    */
   public void testEquals() throws Exception
   {
      PSDeploymentServerConnectionInfo info = new PSDeploymentServerConnectionInfo(
            "host", 1, "user", "pass", false);
      PSDeploymentServerConnectionInfo same = new PSDeploymentServerConnectionInfo(
            "host", 1, "user", "pass", false);
      assertTrue(info.equals(same));
      assertTrue(info.hashCode() == same.hashCode());

      PSDeploymentServerConnectionInfo different;
      different = new PSDeploymentServerConnectionInfo("host", 2, "user",
            "pass", false);
      assertFalse(info.equals(different));
      different = new PSDeploymentServerConnectionInfo("host", 1, "user",
            "pass", true);
      assertFalse(info.equals(different));
      assertFalse(info.hashCode() == different.hashCode());

      different.copyFrom(info);
      assertTrue(info.equals(different));
      assertTrue(info.hashCode() == different.hashCode());
   }

   /**
    * Tests XML serialization.
    * 
    * @throws Exception if there are any errors
    */
   public void testXml() throws Exception
   {
      PSDeploymentServerConnectionInfo info = new PSDeploymentServerConnectionInfo(
            "host", 1, "user", "pass", false);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      assertEquals(info, new PSDeploymentServerConnectionInfo(info.toXml(doc)));
   }

   /**
    * Helper method for constructing
    * <code>PSDeploymentServerConnectionInfo</code> and catching any
    * <code>IllegalArgumentException</code>.
    * 
    * @param host value passed to the ctor
    * @param port value passed to the ctor
    * @param user value passed to the ctor
    * @param pass value passed to the ctor
    * @param isEncrypted value passed to the ctor
    * 
    * @return <code>true</code> if the object constructed without error;
    *         <code>false</code> if the constructor threw an
    *         IllegalArgumentException
    */
   private boolean isCtorValid(String host, int port, String user, String pass,
         boolean isEncrypted)
   {
      boolean valid = true;
      try
      {
         new PSDeploymentServerConnectionInfo(host, port, user, pass,
               isEncrypted);
      }
      catch (IllegalArgumentException e)
      {
         valid = false;
      }
      return valid;
   }

   /**
    * Collects all this class' tests into a TestSuite.
    * 
    * @return A suite of tests, one for each test in the class. Never
    *         <code>null</code>.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite
            .addTest(new PSDeploymentServerConnectionInfoTest("testConstructor"));
      suite.addTest(new PSDeploymentServerConnectionInfoTest("testEquals"));
      suite.addTest(new PSDeploymentServerConnectionInfoTest("testXml"));
      return suite;
   }

}
