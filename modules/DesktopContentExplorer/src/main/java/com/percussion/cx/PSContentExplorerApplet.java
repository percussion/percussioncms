/******************************************************************************
 *
 * [ PSContentExplorerApplet.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.cx;

import com.percussion.border.PSFocusBorder;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSRelationshipInfo;
import com.percussion.cms.objectstore.PSRelationshipInfoSet;
import com.percussion.cms.objectstore.PSUserInfo;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.cx.javafx.PSDesktopExplorerWindow;
import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.cx.objectstore.PSMenuBar;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.cx.objectstore.PSProperties;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSBeansException;
import com.percussion.error.PSException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;
import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.tools.help.PSJavaHelp;
import com.percussion.util.PSHttpConnection;
import com.percussion.util.PSRemoteAppletRequester;
import com.percussion.util.PSStopwatch;
import com.percussion.utils.collections.PSIteratorUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The main class for 'Content Explorer Applet'.
 */
public class PSContentExplorerApplet extends JApplet implements IPSActionListener
{

   private static final String RESOURCE_NAME = "com.percussion.cx.PSContentExplorerResources";

   private static final long serialVersionUID = 1L;

   public static ConcurrentMap<String, PSRelationshipInfo> REL_MAP = new ConcurrentHashMap<String, PSRelationshipInfo> ();
   static Logger log = Logger.getLogger(PSContentExplorerApplet.class);

   private PSHttpConnection m_httpConnection;

   private boolean m_initialized = false;

   private final String collapsed_icon = "images/collapsedIcon.gif";

   private final String expanded_icon = "images/expandedIcon.gif";

   private  boolean isApplication = false;

   private PSCESessionManager check = null;
   
   private PSContentExplorerHeader dceHeader = null;
   
   private WebEngine webEngine = null;
   private boolean m_createdFromFrame = false;

   /**
    * The Server Admin resources
    */
   private ResourceBundle m_res = null;

   public ResourceBundle getResources()
   {
      PSContentExplorerUtils.OutputJaxpImplementationInfo();

      if (m_res == null)
         m_res = ResourceBundle.getBundle(RESOURCE_NAME, Locale.getDefault());
      return m_res;
   }

   public PSContentExplorerApplet(){
      super();
   }

   public PSContentExplorerApplet (boolean createdFromFrame){
      super();
      m_createdFromFrame = createdFromFrame;
   }


   static String getJaxpImplementationInfo(String componentName, Class componentClass)
   {
      CodeSource source = componentClass.getProtectionDomain().getCodeSource();
      return MessageFormat.format("{0} implementation: {1} loaded from: {2}", componentName, componentClass.getName(),
            source == null ? "Java Runtime" : source.getLocation());
   }

   /**
    * Initializes the applet framework.
    */
   @Override
   public synchronized void init()
   {

      PSContentExplorerUtils.OutputJaxpImplementationInfo();

      PSSecureXMLUtils.setupJAXPDefaults();

      if(!isApplication)
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();

         URL inputUrl = loader.getResource("wce_log4j.properties");
         if (inputUrl != null)
            {
               PropertyConfigurator.configure(inputUrl);
               log.info("log4j configured");
            }
      }
      //As LoginContextWill be set by ContentExplorerFrame, once login details are gathered from user
      if(!m_createdFromFrame) {
         setupApplet(null);
      }
   }


   public synchronized void setUserInfo(){
      // test logged in
      URL rxCodeBase = getRhythmyxCodeBase();

      try
      {
         log.debug("checking userinfo");
         ms_userInfo = new PSUserInfo(getHttpConnection(), rxCodeBase);
         log.debug("UserInfo sessionId = " + ms_userInfo.getSessionId());
         log.debug("UserInfo user = " + ms_userInfo.getUserName());
         log.debug("UserInfo locale = " + ms_userInfo.getLocale());
         if( m_httpConnection != null){
            m_httpConnection = new PSHttpConnection(getRhythmyxCodeBase(), ms_userInfo.getSessionId());
         }
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         displayErrorMessage(getDialogParentFrame(), getClass(), "{0}", new String[]
                 {e.getMessage()}, "Error", null);
         log.error("Error getting userinfo", e);
      }

   }

   public synchronized void setupApplet(PSUserInfo userInfo)
   {
      if(userInfo == null){
         setUserInfo();
      }else{
         ms_userInfo = userInfo;
         m_httpConnection = new PSHttpConnection(getRhythmyxCodeBase(), ms_userInfo.getSessionId());

      }

      try
      {

         if (PSAjaxSwingWrapperLocator.getInstance().isAjaxSwingEnabled())
         {

            // Clear all cookies that may have been left over from previous
            CookieManager manager = new CookieManager();
            manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(manager);
         }
      }
      catch (Exception e)
      {
         log.error("Something is wrong with cookie manager. {}",e);
      }

      try
      {


         m_splash = new SplashScreen();
         m_splash.setMaxMessageCount(6);

         String debug = getParameter(PSContentExplorerConstants.PARAM_DEBUG);
         if (debug != null) // reset isDebug if not on
         {
            log.setLevel(Level.DEBUG);
            ms_isDebug = "TRUE".equalsIgnoreCase(debug) || "YES".equalsIgnoreCase(debug);
         }
         String showSplash = getParameter(PSContentExplorerConstants.PARAM_SHOW_SPLASH);
         if (showSplash != null)
            m_splash.show();

         // Resize to full frame



         m_cursor = new CxCursor(getParentFrame());
         m_cursor.start();

         ms_thisApplet = this;
         m_osName = System.getProperty("os.name");

         try {
            UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
         } catch (Exception e) {
                    e.printStackTrace();
         }

         if(isMacPlatform()) {
            InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), DefaultEditorKit.copyAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), DefaultEditorKit.pasteAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), DefaultEditorKit.cutAction);

         }

         UIManager.put("Tree.collapsedIcon", PSImageIconLoader.loadIcon(collapsed_icon, false, ms_thisApplet));
         UIManager.put("Tree.expandedIcon", PSImageIconLoader.loadIcon(expanded_icon, false, ms_thisApplet));


         String rsflag = getParameter(PSContentExplorerConstants.PARAM_RESTRICTSEARCHFIELDSTOUSERCOMMUNITY);
         if (rsflag != null && rsflag.equalsIgnoreCase("yes"))
            ms_restrictContent = true;

         // set the m_searchableFields according to "CacheSearchableFields"
         // property, defined in server.properties
         String cacheSearchFields = getParameter(PSContentExplorerConstants.PARAM_CACHE_SEARCHABLE_FIELDS);
         log.debug("PARAM: " + PSContentExplorerConstants.PARAM_CACHE_SEARCHABLE_FIELDS + " = " + cacheSearchFields);

         // default to cache the searchable fields per JVM
         m_searchableFields = ms_searchableFieldsPerJVM;
         if (cacheSearchFields != null && cacheSearchFields.equalsIgnoreCase("CachePerApplet"))
            m_searchableFields = new HashMap();
         else if (cacheSearchFields != null && cacheSearchFields.equalsIgnoreCase("None"))
            m_searchableFields = null;

         // set the "ms_isManagedNavUsed" from the applet parameter
         String isManagedNavUsed = getParameter(PSContentExplorerConstants.PARAM_IS_MANAGEDNAV_USED);
         ms_isManagedNavUsed = "YES".equalsIgnoreCase(isManagedNavUsed);
         log.debug("PARAM: " + PSContentExplorerConstants.PARAM_IS_MANAGEDNAV_USED + " = " + isManagedNavUsed);
         log.debug("       ms_isManagedNavUsed = " + ms_isManagedNavUsed);

         // Initialize view first because the applet behavior is defined by
         // the view.
         String view = getParameter(PSContentExplorerConstants.PARAM_VIEW);
         if (view == null || !PSUiMode.isValidView(view))
         {
            debugMessage("The parameter " + PSContentExplorerConstants.PARAM_VIEW + "is either not supplied or has a invalid value: " + view);
            debugMessage("Assuming default view: " + PSUiMode.TYPE_VIEW_CX);
            m_view = PSUiMode.TYPE_VIEW_CX;
         }
         else
            m_view = view;

         debugMessage("Current view is " + m_view);

         try
         {
            m_splash.setMessage("Retrieving user options");
            m_optionsManager = new PSOptionManager(this);
            m_optionsManager.load();
         }
         catch (PSContentExplorerException e)
         {
            // ignore this error for now, since we will use defaults
            // if the options manager could not be loaded
            debugMessage("Error loading options: " + e.getLocalizedMessage());
         }
         m_splash.setMessage("Retrieving actions");

         m_actManager = new PSActionManager(this);

         m_splash.setMessage("Getting i18n values");
         if (ms_i18nKeyValue == null)
         {
            try {
               ms_i18nKeyValue = PSI18NTranslationKeyValues.getInstance();
               List<String> packages = new ArrayList<String>();
               packages.add("com.percussion.wizard");
               packages.add("com.percussion.search.ui");
               packages.add("com.percussion.guitools");
               packages.add("com.percussion.cx");
               ms_i18nKeyValue.setPackages(packages);
               ms_i18nKeyValue.load(new PSRemoteAppletRequester(getHttpConnection(), getRhythmyxCodeBase()));
               debugMessage(PSXmlDocumentBuilder.toString(ms_userInfo.toXml(PSXmlDocumentBuilder.createXmlDocument())));
            }catch (PSBeansException ex){
               ex.printStackTrace();
            }
         }

         ms_optChangeUpdater = new OptionsUpdater();

         /*
          * Keeps the singleton instance of help class to load the map or
          * helpset files once only.
          */
         if (ms_help == null)
         {
            m_splash.setMessage("Retrieving user help");
            String helpFile = getParameter(PSContentExplorerConstants.PARAM_HELPSET);
            log.debug("Help url pre" + helpFile);
            String helpSetURL = PSJavaHelp.getHelpSetURL(helpFile, true, getRhythmyxCodeBase().toString());
            log.debug("Help url post " + helpSetURL);
            if (helpSetURL != null && helpSetURL.trim().length() != 0)
            {
               ms_help = PSJavaHelp.getInstance();
               ms_help.setHelpSet(helpSetURL, "com.percussion.cx.helptopicmapping");
            }
            else
            {
               debugMessage("HelpSet URL is not available. So help is not activated.");
            }
         }
         if (ms_help != null)
            ms_help.clearBroker();
         m_splash.setMessage("Initializing the view");

         loadFlaggedFoldersSet();

         UIManager.getDefaults().put(PSContentExplorerConstants.DISPLAY_OPTIONS, null);
         initView();

         m_splash.setMessage("Restoring applet state information");
         restoreAppletState();

         m_parentFrame = getParentFrame();

         PSAjaxSwingWrapperLocator.getInstance().refreshWindow(getHttpConnection());

         m_initialized = true;
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         if (ms_isDebug)
            ex.printStackTrace();
         System.exit(ERROR);
         if (ex instanceof RuntimeException)
         {
            throw (RuntimeException) ex;

         }
         else
         {

            throw new RuntimeException("Exception Initializing applet, " + ex.getLocalizedMessage(), ex);
         }
      }
   }

   /**
    * Invokes a new thread to set/execute default selection/action after the
    * applet is showing. Does the following.
    * <ol>
    * <li>In CX View, selects new search node if 'CONTENTID' parameter is
    * available, otherwise as defined by the 'sys_cxinternalpath',
    * 'sys_cxdisplaypath', <code>PARAM_PATH_DEFAULT</code>. Checks in the above
    * mentioned order and selects the first one that is provided.</li>
    * <li>In RC View, executes search action that displays search dialog to
    * start with</li>
    */
   @Override
   public void start()
   {
      if (m_initialized)
      {
         if (m_cursor == null)
         {
            m_cursor = new CxCursor(getParentFrame());
            m_cursor.start();
         }

         SwingUtilities.invokeLater(() -> {
            registerKeyEventPostProcessor();

            // Initiate 'Search Action' by default if the view is 'RC'
            if (m_view.equals(PSUiMode.TYPE_VIEW_RC))
            {
               PSMenuAction action = new PSMenuAction(IPSConstants.ACTION_SEARCH, IPSConstants.ACTION_SEARCH);
               m_actManager.executeAction(action, null);
            }
            else if (m_view.equals(PSUiMode.TYPE_VIEW_CX))
            {
               String navPath = null;
               boolean byName = true;
               String contentId = getParameter(PSContentExplorerConstants.PARAM_CONTENTID);
               /**
                * If the content id present, set the 'New Search path' as
                * default and use content id as the search criteria.
                */
               if (contentId != null && contentId.trim().length() > 0)
               {
                  debugMessage("Setting SEARCH as default path to select " + "with content id as search criteria");

                  // Initialize search criteria on a new search node using
                  // the content id and then select that path to execute the
                  // search criteria and load its children.
                  PSNode defaultSearch = m_actManager.getNewSearchNode();
                  PSNode searchResults = m_actManager.getSearchResultsNode();
                  // Node must be added to the tree
                  PSMainView view = (PSMainView) m_mainView;
                  PSNavigationTree tree = view.getNavTree();
                  m_actManager.initContentIdSearch(defaultSearch, contentId);
                  navPath = tree.getPath(searchResults);
               }
               else
               {
                  // Internal path selection always overrides the display
                  // path
                  String internalNavPath = getParameter(PSContentExplorerConstants.PARAM_CXINTERNALPATH);
                  if (internalNavPath != null && internalNavPath.trim().length() > 0)
                  {
                     if (internalNavPath.startsWith(PSContentExplorerConstants.PATH_PREFIX))
                     {
                        navPath = navPath.substring(PSContentExplorerConstants.PATH_PREFIX.length());
                        if (navPath.equals(PSContentExplorerConstants.PARAM_INBOX_CONST))
                           navPath = PSContentExplorerConstants.PARAM_PATH_INBOX;
                        else if (navPath.equals(PSContentExplorerConstants.PARAM_OUTBOX_CONST))
                           navPath = PSContentExplorerConstants.PARAM_PATH_OUTBOX;
                        else if (navPath.equals(PSContentExplorerConstants.PARAM_RECENT_CONST))
                           navPath = PSContentExplorerConstants.PARAM_PATH_RECENT;
                        else if (navPath.equals(PSContentExplorerConstants.PARAM_NEWSEARCH_CONST))
                           navPath = PSContentExplorerConstants.PARAM_PATH_NEWSEARCH;
                        else
                        {
                           throw new RuntimeException("Unknown constant value for parameter " + PSContentExplorerConstants.PARAM_CXINTERNALPATH);
                        }
                     }
                     else
                        navPath = internalNavPath;

                     debugMessage("Internal path is set as path to select " + navPath);
                  }
                  else
                  {
                     navPath = getParameter(PSContentExplorerConstants.PARAM_CXDISPLAYPATH);
                     if (navPath != null && navPath.trim().length() > 0)
                     {
                        byName = false;
                        debugMessage("Display path is set as path to select " + navPath);
                     }
                  }
               }
               if (navPath == null || navPath.trim().length() == 0)
               {
                  // use the saved option if it exists and can be found
                  if (m_selPath != null && m_selPath.trim().length() != 0)
                  {
                     PSNode test = ((PSMainView) m_mainView).getNavTree().getNode(m_selPath, true);
                     if (test != null)
                        navPath = m_selPath;
                  }

                  if (navPath == null || navPath.trim().length() == 0)
                  {
                     // if navPath still not set default to Inbox
                     navPath = PSContentExplorerConstants.PARAM_PATH_DEFAULT;
                     debugMessage("Using default as path to select " + navPath);
                  }
               }

               ((PSMainView) m_mainView).getNavTree().setSelectionPath(navPath, byName);

               // next lines used to make sure when we select the item in the
               // tree, that the main view is updated correctly -mgb
               m_mainView.invalidate();
               m_mainView.validate();
               m_mainView.repaint();
            }
            else if (m_view.equals(PSUiMode.TYPE_VIEW_IA))
            {
               ((PSMainView) m_mainView).getNavTree().setSelectionPath(PSContentExplorerConstants.PARAM_PATH_PARENT_ITEM, true);

            }
            else if (m_view.equals(PSUiMode.TYPE_VIEW_DT))
            {
               ((PSDependencyViewer) m_mainView).selectDefaultNode();
            }
            m_splash.setMessage("Loading complete");
            m_splash.dispose();

         });
      }

   }

   /**
    * Save the user options on applet stop.
    */
   @Override
   public void stop()
   {
      try
      {
         debugMessage("saving the display options");
         if (m_optionsManager != null)
         {
            saveAppletState();
            m_optionsManager.save(false);
         }
         else
         {
            debugMessage("Stopping applet. m_optionsManager is NULL");
         }

         if (m_sessionCheck != null)
         {
            m_sessionCheck.stop();
            m_sessionCheck = null;
         }


         if (m_cursor != null)
         {
            m_cursor.terminate();
            m_cursor = null;
         }
         unregisterKeyEventPostProcessor();
      }
      catch (PSContentExplorerException ex)
      {
         log.debug("Cannot save user options on stop session may already be expired",ex);
      }
   }


   /**
    *
    */
   public void logout()
   {
      try
      {
         if (m_httpConnection!=null)
         {
            URL url = new URL(getRhythmyxCodeBase(), "../logout");
            m_httpConnection.postData(url, new HashMap<String,String>());
         }
      }
      catch ( IOException | PSException e)
      {
         log.debug("Error sending logout request- this may be normal as session may already be closed ",e );
      }
   }

   @Override
   public void destroy()
   {
      if(check!=null)
      {
         check.shutdown();
         check = null;
      }
      if (ms_optChangeUpdater != null)
         ms_optChangeUpdater.removeApplet(this);
      else
      {
         debugMessage("Destroying applet. ms_optChangeUpdater is NULL");
      }
   }

   /**
    * Save the current applet state, this includes all the expanded folders, the
    * display formats set on them and the currently selected folder.
    */
   public void saveAppletState()
   {
      PSOptions options = m_optionsManager.getCategoryOptions(m_view);

      // if not defined yet, create a new one
      if (options == null)
      {
         options = new PSOptions(m_view);
         m_optionsManager.addUserOptions(options);
      }

      if (m_view.equals(PSUiMode.TYPE_VIEW_CX))
      {
         String sel = ((PSMainView) m_mainView).getNavTree().getSelectedPath(true);

         if (sel != null && sel.trim().length() > 0)
            options.addOption(PSUiMode.TYPE_MODE_NAV, PSContentExplorerConstants.SELECTED_PATH_OPTION, sel);

         String divLoc = "" + m_mainView.getDividerLocation();
         options.addOption(PSUiMode.TYPE_MODE_NAV, PSContentExplorerConstants.DIVIDER_LOCATION_OPTION, divLoc);

         PSExpandedOption exp = new PSExpandedOption(((PSMainView) m_mainView).getNavTree().getExpandedList());
         options.addOption(PSUiMode.TYPE_MODE_NAV, PSContentExplorerConstants.EXPANDED_OPTION, exp);

         if (m_dfo != null && m_dfo.haveDisplayFormats())
            options.addOption(PSUiMode.TYPE_MODE_NAV, PSContentExplorerConstants.DISPLAY_FORMAT_OPTION, m_dfo);

         if (m_cwo != null && m_cwo.haveColumnWidths())
            options.addOption(PSUiMode.TYPE_MODE_NAV, PSContentExplorerConstants.COlUMN_WIDTHS_OPTION, m_cwo);
      }
   }

   /**
    * Restore the applet state based on the saved options, this includes all the
    * expanded folders, the display formats set on them and the currently
    * selected folder.
    */
   private void restoreAppletState()
   {
      PSOptions options = m_optionsManager.getCategoryOptions(m_view);

      if (options != null)
      {
         PSOption option = options.getOption(PSContentExplorerConstants.DISPLAY_FORMAT_OPTION);
         if (option != null)
            m_dfo = (PSDisplayFormatOption) option.getOptionValue();

         option = options.getOption(PSContentExplorerConstants.COlUMN_WIDTHS_OPTION);
         if (option != null)
            m_cwo = (PSColumnWidthsOption) option.getOptionValue();

      

         option = options.getOption(PSContentExplorerConstants.EXPANDED_OPTION);
         if (option != null)
         {
            PSStopwatch timer = new PSStopwatch();
            try
            {
               timer.start();
               m_actManager.getFolderActionManager().setQuickExpand(true);
               PSExpandedOption expanded = (PSExpandedOption) option.getOptionValue();
               if (expanded != null)
                  ((PSMainView) m_mainView).getNavTree().setExpandedList(expanded.getPaths());
            }
            finally
            {
               m_actManager.getFolderActionManager().setQuickExpand(false);
               timer.stop();
            }
         }
         
         option = options.getOption(PSContentExplorerConstants.SELECTED_PATH_OPTION);
         if (option != null)
         {
            m_selPath = (String) option.getOptionValue();
            ((PSMainView) m_mainView).getNavTree().setSelectionPath(m_selPath, false);
         }

         option = options.getOption(PSContentExplorerConstants.DIVIDER_LOCATION_OPTION);
         if (option != null)
         {
            String divLoc = (String) option.getOptionValue();
            if (divLoc != null)
               m_mainView.setDividerLocation(Integer.parseInt(divLoc));
         }
      }
   }

   /**
    * Given a parent node, returns the column widths currently set for that
    * node. If the node is not found or the column width options have not been
    * initialized, returns <code>null</code>.
    *
    * @param node the node of an existing item, must not be <code>null</code> or
    *           empty.
    * @return the column widths for the specified node, <code>null</code> if the
    *         node is not found or the column width options have not been
    *         initialized properly
    */
   public List getColumnWidthsFromOptions(PSNode node)
   {
      if (m_cwo == null)
         return null;

      String nodePath = ((PSMainView) m_mainView).getNavTree().convertNodeToPath(node);

      return m_cwo.getItemColumnWidths(nodePath);
   }

   /**
    * Given a parent node and a list of column widths, save that information
    * into the column width options object. This will be used later for both
    * retrieving the column widths at runtime and for saving the current column
    * widths.
    *
    * @param node the node of an exisiting item, must not be <code>null</code>
    *           or empty.
    * @param widths the column widths of the specified node. If
    *           <code>null</code> or empty, we remove this entry from the column
    *           widths object map.
    */
   public void saveColumnWidthsToOptions(PSNode node, List widths)
   {
      if (m_cwo == null)
         m_cwo = new PSColumnWidthsOption();

      if (node != null)
      {
         String nodePath = ((PSMainView) m_mainView).getNavTree().convertNodeToPath(node);

         if (widths == null || widths.isEmpty())
            m_cwo.removeItemColumnWidths(nodePath);
         else
            m_cwo.addItemColumnWidths(nodePath, widths);
      }
   }

   /**
    * Given an id to a folder, returns the display format currently set for that
    * folder. If the folder is not found or the display format options have not
    * been inited, returns <code>null</code>.
    *
    * @param node the node of an existing item, must not be <code>null</code> or
    *           empty.
    * @return the display format for the specified folder, <code>null</code> if
    *         the folder is not found or the display format options have not
    *         been initialized properly
    */
   public String getDisplayFormatIdFromOptions(PSNode node)
   {
      if (m_dfo == null)
         return null;

      String nodePath = ((PSMainView) m_mainView).getNavTree().convertNodeToPath(node);

      return m_dfo.getItemDisplayFormat(nodePath);
   }

   /**
    * Given a folder id and a display format, save that information into the
    * display format options object. This will be used later for both retrieving
    * the display format at runtime and for saving the current display format.
    *
    * @param node the node of an exisiting item, must not be <code>null</code>
    *           or empty.
    */
   public void saveDisplayFormatIdToOptions(PSNode node)
   {
      if (m_dfo == null)
         m_dfo = new PSDisplayFormatOption();

      if (node != null)
      {
         String nodePath = ((PSMainView) m_mainView).getNavTree().convertNodeToPath(node);
         String displayFormatId = node.getDisplayFormatId();
         if (displayFormatId == null || displayFormatId.trim().length() == 0)
            m_dfo.removeItemDisplayFormat(nodePath);
         else
            m_dfo.addItemDisplayFormat(nodePath, displayFormatId);

         // be sure to reset the column widths since we have now set a new
         // display format on the specified node
         saveColumnWidthsToOptions(node, null);
      }
   }

   /**
    * Gets the parent frame of this applet and initializes the parent frame of
    * the applet and caches. Use {@link #getDialogParentFrame()}if you need a
    * frame for a dialog parent.
    *
    * @return the frame may be <code>null</code> if this applet is not contained
    *         in a frame.
    */
   public Frame getParentFrame()
   {
      if (m_parentFrame == null)
      {
         Window parentWindow = SwingUtilities.getWindowAncestor(this);

         if (parentWindow instanceof Frame)
            m_parentFrame = (Frame) parentWindow;
      }
      return m_parentFrame;
   }

   /**
    * Gets the parent frame of this applet when appropriate. This is intended to
    * be set as the parent frame for all dialogs created in the applet to allow
    * proper modal dialog behavior. See {@link #getParentFrame()}for more
    * details.
    *
    * @return the aprent frame or <code>null</code> if this applet is not
    *         contained in a frame or is running on a broken version of the
    *         plugin.
    */
   public Frame getDialogParentFrame()
   {
      Frame parentFrame = getParentFrame();

      if (parentFrame != null)
      {
         // Work around a swing bug that exhibits itself in IE sometimes
         if (parentFrame.isLightweight())
         {
            if (parentFrame.getParent() != null)
               return parentFrame;
            else
               return null;
         }
         else if (parentFrame.isDisplayable())
         {
            return parentFrame;
         }
         else
         {
            return null;
         }
      }
      else
      {
         return null;
      }
   }

   /**
    * Get the Rhythmyx code base. This is the location of the sys_resources
    * application that contains system resource, including the applet JAR's
    * which are expected in a subdirectory names <code>AppletJars</code>, e.g.
    * <code>http://host:port/Rhythmyx/sys_resources/</code>.
    *
    * @return the URL to the Rhythmyx system resources, never <code>null</code>.
    */
   public URL getRhythmyxCodeBase()
   {
      String codeBase = getCodeBase().toString();
      int index = codeBase.indexOf("dce/");
      URL documentBase = null;
      if (index > 0)
      {

         try
         {
            codeBase = codeBase.substring(0, index);
            if(!codeBase.contains("Rhythmyx")) {
               codeBase = codeBase + "Rhythmyx/";
            }
            documentBase = new URL(codeBase);
         }
         catch (MalformedURLException e)
         {
            e.printStackTrace();
         }
      }

      return documentBase;
   }

   /**
    * We cannot dynamically set the codebase for AjaxSwing. We therefore set an
    * applet parameter with the value and override the applet getCodeBase method
    * if the value is available.
    */
   @Override
   public URL getCodeBase()
   {

      if (PSAjaxSwingWrapperLocator.getInstance().isAjaxSwingEnabled())
      {

         URL documentBase = super.getCodeBase();
         log.debug("original codebase for ajaxswing = " + documentBase);
         String host = documentBase.getHost();
         String proto = documentBase.getProtocol();
         int port = documentBase.getPort();
         try
         {
            documentBase = new URL(proto, host, port, "/Rhythmyx/dce/");
         }
         catch (Exception e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         return documentBase;

      }
      else if (getParameter("SWING") != null && getParameter("SWING").equals("true"))
      {

         URL documentBase = super.getCodeBase();

         // these should already be set in the login panel
         String host = getParameter("serverName");
         String proto = getParameter("protocol");
         int port = (StringUtils.isEmpty(getParameter("port"))) ? -1 : Integer.parseInt(getParameter("port"));

         try
         {
            documentBase = new URL(proto, host, port, "/Rhythmyx/dce/");
         }
         catch (Exception e)
         {
            log.error("invalid document base "+proto + " " + host + " " + port);
         }
         return documentBase;

      }
      else
      {
         return super.getCodeBase();

      }
   }

   /**
    * We cannot dynamically set the codebase for AjaxSwing. We therefore set an
    * applet parameter with the value and override the applet getCodeBase method
    * if the value is available.
    */

   @Override
   public URL getDocumentBase()
   {

      if (!PSAjaxSwingWrapperLocator.getInstance().isAjaxSwingEnabled() && !getParameter("SWING").equals("true"))
      {
         return super.getCodeBase();
      }
      else
      {
         URL documentBase = super.getCodeBase();
         String host = documentBase.getHost();
         String proto = documentBase.getProtocol();
         int port = documentBase.getPort();
         try
         {
            documentBase = new URL(proto, host, port, "/Rhythmyx/dce/");
         }
         catch (Exception e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         return documentBase;
      }

   }

   /**
    * Get the system log base. This is the location of the sys_logs application
    * that contains system logs, e.g.
    * <code>http://host:port/Rhythmyx/sys_logs/</code>.
    *
    * @return the URL to the system logs, never <code>null</code>.
    */
   public URL getSystemLogBase()
   {
      String codeBase = getCodeBase().toString();
      int index = codeBase.indexOf("/Rhythmyx/dce/");

      URL systemLogBase = null;
      try
      {
         systemLogBase = new URL(codeBase.substring(0, index) + "sys_logs/");
      }
      catch (MalformedURLException e)
      {
         // this should never happen
      }

      return systemLogBase;
   }

   /**
    * Gets the current view represented by this applet.
    *
    * @return the view, never <code>null</code> or empty, one of the <code>
    * PSUiMode.TYPE_VIEW_xxx</code> values.
    */
   public String getView()
   {
      return m_view;
   }

   /**
    * Initializes the view represented by this applet based on the parameter
    * values and initializes the appropriate listeners to listen to selection
    * changes in main view and to listen to menu actions. If any of the
    * parameter is not available, tries to get from default it it is defined one
    * for it. See <code>PARAM_xxx</code> values for description of default.
    *
    * @throws IOException if it could not load the document from an url or from
    *            jar.
    * @throws SAXException if any parsing error occurs loading the document from
    *            an input stream
    * @throws PSUnknownNodeTypeException if the document loaded is not in
    *            expected format of that parameter.
    * @throws ParserConfigurationException if there is an error creating the
    *            document builder.
    * @throws PSCmsException if there is an error cataloging data from server.
    * @throws PSContentExplorerException
    */
   private void initView() throws IOException, SAXException, PSUnknownNodeTypeException, ParserConfigurationException,
         PSCmsException, PSContentExplorerException
   {
      // Add you as listener, so can listen on to refresh options and
      // action initiated process.
      m_actManager.addActionListener(this);

      // Get the menu bar
      Document menuDoc = getParameterDocument(PSContentExplorerConstants.PARAM_MENU_URL);
      if (menuDoc == null || menuDoc.getDocumentElement() == null)
         throw new RuntimeException("Document is not found to show top-level action bar");

      // Put display options for use by renderers
      // Since all instances of applet share the same UIManager and this manager
      // holds the latest display options, do not override with the options that
      // we got from server.
      Object optionsObj = UIManager.getDefaults().get(PSContentExplorerConstants.DISPLAY_OPTIONS);
      PSDisplayOptions options = null;
      if (optionsObj != null)
      {
         ClassLoader loader = optionsObj.getClass().getClassLoader();

         ClassLoader cl = ClassLoader.getSystemClassLoader();

         ((URLClassLoader) cl).getURLs();


         if (loader instanceof URLClassLoader)
         {
            ((URLClassLoader) loader).getURLs();


         }
         options = (PSDisplayOptions) optionsObj;
         m_optionsManager.getDisplayOptions().copyFrom(options);

      }
      else
      {
         log.debug("options null setting");
         options = m_optionsManager.getDisplayOptions();
         UIManager.getDefaults().put(PSContentExplorerConstants.DISPLAY_OPTIONS, m_optionsManager.getDisplayOptions());
      }

      // get the standard
      Element menuInputs = menuDoc.getDocumentElement();

      /*
       * JAVA WEB START FORKING If started with JWS do the following: RHYT-455:
       * . The User, Logout, Community, Role, and Locale changing options are
       * incorporated into the Desktop Content Explorer menu bar.
       */
      if (isApplication())
      {

         // menuInputs.appendChild()
      }

      // Create the menu bar with menu source that gets updated as selection
      // changes.
      PSMenuBar menuBar = new PSMenuBar(menuInputs);
      PSMenuSource menuSource = new PSMenuSource();

      // This has been modified to fully populate on selection change
      // instead of waiting to upload submenus on selection. This
      // makes accessibility keys in submenus available.
      m_globalMenuBar = new PSContentExplorerMenuBar(menuBar, menuSource, m_actManager);

      // Construct View panel
      if (m_view.equals(PSUiMode.TYPE_VIEW_DT))
      {
       //Impact analysis populate relationship map
         PSRelationshipInfoSet relationships = m_actManager.getRelationshipsManager().getRelationships();
         getRelMap().clear();
         Iterator iter = relationships.getComponents();
         while( iter.hasNext()){
            PSRelationshipInfo info = (PSRelationshipInfo) iter.next();
            getRelMap().put(info.getName(), info);
         }
         if (relationships == null || !relationships.getComponents().hasNext())
            throw new RuntimeException("relationships are not found to show dependency tree view");

         String name = getParameter(PSContentExplorerConstants.PARAM_ITEM_TITLE);
         String contentid = getParameter(PSContentExplorerConstants.PARAM_CONTENTID);
         String revisionid = getParameter(PSContentExplorerConstants.PARAM_REVISIONID);

         if (name == null || contentid == null || revisionid == null)
            throw new RuntimeException("Item is not found to initialize dependency tree");
         Map<String, String> iconMap = PSCxUtil.getItemIcons(
               Collections.singletonList(new PSLocator(contentid, revisionid)), getApplet());
         String iconKey = iconMap.get(contentid);
         PSNode itemNode = new PSNode(name, name, PSNode.TYPE_DTITEM, "", iconKey, true, -1);
         PSProperties nodeProps = getDisplayFormatProperties(contentid);
         itemNode.setProperties(nodeProps);
         itemNode.setProperty(IPSConstants.PROPERTY_CONTENTID, contentid);
         itemNode.setProperty(IPSConstants.PROPERTY_REVISION, revisionid);
         itemNode.setProperty(IPSConstants.PROPERTY_RELATIONSHIP, ((PSRelationshipInfo) relationships.getComponents()
               .next()).getName());

         m_mainView = new PSDependencyViewer(m_actManager, itemNode);

         // Construct action bar (menu bar)
         m_actionBar = new PSActionBar(this, m_globalMenuBar, m_view, relationships, (PSDependencyViewer) m_mainView);

         // register the view panel as an action listener for the menu actions
         m_actManager.addActionListener((PSDependencyViewer) m_mainView);

      }
      else
      {
         Document navDoc = getParameterDocument(PSContentExplorerConstants.PARAM_NAV_URL);
         if (navDoc == null || navDoc.getDocumentElement() == null)
            throw new RuntimeException("Document is not found to show navigational tree.");

         PSNode navRoot = new PSNode(navDoc.getDocumentElement());
         m_mainView = new PSMainView(navRoot, m_view, this);

         m_actionBar = new PSActionBar(this, m_globalMenuBar, m_view, null, null);
         // Add search listener to allow the action bar to react to search
         // results.
         m_actManager.addSearchListener(m_actionBar);

         PSMainView tempView = (PSMainView) m_mainView;
         // add selection listener that are interested in selection changes

         tempView.addSelectionListener(m_actionBar);
         tempView.addSelectionListener(m_globalMenuBar);

         // action manager needs to know current content path,
         // so that it can pass this information down to the folder dialog.
         tempView.addSelectionListener(m_actManager);

         // register the panel as an action listener
         m_actManager.addActionListener(tempView);
      }
      getContentPane().removeAll();
      getContentPane().setLayout(new BorderLayout());
      
   
      
      JPanel headerPanel = new JPanel();
      headerPanel.setName("Header");
      headerPanel.setLayout(new BorderLayout());
      if (PSContentExplorerApplication.getBaseFrame()!=null  && m_view.equals(PSUiMode.TYPE_VIEW_CX))
      {
         dceHeader = new PSContentExplorerHeader(this);
         headerPanel.add(dceHeader, BorderLayout.CENTER);
      }
      headerPanel.add(m_actionBar, BorderLayout.SOUTH);
      
      m_actionBar.setBorder(BorderFactory.createEmptyBorder());
      
      getContentPane().add(headerPanel, BorderLayout.NORTH);
      getContentPane().add(m_mainView, BorderLayout.CENTER);

      applyDisplayOptions(options, this);
      
      
      PSFocusBorder focusBorder = new PSFocusBorder(1, options);
      if (dceHeader!=null)
         focusBorder.addToAllNavigable(dceHeader);
      focusBorder.addToAllNavigable(m_actionBar);
      focusBorder.addToAllNavigable(m_globalMenuBar);
      focusBorder.addToAllNavigable(m_mainView);

      setDefaultFocus();
      // Add this applet instance to update the display when the options change
      ms_optChangeUpdater.addApplet(this);
   }

   public void setDefaultFocus()
   {
      if (dceHeader!=null)
         dceHeader.requestFocus();
   }


   /**
    * Returns the properties for the supplied contentid, gets the default
    * display format and uses executable search to search for the supplied
    * content id, that will return a node with all properties from the display
    * format set on the node. Gets the properties that were set on the node and
    * returns them.
    *
    * @param contentid must not be empty and must be an Integer id of the
    *           content item.
    * @return the properties corresponding to the default displayformat columns.
    *         May be <code>null</code>.
    * @throws PSContentExplorerException
    */
   private PSProperties getDisplayFormatProperties(String contentid) throws PSContentExplorerException
   {
      if (StringUtils.isBlank(contentid))
         throw new IllegalArgumentException("contentid must not be blank");
      Integer id = null;
      try
      {
         id = new Integer(contentid);
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException("contentid must not be an Integer");
      }
      PSDisplayFormat format = getActionManager().getDisplayFormatCatalog().getDisplayFormatById("0"); // get
      // the
      // default
      // display
      // format

      // create a search and add the fields for the dynamic properties
      PSExecutableSearch search = new PSExecutableSearch(getRhythmyxCodeBase(), format, Collections.singletonList(id),
            getApplet());

      // execute the search
      PSNode tmpNode = new PSNode("temp", "temp", PSNode.TYPE_PARENT, null, null, false, -1);
      Iterator results = search.executeSearch(tmpNode).iterator();
      PSNode resNode = (PSNode) results.next();
      return resNode.getProperties();
   }

   /**
    * Implemented to listen to change of display options. Applies new options to
    * all components and repaints.
    *
    * @param event the action event that provides the refresh hint, may not be
    *           <code>null</code>.
    *
    * @throws IllegalArgumentException if event is <code>null</code>.
    */
   @Override
   public void actionExecuted(PSActionEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event may not be null.");

      if (event.getRefreshHint().equals(PSActionEvent.REFRESH_OPTIONS))
      {
         PSDisplayOptions options = m_optionsManager.getDisplayOptions();

         UIManager.getDefaults().put(PSContentExplorerConstants.DISPLAY_OPTIONS, options);
         ms_optChangeUpdater.optionsChange(options);
      }
   }

   /**
    * Displays status dialog in a new thread whose progress bar is updated by
    * the monitor as and when monitor's status gets updated. If the process is
    * completed, by the time the dialog is initialized, the dialog is not shown.
    *
    * @param monitor the monitor to use with dialog to update its status or to
    *           cancel the process, may not be <code>null</code>
    */
   @Override
   public void actionInitiated(final PSProcessMonitor monitor)
   {
      if (monitor == null)
         throw new IllegalArgumentException("monitor may not be null.");

      final PSContentExplorerStatusDialog dlg = new PSContentExplorerStatusDialog(getDialogParentFrame(), monitor,
            getApplet());

      SwingUtilities.invokeLater(() -> {
         if (monitor.getStatus() != PSProcessMonitor.STATUS_COMPLETE)
            dlg.setVisible(true);
      });
   }

   /**
    * The class used to update the display options for all the applet instances
    * loaded in this process (jvm).
    */
   private class OptionsUpdater
   {
      /**
       * Handles the change of display options. This should be called whenever
       * the options are changed in any instance.
       *
       * @param options the new display options to apply to all the applets that
       *           are interested in options change.
       *
       * @throws IllegalArgumentException if options is <code>null</code>
       */
      public void optionsChange(PSDisplayOptions options)
      {
         if (options == null)
            throw new IllegalArgumentException("options may not be null.");

         Iterator<PSContentExplorerApplet> applets = m_applets.iterator();
         while (applets.hasNext())
         {
            PSContentExplorerApplet applet = applets.next();
            applet.applyDisplayOptions(options, applet);
            applet.repaint();

            // update option manager options to reflect the change
            applet.getOptionsManager().getDisplayOptions().copyFrom(options);
         }
      }

      /**
       * Adds the applet to its list of applets to be updated with new display
       * options.
       *
       * @param applet the applet to be updated, may not be <code>null</code>
       */
      public void addApplet(PSContentExplorerApplet applet)
      {
         if (applet == null)
            throw new IllegalArgumentException("applet may not be null.");

         m_applets.add(applet);
      }

      /**
       * Removes the applet from its list of applets to be updated with new
       * display options.
       *
       * @param applet the applet, not <code>null</code>.
       */
      public void removeApplet(final PSContentExplorerApplet applet)
      {
         if (applet == null)
         {
            throw new IllegalArgumentException("applet may not be null.");
         }

         m_applets.remove(applet);
      }

      /**
       * The list of applets to update, initialized at construction time and
       * gets modified through calls to
       * {@link #addApplet(PSContentExplorerApplet)}. Never <code>null</code>
       */
      private List<PSContentExplorerApplet> m_applets = new ArrayList<PSContentExplorerApplet>();
   }

   /**
    * Applys display options specified to the supplied component and all its
    * sub-components recurively. The background color is not applied to some
    * components that holds to its default background(JPopupMenu, JViewPort,
    * JTree and JTable).
    *
    * @param dispOptions the display options to apply, if <code>null</code>
    *           nothing will be applied.
    * @param comp the parent component, assumed not <code>null</code>
    */
   void applyDisplayOptions(PSDisplayOptions dispOptions, Component comp)
   {
      if (dispOptions != null)
      {
         
        // if (!(comp instanceof JPopupMenu || comp instanceof JViewport || comp instanceof JTree
               
         if (!(SwingUtilities.getAncestorOfClass(PSContentExplorerHeader.class, comp) != null || comp instanceof PSContentExplorerHeader ||comp instanceof JViewport || comp instanceof JTree
               || comp instanceof JTable || comp instanceof JScrollPane || comp instanceof JComboBox || comp instanceof JTextArea))
         {
            comp.setBackground(dispOptions.getBackGroundColor());
         }
      
         if (SwingUtilities.getAncestorOfClass(PSContentExplorerHeader.class, comp) != null)
         {
           
            if (!(comp instanceof JComboBox))
               comp.setForeground(dispOptions.getForeGroundColor());
            comp.setFont(dispOptions.getMenuFont());
         }
         else if (SwingUtilities.getAncestorOfClass(m_actionBar.getClass(), comp) != null)
         {
           
            if (!(comp instanceof JComboBox))
               comp.setForeground(dispOptions.getMenuForeGroundColor());
            comp.setFont(dispOptions.getMenuFont());
         }
         else
         {
            if (!(comp instanceof JComboBox))
            {
               // Use different colors if the component represents Headings
               if (StringUtils.contains(PSContentExplorerConstants.HEADING_COMP_NAME,comp.getName()))
                  comp.setForeground(dispOptions.getHeadingForeGroundColor());
               else
                  comp.setForeground(dispOptions.getForeGroundColor());
            }

            comp.setFont(dispOptions.getFont());
         }
         if (comp instanceof Container)
         {
            Container container = (Container) comp;
            for (int i = 0; i < container.getComponentCount(); i++)
            {
               Component childComp = container.getComponent(i);
               applyDisplayOptions(dispOptions, childComp);
            }
         }
      }
   }

   /**
    * Set wait cursor before a lengthy operation. Must call resetCursor() after
    * the operation is complete.
    */
   void setWaitCursor()
   {
      m_cursor.setWaitCursor();
   }

   /**
    * Reset the cursor to normal. Normally called when the previous cursor was a
    * wait cursor and a lengthy operation is complete.
    */
   void resetCursor()
   {
      m_cursor.resetCursor();
   }

   /**
    * Return the user's current state information with Rhythmyx server.
    *
    * @return user inof object, never <code>null</code>.
    */
   public PSUserInfo getUserInfo()
   {
      return ms_userInfo;
   }

   /**
    * Return the current client context.
    *
    * @return client context
    */
   public String getClientContext()
   {
      /** @todo implement me */
      return "client context";
   }

   /**
    * Gets the action manager of this applet.
    *
    * @return the manager, never <code>null</code>
    */
   public PSActionManager getActionManager()
   {
      return m_actManager;
   }

   /**
    * Gets the option manager of this applet.
    *
    * @return the manager, never <code>null</code>
    */
   public PSOptionManager getOptionsManager()
   {
      return m_optionsManager;
   }

   /**
    * Gets the global menu bar of this applet.
    *
    * @return the menu bar, never <code>null</code>
    */
   public PSContentExplorerMenuBar getGlobalMenuBar()
   {
      return m_globalMenuBar;
   }

   /**
    * Gets the resource string identified by the specified key. If the resource
    * cannot be found, the key itself is returned.
    *
    * @param key identifies the resource to be fetched; may not be <code>null
    * </code> or empty.
    *
    * @return String value of the resource identified by <code>key</code>, or
    *         <code>key</code> itself.
    *
    * @throws IllegalArgumentException if key is <code>null</code> or empty.
    */
   public String getResourceString(String key)
   {
      if (key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty");

      String resourceValue = PSI18NTranslationKeyValues.getInstance().getTranslationValue(key);

      // if the resourceValue is empty, return key as specified in the contract:
      if (resourceValue.trim().length() == 0)
         resourceValue = key;
      props.put(key, "");
      return resourceValue;
   }

   private final Properties props = new Properties();

   /**
    * Gets the resource string identified by the specified key for the specified
    * class instance. If the resource cannot be found, the key itself is
    * returned. Constructs the key from the class name if the key does not
    * confirm to the format 'classname@key'.
    *
    * @param resClass the class instance for which the resource is required, may
    *           not be <code>null</code>
    * @param key identifies the resource to be fetched; may not be <code>null
    * </code> or empty.
    *
    * @return String value of the resource identified by <code>key</code>, or
    *         <code>key</code> itself.
    *
    * @throws IllegalArgumentException if key is <code>null</code> or empty.
    */
   public String getResourceString(Class resClass, String key)
   {
      if (resClass == null)
         throw new IllegalArgumentException("resClass may not be null.");

      if (key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty");

      if (key.startsWith("@"))
         key = resClass.getName() + key;
      else if (!key.startsWith(resClass.getName()))
         key = resClass.getName() + "@" + key;

      return getResourceString(key);
   }

   /**
    * Indicates if the applet is currently running on a Mac OS platform
    *
    * @return <code>true</code> if the platform is Mac.
    */
   public static boolean isMacPlatform()
   {
      return m_osName.startsWith("Mac OS");
   }

   /**
    * Convenience method that calls
    * {@link #getResourceMnemonic(String, String, char)
    * getResourceMnemonic(resClass.getName(), label, mnemonic)}
    */
   public static char getResourceMnemonic(Class resClass, String label, char mnemonic)
   {
      if (resClass == null)
         throw new IllegalArgumentException("resClass may not be null.");

      if (label == null || label.trim().length() == 0)
         throw new IllegalArgumentException("label may not be null or empty");

      return getResourceMnemonic(resClass.getName(), label, mnemonic);
   }

   /**
    * This method retrieves a resource that must contain a single character to
    * use as the mnemonic for a given label, button or menu item.
    *
    * @param category the category of the resource, usually the classname, must
    *           never be <code>null</code> or empty
    * @param label the label of the specific item, must never be
    *           <code>null</code> or empty
    * @param mnemonic the value to return if no mnemonic is found
    * @return The mnemonic or the mnemonic parameter if no mnemonic is found
    */
   public static char getResourceMnemonic(String category, String label, char mnemonic)
   {
      if (category == null || category.trim().length() == 0)
         throw new IllegalArgumentException("category may not be null or empty");

      if (label == null || label.trim().length() == 0)
         throw new IllegalArgumentException("label may not be null or empty");

      // Make sure we strip @ from the label
      if (label.startsWith("@") && label.length() > 1)
         label = label.substring(1);

      String string = category + "@" + label;

      int m = PSI18NTranslationKeyValues.getInstance().getMnemonic(string);

      if (m != 0)
      {
         return (char) m;
      }
      else
      {
         return mnemonic;
      }
   }

   /**
    * Lookup the associated tooltip text for the given class and label
    *
    * @param clazz the class, must never be <code>null</code>
    * @param label the label, must never be <code>null</code> or empty
    * @return the tooltip or <code>null</code> if undefined
    */
   public static String getResourceTooltip(Class clazz, String label)
   {
      if (clazz == null)
         throw new IllegalArgumentException("clazz may not be null.");

      if (label == null || label.trim().length() == 0)
         throw new IllegalArgumentException("label may not be null or empty");

      // Make sure we strip @ from the label
      if (label.startsWith("@") && label.length() > 1)
         label = label.substring(1);

      String key = clazz.getName() + "@" + label;

      return PSI18NTranslationKeyValues.getInstance().getTooltip(key);
   }

   /**
    * The method that is called by JavaScript to referesh the view when the
    * LiveConnect is enabled.
    *
    * @param refreshIdentifier the identifier, must be one of the REFRESH_xxx
    *           values
    *
    * @throws IllegalArgumentException if refreshIdentifier is invalid.
    */
   public void refresh(String refreshIdentifier)
   {
      refresh(refreshIdentifier, null, null);
   }

   /**
    * The method that is called by JavaScript to referesh the specified item in
    * the view when the LiveConnect is enabled.
    *
    * @param refreshIdentifier the identifier, must be one of the REFRESH_xxx
    *           values
    * @param contentIds a list of content ids of the items to refresh,
    *           semi-colon delimited, may be <code>null</code> or empty to
    *           refresh the entire view.
    * @param revisionIds a list of revision ids of the items to refresh,
    *           semi-colon delimited, may be <code>null</code> or empty, ignored
    *           if <code>contentId</code> is <code>null</code> or empty.
    */
   public void refresh(String refreshIdentifier, String contentIds, String revisionIds)
   {
      log.debug("refresh called " + refreshIdentifier + "," + contentIds + "," + revisionIds);
      if (refreshIdentifier == null)
         throw new IllegalArgumentException("refreshIdentifier may not be null");
      if (contentIds != null && contentIds.trim().length() == 0)
         contentIds = null;

      PSActionEvent event;
      if (contentIds != null)
      {
         // cause dirtying of nodes to get selective refresh in all views etc.
         StringTokenizer tok = new StringTokenizer(contentIds, ";");
         while (tok.hasMoreTokens())
         {
            String contentId = tok.nextToken();
            PSNode tmpNode = new PSNode("tmp", "tmp", PSNode.TYPE_ITEM, null, null, false, -1);
            tmpNode.setProperty(IPSConstants.PROPERTY_CONTENTID, contentId);
            event = new PSActionEvent(PSActionEvent.DIRTY_NODES);
            event.setRefreshNodes(PSIteratorUtils.iterator(tmpNode));
            ((IPSActionListener) m_mainView).actionExecuted(event);
         }
      }

      // now refresh the view unless hint was just to dirty
      if (!refreshIdentifier.equals(PSActionEvent.DIRTY_NODES))
      {
         // make the refresh view to be asynchronous, so that the caller
         // (a browser) does not have to wait for this (lengthy) operation.
         final String innerRefreshIdentifier = refreshIdentifier;
         SwingUtilities.invokeLater(() -> {
            PSActionEvent theEvent = new PSActionEvent(innerRefreshIdentifier);
            ((IPSActionListener) m_mainView).actionExecuted(theEvent);

            // Several plugins have painting issues after a child window is
            // closed on Windows 2000. We force repaint after refresh.
            //if (m_osName.indexOf("2000") != -1)

               repaint();

         });
      }
   }

   /**
    * Gets document represented by the supplied applet parameter. If the
    * parameter is not set with this applet, it tries to get the default value
    * based on the view the applet representing. Logs the message if the debug
    * mode is turned on and it is using defaults.
    *
    * @param parameter the parameter whose value is a url that gets a document,
    *           may not be <code>null</code> or empty.
    *
    * @return the document, may be <code>null</code> if the supplied parameter
    *         does not exist and does not have a default or its url does not
    *         result in a document.
    *
    * @throws IllegalArgumentException if parameter is not valid.
    * @throws IOException if it could not load the document from an url or from
    *            jar.
    * @throws SAXException if any parsing error occurs loading the document from
    *            an input stream
    * @throws ParserConfigurationException if there is an error creating the
    *            document builder.
    */
   public Document getParameterDocument(String parameter) throws IOException, SAXException,
         ParserConfigurationException
   {
      if (parameter == null || parameter.trim().length() == 0)
         throw new IllegalArgumentException("parameter may not be null or empty.");

      Document doc = null;
      String url = null;
      if (parameter.equals(PSContentExplorerConstants.PARAM_OPTIONS_URL))
      {
         url = PSContentExplorerConstants.OPTIONS_URL;
      }
      else
      {
         url = getParameter(parameter);
      }

      if (url == null || url.trim().length() == 0)
      {
         debugMessage("The parameter " + parameter + "is not supplied.");

         InputStream in = null;
         String defaultXML = null;
         if (parameter.equals(PSContentExplorerConstants.PARAM_MENU_URL))
         {
            if (m_view.equals(PSUiMode.TYPE_VIEW_CX))
               defaultXML = PSContentExplorerConstants.CX_MENU_XML;
            else if (m_view.equals(PSUiMode.TYPE_VIEW_IA))
               defaultXML = PSContentExplorerConstants.IA_MENU_XML;
            else
               defaultXML = PSContentExplorerConstants.DT_MENU_XML;

         }
         else if (parameter.equals(PSContentExplorerConstants.PARAM_NAV_URL))
         {
            if (m_view.equals(PSUiMode.TYPE_VIEW_CX))
               defaultXML = PSContentExplorerConstants.CX_NAV_XML;
         }
         else if (parameter.equals(PSContentExplorerConstants.PARAM_OPTIONS_URL))
            defaultXML = PSContentExplorerConstants.OPTIONS_XML;
         else if (parameter.equals(PSContentExplorerConstants.PARAM_RS_URL))
            defaultXML = PSContentExplorerConstants.RELATIONS_XML;

         if (defaultXML != null)
         {
            debugMessage("Loading the default document " + defaultXML);

            in = getClass().getResourceAsStream(defaultXML);
            if (in != null)
               doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         }
      }
      else
      {
         doc = getXMLDocument(url);
      }

      return doc;
   }

   /**
    * Easy access method for the new search node. New search node is required
    * very frequently.
    *
    * @return PSNode object for the new search node, may be <code>null</code>
    * @deprecated This is kept for backward compatabilty and is obsolete. Action
    *             manager now has this method.
    */
   @Deprecated
   public PSNode getNewSearchNode()
   {
      if (m_actManager != null)
      {
         return m_actManager.getNewSearchNode();
      }
      return null;
   }

   /**
    * Gets the node that represents the system folder.
    *
    * @return the system folder node, may be <code>null</code> if the current
    *         view does not have this node in its navigational tree.
    */
   public PSNode getSystemFoldersNode()
   {
      return ((PSMainView) m_mainView).getNavTree().getNode(PSContentExplorerConstants.PARAM_PATH_FOLDERS, true);
   }

   /**
    * Easy acces method for the default node. Default node is the node that gets
    * selected on initialization if no other selection is done. This helps to
    * make sure a node always is selected in the navigation pane.
    *
    * @return PSNode object for the default node, never be <code>null</code>.
    */
   public PSNode getDefaultNode()
   {
      return ((PSMainView) m_mainView).getNavTree().getNode(PSContentExplorerConstants.PARAM_PATH_DEFAULT, true);
   }

   /**
    * Easy acces method for getting the selected node.
    *
    * @return selNode the currently selected node, may be <code>null</code>.
    */
   public PSNode getSelectedNavTreeNode()
   {

      if (m_mainView instanceof PSMainView)
         return ((PSMainView) m_mainView).getNavTree().getSelectedNode();
      else
         return null;
   }

   /**
    * Easy acces method for selecting a specific node.
    *
    * @param selNode the specific node to select, must not be <code>null</code>.
    */
   public void setSelectedNavTreeNode(PSNode selNode)
   {
      if (selNode == null)
         throw new IllegalArgumentException("selNode may not be null.");
      if (m_mainView instanceof PSMainView)
         ((PSMainView) m_mainView).getNavTree().selectNode(selNode);
   }

   /**
    * Easy access method. Gets the node that is matching the supplied nodes from
    * already loaded nodes of the tree. Uses <code>equals()</code> method for
    * matching.
    *
    * @param node the node to match, cannot be <code>null</code>
    *
    * @return the matching tree node, may be <code>null</code> if there is no
    *         match.
    */
   public PSNavigationTree.PSTreeNode getTreeNode(PSNode node)
   {
      if (m_mainView instanceof PSMainView)
         return ((PSMainView) m_mainView).getNavTree().getTreeNode(node);
      else
         return null;
   }

   /**
    * Get the view containing the tree w/ folders, views, etc.
    *
    * @return If this instance of the applet uses the <code>PSMainView</code> as
    *         the nav pane, then it is returned, otherwise, <code>null</code> is
    *         returned.
    */
   public PSNavigationTree getNavTree()
   {
      if (m_mainView instanceof PSMainView)
         return ((PSMainView) m_mainView).getNavTree();
      else
         return null;
   }

   /**
    * @return <code>true</code> if content needs to be restricted to user
    *         community.
    */
   public boolean isContentRestrict()
   {
      return ms_restrictContent;
   }

   /**
    * Gets the cache that is used to (lazily) cache the catalogged searchable
    * fields.
    *
    * @return the cache object. It maps cataloger flag of the applet (as
    *         <code>String</code> object) to its corresponding searchable fields
    *         (as <code>PSContentEditorFieldCataloger</code> object). It may be
    *         <code>null</code> if {@link PSContentExplorerConstants#PARAM_CACHE_SEARCHABLE_FIELDS}
    *         parameter of the applet is <code>None</code>.
    *
    * @see PSContentExplorerConstants#PARAM_CACHE_SEARCHABLE_FIELDS
    */
   public Map getSearachableFieldsCache()
   {
      return m_searchableFields;
   }

   /**
    * Returns the debug flag.
    *
    * @return <code>true</code> if applet is in debug mode, otherwise <code>
    * false</code>
    */
   public boolean isDebug()
   {
      return ms_isDebug;
   }

   /**
    * @return <code>true</code> if the ManagedNav (feature) is used by the
    *         current Rhythmyx server instance; otherwise return
    *         <code>false</code>.
    */
   public boolean isManagedNavUsed()
   {
      return ms_isManagedNavUsed;
   }

   /**
    * Returns current search configuration.
    *
    * @return the search configuration, never <code>null</code>.
    *
    * @throws PSCmsException if an error occurs.
    */
   public PSSearchConfig getSearchConfig() throws PSCmsException
   {
      return m_actManager.getSearchConfig();
   }

   /**
    * Method to write the supplied message to console if debug mode is on.
    *
    * @param msg message to write to console, nothing is done if
    *           <code>null</code> or <code>empty</code>.
    */
   public void debugMessage(String msg)
   {
      if (!isDebug() || msg == null || msg.trim().length() < 1)
         return;
       log.debug(msg);
   }

   /**
    * Method to print stack trace to console if debug mode is on.
    *
    * @param t any Exception thrown to write to console, nothing is done if
    *           <code>null</code> or <code>empty</code>.
    */
   public void debugMessage(Throwable t)
   {
      if (!isDebug() || t == null)
         return;
      t.printStackTrace();
   }

   /**
    * Convenience method that calls {@link #getXMLDocument(String, Map)
    * getXMLDocument(strUrl, <code>null</code>)}.
    */
   public Document getXMLDocument(String strUrl) throws IOException, SAXException, ParserConfigurationException
   {
      return getXMLDocument(strUrl, null);
   }

   /**
    * Retrieves the specified xml document from the relative url given.
    *
    * @param strUrl a url relative to {@link #getRhythmyxCodeBase()}, must not
    *           be <code>null</code>, or empty.
    *
    * @param params These name/value pairs are posted with the request made to
    *           the supplied URL. The key is the name of the param. If the value
    *           is an instance of <code>List</code>, then the param is submitted
    *           for each entry in the list. In all cases, the
    *           <code>toString</code> method is used to obtain the value from
    *           the entry. May be <code>null</code> or empty. Neither the keys
    *           or values should be URL encoded.
    *
    * @return the document from the processed url, if the builder factory, or
    *         the builder was not initialized, returns <code>null</code>
    *
    * @throws IOException
    * @throws SAXException
    * @throws ParserConfigurationException
    */
   @SuppressWarnings("unused")
   public Document getXMLDocument(String strUrl, Map<String, ? extends Object> params) throws IOException,
         SAXException, ParserConfigurationException
   {
      if (StringUtils.isBlank(strUrl))
         throw new IllegalArgumentException("strUrl may not be null or empty");
      /*
       * Strip leading "../" which used to be required when this method
       * constructed urls against the codebase, but now is this handled by the
       * remote applet requestor.
       */
      if (strUrl.startsWith("../"))
         strUrl = strUrl.substring("../".length());
      // Massage strUrl to remove spaces. Really, all parameter
      // values should be properly URL encoded by now, but they aren't
      strUrl = strUrl.replace(' ', '+');
      PSRemoteAppletRequester requestor = new PSRemoteAppletRequester(getHttpConnection(), getRhythmyxCodeBase());

      Document document = null;
      try{
        document = requestor.getDocument(strUrl, params == null ? new HashMap<String, String>() : params);
      }catch(IOException ex){

             //if the session is expired close an application for the DCE only.

           if( PSContentExplorerApplication.sessionExpired > 1 && PSContentExplorerApplication.getApplet().getParameter("SWING") != null && PSContentExplorerApplication.getApplet().getParameter("SWING").equals("true")
                      && ex.getMessage().contains("You must log back into Rhythmyx to continue")){

                   Platform.exit();
                   System.exit(0);


            }else{
                ++PSContentExplorerApplication.sessionExpired;
                throw new IOException(ex.toString());
             }
          }



      return document;
   }

   /**
    * Retrieves the specified icon from the relative url given.
    *
    * @param strUrl a relative url, must not be <code>null</code>, or empty
    *
    * @return the icon from the processed url, returns <code>null</code> if the
    *         icon could not be loaded
    *
    * @throws MalformedURLException
    */
   public Icon getIcon(String strUrl) throws MalformedURLException
   {
      URL url = new URL(getRhythmyxCodeBase(), strUrl);

      ImageIcon icon = (ImageIcon) m_imgCache.get(url.toString());
      if (icon == null)
      {
         Image image = Toolkit.getDefaultToolkit().createImage(url);
         icon = new ImageIcon(image);
         m_imgCache.put(url.toString(), icon);
      }

      return icon;
   }

   private static HashMap m_imgCache = new HashMap();

   /**
    * Determine if an external search engine is available on the server.
    * 
    *
    * @return <code>true</code> if one is available, <code>false</code> if not.
    */
   public boolean isSearchEngineAvailable()
   {
      try
      {
         return this.getSearchConfig().isFtsEnabled();
      }
      catch (PSCmsException e)
      {
         log.error("Cannot get search config for FtsEnabled",e);
         return false;
      }
   }

   /**
    * Displays error message dlg by constructing the message and title from the
    * supplied keys. Dynamically formats the message and title if the
    * corresponding parameters are supplied. See
    * {@link #getResourceString(Class, String) } for more info on message
    * construction. Uses
    * {@link MessageFormat#format(String, Object[])}for formatting.
    *
    * @param parent the parent window of the error dialog, may be <code>null
    * </code>. If <code>null</code> uses this applet's frame as parent frame.
    * @param source the class object for whom the message is displayed, used in
    *           the preparation of the message and title, may not be
    *           <code>null</code>
    * @param msgKey the key of the message, may not be <code>null</code> or
    *           empty.
    * @param msgParams the dynamic values to replace the arguments in message,
    *           if <code>null</code> the message will not be formatted even if
    *           it has arguments to be dynamically replaced.
    * @param titleKey the key of the title, may not be <code>null</code> or
    *           empty.
    * @param titleParams the dynamic values to replace the arguments in title,
    *           if <code>null</code> the title will not be formatted even if it
    *           has arguments to be dynamically replaced.
    */
   public void displayErrorMessage(Window parent, Class source, String msgKey, Object[] msgParams, String titleKey,
         Object[] titleParams)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null.");

      if (msgKey == null || msgKey.trim().length() == 0)
         throw new IllegalArgumentException("msgKey may not be null or empty.");

      if (titleKey == null || titleKey.trim().length() == 0)
         throw new IllegalArgumentException("titleKey may not be null or empty.");

      String msg = getResourceString(source, msgKey);
      if (msgParams != null && msgParams.length != 0)
         msg = MessageFormat.format(msg, msgParams);

      String title = getResourceString(source, titleKey);
      if (titleParams != null && titleParams.length != 0)
         title = MessageFormat.format(title, titleParams);
      
        if (StringUtils.isNotEmpty(msg)
                && (msg.contains("401") || msg.contains("Not Authenticated"))) {
            boolean hasErrors = false;
            if (ms_thisApplet != null
                    && ms_thisApplet.getParentFrame() instanceof PSDesktopExplorerWindow) {
                PSContentExplorerApplication.getBaseFrame().logout();
            } else {
                try {
                    JSObject window = JSObject.getWindow(ms_thisApplet);
                    log.debug("Deteceted 401 or Not Authenticated message, returning to login screen. Msg is: "
                            + msg);
                    window.eval("if (window.parent) {window.parent.location.href('/Rhythmyx/login')} else {window.location.href('/Rhythmyx/login')}");
                } catch (JSException e) {
                    hasErrors = true;
                    log.error("Error redirecting Rhythmyx to login screen.", e);
                }
            }
            if (!hasErrors)
                return;
        }

      ErrorDialogs.showErrorDialog(parent, msg, title, JOptionPane.ERROR_MESSAGE);
   }

   /**
    * Returns set of flagged folders, modifying this Set will not affect the
    * Applet's flagged folder set.
    *
    * @return Never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public Set getFlaggedFolderSet()
   {
      return new HashSet(ms_flaggedFolders);
   }

   /**
    * Adds or Removes a flagged folder from the Applets flagged folder set.
    *
    * @param folderid a numeric string representing the folder id, cannot be
    *           <code>null</code> or empty and must be a numeric string.
    *
    * @param add if set to <code>true</code> will add the folderid to the set
    *           else it will remove it
    */
   @SuppressWarnings("unchecked")
   public void toggleFlaggedFolder(String folderid, boolean add)
   {
      if (folderid == null || folderid.trim().length() == 0)
         throw new IllegalArgumentException("folderid cannot be null or empty.");
      try
      {
         Integer.parseInt(folderid);
      }
      catch (NumberFormatException nfe)
      {
         throw new IllegalArgumentException("folderid must be a numeric string value.");
      }
      // If folder id exists then remove it
      if (!add && ms_flaggedFolders.contains(folderid))
      {
         ms_flaggedFolders.remove(folderid);
      }
      else if (add)
      {
         // else add it
         ms_flaggedFolders.add(folderid);
      }
   }

   /**
    * Loads the set of folders flagged for publishing
    *
    * @throws IOException
    * @throws SAXException
    * @throws ParserConfigurationException
    */
   @SuppressWarnings("unchecked")
   public void loadFlaggedFoldersSet() throws IOException, SAXException, ParserConfigurationException
   {
      Set flags = new HashSet();
      final Document doc = getXMLDocument(PSContentExplorerConstants.APP_RESOURCE_FLAGGED_FOLDERS);
      final NodeList nl = doc.getElementsByTagName(PSContentExplorerConstants.ELEM_PUBLISH_FLAG);
      Element flag = null;
      String value = null;
      final int len = nl.getLength();
      for (int i = 0; i < len; i++)
      {
         flag = (Element) nl.item(i);
         value = flag.getAttribute(PSContentExplorerConstants.ATTR_VALUE);
         if (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true"))
         {
            flags.add(flag.getAttribute(PSContentExplorerConstants.ATTR_FOLDERID));
         }
      }
      ms_flaggedFolders = flags;

   }

   public PSHttpConnection getHttpConnection()
   {
      if(m_httpConnection == null){
         m_httpConnection = new PSHttpConnection(getRhythmyxCodeBase(), " ");
     }
      return m_httpConnection;
   }

   public boolean isInitialized()
   {
      return m_initialized;
   }


   /**
    * This class serves as a workaround for the Swing issue in that when we set
    * the cursor to wait cursor, move the mouse outside the applet and come back
    * it gets reset to default cursor or some vague styled one.
    * <p>
    * The logic here is very simple in which we do not depend on the internal
    * state of the cursor, rather we depend on our own flag that can be set and
    * reset in the beginning and ending of any lengthy operation. The run()
    * method of this class makes sure to set the cursor to the style of the
    * member variable settable from outside.
    * <p>
    * <em>Note: Do not use System.out.print* in this class.</em>
    */
   class CxCursor extends Thread
   {
      /**
       * Ctor. Sets the refernce to the parent fram for the cursor.
       *
       * @param frame must not be <code>null</code>
       * @throws IllegalArgumentException if the parent frame is
       *            <code>null</code>.
       */
      CxCursor(Frame frame)
      {
         if (frame == null)
            throw new IllegalArgumentException("Cursor's parent frame must not be null");
         m_frame = frame;
      }

      /**
       * Set cursor to wait cursor.
       */
      void setWaitCursor()
      {
         m_cxCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
      }

      /**
       * set cursor to default style one
       */
      void resetCursor()
      {
         m_cxCursor = Cursor.getDefaultCursor();
      }

      /**
       * Stop the thread
       */
      void terminate()
      {
         run = false;
      }

      /**
       * Sets the cursor to {@link #m_cxCursor}after every specified interval
       * {@link #REFRESH_INTERVAL}of sleep time.
       */
      @Override
      public void run()
      {
         while (run)
         {
            m_frame.setCursor(m_cxCursor);
            try
            {
               Thread.sleep(REFRESH_INTERVAL);
            }
            catch (Exception e)
            {
            }
         }
      }

      /**
       * Reference to the parent frame for which the cursor style was set,
       * initialized in the constructor, never <code>null</code> after that.
       */
      private Frame m_frame = null;

      /**
       * Cursor which is set for the Parent frame for every designated interval
       * of time. Initialized to default style. This style is what gets set by
       * the caller.
       */
      private Cursor m_cxCursor = Cursor.getDefaultCursor();

      /**
       * flag to terminate the thread.
       */
      private boolean run = true;

      /**
       * Interval between setting the cursor. User should never see wrong cursor
       * longer than this!.
       */
      static final int REFRESH_INTERVAL = 125;
   }

   /**
    * Class that repesents a splash\progress screen that appears while the CX
    * applet is loading
    */
   class SplashScreen
   {

      /**
       * Creates the splash window and adds all components to it.
       */
      private void initDialog()
      {
         if (ms_isDebug)
            log.debug("In initDialog method");
         mi_window = new JWindow();
         Container contentPane = mi_window.getContentPane();
         String imagePath= "/images/splash.gif";

         int yPos = 154;
         ImageIcon image = new ImageIcon(getClass().getResource(imagePath));

         int width = image.getIconWidth();
         int height = image.getIconHeight();

         contentPane.setLayout(null);
         contentPane.setBackground(Color.white);

         mi_window.setSize(width, height);

         JLabel labelWait = new JLabel("Please wait while loading Content Explorer...", SwingConstants.CENTER);

         labelWait.setBounds(0, yPos, width, 30);
         contentPane.add(labelWait);

         mi_msg = new JLabel("", SwingConstants.CENTER);
         mi_msg.setForeground(Color.blue);
         yPos += 25;
         mi_msg.setBounds(0, yPos, width, 30);
         contentPane.add(mi_msg);

         yPos += 25;
         mi_bar = new JProgressBar(0, mi_maxMsgCount);
         mi_bar.setValue(0);
         mi_bar.setStringPainted(true);

         mi_bar.setBounds((int) ((width - width * .5) / 2), yPos, (int) (width * .5), 20);
         contentPane.add(mi_bar);

         // add the image
         JLabel imageLabel = new JLabel(image);
         imageLabel.setOpaque(false);
         imageLabel.setBounds(0, 0, width, height);
         contentPane.add(imageLabel);

         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension size = mi_window.getSize();
         mi_window.setLocation((screenSize.width - size.width) / 2, (screenSize.height - size.height) / 2);

      }

      /**
       * Displays the splash screen only on the first loading
       */
      void show()
      {
         if (ms_isDebug)
            log.debug("In show method");
         if (mi_shown)
         {
            if (ms_isDebug)
               log.debug("Splash already shown, skipping.");
            return;
         }

         if (mi_window == null)
            initDialog();

         mi_window.setVisible(true);
         mi_shown = true;

      }

      /**
       * Disposes the splash screen and releases the native window peer.
       */
      void dispose()
      {
         if (mi_window != null)
         {
            mi_window.dispose();
            mi_msgCount = 0;
            mi_window = null;
         }

      }

      /**
       * Sets the currently displayed status message
       *
       * @param msg the message , can be <code>null</code>.
       */
      void setMessage(String msg)
      {
         if (mi_window != null && msg != null)
         {
            mi_msg.setText(msg);
            mi_bar.setValue(++mi_msgCount);
         }

      }

      /**
       * Sets the maximum number of messages that will be set in the splash
       * screen. This is used with the progress bar.
       *
       * @param count
       */
      void setMaxMessageCount(int count)
      {
         mi_maxMsgCount = count;
      }

      private JWindow mi_window;

      private boolean mi_shown;

      private JLabel mi_msg;

      private JProgressBar mi_bar;

      private int mi_maxMsgCount;

      private int mi_msgCount;

   }

   /**
    * Get the first instance of the content explorer applet that was
    * constructed, which is assumed to be the main CX applet.
    *
    * @return The first instance of the applet constructed. Never be
    *         <code>null</code> under normal conditions. May be
    *         <code>null</code> under very rare situations when the applet is
    *         not initialized successfully. If a user bookmarks a page with a
    *         different instance of the applet such as the impact analyzer or
    *         document assembly, it is possible that one of those instances
    *         could be returned, but this is not a supported use case.
    */
   public PSContentExplorerApplet getApplet()
   {
      return ms_thisApplet;
   }

   /**
    * Reference to the self correcting cursor object that is constructed during
    * initialization of the applet, never <code>null</code> after that.
    */
   private CxCursor m_cursor = null;

   /**
    * The singleton instance of the help to hold on for not garbage collecting,
    * initialized when the first time the applet is loaded and never <code>null
    * </code> or modified after that.
    */
   private PSJavaHelp ms_help = null;

   /**
    * The action manager to handle all actions (local, system and custom
    * actions), initialized in <code>init()</code> method and is not modified
    * and never <code>null</code> after that.
    */
   PSActionManager m_actManager;

   /**
    * The manager that handles loading and saving of options, initialized in
    * <code>init()</code> method and is not modified and never <code>null</code>
    * after that.
    */
   private PSOptionManager m_optionsManager;

   /**
    * The object that holds on to the loaded applets and updates the display
    * options when it is informed, initialized when the applet is loaded and
    * never <code>null</code> after that.
    */
   private OptionsUpdater ms_optChangeUpdater = null;

   /**
    * The main view panel that represents the current view of the applet,
    * initialized in the <code>initView()</code> and never <code>null</code> or
    * modified after that.
    */
   JSplitPane m_mainView;

   /**
    * The action bar that represents the menu bar of the applet, initialized in
    * the <code>initView()</code> and never <code>null</code> or modified after
    * that.
    */
   private PSActionBar m_actionBar;

   /**
    * The current view represented by this applet, initialized in <code>
    * initView()</code> method and is not modified, never <code>null</code> or
    * empty after that.
    */
   String m_view;

   /**
    * The flag to indicate debug mode of the applet. By default it is turned
    * off, and can be turned on by supplying parameter <code>PARAM_DEBUG</code>
    * with value 'TRUE'. In debug mode it logs the stack trace in case of an
    * exception with detailed message and the assumed default values for the
    * parameters that are not supplied.
    */
   private boolean ms_isDebug = true;

   /**
    * The flag is used to indicate if the managed nav is used by the Rhythmyx
    * server. It is set by initializing the applet. Default to
    * <code>false</code>.
    */
   private boolean ms_isManagedNavUsed = false;

   /**
    * Initialized in the init, never <code>null</code> after that and invariant.
    */
   private PSI18NTranslationKeyValues ms_i18nKeyValue = null;

   /**
    * The menu bar that represents the global menu of the applet, initialized in
    * <code>initView()</code> and never <code>null</code> or modified after
    * that.
    */
   private PSContentExplorerMenuBar m_globalMenuBar;

   /**
    * Set of all folders with the publish folder flag set
    */
   private Set ms_flaggedFolders = new HashSet();

   /**
    * Object representing current user's state inofrmation. Initialized on
    * applet initialization. Povides easy access to all required user state
    * variables such as user name, community etc., never <code>null</code> after
    * applet is successfully initialized.
    */
   PSUserInfo ms_userInfo;

   /**
    * SessionKeeper object that is initialized during applet start and never
    * <code>null</code> after that.
    */
   PSCESessionManager m_sessionCheck = null;

   /**
    * The parent frame of this applet, initialized when this applet is
    * initialized and never <code>null</code> or modified after that.
    */
   private Frame m_parentFrame = null;

   /**
    * Operating system name. Initialized upon applet loading, never empty after
    * that.
    */
   public static String m_osName = "";

   /**
    * RestrictSearchFieldsToUserCommunity property, default is <code>true</code>
    */
   private boolean ms_restrictContent = false;

   /**
    * The splash screen\progress screen used while waiting for cx to load.
    */
   SplashScreen m_splash;

   /**
    * static reference to the instance of this applet. Never <code>null</code>
    * after successful initialization of the applet.
    */
   private PSContentExplorerApplet ms_thisApplet = null;

   /**
    * Storage for the column width options, need to keep in memory since the
    * columns when refreshed lose the widths.
    */
   private PSColumnWidthsOption m_cwo = null;

   /**
    * Storage for the display format options, need to keep in memory since the
    * folders when refreshed lose the display format.
    */
   private PSDisplayFormatOption m_dfo = null;

   /**
    * Storage for the initial selection path stored as user options
    */
   String m_selPath = null;

   /**
    * Register key event post processor to handle special key strokes to
    * simulate "browser behavior".
    */
   void registerKeyEventPostProcessor()
   {
      // set key event post processor to handle special key strokes to
      // simulate "browser behavior".
      if (m_keyEventPostProcessor == null)
      {
         KeyboardFocusManager focusMgr = KeyboardFocusManager.getCurrentKeyboardFocusManager();
         m_keyEventPostProcessor = new PSKeyEventPostProcessor(this);
         focusMgr.addKeyEventPostProcessor(m_keyEventPostProcessor);
      }
   }

   /**
    * Unregister the key event post processor if it is registered by
    * {@link #registerKeyEventPostProcessor()}.
    */
   private void unregisterKeyEventPostProcessor()
   {
      if (m_keyEventPostProcessor != null)
      {
         KeyboardFocusManager focusMgr = KeyboardFocusManager.getCurrentKeyboardFocusManager();
         focusMgr.removeKeyEventPostProcessor(m_keyEventPostProcessor);
         m_keyEventPostProcessor = null;
      }
   }

   
   public  boolean isApplication()
   {
      return isApplication;
   }

   public  void setIsApplication(boolean isApplication)
   {
      this.isApplication = isApplication;
   }

   public static ConcurrentMap<String, PSRelationshipInfo> getRelMap()
   {
      return REL_MAP;
   }


   public static void setRelMap(ConcurrentMap<String, PSRelationshipInfo> relMap)
   {
      PSContentExplorerApplet.REL_MAP = relMap;
   }

   /**
    * The key event post processor, used to handle special key strokes to
    * simulate "browser behavior". Set only by both
    * {@link #registerKeyEventPostProcessor()}and
    * {@link #unregisterKeyEventPostProcessor()}
    */
   private PSKeyEventPostProcessor m_keyEventPostProcessor = null;


   /**
    * This is used to (lazily) cache the catalogged searchable fields. It maps
    * cataloger flag of the applet (as <code>String</code> object) to its
    * corresponding searchable fields (as
    * <code>PSContentEditorFieldCataloger</code> object). It may be
    * <code>null</code> if {@link PSContentExplorerConstants#PARAM_CACHE_SEARCHABLE_FIELDS} parameter of
    * the applet is <code>None</code>.
    *
    * @see PSContentExplorerConstants#PARAM_CACHE_SEARCHABLE_FIELDS
    */
   private Map m_searchableFields = null;

   /**
    * Just like {@link #m_searchableFields}, except this is used when cached per
    * JVM. It is never <code>null</code>, but may be empty.
    * <p>
    * Note, this cache will be used for all applet instances.
    *
    * @see PSContentExplorerConstants#PARAM_CACHE_SEARCHABLE_FIELDS
    */
   private Map ms_searchableFieldsPerJVM = new HashMap();



   /**
    * The flag is used to indicate if AjaxSwing is being used to access the
    * applet.
    */
   private boolean ms_useAjaxSwing = false;

   /**
    * This is used to handle special key strokes to simulate "browser behavior"
    */
   public class PSKeyEventPostProcessor implements KeyEventPostProcessor
   {
      /**
       * Constructs the object.
       *
       * @param applet The current applet object, may not be <code>null</code>.
       */
      public PSKeyEventPostProcessor(PSContentExplorerApplet applet)
      {
         if (applet == null)
            throw new IllegalArgumentException("applet may not be null");

         m_applet = applet;
         if (m_applet.m_view.equals(PSUiMode.TYPE_VIEW_CX))
            m_isCXView = true;
      }

      // implements interface method
      @Override
      public boolean postProcessKeyEvent(KeyEvent e)
      {
         // only handle key released event
         if (e.getID() != KeyEvent.KEY_RELEASED)
            return false;

         boolean isConsumed = e.isConsumed();
         if (isConsumed)
            return false;

         // call JavaScript if the key is one of the target keys
         try
         {
            if (m_isCXView)
            {
               if (e.getKeyCode() == KeyEvent.VK_F6)
               {
                  if (dceHeader!=null)
                  {
                     dceHeader.requestFocusInWindow();
                  } else 
                     focusBannerFrame();
                  
                  isConsumed = true;
               }
            }
         }
         catch (Throwable ex)
         {
            log.error("Caught exception: " + ex.toString());
            ex.printStackTrace();
         }

         return isConsumed;
      }

      /**
       * Call JavaScripte to set focus to the banner frame
       */
      private void focusBannerFrame()
      {
         JSObject window = JSObject.getWindow(m_applet);
         
         
         
         Object[] args = new Object[0];
         window.call("focusBannerFrame", args);
      }

      /**
       * The current applet object, init by ctor, never <code>null</code> after
       * that.
       */
      private PSContentExplorerApplet m_applet;

      /**
       * <code>true</code> if the applet is used by Content Explorer. Set by
       * constructor, never modified after that.
       */
      private boolean m_isCXView = false;

}
   
   public WebEngine getEngine()
   {
      return webEngine;
   }

   public void setEngine(WebEngine webEngine)
   {
      this.webEngine = webEngine;
   }



      
   
}
