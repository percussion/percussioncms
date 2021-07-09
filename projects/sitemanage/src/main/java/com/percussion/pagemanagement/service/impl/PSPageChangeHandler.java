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
package com.percussion.pagemanagement.service.impl;


import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.assetmanagement.service.impl.PSWidgetAssetRelationshipService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.impl.PSItemWorkflowService;
import com.percussion.pagemanagement.data.PSPageChangeEvent;
import com.percussion.pagemanagement.data.PSPageChangeEvent.PSPageChangeEventType;
import com.percussion.pagemanagement.service.IPSPageChangeListener;
import com.percussion.rest.Guid;
import com.percussion.services.legacy.IPSCmsObjectMgrInternal;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.share.dao.IPSContentItemDao;
import com.percussion.share.dao.impl.PSContentItem;
import com.percussion.share.service.exception.PSDataServiceException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;

/**
 * This class implements the {@link IPSPageChangeListener} interface and gets
 * notified when a page is changed. It currently handles the title updates and
 * page summary updates.
 * 
 * @author BJoginipally
 */
public class PSPageChangeHandler implements IPSPageChangeListener
{
    private IPSCmsObjectMgrInternal m_cmsObjectMgr = (IPSCmsObjectMgrInternal) PSCmsObjectMgrLocator.getObjectManager();
    public PSPageChangeHandler()
    {
 
    }
    
   /*
    * //see base class method for details
    */
   @Override
   public void pageChanged(PSPageChangeEvent pageChangeEvent)
   {
       //If contentItemDao is null get the bean from the Web Application Context
       if(contentItemDao == null)
       {
           contentItemDao = (IPSContentItemDao) getWebApplicationContext().getBean("contentItemDao");
       }

       //If contentItemDao is null get the bean from the Web Application Context
       if(widgetAssetRelationshipService == null)
       {
         widgetAssetRelationshipService = (PSWidgetAssetRelationshipService) getWebApplicationContext()
               .getBean("widgetAssetRelationshipService");
       }

       String pageId = pageChangeEvent.getPageId();
       String itemId = pageChangeEvent.getItemId();
       PSPageChangeEventType type = pageChangeEvent.getType();
       if((type.equals(PSPageChangeEventType.ITEM_ADDED) || type.equals(PSPageChangeEventType.ITEM_SAVED) || 
             type.equals(PSPageChangeEventType.ITEM_REMOVED))&& StringUtils.isBlank(itemId))
          throw new IllegalArgumentException("itemId must not be blank for item events");
          
       PSContentItem page = null;
       PSContentItem asset = null;
       
       try
       {
          page = contentItemDao.find(pageId);
          if(page == null){
        	  throw new Exception("Unable to find Page with id " + pageId);
          }
          	
       }
       catch(Exception e)
       {
           log.error("Error while finding the Page with the pageId " + pageId  + " in pageChanged Event Handler.",e);
           //FB: NP_NULL_PARAM_DEREF NC 1-17-16 If there is no Page there is no point in continuing
           return;
       }
       
       //Load the Asset
       if(!type.equals(PSPageChangeEventType.ITEM_REMOVED ) && itemId != null){
	       try{
               asset = contentItemDao.find(itemId);
               if(asset == null){
              	 throw new Exception("Unable to find Asset with id " + itemId);
               }
	       }catch(Exception e){
	    	   log.error("Error while finding the Asset with the itemId {} in pageChanged Event Handler.  Error: {}",itemId,e.getMessage());
	    	   log.debug(e.getMessage(),e);
	       }
       }
       
       if(type.equals(PSPageChangeEventType.ITEM_ADDED) || type.equals(PSPageChangeEventType.ITEM_SAVED) && asset!=null)
          updateLinkText(page, asset);
       
       // Story 353: sync the page title with the blog post widget title
       if(type.equals(PSPageChangeEventType.PAGE_META_DATA_SAVED))
       {
           updateBlogPostWidgetTitle(page);
       }
           
       //Update the author on page change, @TODO handle asset deletes.
       if(asset!=null)
          updateAuthor(page, asset);
       updateSummary(page);
       PSNotificationEvent notifyEvent = new PSNotificationEvent(EventType.PAGE_SAVED, page.getId());
       IPSNotificationService srv = PSNotificationServiceLocator.getNotificationService();
       srv.notifyEvent(notifyEvent);
   }
   
    /**
     * Gets all the widgets in the page and find the blog post widget. If the page has 
     * one, it updates its title. If it doesn't, just return.
     * 
     * @param page The page where the metadata has been changed.
     */
    private void updateBlogPostWidgetTitle(PSContentItem page)
    {
        try {
            // get all the local assets and retrieve them to see their types
            Set<String> assets = widgetAssetRelationshipService.getLocalAssets(page.getId());
            IPSItemWorkflowService workFlowService = (PSItemWorkflowService) getWebApplicationContext()
                    .getBean("workflowRestService");

            if (assets != null) {
                for (String assetId : assets) {
                    try {
                        PSContentItem asset = contentItemDao.find(assetId);
                        if (BLOG_POST_ASSET_TYPE.equals(asset.getType())) {
                            Map<String,Object> pageFields = page.getFields();
                            String pageTitle = (String) pageFields.get(PAGE_LINK_TEXT_FIELD_NAME);

                            Map<String,Object> assetFields = asset.getFields();
                            if (assetFields.containsKey(BLOG_POST_WIDGET_TITLE)) {
                                assetFields.put(BLOG_POST_WIDGET_TITLE, pageTitle);

                                if (!workFlowService.isCheckedOutToCurrentUser(asset.getId())) {
                                    workFlowService.checkOut(asset.getId());
                                }

                                contentItemDao.save(asset);
                            }

                            break;
                        }
                    } catch (PSDataServiceException | IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                        log.warn("Error updating Linked Title. Error:{}", e.getMessage());
                        log.debug(e.getMessage(), e);
                    }
                }
            }
        } catch (IPSWidgetAssetRelationshipService.PSWidgetAssetRelationshipServiceException e) {
            log.warn("Error updating Linked Title. Error:{}", e.getMessage());
            log.debug(e.getMessage(), e);
        }
    }

    /**
    * Check if the asset is a blog post widget, and then updates the page title. If it is not, and 
    * this asset is a title widget, check for the sync_link_text field on the asset and if it exists 
    * and equal to "1" then updates resource_link_title field value on the page and deletes the asset 
    * other wise just return
    * @param page
    * @param assetId
    */
   private void updateLinkText(PSContentItem page, PSContentItem assetId)
   {
       try {
           String assetType = assetId.getType();

           // Story 353: the blog post asset title and the page title should sync
           if (assetType.equalsIgnoreCase(BLOG_POST_ASSET_TYPE)) {
               Map<String, Object> assetFields = assetId.getFields();
               String assetTitle = (String) assetFields.get(BLOG_POST_WIDGET_TITLE);

               Map<String, Object> pageFields = page.getFields();
               if (pageFields.containsKey(PAGE_LINK_TEXT_FIELD_NAME)) {
                   pageFields.put(PAGE_LINK_TEXT_FIELD_NAME, assetTitle);
                   contentItemDao.save(page);
               }
           } else if (assetType.equalsIgnoreCase(TITLE_WIDGET_TYPE)) {
               Map<String, Object> assetFields = assetId.getFields();
               String syncValue = (String) assetFields.get(TITLE_WIDGET_SYNC_FIELD_NAME);
               if (syncValue == null || !syncValue.equals(TITLE_WIDGET_SYNC))
                   return;

               String assetTitle = (String) assetFields.get(TITLE_WIDGET_TITLE_FIELD_NAME);
               Map<String, Object> pageFields = page.getFields();
               if (pageFields.containsKey(PAGE_LINK_TEXT_FIELD_NAME)) {
                   pageFields.put(PAGE_LINK_TEXT_FIELD_NAME, assetTitle);
                   contentItemDao.save(page);
                   contentItemDao.delete(assetId.getId());
               }

               // We could have the case of a title widget together width a blog post widget
               // so we need to keep the sync between 3 fields
               updateBlogPostWidgetTitle(page);
           } else {
               // just return if this is not a title widget or a blog post widget
               return;
           }
       } catch (PSDataServiceException e) {
           log.error(e.getMessage());
           log.debug(e.getMessage(), e);
       }
   }
  
   /**
    * Updates the summary of the page, if the page's auto generate summary field is checked then updates the page summary
    * by getting the page summary from the first rich text asset that has more link in it. 
    */
   private void updateSummary(PSContentItem page)
   {
       try {
           Map<String, Object> pageFields = page.getFields();
           String autoGen = (String) pageFields.get(PAGE_SUMMARY_GEN_FIELD_NAME);
           if (autoGen == null || !autoGen.equals(AUTO_GENERATE_SUMMARY))
               return;
           String newSummary = generatePageSummary(page.getId());
           if (pageFields.containsKey(PAGE_SUMMARY_FIELD_NAME)) {
               //Update Content Post Date equals to first publish date in case postdate is set to null
               Integer intg = (new Guid(page.getId())).getUuid();
               Date postDate = m_cmsObjectMgr.getFirstPublishDate(intg);
               if(page.getFields() != null && page.getFields().get("sys_contentpostdate") == null
                       && postDate!= null){
                   page.getFields().put("sys_contentpostdate",postDate.toString());
               }
               pageFields.put(PAGE_SUMMARY_FIELD_NAME, newSummary);
               contentItemDao.save(page);
           }
       } catch (PSDataServiceException e) {
           log.warn("Error update Page summary for Page: {} Error: {}",page.getId(),e.getMessage() );
           log.debug(e.getMessage(),e);
       }
   }
   
   /**
    * Updates the author of the page, if the supplied asset type supports the author. 
    * @param page, assumed not <code>null</code>.
    * @param asset, assumed not <code>null</code>.
    */
   private void updateAuthor(PSContentItem page, PSContentItem asset)
   {
       try {
           String assetType = asset.getType();
           if (authorSupportedTypes.containsKey(assetType)) {
               Map<String,Object> assetFields = asset.getFields();
               String authorFieldName = authorSupportedTypes.get(assetType);
               String author = (String) assetFields.get(authorFieldName);
               Map<String,Object> pageFields = page.getFields();
               if (pageFields.containsKey(PAGE_AUTHOR_FIELD_NAME)) {
                   pageFields.put(PAGE_AUTHOR_FIELD_NAME, author);
                   contentItemDao.save(page);
               }
           }
       } catch (PSDataServiceException e) {
           log.warn("Error update Author for Page: {} Error: {}",page.getId(),e.getMessage());
           log.debug(e.getMessage(),e);
       }
   }
   /**
    * Helper method that generates the page summary. Gets local assets and shared assets of the page and from the first 
    * rich text field grabs the text from the beginning to the first more link represented by 
    * <span class="perc-blog-more-link"></span>. Stops processing after finding first rich text asset that has more link.  
    */
   private String generatePageSummary(String pageId)
   {
       String summary = "";

       try {

           Set<String> assetIds = widgetAssetRelationshipService.getLocalAssets(pageId);
           assetIds.addAll(widgetAssetRelationshipService.getSharedAssets(pageId));
           for (String assetId : assetIds) {
               try {
                   PSContentItem asset = contentItemDao.find(assetId, false);
                   //If the asset exists and its type is a more link supported type then get extract the summary.
                   if (asset != null && moreLinkSupportTypes.containsKey(asset.getType())) {
                       Map<String, Object> assetFields = asset.getFields();
                       String text = (String) assetFields.get(moreLinkSupportTypes.get(asset.getType()));
                       int moreIndex = StringUtils.indexOf(text, MORE_LINK_TEXT);
                       if (moreIndex != -1) {
                           summary = text.substring(0, moreIndex + MORE_LINK_TEXT.length());
                           break;
                       }
                   }
               } catch (PSDataServiceException e) {
                   log.warn(e.getMessage());
                   log.debug(e.getMessage(),e);
               }
           }
       } catch (IPSWidgetAssetRelationshipService.PSWidgetAssetRelationshipServiceException e) {
          log.warn("Error generating Page summary for Page: {} Error: {}",pageId,e.getMessage());
          log.debug(e.getMessage(),e);
       }
       return summary;

   }

   //Initialized on the first call to the #pageChanged() method. 
   private IPSContentItemDao contentItemDao;
   //Initialized on the first call to the #pageChanged() method. 
   private PSWidgetAssetRelationshipService widgetAssetRelationshipService;
   
   private static final String MORE_LINK_TEXT = "<!-- morelink -->";
   
   // Constants for page and title widget fields
   private static final String TITLE_WIDGET_SYNC_FIELD_NAME = "sync_link_text";
   private static final String TITLE_WIDGET_SYNC = "1";
   private static final String PAGE_LINK_TEXT_FIELD_NAME = "resource_link_title";
   private static final String TITLE_WIDGET_TYPE = "percTitleAsset";
   private static final String TITLE_WIDGET_TITLE_FIELD_NAME = "text";

   // Constants for blog post asset fields
   private static final String BLOG_POST_ASSET_TYPE = "percBlogPostAsset";
   private static final String BLOG_POST_WIDGET_TITLE = "displaytitle";
   
   // Constants for page and summary fields
   private static final String PAGE_SUMMARY_FIELD_NAME = "page_summary";
   private static final String PAGE_SUMMARY_GEN_FIELD_NAME = "auto_generate_summary";
   private static final String AUTO_GENERATE_SUMMARY = "1";
   private static final String PAGE_AUTHOR_FIELD_NAME = "page_authorname";

   /**
    * A map of content type name and a more link capable field name.
    */
   private static Map<String, String> moreLinkSupportTypes = new HashMap<>();
   static
   {
      moreLinkSupportTypes.put("percRichTextAsset", "text");
      moreLinkSupportTypes.put(BLOG_POST_ASSET_TYPE, "postbody");
   }
   
   private static Map<String, String> authorSupportedTypes = new HashMap<>();
   static
   {
      authorSupportedTypes.put("percBlogPostAsset", "authorname");
   }
   /**
    * Logger for this class
    */
   public static final Logger log = LogManager.getLogger(PSPageChangeHandler.class);

}
