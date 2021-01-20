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
package com.percussion.services.pkginfo.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test case for the {@link PSIdName} class
 */
@Category(IntegrationTest.class)
public class PSIdNameTest
{
   /**
    * Tests the constructor, and get/set methods.
    *
    */
   @Test
   public void testCtorGetSet() {
      String id = (new PSGuid(PSTypeEnum.INTERNAL, 301)).toString();
      String name = "foo";
      
      // test valid args
      PSIdName idName = new PSIdName(id, name);
      assertEquals(id, idName.getId());
      assertEquals(name, idName.getName());
      assertEquals(PSTypeEnum.INTERNAL, idName.getType());
      
      // test invalid id
      try
      {
         idName = new PSIdName(null, name);
      }
      catch (IllegalArgumentException e)
      {
         // correct
      }
      
      try
      {
         idName = new PSIdName("", name);
      }
      catch (IllegalArgumentException e)
      {
         // correct
      }
      
      assertEquals(idName.getId(), id);
      
      // test invalid name
      try
      {
         idName = new PSIdName(id, null);
      }
      catch (IllegalArgumentException e)
      {
         // correct
      }
      
      try
      {
         idName = new PSIdName(id, "");
      }
      catch (IllegalArgumentException e)
      {
         // correct
      }
      
      assertEquals(idName.getName(), name);
      
      // test set methods
      String id2 = (new PSGuid(PSTypeEnum.INTERNAL, 302)).toString();
      idName.setId(id2);
      assertEquals(idName.getId(), id2);
      
      try
      {
         idName.setId(null);
      }
      catch (IllegalArgumentException e)
      {
         // correct
      }
      
      try
      {
         idName.setId("");
      }
      catch (IllegalArgumentException e)
      {
         // correct
      }
      
      assertEquals(idName.getId(), id2);
      
      String name2 = "foo2";
      idName.setName(name2);
      assertEquals(idName.getName(), name2);
      
      try
      {
         idName.setName(null);
      }
      catch (IllegalArgumentException e)
      {
         // correct
      }
      
      try
      {
         idName.setName("");
      }
      catch (IllegalArgumentException e)
      {
         // correct
      }
      
      assertEquals(idName.getName(), name2);
   }
   
   /**
    * Tests the equals and hashcode methods.
    * 
    * @throws Exception if the test fails
    */
   public void testEquals() throws Exception
   {
      PSIdName idName1 = new PSIdName(
            (new PSGuid(PSTypeEnum.INTERNAL, 301)).toString(), "foo");
      PSIdName idName2 = new PSIdName(
            (new PSGuid(PSTypeEnum.INTERNAL, 301)).toString(), "foo");
      assertEquals(idName1, idName2);
      assertEquals(idName1.hashCode(), idName2.hashCode());
      
      idName1.setId((new PSGuid(PSTypeEnum.INTERNAL, 302)).toString());
      assertFalse(idName1.equals(idName2));
      assertFalse(idName1.hashCode() == idName2.hashCode());
      
      idName2.setId(idName1.getId());
      assertEquals(idName1, idName2);
      assertEquals(idName1.hashCode(), idName2.hashCode());
      
      idName1.setName("foo2");
      assertFalse(idName1.equals(idName2));
      
      idName2.setName("foo2");
      assertEquals(idName1, idName2);
      assertEquals(idName1.hashCode(), idName2.hashCode());
   }
}

