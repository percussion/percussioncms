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

import junit.framework.TestCase;

/**
 * Test legacy guids
 * 
 * @author dougrand
 */
public class PSLegacyGuidTest extends TestCase
{
   public void testLongValueRoundtrip()
   {
      final PSLegacyGuid guid = new PSLegacyGuid(101, 15, 12345);
      final long value = guid.longValue(); 
      assertEquals(value, new PSLegacyGuid(value).longValue());
   }

   public void testChildId()
   {
      PSLegacyGuid guid = new PSLegacyGuid(101, 15, 12345);
      assertTrue(guid.isChildGuid());
      assertEquals(101, guid.getContentTypeId());
      assertEquals(15, guid.getChildId());
      assertEquals(12345, guid.getUUID());
      assertEquals(12345, guid.getContentId());
   }
   
   public void testContentId()
   {
      PSLegacyGuid guid = new PSLegacyGuid(12345, 2);
      assertEquals(12345, guid.getUUID());
      assertEquals(12345, guid.getContentId());
      assertEquals(2, guid.getRevision());
      
      guid = new PSLegacyGuid(1, -1);
      assertEquals(-1, guid.getRevision());
      assertEquals(PSTypeEnum.LEGACY_CONTENT.getOrdinal(), guid.getType());
      
      guid = new PSLegacyGuid(-1, -1);
      assertEquals(-1, guid.getContentId());
      assertEquals(-1, guid.getRevision());
      assertEquals(PSTypeEnum.LEGACY_CONTENT.getOrdinal(), guid.getType());

      guid = new PSLegacyGuid(-0xff, -1);
      assertEquals(-0xff, guid.getContentId());
      assertEquals(-1, guid.getRevision());
      assertEquals(PSTypeEnum.LEGACY_CONTENT.getOrdinal(), guid.getType());
   }
   
   public void testStringVsGuid()
   {
      // test PSTypeEnum.LEGACY_CONTENT type
      PSLegacyGuid guid = new PSLegacyGuid(12345, 2);
      PSLegacyGuid guid_2 = new PSLegacyGuid(guid.toString());
      
      assertEquals(guid, guid_2);
      
      guid = new PSLegacyGuid(12345, -1);
      guid_2 = new PSLegacyGuid(guid.toString());
      
      assertEquals(guid, guid_2);
      
      // test PSTypeEnum.LEGACY_CHILD type
      guid = new PSLegacyGuid(101, 15, 12345);
      guid_2 = new PSLegacyGuid(guid.toString());
      
      assertEquals(guid, guid_2);    
      
      // test from PSGuid to PSLegacyGuid
      guid = new PSLegacyGuid(12345, 2);
      guid_2 = new PSLegacyGuid( new PSGuid(guid.toString()));
      
      assertEquals(guid, guid_2);
      
      guid = new PSLegacyGuid(101, 15, 12345);
      guid_2 = new PSLegacyGuid( new PSGuid(guid.toString()));
      assertEquals(guid, guid_2);
   }
}
