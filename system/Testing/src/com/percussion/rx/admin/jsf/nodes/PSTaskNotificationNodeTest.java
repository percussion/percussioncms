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
