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
package com.percussion.cms.handlers;

import static com.percussion.cms.handlers.PSModifyCommandHandler.getAction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.percussion.cms.PSEditorChangeEvent;
import com.percussion.cms.PSModifyPlan;
import com.percussion.server.PSRequest;
import com.percussion.utils.testing.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;


/**
 * @author Andriy Palamarchuk
 */
@Category(UnitTest.class)
public class PSModifyCommandHandlerTest
{
   @Test
   public void testGetAction()
   {
      final PSRequest request = new PSRequest(null, null, null, null);
      
      // unknown plan
      try
      {
         getAction(-10, request);
         fail();
      }
      catch (IllegalStateException success) {}
      
      assertEquals(PSEditorChangeEvent.ACTION_INSERT, 
            getAction(PSModifyPlan.TYPE_INSERT_PLAN, request));

      assertEquals(PSEditorChangeEvent.ACTION_UPDATE,
            getAction(PSModifyPlan.TYPE_UPDATE_PLAN, request));
      assertEquals(PSEditorChangeEvent.ACTION_UPDATE,
            getAction(PSModifyPlan.TYPE_UPDATE_NO_BIN_PLAN, request));

      // sequence with no child id should return the undefined action
      assertNull(request.getParameter(
            PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME));
      assertEquals(PSEditorChangeEvent.ACTION_UNDEFINED,
            getAction(PSModifyPlan.TYPE_UPDATE_SEQUENCE, request));
      request.setParameter(PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME,
            "101");
      assertEquals(PSEditorChangeEvent.ACTION_UPDATE,
            getAction(PSModifyPlan.TYPE_UPDATE_SEQUENCE, request));

      assertEquals(PSEditorChangeEvent.ACTION_DELETE,
            getAction(PSModifyPlan.TYPE_DELETE_ITEM, request));
   }
}
