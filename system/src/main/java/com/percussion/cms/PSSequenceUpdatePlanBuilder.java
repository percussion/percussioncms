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
