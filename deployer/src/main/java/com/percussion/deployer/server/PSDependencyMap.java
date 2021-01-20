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

import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.server.dependencies.PSDependencyHandler;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Map that defines all supported dependency types, and maintains parent and 
 * child relations between them.
 */
public class PSDependencyMap 
{
   /**
    * Construct this map from a list of dependency type defintions.
    * 
    * @param defs An iterator over zero or more <code>PSDependencyDef</code>
    * objects.  May not be <code>null</code>, or contain <code>null</code> 
    * entries.
    * 
    * @throws IllegalArgumentException if <code>defs</code> is invalid.
    * @throws PSDeployException if there are any other errors.
    */
   public PSDependencyMap(Iterator<PSDependencyDef> defs)
      throws PSDeployException
   {
      if (defs == null)
         throw new IllegalArgumentException("defs may not be null");
         
      while(defs.hasNext())
      {
         Object obj = defs.next();
         if (obj == null)
            throw new IllegalArgumentException(
               "defs may not contain <code>null</code> entries");
         if (!(obj instanceof PSDependencyDef))
            throw new IllegalArgumentException(
               "defs must only contain PSDependencyDef objects");
         
         PSDependencyDef def = (PSDependencyDef)obj;
         String type = def.getObjectType();
         m_dependencyMap.put(type, def);
      }
      
      buildDependencyMaps();
   }
   
   /**
    * Construct this map from its XML representation.
    * 
    * @param sourceNode The element containing the XML defintion. May not be
    * <code>null</code>.  Format is:
    * 
    * <pre><code>
    * &lt;ELEMENT PSXDependencyMap (PSXDependencyDef*) >
    * </code></pre>
    * 
    * @throws IllegalArgumentException if <code>sourceNode</code> is 
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if <code>sourceNode</code> is 
    * malformed.
    * @throws PSDeployException if there are any other errors.
    */
   public PSDependencyMap(Element sourceNode) 
      throws PSUnknownNodeTypeException, PSDeployException
   {
      this(sourceNode, true);
   }
   
   /**
    * Package private ctor to allow choice in building dependency maps for unit 
    * testing when <code>PSDependencyHandler</code> classes are not available.
    * See {@link #PSDependencyMap(Element)} for information on params and 
    * exceptions not noted below.
    * 
    * @param buildDepMaps If <code>true</code>, handler, parent and child maps
    * will be built (requires <code>PSDependencyHandler</code> to be implemented
    * for each <code>PSDependencyDef</code> to be defined, otherwise only loads
    * the defs.  If <code>false</code>, then any calls made to  
    * <code>getDependencyHandler()</code>, 
    * <code>getChildDependencyTypes()</code> or
    * <code>getParentDependencyTypes()</code> will throw 
    * <code>IllegalArgumentException</code>s.
    */
   PSDependencyMap(Element sourceNode, boolean buildDepMaps)
      throws PSUnknownNodeTypeException, PSDeployException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");
         
      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element defEl = tree.getNextElement(PSDependencyDef.XML_NODE_NAME, 
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      
      while (defEl != null)
      {
         PSDependencyDef def = new PSDependencyDef(defEl);
         m_dependencyMap.put(def.getObjectType(), def);
         defEl = tree.getNextElement(PSDependencyDef.XML_NODE_NAME, 
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
      
      if (buildDepMaps)
         buildDependencyMaps();
   }
   
   
   /**
    * Get the dependency def for the specified type.  
    * 
    * @param type The type to return, may not be <code>null</code> or empty.
    * 
    * @return The def, or <code>null</code> if a defintion of that type is not
    * found in this map.
    * 
    * @throws IllegalArgumentException if <code>type</code> is <code>null</code> 
    * or empty.
    */
   public PSDependencyDef getDependencyDef(String type)
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");
         
      return (PSDependencyDef)m_dependencyMap.get(type);
   }

   /**
    * Gets the handler for the specified def.
    * 
    * @param def The dependency def to get the handler for, may not be 
    * <code>null</code> and must be defined by this map.
    * 
    * @return The handler, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>def</code> is invalid or not
    * defined by this map.
    */
   public PSDependencyHandler getDependencyHandler(PSDependencyDef def)
   {
      if (def == null)
         throw new IllegalArgumentException("def may not be null");
      
      PSDependencyHandler handler = 
         (PSDependencyHandler)m_handlerMap.get(def.getObjectType());
      if (handler == null)
         throw new IllegalArgumentException("def not found in this map");
      
      return handler;      
   }
   
   /**
    * Get the types of dependencies that may be children of the supplied
    * dependency def.
    * 
    * @param def The dependency def whose child types will be returned. May
    * not be <code>null</code> and must be defined by this map.
    * 
    * @return an iterator over zero or more <code>PSDependencyDef</code> 
    * objects.  Never <code>null</code>, may be empty.
    * 
    * @throws IllegalArgumentException if <code>def</code> is invalid or not
    * defined by this map.
    */
   public Iterator getChildDependencyTypes(PSDependencyDef def) 
   {
      if (def == null)
         throw new IllegalArgumentException("def may not be null");
      
      List childList = (List)m_childDefMap.get(def.getObjectType());
      if (childList == null)
         throw new IllegalArgumentException("def not found in this map");
         
      return childList.iterator();
   }

   /**
    * Get the types of dependencies that may be parents of the supplied
    * dependency def.
    * 
    * @param def The dependency def whose parent types will be returned. May
    * not be <code>null</code> and must be defined by this map.
    * 
    * @return an iterator over zero or more <code>PSDependencyDef</code> 
    * objects.  Never <code>null</code>, may be empty.
    * 
    * @throws IllegalArgumentException if <code>def</code> is invalid or not
    * defined by this map.
    */
   public Iterator getParentDependencyTypes(PSDependencyDef def)
   {
      if (def == null)
         throw new IllegalArgumentException("def may not be null");
      
      if (!m_handlerMap.containsKey(def.getObjectType()))
         throw new IllegalArgumentException("def not found in this map");
      
      List parentList = (List)m_parentDefMap.get(def.getObjectType());
      if (parentList == null)
      {
         // that's okay, just means no one reports this def as a child
         parentList = new ArrayList();
      }
      
      return parentList.iterator();
   }

   /**
    * Gets all dependency defintions from this map
    * 
    * @return An iterator over zero or more <code>PSDependencyDef</code>
    * objects, never <code>null</code>.
    */
   public Iterator<PSDependencyDef> getDefs()
   {
      return m_dependencyMap.values().iterator();
   }
   
   /**
    * Adds entries to the handler map, parent map, and child map.  Handler
    * classes for each dependency type are instantiated and added to the handler 
    * map ({@link #m_handlerMap}), the child type defintions for each dependency 
    * type are determined and added to the child map ({@link #m_childDefMap}), 
    * and the parent type definitions for each dependency type are determined 
    * and added to the parent map ({@link #m_parentDefMap}).
    * 
    * @throws PSDeployException if there are any errors.
    */
   private void buildDependencyMaps() throws PSDeployException
   {
      m_handlerMap = new HashMap();
      m_parentDefMap = new HashMap();
      m_childDefMap = new HashMap();
      
      Iterator defs = m_dependencyMap.values().iterator();
      while (defs.hasNext())
      {
         // create handler and add to handler map
         PSDependencyDef def = (PSDependencyDef)defs.next();
         String defType = def.getObjectType();
         PSDependencyHandler handler = PSDependencyHandler.getHandlerInstance(
            def, this);
         m_handlerMap.put(defType, handler);
         
         Iterator childTypes = handler.getChildTypes();
         List childList = new ArrayList();
         m_childDefMap.put(defType, childList);
         while(childTypes.hasNext())
         {
            // get child defs and add them as children in the child map
            String childType = (String)childTypes.next();
            PSDependencyDef childDef = getDependencyDef(childType);
            if (childDef == null)
            {
               Object args[] = {childType, defType};
               throw new PSDeployException(
                  IPSDeploymentErrors.CHILD_DEPENDENCY_TYPE_NOT_FOUND, args);
            }
            childList.add(childDef);
            
            // for each child, add the current def as its parent in parent map
            List parentList = (List)m_parentDefMap.get(childType);
            if (parentList == null)
            {
               parentList = new ArrayList();
               m_parentDefMap.put(childType, parentList);
            }
            parentList.add(def);
         }
      }
   }
   
   /**
    * Constant for this object's root XML node.
    */
   public static final String XML_NODE_NAME = "PSXDependencyMap";

   /**
    * Map of dependency defintions by type.  Key is the dependency type as a
    * <code>String</code>, never <code>null</code>, and the  value is the 
    * <code>PSDependencyDef</code> object, never <code>null</code>.  Map is 
    * never <code>null</code>, entries are added during ctor, never modified
    * after that.
    */
   private Map<String, PSDependencyDef> m_dependencyMap = new HashMap<String, PSDependencyDef>();
   
   /**
    * Map of dependency handlers by type.  Key is the dependency type as a
    * <code>String</code>, never <code>null</code>, and the  value is the 
    * <code>PSDependencyHandler</code> object, never <code>null</code>.  Map is 
    * initialized and filled during construction, never modified after that.
    */
   private Map m_handlerMap = new HashMap();
   
   /**
    * Map of parent dependency defintions by child type.  Key is the child's 
    * dependency type as a <code>String</code>, never <code>null</code>, and the  
    * value is the a list of parent <code>PSDependencyDef</code> objects, never
    * <code>null</code> or empty.  Map is never <code>null</code>, entries are 
    * added during ctor, never modified after that.  Not every type represented
    * by this map will have an entry - only those that are children of another
    * dependency.
    */
   private Map m_parentDefMap;
   
   /**
    * Map of child dependency defintions by parent type.  Key is the parent's 
    * dependency type as a <code>String</code>, never <code>null</code>, and the  
    * value is the a list of child <code>PSDependencyDef</code> objects, never
    * <code>null</code> or empty.  Map is never <code>null</code>, entries are 
    * added during ctor, never modified after that.  Every type represented by 
    * this map will have an entry - those that do not have child dependencies
    * will have an empty list.
    */
   private Map m_childDefMap;
}
