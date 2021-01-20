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

package com.percussion.services.relationship.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipPropertyData;
import com.percussion.services.relationship.data.PSRelationshipData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

/**
 * A helper class used for building HQL from a given relationship filter.
 */
class PSHQLQueryHelper  implements IPSQueryHelper
{
   /**
    * Creates an instance
    *
    * @param filter the filter contains the query criteria.
    * @param configMap it maps config id to config object, 
    *   never <code>null</code> or empty.
    * @param nameMapToId it maps config name to config id, never 
    *   <code>null</code> or empty.
    */
   public PSHQLQueryHelper(PSRelationshipFilter filter,
         Map<Integer, PSRelationshipConfig> configMap,
         Map<String, Integer> nameMapToId) {

      m_filter = filter;
      m_configMap = configMap;
      m_nameMapToId = nameMapToId;

      // separate pre-defined user properties and custom properties
      if (m_filter.getProperties().size() > 0)
      {
         Collection<String> pduPropNames = PSRelationshipConfig
               .getPreDefinedUserPropertyNames();
         m_pduProps = new ArrayList<Map.Entry<String, String>>();
         m_customProps = new ArrayList<Map.Entry<String, String>>();

         // separate pre-defined and customer properties
         Set<Map.Entry<String, String>> props = m_filter.getProperties()
               .entrySet();
         for (Map.Entry<String, String> prop : props)
         {
            if (pduPropNames.contains(prop.getKey()))
               m_pduProps.add(prop);
            else
               m_customProps.add(prop);
         }
      }
   }

   /**
    * @return the filter object, never <code>null</code>.
    */
   public PSRelationshipFilter getFilter()
   {
      return m_filter;
   }

   /**
    * @return <code>true</code> if need to filter by owner revision as part
    *    of the post filtering process.
    */
   public boolean mayFilterOwnerRev()
   {
      boolean filterOwnerRev = true;

      if (m_filter.getLimitToOwnerRevision())
      {
         filterOwnerRev = false;
      }
      else if (m_filter.getOwner() == null
            || m_filter.getOwner().getRevision() == -1)
      {
         filterOwnerRev = false;
      }

      return filterOwnerRev;
   }

   /**
    * @return <code>true</code> if need to filter by dependent revision as
    *    part of the post filtering process.
    */
   public boolean mayFilterDependentRev()
   {
      boolean filterDependentRev = true;

      if (m_filter.getDependent() == null
            || m_filter.getDependent().getRevision() == -1
            || m_filter.getDependents().size() > 1)
      {
         filterDependentRev = false;
      }

      return filterDependentRev;
   }

   /**
    * Executes the query which is specified by the filter.
    * @param sess the session object which contains the data needed for 
    *   executing the query, may not be <code>null</code>.
    * @return the result set of the query, never <code>null</code>, but may be
    *   empty.
    */
   @SuppressWarnings("unchecked")
   public List<PSRelationshipData> executeQuery(Session sess)
   {
      appendOwnerJoinCriteria();
      appendDependentJoinCriteria();
      appendRestCriterias();

      return (List<PSRelationshipData>) getQuery(sess).list();
   }
   
   /**
    * Append the criteria that requires join the owner id (of the
    * relationship table) to the content id (of the contentstatus table).
    * Note, this must be called first for creating a HQL.
    */
   private void appendOwnerJoinCriteria()
   {
      if (m_filter.getLimitToEditOrCurrentOwnerRevision())
      {
         appendSelectJoinOwnerId(m_qryBuffer);
         m_qryBuffer
               .append(" ((c.m_editRevision > 0 and r.owner_revision = c.m_editRevision) or (r.owner_revision = c.m_currRevision))");

         m_isAddAND = true;
      }

      if (m_filter.getLimitToPublicOwnerRevision())
      {
         if (m_isAddAND)
            m_qryBuffer.append(AND);
         else
            appendSelectJoinOwnerId(m_qryBuffer);
         m_qryBuffer.append(" r.owner_revision = c.m_publicRevision");

         m_isAddAND = true;
      }

      if (m_filter.getOwnerContentTypeId() != -1)
      {
         if (m_isAddAND)
            m_qryBuffer.append(AND);
         else
            appendSelectJoinOwnerId(m_qryBuffer);

         m_qryBuffer.append("c.m_contentTypeId").append(EQ).append(
               FN_OWNER_CTYPEID);
         m_paramNames.add(FN_OWNER_CTYPEID);
         m_paramValues.add(new Long(m_filter.getOwnerContentTypeId()));
         m_isAddAND = true;
      }
      if (m_filter.getOwnerObjectType() != -1)
      {
         if (m_isAddAND)
            m_qryBuffer.append(AND);
         else
            appendSelectJoinOwnerId(m_qryBuffer);

         m_qryBuffer.append("c.m_objectType").append(EQ).append(
               FN_OWNER_OBJTYPE);
         m_paramNames.add(FN_OWNER_OBJTYPE);
         m_paramValues.add(new Integer(m_filter.getOwnerObjectType()));
         m_isAddAND = true;
      }
   }

   /**
    * Append the criteria that requires join the dependent id (of the
    * relationship table) to the content id (of the contentstatus table).
    * Note, this must be called after the {@link #appendOwnerJoinCriteria()}.
    */
   private void appendDependentJoinCriteria()
   {
      if (m_filter.getDependentContentTypeIds() != null
            && (!m_filter.getDependentContentTypeIds().isEmpty()))
      {
         if (m_isAddAND)
            throw new UnsupportedOperationException(
                  "Cannot join both owner and dependent ids (from "
                        + IPSConstants.PSX_RELATIONSHIPS
                        + " table) to CONTENTSTATUS table.");

         appendSelectJoinDependentId(m_qryBuffer);

         if (m_filter.getDependentContentTypeIds().size() == 1)
         {
            m_qryBuffer.append("c.m_contentTypeId").append(EQ).append(
                  FN_DEPENDENT_CTYPEID);
            m_paramNames.add(FN_DEPENDENT_CTYPEID);
            m_paramValues.add(new Long(m_filter.getDependentContentTypeId()));
         }
         else
         {
            m_qryBuffer.append("c.m_contentTypeId").append(" IN (:").append(
                  FN_DEPENDENT_CTYPEIDS).append(") ");
            m_paramNames.add(FN_DEPENDENT_CTYPEIDS);
            m_paramValues.add(m_filter.getDependentContentTypeIds());
         }

         m_isAddAND = true;
      }

      if (m_filter.getDependentObjectType() != -1)
      {
         if (m_isAddAND && m_filter.getDependentContentTypeId() != -1)
            throw new UnsupportedOperationException(
                  "Cannot join both owner and dependent ids (from "
                        + IPSConstants.PSX_RELATIONSHIPS
                        + " table) to CONTENTSTATUS table.");

         if (m_isAddAND)
            m_qryBuffer.append(AND);
         else
            appendSelectJoinDependentId(m_qryBuffer);

         m_qryBuffer.append("c.m_objectType").append(EQ).append(
               FN_DEPENDENT_OBJTYPE);
         m_paramNames.add(FN_DEPENDENT_OBJTYPE);
         m_paramValues.add(new Integer(m_filter.getDependentObjectType()));
         m_isAddAND = true;
      }

      // add the SELECT clause if has not done yet
      if (!m_isAddAND)
      {
         m_qryBuffer.append("select r from PSRelationshipData as r ");
         if (joinPropertiesTable())
         {
            m_qryBuffer.append(FROM_P);
            m_qryBuffer.append("where ");
            m_qryBuffer.append(JOIN_RID);
            m_isAddAND = true;
         }
         else
         {
            m_where = "where ";
         }
      }
   }

   /**
    * Append the criterias that can simply added to the WHERE clause
    * Note, this must be called after the
    * {@link #appendDependentJoinCriteria()}.
    */
   private void appendRestCriterias()
   {
      if (m_filter.getOwner() != null)
      {
         appendAndWhere();

         m_qryBuffer.append(R_TABLE).append(FN_OWNERID).append(EQ).append(
               FN_OWNERID);
         m_paramNames.add(FN_OWNERID);
         m_paramValues.add(new Integer(m_filter.getOwner().getId()));
         m_isAddAND = true;

         if (m_filter.getLimitToOwnerRevision())
         {
            appendAndWhere();

            m_qryBuffer.append(R_TABLE).append(FN_OWNER_REV).append(EQ).append(
                  FN_OWNER_REV);
            m_paramNames.add(FN_OWNER_REV);
            m_paramValues.add(new Integer(m_filter.getOwner().getRevision()));
            m_isAddAND = true;
         }
      }
      if (m_filter.getDependents() != null)
      {
         appendAndWhere();

         if (m_filter.getDependents().size() == 1)
         {
            m_qryBuffer.append(R_TABLE).append(FN_DEPENDENTID).append(EQ)
                  .append(FN_DEPENDENTID);
            m_paramNames.add(FN_DEPENDENTID);
            m_paramValues.add(new Integer(m_filter.getDependent().getId()));
         }
         else
         {
            m_qryBuffer.append(R_TABLE).append(FN_DEPENDENTID).append(" IN (:")
                  .append(FN_DEPENDENTIDS).append(") ");
            m_paramNames.add(FN_DEPENDENTIDS);
            List<Integer> ids = new ArrayList<Integer>();
            for (PSLocator loc : m_filter.getDependents())
               ids.add(loc.getId());
            m_paramValues.add(ids);
         }

         m_isAddAND = true;
      }
      if (m_filter.getRelationshipId() > 0)
      {
         appendAndWhere();

         m_qryBuffer.append(R_TABLE).append(FN_RID).append(EQ).append(FN_RID);
         m_paramNames.add(FN_RID);
         m_paramValues.add(new Integer(m_filter.getRelationshipId()));
         m_isAddAND = true;
      }
      Set<Integer> ids = getRelationshipNameIds(m_filter);
      if (!ids.isEmpty())
      {
         appendAndWhere();

         if (ids.size() == 1)
         {
            m_qryBuffer.append(R_TABLE).append(FN_CONFIGID).append(EQ).append(
                  FN_CONFIGID);
            m_paramNames.add(FN_CONFIGID);
            m_paramValues.add(ids.iterator().next());
         }
         else
         {
            m_qryBuffer.append(R_TABLE).append(FN_CONFIGID).append(" in (:")
                  .append(FN_CONFIGID_LIST).append(")");
            m_paramNames.add(FN_CONFIGID_LIST);
            m_paramValues.add(ids);
         }
         m_isAddAND = true;
      }

      appendPropertiesRestriction();
      appendCrossSiteLinksRestriction();
   }

   /**
    * Gets the relationship name ids from the supplied filter. It looks for the
    * relationship names from {@link PSRelationshipFilter#getNames()}, 
    * {@link PSRelationshipFilter#getCategory()} and 
    * {@link PSRelationshipFilter#getType()}.
    * 
    * @param filter the filter, assumed not <code>null</code>.
    * 
    * @return the ids. It may be empty if there is no names, category or type
    *    specified in the given filter.
    */
   @SuppressWarnings("unchecked")
   private Set<Integer> getRelationshipNameIds(PSRelationshipFilter filter)
   {
      if (filter.getNames().isEmpty() && filter.getCategory() == null
            && filter.getType() == null)
      {
         return (Set<Integer>) Collections.EMPTY_SET;
      }

      HashSet<Integer> ids = new HashSet<Integer>();
      Integer id;
      // get the names first
      for (String name : filter.getNames())
      {
         id = m_nameMapToId.get(name);
         if (id != null)
            ids.add(id);
         else
            ids.add(-1); // add an unknown  
      }

      // get the names from the category
      if (filter.getCategory() != null)
      {
         Collection<PSRelationshipConfig> configs = m_configMap.values();
         for (PSRelationshipConfig config : configs)
         {
            if (config.getCategory().equalsIgnoreCase(filter.getCategory()))
            {
               id = m_nameMapToId.get(config.getName());
               if (id != null)
                  ids.add(id);
            }
         }
      }

      // get names from the relationship type
      if (filter.getType() != null)
      {
         if (filter.getType().equals(PSRelationshipFilter.FILTER_TYPE_SYSTEM))
         {
            ids.addAll(getConfigIdsFromType(PSRelationshipFilter.FILTER_TYPE_SYSTEM));
         }
         else if (filter.getType()
               .equals(PSRelationshipFilter.FILTER_TYPE_USER))
         {
            ids.addAll(getConfigIdsFromType(PSRelationshipFilter.FILTER_TYPE_USER));
            // if there is such (user) type, then make up a fake id to purposely
            // force query return nothing.
            if (ids.isEmpty())
               ids.add(-1);
         }
      }

      return ids;
   }

   /**
    * Gets all configuration ids for a given relationship type.
    * 
    * @param type the relationship type, it is either 
    * {@link PSRelationshipFilter#FILTER_TYPE_SYSTEM} or
    * {@link PSRelationshipFilter#FILTER_TYPE_USER}
    *  
    * @return the config ids, may be empty, but never <code>null</code>.
    */
   private Set<Integer> getConfigIdsFromType(String type)
   {
      Set<Integer> ids = new HashSet<Integer>();

      Collection<PSRelationshipConfig> configs = m_configMap.values();
      for (PSRelationshipConfig config : configs)
      {
         if (config.getType().equals(type))
         {
            Integer id = m_nameMapToId.get(config.getName());
            if (id != null)
               ids.add(id);
         }
      }

      return ids;

   }

   /**
    * @param sess the session to create the query with, assumed not <code>null</code>
    * @return the created HQL object, never <code>null</code>.
    */
   private Query getQuery(Session sess)
   {
      String[] pnames = new String[m_paramNames.size()];
      m_paramNames.toArray(pnames);
      Object[] pvalues = new Object[m_paramValues.size()];
      m_paramValues.toArray(pvalues);

      Query qry = sess.createQuery(m_qryBuffer.toString());
      for (int i = 0; i < pnames.length; i++)
      {
         if (pvalues[i] instanceof Collection)
            qry.setParameterList(pnames[i], (Collection) pvalues[i]);
         else
            qry.setParameter(pnames[i], pvalues[i]);
      }

      return qry;
   }

   /**
    * Appends {@link #AND} or {@link #m_where} before appending further
    * restrictions.
    */
   private void appendAndWhere()
   {
      if (m_isAddAND)
         m_qryBuffer.append(AND);

      else if (m_where != null)
         m_qryBuffer.append(m_where);
   }

   /**
    * Appends SELECT clause, join owner id (with CONTENTSTATUS table) and
    * possible join rid (with PSX_RELTAIOTNSHIPPROPERTIES table).
    *
    * @param qryBuffer the buffer used to append the query string, assumed
    *    not <code>null</code>.
    */
   private void appendSelectJoinOwnerId(StringBuffer qryBuffer)
   {
      qryBuffer.append(SELECT_FROM_R_AND_C);
      if (joinPropertiesTable())
         qryBuffer.append(FROM_P);

      qryBuffer.append(JOIN_OWNERID);
      if (joinPropertiesTable())
         qryBuffer.append(JOIN_RID).append(AND);
   }

   /**
    * Appends SELECT clause, join dependent id (with CONTENTSTATUS table) and
    * possible join rid (with PSX_RELTAIOTNSHIPPROPERTIES table).
    *
    * @param qryBuffer the buffer used to append the query string, assumed
    *    not <code>null</code>.
    */
   private void appendSelectJoinDependentId(StringBuffer qryBuffer)
   {
      qryBuffer.append(SELECT_FROM_R_AND_C);
      if (joinPropertiesTable())
         qryBuffer.append(FROM_P);

      qryBuffer.append(JOIN_DEPENDENTID);
      if (joinPropertiesTable())
         qryBuffer.append(JOIN_RID).append(AND);
   }

   /**
    * Filtering the retrieved custom properties against the properties
    * specified in the relationship filter. The additional filtering is done
    * in case insensitive.
    *
    * @param props the to be filtered customer properties. It may be empty
    *    or <code>null</code>.
    *
    * @return <code>true</code> if the supplied customer properties matches
    *    the filter criteria; otherwise return <code>false</code>.
    */
   public boolean filterCustomProperties(
         Collection<PSRelationshipPropertyData> props)
   {
      if (m_customProps.size() <= 1) // no additional filtering needed.
         return true;

      if (props == null || props.size() < m_customProps.size())
         return false;

      PSRelationshipPropertyData prop;
      for (Map.Entry<String, String> entry : m_customProps)
      {
         prop = getCustomProperty(entry.getKey(), props);
         if (prop == null)
            return false;
         else if (!entry.getValue().equalsIgnoreCase(prop.getValue()))
            return false;
      }

      return true;
   }

   /**
    * Gets a property with the supplied name from the given property list.
    *
    * @param propName the to be retrieved property name, assumed not
    *    <code>null</code>
    * @param props a list of properties, assunmed not <code>null</code>.
    *
    * @return the property, may be <code>null</code> if cannot find one.
    */
   private PSRelationshipPropertyData getCustomProperty(String propName,
         Collection<PSRelationshipPropertyData> props)
   {
      for (PSRelationshipPropertyData prop : props)
      {
         if (propName.equalsIgnoreCase(prop.getName()))
            return prop;
      }
      return null;
   }

   /**
    * Determines whether to join the relationship properties table.
    *
    * @return <code>true</code> if join the properties table, there are
    *    customer properties specified in the filter; otherwise return
    *    <code>false</code>.
    */
   private boolean joinPropertiesTable()
   {
      return (!m_customProps.isEmpty());
   }

   /**
    * Append the criteria of limiting to cross site or folder relationships
    * if it is configured in the filter.
    */
   private void appendCrossSiteLinksRestriction()
   {
      if (!m_filter.getLimitToCrossSiteLinks())
         return;
      
      appendAndWhere();
      m_qryBuffer.append("((" + FN_FOLDER_ID + " is not null) or (" + FN_SITE_ID + " is not null))");
      m_isAddAND = true;      
   }
   
   /**
    * Append the property restriction (that is specified in the filter) to
    * the constructed HQL.
    *
    * @param qryBuffer the query string buffer, used to construct HQL,
    *    assumed not <code>null</code>.
    * @param paramNames the parameter names, used to construct HQL,
    *    assumed not <code>null</code>.
    * @param paramValues the parameter values, used to construct HQL,
    *    assumed not <code>null</code>.
    * @param isAddAND <code>true</code> if need to add {@link #AND} before
    *    append more restriction.
    * @param where the WHERE string, may be <code>null</code>. Need to
    *    add this string before appending more restriction if it is not
    *    <code>null</code> and isAddAND is <code>false</code>.
    *
    * @return <code>true</code> if need to append {@link #AND} before
    *    append more restrictions after this call.
    */
   private void appendPropertiesRestriction()
   {
      // handle pre-defined user properties

      if (!m_pduProps.isEmpty())
      {
         for (Map.Entry<String, String> prop : m_pduProps)
         {
            appendAndWhere();
            if (prop.getKey().equalsIgnoreCase(
                  PSRelationshipConfig.PDU_FOLDERID))
            {
               m_qryBuffer.append(R_TABLE).append(FN_FOLDER_ID).append(EQ)
                     .append(FN_FOLDER_ID);
               m_paramNames.add(FN_FOLDER_ID);
               m_paramValues.add(new Integer(prop.getValue()));
               m_isAddAND = true;
            }
            else if (prop.getKey().equalsIgnoreCase(
                  PSRelationshipConfig.PDU_INLINERELATIONSHIP))
            {
               m_qryBuffer.append(R_TABLE).append(FN_INLINELINK).append(EQ)
                     .append(FN_INLINELINK);
               m_paramNames.add(FN_INLINELINK);
               m_paramValues.add(prop.getValue());
               m_isAddAND = true;
            }
            else if (prop.getKey().equalsIgnoreCase(
                  PSRelationshipConfig.PDU_SITEID))
            {
               m_qryBuffer.append(R_TABLE).append(FN_SITE_ID).append(EQ)
                     .append(FN_SITE_ID);
               m_paramNames.add(FN_SITE_ID);
               m_paramValues.add(new Long(prop.getValue()));
               m_isAddAND = true;
            }
            else if (prop.getKey().equalsIgnoreCase(
                  PSRelationshipConfig.PDU_SLOTID))
            {
               m_qryBuffer.append(R_TABLE).append(FN_SLOT_ID).append(EQ)
                     .append(FN_SLOT_ID);
               m_paramNames.add(FN_SLOT_ID);
               m_paramValues.add(new Long(prop.getValue()));
               m_isAddAND = true;
            }
            else if (prop.getKey().equalsIgnoreCase(
                  PSRelationshipConfig.PDU_SORTRANK))
            {
               m_qryBuffer.append(R_TABLE).append(FN_SORT_RANK).append(EQ)
                     .append(FN_SORT_RANK);
               m_paramNames.add(FN_SORT_RANK);
               m_paramValues.add(new Integer(prop.getValue()));
               m_isAddAND = true;
            }
            else if (prop.getKey().equalsIgnoreCase(
                  PSRelationshipConfig.PDU_VARIANTID))
            {
               m_qryBuffer.append(R_TABLE).append(FN_VARIANT_ID).append(EQ)
                     .append(FN_VARIANT_ID);
               m_paramNames.add(FN_VARIANT_ID);
               m_paramValues.add(new Long(prop.getValue()));
               m_isAddAND = true;
            }
         }
      }

      // handle custom properties. It is tricky to construct a query
      // when there are more than one custom properties. We will just
      // use one (the 1st one) custom property here, then perform additional
      // filtering afterwards
      if (!m_customProps.isEmpty())
      {
         Map.Entry<String, String> prop = m_customProps.iterator().next();

         appendAndWhere();
         m_qryBuffer.append(P_TABLE).append(FN_PROPERTY_NAME).append(EQ)
               .append(FN_PROPERTY_NAME);
         ;
         m_paramNames.add(FN_PROPERTY_NAME);
         m_paramValues.add(prop.getKey());
         m_isAddAND = true;

         appendAndWhere();
         m_qryBuffer.append(P_TABLE).append(FN_PROPERTY_VALUE).append(EQ)
               .append(FN_PROPERTY_VALUE);
         ;;
         m_paramNames.add(FN_PROPERTY_VALUE);
         m_paramValues.add(prop.getValue());
      }
   }

   /**
    * The filter used to create HQL, set by constructor, never modified
    * after that.
    */
   final private PSRelationshipFilter m_filter;

   /**
    * A list of pre-defined user properties, which exist in the main
    * relationships table ({@link IPSConstants#PSX_RELATIONSHIPS}). Never
    * <code>null</code>, but may be empty.
    */
   @SuppressWarnings("unchecked")
   private Collection<Map.Entry<String, String>> m_pduProps = Collections.EMPTY_LIST;

   /**
    * A list of customer properties, which exist in the relationship
    * properties table ({@link IPSConstants#PSX_RELATIONSHIPPROPERTIES}).
    * Never <code>null</code>, but may be empty.
    */
   @SuppressWarnings("unchecked")
   private Collection<Map.Entry<String, String>> m_customProps = Collections.EMPTY_LIST;

   /**
    * The query string buffer, used to create HQL, never <code>null</code>,
    * may be empty.
    */
   private StringBuffer m_qryBuffer = new StringBuffer();

   /**
    * The parameter list, used to record the parameter names for the HQL,
    * never <code>null</code>, may be empty.
    */
   private List<String> m_paramNames = new ArrayList<String>();

   /**
    * The parameter values list, used to record the values of the above
    * parameter names. Never <code>null</code>, may be empty.
    */
   private List<Object> m_paramValues = new ArrayList<Object>();

   /**
    * Determines whether to append a {@link #AND} before add further
    * restrictions while constructing the HQL. Default to <code>false</code>.
    */
   private boolean m_isAddAND = false;

   /**
    * The WHERE string that need to be added or appended before add further
    * restrictions while constructing the HQL. Default to <code>null</code>.
    */
   private String m_where = null;

   /**
    * It maps the config id to its relationship configuration object. It is
    * initialized by {@link #loadConfigs()}.
    */
   private Map<Integer, PSRelationshipConfig> m_configMap = null;

   /**
    * It maps the config name to its config id. It is initialized by 
    * {@link #loadConfigs()}.
    */
   private Map<String, Integer> m_nameMapToId = null;

   /**
    * A list of field names for PSRelationshipData class.
    */
   private static final String FN_RID = "rid";

   private static final String FN_CONFIGID = "config_id";

   private static final String FN_OWNERID = "owner_id";

   private static final String FN_OWNER_REV = "owner_revision";

   private static final String FN_DEPENDENTID = "dependent_id";

   private static final String FN_DEPENDENTIDS = "dependent_ids";

   private static final String FN_CONFIGID_LIST = "config_id_list";

   private static final String FN_OWNER_CTYPEID = "owner_ctypeid";

   private static final String FN_OWNER_OBJTYPE = "owner_objtype";

   private static final String FN_DEPENDENT_CTYPEID = "dependent_ctypeid";

   private static final String FN_DEPENDENT_CTYPEIDS = "dependent_ctypeids";

   private static final String FN_DEPENDENT_OBJTYPE = "dependent_objtype";

   // a list of pre-defined user properties
   private static final String FN_FOLDER_ID = "folder_id";

   private static final String FN_SITE_ID = "site_id";

   private static final String FN_SLOT_ID = "slot_id";

   private static final String FN_SORT_RANK = "sort_rank";

   private static final String FN_VARIANT_ID = "variant_id";

   private static final String FN_INLINELINK = "inline_relationship";

   // a list of field names for PSRelationshipPersistentProperty
   private static final String FN_PROPERTY_NAME = "m_propertyName";

   private static final String FN_PROPERTY_VALUE = "m_propertyValue";

   /**
    * Literals used to construct HQL
    */
   private static final String SELECT_FROM_R_AND_C = "select r from PSRelationshipData as r, PSComponentSummary as c";

   private static final String FROM_P = ", PSRelationshipPropertyData as p ";

   private static final String JOIN_OWNERID = " where r.owner_id = c.m_contentId and ";

   private static final String JOIN_DEPENDENTID = " where r.dependent_id = c.m_contentId and ";

   private static final String JOIN_RID = " r.rid = p.m_rid ";

   private static final String R_TABLE = "r.";

   private static final String P_TABLE = "p.";

   private static final String EQ = " = :";

   private static final String AND = " and ";

}
