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
package com.percussion.services.publisher.impl;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.rx.publisher.IPSPublisherItemStatus;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSPublisherJobStatus.ItemState;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.data.PSDemandWork;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.catalog.IPSCatalogErrors;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSCatalogException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.contentmgr.data.PSQueryResult;
import com.percussion.services.contentmgr.data.PSRow;
import com.percussion.services.contentmgr.data.PSRowComparator;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.filter.data.PSFilterItem;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSContentListGenerator;
import com.percussion.services.publisher.IPSDeliveryType;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionContentList;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.IPSEditionTaskLog;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSPubStatus;
import com.percussion.services.publisher.IPSPubStatus.EndingState;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.IPSPublisherServiceErrors;
import com.percussion.services.publisher.IPSSiteItem;
import com.percussion.services.publisher.IPSSiteItem.Operation;
import com.percussion.services.publisher.IPSSiteItem.Status;
import com.percussion.services.publisher.IPSTemplateExpander;
import com.percussion.services.publisher.PSPublisherException;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.PSRuntimePublisherException;
import com.percussion.services.publisher.data.PSContentList;
import com.percussion.services.publisher.data.PSContentListItem;
import com.percussion.services.publisher.data.PSContentListResults;
import com.percussion.services.publisher.data.PSDeliveryType;
import com.percussion.services.publisher.data.PSEdition;
import com.percussion.services.publisher.data.PSEditionContentList;
import com.percussion.services.publisher.data.PSEditionTaskDef;
import com.percussion.services.publisher.data.PSEditionTaskLog;
import com.percussion.services.publisher.data.PSEditionTaskParam;
import com.percussion.services.publisher.data.PSItemPublishingHistory;
import com.percussion.services.publisher.data.PSPubItem;
import com.percussion.services.publisher.data.PSPubStatus;
import com.percussion.services.publisher.data.PSSiteItem;
import com.percussion.services.publisher.data.PSSortCriterion;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.utils.general.PSServiceConfigurationBean;
import com.percussion.services.utils.xml.PSXStreamObjectStream;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSBaseBean;
import com.percussion.util.PSSqlHelper;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jdbc.IPSDatasourceManager;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.timing.PSTimer;
import com.percussion.utils.types.PSPair;
import com.percussion.utils.xml.PSInvalidXmlException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import static com.percussion.services.utils.orm.PSDataCollectionHelper.MAX_IDS;
import static com.percussion.services.utils.orm.PSDataCollectionHelper.clearIdSet;
import static com.percussion.services.utils.orm.PSDataCollectionHelper.createIdSet;
import static com.percussion.services.utils.orm.PSDataCollectionHelper.executeQuery;
import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**`
 * Implementation of content list service, see interface for contracts.
 * 
 * @author dougrand
 */
@Transactional
@PSBaseBean("sys_publisherservice")
public class PSPublisherService
      implements
         IPSPublisherService
{
   private SessionFactory sessionFactory;
   
   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   public static final int BATCH_SIZE = 50;
   
   /**
    * The message separator, used to gather messages together and later to 
    * break them apart for viewing.
    */
   public final static String MESSAGE_SEPARATOR =
         "<<<-------------------------->>>";
   
   /**
    * A class that represents a single publishing case. Used for looking up
    * locations from site item info in content list processing
    */
   private static class PubItem
   {
      /**
       * The content id.
       */
      private Integer mi_contentId;

      /**
       * The template id.
       */
      private Long mi_templateId;

      /**
       * Ctor.
       * 
       * @param cid
       * @param tid
       */
      public PubItem(Integer cid, Long tid) {
         mi_contentId = cid;
         mi_templateId = tid;
      }

      /**
       * Ctor.
       * 
       * @param item a site item, assumed not <code>null</code>
       */
      public PubItem(IPSSiteItem item) {
         mi_contentId = item.getContentId();
         mi_templateId = item.getTemplateId();
      }

      /**
       * Ctor.
       * 
       * @param item a content list item, assumed not <code>null</code>
       */
      public PubItem(PSContentListItem item) {
         PSLegacyGuid lg = (PSLegacyGuid) item.getItemId();
         mi_contentId = lg.getContentId();
         mi_templateId = item.getTemplateId().longValue();
      }

      /**
       * @return Returns the contentId.
       */
      public Integer getContentId()
      {
         return mi_contentId;
      }

      /**
       * @param contentId The contentId to set.
       */
      public void setContentId(Integer contentId)
      {
         mi_contentId = contentId;
      }

      /**
       * @return Returns the templateId.
       */
      public Long getTemplateId()
      {
         return mi_templateId;
      }

      /**
       * @param templateId The templateId to set.
       */
      public void setTemplateId(Long templateId)
      {
         mi_templateId = templateId;
      }

      @Override
      public boolean equals(Object b)
      {
         return EqualsBuilder.reflectionEquals(this, b);
      }

      @Override
      public int hashCode()
      {
         return HashCodeBuilder.reflectionHashCode(this);
      }

      @Override
      public String toString()
      {
         return "<PubItem " + mi_contentId + " template: " + mi_templateId
               + ">";
      }
   }

   /**
    * Partial url path to the assembly servlet.
    */
   private static final String ASSEMBLER_RENDER_PATH = "/"
      + IPSAssemblyService.ASSEMBLY_URL;

   /**
    * Field definition used in the queries.
    */
   private static final String RX_SYS_CONTENTID = "rx:sys_contentid";

   /**
    * Field definition used in the queries.
    */
   private static final String RX_SYS_FOLDERID = "rx:sys_folderid";

   /**
    * Field definition used in the queries.
    */
   private static final String RX_SYS_CONTENTTYPEID = "rx:sys_contenttypeid";

   /**
    * Field definition used in the queries
    */
   private static final String RX_SYS_REVISION = "rx:sys_revision";

   /**
    * Logger used for publisher service.
    */
   private static Logger log = LogManager.getLogger(PSPublisherService.class);
   
   /**
    * These ids are stored by the demand publishing system. These are stored
    * based on the edition id (the key), which generally will avoid a collision
    * between two users wanting to demand publish at the same time. This
    * may need to be extended using the user session at some point.
    * 
    * @deprecated only used for deprecated calls in this service. Use the
    * business publisher service instead.
    */
   private Map<Integer, int[]> m_demandids = new HashMap<>();

   /**
    * As with the demand id above, the demand folder stores the relevant parent
    * folder for the ids being published on demand. The key is the edition, 
    * and the same caveats apply as for the demand ids above.
    * 
    * @deprecated only used for deprecated calls in this service. Use the
    * business publisher service instead.
    */
   private Map<Integer, Integer> m_demandfolder =
         new HashMap<>();

   /**
    * Service configuration bean. It is fired by Spring bean configuration.
    */
   private PSServiceConfigurationBean m_configurationBean;
   
   /**
    * Business publisher bean.
    */
   private IPSRxPublisherService m_rxpub;

   /*
    * Program resources.
    */
   private static ResourceBundle ms_Res = null;

   public IPSContentList createContentList(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      PSContentList cl = new PSContentList();
      cl.setName(name);
      cl.setGUID(PSGuidHelper.generateNext(PSTypeEnum.CONTENT_LIST));
      return cl;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.IPSPublisherService#loadContentLists(java.util.List)
    */
   @Transactional
   public List<IPSContentList> loadContentLists(List<IPSGuid> ids)
   {
      if (ids == null || ids.size() == 0)
      {
         throw new IllegalArgumentException("ids may not be null or empty");
      }
      // @TODO load from cache
      List<IPSContentList> rval = new ArrayList<>();
      for (IPSGuid g : ids)
      {
         try {
            rval.add(loadContentList(g));
         } catch (PSNotFoundException e) {
            log.warn("Content not found for Guid: {}, skipping Guid",g.toStringUntyped());
         }
      }
      return rval;
   }
   
   @Transactional
   public IPSContentList loadContentList(IPSGuid id) throws PSNotFoundException {
      // @TODO load from cache
      IPSContentList clist = loadContentListModifiable(id);
      loadItemFilterIfNeeded(clist);
      
      return clist;
   }

   @Transactional
   public IPSContentList loadContentListModifiable(IPSGuid id) throws PSNotFoundException {
      if (id == null)
      {
         throw new IllegalArgumentException("id may not be null");
      }
      
      IPSContentList clist = (IPSContentList) sessionFactory.getCurrentSession().get(
            PSContentList.class, id.longValue());
      if (clist == null)
         throw new PSNotFoundException(id);
      
      return clist;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.IPSPublisherService#saveContentLists(java.util.List)
    */
   @Transactional
   public void saveContentLists(List<IPSContentList> lists)
   {
      if (lists == null || lists.size() == 0)
      {
         throw new IllegalArgumentException("lists may not be null or empty");
      }
      Session s = sessionFactory.getCurrentSession();

      for (IPSContentList g : lists)
      {
         s.saveOrUpdate(g);
      }

   }

   @Transactional
   public void saveContentList(IPSContentList clist)
   {
      if (clist == null)
      {
         throw new IllegalArgumentException("clist may not be null");
      }      
      sessionFactory.getCurrentSession().saveOrUpdate(clist);
   }


   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.IPSPublisherService#deleteContentLists(java.util.List)
    */
   @Transactional
   public void deleteContentLists(List<IPSContentList> lists)
   {
      if (lists == null || lists.size() == 0)
      {
         throw new IllegalArgumentException("lists may not be null or empty");
      }
      Session s =sessionFactory.getCurrentSession();

      for (IPSContentList g : lists)
      {
         s.delete(g);
      }

   }

   @Transactional
   public IPSContentList loadContentList(String name) throws PSNotFoundException
   {
      // @TODO load from cache
      IPSContentList clist = findContentListByName(name);
      if (clist == null)
         throw new PSNotFoundException(name, PSTypeEnum.CONTENT_LIST);
      return clist;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.IPSPublisherService#findContentListByName(java.lang.String)
    */
   @Transactional
   public IPSContentList findContentListByName(String name) throws PSNotFoundException {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      // @TODO load from cache
      Session s = sessionFactory.getCurrentSession();

      Criteria c = s.createCriteria(PSContentList.class);
      c.add(Restrictions.eq("name", name));
      List results = c.list();
      if (results.size() == 0)
         return null;

      IPSContentList clist = (IPSContentList) results.get(0);
      loadItemFilterIfNeeded(clist);
      return clist;

   }
   
   /*
    * Loads the referenced item filter for the given Content List if it has one.
    * @param clist the Content List in question, assumed not <code>null</code>.
    */
   private void loadItemFilterIfNeeded(IPSContentList clist) throws PSNotFoundException {
      if (clist.getFilterId() == null)
         return;
      
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      IPSItemFilter filter = fsvc.loadFilter(clist.getFilterId());
      
      // setFilter() is @deprecated in interface, but not in class level 
      ((PSContentList)clist).setFilter(filter);
   }

   @Transactional
   public List<IPSContentList> findAllContentLists(String filter)
   {
      if (filter == null)
      {
         throw new IllegalArgumentException("filter may not be null");
      }
      // @TODO load from cache
      Session s = sessionFactory.getCurrentSession();

      Criteria c = s.createCriteria(PSContentList.class);
      if (!StringUtils.isBlank(filter))
      {
         c.add(Restrictions.ilike("name", "%" + filter + "%"));
      }
      c.addOrder(Order.asc("name"));
      c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
      List<IPSContentList> results = c.list();
      for (IPSContentList clist : results)
      {
         try {
            loadItemFilterIfNeeded(clist);
         } catch (PSNotFoundException e) {
            log.warn("Skipping item filter: {}",e.getMessage());
         }
      }
      return results;

   }
   
   @Transactional
   public List<String> findAllContentListNames(String filter)
   {
      if (filter == null)
      {
         throw new IllegalArgumentException("filter may not be null");
      }
      // @TODO load from cache
      Session s = sessionFactory.getCurrentSession();

      Criteria c = s.createCriteria(PSContentList.class);
      if (!StringUtils.isBlank(filter))
      {
         c.add(Restrictions.ilike("name", "%" + filter + "%"));
      }
      c.addOrder(Order.asc("name"));
      c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
      List<IPSContentList> results = c.list();
      List<String> nameList = new ArrayList<>();
      for (IPSContentList clist : results)
      {
         nameList.add(clist.getName());
      }

      return nameList;

   }
   
   @Transactional
   public IPSContentList findContentListById(IPSGuid contListID) throws PSNotFoundException {
      if (contListID == null)
      {
         throw new IllegalArgumentException("contListID may not be null");
      }
      
      // @TODO load from cache
      Session s = getSessionFactory().getCurrentSession();;

      Criteria c = s.createCriteria(PSContentList.class);
      c.add(Restrictions.eq("contentListId", contListID.longValue()));
      List results = c.list();
      if (results.size() == 0)
      {
         return null;
      }
      IPSContentList clist = (IPSContentList) results.get(0);
      loadItemFilterIfNeeded(clist);
      return clist;

   }

   @Transactional
   public List<IPSDeliveryType> findAllDeliveryTypes()
   {
      return sessionFactory.getCurrentSession().createCriteria(PSDeliveryType.class).list();
   }

   /*
    * //see base class method for details
    */
   public List<PSContentListItem> executeContentList(IPSContentList list,
         Map<String, String> overrides, boolean isPublish, int deliveryContext)
         throws PSPublisherException
   {

      if (overrides == null)
         throw new IllegalArgumentException("overrides may not be null.");

      String siteidstr = overrides.get(IPSHtmlParameters.SYS_SITEID);
      IPSGuid siteId = null;
      try
      {
         siteId = new PSGuid(PSTypeEnum.SITE, siteidstr);
      }
      catch (Exception e)
      {
         log.error("Bad site id found " + siteidstr, e);
      }
      IPSGuid deliveryContextId = new PSGuid(PSTypeEnum.CONTEXT,
            deliveryContext);
      return executeContentList(list, overrides, isPublish, deliveryContextId,
            siteId);
   }
   

   /*
    * //see base class method for details
    */
   public PSContentListResults runContentList(final IPSContentList list,
         final Map<String, String> overrides, final boolean publish,
         final IPSGuid deliveryContextId, final IPSGuid siteId)
      throws PSPublisherException
   {
      if (siteId == null)
         throw new IllegalArgumentException("siteId may not be null.");
      
      final Map<String,String> overrideParams;
      if (overrides == null)
         overrideParams = new HashMap<>();
      else
         overrideParams = new HashMap<>(overrides);
      // override the context & site IDs parameters to keep them in sync 
      overrideParams.put(IPSHtmlParameters.SYS_CONTEXT,
            "" + deliveryContextId.getUUID());
      overrideParams.put(IPSHtmlParameters.SYS_SITEID, "" + siteId.getUUID());
      // set "siteId" is for backward compatible only, 
      // not needed by current implementation. 
      overrideParams.put("siteid", "" + siteId.getUUID());
      

      Timer sw = new Timer();
      String gen = list.getGenerator();
      String exp = list.getExpander();
      final IPSContentListGenerator generator;
      final IPSTemplateExpander expander;

      try
      {
         generator = (IPSContentListGenerator) lookupExtension(gen);
         expander = (IPSTemplateExpander) lookupExtension(exp);
      }
      catch (Exception e)
      {
         throw new PSPublisherException(
               IPSPublisherServiceErrors.EXTENSION_LOOKUP, e);
      }
      final Map<String, String> genparams = list.getGeneratorParams();
      genparams.putAll(overrideParams);
      final Map<String, String> expparams = list.getExpanderParams();
      expparams.putAll(overrideParams);
      
      // Call generator
      final Timer swTemp = new Timer();
      final QueryResult result = generator.generate(genparams);
      
      swTemp.logElapsed("Call generator for content list '" + list.getName()
            + "' (" + list.getGUID().getUUID() + ")");
      
      try
      {
         final long size = result.getRows().getSize();
         final long chunks = size == 0 ? 0 : (long) Math.ceil(((double) size / (double) contentListChunkSize()));
         log.debug("Splitting content list size: " + size + " into " + chunks + " chunks.");
         final Iterator<QueryResult> splitResults = PSQueryResultUtils.splitQueryResults(result, contentListChunkSize());
         final Iterator<List<PSContentListItem>> li = 
            Iterators.transform(splitResults, new Function<QueryResult, List<PSContentListItem>>()
         {
            private long chunk  = 0;
            public List<PSContentListItem> apply(QueryResult qr)
            {
               try
                     {
                        PSTimer swTemp2 = new PSTimer(log);

                        chunk++;
                        log.debug("Pulling chunk: " + chunk + " of " + chunks);
                        // allow transaction by not going via "this" which is
                        // not seen by spring

                        List<PSContentListItem> Contentlistitem = PSPublisherServiceLocator.getPublisherService().getContentListItems(list, expander, qr, publish,
                              siteId, deliveryContextId, expparams, overrideParams);

                        swTemp2.logElapsed("Total time for content list chunk, number of items is: "
                              + Contentlistitem.size());

                        return Contentlistitem;
                     }
                     catch (PSPublisherException e)
                     {
                        throw new PSRuntimePublisherException(e);
                     }
                  }
               });

         Iterator<PSContentListItem> it = new PSIteratorChain<PSContentListItem>()
         {

            @Override
            protected Iterator<PSContentListItem> nextIterator()
            {
               if (li.hasNext())
                  return li.next().iterator();
               return null;
            }
            
         };
         
         sw.logElapsed("Finish executing content list '" + list.getName()
               + "', selected " + size + " items.");
         return new PSContentListResults(it, size);

      }
      catch (RepositoryException e)
      {
         throw new PSPublisherException(
               IPSPublisherServiceErrors.ROW_RETRIEVAL, e);
      }
      catch (PSRuntimePublisherException re) {
          throw re.getPublisherException();
      }
   }
   
   /**
    * The content list results are split based on chunk size.
    * @return the chunk size (number of items in each processing chunk), -1 indicates 
    * no splitting into chunks.
    */
   private static int contentListChunkSize()
   {
      if (PSServer.getServerProps() == null) return 1000;
      String prop = PSServer.getServerProps().getProperty("contentListChunkSize", "1000");
      Integer p = Integer.parseInt(prop);
      if (p <= 0) return Integer.MAX_VALUE;
      return p;
   }
   

   public List<PSContentListItem> executeContentList(IPSContentList list,
         Map<String, String> overrides, boolean publish,
         IPSGuid deliveryContextId, IPSGuid siteId) throws PSPublisherException
   {
      return Lists.newArrayList(runContentList(list, overrides, publish, deliveryContextId, siteId).iterator());
   }

   @Transactional
   public List<PSContentListItem> getContentListItems(IPSContentList list,
                                                      IPSTemplateExpander expander, QueryResult result, boolean publish,
                                                      IPSGuid siteId, IPSGuid deliveryContextId,
                                                      Map<String, String> expparams, Map<String, String> overrideParams) throws PSPublisherException {
      try {
         List<PSContentListItem> rval;
         Timer swTemp;
         swTemp = new Timer();
         Map<Integer, PSComponentSummary> cidToSum =
                 createContentIdToSummaryMap(result);
         swTemp.logElapsed("Contentlist generator generated " + cidToSum.size() + " items.");

         // create filter items
         swTemp = new Timer();
         Map<Row, IPSFilterItem> rowToItem = new HashMap<>();
         List<IPSFilterItem> items = createContentListFilterItems(result,
                 cidToSum, rowToItem, siteId);
         swTemp.logElapsed("Create filter items");

         // filter items
         swTemp = new Timer();
         Map<Object, IPSFilterItem> okitems =
                 new HashMap<>();

         PSQueryResult filteredresults = filterContentListItems(list, result,
                 items, cidToSum, rowToItem, okitems, overrideParams,
                 list.getType().equals(IPSContentList.Type.INCREMENTAL), siteId);

         swTemp.logElapsed("Contentlist after filtering resulted in " + okitems.size() + " items.");

         // Expand the results
         swTemp = new Timer();
         rval = expander.expand(filteredresults, expparams, cidToSum);
         swTemp.logElapsed("Contentlist after expanding resulted in " + rval.size() + " items.");

         if (!publish) {
            rval = adjustContentListForUnpublishing(deliveryContextId, rval,
                    siteId);
         }

         swTemp = new Timer();
         adjustContentListItemRevisions(rval, okitems);
         swTemp.logElapsed("Adjust item revisions");
         rval = sortPagesToEnd(rval, cidToSum);
         return rval;
      } catch (RepositoryException e) {
         throw new PSPublisherException(
                 IPSPublisherServiceErrors.ROW_RETRIEVAL, e);
      } catch (PSFilterException e) {
         throw new PSPublisherException(
                 IPSPublisherServiceErrors.FILTER_MALFUNCTION, e, list
                 .getFilter().getName());
      }
   }

   @Override
   public void updatePubLogHidden(PSPubServer psPubServer) {
      // First disconnect records belonging to the given jobId. Then find
      // all records that are disconnected and not in use by the site items
      // table and remove them
      Session s = getSessionFactory().getCurrentSession();

      Query q = s.createQuery(
              "update PSPubStatus set hidden = 'Y' where pubServerId = :pubServerId");
      q.setLong("pubServerId", psPubServer.getServerId());
      q.executeUpdate();
   }

   private class SortPagesToEnd implements Comparator<PSContentListItem> 
   { 
      private Map<Integer, PSComponentSummary> cidToSum;
      private long pagectId=0;
      private final static String PAGE_CONTENT_TYPE = "percPage";
      
      /***
       * Used to sort pages to the end of the content list.
       * 
       * @param cidToSum
       */
      public SortPagesToEnd(Map<Integer, PSComponentSummary> cidToSum){
         this.cidToSum = cidToSum;
        
         try
         {
            pagectId = PSItemDefManager.getInstance().contentTypeNameToId(PAGE_CONTENT_TYPE);
         }
         catch (PSInvalidContentTypeException e)
         {
            log.error("Unable to locate Page content type for Select Item sorting", e);
         }
      }
      
       // Used to compare items for a page type to sort them to the end of the content list. 
      //  could be updated for any template that is a page type. 
       public int compare(PSContentListItem a, PSContentListItem b) 
       { 
          PSComponentSummary suma = cidToSum.get(a.getItemId().getUUID());
          PSComponentSummary sumb = cidToSum.get(b.getItemId().getUUID());
         
          if(suma.getContentTypeId() == pagectId & sumb.getContentTypeId() == pagectId) 
             return 0;
          else if(suma.getContentTypeId() != pagectId & sumb.getContentTypeId() == pagectId)
             return -1;
          else
             return 1;
       } 
   }
   
   /***
    * Sort Pages to end. 
    * @param rval
    * @param cidToSum
    * @return
    */
   private List<PSContentListItem> sortPagesToEnd(List<PSContentListItem> rval, Map<Integer, PSComponentSummary> cidToSum)
   {
      Collections.sort(rval,new SortPagesToEnd(cidToSum));
      return rval;
   }

   /**
    * Iterate over the results and build a map from contentid to component
    * summary.
    * 
    * @param result the result from the generator, assumed never
    *           <code>null</code>.
    * @return the map, never <code>null</code>
    * @throws RepositoryException
    * @throws ItemNotFoundException
    * @throws ValueFormatException
    */
   private Map<Integer, PSComponentSummary> createContentIdToSummaryMap(
         QueryResult result) throws RepositoryException, ItemNotFoundException,
         ValueFormatException
   {
      Map<Integer, PSComponentSummary> cidToSum =
            new HashMap<>();
      RowIterator riter = result.getRows();
      List<Integer> cids = new ArrayList<>();
      boolean first = true;

      while (riter.hasNext())
      {
         Row r = riter.nextRow();
         if (first)
         {
            first = false;
            if (r.getValue(RX_SYS_CONTENTID) == null)
            {
               throw new IllegalArgumentException(
                     "Rows must contain rx:sys_contentid");
            }
         }
         Value cid = r.getValue(RX_SYS_CONTENTID);
         cids.add((int) cid.getLong());
      }

      // Get the summaries
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      List<PSComponentSummary> sums = cms.loadComponentSummaries(cids);

      for (PSComponentSummary s : sums)
      {
         cidToSum.put(s.getContentId(), s);
      }

      return cidToSum;
   }

   /**
    * Create and return the filter items to use when creating the content list.
    * The filter items are used to determine which of the queried results are
    * going to be kept in the finished content list, and what revisions of those
    * items will be published.
    * 
    * @param result the query result to use, assumed never <code>null</code>.
    * @param cidToSum a map of content id to content summaries that is used in
    *           multiple places in the code, assumed never <code>null</code>.
    * @param rowToItem a map from row to filter item that communicates between
    *         this method and
    *         {@link #filterContentListItems(IPSContentList, QueryResult, List, Map, Map, Map, Map, boolean, IPSGuid)}
    *           assumed never <code>null</code>.
    * @param siteid the site id, assumed never <code>null</code>.
    * @return a list of filter items, never <code>null</code>.
    * 
    * @throws RepositoryException
    * @throws ItemNotFoundException
    * @throws ValueFormatException
    */
   private List<IPSFilterItem> createContentListFilterItems(QueryResult result,
         Map<Integer, PSComponentSummary> cidToSum,
         Map<Row, IPSFilterItem> rowToItem, IPSGuid siteid)
         throws RepositoryException, ItemNotFoundException,
         ValueFormatException
   {
      List<IPSFilterItem> items = new ArrayList<>();
      RowIterator riter;
      riter = result.getRows();
      while (riter.hasNext())
      {
         Row r = riter.nextRow();
         Value cid = r.getValue(RX_SYS_CONTENTID);
         Value fid = null;
         try {
            fid = r.getValue(RX_SYS_FOLDERID);
         } catch (ItemNotFoundException e) {
            log.debug("No Folder id used in query");
         }
         int contentId = (int) cid.getLong();
         PSComponentSummary sum = cidToSum.get(contentId);
         if (sum==null)
         {
            log.debug("Item "+contentId +" may have been deleted, will filter");
            break;
         }
         PSLegacyGuid itemid = new PSLegacyGuid(contentId, sum
               .getPublicOrCurrentRevision());
         PSLegacyGuid folderid = null;
         if (fid != null)
         {
            folderid = new PSLegacyGuid((int) fid.getLong(), 0);
         }
         IPSFilterItem item = new PSFilterItem(itemid, folderid, siteid);
         rowToItem.put(r, item);
         items.add(item);
      }
      return items;
   }

   /**
    * Adjust the content list items revisions by checking the returned values
    * from the filtering process.
    * 
    * @param items the content list items, assumed never <code>null</code>.
    * @param okitems the map of keys to filter items, note that the semantics of
    *           the key of the {@link PSContentListItem} must match the value
    *           and semantics of the key of {@link PSFilterItem} for this to
    *           work.
    */
   private void adjustContentListItemRevisions(List<PSContentListItem> items,
         Map<Object, IPSFilterItem> okitems)
   {
      for (PSContentListItem item : items)
      {
         IPSFilterItem filtereditem = okitems.get(item.getKey());
         assert filtereditem != null;
         IPSGuid filtereditemid = filtereditem.getItemId();
         if (!filtereditemid.equals(item.getItemId()))
         {
            item.setItemId(filtereditemid);
         }
      }
   }

   /**
    * For unpublishing, we need to use the original published location if we
    * have it available. We query the site items to find this information, then
    * walk the content list items and set the location if we have it.
    * 
    * @param deliveryContextId the delivery content used for location generator,
    *    assumed not <code>null</code>.
    * @param contentListItems the content list items being adjusted for the
    *           unpublish, assumed never <code>null</code>
    * @param siteid the site being published, assumed never <code>null</code>
    * 
    * @return a list of Content List Items with non-null locations, never
    *    <code>null</code>, but may be empty.
    */
   private List<PSContentListItem> adjustContentListForUnpublishing(
         IPSGuid deliveryContextId, List<PSContentListItem> contentListItems,
         IPSGuid siteid)
   {
      Collection<IPSSiteItem> siteitems = findSiteItems(siteid,
            deliveryContextId.getUUID());
      // Create a map from the content information to a location
      Map<PubItem, Object> pubitems = new HashMap<>();
      for (IPSSiteItem si : siteitems)
      {
         PubItem pitem = new PubItem(si);
         Object v = pubitems.get(pitem);
         if (v == null)
         {
            pubitems.put(pitem, si);
         }
         else
         {
            if (v instanceof IPSSiteItem)
            {
               List<IPSSiteItem> vlist = new ArrayList<>();
               vlist.add((IPSSiteItem)v);
               vlist.add(si);
               pubitems.put(pitem, vlist);
            }
            else
            {
               ((List<IPSSiteItem>)v).add(si);
            }
         }
      }

      // Walk the content list items and set the location if we have one
      // otherwise remove the item as it isn't available for unpublishing
      List<PSContentListItem> result = new ArrayList<>();
      for (PSContentListItem ci : contentListItems)
      {
         Object v = pubitems.get(new PubItem(ci));
         if (v != null)
         {
            if (v instanceof IPSSiteItem)
            {
               IPSSiteItem si = (IPSSiteItem)v;
               if (StringUtils.isNotBlank(si.getLocation()))
               {
                  ci.setLocation(si.getLocation());
                  result.add(ci);
               }
            }
            else if (v instanceof List)
            {
               boolean isFirst = true;
               for (IPSSiteItem si : (List<IPSSiteItem>)v)
               {
                  PSContentListItem cloned = (isFirst) ? ci : ci.clone();
                  if (StringUtils.isNotBlank(si.getLocation()))
                  {
                     cloned.setLocation(si.getLocation());
                     result.add(cloned);
                  }
                  isFirst = false;
               }
            }
         }
      }
      return result;
   }

   /**
    * Filter the results using the supplied item filter. This method also
    * adjusts the map passed in okitems to map the key for each item with the
    * actual adjusted {@link IPSFilterItem} so that a later stage can adjust the
    * returned revisions (if modified). This method only returns items that are
    * returned by the item filter.
    * <p>
    * Note that if the content type id is not in the results, this code will add
    * it. Takes the revision from filtered item and adds it to the query result.
    * 
    * 
    * @param list the content list, assumed never <code>null</code>.
    * @param sourceItems the source items produced by the generator, assumed
    *           never <code>null</code>
    * @param items the filter items passed from
    *           {@link #executeContentList(IPSContentList, Map, boolean, int)}
    *           which are being filtered here.
    * @param cidToSum the map of content id to component summary, assumed never
    *           <code>null</code>.
    * @param rowToItem the map of row to filter item, assumed never
    *           <code>null</code>.
    * @param okitems the map of filter keys to filter items, assumed never
    *           <code>null</code>.
    * @param overrides
    * @param siteId 
    * @param isIncremental
    * @return the filtered results, never <code>null</code>.
    * @throws RepositoryException
    * @throws ItemNotFoundException
    * @throws ValueFormatException
    * @throws PSFilterException
    * @throws PSPublisherException 
    */
   private PSQueryResult filterContentListItems(IPSContentList list,
         QueryResult sourceItems, List<IPSFilterItem> items,
         Map<Integer, PSComponentSummary> cidToSum,
         Map<Row, IPSFilterItem> rowToItem, Map<Object, IPSFilterItem> okitems,
         Map<String, String> overrides, boolean isIncremental, IPSGuid siteId) throws RepositoryException,
         ItemNotFoundException, ValueFormatException, PSFilterException, PSPublisherException
   {
      Timer swTemp;
      // Incremental, filter the content list items
      if (isIncremental)
      {
         swTemp = new Timer();
         List<Integer> movedIds = findMovedItems(siteId, false);
         swTemp.logElapsed("Find " + movedIds.size() + " moved items");

         PSIncrementalPublishingFilter incremental = 
            new PSIncrementalPublishingFilter();
         
         // Modifies the list "in place"
         swTemp = new Timer();
         
         incremental.filter(items, overrides, movedIds);

         swTemp.logElapsed("Contentlist after incremental filtering resulted in "
               + items.size() + " items.");
      }
      
      PSTimer timer = new PSTimer(log);
      if (list.getFilter() != null)
      {
         timer = new PSTimer(log);
         items = list.getFilter().filter(items, overrides);
         timer.logElapsed("Items Filtered: " + items.size() + " with filter: " + list.getFilter().getName());

      }

      RowIterator riter;
      for (IPSFilterItem item : items)
      {
         okitems.put(item.getKey(), item);
      }

      PSRowComparator rcomp = new PSRowComparator(
            new ArrayList<>());
      String[] origcnames = sourceItems.getColumnNames();
      boolean found = false;
      for (String c : origcnames)
      {
         if (c.equals(RX_SYS_CONTENTTYPEID))
         {
            found = true;
            break;
         }
      }
      String[] cnames = origcnames;
      if (!found)
      {
         cnames = new String[origcnames.length + 1];
         System.arraycopy(origcnames, 0, cnames, 0, origcnames.length);
         cnames[cnames.length - 1] = RX_SYS_CONTENTTYPEID;
      }
      //Add sys_revision column if does not exist
      if(!ArrayUtils.contains(cnames, RX_SYS_REVISION))
      {
         cnames = (String[]) ArrayUtils.add(cnames, cnames.length,
               RX_SYS_REVISION);
      }
      PSQueryResult filteredresults = new PSQueryResult(cnames, rcomp);
      riter = sourceItems.getRows();
      while (riter.hasNext())
      {
         try{
         Row r = riter.nextRow();
         Value cid = r.getValue(RX_SYS_CONTENTID);
         PSComponentSummary sum = cidToSum.get((int) cid.getLong());
         Map<String, Object> rowdata = new HashMap<>();
         for (int i = 0; i < origcnames.length; i++)
         {
            String c = origcnames[i];
            rowdata.put(origcnames[i], r.getValue(c));
         }
         rowdata.put(RX_SYS_CONTENTTYPEID, sum.getContentTypeId());
         IPSFilterItem filtereditem = okitems.get(rowToItem.get(r).getKey());
         if (filtereditem != null)
         {
            IPSGuid id = filtereditem.getItemId();
            if(id instanceof PSLegacyGuid)
            {
               rowdata.put(RX_SYS_REVISION,((PSLegacyGuid)id).getRevision());
            }
            PSRow nrow = new PSRow(rowdata);
            filteredresults.addRow(nrow);
         }
         }catch(NullPointerException npe){
            log.warn("Removing content item from list.", npe);
         }
      }
      return filteredresults;
   }

   /**
    * Lookup an extension.
    * 
    * @param name the name of the extension, never <code>null</code> or empty
    * @return the extension, never <code>null</code>
    * @throws PSExtensionException
    * @throws PSNotFoundException
    */
   private IPSExtension lookupExtension(String name)
      throws PSExtensionException,
      com.percussion.design.objectstore.PSNotFoundException
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      IPSExtensionManager emgr = PSServer.getExtensionManager(null);
      PSExtensionRef ref = new PSExtensionRef(name);
      return emgr.prepareExtension(ref, null);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.catalog.IPSCataloger#getTypes()
    */
   public PSTypeEnum[] getTypes()
   {
      return new PSTypeEnum[]
      {PSTypeEnum.CONTENT_LIST};
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.catalog.IPSCataloger#getSummaries(com.percussion.services.catalog.PSTypeEnum)
    */
   @Transactional
   public List<IPSCatalogSummary> getSummaries(PSTypeEnum type)
   {
      List<IPSCatalogSummary> rval = new ArrayList<>();

      Session s = sessionFactory.getCurrentSession();

         if (type.getOrdinal() == PSTypeEnum.CONTENT_LIST.getOrdinal())
         {
            Criteria c = s.createCriteria(PSContentList.class);
            List<IPSContentList> results = c.list();
            for (IPSContentList contentlist : results)
            {
               rval.add(new PSObjectSummary(contentlist.getGUID(), contentlist
                     .getName(), contentlist.getName(), contentlist
                     .getDescription()));
            }
         }

      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.catalog.IPSCataloger#loadByType(com.percussion.services.catalog.PSTypeEnum,
    *      java.lang.String)
    */
   public void loadByType(PSTypeEnum type, String item)
         throws PSCatalogException
   {
      try
      {
         if (type.equals(PSTypeEnum.CONTENT_LIST))
         {
            IPSContentList temp = new PSContentList();
            temp.fromXML(item);
            List<IPSContentList> lists = new ArrayList<>();
            lists.add(temp);
            saveContentLists(lists);
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
      catch (PSInvalidXmlException e)
      {
         throw new PSCatalogException(IPSCatalogErrors.XML, e, item);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.catalog.IPSCataloger#saveByType(com.percussion.utils.guid.IPSGuid)
    */
   public String saveByType(IPSGuid id) throws PSCatalogException
   {
      try
      {

         if (id.getType() == PSTypeEnum.CONTENT_LIST.getOrdinal())
         {
            List<IPSGuid> ids = new ArrayList<>();
            ids.add(id);
            IPSContentList temp = loadContentLists(ids).get(0);
            return temp.toXML();
         }
         else
         {
            PSTypeEnum type = PSTypeEnum.valueOf(id.getType());
            throw new PSCatalogException(IPSCatalogErrors.UNKNOWN_TYPE, type
                  .toString());
         }
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

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.IPSPublisherService#constructAssemblyUrl(java.lang.String,
    *      int, java.lang.String, com.percussion.utils.guid.IPSGuid,
    *      com.percussion.utils.guid.IPSGuid, com.percussion.utils.guid.IPSGuid,
    *      com.percussion.services.assembly.IPSAssemblyTemplate,
    *      com.percussion.services.filter.IPSItemFilter, int, boolean)
    */
   public String constructAssemblyUrl(String host, int port, String protocol,
         IPSGuid siteguid, IPSGuid contentid, IPSGuid folderguid,
         IPSAssemblyTemplate template, IPSItemFilter filter, int context,
         boolean publish)
   {
      if (StringUtils.isBlank(host))
      {
         host = PSServer.getHostAddress();
      }
      if (port == 0)
      {
         port = PSServer.getListenerPort();
      }
      if (StringUtils.isBlank(protocol))
      {
         protocol = "http";
      }
      if (!protocol.equals("http") && !protocol.equals("https"))
      {
         throw new IllegalArgumentException("protocol must be http or https");
      }
      if (contentid == null)
      {
         throw new IllegalArgumentException("contentid may not be null");
      }
      if (template == null)
      {
         throw new IllegalArgumentException("template may not be null");
      }
      if (filter == null)
      {
         throw new IllegalArgumentException("filter may not be null");
      }
      if (!(contentid instanceof PSLegacyGuid))
      {
         throw new IllegalArgumentException("contentid must be a legacy guid");
      }
      if (folderguid != null && !(folderguid instanceof PSLegacyGuid))
      {
         throw new IllegalArgumentException("folderguid must be a legacy guid");
      }
      PSLegacyGuid cid = (PSLegacyGuid) contentid;
      PSLegacyGuid fid = folderguid != null ? (PSLegacyGuid) folderguid : null;

      Map<String, String> params = new HashMap<>();
      StringBuilder b = new StringBuilder();
      b.append(protocol);
      b.append("://");
      b.append(host);
      b.append(':');
      b.append(port);
      String root = PSServer.getRequestRoot();
      b.append(root);
      b.append(ASSEMBLER_RENDER_PATH);

      String base = b.toString();

      params.put(IPSHtmlParameters.SYS_CONTENTID, Integer.toString(cid
            .getContentId()));
      params.put(IPSHtmlParameters.SYS_REVISION, Integer.toString(cid
            .getRevision()));
      if (fid != null)
      {
         params.put(IPSHtmlParameters.SYS_FOLDERID, Integer.toString(fid
               .getContentId()));
      }
      if (siteguid != null)
      {
         params.put(IPSHtmlParameters.SYS_SITEID, Long.toString(siteguid
               .longValue()));
      }

      params.put(IPSHtmlParameters.SYS_ITEMFILTER, filter.getName());
      params.put(IPSHtmlParameters.SYS_CONTEXT, Integer.toString(context));
      params.put(IPSHtmlParameters.SYS_TEMPLATE, Long.toString(template
            .getGUID().longValue()));
      params.put(IPSHtmlParameters.SYS_PUBLISH, publish
            ? "publish"
            : "unpublish");
      return PSUrlUtils.createUrl(base, params.entrySet().iterator(), null);
   } 

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.IPSPublisherService#findSiteItems(com.percussion.utils.guid.IPSGuid,
    *      int)
    */
   @Transactional
   public Collection<IPSSiteItem> findSiteItems(IPSGuid siteid,
         int deliveryContext)
   {
      if (siteid == null)
      {
         throw new IllegalArgumentException("siteid may not be null");
      }

      Session s = sessionFactory.getCurrentSession();

      Query q = s.getNamedQuery("pssiteitem_query_joined_items");
      q.setLong("siteid", siteid.longValue());
      q.setInteger("context", deliveryContext);
      return createSiteItemsList(q.list());

   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.IPSPublisherService#findSiteItems(com.percussion.utils.guid.IPSGuid,
    *      int)
    */
   public Collection<IPSSiteItem> findSiteItemsByPubServer(IPSGuid serverId, int deliveryContext)
   {
      if (serverId == null)
      {
         throw new IllegalArgumentException("siteid may not be null");
      }

      Session s = getSessionFactory().getCurrentSession();

         Query q = s.getNamedQuery("pssiteitem_pubserver_query_joined_items");
         q.setLong("serverid", serverId.longValue());
         q.setInteger("context", deliveryContext);
         return createSiteItemsList(q.list());


      }
   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPublisherService#findSiteItemsByIds(com.percussion.utils.guid.IPSGuid, int, java.util.Collection)
    */
   @Transactional
   public Collection<IPSSiteItem> findSiteItemsByIds(IPSGuid siteid,
         int deliveryContext, Collection<Integer> contentIds)
   {
      return findSiteItemsByIds_Common(siteid, deliveryContext, contentIds);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPublisherService#findSiteItemsByIds_ReadUncommit(com.percussion.utils.guid.IPSGuid, int, java.util.Collection)
    */
   @Transactional (isolation = Isolation.READ_UNCOMMITTED)
   public Collection<IPSSiteItem> findSiteItemsByIds_ReadUncommit(IPSGuid siteid,
         int deliveryContext, Collection<Integer> contentIds)
   {
      return findSiteItemsByIds_Common(siteid, deliveryContext, contentIds);
   }

   /**
    * @param siteId
    * @param deliveryContext
    * @param contentIds
    * @return
    */
   public Collection<IPSSiteItem> findSiteItemsByIds_Common(IPSGuid siteId,
         int deliveryContext, Collection<Integer> contentIds)
   {
      notNull(siteId);
      notNull(contentIds);
      notEmpty(contentIds);

      Session s = getSessionFactory().getCurrentSession();
      long idset = 0;

      try
      {
         Query q;
         if (contentIds.size() >= MAX_IDS)
         {
            idset = createIdSet(s, contentIds);
            q = s.getNamedQuery("pssiteitem_query_joined_items_by_tempid");
            q.setParameter("idset", idset);
         }
         else
         {
            q = s.getNamedQuery("pssiteitem_query_joined_items_by_ids");
            q.setParameterList("contentIds", contentIds);
         }

         q.setLong("siteid", siteId.longValue());
         q.setInteger("context", deliveryContext);
         return createSiteItemsList(q.list());
      }
      finally
      {
         if (idset != 0)
         {
            clearIdSet(s, idset);
         }

      }
   }

   /**
    * The actual code to implements {@link #findSiteItemsByIds(IPSGuid, int, Collection)}
    */
   public Collection<IPSSiteItem> findServerItemsByIds(IPSGuid serverId,
         int deliveryContext, Collection<Integer> contentIds)
   {
      notNull(serverId);
      notNull(contentIds);
      notEmpty(contentIds);

      Session s = getSessionFactory().getCurrentSession();
      long idset = 0;

      try
      {
         Query q;
         if (contentIds.size() >= MAX_IDS)
         {
            idset = createIdSet(s, contentIds);
            q = s.getNamedQuery("pssiteitem_server_query_joined_items_by_tempid");
            q.setParameter("idset", idset);
         }
         else
         {
            q = s.getNamedQuery("pssiteitem_server_query_joined_items_by_ids");
            q.setParameterList("contentIds", contentIds);
         }

         q.setLong("serverid", serverId.longValue());
         q.setInteger("context", deliveryContext);
         return createSiteItemsList(q.list());
      }
      finally
      {
         if (idset != 0)
         {
            clearIdSet(s, idset);
         }
      }
   }

   /**
    * A query used to find item IDs where the items have been modified since
    * last publish run, and the failed to published items.
    * The filtered IDs are specified in the IN Clause of the query.
    */
   private static String QUERY_ITEMS_SINCE_LAST_PUBLISH_IN_CLAUSE = "SELECT c.m_contentId "
         + "FROM PSSiteItem i, PSPubStatus s, PSComponentSummary c "
         + "WHERE "
         + "i.siteId = :siteId AND "
         + "i.contextId = :context AND "
         + "i.contentId = c.m_contentId AND "
         + "c.m_contentId in (:cids) AND "
         + "(s.startDate < c.m_contentLastModifiedDate OR "
         + "i.status <> 0)";
   
   /**
    * Similar as {@link #QUERY_ITEMS_SINCE_LAST_PUBLISH_IN_CLAUSE}, except
    * the filtered IDs are specified in the temp ID table.
    */
   private static String QUERY_ITEMS_SINCE_LAST_PUBLISH_TEMPIDS = "SELECT c.m_contentId "
         + "FROM PSSiteItem i, "
         + "PSPubStatus s, PSComponentSummary c, PSTempId t "
         + "WHERE "
         + "i.siteId = :siteId AND "
         + "i.contextId = :context AND "
         + "i.contentId = c.m_contentId AND "
         + "c.m_contentId = t.pk.itemId AND "
         + "t.pk.id = :idset AND "
         + "(s.startDate < c.m_contentLastModifiedDate OR "
         + "i.status <> 0)";

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPublisherService#findItemsSinceLastPublish(com.percussion.utils.guid.IPSGuid, int, java.util.Collection)
    */
   @Transactional
   public Collection<Integer> findItemsSinceLastPublish(
         IPSGuid siteId, int deliveryContext, Collection<Integer> cids)
   {
      if (cids.isEmpty())
         return new ArrayList<>();

      Session s = getSessionFactory().getCurrentSession();
      long idset = 0;
      
      try
      {
         if (cids.size() >= MAX_IDS)
         {
            idset = createIdSet(s, cids);
         }

         Collection<Integer> newIds = findNewItemsSinceLastPublish(siteId, deliveryContext, cids, idset, s);
         Collection<Integer> modifiedIds = findModifiyItemsSinceLastPublish(siteId, deliveryContext, cids, idset, s);
         modifiedIds.addAll(newIds);
         return modifiedIds;
      }
      finally
      {
         if (idset != 0)
         {
            clearIdSet(s, idset);
         }

      }
      
   }
   
   private Collection<Integer> findModifiyItemsSinceLastPublish(
         IPSGuid siteId, int deliveryContext, Collection<Integer> cids, long idset, Session s)
   {
      Query q;
      if (idset == 0)
      {
         q = s.createQuery(QUERY_ITEMS_SINCE_LAST_PUBLISH_IN_CLAUSE);
         q.setParameterList("cids", cids);
      }
      else
      {
         q = s.createQuery(QUERY_ITEMS_SINCE_LAST_PUBLISH_TEMPIDS);
         q.setParameter("idset", idset);
      }
      q.setLong("siteId", siteId.longValue());
      q.setInteger("context", deliveryContext);

      Timer swTemp = new Timer();
      List<Integer> contentIds = (idset != 0) ? (List)executeQuery(q) : q.list();

      // dedupe IDs
      Set<Integer> results = new HashSet<>();
      results.addAll(contentIds);

      if (log.isDebugEnabled())
         swTemp.logElapsed("Find " + results.size() + " modified items.");

      return results;
   }

   private static String QUERY_NEWITEMS_SINCE_LAST_PUBLISH_IN_CLAUSE = "SELECT c.m_contentId "
      + "FROM PSComponentSummary c "
      + "WHERE "
      + "c.m_contentId in (:cids) AND " 
      + "c.m_contentId NOT IN " 
         + "(select i.contentId FROM PSSiteItem i where i.siteId = :siteId AND "
         + "i.contextId = :context)";

   private static String QUERY_NEWITEMS_SINCE_LAST_PUBLISH_TEMPIDS = "SELECT c.m_contentId "
      + "FROM PSComponentSummary c, PSTempId t "
      + "WHERE "
      + "c.m_contentId = t.pk.itemId AND "
      + "t.pk.id = :idset AND "
      + "c.m_contentId NOT IN " 
         + "(select i.contentId FROM PSSiteItem i where i.siteId = :siteId AND "
         + "i.contextId = :context)";
   
   
   private Collection<Integer> findNewItemsSinceLastPublish(
         IPSGuid siteId, int deliveryContext, Collection<Integer> cids, long idset, Session s)
   {
      Query q;
      if (idset == 0)
      {
         q = s.createQuery(QUERY_NEWITEMS_SINCE_LAST_PUBLISH_IN_CLAUSE);
         q.setParameterList("cids", cids);
      }
      else
      {
         q = s.createQuery(QUERY_NEWITEMS_SINCE_LAST_PUBLISH_TEMPIDS);
         q.setParameter("idset", idset);
      }
      q.setLong("siteId", siteId.longValue());
      q.setInteger("context", deliveryContext);
      // 

      Timer swTemp = new Timer();      
      List<Integer> contentIds = (idset != 0) ? (List)executeQuery(q) : q.list();

      // dedupe IDs
      Set<Integer> results = new HashSet<>();
      results.addAll(contentIds);

      if (log.isDebugEnabled())
         swTemp.logElapsed("Found " + results.size() + " new items.");

      return results;
   }
   
   /*
    * The queries return a collection of object arrays. This method converts
    * that into site item objects that contain all the data that callers
    * require.
    * 
    * @param rows the rows, assumed never <code>null</code>.
    * @return a collection of site items, never <code>null</code>.
    */
   private Collection<IPSSiteItem> createSiteItemsList(List<PSSiteItem> rows)
   {
      Collection<IPSSiteItem> rval = new ArrayList<>();
      for(PSSiteItem row : rows)
      {
         PSSiteItem si = row;
         rval.add(si);
      }
      return rval;
   }

   @Transactional
   public Collection<IPSSiteItem> findSiteItemsAtLocation(
         IPSGuid siteid, String location)
   {
      if (siteid == null)
      {
         throw new IllegalArgumentException("siteid may not be null");
      }
      if (StringUtils.isBlank(location))
      {
         throw new IllegalArgumentException(
               "location may not be null or empty");
      }

      Session s = getSessionFactory().getCurrentSession();

         Query q = s.getNamedQuery("pssiteitem_query_at_location");
         q.setLong("siteid", siteid.longValue());
         q.setString("location", location);
         return createSiteItemsList(q.list());


   }

   /**
    * The HQL used to mark (negate) the folder-id for the site item entries with the specified folder IDs (from in-clause)
    */
   private final static String MARK_FOLDERID_4_SITEITEM_IN_FOLDERS_INCLAUSE = "update PSSiteItem set folderId = folderId * -1 "
         + "where siteId = :siteId and status = operation and referenceId in (select i.referenceId from PSSiteItem i "
         + "where i.folderId in (:folderIds))";

   /**
    * The HQL used to mark (negate) the folder-id for the site item entries with the specified folder IDs (from temp-id table)
    */
   private final static String MARK_FOLDERID_4_SITEITEM_IN_FOLDERS_TEMPID = "update PSSiteItem set folderId = folderId * -1 "
         + "where siteId = :siteId and status = operation and "
         + "referenceId in (select i.referenceId from PSSiteItem i, PSTempId t "
         + "where i.folderId = t.pk.itemId and t.pk.id = :idset)";

   /**
    * This does the same as described in {@link #markFolderIdsForMovedFolders(IPSGuid, Collection)}, 
    * but it is for non-MySQL backend repository.
    */
   private void markFolderIdsForMovedFolders_NonMySQL(IPSGuid siteid, Collection<Integer> folderIds)
   {
      Session s = getSessionFactory().getCurrentSession();
      long idset = 0;
      
      try
      {
         Query q;
         if (folderIds.size() < MAX_IDS)
         {
            q = s.createQuery(MARK_FOLDERID_4_SITEITEM_IN_FOLDERS_INCLAUSE);
            q.setParameterList("folderIds", folderIds);
         }
         else
         {
            idset = createIdSet(s, folderIds);
            
            q = s.createQuery(MARK_FOLDERID_4_SITEITEM_IN_FOLDERS_TEMPID);
            q.setParameter("idset", idset);
         }
         q.setLong("siteid", siteid.longValue());
         q.executeUpdate();
      }
      finally
      {
         if (idset != 0)
         {
            clearIdSet(s, idset);
         }

      }      
   }

   /**
    * The HQL used to get the reference IDs for the specified folder IDs (from an in clause). 
    */
   private final static String GET_REFS_4_FOLDER_IDS_INCLAUSE = "select i.referenceId from PSSiteItem i "
      + "where i.siteId = :siteid and "
      + "i.status = i.operation and i.folderId in (:folderIds))";

   /**
    * The HQL used to get the reference IDs for the specified folder IDs (from temp-id table)
    */
   private final static String GET_REFS_4_FOLDER_IDS_TEMPID = "select i.referenceId from PSSiteItem i, PSTempId t "
      + "where i.siteId = :siteid and "
      + "i.status = i.operation and i.folderId = t.pk.itemId and t.pk.id = :idset";
   
   /**
    * Gets the reference IDs for the folder IDs in the site item table.
    *
    * @param siteid the site ID, assumed not <code>null</code>.
    * @param folderIds the folder IDs, assumed not <code>null</code> or empty. 
    * @param s current session, assumed not <code>null</code>.
    *
    * @return the reference IDs, never <code>null</code>, but may be empty.
    */
   private Collection<Long> getReferencesForFolderIds(IPSGuid siteid, Collection<Integer> folderIds, Session s)
   {
      long idset = 0;
      
      try
      {
         Query q;
         if (folderIds.size() < MAX_IDS)
         {
            q = s.createQuery(GET_REFS_4_FOLDER_IDS_INCLAUSE);
            q.setParameterList("folderIds", folderIds);
         }
         else
         {
            idset = createIdSet(s, folderIds);
            
            q = s.createQuery(GET_REFS_4_FOLDER_IDS_TEMPID);
            q.setParameter("idset", idset);
         }
         q.setLong("siteid", siteid.longValue());
         List<Long> refIds = (idset != 0) ? (List)executeQuery(q) : q.list();
         return refIds;
      }
      finally
      {
         if (idset != 0)
         {
            clearIdSet(s, idset);
         }
      }      
   }
   
   /**
    * The HQL used to mark (negate) the folder-id for the site item entries with the specified reference IDs (from in-clause)
    */
   private final static String MARK_FOLDERID_4_SITEITEM_IN_REFS_INCLAUSE = "update PSSiteItem set folderId = folderId * -1 "
      + "where referenceId in (:refIds)";

   /**
    * The HQL used to mark (negate) the folder-id for the site item entries with the specified reference IDs (from temp-id table)
    */
   private final static String MARK_FOLDERID_4_SITEITEM_IN_REFS_TEMPID = "update PSSiteItem set folderId = folderId * -1 "
      + "where referenceId in (select t.pk.itemId from PSTempId t where t.pk.id = :idset)";
   
   /**
    * This does the same as described in {@link #markFolderIdsForMovedFolders(IPSGuid, Collection)}, 
    * but it is for MySQL back-end repository.
    * <p> 
    * Note, in MySQL, we have to get the reference IDs from the folder IDs first, then update the folder IDs
    * from the reference IDs. Otherwise we'll get JDBC error from MySQL: <code>SQL Error: 1093, SQLState: HY000<code>, which means
    * <code>You can't specify target table 'PSX_PUBLICATION_DOC' for update in FROM clause</code>.  
    * 
    * @param siteid the site ID, assumed not <code>null</code>.
    * @param folderIds the folder IDs, assumed not <code>null</code> or empty. 
    */
   private void markFolderIdsForMovedFolders_MySQL(IPSGuid siteid, Collection<Integer> folderIds)
   {
      Session s = getSessionFactory().getCurrentSession();
      
      long idset = 0;
      
      try
      {
         Collection<Long> refIds = getReferencesForFolderIds(siteid, folderIds, s);
         if (refIds.isEmpty())
            return;
         
         Query q;
         if (refIds.size() < MAX_IDS)
         {
            q = s.createQuery(MARK_FOLDERID_4_SITEITEM_IN_REFS_INCLAUSE);            
            q.setParameterList("refIds", refIds);
         }
         else
         {
            idset = createIdSet(s, refIds);
            
            q = s.createQuery(MARK_FOLDERID_4_SITEITEM_IN_REFS_TEMPID);
            q.setParameter("idset", idset);
         }
         q.executeUpdate();         
      }
      finally
      {
         if (idset != 0)
         {
            clearIdSet(s, idset);
         }

      }      
   }

   @Transactional
   public void markFolderIdsForMovedFolders(IPSGuid siteid, Collection<Integer> folderIds)
   {
      notNull(siteid, "siteid");
      notNull(folderIds, "folderIds");
      
      if (folderIds.isEmpty())
         return;

      if (isMySQL())
         markFolderIdsForMovedFolders_MySQL(siteid, folderIds);
      else
         markFolderIdsForMovedFolders_NonMySQL(siteid, folderIds);
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPublisherService#findEditionsBySiteAndContentListGenerator(IPSGuid, String)
    */
   @Transactional
   public List<IPSGuid> findEditionsBySiteAndContentListGenerator(
         IPSGuid siteId, String clistGenerator) 
      throws PSPublisherException
   {
      if (null == siteId)
      {
         throw new IllegalArgumentException("siteId cannot be null");  
      }
      if (StringUtils.isBlank(clistGenerator))
      {
         throw new IllegalArgumentException(
               "clistGenerator cannot be null or empty");  
      }
      
      /*
       * We use straight JDBC because some needed classes are missing for HQL 
       * and I'm not sure if it is possible to do this query in HQL (with a
       * count(*) subselect.)
       */
      final String QUERY_EDITIONS = 
         "SELECT DISTINCT E.EDITIONID"
         + " FROM" 
         + "  {0}.RXEDITION E," 
         + "  {0}.RXEDITIONCLIST EC,"
         + "  {0}.RXCONTENTLIST C"
         + " WHERE "
         + "  E.DESTSITE = ?"
         + "  AND E.EDITIONID = EC.EDITIONID"
         + "  AND C.CONTENTLISTID = EC.CONTENTLISTID"
         + "  AND C.GENERATOR = ?"
         + "  AND (SELECT COUNT(*) FROM {0}.RXEDITIONCLIST EC WHERE E.EDITIONID = EC.EDITIONID) > 1";

      Session sess = sessionFactory.getCurrentSession();
      String tablePrefix;
      Connection conn = null;

      Iterator<?> rs = null;
      try
      {
         /* fully qualify the table names - we do this by using a fake name,
          * then stripping the fake name off the returned fully qualified name
          * and using that prefix to fixup all table names in the query
          */
         tablePrefix = PSSqlHelper.qualifyTableName("rx");
         tablePrefix = tablePrefix.substring(0, tablePrefix.lastIndexOf('.'));
         String query = MessageFormat.format(QUERY_EDITIONS,
               new Object[] { tablePrefix });
         Query stmt = sess.createSQLQuery(query);

         int i = 0;
         stmt.setParameter(i++, siteId.getUUID());
         stmt.setString(i++, clistGenerator);
         rs = stmt.list().iterator();
         List<IPSGuid> results = new ArrayList<>();
         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();

         while (rs.hasNext())
         {

            results.add(gmgr.makeGuid((Integer)rs.next(), PSTypeEnum.EDITION));
         }
         return results;
      }
      catch (SQLException e)
      {
         /* Never expect this so didn't create a specific msg. */
         throw new PSPublisherException(IPSPublisherServiceErrors.RUNTIME_ERROR, 
               e.getLocalizedMessage());
      }
      }
   
   @Transactional
   public void touchContentItems(Collection<Integer> cids)
   {
      Session session = sessionFactory.getCurrentSession();

         PSTouchParentItemsHandler handler = new PSTouchParentItemsHandler(
            session);
         handler.addSpecificIds(cids);
         log.debug("List of items to update is: " + handler.toString());
         handler.touchContentItems();

   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.IPSPublisherService#touchActiveAssemblyParents(java.util.Collection)
    */
   @Transactional
   public Collection<Integer> touchActiveAssemblyParents(
         Collection<Integer> cids)
   {
      Session session = sessionFactory.getCurrentSession();

         PSTouchParentItemsHandler handler =
               new PSTouchParentItemsHandler(session);
         handler.addParents(cids);
         log.debug("List of items to update is: " + handler.toString());
         return handler.touchContentItems();

      }

   @Transactional
   public Collection<Integer> touchItemsAndActiveAssemblyParents(
         Collection<Integer> cids)
   {
      Session session = sessionFactory.getCurrentSession();

         PSTouchParentItemsHandler handler =
               new PSTouchParentItemsHandler(session);
         handler.addSpecificIds(cids);
         handler.addParents(cids);
         log.debug("List of items to update is: " + handler.toString());
         return handler.touchContentItems();

      }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.IPSPublisherService#touchActiveAssemblyParentsByGuids(java.util.Collection)
    */
   @Transactional
   public Collection<Integer> touchActiveAssemblyParentsByGuids(
         Collection<IPSGuid> cids)
   {
      Set<Integer> contentids = new HashSet<>();
      for (IPSGuid cid : cids)
      {
         PSLegacyGuid lg = (PSLegacyGuid) cid;
         contentids.add(lg.getContentId());
      }
      return touchActiveAssemblyParents(contentids);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.IPSPublisherService#touchContentTypeItems(java.util.Collection)
    */
   @Transactional
   public Collection<Integer> touchContentTypeItems(
         Collection<IPSGuid> ctypeids)
   {
      Session s = sessionFactory.getCurrentSession();

         PSTouchParentItemsHandler handler = new PSTouchParentItemsHandler(s);
         addItemsAndParentsToHandler(ctypeids, handler);
         return handler.touchContentItems();


   }

   /*
    * //see base class method for details
    */
   @Transactional
   public Collection<Integer> getContentTypeItems(
         Collection<IPSGuid> ctypeids)
   {
      Session s = sessionFactory.getCurrentSession();


         PSTouchParentItemsHandler handler = new PSTouchParentItemsHandler(s);
         addItemsAndParentsToHandler(ctypeids, handler);
         return handler.getItemIds();

   }
   
   /**
    * Adds items of the specified Content Types, and the parents of the items
    * to the given handler.
    * 
    * @param ctypeids the list IDs of Content Type for the added items, assumed
    *    not <code>null</code>.
    * @param handler the touch parent item handler, to collect the items and 
    *    their parents, assumed not <code>null</code>.
    */
   private void addItemsAndParentsToHandler(
         Collection<IPSGuid> ctypeids, PSTouchParentItemsHandler handler)
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      Set<Integer> cids = new HashSet<>();

      for (IPSGuid ctid : ctypeids)
      {
         try
         {
            cids.addAll(cms.findContentIdsByType(ctid.longValue()));
         }
         catch (PSORMException e)
         {
            log.error(
                  "Problem getting item ids for content type " + ctid);
         }
      }

      handler.addSpecificIds(cids);
      handler.addParents(cids);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.IPSPublisherService#executeDemandPublish(java.lang.String[],
    *      String, java.lang.String, boolean)
    */
   public boolean executeDemandPublish(String[] ids, String parentFolderId,
         final String edition, boolean wait) throws PSNotFoundException {
      if (ids == null)
      {
         throw new IllegalArgumentException("ids may not be null");
      }
      if (ids.length == 0)
      {
         throw new IllegalArgumentException("ids may not be empty");
      }
      if (StringUtils.isBlank(parentFolderId))
      {
         throw new IllegalArgumentException(
               "parentFolderId may not be null or empty");
      }
      if (!StringUtils.isNumeric(parentFolderId))
      {
         throw new IllegalArgumentException("parentFolderId must be numeric");
      }
      if (StringUtils.isBlank(edition))
      {
         throw new IllegalArgumentException("edition may not be null or empty");
      }
      if (!StringUtils.isNumeric(edition))
      {
         throw new IllegalArgumentException("edition must be numeric");
      }

      int contentids[] = new int[ids.length];
      int i = 0;
      for (String id : ids)
      {
         if (StringUtils.isBlank(id))
         {
            throw new IllegalArgumentException("id" + i
                  + " may not be null or empty");
         }
         if (!StringUtils.isNumeric(id))
         {
            throw new IllegalArgumentException("id" + i + " must be numeric");
         }
         contentids[i++] = Integer.parseInt(id);
      }

      final int editionid = Integer.parseInt(edition);
      int folderid = Integer.parseInt(parentFolderId);
      synchronized (m_demandids)
      {
         // Synchronized on one variable
         m_demandids.put(editionid, contentids);
         m_demandfolder.put(editionid, folderid);
      }

      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid folderguid = gmgr.makeGuid(new PSLocator(folderid));
      PSDemandWork work = new PSDemandWork();
      for(int cid : contentids)
      {
         IPSGuid contentitem = gmgr.makeGuid(new PSLocator(cid));
         work.addItem(folderguid, contentitem);
      }
      long job = m_rxpub.queueDemandWork(editionid, work);

      if (wait)
      {
         IPSPublisherJobStatus status = m_rxpub.getPublishingJobStatus(job);
         while(!status.getState().isTerminal())
         {
            try
            {
               wait(1000);
               status = m_rxpub.getPublishingJobStatus(job);
            }
            catch (InterruptedException e)
            {
               log.warn("Wait interrupted for demand publish", e);
            }
         }
      }

      return true;
   }

   
   public int getCurrentContentListItemCount(@SuppressWarnings("unused")
   String editionid)
   {
      throw new UnsupportedOperationException(
         "Use the business publisher service to get publishing status");
   }


   public int[] getDemandPublishIds(int edition)
   {
      synchronized (m_demandids)
      {
         return m_demandids.get(edition);
      }
   }

   public int getDemandFolderId(int edition)
   {
      synchronized (m_demandids)
      {
         Integer folderid = m_demandfolder.get(edition);
         return folderid == null ? 0 : folderid.intValue();
      }
   }
   
   /*
    * //see base class method for details
    */
   public IPSEdition loadEdition(IPSGuid id) throws PSNotFoundException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      IPSEdition edition = loadEditionModifiable(id);

      return edition;
   }

   /*
    * //see base class method for details
    */
   @Transactional
   public IPSEdition loadEditionModifiable(IPSGuid id)
      throws PSNotFoundException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      IPSEdition edition = (IPSEdition) sessionFactory.getCurrentSession().get(
            PSEdition.class, id.longValue());
      
      if (edition == null)
         throw new PSNotFoundException(id);
      
      return edition;
   }
   
   public IPSEditionContentList createEditionContentList()
   {
      IPSGuid id = PSGuidHelper.generateNext(PSTypeEnum.EDITION_CONTENT_LIST);
      IPSEditionContentList eclist = new PSEditionContentList(id);
      return eclist;
   }
   
   @Transactional
   public List<IPSEditionContentList> loadEditionContentLists(IPSGuid editionId)
   {
      if (editionId == null)
      {
         throw new IllegalArgumentException("editionId may not be null");
      }
      Session session = sessionFactory.getCurrentSession();
      Query query = session.createQuery("select cl " +
           "from PSEditionContentList cl " +
           "where cl.id.editionid = :edition");
      query.setParameter("edition",editionId.longValue());
      return 
              query.list();
   }

   @Transactional
   public void deleteEditionContentList(IPSEditionContentList list)
   {
      if (list == null)
      {
         throw new IllegalArgumentException("list may not be null");
      }
      getSessionFactory().getCurrentSession().delete(list);
   }
   
   public void deleteStatusList(List<IPSPubStatus> statusList)
   {
      if (statusList == null || statusList.size() == 0)
      {
         throw new IllegalArgumentException("statusList may not be null or empty");
      }
      Session s = getSessionFactory().getCurrentSession();


         for (IPSPubStatus status : statusList)
         {
            s.delete(status);
         }


   }

   @Transactional
   public void saveEditionContentList(IPSEditionContentList list)
   {
      if (list == null)
      {
         throw new IllegalArgumentException("list may not be null");
      }
      getSessionFactory().getCurrentSession().saveOrUpdate(list);
   }

   @Transactional
   public IPSDeliveryType loadDeliveryType(String dtypeName)
      throws PSNotFoundException
   {
      // @TODO cache the ID/name mapping, then we can retrieve the object
      // from cache.
      if (StringUtils.isBlank(dtypeName))
      {
         throw new IllegalArgumentException(
               "dtypeName may not be null or empty");
      }
      Session session = getSessionFactory().getCurrentSession();
      List<IPSDeliveryType> results = session.createQuery("select dt " +
              "from PSDeliveryType dt where dt.name = :name").setParameter("name",dtypeName).list();

      if (results.size() > 0)
         return results.get(0);
      
      throw new PSNotFoundException(dtypeName, PSTypeEnum.DELIVERY_TYPE);
   }
   
   public IPSDeliveryType loadDeliveryType(IPSGuid id)
      throws PSNotFoundException
   {
      if (id == null)
      {
         throw new IllegalArgumentException("id may not be null");
      }
      IPSDeliveryType dtype = loadDeliveryTypeModifiable(id);

      return dtype;
   }

   @Transactional
   public IPSDeliveryType loadDeliveryTypeModifiable(IPSGuid locationid)
      throws PSNotFoundException
   {
      if (locationid == null)
         throw new IllegalArgumentException("locationid may not be null");

      IPSDeliveryType dtype = (IPSDeliveryType) getSessionFactory().getCurrentSession()
         .get(PSDeliveryType.class, locationid.longValue());
      if (dtype == null)
         throw new PSNotFoundException(locationid);
      
      return dtype;
   }

   @Transactional(propagation=Propagation.REQUIRES_NEW)
   public void updatePublishingInfo(List<IPSPublisherItemStatus> stati)
   {
      if (stati == null || stati.size() == 0)
      {
         throw new IllegalArgumentException("stati may not be null or empty");
      }
      Session s = getSessionFactory().getCurrentSession();
      try
      {
         int count = 0;
         for (IPSPublisherItemStatus status:stati)
         {
            // Note: this updates PSPubItem, which is OK.  Also updates PSSiteItem
            // as well down below so both are kept in sync.
            PSPubItem pubItem = createOrUpdatePubItem(status, s);
            s.saveOrUpdate(pubItem);
            if (status.getState().equals(ItemState.DELIVERED))
            {
               if (pubItem.getOperation().equals(IPSSiteItem.Operation.UNPUBLISH))
               {
                  PSSiteItem wrapperItem = new PSSiteItem();
                  wrapperItem.referenceId = pubItem.getUnpublishRefId();
                  s.delete(wrapperItem);
               }  
               else
               {
                  PSSiteItem siteItem = createSiteItem(status,pubItem,s);
                  s.saveOrUpdate(siteItem);
               }
            }
            if ( ++count % BATCH_SIZE == 0 ) {
               //flush a batch of updates and release memory:
               s.flush();
               s.clear();
           }
            
         }
      }
      catch(Exception e)
      {
         log.error("Problem updating publishing info", e);
      }

      }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPublisherService#updateItemPubDateByJob(long)
    */
   @Transactional
   public void updateItemPubDateByJob(long jobId, Date date)
   {
       Session s = getSessionFactory().getCurrentSession();
       
       // could iterate on findPubItemStatusForJobIterable(jobId); but only
       // need id this should be quicker.
       // 
       // NOTE: this method queries the PSPubItem class (not PSSiteItem) after the 
       // publishing schema updates.  This is also OK because the component summary gets updated
       // from the result of the PSPubItem and later all publishing tables get updated correctly.
       Criteria criteria = s.createCriteria(PSPubItem.class);
       criteria.add(Restrictions.eq("statusId", jobId));
       criteria.add(Restrictions.eq("status", (short) Status.SUCCESS.ordinal()));
       criteria.setProjection(Projections.property("contentId"));

       List<Integer> ids = criteria.list();

       IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();

        objMgr.setPublishDate(ids,date);

   }

   /**
    * Create or update the pub items. For most data in the item status, the
    * record will only be updated if the corresponding element is specified.
    * Note that the following data must be present on every message to ensure
    * that a record can be created:
    * <ul>
    * <li>reference id
    * <li>job id
    * <li>content guid
    * <li>folder guid 
    * <li>context
    * <li>site id
    * <li>template id
    * </ul>
    * State data must always be present.
    * <p>
    * This method takes a list of status records. It loads the corresponding
    * pub items if they exist. Then it updates the records that exist and 
    * creates the records that do not exist. Then the collection of records
    * is persisted to the database.
    * 
    * @param stati the item statuses, assumed never <code>null</code>.
    * @param s the hibernate session, assumed never <code>null</code>.
    * 
    * @return a list of persisted publish items, never <code>null</code>, may
    *    be empty.
    */
   /*
   private List<IPSPublisherItemStatus> createOrUpdatePubItems(
         List<IPSPublisherItemStatus> stati, Session s, Map<Long,IPSPubItemStatus> imap)
   {
      List<IPSPublisherItemStatus> itemStatuses = new ArrayList<IPSPublisherItemStatus>();
      for(IPSPublisherItemStatus status : stati)
      {         
         PSPubItem pubItem = getPubItem(status, imap);
         if (pubItem != null)
         {
            s.saveOrUpdate(pubItem);
            itemStatuses.add(status);
         }
      }
      return itemStatuses;
   }
   */

   /**
    * Get a to be persisted publish item from a item status
    * 
    * Note: Site item does not need to be updated at this point as it will get updated on successful publish
    * or unpublish.  Basically after it is delivered.
    * 
    * @param status the current status of the publishing item, assumed not 
    *    <code>null</code>.
    * @param s the session
    *    assumed not <code>null</code>.
    * 
    * @return the publish item, may be <code>null</code> if unable to create
    *    one by missing data.
    */
   private PSPubItem createOrUpdatePubItem(IPSPublisherItemStatus status, Session s)
   {
      PSPubItem pubItem = (PSPubItem)s.get(PSPubItem.class, status.getReferenceId());
      
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      
      if (! status.getState().isPersistable())
      {
         log.warn(
               "Request to persist an item in unexpected state: " + status);
         return null;
      }
      
      PSLocator folder = null;
      if (status.getFolderId() != null)
      {
         folder = gmgr.makeLocator(status.getFolderId());
      }
      
      if (pubItem == null)
      { 
         IPSGuid cid = status.getId();
         if (cid == null)
         {
            log.warn("No content id in status: " + status);
            return null;
         }
         PSLocator loc = gmgr.makeLocator(cid);
         pubItem = new PSPubItem();
         pubItem.referenceId = status.getReferenceId();
         pubItem.contentId = loc.getId();
         pubItem.revisionId = loc.getRevision();
         pubItem.statusId = status.getJobId();
         pubItem.page = status.getPage();
         pubItem.version = 0;
         
         if (status.getTemplateId() != null)
         {
            pubItem.templateId = status.getTemplateId().longValue();
         }
         pubItem.deliveryType = status.getDeliveryType();
         if (status.isPublish())
            pubItem.operation = (short) Operation.PUBLISH.ordinal();
         else
            pubItem.operation = (short) Operation.UNPUBLISH.ordinal();
         pubItem.unpublishRefId = status.getUnpublishRefId();
         
      }

      if (folder != null)
      {
         pubItem.folderId = folder.getId();
      }
      
      if (status.getAssemblyUrl() != null)
      {
         pubItem.assemblyUrl = status.getAssemblyUrl();
      }
      
      if (status.getPublishedDate() != null)
      {
         pubItem.date = status.getPublishedDate();
      }
      
      if (status.getPublishedLocation() != null)
      {
         pubItem.location = status.getPublishedLocation();
      }

      if (status.getElapsed() >= 0)
      {
         pubItem.elapsed = status.getElapsed();
      }
      
      if (status.getPage() != null)
      {
         pubItem.page = status.getPage();
      }
      
      if (status.getState().equals(ItemState.DELIVERED))
         pubItem.status = (short) Status.SUCCESS.ordinal();
      else if (status.getState().equals(ItemState.FAILED))
         pubItem.status = (short) Status.FAILURE.ordinal();
      else if (status.getState().equals(ItemState.CANCELLED))
         pubItem.status = (short) Status.CANCELLED.ordinal();
      
      if (status.getMessages() != null && status.getMessages().length > 0)
      {
         // Messages are appended to the clob with simple separators
         for(String message : status.getMessages())
         {
            if (pubItem.message == null)
               pubItem.message = message;
            else
               pubItem.message += MESSAGE_SEPARATOR + message;
         }
      }
      
      return pubItem;
   }
   
   /**
    * Creates a Site Item from the given item status.
    * 
    * @param status the source of the item status passed in for updating, 
    *    assumed not <code>null</code>. 
    * @param pubItem the current instance of the item status that is already
    *    saved in the publication doc/log. Assumed not <code>null</code>.
    * @param s the session, assumed not <code>null</code>.
    * 
    * @return the created Site Item, never <code>null</code>.
    */
   private PSSiteItem createSiteItem(IPSPublisherItemStatus status,
         PSPubItem pubItem, Session s)
   {
      int contentId = pubItem.getContentId();
      long templateid = pubItem.getTemplateId().longValue();

      Integer folderId = pubItem.getFolderId();
      Integer page = pubItem.page;
    
      // If we haven't found it this run, then look it up based on all
      // the information we have available. We may get multiple records
      // as the item may be published across more than one site.
      Criteria crit = s.createCriteria(PSSiteItem.class);
      crit.add(Restrictions.eq("contentId", contentId));
      if (folderId != null)
      {
         crit.add(Restrictions.eq("folderId", folderId));
      }
      else
      {
         crit.add(Restrictions.isNull("folderId"));
      }
      crit.add(Restrictions.eq("templateId", templateid));
      if (page != null)
      {
         crit.add(Restrictions.eq("page", page));
      }
      else
      {
         crit.add(Restrictions.isNull("page"));
      }
      
      crit.add(Restrictions.eq("siteId", status.getSiteId().longValue()));
      crit.add(Restrictions.eq("contextId", status.getDeliveryContext()));
      if (status.getPubServerId() != null)
         crit.add(Restrictions.eq("serverId", status.getPubServerId()));
      
      PSSiteItem siteItem = null;
      try {
         siteItem = (PSSiteItem) crit.uniqueResult();
      } catch (RuntimeException e)
      {
         log.error("Non unique site item entry",e);
         throw e;
      }
      if (siteItem != null)
      {
         s.delete(siteItem);
         // Make this a new item
         siteItem.referenceId = status.getReferenceId();
      }
      else
      {
         // Or create the site item
         siteItem = new PSSiteItem();
         siteItem.referenceId = status.getReferenceId();
         siteItem.contextId = status.getDeliveryContext();
         siteItem.siteId = status.getSiteId().longValue();
         siteItem.serverId = status.getPubServerId();
      }
      
      if (status.getUnpublishingInformation() != null)
         siteItem.unpublishInfo = status.getUnpublishingInformation();
      siteItem.status = (short) pubItem.getStatus().ordinal();
      siteItem.statusId = pubItem.getStatusId();
      siteItem.contentId = contentId;
      siteItem.revisionId = pubItem.getRevisionId();
      siteItem.folderId = pubItem.getFolderId();
      siteItem.templateId = pubItem.getTemplateId();
      siteItem.location = pubItem.getLocation();
      siteItem.date = pubItem.getDate();
      siteItem.operation = (short) pubItem.getOperation().ordinal();
      siteItem.assemblyUrl = pubItem.getAssemblyUrl();
      siteItem.elapsed = pubItem.getElapsed();
      siteItem.page = page;
      siteItem.deliveryType = pubItem.getDeliveryType();
      siteItem.unpublishRefId = pubItem.getUnpublishRefId();
      return siteItem;
   }
   
   @Transactional
   public void initPublishingStatus(long statusid, Date start, IPSGuid edition) throws PSNotFoundException {
      if (start == null)
      {
         throw new IllegalArgumentException("start may not be null");
      }
      if (edition == null)
      {
         throw new IllegalArgumentException("edition may not be null");
      }
      if (edition.getType() != PSTypeEnum.EDITION.getOrdinal())
      {
         throw new IllegalArgumentException(
               "edition guid not of the right type");
      }
      
      IPSEdition editionPublishing = loadEdition(edition);
      
      PSPubStatus stat = new PSPubStatus(statusid, edition.getUUID(), start);
      String name = getServerId(); 
      stat.setServer(name);
      if (editionPublishing.getPubServerId() != null)
      {
         stat.setPubServerId(editionPublishing.getPubServerId().longValue());
      }
      getSessionFactory().getCurrentSession().save(stat);
   }

   public String getServerId()
   {
      String name = PSServer.getServerName() + ":" + PSServer.getListenerPort();
      return name;
   }
   
   @Transactional
   public void finishedPublishingStatus(long statusid, Date end,
         IPSPubStatus.EndingState endingStatus)
   {
      assert end != null;
      assert endingStatus != null;
      
      PSPubStatus stat = 
         (PSPubStatus) getSessionFactory().getCurrentSession().get(PSPubStatus.class, statusid);
      stat.setEndDate(end);
      stat.setEndingStatus(endingStatus);
      getCountsForPubStatus(stat);
      getSessionFactory().getCurrentSession().update(stat);
   }
   
   @Transactional
   public IPSPubStatus updateCounts(long statusid) 
   {
      PSPubStatus stat = 
         (PSPubStatus) getSessionFactory().getCurrentSession().get(PSPubStatus.class, statusid);
      getCountsForPubStatus(stat);
      getSessionFactory().getCurrentSession().update(stat);
      return stat;
   }

   @Transactional
   public void deleteDeliveryType(IPSDeliveryType deliveryType)
   {
      if (deliveryType == null)
         throw new IllegalArgumentException("deliveryType may not be null");

      getSessionFactory().getCurrentSession().delete(deliveryType);

   }

   @Transactional
   public void deleteEdition(IPSEdition edition)
   {
      if (edition == null)
      {
         throw new IllegalArgumentException("edition may not be null");
      }
      deleteEditionTasks(edition);
      deleteEditionContentLists(edition);
      getSessionFactory().getCurrentSession().delete(edition);

   }

   /**
    * Deletes all the tasks for the edition.
    * @param edition the edition to delete tasks for.
    * Assumed not <code>null</code>.
    */
   private void deleteEditionTasks(IPSEdition edition)
   {
      List<IPSEditionTaskDef> tasks = 
         loadEditionTasks(edition.getGUID());
      for(IPSEditionTaskDef task : tasks)
      {
         deleteEditionTask(task);
      }
   }

   /*
    * Deletes all the content list associations for the edition.
    * @param edition the edition to delete content lists for.
    * Assumed not <code>null</code>.
    */
   private void deleteEditionContentLists(IPSEdition edition)
   {
      final List<IPSEditionContentList> contentLists =
            loadEditionContentLists(edition.getGUID());
      for (final IPSEditionContentList contentList : contentLists)
      {
         deleteEditionContentList(contentList);
      }
   }

   @Transactional
   public List<IPSContentList> findAllContentListsBySite(IPSGuid siteId) throws PSNotFoundException {
      if (siteId == null)
      {
         throw new IllegalArgumentException("siteId may not be null");
      }
      
      List<IPSContentList> result = getSessionFactory().getCurrentSession().createQuery(
            "select distinct c " +
            "from PSContentList c, PSEditionContentList ecl, PSEdition e " +
            "where ecl.pk.contentlistid = c.contentListId and " +
            "e.editionid = ecl.pk.editionid and " +
            "e.destsite = :siteid").setParameter("siteid",
            siteId.longValue()).list();
      for (IPSContentList clist : result)
      {
         loadItemFilterIfNeeded(clist);
      }
      return result;
   }

   @Transactional
   public List<IPSEdition> findAllEditionsBySite(IPSGuid siteId)
   {
      if (siteId == null)
      {
         throw new IllegalArgumentException("siteId may not be null");
      }
      
      return getSessionFactory().getCurrentSession().createQuery(
            "select e from PSEdition e where e.destsite = :siteid").setParameter(
            "siteid", siteId.longValue()).list();
   }


   public List<IPSEdition> findAllEditionsByPubServer(IPSGuid pubServerId)
   {
      if (pubServerId == null)
      {
         throw new IllegalArgumentException("pubServerId may not be null");
      }
      
      return getSessionFactory().getCurrentSession().createQuery(
            "select e from PSEdition e where e.pubserver = :pubServerId").setParameter(
            "pubServerId", pubServerId.longValue()).list();
   }
   
   @SuppressWarnings("unchecked")
   public IPSEdition findEditionByName(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      Session s = getSessionFactory().getCurrentSession();

         Criteria c = s.createCriteria(PSEdition.class);
         c.add(Restrictions.eq("displaytitle", name));
         List results = c.list();
         if (results.size() == 0)
         {
            return null;
         }
         return (IPSEdition) results.get(0);


      }

   @Transactional
   public List<IPSEdition> findAllEditions(String filter)
   {
      if (filter == null)
      {
         throw new IllegalArgumentException("filter may not be null");
      }
      Session s = getSessionFactory().getCurrentSession();

         Criteria c = s.createCriteria(PSEdition.class);
         if (!StringUtils.isBlank(filter))
         {
            c.add(Restrictions.ilike("displaytitle", "%" + filter + "%"));
         }
         c.addOrder(Order.asc("displaytitle"));
         c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
         List results = c.list();
         return results;

      }

   @Transactional
   public void saveDeliveryType(IPSDeliveryType deliveryType)
   {
      if (deliveryType == null)
         throw new IllegalArgumentException("deliveryType may not be null");

      getSessionFactory().getCurrentSession().saveOrUpdate(deliveryType);

   }

   /*
    * //see base class method for details
    */
   @Transactional
   public void saveEdition(IPSEdition edition)
   {
      if (edition == null)
         throw new IllegalArgumentException("edition may not be null");
      
      getSessionFactory().getCurrentSession().saveOrUpdate(edition);

   }

   public IPSDeliveryType createDeliveryType()
   {
      IPSDeliveryType dt = new PSDeliveryType();
      dt.setGUID(PSGuidHelper.generateNext(PSTypeEnum.DELIVERY_TYPE));
      return dt;
   }

   public IPSEditionTaskDef createEditionTask()
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid id = gmgr.createGuid(PSTypeEnum.EDITION_TASK_DEF);
      IPSEditionTaskDef task = new PSEditionTaskDef(id);
      return task;
   }

   @Transactional
   public void deleteEditionTask(IPSEditionTaskDef task)
   {
      if (task == null)
      {
         throw new IllegalArgumentException("task may not be null");
      }
      getSessionFactory().getCurrentSession().delete(task);
   }

   @Transactional
   public List<IPSEditionTaskDef> loadEditionTasks(IPSGuid editionid)
   {
      return getSessionFactory().getCurrentSession()
         .createQuery(
               "from PSEditionTaskDef where editionId = :edition " +
               "order by sequence asc").setParameter(
               "edition", editionid.longValue()).list();
   }

   @Transactional
   public IPSEditionTaskDef findEditionTaskById(IPSGuid id) 
      throws PSNotFoundException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      return (IPSEditionTaskDef) getSessionFactory().getCurrentSession()
         .get(PSEditionTaskDef.class, id.longValue());
   }

   @Transactional
   public void saveEditionTask(IPSEditionTaskDef task)
   {
      if (task == null)
      {
         throw new IllegalArgumentException("task may not be null");
      }
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      PSEditionTaskDef data = (PSEditionTaskDef) task;
      
      // Fix any properties that have no assigned ids
      for(PSEditionTaskParam p : data.getInternalParams())
      {
         if (p.getTaskParamId() == null)
         {
            p.setTaskParamId(gmgr.createLongId(PSTypeEnum.INTERNAL));
         }
      }
      
      getSessionFactory().getCurrentSession().saveOrUpdate(task);
   }
   
   public IPSEdition createEdition()
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      PSEdition ed = new PSEdition();
      ed.setGUID(gmgr.createGuid(PSTypeEnum.EDITION));
      return ed;
   }

   public IPSEditionTaskLog createEditionTaskLog()
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      PSEditionTaskLog log = new PSEditionTaskLog();
      log.setReferenceId(gmgr.createLongId(PSTypeEnum.PUB_REFERENCE_ID));
      return log;
   }

   @Transactional
   public List<IPSEditionTaskLog> findEditionTaskLogEntriesByJobId(long jobid)
   {
      Session s = getSessionFactory().getCurrentSession();

         Criteria c = s.createCriteria(PSEditionTaskLog.class);
         c.add(Restrictions.eq("jobId", jobid));
         c.addOrder(Order.asc("referenceId"));
         return c.list();


      }

   @Transactional
   public List<Long> findExpiredJobs(Date beforeDate)
   {
      notNull(beforeDate);
      
      Session s = getSessionFactory().getCurrentSession();

         Criteria c = s.createCriteria(PSPubStatus.class);
         c.add(Restrictions.le("endDate", beforeDate));
         c.setProjection(Projections.property("statusId"));
         return c.list();

      }

   @Transactional
   public List<Long> findExpiredAndHiddenJobs(Date beforeDate)
   {
      notNull(beforeDate);
      
      Session s = getSessionFactory().getCurrentSession();

         Criteria c = s.createCriteria(PSPubStatus.class);
         c.add(Restrictions.or(Restrictions.le("endDate", beforeDate),
               Restrictions.eq("hidden", Character.valueOf('Y'))));
         c.setProjection(Projections.property("statusId"));
         return c.list();

      }
   
   @Transactional
   public IPSEditionTaskLog loadEditionTaskLog(long referenceId)
   {
      return (IPSEditionTaskLog) getSessionFactory().getCurrentSession().get(
            PSEditionTaskLog.class, referenceId);
   }

   /*
    * //see base interface method for details
    */
   @Transactional
   public void cancelUnfinishedJobItems(long jobId)
   {
      Session s = getSessionFactory().getCurrentSession();

         String sql = "update PSPubItem set status = :statusid " + 
            "where statusId = :job and status <> :success and status <> :failure";
         Query q = s.createQuery(sql);
         q.setShort("statusid", (short)IPSSiteItem.Status.CANCELLED.ordinal());
         q.setShort("success", (short)IPSSiteItem.Status.SUCCESS.ordinal());
         q.setShort("failure", (short)IPSSiteItem.Status.FAILURE.ordinal());
         q.setLong("job", jobId);
         q.executeUpdate();

      }
   
   @Transactional
   public void purgeJobLog(long jobId)
   {
      // First disconnect records belonging to the given jobId. Then find
      // all records that are disconnected and not in use by the site items
      // table and remove them
      Session s = getSessionFactory().getCurrentSession();

         Query q = s.createQuery(
               "update PSPubStatus set hidden = 'Y' where statusId = :jobId");
         q.setLong("jobId", jobId);
         q.executeUpdate();

         deletePublicationDocs(s, jobId);
         
         q = s.createQuery(
               "delete from PSPubStatus where statusId = :jobId and statusId not in " +
                  "(select distinct p.statusId from PSSiteItem s, PSPubItem p " +
                     "where s.referenceId = p.referenceId and p.statusId = :jobId)");
         q.setLong("jobId", jobId);
         q.executeUpdate();

         q = s.createQuery("delete from PSEditionTaskLog where jobId = :jobId");
         q.setLong("jobId", jobId);
         q.executeUpdate();


      }

   /**
    * Delete publish logs from publication doc for the specified job
    * 
    * @param s the opened session
    * @param jobId the id of the job.
    */
   private void deletePublicationDocs(Session s, long jobId)
   {
      // Note: no need to remove from site item here after performance changes either.
      Query q = s.createQuery(
            "select referenceId from PSPubItem where statusId = :jobId and " +
            "referenceId not in (select s.referenceId from PSSiteItem s, PSPubItem p " +
            "where s.referenceId = p.referenceId and p.statusId = :jobId)");
      q.setLong("jobId", jobId);
      List<Long> references = q.list();
      
      if (references == null || references.isEmpty())
      {
         return;
      }
      
      if (references.size() < MAX_IDS)
      {
         deletePublicationDocs(s, references, jobId);
      }
      else
      {
         // we need to paginate the query to avoid oracle problems
         for (int i = 0; i < references.size(); i += MAX_IDS)
         {
            int end = (i + MAX_IDS > references.size()) ? references.size() : i + MAX_IDS;
            deletePublicationDocs(s, references.subList(i, end), jobId);
         }
      }
   }

   /**
   * Delete publish logs from publication doc for the specified entries. 
   * 
   * @param s the opened session
   * @param references the entry IDs to be deleted, assumed not <code>null</code> or empty.
   * @param jobId the id of the job.
   */
   private void deletePublicationDocs(Session s, List<Long> references, Long jobId)
   {
      Query q = s.createQuery(
            "delete from PSPubItem where statusId = :jobId and " +
            "referenceId in (" + join(references, ",") + ") ");
      q.setLong("jobId", jobId);
      q.executeUpdate();
   }

   @Transactional
   public void saveEditionTaskLog(IPSEditionTaskLog log)
   {
      getSessionFactory().getCurrentSession().saveOrUpdate(log);
   }
   @Transactional
   public List<Long> findRefIdForJob(long jobid, List<PSSortCriterion> sort)
   {
      Session session = getSessionFactory().getCurrentSession();

         Criteria c = session.createCriteria(PSPubItem.class);
         
         // set the where clause
         c.add(Restrictions.eq("statusId", jobid));
         
         // set the selection clause
         ProjectionList list = Projections.projectionList();
         list.add(Projections.property("referenceId"));
         c.setProjection(list);

         // set the order by clause
         if (sort != null)
         {
            for(PSSortCriterion s : sort)
            {
               if (s.isAscending())
                  c.addOrder(Order.asc(s.getProperty()));
               else
                  c.addOrder(Order.desc(s.getProperty()));
            }
         }
         
         List<Long> values = c.list();
         return values;

      }
   
   @Transactional
   public List<IPSPubItemStatus> findPubItemStatusForJob(long jobid)
   {
      Session session = getSessionFactory().getCurrentSession();

         Criteria c = session.createCriteria(PSPubItem.class);
         c.add(Restrictions.eq("statusId", jobid));
         List<IPSPubItemStatus> values = c.list();
         return values;

   }
   
   @Transactional
   public Iterable<IPSPubItemStatus> findPubItemStatusForJobIterable(long jobid)
   {
      Session session = getSessionFactory().getCurrentSession();
      try
      {
         Query q =session.createQuery("select p from PSPubItem p where p.statusId = :statusId");
         q.setParameter("statusId", new Long(jobid));
         Iterator<IPSPubItemStatus> values = q.iterate();
         PSXStreamObjectStream<IPSPubItemStatus> rvalue = new PSXStreamObjectStream<>();
         rvalue.writeObjects(values);
         return rvalue;
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }

   }

   @Transactional
   public List<IPSPubStatus> findPubStatusBySite(IPSGuid siteId)
   {
      List<IPSEdition> editions = findAllEditionsBySite(siteId);
      List<Long> editionIds = new ArrayList<>();
      if (editions.isEmpty())
         return Collections.EMPTY_LIST;
      
      for(IPSEdition e : editions)
      {
         editionIds.add(((PSEdition) e).getId());
      }
      return findPubStatusByEditionList(editionIds);
   }
   
   @Override
   @Transactional
   public List<IPSPubStatus> findPubStatusBySiteWithFilters(IPSGuid siteId, int numDays, int maxCount)
   {
      List<IPSEdition> editions = findAllEditionsBySite(siteId);
      List<Long> editionIds = new ArrayList<>();
      if (editions.isEmpty())
         return Collections.EMPTY_LIST;
      
      for(IPSEdition e : editions)
      {
         editionIds.add(((PSEdition) e).getId());
      }
      return findPubStatusByEditionListWithFilters(editionIds, numDays, maxCount);
   }
   
   public List<IPSPubStatus> findPubStatusBySiteAndServer(IPSGuid siteId, IPSGuid pubServerId)
   {
      List<IPSPubStatus> statusList = findAllPubStatusBySiteAndServer(siteId, pubServerId, -1, -1);
      
      if (statusList == null || statusList.isEmpty())
         return Collections.EMPTY_LIST;
      else
         return statusList;
   }
   
   @Override
   public List<IPSPubStatus> findPubStatusBySiteAndServerWithFilters(IPSGuid siteId, IPSGuid serverId, int numDays,
         int maxCount)
   {
      List<IPSPubStatus> statusList = findAllPubStatusBySiteAndServer(siteId, serverId, numDays, maxCount);
      
      if (statusList == null || statusList.isEmpty())
         return Collections.EMPTY_LIST;
      else
         return statusList;
   }

   @Transactional
   public List<IPSPubStatus> findPubStatusByEdition(IPSGuid edition)
   {
      if (edition == null)
      {
         throw new IllegalArgumentException("edition may not be null");
      }
      return findPubStatusByEditionList(
            Collections.singletonList(edition.longValue()));
   }
   
   @Transactional
   public List<IPSPubStatus> findAllPubStatus()
   {
      return findPubStatusByEditionList(Collections.EMPTY_LIST);
   }
   
   @Override
   @Transactional
   public List<IPSPubStatus> findAllPubStatusWithFilters(int days, int maxCount)
   {
      return findPubStatusByEditionListWithFilters(Collections.EMPTY_LIST, days, maxCount);
   }
   
   /**
    * Retrieves a list of publication status.
    * @param siteId the id of the site
    * @param serverId the edition server name
    * @param days the number of days in past to filter by. UI displays 3, 5, and 10 days. Can be -1 to void property.
    * @param maxCount the max number of items to return.  UI displays 20, 30, and 50. -1 Can be used to void max property.
    * @return a list of IPSPubStatus
    */
   public List<IPSPubStatus> findAllPubStatusBySiteAndServer(IPSGuid siteId, IPSGuid serverId, int days, int maxCount)
   {
      Session s = getSessionFactory().getCurrentSession();
      Calendar cal = Calendar.getInstance();
      List<IPSPubStatus> stati = new ArrayList<>();
      
      if (days != -1)
          cal.add(Calendar.DAY_OF_YEAR, -days);
      Date fromDate = cal.getTime();
      
      if (siteId == null)
      {
         throw new IllegalArgumentException("siteId may not be null");
      }
      if (serverId == null)
      {
         throw new IllegalArgumentException("serverId may not be null");
      }
         
      String sqlQuery = "select status from PSPubStatus status, PSPubServer server "
            + "where server.siteId = :siteid and server.serverId = :serverid and "
            + "server.serverId = status.pubServerId and status.hidden is null ";
      
      if (days != -1)
         sqlQuery += "and status.startDate >= :fromDate ";
      
      sqlQuery += "order by status.startDate desc";
      
      log.debug("Query is: " + sqlQuery);
      
      Query query = null;
      try 
      {
          query = s.createQuery(sqlQuery);
      }
      catch (HibernateException hibernateException)
      {
          log.error(hibernateException.getMessage());
      }
      
      query.setParameter("siteid", siteId.longValue());
      query.setParameter("serverid", serverId.longValue());
      if (days != -1) {
         query.setParameter("fromDate", fromDate);
      }
      
      if (maxCount != -1) {
         query.setMaxResults(maxCount);
      }
      
      log.debug("HQL query string is: " + query.getQueryString());
      List<Object[]> result = query.list();
      for(Object r : result) {
          stati.add((IPSPubStatus)r);
      }
      
      return stati;
   }

   /**
    * Finds the last successful publish status for the specified item.
    * 
    * @param id the ID of the item in question, never <code>null</code>.
    * 
    * @return the published status of the specified item. It may be
    * <code>null</code> if the item has not been published successfully.
    */
   public IPSPubItemStatus findLastPublishedItemStatus(IPSGuid id)
   {
      notNull(id, "id cannot be null.");
      if (!(id instanceof PSLegacyGuid))
      {
         throw new IllegalArgumentException(
               "id may not be null and must be an instance of PSLegacyGuid");
      }

      PSLegacyGuid lguid = (PSLegacyGuid) id;

      // Note: query to pub item or siteitem doesn't matter here as they both
      // will have updated information if the job successful.
      Session session = getSessionFactory().getCurrentSession();

      Short publishOp = new Short((short)IPSSiteItem.Operation.PUBLISH.ordinal());
      Short success = new Short ((short)IPSSiteItem.Status.SUCCESS.ordinal());
      Criteria c = session.createCriteria(PSPubItem.class).add(
            Restrictions.eq("contentId", new Integer(lguid.getContentId())))
            .add(Restrictions.eq("operation", publishOp))
            .add(Restrictions.eq("status", success))
            .addOrder(Order.desc("date"));
      c.setMaxResults(1);
      List<IPSPubItemStatus> results = c.list();

      return results.isEmpty() ? null : results.get(0);
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPublisherService#findLastPublishedItemStatus(com.percussion.utils.guid.IPSGuid, java.util.List)
    */
   public IPSPubItemStatus findLastPublishedItemStatus(IPSGuid id, Long serverId)
   {
      notNull(id, "id cannot be null.");
      if (!(id instanceof PSLegacyGuid))
      {
         throw new IllegalArgumentException(
               "id may not be null and must be an instance of PSLegacyGuid");
      }
      notNull(serverId);
      IPSPubItemStatus pubItemStatus = null;
      List<IPSPubStatus> pubStatuses = findPubStatusByServer(serverId);
      if(!pubStatuses.isEmpty())
      {
         List<Long> jobIds = new ArrayList<>();
         for (IPSPubStatus ipsPubStatus : pubStatuses)
         {
            jobIds.add(ipsPubStatus.getStatusId());
         }
         PSLegacyGuid lguid = (PSLegacyGuid) id;

         Session session = getSessionFactory().getCurrentSession();

            Short publishOp = new Short((short)IPSSiteItem.Operation.PUBLISH.ordinal());
            Short success = new Short ((short)IPSSiteItem.Status.SUCCESS.ordinal());
            Criteria c = session.createCriteria(PSPubItem.class)
                  .add(Restrictions.eq("contentId", new Integer(lguid.getContentId())))
                  .add(Restrictions.eq("operation", publishOp))
                  .add(Restrictions.eq("status", success))
                  .addOrder(Order.desc("date"));
            if (jobIds.size() == 1)
            {
               c.add(Restrictions.eq("statusId", jobIds.get(0)));
            }
            else
            {
               c.add(Restrictions.in("statusId", jobIds));
            }            
            c.setMaxResults(1);
            List<IPSPubItemStatus> results = c.list();

            pubItemStatus = results.isEmpty() ? null : results.get(0);

         
      }
      return pubItemStatus;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPublisherService#findItemPublishingHistory(com.percussion.utils.guid.IPSGuid)
    */
   public List<PSItemPublishingHistory> findItemPublishingHistory(IPSGuid id)
   {
      notNull(id, "id cannot be null.");
      List<PSItemPublishingHistory> results = new ArrayList<>();
      if (!(id instanceof PSLegacyGuid))
      {
         throw new IllegalArgumentException(
               "id may not be null and must be an instance of PSLegacyGuid");
      }
      
      PSLegacyGuid lguid = (PSLegacyGuid) id; 
      
      Session session = getSessionFactory().getCurrentSession();

         Query q =session.createQuery("select sr.name, p.date, p.operation, p.status, p.message, p.statusId from PSPubItem p, PSPubStatus s, PSPubServer sr where p.statusId = s.statusId and s.pubServerId = sr.serverId and p.contentId = :contentId order by p.date desc");
         q.setParameter("contentId", new Integer(lguid.getContentId()));
         List<Object[]> rows = q.list();
         Set<Integer> statusIds = new HashSet<>();
         for(Object[] row : rows){
            int statusId = Integer.valueOf(StringUtils.EMPTY + row[5]);
            if(!statusIds.contains(statusId)) {
               String srvName = StringUtils.EMPTY + row[0];
               Date date = (Date)row[1];
               String operation = "Error";
               try{
                  int op = Integer.valueOf(StringUtils.EMPTY + row[2]);
                  operation = (IPSSiteItem.Operation.values()[op]).toString();
               }
               catch(Exception e){
                  //This should not happen, in case happens log the details and send the operation as error
                  log.error("Error occurred converting the operation for publishing entry for content id: "
                  + lguid.toString() + " and publishing date " + date.toString(), e);
               }
               String status = "Error";
               try{
                  int st = Integer.valueOf(StringUtils.EMPTY + row[3]);
                  status = (IPSSiteItem.Status.values()[st]).toString();
               }
               catch(Exception e){
                  //This should not happen, in case happens log the details and send the operation as error
                  log.error("Error occurred converting the status for publishing entry for content id: "
                  + lguid.toString() + " and publishing date " + date.toString(), e);
               }
               String message = StringUtils.EMPTY + row[4];
               PSItemPublishingHistory ph = new PSItemPublishingHistory(srvName,date,status,operation, message);
               results.add(ph);
            }
            statusIds.add(statusId);
         }

      return results;
   }
   /**
    * Factored common code for retrieving job status.
    * Setting days or maxCount will pass in limits on the returned data
    * from the SQL query.
    * 
    * @param editionids one ore more editions, assumed never <code>null</code>.
    * @param days the number of days to limit by. Can be set to -1 to void the property.
    * @param maxCount the maximum number of entries to return. Can be set to -1 to void the property.
    * @return a list of results, never <code>null</code>.
    */
   private List<IPSPubStatus> findPubStatusByEditionListWithFilters(List<Long> editionids, int days, int maxCount)
   {
      Session session = getSessionFactory().getCurrentSession();
      Criteria c = session.createCriteria(PSPubStatus.class);

      Calendar cal = Calendar.getInstance();
      if (days != -1)
          cal.add(Calendar.DAY_OF_YEAR, -days);
      Date fromDate = cal.getTime();

      if (editionids.size() == 1)
      {
         c.add(Restrictions.eq("editionId", editionids.get(0)));
      }
      else if (editionids.size() > 0)
      {
         c.add(Restrictions.in("editionId", editionids));
      }
      if (days != -1) {
         c.add(Restrictions.gt("startDate", fromDate));
      }
      c.add(Restrictions.isNull("hidden"));
      c.add(Restrictions.isNotNull("startDate"));
      c.addOrder(Order.desc("startDate"));
      if (maxCount != -1) {
         c.setMaxResults(maxCount);
      }
      return c.list();
   }
   
   /**
    * Factored common code for retrieving job status.
    * 
    * @param editionids one ore more editions, assumed never <code>null</code>.
    * @return a list of results, never <code>null</code>.
    */
   private List<IPSPubStatus> findPubStatusByEditionList(List<Long> editionids)
   {
      Session session = getSessionFactory().getCurrentSession();

      Criteria c = session.createCriteria(PSPubStatus.class);
      if (editionids.size() == 1)
      {
         c.add(Restrictions.eq("editionId", editionids.get(0)));
      }
      else if (editionids.size() > 0)
      {
         c.add(Restrictions.in("editionId", editionids));
      }
      c.add(Restrictions.isNull("hidden"));
      c.add(Restrictions.isNotNull("startDate"));
      c.addOrder(Order.desc("startDate"));
      return c.list();

   }
   
   /**
    * Checks to see if the site has been published.
    *
    * Note: Removed @Transactional from the method as calling method is
    * in interface and is now marked as transactional.
    * 
    * @param editionids one ore more editions, assumed never <code>null</code>.
    * @return <code>true</code> if the site has been published.
    */
   private boolean findIsSitePublished(List<Long> editionids)
   {
      Session session = getSessionFactory().getCurrentSession();
      Criteria c = session.createCriteria(PSPubStatus.class);
      if (editionids.size() == 1)
      {
         c.add(Restrictions.eq("editionId", editionids.get(0)));
      }
      else if (editionids.size() > 0)
      {
         c.add(Restrictions.in("editionId", editionids));
      }
      c.add(Restrictions.isNull("hidden"));
      c.add(Restrictions.isNotNull("startDate"));
      c.setMaxResults(1);
      c.setCacheable(true);
      c.setFetchSize(1);
      if (c.list().size() > 0)
         return true;
      else
         return false;
   }
   
   /**
    * Finds the pub statuses by server id
    * 
    * @param serverId theServerId, assumed never <code>null</code>.
    * @return a list of results, never <code>null</code> may be empty.
    */
   private List<IPSPubStatus> findPubStatusByServer(Long serverId)
   {
      Session session = getSessionFactory().getCurrentSession();

         Criteria c = session.createCriteria(PSPubStatus.class);
         c.add(Restrictions.eq("pubServerId", serverId));
         c.add(Restrictions.isNull("hidden"));
         c.add(Restrictions.isNotNull("startDate"));
         c.addOrder(Order.desc("startDate"));
         return c.list();

      }

   
   
   public IPSPubStatus findLastPubStatusByEdition(IPSGuid editionId)
   {
      Validate.notNull(editionId);
      Session session = getSessionFactory().getCurrentSession();

         Criteria c = session.createCriteria(PSPubStatus.class);
         c.add(Restrictions.eq("editionId", editionId.longValue()));

         c.add(Restrictions.isNull("hidden"));
         c.add(Restrictions.isNotNull("endDate"));
         c.addOrder(Order.desc("endDate"));
         c.setMaxResults(1);
         
         List<IPSPubStatus> results = c.list();
         return results.isEmpty() ? null : results.get(0);


      }

   private static boolean fixed = false;
   
   /**
    * Called by spring for initialization on boot.
    */
   @Transactional
   public void init() {
      log.info("Initializing Publisher Service");
       log.info("TODO: Move fixPubStatus to rxFix Job.");
      //fixPubStatus(false);
   }

   /**
    * Fixes aborted publishing jobs that did not finish
    * because of a server shutdown.
    * If the PubStatus server name equals the currently 
    * running server and the end date is null than the
    * PubStatus will be marked aborted.
    * 
    * @param force will force the operation to occur (used for unit testing).
    * @return number of statuses corrected.
    */
   @Transactional
   public int fixPubStatus(boolean force)
   {
      if (fixed && ! force) return 0;
      Session session = getSessionFactory().getCurrentSession();
      try
      {
         Query q = session.getNamedQuery("unfinishedPubStatusForServer");
         q.setParameter("server", getServerId());
         List<Object> pubStats = q.list();
         int i = 0;
         for (Object o : pubStats) {
            PSPubStatus ps = (PSPubStatus) o;
            ps.setEndingStatus(EndingState.ABORTED);
            session.persist(ps);
            i++;
         }
         session.flush();
         fixed = true;
         if (i != 0)
            log.info("Fixed aborted pubstatus total: " + i);
         return i;
      }
      catch (Throwable e)
      {
         log.warn("Failed to fix update publish status", e);
         return 0;
      }
   }

   /**
    * Get the summary counts for a given publish status.
    * 
    * @param pubstatus the publish status in question, assumed not 
    *    <code>null</code>.
    */
   private void getCountsForPubStatus(PSPubStatus pubstatus)
   {
      Session session = getSessionFactory().getCurrentSession();

      Query q = session.getNamedQuery("item_completion");
      setStatusSummaryData(pubstatus, q, IPSSiteItem.Operation.PUBLISH,
            IPSSiteItem.Status.SUCCESS, 0);
      setStatusSummaryData(pubstatus, q, IPSSiteItem.Operation.UNPUBLISH,
            IPSSiteItem.Status.SUCCESS, 1);
      setStatusSummaryData(pubstatus, q, IPSSiteItem.Operation.PUBLISH,
            IPSSiteItem.Status.FAILURE, 2);
      setStatusSummaryData(pubstatus, q, IPSSiteItem.Operation.UNPUBLISH,
            IPSSiteItem.Status.FAILURE, 2);

      }
   

   /**
    * Query the items for a particular kind of record and set the data in 
    * the given publish status. For failed requests the code sums since this 
    * needs to be called twice.
    * 
    * @param pubstatus the publish status in question, assumed never 
    *    <code>null</code>.
    * @param q the query, assumed never <code>null</code>.
    * @param operation the operation, assumed never <code>null</code>.
    * @param status the status, assumed never <code>null</code>.
    * @param target the particular target to set, <code>0</code> for
    * delivered, <code>1</code> for removed and <code>2</code> for 
    * failed.
    */
   private void setStatusSummaryData(
         PSPubStatus pubstatus, Query q,
         Operation operation, Status status, int target)
   {
      q.setShort("operation", (short) operation.ordinal() );
      q.setShort("status", (short) status.ordinal());
      q.setParameterList("ids", Collections.singleton(pubstatus.getStatusId()));
      List result = q.list();
      for(Object r : result)
      {
         Object arr[] = (Object[]) r;
         // Number sid = (Number) arr[0];
         Number count = (Number) arr[1];

         switch(target)
         {
            case 0:
               pubstatus.setDelivered(count.intValue());
               break;
            case 1:
               pubstatus.setRemoved(count.intValue());
               break;
            case 2:
               pubstatus.setFailed(count.intValue());
               break;
         }
      }
   }

   @Transactional
   public IPSGuid findEditionIdForJob(long jobid)
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      List results = 
              getSessionFactory().getCurrentSession().createQuery(
               "select s.editionId from PSPubStatus s " +
               "where s.statusId = :statusid").setParameter(
               "statusid", jobid).list();
      if (results != null && results.size() > 0)
      {
         Number editionId = (Number) results.get(0);
         return gmgr.makeGuid(editionId.longValue(), PSTypeEnum.EDITION);
      }
      else
         return null;
   }

   @Transactional
   public IPSPubStatus findPubStatusForJob(long jobid)
   {
      List results = getSessionFactory().getCurrentSession().createQuery(
            "select s from PSPubStatus s where s.statusId = :sid").setParameter(
            "sid", jobid).list();
      if (results != null && results.size() > 0)
      {
         return (IPSPubStatus) results.get(0);
      }
      else
      {
         return null;
      }
   }

   @Transactional
   public void deleteSiteItems(IPSGuid siteguid)
   {
      if (siteguid == null)
      {
         throw new IllegalArgumentException("siteguid may not be null");
      }
      if (siteguid.getType() != PSTypeEnum.SITE.getOrdinal())
      {
         throw new IllegalArgumentException("guid must be of type site");
      }
      long siteid = siteguid.longValue();
      Session s = getSessionFactory().getCurrentSession();

         Query q = s.createQuery("delete from PSSiteItem where siteId = :site");
         q.setLong("site", siteid);
         q.executeUpdate();

   }

   /**
    * @return the configurationBean
    */
   public PSServiceConfigurationBean getConfigurationBean()
   {
      return m_configurationBean;
   }

   /*
    * @param configurationBean the configurationBean to set
    */
   public void setConfigurationBean(
         PSServiceConfigurationBean configurationBean)
   {
      m_configurationBean = configurationBean;
   }
   
   @Transactional
   public List<Long> findReferenceIdsToUnpublish(IPSGuid siteId, String flags)
   {
      if (siteId == null)
         throw new IllegalArgumentException("siteId may not be null.");
      if (StringUtils.isBlank(flags))
         throw new IllegalArgumentException("flags may not be null or empty.");
      
      return findReferenceIdsToUnpublishSiteOrServer(siteId, flags, true);
   }
   
   public List<Long> findReferenceIdsToUnpublishByServer(IPSGuid serverId, String flags)
   {
      if (serverId == null)
         throw new IllegalArgumentException("siteId may not be null.");
      if (StringUtils.isBlank(flags))
         throw new IllegalArgumentException("flags may not be null or empty.");
      
      return findReferenceIdsToUnpublishSiteOrServer(serverId, flags, false);
   }

   public static boolean isHandleChangedLocation()
   {
      // TODO Auto-generated method stub
      return true;
   }
   
   private List<Long> findReferenceIdsToUnpublishSiteOrServer(IPSGuid objectId, String flags, boolean isSite)
   {
      if (objectId == null)
         throw new IllegalArgumentException("objectId may not be null.");
      if (StringUtils.isBlank(flags))
         throw new IllegalArgumentException("flags may not be null or empty.");
      
      String[] unpublishFlags = flags.split(",");
      List<String> upflagList = new ArrayList<>();
      for(String flag : unpublishFlags)
      {
         upflagList.add(flag);
      }
      List<Long> rval = new ArrayList<>();
      Session s = getSessionFactory().getCurrentSession();

         // Note that in all cases, we're looking for either successfully 
         // published, or unsuccessfully unpublished items. This means that
         // the operation value and the status value will be equal.
         //
         // Purged items
         String whereCl = isSite ? "s.siteId = :objid" : "s.serverId = :objid";
         
         Query q = s.createQuery("select s.referenceId "
               + "from PSSiteItem s "
               + "where " + whereCl
               + " AND s.status = s.operation "
               + "AND s.contentId not in "
               + "(select c.m_contentId from PSComponentSummary c)");
         q.setLong("objid", objectId.longValue());
         List<Long> purgedItems = q.list();
         rval.addAll(purgedItems);

         log.debug("Found purged items for unpublish: " + purgedItems.size());
         
         // Items not in their original folder
         List movedItems = new ArrayList<Long>();
         if (isHandleChangedLocation())
            movedItems = findMovedItems(objectId, true);
         
         log.debug("Found moved items for unpublish: " + movedItems.size());
         
         rval.addAll(movedItems);
         
         // Items in an archive state
         q = s.createQuery("select s.referenceId " +
                "from PSSiteItem s, PSComponentSummary cs," +
                " PSState state " +
                "where " + whereCl +
                " AND s.status = s.operation " +
                "AND cs.m_contentId = s.contentId " +
                "AND state.stateId = cs.m_contentStateId " +
                "AND state.workflowId = cs.m_workflowAppId " +
                "AND state.contentValidValue in (:flags)");
         q.setParameterList("flags", upflagList);
         q.setLong("objid", objectId.longValue());
         List<Long> archiveItems = q.list();
         rval.addAll(archiveItems);

         log.debug("Found archived items for unpublish: " + archiveItems.size());

      return rval;
   }

   /*
    * Finds a list of items that have been moved from its original folder
    * since last publishing run for a given site.
    * @param site the site in question, assumed not <code>null</code>.
    * @param isGetReferenceId it is <code>true</code> if getting the reference
    *    ID of the moved item from the Site Item table; otherwise getting
    *    the content ID of the moved item from Pub Doc table. 
    * @return <code>List<Long></code> of reference IDs or 
    *    <code>List<Integer></code> of content IDs. Never <code>null</code>, 
    *    but may be empty. 
    */
   private List findMovedItems(IPSGuid serverId, boolean isGetReferenceId)
   {
      Session s = getSessionFactory().getCurrentSession();

         // Note that in all cases, we're looking for either successfully 
         // published, or unsuccessfully unpublished items. This means that
         // the operation value and the status value will be equal.
         //
         // Items not in their original folder
         s.enableFilter("relationshipConfigFilter");
         String idRef = isGetReferenceId ? "s.referenceId" : "p.contentId";
         Query q = s.createQuery("select " + idRef + " " +
               "from PSSiteItem s " +
               "where s.serverId = :serverid " +
               "AND s.status = s.operation " +
               "AND (select count(cs.m_contentId) " +
               "     from PSComponentSummary cs " +
               "     where cs.m_contentId = s.contentId AND " +
               "           s.folderId in (select pfolder.owner_id " +
               "                          from cs.parentFolders pfolder)) = 0");
         q.setLong("serverid", serverId.longValue());
         return q.list();

      }

   @Transactional
   public List<IPSPubItemStatus> findPubItemStatusForReferenceIds(
         List<Long> refs)
   {
      List<IPSPubItemStatus> rval = new ArrayList<>();
      Session s = getSessionFactory().getCurrentSession();

         final int interval = 300;
         for(int i = 0; i < refs.size(); i += interval)
         {
            int end = i + interval;
            List<Long> refSubList = refs.subList(i, 
                  end > refs.size() ? refs.size() : end);
            Criteria c = s.createCriteria(PSPubItem.class);
            c.add(Restrictions.in("referenceId", refSubList));
            rval.addAll(c.list());
         }

      return rval;
   }

   @Transactional
   public List<PSSiteItem> findSiteItemsForReferenceIds(List<Long> refs)
   {
      List<PSSiteItem> rval = new ArrayList<>();
      Session s = getSessionFactory().getCurrentSession();

         final int interval = 300;
         for(int i = 0; i < refs.size(); i += interval)
         {
            int end = i + interval;
            List<Long> refSubList = refs.subList(i, 
                  end > refs.size() ? refs.size() : end);
            Criteria c = s.createCriteria(PSSiteItem.class);
            c.add(Restrictions.in("referenceId", refSubList));
            rval.addAll(c.list());
         }

      return rval;
   }

   @Transactional
   public Object[] findUnpublishInfoForAssemblyItem(IPSGuid contentId,
         IPSGuid contextId, IPSGuid templateId, IPSGuid siteId, Long serverId,
         String targetPath)
   {
      Session s = getSessionFactory().getCurrentSession();

         String queryName = serverId != null ? "site_item_and_doc_with_server_id" : "site_item_and_doc";
         
         Query q = s.getNamedQuery(queryName);
         q.setInteger("contentId", contentId.getUUID());
         q.setInteger("context", contextId.getUUID());
         q.setLong("templateId", templateId.longValue());
         if (serverId != null)
            q.setLong("serverId", serverId.longValue());
         else
            q.setLong("siteId", siteId.longValue());
         q.setString("location", targetPath);
         Object data[] = (Object[]) q.uniqueResult();
         return data;

      }
   
   public int findLastPublishedItemsBySite(IPSGuid siteId,
         Collection<Integer> contentIds)
   {
      notNull(siteId);
      notNull(contentIds);
      
      
      Session s = sessionFactory.getCurrentSession();
      long idset = 0;
      
      try
      {
         Query q;
         if (contentIds.size() < MAX_IDS)
         {
            q = s.getNamedQuery("lastPublishedItemsBySite_InClause");
            q.setParameterList("contentIds", contentIds);
         }
         else
         {
            idset = createIdSet(s, contentIds);
            
            q = s.getNamedQuery("lastPublishedItemsBySite_TempId");
            q.setParameter("idset", idset);
         }
         q.setLong("siteId", siteId.longValue());
         List<Long> result = q.list();
         return result.size();
      }
      finally
      {
         if (idset != 0)
         {
            clearIdSet(s, idset);
         }
      }
   }

   /**
    * @return the rxpub bean
    */
   public IPSRxPublisherService getRxpub()
   {
      return m_rxpub;
   }

   /**
    * @param rxpub the rxpub bean to set
    */
   public void setRxpub(IPSRxPublisherService rxpub)
   {
      m_rxpub = rxpub;
   }

   @Transactional
   public List<IPSContentList> findAllUnusedContentLists()
   {
      return sessionFactory.getCurrentSession().createQuery("select cl from PSContentList cl " +
            "where cl.contentListId not in " +
            "(select el.pk.contentlistid from PSEditionContentList el)").list();
   }
   
   /**
    * A utility class, use to log elapsed time for a process.
    */
   private class Timer extends PSTimer
   {
      Timer()
      {
         super(log);
      }
   }
   
   /**
    * Sets the data-source manager, used to determine the type of back-end repository.
    *
    * @param dsMgr The data-source manager, may not be <code>null</code>.
    */
   public void setDatasourceManager(IPSDatasourceManager dsMgr)
   {
      if (dsMgr == null)
         throw new IllegalArgumentException("dsMgr may not be null");

      m_dsMgr = dsMgr;
   }

   /**
    * The data-source manager, used to determine the type of back-end repository.
    * initialized by the first call to {@link #setDatasourceManager(IPSDatasourceManager)}, 
    * never <code>null</code> after that.
    */
   private IPSDatasourceManager m_dsMgr;

   /**
    * Determines if the back-end repository is MySQL or not.
    * The back-end repository is MySQL if it is <code>true</code>.
    * It defaults to <code>null</code> initially, but updated by {@link #isMySQL()} only once.
    */
   private Boolean m_isMySQL = null;
   
   /**
    * Determines if the repository is a derby database.
    *
    * @return <code>true</code> if it is a derby database; otherwise return
    *   <code>false</code>.
    */
   private boolean isMySQL()
   {
      if (m_isMySQL != null)
         return m_isMySQL.booleanValue();
      
      try
      {
         PSConnectionDetail connDetail = m_dsMgr.getConnectionDetail(null);
         m_isMySQL = PSSqlHelper.isMysql(connDetail.getDriver());
         return m_isMySQL.booleanValue();
      }
      catch (Exception e)
      {
         log.error("Failed to determine database type", e);
         return false;
      }
   }

   @Override
   @Transactional
   public boolean isSitePublished(IPSGuid siteId)
   {
      List<IPSEdition> editions = findAllEditionsBySite(siteId);
      List<Long> editionIds = new ArrayList<>();
      if (editions.isEmpty())
         return false;
      for(IPSEdition e : editions)
      {
         editionIds.add(((PSEdition) e).getId());
      }
      return findIsSitePublished(editionIds);
   }
   
  }
