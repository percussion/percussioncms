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

import com.percussion.server.IPSCgiVariables;
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
