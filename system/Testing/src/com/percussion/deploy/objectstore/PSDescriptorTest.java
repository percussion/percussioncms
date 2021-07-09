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
 
package com.percussion.deploy.objectstore;

import com.percussion.utils.testing.UnitTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the <code>PSDescriptor</code> objects.
 */
@Category(UnitTest.class)
public class PSDescriptorTest
{
   public PSDescriptorTest()
   {

   }
   
   /**
    * Test the export descriptor
    * 
    * @throws Exception if there are any errors
    */
   @Test
   public void testExport() throws Exception
   {
      PSExportDescriptor desc = getExportDescriptor(false);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      assertEquals(desc, new PSExportDescriptor(desc.toXml(doc)));
      
      desc = getExportDescriptor(true);
      assertEquals(desc, new PSExportDescriptor(desc.toXml(doc)));
      
   }
   
   /**
    * Test the import descriptor
    * 
    * @throws Exception if there are any errors
    */
   @Test
   public void testImport() throws Exception
   {
      PSArchiveInfo info = PSArchiveInfoTest.getArchiveInfo(true);
      PSImportDescriptor desc = new PSImportDescriptor(info);
      List pkgList = desc.getImportPackageList();
      
      assertTrue(pkgList.isEmpty());   
      if(info.getArchiveDetail().getExportDescriptor().getPackages().hasNext())
      {   
         Iterator packages = info.getArchiveDetail().
            getExportDescriptor().getPackages();
         PSDeployableElement element = (PSDeployableElement)packages.next();
         pkgList.add(new PSImportPackage(element));
         assertTrue(desc.isPackageIncluded(element));
      }

      PSDeployableElement de1 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "foo", "foo", "bar", 
         "bar", true, false, false);
      assertTrue(!desc.isPackageIncluded(de1));   
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      assertEquals(desc, new PSImportDescriptor(desc.toXml(doc)));
   }
   
   /**
    * Construct an export descriptor
    * 
    * @param full <code>true</code> to include modified and missing package
    * lists, <code>false</code> otherwise.
    * 
    * @return The descriptor, never <code>null</code>.
    */
   public static PSExportDescriptor getExportDescriptor(boolean full)
   {
      PSExportDescriptor desc = new PSExportDescriptor("test");
      desc.setDescription("this is a test");
      PSDeployableElement de1 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "1", "TestElem", "Test Element", 
         "myTestElement", true, false, false);
         
      PSDeployableElement de2 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "2", "TestElem2", "Test Element2", 
         "myTestElement2", true, false, false);
      PSDeployableElement de3 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "3", "TestElem3", "Test Element3", 
         "myTestElement3", true, false, true);

      List pkgs = new ArrayList(3);
      pkgs.add(de1);      
      pkgs.add(de2);      
      pkgs.add(de3);      
      desc.setPackages(pkgs.iterator());

      if (full)
      {
         List names = new ArrayList();
         names.add("de2");
         names.add("de3");
         desc.setModifiedPackages(names.iterator());
         
         names.clear();
         names.add("de4");
         desc.setMissingPackages(names.iterator());
      }      
      
      return desc;
   }
   
}
