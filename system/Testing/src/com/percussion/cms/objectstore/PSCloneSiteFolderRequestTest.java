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
