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
 
package com.percussion.deployer.server;

import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

/**
 * Represents the defintion of a dependency type.  Each dependency instance 
 * represents one of the types defined by this class.
 */
public class PSDependencyDef
{
   /**
    * Construct this object from its member values.
    * 
    * @param objectType The type of object this class defines.  May not be
    * <code>null</code> or empty, and should be unique across all instances of
    * this class.
    * @param objectTypeName The user facing displayable form of the 
    * <code>objecType</code>, never <code>null</code> or empty.
    * @param handlerClass The name of the class that handles this dependency
    * type at runtime.  May not be <code>null</code> or empty.
    * @param supportsIdTypes If <code>true</code>, this dependency type supports
    * id type mappings, otherwise it does not.  See {@link #supportsIdTypes()}
    * for more info.
    * @param supportsIdMapping If <code>true</code>, the dependency id of a
    * dependency of this type may be mapped from one server to another, 
    * otherwise it does not.  See {@link #supportsIdMapping()} for more info.
    * @param supportsUserDependencies If <code>true</code>, this type allows
    * user defined dependencies to be added as children, <code>false</code>
    * otherwise.
    * @param isDeployableElement If <code>true</code>, this type represents a
    * deployable element, <code>false</code> otherwise.
    * @param canBeAncestor If <code>true</code>, dependencies of this type 
    * may be returned as an ancestor of another dependency, <code>false</code> 
    * if not.
    * @param supportsParentId If <code>true</code>, this type allows a parent id
    * to be specified, <code>false</code> otherwise.
    * @param autoExpand If <code>true</code>, the dependency tree in the UI 
    * will auto-expand local dependency nodes.  If <code>false</code>, 
    * the nodes will not auto-expand to display all local dependencies.
    * 
    * @throws IllegalArgumentException if any param is invalid
    */
   public PSDependencyDef(String objectType, String objectTypeName, 
      String handlerClass, boolean supportsIdTypes, boolean supportsIdMapping,
         boolean supportsUserDependencies, boolean isDeployableElement, 
         boolean canBeAncestor, boolean supportsParentId, boolean autoExpand)
   {
      if (objectType == null || objectType.trim().length() == 0)
         throw new IllegalArgumentException(
            "objectType may not be null or empty");
      
      if (objectTypeName == null || objectTypeName.trim().length() == 0)
         throw new IllegalArgumentException(
            "objectTypeName may not be null or empty");
            
      if (handlerClass == null || handlerClass.trim().length() == 0)
         throw new IllegalArgumentException(
            "handlerClass may not be null or empty");
            
      m_objectType = objectType;
      m_objectTypeName = objectTypeName;
      m_handlerClass = handlerClass;
      m_supportsIdTypes = supportsIdTypes;
      m_supportsIdMapping = supportsIdMapping;
      m_supportsUserDependencies = supportsUserDependencies;
      m_isDeployableElement = isDeployableElement;
      m_canBeAncestor = canBeAncestor;
      m_supportsParentId = supportsParentId;
      m_autoExpand = autoExpand;
   }
   
   /**
    * Construct this defintion from its XML representataion.
    * 
    * @param sourceNode The element containing this objects XML state.  May not 
    * be <code>null</code>.  Format is:
    * 
    * <pre><code>
    * &lt;ELEMENT PSXDependencyDef EMPTY >
    * &lt;ATTLIST PSXDependencyDef 
    *    objectType CDATA #REQUIRED
    *    objectTypeName CDATA #REQUIRED
    *    handlerClass CDATA #REQUIRED
    *    isDeployableElement (yes | no) "no"
    *    supportsIdTypes (yes | no) "no"
    *    supportsIdMapping (yes | no) "no"
    *    supportsUserDependencies (yes | no) "no"
    *    canBeAncestor (yes | no) "yes"
    *    supportsParentId (yes | no) "no"
    *    autoExpand (yes | no) "yes"
    *    guidType CDATA #IMPLIED
    * >
    * </code></pre>
    * 
    * @throws IllegalArgumentException if <code>sourceNode</code> is 
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if <code>sourceNOde</code> is 
    * malformed.
    */
   public PSDependencyDef(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");
         
      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      m_objectType = PSDeployComponentUtils.getRequiredAttribute(sourceNode, 
         XML_ATTR_OBJECT_TYPE);
      m_objectTypeName = PSDeployComponentUtils.getRequiredAttribute(sourceNode, 
         XML_ATTR_OBJECT_TYPE_NAME);
      m_handlerClass = PSDeployComponentUtils.getRequiredAttribute(sourceNode, 
         XML_ATTR_HANDLER_CLASS);
      m_isDeployableElement = XML_ATTR_TRUE.equals(sourceNode.getAttribute(
         XML_ATTR_IS_DEPLOYABLE_ELEMENT));
      m_supportsIdTypes = XML_ATTR_TRUE.equals(sourceNode.getAttribute(
         XML_ATTR_SUPPORTS_ID_TYPES));
      m_supportsIdMapping = XML_ATTR_TRUE.equals(sourceNode.getAttribute(
         XML_ATTR_SUPPORTS_ID_MAPPING));
      m_supportsUserDependencies = XML_ATTR_TRUE.equals(sourceNode.getAttribute(
         XML_ATTR_SUPPORTS_USER_DEPENDENCIES));
      m_canBeAncestor = !XML_ATTR_FALSE.equals(sourceNode.getAttribute(
         XML_ATTR_CAN_BE_ANCESTOR));
      m_supportsParentId = XML_ATTR_TRUE.equals(sourceNode.getAttribute(
         XML_ATTR_SUPPORTS_PARENT_ID));
      // this should default to true if not specified
      m_autoExpand = !XML_ATTR_FALSE.equals(sourceNode.getAttribute(
         XML_ATTR_AUTO_EXPAND));
      String guidType = sourceNode.getAttribute(XML_ATTR_GUID_TYPE);
      if (!StringUtils.isBlank(guidType))
         m_guidType = guidType;
   }
   
   /**
    * Gets the object type this class defines
    * 
    * @return The type, never <code>null</code> or empty.
    */
   public String getObjectType()
   {
      return m_objectType;
   }
   
   /**
    * Gets the name of the object type this class defines
    * 
    * @return The type name, never <code>null</code> or empty.
    */
   public String getObjectTypeName()
   {
      return m_objectTypeName;
   }
   
   /**
    * Determines if the type this class defines supports ID types.  See 
    * {@link com.percussion.deployer.objectstore.PSApplicationIDTypes 
    * PSApplicationIDTypes} for more info.
    * 
    * @return <code>true</code> if it supports Id Types, <code>false</code> if
    * it does not.
    */
   public boolean supportsIdTypes()
   {
      return m_supportsIdTypes;
   }
   
   /**
    * Determines if the dependency id of a dependency of this type may be mapped 
    * from one server to another.  See 
    * {@link com.percussion.deployer.objectstore.PSIdMap PSIdMap} for more info.
    * 
    * @return <code>true</code> if it supports Id mapping, <code>false</code> if
    * it does not.
    */
   public boolean supportsIdMapping()
   {
      return m_supportsIdMapping;
   }
   
   /**
    * Determines if this dependency type supports adding user depedencies as
    * child dependencies..
    * 
    * @return <code>true</code> if it is suppported, <code>false</code> if
    * it does not.
    */
   public boolean supportsUserDependencies()
   {
      return m_supportsUserDependencies;
   }
   
   /**
    * Gets the name of the class that handles this dependency type at runtime.
    * 
    * @return The class name, never <code>null</code> or empty.
    */
   public String getHandlerClassName()
   {
      return m_handlerClass;
   }

   /**
    * Determines if this def represents a deployable element.
    * 
    * @return <code>true</code> if it represents a deployable element, 
    * <code>false</code> otherwise.
    */
   public boolean isDeployableElement()
   {
      return m_isDeployableElement;
   }
   
   /**
    * Determines if this def represents a dependency that can be returned as an
    * ancestor of another dependency.
    * 
    * @return <code>true</code> if it can be an ancestor, 
    * <code>false</code> otherwise.
    */
   public boolean canBeAncestor()
   {
      return m_canBeAncestor;
   }
   
   /**
    * Determines if this def represents a dependency that can have a parent id
    * specified.
    * 
    * @return <code>true</code> if it supports specifying a parent id, 
    * <code>false</code> otherwise.
    */
   public boolean supportsParentId()
   {
      return m_supportsParentId;
   }
   
   /**
    * Determines if this def represents a dependency that should auto-expand in
    * to display all local dependencies in the dependency tree in the UI.
    * 
    * @return <code>true</code> if it should auto-expand, <code>false</code>
    * otherwise.
    */
   public boolean shouldAutoExpand()
   {
      return m_autoExpand;
   }
   
   /**
    * Get guid type that corresponds to this def's object type, may be used to
    * construct a <code>PSTypeEnum</code>.
    * 
    * @return The type, may be <code>null</code>, never empty.  
    */
   public String getGuidType()
   {
      return m_guidType;
   }
   
   /**
    * Constant for this object's root XML node.
    */
   public static final String XML_NODE_NAME = "PSXDependencyDef";
   
   /**
    * The type of object this class defines. Initialized during the ctor, never
    * <code>null</code> or empty or modified after that.
    */
   private String m_objectType;
   
   /**
    * The name of the type of object this class defines. Initialized during the
    * ctor, never <code>null</code> or empty or modified after that.
    */
   private String m_objectTypeName;
   
   /**
    * The name of the class that handles this dependency type at runtime. 
    * Initialized during the ctor, never <code>null</code> or empty or modified 
    * after that.
    */
   private String m_handlerClass;
   
   /**
    * <code>true</code> if this dependency type supports Id type definitions, 
    * <code>false</code> otherwise.  Initialized during the ctor, never 
    * modified after that.
    */
   private boolean m_supportsIdTypes = false;
   
   /**
    * <code>true</code> if this dependency type supports Id mapping, 
    * <code>false</code> otherwise.  Initialized during the ctor, never 
    * modified after that.
    */
   private boolean m_supportsIdMapping = false;
   
   /**
    * <code>true</code> if this dependency type supports adding user 
    * dependencies, <code>false</code> otherwise.  Initialized during the ctor, 
    * modified after that.
    */
   private boolean m_supportsUserDependencies = false;
   
   /**
    * <code>true</code> if this dependency type represents a deployable element,
    * <code>false</code> otherwise.  Initialized during the ctor, 
    * modified after that.
    */
   private boolean m_isDeployableElement = false;
   
   /**
    * <code>true</code> if this dependency type can be an ancestor of another
    * type, <code>false</code> otherwise.  Used to avoid searching on extremely
    * large numbers of possible ancestors. Initialized during the ctor, 
    * modified after that.
    */
   private boolean m_canBeAncestor = true;
   
   /**
    * <code>true</code> if a dependency of this type can specify a parent id, 
    * <code>false</code> otherwise.  Initialized during the ctor, never 
    * modified after that.
    */
   private boolean m_supportsParentId = true;

   /**
    * <code>true</code> if the dependency tree in the UI should auto-expand
    * to display all local dependencies, <code>false</code> if it should not
    * auto-expand.  Defaults to <code>true</code>, set during the ctor, never
    * modified after that.
    */   
   private boolean m_autoExpand = true;
   
   /**
    * Guid type in it's string format, used to map the dependency type to a
    * <code>PSTypeEnum</code> type, may be <code>null</code> if the type cannot
    * be mapped.
    */
   private String m_guidType = null;
   // XML constants
   private static final String XML_ATTR_OBJECT_TYPE = "objectType";
   private static final String XML_ATTR_OBJECT_TYPE_NAME = "objectTypeName";
   private static final String XML_ATTR_HANDLER_CLASS = "handlerClass";
   private static final String XML_ATTR_SUPPORTS_ID_TYPES = "supportsIdTypes";
   private static final String XML_ATTR_SUPPORTS_ID_MAPPING = 
      "supportsIdMapping";
   private static final String XML_ATTR_SUPPORTS_USER_DEPENDENCIES = 
      "supportsUserDependencies";
   private static final String XML_ATTR_IS_DEPLOYABLE_ELEMENT = 
      "isDeployableElement";
   private static final String XML_ATTR_CAN_BE_ANCESTOR = 
      "canBeAncestor";
   private static final String XML_ATTR_SUPPORTS_PARENT_ID = 
      "supportsParentId";
   private static final String XML_ATTR_AUTO_EXPAND = "autoExpand";
   private static final String XML_ATTR_TRUE = "yes";
   private static final String XML_ATTR_FALSE = "no";
   private static final String XML_ATTR_GUID_TYPE = "guidType";
}

