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
package com.percussion.pagemanagement.dao.impl;

import com.percussion.pagemanagement.dao.IPSTemplateDao;
import com.percussion.pagemanagement.dao.IPSWidgetItemIdGenerator;
import com.percussion.pagemanagement.data.PSMetadataDocType;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplate.PSTemplateTypeEnum;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.IPSTemplateService.PSTemplateException;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateBinding;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.dao.IPSContentItemDao;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.dao.impl.PSContentItem;
import com.percussion.share.data.IPSContentItem;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.theme.data.PSThemeSummary;
import com.percussion.theme.service.IPSThemeService;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.service.impl.PSSiteConfigUtils;
import com.percussion.webservices.assembly.IPSAssemblyDesignWs;
import com.percussion.webservices.content.IPSContentWs;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.percussion.sitemanage.service.IPSSiteSectionMetaDataService.SECTION_SYSTEM_FOLDER_NAME;
import static com.percussion.sitemanage.service.IPSSiteSectionMetaDataService.TEMPLATES;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNumeric;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

/**
 * 
 * Low level CRUD of templates.
 * <p>
 * Templates with {@link PSTemplate#isReadOnly()} <code>true</code> cannot be
 * saved.
 * 
 * @author adamgent
 * 
 */
@Component("templateDao")
@Lazy
@Transactional(noRollbackFor = Exception.class)
public class PSTemplateDao implements IPSTemplateDao, ApplicationContextAware
{

    private IPSContentItemDao contentItemDao;

    private IPSIdMapper idMapper;

    private IPSAssemblyService assemblyService;

    private IPSAssemblyDesignWs assemblyDesignWs;

    private IPSWidgetItemIdGenerator widgetItemIdGenerator;

    private IPSSiteManager siteMgr;

    private IPSContentWs contentWs;

    private IPSFolderHelper folderHelper;

    private IPSThemeService themeService;

    

    private IPSGuidManager guidMgr;

    public static final String TPL_CONTENT_TYPE = IPSTemplateService.TPL_CONTENT_TYPE;

    /**
     * The fully qualified name of the page-assembler exit.
     */
    public static final String PAGE_ASSEMBLER = "Java/global/percussion/assembly/pageAssembler";

    private static final String SERVLET_ROOT = "/Rhythmyx/";

    private static final String TPL_IMAGES_DIR = "rx_resources/images/TemplateImages";

    /**
     * The shared (site) name for all system templates. This is used to retrieve
     * image URLs of the system templates.
     */
    private static final String ANY_SITE = "AnySite";

    @Autowired
    public PSTemplateDao(IPSAssemblyDesignWs assemblyDesignWs, IPSAssemblyService assemblyService,
            IPSContentItemDao contentItemDao, IPSIdMapper idMapper, IPSWidgetItemIdGenerator widgetItemIdGenerator,
            IPSSiteManager siteMgr, IPSContentWs contentWs, IPSFolderHelper folderHelper,
            IPSGuidManager guidMgr)
    {
        super();
        this.assemblyDesignWs = assemblyDesignWs;
        this.assemblyService = assemblyService;
        this.contentItemDao = contentItemDao;
        this.idMapper = idMapper;
        this.widgetItemIdGenerator = widgetItemIdGenerator;
        this.siteMgr = siteMgr;
        this.contentWs = contentWs;
        this.folderHelper = folderHelper;
        this.guidMgr = guidMgr;

        m_imgFileExts.add(".gif");
        m_imgFileExts.add(".jpeg");
        m_imgFileExts.add(".jpg");
        m_imgFileExts.add(".png");
    }

    public void delete(String id) throws PSDataServiceException {
        PSTemplate template = find(id);
        contentItemDao.delete(id);
        PSNotificationEvent notifyEvent = new PSNotificationEvent(EventType.TEMPLATE_DELETE, id);
        IPSNotificationService srv = PSNotificationServiceLocator.getNotificationService();
        srv.notifyEvent(notifyEvent);
    }

    public PSTemplate find(String id) throws PSDataServiceException {
        notNull(id, "id");

        IPSGuid assemblyTemplateGuid = getAssemblyTemplateGuid(id);
        if (assemblyTemplateGuid != null)
        {
            log.debug("Finding assembly template for id: {}" , id);
            PSTemplate t = new PSTemplate();
            try {
                loadTemplateFromBaseTemplate(assemblyTemplateGuid, t);
            } catch (PSTemplateException e) {
               throw new LoadException(e.getMessage(),e);
            }
            return t;
        }

        IPSContentItem contentItem = contentItemDao.find(id);
        if (contentItem == null) {
            return null;
        }

        if (!isTemplateType(contentItem))
        {
            log.debug("Item is not of template type");
            return null;
        }

        isTrue(isNotBlank(contentItem.getId()), "contentItem#getId() is blank");
        PSTemplate template = new PSTemplate();

        Map<String, Object> f = contentItem.getFields();

        template.setId(contentItem.getId());
        template.setName((String) f.get("sys_title"));
        template.setDescription((String) f.get("description"));
        template.setLabel((String) f.get("label"));
        template.setHtmlHeader((String) f.get("htmlHeader"));
        template.setCssRegion((String) f.get("cssRegion"));
        template.setCssOverride((String) f.get("cssOverride"));
        template.setTheme((String) f.get("theme"));
        template.setSourceTemplateName((String) f.get("srcTemplate"));
        template.setType((String) f.get("type"));

        String version = (String) f.get("content_migration_version");
        if (isNumeric(version)) {
            template.setContentMigrationVersion(version);
        }

        PSHtmlMetadataUtils.fromMap(template, f);

        // Convert doc type value to doc type object
        String customDocType = (String) f.get("doc_type");
        PSMetadataDocType metaDocType = PSMetadataDocTypeUtils.convertDocTypeValueToObject(customDocType);
        template.setDocType(metaDocType);

        String bodyMarkup = (String) f.get("bodyMarkup");
        template.setBodyMarkup(bodyMarkup);

        String data = (String) f.get("data");

        if (isNotBlank(data))
        {
            if (log.isTraceEnabled()) {
                log.trace("Unmarshaling Region tree: {}", data);
            }
            PSRegionTree tree = PSSerializerUtils.unmarshal(data, PSRegionTree.class);
            widgetItemIdGenerator.generateIds(tree);
            template.setRegionTree(tree);
        }

        return template;
    }

    /**
     * Validate that the content item is of the right type.
     * 
     * @param contentItem
     */
    private void validateType(IPSContentItem contentItem) throws LoadException {
        notNull(contentItem, "contentItem");
        if (!isTemplateType(contentItem))
        {
            throw new LoadException("Id: " + contentItem.getId() + " is of type: " + contentItem.getType()
                    + " should be of type: " + TPL_CONTENT_TYPE);
        }
    }

    private boolean isTemplateType(IPSContentItem contentItem)
    {
        return TPL_CONTENT_TYPE.equals(contentItem.getType());
    }

    public List<PSTemplate> findAll() throws PSDataServiceException {
        Collection<Integer> ids = contentItemDao.findAllItemIdsByType(TPL_CONTENT_TYPE);
        List<PSTemplate> results = new ArrayList<>();
        for (Integer id : ids)
        {
            PSLegacyGuid guid = new PSLegacyGuid(id, -1);
            String sid = idMapper.getString(guid);
            results.add(find(sid));
        }
        return results;
    }

    public List<PSTemplateSummary> findAllSummaries() throws com.percussion.share.dao.IPSGenericDao.LoadException, PSTemplateException {
        List<PSTemplateSummary> templateSummaries = new ArrayList<>();
        templateSummaries.addAll(findBaseTemplates("all"));
        templateSummaries.addAll(findAllUserTemplateSummariesByType(PSTemplateTypeEnum.NORMAL));

        Collections.sort(templateSummaries, tempSumComp);

        return templateSummaries;
    }

    public List<PSTemplateSummary> findAllSummaries(String siteName)
            throws com.percussion.share.dao.IPSGenericDao.LoadException, PSTemplateException {
        List<PSTemplateSummary> templateSummaries = new ArrayList<>();
        templateSummaries.addAll(findBaseTemplates("all"));
        if (!("unknown".equals(siteName)))
        {
            templateSummaries.addAll(findAllUserTemplateSummariesByType(PSTemplateTypeEnum.NORMAL, siteName));
        }

        Collections.sort(templateSummaries, tempSumComp);

        return templateSummaries;
    }

    @Override
    public PSTemplate save(PSTemplate template) throws PSDataServiceException {
           return save(template, null);
    }

    public PSTemplate save(PSTemplate template, String siteId)
            throws PSDataServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Saving template: " + template);
        }

        notNull(template, "template");

        String id = template.getId();
        PSContentItem contentItem;

        if (template.isReadOnly())
        {
            throw new SaveException("Cannot save readonly template: " + template);
        }
        if (id == null)
        {
            contentItem = new PSContentItem();
            contentItem.setType(TPL_CONTENT_TYPE);
        }
        else
        {
            contentItem = contentItemDao.find(id);
            if (contentItem == null)
            {
                throw new SaveException("Cannot save template with id: " + id + " Template does not exist anymore");
            }
        }
        validateType(contentItem);

        Map<String, Object> f = contentItem.getFields();
        f.put("sys_title", template.getName());
        f.put("description", template.getDescription());
        f.put("label", template.getLabel());
        f.put("htmlHeader", template.getHtmlHeader());
        f.put("cssRegion", template.getCssRegion());
        f.put("cssOverride", template.getCssOverride());
        f.put("theme", template.getTheme());
        f.put("srcTemplate", template.getSourceTemplateName());
        f.put("bodyMarkup", template.getBodyMarkup());
        String docTypeValue = PSMetadataDocTypeUtils.getDocType(template.getDocType());
        f.put("doc_type", docTypeValue);
        f.put("type", template.getType());
        f.put("content_migration_version", template.getContentMigrationVersion());

        PSHtmlMetadataUtils.toMap(template, f);

        PSRegionTree tree = template.getRegionTree();
        if (tree != null)
        {
            widgetItemIdGenerator.generateIds(tree);
            String data = PSSerializerUtils.marshal(tree);
            f.put("data", data);
        }

        if (siteId != null)
        {
            IPSSite site = siteMgr.findSite(siteId);
            String folderPath = getSiteTemplateFolderPath(site);

            contentItem.setFolderPaths(asList(folderPath));
        }

        contentItem = contentItemDao.save(contentItem);
        PSNotificationEvent notifyEvent = new PSNotificationEvent(EventType.TEMPLATE_SAVED, contentItem.getId());
        IPSNotificationService srv = PSNotificationServiceLocator.getNotificationService();
        srv.notifyEvent(notifyEvent);
        return find(contentItem.getId());
    }

    /**
     * Creates a template summary from the specified read only template summary.
     * This is for read only template, not for read/write template.
     * 
     * @param sum the read only template summary of the template, assumed not
     *            <code>null</code>.
     * @param imagePath the image path that is absolute path to the root of the
     *            main serlvet, assumed not <code>null</code>.
     * 
     */
    private void createReadOnlyTemplateSummary(PSTemplateSummary tsum, IPSCatalogSummary sum, String imagePath)
    {
        tsum.setId(idMapper.getString(sum.getGUID()));
        tsum.setName(sum.getName());
        tsum.setLabel(sum.getLabel());
        tsum.setDescription(sum.getDescription());
        tsum.setImageThumbPath(imagePath);
        tsum.setReadOnly(true);
    }

    /*
     * //see base interface method for details
     */
    public PSTemplate createTemplate(String name, String srcId) throws PSDataServiceException {
        return createTemplateFromSrc(srcId, name);
    }

    /**
     * Creates a core item from a system (read only) template.
     * 
     * @param srcId the ID of the system template, assumed not <code>null</code>
     *            .
     * @param name the name of the new item, assumed not <code>null</code>.
     * 
     * @return the core item, never <code>null</code>.
     */
    private PSTemplate createTemplateFromSrc(String srcId, String name) throws PSDataServiceException {
        PSTemplate template;
        IPSGuid templateGuid = getAssemblyTemplateGuid(srcId);
        if (templateGuid != null)
        {
            template = new PSTemplate();
            loadTemplateFromBaseTemplate(templateGuid, template);
            String imagePath = getThumbImgPath(asList(template.getName())).get(0);
            template.setName(name);
            template.setImageThumbPath(imagePath);
            template.setReadOnly(false);
            template.setId(null);
            template.setDocType(PSMetadataDocTypeUtils.getDefaultDocType());
            template.setType(PSTemplateTypeEnum.NORMAL.getLabel());
        }
        else
        {
            template = createTemplateFromUserTemplate(srcId, name);
        }

        template.setContentMigrationVersion("0");

        return template;
    }

    private IPSGuid getAssemblyTemplateGuid(String templateId)
    {
        IPSGuid templateGuid = idMapper.getGuid(templateId);
        if (templateGuid.getType() == PSTypeEnum.TEMPLATE.getOrdinal()) {
            return templateGuid;
        }
        return null;
    }

    /**
     * Creates a core item from a system template.
     * 
     * @param srcId the ID of the system template, assumed not <code>null</code>
     *            .
     * @param templateName the name of the created template item, assumed not blank.
     * 
     */
    private void loadTemplateFromBaseTemplate(IPSGuid srcId, PSTemplate templateName) throws PSTemplateException {

        try
        {
            IPSAssemblyTemplate srcTpl = loadBaseTemplateById(srcId);
            String imagePath = getThumbImgPath(asList(srcTpl.getName())).get(0);
            IPSCatalogSummary sum = findAssemblyTemplate(srcTpl.getName()).get(0);
            createReadOnlyTemplateSummary(templateName, sum, imagePath);
            templateName.setSourceTemplateName(srcTpl.getName());
            String srcContent = srcTpl.getTemplate();
            if (StringUtils.isNotBlank(srcContent))
            {
                templateName.setBodyMarkup(srcContent);
            }
            String cssRegion = getCssRegionBinding(srcTpl);
            if (StringUtils.isNotBlank(cssRegion))
            {
                templateName.setCssRegion(cssRegion);
            }
            // set theme
            List<PSThemeSummary> themes = themeService.findAll();
            if (!themes.isEmpty())
            {
                templateName.setTheme(themes.get(0).getName());
            }
        }
        catch (Exception e)
        {
            throw new PSTemplateException("Failed to copy system template to PSCoreItem.", e);
        }
    }

    /**
     * Gets the expression of the {@link #CSS_REGION_VARIABLE} binding variable
     * from the given base template.
     * 
     * @param srcTemplate the base template, assumed not <code>null</code>.
     * 
     * @return the binding expression, may be blank if not defined.
     */
    private String getCssRegionBinding(IPSAssemblyTemplate srcTemplate)
    {
        for (IPSTemplateBinding binding : srcTemplate.getBindings())
        {
            if (CSS_REGION_VARIABLE.equalsIgnoreCase(binding.getVariable())) {
                return binding.getExpression();
            }
        }
        return null;
    }

    /**
     * Creates a core item from an user template.
     * 
     * @param srcId the ID of the uesr template, assumed not <code>null</code>.
     * @param name the name of the created template item, assumed not blank.
     * 
     * @return the created core item, never <code>null</code>.
     */
    private PSTemplate createTemplateFromUserTemplate(String srcId, String name) throws PSDataServiceException {
        PSTemplate srcTpl = find(srcId);
        PSTemplate copy = srcTpl.clone();
        copy.setId(null);
        copy.setName(name);
        return copy;
    }

    /*
     * //see base class method for details
     */
    public IPSAssemblyTemplate loadBaseTemplateById(IPSGuid id) throws PSTemplateException {
        if (id == null) {
            throw new IllegalArgumentException("id may not be null.");
        }

        try
        {
            return assemblyService.loadUnmodifiableTemplate(id);
        }
        catch (PSAssemblyException e)
        {
            throw new PSTemplateException("Failed to find Template with ID = " + id.toString(), e);
        }
    }

    /*
     * //see base class method for details
     */
    public PSAssemblyTemplate loadBaseTemplateByName(String name) throws PSTemplateException {
        try
        {
            PSAssemblyTemplate tpl = assemblyService.findTemplateByName(name);
            if (tpl == null)
            {
                throw new PSTemplateException("Failed to find Template with name = " + name);
            }
            return tpl;
        }
        catch (PSAssemblyException e)
        {
            throw new PSTemplateException("Failed to find Template with name = " + name, e);
        }
    }

    public List<IPSCatalogSummary> findBaseAssemblyTemplates(String type)
    {

        List<IPSCatalogSummary> templates = new ArrayList<>();
        if (type.equalsIgnoreCase("base"))
        {
            templates.addAll(assemblyDesignWs.findAssemblyTemplates("perc.base.*", null, null, null, null, null,
                    PAGE_ASSEMBLER));
        }
        else if (type.equalsIgnoreCase("resp"))
        {
            templates.addAll(assemblyDesignWs.findAssemblyTemplates("perc.resp.*", null, null, null, null, null,
                    PAGE_ASSEMBLER));
        }
        else if (type.equalsIgnoreCase("all"))
        {
            templates.addAll(assemblyDesignWs.findAssemblyTemplates("perc.base.*", null, null, null, null, null,
                    PAGE_ASSEMBLER));
            templates.addAll(assemblyDesignWs.findAssemblyTemplates("perc.resp.*", null, null, null, null, null,
                    PAGE_ASSEMBLER));
        }
        return templates;

    }

    public List<IPSCatalogSummary> findAssemblyTemplate(String name)
    {
        return assemblyDesignWs.findAssemblyTemplates(name, null, null, null, null, null,
                PAGE_ASSEMBLER);
    }

    /**
     * Gets the image paths for the specified image names.
     * 
     * @param names the image names, assumed not empty.
     * 
     * @return image paths, which is the absolute path to the main servlet of
     *         the server (without protocol//host:port).
     */
    private List<String> getThumbImgPath(List<String> names)
    {
        List<String> imgs = new ArrayList<>();
        for (String path : assemblyDesignWs.getTemplateThumbImages(names, ANY_SITE))
        {
            imgs.add(SERVLET_ROOT + path);
        }
        return imgs;
    }

    /*
     * //see base interface method for details
     */
    public List<PSTemplateSummary> findBaseTemplates(String type)
    {
        List<IPSCatalogSummary> templates = findBaseAssemblyTemplates(type);
        if (templates.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> names = new ArrayList<>();
        for (IPSCatalogSummary summary : templates)
        {
            names.add(summary.getName());
        }
        List<String> imgs = getThumbImgPath(names);

        List<PSTemplateSummary> results = new ArrayList<>();
        for (int i = 0; i < templates.size(); i++)
        {
            PSTemplateSummary result = new PSTemplateSummary();
            createReadOnlyTemplateSummary(result, templates.get(i), imgs.get(i));
            results.add(result);
        }

        Collections.sort(results, tempSumComp);

        return results;
    }

    /*
     * //see base interface method for details
     */
    public List<PSTemplateSummary> findAllUserTemplates() throws PSTemplateException {
        List<PSTemplateSummary> results = new ArrayList<>();
        List<IPSSite> allSites = siteMgr.findAllSites();
        for (IPSSite site : allSites)
        {
            String path = getSiteTemplateFolderPath(site);
            List<PSItemSummary> items = contentWs.findFolderChildren(path, false);
            for (PSItemSummary item : items)
            {
                String id = idMapper.getString(item.getGUID());
                PSTemplateSummary template = loadUserTemplateSummary(id, site.getName());
                if (template != null) {
                    results.add(template);
                }
            }
        }
        return results;
    }

    public List<PSTemplateSummary> findAllUserTemplates(String siteName) throws PSTemplateException {
        List<PSTemplateSummary> results = new ArrayList<>();

        IPSSite site = siteMgr.findSite(siteName);
        String path = getSiteTemplateFolderPath(site);
        List<PSItemSummary> items = contentWs.findFolderChildren(path, false);
        for (PSItemSummary item : items)
        {
            String id = idMapper.getString(item.getGUID());
            PSTemplateSummary template = loadUserTemplateSummary(id, site.getName());
            if (template != null) {
                results.add(template);
            }
        }
        return results;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.pagemanagement.dao.IPSTemplateDao#
     * findAllUserTemplateSummariesByType
     * (com.percussion.pagemanagement.data.PSTemplate.PSTemplateTypeEnum)
     */
    public List<PSTemplateSummary> findAllUserTemplateSummariesByType(PSTemplateTypeEnum type) throws PSTemplateException {
        List<PSTemplateSummary> templateSummaries = findAllUserTemplates();
        List<PSTemplateSummary> results = new ArrayList<>();
        for (PSTemplateSummary item : templateSummaries)
        {
            try {
                PSTemplate template = find(item.getId());
                if (type == null || type.equals(PSTemplateTypeEnum.NORMAL)) {
                    if (template.getType() == null
                            || PSTemplateTypeEnum.NORMAL.equals(PSTemplateTypeEnum.getEnum(template.getType()))) {
                        results.add(item);
                    }
                } else if (type.equals(PSTemplateTypeEnum.getEnum(template.getType()))) {
                    results.add(item);
                }
            } catch (PSDataServiceException e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(),e);
                //Allow loop to continue so that one bad item doesn't prevent all from being processed.
            }
        }

        return results;
    }

    public List<PSTemplateSummary> findAllUserTemplateSummariesByType(PSTemplateTypeEnum type, String siteName) throws PSTemplateException {
        List<PSTemplateSummary> templateSummaries = findAllUserTemplates(siteName);
        List<PSTemplateSummary> results = new ArrayList<>();
        for (PSTemplateSummary item : templateSummaries)
        {
            try {
                PSTemplate template = find(item.getId());
                if (type == null || type.equals(PSTemplateTypeEnum.NORMAL)) {
                    if (template.getType() == null
                            || PSTemplateTypeEnum.NORMAL.equals(PSTemplateTypeEnum.getEnum(template.getType()))) {
                        results.add(item);
                    }
                } else if (type.equals(PSTemplateTypeEnum.getEnum(template.getType()))) {
                    results.add(item);
                }
            } catch (PSDataServiceException e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(),e);
                //Allow processing to continue.
            }
        }

        return results;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.pagemanagement.dao.IPSTemplateDao#findUserTemplatesByType
     * (com.percussion.pagemanagement.data.PSTemplate.PSTemplateTypeEnum)
     */
    public List<PSTemplate> findUserTemplatesByType(PSTemplateTypeEnum type) throws PSTemplateException {
        List<PSTemplateSummary> templateSummaries = findAllUserTemplates();
        List<PSTemplate> results = new ArrayList<>();
        for (PSTemplateSummary item : templateSummaries)
        {
            try {
                PSTemplate template = find(item.getId());
                if (type == null || type.equals(PSTemplateTypeEnum.NORMAL)) {
                    if (template.getType() == null
                            || PSTemplateTypeEnum.NORMAL.equals(PSTemplateTypeEnum.getEnum(template.getType()))) {
                        results.add(template);
                    }
                } else if (type.equals(PSTemplateTypeEnum.getEnum(template.getType()))) {
                    results.add(template);
                }
            } catch (PSDataServiceException e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(),e);
                //Continue
            }
        }

        return results;
    }

    /**
     * Gets the folder path that is used to store all templates for the
     * specified site.
     * 
     * @param site the site in question, assumed not <code>null</code>.
     * 
     * @return the folder path, not blank.
     */
    private String getSiteTemplateFolderPath(IPSSite site)
    {
        return folderHelper.concatPath(site.getFolderRoot(), SECTION_SYSTEM_FOLDER_NAME, TEMPLATES);
    }

    /*
     * This method does not make sense any more because the same template can be
     * used for different site. This is only used by unit test for now and
     * should be removed along with related unit test.
     */
    public PSTemplate findUserTemplateByName_UsedByUnitTestOnly(String name) throws PSDataServiceException {
        for (PSTemplateSummary template : findAllUserTemplates())
        {
            if (template.getName().equalsIgnoreCase(name)) {
                return find(template.getId());
            }
        }

        return null;
    }

    public IPSGuid findUserTemplateIdByName(String templateName, String siteName)
    {
        IPSSite site = siteMgr.findSite(siteName);
        String path = getSiteTemplateFolderPath(site);
        path = folderHelper.concatPath(path, templateName);
        return contentWs.getIdByPath(path);
    }

    public List<PSTemplateSummary> loadUserTemplateSummaries(List<String> ids, String siteName) throws PSTemplateException {
        notNull(ids);

        List<PSTemplateSummary> results = new ArrayList<>();
        for (String id : ids)
        {
            PSTemplateSummary summary = loadUserTemplateSummary(id, siteName);
            if (summary != null) {
                results.add(summary);
            }
        }
        Collections.sort(results, tempSumComp);
        return results;
    }

    /**
     * Loads the specified template.
     * 
     * @param id the ID of the template, assumed not blank.
     * 
     * @return the specified template, never <code>null</code>.
     */
    private PSTemplateSummary loadUserTemplateSummary(String id, String siteName) throws PSTemplateException {
       try {
           PSTemplateSummary summary = find(id);
           if (summary != null) {
               String imgPath = getTemplateThumbPath(summary, siteName);

               summary.setImageThumbPath(imgPath);
           }
           return summary;
       } catch (PSDataServiceException e) {
           throw new PSTemplateException(e.getMessage(),e);
       }
    }

    /**
     * see base interface method for details
     */
    public String getTemplateThumbPath(PSTemplateSummary summary, String siteName)
    {
        String imgPath = null;
        if (summary != null)
        {

            String sumName = summary.getName();
            if (StringUtils.isNotBlank(siteName))
            {
                imgPath = getImgPath(TPL_IMAGES_DIR + '/' + siteName + '/' + sumName);

                if (imgPath == null)
                {
                    imgPath = getImgPath(TPL_IMAGES_DIR + '/' + siteName + '/' + summary.getId() + "-template");
                }

                if (imgPath == null)
                {
                    imgPath = getImgPath(TPL_IMAGES_DIR + '/' + ANY_SITE + '/' + sumName);
                    PSNotificationEvent notifyEvent = new PSNotificationEvent(EventType.TEMPLATE_SAVED, summary.getId());
                    IPSNotificationService srv = PSNotificationServiceLocator.getNotificationService();
                    srv.notifyEvent(notifyEvent);
                }
            }

            if (imgPath == null)
            {
                List<String> imgs = getThumbImgPath(Collections.singletonList(summary.getSourceTemplateName()));
                imgPath = imgs.get(0);
                PSNotificationEvent notifyEvent = new PSNotificationEvent(EventType.TEMPLATE_LOAD, summary.getId());
                IPSNotificationService srv = PSNotificationServiceLocator.getNotificationService();
                srv.notifyEvent(notifyEvent);
            }
        }

        return imgPath;
    }

    /**
     * Generates the absolute path for the template icon image based on image
     * extension sorted alphabetically.
     * 
     * @param path - the path of the template icon image.
     * 
     * @return absolute path of template icon image
     */
    private String getImgPath(String path)
    {
        for (String ext : m_imgFileExts)
        {
            String imgPath = (PSSiteConfigUtils.getRootDirectory() + "/" + path + ext).replace("\\", "/");
            File imgFile = new File(imgPath);
            if (imgFile.exists())
            {
                return SERVLET_ROOT + path + ext;
            }
        }

        return null;
    }

    /**
     * Used for sorting of {@link PSTemplateSummary} objects. Sorts
     * alphabetically by name (case-sensitive).
     * 
     * @author peterfrontiero
     */
    public static class PSTemplateSummaryComparator implements Comparator<PSTemplateSummary>
    {
        public int compare(PSTemplateSummary ts1, PSTemplateSummary ts2)
        {
            return ts1.getName().compareTo(ts2.getName());
        }
    }

    /**
     * Used for generating a template to export
     * 
     * @author leonardohildt
     */
    public PSTemplate generateTemplateToExport(String id, String name) throws PSTemplateException {
        notNull(id, "id");
        PSTemplate template;
        // Set the server version
        String serverVersion = PSServer.getVersionString();
        // Strip out the build details
        int indexBuild = serverVersion.indexOf("Build", 0);
        if (indexBuild > 0)
        {
            serverVersion = serverVersion.substring(0, indexBuild - 1);
        }
        try {
            template = find(id);
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new PSTemplateException(e.getMessage(),e);
        }
        template.setId(null);
        // Clean up the name if contains any extension
        int indexFilter = name.indexOf(".xml", 0);
        if (indexFilter > 0)
        {
            name = name.substring(0, indexFilter);
        }
        template.setName(name);
        template.setServerVersion(serverVersion);
        PSRegionTree tree = template.getRegionTree();
        if (tree != null)
        {
            widgetItemIdGenerator.deleteIds(tree);
            template.setRegionTree(tree);
        }
        return template;
    }

    /**
     * Used for generating a template from a source file
     * 
     * @author leonardohildt
     */
    public PSTemplate generateTemplateFromSource(PSTemplate template, String siteId) throws PSTemplateException {
        if (log.isDebugEnabled()){
            log.debug("Saving template: " + template);}
        notNull(template, "template");

        IPSGuid siteGuid = guidMgr.makeGuid(siteId, PSTypeEnum.SITE);
        IPSSite site = siteMgr.findSite(siteGuid);

        this.lastValueUsed = 0;
        String templateName = generateTemplateName(template.getName(), site);
        template.setName(templateName);
        // Do not considerate the server version
        template.setServerVersion(null);
        // Set a valid theme
        setTheme(template);

        try {
            return save(template, site.getName());
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new PSTemplateException(e.getMessage(),e);
        }
    }

    private String generateTemplateName(String name, IPSSite site) throws PSTemplateException {
        String suggestedName = name;
        String appendSuffix = "";
        if (lastValueUsed == 0)
        {
            suggestedName = name;
        }
        else
        {
            appendSuffix = "(" + this.lastValueUsed + ")";
            int indexFilter = suggestedName.indexOf("(", 0);
            if (indexFilter > 0)
            {
                suggestedName = suggestedName.substring(0, indexFilter);
            }
        }
        this.lastValueUsed++;
        String templateName = suggestedName + appendSuffix;
        // Get the list of template for the given site
        List<PSTemplateSummary> results = new ArrayList<>();

        String path = getSiteTemplateFolderPath(site);
        List<PSItemSummary> items = contentWs.findFolderChildren(path, false);
        for (PSItemSummary item : items)
        {
            String id = idMapper.getString(item.getGUID());
            PSTemplateSummary template = loadUserTemplateSummary(id, site.getName());
            if (template != null) {
                results.add(template);
            }
        }
        // Validate the name against the names of the templates already exist in
        // the site
        for (PSTemplateSummary comparedTemplate : results)
        {
            if (comparedTemplate.getName().equals(templateName))
            {
                return this.generateTemplateName(templateName, site);
            }
        }
        return templateName;
    }

    private void setTheme(PSTemplate object) throws PSTemplateException {
        try {
            // set theme
            List<PSThemeSummary> themes = themeService.findAll();
            if (!themes.isEmpty()) {
                if (object.getTheme() == null) {
                    object.setTheme(themes.get(0).getName());
                } else {
                    boolean existTheme = false;
                    for (PSThemeSummary theme : themes) {
                        if (theme.getName().equals(object.getTheme())) {
                            existTheme = true;
                            break;
                        }
                    }
                    if (!existTheme) {
                        object.setTheme(themes.get(0).getName());
                    }
                }
            }
        } catch (PSDataServiceException e) {
            throw new PSTemplateException(e.getMessage(),e);
        }
    }

    public IPSThemeService getThemeService()
    {
        return themeService;
    }

    public void setThemeService(IPSThemeService themeService)
    {
        this.themeService = themeService;
    }
    /**
     * Used for sorting template summaries. Never <code>null</code>.
     */
    private PSTemplateSummaryComparator tempSumComp = new PSTemplateSummaryComparator();

    /**
     * The binding variable name that contains the CSS used by the regions.
     */
    private static final String CSS_REGION_VARIABLE = "$perc_cssRegion";

    /**
     * Used for generating the suffix for the template name Never
     * <code>null</code>.
     */
    private int lastValueUsed;

    /**
     * Used for crawling through all available image extensions.
     */
    private Set<String> m_imgFileExts = new TreeSet<>();

    private ApplicationContext applicationContext;

    /**
     * The log instance to use for this class, never <code>null</code>.
     */

    private static final Logger log = LogManager.getLogger(PSTemplateDao.class);

   
    // TODO Remove loop,  theme service constructor adds templateService, that adds templateDao, this class
   
    @PostConstruct
    public void postConstruct()
    {
        setThemeService(applicationContext.getBean(IPSThemeService.class));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
        
    }
}
