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
package com.percussion.services.legacy.impl;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSEditorChangeEvent;
import com.percussion.cms.handlers.PSCommandHandler;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSWorkflowCommandHandler;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.data.utils.PSHibernateEvictionTableUpdateHandler;
import com.percussion.design.objectstore.PSConfig;
import com.percussion.design.objectstore.PSConfigurationFactory;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.design.objectstore.PSRole;
import com.percussion.design.objectstore.PSRoleConfiguration;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.server.PSDatabaseComponentLoader;
import com.percussion.error.PSException;
import com.percussion.i18n.PSLocale;
import com.percussion.search.PSSearchIndexEventQueue;
import com.percussion.server.IPSHandlerInitListener;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.PSPersistentProperty;
import com.percussion.server.PSPersistentPropertyMeta;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.server.cache.PSItemSummaryCache;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.data.IPSIdentifiableItem;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.IPSCmsObjectMgrInternal;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.legacy.data.PSItemEntry;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.memory.PSCacheAccessLocator;
import com.percussion.services.menus.PSActionMenu;
import com.percussion.services.menus.PSUIMode;
import com.percussion.services.menus.PSUiContext;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationHelper;
import com.percussion.services.relationship.data.PSRelationshipConfigName;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.system.data.PSContentStatusHistory;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.workflow.IPSStatesContext;
import com.percussion.workflow.IPSWorkflowAppsContext;
import com.percussion.workflow.PSStatesContext;
import com.percussion.workflow.PSWorkflowAppsContext;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.ShortType;
import org.hibernate.type.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.percussion.services.utils.orm.PSDataCollectionHelper.MAX_IDS;
import static com.percussion.services.utils.orm.PSDataCollectionHelper.clearIdSet;
import static com.percussion.services.utils.orm.PSDataCollectionHelper.createIdSet;
import static com.percussion.services.utils.orm.PSDataCollectionHelper.executeQuery;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Implementation class for legacy object accessing
 * 
 * @author dougrand
 */
@Transactional(noRollbackFor = Exception.class)
@PSBaseBean("sys_cmsObjectMgr")
public class PSCmsObjectMgr
      implements
         IPSCmsObjectMgrInternal, IPSHandlerInitListener
{

   private static final String UPDATE_DATE_HQL = "update PSComponentSummary cs set cs.%s = :dateToSet where cs.m_contentId in (:ids)";

   private static final String WHERE_NULL = " and cs.%s is null";

    private static final String UPDATE_DATE_ONLY_ONCE = "update PSComponentSummary cs set cs.%s = :dateToSet where cs.m_contentId in (:ids) and cs.%s is null";
    private static final Object DATE_UPDATE_SYNC_OBJECT = new Object();

   /**
    * Logger
    */
   private static final Logger ms_log = LogManager.getLogger("PSCmsObjectMgr");
   
   private static final int BATCH_SIZE = 50;
   private static ThreadLocal<Integer> ms_itemCount = new ThreadLocal<>();
  
   private Map<Long,PSCommandHandler> workflowHandlers = new ConcurrentHashMap<>(16, 0.9f, 1);

   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   
   /**
    * Ctor, invoked from Spring
    */
   public PSCmsObjectMgr()
   {
      String tables[] =
      {"CONTENTSTATUS", "RXLOCALE"};
      String pks[] =
      {"CONTENTID", "LOCALEID"};
      Class clazz[] =
      {PSComponentSummary.class, PSLocale.class};

      PSServer.addInitListener(new PSHibernateEvictionTableUpdateHandler(tables, pks, clazz));
   }

   /**
    * Maximum of number of Content ID in the IN clause with the UPDATE statement
    */
   private int MAX_UPDATE_ITEMS = 250;

   /*
    *  (non-Javadoc)
    * 
    * @see com.percussion.services.legacy.IPSCmsObjectMgr#touchItems(java.util.
    * Collection)
    */
   public void touchItems(Collection<Integer> ids)
   {
      Date now = new Date();
      updateSummaryDate("m_contentLastModifiedDate", now, ids, true);
      
     PSItemSummaryCache cache = PSItemSummaryCache.getInstance();
      if (cache != null)
         cache.updateLastModifiedDate(ids, now);
        
   }
    public void setPostDate(Collection<Integer> ids) {
        setPostDate(ids,new Date());
    }

    public Date getFirstPublishDate(Integer contentId){
        Date postDate  = null;
        Session session = sessionFactory.getCurrentSession();
        Query q = session.getNamedQuery("getPostDate");
        q.setParameter("contentId", contentId);
        List result = q.list();
        if(result != null && result.size()>0)
            postDate = (Date) result.get(0);
        return postDate;
    }
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.legacy.IPSCmsObjectMgr#setPostDate(java.util.
    * Collection)
    */
   public void setPostDate(Collection<Integer> ids, Date date)
   {

      // Data is not backed up// and this affects ordering of content on
      // customer system.
      PSItemSummaryCache cache = PSItemSummaryCache.getInstance();
      List<Integer> filteredIds = new ArrayList<>();
       for (Integer id:ids) {
           PSItemEntry itemEntry = (PSItemEntry)cache.getItem(id);
           if(itemEntry.getPostDate() == null){
               filteredIds.add(id);
           }
       }
      updateSummaryDate("m_contentPostDate", date, filteredIds, false);
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.legacy.IPSCmsObjectMgr#setPostDate(java.util.
    * Collection)
    */
   public void setPublishDate(List<Integer> ids, Date date)
   {
      updateSummaryDate("m_contentPublishDate",date,ids,true);
      setPostDate(ids,date);
   }

   
   
   
    @Override
    public List<PSUIMode> findUiModes() {
        Session session = sessionFactory.getCurrentSession();
  		try{
            return session.createCriteria(PSUIMode.class).list();
        }catch(Exception e){
            ms_log.warn("An error occurred while listing UI Contexts:" + e.getMessage());
            return new ArrayList<>();
      }
	}

    @Override
    public List<PSActionMenu> findActionMenus() {
        Session session = sessionFactory.getCurrentSession();

        try {
            return session.createCriteria(PSActionMenu.class).addOrder(Order.asc("sortOrder")).list();
        } catch (Exception e) {
            ms_log.warn("An error occurred while listing action menus: {}" , e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<PSActionMenu> findActionMenusByType(String type) {
        Session session = sessionFactory.getCurrentSession();

        try {
            return session.createCriteria(PSActionMenu.class).add(Restrictions.ilike("type",type)).addOrder(Order.asc("sortOrder")).list();
        }catch(Exception e) {
            ms_log.warn("An error occurred while listing action menus by type: {}" , e.getMessage());
            return new ArrayList<>();
        }
   }

    @Override
    public List<PSUiContext> findUiContexts() {
        Session session = sessionFactory.getCurrentSession();
        try {
            return session.createCriteria(PSUiContext.class).list();
        }catch(Exception e) {
            ms_log.warn("An error occurred while listing UI Contexts: {}" ,e.getMessage());
            return new ArrayList<>();
        }
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.legacy.IPSCmsObjectMgr#clearStartDate(java.util.
    * Collection)
    */
   public void clearStartDate(Collection<Integer> ids)
   {
      updateSummaryDate("m_contentStartDate", null, ids, true);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.legacy.IPSCmsObjectMgr#clearExpiryDate(java.util.
    * Collection)
    */
   public void clearExpiryDate(Collection<Integer> ids)
   {
      updateSummaryDate("m_contentExpiryDate", null, ids, true);
               }
     
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.legacy.IPSCmsObjectMgr#createLocale(String, String)
    */
   public PSLocale createLocale(String languageString, String displayName)
   {
      PSLocale locale = new PSLocale(languageString, displayName, null, PSLocale.STATUS_INACTIVE);
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      locale.setLocaleId((int) gmgr.createGuid(PSTypeEnum.LOCALE).longValue());

      return locale;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.legacy.IPSCmsObjectMgr#getLocaleById(long)
    */
   public PSLocale loadLocale(int id)
   {
      return (PSLocale) sessionFactory.getCurrentSession().get(PSLocale.class,
            new Integer(id));
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.legacy.IPSCmsObjectMgr#getLocaleByName(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public PSLocale findLocaleByLanguageString(String lang)
   {

         Criteria c = sessionFactory.getCurrentSession().createCriteria(PSLocale.class).add(
               Restrictions.eq("m_languageString", lang));
         List<PSLocale> locales = c.list();
         if (locales != null && locales.size() > 0)
         {
            return locales.get(0);
         }
         else
         {
            return null;
         }

   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.legacy.IPSCmsObjectMgr#getLocaleByStatus(int)
    */
   @SuppressWarnings("unchecked")
   public List<PSLocale> findLocaleByStatus(int status)
   {
      Session session = sessionFactory.getCurrentSession();

         Criteria c = session.createCriteria(PSLocale.class).add(Restrictions.eq("m_status", status))
               .addOrder(Order.asc("m_displayName"));
         List<PSLocale> locales = c.list();
         return locales;
      }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.legacy.IPSCmsObjectMgr#findLocales(String, String)
    */
   @SuppressWarnings("unchecked")
   public List<PSLocale> findLocales(String lang, String label)
   {
      Session session = sessionFactory.getCurrentSession();

         List<PSLocale> locales = new ArrayList<>();

         Map<String, String> paramMap = new LinkedHashMap<>();
         String queryString = "from PSLocale locale ";

         if (!StringUtils.isBlank(lang))
         {
            queryString += "where locale.m_languageString = :lang ";
            paramMap.put("lang", lang);
         }

         if (!StringUtils.isBlank(label))
         {
            if (paramMap.isEmpty())
               queryString += "where ";
            else
               queryString += "and ";

            queryString += "locale.m_displayName like :label ";
            paramMap.put("label", label);
         }

         queryString += "order by locale.m_displayName asc";
         Query query = session.createQuery(queryString);
         for (Map.Entry<String, String> entry : paramMap.entrySet())
         {
            query.setString(entry.getKey(), entry.getValue());
         }

         locales = (List<PSLocale>) query.list();

         return locales;

      }

   @SuppressWarnings("unchecked")
   public List<PSLocale> findAllLocales()
   {
      Session session = sessionFactory.getCurrentSession();

         List<PSLocale> locales = new ArrayList<>();

         Query query = session.createQuery("from PSLocale locale " + "order by locale.m_displayName asc");

         locales = (List<PSLocale>) query.list();

         return locales;

      }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.legacy.IPSCmsObjectMgr#findAllPersistentMeta()
    */
   @SuppressWarnings("unchecked")
   public List<PSPersistentPropertyMeta> findAllPersistentMeta()
   {
      Session s = sessionFactory.getCurrentSession();

         Criteria c = s.createCriteria(PSPersistentPropertyMeta.class);
         return (List<PSPersistentPropertyMeta>) c.list();

      }

    /**
     * Save a list of PersistentPropertyMeta
     *
     * @param list
     * @return
     */
    @Override
    public List<PSPersistentPropertyMeta> saveAllPersistentMeta(List<PSPersistentPropertyMeta> list) {

        Session s = sessionFactory.getCurrentSession();
        list.forEach(pm -> s.saveOrUpdate(pm));

        return list;
    }

    /***
     * Delete a list of PersistentProperty Meta
     * @param list
     */
    @Override
    public void deleteAllPersistentMeta(List<PSPersistentPropertyMeta> list) {

        Session s = sessionFactory.getCurrentSession();
        list.forEach(pm -> s.delete(pm));
    }

    /***
     * Save one PersistentPropertyMeta
     *
     * @param meta
     * @return
     */
    @Override
    public PSPersistentPropertyMeta savePersistentPropertyMeta(PSPersistentPropertyMeta meta) {


        Session session = sessionFactory.getCurrentSession();
       Criteria criteria = session.createCriteria(PSPersistentPropertyMeta.class);
       PSPersistentPropertyMeta prop = ((PSPersistentPropertyMeta) criteria.add(Restrictions.eq("propertyName", meta.getPropertyName()))
               .add((Restrictions.eq("userName",meta.getUserName()))).uniqueResult());

       if(prop == null){
          //add new
           IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
           meta.setPropertyId(gmgr.createId("HIB_PSX_PERSISTEDPROPERTYMETA"));
           session.persist(meta);
         return meta;
       }else{
          prop.setClassName(meta.getClassName());
          prop.setEnabledState(meta.getEnabledState());
          prop.setOverridable(meta.getOverridable());
          prop.setPropertyName(meta.getPropertyName());
          prop.setPropertySaveType(meta.getPropertySaveType());
          return (PSPersistentPropertyMeta)session.merge(prop);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.legacy.IPSCmsObjectMgr#findPersistentMetaByName(
    * java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public List<PSPersistentPropertyMeta> findPersistentMetaByName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty.");

      List<PSPersistentPropertyMeta> metas = sessionFactory.getCurrentSession()
            .createQuery(
                  "from PSPersistentPropertyMeta pm where pm.userName like :name").setParameter(
                  "name", name).list();
      return metas;
   }

   /**
    * Find all persistent property's meta objects.
    * 
    * @return the meta objects, never <code>null</code>, but may be empty.
    */
   @SuppressWarnings("unchecked")
   public List<PSPersistentProperty> findAllPersistentProperties()
   {
      Session s = sessionFactory.getCurrentSession();

         Criteria c = s.createCriteria(PSPersistentProperty.class);
         return (List<PSPersistentProperty>) c.list();

      }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.legacy.IPSCmsObjectMgr#
    * findPersistentPropertiesByName(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public List<PSPersistentProperty> findPersistentPropertiesByName(String userName)
   {
      if (StringUtils.isBlank(userName))
         throw new IllegalArgumentException("userName may not be null or empty.");

      List<PSPersistentProperty> props = sessionFactory.getCurrentSession()
            .createQuery(
                  "from PSPersistentProperty p where p.m_userName like :name").setParameter(
                  "name", userName).list();
      return props;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.legacy.IPSCmsObjectMgr#savePersistentProperty(com.
    * percussion.server.PSPersistentProperty)
    */
   public void savePersistentProperty(PSPersistentProperty prop)
   {
      if (prop == null)
         throw new IllegalArgumentException("prop may not be null.");

      sessionFactory.getCurrentSession().merge(prop);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.legacy.IPSCmsObjectMgr#deletePersistentProperty(
    * com.percussion.server.PSPersistentProperty)
    */
   public void deletePersistentProperty(PSPersistentProperty prop)
   {
      if (prop == null)
         throw new IllegalArgumentException("prop may not be null.");

      sessionFactory.getCurrentSession().delete(prop);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.legacy.IPSCmsObjectMgr#updatePersistentProperty(
    * com.percussion.server.PSPersistentProperty)
    */
   public void updatePersistentProperty(PSPersistentProperty prop)
   {
      if (prop == null)
         throw new IllegalArgumentException("prop may not be null.");

      sessionFactory.getCurrentSession().update(prop);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.legacy.IPSCmsObjectMgr#saveOrUpdateLocale(com.percussion.
    * i18n.PSLocale)
    */
   public void saveLocale(PSLocale locale)
   {
      sessionFactory.getCurrentSession().saveOrUpdate(locale);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.legacy.IPSCmsObjectMgr#remove(com.percussion.i18n.PSLocale)
    */
   public void deleteLocale(PSLocale locale)
   {
      sessionFactory.getCurrentSession().delete(locale);
   }

   @SuppressWarnings("unchecked")
   public List<PSComponentSummary> loadComponentSummaries(Collection<Integer> ids)
   {
      Session s = sessionFactory.getCurrentSession();
      List<PSComponentSummary> summaries = new ArrayList<>();

         for (Integer id : ids)
         {
            PSComponentSummary sum = (PSComponentSummary) s.get(PSComponentSummary.class, id);
            if (sum != null)
               summaries.add(sum);
         }
         fixupLocators(summaries);


      return summaries;
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.legacy.IPSCmsObjectMgr#loadComponentSummary(int)
    */
   public PSComponentSummary loadComponentSummary(int contentid,boolean refresh)
   {
      Session s = sessionFactory.getCurrentSession();
      PSComponentSummary sum = (PSComponentSummary) s.get(PSComponentSummary.class, contentid);
      if(refresh) {
          s.refresh(sum);
      }
       fixupLocator(sum);
       return sum;

   }
    public PSComponentSummary loadComponentSummary(int contentid)
    {
        return loadComponentSummary(contentid,false);
    }

    /**
    * Call this on all finders to cleanup component summaries.
    * 
    * @param summaries the summaries to modify, assumed never <code>null</code>
    */
   private void fixupLocators(List<PSComponentSummary> summaries)
   {
      for (PSComponentSummary s : summaries)
      {
         fixupLocator(s);
      }
   }

   /**
    * Call this on any finder that returns a single component summary
    * 
    * @param s a summary to modify, may be <code>null</code>
    */
   private void fixupLocator(PSComponentSummary s)
   {
      if (s == null)
         return;
      int contentid = s.getContentId();
      int revision = s.getCurrentLocator().getRevision();
      s.setLocator(new PSLocator(contentid, revision));
   }

   @SuppressWarnings("unchecked")
   public List<PSComponentSummary> findComponentSummariesByCheckedOutUsers(Set<String> users) throws PSORMException
   {
      Session s = sessionFactory.getCurrentSession();

         List<PSComponentSummary> summaries = s
               .createQuery("from PSComponentSummary c where c.m_checkoutUserName in (:users)")
               .setParameterList("users", users).list();
         fixupLocators(summaries);
         return summaries;

      }
   
   @SuppressWarnings("unchecked")
   public List<PSComponentSummary> findComponentSummariesByType(long contentType) throws PSORMException
   {
      List<PSComponentSummary> summaries = sessionFactory.getCurrentSession()
            .createQuery(
                  "from PSComponentSummary c where c.m_contentTypeId = :type").setParameter(
                  "type", contentType).list();
      fixupLocators(summaries);
      return summaries;
   }

   @SuppressWarnings("unchecked")
   public Collection<Integer> findContentIdsByType(long contentType) throws PSORMException
   {
      return sessionFactory.getCurrentSession()
            .createQuery(
                  "select c.m_contentId "
                        + "from PSComponentSummary c where c.m_contentTypeId = :type").setParameter(
                  "type", contentType).list();
   }

   @SuppressWarnings("unchecked")
   public Collection<Integer> findContentIdsByWorkflow(int workflowid) throws PSORMException
   {
      return sessionFactory.getCurrentSession()
            .createQuery(
                  "select c.m_contentId "
                        + "from PSComponentSummary c where c.m_workflowAppId = :workflowid").setParameter(
                  "workflowid", workflowid).list();
   }

   @SuppressWarnings("unchecked")
   public Collection<Integer> findContentIdsByWorkflowStatus(int workflowid, int stateid) throws PSORMException
   {

      
      return sessionFactory.getCurrentSession()
            .createQuery(
                  "select c.m_contentId "
                        + "from PSComponentSummary c where c.m_workflowAppId = :workflowid " +
                        "and c.m_contentStateId = :stateid")
              .setParameter("workflowid",workflowid).setParameter("stateid",stateid)
              .list();

   }
   
   public void saveComponentSummaries(List<PSComponentSummary> summaries) throws PSORMException
   {
      Session session = sessionFactory.getCurrentSession();
      summaries.forEach(sum -> session.saveOrUpdate(sum));

   }

   public void deleteComponentSummaries(List<PSComponentSummary> summaries) throws PSORMException
   {
      Session session = sessionFactory.getCurrentSession();
      summaries.forEach(sum -> session.delete(sum));
   }

   public void evictComponentSummaries(List<Integer> ids)
   {
      SessionFactory fact = getSessionFactory();
      for (Integer id : ids)
      {
         fact.getCache().evictEntity(PSComponentSummary.class, id);
      }
   }

   public IPSWorkflowAppsContext loadWorkflowAppContext(int workflowAppId)
   {
      IPSWorkflowAppsContext wf = null;

      PSWorkflow workflow = loadWorkflow(workflowAppId);
      if (workflow != null)
         wf = new PSWorkflowAppsContext(workflow);

      return wf;
   }

   public IPSStatesContext loadWorkflowState(int workflowAppId, int stateid)
   {
      IPSStatesContext statesCtx = null;
      IPSWorkflowService svc = PSWorkflowServiceLocator.getWorkflowService();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid workflowId = gmgr.makeGuid(workflowAppId, PSTypeEnum.WORKFLOW);
      IPSGuid stateId = gmgr.makeGuid(stateid, PSTypeEnum.WORKFLOW_STATE);
      PSState state = svc.loadWorkflowState(stateId, workflowId);
      if (state != null)
      {
         statesCtx = new PSStatesContext(state);
      }

      return statesCtx;
   }

   @SuppressWarnings("unchecked")
   public <T extends IPSIdentifiableItem> List<T> filterItemsByPublishableFlag(List<T> items, List<String> flags)
         throws PSORMException
   {
      Session s = sessionFactory.getCurrentSession();
      List<T> rval = new ArrayList<>();
      List<Integer> cids = new ArrayList<>();
      for (T item : items)
      {
         PSLegacyGuid lg = (PSLegacyGuid) item.getItemId();
         cids.add(lg.getContentId());
      }
      long idset = 0;
      
      try
      {
         Query q;
         if (cids.size() < MAX_IDS)
         {
            q = s.createQuery("SELECT c.m_contentId " + "FROM PSComponentSummary c, PSState s "
                  + "WHERE c.m_contentId in (:ids) AND " + "c.m_workflowAppId = s.workflowId AND "
                  + "c.m_contentStateId = s.stateId AND " + "s.contentValidValue in (:flags)");
            q.setParameterList("ids", cids);
            q.setParameterList("flags", flags);
         }
         else
         {
            idset = createIdSet(s, cids);
            q = s.createQuery("SELECT c.m_contentId " + "FROM PSComponentSummary c, PSState s, PSTempId t "
                  + "WHERE c.m_contentId = t.pk.itemId AND " + "t.pk.id = :idset AND "
                  + "c.m_workflowAppId = s.workflowId AND " + "c.m_contentStateId = s.stateId AND "
                  + "s.contentValidValue in (:flags)");
            q.setParameter("idset", idset);
            q.setParameterList("flags", flags);           
         }
         // 
         List<Integer> results = (idset != 0) ? (List) executeQuery(q) : q.list();
         Set<Integer> passedcids = new HashSet<>();
         for (Integer cid : results)
         {
            passedcids.add(cid);
         }
         for (T item : items)
         {
            PSLegacyGuid lg = (PSLegacyGuid) item.getItemId();
            if (passedcids.contains(lg.getContentId()))
            {
               rval.add(item);
            }
         }
      }
      finally
      {
         if (idset != 0)
         {
            clearIdSet(s, idset);
         }

      }

      return rval;
   }

   public void handleDataEviction(Class clazz, Serializable id) throws PSORMException
   {
      Session s = sessionFactory.getCurrentSession();

      try
      {
         ms_log.debug("Evicting " + id + " for class " + clazz);
         SessionFactory sf = s.getSessionFactory();
         ClassMetadata cm = sf.getClassMetadata(clazz);

         if (id instanceof String)
         {
            if (cm.getIdentifierType() instanceof LongType)
               id = new Long((String) id);
            else if (cm.getIdentifierType() instanceof IntegerType)
               id = new Integer((String) id);
            else if (cm.getIdentifierType() instanceof ShortType)
               id = new Short((String) id);
            else if (!(cm.getIdentifierType() instanceof StringType))
               id = null;
         }
         if (id != null)
            s.getSessionFactory().getCache().evictEntity(clazz, id);
         else
            s.getSessionFactory().getCache().evictEntityRegion(clazz);
         
         // Handle eviction from data object cache for classes that may be
         // cached in memory. Required until we eliminate applications. (if
         // ever)
         if (id instanceof Number)
         {
            PSTypeEnum type = PSTypeEnum.valueOf(clazz);

            if (type != null)
            {
               IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
               IPSGuid invalidateGuid = gmgr.makeGuid(((Number) id).longValue(), type);
               PSNotificationHelper.notifyInvalidation(invalidateGuid);
            }
         }
      }
      catch (Throwable t)
      {
         throw new PSORMException("Problem evicting object of class " + clazz + " from cache with id " + id);
      }

   }

   // implement IPSCmsObjectMgr.findAllConfigs()
   @SuppressWarnings(value =
   {"unchecked"})
   public Collection<PSConfig> findAllConfigs() throws PSCmsException
   {
      Session s = sessionFactory.getCurrentSession();

         List<PSConfig> result = s.createCriteria(PSConfig.class).list();
         for (PSConfig c : result)
         {
            if (PSConfigurationFactory.RELATIONSHIPS_CFG.equals(c.getName()))
               resetRelationshipConfigIds(c, s);
         }
         return result;

      }

   // implement IPSCmsObjectMgr.findConfig(String)
   public PSConfig findConfig(String name) throws PSCmsException
   {
      Session s = sessionFactory.getCurrentSession();

         PSConfig config = (PSConfig) s.get(PSConfig.class, name);
         if (PSConfigurationFactory.RELATIONSHIPS_CFG.equals(name))
            resetRelationshipConfigIds(config, s);

         return config;


   }

   // implement IPSCmsObjectMgr.saveConfig(PSCOnfig)
   public void saveConfig(PSConfig config) throws PSCmsException
   {
      Session s = sessionFactory.getCurrentSession();

         if (config.getName().equals(PSConfigurationFactory.RELATIONSHIPS_CFG))
         {
            PSRelationshipConfigSet configSet = getRelationshipConfigSet(config);
            validateSysRelationshipConfigs(configSet);
            if (updateUserRelationshipConfigNameIds(configSet, s))
               setRelationshipConfigSet(config, configSet);
         }

         s.update(config);

   }

   /**
    * Resets the ids for all relationship configurations according to the id /
    * name mapping table. This is used to keep the relationship configurations
    * (saved in config table) in sync with the id / name mapping. An unknown
    * config name (of a config) will be saved into the id/name mapping table,
    * where the id will be the next available number for the id/name mapping
    * table.
    * 
    * @param config the relationship configuration set, retrieved from the
    *           config table, assumed not <code>null</code>.
    * @param s the session object, used for the persistent layer, assumed not
    *           <code>null</code>.
    * 
    * @return <code>true</code> if any of the id has been reset in the given
    *         config object.
    * 
    * @throws PSCmsException if failed to construct relationship configuration
    *            set from the supplied config object.
    */
   private boolean resetRelationshipConfigIds(PSConfig config, Session s) throws PSCmsException
   {
      PSRelationshipConfigSet configset = getRelationshipConfigSet(config);

      Collection<PSRelationshipConfigName> names = findAllRelationshipConfigNames();
      Collection<PSRelationshipConfigName> existNames = new ArrayList<>();
      Iterator configs = configset.iterator();
      PSRelationshipConfig rconfig;
      PSRelationshipConfigName cfgName;

      boolean isResetId = false;
      while (configs.hasNext())
      {
         rconfig = (PSRelationshipConfig) configs.next();
         cfgName = findConfigName(rconfig.getName(), names);
         if (cfgName != null)
         {
            existNames.add(cfgName);
            if (rconfig.getId() != cfgName.getId())
            {
               rconfig.resetId();
               rconfig.setId(cfgName.getId());
               isResetId = true;
            }
         }
         else
         {
            // the name of the config does not exist in the id/name mapping
            // table. It should never happen. Same the (new) name
            int id = (int) PSGuidHelper.generateNextLong(PSTypeEnum.RELATIONSHIP_CONFIGNAME);
            rconfig.resetId();
            rconfig.setId(id);
            isResetId = true;
            cfgName = new PSRelationshipConfigName(rconfig.getName(), id);
            s.save(cfgName);
         }
      }

      // if the id is out of sync between document and id/name mapping table
      // update the config object
      if (isResetId)
         setRelationshipConfigSet(config, configset);

      return isResetId;
   }

   /**
    * Updates the names for the user defined relationship configurations, and
    * keep the id & name in sync between the relationship configs and the
    * id/name mapping table. This must be called before store the relationship
    * configurations to the database. It does the following for each user
    * defined relationship configurations: (1) if id of the config is
    * unassigned, then set an assigned id to the config and save (insert) the
    * id/name to the mapping table (2) if the id of the config does not exist in
    * the id/name mapping table, then save (insert) the id/name to the mapping
    * table. (3) if same id but different name between the config and the
    * id/name mapping table, then update the id/name mapping table with the name
    * of the config. (4) remove any orphen entries in the id/name mapping table.
    * 
    * @param configSet the to be updated (or saved) relationship configurations,
    *           assumed not <code>null</code>.
    * @param s the session object, used for the persistent layer, assumed not
    *           <code>null</code>.
    * 
    * @return <code>true</code> if any of the id has been reset in the given
    *         user defined configurations.
    * 
    * @throws PSCmsException if failed to update the config names.
    */
   private boolean updateUserRelationshipConfigNameIds(PSRelationshipConfigSet configSet, Session s)
         throws PSCmsException
   {
      Collection<PSRelationshipConfigName> names = findAllRelationshipConfigNames();
      Collection<PSRelationshipConfigName> existNames = new ArrayList<>();
      PSRelationshipConfigName cfgName;

      boolean isResetId = false;
      for (PSRelationshipConfig config : configSet.getConfigList())
      {
         validateRelationshipConfigNameId(config, configSet);
         cfgName = config.isAssinedId() ? findConfigNameById(config.getId(), names) : null;
         if (cfgName != null)
         {
            existNames.add(cfgName);
            if (!cfgName.getName().equalsIgnoreCase(config.getName()))
            {
               cfgName.setName(config.getName());
               s.update(cfgName); // update with the new name
            }
         }
         else
         // new id & name pair
         {
            int id = config.getId();
            if (!config.isAssinedId())
            {
               id = (int) PSGuidHelper.generateNextLong(PSTypeEnum.RELATIONSHIP_CONFIGNAME);
               config.resetId();
               config.setId(id);
               isResetId = true;
            }
            cfgName = new PSRelationshipConfigName(config.getName(), id);
            s.save(cfgName);
         }
      }

      // delete unused config names
      names.removeAll(existNames);
      for (PSRelationshipConfigName name : names)
      {
         try
         {
            s.delete(name);
         }
         catch (Exception e)
         {
            String[] args = new String[]
            {name.getName(), String.valueOf(name.getId()), e.getLocalizedMessage()};
            throw new PSCmsException(IPSCmsErrors.FAILED_DELETE_REL_CONFIG_NAME, args);
         }
      }

      return isResetId;
   }

   /**
    * Validates the id of a user defined relationship config. Do nothing if it
    * does not have an assigned id; otherwise, the assigned id must not already
    * been used by other configurations.
    * 
    * @param config the to be validated user relationship config, assumed not
    *           <code>null</code>.
    * @param configSet the configuration set, assumed never <code>null</code>
    * 
    * @throws PSCmsException if the validation failed.
    */
   private void validateRelationshipConfigNameId(PSRelationshipConfig config, PSRelationshipConfigSet configSet)
         throws PSCmsException
   {
      if (!config.isAssinedId())
         return;

      int id = config.getId();
      Iterator configs = configSet.iterator();
      while (configs.hasNext())
      {
         PSRelationshipConfig c = (PSRelationshipConfig) configs.next();
         if (c != config) // avoid itself
         {
            if (c.getId() == config.getId())
            {
               String[] args = new String[]
               {String.valueOf(id), config.getName()};
               throw new PSCmsException(IPSCmsErrors.INVALID_REL_CONFIG_ID, args);
            }
            if (c.getName().equalsIgnoreCase(config.getName()))
            {
               throw new PSCmsException(IPSCmsErrors.INVALID_REL_CONFIG_NAME, config.getName());
            }

         }
      }
   }

   /**
    * Validating pre-defined system relationship configurations in the given
    * configs. Make sure the ids/names of the system relationship configurations
    * match the predefined ones in {@link PSRelationshipConfig.SysConfigEnum}.
    * 
    * @param configSet the to be validated configurations.
    * 
    * @throws PSCmsException if the validation failed.
    */
   private void validateSysRelationshipConfigs(PSRelationshipConfigSet configSet) throws PSCmsException
   {
      // the pre-defined name must match its pre-defined id
      for (PSRelationshipConfig.SysConfigEnum sc : PSRelationshipConfig.SysConfigEnum.values())
      {
         PSRelationshipConfig c = configSet.getConfig(sc.getName());
         if (c!=null &&  c.getId() != sc.getId())
         {
            String[] args = new String[]
            {sc.getName(), String.valueOf(sc.getId()), String.valueOf(c.getId())};
            throw new PSCmsException(IPSCmsErrors.UNKOWN_SYS_REL_CONFIG_ID, args);
         }
      }
   }

   /**
    * Gets the relationship configurations from the supplied config.
    * 
    * @param config it contains XML representation of the relationship
    *           configurations, assumed not <code>null</code>.
    * 
    * @return the retrieved relationship configurations, never
    *         <code>null</code>.
    * 
    * @throws PSCmsException failed to create relationship config set from its
    *            XML representation.
    */
   private PSRelationshipConfigSet getRelationshipConfigSet(PSConfig config) throws PSCmsException
   {
      Document doc = (Document) config.getConfig();
      PSRelationshipConfigSet configset;
      try
      {
         configset = new PSRelationshipConfigSet(doc.getDocumentElement(), null, null);
         return configset;
      }
      catch (PSUnknownNodeTypeException e)
      {
         // this is not possible
         e.printStackTrace();
         throw new PSCmsException(IPSCmsErrors.FAILED_GET_REL_CONFIG_FROM_XML, e.getLocalizedMessage());
      }
   }

   /**
    * Replaces the relationship configurations in the supplied {@link PSConfig}
    * object with the new relationship config set.
    * 
    * @param config the object that hold the XML representation of the
    *           relationship configurations, assumed not <code>null</code>.
    * @param configSet the new relationship config set, assumed not
    *           <code>null</code>.
    */
   private void setRelationshipConfigSet(PSConfig config, PSRelationshipConfigSet configSet)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = configSet.toXml(doc);
      PSXmlDocumentBuilder.replaceRoot(doc, root);
      config.setConfig(doc);
   }

   /**
    * Finds the config name object with the supplied name from the given name
    * list.
    * 
    * @param name the looked up name, assumed not <code>null</code>.
    * @param cfgnames a list of config names, assumed not <code>null</code>, but
    *           may be empty.
    * 
    * @return the found config name object, it may be <code>null</code> if
    *         cannot find one.
    */
   private PSRelationshipConfigName findConfigName(String name, Collection<PSRelationshipConfigName> cfgnames)
   {
      for (PSRelationshipConfigName cfgname : cfgnames)
      {
         if (cfgname.getName().equals(name))
            return cfgname;
      }
      return null;
   }

   /**
    * Finds the config name object by a specified id from the given name list.
    * 
    * @param id the looked up id.
    * @param cfgnames a list of config names, assumed not <code>null</code>, but
    *           may be empty.
    * 
    * @return the config name object with the specified id, it may be
    *         <code>null</code> if cannot find one.
    */
   private PSRelationshipConfigName findConfigNameById(int id, Collection<PSRelationshipConfigName> cfgnames)
   {
      for (PSRelationshipConfigName cfgname : cfgnames)
      {
         if (cfgname.getId() == id)
            return cfgname;
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   public void flushSecondLevelCache()
   {
      Session s = sessionFactory.getCurrentSession();

         SessionFactory fact = s.getSessionFactory();

         fact.getCache().evictQueryRegions();
         Map<String, ClassMetadata> metamap = fact.getAllClassMetadata();
         Map<String, CollectionMetadata> collmap = fact.getAllCollectionMetadata();
         for (String entity : metamap.keySet())
         {
            ClassMetadata data = metamap.get(entity);
            Class pclass = data.getMappedClass();
            fact.getCache().evictEntityRegion(pclass);
         }
         for (String collection : collmap.keySet())
         {
            fact.getCache().evictCollectionRegion(collection);
         }
         if (ms_log.isDebugEnabled())
            ms_log.debug("Flushed hibernate 2nd level cache.");
         
         // Clear the Eh cache
         IPSCacheAccess cache = PSCacheAccessLocator.getCacheAccess();
         cache.clear();


   }

   // Implements IPSRelationshipService.findAllRelationshipConfigNames()
   @SuppressWarnings("unchecked")
   public Collection<PSRelationshipConfigName> findAllRelationshipConfigNames()
   {
      List<PSRelationshipConfigName> names = sessionFactory.getCurrentSession()
              .createCriteria(PSRelationshipConfigName.class).list();

      if (names == null)
         return Collections.EMPTY_LIST;
      else
         return names;
   }

   // Implements IPSRelationshipService.findRelationshipConfigNames()
   @SuppressWarnings("unchecked")
   public List<PSRelationshipConfigName> findRelationshipConfigNames(String name)
   {
      Session sess = sessionFactory.getCurrentSession();

         List rels = sess.createCriteria(PSRelationshipConfigName.class).add(Restrictions.ilike("config_name", name))
               .list();
         return rels;

      }

   /*
    * (non-Javadoc)
    * 
    * @see IPSCmsObjectMgr#findRolesByName(String)
    */
   public List<PSRole> findRolesByName(String name)
   {
      if (StringUtils.isBlank(name))
         name = "%";

      PSRequest request = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);

      PSRoleConfiguration config = new PSRoleConfiguration();
      try
      {
         config.fromDb(new PSDatabaseComponentLoader(request));
      }
      catch (PSException e)
      {
         // ignore, this should never happen
      }

      List<PSRole> roles = new ArrayList<>();

      Pattern pattern = Pattern.compile(name.replace("%", ".*"), Pattern.CASE_INSENSITIVE);
      Iterator components = config.getRoles().iterator();
      while (components.hasNext())
      {
         PSRole role = (PSRole) components.next();

         Matcher matcher = pattern.matcher(role.getName());
         if (matcher.matches())
            roles.add(role);
      }

      return roles;
   }

   @SuppressWarnings("unchecked")
   public List<IPSGuid> findPublicOrCurrentGuids(List<Integer> ids)
   {
      Session s = sessionFactory.getCurrentSession();


         List<PSComponentSummary> results = loadComponentSummaries(ids);
         List<IPSGuid> rval = new ArrayList<>(results.size());
         Map<Integer, IPSGuid> rmap = new HashMap<>(results.size());
         for (PSComponentSummary r : results)
         {
            Integer contentid = r.getContentId();
            Integer revision = r.getPublicOrCurrentRevision();
            rmap.put(contentid, new PSLegacyGuid(contentid, revision));
         }

         // Maintain ordering
         for (Integer id : ids)
         {
            rval.add(rmap.get(id));
         }
         return rval;

      }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.legacy.IPSCmsObjectMgr#loadCmsObject(int)
    */
   public PSCmsObject loadCmsObject(int objectType)
   {
      Session s = sessionFactory.getCurrentSession();

         PSCmsObject cmsObject = (PSCmsObject) s.get(PSCmsObject.class, objectType);
         return cmsObject;

      }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.legacy.IPSCmsObjectMgr#findAllCmsObjects()
    */
   @SuppressWarnings("unchecked")
   public List<PSCmsObject> findAllCmsObjects()
   {
      Session session = sessionFactory.getCurrentSession();

         Query query = session.createQuery("from PSCmsObject");

         List<PSCmsObject> cmsObjects = (List<PSCmsObject>) query.list();

         return cmsObjects;


   }

   public Set<Long> findContentTypesForIds(Collection<? extends Object> contentIds)
   {
      if (contentIds.size() == 0)
      {
         return Collections.EMPTY_SET;
      }
      
      Session session = sessionFactory.getCurrentSession();

         // Convert incoming id strings to id numbers as appropriate
         List<Integer> ids = new ArrayList<>();
         for(Object id : contentIds)
         {
            if (id instanceof String)
            {
               try
               {
                  ids.add(Integer.parseInt((String) id));
               }
               catch(NumberFormatException nfe)
               {
                  ms_log.error("Bad content id found " + id);
               }
            }
            else if (id instanceof Integer)
            {
               ids.add((Integer) id);
            }
            else if (id instanceof Number)
            {
               ids.add(((Number) id).intValue());
            }
            else
            {
               ms_log.error("Bad contentid found (wrong class): " + id);
            }
         }
         
         Set<Long> rval = new HashSet<>();
         List<Number> results;
         // Grab max ids at a time from the list, if less than max do the
         // entire list
         results = new ArrayList<>();
         for (int i = 0; i < ids.size(); i += MAX_IDS)
         {
            int end = i + MAX_IDS;
            if (end > ids.size())
            {
               end = ids.size();
            }
            Criteria c = session.createCriteria(PSComponentSummary.class);
            c.add(Restrictions.in("m_contentId", ids.subList(i, end)));
            c.setProjection(Projections.distinct(Projections.property("m_contentTypeId")));
            results.addAll(c.list());
         }
         
         for(Number n : results)
         {
            rval.add(n.longValue());
         }
         return rval;

      }
   
   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.legacy.IPSCmsObjectMgr#findItemEntries(java.util.
    * List, java.util.Comparator)
    */
   public List<IPSItemEntry> findItemEntries(List<Integer> contentIds, Comparator<IPSItemEntry> comparator)
   {
      notNull(contentIds);
      
      PSItemSummaryCache itemCache = PSItemSummaryCache.getInstance();
      if (itemCache == null)
         return Collections.emptyList();
      
      List<IPSItemEntry> itemEntries = itemCache.getItems(contentIds);
      
      if (comparator != null)
         Collections.sort(itemEntries, comparator);
      
      return itemEntries;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.legacy.IPSCmsObjectMgr#findItemEntry(int)
    */
   public IPSItemEntry findItemEntry(int contentId)
   {
      PSItemSummaryCache itemSummaryCache = PSItemSummaryCache.getInstance();
      return itemSummaryCache == null ? null : itemSummaryCache.getItem(contentId);
   }
   
   /**
    * Convert an integer object to int
    * 
    * @param i the integer object, it may be <code>null</code>.
    * @param defaultValue the returned default value if the integer object is
    *           <code>null</code>.
    *   
    * @return the primitive integer.
    */
   private int toInt(Object i, int defaultValue)
   {
      if (i == null)
         return defaultValue;
      
      if (i instanceof Long)
         return ((Long) i).intValue();
      else
         return ((Integer) i).intValue();
   }

   /**
    * Set the state name for the specified item
    * 
    * @param item the item, not <code>null</code>.
    * @param wfStateIdMap maps the work-flow ID to the related state ID / name map, never <code>null</code>.
    * this is used for caching the work-flow state ID/name mapping to improve processing performance.
    */
   private void setStateName(PSItemEntry item, Map<Integer, Map<Integer, String>> wfStateIdMap)
   {
      if (item.getWorkflowAppId() < 0 || item.getContentStateId() < 0)
         return;
      
      Map<Integer, String> map = getStateIdNameMap(item.getWorkflowAppId(), wfStateIdMap);
      String name = map.get(item.getContentStateId());
      item.setStateName(name);
   }
   
   /**
    * Gets the state ID/name mapping for the specified work-flow.
    * 
    * @param wfId the ID of the work-flow.
    * @param wfStateIdNameMap maps the work-flow ID to the related state ID /
    *           name map, never <code>null</code>. this is used for caching the
    *           work-flow state ID/name mapping to improve processing
    *           performance.
    * @return the state ID/name mapping of the specified work-flow, never
    *         <code>null</code>.
    */
   private Map<Integer, String> getStateIdNameMap(int wfId, Map<Integer, Map<Integer, String>> wfStateIdNameMap)
   {
      Map<Integer, String> stateIdNameMap = wfStateIdNameMap.get(wfId);
      if (stateIdNameMap != null)
         return stateIdNameMap;
      
      PSWorkflow wf = loadWorkflow(wfId);
      Map<Integer, String> map = new HashMap<>();
      if (wf == null)
      {
         ms_log.warn("Failed to load workflow id = " + wfId);
         wfStateIdNameMap.put(wfId, map);         
         return map;
      }
      
      for (PSState s : wf.getStates())
      {
         map.put((int)s.getStateId(), s.getName());
      }
      wfStateIdNameMap.put(wfId, map);
      return map;         
   }

   /**
    * Load the specified workflow, see
    * {@link IPSWorkflowService#loadWorkflow(IPSGuid)} for details.
    * 
    * @param wfId
    * 
    * @return The workflow, <code>null</code> if not found.
    */
   private PSWorkflow loadWorkflow(int wfId)
   {
      IPSWorkflowService svc = PSWorkflowServiceLocator.getWorkflowService();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      PSWorkflow wf = svc.loadWorkflow(gmgr.makeGuid(wfId, PSTypeEnum.WORKFLOW));
      return wf;
   }
   
   /**
    * Set the content type label for the specified item if possible.
    * 
    * @param item the item, not <code>null</code>.
    * @param ctTypeIdLabel it maps content type ID to content type name, never
    *           <code>null</code>. This is used for caching the content type
    *           ID/name to improve processing performance.
    */
   private void setContentTypeLabel(PSItemEntry item, Map<Integer, String> ctTypeIdLabel)
   {
      int contentTypeId = item.getContentTypeId();
      if (contentTypeId < 0)
         return;
      
      String label = ctTypeIdLabel.get(contentTypeId);
      if (label != null)
      {
         item.setContentTypeLabel(label);
         return;
      }
      
      try
      {
         label = PSItemDefManager.getInstance().contentTypeIdToLabel(contentTypeId);
         ctTypeIdLabel.put(contentTypeId, label);
         item.setContentTypeLabel(label);
      }
      catch (PSInvalidContentTypeException e)
      {
         ms_log.warn("Invalid content type id (" + contentTypeId + ") for contentId = " + item.getContentId());
      }
   }
   
   /**
    * Create an item entry from a row of item info
    * 
    * @param item the raw data return from query, assumed not <code>null</code>.
    * @param wfStateIdNameMap maps the work-flow ID to the related state ID /
    *           name map, never <code>null</code>. this is used for caching the
    *           work-flow state ID/name mapping to improve processing
    *           performance.
    * @param ctTypeIdLabel it maps content type ID to content type name, never
    *           <code>null</code>. This is used for caching the content type
    *           ID/name to improve processing performance.
    * 
    * @return the item entry, not <code>null</code>.
    */
   private PSItemEntry createItemEntry(Object[] item, Map<Integer, Map<Integer, String>> wfStateIdNameMap,
         Map<Integer, String> ctTypeIdLabel)
   {
      int contentId         = toInt(item[0], -1);
      String name           = (String)item[1];
      int communityId       = toInt(item[2], -1);
      int contentTypeId     = toInt(item[3], -1);
      int objectType        = toInt(item[4], -1);
      String createdBy      = (String)item[5];
      Date lastModifiedDate = (Date)item[6];
       Date postDate         = (Date)item[7];
       if(item[7] == null && item[16] != null) {
           //Find the first PublishDate From PSX_PUBLICATION_DOC and set that as post Date.
           postDate = getFirstPublishDate(contentId);
       }
      Date createdDate      = (Date)item[8];
      int workflowId        = toInt(item[9], -1);
      int stateId           = toInt(item[10], -1);
      int tipRevision           = toInt(item[11], -1);
      int currentRevision           = toInt(item[12], -1);
      int publicRevision           = toInt(item[13], -1);
      String lastModifier = (String)item[14];
      String checkedOutUserName = (String)item[15];
      PSItemEntry itemEntry = new PSItemEntry(contentId,
               name,
               communityId,
               contentTypeId,
               objectType,
               createdBy,
               lastModifiedDate,
               lastModifier,
               postDate,
               createdDate,
               workflowId,
               stateId,
               tipRevision,currentRevision,publicRevision,
              checkedOutUserName);
      
      setStateName(itemEntry, wfStateIdNameMap);
      setContentTypeLabel(itemEntry, ctTypeIdLabel);

      return itemEntry;
   }

   private static final String itemQuery = "select c.m_contentId, c.m_name, c.m_communityId, " +
           "c.m_contentTypeId, c.m_objectType, c.m_contentCreatedBy, " +
           "c.m_contentLastModifiedDate, c.m_contentPostDate, c.m_contentCreatedDate, " +
           "c.m_workflowAppId, c.m_contentStateId, c.m_tipRevision, c.m_currRevision, " +
           "c.m_publicRevision, c.m_contentLastModifier, c.m_checkoutUserName, c.m_contentPublishDate from PSComponentSummary c";
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.legacy.IPSCmsObjectMgr#loadAllItemEntries()
    */
   public Collection<IPSItemEntry> loadAllItemEntries()
   {
      Session session = sessionFactory.getCurrentSession();

         Query q = session.createQuery(itemQuery);
         List<Object[]> listItems = q.list();
         
         List<IPSItemEntry> allEntries = new ArrayList<>();
         Map<Integer, Map<Integer, String>> wfStateIdNameMap = new HashMap<>();
         Map<Integer, String> ctTypeIdLabelMap = new HashMap<>();
         
         for (Object[] row : listItems)
         {
            PSItemEntry entry = createItemEntry(row, wfStateIdNameMap, ctTypeIdLabelMap);
            allEntries.add(entry);
         }
         
         return allEntries;

      }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.legacy.IPSCmsObjectMgr#loadItemEntry(Integer id)
     */
    public IPSItemEntry loadItemEntry(Integer id)
    {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery(itemQuery + " where c.m_contentId=" + id);
        List<Object[]> listItems = q.list();

        Map<Integer, Map<Integer, String>> wfStateIdNameMap = new HashMap<>();
        Map<Integer, String> ctTypeIdLabelMap = new HashMap<>();
        if(listItems.size() > 0 ){
            //Should never happen unless corruted data
            if(listItems.size() > 1){
                ms_log.warn("Duplicate records found for id: {} Returning 1st record" + id);
            }
            return createItemEntry(listItems.get(0), wfStateIdNameMap, ctTypeIdLabelMap);
        }
        return null;
    }

   public void changeWorkflowForItem(int itemId, int workflowId, List<String> validStateNames) throws PSORMException
   {
      Validate.notNull(validStateNames);
            
      if (!findCheckedOutContentIds(Arrays.asList(itemId)).isEmpty())
      {
         throw new IllegalArgumentException("Item with id " + itemId + " is checked out.");         
      }
      
      IPSItemEntry itemEntry = findItemEntry(itemId);
      if (itemEntry == null)
      {
         throw new IllegalArgumentException("Item with id " + itemId + " does not exist.");
      }
      
      if (itemEntry.isFolder())
      {
         throw new IllegalArgumentException("Item with id " + itemId + " is a folder.");
      }
      
      int itemWf = itemEntry.getWorkflowAppId();
      String itemState = itemEntry.getStateName();
      
      // skip if already in the correct workflow
      if (itemWf == workflowId)
         return;
      
      // If not a valid state, change to default
      if (!validStateNames.contains(itemState))
         itemState = validStateNames.get(0);
      
      Map<Integer, String> wfStateIdNameMap = getStateIdNameMap(workflowId,
            new HashMap<>());
      Map<String, Integer> wfStateNameIdMap = MapUtils.invertMap(wfStateIdNameMap);
      
      Integer targetStateId = wfStateNameIdMap.get(itemState);
      if (targetStateId == null)
         throw new RuntimeException("State name " + itemState + " not found in workflow with id: " + workflowId);
      
      PSRequest userReq = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      String roleName = userReq.getUserSession().getUserRoles().get(0);
      ms_itemCount.set(0);
      updateWorkflowAndState(workflowId, targetStateId, Arrays.asList(itemEntry), roleName, userReq);
   }

   @Override
   public void forceCheckinUsers(HashMap<String, PSUserSession> usersMap)
   {
      Session session = sessionFactory.getCurrentSession();
      Validate.notNull(usersMap);
 
      if (usersMap.size()>0)
      {
         
         IPSSystemService svc = PSSystemServiceLocator.getSystemService();
         try {
         List<PSComponentSummary> summaries = findComponentSummariesByCheckedOutUsers(usersMap.keySet());
         Date now = new Date();
         for (PSComponentSummary summary : summaries)
         {
            String checkedOutTo =  summary.getCheckoutUserName();
            PSUserSession sess = usersMap.get(checkedOutTo);
            int id = summary.getContentId();
            int tip = summary.getTipRevision();
            IPSItemEntry itemEntry = findItemEntry(id);
            summary.setEditRevision(-1);
            summary.setCurrRevision(tip);   
            summary.setCheckoutUserName("");
            
            PSContentStatusHistory newHistory = new PSContentStatusHistory();
            newHistory.setActor("rxserver");
            newHistory.setCheckoutUserName("");
            newHistory.setContentId(summary.getContentId());
            newHistory.setEventTime(now);
            newHistory.setId(-1);
            newHistory.setIsValidValue(summary.isValidState(summary.getState())? "Y" : "N");
        
            newHistory.setLastModifiedDate(summary.getContentLastModifiedDate());
            newHistory.setLastModifierName(summary.getContentLastModifier());
            newHistory.setRevision(tip);
            newHistory.setRoleName("Admin");
            newHistory.setStateId(summary.getState());
            newHistory.setStateName(itemEntry.getStateName());
            newHistory.setTitle(summary.getName());
            newHistory.setTransitionLabel("CheckOut");
            newHistory.setWorkflowId(summary.getWorkflowAppId());
            newHistory.setSessionId(StringUtils.defaultString(sess.getId()));
            
            session.update(summary);
            svc.saveContentStatusHistory(newHistory);
            
            //  Need to update change listners of checkin
               PSEditorChangeEvent e = new PSEditorChangeEvent(PSEditorChangeEvent.ACTION_CHECKIN,
                     summary.getContentId(), tip, -1, -1, summary.getContentTypeId());
     
            PSCommandHandler handler = workflowHandlers.get(summary.getContentTypeId());
            
            if (handler!=null)
               handler.updateChangeListners(e);
            
            ms_log.debug("Force Checked in "+summary.getContentId() + " for user "+ checkedOutTo);
         }
         }
         catch (PSORMException | PSSystemValidationException e)
         {
            ms_log.error("Failed to force checkin users ",e);
         }

         
      }
         
   }
   
   public void changeWorfklowForItems(List<Integer> folderIds, int workflowId, List<String> validStateNames)
         throws PSCmsException, PSORMException
   {
      Validate.notNull(folderIds);
      Validate.notNull(validStateNames);
      
      PSRequest userReq = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      String roleName = validateWorkflowAdmin(workflowId, userReq.getUserSession().getUserRoles());
      
      // build map of state id to list of items to move to that state
      Map<Integer, List<IPSItemEntry>> entriesByTargetState = new HashMap<>();
      
      // get map of state names to ids
      Map<Integer, String> wfStateIdNameMap = getStateIdNameMap(workflowId,
            new HashMap<>());
      Map<String, Integer> wfStateNameIdMap = MapUtils.invertMap(wfStateIdNameMap);
      
      PSServerFolderProcessor proc = PSServerFolderProcessor.getInstance();
      
      int itemCount = 0;
      int folderCount = folderIds.size();
      PSNotificationHelper.notifyEvent(EventType.WORKFLOW_FOLDER_ASSIGNMENT_QUEUEING, folderCount);
      
      try
      {
         for (Integer folderId : folderIds)
         {
            PSLocator loc = new PSLocator(folderId);
            List<Integer> contentIds = new ArrayList<>(proc.getChildIds(loc, false));
            
            Set<Integer> checkedOutContent = findCheckedOutContentIds(contentIds);
            contentIds.removeAll(checkedOutContent);
            
            // add items to correct list in target state map
            List<IPSItemEntry> itemEntries = findItemEntries(contentIds, null);
            for (IPSItemEntry itemEntry : itemEntries)
            {
               if (itemEntry.isFolder())
                  continue;
               
               int itemWf = itemEntry.getWorkflowAppId();
               String itemState = itemEntry.getStateName();
               
               // skip if already in the correct workflow
               if (itemWf == workflowId)
                  continue;
               
               // If not a valid state, change to default
               if (!validStateNames.contains(itemState))
                  itemState = validStateNames.get(0);
               
               Integer targetStateId = wfStateNameIdMap.get(itemState);
               if (targetStateId == null)
                  throw new RuntimeException(
                        "State name " + itemState + " not found in workflow with id: " + workflowId);
               
               List<IPSItemEntry> stateEntries = entriesByTargetState.get(targetStateId);
               if (stateEntries == null)
               {
                  stateEntries = new ArrayList<>();
                  entriesByTargetState.put(targetStateId, stateEntries);
               }
               
               stateEntries.add(itemEntry);
               itemCount++;
            }
            
            folderCount--;
            PSNotificationHelper.notifyEvent(EventType.WORKFLOW_FOLDER_ASSIGNMENT_QUEUEING, new Integer(-1));
         }
      }
      finally
      {
         if (folderCount > 0)
         {
            PSNotificationHelper.notifyEvent(EventType.WORKFLOW_FOLDER_ASSIGNMENT_QUEUEING,
                  new Integer(folderCount * -1));
         }
      }
      
      PSNotificationHelper.notifyEvent(EventType.WORKFLOW_FOLDER_ASSIGNMENT_PROCESSING, new Integer(itemCount));
      ms_itemCount.set(itemCount);
      
      // now perform an update for each state
      try
      {
         for (Integer stateId : entriesByTargetState.keySet())
         {
            updateWorkflowAndState(workflowId, stateId, entriesByTargetState.get(stateId), roleName, userReq);
         }
      }
      finally
      {
         int remaining = ms_itemCount.get();
         if (remaining > 0)
         {
            PSNotificationHelper.notifyEvent(EventType.WORKFLOW_FOLDER_ASSIGNMENT_PROCESSING,
                  new Integer(itemCount * -1));
         }
      }
      
   }

   /**
    * Validate the current user is in the workflow admin role
    * 
    * @param workflowId The id of the workflow to check, assumed valid.
    * @param roles List of the user's roles, assumed not <code>null</code>.
    * 
    * @return The Admin role name.
    * 
    * @throws RuntimeException if the user is not an admin.
    */
   private String validateWorkflowAdmin(int workflowId, List<String> roles)
   {
      PSWorkflow workflow = loadWorkflow(workflowId);
      if (workflow == null)
         throw new RuntimeException("No workflow found for id: " + workflowId);
      
      String adminRole = workflow.getAdministratorRole();
      if (!roles.contains(adminRole))
         throw new RuntimeException("User must be a member of the workflow admin role: " + adminRole);
      
      return adminRole;
   }

   /**
    * For the given list of content ids, return a set of those that are
    * currently checked out.
    * 
    * @param ids The list to check, assumed not <code>null</code>.
    * @return The set, never <code>null</code>, may be empty.
    * 
    * @throws PSORMException If there is an error querying the repository.
    */
   private Set<Integer> findCheckedOutContentIds(List<Integer> ids) throws PSORMException
   {
      if (ids.size() == 0)
      {
         return Collections.EMPTY_SET;
      }
      
      Session session = sessionFactory.getCurrentSession();

         Set<Long> rval = new HashSet<>();
         Set<Integer> results;
         // Grab max ids at a time from the list, if less than max do the
         // entire list
         results = new HashSet<>();
         for (int i = 0; i < ids.size(); i += MAX_IDS)
         {
            int end = i + MAX_IDS;
            if (end > ids.size())
            {
               end = ids.size();
            }
            Criteria c = session.createCriteria(PSComponentSummary.class);
            c.add(Restrictions.in("m_contentId", ids.subList(i, end)));
            c.add(Restrictions.ne("m_checkoutUserName", ""));
            c.setProjection(Projections.property("m_contentId"));
            results.addAll(c.list());
         }
         
         return results;

      }

   /**
    * Update the workflow and state ids in the CONTENTSTATUS tablef or the
    * supplied items, also add a CONTENTSTATUSHISTORY entry.
    * 
    * @param workflowId The workflow id to use for the update
    * @param stateId The state id to use for the update
    * @param itemEntries The list of items to update, assumed not
    *           <code>null</code>.
    * @param roleName The role name the user is acting under, assumed not
    *           <code>null</code> or empty, used for the history entry.
    * @param req The current request, used to get the user's name and session id
    *           for the history entry.
    */
   private void updateWorkflowAndState(int workflowId, Integer stateId, List<IPSItemEntry> itemEntries, String roleName,
         PSRequest req)
   {
      Collection<Integer> ids = new ArrayList<>();
      List<PSContentStatusHistory> histories = new ArrayList<>();
      
      Date now = new Date();
      
      IPSStatesContext state = loadWorkflowState(workflowId, stateId);
      
      String sessid = req.getUserSessionId();
      String user = req.getUserSession().getRealAuthenticatedUserEntry();
      for (IPSItemEntry entry : itemEntries)
      {
         ids.add(entry.getContentId());
         
         PSContentStatusHistory hist = new PSContentStatusHistory();
         hist.setId(-1L);
         hist.setActor(user);
         hist.setContentId(entry.getContentId());
         hist.setEventTime(now);
         hist.setIsValidValue(state.getIsValid() ? "Y" : "N");
         hist.setLastModifiedDate(now);
         hist.setLastModifierName(user);
         hist.setRevision(entry.getTipRevision());
         hist.setRoleName(roleName);
         hist.setSessionId(sessid);
         hist.setStateId(stateId);
         hist.setStateName(state.getStateName());
         hist.setTitle(entry.getName());
         hist.setTransitionId(0);
         hist.setWorkflowId(workflowId);
         hist.setTransitionLabel("Change Workflow");
         
         histories.add(hist);
      }
      
      Session s = sessionFactory.getCurrentSession();

         int count=0;
         for (Integer id : ids)
         {
            // will get from second level cache
            PSComponentSummary summary = (PSComponentSummary)s.get(PSComponentSummary.class, id);
         
            summary.setWorkflowAppId(workflowId);
            summary.setContentStateId(stateId);
            
            if (++count % BATCH_SIZE == 0)
            {
               //flush a batch of updates and release memory:
               s.flush();
               s.clear();
            }
            
         }

      
      // update the item cache
      PSItemSummaryCache cache = PSItemSummaryCache.getInstance();
      if (cache != null)
      {
         cache.updateWorkflowAndState(ids, workflowId, stateId);
      }
      
      // add content status history entries
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();
      for (PSContentStatusHistory hist : histories)
      {
         svc.saveContentStatusHistory(hist);
      }

      // reindex the items
      for (Integer id : ids)
      {
         PSSearchIndexEventQueue.getInstance().indexItem(new PSLocator(id));
         int itemCount = ms_itemCount.get();
         if (itemCount > 0)
         {
            PSNotificationHelper.notifyEvent(EventType.WORKFLOW_FOLDER_ASSIGNMENT_PROCESSING, new Integer(-1));
            itemCount--;    
            ms_itemCount.set(itemCount);
         }
      }
   }

   public Integer getItemCount(String rootFolderPath, List<String> stateNames, String contentTypeName)
         throws PSCmsException, PSInvalidContentTypeException
   {
      // Verify required parameters are not null
      notNull(rootFolderPath);
      notNull(stateNames);
      notNull(contentTypeName);

      // Initialization
      Integer itemCount = 0;
      PSServerFolderProcessor folderProcessor = PSServerFolderProcessor.getInstance();
      PSItemSummaryCache cache = PSItemSummaryCache.getInstance();
      PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();

      // Get the id of the root folder
      int rootFolderId = folderProcessor.getIdByPath(rootFolderPath);

      // Get the item ids of all the children of rootFolder
      Set<Integer> itemIds = folderProcessor.getChildIds(new PSLocator(rootFolderId), true);

      // Get all the children of the root folder from ItemSummary cache.
      List<IPSItemEntry> itemEntries = cache.getItems(new ArrayList<>(itemIds));

      // For each child, check if it is of type contentTypeName and if its state
      // matches one of stateNames
      long specifiedContentTypeId = itemDefMgr.contentTypeNameToId(contentTypeName);
      for (IPSItemEntry itemEntry : itemEntries)
      {
         if (itemEntry.getContentTypeId() == specifiedContentTypeId && stateNames.contains(itemEntry.getStateName()))
         {
            // If the child matches the criteria, count it.
            itemCount++;
         }
      }

      return itemCount;
   }
   
   public void setRevisionLocks(List<Integer> ids)
   {
      Session session = sessionFactory.getCurrentSession();


         List<PSComponentSummary> summaries = loadComponentSummaries(ids);
         for (PSComponentSummary summary : summaries)
         {
            session.update(summary);
         }

      }

   // see IPSHandlerInitListener interface
   public void initHandler(IPSRequestHandler requestHandler)
   {
      if (requestHandler instanceof PSContentEditorHandler)
      {
         PSContentEditorHandler ceh = (PSContentEditorHandler) requestHandler;
         PSCommandHandler workflowCommandHandler = ceh.getCommandHandler(PSWorkflowCommandHandler.COMMAND_NAME);
         workflowHandlers.put(ceh.getContentTypeId(),workflowCommandHandler);
      
      }
   }
   
   @Override
   public void shutdownHandler(IPSRequestHandler requestHandler)
   {
      if (requestHandler instanceof PSContentEditorHandler)
      {
         PSContentEditorHandler ceh = (PSContentEditorHandler) requestHandler;
         workflowHandlers.remove(ceh.getContentTypeId());
      }
      
   }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSummaryDateFieldBatch(String fieldName, Date dateToSet, List<Integer> ids, boolean updateExisting)
    {
        Session s = sessionFactory.getCurrentSession();

            String queryToUse = String.format(UPDATE_DATE_HQL, fieldName);
            if (!updateExisting)
                queryToUse+=String.format(WHERE_NULL, fieldName);

            Query query = s.createQuery(queryToUse).setParameter("dateToSet", dateToSet)
                    .setParameterList("ids", ids);
            int result = query.executeUpdate();
        PSItemSummaryCache cache = PSItemSummaryCache.getInstance();
        if ((cache != null && dateToSet != null) && "m_contentPostDate".equals(fieldName))
            cache.updatePostDate(ids,dateToSet);
                ms_log.debug(String.format("Updating %s with %d ids, result updated %d rows", fieldName, ids.size(), result));

    }

    public void updateSummaryDate(String fieldName, Date dateToSet, Collection<Integer> ids, boolean updateExisting)
    {
        synchronized (DATE_UPDATE_SYNC_OBJECT)
        {
            // we have to call methods in this class with proxy to respect
            // transactional annotations.
            IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
            List<Integer> idBatch = new ArrayList<>();
            for (int id : ids)
            {
                idBatch.add(id);
                if (idBatch.size() == BATCH_SIZE)
                {
                    objMgr.updateSummaryDateFieldBatch(fieldName, dateToSet, idBatch, updateExisting);
                    idBatch.clear();
                }
            }
            if (idBatch.size() > 0)
                objMgr.updateSummaryDateFieldBatch(fieldName, dateToSet, idBatch, updateExisting);
        }
    }

       
}
