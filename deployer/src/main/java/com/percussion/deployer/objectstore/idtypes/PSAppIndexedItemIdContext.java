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
 * ID Context to represent an item whose context is determined only by its
 * ordinal position in its parent context.
 */
public class PSAppIndexedItemIdContext extends PSApplicationIdContext
{
   /**
    * Construct this context specifying its ordinal position in its parent 
    * context.
    * 
    * @param type One of the <code>TYPE_xxx</code> values.
    * @param index The index into the parent rule list this rule is from.  May 
    * not be less than 0.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSAppIndexedItemIdContext(int type, int index)
   {
      if (!validateType(type))
         throw new IllegalArgumentException("invalid type");
      
      if (!validateIndex(index))
         throw new IllegalArgumentException("index is invalid");
         
      m_type = type;
      m_index = index;
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
   public PSAppIndexedItemIdContext(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }
   
   /**
    * Get the index into the parent rule list this rule is from.
    * 
    * @return The index, >=0.
    */
   public int getIndex()
   {
      return m_index;
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
         key), new Object[] {String.valueOf(m_index)});
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
    * &lt;!ELEMENT PSXAppIndexedItemIdContext (PSXApplicationIDContext?)>
    * &lt;!ATTLIST PSXAppIndexedItemIdContext
    *    index CDATA #REQUIRED
    *    type CDATA #REQUIRED
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
      root.setAttribute(XML_ATTR_INDEX, String.valueOf(m_index));
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

      String strIndex = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_INDEX);
      m_index = -1;
      try 
      {
         m_index = Integer.parseInt(strIndex);
      }
      catch (NumberFormatException ex) 
      {
         // fall thru
      }
      
      if (!validateIndex(m_index))
      {
         Object[] args = {XML_NODE_NAME, XML_ATTR_INDEX, strIndex};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
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

      if (!(obj instanceof PSAppIndexedItemIdContext))
         throw new IllegalArgumentException("obj wrong type");

      PSAppIndexedItemIdContext other = (PSAppIndexedItemIdContext)obj;
      m_index = other.m_index;
      super.copyFrom(other);
   }
   
   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;

      if (!(obj instanceof PSAppIndexedItemIdContext))
         isEqual = false;
      else 
      {
         PSAppIndexedItemIdContext other = (PSAppIndexedItemIdContext)obj;
         if (m_index != other.m_index)
            isEqual = false;
         else if (!super.equals(other))
            isEqual = false;
      }

      return isEqual;
   }
   
   // see IPSDeployComponent
   public int hashCode()
   {
      return m_type + m_index + super.hashCode();
   }

   /**
    * Check the supplied index to see if it is valid (>=0)
    * 
    * @param index The index to check.
    * 
    * @return <code>true</code> if it is valid, <code>false</code> otherwise.
    */
   private boolean validateIndex(int index)
   {
      return index >= 0;
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
   public static final String XML_NODE_NAME = "PSXAppIndexedItemIdContext";
 
   /**
    * Index of this rule in its parent's list.  Intialized
    * during ctor, modfied only by calls to <code>copyFrom()</code>.
    */
   private int m_index;
   
   /**
    * Indicates which part of an application this object represents, one 
    * of the <code>TYPE_XXX</code> values.  Initialized during construction, 
    * only modified by a call to <code>copyFrom()</code>.
    */
   private int m_type;
   
   
   /**
    * Constant to indicate this context represents a conditional request
    */
   public static final int TYPE_CONDITIONAL_REQUEST = 0;
   
   /**
    * Constant to indicate this context represents an rule object
    */
   public static final int TYPE_RULE = 1;

   /**
    * Constant to indicate this context represents a conditional exit
    */
   public static final int TYPE_CUSTOM_ACTION_GROUP = 2;

   /**
    * Constant to indicate this context represents a custom action group
    */
   public static final int TYPE_CONDITIONAL_EXIT = 3;

   /**
    * Constant to indicate this context represents an conditional stylesheet
    * object.
    */
   public static final int TYPE_CONDITIONAL_STYLESHEET = 4;
   
   /**
    * Constant to indicate this context represents an conditional effect
    * object.
    */
   public static final int TYPE_CONDITIONAL_EFFECT = 5;
   
   /**
    * Constant to indicate this context represents an conditional extension
    * object.
    */
   public static final int TYPE_CONDITIONAL_EXTENSION = 6;
   
   /**
    * Constant to indicate this context represents a clone field override.
    */
   public static final int TYPE_CLONE_FIELD_OVERRIDE = 7;
   
   /**
    * Enumeration of string constants representing each of the 
    * <code>TYPE_XXX</code> values, for Xml serialization.  Index of each value
    * must match its corresponding <code>TYPE_xxx</code> constant value.
    */
   private static final String[] TYPE_ENUM = {"ConditionalRequest", "Rule", 
      "CustomActionGroup", "ConditionalExit", "ConditionalStylesheet", 
      "ConditionalEffect", "ConditionalExtension", "CloneFieldOverride"};
      
   // private xml constant
   private static final String XML_ATTR_TYPE = "type";
   private static final String XML_ATTR_INDEX = "index";
   
}
