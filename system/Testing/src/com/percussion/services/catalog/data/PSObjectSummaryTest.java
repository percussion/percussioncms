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
package com.percussion.services.catalog.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.data.PSUserAccessLevel;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import org.junit.Ignore;
import org.junit.Test;

import java.security.SecureRandom;
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
   private static SecureRandom ms_rand = new SecureRandom();

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
