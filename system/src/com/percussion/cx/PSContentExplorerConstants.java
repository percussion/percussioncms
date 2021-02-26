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

package com.percussion.cx;

import java.util.HashMap;
import java.util.Map;

public class PSContentExplorerConstants
{

   /**
    * Applets width.
    */
   public static final int APPLET_WIDTH = 960;
   /**
    * Applets height.
    */
   public static final int APPLET_HEIGHT = 700;
   public static final String POPUP_TITLE = "POPUP_TITLE";
   /* The parameter that defines a url to get navigational tree xml for 'CX' or
    * 'IA' views. If this is not supplied for 'CX' view, loads the xml as
    * defined by the file <code>CE_NAV_XML</code>, for IA view raises an
    * exception.
    */
   public static final String PARAM_NAV_URL = "NAV_URL";
   /**
    * The suffix to use with icon constants to put or get expanded version of an
    * icon. The constant used for an icon of any node is the node type. See
    * {@link com.percussion.cx.objectstore#PSNode}for more information on nodes.
    */
   public static final String EXP_SUFFIX = "_EXPAND";
   /**
    * The parameter to define the applet is in debug mode or not. The allowed
    * values are 'TRUE' or 'FALSE'. The default is 'FALSE'.
    */
   public static final String PARAM_DEBUG = "DEBUG";
   /**
    * The name of the parameter that gives the
    * RestrictSearchFieldsToUserCommunity property value in server.properties.
    */
   public static final String PARAM_RESTRICTSEARCHFIELDSTOUSERCOMMUNITY = "RestrictSearchFieldsToUserCommunity";
   /**
    * The name of the parameter that gives the
    * <code>CacheSearchableFields</code> property value in server.proerpties.
    * <p>
    * The possible values are:
    * <TABLE BORDER="1">
    * <TR>
    * <TH>Property Value</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>CachePerJVM</TD>
    * <TD>The catalogged searchable fields will be cached per JVM. All applet
    * instances will share the same cache, hence the cache will not be reloaded
    * when the applets that are started from within "Content tab" and Active
    * Assembly page. This is the default behavior if the property does not exist
    * in the server.properties.</TD>
    * </TR>
    * <TR>
    * <TD>CachePerApplet</TD>
    * <TD>The catalogged searchable fields will be cached per applet instance.</TD>
    * </TR>
    * <TR>
    * <TD>None</TD>
    * <TD>Do not cache the searchable fields.</TD>
    * </TR>
    * </TABLE>
    */
   public static final String PARAM_CACHE_SEARCHABLE_FIELDS = "CacheSearchableFieldsInApplet";
   /**
    * The name of the parameter that indicate whether the managed navigation is
    * used or not. The value of the parameter is <code>yes</code> if the managed
    * navigation is used by the server; otherwise the managed navigation is not
    * used.
    */
   public static final String PARAM_IS_MANAGEDNAV_USED = "isManagedNavUsed";
   /**
    * The name of the parameter that indicate whether to use the session keeper
    * functionality. This makes the appet ping the server to keep the session
    * alive while the applet is running. If the new session warning and logout
    * functionality this may not be wanted as it may be required for this
    * functionality to log out the user
    */
   public static final String PARAM_USE_SESSION_KEEPER = "useSessionKeeper";
   /**
    * The parameter that defines the view that this instance of the applet
    * should represent. The value must be one of the <code>
    * PSUiMode.TYPE_VIEW_xxx</code> values. If this parameter is not suppled, it
    * assumes 'CX' view.
    */
   public static final String PARAM_VIEW = "VIEW";
   /**
    * The parameter that defines a url to get menu xml. If this is not supplied
    * it assumes defaults for each view.
    */
   public static final String PARAM_MENU_URL = "MENU_URL";
   /**
    * The parameter that defines a url to get all possible relations an item can
    * have. If not supplied uses
    */
   public static final String PARAM_RS_URL = "RELATIONS_URL";
   /**
    * The parameter that defines the relationship to be selected by default in
    * 'DT' view when the applet is loaded.
    */
   public static final String PARAM_INITIAL_RS = "INITIAL_RS";
   /**
    * The parameter that defines a url to get ancestors of an item for a
    * particular relationship.
    */
   public static final String PARAM_ANC_URL = "ANCESTOR_URL";
   /**
    * The parameter that defines a url to get descendants of an item for a
    * particular relationship.
    */
   public static final String PARAM_DESC_URL = "DESCENDANT_URL";
   /**
    * The parameter that defines a url to get options.
    */
   public static final String PARAM_OPTIONS_URL = "OPTIONS_URL";
   /**
    * The parameter that defines a url to get options.
    */
   public static final String OPTIONS_URL = "../sys_cxSupport/options.xml";
   /**
    * The parameter that defines the 'CONTENTID' of the item
    */
   public static final String PARAM_CONTENTID = "CONTENTID";
   /**
    * The parameter that defines the 'REVISIONID' of the item.
    */
   public static final String PARAM_REVISIONID = "REVISIONID";
   /**
    * The parameter that defines the 'TITLE' of the item.
    */
   public static final String PARAM_ITEM_TITLE = "TITLE";
   /**
    * The parameter that defines the 'TITLE' of the item.
    */
   public static final String PARAM_USE_AJAX_SWING = "USE_AJAX_SWING";
   /**
    * RELATIONSHIP_NAME property that is set based on user selection made in the
    * dropdown box of the dependency viewer window, defaults to empty string.
    */
   public static final String PARAM_RELATIONSHIP_NAME = "RELATIONSHIP_NAME";
   /**
    * The parameter that provides the path to select by default when the applet
    * is loaded.
    */
   public static final String PARAM_CX_PATH = "sys_cxpath";
   /**
    * The default file to load the menu xml for 'CE' view if the <code>
    * PARAM_MENU_URL</code> is not supplied.
    */
   static final String CX_MENU_XML = "ContentExplorerMenu.xml";
   /**
    * The default file to load the menu xml for 'IA' view if the <code>
    * PARAM_MENU_URL</code> is not supplied.
    */
   static final String IA_MENU_XML = "ItemAssemblyMenu.xml";
   /**
    * The default file to load the menu xml for 'DT' view if the <code>
    * PARAM_MENU_URL</code> is not supplied.
    */
   static final String DT_MENU_XML = "DependencyTreeMenu.xml";
   /**
    * The default file to load the navigational xml for 'CX' view if the <code>
    * PARAM_NAV_URL</code> is not supplied.
    */
   static final String CX_NAV_XML = "ContentExplorer.xml";
   /**
    * The default file to load the relations for 'DT' view if the <code>
    * PARAM_RS_URL</code> is not supplied.
    */
   static final String RELATIONS_XML = "Relations.xml";
   /**
    * The default file to load the options from if the <code>PARAM_OPTIONS_URL
    * </code> is not supplied.
    */
   static final String OPTIONS_XML = "ContentExplorerOptions.xml";
   /**
    * The constant used as key to store the display options in UIManager
    * defaults.
    */
   public static final String DISPLAY_OPTIONS = "DisplayOptions";
   /**
    * The parameter which defines the helpset url to be used to display help for
    * this application using JavaHelp viewer.
    */
   public static final String PARAM_HELPSET = "helpset_file";
   /**
    * The constant for the parameter that specifies if selective view refresh is
    * enabled. Value is "yes" if enabled, "no" if not.
    */
   public static final String PARAM_SELECTIVE_VIEW_REFRESH = "sys_useSelectiveViewRefresh";
   /**
    * The constant for the parameter to define the default selection path for CX
    * or IA view. This path represents internal names of nodes.
    */
   public static final String PARAM_CXINTERNALPATH = "sys_cxinternalpath";
   /**
    * The constant for the parameter to define the default selection path for CX
    * or IA view. This path represents display names of nodes.
    */
   static final String PARAM_CXDISPLAYPATH = "sys_cxdisplaypath";
   /**
    * The application resource used to retreive the list of folders with the
    * publish flag set
    */
   static final String APP_RESOURCE_FLAGGED_FOLDERS = "../sys_psxObjectSupport/getAllFolderPublishFlags.xml";
   /**
    * The constant for the parameter that specifies if an external search engine
    * is available. Value is "yes" if available, "no" if not.
    */
   public static final String PARAM_SEARCH_ENGINE_AVAILABLE = "sys_isSearchEngineAvailable";
   /**
    * The prefix for path variable macro.
    */
   static final String PATH_PREFIX = "$";
   /**
    * The following represents different variable names for known paths
    * correspondingly.
    */
   public static final String PARAM_INBOX_CONST = "INBOX";
   public static final String PARAM_OUTBOX_CONST = "OUTBOX";
   public static final String PARAM_RECENT_CONST = "RECENT";
   public static final String PARAM_NEWSEARCH_CONST = "NEWSEARCH";
   /**
    * The following represents the paths of specific nodes in navigation tree of
    * content explorer view.
    */
   public static final String PATH_MYCONTENT = "//Views//MyContent/";
   public static final String PARAM_PATH_INBOX = PATH_MYCONTENT + "Inbox";
   public static final String PARAM_PATH_OUTBOX = PATH_MYCONTENT + "Outbox";
   public static final String PARAM_PATH_RECENT = PATH_MYCONTENT + "Recent";
   public static final String PARAM_PATH_NEWSEARCH = "//SearchResults/NewSearch";
   public static final String PARAM_PATH_FOLDERS = "//Folders";
   // Default path is set to New Search
   public static final String PARAM_PATH_DEFAULT = PARAM_PATH_RECENT;
   // Indicates if splash screen should be shown
   public static final String PARAM_SHOW_SPLASH = "SHOW_SPLASH";
   /**
    * The path to the parent item in 'IA' view.
    */
   static final String PARAM_PATH_PARENT_ITEM = "//Parent";
   /**
    * The constant to identify a component as heading.
    */
   public static final String HEADING_COMP_NAME = "Heading";
   /**
    * static constants used for saving and restoring options
    */
   static final String EXPANDED_OPTION = "Expanded";
   static final String DISPLAY_FORMAT_OPTION = "DisplayFormat";
   static final String COlUMN_WIDTHS_OPTION = "ColumnWidths";
   static final String SELECTED_PATH_OPTION = "SelectedPath";
   static final String DIVIDER_LOCATION_OPTION = "DividerLocation";
   /**
    * XML Element representing a folder publish flag
    */
   static final String ELEM_PUBLISH_FLAG = "PublishFlag";
   /**
    * XML Attribute representing a folderid
    */
   static final String ATTR_FOLDERID = "folderid";
   /**
    * XML Attribute representing a value
    */
   static final String ATTR_VALUE = "value";
   public static final int CONTENT_EXPLORER = 1;
   public static final int INNER_APPLET= 2;
   // String constant for "yes"
   static final String CONST_YES = "yes";
   /**
    * The default name for the Workflow action menu.
    */
   static final String ACTION_WORKFLOW = "Workflow";
   /**
    * The default url for the Workflow action menu.
    */
   public static final String WORKFLOW_MENU_ACTION_URL = "../sys_cxSupport/wfactionset.xml";
   /**
    * String constant for the action parameter name "sys_command"
    */
   static final String ACTION_PARAM_SYS_COMAND = "sys_command";
   /**
    * String constant for the default value for action property value for
    * ACTION_PROPERTY_TARGET
    */
   static final String ACTION_TARGET_DEFAULT = "_new";
   /**
    * String constant for the default value for action property value for
    * ACTION_PROPERTY_TARGET_STYLE
    */
   public static final String ACTION_TARGET_STYLE_DEFAULT = "";
   /**
    * String constant for the default value for action property value for
    * ACTION_PROPERTY_LAUNCHES_WINDOW
    */
   public static final String ACTION_LAUNCHES_WINDOW_DEFAULT = CONST_YES;
   /**
    * Integer constant for the maximum likely length of the url that can be
    * passed to ShowWindow function.
    */
   public static final int MAX_SHOWWINDOW_URL_LENGTH = 256;
   /**
    * Integer constant for the maximum likely length for url in get method on
    * most of the browsers.
    */
   public static final int MAX_GET_URL_LENGTH = 2048;
   /**
    * Name of html paramter to save the server action urlin to session.
    */
   public static final String SYS_SERVERACTIONURL = "sys_serveractionurl";
   /**
    * Contstant for the name of resource to save the server action url. This
    * resource has a pre-exit which sets a session variable called
    * SYS_SERVERACTIONURL with the value from the htmlparameter
    * SYS_SERVERACTIONURL.
    */
   public static final String SAVE_SERVERACTIONURL = "../sys_cxSupport/saveserveractionurl.html";
   /**
    * Default Actions for various types of nodes. These are invoked on mouse
    * double click. The default actions are assigned in the order of priority so
    * that first action that is found in the possible actions for the mode and
    * selection is executed. For example, for a content item, we need to specify
    * two default actions "Edit" and "View" so that if user cannot edit he may
    * be able to view.
    */
   public final static Map<String, String[]> ms_NodeDefaultActionMap = new HashMap<>();

}
