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
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.util.PSCollection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Plan builder for performing inserts of Simple child rows
 */
public class PSSimpleChildInsertPlanBuilder extends PSModifyPlanBuilder
{
   // see superclass
   public PSSimpleChildInsertPlanBuilder(PSContentEditorHandler ceh,
      PSContentEditor ce, PSApplication app)
   {
      super(ceh, ce, app);
   }

   /**
    * Creates a plan that will perform an insert of any Simple child rows.  See
    * {@link PSModifyPlanBuilder#createModifyPlan(PSDisplayMapper, PSFieldSet)
    * super.createModifyPlan()} for additional details.
    */
   public PSModifyPlan createModifyPlan(PSDisplayMapper mapper,
      PSFieldSet fieldSet) throws PSSystemValidationException, SQLException
   {
      if (mapper == null || fieldSet == null)
         throw new IllegalArgumentException("one or more params is null");

      if (fieldSet.getType() != PSFieldSet.TYPE_SIMPLE_CHILD)
         throw new IllegalArgumentException(
            "fieldSet type must be TYPE_SIMPLE_CHILD");

      PSModifyPlan plan = new PSModifyPlan(PSModifyPlan.TYPE_INSERT_PLAN);

      ArrayList simpleChildMappings = new ArrayList(3);

      // create insert resource
      String reqName = "SimpleInsert" + mapper.getId();
      PSDataMapper simpleInsertSystemMapper =
         PSApplicationBuilder.createSystemMappings(addTableKeys(
            simpleChildMappings, mapper, fieldSet).iterator());

      PSApplicationBuilder.createInsertDataset(m_internalApp, reqName,
         m_ce, mapper, null, simpleInsertSystemMapper);

      /* need the param to use to set list lengths in case there are more
       * than one simple child on the parent item.
       */
      String controlParam = getSimpleFieldParam(fieldSet, mapper);

      /* add to the plan - only want to perform this step
       * if we actually get a value to insert.
       */
      PSCollection conditions = createSimpleConditional(controlParam);
      PSUpdateStep insertStep = new PSConditionalModifyStep(reqName,
         m_internalApp.getRequestTypeHtmlParamName(),
         m_internalApp.getRequestTypeValueInsert(), true, conditions);
      insertStep.setControlParam(controlParam);
      plan.addModifyStep(insertStep);

      return plan;
   }

   /**
    * Given a display mapper and fieldset, creates a PSConditional
    * object collection that
    * will only evaluate to <code>true</code> if the parameter is not
    * <code>null</code> in the incoming html parameters.
    * This is used to only perform simple child inserts if the values are
    * provided.  If no values are provided, then we will still want to do
    * deletes, but not any inserts.
    *
    * @param paramName The name of the parameter to check.  May be
    * <code>null</code>.
    *
    * @return A collection of PSConditional objects, never <code>null</code>.
    */
   private PSCollection createSimpleConditional(String paramName)
   {
      PSCollection conditionals = new PSCollection(PSConditional.class);

      if (paramName != null)
      {
         try
         {
            PSConditional cond = new PSConditional(
               new PSHtmlParameter(paramName), PSConditional.OPTYPE_ISNOTNULL,
               null, PSConditional.OPBOOL_AND);
            conditionals.add(cond);
         }
         catch (IllegalArgumentException e)
         {
            throw new IllegalArgumentException(e.getLocalizedMessage());
         }
      }

      return conditionals;
   }


   /**
    * Determines the Html paramter name for the field mapped in the Simple
    * child mapper.  Assumes that the mapper contains a mapping
    * referencing a field in the simple child field set.
    *
    * @param fieldSet The fieldSet that the mapper references.  Assumed not
    * <code>null</code>.
    * @param mapper The simple child mapper, assumed not <code>null</code> and
    * of the correct type.
    *
    * @return The paramter name of the first mapped field, or <code>null</code>
    * if none are found.
    */
   private String getSimpleFieldParam(PSFieldSet fieldSet,
      PSDisplayMapper mapper)
   {
      String result = null;

      Iterator mappings = mapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         Object o = fieldSet.get(mapping.getFieldRef());
         if (o instanceof PSFieldSet)
            continue;
         result = ((PSField)o).getSubmitName();
         break;
      }

      return result;
   }
}
