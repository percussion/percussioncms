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
public class PSChildInsertPlanBuilder extends PSModifyPlanBuilder
{
   // see superclass
   public PSChildInsertPlanBuilder(PSContentEditorHandler ceh,
      PSContentEditor ce, PSApplication app)
   {
      super(ceh, ce, app);
   }

   /**
    * Creates a plan that will perform an insert of the complex child item
    * specified by the supplied mapper.   See
    * {@link PSModifyPlanBuilder#createModifyPlan(PSDisplayMapper, PSFieldSet)
    * super.createModifyPlan()} for additional details.
    *
    * @return The plan, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if fieldSet type is not
    * {@link PSFieldSet#TYPE_COMPLEX_CHILD} or if either param is <code>null
    * <code>.
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
      PSModifyPlan plan = new PSModifyPlan(PSModifyPlan.TYPE_INSERT_PLAN);
      plan.addModifyStep(createRevisionValidationStep());

      String insertRequestName = "InsertChild" + mapper.getId();

      /* for complex children, inserting must update the parent's system
       * fields, while for parent items, the system fields are simply
       * inserted.
       */
      PSDataMapper childInsertSystemMapper = getSystemUpdateMapper(mapper,
         fieldSet);

      // make the child insert mapper - this will add child table keys
      ArrayList insertSysMappings = new ArrayList(3);
      PSDataMapper insertSystemMapper =
         PSApplicationBuilder.createSystemMappings(addTableKeys(
            insertSysMappings, mapper, fieldSet, true).iterator());

      // make the system update mapper

      /* If the complex child does not contain a simple child, then we can
       * allow multi-row inserts.
       */
      boolean allowMultiRowInsert = false;
      if (allowMultipleInserts(mapper, fieldSet))
         allowMultiRowInsert = true;

      // create the insert resource
      PSApplicationBuilder.createInsertDataset(m_internalApp, insertRequestName,
         m_ce, mapper, null, insertSystemMapper);

      // add to plan
      plan.addModifyStep(new PSUpdateStep(insertRequestName,
         m_internalApp.getRequestTypeHtmlParamName(),
         m_internalApp.getRequestTypeValueInsert(), allowMultiRowInsert));

      // create another resource to update the content status table on insert
      String sysReqName = insertRequestName + "SysUpdate";

      PSApplicationBuilder.createUpdateDataset(m_internalApp, sysReqName,
         m_ce, null, null, childInsertSystemMapper, false);
      plan.addModifyStep(new PSUpdateStep(sysReqName,
         m_internalApp.getRequestTypeHtmlParamName(),
         m_internalApp.getRequestTypeValueUpdate()));

      return plan;
   }

}
