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
import com.percussion.data.PSConditionalEvaluator;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSSingleHtmlParameter;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.util.PSCollection;
import com.percussion.util.PSUniqueObjectGenerator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Plan builder for performing updates.
 */
public class PSUpdatePlanBuilder extends PSModifyPlanBuilder
{
   // see superclass
   public PSUpdatePlanBuilder(PSContentEditorHandler ceh, PSContentEditor ce,
      PSApplication app)
   {
      super(ceh, ce, app);
   }

   /**
    * Creates a plan that will perform an update of the item or child specified
    * by the supplied mapper. Any binary fields are added as separate
    * conditional steps and are only updated if data is supplied.  See
    * {@link PSModifyPlanBuilder#createModifyPlan(PSDisplayMapper, PSFieldSet)
    * super.createModifyPlan()} for details.
    */
   public PSModifyPlan createModifyPlan(PSDisplayMapper mapper,
      PSFieldSet fieldSet) throws PSValidationException, SQLException
   {
      if (mapper == null || fieldSet == null)
         throw new IllegalArgumentException("one or more params is null");

      // create plan and add revision validation since we will be updating
      PSModifyPlan plan = new PSModifyPlan(PSModifyPlan.TYPE_UPDATE_PLAN);
      plan.addModifyStep(createRevisionValidationStep());
      PSDataMapper updateSystemMapper = null;

      String updateRequestName = "Update" + mapper.getId();
      int insertUpdateFlag = PSApplicationBuilder.FLAG_ALLOW_INSERTS |
         PSApplicationBuilder.FLAG_ALLOW_UPDATES;

      updateSystemMapper = getSystemUpdateMapper(mapper, fieldSet);

      // the main update resource will exclude all binary fields
      PSApplicationBuilder.createUpdateDataset(m_internalApp, updateRequestName,
         m_ce, mapper, null, updateSystemMapper, false, insertUpdateFlag);

      // add to plan
      plan.addModifyStep(new PSUpdateStep(updateRequestName,
         m_internalApp.getRequestTypeHtmlParamName(),
         m_internalApp.getRequestTypeValueUpdate()));


      /* now create an update for each binary field that we will update.  If
       * necessary, create a clear resource as well.
       */
      Iterator binFields = getBinaryFields(fieldSet).iterator();
      Map binFieldMap = new HashMap();
      
      String baseReqName = "UpdateBinary";
      while (binFields.hasNext())
      {
         PSField binField = (PSField)binFields.next();

         // be sure the field is mapped
         PSDisplayMapping mapping = getMapping(mapper,
            binField.getSubmitName());
         if (mapping == null)
            continue;

         // create copy of display mapper with only the one binary field
         PSDisplayMapper binMapper = new PSDisplayMapper(
            mapper.getFieldSetRef());
         binMapper.add(new PSDisplayMapping(mapping.getFieldRef(),
            mapping.getUISet()));

         // create the dataset
         updateSystemMapper = getSystemUpdateMapper(mapper, fieldSet);
         String binReqName = PSUniqueObjectGenerator.makeUniqueName(
            baseReqName);
         PSApplicationBuilder.createUpdateDataset(m_internalApp, binReqName,
            m_ce, binMapper, null, updateSystemMapper, true, insertUpdateFlag);

         // add to plan
         PSCollection conds = createBinaryUpdateConditional(binField);
         plan.addModifyStep(new PSConditionalModifyStep(binReqName,
            m_internalApp.getRequestTypeHtmlParamName(),
            m_internalApp.getRequestTypeValueUpdate(), false, conds));

         // add to binfieldmap list
         List condList = new ArrayList();
         condList.add(new PSConditionalEvaluator(conds));

         // do the same if we need to clear as well
         PSCollection clearConditions = createBinaryClearConditional(
            binField);
         if (clearConditions != null)
         {
            // need to map the backend column to something that will be null
            PSDataMapping clearMapping = null;
            try
            {
               clearMapping = new PSDataMapping(
                  new PSTextLiteral(""), binField.getLocator());
            }
            catch (IllegalArgumentException e)
            {
               // won't happen, but just in case...
               throw new IllegalArgumentException(e.getLocalizedMessage());
            }
            updateSystemMapper.add(clearMapping);

            // create dataset
            String binClearReqName = PSUniqueObjectGenerator.makeUniqueName(
               baseReqName);
            PSApplicationBuilder.createUpdateDataset(m_internalApp,
               binClearReqName, m_ce, null, null, updateSystemMapper, true,
               insertUpdateFlag);

            // add to plan
            plan.addModifyStep(new PSConditionalModifyStep(binClearReqName,
               m_internalApp.getRequestTypeHtmlParamName(),
               m_internalApp.getRequestTypeValueUpdate(), false,
               clearConditions));

            // add to binfieldmap list
            condList.add(new PSConditionalEvaluator(clearConditions));                           
         }
         
         // add conditions to binfieldmap
         binFieldMap.put(binField.getSubmitName(), condList);
      }
      
      plan.setBinaryFields(binFieldMap);

      return plan;
   }


   /**
    * Locates a mapping for the supplied field name, compare is case
    * insensitive.
    *
    * @param mapper The display mapper to check, may not be <code>null</code>.
    * @param fieldName The submit name of the field, may not be
    * <code>null</code> or empty.
    *
    * @return The display mapping if found, <code>null</code> if not.
    */
   private PSDisplayMapping getMapping(PSDisplayMapper mapper, String fieldName)
   {
      if (mapper == null || fieldName == null || fieldName.trim().length() == 0)
         throw new IllegalArgumentException("invalid param");

      Iterator mappings = mapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         if (mapping.getFieldRef().equalsIgnoreCase(fieldName))
            return mapping;
      }

      return null;
   }


   /**
    * Given a binary field, creates a collection of conditionals that will
    * evaluate to <code>true</code> only if the binary field data is supplied
    * and a "clear" has not been specified.
    *
    * @param field The binary field.   Either it has forceBinary flag or the
    * backend datatype is binary.  Assumed not <code>null</code>.
    *
    * @return A collection of PSConditional objects, never <code>null</code> or
    * empty.
    */
   private PSCollection createBinaryUpdateConditional(PSField binField)
   {
      PSCollection conditionals = new PSCollection(PSConditional.class);

      String paramName = binField.getSubmitName();
      String clearParamName = binField.getClearBinaryParam();

      try
      {
         PSConditional cond = new PSConditional(
            new PSSingleHtmlParameter(paramName),
            PSConditional.OPTYPE_ISNOTNULL, null, PSConditional.OPBOOL_AND);
         conditionals.add(cond);

         // need to exclude this if a "clear" has been specified.
         if (clearParamName != null)
         {
            cond = new PSConditional(
               new PSSingleHtmlParameter(clearParamName),
               PSConditional.OPTYPE_NOTEQUALS,
               new PSTextLiteral(CLEAR_PARAM_TRUE), PSConditional.OPBOOL_AND);
            conditionals.add(cond);
         }
      }
      catch (IllegalArgumentException e)
      {
         // can't happen, but just in case
         throw new IllegalArgumentException(e.toString());
      }

      return conditionals;
   }

   /**
    * Given a binary field, creates a collection of conditionals that will
    * evaluate to <code>true</code> only if the binary field is to be
    * cleared.
    *
    * @param field The binary field.   Either it has forceBinary flag or the
    * backend datatype is binary.  Assumed not <code>null</code>.
    *
    * @return A collection of PSConditional objects.  Never empty.  If the
    * binField does not specifiy a clearParamName, then <code>null</code> is
    * returned.
    */
   private PSCollection createBinaryClearConditional(PSField binField)
   {
      PSCollection conditionals = null;

      String clearParamName = binField.getClearBinaryParam();
      if (clearParamName != null)
      {
         try
         {
            conditionals = new PSCollection(PSConditional.class);
            PSConditional cond = new PSConditional(
               new PSSingleHtmlParameter(clearParamName),
               PSConditional.OPTYPE_EQUALS, new PSTextLiteral(CLEAR_PARAM_TRUE),
               PSConditional.OPBOOL_AND);
            conditionals.add(cond);
         }
         catch (IllegalArgumentException e)
         {
            // can't happen, but just in case
            throw new IllegalArgumentException(e.getLocalizedMessage());
         }
      }

      return conditionals;
   }

   /**
    * Constant for value of the binary clear parameter if it is to indicate that
    * the field should be cleared.  Never <code>null</code> or empty.
    */
   private static final String CLEAR_PARAM_TRUE = "yes";
}
