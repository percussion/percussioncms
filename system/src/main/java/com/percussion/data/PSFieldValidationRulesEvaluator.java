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
package com.percussion.data;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSChoiceBuilder;
import com.percussion.cms.PSDisplayFieldElementBuilder;
import com.percussion.cms.PSTableValueBuilder;
import com.percussion.cms.handlers.IPSInternalCommandRequestHandler;
import com.percussion.cms.handlers.PSBinaryCommandHandler;
import com.percussion.cms.handlers.PSCommandHandler;
import com.percussion.design.objectstore.PSApplyWhen;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldValidationRules;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.extension.PSExtensionException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.util.PSHtmlParameters;
import com.percussion.util.PSUrlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluator for field validation rules. Evaluates the field validation
 * rules for the supplied field.
 */
public class PSFieldValidationRulesEvaluator
{
   /**
    * Create a new field validation evaluator for the provided field.
    *
    * @param field the field to create the validation evaluator for, not
    *    <code>null</code>.
    * @param uiSet the ui set to create the validation evaluator for,
    *    not <code>null</code>.
    * @throws IllegalArgumentException if the provided field is
    *    <code>null</code>.
    */
   public PSFieldValidationRulesEvaluator(PSField field, PSUISet uiSet)
   {
      if (field == null || uiSet == null)
         throw new IllegalArgumentException("field and uiSet cannot be null");

      m_field = field;
      m_uiSet = uiSet;
   }

   /**
    * Checks if this field is valid against the provided data.
    *
    * @param pageId the pageid which we are validating, not
    *    <code>null</code>.
    * @param data the execution data based on which the validation will be
    *    performed, not <code>null</code>.
    * @param errorCollector the error collector to be updated in case of a
    *    validation error, not <code>null</code>.
    * @return <code>true</code> if the validation succeeded,
    *    <code>false</code> otherwise.
    * @throws IllegalArgumentException if any of the provided parameters is
    *    <code>null</code>.
    */
   public boolean isValid(Integer pageId, PSExecutionData data,
      PSErrorCollector errorCollector)
   {
      if (pageId == null || data == null || errorCollector == null)
         throw new IllegalArgumentException("parameters cannot be null");

      try
      {
         if (evaluateApplyWhen(data))
         {
            PSFieldValidationRules rules = m_field.getValidationRules();
            if (rules == null)
               return true;

            PSRuleListEvaluator eval = new PSRuleListEvaluator(rules.getRules());
            boolean isValid = eval.isMatch(data);
            if (!isValid)
               errorCollector.add(pageId, this);

            return isValid;
         }

         return true;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.getLocalizedMessage(),e);
      }
   }

   /**
    * Validates the occurrence settings for this field.
    *
    * @param data the execution data to evaluate against, not
    *    <code>null</code>.
    * @param pageId the pageid which we are validating, not
    *    <code>null</code>.
    * @param page the page document based on which the validation will be
    *    performed, not <code>null</code>.
    * @param errorCollector the error collector to be updated in case of a
    *    validation error, not <code>null</code>.
    * @param lang the language/locale string to be used to produce the localized
    *    error message. This must follow the XML notation for language or locale
    *    string. See {@link PSI18nUtils#getLocaleFromString} for details. May be
    *    <code>null</code> or <code>empty</code>.
    * @return <code>true</code> if the occurrence settings are fulfilled,
    *    <code>false</code> otherwise.
    * @throws IllegalArgumentException if any of the provided parameters except 
    * lang is <code>null</code>.
    */
   public boolean isValidOccurrence(PSExecutionData data, Integer pageId,
      Document page, PSErrorCollector errorCollector, String lang)
   {
      if (data == null || pageId == null || page == null ||
         errorCollector == null)
            throw new IllegalArgumentException("parameters cannot be null");

      // check field dimension
      List submitNames = new ArrayList();
      submitNames.add(m_field.getSubmitName());
      List displayNames = new ArrayList();
      String label = "unlabeled";
      if ( null != m_uiSet.getLabel())
         label = m_uiSet.getLabel().getText();
      displayNames.add(label);
      List args = new ArrayList();
      Integer transId = PSCommandHandler.getTransitionId(data);
      int dimension = m_field.getOccurrenceDimension(transId);
      int valueCount = getValueCount(m_field.getSubmitName(), page);
      String pattern = "";
      switch (dimension)
      {
         case PSField.OCCURRENCE_DIMENSION_OPTIONAL:
         case PSField.OCCURRENCE_DIMENSION_ZERO_OR_MORE:
            return true;

         case PSField.OCCURRENCE_DIMENSION_REQUIRED:
         case PSField.OCCURRENCE_DIMENSION_ONE_OR_MORE:
            if (valueCount > 0)
            {
               return true;
            }
            else if (m_field.isForceBinary())
            {
               PSRequest request = data.getRequest();
               Map backupHtmlParams = request.getParameters();
               try
               {
                  IPSInternalRequestHandler irh =
                     PSServer.getInternalRequestHandler(PSUrlUtils.createUrl(
                        null, null, request.getRequestFileURL(), null, null,
                           new PSRequestContext(request)));

                  IPSInternalCommandRequestHandler rh = null;
                  if (irh instanceof IPSInternalCommandRequestHandler)
                  {
                     rh = (IPSInternalCommandRequestHandler) irh;

                     Map htmlParams = PSHtmlParameters.createStandardParams(
                        backupHtmlParams);
                     htmlParams.put(IPSConstants.SUBMITNAME_PARAM_NAME,
                        m_field.getSubmitName());
                     request.setParameters((HashMap) htmlParams);

                     Document doc = rh.makeInternalRequest(request,
                        PSBinaryCommandHandler.COMMAND_NAME);
                     if (doc != null)
                        return true;
                  }
               }
               catch (Throwable e)
               {
                  // guess it wasn't there
               }
               finally
               {
                  request.setParameters((HashMap) backupHtmlParams);
               }
            }

            args.add(m_field.getSubmitName());
            pattern = PSI18nUtils.getString(
               "psx.ce.error@requiredOccurrence", lang);

            errorCollector.add(pageId, submitNames, displayNames, pattern, args);
            break;

         case PSField.OCCURRENCE_DIMENSION_COUNT:
            if (valueCount == m_field.getOccurrenceCount(transId))
               return true;

            args.add(m_field.getSubmitName());
            args.add(Integer.toString(m_field.getOccurrenceCount(transId)));
            args.add(Integer.toString(valueCount));
            pattern = PSI18nUtils.getString(
               "psx.ce.error@countedOccurrence", lang);

            errorCollector.add(pageId, submitNames, displayNames, pattern, args);
            break;
      }

      return false;
   }

   /**
    * Evaluate the apply when rules. Before the apply rules are evaluated
    * a few implicit rules will be checked.
    * <ol><li>
    *    If no apply rules are specified, this will return <code>true</code>
    *    so the field validation will be processed.
    * </li>
    * <li>
    *    If empty fields are allowed and the provided field does not have
    *    a value this will return <code>false</code>, meaning no validation
    *    will be processed.
    * </li>
    * <li>
    *    If all tests are passed so far, the apply rules will be evaluated,
    *    initializing the result value.
    * </li></ol>
    *
    * @param data the execution data to evaluate against, assumed not
    *    <code>null</code>.
    * @return <code>true</code> if the field should be validated,
    *    <code>false</code> otherwise.
    */
   private boolean evaluateApplyWhen(PSExecutionData data)
      throws PSExtensionException, PSNotFoundException
   {
      PSApplyWhen applyWhen = getApplyWhen();
      if (applyWhen == null)
         return true;

      // check field empty rule
      if (!applyWhen.ifFieldEmpty())
      {
         if (getValueCount(m_field.getSubmitName(), data) == 0)
            return false;
      }

      PSRuleListEvaluator eval =
         new PSRuleListEvaluator(applyWhen.iterator());

      // evaluate the rules
      boolean result = eval.isMatch(data);

      return result;
   }

   /**
    * Get the number of values from the execution data for the provided
    * parameter name.
    *
    * @paramName the parameter name we want the value count for, assumed not
    *    <code>null</code>.
    * @param data the execution data to get the value count from, assumed not
    *    <code>null</code>.
    * @return The number of parameter values found.<br />
    *   Returns 0 if, the value extracted from <code>data</code> is 
    *   <ul>
    *       <li>a <code>String</code> which is empty, blank filled, or null.
    *       </li>
    *       <li>a <code>List</code> which is empty or null.
    *       </li>
    *       <li>a <code>List</code> whose elements are either null or
    *       a <code>String</code> which is empty, blank filled, or null.
    *       </li>
    *   </ul>
    */
   private int getValueCount(String paramName, PSExecutionData data)
   {
      PSHtmlParameterExtractor extractor =
         new PSHtmlParameterExtractor(new PSHtmlParameter(
            m_field.getSubmitName()));

      Object o = extractor.extract(data);
      if (o instanceof String)
      {
         String param = (String) o;
         if (param == null || param.trim().length() == 0)
            return 0;

         return 1;
      }
      if (o instanceof List)
      {
         List params = (List) o;
         if (params == null || params.isEmpty())
            return 0;
         
         // If the list contains nothing but nulls and/or empty strings, 
         // this counts as an empty list.
         int count = 0;
         for ( Object o2:  params)
         {
            if (o2 == null)
               continue;
            
            if (o2 instanceof String)
            {
               String param = (String) o2;
               if (param == null || param.trim().length() == 0)
                  continue;
            }
            count++; 
         }
         return count;     
      }

      return 0;
   }

   /**
    * Get the number of values from the page document for the provided
    * parameter name.
    *
    * @paramName the parameter name we want the value count for, assumed not
    *    <code>null</code>.
    * @param page the page document to get the value count from, assumed not
    *    <code>null</code>.
    * @return the number of parameter values found.
    */
   private int getValueCount(String paramName, Document page)
   {
      int counter = 0;

      NodeList controls = page.getElementsByTagName(
         PSDisplayFieldElementBuilder.CONTROL_NAME);
      for (int i=0; i<controls.getLength(); i++)
      {
         Element control = (Element) controls.item(i);
         String pn = control.getAttribute(
            PSDisplayFieldElementBuilder.PARAMNAME_NAME);

         if (paramName.equals(pn))
         {
            String dimension = control.getAttribute(
               PSDisplayFieldElementBuilder.DIMENSION_NAME);

            NodeList values = null;
            if (dimension.equals(PSDisplayFieldElementBuilder.DIMENSION_SINGLE))
            {
               NodeList nl = control.getChildNodes();
               boolean hasValue = false;
               for(int j=0;nl != null && j<nl.getLength();j++)
               {
                  if (nl.item(j).getNodeType() == Node.ELEMENT_NODE
                        && nl.item(j).getNodeName().equals(
                              PSDisplayFieldElementBuilder.DATA_NAME))
                  {
                     hasValue = true;
                     break;
                  }
               }
               if (hasValue)
                  counter ++;
            }
            else if (dimension.equals(PSDisplayFieldElementBuilder.DIMENSION_ARRAY))
            {
               values = control.getElementsByTagName(
                  PSChoiceBuilder.DISPLAYENTRY_NAME);
               if (values != null)
               {
                  for (int j=0; j<values.getLength(); j++)
                  {
                     Element value = (Element) values.item(j);
                     String selected = value.getAttribute(
                        PSChoiceBuilder.SELECTED_ATTRIBUTE_NAME);
                     if (selected.equals(PSChoiceBuilder.ATTRIB_BOOLEAN_TRUE))
                        counter++;
                  }
               }
            }
            else if (dimension.equals(PSDisplayFieldElementBuilder.DIMENSION_TABLE))
            {
               values = control.getElementsByTagName(
                  PSTableValueBuilder.ROWSET_NAME);

               if (values != null)
                  counter += values.getLength();
            }
         }
      }

      return counter;
   }

   /**
    * Get the field evaluated through this evaluator. This should be treated
    * as read only object.
    *
    * @return the field to be evaludated, never <code>null</code>.
    */
   public PSField getField()
   {
      return m_field;
   }

   /**
    * Get the fields UI set this evaluator is validating. This should be
    * treated as a read only object.
    *
    * @param the UI set of the field beeing evaluated, never
    *    <code>null</code>.
    */
   public PSUISet getUISet()
   {
      return m_uiSet;
   }

   /**
    * Returns the apply when rules of the validation rule being evaluated.
    *
    * @return The PSApplyWhen object if the rule contains apply when rules,
    * <code>null</code> if not.
    */
   public PSApplyWhen getApplyWhen()
   {
      PSFieldValidationRules validationRules = m_field.getValidationRules();
      if (validationRules == null)
         return null;

      return validationRules.getApplyWhen();
   }

   /**
    * The field this evaluator is validating, specified during construction,
    * never <code>null</code> after that.
    */
   private PSField m_field = null;

   /**
    * The UI set for the field this evaluator was created. Specified during
    * construction, never <code>null</code> after that.
    */
   private PSUISet m_uiSet = null;
}
