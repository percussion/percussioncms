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

package com.percussion.deployer.objectstore.idtypes;

import com.percussion.deployer.objectstore.IPSDeployComponent;
import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ResourceBundle;

/**
 * Context to represent an item in a contenteditor that may contain other 
 * contexts, but is really a placeholder for other commonly used objects and 
 * does not have any data of its own.
 */
public class PSAppCEItemIdContext extends PSApplicationIdContext
{
   /**
    * Construct this context using the supplied type
    * 
    * @param type One of the <code>TYPE_xxx</code> values.
    * 
    * @throws IllegalArgumentException if <code>type</code> is invalid.
    */
   public PSAppCEItemIdContext(int type)
   {
      if (!validateType(type))
         throw new IllegalArgumentException("invalid type");
      
      m_type = type;
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSAppCEItemIdContext(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }
   
   /**
    * Get the type of field item to which this context refers
    * 
    * @return The type, one of the <code>TYPE_XXX</code> values.
    */
   public int getType()
   {
      return m_type;
   }
   
   //see PSApplicationIdContext
   public String getDisplayText()
   {
      ResourceBundle bundle = getBundle();
      String text = null;
      text = bundle.getString("appIdCtxCE" + TYPE_ENUM[m_type]);
      text = addParentDisplayText(text);
      
      return text;
   }
   
   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <!--
    *    PSXApplicationIdContext is a place holder for the root node of the XML
    *    representation of any class derived from PSApplicationIdContext that
    *    is this context's parent context.
    * -->
    * <pre><code>
    * &lt;!ELEMENT PSXAppCEFieldItemIdContext (PSXApplicationIDContext?)>
    * &lt;!ATTLIST PSXAppCEFieldItemIdContext
    *    type CDATA #REQUIRED
    * </code></pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc should not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_TYPE, TYPE_ENUM[m_type]);
      PSApplicationIdContext parent = getParentCtx();
      if (parent != null)
         root.appendChild(parent.toXml(doc));

      return root;
      
   }

   /**
    * Restores this object's state from its XML representation.  See
    * {@link #toXml(Document)} for format of XML.  See
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode should not be null");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      String strType = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_TYPE);
      m_type = -1;
      for (int i = 0; i < TYPE_ENUM.length && m_type == -1; i++) 
      {
         if (TYPE_ENUM[i].equals(strType))
            m_type = i;
      }
      if (!validateType(m_type))
      {
         Object[] args = {XML_NODE_NAME, XML_ATTR_TYPE, strType};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element ctxEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (ctxEl != null)
         setParentCtx(PSApplicationIDContextFactory.fromXml(ctxEl));
   }
   
   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj may not be null");

      if (!(obj instanceof PSAppCEItemIdContext))
         throw new IllegalArgumentException("obj wrong type");

      PSAppCEItemIdContext other = (PSAppCEItemIdContext)obj;
      m_type = other.m_type;
      super.copyFrom(other);
   }
   
   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;

      if (!(obj instanceof PSAppCEItemIdContext))
         isEqual = false;
      else 
      {
         PSAppCEItemIdContext other = (PSAppCEItemIdContext)obj;
         if (m_type != other.m_type)
            isEqual = false;
         if (!super.equals(other))
            isEqual = false;
      }
      
      return isEqual;
   }
   
   // see IPSDeployComponent
   public int hashCode()
   {
      return m_type + super.hashCode();
   }
   
   /**
    * Validates the supplied type is one of the <code>TYPE_XXX</code> values.
    * 
    * @param type The value to check.
    * 
    * @return <code>true</code> if the type is valid, <code>false</code>
    * otherwise.
    */
   private boolean validateType(int type)
   {
      return type >=0 && type < TYPE_ENUM.length;
   }
   
   
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXAppCEItemIdContext";
   
   
   /**
    * Indicates which part of a <code>PSField</code> this object represents, one 
    * of the <code>TYPE_XXX</code> values.  Initialized during construction, 
    * only modified by a call to <code>copyFrom()</code>.
    */
   private int m_type;
   
   /**
    * Constant to indicate this context represents the default value of a field.
    */
   public static final int TYPE_DEFAULT_VALUE = 0;
   
   /**
    * Constant to indicate this context represents the input translation of a
    * field.
    */
   public static final int TYPE_INPUT_TRANSLATION = 1;
   
   /**
    * Constant to indicate this context represents the output translation of a
    * field.
    */
   public static final int TYPE_OUTPUT_TRANSLATION = 2;
   
   /**
    * Constant to indicate this context represents the validation rule of a
    * field.
    */
   public static final int TYPE_VALIDATION_RULE = 3;
   
   /**
    * Constant to indicate this context represents the visibility rule of a
    * field.
    */
   public static final int TYPE_VISIBILITY_RULE = 4;
   
   /**
    * Constant to indicate this context represents an applywhen from a
    * field.
    */
   public static final int TYPE_APPLY_WHEN = 5;
   
   /**
    * Constant to indicate this context represents a default UI from a UI
    * defition.
    */
   public static final int TYPE_DEFAULT_UI = 6;
   
   /**
    * Constant to indicate this context represents a choices from a ui set.
    */
   public static final int TYPE_CHOICES = 7;
   
   /**
    * Constant to indicate this context represents the read only rules from a 
    * ui set.
    */
   public static final int TYPE_READ_ONLY_RULES = 8;
   
   /**
    * Constant to indicate this context represents a data locator from a 
    * content editor.
    */
   public static final int TYPE_DATA_LOCATOR = 9;
   
   /**
    * Constant to indicate this context represents a choice filter from a 
    * content editor.
    */
   public static final int TYPE_CHOICE_FILTER = 10;
   
   /**
    * Enumeration of string constants representing each of the 
    * <code>TYPE_XXX</code> values, for Xml serialization.  Index of each value
    * must match its corresponding <code>TYPE_xxx</code> constant value.
    */
   private static final String[] TYPE_ENUM = {"DefaultValue", 
      "InputTranslation", "OutputTranslation", "FieldValidation", 
      "FieldVisibility", "ApplyWhen", "DefaultUI", "Choices", "ReadOnlyRules",
      "DataLocator", "ChoiceFilter"};
      
   // private xml constant
   private static final String XML_ATTR_TYPE = "type";
      
}
