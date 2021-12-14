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
package com.percussion.util;

/**
 * This interface specifies all HTML parameters used and known by Rhythmyx
 * code, applications and extensions.
 */
public interface IPSHtmlParameters
{
   /**
    * The parameter name used to pass the contentid as HTML parameter to
    * HTML requests.
    */
   public static final String SYS_CONTENTID = "sys_contentid";
   
   
   /**
    * The parameter name used to pass the content title as HTML parameter to
    * HTML requests.
    */
   public static final String SYS_TITLE = "sys_title";

   /**
    * The parameter name used to pass the content revision as HTML parameter
    * to HTML requests.
    */
   public static final String SYS_REVISION = "sys_revision";
   
   /**
    * The parameter name used to pass the current content revision as HTML 
    * parameter to HTML requests.
    */
   public static final String SYS_CURRENTREVISION = "sys_currentrevision";

   /**
    * The parameter name used to pass the contextid as HTML parameter
    * to HTML requests.
    */
   public static final String SYS_CONTEXT = "sys_context";

   /**
    * The parameter name used to pass the assembly context ID as HTML parameter
    * to HTML requests.
    */
   public static final String SYS_ASSEMBLY_CONTEXT = "sys_assembly_context";
   
   /**
    * The parameter name used to pass the content variantid as HTML parameter
    * to HTML requests.
    */
   public static final String SYS_VARIANTID = "sys_variantid";

   /**
    * The parameter name used to pass the authorization type as HTML parameter
    * to HTML requests.
    */
   public static final String SYS_AUTHTYPE = "sys_authtype";

   /**
    * The parameter name used to pass the siteid as HTML parameter
    * to HTML requests.
    */
   public static final String SYS_SITEID = "sys_siteid";

   /**
    * The parameter name used to pass the transitionid as HTML parameter
    * to HTML requests.
    */
   public static final String SYS_TRANSITIONID = "sys_transitionid";

   /**
    * The parameter name used to pass the assignment type as HTML parameter
    * to HTML requests.
    */
   public static final String SYS_ASSIGNMENTTYPE = "sys_assignmenttype";
   
   /**
    * The parameter name used to pass the assignment type id as HTML parameter
    * to HTML requests.
    */
   public static final String SYS_ASSIGNMENTTYPEID = "sys_assignmenttypeid";   

   /**
    * The parameter name used to pass the workflow new adhoc user list as HTML
    * parameter to HTML requests.
    */
   public static final String SYS_WF_ADHOC_USERLIST = "sys_wfAdhocUserList";

   /**
    * The parameter name used to pass the slotid to HTML requests.
    */
   public static final String SYS_SLOTID = "sys_slotid";

   /**
    * The parameter name used to pass the slot name to HTML requests.
    */
   public static final String SYS_SLOTNAME = "sys_slotname";

   /**
    * The parameter name used to pass the sortrank to HTML requests.
    */
   public static final String SYS_SORTRANK = "sys_sortrank";

   /**
    * The parameter name used to pass the command to HTML requests.
    */
   public static final String SYS_COMMAND = "sys_command";

   /**
    * The parameter name used to pass the sessionid to HTML requests. The name
    * is required by the server in this form.
    */
   public static final String SYS_SESSIONID = "pssessionid";

   /**
    * The parameter name used to pass the workflowid to HTML requests. The name
    * is required by the cms system in exactly this form.
    */
   public static final String SYS_WORKFLOWID = "sys_workflowid";

   /**
    * The parameter name used to pass the allowed sites list.
    * This is used by the publishing services to check if the assets must be published
    */   
   public static final String SYS_ALLOWEDSITES = "sys_allowed_sites";

   /**
    * The session param name for community id.
    */
   public static final String SYS_COMMUNITY = "sys_community";

   /**
    * The html param name for community id.
    */
   public static final String SYS_COMMUNITYID = "sys_communityid";

   /**
    * The html param name for content item community id.
    */
   public static final String SYS_ITEM_COMMUNITYID = "sys_itemcommunityid";

   /**
    * The html param name for cloned parent id.
    */
   public static final String SYS_CLONEDPARENTID = "sys_clonedparentid";

   /**
    * The optional parameter name used to pass the URL to be used when
    * redirecting after an update request.  Supplying this value takes
    * precedence over any redirect behavior already specified by the application
    * either as a query resource attached to an update resource, or by an
    * application flow specified for a content editor.  This parameter is
    * ignored by query requests, must contain all the parameters it needs as
    * part of its query string (no parameters from the current request context
    * will be propagated), is ignored if a field validation error occurs (if the
    * resource is a content editor), and is ignored if an exception occurs while
    * processing the request.  The resulting value returned to the client using
    * the HTTP_MOVED_TEMPORARILY status code (302).  The value supplied is
    * assumed to be URL-8 encoded and is not validated or modified in any way.
    */
   public static final String DYNAMIC_REDIRECT_URL = "psredirect";

   /**
    * HTML parameter for the current view.
    */
   public static final String SYS_VIEW = "sys_view";

   /**
    * HTML parameter name for the active itemid. This is the parameter used for
    * drilldown feature in the active assembly. The dynamic javascript is
    * generated for this item only. This id is typically the SYSID property
    * value from the relationship tables. Will be empty if the parent item
    * (page) is the active one.
    */
   public static final String SYS_ACTIVEITEMID = "sys_activeitemid";

   /**
    * HTML parameter value to indicate the system is in active assembly mode.
    * This will be the value of the parameter SYS_COMMAND.
    */
   public static final String SYS_ACTIVE_ASSEMBLY = "editrc";
   
   /**
    * HTML parameter name that indicates which active assembly mode
    * that the user has indicated.
    */
   public static final String SYS_ACTIVE_ASSEMBLY_MODE = "sys_aamode";
   
   /**
    * HTML parameter value to indicate icon's shown active assembly mode.
    * This will be the value of the parameter <code>SYS_ACTIVE_ASSEMBLY_MODE</code>.
    */
   public static final String SYS_AAMODE_ICONS = "0";
   
   /**
    * HTML parameter value to indicate icon's <b>not</b> shown active assembly mode.
    * This will be the value of the parameter <code>SYS_ACTIVE_ASSEMBLY_MODE</code>.
    */
   public static final String SYS_AAMODE_NOICONS = "1";
   
   /**
    * The parameter name used to pass the contenttypeidid as HTML parameter to
    * HTML requests.
    */
   public static final String SYS_CONTENTTYPEID = "sys_contenttypeid";

   /**
    * The fieldset name used for related content.
    */
   public static final String RELATED_CONTENT = "relatedcontent";

   /**
    * The parameter name used to pass the relationship type as HTML parameter
    * to HTML requests.
    */
   public static final String SYS_RELATIONSHIPTYPE = "sys_relationshiptype";

   /**
    * The parameter name used to pass the relationshipid as HTML parameter
    * to HTML requests.
    */
   public static final String SYS_RELATIONSHIPID = "sys_relationshipid";

   /**
    * The parameter name used to pass the dependentid as HTML parameter to
    * HTML requests.
    */
   public static final String SYS_DEPENDENTID = "sys_dependentid";

   /**
    * The parameter name used to pass the dependentrevision as HTML parameter
    * to HTML requests.
    */
   public static final String SYS_DEPENDENTREVISION = "sys_dependentrevision";

   /**
    * The parameter name used to pass the dependentvariantid as HTML parameter
    * to HTML requests.
    */
   public static final String SYS_DEPENDENTVARIANTID = "sys_dependentvariantid";

   /**
    * The parameter name used to pass the locale as HTML parameter
    * to HTML requests.
    */
   public static final String SYS_LANG = "sys_lang";

   /**
    * The parameter name used to pass a flag to the workflow command handler
    * telling it that the current processed transition was forced through a
    * strong relationship.
    */
   public static final String SYS_FORCEDEPENDENT = "sys_forcedependent";

   /**
    * The parameter name used to pass the workflow stateid to HTML requests. The
    * name is required by the cms system in exactly this form.
    */
   public static final String SYS_CONTENTSTATEID = "sys_contentstateid";

   /**
    * A flag that controls the PSLocalCataloger. See the 
    * {@link com.percussion.cms.objectstore.server.PSLocalCataloger 
    * class description} for more details.
    */
   public static final String SYS_INCLUDEHIDDENFIELDS =
         "sys_includehiddenfields";

   /**
    * A flag that controls the PSLocalCataloger. See the 
    * {@link com.percussion.cms.objectstore.server.PSLocalCataloger 
    * class description} for more details.
    */
   public static final String SYS_INCLUDERESULTONLYFIELDS =
         "sys_includeresultonlyfields";

   /**
    * A flag that controls the PSLocalCataloger. See the 
    * {@link com.percussion.cms.objectstore.server.PSLocalCataloger 
    * class description} for more details.
    */
   public static final String SYS_RESTRICTFIELDSTOUSERCOMMUNITY =
         "sys_restrictfieldstousercommunity";

   /**
    * A flag that controls the PSLocalCataloger. See the 
    * {@link com.percussion.cms.objectstore.server.PSLocalCataloger 
    * class description} for more details.
    */
   public static final String SYS_USERSEARCH =
         "sys_usersearch";

   /**
    * A flag that controls the PSLocalCataloger. See the 
    * {@link com.percussion.cms.objectstore.server.PSLocalCataloger 
    * class description} for more details.
    */
   public static final String SYS_CTYPESHIDEFROMMENU =
         "sys_ctypeshidefrommenu";

   /**
    * A flag that controls the PSLocalCataloger. See the 
    * {@link com.percussion.cms.objectstore.server.PSLocalCataloger 
    * class description} for more details.
    */
   public static final String SYS_EXCLUDE_CHOICES = "sys_excludechoices";
   

   /**
    * Used by the 
    * {@link com.percussion.cms.objectstore.client.PSRemoteCataloger} to pass
    * the set of field names to catalog
    */
   public static final String SYS_CE_FIELD_NAME = "sys_cefieldname";
   
   /**
    * Html parameter name for publishing edition id
    */
   public static final String SYS_EDITIONID =
         "sys_editionid";

   /**
    * Html parameter name for publishing mirror srcsite id
    */
   public static final String SYS_SRCSITEID =
         "sys_srcsiteid";

   /**
    * Html parameter name for publishing recovery pubstatus id
    */
   public static final String SYS_PUBSTATUSID =
         "sys_pubstatusid";

   /**
    * Html parameter name for the publication id
    */
   public static final String SYS_PUBLICATIONID =
         "sys_publicationid";

   /**
    * Html paramter name to supply a community id to override the current
    * community selection.
    */
   public static final String SYS_OVERRIDE_COMMUNITYID =
      "sys_overridecommunityid";
   
   /**
    * The parameter name used to pass the objecttype as HTML parameter to
    * HTML requests.
    */
   public static final String SYS_OBJECTTYPE = "sys_objecttype";

   /**
    * Html parameter name to supply the validation error
    * (in <code>String</code>) if there is one caused by processing current
    * request.
    */
   public static final String SYS_VALIDATION_ERROR = "sys_validation_error";
   
   /**
    * The parameter name used to pass the folderid as HTML parameter to
    * HTML requests.
    */
   public static final String SYS_FOLDERID = "sys_folderid";

   /**
    * The parameter name used to pass the asset folderid as HTML parameter
    * to HTML requests.
    */
   public static final String SYS_ASSET_FOLDERID = "sys_asset_folderid";
   
   /**
    * Html parameter that when set to true will cause the server to not send
    * a redirect if asked, but instead just put the redirect url into the
    * responses content body. This means the browser will not handle the
    * redirect. Also adds special response so that webImageFx will think
    * the upload is succesful.
    */
   public static final String WIFXUPLOAD = "webimagefxupload";

   /**
    * Html parameter name to tell the server to default the user's community
    * when the community (normally from {@link #SYS_OVERRIDE_COMMUNITYID}
    * ) is invalid.
    */
   public static final String SYS_FALLBACK_COMMUNITYID =
      "sys_fallbackcommunityid";
      
   /**
    * The HTML parameter name used to pass the selected search definition to
    * HTML requests.
    */
   public static final String SYS_SEARCHID = "sys_searchid";

   /**
    * HTML parameter name used to override the "showInPreview" attribute of the
    * fields in Content Editor. If its value is "yes", then always set
    * "showInPreview" attribute to "yes"; otherwise not to override the
    * attribute value. The value is case insensitive
    */
   public static final String SYS_SHOW_IN_PREVIEW = "sys_showInPreview";

   /**
    * Html parameter named used by html search to pass the selected display
    * format id.
    */
   public static final String SYS_DISPLAYFORMATID = "sys_displayformatid";
   
   /**
    * Html parameter named used by html search to pass the maximum number of
    * search results to return.
    */
   public static final String SYS_MAXIMUM_SEARCH_RESULTS = 
      "sys_maxsearchresults";
   
   /**
    * Html parameter named used by html search to pass the full text query
    * string.
    */
   public static final String SYS_FULLTEXTQUERY = "sys_fulltextquery";

   /**
    * Html parameter named used by html search to pass the lucene query
    * synonym expansion setting.
    */
   public static final String SYS_SYNONYM_EXPANSION =
      "sys_synonymexpansion";
   
   /**
    * The HTML parameter name used for inline slot id's.
    */
   public static final String SYS_INLINE_SLOTID = "inlineslotid";
   
   /**
    * The HTML parameter used to submit the requested search mode.
    */
   public static final String SYS_SEARCH_MODE = "sys_searchMode";    

   /**
    * HTML parameter used to signal an inline link data update. In some cases 
    * while inline links are processed we need to update field data with
    * inline link information. In such cases we want to skip inline link 
    * processing. To do that provide this parameter set to <code>yes</code>,
    * case insensitive.
    */
   public static final String SYS_INLINELINK_DATA_UPDATE = 
      "sys_inlineLinksDataUpdate";
   
   /**
    * The HTML parameter used to pass the case sensitive setting of a search.
    */
   public static final String SYS_IS_SEARCH_CASE_SENSITIVE = "sys_isCaseSensitive";
   
   /**
    * Set this parameter to true if the request should not take on the community
    * of the content item being referenced. Only active for the action page at 
    * this time.
    */
   public static final String SYS_STICKY_COMMUNITY = "sys_stickycommunity";

   /**
    * HTML parameter used to pass the original siteid all along the assembly 
    * process. This is used for generating cross-site links in the pages or 
    * snippets.
    */
   public static final String SYS_ORIGINALSITEID = "sys_originalsiteid";
   
   /**
    * Parameter used to disable the rxs_NavFolderEffect.  Supply "y" as the 
    * case-sensitive value to disable this effect when saving folder contents
    * relationships.
    */
   public static final String RXS_DISABLE_NAV_FOLDER_EFFECT = 
      "rxs_disableNavFolderEffect";
   
   /**
    * Parameter is set by the folder processor when a item or folder is moved from
    * one folder to another folder. The parameter is the source folders content id.
    * 
    * This can be used by Folder Effects that need to know what the source folder
    * was.
    */
   public static final String SYS_MOVE_SOURCE_FOLDER_ID = "sys_moveSourceFolderId";
   
   /**
    * Parameter is set by the folder processor when a item or folder is moved from
    * one folder to another folder. The parameter is the target folders content id.
    * 
    * This can be used by Folder Effects that need to know what the target folder
    * is.
    */
   public static final String SYS_MOVE_TARGET_FOLDER_ID = "sys_moveTargetFolderId";
   
   /**
    * Parameter used to override the sys_title field when cloning an item. 
    */
   public static final String SYS_TITLE_OVERRIDE = "sys_title_override";
   
   /**
    * Parameter used to override the sys_communityid field when cloning an item. 
    */
   public static final String SYS_COMMUNITYID_OVERRIDE = 
      "sys_communityid_override";
   
   /**
    * Parameter used to override the sys_workflowid field when cloning an item. 
    */
   public static final String SYS_WORKFLOWID_OVERRIDE = 
      "sys_workflowid_override";
   
   /**
    * Img related attribute names.
    */
   public final static String ATTR_WIDTH = "width"; 
   public final static String ATTR_RX_WIDTH = "rxwidth";
   public final static String ATTR_HEIGHT = "height";
   public final static String ATTR_RX_HEIGHT = "rxheight";
   
   /**
    * The HTML parameter used to turn off the assembler cache when performing
    * a request against a resource. The requested result will not by cached
    * by the assembler cache if the value of this parameter equals
    * {@link com.percussion.cms.IPSConstants#BOOLEAN_TRUE}.
    * <p>
    * Note: {@link #SYS_IS_PAGE_CACHE_OFF} parameter superseds this parameter.
    */
   public static final String SYS_IS_ASSEMBLER_CACHE_OFF =
      "sys_isAssemblerCacheOff";

   /**
    * The HTML parameter used to turn off the resource cache when performing
    * a request against a resource. The requested result will not be cached
    * by the resource cache if the value of this parameter equals
    * {@link com.percussion.cms.IPSConstants#BOOLEAN_TRUE}.
    * <p>
    * Note: {@link #SYS_IS_PAGE_CACHE_OFF} parameter superseds this parameter.
    */
   public static final String SYS_IS_RESOURCE_CACHE_OFF =
      "sys_isResourceCacheOff";

   /**
    * The HTML parameter used to turn off the page cache when performing a
    * request against a resource. The requested result will not be cached by
    * the page cache if the value of this parameter equals
    * {@link com.percussion.cms.IPSConstants#BOOLEAN_TRUE}.
    * <p>
    * Note: the page cache includes both assembler and resource cache. This
    * parameter superseds both {@link #SYS_IS_ASSEMBLER_CACHE_OFF} and
    * {@link #SYS_IS_RESOURCE_CACHE_OFF}.
    */
   public static final String SYS_IS_PAGE_CACHE_OFF =
      "sys_isPageCacheOff";
   
   /**
    * This HTML parameter is used with internal requests. If set to
    * {@link com.percussion.cms.IPSConstants#BOOLEAN_TRUE}, internal update
    * requests will execute all post exits and send a response. If not present
    * or for any other value, internal update requests will not execute the post
    * exits and not send a response.
    */
   public static final String SYS_SENDUPDATERESPONSE = "sys_sendupdateresponse";
   
   /**
    * The HTML parameter used to pass parent folder path to search results
    * processing extensions. This is a read-only parameter that the extension can
    * use for processing the result rows. The request context in the extension
    * will contain this parameter if the search was performed in the context of
    * a parent folder. The value of this parameter if present, will be a fully
    * qualified path of the folder the search is performed under.
    */
   public static final String SYS_PARENTFOLDERPATH = "sys_parentFolderPath";
   
   /**
    * This HTML parameter is used to pass the content path for the item to be 
    * assembled
    */
   public static final String SYS_PATH = "sys_path";   
   
   /**
    * This HTML parameter is used to pass the name of the variant to use
    * in assembly. This name must be unique.
    */
   public static final String SYS_VARIANT = "sys_variant";
   
   /**
    * Header name used to indicate that the request should attempt basic
    * authorization rather than form based.
    */
   public static final String SYS_USE_BASIC_AUTH = "RX_USEBASICAUTH";

   /**
    * This HTTP parameter is used to pass the URL protocol to use when 
    * constructing the site folder publishing content list urls.
    */
   public static final String SYS_PROTOCOL = "sys_protocol";
   
   /**
    * This HTTP parameter is used to pass the host name or ip address to use
    * when constructing the site folder publishing content list urls.
    */
   public static final String SYS_HOST = "sys_host";
   
   /**
    * This HTTP parameter is used to pass the port to use
    * when constructing the site folder publishing content list urls.
    */
   public static final String SYS_PORT = "sys_port";

   /**
    * This HTTP parameter is used to specify the item filter to use during
    * assembly.
    */
   public static final String SYS_ITEMFILTER = "sys_itemfilter";

   /**
    * This HTTP parameter specifies the name of a template to be used to 
    * assemble a content item
    */
   public static final String SYS_TEMPLATE = "sys_template";

   /**
    * The resource definition id specifies the resource used to assembly
    * links and content.
    */
   public static final String PERC_RESOURCE_DEFINITION_ID = "perc_resource_definition_id";

   /**
    * This HTTP parameter tells the recipient to run in debug mode
    */
   public static final String SYS_DEBUG = "sys_debug";

   /**
    * This HTTP parameter controls the mode that the assembly manager is 
    * running in. The value "AA" causes the fields and slots to include 
    * controls. Otherwise the template is rendered without controls. The
    * value "AA_Link" causes the links assembled to include sys_command=editrc.
    */
   public static final String SYS_MODE = "sys_mode";

   /**
    * Use this HTTP parameter to save the original folder id when we need to
    * override. Used by the attach clone to folder effect to save the original
    * information in the relationship command handler to allow the right
    * folder to become the parent.
    */
   public static final String SYS_ORIGINALFOLDERID = "sys_originalFolderId";

   /**
    * Use this HTTP parameter to indicate what publishing plug in should receive
    * the assembled content.
    */
   public static final String SYS_DELIVERYTYPE = "sys_deliverytype";

   /**
    * An HTTP parameter to indicates if the content list should be published
    * or unpublished. 
    */
   public static final String SYS_PUBLISH = "sys_publish";

   /**
    * An HTTP parameter used to specify the content list name to execute
    */
   public static final String SYS_CONTENTLIST = "sys_contentlist";

   /**
    * An HTTP parameter that specifies a context to use for 
    * building a content list. The associated value will replace the
    * context supplied for assembly
    */
   public static final String SYS_DELIVERY_CONTEXT = "sys_delivery_context";
   
   /**
    * A publisher supplied HTTP parameter that limits the results from a content
    * list.
    */
   public static final String MAXRESULTSPERPAGE = "maxrowsperpage";
   
   /**
    * The HTTP parameter that tells the content list servlet what page to 
    * returned in paged mode.
    */
   public static final String SYS_PAGE = "psfirst";

   /**
    * The request/HTTP parameter that indicates whether to bind 
    * "$sys.page_suffix" to the page number of assembled/paginated item when 
    * generating a location using a JEXL expression. 
    */
   public static final String SYS_USE_PAGE_SUFFIX = "sys_usePageSuffix";
   
   /**
    * THe HTTP parameter that holds the hibernate version from the contentstatus
    * table.
    */
   public static final String SYS_HIBERNATEVERSION = "sys_hibernateVersion";
   
   /**
    * This parameter is used to inform the checkout process to always check
    * out the same revision (not a copy of the current revision). In another
    * words, the checkout process will ignore the revisionLock or the revision
    * number if this flag is on.
    * <p> 
    * The checkout same revision flag is on only if the value of the parameter 
    * is {@link com.percussion.cms.IPSConstants#BOOLEAN_TRUE}. 
    */
   public static final String SYS_CHECKOUT_SAME_REVISION = "sys_checkoutSameRevision";
   
   /**
    * Name of the param/field that specifies a workflow statename.
    */
   public static final String SYS_STATE_NAME = "sys_statename";   
   
   /**
    * Parameter to indicate a partial rendering of a page. The value of the
    * parameter, if present, consists of "part:name" where <em>part</em>
    * indicates what part to render and <em>name</em> indicates what the
    * element of that part is. For example "slot:rffList" indicates that the
    * <em>rffList</em> slot should be rendered.
    */
   public static final String SYS_PART = "sys_part";
   
   /**
    * Name of the param/field that specifies a Content Editor field name.
    */
   public static final String SYS_FIELD_NAME = "sys_fieldname";
   
   /**
    * The permissions associated with a folder (may be extended to items at
    * some point). Used by the CX and search system to report the information
    * back.
    */
   public static final String SYS_PERMISSIONS = "sys_permissions";
   
   /**
    * This is really not a parameter, but rather the name of the slot/filter
    * parameter passed by assembly for the purposes of detecting when the 
    * revision of an item should be chosen as the edit revision as the 
    * specified user has the item checked out.
    */
   public static final String SYS_USER = "sys_user";

   /**
    * Assembly parameter used internally as hint that the item being assembled
    * is going into an AA slot (finder is Active Assembly).
    */
   public static final String SYS_FORAASLOT = "sys_foraaslot";
   
   /**
    * Parameter used for setting the cached page url when a field validation
    * error occurs while submitting the content item.
    */
   public static final String SYS_CE_CACHED_PAGEURL = "sys_ceCachedPageUrl";
  
   /**
    * The name of the request parameter for specifying a publishing job ID.
    * This is typically used in a URL for viewing a publishing job.
    */
   public static final String PUBLISH_JOB_ID = "sys_publishingJobId";
 
   /**
    * Initially used by the Impact analyzer. If supplied in the request, only
    * those relationships whose category matches the supplied category will be
    * shown (case-insensitive.)
    */
   public static final String SYS_RELATIONSHIP_CATEGORY_FILTER = 
      "sys_relationshipCategoryFilter";
   
   /**
    * HTML parameter name used to pass the redirect url value when requesting
    * the login page. This is used to record the original request that the user was attempting
    * when redirected to the login page while doing form based authentication.
    */
   public static final String SYS_REDIRECT = "sys_redirect";
   
   /**
    * The html parameter which defines how the uploaded xml document should be
    * treated. The valid expected values for this parameter are 'useValidating',
    * 'useNonValidating' and 'treatAsText'. If the request has multiple xml
    * documents to be uploaded, then this parameter should have values for each
    * xml document delimited by ';'.
    */
   public static final String REQ_XML_DOC_FLAG = "psxmldoc";

   /**
    * The constant to define that the xml document uploaded should be treated as
    * text. So it should be mapped to a parameter.
    */
   public static final String XML_DOC_AS_TEXT = "treatAsText";

   /**
    * The parameter of a content list to indicate whether to un-publish an item
    * from its previous published location. The publishing job will un-publish
    * an item from its previous published location if the value of the parameter
    * is <code>true</code>.
    */
   public static final String SYS_UNPUBLISH_CHANGED_LOCATION = "sys_unpublishChangedLocation";

   /*
    * Sets the api client used to do the request e.g. "WebDav"  can be used
    * to use different validation if the request has not been made through browser.
    */
   public static final String SYS_CLIENT = "sys_client";
   
   /**
    * The html param name for which content explorer ui to use, stored in session
    */
  
   public static final String SYS_UI = "sys_ui";
 /**
    * The key of the private object on the request, used to overwrite the URL generator in preview mode.
    * The (private) object contains 2 values (in String[2] object). 
    * The <b>1st value</b> is a required value, which is the fully qualified UDF name of the generator, context and name of the UDF.
    * The <b>2nd value</b> is an optional value, a template ID (if specified) in the link, 
    * where we want to use the default link generator instead of the specified one. 
    * For example, the 2nd value may be the ID of a thumbnail template, where we want to use the default preview URL generator, 
    * but not the specified link generator. 
    */
   public static final String SYS_OVERWRITE_PREVIEW_URL_GEN = "sys_overwritePreviewUrlGenerator";

}
