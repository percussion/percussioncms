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
 * Plan builder for performing sequence reordering of complex child content
 * items.
 */
public class PSSequenceUpdatePlanBuilder extends PSModifyPlanBuilder
{
   // see superclass
   public PSSequenceUpdatePlanBuilder(PSContentEditorHandler ceh,
      PSContentEditor ce, PSApplication app)
   {
      super(ceh, ce, app);
   }

   /**
    * Creates a plan that will perform a resequencing of rows in a child content
    * table.   See {@link PSModifyPlanBuilder#createModifyPlan(PSDisplayMapper,
    * PSFieldSet) super.createModifyPlan()} for additional details.
    *
    * @throws IllegalArgumentException if fieldSet type is not
    * {@link PSFieldSet#TYPE_COMPLEX_CHILD} or if sequencing is not supported.
    */
   public PSModifyPlan createModifyPlan(PSDisplayMapper mapper,
      PSFieldSet fieldSet) throws PSSystemValidationException, SQLException
   {
      if (mapper == null || fieldSet == null)
         throw new IllegalArgumentException("one or more params is null");

      if (fieldSet.getType() != PSFieldSet.TYPE_COMPLEX_CHILD)
         throw new IllegalArgumentException(
            "fieldSet type must be TYPE_COMPLEX_CHILD");

      if (!fieldSet.isSequencingSupported())
         throw new IllegalArgumentException(
            "fieldSet does not support sequencing");

      String seqUpdateReqName = "SeqUpdate" + mapper.getId();
      ArrayList seqUpdateMappings = new ArrayList(3);

      PSDataMapper seqUpdateMapper =
         PSApplicationBuilder.createSystemMappings(addTableKeys(
            seqUpdateMappings, mapper, fieldSet, true).iterator());

      PSApplicationBuilder.createUpdateDataset(m_internalApp,
         seqUpdateReqName, m_ce, null, null, seqUpdateMapper, false);

      PSModifyPlan plan = new PSModifyPlan(
         PSModifyPlan.TYPE_UPDATE_SEQUENCE);
      plan.addModifyStep(createRevisionValidationStep());

      plan.addModifyStep(new PSUpdateStep(seqUpdateReqName,
         m_internalApp.getRequestTypeHtmlParamName(),
         m_internalApp.getRequestTypeValueUpdate(), true));


      // also add the system table update step to this plan
      String sysReqName = seqUpdateReqName + "SysUpdate";
      PSDataMapper systemMapper = getSystemUpdateMapper(mapper,
         fieldSet);

      PSApplicationBuilder.createUpdateDataset(m_internalApp, sysReqName,
         m_ce, null, null, systemMapper, false);

      plan.addModifyStep(new PSUpdateStep(sysReqName,
         m_internalApp.getRequestTypeHtmlParamName(),
         m_internalApp.getRequestTypeValueUpdate()));

      return plan;
   }

}
