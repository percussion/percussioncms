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

package com.percussion.search;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSSearchIndexEventQueueTest extends ServletTestCase
{
   public void testPause() throws Exception
   {
      PSSearchIndexEventQueue eventQueue = PSSearchIndexEventQueue.getInstance();
      
      try
      {
         assertEquals("Running", eventQueue.getStatus());
         
         eventQueue.clearQueues();
         
         eventQueue.pause();
         assertEquals("Paused", eventQueue.getStatus());
         assertEquals(0, eventQueue.size());
         eventQueue.queueEvent(new PSSearchEditorChangeEvent(PSSearchEditorChangeEvent.ACTION_DELETE, 999999, 1, 310, true));
         assertEquals(1, eventQueue.size());
         Thread.sleep(3000);
         assertEquals(1, eventQueue.size());
           
      }
      finally
      {
         eventQueue.resume();
      }
      assertEquals("Running", eventQueue.getStatus());
   }
}
