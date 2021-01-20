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

package com.percussion.deployer.client;

import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deployer.objectstore.PSApplicationIDTypes;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.server.IPSJobHandle;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Creates a dependency tree for migrating content to a specific target server.
 * The top-level element of the tree will be a folder, and the tree will be
 * populated with folder definitions, folder contents, content items, and
 * content relations by recursively descending the folder hierarchy.
 * <p>
 * It is intended that the dependency tree created the <code>build</code>
 * method will be exported by the caller using a <code>PSExportJob</code>.
 * The job's <code>
 * setDepKeysToExclude</code> method should be set with the
 * result from this class' <code>getExcludedDependencies</code>, so that the
 * export job will not add those dependencies as "missing".
 * <p>
 * All included content items must have their application ids typed. When using
 * the MSM client, this is accomplished by user-interaction with a dialog. When
 * using this class, this is accomplished by providing a
 * <code>IPSApplicationIDTypesResolver</code> to the <code>setIdTyper</code>
 * method.
 * <p>
 * It is possible to include only a subset of content in the dependency tree.
 * Any content items from a content type that does not have an id mapping will
 * not be included. Additionally, any variants or slots that do not have an id
 * mapping will be suppressed as dependencies of the included content items and
 * relations.
 * <p>
 * Example code for invoking the class:
 * <pre><code>
 * PSDeploymentManager deployMgr = new PSDeploymentManager(sourceConn);
 * PSFolderContentsDescriptorBuilder builder = new PSFolderContentsDescriptorBuilder(
 *       deployMgr, sourceFolder, idMap);
 * Set idTypedFields = ms_config.getIdTypedFields();
 * if (idTypedFields != null)
 * {
 *    builder.setIdTyper(new PSOApplicationIDTypesResolver(idTypedFields));
 * }
 * PSDeployableElement de = builder.build();
 * PSOContentMigrationHelper.saveIdMap(idMap, target);
 * Set depsToExclude = builder.getExcludedDependencies();
 * export(descriptorName, de, deployMgr, depsToExclude);
 * copyArchive(descriptorName, deployMgr);
 * </code></pre>
 */
public class PSFolderContentsDescriptorBuilder
{

   /**
    * Constructs a builder that can create a dependency tree of all the folders
    * and content items beneath the specified folder path, as long as the
    * dependent items are included in the specified ID map.
    * 
    * @param sourceManager manages all client communication with the source
    *           Rhythmyx server, never <code>null</code>
    * @param sourceFolderPath path to the Rhythmyx folder that will be the root
    *           of the dependency tree, never <code>null</code> or empty
    * @param idMap ID mapping between the source and target servers. New content
    *           items will be added to the map, so they will receive new ids on
    *           import. Also used to limit which content items are included in
    *           the dependency tree; items from unmapped content types are not
    *           included. Never <code>null</code>.
    */
   public PSFolderContentsDescriptorBuilder(PSDeploymentManager sourceManager,
         String sourceFolderPath, PSIdMap idMap)
   {

      if (sourceManager == null)
         throw new IllegalArgumentException(
               "deployment manager may not be null");
      if (sourceFolderPath == null || sourceFolderPath.trim().length() == 0)
         throw new IllegalArgumentException(
               "source folder path may not be null or empty");
      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      m_sourceMgr = sourceManager;
      m_idMap = idMap;

      // folder dependency ids do not include the leading double-slash
      m_sourceFolderId = sourceFolderPath.replaceFirst("^//", "");
   }

   public PSDeployableElement build(IPSJobHandle job) throws PSDeployException
   {
      if (job == null)
         throw new IllegalArgumentException("IPSJobHandle may not be null");

      m_job = job;
      return build();
   }

   public PSDeployableElement build() throws PSDeployException
   {
      // these fields should have been inited in ctor
      assert m_sourceFolderId != null;
      assert m_sourceMgr != null;

      // init the other fields
      m_included = new HashMap();
      m_includedCount = 0;
      m_excludedDependencies = new HashSet();

      PSDeployableElement pkg = null;
      pkg = getTopFolderElement();
      if (pkg == null)
      {
         ms_log.error("failed to find top-level folder");
      }
      else
      {
         // remember property value to reset after process
         String previousMaxDeps = System
               .getProperty(IPSDeployConstants.PROP_MAX_DEPS);

         // set a large value for the maximum number of dependencies that will
         // be loaded
         System.setProperty(IPSDeployConstants.PROP_MAX_DEPS, String
               .valueOf(MAX_DEPS));

         loadChildren(pkg);

         if (previousMaxDeps != null)
            System.setProperty(IPSDeployConstants.PROP_MAX_DEPS,
                  previousMaxDeps);
         else
            System.setProperty(IPSDeployConstants.PROP_MAX_DEPS, String
                  .valueOf(IPSDeployConstants.MAX_DEPS));
      }
      return pkg;
   }

   /**
    * Gets the set of dependencies that have been excluded during the folder
    * tree traversal.
    * 
    * @return set of <code>String</code>, each representing the key of a
    *         <code>PSDependency<code>; never <code>null</code>, may be empty.
    */
   public Set getExcludedDependencies()
   {
      return m_excludedDependencies;
   }

   /**
    * Sets the class that will be used to define any undefined application ID
    * types in included dependencies.
    * 
    * @param idTyper will be used to define any undefined application ID types
    *           in included dependencies. May be <code>null</code> if source
    *           data contains no undefined application ID types.
    */
   public void setIdTyper(IPSApplicationIDTypesResolver idTyper)
   {
      m_idTyper = idTyper;
   }

   /**
    * Gets the deployable element that represents the top-level folder of the
    * source folder.
    * 
    * @return The top-level folder deployable element that contains the source
    *         folder, or <code>null</code> if no such folder can be found.
    * 
    * @throws PSDeployException
    */
   private PSDeployableElement getTopFolderElement() throws PSDeployException
   {
      PSDeployableElement pkg = null;
      Iterator i = m_sourceMgr
            .getDeployableElements(IPSDeployConstants.DEP_OBJECT_TYPE_FOLDER);
      while (i.hasNext() && pkg == null)
      {
         PSDeployableElement element = (PSDeployableElement) i.next();
         ms_log.debug("considering " + element);
         if (m_sourceFolderId.startsWith(element.getDisplayName()))
         {
            pkg = element;
         }
      }
      return pkg;
   }

   private void loadChildren(PSDependency dep) throws PSDeployException
   {
      loadDependencies(dep);

      // recurse into children
      Iterator i = dep.getDependencies();
      while (i.hasNext() && !isCancelled())
      {
         PSDependency childDep = (PSDependency) i.next();

         // avoid loops: check if this dependency has already been processed
         if (m_included.containsKey(childDep.getKey()))
         {
            // make sure each instance of included dependencies are included
            // TODO: use a treectx to manage this
            if (childDep.canBeIncludedExcluded())
            {
               childDep.setIsIncluded(true);
            }
            continue;
         }

         // remember removed dependencies so they are not checked again
         if (previouslyRemoved(childDep))
         {
            i.remove();
         }
         else if (processDependency(childDep))
         {
            ms_log.debug("removing " + childDep);
            i.remove();
            rememberRemoval(childDep);

            /*
             * folder contents could be included, then removed when all child
             * content items are removed, so update cache.
             */
            m_included.remove(childDep.getKey());

         }
      }
   }

   /**
    * Loads any id types for the specified dependency and delegates undefined
    * types to user-provided class for resolution. The class must define all ID
    * types or an exception will be thrown.
    * 
    * @param dep the dependency whose id types will be loaded and defined,
    *           assumed not <code>null</code>
    * @throws PSDeployException if an error occurs while loading or saving id
    *            types, or if the user-provided class does not define all id
    *            types.
    */
   private void defineIdTypes(PSDependency dep) throws PSDeployException
   {
      Iterator depIDTypes = m_sourceMgr.getIdTypes(Collections.singleton(dep)
            .iterator());
      while (depIDTypes.hasNext())
      {
         PSApplicationIDTypes types = (PSApplicationIDTypes) depIDTypes.next();
         /*
          * run like client's "typical" mode: only undefined id types are
          * processed
          */
         boolean incompleteOnly = true;
         Iterator mappings = types.getAllMappings(incompleteOnly);
         if (mappings.hasNext())
         {
            // pass undefined mappings to user-provided class for resolution
            if (m_idTyper != null)
            {
               m_idTyper.defineIdTypes(mappings);
            }

            // make sure all ids have been typed
            mappings = types.getAllMappings(incompleteOnly);
            if (mappings.hasNext())
            {
               PSApplicationIDTypeMapping mapping = (PSApplicationIDTypeMapping) mappings
                     .next();
               throw new PSDeployException(
                     IPSDeploymentErrors.INCOMPLETE_ID_TYPE_MAPPING,
                     new Object[]
                     {mapping.getType(), dep.getDependencyId(),
                           mapping.getValue(),
                           mapping.getContext().getDisplayText()});
            }

            // save the modified types
            m_sourceMgr.saveIdTypes(Collections.singleton(types).iterator());
         }
      }
   }

   /**
    * TODO
    * 
    * @param dep
    * @return <code>true</code> if the dependency should be removed from the
    *         dependency tree (because it is not ID mapped and its presence in
    *         the tree would cause a validation error); <code>false</code>
    *         otherwise.
    * @throws PSDeployException
    */
   private boolean processDependency(PSDependency dep) throws PSDeployException
   {
      boolean shouldRemove = false;

      String type = dep.getObjectType();
      if (type.equals(IPSDeployConstants.DEP_OBJECT_TYPE_FOLDER))
      {
         include(dep);
         loadChildren(dep);
      }
      else if (type.equals(IPSDeployConstants.DEP_OBJECT_TYPE_FOLDER_DEF))
      {
         if (isSelectedByPath(dep.getDependencyId()))
         {
            ms_log.debug("entering folder " + dep.getDependencyId());
            include(dep);
            loadChildren(dep);
         }
         else
         {
            shouldRemove = true;
         }
      }
      else if (type.equals(IPSDeployConstants.DEP_OBJECT_TYPE_FOLDER_CONTENTS))
      {
         // TODO: only include folder contents for root folder and descendents
         // (not ancestors)
         include(dep);
         loadChildren(dep);
         // if all the children have been removed, remove the folder contents
         if (dep.getChildCount(false) == 0)
         {
            ms_log.warn("all child items suppressed for " + dep);
            shouldRemove = true;
         }
      }
      else if (type.equals(IPSDeployConstants.DEP_OBJECT_TYPE_CONTENT_DEF))
      {
         shouldRemove = shouldRemoveContentItem(dep);
         // children have already been processed
      }
      else if (type.equals(IPSDeployConstants.DEP_OBJECT_TYPE_VARIANT_DEF))
      {
         // remove from tree if it is not included in the map
         // (the actual relationship data that uses the variant
         // will need to be suppress during install)
         if (!isIdMapped(dep))
         {
            shouldRemove = true;
         }
      }
      else if (type.equals(IPSDeployConstants.DEP_OBJECT_TYPE_SLOT))
      {
         shouldRemove = shouldRemoveSlot(dep);
      }
      else
      {
         /*
          * Other types (such as "Relationship" and "Display Format Defintion")
          * are not included. Their children will be expanded by
          * addMissingDependencies when the descriptor is exported.
          */
      }

      return shouldRemove;
   }

   /**
    * Determines if the specified slot should be removed from the descriptor.
    * Since slots are never mapped, this method fetches the dependent slot
    * defintion and checks to see if it has been ID mapped. If it has not been
    * mapped, the slot should be removed and the method returns
    * <code>true</code>. Before returning, the slot's dependents are cleared,
    * so that if the slot is not removed, addMissingDependencies does not expand
    * the slot definition.
    * 
    * @param slot the slot to test, assumed not <code>null</code>.
    * @return <code>true</code> if the slot should be removed from the
    *         descriptor, <code>false</code> if the slot should remain in the
    *         descriptor.
    * 
    * @throws PSDeployException propagated if errors occur in loadDependencies
    */
   private boolean shouldRemoveSlot(PSDependency slot) throws PSDeployException
   {
      boolean shouldRemoveSlot = false;

      // TODO: maintain included slots in m_included to avoid repeat checks

      if (previouslyRemoved(slot))
      {
         // yes we've seen the slot, it is excluded (don't check again)
         shouldRemoveSlot = true;
      }
      else
      {
         loadDependencies(slot);
         Iterator slotChildren = slot.getDependencies();
         while (slotChildren.hasNext() && !isCancelled())
         {
            PSDependency slotChild = (PSDependency) slotChildren.next();
            String objectType = slotChild.getObjectType();
            if (objectType.equals(IPSDeployConstants.DEP_OBJECT_TYPE_SLOT_DEF))
            {
               // remove the slot if it has not been ID mapped
               if (!isIdMapped(slotChild))
               {
                  shouldRemoveSlot = true;
               }
            }
         }

         /*
          * since the slot will never be marked included, remove its child
          * dependencies, so they will not be expanded by
          * addMissingDependencies.
          */
         slot.setDependencies(null);
      }
      return shouldRemoveSlot;
   }

   /*
    * not all content types are included, so make sure this item's type is
    * included by fetching dependencies and checking the map
    */
   private boolean shouldRemoveContentItem(PSDependency item)
         throws PSDeployException
   {
      boolean shouldRemoveItem = false;

      loadDependencies(item);

      // first, determine if this item will be included by checking its
      // dependent type
      Iterator i = item.getDependencies();
      while (i.hasNext() && !isCancelled())
      {
         PSDependency itemChild = (PSDependency) i.next();
         String objectType = itemChild.getObjectType();
         if (objectType
               .equals(IPSDeployConstants.DEP_OBJECT_TYPE_CONTENT_EDITOR))
         {
            // have we seen this content type?
            if (m_included.containsKey(itemChild.getKey()))
            {
               // yes the type is included, so include the item
               include(item);
            }
            else
            {
               if (previouslyRemoved(itemChild))
               {
                  shouldRemoveItem = true;
               }
               else
               {
                  // no we haven't seen this type yet, so drill down
                  loadDependencies(itemChild);
                  Iterator ii = itemChild.getDependencies();
                  while (ii.hasNext() && !isCancelled())
                  {
                     PSDependency typeChild = (PSDependency) ii.next();
                     if (typeChild.getObjectType().equals(
                           IPSDeployConstants.DEP_OBJECT_TYPE_CONTENT_TYPE))
                     {
                        // is this content type mapped?
                        if (isIdMapped(typeChild))
                        {
                           // yes, include the content item
                           // (but not the type or type def)
                           include(item);
                           // remember we've seen this content type
                           m_included.put(itemChild.getKey(), itemChild);
                        }
                        else
                        {
                           // no, remove the content item from the tree
                           shouldRemoveItem = true;
                           rememberRemoval(itemChild);
                        }
                     }
                  }
                  // clear content type's dependencies, since it is not included
                  itemChild.setDependencies(null);
               }
            }
         }
      }

      // second, if the item is included, expand any dependent relationships
      if (!shouldRemoveItem)
      {
         i = item.getDependencies();
         while (i.hasNext() && !isCancelled())
         {
            PSDependency itemChild = (PSDependency) i.next();
            if (itemChild.getObjectType().equals(
                  IPSDeployConstants.DEP_OBJECT_TYPE_CONTENT_RELATION))
            {
               include(itemChild);
               loadChildren(itemChild);
            }
            else
            {
               /*
                * Other types (such as "Community" and "Workflow") are not
                * included. Their children will be expanded by
                * addMissingDependencies when the descriptor is exported.
                */
            }
         }
      }
      return shouldRemoveItem;
   }

   /**
    * Checks if the supplied dependency has already been seen and removed.
    * 
    * @param dep
    * @return <code>true</code> if the dependency has already been seen and
    *         removed; <code>false</code> otherwise.
    */
   private boolean previouslyRemoved(PSDependency dep)
   {
      return m_excludedDependencies.contains(dep.getKey());
   }

   /**
    * Remembers the supplied dependency was seen and removed by caching it in a
    * <code>Set</code>. Paired with {@link #previouslyRemoved(PSDependency)}
    * 
    * @param dep
    */
   private void rememberRemoval(PSDependency dep)
   {
      m_excludedDependencies.add(dep.getKey());
   }

   /**
    * Determines if the specified folder is an ancestor or descendent of the
    * source folder.
    * 
    * @param folder
    * @return <code>true</code> if the folder is an ancestor or descendent of
    *         the source folder, <code>false</code> otherwise.
    */
   private boolean isSelectedByPath(String folder)
   {
      boolean selected;
      if (folder.startsWith(m_sourceFolderId))
      {
         // all descendents of the source folder are included
         selected = true;
      }
      else if (m_sourceFolderId.startsWith(folder))
      {
         // all ancestors of the source folder are included
         selected = true;
      }
      else
      {
         // skip any other folder
         selected = false;
      }
      return selected;
   }

   /**
    * Checks if the specified dependency is defined in the idMap field.
    * 
    * @param dep dependency to check, assumed not <code>null</code>.
    * @return <code>true</code> if the specified dependency is defined in the
    *         idMap field; <code>false</code> otherwise.
    */
   private boolean isIdMapped(PSDependency dep)
   {
      return m_idMap.getMapping(dep) != null;
   }

   /**
    * Requests the deployment manager load the specified object's dependencies.
    * 
    * @param dep
    * @throws PSDeployException
    */
   private void loadDependencies(PSDependency dep) throws PSDeployException
   {
      updateStatus("loading " + dep);
      if (dep.supportsIdTypes())
      {
         defineIdTypes(dep);
      }
      m_sourceMgr.loadDependencies(dep);
   }

   /**
    * Marks the specified dependency as included, and updates the
    * <code>m_included</code> cache. If the dependency supports ID types, it
    * is stored in <code>m_idTypesLocalDeps</code> for later processing.
    * 
    * @param dep the dependency to include, assumed not <code>null</code>
    */
   private void include(PSDependency dep)
   {
      /*
       * maintain count of the number of included dependencies to allow more
       * informative status messages.
       */
      m_includedCount++;
      ms_log.debug("include #" + m_includedCount + ":" + dep + " "
            + dep.getDependencyId());

      m_included.put(dep.getKey(), dep);
      if (dep.canBeIncludedExcluded())
      {
         dep.setIsIncluded(true);
      }

      /*
       * If the included dependency is a content item, and it doesn't have an id
       * mapping, it needs to marked as a new object so it will be assigned a
       * new id on import.
       */
      String type = dep.getObjectType();
      if (type.equals(IPSDeployConstants.DEP_OBJECT_TYPE_CONTENT_DEF))
      {
         if (!isIdMapped(dep))
         {
            ms_log.debug("add to server: " + dep + " " + dep.getDependencyId());

            PSIdMapping idMapping = new PSIdMapping(dep.getDependencyId(), dep
                  .getDisplayName(), dep.getObjectType(), dep.getParentId(),
                  dep.getParentId(), dep.getParentType(), true);
            idMapping.setIsNewObject(true);
            m_idMap.addMapping(idMapping);
         }
      }

   }

   private void updateStatus(String message)
   {
      if (m_job == null)
      {
         // without a job handle, just log the message
         ms_log.info(message);
      }
      else
      {
         m_job.updateStatus(message);
      }
   }

   private boolean isCancelled()
   {
      if (m_job == null)
      {
         // without a job handle, it is not possible to stop the process
         return false;
      }
      else
      {
         return m_job.isCancelled();
      }
   }

   /**
    * Tracks the dependencies that have been included, using the dependency key
    * for uniqueness. The dependency object itself cannot be used as a key,
    * since a dependency with children is not equal to the same dependency
    * without children. Assigned in the <code>build()</code> method, never
    * <code>null</code>.
    */
   private Map m_included;

   /**
    * Counts how many dependencies have been included.
    */
   private int m_includedCount;

   /**
    * Tracks the dependencies that have been removed, using the dependency key
    * for uniqueness. Assigned in the <code>build()</code> method, never
    * <code>null</code>.
    */
   private Set m_excludedDependencies;

   /**
    * Full path to the source folder, whose content will be recursively
    * migrated. Assigned in ctor, never <code>null</code> or empty.
    */
   private String m_sourceFolderId;

   /**
    * Manages client requests to the Rhythmyx server defined by
    * <code>m_sourceConn</code>. Assigned in ctor, never <code>null</code>.
    */
   private PSDeploymentManager m_sourceMgr;

   /**
    * Defines the ID transformation from source to target. Any source
    * dependencies that are not included in the map will be suppressed. Assigned
    * in ctor, never <code>null</code>.
    */
   private PSIdMap m_idMap;

   /**
    * The job handle to use to update the status. Assigned in the
    * <code>build()</code> method, may be <code>null</code>, but then the
    * process cannot be cancelled.
    */
   private IPSJobHandle m_job;

   /**
    * Resolves any undefined application ID types. Assigned by setter, may be
    * <code>null</code> if there are no undefined application ID types in the
    * source data to be exported.
    */
   private IPSApplicationIDTypesResolver m_idTyper;

   /**
    * Determines the maximum number of child dependencies that will be loaded
    * for a single parent dependency. Assign to a system property
    * {@link IPSDeployConstants#PROP_MAX_DEPS} to override the default.
    */
   private static final int MAX_DEPS = 1000;

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static final Logger ms_log = Logger
         .getLogger(PSFolderContentsDescriptorBuilder.class);

}
