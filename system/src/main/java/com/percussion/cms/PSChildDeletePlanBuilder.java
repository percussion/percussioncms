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
 * Plan builder for performing inserts of complex child content items.
 */
public class PSChildDeletePlanBuilder extends PSModifyPlanBuilder
{
   // see superclass
   public PSChildDeletePlanBuilder(PSContentEditorHandler ceh,
      PSContentEditor ce, PSApplication app)
   {
      super(ceh, ce, app);
   }

   /**
    * Creates a plan that will perform a delete of the complex child item
    * specified by the supplied mapper.   See
    * {@link PSModifyPlanBuilder#createModifyPlan(PSDisplayMapper, PSFieldSet)
    * super.createModifyPlan()} for additional details.
    *
    * @return The plan, never <code>null</code>.
    *
    * @throws IllegalArgumentException if fieldSet type is not
    * {@link PSFieldSet#TYPE_COMPLEX_CHILD} or if either param is
    * <code>null</code>.
    */
   public PSModifyPlan createModifyPlan(PSDisplayMapper mapper,
      PSFieldSet fieldSet) throws PSSystemValidationException, SQLException
   {
      if (mapper == null || fieldSet == null)
         throw new IllegalArgumentException("one or more params is null");

      if (fieldSet.getType() != PSFieldSet.TYPE_COMPLEX_CHILD)
         throw new IllegalArgumentException(
            "fieldSet type must be TYPE_COMPLEX_CHILD");

      // create plan and add revision validation since we will be updating
      PSModifyPlan plan = new PSModifyPlan(
         PSModifyPlan.TYPE_DELETE_COMPLEX_CHILD);
      plan.addModifyStep(createRevisionValidationStep());

      String delReqName = "DelChild" + mapper.getId();
      ArrayList delChildMappings = new ArrayList(3);
      PSDataMapper delChildSystemMapper =
         PSApplicationBuilder.createSystemMappings(
            addTableKeys(delChildMappings, mapper, fieldSet).iterator());

      PSApplicationBuilder.createDeleteDataset(m_internalApp, delReqName,
         m_ce, null, null, delChildSystemMapper);

      plan.addModifyStep(new PSUpdateStep(delReqName,
         m_internalApp.getRequestTypeHtmlParamName(),
         m_internalApp.getRequestTypeValueDelete()));

      /* for complex children, deleting must update the parent's system
       * fields.
       */
      String sysReqName = delReqName + "SysUpdate";
      PSDataMapper childDeleteSystemMapper = getSystemUpdateMapper(mapper,
         fieldSet);

      PSApplicationBuilder.createUpdateDataset(m_internalApp, sysReqName,
         m_ce, null, null, childDeleteSystemMapper, false);
         
      plan.addModifyStep(new PSUpdateStep(sysReqName,
         m_internalApp.getRequestTypeHtmlParamName(),
         m_internalApp.getRequestTypeValueUpdate()));

      return plan;
   }

}
