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
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;

/**
 * Test the clone site folder request according to the schema defined in schema
 * sys_FolderParameters.xsd.
 */
public class PSCloneSiteFolderRequestTest extends TestCase
{
   // see base class for documentation
   public PSCloneSiteFolderRequestTest(String name)
   {
      super(name);
   }
   
   /**
    * Test all public constuctors.
    * 
    * @throws Exception for any error.
    */
   public void testConstructors() throws Exception
   {
      PSLocator source = new PSLocator(1, 1);
      PSLocator target = new PSLocator(1, 1);
      PSCloningOptions options = new PSCloningOptions(
         PSCloningOptions.TYPE_SITE, "siteToCopy", "siteName", "folderName", 
         PSCloningOptions.COPY_NO_CONTENT, PSCloningOptions.COPYCONTENT_AS_LINK, 
         null);
      
      // test valid parameters
      new PSCloneSiteFolderRequest(source, target, options);
      
      // test invalid source
      Exception exception = null;
      try
      {
         new PSCloneSiteFolderRequest(null, target, options);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
      
      // test invalid target
      exception = null;
      try
      {
         new PSCloneSiteFolderRequest(source, null, options);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
      
      // test invalid options
      exception = null;
      try
      {
         new PSCloneSiteFolderRequest(source, target, null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
   }
   
   /**
    * Test all public methods contracts.
    * 
    * @throws Exception for any error.
    */
   public void testPublicAPI() throws Exception
   {
      Map communityMappings = new HashMap();
      communityMappings.put(new Integer(1), new Integer(2));
      communityMappings.put(new Integer(3), new Integer(4));
      communityMappings.put(new Integer(5), new Integer(6));
      
      PSCloningOptions options = new PSCloningOptions(
         PSCloningOptions.TYPE_SITE, "siteToCopy", "siteName", "folderName", 
         PSCloningOptions.COPY_NO_CONTENT, PSCloningOptions.COPYCONTENT_AS_LINK, 
         communityMappings);

      PSLocator locator_1 = new PSLocator(1, 1);
      PSLocator locator_2 = new PSLocator(2, 2);
      
      PSCloneSiteFolderRequest request_1 = new PSCloneSiteFolderRequest(
         locator_1, locator_1, options);
      
      PSCloneSiteFolderRequest request_2 = new PSCloneSiteFolderRequest(
         locator_2, locator_2, options);
      
      assertTrue(!request_1.equals(request_2));
      
      // test copyFrom
      request_2.copyFrom(request_1);
      assertTrue(request_1.equals(request_2));
      
      // test clone
      assertTrue(request_1.equals(request_1.clone()));
      
      // test toXml / fromXml
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSCloneSiteFolderRequest request_1_copy = 
         new PSCloneSiteFolderRequest(request_1.toXml(doc), null, null);
      assertTrue(request_1.equals(request_1_copy));
   }
   
   // see base class for documentation
   public static Test suite()
   {
      TestSuite suite = new TestSuite(PSCloneSiteFolderRequestTest.class);
      
      return suite;
   }
}
