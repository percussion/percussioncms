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

package com.percussion.services.relationship.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipPropertyData;
import com.percussion.error.PSException;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.data.PSRelationshipConfigName;
import com.percussion.services.relationship.data.PSRelationshipData;
import com.percussion.util.PSSiteManageBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the Hibernate implementation of the <code>IPSRelationshipService</code>.
 *
 */
@Transactional
@PSSiteManageBean("sys_relationshipService")
public class PSRelationshipService
        implements
        IPSRelationshipService
{


   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   /* (non-Javadoc)
    * @see IPSRelationshipService#getRelationshipData(int)
    */
   public PSRelationship loadRelationship(int id) throws PSException
   {
      loadConfigs(); // load configs if needed

      PSRelationshipData rdata = (PSRelationshipData) sessionFactory.getCurrentSession()
              .get(PSRelationshipData.class, new Integer(id));
      if (rdata != null && setConfigAddChildProperties(rdata, null, false, false))
         return getRelationship(rdata);
      else
         return null;
   }

   /* (non-Javadoc)
    * @see IPSRelationshipService#findPersistedRid(Collection<Integer>)
    */
   @SuppressWarnings("unchecked")
   public List<Integer> findPersistedRid(Collection<Integer> testedIds)
   {
      if (testedIds == null)
         throw new IllegalArgumentException("testedIds may not be null.");

      // process all together if less than the max
      if (testedIds.size() < MAX_NUM_OF_IN_CLAUSE)
         return findPersistedRids(testedIds);

      // otherwise, process the IDs in groups
      List<Integer> returnIds = new ArrayList<>();
      List<Integer> groupIds = new ArrayList<>();
      for (Integer rid : testedIds)
      {
         groupIds.add(rid);
         if (groupIds.size() == MAX_NUM_OF_IN_CLAUSE)
         {
            returnIds.addAll(findPersistedRids(groupIds));
            groupIds.clear();
         }
      }
      // process whatever left
      if (groupIds.size() > 0)
         returnIds.addAll(findPersistedRids(groupIds));

      return returnIds;
   }

   /**
    * The same as {@link #findPersistedRid(Collection)}, except the number
    * of the IDs is assumed less or equals to {@link #MAX_NUM_OF_IN_CLAUSE}
    */
   @SuppressWarnings("unchecked")
   private List<Integer> findPersistedRids(Collection<Integer> testedIds)
   {
      if (testedIds.isEmpty())
         return Collections.EMPTY_LIST;
      else
         return (List<Integer>) sessionFactory.getCurrentSession().createQuery(
                 "select r.rid from PSRelationshipData as r where r.rid in (:rids)").setParameterList(
                 "rids",
                 testedIds).list();
   }

   // Implements IPSRelationshipService.findByFilter()
   @SuppressWarnings("unchecked")
   public List<PSRelationship> findByFilter(
           PSRelationshipFilter filter) throws PSException
   {
      List<PSRelationship> rels = null;
      loadConfigs(); // load configs if needed

      // execute the query and load the data
      Session sess = sessionFactory.getCurrentSession();

      IPSQueryHelper qry;

      List<PSRelationshipFilter> filters = getProcessedFilters(filter);
      for (PSRelationshipFilter f : filters)
      {
         // query with HQL, which may be slower than straight JDBC
         // qry = new PSHQLQueryHelper(filter, m_configMap,  m_nameMapToId);
         // query with straight JDBC, which should be faster than HQL
         qry = new PSHQLQueryHelper(f, m_configMap, m_nameMapToId);
         //qry = new PSJDBCQueryHelper(f, m_configMap, m_nameMapToId);

         List<PSRelationshipData> relsData = qry.executeQuery(sess);
         if (rels == null)
            rels = postProcessResultList(relsData, qry);
         else
            rels.addAll(postProcessResultList(relsData, qry));
      }


      return rels;
   }

   /**
    * Gets a list of ready to be processed filter from the specified filter.
    * A ready to be processed filter cannot contain more than
    * {@link #MAX_NUM_OF_IN_CLAUSE}.
    *
    * @param filter the source filter; assumed not <code>null</code>.
    *
    * @return a list of ready to be processed filters; never <code>null</code>
    *    or empty.
    */
   private List<PSRelationshipFilter> getProcessedFilters(
           PSRelationshipFilter filter)
   {
      List<PSLocator> dependents = filter.getDependents();
      if (dependents == null || dependents.size() <= MAX_NUM_OF_IN_CLAUSE)
      {
         return Collections.singletonList(filter);
      }

      List<PSRelationshipFilter> filters = new ArrayList<>();
      PSRelationshipFilter f;
      Set<PSLocator> deps = new HashSet<>();
      for(PSLocator loc : dependents)
      {
         deps.add(loc);
         if (deps.size() >= MAX_NUM_OF_IN_CLAUSE)
         {
            f = new PSRelationshipFilter(filter);
            f.setDependents(null); // force to reset the dependents
            f.setDependents(deps);
            filters.add(f);
            deps = new HashSet<>();
         }
      }
      if (!deps.isEmpty())
      {
         f = new PSRelationshipFilter(filter);
         f.setDependents(null); // force to reset the dependents
         f.setDependents(deps);
         filters.add(f);
      }
      return filters;
   }

   /**
    * The maximum number of element in a IN Clause. This number cannot be
    * bigger than 1000, which is the limit of the IN Clause for Oracle.
    */
   private static final int MAX_NUM_OF_IN_CLAUSE = 999;

   /**
    * Post process the supplied query result. It set the related relationship
    * config object for each entity and perform further filtering based on
    * the filter criteria.
    *
    * @param relsData the query result, assumed not <code>null</code>.
    * @param qryHelper the query helper object, used to perform additional
    *   filtering if needed.
    *
    * @return a list of relationship objects that have been processed
    *   successfully. It may be empty, but never <code>null</code>.
    */
   private List<PSRelationship> postProcessResultList(
           List<PSRelationshipData> relsData, IPSQueryHelper qryHelper)
   {
      List<PSRelationship> rels = new ArrayList<>(relsData
              .size());
      boolean filterOwnerRev = qryHelper.mayFilterOwnerRev();
      boolean filterDepedentRev = qryHelper.mayFilterDependentRev();
      for (PSRelationshipData rdata : relsData)
      {
         if (setConfigAddChildProperties(rdata, qryHelper, filterOwnerRev,
                 filterDepedentRev))
         {
            rels.add(getRelationship(rdata));
         }
      }

      return rels;
   }

   /**
    * Creates a relationship data from this object. This is used to save
    * the object in the persistent layer.
    *
    * @return the relationship data, never <code>null</code>.
    */
   private PSRelationshipData getRelationshipData(PSRelationship rel)
   {
      PSRelationshipData data = new PSRelationshipData(
              rel.getId(), rel.getConfig(), rel.getOwner().getId(),
              rel.getOwner().getRevision(),
              rel.getDependent().getId(), rel.getDependent().getRevision());

      data.setPersisted(rel.isPersisted());

      // set user properties
      for (PSRelationshipPropertyData prop : rel.getAllUserProperties())
      {
         setUserProperty(prop, data);
      }

      return data;
   }

   /**
    * Set the supplied user property, which may be either a custom or
    * pre-defined user property.
    *
    * @param prop the to be set user property, never <code>null</code>. It may
    *    be a new or existing user property.
    */
   private void setUserProperty(PSRelationshipPropertyData prop,
                                PSRelationshipData rdata)
   {
      if (prop == null)
         throw new IllegalArgumentException("prop must not be null.");

      PSRelationshipPropertyData myprop = rdata.getProperty(prop.getName());

      // if cannot find, then try one of the pre-defined user properties
      if (myprop == null)
      {
         try
         {
            // if cannot find, then try one of the pre-defined user properties
            if (prop.getName().equalsIgnoreCase(
                    PSRelationshipConfig.PDU_FOLDERID))
            {
               if (prop.getValue() != null)
                  rdata.setFolderId(Integer.parseInt(prop.getValue()));
               return;
            }

            if (prop.getName().equalsIgnoreCase(
                    PSRelationshipConfig.PDU_INLINERELATIONSHIP))
            {
               rdata.setInlineRelationship(prop.getValue());
               return;
            }

            if (prop.getName().equalsIgnoreCase(PSRelationshipConfig.PDU_WIDGET_NAME))
            {
               rdata.setWidgetName(prop.getValue());
               return;
            }

            if (prop.getName()
                    .equalsIgnoreCase(PSRelationshipConfig.PDU_SITEID))
            {
               if (prop.getValue() != null)
                  rdata.setSiteId(Long.parseLong(prop.getValue()));
               return;
            }

            if (prop.getName()
                    .equalsIgnoreCase(PSRelationshipConfig.PDU_SLOTID))
            {
               if (prop.getValue() != null)
                  rdata.setSlotId(Long.parseLong(prop.getValue()));
               return;
            }

            if (prop.getName().equalsIgnoreCase(
                    PSRelationshipConfig.PDU_SORTRANK))
            {
               if (prop.getValue() != null)
                  rdata.setSortRank(Integer.parseInt(prop.getValue()));
               return;
            }

            if (prop.getName().equalsIgnoreCase(
                    PSRelationshipConfig.PDU_VARIANTID))
            {
               if (prop.getValue() != null)
                  rdata.setVariantId(Long.parseLong(prop.getValue()));
               return;
            }
         }
         catch (NumberFormatException e)
         {
            // it must be one of the pre-defined user properties
            // with a BAD string value for integer.
            // Ignore the bad value, defaults to null
            return;
         }

         // must be a new property, just add
         rdata.addProperty(prop);
      }
      else
      {
         if (! myprop.equals(prop))
         {
            myprop.setPersisted(prop.isPersisted());
            myprop.setValue(prop.getValue());
         }
      }
   }


   /**
    * Set the relationship config for the supplied data, add (additional)
    * child properties, and performs additional filtering, such as filtering by
    * owner revision, dependent revision and child/custom properties if needed.
    *
    * @param rdata the relationship data, assumed not <code>null</code>.
    * @param qryHelper the query helper object, used to perform additional
    *   filtering if needed. It may be <code>null</code> if do not perform the
    *   additional filtering process.
    * @param filterOwnerRev <code>true</code> if need to consider to filter by
    *   owner revision.
    * @param filterDependentRev <code>true</code> if need to consider to filter by
    *   dependent revision.
    *
    * @return <code>true</code> if successful done the above;
    *   return <code>false</code> if cannot find a matching relationship config
    *   for the given relationship data or the child properties does not
    *   match the criteria of the filter.
    */
   private boolean setConfigAddChildProperties(
           PSRelationshipData rdata, IPSQueryHelper qryHelper,
           boolean filterOwnerRev, boolean filterDependentRev)
   {
      PSRelationshipConfig config;
      config = m_configMap.get(new Integer(rdata.getConfigId()));
      if (config == null)
      {
         ms_logger.warn("Cannot find relationship config for "
                 + rdata.toString());
         return false;
      }
      else
      {
         rdata.setConfig(config);
         // filter by owner revision
         if (filterOwnerRev)
         {
            if (config.useOwnerRevision()
                    && rdata.getOwnerRevision() != qryHelper.getFilter()
                    .getOwner().getRevision())
               return false;
         }

         // filter by dependent revision
         if (filterDependentRev)
         {
            if (config.useDependentRevision()
                    && rdata.getDependentRevision() != qryHelper.getFilter()
                    .getDependent().getRevision())
               return false;
         }

         // load the additional properties if needed
         if (!config.getCustomPropertyNames().isEmpty())
         {
            Collection<PSRelationshipPropertyData> relProps = null;
            relProps = findPropertiesByRid(rdata.getId());
            if (qryHelper == null)
               rdata.setProperties(relProps);
            else if (qryHelper.filterCustomProperties(relProps))
               rdata.setProperties(relProps);
            else
               return false; // custom properties does not match criteria
         }
      }
      return true;
   }

   /* (non-Javadoc)
    * @see IPSRelationshipService#saveRelationship(PSRelationship)
    */
   public void saveRelationship(PSRelationship rel) throws PSException
   {
      loadConfigs(); // load configs if needed

      if (rel == null)
         throw new IllegalArgumentException("rel may not be null");

      if (rel.getId() == -1)
      {
         int id;
         try{
            id = PSRelationshipCommandHandler.getNextId();
         }
         catch (PSCmsException e) {
            e.printStackTrace();
            throw new RuntimeException(e); // should never happen;
         }
         rel.setId(id);
         rel.setPersisted(false);
      }

      PSRelationshipData rdata = getRelationshipData(rel);
      Session sess = sessionFactory.getCurrentSession();

      // update config id if needed
      if (rdata.getConfigId() == -1)
      {
         Integer configId = m_nameMapToId.get(rdata.getConfig()
                 .getName());

         if (configId == null) // this is not possible
            throw new IllegalStateException(
                    "Unknown relationship configuration name: "
                            + rdata.getConfig().getName());
         rdata.setConfigId(configId.intValue());
      }

      // do save
      if (!rdata.isPersisted())
         sess.save(rdata);
      else
         rdata = (PSRelationshipData) sess.merge(rdata);

      saveOrUpdateRelationshipProperties(rdata, sess);

      rel.setPersisted(true);


   }

   /* (non-Javadoc)
    * @see IPSRelationshipService#saveRelationship(Collection<PSRelationship>)
    */
   public void saveRelationship(Collection<PSRelationship> rdatas) throws PSException
   {
      if (rdatas == null || rdatas.isEmpty())
         throw new IllegalArgumentException("rdatas may not be null or empty");

      // don't call getHibernateTemplate().saveOrUpdateAll(rdatas), but use
      // saveRelationshipData(PSRelationshipData) instead since it will not
      // do the actual save if it not dirty.
      for (PSRelationship rdata : rdatas)
      {
         saveRelationship(rdata);
      }
   }

   /* (non-Javadoc)
    * @see IPSRelationshipService#deleteRelationship(PSRelationship)
    */
   public void deleteRelationship(PSRelationship rdata)
   {
      if (rdata == null)
         throw new IllegalArgumentException("rdata may not be null");

      deleteRelationshipByRid(rdata.getId());
   }

   /* (non-Javadoc)
    * @see IPSRelationshipService#deleteRelationship(Collection<PSRelationship>)
    */
   public void deleteRelationship(Collection<PSRelationship> rdatas)
   {
      if (rdatas == null || rdatas.isEmpty())
         throw new IllegalArgumentException("rdatas may not be null or empty");

      for (PSRelationship rdata : rdatas)
         deleteRelationship(rdata);
   }


   /* (non-Javadoc)
    * @see IPSRelationshipService#deleteRelationshipById(int)
    */
   public int deleteRelationshipByRid(int rid)
   {
      //getHibernateTemplate().de.deleteAll(rdatas);
      Session sess = sessionFactory.getCurrentSession();
      int count;


      StringBuilder sqlBuffer = new StringBuilder();

      // delete from {@link IPSConstants#PSX_RELATIONSHIPPROPERTIES}
      sqlBuffer.append("delete from PSRelationshipPropertyData p where p.m_rid = :rid");
      Query sql = sess.createQuery(sqlBuffer.toString());
      sql.setParameter("rid", new Integer(rid));
      sql.executeUpdate();

      // delete from IPSConstants#PSX_RELATIONSHIPS
      sqlBuffer = new StringBuilder();
      sqlBuffer.append("delete from PSRelationshipData r where r.rid = :rid");
      sql = sess.createQuery(sqlBuffer.toString());
      sql.setParameter("rid", new Integer(rid));
      count = sql.executeUpdate();


      return count;
   }

   /**
    * Loads the relationship configurations if it has not been loaded; otherwise
    * do nothing.
    *
    * @throws PSException if failed to load the relationship configurations.
    */
   private void loadConfigs() throws PSException
   {
      if (m_configMap == null || m_nameMapToId == null)
      {
         reloadConfigs();
      }
   }

   /* (non-Javadoc)
    * @see IPSRelationshipService#reloadConfigs()
    */
   public void reloadConfigs() throws PSException
   {
      Map<Integer, PSRelationshipConfig> configMap = null;
      Map<String, Integer> nameMapToId = null;

      // load the configs from repository if has not done yet.
      PSRelationshipCommandHandler.loadConfigs();

      // initialize the m_configMap
      configMap = new HashMap<>();
      nameMapToId = new HashMap<>();

      IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
      Collection<PSRelationshipConfigName> configNames = objMgr
              .findAllRelationshipConfigNames();
      PSRelationshipConfig config;
      for (PSRelationshipConfigName cname : configNames)
      {
         config = PSRelationshipCommandHandler.getRelationshipConfig(cname
                 .getName());
         if (config != null)
         {
            configMap.put(new Integer(cname.getId()), config);
            nameMapToId.put(cname.getName(), new Integer(cname.getId()));
         }
         else
         {
            ms_logger.warn("Cannot find relationship configuration from "
                    + cname.toString());
         }
      }

      // expecting very infrequent usage of this call, so we don't
      // synchronize the access of these 2 variables.
      m_configMap = configMap;
      m_nameMapToId = nameMapToId;
   }

   @Override
   public List<PSRelationshipData> findByDependentId(int dependentId) {
      Session session = sessionFactory.getCurrentSession();

      Query query = session
              .createQuery("from PSRelationshipData " + " where dependent_id =  " + Integer.toString(dependentId));
      List<PSRelationshipData> results = query.list();
      return results;
   }

   /**
    * Gets a list of relationship properties for the supplied relationship id
    *
    * @param rid the relationship id.
    *
    * @return a list of relationship properties, never <code>null</code>, but
    * by empty.
    */
   @SuppressWarnings("unchecked")
   private Collection<PSRelationshipPropertyData> findPropertiesByRid(
           int rid)
   {
      // execute the query and load the data
      Collection<PSRelationshipPropertyData> rels = null;
      Session sess = sessionFactory.getCurrentSession();

      StringBuilder qryBuffer = new StringBuilder();
      qryBuffer.append("select r from PSRelationshipPropertyData r where ")
              .append("r.m_rid = :rid");

      Query qry = sess.createQuery(qryBuffer.toString());
      qry.setParameter("rid", new Integer(rid));
      rels = qry.list();


      return rels;
   }

   /**
    * Save or update the supplied properties.
    *
    * @param rdata the to be saved or updated properties, assmed not
    *    <code>null</code>, but may be empty.
    *
    * @param sess the session used to save or update to the repository. Assumed
    *    not <code>null</code> and it is closed/released by the called.
    */
   private void saveOrUpdateRelationshipProperties(
           PSRelationshipData rdata, Session sess)
   {
      if (rdata.getChildProperties().isEmpty())
         return; // do nothing

      for (PSRelationshipPropertyData prop : rdata.getChildProperties())
      {
         prop.setRid(rdata.getId()); // make sure set to correct parent id
         if (!prop.isPersisted()) // not exist in repository
         {
            sess.save(prop);
            prop.setPersisted(true);
         }
         else
         {
            sess.update(prop);
         }
      }
   }

   /**
    * Creates a {@link PSRelationship} object from a given relationship data
    * object.
    *
    * @param rdata the relationship data used to create the
    *   {@link PSRelationship} object. Assumed not <code>null</code>.
    *
    * @return the created {@link PSRelationship} object, never <code>null</code>.
    */
   private PSRelationship getRelationship(PSRelationshipData rdata)
   {
      PSRelationship rel = new PSRelationship(rdata.getId(),
              new PSLocator(rdata.getOwnerId(), rdata.getOwnerRevision()),
              new PSLocator(rdata.getDependentId(), rdata.getDependentRevision()),
              rdata.getConfig());
      rel.setPersisted(true);

      // set user properties
      Set<String> pnames = rdata.getConfig().getUserProperties().keySet();
      PSRelationshipPropertyData srcProp;
      for (String pname : pnames)
      {
         srcProp = getUserProperty(rdata, pname);
         if (srcProp != null)
            rel.setUserProperty(srcProp);
      }

      return rel;
   }

   /**
    * Get a specified user property from a relationship data object.
    *
    * @param r the relationship data, assumed not <code>null</code>.
    * @param name the name of the property, never <code>null</code>. This
    *   may be the name of a custom or pre-defined user property.
    *
    * @return the user property, it may be <code>null</code> if cannot find.
    */
   private PSRelationshipPropertyData getUserProperty(PSRelationshipData r,
                                                      String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name may not be null.");

      PSRelationshipPropertyData retProp = r.getProperty(name);
      if (retProp != null)
         return retProp;

      // try the pre-defined user properties
      if (name.equalsIgnoreCase(PSRelationshipConfig.PDU_FOLDERID))
         retProp = getIntProp(PSRelationshipConfig.PDU_FOLDERID, r
                 .getFolderId());

      if (name.equalsIgnoreCase(PSRelationshipConfig.PDU_INLINERELATIONSHIP))
         retProp = new PSRelationshipPropertyData(
                 PSRelationshipConfig.PDU_INLINERELATIONSHIP, r
                 .getInlineRelationship());

      if (name.equalsIgnoreCase(PSRelationshipConfig.PDU_WIDGET_NAME))
         retProp = new PSRelationshipPropertyData(PSRelationshipConfig.PDU_WIDGET_NAME, r.getWidgetName());

      if (name.equalsIgnoreCase(PSRelationshipConfig.PDU_SITEID))
         retProp = getLongProp(PSRelationshipConfig.PDU_SITEID, r.getSiteId());

      if (name.equalsIgnoreCase(PSRelationshipConfig.PDU_SLOTID))
         retProp = getLongProp(PSRelationshipConfig.PDU_SLOTID, r.getSlotId());

      if (name.equalsIgnoreCase(PSRelationshipConfig.PDU_SORTRANK))
         retProp = getIntProp(PSRelationshipConfig.PDU_SORTRANK, r
                 .getSortRank());

      if (name.equalsIgnoreCase(PSRelationshipConfig.PDU_VARIANTID))
         retProp = getLongProp(PSRelationshipConfig.PDU_VARIANTID, r
                 .getVariantId());

      if (retProp != null)
         retProp.setPersisted(r.isPersisted());

      return retProp;
   }

   /**
    * Creates a relationship property from a name and an integer value.
    *
    * @param name the name of the property, assumed not <code>null</code> or
    *    empty.
    * @param value the value of the property. It may be <code>-1</code> if
    *    the value of this property is unknown (or <code>null</code> in the
    *    repository).
    *
    * @return the created relationship property, never <code>null</code>.
    */
   private PSRelationshipPropertyData getIntProp(String name, int value)
   {
      PSRelationshipPropertyData prop;

      if (value == -1)
         prop = new PSRelationshipPropertyData(name, null);
      else
         prop = new PSRelationshipPropertyData(name, String.valueOf(value));

      return prop;
   }

   /**
    * Creates a relationship property from a name and a long value.
    *
    * @param name the name of the property, assumed not <code>null</code> or
    *           empty.
    * @param value the value of the property. It may be <code>-1</code> if the
    *           value of this property is unknown (or <code>null</code> in the
    *           repository).
    *
    * @return the created relationship property, never <code>null</code>.
    */
   private PSRelationshipPropertyData getLongProp(String name, long value)
   {
      PSRelationshipPropertyData prop;

      if (value == -1)
         prop = new PSRelationshipPropertyData(name, null);
      else
         prop = new PSRelationshipPropertyData(name, String.valueOf(value));

      return prop;
   }

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
    * The logger for this class.
    */
   private static final Logger ms_logger = LogManager.getLogger("RelationshipService");

   /*
   @SuppressWarnings("unchecked")
   public List<PSRelationshipData> findByCriteria(int ownderId)
   {
      Session sess = getSession();
      try
      {
         List rels = sess.createCriteria(PSRelationshipData.class).add(
               Restrictions.eq("owner_id", new Integer(ownderId))).list();
         return rels;
      }
      finally
      {
         releaseSession(sess);
      }
   }

   public List<PSRelationshipData> findByJDBC(int ownerId)
   {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      List<PSRelationshipData> dataList = new ArrayList<PSRelationshipData>();

      Session sess = getSession();
      try
      {
         String qryString = "select r.rid,r.config_id,r.owner_id,r.owner_revision,r.dependent_id,r.dependent_revision,r.slot_id,r.sort_rank,r.variant_id,r.folder_id,r.site_id,r.inline_relationship from rxrhino.dbo.PSX_OBJECTRELATIONSHIP as r where r.owner_id=?";

         conn = sess.connection();
         stmt = PSPreparedStatement.getPreparedStatement(conn, qryString);
         stmt.setInt(1, ownerId);
         rs = stmt.executeQuery();

         PSRelationshipData rdata;
         while (rs.next())
         {
            rdata = new PSRelationshipData();

            rdata.setId(rs.getInt(1));
            rdata.setConfigId(rs.getInt(2));
            rdata.setOwnerId(rs.getInt(3));
            rdata.setOwnerRevision(rs.getInt(4));
            rdata.setDependentId(rs.getInt(5));
            rdata.setDependentRevision(rs.getInt(6));
            rdata.setSlotId(rs.getLong(7));
            rdata.setSortRank(rs.getInt(8));
            rdata.setVariantId(rs.getLong(9));
            rdata.setFolderId(rs.getInt(10));
            rdata.setSiteId(rs.getLong(11));
            rdata.setInlineRelationship(rs.getString(12));

            dataList.add(rdata);
         }

         return dataList;
      }
      catch (SQLException e)
      {
         e.printStackTrace();
         throw new RuntimeException(e);
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
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
               // ignore, should not happen here.
               e.printStackTrace();
            }
         }
         releaseSession(sess);
      }
   }
   */
   @Override
   public void updateRelationshipData(
           PSRelationshipData rdata)
   {
      Session session = sessionFactory.getCurrentSession();
      session.update(rdata);
   }

   @Override
   public List<PSRelationshipData> findByDependentIdConfigId(int dependentId, int configId) {
      Session session = sessionFactory.getCurrentSession();

      Query query = session
              .createQuery("from PSRelationshipData " + " where dependent_id =  " + Integer.toString(dependentId) +" and config_id = " + Integer.toString(configId));
      List<PSRelationshipData> results = query.list();
      return results;
   }
}
