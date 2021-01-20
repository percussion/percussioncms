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

import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Unit test for the <code>PSDescriptor</code> objects.
 */
public class PSDescriptorTest extends TestCase
{
   /**
    * Construct this unit test
    * 
    * @param name The name of this test.
    */
   public PSDescriptorTest(String name)
   {
      super(name);
   }
   
   /**
    * Test the export descriptor
    * 
    * @throws Exception if there are any errors
    */
   public void testExport() throws Exception
   {
      PSExportDescriptor desc = getExportDescriptor(false);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element elem = desc.toXml(doc);
      PSExportDescriptor desc2 = new PSExportDescriptor(elem);
      
      assertEquals(desc, desc2);
      
      desc = getExportDescriptor(true);
      assertEquals(desc, new PSExportDescriptor(desc.toXml(doc)));
      
   }
   
   /**
    * Test the export descriptor
    * 
    * @throws Exception if there are any errors
    */
   public void testPkgDepList() throws Exception
   {
      ArrayList<Map<String, String>> pkgDepList1 = 
         new ArrayList<Map<String, String>>();
      
      ArrayList<Map<String, String>> pkgDepList2 = 
         new ArrayList<Map<String, String>>();
      
      PSExportDescriptor desc = getExportDescriptor(false);
      
      //Create package dependencies
      Map<String,String> pkgDepMap1 = new HashMap<String,String>();
      pkgDepMap1.put(PSDescriptor.XML_PKG_DEP_NAME, "PKGNAME1");
      pkgDepMap1.put(PSDescriptor.XML_PKG_DEP_VERSION, "1.0.0");      
      pkgDepMap1.put(PSDescriptor.XML_PKG_DEP_IMPLIED, Boolean.toString(true));
      
      Map<String,String> pkgDepMap2 = new HashMap<String,String>();
      pkgDepMap2.put(PSDescriptor.XML_PKG_DEP_NAME, "PKGNAME2");
      pkgDepMap2.put(PSDescriptor.XML_PKG_DEP_VERSION, "1.0.0");     
      pkgDepMap2.put(PSDescriptor.XML_PKG_DEP_IMPLIED, Boolean.toString(false));
           
      ArrayList<Map<String, String>> pkgDepList = 
         new ArrayList<Map<String, String>>();     
      
      pkgDepList.add(pkgDepMap1); 
      pkgDepList.add(pkgDepMap2);
      
      assertEquals(desc.getPkgDepList(), pkgDepList);     
   }
   
   /**
    * Test the import descriptor
    * 
    * @throws Exception if there are any errors
    */
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
      
      //Create package dependenies
      desc.setPkgDep("PKGNAME1", "1.0.0", true);
      desc.setPkgDep("PKGNAME2", "1.0.0", false);

      //Add Deployable Elements
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
   
   /**
    * Test the <code>PSDescriptor</code> fomatVersion method.
    * @throws Exception on any error.
    */
   public void testFormatVersion() throws Exception
   {
      /*
      assertTrue(
         PSDescriptor.formatVersion("1", false, false).equals("1.0.0"));
      assertTrue(
         PSDescriptor.formatVersion("1.2", false, false).equals("1.2.0"));
      assertTrue(
         PSDescriptor.formatVersion("1.0.2", false, false).equals("1.0.2"));
      assertTrue(
         PSDescriptor.formatVersion("1.0.2.extra", false, false).equals("1.0.2"));
      assertTrue(
         PSDescriptor.formatVersion("1.0.2.extra", true, false).equals("1.0.2.extra"));
      assertTrue(
         PSDescriptor.formatVersion("1.2.extra", false, false).equals("1.2.0"));
      assertTrue(
         PSDescriptor.formatVersion("foo", false, false).equals(""));
      assertTrue(
         PSDescriptor.formatVersion("extra.1.2.1", false, false).equals(""));
      assertTrue(
         PSDescriptor.formatVersion(null, false, false).equals(""));
      assertTrue(
         PSDescriptor.formatVersion("1.0.2.extra", true, true).equals("1.0.2.extra"));
      */
      try
      {
          PSDescriptor.formatVersion("1.2.extra", false, true).equals("1.2.0");
          fail();
      }
      catch(RuntimeException ignore){}
      try
      {
         PSDescriptor.formatVersion("foo", false, true).equals("");
          fail();
      }
      catch(RuntimeException ignore){}
      try
      {
         PSDescriptor.formatVersion("extra.1.2.1", false, true).equals("");
          fail();
      }
      catch(RuntimeException ignore){}
          
      
   }
   
   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      //suite.addTest(new PSDescriptorTest("testExport"));
      //suite.addTest(new PSDescriptorTest("testImport"));
      suite.addTest(new PSDescriptorTest("testFormatVersion"));
      return suite;
   }
   
}
