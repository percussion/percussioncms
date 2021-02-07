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
package com.percussion.services.catalog.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.data.PSUserAccessLevel;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Test the object summary object, primarily test the serialization using
 * the helper
 * 
 * @author dougrand
 */
public class PSObjectSummaryTest
{
   private static Random ms_rand = new Random(System.currentTimeMillis()); 

   public PSObjectSummaryTest(){}
   /**
    * Round trip an incomplete summary as the first test
    * @throws Exception
    */
   @Test
   public void testSerialization() throws Exception
   {
      PSObjectSummary nsum = new PSObjectSummary();
      nsum.setGUID(new PSGuid(PSTypeEnum.ACL,ms_rand.nextInt(1000)));
      nsum.setName("Test object summary");
      nsum.setLabel("Test object summary label");
      String ser = PSXmlSerializationHelper.writeToXml(nsum);
      
      PSObjectSummary restore = 
         (PSObjectSummary) PSXmlSerializationHelper.readFromXML(ser);
      
      assertEquals(nsum, restore);
   }
   
   /**
    * Test fully populated object summary object
    * @throws Exception
    */
   @Test
   @Ignore ("TODO: Fix me.  Test fails on certain JRE versions / OS")
   public void testCompleteSerialization() throws Exception
   {
      PSObjectSummary nsum = new PSObjectSummary();
      nsum.setGUID(new PSGuid(PSTypeEnum.ACL,ms_rand.nextInt(1000)));
      nsum.setName("Test object summary");
      nsum.setLabel("Test object summary label");
      nsum.setLockedInfo("session_1", "orange_julius", 123456789);
      Collection<PSPermissions> permissions = new ArrayList<PSPermissions>();
      
      permissions.add(PSPermissions.RUNTIME_VISIBLE);
      permissions.add(PSPermissions.OWNER);
      
      nsum.setPermissions(new PSUserAccessLevel(permissions));
      
      String ser = PSXmlSerializationHelper.writeToXml(nsum);
      System.out.println(ser);
      System.out.println();
      PSObjectSummary restore = 
         (PSObjectSummary) PSXmlSerializationHelper.readFromXML(ser);
      System.out.println(restore);

      assertEquals("Expected to be equal", nsum, restore);
   }
}
