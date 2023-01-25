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

import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.error.PSSqlException;
import com.percussion.design.objectstore.PSChoices;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.error.PSNotFoundException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSServerErrors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates the DisplayField element according to the ContentEditor.dtd when
 * the data is an entire result set which contains a single column with
 * multiple rows.
 */
public class PSMultiValueBuilder extends PSDisplayFieldBuilder
{
   /**
    * See {@link PSDisplayFieldBuilder#PSDisplayFieldBuilder(PSFieldSet,
    * PSUISet, PSEditorDocumentBuilder) base} class for a description of
    * params and exceptions unless listed below.
    */
   public PSMultiValueBuilder(PSFieldSet fieldSet, PSUISet ui,
         PSEditorDocumentBuilder parentBuilder)
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException
   {
      super(fieldSet, ui, parentBuilder);
      m_isFieldShown = parentBuilder.showField(fieldSet.getSimpleChildField());
      m_fieldSetName = fieldSet.getName();
   }

   /**
    * See base class for description. This class differs in that it only saves
    * the row data.  The data element is never actually added, as the data
    * is contained in the choice element added by {@link #addChoiceElement}.
    * {@link #showField} should always be called before this method, and this
    * method should never be called if the {@link #showField} returns <code>
    * false</code>.
    *
    * @throws PSDataExtractionException if <code>showField</code> would
    * return <code>false</code>, as the resultset used by this builder may
    * have already been popped.
    */
   protected boolean addDataElement(Document doc, Element parent,
         PSExecutionData data, boolean isNewDoc)
      throws PSDataExtractionException
   {
      if (null == doc || null == parent || null == data)
         throw new IllegalArgumentException("one or more params was null");

      boolean addedElem = false;

      List rows = null;
      try
      {
         if (!m_isFieldShown)
            throw new PSDataExtractionException(
                  IPSServerErrors.CE_MISSING_RESULTSET, m_fieldSetName);

         if (data != null && !isNewDoc)
         {
            try
            {
               data.saveResultSetContext();
               if (data.getNextResultSet() == null)
                  throw new PSDataExtractionException(
                        IPSServerErrors.CE_MISSING_RESULTSET, m_fieldSetName);

               // only need to read rows if we are being displayed
               rows = new ArrayList();
               while (data.readRow())
                  rows.add(data.getCurrentResultRowData()[0].toString());
            }
            finally
            {
               data.restoreResultSetContext();
            }
         }
      }
      catch (SQLException e)
      {
         throw new PSDataExtractionException( IPSServerErrors.CE_SQL_ERRORS,
               PSSqlException.getFormattedExceptionText(e));
      }
      finally
      {
         // set our rows in the ThreadLocal
         m_rows.set(rows);
         return addedElem;
      }
   }

   // See base class for description.
   public boolean addChoiceElement(Document doc, Element parent,
         PSChoices choices, PSExecutionData data, boolean isNewDoc)
      throws PSDataExtractionException
   {
      if (null == doc || null == parent || null == choices || null == data)
         throw new IllegalArgumentException("one or more params was null");
      
      boolean addedElem = PSChoiceBuilder.addChoiceElement(
         doc, parent, choices, data, isNewDoc, true);

      List rows = (List)m_rows.get();
      if (!isNewDoc && rows != null)
         PSDisplayFieldElementBuilder.selectChoices(parent, rows);
      
      return addedElem;
   }

   /**
    * See base class for description.  This class will pop the resultset off the
    * stack in the execution data if this field will not be shown.
    */
   protected boolean showField(PSExecutionData data)
      throws PSDataExtractionException
   {
      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      if (!m_isFieldShown)
      {
         try
         {
            data.saveResultSetContext();
            if (data.getNextResultSet() == null)
               throw new PSDataExtractionException(
                     IPSServerErrors.CE_MISSING_RESULTSET, m_fieldSetName);
         }
         catch (SQLException e)
         {
            throw new PSDataExtractionException( IPSServerErrors.CE_SQL_ERRORS,
                  PSSqlException.getFormattedExceptionText(e));
         }
         finally
         {
            data.restoreResultSetContext();
         }
      }

      return m_isFieldShown;
   }

   /**
    * A ThreadLocal variable containing the list of strings representing the row
    * data created in {@link #addDataElement}. Initialized with a <code>null
    * </code> value, the list is recreated in each call to <code>addDataElement
    * </code> and then used in the {@link #addChoiceElement} to select the
    * appropriate elements.
    */
   private static ThreadLocal m_rows = new ThreadLocal();

   /**
    * Set in ctor, determines if this builder's field should ever be
    * displayed.  Never modified after that.  Used to hide this field if
    * showInPreview="no".
    */
   private boolean m_isFieldShown = true;

   /**
    * The name of this builder's fieldset.  Initialized in the ctor, never
    * <code>null</code> after that.
    */
   private String m_fieldSetName = null;
}


