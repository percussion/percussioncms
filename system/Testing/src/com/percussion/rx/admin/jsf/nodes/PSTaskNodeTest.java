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
package com.percussion.rx.admin.jsf.nodes;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.schedule.data.PSNotifyWhen;
import com.percussion.services.schedule.data.PSScheduledTask;

import junit.framework.TestCase;

/**
 * @author Andriy Palamarchuk
 */
public class PSTaskNodeTest extends TestCase
{
   public void testGetNotifyWhenChoices()
   {
      final PSScheduledTask schedule = new PSScheduledTask();
      schedule.setId(new PSGuid(HOST_ID, PSTypeEnum.SCHEDULED_TASK, UUID));
      schedule.setName("Label1");
      final PSTaskNode n = new PSTaskNode(schedule);
      assertEquals(PSNotifyWhen.values().length,
            n.getNotifyWhenChoices().size());
   }

   /**
    * Sample GUID UUID.
    */
   protected static final int UUID = 123;

   /**
    * Sample GUID host id.
    */
   protected static final int HOST_ID = 10;
}
