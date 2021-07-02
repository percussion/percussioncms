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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.contentmgr.impl.legacy;

import com.percussion.cms.IPSEditorChangeListener;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSEditorChangeEvent;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.objectstore.*;
import com.percussion.cms.objectstore.server.IPSItemDefElementProcessor;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.IPSHandlerInitListener;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.*;
import com.percussion.services.contentmgr.data.*;
import com.percussion.services.contentmgr.impl.IPSContentRepository;
import com.percussion.services.contentmgr.impl.IPSTypeKey;
import com.percussion.services.contentmgr.impl.PSContentInternalLocator;
import com.percussion.services.contentmgr.impl.PSContentUtils;
import com.percussion.services.contentmgr.impl.legacy.PSTypeConfiguration.GeneratedClassBase;
import com.percussion.services.contentmgr.impl.query.IPSFolderExpander;
import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode;
import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode.Op;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeComparison;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeIdentifier;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeValue;
import com.percussion.services.contentmgr.impl.query.visitors.*;
import com.percussion.services.datasource.PSDatasourceMgrLocator;
import com.percussion.services.datasource.UpperCaseNamingStrategy;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationHelper;
import com.percussion.services.relationship.data.PSRelationshipData;
import com.percussion.services.utils.orm.data.PSTempId;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSBaseBean;
import com.percussion.util.PSStopwatch;
import com.percussion.utils.beans.PSPropertyWrapper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jdbc.IPSDatasourceManager;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jsr170.IPSPropertyInterceptor;
import com.percussion.utils.jsr170.PSMultiProperty;
import com.percussion.utils.jsr170.PSProperty;
import com.percussion.utils.jsr170.PSValueFactory;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.string.PSFolderStringUtils;
import com.percussion.utils.string.PSXmlPIUtils;
import com.percussion.utils.types.PSPair;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyHbmImpl;
import org.hibernate.cfg.Configuration;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PreDestroy;
import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;
import javax.persistence.Column;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.percussion.services.utils.orm.PSDataCollectionHelper.clearIdSet;
import static com.percussion.services.utils.orm.PSDataCollectionHelper.executeQuery;

/**
 * Content repository implementation that stores content nodes, properties and
 * children in the database. This implementation acts as a facade on the old
 * legacy schema
 *
 * @author dougrand
 */
@Transactional
@PSBaseBean("sys_legacyContentRepository")
public class PSContentRepository
        implements
        IPSContentRepository,
        IPSHandlerInitListener,
        IPSEditorChangeListener
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
     * Logger used for the content repository
     */
    private static final Logger ms_log = LogManager.getLogger("PSContentRepository");

    /**
     * Eponymously named
     */
    static final int FOLDER_CONTENT_TYPE = 101;
    static final String COLUMN_PREFIX_CGLIB="$cglib_prop_";

    /**
     * The implementation of the folder expander used by the server code. The
     * expander is an internal plugin that is replaced by test code when run
     * outside of the server
     */
    static class FolderExpander implements IPSFolderExpander
    {
        /**
         * (non-Javadoc)
         *
         * @throws InvalidQueryException
         *
         * @see com.percussion.services.contentmgr.impl.query.IPSFolderExpander#expandPath(java.lang.String)
         */
        public List<IPSGuid> expandPath(String path) throws InvalidQueryException
        {
            // Fix jcr:root
            path = path.replace("/jcr:root/", "//");

            PSRequest req = PSRequest.getContextForRequest();
            PSServerFolderProcessor proc = PSServerFolderProcessor.getInstance();
            List<IPSGuid> rval = new ArrayList<>();

            // Enumerate the folders and return the GUIDs of the matched
            // folders
            String rootPath = PSFolderStringUtils
                    .getFolderRootPathFromPattern(path);
            try
            {
                rval = proc.findMatchingFolders(path);
                if (rval == null)
                {
                    ms_log.warn("Root path '" + rootPath
                            + "' not found", new Exception("Root path '" + rootPath
                            + "' not found"));
                    rval = new ArrayList<>();
                }
            }
            catch (PSCmsException e)
            {
                throw new InvalidQueryException(
                        "Problem expanding folder paths for query", e);
            }
            return rval;
        }
    }

    /**
     * A place holder to collect removed and added configurations.
     *
     * It is used to determine if we can restore previously removed
     * configurations without calling {@link #createSessionFactory()} in the
     * following scenario:
     * (a) while saving a Content Editor (RX application), shutdown the
     *     application, unregister (or remove) the related configurations.
     * (b) start the application, register (or add) the same set of
     *     configurations
     */
    private class ChangedConfigs
    {
        /**
         * Collects a removed configuration.
         * @param k the key of the removed configuration, assumed not
         *    <code>null</code>.
         * @param v the removed configuration, assumed not <code>null</code>.
         */
        void collectRemovedConfig(IPSTypeKey k, PSTypeConfiguration v)
        {
            m_removedConfigs.put(k, v);
        }

        /**
         * Collects a added configuration.
         * @param k the key of the added configuration, assumed not
         *    <code>null</code>.
         * @param v the added configuration, assumed not <code>null</code>.
         */
        void collectAddedConfig(IPSTypeKey k, PSTypeConfiguration v)
        {
            m_addedConfigs.put(k, v);
        }

        /**
         * Determines if we can restore previously removed configurations, where
         * the added configurations are the same as the removed configurations.
         *
         * @return <code>true</code> if the removed and added sets of
         * configurations are the same.
         */
        boolean areCollectedConfigsRestorable()
        {
            if (m_removedConfigs.isEmpty() || m_addedConfigs.isEmpty()
                    || (m_removedConfigs.size() != m_addedConfigs.size()))
                return false;

            // has to link up parent/child configs; otherwise we may be unexpected
            // result from PSTypeConfiguration.equals() below
            linkChildToParent(m_addedConfigs);
            for (Map.Entry<IPSTypeKey, PSTypeConfiguration> e : m_addedConfigs
                    .entrySet())
            {
                PSTypeConfiguration r = m_removedConfigs.get(e.getKey());
                if (r == null || ! r.equalMetaData(e.getValue()))
                    return false;
            }
            return true;
        }

        /**
         * Gets all removed configurations.
         * @return the removed configurations, may be empty,
         *    never <code>null</code>
         */
        Map<IPSTypeKey, PSTypeConfiguration> getRemovedConfigs()
        {
            return m_removedConfigs;
        }

        /**
         * Clear both removed and added configurations. This should be called
         * before collecting the removed configurations and after processing
         * added (or registered) configurations.
         */
        void clear()
        {
            m_removedConfigs.clear();
            m_addedConfigs.clear();
        }

        /**
         * The place holder for removed configurations, never <code>null</code>,
         * may be empty.
         */
        Map<IPSTypeKey, PSTypeConfiguration> m_removedConfigs = new HashMap<>();

        /**
         * The place holder for added configurations, never <code>null</code>,
         * may be empty.
         */
        Map<IPSTypeKey, PSTypeConfiguration> m_addedConfigs = new HashMap<>();
    }

    /**
     * Gets the changed configurations object from thread local memory. The
     * object will be created and put into the thread local memory if not exist.
     * @return the above object, never <code>null</code>.
     */
    private ChangedConfigs getChangedConfigs()
    {
        ChangedConfigs configs = ms_changedConfigs.get();
        if (configs == null)
        {
            configs = new ChangedConfigs();
            ms_changedConfigs.set(configs);
        }
        return configs;
    }

    /**
     * Initialized {@link #ms_csFieldToProperties}. It should only be called
     * once during system start up where {@link #ms_csFieldToProperties} is
     * empty.
     */
    private void initFieldToProperty()
    {
        if (!ms_csFieldToProperties.isEmpty())
            throw new IllegalStateException(
                    "Cannot be called if ms_csFieldToProperties is not empty.");

        PSContentEditorSystemDef systemDef = PSServer
                .getContentEditorSystemDef();
        PSFieldSet fieldset = systemDef.getFieldSet();
        Map<String, String> columnToProp = new HashMap<>();
        Field[] csfields = PSComponentSummary.class.getDeclaredFields();
        for (Field field : csfields)
        {
            Column ann = field.getAnnotation(Column.class);
            if (ann != null)
            {
                String column = ann.name();
                String varname = field.getName();
                // Strip m_
                varname = varname.substring(2);
                columnToProp.put(column.toUpperCase(), varname);
            }
        }
        for (PSField field : fieldset.getAllFields())
        {
            String fieldName = field.getSubmitName();
            String[] columnNames = field.getLocator().getColumnsForSelect();
            if (columnNames == null || columnNames.length == 0)
                continue;
            String[] parts = columnNames[0].split("\\x2E");
            if (parts.length < 2)
            {
                ms_log.warn("Field has incomplete column info: " + fieldName);
                continue;
            }
            String column = parts[1];
            String prop = columnToProp.get(column.toUpperCase());
            if (prop != null)
            {
                ms_csFieldToProperties.put(fieldName, prop);
            }
        }
        // Add added fields that are not in the systemdef
        for (int i = 0; i < ms_fieldToAdd.length; i += 2)
        {
            String property = PSContentUtils.internalizeName(ms_fieldToAdd[i]);
            String field = ms_fieldToAdd[i + 1];
            ms_csFieldToProperties.put(property, field);
        }
    }

    /**
     * The thread local memory to hold an instance of the changed configurations.
     */
    private ThreadLocal<ChangedConfigs> ms_changedConfigs = new ThreadLocal<>();

    /**
     * This reader/writer lock allows safe update of the content repository while
     * allowing general access for readers. The read lock is taken for normal
     * operations. The write lock is taken when the item def manager updates the
     * content repository information.
     */
    private ReentrantReadWriteLock m_rwlock = new ReentrantReadWriteLock(true);

    /**
     * This listener instance is kept to allow it to be removed on destruction.
     * Never <code>null</code> after ctor.
     */
    private final PSContentTypeChangeListener m_contentTypeChangeListener;

    /**
     * These are the capabilities implemented by the legacy content repository
     */
    private final static Capability[] ms_capabilities = new Capability[]
            {Capability.READ};

    /**
     * Stores configurations by key. The configuration process also stores
     * references to the child configurations in the parent to enable child
     * loading.
     */
    private static Map<IPSTypeKey, PSTypeConfiguration> ms_configuration = new HashMap<>();

    /**
     * Stores correspondances between content summary "properties" and the system
     * field names. This is derived at startup from the system def normally, and
     * a subset is created for unit testing. Never <code>null</code> and never
     * empty after construction.
     */
    private static Map<String, String> ms_csFieldToProperties = new HashMap<>();

    /**
     * @throws Exception
     *
     */
    @SuppressWarnings("unchecked")
    public PSContentRepository() throws Exception {
        super();
        m_contentTypeChangeListener = new PSContentTypeChangeListener(this);
        // Register a listener with the item def manager
        PSItemDefManager.getInstance().addListener(m_contentTypeChangeListener);
        // Register a listener with PSServer
        PSServer.addInitListener(this);
    }

    /**
     * Finds a given parent configuration and adds the child. Only called from
     * {@link PSTypeConfiguration}.
     *
     * @param parentid the parentid, assumed to be valid
     * @param childconfig the child's configuration, assumed never
     *           <code>null</code>
     */
    static void addChildConfiguration(int parentid,
                                      PSTypeConfiguration childconfig)
    {
        synchronized (ms_configuration)
        {
            PSContentTypeKey key = new PSContentTypeKey(parentid);
            PSTypeConfiguration parent = ms_configuration.get(key);
            if (parent == null)
            {
                throw new RuntimeException(
                        "Couldn't find parent config for type key " + key);
            }
            parent.addChildConfiguration(childconfig);
        }
    }

    /**
     * Get a given content type configuration. Child configurations can be
     * obtained from the parent
     *
     * @param contenttypeid the content type id
     * @return the type configuration, this will return <code>null</code> if
     *         the type id is not known
     */
    public static PSTypeConfiguration getTypeConfiguration(int contenttypeid)
    {
        synchronized (ms_configuration)
        {
            PSContentTypeKey key = new PSContentTypeKey(contenttypeid);
            return ms_configuration.get(key);
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.contentmgr.impl.IPSContentRepository#save(java.util.List,
     *      boolean)
     */
    @SuppressWarnings("unused")
    public void save(List<Node> nodes, boolean deep)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.contentmgr.impl.IPSContentRepository#delete(java.util.List)
     */
    public void delete(@SuppressWarnings("unused")
                               List<Node> nodes)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.contentmgr.impl.IPSContentRepository#loadByPath(java.util.List,
     *      com.percussion.services.contentmgr.PSContentMgrConfig)
     */
    public List<Node> loadByPath(List<String> paths, PSContentMgrConfig config)
            throws RepositoryException
    {
        try
        {
            //m_rwlock.readLock().lock();
            PSRequest req = PSRequest.getContextForRequest();
            IPSFolderProcessor proc = PSServerFolderProcessor.getInstance();
            // Translate each path to a component summary. From this the contentid
            // and revision can be extracted
            List<IPSGuid> guids = new ArrayList<>();
            for (String path : paths)
            {
                try
                {
                    int revision = 0;
                    String parts[] = path.split("#");
                    if (parts.length > 2)
                    {
                        throw new RepositoryException(
                                "# may only be specified on the leaf node (content item)");
                    }

                    PSComponentSummary summary = null;
                    if (parts.length > 1)
                    {
                        String rev = parts[1];
                        path = parts[0];
                        revision = Integer.parseInt(rev);
                        summary = proc.getSummary(path);
                        if (summary == null)
                        {
                            throw new ItemNotFoundException(
                                    "Can't find item on path: " + path);
                        }
                    }
                    else
                    {
                        summary = proc.getSummary(path);
                        if (summary == null)
                        {
                            throw new ItemNotFoundException(
                                    "Can't find item on path: " + path);
                        }
                        PSLocator cur = summary.getCurrentLocator();
                        revision = cur.getRevision();
                    }
                    guids.add(new PSLegacyGuid(summary.getContentId(), revision));
                }
                catch (PSCmsException e)
                {
                    throw new RepositoryException(e);
                }
            }
            return loadByGUID(guids, config);
        }
        finally
        {
            //m_rwlock.readLock().unlock();
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.contentmgr.impl.IPSContentRepository#getCapabilities()
     */
    public Capability[] getCapabilities()
    {
        return ms_capabilities;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.contentmgr.impl.IPSContentRepository#loadByGUID(java.util.List,
     *      com.percussion.services.contentmgr.PSContentMgrConfig)
     */
    @SuppressWarnings("unchecked")
    public List<Node> loadByGUID(List<IPSGuid> guids, PSContentMgrConfig cconfig)
            throws RepositoryException
    {
        Set<PSContentMgrOption> options = cconfig != null
                ? cconfig.getOptions()
                : new HashSet<>();
        Session session = sessionFactory.getCurrentSession();
        List<Node> rval = new ArrayList<>();
        try
        {
            m_rwlock.readLock().lock();
            // Get the component summaries for items as appropriate
            IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
            PSStopwatch sw = new PSStopwatch();
            List<Integer> ids = new ArrayList<>();
            for (IPSGuid g : guids)
            {
                if (!(g instanceof PSLegacyGuid))
                {
                    ms_log.warn("Bad guid found: " + g);
                    continue;
                }
                PSLegacyGuid guid = (PSLegacyGuid) g;
                ids.add(guid.getContentId());
            }
            List<PSComponentSummary> summaries = cms.loadComponentSummaries(ids);
            if (summaries.isEmpty())
            {
                return Collections.emptyList(); // none of the items exist
            }

            Map<Integer, PSComponentSummary> summarymap = new HashMap<>();
            for (PSComponentSummary s : summaries)
            {
                if (s != null)
                {
                    summarymap.put(s.getContentId(), s);
                }
            }
            ms_log.debug("Loaded all summaries  " + sw);
            Map<PSLegacyGuid, GeneratedClassBase> loadedInstances = loadInstances(
                    session, guids, summarymap);
            ms_log.debug("Loaded all instances  " + sw);
            for (IPSGuid guid : guids)
            {
                if (!(guid instanceof PSLegacyGuid))
                {
                    throw new RepositoryException(
                            "Passed guids must be of type PSLegacyGuid");
                }
                PSContentNode node = null;
                PSLegacyGuid legacyguid = (PSLegacyGuid) guid;
                if (!legacyguid.isChildGuid())
                {
                    if (summaries.size() == 0)
                    {
                        throw new ItemNotFoundException("Item not found: " + guid);
                    }
                    PSComponentSummary summary = summarymap.get(legacyguid
                            .getContentId());
                    // Can't load if the summary is missing
                    if (summary == null)
                        continue;
                    GeneratedClassBase instance = loadedInstances.get(legacyguid);
                    // Create type key
                    IPSTypeKey key = new PSContentTypeKey(summary.getContentTypeId());
                    PSTypeConfiguration config = ms_configuration.get(key);
                    if (config == null)
                    {
                        throw new RepositoryException(
                                "No content type info found for content type id: "
                                        + summary.getContentTypeId());
                    }
                    node = new PSContentNode(guid, summary.getName(), null, config,
                            summary, instance);
                    // Add properties from content status
                    sw = new PSStopwatch();
                    addComponentSummaryFields(node, summary);
                    ms_log.debug(" cs fields  " + sw);
                    // Add revision as a property
                    node.addProperty(new PSProperty("rx:revision", node, legacyguid
                            .getRevision()));
                    if (instance == null)
                    {
                        ms_log.warn("Could not find item in database: " + legacyguid);
                    }
                    else
                    {
                        addPropertiesFromConfig(node, config, instance, cconfig);
                    }
                    ms_log.debug(" obj fields " + sw);
                    node.setContentManagerConfiguration(cconfig);
                    if (!options.contains(PSContentMgrOption.LAZY_LOAD_CHILDREN))
                    {
                        findAndLoadChildren(node, config, cconfig);
                    }

                    PSContentPropertyLoader loader = null;
                    loader = new PSContentPropertyLoader(node);
                    // Add "lazy" properties
                    addLazyProperties(node, config, cconfig, loader);

                    if (!options.contains(PSContentMgrOption.LOAD_MINIMAL))
                    {
                        loader.getLazy(); // Force the load
                    }

                    rval.add(node);
                }
                else if (guid.getType() == PSTypeEnum.LEGACY_CHILD.getOrdinal())
                {
                    sw = new PSStopwatch();
                    long contenttypeid = legacyguid.getContentTypeId();
                    int childid = legacyguid.getChildId();
                    IPSTypeKey key = new PSContentChildTypeKey(contenttypeid,
                            childid);
                    PSTypeConfiguration config = ms_configuration.get(key);
                    GeneratedClassBase instance = loadedInstances.get(legacyguid);
                    node = new PSContentNode(legacyguid, config.getChildField(),
                            null, config, null, instance);
                    addPropertiesFromConfig(node, config, instance, null);
                    rval.add(node);
                    ms_log.debug("Bound child " + legacyguid);
                    ms_log.debug(" obj fields " + sw);
                }

            }
        }
        finally
        {
            m_rwlock.readLock().unlock();

        }

        return rval;
    }

    /**
     * Add the properties belonging to the lazy class
     *
     * @param node the node being modified, assumed not <code>null</code>
     * @param config the configuration, assumed not <code>null</code>
     * @param cconfig the content manager config, may be <code>null</code>
     * @param loader the loader to use for the properties, assumed not
     *           <code>null</code>
     */
    private void addLazyProperties(PSContentNode node,
                                   PSTypeConfiguration config, PSContentMgrConfig cconfig,
                                   PSContentPropertyLoader loader)
    {
        Class lazyClass = config.getLazyLoadClass();
        if (lazyClass == null)
            return;

        Set<String> bodyProperties = config.getBodyProperties();

        node.setLazyLoader(loader);
        List<String> props = config.getProperties().get(lazyClass);

        for (String property : props)
        {
            PSField field = config.getField(property);
            try
            {
                PSProperty p = new PSProperty(PSContentUtils
                        .externalizeName(property), node, loader);
                if (cconfig != null)
                {
                    addConfigInterceptors(cconfig, bodyProperties, field, p);
                }
                node.addProperty(p);
            }
            catch (RepositoryException e)
            {
                ms_log.error("Problem while creating lazy property " + property, e);
            }
        }
    }

    /**
     * Add the interceptors required for this property given the configuration.
     * The interceptors are added in a specific order, care must be taken when
     * adding new ones. The specific order considers:
     * <ul>
     * <li>The amount of work each interceptor is to do, try to put more costly
     * interceptors later in the process
     * <li>The required format of input to the interceptor, interceptors that
     * require xml must be put before those that might strip out xml
     * </ul>
     *
     * @param cconfig the configuration, assumed never <code>null</code>
     * @param bodyProperties the body properties, assumed never <code>null</code>
     * @param field the field being processed, assumed never <code>null</code>
     * @param property the property being created, assumed never
     *           <code>null</code>
     * @throws RepositoryException this should never be thrown, it would indicate
     *            a problem with the property object
     */
    private void addConfigInterceptors(PSContentMgrConfig cconfig,
                                       Set<String> bodyProperties, PSField field, PSProperty property)
            throws RepositoryException
    {
        boolean bodyaccess = false;

        if (bodyProperties.contains(property.getName())
                && cconfig.getBodyAccess() != null)
        {
            bodyaccess = true;
            property.addInterceptor(cconfig.getBodyAccess());
        }
        if (field.isCleanupNamespaces() && cconfig.getNamespaceCleanup() != null)
        {
            property.addInterceptor(cconfig.getNamespaceCleanup());
        }
        if (bodyaccess)
        {
            // The div tag cleanup interceptor can make the content
            // invalid
            // xml, so this must not be before any interceptor that relies
            // on valid xml content
            if (cconfig.getDivTagCleanup() != null)
                property.addInterceptor(cconfig.getDivTagCleanup());

            // Finally, remove any <?psx-activetag ?> wrappers that were
            // put in place by earlier steps to allow ASP/JSP tags to be
            // stored
            if (field.isAllowActiveTags())
            {
                property.addInterceptor(new IPSPropertyInterceptor()
                {
                    public Object translate(Object originalValue)
                    {
                        return PSXmlPIUtils.removePI((String) originalValue);
                    }
                });
            }
        }
    }

    /**
     * Load necessary instances for the list of guids in as efficient a manner as
     * possible. This then returns a map whose keys are the guids and the data is
     * the internal instances.
     *
     * @param session the hibernate session, assumed not <code>null</code>
     * @param guids the list of ids, assumed not <code>null</code>
     * @param summarymap the summary map of component summaries, assumed not
     *           <code>null</code>
     * @return the map of instances
     * @throws RepositoryException
     */
    @SuppressWarnings("unchecked")
    private Map<PSLegacyGuid, GeneratedClassBase> loadInstances(Session session,
                                                                List<IPSGuid> guids, Map<Integer, PSComponentSummary> summarymap)
            throws RepositoryException
    {
        Map<PSLegacyGuid, GeneratedClassBase> rval = new HashMap<>();
        Map<Long, Class> typeToClassMap = new HashMap<>();
        MultiMap typeToIdsMap = new MultiHashMap();
        for (IPSGuid g : guids)
        {
            if (!(g instanceof PSLegacyGuid))
            {
                ms_log.error("Bad guid found of class " + g.getClass());
                continue;
            }
            PSLegacyGuid guid = (PSLegacyGuid) g;
            PSComponentSummary s = summarymap.get(guid.getContentId());
            if (s == null)
            {
                ms_log.error("No component summary found for guid " + guid);
                continue;
            }
            long type = s.getContentTypeId();
            if (typeToClassMap.get(type) == null)
            {
                IPSTypeKey key = new PSContentTypeKey(type);
                PSTypeConfiguration config = ms_configuration.get(key);
                if (config == null)
                {
                    throw new RepositoryException(
                            "No content type info found for content type id: " + type);
                }
                typeToClassMap.put(type, config.getMainClass());
            }
            typeToIdsMap.put(type, new PSLegacyCompositeId(guid));
        }
        // Now we have the ids grouped per type, we can load them in groups
        // using hibernate
        for (long type : typeToClassMap.keySet())
        {
            Class iclass = typeToClassMap.get(type);
            List<PSLegacyCompositeId> ids = (List<PSLegacyCompositeId>) typeToIdsMap
                    .get(type);
            List<GeneratedClassBase> results = new ArrayList<>();
            for (PSLegacyCompositeId id : ids)
            {
                GeneratedClassBase data = (GeneratedClassBase) session.get(iclass,
                        id);
                if (data != null)
                    results.add(data);
            }

            if (results.size() != ids.size())
            {
                ms_log.warn("Missing instance objects");
            }
            for (GeneratedClassBase inst : results)
            {
                PSPropertyWrapper w = new PSPropertyWrapper(inst);
                PSLegacyCompositeId id = (PSLegacyCompositeId) w
                        .getPropertyValue(PSContentNode.ID_PROPERTY_NAME);
                PSLegacyGuid guid = new PSLegacyGuid(id.getSys_contentid(), id
                        .getSys_revision());
                rval.put(guid, inst);
            }
        }

        return rval;
    }

    /**
     * Iterate over the defined properties and add property objects for each
     *
     * @param node the parent node, assumed not <code>null</code>
     * @param config the configuration for the node (parent or child), assumed
     *           not <code>null</code>
     * @param rep the representation object from the database, assumed not
     *           <code>null</code>
     * @param cconfig content manager configuration, may be <code>null</code>
     * @throws RepositoryException
     */
    private void addPropertiesFromConfig(PSContentNode node,
                                         PSTypeConfiguration config, Object rep, PSContentMgrConfig cconfig)
            throws RepositoryException
    {
        List<String> props = config.getProperties().get(rep.getClass());
        Set<String> simpleChildren = config.getSimpleChildProperties();
        Set<String> bodyProperties = config.getBodyProperties();

        for (String property : props)
        {
            PSField field = config.getField(property);

            if (simpleChildren.contains(property))
            {
                node.addProperty(new PSMultiProperty(PSContentUtils
                        .externalizeName(property), node, rep));
            }
            else
            {
                PSProperty p = new PSProperty(PSContentUtils
                        .externalizeName(property), node, rep, null);
                boolean bodyproperty = bodyProperties.contains(p.getName());
                if (bodyproperty && cconfig != null
                        && cconfig.getBodyAccess() != null)
                {
                    p.addInterceptor(cconfig.getBodyAccess());
                }
                if (field.isCleanupNamespaces() && cconfig != null
                        && cconfig.getNamespaceCleanup() != null)
                {
                    p.addInterceptor(cconfig.getNamespaceCleanup());
                }
                // This must be the last interceptor
                if (bodyproperty && cconfig != null
                        && cconfig.getDivTagCleanup() != null)
                {
                    p.addInterceptor(cconfig.getDivTagCleanup());
                }
                node.addProperty(p);
            }
        }

        if (config.isSortedChild())
        {
            node.addProperty(new PSProperty("rx:sys_sortrank", node, rep, null));
        }
    }

    /**
     * Get the children. We do this by using the child configurations to directly
     * load each child by relating back the content id + revision id of the
     * parent item.
     *
     * @param parent The parent node, assumed never <code>null</code>
     * @param config The parent node's configuration, assumed never
     *           <code>null</code>
     * @param cmgrConfig The content manager configuration, may be
     *           <code>null</code>
     * @throws RepositoryException
     */
    private void findAndLoadChildren(PSContentNode parent,
                                     PSTypeConfiguration config, PSContentMgrConfig cmgrConfig)
            throws RepositoryException
    {
        PSLegacyGuid guid = (PSLegacyGuid) parent.getGuid();
        int content_id = guid.getContentId();
        int revision = guid.getRevision();
        for (PSTypeConfiguration child : config.getChildren())
        {
            StringBuffer query = new StringBuffer();
            query.append("from ");
            query.append(child.getImplementingClasses().get(0)
                    .getImplementingClass().getName());
            query.append(" where sys_contentid = :cid and sys_revision = :rev");
            if (child.isSortedChild())
            {
                query.append(" order by sys_sortrank asc");
            }
            String params[] =
                    {"cid", "rev"};
            Object values[] =
                    {content_id, revision};
            List children = sessionFactory.getCurrentSession().createQuery(
                    query.toString()).setParameter("cid",content_id).setParameter("rev",revision).list();
            for (Object rep : children)
            {
                PSLegacyGuid legacyguid = new PSLegacyGuid(
                        child.getContenttypeid(), child.getChildid(), getSysId(rep));
                PSContentNode node = parent.addNode(child.getChildField(),
                        legacyguid);
                node.setContentManagerConfiguration(cmgrConfig);
                node.setConfiguration(child);
                addPropertiesFromConfig(node, child, rep, cmgrConfig);
                PSContentPropertyLoader loader = null;
                loader = new PSContentPropertyLoader(node);
                // Add "lazy" properties
                addLazyProperties(node, child, cmgrConfig, loader);
            }
        }
        parent.setChildrenLoaded(true);
    }

    /**
     * Get the sysid, grabs the sys_id using reflection
     *
     * @param rep the internal object used by hibernate
     * @return the id
     */
    private int getSysId(Object rep)
    {
        try
        {
            Method m = rep.getClass().getMethod("getSys_sysid", new Class[]
                    {});
            return (Integer) m.invoke(rep, new Object[]
                    {});
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * These are the fields that need to be added if not present to the
     * content item properties. This list is package protected so it may be
     * used by the type configuration code, which needs to get all the
     * properties for a type.
     */
    final static String ms_fieldToAdd[] = {
            IPSContentPropertyConstants.RX_SYS_CONTENTCREATEDBY,
            "contentCreatedBy",
            IPSContentPropertyConstants.RX_SYS_CONTENTCREATEDDATE,
            "contentCreatedDate",
            IPSContentPropertyConstants.RX_SYS_CONTENTLASTMODIFIEDATE,
            "contentLastModifiedDate",
            IPSContentPropertyConstants.RX_SYS_CONTENTLASTMODIFIER,
            "contentLastModifier",
            IPSContentPropertyConstants.RX_SYS_CONTENTTYPEID,
            "contentTypeId",
            IPSContentPropertyConstants.RX_SYS_CONTENTSTATEID,
            "contentStateId"
    };

    /* (non-Javadoc)
     * @see com.percussion.services.contentmgr.impl.IPSContentRepository#getUnmappedSystemFields()
     */
    public Set<String> getUnmappedSystemFields()
    {
        Set<String> rval = new HashSet<>();
        for(int i = 0; i < ms_fieldToAdd.length; i += 2)
        {
            rval.add(ms_fieldToAdd[i]);
        }
        return rval;
    }

    /**
     * Add fields from the component summary to the node
     *
     * @param node the node, assumed never <code>null</code>
     * @param summary the summary, assumed never <code>null</code>
     * @throws RepositoryException if something goes wrong
     */
    private void addComponentSummaryFields(PSContentNode node,
                                           PSComponentSummary summary) throws RepositoryException
    {
        for (Map.Entry<String, String> fieldToProp : ms_csFieldToProperties
                .entrySet())
        {
            node.addProperty(new PSProperty("rx:" + fieldToProp.getKey(), node,
                    summary, fieldToProp.getValue()));
        }

        // Add Component summary fields that are not part of the system
        // def display mapping. Checks before adding to make sure that
        // this implementation hasn't mapped the field
        for (int i = 0; i < ms_fieldToAdd.length; i += 2)
        {
            String property_name = ms_fieldToAdd[i];
            String summary_prop = ms_fieldToAdd[i + 1];
            String field_name =
                    property_name.substring(property_name.indexOf(':') + 1);
            if (!ms_csFieldToProperties.containsKey(field_name))
            {
                node.addProperty(new PSProperty(property_name, node, summary,
                        summary_prop));
            }
        }
        // Add content id if not present
        if (!ms_csFieldToProperties.containsKey("sys_contentid"))
        {
            node.addProperty(new PSProperty(
                    IPSContentPropertyConstants.RX_SYS_CONTENTID, node, summary
                    .getContentId()));
        }
        // Add revision if not present
        if (!ms_csFieldToProperties.containsKey("sys_revision"))
        {
            node.addProperty(new PSProperty(
                    IPSContentPropertyConstants.RX_SYS_REVISION, node, summary
                    .getPublicOrCurrentRevision()));
        }

        // Add specific fields for JSR-170
        if (summary.getCheckoutUserName() != null)
        {
            node.addProperty(new PSProperty(
                    IPSContentPropertyConstants.JCR_IS_CHECKEDOUT, node, true));
        }
        else
        {
            node.addProperty(new PSProperty(
                    IPSContentPropertyConstants.JCR_IS_CHECKEDOUT, node, false));
        }
    }

    /**
     * Sets the datasource manager to use override the configuration.
     *
     * @param dsMgr The datasource manager, may not be <code>null</code>.
     */
    public void setDatasourceManager(IPSDatasourceManager dsMgr)
    {
        if (dsMgr == null)
            throw new IllegalArgumentException("dsMgr may not be null");

        m_dsMgr = dsMgr;
    }

    /**
     * The datasource manager to use to override the new configuration,
     * initalized by the first call to
     * {@link #setDatasourceManager(IPSDatasourceManager)}, never
     * <code>null</code> after that.
     */
    private IPSDatasourceManager m_dsMgr;

    /**
     * Determines if the repository is a derby database.
     *
     * @return <code>true</code> if it is a derby database; otherwise return
     *   <code>false</code>.
     */
    private boolean isDerbyDatabase()
    {
        try
        {
            PSConnectionDetail connDetail = m_dsMgr.getConnectionDetail(null);
            return connDetail.getDriver().equalsIgnoreCase("derby");
        }
        catch (Exception e)
        {
            ms_log.error("Failed to determine database type", e);
        }
        return false;
    }

    /**
     * Update the content repository with the current configuration. The writer
     * lock guarantees that no one is currently using the content repository
     * during the update process.
     *
     * This method runs through the list, updates the list we hold with the
     * repository. Then that list is handled by a separate method that creates a
     * new session factory with the appropriate classes and mappings for
     * hibernate.
     *
     * @param waitingChanges The list of modifications to handle, never
     *           <code>null</code>
     *
     * @throws Exception if there is a problem processing the configuration
     */
    @SuppressWarnings("unchecked")
    public void configure(List<PSContentTypeChange> waitingChanges)
            throws Exception
    {
        if (waitingChanges == null)
        {
            throw new IllegalArgumentException("waitingChanges may not be null");
        }
        try
        {
            m_rwlock.writeLock().lock();

            if (ms_csFieldToProperties.size() == 0)
                initFieldToProperty();

            if (removeUnregisteredConfigs(waitingChanges))
                return; // no further action if unregister/remove only

            addRegisteredConfigs(waitingChanges, isDerbyDatabase());

            if (restoreUnregisteredConfigs())
            {
                // no further action after restored previously unregistered/removed
                // configurations
                return;
            }

            createSessionFactory();
        }
        catch (Exception e)
        {
            ms_log.fatal("Could not configure the content repository "
                    + "- correct the configuration", e);
        }
        finally
        {
            m_rwlock.writeLock().unlock();
        }
    }

    /**
     * Creates a hibernate session factory from the {@link #ms_configuration}.
     * Assumed the caller own the write lock of {@link #m_rwlock}.
     * <P>
     * Note, this call will increase heap size (by hibernate), have seen the
     * heap size increased to 30MB in a customer's environment, 2M in FF.
     *
     * @throws Exception if an error occurs during this process.
     */
    private void createSessionFactory() throws Exception
    {
        // Recreate parent - child relationships between the configuration
        // objects
        for (Map.Entry<IPSTypeKey, PSTypeConfiguration> e : ms_configuration
                .entrySet())
        {
            e.getValue().clearChildren();
        }
        linkChildToParent(ms_configuration);

        // Now process the configurations, creating a new SessionFactory
        // in the process
        Configuration hibConfig = new Configuration();
        for (Map.Entry<IPSTypeKey, PSTypeConfiguration> e : ms_configuration
                .entrySet())
        {
            List<PSTypeConfiguration.ImplementingClass> list = e.getValue()
                    .getImplementingClasses();
            for (PSTypeConfiguration.ImplementingClass iclass : list)
            {
                // Don't register PSComponentSummary
                if (iclass.getConfiguration() != null)
                {
                    String config = iclass.getConfiguration();
                    try(InputStream s = new ByteArrayInputStream(config
                            .getBytes(StandardCharsets.UTF_8))) {
                        hibConfig.addInputStream(s);
                    }
                }
            }
        }
        hibConfig.addAnnotatedClass(PSComponentSummary.class);
        hibConfig.addAnnotatedClass(PSRelationshipData.class);
        hibConfig.addAnnotatedClass(PSTempId.class);
        // Copy properties from main session factory configuration
        IPSDatasourceManager dsMgr = PSDatasourceMgrLocator.getDatasourceMgr();
        Properties props = new Properties();
        props.putAll(dsMgr.getHibernateProperties(null));
        hibConfig.setProperties(props);
        hibConfig.setPhysicalNamingStrategy(new UpperCaseNamingStrategy());
        hibConfig.setImplicitNamingStrategy(new ImplicitNamingStrategyLegacyHbmImpl());
        // Note, getSessionFactory().close() does not release resources or
        // the heap memories by the existing session factory

        setSessionFactory(hibConfig.buildSessionFactory());
    }

    /**
     * Restores the previously unregistered configurations if they are the same
     * as the currently registered ones.
     * Assumed the caller own the write lock of {@link #m_rwlock}
     *
     * @return <code>true</code> if restored previously unregistered
     *    configurations.
     */
    private boolean restoreUnregisteredConfigs()
    {
        boolean canRecoverRemoved = getChangedConfigs()
                .areCollectedConfigsRestorable();
        if (canRecoverRemoved)
        {
            ms_configuration.putAll(getChangedConfigs().getRemovedConfigs());
        }

        // clear the history after processed registered configs.
        getChangedConfigs().clear();

        return canRecoverRemoved;
    }

    /**
     * Adds registered configurations from the supplied changes.
     * Assumed the caller own the write lock of {@link #m_rwlock}
     *
     * @param waitingChanges the list of changed objects, assumed not
     * <code>null</code>.
     * @param isDerbyDatabase <code>true</code> if the repository is a derby
     *   database.
     *
     * @throws RepositoryException if an error occurs.
     */
    private void addRegisteredConfigs(List<PSContentTypeChange> waitingChanges,
                                      boolean isDerbyDatabase) throws RepositoryException
    {
        final boolean isDerby = isDerbyDatabase;
        // Now walk the list and recreate all that are being added (i.e.
        // not deleted)
        Object args[] = new Object[]
                {};
        for (PSContentTypeChange change : waitingChanges)
        {
            if (change.isRegister())
            {
                mapElements(change.getDefinition(),
                        new IPSItemDefElementProcessor()
                        {
                            @SuppressWarnings("unused")
                            public Object processParentElement(
                                    PSItemDefinition definition, Object[] a)
                                    throws Exception
                            {

                                IPSTypeKey key = new PSContentTypeKey(definition
                                        .getContentEditor().getContentType());
                                PSTypeConfiguration config = new PSTypeConfiguration(
                                        definition, null, isDerby);
                                return new Object[]
                                        {key, config};
                            }

                            @SuppressWarnings("unused")
                            public Object processChildElement(
                                    PSItemDefinition definition, PSItemChild child,
                                    Object[] a) throws Exception
                            {
                                IPSTypeKey key = new PSContentChildTypeKey(
                                        definition.getContentEditor().getContentType(),
                                        child.getChildId());
                                PSTypeConfiguration config = new PSTypeConfiguration(
                                        definition, child, isDerby);
                                return new Object[]
                                        {key, config};
                            }
                        }, args);
            }
        }
    }

    /**
     * Removes un-registered configurations from the supplied changes.
     * Assumed the caller own the write lock of {@link #m_rwlock}
     *
     * @param waitingChanges the list of changed objects, assumed not
     * <code>null</code>.
     *
     * @return <code>true</code> if the changed objects are all unregistered
     * and their related configurations are removed from
     * {@link #ms_configuration}.
     */
    private boolean removeUnregisteredConfigs(
            List<PSContentTypeChange> waitingChanges)
    {
        // it is "true" if all elements of the "waitingChanges" are to be
        // unregistered
        boolean isUnregisterOnly = true;

        Set<Long> types = new HashSet<>();
        // Get the list of types being add/removed. We'll remove all the
        // configurations, then rerun the list and re-add those that were
        // not deletes.
        for (PSContentTypeChange change : waitingChanges)
        {
            types.add(change.getDefinition().getContentEditor()
                    .getContentType());
            if (change.isRegister())
                isUnregisterOnly = false;
        }

        // Walk the map of configurations and remove any that match a
        // content type
        Set<IPSTypeKey> toRemove = new HashSet<>();
        for (Map.Entry<IPSTypeKey, PSTypeConfiguration> entry : ms_configuration
                .entrySet())
        {
            if (types.contains(entry.getKey().getContentType()))
            {
                toRemove.add(entry.getKey());
            }
        }

        // clear place holder before collecting the removed configurations.
        if (isUnregisterOnly && (!toRemove.isEmpty()))
            getChangedConfigs().clear();

        for (IPSTypeKey key : toRemove)
        {
            PSTypeConfiguration removed = ms_configuration.get(key);
            if (key != null)
            {
                ms_configuration.remove(key);
                getChangedConfigs().collectRemovedConfig(key, removed);
            }
        }

        return isUnregisterOnly && (!toRemove.isEmpty());
    }

    /**
     * Process a content type using the given processor
     *
     * @param def The item def, must never be <code>null</code>
     * @param proc The processor, must never be <code>null</code>
     * @param args The args, may be <code>null</code>
     * @throws RepositoryException
     */
    @SuppressWarnings("unchecked")
    protected void mapElements(PSItemDefinition def,
                               IPSItemDefElementProcessor proc, Object args[])
            throws RepositoryException
    {
        try
        {
            PSCoreItem item = new PSCoreItem(def);
            Object results[];

            results = (Object[]) proc.processParentElement(def, args);
            ms_configuration.put((IPSTypeKey) results[0],
                    (PSTypeConfiguration) results[1]);
            getChangedConfigs().collectAddedConfig((IPSTypeKey) results[0],
                    (PSTypeConfiguration) results[1]);
            Iterator children = item.getAllChildren();
            while (children.hasNext())
            {
                PSItemChild child = (PSItemChild) children.next();
                results = (Object[]) proc.processChildElement(def, child, args);
                ms_configuration.put((IPSTypeKey) results[0],
                        (PSTypeConfiguration) results[1]);
                getChangedConfigs().collectAddedConfig((IPSTypeKey) results[0],
                        (PSTypeConfiguration) results[1]);
            }
        }
        catch (PSInvalidContentTypeException icte)
        {
            ms_log.warn("Skipping unknown content type " + def.getName());
        }
        catch (PSCmsException ce)
        {
            throw new RepositoryException(ce.getLocalizedMessage());
        }
        catch (Exception e)
        {
            ms_log.error("Exception during item def processing", e);
            throw new RepositoryException(e);
        }
    }

    /**
     * Links all child configuration object to its parent for the given map.
     * @param map the map in question, assumed not <code>null</code>, but may
     *    be empty.
     */
    private void linkChildToParent(Map<IPSTypeKey, PSTypeConfiguration> map)
    {
        for (Map.Entry<IPSTypeKey, PSTypeConfiguration> e : map.entrySet())
        {
            PSTypeConfiguration config = e.getValue();
            if (config.getChildid() != 0)
            {
                IPSTypeKey parentKey = new PSContentTypeKey(config
                        .getContenttypeid());
                PSTypeConfiguration parent = map.get(parentKey);
                if (parent == null)
                {
                    ms_log.warn("Found child configuration without a parent: "
                            + config.getChildField());
                    continue;
                }
                parent.addChildConfiguration(config);
            }
        }
    }

    /**
     * Get an item def using the rxserver user id.
     *
     * @param cTypeKey Assumed not <code>null</code>.
     * @return A valid definition for the requested content editor type, never
     *         <code>null</code>.
     * @throws PSInvalidContentTypeException if there is a problem looking up the
     *            item def in {@link PSItemDefManager}.
     */
    protected PSItemDefinition getItemDef(PSKey cTypeKey)
            throws PSInvalidContentTypeException
    {
        PSItemDefinition def = PSItemDefManager.getInstance().getItemDef(
                cTypeKey.getPartAsInt(cTypeKey.getDefinition()[0]),
                PSItemDefManager.COMMUNITY_ANY);
        return def;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.server.IPSHandlerInitListener#initHandler(com.percussion.server.IPSRequestHandler)
     */
    public void initHandler(IPSRequestHandler requestHandler)
    {
        if (requestHandler == null)
            throw new IllegalArgumentException("requestHandler may not be null");

        if (requestHandler instanceof PSContentEditorHandler)
        {
            // add this instance as listener to all content editors
            PSContentEditorHandler ceh = (PSContentEditorHandler) requestHandler;
            ceh.addEditorChangeListener(this);
        }
    }

    /**
     * Method called when Spring's container is destroyed
     */
    @PreDestroy
    public void destroy()
    {
        PSItemDefManager.getInstance()
                .removeListener(m_contentTypeChangeListener);
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.server.IPSHandlerInitListener#shutdownHandler(com.percussion.server.IPSRequestHandler)
     */
    @SuppressWarnings("unused")
    public void shutdownHandler(IPSRequestHandler requestHandler)
    {
        // Ignore
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.cms.IPSEditorChangeListener#editorChanged(com.percussion.cms.PSEditorChangeEvent)
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void editorChanged(PSEditorChangeEvent e)
    {
        int content_id = e.getContentId();
        int revision = e.getRevisionId();

        // Invalidate that object from the secondary hibernate cache and
        // any content repository internal cache.
        //
        // We need to call "around" the interface to the content repository
        // because the hibernate session won't be correctly setup for the
        // direct call to this listener
        IPSContentRepository rep = PSContentInternalLocator.getLegacyRepository();
        PSLegacyGuid guid = new PSLegacyGuid(content_id, revision);
        List<IPSGuid> guids = new ArrayList<>();
        guids.add(guid);
        rep.evict(guids);
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.contentmgr.impl.IPSContentRepository#evict(java.util.List)
     */
    @SuppressWarnings("unchecked")
    public void evict(List<IPSGuid> guids)
    {
        Session session = sessionFactory.getCurrentSession();
        try
        {
            m_rwlock.readLock().lock();
            IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
            List<Integer> ids = new ArrayList<>();
            Map<Integer, PSComponentSummary> summarymap = new HashMap<>();
            SessionFactory fact = this.getSessionFactory();

            for (IPSGuid g : guids)
            {
                PSLegacyGuid legacyguid = (PSLegacyGuid) g;
                ids.add(legacyguid.getContentId());
            }
            // Evict any summaries currently cached
            cms.evictComponentSummaries(ids);

            // Next get the component summaries, needed to access the
            // content type information.
            List<PSComponentSummary> summaries = cms.loadComponentSummaries(ids);
            for (PSComponentSummary s : summaries)
            {
                summarymap.put(s.getContentId(), s);
            }

            // Now start evicting. The eviction starts with the main classes, and
            // then goes to the children if there are any child objects.
            for (IPSGuid g : guids)
            {
                PSLegacyGuid legacyguid = (PSLegacyGuid) g;
                PSNotificationHelper.notifyEvent(EventType.CONTENT_CHANGED,
                        legacyguid);
                PSComponentSummary s = summarymap.get(legacyguid.getContentId());
                Set<String> affectedclasses = new HashSet<>();

                // If the component summary is not there, we've never loaded
                // this item through the repository interface, or the second
                // level cache has already booted the data.
                if (s == null)
                    continue;
                IPSTypeKey key = new PSContentTypeKey(s.getContentTypeId());
                PSTypeConfiguration config = ms_configuration.get(key);
                List<PSTypeConfiguration.ImplementingClass> classes = config
                        .getImplementingClasses();
                for (PSTypeConfiguration.ImplementingClass ic : classes)
                {
                    if (!ic.getImplementingClass().equals(PSComponentSummary.class))
                    {
                        PSLegacyCompositeId id = new PSLegacyCompositeId(legacyguid);
                        fact.getCache().evictEntity(ic.getImplementingClass(), id);
                        affectedclasses.add(ic.getImplementingClass()
                                .getCanonicalName());
                    }
                }

                // For each child, we need to actually do a projection to grab
                // the sys_ids so they can be evicted.
                for (PSTypeConfiguration child : config.getChildren())
                {
                    for (PSTypeConfiguration.ImplementingClass ic : child
                            .getImplementingClasses())
                    {

                        affectedclasses.add(ic.getImplementingClass()
                                .getCanonicalName());

                    }
                }

                // Evict any associated collections. Doesn't try to preserve
                // other possible objects
                Map<String, CollectionMetadata> colmeta = fact
                        .getAllCollectionMetadata();
                for (String cname : colmeta.keySet())
                {
                    int l = cname.lastIndexOf('.');
                    String base = cname.substring(0, l);
                    if (affectedclasses.contains(base))
                    {
                        fact.getCache().evictCollectionRegion(cname);
                    }
                }
            }
        }
        catch (Exception e)
        {
            ms_log.error("Problem while handling cache eviction", e);
        }
        finally
        {
            m_rwlock.readLock().unlock();

        }
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.contentmgr.impl.IPSContentRepository#loadChildren(java.util.List,
     *      com.percussion.services.contentmgr.PSContentMgrConfig)
     */
    public void loadChildren(List<Node> nodes, PSContentMgrConfig config)
            throws RepositoryException
    {
        for (Node n : nodes)
        {
            PSContentNode node = (PSContentNode) n;
            findAndLoadChildren(node, node.getConfiguration(), config);
        }

    }

    /**
     * The maximum date value that is acceptable by hibernate binding API
     */
    private static java.util.Date MAX_DATE;

    /**
     * The minimum date value that is acceptable by hibernate binding API
     */
    private static java.util.Date MIN_DATE;
    static {
        Calendar c = Calendar.getInstance();
        //FB: DMI_BAD_MONTH  NC 1-17-16
        c.set(9999, 11, 31);
        MAX_DATE = c.getTime();

        c.set(1753, 1, 1);
        MIN_DATE = c.getTime();
    }

    /**
     * Validates the specified date value and error log a message if the value
     * is not between {@link #MIN_DATE} and {@link #MAX_DATE}.
     *
     * @param tag the error tag for logged error message, assumed not blank.
     * @param d the date value in question, assumed not <code>null</code>.
     */
    private void validateDate(String tag, Date d)
    {
        if (d.before(MIN_DATE) || d.after(MAX_DATE))
        {
            ms_log.error("[" + tag + "] (" + Thread.currentThread().getId() + ") INVALID date: " +  d.toString());
        }
    }

    /**
     * Log the specified parameter if the debug is enabled. It also validates
     * the value of a date if the value type is {@link java.util.Date} and
     * the debug is enabled.
     * <p>
     * Do nothing is debug log is not enabled.
     *
     * @param pname the name of the parameter, it may be <code>null</code>
     * or empty.
     * @param value the value of the parameter, assumed not <code>null</code>.
     */
    private void logParameter(String pname, Object value)
    {
        if (!ms_log.isDebugEnabled())
            return;

        if (value instanceof java.util.Date)
        {
            java.util.Date dvalue = (java.util.Date) value;
            validateDate(pname, dvalue);
        }

        ms_log.debug("[" + Thread.currentThread().getId() + "] \"" + pname + "\" = " + value.toString() + ", " + value.getClass().getName());
    }

    /**
     * Gets the IDs of the content type that involved with the query.
     *
     * @param psquery the query to perform, assumed not <code>null</code>
     *
     * @return the IDs of the content types, not <code>null</code>, may be empty.
     *
     * @throws RepositoryException if an error occurs.
     */
    private Set<Long> getTypeIds(PSQuery psquery) throws RepositoryException
    {
        List<PSQueryNodeIdentifier> types = psquery.getTypeConstraints();
        Set<Long> typeids = new HashSet<>();
        IPSContentMgr cms = PSContentMgrLocator.getContentMgr();
        for (PSQueryNodeIdentifier type : types)
        {
            String name = type.getName();
            if (name.equals("nt:base") || name.equals("*"))
            {
                List<IPSNodeDefinition> defs = cms.findAllItemNodeDefinitions();
                for (IPSNodeDefinition def : defs)
                {
                    if (def.getObjectType() == 1)
                    {
                        typeids.add(def.getGUID().longValue());
                    }
                }
                break;
            }
            else
            {
                IPSNodeDefinition def = cms
                        .findNodeDefinitionByName(type.getName());
                typeids.add(def.getGUID().longValue());
            }
        }
        return typeids;
    }

    /**
     * Gets the column names for the specified query.
     *
     * @param psquery the specified query, assumed not <code>null</code>.
     * @param typeids the IDs of content types involved with the query,
     * assumed not <code>null</code>.
     *
     * @return an array of column names, never <code>null</code> but
     * may be zero length for a result that doesn't require anything
     * but nodes for output
     *
     * @throws InvalidQueryException if content types are not defined.
     */
    private String[] getQueryColumns(PSQuery psquery, Set<Long> typeids)
            throws InvalidQueryException
    {
        String columns[] = psquery.getColumns();
        if (columns != null)
            return columns;

        Set<String> allprops = new HashSet<>();
        // projection, use all single valued properties from all
        // types
        for (Long typeid : typeids)
        {
            IPSTypeKey key = new PSContentTypeKey(typeid);
            PSTypeConfiguration type = ms_configuration.get(key);
            if (type == null)
            {
                throw new InvalidQueryException("Unknown content type id "
                        + typeid);
            }
            for(List<String> props : type.getProperties().values())
            {
                // Add all properties mapped by classes
                for (String p : props)
                {
                    allprops.add(PSContentUtils.externalizeName(p));
                }
            }
        }
        // Add contentstatus fields
        for (String p : ms_csFieldToProperties.keySet())
        {
            allprops.add(PSContentUtils.externalizeName(p));
        }
        columns = new String[allprops.size()];
        allprops.toArray(columns);

        return columns;
    }

    /**
     * Gets the where clause (that will be used for the specified
     * query and potentially the content IDs that are inserted
     * into the temporary table during this process.
     *
     * @param query the query to perform, assumed not <code>null</code>.
     * @param s the hibernate session, assumed not <code>null</code>.
     * @param params the parameters that are used for the query,
     * assumed not <code>null</code>.
     * @param collectionIds the default content IDs for the query,
     * assumed not <code>null</code>.
     *
     * @return the where clause (1st) and the content IDs (2nd, which is be
     * the same or different from the passed in one).
     *
     * @throws InvalidQueryException if failed to create the where clause.
     * @throws ValueFormatException if failed to create a value.
     */
    private PSPair<IPSQueryNode, List<Long>> getWhereClause(
            javax.jcr.query.Query query, Session s,
            Map<String, ? extends Object> params, List<Long> collectionIds)
            throws InvalidQueryException, ValueFormatException
    {
        if (((PSQuery) query).getWhere() == null)
        {
            return new PSPair<>(null, collectionIds);
        }

        PSQueryTransformer transformer = new PSQueryTransformer(
                new FolderExpander(), params, s);

        IPSQueryNode internalwhere = ((PSQuery) query).getWhere().accept(transformer);
        collectionIds = transformer.getIdCollections();
        // Handle some special cases where we're down to a boolean value. This
        // will happen if something is false, and the value has been propagated
        // up to the "root" of the where clause
        if (internalwhere instanceof PSQueryNodeValue)
        {
            Object val = ((PSQueryNodeValue) internalwhere).getValue();
            if (val instanceof Boolean && (Boolean) val)
            {
                // Reconstruct a true or false node here
                Value lval = PSValueFactory.createValue(new Long(1));
                PSQueryNodeValue left = new PSQueryNodeValue(lval);
                left.setType(PropertyType.LONG);
                PSQueryNodeValue right = new PSQueryNodeValue(lval);
                right.setType(PropertyType.LONG);
                internalwhere = new PSQueryNodeComparison(left, right, Op.EQ);
            }
            else if (val instanceof Boolean && ! (Boolean) val)
            {
                //NOOP JDBC does not support parameters on both sides for not equal (?<>?)
                //See #createQueryWhere in this class.
                //ADAM GENT
            }
            else
            {
                ms_log.error("Non boolean value returned for query reduction: "
                        + val);
                return null;
            }
        }

        return new PSPair<>(internalwhere, collectionIds);
    }

    /**
     * Creates the query where builder for the specified query.
     *
     * @param psquery the query to perform, assumed not <code>null</code>.
     * @param typeid the content ID, assumed not <code>null</code>.
     * @param internalwhere the where clause of the query, it may be
     * <code>null</code> if there is where clause.
     * @param params the extra parameters for the query, may be <code>null</code>.
     * @param type the repository configuration of the content type,
     * assumed not <code>null</code>.
     *
     * @return the where-builder (1st) and the literal where clause (2nd),
     * not <code>null</code>.
     *
     * @throws InvalidQueryException if failed to create the where-builder.
     */
    private PSPair<PSQueryWhereBuilder, String> createQueryWhere(PSQuery psquery,
                                                                 Long typeid, IPSQueryNode internalwhere,
                                                                 Map<String, ? extends Object> params, PSTypeConfiguration type)
            throws InvalidQueryException
    {
        PSQueryPropertyLimiter proplimiter = new PSQueryPropertyLimiter();
        PSQueryPropertyType proptype = new PSQueryPropertyType();
        PSQueryWhereBuilder wherebuilder = new PSQueryWhereBuilder(type,
                params);
        wherebuilder.processProjection(psquery.getProjection());
        wherebuilder.processSortfields(psquery.getSortFields());
        proplimiter.setConfig(type);
        proptype.setConfig(type);
        IPSQueryNode limited = null;
        String where = null;
        if (internalwhere != null)
        {
            limited = internalwhere.accept(proplimiter);
            limited = limited.accept(proptype);
            IPSQueryNode result = limited.accept(wherebuilder);
            where = wherebuilder.toString();
            if (result instanceof PSQueryNodeValue)
            {
                PSQueryNodeValue val = (PSQueryNodeValue) result;
                Object value = val.getValue();
                if (value instanceof Boolean)
                {
                    if (!((Boolean) value))
                    {
                        // If the result was false that means that the
                        // expression evaluated to false, and so no results
                        // should be returned
                        //
                        // This usually is caused by 'jcr:path like' for a folder
                        // path that does not exist.
                        // ADAM GENT
                        wherebuilder.getQueryParams().clear();
                        where = "1 <> 1";
                    }
                }
            }
        }
        return new PSPair<>(wherebuilder, where);
    }

    /**
     * Prepare an executable query for the specified JCR query.
     *
     * @param psquery the query to perform, assumed not <code>null</code>.
     * @param typeid the content type ID that involved with the query,
     * assumed not <code>null</code>.
     * @param s the hibernate session, assumed not <code>null</code>.
     * @param internalwhere the where clause of the query,
     * it may be <code>null</code>.
     * @param maxresults the maximum returned result set.
     * @param params the extra parameters for the query, may be <code>null</code>.
     *
     * @return the executable query, not <code>null</code>.
     *
     * @throws InvalidQueryException if failed to prepare the query.
     */
    @SuppressWarnings("unchecked")
    private Query prepareQuery(PSQuery psquery, Long typeid, Session s,
                               IPSQueryNode internalwhere, int maxresults,
                               Map<String, ? extends Object> params) throws InvalidQueryException
    {
        IPSTypeKey key = new PSContentTypeKey(typeid);
        PSTypeConfiguration type = ms_configuration.get(key);
        if (type == null)
        {
            ms_log.warn("Query problem: type not found for type id "
                    + typeid);
            return null;
        }

        PSPair<PSQueryWhereBuilder, String> pair = createQueryWhere(psquery, typeid, internalwhere, params, type);
        PSQueryWhereBuilder wherebuilder = pair.getFirst();
        String where = pair.getSecond();

        // Create projection needed by results
        String projection = psquery.getProjection(type,
                wherebuilder.getInuse());

        // Get the sort clause
        String sort = psquery.getSortClause(type, wherebuilder.getInuse());

        // Create the query string
        StringBuffer querystr = new StringBuffer();

        querystr.append("select ");
        querystr.append(projection);
        querystr.append(" from PSComponentSummary as cs ");
        querystr.append(" left outer join cs.parentFolders as f");
        int i = 0;
        for(Class c : wherebuilder.getInuse())
        {
            if (c.equals(PSComponentSummary.class)) continue;
            querystr.append(',');
            querystr.append(c.getName());
            querystr.append(" as c");
            querystr.append(i++);
        }
        querystr.append(" where ");
        i = 0;
        for(Class c : wherebuilder.getInuse())
        {
            if (c.equals(PSComponentSummary.class)) continue;
            if (i == 0)
            {
                querystr.append("cs.m_contentId = ");
            }
            else
            {
                querystr.append('c');
                querystr.append(i - 1);
                querystr.append(".id.sys_contentid = ");
            }

            querystr.append('c');
            querystr.append(i++);
            querystr.append(".id.sys_contentid and ");
        }
        querystr.append("cs.m_contentTypeId = ");
        querystr.append(typeid);
        querystr.append(" and c0.id.sys_revision = "
                + "cs.m_currRevision");
        if (!StringUtils.isBlank(where))
        {
            //If there is more than one where clause condition
            //then an extra AND is left out in query that needs to be removed
            if(where.trim().toUpperCase().startsWith("( AND")){
                where = where.replace("( AND","( ");
            }
            querystr.append(" and ");
            querystr.append(where);
        }
        querystr.append(' ');
        querystr.append(sort);

        if (ms_log.isDebugEnabled())
            ms_log.debug("[" + Thread.currentThread().getId() + "] HQL Query execution: " + querystr.toString());

        Query q = s.createQuery(querystr.toString());
        Map<String, Object> qparams = wherebuilder.getQueryParams();
        for (String pname : qparams.keySet())
        {
            Object value = qparams.get(pname);
            logParameter(pname, value);

            q.setParameter(pname, value);
        }
        if (maxresults > 0)
            q.setMaxResults(maxresults);

        return q;
    }

    /**
     * Gathers or merges the current result.
     *
     * @param results the returned results that contains all collected
     * result set, assumed not <code>null</code>.
     *
     * @param rval the current result set to be collected, assumed
     * not <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    private void gatherQueryResults(List<Map> results, PSQueryResult rval)
    {
        // The results are a map. Create a row for each result
        for (Map result : results)
        {
            // Handle folder info, replace "sys_folderid" with "rx:sys_folderid"
            Integer fid = (Integer) result.get(IPSHtmlParameters.SYS_FOLDERID);
            if (fid != null)
            {
                result.remove(IPSHtmlParameters.SYS_FOLDERID);
                result.put(IPSContentPropertyConstants.RX_SYS_FOLDERID, fid);
            }
            rval.addRow(new PSRow(result));
        }
    }

    /**
     * Limits the result set to the specified maximum result set.
     *
     * @param rval the query result set to be limited, assumed not <code>null</code>.
     * @param maxresults the maximum number of result set, <code>-1</code> is
     * unlimited.
     * @param columns the column names of the query, not <code>null</code>
     * @param rowcomparator the row comparator of the result set.
     *
     * @return the limited result set, never <code>null</code>.
     */
    private PSQueryResult limitQueryResults(PSQueryResult rval, int maxresults,
                                            String[] columns, PSRowComparator rowcomparator)
    {
        if (maxresults > 0 && rval.getCount() > maxresults)
        {
            // Create a new return object and add just max results rows
            PSQueryResult limited = new PSQueryResult(columns, rowcomparator);
            RowIterator riter = rval.getRows();
            for (int i = 0; i < maxresults; i++)
            {
                limited.addRow((PSRow) riter.nextRow());
            }
            rval = limited;
        }
        return rval;
    }

    /**
     * Perform the internal portion of the query, starting with the query
     * description in the query node. This is done by executing one query per
     * specified content type. The where clause is usable for all types. Note
     * that if a type is found that doesn't implement all the referenced
     * properties, this will throw an invalid query exception.
     * <p>
     * Each query is processed in stages. There are initial stages that are in
     * common, then a series of stages that are performed for each content
     * type involved.
     * <p>
     * The first stage is actually performed outside of this code. The initial
     * query, in XPath or SQL format is parsed into a descriptive tree of
     * operators and operands. This tree is the core of the processing here.
     * <p>
     * The code below starts with this tree, and the remainder of the query
     * description from the parsing process, which consists of the filter
     * (query), the projection (what fields are desired, or the node), and
     * the ordering criteria.
     * <p>
     * The code starts with building a list of all content types that will be
     * queried. The eventual processing works one content item at a time to
     * build the finished list. Each query will be limited by the max results
     * and therefore contains ordering information so that we get the right
     * first results from each query. However, we also need to order the
     * complete results before doing a max results limit on the finished set.
     * <p>
     * The tree is first expanded, replacing references to <em>jcr:path</em> with
     * folder id references. This uses the {@link FolderExpander} to process
     * the paths supplied to the jcr:path <em>like</em> or <em>=</em> operators.
     * <p>
     * This results in an internal where tree which is used as a start for
     * each per content type iteration. Each iteration runs this tree first
     * through a property limiter that removes any properties that are not
     * available for a given content type. This is evaluated effectively as
     * a <em>false</em> value and replaced by a comparison like 1 <> 1.
     * <p>
     * Lastly, the where clause is built. This is then used to create the HQL
     * query that is passed into Hibernate.
     * <p>
     * The result of the query is a result object. This is a bimorphic object
     * that represents either a set of Nodes or a set of properties (projection)
     * on a series of nodes.
     *
     * @param query the query to perform, never <code>null</code>
     * @param maxresults the maximum number of results to return or 0 for no
     *           limit
     * @param params a map of variables to be substituted in the query, may be
     *           <code>null</code>
     * @param locale the locale to use when sorting results, may be
     *           <code>null</code> or empty.
     * @return a list of result guids, never <code>null</code>
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException if the query fails
     *
     * @see IPSQueryNode and its concrete class implementations to understand
     * the query tree
     * @see PSQueryNodeVisitor and its subclasses to understand the visitor
     * pattern used to process the query tree
     */
    @SuppressWarnings("unchecked")
    public QueryResult executeInternalQuery(javax.jcr.query.Query query,
                                            int maxresults, Map<String, ? extends Object> params, String locale)
            throws InvalidQueryException, RepositoryException
    {
        if (query == null)
        {
            throw new IllegalArgumentException("query may not be null");
        }

        // Get the list of types
        PSQuery psquery = (PSQuery) query;
        Set<Long> typeids = getTypeIds(psquery);

        String columns[] = getQueryColumns(psquery, typeids);

        PSRowComparator rowcomparator = psquery.getSorter();
        if (StringUtils.isNotBlank(locale))
        {
            rowcomparator.setLocale(new Locale(locale));
        }
        PSQueryResult rval = new PSQueryResult(columns, rowcomparator);
        Session s = sessionFactory.getCurrentSession();
        List<Long> collectionIds = Collections.EMPTY_LIST;
        try
        {
            s.enableFilter("relationshipConfigFilter");
            PSPair<IPSQueryNode, List<Long>> pair = getWhereClause(query, s, params, collectionIds);
            if (pair == null)
                return rval;

            IPSQueryNode internalwhere = pair.getFirst();
            collectionIds = pair.getSecond();

            for (Long typeid : typeids)
            {
                Query q = prepareQuery(psquery, typeid, s, internalwhere, maxresults, params);
                if (q == null)
                    continue;

                PSStopwatch sw = new PSStopwatch();
                sw.start();
                List<Map> results = (collectionIds.size() > 0) ? (List)executeQuery(q) : q.list();
                sw.stop();

                if (ms_log.isDebugEnabled())
                    ms_log.debug("HQL Query execution on content type " + typeid + ":" + sw);

                gatherQueryResults(results, rval);
            }

            // Limit results?
            rval = limitQueryResults(rval, maxresults, columns, rowcomparator);
        }
        finally
        {
            if (!collectionIds.isEmpty())
            {
                // Cleanup any ids allocated in the temp table
                for (Long idset : collectionIds)
                {
                    clearIdSet(s, idset);
                }
            }

        }

        return rval;
    }

    /**
     * Package function to translate the field names for the component summary to
     * instance fields needed for HQL and object access. It is an error to call
     * this before the system is configured.
     *
     * @param fieldname the name of the field to translate, never
     *           <code>null</code> or empty
     * @return the mapped name, <code>null</code> if unknown
     */
    public static String mapCSFieldToProperty(String fieldname)
    {
        if (StringUtils.isBlank(fieldname))
        {
            throw new IllegalArgumentException(
                    "fieldname may not be null or empty");
        }
        if (ms_csFieldToProperties.size() == 0)
        {
            throw new IllegalStateException("Not configured yet!");
        }
        return ms_csFieldToProperties.get(fieldname);
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.contentmgr.impl.query.IPSPropertyMapper#translateProperty(java.lang.String)
     */
    public String translateProperty(String propname)
    {
        String mapped = ms_csFieldToProperties.get(propname);

        if (mapped != null)
        {
            return "sys_componentsummary." + mapped;
        }
        else
        {
            return propname;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.contentmgr.impl.IPSContentRepository#loadBodies(java.util.List)
     */
    @SuppressWarnings("unchecked")
    public void loadBodies(List<Node> nodes) throws RepositoryException
    {
        Session s = sessionFactory.getCurrentSession();
        PSRequest req = (PSRequest) PSRequestInfo
                .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
        boolean allowBinaryNew=false ;
        if(req.getParameter("allowBinary")=="true"){
            allowBinaryNew=true;
            req.setParameter("allowBinary","false");
        }
        //Load Image Pages
        if(req.getRequestFileURL() != null && req.getRequestFileURL().contains("percImageAsset")){
            allowBinaryNew=true;
        }


        // For each node, load the body data
        for (Node n : nodes)
        {
            PSContentNode cn = (PSContentNode) n;
            PSContentNode parent = (PSContentNode) n.getParent();
            PSContentPropertyLoader loader = cn.getLazyLoader();
            PSTypeConfiguration type = cn.getConfiguration();
            PSLegacyGuid lg = (PSLegacyGuid) cn.getGuid();
            Class ic = type.getLazyLoadClass();
            if (ic == null)
                continue;
            Object data = null;
            String[] columnNames = getSessionFactory().getClassMetadata(ic).getPropertyNames();

            if (cn.getConfiguration().isParent()) {

                if(ic.getName().contains("_lobs") && !allowBinaryNew) {

                    PSLegacyCompositeId id = new PSLegacyCompositeId((PSLegacyGuid)
                            cn.getGuid());

                    StringBuffer query = new StringBuffer();
                    query.append("Select ");

                    for (String field : columnNames) {
                        if(type.getM_fieldToType().get(field)!=null && type.getM_fieldToType().get(field).equals(Blob.class))
                            continue;

                        query.append("ab."+field + " as "+COLUMN_PREFIX_CGLIB+field+ ",");//FOR ANOTHER LIBRARY NEED TO CHANGE LOGIC
                    }

                    //removing comma from endPSJdbcImportExportHelperTests
                    if (query.length() > 0) {
                        query.setLength(query.length() - 1);
                    }
                    query.append(" from ");
                    query.append(ic.getName() + " ab");
                    query.append(" where ab.sys_id.sys_contentid = :cid " +
                            "and ab.sys_id.sys_revision = :rev");
                    List datas = s.createQuery(query.toString()).setParameter("cid",id.getSys_contentid())
                            .setParameter("rev", id.getSys_revision()).setResultTransformer(Transformers.aliasToBean(ic)).list();

                    if (datas.isEmpty()) continue;
                    data = datas.get(0);
                } else {
                    PSLegacyCompositeId id = new PSLegacyCompositeId((PSLegacyGuid)
                            cn.getGuid());
                    data = s.get(ic, id);
                }


            } else {
                PSLegacyGuid pg = (PSLegacyGuid) parent.getGuid();
                StringBuffer query = new StringBuffer();
                query.append("from ");
                query.append(ic.getName());
                query.append(" where sys_contentid = :cid " +
                        "and sys_revision = :rev and sys_sysid = :child");

                List children = sessionFactory.getCurrentSession().createQuery(
                        query.toString()).setParameter("cid", pg.getContentId())
                        .setParameter("rev", pg.getRevision())
                        .setParameter("child", lg.getContentId()).list();
                if (children.isEmpty()) continue;
                data = children.get(0);
            }
            loader.setData(data);
        }
    }

    public NodeType findNodeType(PSNodeDefinition nodeDef) throws NoSuchNodeTypeException
    {
        PSContentTypeKey key = new PSContentTypeKey(nodeDef.getId());
        synchronized(ms_configuration)
        {
            NodeType type = ms_configuration.get(key);
            if (type == null)
            {
                throw new NoSuchNodeTypeException("Not found: " + nodeDef.getName());
            }
            else
            {
                return type;
            }
        }
    }

}
