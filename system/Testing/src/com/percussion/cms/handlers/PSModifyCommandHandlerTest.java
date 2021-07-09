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
