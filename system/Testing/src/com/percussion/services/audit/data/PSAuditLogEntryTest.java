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

