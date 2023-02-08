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
