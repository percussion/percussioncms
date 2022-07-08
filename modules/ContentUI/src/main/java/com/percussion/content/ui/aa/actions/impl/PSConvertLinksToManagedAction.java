/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSInlineLinkField;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.server.webservices.PSWebServicesRequestHandler;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.services.notification.filemonitor.IPSFileMonitorService;
import com.percussion.services.notification.filemonitor.PSFileMonitorServiceLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSUnknownContentTypeException;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.security.IPSSecurityDesignWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This class implements an action that accepts an html body fragment that 
 * it analyzes for unmanaged links. If any are found, they will be processed
 * based on the configuration of the autoLinkGeneration.properties file.
 * 
 * See the spec for story CMS-24 for more details.
 * 
 * @author PaulHoward
 */
public class PSConvertLinksToManagedAction extends PSAAActionBase implements IPSNotificationListener
{
   /**
    * Id used to represent a non-existent ctype.
    */
   private static final long INVALID_CTYPE_ID = -1;
   
   /**
    * Never <code>null</code> after class init.
    */
   private static Log ms_log = LogFactory.getLog(PSConvertLinksToManagedAction.class);

   /**
    * Used to load templates. Never <code>null</code> after instance init.
    */
   private IPSAssemblyService assemblyService = PSAssemblyServiceLocator.getAssemblyService();
   
   /**
    * This is really here for testing purposes, but could be used if a purpose
    * arises. 
    * 
    * @param configProps Never <code>null</code>. This class takes ownership
    * of the map. The caller should not change it after passed to this method.
    */
   public PSConvertLinksToManagedAction(Properties configProps)
   {
      if (configProps == null)
         throw new IllegalArgumentException("configProps cannot be null");
      loadProperties(configProps);
   }
   
   /**
    * Loads the configuration from disk and registers for notifications if the
    * file changes.
    */
   public PSConvertLinksToManagedAction()
   {
      loadProperties(null);
      try
      {
         IPSFileMonitorService fileMon = PSFileMonitorServiceLocator.getFileMonitorService();
         fileMon.monitorFile(config.getDefaultConfigFile());

         IPSNotificationService svc = PSNotificationServiceLocator.getNotificationService();
         svc.addListener(EventType.FILE, this);
      }
      catch (RuntimeException e)
      {
         //don't want to stop server startup over this, just log
         ms_log.warn(
            "Error occurred trying to register to watch auto-managed-link properties file. Automatic reload disabled.", 
            e);
      }
   }

   /**
    * Key is the lower-cased 'Published URL' value, with the leading
    * <b>{scheme}://<b> part removed and always with a trailing slash, value is the
    * associated site. A siteinfo may appear multiple times in the map if the
    * site has been configured w/ aliases.
    */
   private Map<String, SiteInfo> siteInfos = new HashMap<String, SiteInfo>();
   
   /**
    * Contains all keys in {@link #siteInfos}, ordered from longest to shortest.
    * Length is determined by the # of slashes (path separators.)
    */
   private List<String> orderedDomainNames = new ArrayList<String>();
      
   /**
    * A local container
    * @author PaulHoward
    *
    */
   private class SiteInfo
   {
      /**
       * The repository path to the root of the site, e.g. //Sites/cdc.gov, no trailing slash.
       */
      private String rxVirtualPath;
      
      /**
       * The site's authority, as extracted from the 'Published Url' site property. Never blank.
       * Either a server name or server:port.
       */
      private String authority;
      
      /**
       * The value of the "Published Url" site property. Never blank. No trailing slash.
       */
      private String publishedUrlPath;
      
      public SiteInfo(String domainName, String rxVirtualPath, String publishedUrlPath)
      {
         this.authority = domainName;
         this.rxVirtualPath = stripTrailingSlash(rxVirtualPath);
         this.publishedUrlPath = stripTrailingSlash(publishedUrlPath);
      }

      /**
       * @param path Assumed not <code>null</code>.
       * @return The supplied path with no trailing slash. 
       */
      private String stripTrailingSlash(String path)
      {
         if (path.endsWith("/"))
            return path.substring(0, path.length()-1);
         return path;
      }
   }

   /**
    * The configuration options for auto generating links. Created by {@link #loadProperties(Properties)},
    * then never <code>null</code>.
    */
   private PSAutoLinkGenerationProperties config;
   
   /**
    * Loads all properties and validates them against this server, discarding any that
    * don't exist or fail validation (and logging it.)
    * 
    * @param configProps Assumed not <code>null</code>.
    */
   private void loadProperties(Properties configProps)
   {
      siteInfos.clear();
      orderedDomainNames.clear();
      config = new PSAutoLinkGenerationProperties(configProps);
      IPSSiteManager siteManager = PSSiteManagerLocator.getSiteManager();
      
      //validate the sites and content types
      Collection<PSPair<String, Set<String>>> sitesAndAliases = config.getManagedSitesAndAliases();
      ms_log.info("Validating auto link generation properties...");
      for (PSPair<String, Set<String>> pair : sitesAndAliases)
      {
         String siteName = pair.getFirst();
         String siteUrl = "";
         try
         {
            IPSSite site = siteManager.loadSite(siteName);
            siteUrl = site.getBaseUrl();
            if (StringUtils.isBlank(siteUrl) || StringUtils.isBlank(site.getFolderRoot()))
            {
               ms_log.warn("Either the base URL or the folder root for the site is missing, ignoring " + siteName);
               continue;
            }
            if (!isHttpScheme(siteUrl))
            {
               ms_log.warn("Published url doesn't start with http:// or https://, ignoring " + siteName);
               continue;
            }

            String subUrl = siteUrl.substring(siteUrl.indexOf('/')+2);
            if (!subUrl.endsWith("/"))
               subUrl = subUrl + "/";
            String path;
            int pathStart = subUrl.indexOf('/');
            if (pathStart >= 0)
               path = subUrl.substring(pathStart);
            else
               path = "";
            if (!path.endsWith("/"))
               path = path + "/";
            
            Set<String> fullAliases = new HashSet<String>();
            Set<String> aliases = new HashSet<String>(pair.getSecond());
            fullAliases.add(subUrl);
            for (String alias : aliases)
            {
               if (alias.contains("/")) {
                  if (!alias.endsWith("/")) 
                       alias=alias+"/";
                  fullAliases.add(alias);
               }
               else
                  fullAliases.add(alias + path);
            }
            String virtualPath = site.getFolderRoot().trim();
            if (virtualPath.endsWith("/"))
               virtualPath = virtualPath.substring(0, virtualPath.length()-1);
            //only pass the host:port part of the subUrl
            SiteInfo info = new SiteInfo(subUrl.substring(0, subUrl.indexOf('/')), virtualPath, siteUrl);
            for (String alias : fullAliases)
            {
               String lc = alias.toLowerCase();
               siteInfos.put(lc, info);
               orderedDomainNames.add(lc);
            }
         }
         catch (PSNotFoundException e)
         {
            // log and continue
            ms_log.warn(MessageFormat.format("Rhythmyx site not found, ignoring {0}", siteName));
         }
      }
      
      Collections.sort(orderedDomainNames, new Comparator<String>()
      {
         /**
          * Results in descending ordering by # of path parts.
          */
         public int compare(String s1, String s2)
         {
            int size1 = getPathPartCount(s1);
            int size2 = getPathPartCount(s2);
            return size1 == size2 ? 0 : (size1 < size2 ? 1 : -1);
         }
         
         /**
          * Counts how many parts in the supplied path.
          * @param s Assumed not <code>null</code>.
          * @return A value >= 0 depending on how many slashes appear in s.
          */
         private int getPathPartCount(String s)
         {
            StringTokenizer toker = new StringTokenizer(s, "/");
            return toker.countTokens();
         }
      });
      
      if (siteInfos.isEmpty())
      {
         ms_log.info("No valid sites found. Auto link generation is disabled.");
      }
      
      if (null == config.getManagedExternalLinkContentTypeName() && null == config.getExternalLinkContentTypeName())
      {
         ms_log.info("No content types specified. Auto link generation is disabled.");
         siteInfos.clear();
      }
      
      if (null == config.getTemplateName())
      {
         ms_log.info("No templateName specified. Auto link generation is disabled.");
         siteInfos.clear();
      }

      ms_log.info("Finished validating properties.");
   }

   /**
    * Reloads the configuration when the file changes.
    * @see IPSNotificationListener
    */
   public void notifyEvent(PSNotificationEvent notification)
   {
      if (((File)notification.getTarget()).getName().equals(config.getDefaultConfigFile().getName()))
         loadProperties(null);
   }
   
   // see interface for more detail
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      String result = null;
      try
      {
         String content = (String) getParameter(params, "content");
         if (content == null)
            content = "";
         if (!isEnabled())
            throw new PSAAClientActionException("Automatic link generation is disabled, content is unchanged.");

         IPSAssemblyTemplate template;
         try
         {
            template = assemblyService.findTemplateByName(config.getTemplateName());
         }
         catch (PSAssemblyException e1)
         {
            throw new PSAAClientActionException(
                  MessageFormat.format("Automatic link generation disabled - configured template not found - {0}", 
                        config.getTemplateName()));
         }
         int templateId = template.getGUID().getUUID();
         
         String communityName = config.getCommunityName();
         String communityId = null;
         if (communityName != null)
         {
            IPSSecurityDesignWs sds = PSSecurityWsLocator.getSecurityDesignWebservice();
            List<IPSCatalogSummary> sums = sds.findCommunities(communityName);
            if (sums.size() == 1)
               communityId = String.valueOf(sums.get(0).getGUID().getUUID());
            else
            {
               throw new PSAAClientActionException(
                     MessageFormat.format("Automatic link generation disabled - configured community not found - {0}", 
                           config.getCommunityName()));
            }
         }

         content = URLDecoder.decode(content, "UTF-8");

         /*
          * in the pair - the first is the original href, the 2nd is the
          * message, either why it was skipped, or the full path to the 
          * linked item
          */
         List<PSPair<String, String>> skippedLinkMessages = new ArrayList<PSPair<String, String>>();
         List<PSPair<String, String>> externalLinkMessages = new ArrayList<PSPair<String, String>>();
         List<PSPair<String, String>> managedAutoLinkMessages = new ArrayList<PSPair<String, String>>();
         List<PSPair<String, String>> errorMessages = new ArrayList<PSPair<String, String>>();

         Document doc = Jsoup.parseBodyFragment(content);
         Elements links = getEligibleATags(doc);
         int managedLinkCount = 0;

         int contentId = getIntParameter(params, IPSHtmlParameters.SYS_CONTENTID, -1);
         int folderId = getIntParameter(params, IPSHtmlParameters.SYS_FOLDERID, -1);
         
         /*
          * If a valid //Sites path, then itemPath will contain it, otherwise it will be null.
          */
         String itemPath = null;
         PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
         PSServerFolderProcessor folderProc = new PSServerFolderProcessor();//req, null);
         int objId = -1;
         if (folderId < 0 && contentId > 0)
         {
            objId = contentId;
         }
         else if (folderId > 0)
         {
            objId = folderId;
         }
         if (objId > 0)
         {
            String[] paths = folderProc.getFolderPaths(new PSLocator(objId));
            if (paths.length == 1 && paths[0].startsWith("//Sites"))
               itemPath = paths[0];
         }
         
         for (Element link : links)
         {
            String linkHref = link.attr("href");
            PSPair<LinkType, String> linkType = getLinkType(link, itemPath, contentId);
            String errorMessage = "";
            long contentTypeId = INVALID_CTYPE_ID;
            String contentTypeName = null;
            try
            {
               StringBuilder buf = new StringBuilder();

               if (linkType.getFirst() == LinkType.Managed)
               {
                  managedLinkCount++;
                  continue;
               }
               else if (linkType.getFirst() == LinkType.Unsupported)
               {
                  skippedLinkMessages.add(new PSPair<String, String>(linkHref, linkType.getSecond()));
                  continue;
               }
               else if (linkType.getFirst() == LinkType.ManagedExternalLink)
               {
                  contentTypeName = config.getManagedExternalLinkContentTypeName();
                  contentTypeId = getContentTypeId(contentTypeName);
                  if (contentTypeId == INVALID_CTYPE_ID)
                  {
                     skippedLinkMessages.add(new PSPair<String, String>(linkHref, 
                           "No managedAutoLink content type configured"));
                     continue;
                  }
               }
               else if (linkType.getFirst() == LinkType.ExternalLink)
               {
                  contentTypeName = config.getExternalLinkContentTypeName();
                  contentTypeId = getContentTypeId(contentTypeName);
                  if (contentTypeId == INVALID_CTYPE_ID)
                  {
                     skippedLinkMessages.add(new PSPair<String, String>(linkHref, 
                           "No externalLink content type configured"));
                     continue;
                  }
               }

               errorMessage = "Failed while creating item: ";
               int dependentContentId = maybeCreateDependentItem(linkType.getFirst(), folderProc, contentTypeName, 
                     itemPath, linkHref, link.text(), communityId, buf, req);
               errorMessage = "Failed while creating inline template: ";
               String replacementLinkText = createReplacementText(dependentContentId, contentTypeId, templateId, 
                     linkHref, link.html());
               replaceExistingLink(link, replacementLinkText);
               if (linkType.getFirst() == LinkType.ManagedExternalLink)
                  managedAutoLinkMessages.add(new PSPair<String, String>(linkHref, buf.toString()));
               else
                  externalLinkMessages.add(new PSPair<String, String>(linkHref, buf.toString()));
            }
            catch (Exception e)
            {
               String msg = "";
               if (e instanceof PSErrorResultsException)
               {
                  PSErrorResultsException ere = (PSErrorResultsException) e;
                  for (Object o : ere.getErrors().values())
                     msg = msg + ((Exception) o).getLocalizedMessage();
               }
               else
                  msg = e.getLocalizedMessage();
               errorMessages.add(new PSPair<String, String>(linkHref, errorMessage + msg));
            }
         }
         content = doc.body().html();
         JSONObject obj = new JSONObject();
         obj.append("content", content);
         obj.append("message", buildFinalMessage(links.size(), managedLinkCount, managedAutoLinkMessages, 
               externalLinkMessages, skippedLinkMessages, errorMessages));
         result = obj.toString();
         return new PSActionResponse(result, PSActionResponse.RESPONSE_TYPE_JSON);
      }
      catch (Exception e)
      {
         if (e instanceof PSAAClientActionException)
            throw (PSAAClientActionException) e;
         else
            throw new PSAAClientActionException(e);
      }
   }
   
   /**
    * An eligible link is one that is not currently managed and is not a child
    * of a managed element.
    * @param doc Assumed not <code>null</code>.
    * @return Never <code>null</code>, may be empty.
    */
   private Elements getEligibleATags(Document doc)
   {
      Elements links = doc.getElementsByTag("a");
      Elements managed = doc.getElementsByAttributeValueMatching(IPSHtmlParameters.SYS_DEPENDENTVARIANTID, "\\d+");
      
      Elements results = new Elements();
      for (Element link : links)
      {
         boolean match = false;
         for (Element elem : managed)
         {
            //is it already managed directly
            if (link.equals(elem))
            {
               match = true;
               break;
            }
         }
         //is it part of managed template content
         Elements ancestors = link.parents();
         for (Element ancestor : ancestors)
         {
            for (Element elem : managed)
            {
               //is it already managed directly
               if (elem.equals(ancestor))
               {
                  match = true;
                  break;
               }
            }
            if (match)
               break;
         }
         if (!match)
            results.add(link);
      }
      return results;
   }

   /**
    * Creates a new link in the doc that currently owns the supplied link, replacing the supplied one.
    * @param link Assumed not <code>null</code>.
    * @param replacementLinkText The html text to use to create the link tag.
    */
   private void replaceExistingLink(Element link, String replacementLinkText)
   {
      link.after(replacementLinkText);
      link.remove();
   }

   /**
    * Creates the html text for the newly created, managed link. Ephox specific elements may be added
    * to the generated link, depending on various factors.
    * 
    * @param dependentContentId The id of the newly created or existing target item of the link.
    * @param contentTypeId The id of the content type of the target item.
    * @param templateId The id of the template used to render the inline template.
    * @param linkHref The href attribute value of the original link, before conversion.
    * @param linkContent The content of the original link, before conversion.
    * @return The html, as a string that represents the newly created link.
    * @throws PSAAClientActionException
    */
   private String createReplacementText(int dependentContentId, long contentTypeId, int templateId, String linkHref, 
         String linkContent) 
         throws PSAAClientActionException
   {
      // this is <String, Object> instead of <String, String> so it can be passed to other actions
      Map<String, Object> attributes = new HashMap<String, Object>();
      
      //inline slot
      int slotId = 105;
      
      PSGetSnippetContentAction templateGenerator = new PSGetSnippetContentAction();
      attributes.put(OBJECT_ID_PARAM, PSAAObjectId.createObjectString(dependentContentId, templateId,
            contentTypeId, slotId));
      PSActionResponse resp = templateGenerator.execute(attributes);
      String templateContent = resp.getResponseData();

      attributes.clear();
      attributes.put(IPSHtmlParameters.SYS_DEPENDENTVARIANTID, String.valueOf(templateId));
      attributes.put(IPSHtmlParameters.SYS_DEPENDENTID, String.valueOf(dependentContentId));
      attributes.put(IPSHtmlParameters.SYS_SITEID, "");
      attributes.put(IPSHtmlParameters.SYS_FOLDERID, "");
      attributes.put(PSInlineLinkField.RX_INLINESLOT, String.valueOf(slotId));
      attributes.put(PSInlineLinkField.RX_INLINETYPE, "rxvariant");
      attributes.put("contenteditable", "false");

      attributes.put(PSInlineLinkField.RX_SELECTEDTEXT, encodeURIComponent(linkContent));
      String anchor = getAnchor(linkHref);
      if (anchor != null)
         attributes.put(PSInlineLinkField.RX_ANCHORTEXT, anchor);
      Element body = ((Document)Jsoup.parse(templateContent)).body();
      Element template = body.child(0);
      Set<String> cssClasses = new HashSet<String>();
      
      if (isAllowTrueInlineTemplates())
      {
         if (template.isBlock())
         {
            cssClasses.add(INLINE_VARIANT_CLASS_NAME);
         }
         else
         {
            //add surrounding image tags for ephox
            String imageTag = "<img alt=\"@inlinemarker@\" width=\"9\" height=\"8\" src=\"{0}\"/>";
            template.before(MessageFormat.format(imageTag, "../sys_resources/images/inlinestart.png"));
            template.after(MessageFormat.format(imageTag, "../sys_resources/images/inlineend.png"));
            
            // added to have the tinymce include the arrow images surrounding the link to true inline variant
            cssClasses.add(INLINEVARIANT_CLASS_NAME);
            
         }
      }
      else
      {
         cssClasses.add(INLINE_VARIANT_CLASS_NAME);
         template = template.wrap("<div></div>");
         //(ph) not sure why this is added, but that's what rx_ephox.js does
         attributes.put("style", "display: inline;");
      }
      
      //fixup the outer element
      for (String cssName : cssClasses)
         template.addClass(cssName);
      for (String key : attributes.keySet())
      {
         template.attr(key, attributes.get(key).toString());
      }
      return body.html();
   }

   /**
    * Extracts the anchor tag from the supplied link and returns it.
    * @param linkHref Anything allowed.
    * @return Either the anchor text, or <code>null</code> if non-blank anchor is not found.
    * The leading hash is not included in the result.
    */
   private String getAnchor(String linkHref)
   {
      if (StringUtils.isBlank(linkHref))
         return null;
      
      int pos = linkHref.indexOf('#');
      if (pos < 0)
         return null;
      String anchorText = linkHref.substring(pos+1);
      if (StringUtils.isBlank(anchorText))
         return null;
      return anchorText;
   }

   /**
    * The name of the class assigned to inline, block templates.
    */
   private static String INLINE_VARIANT_CLASS_NAME = "rx_ephox_inlinevariant";
   
   /**
    * The name of the class assigned to inline, block templates in span tag.
    */
   private static String INLINEVARIANT_CLASS_NAME = "rx_inlinevariant";
   
   /**
    * Determines if auto link generation is configured in such a way 
    * as to be enabled.
    * @return <code>true</code> if 
    */
   private boolean isEnabled()
   {
      return siteInfos.size() > 0;
   }

   /**
    * Attempts to find an item in the system at the path specified by
    * <code>linkHref</code>. If it does, returns information for that item.
    * Otherwise, creates a new item of the specified type.
    * 
    * @param linkType The type of the link as determined by
    * .
    * @param folderProc Assumed not <code>null</code>.
    * @param contentTypeName The name of the ctype you want to create. Will look
    * up the id based on the name.
    * @param itemFolderPath The parent folder of the item containing the link,
    * or <code>null</code> if the item is not under //Sites. Only used for
    * site/page-relative links. If <code>null</code>,
    * @param linkHref The value of the link's href. Expected to start with
    * http[s]// or be site/page relative.
    * @param linkContent
    * @param communityId Either a valid, numeric id or <code>null</code> to
    * use the user's community.
    * @param folderPathBuf The full path of the item is appended to this buffer.
    * Assumed not <code>null</code>.
    * @param req Assumed not <code>null</code>.
    * 
    * @return The contentId of the dependent item.
    * @throws PSErrorException
    * @throws PSUnknownContentTypeException
    * @throws PSErrorResultsException
    * @throws MalformedURLException If the href isn't a recognized url format.
    * @throws PSException If checkin fails. 
    */
   private int maybeCreateDependentItem(LinkType linkType, PSServerFolderProcessor folderProc, String contentTypeName, 
         String itemFolderPath, String linkHref, String linkContent, String communityId, StringBuilder folderPathBuf,
         PSRequest req) 
         throws PSUnknownContentTypeException, PSErrorException, PSErrorResultsException, 
            MalformedURLException, PSException
   {
      if (isRelative(linkHref))
      {
         assert(itemFolderPath != null);
         
         //build a fully qualified path - 
         PSPair<String, SiteInfo> siteInfo = getSiteInfo(itemFolderPath);
         //re-assign because we want to use the fully qualified name for relative links
         linkHref = createFullUrl(itemFolderPath, linkHref, siteInfo.getSecond());
      }
      
      URL url;
      //will have the trailing slash
      String parentFolderPath = null;
      url = new URL(linkHref);
      String itemName;
      boolean externalLink = linkType == LinkType.ExternalLink;
      boolean useDefaultNames = false;
      if (externalLink)
      {
         PSPair<String, String> pathParts = splitPath(url);
         String suffix = "";
         if (StringUtils.isNotBlank(url.getQuery()))
         {
            /* We arbitrarily take the first 8 chars of the SHA. Should be 
             * enough to prevent collisions.
             */
            suffix = "." + ShaSum(url.getQuery()).substring(1, 8);
         }
         parentFolderPath = "//Folders/external/" + url.getHost() + pathParts.getFirst();
         String filename = pathParts.getSecond();
         if (StringUtils.isBlank(filename))
            filename = url.getAuthority().replace(':', '.');
         itemName = filename + suffix;
      }
      else
      {
         PSPair<String, String> pathParts = splitPath(url);
         PSPair<String, SiteInfo> pair = getSiteInfo(linkHref);
         String path = pathParts.getFirst();
         path = path.substring(pair.getFirst().substring(pair.getFirst().indexOf('/')).length()-1);
         parentFolderPath = "//Folders" + pair.getSecond().rxVirtualPath.substring("//Sites".length()) + path;
         itemName = pathParts.getSecond();
         if (StringUtils.isBlank(itemName))
         {
            useDefaultNames = true;
            itemName = config.getDefaultPageName();
            if (itemName == null)
               itemName = DEFAULT_PAGE_NAME;
         }
      }
      
      StringBuilder siteItemNameBuf = new StringBuilder(itemName);
      if (!externalLink)
      {
         //does the item already exist?
         String sitePath = "//Sites" + parentFolderPath.substring("//Folders".length());
         int cid = findUniqueName(folderProc, sitePath, siteItemNameBuf, useDefaultNames, -1);
         if (cid != -1)
         {
            folderPathBuf.append(sitePath + siteItemNameBuf);
            return cid;
         }
      }

      StringBuilder itemNameBuf = new StringBuilder(itemName);
      int cid = findUniqueName(folderProc, parentFolderPath, itemNameBuf, externalLink ? false : true, 
            externalLink ? 1 : -1);
      folderPathBuf.append(parentFolderPath + itemNameBuf.toString());
      if (cid != -1)
         return cid;
      
      //create new item
      IPSContentWs cws = PSContentWsLocator.getContentWebservice();
      List<PSCoreItem> items = cws.createItems(contentTypeName, 1);
      
      PSCoreItem item = items.get(0);
      setField(item, IPSHtmlParameters.SYS_TITLE, itemNameBuf.toString());
      setField(item, "displaytitle", StringUtils.isBlank(linkContent) ? itemName : linkContent);
      setField(item, "url", StripAnchor(linkHref));
      if (communityId != null)
         setField(item, IPSHtmlParameters.SYS_COMMUNITYID, communityId);
      
      /*
       * I am bypassing cws's capability to check-in because it requires that
       * you be in the same community, which may not be the case here.
       */
      List<IPSGuid> results = cws.saveItems(items, false, false);
      checkInNoCommunityCheck(req, results.get(0).getUUID());

      cws.addFolderTree(parentFolderPath);
      cws.addFolderChildren(cws.getIdByPath(parentFolderPath), Collections
            .singletonList(results.get(0)));
      
      if (config.getWorkflowTriggerName() != null)
      {
         PSWebserviceUtils.transitionItem(results.get(0).getUUID(), config.getWorkflowTriggerName(), 
               "Transitioned by system when managed item was created by system.", null);
      }
      
      return results.get(0).getUUID();
   }

   /**
    * The name to use if a url doesn't have a filename part and a default
    * page name was not configured.
    */
   private static final String DEFAULT_PAGE_NAME = "index.html";
   
   /**
    * Attempts to load the summary for the item with the given name in the
    * specified folder. If the object is a folder, adds a monotonically
    * increasing number until a name that doesn't match a folder is found.
    * 
    * @param folderProc Assumed not <code>null</code>.
    * @param parentPath The full path to the item. Assumed not <code>null</code>
    * or empty.
    * @param itemNameBuf The base name of the item. This buffer will be modified
    * to indicate the final unique name that was found.
    * @param useDefaultNames If <code>true</code>, use the list of names specified in the
    * config file rather than the name supplied in itemNameBuf when looking for a match.
    * If a match is not found, the name supplied in itemNameBuf will be used as the final
    * name.
    * @param depth Used for recursion. If you want to find a unique name, pass 1
    * for the first call, otherwise, pass -1 if you just want to try once.
    *
    * @return The contentId of a matching item, or -1. In either case, the value
    * in itemNameBuf will contain a name that is unique. e.g. foo.html.1
    * @throws PSCmsException If any errors when attempting to load summaries.
    * @throws RuntimeException If depth is -1 and a name conflict occurs with a folder.
    */
   private int findUniqueName(PSServerFolderProcessor folderProc, String parentPath, StringBuilder itemNameBuf, 
         boolean useDefaultNames, int depth) throws PSCmsException
   {
      Collection<String> pathsToCheck = new ArrayList<String>();
      if (useDefaultNames)
      {
         Collection<String> pageNames = config.getDefaultPageNames();
         for (String p : pageNames)
            pathsToCheck.add(parentPath + p);
      }
      
      for (String path : pathsToCheck)
      {
         //TODO - find way to make it happen in cache (Ben?)
         PSComponentSummary sum = folderProc.getSummary(path);
         if (sum != null && sum.getObjectType() == 1)
         {
            itemNameBuf.setLength(0);
            itemNameBuf.append(path.substring(path.lastIndexOf('/')+1));
            return sum.getContentId();
         }
      }
      
      String fullPath = parentPath + itemNameBuf.toString();
      PSComponentSummary sum = folderProc.getSummary(fullPath);
      int result = -1;
      if (sum != null)
      {
         //where's the constant for object type item (Ben?)
         if (sum.getObjectType() == 1)
            return sum.getContentId();
         else if (depth < 0)
         {
            throw new RuntimeException(MessageFormat.format(
                  "Unable to convert this auto link - encountered name collision with folder {0}", fullPath));
         }
         if (depth > 1)
         {
            //remove the previous suffix
            itemNameBuf.setLength(itemNameBuf.length() - ("." + (depth-1)).length());
         }
         itemNameBuf.append("." + depth);
         result = findUniqueName(folderProc, parentPath, itemNameBuf, false, depth+1);
      }
      return result;
   }
   
   /**
    * Does the equivalent checkin of
    * {@link IPSContentWs#saveItems(List, boolean, boolean)} without requiring
    * user to be in same community.
    * 
    * @param req The current request, assumed not <code>null</code>.
    * @param cId The content Id of the item to checkin.
    * @throws PSException If the checkin fails for any reason.
    */
   private void checkInNoCommunityCheck(PSRequest req, int cId) throws PSException
   {
      // get request and reset parameter left overs from previous calls
      Map<String, Object> oldParams = req.getParameters();
      req.setParameters((Iterator) new HashMap<Object,Object>());
      String contentId = Integer.toString(cId);
      req.setParameter(IPSHtmlParameters.SYS_CONTENTID, contentId);
      PSWebServicesRequestHandler ws = PSWebServicesRequestHandler.getInstance();
      ws.executeCheckInOut(req, IPSConstants.TRIGGER_CHECKIN);
      req.setParameters(oldParams);
   }
   
   /**
    * Takes path from the supplied url and splits the resource name from its
    * folder and returns the parts. Assumes that if no extension is present
    * on the last path part, it is part of the directory name.
    * 
    * @param url Assumed not <code>null</code>.
    * @return The folder with a trailing slash in the first part, the resource
    * name in the second, which may be an empty string.
    */
   private PSPair<String, String> splitPath(URL url)
   {
      String path = url.getPath();
      if (StringUtils.isBlank(path))
         path = "/";
      path = path.trim();
      //strip off trailing slash if there are path parts
      if (path.length() > 2 && path.endsWith("/"))
         path = path.substring(0, path.length()-1);
      int lastSlashPos = path.lastIndexOf("/");
      //the +1 removes the leading slash
      String itemName = path.substring(lastSlashPos+1);
      //the +1 leaves the trailing slash
      path = path.substring(0, lastSlashPos+1);
      if (StringUtils.isNotBlank(itemName) && itemName.indexOf('.') < 0)
      {
         path = path + itemName + "/";
         itemName = "";
      }
      return new PSPair<String, String>(path, itemName);
   }
   
   /**
    * Build a fully qualified http url from the supplied parts. Assumes that the
    * content was originally copied from a page on the website, so the link is
    * relative to the website, not the virtual path of the current parent.
    * 
    * @param itemFolderPath The path of the page containing the link. Assumed does not contain trailing slash.
    * @param linkHref Assumed page or site relative. Assumed not <code>null</code>.
    * @param siteInfo Assumed not <code>null</code>.
    * @return The generated url, or <code>null</code> if the href was not
    * site/page relative.
    */
   private String createFullUrl(String itemFolderPath, String linkHref,
         SiteInfo siteInfo)
   {
      String result = null;
      // site relative
      if (linkHref.startsWith("/"))
      {
         result = siteInfo.publishedUrlPath + linkHref;
      }
      else
      {
         String folderPath = siteInfo.publishedUrlPath
               + itemFolderPath.substring(siteInfo.rxVirtualPath.length(), itemFolderPath.length());
         if (!folderPath.endsWith("/"))
            folderPath = folderPath + "/";
         result = folderPath + linkHref;
      }
      return result;
   }

   /**
    * Removes any anchor part of the supplied url and returns the result.
    * 
    * @param linkHref Assumed not <code>null</code>.
    * @return The supplied link w/o its anchor.
    */
   private String StripAnchor(String linkHref)
   {
      int hashPos = linkHref.indexOf('#');
      if (hashPos < 0)
         return linkHref;
      
      return linkHref.substring(0, hashPos);
   }

   /**
    * @param linkHref Can handle a real link's href or a path that begins
    * //Sites. Assumed not <code>null</code>.
    * @return The first is the domain name, the second is the associated site.
    * May be <code>null</code> if no match is found or the supplied href isn't
    * as described.
    */
   private PSPair<String, SiteInfo> getSiteInfo(String linkHref)
   {
      PSPair<String, SiteInfo> result = null;
      if (isHttpScheme(linkHref))
      {
         String subPath = linkHref.substring(linkHref.indexOf('/')+2).toLowerCase();
         //handle links to default root page
         if (subPath.indexOf('/') < 0)
            subPath = subPath + "/";
         /* 
          * We have to search from the longest name first or we may get the wrong site.
          */
         for (String key : orderedDomainNames)
         {
            if (subPath.startsWith(key))
            {
               result = new PSPair<String, SiteInfo>(key, siteInfos.get(key));
               break;
            }
         }
      } 
      else if (linkHref.startsWith("//Sites/"))
      {
         for (SiteInfo info : siteInfos.values())
         {
            if (linkHref.startsWith(info.rxVirtualPath))
               result = new PSPair<String, SiteInfo>(info.authority, info);
         }
      }
      return result;
   }

   /**
    * Helper method to set the values of {@link IPSHtmlParameters#SYS_TITLE}
    * field and displaytitle filed to the supplied itemTitle on the
    * supplied coreItem, if those fields exist. 
    * 
    * @param value Assumed not <code>null</code>.
    * @param coreItem Assumed not <code>null</code>.
    */
   private boolean setField(PSCoreItem coreItem, String name, String value)
   {
      Iterator<PSItemField> fields = coreItem.getAllFields();
      PSItemField field = null;
      while (fields.hasNext())
      {
         PSItemField fld = fields.next();
         if (fld.getName().equals(name))
         {
            field = fld;
            break;
         }
      }
      if (field != null)
      {
         field.addValue(new PSTextValue(value));
      }
      return field != null;
   }

   /**
    * Creates an SHA-1 string of the supplied text.
    * @param text Assumed not <code>null</code>.
    * @return The 40 char string representing the SHA-1.
    */
   private static String ShaSum(String text)
   {
      try
      {
         MessageDigest md = MessageDigest.getInstance("SHA-1");
         byte[] sha1hash = new byte[40];
         md.update(text.getBytes("UTF-8"), 0, text.length());
         sha1hash = md.digest();
         return byteArray2Hex(md.digest(sha1hash));
      }
      catch (NoSuchAlgorithmException e)
      {
         //shouldn't happen
         throw new RuntimeException(e);
      }
      catch (UnsupportedEncodingException e)
      {
         //shouldn't happen
         throw new RuntimeException(e);
      }
   }

   /**
    * Converts the supplied byte array into a hex string representation of the byte values,
    * 2 chars per byte.
    * @param hash Assumed not <code>null</code>.
    * @return A string of hex chars whose length is 2*hash.length.
    */
   private static String byteArray2Hex(final byte[] hash)
   {
      Formatter formatter = new Formatter();
      for (byte b : hash)
      {
         formatter.format("%02x", b);
      }
      return formatter.toString();
   }

   /**
    * Is the supplied link site or page relative?
    * @param linkHref Anything allowed.
    * 
    * @return <code>true</code> if site or page relative, otherwise <code>false</code>.
    */
   private boolean isRelative(String linkHref)
   {
      return linkHref == null || linkHref.startsWith("/") || !linkHref.contains("/") 
            || !containsScheme(linkHref);
   }

   /**
    * 
    * @param name A content type name. May be <code>null</code>.
    * @return A positive id if a content type exists with the supplied name, 
    * otherwise, -1.
    */
   public static long getContentTypeId(String name)
   {
      PSItemDefManager itemDefManager = PSItemDefManager.getInstance();
      long id = INVALID_CTYPE_ID;
      try
      {
         if (name != null)
            id = itemDefManager.contentTypeNameToId(name);
      }
      catch (PSInvalidContentTypeException e)
      {
         //ignore
      }
      return id;
   }


   /**
    * This is protected only to allow removal of PSServer dependency for unit testing.
    * @return
    */
   protected boolean isAllowTrueInlineTemplates()
   {
      Properties serverProps = PSServer.getServerProps();
      String isAllowValue = serverProps.getProperty("allowTrueInlineTemplates", "false");
      return isAllowValue.equals("true");
   }

   /**
    * Builds a string of all the supplied messages that is appropriate to show
    * to the end user that represents what happened during processing of the
    * links.
    * 
    * @param totalLinkCount Total count of links in the processed html.
    * @param managedLinkCount Total count of links in the processed html that
    * are already being managed.
    * @param managedExternalLinkMessages A message for each external link that
    * was brought under management that is expected to be imported at a later
    * time. The first part is the original href. The 2nd part is the full Rx
    * virtual to the new or existing item.
    * @param externalLinkMessages A message for each external link that was
    * brought under management that is not expected to be imported later. The
    * first part is the original href. The 2nd part is the full Rx virtual to
    * the new or existing item.
    * @param skippedLinkMessages A message for each skipped link. The first part
    * is the original href. The 2nd part is the reason why it was skipped.
    * @param errorMessages A message for each link that caused an error,
    * typically when trying to create the item. The first part is the original
    * href. The 2nd part is the reason why it errored.
    * @return The complete message.
    */
   private String buildFinalMessage(int totalLinkCount, int managedLinkCount, 
         List<PSPair<String, String>> managedExternalLinkMessages, 
         List<PSPair<String, String>> externalLinkMessages,
         List<PSPair<String, String>> skippedLinkMessages,
         List<PSPair<String, String>> errorMessages)
   {
      StringBuilder buf = new StringBuilder();
      if (totalLinkCount == managedLinkCount)
      {
         buf.append("No unmanaged links found.");
         return buf.toString();
      }

      String msg1 = "Total unmanaged links: {0}: converted {1}, skipped {2}, errored {3}\n";
      buf.append(MessageFormat.format(msg1, totalLinkCount - managedLinkCount, 
            managedExternalLinkMessages.size() + externalLinkMessages.size(), skippedLinkMessages.size(),
            errorMessages.size()));

      if (externalLinkMessages.size() > 0)
      {
         String msg2 = "\nCreated or linked to {0} external link item(s) linking to unmanaged site(s):\n";
         buf.append(MessageFormat.format(msg2, externalLinkMessages.size()));
         addMessages(buf, externalLinkMessages);
      }
      
      if (managedExternalLinkMessages.size() >0)
      {
         String msg3 = "\nCreated or linked to {0} auto link item(s) linking to managed site(s):\n";
         buf.append(MessageFormat.format(msg3, managedExternalLinkMessages.size()));
         addMessages(buf, managedExternalLinkMessages);
      }
      
      if (skippedLinkMessages.size() > 0)
      {
         String msg4 = "\nSkipped {0} link(s)\n";
         buf.append(MessageFormat.format(msg4, skippedLinkMessages.size()));
         addMessages(buf, skippedLinkMessages);
      }
      
      if (errorMessages.size() > 0)
      {
         String msg5 = "\nFailed to convert {0} link(s)\n";
         buf.append(MessageFormat.format(msg5, errorMessages.size()));
         addMessages(buf, errorMessages);
      }
      
      buf.append("\n");
      
      return buf.toString();
   }

   /**
    * Takes the 2 values in each entry of the supplied list and appends them to
    * the supplied buffer. Each entry is appended with a trailing newline. If 
    * any entries have the same 2 pair parts, they are combined in a single message
    * and a trailing quantifier is added (e.g. http://www.google.com (//Folders/...) (3)).
    * 
    * @param buf The generated message is appended to this buffer. No leading
    * newline is added before appending.
    * @param messages The first of the pair is the original href, the 2nd is a
    * user message indicating the disposition of the href.
    */
   private void addMessages(StringBuilder buf, List<PSPair<String, String>> messages)
   {
      Map<PSPair<String, String>, Integer> uniqueMessageCounter = new HashMap<PSPair<String,String>, Integer>();
      for (PSPair<String, String> pair : messages)
      {
         Integer counter = uniqueMessageCounter.get(pair);
         if (counter == null)
         {
            counter = 1;
         }
         else
            counter += 1;
         uniqueMessageCounter.put(pair, counter);
      }

      for (PSPair<String, String> pair : messages)
      {
         Integer count = uniqueMessageCounter.remove(pair);
         if (count == null)
            continue;
         String href = StringUtils.isBlank(pair.getFirst()) ? "" : pair.getFirst();
         String template = "      {0} ({1}) {2}\n";
         buf.append(MessageFormat.format(template, href, pair.getSecond(), count > 1 ? "("+count+")" : ""));
      }
   }

   private enum LinkType
   {
      /**
       * Already an Rx link.
       */
      Managed,
      
      /**
       * Expect to be imported at a later date, currently to be managed via an external link ctype.
       */
      ManagedExternalLink,
      
      /**
       * To be managed via an external link ctype.
       */
      ExternalLink,
      
      /**
       * A link that we won't manage.
       */
      Unsupported
   }

   /**
    * Determine the type of the supplied href.
    * 
    * @param link The parsed link. Assumed not <code>null</code>.
    * @param itemFolderPath The path of the folder containing the item
    * containing the link. Will be <code>null</code> if the folder is not
    * under //Sites.
    * @param contentId The id of the item that contains this link, -1 if it
    * doesn't exist yet.
    * 
    * @return The first part is the type of link, the 2nd part is only used when
    * the type is {@link LinkType#Unsupported}, in which case it is a message
    * to why that is suitable to display to the user. Otherwise, it is
    * <code>null</code>.
    */
   private PSPair<LinkType, String> getLinkType(Element link, String itemFolderPath, int contentId)
   {
      String linkHref = link.attr("href");
      LinkType type = LinkType.Unsupported;
      String message = null;

      if (linkHref != null)
         linkHref = linkHref.trim();
      
      if (StringUtils.isBlank(linkHref))
      {
         message = "Empty href";
      }
      else if (linkHref.startsWith("#"))
      {
         message = "Local anchor href";
      }
      else if (StringUtils.isNotBlank(link.attr(IPSHtmlParameters.SYS_DEPENDENTVARIANTID)))
         type = LinkType.Managed;
      else if (!isHttpScheme(linkHref) && containsScheme(linkHref))  
      {
         message = "Unsupported scheme: " + linkHref.substring(0, linkHref.indexOf(":"));
      }
      else if (link.getElementsByTag("img").size() > 0)
      {
         message = "Can't automatically manage links that contain img tags.";
      }
      else if (linkHref.contains("?"))
      {
         type = LinkType.ExternalLink;
      }
      else
      {
         if (isHttpScheme(linkHref))
         {
            PSPair<String, SiteInfo> info = getSiteInfo(linkHref);
            if (info == null)
               type = LinkType.ExternalLink;
            else
               type = LinkType.ManagedExternalLink;
         }
         else
         {
            //site or page relative
            if (contentId == -1)
               message = "Site and page relative links are not supported until the page is saved.";
            else
            {
               //build a fully qualified path - figure out what site the item is in, is it a supported site?
               PSPair<String, SiteInfo> info = getSiteInfo(itemFolderPath);
               if (info == null)
               {
                  type = LinkType.Unsupported;
                  message = "Site and page relative links are not supported in a page that is in an unmanaged site.";
               }
               else
                  type = LinkType.ManagedExternalLink;
            }
         }
      }
      return new PSPair<LinkType, String>(type, message);
   }

   /**
    * @return <code>true</code> if the href starts with http:// or https://, otherwise <code>false</code>.
    */
   private boolean isHttpScheme(String href)
   {
      return href.startsWith("http://") || href.startsWith("https://");
   }

   /**
    * @param href Assumed not <code>null</code>.
    * @return <code>true</code> if this href has a scheme on it, otherwise <code>false</code>.
    */
   private boolean containsScheme(String href)
   {
      // not a perfect impl, but good enough for this
      if (!href.contains("/"))
      {
         return href.contains(":");
      }
      return href.substring(0, href.indexOf('/')).contains(":");
   }

   /**
    * Encodes the passed String as UTF-8 using an algorithm that's compatible
    * with JavaScript's <code>encodeURIComponent</code> function. Returns
    * <code>null</code> if the String is <code>null</code>.
    * 
    * Copied from http://stackoverflow.com/questions/607176/
    *    java-equivalent-to-javascripts-encodeuricomponent-that-produces-identical-output
    * 
    * @param s The String to be encoded
    * @return the encoded String
    */
   public static String encodeURIComponent(String s)
   {
      String result = null;

      try
      {
         result = URLEncoder.encode(s, "UTF-8")
               .replaceAll("\\+", "%20")
               .replaceAll("\\%21", "!")
               .replaceAll("\\%27", "'")
               .replaceAll("\\%28", "(")
               .replaceAll("\\%29", ")")
               .replaceAll("\\%7E", "~");
      }

      // This exception should never occur.
      catch (UnsupportedEncodingException e)
      {
         result = s;
      }

      return result;
   }
}
