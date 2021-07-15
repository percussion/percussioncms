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
 
package com.percussion.services.publisher.data;

import com.percussion.services.guidmgr.data.PSGuid;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the {@link PSEditionTaskDef} object.
 */
@Category(IntegrationTest.class)
public class PSEditionTaskDefTest
{
   /**
    * Test equals and hashcode
    * 
    * @throws Exception if the test fails.
    */
   @Test
   public void testEquals() throws Exception
   {
      // testing with PSEditionTaskDef(IPSGuid, IPSGuid), no parameters
      PSEditionTaskDef task1 = new PSEditionTaskDef(new PSGuid("0-115-101"), 
            new PSGuid("0-110-301"));
      PSEditionTaskDef task2 = new PSEditionTaskDef(new PSGuid("0-115-101"),
            new PSGuid("0-110-301"));
      assertEquals(task1, task2);
      assertEquals(task1.hashCode(), task2.hashCode());
      
      PSEditionTaskDef task3 = new PSEditionTaskDef(new PSGuid("0-115-102"),
            new PSGuid("0-110-301"));
      assertTrue(!task1.equals(task3));
      
      // testing with extension name
      task1.setExtensionName(
            "Java/global/percussion/system/sys_editionCommandTask");
      task2.setExtensionName(
            "Java/global/percussion/system/sys_editionCommandTask");
      assertEquals(task1, task2);
      assertEquals(task1.hashCode(), task2.hashCode());
         
      // testing with one parameter
      task1.setParam("name", "value");
      task2.setParam("name", "value");
      assertEquals(task1, task2);
      assertEquals(task1.hashCode(), task2.hashCode());
      
      // testing with multiple parameters
      task1.setParam("name2", "value2");
      task2.setParam("name2", "value2");
      assertEquals(task1, task2);
      assertEquals(task1.hashCode(), task2.hashCode());
      
      task2.setParam("name2", "value3");
      assertTrue(!task1.equals(task2));
   }
   
   /**
    * Test the xml serialization
    * 
    * @throws Exception if there are any errors.
    */
   @Test
   public void testXml() throws Exception
   {
      // testing with no parameters
      PSEditionTaskDef task1 = new PSEditionTaskDef(new PSGuid("0-115-101"),
            new PSGuid("0-110-301"));
      task1.setContinueOnFailure(false);
      task1.setExtensionName(
            "Java/global/percussion/system/sys_editionCommandTask");
      task1.setSequence(1);
      task1.setVersion(new Integer(0));
      
      String str = task1.toXML();
      PSEditionTaskDef task2 = new PSEditionTaskDef(new PSGuid("0-115-101"),
            new PSGuid("0-110-301"));
      assertTrue(!task1.equals(task2));
      task2.fromXML(str);
      assertTrue(task1.equals(task2));
      
      // testing with multiple parameters
      task1.setParam("command", "echo Hello");
      task1.setParam("command2", "echo GoodBye");
      task2 = new PSEditionTaskDef(new PSGuid("0-115-101"),
            new PSGuid("0-110-301"));
      assertTrue(!task1.equals(task2));
      str = task1.toXML();
      task2.fromXML(str);
      assertTrue(task1.equals(task2));
   }
}
