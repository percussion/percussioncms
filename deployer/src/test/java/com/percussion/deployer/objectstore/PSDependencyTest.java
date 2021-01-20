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

package com.percussion.deployer.objectstore;

import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 *  Unit test class for all dependency objects.
 */
public class PSDependencyTest
{
   @Rule
   public TemporaryFolder tempFolder = new TemporaryFolder();


   public PSDependencyTest(){}

   /**
    * Tests the <code>getParentDependency</code> method.
    * @throws Exception
    */
   @Test
   public void testDoublyLinkedList() throws Exception
   {
      PSDeployableObject do1 = new PSDeployableObject(
            PSDependency.TYPE_LOCAL, "1", "TestObj1", "Test Object1", 
            "myTestObject1", true, false, true);
      // parent dep should be null until this is assigned as a child
      assertNull(do1.getParentDependency());
      
      PSDeployableObject do2 = new PSDeployableObject(
            PSDependency.TYPE_SHARED, "2", "TestObj2", "Test Object2", 
            "myTestObject2", true, false, false);
      // parent dep should be null until this is assigned as a child
      assertNull(do2.getParentDependency());
         
         List objList = new ArrayList();
         objList.add(do1);
         objList.add(do2);

         PSDeployableElement de1 = new PSDeployableElement(
               PSDependency.TYPE_SHARED, "1", "TestElem", "Test Element", 
               "myTestElement", true, false, false);

         de1.setDescription("This is a test!");
         de1.setDependencies(objList.iterator());

         assertNull(de1.getParentDependency());
         assertNotNull(do1.getParentDependency());
         assertSame(de1, do1.getParentDependency());
         assertNotNull(do2.getParentDependency());
         assertSame(de1, do2.getParentDependency());
         
         // make sure fromXml sets the parent
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element el = de1.toXml(doc);
         PSDeployableElement de2 = new PSDeployableElement(el);
         assertEquals(de1, de2);
         for (Iterator i = de2.getDependencies(); i.hasNext();)
         {
            PSDependency dep = (PSDependency) i.next();
            assertNotNull(dep.getParentDependency());
            assertSame(de2, dep.getParentDependency());         
         }

         // see if clone maintains parents correctly
         PSDependency clone = (PSDependency) de1.clone();
         assertEquals(de1, clone);
         // de1's deps should point to de1
         for (Iterator i = de1.getDependencies(); i.hasNext();)
         {
            PSDependency dep = (PSDependency) i.next();
            assertNotNull(dep.getParentDependency());
            assertSame(de1, dep.getParentDependency());         
         }
         // clone's deps should point to clone (clone is deep)
         for (Iterator i = clone.getDependencies(); i.hasNext();)
         {
            PSDependency dep = (PSDependency) i.next();
            assertNotNull(dep.getParentDependency());
            assertSame(clone, dep.getParentDependency());         
         }
         
         // finally, make sure removed children have parent dep cleared
         de1.setDependencies(null);
         assertNull(do1.getParentDependency());
         assertNull(do2.getParentDependency());
   }
   
   
   /**
    * Test all functionality of the dependency objects.
    * 
    * @throws Exception if there are any errors.
    */
   @Test
   public void testAll() throws Exception
   {
      PSDeployableElement de1 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "1", "TestElem", "Test Element", 
         "myTestElement", true, false, false);
         
      PSDeployableElement de2 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "2", "TestElem2", "Test Element2", 
         "myTestElement2", true, false, true);
      PSDeployableElement de3 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "3", "TestElem3", "Test Element3", 
         "myTestElement3", true, false, false);
      List elemList = new ArrayList();
      elemList.add(de2);
      elemList.add(de3);
      
      PSDeployableObject do1 = new PSDeployableObject(
         PSDependency.TYPE_LOCAL, "1", "TestObj1", "Test Object1", 
         "myTestObject1", true, false, true);
      List classList = new ArrayList();
      classList.add("com.percussion.deployer.PSDependency");
      classList.add("com.percussion.deployer.PSDependencyTest");
      do1.setRequiredClasses(classList.iterator());  
      PSDeployableObject do2 = new PSDeployableObject(
         PSDependency.TYPE_SHARED, "2", "TestObj2", "Test Object2", 
         "myTestObject2", true, false, false);
      
      List objList = new ArrayList();
      objList.add(do1);
      objList.add(do2);
      
      de1.setDescription("This is a test!");
      de1.setDependencies(objList.iterator());
      de1.setAncestors(elemList.iterator());
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = de1.toXml(doc);
      PSDeployableElement tgtEl = new PSDeployableElement(el);
      assertEquals(de1, tgtEl);
      tgtEl.copyFrom(de1);
      assertEquals(de1, tgtEl);
      
      Iterator deps = de1.getDependencies(PSDependency.TYPE_SHARED);
      assertTrue(deps.hasNext());
      assertEquals(do2, deps.next());

      File folder = tempFolder.newFolder("rx_resources","ewebeditpro");
      File file = new File(folder.getAbsolutePath() + "config.xml");
      do1.setDependencies(PSIteratorUtils.emptyIterator());
      PSUserDependency userDep1 = do1.addUserDependency(file);
      el = userDep1.toXml(doc);
      PSUserDependency userDep2 = new PSUserDependency(el);
      assertEquals(userDep1, userDep2);
      
      PSDeployableObject do3 = new PSDeployableObject(
         PSDependency.TYPE_SYSTEM, "3", "TestObj3", "Test Object3", 
         "myTestObject3", true, false, false);
      
      PSDeployableObject do4 = new PSDeployableObject(
         PSDependency.TYPE_SERVER, "4", "TestObj4", "Test Object4", 
         "myTestObject4", true, false, false);
      
      assertTrue(!do1.canBeIncludedExcluded());
      assertTrue(do2.canBeIncludedExcluded());
      assertTrue(!do3.canBeIncludedExcluded());
      assertTrue(!do4.canBeIncludedExcluded());
      
      assertTrue(do1.isIncluded());
      assertTrue(!do2.isIncluded());
      assertTrue(!do3.isIncluded());
      assertTrue(!do4.isIncluded());
            
      assertTrue(do1.shouldAutoExpand());
      do2.copyFrom(do1);
      assertEquals(do1, do2);
      assertTrue(do2.shouldAutoExpand());
      do1.setShouldAutoExpand(false);
      assertTrue(!do1.shouldAutoExpand());
      assertTrue(!do1.equals(do2));
      do2.copyFrom(do1);
      assertEquals(do1, do2);
      doc = PSXmlDocumentBuilder.createXmlDocument();
      do2 = new PSDeployableObject(do1.toXml(doc));
      System.out.println(PSXmlDocumentBuilder.toString(do1.toXml(doc)));
      System.out.println(PSXmlDocumentBuilder.toString(do2.toXml(doc)));
      assertTrue(!do2.shouldAutoExpand());
      assertEquals(do1, do2);
   }
}
