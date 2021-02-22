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
package com.percussion.services.system.impl;

import static com.percussion.services.utils.orm.PSDataCollectionHelper.MAX_IDS;
import static com.percussion.services.utils.orm.PSDataCollectionHelper.clearIdSet;
import static com.percussion.services.utils.orm.PSDataCollectionHelper.createIdSet;
import static com.percussion.services.utils.orm.PSDataCollectionHelper.executeQuery;
import static com.percussion.webservices.PSWebserviceUtils.getUserCommunityId;
import static com.percussion.webservices.PSWebserviceUtils.getUserName;
import static com.percussion.webservices.PSWebserviceUtils.getUserRoles;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.content.IPSMimeContentTypes;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.security.PSRoleManager;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.jms.IPSQueueSender;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.notification.PSNotificationHelper;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.system.IPSSystemErrors;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSAssignmentTypeHelper;
import com.percussion.services.system.PSSystemException;
import com.percussion.services.system.data.PSConfigurationTypes;
import com.percussion.services.system.data.PSContentStatusHistory;
import com.percussion.services.system.data.PSDependency;
import com.percussion.services.system.data.PSMimeContentAdapter;
import com.percussion.services.system.data.PSSharedProperty;
import com.percussion.services.system.data.PSUIComponent;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.data.PSAdhocTypeEnum;
import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.services.workflow.data.PSWorkflowRole;
import com.percussion.util.IOTools;
import com.percussion.util.PSBaseBean;
import com.percussion.util.PSCharSetsConstants;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.jdbc.IPSDatasourceManager;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.workflow.mail.IPSMailMessageContext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements all services defined with the <code>IPSSystemService</code>
 * interface.
 */
@Transactional
@PSBaseBean("sys_systemService")
public class PSSystemService
   implements IPSSystemService
{

   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   public PSSystemService(IPSDatasourceManager dsMgr,  
         IPSGuidManager guidMgr, IPSWorkflowService wfService, IPSCmsObjectMgr cmsMgr)
   {
      m_dsMgr = dsMgr;
      m_guidMgr = guidMgr;
      m_wfService = wfService;
      m_cmsMgr = cmsMgr;
   }
   
   /* (non-Javadoc)
    * @see IPSSystemService#findSharedPropertiesByName(String)
    */
   @SuppressWarnings("unchecked")
   public List<PSSharedProperty> findSharedPropertiesByName(String name)
   {
      Session session = sessionFactory.getCurrentSession();

         // get all shared properties if no name is supplied
         if (StringUtils.isBlank(name))
            name = "%";

         Criteria criteria = session.createCriteria(PSSharedProperty.class);
         criteria.add(Restrictions.like("name", name));
         criteria.addOrder(Order.asc("name"));

         Set<PSSharedProperty> properties = new HashSet<>(
            criteria.list());

         return new ArrayList<>(properties);

   }

   /* (non-Javadoc)
    * @see IPSSystemService#loadHierarchyNode(IPSGuid)
    */
   @SuppressWarnings("unchecked")
   public PSSharedProperty loadSharedProperty(IPSGuid id)
      throws PSSystemException
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");

      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSSharedProperty.class);
         criteria.add(Restrictions.eq("id", id.longValue()));

         List<PSSharedProperty> properties =
            (List<PSSharedProperty>) criteria.list();
         if (properties.isEmpty())
            throw new PSSystemException(
               IPSSystemErrors.MISSING_SHARED_PROPERTY, id);

         return properties.get(0);

   }

   /* (non-Javadoc)
    * @see IPSSystemService#deleteSharedProperty(IPSGuid)
    */
   public void deleteSharedProperty(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");

      try
      {
         PSSharedProperty property = loadSharedProperty(id);
         sessionFactory.getCurrentSession().delete(property);
      }
      catch (PSSystemException e)
      {
         // ignore non existing node
      }
   }

   /* (non-Javadoc)
    * @see IPSSystemService#saveSharedProperty(PSSharedProperty)
    */
   public void saveSharedProperty(PSSharedProperty property)
   {
      if (property == null)
         throw new IllegalArgumentException("property cannot be null");

      Session session = sessionFactory.getCurrentSession();
      try
      {
         if (property.getVersion() == null)
         {
            // set the guid if this is the first time this property is saved
            if (property.getGUID().getUUID() == 0)
               property.setGUID(m_guidMgr.createGuid(
                  PSTypeEnum.SHARED_PROPERTY));

            session.persist(property);
         }
         else
            session.update(property);
      }
      finally
      {
         session.flush();
      }
   }

   public List<PSDependency> findDependencies(List<IPSGuid> ids)
   {
      if (ids == null)
         throw new IllegalArgumentException("ids may not be null");

      List<PSDependency> results = new ArrayList<>(ids.size());

      PSDependencyHelper depHelper = new PSDependencyHelper();
      depHelper.enableCache();

      try
      {
         for (IPSGuid id : ids)
         {
            PSDependency dep = new PSDependency();
            dep.setId(id.longValue());
            results.add(dep);

            try
            {
               dep.setDependents(depHelper.findDependents(id));
            }
            catch (Exception e)
            {
               ms_logger.error("Error finding dependencies for :" +
                  id.toString(), e);
               throw new RuntimeException(e);
            }
         }
      }
      finally
      {
         depHelper.disableCache();
      }


      return results;
   }

   public List<PSDependency> findCompositeDependencies(List<IPSGuid[]> ids)
   {
      if (ids == null)
         throw new IllegalArgumentException("ids may not be null");

      List<PSDependency> results = new ArrayList<>(ids.size());

      PSDependencyHelper depHelper = new PSDependencyHelper();
      depHelper.enableCache();

      try
      {
         for (IPSGuid[] compid : ids)
         {
            if (compid == null || compid.length < 2)
            {
               throw new IllegalArgumentException("Each array in the " +
                    "supplied ids must contain at least 2 elements");
            }

            IPSGuid id = compid[0];
            if (id == null)
            {
               throw new IllegalArgumentException("Each array in the " +
                    "supplied ids may not contain null elements");
            }

            PSDependency dep = new PSDependency();
            dep.setId(id.longValue());
            results.add(dep);

            try
            {
               dep.setDependents(depHelper.findDependents(compid));
            }
            catch (Exception e)
            {
               ms_logger.error("Error finding dependencies for :" +
                  id.toString(), e);
               throw new RuntimeException(e);
            }
         }
      }
      finally
      {
         depHelper.disableCache();
      }


      return results;
   }

   public PSMimeContentAdapter loadConfiguration(PSConfigurationTypes type)
      throws FileNotFoundException
   {
      if (type == null)
         throw new IllegalArgumentException("type may not be null");

      PSMimeContentDescriptor desc = getContentDescriptor(type);

      PSMimeContentAdapter content = new PSMimeContentAdapter();
      content.setGUID(desc.getGuid());
      content.setCharacterEncoding(desc.getCharacterEncoding());
      content.setMimeType(desc.getMimeType());
      content.setName(type.name());

      InputStream in;
      long length;
      File file = desc.getConfigFile();
      if (file.exists())
      {
         in = new FileInputStream(file);
         length = file.length();
      }
      else
      {
         in = new ByteArrayInputStream(new byte[0]);
         length = 0;
      }

      content.setContent(in);
      content.setContentLength(length);

      return content;
   }

   public void saveConfiguration(PSMimeContentAdapter config) throws IOException
   {
      if (config == null)
         throw new IllegalArgumentException("config may not be null");

      PSConfigurationTypes type = PSConfigurationTypes.valueOf(
         config.getName());
      PSMimeContentDescriptor desc = getContentDescriptor(type);
      InputStream in = config.getContent();
      if (in == null)
         throw new IllegalArgumentException("in may not be null");

      OutputStream out = null;
      boolean isSaved = false;
      File cFile = desc.getConfigFile();
      try
      {
         out = new FileOutputStream(cFile);
         IOTools.copyStream(in, out);
         isSaved = true;
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (IOException e)
            {
            }
         }

         if (out != null)
         {
            try
            {
               out.close();
            }
            catch (IOException e)
            {
            }
         }
      }

      if (isSaved)
      {
         // notify the objects that care about file changes
         PSNotificationHelper.notifyFile(cFile);
      }
   }

   public File getConfigurationFile(PSConfigurationTypes type)
   {
      if (type == null)
         throw new IllegalArgumentException("type may not be null");

      PSMimeContentDescriptor desc = getContentDescriptor(type);
      if (desc == null)
      {
         // a bug if the enum has an type not configured
         throw new IllegalArgumentException("Configuration Type not supported: "
            + type);
      }

      return desc.getConfigFile();
   }

   /* (non-Javadoc)
    * @see IPSSystemService#findContentStatusHistory(IPSGuid)
    */
   @SuppressWarnings("unchecked")
   public List<PSContentStatusHistory> findContentStatusHistory(IPSGuid id)
   {
      if (!(id instanceof PSLegacyGuid))
         throw new IllegalArgumentException(
            "id may not be null and must be an instance of PSLegacyGuid");

      PSLegacyGuid lguid = (PSLegacyGuid) id;

      Session session = sessionFactory.getCurrentSession();

         Criteria c = session.createCriteria(PSContentStatusHistory.class)
         .add(Restrictions.eq("contentId", new Integer(lguid.getContentId())))
         .addOrder(Order.asc("id"));

         List<PSContentStatusHistory> results = c.list();

         return results;


   }

   /**
    * Deletes the specified content history entry.
    * 
    * @param entity the to be deleted content history entry, not <code>null</code>
    */
   public void deleteContentStatusHistory(PSContentStatusHistory entity)
   {
      notNull(entity);
      sessionFactory.getCurrentSession().delete(entity);
   }
   
   /**
    * Save the specified content history entry. A new ID will be set if the ID 
    * is less than <code>0</code>.
    * 
    * @param entity the to be saved content history entry, not <code>null</code>.
    */
   public void saveContentStatusHistory(PSContentStatusHistory entity)
   {
      notNull(entity);
      
      if (entity.getId() >= 0L)
      {
         sessionFactory.getCurrentSession().update(entity);
         return;
      }

      IPSGuid id = m_guidMgr.createGuid(PSTypeEnum.ITEM_HISTORY);
      entity.setId(id.longValue());
      sessionFactory.getCurrentSession().save(entity);
   }
   
   /**
    * Gets the content IDs where there is no specified work-flow
    * activities before the specified date; but there are such
    * work-flow activities after the date. The work-flow activities
    * is represented by the state name of the work-flow. 
    *    
    * The expected parameters of this query are: begin-date, end-date and state-name.
    * 
    * Note, JDBC seems have trouble to set Date (or Timestamp) parameter, then use it
    *    to compare values in the database. It cannot pick up the values within the same day.
    *    The workaround is to "bake" the date value into the query.
    */
   private static String FIND_NEW_ACTIVITIES_InClause = "select distinct h1.contentId "
      + "from PSContentStatusHistory h1 "
      + "where h1.contentId in (:contentId) and "
      + "h1.contentId in "
         + "(select h2.contentId from PSContentStatusHistory h2 where "
         + "h2.contentId = h1.contentId and h2.stateName = {2} and h2.eventTime >= {0} and h2.eventTime < {1}) and "
      + "h1.contentId not in "
         + "(select h3.contentId from PSContentStatusHistory h3 where "
         + "h3.contentId = h1.contentId and h3.stateName = {2} and h3.eventTime < {0})";

   /**
    * This is the same as {@link #FIND_NEW_ACTIVITIES_InClause}, except the content IDs are
    * stored in the temporary table ID
    */
   private static String FIND_NEW_ACTIVITIES_TempIds = "select distinct h1.contentId "
      + "from PSContentStatusHistory h1, PSTempId t "
      + "where h1.contentId = t.pk.itemId and t.pk.id = :idset and "
      + "h1.contentId in "
         + "(select h2.contentId from PSContentStatusHistory h2 where "
         + "h2.contentId = h1.contentId and h2.stateName = {2} and h2.eventTime >= {0} and h2.eventTime < {1}) and "
      + "h1.contentId not in "
         + "(select h3.contentId from PSContentStatusHistory h3 where "
         + "h3.contentId = h1.contentId and h3.stateName = {2} and h3.eventTime < {0})";
      
   public int findNewContentActivities(Collection<Integer> cids,
         Date beginDate, Date endDate, String stateName)
   {
      notNull(stateName);
      notEmpty(stateName);
      notNull(cids);
      notNull(beginDate);
      notNull(endDate);

      Object[] params = new Object[]{getDateString(beginDate), 
            getDateString(endDate), "'" + stateName + "'"};
      List<Long> count = findContentActivities(cids,
            FIND_NEW_ACTIVITIES_InClause, FIND_NEW_ACTIVITIES_TempIds, params);
      return count.size();
   }

   /**
    * Gets the total number of content activities after the specified 
    * date for the specified content IDs. The content activities is
    * represented by the state-name of the work-flow.
    *    
    * The expected parameters of this query are: begin-date, end-date and state-name.
    *  
    * Note, JDBC seems have trouble to set Date (or Timestamp) parameter, then use it
    *    to compare values in the database. It cannot pick up the values within the same day.
    *    The workaround is to "bake" the date value into the query.
    */
   private static String FIND_NUM_ACTIVITIES_InClause = "select contentId "
         + "from PSContentStatusHistory "
         + "where contentId in (:contentId) and " 
         + "stateName = {2} and eventTime >= {0} and eventTime < {1}";

   // same as FIND_NUM_ACTIVITIES_InClause, but with "transitionLabel" specified
   private static String FIND_NUM_ACTIVITIES_InClause_2 = "select contentId "
      + "from PSContentStatusHistory "
      + "where contentId in (:contentId) and " 
      + "stateName = {2} and transitionLabel = {3} and eventTime >= {0} and eventTime < {1}";

   /**
    * This is the same as {@link #FIND_NUM_ACTIVITIES_InClause}, except the content IDs
    * are stored in the temporary ID table.
    */
   private static String FIND_NUM_ACTIVITIES_TempIds = "select h.contentId "
      + "from PSContentStatusHistory h, PSTempId t "
      + "where h.contentId = t.pk.itemId and t.pk.id = :idset and " 
      + "h.stateName = {2} and h.eventTime >= {0} and h.eventTime < {1}";
   // same as FIND_NUM_ACTIVITIES_TempIds, but with "transitionLabel" specified
   private static String FIND_NUM_ACTIVITIES_TempIds_2 = "select h.contentId "
      + "from PSContentStatusHistory h, PSTempId t "
      + "where h.contentId = t.pk.itemId and t.pk.id = :idset and " 
      + "h.stateName = {2} and h.transitionLabel = {3} and h.eventTime >= {0} and h.eventTime < {1}";

   public int findNumberContentActivities(Collection<Integer> cids,
         Date beginDate, Date endDate, String stateName, String transitionName)
   {
      List<Long> count;
      count = findPageIdsContentActivities(cids, beginDate, endDate, stateName, transitionName);
      return count.size();
   }

   public List<Long> findPageIdsContentActivities(Collection<Integer> cids,
         Date beginDate, Date endDate, String stateName, String transitionName)
   {
      notNull(stateName);
      notEmpty(stateName);
      notNull(cids);
      notNull(beginDate);
      notNull(endDate);

      Object[] params;
      
      List<Long> pageIds;
      if (transitionName == null)
      {
         params = new Object[]{getDateString(beginDate), getDateString(endDate),
               "'" + stateName + "'"};
         pageIds = findContentActivities(cids,
               FIND_NUM_ACTIVITIES_InClause, FIND_NUM_ACTIVITIES_TempIds, params);
      }
      else
      {
         params = new Object[]{getDateString(beginDate), getDateString(endDate),
               "'" + stateName + "'", "'" + transitionName + "'"};
         pageIds = findContentActivities(cids,
               FIND_NUM_ACTIVITIES_InClause_2, FIND_NUM_ACTIVITIES_TempIds_2, params);
      }
      return pageIds;
   }
   
   /**
    * Gets the published content IDs from the specified IDs and within the specified date range.
    * The published items must have transitioned to the specified "publish state", 
    * but have not transitioned to an "archive state" between the date of last "publish state" 
    * and the end of the specified date range.
    * <p>
    * Expected parameters are: begin-date, end-date, publish-state and archive-state.
    * <p>
    * Note, this query is used when the number of content IDs is less than {@link #MAX_IDS}.   
    */
   private static String FIND_NUM_PUB_ITEMS_InClause = "SELECT DISTINCT h1.contentId "
      + "FROM PSContentStatusHistory h1 "
      + "WHERE h1.contentId IN (:contentId) AND "
      + "h1.stateName = {2} AND h1.eventTime < {1} AND "
      + "h1.contentId NOT IN (SELECT h2.contentId "
         + "FROM PSContentStatusHistory h2 "
         + "WHERE h2.contentId = h1.contentId AND "
         + "h2.stateName = {3} AND h2.eventTime > (SELECT MAX(h3.eventTime) "
            + "FROM PSContentStatusHistory h3 "
            + "WHERE h3.contentId = h2.contentId AND h3.stateName = {2} AND h3.eventTime < {1} "
            + "GROUP BY h3.contentId))";

   /**
    * This is the same as {@link #FIND_NUM_PUB_ITEMS_InClause}, except this query is used
    * when the number of content IDs is greater than {@link #MAX_IDS}.
    */
   private static String FIND_NUM_PUB_ITEMS_TempIds = "SELECT DISTINCT h1.contentId "
      + "FROM PSContentStatusHistory h1, PSTempId t "
      + "WHERE h1.contentId = t.pk.itemId AND t.pk.id = :idset AND "
      + "h1.stateName = {2} AND h1.eventTime < {1} AND "
      + "h1.contentId NOT IN (SELECT h2.contentId "
         + "FROM PSContentStatusHistory h2 "
         + "WHERE h2.contentId = h1.contentId AND "
         + "h2.stateName = {3} AND h2.eventTime > (SELECT MAX(h3.eventTime) "
            + "FROM PSContentStatusHistory h3 "
            + "WHERE h3.contentId = h2.contentId AND h3.stateName = {2} AND h3.eventTime < {1} "
            + "GROUP BY h3.contentId))";

   public int findPublishedItems(Collection<Integer> cids, Date beginDate,
         Date endDate, String pubStateName, String archiveStateName)
   {
      notNull(pubStateName);
      notEmpty(pubStateName);
      notNull(archiveStateName);
      notEmpty(archiveStateName);
      notNull(cids);
      notNull(beginDate);
      notNull(endDate);

      Object[] params = new Object[]{getDateString(beginDate), 
            getDateString(endDate), "'" + pubStateName + "'", "'" + archiveStateName + "'"};
      List<Long> count = findContentActivities(cids,
            FIND_NUM_PUB_ITEMS_InClause, FIND_NUM_PUB_ITEMS_TempIds, params);
      
      return count.size();
   }
   
   /**
    * Gets the published content IDs from the specified IDs.
    * The published items must have transitioned to the specified "publish state."
    * <p>
    * Expected parameters are: publish-state and archive-state.
    * <p>
    * Note, this query is used when the number of content IDs is less than {@link #MAX_IDS}.   
    */
   private static String FIND_PUB_ITEMS_InClause = "SELECT DISTINCT h1.contentId "
      + "FROM PSContentStatusHistory h1 "
      + "WHERE h1.contentId IN (:contentId) AND "
      + "h1.stateName = {0} AND "
      + "h1.contentId NOT IN "
      + "(SELECT h2.contentId "
         + "FROM PSContentStatusHistory h2 "
         + "WHERE h2.contentId = h1.contentId AND "
         + "h2.stateName = {1} AND "
         + "h2.eventTime > "
         + "(SELECT MAX(h3.eventTime) "
            + "FROM PSContentStatusHistory h3 "
            + "WHERE h3.contentId = h2.contentId AND "
            + "h3.stateName = {0} GROUP BY h3.contentId))";

   /**
    * This is the same as {@link #FIND_PUB_ITEMS_InClause}, except this query is used when the number of content IDs is
    * greater than {@link #MAX_IDS}.
    */
   private static String FIND_PUB_ITEMS_TempIds = "SELECT DISTINCT h1.contentId "
      + "FROM PSContentStatusHistory h1, PSTempId t "
      + "WHERE h1.contentId = t.pk.itemId AND t.pk.id = :idset AND "
      + "h1.stateName = {0} AND "
      + "h1.contentId NOT IN "
      + "(SELECT h2.contentId "
         + "FROM PSContentStatusHistory h2 "
         + "WHERE h2.contentId = h1.contentId AND "
         + "h2.stateName = {1} AND "
         + "h2.eventTime > "
         + "(SELECT MAX(h3.eventTime) "
            + "FROM PSContentStatusHistory h3 "
            + "WHERE h3.contentId = h2.contentId AND "
            + "h3.stateName = {0} GROUP BY h3.contentId))";
   
   public Collection<Long> findPublishedItems(Collection<Integer> cids, String pubStateName, String archiveStateName)
   {
      notNull(pubStateName);
      notEmpty(pubStateName);
      notNull(archiveStateName);
      notEmpty(archiveStateName);
      notNull(cids);
      
      Object[] params = new Object[]{"'" + pubStateName + "'", "'" + archiveStateName + "'"};
      return findContentActivities(cids, FIND_PUB_ITEMS_InClause, FIND_PUB_ITEMS_TempIds, params);
   }

   /**
    * Utility method used by both
    * {@link #findNewContentActivities(String, Collection, Date, Date)} and
    * {@link #findNumberContentActivities(String, Collection, Date, Date)}.
    * Executes the specified query based on the specified state, items, begin
    * and end dates.
    * 
    * @param cids the IDs of the items in question, not <code>null</code>.
    * @param queryInClause the query used if the number of items are small,
    *           assumed not blank.
    * @param queryTempIds the query used for large number of items, assumed not
    *           blank.
    * @param params the parameters used to format the actual query from above
    *           source queries, not empty.
    * 
    * @return the result set, caller is responsible to interpret the content of
    *         it, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   private List<Long> findContentActivities(Collection<Integer> cids,
         String queryInClause, String queryTempIds, Object[] params)
   {
      notNull(cids);
      notNull(params);

      Session s = sessionFactory.getCurrentSession();
      long idset = 0;
      
      //String querySource = queryTempIds;
      String querySource = cids.size() < MAX_IDS ? queryInClause : queryTempIds;
      String queryString = MessageFormat.format(querySource, params);

      try
      {
         Query q = s.createQuery(queryString);
         if (cids.size() < MAX_IDS)
         {
            q.setParameterList("contentId", cids);
         }
         else
         {
            idset = createIdSet(s, cids);
            q.setParameter("idset", idset);
         }

         List<Integer> countList = (idset != 0) ? (List)executeQuery(q) : q.list();
         
         List<Long> convertList = new ArrayList<>();
         for(int val:countList)
         {
            convertList.add(new Long(val));
         }
         
         return convertList;
      }
      finally
      {
         if (idset != 0)
         {
            clearIdSet(s, idset);
         }

      }
   }

   public PSContentStatusHistory findLastCheckInOut(IPSGuid id)
   {
      notNull(id, "id cannot be null.");
      if (!(id instanceof PSLegacyGuid))
      {
         throw new IllegalArgumentException(
               "id may not be null and must be an instance of PSLegacyGuid");
      }

      PSLegacyGuid lguid = (PSLegacyGuid) id;

      Session session = sessionFactory.getCurrentSession();

         // Note, transitionLabel column is always "CheckOut" for both
         //       check in & out actions. However, the checkout user is blank
         //       if checkin; otherwise it is checkout action.
         
         Criteria c = session.createCriteria(PSContentStatusHistory.class).add(
               Restrictions.eq("contentId", new Integer(lguid.getContentId())))
               .add(Restrictions.eq("transitionLabel", "CheckOut")).addOrder(
                     Order.desc("id"));
         c.setMaxResults(1);
         List<PSContentStatusHistory> results = c.list();

         return results.isEmpty() ? null : results.get(0);

   }
   
   /**
    * Get the descriptor for the specified type
    *
    * @param type The type, assumed not <code>null</code>.
    *
    * @return The descriptor, never <code>null</code>.
    */
   private PSMimeContentDescriptor getContentDescriptor(
      PSConfigurationTypes type)
   {
      initContentDescriptors();
      PSMimeContentDescriptor desc = m_mimeContentMap.get(type);
      if (desc == null)
      {
         // a bug if the enum has an type not configured
         throw new RuntimeException("Configuration Type not supported: " +
            type);
      }
      return desc;
   }

   /**
    * Initializes the contents of {@link #m_mimeContentMap}
    */
   private void initContentDescriptors()
   {
      if (m_mimeContentMap != null)
         return;

      m_mimeContentMap = new HashMap<>();

      m_mimeContentMap.put(PSConfigurationTypes.SERVER_PAGE_TAGS,
         new PSMimeContentDescriptor(new PSGuid(PSTypeEnum.CONFIGURATION,
            PSConfigurationTypes.SERVER_PAGE_TAGS.getId()),
            PSConfigurationTypes.SERVER_PAGE_TAGS.getFileName(), new File(
               PSServer.getBaseConfigDir(), "XSpLit"), //$NON-NLS-1$
            IPSMimeContentTypes.MIME_TYPE_TEXT_XML, null));

      m_mimeContentMap.put(PSConfigurationTypes.TIDY_CONFIG,
         new PSMimeContentDescriptor(new PSGuid(PSTypeEnum.CONFIGURATION,
            PSConfigurationTypes.TIDY_CONFIG.getId()),
            PSConfigurationTypes.TIDY_CONFIG.getFileName(), new File(PSServer
               .getBaseConfigDir(), "XSpLit"))); //$NON-NLS-1$

      m_mimeContentMap.put(PSConfigurationTypes.LOG_CONFIG,
         new PSMimeContentDescriptor(new PSGuid(PSTypeEnum.CONFIGURATION,
            PSConfigurationTypes.LOG_CONFIG.getId()),
            PSConfigurationTypes.LOG_CONFIG.getFileName(), PathUtils.getRxPath().resolve(Paths.get("jetty","base","resources")).toAbsolutePath().toFile(), IPSMimeContentTypes.MIME_TYPE_TEXT_XML, null));

      m_mimeContentMap.put(PSConfigurationTypes.NAV_CONFIG,
         new PSMimeContentDescriptor(new PSGuid(PSTypeEnum.CONFIGURATION,
            PSConfigurationTypes.NAV_CONFIG.getId()),
            PSConfigurationTypes.NAV_CONFIG.getFileName(), new File(PSServer
               .getRxConfigDir())));

      m_mimeContentMap.put(PSConfigurationTypes.WF_CONFIG,
         new PSMimeContentDescriptor(new PSGuid(PSTypeEnum.CONFIGURATION,
            PSConfigurationTypes.WF_CONFIG.getId()),
            PSConfigurationTypes.WF_CONFIG.getFileName(), new File(PSServer
               .getBaseConfigDir(), "Workflow"))); //$NON-NLS-1$

      m_mimeContentMap.put(PSConfigurationTypes.THUMBNAIL_CONFIG,
         new PSMimeContentDescriptor(new PSGuid(PSTypeEnum.CONFIGURATION,
            PSConfigurationTypes.THUMBNAIL_CONFIG.getId()),
            PSConfigurationTypes.THUMBNAIL_CONFIG.getFileName(), new File(
               PSServer.getRxConfigDir())));

      m_mimeContentMap.put(PSConfigurationTypes.AUTH_TYPES,
         new PSMimeContentDescriptor(new PSGuid(PSTypeEnum.CONFIGURATION,
            PSConfigurationTypes.AUTH_TYPES.getId()),
            PSConfigurationTypes.AUTH_TYPES.getFileName(), new File(PSServer
               .getRxConfigDir())));

      m_mimeContentMap.put(PSConfigurationTypes.SYSTEM_VELOCITY_MACROS,
         new PSMimeContentDescriptor(new PSGuid(PSTypeEnum.CONFIGURATION,
            PSConfigurationTypes.SYSTEM_VELOCITY_MACROS.getId()),
            PSConfigurationTypes.SYSTEM_VELOCITY_MACROS.getFileName(),
            new File(PSServer.getRxFile("sys_resources/vm")))); //$NON-NLS-1$

      m_mimeContentMap.put(PSConfigurationTypes.USER_VELOCITY_MACROS,
         new PSMimeContentDescriptor(new PSGuid(PSTypeEnum.CONFIGURATION,
            PSConfigurationTypes.USER_VELOCITY_MACROS.getId()),
            PSConfigurationTypes.USER_VELOCITY_MACROS.getFileName(), new File(
               PSServer.getRxFile("rx_resources/vm")))); //$NON-NLS-1$
   }

   /**
    * Load a ui component by name
    *
    * @param name the ui component's name, never <code>null</code> or empty,
    *           may not contain wildcards
    * @return the ui component or <code>null</code> if the ui component is not
    *         found, if multiples are found, the first is returned
    */
   @SuppressWarnings("unchecked")
   public PSUIComponent findComponentByName(String name)
   {
      Session s = sessionFactory.getCurrentSession();

         Criteria c = s.createCriteria(PSUIComponent.class);
         c.add(Restrictions.eq("name", name));
         List<PSUIComponent> results = c.list();
         if (results.size() == 0)
            return null;
         return results.get(0);

   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.system.IPSSystemService#sendEmail(com.percussion.workflow.mail.IPSMailMessageContext)
    */
   public void sendEmail(IPSMailMessageContext emailContext)
   {
      if (emailContext == null)
      {
         throw new IllegalArgumentException("emailContext may not be null");
      }
      if (m_emailSender == null)
      {
         throw new IllegalStateException("The m_emailSender is not configured");
      }
      m_emailSender.sendMessage(emailContext);
   }

   /**
    * Get the email queue sender, as a required getter for Spring. 
    * 
    * @return the email sender, it may not be <code>null</code> in a correct
    *    configured environment.
    */
   public IPSQueueSender getEmailSender()
   {
      return m_emailSender;
   }

   /**
    * Set the email queue sender, only here for spring wiring
    * @param emailSender the email queue sender, never <code>null</code>
    */
   @Autowired
   public void setEmailSender(IPSQueueSender emailSender)
   {
      if (emailSender == null)
      {
         throw new IllegalArgumentException("emailSender may not be null");
      }
      m_emailSender = emailSender;
   }
   
   public List<PSAssignmentTypeEnum> getContentAssignmentTypes(
         List<IPSGuid> ids, String user, 
         List<String> roles, int community)
         throws PSSystemException
   {
      if (user == null || StringUtils.isBlank(user))
      {
         throw new IllegalArgumentException("user may not be null or empty");
      }
      if (roles == null)
      {
         throw new IllegalArgumentException("roles may not be null");
      }
      PSAssignmentTypeHelper helper = new PSAssignmentTypeHelper( 
            user, roles, community);
      List<PSAssignmentTypeEnum> rval = new ArrayList<>();
      for (int i = 0; i < ids.size(); i++)
      {
         rval.add(helper.getAssignmentType(ids.get(i)));
      }
      return rval;
   }
   
   /*
    * //see base interface method for details
    */
   @SuppressWarnings("unchecked")
   public List<PSAssignmentTypeEnum> getContentAssignmentTypes(List<IPSGuid> ids)
         throws PSSystemException
   {
      String user = getUserName();
      int communityId = getUserCommunityId();
      List<String> roles = getUserRoles();
      return getContentAssignmentTypes(ids, user, roles, communityId);
   }
   
   
   public List<String> getAdhocRoles(IPSGuid contentId, IPSGuid transitionId)
   {
      if (contentId == null)
         throw new IllegalArgumentException("contentId may not be null");
      
      if (transitionId == null)
         throw new IllegalArgumentException("transitionId may not be null");
      
      List<String> results = new ArrayList<>();
      
      // get the component summary
      PSComponentSummary sum = getComponentSummary(contentId);
      if (sum == null)
         return results;
      
      // load the to-state
      PSState toState = getToState(sum, transitionId);
      if (toState == null)
         return results;
      
      // start by adding adhoc normal roles only, but if we come across 
      // anonymous, then just add all roles on the system
      List<PSAssignedRole> assignedRoles = toState.getAssignedRoles();
      List<IPSGuid> roleIds = new ArrayList<>();
      boolean anonymous = false;
      for (PSAssignedRole role : assignedRoles)
      {
         if (role.getAdhocType().equals(PSAdhocTypeEnum.ENABLED))
            roleIds.add(role.getGUID());
         else if (role.getAdhocType().equals(PSAdhocTypeEnum.ANONYMOUS))
         {
            anonymous = true;
            break;
         }
      }
      
      if (!anonymous)
      {
         IPSGuid wfId = m_guidMgr.makeGuid(sum.getWorkflowAppId(), 
            PSTypeEnum.WORKFLOW);
         
         PSWorkflow wf = m_wfService.loadWorkflow(wfId);
         for (PSWorkflowRole role : wf.getRoles())
         {
            if (roleIds.contains(role.getGUID()))
               results.add(role.getName());
         }
      }
      else
      {
         IPSBackEndRoleMgr beRoleMgr = PSRoleMgrLocator.getBackEndRoleManager();
         results.addAll(beRoleMgr.getRhythmyxRoles());
      }
      
      PSAssignmentTypeHelper.filterAssignedRolesByCommunity(contentId, results);
      
      return results;
   }

   @SuppressWarnings("unchecked")
   public List<String> getAdhocRoleMembers(IPSGuid contentId, 
      IPSGuid transitionId, String roleName, String nameFilter)
   {
      if (contentId == null)
         throw new IllegalArgumentException("contentId may not be null");
      
      if (transitionId == null)
         throw new IllegalArgumentException("transitionId may not be null");
      
      if (StringUtils.isBlank(roleName))
         throw new IllegalArgumentException(
            "roleName may not be null or empty");
      
      List<String> results = new ArrayList<>();
      
      // get the component summary
      PSComponentSummary sum = getComponentSummary(contentId);
      if (sum == null)
         return results;
      
      // load the to-state
      PSState toState = getToState(sum, transitionId);
      if (toState == null)
         return results;
      
      // see if normal adhoc role or anonymous
      List<IPSGuid> roleIds = new ArrayList<>();
      boolean anonymous = false;
      for (PSAssignedRole role : toState.getAssignedRoles())
      {
         if (role.getAdhocType().equals(PSAdhocTypeEnum.ENABLED))
            roleIds.add(role.getGUID());
         else if (role.getAdhocType().equals(PSAdhocTypeEnum.ANONYMOUS))
         {
            anonymous = true;
            break;
         }
      }
      
      if (!anonymous)
      {
         IPSGuid wfId = m_guidMgr.makeGuid(sum.getWorkflowAppId(), 
            PSTypeEnum.WORKFLOW);
         
         // ensure an adhoc role is specified
         boolean match = false;
         PSWorkflow wf = m_wfService.loadWorkflow(wfId);
         for (PSWorkflowRole role : wf.getRoles())
         {
            if (role.getName().equals(roleName))
            {
               if (roleIds.contains(role.getGUID()))
                  match = true;
               break;
            }
         }
         
         if (!match)
            return results;
      }
      
      // filter by community if not anonymous
      String communityId = anonymous ? null : String.valueOf(
         sum.getCommunityId());
      PSRoleManager roleMgr = PSRoleManager.getInstance();
      Set<PSSubject> subjects = roleMgr.getSubjects(roleName, nameFilter, 
         PSSubject.SUBJECT_TYPE_USER, null, communityId, true);
      for (PSSubject subject : subjects)
      {
         results.add(subject.getName());
      }
      
      return results;
   }
   
   /**
    * Loads the component summary for the specified item.
    * 
    * @param contentId The content id, assumed not <code>null</code>.
    * 
    * @return the summary, or <code>null</code> if it could not be found 
    * (an error message is logged in this case).
    */
   private PSComponentSummary getComponentSummary(IPSGuid contentId)
   {
      PSComponentSummary sum = m_cmsMgr.loadComponentSummary(contentId.getUUID());
      if (sum == null)
      {
         ms_logger.error("Failed to locate component summary for item: " + 
            contentId);
      }
      
      return sum;
   }
   
   /**
    * Load the destination state of the specified transition.
    * 
    * @param sum The component summary of the item, assumed not
    * <code>null</code>, used to determine the current workflow state.
    * @param transitionId The id of the transition in the current workflow
    * state, assumed not <code>null</code>.
    * 
    * @return The destination state, may be <code>null</code> if we are not able
    * to locate it (error messages are logged in this case).
    */
   private PSState getToState(PSComponentSummary sum, IPSGuid transitionId)
   {
      IPSGuid wfId = m_guidMgr.makeGuid(sum.getWorkflowAppId(), 
         PSTypeEnum.WORKFLOW);
      IPSGuid fromStateId = m_guidMgr.makeGuid(sum.getContentStateId(), 
         PSTypeEnum.WORKFLOW_STATE);
      
      PSState fromState = m_wfService.loadWorkflowState(fromStateId, wfId);
      if (fromState == null)
      {
         ms_logger.error("Failed to locate state for workflowid " + 
            sum.getWorkflowAppId() + " and stateid " + sum.getContentStateId());
         return null;
      }      
      
      // find the transition
      PSTransition trans = null;
      for (PSTransition test : fromState.getTransitions())
      {
         if (test.getGUID().equals(transitionId))
         {
            trans = test;
            break;
         }
      }
      
      if (trans == null)
      {
         ms_logger.error("Failed to locate transition for workflowid " + 
            sum.getWorkflowAppId() + ", stateid " + sum.getContentStateId() + 
            ", and transitionid " + transitionId.getUUID());
         return null;         
      }
      
      PSState toState = m_wfService.loadWorkflowState(m_guidMgr.makeGuid(
         trans.getToState(), PSTypeEnum.WORKFLOW_STATE), wfId);
      
      if (toState == null)
      {
         ms_logger.error("Failed to locate state for workflowid " + 
            sum.getWorkflowAppId() + " and stateid " + trans.getToState());
      }
      
      return toState;
   }

   /**
    * Gets the string representation of a given date and time.
    * 
    * @param date the date & time in question, assumed not <code>null</code>.
    * 
    * @return string representation of the given date & time, never <code>null</code>.
    */
   private String getDateString(Date date)
   {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String dateString = "'" + format.format(date) + "'";

      if (isOracle())
         return "TO_DATE(" + dateString + ", 'YYYY-MM-DD HH24:MI:SS')";
      else
         return dateString;
   }

   /**
    * Determines if the repository is an oracle database.
    * 
    * @return It is <code>true</code> if the repository is an oracle database.
    */
   private boolean isOracle()
   {
      if (m_isOracle != null)
         return m_isOracle.booleanValue();
      
      try
      {
         PSConnectionDetail connDetail = m_dsMgr.getConnectionDetail(null);
         m_isOracle = PSSqlHelper.isOracle(connDetail.getDriver()) ? Boolean.TRUE : Boolean.FALSE;
         return m_isOracle.booleanValue();
      }
      catch (Exception e)
      {
         ms_logger.error("Failed to determine database type", e);
      }
      return false;
   }

   /**
    * Determines if the repository is an oracle database or not.
    * It is <code>true</code> if the repository is an oracle database.
    * Initialized to <code>null</code> and it is lazily set by {@link #isOracle()}.
    */
   private static Boolean m_isOracle = null;
   
   /**
    * The datasource manager to use to override the new configuration,
    * initalized by the first call to
    * {@link #setDatasourceManager(IPSDatasourceManager)}, never
    * <code>null</code> after that.
    */
   private IPSDatasourceManager m_dsMgr;
   
   /**
    * Logger to use, never <code>null</code>.
    */
   private static Log ms_logger = LogFactory.getLog(
      PSSystemService.class);

   /**
    * Describes a particular configuration that may be loaded and saved.
    */
   private class PSMimeContentDescriptor
   {
      /**
       * Calls
       * {@link #PSMimeContentDescriptor(IPSGuid, String, File, String, String)
       * this(guid, fileName, path, null, null)}
       * @param guid The guid, assumed not <code>null</code>.
       * @param fileName The filename, assumed not <code>null</code> or empty.
       * @param path The directory in which the file is located, assumed not
       * <code>null</code>
       */
      PSMimeContentDescriptor(IPSGuid guid, String fileName, File path)
      {
         this(guid, fileName, path, null, null);
      }

      /**
       * Construct a descriptor
       *
       * @param guid The guid, assumed not <code>null</code>.
       * @param fileName The filename, assumed not <code>null</code> or empty.
       * @param dir The directory in which the file is located, assumed not
       * <code>null</code>.
       * @param mimeType The mimetype to use when sending the content , may be
       * <code>null</code> or empty to use the default
       * @param charEnc The character encoding to use when encoding the file
       * contents, may be <code>null</code> or empty to use the default.
       */
      PSMimeContentDescriptor(IPSGuid guid, String fileName, File dir,
         String mimeType, String charEnc)
      {
         mi_guid = guid;

         mi_path = new File(dir, fileName);

         if (!StringUtils.isBlank(mimeType))
            mi_mimeType = mimeType;
         if (!StringUtils.isBlank(charEnc))
            mi_charEncoding = charEnc;
      }

      /**
       * Get the guid supplied during ctor.
       *
       * @return The guid, never <code>null</code>.
       */
      public IPSGuid getGuid()
      {
         return mi_guid;
      }

      /**
       * Get the path to the file.
       *
       * @return The path, never <code>null</code>.
       */
      public File getConfigFile()
      {
         return mi_path;
      }

      /**
       * Get the mime type supplied during ctor.
       *
       * @return The mime type, never <code>null</code> or empty.
       */
      public String getMimeType()
      {
         return mi_mimeType;
      }

      /**
       * Get the mime type supplied during ctor.
       *
       * @return The mime type, never <code>null</code> or empty.
       */
      public String getCharacterEncoding()
      {
         return mi_charEncoding;
      }

      /**
       * The guid supplied during construction, immutable.
       */
      private IPSGuid mi_guid;

      /**
       * The mime type supplied during construction, immutable.
       */
      private String mi_mimeType = IPSMimeContentTypes.MIME_TYPE_TEXT_PLAIN;

      /**
       * The char encoding supplied during construction.
       */
      private String mi_charEncoding = PSCharSetsConstants.rxStdEnc();

      /**
       * The file path specified during construction, immutable.
       */
      private File mi_path;
   }

   /**
    * Map of config types to their descriptors, <code>null</code> until first
    * call to {@link #getContentDescriptor(PSConfigurationTypes)}
    */
   private Map<PSConfigurationTypes, PSMimeContentDescriptor> m_mimeContentMap
   = null;
   
   /**
    * JMS queue sender for queuing email messages. It is set/wired by Spring.
    */
   IPSQueueSender m_emailSender = null;
   
   /**
    * The GUID manager, initialized by constructor.
    */
   IPSGuidManager m_guidMgr;
   
   /**
    * The workflow service, initialized by constructor.
    */
   IPSWorkflowService m_wfService;
   
   /**
    * CMS object manager, initialized by constructor.
    */
   IPSCmsObjectMgr m_cmsMgr;
}

