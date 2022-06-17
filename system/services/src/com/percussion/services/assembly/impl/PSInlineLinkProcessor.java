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
package com.percussion.services.assembly.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSInlineLinkField;
import com.percussion.cms.PSRelationshipData;
import com.percussion.cms.PSSingleValueBuilder;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.data.PSConversionException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.html.PSHtmlParsingException;
import com.percussion.html.PSHtmlUtils;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.security.SecureStringUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.jexl.PSLocationUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrConfig;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.PSContentMgrOption;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.data.PSFilterItem;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.codec.PSXmlDecoder;
import com.percussion.utils.codec.PSXmlEncoder;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jsr170.IPSPropertyInterceptor;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.percussion.services.assembly.IPSAssemblyErrors.INLINE_LINK_ERROR;
import static com.percussion.services.assembly.IPSAssemblyErrors.INLINE_TEMPLATE_ERROR;
import static com.percussion.services.assembly.IPSAssemblyErrors.INLINE_TEMPLATE_NON_HTML;

/**
 * The inline link processor substitutes assembled templates and links for
 * elements in the body.
 * 
 * @author dougrand
 */
public class PSInlineLinkProcessor implements IPSPropertyInterceptor
{
   private static final String PERC_BROKENLINK = "perc-brokenlink";
   private static final String PERC_NOTPUBLICLINK = "perc-notpubliclink";
   private final PSLocationUtils locationUtils = new PSLocationUtils();
   private final PSInlineLinkProcessor linkProcessor = this;
   /**
    * The context, only initialized if we find inline content.
    */
   private IPSRequestContext requestContext = null;

   /**
    * The relationship data for the parent item, only initialized if we
    * find inline content.
    */
   private PSRelationshipData relationshipData = null;

   /**
    * The filter to use with the links
    */
   private IPSItemFilter itemFilter = null;

   /**
    * Work item being processed
    */
   private IPSAssemblyItem workItem = null;

   private static final Logger log = LogManager.getLogger(IPSConstants.ASSEMBLY_LOG);


   /**
    * A convenient method that checks a server property called  is available with a value of <code>true</code>
    * @return <code>true</code> or false based on the property.
    */
   private boolean doManageAll() {
      String autoProp = "true";
      if(PSServer.getServerProps()!=null)
      {
         autoProp = StringUtils.defaultString(PSServer.getServerProps().getProperty(IPSConstants.SERVER_PROP_MANAGELINKS));
      }
      return "true".equalsIgnoreCase(autoProp);
   }

   /**
    * If the path is not blank and if the do manage all is true and path starts with either /Sites/ or //Sites/ or /Assets/ or //Assets/ then returns true
    * otherwise false.
    * @param path may be blank.
    * @return true if managed otherwise false.
    */
   private boolean isManagableLink(String path)
   {
      return StringUtils.isNotBlank(path) && doManageAll() && (path.startsWith("/Sites/") || path.startsWith("/Assets/") || path.startsWith("//Sites/") || path.startsWith("//Assets/"));
   }

   /**
    * Create the target item for the inline reference that will be used to
    * determine the target url or the target template content.
    *
    * @param depid the dependent item id
    * @param depvariant the dependent item template id
    * @param siteid the site id
    * @param folderid the folder id
    * @param rid the relationship id
    * @param selectedText the selected text passed to the target item as a
    *           parameter.
    * @return the target item, <code>null</code> if the item should be
    *         filtered or does not exist
    */
   private IPSAssemblyItem getTargetItem(String depid, String depvariant,
                                         String siteid, String folderid, String rid, String selectedText)
           throws PSAssemblyException, PSCmsException
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      IPSAssemblyItem sourceitem = linkProcessor.getWorkItem();
      // Check that we have the right content id. We may need to replace it
      // with one from a promotable version
      if (relationshipData == null)
      {
         PSRequest req = PSRequest.getContextForRequest();
         requestContext = new PSRequestContext(req);
         PSLegacyGuid parentguid = (PSLegacyGuid) sourceitem.getId();
         relationshipData = PSSingleValueBuilder.buildRelationshipData(
                 requestContext, parentguid.getLocator());
      }
      depid =
              PSSingleValueBuilder
                      .getCorrectedContentId(requestContext, depid, rid, relationshipData);

      if (StringUtils.isBlank(depid))
         return null; // Item is purged

      int contentid = Integer.parseInt(depid);

      PSComponentSummary sum = cms.loadComponentSummary(contentid);
      if (sum == null)
      {
         return null;
      }

      // Create a guid and filter it to decide whether this item is active
      IPSGuid thisguid = new PSLegacyGuid(contentid, sum
              .getPublicOrCurrentRevision());
      IPSGuid folderguid = null, siteguid = null;
      if (NumberUtils.toInt(folderid) > 0)
         folderguid = new PSLegacyGuid(Integer.parseInt(folderid), 0);
      if (NumberUtils.toInt(siteid)>0)
         siteguid = new PSGuid(PSTypeEnum.SITE, Long.parseLong(siteid));


      IPSFilterItem item = new PSFilterItem(thisguid, folderguid, siteguid);
      List<IPSFilterItem> input = Collections.singletonList(item);
      try
      {
         Map<String, String> params = new HashMap<>();
         params.put(IPSHtmlParameters.SYS_SITEID, siteid);
         IPSItemFilter filter = linkProcessor.getItemFilter();

         List<IPSFilterItem> output = filter.filter(input,
                 params);
         if (output.isEmpty())
         {
            return null;
         }
      }
      catch (PSFilterException e)
      {
         log.error("Problem filtering item for inline link", e);
         return null;
      }

      IPSAssemblyItem targetitem = (IPSAssemblyItem) sourceitem.clone();
      targetitem.setPath(null);
      targetitem.setTemplate(null);
      targetitem.setNode(null);
      targetitem.setParameterValue(IPSHtmlParameters.SYS_VARIANTID, depvariant);
      targetitem.setParameterValue(IPSHtmlParameters.SYS_CONTENTID, depid);
      if(StringUtils.isNotBlank(selectedText))
      {
         targetitem.setParameterValue(PSSingleValueBuilder.INLINE_TEXT,
                 selectedText);
      }
      targetitem.setParameterValue(IPSHtmlParameters.SYS_REVISION, Integer
              .toString(sum.getPublicOrCurrentRevision()));
      targetitem.removeParameterValue(IPSHtmlParameters.SYS_TEMPLATE);
      targetitem.removeParameterValue(IPSHtmlParameters.SYS_PART);
      if (NumberUtils.toInt(folderid)>0)
      {
         targetitem.setParameterValue(IPSHtmlParameters.SYS_FOLDERID, folderid);
         targetitem.setFolderId(Integer.parseInt(folderid));
      }
      else
      {
         targetitem.removeParameterValue(IPSHtmlParameters.SYS_FOLDERID);
         targetitem.setFolderId(-1);
      }
      IPSGuid origSiteId = linkProcessor.getWorkItem().getSiteId();

      if (NumberUtils.toInt(siteid)>0)
      {
         targetitem.setParameterValue(IPSHtmlParameters.SYS_SITEID, siteid);
         if (NumberUtils.toInt(siteid)>0)
            targetitem.setSiteId(siteguid);
      }
      else
      {
         // We are setting to owner site id if we do not have one be careful when merging rhythmyx
         targetitem.setParameterValue(IPSHtmlParameters.SYS_SITEID, Integer.toString(origSiteId.getUUID()));
         targetitem.setSiteId(origSiteId);
      }

      if(origSiteId != null && (siteguid==null || !siteguid.equals(origSiteId)))
      {
         targetitem.setParameterValue(IPSHtmlParameters.SYS_ORIGINALSITEID, Long
                 .toString(origSiteId.longValue()));
      }

      // Propagate sys_command for active assembly for link generation, but
      // not for rendering
      String command = sourceitem.getParameterValue(
              IPSHtmlParameters.SYS_COMMAND, "");
      if (StringUtils.isNotBlank(command))
      {
         targetitem.removeParameterValue(IPSHtmlParameters.SYS_COMMAND);
         targetitem.setParameterValue(IPSHtmlParameters.SYS_MODE,
                 PSLocationUtils.AA_LINK);
      }
      targetitem.normalize();

      return targetitem;
   }

   public String getValidFlag(IPSAssemblyItem target)
   {
      int context = target.getContext();
      if (context == 0)
      {
         return PSSingleValueBuilder.getValidFlag(target.getId().getUUID());
      }
      return "y";
   }

   /**
    * Create a new inline link processor. One instance should be created for
    * each property to be processed.
    * 
    * @param filter the item filter, never <code>null</code>
    * @param workitem the work item being assembled, never <code>null</code>
    */
   public PSInlineLinkProcessor(IPSItemFilter filter,
         IPSAssemblyItem workitem) {
      if (filter == null)
      {
         throw new IllegalArgumentException("filter may not be null");
      }
      if (workitem == null)
      {
         throw new IllegalArgumentException("workitem may not be null");
      }
      itemFilter = filter;
      workItem = workitem;
   }

   public Object translate(Object originalValue)
   {
      if (originalValue == null)
         return null;

      if (originalValue instanceof String)
      {
         if (StringUtils.isBlank((String) originalValue))
         {
            return originalValue;
         }

         try
         {
            return processInlineLinks((String) originalValue);
         }
         catch (Exception e)
         {
            PSTrackAssemblyError
               .addProblem("Problem processing inline links", e);
            PSXmlEncoder enc = new PSXmlEncoder();
            log.warn("Problem processing inline links", e);
            return "<div class='perc-assembly-error'>" +
                    PSI18nUtils
                            .getString("psx_assembly@Error processing inline link") +
                    " " +
                    enc.encode(e.getMessage()) +
                    "<h2 class='perc-assembly-orginal-content'>Original content:</h2>" +
                    enc.encode(originalValue) +
                    "</div>";
         }
      }
      else
      {
         return originalValue;
      }
   }

   /**
    * Do the actual inline link processing by creating a SAX parser and using
    * the {@link PSInlineLinkContentHandler} to do the real work. The handler
    * holds the results and releases them through the
    * {@link PSInlineLinkContentHandler#toString()} method.
    * 
    * @param body the body to be processed, assumed never <code>null</code> or
    *           empty
    * @return the processed body, never <code>null</code> or empty
    */
   private String processInlineLinks(String body)
   {
      try {
         //Don't bother trying to parse if the string doesn't contain html / xml
         if(SecureStringUtils.isHTML(body) || SecureStringUtils.isXML(body)) {
            return PSHtmlUtils.createHTMLDocument(body, StandardCharsets.UTF_8,false,null).body().html();
         }
      }catch (PSHtmlParsingException e){
         log.error("Error parsing content for inline links in Content Type field. Error: {}. The offending source code was: {}",
                 PSExceptionUtils.getMessageForLog(e), body);
      }
      return body;
   }

   /**
    * @return Returns the itemFilter.
    */
   public IPSItemFilter getItemFilter()
   {
      return itemFilter;
   }

   /**
    * @return Returns the workItem.
    */
   public IPSAssemblyItem getWorkItem()
   {
      return workItem;
   }

   /**
    * Initializes the sys_manageLinksConverter UDF and caches it in a member
    * variable.  If any errors occur, they are logged but not propagated.
    */
   protected void initGeneratorUDF()
   {
      try
      {
         PSExtensionRef extRef = new PSExtensionRef("Java/global/percussion/content/sys_manageLinksConverter");
         IPSExtensionManager extMgr = PSServer.getExtensionManager(null);
         m_managedLinkUdf =
                 (IPSUdfProcessor) extMgr.prepareExtension(extRef, null);
      }
      catch (PSNotFoundException | PSExtensionException e)
      {
         log.error("Error initializing sys_manageLinksConverter. Error: {}",
                 PSExceptionUtils.getMessageForLog(e));
      }

   }

   /**
    * Get the ready to use managed links converter UDF which is
    * <em>sys_manageLinksConverter</em>.
    *
    * @return location generator UDF, never <code>null</code>
    */
   public synchronized IPSUdfProcessor getManagedLinkConverterUdf()
   {
      if (m_managedLinkUdf == null)
         initGeneratorUDF();
      return m_managedLinkUdf;
   }

   /**
    * Caches the sys_manageLinksConverter UDF used to convert the new managed links to old style links.
    * Initialized in {@link #initGeneratorUDF()}, never <code>null</code> after
    * that.
    */
   private IPSUdfProcessor m_managedLinkUdf = null;

   protected class InlineLink {

      private boolean isNotPublic;
      private String inlineType;
      private String dependentVariantId;
      private String dependentId;
      private String relationshipId;
      private String siteId;
      private String folderId;
      private String selectedText;
      private String href;
      private String resourceDefinitionId;
      private Map<String, String> overrides;
      private boolean dataAltOverride = false;
      private boolean dataTitleOverride = false;
      private boolean dataDecorativeOverride = false;
      private boolean isUpgradeScenario = false;
      private boolean isBroken = false;
      private String replacementBody = null;
      private PSXmlDecoder enc = new PSXmlDecoder();

      public InlineLink(Attributes attrs)
      {
         super();
         // This block of code is executed for any inline content. If the
         // inline content is a variant, then the state is set to IGNORE
         // so that the inline content of the variant from the body field
         // is swallowed
         inlineType = attrs.get(PSSingleValueBuilder.INLINE_TYPE);
         overrides = new HashMap<>();

         // Get attributes
         dependentVariantId =
                 attrs.get(IPSHtmlParameters.SYS_DEPENDENTVARIANTID);
         dependentId = attrs.get(IPSHtmlParameters.SYS_DEPENDENTID);
         relationshipId = attrs.get(IPSHtmlParameters.SYS_RELATIONSHIPID);

         siteId = attrs.get(IPSHtmlParameters.SYS_SITEID);
         folderId = attrs.get(IPSHtmlParameters.SYS_FOLDERID);
         selectedText = StringUtils.defaultString(attrs
                 .get(PSInlineLinkField.RX_SELECTEDTEXT));
         selectedText = PSSingleValueBuilder
                 .decodeSelectedText(selectedText);
         PSRequest req = PSRequest.getContextForRequest();
         requestContext = new PSRequestContext(req);

         // Get the siteid and folder id from relationship
         Map<String, String> relMap = PSSingleValueBuilder
                 .getSiteAndFolderFromRelationship(relationshipId, dependentId, requestContext);
         // If they exist update the site and folder ids
         siteId = StringUtils.isNotBlank(relMap
                 .get(IPSHtmlParameters.SYS_SITEID)) ? relMap
                 .get(IPSHtmlParameters.SYS_SITEID) : siteId;
         folderId = StringUtils.isNotBlank(relMap
                 .get(IPSHtmlParameters.SYS_FOLDERID)) ? relMap
                 .get(IPSHtmlParameters.SYS_FOLDERID) : folderId;
         // If we do not find the siteid from relationships, use the
         // site id from the assembly workitem

         if (StringUtils.isBlank(siteId))
         {
            siteId = linkProcessor.getWorkItem().getParameterValue(
                    IPSHtmlParameters.SYS_SITEID, "");
         }

         if (!StringUtils.isBlank(attrs.get(PSSingleValueBuilder.DATA_DESCRIPTION_OVERRIDE))) {
            dataAltOverride = Boolean.parseBoolean(attrs.get(PSSingleValueBuilder.DATA_DESCRIPTION_OVERRIDE));
            dataTitleOverride = Boolean.parseBoolean(attrs.get(PSSingleValueBuilder.DATA_TITLE_OVERRIDE));
            isUpgradeScenario = false;
         }
         else if(inlineType.equals("rximage"))
            isUpgradeScenario = true;

         if(!StringUtils.isBlank(attrs.get(PSSingleValueBuilder.DATA_DECORATIVE_OVERRIDE)))
            dataDecorativeOverride = Boolean.parseBoolean(attrs.get(PSSingleValueBuilder.DATA_DECORATIVE_OVERRIDE));

         href = attrs.get(PSSingleValueBuilder.HREF);
         resourceDefinitionId = attrs.get(PSSingleValueBuilder.RESOURCE_DEFINITION_ID);

      }

      public InlineLink(String path, String type) throws PSConversionException, JDOMException, IOException
      {
         super();
         inlineType = type;
         overrides = new HashMap<>();
         IPSUdfProcessor processor = getManagedLinkConverterUdf();
         if (processor == null)
         {
            throw new UnsupportedOperationException("Can't create InlineLink object, the processor is not initialized.");
         }
         Object[] params = new Object[2];

         params[0] = type.equalsIgnoreCase("rxhyperlink") ? "<a perc-managed=\"true\" href=\"" + path + "\">LinkText</a>" : "<img  perc-managed=\"true\" src=\""
                 + path + "\"/>";
         params[1] = "true";
         Map<String,String> props = (Map<String,String>) processor.processUdf(params, requestContext);
         // Get attributes
         dependentVariantId = props.get(IPSHtmlParameters.SYS_DEPENDENTVARIANTID);
         dependentId = props.get(IPSHtmlParameters.SYS_DEPENDENTID);
         PSRequest req = PSRequest.getContextForRequest();
         requestContext = new PSRequestContext(req);

         siteId = linkProcessor.getWorkItem().getParameterValue(IPSHtmlParameters.SYS_SITEID, "");

         href = props.get("path");
      }

      protected String getLink(IPSAssemblyItem assemblyItem) {
         /*
          * Set the new resource definition id here.
          * The resource location scheme generator will use
          * it to load resource definition.
          */
         if (this.resourceDefinitionId != null) {
            requestContext.setParameter(
                    IPSHtmlParameters.PERC_RESOURCE_DEFINITION_ID,
                    this.resourceDefinitionId);
         }
         try {
            return (String) enc.encode(locationUtils.generate(
                    assemblyItem, Long.valueOf(this.dependentVariantId)));
         }
         catch (EncoderException e)
         {
            throw new RuntimeException("Failed to decode link: " + this, e);
         }
         finally {
            if (this.resourceDefinitionId != null)
               requestContext.removeParameter(IPSHtmlParameters.PERC_RESOURCE_DEFINITION_ID);
         }
      }

      protected Map<String, String> getAltTextAndTitleFromAsset(IPSAssemblyItem assemblyItem) {
         Map<String, String> items = new HashMap<>();
         IPSGuid guid = assemblyItem.getId();
         List<IPSGuid> guidList = new ArrayList<>();
         List<Node> nodeList;
         guidList.add(guid);
         IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
         try
         {
            PSContentMgrConfig conf = new PSContentMgrConfig();

            //Don't return the binary value by default.
            conf.addOption(PSContentMgrOption.LOAD_MINIMAL);
            conf.addOption(PSContentMgrOption.LAZY_LOAD_CHILDREN);

            nodeList = mgr.findItemsByGUID(guidList, conf);

            //Add alt and title if available
            if(nodeList.get(0).hasProperty("rx:alttext")) {
               items.put(PSSingleValueBuilder.ALT, nodeList.get(0).getProperty("rx:alttext").getString());
            }
            if(nodeList.get(0).hasProperty("rx:displaytitle")) {
               items.put(PSSingleValueBuilder.TITLE, nodeList.get(0).getProperty("rx:displaytitle").getString());
            }
         }
         catch (RepositoryException e)
         {
            log.error("Unable to get node for alt and title text with ID: {} and error message: {}",
                    assemblyItem.getId(),PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         }

         return items;
      }

      public IPSAssemblyItem getTargetItem() throws PSAssemblyException, PSCmsException, PSORMException, CloneNotSupportedException {
         return linkProcessor.getTargetItem(dependentId, dependentVariantId,
                 siteId, folderId, relationshipId, selectedText);
      }

   }

   protected InlineLink doRxHyperLink(InlineLink link, IPSAssemblyItem target, String jrcPath) {
      if (target != null){
         String newHref = link.getLink(target);
         if (StringUtils.isNotBlank(newHref)) {
            if(link.href != null) {
               int anchorindex = link.href.lastIndexOf("#");
               if(anchorindex != -1) {
                  String anchor = link.href.substring(anchorindex+1);
                  if (!StringUtils.isEmpty(anchor.trim())) {
                     newHref = newHref + "#" + anchor;
                  }
               }
            }
            link.overrides.put(PSSingleValueBuilder.HREF, newHref);
            return link;
         }
      }
      //else the link is broken
      log.debug("Broken Inline link from item {}", link.href);
      String overrideValue = AssemblerInfoUtils.getBrokenLinkOverrideValue(jrcPath);
      link.overrides.put(PSSingleValueBuilder.HREF, overrideValue);
      return link;
   }

   protected InlineLink doRxImage(InlineLink link, IPSAssemblyItem target) throws Exception {

      if (target == null )
      {
         if (linkProcessor.getWorkItem().getContext() > 0)
         {
            return null;
         }
         link.isBroken = true;
         return link;
      }


      link.overrides.put("src", link.getLink(target));

      Map<String, String> altAndTitle;

      // get alt text and title from actual asset itself
      // only if rtw overrides are not set
      if(!link.dataAltOverride || !link.dataTitleOverride) {
         altAndTitle = link.getAltTextAndTitleFromAsset(target);
         link.overrides.put(PSSingleValueBuilder.ALT, altAndTitle.get(PSSingleValueBuilder.ALT));
         link.overrides.put(PSSingleValueBuilder.TITLE, altAndTitle.get(PSSingleValueBuilder.TITLE));
      }

      return link;
   }

   protected InlineLink doRxVariant(InlineLink link, IPSAssemblyItem target) throws Exception {

      if (target != null)
      {
         IPSAssemblyService asm = PSAssemblyServiceLocator
                 .getAssemblyService();
         List<IPSAssemblyItem> listofitems = Collections
                 .singletonList(target);
         List<IPSAssemblyResult> results = asm.assemble(listofitems);
         IPSAssemblyResult result = results.get(0);
         if (result.getStatus() != IPSAssemblyResult.Status.SUCCESS)
         {
            throw new PSAssemblyException(INLINE_TEMPLATE_ERROR);
         }
         else if (!result.getMimeType().startsWith("text/html"))
         {
            throw new PSAssemblyException(INLINE_TEMPLATE_NON_HTML,
                            result.getMimeType());
         }
         else
         {
            // Suppress the underlying data
            link.replacementBody = new String(result.getResultData(),
                    StandardCharsets.UTF_8);
         }
      }
      else
      {
         return null;
      }

      return link;
   }

   private void handleError(Attributes attrs, String replacementbody,
                            Exception e) throws PSAssemblyException {
      log.error("Problem processing inline link for item {} Error: {}", linkProcessor.getWorkItem().getId(),PSExceptionUtils.getMessageForLog(e));
      log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      PSTrackAssemblyError
              .addProblem("Problem processing inline links", e);
      StringBuilder message = new StringBuilder();
      message.append("Problem while processing inline link for item ");
      message.append(linkProcessor.getWorkItem().getId());
      message.append(": ");
      message.append(PSExceptionUtils.getMessageForLog(e));
      message.append(" See the replacement body in the console log. ");
      message.append("The attributes on the affected link were: ");
      int i = 0;
      for (Attribute attr : attrs)
      {

         String name = attr.getKey();
         String value = attr.getValue();
         if (i > 0)
         {
            message.append(",");
         }
         message.append(name);
         message.append("=\"");
         message.append(value);
         message.append("\"");
         i++;
      }

      log.error("Actual replacement body for error was: {}", replacementbody);
      throw new PSAssemblyException(INLINE_LINK_ERROR,e,message.toString());
   }

   public void processATags(Elements elements) {

      IPSUdfProcessor processor = getManagedLinkConverterUdf();

      for(Element e : elements) {
         String path = e.attributes().get(PSSingleValueBuilder.HREF);
         String type = null;
         if (StringUtils.isEmpty(e.attributes().get(PSSingleValueBuilder.ATTR_INLINESLOT)) && isManagableLink(path)) {
            type = "rxhyperlink";
         }


         InlineLink link = null;
         IPSAssemblyItem target = null;
         try {
            link = new InlineLink(path, type);
            target = link.getTargetItem();
            link = doRxHyperLink(link, target, e.attributes().get(PSSingleValueBuilder.JRCPATH));



         if (link != null && StringUtils.isBlank(link.dependentId)) {
            continue; //this is not an inline link
         }

         if (link != null && !link.isBroken && target != null) {
            String flag = getValidFlag(target);
            if (flag.equals("u"))
               link.isBroken = true;
            else if (!flag.equals("y") && !flag.equals("i"))
               link.isNotPublic = true;
         }

         } catch (PSConversionException | IOException | JDOMException | PSCmsException | PSAssemblyException | PSORMException | CloneNotSupportedException ex) {
            ex.printStackTrace();
         }
      }
   }

   public void processIMGTags(Elements elements) {

      IPSUdfProcessor processor = getManagedLinkConverterUdf();

      //Process each img tag in the list
      for (Element e : elements) {

         String type = "rximage";
         InlineLink link = new InlineLink(e.attributes());

         boolean foundClass = false;
         boolean foundAlt = false;
         boolean foundTitle = false;
         boolean foundSrc = false;

         for (Attribute attr : e.attributes()) {
            String name = attr.getKey().toLowerCase();
            if (PSSingleValueBuilder.IGNORED_ATTRIBUTES.contains(name))
               continue;

            // if the values are not the same we assume a previous
            // override value and use that
            // Skip our attributes used for inline links
            String override = "";
            if (link.overrides != null && link.overrides.containsKey(name)) {
               override = link.overrides.get(name);
            }

            //class attribute
            if (name.equals(PSSingleValueBuilder.CLASS)) {
               foundClass = handleClassAttribute(link, attr);
               continue;
            }

            //alt attribute
            if (name.equals(PSSingleValueBuilder.ALT)) {
               foundAlt = handleAltAttribute(link, attr);
               continue;
            }

            //title
            if (name.equals(PSSingleValueBuilder.TITLE)) {
               foundTitle = handleTitleAttribute(link, attr);
               continue;
            }

            //src
            if (name.equals(PSSingleValueBuilder.SRC)) {
               foundSrc = handleSrcAttribute(link, attr, e.attributes().get(PSSingleValueBuilder.JRCPATH), override);
               continue;
            }

         }

         if (!foundTitle)
         {
            if (!link.dataTitleOverride && !link.isUpgradeScenario && !link.dataDecorativeOverride )
               e.attr(PSSingleValueBuilder.TITLE,link.overrides.get(PSSingleValueBuilder.TITLE));
            else
               e.attr(PSSingleValueBuilder.TITLE, "");
         }

         if (!foundAlt)
         {
            if (!link.dataAltOverride && !link.isUpgradeScenario && !link.dataDecorativeOverride)
               e.attr(PSSingleValueBuilder.ALT, link.overrides.get(PSSingleValueBuilder.ALT));
            else
               e.attr(PSSingleValueBuilder.ALT, "");
         }

      }
   }

   private boolean handleSrcAttribute(InlineLink link, Attribute srcAttr, String jcrpath, String override) {
      if(link == null || srcAttr == null)
         throw new IllegalArgumentException();

      boolean ret = false;

         if(override != null && !StringUtils.isEmpty(override)){
            srcAttr.setValue(override);
            ret = true;
         }else {
            if (jcrpath != null && !StringUtils.isEmpty(jcrpath)){
               srcAttr.setValue(jcrpath);
               ret = true;
            }
         }
      return ret;
   }

   /**
    * Process the title attribute.
    * @param link a valid InlineLink
    * @param attr a valid title attribute
    * @return True if the title attribute was processed.
    */
   private boolean handleTitleAttribute(InlineLink link, Attribute attr) {

      if(link==null || attr == null)
         throw new IllegalArgumentException();
      boolean ret = false;

      if(!link.dataDecorativeOverride) {
         if (!link.dataTitleOverride) {
            if (link.overrides != null) {
               String val = link.overrides.get(PSSingleValueBuilder.TITLE);
               if (val != null) {
                  attr.setValue(val);
                  ret = true;
               }
            }
         } else {
            ret = true;
         }
      }else {
         attr.setValue("");
         ret = true;
      }
      return ret;
   }

   /**
    * Process alt attribute
    * @param link a link. not null
    * @param attr an alt attribute. not null.
    * @return true if the alt attribute was processed
    */
   private boolean handleAltAttribute(InlineLink link, Attribute attr) {
      boolean ret = false;

      if(link == null || attr == null)
         throw new IllegalArgumentException();

      if(!link.dataDecorativeOverride) {
         if (!link.dataAltOverride) {
            attr.setValue( link.overrides.get(PSSingleValueBuilder.ALT));
            ret = true;
         }
         else {
            ret = true;
         }
      } else {
         attr.setValue("");
         ret = true;
      }

      return ret;
   }

   /**
    * Handle class attribute
    * @param link An inline link, not null
    * @param attr A class attribute, not null
    * @return True if a class attribute with a value has been found.
    */
   private boolean handleClassAttribute(InlineLink link,Attribute attr ) {

         String existingClass = attr.getValue().trim();
         boolean foundClass = false;

         if(link.isNotPublic)
         {
            if(!existingClass.contains(PERC_NOTPUBLICLINK))
               existingClass += " " + PERC_NOTPUBLICLINK;
         }
         else
            existingClass = StringUtils.remove(existingClass, PERC_NOTPUBLICLINK).trim();

         if(link.isBroken)
         {
            if (!existingClass.contains(PERC_BROKENLINK))
               existingClass += " " + PERC_BROKENLINK;
         }
         else
            existingClass = StringUtils.remove(existingClass, PERC_BROKENLINK).trim();

         if (!StringUtils.isEmpty(existingClass)){
            foundClass = true;
            attr.setValue(existingClass);
         }

         return foundClass;
   }

   public void processTags(Document doc)
   {
      processATags(doc.select("a"));
      processIMGTags(doc.select("img"));
   }



         // At this point we need to decide what to do. If the above was
         // just a reference to an image or link, then the current state
         // will be unmodified (and will be passthrough)
   /*      if (link.replacementBody == null)
         {

            for (int i = 0; i < attrs.getLength(); i++)
            {



               // here we handle the rest of the attributes as normal
               //After new Customization for broken link, we need to set override value
               // as selected by user

               if(PSSingleValueBuilder.HREF.equals(name)){
                  if(override != null)
                     writeToWriter(name, override);
               }else {
                  if (!StringUtils.isBlank(override))
                     writeToWriter(name, override);
                  else
                     writeToWriter(name, attrs.getValue(i));
               }

            }



            if (!foundClass)
            {
               String class_attr = null;
               if (link.isBroken)
               {
                  class_attr = PERC_BROKENLINK;
               } else if (link.isNotPublic)
               {
                  class_attr = PERC_NOTPUBLICLINK;
               }

               if (class_attr!=null)
                  writeToWriter(PSSingleValueBuilder.CLASS, class_attr);
            }

         }
         else if (StringUtils.isNotBlank(link.replacementBody))
         {
            log.debug("Replace: {}", qname);
            // If we get here we had an inline variant and the state will
            // be set to ignore at the end of this block
            // Flush
            m_writer.flush();
            // Parse the replacement body to include in output
            SAXParser parser = PSSaxHelper.newSAXParser(this);
            InputSource source = new InputSource(new StringReader(
                    link.replacementBody));
            try
            {
               // Set the stack so the inline content is parsed and ignored
               // up to the body tag. PIs in the content will still be
               // read by the superclass because we delegate before
               // processing PIs
               pushState(qname, State.IGNORE_TO_BODY);
               parser.parse(source, this);
            }
            finally
            {
               popState();
            }
            setCurrentState(State.IGNORE);
         }
      }
      else if (currentstate.equals(State.PASSTHROUGH))
      {


         m_writer.writeStartElement(qname);
         log.debug("Write Start {} depth {}", qname, m_stateStack.size());
         for (int i = 0; i < attrs.getLength(); i++)
         {
            String name = attrs.getQName(i);
            String value = attrs.getValue(i);
            if (PSSingleValueBuilder.IGNORED_ATTRIBUTES.contains(name))
               continue;

            if (name.equals(PSSingleValueBuilder.CLASS) && value.contains(PERC_BROKENLINK))
            {
               value = StringUtils.replace(value,PERC_BROKENLINK, "").trim();
               if (StringUtils.isBlank(value))
                  continue;
            }

            writeToWriter(name, value);
         }
         */
     // }

}
