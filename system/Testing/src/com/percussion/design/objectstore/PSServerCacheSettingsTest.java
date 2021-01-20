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

import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Tests the basic functionality of the <code>PSServerCacheSettings</code> class.
 */
public class PSServerCacheSettingsTest extends TestCase
{
   /**
    * Constructs an instance of this class to run the test implemented by the
    * named method.
    *
    * @param methodName name of the method that implements a test
    */
   public PSServerCacheSettingsTest(String name)
   {
      super( name );
   }

   /**
    * Collects all the tests implemented by this class into a single suite.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest( new PSServerCacheSettingsTest( "testCtors" ) );
      suite.addTest( new PSServerCacheSettingsTest( "testXml" ) );
      return suite;
   }


   /**
    * Performs basic tests with the constructors. Makes sure ctors provide
    * default values to the members of the object and rejects invalid
    * parameters.
    */
   public void testCtors() throws Exception
   {
      PSServerCacheSettings cacheSettings = new PSServerCacheSettings();

      //Make sure by default it is disabled.
      assertTrue( !cacheSettings.isEnabled());
      assertEquals( cacheSettings.getMaxMemoryUsage(), 100*1024*1024); //100MB
      assertEquals( cacheSettings.getMaxDiskUsage(), 1024*1024*1024); //1GB
      assertEquals( cacheSettings.getMaxPageSize(), 100*1024); //100 KB Size
      assertEquals( cacheSettings.getAgingTime(), -1); //Unlimited caching

      //Make sure all the parameters got correct values
      cacheSettings = new PSServerCacheSettings(
         true, true, 10*1024*1024, 100*1024*1024, 100*1024, 300);
      assertTrue( cacheSettings.isEnabled());
      assertEquals( cacheSettings.getMaxMemoryUsage(), 10*1024*1024); //10MB
      assertEquals( cacheSettings.getMaxDiskUsage(), 100*1024*1024); //100 MB
      assertEquals( cacheSettings.getMaxPageSize(), 100*1024); //100 KB      
      assertEquals( cacheSettings.getAgingTime(), 300); //300 minutes

      //maximum memory usage can not be < -1
      testFailCtor( true, true, -2, 100*1024*1024, 100*1024,  300 );

      //maximum disk space usage can not be < -1
      testFailCtor( true, true, 10*1024*1024, -2, 100*1024, 300 );

      //Both memory usage and disk usage can not be zero
      testFailCtor( true, true, 0, 0, 100*1024, 300 );
      
      //maximum page size can not be < -1
      testFailCtor( true, true, 10*1024*1024, 100*1024*1024, -2, 300 );
      
      //maximum page size can not be zero
      testFailCtor( true, true, 10*1024*1024, 100*1024*1024, 0, 300 );

      //Cache aging time can not be < -1
      testFailCtor( true, true, 10*1024*1024, 100*1024*1024, 100*1024, -2 );

      //Cache aging time can not be zero
      testFailCtor( true, true, 10*1024*1024, 100*1024*1024, 100*1024, 0 );
   }

   /**
    * Creates a <code>PSServerCacheSettings</code> object from the supplied
    * values and asserts that the constructor fails by throwing an <code>
    * IllegalArgumentException</code>
    *
    * @param enabled the parameter required for creating the object.
    * @param folderCacheEnabled the parameter required for creating the object.
    * @param memUsage the parameter required for creating the object.
    * @param diskUsage the parameter required for creating the object.
    * @param pageSize the parameter required for creating the object.
    * @param agingTime the parameter required for creating the object.
    */
   private void testFailCtor(boolean enabled, boolean folderCacheEnabled, 
      long memUsage, long diskUsage, long pageSize, long agingTime)
   {
      boolean didThrow = false;
      try
      {
         PSServerCacheSettings cacheSettings = new PSServerCacheSettings(
            enabled, folderCacheEnabled, memUsage, diskUsage, pageSize, 
            agingTime);
      }
      catch(IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue( didThrow );
   }

   /**
    * Tests all valid and invalid cases in creating a <code>
    * PSServerCacheSettings</code> object from an xml element.
    * <br>
    * List of test cases:
    * <ol>
    * <li>From xml, all members got initialized properly</li>
    * <li>Test Equals method, compare the one created from xml with the one
    * created using parameters</li>
    * <li>Tests for the attributes whose values are numbers throws exception for
    * non-number values </li>
    * <li>Tests for missing required attributes</li>
    * </ol>
    * @throws Exception
    */
   public void testXml() throws Exception
   {
      //Test through xml everything got initialized properly.
      Element cacheEl = getXMLNode(true, true, String.valueOf(10*1024*1024),
         String.valueOf(100*1024*1024), String.valueOf(100*1024), "300");
      PSServerCacheSettings cacheSettings = new PSServerCacheSettings(
         cacheEl, null, null );
      assertTrue( cacheSettings.isEnabled());
      assertTrue( cacheSettings.isFolderCacheEnabled());
      assertEquals( cacheSettings.getMaxMemoryUsage(), 10*1024*1024); //10MB
      assertEquals( cacheSettings.getMaxDiskUsage(), 100*1024*1024); //100 MB
      assertEquals( cacheSettings.getMaxPageSize(), 100*1024); //100 KB
      assertEquals( cacheSettings.getAgingTime(), 300); //300 minutes

      //test for equals method, compares the one created from xml with the one
      //created using parameters.
      PSServerCacheSettings otherCacheSet = new PSServerCacheSettings(
         true, true, 10*1024*1024, 100*1024*1024, 100*1024, 300);
      assertEquals(cacheSettings, otherCacheSet);

      //maximum memory usage can not be a string
      testFailXml( true, true, "test", String.valueOf(100*1024*1024), 
         String.valueOf(100*1024), "300" );

      //maximum disk space usage can not be a string
      testFailXml( true, true, String.valueOf(10*1024*1024), "test", 
         String.valueOf(100*1024), "300" );
         
      //maximum page size can not be a string
      testFailXml( true, true, String.valueOf(10*1024*1024), 
         String.valueOf(100*1024*1024), "test", "300" );         

      //Cache aging time can not be a string
      testFailXml( true, true, String.valueOf(10*1024*1024),
         String.valueOf(100*1024*1024), String.valueOf(100*1024), "test" );

      //missing "enabled" attribute
      Element copy = (Element)cacheEl.cloneNode(true);
      copy.removeAttribute("enabled");
      testFailXml( copy );

      //missing "memUsage" attribute
      copy = (Element)cacheEl.cloneNode(true);
      copy.removeAttribute("maxMemory");
      testFailXml( copy );

      //missing "maxDiskSpace" attribute
      copy = (Element)cacheEl.cloneNode(true);
      copy.removeAttribute("maxDiskSpace");
      testFailXml( copy );
      
      //missing "maxPageSize" attribute
      copy = (Element)cacheEl.cloneNode(true);
      copy.removeAttribute("maxPageSize");
      testFailXml( copy );

      //missing "agingTime" attribute
      copy = (Element)cacheEl.cloneNode(true);
      copy.removeAttribute("agingTime");
      testFailXml( copy );
   }

   /**
    * Creates a <code>PSServerCacheSettings</code> object from supplied source
    * element and asserts that the constructor fails by throwing a <code>
    * PSUnknownNodeTypeException</code>.
    *
    * @param source the source element to construct the object from, assumed
    * not to be <code>null</code>
    */
   private void testFailXml(Element source)
   {
      boolean didThrow = false;
      try
      {
         PSServerCacheSettings cacheSettings = new PSServerCacheSettings(
            source, null, null );
      }
      catch(PSUnknownNodeTypeException e)
      {
         didThrow = true;
      }
      assertTrue( didThrow );
   }

   /**
    * Creates an xml element from the supplied values and calls {@link
    * #testFailXml(Element)} to assert that constructing {@link
    * PSServerCacheSettings#PSServerCacheSettings(Element)
    * PSServerCacheSettings} fails by throwing a <code>
    * PSUnknownNodeTypeException</code>.
    *
    * @param enabled the parameter required for creating the object.
    * @param folderCacheEnabled <code>true</code> if enabled the folder cache.
    * @param memUsage the parameter required for creating the object.
    * @param diskUsage the parameter required for creating the object.
    * @param pageSize the parameter required for creating the object.
    * @param agingTime the parameter required for creating the object.
    */
   private void testFailXml(boolean enabled, boolean folderCacheEnabled,
         String memUsage, String diskUsage, String pageSize, String agingTime)
   {
      testFailXml(getXMLNode(enabled, folderCacheEnabled, memUsage, diskUsage,
            pageSize, agingTime));
   }

   /**
    * Creates a <code>PSXServerCacheSettings</code> element with supplied values
    * as its attribute values. Please see {@link PSServerCacheSettings#toXml}
    * for the format of the element
    *
    * @param enabled the attribute value required for creating the element.
    * @param folderCacheEnabled the attribute value to enable/disable folder
    *    cache.
    * @param memUsage the attribute value required for creating the element.
    * @param diskUsage the attribute value required for creating the element.
    * @param pageSize the parameter required for creating the object.
    * @param agingTime the attribute value required for creating the element.
    *
    * @return the element, never <code>null</code>
    */
   private Element getXMLNode(boolean enabled, boolean folderCacheEnabled,
         String memUsage, String diskUsage, String pageSize, String agingTime)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = doc.createElement("PSXServerCacheSettings");
      root.setAttribute("id", "0");
      if(enabled)
         root.setAttribute("enabled", "yes");
      else
         root.setAttribute("enabled", "no");
      if (folderCacheEnabled)
         root.setAttribute("folderCacheEnabled", "yes");
      else
         root.setAttribute("folderCacheEnabled", "no");
      root.setAttribute("maxMemory", memUsage);
      root.setAttribute("maxDiskSpace", diskUsage);
      root.setAttribute("maxPageSize", pageSize);      
      root.setAttribute("agingTime", agingTime);

      return root;
   }
}
