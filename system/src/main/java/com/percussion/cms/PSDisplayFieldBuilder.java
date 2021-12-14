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

import com.percussion.data.IPSDataExtractor;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSDataExtractorFactory;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSRuleListEvaluator;
import com.percussion.data.PSViewEvaluator;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSChoices;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.design.objectstore.PSVisibilityRules;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSServerErrors;
import com.percussion.util.PSMapPair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This is the base class for all builders that create the DisplayField
 * element in the result document generated for a query request. Based on
 * the field definition, an executable format is created that is efficient to
 * process at run time.
 * <p>The display field is optionally created, and all appropriate child
 * elements and attributes are set according to the ContentEditor dtd.
 * <p>Derived classes have control over some of the content by overriding the
 * various add... methods.
 */
public class PSDisplayFieldBuilder implements IPSBuildStep
{
   /**
    * Creates the basic display node with no data, using the supplied
    * properties to set attributes and elements. Anything containing an
    * IPSReplacementValue will be processed at run time (when execute is
    * called). See the ContentEditor.dtd file for a def of the DisplayField
    * element. See {@link #initField(PSField, PSUISet, String,
    * PSEditorDocumentBuilder) initField} for a description of the declared
    * exceptions.
    *
    * @param field The def for the field used as the basis for this element.
    *    Never </code>null</code>.
    *
    * @param ui The interface definition for this row. Never </code>null</code>.
    *
    * @param parentBuilder This builder is always a single row in a larger
    *    document. The larger document is managed by this parentBuilder.
    *    Never <code>null</code>.
    */
   public PSDisplayFieldBuilder( PSField field, PSUISet ui,
         PSEditorDocumentBuilder parentBuilder )
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException
   {
      if ( null == field || null == ui || null == parentBuilder )
         throw new IllegalArgumentException( "One or more params was null." );

      initField( field, ui, PSDisplayFieldElementBuilder.DIMENSION_SINGLE, 
         parentBuilder );
   }


   /**
    * Creates the basic display node with no data, using the supplied
    * properties to set attributes and elements. Anything containing an
    * IPSReplacementValue will be processed at run time (when execute is
    * called). See the ContentEditor.dtd file for a def of the DisplayField
    * element. See {@link #initField(PSField, PSUISet, String,
    * PSEditorDocumentBuilder) initField} for a description of the declared
    * exceptions.
    *
    * @param fieldSet The def for the fieldset used as the basis for this
    *    element. Never </code>null</code>.
    *
    * @param ui The interface definition for this row. Never </code>null</code>.
    *
    * @param parentBuilder This builder is always a single row in a larger
    *    document. The larger document is managed by this parentBuilder.
    *    Never <code>null</code>.
    */
   public PSDisplayFieldBuilder( PSFieldSet fieldSet, PSUISet ui,
         PSEditorDocumentBuilder parentBuilder )
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException
   {
      if ( null == fieldSet || null == ui || null == parentBuilder )
         throw new IllegalArgumentException( "One or more params was null." );

      if ( null != ui.getControl())
      {
         m_paramExtractors =
               prepareCustomParams( ui.getControl().getParameters());
      }

      if ( fieldSet.getType() == PSFieldSet.TYPE_SIMPLE_CHILD )
      {
         PSField field = fieldSet.getSimpleChildField();

         if ( null == ui.getChoices())
         {
            throw new PSSystemValidationException(
                  IPSServerErrors.CE_MISSING_CHOICESET, fieldSet.getName());
         }

         initField( field, ui, PSDisplayFieldElementBuilder.DIMENSION_ARRAY, 
            parentBuilder );
         return;
      }

      // we have a complex child

      // allow label to be empty
      if ( null != ui.getLabel())
      {
         m_label = ui.getLabel().getText();
         if ( null != ui.getErrorLabel())
         {
            m_errLabel = ui.getErrorLabel().getText();
         }
         if ( null == m_errLabel || m_errLabel.trim().length() == 0 )
            m_errLabel = m_label;

         m_labelSrcType = ui.getLabelSourceType();
      }

      //Get the accessKey
      m_accessKey = ui.getAccessKey();


      if ( null != ui.getChoices())
      {
         throw new PSSystemValidationException(
               IPSServerErrors.CE_CHOICESET_NOT_SUPPORTED, fieldSet.getName());
      }

      m_controlName = ui.getControl().getName();
      if ( null == m_controlName || m_controlName.trim().length() == 0 )
      {
         throw new PSSystemValidationException(
               IPSServerErrors.CE_MISSING_CONTROL_NAME, fieldSet.getName());
      }
      m_dimension = PSDisplayFieldElementBuilder.DIMENSION_TABLE;
      m_isRequired = false;
      m_isReadOnly = true;
      m_dataType = getDataType( fieldSet );
      m_isVisible = true;
      m_submitName = fieldSet.getName();
      m_viewEvaluator = parentBuilder.getViewEvaluator();
   }


   /**
    * Creates the basic display node with no data, using the supplied
    * properties to set attributes and elements. Anything containing an
    * IPSReplacementValue will be processed at run time (when execute is
    * called). See the ContentEditor.dtd file for a def of the DisplayField
    * element.
    *
    * @param field The def for the field used as the basis for this element.
    *    Assumed not </code>null</code>.
    *
    * @param ui The interface definition. Assumed not </code>null</code>.
    *
    * @param dataDimension An indicator of the type of data that will be
    *    added for the value. Must be one of the DIMENSION_... constants.
    *
    * @param parentBuilder This builder is always a single row in a larger
    *    document. The larger document is managed by this parentBuilder.
    *    Assumed not <code>null</code>.
    *
    * @throws PSExtensionException If UDFs are used but they can't be loaded.
    *
    * @throws PSNotFoundException If a UDF can't be found.
    *
    * @throws PSSystemValidationException If there are any inconsistencies in the
    *    definitions.
    */
   private void initField( PSField field, PSUISet ui,
         String dataDimension, PSEditorDocumentBuilder parentBuilder )
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException
   {
      if ( null == dataDimension || !(dataDimension.equals(
         PSDisplayFieldElementBuilder.DIMENSION_SINGLE)
         || dataDimension.equals(PSDisplayFieldElementBuilder.DIMENSION_ARRAY)
         || dataDimension.equals(PSDisplayFieldElementBuilder.DIMENSION_TABLE)))
      {
         throw new IllegalArgumentException(
               "Invalid type for dataDimension (" + dataDimension + ")" );
      }

      // allow label to be empty
      if ( null != ui.getLabel())
      {
         m_label = ui.getLabel().getText();
         if ( null != ui.getErrorLabel())
         {
            m_errLabel = ui.getErrorLabel().getText();
         }
         if ( null == m_errLabel || m_errLabel.trim().length() == 0 )
            m_errLabel = m_label;
         m_labelSrcType = ui.getLabelSourceType();
      }
      m_submitName = field.getSubmitName();
      //Get the accessKey
      m_accessKey = ui.getAccessKey();
      m_choices = ui.getChoices();
      if (ui.getControl() == null)
         throw new PSSystemValidationException(
               IPSServerErrors.CE_MISSING_CONTROL, field.getSubmitName());
      m_controlName = ui.getControl().getName();
      if ( null == m_controlName || m_controlName.trim().length() == 0 )
         throw new PSSystemValidationException(
               IPSServerErrors.CE_MISSING_CONTROL_NAME, field.getSubmitName());
      m_dataType = getDataTypeText(field.getType());
      m_fieldValueTypeText = field.getFieldValueTypeText();
      m_dimension = dataDimension;
      // get default occurrence dimension
      m_isRequired = field.getOccurrenceDimension(null) ==
            PSField.OCCURRENCE_DIMENSION_REQUIRED;
      m_paramExtractors = prepareCustomParams( ui.getControl().getParameters());
      Iterator roRules = ui.getReadOnlyRules();
      m_isReadOnly = !parentBuilder.getDocContext().isEditMode();
      if ( roRules.hasNext())
         m_readOnlyHandler = new PSRuleListEvaluator( roRules );

      if ( null == field.getVisibilityRules())
         m_omit = false;
      else
         m_omit = field.getVisibilityRules().getDataHiding() ==
               PSVisibilityRules.DATA_HIDING_XML;

      m_visibilityHandler = new PSRuleListEvaluator(field.getVisibilityRules());
      m_clearBinaryParam = field.getClearBinaryParam();
      m_viewEvaluator = parentBuilder.getViewEvaluator();
   }

   /**
    * A convenience constructor for creating hidden fields. The supplied
    * control name must be able to handle a hidden field. No label will be
    * supplied.
    *
    * @param controlName The name of a control that can be found in the
    *    XSL control library. Never <code>null</code> or empty.
    *
    * @param submitName The name of the variable to use when submitting a
    *    value for this field to the server. Typically, the HTML parameter
    *    name. Never <code>null</code> or empty.
    *
    * @param parentBuilder This builder is always a single row in a larger
    *    document. The larger document is managed by this parentBuilder.
    *    Never <code>null</code>.
    */
   public PSDisplayFieldBuilder( String controlName, String submitName,
         PSEditorDocumentBuilder parentBuilder )
   {
      if ( null == controlName || controlName.trim().length() == 0
            || null == submitName || submitName.trim().length() == 0
            || null == parentBuilder )
      {
         throw new IllegalArgumentException(
               "One or more of the params was null or empty." );
      }
      m_submitName = submitName;
      m_controlName = controlName;
      m_isRequired = true;
      m_isVisible = false;
      m_dataType = PSDisplayFieldElementBuilder.DATATYPE_SYSTEM;
      m_dimension = PSDisplayFieldElementBuilder.DIMENSION_SINGLE;
      m_omit = false;
      m_isReadOnly = !parentBuilder.getDocContext().isEditMode();
      m_viewEvaluator = parentBuilder.getViewEvaluator();
   }


   /**
    * If the field has any associated data, this method should be overridden
    * by derived classes to set the correct value element, according to the
    * dtd. This is a no-op in this class.
    *
    * @param doc The document to use to create the node. It will not be
    *    modified. Never <code>null</code>.
    *
    * @param parent The node to which the data element will be added if one
    *    is created. Never <code>null</code>.
    *
    * @param data The data that is used to evaluate all run-time operations.
    *    If <code>null</code>, nothing is done.
    *
    * @param isNewDoc A flag to indicate whether the request is to modify
    *    existing content or create new content. Affects the behavior of
    *    default values.
    *
    * @return <code>true</code> if an element is added, <code>false</code>
    *    otherwise. <code>false</code> is returned unless overridden by a
    *    sub class..
    *
    * @throws PSDataExtractionException If any problems occur trying to get
    *    the values from the execution data.
    */
   protected boolean addDataElement( Document doc, Element parent,
         PSExecutionData data, boolean isNewDoc )
      throws PSDataExtractionException
   {
      if ( null == doc || null == parent || null == data )
         throw new IllegalArgumentException( "One or more params is null." );
      
      // suppress eclipse warning
      if (isNewDoc);
      
      return false;
   }


   // see IPSBuildStep for description
   public void execute(IPSBuildContext ctx, PSExecutionData data,
         boolean isNewDoc )
      throws PSDataExtractionException
   {
      if ( null == ctx || null == data )
         throw new IllegalArgumentException( "One or more params is null." );
      if ( null == ctx.getResultDocument())
      {
         throw new IllegalArgumentException(
            "Context document cannot be null." );
      }

      Element dispNode = createNode( ctx.getResultDocument(), data, isNewDoc,
            false );
      if ( null != dispNode )
      {
         if (PSDisplayFieldElementBuilder.isHiddenDisplayField(dispNode))
            ctx.addHiddenField(dispNode, m_controlName);
         else
            ctx.addVisibleField(dispNode, m_controlName);
      }
   }


   /**
    * Infers the type of a field set by looking at the type of fields it
    * contains.
    *
    * @param fs The fieldset for which you wish to infer its type. Assumed
    *    not <code>null</code>.
    *
    * @return The display version of the type, if it can be determined, the
    *    empty string otherwise.
    */
   private String getDataType( PSFieldSet fs )
   {
      Iterator fieldNames = fs.getNames();
      String dataType = "";
      while (fieldNames.hasNext())
      {
         Object o = fs.get((String) fieldNames.next());
         if ( o instanceof PSField )
            dataType = getDataTypeText(((PSField) o).getType());
      }
      return dataType.equals( UNKNOWN_TYPE_TEXT ) ? "" : dataType;
   }
   
   /**
    * Retrieves the name of the control associated with this field.
    * @return the control name, never <code>null</code> or empty.
    */
   protected String getControlName()
   {
      return m_controlName;
   }

   /**
    * Does the work of creating the node, with no data element. If the
    * visibility rules are false and the type is omit, then no element is
    * generated and <code>null</code> is returned.  Visibility rule checking is
    * subject to the value of the 
    * {@link IPSConstants#SKIP_FIELD_VISIBILITY_RULES} request private object.
    *
    * @param doc A valid document used to create the nodes. This should be
    *    the doc that the nodes will eventually be added to. Never <code>
    *    null</code>.
    *
    * @param data The data that is used to evaluate all run-time operations.
    *    Must not be <code>null</code>.
    *
    * @param isError A flag to indicate whether to build a standard document
    *    or a document returned after a validation error. The main difference
    *    is a possible label difference and an attribute indicating the type
    *    of node.
    *
    * @return A valid element if the visibilty rules indicate that it should
    *    should be created, otherwise, <code>null</code>.
    *
    * @throws PSDataExtractionException If any problems occur trying to get
    *    the values from the execution data.
    */
   private Element createNode( Document doc, PSExecutionData data,
         boolean isNewDoc, boolean isErrorDoc )
      throws PSDataExtractionException
   {
      if ( null == doc || null == data )
         throw new IllegalArgumentException( "one or more params was null" );

      // first check if we even need to do anything
      if (!showField(data))
         return null;

      boolean isVisible = m_isVisible;
      Object objSkipVisibilityRule = data.getRequest().getPrivateObject(
         IPSConstants.SKIP_FIELD_VISIBILITY_RULES);
      boolean skipVisibilityRule = (objSkipVisibilityRule instanceof Boolean) &&
         ((Boolean)objSkipVisibilityRule).booleanValue();
      
      if ( null != m_visibilityHandler)
      {
         if (skipVisibilityRule)
            isVisible = true;
         else
            isVisible = m_visibilityHandler.isMatch(data);
      }
      
      if ( !isVisible && m_omit )
         return null;

      // if still visible, check view
      if (isVisible)
         isVisible = m_viewEvaluator.isFieldVisible(m_submitName, data);

      String displayType = getDisplayTypeText( !isVisible, isErrorDoc );

      String label = null;
      if ( null != m_label )
         label = isErrorDoc ? m_errLabel : m_label;

      Element dispNode = PSDisplayFieldElementBuilder.createDisplayFieldElement(
         doc, displayType, label, m_labelSrcType, m_fieldValueTypeText);

      dispNode.appendChild( createControlNode( doc, data, isNewDoc ));
      return dispNode;
   }


   /**
    * Does the work of creating the Control node as defined in the
    * sys_ContentEditor.dtd.
    *
    * @param doc A valid document used to create the nodes. This should be
    *    the doc that the nodes will eventually be added to. Never <code>null
    *    </code>.
    *
    * @param data The data that is used to evaluate all run-time operations.
    *    Must not be <code>null</code>.
    *
    * @param isNewDoc A flag to indicate whether any info can be found in the
    *    execution data. See {@link #addDataElement(Document, Element,
    *    PSExecutionData, boolean) addDataElement} for more details on this
    *    parameter (it is passed through to this method).
    *
    * @return A valid element containing the data child, if an existing doc,
    *    or no data child if a new doc.
    *
    * @throws PSDataExtractionException If any problems occur trying to get
    *    the values from the execution data.
    */
   public Element createControlNode( Document doc, PSExecutionData data,
         boolean isNewDoc )
      throws PSDataExtractionException
   {
      if ( null == doc || null == data )
         throw new IllegalArgumentException( "one or more params was null" );

      Element control = PSDisplayFieldElementBuilder.createControlElement(doc, 
         m_controlName, m_submitName, m_dataType, m_dimension, isReadOnly(data), 
         m_isRequired, m_accessKey, m_clearBinaryParam);

      addDataElement( doc, control, data, isNewDoc );

      if (m_choices != null)
         addChoiceElement( doc, control, m_choices, data, isNewDoc );

      addParamElement( doc, control, m_paramExtractors.iterator(), data );
      return control;
   }

   /**
    * Override the read only property of this builder as determined when it
    * was constructed. If set to <code>true</code>, any existing read-only
    * rules are ignored.
    *
    * @param readOnly A flag to indicate the read-only property. If <code>
    *    true</code>, the generated field will be marked as read-only
    *    regardless of how it was created.
    */
   public void setReadOnly( boolean readOnly )
   {
      m_isReadOnly = readOnly;
   }

   /**
    * If the field has any associated data, this method should be overridden
    * by derived classes to set the correct value element, according to the
    * dtd.
    *
    * @param doc The document to use to create the node. It will not be
    *    modified. Never <code>null</code>.
    *
    * @param parent The node to which the new element will be added if it
    *    is created. Never <code>null</code>.
    *
    * @param params A list of IPSDataExtractor objects. Never <code>null</code>. 
    *    If empty, no element will be added.
    *
    * @param data The data that is used to evaluate all run-time operations.
    *    Must not be <code>null</code>.
    *
    * @return <code>true</code> if an element is added, <code>false</code>
    *    otherwise. Always <code>false</code> for this class.
    *
    * @throws PSDataExtractionException If any problems occur trying to get
    *    the values from the execution data.
    */
   protected boolean addParamElement( Document doc, Element parent,
         Iterator params, PSExecutionData data )
      throws PSDataExtractionException
   {
      if ( null == doc || null == parent || null == params || null == data )
         throw new IllegalArgumentException( "one or more params was null" );

      if ( !params.hasNext())
         return false;

      // create copy of param list with extracted values
      List newParamList = new ArrayList();
      while ( params.hasNext())
      {
         PSMapPair pair = (PSMapPair) params.next();
         Object[] pairValues = (Object[]) pair.getValue();

         Object o = ((IPSDataExtractor) pairValues[0]).extract( data );
         if ( null == o )
         {
            o = "";
         }
         PSMapPair newPair = new PSMapPair(pair.getKey(), 
            new Object[]{o, pairValues[1]});
         newParamList.add(newPair);
      }
      
      // create params el
      PSDisplayFieldElementBuilder.addParamListElement(doc, parent, 
         newParamList.iterator());
      
      return true;
   }

   /**
    * A field can be a user entered value or a set of options the user
    * can choose from. This method adds the set of choices, or a link for the
    * stylesheet to use to get the set of choices.  Is called immediately
    * following {@link #addDataElement}.
    *
    * @param doc The document to use to create the node. It will not be
    *    modified. Never <code>null</code>.
    *
    * @param parent The node to which the data element will be added if one
    *    is created. Never <code>null</code>.
    *
    * @param choices The choice set definition which defines where to get the
    *    choices and how to order them. Never <code>null</code>.
    *
    * @param data The data that is used to evaluate all run-time operations.
    *    Must not be <code>null</code>.
    *
    * @param isNewDoc <code>true</code> if this is for a new document,
    *    <code>false</code> otherwise.
    *
    * @return <code>true</code> if an element is added, <code>false</code>
    *    otherwise. Returns <code>false</code> by default.
    *
    * @throws PSDataExtractionException If any problems occur trying to get
    *    the values from the execution data.
    */
   public boolean addChoiceElement(Document doc, Element parent,
         PSChoices choices, PSExecutionData data, boolean isNewDoc)
      throws PSDataExtractionException
   {
      if (null == doc || null == parent || null == choices || null == data)
         throw new IllegalArgumentException("one or more params was null");

      return PSChoiceBuilder.addChoiceElement(doc, parent, choices, data,
         isNewDoc, false);
   }

   /**
    * Determines if this field should be displayed at runtime.  This method
    * will always return <code>true</code>, but can be overriden by derrived
    * classes to appropriate hide themselves.  Any class that is hiding itself
    * must handle popping any resultsets off the stack in the execution data
    * that would normally not be left after a call to {@link #addDataElement}.
    *
    * @param data The execution data, may not be <code>null</code>.
    *
    * @throws PSDataExtractionException if there is an error popping the
    * resultset from the stack.
    */
   protected boolean showField(PSExecutionData data)
      throws PSDataExtractionException
   {
      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      return true;
   }

   /**
    * Gets this builder's view evaluator, used by derived classes to determine
    * if fields should be visible, and to get the name to use for the view
    * parameter when creating action links.
    *
    * @return The view evaluator, never <code>null</code>.
    */
   protected PSViewEvaluator getViewEvaluator()
   {
      return m_viewEvaluator;
   }

   /**
    * Builds a list of extractors for the supplied parameter set.
    *
    * @param paramSet The set of params, assumed not <code>null</code>, may be
    *    empty.
    *
    * @return A List containing 0 or more PSMapPair objects. Each pair
    *    contains the parameter name as the key and an IPSDataExtractor as the
    *    value. Never <code>null</code>.
    */
   private List prepareCustomParams( Iterator paramSet )
   {
      List paramExtractors = new ArrayList();
      while ( paramSet.hasNext())
      {
         PSParam param = (PSParam) paramSet.next();
         IPSReplacementValue value = param.getValue();
         if ( null == value )
         {
            value = new PSTextLiteral( "" );
         }

         IPSDataExtractor extractor = null;
         try
         {
            extractor =
                  PSDataExtractorFactory.createReplacementValueExtractor( value );
         }
         catch ( IllegalArgumentException e )
         {
            throw new IllegalArgumentException( e.getLocalizedMessage());
         }

         Object[] array =
         {
            extractor,
            param.getSourceType()
         };

         paramExtractors.add( new PSMapPair( param.getName(), array ));
      }
      return paramExtractors;
   }

   /**
    * Determines the text value to use for the displayType attribute, based on
    * the 2 flags. There are 3 allowed values as defined by the dtd.
    *
    * @param isHidden A flag indicating whether this node is hidden or not.
    *    Hidden nodes are not visible to the end user.
    *
    * @param isError A flag indicating whether this node had a validation
    *    error. If a node is hidden, the error flag is ignored.
    *
    * @return The text to use for the displayType attribute.
    */
   private String getDisplayTypeText( boolean isHidden, boolean isError)
   {
      String value;
      if ( isHidden )
         value = PSDisplayFieldElementBuilder.DISPLAY_TYPE_HIDDEN;
      else if ( isError )
         value = PSDisplayFieldElementBuilder.DISPLAY_TYPE_ERROR;
      else
         value = PSDisplayFieldElementBuilder.DISPLAY_TYPE_NORMAL;
      return value;
   }

   /**
    * Translates a type code into a string suitable for writing to the output
    * document as the value of the dataType attribute of this node. If the
    * type can't be found, "UNKNOWN" is returned.
    *
    * @param dataType One of the DATATYPE_... types in this method.
    *
    * @return The string to use as the attribute value. Never empty. If the
    *    type is not recognized {@link #UNKNOWN_TYPE_TEXT } is returned.
    */
   private String getDataTypeText( int dataType )
   {
      String value;
      switch ( dataType )
      {
         case PSField.TYPE_LOCAL:
            value = PSDisplayFieldElementBuilder.DATATYPE_LOCAL;
            break;

         case PSField.TYPE_SYSTEM:
            value = PSDisplayFieldElementBuilder.DATATYPE_SYSTEM;
            break;

         case PSField.TYPE_SHARED:
            value = PSDisplayFieldElementBuilder.DATATYPE_SHARED;
            break;

         default:
            value = UNKNOWN_TYPE_TEXT;
      }

      return value;
   }


   /**
    * Determines whether this field should be read only for this request
    * by evaluating the read-only handler. If there is no handler, <code>
    * false</code> is returned.
    *
    * @param data The data context to use while checking the conditions.
    *    Assumed not to be <code>null</code>.
    *
    * @return <code>true</code> if the field should be marked read-only,
    *    <code>false</code> otherwise.
    */
   private boolean isReadOnly( PSExecutionData data )
   {
      if ( m_isReadOnly )
         return true;
      else if ( null == m_readOnlyHandler )
         return false;
      else
         return m_readOnlyHandler.isMatch( data );
   }

   
   /**
    * The name of the control used to display this field. Never <code>null
    * </code> or empty after it is assigned at construction.
    */
   private String m_controlName;

   /**
    * The name to be used when a value for this field is submitted to the
    * server. Typically, the Html parameter name. Never empty after it is
    * assigned at construction.
    */
   private String m_submitName;

   /**
    * The value for the data type attribute (system, shared or user). Never
    * empty after construction.
    */
   private String m_dataType;

   /**
    * The value for the field value type attribute (unknown,content,meta). 
    * Never empty after construction.
    */
   private String m_fieldValueTypeText = 
      PSField.FIELD_VALUE_TYPE_ENUM[PSField.FIELD_VALUE_TYPE_UNKNOWN];

   /**
    * One of the DIMENSION_xxx values which defines this particular builder.
    * Always valid after construction.
    */
   private String m_dimension;

   /**
    * This flag indicates whether this field is required to have a value. It
    * has no effect on the behavior of this object, it is just passed thru to
    * the output doc.
    */
   private boolean m_isRequired;

   /**
    * This flag indicates what the default visibility is. If the field has
    * visibility rules, they will override this value.
    */
   private boolean m_isVisible;

   /**
    * If a field type supports choices, they are saved here. Choices are an
    * ordered set of possible values for a field. Set during construction,
    * then immutable.
    */
   private PSChoices m_choices;

   /**
    * These are custom properties for the associated control. They are not
    * used by the engine, they are just passed through to the output doc.
    * Never <code>null</code>, may be empty. Immutable after construction.
    * The list contains <code>PSMapPair</code> objects, the key is the
    * parameter name as <code>java.lang.String</code> and the value an object
    * array where element 0 is the extractor
    * (<code>com.percussion.data.IPSDataExtractor</code>) and element 1 the
    * source type as <code>java.lang.String</code>.
    */
   private List m_paramExtractors = new ArrayList();

   /**
    * The label to use for normal display. Either <code>null</code> or non-
    * empty (after construction). It should only be <code>null</code> if the
    * control is hidden.
    */
   private String m_label;

   /**
    * The label to use if this field had a validation error. Either <code>null
    * </code> or non-empty (after construction). It should only be <code>null
    * </code> if the control is hidden.
    */
   private String m_errLabel;

   /**
    * If <code>false</code>, then the m_readOnlyHandler is used to determine
    * the read-only state.
    */
   private boolean m_isReadOnly = false;

   /**
    * Access key character for the control.
    */
   private String m_accessKey;


   /**
    * Contains the executable version of the read only rule set. If there are
    * no rules, it is <code>null</code>. You can easily make a field read-only
    * by creating the evaluator with a <code>null</code> argument (which always
    * evaluates to <code>true</code>.
    */
   private PSRuleListEvaluator m_readOnlyHandler;

   /**
    * A flag indicating what to do if the visibility rules evaluate to <code>
    * false</code>. If this flag is <code>true</code>, then this field is
    * not included in the output document.
    */
   private boolean m_omit;

   /**
    * If not <code>null</code>, this object is evaluated when this step is
    * executed to determine if this field should be shown.
    */
   private PSRuleListEvaluator m_visibilityHandler;

   /**
    * The parameter name that will be used to inidicate if a binary field is to
    * be cleared during update.  May be set during construction, may be <code>
    * null</code> or empty.
    */
   private String m_clearBinaryParam = null;

   /**
    * Indicates the overriding label source type.
    * It's initialized in the <code>PSDisplayFieldBuilder</code> constructor.
    * Might be <code>null</code> if no label is defined.
    */
   private String m_labelSrcType = null;

   /**
    * Used to determine if fields should be visible at runtime, and to determine
    * the name to use for the view parameters when constructing actionlinks.
    * Initialized during construction, never <code>null</code> or modified after
    * that.
    */
   private PSViewEvaluator m_viewEvaluator;


   private static final String UNKNOWN_TYPE_TEXT = "UNKNOWN";
}


