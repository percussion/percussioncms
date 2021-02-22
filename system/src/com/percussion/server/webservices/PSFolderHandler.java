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
package com.percussion.server.webservices;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSCloneSiteFolderRequest;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderProcessorProxy;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSSaveResults;
import com.percussion.cms.objectstore.ws.PSLocatorWithName;
import com.percussion.cms.objectstore.ws.PSRemoteFolderProcessor;
import com.percussion.cms.objectstore.ws.PSWsFolderProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.server.PSRequest;
import com.percussion.server.cache.PSItemSummaryCache;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class is used to handle all folder related operations for webservices.
 * These operations are specified in the "Folder" port in the
 * <code>WebServices.wsdl</code>.
 *
 * @see {@link com.percussion.hooks.webservices.PSWSFolder}.
 */
public class PSFolderHandler extends PSWebServicesBaseHandler
{
   /**
    * Processing the create folder request. The parent document upon
    *    completion will contain the created folder object in XML.
    *    The root element is <code>CreateFolderResponse</code> which
    *    is specified in <code>sys_FolderParameters.xsd</code>.
    *
    * @param request The request that contains the input paramters,
    *    the <code>CreateFolderRequest</code> element, that is specified in
    *    sys_FolderParameters.xsd. Assume not <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    *
    * @throws PSException if any error occurs.
    */
   void createFolderAction(PSRequest request, Document parent)
      throws PSException
   {
      Document inputDoc = request.getInputDocument();

      // get to be created folder object from the input document
      Element root = inputDoc.getDocumentElement();
      PSXMLDomUtil.checkNode(root, PSWsFolderProcessor.CREATE_FOLDER_REQUEST);

      Element folderEl = PSXMLDomUtil.getFirstElementChild(root);
      folderEl = validateCreateFolderRequest(folderEl);
      PSFolder folder = new PSFolder(folderEl);

      PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();

      PSSaveResults results = proxy.save(new IPSDbComponent[] { folder });
      PSFolder newFolder = (PSFolder)results.getResults()[0];

      Element respRoot = parent.getDocumentElement();
      respRoot.appendChild(newFolder.toXml(parent));
   }

   /**
    * Processing the open folder request. The parent document upon
    *    completion will contain the folder object in XML.
    *    The root element is <code>OpenFolderResponse</code> which
    *    is specified in <code>sys_FolderParameters.xsd</code>.
    *
    * @param request The request that contains the input paramters,
    *    the <code>OpenFolderRequest</code> element, that is specified in
    *    sys_FolderParameters.xsd. Assume not <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    *
    * @throws PSException if any error occurs.
    */
   void openFolderAction(PSRequest request, Document parent) throws PSException
   {
      // get the input data from request
      int folderId = getFolderIdFromDoc(request.getInputDocument(),
         PSWsFolderProcessor.OPEN_FOLDER_REQUEST,
         PSWsFolderProcessor.FOLDER_ID_EL, 
         PSRemoteFolderProcessor.OPEN_FOLDER_OPERATION);

      PSLocator locator = new PSLocator(folderId, 1);

      PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();
      Element[] elements = null;
      try
      {
         elements = proxy.load(FOLDER_PROXY_TYPE, new PSKey[] { locator });
      }
      catch (PSCmsException e)
      {
         String args[] = {String.valueOf(folderId), e.getLocalizedMessage()};
         throw new PSCmsException(IPSCmsErrors.FAIL_OPEN_FOLDER, args);
      }
      PSFolder folder = new PSFolder(elements[0]);

      // creates the response document which contains the folder object
      Element respRoot = parent.getDocumentElement();
      respRoot.appendChild(folder.toXml(parent));
   }

   /**
    * Processing the delete folder request.
    *
    * @param request The request that contains the input paramters,
    *    the <code>DeleteFolderRequest</code> element, that is specified in
    *    sys_FolderParameters.xsd. Assume not <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    *
    * @throws PSException if any error occurs.
    */
   void deleteFolderAction(PSRequest request, Document parent)
      throws PSException
   {
      Document inputDoc = request.getInputDocument();

      // get the input data from request
      int folderId = getFolderIdFromDoc(inputDoc,
         PSWsFolderProcessor.DELETE_FOLDER_REQUEST,
         PSWsFolderProcessor.FOLDER_ID_EL, 
         PSRemoteFolderProcessor.DELETE_FOLDER_OPERATION);

      PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();
      proxy.delete(FOLDER_PROXY_TYPE, new PSKey[]
         { new PSLocator(folderId, 1) });

      addResultResponseXml("success", 0, null, parent);
   }
   
   /**
    * Processing the purge folder request. This action first deletes all
    * folders recursivly and if that operation is successful, it purges all
    * content items found in all deleted folders.
    *
    * @param request the request that contains the input paramters,
    *    the <code>PurgeFolderRequest</code> element, that is specified in
    *    sys_FolderParameters.xsd. Assumed not <code>null</code>.
    * @param parent the parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    * @throws PSException if any error occurs.
    */
   void purgeFolderAction(PSRequest request, Document parent) throws PSException, PSValidationException {
      Document inputDoc = request.getInputDocument();

      // get the input data from request
      int folderId = getFolderIdFromDoc(inputDoc,
         PSWsFolderProcessor.PURGE_FOLDER_REQUEST,
         PSWsFolderProcessor.FOLDER_ID_EL, 
         PSRemoteFolderProcessor.PURGE_FOLDER_OPERATION);
      
      purgeFolderAndChildItems(folderId, request);

      addResultResponseXml("success", 0, null, parent);
   }
   

   /**
    * Purge the specified folder and its descendent child folders and items.
    * 
    * @param folderId the to be purged folder id.
    * @param request the current request, never <code>null</code>.
    * 
    * @throws PSException if an error occurs while purging the specified folder
    *    and its child folders and items.
    */
   public static void purgeFolderAndChildItems(int folderId, PSRequest request)
           throws PSException, PSValidationException {
      PSLocator folder = new PSLocator(folderId, 1);

      List<String> itemIds = new ArrayList<String>(); 
      getFolderContent(folder, itemIds);

      /*
       * Delete the folders first. This will throw an exception if the current
       * user does not have the permission to delete the folders. In that case
       * we also don't want the user to delete any content items.
       */
      PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();
      proxy.delete(FOLDER_PROXY_TYPE, new PSKey[] { folder });
      
      // now purge all content items collected in a previous step
      PSContentDataHandler.purgeItems(request, itemIds);
   }
   
   /**
    * Get a list of content id's for all content items for the supplied folder 
    * recursivly.
    * 
    * @param folder the folder for which to get a list of content id's,
    *    assumed not <code>null</code>.
    * @param itemIds a list into which all content id's are collected. This is
    *    a list of content id's as <code>String</code> objects for all content 
    *    items found in the supplied folder recursivly.
    * @throws PSCmsException for any relationship processor error.
    */
   static private void getFolderContent(
      PSLocator folder, List<String> itemIds) throws PSCmsException
   {
      List<PSComponentSummary> children = getFolderSummaries( 
            folder);
         
      Iterator walker = children.iterator();
      while (walker.hasNext())
      {
         PSComponentSummary summary = (PSComponentSummary) walker.next();
         if (summary.isFolder())
            getFolderContent(summary.getCurrentLocator(), 
                  itemIds);
         else if (summary.isItem())
            itemIds.add(String.valueOf(summary.getContentId()));
      }
   }
   
   /**
    * Get the summaries of all children for the supplied folder locator.
    * 
    * @param locator the locator of the folder for which to get all children,
    *    assumed not <code>null</code>.
    * @return a list of <code>PSComponentSummary</code> objects with all direct
    *    children found for the supplied folder locator, never 
    *    <code>null</code>, may be empty.
    * @throws PSCmsException for any error making the lookup request.
    */
   static private List<PSComponentSummary> getFolderSummaries(
         PSLocator locator) throws PSCmsException
   {
      PSComponentSummary[] sums = PSServerFolderProcessor.getInstance().getChildSummaries(locator);
      return Arrays.asList(sums);
   }

   /**
    * Processing the update folder request. The parent document upon
    *    completion will contain the updated folder object in XML.
    *    The root element is <code>UpdateFolderResponse</code> which
    *    is specified in <code>sys_FolderParameters.xsd</code>.
    *
    * @param request The request that contains the input paramters,
    *    the <code>UpdateFolderResponse</code> element, that is specified in
    *    sys_FolderParameters.xsd. Assume not <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    *
    * @throws PSException if any error occurs.
    */
   void updateFolderAction(PSRequest request, Document parent)
      throws PSException
   {
      Document inputDoc = request.getInputDocument();

      // get to be updated folder object from the input document
      Element root = inputDoc.getDocumentElement();
      PSXMLDomUtil.checkNode(root, PSWsFolderProcessor.UPDATE_FOLDER_REQUEST);

      Element folderEl = PSXMLDomUtil.getFirstElementChild(root);
      PSFolder folder = new PSFolder(folderEl);
      
      PSSaveResults results =  PSServerFolderProcessor.getInstance().save(new IPSDbComponent[] { folder });
      PSFolder updatedFolder = (PSFolder)results.getResults()[0];

      // create response document
      Element respRoot = parent.getDocumentElement();
      respRoot.appendChild(updatedFolder.toXml(parent));
   }

   /**
    * Processing the request for adding folder children.
    *
    * @param request The request that contains the input paramters,
    *    the <code>AddFolderChildrenRequest</code> element, that is specified in
    *    sys_FolderParameters.xsd. Assume not <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    *
    * @throws PSException if any error occurs.
    */
   void addFolderChildrenAction(PSRequest request, Document parent)
      throws PSException
   {
      // get the parameters from the input document
      ParentChildIds pcIds =
         new ParentChildIds(
            request.getInputDocument(),
            PSWsFolderProcessor.ADD_FOLDERCHILDREN_REQUEST,
            PSWsFolderProcessor.TARGET_PARENT_ID_EL,
            PSWsFolderProcessor.ADD_FOLDERCHILDREN_OPERATION);

 

      PSServerFolderProcessor.getInstance().addChildren(pcIds.getChildLocators(), pcIds.getParentLocator());

      addResultResponseXml("success", 0, null, parent);
   }

   /**
    * Processing the request for copying folder children.
    *
    * @param request The request that contains the input paramters,
    *    the <code>CopyFolderChildrenRequest</code> element, that is specified in
    *    sys_FolderParameters.xsd. Assume not <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    *
    * @throws PSException if any error occurs.
    */
   void copyFolderChildrenAction(PSRequest request, Document parent)
      throws PSException
   {
      ParentChildIds pcIds =
         new ParentChildIds(
            request.getInputDocument(),
            PSWsFolderProcessor.COPY_FOLDERCHILDREN_REQUEST,
            PSWsFolderProcessor.TARGET_PARENT_ID_EL,
            true,
            PSWsFolderProcessor.COPY_FOLDERCHILDREN_OPERATION);

         PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();

         proxy.copyChildren(pcIds.getChildLocators(), pcIds.getParentLocator());


      addResultResponseXml("success", 0, null, parent);
   }

   /**
    * Processing the get folder children request. The parent document upon
    *    completion will contain the updated folder object in XML.
    *    The root element is <code>GetFolderChildrenResponse</code> which
    *    is specified in <code>sys_FolderParameters.xsd</code>.
    *
    * @param request The request that contains the input paramters,
    *    the <code>GetFolderChildrenRequest</code> element, that is specified in
    *    sys_FolderParameters.xsd. Assume not <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    *
    * @throws PSException if any error occurs.
    */
   void getFolderChildrenAction(PSRequest request, Document parent)
      throws PSException
   {
      // get the input data from request
      PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();
      PSComponentSummary[] summaryArray = null;

      int id = getFolderIdFromDoc(request.getInputDocument(),
         PSWsFolderProcessor.GET_FOLDERCHILDREN_REQUEST,
         PSWsFolderProcessor.FOLDER_ID_EL, 
         PSRemoteFolderProcessor.GET_FOLDERCHILDREN_OPERATION);

      summaryArray = proxy.getChildSummaries(new PSLocator(id, 1));

      PSComponentSummaries summaries = new PSComponentSummaries(summaryArray);

      // create response document
      Element respRoot = parent.getDocumentElement();
      respRoot.appendChild(summaries.toXml(parent));
   }
   
   /**
    * Process the get folder communities request.
    * 
    * @param request the request that contains the input paramters,
    *    the <code>GetFolderCommunitiesRequest</code> element, that is 
    *    specified in <code>sys_FolderParameters.xsd</code>, assumed not 
    *    <code>null</code>.
    * @param parent the parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    * @throws PSException if any error occurs.
    */
   void getFolderCommunitiesAction(PSRequest request, Document parent)
      throws PSException
   {
      // get the input data from request
      PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();

      int id = getFolderIdFromDoc(request.getInputDocument(),
         PSWsFolderProcessor.GET_FOLDERCOMMUNITIES_REQUEST,
         PSWsFolderProcessor.FOLDER_ID_EL,
         PSWsFolderProcessor.GET_FOLDERCOMMUNITIES_OPERATION);

      Set communities = proxy.getFolderCommunities(new PSLocator(id, 1));
      
      // create response document
      Element communitiesElem = parent.createElement("Communities");
      
      Iterator walker = communities.iterator();
      while (walker.hasNext())
      {
         Integer community = (Integer) walker.next();
         Element communityElem = parent.createElement("Community");
         communityElem.setAttribute("id", community.toString());
         communitiesElem.appendChild(communityElem);
      }

      Element respRoot = parent.getDocumentElement();
      respRoot.appendChild(communitiesElem);
   }
   
   /**
    * Process the clone site folder request.
    * 
    * @param request the request that contains the input paramters,
    *    the <code>CloneSiteFolderRequest</code> element, that is 
    *    specified in <code>sys_FolderParameters.xsd</code>, assumed not 
    *    <code>null</code>.
    * @param parent the parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    * @throws PSException if any error occurs.
    */
   void cloneSiteFolderAction(PSRequest request, Document parent)
      throws PSException
   {
      // get the input data from request
      PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();

      PSCloneSiteFolderRequest msg = new PSCloneSiteFolderRequest(
         request.getInputDocument().getDocumentElement(), null, null);

      String errorLog = proxy.copyFolder(msg.getSource(), msg.getTarget(), 
         msg.getOptions());

      // create response document
      Element resultsElem = parent.createElement("CloneSiteFolderResults");
      resultsElem.setAttribute("errorLog", errorLog == null ? "" : errorLog);

      Element respRoot = parent.getDocumentElement();
      respRoot.appendChild(resultsElem);
   }
   
   /**
    * Processing the move folder children request.
    *
    * @param request The request that contains the input paramters,
    *    the <code>MoveFolderChildrenRequest</code> element, that is specified in
    *    sys_FolderParameters.xsd. Assume not <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    *
    * @throws PSException if any error occurs.
    */
   void moveFolderChildrenAction(PSRequest request, Document parent)
      throws PSException
   {
      ParentChildIds pcIds =
         new ParentChildIds(
            request.getInputDocument(),
            PSWsFolderProcessor.MOVE_FOLDERCHILDREN_REQUEST,
            PSWsFolderProcessor.SOURCE_PARENT_ID_EL,
            PSWsFolderProcessor.TARGET_PARENT_ID_EL,
            PSWsFolderProcessor.FORCE,
            PSWsFolderProcessor.MOVE_FOLDERCHILDREN_OPERATION);

      PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();

      proxy.moveChildren(
         pcIds.getParentLocator(),
         pcIds.getChildLocators(),
         pcIds.getParentLocator2(),
         pcIds.isForce());

      addResultResponseXml("success", 0, null, parent);
   }

   /**
    * Processing the copy folder security request.
    *
    * @param request The request that contains the input parameters (source and
    *    target folder id), the <code>CopyFolderSecurityRequest</code> element.
    *    Assume not <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    *
    * @throws PSException if any error occurs.
    */
   void copyFolderSecurityAction(PSRequest request, Document parent)
      throws PSException
   {
      // get the input data from request
      Element root = request.getInputDocument().getDocumentElement();
      PSXMLDomUtil.checkNode(root, PSWsFolderProcessor.COPY_FOLDERSECURITY_REQUEST);

      Element el = PSXMLDomUtil.getFirstElementChild(root,
            PSWsFolderProcessor.SOURCE_FOLDER_ID_EL);
      
      int srcFolderId = getContentId(el,
            PSWsFolderProcessor.COPY_FOLDERSECURITY_OPERATION);
      
      el = PSXMLDomUtil.getNextElementSibling(el,
            PSWsFolderProcessor.TARGET_FOLDER_ID_EL);
      
      int tgtFolderId = getContentId(el,
            PSWsFolderProcessor.COPY_FOLDERSECURITY_OPERATION);
      
      PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();
      proxy.copyFolderSecurity(new PSLocator(srcFolderId, 1),
            new PSLocator(tgtFolderId, 1));      

      addResultResponseXml("success", 0, null, parent);
   }
   
   /**
    * Processing the request for removing folder children.
    *
    * @param request The request that contains the input paramters,
    *    the <code>RemoveFolderChildrenRequest</code> element, that is specified in
    *    sys_FolderParameters.xsd. Assume not <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    *
    * @throws PSException if any error occurs.
    */
   void removeFolderChildrenAction(PSRequest request, Document parent)
      throws PSException
   {
      ParentChildIds pcIds =
         new ParentChildIds(
            request.getInputDocument(),
            PSWsFolderProcessor.REMOVE_FOLDERCHILDREN_REQUEST,
            PSWsFolderProcessor.PARENT_ID_EL,
            null,
            PSWsFolderProcessor.FORCE,
            PSWsFolderProcessor.REMOVE_FOLDERCHILDREN_OPERATION);

      PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();

      proxy.removeChildren(
         pcIds.getParentLocator(),
         pcIds.getChildLocators(),
         pcIds.isForce());

      addResultResponseXml("success", 0, null, parent);
   }

   /**
    * Given a folder or item id, retrieves the fully qualified path. Either
    * a valid path is returned or a ResultResponse w/ an error is returned.
    * The result may contain more than 1 path.
    * <p>The dtd for this fragment is:
    * <pre>
    * &lt!ELEMENT GetFolderPathsResponse (FolderPaths | com:ResultResponse)&gt
    * &lt!ELEMENT FolderPaths (Path+)&gt
    * &lt!ELEMENT Path (#PCDATA)&gt
    * </pre>
    * <p>This has not been exposed in the wsdl.
    *
    * @param request The context for this request. Never <code>null</code>.
    * 
    * @param response The parent document to add the response element to,
    *    never <code>null</code> and it will already contain the correct
    *    base element for the response.
    *
    * @throws PSException if any error occurs.
    */
   void getFolderPathsAction(PSRequest request, Document response)
      throws PSException
   {
      if ( null == request)
      {
         throw new IllegalArgumentException("request cannot be null");  
      }
      if ( null == response)
      {
         throw new IllegalArgumentException("response doc cannot be null");  
      }

      PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();

      int id = getFolderIdFromDoc(request.getInputDocument(),
            PSWsFolderProcessor.GET_FOLDER_PATH_REQUEST,
            PSWsFolderProcessor.CHILD_ID_EL, 
            PSRemoteFolderProcessor.GET_FOLDER_PATH_OPERATION);

      String[] paths = proxy.getFolderPaths(new PSLocator(id, 1));
      Element parent = PSXmlDocumentBuilder.addEmptyElement(response,  
            response.getDocumentElement(), 
            PSWsFolderProcessor.FOLDER_PATHS_EL);
      if (paths.length >= 1 && paths[0] != null && paths[0].trim().length() > 0)
      {
         // return the valid path
         for (int i=0; i < paths.length; i++)
         {
            PSXmlDocumentBuilder.addElement(response, parent, 
                  PSWsFolderProcessor.PATH_EL, 
                  paths[i]);
         }
      }
      else
      {
         // validating the content id
         PSItemSummaryCache cache = PSItemSummaryCache.getInstance();
         boolean validateId = false;
         if (cache != null && cache.getItem(id) != null) 
         {    // validating from cache
             validateId = true;
         }
         else // validating the id from repository
         {
            IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
            PSComponentSummary sum = cms.loadComponentSummary(id);
            if (sum != null)
               validateId = true;
         }

         if (!validateId)
            throw new PSCmsException(IPSCmsErrors.INVALID_FOLDER_ID, String
                  .valueOf(id));
      }
   }
   
   /**
    * Processing the get parent folder request. The parent document upon
    *    completion will contain the updated folder object in XML.
    *    The root element is <code>GetFolderParentResponse</code> which
    *    is specified in <code>sys_FolderParameters.xsd</code>.
    *
    * @param request The request that contains the input paramters,
    *    the <code>GetFolderParentRequest</code> element, that is specified in
    *    sys_FolderParameters.xsd. Assume not <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    *
    * @throws PSException if any error occurs.
    */
   void getParentFolderAction(PSRequest request, Document parent)
      throws PSException
   {
      // get the input data from request
      PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();
      PSComponentSummary[] summaryArray = null;

      int id = getFolderIdFromDoc(request.getInputDocument(),
         PSWsFolderProcessor.GET_PARENTFOLDER_REQUEST,
         PSWsFolderProcessor.CHILD_ID_EL, 
         PSRemoteFolderProcessor.GET_PARENTFOLDER_OPERATION);
      summaryArray = proxy.getParentSummaries(new PSLocator(id, 1));

      if (summaryArray.length == 0)
      {
         throw new PSCmsException(IPSCmsErrors.FAIL_GET_PARENT_FOLDER, 
               String.valueOf(id));
      }

      PSComponentSummaries summaries = new PSComponentSummaries(summaryArray);

      // create response document
      Element respRoot = parent.getDocumentElement();
      respRoot.appendChild(summaries.toXml(parent));
   }
   
   /**
    * Processing the get descendents Locators request. The parent document upon
    *    completion will contain the array of locators in XML.
    *    The root element is <code>GetDescendentsLocatorsResponse</code> which
    *    is specified in <code>sys_FolderParameters.xsd</code>.
    *
    * @param request The request that contains the input paramters,
    *    the <code>GetDescendentsLocatorsRequest</code> element, that is specified in
    *    sys_FolderParameters.xsd. Assume not <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    *
    * @throws PSException if any error occurs.
    */
   void getDescendentsLocatorsAction(PSRequest request, Document parent)
      throws PSException
   {
      // get the input data from request
      PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();
      PSKey[] locatorArray = null;
   
      int id = getFolderIdFromDoc(request.getInputDocument(),
         PSWsFolderProcessor.GET_DESCENDENTSLOCATORS_REQUEST,
         PSWsFolderProcessor.PARENT_ID_EL, 
         PSRemoteFolderProcessor.GET_DESCENDENTSLOCATORS_OPERATION);
   
      locatorArray = proxy.getDescendentFolderLocators(new PSLocator(id, 1));  
   
      // create response document
      Element respRoot = parent.getDocumentElement();
      for(int i = 0; i < locatorArray.length; i++)
         respRoot.appendChild(locatorArray[i].toXml(parent));
      
   }
   
   /**
    * Processing the get descendants Locators without filter request. The parent
    *    document upon completion will contain the array of locators in XML.
    *
    * @param request The request that contains the input param3ters,
    *    the <code>GetDescendentsLocatorsWithoutFilterRequest</code> element.
    *    Assume not <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    *
    * @throws PSException if any error occurs.
    */
   void getDescendentsLocatorsWithoutFilterAction(PSRequest request, Document parent)
      throws PSException
   {
      // get the input data from request
      PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();
      PSKey[] locatorArray = null;
   
      int id = getFolderIdFromDoc(request.getInputDocument(),
         PSWsFolderProcessor.GET_DESCENDENTSLOCATORS_WITHOUTFILTER_REQUEST,
         PSWsFolderProcessor.PARENT_ID_EL, 
         PSRemoteFolderProcessor.GET_DESCENDENTSLOCATORS_WITHOUTFILTER_OPERATION);
   
      locatorArray = proxy.getDescendentFolderLocatorsWithoutFilter(new PSLocator(id, 1));  
   
      // create response document
      Element respRoot = parent.getDocumentElement();
      for(int i = 0; i < locatorArray.length; i++)
         respRoot.appendChild(locatorArray[i].toXml(parent));
   }

   /**
    * Processing the getSummaryByPath request. The parent document upon
    *    completion will contain the updated folder object in XML.
    *    The root element is <code>GetSummaryByPathResponse</code> which
    *    is specified in <code>sys_FolderParameters.xsd</code>.
    *
    * @param req The request that contains the input paramters,
    *    the <code>GetSummaryByPathRequest</code> element, that is specified in
    *    sys_FolderParameters.xsd. Assume not <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    *
    * @throws PSException if any error occurs.
    */
   void getSummaryByPathAction(PSRequest req, Document parent)
      throws PSException
   {
      PSFolderProcessorProxy proxy = getFolderProcessorProxy(req);
      String path = getRequestValueFromDoc(
            req.getInputDocument(),
            PSWsFolderProcessor.GET_SUMMARYBYPATH_REQUEST,
            PSWsFolderProcessor.PATH_EL);
      PSComponentSummary summary = proxy.getSummary(path);

      // create response document
      Element respRoot = parent.getDocumentElement();
      if (summary == null)
      {
         Element nullEl =
            parent.createElement(PSRemoteFolderProcessor.NULL_RESULT);
         respRoot.appendChild(nullEl);
      }
      else
      {
         respRoot.appendChild(summary.toXml(parent));
      }
   }

   private String getRequestValueFromDoc(
      Document inputDoc,
      String parentEl,
      String childEl)
      throws PSUnknownNodeTypeException
   {
      Element root = inputDoc.getDocumentElement();
      PSXMLDomUtil.checkNode(root, parentEl);

      Element el = PSXMLDomUtil.getFirstElementChild(root, childEl);

      return PSXMLDomUtil.getElementData(el);
   }
   
   /**
    * Creates an instance of <code>PSProcessorProxy</code> with a given request.
    *
    * @param request the request to be used to construct the proxy object,
    *    assume not <code>null</code>.
    * @return the created proxy object, never <code>null</code>.
    * @throws PSCmsException if any error occurs.
    */
   private static PSFolderProcessorProxy getFolderProcessorProxy(
      PSRequest request)
      throws PSCmsException
   {
      return new PSFolderProcessorProxy(
         PSComponentProcessorProxy.PROCTYPE_SERVERLOCAL, request);
   }


   /**
    * Get the folder id from a input document. The exptected XML format is:
    * <pre>
    * &lt;ELEMENT parentEl (idElName)&gt;
    * &lt;ELEMENT idElName (#PCDATA)&gt;
    * </pre>
    * where <code>parentEl</code> and <code>idElName</code> are determined by
    * the caller of the method.
    *
    * @param inputDoc the input document, assume not <code>null</code>.
    * @param parentEl the name of the expected root node, assume not
    *    <code>null</code>.
    * @param idElName the expected node name that contains the id, assume not
    *    <code>null</code>.
    * @param action the name of the action for which to extract the folder
    *    id, assumed not <code>null</code> or empty. Used only for exception 
    *    info if the content id is not a valid number.
    * @return the extracted folder id.
    * @throws PSException if the XML is malformed or the folder id is not a
    *    parsable integer.
    */
   private int getFolderIdFromDoc(Document inputDoc, String parentEl,
      String idElName, String action) throws PSException
   {
      Element root = inputDoc.getDocumentElement();
      PSXMLDomUtil.checkNode(root, parentEl);

      Element el = PSXMLDomUtil.getFirstElementChild(root, idElName);
      return getContentId(el, action);
   }
   
   /**
    * Get a content id from a supplied Element.
    * 
    * @param idElem the element of the folder id, assume not 
    *    <code>null</code>, but may be empty.
    *  
    * @param action the action of the current request. Used only for exception 
    * info if the content id is not a valid number.
    * 
    * @return the content id.
    * 
    * @throws PSException if <code>idElem</code> is empty or invalid id.
    */
   private int getContentId(Element idElem, String action) throws PSException
   {
      int id = -1;
      try
      {
         id = Integer.parseInt(PSXMLDomUtil.getElementData(idElem));
      }
      catch (NumberFormatException e)
      {
         Object args[] = { idElem.getNodeName(), action };
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_MISSING_ID, args);
      }

      return id;
   }

   
   /** 
    * If the request is for createFolder, an empty PSLocator is invalid instead
    * set some sane defaults.
    * @param sourceNode
    * @return
    * @throws PSUnknownNodeTypeException
    */
   protected static Element validateCreateFolderRequest(Element sourceNode)
   throws PSUnknownNodeTypeException
   {
       /* PSKey.XML_NODE_NAME is "PSXLocator" */
       NodeList locList = sourceNode.getElementsByTagName("PSXLocator");
       int sz = locList.getLength();
       for ( int i=0; i<sz; i++ )
       {
           Element el = (Element) locList.item(i);
           PSLocator psl = new PSLocator(el);
           if ( psl.getId() == -1 && psl.getRevision() == -1 )
           {
               el.setAttribute(PSKey.XML_ATTR_NEED_GEN_ID, 
                       PSKey.XML_TRUE); 
               el.setAttribute(PSKey.XML_ATTR_IS_PERSISTED,
                       PSKey.XML_FALSE); 
           }
       }
       return sourceNode;
   }

   
   /**
    * This is a helper class, it handles reading XML document which contains
    * parent and child ids, as input parameters.
    */
   private class ParentChildIds
   {
      /**
       * Constructs the object. It will retrieve one parent id and a list of
       * child ids from a input document.
       *
       * @param inputDoc The input document, assume not <code>null</code>.
       * @param rootName The root name of the document, assume not
       *    <code>null</code>.
       * @param parentIdName The parent id name, assume not <code>null</code>.
       * @param action the current request action, assume not <code>null</code>.
       * 
       * @throws PSException if an error occurs.
       */
      private ParentChildIds(
         Document inputDoc,
         String rootName,
         String parentIdName,
         String action)
         throws PSException
      {
         processDoc(inputDoc, rootName, parentIdName, null, null, false, action);
      }

      /**
       * Constructs the object. It will retrieve one parent id, a list of child
       * ids and recursively flag from a input document.
       *
       * @param inputDoc The input document, assume not <code>null</code>.
       *
       * @param rootName The root name of the document, assume not
       *    <code>null</code>.
       *
       * @param parentIdName The parent id name, assume not <code>null</code>.
       *
       * @param recursive The recursive flag.
       *
       * @param action the current request action, assume not <code>null</code>.
       *
       * @throws PSException if an error occurs.
       */
      private ParentChildIds(
         Document inputDoc,
         String rootName,
         String parentIdName,
         boolean recursive,
         String action)
         throws PSException
      {
         processDoc(inputDoc, rootName, parentIdName, null, null, recursive,
            action);
      }

      /**
       * Constructs the object. It will retrieve two parent ids and a list of
       * child ids from a input document.
       *
       * @param inputDoc The input document, assume not <code>null</code>.
       *
       * @param rootName The root name of the document, assume not
       *    <code>null</code>.
       *
       * @param parentIdName The parent id name, assume not <code>null</code>.
       *
       * @param parentIdName2 The 2nd parent id name, assume not
       *    <code>null</code>.
       *
       * @param forceName force element name, assumed not <code>null</code>.
       *
       * @param action the current request action, assume not <code>null</code>.
       *  
       * @throws PSException if an error occurs.
       */
      private ParentChildIds(
         Document inputDoc,
         String rootName,
         String parentIdName,
         String parentIdName2,
         String forceName,
         String action)
         throws PSException
      {
         processDoc(inputDoc, rootName, parentIdName, parentIdName2, forceName, 
            false, action);
      }

      /**
       * Constructs the object. It will retrieve two parent ids and a list of
       * child ids from a input document.
       *
       * @param inputDoc The input document, assume not <code>null</code>.
       *
       * @param rootName The root name of the document, assume not
       *    <code>null</code>.
       *
       * @param parentIdName The parent id name, assume not <code>null</code>.
       *
       * @param parentIdName2 The 2nd parent id name, assume not
       *    <code>null</code>.
       *
       * @param action the current request action, assume not <code>null</code>.
       *  
       * @throws PSException if an error occurs.
       */
      private ParentChildIds(
         Document inputDoc,
         String rootName,
         String parentIdName,
         String parentIdName2,
         String action)
         throws PSException
      {
         processDoc(inputDoc, rootName, parentIdName, parentIdName2, null,
            false, action);
      }

      /**
       * Extracts the parent and child ids from a input document.
       * The XML formate is:
       * <pre>
       * <!ELEMENT rootName (parentIdName, ChildIds, Recursive?, parentIdName2?)>
       * <!ELEMENT parentIdName (#PCDATA)>
       * <!ELEMENT ChildIds (ChildId+)>
       * <!ELEMENT Recursive (true | false)>
       * <!ELEMENT parentIdName2 (#PCDATA)>
       * </pre>
       * where <code>rootName</code>, <code>parentIdName</code> and
       * <code>parentIdName2</code> is determined by caller.
       *
       * @param inputDoc The input document, assume not <code>null</code>.
       *
       * @param rootName The root name of the document, assume not
       *    <code>null</code>.
       *
       * @param parentIdName The parent id name, assume not <code>null</code>.
       *
       * @param parentIdName2 The 2nd parent id name. If it is
       *    <code>null</code>, then not expecting the 2nd parent id element at
       *    the end of the XML document.
       *    
       * @param forceName name of the element for force flag, assumed not 
       *    <code>null</code>
       *
       * @param hasRecursive <code>true</code> if has <code>Recursive</code>
       *    element.
       * 
       * @param action the current request action, assume not <code>null</code>.
       * 
       * @throws PSException if contains invalid folder ids.
       */
      private void processDoc(
         Document inputDoc,
         String rootName,
         String parentIdName,
         String parentIdName2,
         String forceName,
         boolean hasRecursive,
         String action)
         throws PSException
      {
         Element root = inputDoc.getDocumentElement();
         PSXMLDomUtil.checkNode(root, rootName);

         Element parentIdEl =
            PSXMLDomUtil.getFirstElementChild(root, parentIdName);
         m_parentId = getContentId(parentIdEl, action);

         Element childIdsEl =
            PSXMLDomUtil.getNextElementSibling(
               parentIdEl,
               PSWsFolderProcessor.CHILD_IDS_EL);

         m_childIds = getChildLocators(childIdsEl, action);

         // The following element can only exist one at a time,
         // not both at the same time
         Element parentId2El = null;
         if (parentIdName2 != null)
         {
            parentId2El =
               PSXMLDomUtil.getNextElementSibling(childIdsEl, parentIdName2);
            m_parentId2 = getContentId(parentId2El, action);
         }
         Element prev = childIdsEl;
         if (parentId2El != null)
            prev = parentId2El;
         if (forceName != null)
         {
            Element forceEl = PSXMLDomUtil
               .getNextElementSibling(prev, forceName);
            String temp = PSXMLDomUtil.getElementData(forceEl);
            m_force = temp.trim().equalsIgnoreCase("yes") ? true : false;
         }
         if (hasRecursive)
         {
            m_hasRecursive = hasRecursive;

            Element recursiveEl =
               PSXMLDomUtil.getNextElementSibling(
                  childIdsEl,
                  PSWsFolderProcessor.RECURSIVE_EL);
            m_recursive = PSXMLDomUtil.getBooleanElementData(recursiveEl);
         }
      }

      /**
       * Get a list of child ids from a XML element. The format of the XML is
       * specified in <code>ChildIds</code> element in the
       * sys_FolderParameters.xsd. All <code>ChildIds</code> elements must
       * either have the <code>name</code> attribute or not have the attribute.
       * It cannot be mixed.
       *
       * @param childIdsEl The XML element that contains a list of child ids.
       *    Assume it is not <code>null</code>
       * @param action the action or operation of the current request. 
       *    Assume it is not <code>null</code> or empty.
       *
       * @return A list of locators in <code>PSLocator</code> or 
       *    <code>PSLocatorWithName</code> objects. Never contain mixed
       *    objects, <code>null</code> or empty. 
       *
       * @throws PSException if some <code>ChildIds</code> element has
       *    <code>name</code> attribute, but not others, or other error occurs.
       */
      private List getChildLocators(Element childIdsEl, String action)
         throws PSException
      {
         Set childIds = new HashSet();

         Element el =
            PSXMLDomUtil.getFirstElementChild(
               childIdsEl,
               PSWsFolderProcessor.CHILD_ID_EL);
         boolean isFirstElement = true;
         while (el != null)
         {
            PSXMLDomUtil.checkNode(el, PSWsFolderProcessor.CHILD_ID_EL);
            int id = getContentId(el, action);
            String name = el.getAttribute(PSLocatorWithName.ATTR_OVERRIDE_NAME);
            // Make sure all child elements either specify "name" or do not 
            // specify name. Error on mixed case, some child element has name
            // other child element does not.
            boolean hasName = false;
            if (name == null || name.trim().length() == 0)
            {
               if (isFirstElement)
               {
                  hasName = false;
               }
               else if (hasName)
               {
                  String args[] = new String[] {
                        PSWsFolderProcessor.CHILD_ID_EL, action };
                  throw new PSException(
                        IPSWebServicesErrors.INVALID_MIXED_CHILD_IDS, args);
               }
               
               childIds.add(new PSLocator(id, 1));
            }
            else
            {
               if (isFirstElement)
               {
                  hasName = true;
               }
               else if (! hasName)
               {
                  String args[] = new String[] {
                        PSWsFolderProcessor.CHILD_ID_EL, action };
                  throw new PSException(
                        IPSWebServicesErrors.INVALID_MIXED_CHILD_IDS, args);
               }
               
               childIds.add(new PSLocatorWithName(id, 1, name));
            }
            
            isFirstElement = false;

            el = PSXMLDomUtil.getNextElementSibling(el);
         }

         return new ArrayList(childIds);
      }

      /**
       * Get the 1st parent locator with revision id default to <code>1</code>.
       *
       * @return The 1st parent locator, never <code>null</code>.
       */
      private PSLocator getParentLocator()
      {
         return new PSLocator(m_parentId, 1);
      }

      /**
       * Get the 2nd parent locator with revision id default to <code>1</code>.
       *
       * @return The 2nd parent locator, never <code>null</code>.
       */
      private PSLocator getParentLocator2()
      {
         return new PSLocator(m_parentId2, 1);
      }

      /**
       * Get a list of the child locators. All revision id is default to
       * <code>1</code>.
       *
       * @return A list over <code>PSLocator</code> or 
       *    <code>PSLocatorWithName</code> objects. It never contains mixed
       *    objects. Never <code>null</code> or empty.
       */
      private List getChildLocators()
      {
         return m_childIds;
      }

      /**
       * Force move in case if any of the children are involved in cross site
       * linking. Initialized by ctor. Default is <code>false</code>.
       * @return force flag as described
       */
      public boolean isForce()
      {
         return m_force;
      }

      /**
       * The parent id of the object. Initialized by constructor.
       */
      int m_parentId;

      /**
       * The 2nd optional parent id of the object. It is <code>-1</code> if not
       * exist.
       */
      int m_parentId2 = -1;

      /**
       * <code>true</code> if this object has a recursive flag. Initialized by
       * constrcutor.
       */
      private boolean m_hasRecursive = false;

      /**
       * If <code>m_hasRecursive</code> is <code>true</code> then this
       * value is valid. Initialized by constrcutor.
       */
      private boolean m_recursive;

      /**
       * A list of child ids in <code>Integer</code> objects. Initialized by
       * constructor. Never <code>null</code> or empty after that.
       */
      List m_childIds = new ArrayList();
      
      /**
       * @see #isForce()
       */
      private boolean m_force = false;
   }

   /**
    * The constant for folder proxy type.
    */
   public static final String FOLDER_PROXY_TYPE =
      PSDbComponent.getComponentType(PSFolder.class);
}
