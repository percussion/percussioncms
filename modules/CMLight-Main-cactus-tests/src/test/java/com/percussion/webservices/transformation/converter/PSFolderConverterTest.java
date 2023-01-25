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
package com.percussion.webservices.transformation.converter;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSObjectAcl;
import com.percussion.cms.objectstore.PSObjectAclEntry;
import com.percussion.cms.objectstore.PSObjectPermissions;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.transformation.PSTransformationException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

/**
 * Unit tests for the {@link PSActionConverter} class.
 */
@Category(IntegrationTest.class)
public class PSFolderConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object and vice versa.
    */
   public void testConversion() throws Exception
   {
      // test with simple folder
      PSFolder folder = createFolder(10, "folder1", -1, 100);
      roundTripConvertion(folder);
      
      folder = createFolder(11, "folder2", 2, 101);
      roundTripConvertion(folder);
   }

   /**
    * Test a list of server object convert to client array, and vice versa.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testListToArray() throws Exception
   {
      // test simple action objects
      List<PSFolder> srcList = new ArrayList<PSFolder>();
      srcList.add(createFolder(11, "folder1", -1, 100));
      srcList.add(createFolder(12, "folder2", 1, 101));
      
      List<PSAction> srcList2 = roundTripListConversion(
            com.percussion.webservices.content.PSFolder[].class, srcList);

      assertTrue(srcList.equals(srcList2));
   }

   private PSFolder createFolder(int id, String name, int communityId,
         int displayFormatId)
   {
      PSFolder f = new PSFolder(name, id, communityId, 
            PSObjectPermissions.ACCESS_ADMIN, name);
      
      f.setFolderPath("//Folders/Test/" + name);
      f.setDisplayFormatId(displayFormatId);
      f.setDisplayFormatName(name + " display format");
      if (communityId != -1)
         f.setCommunityName(name + " community");
      
      PSObjectAclEntry aclEntry;
      PSObjectAcl tgtAcls = new PSObjectAcl();

      aclEntry = new PSObjectAclEntry(PSObjectAclEntry.ACL_ENTRY_TYPE_USER, 
            "User " + name, PSObjectAclEntry.ACCESS_READ);
      tgtAcls.add(aclEntry);
      aclEntry = new PSObjectAclEntry(PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE, 
            "Admin " + name, PSObjectAclEntry.ACCESS_ADMIN);
      tgtAcls.add(aclEntry);
      f.setAcl(tgtAcls);

      return f;
   }
   
   @SuppressWarnings("unused")
   private void roundTripConvertion(PSFolder source) throws PSTransformationException
   {
      PSFolder target = (PSFolder) roundTripConversion(PSFolder.class,
            com.percussion.webservices.content.PSFolder.class, source);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      //System.out.println(PSXmlDocumentBuilder.toString(source.toXml(doc)));
      //System.out.println(PSXmlDocumentBuilder.toString(target.toXml(doc)));

      // verify the the round-trip object is equal to the source object
      assertTrue(source.equalsFull(target));
   }

}

