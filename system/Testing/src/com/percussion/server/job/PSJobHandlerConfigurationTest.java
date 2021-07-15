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

package com.percussion.server.job;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for the <code>PSJobHandlerConfiguration</code> class
 */
public class PSJobHandlerConfigurationTest
{
   
   /**
    * Construct this unit test
    *
    */
   public PSJobHandlerConfigurationTest()
   {
   }
   
   /**
    * Tests the config
    * 
    * @throws Exception if there are any errors.
    */
   @Test
   public void testConfig() throws Exception
   {
      PSJobHandlerConfiguration cfg = getConfig();
      Properties hProps = cfg.getHandlerInitParams();
      assertEquals(hProps.getProperty("name1"), "value1");
      assertEquals(hProps.getProperty("name2"), "value2");
      
      for (int c=1; c < 3; c++)
      {
         for (int j=1; j < 3; j++)
         {
            String cat = "cat" + c;
            String job = "job" + j;

            // classname
            String className = cfg.getJobClassName(cat, cat + job);
            assertEquals(className, "com.percussion." + cat + "." + job);
            
            Properties jobProps = cfg.getJobInitParams(cat, cat + job);
            
            for (int p=1; p <3; p++)
            {
               String name = "name" + p;
               String value = "value" + p;
               
               String cname = cat + name;
               String cvalue = cat + value;
               
               String jname = cat + job + name;
               String jvalue = cat + job + value;
               
               
               // handler params
               assertEquals(jobProps.getProperty(name), value);
               
               // cat params
               assertEquals(jobProps.getProperty(cname), cvalue);
               
               // job param
               assertEquals(jobProps.getProperty(jname), jvalue);
            }
         }
      }
   }
   
   /**
    * Load the config from the unit test resource directory.
    * 
    * @return The config, never <code>null</code>.
    * 
    * @throws Exception if there are any errors.
    */
   private PSJobHandlerConfiguration getConfig() throws Exception
   {
      try( InputStream in = this.getClass().getResourceAsStream(RESOURCE_D0C))
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         return new PSJobHandlerConfiguration(doc);
      }
   }

   
   /**
    * Defines the path to the files used by this unit test, relative from the
    * E2 root.
    */ 
   private static final String RESOURCE_D0C = 
      "/com/percussion/server/job/sys_JobHandlerConfiguration.xml";
   
}
