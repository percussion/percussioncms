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
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSSystemValidationException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Plan builder for performing inserts of parent content items.
 */
public class PSInsertPlanBuilder extends PSModifyPlanBuilder
{
   // see superclass
   public PSInsertPlanBuilder(PSContentEditorHandler ceh, PSContentEditor ce,
      PSApplication app)
   {
      super(ceh, ce, app);
   }

   /**
    * Creates a plan that will perform an insert of the parent item specified by
    * the supplied mapper.   See
    * {@link PSModifyPlanBuilder#createModifyPlan(PSDisplayMapper, PSFieldSet)
    * super.createModifyPlan()} for additional details.
    *
    * @throws IllegalArgumentException if fieldSet type is not
    * {@link PSFieldSet#TYPE_PARENT}.
    */
   public PSModifyPlan createModifyPlan(PSDisplayMapper mapper,
      PSFieldSet fieldSet) throws PSSystemValidationException, SQLException
   {
      if (mapper == null || fieldSet == null)
         throw new IllegalArgumentException("one or more params is null");

      if (fieldSet.getType() != PSFieldSet.TYPE_PARENT)
         throw new IllegalArgumentException(
            "fieldSet type must be TYPE_PARENT");

      PSModifyPlan plan = new PSModifyPlan(PSModifyPlan.TYPE_INSERT_PLAN);
      PSDataMapper insertSystemMapper = null;

      String insertRequestName = "Insert" + mapper.getId();

      // create the parent insert system mapper
      ArrayList insertSysMappings =
         PSApplicationBuilder.getSystemInsertMappings(m_ceHandler, m_ce);
      insertSystemMapper = PSApplicationBuilder.createSystemMappings(
         addTableKeys(insertSysMappings, mapper, fieldSet).iterator());

      // create the resource
      PSApplicationBuilder.createInsertDataset(m_internalApp, insertRequestName,
         m_ce, mapper, null, insertSystemMapper);

      // add to plan
      plan.addModifyStep(new PSUpdateStep(insertRequestName,
         m_internalApp.getRequestTypeHtmlParamName(),
         m_internalApp.getRequestTypeValueInsert()));

      // add all binary fields to plan as they are all changed each time
      Map binFieldMap = new HashMap();
      Iterator binFields = getBinaryFields(fieldSet).iterator();
      while (binFields.hasNext())
      {
         PSField binField = (PSField)binFields.next();
         binFieldMap.put(binField.getSubmitName(), null);
      }
      plan.setBinaryFields(binFieldMap);

      return plan;
   }
         
}
