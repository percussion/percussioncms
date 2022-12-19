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
import com.percussion.error.PSNotFoundException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.html.PSHtmlParsingException;
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
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Do the actual processing of inline content. Each element is examined for the
 * attribute rxinlineslot. If the attribute exists then the processor handles
 * that element (and contained elements for inline templates) differently.
 * <p>
 * The processor addresses each presented component in turn. It then exhibits
 * one of these behaviors:
 * <ul>
 * <li>Inline links (A and IMG elements): The current element is modified to
 * reference a new generated link. If the reference content item is not valid
 * for the context, the link is removed.</li>
 * <li>Inline templates: The element is passed through, with all content until
 * the end of the element ignored. That content is replaced by the assembly of
 * the template referenced.</li>
 * <li>Regular elements: The element is copied to the output if the handler is
 * in pass through mode, or swallowed in ignore mode.</li>
 * </ul>
 * <p>
 * The handler maintains a stack of elements and states. These enable the
 * processor to adjust the handling as it goes.
 * 
 * @author dougrand
 * 
 */
@SuppressWarnings("unused") 
public class PSInlineLinkContentHandler
{
   private static final String PERC_BROKENLINK = "perc-brokenlink";
   private static final String PERC_NOTPUBLICLINK = "perc-notpubliclink";
   private static final String A_TAG="a";
   private static final String IMG_TAG="img";
   private static final String HREF_ATTR="href";
   private static final String SRC_ATTR="src";
   private static final String RXLINK="rxhyperlink";
   private static final String RXIMAGE="rximage";
   private static final String RXVARIANT="rxvariant";
   /**
    * A filler string that is used to fill the empty elements. So that they
    * are not self closed.
    */
   public static final String RX_FILLER = "##RX_FILLER##";
   
   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(IPSConstants.ASSEMBLY_LOG);

   /**
    * The location utils, used to calculate urls for inline links and such
    */
   private static PSLocationUtils ms_lutils = new PSLocationUtils();


   /**
    * The calling link processor, set in the ctor
    */
   private PSInlineLinkProcessor m_processor = null;
   
   /**
    * The context, only initialized if we find inline content.
    */
   private IPSRequestContext m_context = null;
   
   /**
    * The relationship data for the parent item, only initialized if we
    * find inline content.
    */
   private PSRelationshipData m_relationshipData = null;

   public PSInlineLinkContentHandler() {
   }

   /**
    * Process Inline Links or Images in the document
    * @param htmlDoc
    * @param inlineLinkProcessor
    * @return
    * @throws PSHtmlParsingException
    */

   public String processDocument(Document htmlDoc,PSInlineLinkProcessor inlineLinkProcessor) throws PSHtmlParsingException {

      if (htmlDoc == null || inlineLinkProcessor == null)
      {
         throw new IllegalArgumentException(
                 "source or linkProcessor may not be null or empty");
      }

      m_processor = inlineLinkProcessor;
      String result = htmlDoc.toString();
      //Process all "a" Tags
      Elements aTags = htmlDoc.select(A_TAG);
      processATags(aTags);
      //Process all "img" tags
      Elements imgTags = htmlDoc.select(IMG_TAG);
      processIMGTags(imgTags);
      result=htmlDoc.toString();
      result = StringUtils.replace(result, RX_FILLER, "");
      return result;
   }

   /**
    * Processes all "a" tags in the document
    * @param aTags
    * @throws PSHtmlParsingException
    */
   private void processATags(Elements aTags) throws PSHtmlParsingException {
      if(aTags == null || aTags.isEmpty() ){
         return;
      }
      String inlineType;
      String path;
      for(Element aTag: aTags){
         inlineType = aTag.attr(PSSingleValueBuilder.INLINE_TYPE);
         if(inlineType.equalsIgnoreCase(PSInlineLinkContentHandler.RXLINK)) {
            path = aTag.attr(HREF_ATTR);

            processTag(aTag, path, RXLINK, false);
         }
      }
   }

   /**
    * Processes all "img" tags in the document
    * @param imgTags
    * @throws PSHtmlParsingException
    */

   private void processIMGTags(Elements imgTags) throws PSHtmlParsingException {
      if(imgTags == null || imgTags.isEmpty() ){
         return;
      }
      String inlineType;
      String path;

      for(Element imgTag: imgTags){

         inlineType = imgTag.attr(PSSingleValueBuilder.INLINE_TYPE);

         if(inlineType.equalsIgnoreCase(PSInlineLinkContentHandler.RXIMAGE)) {
            path = imgTag.attr(SRC_ATTR);
            processTag(imgTag, path, RXIMAGE, true);
         }
      }
   }

   private void processTag(Element tag, String path,String type,boolean imgTag) throws PSHtmlParsingException {
      InlineLink link = null;
      Attributes attrs = tag.attributes();
      boolean tagProcessed = false;
      if(StringUtils.isEmpty(tag.getElementsByAttribute(PSSingleValueBuilder.ATTR_INLINESLOT).val()) && isManagableLink(path))
      {
         IPSUdfProcessor processor = getManagedLinkConverterUdf();
         if(processor != null)
         {
            try
            {
               link = new InlineLink(path, type);
               if(StringUtils.isBlank(link.dependentId))
               {
                  link = null;
               }
            }
            catch(Exception e)
            {
               log.error("Error occurred generating the inline link attributes for path {}. Error: {}",
                       path,
                       PSExceptionUtils.getMessageForLog(e));
            }
         }
      }
      else if(StringUtils.isNotEmpty(String.valueOf(tag.getElementsByAttribute(PSSingleValueBuilder.ATTR_INLINESLOT))))
      {
         link = new InlineLink(tag.attributes());
      }

      if (link != null)
      {
         try {
            IPSAssemblyItem target = link.getTargetItem();

            if (link.inlineType.equals(RXLINK)) {
               doRxHyperLink(link, target, attrs.get(PSSingleValueBuilder.JRCPATH));
            } else if (link.inlineType.equals(RXIMAGE)) {
               doRxImage(link, target);
            } else if (link.inlineType.equals(RXVARIANT)) {
               doRxVariant(link, target);
            }

            if (!link.isBroken && target != null) {
               String flag = getValidFlag(target);
               if (flag.equals("u"))
                  link.isBroken = true;
               else if (!flag.equals("y") && !flag.equals("i"))
                  link.isNotPublic = true;
            }
         }catch (Exception e){
            handleError(attrs, link.replacementBody, e);
         }

         // At this point we need to decide what to do. If the above was
         // just a reference to an image or link, then the current state
         // will be unmodified (and will be passthrough)
         if (link.replacementBody == null)
         {

            boolean foundClass = false;
            boolean foundAlt = false;
            boolean foundTitle = false;
            Iterator<Attribute> attributes = attrs.iterator();
            while (attributes.hasNext())
            {
               Attribute attr = attributes.next();
               String name = attr.getKey();
               // Skip our attributes used for inline links
               if (PSSingleValueBuilder.IGNORED_ATTRIBUTES.contains(name))
                  continue;
               String override = link.overrides.get(name);

               if(name.equals(PSSingleValueBuilder.CLASS))
               {
                  String existingClass = attr.getValue().trim();
                  foundClass = true;
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
                  if (!StringUtils.isEmpty(existingClass))
                     attr.setKey (PSSingleValueBuilder.CLASS);
                  attr.setValue(existingClass.trim());
                  continue;
               }


               if(imgTag) {
                  // fix to not break upgrade of images alt/title text
                  if((link.isUpgradeScenario && name.equals(PSSingleValueBuilder.ALT))
                          || (link.isUpgradeScenario && name.equals(PSSingleValueBuilder.TITLE))) {
                     // if the values are not the same we assume a previous
                     // override value and use that
                     if(name.equals(PSSingleValueBuilder.ALT))
                        foundAlt = true;
                     else if(name.equals(PSSingleValueBuilder.TITLE))
                        foundTitle = true;
                     if("src".equals(name)){
                        if(override != null ){
                           attr.setKey (name);
                           attr.setValue(override);
                        }else {
                           String v = (String)attrs.get(PSSingleValueBuilder.JRCPATH);
                           if (v == null || v.trim().isEmpty()) {
                              attr.setValue(attr.getValue());
                           } else {
                              attr.setValue(v);
                           }
                        }

                     }else {
                        attr.setValue(attr.getValue());
                     }
                     continue;
                  }

                  if(name.equals(PSSingleValueBuilder.ALT)) {
                     foundAlt=true;
                     if(!link.dataDecorativeOverride) {
                        if (!link.dataAltOverride)
                           attr.setValue(link.overrides.get(PSSingleValueBuilder.ALT));
                        else
                           attr.setValue(attr.getValue());
                     } else {
                        attr.setValue("");
                     }
                     continue;
                  }

                  if(name.equals(PSSingleValueBuilder.TITLE)) {
                     foundTitle=true;
                     if(!link.dataDecorativeOverride) {
                        if (!link.dataTitleOverride) {
                           if (link.overrides != null) {
                              String val = link.overrides.get(PSSingleValueBuilder.TITLE);
                              if (val != null) {
                                 attr.setValue(val);
                              }
                           }
                        } else {
                           attr.setValue(attr.getValue());
                        }
                     }else {
                        attr.setValue("");
                     }
                     continue;
                  }

                  if("src".equals(name)){
                     if(override != null ){
                        attr.setValue(override);
                     }else {
                        String v = (String) attrs.get(PSSingleValueBuilder.JRCPATH);
                        if (v == null || v.trim().isEmpty()) {
                           attr.setValue( attr.getValue());
                        } else {
                           attr.setValue( v);

                        }
                     }
                  }else {
                     attr.setValue( attr.getValue());
                  }

                  continue;
               }

               // here we handle the rest of the attributes as normal
               //After new Customization for broken link, we need to set override value
               // as selected by user

               if(PSSingleValueBuilder.HREF.equals(name)){
                  if(override != null)
                     attr.setValue(  override);
               }else {
                  if (!StringUtils.isBlank(override))
                     attr.setValue(  override);
                  else
                     attr.setValue(  attr.getValue());
               }

            }

            if (!foundTitle && imgTag)
            {
               if (!link.dataTitleOverride && !link.isUpgradeScenario && !link.dataDecorativeOverride )
                  tag.attributes().put(PSSingleValueBuilder.TITLE, link.overrides.get(PSSingleValueBuilder.TITLE));
               else
                  tag.attributes().put(PSSingleValueBuilder.TITLE, "");
            }

            if (!foundAlt && imgTag)
            {
               if (!link.dataAltOverride && !link.isUpgradeScenario && !link.dataDecorativeOverride)
                  tag.attributes().put(PSSingleValueBuilder.ALT, link.overrides.get(PSSingleValueBuilder.ALT));
               else
                  tag.attributes().put(PSSingleValueBuilder.ALT, "");
            }

            if (!foundClass) {
               String class_attr = null;
               if (link.isBroken) {
                  class_attr = PERC_BROKENLINK;
               } else if (link.isNotPublic) {
                  class_attr = PERC_NOTPUBLICLINK;
               }

               if (class_attr != null)
                  tag.attributes().put(PSSingleValueBuilder.CLASS, class_attr);

               if (class_attr != null && class_attr.contains(PERC_BROKENLINK)) {
                  class_attr = StringUtils.replace(class_attr, PERC_BROKENLINK, "").trim();
                  tag.attributes().put(PSSingleValueBuilder.CLASS, class_attr);
               }
            }
         }
         else if (StringUtils.isNotBlank(link.replacementBody))
         {
            log.debug("Replace: {}", tag);
            tag.html(link.replacementBody);
         }
      }

   }

   private String getValidFlag(IPSAssemblyItem target) throws PSFilterException
   {
      int context = target.getContext();
      if (context == 0)
      {
         return PSSingleValueBuilder.getValidFlag(target.getId().getUUID());
      }
      return "y";
   }

   private void doRxImage(InlineLink link, IPSAssemblyItem target) throws Exception {


      if (target == null )
      {
         if (m_processor.getWorkItem().getContext() > 0)
         {
            return;
         }
         link.isBroken = true;
         return;
      }


      link.overrides.put("src", link.getLink(target));

      Map<String, String> altAndTitle = new HashMap<>();

      // get alt text and title from actual asset itself
      // only if rtw overrides are not set
      if(!link.dataAltOverride || !link.dataTitleOverride) {
         altAndTitle = link.getAltTextAndTitleFromAsset(target);
         link.overrides.put(PSSingleValueBuilder.ALT, altAndTitle.get(PSSingleValueBuilder.ALT));
         link.overrides.put(PSSingleValueBuilder.TITLE, altAndTitle.get(PSSingleValueBuilder.TITLE));
      }

      return;
   }

   protected void doRxHyperLink(InlineLink link, IPSAssemblyItem target, String jrcPath) {
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
            return ;
         }
      }
      //else the link is broken
      log.debug("Broken Inline link from item {}", link.href);
      String overrideValue = AssemblerInfoUtils.getBrokenLinkOverrideValue(jrcPath);
      link.overrides.put(PSSingleValueBuilder.HREF, overrideValue);
      return ;
   }


   protected void doRxVariant(InlineLink link, IPSAssemblyItem target) throws Exception {

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
            throw new Exception("Failed to expand inline template");
         }
         else if (!result.getMimeType().startsWith("text/html"))
         {
            throw new Exception(
                    "Inline template expanded to non-html value: "
                            + result.getMimeType());
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
         return ;
      }

      return;
   }


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
         m_context = new PSRequestContext(req);

         // Get the siteid and folder id from relationship
         Map<String, String> relMap = PSSingleValueBuilder
                 .getSiteAndFolderFromRelationship(relationshipId, dependentId, m_context);
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
            siteId = m_processor.getWorkItem().getParameterValue(
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
         Map<String,String> props = (Map<String,String>) processor.processUdf(params, m_context);
         // Get attributes
         dependentVariantId = props.get(IPSHtmlParameters.SYS_DEPENDENTVARIANTID);
         dependentId = props.get(IPSHtmlParameters.SYS_DEPENDENTID);
         PSRequest req = PSRequest.getContextForRequest();
         m_context = new PSRequestContext(req);

         siteId = m_processor.getWorkItem().getParameterValue(IPSHtmlParameters.SYS_SITEID, "");

         href = props.get("path");
      }

      protected String getLink(IPSAssemblyItem assemblyItem) {
         /*
          * Set the new resource definition id here.
          * The resource location scheme generator will use
          * it to load resource definition.
          */
         if (this.resourceDefinitionId != null) {
            m_context.setParameter(
                    IPSHtmlParameters.PERC_RESOURCE_DEFINITION_ID,
                    this.resourceDefinitionId);
         }
         try {
            String link = (String) ms_lutils.generate(
                    assemblyItem, Long.valueOf(this.dependentVariantId));
            return link;
         }

         finally {
            if (this.resourceDefinitionId != null)
               m_context.removeParameter(IPSHtmlParameters.PERC_RESOURCE_DEFINITION_ID);
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

         IPSAssemblyItem asmItem = PSInlineLinkContentHandler.this.getTargetItem(dependentId, dependentVariantId,
                 siteId, folderId, relationshipId, selectedText);
         return asmItem;
      }
   }
   private void handleError(Attributes attrs, String replacementbody,
                            Exception e) throws PSHtmlParsingException {
      log.error("Problem processing inline link for item {} Error: {}", m_processor.getWorkItem().getId(),PSExceptionUtils.getMessageForLog(e));
      log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      PSTrackAssemblyError
              .addProblem("Problem processing inline links", e);
      StringBuilder message = new StringBuilder();
      message.append("Problem while processing inline link for item ");
      message.append(m_processor.getWorkItem().getId());
      message.append(": ");
      message.append(PSExceptionUtils.getMessageForLog(e));
      message.append(" See the replacement body in the console log. ");
      message.append("The attributes on the affected link were: ");
      int len = attrs != null ? attrs.size() : 0;
      Iterator<Attribute> attributesItr = attrs.iterator();
      int i = 0;
      while (attributesItr.hasNext())
      {
         Attribute attr = attributesItr.next();
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
      throw new PSHtmlParsingException(message.toString(), e);
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
    * @throws PSORMException
    * @throws CloneNotSupportedException
    * @throws PSCmsException
    * @throws PSAssemblyException
    */
   private IPSAssemblyItem getTargetItem(String depid, String depvariant,
                                         String siteid, String folderid, String rid, String selectedText)
           throws PSORMException, CloneNotSupportedException,
           PSAssemblyException, PSCmsException
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      IPSAssemblyItem sourceitem = m_processor.getWorkItem();
      // Check that we have the right content id. We may need to replace it
      // with one from a promotable version
      if (m_relationshipData == null)
      {
         PSRequest req = PSRequest.getContextForRequest();
         m_context = new PSRequestContext(req);
         PSLegacyGuid parentguid = (PSLegacyGuid) sourceitem.getId();
         m_relationshipData = PSSingleValueBuilder.buildRelationshipData(
                 m_context, parentguid.getLocator());
      }
      depid =
              PSSingleValueBuilder
                      .getCorrectedContentId(m_context, depid, rid, m_relationshipData);

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
         IPSItemFilter filter = m_processor.getItemFilter();
         if(filter != null) {

            List<IPSFilterItem> output = filter.filter(input,
                    params);
            if (output.size() == 0) {
               return null;
            }
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
      IPSGuid origSiteId = m_processor.getWorkItem().getSiteId();

      if (NumberUtils.toInt(siteid)>0)
      {
         targetitem.setParameterValue(IPSHtmlParameters.SYS_SITEID, siteid);
         if (NumberUtils.toInt(siteid)>0)
            targetitem.setSiteId(siteguid);
      }
      else
      {
         if(origSiteId != null) {
            // We are setting to owner site id if we do not have one be careful when merging rhythmyx
            targetitem.setParameterValue(IPSHtmlParameters.SYS_SITEID, Integer.toString(origSiteId.getUUID()));
            targetitem.setSiteId(origSiteId);
         }
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
    * A convenient method that checks a server property called  is available with a value of <code>true</code>
    * @return <code>true</code> or false based on the property.
    */
   private boolean doManageAll() {
      String autoProp = "true";
      if(PSServer.getServerProps()!=null)
      {
         autoProp = StringUtils.defaultString(PSServer.getServerProps().getProperty(IPSConstants.SERVER_PROP_MANAGELINKS));
      }
      return "true".equalsIgnoreCase(autoProp)?true:false;
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
   private synchronized IPSUdfProcessor getManagedLinkConverterUdf()
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


}
