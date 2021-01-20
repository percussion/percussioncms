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

public interface IPSConstants
{
   /**
    * The following describes the constants for the actions handled by client.
    */
   public static final String ACTION_CREATE_FOLDER = "Create_New_Folder";
   public static final String ACTION_EDIT_FOLDER = "Edit_Folder";
   public static final String ACTION_DELETE = "Delete";
   public static final String ACTION_FORCE_DELETE = "Force_Delete";
   public static final String ACTION_DELETE_SEARCH = "Delete_Search";
   public static final String ACTION_PURGE = "Purge";
   public static final String ACTION_PURGEALL = "PurgeAll";
   public static final String ACTION_PURGE_NAV = "PurgeNav";
   public static final String ACTION_SEARCH = "Search";
   public static final String ACTION_SLOT_SEARCH = "Slot_Search";
   public static final String ACTION_LINK_TO_SLOT = "Link_To_Slot";
   public static final String ACTION_EDIT_SEARCH = "Edit_Query";
   public static final String ACTION_SAVEAS = "Save_As";
   public static final String ACTION_CHANGE_DF = "Change_Display_Format";
   public static final String ACTION_REFRESH = "Refresh";
   public static final String ACTION_OPTIONS = "Options";
   public static final String ACTION_CXT_HELP = "Context_Help";
   public static final String ACTION_TOP_HELP = "topMenuHelp";
   public static final String ACTION_ABOUT = "About";
   public static final String ACTION_COPY = "Copy";
   public static final String ACTION_COPY_URL_TO_CLIPBOARD = "Copy_URL_to_Clipboard";
   public static final String ACTION_MOVE = "Move";
   public static final String ACTION_FORCE_MOVE = "Force_Move";
   public static final String ACTION_PASTE = "Paste";
   public static final String ACTION_PASTE_LINK = "Paste_As_Link";
   public static final String ACTION_PASTE_NEW_COPY = "Paste_As_New_Copy";
   public static final String ACTION_PASTE_NEW_TRNSL = "Paste_As_New_Translation";
   public static final String ACTION_PASTE_DF = "Paste_Display_Format";
   public static final String ACTION_ARRANGE_REMOVE = "Arrange_Remove";
   public static final String ACTION_MOVE_UPLEFT = "Arrange_MoveUpLeft";
   public static final String ACTION_MOVE_DOWNRT = "Arrange_MoveDownRight";
   public static final String ACTION_OPEN = "Open";
   public static final String ACTION_EDIT = "Edit";
   public static final String ACTION_VIEW_CONTENT = "View_Content";
   public static final String ACTION_FORCE_CHECKIN = "forcecheckin";
   public static final String ACTION_CHANGE_VARIANT = "Change_Variant";
   public static final String ACTION_PASTE_LINK_TO_SLOT = "Paste_As_Link_To_Slot";
   public static final String ACTION_PASTE_LINK_SEARCH_TO_SLOT =
      "Paste_As_Link_Search_To_Slot";
   public static final String ACTION_MOVE_TO_SLOT = "Move_To_Slot";
   public static final String ACTION_COPY_ACL_TO_SUBFOLDERS =
      "Copy_ACL_To_Subfolders";
   
   /**
    * An action taken against a nav folder. Performs a search limited to the
    * target folder and below.
    */
   public static final String ACTION_SEARCH_WITHIN_FOLDER = 
      "Search_Within_Folder";
   
   /**
    * An action taken on a folder returned in a search result. It will open
    * the nav tree to match the current folder and make that folder active.
    */
   public static final String ACTION_OPEN_FOLDER_REF = "Open_Folder_Ref";
   public static final String ACTION_NO_ENTRIES = "No_Entries";

   /**
    * The following describes the constants for various properties of cms
    * components, for example these can be properties of a folder or an item or
    * an action.
    */
   public static final String PROPERTY_CONTENTID = "sys_contentid";
   public static final String PROPERTY_CONTENTTYPEID = "sys_contenttypeid";
   public static final String PROPERTY_SORTRANK = "sys_sortrank";
   public static final String PROPERTY_OBJECTTYPE = "sys_objecttype";
   public static final String PROPERTY_CHECKOUTSTATUS = "sys_checkoutstatus";
   public static final String PROPERTY_CHECKOUTUSER = "sys_contentcheckoutusername";
   public static final String PROPERTY_ASSIGNMENTTYPE = "sys_assignmenttype";
   public static final String PROPERTY_ASSIGNMENTTYPEID = "sys_assignmenttypeid";
   public static final String PROPERTY_PUBLISHABLETYPE = "sys_publishabletype";
   public static final String PROPERTY_WORKFLOWID = "sys_workflowid";
   public static final String PROPERTY_DISPLAYFORMATID = "sys_displayformat";
   public static final String PROPERTY_COMMUNITY = "sys_community";
   public static final String PROPERTY_DFCATEGORY = "sys_dfcategory";
   public static final String PROPERTY_SEARCHID = "sys_search";
   public static final String PROPERTY_REVISION = "sys_revision";
   public static final String PROPERTY_TIPREVISION = "sys_tiprevision";
   public static final String PROPERTY_VARIANTID = "sys_variantid";
   public static final String PROPERTY_SLOTID = "sys_slotid";
   public static final String PROPERTY_ALLOWEDCONTENT = "sys_allowedcontent";
   public static final String PROPERTY_RELATIONSHIPID = "sys_relationshipid";
   public static final String PROPERTY_RELATIONSHIP = "relationship";
   public static final String PROPERTY_SUP_MULTI_SELECT = "SupportsMultiSelect";
   public static final String PROPERTY_MODE = "sys_mode";
   public static final String PROPERTY_UICONTEXT = "sys_uicontext";
   public static final String PROPERTY_LANG = "sys_lang";
   public static final String PROPERTY_USERNAME = "sys_username";
   public static final String PROPERTY_HIDDEN = "hidden";

   /**
    * Property used to indicate if a menu item is checked.
    */
   public static final String PROPERTY_MENU_ITEM_CHECKED = "menuItemChecked";
   public static final String PROPERTY_TRUE = "true";
   public static final String PROPERTY_FALSE = "false";

   /**
    * String constant for the action property name of the target browser window
    */
   public static final String PROPERTY_TARGET = "target";

   /**
    * String constant for the action property name of the target browser window
    * style
    */
   public static final String PROPERTY_TARGET_STYLE = "targetStyle";

   /**
    * String constant for the action property name to indicate if the action
    * needs to launch a window
    */
   public static final String PROPERTY_LAUNCHES_WINDOW = "launchesWindow";

   /**
    * String constant for storing the slot path for slot searches.
    */
   public static final String PROPERTY_SLOT_NODE = "slotNode";
   
   /**
    * The name of the node property used to store the folder path if a search 
    * is to be folder limited. Must have a non-empty value to be used.
    */
   public static final String PROPERTY_FOLDER_PATH = "folderPath";   
}
