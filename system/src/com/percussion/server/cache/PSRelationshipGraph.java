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
package com.percussion.server.cache;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSExceptionUtils;
import com.percussion.services.error.PSNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class is used to 'plot' graphs for a specific relationship type, so
 * that we can retrieve the parent and/or child graph paths of a specified item.
 * <p>
 * Note: it is necessary to plot all relationships, otherwise it only contains
 * partial graths. The current implementation is for folder relationship only.
 */
class PSRelationshipGraph
{
   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static final Logger log = LogManager.getLogger("PSRelationshipGraph");

   public void addRelationship(Integer relationshipId, PSLocator parent, PSLocator child)
   {
      addRelationship(relationshipId,parent,child,-1);
   }
   
   /**
    * Adds (or plots) the supplied parent & child relationship into the graph.
    *
    * @param relationshipId the related object, never <code>null</code>.
    * @param parent the parent object, never <code>null</code>.
    * @param child the child object, never <code>null</code>.
    */
   public void addRelationship(Integer relationshipId, PSLocator parent, PSLocator child, int sortRank)
   {
      if (relationshipId == null)
         throw new IllegalArgumentException("relatedObj may not be null");
      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");
      if (child == null)
         throw new IllegalArgumentException("child may not be null");

      // store the relationship into m_childMap first
      ArrayList<PSGraphEntry> children = m_parentMapToChildren.get(parent);
      PSGraphEntry childObj = new PSGraphEntry(child, relationshipId, sortRank);
      if (children == null)
      {
         children = new ArrayList<>();
         children.add(childObj);
         m_parentMapToChildren.put(parent, children);
      }
      else
      {
         children.add(childObj);
         Collections.sort(children);
      }

      // store the relationship into m_parentMap first
      ArrayList<PSGraphEntry> parents = m_childMapToParent.get(child);
      PSGraphEntry parentObj = new PSGraphEntry(parent, relationshipId, sortRank);
      if (parents == null)
      {
         parents = new ArrayList<>();
         parents.add(parentObj);
         m_childMapToParent.put(child, parents);
      }
      else
      {
         parents.add(parentObj);
         
      }
   }

   /**
    * Removes the supplied relationship from the graph.
    *
    * @param relatedObj
    *           the related object, never <code>null</code>.
    * @param parent
    *           the parent object, never <code>null</code>.
    * @param child
    *           the child object, never <code>null</code>.
    */
   public void removeRelationship(Integer relatedObj, PSLocator parent, PSLocator child)
   {
      if (relatedObj == null)
         throw new IllegalArgumentException("relatedObj may not be null");
      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");
      if (child == null)
         throw new IllegalArgumentException("child may not be null");

      PSGraphEntry childEntry = new PSGraphEntry(child, relatedObj);
      PSGraphEntry parentEntry = new PSGraphEntry(parent, relatedObj);

      // update the m_childMap
      ArrayList<PSGraphEntry> children = m_parentMapToChildren.get(parent);
      if (children != null)
      {
         if (children.remove(childEntry) && children.isEmpty())
            m_parentMapToChildren.remove(parent);
      }

      // update the m_parentMap
      ArrayList<PSGraphEntry> parents = m_childMapToParent.get(child);
      if (parents != null)
      {
         if (parents.remove(parentEntry) && parents.isEmpty())
         m_childMapToParent.remove(child);
      }
   }

   /**
    * Returns the paths to the root for the supplied child.
    * <p>
    * Note: this is implemented for folder relationships. It needs to be
    * overridden for other relationships.
    *
    * @param child
    *           the child object, never <code>null</code>.
    *
    * @return a 2 dimension array. The 1st dimension is a list of paths; the
    *         2nd dimension contains the actual paths, which is an array of
    *         <code>PSGraphEntry[]</code> objects. Within each path, the 1st
    *         element is the root, followed by its immediate child, ...etc.
    *         Never <code>null</code>, but may be empty if the supplied
    *         object is the root, which does not have parent.
    */
   public List<List<PSGraphEntry>> getPathsToRoot(PSLocator child, String relationshipTypeName) {
      if (child == null)
         throw new IllegalArgumentException("child may not be null");

      List<List<PSGraphEntry>> ret = new ArrayList<>();
      // For folder need to check revisionless
      PSLocator childLoc = child.getRevision() == -1 ? child : new PSLocator(child.getId());

      ArrayList<PSGraphEntry> parents = m_childMapToParent.get(childLoc);
      if (parents != null && !parents.isEmpty()) {
         for (PSGraphEntry parent : parents) {
            PSRelationship rel = null;
            try {
               rel = PSFolderRelationshipCache.getInstance().getRelationship(parent.getrelationshipId());
               if (rel == null || !rel.getConfig().getName().equalsIgnoreCase(relationshipTypeName)) {
                  continue;
               }
            } catch (PSNotFoundException e) {
               log.warn("Relationship Not Found : {} : Error : {} " ,parent.getrelationshipId(), PSExceptionUtils.getMessageForLog(e));
            }

            List<PSGraphEntry> path = new ArrayList<>();
            getFolderPath(path, parent, relationshipTypeName);
            // reverse the path
            Collections.reverse(path);
            ret.add(path);
         }
      }

      return ret;
   }

   /**
    * This is the same as {@link #getChildren(Object)}, but it returns a list
    * instead of array.
    * 
    * @param parent the parent, never <code>null</code>.
    * 
    * @return a list of immediate child objects, never <code>null</code>
    *   
    */
   public List<PSGraphEntry> getChildrenList(PSLocator parent)
   {
      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");
      ArrayList<PSGraphEntry> children = m_parentMapToChildren.get(parent);
      return children==null ? new CopyOnWriteArrayList<>() : new CopyOnWriteArrayList(children);
   }

   /**
    * Returns the immediate parent objects for the supplied child.
    *
    * @param child the child object, never <code>null</code>.
    *
    * @return the parent objects, never <code>null</code>, but may be empty.
    */
   public List<PSGraphEntry> getParents(PSLocator child)
   {
      if (child == null)
         throw new IllegalArgumentException("child may not be null");

      ArrayList<PSGraphEntry> parentList = m_childMapToParent.get(child);
      if (parentList == null)
      {
         parentList = new ArrayList<>();
      }

      return parentList;
   }

   /**
    * Gets the folder path for the supplied child item.
    *
    * @param path
    *           the folder path that is returned to the caller. It is a list of
    *           {@link PSGraphEntry}objects. Assume never <code>null</code>,
    *           but may be empty.
    * @param childFolder
    *           the child folder object. Assume not <code>null</code>.
    */
   private void getFolderPath(List<PSGraphEntry> path, PSGraphEntry childFolder, String relationshipTypeName)
   {
      PSFolderRelationshipCache cache = PSFolderRelationshipCache.getInstance();
      PSRelationship childRel = null;
      if (cache != null) {
         try {
            childRel = cache.getRelationship(childFolder.getrelationshipId());
         } catch (PSNotFoundException e) {
            log.warn("Relationship Not Found : {} : Error : {} " ,childFolder.getrelationshipId(), PSExceptionUtils.getMessageForLog(e));
            return;
         }
      }
      if (childRel != null && childRel.getConfig().getName().equalsIgnoreCase(relationshipTypeName)) {
          path.add(childFolder);
      } else if (!childRel.getConfig().getName().equalsIgnoreCase(relationshipTypeName)) {
         // we are going down the path of a relationship that doesn't match the relationshipType
         return;
      }
      ArrayList<PSGraphEntry> parents = m_childMapToParent.get(childFolder
            .getValue());

      if (parents != null && !parents.isEmpty())
      {
          List<PSGraphEntry> filteredParents = new ArrayList<>();
          for (PSGraphEntry entry : parents) {
              if (cache != null) {
                 PSRelationship rel = null;
                 try {
                    rel = cache.getRelationship(entry.getrelationshipId());
                    if (rel != null && rel.getConfig().getName().equalsIgnoreCase(relationshipTypeName)) {
                       filteredParents.add(entry);
                    }
                 } catch (PSNotFoundException e) {
                    log.warn("Relationship Not Found : {} : Error : {} " ,entry.getrelationshipId(), PSExceptionUtils.getMessageForLog(e));
                 }

              }
          }

         Iterator<PSGraphEntry> it = filteredParents.iterator();
         // there may be a situation where the above code would remove
          // any items in the iterator.  This is when the parents.size() == 1
          // and that 1 item is not of the type of relationship being searched for.
          if (it.hasNext()) {
              getFolderPath(path, (PSGraphEntry) it.next(), relationshipTypeName);
          }
      }
   }

   /**
    * Trim the graph to save memory.
    */
   public void trimSize()
   {
      ArrayList<PSGraphEntry> value;
      Iterator<ArrayList<PSGraphEntry>> values;
      for (int i=0; i<2; i++)
      {
         if (i == 0)
            values = m_childMapToParent.values().iterator();
         else
            values = m_parentMapToChildren.values().iterator();

         while (values.hasNext())
         {
            value = values.next();
            value.trimToSize();
         }
      }
   }

   /**
    * Returns all parents from the current graph.
    * 
    * @return a set of <code>Object</code>, which is the <code>parent</code>
    *    object that was passed in 
    *    {@link #addRelationship(Object, Object, Object)}, never 
    *    <code>null</code>, may be empty. It cannot be modified by the caller.
    */
   Set<PSLocator> getAllParents()
   {
      return Collections.unmodifiableSet(m_parentMapToChildren.keySet());
   }
   
   /**
    * Returns the total number of parents in the graph.
    * 
    * @return total number of parents.
    */
   int getTotalParent()
   {
      return m_parentMapToChildren.size();
   }
   
   /**
    * Returns the total number of children in the graph.
    * 
    * @return total number of children.
    */
   int getTotalChildren()
   {
      return m_childMapToParent.size();
   }
   
   /**
    * It maps the child object to its parent. The map keys are child in
    * <code>Object</code>; the map values are collections of parents in
    * <code>Collection</code> objects. Each value (the <code>Collection</code>
    * object) is a collection of {@link PSGraphEntry} object.
    * <p>
    * It may be empty, but never <code>null</code>.
    */
   private  Map<PSLocator,ArrayList<PSGraphEntry>> m_childMapToParent = new ConcurrentHashMap<>();

   /**
    * It maps the parent object to its children. The map keys are parents in
    * <code>Object</code> objects; the map values are lists of child in
    * <code>Collection</code> objects. Each value (the <code>Collection</code>
    * object) is a collection of {@link PSGraphEntry} object.
    * <p>
    * It may be empty, but never
    * <code>null</code>.
    */
   private Map<PSLocator,ArrayList<PSGraphEntry>> m_parentMapToChildren = new ConcurrentHashMap<>();

}
