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

import java.text.MessageFormat;

/**
 * Context to represent an item in an application identified only by its name.
 */
public class PSAppNamedItemIdContext extends PSApplicationIdContext
{
   /**
    * Construct this context from its name and type.
    * 
    * @param type One of the <code>TYPE_xxx</code> values.
    * @param name The name of the item to which this context refers.  May not be 
    * <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>name</code> is invalid.
    */
   public PSAppNamedItemIdContext(int type, String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
            "name may not be null or empty");
      
      if (!validateType(type))
         throw new IllegalArgumentException("invalid type");
      
      m_name = name;
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
   public PSAppNamedItemIdContext(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the name of the item to which this context refers.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
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
      String key = "appIdCtx" + TYPE_ENUM[m_type];
      
      String text = MessageFormat.format(getBundle().getString(
         key), new Object[] {m_name});
      text = addParentDisplayText(text);
      
      return text;
   }
   
   // see base class
   public String getIdentifier()
   {
      // only return param and property names
      return (m_type == TYPE_PARAM || m_type == TYPE_PSPROPERTY) ? m_name : 
         null;
   }
   
   //see PSApplicationIdContext
   public void updateCtxValue(Object value)
   {
      if (value == null)
         throw new IllegalArgumentException("value may not be null");
      
      if (!(value instanceof String))
         throw new IllegalArgumentException(
            "value must be instanceof String");

      // only simple child type contains values
      if (m_type == TYPE_SIMPLE_CHILD_VALUE)
         m_name = (String) value;
      
      // now notify listeners
      notifyCtxChangeListeners(this);
   }   
   
   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <!--
    *    PSXApplicationIdContext is a place holder for the root node of the XML
    *    representation of any class derived from PSApplicationIdContext that
    *    is this context's parent context.
    * -->
    * <pre><code>
    * &lt;!ELEMENT PSXAppNamedItemIdContext (PSXApplicationIDContext?)>
    * &lt;!ATTLIST PSXAppNamedItemIdContext
    *    type CDATA #REQUIRED
    *    name CDATA #REQUIRED
    * >
    * </code></pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc should not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, m_name);
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

      m_name = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_NAME);
      
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

      if (!(obj instanceof PSAppNamedItemIdContext))
         throw new IllegalArgumentException("obj wrong type");

      PSAppNamedItemIdContext other = (PSAppNamedItemIdContext)obj;
      m_name = other.m_name;
      m_type = other.m_type;
      super.copyFrom(other);
   }
   
   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;

      if (!(obj instanceof PSAppNamedItemIdContext))
         isEqual = false;
      else 
      {
         PSAppNamedItemIdContext other = (PSAppNamedItemIdContext)obj;
         if (!m_name.equals(other.m_name))
            isEqual = false;
         else if (m_type != other.m_type)
            isEqual = false;
         else if (!super.equals(other))
            isEqual = false;
      }

      return isEqual;
   }
   
   public int hashCode()
   {
      return m_name.hashCode() + m_type + super.hashCode();
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
    * Name of the field this context represents, never <code>null</code> or 
    * empty after ctor, may be modified by a call to <code>copyFrom()</code>.
    */
   private String m_name;
   
   /**
    * Indicates which part of an application this object represents, one 
    * of the <code>TYPE_XXX</code> values.  Initialized during construction, 
    * only modified by a call to <code>copyFrom()</code>.
    */
   private int m_type;
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXAppNamedItemIdContext";
   
   /**
    * Constant to indicate this context represents a content editor field.
    */
   public static final int TYPE_CE_FIELD = 0;
   
   /**
    * Constant to indicate this context represents a control in a ui set.
    */
   public static final int TYPE_CONTROL = 1;
   
   /**
    * Constant to indicate this context represents a content editor display
    * mapping.
    */
   public static final int TYPE_DISPLAY_MAPPING = 2;
   
   /**
    * Constant to indicate this context represents a content editor field set.
    */
   public static final int TYPE_FIELD_SET = 3;
   
   /**
    * Constant to indicate this context represents a <code>PSParam</code> 
    * object.
    */
   public static final int TYPE_PARAM = 4;
   
   /**
    * Constant to indicate this context represents an applciation flow
    * object.
    */
   public static final int TYPE_APP_FLOW = 5;

   /**
    * Constant to indicate this context represents an stylesheet set
    * object.
    */
   public static final int TYPE_STYLESHEET_SET = 6;
   
   /**
    * Constant to indicate this context represents a result page object.
    */
   public static final int TYPE_RESULT_PAGE = 7;
   
   /**
    * Constant to indicate this context represents a set of input data exits
    * for a command in the system def.
    */
   public static final int TYPE_SYS_DEF_INPUT_DATA_EXITS = 8;

   /**
    * Constant to indicate this context represents a set of result data exits
    * for a command in the system def.
    */
   public static final int TYPE_SYS_DEF_RESULT_DATA_EXITS = 9;
   
   /**
    * Constant to indicate this context represents a set of init params
    * for a command in the system def.
    */
   public static final int TYPE_SYS_DEF_INIT_PARAMS = 10;
   
   /**
    * Constant to indicate this context represents a <code>PSProperty</code>
    * object
    */
   public static final int TYPE_PSPROPERTY = 11;
   
   /**
    * Constant to indicate this context represents a <code>PSFunctionCall</code>
    * object
    */
   public static final int TYPE_FUNCTION_CALL = 12;
   
   /**
    * Constant to indicate this context represents a <code>PSProcessCheck</code>
    * object
    */
   public static final int TYPE_PROCESS_CHECK = 13;
   
   /**
    * Constant to indicate this context represents a content item field
    */
   public static final int TYPE_ITEM_FIELD = 14;
   
   /**
    * Constant to indicate this context represents a child item 
    */
   public static final int TYPE_CHILD_ITEM = 15;   
   
   /**
    * Constant to indicate this context represents a child item 
    */
   public static final int TYPE_SIMPLE_CHILD_VALUE = 16;   

   /**
    * Enumeration of string constants representing each of the
    * <code>TYPE_XXX</code> values, for Xml serialization.  Index of each value
    * must match its corresponding <code>TYPE_xxx</code> constant value.
    */
   private static final String[] TYPE_ENUM = {"CEField", "Control",
      "DisplayMapping", "FieldSet", "Param", "AppFlow",
      "CommandHandlerStylesheets", "ResultPage", "SysDefInputDataExitCmd",
      "SysDefResultDataExitCmd", "SysDefInitParamCmd", "PSProperty",
      "PSFunctionCall", "PSProcessCheck", "ItemFieldValue", "ChildItem", 
      "SimpleChildValue"};
      
   // private xml constant
   private static final String XML_ATTR_TYPE = "type";
   private static final String XML_ATTR_NAME = "name";
   
}
