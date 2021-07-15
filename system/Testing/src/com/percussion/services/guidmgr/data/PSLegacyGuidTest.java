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
