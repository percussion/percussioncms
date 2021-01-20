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

import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSField;
import com.percussion.util.PSMapPair;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Utility class to assist in the building of the DisplayField element defined
 * in the sys_ContentEditor.dtd.
 */
public class PSDisplayFieldElementBuilder
{
   /**
    * Create a DisplayField element using the supplied parameters.
    * 
    * @param doc The document to use, may not be <code>null</code>.
    * @param displayType One of the <code>DISPLAY_TYPE_XXX</code> values, never
    * <code>null</code>.
    * @param displayLabel The display label to use, may be <code>null</code> or 
    * empty.
    * @param labelSource Indicates the overriding label source type.  May only 
    * be <code>null</code> if <code>displayLabel</code> is <code>null</code>.
    * If supplied, must be one of the <code>PSContentEditorMapper.SYSTEM</code>,
    * <code>PSContentEditorMapper.SHARED</code> or
    * <code>PSContentEditorMapper.LOCAL</code> values.
    * @param fieldValueType The field value type of this field, may not be 
    * <code>null</code>. Must be one of values in PSField.FIELD_VALUE_TYPE_ENUM.
    *  
    * @return The DisplayField element, never <code>null</code>.  This element
    * does not contain a Control child element, and thus will not be a valid
    * DisplayField element until supplied to a call to 
    * {@link #addControlElement(Document, Element, String, String, String, 
    * String, boolean, boolean, String, String) addControlElement}.
    */
   public static Element createDisplayFieldElement(Document doc, String 
      displayType, String displayLabel, String labelSource, 
      String fieldValueType)
   {
      // validate params
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      if (displayType == null)
         throw new IllegalArgumentException("displayType may not be null");
      if (!(displayType.equals(DISPLAY_TYPE_NORMAL) || displayType.equals(
         DISPLAY_TYPE_ERROR) || displayType.equals(DISPLAY_TYPE_HIDDEN)))
      {
         throw new IllegalArgumentException("invalid displayType value");
      }
      
      if (labelSource == null && displayLabel != null)
      {
         throw new IllegalArgumentException(
            "labelSource may not be null if displayType is not null");
      }
      else if (labelSource != null && !isValidSource(labelSource))
      {
         throw new IllegalArgumentException("invalid labelSource value");
      }
      
      Element dispNode = doc.createElement( DISPLAYFIELD_NAME );
      dispNode.setAttribute( DISPLAYTYPE_NAME, displayType);
      dispNode.setAttribute( DISPLAYFIELD_FIELDVALUETYPE_NAME, fieldValueType);

      if ( null != displayLabel)
      {
         Element label = doc.createElement(LABEL_NAME);
         label.appendChild( doc.createTextNode(displayLabel));
         label.setAttribute( SOURCE_TYPE_NAME, labelSource);
         dispNode.appendChild(label);
      }

      return dispNode;
   }

   /**
    * Create a DisplayField element using the supplied parameters.
    *
    *    @deprecated
    *    @see createDisplayFieldElement
    */
   public static Element createDisplayFieldElement(Document doc, String 
      displayType, String displayLabel, String labelSource)
   {
      
      Element dispNode = createDisplayFieldElement(doc, displayType, 
            displayLabel, labelSource, 
            PSField.FIELD_VALUE_TYPE_ENUM[PSField.FIELD_VALUE_TYPE_UNKNOWN]);

      return dispNode;
   }

   /**
    * Convenience version of {@link #createControlElement(Document, String, 
    * String, String, String, boolean, boolean, String, String) 
    * createControlElement()} that appends the created Control element to the 
    * supplied DisplayField element.  Only the extra parameter is described
    * below.
    * 
    * @param displayFieldEl The element to which the Control element is 
    * appended, must be a DisplayField element, usually returned by a call to
    * {@link #createDisplayFieldElement(Document, String, String, String) 
    * createDisplayFieldElement}, may not be <code>null</code>.
    */
   public static Element addControlElement(Document doc, Element displayFieldEl, 
      String controlName, String submitName, String dataType, String dimension, 
      boolean isReadOnly, boolean isRequired, String accessKey, 
      String clearBinaryParam)
   {
      // validate params - just check the element, delegate the rest to create
      // method
      if (displayFieldEl == null || !displayFieldEl.getNodeName().equals(
         DISPLAYFIELD_NAME))
      {
         throw new IllegalArgumentException("invalid displayFieldEl");
      }
      
      Element control = createControlElement(doc, controlName, submitName, 
         dataType, dimension, isReadOnly, isRequired, accessKey, 
         clearBinaryParam);
      displayFieldEl.appendChild(control);
      
      return control;
   }
   

   /**
    * Creates a Control element using the supplied parameters.
    * 
    * @param doc The document to use, may not be <code>null</code>.
    * @param controlName The name of the control, may not be <code>null</code> 
    * or empty.
    * @param submitName The submit name of the field this control will render,
    * may not be <code>null</code> or empty.
    * @param dataType The source of the field rendered by this control, one of
    * the <code>DATATYPE_xxx</code> values, never <code>null</code>.
    * @param dimension One of the <code>DIMENSION_xxx</code> values, never
    * <code>null</code>.
    * @param isReadOnly <code>true</code> if the control will not be editable,
    * <code>false</code> otherwise.
    * @param isRequired <code>true</code> if the field will require a value for 
    * the item to be transitioned, <code>false</code> if not.
    * @param accessKey The value to use to enable accessability key navigation
    * to this control, may be <code>null</code> or empty.
    * @param clearBinaryParam The name of the param to control if a binary
    * field should be cleared, may be <code>null</code> or empty.
    * 
    * @return The Control element, never <code>null</code>.
    */
   public static Element createControlElement(Document doc, String controlName, 
      String submitName, String dataType, String dimension, boolean isReadOnly, 
      boolean isRequired, String accessKey, String clearBinaryParam)
   {
      // validate params 
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
         
      if (controlName == null || controlName.trim().length() == 0)
         throw new IllegalArgumentException(
            "controlName may not be null or empty");
            
      if (submitName == null || submitName.trim().length() == 0)
         throw new IllegalArgumentException(
            "submitName may not be null or empty");
            
      if (dataType == null || !(dataType.equals(DATATYPE_LOCAL) || 
         dataType.equals(DATATYPE_SHARED) || dataType.equals(DATATYPE_SYSTEM)))
      {
         throw new IllegalArgumentException("invalid dataType");
      }
      
      if (dimension == null || !(dimension.equals(DIMENSION_SINGLE) || 
         dimension.equals(DIMENSION_ARRAY) || 
         dimension.equals(DIMENSION_TABLE)))
      {
         throw new IllegalArgumentException("invalid dimension");
      }
      

      String [][] controlAttribs =
      {
         { CONTROLNAME_NAME, controlName },
         { PARAMNAME_NAME, submitName },
         { DATATYPE_NAME, dataType },
         { DIMENSION_NAME, dimension },
         { READONLYFLAG_NAME, attribBooleanText(isReadOnly) },
         { REQUIREDFLAG_NAME, attribBooleanText(isRequired) }
      };

      Element control = createControlElement(doc, controlAttribs);

      // add optional attributes
      if (clearBinaryParam != null)
         control.setAttribute(CLEARBINARYPARAM_NAME, clearBinaryParam);

      if (accessKey != null)
         control.setAttribute(ACCESSKEY, accessKey);

      return control;
   }
   
   /**
    * Appends a ParamList element to the supplied Control element using the 
    * supplied params.
    * 
    * @param doc The doc to use, may not be <code>null</code>.
    * @param controlEl The element to which the created ParamList element is 
    * appended, may not be <code>null</code> and must be a Control element, 
    * usually returned by a call to {@link #addControlElement(Document, Element, 
    * String, String, String, String, boolean, boolean, String, String) 
    * addControlElement()}.
    * @param params An iterator over zero or more <code>PSMapPair</code> objects
    * where the key is the param name as a <code>String</code> and the value
    * is an <code>Object[2]</code> where index 0 contains the value and index
    * 1 contains the source type of the param (one of the  
    * <code>PSContentEditorMapper.SYSTEM</code>,
    * <code>PSContentEditorMapper.SHARED</code> or
    * <code>PSContentEditorMapper.LOCAL</code> values).  The object at index 0
    * may be <code>null</code>, in which case an empty string is used, otherwise
    * <code>toString()</code> is called on the object.  Never <code>null</code>.
    * 
    * @return The ParamList element, never <code>null</code>.
    */
   public static Element addParamListElement(Document doc, Element controlEl, 
      Iterator params)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      if (controlEl == null || !controlEl.getNodeName().equals(CONTROL_NAME))
         throw new IllegalArgumentException("invalid controlEl");
      if (params == null)
         throw new IllegalArgumentException("param may not be null");
      
      Element paramListEl = doc.createElement( "ParamList" );
      while ( params.hasNext())
      {
         Element paramEl = doc.createElement(
            PSEditorDocumentBuilder.PARAM_NAME);
         PSMapPair pair = (PSMapPair) params.next();
         Object[] pairValues = (Object[]) pair.getValue();

         Object o = pairValues[0];
         if ( null == o )
         {
            o = "";
         }
         paramEl.setAttribute( "name", (String) pair.getKey());
         String sourceType = (String) pairValues[1];
         if (sourceType != null && sourceType.trim().length() > 0)
         {
            if (!isValidSource(sourceType))
               throw new IllegalArgumentException(
                  "params contains an invalid source type");
            paramEl.setAttribute(SOURCE_TYPE_NAME, sourceType);
         }
         
         paramEl.appendChild( doc.createTextNode( o.toString()));
         paramListEl.appendChild( paramEl );
      }
      controlEl.appendChild( paramListEl );      
      
      return paramListEl;
   }   
   
   /**
    * Creates and appends a Value element to the supplied Control element using 
    * the specified value.
    *  
    * @param doc The doc to use, may not be <code>null</code>.
    * @param controlEl The element to which the created Value element is 
    * appended, may not be <code>null</code> and must be a Control element, 
    * usually returned by a call to {@link #addControlElement(Document, Element, 
    * String, String, String, String, boolean, boolean, String, String) 
    * addControlElement()}.     
    * @param value The value to append, may be <code>null</code> or empty.
    * 
    * @return The created element.
    */
   public static Element addDataElement(Document doc, Element controlEl, 
      String value)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      if (controlEl == null || !controlEl.getNodeName().equals(CONTROL_NAME))
         throw new IllegalArgumentException("invalid controlEl");
      
      Element dataNode = doc.createElement(DATA_NAME);
      dataNode.appendChild(doc.createTextNode(value));

      controlEl.appendChild(dataNode);

      return null;
   }
   
   /**
    * Convenience method to appends the supplied <code>choicesEl</code> to the 
    * supplied Control element, cloning and importing the element if the 
    * supplied <code>doc</code> is not the owner.
    * 
    * @param doc The doc to use, may not be <code>null</code>. 
    * @param controlEl The element to which the created Value element is 
    * appended, may not be <code>null</code> and must be a Control element, 
    * usually returned by a call to {@link #addControlElement(Document, Element, 
    * String, String, String, String, boolean, boolean, String, String) 
    * addControlElement()}.  
    * @param choicesEl The DisplayChoices element to append, may not be 
    * <code>null</code>.
    * 
    * @return Element The supplied element possibly cloned and imported into 
    * doc.
    */
   public static Element addChoiceElement(Document doc, Element controlEl, 
      Element choicesEl)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      if (controlEl == null || !controlEl.getNodeName().equals(CONTROL_NAME))
         throw new IllegalArgumentException("invalid controlEl");
      if (choicesEl == null || !choicesEl.getNodeName().equals(
         PSChoiceBuilder.DISPLAYCHOICES_NAME))
            throw new IllegalArgumentException("invalid choicesEl");
            
      return (Element)PSXmlDocumentBuilder.copyTree(doc, controlEl, 
         choicesEl, true);      
   }
   
   /**
    * Selects all matching choices within any DisplayChoices element found 
    * below the supplied <code>controlEl</code>.
    * 
    * @param controlEl The element in which the choices will be selected, may 
    * not be <code>null</code> and must be a Control element, 
    * usually returned by a call to {@link #addControlElement(Document, Element, 
    * String, String, String, String, boolean, boolean, String, String) 
    * addControlElement()}.   
    * @param rows A list of display values as <code>String</code> objects to 
    * match, never <code>null</code>, may be empty.
    */
   public static void selectChoices(Element controlEl, List rows)
   {
      // enforce params
      if (controlEl == null || !controlEl.getNodeName().equals(CONTROL_NAME))
         throw new IllegalArgumentException("invalid controlEl");
      if (rows == null)
         throw new IllegalArgumentException("rows may not be null");
      
      // no need if rows is empty
      if (rows.isEmpty())
         return;
         
      NodeList nodes = controlEl.getElementsByTagName(
         PSChoiceBuilder.DISPLAYCHOICES_NAME);
      int nodeCount = nodes.getLength();
      for (int row=0; row<rows.size() && nodeCount>0; row++)
      {
         String rowValue = (String) rows.get(row);
         for (int i=0; i<nodeCount; i++)
            PSChoiceBuilder.select((Element) nodes.item(i), rowValue);
      }
   }

   /**
    * Private ctor to enforce static use
    */
   private PSDisplayFieldElementBuilder()
   {
   }



   /**
    * Gets the name of the control contained by the specified displayNode.
    *
    * @param dispNode The display field element, may not be <code>null</code>,
    * must be a valid display field element.
    *
    * @return The name of the control contained in the display node, never
    * <code>null</code> or empty.
    */
   public static String getControlName(Element dispNode)
   {
      if (dispNode == null)
         throw new IllegalArgumentException("dispNode may not be null");
   
      PSXmlTreeWalker walker = new PSXmlTreeWalker( dispNode );
      Element control = walker.getNextElement(CONTROL_NAME, true);
      if (control == null)
         throw new IllegalArgumentException("dispNode not valid");
   
      String controlName = control.getAttribute(CONTROLNAME_NAME);
   
      if (controlName == null || controlName.trim().length() == 0)
         throw new IllegalArgumentException("dispNode not valid");
   
      return controlName;
   }
   
   /**
    * Determines if the supplied DisplayField element specifies a hidden field.
    * 
    * @param displayFieldEl The display field element, may not be 
    * <code>null</code>.
    * 
    * @return <code>true</code> if it is hidden, <code>false</code> otherwise.  
    */
   public static boolean isHiddenDisplayField(Element displayFieldEl)
   {
      if (displayFieldEl == null || !displayFieldEl.getNodeName().equals(
         DISPLAYFIELD_NAME))
      {
         throw new IllegalArgumentException("invalid displayFieldEl");
      }
      
      return (DISPLAY_TYPE_HIDDEN.equals(displayFieldEl.getAttribute(
         DISPLAYTYPE_NAME)));
   }

   /**
    * Helper method to create a control node and add the specified attributes.
    *
    * @param doc The doc to which the element will be added, may not be
    * <code>null</code>.
    * @param controlAttribs A two dimensional array of name-value pairs, each
    * specifying an attribute.  May not be <code>null</code>, may be empty.
    *
    * @return The control element, never <code>null</code>.
    */
   private static Element createControlElement(Document doc,
      String[][] controlAttribs)
   {
      if ( null == doc)
         throw new IllegalArgumentException( "doc may not be null" );
   
      if (controlAttribs == null)
         throw new IllegalArgumentException("controlAttribs may not be null");
   
      Element control = doc.createElement(CONTROL_NAME);
   
      for ( int i = 0; i < controlAttribs.length; ++i )
      {
         control.setAttribute( controlAttribs[i][0], controlAttribs[i][1] );
      }
   
      return control;
   }
   
   /**
    * Get the value for a boolean attribute.
    *
    * @param The flag that determines which string to return.
    *
    * @return The text to be used as the value for a boolean attribute.
    */
   private static String attribBooleanText( boolean b )
   {
      return b ? ATTRIB_BOOLEAN_TRUE : ATTRIB_BOOLEAN_FALSE;
   }

   /**
    * Creates a hidden display field element using a hidden control using
    * the supplied name and value.  No label will be supplied.
    *
    * @param doc The document to which the control is to be added.  May not be
    * <code>null</code>.
    * @param submitName The name of the control, never <code>null</code> or
    * empty.
    * @param value The value to set on the control, may not be 
    * <code>null</code>, may be empty. 
    * @param isReadOnly If <code>true</code>, the control will be created as
    * read only, if <code>false</code> it will not be created as read only.
    *
    * @return The display field element, never <code>null</code> or empty.
    */
   public static Element createHiddenFieldElement(Document doc, 
      String controlName, String submitName, String value, boolean isReadOnly)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
   
      if (controlName == null || controlName.trim().length() == 0)
         throw new IllegalArgumentException(
            "controlName may not be null or empty");
   
      if (submitName == null || submitName.trim().length() == 0)
         throw new IllegalArgumentException(
            "submitName may not be null or empty");
   
      if (value == null)
         throw new IllegalArgumentException("value may not be null");
   
      Element dispNode =  
         createDisplayFieldElement(doc, DISPLAY_TYPE_HIDDEN, null, null);

      Element control = PSDisplayFieldElementBuilder.addControlElement(doc, 
         dispNode, controlName, submitName, DATATYPE_SYSTEM, 
         PSDisplayFieldElementBuilder.DIMENSION_SINGLE, isReadOnly, true, null, 
         null);

      addDataElement(doc, control, value);
   
      return dispNode;
   }   
   
   /**
    * Determines if the supplied source is valid, e.g. one of the 
    * <code>PSContentEditorMapper.SYSTEM</code>,
    * <code>PSContentEditorMapper.SHARED</code> or
    * <code>PSContentEditorMapper.LOCAL</code> values.
    * 
    * @param source The source, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if it is valid, <code>false</code> if not.
    */
   private static boolean isValidSource(String source)
   {
      return source.equals(PSContentEditorMapper.LOCAL) || 
         source.equals(PSContentEditorMapper.SHARED) || 
         source.equals(PSContentEditorMapper.SYSTEM);
   }

   /**
    * Constant for the DisplayField element name.
    */
   public static final String DISPLAYFIELD_NAME = "DisplayField";

   /**
    * Constant for the displayType attribute of the DisplayField element.
    */
   public static final String DISPLAYTYPE_NAME = "displayType";

   /**
    * Constant for the fieldValueType attribute of the DisplayField element.
    */
   public static final String DISPLAYFIELD_FIELDVALUETYPE_NAME = "fieldValueType";

   /**
    * Constant for the DisplayLabel element name.
    */
   public static final String LABEL_NAME = "DisplayLabel";

   /**
    * Constant for the sourceType attribute name.
    */
   public static final String SOURCE_TYPE_NAME = "sourceType";
   
   
   /**
    * Constant for the paramName attribute of the Control element.
    */
   public static final String PARAMNAME_NAME = "paramName";
   
   /**
    * Constant for the dimension attribute of the Control element.
    */
   public static final String DIMENSION_NAME = "dimension";

   /**
    * Constant for the Value element of a Control.
    */
   public static final String DATA_NAME = "Value";

   /**
    * One of the possible values for the displayType attribute of the
    * display field node. Indicates to the stylesheet the field should not
    * be visible to the user when rendered.
    */
   public static final String DISPLAY_TYPE_HIDDEN = "sys_hidden";

   /**
    * One of the possible values for the displayType attribute of the
    * display field node. Indicates to the stylesheet the field should be
    * rendered, but how it's rendered may change to bring attention to the
    * field.
    */
   public static final String DISPLAY_TYPE_ERROR = "sys_error";

   /**
    * One of the possible values for the displayType attribute of the
    * display field node. Indicates to the stylesheet the field should
    * be rendered normally according to its control type.
    */
   public static final String DISPLAY_TYPE_NORMAL = "sys_normal";

   /**
    * One of the possible values for the dataType attribute. This means the
    * field came from the local definition.
    */
   public static final String DATATYPE_LOCAL = "sys_local";

   /**
    * One of the possible values for the dataType attribute. This means the
    * field came from the shared definition.
    */
   public static final String DATATYPE_SHARED = "sys_shared";

   /**
    * One of the possible values for the dataType attribute. This means the
    * field came from the system definition.
    */
   public static final String DATATYPE_SYSTEM = "sys_system";

   /**
    * Constant for the Control element name.
    */
   public static final String CONTROL_NAME = "Control";

   /**
    * One of the possible values for the dataDimension param. This means the
    * Value element will be a single value.
    */
   public static final String DIMENSION_SINGLE = "single";

   /**
    * One of the possible values for the dataDimension param. This means the
    * Value element will be an array of values.
    */
   public static final String DIMENSION_ARRAY = "array";

   /**
    * One of the possible values for the dataDimension param. This means the
    * Value element will be a table (2 dimensional array of values).
    */
   public static final String DIMENSION_TABLE = "table";

   /**
    * Name of the attribute of the element {@link CONTROL_NAME Control} 
    * representing the name of the control.
    * @see sys_ContentEditor.dtd file for more details.
    */
   static final String CONTROLNAME_NAME = "name";
   
   // private xml constants
   private static final String DATATYPE_NAME = "dataType";
   private static final String READONLYFLAG_NAME = "isReadOnly";
   private static final String REQUIREDFLAG_NAME = "isRequired";   
   private static final String CLEARBINARYPARAM_NAME = "clearBinaryParam";
   private static final String ACCESSKEY = "accessKey";
   
   private static final String ATTRIB_BOOLEAN_TRUE = "yes";
   private static final String ATTRIB_BOOLEAN_FALSE = "no";

}
