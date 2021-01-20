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

import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * PSIdMap encapsulates a list of <code>PSIsMapping</code> objects for
 * a source server.
 */
public class PSIdMap implements IPSDeployComponent
{

   /**
    * Construcuting the object for a source server name,
    * <code>sourceServer</code>.
    *
    * @param sourceServer Identifies the source repository using the form
    * <driver>:<server>:<database>:<origin>, it may not be <code>null</code>
    * or empty.
    *
    * @throws IllegalArgumentException if <code>sourceServer</code> is
    * <code>null</code> or empty.
    */
   public PSIdMap(String sourceServer)
   {
      if ( sourceServer == null || sourceServer.trim().length() == 0 )
         throw new IllegalArgumentException(
            "sourceServer may not be null or empty");

      m_sourceServer = sourceServer;
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
   public PSIdMap(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get a list of <code>PSIdMapping</code> objects.
    *
    * @return iterator over zero or more <code>PSIdMapping</code> objects,
    * never <code>null</code>.
    */
   public Iterator getMappings()
   {
      return m_mappingList.iterator();
   }

   
   /**
    * Get a specified <code>PSIdMapping</code> object.
    * 
    * @param dep The specified dependency, may not be <code>null</code>.
    * 
    * @return the mapping for the specified dependency, or <code>null</code>
    *         if not found in the map.
    */
   public PSIdMapping getMapping(PSDependency dep)
   {
      if (dep == null)
         throw new IllegalArgumentException("PSDependency may not be null");

      if (dep.supportsParentId())
         return getMapping(dep.getDependencyId(), dep.getObjectType(), dep
               .getParentId(), dep.getParentType());
      else
         return getMapping(dep.getDependencyId(), dep.getObjectType(), null,
               null);
   }

   
   /**
    * Get a specified <code>PSIdMapping</code> object.
    *
    * @param sourceId The specified source ID of the <code>PSIdMapping</code>,
    * it may not be <code>null</code> or empty.
    * @param objectType The specified object-type of the
    * <code>PSIdMapping</code>, it may not be <code>null</code> or empty.
    *
    * @return the mapping for the specified source id and type, or
    * <code>null</code> if not found in the map.
    */
   public PSIdMapping getMapping(String sourceId, String objectType)
   {
      if ( sourceId == null || sourceId.trim().length() == 0 )
         throw new IllegalArgumentException(
            "sourceId may not be null or empty");
      if ( objectType == null || objectType.trim().length() == 0 )
         throw new IllegalArgumentException(
            "objectType may not be null or empty");

      return getMapping(sourceId, objectType, null, null);
   }

   /**
    * Get a specified <code>PSIdMapping</code> object.
    *
    * @param sourceId The specified source ID of the <code>PSIdMapping</code>,
    * it may not be <code>null</code> or empty.
    * @param objectType The specified object-type of the
    * <code>PSIdMapping</code>, it may not be <code>null</code> or empty.
    * @param parentId The id of the parent if the specified source supports
    * parent id.  May be <code>null</code>, never empty.
    * @param parentType The type of the parent if the specified source 
    * supports parent id.  May be <code>null</code> only if 
    * <code>parentId</code> is <code>null</code>, never empty.
    *
    * @return the mapping for the specified source id and type, or
    * <code>null</code> if not found in the map.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSIdMapping getMapping(String sourceId, String objectType, 
      String parentId, String parentType)
   {
      if ( sourceId == null || sourceId.trim().length() == 0 )
         throw new IllegalArgumentException(
            "sourceId may not be null or empty");
      if ( objectType == null || objectType.trim().length() == 0 )
         throw new IllegalArgumentException(
            "objectType may not be null or empty");
            
      if (parentId != null && parentId.trim().length() == 0)
         throw new IllegalArgumentException("parentId may not be empty");
      if (parentId != null && (parentType == null || 
         parentType.trim().length() == 0))
      {
         throw new IllegalArgumentException(
            "parentType may not be null or empty");
      }
      
      Iterator mappings = m_mappingList.iterator();
      PSIdMapping result = null;
      while (mappings.hasNext() && result == null)
      {
         PSIdMapping mapping = (PSIdMapping) mappings.next();
         if ( sourceId.equals(mapping.getSourceId()) &&
            objectType.equals(mapping.getObjectType()) )
         {
            if (parentId != null)
            {
               if ( parentId.equals(mapping.getSourceParentId()) &&
                  parentType.equals(mapping.getParentType()))
               {
                  result = mapping;
               }
            }
            else
               result = mapping;
         }
      }
      return result;
   }

   
   /**
    * Get a specified <code>PSIdMapping</code> object based on the target id.
    *
    * @param targetId The specified target ID of the <code>PSIdMapping</code>,
    * it may not be <code>null</code> or empty.
    * @param objectType The specified object-type of the
    * <code>PSIdMapping</code>, it may not be <code>null</code> or empty.
    *
    * @return the mapping for the specified target id and type, or
    * <code>null</code> if not found in the map.
    */
   public PSIdMapping getTargetMapping(String targetId, String objectType)
   {
      if ( targetId == null || targetId.trim().length() == 0 )
         throw new IllegalArgumentException(
            "targetId may not be null or empty");
      if ( objectType == null || objectType.trim().length() == 0 )
         throw new IllegalArgumentException(
            "objectType may not be null or empty");

      return getTargetMapping(targetId, objectType, null, null);
      
   }
   
   /**
    * Get a specified <code>PSIdMapping</code> object based on the target id.
    *
    * @param targetId The specified target ID of the <code>PSIdMapping</code>,
    * it may not be <code>null</code> or empty.
    * @param objectType The specified object-type of the
    * <code>PSIdMapping</code>, it may not be <code>null</code> or empty.
    * @param targetParentId The id of the parent if the specified target 
    * supports parent id.  May be <code>null</code>, never empty.
    * @param parentType The type of the parent if the specified target 
    * supports parent id.  May be <code>null</code> only if 
    * <code>parentId</code> is <code>null</code>, never empty.
    *
    * @return the mapping for the specified target id and type, or
    * <code>null</code> if not found in the map.
    */
   public PSIdMapping getTargetMapping(String targetId, String objectType, 
      String targetParentId, String parentType)
   {
      if ( targetId == null || targetId.trim().length() == 0 )
         throw new IllegalArgumentException(
            "targetId may not be null or empty");
      if ( objectType == null || objectType.trim().length() == 0 )
         throw new IllegalArgumentException(
            "objectType may not be null or empty");

      if (targetParentId != null && targetParentId.trim().length() == 0)
         throw new IllegalArgumentException("targetParentId may not be empty");
      if (targetParentId != null && (parentType == null || 
         parentType.trim().length() == 0))
      {
         throw new IllegalArgumentException(
            "parentType may not be null or empty");
      }
      
      Iterator mappings = m_mappingList.iterator();
      PSIdMapping result = null;
      while (mappings.hasNext() && result == null)
      {
         PSIdMapping mapping = (PSIdMapping) mappings.next();
         if ( targetId.equals(mapping.getTargetId()) &&
            objectType.equals(mapping.getObjectType()) )
         {
            if (targetParentId != null)
            {
               if ( targetParentId.equals(mapping.getTargetParentId()) &&
                  parentType.equals(mapping.getParentType()))
               {
                  result = mapping;
               }
            }
            else
               result = mapping;
         }
      }
      return result;
      
   }
   
   /**
    * Adding a <code>PSIdMapping</code> object.
    *
    * @param mapping The <code>PSIdMapping</code> object to be added, it may
    * not be <code>null</code>
    *
    * @throws IllegalArgumentException If <code>mapping</code> is
    * <code>null</code>.
    */
   public void addMapping(PSIdMapping mapping)
   {
      if ( mapping == null )
         throw new IllegalArgumentException("mappinge may not be null");

      m_mappingList.add(mapping);
   }
   
   /**
    * Removes the supplied <code>PSIdMapping</code> object if it exists in the 
    * map. Uses {@link #getMapping(String, String) 
    * getMapping(sourceId, objectType)} for checking existance of mapping.
    *
    * @param mapping The <code>PSIdMapping</code> object to be removed, may
    * not be <code>null</code>
    *
    * @throws IllegalArgumentException If <code>mapping</code> is
    * <code>null</code>.
    */
   public void removeMapping(PSIdMapping mapping)
   {
      if ( mapping == null )
         throw new IllegalArgumentException("mappinge may not be null");

      mapping = getMapping(mapping.getSourceId(), mapping.getObjectType(), 
         mapping.getSourceParentId(), mapping.getParentType());
      
      if(mapping != null)
         m_mappingList.remove(mapping);
   }
   
   /**
    * Checks whether the mapping identified by the supplied id and type is 
    * mapped or not.
    * 
    * @param sourceId the source id of the mapping, may not be <code>null</code>
    * or empty.
    * @param objectType the object type of the mapping, may not be <code>null
    * </code> or empty.
    * @param parentId The id of the parent if the specified source supports
    * parent id.  May be <code>null</code>, never empty.
    * @param parentType The type of the parent if the specified source 
    * supports parent id.  May be <code>null</code> only if 
    * <code>parentId</code> is <code>null</code>, never empty.
    * 
    * @return <code>true</code> if the mapping exists and is mapped, otherwise
    * <code>false</code>
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public boolean isMapped(String sourceId, String objectType, 
      String parentId, String parentType)
   { 
      if ( sourceId == null || sourceId.trim().length() == 0 )
         throw new IllegalArgumentException(
            "sourceId may not be null or empty");
      if ( objectType == null || objectType.trim().length() == 0 )
         throw new IllegalArgumentException(
            "objectType may not be null or empty");
            
      if (parentId != null && parentId.trim().length() == 0)
         throw new IllegalArgumentException("parentId may not be empty");
      if (parentId != null)
      {
         if(parentType == null || parentType.trim().length() == 0)
         {
            throw new IllegalArgumentException(
               "parentType may not be null or empty");
         }
      }
      else if(parentType != null)
      {
         throw new IllegalArgumentException(
               "parentType must be null if parentId is null");
      }            
             
      PSIdMapping mapping = getMapping(sourceId, objectType, 
         parentId, parentType);
      if(mapping != null && mapping.isMapped())
         return true;
         
      return false;
   }
   
   /**
    * Convenience method for {@link #isMapped(String, String, String, String) 
    * isMapped(sourceId, objectType, null, null)}. Please see the link for more
    * information. This should be called only if the supplied source id does not
    * have a parent id.
    */
   public boolean isMapped(String sourceId, String objectType)
   {
      if ( sourceId == null || sourceId.trim().length() == 0 )
         throw new IllegalArgumentException(
            "sourceId may not be null or empty");
      if ( objectType == null || objectType.trim().length() == 0 )
         throw new IllegalArgumentException(
            "objectType may not be null or empty");
            
      return isMapped(sourceId, objectType, null, null);
   }

   /**
    * Get the name of the source server
    *
    * @return The name of the source server, it never be <code>null</code> or
    * empty.
    */
   public String getSourceServer()
   {
      return m_sourceServer;
   }

   
   /**
    * Gets the new id from this map for a given source id and type.
    * 
    * @param id The source id to get a new value for, may not be
    * <code>null</code> or empty.
    * @param type The type of id, may not be <code>null</code> or empty.
    * 
    * @return The new id, never <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if the id cannot be obtained.
    */
   public String getNewId(String id, String type) throws PSDeployException
   {
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
         
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");
      
      return getNewId(id, type, null, null);
   }  
   
   /**
    * Gets the new id from this map for a given source id and type, and parent
    * id and type.
    * 
    * @param id The source id to get a new value for, may not be
    * <code>null</code> or empty.
    * @param type The type of id, may not be <code>null</code> or empty.
    * @param parentId The id of the parent if the specified <code>type</code> 
    * supports parent id.  May be <code>null</code>, never empty.
    * @param parentType The type of the parent if the specified 
    * <code>type</code> supports parent id.  May be <code>null</code> only if 
    * <code>parentId</code> is <code>null</code>, never empty.
    * 
    * @return The new id, never <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if the id cannot be obtained.
    */
   public String getNewId(String id, String type, String parentId, 
      String parentType) throws PSDeployException
   {
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
         
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");
      
      if (parentId != null && parentId.trim().length() == 0)
         throw new IllegalArgumentException("parentId may not be empty");
      if (parentId != null && (parentType == null || 
         parentType.trim().length() == 0))
      {
         throw new IllegalArgumentException(
            "parentType may not be null or empty");
      }
      
      PSIdMapping idMapping = getMapping(id, type, parentId, parentType);
      if (idMapping == null)
      {
         Object[] args = {type, id, getSourceServer()};
         throw new PSDeployException(IPSDeploymentErrors.MISSING_ID_MAPPING, 
            args);
      }
      
      String newValue = idMapping.getTargetId();
      if (newValue == null)
      {
         String errId = id;
         if (parentId != null)
            errId = id + ":" + parentId;
         Object[] args = {type, errId, getSourceServer()};
         throw new PSDeployException(
            IPSDeploymentErrors.INCOMPLETE_ID_MAPPING, args);
      }
      
      return newValue;
   }  
   
   /**
    * Gets the new id as an int from this map for a given source id and type, 
    * and parent id and type.
    * 
    * @param id The source id to get a new value for, may not be
    * <code>null</code> or empty.
    * @param type The type of id, may not be <code>null</code> or empty.
    * @param parentId The id of the parent if the specified <code>type</code> 
    * supports parent id.  May be <code>null</code>, never empty.
    * @param parentType The type of the parent if the specified 
    * <code>type</code> supports parent id.  May be <code>null</code> only if 
    * <code>parentId</code> is <code>null</code>, never empty.
    * 
    * @return The new id, never <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if the id cannot be obtained.
    */
   public int getNewIdInt(String id, String type, String parentId, 
      String parentType) throws PSDeployException
   {
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
         
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");
      
      if (parentId != null && parentId.trim().length() == 0)
         throw new IllegalArgumentException("parentId may not be empty");
      if (parentId != null && (parentType == null || 
         parentType.trim().length() == 0))
      {
         throw new IllegalArgumentException(
            "parentType may not be null or empty");
      }
      
      String strId = getNewId(id, type, parentId, parentType);
      try
      {
         return Integer.parseInt(strId);
      }
      catch (NumberFormatException e)
      {
         Object[] args = {type, id, getSourceServer(), strId};
         throw new PSDeployException(
            IPSDeploymentErrors.INVALID_ID_MAPPING_TARGET, args);
      }
      
   }  
   
   /**
    * Gets the new id from the supplied map for a given source id and type.
    * 
    * @param id The source id to get a new value for, may not be
    * <code>null</code> or empty.
    * @param type The type of id, may not be <code>null</code> or empty.
    * 
    * @return The new id, never <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if the id cannot be obtained.
    */
   public int getNewIdInt(String id, String type) throws PSDeployException
   {
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
         
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");
      
      return getNewIdInt(id, type, null, null);
   }  
   
   /**
    * Serializes this object's state to its XML representation.  Format is:
    *
    * <pre><code>
    *    &lt;!ELEMENT PSXIdMap (PSXIdMapping*)
    *    &lt;!ATTLIST PSXIdMap
    *       sourceServer CDATA #REQUIRED
    * </code>/<pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc should not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_SRC_SERVER, m_sourceServer);

      Iterator list = m_mappingList.iterator();
      while ( list.hasNext() )
      {
         PSIdMapping mapping = (PSIdMapping) list.next();
         root.appendChild(mapping.toXml(doc));
      }
      return root;
   }

   /**
    * See {@link IPSDeployComponent#hashCode()} for more info on method
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

      m_sourceServer = PSDeployComponentUtils.getRequiredAttribute(
         sourceNode, XML_ATTR_SRC_SERVER);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      m_mappingList.clear(); // initialize internal data.
      Element mappingEl = tree.getNextElement(PSIdMapping.XML_NODE_NAME,
         FIRST_FLAGS);
      while (mappingEl != null)
      {
         PSIdMapping mapping = new PSIdMapping(mappingEl);
         m_mappingList.add(mapping);
         mappingEl = tree.getNextElement(PSIdMapping.XML_NODE_NAME, NEXT_FLAGS);
      }

   }

   /**
    * See {@link IPSDeployComponent#copyFrom()} for more info on method
    * signature.
    */
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj parameter should not be null");

      if (!(obj instanceof PSIdMap))
         throw new IllegalArgumentException(
            "obj wrong type, expecting PSIdMapping");

      PSIdMap obj2 = (PSIdMap) obj;

      m_sourceServer = obj2.m_sourceServer;
      m_mappingList.clear();
      m_mappingList.addAll(obj2.m_mappingList);
   }

   /**
    * See {@link IPSDeployComponent#hashCode()} for more info on method
    * signature.
    */
   public int hashCode()
   {
      return m_sourceServer.hashCode() + m_mappingList.hashCode();
   }

   /**
    * See {@link IPSDeployComponent#equals(Object)} for more info on method
    * signature.
    */
   public boolean equals(Object obj)
   {
      boolean result = false;

      if ((obj instanceof PSIdMap))
      {
         PSIdMap obj2 = (PSIdMap) obj;
         result = m_sourceServer.equals(obj2.m_sourceServer) &&
            m_mappingList.equals(obj2.m_mappingList);
      }
      return result;
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXIdMap";

   // Private XML attribute constant for <code>XML_NODE_NAME</code>
   private static final String XML_ATTR_SRC_SERVER = "sourceServer";

   /**
    * The identifier of the source server, initialized by constructor, modified
    * by {@link #fromXml(Element)} and {@link #copyFrom(IPSDeployComponent). It
    * will never be <code>nill</code> or empty
    */
   private String m_sourceServer;

   /**
    * Containing a list of <code>PSIdMapping</code> objects. It will never be
    * <code>null</code>, may be empty.
    */
   private List m_mappingList = new ArrayList();

   /**
    * flags to walk to a child node of a XML tree
    */
   private static final int FIRST_FLAGS =
      PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
      PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

   /**
    * flags to walk to a sibling node of a XML tree
    */
   private static final int NEXT_FLAGS =
      PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
      PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
}
