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

import com.percussion.security.PSSecurityProvider;

import java.io.File;
/**
 * This file contains various constants that are used across 2 or more
 * editors. It serves as a registry. This includes such things as:
 * <ul>
 * <li>The names of xml document elements and attributes</li>
 * <li>The names of table columns for system tables</li>
 * <li>The names of system fields</li>
 * </ul>
 */
public interface IPSConstants
{
   /**
    * This is part of the primary key for the main content item table. Never
    * empty.
    */
   public static final String ITEM_PKEY_CONTENTID = "CONTENTID";

   /**
    * This is part of the primary key for the main content item table. Never
    * empty.
    */
   public static final String ITEM_PKEY_REVISIONID = "REVISIONID";

   /**
    * This is the primary key for all child tables. Never empty.
    */
   public static final String CHILD_ITEM_PKEY = "SYSID";

   /**
    * This is the foreign key to the parent for any table that is a child of a
    * child. Never empty.
    */
   public static final String CHILD_OF_CHILD_PKEY = "PARENTID";

   /**
    * The (optional) column that stores the list position an item within all
    * the items for the contentid.
    */
   public static final String CHILD_SORT_KEY = "SORTRANK";

   /**
    * The relationship main table name.
    */
   public static final String PSX_RELATIONSHIPS = "PSX_OBJECTRELATIONSHIP";

   /**
    * The relationship configuration name table.
    */
   public static final String PSX_RELATIONSHIPCONFIGNAME = "PSX_RELATIONSHIPCONFIGNAME";

   /**
    * The column name for the relationship tables unique id column.
    */
   public static final String RS_RID = "RID";

   /**
    * The relationship properties table name.
    */
   public static final String PSX_RELATIONSHIPPROPERTIES =
      "PSX_OBJECTRELATIONSHIPPROP";

   /**
    * The column name for the relationship properties table property name column.
    */
   public static final String RS_PROPERTYNAME = "PROPERTYNAME";

   /**
    * The column name for the relationship properties table property value column.
    */
   public static final String RS_PROPERTYVALUE = "PROPERTYVALUE";

   /**
    * The HTML parameter name that specifies the submit name for the requested
    * binary field.
    */
   public static final String SUBMITNAME_PARAM_NAME = "sys_submitname";

   /**
    * This is the last modifier column of the main content item table. Never
    * empty.
    */
   public static final String ITEM_CONTENTLASTMODIFIER =
      "CONTENTLASTMODIFIER";

   /**
    * This is the last modified date column of the main content item table.
    * Never empty.
    */
   public static final String ITEM_CONTENTLASTMODIFIEDDATE =
      "CONTENTLASTMODIFIEDDATE";

   /**
    * This is the edit revision column of the main content item table. Never
    * empty.
    */
   public static final String ITEM_EDITREVISION =
      "EDITREVISION";

   /**
    * This is the tip revision column of the main content item table. Never
    * empty.
    */
   public static final String ITEM_TIPREVISION =
      "TIPREVISION";

   /**
    * This is the current revision column of the main content item table. Never
    * empty.
    */
   public static final String ITEM_CURRENTREVISION =
      "CURRENTREVISION";

   /**
    * This is the content state id column of the main content item
    * table. Never empty.
    */
   public static final String ITEM_CONTENTSTATEID = "CONTENTSTATEID";

   /**
    * This is the author column of the main content item
    * table. Never empty.
    */
   public static final String ITEM_CONTENTCREATEDBY = "CONTENTCREATEDBY";

   /**
    * This is the checked out username of the main content item
    * table. Never empty.
    */
   public static final String ITEM_CONTENTCHECKOUTUSERNAME =
      "CONTENTCHECKOUTUSERNAME";

   /**
    * This is the title column of the main content item table.
    * May be empty;
    */
   public static final String ITEM_TITLE = "TITLE";

   /* Init params have names of the form 'com.percussion.<name>'. */
   /**
    * The name of the InitParam in the system def that contains the name of
    * the control to use when a hidden control is needed. The specified
    * control can be overridden by the end purchaser. Never empty.
    */
   public static final String  HIDDEN_CONTROL_PARAM_NAME =
         "com.percussion.hidden_control_name";

   /**
    * Name of the main content item table.
    */
   public static final String CONTENT_STATUS_TABLE = "CONTENTSTATUS";

   /**
    * Name of the column in the related content table that identifies the
    * content id to which an item is related.
    */
   public static final String RELATED_CONTENTID_COLUMN = "ITEMCONTENTID";

   /**
    * Name of the table that holds the content variant registrations.
    */
   public static final String CONTENT_VARIANTS_TABLE = "PSX_TEMPLATE";

   /**
    * Name of the column in the contentstatus table that identifies the
    * content type of that row.
    */
   public static final String CONTENTTYPEID_COLUMN = "CONTENTTYPEID";

   /**
    * Name of the column in the content variants table that contains the
    * id of that variant.
    */
   public static final String VARIANTID_COLUMN = "TEMPLATE_ID";

   /**
    * Name of the column in the content variants table that contains the
    * url used to query a new editor page.
    */
   public static final String ASSEMBLYURL_COLUMN = "ASSEMBLYURL";

   /**
    * Name of the table that holds the context variables
    */
   public static final String RXASSEMBLERPROPERTIES_TABLE = "RXASSEMBLERPROPERTIES";

   /**
    * Name of the column in the ASSEMBLERPROPERTIES table that holds a value of
    * the context variable.
    */
   public static final String PROPERTYVALUE_COLUMN = "PROPERTYVALUE";

   /**
    * The name of the (hidden) Rx application that contains the resources
    * used by the crow editors.
    */
   public static final String EDITOR_SUPPORT_APPNAME = "sys_ceSupport";

   /** The base URL for the lookup application if the choices are global */
   public static final String GLOBAL_LOOKUP = EDITOR_SUPPORT_APPNAME + "/lookup";

   /*
    * Value used for transition ID to indicate that a check in or check out was
    * performed (They don't have an entry in the TRANSITIONS table. Set in
    * PSExitPerformTransition. Used by PSExitUpdateHistory).
    */
    public static final int TRANSITIONID_CHECKINOUT = 0;

   /*
    * Value used for transition ID to indicate that there was no error, but  no
    * action was taken. This is used when a user attempts to check out a
    * document that is already checked out. Set in PSExitPerformTransition
    * Used by PSExitUpdateHistory.
    */
    public static final int TRANSITIONID_NO_ACTION_TAKEN = -1;

   /**
    * Revision value used when there is no corresponding actual revision.
    * Used for the edit revison of a content item that is not checked out, or
    * for the tip revision before it is given a meaninful value.
    */
    public static final int NO_CORRESPONDING_REVISION_VALUE = -1;

   /**
    * Workflow id used when there is no workflow id exist for an content item.
    * For example, if the object is not workflowable, its workflow id should
    * be this value.
    */
    public static final int INVALID_WORKFLOW_ID = -1;

   /**
    * This is the value text to use for document attributes that have a boolean
    * value of true.
    */
   public static final String BOOLEAN_TRUE = "yes";

   /**
    * This is the value text to use for document attributes that have a boolean
    * value of false.
    */
   public static final String BOOLEAN_FALSE = "no";

   /**
    * The name of the default view to use if one has not been specified.
    * Currently this is the <code>sys_Default</code> view.  This is the main
    * content editor view, includes all fields, and is linked to a stylesheet
    * through the system def that displays the full CE UI.
    */
   public static final String DEFAULT_VIEW_NAME = "sys_Default";

   /**
    * The name of the system view that includes all fields, but is linked
    * through the system def with a stylesheet that only displays the the
    * fields, and no other parts of the CE UI.
    */
   public static final String SYS_ALL_VIEW_NAME = "sys_All";

   /**
    * The name of the system view that includes all content fields.
    */
   public static final String SYS_CONTENT_VIEW_NAME = "sys_Content";

   /**
    * The name of the system view that includes all meta data (system) fields.
    */
   public static final String SYS_ITEM_META_VIEW_NAME = "sys_ItemMeta";

   /**
    * The name of the system view that includes a single field.  The format is
    * <code>sys_SingleField:<fieldName></code> where
    * <code>fieldName</code> is the name of the single field.
    */
   public static final String SYS_SINGLE_FIELD_VIEW_NAME = "sys_SingleField:";
   
   /**
    * The name of the system view that includes a list of hidden fields. The
    * displayed fields are all fields (from {@link #SYS_ALL_VIEW_NAME}) minus
    * the fields specified by this view. The list of hidden fields are comma
    * delimited, such as, "sys_HiddenFields:XXX,YYY,ZZZ". The list of fields
    * may be empty, in this case, all fields (from {@link #SYS_ALL_VIEW_NAME})
    * will be displayed.
    */
   public static final String SYS_HIDDEN_FIELDS_VIEW_NAME = "sys_HiddenFields:";
   
   /**
    * Name of the system field that denotes the relevancy ranking of a search
    * result, currently "sys_relevancy".
    */
   public static final String SYS_RELEVANCY = "sys_relevancy";
   
   /**
    * The name of the system field that acts as a flag to indicate that
    * a folder should be published with a site.(Site folder Publishing)
    */
   public static final String SYS_PUBLISH_FOLDER_WITH_SITE = 
      "sys_publishFolderWithSite";
   
   /**
    * The name of query attribute which determines how SFP should handle
    * publish flagged site folders.
    */
   public static final String INCLUDE_FOLDERS = 
      "IncludeFolders";
   
   /**
    * Name of the system field that denotes the current revision of an item, 
    * currently "sys_currentrevision".
    */
   public static final String SYS_CURRENTREVISION = "sys_currentrevision";

   /* The following contstants are temporary. Most of them are the same as
      constants in com.percussion.workflow.PSWorkFlowUtils. A couple of them
      define constants specified in rxworkflow.properties. We need to come up
      with a pattern that allows the sharing of constants across
      independent sub-systems. I don't want the server to have a dependency on
      the workflow system. */
   public static final String CHECKINOUT_CONDITION_IGNORE = "ignore";
   public static final String CHECKINOUT_CONDITION_CHECKIN = "checkin";
   public static final String CHECKINOUT_CONDITION_CHECKOUT = "checkout";
   public static final String DEFAULT_ACTION_TRIGGER_NAME = "WFAction";
   public static final String TRIGGER_CHECKOUT = "checkout";
   public static final String TRIGGER_CHECKIN = "checkin";
   public static final String TRIGGER_FORCE_CHECKIN = "forcecheckin";
   public static final int ASSIGNMENT_TYPE_NOT_IN_WORKFLOW = 0;
   public static final int ASSIGNMENT_TYPE_NONE = 1;
   public static final int ASSIGNMENT_TYPE_READER = 2;
   public static final int ASSIGNMENT_TYPE_ASSIGNEE = 3;
   public static final int ASSIGNMENT_TYPE_ADMIN = 4;
   public static String DEFAULT_NEWSTATEID_NAME = "newstateid";
   public static String WF_ACTION_PERFORMED = "WFActionPerformed";

   /**
    * Checkout status indicating it is not checked out to any body.
    */
   public static String CHECKOUT_STATUS_NOBODY = "Checked In";

   /**
    * Checkout status indicating it is checked out to the current user.
    */
   public static String CHECKOUT_STATUS_MYSELF = "Checked Out by Me";

   /**
    * Checkout status indicating it is checked out to somebody other than the 
    * current user.
    */
   public static String CHECKOUT_STATUS_SOMEONEELSE = "Checked Out";

   /**
    * Constant for security provider type name for internal user. This is 
    * redefined here since security provider classes are obfuscated.
    * @see PSSecurityProvider#SP_TYPE_RXINTERNAL
    * @see PSSecurityProvider#getSecurityProviderTypeString(int)
    */
   public static String SP_TYPE_RXINTERNAL =
      PSSecurityProvider.getSecurityProviderTypeString(
         PSSecurityProvider.SP_TYPE_RXINTERNAL);
   
   /**
    * Constant for internal user name. This is redefined here since security 
    * provider classes are obfuscated.
    * @see PSSecurityProvider#INTERNAL_USER_NAME
    */
   public static String INTERNAL_USER_NAME =
      PSSecurityProvider.INTERNAL_USER_NAME;
   
   /**
    * Constant for the file object referencing the properties file that defines
    * authtypes.
    */
   public static final String AUTHTYPE_PROP_FILE = "rxconfig" + 
      File.separator + "Server" + File.separator + "authtypes.properties";
   
   /**
    * Prefix for authtype properties used to build the key in the 
    * {@link #AUTHTYPE_PROP_FILE}.  Key format is <code>AUTHTYPE_PREFIX</code> + 
    * authtype.
    */
   public static final String AUTHTYPE_PREFIX = "authtype.";
   
   /**
    * Name of the request private object to use to disable field visibility 
    * rules processing by the content editor.  If value is a {@link Boolean}
    * object with a <code>true</code> value, visibility rules will be disabled,
    * otherwise it has no effect.
    */
   public static final String SKIP_FIELD_VISIBILITY_RULES = 
      "sys_skipFieldVisibilityRules";

   /**
    * Name of the request private object to use to indicate that an item is
    * being loaded in order to index content for search. A value that is a
    * {@link Boolean} object with a <code>true</code> value indicates that
    * this is the case, otherwise not.  Intended to allow for conditional
    * behavior in downstream processing.
    */
   public static final Object LOAD_FOR_SEARCH_INDEX = "sys_loadForSearchIndex";
   
   /**
    * Characters that are invalid for the file name in Windows, which is more
    * restrictive than UNIX.
    * <p>
    * The invalid characters for the file name in Windows are:
    * <pre>
    * \ / | < > ? " : *
    * </pre> 
    */
   public static String INVALID_WINDOWS_FILE_CHARACTERS = "\\/|<>?\":*";
   
   /**
    * Characters that should not be used as part of URL; otherwise it may cause
    * error in REST layer when the item name contain any of the characters.
    * <p>
    * '#' - used by anchors in HTML<br>
    * ';' - used to append "jsessionid=..." to URL<br>
    * '%' - used to URL encode/escape other characters.
    * </p>
    */
   public static String UNSAFE_URL_CHARACTERS = " #;%[]<>{}|\\^~`/?:@=&";
   
   /**
    * Characters that are invalid for item names (sys_title).
    * It is the combination of "invalid characters for the file name in 
    * Windows" and "unsafe URL characters".
    */
   public static String INVALID_ITEM_NAME_CHARACTERS = INVALID_WINDOWS_FILE_CHARACTERS + UNSAFE_URL_CHARACTERS;
}

