/******************************************************************************
 *
 * [ PSFolderActionManager.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.cx;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSCloningOptions;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderProcessorProxy;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSSaveResults;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.cms.objectstore.PSSecurityProviderCataloger;
import com.percussion.cms.objectstore.PSSite;
import com.percussion.cms.objectstore.ws.PSRemoteFolderAgent;
import com.percussion.cx.catalogers.PSCommunityCataloger;
import com.percussion.cx.catalogers.PSGlobalTemplateCataloger;
import com.percussion.cx.catalogers.PSLocaleCataloger;
import com.percussion.cx.catalogers.PSRoleCataloger;
import com.percussion.cx.catalogers.PSSiteCataloger;
import com.percussion.cx.catalogers.PSSubjectCataloger;
import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.cx.objectstore.PSProperties;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSStringComparator;
import com.percussion.util.PSUrlUtils;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.collections.PSIteratorUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class that manges all folder related actions. Basically a mediator between
 * the content explorer and the folder processor proxy. Converts the input data
 * of from the contexnt explorer to the format the processor needs and executes
 * the services provided by the processor.
 * 
 * @author Ram
 * @version 1.0
 */
public class PSFolderActionManager
{
   /**
    * Constructs this manager with remote proxy that is set with folder context
    * to handle actions related to the folders.
    * 
    * @param actionManager the action manager, which created this
    *           FolderActionMgr , never <code>null</code>
    * @param urlBase The base url of the applet (connection to server), may not
    *           be <code>null</code>
    * @throws PSCmsException if one of the proxies throws an exception, see the
    *            proxies for details
    */
   public PSFolderActionManager(PSActionManager actionManager, URL urlBase)
         throws PSCmsException
   {
      if (actionManager == null)
         throw new IllegalArgumentException("actionManager may not be null.");

      if (urlBase == null)
         throw new IllegalArgumentException("urlBase may not be null.");

      m_actionManager = actionManager;
      m_appletBase = urlBase;

      PSRemoteFolderAgent agent = new PSRemoteFolderAgent(actionManager.getApplet().getHttpConnection(), urlBase);
      m_componentProxy = new PSComponentProcessorProxy(
            PSComponentProcessorProxy.PROCTYPE_REMOTE, agent);

      m_folderProxy = new PSFolderProcessorProxy(
            PSFolderProcessorProxy.PROCTYPE_REMOTE, agent);
   }

   /**
    * Creates a new folder and links to the folder represented by the parent
    * folder node as a child. This method does not check for duplicate name of
    * the folder.
    * 
    * @param parentFolderNode the node that represents the parent folder, may
    *           not be <code>null</code> and must represent a folder.
    * @param folder the new folder to create, may not be <code>null</code>
    * @return the folder object, never <code>null</code>
    * @throws PSCmsException if an error happens either creating or linking to
    *            parent folder.
    */
   public PSFolder createFolder(PSNode parentFolderNode, PSFolder folder)
         throws PSCmsException
   {
      validateNodeAsFolder(parentFolderNode);

      if (folder == null)
         throw new IllegalArgumentException("folder may not be null.");

      //save the folder and get back the folder with locator
      PSSaveResults results = m_componentProxy
            .save(new IPSDbComponent[] { folder });
      folder = (PSFolder) results.getResults()[0];
      List<PSLocator> list = new ArrayList<PSLocator>();
      list.add(folder.getLocator());

      //add this folder to parent
      PSFolder parent = nodeToFolder(parentFolderNode);
      PSLocator locator = parent.getLocator();
      try
      {
         m_folderProxy.addChildren(list, locator);
      }
      catch (PSCmsException e)
      {
         // failed to attach the created folder to its parent, 
         // then remove the created (orphan's) folder
         m_componentProxy.delete(folder.getComponentType(),
             new PSKey[] { folder.getLocator() });
         throw e;
      }

      // insert dirty node under parent folder to trigger refresh
      PSNode tmpNode = new PSNode("tmp", "tmp", PSNode.TYPE_FOLDER, null, null,
            false, folder.getPermissions().getPermissions());
      tmpNode.setProperty(IPSConstants.PROPERTY_CONTENTID, String
            .valueOf(folder.getLocator().getId()));
      tmpNode.setIsDirty(true);
      parentFolderNode.addChild(tmpNode);

      return folder;
   }

   /**
    * Gets the fully qualified folder path(s) of the supplied id from the
    * server.
    * 
    * @param key Never <code>null</code>.
    * 
    * @return One or more paths of the form '//a/b/c...' if <code>key</code>
    *         is a child of a valid folder. If the supplied key is valid, but
    *         not a folder child, an empty array is returned. Otherwise, an
    *         exception is thrown.
    * 
    * @throws PSCmsException If any problems retrieving the paths, including a
    *            bad id.
    */
   public String[] getFolderPaths(PSLocator key) throws PSCmsException
   {
      if (null == key)
      {
         throw new IllegalArgumentException("key cannot be null");
      }

      String[] paths = m_folderProxy.getFolderPaths(key);
      return paths;
   }

   /**
    * Saves the modified folder on the server.
    * 
    * @param folder the folder to update, may not be <code>null</code>
    * 
    * @return the updated folder, never <code>null</code>
    * 
    * @throws PSCmsException if an error happens saving the folder.
    */
   public PSFolder modifyFolder(PSFolder folder) throws PSCmsException
   {
      if (folder == null)
         throw new IllegalArgumentException("folder may not be null.");

      PSSaveResults results = m_componentProxy
            .save(new IPSDbComponent[] { folder });
      folder = (PSFolder) results.getResults()[0];

      return folder;
   }

   /**
    * Clears the publish folder flags from all descendent folders of the folder
    * passed in.
    * 
    * @param folder the parent folder to get descendents from, cannot be
    *           <code>null</code>
    * @throws PSCmsException if any error occurs
    */
   public void clearDescendentPublishFlags(PSFolder folder)
         throws PSCmsException
   {
      if (folder == null)
         throw new IllegalArgumentException("folder cannot be null.");
      // Get all the descendant folder locators
      PSLocator[] descendentKeys = m_folderProxy
            .getDescendentFolderLocators(new PSLocator(folder.getLocator()
                  .getPartAsInt()));
      // Create list of flagged descendant folders
      Set flaggedFolders = getApplet().getFlaggedFolderSet();
      List<PSLocator> flaggedKeys = new ArrayList<PSLocator>();
      for (int i = 0; i < descendentKeys.length; i++)
      {
         if (flaggedFolders.contains(descendentKeys[i]
               .getPart(PSLocator.KEY_ID)))
            flaggedKeys.add(descendentKeys[i]);
      }
      // Load the folders
      Element[] descendentsEl = m_componentProxy.load(ms_folderCompType,
            flaggedKeys.toArray(new PSKey[flaggedKeys.size()]));
      List<PSFolder> updatedfolder = new ArrayList<PSFolder>();
      // Go through each folder to see if the publish folder flag exists
      // if it does then delete it and add it to the list for updating
      try
      {
         for (int i = 0; i < descendentsEl.length; i++)
         {
            PSFolder descendent = new PSFolder(descendentsEl[i]);
            
           descendent.setPublishOnlyInSpecialEdition(false);
           updatedfolder.add(descendent);
         
         }
         if (updatedfolder.size() > 0)
         {
            IPSDbComponent[] comps = new IPSDbComponent[updatedfolder.size()];
            m_componentProxy.save(updatedfolder
                  .toArray(comps));

            // Refresh the list of flagged folders
            try
            {
               m_actionManager.getApplet().loadFlaggedFoldersSet();
            }
            catch (Exception e)
            {
               // Just print a stacktrace as we don't want
               // to throw an exception for this minor error
               e.printStackTrace();
            }

         }
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSCmsException(e);
      }
   }

   /**
    * Loads the folder represented by supplied node's locator.
    * 
    * @param node the folder node, may not be <code>null</code>
    * 
    * @return the loaded folder, never <code>null</code>
    * 
    * @throws PSContentExplorerException if an error happens loading the folder.
    */
   public PSFolder loadFolder(PSNode node) throws PSContentExplorerException
   {
      validateNodeAsFolder(node);

      try
      {
         PSFolder f = nodeToFolder(node);

         Element[] els = m_componentProxy.load(f.getComponentType(),
               new PSLocator[] { f.getLocator() });

         if (els.length < 1)
            throw new IllegalStateException("Could not load the folder "
                  + f.getName());

         f.fromXml(els[0]);
         return f;
      }
      catch (PSException unte)
      {
         throw new PSContentExplorerException(
               IPSContentExplorerErrors.GENERAL_ERROR, unte.toString());
      }
   }

   private void removeSystemFolder(List<PSNode> children){
      if(children != null){
         for (PSNode child:children){
            if("$System$".equals(child.getName())){
               children.remove(child);
               break;
            }
         }
      }
   }

   /**
    * Loads the children of the supplied folder node. The results returned from
    * the folder processor proxy are converted to the content explorer node
    * objects.
    * 
    * @param parentFolderNode the parent folder node whose children need to be
    *           loaded, may not be <code>null</code>
    * 
    * @return An iterator over zero or more child <code>PSNode</code> objects,
    *         never <code>null</code>. The list is grouped with folders
    *         first, and items following, and each group is sorted ascending
    *         case insensitive by the value of each node's
    *         <code>toString()</code> method.
    * 
    * @throws PSContentExplorerException if an error happens loading the folder
    *            or its children with all columns according to its display
    *            format.
    */
   @SuppressWarnings("unchecked")
   public Iterator loadChildren(PSNode parentFolderNode)
         throws PSContentExplorerException
   {
      validateNodeAsFolder(parentFolderNode);

      // Call listeners for start of loading
      for (Iterator iter = m_searchListeners.iterator(); iter.hasNext();)
      {
         IPSSearchListener listener = (IPSSearchListener) iter.next();
         listener.searchInitiated(parentFolderNode);
      }

      // Reset quickload flag
      if (parentFolderNode.isQuickLoaded())
      {
         parentFolderNode.setQuickLoaded(false);
         
         // add dummy node to trigger load of items only
         parentFolderNode.addChild(PSNode.createDirtyNewItemNode());
      }

      try
      {
         String formatid = parentFolderNode.getDisplayFormatId();
         if (StringUtils.isBlank(formatid))
         {
            // first check the display format options, if we find a display
            // format for the specified folder use that otherwise continue
            formatid = m_actionManager.getApplet()
                  .getDisplayFormatIdFromOptions(parentFolderNode);

            if (StringUtils.isBlank(formatid))
            {
               // If not found in the options, obtain from the parent
               PSLocator key = PSActionManager
                  .nodeToLocator(parentFolderNode);
               Element[] elems = m_componentProxy.load(ms_folderCompType,
                     new PSKey[] { key });
               PSFolder folder = new PSFolder(elems[0]);

               formatid = folder.getDisplayFormatPropertyValue();
               parentFolderNode.setDisplayFormatId(formatid);

               m_actionManager.getApplet().saveDisplayFormatIdToOptions(
                     parentFolderNode);
            }
            else
            {
               parentFolderNode.setDisplayFormatId(formatid);
            }
         }
         PSDisplayFormat format = getDisplayFormatById(formatid, true);
         
         // now do item search and add resulting item nodes
         List<PSNode> resList = new ArrayList();
         PSSearch search = new PSSearch();
         search.setMaximumNumber(PSSearch.UNLIMITED_MAX);
         String folderId = parentFolderNode.getContentId();
         search.setProperty(PSSearch.PROP_FOLDER_PATH, folderId);
         search.setProperty(PSSearch.PROP_FOLDER_PATH_RECURSE, "false");
         search.setProperty(PSSearch.PROP_OVERRIDE_GLOBAL_MAX_RESULTS, 
            PSSearch.BOOL_YES);
         if (isQuickExpand())
         {
            // Limit the search to folders for quick load
            PSSearchField field = 
               new PSSearchField(IPSHtmlParameters.SYS_CONTENTTYPEID,
                     "Content Type", "", 
                     PSSearchField.TYPE_NUMBER, null);
            field.setFieldValue(PSSearchField.OP_EQUALS, "101");
            search.addField(field);
            parentFolderNode.setQuickLoaded(true);
         }

         // Get all items in the list according to the display format.
         PSExecutableSearch searchEx = 
            new PSExecutableSearch(m_appletBase, format, search, getApplet());
         searchEx.addColumnName(IPSHtmlParameters.SYS_PERMISSIONS);
         
         List itemNodes = searchEx.executeSearch(parentFolderNode, true, 
               false);

         removeSystemFolder(itemNodes);
         if(parentFolderNode.getName().equals("Sites")) {
            itemNodes = removeCM1SiteFolders(itemNodes);
         }
         // sort this list before adding to the results - note that the
         // comparator will sort case insensitive (bad constant name).
         Collections.sort(itemNodes, new PSStringComparator(
               PSStringComparator.SORT_CASE_INSENSITIVE_ASC));
         resList.addAll(itemNodes);

         // set the children on the parent node
         parentFolderNode.setChildren(resList.iterator());

         return resList.iterator();
      }
      catch (PSException ex)
      {
         throw new PSContentExplorerException(
               IPSContentExplorerErrors.GENERAL_ERROR, ex.toString());
      }
      catch (PSContentExplorerException ex)
      {
         throw ex;
      }
      finally
      {
         // Call listeners for end of loading
         for (Iterator iter = m_searchListeners.iterator(); iter.hasNext();)
         {
            IPSSearchListener listener = (IPSSearchListener) iter.next();
            listener.searchCompleted(parentFolderNode);
         }
      }
   }

   /**
    * See {@link PSFolderProcessorProxy#getDescendentFolderLocatorsWithoutFilter(PSLocator)}.
    */
   public PSLocator[] getDescendantFoldersWithoutFilter(PSLocator folderId)
      throws PSCmsException
   {
      return m_folderProxy.getDescendentFolderLocatorsWithoutFilter(folderId);
   }
   
   /**
    * Get the path of the supplied node
    * 
    * @param folderNode The folder node to check, assumed not <code>null</code>
    * 
    * @return The path, never <code>null</code> or empty.
    */
   private String getFolderPath(PSNode folderNode)
   {
      StringBuilder folderPath = new StringBuilder();
      PSNode current = folderNode;
      while(current != null && ! current.getType().equals("ROOT"))
      {
         folderPath.insert(0, current.getName());
         folderPath.insert(0, '/');
         PSNavigationTree.PSTreeNode treeNode = current.getAssociatedTreeNode();
         PSNavigationTree.PSTreeNode parentTreeNode = (PSNavigationTree.PSTreeNode) treeNode.getParent();
         current = (PSNode) (parentTreeNode != null ? 
               parentTreeNode.getUserObject() : null);
      }
      folderPath.insert(0, '/');
      
      return folderPath.toString();
   }

   /**
    * Adds the items or folders represented by the supplied node list to the
    * folder represented by the target folders. Grabs the ids from node
    * properties and constructs locators (keys) corresponding to items and
    * folders persisting on server.
    * 
    * @param tgtFolderNode the node representing the target folder, may not be
    *           <code>null</code> and must be of type <code>TYPE_FOLDER</code>
    *           or <code>TYPE_SYS_FOLDER</code> or <code>TYPE_SYS_SITE</code>.
    * @param nodeList list of nodes representing items or folders, may not be
    *           <code>null</code> or empty and must be of type
    *           <code>TYPE_FOLDER</code> only if the target node type is
    *           <code>TYPE_SYS_FOLDER</code> or <code>
    * TYPE_SYS_SITE</code>.
    *           <code>TYPE_ITEM</code> is only allowed for target of type
    *           <code>TYPE_FOLDER</code>
    * 
    * @throws PSCmsException if an error happens while processing the request.
    */
   public void add(PSNode tgtFolderNode, Iterator nodeList)
         throws PSCmsException
   {
      validateNodeAsFolder(tgtFolderNode);
      PSLocator targetLocator = PSActionManager.nodeToLocator(tgtFolderNode);

      List childNodes = PSIteratorUtils.cloneList(nodeList);
      validateChildrenForFolderActions(tgtFolderNode.getType(), childNodes
            .iterator());

      removeFoldersWithExistingNames(tgtFolderNode, childNodes);

      //Get all locators and request proxy to execute add request
      if (!childNodes.isEmpty())
      {
         List childLocators = PSActionManager.nodesToLocators(childNodes
               .iterator());

         m_folderProxy.addChildren(childLocators, targetLocator);
      }
   }

   /**
    * Checks the folders in the supplied list with children of the supplied
    * target folder node and if a matching folder by case-insensitive name
    * comparison is found, it removes that from the supplied list.
    * 
    * @param tgtFolderNode the target parent folder node, assumed not to be
    *           <code>null</code>
    * @param childNodes the list of child nodes to check, assumed not to be
    *           <code>null</code>
    */
   private void removeFoldersWithExistingNames(PSNode tgtFolderNode,
         List childNodes)
   {
      Iterator children = childNodes.iterator();
      while (children.hasNext())
      {
         PSNode child = (PSNode) children.next();
         if (child.isOfType(PSNode.TYPE_FOLDER)
               && containsChildWithName(tgtFolderNode, child.getName()))
         {
            getApplet().displayErrorMessage(null, getClass(),
                  "Ignoring the folder <{0}>, because a folder with that "
                        + "name already exists in the folder <{1}>",
                  new String[] { child.getName(), tgtFolderNode.getName() },
                  "Warning", null);
            children.remove();
         }
      }
   }

   /**
    * Copies the items or folders represented by the supplied node list to the
    * folder represented by the target folder node. Grabs the ids from node
    * properties and constructs locators (keys) corresponding to items and
    * folders persisting on server to execute the request.
    * 
    * @param tgtFolderNode the node representing the target folder, may not be
    *           <code>null</code> and must be of type <code>TYPE_FOLDER</code>
    *           or <code>TYPE_SYS_FOLDER</code> or <code>TYPE_SYS_SITE</code>.
    * @param nodeList list of nodes representing items or folders, may not be
    *           <code>null</code> or empty and must be of type
    *           <code>TYPE_FOLDER</code> only if the target node type is
    *           <code>TYPE_SYS_FOLDER</code> or <code>
    * TYPE_SYS_SITE</code>.
    *           <code>TYPE_ITEM</code> is only allowed for target of type
    *           <code>TYPE_FOLDER</code>
    * 
    * @throws PSCmsException if an error happens while processing the request.
    */
   public void copy(PSNode tgtFolderNode, Iterator nodeList)
         throws PSCmsException
   {
      validateNodeAsFolder(tgtFolderNode);
      PSLocator targetLocator = PSActionManager.nodeToLocator(tgtFolderNode);

      List childNodes = PSIteratorUtils.cloneList(nodeList);
      validateChildrenForFolderActions(tgtFolderNode.getType(), childNodes
            .iterator());

      removeFoldersWithExistingNames(tgtFolderNode, childNodes);

      //Get all locators and request proxy to execute add request
      if (!childNodes.isEmpty())
      {
         List childLocators = PSActionManager.nodesToLocators(childNodes
               .iterator());
         m_folderProxy.copyChildren(childLocators, targetLocator);

         // add dummy, dirty item node so selective refresh will take effect
         tgtFolderNode.addChild(PSNode.createDirtyNewItemNode());
      }
   }

   /**
    * Get a set of all communities found for the supplied source node recursivly
    * down to the bottom.
    * 
    * @param source the source node from which to start the search for all
    *           communities in the tree, not <code>null</code>.
    * @return a set of all community ids found in the supplied source node
    *         recursivly down to the bottom as <code>Integer</code> objects,
    *         never <code>null</code>, may be empty.
    * @throws PSCmsException for any error.
    */
   public Set getFolderCommunities(PSNode source) throws PSCmsException
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");

      return m_folderProxy.getFolderCommunities(PSActionManager
            .nodeToLocator(source));
   }

   /**
    * Copy the tree rooted at <code>source</code> and link it as a child of
    * <code>target</code>, following the supplied options.
    * 
    * @param source the source Site Folder or Site Subfolder which needs to be
    *           cloned, not <code>null</code>.
    * @param target the parent Folder into which the source will be cloned, not
    *           <code>null</code>.
    * @param options the cloning options, not <code>null</code>.
    * @return the name of the log file if there were errors, <code>null</code>
    *         otherwise.
    * @throws PSCmsException for any error.
    */
   public ErrorResults copyFolder(PSNode source, PSNode target,
         PSCloningOptions options) throws PSCmsException
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");

      if (target == null)
         throw new IllegalArgumentException("target cannot be null");

      if (options == null)
         throw new IllegalArgumentException("options cannot be null");

      /*
       * If this is a site clone we must clone the site definition first because
       * the new site id is required for the site mapping by the copyFolder
       * functionality.
       */
      try
      {
         if (source.getType() == PSNode.TYPE_SITE)
         {
            // add and URL encode parameters
            String base = "../sys_pubSites/copySiteDef.xml";
            Map<String, String> queryParams = new HashMap<String, String>(3);
            queryParams.put("sys_originalSiteName", options.getSiteToCopy());
            queryParams.put("sys_newSiteName", options.getSiteName());
            queryParams.put("sys_newFolderPath", normalizePath(m_actionManager
                  .getApplet().getNavTree().getParentPath(source), options
                  .getFolderName()));
            String query = PSUrlUtils.createUrl(base, queryParams.entrySet()
                  .iterator(), null);

            URL url = new URL(m_appletBase, query);
            Document doc = PSXmlDocumentBuilder.createXmlDocument(url
                  .openStream(), false);
            Element root = doc.getDocumentElement();
            String NODE_NAME = "CopySiteDefResults";
            if (!root.getNodeName().equals(NODE_NAME))
            {
               // this should not happen unless the system is broken
               throw new RuntimeException("Received an invalid document. "
                     + "Expected root node=" + NODE_NAME
                     + " but received doc: "
                     + PSXmlDocumentBuilder.toString(doc));
            }

            // we've created a new site, so clear the cache
            getSiteCataloger().refresh();

            boolean success = PSXMLDomUtil.getBooleanData(PSXMLDomUtil
                  .getAttributeTrimmed(root, "success"));
            if (!success)
            {
               ErrorResults results = new ErrorResults();
               results.m_siteCloneErrorText = handleErrorResult(root);

               return results;
            }

            // add the source-target site map to our copy options
            String sourceId = PSXMLDomUtil.getAttributeTrimmed(root,
                  "sourceSiteId");
            String copyId = PSXMLDomUtil
                  .getAttributeTrimmed(root, "copySiteId");
            options.addSiteMapping(new Integer(sourceId), new Integer(copyId));
         }
      }
      catch (Exception e)
      {
         throw new PSCmsException(IPSContentExplorerErrors.GENERAL_ERROR, e
               .getMessage());
      }

      // copy the specified folder
      ErrorResults results = new ErrorResults();
      results.m_logFileName = m_folderProxy.copyFolder(PSActionManager
            .nodeToLocator(source), PSActionManager.nodeToLocator(target),
            options);

      return results;
   }

   /**
    * See {@link PSFolderProcessorProxy#copyFolderSecurity(PSLocator, PSLocator)}.
    */
   public void copyFolderSecurity(PSLocator source, PSLocator target)
      throws PSCmsException
   {
      m_folderProxy.copyFolderSecurity(source, target);
   }
   
   /**
    * A structure to hold 2 pieces of error information to return to the caller.
    */
   class ErrorResults
   {
      /**
       * The name of the log file containing error information generated while
       * copying a folder tree. Will be <code>null</code> if no errors
       * occurred during this phase of the processing.
       */
      public String m_logFileName = null;

      /**
       * If any errors occurred while cloning the associated site, this will
       * contain the message of the exception and a stack trace. Will be
       * <code>null</code> if no errors occurred during this phase of the
       * processing.
       */
      public String m_siteCloneErrorText = null;

      /**
       * Checks if any of the processing phases failed.
       * 
       * @return <code>true</code> if at least 1 phase failed,
       *         <code>false</code> if everything was successful. On failure,
       *         check each member variable for non- <code>null</code>.
       */
      public boolean wasSuccessful()
      {
         return m_logFileName == null && m_siteCloneErrorText == null;
      }
   }

   /**
    * If <code>path</code> is relative, it is normalized and appended onto
    * <code>cwd</code>. Normalization means removing all leading "../" and
    * "./". All "\" are replaced with "/" in both supplied paths before
    * returning. Neither path is checked for well-formedness.
    * 
    * @param cwd Assumed to be a well formed path.
    * @param path Assumed to be a well formed path.
    * @return If <code>path</code> is absolute, it is returned with only the
    *         slashes normalized. Otherwise, any leading .. or . are removed
    *         (and <code>cwd</code> adjusted accordingly) and
    *         <code>path</code> is appended to <code>cwd</code> and
    *         returned.
    * 
    * @throws PSCmsException If the number of parts in cwd aren't sufficient to
    *            normalize all the leading ".."s in path.
    */
   private String normalizePath(String cwd, String path) throws PSCmsException
   {
      String tmpCwd = cwd.replace('\\', '/');
      String tmpPath = path.replace('\\', '/');
      if (tmpPath.startsWith("//"))
         return tmpPath;
      boolean done = false;
      while (!done)
      {
         if (tmpPath.startsWith("./"))
            tmpPath = tmpPath.substring(2, tmpPath.length());
         else if (tmpPath.startsWith("../"))
         {
            tmpPath = tmpPath.substring(3, tmpPath.length());
            int pos = tmpCwd.lastIndexOf('/');
            if (pos != -1)
               tmpCwd = tmpCwd.substring(0, pos);
            else
            {
               String[] args = { cwd, path };
               throw new PSCmsException(
                     IPSContentExplorerErrors.INCOMPATIBLE_PATHS, args);
            }
         }
         else
            done = true;
      }
      if (!tmpCwd.endsWith("/"))
         tmpCwd += "/";
      return tmpCwd + tmpPath;
   }

   /**
    * Looks for a specified format in the <code>root</code> and builds a
    * string containing the error text and stack trace. The expected dtd is as
    * follows:
    * 
    * <pre>
    * 
    *  
    *   &lt;!ELEMENT Error (Message, StackTrace)&gt;
    *   &lt;!ELEMENT Message (#PCDATA)&gt;
    *   &lt;!ELEMENT StackTrace (#PCDATA)&gt;
    *   
    *  
    * </pre>
    * 
    * Error is expected to be the first child of the supplied element.
    * 
    * @param root The document root returned from certain Rx requests. Never
    *           <code>null</code>.
    * @return If an error message is present, the text of the message and stack
    *         trace, otherwise an empty string.
    * @throws PSCmsException If the document contains the expected error
    *            element, but is otherwise malformed.
    */
   private String handleErrorResult(Element root) throws PSCmsException
   {
      try
      {
         Element error = PSXMLDomUtil.getFirstElementChild(root, "Error");
         Element msg = PSXMLDomUtil.getFirstElementChild(error, "Message");
         Element stack = PSXMLDomUtil.getNextElementSibling(msg, "StackTrace");
         return PSXMLDomUtil.getElementData(msg) + "\r\n\r\n"
               + PSXMLDomUtil.getElementData(stack);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSCmsException(e);
      }
   }

   /**
    * Removes the items or folders represented by the supplied node list from
    * the folder represented by the parent node. Grabs the ids from node
    * properties and constructs locators (keys) corresponding to items and
    * folders persisting on server to execute the request.
    * 
    * @param parentNode the node representing the parent folder, may not be
    *           <code>null</code> and must be of type <code>TYPE_FOLDER</code>
    *           or <code>TYPE_SYS_FOLDER</code> or <code>TYPE_SYS_SITE</code>.
    * @param nodeList list of nodes representing items or folders, may not be
    *           <code>null</code> or empty and must be of type
    *           <code>TYPE_FOLDER</code> only if the parent node type is
    *           <code>TYPE_SYS_FOLDER</code> or <code>TYPE_SYS_SITE</code>.
    *           <code>TYPE_ITEM</code> is only allowed for parent of type
    *           <code>TYPE_FOLDER</code>
    *           
    * This will orphan but not purge items other than navigation items. See
    * deleteAllContent to remove all items that are not linked elsewhere.
    * 
    * @param force <code>true</code> to force the action in case child nodes 
    *           are participating in cross site links. <code>false</code> to 
    *           error out in such a case.
    * 
    * @throws PSCmsException if an error happens while processing the request.
    */
   public void delete(PSNode parentNode, Iterator nodeList, boolean force)
           throws PSCmsException, PSNotFoundException {
      validateNodeAsFolder(parentNode);
      PSLocator parentLocator = PSActionManager.nodeToLocator(parentNode);

      List childNodes = PSIteratorUtils.cloneList(nodeList);
      validateChildrenForFolderActions(parentNode.getType(), childNodes
            .iterator());

      //Get all locators and request proxy to execute add request
      List childLocators = PSActionManager.nodesToLocators(childNodes
            .iterator());

      m_folderProxy.removeChildren(parentLocator, childLocators, force);
   }

   /**
    * Removes the items or folders represented by the supplied node list from
    * the folder represented by the parent node. Grabs the ids from node
    * properties and constructs locators (keys) corresponding to items and
    * folders persisting on server to execute the request.  Will cascade and
    * purge all content
    * 
    * @param parentNode the node representing the parent folder, may not be
    *           <code>null</code> and must be of type <code>TYPE_FOLDER</code>
    *           or <code>TYPE_SYS_FOLDER</code> or <code>TYPE_SYS_SITE</code>.
    * @param nodeList list of nodes representing items or folders, may not be
    *           <code>null</code> or empty and must be of type
    *           <code>TYPE_FOLDER</code> only if the parent node type is
    *           <code>TYPE_SYS_FOLDER</code> or <code>TYPE_SYS_SITE</code>.
    *           <code>TYPE_ITEM</code> is only allowed for parent of type
    *           <code>TYPE_FOLDER</code>
    *           
    * @throws PSCmsException if an error happens while processing the request.
    */
   public void purgeAllContent(PSNode parentNode, Iterator nodeList)
         throws PSCmsException
   {
      PSLocator parentLocator = null;
      if (parentNode.isAnyFolderType()) {
         parentLocator = PSActionManager.nodeToLocator(parentNode);
      } else {
         parentLocator = new PSLocator(0);
      }
         
   
      List childNodes = PSIteratorUtils.cloneList(nodeList);
      validateChildrenForPurge(childNodes.iterator());

      //Get all locators and request proxy to execute add request
      List childLocators = PSActionManager.nodesToLocators(childNodes
            .iterator());

      m_folderProxy.purgeFolderAndChildItems(parentLocator, childLocators);
   }
   
   /**
    * Purge all Navon and Navtree items below the passed in folder.
    * @param folder  PSNode for the folder
    * @throws PSCmsException
    */
   public void purgeAllNav(PSNode folder) throws PSCmsException
   {
       validateNodeAsFolder(folder);
       PSLocator folderLoc = PSActionManager.nodeToLocator(folder);
       m_folderProxy.purgeFolderNavigation(folderLoc);
   }

   /**
    * Validates that the supplied node is a valid folder node.
    * 
    * @param folderNode the folder node to validate, may not be <code>null
    * </code>
    *           and must be of type <code>TYPE_FOLDER</code> or <code>
    * TYPE_SYS_FOLDER</code>
    *           or <code>TYPE_SYS_SITE</code>.
    */
   private void validateNodeAsFolder(PSNode folderNode)
   {
      if (folderNode == null)
         throw new IllegalArgumentException("folderNode may not be null.");

      if (!folderNode.isAnyFolderType())
         throw new IllegalArgumentException("folderNode must be a folder.");
   }

   /**
    * Validates that the supplied child nodes can be linked/delinked to/from a
    * target node of the supplied type.
    * 
    * @param targetNodeType the target node type to validate for, assumed not
    *           <code>null</code>.
    * @param childNodes the list of child nodes, may not be <code>null</code>
    *           or empty.
    */
   private void validateChildrenForFolderActions(String targetNodeType,
         Iterator childNodes)
   {
      if (childNodes == null || !childNodes.hasNext())
         throw new IllegalArgumentException(
               "childNodes may not be null or empty.");

      while (childNodes.hasNext())
      {
         PSNode node = (PSNode) childNodes.next();
         if (node.isOfType(PSNode.TYPE_ITEM))
         {
            if (!PSActionManager.isFolderType(targetNodeType))
               throw new IllegalArgumentException("Items can not be "
                     + "linked/delinked to/from system folders or sites");
         }
         else if (!node.isFolderType())
            throw new IllegalArgumentException(
                  "Only Items and folders can be linked/delinked to folders");
      }
   }

   /**
    * Validates that the supplied child nodes can be purged
    * 
    * @param childNodes the list of child nodes, may not be <code>null</code>
    *           or empty.
    */
   private void validateChildrenForPurge(
         Iterator childNodes)
   {
      if (childNodes == null || !childNodes.hasNext())
         throw new IllegalArgumentException(
               "childNodes may not be null or empty.");

      while (childNodes.hasNext())
      {
         PSNode node = (PSNode) childNodes.next();
         if (!node.isOfType(PSNode.TYPE_ITEM) && !node.isFolderType())
         {
               throw new IllegalArgumentException("Only folders and items can be purged");
         }
      }
   }

   /**
    * Moves the items or folders represented by the supplied node list from the
    * folder represented by the source folder node to the folder represented by
    * the target folder node. Grabs the ids from node properties and constructs
    * locators (keys) corresponding to items and folders persisting on server to
    * execute the request.
    * 
    * @param srcFolderNode the node representing the current parent folder of
    *           the supplied node list, may not be <code>null</code> and must
    *           be of type <code>TYPE_FOLDER</code> or
    *           <code>TYPE_SYS_FOLDER</code> or <code>TYPE_SYS_SITE</code>.
    * @param tgtFolderNode the node representing the target folder to which the
    *           the supplied node list need to be moved, may not be
    *           <code>null</code> and must be of type <code>TYPE_FOLDER</code>
    *           or <code>TYPE_SYS_FOLDER</code> or <code>TYPE_SYS_SITE</code>.
    * @param nodeList list of <code>PSNodes</code> representing items or
    *           folders, may not be <code>null</code> or empty and must be of
    *           type <code>TYPE_FOLDER</code> only if the target node type is
    *           <code>TYPE_SYS_FOLDER</code> or <code>TYPE_SYS_SITE</code>.
    *           <code>TYPE_ITEM</code> is only allowed for target of type
    *           <code>TYPE_FOLDER</code>
    *           
    * @param force <code>true</code> to force the action in case child nodes 
    *           are participating in cross site links. <code>false</code> to 
    *           error out in such a case.
    * 
    * @throws PSCmsException if an error happens while processing the request.
    *            If errors happen while attempting to update the site folder
    *            paths, all processing is completed and all errors are grouped
    *            together in 1 message.
    */
   public void move(PSNode srcFolderNode, PSNode tgtFolderNode,
         Iterator nodeList, boolean force) throws PSCmsException
   {
      //check source folder node
      validateNodeAsFolder(srcFolderNode);
      PSLocator srcLocator = PSActionManager.nodeToLocator(srcFolderNode);

      //check target folder node
      validateNodeAsFolder(tgtFolderNode);
      PSLocator tgtLocator = PSActionManager.nodeToLocator(tgtFolderNode);

      List childNodes = PSIteratorUtils.cloneList(nodeList);
      validateChildrenForFolderActions(tgtFolderNode.getType(), childNodes
            .iterator());

      removeFoldersWithExistingNames(tgtFolderNode, childNodes);

      Collection<String> errors = new ArrayList<String>();
      //Get all locators and request proxy to execute add request
      if (!childNodes.isEmpty())
      {
         List childLocators = PSActionManager.nodesToLocators(childNodes
               .iterator());
         m_folderProxy.moveChildren(srcLocator, childLocators, tgtLocator,
            force);
         Iterator children = childNodes.iterator();
         while (children.hasNext())
         {
            PSNode node = (PSNode) children.next();
            if (node.getType() == PSNode.TYPE_SITE 
                  && !(tgtFolderNode.getType() == PSNode.TYPE_SITE
                     || tgtFolderNode.getType() == PSNode.TYPE_SITESUBFOLDER))
            {
               PSNavigationTree tree = m_actionManager.getApplet()
                     .getNavTree();
               String srcPath = 
                     tree.getPath(srcFolderNode) + "/" + node.getName();
               String tgtPath = 
                     tree.getPath(tgtFolderNode) + "/" + node.getName();
               String result = updateSiteDefinitions(srcPath, tgtPath);
               if (result != null)
               {
                  errors.add(result);
               }
            }
         }
      }

      // Update any indicator in the action bar
      runSearchCompleted(srcFolderNode);

      if (errors.size() > 0)
      {
         String errorText = "";
         for (Iterator iter = errors.iterator(); iter.hasNext();)
         {
            errorText += iter.hasNext();
            errorText += "\r\n";
         }
         throw new PSCmsException(
               IPSContentExplorerErrors.SITEDEF_UPDATE_FAILURES, errorText);
      }
   }

   /**
    * Makes a request to the Rx server asking that all site definition objects
    * that have a folder path of <code>sourcePath</code> be changed to have a
    * folder path of <code>targetPath</code>. Each path should be of the form
    * "//a/b/c".
    * 
    * @param sourcePath The original folder path. Assumed not <code>null</code>
    *           or empty.
    * @param targetPath The new folder path. Assumed not <code>null</code> or
    *           empty.
    * 
    * @return <code>null</code> if successful, otherwise, text describing the
    *         error and stack trace.
    * @throws PSCmsException 
    */
   private String updateSiteDefinitions(String sourcePath, String targetPath)
         throws PSCmsException
   {
      String error = null;
      try
      {
         String query = "../sys_pubSites/modifySiteDefs.xml";
         query += "?sys_originalPath=" + URLEncoder.encode(sourcePath, "UTF8");
         query += "&sys_newPath=" + URLEncoder.encode(targetPath, "UTF8");
         URL url = new URL(m_appletBase, query);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(
               url.openStream(), false);
         Element root = doc.getDocumentElement();
         //sites were modified, so clear cache
         getSiteCataloger().refresh();
         String NODE_NAME = "ModifySiteDefsResults";
         if (!root.getNodeName().equals(NODE_NAME))
         {
            //this should not happen unless the system is broken
            throw new RuntimeException(
                  "Received an invalid document. Expected " + "root node="
                        + NODE_NAME + " but received doc: "
                        + PSXmlDocumentBuilder.toString(doc));
         }
         boolean success = PSXMLDomUtil.getBooleanData(PSXMLDomUtil
               .getAttributeTrimmed(root, "success"));
         if (!success)
         {
            error = handleErrorResult(root);
         }
      }
      catch (Exception e)
      {
         throw new PSCmsException(IPSContentExplorerErrors.GENERAL_ERROR, e
               .getMessage());
      }
      return error;
   }

   /**
    * Checks the parent folder node whether this contains a child with the
    * supplied folder name.
    * 
    * @param parentFolderNode the parent folder node to check, assumed not to be
    *           <code>null</code>
    * @param folderName the folder name to check, assumed not to be <code>null
    * </code>or empty.
    * 
    * @return <code>true</code> if a folder with that name exists in the
    *         loaded children (current cache) of the parent folder, otherwise
    *         <code>false</code>
    */
   private boolean containsChildWithName(PSNode parentFolderNode,
         String folderName)
   {
      Iterator children = parentFolderNode.getChildren();
      if (children != null)
      {
         while (children.hasNext())
         {
            PSNode child = (PSNode) children.next();
            if (child.isOfType(PSNode.TYPE_FOLDER))
            {
               if (child.getName().equalsIgnoreCase(folderName))
               {
                  return true;
               }
            }
         }
      }

      return false;
   }
   
   public PSContentExplorerApplet getApplet()
   {
      return m_actionManager.getApplet();
   }

   /**
    * Gets the display formats available for folders.
    * 
    * @return the list of display formats, never <code>null</code> or empty.
    */
   public Iterator getDisplayFormats()
   {
      PSDisplayFormatCatalog dispFormatCatalog = m_actionManager
            .getDisplayFormatCatalog();

      return dispFormatCatalog.getFolderDisplayFormats();
   }

   /**
    * @return cached Community cataloger, never <code>null</code> or empty.
    * @throws PSCmsException if an error happens while processing the request.
    */
   public PSCommunityCataloger getCommunityCataloger() throws PSCmsException
   {
      return m_actionManager.getCommunityCataloger();
   }

   /**
    * @return cached Locale cataloger, never <code>null</code> or empty.
    * @throws PSCmsException if an error happens while processing the request.
    */
   public PSLocaleCataloger getLocaleCataloger() throws PSCmsException
   {
      return m_actionManager.getLocaleCataloger();
   }

   /**
    * @return cached Role cataloger, never <code>null</code>.
    * @throws PSCmsException if an error happens while processing the request.
    */
   public PSRoleCataloger getRoleCataloger() throws PSCmsException
   {
      return m_actionManager.getRoleCataloger();
   }

   /**
    * @return cached Subject / user cataloger, never <code>null</code>.
    * @throws PSCmsException if an error happens while processing the request.
    */
   public PSSubjectCataloger getSubjectCataloger() throws PSCmsException
   {
      return m_actionManager.getSubjectCataloger();
   }

   /**
    * @return cached security provider cataloger, never <code>null</code>.
    * @throws PSCmsException if an error happens while processing the request.
    */
   public PSSecurityProviderCataloger getSecurityProviderCataloger()
         throws PSCmsException
   {
      return m_actionManager.getSecurityProviderCataloger();
   }

   /**
    * Get the global template cataloger.
    * 
    * @return the cached global template cataloger, never <code>null</code>.
    * @throws PSCmsException if an error happens while processing the request.
    */
   public PSGlobalTemplateCataloger getGlobalTemplateCataloger()
         throws PSCmsException
   {
      return m_actionManager.getGlobalTemplateCataloger();
   }

   /**
    * Get the site name cataloger.
    * 
    * @return the cached site name cataloger, never <code>null</code>.
    * @throws PSCmsException if an error happens while processing the request.
    */
   public PSSiteCataloger getSiteCataloger() throws PSCmsException
   {
      return m_actionManager.getSiteCataloger();
   }

   /**
    * Gets the displayformat identified by the supplied format id from the
    * avialable list of folder formats.
    * 
    * @param formatid id of the display format to get, may not be <code>null
    * </code> or empty.
    * @param getDefault supply <code>true</code> to get the first one in the
    *           available list if the supplied formatid is not found.
    * 
    * @return the display format, may be <code>null</code> if the getDefault
    *         is <code>false</code>
    */
   public PSDisplayFormat getDisplayFormatById(String formatid,
         boolean getDefault)
   {
      if (formatid == null || formatid.trim().length() == 0)
         throw new IllegalArgumentException(
               "formatid may not be null or empty.");

      PSDisplayFormat match = null;
      Iterator iter = getDisplayFormats();
      while (iter.hasNext())
      {
         PSDisplayFormat format = (PSDisplayFormat) iter.next();
         String id = String.valueOf(format.getDisplayId());
         if (id.equals(formatid))
         {
            match = format;
            break;
         }
      }

      if (match == null && getDefault)
         match = (PSDisplayFormat) getDisplayFormats().next();

      return match;
   }

   /**
    * Helper method to convert a <code>PSFolder</code> object to <code>PSNode
    * </code>
    * object. The converted node is of type <code>TYPE_FOLDER</code>, with
    * name and label as folder name and is set with content if, revision and
    * community id properties.
    * 
    * @param folder the folder to convert, may not be <code>null</code>
    * 
    * @return the node, never <code>null</code>
    */
   public static PSNode folderToNode(PSFolder folder)
   {
      if (folder == null)
         throw new IllegalArgumentException("folder may not be null.");

      PSNode node = new PSNode(folder.getName(), folder.getName(),
            PSNode.TYPE_FOLDER, "", null, false, folder.getPermissions()
                  .getPermissions());
      PSProperties props = new PSProperties();
      props.setProperty(IPSConstants.PROPERTY_CONTENTID, Integer
            .toString(folder.getLocator().getId()));
      props.setProperty(IPSConstants.PROPERTY_REVISION, Integer
            .toString(folder.getLocator().getRevision()));
      props.setProperty(IPSConstants.PROPERTY_COMMUNITY, Integer
            .toString(folder.getCommunityId()));
      node.setProperties(props);

      return node;
   }

   /**
    * Helper method to convert from <code>PSNode</code> object to
    * <code>PSFolder</code> object.
    * 
    * @param node the node to convert may not be <code>null</code> and must
    *           have properties for content id and community.
    * 
    * @return The returned folder is a partial representation of the folder as
    *         it exists in the db. Only the name and locator are valid values,
    *         never <code>null</code>
    */
   public static PSFolder nodeToFolder(PSNode node)
   {
      if (node == null)
         throw new IllegalArgumentException("node must not be null");

      int commid = -1;
      int contid = -1;
      try
      {
         contid = Integer.parseInt(node.getContentId());
      }
      catch (NumberFormatException e)
      {
         throw new IllegalArgumentException("Invalid node as a folder - "
               + node);
      }

      return new PSFolder(node.getName(), contid, commid, node.getPermissions()
            .getPermissions(), "");
   }

   /**
    * Convenience method to convert a list of <code>PSNode</code> objects to
    * <code>PSFolder</code> objects.
    * 
    * @param nodeList the list of nodes to convert, may not be <code>null</code>
    * 
    * @return the list of <code>PSFolder</code>s, never <code>null</code>,
    *         may be empty if the supplied list is empty.
    */
   public static List<PSFolder> nodesToFolders(Iterator nodeList)
   {
      if (nodeList == null)
         throw new IllegalArgumentException("nodeList must not be null");

      List<PSFolder> list = new ArrayList<PSFolder>();
      while (nodeList.hasNext())
      {
         list.add(nodeToFolder((PSNode) nodeList.next()));
      }
      return list;
   }

   /**
    * Convenience method to convert a list of PSFolder objects to PSNode
    * objects.
    * 
    * @param folderList must not be <code>null</code>
    * @return iterator of converted nodes, never <code>null</code>
    */
   /**
    * Convenience method to convert a list of <code>PSFolder</code> objects to
    * <code>PSNode</code> objects. See {@link #folderToNode(PSFolder)}for
    * description of converted objects.
    * 
    * @param folderList the list of folders to convert, may not be <code>null
    * </code>
    * 
    * @return the list of <code>PSNode</code>s, never <code>null</code>,
    *         may be empty if the supplied list is empty.
    */
   public static Iterator<PSNode> foldersToNodes(Iterator folderList)
   {
      if (folderList == null)
         throw new IllegalArgumentException("folderList must not be null");

      List<PSNode> list = new ArrayList<PSNode>();
      while (folderList.hasNext())
      {
         list.add(folderToNode((PSFolder) folderList.next()));
      }
      return list.iterator();
   }

   /**
    * Get the folder type for the supplied folder summary.
    * 
    * @param folder the folder summary for which to get the type, never
    *           <code>null</code>.
    * @param parent the parent node of the supplied folder, never
    *           <code>null</code>.
    * @return the folder type, always one of the <code>PSNode.TYPE_xxx</code>
    *         values that are folder types.
    */
   public String getFolderType(PSComponentSummary folder, PSNode parent)
   {
      if (folder == null)
         throw new IllegalArgumentException("folder cannot be null");
      if (parent == null)
         throw new IllegalArgumentException("parent node cannot be null");

      return getFolderType(folder.getContentId(), folder.getName(), parent);
   }
   
   /**
    * Get the folder type for the specified folder.
    * 
    * @param folderId The id of the folder.
    * @param folderName The name of the folder, may not be <code>null</code>
    * or empty.
    * @param parent the parent node of the supplied folder, never
    * <code>null</code>.
    * 
    * @return the folder type, always one of the <code>PSNode.TYPE_xxx</code>
    * values that are folder types.
    */
   public String getFolderType(int folderId, String folderName, PSNode parent)
   {
      if (parent == null)
         throw new IllegalArgumentException("parent node cannot be null");
      
      if (StringUtils.isBlank(folderName))
         throw new IllegalArgumentException(
            "folderName may not be null or empty");
      
      String type = PSNode.TYPE_FOLDER;
      try
      {
         int id = folderId;
         if (id == PSFolder.ROOT_ID)
            type = PSNode.TYPE_FOLDER;
         else if (id == PSFolder.SYS_SITES_ID)
            type = PSNode.TYPE_SYS_SITES;
         else if (id == PSFolder.SYS_FOLDERS_ID)
            type = PSNode.TYPE_SYS_FOLDERS;
         else if (parent.isSiteFolder() || parent.isSiteSubfolder())
            type = PSNode.TYPE_SITESUBFOLDER;
         else
         {
            PSNavigationTree navtree = 
               m_actionManager.getApplet().getNavTree();
            if (navtree == null)
            {
               // not in the main applet, so don't try to build the folder path
               
               return type;
            }
            
            String path = getFolderPath(parent);
            if (!path.endsWith("/"))
               path += "/";
            path += folderName;
            Iterator sites = getSiteCataloger().getSites().iterator();
            while (sites.hasNext())
            {
               PSSite site = (PSSite) sites.next();
               String sitePath = site.getFolderRoot();
               sitePath = sitePath.replace('\\', '/');
               if (sitePath.endsWith("/"))
                  sitePath = sitePath.substring(0, sitePath.length() - 1);
               if (path.equalsIgnoreCase(sitePath))
                  return PSNode.TYPE_SITE;
            }
         }
      }
      catch (PSCmsException e)
      {
         // should never happen, ignore
      }

      return type;      
   }

   /**
    * Determines the folder icon key for the given type.
    * 
    * @param type the folder type for which to get the icon key, one of the
    *           <code>PSNode.TYPE_xxx</code> values that are folders.
    * @return the folder icon key for the supplied type, never <code>null</code>
    *         or empty.
    */
   public static String getFolderIconKey(String type)
   {
      String iconKey = null;
      if (PSNode.TYPE_FOLDER.equals(type))
         iconKey = "Folder";
      else if (PSNode.TYPE_SITE.equals(type))
         iconKey = "Site";
      else if (PSNode.TYPE_SITESUBFOLDER.equals(type))
         iconKey = "SiteSubfolder";
      else if (PSNode.TYPE_SYS_FOLDERS.equals(type))
         iconKey = "Folders";
      else if (PSNode.TYPE_SYS_SITES.equals(type))
         iconKey = "Sites";

      if (iconKey == null)
      {
         throw new IllegalArgumentException("Invalid type supplied. Must be "
               + "one of the PSNode.TYPE_xxx values that indicate some sort "
               + "of folder.");
      }

      return iconKey;
   }

   /**
    * If this field is <code>true</code>, then this manager should be using a
    * fast expand on folder children nodes. This mode will not fully load any
    * non-folder children. Used in applet startup to speed the expansion of the
    * folder hierarchy. Always <code>false</code> after startup completes.
    * 
    * @return Returns the quickExpand state
    */
   public boolean isQuickExpand()
   {
      return m_quickExpand;
   }

   /**
    * Set a new quick expand state, see {@link #isQuickExpand()}for details.
    * 
    * @param quickExpand The new quickExpand state to set.
    */
   public void setQuickExpand(boolean quickExpand)
   {
      m_quickExpand = quickExpand;
   }

   /**
    * Add a search listener to the list of search listeners held for this
    * manager.
    * 
    * @param l a search listener, must never be <code>null</code>
    */
   public void addSearchListener(IPSSearchListener l)
   {
      if (l == null)
      {
         throw new IllegalArgumentException("l must never be null");
      }
      m_searchListeners.add(l);
   }

   /**
    * Runs the search completed methods of any search listeners to updated item
    * indicators.
    * 
    * @param current The current node being manipulated, must never be
    *           <code>null</code>
    */
   public void runSearchCompleted(PSNode current)
   {
      if (current == null)
      {
         throw new IllegalArgumentException("current must never be null");
      }
      Iterator iter = m_searchListeners.iterator();
      while (iter.hasNext())
      {
         IPSSearchListener listener = (IPSSearchListener) iter.next();
         listener.searchCompleted(current);
      }

   }
   
   /**
    * @return the folder proxy, never <code>null</code>.
    */
   public PSFolderProcessorProxy getFolderProxy()
   {
      return m_folderProxy;
   }

   /**
    * Component processor proxy. Initialized in the constructor, never
    * <code>null</code> or modified after that.
    */
   private PSComponentProcessorProxy m_componentProxy = null;

   /**
    * Folder processor proxy. Initialized in the constructor, never
    * <code>null</code> or modified after that.
    */
   private PSFolderProcessorProxy m_folderProxy = null;

   /**
    * Action manager, which created this FolderActionMgr, never
    * <code>null</code>, after the ctor is finished.
    */
   private PSActionManager m_actionManager = null;

   /**
    * The base url of the applet, initialized in the constructor, never <code>
    * null</code> or modified after that.
    */
   private URL m_appletBase = null;

   /**
    * See {@link #isQuickExpand()}for an explanation of this field.
    */
   private boolean m_quickExpand = false;

   /**
    * List of search listeners, never <code>null</code>, but could be empty.
    * Added to by {@link #addSearchListener(IPSSearchListener)}and used by
    * {@link #runSearchCompleted(PSNode)}. Note that there is no current
    * mechanism to remove listeners.
    */
   private Collection<IPSSearchListener> m_searchListeners = 
      new ArrayList<IPSSearchListener>();

   /**
    * The name of the component type for folders to use with the processor
    * proxy.
    */
   public static final String ms_folderCompType = PSFolder
         .getComponentType(PSFolder.class);

   private static List<String> cm1SiteRootFolder = new ArrayList<>();

   public static void addCM1SiteRootFolder(String folder){
      if(folder != null && folder.startsWith("//Sites/")){
         folder = folder.replace("//Sites/","");
      }
      if(!cm1SiteRootFolder.contains(folder)){
         cm1SiteRootFolder.add(folder);
      }
   }

   public static List<PSNode> removeCM1SiteFolders(List<PSNode> childrenNodes){
      List newChildrenList = new ArrayList();
      for(PSNode child:childrenNodes){
         if(!cm1SiteRootFolder.contains(child.getName())){
            newChildrenList.add(child);
         }
      }
      return newChildrenList;
   }

}
