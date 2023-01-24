/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.cx;

import com.percussion.border.PSFocusBorder;
import com.percussion.cms.PSActionVisibilityChecker;
import com.percussion.cms.PSActionVisibilityGlobalState;
import com.percussion.cms.PSActionVisibilityObjectState;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionVisibilityContexts;
import com.percussion.cms.objectstore.PSCloningOptions;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderPermissions;
import com.percussion.cms.objectstore.PSObjectPermissions;
import com.percussion.cms.objectstore.PSProcessorProxy;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.PSSecurityProviderCataloger;
import com.percussion.cms.objectstore.client.PSRemoteCataloger;
import com.percussion.cx.catalogers.PSCommunityCataloger;
import com.percussion.cx.catalogers.PSCommunityContentTypeMapperCataloger;
import com.percussion.cx.catalogers.PSGlobalTemplateCataloger;
import com.percussion.cx.catalogers.PSLocaleCataloger;
import com.percussion.cx.catalogers.PSRoleCataloger;
import com.percussion.cx.catalogers.PSSiteCataloger;
import com.percussion.cx.catalogers.PSSubjectCataloger;
import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.cx.javafx.PSBrowserUtils;
import com.percussion.cx.javafx.PSDesktopExplorerWindow;
import com.percussion.cx.javafx.PSFileSaver;
import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.cx.objectstore.PSParameters;
import com.percussion.cx.objectstore.PSProperties;
import com.percussion.cx.wizards.PSCommunityMappingsPage;
import com.percussion.cx.wizards.PSContentCopyOptionsPage;
import com.percussion.cx.wizards.PSCopySiteCopyOptionsPage;
import com.percussion.cx.wizards.PSCopySiteNamePage;
import com.percussion.cx.wizards.PSCopySiteSubfolderCopyOptionsPage;
import com.percussion.cx.wizards.PSCopySiteSubfolderNamePage;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSAboutDialog;
import com.percussion.guitools.UTBrowserControl;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;
import com.percussion.search.PSSearchFieldFilterMap;
import com.percussion.tools.help.PSJavaHelp;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSFormatVersion;
import com.percussion.util.PSHttpConnection;
import com.percussion.util.PSHttpUtils;
import com.percussion.util.PSLineBreaker;
import com.percussion.util.PSRemoteAppletRequester;
import com.percussion.util.PSURLEncoder;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.collections.PSIteratorUtils;
import com.percussion.wizard.PSWizardStartFinishPanel;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import javafx.application.Platform;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A manager that handles all actions. This includes retrieving dynamic menus,
 * regular menus, and executing actions that each menu item describes.
 */
public class PSActionManager implements IPSConstants, IPSSelectionListener
{

   private PSDesktopExplorerWindow dceWindow = null;
   
   static Logger log = Logger.getLogger(PSActionManager.class);

   /**
    * Constructs the action manager with supplied parameters, a reference back
    * to the parent applet.
    *
    * @param applet the applet for which actions are to be done, may not be
    *           <code>null</code>
    */
   public PSActionManager(PSContentExplorerApplet applet) throws PSContentExplorerException, PSCmsException
   {
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");

      applet.getHttpConnection().setBoundary(PSHttpConnection.CX_BOUNDARY);

      m_applet = applet;
     
      if (applet!=null && m_applet.getParentFrame() instanceof PSDesktopExplorerWindow)
         dceWindow = (PSDesktopExplorerWindow)m_applet.getParentFrame(); 
      
      
      
      // Initialize clipboard
      m_clipBoard = new PSClipBoard();

      // Initialize remote proxy and cataloger
      URL baseUrl = m_applet.getRhythmyxCodeBase();
      PSRemoteAppletRequester remoteReq = new PSRemoteAppletRequester(applet.getHttpConnection(), baseUrl);
      m_componentProxy = new PSComponentProcessorProxy(PSProcessorProxy.PROCTYPE_REMOTE, remoteReq);

      m_relationshipProxy = new PSRelationshipProcessorProxy(PSProcessorProxy.PROCTYPE_REMOTE, remoteReq);

      m_remCataloger = new PSRemoteCataloger(remoteReq);

      // initialize/load search and display formats
      ms_dfCatalog = new PSDisplayFormatCatalog(m_componentProxy, m_applet.getUserInfo(), m_applet);

      // initialize/load action visibility context maps
      loadActionVisibilityContexts();

      // initialize all sub-managers required to fulfill the actions
      m_folderMgr = new PSFolderActionManager(this, baseUrl);
      m_irsManager = new PSItemRelationshipsManager(m_relationshipProxy, m_folderMgr, m_remCataloger, baseUrl, m_applet);

      m_searchViewMgr = new PSSearchViewActionManager(m_componentProxy, m_remCataloger, baseUrl, m_applet);

      if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_IA) || m_applet.getView().equals(PSUiMode.TYPE_VIEW_RC))
      {
         m_searchViewMgr.setSearchMode(PSSearchViewActionManager.MODE_IASEARCH);
      }

      // Proactively refresh the site catalog in case site data has been
      // modified from outside of the applet.
      if (ms_siteCataloger != null)
         ms_siteCataloger.refresh();
   }

   /**
    * This is the implementation of {@link IPSSelectionListener}; (MainView
    * fires this notification).
    */
   @Override
   public void selectionChanged(PSSelection sel)
   {
      if (sel == null)
         throw new IllegalArgumentException("selection may not be null");

      if (sel instanceof PSNavigationalSelection)
         m_navSelectionPath = ((PSNavigationalSelection) sel).getSelectionPath();
   }

   /**
    * Initializes the action map and visibility context maps and maps visibility
    * contexts for each action.
    *
    * @throws PSContentExplorerException if an error happens loading action
    *            components.
    */
   private void loadActionVisibilityContexts() throws PSContentExplorerException
   {
      try
      {
         m_visibilityContextsMap = new HashMap<>();

         Document doc = getXMLDocument("../sys_cxSupport/ActionVisibilityContexts.html?compressedvc=yes");
         NodeList actions = doc.getElementsByTagName("Action");
         for (int i = 0; actions != null && i < actions.getLength(); i++)
         {
            Element action = (Element) actions.item(i);
            String key = action.getAttribute("actionid");

            NodeList nodes = action.getElementsByTagName("vc");
            if (nodes == null || nodes.getLength() == 0)
               continue;

            Element contextsElem = (Element) nodes.item(0);

            PSActionVisibilityContexts contexts = m_visibilityContextsMap.get(key);
            if (contexts == null)
            {
               contexts = createVisibilityContext(contextsElem);
               m_visibilityContextsMap.put(key, contexts);
            }
            else
            {
               Iterator ctxts = createVisibilityContext(contextsElem).iterator();

               while (ctxts.hasNext())
                  contexts.add((IPSDbComponent) ctxts.next());
            }
         }
      }
      catch (Exception ex)
      {
         throw new PSContentExplorerException(IPSContentExplorerErrors.GENERAL_ERROR, ex.getLocalizedMessage());
      }
   }

   /**
    * Convenient method to create visibility contexts from a compressed
    * visibility context element.
    *
    * @return PSActionVisibilityContexts may be empty but never
    *         <code>null</code>.
    */
   private PSActionVisibilityContexts createVisibilityContext(Element vcElem)
   {
      PSActionVisibilityContexts vcs = new PSActionVisibilityContexts();
      String name = vcElem.getAttribute("name");
      String value = PSXmlTreeWalker.getElementData(vcElem);
      String[] values = StringUtils.split(value, ",");
      if (value.indexOf("#RXC#") != -1)
      {
         for (int i = 0; i < values.length; i++)
         {

            if (values[i].indexOf("#RXC#") != -1)
            {
               values[i] = values[i].replace("#RXC#", ",");
            }
         }
      }
      vcs.addContext(name, values);
      return vcs;
   }

   /**
    * Loads all the slot definitions on demand. The map is only loaded the first
    * time this is called.
    */
   private void loadSlotDefinitions() throws PSContentExplorerException
   {
      // if already loaded, just return
      if (m_slotDefMap != null)
         return;

      m_slotDefMap = new HashMap<String, List<ContentIdVariantId>>();
      try
      {
         String sdUrl = "../sys_cxItemAssembly/slotcontent.xml";
         Element root = getXMLDocument(sdUrl).getDocumentElement();

         Element slotEl = PSXMLDomUtil.getFirstElementChild(root);
         while (slotEl != null && slotEl.getNodeName().equals("slot"))
         {
            String slotId = PSXMLDomUtil.checkAttribute(slotEl, "slotid", true);

            List<ContentIdVariantId> dataList = new ArrayList<ContentIdVariantId>();

            Element ctEl = PSXMLDomUtil.getFirstElementChild(slotEl);
            while (ctEl != null && ctEl.getNodeName().equals("contenttype"))
            {
               String cId = PSXMLDomUtil.checkAttribute(ctEl, "contenttypeid", true);

               Element varEl = PSXMLDomUtil.getFirstElementChild(ctEl);
               while (varEl != null && varEl.getNodeName().equals("variant"))
               {

                  String vId = PSXMLDomUtil.checkAttribute(varEl, "variantid", true);

                  ContentIdVariantId data = new ContentIdVariantId(cId, vId);
                  dataList.add(data);

                  varEl = PSXMLDomUtil.getNextElementSibling(varEl);
               }
               ctEl = PSXMLDomUtil.getNextElementSibling(ctEl);
            }
            m_slotDefMap.put("" + slotId, dataList);

            slotEl = PSXMLDomUtil.getNextElementSibling(slotEl);
         }
      }
      catch (Exception e)
      {
         /**
          * @todo handle error properly and I18n.
          */
         throw new PSContentExplorerException(1000, e.getMessage());
      }
   }

   /**
    * Gets the cataloger for display formats that provides helper methods to
    * access the display formats.
    *
    * @return the cataloger, never <code>null</code>
    * @throws IllegalStateException if the catalog is not yet initialized. The
    *            cataloger is initialized when the first instance of this class
    *            is loaded.
    */
   public PSDisplayFormatCatalog getDisplayFormatCatalog()
   {
      if (ms_dfCatalog == null)
         throw new IllegalStateException("Display format catalog is not yet initialized");

      return ms_dfCatalog;
   }

   /**
    * Lazily loads and caches the Community cataloger.
    *
    * @return the community cataloger, never <code>null</code>.
    * @throws PSCmsException if the catalog fails to fetch the data from server
    */
   public PSCommunityCataloger getCommunityCataloger() throws PSCmsException
   {
      if (m_communityCataloger == null)
      {
         m_communityCataloger = new PSCommunityCataloger(m_applet.getRhythmyxCodeBase());
      }

      return m_communityCataloger;
   }

   /**
    * Lazily loads and caches the community / content type mapper.
    *
    * @return the caches mapper, never <code>null</code>.
    *
    * @throws PSCmsException if catalog the mapper failed.
    */
   public PSCommunityContentTypeMapperCataloger getCommunityContentTypeMapper() throws PSCmsException
   {
      if (m_commCtMapper == null)
      {
         m_commCtMapper = new PSCommunityContentTypeMapperCataloger(m_applet.getRhythmyxCodeBase());
      }

      return m_commCtMapper;
   }

   /**
    * Lazily loads and caches the Locale cataloger.
    *
    * @return the locale cataloger, never <code>null</code>.
    * @throws PSCmsException if the catalog fails to fetch the data from server
    */
   public PSLocaleCataloger getLocaleCataloger() throws PSCmsException
   {
      if (m_localeCataloger == null)
      {
         m_localeCataloger = new PSLocaleCataloger(m_applet.getRhythmyxCodeBase());
      }

      return m_localeCataloger;
   }

   /**
    * Lazily loads and caches the Role cataloger.
    *
    * @return the role cataloger, never <code>null</code>.
    * @throws PSCmsException if the catalog fails to fetch the data from server
    */
   public PSRoleCataloger getRoleCataloger() throws PSCmsException
   {
      if (m_roleCataloger == null)
      {
         m_roleCataloger = new PSRoleCataloger(m_applet.getRhythmyxCodeBase());
      }

      return m_roleCataloger;
   }

   /**
    * Lazily loads and caches the Subject cataloger.
    *
    * @return the subject cataloger, never <code>null</code>.
    * @throws PSCmsException if the catalog fails to fetch the data from server
    */
   public PSSubjectCataloger getSubjectCataloger() throws PSCmsException
   {
      if (m_subjectCataloger == null)
      {
         m_subjectCataloger = new PSSubjectCataloger(m_applet.getRhythmyxCodeBase());
      }

      return m_subjectCataloger;
   }

   /**
    * Lazily loads and caches the Security Provider Cataloger.
    *
    * @return the subject cataloger, never <code>null</code>
    * @throws PSCmsException if the catalog fails to fetch the data from server
    */
   public PSSecurityProviderCataloger getSecurityProviderCataloger() throws PSCmsException
   {
      if (m_securityProviderCataloger == null)
      {
         m_securityProviderCataloger = new PSSecurityProviderCataloger(m_applet.getRhythmyxCodeBase());
      }

      return m_securityProviderCataloger;
   }

   /**
    * Lazily loads and caches the global template cataloger.
    *
    * @return the global template cataloger, never <code>null</code>.
    * @throws PSCmsException if the catalog fails to fetch the data from the
    *            server.
    */
   public PSGlobalTemplateCataloger getGlobalTemplateCataloger() throws PSCmsException
   {
      if (ms_globalTemplateCataloger == null)
         ms_globalTemplateCataloger = new PSGlobalTemplateCataloger(m_applet.getRhythmyxCodeBase());

      return ms_globalTemplateCataloger;
   }

   /**
    * Lazily loads and caches the site name cataloger.
    *
    * @return the site name cataloger, never <code>null</code>.
    * @throws PSCmsException if the catalog fails to fetch the data from the
    *            server.
    */
   public PSSiteCataloger getSiteCataloger() throws PSCmsException
   {
      if (ms_siteCataloger == null)
      {
         ms_siteCataloger = new PSSiteCataloger(m_applet.getRhythmyxCodeBase());
      }

      return ms_siteCataloger;
   }

   /**
    * Delegates the request to search view manager to fulfill the request. See
    * {@link PSSearchViewActionManager#initContentIdSearch(PSNode, String)} for
    * more information.
    *
    * @param searchNode the new search node to initialize, may not be <code>null
    * </code> and must have search id defined.
    * @param contentid the contentid to use as search criteria, may not be
    *           <code>null</code> or empty.
    */
   public void initContentIdSearch(PSNode searchNode, String contentid)
   {
      m_searchViewMgr.initContentIdSearch(searchNode, contentid);
      addNewSearchNode();
      informListeners(PSIteratorUtils.iterator(searchNode));
   }

   /**
    * Gets the remote cataloger used to make any catalog requests to the server.
    *
    * @return the cataloger, never <code>null</code>
    */
   public PSRemoteCataloger getRemoteCataloger()
   {
      return m_remCataloger;
   }

   /**
    * Gets the clip board associated with this manager.
    *
    * @return the clip board, never <code>null</code>
    */
   public PSClipBoard getClipBoard()
   {
      return m_clipBoard;
   }

   /**
    * Gets the manager that handles the item relationships. Currently supports
    * getting children or parents of an item or folder for a specific
    * relationship.
    *
    * @return the manager, never <code>null</code>
    */
   public PSItemRelationshipsManager getRelationshipsManager()
   {
      return m_irsManager;
   }

   /**
    * Adds the listener to its list of listeners that are interested in action
    * result.
    *
    * @param listener interested listener, may not be <code>null</code>
    *
    */
   public void addActionListener(IPSActionListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null.");

      m_actionListeners.add(listener);
   }

   /**
    * Makes a query to the server to refresh the item/folder/category identified
    * by the supplied node and merges the current supplied node with the
    * refreshed node got from the server and returns the node.
    *
    * @param node node to refresh, may not be <code>null</code>
    *
    * @return the node after refreshed, may be <code>null</code> if the node
    *         does not exist any more on the server.
    */
   public PSNode refresh(PSNode node)
   {
      if (node == null)
         throw new IllegalArgumentException("node must not be null");

      // Load/Reload the children if it supports dynamic children.
      if (node.hasDynamicChildren())
         loadChildren(node);

      return node;
   }

   /**
    * Gets the context menu that can be shown as sub-menu for the supplied
    * parent action. Sets with its child actions and returns the same parent
    * action. If no child actions are found, sets the 'No Entries' as child
    * action. Displays an error message if an exception happens.
    *
    * @param action the action object that represents TYPE_MENU or
    *           TYPE_CONTEXTMENU for which the sub-menu is requested, may not be
    *           <code>
    * null</code>
    *
    * @param selection the selected node for which the context-menu need to get,
    *           may be <code>null</code>
    *
    * @throws IllegalArgumentException if the action requires selection and
    *            selection is not supplied.
    *
    * @return the action with its children to define menu, may be <code>null
    * </code> if there is an exception
    */
   public PSMenuAction getContextMenu(PSMenuAction action, PSSelection selection)
   {
      if (action == null)
         throw new IllegalArgumentException("action must not be null");

      return getContextMenu(action, selection, -1);
   }

   /**
    * The same as {@link #getContextMenu(PSMenuAction, PSSelection)}. In
    * addition, this will optionally filter the menu action against the selected
    * nodes in the clip board if there is any.
    *
    * @param action the menu action in question. It may not be <code>null</code>
    *           .
    * @param selection the target node. It may not be <code>null</code>.
    * @param clipType if this is one of the values in
    *           <code>PSClipBoard.TYPE_XXX</code>, then filtering the action
    *           against the selections (source nodes) from the clip board. If it
    *           is <code>-1</code>, then filtering the action against the given
    *           <code>selection</code>.
    *
    * @return the filtered action. It may be <code>null</code> if the action is
    *         not visible for the selected nodes or error occurs.
    */
   public PSMenuAction getContextMenu(PSMenuAction action, PSSelection selection, int clipType)
   {
      if (action == null)
         throw new IllegalArgumentException("action must not be null");
      if (selection == null)
         throw new IllegalArgumentException("selection must not be null");

      m_applet.setWaitCursor();
      try
      {
         action = addChildMenuActions(action, selection);
         if (clipType != -1)
         {
            PSSelection srcSel = m_clipBoard.getClipSelection(clipType);
            if (srcSel != null)
               action = filterMenuAction(action, srcSel, false);
         }
         else
         {
            action = filterMenuAction(action, selection, true);
         }
      }
      catch (PSContentExplorerException ex)
      {
         m_applet.displayErrorMessage(null, getClass(), "ActionMenuFailure", new String[]
         {action.getName(), ex.getLocalizedMessage()}, "Error", null);
         action = null;
      }
      finally
      {
         m_applet.resetCursor();
      }
      return action;
   }

   /**
    * Adds child menu actions for the given action if there is any.
    *
    * @param action the action in question, assumed not <code>null</code>.
    * @param selection the selected node for the specified action.
    * @return the specified action. Never <code>null</code>.
    * @throws PSContentExplorerException if an error occurs.
    */
   @SuppressWarnings("unchecked")
   private PSMenuAction addChildMenuActions(PSMenuAction action, PSSelection selection)
         throws PSContentExplorerException
   {
      Iterator childActions = null;
      if (action.getName().endsWith(ACTION_PASTE)) // client sub-menu
      {
         childActions = getPasteMenu(action, selection);
      }
      else if (action.getName().equals(ACTION_CHANGE_DF)) // client sub-menu
      {
         childActions = getDisplayFormatsMenu(selection);
      }
      else if (action.getName().endsWith(ACTION_PASTE_LINK_TO_SLOT) || action.getName().endsWith(ACTION_MOVE_TO_SLOT)) // client
                                                                                                                       // sub-menu
      {
         childActions = getPasteVariantsMenu(action, selection);
      }
      else if (action.getURL().trim().length() > 0)
      {
         String url = makeUrl(action, selection);
         childActions = getActionChildren(action, url, selection);
      }
      // Set the child actions if found any.
      if (childActions != null)
         action.setChildren(childActions);

      return action;
   }

   /**
    * The same as {@link #filterMenuAction(PSMenuAction, PSSelection, boolean)}. In addition, this
    * will return a default "No Entries" action if the action is not visible to
    * the selected nodes.
    *
    * @param action the menu action in question, assumed not <code>null</code>.
    * @param selection the selected nodes in question, assumed not
    *           <code>null</code>.
    * @param isTestMultiSelect <code>true</code> if need to test whether the
    *           action supports multi-selection in conjunction of whether the
    *           given selection contains multi-nodes. It must be
    *           <code>false</code> if the given selection is retrieved from the
    *           clip board (the source nodes); otherwise it must be
    *           <code>true</code>.
    *
    * @return menu action for the selected nodes. It may be <code>null</code> if
    *         the action is not visible for the selected nodes.
    *
    * @throws PSContentExplorerException if an error occurs.
    */
   private PSMenuAction filterMenuAction(PSMenuAction action, PSSelection selection, boolean isTestMultiSelect)
         throws PSContentExplorerException
   {
      PSMenuAction filterAction = filter((PSMenuAction) action.clone(), selection, isTestMultiSelect);
      if (filterAction == null || !filterAction.getChildren().hasNext())
      {
         String label = m_applet.getResourceString(getClass(), "No Entries");
         action.setChildren(PSIteratorUtils.iterator(new PSMenuAction(ACTION_NO_ENTRIES, label)));
      }
      else
         action = filterAction;

      return action;
   }

   /**
    * Gets the display format actions for the current selected node. The display
    * format actions are available only for the 'Folders', 'Searches' and
    * 'Views'.
    *
    * @param selection the current selection to get the menu, assumed not <code>
    * null</code>.
    *
    * @return the list of display format menu actions, never <code>null</code>,
    *         may be empty.
    */
   private Iterator getDisplayFormatsMenu(PSSelection selection)
   {
      List<PSMenuAction> childActions = new ArrayList<PSMenuAction>();

      Iterator dispFormats = null;
      if (selection.isFolderType())
         dispFormats = m_folderMgr.getDisplayFormats();
      else if (selection.isOfType(PSNode.TYPE_NEW_SRCH) || selection.isOfType(PSNode.TYPE_SAVE_SRCH)
            || selection.isOfType(PSNode.TYPE_CUSTOM_SRCH) || selection.isOfType(PSNode.TYPE_STANDARD_SRCH)
            || selection.isOfType(PSNode.TYPE_VIEW))
      {
         dispFormats = m_searchViewMgr.getDisplayFormats();
      }

      if (dispFormats != null && dispFormats.hasNext())
      {
         String displayId = "";
         while (dispFormats.hasNext())
         {
            PSDisplayFormat format = (PSDisplayFormat) dispFormats.next();
            PSMenuAction menuAction = new PSMenuAction(ACTION_CHANGE_DF, format.getDisplayName());
            PSProperties props = new PSProperties();

            displayId = String.valueOf(format.getDisplayId());
            props.setProperty(PROPERTY_DISPLAYFORMATID, displayId);

            if (((PSNode) selection.getNodeList().next()).getDisplayFormatId().equalsIgnoreCase(displayId))
            {
               props.setProperty(PROPERTY_MENU_ITEM_CHECKED, PROPERTY_TRUE);
            }

            menuAction.setProperties(props);
            childActions.add(menuAction);
         }
      }

      return childActions.iterator();
   }

   /**
    * Builds the variant menu action by making request to the server by using
    * the URL of the specified action. This menu is displayed whenever one or
    * more items are moved (or pasted ) to a slot. The user needs to choose one
    * of the displayed variants for the selected items to use the variant in
    * that slot. These variant options are computed as an intersetion of all
    * variants for the items in the clipborad and the variants allowed in the
    * target slot. This computation is the responsibilty of the server (request
    * url of the action).
    *
    * @param action must not be <code>null</code> and url must not be empty.
    *
    * @param selection the current selection to get the menu, must not be <code>
    * null</code>.
    *
    * @return the list of display format menu actions, never <code>null</code>,
    *         may be empty.
    */
   private Iterator getPasteVariantsMenu(PSMenuAction action, PSSelection selection) throws PSContentExplorerException
   {
      if (action == null)
         throw new IllegalArgumentException("action must not be null");

      if (selection == null)
         throw new IllegalArgumentException("selection must not be null");

      PSNode tgtNode = (PSNode) selection.getNodeList().next();

      int clipType = PSClipBoard.TYPE_COPY;
      if (action.getName().startsWith(PSMenuAction.PREFIX_DROP_PASTE))
         clipType = PSClipBoard.TYPE_DRAG;

      PSSelection clipSelection = m_clipBoard.getClipSelection(clipType);

      String url = makeUrl(action, clipSelection);

      url = appendParamToUrl(url, PROPERTY_SLOTID, tgtNode.getProp(PROPERTY_SLOTID));
      // Action is passed so that it can be used while executing the action
      url = appendParamToUrl(url, "sys_action", action.getName());

      return getActionChildren(action, url, selection);
   }

   /**
    * Gets the context menu that can be shown as pop-up menu. If no actions are
    * found for pop-up menu, returns a dummy parent action with one child action
    * 'No Entries'.
    *
    * @param selection the selected node for which the context-menu need to get,
    *           may not be <code>null</code>
    *
    * @return the action whose children defines menu, never <code>null</code>
    *         and may have <code>PSMenuAction</code> children.
    */
   public PSMenuAction getContextMenu(PSSelection selection)
   {
      PSMenuAction action = null;
      String uiContext = null;
      PSMenuAction retAction = null;

      m_applet.setWaitCursor();
      try
      {
         String mode = selection.getMode().getViewMode();
         Iterator iter = selection.getNodeList();
         while (iter.hasNext())
         {
            uiContext = ((PSNode) iter.next()).getType();
            action = lookupAction(mode, uiContext, action);
         }

         // create a dummy action with child 'No Entries'
         retAction = new PSMenuAction("parent", "parent");
         retAction.setType(PSMenuAction.TYPE_MENU);
         String label = m_applet.getResourceString(getClass(), "No Entries");
         retAction.setChildren(PSIteratorUtils.iterator(new PSMenuAction(ACTION_NO_ENTRIES, label)));

         // Update the action to return for context menu if we found action
         // children based on mode and uicontext of the selection.
         if (action != null)
         {
            PSMenuAction filterAction = filter((PSMenuAction) action.clone(), selection, true);
            if (filterAction != null)
            {
               // Modified to populate initially and do not wait for selection
               Iterator children = filterAction.getChildren();
               if (children.hasNext())
               {
                  poplateDynamic(children, selection);
               }
               retAction = filterAction;
            }

         }
      }
      catch (PSContentExplorerException ex)
      {
         ErrorDialogs.showErrorMessage(null, ex.getLocalizedMessage(), m_applet.getResourceString(getClass(), "Error"));
         retAction = null;
      }
      finally
      {
         m_applet.resetCursor();
      }
      return retAction;
   }

   @SuppressWarnings("rawtypes")
   private void poplateDynamic(Iterator children, PSSelection selection) throws PSContentExplorerException
   {

      while (children.hasNext())
      {
         PSMenuAction action = (PSMenuAction) children.next();

         if (action.getType().equals(PSMenuAction.TYPE_MENU) && action.getURL().trim().length() > 0)
         {
            String url = makeUrl(action, selection);
            Iterator childActions = getActionChildren(action, url, selection);
            action.setChildren(childActions);
            poplateDynamic(action.getChildren(), selection);
            action.setURL("");
         }
      }
   }

   /**
    * Get the folder manager from the action manager
    *
    * @return the folder manager, never <code>null</code>
    */
   public PSFolderActionManager getFolderActionManager()
   {
      return m_folderMgr;
   }

   /**
    * Get the component processor proxy from the action manager
    *
    * @return Returns the componentProxy, never <code>null</code>
    */
   public PSComponentProcessorProxy getComponentProxy()
   {
      return m_componentProxy;
   }

   /**
    * Gets the list of pop-up menu actions for paste actions. Prefixes the
    * internal name of the actions with {@link PSMenuAction#PREFIX_DROP_PASTE
    * DropPaste} or {@link PSMenuAction#PREFIX_DROP_PASTE CopyPaste}to recognise
    * the 'Paste' action menu.
    *
    * @param action the 'Paste' action, assumed that it represents either one of
    *           the above mentioned actions. Modified to set children action.
    * @param selection the target of this paste action, assumed not <code>null
    * </code> and empty.
    *
    * @return the child actions to represent the sub-menu of the supplied
    *         action, never <code>null</code>, may be empty.
    */
   private Iterator getPasteMenu(PSMenuAction action, PSSelection selection) throws PSContentExplorerException
   {
      List<PSMenuAction> children = new ArrayList<PSMenuAction>();

      if (selection.getNodeListSize() > 0)
      {
         PSNode node = (PSNode) selection.getNodeList().next();
         if (node.isOfType(PSNode.TYPE_SLOT_ITEM))
            node = selection.getParent();

         int clipType = PSClipBoard.TYPE_COPY;
         String prefix = PSMenuAction.PREFIX_COPY_PASTE;
         if (action.getName().startsWith(PSMenuAction.PREFIX_DROP_PASTE))
         {
            prefix = PSMenuAction.PREFIX_DROP_PASTE;
            clipType = PSClipBoard.TYPE_DRAG;
         }

         if (canAcceptPaste(selection.getMode().getView(), node, clipType))
         {
            String url = makePasteUrl(action, selection);
            Iterator actionChildren = getActionChildren(action, url, selection);

            while (actionChildren.hasNext())
            {
               PSMenuAction child = (PSMenuAction) actionChildren.next();
               if (isPasteChildActionPossible(child, selection, clipType))
               {
                  child.setName(prefix + child.getName());
                  children.add(child);
               }
            }
         }
      }

      return children.iterator();
   }

   /**
    * Verifies that the target folder(s) have at least Write access in the
    * folder permissions calculated for a given user.
    *
    * @param targetSel target selection, which could be multiple items and
    *           folders, assumed never <code>null</code>.
    * @return <code>true</code> if folder security permits to paste into the
    *         target folder, <code>false</code> otherwise.
    */
   private boolean isFolderPasteAllowed(PSSelection targetSel)

   {
      PSNode parentNode = targetSel.getParent();

      Iterator targetNodes = targetSel.getNodeList();

      while (targetNodes.hasNext())
      {
         PSNode targetNode = (PSNode) targetNodes.next();
         PSObjectPermissions tgtPerm;

         if (targetNode.isAnyFolderType())
         {
            tgtPerm = targetNode.getPermissions();
         }
         else
         {
            // must be an item, check parent folder permissions
            tgtPerm = parentNode.getPermissions();
         }

         if (tgtPerm != null && !tgtPerm.hasWriteAccess())
            return false;
      }

      return true;
   }

   /**
    * Verifies that the parent folder of the selected items has at least Write
    * access permissions as well as all the selected folders have Admin access
    * permissions calculated for a given user.
    *
    * @param selection selected items, which could be items and/or folders,
    *           assumed never <code>null</code>.
    * @return <code>true</code> if removal of a given selection is allowed,
    *         <code>false</code> otherwise.
    */
   private boolean isFolderRemoveAllowed(PSSelection selection)
   {
      PSNode selParent = selection.getParent();
      PSObjectPermissions tgtPerm = selParent.getPermissions();

      if (tgtPerm == null || !tgtPerm.hasWriteAccess())
         return false; // must have write access to the parent

      // check that each selected folder has admin access
      Iterator sourceNodes = selection.getNodeList();
      while (sourceNodes.hasNext())
      {
         PSNode sourceNode = (PSNode) sourceNodes.next();

         if (sourceNode.isAnyFolderType())
         {
            PSObjectPermissions srcPerm = sourceNode.getPermissions();

            if (srcPerm != null && !srcPerm.hasAdminAccess())
               return false;
         }
      }

      return true;
   }

   /**
    * This method checks if the specified paste child action is possible for the
    * combination of target selection and clip board selection.
    *
    * @param pasteChildAction check candidate must not be <code>null</code>.
    * @param tgtSelection target selection basically drop or paste target, must
    *           not be <code>null</code>
    * @param clipType clip board type, i.e. whether drag copy or keyboard copy.
    * @return <code>true</code> if this action is possible for the combination,
    *         <code>false</code> otherwise.
    */
   private boolean isPasteChildActionPossible(PSMenuAction pasteChildAction, PSSelection tgtSelection, int clipType)
   {
      PSSelection selection = m_clipBoard.getClipSelection(clipType);
      PSNode selParent = selection.getParent();
      String actionName = pasteChildAction.getName();

      // If the view does not support copy paste, no need to check whether Paste
      // child action is possible, it is always false.
      if (!viewSupportsCopyPaste())
         return false;

      if (!isFolderPasteAllowed(tgtSelection))
         return false;

      if (actionName.equals(ACTION_PASTE_LINK_TO_SLOT) || actionName.equals(ACTION_MOVE_TO_SLOT)
            || actionName.equals(ACTION_PASTE_LINK_SEARCH_TO_SLOT))
      {
         if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_IA))
         {
            // entire target selection must be slot(s)
            if (!tgtSelection.isOfType(PSNode.TYPE_SLOT) && !tgtSelection.isOfType(PSNode.TYPE_SLOT_ITEM))
            {
               return false;
            }
            // Only slot items can be moved
            if (actionName.equals(ACTION_MOVE_TO_SLOT) && !selection.isOfType(PSNode.TYPE_SLOT_ITEM))
               return false;
            // There are two actions available now when we drag slot items and
            // drop them on slots. One shows variant list and otherone just adds
            // the selected variant. The action that shows variant list should
            // be
            // available only when dragging and droping the items between slots.
            // The action that just adds the items to slots should be available
            // when draging and droping the items from the search results.
            // This action just adds the items to slot.
            if (actionName.equals(ACTION_PASTE_LINK_SEARCH_TO_SLOT)
                  && !(selParent.isOfType(PSNode.TYPE_NEW_SRCH) || selParent.isOfType(PSNode.TYPE_CATEGORY)))
               return false;
            // This action shows the variant list.
            if (actionName.equals(ACTION_PASTE_LINK_TO_SLOT)
                  && (selParent.isOfType(PSNode.TYPE_NEW_SRCH) || selParent.isOfType(PSNode.TYPE_CATEGORY)))
               return false;
            return true;
         }
         return false;
      }
      else if (actionName.equals(ACTION_PASTE_LINK) || actionName.equals(ACTION_PASTE_NEW_COPY))
      {
         /*
          * Assume CX views The entire target selection must be either
          * Folder(s), Site, Site Subfolder, System Sites or System Folders.
          */
         if (!tgtSelection.isAnyFolderType())
            return false;
         // No selected node can be in the target node
         Iterator selNodes = selection.getNodeList();
         while (selNodes.hasNext())
         {
            if (tgtSelection.containsNode((PSNode) selNodes.next()))
               return false;
         }

         // Folders accept only folders and items for linking or copying
         if (tgtSelection.isFolderType())
         {
            // Selection can contain only either folders or items
            List selTypes = selection.getTypes();
            int selTotal = selTypes.size();
            selTypes.remove(PSNode.TYPE_FOLDER);
            selTypes.remove(PSNode.TYPE_SITE);
            selTypes.remove(PSNode.TYPE_SITESUBFOLDER);
            boolean selHasFolders = selTotal > selTypes.size();
            selTypes.remove(PSNode.TYPE_ITEM);
            if (selTypes.size() > 0)
               return false;
            // if selection contains atleast one folder
            // paste as link not allowed
            if (selHasFolders && actionName.equals(ACTION_PASTE_LINK))
               return false;

            // Removed some code that checks whether the source is a folder
            // or not if it is a folder it returns false for
            // ACTION_PASTE_NEW_COPY
            // action, we added a wizard for folder paste as new copy and
            // We don't need to restrict that action anymore.

            // if selection's parent node is part of target selection
            if (tgtSelection.containsNode(selParent))
            {
               // copy as link not allowed
               if (actionName.equals(ACTION_PASTE_LINK))
                  return false;
            }
         }
         // System Folder system Site accept only folders for linking or copying
         else if (tgtSelection.isOfType(PSNode.TYPE_SYS_FOLDERS) || tgtSelection.isOfType(PSNode.TYPE_SYS_SITES))
         {
            if (!selection.isFolderType())
               return false;
            // if selection's parent node is part of target selection
            if (tgtSelection.containsNode(selParent))
            {
               // copy as link not allowed
               if (actionName.equals(ACTION_PASTE_LINK))
                  return false;
            }
         }
      }
      else if (actionName.equals(ACTION_MOVE) || actionName.equals(ACTION_FORCE_DELETE))
      {
         if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_IA))
         {
            // entire target selection must be slot(s) slot item(s)
            if (!tgtSelection.isOfType(PSNode.TYPE_SLOT) && !selection.isOfType(PSNode.TYPE_SLOT_ITEM))
               return false;
            // Only slot items can be moved
            if (!selection.isOfType(PSNode.TYPE_SLOT_ITEM))
               return false;
            return true;
         }

         // else assume CX views

         // target selection must be single node
         if (tgtSelection.isMultiSelect())
            return false;

         // Selection parent cannot be the same as target node for move
         PSNode tgtNode = (PSNode) tgtSelection.getNodeList().next();
         if (tgtNode == selParent)
            return false;

         // Selecion is not possible if selection contains the target node
         if (selection.containsNode(tgtNode))
            return false;

         // Parent node of the selection must be folder or system folder or
         // system site for this action
         if (!selParent.isAnyFolderType())
            return false;
         // entire target selection must be folder(s)
         if (!tgtSelection.isAnyFolderType())
            return false;

         if (!isFolderRemoveAllowed(selection))
            return false;
      }
      else if (actionName.equals(ACTION_PASTE_NEW_TRNSL))
      {
         // entire target selection must be folder(s)
         if (!tgtSelection.isFolderType())
            return false;

         // Only items can have translations
         if (!selection.isOfType(PSNode.TYPE_ITEM))
            return false;
      }
      else if (actionName.equals(ACTION_PASTE_DF))
      {
         // selection and target selection must be of single node
         if (selection.isMultiSelect() || tgtSelection.isMultiSelect())
            return false;
         if (tgtSelection.isFolderType())
         {
            if (!selection.isFolderType())
               return false;
         }
      }
      return true;
   }

   /**
    * Gets the list of actions by executing the supplied url.
    *
    * @param url the url to execute, may not be <code>null</code> or empty.
    * @param selection The current selection, assumed not <code>null</code>.
    *
    * @return the list of actions, never <code>null</code>, may be empty.
    *
    * @throws PSContentExplorerException if the url is not provided or an
    *            exception happened executing the url
    */
   private Iterator getActionChildren(PSMenuAction action, String url, PSSelection selection)
         throws PSContentExplorerException
   {
      try
      {
         if (StringUtils.isBlank(url))
         {
            throw new PSContentExplorerException(IPSContentExplorerErrors.ACTION_GET_CHILDREN,
                  m_applet.getResourceString(getClass(), "No url specified"));
         }

         List<PSMenuAction> resultChildList;
         List<PSMenuAction> childList;

         boolean wfAction = isWorkflowMenuAction(action, url);
         childList = wfAction ? null : getCachedActionChildren(url, selection);
         if (childList == null)
         {
            Map<String, Object> params = new HashMap<String, Object>();
            String processedUrl = parseQueryParams(url, params);
            childList = new ArrayList<PSMenuAction>();
            Element root = getXMLDocument(processedUrl, params).getDocumentElement();
            Element el = PSXMLDomUtil.getFirstElementChild(root);
            while (el != null)
            {
               // Valid actions will have a name attribute. If this attribute
               // is missing, it is likely because the query resulted in no
               // actions, and therefore an empty document. Note that the
               // value is never null from this call
               String name = el.getAttribute(PSMenuAction.NAME_ATTR);
               // The url may return the XML for a PSAction, which includes
               // Props and Params child elements - we are only concerned with
               // child Action elements
               if (el.getNodeName().equals(PSMenuAction.XML_NODE_NAME) && name.trim().length() > 0)
                  childList.add(new PSMenuAction(el));
               el = PSXMLDomUtil.getNextElementSibling(el);
            }

            cacheActionChildren(url, selection, childList);

            resultChildList = getCachedActionChildren(url, selection);
            if (resultChildList == null)
               resultChildList = childList;
         }
         else
         {
            resultChildList = childList;
         }

         if (!PSContentExplorerConstants.ACTION_WORKFLOW.equals(action.getName()))
            setPassThruParams(action, resultChildList);

         return resultChildList == null ? new ArrayList<PSMenuAction>().iterator() : resultChildList.iterator();
      }
      catch (Exception ex)
      {
         throw new PSContentExplorerException(IPSContentExplorerErrors.ACTION_GET_CHILDREN, ex.toString());
      }
   }

   /**
    * Sets the "targetStyle","target","launchesWindow" parameters on all the
    * returned action items if set. These are removed from the request to the
    * server so that caching will work if only these values are changed for a
    * menu.
    *
    * @param action
    * @param resultChildList
    */
   private void setPassThruParams(PSMenuAction action, List<PSMenuAction> resultChildList)
   {
      PSProperties passThruParamValues = new PSProperties();
      boolean foundPassThru = false;
      for (String paramName : PASS_THRU_PARAMS)
      {
         String paramValue = action.getParameter(paramName);
         if (paramValue != null && paramValue.length() > 0)
         {
            passThruParamValues.setProperty(paramName, paramValue);
            foundPassThru = true;
         }
      }

      if (foundPassThru)
      {
         setPassThruParams(resultChildList.iterator(), passThruParamValues);
      }
   }

   /**
    * Sets the "targetStyle","target","launchesWindow" parameters on all the
    * returned action items if set. These are removed from the request to the
    * server so that caching will work if only these values are changed for a
    * menu. This handles recursive update of child menu actions.
    *
    * @param resultChildList
    * @param passThruParamValues
    */
   private void setPassThruParams(Iterator<PSMenuAction> resultChildList, PSProperties passThruParamValues)
   {
      while (resultChildList.hasNext())
      {
         PSMenuAction child = resultChildList.next();
         if (child.getType().equalsIgnoreCase("MENUITEM"))
         {
            child.getURL();

            for (String prop : PASS_THRU_PARAMS)
            {
               String propValue = passThruParamValues.getProperty(prop);
               if (propValue != null)
                  child.getProperties().setProperty(prop, propValue);

            }
         }
         else if (child.getType().equalsIgnoreCase("MENU"))
         {
            setPassThruParams(child.getChildren(), passThruParamValues);
         }
      }
   }

   /**
    * Checks to see if the menu action is a workflow menu action based if the
    * name of the action is ({@value PSContentExplorerConstants#ACTION_WORKFLOW} or the url is:
    * {@link PSContentExplorerConstants#WORKFLOW_MENU_ACTION_URL}.
    *
    * @param action not null.
    * @param url not null.
    * @return <code>true</code> if the menu action is classified as workflow
    *         menu action.
    */
   private boolean isWorkflowMenuAction(PSMenuAction action, String url)
   {
      boolean wfAction = false;
      try
      {

         URI parsedUrl = new URI(url);
         if (PSContentExplorerConstants.WORKFLOW_MENU_ACTION_URL.equals(parsedUrl.getPath()) || PSContentExplorerConstants.ACTION_WORKFLOW.equals(action.getName()))
         {
            wfAction = true;
            if (!PSContentExplorerConstants.WORKFLOW_MENU_ACTION_URL.equals(parsedUrl.getPath()))
            {
               log.debug("Workflow action url is different than default.");
            }
            if (!PSContentExplorerConstants.ACTION_WORKFLOW.equals(action.getName()))
            {
               log.debug("Workflow action name is different than default.");
            }
         }
         return wfAction;
      }
      catch (URISyntaxException e)
      {
         log.error(e);
      }
      return wfAction;
   }

   /**
    * Parses the query params from the supplied <code>url</code> and places them
    * into the supplied map, URL decoding them first.
    *
    * @param url Assumed not <code>null</code>.
    * @param params Assumed not <code>null</code>. Each value is either a
    *           <code>List&lt;String></code> or a <code>String</code>.
    * @return the leading part of the URL upto, but not including the '?'. Never
    *         <code>null</code>.
    */
   private String parseQueryParams(String url, Map<String, Object> params)
   {
      Map<String, Object> p = PSHttpUtils.parseQueryParamsString(url, false, true);
      params.putAll(p);
      return PSHttpUtils.parseHttpPath(url);
   }

   /**
    * Attempts to get cached actions for the specified url and selection.
    *
    * @param url The url used to load the actions, assumed not <code>null</code>
    *           or empty.
    * @param selection The current selection, assumed not <code>null</code>.
    *
    * @return The actions, <code>null</code> if not previously cached.
    */
   private List<PSMenuAction> getCachedActionChildren(String url, PSSelection selection)
   {
      List<PSMenuAction> cacheActions = null;

      if (cacheActionsInNode(url))
      {
         // can't support multiple selections
         if (!selection.isMultiSelect())
         {
            // get from node
            PSNode node = (PSNode) selection.getNodeList().next();
            cacheActions = node.getChildMenuActions(url);
         }
      }
      else
      {
         cacheActions = m_childMenuCache.get(url);
      }

      List<PSMenuAction> results = null;
      // return new list instance so the cached list is not modified
      if (cacheActions != null)
      {
         results = new ArrayList<PSMenuAction>();
         for (PSMenuAction psMenuAction : cacheActions)
         {
            try
            {
               results.add(new PSMenuAction(psMenuAction.toXml(PSXmlDocumentBuilder.createXmlDocument())));
            }
            catch (PSUnknownNodeTypeException e)
            {
               // This should not happen as we just creating the new action
               // fromXml to toXml.
               m_applet.debugMessage(e.getLocalizedMessage());
            }
         }
      }
      return results;
   }

   /**
    * Stores the actions for the specified url and selection.
    *
    * @param url The url used to load the actions, assumed not <code>null</code>
    *           or empty.
    * @param selection The current selection, assumed not <code>null</code>.
    * @param actions The actions to cache, assumed not <code>null</code>.
    */
   private void cacheActionChildren(String url, PSSelection selection, List<PSMenuAction> actions)
   {
      List<PSMenuAction> cacheActions = new ArrayList<PSMenuAction>(actions);

      if (cacheActionsInNode(url))
      {
         // can't support multiple selections
         if (selection.isMultiSelect())
            return;

         // store in node
         PSNode node = (PSNode) selection.getNodeList().next();
         node.setChildMenuActions(url, cacheActions);
      }
      else
      {
         m_childMenuCache.put(url, cacheActions);
      }
   }

   /**
    * Determine if actions returned by the specified url must be cached in the
    * selected node, or if they can be cached across all nodes.
    *
    * @param url The url to check, assumed not <code>null</code> or empty.
    *
    * @return <code>true</code> if the actions must be cached in the node,
    *         <code>false</code> otherwise.
    */
   private boolean cacheActionsInNode(String url)
   {
      int sep = url.indexOf('?');
      if (sep == -1)
         return false;

      String params = url.substring(sep, url.length());
      if (params.indexOf(IPSHtmlParameters.SYS_CONTENTID + "=") > -1)
         return true;
      if (params.indexOf(IPSHtmlParameters.SYS_FOLDERID + "=") > -1)
         return true;

      return false;
   }

   /**
    * Filter this action based on the specified contexts. All actions not
    * available are removed, including child actions. Note: this method is
    * called recursively to handle the child actions.
    * <P>
    * Note that the rules used here should be in sync with the rules used in
    * {@link com.percussion.uicontext.PSFilterContextMenu}.
    *
    * @param action the current action to perform the fitler on, must not be
    *           <code>null</code>
    *
    * @param selection the current selection of nodes the action is being
    *           applied to, must not be <code>null</code>
    *
    * @param isTestMultiSelect <code>true</code> if need to test whether the
    *           action supports multi-selection in conjunction of whether the
    *           given selection contains multi-nodes. It must be
    *           <code>false</code> if the given selection is retrieved from the
    *           clip board (the source nodes); otherwise it must be
    *           <code>true</code>.
    *
    * @return the filtered action, all items not available are removed from the
    *         action, may return <code>null</code>
    *
    * @throws PSContentExplorerException if there are any errors.
    */
   private PSMenuAction filter(PSMenuAction action, PSSelection selection, boolean isTestMultiSelect)
         throws PSContentExplorerException
   {
      // lazily load dynamic properties for the selected nodes
      loadDynamicProperties(selection);
      PSSelection clipSel = null;
      if (action.getName().equals(ACTION_PASTE))
      {
         clipSel = m_clipBoard.getClipSelection(PSClipBoard.TYPE_COPY);
         if (clipSel == null)
            return null;
      }
      else if (action.getName().equals(ACTION_DELETE) || action.getName().equals(ACTION_FORCE_DELETE))
      {
         if (!isRemovable(selection))
            return null;
      }
      else if (action.getName().equals(ACTION_PURGE))
      {
         if (!isPurgeable(selection))
            return null;
      }

      if (!checkActionVisibility(action, selection, isTestMultiSelect))
         return null;

      List<PSMenuAction> keep = new ArrayList<PSMenuAction>();
      PSMenuAction childAction = null;
      Iterator iter = action.getChildren();
      if (iter.hasNext())
      {
         while (iter.hasNext())
         {
            childAction = (PSMenuAction) iter.next();
            // recurse filter for each child
            if (filter(childAction, selection, isTestMultiSelect) != null)
            {
               if (clipSel == null)
                  keep.add(childAction);
               // if action.getName() == ACTION_PASTE,
               // then filter action against the source nodes (in clip board)
               else if (filter(childAction, clipSel, false) != null)
                  keep.add(childAction);
            }
         }
         action.setChildren(keep.iterator());
      }
      return action;
   }

   /**
    * For each node in the selection that has not had dynamic properties loaded,
    * queries properties from the server and adds them to the node. If
    * {@link #ms_dynColumNames} is empty, this method is a noop.
    *
    * @param selection The current selection of nodes, may be <code>null</code>.
    * @throws PSContentExplorerException if there are any errors.
    */
   private void loadDynamicProperties(PSSelection selection) throws PSContentExplorerException
   {
      if (ms_dynColumNames.isEmpty())
         return;

      // walk selected nodes and build list of nodes that need props loaded, and
      // a list of content id's as well
      Map<String, PSNode> nodeMap = new HashMap<String, PSNode>();
      List<Integer> idList = new ArrayList<Integer>();
      Set<String> ctTypeIDs = new HashSet<String>();
      if (selection != null)
      {
         Iterator nodes = selection.getNodeList();
         while (nodes.hasNext())
         {
            PSNode node = (PSNode) nodes.next();

            // make sure we have an item
            if (!(node.getType().equals(PSNode.TYPE_DTITEM) || node.getType().equals(PSNode.TYPE_ITEM)))
            {
               node.setAreDynamicPropsLoaded(true);
               continue;
            }

            String strContentId = node.getContentId();
            if (strContentId == null || strContentId.trim().length() == 0)
            {
               // should not happen
               throw new RuntimeException("contentid may not be null for type ITEM or DTITEM");
            }

            if (!node.areDynamicPropsLoaded())
            {
               try
               {
                  idList.add(new Integer(strContentId));
                  nodeMap.put(strContentId, node);
                  String ctID = node.getProp(PROPERTY_CONTENTTYPEID);
                  if (ctID.trim().length() > 0)
                     ctTypeIDs.add(ctID);
               }
               catch (NumberFormatException ex)
               {
                  // should not happen
                  throw new RuntimeException("invalid contentid for type ITEM or DTITEM: " + strContentId);
               }
            }
         }
      }

      // if nothing to search on, we're done
      if (idList.isEmpty())
         return;

      // create a search and add the fields for the dynamic properties
      PSExecutableSearch search = new PSExecutableSearch(m_applet.getRhythmyxCodeBase(), ms_dynColumNames, idList,
            m_applet);
      if (!ctTypeIDs.isEmpty())
         search.setContentTypeIdList(ctTypeIDs);

      // execute the search
      PSNode tmpNode = new PSNode("temp", "temp", PSNode.TYPE_PARENT, null, null, false, -1);
      Iterator results = search.executeSearch(tmpNode).iterator();
      while (results.hasNext())
      {
         PSNode resultNode = (PSNode) results.next();
         PSNode srcNode = nodeMap.get(resultNode.getContentId());
         if (srcNode != null)
         {
            // add the results column values as properties
            PSProperties srcProps = srcNode.getProperties();
            PSProperties resultProps = resultNode.getProperties();
            Iterator cols = ms_dynColumNames.iterator();
            while (cols.hasNext())
            {
               String colName = cols.next().toString();
               String val = resultProps.getProperty(colName);
               srcProps.setProperty(colName, val);

            }
            srcNode.setAreDynamicPropsLoaded(true);
         }
      }
   }

   /**
    * Checks the cached value of any dynamic params that may be stale, retrieves
    * the current value from the server and prompts the user to continue with
    * the updated value. Currently the only dynamic param checked is
    * <code>$sys_revision</code>.
    *
    * @param action The action being executed whose params are checked for
    *           dynamic valuess, assumed not <code>null</code>.
    * @param selection The selected nodes to check, assumed not
    *           <code>null</code>, may be empty.
    *
    * @return <code>true</code> if the action should continue,
    *         <code>false</code> if not. In either case, the value of any
    *         dynamic properties checked is updated to the current revision. If
    *         <code>false</code> is selected, all selected nodes are refreshed.
    */
   private boolean checkDynamicParams(PSMenuAction action, PSSelection selection)
   {
      boolean paramsValid = true;

      // see if action has sys_revision param
      boolean hasRevision = false;
      PSParameters actionParams = action.getParameters();
      if (actionParams != null)
      {
         Iterator iter = actionParams.getParamKeys();
         String matchParam = "$" + IPSHtmlParameters.SYS_REVISION;
         while (iter.hasNext() && !hasRevision)
         {
            String key = iter.next().toString();
            String value = actionParams.getParameter(key);
            if (matchParam.equals(value))
            {
               hasRevision = true;
            }
         }
      }

      // if not, nothing to do here
      if (!hasRevision)
         return paramsValid;

      // build list of items to search
      List<Integer> contentIds = new ArrayList<Integer>();
      List<String> ctypeIds = new ArrayList<String>();
      Map<String, PSNode> nodeMap = new HashMap<String, PSNode>();
      Iterator nodes = selection.getNodeList();
      while (nodes.hasNext())
      {
         PSNode node = (PSNode) nodes.next();

         // make sure we have an item
         if (!(node.getType().equals(PSNode.TYPE_DTITEM) || node.getType().equals(PSNode.TYPE_ITEM)))
         {
            continue;
         }

         String strContentId = node.getContentId();
         if (strContentId == null || strContentId.trim().length() == 0)
         {
            // should not happen
            throw new RuntimeException("contentid may not be null for type ITEM or DTITEM");
         }

         PSProperties nodeProps = node.getProperties();
         if (nodeProps != null)
         {
            contentIds.add(new Integer(strContentId));
            nodeMap.put(strContentId, node);
            String ctId = node.getProp(PROPERTY_CONTENTTYPEID);
            if (ctId.trim().length() > 0)
               ctypeIds.add(ctId);
         }
      }

      // if nothing to search on, we're done
      if (contentIds.isEmpty())
         return paramsValid;

      // create a search and add the field we care about
      List<String> colNames = new ArrayList<String>();
      colNames.add("sys_revision");
      PSExecutableSearch search = new PSExecutableSearch(m_applet.getRhythmyxCodeBase(), colNames, contentIds, m_applet);
      if (!ctypeIds.isEmpty())
         search.setContentTypeIdList(ctypeIds);

      // execute the search
      PSNode tmpNode = new PSNode("temp", "temp", PSNode.TYPE_PARENT, null, null, false, -1);
      Iterator results;
      try
      {
         results = search.executeSearch(tmpNode).iterator();
      }
      catch (PSContentExplorerException e)
      {
         m_applet.displayErrorMessage(null, getClass(),
               "Exception checking dynamic action params for selected nodes: {0}", new String[]
               {e.getLocalizedMessage()}, "Error", null);
         return false;
      }

      List<PSNode> dirtyNodes = new ArrayList<PSNode>();
      while (results.hasNext())
      {
         PSNode resultNode = (PSNode) results.next();
         PSNode srcNode = nodeMap.get(resultNode.getContentId());
         if (srcNode != null)
         {
            // add the results column values as properties
            PSProperties srcProps = srcNode.getProperties();
            PSProperties resultProps = resultNode.getProperties();
            String val = resultProps.getProperty(IPSHtmlParameters.SYS_REVISION);
            if (!val.equals(srcProps.getProperty(IPSHtmlParameters.SYS_REVISION)))
            {
               // we have a stale revision, update the value in case it's used
               paramsValid = false;
               srcProps.setProperty(IPSHtmlParameters.SYS_REVISION, val);
               // set field as dirty so it's refreshed completely
               dirtyNodes.add(srcNode);
            }
         }
      }

      if (!paramsValid)
      {
         // prompt user and set result on paramsvalid
         String key = selection.isMultiSelect()
               ? "Batch Stale Revision Warning: Continue?"
               : "Stale Revision Warning: Continue?";
         String warning = m_applet.getResourceString(getClass(), key);
         String title = m_applet.getResourceString(getClass(), "Error");
         warning = PSLineBreaker.wrapString(warning, 78, 77, "\n");
         int result = JOptionPane.showConfirmDialog(getApplet(), warning, title, JOptionPane.YES_NO_OPTION);
         paramsValid = result == JOptionPane.YES_OPTION;
      }

      if (!paramsValid)
      {
         // user said no, dirty matching nodes and refresh current view
         informListeners(dirtyNodes.iterator(), PSActionEvent.DIRTY_NODES);
         informListeners(PSActionEvent.REFRESH_NAV_SELECTED);
      }

      return paramsValid;
   }

   /**
    * Determines if the supplied action can handle multiple items in a single
    * request.
    *
    * @param action Assumed not <code>null</code>.
    *
    * @return Always <code>true</code> for the special action whose id is -1,
    *         otherwise, the value as noted by the multi-select property on the
    *         action.
    */
   private boolean actionSupportsMultiSelection(PSMenuAction action)
   {
      // If it is the special hidden root action return true
      if (action.getActionId() == -1)
         return true;
      // check if we have multiple selection
      String val = action.getProperty(PROPERTY_SUP_MULTI_SELECT);

      // default is "no" if not found
      return val != null && val.equalsIgnoreCase("yes");
   }

   /**
    * Provides the user state specific to the content explorer.
    */
   private class CxGlobalState extends PSActionVisibilityGlobalState
   {

      @Override
      public String getClientContext()
      {
         return m_applet.getClientContext();
      }

      @Override
      public int getCommunityUuid()
      {
         return m_applet.getUserInfo().getCommunityId();
      }

      @Override
      public String getLocale()
      {
         return m_applet.getUserInfo().getLocale();
      }

      @Override
      public Collection<String> getRoles()
      {
         Collection<String> roles = new ArrayList<String>();
         Iterator it = m_applet.getUserInfo().getRoles();
         while (it.hasNext())
            roles.add(it.next().toString());
         return roles;
      }

   }

   /**
    * Filter the specified actions based on the visibility contexts.
    *
    * @param action the action to be filtered, never <code>null</code>
    *
    * @param selection the current selection of nodes to be processed, never
    *           <code>null</code>
    *
    * @param isTestMultiSelect <code>true</code> if need to test whether the
    *           action supports multi-selection in conjunction of whether the
    *           given selection contains multi-nodes. It must be
    *           <code>false</code> if the given selection is retrieved from the
    *           clip board (the source nodes); otherwise it must be
    *           <code>true</code>.
    *
    * @return true to keep the specified action, otherwise returns false
    */
   private boolean checkActionVisibility(PSMenuAction action, PSSelection selection, boolean isTestMultiSelect)
   {
      PSActionVisibilityContexts vcs = m_visibilityContextsMap.get("" + action.getActionId());

      boolean isSupportMultiSel = isTestMultiSelect ? actionSupportsMultiSelection(action) : true;

      PSActionVisibilityChecker visibilityChecker = new PSActionVisibilityChecker(action.getActionId(),
            isSupportMultiSel, vcs);

      PSActionVisibilityGlobalState globalState = new CxGlobalState();
      Collection<PSActionVisibilityObjectState> objStates = new ArrayList<PSActionVisibilityObjectState>();
      Iterator selIter = selection.getNodeList();
      PSNode parent = selection.getParent();
      while (selIter.hasNext())
      {
         PSNode node = (PSNode) selIter.next();
         objStates.add(new CxObjectState(node, parent));
      }

      return visibilityChecker.isVisible(globalState, objStates);
   }

   /**
    * Provides the object instance state specific to the content explorer.
    */
   private class CxObjectState extends PSActionVisibilityObjectState
   {
      /**
       * @param child The node that represents the instance of the object that
       *           the action would act upon. Never <code>null</code>. A copy of
       *           the node is held by this class, so changes to the supplied
       *           object may affect the behavior of this class. This class
       *           treats it read-only.
       *
       * @param parent Similar to <code>child</code>, except it is the direct
       *           parent of the child. May be <code>null</code> if the child is
       *           the root.
       */
      public CxObjectState(PSNode child, PSNode parent)
      {
         if (null == child)
         {
            throw new IllegalArgumentException("node cannot be null");
         }
         m_child = child;
         m_parent = parent;
      }

      @Override
      public int getAssignmentType()
      {
         return toInt(m_child.getAssignmentTypeID());
      }

      @Override
      public String getCheckoutStatus()
      {
         return m_child.getCheckOutStatus();
      }

      @Override
      public int getContentTypeUuid()
      {
         return toInt(m_child.getContentTypeId());
      }

      @Override
      public PSObjectPermissions getFolderPermissions()
      {
         PSObjectPermissions perm = null;

         if (m_child.isOfType(PSNode.TYPE_SYS_FOLDERS) || m_child.isOfType(PSNode.TYPE_SYS_SITES))
         {
            try
            {
               perm = m_folderMgr.loadFolder(m_child).getPermissions();
            }
            catch (PSContentExplorerException ex)
            {
               perm = new PSFolderPermissions(PSObjectPermissions.ACCESS_READ);
            }
         }
         else if (m_child.isFolderType())
         {
            perm = m_child.getPermissions();
         }
         else if (m_child.isItemType())
         {
            if (m_parent != null)
            {
               if (m_parent.isAnyFolderType())
                  perm = m_parent.getPermissions();
            }
         }
         return perm;
      }

      @Override
      public int getObjectType()
      {
         return toInt(m_child.getObjectType());
      }

      @Override
      public String getPublishableType()
      {
         return m_child.getPublishableType();
      }

      @Override
      public int getWorkflowAppUuid()
      {
         return toInt(m_child.getWorkflowAppId());
      }

      /**
       * Convert a string to a number.
       *
       * @param num may be <code>null</code> or empty.
       *
       * @return -1 if the <code>num</code> is <code>null</code> or empty,
       *         otherwise the integer as represented by the supplied text.
       *
       * @throws NumberFormatException If <code>num</code> is non-empty and it
       *            is not an integer.
       */
      private int toInt(String num)
      {
         if (StringUtils.isBlank(num))
            return -1;
         return Integer.parseInt(num);
      }

      /**
       * The node supplied in the ctor. Never <code>null</code> or modified
       * after construction.
       */
      private PSNode m_child;

      /**
       * The node supplied in the ctor. Never modified after construction.
       */
      private PSNode m_parent;
   }

   /**
    * Lookup the action in the cached list of actions, if not found, load from
    * the rx server based on the mode and context as specified.
    *
    * @param mode the mode that the actions belong to, must not be <code>null
    * </code> or empty
    *
    * @param uiContext the context to which the selection has been set, must not
    *           be <code>null</code> or empty
    *
    * @param action the current action to merge with, may be <code>null</code>
    */
   private PSMenuAction lookupAction(String mode, String uiContext, PSMenuAction action)
   {
      try
      {
         PSMenuAction newAction = null;
         String url = "/sys_cxSupport/ActionList.html" + "?sys_mode=" + mode + "&sys_uicontext=" + uiContext;

         // check to see if the key exists in the action map
         // if found use that as the actions
         if (m_actionMap.containsKey(url))
         {
            newAction = m_actionMap.get(url);
         }
         else
         {
            // otherwise load from the rx server the list of actions
            newAction = new PSMenuAction(getXMLDocument(url).getDocumentElement());
            m_actionMap.put(url, newAction);
         }
         newAction = (PSMenuAction) newAction.clone();
         action = newAction.merge(action);
      }
      catch (Exception ex)
      {
         /** @todo catching the right exception ?? */
         ex.printStackTrace();
      }
      return action;
   }

   /**
    * Loads the child nodes of the supplied node by executing the children url
    * of the node and returns the the children as iterator object for further
    * use. Displays an error message if an exception happens loading the
    * children.
    *
    * @param node parent node, must not be <code>null</code>
    *
    * @return appended child nodes as an iterator, may be <code>null</code> if
    *         an exception happens loading the children.
    *
    * @throws IllegalArgumentException if the node is <code>null</code>.
    */
   public Iterator loadChildren(PSNode node)
   {
      if (node == null)
         throw new IllegalArgumentException("node must not be null");

      m_applet.setWaitCursor();
      Iterator children = null;
      try
      {
         // Load children using Item Relationship Manager for dependency tree
         // mode
         if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_DT))
         {
            children = m_irsManager.loadDependencies(node);
         }
         else
         {
            /**
             * If the node type is "Folder" or "SystemFolder" or "SystemSite"
             * load the children using the folder API.
             */
            if (node.isAnyFolderType())
            {
               children = m_folderMgr.loadChildren(node);
            }
            /**
             * If the node type is related to a "Search" or "View" load the
             * children using the Search View Manager.
             */
            else if (node.isSearchType())
            {
               children = m_searchViewMgr.loadChildren(node);
            }
            else
            {
               String sUrl = node.getChildrenURL();
               if (sUrl.trim().length() < 1)
               {
                  String msg = "Node does not have a children URL and is not "
                        + "of a standard node type that can have dynamic children";
                  m_applet.debugMessage(msg);
               }
               else
               {
                  Document doc = getXMLDocument(sUrl);
                  loadChildrenFromDocument(node, doc);
                  if (node.getName().equals("SearchResults"))
                  {
                     m_searchResultsNode = node;
                     hideOrShowNewSearchNode();
                  }
                  children = node.getChildren();
                  Iterator searchChildren = node.getChildren();
                  while (searchChildren.hasNext())
                  {
                     PSNode searchNode = (PSNode) searchChildren.next();
                     if (searchNode.getType().equals(PSNode.TYPE_EMPTY_SRCH))
                     {
                        m_emptySearchNode = searchNode;
                     }
                  }
               }
            }
            if (children == null)
            {
               children = PSIteratorUtils.emptyIterator();
            }
         }
         node.setChildren(children);
         children = node.getChildren();
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         // Any known exception the code throws should be added here.
         if (ex instanceof PSException || ex instanceof ParserConfigurationException || ex instanceof SAXException
               || ex instanceof IOException || ex instanceof PSContentExplorerException)
         {
            m_applet.displayErrorMessage(null, getClass(), "Exception loading children: {0}", new String[]
            {ex.getLocalizedMessage()}, "Error", null);
         }
         else
         {
            m_applet.debugMessage("Unknown exception caught" + ex.getClass());
            m_applet.debugMessage(ex);

            m_applet.displayErrorMessage(null, getClass(), "Unknown exception loading children: {0}", new String[]
            {ex.getLocalizedMessage()}, "Error", null);
         }
      }
      finally
      {
         m_applet.resetCursor();
      }
      return children;
   }

   /**
    * Return whether the action manager is quick loading nodes that match the
    * type of the passed node. The only current implementer of this are folder
    * nodes, so for all other types this will return <code>false</code>.
    *
    * @param node A representative node, must never be <code>null</code>.
    * @return <code>true</code> if nodes of this type are being quick loaded.
    */
   public boolean isQuickLoading(PSNode node)
   {
      if (node == null)
      {
         throw new IllegalArgumentException("node must never be null");
      }

      if (node.isAnyFolderType())
      {
         return m_folderMgr.isQuickExpand();
      }
      else
      {
         return true;
      }
   }

   /**
    * Extracts the child nodes from the supplied document and sets them as
    * children of the supplied parent node. If the document contains display
    * format of the children, it updates the display format for the children on
    * the parent node, otherwise clears the current display format. If the
    * document is empty or does not have any child nodes, sets empty child list
    * on the parent.
    * <p>
    * <!ELEMENT root(TableMeta?, Node*)> See the
    * {@link com.percussion.cx.objectstore.PSNode} for the expected format of
    * the sub-elements.
    *
    * @param parent the parent that need to set with children, may not be <code>
    * null</code>
    * @param doc the document containing child nodes, may not be <code>null
    * </code>, must conform to the above specified format.
    *
    * @throws PSUnknownNodeTypeException if the document is not of expected
    *            format.
    */
   public static void loadChildrenFromDocument(PSNode parent, Document doc) throws PSUnknownNodeTypeException
   {
      if (parent == null)
         throw new IllegalArgumentException("parent may not be null.");

      if (doc == null)
         throw new IllegalArgumentException("doc may not be null.");

      List<PSNode> childList = new ArrayList<PSNode>();
      Element root = doc.getDocumentElement();
      if (root != null)
      {
         Element el = PSXMLDomUtil.getFirstElementChild(root);
         if (el != null && el.getNodeName().equals(PSNode.TABLEMETA_NODE))
         {
            // could have an empty TableMeta tag which should be ignored
            if (el.hasChildNodes())
               parent.setChildrenDisplayFormat(el);
            else
               parent.clearChildrenDisplayFormat();

            el = PSXMLDomUtil.getNextElementSibling(el);
         }
         else
            parent.clearChildrenDisplayFormat();

         while (el != null)
         {
            childList.add(new PSNode(el));
            el = PSXMLDomUtil.getNextElementSibling(el);
         }
      }
      parent.setChildren(childList.iterator());
   }

   /**
    * Executes the action defined by the menu-item action url and informs the
    * action listeners that are registered with this manager. If the action is a
    * single action, it calls listener's <code>actionCompleted(String)</code>
    * after completion of action. If the action is a batch action, it initiates
    * the action and calls listener's <code>actionInitiated(IPSProcessMonitor)
    * </code> to monitor the process with it.
    *
    * @param action the action object that represents TYPE_MENUITEM, may not be
    *           <code>null</code>
    *
    * @param selection the selected node on which supplied action should work,
    *           may be <code>null</code>
    *
    * @throws IllegalArgumentException if the action requires selection and
    *            selection is not supplied.
    */
   public void executeAction(PSMenuAction action, PSSelection selection)
   {
      executeAction(action, selection, null);
   }

   /**
    * Does the work defined by {@link #executeAction(PSMenuAction, PSSelection)}
    * but adds an additional parameter that is action specific.
    *
    * @param data The type is determined by the action. It is present to support
    *           virtually executing actions with slightly different behaviors
    *           than what would happen when executed from the UI. May be
    *           <code>null</code>. Only used by CLIENT actions.
    */
   private void executeAction(PSMenuAction action, PSSelection selection, Object data)
   {
      if (action == null)
         throw new IllegalArgumentException("action must not be null");

      m_searchViewMgr.resetSearchListeners();

      if (action.supportsBatchProcessing())
      {
         if (selection == null)
            throw new IllegalArgumentException("selection may not be null.");

         executeBatchAction(action, selection);
      }
      else if (action.isClientAction())
         executeClientAction(action, selection, data);
      else
         executeServerAction(action, selection);

   }

   /**
    * Add a search listener to the search view manager.
    *
    * @param l the search listener, must never be <code>null</code>, checked in
    *           the called method.
    */
   public void addSearchListener(IPSSearchListener l)
   {
      m_searchViewMgr.addSearchListener(l);
      m_folderMgr.addSearchListener(l);
   }

   /**
    * Executes the supplied batch action on the selection by making a url
    * request to the server for each node in the selection. Does not launch new
    * window. Ignores even if that property is specified. Informs the listeners
    * of this action that this batch process is initiated to give the a clue
    * that they can visually represent the action execution process by a
    * progress bar using the supplied process monitor.
    *
    * @param action the action to execute, assumed not <code>null</code> and is
    *           a server action.
    * @param selection the list of nodes on which the supplied action should
    *           act, assumed not <code>null</code>
    */
   private void executeBatchAction(final PSMenuAction action, final PSSelection selection)
   {
      // make sure we don't have stale revisions
      if (!checkDynamicParams(action, selection))
         return;

      final boolean workflowAction = isWorkflowAction(action);
      // Workflow is a known server action that supports batch processing.
      // And need to be provided with a dialog.
      if (workflowAction)
      {
         // If user didn't want to continue, do not continue
         if (!prepareForWorkflowAction(action, selection))
            return;
      }

      final PSProcessMonitor monitor = new PSProcessMonitor(selection.getNodeListSize(), m_applet);

      informListeners(monitor);

      Thread workerThread = new Thread()
      {
         @Override
         public void run()
         {
            List<PSNode> processedNodes = new ArrayList<PSNode>();

            try
            {
               monitor.setStatus(PSProcessMonitor.STATUS_RUN);

               final Map itemIdToStateId = new HashMap();
               // the 2nd chk is an optimization to prevent an unneeded srvr
               // query
               if (workflowAction && selection.getNodeListSize() > 1)
               {
                  /*
                   * the key is the item's contentId, the value is the id of the
                   * WF state that the item was in before the processing started
                   */
                  List ids = new ArrayList();
                  Iterator selIter = selection.getNodeList();
                  while (selIter.hasNext())
                  {
                     ids.add(((PSNode) selIter.next()).getContentId());
                  }
                  // get current WF state of all items for use while processing
                  getItemIdToStateIdMap(ids, itemIdToStateId);
               }

               Iterator iter = selection.getNodeList();
               int code = 0;
               int i = 0;
               boolean first = true;
               while (iter.hasNext())
               {
                  while (monitor.getStatus() == PSProcessMonitor.STATUS_PAUSE)
                     sleep(10);

                  if (monitor.getStatus() == PSProcessMonitor.STATUS_STOP)
                     break;

                  PSNode node = (PSNode) iter.next();
                  processedNodes.add(node);
                  monitor.updateStatus(++i, node);

                  try
                  {
                     if (workflowAction)
                     {
                        if (!first && !inSameState(itemIdToStateId, node.getContentId()))
                        {
                           /*
                            * skip the item because it was processed due to
                            * mandatory relationships from a previously
                            * processed item. See Rx-11003.
                            */
                           m_applet.debugMessage("Skipped item that auto transitioned: " + node.getContentId());
                           continue;
                        }
                        first = false;
                     }
                     String url = makeUrl(action, node, selection.getMode().getViewMode(), selection.getParent());

                     code = executeUrl(url);

                     m_applet.debugMessage("Response code is: " + code);
                  }
                  catch (PSContentExplorerException e)
                  {
                     m_applet.debugMessage(e);

                     Object[] args =
                     {action.getName(), e.getLocalizedMessage()};
                     String error = MessageFormat
                           .format(
                                 m_applet
                                       .getResourceString(getClass(),
                                             "An error occurred processing action {0} in batch mode. The error returned was: {1}. \n\nDo you want to continue?"),
                                 args);

                     PSLineBreaker.wrapString(error, 78, 77, "\n");
                     monitor.showError(null, error);
                  }
               }

               if (monitor.getStatus() != PSProcessMonitor.STATUS_STOP)
                  monitor.setStatus(PSProcessMonitor.STATUS_COMPLETE);

            }
            catch (InterruptedException e)
            {
               m_applet.debugMessage("Interrupted " + e.getLocalizedMessage());
               monitor.setStatus(PSProcessMonitor.STATUS_COMPLETE);
               Thread.currentThread().interrupt();
            }
            catch (PSContentExplorerException e)
            {
               String msg = "Failed to acquire initial states of items, no items were transitioned: "
                     + e.getLocalizedMessage();
               JOptionPane.showMessageDialog(getApplet().getDialogParentFrame(), msg, "Fatal Error",
                     JOptionPane.ERROR_MESSAGE);
               monitor.setStatus(PSProcessMonitor.STATUS_COMPLETE);
            }

            // dirty processed nodes
            informListeners(processedNodes.iterator(), PSActionEvent.DIRTY_NODES);

            // refresh
            informListeners(PSActionEvent.REFRESH_NAV_SELECTED);
         }
      };

      workerThread.start();
   }

   /**
    * Compares an item's current state as obtained from the server against a
    * supplied value.
    *
    * @param itemIdToStateIdOriginal A set of content ids to workflow state ids
    *           (both as <code>Integer</code>). Assumed not <code>null</code>
    *           and that there is an entry for the supplied
    *           <code>contentId</code>.
    * @param contentId The server will be queried to obtain the current workflow
    *           state of this item. Assumed a valid # that is present as a key
    *           in <code>itemIdToStateIdOriginal</code>.
    *
    * @return <code>true</code> if the item's state as found in
    *         <code>itemIdToStateIdOriginal</code> matches what was queried from
    *         the server, <code>false</code> otherwise. Assumed to be an id that
    *         is present in the map.
    *
    * @throws PSContentExplorerException If a failure occurs when querying the
    *            server.
    */
   private boolean inSameState(Map itemIdToStateIdOriginal, String contentId) throws PSContentExplorerException
   {
      Integer cid = new Integer(contentId);
      Map itemIdToStateIdCur = new HashMap();
      getItemIdToStateIdMap(Collections.singletonList(cid), itemIdToStateIdCur);
      Integer originalState = (Integer) itemIdToStateIdOriginal.get(cid);
      Integer curState = (Integer) itemIdToStateIdCur.get(cid);
      return originalState.equals(curState);
   }

   /**
    * Queries the server to obtain the current workflow state for each of the
    * supplied ids. If an id is invalid, it is skipped.
    *
    * @param contentIds Assumed not <code>null</code>. The item ids for which
    *           you want state ids.
    *
    * @param results Assumed not <code>null</code>. Results are blindly added to
    *           this map, meaning if any entry already exists, it could be
    *           overwritten. The key is the content Id, the value is the WF
    *           state Id, both as <code>Integer</code>.
    *
    * @throws PSContentExplorerException If a problem occurs while talking to
    *            the server or parsing the result document.
    */
   private void getItemIdToStateIdMap(List contentIds, Map results) throws PSContentExplorerException
   {
      Map params = new HashMap();
      params.put(IPSHtmlParameters.SYS_CONTENTID, contentIds);
      URL url;
      try
      {
         url = new URL(m_applet.getRhythmyxCodeBase(), "../sys_cxSupport/getWfStateIds.xml");
         String resultDoc = m_applet.getHttpConnection().postData(url, params);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(new StringReader(resultDoc), false);
         /*
          * The format of the returned doc is (as an example): <ContentItems>
          * <Item contentId="103" wfStateId="5"/> <Item contentId="104"
          * wfStateId="5"/> </ContentItems>
          */
         NodeList nl = doc.getElementsByTagName("Item");
         for (int i = 0; i < nl.getLength(); i++)
         {
            Node n = nl.item(i);
            String cid = PSXMLDomUtil.getAttributeTrimmed((Element) n, "contentId");
            String wfId = PSXMLDomUtil.getAttributeTrimmed((Element) n, "wfStateId");
            results.put(new Integer(cid), new Integer(wfId));
         }
      }
      catch (MalformedURLException e)
      {
         throw new PSContentExplorerException(0, e.getLocalizedMessage());
      }
      catch (IOException e)
      {
         throw new PSContentExplorerException(0, e.getLocalizedMessage());
      }
      catch (PSException e)
      {
         throw new PSContentExplorerException(0, e.getLocalizedMessage());
      }
      catch (SAXException e)
      {
         throw new PSContentExplorerException(0, e.getLocalizedMessage());
      }
   }

   /**
    * Workflow actions require special attention as it requires user input or
    * confirmation to continue with the action. So this method prompts the user
    * for required input (comment text) and sets those on the action as
    * parameters.
    * <p>
    * Currently it does the following.
    * <ol>
    * <li>For 'Force check-in' action, prompts the user with detailed
    * confirmation message to check-in.</li>
    * <li>All other actions, prompts user to input a <code>Required</code> or
    * <code>Optional</code> comment. For <code>Required</code> comments some
    * text must be entered. For <code>Optional</code> comments some text may be
    * entered. Maximum length of a comment is 255 chars.</li>
    * </ol>
    *
    * @param action the action that represents one of the workflow transitions,
    *           assumed not <code>null</code>
    * @param selection the selection that represents the selected nodes, assumed
    *           not <code>null</code>
    *
    * @return <code>true</code> if user clicks OK to continue and there is no
    *         error, otherwise <code>false</code>.
    */
   private boolean prepareForWorkflowAction(PSMenuAction action, PSSelection selection)
   {

      // Handle a Forced Check-in
      if (action.getName().equals(ACTION_FORCE_CHECKIN))
      {
         Set<String> checkOutUsers = new HashSet<String>();
         Iterator nodes = selection.getNodeList();
         while (nodes.hasNext())
         {
            PSNode node = (PSNode) nodes.next();
            checkOutUsers.add(node.getProp(PROPERTY_CHECKOUTUSER));
         }
         String title = m_applet.getResourceString(getClass(), "Warning");
         String[] data = new String[3];
         data[0] = ErrorDialogs.cropErrorMessage(MessageFormat.format(
               m_applet.getResourceString(getClass(), "Selected Item Check-out users: <{0}>"), new Object[]
               {checkOutUsers.toString()}));
         data[1] = "\n";
         data[2] = ErrorDialogs.cropErrorMessage(m_applet.getResourceString(getClass(), "Force Check-in Warning"));

         int option = JOptionPane.showConfirmDialog(null, data, title, JOptionPane.OK_CANCEL_OPTION);
         if (option == JOptionPane.CANCEL_OPTION)
            return false;
      }
      // Ask for Workflow Comment if needed
      if ("hide".equalsIgnoreCase(action.getProperty("commentRequired")))
      {
         // Set the comment to null
         action.setParameter("commenttext", "");
      }
      else
      {
         //
         // Build the dialog to request a Workflow Comment
         // (Note: This Swing code replaced an arcane use of JOptionPane
         // which refused to allow focus to be placed in the JTextArea
         // on JREs 1.6 and later. It became a customer issue (RX-14406)
         // which forced rewrite using a JDialog. It is probably too much
         // Swing specific code to have here.)
         //
         // Build the dialog title and get display options
         boolean commentRequired = "yes".equalsIgnoreCase(action.getProperty("commentRequired"));
         String dlgtitle = m_applet.getResourceString(getClass(), "Enter workflow comment");
         if (commentRequired)
         {
            dlgtitle += m_applet.getResourceString(getClass(), "(Required)");
         }
         else
         {
            dlgtitle += m_applet.getResourceString(getClass(), "(Optional)");
         }

         PSDisplayOptions dispOptions = (PSDisplayOptions) UIManager.getDefaults().get(
               PSContentExplorerConstants.DISPLAY_OPTIONS);
         PSFocusBorder border = new PSFocusBorder(1, dispOptions);

         // Set up values for dialog
         int width = 400;
         int height = 200;
         Frame appletFrame = getApplet().getDialogParentFrame();

         // "cancelWorkflowOperation" and "textStringArea" needed for
         // OK/Cancel Button action listeners and dialog Close operation
         final boolean cancelWorkflowOperation[] = new boolean[1];
         cancelWorkflowOperation[0] = false;
         final String textAreaString[] = new String[1];
         textAreaString[0] = "";

         //
         // Build the dialog
         //
         final JDialog dlg = new JDialog(appletFrame, dlgtitle, true);
         dlg.setSize(width, height);
         dlg.setResizable(true);

         dlg.setLocationRelativeTo(appletFrame);
         dlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
         dlg.addWindowListener(new WindowAdapter()
         {
            @Override
            public void windowClosing(WindowEvent we)
            {
               textAreaString[0] = "";
               cancelWorkflowOperation[0] = true;
               dlg.setVisible(false);
               dlg.dispose();
            }
         });

         // Use BorderLayout and add in the components of dialog
         dlg.getContentPane().setLayout(new BorderLayout());
         border.addToAllNavigable((JComponent) dlg.getContentPane());

         JLabel label = new JLabel(m_applet.getResourceString(getClass(),
               "Please enter comment for the workflow transition."));
         JPanel labelPanel = new JPanel();
         labelPanel.add(label);
         dlg.getContentPane().add(labelPanel, BorderLayout.NORTH);

         JPanel eastPanel = new JPanel();
         eastPanel.setSize(70, 200);
         dlg.getContentPane().add(eastPanel, BorderLayout.EAST);

         JPanel westPanel = new JPanel();
         westPanel.setSize(70, 200);
         dlg.getContentPane().add(westPanel, BorderLayout.WEST);

         final JTextArea textArea = new JTextArea();
         textArea.setLineWrap(true);
         textArea.setWrapStyleWord(true);

         JScrollPane pane = new JScrollPane(textArea);
         pane.setPreferredSize(new Dimension(200, 150));
         dlg.getContentPane().add(pane, BorderLayout.CENTER);

         JButton okButton = new JButton("OK");
         okButton.setMnemonic('O');
         okButton.addActionListener(ev -> {
            textAreaString[0] = textArea.getText().trim();
            cancelWorkflowOperation[0] = false;
            dlg.setVisible(false);
            dlg.dispose();
         });

         JButton cancelButton = new JButton("Cancel");
         cancelButton.setMnemonic('C');
         cancelButton.addActionListener(ev -> {
            textAreaString[0] = "";
            cancelWorkflowOperation[0] = true;
            dlg.setVisible(false);
            dlg.dispose();
         });

         JPanel buttonPanel = new JPanel();
         buttonPanel.add(okButton);
         buttonPanel.add(cancelButton);
         dlg.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

         //
         // Now, display the dialog and,
         // make sure the text entered meets requirements.
         // If not, display and error and force the edit of comment.
         //
         boolean repeat = false;
         String comment = "";
         textArea.setText("");
         do
         {
            repeat = false;

            // Display the modal dialog (after forcing focus to text area)
            // In the call to "setVisible()", the Window Listener for the dialog
            // and the Action Listeners for the buttons are called. Temporary
            // variables "cancelWorkflowOperation" and "textAreaString" are set
            // in the listeners.
            // (and are therefore declared as final arrays to allow them to be
            // used
            // within the listeners).
            textArea.requestFocus();
            dlg.setVisible(true);

            // Check for Cancel button or Window Close icon.
            // If so, all best are off, cancel the workflow.
            if (cancelWorkflowOperation[0])
            {
               return false;
            }

            // Fetch the comment entered into text area
            // Check it for presence and length,
            // -if its not good, loop back through but don't discard comment
            // (let it be edited).
            comment = textAreaString[0];
            if (comment.length() == 0 && commentRequired)
            {
               ErrorDialogs.showErrorMessage(appletFrame, MessageFormat.format(m_applet.getResourceString(getClass(),
                     "Comment must be entered for the workflow transition <{0}>"), new Object[]
               {action.getLabel()}), m_applet.getResourceString(getClass(), "Error"));
               repeat = true;
            }
            else if (comment.length() > 255)
            {
               ErrorDialogs.showErrorMessage(appletFrame, MessageFormat.format(
                     m_applet.getResourceString(getClass(), "Comment text length cannot exceed 255 characters"),
                     (Object[]) null), m_applet.getResourceString(getClass(), "Error"));
               repeat = true;
            }
         }
         while (repeat);


         //
         // Assign to action the parameter (commenttext)
         //
         action.setParameter("commenttext", comment);
      }

      return true;
   }

   /**
    * The method that handles all actions whose handler is 'client'. Typical
    * actions handled by this method represent UI actions like displaying
    * dialogs, launching new window and refreshing the user interface or
    * applying display options.
    *
    * @param action the action to execute, assumed not <code>null</code> and
    *           handler is <code>PSAction.HANDLER_CLIENT</code>.
    * @param selection the selection of nodes, may be <code>null</code> ignored
    *           if the action does not depend on a selection. F
    * @param data Optional information that may be used by certain actions to
    *           change their behavior. May be <code>null</code> for all actions.
    *           The expected type is determined by the action.
    *
    *           NOTE: Garbage Collection during searches Searches returning
    *           5500+ results would use more than 60MB of plugins'
    *           memory(Default=97.6MB). A following search performed immediately
    *           would throw OutOfMemory Exception. This call to GC would
    *           mitigate the plugin from throwing OutOfMemory exception. We
    *           could still see the Exception if the a search results in 5500+
    *           rows. However, bumping up the plugin memory would solve it.
    */
   private void executeClientAction(PSMenuAction action, PSSelection selection, Object data)
   {
      if (action == null)
         throw new IllegalArgumentException("action must not be null");

      try
      {
         m_applet.setWaitCursor();
         m_applet.debugMessage("Executing " + action.getName() + " action");

         if (requireSelection(action) && selection == null)
            throw new IllegalArgumentException("selection not found for the action " + action.getName());

         if (action.getName().equals(ACTION_OPTIONS))
         {
            PSDisplayOptions defOptions = m_applet.getOptionsManager().getDefaultDisplayOptions();
            PSDisplayOptions userOptions = m_applet.getOptionsManager().getDisplayOptions();

            PSDisplayOptionsDialog dlg = new PSDisplayOptionsDialog(m_applet.getDialogParentFrame(), userOptions,
                  defOptions, m_applet);
            dlg.pack();
            dlg.setVisible(true);

            if (dlg.isOk())
               informListeners(PSActionEvent.REFRESH_OPTIONS);
         }
         else if (action.getName().equals(ACTION_SEARCH)
               || action.getName().equals(ACTION_SLOT_SEARCH)
               || action.getName().equals(ACTION_EDIT_SEARCH) && ((PSNode) selection.getNodeList().next())
                     .isOfType(PSNode.TYPE_EMPTY_SRCH))
         {
            if (action.getName().equals(ACTION_SLOT_SEARCH))
            {
               PSNode selectedSlotNode = (PSNode) selection.getNodeList().next();
               if (selectedSlotNode.getSlotId().length() == 0)
                  throw new IllegalArgumentException("The action " + ACTION_SLOT_SEARCH + "is not a valid action for "
                        + selectedSlotNode.getName());

               m_newSearchNode.setPropertyObj(PROPERTY_SLOT_NODE, selectedSlotNode);

               m_newSearchNode.setProperty(PROPERTY_SLOTID, selectedSlotNode.getSlotId());
            }

            String oldFolderPath = m_newSearchNode.getProp(PROPERTY_FOLDER_PATH);
            String path = null;
            if (null != data && data.toString().trim().length() > 0)
               path = data.toString();
            m_newSearchNode.setProperty(PROPERTY_FOLDER_PATH, path);

            boolean isRcSearch = m_newSearchNode.getSlotId().length() > 0;
            // prepare the search fields filter and pass it to the
            // PSSearchDialog
            Map searchFieldFilterMap = prepareSearchFilterMap(m_newSearchNode);
            // see the NOTE above
            Runtime.getRuntime().gc();
            PSSearchDialog dlg = new PSSearchDialog(m_applet.getDialogParentFrame(), m_searchViewMgr, m_remCataloger,
                  m_newSearchNode, searchFieldFilterMap, true, isRcSearch, m_applet.getSearchConfig(),
                  m_applet.getSearachableFieldsCache(), m_applet.getCodeBase());
            dlg.setVisible(true);
            if (dlg.isOk())
            {
               PSSearchViewActionManager.setAsInitialized(m_newSearchNode);
               if (m_newSearchNode.isHidden())
               {
                  addNewSearchNode();
               }
               informListeners(PSIteratorUtils.iterator(m_newSearchNode));
            }
            else
            {
               /*
                * they cancelled, so make the existing search results behavior
                * unchanged
                */
               m_newSearchNode.setProperty(PROPERTY_FOLDER_PATH, oldFolderPath);
            }
         }
         else if (action.getName().equals(ACTION_EDIT_SEARCH))
         {
            PSNode searchNode = (PSNode) selection.getNodeList().next();
            if (!searchNode.isOfType(PSNode.TYPE_SAVE_SRCH) && !searchNode.isOfType(PSNode.TYPE_NEW_SRCH)
                  && !searchNode.isOfType(PSNode.TYPE_CUSTOM_SRCH) && !searchNode.isOfType(PSNode.TYPE_STANDARD_SRCH))
            {
               throw new IllegalArgumentException("Found a node that is not search to edit query.");
            }

            // Show dialog always for 'CX' view, but in other views (IA & RC)
            // only if the slot id is available to search on.
            if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_CX) || searchNode.getSlotId().length() > 0)
            {
               boolean isRcSearch = searchNode.getSlotId().length() > 0;
               // prepare the search fields filter and pass it to the
               // PSSearchDialog
               Map searchFieldFilterMap = prepareSearchFilterMap(searchNode);
               // see the NOTE above
               Runtime.getRuntime().gc();
               PSSearchDialog dlg = new PSSearchDialog(m_applet.getDialogParentFrame(), m_searchViewMgr,
                     m_remCataloger, searchNode, searchFieldFilterMap, false, isRcSearch, m_applet.getSearchConfig(),
                     m_applet.getSearachableFieldsCache(), m_applet.getCodeBase());

               dlg.setVisible(true);
               if (dlg.isOk())
               {
                  // Sets any search as initialized if not yet initialized
                  if (PSSearchViewActionManager.isNodeInitializable(searchNode))
                  {
                     PSSearchViewActionManager.setAsInitialized(searchNode);
                  }
                  // Save the search if it is of the type saveable
                  if (searchNode.isOfType(PSNode.TYPE_SAVE_SRCH))
                  {
                     m_searchViewMgr.saveSearch(dlg.getSearch());
                  }
                  informListeners(PSActionEvent.REFRESH_NAV_SELECTED);
               }
            }
         }
         else if (action.getName().equals(ACTION_CREATE_FOLDER))
         {
            PSNode parentFolderNode = null;
            if (selection != null)
               parentFolderNode = (PSNode) selection.getNodeList().next();

            boolean isFolderSel = true;
            if (parentFolderNode == null || !parentFolderNode.isAnyFolderType())
            {
               parentFolderNode = m_applet.getSystemFoldersNode();
               isFolderSel = false;
            }

            if (parentFolderNode == null)
               throw new IllegalStateException("Parent folder is not found to create a new folder");

            PSFolderDialog dlg = new PSFolderDialog(m_applet.getDialogParentFrame(), m_applet.getUserInfo(),
                  m_folderMgr, null, parentFolderNode, m_navSelectionPath);

            dlg.setVisible(true);

            if (dlg.isOk())
            {
               if (isFolderSel)
                  informListeners(PSActionEvent.REFRESH_NAV_SELECTED);
               else
                  informListeners(PSIteratorUtils.iterator(parentFolderNode));
            }
         }
         else if (action.getName().equals(ACTION_EDIT_FOLDER))
         {
            PSNode folderNode = (PSNode) selection.getNodeList().next();

            if (!folderNode.isFolderType())
               throw new IllegalArgumentException("Invalid node for the action " + action.getName());

            PSFolderDialog dlg = new PSFolderDialog(m_applet.getDialogParentFrame(), m_applet.getUserInfo(),
                  m_folderMgr, folderNode, selection.getParent(), m_navSelectionPath);

            dlg.setVisible(true);
            if (!dlg.isOk())
               return;

            // dirty the selected node
            informListeners(selection.getNodeList(), PSActionEvent.DIRTY_NODES);
            PSNode currentSelectedNode = m_applet.getSelectedNavTreeNode();

            if (selection.getMode().getViewMode().equals(PSUiMode.TYPE_CXMAN))
               informListeners(PSActionEvent.REFRESH_NAV_SELECTED);
            else
            {
               informListeners(PSActionEvent.REFRESH_NAV_SEL_PARENT);
               if (currentSelectedNode != null)
               {
                  // Rx-05-02-0118 Fix selection in nav tree after
                  // folder property change
                  // This code causes the parent node to reload immediately
                  // so we can find the new PSNode that matches the id of the
                  // old node
                  String content_id = currentSelectedNode.getContentId();
                  PSNode match = null;
                  // Search for the matching new node in the refreshed parent
                  PSNode parent = m_applet.getNavTree().getSelectedNode();
                  m_folderMgr.loadChildren(parent);
                  Iterator citer = parent.getChildren();
                  while (citer.hasNext())
                  {
                     PSNode child = (PSNode) citer.next();
                     if (child.getContentId().equals(content_id))
                     {
                        match = child;
                        break;
                     }
                  }
                  if (match != null)
                  {
                     m_applet.getNavTree().selectNode(match);
                  }
               }

            }
         }
         else if (action.getName().equals(ACTION_COPY_ACL_TO_SUBFOLDERS))
         {
            PSNode folderNode = (PSNode) selection.getNodeList().next();
            PSFolder folder = m_folderMgr.loadFolder(folderNode);
            if (folder.getPermissions().hasServerAdminAccess())
            {
               propagateFolderSecurity(folderNode);
            }
            else
            {
               Object[] args =
               {action.getLabel()};

               String error = MessageFormat.format(m_applet.getResourceString(getClass(), "ServerAdminAccessRequired"),
                     args);

               ErrorDialogs.showErrorMessage(null, error,
                     m_applet.getResourceString(getClass(), "Server Admin Access Required"));
            }
         }
         else if (action.getName().equals(ACTION_DELETE) || action.getName().equals(ACTION_FORCE_DELETE))
         {
            int ok = JOptionPane.showConfirmDialog(
                  null,
                  m_applet.getResourceString(getClass().getName() + "@Delete actions cannot be undone. "
                        + "Are you sure you want to delete?"),
                  m_applet.getResourceString(getClass().getName() + "@Confirm delete"), JOptionPane.YES_NO_OPTION);

            if (ok != JOptionPane.YES_OPTION)
               return;
            boolean force = action.getName().equals(ACTION_FORCE_DELETE);
            m_folderMgr.delete(selection.getParent(), selection.getNodeList(), force);

            // dirty the selected node
            informListeners(selection.getNodeList(), PSActionEvent.DIRTY_NODES);

            if (selection.getMode().getViewMode().equals(PSUiMode.TYPE_CXMAN))
               informListeners(PSActionEvent.REFRESH_NAV_SELECTED);
            else
               informListeners(PSActionEvent.REFRESH_NAV_SEL_PARENT);
         }
         else if (action.getName().equals(ACTION_PURGEALL))
         {
            handlePurgeAllAction(selection);
         }
         else if (action.getName().equals(ACTION_PURGE_NAV))
         {
            handlePurgeNavAction(selection);
         }
         else if (action.getName().equals(ACTION_DELETE_SEARCH))
         {
            if (!selection.isOfType(PSNode.TYPE_SAVE_SRCH))
            {
               throw new IllegalArgumentException("Invalid node type for the action " + action.getName());
            }
            int ok = JOptionPane.showConfirmDialog(
                  null,
                  m_applet.getResourceString(getClass().getName() + "@Delete actions cannot be undone. "
                        + "Are you sure you want to delete?"),
                  m_applet.getResourceString(getClass().getName() + "@Confirm delete"), JOptionPane.YES_NO_OPTION);

            if (ok != JOptionPane.YES_OPTION)
               return;

            m_searchViewMgr.delete(selection.getNodeList());

            if (selection.getMode().getViewMode().equals(PSUiMode.TYPE_CXMAN))
               informListeners(PSActionEvent.REFRESH_NAV_SELECTED);
            else
               informListeners(PSActionEvent.REFRESH_NAV_SEL_PARENT);
         }
         else if (action.getName().equals(ACTION_REFRESH))
         {
            m_applet.debugMessage("Executing \'Refresh\' action");

            if (selection == null)
               informListeners(PSActionEvent.REFRESH_NAV_ROOT, true);
            else if (selection instanceof PSNavigationalSelection)
               informListeners(PSActionEvent.REFRESH_NAV_SELECTED, true);
            else
               informListeners(PSActionEvent.REFRESH_NAV_SEL_PARENT, true);
         }
         else if (action.getName().equals(ACTION_COPY))
         {
            m_clipBoard.setClip(PSClipBoard.TYPE_COPY, selection);
         }
         else if (action.getName().equals(ACTION_COPY_URL_TO_CLIPBOARD))
         {
            Iterator selectedItems = selection.getNodeList();
            PSNode item = (PSNode) selectedItems.next();
            String codeBase = m_applet.getRhythmyxCodeBase().toString();
            String link = codeBase + "/sys_ActionPage/Panel.html?sys_contentid=" + item.getContentId();
            Transferable trans = new StringSelection(link);
            try
            {
               Clipboard clip = getApplet().getToolkit().getSystemClipboard();
               clip.setContents(trans, new ClipboardOwner()
               {
                  @Override
                  @SuppressWarnings("unused")
                  public void lostOwnership(Clipboard arg0, Transferable arg1)
                  {
                     // Ignore loss of ownership
                  }
               });
            }
            catch (Exception e1)
            {
               m_applet.debugMessage("Unable to copy to clipboard, problem was " + e1.getMessage());
            }
         }
         else if (action.getName().equals(ACTION_SAVEAS))
         {
            Iterator iter = selection.getNodeList();
            PSNode newSearchNode = (PSNode) iter.next();
            if (!newSearchNode.isOfType(PSNode.TYPE_NEW_SRCH))
            {
               throw new IllegalArgumentException("Invalid node found for action " + ACTION_SAVEAS);
            }

            PSSaveSearchDialog dlg = new PSSaveSearchDialog(m_applet.getDialogParentFrame(), m_applet);
            dlg.setVisible(true);
            if (dlg.isOk())
            {
               String saveName = dlg.getSearchName();
               m_searchViewMgr.saveNewSearch(newSearchNode, saveName, dlg.getShowTo());

               informListeners(PSActionEvent.REFRESH_NAV_SEL_PARENT);
            }
         }
         else if (action.getName().equals(ACTION_CHANGE_DF))
         {
            String newDf = action.getProperties().getProperty(PROPERTY_DISPLAYFORMATID);
            if (newDf == null || newDf.length() < 1)
            {
               throw new IllegalArgumentException("Displayformat id for action " + ACTION_CHANGE_DF + "is not found");
            }

            // take only the first one
            PSNode node = (PSNode) selection.getNodeList().next();
            if (node.isFolderType() || node.isOfType(PSNode.TYPE_NEW_SRCH) || node.isOfType(PSNode.TYPE_SAVE_SRCH)
                  || node.isOfType(PSNode.TYPE_CUSTOM_SRCH) || node.isOfType(PSNode.TYPE_STANDARD_SRCH)
                  || node.isOfType(PSNode.TYPE_VIEW))
            {
               node.setDisplayFormatId(newDf);
               m_applet.saveDisplayFormatIdToOptions(node);
               informListeners(PSActionEvent.REFRESH_NAV_SELECTED, true);
            }
            else
            {
               throw new IllegalArgumentException("The action " + ACTION_CHANGE_DF + "is not valid for selected node "
                     + node);
            }
         }
         else if (action.getName().startsWith(PSMenuAction.PREFIX_DROP_PASTE)
               || action.getName().startsWith(PSMenuAction.PREFIX_COPY_PASTE))
         {
            if (!viewSupportsCopyPaste())
               throw new IllegalStateException("Invalid action " + action.getName() + "for this view");

            int clipType = PSClipBoard.TYPE_COPY;
            if (action.getName().startsWith(PSMenuAction.PREFIX_DROP_PASTE))
               clipType = PSClipBoard.TYPE_DRAG;

            Iterator clipNodes = m_clipBoard.getClip(clipType);
            if (clipNodes == null)
               throw new IllegalStateException("Paste action is called without content in the clip board.");

            List<PSNode> refreshNodes = new ArrayList<PSNode>();
            PSNode targetParent = selection.getParent();
            Iterator targetNodes = selection.getNodeList();

            // Remember where we currently are to restore this below
            PSNode currentSelectedNode = m_applet.getSelectedNavTreeNode();

            PSNode srcParent = m_clipBoard.getClipSource(clipType);
            boolean isFullRefresh = true;

            if (action.getName().endsWith(ACTION_PASTE_DF))
            {
               // Take only the first node for getting display format of the
               // source
               PSNode nodeSrc = (PSNode) clipNodes.next();
               String nodeSrcType = nodeSrc.getType();

               // Displays an error if the current content in the clip board
               // is not a valid type that supports display format
               if (!isFolderType(nodeSrcType) && !nodeSrcType.equals(PSNode.TYPE_SAVE_SRCH)
                     && !nodeSrcType.equals(PSNode.TYPE_CUSTOM_SRCH) && !nodeSrcType.equals(PSNode.TYPE_STANDARD_SRCH)
                     && !nodeSrcType.equals(PSNode.TYPE_VIEW))
               {
                  ErrorDialogs.showErrorMessage(null, m_applet.getResourceString(getClass(),
                        "Source must be a Folder/Saved Search/View for this Paste action"), m_applet.getResourceString(
                        getClass(), "Error"));
                  return;
               }

               String newDf = nodeSrc.getDisplayFormatId();
               if (newDf == null || newDf.length() < 1)
               {
                  throw new IllegalArgumentException("Displayformat id is not found for " + nodeSrc);
               }

               // Apply the display format of the source to all target(selected)
               // nodes
               while (targetNodes.hasNext())
               {
                  PSNode nodeTgt = (PSNode) targetNodes.next();
                  String tgtType = nodeTgt.getType();
                  if (isFolderType(tgtType) || tgtType.equals(PSNode.TYPE_SAVE_SRCH)
                        || tgtType.equals(PSNode.TYPE_CUSTOM_SRCH) || tgtType.equals(PSNode.TYPE_STANDARD_SRCH)
                        || tgtType.equals(PSNode.TYPE_VIEW))
                  {
                     nodeTgt.setDisplayFormatId(newDf);
                     m_applet.saveDisplayFormatIdToOptions(nodeTgt);
                     refreshNodes.add(nodeTgt);
                  }
               }
            }
            else if (action.getName().endsWith(ACTION_PASTE_LINK) || action.getName().endsWith(ACTION_PASTE_NEW_COPY)
                  || action.getName().endsWith(ACTION_MOVE) || action.getName().endsWith(ACTION_FORCE_MOVE)
                  || action.getName().endsWith(ACTION_MOVE_TO_SLOT)
                  || action.getName().endsWith(ACTION_PASTE_LINK_TO_SLOT)
                  || action.getName().endsWith(ACTION_PASTE_LINK_SEARCH_TO_SLOT))
            {
               if (srcParent == null || targetParent == null)
                  throw new IllegalStateException("Either parent of the source or target may not be null");

               if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_IA))
               {
                  PSItemAssemblyManager mgr = new PSItemAssemblyManager(this);

                  if (targetParent.isOfType(PSNode.TYPE_SLOT))
                     refreshNodes.add(targetParent);
                  while (targetNodes.hasNext())
                  {
                     PSNode target = (PSNode) targetNodes.next();

                     /*
                      * the new variant id is stored as action property in this
                      * case. Change the sys_variantid property of all clipboard
                      * nodes before modifying.
                      */
                     String varid = action.getProperty(PROPERTY_VARIANTID);
                     // only if action got a new variant id previously set
                     if (varid != null && varid.trim().length() > 0)
                     {
                        List<PSNode> tempNodes = new ArrayList<PSNode>();
                        PSNode node = null;
                        while (clipNodes.hasNext())
                        {
                           node = (PSNode) clipNodes.next();
                           node.setProperty(PROPERTY_VARIANTID, varid);
                           tempNodes.add(node);
                        }
                        clipNodes = tempNodes.iterator();
                     }
                     if (action.getName().endsWith(ACTION_MOVE_TO_SLOT))
                     {
                        /*
                         * Reorder if the source and target nodes belong to the
                         * same parent or if the target is the slot that is
                         * parent of the source. In later case it puts the
                         * source at the end of the list. If the parent slot of
                         * source and target items are different, it updates
                         * target and source parents.
                         */
                        if (srcParent == targetParent || srcParent == target)
                        {
                           mgr.reorder(srcParent, target, clipNodes);
                        }
                        else
                        {
                           mgr.update(targetParent, target, clipNodes);
                           // Source need to be refreshed in case of move when
                           // source and target parents are different.
                           refreshNodes.add(srcParent);
                        }
                     }
                     else if (action.getName().endsWith(ACTION_PASTE_LINK_TO_SLOT)
                           || action.getName().endsWith(ACTION_PASTE_LINK_SEARCH_TO_SLOT))
                     {
                        // Always inserts in case of links and copies
                        mgr.insert(targetParent, target, clipNodes);
                        // Source does not really need to be refreshed but needs
                        // to be selected after proess is completed
                        refreshNodes.add(srcParent);
                     }
                     // Always insert in the beginning as we to keep the last
                     // node as selected node
                     if (target.isOfType(PSNode.TYPE_SLOT))
                        refreshNodes.add(0, target);
                  }
               }
               else
               {
                  boolean refreshSrcParent = false;
                  boolean addedSrcParent = false;
                  while (targetNodes.hasNext())
                  {
                     PSNode target = (PSNode) targetNodes.next();

                     // right-now supports only pasting on Folders
                     if (!target.isAnyFolderType())
                        continue;

                     if (target.isOfType(PSNode.TYPE_SYS_FOLDERS) || target.isOfType(PSNode.TYPE_SYS_SITES))
                     {
                        // Can Paste only folders
                        List<PSNode> list = new ArrayList<PSNode>();
                        PSNode tmp = null;
                        while (clipNodes.hasNext())
                        {
                           tmp = (PSNode) clipNodes.next();
                           if (tmp.isFolderType())
                              list.add(tmp);
                        }
                        clipNodes = list.iterator();
                     }

                     if (clipNodes.hasNext())
                        refreshSrcParent = true;

                     // save clip nodes list for dirty action
                     List clipList = PSIteratorUtils.cloneList(clipNodes);
                     clipNodes = clipList.iterator();
                     boolean doDirty = true;
                     if (action.getName().endsWith(ACTION_PASTE_LINK))
                     {
                        m_folderMgr.add(target, clipList.iterator());
                        // no need to dirty parent on copy
                        refreshSrcParent = false;
                     }
                     else if (action.getName().endsWith(ACTION_PASTE_NEW_COPY))
                     {
                        // the copy action must handle dirtying
                        doDirty = false;
                        // no need to dirty parent on copy
                        refreshSrcParent = false;

                        /*
                         * There can only be one source for copy site and copy
                         * site subfolder actions.
                         */
                        PSNode singleSource = null;
                        if (clipList.size() == 1)
                           singleSource = (PSNode) clipList.get(0);

                        PSFolderActionManager.ErrorResults errorLog = null;
                        if (singleSource != null && singleSource.isOfType(PSNode.TYPE_SITE)
                              && isChild("//Sites", target)
                              && !(target.isOfType(PSNode.TYPE_SITE) || target.isOfType(PSNode.TYPE_SITESUBFOLDER)))
                        {
                           PSWizardDialog dialog = createCopySiteWizard(singleSource, target);

                           if (dialog.isOk())
                           {
                              m_applet.setWaitCursor();

                              errorLog = m_folderMgr.copyFolder(singleSource, target,
                                    createSiteCloningOptions(dialog.getData()));
                           }
                        }
                        else if (singleSource != null && singleSource.isAnyFolderType()
                              && (target.isSiteFolder() || target.isSiteSubfolder()))
                        {
                           PSWizardDialog dialog = createCopySiteSubfolderWizard(singleSource, target);

                           if (dialog.isOk())
                           {
                              m_applet.setWaitCursor();

                              errorLog = m_folderMgr.copyFolder(singleSource, target,
                                    createSiteSubfolderCloningOptions(dialog.getData()));
                           }
                        }
                        else
                           m_folderMgr.copy(target, clipList.iterator());

                        if (!(errorLog == null || errorLog.wasSuccessful()))
                        {
                           if (errorLog.m_logFileName != null)
                           {
                              /*
                               * There were errors cloning a site or site
                               * subfolder. Display the error message to inform
                               * the user.
                               */
                              String logLocation = m_applet.getSystemLogBase().toExternalForm()
                                    + errorLog.m_logFileName;

                              Object[] args =
                              {logLocation};
                              String msg = m_applet.getResourceString(getClass(),
                                    "@There were errors copying a Site or Site "
                                          + "Subfolder which you must fix manually. The "
                                          + "errors are logged to the file: {0}.\n\nDo "
                                          + "you want to view the error log now?");
                              String message = MessageFormat.format(msg, args);
                              int option = JOptionPane
                                    .showConfirmDialog(m_applet.getDialogParentFrame(),
                                          PSLineBreaker.wrapString(message, 80, 10, null), "Error",
                                          JOptionPane.YES_NO_OPTION);
                              if (option == JOptionPane.OK_OPTION)
                                 UTBrowserControl.displayURL(logLocation);
                           }
                           if (errorLog.m_siteCloneErrorText != null)
                           {
                              Object[] args =
                              {errorLog.m_siteCloneErrorText};
                              String msg = m_applet.getResourceString(getClass(), "@SiteDefCloneProblem");
                              String message = MessageFormat.format(msg, args);
                              JOptionPane.showMessageDialog(m_applet.getDialogParentFrame(),
                                    PSLineBreaker.wrapString(message, 80, 10, null), "Error",
                                    JOptionPane.WARNING_MESSAGE);
                           }
                        }
                     }
                     else
                     {
                        boolean force = action.getName().endsWith(ACTION_FORCE_MOVE);
                        m_folderMgr.move(srcParent, target, clipNodes, force);
                     }

                     try
                     {
                        // Need to reload the flagged folder set
                        m_applet.loadFlaggedFoldersSet();
                     }
                     catch (Exception e)
                     {
                        // Just print a stacktrace if this fails as
                        // it is not vital
                        e.printStackTrace();
                     }

                     // do selective refresh
                     isFullRefresh = false;

                     // dirty nodes for the refresh
                     if (doDirty)
                     {
                        informListeners(clipList.iterator(), PSActionEvent.DIRTY_NODES);
                     }

                     refreshNodes.add(target);

                     if (refreshSrcParent && !addedSrcParent && target != srcParent)
                     {
                        refreshNodes.add(srcParent);
                        addedSrcParent = true;
                     }
                  }
               }
            }

            if (clipType == PSClipBoard.TYPE_DRAG)
               m_clipBoard.clearDragClip();

            // call inform listeners with proper nodes to refresh.
            informListeners(refreshNodes.iterator(), isFullRefresh);
            // Keep the view pointing to the original node
            m_applet.setSelectedNavTreeNode(currentSelectedNode);
            // Fix the count since a move will cause the counts in the folder
            // to be off
            currentSelectedNode.fixSearchResultCount();
            // Refresh search stuff
            if (currentSelectedNode.isAnyFolderType())
            {
               m_folderMgr.runSearchCompleted(currentSelectedNode);
            }
         }
         else if (action.getName().equals(ACTION_ARRANGE_REMOVE))
         {
            if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_IA))
            {
               Iterator targetNodes = selection.getNodeList();

               PSItemAssemblyManager mgr = new PSItemAssemblyManager(this);
               mgr.delete(selection.getParent(), targetNodes);

               List<PSNode> refreshNodes = new ArrayList<PSNode>();
               refreshNodes.add(selection.getParent());

               informListeners(refreshNodes.iterator());
            }
         }
         else if (action.getName().equals(ACTION_MOVE_UPLEFT))
         {
            if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_IA))
            {
               PSItemAssemblyManager mgr = new PSItemAssemblyManager(this);
               mgr.reorder(selection, -1);

               List<PSNode> refreshNodes = new ArrayList<PSNode>();
               refreshNodes.add(selection.getParent());

               informListeners(refreshNodes.iterator());
            }
         }
         else if (action.getName().equals(ACTION_MOVE_DOWNRT))
         {
            if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_IA))
            {
               PSItemAssemblyManager mgr = new PSItemAssemblyManager(this);
               mgr.reorder(selection, 1);

               List<PSNode> refreshNodes = new ArrayList<PSNode>();
               refreshNodes.add(selection.getParent());

               informListeners(refreshNodes.iterator());
            }
         }
         else if (action.getName().equals(ACTION_LINK_TO_SLOT))
         {
            if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_IA))
            {
               PSNode origSelection = m_applet.getSelectedNavTreeNode();

               PSItemAssemblyManager mgr = new PSItemAssemblyManager(this);

               PSNode slotNode = (PSNode) m_newSearchNode.getSlotNode();

               mgr.insert(slotNode, slotNode, selection.getNodeList());

               List<PSNode> refreshNodes = new ArrayList<PSNode>();
               refreshNodes.add(slotNode);
               informListeners(refreshNodes.iterator());

               // always reselect the original selection since the refresh
               // will change the selection
               m_applet.setSelectedNavTreeNode(origSelection);
            }
            else if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_RC))
            {
               String ownerContentId = m_newSearchNode.getContentId();
               String slotId = m_newSearchNode.getSlotId();

               m_applet.debugMessage("Linking selected items to slot " + slotId + " of the content item "
                     + ownerContentId + ")");
               PSItemAssemblyManager mgr = new PSItemAssemblyManager(this);
               mgr.insert(ownerContentId, slotId, selection.getNodeList());

               // Use LiveConnect to call refreshParent javascript
               // method
               JSObject window = JSObject.getWindow(m_applet);
               Object[] args = new Object[]
               {};
               window.call("refreshParent", args);
            }
         }
         else if (action.getName().endsWith(ACTION_CHANGE_VARIANT))
         {
            if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_IA))
            {
               PSNode target = selection.getParent();
               PSNode nodeCurrent = (PSNode) selection.getNodeList().next();
               String newVarid = action.getProperty(PROPERTY_VARIANTID);
               String oldVarid = nodeCurrent.getProp(PROPERTY_VARIANTID);
               if (newVarid.equals(oldVarid))
               {
                  m_applet.debugMessage("No change since old and new varaintids are same");
                  return;
               }
               nodeCurrent.setProperty(PROPERTY_VARIANTID, newVarid);
               List<PSNode> list = new ArrayList<PSNode>();
               list.add(nodeCurrent);

               PSItemAssemblyManager mgr = new PSItemAssemblyManager(this);
               mgr.update(target, target, list.iterator());
            }
         }
         else if (action.getName().equals(ACTION_TOP_HELP) || action.getName().equals(ACTION_CXT_HELP)) // 'Help'
                                                                                                        // and
                                                                                                        // 'topMenuHelp'
         {
            String helpId;
            if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_CX))
               helpId = "ContentExplorer";
            else if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_IA))
               helpId = "ItemAssembly";
            else if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_RC))
               helpId = "RelatedContentSearch";
            else
               helpId = "DependencyViewer";
            if (action.getName().equals(ACTION_CXT_HELP))
            {
               PSNode helpNode = (PSNode) selection.getNodeList().next();
               String nodeType = helpNode.getType();
               String helpTypeHint = helpNode.getHelpTypeHint();
               if (nodeType.equals(PSNode.TYPE_SYS_SITES))
               {
                  helpId = "Sites";
               }
               else if (nodeType.equals(PSNode.TYPE_SYS_FOLDERS) || isFolderType(nodeType))
               {
                  helpId = "Folders";
               }
               else if (nodeType.equals(PSNode.TYPE_SYS_CATEGORY) || nodeType.equals(PSNode.TYPE_CATEGORY))
               {
                  if (helpTypeHint != null && helpTypeHint.equals(PSNode.HELP_TYPE_HINT_SRCH))
                  {
                     helpId = "Search";
                  }
                  else
                  {
                     helpId = "Categories";
                  }
               }
               else if (nodeType.equals(PSNode.TYPE_SYS_VIEW) || nodeType.equals(PSNode.TYPE_VIEW))
               {
                  helpId = "Views";
               }
               else if (nodeType.equals(PSNode.TYPE_SLOT) || nodeType.equals(PSNode.TYPE_SLOT_ITEM))
               {
                  helpId = "SlotItemAssembly";
               }
               else if (nodeType.equals(PSNode.TYPE_NEW_SRCH) || nodeType.equals(PSNode.TYPE_CUSTOM_SRCH)
                     || nodeType.equals(PSNode.TYPE_STANDARD_SRCH) || nodeType.equals(PSNode.TYPE_SAVE_SRCH))
               {
                  if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_CX))
                     helpId = "Search";
                  else if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_IA)
                        || m_applet.getView().equals(PSUiMode.TYPE_VIEW_RC))
                  {
                     helpId = "RelatedContentSearch";
                  }
               }
            }

            PSJavaHelp.launchHelp(helpId);
         }
         else if (action.getName().equals(ACTION_ABOUT))
         {
            PSI18NTranslationKeyValues resources = PSI18NTranslationKeyValues.getInstance();
            String appPackage = getClass().getPackage().getName();
            String title = resources.getTranslationValue(appPackage + "@title");
            PSFormatVersion version = new PSFormatVersion(appPackage);

            PSAboutDialog dlg = new PSAboutDialog(m_applet.getDialogParentFrame(), title, version.getVersionString());
            dlg.setAppletContext(m_applet.getAppletContext());
            dlg.setVisible(true);
         }
         else if (action.getName().equals(ACTION_OPEN_FOLDER_REF))
         {
            PSNode node = (PSNode) selection.getNodeList().next();
            /*
             * Use the path, if present, otherwise, retrieve the path from the
             * server based on the folder id.
             */
            String id = node.getContentId();
            String[] results;
            try
            {
               results = m_folderMgr.getFolderPaths(new PSLocator(id));
            }
            catch (PSCmsException e)
            {
               // could happen if folder removed since search results generated
               JOptionPane.showMessageDialog(
                     m_applet.getDialogParentFrame(),
                     m_applet.getResourceString(getClass().getName() + "@Could not find path of this folder, probably "
                           + "because the folder was deleted."),
                     m_applet.getResourceString(getClass().getName() + "@Invalid Folder Path"), JOptionPane.OK_OPTION);

               return;

            }

            if (results.length == 0)
            {
               // should never happen
               m_applet.debugMessage("No folder path found for id " + id);
               return;
            }
            String path = results[0] + "/" + node.getName();
            ((PSMainView) m_applet.m_mainView).getNavTree().setSelectionPath(path, false);
            return;
         }
         else if (action.getName().equals(ACTION_SEARCH_WITHIN_FOLDER))
         {
            /*
             * This action depends on the current behavior that the
             * SearchResults parent node loads it's children before the applet
             * starts.
             */
            PSNode node = (PSNode) selection.getNodeList().next();
            if (!node.isAnyFolderType())
            {
               // may happen if action mis-configured
               JOptionPane.showConfirmDialog(
                     m_applet.getDialogParentFrame(),
                     m_applet.getResourceString(getClass().getName()
                           + "@This action is only supported on folders. See your "
                           + "admin to correct the Rhythmyx configuration."),
                     m_applet.getResourceString(getClass().getName() + "@Wrong Node Type"), JOptionPane.OK_OPTION);

               return;
            }
            if (null == m_emptySearchNode)
            {
               // should never happen
               m_applet.debugMessage("No empty search node set before action '" + action.getLabel() + "' was called.");
               return;
            }
            String path = getNavSelectionPath();
            if (path.length() == 0)
            {
               // should never happen
               m_applet.debugMessage("Trying to perform action " + action.getLabel() + ", but no node selected.");
               return;
            }
            /*
             * If the selected item is in the main display pane, path will only
             * be the tree part. In that case, we need to add the last part onto
             * the path.
             */
            if (!path.endsWith(node.getLabel()))
            {
               if (!path.endsWith("/"))
                  path += "/";
               path += node.getLabel();
            }

            PSSelection sel = new PSSelection(new PSUiMode(PSUiMode.TYPE_VIEW_CX, PSUiMode.TYPE_MODE_NAV), null,
                  PSIteratorUtils.iterator(m_emptySearchNode));
            PSMenuAction defAction = findDefaultAction(sel);
            if (defAction != null)
               executeAction(defAction, sel, path);
            return;
         }
         else if (!action.getName().equals(ACTION_NO_ENTRIES))
         {
            JOptionPane.showMessageDialog(null, "Action '" + action.getName() + "' not implemented yet");
         }
      }
      catch (PSException e)
      {
         m_applet.debugMessage(e);
         m_applet.displayErrorMessage(null, getClass(), "Error executing action <{0}> : {1}", new String[]
         {action.getName(), e.getLocalizedMessage()}, "Error", null);
         e.printStackTrace();
      }
      catch (PSContentExplorerException e)
      {
         m_applet.debugMessage(e);
         m_applet.displayErrorMessage(null, getClass(), "Error executing action <{0}> : {1}", new String[]
         {action.getName(), e.getLocalizedMessage()}, "Error", null);
         e.printStackTrace();
      }
      finally
      {
         m_applet.resetCursor();
      }
   }

   /**
    * Handle the PurgeNav action
    *
    * @param selection
    */
   private void handlePurgeNavAction(PSSelection selection)
   {
      int ok = JOptionPane.showConfirmDialog(
            null,
            m_applet.getResourceString(getClass().getName() + "@Delete navigation cannot be undone. "
                  + "Are you sure you want to delete?"),
            m_applet.getResourceString(getClass().getName() + "@Confirm delete"), JOptionPane.YES_NO_OPTION);

      if (ok != JOptionPane.YES_OPTION)
         return;
      try
      {
         m_folderMgr.purgeAllNav((PSNode) selection.getNodeList().next());
      }
      catch (PSCmsException e)
      {
         m_applet.debugMessage(e);
         m_applet.displayErrorMessage(null, getClass(), "@Could not Navigation from folder : {0}", new String[]
         {e.getLocalizedMessage()}, "Error", null);
      }
      // dirty the selected node
      informListeners(selection.getNodeList(), PSActionEvent.DIRTY_NODES);

      informListeners(PSActionEvent.REFRESH_NAV_SELECTED);
   }

   /**
    * Handle the PurgeAll action
    *
    * @param selection
    */
   private void handlePurgeAllAction(PSSelection selection)
   {
      int ok = JOptionPane.showConfirmDialog(
            null,
            m_applet.getResourceString(getClass().getName()
                  + "@This will permanently delete content and all subfolder content"
                  + "Are you sure you want to do this?"),
            m_applet.getResourceString(getClass().getName() + "@Confirm delete"), JOptionPane.YES_NO_OPTION);

      if (ok != JOptionPane.YES_OPTION)
         return;

      try
      {
         m_folderMgr.purgeAllContent(selection.getParent(), selection.getNodeList());
      }
      catch (PSCmsException e)
      {
         m_applet.debugMessage(e);
         m_applet.displayErrorMessage(null, getClass(), "@Could not purge folder and content : {0}", new String[]
         {e.getLocalizedMessage()}, "Error", null);
      }
      // dirty the selected node
      informListeners(selection.getNodeList(), PSActionEvent.DIRTY_NODES);

      if (selection.getMode().getViewMode().equals(PSUiMode.TYPE_CXMAN))
         informListeners(PSActionEvent.REFRESH_NAV_SELECTED);
      else
         informListeners(PSActionEvent.REFRESH_NAV_SEL_PARENT);
   }

   /**
    * Creates a new wizard dialog used to request user input for the copy site
    * action.
    *
    * @param source the source node that will be copied, assumed not
    *           <code>null</code>.
    * @param target the target node to which the source will be copied, assumed
    *           not <code>null</code>.
    * @return a new wizard dialog for the copy site action, never
    *         <code>null</code>.
    * @throws PSCmsException for any error creating the wizard dialog.
    */
   private PSWizardDialog createCopySiteWizard(PSNode source, PSNode target) throws PSCmsException
   {
      try
      {
         m_applet.setWaitCursor();

         Object[][] pages =
         {
               {PSWizardStartFinishPanel.class.getName(), null,
                     m_applet.getResourceString(getClass(), "Copy Site Start Page Instruction")},
               {PSCopySiteNamePage.class.getName(),
                     new PSCopySiteNamePage.InputData(source, getSiteCataloger().getSites(), getChildFolders(target)),
                     m_applet.getResourceString(getClass(), "Copy Site Name Page Instruction")},
               {PSCopySiteCopyOptionsPage.class.getName(), null,
                     m_applet.getResourceString(getClass(), "Copy Site Copy Options Page Instruction")},
               {PSContentCopyOptionsPage.class.getName(), null,
                     m_applet.getResourceString(getClass(), "Copy Site Content Copy Options Page Instruction")},
               {
                     PSCommunityMappingsPage.class.getName(),
                     new PSCommunityMappingsPage.InputData(getCommunityContentTypeMapper(), getCommunityCataloger()
                           .getCommunities(), m_folderMgr.getFolderCommunities(source)),
                     m_applet.getResourceString(getClass(), "Copy Site Community Mapping Page Instruction")},
               {PSWizardStartFinishPanel.class.getName(), null,
                     m_applet.getResourceString(getClass(), "Copy Site Finish Page Instruction")}};

         String title = m_applet.getResourceString(getClass(), "Copy Site: ") + source.getName();

         return new PSWizardDialog(m_applet.getDialogParentFrame(), pages, title, m_applet);
      }
      finally
      {
         m_applet.resetCursor();
      }
   }

   /**
    * Checks if the location of <code>child</code> in the navigation tree is at
    * or under the supplied <code>path</code>.
    *
    * @param path Should be of the form "//a/b", where "//" is the only required
    *           part. Assumed not <code>null</code> or empty. Trailing slashes
    *           do not affect the comparison, which is done case-insensitive.
    * @param child Assumed not <code>null</code>.
    * @return If the path of the tree element associated with <code>child</code>
    *         begins with <code>path</code>, <code>true</code>, otherwise
    *         <code>false</code>.
    */
   private boolean isChild(String path, PSNode child)
   {
      String nodePath = m_applet.getNavTree().getPath(child);
      if (path.endsWith("/"))
         path = path.substring(0, path.length() - 1);
      return nodePath.toLowerCase().startsWith(path.toLowerCase());
   }

   /**
    * Create the site cloning options.
    *
    * @param data an array of wizard page output data, assumed not
    *           <code>null</code>.
    * @return the site cloning options, never <code>null</code>.
    */
   private PSCloningOptions createSiteCloningOptions(Object[] data)
   {
      PSCopySiteNamePage.OutputData nameOptions = (PSCopySiteNamePage.OutputData) data[1];
      PSCopySiteCopyOptionsPage.OutputData copyOptions = (PSCopySiteCopyOptionsPage.OutputData) data[2];
      PSContentCopyOptionsPage.OutputData contentCopyOptions = (PSContentCopyOptionsPage.OutputData) data[3];
      PSCommunityMappingsPage.OutputData communityMappings = (PSCommunityMappingsPage.OutputData) data[4];

      return new PSCloningOptions(PSCloningOptions.TYPE_SITE, nameOptions.getCopiedSiteName(),
            nameOptions.getNewSiteName(), nameOptions.getFolderName(), copyOptions.getCopyOption(),
            contentCopyOptions.getCopyOption(), communityMappings);
   }

   /**
    * Get a collection with all child folder names for the supplied target node.
    *
    * @param target the target node for whicch to get the child folder names,
    *           assumed not <code>null</code>.
    * @return a collection of child folder names as <code>String</code> objects,
    *         never <code>null</code>, may be empty.
    */
   private Collection getChildFolders(PSNode target)
   {
      Collection<String> folderNames = new ArrayList<String>();
      Iterator targetChildren = target.getChildren();
      while (targetChildren != null && targetChildren.hasNext())
      {
         PSNode targetChild = (PSNode) targetChildren.next();
         if (targetChild.isAnyFolderType())
            folderNames.add(targetChild.getName());
      }

      return folderNames;
   }

   /**
    * Creates a new wizard dialog used to request user input for the copy site
    * subfolder action.
    *
    * @param source the source node that will be copied, assumed not
    *           <code>null</code>.
    * @param target the target node to which the source will be copied, assumed
    *           not <code>null</code>.
    * @return a new wizard dialog for the copy site subfolder action, never
    *         <code>null</code>.
    * @throws PSCmsException for any error creating the wizard dialog.
    */
   private PSWizardDialog createCopySiteSubfolderWizard(PSNode source, PSNode target) throws PSCmsException
   {
      try
      {
         m_applet.setWaitCursor();

         Object[][] pages =
         {
               {PSWizardStartFinishPanel.class.getName(), null,
                     m_applet.getResourceString(getClass(), "Copy Site Subfolder Start Page Instruction")},
               {PSCopySiteSubfolderNamePage.class.getName(),
                     new PSCopySiteSubfolderNamePage.InputData(getChildFolders(target)),
                     m_applet.getResourceString(getClass(), "Copy Site Subfolder Name Page Instruction")},
               {PSCopySiteSubfolderCopyOptionsPage.class.getName(), null,
                     m_applet.getResourceString(getClass(), "Copy Site Subfolder Copy Options Page Instruction")},
               {
                     PSContentCopyOptionsPage.class.getName(),
                     null,
                     m_applet
                           .getResourceString(getClass(), "Copy Site Subfolder Content Copy Options Page Instruction")},
               {
                     PSCommunityMappingsPage.class.getName(),
                     new PSCommunityMappingsPage.InputData(getCommunityContentTypeMapper(), getCommunityCataloger()
                           .getCommunities(), m_folderMgr.getFolderCommunities(source)),
                     m_applet.getResourceString(getClass(), "Copy Site Subfolder Community Mapping Page Instruction")},
               {PSWizardStartFinishPanel.class.getName(), null,
                     m_applet.getResourceString(getClass(), "Copy Site Subfolder Finish Page Instruction")}};

         String title = m_applet.getResourceString(getClass(), "Copy Site Subfolder: ") + source.getName();

         return new PSWizardDialog(m_applet.getDialogParentFrame(), pages, title, m_applet);
      }
      finally
      {
         m_applet.resetCursor();
      }
   }

   /**
    * Create the site subfolder cloning options.
    *
    * @param data an array of page output data, assumed not <code>null</code>.
    * @return the site subfolder cloning options, never <code>null</code>.
    */
   private PSCloningOptions createSiteSubfolderCloningOptions(Object[] data)
   {
      PSCopySiteSubfolderNamePage.OutputData nameOptions = (PSCopySiteSubfolderNamePage.OutputData) data[1];
      PSCopySiteSubfolderCopyOptionsPage.OutputData copyOptions = (PSCopySiteSubfolderCopyOptionsPage.OutputData) data[2];
      PSContentCopyOptionsPage.OutputData contentCopyOptions = (PSContentCopyOptionsPage.OutputData) data[3];
      PSCommunityMappingsPage.OutputData communityMappings = (PSCommunityMappingsPage.OutputData) data[4];

      return new PSCloningOptions(PSCloningOptions.TYPE_SITE_SUBFOLDER, nameOptions.getFolderName(),
            copyOptions.getCopyOption(), contentCopyOptions.getCopyOption(), communityMappings);
   }

   /**
    * Easy access method for the new search node.
    *
    * @return new search <code>PSNode</code> object, never <code>null</code>.
    */
   PSNode getNewSearchNode()
   {
      return m_newSearchNode;
   }

   /**
    * Access method for search results node.
    *
    * @return results {@link PSNode} object might be <code>null</code>
    */
   PSNode getSearchResultsNode()
   {
      return m_searchResultsNode;
   }

   /**
    * Checks whether the supplied action requires any selection or not. The
    * actions <code>ACTION_EDIT_FOLDER</code>, <code>ACTION_DELETE</code>,
    * <code>ACTION_SLOT_SEARCH</code>, <code>ACTION_CXT_HELP</code>,
    * <code>ACTION_EDIT_SEARCH</code>, <code>ACTION_SAVEAS</code>,
    * <code>ACTION_CHANGE_DF</code>, <code>ACTION_COPY</code>
    * <code>ACTION_ARRANGE_REMOVE</code>, <code>ACTION_MOVE_UPLEFT</code>,
    * <code>ACTION_MOVE_DOWNRT</code> and any Paste actions requires selection
    * and others do not.
    *
    * @param action the action to check, assumed not <code>null</code>
    *
    * @return <code>true</code> if it requires, otherwise <code>false</code>
    */
   private boolean requireSelection(PSMenuAction action)
   {
      boolean requireSel = false;
      String name = action.getName();
      if (name.startsWith(PSMenuAction.PREFIX_COPY_PASTE) || name.startsWith(PSMenuAction.PREFIX_DROP_PASTE)
            || name.equals(ACTION_EDIT_FOLDER) || name.equals(ACTION_DELETE) || name.equals(ACTION_FORCE_DELETE)
            || name.equals(ACTION_SLOT_SEARCH) || name.equals(ACTION_CXT_HELP) || name.equals(ACTION_EDIT_SEARCH)
            || name.equals(ACTION_SAVEAS) || name.equals(ACTION_CHANGE_DF) || name.equals(ACTION_COPY)
            || name.equals(ACTION_ARRANGE_REMOVE) || name.equals(ACTION_MOVE_UPLEFT) || name.equals(ACTION_MOVE_DOWNRT)
            || name.equals(ACTION_LINK_TO_SLOT))
      {
         requireSel = true;
      }

      return requireSel;
   }

   /**
    * Helper method to know whether and action is a workflow action or not. This
    * is determined by the fact that all workflow actions expect the HTML
    * parameter sys_command=workflow.
    *
    * @param action action of interest, must not be <code>null</code>.
    * @return <code>true</code> if it is a workflow action, <code<false</code>
    *         otherwise.
    * @throws IllegalArgumentException
    */
   private boolean isWorkflowAction(PSMenuAction action)
   {
      if (action == null)
         throw new IllegalArgumentException("action must not be null");

      PSParameters params = action.getParameters();
      if (params == null)
         return false;

      String propWf = params.getParameter(PSContentExplorerConstants.ACTION_PARAM_SYS_COMAND);

      if (propWf != null && propWf.equalsIgnoreCase("workflow"))
         return true;

      return false;
   }

   /**
    * The method that handles all actions whose handler is 'SERVER'. Sever
    * actions in turn are of two types, namely, ones that launch a new browser
    * window and the other execute without launching the window (like checkin or
    * checkout).
    *
    * @param action the action to execute, assumed not <code>null</code> and
    *           handler is <code>PSAction.HANDLER_SERVER</code>.
    * @param selection the selection of nodes, may be <code>null</code> ignored
    *           if the action does not depend on a selection.
    */
   private void executeServerAction(PSMenuAction action, PSSelection selection)
   {
      try
      {
         m_applet.setWaitCursor();
         try
         {
            // make sure we don't have a stale revision
            if (!checkDynamicParams(action, selection))
               return;

            PSProperties props = action.getProperties();
            // Set the defaults for properties
            String target = PSContentExplorerConstants.ACTION_TARGET_DEFAULT;
            String style = PSContentExplorerConstants.ACTION_TARGET_STYLE_DEFAULT;
            String launchNewWindow = PSContentExplorerConstants.ACTION_LAUNCHES_WINDOW_DEFAULT;
            if (props != null) // override the defaults
            {
               launchNewWindow = props.getProperty(PROPERTY_LAUNCHES_WINDOW, PSContentExplorerConstants.ACTION_LAUNCHES_WINDOW_DEFAULT);
               target = props.getProperty(PROPERTY_TARGET, PSContentExplorerConstants.ACTION_TARGET_DEFAULT);
               style = props.getProperty(PROPERTY_TARGET_STYLE, PSContentExplorerConstants.ACTION_TARGET_STYLE_DEFAULT);
            }

            String actionUrl = makeUrlForServerAction(action, selection);
            if (actionUrl.length() < 1)
            {
               String message = m_applet.getResourceString(getClass(), "CommandFailure");
               throw new Exception(message + " no action url");
            }
            if(actionUrl.startsWith("../")){
               actionUrl = actionUrl.substring(3);
            }
            if (launchNewWindow.equalsIgnoreCase(PSContentExplorerConstants.CONST_YES))
            {

               URL absUrl = new URL(m_applet.getRhythmyxCodeBase(), actionUrl);
               WindowScript script = null;
               /*
                * It has been observed that some browsers and plugin
                * combinations have a MAX_SHOWWINDOW_URL_LENGTH character limit
                * on the url that can be passed to the ShowWindow function. If
                * the URL length is less than MAX_SHOWWINDOW_URL_LENGTH
                * Characters send it to the ShowWindow function.
                */
               if (absUrl.toExternalForm().trim().length() < PSContentExplorerConstants.MAX_GET_URL_LENGTH){

                  script = new WindowScript(absUrl.toExternalForm(), target, style, action, selection);
               }
               /*
                * It has been observed that the get method has a limitation of
                * MAX_GET_URL_LENGTH. If the url is less than MAX_GET_URL_LENGTH
                * then post the url to the server to save it in session.
                */
               else if (absUrl.toExternalForm().trim().length() < PSContentExplorerConstants.MAX_GET_URL_LENGTH)
               {
                  // Post the url to the server where it gets saved in the
                  // session
                  Map<String, String> params = new HashMap<String, String>();
                  params.put(PSContentExplorerConstants.SYS_SERVERACTIONURL, actionUrl);
                  postData(PSContentExplorerConstants.SAVE_SERVERACTIONURL, params);
                  // Execute this url which gets the actual server action url
                  // and redirects.
                  script = new WindowScript(EXECUTE_SERVERACTIONURL, target, style, action, selection);
               }
               else
               {
                  ErrorDialogs.showErrorMessage(null, m_applet.getResourceString(getClass(), "TooManySelected"),
                        m_applet.getResourceString(getClass(), "Error"));
                  return;
               }
               m_applet.debugMessage("script = " + script);
               script.show();
            }
            else
            {
               Map params = new HashMap();
               String sUrl = PSContentExplorerUtils.splitUrl(actionUrl, params);
               URL url = new URL(m_applet.getRhythmyxCodeBase(), sUrl);
               try
               {
                  m_applet.getHttpConnection().postData(url, params);
               }
               catch (Exception e)
               {
                  ErrorDialogs.showErrorMessage(null, e.getMessage(), m_applet.getResourceString(getClass(), "Error"));
                //logout completely once session timeedout
                  if(m_applet.getParameter("SWING") != null && m_applet.getParameter("SWING").equals("true") && e.getMessage().contains("You must log back into Rhythmyx to continue")){
                     ((PSContentExplorerFrame)m_applet.getDialogParentFrame()).cleanup();

                     Platform.exit();
                     System.exit(0);
                  }

                  return;
               }
            }

            String refreshHint = props.getProperty("refreshHint");
            if (StringUtils.isNotEmpty(refreshHint))
            {
               informListeners(refreshHint);
            }
         }
         catch (JSException jse)
         {
            String locMsg = jse.getLocalizedMessage();
            m_applet.debugMessage(jse);
            if (locMsg == null || locMsg.length() == 0)
               locMsg = "Popup Blocker Error Message";

            m_applet.displayErrorMessage(null, getClass(), locMsg, null, "Error", null);
         }
         catch (Exception e)
         {
            m_applet.debugMessage(e);
            m_applet.displayErrorMessage(null, getClass(), "Error executing action <{0}> : {1}", new String[]
            {action.getName(), e.getLocalizedMessage()}, "Error", null);

         }
      }
      finally
      {
         m_applet.resetCursor();
      }
   }

   /**
    * Makes action Url for a server action by replacing the dynamic parameter
    * values from each node properties in the selection or clipboard dependening
    * on the context of action. This method behaves different for a normal
    * context and dragNdrop or copy-paste contexts.
    * <p>
    * For the first case: The dynamic parameter values are taken from the node
    * properties of the current selection and the special properties, viz.
    * sys_folderid and sys_slotid are taken from the parent node of the
    * selection.
    * <p>
    * For the second case: The dynamic parameter values correspond to the nodes
    * in the clipboard and the special properties correspond to the selected or
    * drop target nodes.
    * <p>
    *
    * @param action must not be <code>null</code>
    * @param selection may be <code>null</code> in which case the unchanged url
    *           is returned.
    * @return new url with dynamic values for the parameters, may be
    *         <code>empty</code> but not <code>null</code>
    */
   private String makeUrlForServerAction(PSMenuAction action, PSSelection selection)
   {
      if (action == null)
         throw new IllegalArgumentException("action must not be null");

      String actionUrl = action.getURL();

      actionUrl = removeNewLineCharacterFromString(actionUrl);

      if (selection == null)
         return actionUrl;

      PSParameters actionParams = action.getParameters();
      if (actionParams == null)
         return actionUrl;

      if (action.getName().startsWith(PSMenuAction.PREFIX_COPY_PASTE)
            || action.getName().startsWith(PSMenuAction.PREFIX_DROP_PASTE))
      {
         // Special cases of Copy-Paste or drag-n-drop are treated differently
         int clipType = PSClipBoard.TYPE_COPY;
         if (action.getName().startsWith(PSMenuAction.PREFIX_DROP_PASTE))
            clipType = PSClipBoard.TYPE_DRAG;

         Iterator nodes = m_clipBoard.getClip(clipType);

         // Process the dyamic parameters for each node
         while (nodes.hasNext())
         {
            actionUrl = appendDynamcParams(action, actionUrl, actionParams, (PSNode) nodes.next(), selection.getMode()
                  .getViewMode());
         }
         Iterator tgtNodes = selection.getNodeList();
         while (tgtNodes.hasNext())
         {
            PSNode tgtNode = (PSNode) tgtNodes.next();
            // Process the special parameters
            actionUrl = appendSpecialParams(actionUrl, actionParams, tgtNode);
         }
         // Process the static parameters
         actionUrl = appendStaticParams(actionUrl, actionParams);
         // append the refreshHint param to the actionParams
         appendRefreshHint(action, actionParams);

      }
      else
      {
         actionUrl = makeUrl(action, selection);
         // Process the special parameters
         actionUrl = appendSpecialParams(actionUrl, actionParams, selection.getParent());
      }

      m_applet.debugMessage("makeUrlForServerAction() = " + actionUrl);

      return actionUrl;
   }

   /**
    * Method to remove new line characters from the action URL supplied.
    * The currenlty supported new line characters are :
    * {"\n", "\r", "\r\n"}
    *
    * @param actionURL the action url to which the dynamic parameters are
    *           appended, must not be <code>null</code>
    *
    * @return new url with new line characters removed from it
    */
   private String removeNewLineCharacterFromString(String actionURL){
      String[] newLineCharArray = {"\n", "\r", "\r\n"};

      for(int i = 0; i<newLineCharArray.length; i++){
         if(actionURL.contains(newLineCharArray[i])){
            actionURL = actionURL.replace(newLineCharArray[i], "");
         }
      }
      actionURL = actionURL.trim();
      return actionURL;
   }

   /**
    * Method to transform the special dynamic properties from the supplied
    * target node to the action URL supplied. The currenlty supported special
    * propeties are:
    * <ol>
    * <li>{@link IPSHtmlParameters#SYS_FOLDERID}, and</li>
    * <li>{@link IPSHtmlParameters#SYS_SLOTID}</li>
    * </ol>
    *
    * @param actionUrl the action url to which the dynamic parameters are
    *           appended, must not be <code>null</code>
    * @param actionParams action parameters object from which the parameter keys
    *           are looked up, may be <code>null</code> in which case the
    *           unchanged url is returned.
    * @param tgtNode node from which the dynamic values for the parameters are
    *           taken, may be <code>null</code> in which case the unchanged url
    *           is returned.
    * @return new url with dynamic values for the parameters, may be
    *         <code>empty</code> but never <code>null</code>
    */
   private String appendSpecialParams(String actionUrl, PSParameters actionParams, PSNode tgtNode)
   {
      if (actionUrl == null)
         throw new IllegalArgumentException("actionUrl must not be null");

      if (actionParams == null)
         return actionUrl;

      if (tgtNode == null)
         return actionUrl;

      Iterator keys = actionParams.getParamKeys();
      String key, value, paramName = null;
      while (keys.hasNext())
      {
         key = keys.next().toString();
         value = actionParams.getParameter(key);
         if (!value.startsWith("$") || value.startsWith("$$"))
         {
            // Not a special parameter - skip
            continue;
         }
         value = value.substring(1);
         if (!ms_specialParams.contains(value))
         {
            // not a special param - skip
            continue;
         }

         if (value.equals(IPSHtmlParameters.SYS_FOLDERID))
         {
            paramName = IPSHtmlParameters.SYS_FOLDERID;
            // If the target node is of type folder use its contentid
            if (tgtNode.isAnyFolderType())
               value = tgtNode.getProp(PROPERTY_CONTENTID);
            else
               // else use sys_folderid
               value = tgtNode.getProp(paramName);

         }
         else if (value.equals(IPSHtmlParameters.SYS_SLOTID))
         {
            paramName = IPSHtmlParameters.SYS_SLOTID;
            value = tgtNode.getProp(PROPERTY_SLOTID);
         }
         if (paramName != null && StringUtils.isNotEmpty(value))
            actionUrl = appendParamToUrl(actionUrl, paramName, value);
      }
      return actionUrl;
   }

   /**
    * Makes action Url by replacing the dynamicparameter values from each node
    * properties in the selection appending the static parameters in the last.
    *
    * @param action must not be <code>null</code>
    * @param selection may be <code>null</code> in which case the unchanged url
    *           is returned.
    * @return new url with dynamic values for the parameters, may be
    *         <code>empty</code> but not <code>null</code>
    */
   private String makeUrl(PSMenuAction action, PSSelection selection)
   {
      if (action == null)
         throw new IllegalArgumentException("action must not be null");

      String actionUrl = action.getURL();

      actionUrl = removeNewLineCharacterFromString(actionUrl);

      if (selection == null)
         return actionUrl;

      PSParameters actionParams = action.getParameters();
      if (actionParams == null)
         return actionUrl;

      // append the refreshHint param to the actionParams
      appendRefreshHint(action, actionParams);

      if (action.getName().startsWith(PSMenuAction.PREFIX_COPY_PASTE)
            || action.getName().startsWith(PSMenuAction.PREFIX_DROP_PASTE))
      {
         int clipType = PSClipBoard.TYPE_COPY;
         if (action.getName().startsWith(PSMenuAction.PREFIX_DROP_PASTE))
            clipType = PSClipBoard.TYPE_DRAG;
         Iterator nodes = m_clipBoard.getClip(clipType);

         // Process the dyamic parameters for source nodes
         while (nodes.hasNext())
         {
            actionUrl = appendDynamcParams(action, actionUrl, actionParams, (PSNode) nodes.next(), selection.getMode()
                  .getViewMode());
         }
         Iterator tgtNodes = selection.getNodeList();
         while (tgtNodes.hasNext())
         {
            PSNode tgtNode = (PSNode) tgtNodes.next();
            // Process the special parameters
            actionUrl = appendSpecialParams(actionUrl, actionParams, tgtNode);
         }
         // Process the static parameters
         actionUrl = appendStaticParams(actionUrl, actionParams);
      }
      else
      {
         Iterator nodes = selection.getNodeList();
         // Process the dyamic parameters for each node
         while (nodes.hasNext())
         {
            PSNode psn = (PSNode) nodes.next();
            actionUrl = appendDynamcParams(action, actionUrl, actionParams, psn, selection.getMode().getViewMode());
         }

         // Done with dynamic params, now resolve the special params of
         // selection's parent
         PSNode node = selection.getParent();
         if (node != null && node.isAnyFolderType())
         {
            // if selection parent is of type folder the target node will be
            // the selection parent
            actionUrl = appendSpecialParams(actionUrl, actionParams, node);
         }
         else
         {
            // Otherwise, iterate through entire selection
            nodes = selection.getNodeList();
            // Process the special parameters for each node
            while (nodes.hasNext())
            {
               PSNode psn = (PSNode) nodes.next();
               actionUrl = appendSpecialParams(actionUrl, actionParams, psn);
            }
         }
         // Process the static parameters
         actionUrl = appendStaticParams(actionUrl, actionParams);
      }

     // m_applet.debugMessage("makeUrl() = " + actionUrl);

      return actionUrl;
   }

   /**
    * Special method to Paste action Url. This could be a complex computation
    * based on clipboard (mormal or drag-drop) content and current selection.
    * The base Url is hardcoded.
    *
    * @param action must not be <code>null</code>
    * @param selection must not be <code>null</code>.
    * @return new url to get the paste menu applicable to current context, not
    *         <code>null</code> or <code>empty</code>.
    */
   private String makePasteUrl(PSMenuAction action, PSSelection selection)
   {
      if (action == null)
         throw new IllegalArgumentException("action must not be null");

      if (selection == null || selection.getNodeListSize() < 1)
         throw new IllegalArgumentException("selection must not be null or empty");

      /**
       * @todo actions to be mergded and filtered based several conditions
       */
      PSNode node = (PSNode) selection.getNodeList().next();
      String actionUrl = "../sys_cxSupport/ActionList.html?sys_action=" + ACTION_PASTE + "&sys_mode="
            + selection.getMode().getViewMode() + "&sys_uicontext=" + node.getType();

      m_applet.debugMessage("makePasteUrl() = " + actionUrl);

      return actionUrl;
   }

   /**
    * Makes action Url dynamic by replacing the parameter values from the
    * supplied node properties and appending the static parameters in the last.
    *
    * @param action must not be <code>null</code>
    * @param node may be <code>null</code> in which case the unchanged url is
    *           returned.
    * @param mode the mode of the applet in which the action is executing,
    *           assumed not to be <code>null</code> or empty and one of valid
    *           view modes in <code>
    * PSUiMode</code>
    * @param selParent the selection's parent node
    * @return new url with dynamic values for the parameters, may be
    *         <code>empty</code> but not <code>null</code>
    */
   @SuppressWarnings("unused")
   String makeUrl(PSMenuAction action, PSNode node, String mode, PSNode selParent) throws PSContentExplorerException
   {
      if (action == null)
         throw new IllegalArgumentException("action must not be null");

      String actionUrl = action.getURL();

      actionUrl = removeNewLineCharacterFromString(actionUrl);

      if (node == null)
         return actionUrl;

      PSParameters actionParams = action.getParameters();
      if (actionParams == null)
         return actionUrl;

      // append the refreshHint param to the actionParams
      appendRefreshHint(action, actionParams);
      // Process the dyamic parameters for each node
      actionUrl = appendDynamcParams(action, actionUrl, actionParams, node, mode);
      // Process the static parameters
      actionUrl = appendStaticParams(actionUrl, actionParams);

      // Done with dynamic params, now resolve the special params
      actionUrl = appendSpecialParams(actionUrl, actionParams, selParent);
     // m_applet.debugMessage("makeUrl() = " + actionUrl);

      return actionUrl;
   }

   /**
    * refreshHint is a property of an action but for server actions we need to
    * pass it as a parameter if exists. Incase if user adds a parameter called
    * refreshHint, then do not touch it.
    *
    * @param action must not be <code>null</code>
    * @param actionParams must not be <code>null</code>
    */
   private void appendRefreshHint(PSMenuAction action, PSParameters actionParams)
   {
      String refreshHint = action.getProperty(PSAction.PROP_REFRESH_HINT);
      if (!action.isClientAction() && actionParams.getParameter(PSAction.PROP_REFRESH_HINT) == null
            && refreshHint != null && refreshHint.trim().length() > 0)
      {
         actionParams.setParameter(PSAction.PROP_REFRESH_HINT, refreshHint);
      }

   }

   /**
    * Appends static parameters to action Url from the action parameter list.
    *
    * @param actionUrl must not be <code>null</code> or <code>empty</code>
    * @param actionParams action parameters object may be <code>null</code> in
    *           which case the unchanged url is returned.
    * @return new url with static the parameters, may be <code>empty</code> but
    *         not <code>null</code>
    */
   private String appendStaticParams(String actionUrl, PSParameters actionParams)
   {
      if (actionUrl == null)
         throw new IllegalArgumentException("actionUrl must not be null");

      if (actionParams == null)
         return actionUrl;

      String key = null;
      String value = null;
      Iterator iter = actionParams.getParamKeys();
      while (iter.hasNext())
      {
         key = iter.next().toString();
         value = actionParams.getParameter(key);
//         //CMS-8722 : the psredirect parameter was giving malformed URL exception while redirecting
//         if(key.equalsIgnoreCase("psredirect")){
//            URL serverCodeBaseURL = m_applet.getCodeBase();
//            String protocol = serverCodeBaseURL.getProtocol();
//            String host = serverCodeBaseURL.getHost();
//            String port = String.valueOf(serverCodeBaseURL.getPort());
//            value = value.replaceFirst("../",protocol+"://"+host+":"+port+"/Rhythmyx/");
//         }
         if (value.startsWith("$")) // Dynamic value, already dealt with
            continue;
         if (PASS_THRU_PARAMS.contains(key))
            continue;
         actionUrl = appendParamToUrl(actionUrl, key, value);
      }
      return actionUrl;
   }

   /**
    * Appends dynamic parameters to action Url by replacing the parameter values
    *
    * @param action must not be <code>null</code> or <code>empty</code>
    * @param actionUrl must not be <code>null</code>
    * @param actionParams action parameters object may be <code>null</code> in
    *           which case the unchanged url is returned.
    * @param node node from which the dynamic values for the parameters are
    *           taken, may be <code>null</code> in which case the unchanged url
    *           is returned.
    * @param mode the mode of the applet in which the action is executing,
    *           assumed not to be <code>null</code> or empty and one of valid
    *           view modes in <code>
    * PSUiMode</code>
    * @return new url with static the parameters, may be <code>empty</code> but
    *         not <code>null</code>
    */
   private String appendDynamcParams(PSMenuAction action, String actionUrl, PSParameters actionParams, PSNode node,
         String mode)
   {
      if (action == null)
         throw new IllegalArgumentException("action must not be null");

      if (actionUrl == null)
         throw new IllegalArgumentException("actionUrl must not be null");

      if (actionParams == null)
         return actionUrl;

      if (node == null)
         return actionUrl;

      String key = null;
      String value = null;
      PSProperties nodeProps = node.getProperties();
      if (nodeProps != null)
      {
         Iterator iter = actionParams.getParamKeys();
         while (iter.hasNext())
         {
            key = iter.next().toString();
            value = actionParams.getParameter(key);
            if (StringUtils.isEmpty(value))
            {
               continue;
            }
            if (!value.startsWith("$")) // Not a dynamic value - skip
               continue;

            if (value.startsWith("$$"))
            {
               String resParam = value.substring(2);
               if (resParam.equals(PROPERTY_MODE))
                  value = mode;
               else if (resParam.equals(PROPERTY_UICONTEXT))
                  value = node.getType();
               else if (resParam.equals(PROPERTY_COMMUNITY))
                  value = String.valueOf(m_applet.getUserInfo().getCommunityId());
               else if (resParam.equals(PROPERTY_LANG))
                  value = m_applet.getUserInfo().getLocale();
               else if (resParam.equals(PROPERTY_USERNAME))
                  value = m_applet.getUserInfo().getUserName();
               else
               {
                  m_applet.debugMessage("Unknown special/dynamic parameter value " + resParam);
                  continue;
               }
            }
            else
               value = nodeProps.getProperty(value.substring(1));
            if (StringUtils.isEmpty(value) || ms_specialParams.contains(value))
            {
               // special param - skip
               continue;
            }
            /*
             * Node's sys_revision property holds current revision, when an item
             * is checked out by some one else but force checkin action needs
             * edit revision which is one revision more than the current
             * revision. Bump up the revision by one only if it is force checkin
             * action
             */
            if (action.getName().equals(ACTION_FORCE_CHECKIN)
                  && key.equals(PROPERTY_REVISION))
            {
               value = node.getProp(PROPERTY_TIPREVISION);
            }

            actionUrl = appendParamToUrl(actionUrl, key, value);
         }
      }
      return actionUrl;
   }

   /**
    * Appends the parameter suitably at the nd of the Url string. If there are
    * already one or more parameters in the string, the new parameter is added
    * with and '&' otherwsie with '?'.
    *
    * @param Url must not be <code>null<code> or <code>empty</code>. Validity of
    *           he Url supplied is not checked.
    * @param name must not be <code>null<code> or <code>empty</code>.
    * @param value may be <code>null<code> or <code>empty</code>. This will be
    *           URLEncoded.
    * @return new Url string, never <code>null<code> or <code>empty</code>
    * @throws IllegalArgumentException if parameters supplied are invalid.
    */
   private String appendParamToUrl(String Url, String name, String value)
   {
      if (Url == null || Url.trim().length() < 1)
         throw new IllegalArgumentException("Url must not be null or empty");
      if (name == null || name.trim().length() < 1)
         throw new IllegalArgumentException("name must not be null or empty");
      if (value == null)
         value = "";
      value = PSURLEncoder.encodePath(value);
      if (Url.indexOf('?') == -1)
      {
         Url = Url + "?" + name + "=" + value;
      }
      else
      {
         Url = Url + "&" + name + "=" + value;
      }
      return Url;
   }

   /**
    * Notifies the action listeners that the action is initiated and sets with
    * the process monitor to get the process status on.
    *
    * @param monitor the monitor that gets updated with status, assumed not
    *           <code>null</code>.
    */
   private void informListeners(PSProcessMonitor monitor)
   {
      Iterator listeners = m_actionListeners.iterator();
      while (listeners.hasNext())
      {
         IPSActionListener listener = (IPSActionListener) listeners.next();
         listener.actionInitiated(monitor);
      }
   }

   /**
    * Convenience method that calls {@link #informListeners(String, boolean)
    * informListeners(hint, false)}.
    */
   void informListeners(String hint)
   {
      informListeners(hint, false);
   }

   /**
    * Notifies the action listeners that the action is executed.
    *
    * @param hint the hint that describes the action to takeby the listeners,
    *           assumed not <code>null</code> or empty.
    * @param isFullRefresh <code>true</code> to force refresh of all specified
    *           nodes, <code>false</code> to allow possible refresh of only
    *           dirty nodes.
    */
   private void informListeners(String hint, boolean isFullRefresh)
   {
      PSActionEvent event = new PSActionEvent(hint);
      event.setIsFullRefresh(isFullRefresh);

      Iterator listeners = m_actionListeners.iterator();
      while (listeners.hasNext())
      {
         IPSActionListener listener = (IPSActionListener) listeners.next();
         listener.actionExecuted(event);
      }
   }

   /**
    * Notifies the action listeners that the action is executed with the nodes
    * to refresh.
    *
    * @param nodesToRefresh the list of nodes to refresh, assumed not <code>null
    * </code> or empty.
    */
   private void informListeners(Iterator nodesToRefresh)
   {
      informListeners(nodesToRefresh, false);
   }

   /**
    * Notifies the action listeners that the action is executed with the nodes
    * to refresh.
    *
    * @param nodesToRefresh the list of nodes to refresh, assumed not <code>null
    * </code> or empty.
    * @param isFullRefresh <code>true</code> to force refresh of all specified
    *           nodes, <code>false</code> to allow possible refresh of only
    *           dirty nodes.
    */
   private void informListeners(Iterator nodesToRefresh, boolean isFullRefresh)
   {
      informListeners(nodesToRefresh, PSActionEvent.REFRESH_NODES, isFullRefresh);
   }

   /**
    * Notifies the action listeners that the specified action is executed with
    * the supplied nodes.
    *
    * @param nodesToRefresh the list of nodes to refresh, assumed not <code>null
    * </code> or empty.
    * @param hint the hint that describes the action to takeby the listeners,
    *           assumed not <code>null</code> or empty, and to be one of the
    *           <code>PSActionEvent</code> hint constants.
    */
   void informListeners(Iterator nodesToRefresh, String hint)
   {
      informListeners(nodesToRefresh, hint, false);
   }

   /**
    * Notifies the action listeners that the specified action is executed with
    * the supplied nodes.
    *
    * @param nodesToRefresh the list of nodes to refresh, assumed not <code>null
    * </code> or empty.
    * @param hint the hint that describes the action to takeby the listeners,
    *           assumed not <code>null</code> or empty, and to be one of the
    *           <code>PSActionEvent</code> hint constants.
    * @param isFullRefresh <code>true</code> to force refresh of all specified
    *           nodes, <code>false</code> to allow possible refresh of only
    *           dirty nodes.
    */
   private void informListeners(Iterator nodesToRefresh, String hint, boolean isFullRefresh)
   {
      if (!nodesToRefresh.hasNext())
         return;

      PSActionEvent event = new PSActionEvent(hint);
      event.setRefreshNodes(nodesToRefresh);
      event.setIsFullRefresh(isFullRefresh);

      Iterator listeners = m_actionListeners.iterator();
      while (listeners.hasNext())
      {
         IPSActionListener listener = (IPSActionListener) listeners.next();
         listener.actionExecuted(event);
      }
   }

   /**
    * Executes the supplied Url and return the HTTP response code. The execution
    * follows applet rules and uses GET method.
    *
    * @param strUrl must not be <code>null</code> or <code>empty</code>
    * @return HTTP Response code.
    * @throws PSContentExplorerException
    */
   public int executeUrl(String strUrl) throws PSContentExplorerException
   {
      if (strUrl == null || strUrl.trim().length() == 0)
         throw new IllegalArgumentException("strUrl must not be null or empty");

      if(strUrl.startsWith("../")){
         strUrl = strUrl.substring(3);
      }

      URL theUrl = null;
      HttpURLConnection connection = null;
      int responseCode = 0;
      try
      {
         theUrl = new URL(m_applet.getRhythmyxCodeBase(), strUrl);
         connection = (HttpURLConnection) theUrl.openConnection();
         // Make sure browser doesn't cache this URL.
         connection.setUseCaches(false);
         responseCode = connection.getResponseCode();
         if (responseCode >= 400)
         {
            InputStream is = connection.getErrorStream();
            if (is == null) // This should not happen
               is = connection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line;
            String linefeed = "\n";
            String msg = "";
            while ((line = in.readLine()) != null)
            {
               msg += line + linefeed;
            }
            m_applet.debugMessage(msg);
            throw new PSContentExplorerException(responseCode, msg);
         }
      }
      catch (MalformedURLException me)
      {
         throw new PSContentExplorerException(0, me.getMessage());
      }
      catch (IOException ioe)
      {
         throw new PSContentExplorerException(0, ioe.getMessage());
      }
      return responseCode;
   }

   /**
    * This performs a POST request to the destination provided by strUrl and
    * passing all the parameters encoded in the paramMap.
    *
    * @param strUrl a valid url path, must not be <code>null</code> or empty
    *
    * @param paramMap a map of key value pairs to be included in the post, must
    *           not be <code>null</code> or empty
    *
    * @return the string result of the post call, may be empty, or a result code
    *         number if the post response code > 400
    *
    * @throws MalformedURLException
    * @throws IOException
    * @throws ProtocolException
    */
   public String postData(String strUrl, Map paramMap) throws MalformedURLException, IOException, ProtocolException,
         PSException
   {
      if (strUrl == null || strUrl.trim().length() == 0)
         throw new IllegalArgumentException("strUrl must not be null or empty");

      if (paramMap == null)
         throw new IllegalArgumentException("paramMap must not be null");

      URL url = new URL(m_applet.getRhythmyxCodeBase(), strUrl);

      return m_applet.getHttpConnection().postData(url, paramMap);
   }

   /**
    * This submits a POST request (multi-part/formdata) to the Rhythmyx resource
    * provided by strUrl and supplied XML document string as input data
    * document. This is equivalent to submitting the XML string as a file
    * attachment.
    *
    * @param strUrl a valid url path, must not be <code>null</code> or empty.
    *
    * @param XmlString string (UTF-8) represntation of the XML document to post
    *           to the URL specified. Must not be <code>null</code> or empty.
    *
    * @return the string result of the post call, may be empty, or an error page
    *         if the post HTTP response code >= 400
    *
    * @throws MalformedURLException
    * @throws IOException
    * @throws ProtocolException
    * @throws IllegalArgumentException
    */
   public String postXmlData(String strUrl, String XmlString) throws MalformedURLException, IOException,
         ProtocolException, PSException
   {
      if (strUrl == null || strUrl.trim().length() == 0)
         throw new IllegalArgumentException("strUrl must not be null or empty");

      if (XmlString == null || XmlString.length() < 1)
         throw new IllegalArgumentException("XmlString must not be null or empty");

      URL url = new URL(m_applet.getRhythmyxCodeBase(), strUrl);

      return m_applet.getHttpConnection().postXmlData(url, XmlString);
   }

   /**
    * Access method for the applet
    *
    * @return applet associated with this, never <code>null</code>.
    */
   public PSContentExplorerApplet getApplet()
   {
      return m_applet;
   }

   /**
    * Helper method to convert from PSNode list object to PSLocator list object.
    */
   public static List nodesToLocators(Iterator nodeList)
   {
      if (nodeList == null)
         throw new IllegalArgumentException("nodeList must not be null");

      List<PSLocator> list = new ArrayList<PSLocator>();
      while (nodeList.hasNext())
      {
         PSNode node = (PSNode) nodeList.next();
         PSLocator locator = nodeToLocator(node);
         list.add(locator);
      }
      return list;
   }

   /**
    * Helper method to convert from PSNode object to PSLocator object for items
    * and folders
    *
    * @param node the node whose locator need to be extracted, may not be <code>
    * null</code> and must have a property defined for content id.
    *
    * @return the locator, never <code>null</code>
    */
   public static PSLocator nodeToLocator(PSNode node)
   {
      if (node == null)
         throw new IllegalArgumentException("node must not be null");

      int contentid = -1;
      int revision = -1;
      try
      {
         contentid = Integer.parseInt(node.getContentId());
      }
      catch (NumberFormatException e)
      {
         throw new IllegalArgumentException("node does not represent a component that can be located using an id");
      }

      try
      {
         revision = Integer.parseInt(node.getRevision());
      }
      catch (NumberFormatException e)
      {
         // revision is optional, can be ignored
      }

      return new PSLocator(contentid, revision);
   }

   /**
    * Easy access method for
    * {@link PSContentExplorerApplet#getXMLDocument(String)}. See the link for
    * more information.
    */
   public Document getXMLDocument(String url) throws IOException, SAXException, ParserConfigurationException
   {
      return m_applet.getXMLDocument(url);
   }

   /**
    * Easy access method for
    * {@link PSContentExplorerApplet#getXMLDocument(String, Map)}. See the link
    * for more information.
    */
   private Document getXMLDocument(String url, Map<String, Object> params) throws IOException, SAXException,
         ParserConfigurationException
   {
      return m_applet.getXMLDocument(url, params);
   }

   /**
    * Easy access method for
    * {@link PSContentExplorerApplet#getIcon(String)}. See the
    * link for more information.
    */
   public Icon getIcon(String url) throws MalformedURLException
   {
      return m_applet.getIcon(url);
   }

   /**
    * A node that can be moved or copied or whose display format can be copied
    * returns <code>true</code>, otherwise <code>false</code>. The nodes of type
    * <code>TYPE_SYS_CATEGORY</code>, <code>TYPE_SYS_SITES</code>, <code>
    * TYPE_SYS_FOLDERS</code>, <code>TYPE_SYS_VIEW</code>, <code>TYPE_CATEGORY
    * </code> and <code>TYPE_SLOT</code> can not be copied or moved.
    *
    * @return <code>true</code> if it can be copied or moved, otherwise <code>
    * false</code>
    */
   public boolean canCopyOrMove(PSNode target)
   {
      String type = target.getType();

      if (type.equals(PSNode.TYPE_SYS_CATEGORY) || type.equals(PSNode.TYPE_SYS_SITES)
            || type.equals(PSNode.TYPE_SYS_FOLDERS) || type.equals(PSNode.TYPE_SYS_VIEW)
            || type.equals(PSNode.TYPE_CATEGORY) || type.equals(PSNode.TYPE_SLOT) || type.equals(PSNode.TYPE_ROOT)
            || type.equals(PSNode.TYPE_PARENT))
      {
         return false;
      }
      return true;
   }

   /**
    * Checks whether the current view supports copy and paste actions. The views
    * <code>TYPE_VIEW_CX</code> and <code>TYPE_VIEW_IA</code> only supports copy
    * and paste so drag and drop also.
    *
    * @return <code>true</code> if it supports, otherwise <code>false</code>
    */
   public boolean viewSupportsCopyPaste()
   {
      if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_CX) || m_applet.getView().equals(PSUiMode.TYPE_VIEW_IA))
      {
         return true;
      }

      return false;
   }

   /**
    * This method finds the default action that can be executed for the node in
    * selection. This action is calculated based the following:
    * <ol>
    * <li>Take the first node from the selection</li>
    * <li>Get the array configured action names (internal) for the this node
    * from the node default action map defined in ms_NodeDefaultActionMap.</li>
    * <li>Get the possible action tree as context menu for the selection</li>
    * <li>Walk throw each action name in the array to locate in the action tree
    * recursively</li>
    * <li>Return the action if found or otherwise retun null</li>
    * </ol>
    *
    * @param selection the selection object that normally has one selected node.
    *           If it has more than one node only the first one is considered.
    * @return default action for the selected node, <code>null</code> if not
    *         found one.
    */
   public PSMenuAction findDefaultAction(PSSelection selection)
   {
      if (selection.getNodeListSize() < 1)
      {
         throw new IllegalArgumentException("selection must have at least one node");
      }
      PSNode selNode = (PSNode) selection.getNodeList().next();
      if (!PSContentExplorerConstants.ms_NodeDefaultActionMap.containsKey(selNode.getType()))
         return null; // No default actions are definedfor this node type

      PSMenuAction actionTree = getContextMenu(selection);
      String[] actionArray = PSContentExplorerConstants.ms_NodeDefaultActionMap.get(selNode.getType());
      if (actionTree == null || actionArray == null)
         return null;

      PSMenuAction action = null;
      for (String element : actionArray)
      {
         action = findChildActionByName(actionTree, element);
         if (action != null)
            break;
      }
      return action;
   }

   /**
    * Helper method to recurse and walk throw whole action menu tree to find the
    * action sepecified by the name string.
    *
    * @param actionParent action menu tree, assumed not <code>null</code>.
    * @param actionName internal name of the action to locate in the menu tree,
    *           assumed not <code>null</code>.
    */
   private PSMenuAction findChildActionByName(PSMenuAction actionParent, String actionName)
   {
      Iterator children = actionParent.getChildren();
      PSMenuAction action = null;
      while (children.hasNext())
      {
         action = (PSMenuAction) children.next();
         if (action.getName().equals(actionName))
            return action;
         else
         {
            // recurse children
            action = findChildActionByName(action, actionName);
            if (action != null)
               return action;
         }
      }
      return null;
   }

   /**
    * Compute if the specified selection is removable. If selection has any
    * folders, then the parent folder permissions are checked for a minimum
    * required access to allow removal of child folders.
    *
    * @param selection assumed not <code>null</code>
    * @return <code>true</code> if the selection is removable,
    *         <code>false</code> otherwise.
    */
   private boolean isRemovable(PSSelection selection)
   {
      // currently items (may include folders) from only one folder, Sites and
      // Folders can be removed
      PSNode parentNode = selection.getParent();
      if (parentNode == null)
         return false;

      String parentType = parentNode.getType();

      if (!isAnyFolderType(parentType))
         return false;

      return isFolderRemoveAllowed(selection);
   }

   /**
    * Compute if the specified selection is purgeable.
    *
    * @param selection assumed not <code>null</code>
    * @return <code>true</code> if the selection is purgeable,
    *         <code>false</code> otherwise.
    */
   private boolean isPurgeable(PSSelection selection)
   {
      // cuurently items(including folders) from only folders can be removed
      return selection.isOfType(PSNode.TYPE_ITEM);
   }

   /**
    * Checks to see whether a node can accept one or more Paste actioons. All
    * system nodes except <code>TYPE_SYS_FOLDERS</code> will not accept 'Paste'
    * action. Any other node other than system nodes will accept 'Paste' action.
    *
    * @param view the view where the paste is going to be happening, armed with
    *           this specification, we decide if we can paste
    *
    * @target the target node where the paste is to be applied, may be <code>
    * null</code>
    *
    * @clipType the type of paste we are trying to accomplish, one of <code>
    * PSClipBoard.TYPE_DRAG</code> or <code>PSClipBoard.TYPE_COPY</code>
    *
    * @return <code>true</code> if it can accept, otherwise <code>false</code>
    */
   public boolean canAcceptPaste(String view, PSNode target, int clipType)
   {
      String tgtType = target.getType();

      // these node types absolutely do not accept any paste actions.
      if (tgtType.equals(PSNode.TYPE_SYS_CATEGORY) || tgtType.equals(PSNode.TYPE_CATEGORY)
            || tgtType.equals(PSNode.TYPE_SYS_VIEW) || tgtType.equals(PSNode.TYPE_ROOT)
            || tgtType.equals(PSNode.TYPE_ITEM) || tgtType.equals(PSNode.TYPE_PARENT))
      {
         return false;
      }

      PSSelection selection = m_clipBoard.getClipSelection(clipType);
      if (selection == null)
         return false;
      // if we are in the Content Explorer view the following rules apply
      if (view.equals(PSUiMode.TYPE_VIEW_CX))
      {
         if (tgtType.equals(PSNode.TYPE_VIEW) || tgtType.equals(PSNode.TYPE_NEW_SRCH)
               || tgtType.equals(PSNode.TYPE_CUSTOM_SRCH) || tgtType.equals(PSNode.TYPE_STANDARD_SRCH)
               || tgtType.equals(PSNode.TYPE_SAVE_SRCH))
         {
            if (selection.isMultiSelect())
               return false;

            List selTypes = selection.getTypes();
            selTypes.remove(PSNode.TYPE_FOLDER);
            selTypes.remove(PSNode.TYPE_SITE);
            selTypes.remove(PSNode.TYPE_SITESUBFOLDER);
            selTypes.remove(PSNode.TYPE_VIEW);
            selTypes.remove(PSNode.TYPE_NEW_SRCH);
            selTypes.remove(PSNode.TYPE_SAVE_SRCH);
            selTypes.remove(PSNode.TYPE_CUSTOM_SRCH);
            selTypes.remove(PSNode.TYPE_STANDARD_SRCH);
            // selection contains types other than the above
            if (selTypes.size() > 0)
               return false;
         }
         else if (isFolderType(tgtType))
         {
            List selTypes = selection.getTypes();
            selTypes.remove(PSNode.TYPE_FOLDER);
            selTypes.remove(PSNode.TYPE_SITE);
            selTypes.remove(PSNode.TYPE_SITESUBFOLDER);
            selTypes.remove(PSNode.TYPE_ITEM);
            // selection contains types other than the above
            if (selTypes.size() > 0)
               return false;

            // do not allow creating circular folder references
            if (contains(selection.getNodeList(), target))
               return false;
         }
      }
      // if we are in the Item Assembly view the following rules apply
      else if (view.equals(PSUiMode.TYPE_VIEW_IA))
      {
         Iterator srcIter = selection.getNodeList();
         while (srcIter.hasNext())
         {
            PSNode source = (PSNode) srcIter.next();

            // if we are ourselves
            if (target.equals(source))
               return false;

            Iterator childIter = source.getChildren();
            if (childIter != null)
            {
               while (childIter.hasNext())
               {
                  PSNode childNode = (PSNode) childIter.next();

                  // if we are dropping on a child of ourselves
                  if (target.equals(childNode))
                     return false;
               }
            }
            try
            {
               // check target for allowed content
               if (target.isOfType(PSNode.TYPE_SLOT))
               {
                  if (!checkSlotAllowedContent(source, target))
                     return false;
               }
            }
            catch (Exception ex)
            {
               /** @todo handle exception */
            }
         }
      }
      else
      // Any other view does not allow copy and paste
      {
         return false;
      }
      return true;
   }

   /**
    * Test if the supplied list of sources contains the provided node
    * recursivly.
    *
    * @param sources an iterator of <code>PSNode</code> objects over all nodes
    *           to test, may be <code>null</code> or empty.
    * @param node the node to test if it is contained in the supplied sources
    *           recursivly, assumed not <code>null</code>.
    * @return <code>true</code> if the supplied sources contain the provided
    *         node, <code>false</code> otherwise.
    */
   private boolean contains(Iterator sources, PSNode node)
   {
      while (sources != null && sources.hasNext())
      {
         PSNode source = (PSNode) sources.next();
         if (source.equals(node))
            return true;

         if (contains(source.getChildren(), node))
            return true;
      }

      return false;
   }

   /**
    * Test if the supplied type is of type system folder. Currently defined
    * system folder types are:
    * <ul>
    * <li>PSNode.TYPE_SYS_FOLDERS</li>
    * <li>PSNode.TYPE_SYS_SITES</li>
    * </ul>
    *
    * @param type the type to check, not <code>null</code>.
    * @return <code>true</code> if the supplied type was of type system folder,
    *         <code>false</code> otherwise.
    */
   public boolean isSystemFolderType(String type)
   {
      if (type == null)
         throw new IllegalArgumentException("type cannot be null");

      return type.equals(PSNode.TYPE_SYS_FOLDERS) || type.equals(PSNode.TYPE_SYS_SITES);
   }

   /**
    * Test if the supplied type is of type folder. Currently defined folder
    * types are:
    * <ul>
    * <li>PSNode.TYPE_FOLDER</li>
    * <li>PSNode.TYPE_SITE</li>
    * <li>PSNode.TYPE_SITESUBFOLDER</li>
    * </ul>
    *
    * @param type the type to check, not <code>null</code>.
    * @return <code>true</code> if the supplied type was of type folder,
    *         <code>false</code> otherwise.
    */
   public static boolean isFolderType(String type)
   {
      if (type == null)
         throw new IllegalArgumentException("type cannot be null");

      return type.equals(PSNode.TYPE_FOLDER) || type.equals(PSNode.TYPE_SITE) || type.equals(PSNode.TYPE_SITESUBFOLDER);
   }

   /**
    * Test if the supplied type is of any folder type. This is the ORed result
    * of {@link #isSystemFolderType(String)} and {@link #isFolderType(String)}.
    *
    * @param type the type to check, not <code>null</code>.
    * @return <code>true</code> if the supplied type was of any folder type,
    *         <code>false</code> otherwise.
    */
   public boolean isAnyFolderType(String type)
   {
      if (type == null)
         throw new IllegalArgumentException("type cannot be null");

      return isSystemFolderType(type) || isFolderType(type);
   }

   /**
    * Prepares a filter <code>Map</code> which will be applied on search field
    * entries.
    *
    * @param searchNode for which filter map is needed.
    *
    * @return Map The key in the map is search field name and corresponding
    *         value is <code>PSSearchFieldFilter</code> object. Returns
    *         <code>null</code> if no filter needs to be applied.
    * @throws PSContentExplorerException
    */
   @SuppressWarnings("unchecked")
   private Map prepareSearchFilterMap(PSNode searchNode) throws PSContentExplorerException

   {
      Map fm = new HashMap();
      if (searchNode == null)
      {
         throw new IllegalArgumentException("searchNode must not be null");
      }
      try
      {
         // Slot Content Types Filter
         if (searchNode.getSlotId().trim().length() > 0)
         {
            PSSearchFieldFilterMap filterMap = new PSSearchFieldFilterMap(searchNode.getSlotId())
            {
               @Override
               protected Document getDocumentFromServer(String url) throws IOException
               {
                  if (url == null || url.trim().length() == 0)
                     throw new IllegalArgumentException("url may not be null or empty");

                  try
                  {
                     return PSActionManager.this.getXMLDocument("../" + url);
                  }
                  catch (Exception e)
                  {
                     throw new IOException(e.getLocalizedMessage());
                  }
               }

            };

            fm.putAll(filterMap.getFilterMap());
         }
      }
      catch (Exception ex)
      {
         throw new PSContentExplorerException(IPSContentExplorerErrors.GENERAL_ERROR, ex.getLocalizedMessage());
      }
      return fm;
   }

   /**
    * Checks the source node against the target slot to see if the content type
    * and variant are allowed within the slot.
    *
    * @param source the source node to check against, may not be
    *           <code>null</code>
    *
    * @param target the slot to check against, may not be <code>null</code>
    *
    * @return true if the source node is allowed within the target slot
    */
   private boolean checkSlotAllowedContent(PSNode source, PSNode target) throws PSContentExplorerException
   {
      loadSlotDefinitions();

      String slotId = target.getSlotId();

      List slotDefList = m_slotDefMap.get(slotId);

      if (slotDefList == null)
         return false;

      ContentIdVariantId data = new ContentIdVariantId(source.getContentTypeId(), source.getVariantId());

      return slotDefList.contains(data);
   }

   /**
    * Inner data class used to hold the content type id and variant id structure
    * of a specified node. This will be used in the slot def to check for
    * allowances of content types and variants.
    */
   private class ContentIdVariantId
   {
      ContentIdVariantId(String contentId, String variantId)
      {
         m_contentId = contentId;
         m_variantId = variantId;
      }

       @Override
       public boolean equals(Object o) {
           if (this == o) return true;
           if (!(o instanceof ContentIdVariantId)) return false;
           ContentIdVariantId that = (ContentIdVariantId) o;
           return Objects.equals(m_contentId, that.m_contentId) &&
                   Objects.equals(m_variantId, that.m_variantId);
       }

       @Override
       public int hashCode() {
           return Objects.hash(m_contentId, m_variantId);
       }

       private String m_contentId;

      private String m_variantId;
   }

   /**
    * Helper function to hide or show the New Search node based whther it was
    * initialized or not.
    */
   @SuppressWarnings("unchecked")
   private void hideOrShowNewSearchNode()
   {
      if (m_searchResultsNode == null)
         return; // Should never happen

      Iterator children = m_searchResultsNode.getChildren();
      if (!children.hasNext())
         return;

      List newChildren = new ArrayList();
      // Only in case of CX there will be a Empty Search Node first.
      if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_CX))
      {
         // Add the empty search node first
         newChildren.add(children.next());
      }
      PSNode newSearchNode = (PSNode) children.next();
      // may be first time, initialize
      if (m_newSearchNode == null)
         m_newSearchNode = newSearchNode;
      if (!m_newSearchNode.isHidden())
      {
         // already initialized and hence replace the dynamically loaded one
         // with
         // that was already initialized.
         newChildren.add(m_newSearchNode);
      }
      else
      {
         /*
          * Not initialized so do not add (or hide) to the search results yet.
          */
      }
      while (children.hasNext())
      {
         newChildren.add(children.next());
      }
      m_searchResultsNode.setChildren(newChildren.iterator());
   }

   /**
    * Method to insert new search node to search results node as first child and
    * sets the flag to unhide it. Also, informs the listeners that search
    * results node must be refreshed.
    */
   public void addNewSearchNode()
   {
      if (m_newSearchNode == null || m_searchResultsNode == null)
         return; // Should never happen
      // Set it unhidden
      m_newSearchNode.setHidden(false);
      List<PSNode> newChildren = new ArrayList<PSNode>();
      Iterator children = m_searchResultsNode.getChildren();
      // Add new search node as the first node
      newChildren.add(m_newSearchNode);
      // Add the rest now
      while (children.hasNext())
      {
         newChildren.add((PSNode) children.next());
      }
      m_searchResultsNode.setChildren(newChildren.iterator());
      informListeners(PSIteratorUtils.iterator(m_searchResultsNode));
   }

   /**
    * @return Current content selection path, that is supplied by the MainView;
    *         never <code>null</code>, may be <code>empty</code> before the
    *         first tree selection is made.
    */
   public String getNavSelectionPath()
   {
      return m_navSelectionPath;
   }

   /**
    * Copies the security (ACL) of the specified folder to all subfolders. This
    * includes all descendants as well as all descendants of descendants and so
    * on. Also handles user feedback in the form of a progress bar during the
    * operation.
    *
    * @param folderNode represents the folder.
    *
    * @throws PSCmsException if an error occurs retrieving subfolders.
    */
   private void propagateFolderSecurity(final PSNode folderNode) throws PSCmsException
   {
      final PSLocator folderLoc = nodeToLocator(folderNode);
      final PSLocator[] folderChildren = m_folderMgr.getDescendantFoldersWithoutFilter(folderLoc);
      final PSProcessMonitor monitor = new PSProcessMonitor(folderChildren.length, m_applet);

      informListeners(monitor);

      Thread workerThread = new Thread()
      {
         @Override
         public void run()
         {
            try
            {
               monitor.setStatus(PSProcessMonitor.STATUS_RUN);

               int i = 0;
               for (PSLocator childLoc : folderChildren)
               {
                  while (monitor.getStatus() == PSProcessMonitor.STATUS_PAUSE)
                     sleep(10);

                  if (monitor.getStatus() == PSProcessMonitor.STATUS_STOP)
                     break;

                  monitor.updateStatus(++i, folderNode);

                  try
                  {
                     m_folderMgr.copyFolderSecurity(folderLoc, childLoc);
                  }
                  catch (PSCmsException e)
                  {
                     m_applet.debugMessage(e);

                     Object[] args =
                     {e.getLocalizedMessage()};
                     String error = MessageFormat.format(
                           m_applet.getResourceString(getClass(), "FolderSecurityPropagationError")
                                 + "  The error returned was: {0}.", args);

                     PSLineBreaker.wrapString(error, 78, 77, "\n");
                     monitor.showError(null, error);
                  }
               }

               if (monitor.getStatus() != PSProcessMonitor.STATUS_STOP)
                  monitor.setStatus(PSProcessMonitor.STATUS_COMPLETE);
            }
            catch (InterruptedException e)
            {
               m_applet.debugMessage("Interrupted " + e.getLocalizedMessage());
               monitor.setStatus(PSProcessMonitor.STATUS_COMPLETE);
               Thread.currentThread().interrupt();
            }
         }
      };

      workerThread.start();
   }

   /**
    * Convienience class to represent the parts of a javascript new window call.
    */
   private class WindowScript
   {

      /**
       * Constructor to create a new WindowScript object
       *
       * @param actionurl the action URL that the new window will point to,
       *           cannot be <code>null</code>.
       *
       * @param target the window target, May be <code>null</code>.
       *
       * @param style the style string which specifies things like height,
       *           width, toolbar on, status bar on...., may be
       *           <code>null</code>
       */
      private WindowScript(String actionurl, String target, String style, PSMenuAction action, PSSelection selection)
      {

         if (actionurl == null)
            throw new IllegalArgumentException("actionUrl must not be null");
         if (target == null)
            target = PSContentExplorerConstants.ACTION_TARGET_DEFAULT;
         if (style == null)
            style = PSContentExplorerConstants.ACTION_TARGET_STYLE_DEFAULT;

         this.action = action;
         mi_selection = selection;
         mi_actionurl = actionurl;
         mi_target = target;
         mi_style = style;
      }

      /**
       * Invokes a new bowser window that points to the specified action URL.
       *
       * @throws MalformedURLException if error occurs while trying to create a
       *            new URL
       */
      void show() throws MalformedURLException
      {
         Object[] args = new Object[]

               {mi_actionurl, mi_target, mi_style};

         if (PSAjaxSwingWrapperLocator.getInstance().isAjaxSwingEnabled())
         {
            PSAjaxSwingWrapperLocator.getInstance().openWindow(m_applet.getHttpConnection(), mi_actionurl, mi_target,
                  mi_style);

         }
         else if (dceWindow != null)
         {
            if(mi_target.equals("_parent")){
               Map<String, String> queryParams = null;

               queryParams = PSContentExplorerUtils.getQueryMap(mi_actionurl);
               // trap request to old parent explorer url
               
               HashMap<String, String> newParams = new HashMap<String,String>();
               String contentid = queryParams.get("sys_contentid");
               String revision = queryParams.get("sys_revision");
               
               if (contentid != null)
                  newParams.put(PSContentExplorerConstants.PARAM_CONTENTID, queryParams.get("sys_contentid"));
               if (revision != null)
                  newParams.put(PSContentExplorerConstants.PARAM_REVISIONID, queryParams.get("sys_revision"));
               
          
               dceWindow.reloadParent(newParams);

               return;
        
             }

            else if(mi_actionurl.toLowerCase().endsWith("xls")){
               //save dialog for the xsl file
               PSFileSaver fileSaver = new PSFileSaver(mi_actionurl);
               fileSaver.startFileSaver();

            }
            else if(  isRender(mi_actionurl)){
          //whatever has to be rendered, show in external browser
               //this.isExternalURL(mi_actionurl)
               URL url = PSBrowserUtils.toURL(mi_actionurl);

               PSBrowserUtils.openWebpage(url);
            }
            else
            {
               Platform.runLater(() -> {
                  startSwingBrowser();
               });
            }
         }
         else

         {
            // Use LiveConnect to call showWindow javascript
            // method
            JSObject window = JSObject.getWindow(m_applet);
            window.call("showWindow", args);
         }

      }
      private boolean isRender(String url){

       return (url.toLowerCase().contains("/render") && !url.toLowerCase().contains("sys_action")) || url.toLowerCase().contains("sys_compare/compare.html") ;
      }

      private void startSwingBrowser()
      {
         Platform.runLater(() -> {
            dceWindow.openChildWindow(mi_actionurl, mi_target,
            mi_style,mi_selection,action);
         });
      }


      @Override
      public String toString()
      {
         return "javascript:showWindow(\"" + mi_actionurl + "\",\"" + mi_target + "\",\"" + mi_style + "\")";
      }

      private String mi_actionurl = "";

      private String mi_target = "";

      private String mi_style = "";

      private PSMenuAction action;

      private PSSelection mi_selection;
   }

   /**
    * Set whenever MainView fires selection change notification, never
    * <code>null</code>, may be <code>empty</code> before the first tree
    * selection is made.
    */
   private String m_navSelectionPath = "";

   /**
    * Map of all the actions excluding the dynamic menus, initialized in the
    * ctor, never <code>null</code>, may be empty. It uses an String as the key
    * defined as the url with the mode and context and returns a <code>
    * PSAction</code> as the value for each key.
    */
   private final Map<String, PSMenuAction> m_actionMap = new HashMap<String, PSMenuAction>();

   /**
    * A reference back to the applet that initiated this action manager.
    */
   protected PSContentExplorerApplet m_applet;

   /**
    * The clip board that supports drag and copy clips, initialized in the ctor
    * and never <code>null</code> after that. The content of the clip may be
    * modified using this reference object.
    */
   private PSClipBoard m_clipBoard;

   /**
    * The slot definitions are loaded into this map on demand, this will be
    * <code>null</code> until needed. Never modified after loaded.
    */
   private Map<String, List<ContentIdVariantId>> m_slotDefMap;

   /**
    * The list of action listeners that gets informed when an action is
    * executed, initialized to an empty list and gets updated through calls to
    * <code>
    * addActionListener(IPSActionListener)</code>. Never <code>null</code>.
    */
   private final List<IPSActionListener> m_actionListeners = new ArrayList<IPSActionListener>();

   /**
    * Contstant for the name of the resource to execute the server action url.
    * This resource picks the server action url from the session variable
    * SYS_SERVERACTIONURL and redirects to that url.
    */
   private static final String EXECUTE_SERVERACTIONURL = "../sys_cxSupport/executeserveractionurl.html";

   /*
    * Catalog of display formats. Initilized when the first time this class is
    * loaded. Never <code>null</code> after that.
    */
   private PSDisplayFormatCatalog ms_dfCatalog = null;

   /**
    * Catalog of server communities. Lazily initilized by the
    * {@link #getCommunityCataloger() } when it is invoked for the first time.
    * Never <code>null</code> after that.
    */
   private PSCommunityCataloger m_communityCataloger = null;

   /**
    * Catalog of registered locales in the CMS. Lazily initilized by the
    * {@link #getLocaleCataloger() } when it is invoked for the first time. Never
    * <code>null</code> after that.
    */
   private PSLocaleCataloger m_localeCataloger = null;

   /**
    * Catalog of server roles. Lazily initilized by the
    * {@link #getRoleCataloger() } when it is invoked for the first time, never
    * <code>null</code> after that.
    */
   private PSRoleCataloger m_roleCataloger = null;

   /**
    * Catalog of server subjects / users. Lazily initilized by the
    * {@link #getSubjectCataloger() } when it is invoked for the first time,
    * never <code>null</code> after that.
    */
   private PSSubjectCataloger m_subjectCataloger = null;

   /**
    * Catalog of security providers. Lazily initilized by the
    * {@link #getSecurityProviderCataloger() } when it is invoked for the first
    * time, never <code>null</code> after that.
    */
   private PSSecurityProviderCataloger m_securityProviderCataloger = null;

   /**
    * Catalogs global templates. Lazily initilized by the
    * {@link #getGlobalTemplateCataloger() } when it is invoked for the first
    * time. Never <code>null</code> after that.
    */
   private PSGlobalTemplateCataloger ms_globalTemplateCataloger = null;

   /**
    * Catalogs site names. Lazily initilized by the {@link #getSiteCataloger() }
    * when it is invoked for the first time. Never <code>null</code> after that.
    * This member is static because multiple instances of this class may exist
    * at the same time and changes to one should affect the other.
    */
   private PSSiteCataloger ms_siteCataloger = null;

   /**
    * Map of visibility contexts for each defined menu. Initialized in the ctor,
    * never <code>null</code> after that.
    */
   private Map<String, PSActionVisibilityContexts> m_visibilityContextsMap;

   /**
    * The remote proxy that is used to make remote requests to the server from
    * this applet to manage CMS components, initialized in the ctor and never
    * <code>null</code> or modified after that.
    */
   private PSComponentProcessorProxy m_componentProxy;

   /**
    * The remote proxy that is used to make remote requests to the server from
    * this applet, initialized in the ctor and never <code>null</code> or
    * modified after that.
    */
   private PSRelationshipProcessorProxy m_relationshipProxy;

   /**
    * The remote cataloger to use thorughout this applet instance, initialized
    * in the ctor and never <code>null</code> or modified after that.
    */
   private PSRemoteCataloger m_remCataloger;

   /**
    * The manager that handles the item relationships, initialized in the ctor
    * and never <code>null</code> or modified after that.
    */
   private PSItemRelationshipsManager m_irsManager = null;

   /**
    * The manager that handles the actions related folders, initialized in the
    * ctor and never <code>null</code> or modified after that.
    */
   private PSFolderActionManager m_folderMgr = null;

   /**
    * The manager that handles the actions related to searches and views,
    * initialized in the ctor and never <code>null</code> or modified after
    * that.
    */
   private PSSearchViewActionManager m_searchViewMgr = null;

   /**
    * Reference to the New Search node. Initialized when the Searh Results loads
    * its child nodes for the first time. Used to hide until user performs first
    * time search. Initialized when action manager loads the children of Search
    * Results node for the first time, never <code>null</code> after that.
    */
   private PSNode m_newSearchNode = null;

   /**
    * Reference to the node that is used to activate a new search request.
    * Initialized when {@link #loadChildren(PSNode)} is called. May be
    * <code>null</code>, but in general operation shouldn't be.
    */
   private PSNode m_emptySearchNode = null;

   /**
    * Reference to the Search Results node. Initialized when Root node (hidden)
    * of the Content Explorer navigation pane loads its child nodes for the
    * first time. Used to manage new search child node easilty in that we hide
    * it hide until user performs first time search. Initialized when action
    * manager loads the children of Root node of the Content Explorer navigation
    * pane for the first time, never <code>null</code> after that.
    */
   private PSNode m_searchResultsNode = null;

   /**
    * List of column names used to load dynamic properties for search nodes.
    * Never <code>null</code>, no longer used, but left in case we need to
    * reinstitute dynamic menu properties, in which case it should be populated
    * by the static initializer.
    */
   private final List<String> ms_dynColumNames = new ArrayList<String>();

   /**
    * The community / content type mapper, used to lazily cache the mapper in
    * the current applet section. Init to <code>null</code>.
    */
   private PSCommunityContentTypeMapperCataloger m_commCtMapper = null;

   /**
    * Cache of dynamic child menu actions where the key is the url and the value
    * is the resulting action list, never <code>null</code>.
    */
   private Map<String, List<PSMenuAction>> m_childMenuCache = new HashMap<String, List<PSMenuAction>>();

   // Static initilizer
   static
   {
      PSContentExplorerConstants.ms_NodeDefaultActionMap.put(PSNode.TYPE_DTITEM, new String[]
      {ACTION_OPEN});
      PSContentExplorerConstants.ms_NodeDefaultActionMap.put(PSNode.TYPE_ITEM, new String[]
      {ACTION_EDIT, ACTION_VIEW_CONTENT});
      PSContentExplorerConstants.ms_NodeDefaultActionMap.put(PSNode.TYPE_NEW_SRCH, new String[]
      {ACTION_EDIT_SEARCH});
      PSContentExplorerConstants.ms_NodeDefaultActionMap.put(PSNode.TYPE_SAVE_SRCH, new String[]
      {ACTION_EDIT_SEARCH});
      PSContentExplorerConstants.ms_NodeDefaultActionMap.put(PSNode.TYPE_PARENT, new String[]
      {ACTION_EDIT, ACTION_VIEW_CONTENT});
      PSContentExplorerConstants.ms_NodeDefaultActionMap.put(PSNode.TYPE_CUSTOM_SRCH, new String[]
      {ACTION_EDIT_SEARCH});
      PSContentExplorerConstants.ms_NodeDefaultActionMap.put(PSNode.TYPE_EMPTY_SRCH, new String[]
      {ACTION_EDIT_SEARCH});
      PSContentExplorerConstants.ms_NodeDefaultActionMap.put(PSNode.TYPE_STANDARD_SRCH, new String[]
      {ACTION_EDIT_SEARCH});
      PSContentExplorerConstants.ms_NodeDefaultActionMap.put(PSNode.TYPE_FOLDER_REF, new String[]
      {ACTION_OPEN_FOLDER_REF});
   }

   /**
    * Set of special parameter names to resolve in the server action URLs.
    * Currently we have only {@link IPSHtmlParameters#SYS_FOLDERID} and
    * {@link IPSHtmlParameters#SYS_SLOTID}. These special params are added to
    * the URL based on the following contexts: 1. Drop-Paste, Copy-Paste: Params
    * are resolved to target Nodes 2. Regular case: Params are resolved to the
    * selection node's parent.
    */
   public static Set<String> ms_specialParams = new HashSet<String>();
   static
   {
      ms_specialParams.add(IPSHtmlParameters.SYS_FOLDERID);
      ms_specialParams.add(IPSHtmlParameters.SYS_SLOTID);
   }

   /**
    * Some params we just want to add to the final dyanmic action and not pass
    * through to the request for templates. This will prevent re-caching results
    * if only these values have changed.
    */
   private static final Set<String> PASS_THRU_PARAMS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
         "targetStyle", "target", "launchesWindow")));
   

   public PSSearchConfig getSearchConfig()
   {
      return m_searchViewMgr.getSearchConfig();
   }


}
