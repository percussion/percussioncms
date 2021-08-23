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
package com.percussion.services.assembly.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.deploy.server.PSJexlHelper;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.fastforward.globaltemplate.PSRxGlobals;
import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.*;
import com.percussion.services.assembly.IPSAssemblyTemplate.AAType;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
import com.percussion.services.assembly.impl.nav.PSNavHelper;
import com.percussion.services.catalog.IPSCatalogErrors;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSCatalogException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.contentmgr.*;
import com.percussion.services.contentmgr.data.PSContentNode;
import com.percussion.services.contentmgr.data.PSNodeDefinition;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.memory.PSCacheAccessLocator;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteHelper;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.utils.general.PSServiceConfigurationBean;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSBaseBean;
import com.percussion.util.PSStopwatch;
import com.percussion.utils.codec.PSXmlEncoder;
import com.percussion.utils.collections.PSFacadeMap;
import com.percussion.utils.exceptions.PSExceptionHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.timing.PSStopwatchStack;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.jcr.*;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * The assembly service assembles items and provides methods for managing
 * templates and slots.
 *
 * @author dougrand
 */
@PSBaseBean("sys_assemblyService")
@Transactional(noRollbackFor = Exception.class)
public class PSAssemblyService implements IPSAssemblyService
{
   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }
   /**
    * Cache key for content cache.
    */
   static class ContentCacheKey implements Serializable
   {
      /**
       * Serialization id.
       */
      private static final long serialVersionUID = -3176331948324016797L;

      /**
       * The filter id, initialized in ctor, assumed never <code>null</code>.
       */
      IPSGuid mi_filterid;

      /**
       * The item id, initialized in ctor, assumed never <code>null</code>.
       */
      IPSGuid mi_itemid;

      /**
       * The item context, initialized in the ctor.
       */
      int mi_context;

      /**
       * This is set to <code>true</code> if the item was loaded in AA mode. AA
       * mode items have different embedded links and need to be segregated.
       */
      boolean mi_isAA;

      /**
       * Keep an association between a given content id and the cache keys used.
       * The per content item set is added to when loading and shrunk on
       * eviction.
       */
      static Map<IPSGuid, Set<ContentCacheKey>> ms_keys = new HashMap<>();

      /**
       * Create a key.
       *
       * @param itemid item id, assumed never <code>null</code>
       * @param filterid filter id, assumed never <code>null</code>
       * @param aa <code>true</code> for aa
       * @param context the context for the item
       */
      public ContentCacheKey(IPSGuid itemid, IPSGuid filterid, boolean aa, int context)
      {
         mi_filterid = filterid;
         mi_itemid = itemid;
         mi_isAA = aa;
         mi_context = context;
         synchronized (ContentCacheKey.class)
         {
            Set<ContentCacheKey> keys = ms_keys.computeIfAbsent(itemid, k -> new HashSet<>());
            keys.add(this);
         }
      }

      /**
       * Get the keys associated with the given item, and removes the set from
       * the key map. You must take a synchronization lock on the class
       * {@link ContentCacheKey} before calling this method and you must not
       * release it until you are done with the set.
       *
       * @param itemid the item, assumed never <code>null</code>.
       * @return the set, may be <code>null</code> if no keys have been
       *         allocated for the given item.
       */
      public static Set<ContentCacheKey> getAndClearKeys(IPSGuid itemid)
      {
         Set<ContentCacheKey> rval = ms_keys.get(itemid);
         ms_keys.remove(itemid);
         return rval;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof ContentCacheKey)
         {
            ContentCacheKey key = (ContentCacheKey) obj;
            return mi_filterid.equals(key.mi_filterid) && mi_itemid.equals(key.mi_itemid) && mi_isAA == key.mi_isAA
                  && mi_context == key.mi_context;
         }
         return false;
      }

      @Override
      public int hashCode()
      {
         return mi_filterid.hashCode() + mi_itemid.hashCode();
      }
   }

   /**
    * Deal with the content cache for the assembly service. Cached items are
    * held in the memory service in a separate region. Items are flushed from
    * the cache either on a per item basis (for content changes) or wholesale
    * for changes to design objects. The design objects that trigger a flush
    * are:
    * <ul>
    * <li>Template
    * <li>Slot
    * <li>Item Filter
    * <li>Location Scheme or param
    * </ul>
    * This wholesale flush deals with the body having been prefiltered for
    * cached items.
    */
   static class AssemblyContentChangedListener implements IPSNotificationListener
   {
      /**
       * List of types that require invalidation.
       */
      static List<Short> ms_invalidatefor;

      static
      {
         ms_invalidatefor = new ArrayList<>();
         ms_invalidatefor.add(PSTypeEnum.TEMPLATE.getOrdinal());
         ms_invalidatefor.add(PSTypeEnum.SLOT.getOrdinal());
         ms_invalidatefor.add(PSTypeEnum.ITEM_FILTER.getOrdinal());
         ms_invalidatefor.add(PSTypeEnum.LOCATION_SCHEME.getOrdinal());
         ms_invalidatefor.add(PSTypeEnum.LOCATION_PROPERTY.getOrdinal());
      }

      public void notifyEvent(PSNotificationEvent notification)
      {
         IPSCacheAccess cache = PSCacheAccessLocator.getCacheAccess();
         PSStopwatch sw = new PSStopwatch();
         sw.start();
         if (notification.getType().equals(EventType.CONTENT_CHANGED))
         {
            IPSGuid ccid = (IPSGuid) notification.getTarget();
            synchronized (ContentCacheKey.class)
            {
               Set<ContentCacheKey> keys = ContentCacheKey.getAndClearKeys(ccid);
               if (keys != null)
               {
                  for (ContentCacheKey key : keys)
                  {
                     cache.evict(key, CONTENT_REGION);
                  }
               }
            }
         }
         else if (notification.getType().equals(EventType.OBJECT_INVALIDATION))
         {
            IPSGuid guid = (IPSGuid) notification.getTarget();
            short type = guid.getType();
            if (ms_invalidatefor.contains(type))
            {
               cache.clear(CONTENT_REGION);
            }
         }
      }
   }

   /**
    * Listener for the assembly service that takes care of locally cached
    * read-only data. The listener is instantiated and registered on the first
    * access to the map.
    */
   class PSAssemblyNotificationListener implements IPSNotificationListener
   {
      public void notifyEvent(PSNotificationEvent notification)
      {
         IPSGuid guid = (IPSGuid) notification.getTarget();
         short type = guid.getType();
         if (type == PSTypeEnum.NODEDEF.getOrdinal() || type == PSTypeEnum.TEMPLATE.getOrdinal())
         {
            m_cache.evict(TEMPLATE_NAME_KEY, IPSCacheAccess.IN_MEMORY_STORE);
            if (type == PSTypeEnum.TEMPLATE.getOrdinal())
            {
               m_cache.evict(TEMPLATE_NAME_ID_MAP_KEY, IPSCacheAccess.IN_MEMORY_STORE);
            }
         }
      }
   }

   /**
    * Content region.
    */
   private static final String CONTENT_REGION = "content";

   /**
    * Used to identify error information in the bindings.
    */
   public static final String ERROR_VAR = "$___error___";

   /**
    * String identifying the debug assembler in Extensions.xml.
    */
   private static final String DEBUG_ASSEMBLER = "Java/global/percussion/assembly/debugAssembler";

   /**
    * Cache key for template map.
    */
   private static final String TEMPLATE_NAME_KEY = "template_name_map";

   /**
    * Cache key for the template name to id map key.
    */
   private static final String TEMPLATE_NAME_ID_MAP_KEY = "template_name_id_map";

   /**
    * Logger for the assembler.
    */
   private static final Logger log = LogManager.getLogger(PSAssemblyService.class);

   /**
    * Counter used to generate job ids for items that have none specified.
    */
   private static AtomicLong ms_internalJobId = new AtomicLong(-1);

   /**
    * Pagelink preparsed expression.
    */
   private static IPSScript ms_pagelink = PSJexlHelper.createStaticExpression("$pagelink");

   /**
    * Store the current assembly item in thread local storage for access where
    * appropriate.
    */
   private static ThreadLocal<IPSAssemblyItem> ms_item = new ThreadLocal<>();

   /**
    * Notification service, wired by spring.
    */
   private IPSNotificationService m_nsvc;

   /**
    * Cache service, used to invalidate content information.
    */
   private IPSCacheAccess m_cache;

   /**
    * The service configuration bean, used in the assembly service to obtain the
    * maximum cached size.
    */
   private PSServiceConfigurationBean m_configurationBean = null;

   /**
    * Cache region identifier, look at ehcache.xml for more information.
    */
   private static final String CACHE_REGION = "assemblyqueries";

   /**
    * Static expression for page count.
    */
   private static final IPSScript PAGE_COUNT = PSJexlHelper.createStaticExpression("$sys.pagecount");

   /**
    * Static expression for page.
    */
   private static final IPSScript PAGE = PSJexlHelper.createStaticExpression("$sys.page");

   /**
    * Load specific assembler. This static method is available to the assembly
    * service implementation for the purposes of resolving the reference to a
    * particular assembler.
    * <p>
    * This loader uses the extensions manager to find a class that implements
    * {@link IPSAssembler} with the given name.
    *
    * @param name name of assembler to load, never <code>null</code>. The word
    *           "Assembler" is added to the name to lookup the extension. So if
    *           the assembler name is passed as "freemarker" this method looks
    *           for an extension named "freemarkerAssembler".
    *
    * @return an instance of {@link IPSAssembler}, never <code>null</code>
    * @throws PSAssemblyException if the bean does not exist
    */
   static IPSAssembler getAssembler(String name) throws PSAssemblyException
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      IPSExtensionManager emgr = PSServer.getExtensionManager(null);
      try
      {
         PSExtensionRef assemblerref = new PSExtensionRef(name);
         return (IPSAssembler) emgr.prepareExtension(assemblerref, null);
      }
      catch (PSExtensionException e)
      {
         log.error("Serious problem, cannot instantiate {} Error: {}", name, e.getMessage());
         log.debug(e.getMessage(),e);
         throw new PSAssemblyException(IPSAssemblyErrors.ASSEMBLER_INST, name, e);
      }
      catch (com.percussion.design.objectstore.PSNotFoundException e)
      {
         log.error("Serious problem, cannot find {} Error: {}" ,name, e.getMessage());
         throw new PSAssemblyException(IPSAssemblyErrors.ASSEMBLER_INST, name, e);
      }
   }

   public IPSAssemblyResult processServletRequest(HttpServletRequest request, String templatename, String variantidstr)
         throws PSAssemblyException
   {
      try
      {
         if ((StringUtils.isBlank(variantidstr) && StringUtils.isBlank(templatename))
               || (!StringUtils.isBlank(variantidstr) && !StringUtils.isBlank(templatename)))
         {
            throw new PSAssemblyException(IPSAssemblyErrors.PARAMS_VARIANT_OR_TEMPLATE);
         }

         Map<String, String[]> params = new PSFacadeMap<>(request.getParameterMap());
         long jobId;
         if (params.get(IPSHtmlParameters.SYS_PUBSTATUSID) != null)
         {
            jobId = Long.parseLong(params.get(IPSHtmlParameters.SYS_PUBSTATUSID)[0]);
         }
         else
         {
            jobId = ms_internalJobId.decrementAndGet();
         }

         String path = request.getParameter(IPSHtmlParameters.SYS_PATH);

         IPSAssemblyItem work = createAssemblyItem();
         work.setPath(path);
         work.setParameters(params);
         if (work.getJobId() == 0)
         {
            work.setJobId(jobId);
         }
         work.setDebug(request.getRequestURL().toString().endsWith("/debug"));
         if (isUseEditRevisions(request))
            work.setUserName(request.getRemoteUser());
         String publish = work.getParameterValue(IPSHtmlParameters.SYS_PUBLISH, "publish");
         work.setPublish(!publish.equalsIgnoreCase("unpublish"));
         work.normalize();
         String page = request.getParameter("sys_page");
         if (StringUtils.isNotBlank(page) && StringUtils.isNumeric(page))
         {
            work.setPage(new Integer(page));
         }
         List<IPSAssemblyItem> assemblyitems = Collections.singletonList(work);
         List<IPSAssemblyResult> results = assemble(assemblyitems);
         if (results.size() == 0)
         {
            return null;
         }
         else
         {
            return results.get(0);
         }
      }
      catch (PSAssemblyException e)
      {
         // Rethrow assembly exceptions
         throw e;
      }
      catch (Exception e)
      {
         Throwable cause = PSExceptionHelper.findRootCause(e, true);
         log.error("Failure while processing assembly item", cause);
         throw new PSAssemblyException(IPSAssemblyErrors.UNKNOWN_ERROR, cause, e.getLocalizedMessage());
      }
   }

   /**
    * Evaluate the flag whether to use edit revisions or current revisions for
    * the parent as well as AA related items.
    * <p>
    * This flag will be evaluated to <code>true</code>:
    * <ul>
    * <li>If the requested URL is for active assembly, i.e. sys_command=editrc,
    * Or</li>
    * <li>If the request has the parameter useEditRevisions=yes</li>
    * </ul>
    *
    * @param request request object, assumed not <code>null</code>.
    * @return <code>true</code> if to use edit revisions, <code>false</code>
    *         otherwise.
    */
   private boolean isUseEditRevisions(HttpServletRequest request)
   {
      String editrc = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
      if (editrc == null)
         editrc = "";
      String isEditRevisions = request.getParameter("useEditRevisions");
      if (isEditRevisions == null)
         isEditRevisions = "";
      boolean isEdit = editrc.equalsIgnoreCase(IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY)
            || isEditRevisions.equalsIgnoreCase("yes");
      return isEdit;
   }

   public IPSAssemblyItem createAssemblyItem(String path, long jobid, int refid, IPSAssemblyTemplate template,
         Map<String, String> variables, Map<String, String[]> params, Node optNode, boolean isDebug)
         throws PSAssemblyException
   {
      PSAssemblyWorkItem rval = new PSAssemblyWorkItem();
      rval.setPath(path);
      rval.setJobId(jobid);
      rval.setReferenceId(refid);
      rval.setTemplate(template);
      rval.setVariables(variables);
      rval.setParameters(params);
      rval.setNode(optNode);
      rval.setDebug(isDebug);
      rval.setUserName(rval.getParameterValue(IPSHtmlParameters.SYS_USER, null));
      rval.normalize();

      return rval;
   }

   public IPSAssemblyItem createAssemblyItem()
   {
      PSAssemblyWorkItem rval = new PSAssemblyWorkItem();

      rval.setReferenceId(0);
      rval.setJobId(0);

      return rval;
   }

   /**
    * Process and evaluate the binding variables that are used to assemble the
    * given item.
    * 
    * @param item the to be processed item, assumed not <code>null</code>.
    * @param paginatedItems this is used to collect the paginated items, assumed
    *           not <code>null</code>, but may be empty.
    * @param debugItems this is used to collect items that will be assembled by
    *           {@link #DEBUG_ASSEMBLER}, assumed not <code>null</code>, but may
    *           be empty.
    * @param eval the evaluator, assumed not <code>null</code>.
    * @param isLegacy <code>true</code> if the assembler is legacy assembler.
    * 
    * @throws PSAssemblyException if failed to setup the item for assembly
    * @throws PSCmsException if failed to setup the item for assembly
    * @throws PathNotFoundException if failed to setup the item for assembly
    * @throws RepositoryException if failed to setup the item for assembly
    */
   private void processItemBinding(IPSAssemblyItem item, Set<IPSAssemblyResult> paginatedItems,
         List<IPSAssemblyItem> debugItems, PSAssemblyJexlEvaluator eval, boolean isLegacy) throws PSAssemblyException,
           PSCmsException, PathNotFoundException,
           RepositoryException
   {
      try
      {
         ms_item.set(item);
         processBindings(item, eval);
         Number count = null;
         try
         {
            count = (Number) eval.evaluate(PAGE_COUNT);
         }
         catch (Exception e)
         {
            log.error("Problem while processing " + PAGE_COUNT + "binding for item: " + item, e);
         }
         int context = item.getContext();
         if (item.getParentPageReferenceId() == null && count != null && count.intValue() > 1)
         {
            item.setPaginated(true);
            if (context == 0)
            {
               if (item.getPage() == null)
               {
                  item.setPage(1);
                  eval.bind("$sys.page", item.getPage());
               }
            }
            else
            {
               paginatedItems.add((IPSAssemblyResult) item);
            }
         }
      }
      finally
      {
         ms_item.remove();
      }

      // Legacy handles debugging itself
      if (item.isDebug() && !isLegacy)
      {
         debugItems.add(item);
      }
   }

   @Transactional(readOnly = true, rollbackFor =
   {}, noRollbackFor =
   {RuntimeException.class})
   public List<IPSAssemblyResult> assemble(List<IPSAssemblyItem> items) throws PSAssemblyException, PSFilterException,
         PSTemplateNotImplementedException
   {
      if (items == null)
      {
         throw new IllegalArgumentException("items may not be null");
      }
      // map (original) item to its result.
      Map<IPSAssemblyItem, IPSAssemblyResult> assemblyResultMap = new HashMap<>();

      handleItemTemplates(items);
      Map<String, List<IPSAssemblyItem>> byAssembler = groupItemsByAssembler(items);

      // Process each and gather results. Note: keep getLandingPageUrl up
      // to date as this code changes. getLandingPageUrl is not a complete
      // assembly process
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      for (String assemblerName : byAssembler.keySet())
      {
         try
         {
            List<IPSAssemblyItem> perAssemblerItems = byAssembler.get(assemblerName);
            List<IPSAssemblyItem> debugItems = new ArrayList<>();
            Set<IPSAssemblyResult> paginatedItems = new HashSet<>();

            for (IPSAssemblyItem item : perAssemblerItems)
            {
               boolean isLegacy = item.getTemplate().getAssembler().equals(IPSExtension.LEGACY_ASSEMBLER);
               PSAssemblyJexlEvaluator eval = setupItemForAssembly(item, isLegacy);

               IPSAssembler assembler = getAssembler(assemblerName);
               assembler.preProcessItemBinding(item, eval);

               processItemBinding(item, paginatedItems, debugItems, eval, isLegacy);
            }

            perAssemblerItems = getAssemblyItems(perAssemblerItems, paginatedItems, debugItems, assemblyResultMap);

            assembleItems(assemblerName, sws, perAssemblerItems, debugItems, assemblyResultMap);
         }
         catch (PSAssemblyException e)
         {
            log.error("Error while assembling items for assembler " + assemblerName
                  + " no items processed for that assembler", e);
         }
         catch (PSCmsException e)
         {
            log.error("Error during assembly", e);
         }
         catch (PathNotFoundException e)
         {
            log.error("Missing content during assembly", e);
         }
         catch (RepositoryException e)
         {
            log.error("Illegal repository operation during assembly", e);
         }
      }


      checkItemsForEviction(items);

      return getAssemblyResult(items, assemblyResultMap);
   }

   public void preProcessItemBinding(IPSAssemblyItem item, PSAssemblyJexlEvaluator eval) {
      // do nothing by default
   }

   /**
    * Group the given items by the name of the assembler.
    * 
    * @param items to be sorted items, assumed not <code>null</code>.
    * @return the sorted map, never <code>null</code>, but may be empty if the
    *         items is empty.
    */
   private Map<String, List<IPSAssemblyItem>> groupItemsByAssembler(List<IPSAssemblyItem> items)
   {
      Map<String, List<IPSAssemblyItem>> byAssembler = new HashMap<>();

      for (IPSAssemblyItem item : items)
      {
         IPSAssemblyTemplate template = item.getTemplate();
         if (template == null)
         {
            log.error("Skipping item " + item.getId() + " due to missing template");
            continue;
         }
         String assemblername = template.getAssembler();
         List<IPSAssemblyItem> list = byAssembler.get(assemblername);
         if (list == null)
         {
            list = new ArrayList<>();
            byAssembler.put(assemblername, list);
         }
         list.add(item);
      }

      return byAssembler;
   }

   /**
    * Assemble given items with the specified assembler.
    * 
    * @param assemblerName the name of the assembler, assumed not
    *           <code>null</code> or empty.
    * @param sws stop watch, assumed not <code>null</code> or empty.
    * @param perAssemblerItems to be assembled items by the specified assembler.
    *           Assumed not <code>null</code>, may be empty.
    * @param debugItems to be assembled items by {@link #DEBUG_ASSEMBLER}
    *           assembler.
    * @param assemblyResultMap the map that maps the item to its assembled
    *           result. This is used to collect the assembled result.
    * 
    * @throws PSAssemblyException if there's a problem rendering the content
    * @throws ItemNotFoundException if an item is missing from the repository
    * @throws PSFilterException if there's a problem finding or interpreting the
    *            item filter
    * @throws RepositoryException if an error occurs loading data from the
    *            repository
    * @throws PSTemplateNotImplementedException if a passed template is not
    *            supported
    */
   private void assembleItems(String assemblerName, PSStopwatchStack sws, List<IPSAssemblyItem> perAssemblerItems,
         List<IPSAssemblyItem> debugItems, Map<IPSAssemblyItem, IPSAssemblyResult> assemblyResultMap)
         throws PSAssemblyException, ItemNotFoundException, PSFilterException, RepositoryException,
         PSTemplateNotImplementedException
   {
      if (perAssemblerItems.size() > 0)
      {
         try
         {
            sws.start(assemblerName + "#assemble");

            IPSAssembler assembler = getAssembler(assemblerName);
            List<IPSAssemblyResult> res = assembler.assemble(perAssemblerItems);
            for (int i = 0; i < res.size(); i++)
            {
               assemblyResultMap.put(perAssemblerItems.get(i), res.get(i));
            }
         }
         finally
         {
            sws.stop();
         }
      }

      if (debugItems.size() > 0)
      {
         IPSAssembler debugAssembler = getAssembler(DEBUG_ASSEMBLER);
         List<IPSAssemblyResult> res = debugAssembler.assemble(debugItems);
         for (int i = 0; i < res.size(); i++)
         {
            assemblyResultMap.put(debugItems.get(i), res.get(i));
         }
      }
   }

   /**
    * Gets to be assembled items by a specific assembler, without the given
    * paginated items and items to be assembled by {@link #DEBUG_ASSEMBLER}.
    * 
    * @param perAssemblerItems the original to be assembled items, which may
    *           include the paginated and debugged items. Assumed not
    *           <code>null</code>, but may be empty.
    * @param paginatedItems the paginated items, assumed not <code>null</code>,
    *           but may be empty.
    * @param debugItems the items to be assembled by {@link #DEBUG_ASSEMBLER},
    *           assumed not <code>null</code>, but may be empty.
    * @param assemblyResultMap the map that maps the item to its assembled
    *           result. This is used to collect the assembled result.
    * 
    * @return the items without paginated and debugged items, never
    *         <code>null</code>, but may be empty.
    */
   private List<IPSAssemblyItem> getAssemblyItems(List<IPSAssemblyItem> perAssemblerItems,
         Set<IPSAssemblyResult> paginatedItems, List<IPSAssemblyItem> debugItems,
         Map<IPSAssemblyItem, IPSAssemblyResult> assemblyResultMap)
   {
      if (!paginatedItems.isEmpty())
      {
         // Paginated items should not be assembled, just pass through
         for (IPSAssemblyResult paginatedItem : paginatedItems)
         {
            String message = "Preview of paginated items in context other than 0 " + "is not supported.";
            paginatedItem.setResultData(message.getBytes());
            assemblyResultMap.put(paginatedItem, paginatedItem);
         }
         perAssemblerItems.removeAll(paginatedItems);
      }

      if (debugItems.size() > 0)
      {
         perAssemblerItems.removeAll(debugItems);
      }

      return perAssemblerItems;
   }

   /**
    * Gets the assembly result and make sure to keep the results in the same
    * order as the input/original assembly items.
    * 
    * @param items the original assembly items, never <code>null</code>, but may
    *           be empty.
    * @param assemblyResultMap the map that maps the item to its assembled
    *           result. This is used to collect the assembled result.
    * 
    * @return the assembly result, which is in the same order as the given
    *         items, never <code>null</code>, but may be empty.
    */
   private List<IPSAssemblyResult> getAssemblyResult(List<IPSAssemblyItem> items,
         Map<IPSAssemblyItem, IPSAssemblyResult> assemblyResultMap)
   {
      // Get the assembly result and make sure to keep the results in the same
      // order as the input assembly items.
      List<IPSAssemblyResult> rval = new ArrayList<>();
      for (IPSAssemblyItem item : items)
      {
         IPSAssemblyResult res = assemblyResultMap.get(item);
         if (res == null)
            continue;
         rval.add(res);
      }

      return rval;
   }

   /**
    * If we're caching content items, check each content item to see if the item
    * should be kept in the in-memory cache.
    *
    * @param items the items, may be empty but never <code>null</code>
    */
   private void checkItemsForEviction(List<IPSAssemblyItem> items)
   {
      long maxSize = m_configurationBean.getMaxCachedContentNodeSize();
      if (maxSize == 0)
         return;

      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start(getClass().getName() + "#checkItemsForEviction");
      try
      {
         for (IPSAssemblyItem i : items)
         {
            try {
               IPSNode n = (IPSNode) i.getNode();
               long size = n.getSizeInBytes();
               if (size > maxSize) {
                  log.debug("Not caching " + n.getGuid() + " which is approx. " + size
                          + " bytes, maximum size allowed is: " + maxSize + " bytes");
                  m_cache.evict(n.getGuid(), CONTENT_REGION);
               }
            } catch (RepositoryException e) {
               log.warn(e.getMessage());
               log.debug(e.getMessage(),e);
            }
         }
      }
      finally
      {
         sws.stop();
      }
   }

   /**
    * Lookup the template for one or more assembly items. The method first
    * creates a map of the content types of the items. Then it runs through the
    * items. The map could be optimized out for cases where only the ids are
    * passed in, but moving toward names this may prove to be the common case.
    *
    * @param items the items to look up the templates for, assumed not
    *           <code>null</code>
    * @throws PSAssemblyException
    */
   public void handleItemTemplates(List<IPSAssemblyItem> items) throws PSAssemblyException
   {
      // Optimize in case the templates are already present, i.e. they
      // were passed into the assembly engine
      boolean allpresent = true;
      for (IPSAssemblyItem item : items)
      {
         if (item.getTemplate() == null)
         {
            allpresent = false;
            break;
         }
      }
      if (allpresent)
         return;

      // First see if any need additional work
      List<Integer> idsToLoad = new ArrayList<>();
      for (IPSAssemblyItem i : items)
      {
         if (i.getTemplate() == null)
         {
            PSLegacyGuid guid = (PSLegacyGuid) i.getId();
            idsToLoad.add(guid.getContentId());
         }
      }

      if (idsToLoad.size() == 0)
         return;

      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      List<PSComponentSummary> summaries = cms.loadComponentSummaries(idsToLoad);
      Map<Integer, Long> contentIdToType = new HashMap<>();
      for (PSComponentSummary s : summaries)
      {
         contentIdToType.put(s.getContentId(), s.getContentTypeId());
      }

      for (IPSAssemblyItem item : items)
      {
         if (item.getTemplate() == null)
         {
            String templatename = item.getParameterValue(IPSHtmlParameters.SYS_TEMPLATE, null);
            String variantid = item.getParameterValue(IPSHtmlParameters.SYS_VARIANTID, null);
            IPSAssemblyTemplate template = null;
            if (templatename == null && variantid == null)
            {
               throw new RuntimeException("No template name or id present");
            }
            else if (StringUtils.isNumeric(templatename) || StringUtils.isNumeric(variantid))
            {
               String idstr = StringUtils.isNumeric(templatename) ? templatename : variantid;
               template = loadUnmodifiableTemplate(idstr);
               if (template == null)
               {
                  log.error("Template could not be loaded for id {}", idstr);
               }
            }
            else if (StringUtils.isNotBlank(templatename) && !StringUtils.isNumeric(templatename))
            {
               PSLegacyGuid guid = (PSLegacyGuid) item.getId();
               Long ctype = contentIdToType.get(guid.getContentId());
               IPSGuid ctypeguid = new PSGuid(PSTypeEnum.NODEDEF, ctype);
               template = findTemplateByNameAndType(templatename, ctypeguid);
               if (template == null)
               {
                  log.error("Template could not be loaded for name {} and type {}", templatename, ctypeguid);
               }
            }
            item.setTemplate(template);
         }
      }
   }

   /**
    * Process jexl bindings for the template and assign results to the assembly
    * item. When this completes, the evaluated bindings from the eval param and
    * the binding process will replace any bindings currently on the work item.
    *
    * @param item the assembly item, assumed non-<code>null</code>
    * @param eval the evaluator, assumed non-<code>null</code>
    */
   @SuppressWarnings("unchecked")
   private void processBindings(IPSAssemblyItem item, PSAssemblyJexlEvaluator eval)
   {
      IPSAssemblyTemplate t = item.getTemplate();
      List<PSTemplateBinding> bindings = t.getBindings();
      for (IPSTemplateBinding binding : bindings)
      {
         if (binding == null)
            continue;
         String var = binding.getVariable();
         IPSScript exp = null;
         try
         {
            exp = ((PSTemplateBinding) binding).getJexlScript();
            exp.setOwnerType(t.getTemplateType().name());
            exp.setOwnerName(t.getName());
            eval.evaluate(var, exp);
         }
         catch (Exception e)
         {
            if (item.isDebug())
            {
               synchronized (eval.getVars())
               {
                  Map<String, Throwable> emap = (Map<String, Throwable>) eval.getVars().get(ERROR_VAR);
                  if (emap == null)
                  {
                     emap = new HashMap<>();
                     eval.getVars().put(ERROR_VAR, emap);
                  }
                  emap.put(var, e);
               }
            }
            else
            {
               String debugMessage = MessageFormat.format("Problem when evaluating expression \"{1}\" "
                     + "for variable \"{0}\": {2}", var != null ? var : "<no variable>", exp != null
                     ? exp.getSourceText()
                     : "<null>", e.getMessage());

                String message = "Problem when evaluating expression : " + exp.getOwnerType() + " : " + exp.getOwnerName();

               log.debug("ERROR ProcessBinding: {} Error: {}", debugMessage, e.getMessage(), e);
               log.error("ERROR ProcessBinding: {} Error: {}", message, e.getMessage());

               throw new RuntimeException(message, e);
            }
         }
      }
      item.setBindings(eval.getVars());
   }

   /**
    * Setup initial variables for assembly. If the item has not yet been loaded
    * from the content manager, that will be done first. Then all initial
    * variables will be bound into the context. Then all available JEXL
    * extensions are loaded and bound (user and system). Last the bindings are
    * evaluated.
    * <p>
    * For legacy items, a more limited set of data is set up to enable global
    * template handling to work correctly.
    *
    * @param work the assembly item, assumed not <code>null</code>
    * @param isLegacy <code>true</code> when assembling legacy templates
    * @return a jexl evaluator, never <code>null</code>
    * @throws PSAssemblyException
    * @throws PSFilterException
    * @throws RepositoryException
    * @throws ValueFormatException
    * @throws UnsupportedRepositoryOperationException
    * @throws PathNotFoundException
    * @throws PSCmsException
    * @throws PSSiteManagerException
    */
   private PSAssemblyJexlEvaluator setupItemForAssembly(IPSAssemblyItem work, boolean isLegacy)
         throws PSAssemblyException, PSFilterException, PSCmsException, PathNotFoundException,
         UnsupportedRepositoryOperationException, ValueFormatException, RepositoryException
   {
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      try
      {
         sws.start(getClass().getName() + "#setupItemForAssembly");

         String siteidstr = work.getParameterValue(IPSHtmlParameters.SYS_SITEID, null);
         String contextstr = work.getParameterValue(IPSHtmlParameters.SYS_CONTEXT, null);

         String sys_command = work.getParameterValue(IPSHtmlParameters.SYS_COMMAND, null);
         String defaultAAMode = IPSHtmlParameters.SYS_AAMODE_ICONS;
         String isHummingbirdEnabled = "false";
         boolean isHBE = false;
         Properties props = PSServer.getServerProps();
         if (props != null) {
            defaultAAMode = StringUtils.defaultIfEmpty((String) props.get("defaultActiveAssemblyMode"),
                  IPSHtmlParameters.SYS_AAMODE_ICONS);
            isHummingbirdEnabled = StringUtils.defaultIfEmpty(
                  (String)props.get("isHummingbirdEnabled"),
                     "false");
         }
         String sys_aamode = work.getParameterValue(IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY_MODE, defaultAAMode);

         boolean isForAaSlot = Boolean.parseBoolean(work.getParameterValue(IPSHtmlParameters.SYS_FORAASLOT,
               Boolean.toString(true)));
         PSAssemblyJexlEvaluator rval = new PSAssemblyJexlEvaluator(work);
         boolean nonHTML = work.getTemplate().getActiveAssemblyType().equals(AAType.NonHtml);
         boolean isAA;
         // As we do not active assemble child items set $sys.activeAssembly to
         // false if it is a child table item.
         boolean isChildTableItem = false;
         if (work.getId() instanceof PSLegacyGuid && ((PSLegacyGuid) work.getId()).isChildGuid())
            isChildTableItem = true;
         if (isForAaSlot && !nonHTML && StringUtils.isNotEmpty(sys_command)
               && sys_command.equals(IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY) && !isChildTableItem)
         {
            rval.bind("$sys.activeAssembly", true);
            isAA = true;
         }
         else
         {
            rval.bind("$sys.activeAssembly", false);
            isAA = false;
         }

         if (isAA && StringUtils.isNotBlank(sys_aamode))
         {
            rval.bind("$sys_aamode", new Integer(sys_aamode));
         }

         if (work.getPage() != null)
         {
            rval.bind("$sys.page", work.getPage());
         }

         if (!work.hasNode())
         {
            loadContentItem(work, rval, isAA);
         }

         if(isHummingbirdEnabled.equals("true")) 
         {
            isHBE = true;
            rval.bind("$sys.isHummingbirdEnabled", isHBE);
         }
         else
         {
            rval.bind("$sys.isHummingbirdEnabled", isHBE);
         }

         /*
          * Setup initial bindings
          */
         try
         {
            if (siteidstr != null && Integer.parseInt(siteidstr) > 0)
            {
               PSSiteHelper.setupSiteInfo(rval, siteidstr, contextstr);
            }
         }
         catch (NumberFormatException | PSNotFoundException e)
         {
            log.warn(e.getMessage());
            // This should not happen at this level...
            log.debug("Skipping site information setting as the supplied siteid is not an integer.", e);
         }

         IPSAssemblyTemplate templ = work.getTemplate();
         if (!isLegacy || templ.getBindings().size() > 0)
         {
            rval.bind("$sys.item", work.getNode());
            rval.bind("$sys.aautils", new PSAAUtils());
         }
         rval.bind("$sys.template", templ.getTemplate());
         rval.bind("$sys.mimetype", templ.getMimeType());
         rval.bind("$sys.charset", templ.getCharset());
         rval.bind("$sys.asm", this);
         rval.bind("$sys.assemblyItem", work);

         return rval;
      }
      finally
      {
         sws.stop();
      }
   }

   /**
    * Load the content item from the data in the parameters. Validates the data
    * as well. Either the contentid + revision or the path must be specified in
    * the parameters. This method is not called if the item is already loaded in
    * the work item.
    *
    * @param work the work item, assumed not null.
    * @param eval the jexl evaluator to set variables upon, assumed never
    *           <code>null</code>.
    * @param isAA if <code>true</code> this assembly is for active assembly, if
    *           <code>false</code> it is not. This is used to keep items loaded
    *           for AA separate as they have embedded links that are different
    *           from non-AA items.
    *
    * @throws PSAssemblyException
    * @throws RepositoryException
    * @throws PSCmsException
    * @throws PathNotFoundException
    * @throws UnsupportedRepositoryOperationException
    * @throws ValueFormatException
    * @throws PSFilterException
    */
   private void loadContentItem(IPSAssemblyItem work, PSAssemblyJexlEvaluator eval, boolean isAA)
         throws PSAssemblyException, RepositoryException, PSCmsException, PathNotFoundException,
         UnsupportedRepositoryOperationException, ValueFormatException, PSFilterException
   {
      Node item = null;
      IPSCacheAccess cache = null;
      ContentCacheKey key = null;
      // long maxSize = m_configurationBean.getMaxCachedContentNodeSize();
      // TODO: reconsider caching post marlin
      long maxSize = 0;

      if (maxSize > 0)
      {
         cache = PSCacheAccessLocator.getCacheAccess();
         key = new ContentCacheKey(work.getId(), work.getFilter().getGUID(), isAA, work.getContext());
         item = (Node) cache.get(key, CONTENT_REGION);
      }
      if (item == null)
      {
         IPSContentMgr contentmgr = PSContentMgrLocator.getContentMgr();
         // Handle content lookup
         Collection<Node> items = null;

         List<IPSGuid> guids = new ArrayList<>();
         guids.add(work.getId());

         try
         {
            String siteid = work.getParameterValue(IPSHtmlParameters.SYS_SITEID, "");
            Integer stid = !StringUtils.isBlank(siteid) && StringUtils.isNumeric(siteid) ? new Integer(siteid) : null;
            PSContentMgrConfig config = new PSContentMgrConfig();
            config.addOption(PSContentMgrOption.LAZY_LOAD_CHILDREN);
            config.addOption(PSContentMgrOption.LOAD_MINIMAL);
            config.setBodyAccess(new PSInlineLinkProcessor(work.getFilter(), work));
            config.setNamespaceCleanup(new PSNamespaceCleanup(stid));
            // Fix for CMS-3796 <div class="rxbodyfield"> being removed.
            //config.setDivTagCleanup(new PSDivTagCleanup());
            items = contentmgr.findItemsByGUID(guids, config);
         }
         catch (PSFilterException | PSNotFoundException e)
         {
            throw new PSAssemblyException(IPSAssemblyErrors.PARAMS_AUTHTYPE_OR_FILTER, e);
         }

         if (items.size()==0)
            throw new ItemNotFoundException("Can't find item for guid: " + work.getId());
         
         item = items.iterator().next();

         PSContentNode node = (PSContentNode) item;
         if (maxSize > 0 && node.getSizeInBytes() < maxSize)
         {
            cache.save(key, node, CONTENT_REGION);
         }
      }

      // Check to see if this item is a managed nav object. If it is we'll
      // wrap it up into a proxy
      PSNavConfig navcfg = PSNavConfig.getInstance();
      IPSGuid navontype = navcfg.getNavonType();
      IPSGuid navtreetype = navcfg.getNavTreeType();
      IPSGuid itemtype = new PSGuid(PSTypeEnum.NODEDEF, item.getProperty("sys_contenttypeid").getString());
      if (itemtype.equals(navontype) || itemtype.equals(navtreetype))
      {
         PSNavHelper h = work.getNavHelper();
         PSLegacyGuid lg = (PSLegacyGuid) work.getId();
         item = h.getNavNode(lg.getLocator());
         h.setupNavValues(work, eval, item);
      }
      work.setNode(item);
   }

   public PSTypeEnum[] getTypes()
   {
      PSTypeEnum rval[] = new PSTypeEnum[]
      {PSTypeEnum.TEMPLATE, PSTypeEnum.SLOT};

      return rval;
   }

   public List<IPSCatalogSummary> getSummaries(PSTypeEnum type)
   {
      List<IPSCatalogSummary> rval = new ArrayList<>();

      if (type.equals(PSTypeEnum.TEMPLATE))
         rval  = findAllTemplates().stream()
                 .map(t -> new PSObjectSummary(t.getGUID(), t.getName(), t.getLabel(), t.getDescription())).collect(Collectors.toList());
      else if (type.equals(PSTypeEnum.SLOT))
         rval  = findAllSlots().stream()
                 .map(t -> new PSObjectSummary(t.getGUID(), t.getName(), t.getLabel(), t.getDescription())).collect(Collectors.toList());


      return rval;
   }

   @Transactional
   public void loadByType(PSTypeEnum type, String item) throws PSCatalogException
   {
      Session s = sessionFactory.getCurrentSession();
      try
      {
         if (type.equals(PSTypeEnum.TEMPLATE))
         {
            PSAssemblyTemplate temp;
            IPSGuid guid = PSXmlSerializationHelper.getIdFromXml(PSTypeEnum.TEMPLATE, item);
            Integer tversion = null;
            Map<Long, Integer> bversions = null;

            try
            {
               temp = loadTemplate(guid, false);
               // Remember the versions of the template and bindings so
               // they can be restored
               tversion = temp.getVersion();
               bversions = new HashMap<>();
               for (IPSTemplateBinding b : temp.getBindings())
               {
                  PSTemplateBinding binding = (PSTemplateBinding) b;
                  bversions.put(binding.getId(), binding.getVersion());
               }
               temp.setVersion(null);
            }
            catch (PSAssemblyException e)
            {
               temp = createTemplate();
            }
            temp.fromXML(item);

            // Restore bindings if available
            if (tversion != null)
            {
               temp.setVersion(null);
               temp.setVersion(tversion);
               for (IPSTemplateBinding b : temp.getBindings())
               {
                  PSTemplateBinding binding = (PSTemplateBinding) b;
                  Integer bversion = bversions.get(binding.getId());
                  if (bversion != null)
                  {
                     binding.setVersion(null);
                     binding.setVersion(bversion);
                  }
               }
               s.merge(temp);
            }

            saveTemplate(temp);
         }
         else if (type.equals(PSTypeEnum.SLOT))
         {
            IPSGuid guid = PSXmlSerializationHelper.getIdFromXml(PSTypeEnum.SLOT, item);
            IPSTemplateSlot temp;
            if (findSlot(guid) != null)
            {
               temp = loadSlotModifiable(guid);
               ((PSTemplateSlot) temp).setVersion(null);
            }
            else
            {
               temp = createSlot();
            }
            temp.fromXML(item);
            saveSlot(temp);
         }
         else
         {
            throw new PSCatalogException(IPSCatalogErrors.UNKNOWN_TYPE, type.toString());
         }
      }
      catch (PSAssemblyException e)
      {
         throw new PSCatalogException(IPSCatalogErrors.REPOSITORY, e, type);
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

   public String saveByType(IPSGuid id) throws PSCatalogException
   {
      try
      {
         if (id.getType() == PSTypeEnum.TEMPLATE.getOrdinal())
         {
            IPSAssemblyTemplate temp = loadTemplate(id, true);
            return temp.toXML();
         }
         else if (id.getType() == PSTypeEnum.SLOT.getOrdinal())
         {
            IPSTemplateSlot slot = loadSlot(id);
            return slot.toXML();
         }
         else
         {
            PSTypeEnum type = PSTypeEnum.valueOf(id.getType());
            throw new PSCatalogException(IPSCatalogErrors.UNKNOWN_TYPE, type.toString());
         }
      }
      catch (PSAssemblyException e)
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

   public PSAssemblyTemplate createTemplate()
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      PSAssemblyTemplate newvar = new PSAssemblyTemplate();
      newvar.setGUID(gmgr.createGuid(PSTypeEnum.TEMPLATE));
      return newvar;
   }

   @Transactional
   public PSAssemblyTemplate loadTemplate(String guidstr, boolean loadSlots) throws PSAssemblyException
   {
      IPSGuid guid = new PSGuid(PSTypeEnum.TEMPLATE, guidstr);
      PSAssemblyTemplate template = loadTemplate(guid, loadSlots);
      Hibernate.initialize(template);
      return template;
   }

   @Transactional
   public PSAssemblyTemplate loadTemplate(IPSGuid id, boolean loadSlots) throws PSAssemblyException
   {
      PSAssemblyTemplate var = findTemplate(id, loadSlots);
      Hibernate.initialize(var);
      if (var == null)
      {
         throw new PSAssemblyException(IPSAssemblyErrors.TEMPLATE_MISSING, id);
      }
      return var;
   }

   public IPSAssemblyTemplate loadUnmodifiableTemplate(IPSGuid tid) throws PSAssemblyException
   {
      if (tid == null)
      {
         throw new IllegalArgumentException("tid may not be null");
      }
      IPSAssemblyTemplate var = findTemplate(tid);
      if (var == null)
      {
         throw new PSAssemblyException(IPSAssemblyErrors.TEMPLATE_MISSING, tid);
      }
      return var;
   }

   public IPSAssemblyTemplate findTemplate(IPSGuid tid)
   {
      if (tid == null)
      {
         throw new IllegalArgumentException("tid may not be null");
      }

      return findTemplate(tid, true);
   }

   /**
    * Gets a Template from the repository.
    *
    * @param id the ID of the template, assumed not <code>null</code>.
    * @param loadSlots if <code>true</code> loading all associated slots;
    *           otherwise don't load the slots.
    *
    * @return the Template. It may be <code>null</code> if the Template does not
    *         exist.
    */
   @Transactional
   public PSAssemblyTemplate findTemplate(IPSGuid id, boolean loadSlots)
   {
      Session session = sessionFactory.getCurrentSession();

         PSAssemblyTemplate var = session.get(PSAssemblyTemplate.class, id.longValue());

         if (var == null)
         {
            // Try masked id to address issues with legacy ids
            IPSGuid nid = new PSGuid(id.getHostId(), PSTypeEnum.INTERNAL, id.getUUID());
            var = session.get(PSAssemblyTemplate.class, nid.longValue());
         }

         if (var == null)
         {
            return null;
         }

         if (loadSlots)
         {
            forceSlotLoad(var);
         }

         return var;

   }

   public IPSAssemblyTemplate loadUnmodifiableTemplate(String tidstr) throws PSAssemblyException
   {
      IPSGuid guid = new PSGuid(PSTypeEnum.TEMPLATE, tidstr);
      return loadUnmodifiableTemplate(guid);
   }

   /**
    * Force the related template slots to be loaded for all supplied templates.
    *
    * @param templates the templates, assumed not <code>null</code>.
    */
   private void forceSlotLoad(List<IPSAssemblyTemplate> templates)
   {
      for (IPSAssemblyTemplate template : templates)
         forceSlotLoad(template);
   }

   /**
    * Force the related template slots to be loaded for the passed template.
    *
    * @param template the template, assumed never <code>null</code>
    */
   private void forceSlotLoad(IPSAssemblyTemplate template)
   {
      try
      {
         // Force slot loading
         for (IPSTemplateSlot slot : template.getSlots())
         {
            slot.getGUID();
         }
      }
      catch (RuntimeException re)
      {
         log.error("Couldn't force load one or more slots for template: {}, Error: {}",
                  template.getName(), re.getMessage());
         log.debug(re.getMessage(),re);
      }
   }

   @Transactional
   public void saveTemplate(IPSAssemblyTemplate var) throws PSAssemblyException
   {
      Session sess = sessionFactory.getCurrentSession();
      try
      {
         // ideally, we need to make sure the saved object is not in the
         // "memory" region of EHcache. However, this strategy does not work
         // due to the following scenario:
         // EHcache may contain the objects that are retrieved from the 1st
         // level cache of hibernate, and loadSlotModifiable() may also retrieve
         // the objects from the 1st level cache.
         //
         // so we arse not check the saved object against the object stored in
         // "memory" region of EHcache, which is the same way in 6.5.2.
         sess.saveOrUpdate(var);
         sess.flush();

         // the object will be evicted by the framework,
         // see PSEhCacheAccessor.notifyEvent()
      }

      catch (Exception e)
      {
         log.error("Failed to save template id=" + var.getGUID().toString() + ", name=" + var.getName(), e);
         throw new PSAssemblyException(IPSAssemblyErrors.UNKNOWN_CRUD_ERROR, e);
      }

   }




   @SuppressWarnings("unchecked")
   public PSAssemblyTemplate findTemplateByName(String name) throws PSAssemblyException
   {
      if (name == null || StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      PSAssemblyTemplate template = sessionFactory.getCurrentSession().bySimpleNaturalId(PSAssemblyTemplate.class).load(name);
      if (template == null)
      {
         throw new PSAssemblyException(IPSAssemblyErrors.TEMPLATE_MISSING, name);
      }
      return template;
   }

   @SuppressWarnings("unchecked")
   public IPSAssemblyTemplate findTemplateByNameAndType(String name, IPSGuid contenttype) throws PSAssemblyException
   {

      PSAssemblyTemplate template = findTemplateByName(name);

      List<IPSAssemblyTemplate> templateTypes = findTemplatesByContentType(contenttype);

      if (templateTypes.stream().anyMatch(temp -> temp.getGUID() == template.getGUID()))
         return template;
      else
         throw new PSAssemblyException(IPSAssemblyErrors.TEMPLATE_BY_ID_MISSING, name, contenttype.longValue());

   }

   @Transactional
   @SuppressWarnings("unchecked")
   public List<IPSAssemblyTemplate> findTemplatesByAssemblyUrl(String url, boolean loadSlot)
   {
      Session session = sessionFactory.getCurrentSession();
      List<IPSAssemblyTemplate> templates;

         Criteria c = session.createCriteria(PSAssemblyTemplate.class);
         c.setCacheable(true);
         c.setCacheRegion(CACHE_REGION);
         c.add(Restrictions.like("assemblyUrl", url));

         templates = c.list();

         if (loadSlot)
         {
            forceSlotLoad(templates);
         }

      return templates;
   }

   @Transactional
   @SuppressWarnings("unchecked")
   public List<IPSAssemblyTemplate> findTemplatesBySlot(IPSTemplateSlot slot) throws PSAssemblyException
   {
      List<IPSAssemblyTemplate> rval;
      Session session = sessionFactory.getCurrentSession();

         Criteria c = session.createCriteria(PSAssemblyTemplate.class);
         c.setCacheable(true);
         c.setCacheRegion(CACHE_REGION);
         Criteria slots = c.createCriteria("slots");
         slots.add(Restrictions.eq("id", slot.getGUID().longValue()));
         c.setProjection(Projections.id());
         // List of templates
         List<Long> templateIds = c.list();
         rval = new ArrayList<>();
         for (Long l : templateIds)
         {
            IPSGuid templateid = new PSGuid(PSTypeEnum.TEMPLATE, l);
            IPSAssemblyTemplate templ = loadTemplate(templateid, true);
            rval.add(templ);
         }


      return rval;
   }

   @Transactional
   @SuppressWarnings("unchecked")
   public List<IPSAssemblyTemplate> findTemplatesByContentType(IPSGuid contenttype) {
      Session sess = sessionFactory.getCurrentSession();
      return sess.get(PSNodeDefinition.class, contenttype.longValue())
              .getCvDescriptors()
              .stream()
              .map(t -> sess.load(PSAssemblyTemplate.class,Long.valueOf(t.getTemplateId().getUUID())))
              .map(t -> {
                 Hibernate.initialize(t); return t;})
              .collect(Collectors.toList());
   }

   @Transactional
   @SuppressWarnings("unchecked")
   public List<IPSAssemblyTemplate> findTemplates(String name, String contentType,
         Set<IPSAssemblyTemplate.OutputFormat> outputFormats, IPSAssemblyTemplate.TemplateType type,
         Boolean globalFilter, Boolean legacyFilter, String assembler) throws PSAssemblyException
   {
      Session session = sessionFactory.getCurrentSession();
      try
      {
         // get all templates if no name was specified
         if (StringUtils.isBlank(name))
            name = "%";

         Criteria c = session.createCriteria(PSAssemblyTemplate.class);
         c.setCacheable(true);
         c.setCacheRegion(CACHE_REGION);
         c.add(Restrictions.ilike("name", name));
         if (outputFormats != null && !outputFormats.isEmpty())
         {
            Integer[] outputFormatOrdinals = new Integer[outputFormats.size()];
            int index = 0;
            for (IPSAssemblyTemplate.OutputFormat outputFormat : outputFormats)
               outputFormatOrdinals[index++] = outputFormat.ordinal();
            c.add(Restrictions.in("outputFormat", (Object[]) outputFormatOrdinals));
         }
         if (type != null)
            c.add(Restrictions.eq("templateType", type.ordinal()));
         if (!StringUtils.isBlank(assembler))
            c.add(Restrictions.like("assembler", assembler));
         if (globalFilter != null)
         {
            if (globalFilter)
               c.add(Restrictions.eq("outputFormat", OutputFormat.Global.ordinal()));
            else
               c.add(Restrictions.ne("outputFormat", OutputFormat.Global.ordinal()));
         }
         if (legacyFilter != null)
         {
            if (legacyFilter)
            {
               c.add(Restrictions.or(Restrictions.eq("assembler", IPSExtension.LEGACY_ASSEMBLER),
                     Restrictions.isNull("assembler")));
            }
            else
            {
               c.add(Restrictions.ne("assembler", IPSExtension.LEGACY_ASSEMBLER));
            }
         }
         c.addOrder(Order.asc("name"));
         Set<IPSAssemblyTemplate> templates = new HashSet<>(c.list());
         Set<IPSAssemblyTemplate> cttemplates = new HashSet<>();

         if (!StringUtils.isBlank(contentType) && !contentType.equals("%"))
         {
            IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
            List<IPSNodeDefinition> defs = cmgr.findNodeDefinitionsByName(contentType);
            for (IPSNodeDefinition def : defs)
               cttemplates.addAll(findTemplatesByContentType(def.getGUID()));

            Set<IPSAssemblyTemplate> results = templates.stream().filter(cttemplates::contains).collect(Collectors.toSet());

            templates = results;
         }

         List<IPSAssemblyTemplate> resultList = new ArrayList<>(templates);
         forceSlotLoad(resultList);

         return resultList;
      }
      catch (RepositoryException e)
      {
         throw new PSAssemblyException(IPSAssemblyErrors.UNKNOWN_ERROR, e, e.getLocalizedMessage());
      }

   }

   @Transactional
   @SuppressWarnings("unchecked")
   public Set<IPSAssemblyTemplate> findAllTemplates()
   {
      List<IPSAssemblyTemplate> list = sessionFactory.getCurrentSession().createCriteria(PSAssemblyTemplate.class).list();

      return list == null ? Collections.emptySet() : new HashSet<>(list);
   }

   /**
    * Find all the slots in the database.
    *
    * @return a set of slots, could be empty but never <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private List<IPSTemplateSlot> findAllSlots()
   {
      return sessionFactory.getCurrentSession().createCriteria(PSTemplateSlot.class).list();
   }

   @Transactional
   @SuppressWarnings("unchecked")
   public Set<IPSAssemblyTemplate> findAllGlobalTemplates()
   {
      Set<IPSAssemblyTemplate> rval;
      Session session = sessionFactory.getCurrentSession();

         Criteria c = session.createCriteria(PSAssemblyTemplate.class);
         c.add(Restrictions.eq("outputFormat", OutputFormat.Global.ordinal()));
         rval = new HashSet<>(c.list());
         for (IPSAssemblyTemplate template : rval)
         {
            forceSlotLoad(template);
         }


      return rval;
   }

   // see base
   public Set<String> findAll57GlobalTemplates() throws PSAssemblyException
   {
      final Set<String> templateNames = new HashSet<>();
      final File[] stylesheets = getGlobalTemplatesDir().listFiles(getXslFileFilter());
      if (stylesheets == null)
      {
         return templateNames;
      }
      try
      {
         for (final File stylesheet : stylesheets)
         {
            if (stylesheetContainsRootTemplate(stylesheet))
            {
               templateNames.add(extractXslFileName(stylesheet));
            }
         }
      }
      catch (IOException | SAXException e)
      {
         throw new PSAssemblyException(EXCEPTION_MSG, e);
      }
      return templateNames;
   }

   /**
    * Extracts the base XSL file name from the provided stylesheet.
    * 
    * @param stylesheet the stylesheet file to extract file name from. Assumed
    *           not <code>null</code> and that it ends with
    *           {@link #XSL_EXTENSION}.
    * @return the XSL file name without extension.
    */
   private String extractXslFileName(final File stylesheet)
   {
      String name = stylesheet.getName();
      name = name.substring(0, name.indexOf(XSL_EXTENSION));
      return name;
   }

   /**
    * Reads the provided stylesheet and checks that the stylesheet template name
    * corresponds to the provided name.
    *
    * @param stylesheet the stylesheet file. Assumed not <code>null</code> and
    *           that it ends with {@link #XSL_EXTENSION}.
    * @return
    * @throws IOException
    * @throws SAXException
    */
   private boolean stylesheetContainsRootTemplate(File stylesheet) throws IOException, SAXException
   {
      final String name = extractXslFileName(stylesheet);
      boolean foundRootTemplate = false;
      try (InputStream is = new FileInputStream(stylesheet)) {

         Document doc = PSXmlDocumentBuilder.createXmlDocument(is, false);

         NodeList templates = doc.getElementsByTagName("xsl:template");
         for (int j = 0; j < templates.getLength() && !foundRootTemplate; j++) {
            Element template = (Element) templates.item(j);
            String templateName = template.getAttribute(NAME_ATTR);
            int pos = templateName.indexOf(ROOT_EXTENSION);
            if (pos != -1) {
               foundRootTemplate = name.equals(templateName.substring(0, pos));
            }
         }

      }
      return foundRootTemplate;
   }

   @Transactional
   public void deleteTemplate(IPSGuid id) throws PSAssemblyException
   {
      try
      {
         IPSAssemblyTemplate template = loadTemplate(id, false);
         sessionFactory.getCurrentSession().delete(template);
         // The saved object will be (indirectly) evicted by the framework
      }
      catch (DataAccessException e)
      {
         throw new PSAssemblyException(IPSAssemblyErrors.UNKNOWN_CRUD_ERROR, e);
      }
   }

   public IPSTemplateSlot createSlot()
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSTemplateSlot newslot = new PSTemplateSlot();
      newslot.setGUID(gmgr.createGuid(PSTypeEnum.SLOT));
      return newslot;
   }

   @Transactional
   public IPSTemplateSlot loadSlot(String idstr) throws PSAssemblyException {
      return loadSlot(new PSGuid(PSTypeEnum.SLOT, idstr));
   }

   @Transactional
   public IPSTemplateSlot loadSlot(IPSGuid id) throws PSAssemblyException {
      IPSTemplateSlot slot = findSlot(id);
      if (slot == null)
      {
         throw new PSAssemblyException(PSAssemblyException.SLOT_NOT_FOUND);
      }

      return slot;
   }

   @Transactional
   public IPSTemplateSlot loadSlotModifiable(IPSGuid id) throws PSAssemblyException {
      IPSTemplateSlot slot = getSlotById(id);
      if (slot == null)
      {
         throw new PSAssemblyException(PSAssemblyException.SLOT_NOT_FOUND,id);
      }

      return slot;
   }

   public IPSTemplateSlot findSlot(IPSGuid id)
   {
      if (id == null)
      {
         throw new IllegalArgumentException("id may not be null");
      }
      IPSCacheAccess cache = getCache();
      IPSTemplateSlot rval = (IPSTemplateSlot) cache.get(id, IPSCacheAccess.IN_MEMORY_STORE);
      if (rval == null)
      {
         rval = getSlotById(id);
         if (rval != null)
            cache.save(id, rval, IPSCacheAccess.IN_MEMORY_STORE);
      }
      return rval;
   }

   /**
    * Gets the slot from the repository.
    * 
    * @param id the ID of the requested slot, assumed not <code>null</code>.
    * @return the slot object, which may be <code>null</code> if the slot does
    *         not exist.
    */
   @Transactional
   public IPSTemplateSlot getSlotById(IPSGuid id)
   {
      return sessionFactory.getCurrentSession().get(PSTemplateSlot.class, id.longValue());
   }

   @Transactional
   public List<IPSTemplateSlot> loadSlots(List<IPSGuid> ids) throws PSAssemblyException
   {
      List<IPSTemplateSlot> slots = new ArrayList<>();
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException("ids cannot be null or empty");

      for(IPSGuid g : ids){
         slots.add(loadSlot(g));
      }
      return slots;
   }

   @Transactional
   public void saveSlot(IPSTemplateSlot slot) throws PSAssemblyException
   {
      Session s = sessionFactory.getCurrentSession();
      try
      {
         // ideally, we need to make sure the saved object is not in the
         // "memory" region of EHcache. However, this strategy does not work
         // due to the following scenario:
         // EHcache may contain the objects that are retrieved from the 1st
         // level cache of hibernate, and loadSlotModifiable() may also retrieve
         // the objects from the 1st level cache.
         //
         // so we are not check the saved object against the object stored in
         // "memory" region of EHcache, which is the same way in 6.5.2.

         if ( slot.getGUID() != null && !s.contains(slot) && s.load(PSTemplateSlot.class, slot.getGUID().longValue())!=null) {
            s.merge(slot);
         }
         else {

            s.saveOrUpdate( slot );
         }

         // the object will be evicted by the framework,
         // see PSEhCacheAccessor.notifyEvent()
      }
      catch (Exception e)
      {
         log.error("Failed to save slot id=" + slot.getGUID().toString() + ", name=" + slot.getName(), e);
         throw new PSAssemblyException(IPSAssemblyErrors.UNKNOWN_CRUD_ERROR, e);
      }
      finally
      {
         s.flush();

      }
   }

   @Transactional
   @SuppressWarnings("unchecked")
   public IPSTemplateSlot findSlotByName(String name) throws PSAssemblyException
   {
      PSTemplateSlot slot = sessionFactory.getCurrentSession().bySimpleNaturalId(PSTemplateSlot.class).load(name);

      if (slot == null)
      {
         throw new PSAssemblyException(IPSAssemblyErrors.MISSING_SLOT, name);
      }
      return slot;

   }

   @Transactional
   @SuppressWarnings("unchecked")
   public List<IPSTemplateSlot> findSlotsByName(String name)
   {
      Session session = sessionFactory.getCurrentSession();
         // get all slots if no name was specified
         if (StringUtils.isBlank(name) || name.equals("%"))
            return findAllSlots();

         if (!name.contains("%"))
         {
            PSTemplateSlot slot = session.bySimpleNaturalId(PSTemplateSlot.class).load(name);
            return slot == null ? Collections.emptyList() : Collections.singletonList(slot);
         }

         Criteria c = session.createCriteria(PSTemplateSlot.class);
         c.setCacheable(true);
         c.setCacheRegion(CACHE_REGION);
         c.add(Restrictions.ilike("name", name));
         c.addOrder(Order.asc("name"));
         return c.list();

   }

   @Transactional
   @SuppressWarnings("unchecked")
   public List<IPSTemplateSlot> findSlotsByNames(List<String> names)
   {
      if (names == null)
      {
         throw new IllegalArgumentException("names may not be null");
      }

      return names.stream().map( name -> sessionFactory.getCurrentSession()
              .bySimpleNaturalId(PSTemplateSlot.class).load(name))
              .collect(Collectors.toList());
   }

   @Transactional
   public void deleteSlot(IPSGuid id) throws PSAssemblyException
   {
      try
      {
         IPSTemplateSlot slot = loadSlot(id);
         sessionFactory.getCurrentSession().delete(slot);
         // The deleted object will be (indirectly) evicted by the framework
      }
      catch (DataAccessException e)
      {
         throw new PSAssemblyException(IPSAssemblyErrors.UNKNOWN_CRUD_ERROR, e);
      }
   }

   @SuppressWarnings("unchecked")
   public IPSSlotContentFinder loadFinder(String finder) throws PSAssemblyException
   {
      if (StringUtils.isBlank(finder))
      {
         throw new IllegalArgumentException("finder may not be null or empty");
      }

      IPSExtensionManager emgr = PSServer.getExtensionManager(null);
      try
      {
         PSExtensionRef ref = new PSExtensionRef(finder);
         return (IPSSlotContentFinder) emgr.prepareExtension(ref, null);
      }
      catch (PSExtensionException | com.percussion.design.objectstore.PSNotFoundException e)
      {
         throw new PSAssemblyException(IPSAssemblyErrors.MISSING_FINDER, e);
      }
   }

   @Transactional
   @SuppressWarnings("unchecked")
   public String getLandingPageLink(IPSAssemblyItem parentItem, Node landingPage, IPSGuid templateId)
         throws PSAssemblyException
   {
      // Load the template and decide what to do
      IPSAssemblyTemplate template = loadUnmodifiableTemplate(templateId);
      String url = null;
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start("getLandingPage");
      try
      {
         PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
         IPSNode cn = (IPSNode) landingPage;
         PSLegacyGuid lg = (PSLegacyGuid) cn.getGuid();
         Property fidprop = null;
         Property sidprop = null;
         // get folderid and siteid properties
         try
         {
            fidprop = landingPage.getProperty(PSNavHelper.PROP_NAV_LANDINPAGE_FOLDERID);
         }
         catch (Exception e)
         {
            // Not having folder id property is not an error.
            // Ignore it.
         }
         try
         {
            sidprop = landingPage.getProperty(PSNavHelper.PROP_NAV_LANDINPAGE_SITEID);
         }
         catch (Exception e)
         {
            // Not having siteid property is not an error.
            // Ignore it.
         }
         String fid = fidprop == null ? "" : StringUtils.defaultString(fidprop.getString());
         String sid = sidprop == null ? "" : StringUtils.defaultString(sidprop.getString());
         if (StringUtils.isBlank(sid) && parentItem.getSiteId() != null)
         {
            sid = String.valueOf(parentItem.getSiteId().longValue());
         }
         if (StringUtils.isBlank(fid) && StringUtils.isNotBlank(sid))
         {
            IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
            IPSGuid siteguid = PSGuidManagerLocator.getGuidMgr().makeGuid(sid, PSTypeEnum.SITE);
            IPSGuid fguid = smgr.getSiteFolderId(siteguid, lg);
            if (fguid != null)
            {
               PSLegacyGuid flg = (PSLegacyGuid) fguid;
               fid = Integer.toString(flg.getContentId());
            }
         }

         List<PSLocator> folders = processor.getParents(PSRelationshipConfig.TYPE_FOLDER_CONTENT, lg.getLocator());

         if (folders == null || folders.size() == 0)
         {
            throw new IllegalStateException("Navon must be contained in a folder: " + lg);
         }

         IPSAssemblyResult clone = (IPSAssemblyResult) parentItem.clone();

         // Use the landing page template and node
         clone.setNode(landingPage);
         clone.setTemplate(null);
         clone.setParameterValue(IPSHtmlParameters.SYS_TEMPLATE, Long.toString(templateId.longValue()));
         clone.setParameterValue(IPSHtmlParameters.SYS_CONTENTID, Integer.toString(lg.getContentId()));
         clone.setParameterValue(IPSHtmlParameters.SYS_REVISION, Integer.toString(lg.getRevision()));
         if (StringUtils.isBlank(fid))
         {
            fid = Integer.toString(folders.get(0).getId());
         }
         clone.setParameterValue(IPSHtmlParameters.SYS_FOLDERID, fid);
         if (StringUtils.isNotBlank(sid))
         {
            clone.setParameterValue(IPSHtmlParameters.SYS_SITEID, sid);
            clone.setSiteId(PSGuidManagerLocator.getGuidMgr().makeGuid(sid, PSTypeEnum.SITE));
         }

         clone.setTemplate(template);

         if (StringUtils.isBlank(template.getAssembler())
               || template.getAssembler().equals(IPSExtension.LEGACY_ASSEMBLER))
         {
            // Assemble snippet
            List<IPSAssemblyItem> items = new ArrayList<>();
            items.add(clone);
            List<IPSAssemblyResult> results = assemble(items);
            if (results == null || results.size() == 0)
            {
               throw new PSAssemblyException(IPSAssemblyErrors.LANDING_PAGE_URL_1, lg);
            }
            else
            {
               IPSAssemblyResult result = results.get(0);
               String doc = result.toResultString();
               Reader r = new StringReader(doc);

               XMLInputFactory fact = PSSecureXMLUtils.getSecuredXMLInputFactory(false);

               XMLEventReader reader = fact.createXMLEventReader(r);
               while (reader.hasNext())
               {
                  XMLEvent event = reader.nextEvent();
                  if (event.isStartElement())
                  {
                     StartElement e = event.asStartElement();
                     if (e.getName().getLocalPart().equalsIgnoreCase("a"))
                     {
                        // Get href, and we're done
                        PSXmlEncoder enc = new PSXmlEncoder();
                        Attribute a = e.getAttributeByName(new QName("href"));
                        return (String) enc.encode(a.getValue());
                     }
                  }
               }

            }
         }
         else
         {
            PSAssemblyJexlEvaluator eval = setupItemForAssembly(clone, false);
            processBindings(clone, eval);
            // Extract the information we need
            url = (String) eval.evaluate(ms_pagelink);
            if (url == null)
            {
               throw new PSAssemblyException(IPSAssemblyErrors.MISSING_PAGELINK, templateId);
            }
         }
      }
      catch (PSAssemblyException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         log.error("Problem extracting URL from landing page snippet", e);
      }
      finally
      {
         sws.stop();
      }

      return url;
   }



   public void setCurrentAssemblyItem(IPSAssemblyItem item)
   {
      if (item == null)
         ms_item.remove();
      else
         ms_item.set(item);
   }

   public IPSAssemblyItem getCurrentAssemblyItem()
   {
      return ms_item.get();
   }

   /**
    * @return the configurationBean
    */
   public PSServiceConfigurationBean getConfigurationBean()
   {
      return m_configurationBean;
   }

   /**
    * @param configurationBean the configurationBean to set
    */
   @Autowired
   public void setConfigurationBean(PSServiceConfigurationBean configurationBean)
   {
      m_configurationBean = configurationBean;
   }

   /**
    * @return the nsvc
    */
   public IPSNotificationService getNotificationService()
   {
      return m_nsvc;
   }

   /**
    * @param nsvc the nsvc to set
    */
   @Autowired
   public void setNotificationService(IPSNotificationService nsvc)
   {
      m_nsvc = nsvc;

      if (m_nsvc != null)
      {
         IPSNotificationListener listener = new AssemblyContentChangedListener();
         m_nsvc.addListener(EventType.CONTENT_CHANGED, listener);
         m_nsvc.addListener(EventType.OBJECT_INVALIDATION, listener);
         // Listener for the maps
         nsvc.addListener(EventType.OBJECT_INVALIDATION, new PSAssemblyNotificationListener());
      }
   }

   /**
    * Spring property accessor.
    *
    * @return get the cache service
    */
   public IPSCacheAccess getCache()
   {
      return m_cache;
   }

   /**
    * Set the cache service.
    *
    * @param cache the service, never <code>null</code>
    */
   @Autowired
   public void setCache(IPSCacheAccess cache)
   {
      if (cache == null)
      {
         throw new IllegalArgumentException("cache may not be null");
      }
      m_cache = cache;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.assembly.IPSTemplateService#createBindings(java
    * .util.LinkedHashMap)
    */
   public List<PSTemplateBinding> createBindings(LinkedHashMap<String, String> bindings, int startingOrder)
   {
      if (bindings == null)
         throw new IllegalArgumentException("bindings must not be null");

      List<PSTemplateBinding> results = new ArrayList<>();

      for (Map.Entry<String, String> entry : bindings.entrySet())
      {
         if (StringUtils.isBlank(entry.getKey()))
            throw new IllegalArgumentException("the key of the bindings map must not be blank.");
         results.add(new PSTemplateBinding(entry.getKey(), entry.getValue()));
      }

      return results;
   }

   /**
    * Creates a filter filtering XSL files.
    * 
    * @return the XSL files filter. Never <code>null</code>.
    */
   private FileFilter getXslFileFilter()
   {
      return pathname -> pathname.getPath().toLowerCase().endsWith(XSL_EXTENSION);
   }

   /**
    * Directory where legacy 5.7 global templates reside.
    * 
    * @return the global templates directory. Never <code>null</code>.
    */
   private File getGlobalTemplatesDir()
   {
      return new File(PSRxGlobals.ABS_GLOBAL_TEMPLATES_PATH);
   }

   /**
    * The error number indicating to <code>PSException</code> to use the message
    * from the provided exception.
    */
   private static final int EXCEPTION_MSG = 1002;

   /**
    * The file extension used for XSL files.
    */
   private static final String XSL_EXTENSION = ".xsl";

   /**
    * The extension appended to the file name for the root template name.
    */
   private static final String ROOT_EXTENSION = "_root";

   // private XML constants
   private static final String NAME_ATTR = "name";

}
