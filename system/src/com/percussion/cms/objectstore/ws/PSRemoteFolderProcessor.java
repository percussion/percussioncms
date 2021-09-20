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
package com.percussion.cms.objectstore.ws;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSComponentProcessor;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.IPSFolderProcessor;
import com.percussion.cms.objectstore.IPSKeyGenerator;
import com.percussion.cms.objectstore.IPSRelationshipProcessor;
import com.percussion.cms.objectstore.PSCloneSiteFolderRequest;
import com.percussion.cms.objectstore.PSCloningOptions;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderProcessorProxy;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSProcessingStatistics;
import com.percussion.cms.objectstore.PSProcessorCommon;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.PSSaveResults;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.Validate.notNull;

/**
 * This class handles the folder operations on the client side. This is
 * typically used for an applet client to communicate to the Rhythmyx server.
 */
public class PSRemoteFolderProcessor extends PSProcessorCommon
   implements IPSRelationshipProcessor, IPSFolderProcessor
{
   /**
    * Construct a folder processor from a remote agent object.
    *
    * @param rmAgent The remote agent object, it may not be
    *    <code>null</code>.
    */
   public PSRemoteFolderProcessor(PSRemoteFolderAgent rmAgent)
   {
      if (rmAgent == null)
         throw new IllegalArgumentException("wsAgent may not be null");

      m_rmAgent = rmAgent;
   }

   /**
    * Construct a folder processor from a remote requester.
    *
    * @param rmRequester The remote requester object, it may not be
    *    <code>null</code>.
    */
   public PSRemoteFolderProcessor(IPSRemoteRequester rmRequester)
   {
      this(new PSRemoteFolderAgent(rmRequester));
   }

   /**
    * Construct an instance with context and config objects. This ctor is
    * expected by the Proxy.
    *
    * @param ctx The context object for the folder processor, may not be
    *    <code>null</code>.
    *
    * @param procConfig The config properties for this object, may be
    *    <code>null</code> if not exists.
    */
   public PSRemoteFolderProcessor(PSRemoteFolderAgent ctx, Map procConfig)
   {
      this(ctx);
      
      // avoid eclipse warnings
      if (procConfig == null);
   }

   /**
    * Construct an instance with requester and config objects. This ctor is
    * expected by the Proxy.
    *
    * @param req The requester object for the folder processor, may not be
    *    <code>null</code>.
    *
    * @param procConfig The config properties for this object, may be
    *    <code>null</code> if not exists.
    */
   public PSRemoteFolderProcessor(IPSRemoteRequester req, Map procConfig)
   {
      this(req);
      
      // avoid eclipse warnings
      if (procConfig == null);
   }

   /**
    * Default constructor. This is only needed for the derived class
    * <code>PSWsFolderProcessor</code>.
    */
   protected PSRemoteFolderProcessor()
   {
   }

   //see IPSFolderProcessor interface
   @Override
   public String[] getFolderPaths(PSLocator objectId)
      throws PSCmsException
   {
      return getFolderPaths(objectId, PSRelationshipConfig.TYPE_FOLDER_CONTENT);
   }

   @Override
   public String[] getFolderPaths(PSLocator objectId, String relationshipTypeName) throws PSCmsException {
      if ( null == objectId)
      {
         throw new IllegalArgumentException("object id cannot be null");
      }
      return getRelationshipOwnerPaths(PSDbComponent.getComponentType(PSFolder.class),
              objectId, relationshipTypeName);
   }

   //see IPSFolderProcessor interface
   public PSComponentSummary[] getParentSummaries(PSLocator objectId)
         throws PSCmsException
   {
      return getParents(PSDbComponent.getComponentType(PSFolder.class), 
            PSRelationshipConfig.TYPE_FOLDER_CONTENT, objectId);
   }
   
   //see IPSFolderProcessor interface
   public void addChildren(List children, PSLocator targetFolderId)
      throws PSCmsException
   {
      add(PSRelationshipConfig.TYPE_FOLDER_CONTENT, children, targetFolderId);
   }
   
   //see IPSFolderProcessor interface
   public PSComponentSummary[] getChildSummaries(PSLocator folderId) 
      throws PSCmsException
   {
      return getChildren(PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE,
            folderId);
   }

   //see IPSFolderProcessor interface
   public void copyChildren(List children, PSLocator targetFolderId) throws PSCmsException
   {
      copy(PSRelationshipConfig.TYPE_FOLDER_CONTENT, children, targetFolderId);
   }

   public void removeChildren(PSLocator sourceFolderId, List children, boolean force) throws PSCmsException
   {
      if (children == null)
         throw new IllegalArgumentException("children cannot be empty");

      // build the request body for add folder children. It is the
      // <code>RemoveFolderChildrenRequest</code> that is specified in
      // sys_FolderParameters.xsd
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element params = doc.createElement(REMOVE_FOLDERCHILDREN_REQUEST);

      PSXmlDocumentBuilder.addElement(
         doc,
         params,
         PARENT_ID_EL,
         sourceFolderId.getPart(PSLocator.KEY_ID));
      params.appendChild(getChildIdsElement(children, doc));

      PSXmlDocumentBuilder.addElement(
         doc,
         params,
         FORCE,
         force?"yes":"no");

      // send the prepared request parameters to the server, get the response
      Element result =
         sendMessage(
            REMOVE_FOLDERCHILDREN_OPERATION,
            params,
            REMOVE_FOLDERCHILDREN_RESPONSE);

      checkResultResponse(REMOVE_FOLDERCHILDREN_OPERATION, result);
   }

   //see IPSFolderProcessor interface
   public void removeChildren(PSLocator sourceFolderId, List children) throws PSCmsException
   {
      delete((PSKey)sourceFolderId, children);
   }

   // see IPSFolderProcessor interface
   public void moveChildren(PSLocator sourceFolderId, List children,
      PSLocator targetFolderId, boolean force) throws PSCmsException
   {
      if (children == null)
         throw new IllegalArgumentException("children cannot be null");
      else if (children.size() == 0)
         return; // nothing to move from

      // build the request body for add folder children. It is the
      // <code>MoveFolderChildrenRequest</code> that is specified in
      // sys_FolderParameters.xsd
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element params = doc.createElement(MOVE_FOLDERCHILDREN_REQUEST);

      PSXmlDocumentBuilder.addElement(doc, params, SOURCE_PARENT_ID_EL,
         sourceFolderId.getPart(PSLocator.KEY_ID));
      params.appendChild(getChildIdsElement(children, doc));
      PSXmlDocumentBuilder.addElement(doc, params, TARGET_PARENT_ID_EL,
         targetFolderId.getPart(PSLocator.KEY_ID));
      PSXmlDocumentBuilder.addElement(doc, params, FORCE, force ? "yes" : "no");
      
      // send the prepared request parameters to the server, get the response
      Element result = sendMessage(MOVE_FOLDERCHILDREN_OPERATION, params,
         MOVE_FOLDERCHILDREN_RESPONSE);

      checkResultResponse(MOVE_FOLDERCHILDREN_OPERATION, result);
   }

   // see IPSFolderProcessor interface
   public void moveChildren(PSLocator sourceFolderId, List children, PSLocator targetFolderId) throws PSCmsException
   {
      move(PSRelationshipConfig.TYPE_FOLDER_CONTENT, sourceFolderId, children, 
            targetFolderId);
   }

   //see IPSFolderProcessor interface
   public PSComponentSummary getSummary(String path) throws PSCmsException
   {
      return getSummaryByPath(PSDbComponent.getComponentType(PSFolder.class), 
            path, PSRelationshipConfig.TYPE_FOLDER_CONTENT);
   }

   //see IPSFolderProcessor interface
   public PSLocator[] getDescendentFolderLocators(PSLocator folderId)
         throws PSCmsException
   {
      PSKey[] keys = getDescendentsLocators(PSDbComponent.getComponentType(PSFolder.class), 
            PSRelationshipConfig.TYPE_FOLDER_CONTENT, folderId);
      PSLocator[] results = new PSLocator[keys.length];
      System.arraycopy(keys, 0, results, 0, keys.length);
      return results;
   }
   
   //see IPSFolderProcess interface
   @SuppressWarnings("unchecked")
   public PSLocator[] getDescendentFolderLocatorsWithoutFilter(
         PSLocator folderId)
         throws PSCmsException
      {
          Element msg =
            getSingleFolderIdMsg(
               GET_DESCENDENTSLOCATORS_WITHOUTFILTER_REQUEST, PARENT_ID_EL, folderId);

         Element data =
            sendMessage(
               GET_DESCENDENTSLOCATORS_WITHOUTFILTER_OPERATION,
               msg,
               GET_DESCENDENTSLOCATORS_WITHOUTFILTER_RESPONSE);

         List locators = new ArrayList();
         NodeList nl = data.getElementsByTagName("PSXLocator");
         int len = 0;
         if(nl == null || (len = nl.getLength()) == 0)
            return new PSLocator[]{};
         
         try
         {
            for(int i = 0; i < len; i++)
               locators.add(new PSLocator((Element)nl.item(i)));
         }
         catch (PSUnknownNodeTypeException e)
         {
            throw new PSCmsException(e);
         }           

         return (PSLocator[])locators.toArray(new PSLocator[locators.size()]);
      }

   /**
    * See {@link IPSRelationshipProcessor#add(String, String, List, PSKey)
    * interface}
    * @throws UnsupportedOperationException if the relationshipType specified 
    * is not of type {@link PSRelationshipConfig#TYPE_FOLDER_CONTENT}
    */
   public void add(
      String componentType,
      String relationshipType,
      List children,
      PSKey targetParent)
      throws PSCmsException
   {
      if (!relationshipType.equals(PSRelationshipConfig.TYPE_FOLDER_CONTENT))
         throw new UnsupportedOperationException(
            "relationshipType must be "
               + PSRelationshipConfig.TYPE_FOLDER_CONTENT);

      add(componentType, children, targetParent);
   }

   /**
    * See {@link IPSComponentProcessor#load(String, PSKey[])}
    */
   public Element[] load(String componentType, PSKey[] locators)
      throws PSCmsException
   {
      // TODO: enhance web services, to handle bulk operation
      Element[] resultData = new Element[locators.length];

      for (int i = 0; i < locators.length; i++)
      {
         Element msg =
            getSingleFolderIdMsg(
               OPEN_FOLDER_REQUEST,
               FOLDER_ID_EL,
               locators[i]);

         Element response =
            sendMessage(OPEN_FOLDER_OPERATION, msg, OPEN_FOLDER_RESPONSE);

         Element child = PSXMLDomUtil.getFirstElementChild(response);
         String name = PSXMLDomUtil.getUnqualifiedNodeName(child);

         if (name.equals(RESULT_RESPONSE_EL))
         {
            checkResultResponse(OPEN_FOLDER_OPERATION, response);
            resultData[i] = null;
         }
         else
         {
            resultData[i] = child;
         }
      }
      return resultData;
   }
   /**
    * Inserts or updates a list of folder objects.
    * See {@link IPSComponentProcessor#save(IPSDbComponent[])}
    */
   public PSSaveResults save(IPSDbComponent[] components)
      throws PSCmsException
   {
      // TODO: enhance web services, to handle bulk operation
      IPSDbComponent[] comps = new IPSDbComponent[components.length];

      int inserts = 0;
      int updates = 0;

      for (int i = 0; i < comps.length; i++)
      {
         if (components[i].getLocator().isPersisted())
            updates++;
         else
            inserts++;

         comps[i] = save(components[i]);
      }

      PSProcessingStatistics statistics =
         new PSProcessingStatistics(inserts, updates, 0, 0, 0);

      return new PSSaveResults(comps, statistics);
   }

   /**
    * This is the same as {@link #save(IPSDbComponent[])}, except it is dealing
    * with one folder component at a time.
    */
   private IPSDbComponent save(IPSDbComponent component) throws PSCmsException
   {
      if (!(component instanceof PSFolder))
         throw new IllegalArgumentException("component must be PSFolder instance");

      PSFolder folder = (PSFolder) component;
      String saveOperation, saveRequest, saveResponse;
      if (folder.getLocator().isPersisted())
      {
         saveOperation = UPDATE_FOLDER_OPERATION;
         saveRequest = UPDATE_FOLDER_REQUEST;
         saveResponse = UPDATE_FOLDER_RESPONSE;
      }
      else
      {
         saveOperation = CREATE_FOLDER_OPERATION;
         saveRequest = CREATE_FOLDER_REQUEST;
         saveResponse = CREATE_FOLDER_RESPONSE;
      }

      Element msg = getSaveFolderMsg(saveRequest, folder);
      Element data = sendMessage(saveOperation, msg, saveResponse);

      try
      {
         folder = new PSFolder(PSXMLDomUtil.getFirstElementChild(data));
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }

      return folder;
   }

   /**
    * See {@link IPSComponentProcessor#delete(String, PSKey[])} for detail info
    */
   public int delete(String componentType, PSKey[] locators)
      throws PSCmsException
   {
      // TODO: enhance web services, to handle bulk operation
      for (int i = 0; i < locators.length; i++)
      {
         Element msg =
            getSingleFolderIdMsg(
               DELETE_FOLDER_REQUEST,
               FOLDER_ID_EL,
               locators[i]);
         Element result =
            sendMessage(DELETE_FOLDER_OPERATION, msg, DELETE_FOLDER_RESPONSE);

         checkResultResponse(DELETE_FOLDER_OPERATION, result);
      }
      return locators.length;
   }

   /**
    * See {@link IPSComponentProcessor#delete(IPSDbComponent[])}
    */
   public int delete(IPSDbComponent[] comps) throws PSCmsException
   {
      int deleted = 0;
      for (int i = 0; i < comps.length; i++)
         deleted += delete(comps[i]);

      return deleted;
   }

   /**
    * See {@link IPSComponentProcessor#delete(IPSDbComponent)}
    */
   public int delete(IPSDbComponent comp) throws PSCmsException
   {
      return delete(comp.getComponentType(), new PSKey[] { comp.getLocator()});
   }

   /**
    * See {@link IPSRelationshipProcessor#add(String, PSKey, PSKey [])}
    */
   public void add(String relationshipType, List children, PSKey targetParent)
      throws PSCmsException
   {
      if (children == null)
         throw new IllegalArgumentException("children cannot be null");
      
      // avoid eclipse warnings
      if (relationshipType == null);

      // build the request body for add folder children. It is the
      // <code>AddFolderChildrenRequest</code> element that is specified in
      // sys_FolderParameters.xsd
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element params = doc.createElement(ADD_FOLDERCHILDREN_REQUEST);

      PSXmlDocumentBuilder.addElement(
         doc,
         params,
         TARGET_PARENT_ID_EL,
         targetParent.getPart(PSLocator.KEY_ID));
      params.appendChild(getChildIdsElement(children, doc));

      // send the prepared request parameters to the server, get the response
      Element result =
         sendMessage(
            ADD_FOLDERCHILDREN_OPERATION,
            params,
            ADD_FOLDERCHILDREN_RESPONSE);

      checkResultResponse(ADD_FOLDERCHILDREN_OPERATION, result);
   }


   /**
    * See {@link IPSRelationshipProcessor#move(String, PSKey, PSKey [], PSKey)}
    */
   public void move(
      String relationshipType,
      PSKey sourceParent,
      List children,
      PSKey targetParent)
      throws PSCmsException
   {
      moveChildren((PSLocator) sourceParent, children,
         (PSLocator) targetParent, false);
   }

   /**
    * See {@link IPSRelationshipProcessor#copy(String, PSKey, PSKey [])}
    * 
    * @deprecated Use 
    * {@link PSFolderProcessorProxy#copyChildren(List, PSLocator)}.
    */
   public void copy(String relationshipType, List children, PSKey parent)
      throws PSCmsException
   {
      if (children == null)
         throw new IllegalArgumentException("children cannot be null");
      else if (children.size() == 0)
         return; // nothing to copy from

      // build the request body for add folder children. It is the
      // <code>CopyFolderChildrenRequest</code> that is specified in
      // sys_FolderParameters.xsd
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element params = doc.createElement(COPY_FOLDERCHILDREN_REQUEST);

      PSXmlDocumentBuilder.addElement(
         doc,
         params,
         TARGET_PARENT_ID_EL,
         parent.getPart(PSLocator.KEY_ID));
      params.appendChild(getChildIdsElement(children, doc));
      PSXmlDocumentBuilder.addElement(doc, params, RECURSIVE_EL, XML_TRUE);

      // send the prepared request parameters to the server, get the response
      Element result =
         sendMessage(
            COPY_FOLDERCHILDREN_OPERATION,
            params,
            COPY_FOLDERCHILDREN_RESPONSE);

      checkResultResponse(COPY_FOLDERCHILDREN_OPERATION, result);
   }

   /**
    * See {@link IPSRelationshipProcessor#delete(String, PSKey, List)}
    * 
    * @deprecated Use 
    * {@link PSFolderProcessorProxy#removeChildren(PSLocator, List)}.
    */
   public void delete(String relationshipType, PSKey parent, List children)
      throws PSCmsException
   {
      removeChildren((PSLocator)parent, children, false);
   }

   /**
    * See {@link IPSRelationshipProcessor#getChildren(String, PSKey)}
    * 
    * @deprecated Use 
    * {@link PSFolderProcessorProxy#getChildSummaries(PSLocator)}.
    */
   public PSComponentSummary[] getChildren(String type, PSKey parent)
      throws PSCmsException
   {
      Element msg =
         getSingleFolderIdMsg(
            GET_FOLDERCHILDREN_REQUEST,
            FOLDER_ID_EL,
            parent);

      Element data =
         sendMessage(
            GET_FOLDERCHILDREN_OPERATION,
            msg,
            GET_FOLDERCHILDREN_RESPONSE);

      Element summariesEl = PSXMLDomUtil.getFirstElementChild(data);

      PSComponentSummaries summaries = null;
      try
      {
         summaries = new PSComponentSummaries(summariesEl);
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }

      return summaries.toArray();
   }

   /**
    * Send a message to the remote server
    *
    * @param operation The operation or action of the message. Assume not
    *    <code>null</code> or empty.
    *
    * @param message The message or parameter for the operation. Assume not
    *    <code>null</code> or empty.
    *
    * @param respNodeName The expected node name of the response from the
    *    remote server.
    *
    * @return The response from the remote server, never <code>null</code>.
    */
   protected Element sendMessage(
      String operation,
      Element message,
      String respNodeName)
      throws PSCmsException
   {
      return m_rmAgent.sendMessage(operation, message, respNodeName);
   }

   /**
    * Creates a XML representation for a list of child ids.
    *
    * @param childIds The child ids. Assume it is not <code>null</code> or empty.
    *
    * @param doc The document that is used to create the XML element, assume
    *    not <code>null</code>.
    *
    * @return The created XML element, never <code>null</code>.
    */
   private Element getChildIdsElement(List childIds, Document doc)
   {
      Element childIdsEl = doc.createElement(CHILD_IDS_EL);
      for (int i = 0; i < childIds.size(); i++)
      {
         PSKey key = (PSKey) childIds.get(i);
         Element node = PSXmlDocumentBuilder.addElement(
            doc,
            childIdsEl,
            CHILD_ID_EL,
            key.getPart(PSLocator.KEY_ID));
         if (key instanceof PSLocatorWithName)
         {
            String name = ((PSLocatorWithName)key).getOverrideName();
            node.setAttribute(PSLocatorWithName.ATTR_OVERRIDE_NAME, name);
         }
      }

      return childIdsEl;
   }

   /**
    * Creates a message which contains a request with a folder id in it.
    *
    * @param requestEl The XML element name of the request. Assume not
    *    <code>null</code> or empty.
    *
    * @param idElName The name of the element for the id of the locator.
    *    Assume not <code>null</code> or empty.
    *
    * @param locator The locator that contains the folder id for the request.
    *    Assume not <code>null</code>.
    *
    * @return The created soap body. Its format is specified in
    *    sys_FolderParameters.xsd according to the <code>requestEl</code>.
    *    Never <code>null</code>.
    */
   private Element getSingleFolderIdMsg(
      String requestEl,
      String idElName,
      PSKey locator)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = doc.createElement(requestEl);

      Element folderIdEl =
         PSXmlDocumentBuilder.addElement(
            doc,
            root,
            idElName,
            locator.getPart(PSLocator.KEY_ID));

      root.appendChild(folderIdEl);

      return root;
   }
   
   /**
    * Creates a message which contains a request with a source and target
    * folder id in it.
    *
    * @param requestEl The XML element name of the request. Assume not
    *    <code>null</code> or empty.
    *
    * @param srcIdElName The name of the element for the id of the source
    *    locator.  Assume not <code>null</code> or empty.
    *
    * @param tgtIdElName The name of the element for the id of the target
    *    locator.  Assume not <code>null</code> or empty.
    *    
    * @param srcLocator The locator that contains the source folder id for
    *    the request.  Assume not <code>null</code>.
    *    
    * @param tgtLocator The locator that contains the target folder id for
    *    the request.  Assume not <code>null</code>.
    *
    * @return The created soap body.  Never <code>null</code>.
    */
   private Element getSourceTargetFolderIdMsg(
      String requestEl,
      String srcIdElName,
      String tgtIdElName,
      PSKey srcLocator,
      PSKey tgtLocator)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = doc.createElement(requestEl);

      Element srcFolderIdEl =
         PSXmlDocumentBuilder.addElement(
            doc,
            root,
            srcIdElName,
            srcLocator.getPart(PSLocator.KEY_ID));
      
      root.appendChild(srcFolderIdEl);
      
      Element tgtFolderIdEl =
         PSXmlDocumentBuilder.addElement(
            doc,
            root,
            tgtIdElName,
            tgtLocator.getPart(PSLocator.KEY_ID));

      root.appendChild(tgtFolderIdEl);

      return root;
   }

   /**
    * Creates the soap envelope body for create or update folder request.
    *
    * @param saveRequest The save request, assume it is either <code>
    *    CREATE_FOLDER_REQUEST</code> or <code>UPDATE_FOLDER_REQUEST</code>.
    *
    * @param folder The folder object for the soap request, assume not
    *    <code>null</code>
    *
    * @return The created soap body for create or update a folder request.
    *    The XML format is specified in sys_FolderParameters.xsd. Never
    *    <code>null</code>.
    */
   private Element getSaveFolderMsg(String saveRequest, PSFolder folder)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element params = doc.createElement(saveRequest);
      params.appendChild(folder.toXml(doc));

      return params;
   }

   /**
    * Check the result response element.
    *
    * @param operation The current operation of the caller. Assume not
    *    <code>null</code> or empty.
    *
    * @param rootEl The root element of the response, result response must be
    *    the first child element. Assume not <code>null</code>.
    *
    * @throws PSCmsException if the result is not successful or other error
    *    occurs.
    */
   private void checkResultResponse(String operation, Element rootEl)
      throws PSCmsException
   {
      String result;
      Element resultEl = PSXMLDomUtil.getFirstElementChild(rootEl);

      try
      {
         PSXMLDomUtil.checkNode(resultEl, RESULT_RESPONSE_EL);
         result = PSXMLDomUtil.checkAttribute(resultEl, "type", true);
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }

      if (!result.equals("success"))
      {
         Element resultNode = PSXMLDomUtil.getFirstElementChild(resultEl);
         String resultMsg = null;
         if (resultNode == null)
         {
            resultMsg = PSXmlDocumentBuilder.toString(resultEl);
            throw new PSCmsException(
               IPSCmsErrors.FOLDER_OPERATION_FAILED,
               operation);
         }
         else
         {
            resultMsg = PSXMLDomUtil.getElementData(resultNode);
            throw new PSCmsException(IPSCmsErrors.FOLDER_ERROR_MSG, resultMsg);
         }
      }
   }

   /**
    * See {@link IPSComponentProcessor#reorder(int, List)}
    */
   public void reorder(int firstIndex, List comps) throws PSCmsException
   {
      // avoid eclipse warnings
      if (firstIndex == 0);
      if (comps == null);

      throw new IllegalStateException("Not support reorder(int, list)");
   }

   /**
    * See {@link IPSKeyGenerator#allocateIds(String, int)} for detail.
    * This is not supported and not needed in this processor.
    */
   public int[] allocateIds(String lookup, int count) throws PSCmsException
   {
      throw new IllegalStateException("allocateIds(String, int) not supported");
   }

   /**
    * See {@link IPSKeyGenerator#setNextAllocationSize(int)} for detail.
    * This is not supported and not needed in this processor.
    */
   public void setNextAllocationSize(int count)
   {
      throw new IllegalStateException("setNextAllocationSize(int) not supported");
   }

   /**
    * See {@link IPSKeyGenerator#allocateId(String)} for detail.
    * This is not supported and not needed in this processor.
    */
   public int allocateId(String lookup) throws PSCmsException
   {
      throw new IllegalStateException("allocateId(String) not supported");
   }

   /**
    * See {@link IPSRelationshipProcessor#getChildren(String, String, PSKey)
    * interface}
    *
    * @relationshipType The relationship type. It must be the folder
    *    relationship.
    */
   public PSComponentSummary[] getChildren(
      String type,
      String relationshipType,
      PSKey parent)
      throws PSCmsException
   {
      if (!relationshipType.equalsIgnoreCase(PSRelationshipConfig.TYPE_FOLDER_CONTENT))
      {
         throw new IllegalStateException("relationshipType must be \""
               + PSRelationshipConfig.TYPE_FOLDER_CONTENT + "\"");
      }

      return getChildren(type, parent);
   }

   /**
    * See {@link IPSRelationshipProcessor#getParent(String, String, PSKey)
    * interface}
    */
   public PSComponentSummary[] getParents(
      String type,
      String relationshipType,
      PSKey locator)
      throws PSCmsException
   {
      Element msg =
         getSingleFolderIdMsg(GET_PARENTFOLDER_REQUEST, CHILD_ID_EL, locator);

      Element data =
         sendMessage(
            GET_PARENTFOLDER_OPERATION,
            msg,
            GET_PARENTFOLDER_RESPONSE);

      Element summariesEl = PSXMLDomUtil.getFirstElementChild(data);

      PSComponentSummaries summaries = null;
      try
      {
         summaries = new PSComponentSummaries(summariesEl);
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }

      return summaries.toArray();
   }

   /**
    * see IPSRelationshipProcessor#getSummaryByPath(String, String, String)
    * 
    * @deprecated Use {@link PSFolderProcessorProxy#getSummary(String)}.
    */ 
   public PSComponentSummary getSummaryByPath(
      String componentType,
      String path, 
      String relationshipTypeName) 
      throws PSCmsException 
   {
      // create request document
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = doc.createElement(GET_SUMMARYBYPATH_REQUEST);
      Element pathEl =
         PSXmlDocumentBuilder.addElement(
            doc,
            root,
            PATH_EL,
            path);
      root.appendChild(pathEl);

      // send the request
      Element data =
         sendMessage(
            GET_SUMMARYBYPATH_OPERATION,
            root,
            GET_SUMMARYBYPATH_RESPONSE);

      // get the result from the response
      Element summaryEl = PSXMLDomUtil.getFirstElementChild(data);

      try
      {
         PSComponentSummary summary = null;
         if (! summaryEl.getNodeName().equals(NULL_RESULT))
            summary = new PSComponentSummary(summaryEl);
         return summary;
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
   }

   /**
    * @see @see #delete(String, PSKey, List)
    */
   public void delete(PSKey sourceParent, List children) throws PSCmsException
   {
      delete(PSRelationshipConfig.TYPE_FOLDER_CONTENT, sourceParent, children);
   }

   /**
    * See {@link IPSRelationshipProcessor#add(String, List, PSLocator) 
    * interface}
    * 
    * @deprecated Use 
    * {@link PSFolderProcessorProxy#addChildren(List, PSLocator)}.
    */
   public void add(String relationshipType, List children, PSLocator 
      targetParent) 
      throws PSCmsException
   {
      add(relationshipType, children, (PSKey)targetParent);
   }

   /**
    * See {@link IPSRelationshipProcessor#getRelationships(String, PSLocator, 
    * boolean) interface}
    *
    * @throws UnsupportedOperationException Not implemented..
    */
   public PSRelationshipSet getRelationships(String relationshipType, 
      PSLocator locator, boolean owner) 
      throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "getRelationships() is not implemented in : "
            + this.getClass().getName());
   }

   /**
    * See {@link IPSRelationshipProcessor#move(String, PSLocator, List, 
    * PSLocator) interface}
    * 
    * @deprecated Use 
    * {@link PSFolderProcessorProxy#moveChildren(PSLocator, List, PSLocator)}.
    */
   public void move(String relationshipType, PSLocator sourceParent, 
      List children, PSLocator targetParent) 
      throws PSCmsException
   {
      move(relationshipType, (PSKey)sourceParent, children, (PSKey)targetParent);
   }

   /**
    * See {@link IPSRelationshipProcessor#getRelationships(PSRelationshipFilter) 
    * interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public PSRelationshipSet getRelationships(PSRelationshipFilter filter) 
      throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "getRelationships() is not implemented in : " + this.getClass().getName());
   }

   /**
    * See {@link IPSRelationshipProcessor#getSummaries(PSRelationshipFilter, 
    * boolean) interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public PSComponentSummaries getSummaries(PSRelationshipFilter filter, 
      boolean owner) throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "getSummaries() is not implemented in : " + this.getClass().getName());
   }

   /**
    * See {@link IPSRelationshipProcessor#save(PSRelationshipSet) interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public void save(PSRelationshipSet relationships) throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "save() is not implemented in : " + this.getClass().getName());
   }

   /**
    * See {@link IPSRelationshipProcessor#delete(PSRelationshipSet) interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public void delete(PSRelationshipSet relationships)
   {
      throw new UnsupportedOperationException(
         "delete() is not implemented in : " + this.getClass().getName());
   }

   /**
    * See {@link IPSRelationshipProcessor#getConfig(String) interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public PSRelationshipConfig getConfig(String relationshipTypeName) throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "getConfig() is not implemented in : " + this.getClass().getName());
   }

   /**
    * See {@link IPSRelationshipProcessor#getRelationshipOwnerPaths(String, 
    * PSLocator, String) interface}
    *
    * @throws PSCmsException If the request cannot be fulfilled.
    * 
    * @deprecated Use {@link PSFolderProcessorProxy#getFolderPaths(PSLocator)}.
    */
   public String[] getRelationshipOwnerPaths(
      String componentType,
      PSLocator locator,
      String relationshipTypeName)
      throws PSCmsException
   {
      if ( null == componentType)
      {
         throw new IllegalArgumentException("componentType cannot be null");  
      }
      if ( null == locator)
      {
         throw new IllegalArgumentException("locator cannot be null");  
      }
      if ( null == relationshipTypeName)
      {
         throw 
            new IllegalArgumentException("relationshipTypeName cannot be null");  
      }

      // build the request body for add folder children. It is the
      // <code>GetFolderPathsRequest</code>. This has not been exposed in the
      // wsdl
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element params = doc.createElement(GET_FOLDER_PATH_REQUEST);

      PSXmlDocumentBuilder.addElement(
            doc,
            params,
            CHILD_ID_EL,
            locator.getPart(PSLocator.KEY_ID));

      // send the prepared request parameters to the server, get the response
      Element response =
         sendMessage(
            GET_FOLDER_PATH_OPERATION,
            params,
            GET_FOLDER_PATH_RESPONSE);

      Element paths = PSXMLDomUtil.getFirstElementChild(response);
      String name = PSXMLDomUtil.getUnqualifiedNodeName(paths);

      String[] resultData;
      if (name.equals(RESULT_RESPONSE_EL))
      {
         checkResultResponse(GET_FOLDER_PATH_OPERATION, response);
         resultData = new String[0];
      }
      else if (name.equals(FOLDER_PATHS_EL))
      {
         try {
            Element pathEl = null;
            List pathResults = new ArrayList();
            pathEl = PSXMLDomUtil.getFirstElementChild(paths, PATH_EL);
            while (pathEl != null)
            {
               pathResults.add(PSXMLDomUtil.getElementData(pathEl));
               pathEl = PSXMLDomUtil.getNextElementSibling(pathEl, PATH_EL);
            }
            
            resultData = new String[pathResults.size()];
            pathResults.toArray(resultData);
         } 
         catch (PSUnknownNodeTypeException e) 
         {
            throw new PSCmsException(e);
         }
      }
      else
      {
         String[] args = 
         {
               "(" + FOLDER_PATHS_EL + " or " + RESULT_RESPONSE_EL + ")",
               PSXmlDocumentBuilder.toString(response)
         };
         throw new PSCmsException(IPSCmsErrors.RECEIVED_UNKNOWN_DATA, args);
      }
      return resultData;
   }
   
   /**
    * See {@link IPSRelationshipProcessor#isDescendent(String, PSLocator, 
    * PSLocator, String) interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public boolean isDescendent(
      String componentType,
      PSLocator parent,
      PSLocator child,
      String relationshipTypeName)
      throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "isDescendent() is not implemented in : "
            + this.getClass().getName());
   }
   
   /**
    * Return all descendent folders of the parent folder locator passed in.
    * 
    * @see {@link IPSRelationshipProcessor#getDescendentsLocators(String, String, 
    *      PSKey)}
    * 
    * @deprecated Use
    * {@link PSFolderProcessorProxy#getDescendentFolderLocators(PSLocator)}.
    */
   public PSKey[] getDescendentsLocators(
      String type,
      String relationshipType,
      PSKey parent)
      throws PSCmsException
   {
      
      Element msg =
         getSingleFolderIdMsg(
            GET_DESCENDENTSLOCATORS_REQUEST, PARENT_ID_EL, parent);

      Element data =
         sendMessage(
            GET_DESCENDENTSLOCATORS_OPERATION,
            msg,
            GET_DESCENDENTSLOCATORS_RESPONSE);

      List locators = new ArrayList();
      NodeList nl = data.getElementsByTagName("PSXLocator");
      int len = 0;
      if(nl == null || (len = nl.getLength()) == 0)
         return new PSKey[]{};
      
      try
      {
         for(int i = 0; i < len; i++)
            locators.add(new PSLocator((Element)nl.item(i)));
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSCmsException(e);
      }           

      return (PSKey[])locators.toArray(new PSKey[locators.size()]);
   }

   //see IPSFolderProcessor
   public String copyFolder(PSLocator source, PSLocator target,
      PSCloningOptions options) throws PSCmsException
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");
      
      if (target == null)
         throw new IllegalArgumentException("target cannot be null");
      
      if (options == null)
         throw new IllegalArgumentException("options cannot be null");
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSCloneSiteFolderRequest request = new PSCloneSiteFolderRequest(source, 
         target, options);

      Element data = sendMessage(CLONE_SITEFOLDER_OPERATION, request.toXml(doc),
         CLONE_SITEFOLDER_RESPONSE);

      Element results = PSXMLDomUtil.getFirstElementChild(data);
      String errorLog = results.getAttribute("errorLog");
      if (errorLog != null && errorLog.trim().length() > 0)
         return errorLog;
      
      return null;
   }
   
   //see IPSFolderProcessor
   public void copyFolderSecurity(PSLocator source, PSLocator target)
      throws PSCmsException
   {
      notNull(source);
      notNull(target);
    
      Element msg =
         getSourceTargetFolderIdMsg(
               COPY_FOLDERSECURITY_REQUEST,
               SOURCE_FOLDER_ID_EL,
               TARGET_FOLDER_ID_EL,
               source,
               target);
   
      Element result = sendMessage(
            COPY_FOLDERSECURITY_OPERATION,
            msg,
            COPY_FOLDERSECURITY_RESPONSE);

      checkResultResponse(COPY_FOLDERSECURITY_OPERATION, result);
   }
   
   //see IPSFolderProcessor
   public Set getFolderCommunities(PSLocator source) throws PSCmsException
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");
      
      Element msg = getSingleFolderIdMsg(GET_FOLDERCOMMUNITIES_REQUEST,
         FOLDER_ID_EL, source);

      Element data = sendMessage(GET_FOLDERCOMMUNITIES_OPERATION, msg,
         GET_FOLDERCOMMUNITIES_RESPONSE);

      Set communities = new HashSet();
      NodeList communityElements = PSXMLDomUtil.getFirstElementChild(data)
         .getElementsByTagName("Community");
      if (communityElements != null)
      {
         for (int i=0; i<communityElements.getLength(); i++)
         {
            Element communityElement = (Element) communityElements.item(i);
            communities.add(new Integer(communityElement.getAttribute("id")));
         }
      }

      return communities;
   }
   
   /**
    * Purge all items, if list contains a folder, all subfolder content will be purged also
    * @param items A list of items to purge
    */
   public void purgeFolderAndChildItems(List<PSLocator> items) throws PSCmsException
   {
       // build the request body for add folder children. It is the
       // <code>RemoveFolderChildrenRequest</code> that is specified in
       // sys_FolderParameters.xsd
       Document doc = PSXmlDocumentBuilder.createXmlDocument();
   
       Element params = doc.createElement(PURGE_FOLDERCHILDREN_REQUEST);
   
       PSXmlDocumentBuilder.addElement(doc, params, PARENT_ID_EL, "-1");
       params.appendChild(getChildIdsElement(items, doc));
   
       // send the prepared request parameters to the server, get the response
       Element result = sendMessage(PURGE_FOLDERCHILDREN_OPERATION, params, PURGE_FOLDERCHILDREN_RESPONSE);
   
       checkResultResponse(PURGE_FOLDERCHILDREN_OPERATION, result);
   
   }
   /**
    * Purge all items, if list contains a folder, all subfolder content will be purged also
    * @sourceFolderId,  the parent folder if avaliable of the items being purged.  this helps
    * in validation
    * @param items A list of items to purge
    */
   public void purgeFolderAndChildItems(PSLocator sourceFolderId, List<PSLocator> items) throws PSCmsException
   {
       if (items == null)
           throw new IllegalArgumentException("items cannot be empty");

       // build the request body for add folder children. It is the
       // <code>RemoveFolderChildrenRequest</code> that is specified in
       // sys_FolderParameters.xsd
       Document doc = PSXmlDocumentBuilder.createXmlDocument();

       Element params = doc.createElement(PURGE_FOLDERCHILDREN_REQUEST);

       PSXmlDocumentBuilder.addElement(doc, params, PARENT_ID_EL, sourceFolderId.getPart(PSLocator.KEY_ID));
       params.appendChild(getChildIdsElement(items, doc));

       // send the prepared request parameters to the server, get the response
       Element result = sendMessage(PURGE_FOLDERCHILDREN_OPERATION, params, PURGE_FOLDERCHILDREN_RESPONSE);

       checkResultResponse(PURGE_FOLDERCHILDREN_OPERATION, result);

   }

   public void purgeFolderNavigation(PSLocator folder) throws PSCmsException
   {
       if (folder == null)
           throw new IllegalArgumentException("items cannot be empty");

       // build the request body for add folder children. It is the
       // <code>RemoveFolderChildrenRequest</code> that is specified in
       // sys_FolderParameters.xsd
       Document doc = PSXmlDocumentBuilder.createXmlDocument();

       Element params = doc.createElement(PURGE_NAV_REQUEST);

       PSXmlDocumentBuilder.addElement(doc, params, PARENT_ID_EL, folder.getPart(PSLocator.KEY_ID));

       // send the prepared request parameters to the server, get the response
       Element result = sendMessage(PURGE_NAV_OPERATION, params, PURGE_NAV_RESPONSE);

       checkResultResponse(PURGE_NAV_OPERATION, result);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSFolderProcessor#getAncestorLocators(com.percussion.design.objectstore.PSLocator)
    */
   public List<PSLocator> getAncestorLocators(PSLocator folderId) 
      throws PSCmsException
   {
      throw new UnsupportedOperationException(
            "getAncestorLocators() is not implemented in : " + this.getClass().getName());      
   }

   /**
    * The misc XML node name
    */
   public static final String CHILD_ID_EL = "ChildId";
   public static final String CHILD_IDS_EL = "ChildIds";
   public static final String FOLDER_ID_EL = "FolderId";
   public static final String PARENT_ID_EL = "ParentId";
   public static final String TARGET_PARENT_ID_EL = "TargetParentId";
   public static final String SOURCE_PARENT_ID_EL = "SourceParentId";
   public static final String RESULT_RESPONSE_EL = "ResultResponse";
   public static final String RECURSIVE_EL = "Recursive";
   public static final String XML_TRUE = "true";
   public static final String XML_FALSE = "false";
   public static final String PATH_EL = "Path";
   public static final String NULL_RESULT = "NullResult";
   public static final String LOCATORS_EL = "Locators";
   public static final String FORCE = "force";
   public static final String SOURCE_FOLDER_ID_EL = "SourceFolderId";
   public static final String TARGET_FOLDER_ID_EL = "TargetFolderId";
      
   /**
    * The name of the container element for storing folder paths.
    */
   public static final String FOLDER_PATHS_EL = "FolderPaths";

   /**
    * The XML node name in groups on operation, request and response
    */
   public static final String CREATE_FOLDER_OPERATION = "createFolder";
   public static final String CREATE_FOLDER_REQUEST = "CreateFolderRequest";
   public static final String CREATE_FOLDER_RESPONSE = "CreateFolderResponse";

   public static final String OPEN_FOLDER_OPERATION = "openFolder";
   public static final String OPEN_FOLDER_REQUEST = "OpenFolderRequest";
   public static final String OPEN_FOLDER_RESPONSE = "OpenFolderResponse";

   public static final String DELETE_FOLDER_OPERATION = "deleteFolder";
   public static final String DELETE_FOLDER_REQUEST = "DeleteFolderRequest";
   public static final String DELETE_FOLDER_RESPONSE = "DeleteFolderResponse";

   public static final String GET_FOLDER_PATH_OPERATION = "getFolderPaths";
   public static final String GET_FOLDER_PATH_REQUEST = "GetFolderPathsRequest";
   public static final 
         String GET_FOLDER_PATH_RESPONSE = "GetFolderPathsResponse";

   public static final String PURGE_FOLDER_OPERATION = "purgeFolder";
   public static final String PURGE_FOLDER_REQUEST = "PurgeFolderRequest";
   public static final String PURGE_FOLDER_RESPONSE = "PurgeFolderResponse";
   

   public static final String PURGE_NAV_OPERATION = "purgeNav";
   public static final String PURGE_NAV_REQUEST = "PurgeNavRequest";
   public static final String PURGE_NAV_RESPONSE = "PurgeNavResponse";
   
   public static final String PURGE_FOLDERCHILDREN_OPERATION = "purgeFolderChildren";
   public static final String PURGE_FOLDERCHILDREN_REQUEST = "PurgeFolderChildrenRequest";
   public static final String PURGE_FOLDERCHILDREN_RESPONSE = "PurgeFolderChildrenResponse";

   public static final String UPDATE_FOLDER_OPERATION = "updateFolder";
   public static final String UPDATE_FOLDER_REQUEST = "UpdateFolderRequest";
   public static final String UPDATE_FOLDER_RESPONSE = "UpdateFolderResponse";

   public static final String ADD_FOLDERCHILDREN_OPERATION =
      "addFolderChildren";
   public static final String ADD_FOLDERCHILDREN_REQUEST =
      "AddFolderChildrenRequest";
   public static final String ADD_FOLDERCHILDREN_RESPONSE =
      "AddFolderChildrenResponse";

   public static final String COPY_FOLDERCHILDREN_OPERATION =
      "copyFolderChildren";
   public static final String COPY_FOLDERCHILDREN_REQUEST =
      "CopyFolderChildrenRequest";
   public static final String COPY_FOLDERCHILDREN_RESPONSE =
      "CopyFolderChildrenResponse";
   
   public static final String COPY_FOLDERSECURITY_OPERATION =
      "copyFolderSecurity";
   public static final String COPY_FOLDERSECURITY_REQUEST =
      "CopyFolderSecurityRequest";
   public static final String COPY_FOLDERSECURITY_RESPONSE =
      "CopyFolderSecurityResponse";

   public static final String GET_FOLDERCHILDREN_OPERATION =
      "getFolderChildren";
   public static final String GET_FOLDERCHILDREN_REQUEST =
      "GetFolderChildrenRequest";
   public static final String GET_FOLDERCHILDREN_RESPONSE =
      "GetFolderChildrenResponse";
   
   public static final String GET_FOLDERCOMMUNITIES_OPERATION =
      "getFolderCommunities";
   public static final String GET_FOLDERCOMMUNITIES_REQUEST =
      "GetFolderCommunitiesRequest";
   public static final String GET_FOLDERCOMMUNITIES_RESPONSE =
      "GetFolderCommunitiesResponse";

   public static final String CLONE_SITEFOLDER_OPERATION =
      "cloneSiteFolder";
   public static final String CLONE_SITEFOLDER_REQUEST =
      "CloneSiteFolderRequest";
   public static final String CLONE_SITEFOLDER_RESPONSE =
      "CloneSiteFolderResponse";

   public static final String GET_PARENTFOLDER_OPERATION = "getParentFolder";
   public static final String GET_PARENTFOLDER_REQUEST =
      "GetParentFolderRequest";
   public static final String GET_PARENTFOLDER_RESPONSE =
      "GetParentFolderResponse";

   public static final String MOVE_FOLDERCHILDREN_OPERATION =
      "moveFolderChildren";
   public static final String MOVE_FOLDERCHILDREN_REQUEST =
      "MoveFolderChildrenRequest";
   public static final String MOVE_FOLDERCHILDREN_RESPONSE =
      "MoveFolderChildrenResponse";

   public static final String REMOVE_FOLDERCHILDREN_OPERATION =
      "removeFolderChildren";
   public static final String REMOVE_FOLDERCHILDREN_REQUEST =
      "RemoveFolderChildrenRequest";
   public static final String REMOVE_FOLDERCHILDREN_RESPONSE =
      "RemoveFolderChildrenResponse";

   public static final String GET_SUMMARYBYPATH_OPERATION =
      "getSummaryByPath";
   public static final String GET_SUMMARYBYPATH_REQUEST =
      "GetSummaryByPathRequest";
   public static final String GET_SUMMARYBYPATH_RESPONSE =
      "GetSummaryByPathResponse";
   
   public static final String GET_DESCENDENTSLOCATORS_OPERATION = 
      "getDescendentsLocators";
   public static final String GET_DESCENDENTSLOCATORS_REQUEST =
      "GetDescendentsLocatorsRequest";
   public static final String GET_DESCENDENTSLOCATORS_RESPONSE =
      "GetDescendentsLocatorsResponse";
   
   public static final String GET_DESCENDENTSLOCATORS_WITHOUTFILTER_OPERATION = 
      "getDescendentsLocatorsWithoutFilter";
   public static final String GET_DESCENDENTSLOCATORS_WITHOUTFILTER_REQUEST =
      "GetDescendentsLocatorsWithoutFilterRequest";
   public static final String GET_DESCENDENTSLOCATORS_WITHOUTFILTER_RESPONSE =
      "GetDescendentsLocatorsWithoutFilterResponse";
      
   /**
    * It is used to handle communication between the client and server.
    * Initialized by the constructor, never <code>null</code> after that.
    */
   private PSRemoteFolderAgent m_rmAgent;

  
}
