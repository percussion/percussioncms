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

package com.percussion.cms;

import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSSystemValidationException;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Plan builder for deleting simple child rows.  
 */
public class PSSimpleChildDeletePlanBuilder extends PSModifyPlanBuilder
{
   // see superclass
   public PSSimpleChildDeletePlanBuilder(PSContentEditorHandler ceh,
      PSContentEditor ce, PSApplication app)
   {
      super(ceh, ce, app);
   }

   /**
    * Creates a plan that will delete any simple child rows.  See
    * {@link PSModifyPlanBuilder#createModifyPlan(PSDisplayMapper, PSFieldSet)
    * super.createModifyPlan()} for details.
    *
    * @throws IllegalArgumentException if fieldSet type is not
    * {@link PSFieldSet#TYPE_SIMPLE_CHILD}.
    */
   public PSModifyPlan createModifyPlan(PSDisplayMapper mapper,
      PSFieldSet fieldSet) throws PSSystemValidationException, SQLException
   {
      if (mapper == null || fieldSet == null)
         throw new IllegalArgumentException("one or more params is null");

      if (fieldSet.getType() != PSFieldSet.TYPE_SIMPLE_CHILD)
         throw new IllegalArgumentException(
            "fieldSet type must be TYPE_SIMPLE_CHILD");

      PSModifyPlan plan = new PSModifyPlan(PSModifyPlan.TYPE_UPDATE_PLAN);

      ArrayList simpleChildMappings = new ArrayList(3);

      // create delete resource
      String reqName = "SimpleDelete" + mapper.getId();
      PSDataMapper simpleDeleteSystemMapper =
         PSApplicationBuilder.createSystemMappings(
            addTableKeys(simpleChildMappings, mapper, fieldSet).iterator());

      PSApplicationBuilder.createDeleteDataset(m_internalApp, reqName,
         m_ce, mapper, null, simpleDeleteSystemMapper);

      // add to the plan
      IPSModifyStep delStep = new PSUpdateStep(reqName,
         m_internalApp.getRequestTypeHtmlParamName(),
         m_internalApp.getRequestTypeValueDelete());
      plan.addModifyStep(delStep);
      
      return plan;
   }
}
