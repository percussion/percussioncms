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
package com.percussion.services.guidmgr.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;

import junit.framework.TestCase;

/**
 * Test the guid class
 * @author dougrand
 */
public class PSGuidTest extends TestCase
{
   static IPSGuid NG = new PSGuid(PSTypeEnum.NODEDEF,0x0000010200000003L);

   static IPSGuid MG = new PSGuid(PSTypeEnum.INVALID,0x1248ACFF12484210L);

   /**
    * 
    */
   public void testCtors()
   {
      long hostid = 12;
      long uuid = 24;
      PSTypeEnum type = PSTypeEnum.ITEM;
      
      IPSGuid guid = new PSGuid(hostid, type, uuid);
      assertEquals(hostid, guid.getHostId());
      assertEquals(type.getOrdinal(), guid.getType());
      assertEquals(uuid, guid.getUUID());
      
      IPSGuid guid_2 = new PSGuid(guid.toString());
      assertEquals(guid, guid_2);
      
      IPSGuid guid_3 = new PSDesignGuid(guid.longValue());
      assertEquals(guid, guid_3);
      
      IPSGuid guid_4 = new PSGuid(type, uuid);
      assertEquals(type.getOrdinal(), guid_4.getType());
      assertEquals(uuid, guid_4.getUUID());
      
      IPSGuid guid_5 = new PSGuid(guid_4.toString());
      assertEquals(guid_5, guid_4);
      
      IPSGuid guid_6 = new PSGuid(type, guid_4.toStringUntyped());
      assertEquals(guid_6, guid_4);
      
      // test with negative full guid
      IPSGuid guid_7 = new PSGuid(PSTypeEnum.ITEM_FILTER,
            "-2878460938313269246");
      assertTrue(guid_7 != null);

      try
      {
         new PSGuid(type, guid_4.toString());
         assertFalse("Should have thrown exception", false);
      }
      catch(Exception e)
      {
         // OK, should throw exception
      }

      try
      {
         new PSGuid(type, guid_4.longValue());
         assertFalse("Should have thrown exception", false);
      }
      catch(Exception e)
      {
         // OK, should throw exception
      }

      try
      {
         new PSGuid(PSTypeEnum.INVALID, 0x0000010200000003L);
         assertFalse("Should have thrown exception", false);
      }
      catch(Exception e)
      {
         // OK, should throw exception
      }
   }
   
   /**
    * 
    */
   public void testLongValue()
   {
      IPSGuid tv = new PSGuid(PSTypeEnum.ITEM, 1);
      IPSGuid tv2 = new PSGuid(1, PSTypeEnum.ITEM, 1);
      
      assertEquals(0x01L, tv.longValue());
      assertEquals(0x0000010100000001L, tv2.longValue());
   }
   
   /**
    * 
    */
   public void testAccessors()
   {
      long siteid = NG.getHostId();
      int type = NG.getType();
      long uuid = NG.getUUID();

      assertEquals(1, siteid);
      assertEquals(PSTypeEnum.NODEDEF.getOrdinal(), type);
      assertEquals(3, uuid);

      siteid = MG.getHostId();
      type = MG.getType();
      uuid = MG.getUUID();

      assertEquals(0x1248AC, siteid);
      assertEquals(PSTypeEnum.INVALID.getOrdinal(), type);
      assertEquals(0x12484210L, uuid);
   }

   /**
    * 
    */
   public void testToString()
   {
      String str = NG.toString();
      assertEquals("1-2-3", str);
   }

   /**
    * 
    */
   public void testToStringNoType()
   {
      String str = NG.toStringUntyped();
      assertEquals("1-3", str);
   }

   /**
    * 
    */
   public void testAssemble()
   {
      IPSGuid guid = new PSGuid(1, PSTypeEnum.NODEDEF, 3);
      assertEquals(NG, guid);
   }

   /**
    * 
    */
   public void testFromString()
   {
      IPSGuid guid = new PSGuid(PSTypeEnum.NODEDEF, "1-2-3");
      assertEquals(NG, guid);
      
      guid = new PSGuid("1-2-3");
      assertEquals(NG, guid);
   }

   /**
    * 
    */
   public void testFromStringNoType()
   {
      IPSGuid guid = new PSGuid(PSTypeEnum.NODEDEF, "1-3");
      assertEquals(NG, guid);
      
      try
      {
         guid = new PSGuid("1-3");
         assertTrue("Expected exception", false);
      }
      catch(Exception e)
      {
         // OK
      }
   }
   
   /**
    * test a valid guid: negative number, +ve number, and an actual guid type
    *
    */
   public void testIsValid()
   {
      String val = "-2";
      assertFalse(val + " is valid", PSGuid.isValid(PSTypeEnum.TEMPLATE, val));
      val = "301";
      assertTrue(val + " is valid", PSGuid.isValid(PSTypeEnum.TEMPLATE, val));
      val = "39759470036779009";
      assertTrue(val + " is valid", PSGuid.isValid(PSTypeEnum.ITEM_FILTER, val));
   }
}
