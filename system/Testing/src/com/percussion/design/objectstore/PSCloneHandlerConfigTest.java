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

import com.percussion.error.PSExceptionUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

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
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
