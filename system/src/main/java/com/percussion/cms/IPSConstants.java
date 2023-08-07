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
    * Mime Types
    */
   String MIME_HTML="text/html";

   String MIME_XML = "text/xml";

   String MIME_JSON = "application/json";


   /**
    * This is part of the primary key for the main content item table. Never
    * empty.
    */
    String ITEM_PKEY_CONTENTID = "CONTENTID";

   /**
    * This is part of the primary key for the main content item table. Never
    * empty.
    */
    String ITEM_PKEY_REVISIONID = "REVISIONID";

   /**
    * This is the primary key for all child tables. Never empty.
    */
    String CHILD_ITEM_PKEY = "SYSID";

   /**
    * This is the foreign key to the parent for any table that is a child of a
    * child. Never empty.
    */
    String CHILD_OF_CHILD_PKEY = "PARENTID";

   /**
    * The (optional) column that stores the list position an item within all
    * the items for the contentid.
    */
    String CHILD_SORT_KEY = "SORTRANK";

   /**
    * The relationship main table name.
    */
    String PSX_RELATIONSHIPS = "PSX_OBJECTRELATIONSHIP";

   /**
    * The relationship configuration name table.
    */
    String PSX_RELATIONSHIPCONFIGNAME = "PSX_RELATIONSHIPCONFIGNAME";

   /**
    * The column name for the relationship tables unique id column.
    */
    String RS_RID = "RID";

   /**
    * The relationship properties table name.
    */
    String PSX_RELATIONSHIPPROPERTIES =
      "PSX_OBJECTRELATIONSHIPPROP";

   /**
    * The column name for the relationship properties table property name column.
    */
    String RS_PROPERTYNAME = "PROPERTYNAME";

   /**
    * The column name for the relationship properties table property value column.
    */
    String RS_PROPERTYVALUE = "PROPERTYVALUE";

   /**
    * The HTML parameter name that specifies the submit name for the requested
    * binary field.
    */
    String SUBMITNAME_PARAM_NAME = "sys_submitname";

   /**
    * This is the last modifier column of the main content item table. Never
    * empty.
    */
    String ITEM_CONTENTLASTMODIFIER =
      "CONTENTLASTMODIFIER";

   /**
    * This is the last modified date column of the main content item table.
    * Never empty.
    */
    String ITEM_CONTENTLASTMODIFIEDDATE =
      "CONTENTLASTMODIFIEDDATE";

   /**
    * This is the edit revision column of the main content item table. Never
    * empty.
    */
    String ITEM_EDITREVISION =
      "EDITREVISION";

   /**
    * This is the tip revision column of the main content item table. Never
    * empty.
    */
    String ITEM_TIPREVISION =
      "TIPREVISION";

   /**
    * This is the current revision column of the main content item table. Never
    * empty.
    */
    String ITEM_CURRENTREVISION =
      "CURRENTREVISION";

   /**
    * This is the content state id column of the main content item
    * table. Never empty.
    */
    String ITEM_CONTENTSTATEID = "CONTENTSTATEID";

   /**
    * This is the author column of the main content item
    * table. Never empty.
    */
    String ITEM_CONTENTCREATEDBY = "CONTENTCREATEDBY";

   /**
    * This is the checked out username of the main content item
    * table. Never empty.
    */
    String ITEM_CONTENTCHECKOUTUSERNAME =
      "CONTENTCHECKOUTUSERNAME";

   /**
    * This is the title column of the main content item table.
    * May be empty;
    */
    String ITEM_TITLE = "TITLE";

   /* Init params have names of the form 'com.percussion.<name>'. */
   /**
    * The name of the InitParam in the system def that contains the name of
    * the control to use when a hidden control is needed. The specified
    * control can be overridden by the end purchaser. Never empty.
    */
    String  HIDDEN_CONTROL_PARAM_NAME =
         "com.percussion.hidden_control_name";

   /**
    * Name of the main content item table.
    */
    String CONTENT_STATUS_TABLE = "CONTENTSTATUS";

   /**
    * Name of the column in the related content table that identifies the
    * content id to which an item is related.
    */
    String RELATED_CONTENTID_COLUMN = "ITEMCONTENTID";

   /**
    * Name of the table that holds the content variant registrations.
    */
    String CONTENT_VARIANTS_TABLE = "PSX_TEMPLATE";

   /**
    * Name of the column in the contentstatus table that identifies the
    * content type of that row.
    */
    String CONTENTTYPEID_COLUMN = "CONTENTTYPEID";

   /**
    * Name of the column in the content variants table that contains the
    * id of that variant.
    */
    String VARIANTID_COLUMN = "TEMPLATE_ID";

   /**
    * Name of the column in the content variants table that contains the
    * url used to query a new editor page.
    */
    String ASSEMBLYURL_COLUMN = "ASSEMBLYURL";

   /**
    * Name of the table that holds the context variables
    */
    String RXASSEMBLERPROPERTIES_TABLE = "RXASSEMBLERPROPERTIES";

   /**
    * Name of the column in the ASSEMBLERPROPERTIES table that holds a value of
    * the context variable.
    */
    String PROPERTYVALUE_COLUMN = "PROPERTYVALUE";

   /**
    * The name of the (hidden) Rx application that contains the resources
    * used by the crow editors.
    */
    String EDITOR_SUPPORT_APPNAME = "sys_ceSupport";

   /** The base URL for the lookup application if the choices are global */
    String GLOBAL_LOOKUP = EDITOR_SUPPORT_APPNAME + "/lookup";

   /*
    * Value used for transition ID to indicate that a check in or check out was
    * performed (They don't have an entry in the TRANSITIONS table. Set in
    * PSExitPerformTransition. Used by PSExitUpdateHistory).
    */
     int TRANSITIONID_CHECKINOUT = 0;

   /*
    * Value used for transition ID to indicate that there was no error, but  no
    * action was taken. This is used when a user attempts to check out a
    * document that is already checked out. Set in PSExitPerformTransition
    * Used by PSExitUpdateHistory.
    */
     int TRANSITIONID_NO_ACTION_TAKEN = -1;

   /**
    * Revision value used when there is no corresponding actual revision.
    * Used for the edit revison of a content item that is not checked out, or
    * for the tip revision before it is given a meaninful value.
    */
     int NO_CORRESPONDING_REVISION_VALUE = -1;

   /**
    * Workflow id used when there is no workflow id exist for an content item.
    * For example, if the object is not workflowable, its workflow id should
    * be this value.
    */
     int INVALID_WORKFLOW_ID = -1;

   /**
    * This is the value text to use for document attributes that have a boolean
    * value of true.
    */
    String BOOLEAN_TRUE = "yes";

    String TRUE="true";

   /**
    * This is the value text to use for document attributes that have a boolean
    * value of false.
    */
    String BOOLEAN_FALSE = "no";

    String FALSE="false";



   /**
    * The name of the default view to use if one has not been specified.
    * Currently this is the <code>sys_Default</code> view.  This is the main
    * content editor view, includes all fields, and is linked to a stylesheet
    * through the system def that displays the full CE UI.
    */
    String DEFAULT_VIEW_NAME = "sys_Default";

   /**
    * The name of the system view that includes all fields, but is linked
    * through the system def with a stylesheet that only displays the the
    * fields, and no other parts of the CE UI.
    */
    String SYS_ALL_VIEW_NAME = "sys_All";

   /**
    * The name of the system view that includes all content fields.
    */
    String SYS_CONTENT_VIEW_NAME = "sys_Content";

   /**
    * The name of the system view that includes all meta data (system) fields.
    */
    String SYS_ITEM_META_VIEW_NAME = "sys_ItemMeta";

   /**
    * The name of the system view that includes a single field.  The format is
    * <code>sys_SingleField:<fieldName></code> where
    * <code>fieldName</code> is the name of the single field.
    */
    String SYS_SINGLE_FIELD_VIEW_NAME = "sys_SingleField:";
   
   /**
    * The name of the system view that includes a list of hidden fields. The
    * displayed fields are all fields (from {@link #SYS_ALL_VIEW_NAME}) minus
    * the fields specified by this view. The list of hidden fields are comma
    * delimited, such as, "sys_HiddenFields:XXX,YYY,ZZZ". The list of fields
    * may be empty, in this case, all fields (from {@link #SYS_ALL_VIEW_NAME})
    * will be displayed.
    */
    String SYS_HIDDEN_FIELDS_VIEW_NAME = "sys_HiddenFields:";
   
   /**
    * Name of the system field that denotes the relevancy ranking of a search
    * result, currently "sys_relevancy".
    */
    String SYS_RELEVANCY = "sys_relevancy";
   
   /**
    * The name of the system field that acts as a flag to indicate that
    * a folder should be published with a site.(Site folder Publishing)
    */
    String SYS_PUBLISH_FOLDER_WITH_SITE =
      "sys_publishFolderWithSite";
   
   /**
    * The name of query attribute which determines how SFP should handle
    * publish flagged site folders.
    */
    String INCLUDE_FOLDERS =
      "IncludeFolders";
   
   /**
    * Name of the system field that denotes the current revision of an item, 
    * currently "sys_currentrevision".
    */
    String SYS_CURRENTREVISION = "sys_currentrevision";

   /* The following contstants are temporary. Most of them are the same as
      constants in com.percussion.workflow.PSWorkFlowUtils. A couple of them
      define constants specified in rxworkflow.properties. We need to come up
      with a pattern that allows the sharing of constants across
      independent sub-systems. I don't want the server to have a dependency on
      the workflow system. */
    String CHECKINOUT_CONDITION_IGNORE = "ignore";
    String CHECKINOUT_CONDITION_CHECKIN = "checkin";
    String CHECKINOUT_CONDITION_CHECKOUT = "checkout";
    String DEFAULT_ACTION_TRIGGER_NAME = "WFAction";
    String TRIGGER_CHECKOUT = "checkout";
    String TRIGGER_CHECKIN = "checkin";
    String TRIGGER_FORCE_CHECKIN = "forcecheckin";
    int ASSIGNMENT_TYPE_NOT_IN_WORKFLOW = 0;
    int ASSIGNMENT_TYPE_NONE = 1;
    int ASSIGNMENT_TYPE_READER = 2;
    int ASSIGNMENT_TYPE_ASSIGNEE = 3;
    int ASSIGNMENT_TYPE_ADMIN = 4;
   String DEFAULT_NEWSTATEID_NAME = "newstateid";
   String WF_ACTION_PERFORMED = "WFActionPerformed";

   /**
    * Checkout status indicating it is not checked out to any body.
    */
   String CHECKOUT_STATUS_NOBODY = "Checked In";

   /**
    * Checkout status indicating it is checked out to the current user.
    */
   String CHECKOUT_STATUS_MYSELF = "Checked Out by Me";

   /**
    * Checkout status indicating it is checked out to somebody other than the 
    * current user.
    */
   String CHECKOUT_STATUS_SOMEONEELSE = "Checked Out";

   /**
    * Constant for security provider type name for internal user. This is 
    * redefined here since security provider classes are obfuscated.
    * @see PSSecurityProvider#SP_TYPE_RXINTERNAL
    * @see PSSecurityProvider#getSecurityProviderTypeString(int)
    */
   String SP_TYPE_RXINTERNAL =
      PSSecurityProvider.getSecurityProviderTypeString(
         PSSecurityProvider.SP_TYPE_RXINTERNAL);
   
   /**
    * Constant for internal user name. This is redefined here since security 
    * provider classes are obfuscated.
    * @see PSSecurityProvider#INTERNAL_USER_NAME
    */
   String INTERNAL_USER_NAME =
      PSSecurityProvider.INTERNAL_USER_NAME;
   
   /**
    * Constant for the file object referencing the properties file that defines
    * authtypes.
    */
    String AUTHTYPE_PROP_FILE = "rxconfig" +
      File.separator + "Server" + File.separator + "authtypes.properties";
   
   /**
    * Prefix for authtype properties used to build the key in the 
    * {@link #AUTHTYPE_PROP_FILE}.  Key format is <code>AUTHTYPE_PREFIX</code> + 
    * authtype.
    */
    String AUTHTYPE_PREFIX = "authtype.";
   
   /**
    * Name of the request private object to use to disable field visibility 
    * rules processing by the content editor.  If value is a {@link Boolean}
    * object with a <code>true</code> value, visibility rules will be disabled,
    * otherwise it has no effect.
    */
    String SKIP_FIELD_VISIBILITY_RULES =
      "sys_skipFieldVisibilityRules";

   /**
    * Name of the request private object to use to indicate that an item is
    * being loaded in order to index content for search. A value that is a
    * {@link Boolean} object with a <code>true</code> value indicates that
    * this is the case, otherwise not.  Intended to allow for conditional
    * behavior in downstream processing.
    */
    Object LOAD_FOR_SEARCH_INDEX = "sys_loadForSearchIndex";

   /**
    * The default mime type to use if a mime type is undefined.
    */
    String DEFAULT_MIMETYPE="text/html";

    String PARAM_REINIT_TEMPLATE_ENGINE="sys_reinit";


   /**
    * A boolean property that flags template engines to turn off all server caching of templates.
    */
    String SERVER_PROP_NO_CACHE_TEMPLATES = "noCacheTemplates";

   /**
    * When set to true will send output through a compressor based on the mime type
    * of the assembly result.
    */
    String SERVER_PROP_COMPRESS_OUTPUT = "compressOutput";


   /*
    * System template bindings
    */

   /**
    *
    */
    String SYS_PARAM_MIMETYPE="$sys.mimetype";

   /**
    *
    */
    String SYS_PARAM_CHARSET="$sys.charset";

   /**
    *
    */
    String SYS_PARAM_TEMPLATE="$sys.template";

   /**
    *
    */
    String SYS_PARAM_AA= "$sys.activeAssembly";

   /**
    *
    */
    String SYS_PARAM_ASM= "$sys.asm";

   /**
    *
    */
    String SYS_PARAM_ASSEMBLY_ITEM= "$sys.assemblyItem";

   /**
    *
    */
    String SYS_PARAM_INDEX= "$sys.index";

   /**
    *
    */
    String SYS_PARAM_ITEM= "$sys.item";

   /**
    *
    */
    String SYS_PARAM_PARAMS= "$sys.params";

   /**
    *
    */
    String SYS_PARAM_RX= "$rx";

   /**
    *
    */
    String SYS_PARAM_SITE= "$sys.site";

   /**
    *
    */
    String SYS_PARAM_VARIABLES= "$sys.variables";

   /**
    *
    */
    String SYS_PARAM_TOOLS= "$tools";

   /**
    *
    */
    String SYS_PARAM_USER= "$user";

   /**
    * The name of the binding variable, used to turn off the compiled templates
    */
    String SYS_PARAM_NO_CACHE_TEMPLATE = "$sys.noCacheTemplate";

   /**
    *
    */
    String SYS_PARAM_CTX = "$sys.ctx";

   /**
    *
    */
    String SYS_PARAM_SYSTEM="$sys";


    String DB_ACTION_TYPE="DBActionType";
    String DB_ACTION_INSERT="INSERT";
    String DB_ACTION_UPDATE="UPDATE";
    String DB_ACTION_DELETE="DELETE";

   /**
    * Logger Categories
    */
    String PACKAGING_LOG="Package Management";
    String MSM_LOG="Multi-Server Management";
    String NAVIGATION_LOG="Managed Navigation";
    String TEST_LOG="Testing";
    String ASSEMBLY_LOG="Assembly";
    String PUBLISHING_LOG="Publishing";
    String API_LOG="API";
    String WORKFLOW_LOG="Workflow";
    String SECURITY_LOG="Security";
    String I18N_LOG="I18N";
    String SEARCH_LOG="Search";
    String SERVER_LOG="Server";
    String CONTENTREPOSITORY_LOG="Content Repository";
    String DASHBOARD_LOG = "Dashboard";
    String WEBSERVICES_LOG = "Web Services";
    String WIDGET_BUILDER_LOG = "Widget Builder";

    String SAAS_FLAG = "doSAAS";


    String SERVER_PROP_MANAGELINKS = "AUTO_MANAGE_LOCAL_PATHS";

    String SERVER_PROP_ALLOWED_ORIGINS = "allowedOrigins";
    String SERVER_PROP_ALLOWED_ORIGINS_DEFAULT = "*";
    String SERVER_PROP_PUBLIC_CMS_HOSTNAME = "publicCmsHostname";
    String SERVER_PROP_REQUEST_BEHIND_PROXY= "requestBehindProxy";


     String EXTENSIONS_LOG = "Extensions";
     String IMPORT_LOG = "Import";
     String DESIGN_LOG = "Design";
     String CACHING_LOG = "Caching";
    String JAVA_EXTENSIONS_LOG = "Java Extensions";
     String SERVER_PROP_FILE_COPY_BUFFER_SIZE = "fileCopyBufferSize";
     String LEGACY_UI_ATTR = "sys_legacyui";
}

