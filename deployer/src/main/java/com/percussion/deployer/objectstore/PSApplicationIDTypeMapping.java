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

package com.percussion.deployer.objectstore;

import com.percussion.deployer.objectstore.idtypes.PSApplicationIDContextFactory;
import com.percussion.deployer.objectstore.idtypes.PSApplicationIdContext;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The PSApplicationIDTypeMapping class is to encapsulate ID and Type mapping
 * information.
 */
public class PSApplicationIDTypeMapping  implements IPSDeployComponent
{

   /**
    * Construct initial ID-Type mapping object.
    *
    * @param ctx The context of the id this mapping represents. May not be 
    * <code>null</code>.
    * @param value The value of the literal. It may not be <code>null</code> or 
    * empty.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public PSApplicationIDTypeMapping(PSApplicationIdContext ctx, String value)
   {
      if ( ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      if ( value == null || value.trim().length() == 0 )
         throw new IllegalArgumentException("Value may not be null or empty");

      m_ctx = ctx;
      m_value = value;
      m_type = TYPE_UNDEFINED;
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
   public PSApplicationIDTypeMapping(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the context of this mapping.
    * 
    * @return The context, never <code>null</code>.
    */
   public PSApplicationIdContext getContext()
   {
      return m_ctx;
   }
   
   /**
    * Get the context of this id as text to display to an end user..
    *
    * @return the display text, never <code>null</code> or empty.
    */
   public String getContextDisplayText()
   {
      return m_ctx.getDisplayText();
   }

   /**
    * Get the value of the object.
    *
    * @return the value of the object, never <code>null</code> or empty.
    */
   public String getValue()
   {
      return m_value;
   }

   /**
    * Get the type of the object.
    *
    * @return The deployable object type, <code>TYPE_NONE</code> to indicate it
    * does not specify a deployable object, or <code>TYPE_UNDEFINED</code> if
    * the type has not been set (i.e. it is incomplete). It can never be
    * <code>null</code> or empty.
    */
   public String getType()
   {
      return m_type;
   }

   /**
    * Set the type for the object.
    *
    * @param    type    A valid object type id. It may not empty or
    * <code>null</code>
    */
   public void setType(String type)
   {
      if ( type == null || type.trim().length() == 0 )
         throw new IllegalArgumentException("Type may not be null or empty");

      m_type = type;
   }
   
   /**
    * Set the value of this type mapping.  
    * 
    * @param value The new value, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>value</code> is invalid.
    */
   public void setValue(String value)
   {
      if (value == null || value.trim().length() == 0)
         throw new IllegalArgumentException("value may not be null or empty");
      
      m_value = value;
      m_hasNewValue = true;
   }
   
   /**
    * Determines if this mapping's type has been set.
    * 
    * @return <code>true</code> if the type has been defined, <code>false</code>
    * if not.
    */
   public boolean isComplete()
   {
      return !m_type.equals(TYPE_UNDEFINED);
   }
   
   /**
    * Determines if this mapping's type has been set to {@link #TYPE_NONE}, 
    * indicating it's value does not specifiy an id.
    * 
    * @return <code>true</code> if this mapping specifies an id type, 
    * <code>false</code> otherwise.
    */
   public boolean isIdType()
   {
      return !m_type.equals(TYPE_NONE);
   }

   /**
    * Gets the parent dependency id of the dependency if one has been set.  Set
    * {@link #setParent(String, String)} for more info.
    * 
    * @return The parent id, may be <code>null</code>, never empty.
    */
   public String getParentId()
   {
      return m_parentId;
   }
   
   /**
    * Gets the parent dependency type of the dependency if one has been set.  
    * Set {@link #setParent(String, String)} for more info.
    * 
    * @return The parent type, may be <code>null</code>, never empty.  Will only
    * be <code>null</code> if {@link #getParentId()} returns <code>null</code>.
    */
   public String getParentType()
   {
      return m_parentType;
   }
   
   /**
    * Sets id and type of the parent dependency id of the dependency specified 
    * by {@link #getValue()} and {@link #getType()} if it supports parent ids 
    * (see {@link PSDependency#supportsParentId()}).  
    * 
    * @param parentId The parent id, may be <code>null</code>, never empty.
    * @param parentType The parent type, may be <code>null</code>, never empty.
    * May not be <code>null</code> if <code>parentId</code> is not 
    * <code>null</code>.  Ignored if <code>parentId</code> is <code>null</code>.
    * 
    * @throws IllegalArgumentException If any param is invalid.
    */
   public void setParent(String parentId, String parentType)
   {
      if (parentId != null && parentId.trim().length() == 0)
         throw new IllegalArgumentException("parentId may not be empty");
      
      if (parentId != null && (parentType == null || 
         parentId.trim().length() == 0))
      {
         throw new IllegalArgumentException(
            "parentType may not be null or empty");
      }
      
      m_parentId = parentId;
      if (parentId != null)
         m_parentType = parentType;
      else
         m_parentType = null;
   }
   
   
   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * <!--
    *    PSXApplicationIdContext is a place holder for the root node of the XML
    *    representation of any class derived from PSApplicationIdContext.
    * -->
    * &lt;!ELEMENT PSXApplicationIDTypeMapping (PSXApplicationIdContext)>
    * &lt;!ATTLIST PSXApplicationIDTypeMapping
    *    value CDATA #REQUIRED
    *    type CDATA #REQUIRED
    *    parentId CDATA #IMPLIED
    *    parentType CDATA #IMPLIED
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
      root.setAttribute(XML_ATTR_VALUE, m_value);
      root.setAttribute(XML_ATTR_TYPE, m_type);
      if (m_parentId != null)
      {
         root.setAttribute(XML_ATTR_PARENT_ID, m_parentId);
         root.setAttribute(XML_ATTR_PARENT_TYPE, m_parentType);
      }
      root.appendChild(m_ctx.toXml(doc));
      
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

      m_value = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_VALUE);
      m_type = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_TYPE);
      String sTemp = sourceNode.getAttribute(XML_ATTR_PARENT_ID);
      if (sTemp != null && sTemp.trim().length() > 0)
         m_parentId = sTemp;
      else
         m_parentId = null;
      
      if (m_parentId != null)
         m_parentType = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
            XML_ATTR_PARENT_TYPE);
      else
         m_parentType = null;
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element ctxEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (ctxEl == null)
      {
         Object[] args = {XML_NODE_NAME, "ANY", "null"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      
      m_ctx = PSApplicationIDContextFactory.fromXml(ctxEl);
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj parameter should not be null");

      if (!(obj instanceof PSApplicationIDTypeMapping))
         throw new IllegalArgumentException(
            "obj wrong type, expecting PSApplicationIDTypeMapping");

      PSApplicationIDTypeMapping idType = (PSApplicationIDTypeMapping) obj;
      m_ctx = idType.m_ctx;
      m_value = idType.m_value;
      m_type = idType.m_type;
      m_parentId = idType.m_parentId;
      m_parentType = idType.m_parentType;
      m_hasNewValue = idType.m_hasNewValue;
   }

   // see IPSDeployComponent interface
   public int hashCode()
   {
      return m_ctx.hashCode() + m_value.hashCode() + m_type.hashCode() + 
         (m_parentId != null ? m_parentId.hashCode() : 0) + 
         (m_parentType != null ? m_parentType.hashCode() : 0) +
         (m_hasNewValue ? 1 : 0);
   }

   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;

      if (!(obj instanceof PSApplicationIDTypeMapping))
      {
         isEqual = false;
      }
      else
      {
         PSApplicationIDTypeMapping idTypeMap = (PSApplicationIDTypeMapping)obj;

         if (!m_ctx.equals(idTypeMap.m_ctx))
            isEqual = false;
         else if (!m_value.equals(idTypeMap.getValue()))
            isEqual = false;
         else if (!m_type.equals(idTypeMap.getType()))
            isEqual = false;
         else if (m_parentId == null ^ idTypeMap.m_parentId == null)
            isEqual = false;
         else if (m_parentId != null && 
            !m_parentId.equals(idTypeMap.m_parentId))
         {
            isEqual = false;
         }
         else if (m_parentType == null ^ idTypeMap.m_parentType == null)
            isEqual = false;
         else if (m_parentType != null && 
            !m_parentType.equals(idTypeMap.m_parentType))
         {
            isEqual = false;
         }
         else if (m_hasNewValue ^ idTypeMap.m_hasNewValue)
            isEqual = false;
      }

      return isEqual;
   }

   /**
    * Determines if the object contains a defined to a type
    *
    * @return <code>true</code> if the type is not
    * <code>PSApplicationIDTypeMapping.TYPE_UNDEFINED</code>;
    * <code>false</code> otherwise.
    */
   public boolean hasDefinedType()
   {
      return !m_type.equals(TYPE_UNDEFINED);
   }
   
   /**
    * Determines a new value has been set on this mapping since it was 
    * instantiated (see {@link #setValue(String)}.
    * 
    * @return <code>true</code> if a new value has been set, <code>false</code>
    * otherwise.
    */
   public boolean hasNewValue()
   {
      return m_hasNewValue;
   }
   
   /**
    * Type to indicate the literal value does not specify an id
    */
   public static final String TYPE_NONE = "sys_none";

   /**
    * Indicates the type has not yet been set (i.e. incomplete).
    */
   public static final String TYPE_UNDEFINED = "sys_undefined";

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXApplicationIDTypeMapping";

   // Private XML attribute constants
   private static final String XML_ATTR_VALUE = "value";
   private static final String XML_ATTR_TYPE = "type";
   private static final String XML_ATTR_PARENT_ID = "parentId";
   private static final String XML_ATTR_PARENT_TYPE = "parentType";

   /**
    * The context of this mapping.  Initialized during construction, never
    * <code>null</code>after that, only modified by a call to 
    * <code>copyFrom()</code>.
    */
   private PSApplicationIdContext m_ctx;

   /**
    * The value of the identifier. Initialized during construction, never
    * <code>null</code> or empty, after that, modified by
    * calls to <code>setValue()</code> and <code>copyFrom()</code>.
    */
   private String m_value;

   /**
    * The mapping type of the identifier and value. Initialized during
    * construction, never <code>null</code> or empty, after that, only modified
    * by a call to <code>copyFrom()</code> and <code>setType()</code>.
    */
   private String m_type;
   
   /**
    * The parent dependency id of the dependency specified by {@link #m_value}
    * if {@link #m_type} specifies a type that supports parent ids (see 
    * {@link PSDependency#supportsParentId()}).  <code>null</code> unless it has
    * been set by a call to {@link #setParent(String, String)}, never empty.
    */
   private String m_parentId;
   
   /**
    * The parent dependency type of the dependency specified by {@link #m_value}
    * if {@link #m_type} specifies a type that supports parent ids (see 
    * {@link PSDependency#supportsParentId()}).  <code>null</code> unless it has
    * been set by a call to {@link #setParent(String, String)}, never empty.  
    * Will not be <code>null</code> if <code>m_parentId</code> is not 
    * <code>null</code>.
    */
   private String m_parentType;
   
   /**
    * Determines if this mapping has ever been updated with a new value.
    * <code>true</code> if it has, <code>false</code> if not.  Used during
    * installation of dependencies that support id types.  This is a transient
    * runtime value and does not require persistance.  Modified by calls to
    * {@link #setValue(String)}.
    */
   private boolean m_hasNewValue = false;
}
