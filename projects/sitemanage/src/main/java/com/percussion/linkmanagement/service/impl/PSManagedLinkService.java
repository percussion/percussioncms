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
package com.percussion.linkmanagement.service.impl;

import com.percussion.cms.PSSingleValueBuilder;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSExceptionUtils;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.linkmanagement.service.IPSManagedLinkService;
import com.percussion.pagemanagement.assembler.PSRenderAsset;
import com.percussion.pagemanagement.assembler.impl.PSAssemblyItemBridge;
import com.percussion.pagemanagement.data.PSRenderLinkContext;
import com.percussion.pagemanagement.data.PSRenderLinkContext.Mode;
import com.percussion.pagemanagement.service.IPSPageCatalogService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSRenderLinkService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.services.assembly.impl.AssemblerInfoUtils;
import com.percussion.services.assembly.impl.PSReplacementFilter;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrConfig;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.PSContentMgrOption;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.linkmanagement.IPSManagedLinkDao;
import com.percussion.services.linkmanagement.data.PSManagedLink;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSLinkableItem;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.utils.PSJsoupPreserver;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;
import com.phloc.commons.url.URLValidator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.percussion.share.dao.PSFolderPathUtils.concatPath;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * @author JaySeletz
 *
 */
@Component("managedLinkService")
@Lazy
public class PSManagedLinkService implements IPSManagedLinkService
{
    
    /**
     * ID used to create links for new parents, public only for unit test access
     */
    public static final int UNASSIGNED_PARENT_ID = -1;

	private static final Logger log = LogManager.getLogger(PSManagedLinkService.class);

    private IPSManagedLinkDao dao;

    private IPSIdMapper idMapper;

    private IPSContentWs contentWs;

    private IPSGuidManager guidMgr;

    private PSAssemblyItemBridge assemblyItemBridge;
    
    private IPSRenderLinkService renderLinkService;

    private IPSWorkflowHelper workflowHelper;
    
    private IPSPageCatalogService pageCatalogService;
    
    private ThreadLocal<List<Long>> newLinkIds = new ThreadLocal<>();
    
    @Autowired
    public PSManagedLinkService(IPSManagedLinkDao dao, IPSIdMapper idMapper, IPSContentWs contentWs,
            IPSWorkflowHelper workflowHelper, IPSNotificationService notificationService, IPSGuidManager guidMgr, IPSPageService pageService, 
            PSAssemblyItemBridge assemblyItemBridge, IPSRenderLinkService renderLinkService, IPSPageCatalogService pageCatalogService)
    {
        this.dao = dao;
        this.idMapper = idMapper;
        this.contentWs = contentWs;
        this.workflowHelper = workflowHelper;
        this.guidMgr = guidMgr;
        this.assemblyItemBridge = assemblyItemBridge;
        this.renderLinkService = renderLinkService;
        this.pageCatalogService = pageCatalogService;

        notificationService.addListener(EventType.ASSET_DELETED, new IPSNotificationListener()
        {
            @Override
            public void notifyEvent(PSNotificationEvent event)
            {
                itemDeleted((String) event.getTarget());
            }
        });
        
        notificationService.addListener(EventType.PAGE_DELETE, new IPSNotificationListener()
        {
            @Override
            public void notifyEvent(PSNotificationEvent event)
            {
                itemDeleted((String) event.getTarget());
            }
        });
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.linkmanagement.service.IPSManagedLinkService#unmanageImageLinks(java.lang.String)
     */
    @Override
    public String unmanageImageLinks(String source)
    {
        notNull(source);
        String result;
        Document doc = Jsoup.parseBodyFragment(source);
        final Elements imageLinks = doc.select(IMG_SRC);
        
        for (Element link : imageLinks)
        {
            unmanageImageLink(link);
        }
        
        result = doc.body().html();
        return result;
    }
    
    @Override
    public String manageLinks(String parentId, String source)
    {
        String returnHTML = source;
        if (doManageAll() || source.contains(PERC_MANAGED_ATTR)){
            returnHTML = PSJsoupPreserver.formatPreserveTagsForJSoupParse(returnHTML);
            notEmpty(parentId);
            notNull(returnHTML); 
    
            String result;
    
            // parse the source
            Document doc = Jsoup.parseBodyFragment(returnHTML);
    
            // walk all anchor tags
            final Elements links = doc.select(A_HREF);
            
            for (Element link : links)
            {
                manageLink(parentId, link);
            }
    
            // walk the image tags
            final Elements imageLinks = doc.select(IMG_SRC);
            
            for (Element link : imageLinks)
            {
                manageImageLink(parentId, link);
            }
            
            // serialize to the result
            doc.outputSettings().prettyPrint(false);
            result = doc.body().html();
            returnHTML =  result;
        }
        returnHTML = PSJsoupPreserver.formatPreserveTagsForOutput(returnHTML);
        return returnHTML;
    }
    
    @Override
    public String renderLinks(PSRenderLinkContext linkContext, String source, Boolean isStaging, Integer parentId)
    {
        String returnHTML = source;
        ArrayList<Element> managedLinks = new ArrayList<>();
        
        if(parentId == null || parentId == 0){
            log.warn("Rendering managed links for parent item with an invalid contentid");
        }
        if (doManageAll() || source.contains(PERC_MANAGED_ATTR)){
            returnHTML = PSJsoupPreserver.formatPreserveTagsForJSoupParse(returnHTML);
            Validate.notNull(returnHTML);
    
            String result;
    
            // parse the source
            Document doc = Jsoup.parseBodyFragment(returnHTML);
            
            // Cleanup from previous patch
            doc.getElementsByClass(PSSingleValueBuilder.PERC_BROKENLINK).removeClass(PSSingleValueBuilder.PERC_BROKENLINK);
            
            // walk all anchor tags
            final Elements links = doc.select(A_HREF);
            boolean isManaged = false;
            for (Element link : links)
            {
                isManaged = renderLink(linkContext, link, isStaging);
        
                if(isManaged)
                    managedLinks.add(link);
            }
            
            final Elements imageLinks = doc.select(IMG_SRC);
            for (Element link : imageLinks)
            {
               isManaged = renderImageLink(linkContext, link, isStaging);
               
               if(isManaged)
                  managedLinks.add(link);
            }        
      
            //Get rid of any managed links that were removed from the content by the end user. 
            boolean isDeliveryContext = (linkContext != null && linkContext.getMode().equals(
                    PSRenderLinkContext.Mode.PUBLISH));
            if (!isDeliveryContext) {
                cleanupDeletedLinks(managedLinks, parentId);
            }
            
            // serialize to the result
            result = doc.body().html();
    
            returnHTML =  result;
        }
        returnHTML = PSJsoupPreserver.formatPreserveTagsForOutput(returnHTML);
        return returnHTML;
    }
    
	@Override
	public String renderLinksInJSON(PSRenderLinkContext linkContext, String jsonPayload, Boolean isStaging) {
		  JSONObject object = null;
          JSONArray objectArray = new JSONArray();
	     
	        try {
	        	 if(log.isDebugEnabled())
	             	log.debug("Parsing JSON Payload" + jsonPayload);
	        	object = new JSONObject(jsonPayload);

	        	log.debug("Done parsing payload, parsing " + PERC_CONFIG + " array.");

                try {
                    objectArray = object.getJSONArray(PERC_CONFIG);
		            log.debug("Done parsing payload array");
                }catch(JSONException js){
                    //Unable to get the array so log an error that it is missing
                    log.error("An error occurred while trying to manage links in a JSONPayload field.", PSExceptionUtils.getMessageForLog(js));
                    log.debug("An error occurred while trying to manage links in a JSONPayload field.", js);
                    return null;

                }


                String newPath = "";
		        
		        for (int i = 0; i < objectArray.length(); i++) {
		        	PSManagedLink mLink = null;
                    log.debug("Processing payload entry " + i);
                    JSONObject entry = objectArray.getJSONObject(i);


		            //Images
		            if(entry.has(PERC_IMAGEPATH)){
		            	if(entry.has(PERC_IMAGEPATH_LINKID)){
		            		if(!StringUtils.isBlank(entry.getString(PERC_IMAGEPATH_LINKID))){
		            			if(log.isDebugEnabled())
		            				log.debug("Getting updated path for Image entry: " + entry.getString(PERC_IMAGEPATH_LINKID) + 
		            						" with current path of " + entry.get(PERC_IMAGEPATH));
		            			mLink = dao.findLinkByLinkId(Integer.parseInt(entry.getString(PERC_IMAGEPATH_LINKID)));
		            			newPath = renderHref(mLink,linkContext, isStaging);
		            			if(log.isDebugEnabled())
		            				log.debug("Updating payload for Image entry: " + entry.getString(PERC_IMAGEPATH_LINKID) + " with new path of " + newPath);
		            			
		            			entry.put(PERC_IMAGEPATH, newPath);
			            		objectArray.put(i,entry);
			            		if(log.isDebugEnabled())
			            			log.debug("Done updating.");
		            		}
		            	}
		            }
		            
		            //Files
		            if(entry.has(PERC_FILEPATH)){
		            	if(entry.has(PERC_FILEPATH_LINKID)){
		            		if(!StringUtils.isBlank(entry.getString(PERC_FILEPATH_LINKID))){
		            			if(log.isDebugEnabled())
		            				log.debug("Getting updated path for File entry: " + entry.getString(PERC_FILEPATH_LINKID) + 
		            						" with current path of " + entry.get(PERC_FILEPATH));
		            			mLink = dao.findLinkByLinkId(Integer.parseInt(entry.getString(PERC_FILEPATH_LINKID)));
		            			newPath = renderHref(mLink,linkContext, isStaging);
			            		if(log.isDebugEnabled())
		            				log.debug("Updating payload for File entry: " + entry.getString(PERC_FILEPATH_LINKID) + " with new path of " + newPath);
		            			
			              		entry.put(PERC_FILEPATH, newPath);
			            		objectArray.put(i,entry);
			            		if(log.isDebugEnabled())
			            			log.debug("Done updating.");
		            		}
		            	}
		            }
		            
		            //Pages
		            if(entry.has(PERC_PAGEPATH)){
		            	if(entry.has(PERC_PAGEPATH_LINKID)){
		            		if(!StringUtils.isBlank(entry.getString(PERC_PAGEPATH_LINKID))){
		            			if(log.isDebugEnabled())
		            				log.debug("Getting updated path for Page entry: " + entry.getString(PERC_PAGEPATH_LINKID) + 
		            						" with current path of " + entry.get(PERC_PAGEPATH));
		            			mLink = dao.findLinkByLinkId(Integer.parseInt(entry.getString(PERC_PAGEPATH_LINKID)));
		            			newPath = renderHref(mLink,linkContext, isStaging);
			            		if(log.isDebugEnabled())
		            				log.debug("Updating payload for Page entry: " + entry.getString(PERC_PAGEPATH_LINKID) + " with new path of " + newPath);
			            		entry.put(PERC_PAGEPATH, newPath);
			            		objectArray.put(i,entry);
			            		if(log.isDebugEnabled())
			            			log.debug("Done updating.");
		            		}
		            	}
		            }
		        }

		        log.debug("Updating return payload.");
		        object.put(PERC_CONFIG, objectArray);
		        log.debug("Done updating.");
			} catch (JSONException ex) {
                if(jsonPayload == null) {
                    jsonPayload = "";
                }

	            log.error("An error occurred while trying to manage links in a JSONPayload field. Payload was: " + jsonPayload + " Error was: " + ex.getMessage());

				log.debug("Error occurred.  Returning original payload: " + jsonPayload, ex);
				return jsonPayload;
			}

	        log.debug("Returning updated payload with any managed path updates: " + object.toString());
	        return object.toString();
	}
    
    private static boolean getLinkState(Element elem, int contentId)
    {
       String flag = PSSingleValueBuilder.getValidFlag(contentId);
       if (flag.equals("u"))
       {
    	   addAttribute(elem,PSSingleValueBuilder.PERC_BROKENLINK);
           return true;
       }
       
       if (!flag.equals("y") && !flag.equals("i"))
       {
    	   addAttribute(elem,PSSingleValueBuilder.PERC_NOTPUBLICLINK);
    	 
       }
       else
       {
    	   removeAttribute(elem,PSSingleValueBuilder.PERC_NOTPUBLICLINK);
       }
       return false;
    }
    
    private static void addAttribute(Element el, String className)
    {
    	String currentValue = StringUtils.defaultString(el.attr("class"));
    	if (!currentValue.contains(className))
    	{
    		currentValue+=" "+className;
    		el.attr("class",currentValue.trim());
    	}
    }
    
    private static void removeAttribute(Element el, String className)
    {
    	String currentValue = StringUtils.defaultString(el.attr("class"));
    	if (currentValue.contains(className))
    	{
    		StringUtils.replace(currentValue, className, "");
    		StringUtils.replace(currentValue, "  ", " ");
    		el.attr("class",currentValue.trim());
    	}
    	
    }

    @Override
    public String renderLinks(PSRenderLinkContext linkContext, String source, Integer parentId) {
    	return renderLinks(linkContext, source, Boolean.FALSE, parentId);
    }
    
    @Override
    public String manageNewItemLinks(String source)
    {
        Validate.notNull(source);
        
        // set up new link id list
        newLinkIds.set(new ArrayList<>());
        return manageLinks(getNewItemParentId(), source);
    }

    @Override
    public void initNewItemLinks()
    {
        // set up new link id list
        newLinkIds.set(new ArrayList<>());
    }
    
    @Override
    public void updateNewItemLinks(String parentId)
    {
        Validate.notNull(parentId);
        List<Long> linkIds = newLinkIds.get();
        if (linkIds == null)
        {
            log.warn("newLInkIds not initialized for current thread, no links will be updated for parent Id: " + parentId);
            return;
        }
        
        // clear the new item link cache
        newLinkIds.set(null);
        
        for (Long linkId : linkIds)
        {
            PSManagedLink link = dao.findLinkByLinkId(linkId);
            if (link != null)
            {
                try
                {
                    PSLocator parentLoc = idMapper.getLocator(parentId);
                    link.setParentId(parentLoc.getId());
                    link.setParentRevision(parentLoc.getRevision());
                    dao.saveLink(link);
                }
                catch (Exception e)
                {
                    log.error("Unable to update manage link: " + link.toString() + " for parent id: " + parentId + " - "+ e.getLocalizedMessage(), e);
                }
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.linkmanagement.service.IPSManagedLinkService#updateCopyAssetsLinks(java.util.Collection, java.lang.String, java.lang.String, java.util.Map)
     */
    public void updateCopyAssetsLinks(Collection<String> assetIds, String origSiteRoot, String copySiteRoot, Map<String, String> assetMap)
    {
        notNull(assetIds);
        notEmpty(origSiteRoot);
        notEmpty(copySiteRoot);
        notNull(assetMap);
        
        Map<Integer, Integer> assetIdMap = convertGuidToIntegerMap(assetMap);
        for (String guid : assetIds)
        {
            int assetId = idMapper.getContentId(guid);
            updateCopyAssetLinks(assetId, origSiteRoot, copySiteRoot, assetIdMap);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.linkmanagement.service.IPSManagedLinkService#getDependent(org.jsoup.nodes.Element)
     */
    @Override
    public int getDependent(Element elem)
    {
        if(elem == null)
            return -1;
        String linkId = elem.attr(PERC_LINKID_ATTR);
        String path = "img".equalsIgnoreCase(elem.tagName())?elem.attr(SRC_ATTR):elem.attr(HREF_ATTR);
        return getDependent(linkId, path);
    }
    
    @Override
    public int getDependent(String linkId, String path)
    {
        int dependent = -1;
        if(StringUtils.isNotBlank(linkId) && StringUtils.isNumeric(linkId))
        {
            PSManagedLink link = dao.findLinkByLinkId(Long.parseLong(linkId));
            if(link != null)
                dependent = link.getChildId();
        }
        if(dependent == -1)
        {
            try
            {
                dependent = getDependentId(path);
            }
            catch (Exception e)
            {
                log.debug("Failed to find the dependent for the href", e);
            }
        }
        
        return dependent;
    }
    private Map<Integer, Integer> convertGuidToIntegerMap(Map<String, String> guidMap)
    {
        Map<Integer, Integer> integerMap = new HashMap<>();
        for (Map.Entry<String, String> entry : guidMap.entrySet())
        {
            int intKey = idMapper.getContentId(entry.getKey());
            int intValue = idMapper.getContentId(entry.getValue());
            integerMap.put(intKey, intValue);
        }
        return integerMap;
    }
    
    private void updateCopyAssetLinks(int assetId, String origSiteRoot, String copySiteRoot, Map<Integer, Integer> assetIdMap)
    {
        List<PSManagedLink> links = dao.findLinksByParentId(assetId);
        for (PSManagedLink link : links)
        {
            try {
                updateCopyAssetLink(link, origSiteRoot, copySiteRoot, assetIdMap);
            } catch (IPSGenericDao.SaveException e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(),e);
                //Continue processing
            }
        }
    }
    
    private void updateCopyAssetLink(PSManagedLink link, String origSiteRoot, String copySiteRoot, Map<Integer, Integer> assetIdMap) throws IPSGenericDao.SaveException {
        // handle a cloned asset
        Integer copiedAssetId = assetIdMap.get(link.getChildId());
        if (copiedAssetId != null)
        {
            link.setChildId(copiedAssetId.intValue());
            dao.saveLink(link);
            return;
        }
        
        // handle a cloned page
        String relativePath = null;
        relativePath = getRelativePath(origSiteRoot, link.getChildId());
        if (relativePath == null)
            return;
        
        String path = copySiteRoot + relativePath;
        IPSGuid guid = contentWs.getIdByPath(path);
        if (guid == null)
        {
            log.warn("Found a page from original site (" + origSiteRoot + relativePath
                    + "), but cannot find the copied page (" + path + "). Skip updating managed link: "
                    + link.toString());
            return;
        }
        int childId = idMapper.getContentId(guid);
        link.setChildId(childId);
        dao.saveLink(link);
    }
    
    private String getRelativePath(String siteRoot, int contentId)
    {
        PSLegacyGuid id = new PSLegacyGuid(contentId);
        String[] paths = contentWs.findItemPaths(id);
        if (paths.length == 0)
            return null;
        
        String path = paths[0];
        if (!path.startsWith(siteRoot))
            return null;
        
        return path.substring(siteRoot.length());
    }
    
    /**
     * Update the href attribute of the supplied link element with the correct rendering based on the supplied context.
     * If the link id of the element is not found, or doen't resolve to a dependent page or asset, the href resolves to "#".
     * 
     * @param linkContext The context to use, may be <code>null</code>.
     * @param link The element to update, assumed not <code>null</code>.
     * 
     * @return <code>true</code> if the link was successfully rendered for the given context, <code>false</code> if not.
     */
    private boolean renderLink(PSRenderLinkContext linkContext, Element link, Boolean isStaging)
    {
        boolean isBrokenLink = false;
        boolean isDeliveryContext = (linkContext != null && linkContext.getMode().equals(
                PSRenderLinkContext.Mode.PUBLISH));

        try
        {
            long linkId = -1;
            PSManagedLink mLink = null;

            linkId = getLinkId(link);
            if (linkId != -1 && linkId != 0)
            {
                mLink = dao.findLinkByLinkId(linkId);
               
            }
            
            String path = StringUtils.defaultString(link.attr(HREF_ATTR));
            if(!isManageableLink(path))    
            {
            	removeManagedAttributes(link);
            	return false;
            }
            
            if (mLink==null)
            {
                mLink = createTempManagedLink(link, path);
            }

            String href=null;
            
            if (mLink != null)
            {
                href = renderHref(mLink, linkContext, isStaging); 
            }
            

            if (href!=null)
                link.attr(HREF_ATTR, href);
            else
            	isBrokenLink=true;
            if (isDeliveryContext)
            {
                removeManagedAttributes(link);
                //In case Link is broken, then needs to be replaced with user preferred value for href
                if(isBrokenLink) {
                    String overrideValue = AssemblerInfoUtils.getBrokenLinkOverrideValue(href);
                    link.attr(HREF_ATTR, overrideValue);
                }
            }
            else
            {
            	addBrokenClasses(link, mLink,isBrokenLink);
            }
        }
        catch (Exception e)
        {
            log.error("Unable to render managed link: " + link.toString() + ": " + e.getLocalizedMessage(), e);
            if (isDeliveryContext)
                isBrokenLink = true;
        }
        
        return !isBrokenLink;
    }

    private boolean isManageableLink(String path)
    {
    	return path.startsWith("/Sites/") || path.startsWith("/Assets/") || path.startsWith("//Sites/") || path.startsWith("//Assets/"); 	
    }
	private void addBrokenClasses(Element link, PSManagedLink mLink,boolean brokenLink) {
		if(mLink==null || brokenLink)
		{
			link.addClass(PSSingleValueBuilder.PERC_BROKENLINK);
			link.removeClass(PSSingleValueBuilder.PERC_NOTPUBLICLINK);
		}
		else {
			int dependentId = mLink.getChildId();
			String flag = PSSingleValueBuilder.getValidFlag(dependentId);
			if (flag.equals("u"))
		      {
		         link.addClass(PSSingleValueBuilder.PERC_BROKENLINK);
		         link.removeClass(PSSingleValueBuilder.PERC_NOTPUBLICLINK);
		      } else       	      
		      if (!flag.equals("y") && !flag.equals("i"))
		      {
			     link.removeClass(PSSingleValueBuilder.PERC_BROKENLINK);
		         link.addClass(PSSingleValueBuilder.PERC_NOTPUBLICLINK);
		      }
		      else
		      {
		    	 link.removeClass(PSSingleValueBuilder.PERC_BROKENLINK);
		         link.removeClass(PSSingleValueBuilder.PERC_NOTPUBLICLINK);
		      }
		      
		}
	}

    /**
     * Update the src attribute of the supplied image link element with the correct rendering based on the supplied context.
     * If the link id of the element is not found, or doen't resolve to a dependent asset, the src resolves to "#".
     * 
     * @param linkContext The context to use, may be <code>null</code>.
     * @param link The element to update, assumed not <code>null</code>.
     * 
     * @return <code>true</code> if the link was successfully rendered for the given context, <code>false</code> if not.
     */
    private boolean renderImageLink(PSRenderLinkContext linkContext, Element link, Boolean isStaging)
    {
        boolean result = true;
        boolean isDeliveryContext = (linkContext != null && linkContext.getMode().equals(
                PSRenderLinkContext.Mode.PUBLISH));

        try
        {
            long linkId = -1;

            linkId = getLinkId(link);

            PSManagedLink mLink = null;

            if (linkId != -1 && linkId !=0)
            {
                mLink = dao.findLinkByLinkId(linkId);
            }
            
            String path = StringUtils.defaultString(link.attr(SRC_ATTR));
            if(!isManageableLink(path))    
            {
            	removeManagedAttributes(link);
            	return false;
            }
            
            if (mLink==null)
            {
                mLink = createTempManagedLink(link, path);
            }


            Node assetNode=null;
            try {
                assetNode = getAssetNode(mLink.getChildId(), linkContext, isStaging, "rx:alttext");
            }catch(Exception e){
                log.debug("Exception getting Asset Node for link:" + link + ": link is likely broken");
            }

            if (assetNode != null) {
                String alt="";
                if(assetNode.hasProperty("rx:alttext")) {
                    alt = assetNode.getProperty("rx:alttext").getString();
                }

                if (StringUtils.isNotBlank(alt)) {
                    link.attr("alt", alt);
                }

                String title = "";
                if(assetNode.hasProperty("rx:displaytitle")){
                    title = assetNode.getProperty("rx:displaytitle").getString();
                }

                if (StringUtils.isNotBlank(alt)) {
                    link.attr("title", title);
                }
            }
            
            String src=null;
            if (mLink != null)
            {
                src = renderHref(mLink, linkContext, isStaging);
            }
            
            if (src!=null)
                link.attr(SRC_ATTR, src);
            else {
                result = false;
            }
            if (isDeliveryContext)
            {
                removeManagedAttributes(link);
                //In case Link is broken, then needs to be replaced with user preferred value for href
                if(result == false) {
                    String overrideValue = AssemblerInfoUtils.getBrokenLinkOverrideValue(src);
                    link.attr(SRC_ATTR, overrideValue);
                }
            }
            else
            {
            	addBrokenClasses(link, mLink,result);
            }
        }
        catch (Exception e)
        {
            log.debug("Unable to render managed link: " + link.toString() + ": " + e.getLocalizedMessage(), e);
            result = false;
        }

        return result;
    }

    private PSManagedLink createTempManagedLink(Element link, String path) throws Exception
    {
        PSManagedLink mLink = null;
        if(path.startsWith("/Sites/") || path.startsWith("/Assets/") || path.startsWith("//Sites/") || path.startsWith("//Assets/"))    
        {
            int dependentId = getDependentId(path);
            if (dependentId != -1)
            {
                log.debug("Fixed link for path "+path + " to id "+dependentId);
                String anchor = PSReplacementFilter.getAnchor(link.attr(HREF_ATTR));
                mLink = dao.createLink(-1, -1, dependentId, anchor);
            } 
            {
                log.debug("Cannot find item for internal path "+path);
            }
        }
        return mLink;
    }
    
    /**
     * Get the corrected href for dependent item specified by the supplied link using the supplied context.
     * 
     * @param link
     * @param linkContext
     * @param isStaging
     * @return The href, may be <code>null</code> if the link or its dependent item cannot be located.
     */
    private String renderHref(PSManagedLink link, PSRenderLinkContext linkContext, Boolean isStaging)
    {
        String href = null;

        try
        {
            if (link != null)
            {
                if (linkContext == null)
                {
                    href = findItemPath(link.getChildId());
                }
                else
                {
                    href = createHref(link, linkContext, isStaging, href);
                }
            }
        }
        catch (Exception e)
        {
            log.debug("Unable to render href for managed link with ID " + link.getLinkId() + ": " + e.getLocalizedMessage(), e);
        }

        return href;
    }

    private String decodeHref(String href) {
        String ret = href;
        try {
            String test = URLDecoder.decode(href, "utf8");
            if(!test.equals(href)) {
                ret = test;
            }
        }
        catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(),e);
        }
        return ret;
    }

    /**
	 * Create the Href from the context and link.
	 * 
	 * @param link Our existing managed link.
	 * @param linkContext  Used to get url when rendering link.
	 * @param isStaging Used to get link item.
	 * @param href Our existing href to modify.
	 * @return Our completed url to our link.
	 */
	private String createHref(PSManagedLink link,
			PSRenderLinkContext linkContext, Boolean isStaging, String href) throws PSDataServiceException, PSNotFoundException {
		IPSLinkableItem linkItem = getLinkItem(linkContext, link.getChildId(), isStaging);
		//  Add orphaned manage link cleanup somewhere.  catch errors when child does not exist
		if (linkItem != null)
		{
		    href = renderLinkService.renderLink(linkContext, linkItem).getUrl();
		    String anchor=link.getAnchor();
		    
		    if (StringUtils.isNotBlank(anchor))
		    {
		    	href += link.getAnchor();
		    }
		    
		    if(linkContext.getMode().equals(Mode.PUBLISH) && linkItem instanceof PSRenderAsset) {
		    	PSRenderAsset myasset = (PSRenderAsset) linkItem;
		    	try {
		    		if(myasset.getNode().hasProperty("rx:analyticsId")){
		    			Property analyticsProperty = myasset.getNode().getProperty("rx:analyticsId");
		    			String analyticsId = analyticsProperty.getValue().getString();
		    			if(StringUtils.isNotBlank(analyticsId) && !analyticsId.trim().equals("?")){
		    				analyticsId = encodeAnalyticsId(analyticsId);
		    				if(!href.contains(analyticsId)){
		    					href = appendAnalyticsId(href, analyticsId);
		    				}
		    			}
		    		}
		    	} catch (PathNotFoundException e) {
		    		log.error("Failed to retrieve property for Asset: " + myasset.getId() + ". Exception is " + e);
		    	} catch (RepositoryException e) {
		    		log.error("Failed to find property in object's repository for Asset: " + myasset.getId() + ". Exception is " + e);
		    	} catch (UnsupportedEncodingException e) {
		    		log.error("Failed to encode url: " + href + ". Exception is " + e);
				}
		    }
		}
		
		return href;
	}

	/**
	 * Append the analytics id to the end of the url if it forms a valid URL when together. 
	 * 
	 * @param href Our URL that may have the analytics id appended to the end of it.
	 * @param analyticsId our Analytics id to append to the end of the URL.
	 * @return Our URL with the analytics id encoded at the end of it.
	 * @throws UnsupportedEncodingException Failed to encode the analytics id. 
	 */
	private String appendAnalyticsId(String href, String analyticsId) throws UnsupportedEncodingException {
				
		if(URLValidator.isValid("http://localhost" + href + analyticsId)){
			href += analyticsId;
		} else{
			log.warn("The link to asset: " + href + " with encoded analytics id of " + analyticsId + " failed to form a valid URL.");
		}
		
		return href;
	}

	/**
	 * Encode the analytics id with UTF-8
	 * 
	 * @param analyticsId String to encode
	 * @return analyticsId encoded
	 * @throws UnsupportedEncodingException failed to encode analytics id
	 */
	private String encodeAnalyticsId(String analyticsId)
			throws UnsupportedEncodingException {
		analyticsId = StringUtils.removeStart(analyticsId, "?");
		
		if(analyticsId.contains("=")){
			String[] analyticsIdHalves = analyticsId.split("=");
			analyticsId = "?" + URLEncoder.encode(analyticsIdHalves[0], "UTF-8") + "=" + URLEncoder.encode(analyticsIdHalves[1], "UTF-8");
		} else {
			analyticsId = "?" + URLEncoder.encode(analyticsId, "UTF-8");
		}
		return analyticsId;
	}

    private void removeManagedAttributes(Element link)
    {
        link.removeAttr(PERC_MANAGED_ATTR);
        link.removeAttr(PERC_LINKID_ATTR);
        link.removeAttr(PERC_LINKID_OLD_ATTR);
        link.removeAttr(PERC_MANAGED_OLD_ATTR);
        link.removeClass(PSSingleValueBuilder.PERC_NOTPUBLICLINK);
        link.removeClass(PSSingleValueBuilder.PERC_BROKENLINK);
    }

    /**
     * Manages an image link if it is not already managed, unmanages the link if the
     * link's target is invalid. If there are any unexpected errors, they are
     * logged and the method silently returns
     * 
     * @param parentId The ID of the parent
     * @param link The link element to manage, assumed not <code>null</code>.
     */
    private void manageImageLink(String parentId, Element link)
    {
        try
        {
            // see if already managed
            long linkId = getLinkId(link);
            if (linkId != -1 && linkId !=0 )
            {
                // ensure it's valid, remove linkid if not
                if (validateImageLink(parentId, linkId, link))
                {
                    link.attr(PERC_MANAGED_ATTR,"true");
                    return;
                }

                link.removeAttr(PERC_LINKID_ATTR);
            }

            String src = link.attr(SRC_ATTR);
            src = decodeHref(src);
            if (StringUtils.isBlank(src))
                return;

            // attempt to find the target
            int dependentId = getDependentId(src);

            if (dependentId != -1)
            {
                String anchor = PSReplacementFilter.getAnchor(link.attr(HREF_ATTR));
                linkId = createLink(parentId, dependentId, anchor);
                link.attr(PERC_LINKID_ATTR, String.valueOf(linkId));
                link.attr(PERC_MANAGED_ATTR,"true");
                renderImageLink(null,link);
            }
        }
        catch (Exception e)
        {
            log.error("Unable to manage link: " + link.toString() + ": " + e.getLocalizedMessage(), e);
        }
    }



	/**
     */
    private void unmanageImageLink(Element link)
    {
        try
        {
            // see if already managed
            long linkId = getLinkId(link);
            if (linkId != -1 && linkId != 0)
            {
                //PSManagedLink managedLink = dao.findLinkByLinkId(linkId);
                //dao.deleteLink(managedLink);
                
                removeManagedAttributes(link);
            }
        }
        catch (Exception e)
        {
            log.error("Unable to unmanage link: " + link.toString() + ": " + e.getLocalizedMessage(), e);
        }
    }
    
    /**
     * Manages a link if it is not already managed, unmanages the link if the
     * link's target is invalid. If there are any unexpected errors, they are
     * logged and the method silently returns
     * 
     * @param link The link element to manage, assumed not <code>null</code>.
     */
    private void manageLink(String parentId, Element link)
    {
        try
        {
            // see if already managed
            long linkId = getLinkId(link);
            if (linkId != -1 && linkId!=0)
            {
                // ensure it's valid, remove linkid if not
                if (validateLink(parentId, linkId, link))
                {
                    link.attr(PERC_MANAGED_ATTR,"true");
                    return;
                }
                link.removeAttr(PERC_LINKID_ATTR);
            }

            String href = link.attr(HREF_ATTR);

            href=decodeHref(href);
            if (StringUtils.isBlank(href))
                return;

            // attempt to find the target
            int dependentId = getDependentId(href);

            if (dependentId != -1)
            {
            	String anchor = PSReplacementFilter.getAnchor(link.attr(HREF_ATTR));
                linkId = createLink(parentId, dependentId, anchor);
                link.attr(PERC_LINKID_ATTR, String.valueOf(linkId));
                link.attr(PERC_MANAGED_ATTR,"true");
                renderLink(null,link);
            }
        }
        catch (Exception e)
        {
            log.error("Unable to manage link: " + link.toString() + ": " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Ensure the dependent id referenced by this link exists, update the href
     * if it does.
     * 
     * @param parentId The ID of the parent
     * @param linkId The link ID to validate
     * @param linkEl The link element to update w/the correct href
     * 
     * @return <code>true</code> if it exists, <code>false</code> if not.
     * @throws Exception If there are any unexpected errors.
     */
    private boolean validateLink(String parentId, long linkId, Element linkEl) throws Exception
    {
        boolean isValid = false;

        PSManagedLink link = dao.findLinkByLinkId(linkId);
        if (link != null)
        {
            String itemPath = findItemPath(link.getChildId());
            if (itemPath != null)
            {
                isValid = true;
                
                String anchor = PSReplacementFilter.getAnchor(linkEl.attr(HREF_ATTR));
                if (link.getAnchor() != anchor)
                {
                	link.setAnchor(anchor);
                	dao.saveLink(link);
                	link = dao.findLinkByLinkId(linkId);
                }
                // update link href
                linkEl.attr(HREF_ATTR, itemPath);
                
                
                // Not valid if a new item link
                if (String.valueOf(UNASSIGNED_PARENT_ID).equals(parentId))
                    return false;
                
                // invalid link if parent is different (source was manually copied to new parent?)
                PSLocator parentLoc = idMapper.getLocator(parentId);
                if (link.getParentId() != parentLoc.getId())
                {
                    return false;
                }
                
                // update parent revision if different
                if (link.getParentRevision() != parentLoc.getRevision())
                {
                    link.setParentRevision(parentLoc.getRevision());
                    dao.saveLink(link);
                }
            }
        }

        return isValid;
    }
    
    private boolean validateLink(String parentId, long linkId, String path) throws Exception
    {
        boolean isValid = false;

        PSManagedLink link = dao.findLinkByLinkId(linkId);
        if (link != null)
        {
            String itemPath = findItemPath(link.getChildId());
            if (itemPath != null)
            {
                isValid = true;
                
                // Not valid if a new item link
                if (String.valueOf(UNASSIGNED_PARENT_ID).equals(parentId))
                    return false;
                
                // invalid link if parent is different (source was manually copied to new parent?)
                PSLocator parentLoc = idMapper.getLocator(parentId);
                if (link.getParentId() != parentLoc.getId())
                {
                    return false;
                }
                
                // update parent revision if different
                if (link.getParentRevision() != parentLoc.getRevision())
                {
                    link.setParentRevision(parentLoc.getRevision());
                    dao.saveLink(link);
                }
            }
        }

        return isValid;
    }
    
    /**
     * Ensure the dependent id referenced by this image link exists, update the src
     * if it does.
     * 
     * @param parentId The ID of the parent
     * @param linkId The link ID to validate
     * @param linkEl The link element to update w/the correct src
     * 
     * @return <code>true</code> if it exists, <code>false</code> if not.
     * @throws Exception If there are any unexpected errors.
     */
    private boolean validateImageLink(String parentId, long linkId, Element linkEl) throws Exception
    {
        boolean isValid = false;

        PSManagedLink link = dao.findLinkByLinkId(linkId);
        if (link != null)
        {
            String itemPath = findItemPath(link.getChildId());
            if (itemPath != null)
            {
                isValid = true;
                // update link href
                linkEl.attr(SRC_ATTR, itemPath);
                
                // Not valid if a new item link
                if (String.valueOf(UNASSIGNED_PARENT_ID).equals(parentId))
                    return false;
                
                // invalid link if parent is different (source was manually copied to new parent?)
                PSLocator parentLoc = idMapper.getLocator(parentId);
                if (link.getParentId() != parentLoc.getId())
                {
                    return false;
                }
                
                // update parent revision if different
                if (link.getParentRevision() != parentLoc.getRevision())
                {
                    link.setParentRevision(parentLoc.getRevision());
                    dao.saveLink(link);
                }
            }
        }

        return isValid;
    }
    
    /**
     * Finds the first item path for the supplied item.
     * 
     * @param itemId The item to find, assumed not <code>null</code>.
     * 
     * @return The path, never empty, <code>null</code> if the item is not
     *         found.
     */
    private String findItemPath(int itemId)
    {
        String path = null;
        try
        {
            String[] paths = contentWs.findItemPaths(guidMgr.makeGuid(new PSLocator(itemId, -1)));
            if (paths.length > 0)
            {
                path = PSPathUtils.getFinderPath(paths[0]);
            }
        }
        catch (Exception e)
        {
            // not found, return null
        }

        return path;
    }

    /**
     * Create a link in the repository for the supplied parent and dependent.
     * 
     * @param parentId The id (guid as string) of the item containing the link
     * @param dependentId The content id of the item the link is pointing to
     * 
     * @return The link id of the newly created link.
     *
     */
    private long createLink(String parentId, int dependentId, String anchor) throws IPSGenericDao.SaveException {
        int cid;
        int rev;
        List<Long> newIds = newLinkIds.get();
        
        if (parentId.equals(String.valueOf(UNASSIGNED_PARENT_ID)))
        {
            if (newIds == null)
            {
                throw new IllegalStateException("processing new item link, but newLinkIds not initialized on current thread");
            }
            cid = UNASSIGNED_PARENT_ID;
            rev = -1;
        }
        else
        {
            PSLocator parentLoc = idMapper.getLocator(parentId);
            cid = parentLoc.getId();
            rev = parentLoc.getRevision();
        }


        PSManagedLink link = dao.createLink(cid, rev, dependentId, anchor);
        dao.saveLink(link);

        if (newIds != null)
        {
            newIds.add(link.getLinkId());
        }
        
        return link.getLinkId();
    }
    
    /**
     * Locate the item for the specified path and return it's content id.
     * 
     * @param path The path to check.
     * 
     * @return The ID, or -1 if the path does not point to a valid item.
     */
    private int getDependentId(String path) throws Exception
    {
        int dependentId = -1;
        if(!(path.startsWith("/Sites/") || path.startsWith("/Assets/") || path.startsWith("//Sites/") || path.startsWith("//Assets/")))
        	return dependentId;
        String pathMod = path;
        String anchor = PSReplacementFilter.getAnchor(pathMod);
        if (anchor !=null && !anchor.isEmpty())
        	pathMod = pathMod.replace(anchor, "");
        
        // Fixup invalid url when trying to match
        pathMod = decodeHref(pathMod);
        
        String folderPath = PSPathUtils.getFolderPath(pathMod);
        
        IPSGuid guid = contentWs.getIdByPath(folderPath);
        if (guid == null)
        {
            String importedPath = pageCatalogService.convertToImportedFolderPath(folderPath);
            if (!importedPath.equals(folderPath))
                guid = contentWs.getIdByPath(importedPath);
        }
        
        if (guid != null)
        {
            String id = guid.toString();
                if (workflowHelper.isPage(id) || workflowHelper.isAsset(id))
                    dependentId = guid.getUUID();
            else{
                String indexPath = pageCatalogService.convertToImportedFolderPath(concatPath(folderPath, "index.html"));
                guid = contentWs.getIdByPath(indexPath);
                    if (guid != null && workflowHelper.isPage(guid.toString()))
                    {
                        dependentId = guid.getUUID();
                    }
                }
        }
        
        return dependentId;
    }

    /**
     * Extract the link id from the supplied element
     * 
     * @param link the element to check, assumed not <code>null</code>.
     * 
     * @return the link id, or -1 if not found or not a number.
     */
    @Override
    public long getLinkId(Element link)
    {
        long linkId = -1;
        String linkIdOld = null;
        String linkIdVal = null;
        
        if(!(link.attr(PERC_LINKID_OLD_ATTR)).equals("")){
        	linkIdOld = link.attr(PERC_LINKID_OLD_ATTR);
        	link.removeAttr(PERC_LINKID_OLD_ATTR);
        }
     	
        if(link.attr(PERC_LINKID_ATTR).equals("") && (linkIdOld!=null && !linkIdOld.isEmpty())){
        	linkIdVal = linkIdOld;
        	link.removeAttr(PERC_MANAGED_OLD_ATTR);
        	link.attr(PERC_MANAGED_ATTR, "true");
        	link.attr(PERC_LINKID_ATTR, String.valueOf(linkIdVal));
        } else if(!link.attr(PERC_MANAGED_OLD_ATTR).equals("") || !link.attr(PERC_MANAGED_ATTR).equals("") ) {
        	linkIdVal = link.attr(PERC_LINKID_ATTR);
        	link.removeAttr(PERC_MANAGED_OLD_ATTR);
        	link.attr(PERC_MANAGED_ATTR, "true");
        }else{
        	linkIdVal ="0";
        }
        try
        {
            linkId = Long.parseLong(linkIdVal);
        }
        catch (NumberFormatException e)
        {
            // not valid
        }

        return linkId;
    }
    

    /**
     * Get the parent id to use for new item links
     * 
     * @return The id, not <code>null<code/> or empty;
     */
    private String getNewItemParentId()
    {
        return String.valueOf(UNASSIGNED_PARENT_ID);
    }

    /**
     * Check for delete events and if an asset, delete any relationships for
     * which it is the parent
     */
    private void itemDeleted(String assetId)
    {
        IPSGuid guid = guidMgr.makeGuid(assetId);
        List<PSManagedLink> links = dao.findLinksByParentId(guid.getUUID());
        for (PSManagedLink link : links)
        {
            try
            {
                dao.deleteLink(link);
            }
            catch (Exception e)
            {
                log.error("Unable to delete manage link: " + link.toString() + ": " + e.getLocalizedMessage(), e);
            }
        }
    }

    public void cleanupOrphanedLinks()
    {
        try
        {
            dao.cleanupOrphanedLinks();
        }
        catch (Exception e)
        {
            log.error("Unable to cleanup manage links");
        }
    }
    
    
    public void cleanupDeletedLinks(List<Element> validLinks, Integer parentId) {
        List<PSManagedLink> currentLinks = dao.findLinksByParentId(parentId);
        Collection<PSManagedLink> linksToDelete = new ArrayList<>();
        
        for (PSManagedLink currentLink : currentLinks) {
            boolean linkMatched = false;
            for (Element validLink : validLinks) {
                if (validLink.attr(PERC_LINKID_ATTR).equals(
                        Long.toString(currentLink.getLinkId()))) {
                    log.debug("Matched valid link.");
                    linkMatched = true;
                    break;
                }
            }
            if (!linkMatched) {
                log.debug("Adding link id " + currentLink.getLinkId() + " to list of managed links to delete.");
                linksToDelete.add(currentLink);
            }
        }
        
        if (linksToDelete.size() > 0) {
            try {
                dao.deleteLinksInNewTransaction(linksToDelete);
            }
            catch (NullPointerException e) {
                log.error("Links to delete cannot be null or empty.");
            }
        }
    }
    
    @Override
    public List<String> getManagedLinks(Collection<String> parentIds)
    {
        notNull(parentIds);

        List<Integer> convertedParentIds = new ArrayList<>();
        for (String parentId : parentIds)
        {
            IPSGuid guid = guidMgr.makeGuid(parentId);
            convertedParentIds.add(guid.getUUID());
        }
        List<PSManagedLink> links = dao.findLinksByParentIds(convertedParentIds);
        
        List<String> linkIds = new ArrayList<>();
        for(PSManagedLink link : links)
        {
            PSLegacyGuid guid = new PSLegacyGuid(link.getChildId());
            linkIds.add(idMapper.getString(guid));
        }
        
        return linkIds;
    }

    @Override
    public String manageItemPath(String ownerId, String path, String linkId)
    {
        try
        {
            // see if new item
            if (ownerId == null)
                ownerId = getNewItemParentId();
            
            // see if already managed
            long linkIdVal = NumberUtils.toLong(linkId, -1);
            if (linkIdVal != -1)
            {
                // ensure it's valid, remove linkid if not
                if (validateLink(ownerId, linkIdVal, path))
                    return linkId;
            }

            if (StringUtils.isBlank(path))
                return null;

            // attempt to find the target
            int dependentId = getDependentId(path);

            if (dependentId != -1)
            {
                String anchor = PSReplacementFilter.getAnchor(path);
                return String.valueOf(createLink(ownerId, dependentId, anchor));
            }
        }
        catch (Exception e)
        {
            log.error("Unable to manage path: " + path + ": " + e.getLocalizedMessage(), e);
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see com.percussion.linkmanagement.service.IPSManagedLinkService#renderItemPath(com.percussion.pagemanagement.data.PSRenderLinkContext, java.lang.String)
     */
    @Override
    public String renderItemPath(PSRenderLinkContext linkContext, String linkId, Boolean isStaging)
    {
        String href = "#";
        
        try
        {
            long linkIdVal = NumberUtils.toLong(linkId, -1);

            if (linkIdVal != -1)
            {
                PSManagedLink mLink = dao.findLinkByLinkId(linkIdVal);
                href = renderHref(mLink, linkContext, isStaging);
                if (href == null)
                {
                    href = "#";
                }
            }
        }
        catch (Exception e)
        {
            log.error("Unable to render managed link for linkId: " + linkId + ": " + e.getLocalizedMessage(), e);
        }
        
        return href;
    }

    @Override
    public String renderItemPath(PSRenderLinkContext linkContext, String linkId) 
    {
    	return renderItemPath(linkContext, linkId, null);
    }

    @Override
    public boolean doManageAll() {
        return true;
    }

	@Override
	public boolean renderImageLink(PSRenderLinkContext linkContext, Element link) {
		return renderImageLink(linkContext, link, false);
	}

	@Override
	public boolean renderLink(PSRenderLinkContext linkContext, Element link) {
		return renderLink(linkContext, link, false);
	}

	@Override
	public List<PSManagedLink> findLinksByChildId(int contentId) {	
		return dao.findLinksByChildId(contentId);
	}
	
	
	 /**
     * Get the page or asset link item for the supplied id.
     * 
     * @param linkContext The link context to use to determine the revision to use, assumed not <code>null</code>.
     * @param childId The id, assumed not <code>null</code>.
     * 
     * @return The link item for the current revision, <code>null</code> if no page or asset found for the supplied id.
     */
    private IPSLinkableItem getLinkItem(PSRenderLinkContext linkContext, int childId, Boolean isStaging) throws PSValidationException, PSNotFoundException {
        IPSLinkableItem item = null;
        
        // get correct revision
        IPSGuid guid = getCorrectRevisionGuid(childId, linkContext, isStaging);
        if (guid == null) {
            return null;
        }
        String id = guid.toString();
        
        // get either asset or page
        if (workflowHelper.isPage(id))
        {
            item = PSPathUtils.getLinkableItem(id);
        }
        else
        {
            try
            {
                item = assemblyItemBridge.createLinkableItem(guid);
            }
            catch (Exception e)
            {
                // no asset found, so allow to return null
                log.error("Unable to locate asset with id " + id + ": " + e.getLocalizedMessage());
            }
        }
        
        return item;
    }

    private IPSGuid getCorrectRevisionGuid(int id, PSRenderLinkContext linkContext, Boolean isStaging) {
        IPSGuid guid = idMapper.getGuid(new PSLocator(id, -1));
        PSComponentSummary sum;
        try
        {
            sum = workflowHelper.getComponentSummary(guid.toString());
        }
        catch (Exception e)
        {
            // no sum found, so return null
            log.warn("Unable to locate dependent item with id " + id + ", possible broken link: " + e.getLocalizedMessage());

            try
            {
                dao.cleanupOrphanedLinks();
            }
            catch (Exception ex)
            {
                log.error("Cannot cleanup orphan links", ex);
            }

            return null;
        }

        PSLocator loc = null;
        if (linkContext!=null && linkContext.getMode().equals(PSRenderLinkContext.Mode.PUBLISH))
        {
            if(isStaging != null && isStaging && workflowHelper.isItemInStagingState(id))
            {
                loc = sum.getCurrentLocator();
            }
            else
            {
                int rev = sum.getPublicRevision();
                if (rev != -1)
                {
                    loc = new PSLocator(id, rev);
                }
            }
        }
        else
        {
            loc = sum.getCurrentLocator();
        }

        // no valid revision, return null
        if (loc == null)
            return null;

        return idMapper.getGuid(loc);
    }

    /**
     * Gets a node for the specified asset.
     * @param childId - the ID of the asset to retrieve.
     * @param linkContext - link context used to get correct revision.
     * @param isStaging - also used to
     * @param value
     * @return
     */
    private Node getAssetNode(int childId, PSRenderLinkContext linkContext, Boolean isStaging, String value) {
        IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
        IPSGuid guid = getCorrectRevisionGuid(childId, linkContext, isStaging);
        List<IPSGuid> guidList = new ArrayList<>();
        List<Node> nodeList;
        guidList.add(guid);
        try
        {
            PSContentMgrConfig conf = new PSContentMgrConfig();

            //Don't return the binary value by default.
            conf.addOption(PSContentMgrOption.LOAD_MINIMAL);
            conf.addOption(PSContentMgrOption.LAZY_LOAD_CHILDREN);
            nodeList = mgr.findItemsByGUID(guidList, conf);

            return nodeList.get(0);
        }
        catch (RepositoryException e)
        {
            log.error("Unable to get node for alt and title text with ID: " + childId +
                    " and error message:"+ e.getMessage());
            log.debug(e);
        }
        return null;
    }
}
