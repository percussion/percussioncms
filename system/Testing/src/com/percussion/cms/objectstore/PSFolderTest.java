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

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.xml.PSXmlDocumentBuilder;

/**
 * Unit test class for the <code>PSFolder</code> class.
 */
public class PSFolderTest extends TestCase
{
   /**
    * Tests the equals and to/from XML methods
    *
    * @throws Exception if there are any errors.
    */
   public void testAll() throws Exception
   {
      PSFolder folder = new PSFolder("f1", 10, -1,
         PSObjectPermissions.ACCESS_ADMIN, "description");
      folder.setProperty("p1", "value1");
      folder.setProperty("p2", "value2", "desc2");

      // Testing to/from XML
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element folderEl = folder.toXml(doc);
      //System.out.println("\n" + PSXmlDocumentBuilder.toString(folderEl));


      PSFolder targetFolder = new PSFolder(folderEl);

      doc = PSXmlDocumentBuilder.createXmlDocument();
      folderEl = targetFolder.toXml(doc);
      //System.out.println("\n" + PSXmlDocumentBuilder.toString(folderEl));

      assertTrue(folder.equals(targetFolder));

      // Testing clone
      PSFolder clone = (PSFolder) folder.clone();
      PSFolder fullClone = (PSFolder) folder.cloneFull();

      assertTrue(!folder.equals(clone));
      assertTrue(folder.equalsFull(fullClone));
      assertTrue(! folder.equalsFull(clone));

      // Testing empty list of properties
      PSFolder emptyFolder = new PSFolder("f1", 10, -1,
         PSObjectPermissions.ACCESS_ADMIN, "description");
      doc = PSXmlDocumentBuilder.createXmlDocument();
      folderEl = emptyFolder.toXml(doc);
      targetFolder = new PSFolder(folderEl);

      assertTrue(emptyFolder.equals(targetFolder));

   }

   /**
    * Tests the various state
    *
    * @throws Exception if there are any errors.
    */
   public void testState() throws Exception
   {
      PSFolder folder = new PSFolder("f1", 10, -1,
         PSObjectPermissions.ACCESS_ADMIN, "description");

      folder.setProperty("name1", "v1", "d1");

      folder.setState(IPSDbComponent.DBSTATE_UNMODIFIED);

      // the state of the property is MODIFIED, so is the folder's
      assertTrue(folder.getState() == IPSDbComponent.DBSTATE_MODIFIED);
   }
   
   /**
    * Tests the merge method.
    * 
    * @throws Exception
    */
   public void testMerge() throws Exception
   {
      // tests merge properties
      
      // create a persisted folder with persisted properties
      PSFolder origFolder = new PSFolder("f1", 10, -1,
            PSObjectPermissions.ACCESS_ADMIN, "description");
      // set properties
      PSFolderProperty prop = new PSFolderProperty(1, "p1", "value1", "");
      origFolder.addProperty(prop);
      prop = new PSFolderProperty(2, "p2", "value2", "desc2");
      origFolder.addProperty(prop);
      // set ACL entries
      PSObjectAcl acl = new PSObjectAcl();
      PSObjectAclEntry entry = new PSObjectAclEntry(1, 
            PSObjectAclEntry.ACL_ENTRY_TYPE_USER, "qa1", 
            PSObjectAclEntry.ACCESS_READ);
      acl.add(entry);
      entry = new PSObjectAclEntry(1, 
            PSObjectAclEntry.ACL_ENTRY_TYPE_USER, "admin1", 
            PSObjectAclEntry.ACCESS_ADMIN);
      acl.add(entry);
      origFolder.setAcl(acl);
      
      origFolder.setPersisted();
      
      // create a new folder from the original folder
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSFolder folder = new PSFolder(origFolder.toXml(doc));
      
      // create a folder with new state and properties and ACL entries
      PSFolder srcFolder = new PSFolder("f1", 10, -1,
            PSObjectPermissions.ACCESS_ADMIN, "description");
      // create new properties
      srcFolder.setProperty("p1", "modifiedValue1");
      srcFolder.setProperty("newProperty", "newValue");
      // create new ACL entries
      acl = new PSObjectAcl();
      entry = new PSObjectAclEntry(
            PSObjectAclEntry.ACL_ENTRY_TYPE_USER, "qa1", 
            PSObjectAclEntry.ACCESS_WRITE);
      acl.add(entry);
      entry = new PSObjectAclEntry( 
            PSObjectAclEntry.ACL_ENTRY_TYPE_USER, "admin2", 
            PSObjectAclEntry.ACCESS_ADMIN);
      acl.add(entry);
      srcFolder.setAcl(acl);

      
      // MERGE
      folder.mergeFrom(srcFolder);
      
      //\/\/\/\/\/\/\/\/\/\/\/\
      // tests merge properties
      //\/\/\/\/\/\/\/\/\/\/\/\
      
      // "p2" should be removed
      assertTrue(folder.getProperty("p2") == null);
      // "p1" should be modified
      prop = folder.getProperty("p1");
      assertTrue(prop != null);
      assertTrue(prop.getValue().equals("modifiedValue1"));
      assertTrue(prop.getState() == IPSDbComponent.DBSTATE_MODIFIED);
      // "newProperty" should be added
      prop = folder.getProperty("newProperty");
      assertTrue(prop != null);
      assertTrue(prop.getState() == IPSDbComponent.DBSTATE_NEW);

      //\/\/\/\/\/\/\/\/\/\/\/\/
      // tests merge ACL entries
      //\/\/\/\/\/\/\/\/\/\/\/\/
      
      acl = folder.getAcl();
      entry = acl.getAclEntry("qa1", PSObjectAclEntry.ACL_ENTRY_TYPE_USER);
      assertTrue(entry != null);
      assertTrue(entry.getState() == IPSDbComponent.DBSTATE_MODIFIED);
      entry = acl.getAclEntry("admin2", PSObjectAclEntry.ACL_ENTRY_TYPE_USER);
      assertTrue(entry.getState() == IPSDbComponent.DBSTATE_NEW);
      
      // "admin1" should be in the deleted list
      entry = acl.getAclEntry("admin1", PSObjectAclEntry.ACL_ENTRY_TYPE_USER);
      assertTrue(entry == null);
      assertTrue(acl.getDeleteCollection().size() == 1);
   }

   /**
    * Testing ComponentSummary class
    *
    * @throws Exception an error occurs.
    */
   public void testComponentSummary() throws Exception
   {
      PSComponentSummary compSummary = new PSComponentSummary(10, 1, 1, 1,
         PSComponentSummary.TYPE_FOLDER, "folder1", -1,
         PSObjectPermissions.ACCESS_ADMIN);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element compEl = compSummary.toXml(doc);
      //System.out.println("\n" + PSXmlDocumentBuilder.toString(compEl));

      PSComponentSummary target = new PSComponentSummary(compEl);
      //System.out.println("\n" + PSXmlDocumentBuilder.toString(compEl));

      assertTrue(target.equals(compSummary));
   }
   
   /**
    * Test if folders with only different locators have
    * different hashcodes, as they should. Also tests the use case of
    * putting several folders in a Set.
    * 
    * @throws Exception on error
    */
   public void testFolderHashCodeAndEquals() throws Exception
   {
      PSFolder folder1 = new PSFolder("f1", 10, -1,
         PSObjectPermissions.ACCESS_ADMIN, "description");
      folder1.setProperty("p1", "value1");
      folder1.setProperty("p2", "value2", "desc2");
      folder1.setLocator(new PSLocator(1, -1));
      PSFolder folder2 = new PSFolder("f1", 10, -1,
         PSObjectPermissions.ACCESS_ADMIN, "description");
      folder2.setProperty("p1", "value1");
      folder2.setProperty("p2", "value2", "desc2");
      folder2.setLocator(new PSLocator(2, -1));
      PSFolder folder3 = new PSFolder("f1", 10, -1,
         PSObjectPermissions.ACCESS_ADMIN, "description");
      folder3.setProperty("p1", "value1");
      folder3.setProperty("p2", "value2", "desc2");
      folder3.setLocator(new PSLocator(2, -1));
      assertTrue(folder1.hashCode() != folder2.hashCode());
      assertTrue(!folder1.equals(folder2));
      assertTrue(folder2.equals(folder3));
      
      // Test putting folders in a set, the set should have only
      // 2 entries as 2 of the three folders should be equal
      final Set<PSFolder> folderSet = new HashSet<PSFolder>();
      folderSet.add(folder1);
      folderSet.add(folder2);
      folderSet.add(folder3);
      
      assertTrue(folderSet.size() == 2);
   }
   
   /**
    * Tests the merge acl method.
    * 
    * @throws Exception
    */
   public void testMergeAclFrom() throws Exception
   {
      // create a persisted folder
      PSFolder origFolder = new PSFolder("f1", 10, -1,
            PSObjectPermissions.ACCESS_ADMIN, "description");
      
      // set ACL entries
      PSObjectAcl acl = new PSObjectAcl();
      PSObjectAclEntry entry = new PSObjectAclEntry(1, 
            PSObjectAclEntry.ACL_ENTRY_TYPE_USER, "qa1", 
            PSObjectAclEntry.ACCESS_READ);
      acl.add(entry);
      entry = new PSObjectAclEntry(1, 
            PSObjectAclEntry.ACL_ENTRY_TYPE_USER, "admin1", 
            PSObjectAclEntry.ACCESS_ADMIN);
      acl.add(entry);
      origFolder.setAcl(acl);
      
      origFolder.setPersisted();
      
      // create a new folder from the original folder
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSFolder folder = new PSFolder(origFolder.toXml(doc));
      
      // create a folder with new state and properties and ACL entries
      PSFolder srcFolder = new PSFolder("f1", 10, -1,
            PSObjectPermissions.ACCESS_ADMIN, "description");
      
      // create new ACL entries
      acl = new PSObjectAcl();
      entry = new PSObjectAclEntry(
            PSObjectAclEntry.ACL_ENTRY_TYPE_USER, "qa1", 
            PSObjectAclEntry.ACCESS_WRITE);
      acl.add(entry);
      entry = new PSObjectAclEntry( 
            PSObjectAclEntry.ACL_ENTRY_TYPE_USER, "admin2", 
            PSObjectAclEntry.ACCESS_ADMIN);
      acl.add(entry);
      srcFolder.setAcl(acl);
      
      // MERGE
      folder.mergeAclFrom(srcFolder.getAcl());
      
      //\/\/\/\/\/\/\/\/\/\/\/\/
      // tests merge ACL entries
      //\/\/\/\/\/\/\/\/\/\/\/\/
      
      acl = folder.getAcl();
      entry = acl.getAclEntry("qa1", PSObjectAclEntry.ACL_ENTRY_TYPE_USER);
      assertTrue(entry != null);
      assertTrue(entry.getState() == IPSDbComponent.DBSTATE_MODIFIED);
      entry = acl.getAclEntry("admin2", PSObjectAclEntry.ACL_ENTRY_TYPE_USER);
      assertTrue(entry.getState() == IPSDbComponent.DBSTATE_NEW);
      
      // "admin1" should be in the deleted list
      entry = acl.getAclEntry("admin1", PSObjectAclEntry.ACL_ENTRY_TYPE_USER);
      assertTrue(entry == null);
      assertTrue(acl.getDeleteCollection().size() == 1);
   }
}
