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

import com.percussion.utils.server.IPSCgiVariables;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test for the <code>PSResourceCacheSettings</code> class.
 */
public class PSResourceCacheSettingsTest extends TestCase
{
   /**
    * Construct this unit test
    * 
    * @param name The name of this test.
    */
   public PSResourceCacheSettingsTest(String name)
   {
      super(name);
   }
   
   /**
    * Test all functionality of this object.
    * 
    * @throws Exception if there are any errors.
    */
   public void testAll() throws Exception
   {
      // ensure ctor defaults to not enabled
      PSResourceCacheSettings settings = new PSResourceCacheSettings();
      assertTrue(!settings.isCachingEnabled());
      PSResourceCacheSettings settings2 = new PSResourceCacheSettings();
      assertEquals(settings, settings2);
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = settings.toXml(doc);
      settings2 = new PSResourceCacheSettings(root);
      assertEquals(settings, settings2);
      
      // test equals on enabled, be sure it serializes
      settings.setIsCachingEnabled(true);
      assertTrue(!settings.equals(settings2));
      root = settings.toXml(doc);
      settings2 = new PSResourceCacheSettings(root);
      assertEquals(settings, settings2);
      
      // now add some keys and dependencies, and test equals and serialization
      List keys = new ArrayList();
      keys.add(new PSCookie("foo"));
      keys.add(new PSCgiVariable(IPSCgiVariables.CGI_AUTH_TYPE));
      keys.add(new PSUserContext("User/Name"));
      settings.setAdditionalKeys(keys.iterator());
      assertTrue(!settings.equals(settings2));
      root = settings.toXml(doc);
      settings2 = new PSResourceCacheSettings(root);
      assertEquals(settings, settings2);
      
      List deps = new ArrayList();
      deps.add("app1/resource1");
      deps.add("app2/resource2");
      deps.add("app3/resource3");
      
      settings.setDependencies(deps.iterator());
      assertTrue(!settings.equals(settings2));
      root = settings.toXml(doc);
      settings2 = new PSResourceCacheSettings(root);
      assertEquals(settings, settings2);
      
      // test hashcode
      assertEquals(settings.hashCode(), settings2.hashCode());
      
      // test copy
      settings2 = new PSResourceCacheSettings(settings);
      assertEquals(settings, settings2);
   }
   
   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSResourceCacheSettingsTest("testAll"));
      return suite;
   }
}
