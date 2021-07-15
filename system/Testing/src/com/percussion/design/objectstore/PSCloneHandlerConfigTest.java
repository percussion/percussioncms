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

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test for the {@link PSCloneHandlerConfigSet} and 
 * {@link PSCloneHandlerConfigTest} class.
 */
public class PSCloneHandlerConfigTest extends TestCase
{

   private static final Logger log = LogManager.getLogger(PSCloneHandlerConfigTest.class);

   // see base class
   public PSCloneHandlerConfigTest(String name)
   {
      super(name);
   }
 
   /**
    * Test to/from XML methods.
    * 
    * @throws Exception if any errors occur.
    */  
   public void test() throws Exception
   {
      PSCloneHandlerConfigSet configSet = null;
      try
      {
         InputStream is = new FileInputStream(
            new File(RESOURCE_PATH + "cloneHandlerConfigurations.xml"));
         configSet = new PSCloneHandlerConfigSet(PSXmlDocumentBuilder
            .createXmlDocument(is, false).getDocumentElement(), null, null);
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         assertTrue("PSCloneHandlerConfigSet ctor failed", false);
      }
      
      try
      {
         Iterator configs = configSet.iterator();
         while (configs.hasNext())
         {
            PSCloneHandlerConfig config = (PSCloneHandlerConfig) configs.next();
            
            Document doc = PSXmlDocumentBuilder.createXmlDocument();
            Element elem = config.toXml(doc);
            doc.appendChild(elem);
            
            System.out.println("\n\nConfiguration: " + config.getName());
            System.out.println(PSXmlDocumentBuilder.toString(doc));
         }
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         assertTrue("PSCloneHandlerConfigSet toXml failed", false);
      }
   }
   
   // collect all tests into a TestSuite and return it - see base class
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSCloneHandlerConfigTest("test"));
      return suite;
   }

   /**
    * Defines the path to the files used by this unit test, relative from the
    * E2 root.
    */
   private static final String RESOURCE_PATH =
      "UnitTestResources/com/percussion/design/objectstore/";
}
