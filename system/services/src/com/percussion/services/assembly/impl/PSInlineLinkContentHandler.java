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

import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSInlineLinkField;
import com.percussion.cms.PSRelationshipData;
import com.percussion.cms.PSSingleValueBuilder;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.data.PSConversionException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyResult.Status;
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
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.PSSaxCopier;
import com.percussion.utils.xml.PSSaxHelper;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.parsers.SAXParser;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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
public class PSInlineLinkContentHandler extends PSSaxCopier
{
   private static final String PERC_BROKENLINK = "perc-brokenlink";
   private static final String PERC_NOTPUBLICLINK = "perc-notpubliclink";
   
   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(PSInlineLinkContentHandler.class);

   
   
   private PSInlineLinkContentHandler inlineLinkContentHandler = this;

   

   /**
    * The location utils, used to calculate urls for inline links and such
    */
   private static PSLocationUtils ms_lutils = new PSLocationUtils();

   /**
    * The state enumeration is used on the stack to determine how each element
    * is being processed. Read the state descriptions to understand the 
    * working of this processing.
    */
   private enum State {
      /**
       * The passthrough state allows all content to be passed to the output.
       */
      PASSTHROUGH,
      /**
       * The ignore state swallows output
       */
      IGNORE,
      /**
       * Ignore all content until the handler reaches the next BODY element.
       * This is used to filter out headers in included content, i.e. inline
       * templates
       */
      IGNORE_TO_BODY,
      /**
       * Skip the next element, but passthrough the remaining sub
       * elements found.
       */
      SKIP;
      
      /**
       * Find out if the event should render. This is used for all but 
       * elements. For elements, skip is different than passthough.
       * @return <code>true</code> if the state needs the event rendered.
       */
      public boolean isRenderable()
      {
         return this.equals(SKIP) || this.equals(PASSTHROUGH);
      }
      
      /**
       * Determine the next state, used to switch from skip to passthrough
       * @return the next state, never <code>null</code>.
       */
      public State nextState()
      {
         if (this.equals(SKIP))
         {
            return PASSTHROUGH;
         }

         return this;

      }
   }

   /**
    * Encapsulate the state of a single element
    */
   private static class ElementState
   {
      /**
       * Ctor
       * 
       * @param elementname element name, assumed not <code>null</code> or
       *           empty
       * @param state the state, assumed not <code>null</code>
       */
      public ElementState(String elementname, State state) {
         mi_element = elementname;
         mi_state = state;
      }

      /**
       * The element name
       */
      public String mi_element;

      /**
       * The state of the parser, {@link State}
       */
      public State mi_state;

      /**
       * @return Returns the element.
       */
      public String getElement()
      {
         return mi_element;
      }

      /**
       * @return Returns the state.
       */
      public State getState()
      {
         return mi_state;
      }

      /**
       * Modify the state
       * 
       * @param newstate the new state
       */
      public void setState(State newstate)
      {
         mi_state = newstate;
      }

      /**
       * (non-Javadoc)
       * 
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return mi_element + ":" + mi_state;
      }
   }

   /**
    * The stack of state and element names
    */
   private List<ElementState> m_stateStack = new LinkedList<>();

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

   /**
    * Ctor
    * @param writer the stax output writer, never <code>null</code>
    * @param proc the parent processor, never <code>null</code>
    * @throws XMLStreamException
    */
   public PSInlineLinkContentHandler(XMLStreamWriter writer, 
         PSInlineLinkProcessor proc)
         throws XMLStreamException {
      super(writer, null, true);
      if (proc == null)
      {
         throw new IllegalArgumentException("proc may not be null");
      }
      m_processor = proc;
      
      // Clear stack
      m_stateStack.clear();
      pushState("document", State.PASSTHROUGH);
   }

   /**
    * Push new state onto stack
    * 
    * @param element the element name
    * @param state the new state
    */
   private void pushState(String element, State state)
   {
      m_stateStack.add(new ElementState(element, state.nextState()));
      log.debug("Pushed, now {} depth {}", m_stateStack.get(m_stateStack.size() - 1), m_stateStack.size());
   }

   /**
    * (non-Javadoc)
    * 
    * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
    */
   @Override
   public void characters(char[] ch, int start, int length) throws SAXException
   {
      if (getCurrentState().isRenderable())
      {
         super.characters(ch, start, length);
      }
   }

   /**
    * (non-Javadoc)
    * 
    * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @Override
   public void endElement(String uri, String localname, String qname)
         throws SAXException
   {
      if (getCurrentState().equals(State.PASSTHROUGH))
      {
         super.endElement(uri, localname, qname);
         log.debug("Write End {} depth {}", qname, m_stateStack.size());
      }
      log.debug("End {} state: {}", qname, getCurrentState());
      popState();
   }

   /**
    * Pop the current state off the element stack
    */
   private void popState()
   {
      if (m_stateStack.size() > 0)
      {
         m_stateStack.remove(m_stateStack.size() - 1);
      }
      log.debug("Popped to {} depth {}", m_stateStack.get(m_stateStack.size() - 1), m_stateStack.size());
   }

   /**
    * (non-Javadoc)
    * 
    * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace(char[], int,
    *      int)
    */
   @Override
   public void ignorableWhitespace(char[] ch, int start, int length)
         throws SAXException
   {
      if (getCurrentState().isRenderable())
      {
         super.ignorableWhitespace(ch, start, length);
      }
   }

   /**
    * (non-Javadoc)
    * 
    * @see org.xml.sax.helpers.DefaultHandler#processingInstruction(java.lang.String,
    *      java.lang.String)
    */
   @Override
   public void processingInstruction(String target, String data)
         throws SAXException
   {
      if (getCurrentState().isRenderable())
      {
         super.processingInstruction(target, data);
      }
   }

   /**
    * (non-Javadoc)
    * 
    * @see org.xml.sax.helpers.DefaultHandler#skippedEntity(java.lang.String)
    */
   @Override
   public void skippedEntity(String name) throws SAXException
   {
      if (getCurrentState().isRenderable())
      {
         super.skippedEntity(name);
      }
   }

   @Override
   public void comment(char[] ch, int start, int length) throws SAXException
   {
      if (getCurrentState().equals(State.IGNORE) ||
            getCurrentState().equals(State.IGNORE_TO_BODY))
      {
         return;
      }
      
      super.comment(ch, start, length);
   }

   /**
    * (non-Javadoc)
    * 
    * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
    *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
    */
   @Override
   public void startElement(String uri, String localname, String qname,
         Attributes attrs) throws SAXException
   {
      State laststate = getCurrentState();
      String lastelement = getCurrentElement();
      String replacementbody = null;
      resetCharCount();
      pushState(qname, laststate);

      try
      {
         // If the element "above" was ignore to body, then check for body. If
         // body present, change the current element's state
         if (laststate.equals(State.IGNORE_TO_BODY))
         {
            if (lastelement.equalsIgnoreCase("BODY"))
            {
               setCurrentState(State.PASSTHROUGH);
               log.debug("Change state to passthrough on {}", qname);
            }
         }

         // Everything else is based on the current state
         State currentstate = getCurrentState();
         
         String path = "a".equalsIgnoreCase(qname)?attrs.getValue(PSSingleValueBuilder.HREF):"img".equalsIgnoreCase(qname)?attrs.getValue("src"):"";
         InlineLink link = null;
         if(currentstate.equals(State.PASSTHROUGH) && StringUtils.isEmpty(attrs.getValue(PSSingleValueBuilder.ATTR_INLINESLOT)) && isManagableLink(path))
         {
            String type = "a".equalsIgnoreCase(qname)?"rxhyperlink":"img".equalsIgnoreCase(qname)?"rximage":"";
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
                  log.error("Error occurred generating the inline link attributes for path {}", path);
               }
            }
         }
         else if(currentstate.equals(State.PASSTHROUGH) && StringUtils.isNotEmpty(attrs.getValue(PSSingleValueBuilder.ATTR_INLINESLOT)))
         {
            link = new InlineLink(attrs);
         }
         
         if (currentstate.equals(State.PASSTHROUGH)
               && (link != null))
         {

            State inlineLinkState = null;
            IPSAssemblyItem target = link.getTargetItem();
            
            if (link.inlineType.equals("rxhyperlink"))
            {
               inlineLinkState = doRxHyperLink(link, target,attrs.getValue(PSSingleValueBuilder.JRCPATH));
            }
            else if (link.inlineType.equals("rximage"))
            {
               inlineLinkState = doRxImage(link, target);
            }
            else if (link.inlineType.equals("rxvariant"))
            {
               inlineLinkState = doRxVariant(link, target);
            }
            
            if (!link.isBroken && target!=null)
            {
               String flag = getValidFlag(target);
               if (flag.equals("u"))
                  link.isBroken=true;
               else if (!flag.equals("y") && !flag.equals("i"))
                  link.isNotPublic = true;
            }
            
            if (inlineLinkState != null) {
               setCurrentState(inlineLinkState); 
               return;
            }

            // At this point we need to decide what to do. If the above was
            // just a reference to an image or link, then the current state
            // will be unmodified (and will be passthrough)
            if (link.replacementBody == null)
            {
               m_writer.writeStartElement(qname);
               log.debug("Write Start {} depth {}", qname, m_stateStack.size());
               boolean foundClass = false;
               boolean foundAlt = false;
               boolean foundTitle = false;
               for (int i = 0; i < attrs.getLength(); i++)
               {
                  String name = attrs.getQName(i);
                  // Skip our attributes used for inline links
                  if (PSSingleValueBuilder.IGNORED_ATTRIBUTES.contains(name))
                     continue;
                  String override = link.overrides.get(name);

                  if(name.equals(PSSingleValueBuilder.CLASS))
                  {
                     String existingClass = attrs.getValue(i).trim();
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
                         writeToWriter(PSSingleValueBuilder.CLASS, existingClass.trim());
                     continue;
                  }
                  
                  if(qname.equals("img")) {
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
                              writeToWriter(name, override);
                           }else {
                              String v = (String) attrs.getValue(PSSingleValueBuilder.JRCPATH);
                              if (v == null || v.trim().isEmpty()) {
                                 writeToWriter(name, attrs.getValue(i));
                              } else {
                                 writeToWriter(name, v);

                              }
                           }

                        }else {
                            writeToWriter(name, attrs.getValue(i));
                        }
                        continue;
                     }
                     
                     if(name.equals(PSSingleValueBuilder.ALT)) {
                        foundAlt=true;
                        if(!link.dataDecorativeOverride) {
                              if (!link.dataAltOverride)
                                  writeToWriter(PSSingleValueBuilder.ALT, link.overrides.get(PSSingleValueBuilder.ALT));
                              else
                                  writeToWriter(name, attrs.getValue(i));
                        } else {
                            writeToWriter(PSSingleValueBuilder.ALT, "");
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
                                    writeToWriter(PSSingleValueBuilder.TITLE, val);
                                 }
                              }
                           } else {
                              writeToWriter(name, attrs.getValue(i));
                           }
                        }else {
                           writeToWriter(PSSingleValueBuilder.TITLE, "");
                        }
                        continue;
                     }

                      if("src".equals(name)){
                         if(override != null ){
                            writeToWriter(name, override);
                         }else {
                            String v = (String) attrs.getValue(PSSingleValueBuilder.JRCPATH);
                            if (v == null || v.trim().isEmpty()) {
                               writeToWriter(name, attrs.getValue(i));
                            } else {
                               writeToWriter(name, v);

                            }
                         }
					   }else {
                          writeToWriter(name, attrs.getValue(i));
                      }

                      continue;
                  }
                        
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
               
               if (!foundTitle && qname.equals("img"))
               {
                  if (!link.dataTitleOverride && !link.isUpgradeScenario && !link.dataDecorativeOverride )
                      writeToWriter(PSSingleValueBuilder.TITLE, link.overrides.get(PSSingleValueBuilder.TITLE));
                  else
                      writeToWriter(PSSingleValueBuilder.TITLE, "");
               }
               
               if (!foundAlt && qname.equals("img"))
               {
                  if (!link.dataAltOverride && !link.isUpgradeScenario && !link.dataDecorativeOverride)
                      writeToWriter(PSSingleValueBuilder.ALT, link.overrides.get(PSSingleValueBuilder.ALT));
                  else
                      writeToWriter(PSSingleValueBuilder.ALT, "");
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
         }
         else if (currentstate.equals(State.IGNORE))
         {
            log.debug("Ignore: {}", qname);
         }
      }
      catch (Exception e)
      {
         handleError(attrs, replacementbody, e);
      }
   }

   private void writeToWriter(String key, String value) throws XMLStreamException {
       //As m_writer is throwing NPE incase value is null
       if(value == null){
           value = "";
       }
       if(m_writer != null)
             m_writer.writeAttribute(key, value);

   }

   protected State doRxHyperLink(InlineLink link, IPSAssemblyItem target, String jrcPath)
      throws Exception {
         if (target != null){
            String newHref = link.getLink(target);
            if (StringUtils.isNotBlank(newHref)) {
               if(link.href != null) {
                  int anchorindex = link.href.lastIndexOf("#");
                  if(anchorindex != -1) {
                     String anchor = link.href.substring(anchorindex+1);
                     if (anchor != null && anchor.trim() != "") {
                        newHref = newHref + "#" + anchor;
                     }
                  }
               }
               link.overrides.put(PSSingleValueBuilder.HREF, newHref);
               return null;
            }
         }
         //else the link is broken
         log.debug("Broken Inline link from item {}", link.href);
         String overrideValue = AssemblerInfoUtils.getBrokenLinkOverrideValue(jrcPath);
         link.overrides.put(PSSingleValueBuilder.HREF, overrideValue);
         return null;
      }



   public String getValidFlag(IPSAssemblyItem target) throws PSFilterException
   {
      int context = target.getContext();
      if (context == 0)
      {      
        return PSSingleValueBuilder.getValidFlag(target.getId().getUUID());
      }
      return "y";
   }

   protected State doRxImage(InlineLink link, IPSAssemblyItem target) throws Exception {
      
      
      if (target == null )
      {
         if (m_processor.getWorkItem().getContext() > 0)
         {
            return State.IGNORE; // Just skip the IMG tag
         }
         link.isBroken = true;
         return null;
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
      
      return null;
   }
   
   protected State doRxVariant(InlineLink link, IPSAssemblyItem target) throws Exception {

      if (target != null)
      {
    
         IPSAssemblyService asm = PSAssemblyServiceLocator
               .getAssemblyService();
         List<IPSAssemblyItem> listofitems = Collections
               .singletonList(target);
         List<IPSAssemblyResult> results = asm.assemble(listofitems);
         IPSAssemblyResult result = results.get(0);
         if (result.getStatus() != Status.SUCCESS)
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
                  "UTF8");                     
         }
      }
      else
      {
         return State.IGNORE; 
      }
      
      return null;
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
      private PSXmlDecoder enc = new PSXmlDecoder();
      
      public InlineLink(Attributes attrs)
      {
         super();
         // This block of code is executed for any inline content. If the
         // inline content is a variant, then the state is set to IGNORE
         // so that the inline content of the variant from the body field
         // is swallowed
         inlineType = attrs.getValue(PSSingleValueBuilder.INLINE_TYPE);
         overrides = new HashMap<>();

         // Get attributes
         dependentVariantId = 
            attrs.getValue(IPSHtmlParameters.SYS_DEPENDENTVARIANTID);
         dependentId = attrs.getValue(IPSHtmlParameters.SYS_DEPENDENTID);
         relationshipId = attrs.getValue(IPSHtmlParameters.SYS_RELATIONSHIPID);
         
         siteId = attrs.getValue(IPSHtmlParameters.SYS_SITEID);
         folderId = attrs.getValue(IPSHtmlParameters.SYS_FOLDERID);
         selectedText = StringUtils.defaultString(attrs
               .getValue(PSInlineLinkField.RX_SELECTEDTEXT));
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
         
         if (!StringUtils.isBlank(attrs.getValue(PSSingleValueBuilder.DATA_DESCRIPTION_OVERRIDE))) {
            dataAltOverride = Boolean.valueOf(attrs.getValue(PSSingleValueBuilder.DATA_DESCRIPTION_OVERRIDE));
            dataTitleOverride = Boolean.valueOf(attrs.getValue(PSSingleValueBuilder.DATA_TITLE_OVERRIDE));
            isUpgradeScenario = false;
         }
         else if(inlineType.equals("rximage"))
            isUpgradeScenario = true;
         
         if(!StringUtils.isBlank(attrs.getValue(PSSingleValueBuilder.DATA_DECORATIVE_OVERRIDE)))
            dataDecorativeOverride = Boolean.valueOf(attrs.getValue(PSSingleValueBuilder.DATA_DECORATIVE_OVERRIDE));

         href = attrs.getValue(PSSingleValueBuilder.HREF);
         resourceDefinitionId = attrs.getValue(PSSingleValueBuilder.RESOURCE_DEFINITION_ID);

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
         //FB: UR_UNINIT_READ NC 1-17-16
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
            String link = (String) enc.encode(ms_lutils.generate(
                  assemblyItem, Long.valueOf(this.dependentVariantId)));
            return link;
         }
         catch (EncoderException e)
         {
            throw new RuntimeException("Failed to decode link: " + this, e);
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
            items.put(PSSingleValueBuilder.ALT, nodeList.get(0).getProperty("rx:alttext").getString());
            items.put(PSSingleValueBuilder.TITLE, nodeList.get(0).getProperty("rx:displaytitle").getString());
         }
         catch (RepositoryException e)
         {
            log.error("Unable to get node for alt and title text with ID: {} and error message: {}",
                    assemblyItem.getId(), e.getMessage());
            log.debug(e.getMessage(), e);
         }
         
         return items;
      }
      
      public IPSAssemblyItem getTargetItem() throws PSAssemblyException, PSCmsException, PSORMException, CloneNotSupportedException {
         
         IPSAssemblyItem asmItem = inlineLinkContentHandler.getTargetItem(dependentId, dependentVariantId,
               siteId, folderId, relationshipId, selectedText);
         
        
         
         
         return asmItem;
      }

   }
   private void handleError(Attributes attrs, String replacementbody,
         Exception e) throws SAXException
   {
      log.error("Problem processing inline link for item {} Error: {}", m_processor.getWorkItem().getId(), e.getMessage());
      log.debug(e.getMessage(), e);
      PSTrackAssemblyError
         .addProblem("Problem processing inline links", e);
      StringBuilder message = new StringBuilder();
      message.append("Problem while processing inline link for item ");
      message.append(m_processor.getWorkItem().getId());
      message.append(": ");
      message.append(e.getLocalizedMessage());
      message.append(" See the replacement body in the console log. ");
      message.append("The attributes on the affected link were: ");
      int len = attrs != null ? attrs.getLength() : 0;
      for (int i = 0; i < len; i++)
      {
         String name = attrs.getQName(i);
         String value = attrs.getValue(i);
         if (i > 0)
         {
            message.append(",");
         }
         message.append(name);
         message.append("=\"");
         message.append(value);
         message.append("\"");
      }

      log.error(message.toString());
      log.error("Actual replacement body for error was: {}", replacementbody);
      throw new SAXException(message.toString(), e);
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
      
         List<IPSFilterItem> output = filter.filter(input,
               params);
         if (output.size() == 0)
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
      IPSGuid origSiteId = m_processor.getWorkItem().getSiteId();
      
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

   /**
    * Calculate the current state
    * 
    * @return the current state, never <code>null</code>
    */
   private State getCurrentState()
   {
      State currentstate;
      if (m_stateStack.size() > 0)
      {
         ElementState current = m_stateStack.get(m_stateStack.size() - 1);
         currentstate = current.getState();
      }
      else
      {
         currentstate = State.PASSTHROUGH;
      }
      return currentstate;
   }

   /**
    * Return the current top element on the stack
    * 
    * @return the current top element or empty if there are no more elements
    */
   private String getCurrentElement()
   {
      if (m_stateStack.size() > 0)
      {
         ElementState current = m_stateStack.get(m_stateStack.size() - 1);
         return current.getElement();
      }

      return "";

   }

   /**
    * Change current state
    * 
    * @param newstate new state
    */
   private void setCurrentState(State newstate)
   {
      if (m_stateStack.size() > 0)
      {
         ElementState current = m_stateStack.get(m_stateStack.size() - 1);
         current.setState(newstate);
      }
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
          autoProp = StringUtils.defaultString(PSServer.getServerProps().getProperty("AUTO_MANAGE_LOCAL_PATHS"));
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
      catch (PSNotFoundException e)
      {
         log.error(this.getClass().getName(), e);
      }
      catch (PSExtensionException e)
      {
         log.error(this.getClass().getName(), e);
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
   
}
