/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.soln.landingpage;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSAaRelationshipList;
import com.percussion.cms.objectstore.PSActiveAssemblyProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderProperty;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.fastforward.managednav.PSNavAbstractEffect;
import com.percussion.fastforward.managednav.PSNavException;
import com.percussion.fastforward.managednav.PSNavFolderUtils;
import com.percussion.fastforward.managednav.PSNavProxyFactory;
import com.percussion.fastforward.managednav.PSNavRelationshipInfo;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffectResult;
import com.percussion.relationship.annotation.PSEffectContext;
import com.percussion.relationship.annotation.PSHandlesEffectContext;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.cache.PSCacheProxy;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
import com.percussion.services.contentmgr.IPSContentTypeMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.assembly.IPSAssemblyWs;
import com.percussion.webservices.assembly.PSAssemblyWsLocator;
import com.percussion.webservices.assembly.data.PSAssemblyTemplateWs;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.Validate.noNullElements;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Creates a landing page relationship with the navon and
 * a current matching item in the folder.
 * <p>
 * The item will only be processed if the folder properties
 * {@value #FOLDER_PROPERTY_LANDINGPAGE_CONTENTTYPE} and
 * {@value #FOLDER_PROPERTY_LANDINGPAGE_TEMPLATE} are set on the
 * current folder or an ancestor folder.
 * 
 * 
 * @author adamgent
 *
 */
@PSHandlesEffectContext(required={PSEffectContext.PRE_CONSTRUCTION})
public class LandingPageFolderEffect extends PSNavAbstractEffect implements IPSEffect  {

    public static final String FOLDER_PROPERTY_LANDINGPAGE_TEMPLATE = "soln.landingpage.template";
    public static final String FOLDER_PROPERTY_LANDINGPAGE_CONTENTTYPE = "soln.landingpage.contenttype";
    private static final Logger log = LogManager.getLogger(LandingPageFolderEffect.class);

    @Override
    public void attempt(Object[] params, IPSRequestContext req, IPSExecutionContext excontext, PSEffectResult result)
            throws PSExtensionProcessingException, PSParameterMismatchException {
        try {
            if (isExclusive(req)) {
                log.debug("ATTEMPT = exclusion flag detected");
                result.setSuccess();
                return;
            }

            PSRelationship currRel = excontext.getCurrentRelationship();
            PSNavRelationshipInfo currentInfo;
            try {
                currentInfo = new PSNavRelationshipInfo(currRel, req);
            } catch (Exception ex) {
                log.warn("Unable to load relationship info rid is {}. Error: {}",
                        currRel.getId(),
                        PSExceptionUtils.getMessageForLog(ex));
                result.setSuccess();
                return;
            }

            String operation = String.valueOf(excontext.getContextType());
            log.debug("Attempt Current {}\n Operation {}",
                    currentInfo,
                    operation);
            result.setSuccess();
            if (excontext.isPreConstruction()) {
                handleAttemptNew(req, currentInfo, result);
            }
        } 
        catch (Exception ex) {
            log.error("Exception in landing page folder effect", ex);
            result.setError(new PSExtensionProcessingException(this.getClass().getName(), ex));
        }
    }

    /**
     * Handles the <code>attempt</code> method when a new item has been created.
     * 
     * @param req
     *            the parent request
     * @param currentInfo
     *            information about the relationship that caused this event.
     * @param result
     *            the result block to return to the caller.
     * @throws PSNavException
     *             when an error occurs.
     */
    private void handleAttemptNew(IPSRequestContext req, PSNavRelationshipInfo currentInfo, PSEffectResult result)
            throws PSNavException {

        log.debug("Entering handleAttemptNew....");

        LandingPage lp = getPossibleLandingPage(req, currentInfo);

        if (lp != null) {
            log.debug("content type is potential landing page");
            handleAttemptNewLandingPage(req, result, lp);
        }
        else {
            log.debug("ignore this event, not Navon, NavTree or potential landing page");
            result.setSuccess();
        }

        log.debug("Exiting handleAttemptNew....");

    }
    
    
    /**
     * Creates landing page data object.
     * 
     * @param request never <code>null</code>.
     * @param currentInfo never <code>null</code>.
     * @return <code>null</code> indicates a landing page should not be associated.
     */
    private LandingPage getPossibleLandingPage(IPSRequestContext request, PSNavRelationshipInfo currentInfo) throws PSNavException {
        PSComponentSummary item = currentInfo.getDependent();
        long depType = item.getContentTypeId();
        PSNavConfig navConfig = PSNavConfig.getInstance();
        int dependType = (int) depType;
        boolean shouldProcess = 
            item.isItem() 
            && !navConfig.getNavonTypeIds().contains(Long.parseLong(String.valueOf(dependType)))
            && !navConfig.getNavTreeTypeIds().contains(Long.parseLong(String.valueOf(dependType)));
        if ( ! shouldProcess ) return null;
        String contentTypeName = contentTypeName(item);
        notNull(contentTypeName, "Programming error content type name is null");
        
        PSComponentSummary folder = currentInfo.getOwner();
        PSComponentSummary navon = PSNavFolderUtils.getChildNavonSummary(request, folder);
        
        if (navon == null) {
            log.debug("folder has no Navon");
            return null;
        }
        
        if(log.isDebugEnabled()) {
            log.debug("Finding landing page template for: item: {}, folder: {}, contentTypeName: {}",
                            item.getCurrentLocator(), folder.getLocator(), contentTypeName);
        }
        String templateName = getTemplateName(item, folder, navon, contentTypeName);
        
        if (templateName == null) {
            log.debug("No landing page template found for contentTypeName: {}" , contentTypeName);
            return null;
        }
        
        return new LandingPage(item, folder, navon, contentTypeName,templateName);
    }
    
    protected String getTemplateName(
            PSComponentSummary item, 
            PSComponentSummary folder,
            PSComponentSummary navon,
            String contentTypeName)  {
        try {
            Map<String, String> fp = folderProperties(folder);
            if(log.isDebugEnabled()) {
                log.debug("Got folder properties: {}", fp);
            }
            String contentType = fp.get(FOLDER_PROPERTY_LANDINGPAGE_CONTENTTYPE);
            if (contentTypeName.equalsIgnoreCase(contentType)) {
                return fp.get(FOLDER_PROPERTY_LANDINGPAGE_TEMPLATE);
            }
        } 
        catch (PSErrorResultsException e) {
            log.error("Failed to get folder properties for folder: {} Error: {}",
                    folder,
                    PSExceptionUtils.getMessageForLog(e));
        }
        return null;
        
    }
    
    protected Map<String, String> folderProperties(PSComponentSummary folder) throws PSErrorResultsException {
        Map<String, String> props = new HashMap<>();
        IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();
        IPSGuidManager guidManager = PSGuidManagerLocator.getGuidMgr();
        IPSGuid guid = guidManager.makeGuid(folder.getCurrentLocator());
        String path = contentWs.loadFolders(singletonList(guid)).get(0).getFolderPath();
        String[] names = path.replace("//", "").split("/");
        List<String> paths = new ArrayList<>();
        String current = "/";
        for (String name : names) {
            current = current + "/" + name;
            paths.add(current);
        }
        List<PSFolder> folders = contentWs.loadFolders(paths.toArray(new String[0]));
        for (PSFolder f : folders) {

            Iterator<?> it = f.getProperties();
            while (it.hasNext()) {
                PSFolderProperty prop = (PSFolderProperty) it.next();
                props.put(prop.getName(), prop.getValue());
            }
        }
        return props;
    }



    /**
     * Called when the new item being inserted is a potential new landing page.
     * 
     * @param req
     *            the parent request context
       * @param result
     *            the result block to return to the caller.
     * @throws PSNavException
     *             when an error occurs.
     */
    private void handleAttemptNewLandingPage(
            IPSRequestContext req,
            PSEffectResult result, LandingPage lp) throws PSNavException {

        
        if (log.isDebugEnabled()) {
            log.debug("Entering handleAttemptNewLandingPage, {}" , lp);
        }

        PSLocator navonLoc = lp.getNavon().getCurrentLocator();

        try {
            PSNavProxyFactory pf = PSNavProxyFactory.getInstance(req);
            PSComponentSummaries resultSet = findLandingPages(pf, navonLoc);
            log.debug( "{} landing pages found",resultSet.size() );
            if (resultSet.size() == 0) {
                /*
                 * No existing landing page found.
                 */
                log.debug("no slot content found");
                createLandingPageRelationship(pf, 
                        navonLoc, lp.getItem().getCurrentLocator(), 
                        lp.getTemplate(), lp.getContentType());
                log.debug("item added to nav_landing_page slot");

            }
            else {
                log.debug("Navon : {} already has a landing page.", navonLoc);
            }

        } 
        catch (PSCmsException ce) {
            throw new PSNavException(PSNavFolderUtils.class.getName(), ce);
        }

        flushAll();

        log.debug("Exiting handleAttemptNewLandingPage....");
        result.setSuccess();
    }

    private void createLandingPageRelationship(PSNavProxyFactory pf, PSLocator navonLoc, PSLocator itemLoc,
            String templateName, String contentTypeName) throws PSNavException, PSCmsException {
        IPSAssemblyTemplate assemblyTemplate = findTemplate(templateName, contentTypeName);
        if (assemblyTemplate == null)
            throw new PSNavException("Could not find assembly template: " + templateName);
        IPSTemplateSlot landingPageSlot = landingPageSlot();

        // Create the relationship to add the new item to the navon into
        // the appropriate slot using the right template
        PSAaRelationship aaRel = new PSAaRelationship(navonLoc, itemLoc, landingPageSlot,
                assemblyTemplate);
        PSActiveAssemblyProcessorProxy aaProxy = pf.getAaProxy();
        PSAaRelationshipList aaList = new PSAaRelationshipList();
        aaList.add(aaRel);
        log.debug("aaList: {}" , aaList);

        // Add the relationship to the slot
        aaProxy.addSlotRelationships(aaList, -1);
    }

    private PSComponentSummaries findLandingPages(PSNavProxyFactory pf, PSLocator navonLoc) throws PSCmsException {
        /* check if Navon nav_landing_page slot is populated */
        PSRelationshipProcessor relProxy = pf.getRelProxy();

        log.debug("setup the RelationshipFilter");

        PSRelationshipFilter filter = new PSRelationshipFilter();
        filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
        filter.setOwner(navonLoc);
        // standard nav_landing_page slotid - there is a better way to lok this
        // up
        filter.setProperty("sys_slotid", "510");
        filter.setCommunityFiltering(false);
        return relProxy.getSummaries(filter, false);
    }

    private IPSTemplateSlot landingPageSlot() throws PSNavException {
        IPSTemplateService ts = PSAssemblyServiceLocator.getAssemblyService();

        IPSTemplateSlot landingPageSlot;
        try {
            landingPageSlot = ts.findSlotByName("rffNavLandingPage");
            log.debug("got slot with name: {}" , landingPageSlot.getName());
        } catch (PSAssemblyException e) {
            String error = "The slot - rffNavLandingPage was not found: ";
            log.warn(error,e);
            throw new PSNavException(error, e);
        }
        return landingPageSlot;
    }

    private IPSAssemblyTemplate findTemplate(String templateName, String contentTypeName) {
        // Load the templates and select the IPSAssemblyTemplate with
        // templateName
        IPSAssemblyWs aws = PSAssemblyWsLocator.getAssemblyWebservice();
        log.debug("loading template with templatename: {} - contenttypeName: {}",
                templateName,
                contentTypeName);
        List<PSAssemblyTemplateWs> allTemplates = aws.loadAssemblyTemplates(templateName, contentTypeName);
        if (allTemplates.isEmpty()) return null;
        IPSAssemblyTemplate assemblyTemplate = allTemplates.get(0).getTemplate();
        log.debug("got assembly template: {}" , assemblyTemplate.getName());
        return assemblyTemplate;
    }
    
    private String contentTypeName(PSComponentSummary dependent) {
        String contentTypeName = "";
        IPSGuid guid = dependent.getContentTypeGUID();
        List<IPSGuid> ctList = Collections.singletonList(guid);
        log.debug("the guid of the content type of the item being created is: {}" , guid);
        
        try {
            IPSContentTypeMgr ctmgr = PSContentMgrLocator.getContentMgr();
            log.debug("created content type manager...");
            List<IPSNodeDefinition> ctNodes = ctmgr.loadNodeDefinitions(ctList);
            log.debug("loaded the node definitions for cts...");
            for (int i = 0; i < ctNodes.size(); i++) {
                IPSNodeDefinition contentTypeNode = ctNodes.get(i);

                log.debug("iteration {} through the CT nodes: ctInternalName: {} -- ctName: {} -- ct:Label: {}",
                        i,
                        contentTypeNode.getInternalName() ,
                        contentTypeNode.getName(),
                        contentTypeNode.getLabel());

                if (contentTypeNode.getGUID().longValue() == guid.longValue()) {
                    contentTypeName = contentTypeNode.getInternalName();
                    log.debug("content type with id ({})'s name is: {}",
                            guid.getUUID() ,
                            contentTypeName);
                }
            }
        } 
        catch (RepositoryException cex) {
            log.debug("Exception while accessing repository to load Node definitions... error: {}",
                    PSExceptionUtils.getMessageForLog(cex));
        }
        return contentTypeName;
    }

    /**
     * Flushes the assembly cache. This method is here primarily to encapsulate
     * any exceptions thrown from the <code>PSCacheProxy</code>.
     * 
     * @throws PSNavException
     */
    private void flushAll() throws PSNavException {
        try {
            PSCacheProxy.flushAssemblers(null, null, null, null);
        } 
        catch (Exception ex) {
            throw new PSNavException(ex);
        }
        log.debug("cache flushed");
    }
    
    
    protected static class LandingPage {
        private final String template;
        private final String contentType;
        private final PSComponentSummary item;
        private final PSComponentSummary folder;
        private final PSComponentSummary navon;

        
        public LandingPage(PSComponentSummary item, PSComponentSummary folder, PSComponentSummary navon,
                String contentType, String template) {
            super();
            noNullElements(asList(item,folder,navon,template,contentType), 
                    "Programming error: Landing page was setup incorrectly");
            this.item = item;
            this.folder = folder;
            this.navon = navon;
            this.template = template;
            this.contentType = contentType;
        }

        public String getTemplate() {
            return template;
        }

        public String getContentType() {
            return contentType;
        }
        
        public PSComponentSummary getItem() {
            return item;
        }

        
        public PSComponentSummary getFolder() {
            return folder;
        }

        
        public PSComponentSummary getNavon() {
            return navon;
        }
        
        @Override
        public String toString() {
            return format("Entering handleAttemptNewLandingPage, " +
                    "item: {0}, folder: {1}, templateName: {2}, contentTypeName: {3}", 
                    item.getCurrentLocator(), folder.getLocator(), template, contentType);
        }
        
        
    }

	@Override
	public void test(Object[] arg0, IPSRequestContext arg1, IPSExecutionContext arg2, PSEffectResult arg3)
			 {
		if(log.isDebugEnabled())
			log.debug("No operation in IPSEffect.test");
	}

}
