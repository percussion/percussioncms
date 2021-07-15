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
package com.percussion.inlinelinkconverter;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.cms.objectstore.ws.PSRemoteWsRequester;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.util.PSRemoteRequester;
import com.percussion.util.PSStringOperation;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;

/**
 * This is the class for the Rhythmyx Inline Link Conversion Tool. Runs the 
 * {@link #main} method and provides some static utility methods provide 
 * information about the tool.
 */

public class PSInlineLinkConverter
{

   private static final Logger log = LogManager.getLogger(PSInlineLinkConverter.class);

   /**
    * Constructor, called by main with the loaded properties file and
    * xsl document which will be applied on the inline content.
    * 
    * @param props the properties of the conversion. It must contain
    *    the properties that are needed for <code>PSRemoteRequester</code>, 
    *    which are used to communicate with Rhythmyx server. See 
    *    {@link com.percussion.util.PSRemoteRequester}.
    * @param xslDoc The XSL document that needs to be applied on the
    *    inline content to convert it. Must not be <code>null</code>.
    */
   public PSInlineLinkConverter(Properties props, Document xslDoc)
   {
      if (props == null)
      {
         throw new IllegalArgumentException("props must not be null");
      }
      if (xslDoc == null)
      {
         throw new IllegalArgumentException("xslDoc must not be null");
      }
         
      try 
      {
         m_props = props;
         m_XslDoc = xslDoc;
         
         m_userName = m_props.getProperty("loginId");
         PSRemoteRequester requester = new PSRemoteRequester(props);
         m_rtAgent = new PSRemoteAgent(requester);
         
         // get the community
         String communityId = m_props.getProperty("communityId");
         if (communityId != null && communityId.trim().length() != 0)
            m_rtAgent.setCommunity(communityId);
         m_community = m_rtAgent.login();
         
         initWorkflowIdMap(m_community);
         m_allContentTypes = m_rtAgent.getContentTypes(m_community);
         
         // get rx root & sessionid          
         Element responseEl = m_rtAgent.loginEx();
         Element loginDataEl =
            PSXMLDomUtil.getFirstElementChild(responseEl, "LoginData");
         m_RxLocation = getRxLocation();
   
         Element sessionIdEl = 
            PSXMLDomUtil.getFirstElementChild(loginDataEl, "SessionId");
         m_RxSession = PSXMLDomUtil.getElementData(sessionIdEl);
         
      }
      catch (Exception ex) 
      {
         log.error(ex.getMessage());
         log.debug(ex.getMessage(), ex);
         String errorMsg = "Error - failed to construct PSInlineLinkConverter"
            + ", caught exception: " + ex.getMessage();
         writeToLog(errorMsg);
         throw new RuntimeException(errorMsg);
      }
   }

   /**
    * Get the Rhythmyx URL from the properties, assume the variable, 
    * <code>m_props</code>, is not <code>null</code>.
    * 
    * @return the Rxythmyx root, never <code>null</code>.
    */
   private String getRxLocation()
   {  
      String hostName = m_props.getProperty("hostName");
      String port = m_props.getProperty("port");
      String rxroot = m_props.getProperty("serverRoot");
   
      return "http://" + hostName + ":" + port + "/" + rxroot;
   }
      
   /**
    * Creates a document from the given string.
    * 
    * @param content The string of the document, assume not <code>null</code>.
    * 
    * @return The created document, never <code>null</code>.
    * 
    * @throws IOException if I/O error occurs.
    * @throws SAXException if XML parser error occurs.
    */
   private Document createXmlDocument(String content) 
      throws IOException, SAXException
   {
      String docString;

      // prepare the XML string, inserting DOCTYPE for the ENTITY files
      String entityString = 
         NEWLINE
         + "<!DOCTYPE html ["
         + PSXmlDocumentBuilder.getDefaultEntities(m_RxLocation)
         + "]>"
         + NEWLINE
         + NEWLINE;
      
      if (content.startsWith("<?"))
      {
         int headerEnd = content.indexOf("?>");
         if (headerEnd > 0)
         {
            docString = content.substring(0, headerEnd+2)
               + entityString
               + content.substring(headerEnd+2);
         }
         else
         {
            throw new RuntimeException("Error - Invalid XML document, "
               + "start with '<?', but there is no '?>'");
         }
      }
      else
      {
         docString = "<?xml version='1.0' encoding=\"UTF-8\"?>"
            + entityString
            + content;
      }

      Document doc =
         PSXmlDocumentBuilder.createXmlDocument(
            new StringReader(docString),
            false);

      return doc;        
   }
   
   /**
    * Populates the workflow id-name mapper for all workflows in specified
    * community.
    * 
    * @param community The community of the populated workflow, assume not
    *    <code>null</code>.
    * 
    * @throws PSRemoteException if an error occurs.
    */
   private void initWorkflowIdMap(PSEntry community) throws PSRemoteException
   {
      Iterator workflows = m_rtAgent.getWorkflows(community).iterator();
      while (workflows.hasNext())
      {
         PSEntry wf = (PSEntry) workflows.next();
         String name = wf.getLabel().getText();
         m_wfIdNameMap.put(wf.getValue(), normalizeWorkflowName(name));
      }
   }
   
   /**
    * Replaces space character, ' ', with underscore character, '_'. This is
    * because the workflow name is a key in the properties file, which defines
    * transition lists for the workflow. However, the space character cannot be
    * used in the key of the properties file. 
    * 
    * @param wfName the to be normalized workflow name, assume not 
    *    <code>null</code> or empty.
    * 
    * @return The normalized workflow name. Never <code>null</code> or empty.
    */
   private String normalizeWorkflowName(String wfName)
   {
      return wfName.replace(' ', '_');
   }
   
   /**
    * Do the conversion here.
    */
   public void doConvert()
   {
      try
      {
         // redirect the output to a logfile
         m_logger = new BufferedWriter(new FileWriter(LOG_ALL));
         m_loggerFail = new BufferedWriter(new FileWriter(LOG_FAIL));
         m_loggerSuccess = new BufferedWriter(new FileWriter(LOG_SUCCESS));
         
         writeToLog("Conversion Started -- " + new Date());
         writeToLog("");
         System.out.println("Conversion Started");
         
         String contentType = m_props.getProperty("contentType");
         if (contentType != null && contentType.trim().length() > 0)
         {
            convertType(contentType);
         }
         else
         {
            Iterator iter = m_allContentTypes.iterator();
            while (iter.hasNext())
            {
               PSEntry ct = (PSEntry) iter.next();
               convertType(ct.getLabel().getText());
            }
         }
      }
      catch (Throwable t)
      {
         String msg = "An error has occurred: " + t.getMessage();
         System.out.println(msg);

         writeToLog(msg);

         log.error(t.getMessage());
         log.debug(t.getMessage(), t);
      }
      finally
      {
         writeToLog("");
         writeToLog("Conversion Finished -- " + new Date());
         System.out.println("");
         System.out.println("Conversion Finished");

         closeLogger(m_logger);
         closeLogger(m_loggerSuccess);
         closeLogger(m_loggerFail);
      }
   }

   /**
    * Convert the specified content type.
    * 
    * @param contentType the type of content to be converted
    * @throws PSCmsException 
    */
   private void convertType(String contentType) throws PSCmsException
   {
      try
      {
         PSItemDefinition itemDef = m_rtAgent.getTypeDef(contentType);
         if (itemDef == null)
            return;
         
         // now that we have the def, check for the inline link properties 
         // on each of the fields, if found, then this is a contentType
         // that we want to convert
         PSField [] fields =    
            ((PSContentEditorPipe)itemDef.getContentEditor().getPipe()).
               getMapper().getFieldSet().getAllFields();

         boolean mayHaveInlineLinks = false;

         for (int i = 0; i < fields.length; i++)
         {
            PSField field = (PSField)fields[i];
            if (field.mayHaveInlineLinks())
            {
               mayHaveInlineLinks = true;
               List fieldList = (List)m_inlineLinkFieldMap.get(contentType);
               if (fieldList == null)
               {
                  fieldList = new ArrayList();
                  m_inlineLinkFieldMap.put(contentType, fieldList);
               }
               fieldList.add(field.getSubmitName());
            }
         }
         if (mayHaveInlineLinks)
         {
            writeToLog("");
            writeToLog(contentType  + " - Converting");
            convertAllFields(itemDef, contentType);
         }
         else
         {
            writeToLog("");
            writeToLog(contentType  + " - Not converting, no inline link support");
         }
      }
      catch (PSRemoteException e)
      {
         writeToLog("");
         writeToLog(contentType  + " - Not converting");
         writeToLog("Failed to get the content type definition. The registration may be wrong or the content type may not be running.");
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }

   /**
    * Convert all the fields of the specified content type.
    * 
    * @param itemDef the content type definition, used to get the proper fields,
    *    never <code>null</code>.
    * @param contentType the content type to convert, never <code>null</code> or
    *    empty. It must be an valid (or existing) content type.
    * 
    * @throws PSCmsException if an error occurs.
    */
   protected void convertAllFields(PSItemDefinition itemDef, String contentType) 
      throws PSCmsException
   {
      if (itemDef == null)
         throw new IllegalArgumentException("itemDef must not be null");
      if (contentType == null || contentType.trim().length() == 0)
         throw new IllegalArgumentException("contentType must not be null or empty.");
      
      List convertFieldList = (List)m_inlineLinkFieldMap.get(contentType);
      if (convertFieldList == null)
         throw new IllegalArgumentException("Unknown contentType : \""
               + contentType + "\".");
      

      List contentIdList = getContentIds(contentType);
      int totalIds = contentIdList.size();
      if (totalIds > 0)
         System.out.println("Processing \"" + contentType
               + "\" with posible total number of " + totalIds + " ids.");
      Iterator iter = contentIdList.iterator();
      while (iter.hasNext())
      {
         boolean needToConvert = false;
         ContentKey ck = (ContentKey)iter.next();
         
         int index = contentIdList.indexOf(ck);
         int lp = 0;
         int cp = Math.round((index*100)/(totalIds*10));
         if(cp>lp)
         {
            System.out.println("Processing " + cp*10 + "% complete");
            lp=cp;
         }

         try 
         {
            PSClientItem item = getItem(ck, false, itemDef);
            // first see if conversion is necessary, stop as soon
            // as any field needs conversion
            Iterator iterFieldList = convertFieldList.iterator();
            while (iterFieldList.hasNext())
            {
               String fieldName = (String)iterFieldList.next();
               if (convertFieldWithName(ck, item, fieldName))
               {
                  // conversion was necessary
                  needToConvert = true;
                  break;
               }
            }
            
            // if we need to convert, create a new item just in case it has
            // changed since we last got it
            if (!needToConvert)
            {
               writeToLog(" " + ck.m_contentId 
                  + " - not converted, no inline links found");
            }
            else
            {
               if (! preConvert(ck))
                  continue;
                                 
               item = getItem(ck, ck.m_chkoutBeforeProcessing, itemDef);
               iterFieldList = convertFieldList.iterator();
               while (iterFieldList.hasNext())
               {
                  String fieldName = (String)iterFieldList.next();
                  if (! convertFieldWithName(ck, item, fieldName))
                     continue;
               }
               saveUpdatedItem(item);
               
               if (! postConvert(ck))
                  continue;
               
               logSuccessItem(ck);
               writeToLog(" " + ck.m_contentId + " - converted");
            }
         }
         catch (Exception ex) 
         {
            writeToLog(" " + ck.m_contentId + " - Error - " + ex.getMessage());
         }
      }
   }

   /**
    * Pre-process before the actual conversion. Making sure the specified item 
    * can be checked out by the current login id. If the item is in public 
    * state, then transition it to a editable state.
    * 
    * @param ck The specified item, assume not <code>null</code>.
    * 
    * @return <code>true</code> if it is ok to proceed the conversion process;
    *    otherwise, the item cannot be converted.  
    */
   private boolean preConvert(ContentKey  ck)
   {
      boolean success = true;
      
      String itemUser = ck.m_checkoutUsername;
      
      if (ck.isCheckedOut() && m_userName.equalsIgnoreCase(itemUser))
      {
         // checked out by the same user already, no need to be check out later
         ck.m_chkoutBeforeProcessing = false;
      }
      else if (ck.isCheckedOut() && (!itemUser.equals(m_userName)))
      {
         if (! checkInItem(ck)) // force check in
         {
            success = false;
            writeToLog(" failed to force checkin: " + ck.m_contentId 
               + " - not converted, checked out by '" 
               + ck.m_checkoutUsername 
               + "'");
                        
            logFailureItem(ck);
         }
         else // it is checked in now, needs to be checked out before processing
         {
            ck.m_chkoutBeforeProcessing = true;
         }
      }
      else if (ck.isPublic())
      {
         if (! transPublicToEditState(ck))
         {
            success = false;
            writeToLog(" failed transition from public, id: " 
               + ck.m_contentId 
               + " - not converted");
                        
            logFailureItem(ck);
         }
         else // it is in QuickEdit state, but not checked out yet
         {
            ck.m_chkoutBeforeProcessing = true;
         }
      }
      
      return success;
   }
   
   /**
    * Post process after the actual conversion. Transfers the specified item
    * to public state if it was in public state before the conversion.
    * 
    * @param ck The item, assume not <code>null</code>.
    * 
    * @return <code>true</code> if not error occurs; otherwise to transfer to
    *    public state.
    */
   private boolean postConvert(ContentKey  ck)
   {
      boolean success = true;

      if (ck.isPublic())
      {
         if (! transEditToPublicState(ck))
         {
            success = false;
            
            writeToLog(" failed transition to public, id: " 
               + ck.m_contentId 
               + " - not converted");
                           
            logFailureItem(" converted, but fail to transfer to public state, "
               + "id: " + ck.m_contentId + ", " + ck.m_revision);
         }
      }
      
      return success;
   }
   
   /**
    * Transfers the specified item from public to editable state.
    *  
    * @param ck The specified item, assume not <code>null</code>. 
    * 
    * @return <code>true</code> for a successful transition; <code>false</code>
    *    otherwise. 
    */
   private boolean transPublicToEditState(ContentKey ck)
   {
      WorkflowTransitions trans = getTransitions(ck);
         
      if (trans != null)
         return transferItemState(ck, trans.getPubToEditTrans());
      else
         return false; 
   }

   /**
    * Transfers the specified item from current editable state to public state.
    *  
    * @param ck The specified item, assume not <code>null</code>. 
    * 
    * @return <code>true</code> for a successful transition; <code>false</code>
    *    otherwise. 
    */
   private boolean transEditToPublicState(ContentKey ck)
   {
      WorkflowTransitions trans = getTransitions(ck);
         
      if (trans != null)
         return transferItemState(ck, trans.getEditToPubTrans());
      else
         return false; 
   }

   /**
    * Transfers the specified item according to the given list of transitions.
    * 
    * @param ck The specified item, assume not <code>null</code>.
    * 
    * @param transList The transition list, one or more transition id or 
    *    internal (trigger) name, assume not <code>null</code> or empty. 
    * 
    * @return <code>true</code> for a successful transition; <code>false</code>
    *    otherwise.
    */
   private boolean transferItemState(ContentKey ck, List transList)
   {
      boolean success = false;
      PSLocator loc = new PSLocator(ck.m_contentId, ck.m_revision);
      Iterator transIt = transList.iterator();
      try
      {
         while (transIt.hasNext())
         {
            String trans = (String) transIt.next();
            m_rtAgent.transitionItem(loc, trans);     
         }
         success = true;
      }
      catch (PSRemoteException e)
      {
         writeToLog(
            " Error - workflow transition failed on item id: "
               + ck.m_contentId
               + " - Caught exception: "
               + e.getMessage());
      }
      
      return success;
   }
   
   /**
    * Gets the transitions for the specified item.
    *  
    * @param ck The item, assume not <code>null</code>.
    * 
    * @return The transitions for the specified item, may be <code>null</code>
    *    if the transitions are not specified in the properties file.
    */
   private WorkflowTransitions getTransitions(ContentKey ck)
   {
      WorkflowTransitions wfTrans =
         (WorkflowTransitions) m_wfTransMap.get(ck.getWorkflowId());
      if (wfTrans != null)
         return wfTrans;
         
      // cannot find the transition list, it may not been cached yet.
      String wfName = (String) m_wfIdNameMap.get(ck.getWorkflowId());
         
      String trans = m_props.getProperty(wfName);
      List transList1 = new ArrayList();         
      List transList2 = new ArrayList();
      if (trans != null)
      {
         List transLists = PSStringOperation.getSplittedList(trans, ';');
         if (transLists.size() >= 2)
         {
            String trans1, trans2;
            trans1 = (String) transLists.get(0);
            trans2 = (String) transLists.get(1);
            transList1 = PSStringOperation.getSplittedList(trans1, ',');
            transList2 = PSStringOperation.getSplittedList(trans2, ',');
         }
      }
      
      if ((! transList1.isEmpty()) && (! transList2.isEmpty()))
      {
         wfTrans = new WorkflowTransitions(transList1, transList2);
         m_wfTransMap.put(ck.getWorkflowId(), wfTrans);
      }
      else
      {
         String errorMsg = "empty transition lists is not allowed for "
            + "workflow: " + wfName
            + "Must specified transitions for both 'public to editable state'"
            + " and 'editable to public state', they cannot be empty";
         writeToLog(errorMsg);
         throw new RuntimeException(errorMsg);         
      }
      
      return wfTrans;
   }
   
   /**
    * Get all the content id's for the specified content type.
    * 
    * @param contentType the content type to look up all the items
    * @return a list of content ids as Integers
    */
   protected List getContentIds(String contentType) throws PSCmsException
   {
      List retList = new ArrayList();

      String contentTypeId = getContentTypeId(contentType);
      
      Element msg = getSearchRequest(contentTypeId);
      
      PSRemoteWsRequester wsRequester =
         new PSRemoteWsRequester(m_rtAgent.getRemoteRequester());

      // do search      
      Element data = wsRequester.sendRequest(
         SEARCH_OPERATION,
         SEARCH_PORT,
         msg,
         null,
         SEARCH_RESPONSE);

      if (data != null)
      {
         Element resultEl = PSXMLDomUtil.getFirstElementChild(data);
         while (resultEl != null)
         {
            String contentId = null;
            String revision = null;
            String workflowId = null;
            String checkoutUser = "";
            boolean isPublic = false;
            
            Element el = PSXMLDomUtil.getFirstElementChild(resultEl);
            while (el != null)
            {
               String name = el.getAttribute(NAME_ATTR);
               if (name != null)
               {
                  String val = PSXMLDomUtil.getElementData(el);
                  if (name.equalsIgnoreCase(SYS_CONTENTID))            
                  {
                     contentId = val;
                  }
                  else if (name.equalsIgnoreCase(SYS_TIPREVISION))            
                  {
                     revision = val;
                  }
                  else if (name.equalsIgnoreCase(SYS_CHKOUT_USER))
                  {
                     checkoutUser = val;
                  }
                  else if (name.equalsIgnoreCase(SYS_WORKFLOWID))
                  {
                     workflowId = val;
                  }
                  else if (name.equalsIgnoreCase(SYS_PUB_TYPE))
                  {
                     isPublic = val.equalsIgnoreCase(PUBLIC_TYPE);
                  }
               }
               el = PSXMLDomUtil.getNextElementSibling(el);
            }
            // if we have a complete content key add it to the list
            if (contentId != null && contentId.trim().length() > 0 &&
                revision != null && revision.trim().length() > 0 &&
                workflowId != null && workflowId.trim().length() > 0)
            {
               ContentKey ck = new ContentKey(contentId, revision, checkoutUser,
                  workflowId, isPublic);
               retList.add(ck);
            }
            else
            {
               writeToLog(
                  " Missing info, skip item: id="
                     + (contentId==null ? "null" : contentId)
                     + ", rev="
                     + (revision==null ? "null" : revision));
            }
               
            resultEl = PSXMLDomUtil.getNextElementSibling(resultEl);
         }
      }
      
      return retList;      
   }

   /**
    * Get the content type id from the specified name.
    * 
    * @param ctName The content type name, assume not <code>null</code>.
    * 
    * @return The id of the content type name, never <code>null</code> or empty.
    */
   private String getContentTypeId(String ctName)
   {   
      String contentTypeId = null;
      
      Iterator cts = m_allContentTypes.iterator();
      while (cts.hasNext())
      {
         PSEntry ct = (PSEntry) cts.next();
         if (ct.getLabel().getText().equalsIgnoreCase(ctName))
         {
            contentTypeId = ct.getValue();
         }
      }
      if (contentTypeId == null)
      {
         throw new RuntimeException("Failed to find content type: " + ctName);
      }
      
      return contentTypeId;
   }
   
   /**
    * Get the search request for the specified content type id. This is used
    * to request all items of the content type, or only the items that are
    * specified in the properties file if there is any.
    *  
    * @param contentTypeId The content type id, assume not <code>null</code>
    *    or empty.
    * 
    * @return The request element, never <code>null</code>.
    */
   private Element getSearchRequest(String contentTypeId)
   {
      // create search input doc, in the format of:
      // SearchRequest useDatabaseCase="false">
      //    <SearchParams>
      //       <ContentType>#</ContentType>
      //
      //       <Parameter>
      //          <SearchField name="sys_contentid" operator="in">302,303
      //          </SearchField>
      //       </Parameter>
      //      
      //       <SearchResults>
      //          <ResultField name="sys_publishabletype"/>
      //       </SearchResults>
      //    </SearchParams>
      // </SearchRequest>      

      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element msg = doc.createElement(SEARCH_REQUEST);
      Element schParamsEl =
         PSXmlDocumentBuilder.addEmptyElement(doc, msg, SEARCH_PARAMS);
      Element contentTypeEl = PSXmlDocumentBuilder.addElement(
            doc,
            schParamsEl,
            "ContentType",
            contentTypeId);

      // If "contentId" exist, then create the <Parameter> element:
      //      
      // <Parameter>
      //    <SearchField name="sys_communityid" operator="in">10</SearchField>
      // </Parameter>
      Element params = doc.createElement(SEARCH_PARAMETER);
      schParamsEl.appendChild(params);
      Element communityEl = PSXmlDocumentBuilder.addElement(
         doc,
         params,
         SEARCH_FIELD,
         m_community.getValue());
      communityEl.setAttribute(NAME_ATTR, "sys_communityid");
      communityEl.setAttribute("operator", "in");

      //    <SearchField name="sys_contentid" operator="in">302</SearchField>
      String contentId = m_props.getProperty("contentId");
      if (contentId != null && contentId.trim().length() != 0)
      {
         Element srchField =
            PSXmlDocumentBuilder.addElement(
               doc,
               params,
               SEARCH_FIELD,
               contentId);
         srchField.setAttribute(NAME_ATTR, SYS_CONTENTID);
         srchField.setAttribute("operator", "in");
      }
            
      // create <SerachResults> element
      Element resultsEl = doc.createElement(SEARCH_RESULTS);
      Element fieldEl = doc.createElement(RESULT_FIELD);
      fieldEl.setAttribute(NAME_ATTR, SYS_PUB_TYPE);
      resultsEl.appendChild(fieldEl);
 
      schParamsEl.appendChild(resultsEl);
      
      return msg;
   }
   
   /**
    * Get the specific content item from the rx server.
    * 
    * @param ck the specific content id and revision
    * @param checkOut if true, check out the item, otherwise just get the item
    * @return the xml of the item in standard item format
    */
   private PSClientItem getItem(
      ContentKey ck,
      boolean checkOut,
      PSItemDefinition itemDef)
      throws PSRemoteException
   {
      PSLocator loc = new PSLocator(ck.m_contentId, ck.m_revision);
      PSClientItem item =
         m_rtAgent.openItem(loc, true, false, false, checkOut, itemDef);
      
      // update the revision, which may change after check out.
      if (checkOut)
         ck.setRevision("" + item.getRevision());
         
      return item;
   }

   /**
    * Check in the specified item.
    *
    * @param ck The content key of the to be checked in item, assume not
    *    <code>null</code>.
    *
    * @return <code>true</code> if successfully checked in the item or the
    *    item has already checked in by the user of the conversion.
    */
   private boolean checkInItem(ContentKey ck)
   {
      PSLocator loc = new PSLocator(ck.m_contentId, ck.m_revision);
      try
      {
         return m_rtAgent.checkInItem(loc);
      }
      catch (PSRemoteException ex)
      {
         writeToLog(
            "Error - fail to check in item, "
               + "id: "
               + ck.m_contentId
               + ", "
               + ck.m_revision 
               + ". Caught exception: "
               + ex.getMessage());
      }
      return false;
   }

   /**
    * Converts a child field.
    * 
    * @param ck the content key of the converted item, never <code>null</code>.
    * @param item the converted item, never <code>null</code>.
    * @param fieldName the name of the child field, never <code>null</code>
    *    or empty.
    * 
    * @return <code>true</code> if the value of the field is converted; 
    *    otherwise return <code>false</code>.
    */
   protected boolean convertChildField(
         ContentKey ck,
         PSClientItem item,
         String fieldName)
   {
      if (ck == null)
         throw new IllegalArgumentException("ck must not be null");
      if (item == null)
         throw new IllegalArgumentException("item must not be null");
      if (fieldName == null || fieldName.trim().length() == 0)
         throw new IllegalArgumentException("fieldName must not be null or empty");
      
      boolean success = false;
      
      Iterator children = item.getAllChildren();
      PSItemChild childItem;
      PSItemChildEntry entry;
      PSItemField field;
      while (children.hasNext())
      {
         childItem = (PSItemChild) children.next();
         Iterator entries = childItem.getAllEntries();
         while (entries.hasNext())
         {
            entry = (PSItemChildEntry) entries.next();
            field = entry.getFieldByName(fieldName);
            if (convertField(ck, field))
            {
               success = true;
               entry.setAction(PSItemChildEntry.CHILD_ACTION_UPDATE);
            }
         }
      }
      
      return success;
   }

   /**
    * Convert the supplied field.
    * 
    * @param ck the content key of the converted item, never <code>null</code>.
    * @param field the to be converted field, never <code>null</code>.
    * 
    * @return <code>true</code> if the value of the field is converted; 
    *    otherwise return <code>false</code>.
    */
   protected boolean convertField(
      ContentKey ck,
      PSItemField field)
   {
      if (ck == null)
         throw new IllegalArgumentException("ck must not be null");
      if (field == null)
         throw new IllegalArgumentException("field must not be null");
      
      boolean success = false;
      String data = null;
      try 
      {
         IPSFieldValue value = field.getValue();
         if (value == null)
            return false; // no data 
         data = value.getValueAsString();
         if (data == null || data.trim().length() == 0)
            return false; // no data needs to be converted
         
   
         Document root = PSXmlDocumentBuilder.createXmlDocument();
         // add the rx location and session to the root element
         Element el = root.createElement("root");
         el.setAttribute("rxroot", m_RxLocation);
         el.setAttribute("pssessionid", m_RxSession);

         //Document doc = PSXmlDocumentBuilder.createXmlDocument(
         //   new StringReader(data), false);
         Document doc = createXmlDocument(data);
         String oldData =
            PSXmlDocumentBuilder.toString(doc.getDocumentElement());

         Node node = root.importNode(doc.getDocumentElement(), true);
         el.appendChild(node);

         root.appendChild(el);
            
         // convert here using stylesheet
         Document newDoc = transformXML(root, m_XslDoc);
         if (newDoc == null)
            return false;
   
         el = (Element)newDoc.getDocumentElement().getFirstChild();
         String newData = PSXmlDocumentBuilder.toString(el);
         
         if (newData.length() != oldData.length() || !newData.equals(oldData))
         {
            field.clearValues();
            field.addValue(new PSTextValue(newData));
            success = true;
         }
      }
      catch (Exception ex) 
      {
         logFailureItem(ck);
         
         String errMsg =
            "Error - on item (id="
               + ck.m_contentId
               + ", rev="
               + ck.m_revision
               + "), fieldname = \""
               + field.getName()
               + "\", stylesheet conversion failed, exception: ";
         if (ex == null)
            errMsg = errMsg + "null.";
         else
            errMsg = errMsg + ex.getMessage() + ".";
         
         writeToLog(errMsg);

         errMsg = "Error - on item (id="
               + ck.m_contentId
               + ", rev="
               + ck.m_revision
               + "), fieldname = \""
               + field.getName()
               + "\", data: " + data;
         writeToLog(errMsg);
      }
      
      return success;
   }
   
   
   /**
    * Converts the specified field with its name.
    * 
    * @param ck the to be converted content key, never <code>null</code>.
    * @param item the item to convert the field, never <code>null</code>.
    * @param fieldName the actual field name to convert, never <code>null</code>
    *    or empty.
    * 
    * @return <code>true</code> if the value of the field is converted; 
    *    otherwise return <code>false</code>.
    */
   protected boolean convertFieldWithName(
      ContentKey ck,
      PSClientItem item,
      String fieldName)
   {
      if (ck == null)
         throw new IllegalArgumentException("ck must not be null");
      if (item == null)
         throw new IllegalArgumentException("item must not be null");
      if (fieldName == null || fieldName.trim().length() == 0)
         throw new IllegalArgumentException("fieldName must not be null or empty");
      
      PSItemField field = item.getFieldByName(fieldName);
      if (field == null)
         return convertChildField(ck, item, fieldName);
      else
         return convertField(ck, field);
   }
   
   /**
    * Save the updated client item back to the server.
    * 
    * @param item the specific item to save
    */
   private void saveUpdatedItem(PSClientItem item) throws PSRemoteException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element itemEl = item.toMinXml(doc, true, true, false, false);
      m_rtAgent.updateItem(itemEl, true);
   }
   
   /**
    * Transform the document using the supplied XSL.
    * 
    * @param srcDoc the document to transform
    * @param xslDoc the stylesheet to use for the transformation
    * @return the transformed document
    */
   private Document transformXML(Document srcDoc, Document xslDoc)
   {
      Document outNode = null;

      try
      {
         outNode = PSXmlDocumentBuilder.createXmlDocument();
         DOMSource dsource = new DOMSource(xslDoc);
   
         TransformerFactory tfactory = TransformerFactory.newInstance();
         Templates templates = tfactory.newTemplates(dsource);
   
         templates.newTransformer().transform(
            new DOMSource(srcDoc), new DOMResult(outNode));
      }
      catch (TransformerConfigurationException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      catch (TransformerException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      return outNode;
   }

   /**
    * Write a message to the log file.
    * 
    * @param msg the message to write
    */
   private void writeToLog(String msg)
   {
      try 
      {
         m_logger.write(msg);
         m_logger.newLine();
         m_logger.flush();
      }
      catch (Exception ex) { /* ignore */ }
   }

   /**
    * Log the successful converted item id and revision.
    * 
    * @param ck The to be logged item, assume not <code>null</code>.
    * 
    * @throws IOException if error occurs.
    */
   private void logSuccessItem(ContentKey ck) throws IOException
   {
      try 
      {
         m_loggerSuccess.write(" " + ck.m_contentId + ", " + ck.m_revision);
         m_loggerSuccess.newLine();
         m_loggerSuccess.flush();
      }
      catch (Exception ex) { /* ignore */ }
   }

   /**
    * Format the specified item, then call {@link logFailureItem(String)}
    * 
    * @param ck The to be logged item, assume not <code>null</code>.
    */    
   private void logFailureItem(ContentKey ck)
   {
      logFailureItem(" " + ck.m_contentId + ", " + ck.m_revision);
   }
   
   /**
    * Log the un-successful converted item id and revision.
    * 
    * @param logInfo The to be logged item info, assume not <code>null</code>.
    */
   private void logFailureItem(String logInfo)
   {
      try 
      {
         m_loggerFail.write(logInfo);
         m_loggerFail.newLine();
         m_loggerFail.flush();
      }
      catch (Exception ex) { /* ignore */ }
   }

   /**
    * Closes the specified logger.
    * 
    * @param logger The to be closed logger, assume not <code>null</code>.
    */
   private void closeLogger(BufferedWriter logger)
   {
      if (logger != null)
      {
         try 
         {
            logger.flush();
            logger.close();
         }
         catch (Exception ex) { /* ignore */ }
      }
   }
   
   
   /**
    * Inner class to maintain information for a content item, such as id, 
    * revision, ...etc.
    */
   protected class ContentKey
   {
      /**
       * Constructs an object from id and revision.
       * 
       * @param contentId The content id, assume not <code>null</code> or empty.
       * 
       * @param revision The revision, assume not <code>null</code> or empty.
       * 
       * @param checkoutUser The check out user name, assume not 
       *    <code>null</code>, but may be empty.
       * 
       * @param workflowId the workflow id, assume not <code>null</code> or
       *    empty.
       * 
       * @param isPublic <code>true</code> if the item is in public state.
       */      
      private ContentKey(String contentId, String revision, 
         String checkoutUser, String workflowId, boolean isPublic)
      {
         m_contentId = contentId;
         m_revision = revision;
         m_checkoutUsername = checkoutUser;
         m_workflowId = workflowId;
         m_isPublic = isPublic; 
      }

      /**
       * Set revision
       * 
       * @param rev The new revision, assume not <code>null</code> or empty.  
       */      
      private void setRevision(String rev)
      {
         m_revision = rev;
      }
      
      /**
       * Determines whether the item is checked out.
       * 
       * @return <code>true</code> if the item has been checked out; 
       *    <code>false</code> otherwise.
       */
      private boolean isCheckedOut()
      {
         return (
            m_checkoutUsername != null
               && m_checkoutUsername.trim().length() > 0);
      }
      
      /**
       * Determines whether the item is in public state.
       * 
       * @return <code>true</code> if the item is in public state.
       */
      private boolean isPublic()
      {
         return m_isPublic;
      }
      
      /**
       * Get the workflow id
       * 
       * @return the workflow id, never <code>null</code> or empty.
       */
      private String getWorkflowId()
      {
         return m_workflowId;
      }

      protected String getContentId()
      {
         return m_contentId;
      }
      
      protected String getRevision()
      {
         return m_revision;
      }
      /**
       * The content id of the item. Init by ctor, never <code>null</code> or
       * empty after that.
       */
      private String m_contentId;
      
      /**
       * The tip revision of the item. Init by ctor, never <code>null</code> or
       * empty after that. 
       */
      private String m_revision;
      
      /**
       * The user name who checked out the item. It may be empty, but never
       * <code>null</code>.
       */
      private String m_checkoutUsername = "";
      
      /**
       * Indicating whether the item is in public state. <code>true</code> if
       * is in public state.
       */
      private boolean m_isPublic = false;
      
      /**
       * The workflow id of the item. Init by ctor, never <code>null</code> or
       * empty after that.
       */
      private String m_workflowId = "";
      
      /**
       * Indicating if the item needs to be checked out before processing its
       * content. <code>true</code> if it needs to be checked out before
       * conversion; otherwise, <code>false</code>. 
       */
      private boolean m_chkoutBeforeProcessing = true;
   }

   /**
    * Inner class contains transition lists for a workflow. 
    */   
   private class WorkflowTransitions
   {
      /**
       * Construct an object with the specified transition list.
       * 
       * @param pubToEditTrans The transition list used to transfer an item
       *    from public state to an editable state. Assume not <code>null</code>
       *    or empty.
       *  
       * @param editToPubTrans The transition list used to transfer an item
       *    from an editable state to public state. Assume not <code>null</code>
       *    or empty.
       */
      private WorkflowTransitions(List pubToEditTrans, List editToPubTrans)
      {
         m_pubToEditTrans = pubToEditTrans;
         m_editToPubTrans = editToPubTrans;
      }
      
      /**
       * Get the transition list that is used to transfer items from public
       * state to editable state.
       * 
       * @return The transition list, never <code>null</code> or empty.
       */
      private List getPubToEditTrans()
      {
         return m_pubToEditTrans;
      }
      
      /**
       * Get the transition list that is used to transfer items from editable
       * state to public state.
       * 
       * @return The transition list, never <code>null</code> or empty.
       */
      private List getEditToPubTrans()
      {
         return m_editToPubTrans;
      }
      
      /**
       * Init by ctor, never <code>null</code> or empty, see ctor for detail.
       */
      private List m_pubToEditTrans;

      /**
       * Init by ctor, never <code>null</code> or empty, see ctor for detail.
       */
      private List m_editToPubTrans;
   }
   
   /**
    * Main for this converter application.
    * 
    * @param args - @see usage
    */
   public static void main(String[] args)
   {
      // no args required
      if (args.length > 0)
      {
         printUsage();
         System.exit(-1);
      }
      
      // load the properties file
      FileInputStream in = null;
      Properties props = new Properties();
      try
      {
         in = new FileInputStream(DEFAULT_PROPERTIES_FILE);
         props.load(in);
      }
      catch (FileNotFoundException e)
      {
         System.out.println("Unable to locate file: " + DEFAULT_PROPERTIES_FILE);
         printUsage();
         System.exit(-1);
      }
      catch (IOException e)
      {
         System.out.println(
            "Error loading properties from file (" 
               + DEFAULT_PROPERTIES_FILE 
               + "): " 
               + e.toString());
         printUsage();
         System.exit(-1);
      }
      finally
      {
         if (in != null)
         {
            try 
            {
                  in.close();
            }
            catch (Exception e) { /* ignore */ }
         }
      }
      
      FileInputStream cvXSL = null;
      Document xslDoc = null;
      try
      {
         cvXSL = new FileInputStream(INLINE_LINK_CONVERTER_XSL);
         xslDoc = PSXmlDocumentBuilder.createXmlDocument(cvXSL, false);
      }
      catch (FileNotFoundException e)
      {
         System.out.println("Unable to locate file: " + INLINE_LINK_CONVERTER_XSL);
         printUsage();
         System.exit(-1);
      }
      catch (IOException e)
      {
         System.out.println(
            "Error loading xsl file (" 
               + INLINE_LINK_CONVERTER_XSL 
               + "): " 
               + e.toString());
         printUsage();
         System.exit(-1);
      }
      catch (SAXException e)
      {
         System.out.println(
               "Error parsing xsl file (" 
                  + INLINE_LINK_CONVERTER_XSL 
                  + "): " 
                  + e.toString());
            printUsage();
            System.exit(-1);
      }
      finally
      {
         if (cvXSL != null)
         {
            try 
            {
               cvXSL.close();
            }
            catch (Exception e) { /* ignore */ }
         }
      }

      try
      {
         // start the conversion process
         PSInlineLinkConverter ilc = new PSInlineLinkConverter(props, xslDoc);
         ilc.doConvert();
      }
      catch (Exception e)
      {
         System.out.println(
            "Error - caught unknown exception: " + e.getMessage());
         System.exit(-1);
      }

      System.exit(0);
   }

   /**
    * Prints the command line usage of this class to the console.
    */
   protected static void printUsage()
   {
      System.out.println("Usage: ");
      System.out.println("");
      System.out.println(
         "java com.percussion.inlinelinkconverter.PSInlineLinkConverter ");
      System.out.println(
            "See the PSInlineLinkConverter documentation for details on "
            + "the properties file.");
   }

   /**
    * The properties from the property file. Init by ctor, 
    * never <code>null</code>.
    */
   protected Properties m_props = new Properties();
   
   /**
    * This is used to communicate with Rhythmyx server, init by ctor, never
    * <code>null</code> after that.
    */
   protected PSRemoteAgent m_rtAgent = null;

   /**
    * The community used during the conversion process. It is initialized by
    * ctor, never <code>null</code> after that.
    */
   private PSEntry m_community = null;
   
   /**
    * The user name or login id, used to communicate with Rhythmyx server
    * during the conversion process. Init by ctor, never <code>null</code> or 
    * empty after that.
    */
   private String m_userName = null;
   
   /**
    * A list of all content type (as <code>PSEntry</code>) in the current 
    * community. Init by ctor, never <code>null</code> after that. 
    */
   private List m_allContentTypes = null;
   
   /**
    * It maps content type to a list of inline link field names. The map key is
    * the content type as <code>String</code>. The map value is a list of 
    * inline link field names as <code>List</code>, which contains zero or more
    * <code>String</code> objects. Never <code>null</code>.
    */
   private Map m_inlineLinkFieldMap = new HashMap();
  
   /**
    * Document of the stylesheet, used to convert the inline links. 
    */
   private Document m_XslDoc = null;
   
   /**
    * The Rhythmyx URL, init by ctor, never <code>null</code> after that.
    */
   private String m_RxLocation = null;
   
   /**
    * The session id of the current login, init by ctor, never <code>null</code>
    * after that.
    */
   private String m_RxSession = null;

   /**
    * The writer for all logging data, init by ctor, never <code>null</code>
    * after that.  
    */
   private BufferedWriter m_logger = null;
   
   /**
    * The writer for logging successfully converted items. Init by ctor, 
    * never <code>null</code> after that.
    */
   private BufferedWriter m_loggerSuccess = null;
   
   /**
    * The writer for logging the items that were unable to be converted. Init
    * by ctor, never <code>null</code> after that.
    */
   private BufferedWriter m_loggerFail = null;

   /**
    * It maps workflow name to its transition lists. The map key is the name 
    * a workflow as <code>String</code>. The workflow name is normalize by 
    * by {@link #normalizeWorkflowName(String)}. The map value is the transition
    * list of the workflow as {@link WorkflowTransitions}.
    */
   private Map m_wfTransMap = new HashMap();
   
   /**
    * It maps workflow id to workflow name. The map key is the id as 
    * <code>String</code>. The map value is workflow name, which is normalized
    * by {@link normalizeWorkflowName(String)}.
    */
   private Map m_wfIdNameMap = new HashMap();
      
   private static final String DEFAULT_PROPERTIES_FILE =
      "inlinelinkconverter.properties";
   private static final String INLINE_LINK_CONVERTER_XSL =
      "inlinelinkconverter.xsl";

   private static final String NEWLINE = "\r\n";

   /**
    * Send message SOAP constants
    */
   public static final String SEARCH_OPERATION = "search";
   public static final String SEARCH_REQUEST = "SearchRequest";
   public static final String SEARCH_RESPONSE = "SearchResponse";
   public static final String SEARCH_PORT = "Search";
   public static final String SEARCH_RESULTS = "SearchResults";
   public static final String RESULT_FIELD = "ResultField";
   public static final String SEARCH_PARAMETER = "Parameter";
   public static final String SEARCH_FIELD = "SearchField";
   public static final String SEARCH_PARAMS = "SearchParams";
   
   public static final String NAME_ATTR = "name";
   
   /**
    * Search result field names
    */
   public static final String SYS_CONTENTID = "sys_contentid";
   public static final String SYS_TIPREVISION = "sys_tiprevision";
   public static final String SYS_CHKOUT_USER = "sys_contentcheckoutusername";
   public static final String SYS_WORKFLOWID = "sys_workflowid";
   public static final String SYS_PUB_TYPE = "sys_publishabletype";
   
   /**
    * The search result of the {@link #SYS_PUB_TYPE} field when an 
    * item is in public state. 
    */
   public static final String PUBLIC_TYPE = "y";
   
   /**
    * The log file name, which contains all logged information
    */
   public static final String LOG_ALL = "convert.log";
   
   /**
    * The log file name, contains the items (id, rev) that have 
    * successfully converted. 
    */
   public static final String LOG_SUCCESS = "convert_success.log";
   
   /**
    * The log file name, contains the items (id, rev) which were 
    * unable to be converted. 
    */
   public static final String LOG_FAIL = "convert_fail.log";
}
