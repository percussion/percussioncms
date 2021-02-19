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
package com.percussion.sitemanage.impl;

import static org.apache.commons.lang.StringUtils.removeEnd;
import static org.apache.commons.lang.StringUtils.startsWith;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSContentList.Type;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEdition.Priority;
import com.percussion.services.publisher.IPSEditionContentList;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.data.PSEditionContentList;
import com.percussion.services.publisher.data.PSEditionContentListPK;
import com.percussion.services.publisher.data.PSEditionType;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.pubserver.PSPubServerDaoLocator;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.publishing.IPSPublishingWs;
import com.percussion.webservices.publishing.PSPublishingWsLocator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * Helper methods used to create Editions and content lists for CM1 publishing servers.  So far only
 * what's needed for incremental publishing has been refactored to this class to make it available to
 * the upgrade plugin.  More can move over time as needed.
 * 
 * @author JaySeletz I just moved stuff here from PSSitePublishDao, don't blame me, I didn't write it!
 *
 */
public class PSSitePublishDaoHelper
{

   /**
    * Utility method used to generate a name of a content list or edition from
    * the supplied site name and name.
    * 
    * @param siteName The site name, may not be blank.
    * @param suffix The suffix of content list/edition name, may not be blank.
    * @return The site specific name in the form SITENAME_[CONTENT LIST/EDITION
    *         NAME].
    */
   public static String createName(String siteName, String suffix)
   {
       notEmpty(siteName, "siteName");
       notEmpty(suffix, "suffix");
   
       return siteName.toUpperCase() + '_' + suffix.toUpperCase();
   }

   /**
    * Generates the url for a content list.
    * 
    * @param name The content list name, may not be blank.
    * @param type The delivery type, may not be blank.
    * 
    * @return The content list url for the specified name and type.
    */
   public static String makeContentListUrl(String name, String type)
   {
       Validate.notEmpty(name, "name");
       Validate.notEmpty(type, "type");
   
       return "/Rhythmyx/contentlist?sys_deliverytype=" + type + "&sys_contentlist=" + name;
   }

   /**
    * Generates the search query used by the Full Non-Binary content list.
    * 
    * @param siteRoot The site root folder path, may not be blank.
    * 
    * @return The search query for Full Non-Binary content list.
    */
   public static String makeJcrSearchQuery(String siteRoot)
   {
       isTrue(startsWith(siteRoot, "//"), "Folder path must start with //");
       String folderPath = removeEnd(siteRoot, "/") + "/";
       return "select rx:sys_contentid, rx:sys_folderid from nt:base where jcr:path like '"
               + folderPath + "%'";
   }

   public static String createSiteSuffix(IPSSite site)
   {
       return site.getSiteId() + "_";
   }


   /**
    * Builds the expander params for the given Server. If the server is for
    * publishing in xml format, or it is {@link IPSPubServer.PublishType Database type}, it
    * needs to give a different template for the expander.
    * 
    * @param pubServer the {@link PSPubServer}, not <code>null</code>.
    * @return {@link Map}>{@link String}, {@link String}>, never
    *         <code>null</code> but may be empty.
    */
   public static Map<String, String> getExpanderParams(PSPubServer pubServer)
   {
       Validate.notNull(pubServer);
       Map<String, String> params = new HashMap<String, String>();
   
       if (pubServer.isXmlFormat())
       {
           params.put(EXPANDER_TEMPLATE_PARAM_NAME, EXPANDER_TEMPLATE_XML_VALUE);
       }
       else if (pubServer.isDatabaseType())
       {
           params.put(EXPANDER_TEMPLATE_PARAM_NAME, EXPANDER_TEMPLATE_DB_VALUE);
       }
       else
       {
           params.put(EXPANDER_TEMPLATE_PARAM_NAME, "");
       }
   
       return params;
   }

   /**
    * Creates a content list for the publish server.
    * 
    * @param siteName The site name, may not be blank.
    * @param name The name of the new content list, may not be blank.
    * @param type The type of the new content list which determines how it will
    *            be processed, may not be <code>null</code>.
    * @param description The new content list description, may not be
    *            <code>null</code>.
    * @param edtnType The edition type of the new content list, may not be
    *            <code>null</code>.
    * @param generator The content list generator, may not be blank.
    * @param genParams The content list generator parameters.
    * @param filterId The id of the filter to be used with the content list,
    *            may not be <code>null</code>.
    * @param isPublish determines if the content list is used for publishing or unpublishing. It is <code>true</code>
    *            if it is used for publishing.
    * @param pubServer The pubServer, may not be <code>null</code>.      
    */
   public static IPSContentList createPubServerContentList(String siteName, String name, Type type, String description,
           PSEditionType edtnType, String generator, Map<String, String> genParams, IPSGuid filterId, 
           boolean isPublish, PSPubServer pubServer)
   {
       notEmpty(siteName,"siteName may not be blank");
       notEmpty(name, "name");
       notNull(description, "description");
       notNull(type, "type");
       notNull(edtnType, "edtnType");
       notEmpty(generator, "generator");
       notNull(filterId, "filterId");
       notNull(pubServer, "pubServer");
   
       String cListName = createName(pubServer.getName(), name);
       IPSPublishingWs publishWs = PSPublishingWsLocator.getPublishingWebservice();
       IPSContentList cList = publishWs.createContentList(cListName);
       cList.setType(type);
       cList.setEditionType(edtnType);
       cList.setDescription(description + " - " + siteName);
       String url = makeContentListUrl(cListName, pubServer.getPublishType());
       if (!isPublish)
           url += "&" + IPSHtmlParameters.SYS_PUBLISH + "=unpublish";
       cList.setUrl(url);
       cList.setGenerator(generator);
       if (genParams != null)
       {
           cList.setGeneratorParams(genParams);
       }
       
       cList.setExpander(CONTENT_LIST_TEMPLATE_EXPANDER);
       cList.setExpanderParams(getExpanderParams(pubServer));
       cList.setFilterId(filterId);
   
       publishWs.saveContentList(cList);
       return cList;
   }
   
   /**
    * Finds the default pub server for the supplied site id.
    * @param siteId must not be <code>null</code>, must correspond to a valid site.
    * @return default pubserver corresponding to the supplied site.
    * @throws PSNotFoundException if the site is not found.
    */
   public static PSPubServer getDefaultPubServer(IPSGuid siteId) throws PSNotFoundException {
       Validate.notNull(siteId);
       PSPubServer defaultPubServer = null;
       
       IPSSiteManager siteMgr = PSSiteManagerLocator.getSiteManager();
       IPSSite site = siteMgr.findSite(siteId);
       if(site == null)
          throw new PSNotFoundException(siteId);
       IPSPubServerDao pubServerDao = PSPubServerDaoLocator.getPubServerManager();
       List<PSPubServer> servers = pubServerDao.findPubServersBySite(site.getGUID());
       
       for (PSPubServer server : servers)
       {
           if(site.getDefaultPubServer() == server.getServerId())
               return pubServerDao.loadPubServerModifiable(server.getGUID());
       }
       
       return defaultPubServer;
   }

   /**
    * Finds the staging pub server for the supplied site id.
    * @param siteId must not be <code>null</code>, must correspond to a valid site.
    * @return staging pubserver corresponding to the supplied site, may be <code>null</code> if it has not been created yet.
    * @throws PSNotFoundException if the site is not found.
    */
   public static PSPubServer getStagingPubServer(IPSGuid siteId) throws PSNotFoundException {
       Validate.notNull(siteId);
       PSPubServer stagingPubServer = null;
       
       IPSSiteManager siteMgr = PSSiteManagerLocator.getSiteManager();
       IPSSite site = siteMgr.findSite(siteId);
       if(site == null)
          throw new PSNotFoundException(siteId);
       IPSPubServerDao pubServerDao = PSPubServerDaoLocator.getPubServerManager();
       List<PSPubServer> servers = pubServerDao.findPubServersBySite(site.getGUID());
       
       for (PSPubServer server : servers)
       {
          if(PSPubServer.STAGING.equalsIgnoreCase((server.getServerType())))
          {
             stagingPubServer = server;
             break;
          }
       }
       
       return stagingPubServer;
   }
       
   public static IPSContentList createIncrementalContentList(IPSSite site, PSPubServer pubServer, IPSGuid filterId)
   {
       // create an incremental changelist only for default server, handles pages and assets
       Validate.notNull(site);
       Validate.notNull(pubServer);
       Validate.notNull(filterId);
       String suffix = createSiteSuffix(site);
       Map<String, String> incrementalParams = new HashMap<String, String>();
       if(PSPubServer.STAGING.equalsIgnoreCase(pubServer.getServerType())){
          suffix += "STAGING";
          incrementalParams.put(INCREMENTAL_CHANGETYPE_PARAM_NAME, INCREMENTAL_CHANGETYPE_STAGED);
       }
       else{
          incrementalParams.put(INCREMENTAL_CHANGETYPE_PARAM_NAME, INCREMENTAL_CHANGETYPE_LIVE);
       }
       
       return createPubServerContentList(site.getName(), suffix + "_" + Type.INCREMENTAL, IPSContentList.Type.NORMAL, "Site Incremental",
               PSEditionType.AUTOMATIC, INCREMENTAL_GENERATOR, incrementalParams, filterId, true, pubServer);
   }


   /**
    * Creates an edition for the given site. This method assumes that the
    * publish and site folder assembly publishing contexts exist.
    * 
    * @param site The site, may not be <code>null</code>.
    * @param suffix The suffix of the new edition name, may not be blank.
    * @param description The description of the new edition, may not be blank.
    * @param type The type of the new edition, may not be <code>null</code>.
    * @param priority The new edition priority, may not be <code>null</code>.
    * @param cLists The set of content lists to associate with the edition. The
    *            content lists should be ordered by sequence. Never
    *            <code>null</code>.
    * 
    * @throws PSErrorException If a required publishing context does not exist.
    */
   public static void createEdition(IPSSite site, String suffix, String description, PSEditionType type,
           Priority priority, IPSContentList[] cLists, PSPubServer pubServer, boolean isPublishServer,
           boolean isDefaultServer) throws PSErrorException, PSNotFoundException {
       notNull(site, "site");
       notEmpty(suffix, "suffix");
       notEmpty(description, "description");
       notNull(type, "type");
       notNull(priority, "priority");
       notNull(cLists, "cLists");
   
       IPSPublishingWs pubWs = PSPublishingWsLocator.getPublishingWebservice();
       IPSEdition edtn = pubWs.createEdition();
       String edtnName = createName(site.getName(), suffix);
       if (isPublishServer && pubServer != null)
       {
           edtnName = createName(pubServer.getName(), suffix);
       }
       edtn.setName(edtnName);
       edtn.setDisplayTitle(edtnName);
       edtn.setComment(description);
       edtn.setEditionType(type);
       edtn.setSiteId(site.getGUID());
       edtn.setPriority(priority);
       if (pubServer == null)
       {
           pubServer = getDefaultPubServer(site.getGUID());
       }
       edtn.setPubServerId(pubServer.getGUID());
   
       IPSGuid edtnGuid = edtn.getGUID();
       long edtnId = edtnGuid.longValue();
   
       IPSPublishingContext locationContext = pubWs.loadContext(LOCATION_CONTEXT);
       IPSGuid locationContextId = locationContext.getGUID();
       IPSPublishingContext linkContext = pubWs.loadContext(LINK_CONTEXT);
       IPSGuid linkContextId = linkContext.getGUID();
   
       for (int i = 0; i < cLists.length; i++)
       {
           IPSEditionContentList ecl = pubWs.createEditionContentList();
           PSEditionContentListPK eclPk = ((PSEditionContentList) ecl).getEditionContentListPK();
           eclPk.setEditionid(edtnId);
           eclPk.setContentlistid(cLists[i].getGUID().longValue());
           ecl.setSequence(i + 1);
           ecl.setDeliveryContextId(locationContextId);
           ecl.setAssemblyContextId(linkContextId);
           pubWs.saveEditionContentList(ecl);
       }
   
       pubWs.saveEdition(edtn);
   
       if (isAmazonEdition(pubServer) && !isNowEdition(suffix)) //CMS-5763
       {
           /*
            * Add the ant pre-edition task here.
            */
           IPSEditionTaskDef preTask = pubWs.createEditionTask();
           preTask.setContinueOnFailure(false);
           preTask.setEditionId(edtnGuid);
           preTask.setParam("bucket_name", pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_AS3_BUCKET_PROPERTY, ""));
           preTask.setParam("access_key", pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_AS3_ACCESSKEY_PROPERTY, ""));
           preTask.setParam("secret_key", pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_AS3_SECURITYKEY_PROPERTY, ""));
           preTask.setParam("region", pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_EC2_REGION, ""));
           preTask.setSequence(0);
           preTask.setExtensionName(AMAZONS3_EDITION_TASK_EXT_NAME);
           pubWs.saveEditionTask(preTask);
       }
       else if(!isNowEdition(suffix)){
          /*
           * Add the ant pre-edition task here.
           */
          IPSEditionTaskDef preTask = pubWs.createEditionTask();
          preTask.setContinueOnFailure(false);
          preTask.setEditionId(edtnGuid);
          preTask.setParam("ant_file", "copy-resources.xml");
          preTask.setSequence(0);
          preTask.setExtensionName(EDITION_TASK_EXT_NAME);
          pubWs.saveEditionTask(preTask);
          
       }
   
       /*
        * Add the workflow post edition task here.
        */
       if (!suffix.equals(UNPUBLISH_NOW) && !isStagingServer(pubServer))
       {
           IPSEditionTaskDef workflowTask = pubWs.createEditionTask();
           workflowTask.setContinueOnFailure(false);
           workflowTask.setEditionId(edtnGuid);
           workflowTask.setParam("state", PENDING_WORKFLOW_STATE);
           workflowTask.setParam("trigger", LIVE_WORKFLOW_TRANSITION);
           workflowTask.setSequence(1);
           workflowTask.setExtensionName(WF_EDITION_TASK_EXT_NAME);
           pubWs.saveEditionTask(workflowTask);
       }
       /*
        * Add the staging post edition task here.
        */
       if (isStagingServer(pubServer))
       {
           IPSEditionTaskDef stagingPostEdTask = pubWs.createEditionTask();
           stagingPostEdTask.setContinueOnFailure(false);
           stagingPostEdTask.setEditionId(edtnGuid);
           stagingPostEdTask.setSequence(1);
           stagingPostEdTask.setExtensionName(STAGING_EDITION_TASK_EXT_NAME);
           pubWs.saveEditionTask(stagingPostEdTask);
       }

       if ((isDefaultServer || isStagingServer(pubServer)) && !isNowEdition(suffix))
       {
           /*
            * Add the push feeds edition task
            */
           IPSEditionTaskDef pushFeedsTask = pubWs.createEditionTask();
           pushFeedsTask.setContinueOnFailure(false);
           pushFeedsTask.setEditionId(edtnGuid);
           pushFeedsTask.setSequence(2);
           pushFeedsTask.setExtensionName("Java/global/percussion/task/perc_PushFeedDescriptorTask");
           pubWs.saveEditionTask(pushFeedsTask);
   
           /*
            * Add the flush publication cache post edition task
            */
           IPSEditionTaskDef flushPublicationCacheTask = pubWs.createEditionTask();
           flushPublicationCacheTask.setContinueOnFailure(false);
           flushPublicationCacheTask.setEditionId(edtnGuid);
           flushPublicationCacheTask.setSequence(3);
           flushPublicationCacheTask.setExtensionName("Java/global/percussion/task/sys_flushPublicationCache");
           pubWs.saveEditionTask(flushPublicationCacheTask);
       }
       
   }
   
   private static boolean isAmazonEdition(PSPubServer pubServer)
   {
      return pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_DRIVER_PROPERTY).equalsIgnoreCase("AMAZONS3");
   }

   private static boolean isStagingServer(PSPubServer pubServer)
   {
      return PSPubServer.STAGING.toString().equals(pubServer.getServerType());
   }

   private static boolean isNowEdition(String suffix){
      return suffix.equals(PUBLISH_NOW) || suffix.equals(UNPUBLISH_NOW) || suffix.equals(STAGING_PUBLISH_NOW)
            || suffix.equals(STAGING_UNPUBLISH_NOW);
   }
   
   public static IPSGuid getPublicItemFilterGuid()
   {
       IPSPublishingWs publishWs = PSPublishingWsLocator.getPublishingWebservice();
       IPSItemFilter filter = publishWs.findFilterByName(PERC_PUBLIC_ITEM_FILTER_NAME);
       IPSGuid filterId = filter.getGUID();
       return filterId;
   }

   public static IPSGuid getStagingItemFilterGuid()
   {
       IPSPublishingWs publishWs = PSPublishingWsLocator.getPublishingWebservice();
       IPSItemFilter filter = publishWs.findFilterByName(PERC_STAGING_ITEM_FILTER_NAME);
       IPSGuid filterId = filter.getGUID();
       return filterId;
   }

   /**
    * Constant for the publish publishing context.
    */
   public static final String LOCATION_CONTEXT = "ResourceLocation";
   /**
    * Constant for the site folder assembly publishing context.
    */
   public static final String LINK_CONTEXT = "ResourceLink";

   /**
    * Constant for the publish now content list name.
    */
   public static final String PUBLISH_NOW = "PUBLISH_NOW";
   /**
    * Constant for the un-publish now content list name.
    */
   public static final String UNPUBLISH_NOW = "UNPUBLISH_NOW";
   
   /**
    * Constant for the publish now content list name.
    */
   public static final String STAGING_PUBLISH_NOW = "STAGING_PUBLISH_NOW";
   /**
    * Constant for the un-publish now content list name.
    */
   public static final String STAGING_UNPUBLISH_NOW = "STAGING_UNPUBLISH_NOW";
   
   /**
    * Contstant for the incremental generator
    */
   public static final String INCREMENTAL_GENERATOR = "Java/global/percussion/system/sys_IncrementalGenerator";
   /**
    * Constant for the contentChangeType param name for incremental publish
    */
   public static final String INCREMENTAL_CHANGETYPE_PARAM_NAME = "contentChangeType";
   /**
    * contentChangeType param value for live incremental publish
    */
   public static final String INCREMENTAL_CHANGETYPE_LIVE = "PENDING_LIVE";

   /**
    * contentChangeType param value for staged incremental publish
    */
   public static final String INCREMENTAL_CHANGETYPE_STAGED = "PENDING_STAGED";
   
   public static String EDITION_TASK_EXT_NAME = "Java/global/percussion/task/perc_AntEditionTask";
   public static String AMAZONS3_EDITION_TASK_EXT_NAME = "Java/global/percussion/task/perc_AmazonS3EditionTask";
   public static String WF_EDITION_TASK_EXT_NAME = "Java/global/percussion/task/perc_WorkflowEditionTask";
   public static String STAGING_EDITION_TASK_EXT_NAME = "Java/global/percussion/task/perc_StagingPostEditionTask";
   public static final String LIVE_WORKFLOW_TRANSITION = "forcetolive";
   public static final String PENDING_WORKFLOW_STATE = "Pending";
   public static final String CONTENT_LIST_TEMPLATE_EXPANDER = "Java/global/percussion/system/perc_ResourceTemplateExpander";
   public static final String EXPANDER_TEMPLATE_PARAM_NAME = "resourceId";
   public static final String EXPANDER_TEMPLATE_XML_VALUE = "percSystem.pageXml";
   public static final String EXPANDER_TEMPLATE_DB_VALUE = "percSystem.pageDatabase";
   
   /**
    * Public Item Filter name
    */
   private static final String PERC_PUBLIC_ITEM_FILTER_NAME = "perc_public";

   private static final String PERC_STAGING_ITEM_FILTER_NAME = "perc_staging";

   public static void createIncrementalEdition(IPSSite site, PSPubServer pubServer, boolean isDefaultServer) throws PSNotFoundException {
       Validate.notNull(site);
       Validate.notNull(pubServer);
      
       // incremental, no unpublish
       String suffix = createSiteSuffix(site);
       String edtnSuffix = INCREMENTAL;
       if(PSPubServer.STAGING.toString().equals(pubServer.getServerType())){
          suffix += "STAGING";
          edtnSuffix = "STAGING_" + edtnSuffix;
       }
       IPSPublishingWs publishWs = PSPublishingWsLocator.getPublishingWebservice();
       IPSContentList incrementalCList = publishWs.loadContentList(createName(pubServer.getName(), suffix + "_" + INCREMENTAL));
       createEdition(site, edtnSuffix, "Incremental publish for publish server", PSEditionType.AUTOMATIC,
               IPSEdition.Priority.LOWEST, new IPSContentList[] {incrementalCList}, pubServer, true, isDefaultServer);
   }

   /**
    * Constant for the incremental content list name.
    */
   public static final String INCREMENTAL = "INCREMENTAL";

   public static final String STAGING_INCREMENTAL = "STAGING_INCREMENTAL";

}
