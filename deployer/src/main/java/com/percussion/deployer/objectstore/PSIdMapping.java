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

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents a mapping within a {@link PSIdMap}.
 */
public class PSIdMapping implements IPSDeployComponent
{
   /**
    * Constructing the object with the given source ID, source name, type and
    * parent id and type.
    * The constructed object will default to not a new object.
    *
    * @param    sourceId The source ID, may not be <code>null</code> or empty.
    * @param    sourceName The name of the source, may not be <code>null</code>
    * or empty.
    * @param    objectType The type of the object, may not be <code>null</code>
    * or empty.
    * @param parentType The type of the source object's parent.  May be 
    * <code>null</code> if the source does not specify a parent, never emtpy.
    * @param parentId The id of the source parent, may be <code>null</code>, 
    * never empty.  May not be <code>null</code> if <code>parentType</code> is
    * not <code>null</code>, must be <code>null</code> otherwise.
    * @param parentName The name of the source parent, may be <code>null</code>, 
    * never empty.  May not be <code>null</code> if <code>parentType</code> is
    * not <code>null</code>, must be <code>null</code> otherwise.
    * @param    isNewMapping <code>true</code> if it is a new mapping object,
    * <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public PSIdMapping(String sourceId, String sourceName, String objectType, 
      String parentId, String parentName, String parentType, boolean isNewMapping)
   {
      if ( sourceId == null || sourceId.trim().length() == 0 )
         throw new IllegalArgumentException(
            "sourceId may not be null or empty");
      if ( sourceName == null || sourceName.trim().length() == 0 )
         throw new IllegalArgumentException(
            "sourceName may not be null or empty");
      if ( objectType == null || objectType.trim().length() == 0 )
         throw new IllegalArgumentException(
            "objectType may not be null or empty");
      if (parentType != null && parentType.trim().length() == 0)
         throw new IllegalArgumentException("parentType may not be empty");
         
      if (parentType != null)
      { 
         if(parentId == null || parentId.trim().length() == 0)
         {
            throw new IllegalArgumentException(
               "parentId may not be null or empty if parentType is not null");
         }
      }
      else if (parentId != null)
         throw new IllegalArgumentException(
            "parentId must be null if parentType is null");
            
      if (parentType != null)
      {
         if(parentName == null || parentName.trim().length() == 0)
         {
            throw new IllegalArgumentException(
               "parentName may not be null or empty if parentType is not null");
         }
      }
      else if (parentName != null)
         throw new IllegalArgumentException(
            "parentName must be null if parentType is null");            
         
      m_sourceId = sourceId;
      m_sourceName = sourceName;
      m_objectType = objectType;
      m_isNewObject = false;
      m_isNewMapping = isNewMapping;
      m_parentType = parentType;
      m_sourceParentId = parentId;
      m_sourceParentName = parentName;
   }

   /**
    * Convenience ctor calls {@link #PSIdMapping(String, String, String, 
    * String, String, boolean) PSIdMapping(sourceId, sourceName, objectType, 
    * null, null, null, false)}
    */
   public PSIdMapping(String sourceId, String sourceName, String objectType)
   {
      this(sourceId, sourceName, objectType, null, null, null, false);
   }

   /**
    * Convenience ctor calls {@link #PSIdMapping(String, String, String, 
    * String, String, boolean) PSIdMapping(sourceId, sourceName, objectType, 
    * null, null, null, isNewMapping)}
    */
   public PSIdMapping(String sourceId, String sourceName, String objectType,
      boolean isNewMapping)
   {
      this(sourceId, sourceName, objectType, null, null, null, isNewMapping);
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
   public PSIdMapping(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Copy constructor.
    * 
    * @param source The source mapping, may not be <code>null</code>.  A shallow
    * copy of the source is constructed.
    */
   public PSIdMapping(PSIdMapping source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      copyFrom(source);
   }

   /**
    * Determines if this is an existing mapping, or if this is defining a new
    * mapping.
    *
    * @return <code>true</code> if the object is a new mapping,
    * <code>false</code> otherwise.
    */
   public boolean isNewMapping()
   {
      return m_isNewMapping;
   }

   /**
    * Sets the state of the new object. Clears the target information if the
    * new object state is set to <code>true</code>
    *
    * @param isNewObject <code>true</code> if wants to set new object state,
    * <code>false</code> otherwise.
    */
   public void setIsNewObject(boolean isNewObject)
   {
      m_isNewObject = isNewObject;
      if(isNewObject)
      {
         m_targetId = null;
         m_targetName = null;
         
         if(m_parentType != null)
         {
            m_targetParentId = null;
            m_targetParentName = null;
         }
      }
   }
   
   /**
    * Checks whether this mapping is mapped with target or not.
    * 
    * @return <code>true</code> if the {@link #isNewObject() } is <code>true
    * </code> or set with target id, otherwise <code>false</code>
    */
   public boolean isMapped()
   {
      return (isNewObject() || getTargetId() != null);
   }

   /**
    * Convenience method calls {@link #setTarget(String, String, String, String) 
    * setTarget(targetId, targetName, null, null)}
    */
   public void setTarget(String targetId, String targetName)
   {
      setTarget(targetId, targetName, null, null);
   }

   /**
    * Sets target ID, target name, and optionally parent id. 
    *
    * @param targetId The ID of the target, may not be <code>null</code> or
    * empty
    * @param targetName The name of the target, may not be <code>null</code>
    * or empty
    * @param targetParentId The id of the target's parent, may not be 
    * <code>null</code> if the source parent has been set, must be 
    * <code>null</code> if the source's parent has not been set.  Never empty.
    * @param targetParentName The name of the target's parent, may not be 
    * <code>null</code> if the source parent has been set, must be 
    * <code>null</code> if the source's parent has not been set.  Never empty.
    * 
    * @throws IllegalArgumentException If any param is invalid.
    */
   public void setTarget(String targetId, String targetName, 
      String targetParentId, String targetParentName)
   {
      if ( targetId == null || targetId.trim().length() == 0 )
         throw new IllegalArgumentException(
            "targetId may not be null or empty");
      if ( targetName == null || targetName.trim().length() == 0 )
         throw new IllegalArgumentException(
            "targetName may not be null or empty");
      
      if (targetParentId != null)
      {
         if (m_parentType == null)
           throw new IllegalArgumentException(
            "targetParentId must be null if no source parent is set");
         else if (targetParentId.trim().length() == 0)
           throw new IllegalArgumentException(
            "targetParentId may not be empty");
      }
      else if (m_parentType != null)
         throw new IllegalArgumentException(
            "targetParentId may not be null if source parent is set");      
      
      if (targetParentId != null)
      {
         if(targetParentName == null || targetParentName.trim().length() == 0)
         {
            throw new IllegalArgumentException(
               "targetParentName may not be null or empty if source parent is set");
         }
      }
      else if (targetParentName != null)
         throw new IllegalArgumentException(
            "targetParentName must be null if source parent is not set.");                      

      m_targetId = targetId;
      m_targetName = targetName;
      m_targetParentId = targetParentId;
      m_targetParentName = targetParentName;
   }

   /**
    * Get the source ID.
    *
    * @return The source Id. It will never be <code>null</code> or empty.
    */
   public String getSourceId()
   {
      return m_sourceId;
   }

   /**
    * Get the source parent ID.
    *
    * @return The source parent Id. It may be <code>null</code>, never empty.
    */
   public String getSourceParentId()
   {
      return m_sourceParentId;
   }
   
   /**
    * Gets the source parent name.
    *
    * @return The source parent name, may be <code>null</code>, never empty.
    */
   public String getSourceParentName()
   {
      return m_sourceParentName;
   }   

   /**
    * Get the source name.
    *
    * @return The source name. It will never be <code>null</code> or empty.
    */
   public String getSourceName()
   {
      return m_sourceName;
   }

   /**
    * Get the type of the object.
    *
    * @return The object type. It will never be <code>null</code> or empty.
    */
   public String getObjectType()
   {
      return m_objectType;
   }

   /**
    * Get the type of the object' parent.
    *
    * @return The parent type, it may be <code>null</code>, but never be empty.
    */
   public String getParentType()
   {
      return m_parentType;
   }

   
   /**
    * Get the target Id.
    *
    * @return the target id, it may be <code>null</code>, but never be empty
    */
   public String getTargetId()
   {
      return m_targetId;
   }

   /**
    * Get the target name.
    *
    * @return the target name, it may be <code>null</code>, but never be empty
    */
   public String getTargetName()
   {
      return m_targetName;
   }
   
   /**
    * Get the target parent id.
    *
    * @return the target parent id, it may be <code>null</code>, but never be 
    * empty.
    */
   public String getTargetParentId()
   {
      return m_targetParentId;
   }
   
   /**
    * Gets the target parent name.
    *
    * @return the target parent name, may be <code>null</code>, never empty.
    */
   public String getTargetParentName()
   {
      return m_targetParentName;
   }

   /**
    * Determines if this object maps to an existing object on the server, or if
    * it will be inserted as a new object.
    *
    * @return <code>true</code> if it is a new object, <code>false</code>
    * otherwise.
    */
   public boolean isNewObject()
   {
      return m_isNewObject;
   }

   /**
    * Serializes this object's state to its XML representation.  Format is:
    *
    * <pre><code>
    *    &lt;!ELEMENT PSXIdMapping (PSXIdMappingTarget?)
    *    &lt;!ATTLIST PSXIdMapping
    *       sourceId   CDATA #REQUIRED
    *       sourceName CDATA #REQUIRED
    *       objectType CDATA #REQUIRED
    *       isNewObject (true | false) "false" CDATA #REQUIRED
    *       parentType CDATA #IMPLIED
    *       sourceParentId CDATA #IMPLIED
    *       sourceParentName CDATA #IMPLIED
    *    &lt;!ELEMENT PSXIdMappingTarget EMPTY
    *    &lt;!ATTLIST PSXIdMappingTarget
    *       targetId   CDATA #REQUIRED
    *       targetName CDATA #REQUIRED
    *       targetParentId CDATA #IMPLIED
    *       targetParentName CDATA #IMPLIED
    * </code>/<pre>
    *
    * NOTE: the PSXIdMappingTarget element will be ignored if
    * <code>isNewObject</code> attribute is <code>true</code>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc should not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_SRC_ID, m_sourceId);
      root.setAttribute(XML_ATTR_SRC_NAME, m_sourceName);
      root.setAttribute(XML_ATTR_OBJ_TYPE, m_objectType);
      String sIsNewObject = m_isNewObject ? "true" : "false";
      root.setAttribute(XML_ATTR_IS_NEW_OBJECT, sIsNewObject);
      if (m_parentType != null)
      {
         root.setAttribute(XML_ATTR_PARENT_TYPE, m_parentType);
         root.setAttribute(XML_ATTR_SRC_PARENT_ID, m_sourceParentId);
         root.setAttribute(XML_ATTR_SRC_PARENT_NAME, m_sourceParentName);         
      }

      // ignore the targetId and targetName for a new object
      if ( (!m_isNewObject) && m_targetId != null )
      {
         Element targetNode = doc.createElement(XML_NODE_TARGET);
         targetNode.setAttribute(XML_ATTR_TGT_ID, m_targetId);
         targetNode.setAttribute(XML_ATTR_TGT_NAME, m_targetName);
         if (m_targetParentId != null)
         {
            targetNode.setAttribute(XML_ATTR_TGT_PARENT_ID, m_targetParentId);
            targetNode.setAttribute(XML_ATTR_TGT_PARENT_NAME, m_targetParentName);            
         }
         root.appendChild(targetNode);
      }

      return root;
   }

   /**
    * Restores this object's state from its XML representation.
    * In the restored object, its {@link #isNewMapping()} state is always
    * <code>false</code>, its <code>PSXIdMappingTarget</code> element will be
    * ignored if <code>isNewObject</code> attribute is <code>true</code>.
    *
    * See {@link #toXml(Document)} for format of XML.
    * See {@link #IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      m_sourceId = PSDeployComponentUtils.getRequiredAttribute(
         sourceNode, XML_ATTR_SRC_ID);
      m_sourceName = PSDeployComponentUtils.getRequiredAttribute(
         sourceNode, XML_ATTR_SRC_NAME);
      m_objectType = PSDeployComponentUtils.getRequiredAttribute(
         sourceNode, XML_ATTR_OBJ_TYPE);
      
      String sTemp;
      sTemp = sourceNode.getAttribute(XML_ATTR_PARENT_TYPE);
      if (sTemp != null && sTemp.trim().length() > 0)
      {
         m_parentType = sTemp;
         // always have source parent id in this case
         m_sourceParentId = PSDeployComponentUtils.getRequiredAttribute(
            sourceNode, XML_ATTR_SRC_PARENT_ID);
         m_sourceParentName = PSDeployComponentUtils.getRequiredAttribute(
            sourceNode, XML_ATTR_SRC_PARENT_NAME);            
      }

      String sIsNewObject = PSDeployComponentUtils.getRequiredAttribute(
         sourceNode, XML_ATTR_IS_NEW_OBJECT);
      m_isNewObject = sIsNewObject.equals("true") ? true : false;

      m_isNewMapping = false; // always false

      // only consider XML_NODE_TARGET element for existing object
      if ( !m_isNewObject )
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         Element tgtEl = tree.getNextElement(XML_NODE_TARGET,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
         if ( tgtEl == null )
         {
            m_targetId = null;
            m_targetName = null;
            m_targetParentId = null;
            m_targetParentName = null;
         }
         else
         {
            m_targetId = PSDeployComponentUtils.getRequiredAttribute(
               tgtEl, XML_ATTR_TGT_ID);
            m_targetName = PSDeployComponentUtils.getRequiredAttribute(
               tgtEl, XML_ATTR_TGT_NAME);
            if (m_parentType != null)
            {
               m_targetParentId = PSDeployComponentUtils.getRequiredAttribute(
                  tgtEl, XML_ATTR_TGT_PARENT_ID);
               m_targetParentName = PSDeployComponentUtils.getRequiredAttribute(
                  tgtEl, XML_ATTR_TGT_PARENT_NAME);                  
            }
         }
      }
   }

   // See IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj parameter should not be null");

      if (!(obj instanceof PSIdMapping))
         throw new IllegalArgumentException(
            "obj wrong type, expecting PSIdMapping");

      PSIdMapping obj2 = (PSIdMapping) obj;
      m_sourceId = obj2.m_sourceId;
      m_sourceName = obj2.m_sourceName;
      m_objectType = obj2.m_objectType;
      m_targetId = obj2.m_targetId;
      m_targetName = obj2.m_targetName;
      m_isNewObject = obj2.m_isNewObject;
      m_isNewMapping = obj2.m_isNewMapping;
      m_parentType = obj2.m_parentType;
      m_sourceParentId = obj2.m_sourceParentId;
      m_targetParentId = obj2.m_targetParentId;
      m_sourceParentName = obj2.m_sourceParentName;
      m_targetParentName = obj2.m_targetParentName;      
   }

   // See IPSDeployComponent interface
   public int hashCode()
   {
      return m_sourceId.hashCode() + m_sourceName.hashCode() + 
         m_objectType.hashCode() +
         (m_isNewObject ? 1 : 0) + (m_isNewMapping ? 1 : 0) +
         ((m_targetId == null) ? 0 : m_targetId.hashCode()) +
         ((m_targetName == null) ? 0 : m_targetName.hashCode()) +
         ((m_parentType == null) ? 0 : m_parentType.hashCode()) +
         ((m_sourceParentId == null) ? 0 : m_sourceParentId.hashCode()) +
         ((m_targetParentId == null) ? 0 : m_targetParentId.hashCode()) +
         ((m_sourceParentName == null) ? 0 : m_sourceParentName.hashCode()) +
         ((m_targetParentName == null) ? 0 : m_targetParentId.hashCode());         
   }

   // See IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean result = true;

      if (!(obj instanceof PSIdMapping))
         result = false;
      else
      {
         PSIdMapping obj2 = (PSIdMapping) obj;
         
         if (!m_sourceId.equals(obj2.m_sourceId))
            result = false;
         else if (!m_sourceName.equals(obj2.m_sourceName))
            result = false;
         else if(!m_objectType.equals(obj2.m_objectType))
            result = false;
         else if (m_isNewObject != obj2.m_isNewObject)
            result = false;
         else if (m_isNewMapping != obj2.m_isNewMapping)
            result = false;
         else if ((m_targetId == null) ^ (obj2.m_targetId == null))
            result = false;
         else if (m_targetId != null && !m_targetId.equals(obj2.m_targetId))
            result = false;
         else if ((m_targetName == null) ^ (obj2.m_targetName == null))
            result = false;
         else if (m_targetName != null && !m_targetName.equals(
            obj2.m_targetName))
         {
            result = false;
         }
         else if (m_parentType == null ^ obj2.m_parentType == null)
            result = false;
         else if (m_parentType != null && !m_parentType.equals(
            obj2.m_parentType))
         {
            result = false;
         }
         else if (m_sourceParentId == null ^ obj2.m_sourceParentId == null)
            result = false;
         else if (m_sourceParentId != null && !m_sourceParentId.equals(
            obj2.m_sourceParentId))
         {
            result = false;
         }
         else if (m_sourceParentName == null ^ obj2.m_sourceParentName == null)
            result = false;
         else if (m_sourceParentName != null && !m_sourceParentName.equals(
            obj2.m_sourceParentName))
         {
            result = false;
         }         
         else if (m_targetParentId == null ^ obj2.m_targetParentId == null)
            result = false;
         else if (m_targetParentId != null && !m_targetParentId.equals(
            obj2.m_targetParentId))
         {
            result = false;
         }
         else if (m_targetParentName == null ^ obj2.m_targetParentName == null)
            result = false;
         else if (m_targetParentName != null && !m_targetParentName.equals(
            obj2.m_targetParentName))
         {
            result = false;
         }           
      }
      
      return result;
   }
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXIdMapping";

   // Private XML attribute constants
   private static final String XML_ATTR_SRC_NAME = "sourceName";
   private static final String XML_ATTR_SRC_ID = "sourceId";
   private static final String XML_ATTR_OBJ_TYPE = "objectType";
   private static final String XML_ATTR_IS_NEW_OBJECT = "isNewObject";
   private static final String XML_ATTR_IS_NEW_MAPPING = "isNewMapping";
   private static final String XML_ATTR_SRC_PARENT_ID = "sourceParentId";
   private static final String XML_ATTR_SRC_PARENT_NAME = "sourceParentName";   
   private static final String XML_ATTR_PARENT_TYPE = "parentType";

   /**
    * The name of target XML Node/Element
    */
   private static final String XML_NODE_TARGET = "PSXIdMappingTarget";
   private static final String XML_ATTR_TGT_NAME = "targetName";
   private static final String XML_ATTR_TGT_ID = "targetId";
   private static final String XML_ATTR_TGT_PARENT_ID = "targetParentId";
   private static final String XML_ATTR_TGT_PARENT_NAME = "targetParentName";   

   /**
    * The source ID, initialized by constructor, only modified by
    *  {@link #copyFrom(Element)}, never be <code>null</code> or empty.
    */
   private String m_sourceId;
   /**
    * The source name, initialized by constructor, only modified by
    *  {@link #copyFrom(Element)}, never be <code>null</code> or empty.
    */
   private String m_sourceName;

   /**
    * The target ID, initialized by constructor, modified by
    *  {@link #copyFrom(Element)} and {@link #setTarget(String, String)}.
    *  It can be <code>null</code>, but never by empty.
    *  <P>
    *  NOTE: <code>m_targetId</code> and <code>m_targetName</code> can
    *  either both be <code>null</code> or both be none empty
    *  <code>String</code>.
    */
   private String m_targetId;
   /**
    * The target name, initialized by constructor, modified by
    *  {@link #copyFrom(Element)} and {@link #setTarget(String, String)}.
    *  It can be <code>null</code>, but never be empty.
    *  <P>
    *  See {link #m_targetId} for a side note.
    */
   private String m_targetName;

   /**
    * The object type, initialized by constructor, only modified by
    *  {@link #copyFrom(Element)}. It never be <code>null</code> or empty.
    */
   private String m_objectType;

   /**
    * Determining if the object is new, initialized to false, modified
    * by {@link SetIsNewObject(boolean)} and {@link #copyFrom(Element)}.
    * If it is <code>true</code>, the {@link #toXml(Document)} and
    * {@link #fromXml(Element)} method will ignore both
    * <code>m_targetId</code> and <code>m_targetName</code>
    * while constructing or retrieving the XML Element.
    */
   private boolean m_isNewObject = false;

   /**
    * Determining if this is a new mapping object, initialized by construtor.
    * Only modified by {@link #copyFrom(Element)}. This is used by GUI only,
    * so it will not by part of XML. It is always <code>false</code> in the
    * restored object, see {@link #fromXml(Element)}.
    */
   private boolean m_isNewMapping;
   
   /**
    * The source dependency's parent id, if it supports specifying one.  
    * Initialized during construction, may be <code>null</code>, never empty.
    */
   private String m_sourceParentId = null;
   
   /**
    * The source dependency's parent name, if it supports specifying one.  
    * Initialized during construction, may be <code>null</code>, never empty.
    */
   private String m_sourceParentName = null;
   
   /**
    * The source dependency's parent type, if it supports specifying one.  
    * Initialized during construction, may be <code>null</code>, never empty.
    */
   private String m_parentType = null;
   
   /**
    * The target dependency's parent id, if it supports specifying one.  
    * Modified by calls to {@link #setTarget(String, String, String, String) 
    * setTarget(targetId, targetName, targetParentId, targetParentName)}, may be 
    * <code>null</code>, never empty.
    */
   private String m_targetParentId = null;
   
   /**
    * The target dependency's parent name, if it supports specifying one.  
    * Modified by calls to {@link #setTarget(String, String, String, String) 
    * setTarget(targetId, targetName, targetParentId, targetParentName)}, may be 
    * <code>null</code>, never empty.
    */
   private String m_targetParentName = null;   
}
