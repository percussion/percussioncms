/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.taxonomy.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.contentmgr.impl.IPSContentRepository;
import com.percussion.services.contentmgr.impl.PSContentInternalLocator;
import com.percussion.services.contentmgr.impl.legacy.PSContentRepository;
import com.percussion.services.contentmgr.impl.legacy.PSTypeConfiguration;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.taxonomy.domain.Attribute;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Node_editor;
import com.percussion.taxonomy.domain.Node_status;
import com.percussion.taxonomy.domain.Related_node;
import com.percussion.taxonomy.domain.Value;
import com.percussion.taxonomy.web.AbstractTaxonEditorController;
import com.percussion.utils.guid.IPSGuid;

import java.util.Arrays;
import java.util.Collections;

@Transactional
public class HibernateNodeDAO extends HibernateDaoSupport implements NodeDAO {

   ///////////////////////////////////////////////////////////////////////////////////////////////////////////

   @Override
   
   @SuppressWarnings("unchecked")
   public Node getNode(int nodeID, int langID) {
      
      String queryString = "select distinct n from Node n ";
      queryString += "left join fetch n.taxonomy t ";
      queryString += "left join fetch n.nodeEditors ne ";
      queryString += "left join fetch n.relatedNodesForNodeId rn ";
      queryString += "left join fetch n.values v ";
      queryString += "join fetch v.attribute a ";
      queryString += "left join fetch a.attribute_langs al ";
      queryString += "join fetch al.language ";
      queryString += "join fetch v.lang ";
      queryString += "where ";
      queryString += "n.id = " + nodeID + " ";
      queryString += "and al.language.id = " + langID + " ";
      queryString += "and v.lang.id = " + langID + "order by n.id";
      
      return ((Collection<Node>) this.getHibernateTemplate().execute(new HibernateQuery(queryString))).iterator().next();
   }

   @SuppressWarnings("unchecked")
   public Collection<Node> getAllNodes(int taxID, int langID) {
      
      String queryString = "select distinct n from Node n ";
      queryString += "left join fetch n.taxonomy ";
      queryString += "left join fetch n.nodeEditors ne ";
      queryString += "left join fetch n.relatedNodesForNodeId rn ";
      queryString += "join fetch n.values v ";
      queryString += "join fetch v.attribute a ";
      queryString += "left join fetch a.attribute_langs al ";
      queryString += "join fetch al.language ";
      queryString += "join fetch v.lang ";
      queryString += "where ";
      queryString += "n.taxonomy.id = " + taxID + " ";
      queryString += "and al.language.id = " + langID + " ";
      queryString += "and v.lang.id = " + langID + "order by n.id";

      return (Collection<Node>) this.getHibernateTemplate().execute(new HibernateQuery(queryString));
   }

   @SuppressWarnings("unchecked")
   public Collection<Node> getNodesFromSearch(int taxID, int langID, String search_string, boolean exclude_disabled) {
      ArrayList<Object> bind_vars = new ArrayList<Object>();
      
      String queryString = "select distinct n from Node n ";
      queryString += "left join fetch n.taxonomy ";
      queryString += "left join fetch n.nodeEditors ne ";
      queryString += "left join fetch n.relatedNodesForNodeId rn ";
      queryString += "join fetch n.values v ";
      queryString += "join fetch v.attribute a ";
      queryString += "left join fetch a.attribute_langs al ";
      queryString += "join fetch al.language ";
      queryString += "join fetch v.lang ";
      queryString += "where ";
      queryString += "n.taxonomy.id = " + taxID + " ";
     
      if (exclude_disabled) {
         queryString += "and n.Not_leaf = ? ";
         queryString += "and n.node_status.id = " + Node_status.ACTIVE + " ";
         bind_vars.add(false);
      }
      
      queryString += "and al.language.id = " + langID + " ";
      queryString += "and v.lang.id = " + langID + " ";
      queryString += "and lower(v.Name)  like ? order by n.id";

      bind_vars.add("%" + StringUtils.lowerCase(StringUtils.trimToEmpty(search_string)) + "%");

      return (Collection<Node>) getHibernateTemplate().find(queryString, bind_vars.toArray());
   }

   // Better SQL would eliminate the need for this function
   private Collection<Object[]> concatNames(Collection<Object[]> raw) {
      
      Collection<Object[]> refined = new ArrayList<Object[]>();

      // Cache for int node_id, int parent_id, String name, boolean Not Leaf,
      // int node_status.id
      Object[] last_node = new Object[5];
      last_node[0] = -1;
      String current_name = "";

      for (Iterator<Object[]> itr = raw.iterator(); itr.hasNext();) {
         Object[] current_node = itr.next();

         if (last_node[0].equals(-1)) {
            last_node = current_node;
         }

         if (((Integer) current_node[0]).equals(((Integer) last_node[0])) && itr.hasNext()) {
            
            current_name += (String) current_node[2] + " (";
            
         } else if (!((Integer) current_node[0]).equals(((Integer) last_node[0])) && itr.hasNext()) {
            
            // Save last_node
            last_node[2] = current_name + ")";
            
            // fix parens
            if (StringUtils.countMatches(last_node[2].toString(), "(") > StringUtils.countMatches(
                  last_node[2].toString(), ")")) {
               
               last_node[2] = StringUtils.replace(last_node[2].toString(), " ()", ")");
               
            } else {
               last_node[2] = StringUtils.replace(last_node[2].toString(), " ()", "");
            }
            
            refined.add(last_node);
            
            // reset last node to current
            last_node = current_node;
            current_name = (String) current_node[2] + " (";
            
         } else if (((Integer) current_node[0]).equals(((Integer) last_node[0])) && !itr.hasNext()) {

            // Save last_node
            current_name += (String) current_node[2] + " (";
            last_node[2] = current_name + ")";
            
            // fix parens
            if (StringUtils.countMatches(last_node[2].toString(), "(") > StringUtils.countMatches(
                  last_node[2].toString(), ")")) {
               
               last_node[2] = StringUtils.replace(last_node[2].toString(), " ()", ")");
               
            } else {
               last_node[2] = StringUtils.replace(last_node[2].toString(), " ()", "");
            }
            refined.add(last_node);
            
         } else if (!((Integer) current_node[0]).equals(((Integer) last_node[0])) && !itr.hasNext()) {
            
            // Save last_node
            last_node[2] = current_name + ")";
            
            // fix parens
            if (StringUtils.countMatches(last_node[2].toString(), "(") > StringUtils.countMatches(
                  last_node[2].toString(), ")")) {
               
               last_node[2] = StringUtils.replace(last_node[2].toString(), " ()", ")");
               
            } else {
               last_node[2] = StringUtils.replace(last_node[2].toString(), " ()", "");
            }
            
            refined.add(last_node);
            
            // save current node
            refined.add(current_node);
         }
      }
      return (refined);
   }

   /**
    * Return nodeID, parentID, and name of all nodes for a given taxonomy
    */
   @SuppressWarnings("unchecked")
   public Collection<Object[]> getAllNodeNames(int taxonomyID, int langID) {
      String queryString = "select n.id, n.parent.id, v.Name, n.isNodeSelectable, "
            + "n.node_status.id from Node n join n.values v where v.attribute.Is_node_name > 0 "
            + "and n.taxonomy.id = " + taxonomyID + " and v.lang.id = " + langID
            + " order by v.node.id, v.attribute.Is_node_name";
      
      return concatNames(((Collection<Object[]>) this.getHibernateTemplate().execute(new HibernateQuery(queryString))));
   }

   /**
    * Return nodeID, parentID, and name of all nodes for a given taxonomy
    */
   @SuppressWarnings("unchecked")
   public Collection<Object[]> getSomeNodeNames(Collection<Integer> ids, int langID) {
      String queryString = "select n.id, n.parent.id, v.Name, n.Not_leaf, "
            + "n.node_status.id from Node n join n.values v where v.attribute.Is_node_name > 0 " + "and n.id in ("
            + StringUtils.join(ids.toArray(), ',') + ") and v.lang.id = " + langID
            + " order by v.node.id, v.attribute.Is_node_name";
      
      return concatNames(((Collection<Object[]>) this.getHibernateTemplate().execute(new HibernateQuery(queryString))));
   }

   /**
    * Return nodes for ides
    */
   @SuppressWarnings("unchecked")
   public Collection<Node> getSomeNodes(Collection<Integer> ids) {
      String queryString = "select n from Node n where n.id in(" + StringUtils.join(ids.toArray(), ',') + ")";
      
      return ((Collection<Node>) this.getHibernateTemplate().execute(new HibernateQuery(queryString)));
   }

   /**
    * Return all values associated with a given node
    */
   @SuppressWarnings("unchecked")
   public Collection<Value> getValuesForNode(int nodeID, int langID) {
      String queryString = "select v from Value v, Node n where v in elements(n.values) and n.id = " + nodeID
            + " and v.lang.id = " + langID;
      return (Collection<Value>) this.getHibernateTemplate().execute(new HibernateQuery(queryString));
   }

   /**
    * Return all values associated with a given node and attribute combo
    */
   @SuppressWarnings("unchecked")
   public Collection<Value> getSpecificValuesForNode(int nodeID, int attrID, int langID) {
      String queryString = "select v from Value v, Node n where v in elements(n.values) and n.id = " + nodeID
            + " and v.lang.id = " + langID + " and v.attribute.id = " + attrID;
      
      return (Collection<Value>) this.getHibernateTemplate().execute(new HibernateQuery(queryString));
   }

   /**
    * Return all nodes 'related to' the given node
    */
   @SuppressWarnings("unchecked")
   public Collection<Related_node> getRelatedNodes(int nodeID) {
      String queryString = "select r from Related_node r where r.node.id = " + nodeID + " and r.relationship.id = 1";
      return (Collection<Related_node>) this.getHibernateTemplate().execute(new HibernateQuery(queryString));
   }

   /**
    * Return all related nodes 'that reference' the given node
    */
   @SuppressWarnings("unchecked")
   public Collection<Related_node> getRelatedNodeReferences(int nodeID) {
      String queryString = "select r from Related_node r where r.related_node.id = " + nodeID
            + " and r.relationship.id = 1";
      return (Collection<Related_node>) this.getHibernateTemplate().execute(new HibernateQuery(queryString));
   }

   /**
    * Return all nodes 'similar to' the given node
    */
   @SuppressWarnings("unchecked")
   public Collection<Related_node> getSimilarNodes(int nodeID) {
      String queryString = "select r from Related_node r where r.node.id = " + nodeID + " and r.relationship.id = 2";
      
      return (Collection<Related_node>) this.getHibernateTemplate().execute(new HibernateQuery(queryString));
   }

   /**
    * Return all child nodes of the given node
    */
   @SuppressWarnings("unchecked")
   public Collection<Node> getChildNodes(int nodeID) {
      
      String queryString = "select n from Node n left join fetch n.nodeEditors ne where n.parent.id = " + nodeID;
      
      return (Collection<Node>) this.getHibernateTemplate().execute(new HibernateQuery(queryString));
   }

   /**
    * Return all NodeEditors for the given node
    */
   @SuppressWarnings("unchecked")
   public Collection<Node_editor> getNodeEditors(int nodeID) {
      
      String queryString = "select ne from Node_editor ne where ne.node.id = " + nodeID;
      
      return (Collection<Node_editor>) this.getHibernateTemplate().execute(new HibernateQuery(queryString));
   }

   /**
    * Return a nodeName for the given node
    */
   @SuppressWarnings("unchecked")
   public Collection<String> getNodeName(int nodeID, int langID) {
      
      String queryString = "select v.Name from Value v where v.node.id = " + nodeID
            + " and v.attribute.Is_node_name > 0 and v.lang.id = " + langID
            + " order by v.node.id, v.attribute.Is_node_name";
      
      return (Collection<String>) this.getHibernateTemplate().execute(new HibernateQuery(queryString));
   }

   public Map<String, String> deleteNodeAndFriends(int nodeID, int taxonomyID) {
      
      String queryString = "select n.id from Node n where n.parent.id = " + nodeID;
      
      Map<String, String> errors = null;
      List<?> result = (List<?>) this.getHibernateTemplate().execute(new HibernateQuery(queryString));
      
      if (result.size() == 0) {
         
         queryString = "delete from Related_node rn where rn.node.id =" + nodeID + " or rn.related_node.id = " + nodeID;
         getHibernateTemplate().execute(new HibernateDeleteQuery(queryString));
         queryString = "delete from Value v where v.node.id =" + nodeID;
         getHibernateTemplate().execute(new HibernateDeleteQuery(queryString));
         queryString = "delete from Node_editor e where e.node.id =" + nodeID;
         getHibernateTemplate().execute(new HibernateDeleteQuery(queryString));
         queryString = "delete from Node n where n.id =" + nodeID;
         getHibernateTemplate().execute(new HibernateDeleteQuery(queryString));
      
      } else {
         errors = new HashMap<String, String>();
         errors.put(AbstractTaxonEditorController.ACTION_ERROR, "Taxon ID=" + nodeID
               + " has children and cannot be deleted.");
      }
      return errors;
   }

   /**
    * Return all titles for all nodes
    */
   @SuppressWarnings("unchecked")
   public Collection<Object[]> getTitlesForNodes(int taxonomyID, int languageID) {
      
      String queryString = "select v.node.id, al.Name, v.Name from Node n, "
            + "Value v, Attribute a,  Attribute_lang al  where n.taxonomy.id = " + taxonomyID + " and "
            + "v.node.id = n.id and v.lang.id = " + languageID + " and v.attribute.id = a.id and "
            + "a.taxonomy.id = " + taxonomyID + " and al.attribute.id = v.attribute.id and al.language.id = " + languageID
            + " order by v.node.id";

      Object raw = this.getHibernateTemplate().execute(new HibernateQuery(queryString));
      
      return (Collection<Object[]>) raw;
   }

   /**
    * Change the parent of a node
    */
   public void changeParent(int nodeID, int newParentID) {
      
      String queryString = "select n from Node n where n.id = " + nodeID + " or n.id = " + newParentID;
      
      this.getHibernateTemplate().execute(new ParentUpdateQuery(queryString, nodeID, newParentID));
   }

   public void saveNode(Node node) {
      if (node != null) {
         this.getHibernateTemplate().saveOrUpdate(node);
      }
   }

   public void removeNode(Node node) {
      this.getHibernateTemplate().delete(node);
   }

   ///////////////////////////////////////////////////////////////////////////////////////////
   
   /**
    * Special case query used specifically to update the parent of a node
    */
   private class ParentUpdateQuery implements HibernateCallback {

      ///////////////////////////////////////////////////////////////////////////////////////////

      private String query;

      private int nodeID; 
      
      @SuppressWarnings("unused")
      private int newParentID;

      ///////////////////////////////////////////////////////////////////////////////////////////

      public ParentUpdateQuery(String query, int nodeID, int newParentID) {
         this.query = query;
         this.nodeID = nodeID;
         this.newParentID = newParentID;
      }

      ///////////////////////////////////////////////////////////////////////////////////////////

      @SuppressWarnings("unchecked")
      public Object doInHibernate(Session session) throws HibernateException {
         
         Collection<Node> nodes = (Collection<Node>) session.createQuery(query).list();
         
         Node[] tmp = nodes.toArray(new Node[nodes.size()]);
         
         if (tmp[0].getId() == nodeID) {
            
            tmp[0].setParent(tmp[1]);
            session.saveOrUpdate(tmp[0]);
         
         } else {
            tmp[1].setParent(tmp[0]);
            session.saveOrUpdate(tmp[1]);
         }
         return null;
      }
   }

   @SuppressWarnings("unchecked")
   public Collection<Node> findNodesByAttribute(Attribute attribute) {
      Session session = getSession();
      Collection<Node> nodes = null;
      
      try {
         Query query = session.getNamedQuery("findNodesByAttribute").setParameter("attribute", attribute);
         nodes = query.list(); 
      } finally {
         releaseSession(session);
      }
      return nodes;
   }
   
   public List<PSLocator> findItemsUsingNode(String table, String column, Node node, int maxItems, boolean remove)
   {
      Session session = getSession();
      
     
      List<PSLocator> locators = new ArrayList<PSLocator>();
      String testId = Integer.toString(node.getId());
      try {
         Query query = session.createSQLQuery("SELECT CONTENTID,REVISIONID,"+column+" from "+table+" where "+column+ " like '%"+testId+"%'");
         if (maxItems>0)
            query.setMaxResults(maxItems);
         
         ScrollableResults  rows = query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
         int updateCount = 0;
         while(rows.next()){
            Object[] row = rows.get();
            int id = (Integer.parseInt(row[0].toString()));
            int revision = (Integer.parseInt(row[1].toString()));
            String itemList = row[2].toString();
           
            String[] splitArray =  StringUtils.split(itemList, " ,");
            List<String> splitList = new ArrayList<>(Arrays.asList(splitArray));
            if (splitList.contains(testId))
            {
               PSLocator locator = new PSLocator(id,revision);
               PSLegacyGuid guid = new PSLegacyGuid(locator);
             
               locators.add(new PSLocator(id,revision));
               if (remove)
               {
                  IPSContentRepository rep = PSContentInternalLocator.getLegacyRepository();
                
                  
                  splitList.remove(testId);
                  String newString=StringUtils.join(splitList,',');
                  SQLQuery updateQuery = session.createSQLQuery("UPDATE "+table+" SET "+column+"= :newstring where CONTENTID = :content_id and REVISIONID = :revision");
                  updateQuery.setParameter("newstring", newString);
                  updateQuery.setParameter("content_id", id);
                  updateQuery.setParameter("revision", revision);
                  updateQuery.executeUpdate();
                  if (++updateCount % 20 == 0)
                  {
                     session.flush();
                     session.clear();
                  }
                  rep.evict(Collections.singletonList(guid));
               }
            }
        }
         
      } finally {
         releaseSession(session);
      }
      return locators;
      
   }
   
   

   ///////////////////////////////////////////////////////////////////////////////////////////

}
