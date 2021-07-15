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

// Centralized list of all URLS and Paths needed by the UI

(function($) {

    var SERVER_ROOT = "/Rhythmyx";
    var SERVICES_ROOT = SERVER_ROOT + "/services";
    var PERC_ROOT = "/cm";
    var SERVICES = {
        ANALYTICS    : SERVICES_ROOT + "/analytics",
        PAGEMGT      : SERVICES_ROOT + "/pagemanagement",
        SITEMGT      : SERVICES_ROOT + "/sitemanage",
        MIGRATIONMGT : SERVICES_ROOT + "/migrationmanagement",
        PATHMGT      : SERVICES_ROOT + "/pathmanagement",
        ASSETMGT     : SERVICES_ROOT + "/assetmanagement",
        DELIVERY     : SERVICES_ROOT + "/delivery",
        ITEMMGT      : SERVICES_ROOT + "/itemmanagement",
        USERMGT      : SERVICES_ROOT + "/user",
        UTILS        : SERVICES_ROOT + "/utils",
        METADATAMGT  : SERVICES_ROOT + "/metadatamanagement",
        ACTIVITY     : SERVICES_ROOT + "/activitymanagement",
        FINDER       : SERVICES_ROOT + "/searchmanagement",
        WORKFLOW     : SERVICES_ROOT + "/workflowmanagement",
        ROLEMGT      : SERVICES_ROOT + "/rolemanagement",
        DESIGN       : SERVICES_ROOT + "/design",
        FOLDERMGT    : SERVICES_ROOT + "/foldermanagement",
        LICENSE      : SERVICES_ROOT + "/licensemanagement",
        PUBMGT       : SERVICES_ROOT + "/publishmanagement",
        SHARE        : SERVICES_ROOT + "/share",
        WDGMGT       : SERVICES_ROOT + "/widgetmanagement",
        PAGEOPTMGT   : SERVICES_ROOT + "/pageoptimizermanagement",
        RECMGT       : SERVICES_ROOT + "/recentmanagement",
        REDMGT       : SERVICES_ROOT + "/redirectmanagement",
        CLDSVC       : SERVICES_ROOT + "/cloudservicemanagement",
        CATMGT       : SERVICES_ROOT + "/categorymanagement",
        COOKIECONSENT: SERVICES_ROOT + "/cookieconsent"
    };
    var ASSETS    = "Assets";
    var SITES     = "Sites";
    var SEARCH    = "Search";
    var DESIGN    = "Design";
    var RECYCLING = "Recycling";

    $.perc_displayformats = {
        "/" : "",
        "/Design" : "CM1_Design"
    };

    $.perc_paths = {
        // Path management services paths
        //JB The following service is used for asset save as dialog, that needs to be modified to use addNewFolder service.
        PATH_ADD_FOLDER                     : SERVICES.PATHMGT + "/path/addFolder",
        PATH_ADD_NEW_FOLDER                 : SERVICES.PATHMGT + "/path/addNewFolder",// /{path}
        PATH_FOLDER                         : SERVICES.PATHMGT + "/path/folder",
        PATH_CHILD_FOLDERS                  : SERVICES.PATHMGT + "/path/childFolders",
        PATH_PAGINATED_FOLDER               : SERVICES.PATHMGT + "/path/paginatedFolder",
        PATH_ITEM                           : SERVICES.PATHMGT + "/path/item",
        PATH_ITEM_BY_ID                     : SERVICES.PATHMGT + "/path/item/id",
        PATH_ITEM_SUMMARY_BY_WORKFLOW_STATE : SERVICES.PATHMGT + "/path/item/wfState",
        PATH_ITEM_MOVE                      : SERVICES.PATHMGT + "/path/moveItem",
        PATH_ITEM_PROPERTIES                : SERVICES.PATHMGT + "/path/itemProperties",
        PATH_VALIDATE_DELETE_FOLDER         : SERVICES.PATHMGT + "/path/validateFolderDelete",
        PATH_DELETE_FOLDER                  : SERVICES.PATHMGT + "/path/deleteFolder",
        GET_ASSET_PAGINATION_CONFIG         : SERVICES.PATHMGT + "/path/getAssetPaginationConfig",
        PATH_RESTORE_FOLDER                 : SERVICES.PATHMGT + "/path/restoreFolder",
        PATH_RENAME_FOLDER                  : SERVICES.PATHMGT + "/path/renameFolder",
        PATH_GET_FOLDER_PROPERTIES          : SERVICES.PATHMGT + "/path/folderProperties",
        PATH_SAVE_FOLDER_PROPERTIES         : SERVICES.PATHMGT + "/path/saveFolderProperties",
        PATH_LAST_EXISTING                  : SERVICES.PATHMGT + "/path/lastExisting",
        PATH_VALIDATE_EXIST                 : SERVICES.PATHMGT + "/path/validate",
        DISPLAY_FORMAT                      : SERVICES_ROOT + "/ui/uicomps/displayformat/name/",

        // Page management services paths
        TEMPLATES_ALL              : SERVICES.PAGEMGT + "/template/summary/all",
        TEMPLATE_HTML_PARSE        : SERVICES.PAGEMGT + "/render/parse",
        TEMPLATE_EXPORT            : SERVICES.PAGEMGT + "/template/export",
        TEMPLATE_IMPORT            : SERVICES.PAGEMGT + "/template/import",
        TEMPLATES_USER             : SERVICES.PAGEMGT + "/template/summary/all/user",
        TEMPLATE_LOAD_SAVE         : SERVICES.PAGEMGT + "/template",
        TEMPLATE_LOAD_METADATA     : SERVICES.PAGEMGT + "/template/loadTemplateMetadata",
        TEMPLATE_SAVE_METADATA     : SERVICES.PAGEMGT + "/template/saveTemplateMetadata",
        TEMPLATE_CREATE_FROM_URL        : SERVICES.SITEMGT + "/sitetemplates/createFromUrl",
        TEMPLATE_CREATE_FROM_URL_ASYNC  : SERVICES.SITEMGT + "/sitetemplates/createFromUrlAsync",
        TEMPLATE_CREATE_FROM_URL_RESULT : SERVICES.SITEMGT + "/sitetemplates/getImportedTemplate",
        TEMPLATE_CREATE_FROM_PAGE  : SERVICES.SITEMGT + "/sitetemplates/createFromPage",
        TEMPLATE_RENDER            : SERVICES.PAGEMGT + "/render/template",
        TEMPLATE_RENDER_SCRIPTSOFF : SERVICES.PAGEMGT + "/render/template/scriptsoff",
        TEMPLATES_BY_SITE          : SERVICES.SITEMGT + "/sitetemplates/templates",
        TEMPLATES_WITH_NOSITE      : SERVICES.SITEMGT + "/sitetemplates/nosite",
        TEMPLATES_READONLY         : SERVICES.PAGEMGT + "/template/summary/all/readonly",
        TEMPLATES_SAVE             : SERVICES.SITEMGT + "/sitetemplates",
        PAGE_CREATE                : SERVICES.PAGEMGT + "/page",
        SAVE_PAGE_METADATA         : SERVICES.PAGEMGT + "/page/savePageMetadata",
        PAGES_WITH_TEMPLATE        : SERVICES.PAGEMGT + "/page/pagesByTemplate",
        UNASSIGNED_PAGES_BY_SITE   : SERVICES.PAGEMGT + "/page/unassignedPagesBySite",
        MIGRATION_EMPTY_FLAG       : SERVICES.PAGEMGT + "/page/migrationEmptyFlag",
        CLEAR_MIGRATION_EMPTY_FLAG : SERVICES.PAGEMGT + "/page/clearMigrationEmptyFlag",
        RENDER_LINK_PREVIEW        : SERVICES.PAGEMGT + "/renderlink/preview",
        SEARCH_PAGE_ASSETS_BY_STATUS           : SERVICES.PAGEMGT + "/page/searchPageByStatus",

        BLOG_LOAD : SERVICES.SITEMGT + "/section/blogs",
        POST_LOAD : SERVICES.SITEMGT + "/section/blogPosts",

        SECTION_CREATE               : SERVICES.SITEMGT + "/section/create",
        SECTION_CREATE_SECTION_LINK  : SERVICES.SITEMGT + "/section/createSectionLink",
        SECTION_DELETE_SECTION_LINK  : SERVICES.SITEMGT + "/section/deleteSectionLink",
        SECTION_CREATE_EXTERNAL_LINK : SERVICES.SITEMGT + "/section/createExternalLinkSection",
        SECTION_SECTION_FROM_FOLDER  : SERVICES.SITEMGT + "/section/createSectionFromFolder",
        SECTION_CONVERT_TO_FOLDER    : SERVICES.SITEMGT + "/section/convertToFolder/",
        SECTION_DELETE               : SERVICES.SITEMGT + "/section",
        SECTION_LOAD                 : SERVICES.SITEMGT + "/section",
        SECTION_MOVE                 : SERVICES.SITEMGT + "/section/move",
        SECTION_REPLACE_LANDING_PAGE : SERVICES.SITEMGT + "/section/replaceLandingPage",
        SECTION_UPDATE               : SERVICES.SITEMGT + "/section/update",
        SECTION_LINK_UPDATE          : SERVICES.SITEMGT + "/section/updateSectionLink",
        SECTION_EXTERNAL_LINK_UPDATE : SERVICES.SITEMGT + "/section/updateExternalLink",
        SECTION_GET_ROOT             : SERVICES.SITEMGT + "/section/root",
        SECTION_GET_TREE             : SERVICES.SITEMGT + "/section/tree",
        SECTION_GET_CHILDREN         : SERVICES.SITEMGT + "/section/childSections",
        SECTION_GET_PROPERTIES       : SERVICES.SITEMGT + "/section/properties",
        PAGE_DELETE                  : SERVICES.PAGEMGT + "/page",
        PAGE_FORCE_DELETE            : SERVICES.PAGEMGT + "/page/forceDelete",
        PAGE_PURGE                   : SERVICES.PAGEMGT + "/page/purge",
        PAGE_FORCE_PURGE             : SERVICES.PAGEMGT + "/page/forcePurge",
        PAGE_VALIDATE_DELETE         : SERVICES.PAGEMGT + "/page/validateDelete",
        PAGE_COPY                    : SERVICES.PAGEMGT + "/page/copy",
        PAGE_RESTORE                 : SERVICES.PAGEMGT + "/page/restorePage",
        PAGE_CHANGE_TEMPLATE         : SERVICES.PAGEMGT + "/page/changeTemplate",
        /* Returns an assembled page appropriate for editing. Empty widgets may have sample
            content and widgets may be rendered for inline editing. */
        PAGE_EDIT                   : SERVICES.PAGEMGT + "/render/page/editmode",
        PAGE_EDIT_SCRIPTSOFF        : SERVICES.PAGEMGT + "/render/page/editmode/scriptsoff",
        PAGE_PREVIEW                : SERVICES.PAGEMGT + "/render/page/",
        PAGE_EDITURL                : SERVICES.PAGEMGT + "/page/pageEditUrl",
        PAGE_VIEWURL                : SERVICES.PAGEMGT + "/page/pageViewUrl",
        PAGE_NONSEO                 : SERVICES.PAGEMGT + "/page/nonSEOPages",
        WIDGETS_ALL                 : SERVICES.PAGEMGT + "/widget",
        WIDGET_FULL                 : SERVICES.PAGEMGT + "/widget/full",
        WIDGET_INFO                 : SERVICES.PAGEMGT + "/widget/packageinfo",
        THEME_CSS                   : SERVICES.PAGEMGT + "/theme/css",
        REGION_CSS                  : SERVICES.PAGEMGT + "/theme/regioncss",
        REGION_CSS_MERGE            : SERVICES.PAGEMGT + "/theme/regioncss/merge",
        REGION_CSS_PREPARE_FOR_EDIT : SERVICES.PAGEMGT + "/theme/regioncss/prepareForEdit",
        REGION_CSS_CLEAR_CACHE      : SERVICES.PAGEMGT + "/theme/regioncss/clearCache",
        THEME_SUMMARY_ALL           : SERVICES.PAGEMGT + "/theme/summary/all",
        RICHTEXT_CUSTOM_STYLES      : SERVICES.PAGEMGT + "/theme/customstyles",

        //Analytics
        ANALYTICS_GET_CONFIG      : SERVICES.ANALYTICS + "/provider/config",
        ANALYTICS_STORE_CONFIG    : SERVICES.ANALYTICS + "/provider/config",
        ANALYTICS_DELETE_CONFIG   : SERVICES.ANALYTICS + "/provider/config",
        ANALYTICS_TEST_CONNECTION : SERVICES.ANALYTICS + "/provider/testConnection", //{userid}/{password}
        ANALYTICS_GET_PROFILES    : SERVICES.ANALYTICS + "/provider/profiles", //{userid}/{password}

        //Asset management paths
        CONTENT_EDIT_CRITERIA         : SERVICES.ASSETMGT + "/asset/contentEditCriteria",
        ASSET_WIDGET_REL              : SERVICES.ASSETMGT + "/asset/createAssetWidgetRelationship",
        ASSET_FROM_LOCALCONTENT       : SERVICES.ASSETMGT + "/asset/shareLocalContent",
        ASSET_WIDGET_PROMOTE          : SERVICES.ASSETMGT + "/asset/promoteAssetWidget",
        ASSET_WIDGET_REL_UPDATE       : SERVICES.ASSETMGT + "/asset/updateAssetWidgetRelationship",
        ASSET_ADD_TO_FOLDER           : SERVICES.ASSETMGT + "/asset/addAssetToFolder",
        ASSET_WIDGET_DROP_CRITERIA    : SERVICES.ASSETMGT + "/asset/assetWidgetDropCriteria/",
        ASSET_WIDGET_REL_DEL          : SERVICES.ASSETMGT + "/asset/clearAssetWidgetRelationship",
        ASSET_ORPHAN_WIDGET_REL_DEL   : SERVICES.ASSETMGT + "/asset/clearOrphanAssetsWidgetRelationship",
        ASSET_EDITOR_LIBRARY          : SERVICES.ASSETMGT + "/asset/assetEditors",
        ASSET_TYPES                   : SERVICES.ASSETMGT + "/asset/assetTypes",
        ASSET_EDITOR                  : SERVICES.ASSETMGT + "/asset/assetEditor",
        ASSET_EDITOR_URL_FOR_ASSET_ID : SERVICES.ASSETMGT + "/asset/assetEditUrl",
        ASSET_VIEW_URL_FOR_ASSET_ID   : SERVICES.ASSETMGT + "/asset/assetViewUrl",
        ASSET_DELETE                  : SERVICES.ASSETMGT + "/asset",
        ASSET_PURGE                   : SERVICES.ASSETMGT + "/asset/purge",
        ASSET_FORCE_DELETE            : SERVICES.ASSETMGT + "/asset/forceDelete",
        ASSET_VALIDATE_DELETE         : SERVICES.ASSETMGT + "/asset/validateDelete",
        ASSET_REMOVE                  : SERVICES.ASSETMGT + "/asset/remove",
        ASSET_FORCE_REMOVE            : SERVICES.ASSETMGT + "/asset/forceRemove",
        ASSET_UPDATE                  : SERVICES.ASSETMGT + "/asset/updateAsset",
        ASSET_RESTORE                 : SERVICES.ASSETMGT + "/asset/restoreAsset",
        ASSET_UNUSED                  : SERVICES.ASSETMGT + "/asset/unusedAssets",
        ASSET_PROMOTE                 : SERVICES.ASSETMGT + "/asset/promoteAsset",
        CONVERT_WIDGET                : SERVICES.ASSETMGT + "/asset/createWidgetAsset",
        UPDATE_INSPECTED_ELEMENT      : SERVICES.ASSETMGT + "/asset/updateInspectedElements",

        // Site management services paths
        SITES_ALL                      : SERVICES.SITEMGT + "/site",
        SAAS_SITES_NAMES               : SERVICES.SITEMGT + "/site/sass/sitenames",
        SITES_ALL_CHOICES              : SERVICES.SITEMGT + "/site/choices",
        SITES_BY_TEMPLATE              : SERVICES.SITEMGT + "/sitetemplates/sites",
        SITE_CREATE                    : SERVICES.SITEMGT + "/site",
        SITE_CREATE_FROM_URL           : SERVICES.SITEMGT + "/site/importFromUrl",
        SITE_CREATE_FROM_URL_ASYNC     : SERVICES.SITEMGT + "/site/importFromUrlAsync",
        SITE_CREATE_FROM_URL_RESULT    : SERVICES.SITEMGT + "/site/getImportedSite",
        VIEW_IMPORT_LOG                : SERVICES.SITEMGT + "/site/importLogViewer",
        SITE_GET_PROPERTIES            : SERVICES.SITEMGT + "/site/properties",
        SITE_IS_BEING_IMPORTED         : SERVICES.SITEMGT + "/site/isSiteImporting",
        SITE_PUBLISH                   : SERVICES.SITEMGT + "/publish",
        SITE_ITEM_PUBLISH_ACTIONS      : SERVICES.SITEMGT + "/publish/publishingActions",
        PAGE_PUBLISH                   : SERVICES.SITEMGT + "/publish/page",
        RESOURCE_PUBLISH               : SERVICES.SITEMGT + "/publish/resource",
        PAGE_TAKEDOWN                  : SERVICES.SITEMGT + "/publish/takedown/page",
        RESOURCE_TAKEDOWN              : SERVICES.SITEMGT + "/publish/takedown/resource",
        INCREMENTAL_LIST               : SERVICES.SITEMGT + "/publish/incremental/content/",
        INCREMENTAL_RELATED_LIST       : SERVICES.SITEMGT + "/publish/incremental/relatedcontent/",
        INCREMENTAL_PUBLISH            : SERVICES.SITEMGT + "/publish/incremental/publish/",
        SITE_UPDATE_PROPERTIES         : SERVICES.SITEMGT + "/site/updateProperties",
        SITE_DELETE                    : SERVICES.SITEMGT + "/site",
        PUBLISH_CURRENT_STATUS         : SERVICES.SITEMGT + "/pubstatus/current",
        PUBLISH_LOGS                   : SERVICES.SITEMGT + "/pubstatus/logs",
        PUBLISH_PURGE                  : SERVICES.SITEMGT + "/pubstatus/purge",
        PUBLISH_LOGS_DETAILS           : SERVICES.SITEMGT + "/pubstatus/details",
        SITE_GET_PUBLISH_PROPERTIES    : SERVICES.SITEMGT + "/site/publishProperties",
        SITE_UPDATE_PUBLISH_PROPERTIES : SERVICES.SITEMGT + "/site/updatePublishProperties",
        SITE_COPY                      : SERVICES.SITEMGT + "/site/copy",
        SITE_COPY_VALIDATE_FOLDERS     : SERVICES.SITEMGT + "/site/validateFolders",
        SITE_COPY_INFO                 : SERVICES.SITEMGT + "/site/copysiteinfo",
        SITE_STATS_SUMMARY             : SERVICES.SITEMGT + "/site/statistics",
        //Site architecture paths
        SITE_ARCHITECTURE : SERVICES.SITEMGT + "/siteArchitecture",
        // URLS for ui tabs
        URL_ADMIN        : PERC_ROOT + "/app/admin.jsp",
        URL_ARCHITECTURE : PERC_ROOT + "/app/siteArchitecture.jsp",
        URL_DASHBOARD    : PERC_ROOT + "/app/dashboard.jsp",
        URL_WEBMGT       : PERC_ROOT + "/app/webmgt.jsp",
        IMAGE_ROOT       : PERC_ROOT + "/images",

        //Metadata service paths
        METADATA_FIND             : SERVICES.METADATAMGT + "/metadata",
        METADATA_DELETE_BY_PREFIX : SERVICES.METADATAMGT  + "/metadata" + "/byprefix",
        METADATA_FIND_BY_PREFIX   : SERVICES.METADATAMGT  + "/metadata" + "/byprefix",
        METADATA_DELETE           : SERVICES.METADATAMGT + "/metadata",
        METADATA_SAVE             : SERVICES.METADATAMGT + "/metadata",
        METADATA_SAVE_GLOBAL_VARIABLES  : SERVICES.METADATAMGT + "/metadata/globalvariables",

        //Process Monitor Service Paths
        PROCESS_STATUS_ALL           : SERVICES.SITEMGT + "/monitor/all",
        //workflow service paths
        WORKFLOW_CHECKOUT            : SERVICES.ITEMMGT + "/workflow/checkOut",
        WORKFLOW_CHECKIN             : SERVICES.ITEMMGT + "/workflow/checkIn",
        WORKFLOW_FORCE_CHECKOUT      : SERVICES.ITEMMGT + "/workflow/forceCheckOut",
        WORKFLOW_TRANSITION          : SERVICES.ITEMMGT + "/workflow/transition",
        WORKFLOW_TRANSITION_COMMENT  : SERVICES.ITEMMGT + "/workflow/transitionWithComments",
        WORKFLOW_TRANSITIONS         : SERVICES.ITEMMGT + "/workflow/getTransitions",
        WORKFLOW_CHECKED_OUT_TO_USER : SERVICES.ITEMMGT + "/workflow/isCheckedOutToCurrentUser",
        WORKFLOW_IS_APPROVE_ALLOWED  : SERVICES.ITEMMGT + "/workflow/isApproveAvailableToCurrentUser",
        WORKFLOW_BULK_APPROVE        : SERVICES.ITEMMGT + "/workflow/bulkApprove",
        WORKFLOW_STEPPED             : SERVICES.WORKFLOW + "/workflows/",
        WORKFLOW_META                : SERVICES.WORKFLOW + "/workflows/metadata",
        DEFAULT_WORKFLOW_META        : SERVICES.WORKFLOW + "/workflows/metadata/default",
        //User Management
        USER_USERS           : SERVICES.USERMGT  + "/user/users",
        USER_USERS_NAMES     : SERVICES.USERMGT  + "/user/users/names",
        USER_ROLES           : SERVICES.USERMGT  + "/user/roles",
        USER_FIND            : SERVICES.USERMGT  + "/user/find",
        CHANGEPW             : SERVICES.USERMGT  + "/user/changepw",
        USER_EXTERNAL_FIND   : SERVICES.USERMGT  + "/user/external/find",
        USER_EXTERNAL_IMPORT : SERVICES.USERMGT  + "/user/import",
        USER_EXTERNAL_STATUS : SERVICES.USERMGT  + "/user/external/status",
        USER_STATUS          : SERVICES.USERMGT  + "/user/status",
        USER_DELETE          : SERVICES.USERMGT  + "/user/delete",
        USER_UPDATE          : SERVICES.USERMGT  + "/user/update",
        USER_CREATE          : SERVICES.USERMGT  + "/user/create",
        USER_CURRENT         : SERVICES.USERMGT  + "/user/current",
        USER_ACCESS_LEVEL    : SERVICES.USERMGT  + "/user/accessLevel",

        //Role management
        ROLE_FIND                  : SERVICES.ROLEMGT + "/role/find",
        ROLE_DELETE                : SERVICES.ROLEMGT + "/role/delete",
        ROLE_UPDATE                : SERVICES.ROLEMGT + "/role/update",
        ROLE_CREATE                : SERVICES.ROLEMGT + "/role/create",
        ROLE_AVAILABLE_USERS       : SERVICES.ROLEMGT + "/role/availableUsers",
        ROLE_DELETE_VALIDATE       : SERVICES.ROLEMGT + "/role/validateForDelete",
        ROLE_REMOVE_USERS_VALIDATE : SERVICES.ROLEMGT + "/role/validateDeleteUsers",

        //Category Management
        CAT_ALL                 : SERVICES.CATMGT + "/category/all",
        CAT_UPDATE              : SERVICES.CATMGT + "/category/update",
        CAT_LOCK_INFO           : SERVICES.CATMGT + "/category/lockinfo",
        CAT_LOCK_TAB            : SERVICES.CATMGT + "/category/locktab",
        CAT_REMOVE_TAB_LOCK     : SERVICES.CATMGT + "/category/removelocktab",
        CAT_UPDATE_IN_DTS       : SERVICES.CATMGT + "/category/updateindts",

        //Utility
        UTIL_GET_MAXINTERVAL   : SERVICES.UTILS + "/utility/maxInactiveInterval",
        UTIL_GET_PRIVATE_KEYS  : SERVICES.UTILS + "/utility/privatekeys",
        UTIL_ENCRYPT_STRING    : SERVICES.UTILS + "/utility/encryptstring",
        UTIL_ENCRYPT_STRINGS   : SERVICES.UTILS + "/utility/encryptstrings",
        UTIL_DECRYPT_STRING    : SERVICES.UTILS + "/utility/decryptstring",
        UTIL_LOG_DATA          : SERVICES.UTILS + "/utility/log",


        //Item Service
        ITEM_REVISIONS        : SERVICES.ITEMMGT + "/item/revisions",
        ITEM_PROMOTE_REVISION : SERVICES.ITEMMGT + "/item/restoreRevision",
        ITEM_SETDATES         : SERVICES.ITEMMGT + "/item/setitemdates",
        ITEM_GETDATES         : SERVICES.ITEMMGT + "/item/getitemdates",
        ITEM_LAST_COMMENT     : SERVICES.ITEMMGT + "/item/lastComment",
        ASSET_SITE_IMPACT     : SERVICES.ITEMMGT + "/item/siteimpact/asset",
        ADD_TO_MYPAGES        : SERVICES.ITEMMGT + "/item/addtomypages",
        REMOVE_FROM_MYPAGES   : SERVICES.ITEMMGT + "/item/removefrommypages",
        IS_MY_PAGE            : SERVICES.ITEMMGT + "/item/ismypage",
        MY_CONTENT            : SERVICES.ITEMMGT + "/item/mycontent",
        ITEM_SETSOPROMETA     : SERVICES.ITEMMGT + "/item/setsoprometadata",
        ITEM_GETSOPROMETA     : SERVICES.ITEMMGT + "/item/getsoprometadata",
        ITEM_LINKED_TO_ITEM   : SERVICES.ITEMMGT + "/item/findLinkedItems",

        //Finder
        ASSETS_ROOT_NO_SLASH   : ASSETS,
        RECYCLING_ROOT_NO_SLASH: RECYCLING,
        SITES_ROOT_NO_SLASH    : SITES,
        DESIGN_ROOT_NO_SLASH   : DESIGN,
        SEARCH_ROOT_NO_SLASH   : SEARCH,
        ASSETS_ROOT            : '/' + ASSETS,
        SITES_ROOT             : '/' + SITES,
        DESIGN_ROOT            : '/' + DESIGN,
        SEARCH_ROOT            : '/' + SEARCH,
        RECYCLING_ROOT         : '/' + RECYCLING,
        FINDER_SEARCH          : SERVICES.FINDER + "/search/get",
        DESIGN_THEMES          : '/' + DESIGN + '/web_resources/themes',

        //Activity Service
        ACTIVITY_CONTENT         : SERVICES.ACTIVITY + "/activity/contentactivity",
        ACTIVITY_TRAFFIC         : SERVICES.ACTIVITY + "/activity/contenttraffic",
        ACTIVITY_TRAFFIC_DETAILS : SERVICES.ACTIVITY + "/activity/trafficdetails",
        ACTIVITY_EFFECTIVENESS   : SERVICES.ACTIVITY + "/activity/effectiveness",

        // Dashboard
        GADGETLIST : PERC_ROOT + "/gadgets/listing",

        // Forms
        ASSET_FORMS             : SERVICES.ASSETMGT + "/asset/forms",
        ASSET_FORMS_EXPORT      : SERVICES.DELIVERY + "/form/submissions",
        ASSET_FORMS_CLEAR       : SERVICES.DELIVERY + "/form",

        // Comments
        COMMENTS_DEFAULT_MODERATION : SERVICES.DELIVERY + "/comment/defaultModerationState",
        COMMENTS_SET_MODERATIONS    : SERVICES.DELIVERY + "/comment/moderate",
        COMMENTS_GET_ALL            : SERVICES.DELIVERY + "/comment/pageswithcomments",
        COMMENTS_GET_PAGE           : SERVICES.DELIVERY + "/comment/commentsonpage",
        COMMENTS_GET_ARTICLE        : SERVICES.PAGEMGT  + "/page/folderpath/Sites",

        // Cookie Consent
        COOKIE_CONSENT_LOG             : SERVICES.DELIVERY + "/consent/log",
        COOKIE_CONSENT_TOTALS          : SERVICES.DELIVERY + "/consent/log/totals",

        // Web Resources
        WEBRESOURCESMGT                        : SERVICES.DESIGN + "/webresources",
        WEBRESOURCESMGT_VALIDATE_FILE_UPLOAD   : SERVICES.DESIGN + "/webresources/validateFileUpload",
        WEBRESOURCESMGT_FILE_UPLOAD            : SERVICES.DESIGN + "/webresources/uploadFile",

        // Folder management
        FOLDERMGT_FOLDERS_WITH_WORKFLOW        : SERVICES.FOLDERMGT + '/folders',
        FOLDERMGT_START_ASSOCIATED_FOLDERS_JOB : SERVICES.FOLDERMGT + '/folders/GetAssociatedFoldersJob/start/',
        FOLDERMGT_GET_ASSOCIATED_FOLDERS_JOB_STATUS : SERVICES.FOLDERMGT + '/folders/GetAssociatedFoldersJob/status/',
        FOLDERMGT_CANCEL_ASSOCIATED_FOLDERS_JOB : SERVICES.FOLDERMGT + '/folders/GetAssociatedFoldersJob/cancel/',
        FOLDERMGT_FOLDERS_WITH_WORKFLOW_ASSIGN : SERVICES.FOLDERMGT + '/folders/workflowassignment',
        FOLDERMGT_IS_WORKFLOW_ASSIGN_PROGRESS : SERVICES.FOLDERMGT + '/folders/workflowassignment/isInProgress',
        FOLDERMGT_FOLDER_PAGES : SERVICES.FOLDERMGT + '/folders/folderpages/id/',

        // Memberships
        MEMBERSHIP_GET_ALL              : SERVICES.DELIVERY + "/membership/admin/users",
        MEMBERSHIP_ACT_BLK_DEL_ACCOUNT  : SERVICES.DELIVERY + "/membership/admin/account",
        MEMBERSHIP_ACT_SAVE_GROUP       : SERVICES.DELIVERY + "/membership/admin/user/group",

        // License
        LICENSE_ACTIVATE            : SERVICES.LICENSE + "/license/activate",
        LICENSE_GET_INFORMATION     : SERVICES.LICENSE + "/license/status",
        LICENSE_GET_NETSUITE_METHOD : SERVICES.LICENSE + "/license/methodWithCheckUrl",
        LICENSE_GET_MODULE          : SERVICES.LICENSE + "/license/module/",
        LICENSE_SAVE_MODULE         : SERVICES.LICENSE + "/license/module/save",

        // Publish mangement
        SERVER_DETAILS          : SERVICES.PUBMGT + "/servers/",
        DEFAULT_PUB_PATH        : SERVICES.PUBMGT + "/servers/defaultFolderLocation/",
        DEFAULT_SERVER_MODIFIED : SERVICES.PUBMGT + "/servers/isDefaultServerModified/",
        STOP_PUB_SERVER         : SERVICES.PUBMGT + "/servers/stopPublishing/",

        // Jobs
        JOB_STATUS              : SERVICES.SHARE + "/jobstatus",

        // Content Migration Management
        TEMPLATE_MIGRATE_CONTENT  : SERVICES.MIGRATIONMGT + "/contentmigration/migrate",

        // Widget management services
        WIDGET_DEFS_SUMMARIES   : SERVICES.WDGMGT + "/widgetbuilder/summaries",
        WIDGET_DEF              : SERVICES.WDGMGT + "/widgetbuilder/definition/",
        WIDGET_DEF_VALIDATE     : SERVICES.WDGMGT + "/widgetbuilder/validate/",
        WIDGET_DEF_DEPLOY       : SERVICES.WDGMGT + "/widgetbuilder/deploy/",

        // Cloud services
        CLDSVC_ACTIVE           : SERVICES.CLDSVC + "/cloudservice/active/",
        CLDSVC_ACTIVE_FMT       : SERVICES.CLDSVC + "/cloudservice/%s/active/",
        CLDSVC_ACTIVESTATES     : SERVICES.CLDSVC + "/cloudservice/activestates/",
        CLDSVC_INFO             : SERVICES.CLDSVC + "/cloudservice/info/",
        CLDSVC_INFO_FMT         : SERVICES.CLDSVC + "/cloudservice/%s/info/",
        CLDSVC_DATA             : SERVICES.CLDSVC + "/cloudservice/pagedata/",
        CLDSVC_DATA_FMT         : SERVICES.CLDSVC + "/cloudservice/%s/pagedata/",

        // Page optimizer management services
        PAGE_OPT_ACTIVE         : SERVICES.PAGEOPTMGT + "/pageoptimizer/active/",
        PAGE_OPT_DATA           : SERVICES.PAGEOPTMGT + "/pageoptimizer/pagedata/",
        PAGE_OPT_INFO           : SERVICES.PAGEOPTMGT + "/pageoptimizer/info/",

        //Recent management services
        RECENT_ROOT              : SERVICES.RECMGT + "/recent/",
        //Redirect management services
        REDIRECT_ROOT              : SERVICES.REDMGT + "/redirect/",
        //Publishing History
        ITEM_PUB_HISTORY        : SERVICES.ITEMMGT + "/item/pubhistory/"
    };
})(jQuery);
