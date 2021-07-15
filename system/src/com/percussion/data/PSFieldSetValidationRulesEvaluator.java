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

import com.percussion.cms.PSDisplayFieldElementBuilder;
import com.percussion.cms.PSTableValueBuilder;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.i18n.PSI18nUtils;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Evaluator for field set validation rules. Evaluates the field set
 * occurrence rules.
 */
public class PSFieldSetValidationRulesEvaluator
{
   /**
    * Create a new field set validation evaluator for the provided field set.
    *
    * @param fieldSet the field set to create the validation evaluator for,
    *    not <code>null</code>.
    * @param uiSet the ui set to create the validation evaluator for,
    *    not <code>null</code>.
    * @throws IllegalArgumentException if the provided field set or ui set is
    *    <code>null</code>.
    */
   public PSFieldSetValidationRulesEvaluator(PSFieldSet fieldSet,
      PSUISet uiSet)
   {
      if (fieldSet == null)
         throw new IllegalArgumentException("fieldSet and uiSet cannot be null");

      m_fieldSet = fieldSet;
      m_uiSet = uiSet;
   }

   /**
    * Validates the repeatability settings for this field set.
    *
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
    * @return <code>true</code> if the repeatability settings are fulfilled,
    *    <code>false</code> otherwise.
    * @throws IllegalArgumentException if any of the provided parameters except 
    * lang is <code>null</code>.
    */
   public boolean isValidRepeatability(Integer pageId, Document page,
      PSErrorCollector errorCollector, String lang)
   {
      if (pageId == null || page == null || errorCollector == null)
         throw new IllegalArgumentException("parameters cannot be null");

      List submitNames = new ArrayList();
      submitNames.add(m_fieldSet.getName());
      List displayNames = new ArrayList();
      String label = "unlabeled";
      if (m_uiSet.getLabel() != null)
         label = m_uiSet.getLabel().getText();
      displayNames.add(label);
      List args = new ArrayList();
      args.add(m_fieldSet.getName());
      String pattern = "";
      switch (m_fieldSet.getRepeatability())
      {
         case PSFieldSet.REPEATABILITY_ONE_OR_MORE:
            if (getRowCount(m_fieldSet.getName(), page) >= 1)
               return true;

            pattern = PSI18nUtils.getString(
               "psx.ce.error@requiredOccurrence", lang);

            errorCollector.add(pageId, submitNames, displayNames, pattern, args);
            break;

         case PSFieldSet.REPEATABILITY_COUNT:
            int rowCount = getRowCount(m_fieldSet.getName(), page);
            if (rowCount == m_fieldSet.getCount())
               return true;

            args.add(Integer.toString(m_fieldSet.getCount()));
            args.add(Integer.toString(rowCount));

            pattern = PSI18nUtils.getString(
               "psx.ce.error@countedOccurrence", lang);

            errorCollector.add(pageId, submitNames, displayNames, pattern, args);
            break;
      }

      return false;
   }

   /**
    * Get the number of rows from the page document for the provided
    * parameter name.
    *
    * @paramName the parameter name we want the row count for, assumed not
    *    <code>null</code>.
    * @param page the page document to get the row count from, assumed not
    *    <code>null</code>.
    * @return the number of parameter rows found.
    */
   private int getRowCount(String paramName, Document page)
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
            NodeList rows = control.getElementsByTagName(
               PSTableValueBuilder.ROW_NAME);

            return rows.getLength();
         }
      }

      return counter;
   }

   /**
    * Get the field set evaluated through this evaluator. This should be
    * treated as read only object.
    *
    * @return the field set to be evaludated, never <code>null</code>.
    */
   public PSFieldSet getFieldSet()
   {
      return m_fieldSet;
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
    * The field set this evaluator is validating, specified during
    * construction, never <code>null</code> after that.
    */
   private PSFieldSet m_fieldSet = null;

   /**
    * The UI set for the field set this evaluator was created with. Specified
    * during construction, never <code>null</code> after that.
    */
   private PSUISet m_uiSet = null;
}
