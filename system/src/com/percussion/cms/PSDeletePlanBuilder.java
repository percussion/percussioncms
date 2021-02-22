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
