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

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.percussion.taxonomy.domain.Attribute;
import com.percussion.taxonomy.domain.Language;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Value;
import com.percussion.taxonomy.validation.UrlValidator;

/**
 * This DAO is used only to saving/updating the Node.java Domain Object
 * @author rxengineer
 *
 */
public class HibernateValueDAO extends HibernateDaoSupport implements ValueDAO {

   private static final Logger log = LogManager.getLogger(HibernateValueDAO.class);

   /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

   public static final int MAX_FIELD_LENGTH                  = 255;
   public static final int MAX_URL_FIELD_LENGTH              = 2048;
   
   public static final String NODE_ID_PARAM                  = "nodeID";
   public static final String NODE_ATTR_PARAM_PREFIX         = "attr_";
   public static final String NODE_SELECTABLE_PARAM          = "isNodeSelectable";
   
   ///////////////////////////////////////////////////////////////////////////////////////////////////////////

   public Value getValue(int id) {
      Value value = null;
      Session session = getSession();
      try {
         value = (Value) session.get(Value.class, new Integer(id));
      } finally{
         releaseSession(session);
      }
      return value;
   }

   @SuppressWarnings("unchecked")
   public Collection<Value> getAllValues() {
      Collection<Value> values = null;
      Session session = getSession();
      try {
         values = (Collection<Value>) session.createQuery("from Value val").list();
      } finally {
         releaseSession(session);
      }
      return values;
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

   public void saveValue(Value value) {
      this.getHibernateTemplate().saveOrUpdate(value);
   }

   @SuppressWarnings("unchecked")
   public Map<String, String> saveValuesFromParams(Map<String, String[]> params, 
                                                   Collection<Attribute> attributes,
                                                   Node node, 
                                                   int langID,
                                                   String user_name) {
      Map<String, String> values = null;
      try {
         HibernateValueCallback valueSetter = new HibernateValueCallback(params, attributes, 
               node, langID, user_name);
         
         values = (Map<String, String>) this.getHibernateTemplate().execute(valueSetter);
         
      } catch (Exception e) {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      return values;
   }
   
   /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

   public void removeValue(Value value) {
      this.getHibernateTemplate().delete(value);
   }   

   //////////////////////////////////////////////////////////////////////////////////////////////////////
   
   public static String getParamNameFor(Attribute attribute) {
      return NODE_ATTR_PARAM_PREFIX + String.valueOf(attribute.getId());
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

   // Private class to facilitate the complex value saving process
   private class HibernateValueCallback implements HibernateCallback {

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      
      private String username                                    = null;

      private boolean setNotLeaf                                 = false;
      
      private Map<String, String[]> params                       = null;

      private Collection<Attribute> attributes                   = null;
      
      private Node node                                          = null;

      // Default to english
      private int langID                                         = 1;

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

      public HibernateValueCallback(Map<String, String[]> params, 
                                    Collection<Attribute> attributes,
                                    Node node, 
                                    int langID, 
                                    String username) {
         this.node = node;
         this.setNotLeaf = (node != null ? node.getNot_leaf() : this.setNotLeaf);
         this.params = params;
         this.attributes = attributes;
         this.langID = langID;
         this.username = username;
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

      public HashMap<String, String> getErrors(Attribute attribute, 
                                               String param_value, 
                                               boolean newNode) {
         
         HashMap<String, String> ret = new HashMap<String, String>();

         if (param_value == null) {
            // we to not require for multi values
            if (!attribute.getIs_multiple() && attribute.getIs_required()) {
               ret.put((newNode ? "child" : "regular") + attribute.getId(), " Is required and cannot be blank."
                     + error_message_suffix(newNode));
            }
         } else {
            if (attribute.getIs_percussion_item() && !is_valid_url(param_value)) {
               ret.put((newNode ? "child" : "regular") + attribute.getId(), " The URL entered was not valid."
                     + error_message_suffix(newNode));
            } else if (attribute.getIs_percussion_item() && param_value.length() >= MAX_URL_FIELD_LENGTH) {
               ret.put((newNode ? "child" : "regular") + attribute.getId(), " Exceeded the " + MAX_URL_FIELD_LENGTH
                     + " character limit for URLs." + error_message_suffix(newNode));
            } else if ((!attribute.getIs_percussion_item()) && (param_value.length() >= MAX_FIELD_LENGTH)){ 
               // TODO this should be a config var
               ret.put((newNode ? "child" : "regular") + attribute.getId(), " Exceeded the " + MAX_FIELD_LENGTH
                     + " character limit." + error_message_suffix(newNode));
            }
         }
         return ret;
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

      public Object doInHibernate(Session session) throws HibernateException {
         
         Map<String, String> errors = null;
         Collection<Value> forGarbage = new HashSet<Value>();
         
         if (attributes != null) {
            NodeInfo nodeInfo = this.getNodeInfo(session, this.node, this.params);
            
            if (nodeInfo != null) {
               
               Node node = nodeInfo.getNode();
               boolean newNode = nodeInfo.isNew();
               
               errors = new HashMap<String, String>();
               
               for (Attribute attribute : attributes) {
                  errors.putAll(this.saveAttribute(session, nodeInfo, this.username, attribute, this.params, forGarbage));
               }
               
               if (!errors.isEmpty()) {
                 
                  if (newNode) {
                     for (Value v : forGarbage) {
                        session.delete(v);
                     }
                     session.delete(node);
                  }
                  
               } else {
                  // Update our Node if there were no errors
                  node.setNot_leaf(this.setNotLeaf);
                  
                  // I'm doing this here vs. creating a new Attribute and value bc this entire domain design is brittle 
                  // and horrible...the UI is directly tied to the autogenerated id's of the attribute domain objects...BAD
                  node.setIsNodeSelectable(this.isSelectable(this.params));
                  
                  session.saveOrUpdate(node);
               }
            }
         }
         return (errors == null || errors.isEmpty() ? null : errors);
      }
      
      //////////////////////////////////////////////////////////////////////////////////////////////////////
   
      private NodeInfo getNodeInfo(Session session, 
                                   Node node, 
                                   Map<String, String[]> params) {
         boolean isNew = false;
         NodeInfo nodeInfo = null;
         
         if (node != null) {
            
            int nodeId = node.getId();
            isNew = (nodeId <= 0);
            
            if (isNew) {
               // Update our Node before saving values linked to it
               session.saveOrUpdate(node);
            }
            nodeInfo = new NodeInfo(node, isNew);
         }
         return nodeInfo; 
      }
      
      //////////////////////////////////////////////////////////////////////////////////////////////////////
      
      private Map<String, String> saveAttribute(Session session, 
                                                NodeInfo nodeInfo, 
                                                String username, 
                                                Attribute attribute, 
                                                Map<String, String[]> params,
                                                Collection<Value> forGarbage) {
         Map<String, String> errors = new HashMap<String, String>();
         Collection<String> updatedValues = this.getUpdatedValuesFor(attribute, params);
         Map<String, Value> currentValues = this.getCurrentValuesFor(session, nodeInfo.getNode().getId(), attribute);
         
         if (attribute.getIs_multiple()) {
            
            // *********************************** MULTI VALUES *********************************** //
            errors.putAll(this.saveAttrMultiValue(session, nodeInfo.getNode(), nodeInfo.isNew(), username, 
                  attribute, updatedValues, currentValues, forGarbage));
            
         } else {
    
            // *********************************** SINGLE VALUES *********************************** //
            errors.putAll(this.saveAttrSingleValue(session, nodeInfo.getNode(), nodeInfo.isNew(), username, attribute, 
                  StringUtils.trimToNull((String) params.get(getParamNameFor(attribute))[0]), currentValues.values(), forGarbage));
         }
         return errors;
   }
   
   //////////////////////////////////////////////////////////////////////////////////////////////////////

   private Map<String, String> saveAttrMultiValue(Session session, 
                                                  Node node,
                                                  boolean newNode,
                                                  String userName,
                                                  Attribute attribute, 
                                                  Collection<String> newValues, 
                                                  Map<String, Value> currentValues,
                                                  Collection<Value> forGarbage) {
      Map<String, String> errors = new HashMap<String, String>();
      
      // process values
      for (Object obj : newValues.toArray()) {
         
         String s = (String) obj;
         
         if (currentValues.containsKey(s)) {
            // if value exists don't update DB but remove from
            // db_values because we will delete remaining at end of
            // loop
            currentValues.remove(s);
            
         } else if (!getErrors(attribute, s, newNode).isEmpty()) {
            
            errors.putAll(getErrors(attribute, s, newNode));
            
         } else {
            // passed validation create new value
            Value value = new Value();
            value.setAttribute(attribute);
            value.setNode(node);
            value.setLang((Language) session.createQuery("select l from Language l where l.id = " + langID).uniqueResult());
            value.setCreated_by_id(userName);
            value.setCreated_at(new Timestamp(System.currentTimeMillis()));
            value.setName(s);
            session.saveOrUpdate(value);
            
            forGarbage.add(value);
         }
      }
      // remove existing values not in params
      for (Value v : currentValues.values()) {
         session.delete(v);
      }
      return errors;
   }
   
   //////////////////////////////////////////////////////////////////////////////////////////////////////

   private Map<String, String> saveAttrSingleValue(Session session, 
                                                   Node node,
                                                   boolean newNode,
                                                   String userName,
                                                   Attribute attribute, 
                                                   String newValue, 
                                                   Collection<Value> currentValues,
                                                   Collection<Value> forGarbage) {
      Map<String, String> errors = new HashMap<String, String>();
      boolean new_value = false;

      // build our new or existing value object
      Value value = null;
      
      if (currentValues.size() == 1) {
         // find existing value...
         value = currentValues.iterator().next();
         
         // if we are modifing we need modified_by and modified_at
         
      } else if (currentValues.size() == 0) {
       
         // we won't create an object if the param value is null
         new_value = true;
         
         if (newValue != null) {
            value = new Value();
            value.setAttribute(attribute);
            value.setNode(node);
            value.setLang((Language) session.createQuery("select l from Language l where l.id = " + langID).uniqueResult());
            value.setCreated_by_id(userName);
            value.setCreated_at(new Timestamp(System.currentTimeMillis()));
         }
      } else {
         throw new HibernateException("multi values for single value node");
      }

      errors = getErrors(attribute, newValue, newNode);

      // so if there are no errors we can make the DB changes
      if (errors.isEmpty()) {
         if (newValue == null) {
            if (!new_value) {
               session.delete(value);
            }
         }
         else {
            value.setName(newValue);
            session.saveOrUpdate(value);
            forGarbage.add(value);
         }
      }
      return errors;
   }
   
   //////////////////////////////////////////////////////////////////////////////////////////////////////
   
   @SuppressWarnings("unchecked")
   private Map<String, Value> getCurrentValuesFor(Session session, int nodeID, Attribute attribute) {
      Collection<Value> rawValues = session.createQuery(
            "select v from Value v, Node n where v in elements(n.values) and n.id = " + nodeID
               + " and v.attribute.id = " + attribute.getId() + " and v.lang.id = " + langID).list();
   
      Map<String, Value> currentValues = new HashMap<String, Value>();
   
      for (Value v : rawValues) {
         // note we don't handle non unique values here
         currentValues.put(v.getName(), v);
      }
      return currentValues;
   }
   
   private Collection<String> getUpdatedValuesFor(Attribute attribute, Map<String, String[]> params) {
      Set<String> updatedValues = new HashSet<String>();
   
      String paramName = getParamNameFor(attribute);
      
      if (!StringUtils.isBlank(paramName) && params.containsKey(paramName)) {
         for (String s : params.get(paramName)) {
      
            String cleanedString = StringUtils.trimToNull(s);
      
            if (cleanedString != null) {
               updatedValues.add(cleanedString);
            }
         }
      }
      return updatedValues;
   }
   
   private boolean isSelectable(Map<String, String[]> params) {
      boolean isSelectable = true;
      
      if (params != null && params.containsKey(NODE_SELECTABLE_PARAM)) {
         String[] rawData = params.get(NODE_SELECTABLE_PARAM);
         
         if (rawData != null && rawData.length == 1) {
            isSelectable = Boolean.valueOf(rawData[0]);
         }
      }
      return isSelectable;
   }
   
   //////////////////////////////////////////////////////////////////////////////////////////////////////

 }
   //////////////////////////////////////////////////////////////////////////////////////////////////////

   private static class NodeInfo {
      
      private Node node = null;
      private boolean isNew = false;
      
      public NodeInfo(Node node, boolean isNew) {
         this.node = node;
         this.isNew = isNew;
      }
      
      public boolean isNew() {
         return this.isNew;
      }
      
      public Node getNode() {
         return this.node;
      }
   }
   
   //////////////////////////////////////////////////////////////////////////////////////////////////////
   
   private static String error_message_suffix(boolean newNode) {
      return (newNode ? StringUtils.EMPTY : "  The previous value for this field was restored.");
   }

   private static boolean is_valid_url(String url) {
      // add some extra url checking
      String[] schemes = {"http", "https"};
      UrlValidator urlValidator = new UrlValidator(schemes);
      return urlValidator.isValid(url);
   }
   
   //////////////////////////////////////////////////////////////////////////////////////////////////////

}
