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

package com.percussion.cms;

import com.percussion.HTTPClient.Codecs;
import com.percussion.HTTPClient.ParseException;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipDbProcessor;
import com.percussion.data.IPSDataExtractor;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSDataExtractorFactory;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.macro.PSMacroUtils;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.error.PSException;
import com.percussion.extension.PSExtensionException;
import com.percussion.log.PSLogManager;
import com.percussion.log.PSLogServerWarning;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSRequestParsingException;
import com.percussion.server.PSUserSession;
import com.percussion.server.PSUserSessionManager;
import com.percussion.server.cache.PSFolderRelationshipCache;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyResult.Status;
import com.percussion.services.assembly.impl.AssemblerInfoUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.services.utils.general.PSAssemblyServiceUtils;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCms;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.percussion.webservices.PSWebserviceUtils.getItemSummary;
import static com.percussion.webservices.PSWebserviceUtils.isItemCheckedOutToUser;


/**
 * Creates the DisplayField element according to the ContentEditor.dtd when
 * the data source is a single value (as opposed to an array or table).
 */
public class PSSingleValueBuilder extends PSDisplayFieldBuilder
{

   private static final Logger log = LogManager.getLogger(PSSingleValueBuilder.class);

   /**
    * Constant strings
    */
   public static final String ATTR_INLINESLOT = "rxinlineslot";
   public static final String DATA_DESCRIPTION_OVERRIDE = "data-description-override";
   public static final String DATA_TITLE_OVERRIDE = "data-title-override";
   public static final String DATA_DECORATIVE_OVERRIDE = "data-decorative-override";
   public static final String DATA_PREVIOUS_ALT_OVERRIDE = "data-previous-alt-override";
   public static final String DATA_PREVIOUS_TITLE_OVERRIDE = "data-previous-title-override";
   public static final String DATA_CONSTRAIN = "data-constrain";
   public static final String DATA_IMG_TYPE = "data-imgtype";
   public static final String INLINE_TYPE = "inlinetype";
   public static final String RESOURCE_DEFINITION_ID = "resourcedefinitionid";
   public static final String ALT = "alt";
   public static final String CLASS = "class";
   public static final String TITLE = "title";
   public static final String JRCPATH = "data-jcrpath";
   public static final String HREF = "href";

   /**
    * A set of attributes that should be ignored, statically initialized.
    */
   public static Set<String> IGNORED_ATTRIBUTES = new HashSet<String>();

   static
   {
      IGNORED_ATTRIBUTES.add(ATTR_INLINESLOT);
      IGNORED_ATTRIBUTES.add(DATA_IMG_TYPE);
      IGNORED_ATTRIBUTES.add(DATA_CONSTRAIN);
      IGNORED_ATTRIBUTES.add(DATA_PREVIOUS_ALT_OVERRIDE);
      IGNORED_ATTRIBUTES.add(DATA_DESCRIPTION_OVERRIDE);
      
      IGNORED_ATTRIBUTES.add(DATA_PREVIOUS_TITLE_OVERRIDE);
      IGNORED_ATTRIBUTES.add(DATA_TITLE_OVERRIDE);
      IGNORED_ATTRIBUTES.add(DATA_DECORATIVE_OVERRIDE);
      IGNORED_ATTRIBUTES.add(IPSHtmlParameters.SYS_DEPENDENTID);
      IGNORED_ATTRIBUTES.add(IPSHtmlParameters.SYS_DEPENDENTVARIANTID);
      IGNORED_ATTRIBUTES.add(IPSHtmlParameters.SYS_SITEID);
      IGNORED_ATTRIBUTES.add(IPSHtmlParameters.SYS_FOLDERID);
      IGNORED_ATTRIBUTES.add(IPSHtmlParameters.SYS_VARIANTID);
      IGNORED_ATTRIBUTES.add(IPSHtmlParameters.SYS_CONTENTID);
      IGNORED_ATTRIBUTES.add(IPSHtmlParameters.SYS_RELATIONSHIPID);
      IGNORED_ATTRIBUTES.add(INLINE_TYPE);
      IGNORED_ATTRIBUTES.add(RESOURCE_DEFINITION_ID);
   }
   
   public static Set<String> REMOVE_EMPTY_ATTRIBUTES = new HashSet<String>();
   {
      REMOVE_EMPTY_ATTRIBUTES.add(ATTR_INLINESLOT);
      REMOVE_EMPTY_ATTRIBUTES.add(INLINE_TYPE);
      REMOVE_EMPTY_ATTRIBUTES.add(IPSHtmlParameters.SYS_DEPENDENTID);
      REMOVE_EMPTY_ATTRIBUTES.add(IPSHtmlParameters.SYS_DEPENDENTVARIANTID);
      REMOVE_EMPTY_ATTRIBUTES.add(IPSHtmlParameters.SYS_SITEID);
      REMOVE_EMPTY_ATTRIBUTES.add(IPSHtmlParameters.SYS_FOLDERID);
      REMOVE_EMPTY_ATTRIBUTES.add(IPSHtmlParameters.SYS_VARIANTID);
      REMOVE_EMPTY_ATTRIBUTES.add(IPSHtmlParameters.SYS_CONTENTID);
      REMOVE_EMPTY_ATTRIBUTES.add(IPSHtmlParameters.SYS_RELATIONSHIPID);
   }
   
   public static final String PERC_BROKENLINK = "perc-brokenlink";
   public static final String PERC_NOTPUBLICLINK = "perc-notpubliclink";
   
   /**
    * Creates a DisplayField builder that contains a single data value for non
    * binary fields. Use the {@link PSDisplayFieldBuilder base class} directly
    * for binary fields.
    * <p>See the {@link
    * PSDisplayFieldBuilder#PSDisplayFieldBuilder(PSField,PSUISet,
    * PSEditorDocumentBuilder) base class} for description of params.
    *
    * @param value The definition of where to find the data. May be <code>
    *    null</code>, in which case, no Value element will be added.
    */
   public PSSingleValueBuilder( PSField field, PSUISet ui,
         IPSReplacementValue value, PSEditorDocumentBuilder parentBuilder )
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException
   {
      super( field, ui, parentBuilder );
      m_extractor = createExtractor( value );
      m_defExtractor = createExtractor( field.getDefault());
   }

   /**
    * Same as {@link #PSSingleValueBuilder(PSField,PSUISet,IPSReplacementValue,
    * PSEditorDocumentBuilder) PSSingleValueBuilder} except the data source is
    * typically from a backend. See the referenced method for description of
    * similar parameters.
    *
    * @param locator The definition of where to find the data. May be <code>
    *    null</code>. Must implement the IPSReplacementValue interface.
    */
   public PSSingleValueBuilder( PSField field, PSUISet ui,
         IPSBackEndMapping locator, PSEditorDocumentBuilder parentBuilder )
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException
   {
      super( field, ui, parentBuilder );      
      m_extractor = createExtractor(locator );
      m_defExtractor = createExtractor( field.getDefault());
   }


   /**
    * A convenience constructor for creating hidden fields. The supplied
    * control name must be able to handle a hidden field. No label will be
    * supplied. See {@link PSDisplayFieldBuilder#PSDisplayFieldBuilder(
    * String, String, PSEditorDocumentBuilder) base} class for a  description
    * of params and their requirements.
    *
    * @param value The definition of how to find the value for this field.
    *    May be <code>null</code>, in which case no Value element will be
    *    added.
    */
   public PSSingleValueBuilder( String controlName, String submitName,
         IPSReplacementValue value, PSEditorDocumentBuilder parentBuilder )
   {
      super( controlName, submitName, parentBuilder );
      m_extractor = createExtractor( value );
   }

   /**
    * Creates a hidden display field element using a hidden control using
    * the supplied name and value.  No label will be supplied.
    *
    * @param doc The document to which the control is to be added.  May not be
    * <code>null</code>.
    * @param submitName The name of the control, never <code>null</code> or
    * empty.
    * @param value The value to set on the control, may be not be
    * <code>null</code> or empty.
    * @param isReadOnly If <code>true</code>, the control will be created as
    * read only, if <code>false</code> it will not be created as read only.
    *
    * @return The display field element, never <code>null</code> or empty.
    */
   public static Element createHiddenField(Document doc, String controlName,
      String submitName, String value, boolean isReadOnly)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      if (controlName == null || controlName.trim().length() == 0)
         throw new IllegalArgumentException(
            "controlName may not be null or empty");

      if (submitName == null || submitName.trim().length() == 0)
         throw new IllegalArgumentException(
            "submitName may not be null or empty");

      Element dispNode = PSDisplayFieldElementBuilder.createHiddenFieldElement(
         doc, controlName, submitName, value, isReadOnly);

      return dispNode;
   }

   /**
    * Builds a data extractor from the supplied replacement value. The
    * extractor is used to get data from the execution data object at run
    * time.
    *
    * @param value The definition of the value. Maybe <code>null</code>.
    *
    * @return If value is <code>null</code>, <code>null</code> is returned.
    *    Otherwise a valid extractor is returned.
    */
   private IPSDataExtractor createExtractor( IPSReplacementValue value )
   {
      IPSDataExtractor extractor = null;
      try
      {
         if ( null != value )
         {
            extractor = PSDataExtractorFactory.
                  createReplacementValueExtractor( value );
         }
      }
      catch ( IllegalArgumentException e )
      {
         throw new IllegalArgumentException( e.getLocalizedMessage());
      }
      return extractor;
   }

   /**
    * Creates the Value element for the DisplayField parent element. Calls
    * <code>getDataValue</code> to obtain the actual data for the content.
    * If this is a new document, if a default value is specified, it is
    * added.
    * <p>See the base class for a description of the params and return.
    */
   @Override
   protected boolean addDataElement( Document doc, Element parent,
         PSExecutionData data, boolean isNewDoc )
      throws PSDataExtractionException
   {
      boolean addedElem = false;

      String content = null;
      boolean isBECol = null == m_extractor ? false :
            m_extractor.getSource()[0] instanceof PSBackEndColumn;
      if ( isNewDoc && isBECol )
      {
         if ( null != m_defExtractor )
         {
            Object value = m_defExtractor.extract( data );
            content = null == value ? null : value.toString();
         }
      }
      else if ( null != m_extractor )
      {
         Object value = m_extractor.extract( data );
         content = null == value ? null : value.toString();
      }

      if ( null != content && content.length() > 0 )
      {
         //if content has rxinlineslot then fix the variants
         if (content.indexOf("rxinlineslot") > -1)
         {
            IPSRequestContext request =
               new PSRequestContext(data.getRequest());
            boolean isModified = false;
            Document contentDoc = null;
            try
            {
               contentDoc =
                  PSXmlDocumentBuilder.createXmlDocument(
                     new StringReader(content),
                     false);
               isModified = PSInlineLinkField.expandEmptyElement(contentDoc);
               
               processVariant(
                  contentDoc.getDocumentElement(),
                  request,
                  buildRelationshipData(request, null),
                  getControlName());
            }
            catch (Exception e)
            {
               /* This may happen for variety of reasons, however, we cannot
                * throw an excption just for one field and stop processing
                * completely
                * Just write the trace in case if it happens...
                */
               request.printTraceMessage(e.getMessage());
            }
            //Fix for http://bugs/browse/CML-4835
            if (contentDoc != null)
            {
               content = PSXmlDocumentBuilder.toString(contentDoc,
                     PSXmlDocumentBuilder.FLAG_NO_INDENT
                     | PSXmlDocumentBuilder.FLAG_OMIT_XML_DECL);
               if (isModified)
               {
                  content = PSInlineLinkField.replaceString(content,
                        PSInlineLinkField.ELEM_FILLER, "");
               }
            }
            
         }

         PSDisplayFieldElementBuilder.addDataElement(doc, parent, content);
         addedElem = true;
      }
      return addedElem;
   }
   
   /**
    * Convienience method to call 
    * {@link #processVariant(Element, IPSRequestContext, PSRelationshipData, String)} as
    * processVariant(Element, IPSRequestContext, PSRelationshipData, null)
    */
   public static void processVariant(
            Element elem,
            IPSRequestContext request,
            PSRelationshipData inlineLinkRelData) throws PSCmsException
   {
      processVariant(elem, request, inlineLinkRelData, null);
   }

   /**
    * Method to recurse through all the elements and child elements and
    * replace inline links of all types including images and snippets. It does
    * the following:
    * <p>
    * <ol>
    * <li>Walks through every element in the element tree to see of if there are
    * any inline links of any type</li>
    * <li>If there is an inlone link, it regenerates the link to point to right
    * version contentid and right revision</li>
    * <li>The version may need to be changed when an inline item has a
    * promotable version which replaces the original one when it goes to public
    * state. Note that the versions have different contentids</li>
    * <li>The revision is of inline item will be changed to always to point to
    * the current revision </li>
    * </ol>
    * @param elem root element of the document which has inline variants, if
    * <code>null</code> it will not be processed.
    * @param request IPSRequestContext must not be <code>null</code.null.
    * @param inlineLinkRelData inline link relationship data for the parent
    * item. This can be built using the static method
    * {@link PSSingleValueBuilder#buildRelationshipData(IPSRequestContext,
    * PSLocator)}. Must not be <code>null</code>.
    * @param controlName the name of the field control name, may be <code>null</code>.
    * @throws PSCmsException If any error occurs during processing the variant.
    * 
    */
   public static void processVariant(
      Element elem,
      IPSRequestContext request,
      PSRelationshipData inlineLinkRelData, 
      String controlName)
      throws PSCmsException
   {
      if (elem == null)
         return;

      if (request == null)
         throw new IllegalArgumentException("request must not be null");

      if (inlineLinkRelData == null)
         throw new IllegalArgumentException("inlineLinkRelData must not be null");

      boolean processed = false;
      String inlineType = elem.getAttribute(
         PSInlineLinkField.RX_INLINETYPE);
      if(inlineType != null &&
         (inlineType.equals(RX_INLINETYPE_RXVARIANT) ||
          inlineType.equals(RX_INLINETYPE_HYPERLINK) ||
          inlineType.equals(RX_INLINETYPE_IMAGE)))
      {
        
         Element newVar = 
            replaceVariant(elem, request, inlineLinkRelData);
         if(controlName!=null && controlName.equalsIgnoreCase("sys_editlive"))
         {           
            if(newVar != null)
               updateLegacyVariant(newVar);
         }
         processed = true;
      }
      else if (inlineType.equals(RX_INLINETYPE_RXHYPERLINK))
      {
         modifyLink(elem, request, false, inlineLinkRelData);
      }
      else if (inlineType.equals(RX_INLINETYPE_RXIMAGE))
      {
         modifyLink(elem, request, true, inlineLinkRelData);
         processed = true;
      }
      if (!processed)
      {
         // recurse through all child elements
         if (elem.hasChildNodes())
         {
            NodeList children = elem.getChildNodes();
            for (int i=0; i<children.getLength(); i++)
            {
               Node test = children.item(i);
               if (test.getNodeType() == Node.ELEMENT_NODE)
                 processVariant((Element) test, request,
                          inlineLinkRelData, controlName);
            }
         }
      }
   }
   
   /**
    * Fix up inline variants generated by eWebEditPro to follow the new
    * form used by our EditLive implementation.
    * @param elem the variant element, assumed not <code>null</code>.
    */
   private static void updateLegacyVariant(Element elem)
   {
      
      Node parent = elem.getParentNode();
      boolean parentIsEditLiveDivWrapper = false;
      Element parentEl = null;
      if (parent instanceof Element)
      {
         parentEl = (Element) parent;
         String parentName = parentEl.getNodeName();
         String parentClassAttr = parentEl.getAttribute("class");
         parentIsEditLiveDivWrapper = parentName.equalsIgnoreCase("div")
                  && parentClassAttr != null
                  && parentClassAttr.equals("rx_ephox_inlinevariant");
      }

      // All special attribs can just go in this div
      if (parentIsEditLiveDivWrapper)
      {
         moveSpecialAttribs(elem, parentEl);
         parentEl.setAttribute("contenteditable", "false");
      }
      else
      {
         // add wrapper
         Document doc = elem.getOwnerDocument();
         Element wrapper = doc.createElement("div");
         wrapper.setAttribute("class", "rx_ephox_inlinevariant");
         wrapper.setAttribute("contenteditable", "false");
         moveSpecialAttribs(elem, wrapper);
         parentEl.replaceChild(wrapper, elem);
         wrapper.appendChild(elem);
         
      }
      if(elem.getNodeName().equalsIgnoreCase("div") && 
         (elem.getAttributes() == null || elem.getAttributes().getLength() == 0))
      {
         // remove attributless and useless div
         Node p = elem.getParentNode();
         NodeList nl = elem.getChildNodes();
         int len = nl.getLength();
         List<Node> nodes = new ArrayList<Node>();
         for(int i = 0; i < len; i++)
         {
            nodes.add(nl.item(i));
         }
         for(Node child : nodes)
         {
            p.appendChild(elem.removeChild(child));
         }
         p.removeChild(elem);
         
      }
      
      
   }
   
   /**
    * Helper methods to move the special inline variant attributes
    * from one element to another.
    * @param source the source element, assumed not <code>null</code>.
    * @param target the target element, assumed not <code>null</code>.
    */
   private static void moveSpecialAttribs(Element source, Element target)
   {
      String[] attribNames = new String[]
      {
         IPSHtmlParameters.SYS_DEPENDENTID,
         IPSHtmlParameters.SYS_DEPENDENTVARIANTID,
         IPSHtmlParameters.SYS_RELATIONSHIPID,
         IPSHtmlParameters.SYS_FOLDERID,
         IPSHtmlParameters.SYS_SITEID,
         PSInlineLinkField.RX_INLINESLOT,
         PSInlineLinkField.RX_INLINETYPE,
         PSInlineLinkField.RX_SELECTEDTEXT
      };
      String value = null;
      for(String name : attribNames)
      {
         value = source.getAttribute(name);
         if(value != null)
         {
            target.setAttribute(name, value);
            source.removeAttribute(name);
         }
      }
      source.removeAttribute("contenteditable");
      source.removeAttribute("unselectable");
   }

   /**
    * Builds active assembly relationship data to be useful to find out correct
    * contentid which is required when promotable versions go to public state.
    * This data is required while processing inline links and is per one parent
    * item basis.
    * 
    * @param request request context must not be <code>null</code>.
    * @param parent locator for the parent item which is being processed for
    *           inline links. May be <code>null</code> in which case the
    *           locator is built from the parameters
    *           {@link IPSHtmlParameters#SYS_CONTENTID} and
    *           {@link IPSHtmlParameters#SYS_REVISION} in the request context.
    */
   @SuppressWarnings("unchecked")
   public static PSRelationshipData buildRelationshipData(
      IPSRequestContext request,
      PSLocator parent)
      throws PSCmsException
   {
      if (request == null)
         throw new IllegalArgumentException("request must not be null");

      PSRelationshipData inlineLinkRelData = new PSRelationshipData();

      if(parent==null)
      {
         String parentContentId =
            request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         String parentRevision =
            request.getParameter(IPSHtmlParameters.SYS_REVISION);
         parent = new PSLocator(parentContentId, parentRevision);

      }
      PSRelationshipDbProcessor processor = PSRelationshipDbProcessor.getInstance();
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(parent);
      filter.setCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
      filter.setCommunityFiltering(false);
      filter.limitToOwnerRevision(true);

      List<PSRelationship> rels = processor.getRelationshipList(filter);
      if(rels.size()==0){
         PSComponentSummary summary = getItemSummary(parent.getId());
         PSWebserviceUtils.setUserName(request.getOriginalSubject().getName());
         //if Item is checked out by user, then LocalContent version is not updated in relationship, thus use
         // current revision of the LocalConent not the editRevision for finding the relationship
         if (isItemCheckedOutToUser(summary))
         {
            parent = new PSLocator(summary.getContentId(),summary.getCurrRevision());
            filter.setOwner(parent);
            filter.setCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
            filter.setCommunityFiltering(false);
            filter.limitToOwnerRevision(true);
            rels = processor.getRelationshipList(filter);
         }
      }

      for (PSRelationship rel : rels)
      {
         if (!StringUtils.isEmpty(rel.getProperty(PSRelationshipConfig.PDU_INLINERELATIONSHIP)))
         {
            Integer dependentid = new Integer(rel.getDependent().getId());
            inlineLinkRelData.m_relIdContentIdMap.put(
               new Integer(rel.getId()),
               dependentid);
            inlineLinkRelData.m_contentIdPathMap.put(dependentid, null);
         }
      }
   
      return inlineLinkRelData;
   }

   public static void cleanupEmptyAttributes(Element elem)
   {
      NamedNodeMap attributes = elem.getAttributes();

      // get the number of nodes in this map
      int numAttrs = attributes.getLength();
      
      List<String> removeAttrs = null; 
      for (int i = 0; i < numAttrs; i++) {
         Attr attr = (Attr) attributes.item(i);
         String name = attr.getName();
         if (StringUtils.isEmpty(attr.getValue()) && REMOVE_EMPTY_ATTRIBUTES.contains(name))
         {
            if (removeAttrs == null)
               removeAttrs = new ArrayList<String>();
            removeAttrs.add(name);
         }
      }
      
      if (removeAttrs != null)
      {
         for (String att : removeAttrs)
            elem.removeAttribute(att);
      }
      
      
   }
   
   /**
    * Modify normal links and images to point to corerct versions and revisions.
    * @param elem anchor or image element to replace the contentid with the
    * correct one. Assumed not <code>null</code>
    * @param request request context, assumed not <code>null</code>.
    * @param isImage <code>true</code> if the tag (forst parameter) is for
    * an image, <code>false</code> for a link.
    * @param inlineLinkRelData inline link relationship data for the parent
    * item. This can be built using the static method
    * {@link PSSingleValueBuilder#buildRelationshipData(IPSRequestContext,
    * PSLocator)}. Assumed not <code>null</code>.
    * @throws PSCmsException if it is not able to fix last public revision
    *    in the url that the <code>elem</code> holds.
    */
   @SuppressWarnings("unchecked")
   private static void modifyLink(
      Element elem,
      IPSRequestContext request,
      boolean isImage,
      PSRelationshipData inlineLinkRelData)
      throws PSCmsException
   {
      String linkAttr = "href";
      if(isImage)
         linkAttr = "src";

      String href = elem.getAttribute(linkAttr);
      String contentid = elem.getAttribute(IPSHtmlParameters.SYS_DEPENDENTID);
      String relid = elem.getAttribute(IPSHtmlParameters.SYS_RELATIONSHIPID);
      String variantid =
         elem.getAttribute(IPSHtmlParameters.SYS_DEPENDENTVARIANTID);

      String siteid = elem.getAttribute(IPSHtmlParameters.SYS_SITEID).trim();

      String folderid = elem.getAttribute(IPSHtmlParameters.SYS_FOLDERID);

      //Get the siteid and folder id from relationship
      Map<String, String> relMap = getSiteAndFolderFromRelationship(
            relid, contentid, request);
      //If they exist update the site and folder ids
      siteid = StringUtils.isNotBlank(relMap.get(IPSHtmlParameters.SYS_SITEID))
            ? relMap.get(IPSHtmlParameters.SYS_SITEID)
            : siteid;
      folderid = StringUtils.isNotBlank(relMap
            .get(IPSHtmlParameters.SYS_FOLDERID)) ? relMap
            .get(IPSHtmlParameters.SYS_FOLDERID) : folderid;
      
      // siteid or folderid might have been modified from relationship set them
      // back on element as attributes.
      if(StringUtils.isNotBlank(siteid))
         elem.setAttribute(IPSHtmlParameters.SYS_SITEID, siteid);
      if(StringUtils.isNotBlank(folderid))
         elem.setAttribute(IPSHtmlParameters.SYS_FOLDERID, folderid);
      
      //If we do not find the siteid from relationships, use the site id from the request
      if (siteid.length()<1)
         siteid = request.getParameter(IPSHtmlParameters.SYS_SITEID);

      String originalSiteId = request
            .getParameter(IPSHtmlParameters.SYS_ORIGINALSITEID);
      String newContentId =
         getCorrectedContentId(request, contentid, relid, inlineLinkRelData);
      /*
       * If new contentid cannot be found, we replace the link with selected
       * text from the link.
       */
      if (newContentId == null)
      {
         int msgCode = IPSServerErrors.RAW_DUMP;
         String msg =
            "Could not compute corrected contentid for the inline link with "
               + IPSHtmlParameters.SYS_RELATIONSHIPID
               + " = "
               + relid
               + " and contentid = "
               + contentid;
         Object[] args = { msg };
         PSLogManager.write(new PSLogServerWarning(msgCode, args, false, null));
         
         addClass(elem,PERC_BROKENLINK);
         removeClass(elem,PERC_NOTPUBLICLINK);
         return;
      }
      
      
      
      
      String context =
         request.getParameter(IPSHtmlParameters.SYS_CONTEXT, "0");

      String authtype =
         request.getParameter(IPSHtmlParameters.SYS_AUTHTYPE, AUTHTYPE_ALL);

      if (!newContentId.equals(contentid))
      {
         elem.setAttribute(
            IPSHtmlParameters.SYS_DEPENDENTID,
            newContentId);
      }

      if (getLinkState(elem,Integer.parseInt(newContentId)))
         return;
      
      
      
      Map params = new HashMap();
      params.put(IPSHtmlParameters.SYS_CONTENTID, newContentId);
      params.put(IPSHtmlParameters.SYS_VARIANTID, variantid);
      params.put(IPSHtmlParameters.SYS_CONTEXT, context);
      params.put(IPSHtmlParameters.SYS_AUTHTYPE, authtype);
      params.put(IPSHtmlParameters.SYS_SITEID, siteid);
      params.put(IPSHtmlParameters.SYS_FOLDERID, folderid);
      params.put(IPSHtmlParameters.SYS_ORIGINALSITEID, originalSiteId);
      
      String url = PSCms.getPublicationUrl(request, params);

      if (url == null)
         url = "";

      if (url.length() > 0)
      {
         String anchor = "";
         int index = href.indexOf("#");
         if (index > -1)
            anchor = href.substring(index + 1);

         if (anchor.length() > 0)
            url += "#" + anchor;
         if(!authtype.equals(AUTHTYPE_ALL))
         {
            String fixedUrl = null;
            PSException ex = null;
            try
            {
               fixedUrl = PSMacroUtils.fixLinkUrlRevisionForFlags(request, url,
                     "i".toCharArray());
            }
            catch (PSInternalRequestCallException e)
            {
               ex = e;
            }
            catch (PSNotFoundException e)
            {
               ex = e;
            }
            catch (PSRequestParsingException e)
            {
               ex = e;
            }
            if (ex != null)
            {
               throw new PSCmsException(ex);
            }
            if (fixedUrl == null)
               url = "";
            else
               url = fixedUrl;
         }
         elem.setAttribute(linkAttr, url);
      }else{
         elem.setAttribute(linkAttr, href);
      }

   }
   
   private static boolean getLinkState(Element elem, int contentId)
   {
      String flag = getValidFlag(contentId);
      if (flag.equals("u"))
      {
         addClass(elem,PERC_BROKENLINK);
         removeClass(elem,PERC_NOTPUBLICLINK);
         return true;
      }
      removeClass(elem,PERC_BROKENLINK);
      
      if (!flag.equals("y") && !flag.equals("i"))
      {
         addClass(elem,PERC_NOTPUBLICLINK);
      }
      else
      {
         removeClass(elem,PERC_NOTPUBLICLINK);
      }
      return false;
   }

   public static String getValidFlag(int contentid)
   {
      /**
       * First checking if object is in Recycle Bin, if "Yes" then return "u" as
       * flag to mark link as PERC_BROKENLINK
       */
       PSRelationshipFilter filter = new PSRelationshipFilter();
       filter.setCategory(PSRelationshipConfig.CATEGORY_RECYCLED);
       filter.setName(PSRelationshipConfig.TYPE_RECYCLED_CONTENT);
      PSLocator loc = new PSLocator(contentid);
      if(PSFolderRelationshipCache.getInstance().getParentPaths(loc,
              PSRelationshipConfig.TYPE_RECYCLED_CONTENT).length>0){
               return  "u";
       }

      IPSWorkflowService svc = PSWorkflowServiceLocator.getWorkflowService();
      PSComponentSummary componentSummary = PSCmsObjectMgrLocator.getObjectManager().loadComponentSummary(contentid);
      PSState state = svc.loadWorkflowState(new PSGuid(PSTypeEnum.WORKFLOW_STATE,componentSummary.getContentStateId()), new PSGuid(PSTypeEnum.WORKFLOW,componentSummary.getWorkflowAppId()));
      return state.getContentValidValue();
   }
   
   public static void addClass(Element elem, String className)
   {
   
      String classAttrib = elem.getAttribute("class");
      if(StringUtils.isBlank(classAttrib))
         elem.setAttribute("class", className);
      else if (!StringUtils.contains(classAttrib, className)){
          classAttrib = classAttrib + " "+ className;
          elem.setAttribute("class", classAttrib.trim());
      }
   }
   public static void removeClass(Element elem, String className)
   {
      String classAttrib = elem.getAttribute("class");
      if(StringUtils.contains(classAttrib, className)) {
         classAttrib = StringUtils.replace(classAttrib,className, "");
         classAttrib = StringUtils.replace(classAttrib, "  "," ");
         if (classAttrib.length()>0)
            elem.setAttribute("class", classAttrib.trim());
         else
            elem.removeAttribute("class");
      }
   }
   /**
    * Gets the property value of the specified AA relationship.
    *  
    * @param aaRel the AA relationship; assumed not <code>null</code>.
    * @param propertyName the name of the property; assumed not 
    *    <code>null</code> or empty.
    * @param defaultValue the returned (default) value if the property
    *    is not specified (empty) or <code>-1</code>. 
    * 
    * @return the property value of the AA relationship or the defaultValue
    *    as described above. Never <code>null</code> or empty.
    */
   private static String getAaProperty(PSRelationship aaRel,
         String propertyName, String defaultValue)
   {
      String propertyValue = aaRel.getProperty(propertyName);
      if (StringUtils.isBlank(propertyValue) || propertyValue.equals("-1"))
         return defaultValue;
      else
         return propertyValue;
   }
   
   /**
    * Utility method to replace the variant by requesting latest output from
    * the given parameters.
    *
    * @param pssessionid The session id, it may not be <code>null</code> or
    *    empty.
    * @param sys_dependentid The content id, it may be the attribute
    *    of <code>IPSHtmlParameters.SYS_DEPENDENTID</code> of an element.
    *    Assume not <code>null</code> or empty.
    * @param sys_dependentvariantid The variant id, it may be the attribute
    *    of <code>IPSHtmlParameters.SYS_DEPENDENTVARIANTID</code> of an element.
    *    Assume not <code>null</code> or empty.
    * @param rx_selectedtext The selected text, it may be the attribute
    *    of <code>PSInlineLinkField.RX_INLINESLOT</code> of an element.
    *    It may be <code>null</code> or empty.
    * @param rx_inlinetype The inline type, it may be the attribute of
    *    <code>PSInlineLinkField.RX_INLINETYPE</code> of an element.
    *    Assume not <code>null</code> or empty.
    * @param sys_relationshipid The relationship id, it may be the attribute of
    *    <code>IPSHtmlParameters.SYS_RELATIONSHIPID</code> of an element.
    *    Assume not <code>null</code> or empty.
    * @param sys_authtype The authtype, it may be the attribute of
    *    <code>IPSHtmlParameters.SYS_AUTHTYPE</code> of an element.
    *    Assume not <code>null</code> or empty.
    * @param sys_context The authtype, it may be the attribute of
    *    <code>IPSHtmlParameters.SYS_CONTEXT</code> of an element.
    *    Assume not <code>null</code> or empty.
    * @param sys_siteid The site id, it may be the attribute of
    *    <code>IPSHtmlParameters.SYS_SITEID</code> of an element.
    *    It may be <code>null</code> or empty if not defined.
    * @return The element which contains the latest output of the specified
    *    variant. It may be <code>null</code> if an error occurs.
    * @throws PSCmsException if it is not able to fix last public revision
    *    in the url that the elem holds.
    * @deprecated This method was written to access it from sys_InlineLinks.xsl
    *    to replace the variants, but the logic has been moved to
    *    sys_AddAssemblerInfo exit.
    */
   public static String replaceVariant(
         String pssessionid,
         String sys_dependentid,
         String sys_dependentvariantid,
         String rx_selectedtext,
         String rx_inlineslot,
         String rx_inlinetype,
         String sys_relationshipid,
         String sys_authtype,
         String sys_context,
         String sys_siteid)
      throws PSCmsException
   {
      if (pssessionid == null || pssessionid.trim().length() == 0)
         throw new IllegalArgumentException(
            "pssessionid may not be null or empty");
      if (sys_dependentid == null || sys_dependentid.trim().length() == 0)
         throw new IllegalArgumentException(
            "sys_dependentid may not be null or empty");
      if (sys_dependentvariantid == null
         || sys_dependentvariantid.trim().length() == 0)
         throw new IllegalArgumentException(
            "sys_dependentvariantid may not be null or empty");
      if (rx_inlineslot == null || rx_inlineslot.trim().length() == 0)
         throw new IllegalArgumentException(
            "rx_inlineslot may not be null or empty");
      if (rx_inlinetype == null || rx_inlinetype.trim().length() == 0)
         throw new IllegalArgumentException(
            "rx_inlinetype may not be null or empty");
      if (sys_relationshipid == null || sys_relationshipid.trim().length() == 0)
         throw new IllegalArgumentException(
            "sys_relationshipid may not be null or empty");
      if (sys_authtype == null || sys_authtype.trim().length() == 0)
         throw new IllegalArgumentException(
            "sys_authtype may not be null or empty");
      if (sys_context == null || sys_context.trim().length() == 0)
         throw new IllegalArgumentException(
            "sys_context may not be null or empty");


      PSUserSession sess = PSUserSessionManager.getUserSession(pssessionid);
      PSSecurityToken token = new PSSecurityToken(sess);
      PSRequest req = new PSRequest(token);
      PSRequestContext reqContext = new PSRequestContext(req);

      Element varBody =
         replaceVariant(
            pssessionid,
            sys_dependentid,
            sys_dependentvariantid,
            rx_selectedtext,
            rx_inlineslot,
            rx_inlinetype,
            sys_relationshipid,
            sys_authtype,
            sys_context,
            sys_siteid,
            null,
            null,
            null,
            reqContext);

      String retVal = null;

      if (varBody == null)
      {
         retVal = rx_selectedtext;
      }
      else
      {
         Document doc = varBody.getOwnerDocument();
         PSXmlDocumentBuilder.replaceRoot(doc, varBody);

         retVal = PSXmlDocumentBuilder.toString(doc);
      }

      return retVal;
   }

   /**
    * Method to replace the current inline variant with correct one. This is
    * required only if a dependent item's promotable version replaces the
    * dependent.
    * @param elem Variant output element which may have to be replaced. Must
    * not be <code>null</code>
    * @param request request context, must not be <code>null</code>.
    * @param inlineLinkRelData inline link relationship data for the parent
    * item. This can be built using the static method
    * {@link PSSingleValueBuilder#buildRelationshipData(IPSRequestContext,
    * PSLocator)}. Must not be <code>null</code>.
    * @return element which contains the latest output of the specified
    *    variant. It may be <code>null</code> if an error occurs.
    * @throws PSCmsException if it is not able to fix last public revision
    *    in the url that the elem holds.
    * 
    */
   public static Element replaceVariant(
      Element elem,
      IPSRequestContext request,
      PSRelationshipData inlineLinkRelData)
      throws PSCmsException
   {
      if (elem == null)
         throw new IllegalArgumentException("elem must not be null");

      if (request == null)
         throw new IllegalArgumentException("request must not be null");

      if (inlineLinkRelData == null)
         throw new IllegalArgumentException("in must not be null");

      String contentid = elem.getAttribute(
           IPSHtmlParameters.SYS_DEPENDENTID);
      String variantid = elem.getAttribute(
           IPSHtmlParameters.SYS_DEPENDENTVARIANTID);
      String rxselectedtext = elem.getAttribute(
           PSInlineLinkField.RX_SELECTEDTEXT);
      String rxinlineslot = elem.getAttribute(
           PSInlineLinkField.RX_INLINESLOT);
      String inlinetype = elem.getAttribute(
           PSInlineLinkField.RX_INLINETYPE);
      String relationshipid = elem.getAttribute(
                               IPSHtmlParameters.SYS_RELATIONSHIPID).trim();
      String siteid = elem.getAttribute(IPSHtmlParameters.SYS_SITEID).trim();
      String folderid = elem.getAttribute(IPSHtmlParameters.SYS_FOLDERID)
            .trim();
      //Get the siteid and folder id from relationship
      Map<String, String> relMap = getSiteAndFolderFromRelationship(
            relationshipid, contentid, request);
      //If they exist update the site and folder ids
      siteid = StringUtils.isNotBlank(relMap.get(IPSHtmlParameters.SYS_SITEID))
            ? relMap.get(IPSHtmlParameters.SYS_SITEID)
            : siteid;
      folderid = StringUtils.isNotBlank(relMap
            .get(IPSHtmlParameters.SYS_FOLDERID)) ? relMap
            .get(IPSHtmlParameters.SYS_FOLDERID) : folderid;
            
      // siteid or folderid might have been modified from relationship
      if(StringUtils.isNotBlank(siteid))
         elem.setAttribute(IPSHtmlParameters.SYS_SITEID, siteid);
      if(StringUtils.isNotBlank(folderid))
         elem.setAttribute(IPSHtmlParameters.SYS_FOLDERID, folderid);
      
      //If we do not find the siteid from relationships, use the site id from the request
      if (siteid.length()<1)
         siteid = request.getParameter(IPSHtmlParameters.SYS_SITEID);

      String originalSiteId = request
            .getParameter(IPSHtmlParameters.SYS_ORIGINALSITEID);

      String authtype = request.getParameter(
         IPSHtmlParameters.SYS_AUTHTYPE, AUTHTYPE_ALL);
      String context = request.getParameter(IPSHtmlParameters.SYS_CONTEXT, "0");

      contentid =
         getCorrectedContentId(
            request,
            contentid,
            relationshipid,
            inlineLinkRelData);

      return replaceVariant(
         request.getUserSessionId(),
         contentid,
         variantid,
         rxselectedtext,
         rxinlineslot,
         inlinetype,
         relationshipid,
         authtype,
         context,
         siteid,
         folderid,
         originalSiteId,
         elem,
         request);
   }

   /**
    * Returns a map consistsing of sys_siteid,sys_folderid as keys and their
    * values from supplied relationship as values. The values will be empty if
    * failed to get the relationship properties are property is -1.
    * @param rid relationship id, if blank returns empty map.
    * @param contentid the content id of the item for which the request is made.
    * This is used for logging purpose only.
    * @param request The request must not be null. 
    * @return map consisting of sys_siteid and sys_folderid properties. Never
    * <code>null</code> may be empty.
    */
   public static Map<String,String> getSiteAndFolderFromRelationship(String rid,
         String contentid,
         IPSRequestContext request)
   {
      if (request == null)
         throw new IllegalArgumentException("request must not be null");
      Map<String, String> relProps = new HashMap<String, String>();
      if (StringUtils.isBlank(rid) || request == null)
      {
         return relProps;
      }
      // Update siteid and folder id from the relationship. We assume these
      // with relationship are the current ones.
      try
      {
         PSRelationship aaRel = loadRelationship(request, rid);
         String siteid = getAaProperty(aaRel, IPSHtmlParameters.SYS_SITEID, "");
         String folderid = getAaProperty(aaRel, IPSHtmlParameters.SYS_FOLDERID,
               "");
         relProps.put(IPSHtmlParameters.SYS_SITEID, siteid);
         relProps.put(IPSHtmlParameters.SYS_FOLDERID, folderid);
      }
      catch (PSCmsException e)
      {
         /*
          * The relationship may or may not exist with the id in the content.
          * For example user may create an inline link or image or variant to
          * another item and may delete the depenedent item after that. When he
          * previews the parent the relationship does not exist anymore This
          * case is already handled hence just log and do nothing.
          */
         int msgCode = IPSServerErrors.RAW_DUMP;
         String msg = "Failed to load relationship "
               + IPSHtmlParameters.SYS_RELATIONSHIPID
               + " = "
               + rid
               + " and contentid = " + contentid;
         Object[] args =
         {msg};
         PSLogManager.write(new PSLogServerWarning(msgCode, args, false, null));
      }
      return relProps;
   }
   
   /**
    * Load the relationship object for a given value of relationship id.
    * 
    * @param request request context used to talk to server, assume dnot
    *           <code>null</code>.
    * @param relationshipid relationship id string, assumed to be parsed to a
    *           positive integer value.
    * @return relationship object for the relationship id supplied, never
    *         <code>null</code>.
    * @throws PSCmsException if it cannot find or load the object for any
    *            reason.
    */
   private static PSRelationship loadRelationship(IPSRequestContext request,
      String relationshipid) throws PSCmsException
   {
      IPSRelationshipService svc = PSRelationshipServiceLocator
            .getRelationshipService();
      PSRelationship rel = null;
      try
      {
         rel = svc.loadRelationship(Integer.parseInt(relationshipid));
      }
      catch (NumberFormatException e)
      {
         throw new PSCmsException(IPSCmsErrors.LOAD_AA_RELATIONSHIP_FAILED,
               new Object[]
               {
                  relationshipid
               },e);
      }
      catch (PSException e)
      {
         throw new PSCmsException(IPSCmsErrors.LOAD_AA_RELATIONSHIP_FAILED,
               new Object[]
               {
                     relationshipid
               },e);
      }
    
      if(rel==null)
      {
         throw new PSCmsException(IPSCmsErrors.LOAD_AA_RELATIONSHIP_FAILED,
            new Object[]
            {
                  relationshipid
            });
      }
      return rel;
   }

   /**
    * Given the contentid and relationshipid, this method finds the correct
    * contentid based on the following algorithm.
    * <ol>
    * <li>It assumes that the contentid supplied could be wrong because a
    * promotable version of the item with this contentid could have been
    * promoted to replace the current item</li>
    * <li>Checks to see if the inline item's contentid is present in the
    * dependent items of all outgoing AA relationshps for the parent item.</li>
    * <li>If present, the dependent item is NOT replaced by its promotable
    * version and hence contentid does not change. This is the most probable
    * case.</li>
    * <li>If not present, continure with follwing steps.</li>
    * <li>Check to see if the inline link's relationshipid is present in AA
    * relationships</li>
    * <li>If present, replace the contentid of the inline item with the dependent item
    * contentid of the matching relationship.</li>
    * <li>If not present, continue withe following steps</li>
    * <li>For the dependent item of the each AA relationship for the parent item
    * find the tree of promotable versions</li>
    * <li>If the inline item's contentid is present anywhere in the tree, the
    * dependent item of the relationship is assumed to be the correct one.
    * @param request request context, must not be <code>null</code>.
    * @param contentid contentid (as string) of the the inline link item, must
    * not be <code>null</code> or empty.
    * @param relationshipid relationshipid (as string) of the the inline link
    * item, may be <code>null</code> or empty, in which case it is assumed that
    * it is not available and the method looks up the promotable versions
    * without using relationship id.
    * @param inlineLinkRelData  inline link relationship data for the parent
    * item. This can be built using the static method
    * {@link PSSingleValueBuilder#buildRelationshipData(IPSRequestContext,
    * PSLocator)}. Must not be <code>null</code>.
    * @return the correct contentid as string. <code>null</code> if not found
    * one.
    * @throws PSCmsException if error occurs during finding the correct version
    * of the inline item.
    */
   @SuppressWarnings("unchecked")
   public static String getCorrectedContentId(
      IPSRequestContext request,
      String contentid,
      String relationshipid,
      PSRelationshipData inlineLinkRelData)
      throws PSCmsException
   {
      if (request == null)
         throw new IllegalArgumentException("request must not be null");

      if (contentid == null || contentid.length()<1)
         throw new IllegalArgumentException("contentid must not be null");

      if (inlineLinkRelData == null)
         throw new IllegalArgumentException("inlineLinkRelData must not be null");

      if (relationshipid == null || relationshipid.length() < 1)
         relationshipid = "-1";

      Integer cid = new Integer(contentid);
      if(!inlineLinkRelData.m_contentIdPathMap.keySet().contains(cid))
      {
         Integer rid = new Integer(relationshipid);
         Integer newCid =
            (Integer) inlineLinkRelData.m_relIdContentIdMap.get(rid);
         if(newCid!=null)
         {
            contentid = newCid.toString();
         }
         else
         {
            boolean found = false;
            Iterator cids =
               inlineLinkRelData.m_contentIdPathMap.keySet().iterator();
            while (cids.hasNext())
            {
               Integer entry = (Integer) cids.next();
               List locatorList =
                  (List) inlineLinkRelData.m_contentIdPathMap.get(entry);
               if (locatorList == null)
               {
                  locatorList = getPromotedIdList(entry, request);
                  inlineLinkRelData.m_contentIdPathMap.put(entry, locatorList);
               }
               if (locatorListContains(locatorList, contentid))
               {
                  contentid = entry.toString();
                  found = true;
                  break;
               }
            }
            contentid = (found == true) ? contentid : null;
         }
      }
      return contentid;
   }

   /**
    * Helper method to test if the supplied lit of item locators containts the
    * contentid supplied.
    * @param locatorList list of locators to check existence from. Assumed not
    * <code>null</code>.
    * @param contentid contentid to check for, must not be <code>null</code>
    * or empty.
    * @return <code>tue</code> if the locator list contains he contentid,
    * <code>false</code> otherwise.
    */
   private static boolean locatorListContains(List locatorList, String contentid)
   {
      if(contentid==null)
         return false;

      int cid = Integer.parseInt(contentid);
      Iterator iter = locatorList.iterator();
      while (iter.hasNext())
      {
         PSLocator locator = (PSLocator) iter.next();
         if(locator.getId()==cid)
            return true;
      }
      return false;
   }

   /**
    * Helper method to build a list of locator of promotabe versions for an
    * item with specified contentid. Walks throw all promotable version
    * category relationships to generated the list.
    * @param contentid contentid of the item to get the verions' locator list.
    * @param request request context to making internal request to get
    * promotable versions of an item.
    * @return List of locators all promotable versions of the item with
    * contentid supplied.
    */
   @SuppressWarnings("unchecked")
   private static List getPromotedIdList(
      Integer contentid,
      IPSRequestContext request) throws PSCmsException
   {
      if (contentid == null)
         throw new IllegalArgumentException("contentid must not be null");
      if (request == null)
         throw new IllegalArgumentException("request must not be null");

      PSRelationshipDbProcessor processor = PSRelationshipDbProcessor.getInstance();
      Iterator configs = PSRelationshipCommandHandler.getRelationshipConfigs();
      List result = new ArrayList();
      while (configs.hasNext())
      {
         PSRelationshipConfig cfg = (PSRelationshipConfig) configs.next();
         if(!cfg.getCategory().equals(PSRelationshipConfig.CATEGORY_PROMOTABLE))
            continue;
         result.addAll(processor.getOwnerLocators(new PSLocator(contentid
               .intValue()), cfg.getName()));
      }
      return result;
   }

   /**
    * Utility method to replace the variant by requesting latest output from
    * the given parameters.
    *
    * @param pssessionid The session id, assume not <code>null</code> or
    *    empty.
    * @param contentid The content id, assume not <code>null</code> or empty.
    * @param variantid The variant id, assume not <code>null</code> or empty.
    * @param rxselectedtext The selected text, it may be the attribute
    *    of <code>PSInlineLinkField.RX_INLINESLOT</code> of an element.
    *    It may be <code>null</code> or empty.
    * @param inlinetype The inline type, it may be the attribute of
    *    <code>PSInlineLinkField.RX_INLINETYPE</code> of an element.
    *    Assume not <code>null</code> or empty.
    * @param relationshipid The relationship id, it may be the attribute of
    *    <code>IPSHtmlParameters.SYS_RELATIONSHIPID</code> of an element.
    *    Assume not <code>null</code> or empty.
    * @param authtype The authtype, it may be the attribute of
    *    <code>IPSHtmlParameters.SYS_AUTHTYPE</code> of an element.
    *    Assume not <code>null</code> or empty.
    * @param context The authtype, it may be the attribute of
    *    <code>IPSHtmlParameters.SYS_CONTEXT</code> of an element.
    *    Assume not <code>null</code> or empty.
    * @param siteid The site id, it may be the attribute of
    *    <code>IPSHtmlParameters.SYS_SITEID</code> of an element.
    *    It may be <code>null</code> or empty if not defined.
    * @param folderid item's parent folderid, may be <code>null</code> in which
    *    case the request to teh variant will not have the folderid.
    * @param originalsiteid original siteid, may be <code>null</code> in which
    *    case the request to teh variant will not have the original siteid.
    * @param elem Variant output element which needs to be replaced if it is
    *    not <code>null</code>.
    * @param request IPSRequestContext Object assumed not null.
    * @return The element which contains the latest output of the specified
    *    variant. It may be <code>null</code> if an error occurs.
    * @throws PSCmsException if it is not able to fix last public revision
    *    in the url that the elem holds.
    * 
    */
   @SuppressWarnings("unchecked")
   private static Element replaceVariant(
      String pssessionid,
      String contentid,
      String variantid,
      String rxselectedtext,
      String rxinlineslot,
      String inlinetype,
      String relationshipid,
      String authtype,
      String context,
      String siteid,
      String folderid,
      String originalsiteid,
      Element elem,
      IPSRequestContext request)
      throws PSCmsException
   {
       if (rxselectedtext == null)
         rxselectedtext = "";
       if(contentid == null || contentid.trim().length() < 1 ||
            rxinlineslot == null || rxinlineslot.trim().length() < 1 ||
            variantid == null || variantid.trim().length() < 1 ||
            inlinetype == null || inlinetype.trim().length() < 1)
       {
           /*This should not happen,
            *but may happen if user opens the ektron control in html
            *mode and changes some thing over there.
            *We can not fix the variants in that case.
            *Just print the trace message.*/
            request.printTraceMessage(
                "Missing required parameters. Continuing without " +
                "fixing the variants on opening the document.");
            return null;
       }

       //Build a map of required attributes
       Map paramMap = new HashMap(6);
       paramMap.put(IPSHtmlParameters.SYS_SESSIONID, pssessionid);
       paramMap.put(IPSHtmlParameters.SYS_CONTEXT, context);
       paramMap.put(IPSHtmlParameters.SYS_AUTHTYPE, authtype);
       paramMap.put(IPSHtmlParameters.SYS_CONTENTID, contentid);
       paramMap.put(IPSHtmlParameters.SYS_VARIANTID, variantid);
       paramMap.put(IPSHtmlParameters.SYS_SLOTID, rxinlineslot);
       if (siteid != null && siteid.trim().length() != 0)
         paramMap.put(IPSHtmlParameters.SYS_SITEID, siteid);
       if (folderid != null && folderid.trim().length() != 0)
          paramMap.put(IPSHtmlParameters.SYS_FOLDERID, folderid);
       if (originalsiteid != null && originalsiteid.trim().length() != 0)
          paramMap.put(IPSHtmlParameters.SYS_ORIGINALSITEID, originalsiteid);
       String command = request.getParameter(IPSHtmlParameters.SYS_COMMAND);

       if (command != null)
         paramMap.put(IPSHtmlParameters.SYS_COMMAND, command);
      rxselectedtext = decodeSelectedText(rxselectedtext);
       paramMap.put(INLINE_TEXT, rxselectedtext);

       String variantUrl = null;

       IPSInternalRequest variantUrlReq = null;
       try
       {
           //Make an internal request to get the variant url
           variantUrlReq = request.getInternalRequest(
                VARIANTURL, paramMap, false);
           if(variantUrlReq == null)
           {
               /*This should not happen,
                *but may happen if the application is not running.
                *We can not fix the variants in that case.
                *Just print the trace message.*/
                request.printTraceMessage(
                    "Unable to make internal request to " + VARIANTURL +
                    ". Continuing without fixing the variants on opening the document.");
                return null;
           }
           Document relatedDoc = variantUrlReq.getResultDoc();
           if(relatedDoc == null)
           {
               /*This should not happen as there is an sys_emptyDoc exit on the resource.
                *Just print the trace message in case if it happens*/
                request.printTraceMessage(
                "Internal request to " + VARIANTURL + " resulted in null document." +
                " Continuing without fixing the variants on opening the document.");
                return null;
           }
           variantUrl = relatedDoc.getDocumentElement().
                getAttribute(ATTR_PREVEWURL);

           //Add the new assembly level parameter to the variant URL for round tripping 
           variantUrl = AssemblerInfoUtils.appendNewAssemblyLevelParam(request, variantUrl);
             
       }
       catch(Exception e)
       {
           /*This should not happen.
            *Just print the trace message incase if it happens*/
            request.printTraceMessage(e.getMessage());
            return null;
       }
       if(variantUrl == null || variantUrl.trim().length() < 1)
       {
           /*Oh we could not find the variant url for the item represented
            * by contentid and variantid, either somebody might have deleted
            * the item or variant itself.
            *Remove the variant from this document.
            */
           if (elem != null)
           {
              elem.getParentNode().removeChild(elem);
              return elem;
           }
         return null;
       }
       String fixedUrl = variantUrl;
       if(!authtype.equals(AUTHTYPE_ALL))
       {
         PSException ex = null;
         try
         {
            fixedUrl = PSMacroUtils.fixLinkUrlRevisionForFlags(request,
                  variantUrl, "i".toCharArray());
         }
         catch (PSInternalRequestCallException e)
         {
            ex = e;
         }
         catch (PSNotFoundException e)
         {
            ex = e;
         }
         catch (PSRequestParsingException e)
         {
            ex = e;
         }
         if (ex != null)
         {
            throw new PSCmsException(ex);
         }
         if (fixedUrl == null)
         {
            if (elem != null)
            {
               elem.getParentNode().removeChild(elem);
               return elem;
            }
            return null;
         }
       }
      
      IPSAssemblyResult result;
      try
      {
         result = 
            PSAssemblyServiceUtils.getAssembledDocumentResult(
               fixedUrl, paramMap);
      }
      catch (Exception e)
      {
         request.printTraceMessage(e.getLocalizedMessage());
         return null;
      }
       
      byte[] byteResult = null;
      if (result.getStatus() != Status.SUCCESS)
      {
         request.printTraceMessage("Failed to expand inline template");
         return null;
      }
      else if (!result.getMimeType().startsWith("text/html"))
      {
         request
            .printTraceMessage("Inline template expanded to non-html value: "
               + result.getMimeType());
         return null;
      }
      else
      {
         byteResult = result.getResultData();
      }
      ByteArrayInputStream bis = new ByteArrayInputStream(byteResult);
      Document doc = null;
      try
      {
         doc = PSXmlDocumentBuilder.createXmlDocument(bis, false);
      }
      catch (Exception e)
      {
         /*
          * This should not happen. Just print the trace message incase if it
          * happens
          */
         request.printTraceMessage(e.getMessage());
         return null;
      }
      if(doc == null)
      {
          /*This should not happen.
           *Just print the trace message incase if it happens*/
           request.printTraceMessage(
              "Internal request to " + variantUrl +
              " resulted in null output. Continuing without " +
              "fixing this variant on opening the document.");
           return null;
      }

      NodeList nl = doc.getElementsByTagName(ELEM_BODY);
      if (nl != null && nl.getLength() > 0)
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(nl.item(0));
         Element varBody = tree.getNextElement(true);
         if (varBody == null)
         {
            /*
             * This will happen with badly designed variants. Just print the
             * trace message if it happens
             */
            request.printTraceMessage("The output of variant with url "
               + variantUrl + " produced a tree without a child element under"
               + " body element. Continuing without "
               + "fixing this variant on opening the document.");
            return null;
         }
         varBody.setAttribute(IPSHtmlParameters.SYS_DEPENDENTID, contentid);
         varBody.setAttribute(IPSHtmlParameters.SYS_DEPENDENTVARIANTID,
            variantid);
         varBody.setAttribute(PSInlineLinkField.RX_INLINESLOT, rxinlineslot);
         varBody
            .setAttribute(PSInlineLinkField.RX_SELECTEDTEXT, rxselectedtext);
         varBody.setAttribute(PSInlineLinkField.RX_INLINETYPE, inlinetype);
         varBody.setAttribute(IPSHtmlParameters.SYS_RELATIONSHIPID,
            relationshipid);
         if (siteid != null && siteid.length() > 0)
            varBody.setAttribute(IPSHtmlParameters.SYS_SITEID, siteid);
         if (folderid != null && folderid.length() > 0)
            varBody.setAttribute(IPSHtmlParameters.SYS_FOLDERID, folderid);
         addReadOnlyAttributes(varBody);
         if (elem != null)
         {
            Node varNode = elem.getOwnerDocument().importNode(varBody, true);
            elem.getParentNode().replaceChild(varNode, elem);
            return (Element)varNode;
         }
         return varBody;
      }
      return null;
   }

   /**
    * Decodes the supplied selectedText using
    * <code>Codecs.URLDecode(text)</code>. Extracted this code into a
    * separate method so that both editor and assembly uses the same code to
    * decode the selected text. 
    * The selected text needs to be decoded as the
    * original text has been "escaped()" by the Java-Script on the client
    * (browser).
    * 
    * However, Codecs.URLDecode() only works with single byte characters it
    * doesn't work with multi-byte characters. This is fine in this specific
    * situation, because we are expecting the (client) Java-Script will convert
    * multi-byte characters to numeric character reference (in the format of
    * "&#D;", "&#XH" or "&#xH", where "D" is decimal numeric, "H" is hex numeric
    * value.
    * 
    * @param selectedText The text that needs to be decoded, if
    *           <code>null</code> or empty returns empty String.
    * @return String Decoded string or empty String. Never <code>null</code>.
    */
   public static String decodeSelectedText(String selectedText)
   {
      if(StringUtils.isBlank(selectedText))
         return "";
      String result = "";
      try
      {
         result = Codecs.URLDecode(selectedText);
      }
      catch (ParseException e1) // ignore exception
      {
         log.error(e1.getMessage());
         log.debug(e1.getMessage(), e1);
      }
      return result;
   }
   
   /**
    * Utility method to add the non selectable and non editable attributes all
    * the element nodes in a document recursively.
    * 
    * @param elem root Element of the document. If <code>null</code> no
    * attributes will be added.
    */
    private static void addReadOnlyAttributes(Element elem)
    {
      if (elem != null)
      {
            //Add attributes
            if(!ms_noReadOnlyAttribs.contains(
               elem.getNodeName().toLowerCase()))
            {
               elem.setAttribute(
                   CONTENT_EDITABLE,CONTENT_EDITABLE_FALSE);
               elem.setAttribute(
                   UN_SELECTABLE,UN_SELECTABLE_ON);
            }
            // recurse through all child elements
            if (elem.hasChildNodes())
            {
               NodeList children = elem.getChildNodes();
               for (int i=0; i<children.getLength(); i++)
               {
                  Node test = children.item(i);
                  if (test.getNodeType() == Node.ELEMENT_NODE)
                  addReadOnlyAttributes((Element) test);
               }
            }
       }
    }

   /**
    * The runtime representation of the DefaultValue, if there is one. May be
    * <code>null</code>.
    */
   private IPSDataExtractor m_defExtractor;

   /**
    * The runtime representation of the data value. May be <code>null</code>.
    */
   private IPSDataExtractor m_extractor;

   /**
    * The resource to get the variant url, never
    * <code>null</code>.
    */
   private static final String VARIANTURL =
      "sys_ceInlineSearch/varianturl";

   /**
    * The values of inline link types.
    */
   private static String RX_INLINETYPE_HYPERLINK = "hyperlink";
   private static String RX_INLINETYPE_IMAGE = "image";
   private static String RX_INLINETYPE_RXVARIANT = "rxvariant";
   private static String RX_INLINETYPE_RXHYPERLINK = "rxhyperlink";
   private static String RX_INLINETYPE_RXIMAGE = "rximage";

   /**
    * Attribute name to represent the selected text.
    */
   public static String INLINE_TEXT = "inlinetext";

   /**
    * Html element name body.
    */
   private static String ELEM_BODY = "body";

    /**
     * Attribute name to represent the anchor text.
     * Does not include the leading hash.
     */
    public static String ANCHOR_TEXT = "anchortext";

   /**
    * Attribute name that holds preview url
    */
   private static String ATTR_PREVEWURL = "previewurl";

   /**
    * Attribute name to make html elements non editable
    */
   private static String CONTENT_EDITABLE = "contenteditable";

   /**
    * Value of content editable attribute to make it non editable
    */
   private static String CONTENT_EDITABLE_FALSE = "false";

   /**
    * Attribute name to make html elements non selectable
    */
   private static String UN_SELECTABLE = "unselectable";

   /**
    * Value of unselectable attribute to make it non selectable
    */
   private static String UN_SELECTABLE_ON = "on";

   /**
    * List of tags that cannot contain "contenteditable" or "unselectable"
    * attributes, i.e. the read only attributes.
    */
  private static List ms_noReadOnlyAttribs = new ArrayList();

  static
  {
     ms_noReadOnlyAttribs.add("li");
     ms_noReadOnlyAttribs.add("ol");
     ms_noReadOnlyAttribs.add("ul");
  }

   /**
    * Constant that defines the one of the possible values for sys_authtype
    * HTML parameter. It means all content.
    */
   private static final String AUTHTYPE_ALL = "0";
}


