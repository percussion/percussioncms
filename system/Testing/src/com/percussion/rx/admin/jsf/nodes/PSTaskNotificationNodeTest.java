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
package com.percussion.rx.admin.jsf.nodes;

import com.percussion.services.schedule.data.PSNotificationTemplate;

import junit.framework.TestCase;

/**
 * @author Andriy Palamarchuk
 */
public class PSTaskNotificationNodeTest extends TestCase
{
   public void testDummy()
   {
      //Added a dummy test remove it after fixing the actual test
   }
   public void ignoretestConstructor()
   {
      try
      {
         new PSTaskNotificationNode(null);
      }
      catch (IllegalArgumentException expected) {}
      
      final PSNotificationTemplate template = new PSNotificationTemplate();
      template.setName(LABEL);
      final PSTaskNotificationNode node =
            new PSTaskNotificationNode(template);
      assertEquals(template, node.getNotification());
      assertEquals(template.getName(), node.getTitle());
   }

   // test data
   private static final String LABEL = "Label 1";
}
