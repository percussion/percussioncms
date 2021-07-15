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

package com.percussion.services.filestorage.impl;

import com.percussion.services.filestorage.IPSFileStorageService;
import com.percussion.services.filestorage.IPSHashedFieldCataloger;
import com.percussion.services.filestorage.PSFileStorageServiceLocator;
import com.percussion.services.filestorage.PSHashedFieldCatalogerLocator;
import com.percussion.services.filestorage.data.PSHashedColumn;

import java.util.HashSet;
import java.util.Set;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSHashedFieldCatalogerTest extends ServletTestCase
{
   
   IPSFileStorageService storageService;
   
   @Test
   public void testGetHashedFileFields()
   {
      IPSFileStorageService src = PSFileStorageServiceLocator.getFileStorageService();
      IPSHashedFieldCataloger cataloger = PSHashedFieldCatalogerLocator.getHashedFileCatalogerService();
      
      
      Set<PSHashedColumn> hashedColumns = cataloger.getServerHashedColumns();
      System.out.println(hashedColumns);
      Set<PSHashedColumn> testSet = new HashSet<PSHashedColumn>();
      testSet.add(new PSHashedColumn("img1_hash","RXS_CT_SHAREDIMAGE","IMG1_HASH"));
      testSet.add(new PSHashedColumn("img2_hash","RXS_CT_SHAREDIMAGE","IMG2_HASH"));
      testSet.add(new PSHashedColumn("activeimg_hash","RXS_CT_NAVIGATIONIMAGETRIPLE","ACTIVEIMG_HASH"));
      testSet.add(new PSHashedColumn("inactiveimg_hash","RXS_CT_NAVIGATIONIMAGETRIPLE","INACTIVEIMG_HASH"));
      testSet.add(new PSHashedColumn("rolloverimg_hash","RXS_CT_NAVIGATIONIMAGETRIPLE","ROLLOVERIMG_HASH"));
      testSet.add(new PSHashedColumn("item_file_attachement_hash","RXS_CT_SHAREDBINARY","ITEM_FILE_ATTACHMENT_HASH"));
      
      src.getText("111");
      
      assertTrue(CollectionUtils.isEqualCollection(testSet, hashedColumns));
   }

   @Test
   public void testStoreHashedFileFields()
   {
      Set<PSHashedColumn> testSet = new HashSet<PSHashedColumn>();
      testSet.add(new PSHashedColumn("img1_hash","RXS_CT_SHAREDIMAGE","IMG1_HASH"));
      testSet.add(new PSHashedColumn("img2_hash","RXS_CT_SHAREDIMAGE","IMG2_HASH"));
      testSet.add(new PSHashedColumn("activeimg_hash","RXS_CT_NAVIGATIONIMAGETRIPLE","ACTIVEIMG_HASH"));
      testSet.add(new PSHashedColumn("inactiveimg_hash","RXS_CT_NAVIGATIONIMAGETRIPLE","INACTIVEIMG_HASH"));
      testSet.add(new PSHashedColumn("rolloverimg_hash","RXS_CT_NAVIGATIONIMAGETRIPLE","ROLLOVERIMG_HASH"));
      testSet.add(new PSHashedColumn("item_file_attachement_hash","RXS_CT_SHAREDBINARY","ITEM_FILE_ATTACHMENT_HASH"));
      
      IPSHashedFieldCataloger service = PSHashedFieldCatalogerLocator.getHashedFileCatalogerService();
   
      service.storeColumns(testSet);
      Set<PSHashedColumn> storedColumns = service.getStoredColumns();
      
      assertTrue(CollectionUtils.isEqualCollection(testSet,storedColumns));
      
   }
   @Test
   public void testValidateFields()
   {
      IPSHashedFieldCataloger service = PSHashedFieldCatalogerLocator.getHashedFileCatalogerService();
      Set<PSHashedColumn> storedColumns = service.validateColumns();
      System.out.println("Stored columns size "+storedColumns.size());
      for(PSHashedColumn column : storedColumns) {
         System.out.println("Column "+column);
         assertTrue(column.isColumnExists());
      }
   }

   
  
   

   public void validateNewField()
   {

   }
   


}
