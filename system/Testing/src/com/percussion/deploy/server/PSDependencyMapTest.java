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
package com.percussion.deploy.server;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;

/**
 * Unit test for the PSDependencyMap and PSDependencyDef classes
 */
public class PSDependencyMapTest extends TestCase
{
   /**
    * Construct this unit test
    * 
    * @param name The name of this test.
    */
   public PSDependencyMapTest(String name)
   {
      super(name);
   }

   /**
    * Tests the map class.  
    */   
   public void testMap() throws Exception
   {
      PSDependencyMap map = getMap();
      
      Iterator defs = map.getDefs();
      assertTrue(defs != null);
      assertTrue(defs.hasNext());
      while (defs.hasNext())
      {
         PSDependencyDef def = (PSDependencyDef)defs.next();
      }
      
      PSDependencyDef def = map.getDependencyDef("foo");
      assertNull(def);
      
   }
   
   /**
    * Load the map from the unit test resource directory.
    * 
    * @return The config, never <code>null</code>.
    * 
    * @throws Exception if there are any errors.
    */
   private PSDependencyMap getMap() throws Exception
   {
      FileInputStream in = null;
      try 
      {
         File f = new File(RESOURCE_D0C);
         System.out.println("f = " + f.getAbsolutePath());
         in = new FileInputStream(RESOURCE_D0C);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         return new PSDependencyMap(doc.getDocumentElement(), false);
      }
      finally 
      {
         if (in != null)
            try {in.close();} catch(IOException e){}
      }
   }
   
   
   
   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSDependencyMapTest("testMap"));
      return suite;
   }
 
   /**
    * Defines the path to the files used by this unit test, relative from the
    * E2 root.
    */ 
   private static final String RESOURCE_D0C = 
      "config/Deployment/sys_DependencyMap.xml";
   
}
