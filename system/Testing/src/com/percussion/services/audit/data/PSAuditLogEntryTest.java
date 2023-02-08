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
package com.percussion.services.audit.data;

import com.percussion.services.audit.data.PSAuditLogEntry.AuditTypes;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;

import java.util.Date;

import junit.framework.TestCase;

/**
 * Test case for the {@link PSAuditLogEntry} class
 */
public class PSAuditLogEntryTest extends TestCase
{
   /**
    * Tests the equals and hashcode methods.
    * 
    * @throws Exception if the test fails
    */
   public void testEquals() throws Exception
   {
      PSAuditLogEntry entry1 = new PSAuditLogEntry();
      PSAuditLogEntry entry2 = new PSAuditLogEntry();
      assertEquals(entry1, entry2);
      assertEquals(entry1.hashCode(), entry2.hashCode());
      
      entry1.setGUID(new PSGuid(PSTypeEnum.INTERNAL, 301));
      assertFalse(entry1.equals(entry2));
      assertFalse(entry1.hashCode() == entry2.hashCode());
      
      entry2.setGUID(entry1.getGUID());
      assertEquals(entry1, entry2);
      assertEquals(entry1.hashCode(), entry2.hashCode());
      
      entry1.setAction(AuditTypes.SAVE);
      assertFalse(entry1.equals(entry2));
      
      entry2.setAction(entry1.getAction());
      assertEquals(entry1, entry2);
      assertEquals(entry1.hashCode(), entry2.hashCode());
      
      entry1.setDate(new Date());
      assertFalse(entry1.equals(entry2));
      
      entry2.setDate(entry1.getDate());
      assertEquals(entry1, entry2);
      assertEquals(entry1.hashCode(), entry2.hashCode());
      
      entry1.setObjectGUID(new PSGuid(PSTypeEnum.ACL, 301));
      assertFalse(entry1.equals(entry2));
      
      entry2.setObjectGUID(entry1.getObjectGUID());
      assertEquals(entry1, entry2);
      assertEquals(entry1.hashCode(), entry2.hashCode());
      
      entry1.setUserName("admin1");
      assertFalse(entry1.equals(entry2));
      
      entry2.setUserName(entry1.getUserName());
      assertEquals(entry1, entry2);
      assertEquals(entry1.hashCode(), entry2.hashCode());      
   }
}

