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
package com.percussion.services.contentmgr.impl;

import com.percussion.services.contentmgr.PSContentMgrConfig;
import com.percussion.services.contentmgr.data.PSNodeDefinition;
import com.percussion.services.contentmgr.impl.legacy.PSContentTypeChange;
import com.percussion.services.contentmgr.impl.query.IPSPropertyMapper;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

/**
 * The content repository is in charge of managing content items for a given
 * repository. This interface allows the content manager the ability to create,
 * read, update and delete content items.
 * <P>
 * Items in the repository are presented in their raw form. Further manipulation
 * will occur in the content manager for various purposes. These include the
 * translation of property data and the generation of read-only properties to
 * augment the items returned.
 * <P>
 * Items are referenced by UUID or path, there are separate calls for the two
 * cases.
 * <P>
 * The repository can assume correct data is being passed in. The manager is
 * responsible for data validation before calling the repository interfaces.
 * 
 * @author dougrand
 */
public interface IPSContentRepository extends IPSPropertyMapper
{
   /**
    * Load the minimum possible data, intentionally not loading children and
    * body data. Flags are hints to the implementation, which may ignore them.
    */
   public static final int LOAD_MINIMAL = 0;

   /**
    * Load flag that indicates that item children should be loaded. If this is
    * not set, the child information will be present, but the actual items will
    * not be loaded. Don't set this flag if you're only reading or modifying the
    * parent item. This is a hint to the implementation, which may be ignored,
    * i.e. children may always be loaded or never loaded by a given
    * implementation.
    */
   public static final int LOAD_CHILDREN = 0x1;

   /**
    * Load flag that indicates that properties that contain significant amounts
    * of data should be loaded. This is a hint to the implementation, which may
    * be ignored, i.e. the bodies may always be loaded or never loaded by a
    * given implementation.
    */
   public static final int LOAD_BODIES = 0x2;

   /**
    * Load both bodies and child items.
    */
   public static final int LOAD_ALL = LOAD_CHILDREN | LOAD_BODIES;

   /**
    * Attributes of content repositories
    */
   public enum Capability {
      /**
       * Can read content
       */
      READ, 
      /**
       * Can save content 
       */
      WRITE, 
      /**
       * Can version content 
       */
      VERSION, 
      /**
       * Can lock content items 
       */
      LOCK;
   }

   /**
    * Each repository will return an array of capabilities to allow the content
    * manager to discover information that is required for its operation.
    * 
    * @return an array of capabilities, never <code>null</code> and never
    *         empty
    */
   public Capability[] getCapabilities();

   /**
    * Load items. The properties of the items, with the exception of the body
    * fields, are always loaded. The flags provide hints to the implementation.
    * <P>
    * The resulting items are capable of loading the missing bodies and children
    * dynamically by calling internal methods of the repository.
    * 
    * @param config the configuration for the repository, may be
    *           <code>null</code>
    * @param guids the GUIDs for the items that should be loaded. GUIDs
    *           reference a specific version of a specific item. The list may
    *           not have <code>null</code> elements. It is the reponsibility
    *           of the repository plugin to maintain an association between the
    *           guid and a uuid from the repository. For the built in
    *           repository, the guid is the item's primary key and the uuid is
    *           simply another guid that is created for a version chain of
    *           items.
    * 
    * @return the items, never <code>null</code>. Any items that do not exist
    *         in the repository will have a missing entry in the returned list.
    *         It is in the same order as the specified GUIDs, but may be fewer.
    *         
    * @throws RepositoryException if another problem occurs while loading the
    *            item
    */
   List<Node> loadByGUID(List<IPSGuid> guids, PSContentMgrConfig config)
         throws RepositoryException;

   /**
    * Load items. The properties of the items, with the exception of the body
    * fields, are always loaded. The flags provide hints to the implementation.
    * <P>
    * The resulting items are capable of loading the missing bodies and children
    * dynamically by calling internal methods of the repository.
    * <P>
    * Some other notes - you can load the root node by loading the path '/' by
    * itself. This can allow you to traverse the entire repository from the
    * returned content node.
    * 
    * @param paths the paths for the items that should be loaded
    * @param config the content manager configuration, may be <code>null</code>
    * @return the items, never <code>null</code>. Any items that do not exist
    *         in the repository will have a missing entry in the returned list.
    * @throws RepositoryException if another problem occurs while loading the
    *            item
    */
   List<Node> loadByPath(List<String> paths, PSContentMgrConfig config)
         throws RepositoryException;

   /**
    * Save the given objects in the repository. If an item already exists,
    * update its state with the new information. If an item is unchanged, this
    * does nothing. If the deep flag is set, the save will also apply to
    * children of any degree of a given item.
    * 
    * @param nodes items to save, never <code>null</code> or empty
    * @param deep if <code>true</code>, then the decendents of the passed
    *           items are also saved
    * @throws RepositoryException if or or more items in the database have been
    *            modified since the passed items were loaded, or if there's
    *            another problem with the repository
    */
   void save(List<Node> nodes, boolean deep) throws RepositoryException;

   /**
    * Delete the given items
    * 
    * @param nodes the items to delete, never <code>null</code> or empty
    * @throws ItemNotFoundException if one or more items are missing from the
    *            database
    * @throws RepositoryException if there's another problem with the repository
    */
   void delete(List<Node> nodes) throws ItemNotFoundException,
         RepositoryException;

   /**
    * Evict the specified item from the cache.
    * 
    * @param guids the item guids to evict, never <code>null</code>
    */
   void evict(List<IPSGuid> guids);

   /**
    * Update the content repository with the passed changes. The implementation
    * should guarantee that configuration cannot occur while the repository is
    * in use.
    * 
    * @param waitingChanges The list of modifications to handle, never
    *           <code>null</code>
    * 
    * @throws Exception if there is a problem processing the configuration
    */
   void configure(List<PSContentTypeChange> waitingChanges)
         throws Exception;

   /**
    * Load children (if any) for the nodes that are passed in. This is primarily
    * used internally in the implementation to allow lazy child loading.
    * 
    * @param nodes a list of nodes, never <code>null</code> or empty
    * @param config the content manager configuration, may be <code>null</code>
    * @throws RepositoryException if there is a problem loading the children
    */
   void loadChildren(List<Node> nodes, PSContentMgrConfig config)
         throws RepositoryException;

   /**
    * Load bodies and images (if any) for the nodes that are passed in. This is
    * primarily used internally in the implementation to allow lazy body
    * loading.
    * 
    * @param nodes a list of nodes, never <code>null</code> or empty
    * @throws RepositoryException if there is a problem loading the children
    */
   void loadBodies(List<Node> nodes) throws RepositoryException;

   /**
    * Perform the internal portion of the query, starting with the query
    * description in the query node.
    * 
    * @param maxresults the maximum number of results to return, may be 0 to
    *           indicate no limit
    * @param params a map of variables to be substituted in the query, may be
    *           <code>null</code>
    * @param locale the locale to use when sorting results, may be 
    *           <code>null</code> or empty.
    * @param query the query to perform, never <code>null</code>
    * @return the result object, never <code>null</code>
    * @throws InvalidQueryException if the query is invalid
    * @throws RepositoryException if the query fails
    */
   QueryResult executeInternalQuery(Query query, int maxresults,
         Map<String, ? extends Object> params, String locale) throws InvalidQueryException,
         RepositoryException;
   
   /**
    * Lookup the given node type and return the description
    * 
    * @param nodeDef the node definition, never <code>null</code>
    * @return the node type, never <code>null</code>
    * @throws NoSuchNodeTypeException if the node type given doesn't exist. This
    * would be odd since we have the definition.
    */
   NodeType findNodeType(PSNodeDefinition nodeDef) throws NoSuchNodeTypeException;
   
   /**
    * There are a set of fields used by the system that are externalized 
    * implicitly and made available in loaded nodes. 
    * 
    * @return the set of implicit property names, never <code>null</code> or 
    * empty.
    */
   Set<String> getUnmappedSystemFields();
}
