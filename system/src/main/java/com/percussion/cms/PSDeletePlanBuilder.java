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
import java.util.List;

/**
 * Plan builder for performing delete/purge operation.
 */
public class PSDeletePlanBuilder extends PSModifyPlanBuilder
{
   // see superclass
   public PSDeletePlanBuilder(PSContentEditorHandler ceh, PSContentEditor ce,
      PSApplication app)
   {
      super(ceh, ce, app);
   }

   /**
    * Creates a plan that will perform delete of the item or child specified
    * by the supplied mapper. See
    * {@link PSModifyPlanBuilder#createModifyPlan(PSDisplayMapper, PSFieldSet)
    * super.createModifyPlan()} for details.
    */
   public PSModifyPlan createModifyPlan(PSDisplayMapper mapper,
      PSFieldSet fieldSet) throws PSSystemValidationException, SQLException
   {

      if (mapper == null || fieldSet == null)
         throw new IllegalArgumentException("one or more params is null");

      // create a plan for deleting
      PSModifyPlan plan = new PSModifyPlan(PSModifyPlan.TYPE_DELETE_ITEM);

      List deleteMappings = new ArrayList();
      PSDataMapper deleteSystemMapper = null;

      int deleteFlag = PSApplicationBuilder.FLAG_ALLOW_DELETES;

      // add system mappings and resource mappings
      deleteMappings = addTableKeysForDelete(
              getSystemDeleteMappings(mapper, fieldSet), mapper, fieldSet);

      List childMappings = getDeleteChildMappings(mapper, fieldSet);

      // add **to** system mapping & resource mappings,
      //add simple/complex child mappings
      deleteMappings.addAll(childMappings);

      deleteSystemMapper =
         PSApplicationBuilder.createSystemMappings(deleteMappings.iterator());


      //create delete resource
      String deleteRequestName = "Delete" + mapper.getId();
      PSApplicationBuilder.createDeleteDataset(m_internalApp, deleteRequestName,
         m_ce, null, null, deleteSystemMapper);

      // add to plan
      plan.addModifyStep(new PSUpdateStep(deleteRequestName,
         m_internalApp.getRequestTypeHtmlParamName(),
         m_internalApp.getRequestTypeValueDelete()));

      return plan;
   }
}
