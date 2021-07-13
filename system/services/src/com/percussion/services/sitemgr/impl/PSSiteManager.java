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
package com.percussion.services.sitemgr.impl;

import com.percussion.auditlog.PSActionOutcome;
import com.percussion.auditlog.PSAuditLogService;
import com.percussion.auditlog.PSContentEvent;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.IPSCatalogErrors;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSCatalogException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationHelper;
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.IPSSiteManagerErrors;
import com.percussion.services.sitemgr.IPSSiteManagerInternal;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.services.sitemgr.data.PSLocationScheme;
import com.percussion.services.sitemgr.data.PSPublishingContext;
import com.percussion.services.sitemgr.data.PSSite;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.types.PSPair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The site manager deals with the site and related objects. It makes available
 * CRUD methods to manipulate these objects
 * 
 * @author dougrand
 * 
 */
@Transactional (noRollbackFor=PSNotFoundException.class)
public class PSSiteManager
      implements
      IPSSiteManagerInternal
{

   private SessionFactory sessionFactory;
    private PSAuditLogService psAuditLogService=PSAuditLogService.getInstance();
    private PSContentEvent psContentEvent;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }


   /**
    * Listener which invalidates locally cached information
    */
   final class PSSiteNotificationListener implements IPSNotificationListener
   {
      public void notifyEvent(PSNotificationEvent notification)
      {
         IPSGuid guid = (IPSGuid) notification.getTarget();
         short type = guid.getType();
         if (type == PSTypeEnum.LOCATION_SCHEME.getOrdinal()
               || type == PSTypeEnum.LOCATION_PROPERTY.getOrdinal())
         {
             m_cache.evict(LOCATION_MAP_KEY, IPSCacheAccess.IN_MEMORY_STORE);
         }
      }
   }

   /**
    * Key for location scheme map
    */
   static class LocationSchemeKey implements Serializable
   {
      /**
       * Serial id identifies versions of serialized data
       */
      private static final long serialVersionUID = 1L;
      
      /**
       * Holds the template id, initialized in the ctor
       */
      private IPSGuid mi_templateid;

      /**
       * Holds the context, initialized in the ctor
       */
      private IPSGuid mi_contextid;

      /**
       * Holds the content type id, initialized in the ctor
       */
      private IPSGuid mi_contenttypeid;

      /**
       * Ctor
       * 
       * @param tid template id, assumed never <code>null</code>
       * @param contextid context id, assumed never <code>null</code>
       * @param ctid content type id, assumed never <code>null</code>
       */
      public LocationSchemeKey(IPSGuid tid, IPSGuid contextid,
            IPSGuid ctid) {
         mi_templateid = tid;
         mi_contextid = contextid;
         mi_contenttypeid = ctid;
      }

      /*
       * (non-Javadoc)
       * 
       * @see java.lang.Object#equals(java.lang.Object)
       */
      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof LocationSchemeKey)
         {
            LocationSchemeKey lsk = (LocationSchemeKey) obj;
            return lsk.mi_contenttypeid.equals(mi_contenttypeid)
                  && lsk.mi_templateid.equals(mi_templateid)
                  && lsk.mi_contextid.equals(mi_contextid);
         }
         return false;
      }

      /*
       * (non-Javadoc)
       * 
       * @see java.lang.Object#hashCode()
       */
      @Override
      public int hashCode()
      {
         return mi_contenttypeid.hashCode() + mi_templateid.hashCode()
               + mi_contextid.hashCode();
      }
   }

   /**
    * Logger for the site manager
    */
   private static final Logger log = LogManager.getLogger("PSSiteManager");

   /**
    * Cache service, used to invalidate site information
    */
   IPSCacheAccess m_cache = null;

   /**
    * Notification service, used to register a listener for invalidation
    */
   IPSNotificationService m_notifications = null;

   /**
    * Key to lookup the location map in the cache
    */
   static final String LOCATION_MAP_KEY = "sys_location_map";


   
   /**
    * Default constructor.
    */
   public PSSiteManager() 
   {
   }

   /**
    * @see com.percussion.services.sitemgr.IPSSiteManager#createSite()
    */
   public IPSSite createSite()
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      PSSite newsite = new PSSite();
      newsite.setSiteId(gmgr.createGuid(PSTypeEnum.SITE).longValue());
       newsite.setMobilePreviewEnabled(true);
       try {
           psContentEvent = new PSContentEvent(newsite.getSiteId().toString(), newsite.getSiteId().toString(), newsite.getBaseUrl(), PSContentEvent.ContentEventActions.create, PSSecurityFilter.getCurrentRequest().getServletRequest(), PSActionOutcome.SUCCESS);
           psAuditLogService.logContentEvent(psContentEvent);
       }catch (Exception e){
           //Handling exception if loggin not working
       }
      return newsite;
   }

   /*
    * @see com.percussion.services.sitemgr.IPSSiteManager#loadSitesModifiable()    
    */
   public List<IPSSite> loadSitesModifiable()
   {
      return (List<IPSSite>) sessionFactory.getCurrentSession().createCriteria(PSSite.class).setCacheable(true).list();
   }

   
   /**
    * @see com.percussion.services.sitemgr.IPSSiteManager#loadSiteModifiable(com.percussion.utils.guid.IPSGuid)
    */
   public IPSSite loadSiteModifiable(IPSGuid siteid) throws PSNotFoundException
   {
      IPSSite rval = findSiteFromDatabase(siteid);
      
      if (rval == null)
         throw new PSNotFoundException(siteid);
      
      if (log.isDebugEnabled())
      {
         log.debug("Load un-cached site (id=" + siteid.toString()
               + ", name=\"" + rval.getName() + "\".");
      }

      return rval;
   }

   public IPSSite loadSiteModifiable(String siteName) throws PSNotFoundException {
      IPSSite site = findSite(siteName);
      if (site==null)
         throw new PSNotFoundException(siteName, PSTypeEnum.SITE);
      return site;
   }
   
   /**
    * Look up the specified site from the database.
    * 
    * @param siteid the ID of the site, assumed not <code>null</code>.
    * 
    * @return the specified site, it may be <code>null</code> if the site
    * does not exist.
    */
   public IPSSite findSiteFromDatabase(IPSGuid siteid)
   {
      return (IPSSite) sessionFactory.getCurrentSession().get(PSSite.class,
            siteid.longValue());
   }
   
   public IPSSite loadUnmodifiableSite(IPSGuid siteid)
         throws PSNotFoundException
   {
      return loadSite(siteid);
   }
   
   public IPSSite findSite(IPSGuid siteid)
   {
      if (siteid == null)
      {
         throw new IllegalArgumentException("siteid may not be null");
      }

      IPSSite rval = findSiteFromDatabase(siteid);



      if (log.isDebugEnabled())
      {
         log.debug("Load cached site (id=" + siteid.toString()
               + ", name=\"" + rval.getName() + "\".");
      }


      return rval;
   }

   public IPSSite loadSite(IPSGuid siteid) throws PSNotFoundException
   {
      if (siteid == null)
      {
         throw new IllegalArgumentException("siteid may not be null");
      }
      
      IPSSite site = findSite(siteid);
      if (site == null)
         throw new PSNotFoundException(siteid);

      if (log.isDebugEnabled())
      {
         log.debug("Load cached site (id=" + siteid.toString()
               + ", name=\"" + site.getName() + "\".");
      }

      return site;
   }
   
   /**
    * @see com.percussion.services.sitemgr.IPSSiteManager#findAllSites()
    */
   @SuppressWarnings("unchecked")
   public List<IPSSite> findAllSites()
   {
      return loadSitesModifiable();
   }

   
   /**
    * Gets all site IDs directly from the repository.
    *
    * @return all site IDs, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   public synchronized Map<IPSGuid, String> getAllSiteIdNames()
   {
      Map<IPSGuid, String> idNameMap = new HashMap<>();
      
      Session s = sessionFactory.getCurrentSession();

         Criteria c = s.createCriteria(PSSite.class);
         c.setProjection(Projections.projectionList().add(
               Projections.property("siteId"))
               .add(Projections.property("name"))).setCacheable(true);
         List<Object> results = c.list();
         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
         for (Object value : results)
         {
            Object[] values = (Object[]) value;
            Long id = (Long) values[0];
            String name = (String) values[1];
            idNameMap.put(gmgr.makeGuid(id, PSTypeEnum.SITE), name);
         }

         return idNameMap;

   }

   @SuppressWarnings("unchecked")
   public IPSSite findSite(String sitename)
   {
      if (StringUtils.isBlank(sitename))
         throw new IllegalArgumentException(
               "sitename may not be null or empty.");


      return (IPSSite) sessionFactory.getCurrentSession()
              .bySimpleNaturalId(PSSite.class)
              .load(sitename);
   }

   public IPSSite loadSite(String sitename) throws PSNotFoundException
   {
      IPSSite site = findSite(sitename);
      if (site != null)
         return site;
      
      throw new PSNotFoundException(sitename, PSTypeEnum.SITE);
   }

   /**
    * @see com.percussion.services.sitemgr.IPSSiteManager#findSiteByName(java.lang.String)
    * @deprecated use {@link #loadSite(String)} instead.
    */
   @SuppressWarnings("unchecked")
   public IPSSite findSiteByName(String sitename) throws PSSiteManagerException
   {
      try
      {
         return loadSite(sitename);
      }
      catch (PSNotFoundException e)
      {
         throw new PSSiteManagerException(
               IPSSiteManagerErrors.SITE_NAME_NOT_EXIST, sitename);
      }
   }

   /**
    * @see com.percussion.services.sitemgr.IPSSiteManager#saveSite(com.percussion.services.sitemgr.IPSSite)
    */
   public void saveSite(IPSSite site)
   {
      if (site == null)
         throw new IllegalArgumentException("site must not be null.");

      sessionFactory.getCurrentSession().merge(site);

   }

   /**
    * @see com.percussion.services.sitemgr.IPSSiteManager#deleteSite(com.percussion.services.sitemgr.IPSSite)
    */
   public void deleteSite(IPSSite site)
   {
      if (site == null)
         throw new IllegalArgumentException("site must not be null.");

      sessionFactory.getCurrentSession().delete(site);
      
      PSNotificationHelper.notifyEvent(EventType.SITE_DELETED, site.getGUID());
      
      // the object will be evicted by the framework, 
      // see PSEhCacheAccessor.notifyEvent()
   }

   /**
    * @see com.percussion.services.sitemgr.IPSSiteManager#createScheme()
    */
   public IPSLocationScheme createScheme()
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      PSLocationScheme scheme = new PSLocationScheme();
      scheme.setGUID(gmgr.createGuid(PSTypeEnum.LOCATION_SCHEME));
      return scheme;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSiteManager#loadScheme(int)
    */
   public IPSLocationScheme loadScheme(IPSGuid schemeId)
      throws PSNotFoundException
   {
      if (schemeId == null)
         throw new IllegalArgumentException("schemeId may not be null.");
      
      IPSLocationScheme scheme = loadSchemeModifiable(schemeId);
      return scheme;
   }

   public IPSLocationScheme loadSchemeModifiable(IPSGuid schemeId)
      throws PSNotFoundException
   {
      if (schemeId == null)
         throw new IllegalArgumentException("schemeId may not be null.");

      IPSLocationScheme rval = (IPSLocationScheme) sessionFactory.getCurrentSession().get(
            PSLocationScheme.class, schemeId.longValue());
      if (rval == null)
      {
         throw new PSNotFoundException(schemeId);
      }

      return rval;
   }

   /*
    * //see base class method for details
    */
   public IPSLocationScheme loadScheme(int schemeId)
      throws PSNotFoundException
   {
      IPSGuid id = PSGuidUtils.makeGuid(schemeId, PSTypeEnum.LOCATION_SCHEME);
      return loadScheme(id);
   }

   /*
    * //see base class method for details
    */
   @SuppressWarnings("unchecked")
   public List<IPSLocationScheme> findSchemeByAssemblyInfo(
         IPSAssemblyTemplate template, IPSPublishingContext context,
         IPSGuid contenttypeid)
   {
      // Delegate
      return findSchemeByAssemblyInfo(template.getGUID(), context.getGUID(),
            contenttypeid);
   }
   
   /*
    * //see base class method for details
    */
   public List<IPSLocationScheme> findSchemeByAssemblyInfo(IPSGuid templateid,
         IPSPublishingContext context, IPSGuid contenttypeid)
   {
      return findSchemeByAssemblyInfo(templateid, context.getGUID(),
            contenttypeid);
   }

   
   /*
    * //see base class method for details
    */
   @SuppressWarnings("unchecked")
   public List<IPSLocationScheme> findSchemeByAssemblyInfo(IPSGuid templateid,
         IPSGuid contextid, IPSGuid contenttypeid)
   {
      if (templateid == null)
      {
         throw new IllegalArgumentException("templateid may not be null");
      }
      if (contextid == null)
      {
         throw new IllegalArgumentException("contextid may not be null");
      }
      if (contenttypeid == null)
      {
         throw new IllegalArgumentException("contenttypeid may not be null");
      }
      LocationSchemeKey key = new LocationSchemeKey(templateid, contextid,
            contenttypeid);
      ConcurrentHashMap<LocationSchemeKey, List<IPSLocationScheme>> locationSchemeMap = getLocationSchemeMap();
      
      List<IPSLocationScheme> rval = locationSchemeMap.get(key);
      // prevent lock on read with double checked locking
      if (rval == null)
      {
         synchronized (locationSchemeMap)
         {
            rval = locationSchemeMap.get(key);
            if (rval==null)
            {   
               Session s = sessionFactory.getCurrentSession();

                  Criteria c = s.createCriteria(PSLocationScheme.class);
                  c.add(Restrictions.eq("templateId", templateid.longValue()));
                  c.add(Restrictions
                        .eq("contentTypeId", contenttypeid.longValue()));
                  c.add(Restrictions.eq("contextId", contextid.longValue()));
                  rval = c.list();
                  locationSchemeMap.put(key, rval);

            }
         }
      }
      return rval;
  
   }

   /**
    * Get the location scheme map from the cache, create if missing
    * 
    * @return the map, never <code>null</code>
    */
   @SuppressWarnings("unchecked") //cast from cache
   ConcurrentHashMap<LocationSchemeKey, List<IPSLocationScheme>> getLocationSchemeMap()
   {
      ConcurrentHashMap<LocationSchemeKey, List<IPSLocationScheme>> locationSchemeMap = 
         (ConcurrentHashMap<LocationSchemeKey, List<IPSLocationScheme>>) m_cache
            .get(LOCATION_MAP_KEY, IPSCacheAccess.IN_MEMORY_STORE);
      if (locationSchemeMap == null)
      {
         locationSchemeMap = new ConcurrentHashMap<>(8, 0.9f, 1);
         m_cache.save(LOCATION_MAP_KEY, locationSchemeMap,
               IPSCacheAccess.IN_MEMORY_STORE);
      }
      return locationSchemeMap;
   }


   /**
    * @see com.percussion.services.sitemgr.IPSSiteManager#saveScheme(com.percussion.services.sitemgr.IPSLocationScheme)
    */
   public void saveScheme(IPSLocationScheme scheme)
   {
      // cannot save a cloned Location Scheme object; otherwise the child
      // component of this object (parameters) will not be saved as expected.
      if (scheme instanceof PSLocationScheme)
      {
         if (((PSLocationScheme) scheme).isCloned())
            throw new IllegalStateException(
                  "Cannot save a cloned Location Scheme object.");
      }
      sessionFactory.getCurrentSession().saveOrUpdate(scheme);
      
      // the object will be evicted by the framework, 
      // see PSEhCacheAccessor.notifyEvent()
   }

   /**
    * @see com.percussion.services.sitemgr.IPSSiteManager#deleteScheme(com.percussion.services.sitemgr.IPSLocationScheme)
    */
   public void deleteScheme(IPSLocationScheme scheme)
   {
      sessionFactory.getCurrentSession().delete(scheme);
      
      // the object will be evicted by the framework, 
      // see PSEhCacheAccessor.notifyEvent()
   }

   //see interface
   public IPSPublishingContext loadContext(int contextid) 
      throws PSNotFoundException
   {
      return loadContext(PSGuidUtils.makeGuid(contextid, PSTypeEnum.CONTEXT)); 
   }
   
   /*
    * @see com.percussion.services.sitemgr.IPSSiteManager#loadContext(int)
    */
   public IPSPublishingContext loadContext(IPSGuid contextid) 
      throws PSNotFoundException
   {
      return loadContext(contextid, true);
   }

   public IPSPublishingContext loadContextModifiable(IPSGuid contextid)
      throws PSNotFoundException
   {
      return loadContext(contextid, false);
   }
   
   /**
    * This does the same as {@link #loadContext(IPSGuid)}. In addition, this
    * method give an option for the caller to specify whether the returned
    * object includes the child object or not.
    * 
    * @param contextid the Context ID, assumed not <code>null</code>.
    * @param includeChildren <code>true</code> if returned object may include
    *    child object; otherwise the returned object may only include the ID
    *    of the child object.
    *    
    * @return the specified context, never <code>null</code>.
    * 
    * @throws PSNotFoundException if cannot find the context.
    */
   private IPSPublishingContext loadContext(IPSGuid contextid,
         boolean includeChildren) throws PSNotFoundException
   {
      IPSPublishingContext ctx = (IPSPublishingContext) sessionFactory.getCurrentSession()
            .get(PSPublishingContext.class, contextid.longValue());
      if (ctx == null)
      {
         throw new PSNotFoundException(contextid);
      }
      if (includeChildren)
         loadDefaultSchemeIfNeeded(ctx);
      return ctx;
   }

   /**
    * Loads the child component, Default Location Scheme, for the specified 
    * Context if it has one.
    * @param ctx the Context object in question, assumed not <code>null</code>.
    */
   private void loadDefaultSchemeIfNeeded(IPSPublishingContext ctx) throws PSNotFoundException {
      if (ctx.getDefaultSchemeId() == null)
         return;

      // @TODO change this to loadScheme after cache the parent
      IPSLocationScheme scheme = loadSchemeModifiable(ctx
            .getDefaultSchemeId());
      ((PSPublishingContext)ctx).setDefaultScheme(scheme);
   }
   
   @SuppressWarnings("unchecked")
   public IPSPublishingContext loadContext(String contextname)
      throws PSNotFoundException
   {
      if (StringUtils.isBlank(contextname))
         throw new IllegalArgumentException(
               "contextname may not be null or empty");

      List contexts = sessionFactory.getCurrentSession().createQuery(
            "from PSPublishingContext where name = :name").setParameter("name",contextname).list();
      if (contexts.size() < 1)
      {
         throw new PSNotFoundException(contextname, PSTypeEnum.CONTEXT);
      }
      IPSPublishingContext ctx = (IPSPublishingContext) contexts.get(0);
      loadDefaultSchemeIfNeeded(ctx);
      return ctx;
      
   }
   /**
    * @deprecated use {@link #loadContext(String)} instead.
    */
   @SuppressWarnings("unchecked")
   public IPSPublishingContext findContextByName(String contextname) 
      throws PSSiteManagerException
   {
      try
      {
         return loadContext(contextname);
      }
      catch (PSNotFoundException e)
      {
         throw new PSSiteManagerException(IPSSiteManagerErrors.NO_SUCH_CONTEXT, 
               "NAME", contextname);
      }
   }

   /**
    * @see com.percussion.services.catalog.IPSCataloger#getTypes()
    */
   public PSTypeEnum[] getTypes()
   {
      throw new UnsupportedOperationException("not implemented yet");
   }

   /**
    * @see com.percussion.services.catalog.IPSCataloger#getSummaries(com.percussion.services.catalog.PSTypeEnum)
    */
   @SuppressWarnings("unchecked")
   public List<IPSCatalogSummary> getSummaries(PSTypeEnum type) throws PSNotFoundException {
      List<IPSCatalogSummary> rval = new ArrayList<>();

      Session s = sessionFactory.getCurrentSession();

         if (type.getOrdinal() == PSTypeEnum.SITE.getOrdinal())
         {
            List<IPSSite> sites = findAllSites();

            for (IPSSite f : sites)
            {
               rval.add(new PSObjectSummary(f.getGUID(), f.getName(), f
                     .getName(), StringUtils.EMPTY));
            }
         }
         
         if (type.getOrdinal() == PSTypeEnum.CONTEXT.getOrdinal())
         {
            List<IPSPublishingContext> contexts = findAllContexts(false);

            for (IPSPublishingContext c : contexts)
            {
               rval.add(new PSObjectSummary(c.getGUID(), c.getName(), 
                     c.getName(), StringUtils.EMPTY));
            }
         }

      return rval;
   }

   /**
    * @see com.percussion.services.catalog.IPSCataloger#loadByType(com.percussion.services.catalog.PSTypeEnum,
    *      java.lang.String)
    */
   public void loadByType(PSTypeEnum type, String item)
         throws PSCatalogException
   {
      try
      {
         if (type.equals(PSTypeEnum.SITE))
         {
            IPSGuid guid = PSXmlSerializationHelper.getIdFromXml(
                  PSTypeEnum.SITE, item);
            IPSSite temp = null;
            try
            {
               temp = loadUnmodifiableSite(guid);
               ((PSSite) temp).setVersion(null);
            }
            catch (PSNotFoundException e)
            {
               temp = new PSSite();
            }
            ((PSSite) temp).fromXML(item);
            saveSite(temp);
         }
         else
         {
            throw new PSCatalogException(IPSCatalogErrors.UNKNOWN_TYPE, type
                  .toString());
         }
      }
      catch (IOException e)
      {
         throw new PSCatalogException(IPSCatalogErrors.IO, e, type);
      }
      catch (SAXException e)
      {
         throw new PSCatalogException(IPSCatalogErrors.XML, e, item);
      }

   }

   /**
    * @see com.percussion.services.catalog.IPSCataloger#saveByType(com.percussion.utils.guid.IPSGuid)
    */
   public String saveByType(IPSGuid id) throws PSCatalogException
   {
      try
      {
         if (id.getType() == PSTypeEnum.SITE.getOrdinal())
         {
            IPSSite temp = loadSite(id);
            return ((PSSite) temp).toXML();
         }
         PSTypeEnum type = PSTypeEnum.valueOf(id.getType());
         throw new PSCatalogException(IPSCatalogErrors.UNKNOWN_TYPE, type
               .toString());
      }
      catch (PSNotFoundException e)
      {
         throw new PSCatalogException(IPSCatalogErrors.REPOSITORY, e, id);
      }
      catch (IOException e)
      {
         throw new PSCatalogException(IPSCatalogErrors.IO, e, id);
      }
      catch (SAXException e)
      {
         throw new PSCatalogException(IPSCatalogErrors.TOXML, e);
      }
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.sitemgr.IPSSiteManager#getPublishPath(com.percussion.utils.guid.IPSGuid,
    *      com.percussion.utils.guid.IPSGuid)
    */
   public String getPublishPath(IPSGuid siteId, IPSGuid folderId)
           throws PSSiteManagerException, PSNotFoundException {
      if (siteId == null)
         throw new IllegalArgumentException("siteId must not be null.");
      if (!(folderId instanceof PSLegacyGuid))
         throw new IllegalArgumentException(
               "folderId must be an instance of PSLegacyGuid.");

      IPSSite site = loadUnmodifiableSite(siteId);

      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      PSServerFolderProcessor processor = PSServerFolderProcessor.getInstance();

      int rootId = getSiteRootFolderId(site, processor);
      if (rootId == -1)
         return null;
      
      // if the folder is the root folder of the site, return "/"
      if (rootId == ((PSLegacyGuid) folderId).getContentId())
         return PSFolder.PATH_SEP;

      List<PSLocator> siteFolderPath = getSiteFolderPath(
            (PSLegacyGuid) folderId, rootId, site, processor);

      // get the publishing path from 'siteFolderPath'
      StringBuilder pathBuff = new StringBuilder();
      String publishName;
      for (PSLocator f : siteFolderPath)
      {
         try
         {
            publishName = processor.getPubFileName(f.getId());
         }
         catch (PSCmsException e)
         {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            // this should never happen in a properly configured environment
            throw new PSSiteManagerException(
                  IPSSiteManagerErrors.UNEXPECTED_ERROR, e
                        .getLocalizedMessage());
         }

         pathBuff.append(PSFolder.PATH_SEP);
         pathBuff.append(publishName);
      }
      pathBuff.append(PSFolder.PATH_SEP);

      return pathBuff.toString();
   }

   /**
    * Gets the root folder id for the specified site.
    * 
    * @param site the site for which to find the root folder id, assumed not
    *           <code>null</code>.
    * @param processor the folder processor object, assumed not
    *           <code>null</code>.
    * 
    * @return the root folder id of the specified site. 
    * It may be <code>-1</code> if the cannot find root folder of the site, or
    * the root folder of the site is not defined.
    * 
    * @throws PSSiteManagerException if an error occurs.
    */
   private int getSiteRootFolderId(IPSSite site,
         PSServerFolderProcessor processor) throws PSSiteManagerException
   {
      try
      {
         return processor.getIdByPath(site.getFolderRoot());
      }
      catch (PSCmsException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         throw new PSSiteManagerException(
               IPSSiteManagerErrors.FAILED_FIND_ROOT_FOLDER_ID, site.getGUID(),
               site.getFolderRoot(), e.getLocalizedMessage());
      }
   }

   /**
    * Gets the site folder path for the specified folder and site.
    * 
    * @param folderId the specified folder id, assumed not <code>null</code>.
    * @param siteRootId the root folder id of the specified site.
    * @param site the specified site, assumed not <code>null</code>.
    * @param processor the folder processor, assumed not <code>null</code>.
    * 
    * @return a list of locators, where the 1st element is the immediate child
    *         folder of the specified site, and the last element is the
    *         specified folder. Never <code>null</code> or empty.
    * 
    * @throws PSSiteManagerException if the specified folder does not exist
    *            under the specified site.
    */
   private List<PSLocator> getSiteFolderPath(PSLegacyGuid folderId,
         int siteRootId, IPSSite site, PSServerFolderProcessor processor)
         throws PSSiteManagerException
   {
      List<PSLocator> pathToRoot;
      PSLocator folderLocator = folderId.getLocator();

      try
      {
         pathToRoot = processor.getAncestorLocators(folderLocator);
         pathToRoot.add(folderLocator);
      }
      catch (PSCmsException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         throw new PSSiteManagerException(
               IPSSiteManagerErrors.FAILED_GET_FOLDER_PATH, folderId, e
                     .getLocalizedMessage());
      }

      // get the locator path from the specified folder to the site's root
      // folder
      List<PSLocator> siteFolderPath = new ArrayList<>(pathToRoot
            .size());
      boolean foundRoot = false;
      for (int i = 0; i < pathToRoot.size(); i++)
      {
         if (foundRoot)
            siteFolderPath.add(pathToRoot.get(i));
         else if (pathToRoot.get(i).getId() == siteRootId)
            foundRoot = true;
      }

      if (siteFolderPath.isEmpty())
         throw new PSSiteManagerException(IPSSiteManagerErrors.NOT_SITE_FOLDER,
               folderId.getContentId(), site.getGUID(), site.getFolderRoot());

      return siteFolderPath;
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.sitemgr.IPSSiteManager#getSiteFolderId(com.percussion.utils.guid.IPSGuid,
    *      com.percussion.utils.guid.IPSGuid)
    */
   public IPSGuid getSiteFolderId(IPSGuid siteId, IPSGuid contentId)
           throws PSSiteManagerException, PSNotFoundException {
      if (siteId == null)
      {
         throw new IllegalArgumentException("siteId may not be null");
      }
      if (contentId == null)
      {
         throw new IllegalArgumentException("contentId may not be null");
      }
      if (!(contentId instanceof PSLegacyGuid))
      {
         throw new IllegalArgumentException("contentId must be a legacy guid");
      }

      PSLegacyGuid lg = (PSLegacyGuid) contentId;
      IPSSite site = loadUnmodifiableSite(siteId);

      PSRequest request = PSRequest.getContextForRequest();
      PSServerFolderProcessor fproc =PSServerFolderProcessor.getInstance();
      try
      {
         String paths[] = fproc.getFolderPaths(lg.getLocator());
         String matching = null;
         String siteRoot = site.getFolderRoot();
         if(!siteRoot.endsWith("/"))
            siteRoot = siteRoot + "/";
         for (String path : paths)
         {
            if (siteRoot.equals(path + "/") || path.startsWith(siteRoot))
            {
               matching = path;
               break;
            }
         }
         if (matching == null)
         {
            return null;
         }

         int cid = fproc.getIdByPath(matching);
         return new PSLegacyGuid(cid, -1);
      }
      catch (PSCmsException e)
      {
         throw new PSSiteManagerException(
               IPSSiteManagerErrors.UNEXPECTED_ERROR, e);
      }
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSiteManager#getItemSites(com.percussion.utils.guid.IPSGuid)
    */
   @SuppressWarnings("unchecked")
   public List<IPSSite> getItemSites(IPSGuid contentId)
   {
      if (contentId == null)
      {
         throw new IllegalArgumentException("contentId may not be null");
      }
      if (!(contentId instanceof PSLegacyGuid))
      {
         throw new IllegalArgumentException("contentId must be a legacy guid");
      }
      List<IPSSite> matchingSites = new ArrayList<>();
      PSLegacyGuid lg = (PSLegacyGuid) contentId;
      PSRequest request = PSRequest.getContextForRequest();
      PSServerFolderProcessor fproc = PSServerFolderProcessor.getInstance();
      try
      {
         List<IPSSite> allSites = findAllSites();
         String paths[] = fproc.getFolderPaths(lg.getLocator());
         for (String path : paths)
         {
            for (IPSSite site : allSites)
            {
               String siteRoot = site.getFolderRoot();
               if(siteRoot == null)
                  continue;
               if(path.equals(siteRoot) && !matchingSites.contains(site))
               {
                  matchingSites.add(site);
                  continue;
               }
               if(!siteRoot.endsWith("/"))
                  siteRoot = siteRoot + "/";
               if (path.startsWith(siteRoot) && !matchingSites.contains(site))
               {
                  matchingSites.add(site);
               }
            }
         }
      }
      catch (PSCmsException e)
      {
         String errMsg = "Failed to get sites for item id=" + contentId.toString();
         log.error(errMsg);
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         throw new RuntimeException(errMsg, e);
      }
      
      Collections.sort(matchingSites, new Comparator()
      {
         public int compare(Object obj1, Object obj2)
         {
            IPSSite temp1 = (IPSSite) obj1;
            IPSSite temp2 = (IPSSite) obj2;
            return temp1.getName().compareTo(temp2.getName());
         }
      });
      
      return matchingSites;
   }
   
   // implements method from IPSSiteManager interface
   public boolean isContentTypePublishableToSite(IPSGuid contentTypeId,
         IPSGuid siteId) throws PSSiteManagerException, PSNotFoundException {
      if (contentTypeId == null)
      {
         throw new IllegalArgumentException("contentTypeId must not be null");
      }
      // load templates for the supplied content type
      IPSAssemblyService aService = PSAssemblyServiceLocator
            .getAssemblyService();
      List<IPSAssemblyTemplate> contentTypeTemplates = null;
      try
      {
         contentTypeTemplates = aService
               .findTemplatesByContentType(contentTypeId);
      }
      catch (PSAssemblyException e)
      {
         throw new PSSiteManagerException(e.getErrorCode(), e
               .getLocalizedMessage());
      }
      // Normalize one or all sites into a list
      List<IPSSite> sites = null;
      if (siteId == null)
      {
         sites = findAllSites();
      }
      else
      {
         sites = new ArrayList<>();
         sites.add(loadUnmodifiableSite(siteId));
      }
      // get templates publishable to all the sites
      Set<IPSAssemblyTemplate> siteTemplates = new HashSet<>();
      for (int i = 0; i < sites.size(); i++)
      {
         IPSSite site = sites.get(i);
         siteTemplates.addAll(site.getAssociatedTemplates());
      }
      // Is there any intersection of these?
      return !CollectionUtils.intersection(contentTypeTemplates, siteTemplates)
            .isEmpty();
   }

   /**
    * Spring property accessor
    * 
    * @return get the cache service
    */
   public IPSCacheAccess getCache()
   {
      return m_cache;
   }

   /**
    * Set the cache service
    * 
    * @param cache the service, never <code>null</code>
    */
   public void setCache(IPSCacheAccess cache)
   {
      if (cache == null)
      {
         throw new IllegalArgumentException("cache may not be null");
      }
      m_cache = cache;
   }

   /**
    * Get the notification service set by Spring
    * 
    * @return the notification service
    */
   public IPSNotificationService getNotifications()
   {
      return m_notifications;
   }

   /**
    * @param notifications the notification service to set, never
    *           <code>null</code>
    */
   public void setNotifications(IPSNotificationService notifications)
   {
      if (notifications == null)
      {
         throw new IllegalArgumentException("notifications may not be null");
      }
      m_notifications = notifications;
      // Register listener here
      m_notifications.addListener(EventType.OBJECT_INVALIDATION,
            new PSSiteNotificationListener());
   }

   @SuppressWarnings("unchecked")
   public List<IPSPublishingContext> findAllContexts() throws PSNotFoundException {
      return findAllContexts(true);
   }

   /**
    * It does the same as {@link #findAllContexts()}, but it loads the
    * child components as specified by the argument.
    * @param includeChildren <code>true</code> if include child components.
    * @return the loaded Context, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   private List<IPSPublishingContext> findAllContexts(boolean includeChildren) throws PSNotFoundException {
      List<IPSPublishingContext> result = sessionFactory.getCurrentSession()
              .createCriteria(PSPublishingContext.class).list();

      if (includeChildren)
      {
         for (IPSPublishingContext ctx : result)
            loadDefaultSchemeIfNeeded(ctx);
      }
      
      return result;
   }

   @SuppressWarnings("unchecked")
   public List<IPSLocationScheme> findAllSchemes()
   {
      return sessionFactory.getCurrentSession().createCriteria(PSLocationScheme.class).list();
   }
   
   @SuppressWarnings("unchecked")
   public List<String> findDistinctSiteVariableNames()
   {
      List<String> names =
              sessionFactory.getCurrentSession().createQuery("select distinct name from PSSiteProperty")
              .list();
      
      return names != null ? names : Collections.EMPTY_LIST;
   }

   public void deleteContext(IPSPublishingContext context)
   {
      if (context == null)
      {
         throw new IllegalArgumentException("context may not be null");
      }
      sessionFactory.getCurrentSession().delete(context);
      
      // the object will be evicted by the framework, 
      // see PSEhCacheAccessor.notifyEvent()
   }

   @SuppressWarnings("unchecked")
   public List<IPSLocationScheme> findSchemesByContextId(IPSGuid contextid)
   {
      try
      {
         return sessionFactory.getCurrentSession().createQuery(
                 "from PSLocationScheme where contextId = :ctxId").setParameter(
                 "ctxId", contextid.longValue()).list();
      }
      catch (Exception e)
      {
         String errMsg = "Failed to find schemes by context id="
               + contextid.toString();
         throw new RuntimeException(errMsg, e);
      }
   }

   public void saveContext(IPSPublishingContext context)
   {
      if (context == null)
      {
         throw new IllegalArgumentException("context may not be null");
      }
      sessionFactory.getCurrentSession().saveOrUpdate(context);
   }
   
   public IPSPublishingContext createContext()
   {
      PSPublishingContext ctx = new PSPublishingContext();
      long nextId = PSGuidHelper.generateNext(PSTypeEnum.CONTEXT).longValue();
      ctx.setGUID(PSGuidUtils.makeGuid(nextId, PSTypeEnum.CONTEXT));
      return ctx;
   }

   @SuppressWarnings("unchecked")
   public Map<Integer, String> getContextNameMap()
   {
      List<Object[]> values = sessionFactory.getCurrentSession()
         .createQuery("select id, name from PSPublishingContext").list();
      Map<Integer, String> rval = new HashMap<>();
      for(Object[] row : values)
      {
         rval.put(((Long) row[0]).intValue(), (String) row[1]);
      }
      return rval;
   }
   
   /**
    * Finds the Site and Templates associations. This is not exposed in
    * {@link IPSSiteManager} because the map key is not consistent with map
    * value, but we need the ID/Name pair in.
    * 
    * @TODO enhance {@link #getSummaries(PSTypeEnum)} to use projection to load
    * the object so that it can be used to result ID/Name mapping.
    * 
    * @return the association map, where the map key is Site ID/Name, which maps
    * to a collection of associated Template IDs. The collection of Template
    * IDs is never <code>null</code>, but may be empty. The returned map can
    * never be <code>null</code>, but may be empty.
    */
   public Map<PSPair<IPSGuid, String>, Collection<IPSGuid>> findSiteTemplatesAssociations()
   {
      return getSiteTemplateAssociation(sessionFactory.getCurrentSession());

   }

   /**
    * Log the Site / Template association.
    * 
    * @param assoc the association in question, assumed not <code>null</code>.
    */
   private void logSiteTemplateAssoc(
         Map<PSPair<IPSGuid, String>, Collection<IPSGuid>> assoc)
   {
      if (!log.isDebugEnabled())
         return;
      
      String pattern = "Site (id={0}, name=\"{1}\") associate with Templates, IDs={2}.";
      for (Map.Entry<PSPair<IPSGuid, String>, Collection<IPSGuid>> entry : 
         assoc.entrySet())
      {
         PSPair<IPSGuid, String> k = entry.getKey();
         StringBuilder buffer = new StringBuilder();
         for (IPSGuid g : entry.getValue())
         {
            buffer.append(String.valueOf(g.getUUID()) + ", ");
         }
         Object[] args = new Object[] { k.getFirst().toString(),
               k.getSecond(), buffer.toString() };
         MessageFormat form = new MessageFormat(pattern);
         String message = form.format(args);
         log.debug(message);
      }
   }
   
   /**
    * Gets the site and template association, using native SQL
    * to query the repository directly.
    * Note, we cannot use HQL here because the PSX_VARIANT_SITE table is not
    * map to a "hibernated" object. 
    */
   @SuppressWarnings("unchecked")
   private Map<PSPair<IPSGuid, String>, Collection<IPSGuid>> getSiteTemplateAssociation(
         Session sess)
   {
      Map<PSPair<IPSGuid, String>, Collection<IPSGuid>> siteToTemplateIds = new HashMap<>();

      String sql = null;
      try {
         sql = "select s.SITEID, s.SITENAME, st.VARIANTID from "
         + PSSqlHelper.qualifyTableName("RXSITES") + " s "
         + "left outer join "
         + PSSqlHelper.qualifyTableName("PSX_VARIANT_SITE") + " st "
         + "on s.SITEID = st.SITEID";
      } catch (SQLException e) {
         throw new RuntimeException(e);
      }

      List<Object[]> result = sess.createSQLQuery(sql).addScalar("SITEID",
            StandardBasicTypes.LONG).addScalar("SITENAME", StandardBasicTypes.STRING).addScalar(
            "VARIANTID", StandardBasicTypes.LONG).list();
      
      for (Object[] row : result)
      {
         // collect the data
         IPSGuid siteId = new PSGuid(PSTypeEnum.SITE, (Long)row[0]);
         PSPair<IPSGuid, String> site = new PSPair<>(siteId,
               (String)row[1]);

         // This is a result of left outer join, so 3nd value may be null
         // for the Sites are not associate with any Templates  
         IPSGuid tempId = row[2] != null ? new PSGuid(
               PSTypeEnum.TEMPLATE, (Long)row[2]) : null;
               
         // store the result
         Collection<IPSGuid> ids = siteToTemplateIds.get(site);
         if (ids == null)
         {
            ids = new ArrayList<>();
            siteToTemplateIds.put(site, ids);
         }
         if (tempId != null)
            ids.add(tempId); 
      }
      
      if (log.isDebugEnabled())
         logSiteTemplateAssoc(siteToTemplateIds);

      return siteToTemplateIds;
   }   
}
