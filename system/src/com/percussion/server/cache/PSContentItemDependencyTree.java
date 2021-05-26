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
package com.percussion.server.cache;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.data.IPSBackEndErrors;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.extension.services.PSDatabasePool;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSSqlHelper;
import com.percussion.util.PSStopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds and manages a dependency tree of related content items.
 */
public class PSContentItemDependencyTree
{
   /**
    * Makes an internal request to query the server for all rows in the
    * relationship tables using the relationship command handler and stores the
    * results.
    *
    * @throws PSCacheException if no internal request handler was found and for
    *    all internal request execution errors caught.
    */
   PSContentItemDependencyTree() throws PSCacheException
   {
      ms_itemDependencyTree = this;

      try
      {
         m_dependencyMap = initDependencyMap();
      }
      catch (Exception e)
      {
         /**
          * The internal request failed for some reason. Passing this
          * exception up to the next level.
          */
         Object[] args =
         {
            e.getLocalizedMessage()
         };
         throw new PSCacheException(
            IPSServerErrors.CACHE_UNEXPECTED_EXCEPTION, args);
      }
   }

   /**
    * Get the instance of the item dependency tree. This method must be called
    * after this instance has been initialized by the cache manager.
    * <p>
    * This API is a <b>HACK</b> to access the cached (active assembly)
    * relationships. Do not depend on this API, it is subject to be changed
    * after the "proper" API is available in the future.
    *
    * @return The single instance of the dependency tree,
    *    never <code>null</code>.
    *
    * @throws IllegalStateException if it has not been initialized by the
    *    cache manager.
    */
   public static PSContentItemDependencyTree getInstance()
   {
      if (ms_itemDependencyTree == null)
         throw new IllegalStateException(
            "The PSContentItemDependencyTree has not been initialized");

      return ms_itemDependencyTree;
   }

   /**
    * Package private constructor to be used for test purposes only.
    *
    * @param relationships the relationship set to use to construct this tree,
    *    may not be <code>null</code>.
    * @throws PSCacheException if no internal request handler was found and for
    *    all internal request execution errors caught.
    */
   PSContentItemDependencyTree(PSRelationshipSet relationships)
      throws PSCacheException
   {
      if (relationships == null)
        throw new IllegalArgumentException("relationships cannot be null");

      m_dependencyMap = initDependencyMap(relationships);
   }

   /**
    * Initializes a dependency map from the database and returns it.
    * It does not use the relationship processer, but uses JDBC API to
    * to retrieve all "Active Assembly" relationships from the 
    * {@link IPSConstants#PSX_RELATIONSHIPS} view. It speeds up loading the 
    * relationship tree.
    *
    * @return the dependency map initialized, never <code>null</code>, may be
    *    empty.
    *
    * @throws NamingException if the default connection details cannot be
    * obtained.
    * @throws SQLException if SQL error occurs.    *
    * @throws PSException if any other error occurs.
    */
   private Map initDependencyMap() throws SQLException, PSException,
      NamingException
   {
      Map dependencyMap = new Hashtable();
      Integer key = null;
      List value = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      Connection conn = null;
      try
      {
         PSStopwatch watch = new PSStopwatch();
         watch.start();

         String query = getAAInfoQuery();
         conn = PSDatabasePool.getDatabasePool().getConnection();
         if (conn == null)
            throw new PSException(IPSBackEndErrors.EXEC_DATA_NO_CONNECTIONS);
         stmt = PSPreparedStatement.getPreparedStatement(conn, query);
         rs = stmt.executeQuery();

         // Get the list of ids
         while (rs.next())
         {
            int sysId = rs.getInt(1);
            int contentId = rs.getInt(2);
            int revisionId = rs.getInt(3);
            int relatedContentId = rs.getInt(4);
            String temp = rs.getString(5);
            int variantId = -1;
            try
            {
               variantId = Integer.parseInt(temp);
            }
            catch (NumberFormatException e)
            {
               /**
                * This should never happen, but if it does we would like
                * to know. Also if this happens we will continue
                * constructing the dependency tree.
                */
               Object[] args = {String.valueOf(sysId), e.getLocalizedMessage()};
               PSCacheException ce = new PSCacheException(
                  IPSServerErrors.CACHE_DEPENDENCY_SKIPPED, args);
               PSConsole.printMsg("Cache", ce);
               continue;
            }
            // then fill the dependency map
            if (key == null || key.intValue() != relatedContentId)
            {
               key = new Integer(relatedContentId);
               Object test = dependencyMap.get(key);
               if (test != null)
                  value = (List) test;
               else
               {
                  value = new ArrayList();
                  dependencyMap.put(key, value);
               }
            }
            PSItemDependency dependency =
               new PSItemDependency(
                  contentId,
                  revisionId,
                  relatedContentId,
                  sysId,
                  variantId);
            value.add(dependency);
         }
         watch.stop();
         log.debug("Initialize DependencyTree took: {}",  watch.toString());
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         String msg = e.toString();
         throw new RuntimeException(msg);
      }
      finally
      {
         if (null != rs)
            try
            {
               rs.close();
            }
            catch (Exception e)
            {
            };
         if (null != stmt)
            try
            {
               stmt.close();
            }
            catch (Exception e)
            {
            };
         if (conn != null)
            conn.close();
      }
      return dependencyMap;
   }

   /**
    * @return the query that catalog the dependency info from all
    * Active Assembly (category) relationships
    */
   private String getAAInfoQuery()
   {
      // get all relationship configs for the Active Assebly category
      PSRelationshipConfigSet configSet = PSRelationshipCommandHandler
         .getConfigurationSet();
      Collection<PSRelationshipConfig> configs = configSet
         .getConfigListByCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);

      // build the query like:
      // select r... where {@link IPSConstants#PSX_RELATIONSHIPS} r where config_id in
      //      (select n.config_id from IPSConstants.PSX_RELATIONSHIPCONFIGNAME} n where
      //           n.config_name in (...)
      StringBuffer buf = new StringBuffer();

      buf.append("SELECT r.RID, r.OWNER_ID, r.OWNER_REVISION, r.DEPENDENT_ID, r.VARIANT_ID");
      buf.append(" FROM ");
      try {
         buf.append(PSSqlHelper.qualifyTableName(IPSConstants.PSX_RELATIONSHIPS));
      } catch (SQLException e) {
         throw new RuntimeException(e);
      }
      buf.append(" r ");
      buf.append("WHERE r.VARIANT_ID IS NOT NULL AND r.CONFIG_ID IN (");

      // build the sub select query
      buf.append("SELECT n.CONFIG_ID ");
      buf.append(" FROM ");
      try {
         buf.append(PSSqlHelper.qualifyTableName(IPSConstants.PSX_RELATIONSHIPCONFIGNAME));
      } catch (SQLException e) {
         throw new RuntimeException(e);
      }
      buf.append(" n ");
      buf.append("WHERE n.CONFIG_NAME IN (");

      boolean isFirst = true;
      for (PSRelationshipConfig config : configs)
      {
         if (! isFirst)
            buf.append(",");
         buf.append("'");
         buf.append(config.getName());
         buf.append("'");
         isFirst = false;
      }
      buf.append(")"); // close the IN clause for sub select query

      buf.append(")"); // close the IN clause for the main query

      return buf.toString();
   }

   /**
    * Initializes a dependency map from the supplied relationships and returns
    * it.
    *
    * @param relationships the relationships to initialize from, assumed not
    *    <code>null</code>.
    * @return the dependency map initialized, never <code>null</code>, may be
    *    empty.
    *
    * @deprecated This should not be used, it is too slow.
    *    Use {@link #initDependencyMap()} instead.
    */
   private Map initDependencyMap(PSRelationshipSet relationships)
   {
      Map dependencyMap = new Hashtable();

      Integer key = null;
      List value = null;
      Iterator iterator = relationships.iterator();
      while (iterator.hasNext())
      {
         PSRelationship relationship = (PSRelationship) iterator.next();

         /**
          * We are only interested in relationship changes for Active Assembly
          * categorys.
          */
         String category = relationship.getConfig().getCategory();
         if (category == null ||
            !category.equals(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY))
            continue;

         int sysId = relationship.getId();
         try
         {
            int contentId = relationship.getOwner().getId();
            int revisionId = relationship.getOwner().getRevision();
            int relatedContentId = relationship.getDependent().getId();
            int variantId = Integer.parseInt(
               relationship.getProperty(IPSHtmlParameters.SYS_VARIANTID));

            // then fill the dependency map
            if (key == null || key.intValue() != relatedContentId)
            {
               key = new Integer(relatedContentId);
               Object test = dependencyMap.get(key);
               if (test != null)
                  value = (List) test;
               else
               {
                  value = new ArrayList();
                  dependencyMap.put(key, value);
               }
            }

            PSItemDependency dependency = new PSItemDependency(
               contentId, revisionId, relatedContentId, sysId, variantId);
            value.add(dependency);
         }
         catch (NumberFormatException e)
         {
            /**
             * This should never happen, but if it does we would like
             * to know. Also if this happens we will continue
             * constructing the dependency tree.
             */
            Object[] args = {String.valueOf(sysId), e.getLocalizedMessage()};
            PSCacheException ce = new PSCacheException(
               IPSServerErrors.CACHE_DEPENDENCY_SKIPPED, args);
            PSConsole.printMsg("Cache", ce);
         }
      }

      return dependencyMap;
   }

   /**
    * Adds a dependency to the tree and returns all affected items above it in
    * the related content tree.
    *
    * @param sysId The id used to uniquely identify this dependency.
    * @param contentId The content id of the dependent item in the tree.
    * @param revisionId The revision id of the dependent item in the tree.
    * @param variantId The variant id of the dependent item in the tree.
    * @param relatedId The content id of the related item.
    * @param done a map of related content id / revision id pairs as Integers
    *    that have been processed, never <code>null</code>. This map is used to
    *    avoid endless loops or re-process the same content id. Caller should
    *    pass the same map object when processing multiple relationships.
    *
    * @return The items "above" the dependent added item as a <code>List</code>
    *    with a 2-dimensional array, [0] the contentid and [1] the revisionid,
    *    both specified as Strings. Never <code>null</code>, may be empty.
    */
   List addDependency(int sysId, int contentId, int revisionId,
      int relatedId, int variantId, Map done)
   {
      if (done == null)
         throw new IllegalArgumentException("done may not be null.");
      
      ItemSet result = new ItemSet();

      Integer key = new Integer(relatedId);
      List value = null;

      // adding to the dependency map
      Object test = m_dependencyMap.get(key);
      if (test != null)
      {
         value = (List) test;
      }
      else
      {
         value = new ArrayList();
         m_dependencyMap.put(key, value);
      }

      PSItemDependency dependency = new PSItemDependency(contentId, revisionId,
         relatedId, sysId, variantId);
      value.add(dependency);

      // get all dependencies of the dependent item
      add(new Integer(contentId), new Integer(revisionId), result, done);
      return convertSetToList(result);
   }

   /**
    * Convert a set of items to a list.
    *
    * @param resultSet A set of <code>Item</code> objects, assume not
    *    <code>null</code>, but may be empty.
    *
    * @return The converted list. Each element in the list is a
    *    <code>String[2]</code> object, where [0] is the contentid and [1] is
    *    the revision number. Never <code>null</code>, may be empty.
    */
   private List convertSetToList(Set resultSet)
   {
      List result = new ArrayList();

      Iterator items = resultSet.iterator();
      while (items.hasNext())
      {
         Item item = (Item) items.next();
         String[] entry =
         {
            String.valueOf(item.m_id),
            String.valueOf(item.m_rev)
         };
         result.add(entry);
      }

      return result;
   }

   /**
    * Removes a dependency from the tree and returns all affected items above
    * it including itself in the related content tree.
    *
    * @param sysId The id used to uniquely identify this dependency.
    * @param done a map of related content id / revision id pairs as Integers
    *    that have been processed, never <code>null</code>. This map is used to
    *    avoid endless loops or re-process the same content id. Caller should
    *    pass the same map object when processing multiple relationships.
    *
    * @return The items "above" the removed item including the removed item
    *    as a <code>List</code> with a 2-dimensional array, [0] the contentid
    *    and [1] the revisionid, both specified as Strings. Never
    *    <code>null</code>, may be empty.
    */
   List removeDependency(int sysId, Map done)
   {
      if (done == null)
         throw new IllegalArgumentException("done may not be null.");

      ItemSet result = new ItemSet();

      Iterator dependencies = new HashSet(m_dependencyMap.values()).iterator();
      while (dependencies.hasNext())
      {
         List itemList = (List) dependencies.next();
         if (itemList != null)
         {
            Iterator items = itemList.iterator();
            while (items.hasNext())
            {
               PSItemDependency item = (PSItemDependency) items.next();
               if (item.getSysId() == sysId)
               {
                  Integer cid = new Integer(item.getRelatedContentid());
                  Integer rid = new Integer(-1);
                  add(cid, rid, result, done);
                  items.remove();
                  return convertSetToList(result);
               }
            }
         }
      }

      return convertSetToList(result);
   }

   /**
    * Updates the dependencies for the supplied parameters. Will add a new
    * dependency if none was found.
    *
    * @param sysId The id used to uniquely identify this dependency.
    * @param contentId The content id of the item in the tree.
    * @param revisionId The revision id of the item in the tree.
    * @param relatedId The content id of the related item.
    * @param variantId The variant id of the item in the tree.
    * @param done a map of related content id / revision id pairs as Integers
    *    that have been processed, never <code>null</code>. This map is used to
    *    avoid endless loops or re-process the same content id. Caller should
    *    pass the same map object when processing multiple relationships.
    *
    * @return The items "above" the updated item as a <code>List</code> with
    *    a 2-dimensional array, [0] the contentid and [1] the revisionid, both
    *    specified as Strings. Never <code>null</code>, may be empty.
    */
   List updateDependency(int sysId, int contentId, int revisionId,
      int relatedId, int variantId, Map done)
   {
      if (done == null)
         throw new IllegalArgumentException("done may not be null.");

      // update the dependency map
      Integer cid = new Integer(relatedId);

      List value = (List) m_dependencyMap.get(cid);
      if (value == null)
      {
         value = new ArrayList();
         m_dependencyMap.put(cid, value);
         value.add(new PSItemDependency(contentId, revisionId, relatedId, sysId,
            variantId));
      }
      else
      {
         for (int i=0; i<value.size(); i++)
         {
            PSItemDependency item = (PSItemDependency) value.get(i);
            if (sysId == item.getSysId())
            {
               value.set(i, new PSItemDependency(contentId, revisionId,
                  relatedId, sysId, variantId));
               break;
            }
         }
      }

      ItemSet result = new ItemSet(contentId, revisionId);
      add(new Integer(contentId), new Integer(revisionId), result, done);

      return convertSetToList(result);
   }

   /**
    * Gets the items above the specified item in the related content tree.
    * Three parameter combinations are supported. Parameters not used must be
    * set to -1. These are the supported cases:
    * <ol><li>
    * Only contentId is provided: all revisions of the item will used for the
    * selection process. If a variant id is provided, it will be ignored.
    * </li>
    * <li>
    * ContentId and revisionId are provided: the supplied content id and
    * revision id will be used for the selection process. If a variantid is
    * provided, it will be ignored.
    * </li>
    * <li>
    * Only variantId is provided: the variant id is used for the selection
    * process only if content id is specified as -1.
    * </li></ol>
    *
    * @param contentId The content id of the item in the tree, provide -1 if
    *    not used for the selection process.
    * @param revisionId The revision id of the item in the tree, provide -1 if
    *    not used for the selection process.
    * @param variantId The variant id of the item in the tree, provide -1 if
    *    not used for the selection process.
    *
    * @return The items "above" the supplied item as a <code>List</code> with
    *    a 2-dimensional array, [0] the contentid and [1] the revisionid, both
    *    specified as Strings. Never <code>null</code>, may be empty.
    */
   List getDependentItems(int contentId, int revisionId, int variantId)
   {
      ItemSet result = new ItemSet(contentId, revisionId);

      Map done = new HashMap();
      if (contentId >= 0)
      {
         Integer cid = new Integer(contentId);
         Integer rid = null;
         rid = new Integer(revisionId);
         add(cid, rid, result, done);
      }
      else if (variantId >= 0)
      {
         PSItemDependency item = null;
         Iterator dependencies = new HashSet(m_dependencyMap.values())
               .iterator();
         while (dependencies.hasNext())
         {
            List items = (List) dependencies.next();
            if (items != null)
            {
               for (int i=0; i<items.size(); i++)
               {
                  item = (PSItemDependency) items.get(i);
                  if (item.getVariantId() == variantId)
                  {
                     Integer cid = new Integer(item.getRelatedContentid());
                     Integer rid = new Integer(-1);
                     add(cid, rid, result, done);
                  }
               }
            }
         }
      }
      else
      {
         throw new UnsupportedOperationException(
            "The supplied parameter specification is not supported.");
      }

      return convertSetToList(result);
   }

   /**
    * Add the related item for the supplied content id and revision to the
    * provided target list. This is called recursively.
    *
    * @param cid the related content id for which to add all dependents to the
    *    target list, assumed not <code>null</code>.
    * @param rid the related revision id for which to add all dependents to the
    *    target list, assumed not <code>null</code>.
    * @param target the list to which all  dependents are added, assumed not
    *    <code>null</code>.
    * @param done a map of related content id / revision id pairs as Integers
    *    that have been processed, assumed not <code>null</code>. This map is
    *    used to avoid endless loops.
    */
   private void add(Integer cid, Integer rid, ItemSet target, Map done)
   {
      if (done.get(cid) != null)
         return;
      else
         done.put(cid, rid);

      List dependencies = (List) m_dependencyMap.get(cid);
      if (dependencies != null)
      {
         Iterator deps = dependencies.iterator();
         while (deps.hasNext())
         {
            PSItemDependency dependency = (PSItemDependency)deps.next();
            target.add(dependency.getContentId(), dependency.getRevisionId());
            add(new Integer(dependency.getContentId()),
               new Integer(dependency.getRevisionId()), target, done);
         }
      }
   }

   /**
    * Get the owners of the given content id.
    * <p>
    * This API is a <b>HACK</b> see {@link #getInstance()} for detail
    *
    * @param dependentid The dependent id, never <code>null</code>.
    *
    * @return An iterator over zero or more owner ids in <code>Integer</code>
    *    objects, never <code>null</code>.
    */
   public Iterator getOwners(Integer contentid)
   {
      List ownerList = (List) m_dependencyMap.get(contentid);
      if (ownerList != null)
      {
         Iterator items = ownerList.iterator();
         List ids = new ArrayList();
         while (items.hasNext())
         {
            PSItemDependency item = (PSItemDependency) items.next();
            ids.add(new Integer(item.getContentId()));
         }
         return ids.iterator();
      }
      else
         return Collections.EMPTY_LIST.iterator();
   }

   /**
    * Creates a String representation of this object. Currently this is the
    * <code>m_dependencyMap</code> member. Used for testing.
    *
    * @return the String representation of this object, never <code>null</code>.
    */
   public String toString()
   {
      StringBuffer buf = new StringBuffer();

      buf.append("Dependencies(");
      Iterator keys = m_dependencyMap.keySet().iterator();
      while (keys.hasNext())
      {
         Integer key = (Integer) keys.next();
         List dependency = (List) m_dependencyMap.get(key);
         buf.append("key= ");
         buf.append(key.toString());
         buf.append(" value= ");
         if (dependency != null)
         {
            for (int i=0; i<dependency.size(); i++)
            {
               buf.append(dependency.get(i).toString());
               buf.append(", ");
            }
         }
         else
            buf.append("null");
         buf.append(")");
      }

      return buf.toString();
   }

   /**
    * Maps the relationships between all related content items.  Initialized
    * during construction.  Each key is an <code>Integer</code>, specifying
    * the content id of a related item.  Each value a List of content items that
    * are related to the contentid specified by the key, each as a
    * {@link PSItemDependency} object.  Never <code>null</code> after
    * construction.
    */
   private Map<Integer, List<PSItemDependency>> m_dependencyMap = null;

   /**
    * Encapsulates data describing a content relationship from a dependent item
    * to its related item.
    */
   private class PSItemDependency
   {
      /**
       * Constructs a dependency between a content item and a related item.
       *
       * @param contentId The content id of the dependent item.
       * @param revisionId The revision id of the dependent item.
       * @param relatedContentId The content id of the related item.
       * @param sysId The id that uniquely identifies this relationship in the
       *    database.
       * @param variantId The variantId of the dependent item.
       */
      public PSItemDependency(int contentId, int revisionId,
         int relatedContentId, int sysId, int variantId)
      {
         m_sysId = sysId;
         m_contentId = contentId;
         m_revisionId = revisionId;
         m_relatedContentId = relatedContentId;
         m_variantId = variantId;
      }

      /**
       * Gets the contentid of the item to which this dependency is related.
       *
       * @return The content id.
       */
      public int getRelatedContentid()
      {
         return m_relatedContentId;
      }

      /**
       * Gets the unique Id of this relationship.
       *
       * @return The sysId.
       */
      public int getSysId()
      {
         return m_sysId;
      }

      /**
       * Get the content id of the dependent item.
       *
       * @return The content id.
       */
      public int getContentId()
      {
         return m_contentId;
      }

      /**
       * Get the revision id of the dependent item.
       *
       * @return The revision id.
       */
      public int getRevisionId()
      {
         return m_revisionId;
      }

      /**
       * Get the variant id of the dependent item.
       *
       * @return The variant id.
       */
      public int getVariantId()
      {
         return m_variantId;
      }

      /**
       * Produces a String representation of this class. Used for testing.
       * 
       * @return "dependency=(#<i>sysId</i>:(<i>ownerId</i>,<i>ownerRevision</i>)
       *         &lt;-<i>dependentId</i>[<i>variantId</i>])", never
       *         <code>null</code>.
       */
      public String toString()
      {
         StringBuffer buf = new StringBuffer();
         buf.append("dependency=(#");
         buf.append(m_sysId).append(":");
         buf.append("(").append(m_contentId).append(",");
         buf.append(m_revisionId).append(")<-");
         buf.append(m_relatedContentId).append("[");
         buf.append(m_variantId).append("])");
         return buf.toString();
      }

      /**
       * The id that uniquely identifies this relationship in the database,
       * initialized in the constructor, never changed after that.
       */
      private int m_sysId = 0;

      /**
       * The content id of the dependent item, initialized in the constructor,
       * never changed after that.
       */
      private int m_contentId = 0;

      /**
       * The revision id of the dependent item, initialized in the constructor,
       * never changed after that.
       */
      private int m_revisionId = 0;

      /**
       * The content id of the related item, initialized in the constructor,
       * never changed after that.
       */
      private int m_relatedContentId = 0;

      /**
       * The variant id of the related item, initialized in the constructor,
       * never changed after that.
       */
      private int m_variantId = 0;
   }

   /**
    * Inner class to create a set of <code>Item</code> objects returned by
    * various interface methods.
    */
   private class ItemSet extends HashSet
   {
      /**
       * Generated serial number
       */
      private static final long serialVersionUID = -9072684561956368546L;

      /**
       * The default constructor, creates an empty set.
       */
      public ItemSet()
      {
      }

      /**
       * Creates an empty set with the supplied content id / revision id
       * excluded.
       *
       * @param cid the content id to exclude from this list.
       * @param rid the revision id to exclude from this list.
       */
      public ItemSet(int cid, int rid)
      {
         m_cid = cid;
         m_rid = rid;
      }

      /**
       * Adds a new entry for the provided contentId and revisionId. The
       * entry is not added if it matches the exclude signature.
       *
       * @param contentId the contentId to add.
       * @param revisionId the revisionId to add.
       */
      public void add(int contentId, int revisionId)
      {
         if (include(contentId, revisionId))
         {
            Item item = new Item(contentId, revisionId);

            if (!this.contains(item))
               this.add(item);
         }
      }

      /**
       * Should we include an entry for the supplied content id / revision id
       * in this set.
       *
       * @param cid the content id to test.
       * @param rid the revision id to test.
       * @return <code>true</code> if an entry for the supplied parameters
       *    should be included, <code>false</code> otherwise.
       */
      private boolean include(int cid, int rid)
      {
         if (m_cid == -1 || m_rid == -1)
            return true;

         if (cid == m_cid && rid == m_rid)
            return false;

         return true;
      }

      /**
       * The content id for the item to be excluded in this list.
       */
      private int m_cid = -1;

      /**
       * The revision id for the item to be excluded in this list.
       */
      private int m_rid = -1;
   }

   /**
    * The inner class to contain the content id and revision for a given item.
    */
   private class Item
   {
      /**
       * Constructs an item from a id and revision.
       *
       * @param id The content id of the item.
       * @param rev The revision number of the item.
       */
      Item(int id, int rev)
      {
         m_id = id;
         m_rev = rev;
         String hashCode = String.valueOf(id) + String.valueOf(rev);
         m_hashCode = hashCode.hashCode();
      }

      // Override equals(Object) of the base class
      public boolean equals(Object obj)
      {
         if (obj instanceof Item)
         {
            Item other = (Item) obj;

            return m_id == other.m_id && m_rev == other.m_rev;
         }
         else
         {
            return false;
         }
      }

      // Override equals(Object) of the base class
      public int hashCode()
      {
         return m_hashCode;
      }

      // See ctor for description
      private int m_id;

      // See ctor for description
      private int m_rev;

      // Hash code for this object
      private int m_hashCode;
   }

   /**
    * Singleton instance of the item dependency tree.  Not <code>null</code>
    * after call to ctor by the server.
    */
   private static PSContentItemDependencyTree ms_itemDependencyTree = null;


   /**
    * The log4j logger used for this class.
    */
   private static final Logger log = LogManager.getLogger(PSContentItemDependencyTree.class);

}
