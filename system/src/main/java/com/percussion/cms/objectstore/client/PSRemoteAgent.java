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

package com.percussion.cms.objectstore.client;

import com.percussion.HTTPClient.PSBinaryFileData;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSBinaryValue;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSItemFieldMeta;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.cms.objectstore.ws.PSRemoteFolderProcessor;
import com.percussion.cms.objectstore.ws.PSRemoteWsRequester;
import com.percussion.design.objectstore.IPSDatabaseComponent;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.util.IPSRemoteRequesterEx;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class provides convenient methods to communicate with the remote
 * server for various tasks.
 */
public class PSRemoteAgent
{

   private static final Logger log = LogManager.getLogger(PSRemoteAgent.class);

   /**
    * Constructs a new remote agent object without a requester set.
    * A requester must be set before using this agent.
    */
   protected PSRemoteAgent()
   {
   }

   /**
    * Constructs the object from passed in requester.
    *
    * @param requester the {@link com.percussion.util.IPSRemoteRequester}
    * object used to make remote requests. Cannot be <code>null</code>.
    */
   public PSRemoteAgent(IPSRemoteRequester requester)
   {
      setRequester(requester);
   }

   /**
    * Sets the remote requester for this agent
    *
    * @param requester the {@link com.percussion.util.IPSRemoteRequester}
    * object used to make remote requests. Cannot be <code>null</code>.
    */
   protected void setRequester(IPSRemoteRequester requester)
   {
      if (requester == null)
         throw new IllegalArgumentException("Requester cannot be null.");

      m_requester = new PSRemoteWsRequester(requester);
   }


   /**
    * Get the remote requester that is used to communicate to Rhythmyx Server.
    *
    * @return The remote requester, never <code>null</code>.
    */
   public IPSRemoteRequester getRemoteRequester()
   {
      return m_requester.getRemoteRequester();
   }

   /**
    * Determines whether the current connection info contains valid login info.
    *
    * @return <code>true</code> if successful login remote server;
    *    <code>false</code> otherwise.
    */
   public boolean validateLogin()
   {
      try
      {
         login();
      }
      catch (PSRemoteException e)
      {
         return false;
      }
      return true;
   }

   /**
    * Get the default community of the login user.
    *
    * @return The default community.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public PSEntry getDefaultUserCommunity() throws PSRemoteException
   {
      return login();
   }

   /**
    * Set the community of the connection from the supplied community
    *
    * @param community The to be set community, it may be <code>null</code>.
    */
   public void setCommunity(String community)
   {
      m_community = community;
   }

   /**
    * Login to the remote server with the current connection info.
    * This will also set the login user to the community and locale that are
    * defined in the connection info (if they are specified).
    *
    * @return The community that is logged in, never <code>null</code>.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public PSEntry login() throws PSRemoteException
   {
      PSEntry community = null;

      try
      {
         Element responseData = loginEx();

         // get the community id from the login response
         Element loginDataEl =
            PSXMLDomUtil.getFirstElementChild(responseData, "LoginData");
         String communityId =
            PSXMLDomUtil.checkAttribute(
               loginDataEl,
               "defaultCommunityId",
               true);
         community = new PSEntry(communityId, new PSDisplayText("Unknown"));

         Element sessionIdEl =
            PSXMLDomUtil.getFirstElementChild(loginDataEl, "SessionId");
         Element communitiesEl =
            PSXMLDomUtil.getNextElementSibling(sessionIdEl, "Communities");

         Element communityEl =
            PSXMLDomUtil.getFirstElementChild(communitiesEl, "Community");
         while (communityEl != null)
         {
            String id = PSXMLDomUtil.checkAttribute(communityEl, "id", true);
            if (id.equals(communityId))
            {
               String name = PSXMLDomUtil.getElementData(communityEl);
               community = new PSEntry(id, new PSDisplayText(name));
               break;
            }
            communityEl =
               PSXMLDomUtil.getNextElementSibling(communityEl, "Community");
         }
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSRemoteException(e);
      }

      return community;
   }

   /**
    * Just like {@link #login()}, except it returns the response document
    * which is defined in sys_MiscellaneousParameters.xsd
    *
    * @return the response element, <code>LoginResponse</code>, never
    *    <code>null</code>.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public Element loginEx() throws PSRemoteException
   {
      Element params = getLoginParams();
      return sendRequest(LOGIN_ACTION, WS_MISC, params, LOGIN_RESPONSE);
   }


   /**
    * Get a list of communities from the remote server.
    *
    * @return An iterator over zero or more <code>PSEntry</code> objects,
    *    never <code>null</code>, but may be empty.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public List getCommunities() throws PSRemoteException
   {
      List communities = new ArrayList();
      try
      {
         Document doc =
            m_requester.getRemoteRequester().getDocument(
               "sys_cmpCommunities/communities.xml",
               null);

         // retrieve the communities from the "doc", its format is defined by
         // $RxRoot/sys_cmpCommunities/communities.dtd
         // <!ELEMENT communities (componentname, pagename, relatedlinks,
         //                                     newcommunityurl, list* )>
         // <!ELEMENT list (communityname, communityid, communitydesc,
         //                      editcommunityurl, deletecommunityurl )>
         // <!ELEMENT communityid (#PCDATA)>
         // <!ELEMENT communityname (#PCDATA)>
         NodeList communityNodes = doc.getElementsByTagName("list");
         for (int i = 0; i < communityNodes.getLength(); i++)
         {
            Element communityEl = (Element)communityNodes.item(i);
            Element nameEl =
               PSXMLDomUtil.getFirstElementChild(communityEl, "communityname");
            Element idEl =
               PSXMLDomUtil.getNextElementSibling(nameEl, "communityid");
            String name = PSXMLDomUtil.getElementData(nameEl);
            String id = PSXMLDomUtil.getElementData(idEl);

            PSEntry community = new PSEntry(id, new PSDisplayText(name));
            communities.add(community);
         }
      }
      catch (PSUnknownNodeTypeException unex)
      {
         throw new PSRemoteException(unex);
      }
      catch (Exception e)
      {
         throw new PSRemoteException(
            IPSRemoteErrors.REMOTE_UNEXPECTED_ERROR,
            e.toString());
      }

      return communities;
   }

   /**
    * Get a list of content-types for the supplied community
    *
    * @param community The community from where requesting the content-types.
    *    It may not be <code>null</code>.
    *
    * @return An iterator over zero or more <code>PSEntry</code> objects,
    *    never <code>null</code>, but may be empty.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public List getContentTypes(PSEntry community) throws PSRemoteException
   {
      if (community == null)
         throw new IllegalArgumentException("community may not be null");

      return getCTypesOrWorkflows(community, "contenttypelookup.xml");
   }

   /**
    * Get a list of workflows for the supplied community
    *
    * @param community The community from where requesting workflows. It may
    *    not be <code>null</code>.
    *
    * @return An iterator over zero or more <code>PSEntry</code> objects,
    *    never <code>null</code>, but may be empty.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public List getWorkflows(PSEntry community) throws PSRemoteException
   {
      if (community == null)
         throw new IllegalArgumentException("community may not be null");

      return getCTypesOrWorkflows(community, "workflowlookup.xml");
   }

   /**
    * Get a list of content-types or workflows for the supplied community
    *
    * @param community The community from where requesting the content-types.
    *    Assume it is not <code>null</code>.
    *
    * @param resource The resource for geting content-type or workflows,
    *    assume not <code>null</code> or empty.
    *
    * @return An iterator over zero or more <code>PSEntry</code> objects,
    *    never <code>null</code>, but may be empty.
    *
    * @throws PSRemoteException if an error occurs.
    */
   private List getCTypesOrWorkflows(PSEntry community, String resource)
      throws PSRemoteException
   {
      List entries = new ArrayList();

      try
      {
         Map params = new HashMap();
         params.put("communityid", community.getValue());
         Document doc =
            m_requester.getRemoteRequester().getDocument(
               "sys_commSupport/" + resource,
               params);

         // Retrieve the communities from the "doc", its format is defined by
         // $RxRoot/sys_commSupport/commrelationlookup.dtd
         // <!ELEMENT commrelationlookup (reltypename, Addurl, list* )>
         // <!ELEMENT list (name, id, deleteurl )>
         // <!ELEMENT deleteurl (#PCDATA)>
         // <!ELEMENT id (#PCDATA)>
         // <!ELEMENT name (#PCDATA)>
         // <!ELEMENT Addurl (#PCDATA)>
         // <!ELEMENT reltypename (#PCDATA)>
         NodeList nodes = doc.getElementsByTagName("list");
         for (int i = 0; i < nodes.getLength(); i++)
         {
            Element entryEl = (Element)nodes.item(i);
            Element nameEl = PSXMLDomUtil.getFirstElementChild(entryEl, "name");
            Element idEl = PSXMLDomUtil.getNextElementSibling(nameEl, "id");
            String name = PSXMLDomUtil.getElementData(nameEl);
            String id = PSXMLDomUtil.getElementData(idEl);

            if (name == null
               || name.trim().length() == 0
               || id == null
               || id.trim().length() == 0)
            {
               continue;
            }
            PSEntry entry = new PSEntry(id, new PSDisplayText(name));
            entries.add(entry);
         }
      }
      catch (PSUnknownNodeTypeException unex)
      {
         throw new PSRemoteException(unex);
      }
      catch (Exception e)
      {
         throw new PSRemoteException(
         IPSRemoteErrors.REMOTE_UNEXPECTED_ERROR,
            e.toString());
      }

      return entries;
   }

   /**
    * Get a list of transitions for the supplied workflow
    *
    * @param workflow The workflow from where requesting the transitions.
    *    It may not be <code>null</code>.
    *
    * @return An iterator over zero or more <code>PSEntry</code> objects,
    *    never <code>null</code>, but may be empty. The value of the each
    *    <code>PSEntry</code> is the trigger or internal name.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public List getTransitions(PSEntry workflow) throws PSRemoteException
   {
      if (workflow == null)
         throw new IllegalArgumentException("workflow may not be null");

      List entries = new ArrayList();

      try
      {
         Map params = new HashMap();
         params.put(IPSHtmlParameters.SYS_WORKFLOWID, workflow.getValue());
         Document doc =
            m_requester.getRemoteRequester().getDocument(
               "sys_wfLookups/getAllTransitionNames.xml",
               params);

         // Retrieve the transitions from the "doc", its format is defined by
         // $RxRoot/sys_wfLookups/transitions.dtd
         // <!ELEMENT transitions (transition* )>
         // <!ELEMENT transition (#PCDATA)>
         // <!ATTLIST  transition id CDATA #IMPLIED>
         // <!ATTLIST  transition actiontrigger CDATA #IMPLIED>
         // <!ATTLIST  transition label CDATA #IMPLIED>
         // <!ATTLIST  transition description CDATA #IMPLIED>

         NodeList nodes = doc.getElementsByTagName("transition");
         for (int i = 0; i < nodes.getLength(); i++)
         {
            Element entryEl = (Element)nodes.item(i);
            String name = PSXMLDomUtil.checkAttribute(entryEl, "label", true);
            String trigger =
               PSXMLDomUtil.checkAttribute(entryEl, "actiontrigger", true);

            PSEntry entry = new PSEntry(trigger, new PSDisplayText(name));
            entries.add(entry);
         }
      }
      catch (PSUnknownNodeTypeException unex)
      {
         throw new PSRemoteException(unex);
      }
      catch (Exception e)
      {
         throw new PSRemoteException(
         IPSRemoteErrors.REMOTE_UNEXPECTED_ERROR,
            e.toString());
      }

      return entries;
   }



   /**
    * Get the comment from the last transition (not check In or 0ut). If no
    * transitions exist, an empty string is returned.
    *
    * <pre>
    *
    *  &lt;!ELEMENT History (#PCDATA)&gt;
    *  &lt;!ATTLIST History
    *            Comment  CDATA #IMPLIED
    *            Transitionid CDATA #IMPLIED
    *  &gt;
    * </pre>
    *  @param contentId
    *            for the item. Assumed valid content id.
    *
    *  @return the transistion comment. May be <code>null<code> if there is no
    *    transition performed for this supplied item.
    *
    *  @throws PSRemoteException if an error occurs.
    *
    */
   public String getLastTransitionComment(int contentId)
         throws PSRemoteException
   {
      try
      {
         Map params = new HashMap();
         params.put(IPSHtmlParameters.SYS_CONTENTID, Integer
               .toString(contentId));

         Document doc = m_requester.getRemoteRequester().getDocument(
               "sys_psxWorkflowCataloger/getLastTransitionHistory.xml", params);
         NodeList nodes = doc.getElementsByTagName(EL_HISTORY);
         if (nodes.getLength()==0)
            return null;
         Element item = (Element) nodes.item(0);
         String id = item.getAttribute(EL_TRANSITIONID);
         String comment = item.getAttribute(EL_COMMENT);

         return comment;
      }
      catch (Exception e)
      {
         throw new PSRemoteException(IPSRemoteErrors.REMOTE_UNEXPECTED_ERROR, e
               .toString());
      }
   }

   /**
    * Get a list of context variables from the remote server.
    *
    * @return An iterator over zero or more <code>PSEntry</code> objects,
    *         never <code>null</code>, but may be empty.
    *
    * @throws PSRemoteException
    *            if an error occurs.
    */
   public List getContextVariables() throws PSRemoteException
   {
      List entries = new ArrayList();

      try
      {
         Document doc =
            m_requester.getRemoteRequester().getDocument(
               "sys_pubVariables/variableslist.xml",
               null);

         // Retrieve the communities from the "doc", its format is defined by
         // $RxRoot/sys_pubVariables/variableslist.dtd
         // <!ELEMENT variableslist (relatedlinks, componentname, pagename,
         //                            currentpsfirst, newlink, category* )>
         // <!ELEMENT category (propname, newproplink, varlist* )>
         // <!ELEMENT varlist (sitelookupurl, propid, contextlookupurl,
         //                      propertyvalue, editlink, deletelink )>
         // <!ELEMENT propname (#PCDATA)>
         // <!ELEMENT propertyvalue (#PCDATA)>
         // <!ATTLIST  propertyvalue contextid CDATA #REQUIRED>
         //
         // The variable name is the value of "propname" element
         // Its "preview site" is the value of "propertyvalue" element where
         // the attribute of "contextid" == "0"

         NodeList categoryNodes = doc.getElementsByTagName("category");
         for (int i = 0; i < categoryNodes.getLength(); i++)
         {
            Element categoryEl = (Element)categoryNodes.item(i);
            Element nameEl =
               PSXMLDomUtil.getFirstElementChild(categoryEl, "propname");
            NodeList varlistNodes = categoryEl.getElementsByTagName("varlist");
            for (int j = 0; j < varlistNodes.getLength(); j++)
            {
               Element varlistEl = (Element)varlistNodes.item(j);
               NodeList nodes = varlistEl.getElementsByTagName("propertyvalue");
               Element valueEl = (Element)nodes.item(0);
               String contextid =
                  PSXMLDomUtil.checkAttribute(valueEl, "contextid", true);
               if (contextid.equals("0"))
               {
                  String name = PSXMLDomUtil.getElementData(nameEl);
                  String value = PSXMLDomUtil.getElementData(valueEl);
                  // strip the leading "../" in "value"
                  if (value.startsWith("../") && value.length() > 3)
                     value = value.substring(3);

                  PSEntry ctxVar = new PSEntry(value, new PSDisplayText(name));

                  entries.add(ctxVar);
               }
            }
         }
      }
      catch (PSUnknownNodeTypeException unex)
      {
         throw new PSRemoteException(unex);
      }
      catch (Exception e)
      {
         throw new PSRemoteException(
         IPSRemoteErrors.REMOTE_UNEXPECTED_ERROR,
            e.toString());
      }

      return entries;
   }



   /**
    * Perform a transition for the specified content.
    *
    * @param locator The locator of the specified content, may not
    *    <code>null</code>
    * @param transId The transition name or id, may not <code>null</code> or
    *    empty.
    * @param comment If supplied the comment is added as the transition comment.
    *    may be <code>null</code> or empty.
    *
    * @return <code>true</code> if successfully completed the transition;
    *    <code>false</code> otherwise.
    *
    * @throws PSRemoteException if content type not exist or any other
    *    error occurs.
    */
   public boolean transitionItem(PSLocator locator, String transId,
         String comment) throws PSRemoteException
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");
      if (transId == null || transId.trim().length() == 0)
         throw new IllegalArgumentException("transId may not be null or empty");

      Element params = getTransitionItemParams(locator, transId, comment);
      Element responseData =
         sendRequest(TRANSITEM_ACTION, WS_WORKFLOW, params, TRANSITEM_RESPONSE);

      return isSuccessResponse(PSXMLDomUtil.getFirstElementChild(responseData));
   }

   /**
    * Perform a transition for the specified content without comment.
    *
    * @param locator The locator of the specified content, may not
    *    <code>null</code>
    * @param transId The transition name or id, may not <code>null</code> or
    *    empty.
    * @return <code>true</code> if successfully completed the transition;
    *    <code>false</code> otherwise.
    *
    * @throws PSRemoteException if content type not exist or any other
    *    error occurs.
    */
   public boolean transitionItem(PSLocator locator, String transId)
         throws PSRemoteException
   {
      return transitionItem(locator, transId, null);
   }


   /**
    * Just like {@link #newItem(String contentTypeId)},
    * except that it will load the defaults if available for the passed
    * in content Type.
    *
    * @param contentTypeId The name or id of a content type, may not be
    *    <code>null</code> or empty.
    *
    * @return The created <code>PSClientItem</code> object, never
    *    <code>null</code> unless client item cannot be created.
    *
    * @throws PSRemoteException if content type not exist or any other
    *    error occurs.
    */
   public PSClientItem newItemDefault(
      String contentTypeId )
      throws PSRemoteException
   {
      Element params = getNewItemParams( contentTypeId);

      Element respData =
         sendRequest(
            NEWITEM_ACTION,
            WS_CONTENTDATA,
            params,
            NEWITEM_RESPONSE);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(respData);

      // <Item> Node
      Element itemEl =
         tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      try
      {
         PSClientItem item = newItem(contentTypeId);
         item.loadXmlData(itemEl);

         return item;
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSRemoteException(e);
      }
   }
   /**
    * Get a newly created (empty) <code>PSClientItem</code> object from the
    * specified content type name or id.
    *
    * @param contentTypeId The name or id of a content type, may not be
    *    <code>null</code> or empty.
    *
    * @return The created <code>PSClientItem</code> object, never
    *    <code>null</code> unless client item cannot be created.
    *
    * @throws PSRemoteException if content type not exist or any other
    *    error occurs.
    */
   public PSClientItem newItem(String contentTypeId) throws PSRemoteException
   {
      if (contentTypeId == null || contentTypeId.trim().length() == 0)
         throw new IllegalArgumentException("contentTypeId may not be null or empty");

      PSItemDefinition itemDef = getTypeDef(contentTypeId);
      try
      {
         return new PSClientItem(itemDef);
      }
      catch (PSCmsException ex)
      {
         log.info(ex);
         return null;
      }
   }

   /**
    * Get a list of field names associated with a content name or id.
    *
    * @param contentTypeId The name or id of a content type, may not be
    *    <code>null</code> or empty.
    *
    * @return List of <code>String</code> objects, never  <code>null</code>,
    * may be empty.
    *
    * @throws PSRemoteException if content type not exist or any other
    *    error occurs.
    */
   public List getFieldNames(String contentTypeId) throws PSRemoteException
   {
      PSClientItem clientItem = newItem(contentTypeId);
      Iterator itor = clientItem.getAllFieldNames();
      List list = new ArrayList();
      while (itor.hasNext())
         list.add(itor.next());
      return list;
   }

   /**
    * Updates an item (or <code>PSCoreItem</code>) object (in XML format) to
    * the server. The item will be inserted if it does not contains
    * a content-id and a revision number; otherwise the specified item will be
    * updated with the current content.
    *
    * @param itemEl The modified item, may not be <code>null</code>.
    *
    * @param checkin <code>true</code> if checkin the item afterwards;
    *    <code>false</code> leave the item to be checked out by the current
    *    user after updating the item.
    *
    * @return The locator of the updated item, never <code>null</code>.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public PSLocator updateItem(Element itemEl, boolean checkin)
      throws PSRemoteException
   {
      if (itemEl == null)
         throw new IllegalArgumentException("itemEl may not be null");

      Element params = getUpdateItemParams(itemEl, checkin);
      Element responseData =
         sendRequest(
            UPDATEITEM_ACTION,
            WS_CONTENTDATA,
            params,
            UPDATEITEM_RESPONSE);
      return getLocatorFromUpdateResponse(responseData);
   }

   /**
    * Updates an item object to the server. The item will be inserted if it does not contains
    * a content-id; otherwise the specified item will be updated with
    * the current content. This method can also handle the updating of
    * binary field data.
    * @param item the client item, cannot be <code>null</code>.
    *
    * @param checkin <code>true</code> if checkin the item afterwards;
    *    <code>false</code> leave the item to be checked out by the current
    *    user after updating the item.
    * @return The PSLocator for this client item. May be <code>null</code>
    * if the locator could not be found.
    * @throws PSRemoteException
    */
   public PSLocator updateItem(PSClientItem item, boolean checkin)
      throws PSRemoteException
   {
      if(item == null)
         throw new IllegalArgumentException("Client item cannot be null.");
      if(!(m_requester.getRemoteRequester() instanceof IPSRemoteRequesterEx))
         throw new IllegalStateException(
           "The requester must be an instance of " +
           "IPSRemoteRequesterEx when working on binaries.");

      IPSRemoteRequesterEx requester =
         (IPSRemoteRequesterEx)m_requester.getRemoteRequester();
      String app_resource = getAppResource(item);
      PSLocator locator = null;
      // Loop through all fields and setup field params and grab
      // binary data
      Map params = new HashMap();
      List files = new ArrayList();
      Iterator it = item.getAllFields();
      while(it.hasNext())
      {
         PSItemField field = (PSItemField)it.next();
         PSItemFieldMeta meta = field.getItemFieldMeta();

         try
         {
            if(meta.isBinary())
            {
               // Binary field
               IPSFieldValue fieldVal = field.getValue();
               if(fieldVal != null && fieldVal instanceof PSBinaryValueEx)
               {
                  PSBinaryValueEx binVal = (PSBinaryValueEx)fieldVal;
                  byte[] data = (byte[])binVal.getValue();

                  // If no filename defined then just skip
                  // this binary field
                  if(binVal.getFilename() == null ||
                      binVal.getFilename().trim().length()== 0)
                      continue;

                  files.add(new PSBinaryFileData(
                     data,
                     field.getName(),
                     binVal.getFilename(),
                     binVal.getContentType()));
               }
            }
            else
            {
               // Non-Binary field
               String value = field.getValue() != null ?
                  field.getValue().getValueAsString() :
                  "";
               params.put(field.getName(), value);

            }
         }
         catch(PSCmsException e)
         {
            throw new PSRemoteException(e);
         }

      }
      // Add necessary params
      if(item.getContentId() != -1)
      {
         params.put(IPSHtmlParameters.SYS_CONTENTID,
            Integer.toString(item.getContentId()));
         params.put(IPSHtmlParameters.SYS_REVISION,
            Integer.toString(item.getRevision()));
         params.put("DBActionType",
            IPSDatabaseComponent.DATABASE_ACTION_UPDATE);
      }
      else
      {
         params.put("DBActionType",
            IPSDatabaseComponent.DATABASE_ACTION_INSERT);
      }
      params.put(IPSHtmlParameters.SYS_CONTENTTYPEID,
            Long.toString(item.getContentTypeId()));
      params.put(IPSHtmlParameters.SYS_COMMAND, "modify");

      // Turn on a flag to indicate the attached file should be parsed as 
      // regular text, not parse the content as XML if case the mimeType
      // is "text/xml". 
      params.put(IPSHtmlParameters.REQ_XML_DOC_FLAG,
            IPSHtmlParameters.XML_DOC_AS_TEXT);
      // Add Parameter that can be used to detect webdav is being used for validation.
      params.put(IPSHtmlParameters.SYS_CLIENT, "WebDav");
      
      try
      {
         PSBinaryFileData[] tmp = new PSBinaryFileData[files.size()];
         files.toArray(tmp);
         locator =  requester.updateBinary(tmp, app_resource, params);
      }
      catch (IOException e)
      {
         Object[] args = new Object[] {e.getClass().getName(), e.getMessage()};
         throw new PSRemoteException(new PSException(1001, args));
      }
      catch (SAXException e)
      {
         throw new PSRemoteException(new PSException(e.getMessage()));
      }
      // Checkin if needed
      if(checkin && locator != null)
         checkInItem(locator);
      return locator;

   }



   /**
    * Returns the application resource path from a <code>PSClientItem</code>.
    * This method strip off anything preceding the "ApplicationName/resource" and
    * changes the extension to xml from html.
    *
    * @param item
    * @return
    */
   private String getAppResource(PSClientItem item)
   {
      String app_resource = item.getItemDefinition().getEditorUrl();
      if(app_resource == null || app_resource.trim().length() == 0)
         return null;
      app_resource =
         app_resource.substring(0, app_resource.lastIndexOf('.'));
      int aPos = app_resource.lastIndexOf('/');
      if(aPos == -1)
         throw new IllegalStateException(
            "The application resource in the client item is invalid.");
      int bPos = app_resource.lastIndexOf('/', aPos - 1);
      bPos = (bPos == -1) ? 0 : bPos;

      return app_resource.substring(bPos + 1, aPos) +
         app_resource.substring(aPos) + ".xml";

   }

   /**
    * Check out the specified item.
    *
    * @param locator The locator of the to be checked out item, may not be
    *    <code>null</code>.
    *
    * @return <code>true</code> if successfully checked out the item or the
    *    item has already checked out by the current user.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public boolean checkOutItem(PSLocator locator) throws PSRemoteException
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      Element params = getCheckInOutParams(locator, CHECKOUT_REQUEST);
      Element responseEl =
         sendRequest(CHECKOUT_ACTION, WS_MISC, params, CHECKOUT_RESPONSE);

      return isSuccessResponse(PSXMLDomUtil.getFirstElementChild(responseEl));
   }

   /**
    * Check in the specified item.
    *
    * @param locator The locator of the to be checked in item, may not be
    *    <code>null</code>.
    *
    * @return <code>true</code> if successfully checked in the item or the
    *    item has already checked in by the current user.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public boolean checkInItem(PSLocator locator) throws PSRemoteException
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      Element params = getCheckInOutParams(locator, CHECKIN_REQUEST);
      Element responseEl =
         sendRequest(CHECKIN_ACTION, WS_MISC, params, CHECKIN_RESPONSE);

      return isSuccessResponse(PSXMLDomUtil.getFirstElementChild(responseEl));
   }

   /**
    * Convenience method that calls {@link #purgeItems(PSLocator[])}. See that
    * method for details.
    *
    * @param locator the locator of the purged item, never <code>null</code>.
    *
    * @return <code>true</code> if successfully purged the item; otherwise
    *    return <code>false</code>.
    *
    * @throws PSRemoteException if error occurs.
    */
   public boolean purgeItem(PSLocator locator) throws PSRemoteException
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      PSLocator[] items = { locator };
      return purgeItems(items);
   }

   /**
    * Purge the specified items. Do not use this method to attempt to
    * purge folders as it will leave orphan content items, use
    * {@link #removeComponentsFromFolder(PSKey, List)} instead.
    *
    * @param items an array of locators for all items to be purged, may not be
    *    <code>null</code>.
    * @return <code>true</code> if successfully purged.
    * @throws PSRemoteException if an error occurs.
    */
   public boolean purgeItems(PSLocator[] items) throws PSRemoteException
   {
      Element params = getPurgeItemsParams(items);
      Element response = sendRequest(PURGEITEMS_ACTION, WS_CONTENTDATA, params,
         PURGEITEMS_RESPONSE);

      return isSuccessResponse(PSXMLDomUtil.getFirstElementChild(response));
   }

   /**
    * Removed the folder relationships between the supplied parent and its
    * children.
    *
    * @param parent the parent locator of the to be removed children. It may
    *    not be <code>null</code>.
    *
    * @param children the list of locators of the to be removed compontens. It
    *    may not be <code>null</code>.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public void removeComponentsFromFolder(PSKey parent, List children)
      throws PSRemoteException
   {
      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");

      if (children == null)
         throw new IllegalArgumentException("children may not be null");

      PSRemoteFolderProcessor processor = new PSRemoteFolderProcessor(
            m_requester.getRemoteRequester());
      try
      {
         processor.delete(parent, children);
      }
      catch (PSCmsException e)
      {
         throw new PSRemoteException(e);
      }
   }

   /**
    * Purge the entire tree for the supplied component. If the component is an
    * item, only that item is purged. If the component is a folder, then all
    * folders and items contained in the supplied folder will be purged
    * recursivly.
    *
    * @param summary the object to be purged, not <code>null</code>.
    * @return <code>true</code> if successfully purged, <code>false</code>
    *    otherwise.
    * @throws PSRemoteException if an error occurs.
    */
   public boolean purgeTree(PSComponentSummary summary)
      throws PSRemoteException
   {
      if (summary == null)
         throw new IllegalArgumentException("summary cannot be null");

      if (summary.isFolder())
      {
         Element params = getPurgeFolderParams(summary.getCurrentLocator());
         Element response = sendRequest(
            PSRemoteFolderProcessor.PURGE_FOLDER_OPERATION, WS_FOLDER, params,
            PSRemoteFolderProcessor.PURGE_FOLDER_RESPONSE);

         return isSuccessResponse(PSXMLDomUtil.getFirstElementChild(response));
      }
      else if (summary.isItem())
         return purgeItem(summary.getCurrentLocator());

      return false;
   }

   /**
    * Get component processor proxy for remote processor.
    *
    * @return The remote proxy, never <code>null</code>.
    *
    * @throws PSCmsException if any error occurs
    */
   private PSComponentProcessorProxy getRemoteComponentProxy()
      throws PSCmsException
   {

      PSComponentProcessorProxy proxy =
         new PSComponentProcessorProxy(
            PSComponentProcessorProxy.PROCTYPE_REMOTE,
            m_requester.getRemoteRequester());
      return proxy;
   }

   /**
    * Determines whether an operation is successful or not from a given
    * response XML element.
    *
    * It is in the format of:
    *
    * &lt;!ELEMENT ResultResponse EMPTY&gt;
    * &lt;!ATTLIST ResultResponse
    * type (success | failure)
    * &gt;
    *
    * @param responseEl The response XML element, assume not <code>null</code>.
    *
    * @return <code>true</code> if the response indicates a successful
    *    operation; <code>false</code> otherwise.
    */
   private boolean isSuccessResponse(Element responseEl)
   {
      String name = PSXMLDomUtil.getUnqualifiedNodeName(responseEl);
      if (name.equals(XML_NODE_RESPONSE))
      {
         String typeAttr = responseEl.getAttribute(XML_ATTR_TYPE);
         return (typeAttr != null && typeAttr.equals(XML_SUCCESS));
      }
      else
      {
         return false;
      }
   }

   /**
    * Creates a duplicated item from the supplied locator. The "New Copy"
    * configuration will be used to create related child items.
    *
    * @param locator The locator of the to be duplicated item, never
    *    <code>null</code>.
    *
    * @return The created item in XML that is conformed with
    *    <code>sys_StandardItem.xsd</code>, never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if encounter malformed XML.
    * @throws PSRemoteException if other error occurs.
    */
   public Element newCopyItem(PSLocator locator) throws PSRemoteException,
         PSUnknownNodeTypeException
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element params = doc.createElement(NEWCOPY_REQUEST);
      Element contentKeyEl = getContentKeyElement(doc, locator);
      params.appendChild(contentKeyEl);

      Element respData =
         sendRequest(
            NEWCOPY_ACTION,
            WS_CONTENTDATA,
            params,
            NEWCOPY_RESPONSE);

      return PSXMLDomUtil.getFirstElementChild(respData, PSCoreItem.EL_ITEM);
   }

   /**
    * Get an item from a given locator
    *
    * @param locator The locator of the specified item, may not be
    *    <code>null</code>
    * @param includeData <code>true</code> if wants the return item contains
    *    all data; <code>false</code> if don't want include any data
    *    in the returned item.
    * @param checkOut <code>true</code> if wants to checkout the item as well;
    *    <code>false</code> not to apply checkout operation to the item.
    *
    * @return The specified item, never <code>null</code>.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public PSClientItem openItem(
      PSLocator locator,
      boolean includeData,
      boolean checkOut)
      throws PSRemoteException
   {
       return
          openItem(locator, includeData, includeData, includeData, checkOut);
   }

   /**
    * Get an item from a given locator
    *
    * @param locator The locator of the specified item, may not be
    *    <code>null</code>
    *
    * @param includeChildren <code>true</code> if wants to include all
    * child field data in the returned envelope; <code>false</code>
    * if don't want include any child data in the responsed envelope.
    *
    * @param includeRelated <code>true</code> if wants to include all
    * related data in the returned envelope; <code>false</code>
    * if don't want include any related data in the responsed envelope.
    *
    * @param includeBinary <code>true</code> if wants to include all
    * binary field data in the returned envelope; <code>false</code>
    * if don't want include any binary data in the responsed envelope.
    *
    *  @param checkOut <code>true</code> if wants to checkout the item as well;
    *    <code>false</code> not to apply checkout operation to the item.
    *
    * @return The specified item, never <code>null</code>.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public PSClientItem openItem(
      PSLocator locator,
      boolean includeChildren,
      boolean includeRelated,
      boolean includeBinary,
      boolean checkOut)
      throws PSRemoteException
   {
      return openItem(
         locator,
         includeChildren,
         includeRelated,
         includeBinary,
         checkOut,
         null);
   }

   /**
    * Just like {@link #openItem(PSLocator,boolean,boolean,boolean,boolean)},
    * except it pass in the item definition, for the opened item.
    *
    * @param itemDef The item definition of the opened item, it may be
    *    <code>null</code>. The item definition will be retrieved from server
    *    if it is not specified.
    */
   public PSClientItem openItem(
      PSLocator locator,
      boolean includeChildren,
      boolean includeRelated,
      boolean includeBinary,
      boolean checkOut,
      PSItemDefinition itemDef)
      throws PSRemoteException
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      Element params = getOpenItemParams(
         locator, includeChildren, includeRelated, false, checkOut);

      Element respData =
         sendRequest(
            OPENITEM_ACTION,
            WS_CONTENTDATA,
            params,
            OPENITEM_RESPONSE);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(respData);

      // <Item> Node
      Element itemEl =
         tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      try
      {
         PSClientItem item = null;
         if (itemDef == null)
         {
            String contentTypeId = itemEl.getAttribute(CONTENTTYPE_ACTION);
            item = newItem(contentTypeId);
         }
         else
         {
            item = new PSClientItem(itemDef);
         }

         item.loadXmlData(itemEl);
         if(includeBinary)
            loadItemBinaries(item);

         return item;
      }
      catch (PSCmsException e)
      {
         throw new PSRemoteException(e);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSRemoteException(e);
      }
      catch (IOException ioe)
      {
         log.error(ioe.getMessage());
         log.debug(ioe.getMessage(), ioe);
         throw new PSRemoteException(new PSException(ioe.getMessage()));
      }
   }

   /**
    * Loops through all of the items fields looking for binary fields,
    * if a binary field is found a request is made to get the binary, the
    * binary is then added to the field as a <code>PSBinaryValue</code>
    * object.
    * @param item the client item, cannot be <code>null</code>.
    * @throws IOException if the request for the binary has errors.
    */
   private void loadItemBinaries(PSClientItem item) throws IOException
   {
      if(item == null)
         throw new IllegalArgumentException("Client item cannot be null.");
      if(!(m_requester.getRemoteRequester() instanceof IPSRemoteRequesterEx))
         throw new IllegalStateException(
           "The requester must be an instance of " +
           "IPSRemoteRequesterEx when working on binaries.");

      IPSRemoteRequesterEx requester =
         (IPSRemoteRequesterEx)m_requester.getRemoteRequester();
      String app_resource = getAppResource(item);
      Iterator it = item.getAllFields();
      // Set up common params
      Map params = new HashMap(4);
      params.put(IPSHtmlParameters.SYS_COMMAND,
         "binary");
      params.put(IPSHtmlParameters.SYS_CONTENTID,
         Integer.toString(item.getContentId()));
      params.put(IPSHtmlParameters.SYS_REVISION,
         Integer.toString(item.getRevision()));


      while(it.hasNext())
      {
         PSItemField field = (PSItemField)it.next();
         PSItemFieldMeta meta = field.getItemFieldMeta();

         if(meta.isBinary())
         {
            params.put("sys_submitname", field.getName());
            byte[] data = requester.getBinary(app_resource, params);
            field.addValue(new PSBinaryValue(data));

         }

      }
   }

   /**
    * Get the locator from the response of an "updateItem" operation (in XML).
    *
    * The XML format is:
    * &lt;!ELEMENT UpdateItemResponse (ContentKey | ResultResponse)&gt;
    *
    * &lt;!ELEMENT ContentKey EMPTY&gt;
    * &lt;!ATTLIST ContentKey
    * contentId CDATA #REQUIRED
    * revision CDATA #REQUIRED
    * &gt;
    *
    * &lt;!ELEMENT ResultResponse EMPTY&gt;
    * &lt;!ATTLIST ResultResponse
    * type (success | failure)
    * &gt;
     *
    * @param responseEl The response of an "updateItem" operation (in XML).
    *
    * @return The retrieved locator, never <code>null</code>.
    *
    * @throws PSRemoteException if an error occurs.
    */
   private PSLocator getLocatorFromUpdateResponse(Element responseEl)
      throws PSRemoteException
   {
      Element el = PSXMLDomUtil.getFirstElementChild(responseEl);
      String name = PSXMLDomUtil.getUnqualifiedNodeName(el);

      if (!name.equals(CONTENTKEY_NODE) && !isSuccessResponse(el))
      {
         Object[] args =
            { XML_NODE_RESPONSE, PSXmlDocumentBuilder.toString(responseEl)};
         throw new PSRemoteException(IPSRemoteErrors.REMOTE_WRONG_SOAP_RESP, args);
      }

      try
      {
         int id = PSXMLDomUtil.checkAttributeInt(el, XML_ATTR_CONTENT_ID, true);
         int rev = PSXMLDomUtil.checkAttributeInt(el, XML_ATTR_REVISION, true);

         PSLocator locator = new PSLocator(id, rev);
         return locator;
      }
      catch (PSUnknownNodeTypeException e)
      {
         Object[] args = { CONTENTKEY_NODE, PSXmlDocumentBuilder.toString(el)};
         throw new PSRemoteException(IPSRemoteErrors.REMOTE_WRONG_SOAP_RESP, args);
      }
   }

   /**
    * Get an item definition from the specified content type id or name.
    *
    * @param contentTypeId The content type id or name, it may not be
    *    <code>null</code> or empty.
    *
    * @return The item definition object, never <code>null</code>.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public PSItemDefinition getTypeDef(String contentTypeId)
      throws PSRemoteException
   {
      if (contentTypeId == null || contentTypeId.trim().length() == 0)
         throw new IllegalArgumentException(
            "contentTypeId may not be null or empty");

      Element message = getContentTypeParams(contentTypeId);

      try
      {
         Element respData =
            sendRequest(
               CONTENTTYPE_ACTION,
               WS_DESIGN,
               message,
               CONTENTTYPE_RESPONSE);

         Element el = PSXMLDomUtil.getFirstElementChild(respData);
         String itemDefStr = PSXMLDomUtil.getElementData(el);

         Document itemDefDoc =
            PSXmlDocumentBuilder.createXmlDocument(
               new StringReader(itemDefStr),
               false);

         return new PSItemDefinition(itemDefDoc.getDocumentElement());
      }
      catch (Exception e)
      {
         throw new PSRemoteException(
         IPSRemoteErrors.REMOTE_UNEXPECTED_ERROR,
            e.toString());
      }
   }

   /**
    * Send a request to webservices handler of the remote Rhythmyx server.
    *
    * @param action The action of the request, assume not <code>null</code>
    *    or empty.
    * @param wsdlPort The WSDL port as a way to group the actions. Assume
    *    not <code>null</code> or empty.
    * @param params The parameters of the request, assume not <code>null</code>
    * @param respElement The expected XML element name of the responsed root
    *    element.
    *
    * @return The responsed element send from the remote server. It should
    *    never be <code>null</code>.
    *
    * @throws PSRemoteException if the responsed element is not expected or
    *    other error occurred.
    */
   private Element sendRequest(
      String action,
      String wsdlPort,
      Element params,
      String respElement)
      throws PSRemoteException
   {
      // set community and locale if available
      Map extraParams = null;
      extraParams = new HashMap();
      if (m_community != null || m_locale != null)
      {
         extraParams = new HashMap();
         if (m_community != null)
            extraParams.put(IPSHtmlParameters.SYS_COMMUNITY, m_community);
         if (m_locale != null)
            extraParams.put(IPSHtmlParameters.SYS_LANG, m_locale);
      }

      Element data = null;
      try
      {
         data =
            m_requester.sendRequest(
               action,
               wsdlPort,
               params,
               extraParams,
               respElement);
      }
      catch (Exception e)
      {
         throw new PSRemoteException(
         IPSRemoteErrors.REMOTE_UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }

      return data;
   }

   /**
    * Creates a request parameter element for a content type action
    * (or operation).
    *
    * @param contentTypeId The content type id or name, assume not
    *    <code>null</code> or empty.
    *
    * @return The body of the envelope, never <code>null</code>
    */
   private Element getContentTypeParams(String contentTypeId)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element params = doc.createElement(CONTENTTYPE_REQUEST);
      Element data = doc.createElement(XML_NODE_CONTENTTYPE_ID);
      Text dataVal = doc.createTextNode(contentTypeId);
      data.appendChild(dataVal);
      params.appendChild(data);

      return params;
   }

   /**
    * Creates a request parameter element for an open item action or operation.
    *
    * @param locator The locator for the to be retreived item, assume not
    *    <code>null</code>.
    *
    * @param includeChildren <code>true</code> if wants to include all
    * child field data in the returned envelope; <code>false</code>
    * if don't want include any child data in the responsed envelope.
    *
    * @param includeRelated <code>true</code> if wants to include all
    * related data in the returned envelope; <code>false</code>
    * if don't want include any related data in the responsed envelope.
    *
    * @param includeBinary <code>true</code> if wants to include all
    * binary field data in the returned envelope; <code>false</code>
    * if don't want include any binary data in the responsed envelope.
    *
    * @param checkOut <code>true</code> if wants to checkout the item;
    *    <code>false</code> not to apply checkout operation to the item.
    *
    * @return The body of the envelope, never <code>null</code>
    */
   private Element getOpenItemParams(
      PSLocator locator,
      boolean includeChildren,
      boolean includeRelated,
      boolean includeBinary,
      boolean checkOut)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element params = doc.createElement(OPENITEM_REQUEST);

      Element contentKeyEl = getContentKeyElement(doc, locator);
      params.appendChild(contentKeyEl);

      String sIncludeChildren = includeChildren ? XML_TRUE : XML_FALSE;
      String sIncludeRelated = includeRelated ? XML_TRUE : XML_FALSE;
      String sIncludeBinary = includeBinary ? XML_TRUE : XML_FALSE;

      Element data = doc.createElement(EL_INCLUDE_CHILDREN);
      Text dataVal = doc.createTextNode(sIncludeChildren);
      data.appendChild(dataVal);
      params.appendChild(data);

      data = doc.createElement(EL_INCLUDE_RELATED);
      dataVal = doc.createTextNode(sIncludeRelated);
      data.appendChild(dataVal);
      params.appendChild(data);

      data = doc.createElement(EL_INCLUDE_BINARY);
      dataVal = doc.createTextNode(sIncludeBinary);
      data.appendChild(dataVal);
      params.appendChild(data);

      data = doc.createElement(EL_CHECK_OUT);
      dataVal = doc.createTextNode(checkOut ? XML_TRUE : XML_FALSE);
      data.appendChild(dataVal);
      params.appendChild(data);

      return params;
   }

   /**
    * Creates a request parameter element for a update item action.
    *
    * @param elData The XML format of the to be updated item.
    * @param checkin <code>true</code> if the item will be checked in
    *    afterwards; <code>false</code> if the item will be checked out
    *    afterwards.
    *
    * @return The created parameter element, never <code>null</code>
    */
   private Element getUpdateItemParams(Element elData, boolean checkin)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element params = doc.createElement(UPDATEITEM_REQUEST);
      PSXmlDocumentBuilder.copyTree(doc, params, elData);

      Element data = doc.createElement(EL_CHECK_IN);
      Text dataVal = doc.createTextNode(checkin ? XML_TRUE : XML_FALSE);
      data.appendChild(dataVal);
      params.appendChild(data);

      return params;
   }

   /**
    * Creates a request parameter element for a new item action.
    *

    * @param contentType the name or id of a content type, may not be
    *    <code>null</code> or empty.
    *
    * @return The created parameter element, never <code>null</code>
    */
   private Element getNewItemParams(String contentType)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element params = doc.createElement(NEWITEM_REQUEST);

      Element data = doc.createElement(EL_CONTENTTYPE);
      Text dataVal = doc.createTextNode(contentType);
      data.appendChild(dataVal);
      params.appendChild(data);

      return params;
   }

   /**
    * Creates a request parameter element for login action.
    *
    * @return The created parameter element, never <code>null</code>.
    */
   private Element getLoginParams()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element params = doc.createElement(LOGIN_REQUEST);

      return params;
   }

   /**
    * Creates a request parameter element for check in or out action.
    *
    * @param locator The locator of the to be checked out item, assume not
    *    <code>null</code>
    *
    * @param checkInOutName The request name, assume not <code>null</code> or
    *    empty.
    *
    * @return The created parameter element, never <code>null</code>.
    */
   private Element getCheckInOutParams(PSLocator locator, String checkInOutName)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element params = doc.createElement(checkInOutName);

      Element contentKeyEl = getContentKeyElement(doc, locator);
      params.appendChild(contentKeyEl);

      return params;
   }

   /**
    * Creates a request parameter element for the purge action.
    *
    * @param items an array of locators for all items to be purged, assume not
    *    <code>null</code>
    * @return the created parameter element, never <code>null</code>.
    */
   private Element getPurgeItemsParams(PSLocator[] items)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element params = doc.createElement(PURGEITEMS_REQUEST);
      for (int i=0; i<items.length; i++)
      {
         Element purgeKeyEl = createPurgeKeyElement(doc, items[i]);
         params.appendChild(purgeKeyEl);
      }

      return params;
   }
   /**
    * Creates a transition item parameters from a locator and transition-id.
    *
    * @param locator The locator of the transition, assume not <code>null</code>
    *
    * @param transId The transition-id, which can be the name of the transition
    *    or the (number) id of the transition. Assume not <code>null</code> or
    *    empty.
    *
    * @param comment the comment of the transition, may be <code>null</code> if
    *    no comment for the transition.
    *
    * @return The body of the soap envelope for the transition item action.
    */
   private Element getTransitionItemParams(PSLocator locator, String transId,
         String comment)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element params = doc.createElement(TRANSITEM_REQUEST);

      Element contentKeyEl = getContentKeyElement(doc, locator);
      params.appendChild(contentKeyEl);

      Element transIdEl = doc.createElement(EL_TRANSITIONITEM_ID);
      Text value = doc.createTextNode(transId);
      transIdEl.appendChild(value);
      params.appendChild(transIdEl);
      if (comment != null) {
          Element commentEl = doc.createElement(EL_COMMENT);
          value = doc.createTextNode(comment);
          commentEl.appendChild(value);
          params.appendChild(commentEl);
      }

      return params;
   }

   /**
    * Creates the parameters element used for the purge folder action.
    *
    * @param locator the locator of the folder to be purged, assumed not
    *    <code>null</code>.
    * @return the body element of the soap envelop used for the purge fodler
    *    action, never <code>null</code>.
    */
   private Element getPurgeFolderParams(PSLocator locator)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element params = doc.createElement(
         PSRemoteFolderProcessor.PURGE_FOLDER_REQUEST);

      Element folderIdEl = PSXmlDocumentBuilder.addElement(doc, params,
         PSRemoteFolderProcessor.FOLDER_ID_EL, "" + locator.getId());
      params.appendChild(folderIdEl);

      return params;
   }

   /**
    * Creates a content-key element from a given locator.
    *
    * @param locator The locator for the content-key element, assume not
    *    <code>null</code>.
    *
    * @return The created content-key element, never <code>null</code>.
    */
   private Element getContentKeyElement(Document bodyDoc, PSLocator locator)
   {
      Element contentKeyEl = bodyDoc.createElement(CONTENTKEY_NODE);

      contentKeyEl.setAttribute(
         XML_ATTR_CONTENT_ID,
         Integer.toString(locator.getId()));
      contentKeyEl.setAttribute(
         XML_ATTR_REVISION,
         Integer.toString(locator.getRevision()));

      return contentKeyEl;
   }

   /**
    * Creates a purge key element for the supplied locator.
    *
    * @param locator the locator for the purge-key element, assumed not
    *    <code>null</code>.
    * @return the new purge key element, never <code>null</code>.
    */
   private Element createPurgeKeyElement(Document doc, PSLocator locator)
   {
      Element purgeKeyEl = doc.createElement(PURGEKEY_NODE);

      purgeKeyEl.setAttribute(XML_ATTR_CONTENT_ID,
         Integer.toString(locator.getId()));

      return purgeKeyEl;
   }

   /**
    * The requester used to communicate with the webservices handler on the
    * remote server. Initialized by the constructor, never <code>null</code> or
    * modified after that.
    */
   private PSRemoteWsRequester m_requester;

   /**
    * The community id as an optional parameter for the connection. It may be
    * <code>null</code> if the optional is not set.
    */
   protected String m_community = null;

   /**
    * The locale as an optional parameter for the connection. It may be
    * <code>null</code> if the optional is not set.
    */
   protected String m_locale = null;

   // Various WSDL port as a way to group the actions or operations on the
   // webservices handler of the remote Rhythmyx Server.
   private final static String WS_DESIGN = "Design";
   private final static String WS_CONTENTDATA = "ContentData";
   private final static String WS_WORKFLOW = "Workflow";
   private final static String WS_MISC = "Miscellaneous";
   private final static String WS_FOLDER = "Folder";

   /**
    * private XML attribute and its values
    */
   private static final String XML_NODE_RESPONSE = "ResultResponse";

   private static final String OPENITEM_ACTION = "openItem";
   private static final String OPENITEM_REQUEST = "OpenItemRequest";
   private static final String OPENITEM_RESPONSE = "OpenItemResponse";

   private static final String NEWCOPY_ACTION = "newCopy";
   private static final String NEWCOPY_REQUEST = "NewCopyRequest";
   private static final String NEWCOPY_RESPONSE = "NewCopyResponse";


   private static final String NEWITEM_ACTION = "newItem";
   private static final String NEWITEM_REQUEST = "newItemRequest";
   private static final String NEWITEM_RESPONSE = "newItemResponse";

   private static final String UPDATEITEM_ACTION = "updateItem";
   private static final String UPDATEITEM_REQUEST = "UpdateItemRequest";
   private static final String UPDATEITEM_RESPONSE = "UpdateItemResponse";

   private static final String CONTENTTYPE_ACTION = "contentType";
   private static final String CONTENTTYPE_REQUEST = "ContentTypeRequest";
   private static final String CONTENTTYPE_RESPONSE = "ContentTypeResponse";

   private static final String CHECKOUT_ACTION = "checkOut";
   private static final String CHECKOUT_REQUEST = "CheckOutRequest";
   private static final String CHECKOUT_RESPONSE = "CheckOutResponse";

   private static final String CHECKIN_ACTION = "checkIn";
   private static final String CHECKIN_REQUEST = "CheckInRequest";
   private static final String CHECKIN_RESPONSE = "CheckInResponse";

   private static final String TRANSITEM_ACTION = "transitionItem";
   private static final String TRANSITEM_REQUEST = "TransitionItemRequest";
   private static final String TRANSITEM_RESPONSE = "TransitionItemResponse";

   private static final String LOGIN_ACTION = "login";
   private static final String LOGIN_REQUEST = "LoginRequest";
   private static final String LOGIN_RESPONSE = "LoginResponse";

   private static final String PURGEITEMS_ACTION = "purgeItems";
   private static final String PURGEITEMS_REQUEST = "PurgeItemsRequest";
   private static final String PURGEITEMS_RESPONSE = "PurgeItemsResponse";

   private static final String XML_NODE_CONTENTTYPE_ID = "ContentTypeNameId";

   private static final String CONTENTKEY_NODE = "ContentKey";
   private static final String PURGEKEY_NODE = "PurgeKey";
   private static final String XML_TRUE = "true";
   private static final String XML_FALSE = "false";
   private static final String XML_SUCCESS = "success";
   private static final String XML_ATTR_CONTENT_ID = "contentId";
   private static final String XML_ATTR_REVISION = "revision";
   private static final String XML_ATTR_TYPE = "type";

   private static final String EL_INCLUDE_CHILDREN = "IncludeChildren";
   private static final String EL_INCLUDE_RELATED = "IncludeRelated";
   private static final String EL_INCLUDE_BINARY = "IncludeBinary";
   private static final String EL_CHECK_OUT = "CheckOut";
   private static final String EL_CHECK_IN = "CheckIn";
   private static final String EL_CONTENTTYPE = "ContentType";

   private static final String EL_TRANSITIONITEM_ID = "TransitionId";
   private static final String EL_TRANSITIONID = "Transitionid";
   private static final String EL_HISTORY = "History";
   private static final String EL_COMMENT = "Comment";

}

