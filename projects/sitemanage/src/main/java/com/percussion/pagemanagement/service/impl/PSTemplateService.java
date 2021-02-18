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

package com.percussion.pagemanagement.service.impl;

import static com.percussion.share.service.exception.PSParameterValidationUtils.rejectIfBlank;
import static com.percussion.share.service.exception.PSParameterValidationUtils.validateParameters;
import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;

import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.Validate.noNullElements;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;
import static org.apache.commons.lang.math.NumberUtils.toInt;

import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.dao.IPSPageDaoHelper;
import com.percussion.pagemanagement.dao.IPSTemplateDao;
import com.percussion.pagemanagement.dao.IPSWidgetDao;
import com.percussion.pagemanagement.dao.impl.PSHtmlMetadataUtils;
import com.percussion.pagemanagement.data.PSHtmlMetadata;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionCode;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.pagemanagement.data.PSRegionTreeUtils;
import com.percussion.pagemanagement.data.PSRegionWidgetAssociations;
import com.percussion.pagemanagement.data.PSRegionWidgets;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplate.PSTemplateTypeEnum;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.parser.PSParsedRegionTree;
import com.percussion.pagemanagement.parser.PSTemplateRegionParser;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.share.dao.IPSGenericDao.LoadException;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.share.validation.PSValidationErrorsBuilder;
import com.percussion.sitemanage.service.IPSSiteSectionService;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * Will CRUD templates using the {@link IPSTemplateDao}.
 * 
 * @author YuBingChen
 * @author adamgent
 */
@Component("sys_templateService")
@Transactional(noRollbackFor = Exception.class)
public class PSTemplateService implements IPSTemplateService
{

    private IPSTemplateDao templateDao;
    private IPSWidgetAssetRelationshipService widgetAssetRelationshipService;
    private IPSPageDao pageDao;
    private IPSPageDaoHelper pageDaoHelper;
    private IPSWorkflowHelper workflowHelper;
    @Value("${templateService.validatingDeleteTemplate:true}")
    private boolean validatingDeleteTemplate = true;
    private PSAbstractTemplateSorter templateSorter = new PSTemplateSorter();
    private IPSWidgetDao widgetDao;
    private IPSAssemblyService assemblyService;
    private IPSIdMapper idMapper;
    
   
    
    /**
     * Instance of site section service, this service is not autowired by the spring during the constructor. Avoid using 
     * it directly, instead use {@link #getSiteSectionService()}.
     */
     private IPSSiteSectionService siteSectionService;
     
     public IPSSiteSectionService getSiteSectionService()
     {
        if(siteSectionService == null)
        {
           siteSectionService = (IPSSiteSectionService) getWebApplicationContext().getBean("siteSectionService");
        }
        return siteSectionService;
     }

    
    @Autowired
    public PSTemplateService(IPSTemplateDao templateDao, 
            IPSWidgetAssetRelationshipService widgetAssetRelationshipService, 
            IPSPageDao pageDao,
            IPSPageDaoHelper pageDaoHelper,
            IPSWidgetService widgetService,
            IPSWorkflowHelper workflowHelper,
            IPSWidgetDao widgetDao, 
            IPSAssemblyService assemblyService,
            IPSIdMapper idMapper
            )
    {
        super();
        this.templateDao = templateDao;
        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
        this.pageDao = pageDao;
        this.pageDaoHelper = pageDaoHelper;
        this.workflowHelper = workflowHelper;
        this.regionWidgetAssocationsValidator = new RegionWidgetValidator(widgetService);
        this.widgetDao = widgetDao;
        this.assemblyService = assemblyService;
        this.idMapper = idMapper;
        
       
    }

    @Deprecated
    public PSTemplateSummary createTemplate(String name, String srcId)
    {
        return createTemplate(name, srcId, null);
    }
    /*
     * (non-Javadoc)
     * @see com.percussion.pagemanagement.service.IPSTemplateService#createTemplate(java.lang.String, java.lang.String, java.lang.String)
     */
    public PSTemplateSummary createTemplate(String name, String srcId, String siteId)
    {
        return createTemplate(name, srcId, siteId, null);        
    }
    /*
     * (non-Javadoc)
     * @see com.percussion.pagemanagement.service.IPSTemplateService#createTemplate(java.lang.String, java.lang.String, java.lang.String,
     *  com.percussion.pagemanagement.data.PSTemplate.PSTemplateTypeEnum)
     */
    public PSTemplateSummary createTemplate(String name, String srcId, String siteId, PSTemplateTypeEnum type)
    {
        validateParameters("createTemplate").rejectIfBlank("name", name).rejectIfBlank("sourceTemplateId", srcId).throwIfInvalid();
        PSTemplate template = templateDao.createTemplate(name, srcId);
        validate(template);
        updateBodyMarkupOrRegionTree(template);
        if (type != null)
        {
            template.setType(type.getLabel());
        }
        template = templateDao.save(template, siteId);
        
        PSTemplate srcTemplate = templateDao.find(srcId);
        if (!srcTemplate.isReadOnly())
        {
            widgetAssetRelationshipService.copyAssetWidgetRelationships(srcId, template.getId());
        }
        
        return template;        
    }

    public void delete(String id)
    {
        validateParameters("delete").rejectIfBlank("id", id).throwIfInvalid();
        delete(id, false);
    }
    
    public void delete(String id, boolean force)
    {
        PSValidationErrorsBuilder builder = validateParameters("delete")
            .rejectIfBlank("id", id)
            .throwIfInvalid();
        PSTemplate template = null;
        try
        {
            template = load(id);
        }
        catch(LoadException e)
        {
            log.error(e);
            builder.reject("template.failedToLoad", "Cannot delete the selected template as it is not found in the system.");
            builder.throwIfInvalid();
        }
        if (!force)
        {
            String errorMsg = "Template '" +  template.getName() + "' cannot be deleted because it is being used by ";
            if (isAssociatedToPages(id)) {
                errorMsg += "one or more pages.";
                log.error(errorMsg + " Template id: " + id);
                if (isValidatingDeleteTemplate()) {
                    builder.reject("template.inUse", errorMsg);
                    builder.throwIfInvalid();
                }
            }
            else if(isAssociatedToBlogs(id))
            {
                errorMsg += "a blog.";
                log.error( errorMsg + " Template id: " + id);
                if (isValidatingDeleteTemplate()) {
                    builder.reject("template.inUse", errorMsg);
                    builder.throwIfInvalid();
                }
            }
        }
        
        // update the previous revisions of the pages
        pageDaoHelper.replaceTemplateForPageInOlderRevisions(id);
        
        //PSSqlPurgeHelper now handles related asset delete
        //widgetAssetRelationshipService.deleteLocalAssets(id);
        templateDao.delete(id);
    }
    
    /**
     * Checks whether the given template is used by blogs or not.
     * @param templateId assumed to be a valid string of guid representation of template id.
     * @return <code>true</code> if it is being used by a blog, otherwise <code>false</code>.
     */
    private boolean isAssociatedToBlogs(String templateId)
    {
        boolean isUsed = false;
        List<String> blogTemplates = getSiteSectionService().findAllTemplatesUsedByBlogs(null);
        for(String blogTemplate : blogTemplates)
        {
            if(templateId.equals(blogTemplate))
            {
               isUsed = true;
               break;
            }
        }
        return isUsed;
    }


    public PSTemplateSummary find(String id)
    {
        rejectIfBlank("find", "id", id);
        PSTemplate t = templateDao.find(id);
        return fullToSum(t);
    }
    
    /**
     * @deprecated This is used by unit test only. It cannot be used by production code
     */
    public PSTemplateSummary findUserTemplateByName_UsedByUnitTestOnly(String name)
    {
        rejectIfBlank("findUserTemplateByName", "name", name);
        PSTemplate t = templateDao.findUserTemplateByName_UsedByUnitTestOnly(name);
        if (t == null)
            throw new DataServiceLoadException("Failed to find template with name: " + name);
        
        return fullToSum(t);
    }
    
    public IPSGuid findUserTemplateIdByName(String templateName, String siteName)
    {
        rejectIfBlank("findUserTemplateByNameAndSite", "templateName", templateName);
        rejectIfBlank("findUserTemplateByNameAndSite", "siteName", siteName);
        
        IPSGuid templateGuid = templateDao.findUserTemplateIdByName(templateName, siteName);
        
        if (templateGuid == null)
            throw new DataServiceLoadException("Failed to find template with name: " + templateName);
            
        return templateGuid;
    }
    
    private PSTemplateSummary fullToSum(PSTemplate t) {
        PSTemplateSummary ts = new PSTemplateSummary();
        if (t == null)
            return null;
        PSSerializerUtils.copyFullToSummary(t, ts);
        return ts;
    }

    public List<PSTemplateSummary> findAll()
    {
        return sort(templateDao.findAllSummaries());
    }
    
    public List<PSTemplateSummary> findAll(String siteName)
    {
        return sort(templateDao.findAllSummaries(siteName));
    }

    public List<PSTemplateSummary> findAllUserTemplates()
    {
        return sort(templateDao.findAllUserTemplateSummariesByType(PSTemplateTypeEnum.NORMAL));
    }

    public List<PSTemplateSummary> loadUserTemplateSummaries(List<String> ids, String siteName)
    {
       return sort(templateDao.loadUserTemplateSummaries(ids, siteName));
    }
    
    public List<PSTemplateSummary> findBaseTemplates(String type)
    {
        return sort(templateDao.findBaseTemplates(type));
    }
    
    public  List<PSTemplateSummary> sort(List<PSTemplateSummary> items) {
        return templateSorter.sort(items);
    }
    

    public PSTemplate load(String id)
    {
        rejectIfBlank("load", "id", id);
        return templateDao.find(id);
    }
    
    /**
     * see base interface method for details
     */
    public String getTemplateThumbPath(PSTemplateSummary summary, String siteName)
    {
       return templateDao.getTemplateThumbPath(summary, siteName);
    }
    
    public PSTemplate save(PSTemplate object) throws PSBeanValidationException,
    com.percussion.share.service.IPSDataService.DataServiceSaveException
    {
        return save(object, null);
    }

    public PSTemplate save(PSTemplate object, String siteId) throws PSBeanValidationException,
            com.percussion.share.service.IPSDataService.DataServiceSaveException
    {
        return save(object, siteId, null);
    }
    

    @Override
    public PSTemplate save(PSTemplate object, String siteId, String pageId) throws PSBeanValidationException,
            com.percussion.share.service.IPSDataService.DataServiceSaveException
    {
        log.debug("Saving template");
        validate(object);
        
        // if valid page id is supplied, then we'll bump the template's content migration revision
        boolean incrementRevision = false;
        if (pageId != null)
        {
            if (!isValidPageId(pageId))
                throw new DataServiceSaveException("Page must exist and be checked out to the current user");
            incrementRevision = true;
        }
        
        updateBodyMarkupOrRegionTree(object);
        updateMetaData(object);

        PSTemplate template = templateDao.find(object.getId());
        updateRevision(template, object, incrementRevision);
        
        PSTemplate savedTemplate = templateDao.save(object, siteId);
        
        String id = savedTemplate.getId();
        
        // remove assets for the deleted widgets
        widgetAssetRelationshipService.removeAssetWidgetRelationships(id, savedTemplate.getWidgets());
        
        // transition shared assets to Pending
        workflowHelper.transitionToPending(widgetAssetRelationshipService.getSharedAssets(id));        
        
        // update widget names (only if there was a change)
        Map<String, PSPair<String, String>> changedWidgets = getWidgetNamesChanged(template, savedTemplate); 
        if(!changedWidgets.isEmpty())
        {
            widgetAssetRelationshipService.updateWidgetsNames(id, changedWidgets);
        }
        
        return savedTemplate;
    }

    /**
     * Update the revision of the supplied template to save with the value in the current template, optionally
     * incrementing the version.
     * 
     * @param current The current version of the template object, assumed not <code>null</code>.
     * @param toSave The new version of the template object that will be saved, assumed not <code>null</code>.
     * @param increment <code>true</code> to increment the version, <code>false</code> to just copy it.
     * 
     */
    private void updateRevision(PSTemplate current, PSTemplate toSave, boolean increment)
    {
        // No version or bad version interpreted as 0
        int version = toInt(current.getContentMigrationVersion(), 0);
        if (increment)
            version++;
        toSave.setContentMigrationVersion(String.valueOf(version));
    }


    /**
     * Validates the supplied id is a valid id, that a page with that id exists, and that the page is checked out to the current
     * user
     * 
     * @param pageId The id, assumed not <code>null</code>.
     * 
     * @return <code>true</code> if it is valid, false if not.
     */
    private boolean isValidPageId(String pageId)
    {
        boolean isPageIdValid = false;
        try
        {
            PSPage page = pageDao.find(pageId);
            if (page != null)
            {
                isPageIdValid = workflowHelper.isCheckedOutToCurrentUser(pageId);
            }
        }
        catch (Exception e)
        {
            // allow method to return false
        }
        
        return isPageIdValid;
    }

    /**
     * Compares the lists of widgets from the old template and the saved one to
     * see if there has been a change in the widget name. A change in a widget
     * name means that the same widget (the same slot id) in both templates has
     * different name. It only takes into the comparison those widgets that are
     * present in the old template and the updated one.
     * 
     * @param template {@link PSTemplate} the template object as it was before
     *            the update.
     * @param savedTemplate {@link PSTemplate} the template object as after the
     *            update.
     * @return {@link Map}<{@link String}, {@link PSPair}<{@link String},
     *         {@link String}>> The first element is the old name of the widget,
     *         and the second value is the new name. Never <code>null</code>,
     *         but may be empty.
     */
    private Map<String, PSPair<String, String>> getWidgetNamesChanged(PSTemplate template, PSTemplate savedTemplate)
    {
        Map<String, PSPair<String, String>> changedWidgets = new HashMap<>();

        if (template.getWidgets() == null)
        {
            return changedWidgets;
        }

        Map<String, String> oldIdsToWidgetName = getWidgetIdsToNameMap(template.getWidgets());
        Map<String, String> idsToWidgetName = getWidgetIdsToNameMap(savedTemplate.getWidgets());

        for (String oldId : oldIdsToWidgetName.keySet())
        {
            if (!idsToWidgetName.containsKey(oldId))
            {
                continue;
            }

            String oldName = oldIdsToWidgetName.get(oldId);
            String newName = idsToWidgetName.get(oldId);

            if (!(isBlank(newName) && isBlank(oldName)) && !equalsIgnoreCase(oldName, newName))
            {
                changedWidgets.put(oldId, new PSPair<>(oldName, newName));
            }
        }
        return changedWidgets;
    }
    
    /**
     * Iterates over the set of region widgets and builds a map, from the widget
     * id (slot id in the relationships) to the widget name.
     * 
     * @param list {@link List}<{@link PSWidgetItem}> containing the
     *            widget items for a given template. Must not be
     *            <code>null</code>.
     * @return {@link Map}<{@link String}, {@link String}>. Never
     *         <code>null</code>, but may be empty.
     */
    private Map<String, String> getWidgetIdsToNameMap(List<PSWidgetItem> list)
    {
        Map<String, String> map = new HashMap<>();
        for (PSWidgetItem widgetItem : list)
        {
            map.put(widgetItem.getId(), widgetItem.getName());
        }
        return map;
    }

    /**
     * Checks if there are two widgets with the same name, in which case, it
     * throws a {@link DataServiceSaveException}.
     * 
     * @param region {@link PSRegionTree} object representing the root region of
     *            the template.
     * @throws DataServiceSaveException in case more than one widget have the
     *             same name.
     */
    private void checkDuplicatedNames(PSRegionTree region) throws DataServiceSaveException
    {
        List<String> widgetNames = new ArrayList<>();
        for(PSRegionWidgets regionWidget : region.getRegionWidgetAssociations())
        {
            for(PSWidgetItem widgetItem : regionWidget.getWidgetItems())
            {
                if (!isBlank(widgetItem.getName()) && widgetNames.contains(widgetItem.getName()))
                {
                    throw new DataServiceSaveException("Widget name '" + widgetItem.getName()
                            + "' is already in use. Please use another name.");
                }
                else
                {
                    // add the name for further check
                    widgetNames.add(widgetItem.getName());
                }
            }
        }
    }


    /**
     * Update object with Metadata
     */
    private void updateMetaData(PSTemplate object) {
        PSHtmlMetadata metadata = loadHtmlMetadata(object.getId());
        if (object.getAdditionalHeadContent() == null)
        {
            object.setAdditionalHeadContent(metadata.getAdditionalHeadContent());
        }
        if (object.getAfterBodyStartContent() == null)
        {
            object.setAfterBodyStartContent(metadata.getAfterBodyStartContent());
        }
        if (object.getBeforeBodyCloseContent() == null)
        {
            object.setBeforeBodyCloseContent(metadata.getBeforeBodyCloseContent());
        }
        if (object.getProtectedRegion() == null)
        {
            object.setProtectedRegion(metadata.getProtectedRegion());
        }
        if (object.getProtectedRegionText() == null)
        {
            object.setProtectedRegionText(metadata.getProtectedRegionText());
        } 
        if (object.getDocType() == null)
        {
            object.setDocType(metadata.getDocType());
        }
    }
    
    public boolean isAssociatedToPages(String templateId)
    {
        rejectIfBlank("isAssociatedToPages", "templateId", templateId);

        return !pageDaoHelper.findPageIdsByTemplateInRecentRevision(templateId).isEmpty();
    }
    
    private void updateBodyMarkupOrRegionTree(PSTemplate object) {
        /*
         * Do we have the body markup but not the tree?
         */
        if (isNotBlank(object.getBodyMarkup())
                && (object.getRegionTree() == null || object.getRegionTree().getRootRegion() == null))
        {
            /*
             * Then lets create the tree from the markup.
             */
            log.debug("Creating the region tree from markup.");
            Map<String, PSRegion> regions = new HashMap<>();

            PSTemplateRegionParser parser = new PSTemplateRegionParser(regions);
            PSParsedRegionTree<PSRegion, PSRegionCode> pt = parser.parse(object.getBodyMarkup());

            PSRegionTree tree = object.getRegionTree();
            if (tree == null)
            {
                tree = new PSRegionTree();
            }
            tree.setRootRegion(pt.getRootNode());
            object.setRegionTree(tree);
        }
        /*
         * Do we have the tree?
         */
        else if (object.getRegionTree() != null && object.getRegionTree().getRootRegion() != null)
        {
            /*
             * Then lets create the markup from the tree.
             */
            log.debug("Creating markup from tree");
            
            String markup = PSRegionTreeUtils.treeToString(object.getRegionTree().getRootRegion());
            object.setBodyMarkup(markup);

        }
    }

    /*
     * see base interface method for details
     */
    public PSHtmlMetadata loadHtmlMetadata(String id)
    {
        PSHtmlMetadata metadata = new PSHtmlMetadata();
        PSTemplate t = load(id);
        metadata.setId(id);
        PSHtmlMetadataUtils.copy(t, metadata);
        
        return metadata;
    }
    
    /*
     * see base interface method for details
     */
    public void saveHtmlMetadata(PSHtmlMetadata metadata)
    {
        PSTemplate t = load(metadata.getId());
        PSHtmlMetadataUtils.copy(metadata, t);
        save(t);
    }
    
    public PSValidationErrors validate(PSTemplate object)
    {
        PSBeanValidationException e = PSBeanValidationUtils.validate(object);
        regionWidgetAssocationsValidator.validate(object, e);
        e.throwIfInvalid();
        
        PSValidationErrors errors = e.getValidationErrors();
        
        if (object.getRegionTree() != null && object.getRegionTree().getRegionWidgetAssociations() != null)
        {
            checkDuplicatedNames(object.getRegionTree());
        }

        return errors;
    }
    
    
    
    private PSRegionWidgetAssociationsValidator<PSTemplate> regionWidgetAssocationsValidator;
        
    public static class RegionWidgetValidator  extends PSRegionWidgetAssociationsValidator<PSTemplate> {

        public RegionWidgetValidator(IPSWidgetService widgetService)
        {
            super(widgetService);
        }

        @Override
        public String getField()
        {
            return "regionTree";
        }

        @Override
        public PSRegionWidgetAssociations getWidgetAssocations(PSTemplate wa,
                PSBeanValidationException e)
        {
            return wa.getRegionTree();
        }
    
    }
    

    public boolean isValidatingDeleteTemplate()
    {
        return validatingDeleteTemplate;
    }

    public void setValidatingDeleteTemplate(boolean validatingDeleteTemplate)
    {
        this.validatingDeleteTemplate = validatingDeleteTemplate;
    }
    
    
    /**
     * Used to sort templates by name with case insensitive order.
     * @author adamgent
     *
     */
    public abstract static class PSAbstractTemplateSorter implements Comparator<PSTemplateSummary>
    {
        private Collator collator = Collator.getInstance();
        public int compare(PSTemplateSummary t1, PSTemplateSummary t2)
        {
            String name1 = getName(t1);
            String name2 = getName(t2);
            return collator.compare(name1, name2);
        }
        
        /**
         * Override to get the name from the template for sorting.
         * @param t never <code>null</code>.
         * @return never <code>null</code>.
         */
        protected abstract String getName(PSTemplateSummary t);
        
        /**
         * Returns a new sorted list.
         * <strong>Changes to the new list will NOT change the inputted list</strong>
         * @param <T> type of template summary.
         * @param items never <code>null</code> and no <code>null</code> elements.
         * @return never <code>null</code>.
         */
        public <T extends PSTemplateSummary> List<T> sort(List<T> items) {
            notNull(items);
            noNullElements(items);
            ArrayList<T> sorted = new ArrayList<>(items);
            Collections.sort(sorted, this);
            return sorted;
        }
        
    }
    
    
    /**
     * 
     * Sorts readonly and user templates.
     * 
     * @author adamgent
     * @see #getName(PSTemplateSummary)
     */
    public static class PSTemplateSorter extends PSAbstractTemplateSorter {
        
        /**
         * {@inheritDoc}
         * We use the string of characters after the last <code>.</code> for
         * readonly templates.
         * This will remove our default prefix of:
         * <pre>
         * perc.base
         * </pre>
         * from readonly templates.
         * So a read only template named:
         * <pre>
         * perc.base.Box
         * </pre>
         * Will have a name: <code>Box</code> for sorting.
         * Non-readonly templates (user templates) are sorted 
         * by there unalterted name.
         */
        @Override
        protected String getName(PSTemplateSummary t) {
            String name = t.getName();
            if (t.isReadOnly()) {
                String shortName = substringAfterLast(name, ".");
                name = isBlank(shortName) ? name : shortName;
            }
            notEmpty(name);
            return name;
        }
    }

    /**
    * 
    * Export the selected template
    * 
    */
    public PSTemplate exportTemplate(String id, String name) {
        rejectIfBlank("exportTemplate", "id", id);
        //Create the template to return
        PSTemplate templateSelected = templateDao.generateTemplateToExport(id, name);
        return templateSelected;
    }
    
    /**
     * 
     * Import the selected template
     * 
     */
    public PSTemplate importTemplate(PSTemplate template, String siteId)
            throws PSBeanValidationException,
            com.percussion.share.service.IPSDataService.DataServiceSaveException {
        notNull(template, "template");
        rejectIfBlank("importTemplate", "siteId", siteId);
        
        log.debug("Importing template");
        
        Set<PSRegionWidgets> validRegionWidgets = cleanRegionWidgets(template);
        template.getRegionTree().setRegionWidgetAssociations(validRegionWidgets);
        validate(template);
        updateBodyMarkupOrRegionTree(template);
        PSTemplate savedTemplate = templateDao.generateTemplateFromSource(template, siteId);        
        String id = savedTemplate.getId();
        // remove deleted widgets
        widgetAssetRelationshipService.removeAssetWidgetRelationships(id, savedTemplate.getWidgets());
        
        // transition shared assets to Pending
        workflowHelper.transitionToPending(widgetAssetRelationshipService.getSharedAssets(id));        
        
        return savedTemplate;
    }
    
    private Set<PSRegionWidgets> cleanRegionWidgets(PSTemplate template) {
        PSRegionTree tree = template.getRegionTree();
        Collection<PSRegionWidgets> regionWidgetsToValidate = tree.getRegionWidgetAssociations();
        Set<PSRegionWidgets> sets = new HashSet<>();
        if (regionWidgetsToValidate == null) return null;
        //Get the list of widgets in the system
        List<PSWidgetDefinition> fulls = widgetDao.findAll();
        //Validate the widgets included in the template and just consider the valid widgets
        for(PSRegionWidgets w: regionWidgetsToValidate) {
            List<PSWidgetItem> widgetValidItems = new ArrayList<>();
            List<PSWidgetItem> widgetItems = w.getWidgetItems();
            if (widgetItems != null) {
                 for(PSWidgetItem item : widgetItems) {
                     for(PSWidgetDefinition widgetDef: fulls) {
                         if (item.getDefinitionId().equalsIgnoreCase(widgetDef.getId())) {
                             widgetValidItems.add(item);
                         }
                     }
                 }
            }
            if (widgetValidItems.size()>0){
                w.setWidgetItems(widgetValidItems);
                sets.add(w);
            }
        }      
        return sets;
    }

    /**
     * It creates a new template with specified templateName for the site with
     * siteId, using baseTemplateName as the base template.
     * 
     * @param baseTemplateName The name of the template from which to create the
     *            new one. Example: "perc.base.plain" for plain template in base
     *            package.
     * @param templateName The name to use for naming the template, will be
     *            displayed in the UI.
     * @param siteId The id of the site to which the template is going to
     *            belong. Notice this is not the legacy long id, it's the String
     *            id.
     * @return PSTemplateSummary Class that holds information of the created
     *         template.
     * 
     */
    public PSTemplateSummary createNewTemplate(String baseTemplateName, String templateName, String siteId)
            throws PSAssemblyException
    {
        IPSAssemblyTemplate baseTemplate = assemblyService.findTemplateByName(baseTemplateName);
        PSTemplateSummary templateSummary = this.createTemplate(templateName,
                idMapper.getString(baseTemplate.getGUID()), siteId);

        return templateSummary;
    }
    
    
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSTemplateService.class);


}
