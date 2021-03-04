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
package com.percussion.cms;

import static com.percussion.utils.xml.PSSaxHelper.ELEMENTS_NO_SELF_CLOSE_LIST;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static com.percussion.util.PSXMLDomUtil.getElementData;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSContentWorkflowState;
import com.percussion.services.workflow.data.PSState;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSStringOperation;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.PSProcessServerPageTags;
import com.percussion.xml.PSNodePrinter;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * A container to hold all information and behavier required to process fields
 * that may contain inline links.
 */
public class PSInlineLinkField
{

   private static final Logger log = LogManager.getLogger(PSInlineLinkField.class);
   /**
    * Constructs a new instance for the supplied field.
    *
    * @param field a field that may contain inline links, not <code>null</code>.
    */
   public PSInlineLinkField(PSField field)
   {
      if (field == null)
         throw new IllegalArgumentException("field cannot be null");

      m_field = field;
   }

   /**
    * Accessor to the field object.
    *
    * @return the field object, never <code>null</code>.
    */
   public PSField getField()
   {
      return m_field;
   }

   /**
    * In the pre-process, inline link field's content is parsed into an XML
    * document. All inline links within that document will be processed.
    * Then the processed document is saved back into the original source.
    * During this process the <code>m_modifies</code> and <code>m_deletes</code>
    * relationship sets are prepared for the post-process.
    *
    * @param request the request that contains the field to be processed as
    *    HTML parameter, not <code>null</code>.
    * @param deletes The relationship set used to keep track of relationships 
    *    that need to be deleted in the post process. It should be initialized
    *    by {@link getInlineRelationships(IPSRequestContext)}. It may not be 
    *    <code>null</code>, but may be empty. 
    * @param modifies The relationship set used to keep track of relationships 
    *    that need to be modified in the post process. It may not be 
    *    <code>null</code>, but may be empty.
    * 
    * @throws IOException for any I/O errors.
    * @throws SAXException for any parsing error.
    * @throws PSCmsException for all other errors.
    */
   public void preProcess(
      PSRequest request,
      PSRelationshipSet deletes,
      PSRelationshipSet modifies)
      throws PSCmsException, IOException, SAXException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
         
      preProcess(new PSRequestContext(request), deletes, modifies);
   }
   
   
   /**
    * Just like {@link preProcess(PSRequest)} except it accepts an
    * <code>IPSRequestContext</code> parameter.
    * 
    * @param request the request that contains the field to be processed as
    *    HTML parameter, not <code>null</code>.
    */
   public void preProcess(
      IPSRequestContext request,
      PSRelationshipSet deletes,
      PSRelationshipSet modifies)
      throws IOException, SAXException, PSCmsException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      if (deletes == null)
         throw new IllegalArgumentException("deletes cannot be null");
      if (modifies == null)
         throw new IllegalArgumentException("modifies cannot be null");

      String fieldValue = request.getParameter(m_field.getSubmitName());
      if (fieldValue != null && fieldValue.trim().length() > 0)
      {
         Properties tidyProperties = null;
         PSProcessServerPageTags serverPageTags = null;
         if (m_field.shouldCleanupField())
         {
            tidyProperties = new Properties();
            tidyProperties.load(new FileInputStream(PSServer.getRxDir()
                  + File.separator + m_field.getCleanupPropertiesFile()));

            serverPageTags = new PSProcessServerPageTags(new File(
               PSServer.getRxDir(), m_field.getCleanupServerPageTagFile()));
         }

         String serverRoot = "127.0.0.1:" + PSServer.getListenerPort() +
            PSServer.getRequestRoot();
         Document fieldDoc = PSXmlDocumentBuilder.createXmlDocument(
            fieldValue, serverRoot, tidyProperties, serverPageTags,
            m_field.getCleanupEncoding(), false);

         PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();

         processField(fieldDoc.getDocumentElement(), request, processor,
            m_field.cleanupBrokenInlineLinks(), deletes, modifies);

         boolean isModified = expandEmptyElement(fieldDoc);
         
         //Use the version to not to indent. Indenting may messup JavaScript 
         //if present.
         String outputString = "";
         try
         {
            StringWriter swriter = new StringWriter();
            PSNodePrinter np = new PSNodePrinter(swriter);
            np.printNode(fieldDoc);
            outputString = swriter.toString();
         }
         catch (IOException e)
         {
            //Can it happen?
         }

         if(isModified)
         {
            outputString = replaceString(outputString, ELEM_FILLER, "");
         }
       
         
         request.setParameter(m_field.getSubmitName(), outputString);
      }
   }

   /**
    * This is a utility method to expand an empty element and fill it with
    * some dummytext. You should call @link replaceString(String,String,String)
    * method to remove the dummytext.
    * @param fieldDoc must not be <code>null</code> or <code>empty</code>.
    * @return <code>true</code> if expands any empty elements otherwise 
    * <code>false</code>.
    */
   public static boolean expandEmptyElement(Document fieldDoc)
   {
      //Fix for empty elements like textareas and anchor tags.
      /* 
       * Empty elements are getting closed by tidy(like <a id="top"></a>
       * <a id="top"/>, when the same document opened in ektron, 
       * these elements are getting expanded by wrapping some other 
       * text inside them. We first add a filler text for the empty elements
       * if any and then we remove the filler text by string replace method.
       */
      boolean isModified = false;
      for(int i=0; i<ELEMENTS_NO_SELF_CLOSE_LIST.size(); i++)
      {
         NodeList nl = fieldDoc.getElementsByTagName(ELEMENTS_NO_SELF_CLOSE_LIST.get(i));
         if(nl != null && nl.getLength()>0)
         {
            for(int j=0; j<nl.getLength(); j++)
            {
               if(!nl.item(j).hasChildNodes())
               {
                  Text txt = fieldDoc.createTextNode(ELEM_FILLER);
                  nl.item(j).appendChild(txt);
                  isModified = true;
               }
            }
         }
         
      }
      return isModified;
   }

   /**
    * The post process executes all relationship modifies defined in
    * <code>modifies</code> and deletes defined in <code>deletes</code>.
    * It is caller's responsibility to call the <code>preProcess</code>
    * to get the <code>modifies</code> and <code>deletes</code>.
    *
    * @param request the request to operate on, not <code>null</code>.
    * @param deletes the to be deleted relationships, not <code>null</code>.
    * @param modifies the to be modified relationships, not <code>null</code>.
    * 
    * @throws IOException for any I/O errors.
    * @throws SAXException for any parsing error.
    * @throws PSCmsException for all other errors.
    */
   public static void postProcess(
      PSRequest request,
      PSRelationshipSet deletes,
      PSRelationshipSet modifies)
      throws IOException, SAXException, PSCmsException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      if (deletes == null)
         throw new IllegalArgumentException("deletes cannot be null");
      if (modifies == null)
         throw new IllegalArgumentException("modifies cannot be null");
         
      postProcess(new PSRequestContext(request), deletes, modifies);
   }
   
   /**
    * Just like {@link postProcess(PSRequest)} except it accepts an
    * <code>IPSRequestContext</code> parameter.
    * 
    * @param request the request that contains the field to be processed as
    *    HTML parameter, not <code>null</code>.
    */
   public static void postProcess(
      IPSRequestContext request,
      PSRelationshipSet deletes,
      PSRelationshipSet modifies)
      throws IOException, SAXException, PSCmsException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      if (deletes == null)
         throw new IllegalArgumentException("deletes cannot be null");
      if (modifies == null)
         throw new IllegalArgumentException("modifies cannot be null");

      PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
         
      // process moidifies
      processor.save(modifies);
      
      // process deletes
      processor.delete(deletes);
   }

   /**
    * Gets the relationship set member with all existing inline
    * relationships of the current item. This is required to keep track of
    * inline links that have been removed from the field so we can remove the
    * relationships as well.
    *
    * @param request the request to operate on, it may not be <code>null</code>.
    * 
    * @return a set of relationships with all existing inline relationships,
    *    never <code>null</code>, may be empty.
    */
   public static PSRelationshipSet getInlineRelationships(
      IPSRequestContext request)
      throws PSCmsException
   {
      try
      {
         if (request == null)
            throw new IllegalArgumentException("request may not be null");
      
         PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
         PSRelationshipSet result = new PSRelationshipSet();

         String contentid = request.getParameter(
            IPSHtmlParameters.SYS_CONTENTID);
         String revision = request.getParameter(
            IPSHtmlParameters.SYS_REVISION);
         PSLocator owner = new PSLocator(contentid, revision);

         PSRelationshipSet relationships = processor.getDependents(
            PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY, owner);

         for (int i=0; i<relationships.size(); i++)
         {
            PSRelationship relationship = (PSRelationship) relationships.get(i);
            String test = relationship.getProperty(
               PSRelationshipConfig.RS_INLINERELATIONSHIP);

            String slotid = relationship.getProperty(
               IPSHtmlParameters.SYS_SLOTID);
            if ((test != null && test.trim().length() > 0) || 
               isInlineSlot(slotid)) 
            {
               if ((test == null || test.trim().length() == 0)
                     && isInlineSlot(slotid))
               {
                  relationship
                     .setProperty(PSRelationshipConfig.RS_INLINERELATIONSHIP, 
                           "yes");
               }
               result.add(relationship);
            }
         }

         return result;
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
   }
   
   /**
    * Convenience method that calls 
    * {@link #modifyField(Element, Map, Collection) 
    * modifyField(elem, relationshipMap, null)}
    */
   public static void modifyField(Element elem, Map<?, ?> relationshipMap)
   {
      modifyField(elem, relationshipMap, null);
   }
   
   /**
    * Modifies the field in the supplied element for inline links. It replaces
    * the existing Inline links with the supplied new one. The inline links are
    * recognized through the attribute named <code>RX_INLINESLOT</code>.
    * All child nodes of the supplied element are processed recursivly. Inline
    * link elements can contain further inline links.
    *
    * @param elem the element to process for inline links, may not be
    *    <code>null</code>.
    *
    * @param relationshipMap the relationship mapper. It maps each existing
    *    relationship id (in <code>Integer</code> to the cloned relationship (in
    *    <code>PSRelationship</code>). It may not be <code>null</code>.
    * 
    * @param modified <code>PSRelationship</code> objects for which a link is 
    *    modified by this method are added to this collection if supplied.  May
    *    be <code>null</code>, may or may not be empty.
    */
   public static void modifyField(Element elem, Map<?,?> relationshipMap, 
      Collection<PSRelationship> modified)
   {
      if (elem == null)
         throw new IllegalArgumentException("elem may not be null");
      if (relationshipMap == null)
         throw new IllegalArgumentException("relationshipMap may not be null");

      String inlineSlot = elem.getAttribute(RX_INLINESLOT);
      if (inlineSlot != null && inlineSlot.trim().length() > 0)
      {
         modifyRelationships(elem, relationshipMap, modified);
      }

      // recurse through all child elements
      if (elem.hasChildNodes())
      {
         NodeList children = elem.getChildNodes();
         for (int i=0; i<children.getLength(); i++)
         {
            Node test = children.item(i);
            if (test.getNodeType() == Node.ELEMENT_NODE)
               modifyField((Element) test, relationshipMap, modified);
         }
      }
   }
   
   /**
    * Convenience method which calls {@link getInlineRelationshipId(
    * IPSRequestContext, PSField) getInlineRelationshipId(
    * new PSRequestContext(request), field)}.
    */
   public static String getInlineRelationshipId(PSRequest request, 
      PSField field)
   {
      return getInlineRelationshipId(new PSRequestContext(request), field);
   }
   
   /**
    * Get the inline relationship id for the supplied parameters. See 
    * {@link makeInlineRelationshipId(String, String)} for more info on the
    * inline relationship id format.
    * 
    * @param request the request for which to produce the inline relationship
    *    id, not <code>null</code>.
    * @param field the field for which to produce the inline relationship id,
    *    not <code>null</code>.
    * @return the inline relationship id for the supplied request and field,
    *    see method description for details, never <code>null</code> or empty.
    */
   public static String getInlineRelationshipId(IPSRequestContext request, 
      PSField field)
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
         
      if (field == null)
         throw new IllegalArgumentException("field cannot be null");
         
      String childrowid = request.getParameter(
         PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME);
         
      return makeInlineRelationshipId(field.getSubmitName(), childrowid);
   }
   
   /**
    * Make an inline relationship id for the supplied parameters. The id has
    * the format <code>fieldName:childRowId</code>. If the supplied child
    * row id is <code>null</code> or empty, the returned id will just be
    * the <code>fieldName</code>.
    * 
    * @param fieldName The submit name of the content editor field that 
    * contains the inline links. Never <code>null</code> or empty.
    * 
    * @param childRowId The row id of the complex child that contains the
    * field. If the field is in the parent editor, this value must be 
    * <code>null</code> or empty. This method does not validate whether
    * the supplied field should have a child id or not.
    * 
    * @return the relationship id string in the format 
    *    <code>fieldName:childRowId</code>, never <code>null</code> or empty.
    */
   public static String makeInlineRelationshipId(String fieldName, 
      String childRowId)
   {
      if (fieldName == null)
         throw new IllegalArgumentException("fieldName cannot be null");

      fieldName = fieldName.trim();
      if (fieldName.length() == 0)
         throw new IllegalArgumentException("fieldName cannot be empty");
         
      if (childRowId == null || childRowId.trim().length() == 0)
         return fieldName;

      List<String> values = new ArrayList<>();
      values.add(fieldName);
      values.add(childRowId);      
      return PSStringOperation.append(values, INLINE_RELATIONSHIP_ID_DELIMITER); 
   }
   
   /**
    * Get the field name part of the supplied inline relationship id. See
    * {@link getInlineRelationshipId(IPSRequestContext, PSField)} for info on
    * the inline relationship id.
    * 
    * @param inlineRelationshipId the inline relationship id from which to
    * get the field name part, not <code>null</code> or empty. This must 
    * be a value previously generated by {@link getInlineRelationshipId(
    * IPSRequestContext, PSField)}
    * 
    * @return the field name part from the supplied inline relationship id, 
    *    never <code>null</code> or empty.
    */
   public static String getFieldName(String inlineRelationshipId)
   {
      if (inlineRelationshipId == null)
         throw new IllegalArgumentException(
            "inlineRelationshipId cannot be null");
            
      inlineRelationshipId = inlineRelationshipId.trim();
      if (inlineRelationshipId.length() == 0)
         throw new IllegalArgumentException(
            "inlineRelationshipId cannot be empty");
       
      List<?> parts = PSStringOperation.getSplittedList(inlineRelationshipId,
            INLINE_RELATIONSHIP_ID_DELIMITER.charAt(0));
      return parts.get(0).toString();      
   }
   
   /**
    * Get the child row id part of the supplied inline relationship id. See
    * {@link getInlineRelationshipId(IPSRequestContext, PSField)} for info on
    * the inline relationship id.
    * 
    * @param inlineRelationshipId the inline relationship id from which to
    * get the field name part, not <code>null</code> or empty. This must 
    * be a value previously generated by {@link getInlineRelationshipId(
    * IPSRequestContext, PSField)}
    * 
    * @return the child row part from the supplied inline relationship id, 
    *    may be <code>null</code> for parent fields but never empty.
    */
   public static String getChildRowId(String inlineRelationshipId)
   {
      if (inlineRelationshipId == null)
         throw new IllegalArgumentException(
            "inlineRelationshipId cannot be null");
            
      inlineRelationshipId = inlineRelationshipId.trim();
      if (inlineRelationshipId.length() == 0)
         throw new IllegalArgumentException(
            "inlineRelationshipId cannot be empty");
      
      List<String> parts = PSStringOperation.getSplittedList(inlineRelationshipId,
            INLINE_RELATIONSHIP_ID_DELIMITER.charAt(0));
      if (parts.size() == 0 || parts.size() > 2)
      {
            throw new IllegalArgumentException(
                  "Supplied id was not generated by this class.");
      }
      if (parts.size() == 1)
         return null;
      else
         return parts.get(1).toString();
   }
   
   /**
    * Test if the supplied id is for a slot of type inline.
    * 
    * @param slotId the id of the slot to test, not <code>null</code> or empty.
    * @return <code>true</code> if the supplied slot id is of a slot of type
    *    inline slot, <code>false</code> otherwise.
    * @throws PSInternalRequestCallException if the lookup request to get the
    *    inline slot id's fails.
    * @throws PSUnknownNodeTypeException if the lookup request to get the 
    *    inline slot id's returns unexpected results.
    */
   public static boolean isInlineSlot(String slotId) 
      throws PSInternalRequestCallException, PSUnknownNodeTypeException
   {
      if (slotId == null)
         throw new IllegalArgumentException("slotId cannot be null");
         
      slotId = slotId.trim();
      if (slotId.length() == 0)
         throw new IllegalArgumentException("slotId cannot be empty");
            
      return getInlineSlots().contains(slotId);
   }
   
   /**
    * Get the ids of all inline slots. The collection is built the first time
    * this method is called and cached. The server is never queried again.
    * 
    * @return a list of <code>String</code> objects with all inline link
    *    slots defined at the time this method is called the first time, never
    *    <code>null</code>, may be empty.
    * @throws PSInternalRequestCallException if the lookup request to get the
    *    inline slot id's fails.
    * @throws PSUnknownNodeTypeException if the lookup request to get the 
    *    inline slot id's returns unexpected results.
    */
   public static Collection<String> getInlineSlots()
      throws PSInternalRequestCallException, PSUnknownNodeTypeException
   {
      if (ms_inlineslots == null)
      {
         ms_inlineslots = new ArrayList<>();

         PSRequest req = PSRequest.getContextForRequest();
         PSInternalRequest ir = PSServer.getInternalRequest(
            "sys_slots/slotlist.xml", req, null, false);
            
         Document doc = ir.getResultDoc();
         Element slotListElem = doc.getDocumentElement();
         PSXMLDomUtil.checkNode(slotListElem, "slotlist");
         Element slotElem = PSXMLDomUtil.getFirstElementChild(slotListElem,
            "slot");
         while (slotElem != null)
         {
            Node slotType = PSXMLDomUtil.findFirstNamedChildNode(slotElem, 
               "slottype");
            if (slotType != null)
            {
               String data = PSXMLDomUtil.getElementData(slotType);
               if (data.trim().equals(INLINE_SLOT_TYPE))
               {
                  Node id = PSXMLDomUtil.findFirstNamedChildNode(slotElem, 
                     "slotid");
                  ms_inlineslots.add(PSXMLDomUtil.getElementData(id));
               }
            }
            
            slotElem = PSXMLDomUtil.getNextElementSibling(slotElem);
         }
      }
      
      return ms_inlineslots;
   }

   /**
    * Modifies the supplied inline link element. It replaces all relationship
    * specific attribute values with those from the supplied cloned
    * relationship.
    *
    * @param elem the inline link element, assume one of its attribute name is
    *    <code>RX_INLINESLOT</code> and assumed not <code>null</code>.
    *
    * @param relationshipMap the relationship mapper. It maps each existing
    *    relationship id (in <code>Integer</code> to a new relationship (in
    *    <code>PSRelationship</code>). Assume it is not <code>null</code>.
    * 
    * @param modified <code>PSRelationship</code> objects for which a link is 
    *    modified by this method are added to this collection if supplied.  May
    *    be <code>null</code>, may or may not be empty. 
    */
   private static void modifyRelationships(Element elem, Map<?, ?> relationshipMap, 
      Collection<PSRelationship> modified)
   {
      String relationshipid =
         elem.getAttribute(IPSHtmlParameters.SYS_RELATIONSHIPID);
      if (relationshipid == null || relationshipid.trim().length() == 0)
         return;

      PSRelationship relationship = (PSRelationship) relationshipMap.get(
         new Integer(relationshipid));

      if (relationship == null)
         return;
      else if (modified != null)
         modified.add(relationship);

      elem.setAttribute(IPSHtmlParameters.SYS_RELATIONSHIPID,
         Integer.toString(relationship.getId()));

      String dependentID = Integer.toString(relationship.getDependent().getId());
      elem.setAttribute(IPSHtmlParameters.SYS_DEPENDENTID, dependentID);
      
      String slotId = relationship.getProperty(IPSHtmlParameters.SYS_SLOTID);
      elem.setAttribute(RX_INLINESLOT, slotId);
      
      String variantId = relationship.getProperty(
         IPSHtmlParameters.SYS_VARIANTID);
      elem.setAttribute(IPSHtmlParameters.SYS_DEPENDENTVARIANTID, variantId);      
      
      String siteId = relationship.getProperty(IPSHtmlParameters.SYS_SITEID);
      if (siteId != null && siteId.trim().length() > 0)
      {
         elem.setAttribute(IPSHtmlParameters.SYS_SITEID, siteId);
      }
      else // need to remove SYS_SITEID attribute if there was one
      {
         siteId = elem.getAttribute(IPSHtmlParameters.SYS_SITEID);
         if (siteId != null && siteId.trim().length() > 0)
            elem.removeAttribute(IPSHtmlParameters.SYS_SITEID);
      }

      String folderId = relationship
            .getProperty(IPSHtmlParameters.SYS_FOLDERID);
      if (folderId != null && folderId.trim().length() > 0)
      {
         elem.setAttribute(IPSHtmlParameters.SYS_FOLDERID, folderId);
      }
      else // need to remove SYS_FOLDERID attribute if there was one
      {
         folderId = elem.getAttribute(IPSHtmlParameters.SYS_FOLDERID);
         if (folderId != null && folderId.trim().length() > 0)
            elem.removeAttribute(IPSHtmlParameters.SYS_FOLDERID);
      }
      
      // replaces the "sys_contentid" argument with the new dependent id for
      // the value of "href" or "src" attributes.

      NamedNodeMap attributes = elem.getAttributes();
      for (int i=0; i<attributes.getLength(); i++)
      {
         Attr attribute = (Attr) attributes.item(i);
         String value = attribute.getValue();
         int pathIndex = value.indexOf("?");
         if (pathIndex > 0)
         {
            String url = value.substring(0, pathIndex + 1);
            String newValue = null;
            value = value.substring(pathIndex + 1);
            StringTokenizer tokens = new StringTokenizer(value, "&");
            while (tokens.hasMoreTokens())
            {
               String token = tokens.nextToken();
               if (token.startsWith(IPSHtmlParameters.SYS_CONTENTID))
               {
                  token = token.substring(0,
                     token.indexOf(HTML_PARAMETER_VALUE_DELIMITER) + 
                        HTML_PARAMETER_VALUE_DELIMITER.length()) + dependentID;
               }
               if (newValue == null)
                  newValue = url + token;
               else
                  newValue = newValue + "&" + token;
            }

            elem.setAttribute(attribute.getName(), newValue);
         }
      }
   }

   /**
    * Processes the field in the supplied element for inline links. Inline
    * links are recognized through the attribute named
    * <code>RX_INLINESLOT</code>. All child nodes of the supplied element are
    * processed recursivly. Inline link elements cannot contain further inline
    * links.
    *
    * @param elem the element to process fro inline links, assumed not
    *    <code>null</code>.
    * @param request the request to operate on, assuemd not <code>null</code>.
    * @param processor the relationship processor to use, assumed not
    *    <code>null</code>.
    * @param cleanup <code>true</code> to cleanup broken links,
    *    <code>false</code> otherwise.
    * @param deletes The relationship set used to keep track of relationships 
    *    that need to be deleted in the post process. Assume not
    *    <code>null</code>.
    * @param modifies The relationship set used to keep track of relationships 
    *    that need to be modified in the post process. Assume not 
    *    <code>null</code>.
    */
   private void processField(
      Element elem,
      IPSRequestContext request,
      PSRelationshipProcessor processor,
      boolean cleanup,
      PSRelationshipSet deletes,
      PSRelationshipSet modifies)
      throws PSCmsException
   {
      if (elem != null)
      {
          PSSingleValueBuilder.removeClass(elem, PSSingleValueBuilder.PERC_BROKENLINK);
          PSSingleValueBuilder.removeClass(elem, PSSingleValueBuilder.PERC_NOTPUBLICLINK);
          
          
         PSSingleValueBuilder.cleanupEmptyAttributes(elem);
         
        
         boolean processed = false;
         String inlineSlot = elem.getAttribute(RX_INLINESLOT);
         String inlineType = elem.getAttribute(RX_INLINETYPE);
         if (inlineSlot != null && inlineSlot.trim().length() > 0 && 
            inlineType != null && inlineType.trim().length() > 0)
         {
            processInlineLink(elem, request, cleanup, deletes, modifies);
            processed = !inlineType.equals(RX_INLINETYPE_HYPERLINK);
         }
         else
         {
            NamedNodeMap attributes = elem.getAttributes();
            for (int i=0; i<attributes.getLength(); i++)
            {
               Attr attribute = (Attr) attributes.item(i);
               String value = attribute.getValue();
               if (value.indexOf(RX_INLINESLOT) >= 0)
               {
                  StringTokenizer tokens = new StringTokenizer(value, "?&");
                  while (tokens.hasMoreTokens())
                  {
                     String token = tokens.nextToken();
                     if (token.startsWith(IPSHtmlParameters.SYS_DEPENDENTID))
                     {
                        elem.setAttribute(IPSHtmlParameters.SYS_DEPENDENTID,
                           token.substring(token.indexOf(
                              HTML_PARAMETER_VALUE_DELIMITER) + 
                                 HTML_PARAMETER_VALUE_DELIMITER.length()));
                     }
                     else if (token.startsWith(
                        IPSHtmlParameters.SYS_DEPENDENTVARIANTID))
                     {
                        elem.setAttribute(
                           IPSHtmlParameters.SYS_DEPENDENTVARIANTID,
                              token.substring(token.indexOf(
                                 HTML_PARAMETER_VALUE_DELIMITER) + 
                                    HTML_PARAMETER_VALUE_DELIMITER.length()));
                     }
                     else if (token.startsWith(RX_SHORT_DEPENDENTVARIANTID))
                     {
                        //also expect a short version of SYS_DEPENDENTVARIANTID
                        elem.setAttribute(
                           IPSHtmlParameters.SYS_DEPENDENTVARIANTID,
                              token.substring(token.indexOf(
                                 HTML_PARAMETER_VALUE_DELIMITER) + 
                                    HTML_PARAMETER_VALUE_DELIMITER.length()));
                     }
                     else if (token.startsWith(RX_INLINESLOT))
                     {
                        elem.setAttribute(RX_INLINESLOT,
                           token.substring(token.indexOf(
                              HTML_PARAMETER_VALUE_DELIMITER) + 
                                 HTML_PARAMETER_VALUE_DELIMITER.length()));
                     }
                     else if (token.startsWith(RX_SELECTEDTEXT))
                     {
                        elem.setAttribute(RX_SELECTEDTEXT,
                           token.substring(token.indexOf(
                              HTML_PARAMETER_VALUE_DELIMITER) + 
                                 HTML_PARAMETER_VALUE_DELIMITER.length()));
                     }
                     else if (token.startsWith(RX_INLINETYPE))
                     {
                        elem.setAttribute(RX_INLINETYPE,
                           token.substring(token.indexOf(
                              HTML_PARAMETER_VALUE_DELIMITER) + 
                                 HTML_PARAMETER_VALUE_DELIMITER.length()));
                     }
                     else if (token.startsWith(RX_SHORT_INLINETYPE))
                     {
                        //also expect a short version of RX_INLINETYPE
                        elem.setAttribute(RX_INLINETYPE,
                           token.substring(token.indexOf(
                              HTML_PARAMETER_VALUE_DELIMITER) + 
                                 HTML_PARAMETER_VALUE_DELIMITER.length()));
                     }
                  }

                  processInlineLink(elem, request, cleanup, deletes, modifies);

                  processed = true;
                  break;
               }
            }
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
                     processField((Element) test, request, processor, cleanup, 
                        deletes, modifies);
               }
            }
         }
      }
   }

   /**
    * Processes the inline link for the supplied element. If the related item
    * specified in the inline link element still exists, this will insert or
    * update the 'Related Content' relationship. If the related item does not
    * exist anymore, it will replace the complete inline link element with the
    * text found in the 'selectedtext' attribute and remove the relationship if
    * so requestd through th eexit parameter 'clenupBrokenLinks'.
    *
    * @param elem the element to process, may be <code>null</code> in which
    *    case nothing is done.
    * @param request the request to operate on, assumed not <code>null</code>.
    * @param cleanup <code>true</code> to cleanup broken links,
    *    <code>false</code> otherwise.
    * @param deletes The relationship set used to keep track of relationships 
    *    that need to be deleted in the post process. Assume not
    *    <code>null</code>.
    * @param modifies The relationship set used to keep track of relationships 
    *    that need to be modified in the post process. Assume not 
    *    <code>null</code>.
    */
   private void processInlineLink(Element elem, IPSRequestContext request,
      boolean cleanup, PSRelationshipSet deletes, PSRelationshipSet modifies)
      throws PSCmsException
   {
	   
      /**
       * If we made it here, the following element attributes and request
       * parameters are valid.
       */
      String slotid = elem.getAttribute(RX_INLINESLOT);
      String contentid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
      String revision = request.getParameter(IPSHtmlParameters.SYS_REVISION);
      String siteid = elem.getAttribute(IPSHtmlParameters.SYS_SITEID);
      String folderid = elem.getAttribute(IPSHtmlParameters.SYS_FOLDERID);
      String reltype = getRelationshipType(slotid);
      
      String dependentid = elem.getAttribute(IPSHtmlParameters.SYS_DEPENDENTID);
      if (dependentid == null || dependentid.trim().length() == 0)
         throw new RuntimeException(
            "Missing required inline link parameter: " +
               IPSHtmlParameters.SYS_DEPENDENTID);

      String variant = elem.getAttribute(
         IPSHtmlParameters.SYS_DEPENDENTVARIANTID);
      if (variant == null || variant.trim().length() == 0)
         throw new RuntimeException(
            "Missing required inline link parameter: " +
               IPSHtmlParameters.SYS_DEPENDENTVARIANTID);

      String relationshipid = elem.getAttribute(
         IPSHtmlParameters.SYS_RELATIONSHIPID);

      PSLocator owner = new PSLocator(contentid, revision);
      PSLocator dependent = new PSLocator(dependentid);

      if (!removeInlineLink(elem, request, cleanup))
      {
         PSRelationship relationship = getRelationship(relationshipid, deletes);
            
         boolean addToModifyList = false;
         if (relationship == null)
         {
            int rid = PSRelationshipCommandHandler.getNextId();
               relationship = new PSRelationship(rid, owner, dependent,
                     PSRelationshipCommandHandler
                           .getRelationshipConfig(reltype));
            
            // the new relationship, not exist in the backend repository
            relationship.setPersisted(false);  

            // update the requests relationshipid if we created a new one
            request.setParameter(IPSHtmlParameters.SYS_RELATIONSHIPID,
               Integer.toString(relationship.getId()));
            addToModifyList = true;
         }
         else
         {
            PSLocator rDependent = relationship.getDependent();
            String rSlotid = relationship
                  .getProperty(IPSHtmlParameters.SYS_SLOTID);
            String rVariant = relationship
                  .getProperty(IPSHtmlParameters.SYS_VARIANTID);
            String rSiteid = relationship
                  .getProperty(IPSHtmlParameters.SYS_SITEID);
            if (rSiteid == null)
               rSiteid = "";
            String rFolderid = relationship
                  .getProperty(IPSHtmlParameters.SYS_FOLDERID);
            if (rFolderid == null)
               rFolderid = "";
            String inlineRelationshipId = relationship
                  .getProperty(PSRelationshipConfig.RS_INLINERELATIONSHIP);
            
            if (rDependent.getId() != dependent.getId())
               addToModifyList = true;
            else if (!rSlotid.equals(slotid))
               addToModifyList = true;
            else if (!rVariant.equals(variant))
               addToModifyList = true;
            else if (!siteid.equals(rSiteid))
               addToModifyList = true;
            else if (!folderid.equals(rFolderid))
               addToModifyList = true;
            else if (inlineRelationshipId.equals(RS_YES))
            {
               /*
                * Make sure that old style inline relationship id's are
                * updated to the new style.
                */
               addToModifyList = true;
            }
         }
            
         if (addToModifyList)
         {
            relationship.setProperty(IPSHtmlParameters.SYS_SLOTID, slotid);
            relationship.setProperty(IPSHtmlParameters.SYS_VARIANTID, variant);
            relationship.setProperty(PSRelationshipConfig.RS_INLINERELATIONSHIP, 
               getInlineRelationshipId(request, getField()));
   
            //If request contains siteid parameter add it as a property 
            String param = elem.getAttribute(IPSHtmlParameters.SYS_SITEID).trim();
            if(!param.equals(""))
               relationship.setProperty(IPSHtmlParameters.SYS_SITEID, param);

            //If request contains folderid parameter add it as a property 
            param = elem.getAttribute(IPSHtmlParameters.SYS_FOLDERID).trim();
            if(!param.equals(""))
               relationship.setProperty(IPSHtmlParameters.SYS_FOLDERID, param);
            
            modifies.add(relationship);
         }

         elem.setAttribute(IPSHtmlParameters.SYS_RELATIONSHIPID,
            Integer.toString(relationship.getId()));
      }
   }
   
   /**
    * Gets the relationship type name of the supplied slot id. 
    * @param slotid must not be a blank, if not a valid slot id, throws exception.
    * @return The type either from the local storage or loads the slot and gets it.
    * It is {@link PSRelationshipConfig#TYPE_ACTIVE_ASSEMBLY} if cannot find
    * the specified slot.
    */
   private String getRelationshipType(String slotid)
   {
      if(StringUtils.isBlank(slotid))
         throw new IllegalArgumentException("slotid must not be blank");
      String relType = m_slotRelationshipTypes.get(slotid);
      if(relType == null)
      {
         IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
         IPSAssemblyService asmSrvc = PSAssemblyServiceLocator
               .getAssemblyService();
         IPSGuid slotGuid = guidMgr.makeGuid(slotid, PSTypeEnum.SLOT);
         IPSTemplateSlot slot = asmSrvc.findSlot(slotGuid);
         if (slot == null)
         {
            log.warn("Failed to find slot id= {} ", slotid);
            return PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY;
         }
         
         relType = slot.getRelationshipName();
         m_slotRelationshipTypes.put(slotid, relType);
      }
      return relType;
   }

   /**
    * Searches the supplied set for a relationship whose {@link
    * PSRelationshipConfig#RS_INLINERELATIONSHIP} property is equal to the
    * supplied id or it is equal to <code>RS_YES</code> (this latter is for
    * backwards compatibility). 
    *
    * @param relationshipId the inline link relationship id that must 
    * match to find the existing relationship, may be <code>null</code> or 
    * empty, in which case <code>null</code> is returned. The value supplied
    * for this parameter must be one of the values returned by {@link 
    * #getInlineRelationshipId(PSRequest,PSField)}.
    * 
    * @param possibilities The relationship set which will be searched for a 
    * relationship that has an id that matches that supplied in 
    * <code>inlineRelationshipId</code>. Assume not <code>null</code>.
    *
    * @return If a match is found in the set, it is removed and returned,
    * otherwise <code>null</code> is returned.
    */
   private PSRelationship getRelationship(String relationshipId, 
      PSRelationshipSet possibilities)
   {
      PSRelationship result = null;
      if (relationshipId != null && relationshipId.trim().length() > 0)
      {
         for (int i=0; i<possibilities.size(); i++)
         {
            relationshipId = relationshipId.trim();
            PSRelationship relationship = (PSRelationship) possibilities.get(i);
            String test = relationship.getId() + "";
            if (test.equals(relationshipId))
            {
               result = relationship;
               possibilities.remove(result);
               break;
            }
         }
      }
      return result;
   }

   /**
    * This method tests if the dependent defined in the supplied inline link
    * element is still valid and removes the inline link element completly if
    * the dependent was deleted and if so requested.
    *
    * @param elem the inline slot element, assumed not <code>null</code> and
    *    all attributes are available and valid.
    * @param request the request to operate on, assumed not <code>null</code>.
    * @param cleanup <code>true</code> to cleanup broken links,
    *    <code>false</code> otherwise.
    * @return <code>true</code> if the dependent does not exist anymore and the
    *    inline link was removed, <code>false</code> otherwise.
    */
   private boolean removeInlineLink(Element elem, IPSRequestContext request,
      boolean cleanup) throws PSCmsException
   {
      if (!cleanup)
         return false;
      
      
      if (doesDependentExist(elem, request))
      {
         return false;
      }

      return true;
   }

  
  
   
   /**
    * Determines if the dependent item defined in the given inline-link
    * element exist or not.
    * 
    * @param elem the inline-link element, assumed not <code>null</code>.
    * @param request the request, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the dependent item exists; otherwise return
    * <code>false</code>.
    */
   private boolean doesDependentExist(Element elem, IPSRequestContext request)
   {
      String dependentId =
         elem.getAttribute(IPSHtmlParameters.SYS_DEPENDENTID);
      
      if (isBlank(dependentId))
         return false;
      
      try
      {
         int contentId = Integer.valueOf(dependentId);
         PSServerFolderProcessor processor = PSServerFolderProcessor.getInstance();
         return processor.doesItemExist(contentId);
      }
      catch (NumberFormatException e)
      {
         return false;
      }
   }
   
   
   
   /**
    * Gets the link text from the specified A tag element.
    * 
    * @param elem the element in question, assumed <code>null</code>.
    * 
    * @return the value of {@link #RX_SELECTEDTEXT} attribute if exist;
    * or return the link text if the element is an A tag; otherwise
    * return an empty string. 
    */
   private String getLinkTextFromATag(Element elem)
   {
      String selectedText = elem.getAttribute(RX_SELECTEDTEXT);
      if (!isEmpty(selectedText))
         return selectedText;
      
      // make sure this is an inline link element and is an <a> tag
      String inlineType = elem.getAttribute(RX_INLINETYPE);
      if (!RX_INLINETYPE_HYPERLINK.equals(inlineType))
         return "";
      
      if (!"a".equalsIgnoreCase(elem.getNodeName()))
         return "";
      
      return getElementData(elem);
   }
   
   /**
    * A utility function for replacing one string with another string in
    * a given source string.
    * @param srcString source String.
    * @param replace String to be replaced.
    * @param replacewith String to be replaced with.
    * @return String result after replacement. Returns empty string if
    *    source String is empty or <code>null</code>.
    */
   public static String replaceString(String srcString, String replace,
      String replacewith)
   {
      // In a string replace one substring with another
      if (srcString == null || srcString.trim().length() < 1) return "";
      String res = "";
      int i = srcString.indexOf(replace,0);
      int lastpos = 0;
      while (i != -1)
      {
         res += srcString.substring(lastpos,i) + replacewith;
         lastpos = i + replace.length();
         i = srcString.indexOf(replace,lastpos);
      }
      res += srcString.substring(lastpos);  // the rest
      return res;
   }

   /**
    * The field that may contain inline links. Initialized in constructor,
    * never <code>null</code> or changed after that.
    */
   private PSField m_field = null;

   /**
    * A map of slotid and relationship type of the slot. This is filled as 
    * needed in {@link #getRelationshipType(String)}. Do not use it directly to
    * get the relationship type for a given slot, rather use the method.
    */
   private Map<String, String> m_slotRelationshipTypes = 
      new HashMap<>();
   
   /**
    * The name of the attribute that identifies an element as Rhythmyx inline
    * slot.
    */
   public static final String RX_INLINESLOT = "rxinlineslot";

   /**
    * The name of the attribute that holds the original text that was selected
    * while the inline link was created. This is the text with which the inline
    * slot element will be replaced if the dependent item was removed and
    * cleanup is requested.
    */
   public static final String RX_SELECTEDTEXT = "rxselectedtext";

   /**
    * The name of the attribute that holdes the type of the inline link,
    * possible values are <code>hyperlink</code> and <code>image</code>.
    */
   public static final String RX_INLINETYPE = "inlinetype";

   /**
    * This is a short version of RX_INLINETYPE, which is needed
    * to trim the length of the image url while submitting it from Word.
    * This is in an attempt to work around an apparent 255 char Url length
    * limitation in Word 2000, for more info see Rx-03-06-0153.
    */
   private static final String RX_SHORT_INLINETYPE = "inlt";

   /**
    * The value of the inlinetype attribute indicating it as a inline link,
    */
   private static final String RX_INLINETYPE_HYPERLINK = "rxhyperlink";

   /**
    * The old value expected in the relationship property 
    * <code>RS_INLINERELATIONSHIP</code> to recognize it as inline link
    * relationship and for the cleanupBrokenLinks parameter to enable cleanup.
    */
   public static final String RS_YES = "yes";
   
   /**
    * The delimiter used to separate the inline link field name from the
    * child row id in inline relationship identifiers. Exactly 1 character long.
    * Currently a ':'.
    */
   private static final String INLINE_RELATIONSHIP_ID_DELIMITER = ":";

   /**
    * The token that separates the value from the HTML parameter name.
    */
   private static final String HTML_PARAMETER_VALUE_DELIMITER = "=";

   /**
    * This is a short version of SYS_DEPENDENTVARIANTID, which is needed
    * to trim the length of the image url while submitting it from Word.
    * This is in an attempt to work around an apparent 255 char Url length
    * limitation in Word 2000, for more info see Rx-03-06-0153.
    */
   private static final String RX_SHORT_DEPENDENTVARIANTID = "dvid";   

   /**
    * String constant for filling the empty anchor element
    */
   public static final String ELEM_FILLER = "##RX_FILLER##";
   
   /**
    * A list with all inline link slot id's as <code>String</code> found in the
    * first call to {@link isInlineSlot(String)}. The list is never 
    * <code>null</code> or changed after that, it may be empty.
    */
   private static Collection<String> ms_inlineslots = null;
   
   /**
    * The string used to identify a slot as type inline slot.
    */
   private static final String INLINE_SLOT_TYPE = "1";

}
