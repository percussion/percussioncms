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

package com.percussion.deployer.client;

/**
 * Deployment related constants.
 */

/******************************************************************************
 *                               R E A D    T H I S 
 * NOTE: Any ID Type Elements that are defined here must have a counterpart in 
 * $RXDIR/deploy/src/com/percussion/deploy/ui/PSIDTypesPanelResources.properties
 * ****************************************************************************
 */

public interface IPSDeployConstants
{
   /**
    * The root directory for all deployment files stored under the Rhythmyx root
    */
   public static final String DEPLOYMENT_ROOT = "sys_Packager";

   /**
    * Directory below the deployment root containing all server files, relative
    * to the Rhythmyx root.
    */
   public static final String SERVER_DIR = DEPLOYMENT_ROOT + "/server";

   /**
    * Directory below the deployment root containing all client files, relative
    * to the Rhythmyx root.
    */
   public static final String CLIENT_DIR = DEPLOYMENT_ROOT + "/client";

   /**
    * Extension to use for deployment archive files.  Includes the "." in the
    * value.
    */
   public static final String ARCHIVE_EXTENSION = ".ppkg";

   /**
    * Constant for the Request properties ID type element.
    */
   public static final String ID_TYPE_ELEMENT_REQUEST_PROPERTIES =
      "RequestProperties";

   /**
    * Constant for the Content Editor Field ID type element.
    */
   public static final String ID_TYPE_ELEMENT_CE_FIELD = "CEField";

   /**
    * Constant for the Content Editor ui definition ID type element.
    */
   public static final String ID_TYPE_ELEMENT_CE_UI_DEF = "CEUIDefinition";

   /**
    * Constant for the Content Editor Application Flow type element.
    */
   public static final String ID_TYPE_ELEMENT_CE_APP_FLOW = "CEAppFlow";

   /**
    * Constant for the Content Editor custom actions group type element.
    */
   public static final String ID_TYPE_ELEMENT_CE_CUSTOM_ACTIONS =
      "CECustomActionGroup";

   /**
    * Constant for the Content Editor input translations type element.
    */
   public static final String ID_TYPE_ELEMENT_CE_INPUT_TRANSLATIONS =
      "CEInputTranslations";

   /**
    * Constant for the Content Editor output translations type element.
    */
   public static final String ID_TYPE_ELEMENT_CE_OUTPUT_TRANSLATIONS =
      "CEOutputTranslations";

   /**
    * Constant for the Content Editor section link list type element.
    */
   public static final String ID_TYPE_ELEMENT_CE_SECTION_LINK_LIST =
      "CESectionLinkList";

   /**
    * Constant for the Content Editor command handler stylesheets type element.
    */
   public static final String ID_TYPE_ELEMENT_CE_COMMAND_HANDLER_STYLESHEETS =
      "CECommandHandlerStylesheets";

   /**
    * Constant for the Content Editor validation rules type element.
    */
   public static final String ID_TYPE_ELEMENT_CE_VALIDATION_RULES =
      "CEValidationRules";

   /**
    * Constant for the pre-exit type element.
    */
   public static final String ID_TYPE_ELEMENT_INPUT_DATA_EXITS =
      "InputDataExits";

   /**
    * Constant for the post-exit type element.
    */
   public static final String ID_TYPE_ELEMENT_RESULT_DATA_EXITS =
      "ResultDataExits";

   /**
    * Constant for the selector type element.
    */
   public static final String ID_TYPE_ELEMENT_SELECTOR = "Selector";

   /**
    * Constant for the result page set type element.
    */
   public static final String ID_TYPE_ELEMENT_RESULT_PAGES = "ResultPages";

   /**
    * Constant for the data mapper type element.
    */
   public static final String ID_TYPE_ELEMENT_DATA_MAPPER = "DataMapper";

   /**
    * Constant for the initparams type element.
    */
   public static final String ID_TYPE_ELEMENT_INIT_PARAMS = "InitParams";

   /**
    * Constant for the extensions type element.
    */
   public static final String ID_TYPE_ELEMENT_EXTENSIONS = "Extensions";

   /**
    * Constant for the effects type element.
    */
   public static final String ID_TYPE_ELEMENT_EFFECTS = "Effects";

   /**
    * Constant for the user properties type element.
    */
   public static final String ID_TYPE_ELEMENT_USER_PROPERTIES = "UserProps";

   /**
    * Constant for the process check type element.
    */
   public static final String ID_TYPE_ELEMENT_PROCESS_CHECKS = "ProcessChecks";

   /**
    * Constant for url parameters type element.  Represents parameters hardcoded
    * in a url string.
    */
   public static final String ID_TYPE_ELEMENT_URL_PARAMS = "UrlParams";
   
   /**
    * Constant for clone field overrides type element.
    */
   public static final String ID_TYPE_ELEMENT_CLONE_FIELD_OVERRIDES = 
      "CloneFieldOverrides";

   /**
    * Constant for item data field values.  Represents literal ids within 
    * content item data
    */
   public static final String ID_TYPE_ELEMENT_ITEM_DATA = "ItemData";
   
   /**
    * Constant for Finder parameters type element.  
    * Represents parameters hardcoded in a map in tables.
    */
   public static final String ID_TYPE_ELEMENT_SLOT_FINDER_PARAMS = "SlotFinderParams";
   
   
   /**
    * Constant for Content List Expander Params
    */
   public static final String ID_TYPE_ELEMENT_CONTENTLIST_EXPANDER_PARAMS="ExpanderParams";
  
   /**
    * Constant for Content List Generator Params
    */
   public static final String ID_TYPE_ELEMENT_CONTENTLIST_GENERATOR_PARAMS="GeneratorParams";

   /**
    * Constant for Item Filter RuleDef Params
    */
   public static final String ID_TYPE_ELEMENT_RULEDEF_PARAMS="RuleDefParams";

   /**
    * Constant for Template Bindings
    */
   public static final String ID_TYPE_ELEMENT_TEMPLATE_BINDINGS="TemplateBindings";

   
   /**
    * Constant for the application dependency object type
    */
   public static final String DEP_OBJECT_TYPE_APPLICATION = "Application";

   /**
    * Constant for the content definition dependency object type
    */
   public static final String DEP_OBJECT_TYPE_CONTENT_DEF = "ContentDef";

   /**
    * Constant for the content editor dependency object type
    */
   public static final String DEP_OBJECT_TYPE_CONTENT_EDITOR = "ContentEditor";

   /**
    * Constant for the content type dependency object type
    */
   public static final String DEP_OBJECT_TYPE_CONTENT_TYPE = "ContentType";

   /**
    * Shadow for the above CONTENT_TYPE, no different. 
    * todo merge into content_type
    */
   public static final String DEP_OBJECT_TYPE_NEW_CONTENT_TYPE = "NewContentType";

   /**
    * Constant for the content relation dependency object type
    */
   public static final String DEP_OBJECT_TYPE_CONTENT_RELATION = "ContentRelation";

   /**
    * Constant for the folder dependency object type
    */
   public static final String DEP_OBJECT_TYPE_FOLDER = "Folder";

   /**
    * Constant for the folder contents dependency object type
    */
   public static final String DEP_OBJECT_TYPE_FOLDER_CONTENTS = "FolderContents";

   /**
    * Constant for the folder defintion dependency object type
    */
   public static final String DEP_OBJECT_TYPE_FOLDER_DEF = "FolderDef";

   /**
    * Constant for the slot dependency object type
    */
   public static final String DEP_OBJECT_TYPE_SLOT = "Slot";

   /**
    * Constant for the shared group dependency object type
    */
   public static final String DEP_OBJECT_TYPE_SHARED_GROUP = "SharedGroup";

   /**
    * Constant for the System def dependency object type
    */
   public static final String DEP_OBJECT_TYPE_SYSTEM_DEF = "SystemDef";

   /**
    * Constant for the System def element dependency object type.  These
    * elements may be overridden in the shared def.
    */
   public static final String DEP_OBJECT_TYPE_SYSTEM_DEF_ELEMENT =
      "SystemDefElement";
   /**
    * Constant for the slot definition dependency object type
    */
   public static final String DEP_OBJECT_TYPE_SLOT_DEF = "SlotDef";

   /**
    * Constant for the variant definition dependency object type
    */
   public static final String DEP_OBJECT_TYPE_VARIANT_DEF = "VariantDef";

   /**
    * Constant for the Custom dependency object type.
    */
   public static final String DEP_OBJECT_TYPE_CUSTOM = "Custom";

   /**
    * Constant for the loadable handler dependency object type.
    */
   public static final String DEP_OBJECT_TYPE_LOADABLE_HANDLER =
      "LoadableHandler";

   /**
    * Constant for Translation settings
    */
   public static final String DEP_OBJECT_TYPE_TRANSLATIONSETTINGS_DEF = 
      "TranslationSettingsDef";
   
   /**
    * Constant for sys_eWebEditPro control name
    */
   public static final String CONTROL_NAME_SYS_EWEB_EDIT_PRO =
      "sys_eWebEditPro";

   /**
    * Constant for sys_FileWord control name
    */
   public static final String CONTROL_NAME_SYS_FILE_WORD = "sys_FileWord";

   /**
    * Constant for sys_eWebEditPro control SRC param
    */
   public static final String CONTROL_PARAM_NAME_SRC = "SRC";

   /**
    * Constant for sys_FileWord control WordTemplateURL param
    */
   public static final String CONTROL_PARAM_NAME_WORD_TEMPLATE_URL =
      "WordTemplateURL";

   /**
    * Constant specifying duration in minutes after which lock a expires.
    */
   public static final long LOCK_EXPIRATION_DURATION = 30;

   /**
    * The path of help set file relative to the rhythmyx root.
    */
   public static final String HELPSET_FILE =
      "Docs/Rhythmyx/Multi_Server_Manager/Rhythmyx_Multi-Server_Manager.hs";

   /**
    * The id for a preview site object
    */
   public static final String PREVIEW_SITE_ID = "0";

   /**
    * The system property to allow cross-build deployment.  Does not require a
    * value, just must be set, allows deployment across different builds, but
    * not different versions.
    */
   public static final String PROP_ALLOW_BUILD_MISMATCH =
      "com.percussion.deployer.allowBuildMismatch";
   
   /**
    * The system property to allow missing package dependencies.  Does not 
    * require a value, just must be set, allows deployment of a package that has
    * package dependencies that have not been meet.
    */
   public static final String PROP_ALLOW_MISSING_PACKAGE_DEP =
      "com.percussion.deployer.allowMissingPackageDep";

   /**
    * The system property for max child dependencies override. If set to a valid
    * number, determines the maximum number of child dependencies or ancestors
    * that can be returned without an error.  If it does not specify a valid
    * number, or is not specified, the default value {@link #MAX_DEPS} is used.
    */
   public static final String PROP_MAX_DEPS =
      "com.percussion.deployer.maxDeps";

   /**
    * The system property for the Rhythmyx root override.  Set this to specify
    * the server request root when it is not using the standard "/Rhythmyx"
    * value. Can be specified with or without the leading forward slash.
    */
   public static final String PROP_RX_ROOT =
      "com.percussion.deployer.rxRoot";

   /**
    * The system property to allow creation of packages containing Rhythmyx
    * sample applications. Does not require a value, just must be set.
    * All the archives created when running in this mode will have archiveType
    * (attribute of PSXExportDescriptor element) set to
    * <code>PSExportDescriptor.ARCHIVE_TYPE_SAMPLE</code>
    */
   public static final String PROP_CREATE_SAMPLE_ARCHIVE =
      "com.percussion.deployer.createSampleArchive";

   /**
    * The constant for default max child dependencies value, currently 200
    */
   public static final int MAX_DEPS = 200;

   /**
    * Constant to indicate 'Community' element type.
    */
   public static final String TYPE_COMMUNITY = "Community";

   /**
    * Constant to indicate 'Component' element type.
    */
   public static final String TYPE_COMPONENT = "Component";

   /**
    * Constant for the file separator used in catalog results.
    */
   public static final String CAT_FILE_SEP = "/";
   
   /**
    * Constant for the Request parameter validations ID type element.
    */
   public static final String ID_TYPE_ELEMENT_REQUEST_VALIDATIONS =
      "RequestParamValidations";

   /**
    * A util header for templates. IPSAssemblyTemplate upon serialization will
    * not have this header. Just prepend it.
    */
   public static final String XML_HDR_STR = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
   
   /**
    * Key for error messages on validation results
    */
   public static final String ERROR_KEY = "Error"; 
   
   /**
    * Key for warning messages on validation results
    */
   public static final String WARNING_KEY = "Warning"; 
   
   /**
    * A request parameter indicates whether to apply all communities to the 
    * imported package.
    */
   public static final String APPLY_TO_ALL_COMMS = "sys_pkgVisibleToAllCommunities";
}
