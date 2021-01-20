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
package com.percussion.services.contentmgr;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

/**
 * The content manager allows the loading of content items by either GUID or
 * path. Guids and paths can reference either content items or children. You
 * will need to construct special GUIDs to reference either. See
 * {@link com.percussion.services.guidmgr.data.PSLegacyGuid} for more details.
 * <p>
 * Path syntax uses the title fields from folders and items. The path starts
 * with a
 * <q>//</q>
 * for absolute paths. Relative paths start with no slash character. Paths
 * ending in #<em>nnn</em> indicate a specific revision of a content item.
 * <p>
 * Examples:<br/> <em>//Sites/EnterpriseInvestments/Funds/Morningfund</em>
 * references the
 * <q>Morningfund</q>
 * document in the
 * <q>Funds</q>
 * directory.<br/> <em>//Sites/CorporateInvestments/Company/R-and-B#2</em>
 * references revision 2 of the document
 * <q>R-and-B</q>
 * <br/> <em>//Sites/CorporateInvestments/Regions/NorthEast#3/Category#1</em>
 * references the first Category child of revision 3 of the
 * <q>NorthEast</q>
 * document
 * 
 * @author dougrand
 * 
 */
public interface IPSContentMgr extends IPSContentTypeMgr, QueryManager
{
   /**
    * Find the items named by the given paths. A path consists of a series of
    * names, separated by the slash character. The path may start with a slash,
    * in which case the <i>root </i> parameter is ignored and the search
    * commences with the repository root folder (the repository of the root
    * node).
    * <P>
    * To load a specific version of a node, append [v#] to the path, where # is
    * a dot separated version string. Most of the time # will be a simple
    * integer.
    * <P>
    * Depending on the options provided, the properties presented may or may
    * not have output translations applied. See {@link PSContentMgrOption} 
    * and {@link PSContentMgrConfig} for details.
    * 
    * @param sess the session object, must be <code>null</code> in Rhythmyx
    *           6.x, which identifies the workspace and root node to traverse to
    *           find the items specified by the paths. If <code>null</code>
    *           then the entire repository tree is used for the lookup.
    * @param paths the paths to the items, each element is found in turn by
    *           matching the name in the path.
    * @param config the configuration, may be <code>null</code>
    * @return the selected items, never <code>null</code>. The returned list
    *         can have fewer elements if one or more path was not found in the
    *         repository.
    * @throws PathNotFoundException if one or more paths contain missing
    *            elements
    * @throws RepositoryException if there is a problem in the repository
    */
   List<Node> findItemsByPath(Session sess, List<String> paths,
         PSContentMgrConfig config) throws PathNotFoundException,
         RepositoryException;

   /**
    * Find the items named by the given GUIDs. The guid contains the repository
    * index of the repository that has the given item. The passed guids
    * reference a particular version of a particular item, so no added version
    * information is needed.
    * <P>
    * Depending on the options provided, the properties presented may or may
    * not have output translations applied. See {@link PSContentMgrOption} 
    * and {@link PSContentMgrConfig} for details.
    * 
    * @param guids the guids for the items to be loaded, must never be
    *           <code>null</code> and may not contain <code>null</code>
    *           elements
    * @param config the configuration, may be <code>null</code>
    * @return the selected items, never <code>null</code>. The returned list
    *         can have fewer elements if one or more uuid was not found in any
    *         repository.
    * @throws RepositoryException if there is a problem in the repository
    */
   List<Node> findItemsByGUID(List<IPSGuid> guids, PSContentMgrConfig config)
         throws RepositoryException;

   /**
    * Find the items that are of the given type. If there are no items, then
    * this returns an empty list.
    * 
    * @param def the node definition to use in the search, never
    *           <code>null</code>
    * @return the selected items ids, never <code>null</code>. The returned
    *         list may be empty. The returned list can be passed to the GUID
    *         find method to load the nodes.
    * @throws RepositoryException if there is a problem in the repository
    */
   Collection<IPSGuid> findItemIdsByNodeDefinition(NodeDefinition def)
         throws RepositoryException;

   /**
    * Convenience method which calls {@link 
    * #executeQuery(Query, int, Map, String)} with <code>null</code> passed as
    * the value for locale.
    * 
    * @param query the query to perform, never <code>null</code>
    * @param maxresults the maximum results, or -1 for no limit
    * @param params params to be passed to expand variables in the query, may be
    *           <code>null</code>
    * @return a query result, never <code>null</code>
    * @throws InvalidQueryException if the query is invalid for some reason.
    *            Reasons include, but are not limited to, referencing
    *            non-existent content types, and content item properties.
    * @throws RepositoryException if some other problem occurs during the query.
    * @deprecated use {@link #executeQuery(Query, int, Map, String)} instead.
    */
   QueryResult executeQuery(Query query, int maxresults,
         Map<String, ? extends Object> params) throws InvalidQueryException,
         RepositoryException;
   
   /**
    * Execute the given query against the repository. The query was prepared
    * earlier using the query manager API. This is the method called when the
    * query executes itself.
    * 
    * @param query the query to perform, never <code>null</code>
    * @param maxresults the maximum results, or -1 for no limit
    * @param params params to be passed to expand variables in the query, may be
    *           <code>null</code>
    * @param locale the locale, which is used to determine the colating sequence
    *           when ordering results, may be <code>null</code> or empty, in 
    *           which case the current JVM locale is used.
    * @return a query result, never <code>null</code>
    * @throws InvalidQueryException if the query is invalid for some reason.
    *            Reasons include, but are not limited to, referencing
    *            non-existent content types, and content item properties.
    * @throws RepositoryException if some other problem occurs during the query.
    */
   QueryResult executeQuery(Query query, int maxresults,
         Map<String, ? extends Object> params, String locale)
   throws InvalidQueryException, RepositoryException;

   /**
    * Filter the input set of content guids to those that are members of at
    * least one of the passed content types
    * 
    * @param types the input content types, never <code>null</code>
    * @param ids the input guids, never <code>null</code>
    * @return those items that match the content type list, never
    *         <code>null</code> but may be empty.
    */
   Collection<IPSGuid> filterItemsByNodeDefinitions(Set<IPSGuid> types,
         Collection<IPSGuid> ids);
   
   /**
    * Save one or more content items to the repository. Each item is in one of
    * two states: either it is a fresh item that is to be persisted, in which
    * case there should be no existing object of that id in the database 
    * (understanding that we share content status records for different versions
    * of a single content item), or it is an existing object that will be merged
    * to the existing object in the database.
    * <p>
    * It is an error to save a fresh item and find an existing backing object 
    * for the shared or local definition tables, and it is an error to save 
    * an existing object and not to find it's backing object in the database.   
    * <P>
    * For the merge case, only unmodified properties are merged back to the 
    * existing object. For the new case, the node being persisted must have
    * been created using {@link #createItem(NodeDefinition)} or 
    * {@link #createNewRevision(Node)}. 
    * <p>
    * Depending on the options provided, the properties presented may or may not
    * have input translations and validations applied. See
    * {@link PSContentMgrOption} and {@link PSContentMgrConfig} for details.
    * 
    * @param items the items to save, must not be <code>null</code> and must
    *           not be empty
    * @param config the configuration that dictates what options are to be
    *           applied to the save process, never <code>null</code>
    * @throws RepositoryException if one or more items fail a validation then a
    *            <code>ConstraintViolationException</code> is thrown, if there
    *            is an integrity problem with one or more items then one or more
    *            subclasses of this exception may be thrown. An underlying
    *            problem with the repository will throw a straight version of
    *            this exception that is chained to the original ORM problem.
    */
   void saveItems(List<Node> items, PSContentMgrConfig config)
      throws RepositoryException;
   
   /**
    * Create a new content item of the given type. The returned content item's
    * fields will be populated as appropriate using the information about the
    * content editor held by the item definition manager. Initial values must
    * be defined by JEXL scripts for this to take place. Calling this method
    * with an "old" content type will work, but fields with initial values 
    * will not be populated.
    * 
    * @param def the content type definition, never <code>null</code>. 
    * @return the new content item, not yet persisted to the database
    */
   Node createItem(NodeDefinition def);
   
   /**
    * Create a new revision of the existing content item. The new revision
    * is a clone of the existing item, with the exception that the revision
    * will be incremented by one. After this method, the tip revision in the
    * status record will be incremented.
    * 
    * @param existing the existing node, never <code>null</code>
    * @return the new content item, not yet persisted to the database
    */
   Node createItemRevision(Node existing);

   /**
    * Create a new copy of the existing content item. The new copy
    * is a clone of the existing item, but is represented by a separate
    * contentid. Properties and children are copied one for one from the 
    * existing item to the new item.
    * 
    * @param existing the existing node, never <code>null</code>
    * @return the new content item, not yet persisted to the database
    */
   Node copyItem(Node existing);
   
   /**
    * Delete one or more items from the repository. For each item, any existing
    * relationships are removed (whether the relationship is to the item as
    * an owner or dependent), and the status records, shared records and 
    * local records are removed from the repository.
    * <p>
    * Before performing the deletion(s), the method first validates that all
    * the items exist. If an item does not exist, no deletions are performed.
    * 
    * @param items the guids referencing the items to be purged from the 
    * repository, never <code>null</code> or empty
    * @throws RepositoryException if an item does not exist, or cannot be
    * deleted due to a repository problem
    */
   void deleteItems(List<IPSGuid> items) throws RepositoryException;
    
   /**
    * Finds all the content ids of the supplied content type that has the supplied supplied title. Finds the ids
    * case insensitively.
    * @param contentTypeId, must not be <code>null</code>.
    * @param title, must not be <code>null</code>
    * @return list of content ids, may be empty if no items found. Never <code>null</code>.
    * @throws RepositoryException if the content type does not exist.
    */
   public List<String> findNodesByTitle(Long contentTypeId, String title)throws RepositoryException;

   /**
    * Finds all the content ids of the supplied content type that has the supplied field name and field value. Finds the 
    * ids case insensitively.
    * @param contentTypeId, must not be <code>null</code>.
    * @param fieldName, must not be <code>null</code>
    * @param fieldValue, must not be <code>null</code>
    * @return list of content ids, may be empty if no items found. Never <code>null</code>.
    * @throws RepositoryException if the content type does not exist.
    */
   public List<Integer> findItemsByLocalFieldValue(long contentTypeId, String fieldName, String fieldValue);
}
